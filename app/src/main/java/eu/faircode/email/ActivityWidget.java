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

    Copyright 2018-2020 by Marcel Bokhorst (M66B)
*/

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.constraintlayout.widget.Group;
import androidx.core.graphics.ColorUtils;
import androidx.preference.PreferenceManager;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class ActivityWidget extends ActivityBase {
    private int appWidgetId;

    private Spinner spAccount;
    private CheckBox cbSemiTransparent;
    private Button btnColor;
    private View inOld;
    private View inNew;
    private RadioButton rbOld;
    private RadioButton rbNew;
    private Button btnSave;
    private ContentLoadingProgressBar pbWait;
    private Group grpReady;

    private int background = Color.TRANSPARENT;
    private ArrayAdapter<EntityAccount> adapterAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            finish();
            return;
        }

        if (savedInstanceState != null)
            background = savedInstanceState.getInt("fair:color");

        appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

        getSupportActionBar().setSubtitle(R.string.title_widget_title_count);
        setContentView(R.layout.activity_widget);

        spAccount = findViewById(R.id.spAccount);
        cbSemiTransparent = findViewById(R.id.cbSemiTransparent);
        btnColor = findViewById(R.id.btnColor);
        inOld = findViewById(R.id.inOld);
        inNew = findViewById(R.id.inNew);
        rbOld = findViewById(R.id.rbOld);
        rbNew = findViewById(R.id.rbNew);
        btnSave = findViewById(R.id.btnSave);
        pbWait = findViewById(R.id.pbWait);
        grpReady = findViewById(R.id.grpReady);

        final Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

        cbSemiTransparent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setBackground();
                btnColor.setEnabled(!isChecked);
            }
        });

        btnColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int editTextColor = Helper.resolveColor(ActivityWidget.this, android.R.attr.editTextColor);

                ColorPickerDialogBuilder
                        .with(ActivityWidget.this)
                        .setTitle(R.string.title_widget_background)
                        .showColorEdit(true)
                        .setColorEditTextColor(editTextColor)
                        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                        .density(6)
                        .lightnessSliderOnly()
                        .setPositiveButton(android.R.string.ok, new ColorPickerClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                background = selectedColor;
                                setBackground();
                            }
                        })
                        .setNegativeButton(R.string.title_reset, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                background = Color.TRANSPARENT;
                                setBackground();
                            }
                        })
                        .build()
                        .show();
            }
        });

        rbOld.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    rbNew.setChecked(false);
            }
        });

        rbNew.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    rbOld.setChecked(false);
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EntityAccount account = (EntityAccount) spAccount.getSelectedItem();

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ActivityWidget.this);
                SharedPreferences.Editor editor = prefs.edit();
                if (account != null && account.id > 0)
                    editor.putString("widget." + appWidgetId + ".name", account.name);
                else
                    editor.remove("widget." + appWidgetId + ".name");
                editor.putLong("widget." + appWidgetId + ".account", account == null ? -1L : account.id);
                editor.putBoolean("widget." + appWidgetId + ".semi", cbSemiTransparent.isChecked());
                editor.putInt("widget." + appWidgetId + ".background", background);
                editor.putInt("widget." + appWidgetId + ".layout", rbNew.isChecked() ? 1 : 0);
                editor.apply();

                Widget.init(ActivityWidget.this, appWidgetId);

                setResult(RESULT_OK, resultValue);
                finish();
            }
        });

        adapterAccount = new ArrayAdapter<>(this, R.layout.spinner_item1, android.R.id.text1, new ArrayList<EntityAccount>());
        adapterAccount.setDropDownViewResource(R.layout.spinner_item1_dropdown);
        spAccount.setAdapter(adapterAccount);

        ((TextView) inOld.findViewById(R.id.tvCount)).setText("12");
        ((TextView) inNew.findViewById(R.id.tvCount)).setText("12");

        btnColor.setEnabled(!cbSemiTransparent.isChecked());
        grpReady.setVisibility(View.GONE);
        pbWait.setVisibility(View.VISIBLE);

        setResult(RESULT_CANCELED, resultValue);

        new SimpleTask<List<EntityAccount>>() {
            @Override
            protected List<EntityAccount> onExecute(Context context, Bundle args) {
                DB db = DB.getInstance(context);

                return db.account().getSynchronizingAccounts();
            }

            @Override
            protected void onExecuted(Bundle args, List<EntityAccount> accounts) {
                if (accounts == null)
                    accounts = new ArrayList<>();

                EntityAccount all = new EntityAccount();
                all.id = -1L;
                all.name = getString(R.string.title_widget_account_all);
                all.primary = false;
                accounts.add(0, all);

                adapterAccount.addAll(accounts);

                grpReady.setVisibility(View.VISIBLE);
                pbWait.setVisibility(View.GONE);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getSupportFragmentManager(), ex);
            }
        }.execute(this, new Bundle(), "widget:accounts");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("fair:color", background);
        super.onSaveInstanceState(outState);
    }

    private void setBackground() {
        if (cbSemiTransparent.isChecked()) {
            inOld.setBackgroundResource(R.drawable.widget_background);
            inNew.setBackgroundResource(R.drawable.widget_background);
        } else {
            inOld.setBackgroundColor(background);
            inNew.setBackgroundColor(background);
            float lum = (float) ColorUtils.calculateLuminance(background);
            int color = (lum > 0.7 ? Color.BLACK : getResources().getColor(R.color.colorWidgetForeground));

            ((ImageView) inOld.findViewById(R.id.ivMessage)).setColorFilter(color);
            ((TextView) inOld.findViewById(R.id.tvCount)).setTextColor(color);
            ((TextView) inOld.findViewById(R.id.tvAccount)).setTextColor(color);

            ((ImageView) inNew.findViewById(R.id.ivMessage)).setColorFilter(color);
            ((TextView) inNew.findViewById(R.id.tvCount)).setTextColor(color);
            ((TextView) inNew.findViewById(R.id.tvAccount)).setTextColor(color);
        }
    }
}
