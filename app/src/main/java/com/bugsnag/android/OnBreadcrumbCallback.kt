package com.bugsnag.android

/**
 * Add a "on breadcrumb" callback, to execute code before every
 * breadcrumb captured by Bugsnag.
 *
 *
 * You can use this to modify breadcrumbs before they are stored by Bugsnag.
 * You can also return `false` from any callback to ignore a breadcrumb.
 *
 *
 * For example:
 *
 *
 * Bugsnag.onBreadcrumb(new OnBreadcrumbCallback() {
 * public boolean onBreadcrumb(Breadcrumb breadcrumb) {
 * return false; // ignore the breadcrumb
 * }
 * })
 */
fun interface OnBreadcrumbCallback {
    /**
     * Runs the "on breadcrumb" callback. If the callback returns
     * `false` any further OnBreadcrumbCallback callbacks will not be called
     * and the breadcrumb will not be captured by Bugsnag.
     *
     * @param breadcrumb the breadcrumb to be captured by Bugsnag
     * @see Breadcrumb
     */
    fun onBreadcrumb(breadcrumb: Breadcrumb): Boolean
}
