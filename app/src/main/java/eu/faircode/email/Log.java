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

    Copyright 2018-2019 by Marcel Bokhorst (M66B)
*/

public class Log {
    static final String TAG = "fairemail";

    public static int i(String msg) {
        if (BuildConfig.BETA_RELEASE)
            return android.util.Log.i(TAG, msg);
        else
            return 0;
    }

    public static int w(String msg) {
        return android.util.Log.w(TAG, msg);
    }

    public static int e(String msg) {
        return android.util.Log.e(TAG, msg);
    }

    public static int w(Throwable ex) {
        return android.util.Log.w(TAG, ex + "\n" + android.util.Log.getStackTraceString(ex));
    }

    public static int e(Throwable ex) {
        return android.util.Log.e(TAG, ex + "\n" + android.util.Log.getStackTraceString(ex));
    }

    public static int w(String prefix, Throwable ex) {
        return android.util.Log.w(TAG, prefix + " " + ex + "\n" + android.util.Log.getStackTraceString(ex));
    }

    public static int e(String prefix, Throwable ex) {
        return android.util.Log.e(TAG, prefix + " " + ex + "\n" + android.util.Log.getStackTraceString(ex));
    }
}