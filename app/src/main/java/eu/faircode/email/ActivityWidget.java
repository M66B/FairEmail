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

    Copyright 2018-2023 by Marcel Bokhorst (M66B)
*/

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
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
    private CheckBox cbDayNight;
    private CheckBox cbSemiTransparent;
    private ViewButtonColor btnColor;
    private View inOld;
    private View inNew;
    private RadioButton rbOld;
    private RadioButton rbNew;
    private CheckBox cbTop;
    private Button btnSave;
    private ContentLoadingProgressBar pbWait;
    private Group grpReady;

    private ArrayAdapter<EntityAccount> adapterAccount;

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
        long account = prefs.getLong("widget." + appWidgetId + ".account", -1L);
        boolean daynight = prefs.getBoolean("widget." + appWidgetId + ".daynight", false);
        boolean semi = prefs.getBoolean("widget." + appWidgetId + ".semi", true);
        int background = prefs.getInt("widget." + appWidgetId + ".background", Color.TRANSPARENT);
        int layout = prefs.getInt("widget." + appWidgetId + ".layout", 1 /* new */);
        boolean top = prefs.getBoolean("widget." + appWidgetId + ".top", false);

        daynight = daynight && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setSubtitle(R.string.title_widget_title_count);
        setContentView(R.layout.activity_widget);

        spAccount = findViewById(R.id.spAccount);
        cbDayNight = findViewById(R.id.cbDayNight);
        cbSemiTransparent = findViewById(R.id.cbSemiTransparent);
        btnColor = findViewById(R.id.btnColor);
        inOld = findViewById(R.id.inOld);
        inNew = findViewById(R.id.inNew);
        rbOld = findViewById(R.id.rbOld);
        rbNew = findViewById(R.id.rbNew);
        cbTop = findViewById(R.id.cbTop);
        btnSave = findViewById(R.id.btnSave);
        pbWait = findViewById(R.id.pbWait);
        grpReady = findViewById(R.id.grpReady);

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
                updatePreview();
            }
        });

        btnColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int color = btnColor.getColor();
                int editTextColor = Helper.resolveColor(ActivityWidget.this, android.R.attr.editTextColor);

                if (color == Color.TRANSPARENT) {
                    color = Color.WHITE;
                    if (cbSemiTransparent.isChecked() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                        color = ColorUtils.setAlphaComponent(color, 127);
                }

                ColorPickerDialogBuilder
                        .with(ActivityWidget.this)
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
                                updatePreview();
                            }
                        })
                        .setNegativeButton(R.string.title_transparent, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                cbSemiTransparent.setChecked(false);
                                btnColor.setColor(Color.TRANSPARENT);
                                updatePreview();
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

        cbTop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updatePreview();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EntityAccount account = (EntityAccount) spAccount.getSelectedItem();

                SharedPreferences.Editor editor = prefs.edit();
                if (account != null && account.id > 0)
                    editor.putString("widget." + appWidgetId + ".name", account.name);
                else
                    editor.remove("widget." + appWidgetId + ".name");
                editor.putLong("widget." + appWidgetId + ".account", account == null ? -1L : account.id);
                editor.putBoolean("widget." + appWidgetId + ".daynight", cbDayNight.isChecked());
                editor.putBoolean("widget." + appWidgetId + ".semi", cbSemiTransparent.isChecked());
                editor.putInt("widget." + appWidgetId + ".background", btnColor.getColor());
                editor.putInt("widget." + appWidgetId + ".layout", rbNew.isChecked() ? 1 : 0);
                editor.putBoolean("widget." + appWidgetId + ".top", cbTop.isChecked());
                editor.putInt("widget." + appWidgetId + ".version", BuildConfig.VERSION_CODE);
                editor.apply();

                Widget.init(ActivityWidget.this, appWidgetId);

                setResult(RESULT_OK, resultValue);
                finish();
            }
        });

        adapterAccount = new ArrayAdapter<>(this, R.layout.spinner_item1, android.R.id.text1, new ArrayList<EntityAccount>());
        adapterAccount.setDropDownViewResource(R.layout.spinner_item1_dropdown);
        spAccount.setAdapter(adapterAccount);

        // Initialize
        ((TextView) inOld.findViewById(R.id.tvCount)).setText("3");
        ((TextView) inNew.findViewById(R.id.tvCount)).setText("3");
        ((TextView) inNew.findViewById(R.id.tvCountTop)).setText("3");

        cbDayNight.setChecked(daynight);
        cbDayNight.setVisibility(Build.VERSION.SDK_INT < Build.VERSION_CODES.S ? View.GONE : View.VISIBLE);
        cbSemiTransparent.setChecked(semi);
        cbSemiTransparent.setEnabled(!daynight);
        btnColor.setColor(background);
        btnColor.setEnabled(!daynight);
        rbOld.setChecked(layout != 1);
        rbNew.setChecked(layout == 1);
        cbTop.setChecked(top);
        updatePreview();

        grpReady.setVisibility(View.GONE);
        pbWait.setVisibility(View.VISIBLE);

        setResult(RESULT_CANCELED, resultValue);

        Bundle args = new Bundle();

        new SimpleTask<List<EntityAccount>>() {
            @Override
            protected List<EntityAccount> onExecute(Context context, Bundle args) {
                DB db = DB.getInstance(context);

                return db.account().getSynchronizingAccounts(null);
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

                for (int i = 0; i < accounts.size(); i++)
                    if (accounts.get(i).id.equals(account)) {
                        spAccount.setSelection(i);
                        break;
                    }

                grpReady.setVisibility(View.VISIBLE);
                pbWait.setVisibility(View.GONE);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getSupportFragmentManager(), ex);
            }
        }.execute(this, args, "widget:accounts");
    }

    private void updatePreview() {
        boolean semi = cbSemiTransparent.isChecked();
        int background = btnColor.getColor();
        if (background == Color.TRANSPARENT) {
            if (semi) {
                inOld.setBackgroundResource(R.drawable.widget_background);
                inNew.setBackgroundResource(R.drawable.widget_background);
            } else {
                inOld.setBackgroundColor(background);
                inNew.setBackgroundColor(background);
            }
        } else {
            float lum = (float) ColorUtils.calculateLuminance(background);
            int color = (lum > 0.7 ? Color.BLACK : getResources().getColor(R.color.colorWidgetForeground));
            if (semi)
                background = ColorUtils.setAlphaComponent(background, 127);

            inOld.setBackgroundColor(background);
            inNew.setBackgroundColor(background);

            ((ImageView) inOld.findViewById(R.id.ivMessage)).setColorFilter(color);
            ((TextView) inOld.findViewById(R.id.tvCount)).setTextColor(color);
            ((TextView) inOld.findViewById(R.id.tvAccount)).setTextColor(color);

            ((ImageView) inNew.findViewById(R.id.ivMessage)).setColorFilter(color);
            ((TextView) inNew.findViewById(R.id.tvCount)).setTextColor(color);
            ((TextView) inNew.findViewById(R.id.tvCountTop)).setTextColor(color);
            ((TextView) inNew.findViewById(R.id.tvAccount)).setTextColor(color);
        }

        boolean top = cbTop.isChecked();
        ((TextView) inNew.findViewById(R.id.tvCount)).setVisibility(top ? View.GONE : View.VISIBLE);
        ((TextView) inNew.findViewById(R.id.tvCountTop)).setVisibility(top ? View.VISIBLE : View.GONE);
    }
}
