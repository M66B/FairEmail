package com.bugsnag.android

import android.net.TrafficStats
import com.bugsnag.android.internal.JsonHelper
import java.io.IOException
import java.net.HttpURLConnection
import java.net.HttpURLConnection.HTTP_BAD_REQUEST
import java.net.HttpURLConnection.HTTP_CLIENT_TIMEOUT
import java.net.HttpURLConnection.HTTP_OK
import java.net.URL

internal class DefaultDelivery(
    private val connectivity: Connectivity?,
    private val apiKey: String,
    private val maxStringValueLength: Int,
    private val logger: Logger
) : Delivery {

    companion object {
        // 1MB with some fiddle room in case of encoding overhead
        const val maxPayloadSize = 999700
    }

    override fun deliver(payload: Session, deliveryParams: DeliveryParams): DeliveryStatus {
        val status = deliver(
            deliveryParams.endpoint,
            JsonHelper.serialize(payload),
            deliveryParams.headers
        )
        logger.i("Session API request finished with status $status")
        return status
    }

    private fun serializePayload(payload: EventPayload): ByteArray {
        var json = JsonHelper.serialize(payload)
        if (json.size <= maxPayloadSize) {
            return json
        }

        var event = payload.event
        if (event == null) {
            event = MarshalledEventSource(payload.eventFile!!, apiKey, logger).invoke()
            payload.event = event
            payload.apiKey = apiKey
        }

        val (itemsTrimmed, dataTrimmed) = event.impl.trimMetadataStringsTo(maxStringValueLength)
        event.impl.internalMetrics.setMetadataTrimMetrics(
            itemsTrimmed,
            dataTrimmed
        )

        json = JsonHelper.serialize(payload)
        if (json.size <= maxPayloadSize) {
            return json
        }

        val breadcrumbAndBytesRemovedCounts =
            event.impl.trimBreadcrumbsBy(json.size - maxPayloadSize)
        event.impl.internalMetrics.setBreadcrumbTrimMetrics(
            breadcrumbAndBytesRemovedCounts.itemsTrimmed,
            breadcrumbAndBytesRemovedCounts.dataTrimmed
        )
        return JsonHelper.serialize(payload)
    }

    override fun deliver(payload: EventPayload, deliveryParams: DeliveryParams): DeliveryStatus {
        val json = serializePayload(payload)
        val status = deliver(deliveryParams.endpoint, json, deliveryParams.headers)
        logger.i("Error API request finished with status $status")
        return status
    }

    fun deliver(
        urlString: String,
        json: ByteArray,
        headers: Map<String, String?>
    ): DeliveryStatus {

        TrafficStats.setThreadStatsTag(1)
        if (connectivity != null && !connectivity.hasNetworkConnection()) {
            return DeliveryStatus.UNDELIVERED
        }
        var conn: HttpURLConnection? = null

        try {
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
            return DeliveryStatus.UNDELIVERED
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
