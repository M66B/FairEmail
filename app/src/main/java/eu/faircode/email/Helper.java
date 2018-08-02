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

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Helper {
    static final String TAG = BuildConfig.APPLICATION_ID;

    static int resolveColor(Context context, int attr) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }

    static String localizeFolderName(Context context, String name) {
        if ("INBOX".equals(name))
            return context.getString(R.string.title_folder_inbox);
        else if ("OUTBOX".equals(name))
            return context.getString(R.string.title_folder_outbox);
        else
            return name;
    }

    static String localizeFolderName(Context context, TupleFolderEx folder) {
        if (TextUtils.isEmpty(folder.accountName))
            return localizeFolderName(context, folder.name);
        else
            return localizeFolderName(context, folder.name) + "/" + folder.accountName;
    }

    static String formatThrowable(Throwable ex) {
        StringBuilder sb = new StringBuilder();
        sb.append(ex.getMessage());
        Throwable cause = ex.getCause();
        while (cause != null) {
            sb.append(" ").append(cause.getMessage());
            cause = cause.getCause();
        }
        return sb.toString();
    }

    static StringBuilder getDebugInfo() {
        StringBuilder sb = new StringBuilder();

        // Get version info
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

        // Get logcat
        Process proc = null;
        BufferedReader br = null;
        try {
            String[] cmd = new String[]{"logcat", "-d", "-v", "threadtime"};
            proc = Runtime.getRuntime().exec(cmd);
            br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line;
            while ((line = br.readLine()) != null)
                sb.append(line).append("\r\n");
        } catch (IOException ex) {
            Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
        } finally {
            if (br != null)
                try {
                    br.close();
                } catch (IOException ignored) {
                }
            if (proc != null)
                try {
                    proc.destroy();
                } catch (Throwable ex) {
                    Log.w(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
                }
        }

        return sb;
    }
}
