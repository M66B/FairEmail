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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.AlarmManagerCompat;
import androidx.preference.PreferenceManager;

public class AlarmManagerCompatEx {
    static void setAndAllowWhileIdle(
            @NonNull Context context, @NonNull AlarmManager am,
            int type, long trigger, @NonNull PendingIntent pi) {
        if (hasExactAlarms(context))
            try {
                AlarmManagerCompat.setExactAndAllowWhileIdle(am, type, trigger, pi);
            } catch (SecurityException ex) {
                Log.w(ex);
                try {
                    AlarmManagerCompat.setAndAllowWhileIdle(am, type, trigger, pi);
                } catch (SecurityException exex) {
                    // Android 6.0.1 - SDK 23 ? - Samsung J1 Mini Prime (SM-J106F)
                    // java.lang.SecurityException: Permission Denial: getIntentForIntentSender() from pid=30091, uid=10107 requires android.permission.GET_INTENT_SENDER_INTENT
                    //        at android.os.Parcel.readException(Parcel.java:1621)
                    //        at android.os.Parcel.readException(Parcel.java:1574)
                    //        at android.app.IAlarmManager$Stub$Proxy.set(IAlarmManager.java:217)
                    //        at android.app.AlarmManager.setImpl(AlarmManager.java:484)
                    //        at android.app.AlarmManager.setAndAllowWhileIdle(AlarmManager.java:634)
                    //        at androidx.core.app.g.a(SourceFile:-1)
                    //        at androidx.core.app.AlarmManagerCompat$Api23Impl.setAndAllowWhileIdle(SourceFile:-1)
                    //        at androidx.core.app.AlarmManagerCompat.setAndAllowWhileIdle(AlarmManagerCompat:123)
                    //        at eu.faircode.email.AlarmManagerCompatEx.setAndAllowWhileIdle(AlarmManagerCompatEx:41)
                    Log.w(exex);
                    am.set(type, trigger, pi);
                }
            }
        else {
            try {
                AlarmManagerCompat.setAndAllowWhileIdle(am, type, trigger, pi);
            } catch (SecurityException ex) {
                Log.w(ex);
                am.set(type, trigger, pi);
            }
        }
    }

    static boolean hasExactAlarms(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean exact_alarms = prefs.getBoolean("exact_alarms", true);
        return (exact_alarms && canScheduleExactAlarms(context));
    }

    static boolean canScheduleExactAlarms(Context context) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R)
            return true;
        try {
            // https://developer.android.com/about/versions/12/behavior-changes-12#exact-alarm-permission
            AlarmManager am = Helper.getSystemService(context, AlarmManager.class);
            return am.canScheduleExactAlarms();
        } catch (Throwable ex) {
            Log.e(ex);
            return false;
        }
    }
}
