package com.bugsnag.android

import android.net.TrafficStats
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.PrintWriter
import java.net.HttpURLConnection
import java.net.HttpURLConnection.HTTP_BAD_REQUEST
import java.net.HttpURLConnection.HTTP_CLIENT_TIMEOUT
import java.net.HttpURLConnection.HTTP_OK
import java.net.URL

/**
 * Converts a [JsonStream.Streamable] into JSON, placing it in a [ByteArray]
 */
internal fun serializeJsonPayload(streamable: JsonStream.Streamable): ByteArray {
    return ByteArrayOutputStream().use { baos ->
        JsonStream(PrintWriter(baos).buffered()).use(streamable::toStream)
        baos.toByteArray()
    }
}

internal class DefaultDelivery(
    private val connectivity: Connectivity?,
    val logger: Logger
) : Delivery {

    override fun deliver(payload: Session, deliveryParams: DeliveryParams): DeliveryStatus {
        val status = deliver(deliveryParams.endpoint, payload, deliveryParams.headers)
        logger.i("Session API request finished with status $status")
        return status
    }

    override fun deliver(payload: EventPayload, deliveryParams: DeliveryParams): DeliveryStatus {
        val status = deliver(deliveryParams.endpoint, payload, deliveryParams.headers)
        logger.i("Error API request finished with status $status")
        return status
    }

    fun deliver(
        urlString: String,
        streamable: JsonStream.Streamable,
        headers: Map<String, String?>
    ): DeliveryStatus {

        TrafficStats.setThreadStatsTag(1)
        if (connectivity != null && !connectivity.hasNetworkConnection()) {
            return DeliveryStatus.UNDELIVERED
        }
        var conn: HttpURLConnection? = null

        try {
            val json = serializeJsonPayload(streamable)
            conn = makeRequest(URL(urlString), json, headers)

            // End the request, get the response code
            val responseCode = conn.responseCode
            val status = getDeliveryStatus(responseCode)
            logRequestInfo(responseCode, conn, status)
            return status
        } catch (oom: OutOfMemoryError) {
            // attempt to persist the payload on disk. This approach uses streams to write to a
            // file, which takes less memory than serializing the payload into a ByteArray, and
            // therefore has a reasonable chance of retaining the payload for future delivery.
            logger.w("Encountered OOM delivering payload, falling back to persist on disk", oom)
            return DeliveryStatus.UNDELIVERED
        } catch (exception: IOException) {
            logger.w("IOException encountered in request", exception)
            return DeliveryStatus.FAILURE
        } catch (exception: Exception) {
            logger.w("Unexpected error delivering payload", exception)
            return DeliveryStatus.FAILURE
        } finally {
            conn?.disconnect()
        }
    }

    private fun makeRequest(
        url: URL,
        json: ByteArray,
        headers: Map<String, String?>
    ): HttpURLConnection {
        val conn = url.openConnection() as HttpURLConnection
        conn.doOutput = true

        // avoids creating a buffer within HttpUrlConnection, see
        // https://developer.android.com/reference/java/net/HttpURLConnection
        conn.setFixedLengthStreamingMode(json.size)

        // calculate the SHA-1 digest and add all other headers
        computeSha1Digest(json)?.let { digest ->
            conn.addRequestProperty(HEADER_BUGSNAG_INTEGRITY, digest)
        }
        headers.forEach { (key, value) ->
            if (value != null) {
                conn.addRequestProperty(key, value)
            }
        }

        // write the JSON payload
        conn.outputStream.use {
            it.write(json)
        }
        return conn
    }

    private fun logRequestInfo(code: Int, conn: HttpURLConnection, status: DeliveryStatus) {
        runCatching {
            logger.i(
                "Request completed with code $code, " +
                    "message: ${conn.responseMessage}, " +
                    "headers: ${conn.headerFields}"
            )
        }
        runCatching {
            conn.inputStream.bufferedReader().use {
                logger.d("Received request response: ${it.readText()}")
            }
        }

        runCatching {
            if (status != DeliveryStatus.DELIVERED) {
                conn.errorStream.bufferedReader().use {
                    logger.w("Request error details: ${it.readText()}")
                }
            }
        }
    }

    internal fun getDeliveryStatus(responseCode: Int): DeliveryStatus {
        return when {
            responseCode in HTTP_OK..299 -> DeliveryStatus.DELIVERED
            isUnrecoverableStatusCode(responseCode) -> DeliveryStatus.FAILURE
            else -> DeliveryStatus.UNDELIVERED
        }
    }

    private fun isUnrecoverableStatusCode(responseCode: Int) =
        responseCode in HTTP_BAD_REQUEST..499 && // 400-499 are considered unrecoverable
            responseCode != HTTP_CLIENT_TIMEOUT && // except for 408
            responseCode != 429 // and 429
}
