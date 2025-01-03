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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.preference.PreferenceManager;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ActivityWidget extends ActivityBase {
    private int appWidgetId;

    private Spinner spAccount;
    private Spinner spFolder;
    private CheckBox cbDayNight;
    private CheckBox cbSemiTransparent;
    private ViewButtonColor btnBgColor;
    private ViewButtonColor btnFgColor;
    private View inOld;
    private View inNew;
    private RadioButton rbOld;
    private RadioButton rbNew;
    private CheckBox cbTop;
    private Spinner spFontSize;
    private EditText etName;
    private CheckBox cbStandalone;
    private Button btnSave;
    private ContentLoadingProgressBar pbWait;
    private Group grpReady;

    private ArrayAdapter<EntityAccount> adapterAccount;
    private ArrayAdapter<EntityFolder> adapterFolder;

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
        long folder = prefs.getLong("widget." + appWidgetId + ".folder", -1L);
        boolean daynight = prefs.getBoolean("widget." + appWidgetId + ".daynight", false);
        boolean semi = prefs.getBoolean("widget." + appWidgetId + ".semi", true);
        int background = prefs.getInt("widget." + appWidgetId + ".background", Color.TRANSPARENT);
        int foreground = prefs.getInt("widget." + appWidgetId + ".foreground", Color.TRANSPARENT);
        int layout = prefs.getInt("widget." + appWidgetId + ".layout", 1 /* new */);
        boolean top = prefs.getBoolean("widget." + appWidgetId + ".top", false);
        int size = prefs.getInt("widget." + appWidgetId + ".text_size", -1);
        String name = prefs.getString("widget." + appWidgetId + ".name", null);
        boolean standalone = prefs.getBoolean("widget." + appWidgetId + ".standalone", false);

        daynight = daynight && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S);

        setContentView(R.layout.activity_widget);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setSubtitle(R.string.title_widget_title_count);

        spAccount = findViewById(R.id.spAccount);
        spFolder = findViewById(R.id.spFolder);
        cbDayNight = findViewById(R.id.cbDayNight);
        cbSemiTransparent = findViewById(R.id.cbSemiTransparent);
        btnBgColor = findViewById(R.id.btnBgColor);
        btnFgColor = findViewById(R.id.btnFgColor);
        inOld = findViewById(R.id.inOld);
        inNew = findViewById(R.id.inNew);
        rbOld = findViewById(R.id.rbOld);
        rbNew = findViewById(R.id.rbNew);
        cbTop = findViewById(R.id.cbTop);
        spFontSize = findViewById(R.id.spFontSize);
        etName = findViewById(R.id.etName);
        cbStandalone = findViewById(R.id.cbStandalone);
        btnSave = findViewById(R.id.btnSave);
        pbWait = findViewById(R.id.pbWait);
        grpReady = findViewById(R.id.grpReady);

        final Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

        cbDayNight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                cbSemiTransparent.setEnabled(!checked);
                btnBgColor.setEnabled(!checked);
                updatePreview();
            }
        });

        cbSemiTransparent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    btnBgColor.setColor(Color.TRANSPARENT);
                updatePreview();
            }
        });

        btnBgColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int color = btnBgColor.getColor();
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
                                btnBgColor.setColor(selectedColor);
                                updatePreview();
                            }
                        })
                        .setNegativeButton(R.string.title_transparent, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                cbSemiTransparent.setChecked(false);
                                btnBgColor.setColor(Color.TRANSPARENT);
                                updatePreview();
                            }
                        })
                        .build()
                        .show();
            }
        });

        btnFgColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int color = btnFgColor.getColor();
                int editTextColor = Helper.resolveColor(ActivityWidget.this, android.R.attr.editTextColor);

                ColorPickerDialogBuilder
                        .with(ActivityWidget.this)
                        .setTitle(R.string.title_widget_icon)
                        .showColorEdit(true)
                        .setColorEditTextColor(editTextColor)
                        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                        .density(6)
                        .initialColor(color == Color.TRANSPARENT ? Color.WHITE : color)
                        .showLightnessSlider(true)
                        .showAlphaSlider(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                        .setPositiveButton(android.R.string.ok, new ColorPickerClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                btnFgColor.setColor(selectedColor);
                                updatePreview();
                            }
                        })
                        .setNegativeButton(R.string.title_reset, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                btnFgColor.setColor(Color.TRANSPARENT);
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
                spFontSize.setEnabled(!isChecked);
            }
        });

        rbNew.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    rbOld.setChecked(false);
                spFontSize.setEnabled(isChecked);
            }
        });

        cbTop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updatePreview();
            }
        });

        spFontSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                updatePreview();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                updatePreview();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EntityAccount account = (EntityAccount) spAccount.getSelectedItem();
                EntityFolder folder = (EntityFolder) spFolder.getSelectedItem();
                int pos = spFontSize.getSelectedItemPosition();

                SharedPreferences.Editor editor = prefs.edit();
                String name = etName.getText().toString();
                if (TextUtils.isEmpty(name))
                    if (folder == null || folder.id < 0) {
                        if (account != null && account.id > 0)
                            editor.putString("widget." + appWidgetId + ".name", account.name);
                        else
                            editor.remove("widget." + appWidgetId + ".name");
                    } else
                        editor.putString("widget." + appWidgetId + ".name", folder.getDisplayName(ActivityWidget.this));
                else
                    editor.putString("widget." + appWidgetId + ".name", name);
                editor.putLong("widget." + appWidgetId + ".account", account == null ? -1L : account.id);
                editor.putLong("widget." + appWidgetId + ".folder", folder == null ? -1L : folder.id);
                editor.putBoolean("widget." + appWidgetId + ".daynight", cbDayNight.isChecked());
                editor.putBoolean("widget." + appWidgetId + ".semi", cbSemiTransparent.isChecked());
                editor.putInt("widget." + appWidgetId + ".background", btnBgColor.getColor());
                editor.putInt("widget." + appWidgetId + ".foreground", btnFgColor.getColor());
                editor.putInt("widget." + appWidgetId + ".layout", rbNew.isChecked() ? 1 : 0);
                editor.putBoolean("widget." + appWidgetId + ".top", cbTop.isChecked());
                if (pos > 0)
                    editor.putInt("widget." + appWidgetId + ".text_size", pos - 1);
                else
                    editor.remove("widget." + appWidgetId + ".text_size");
                editor.putBoolean("widget." + appWidgetId + ".standalone", cbStandalone.isChecked());
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

        adapterFolder = new ArrayAdapter<EntityFolder>(this, R.layout.spinner_item1, android.R.id.text1, new ArrayList<EntityFolder>()) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                return localize(position, super.getView(position, convertView, parent));
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                return localize(position, super.getDropDownView(position, convertView, parent));
            }

            private View localize(int position, View view) {
                EntityFolder folder = getItem(position);
                if (folder != null) {
                    TextView tv = view.findViewById(android.R.id.text1);
                    tv.setText(EntityFolder.localizeName(view.getContext(), folder.name));
                }
                return view;
            }
        };
        adapterFolder.setDropDownViewResource(R.layout.spinner_item1_dropdown);
        spFolder.setAdapter(adapterFolder);

        spAccount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                EntityAccount account = (EntityAccount) spAccount.getAdapter().getItem(position);
                setFolders(account.id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                setFolders(-1);
            }

            private void setFolders(long account) {
                Bundle args = new Bundle();
                args.putLong("account", account);

                new SimpleTask<List<EntityFolder>>() {
                    @Override
                    protected List<EntityFolder> onExecute(Context context, Bundle args) {
                        long account = args.getLong("account");

                        DB db = DB.getInstance(context);
                        List<EntityFolder> folders = db.folder().getNotifyingFolders(account);
                        if (folders != null && folders.size() > 0)
                            Collections.sort(folders, folders.get(0).getComparator(context));
                        return folders;
                    }

                    @Override
                    protected void onExecuted(Bundle args, List<EntityFolder> folders) {
                        if (folders == null)
                            folders = new ArrayList<>();

                        EntityFolder notifying = new EntityFolder();
                        notifying.id = -1L;
                        notifying.name = getString(R.string.title_widget_folder_notifying);
                        folders.add(0, notifying);

                        adapterFolder.clear();
                        adapterFolder.addAll(folders);

                        int select = 0;
                        for (int i = 0; i < folders.size(); i++)
                            if (folders.get(i).id.equals(folder)) {
                                select = i;
                                break;
                            }

                        spFolder.setSelection(select);
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Log.unexpectedError(getSupportFragmentManager(), ex);
                    }
                }.execute(ActivityWidget.this, args, "widget:folders");
            }
        });

        // Initialize
        ((TextView) inOld.findViewById(R.id.tvCount)).setText("3");
        ((TextView) inNew.findViewById(R.id.tvCount)).setText("3");
        ((TextView) inNew.findViewById(R.id.tvCountTop)).setText("3");

        cbDayNight.setChecked(daynight);
        cbDayNight.setVisibility(Build.VERSION.SDK_INT < Build.VERSION_CODES.S ? View.GONE : View.VISIBLE);
        cbSemiTransparent.setChecked(semi);
        cbSemiTransparent.setEnabled(!daynight);
        btnBgColor.setColor(background);
        btnBgColor.setEnabled(!daynight);
        btnFgColor.setColor(foreground);
        rbOld.setChecked(layout != 1);
        rbNew.setChecked(layout == 1);
        cbTop.setChecked(top);
        spFontSize.setSelection(size + 1);
        etName.setText(name);
        cbStandalone.setChecked(standalone);
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
        boolean daynight = cbDayNight.isChecked();
        boolean semi = cbSemiTransparent.isChecked();
        int background = btnBgColor.getColor();
        int foreground = btnFgColor.getColor();

        int textColorPrimary = Helper.resolveColor(ActivityWidget.this, android.R.attr.textColorPrimary);
        int colorWidgetForeground = ContextCompat.getColor(ActivityWidget.this, R.color.colorWidgetForeground);

        if (background == Color.TRANSPARENT) {
            if (semi) {
                inOld.setBackgroundResource(R.drawable.widget_background);
                inNew.setBackgroundResource(R.drawable.widget_background);
            } else {
                inOld.setBackgroundColor(background);
                inNew.setBackgroundColor(background);
            }
        } else {
            if (semi)
                background = ColorUtils.setAlphaComponent(background, 127);

            inOld.setBackgroundColor(background);
            inNew.setBackgroundColor(background);
        }

        if (daynight && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ((ImageView) inOld.findViewById(R.id.ivMessage)).setColorFilter(
                    foreground == Color.TRANSPARENT ? textColorPrimary : foreground);
            ((TextView) inOld.findViewById(R.id.tvCount)).setTextColor(textColorPrimary);
            ((TextView) inOld.findViewById(R.id.tvAccount)).setTextColor(textColorPrimary);

            ((ImageView) inNew.findViewById(R.id.ivMessage)).setColorFilter(
                    foreground == Color.TRANSPARENT ? textColorPrimary : foreground);
            ((TextView) inNew.findViewById(R.id.tvCount)).setTextColor(colorWidgetForeground);
            ((TextView) inNew.findViewById(R.id.tvCountTop)).setTextColor(colorWidgetForeground);
            ((TextView) inNew.findViewById(R.id.tvAccount)).setTextColor(textColorPrimary);
        } else if (background == Color.TRANSPARENT) {
            ((ImageView) inOld.findViewById(R.id.ivMessage)).setColorFilter(
                    foreground == Color.TRANSPARENT ? colorWidgetForeground : foreground);
            ((TextView) inOld.findViewById(R.id.tvCount)).setTextColor(colorWidgetForeground);
            ((TextView) inOld.findViewById(R.id.tvAccount)).setTextColor(colorWidgetForeground);

            ((ImageView) inNew.findViewById(R.id.ivMessage)).setColorFilter(
                    foreground == Color.TRANSPARENT ? colorWidgetForeground : foreground);
            ((TextView) inNew.findViewById(R.id.tvCount)).setTextColor(colorWidgetForeground);
            ((TextView) inNew.findViewById(R.id.tvCountTop)).setTextColor(colorWidgetForeground);
            ((TextView) inNew.findViewById(R.id.tvAccount)).setTextColor(colorWidgetForeground);
        } else {
            float lum = (float) ColorUtils.calculateLuminance(background);
            int fg = (lum > 0.7f ? Color.BLACK : colorWidgetForeground);

            ((ImageView) inOld.findViewById(R.id.ivMessage)).setColorFilter(
                    foreground == Color.TRANSPARENT ? fg : foreground);
            ((TextView) inOld.findViewById(R.id.tvCount)).setTextColor(fg);
            ((TextView) inOld.findViewById(R.id.tvAccount)).setTextColor(fg);

            ((ImageView) inNew.findViewById(R.id.ivMessage)).setColorFilter(
                    foreground == Color.TRANSPARENT ? fg : foreground);
            ((TextView) inNew.findViewById(R.id.tvCount)).setTextColor(colorWidgetForeground);
            ((TextView) inNew.findViewById(R.id.tvCountTop)).setTextColor(colorWidgetForeground);
            ((TextView) inNew.findViewById(R.id.tvAccount)).setTextColor(fg);
        }

        boolean top = cbTop.isChecked();
        ((TextView) inNew.findViewById(R.id.tvCount)).setVisibility(top ? View.GONE : View.VISIBLE);
        ((TextView) inNew.findViewById(R.id.tvCountTop)).setVisibility(top ? View.VISIBLE : View.GONE);

        int size = spFontSize.getSelectedItemPosition() - 1;
        if (size < 0)
            size = 0;
        float textSize = Helper.getTextSize(this, size);
        ((TextView) inNew.findViewById(R.id.tvCount)).setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        ((TextView) inNew.findViewById(R.id.tvCountTop)).setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
    }
}
