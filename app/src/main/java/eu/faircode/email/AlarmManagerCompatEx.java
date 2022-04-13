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

    Copyright 2018-2022 by Marcel Bokhorst (M66B)
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
                AlarmManagerCompat.setAndAllowWhileIdle(am, type, trigger, pi);
            }
        else
            AlarmManagerCompat.setAndAllowWhileIdle(am, type, trigger, pi);
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
