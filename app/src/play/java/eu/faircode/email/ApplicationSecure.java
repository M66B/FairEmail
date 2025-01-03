package eu.faircode.email;

/*
    This file is part of FairEmail.

    FairEmail is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FairEmail is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with FairEmail.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2018-2025 by Marcel Bokhorst (M66B)
*/

import android.content.Intent;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.google.android.gms.security.ProviderInstaller;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ApplicationSecure extends ApplicationEx implements ProviderInstaller.ProviderInstallListener {
    private static final CountDownLatch lock = new CountDownLatch(1);

    private static final long WAIT_INSTALLED = 750L; // milliseconds

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean ssl_update = prefs.getBoolean("ssl_update", true);
        if (ssl_update) {
            Log.i("Security provider check");
            ProviderInstaller.installIfNeededAsync(this, this);
        } else
            lock.countDown();
    }

    @Override
    public void onProviderInstalled() {
        Log.i("Security provider installed");
        lock.countDown();
    }

    @Override
    public void onProviderInstallFailed(int errorCode, Intent recoveryIntent) {
        Log.i("Security provider install failed" +
                " errorCode=" + errorCode +
                " recoveryIntent=" + recoveryIntent);
        lock.countDown();
    }

    public static boolean waitProviderInstalled() {
        Log.i("Security provider wait");
        try {
            boolean succeeded = lock.await(WAIT_INSTALLED, TimeUnit.MILLISECONDS);
            Log.i("Security provider wait succeeded=" + succeeded);
            return succeeded;
        } catch (InterruptedException ex) {
            Log.i(ex);
            return false;
        }
    }
}
