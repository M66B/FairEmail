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

    Copyright 2018-2021 by Marcel Bokhorst (M66B)
*/

import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.preference.PreferenceManager;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

public class ActivityWidgetSync extends ActivityBase {
    private int appWidgetId;

    private CheckBox cbSemiTransparent;
    private ViewButtonColor btnColor;
    private Button btnSave;

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

        getSupportActionBar().setSubtitle(R.string.title_widget_title_sync);
        setContentView(R.layout.activity_widget_sync);

        cbSemiTransparent = findViewById(R.id.cbSemiTransparent);
        btnColor = findViewById(R.id.btnColor);
        btnSave = findViewById(R.id.btnSave);

        final Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

        btnColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int editTextColor = Helper.resolveColor(ActivityWidgetSync.this, android.R.attr.editTextColor);

                ColorPickerDialogBuilder
                        .with(ActivityWidgetSync.this)
                        .setTitle(R.string.title_widget_background)
                        .showColorEdit(true)
                        .setColorEditTextColor(editTextColor)
                        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                        .density(6)
                        .lightnessSliderOnly()
                        .setPositiveButton(android.R.string.ok, new ColorPickerClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                btnColor.setColor(selectedColor);
                            }
                        })
                        .setNegativeButton(R.string.title_reset, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                btnColor.setColor(Color.TRANSPARENT);
                            }
                        })
                        .build()
                        .show();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ActivityWidgetSync.this);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("widget." + appWidgetId + ".semi", cbSemiTransparent.isChecked());
                editor.putInt("widget." + appWidgetId + ".background", btnColor.getColor());
                editor.apply();

                WidgetSync.init(ActivityWidgetSync.this, appWidgetId);

                setResult(RESULT_OK, resultValue);
                finish();
            }
        });

        setResult(RESULT_CANCELED, resultValue);
    }
}
