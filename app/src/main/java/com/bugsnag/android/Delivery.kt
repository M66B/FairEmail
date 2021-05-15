package com.bugsnag.android

/**
 * Implementations of this interface deliver Error Reports and Sessions captured to the Bugsnag API.
 *
 * A default [Delivery] implementation is provided as part of Bugsnag initialization,
 * but you may wish to use your own implementation if you have requirements such
 * as pinning SSL certificates, for example.
 *
 * Any custom implementation must be capable of sending
 * [Error Reports](https://docs.bugsnag.com/api/error-reporting/)
 * and [Sessions](https://docs.bugsnag.com/api/sessions/) as
 * documented at [https://docs.bugsnag.com/api/](https://docs.bugsnag.com/api/)
 *
 * @see DefaultDelivery
 */
interface Delivery {

    /**
     * Posts an array of sessions to the Bugsnag Session Tracking API.
     *
     * This request must be delivered to the endpoint specified in [deliveryParams] with the given
     * HTTP headers.
     *
     * You should return the [DeliveryStatus] which best matches the end-result of your delivery
     * attempt. Bugsnag will use the return value to decide whether to delete the payload if it was
     * cached on disk, or whether to reattempt delivery later on.
     *
     * For example, a 2xx status code will indicate success so you should return
     * [DeliveryStatus.DELIVERED]. Most 4xx status codes would indicate an unrecoverable error, so
     * the report should be dropped using [DeliveryStatus.FAILURE]. For all other scenarios,
     * delivery should be attempted again later by using [DeliveryStatus.UNDELIVERED].
     *
     * See [https://docs.bugsnag.com/api/sessions/](https://docs.bugsnag.com/api/sessions/)
     *
     * @param payload        The session tracking payload
     * @param deliveryParams The delivery parameters to be used for this request
     * @return the end-result of your delivery attempt
     */
    fun deliver(payload: Session, deliveryParams: DeliveryParams): DeliveryStatus

    /**
     * Posts an Error Report to the Bugsnag Error Reporting API.
     *
     * This request must be delivered to the endpoint specified in [deliveryParams] with the given
     * HTTP headers.
     *
     * You should return the [DeliveryStatus] which best matches the end-result of your delivery
     * attempt. Bugsnag will use the return value to decide whether to delete the payload if it was
     * cached on disk, or whether to reattempt delivery later on.
     *
     * For example, a 2xx status code will indicate success so you should return
     * [DeliveryStatus.DELIVERED]. Most 4xx status codes would indicate an unrecoverable error, so
     * the report should be dropped using [DeliveryStatus.FAILURE]. For all other scenarios,
     * delivery should be attempted again later by using [DeliveryStatus.UNDELIVERED].
     *
     * See [https://docs.bugsnag.com/api/error-reporting/]
     * (https://docs.bugsnag.com/api/error-reporting/)
     *
     * @param payload         The error payload
     * @param deliveryParams The delivery parameters to be used for this request
     * @return the end-result of your delivery attempt
     */
    fun deliver(payload: EventPayload, deliveryParams: DeliveryParams): DeliveryStatus
}
