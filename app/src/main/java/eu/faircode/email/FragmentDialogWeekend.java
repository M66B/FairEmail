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

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;

import java.text.DateFormatSymbols;
import java.util.Arrays;
import java.util.Calendar;

public class FragmentDialogWeekend extends FragmentDialogBase {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        boolean[] days = new boolean[7];

        final Context context = getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String[] daynames = Arrays.copyOfRange(new DateFormatSymbols().getWeekdays(), 1, 8);

        String weekend = prefs.getString("weekend", Calendar.SATURDAY + "," + Calendar.SUNDAY);
        for (String day : weekend.split(","))
            if (!TextUtils.isEmpty(day))
                days[Integer.parseInt(day) - 1] = true;

        return new AlertDialog.Builder(context)
                .setTitle(R.string.title_advanced_schedule_weekend)
                .setMultiChoiceItems(daynames, days, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < days.length; i++)
                            if (days[i]) {
                                if (sb.length() > 0)
                                    sb.append(",");
                                sb.append(i + 1);
                            }
                        prefs.edit().putString("weekend", sb.toString()).apply();
                    }
                })
                .setNegativeButton(R.string.title_setup_done, null)
                .create();
    }
}
