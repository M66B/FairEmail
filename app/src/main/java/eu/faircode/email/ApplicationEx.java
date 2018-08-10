package eu.faircode.email;

/*
    This file is part of Safe email.

    Safe email is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    NetGuard is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with NetGuard.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2018 by Marcel Bokhorst (M66B)
*/

import android.app.Application;
import android.os.Build;
import android.util.Log;

import java.util.Date;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

public class ApplicationEx extends Application {
    private Thread.UncaughtExceptionHandler prev = null;

    @Override
    public void onCreate() {
        super.onCreate();

        prev = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                Log.w(Helper.TAG, "Handling crash");
                DB db = null;
                try {
                    db = DB.getBlockingInstance(ApplicationEx.this);
                    EntityFolder drafts = db.folder().getPrimaryDrafts();
                    if (drafts != null) {
                        Address to = new InternetAddress("marcel+email@faircode.eu", "FairCode");

                        // Get version info
                        StringBuilder sb = new StringBuilder();
                        sb.append(String.format("%s: %s/%d\r\n", BuildConfig.APPLICATION_ID, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));
                        sb.append(String.format("Android: %s (SDK %d)\r\n", Build.VERSION.RELEASE, Build.VERSION.SDK_INT));
                        sb.append("\r\n");

                        // Get device info
                        sb.append(String.format("Brand: %s\r\n", Build.BRAND));
                        sb.append(String.format("Manufacturer: %s\r\n", Build.MANUFACTURER));
                        sb.append(String.format("Model: %s\r\n", Build.MODEL));
                        sb.append(String.format("Product: %s\r\n", Build.PRODUCT));
                        sb.append(String.format("Device: %s\r\n", Build.DEVICE));
                        sb.append(String.format("Host: %s\r\n", Build.HOST));
                        sb.append(String.format("Display: %s\r\n", Build.DISPLAY));
                        sb.append(String.format("Id: %s\r\n", Build.ID));
                        sb.append("\r\n");

                        sb.append(ex.toString()).append("\r\n").append(Log.getStackTraceString(ex));

                        EntityMessage draft = new EntityMessage();
                        draft.account = drafts.account;
                        draft.folder = drafts.id;
                        draft.to = new Address[]{to};
                        draft.subject = BuildConfig.APPLICATION_ID + " crash info";
                        draft.body = "<pre>" + sb.toString().replaceAll("\\r?\\n", "<br />") + "</pre>";
                        draft.received = new Date().getTime();
                        draft.seen = false;
                        draft.ui_seen = false;
                        draft.ui_hide = false;
                        draft.id = db.message().insertMessage(draft);

                        Log.w(Helper.TAG, "Crash info stored as draft");
                    }
                } catch (Throwable e1) {
                    Log.e(Helper.TAG, e1 + "\n" + Log.getStackTraceString(e1));
                } finally {
                    if (db != null)
                        db.close();
                }

                if (prev != null)
                    prev.uncaughtException(thread, ex);
            }
        });
    }
}
