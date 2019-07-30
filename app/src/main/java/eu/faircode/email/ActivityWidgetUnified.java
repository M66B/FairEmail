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

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.preference.PreferenceManager;

public class ActivityWidgetUnified extends ActivityBase {
    private int appWidgetId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            finish();
            return;
        }

        appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

        getSupportActionBar().setSubtitle(R.string.title_folder_unified);
        setContentView(R.layout.activity_widget_unified);

        final CheckBox cbUnseen = findViewById(R.id.cbUnseen);
        final CheckBox cbFlagged = findViewById(R.id.cbFlagged);
        Button btnSave = findViewById(R.id.btnSave);

        final Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ActivityWidgetUnified.this);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("widget." + appWidgetId + ".unseen", cbUnseen.isChecked());
                editor.putBoolean("widget." + appWidgetId + ".flagged", cbFlagged.isChecked());
                editor.apply();

                WidgetUnified.init(ActivityWidgetUnified.this, appWidgetId);
                //WidgetUnified.update(ActivityWidgetUnified.this);

                setResult(RESULT_OK, resultValue);
                finish();
            }
        });

        setResult(RESULT_CANCELED, resultValue);
    }
}
