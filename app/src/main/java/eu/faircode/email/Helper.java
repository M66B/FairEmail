package eu.faircode.email;

/*
    This file is part of FairEmail.

    FairEmail is free software: you can redistribute it and/or modify
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
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

public class Helper {
    static final String TAG = "fairemail";

    static int resolveColor(Context context, int attr) {
        int[] attrs = new int[]{attr};
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs);
        int color = a.getColor(0, 0xFF0000);
        a.recycle();
        return color;
    }

    static void setViewsEnabled(ViewGroup view, boolean enabled) {
        for (int i = 0; i < view.getChildCount(); i++) {
            View child = view.getChildAt(i);
            if (child instanceof Spinner ||
                    child instanceof EditText ||
                    child instanceof CheckBox ||
                    child instanceof ImageView /* =ImageButton */)
                child.setEnabled(enabled);
            if (child instanceof BottomNavigationView) {
                Menu menu = ((BottomNavigationView) child).getMenu();
                menu.setGroupEnabled(0, enabled);
            } else if (child instanceof ViewGroup)
                setViewsEnabled((ViewGroup) child, enabled);
        }
    }

    static String localizeFolderName(Context context, String name) {
        if ("INBOX".equals(name))
            return context.getString(R.string.title_folder_inbox);
        else if ("OUTBOX".equals(name))
            return context.getString(R.string.title_folder_outbox);
        else
            return name;
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

    static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    static StringBuilder getLogcat() {
        StringBuilder sb = new StringBuilder();

        Process proc = null;
        BufferedReader br = null;
        try {
            String[] cmd = new String[]{"logcat", "-d", "-v", "threadtime", TAG + ":I"};
            proc = Runtime.getRuntime().exec(cmd);
            br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line;
            while ((line = br.readLine()) != null)
                sb.append(line).append("\r\n");
        } catch (IOException ex) {
            Log.e(TAG, ex + "\n" + Log.getStackTraceString(ex));
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
                    Log.w(TAG, ex + "\n" + Log.getStackTraceString(ex));
                }
        }

        return sb;
    }

    static Address myAddress() throws UnsupportedEncodingException {
        return new InternetAddress("marcel+fairemail@faircode.eu", "FairCode");
    }

    static String canonicalAddress(String address) {
        String[] a = address.split("\\@");
        if (a.length > 0)
            a[0] = a[0].split("\\+")[0];
        return TextUtils.join("@", a);
    }
}
