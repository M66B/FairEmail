package com.bugsnag.android;

import androidx.annotation.NonNull;

/**
 * Add a "on breadcrumb" callback, to execute code before every
 * breadcrumb captured by Bugsnag.
 * <p>
 * You can use this to modify breadcrumbs before they are stored by Bugsnag.
 * You can also return <code>false</code> from any callback to ignore a breadcrumb.
 * <p>
 * For example:
 * <p>
 * Bugsnag.onBreadcrumb(new OnBreadcrumbCallback() {
 * public boolean onBreadcrumb(Breadcrumb breadcrumb) {
 * return false; // ignore the breadcrumb
 * }
 * })
 */
public interface OnBreadcrumbCallback {

    /**
     * Runs the "on breadcrumb" callback. If the callback returns
     * <code>false</code> any further OnBreadcrumbCallback callbacks will not be called
     * and the breadcrumb will not be captured by Bugsnag.
     *
     * @param breadcrumb the breadcrumb to be captured by Bugsnag
     * @see Breadcrumb
     */
    boolean onBreadcrumb(@NonNull Breadcrumb breadcrumb);

}
