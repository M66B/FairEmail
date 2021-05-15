package com.bugsnag.android

/**
 * Set the endpoints to send data to. By default we'll send error reports to
 * https://notify.bugsnag.com, and sessions to https://sessions.bugsnag.com, but you can
 * override this if you are using Bugsnag Enterprise to point to your own Bugsnag endpoints.
 */
class EndpointConfiguration(

    /**
     * Configures the endpoint to which events should be sent
     */
    val notify: String = "https://notify.bugsnag.com",

    /**
     * Configures the endpoint to which sessions should be sent
     */
    val sessions: String = "https://sessions.bugsnag.com"
)
