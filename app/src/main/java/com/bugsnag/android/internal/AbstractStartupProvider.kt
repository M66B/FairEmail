package com.bugsnag.android.internal

import android.annotation.SuppressLint
import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION_CODES

/**
 * Empty `ContentProvider` used for early loading / startup processing.
 */
abstract class AbstractStartupProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        return true
    }

    final override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor? {
        checkPrivilegeEscalation()
        return null
    }

    final override fun getType(uri: Uri): String? {
        checkPrivilegeEscalation()
        return null
    }

    final override fun insert(uri: Uri, values: ContentValues?): Uri? {
        checkPrivilegeEscalation()
        return null
    }

    final override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        checkPrivilegeEscalation()
        return 0
    }

    final override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int {
        checkPrivilegeEscalation()
        return 0
    }

    @SuppressLint("NewApi")
    protected fun checkPrivilegeEscalation() {
        if (Build.VERSION.SDK_INT !in (VERSION_CODES.O..VERSION_CODES.P)) {
            return
        }

        val caller = callingPackage
        if (caller != null && caller == context?.packageName) {
            return
        }

        throw SecurityException("Provider does not allow Uri permissions to be granted")
    }
}
