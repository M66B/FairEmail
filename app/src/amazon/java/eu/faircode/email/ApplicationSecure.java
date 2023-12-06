package eu.faircode.email;

import android.content.Intent;

import com.google.android.gms.security.ProviderInstaller;

public class ApplicationSecure extends ApplicationEx implements ProviderInstaller.ProviderInstallListener {
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("Security provider check");
        ProviderInstaller.installIfNeededAsync(this, this);
    }

    @Override
    public void onProviderInstalled() {
        Log.i("Security provider installed");
    }

    @Override
    public void onProviderInstallFailed(int errorCode, Intent recoveryIntent) {
        Log.i("Security provider install failed" +
                " errorCode=" + errorCode +
                " recoveryIntent=" + recoveryIntent);
    }
}
