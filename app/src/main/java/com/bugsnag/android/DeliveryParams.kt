package com.bugsnag.android

/**
 * The parameters which should be used to deliver an Event/Session.
 */
class DeliveryParams(

    /**
     * The endpoint to which the payload should be sent
     */
    val endpoint: String,

    /**
     * The HTTP headers which must be attached to the request
     */
    val headers: Map<String, String?>
)
