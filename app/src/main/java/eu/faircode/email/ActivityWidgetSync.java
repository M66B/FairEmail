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

import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.core.graphics.ColorUtils;
import androidx.preference.PreferenceManager;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

public class ActivityWidgetSync extends ActivityBase {
    private int appWidgetId;

    private CheckBox cbDayNight;
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

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean daynight = prefs.getBoolean("widget." + appWidgetId + ".daynight", false);
        boolean semi = prefs.getBoolean("widget." + appWidgetId + ".semi", true);
        int background = prefs.getInt("widget." + appWidgetId + ".background", Color.TRANSPARENT);

        daynight = daynight && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S);

        setContentView(R.layout.activity_widget_sync);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setSubtitle(R.string.title_widget_title_sync);

        cbDayNight = findViewById(R.id.cbDayNight);
        cbSemiTransparent = findViewById(R.id.cbSemiTransparent);
        btnColor = findViewById(R.id.btnColor);
        btnSave = findViewById(R.id.btnSave);

        final Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

        cbDayNight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                cbSemiTransparent.setEnabled(!checked);
                btnColor.setEnabled(!checked);
            }
        });

        cbSemiTransparent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    btnColor.setColor(Color.TRANSPARENT);
            }
        });

        btnColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int color = btnColor.getColor();
                int editTextColor = Helper.resolveColor(ActivityWidgetSync.this, android.R.attr.editTextColor);

                if (color == Color.TRANSPARENT) {
                    color = Color.WHITE;
                    if (cbSemiTransparent.isChecked() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                        color = ColorUtils.setAlphaComponent(color, 127);
                }

                ColorPickerDialogBuilder
                        .with(ActivityWidgetSync.this)
                        .setTitle(R.string.title_widget_background)
                        .showColorEdit(true)
                        .setColorEditTextColor(editTextColor)
                        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                        .density(6)
                        .initialColor(color)
                        .showLightnessSlider(true)
                        .showAlphaSlider(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                        .setPositiveButton(android.R.string.ok, new ColorPickerClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                                    cbSemiTransparent.setChecked(false);
                                btnColor.setColor(selectedColor);
                            }
                        })
                        .setNegativeButton(R.string.title_transparent, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                cbSemiTransparent.setChecked(false);
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
                editor.putBoolean("widget." + appWidgetId + ".daynight", cbDayNight.isChecked());
                editor.putBoolean("widget." + appWidgetId + ".semi", cbSemiTransparent.isChecked());
                editor.putInt("widget." + appWidgetId + ".background", btnColor.getColor());
                editor.putInt("widget." + appWidgetId + ".version", BuildConfig.VERSION_CODE);
                editor.apply();

                WidgetSync.init(ActivityWidgetSync.this, appWidgetId);

                setResult(RESULT_OK, resultValue);
                finish();
            }
        });

        // Initialize
        cbDayNight.setChecked(daynight);
        cbDayNight.setVisibility(Build.VERSION.SDK_INT < Build.VERSION_CODES.S ? View.GONE : View.VISIBLE);
        cbSemiTransparent.setChecked(semi);
        cbSemiTransparent.setEnabled(!daynight);
        btnColor.setColor(background);
        btnColor.setEnabled(!daynight);

        setResult(RESULT_CANCELED, resultValue);
    }
}
