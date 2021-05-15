package com.bugsnag.android

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
    FAILURE
}
