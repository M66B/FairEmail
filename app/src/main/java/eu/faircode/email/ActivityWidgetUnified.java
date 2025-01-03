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
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.core.graphics.ColorUtils;
import androidx.preference.PreferenceManager;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ActivityWidgetUnified extends ActivityBase {
    private int appWidgetId;

    private Spinner spAccount;
    private Spinner spFolder;
    private CheckBox cbUnseen;
    private CheckBox cbShowUnseen;
    private CheckBox cbFlagged;
    private CheckBox cbShowFlagged;
    private CheckBox cbDayNight;
    private CheckBox cbHighlight;
    private ViewButtonColor btnHighlight;
    private CheckBox cbSemiTransparent;
    private ViewButtonColor btnColor;
    private CheckBox cbSeparatorLines;
    private Spinner spFontSize;
    private Spinner spPadding;
    private Spinner spSubjectLines;
    private TextView tvSubjectLinesHint;
    private CheckBox cbAvatars;
    private CheckBox cbAccountName;
    private CheckBox cbCaption;
    private EditText etName;
    private CheckBox cbRefresh;
    private CheckBox cbCompose;
    private CheckBox cbStandalone;
    private Button btnSave;
    private ContentLoadingProgressBar pbWait;
    private Group grpReady;

    private ArrayAdapter<EntityAccount> adapterAccount;
    private ArrayAdapter<TupleFolderEx> adapterFolder;
    private ArrayAdapter<String> adapterFontSize;
    private ArrayAdapter<String> adapterPadding;

    private NumberFormat NF = NumberFormat.getNumberInstance();

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
        boolean unseen = prefs.getBoolean("widget." + appWidgetId + ".unseen", false);
        boolean show_unseen = prefs.getBoolean("widget." + appWidgetId + ".show_unseen", true);
        boolean flagged = prefs.getBoolean("widget." + appWidgetId + ".flagged", false);
        boolean show_flagged = prefs.getBoolean("widget." + appWidgetId + ".show_flagged", false);
        boolean daynight = prefs.getBoolean("widget." + appWidgetId + ".daynight", false);
        boolean highlight = prefs.getBoolean("widget." + appWidgetId + ".highlight", false);
        int highlight_color = prefs.getInt("widget." + appWidgetId + ".highlight_color", Color.TRANSPARENT);
        boolean semi = prefs.getBoolean("widget." + appWidgetId + ".semi", true);
        int background = prefs.getInt("widget." + appWidgetId + ".background", Color.TRANSPARENT);
        boolean separators = prefs.getBoolean("widget." + appWidgetId + ".separators", true);
        int font = prefs.getInt("widget." + appWidgetId + ".font", 0);
        int padding = prefs.getInt("widget." + appWidgetId + ".padding", 0);
        int subject_lines = prefs.getInt("widget." + appWidgetId + ".subject_lines", 1);
        boolean avatars = prefs.getBoolean("widget." + appWidgetId + ".avatars", false);
        boolean account_name = prefs.getBoolean("widget." + appWidgetId + ".account_name", true);
        boolean caption = prefs.getBoolean("widget." + appWidgetId + ".caption", true);
        String name = prefs.getString("widget." + appWidgetId + ".name", null);
        boolean refresh = prefs.getBoolean("widget." + appWidgetId + ".refresh", false);
        boolean compose = prefs.getBoolean("widget." + appWidgetId + ".compose", false);
        boolean standalone = prefs.getBoolean("widget." + appWidgetId + ".standalone", false);

        daynight = daynight && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S);

        setContentView(R.layout.activity_widget_unified);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setSubtitle(R.string.title_widget_title_list);

        spAccount = findViewById(R.id.spAccount);
        spFolder = findViewById(R.id.spFolder);
        cbUnseen = findViewById(R.id.cbUnseen);
        cbShowUnseen = findViewById(R.id.cbShowUnseen);
        cbFlagged = findViewById(R.id.cbFlagged);
        cbShowFlagged = findViewById(R.id.cbShowFlagged);
        cbDayNight = findViewById(R.id.cbDayNight);
        cbHighlight = findViewById(R.id.cbHighlight);
        btnHighlight = findViewById(R.id.btnHighlight);
        cbSemiTransparent = findViewById(R.id.cbSemiTransparent);
        btnColor = findViewById(R.id.btnColor);
        cbSeparatorLines = findViewById(R.id.cbSeparatorLines);
        spFontSize = findViewById(R.id.spFontSize);
        spPadding = findViewById(R.id.spPadding);
        spSubjectLines = findViewById(R.id.spSubjectLines);
        tvSubjectLinesHint = findViewById(R.id.tvSubjectLinesHint);
        cbAvatars = findViewById(R.id.cbAvatars);
        cbAccountName = findViewById(R.id.cbAccountName);
        cbCaption = findViewById(R.id.cbCaption);
        etName = findViewById(R.id.etName);
        cbRefresh = findViewById(R.id.cbRefresh);
        cbCompose = findViewById(R.id.cbCompose);
        cbStandalone = findViewById(R.id.cbStandalone);
        btnSave = findViewById(R.id.btnSave);
        pbWait = findViewById(R.id.pbWait);
        grpReady = findViewById(R.id.grpReady);

        final Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

        cbUnseen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                cbShowUnseen.setEnabled(isChecked);
            }
        });

        cbFlagged.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                cbShowFlagged.setEnabled(!isChecked);
            }
        });

        cbShowFlagged.setVisibility(Build.VERSION.SDK_INT < Build.VERSION_CODES.S ? View.GONE : View.VISIBLE);

        cbDayNight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                cbHighlight.setEnabled(!checked);
                btnHighlight.setEnabled(cbHighlight.isChecked() && !checked);
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

        cbHighlight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                btnHighlight.setVisibility(checked && cbHighlight.isEnabled() ? View.VISIBLE : View.GONE);
                btnHighlight.setEnabled(checked && cbHighlight.isEnabled());
            }
        });

        btnHighlight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int editTextColor = Helper.resolveColor(ActivityWidgetUnified.this, android.R.attr.editTextColor);

                ColorPickerDialogBuilder
                        .with(ActivityWidgetUnified.this)
                        .setTitle(R.string.title_advanced_highlight_color)
                        .showColorEdit(true)
                        .setColorEditTextColor(editTextColor)
                        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                        .density(6)
                        .lightnessSliderOnly()
                        .setPositiveButton(android.R.string.ok, new ColorPickerClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                btnHighlight.setColor(selectedColor);
                            }
                        })
                        .setNegativeButton(R.string.title_reset, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                btnHighlight.setColor(Color.TRANSPARENT);
                            }
                        })
                        .build()
                        .show();
            }
        });

        btnColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int color = btnColor.getColor();
                int editTextColor = Helper.resolveColor(ActivityWidgetUnified.this, android.R.attr.editTextColor);

                if (color == Color.TRANSPARENT) {
                    color = Color.WHITE;
                    if (cbSemiTransparent.isChecked() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                        color = ColorUtils.setAlphaComponent(color, 127);
                }

                ColorPickerDialogBuilder
                        .with(ActivityWidgetUnified.this)
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

        cbCaption.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                etName.setEnabled(isChecked);
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EntityAccount account = (EntityAccount) spAccount.getSelectedItem();
                TupleFolderEx folder = (TupleFolderEx) spFolder.getSelectedItem();

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ActivityWidgetUnified.this);
                SharedPreferences.Editor editor = prefs.edit();

                String name = etName.getText().toString();
                if (TextUtils.isEmpty(name))
                    if (account != null && account.id > 0)
                        if (folder != null && folder.id > 0)
                            editor.putString("widget." + appWidgetId + ".name", folder.getDisplayName(ActivityWidgetUnified.this));
                        else
                            editor.putString("widget." + appWidgetId + ".name", account.name);
                    else
                        editor.remove("widget." + appWidgetId + ".name");
                else
                    editor.putString("widget." + appWidgetId + ".name", name);

                int font = spFontSize.getSelectedItemPosition();
                int padding = spPadding.getSelectedItemPosition();

                editor.putLong("widget." + appWidgetId + ".account", account == null ? -1L : account.id);
                editor.putLong("widget." + appWidgetId + ".folder", folder == null ? -1L : folder.id);
                editor.putString("widget." + appWidgetId + ".type", folder == null ? null : folder.type);
                editor.putBoolean("widget." + appWidgetId + ".unseen", cbUnseen.isChecked());
                editor.putBoolean("widget." + appWidgetId + ".show_unseen", cbShowUnseen.isChecked());
                editor.putBoolean("widget." + appWidgetId + ".daynight", cbDayNight.isChecked());
                editor.putBoolean("widget." + appWidgetId + ".flagged", cbFlagged.isChecked());
                editor.putBoolean("widget." + appWidgetId + ".show_flagged", cbShowFlagged.isChecked());
                editor.putBoolean("widget." + appWidgetId + ".highlight", cbHighlight.isChecked());
                editor.putInt("widget." + appWidgetId + ".highlight_color", btnHighlight.getColor());
                editor.putBoolean("widget." + appWidgetId + ".semi", cbSemiTransparent.isChecked());
                editor.putInt("widget." + appWidgetId + ".background", btnColor.getColor());
                editor.putBoolean("widget." + appWidgetId + ".separators", cbSeparatorLines.isChecked());
                editor.putInt("widget." + appWidgetId + ".font", tinyOut(font));
                editor.putInt("widget." + appWidgetId + ".padding", tinyOut(padding));
                editor.putInt("widget." + appWidgetId + ".subject_lines", spSubjectLines.getSelectedItemPosition() + 1);
                editor.putBoolean("widget." + appWidgetId + ".avatars", cbAvatars.isChecked());
                editor.putBoolean("widget." + appWidgetId + ".account_name", cbAccountName.isChecked());
                editor.putBoolean("widget." + appWidgetId + ".caption", cbCaption.isChecked());
                editor.putBoolean("widget." + appWidgetId + ".refresh", cbRefresh.isChecked());
                editor.putBoolean("widget." + appWidgetId + ".compose", cbCompose.isChecked());
                editor.putBoolean("widget." + appWidgetId + ".standalone", cbStandalone.isChecked());
                editor.putInt("widget." + appWidgetId + ".version", BuildConfig.VERSION_CODE);

                editor.apply();

                WidgetUnified.init(ActivityWidgetUnified.this, appWidgetId);

                setResult(RESULT_OK, resultValue);
                finish();
            }
        });

        adapterAccount = new ArrayAdapter<>(this, R.layout.spinner_item1, android.R.id.text1, new ArrayList<EntityAccount>());
        adapterAccount.setDropDownViewResource(R.layout.spinner_item1_dropdown);
        spAccount.setAdapter(adapterAccount);

        adapterFolder = new ArrayAdapter<TupleFolderEx>(this, R.layout.spinner_item1, android.R.id.text1, new ArrayList<TupleFolderEx>()) {
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
                TupleFolderEx folder = getItem(position);
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
                cbAccountName.setEnabled(account.id < 0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                setFolders(-1);
            }

            private void setFolders(long account) {
                Bundle args = new Bundle();
                args.putLong("account", account);

                new SimpleTask<List<TupleFolderEx>>() {
                    @Override
                    protected List<TupleFolderEx> onExecute(Context context, Bundle args) {
                        long account = args.getLong("account");

                        DB db = DB.getInstance(context);
                        List<TupleFolderEx> folders = db.folder().getFoldersEx(account);
                        if (folders != null && folders.size() > 0)
                            Collections.sort(folders, folders.get(0).getComparator(context));
                        return folders;
                    }

                    @Override
                    protected void onExecuted(Bundle args, List<TupleFolderEx> folders) {
                        if (folders == null)
                            folders = new ArrayList<>();

                        TupleFolderEx unified = new TupleFolderEx();
                        unified.id = -1L;
                        unified.name = getString(R.string.title_widget_folder_unified);
                        folders.add(0, unified);

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
                }.execute(ActivityWidgetUnified.this, args, "widget:folders");
            }
        });

        List<String> sizes = new ArrayList<>();
        sizes.addAll(Arrays.asList(getResources().getStringArray(R.array.fontSizeNames)));
        sizes.add(1, getString(R.string.title_size_tiny));

        adapterFontSize = new ArrayAdapter<>(this, R.layout.spinner_item1, android.R.id.text1, sizes);
        adapterFontSize.setDropDownViewResource(R.layout.spinner_item1_dropdown);
        spFontSize.setAdapter(adapterFontSize);

        adapterPadding = new ArrayAdapter<>(this, R.layout.spinner_item1, android.R.id.text1, sizes);
        adapterPadding.setDropDownViewResource(R.layout.spinner_item1_dropdown);
        spPadding.setAdapter(adapterPadding);

        // Initialize
        cbUnseen.setChecked(unseen);
        cbShowUnseen.setChecked(show_unseen);
        cbShowUnseen.setEnabled(cbUnseen.isChecked());
        cbFlagged.setChecked(flagged);
        cbShowFlagged.setChecked(show_flagged);
        cbShowFlagged.setEnabled(!flagged);
        cbDayNight.setChecked(daynight);
        cbDayNight.setVisibility(Build.VERSION.SDK_INT < Build.VERSION_CODES.S ? View.GONE : View.VISIBLE);
        cbHighlight.setChecked(highlight);
        cbHighlight.setEnabled(!daynight);
        btnHighlight.setVisibility(highlight ? View.VISIBLE : View.GONE);
        btnHighlight.setColor(highlight_color);
        btnHighlight.setEnabled(highlight && !daynight);
        cbSemiTransparent.setChecked(semi);
        cbSemiTransparent.setEnabled(!daynight);
        btnColor.setColor(background);
        btnColor.setEnabled(!daynight);
        cbSeparatorLines.setChecked(separators);
        spFontSize.setSelection(tinyIn(font));
        spPadding.setSelection(tinyIn(padding));
        cbAvatars.setChecked(avatars);
        cbAccountName.setChecked(account_name);
        spSubjectLines.setSelection(subject_lines - 1);
        tvSubjectLinesHint.setText(getString(R.string.title_advanced_preview_lines_hint, NF.format(HtmlHelper.PREVIEW_SIZE)));
        cbCaption.setChecked(caption);
        etName.setText(name);
        etName.setEnabled(caption);
        cbRefresh.setChecked(refresh);
        cbCompose.setChecked(compose);
        cbStandalone.setChecked(standalone);

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

    private int tinyOut(int value) {
        if (value == 1) // tiny
            return 4;
        else if (value > 1)
            return value - 1;
        else
            return value;
    }

    private int tinyIn(int value) {
        if (value == 4)
            return 1;
        else if (value >= 1)
            return value + 1;
        else
            return value;
    }
}
