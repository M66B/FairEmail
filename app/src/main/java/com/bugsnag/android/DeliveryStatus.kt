package com.bugsnag.android

import java.net.HttpURLConnection.HTTP_BAD_REQUEST
import java.net.HttpURLConnection.HTTP_CLIENT_TIMEOUT
import java.net.HttpURLConnection.HTTP_OK

/**
 * Return value for the status of a payload delivery.
 */
enum class DeliveryStatus {

    /**
     * The payload was delivered successfully and can be deleted.
     */
    DELIVERED,

    /**
     * The payload was not delivered but can be retried, e.g. when there was a loss of connectivity
     */
    UNDELIVERED,

    /**
     *
     * The payload was not delivered and should be deleted without attempting retry.
     */
    FAILURE;

    companion object {
        @JvmStatic
        fun forHttpResponseCode(responseCode: Int): DeliveryStatus {
            return when {
                responseCode in HTTP_OK..299 -> DELIVERED
                responseCode in HTTP_BAD_REQUEST..499 && // 400-499 are considered unrecoverable
                    responseCode != HTTP_CLIENT_TIMEOUT && // except for 408
                    responseCode != 429 -> FAILURE

                else -> UNDELIVERED
            }
        }
    }
}
