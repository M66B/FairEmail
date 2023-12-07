package eu.faircode.email;

import android.content.Intent;

import com.google.android.gms.security.ProviderInstaller;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ApplicationSecure extends ApplicationEx implements ProviderInstaller.ProviderInstallListener {
    private static final CountDownLatch lock = new CountDownLatch(1);

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("Security provider check");
        ProviderInstaller.installIfNeededAsync(this, this);
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
            boolean succeeded = lock.await(500L, TimeUnit.MILLISECONDS);
            Log.i("Security provider wait succeeded=" + succeeded);
            return succeeded;
        } catch (InterruptedException ex) {
            Log.e(ex);
            return false;
        }
    }
}
