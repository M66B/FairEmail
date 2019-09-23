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

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.lifecycle.Lifecycle;
import androidx.preference.PreferenceManager;

public class FragmentOptionsDisplay extends FragmentBase implements SharedPreferences.OnSharedPreferenceChangeListener {
    private Button btnTheme;
    private Spinner spStartup;
    private SwitchCompat swCards;
    private SwitchCompat swDate;
    private SwitchCompat swThreading;
    private SwitchCompat swHighlightUnread;
    private SwitchCompat swAvatars;
    private SwitchCompat swGeneratedIcons;
    private SwitchCompat swIdenticons;
    private SwitchCompat swCircular;
    private SwitchCompat swNameEmail;
    private SwitchCompat swAuthentication;
    private SwitchCompat swSubjectTop;
    private SwitchCompat swSubjectItalic;
    private Spinner spSubjectEllipsize;
    private SwitchCompat swFlags;
    private SwitchCompat swPreview;
    private SwitchCompat swPreviewItalic;
    private SwitchCompat swAddresses;
    private SwitchCompat swAttachmentsAlt;

    private SwitchCompat swContrast;
    private SwitchCompat swMonospaced;
    private SwitchCompat swTextColor;
    private SwitchCompat swCollapseQuotes;
    private SwitchCompat swImagesInline;
    private SwitchCompat swRemoteContent;
    private SwitchCompat swSeekbar;
    private SwitchCompat swActionbar;

    private final static String[] RESET_OPTIONS = new String[]{
            "theme", "startup", "cards", "date", "threading", "highlight_unread",
            "avatars", "generated_icons", "identicons", "circular", "name_email",
            "authentication", "subject_top", "subject_italic", "subject_ellipsize",
            "flags", "preview", "preview_italic", "addresses", "attachments_alt",
            "contrast", "monospaced", "text_color",
            "inline_images", "collapse_quotes", "autocontent", "seekbar", "actionbar",
    };

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_setup);
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_options_display, container, false);

        // Get controls

        btnTheme = view.findViewById(R.id.btnTheme);
        spStartup = view.findViewById(R.id.spStartup);
        swCards = view.findViewById(R.id.swCards);
        swDate = view.findViewById(R.id.swDate);
        swThreading = view.findViewById(R.id.swThreading);
        swHighlightUnread = view.findViewById(R.id.swHighlightUnread);
        swAvatars = view.findViewById(R.id.swAvatars);
        swGeneratedIcons = view.findViewById(R.id.swGeneratedIcons);
        swIdenticons = view.findViewById(R.id.swIdenticons);
        swCircular = view.findViewById(R.id.swCircular);
        swNameEmail = view.findViewById(R.id.swNameEmail);
        swAuthentication = view.findViewById(R.id.swAuthentication);
        swSubjectTop = view.findViewById(R.id.swSubjectTop);
        swSubjectItalic = view.findViewById(R.id.swSubjectItalic);
        spSubjectEllipsize = view.findViewById(R.id.spSubjectEllipsize);
        swFlags = view.findViewById(R.id.swFlags);
        swPreview = view.findViewById(R.id.swPreview);
        swPreviewItalic = view.findViewById(R.id.swPreviewItalic);
        swAddresses = view.findViewById(R.id.swAddresses);
        swAttachmentsAlt = view.findViewById(R.id.swAttachmentsAlt);
        swContrast = view.findViewById(R.id.swContrast);
        swMonospaced = view.findViewById(R.id.swMonospaced);
        swTextColor = view.findViewById(R.id.swTextColor);
        swCollapseQuotes = view.findViewById(R.id.swCollapseQuotes);
        swImagesInline = view.findViewById(R.id.swImagesInline);
        swRemoteContent = view.findViewById(R.id.swRemoteContent);
        swSeekbar = view.findViewById(R.id.swSeekbar);
        swActionbar = view.findViewById(R.id.swActionbar);

        setOptions();

        // Wire controls

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        btnTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new FragmentDialogTheme().show(getFragmentManager(), "setup:theme");
            }
        });

        spStartup.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String[] values = getResources().getStringArray(R.array.startupValues);
                prefs.edit().putString("startup", values[position]).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                prefs.edit().remove("startup").apply();
            }
        });

        swCards.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("cards", checked).apply();
            }
        });

        swDate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("date", checked).apply();
            }
        });

        swThreading.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("threading", checked).apply();
                WidgetUnified.update(getContext());
            }
        });

        swHighlightUnread.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("highlight_unread", checked).apply();
            }
        });

        swAvatars.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("avatars", checked).apply();
                ContactInfo.clearCache();
            }
        });

        swGeneratedIcons.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("generated_icons", checked).apply();
                swIdenticons.setEnabled(checked);
                ContactInfo.clearCache();
            }
        });

        swIdenticons.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("identicons", checked).apply();
                ContactInfo.clearCache();
            }
        });

        swCircular.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("circular", checked).apply();
                ContactInfo.clearCache();
            }
        });

        swNameEmail.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("name_email", checked).apply();
            }
        });

        swAuthentication.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("authentication", checked).apply();
            }
        });

        swSubjectTop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("subject_top", checked).apply();
            }
        });

        swSubjectItalic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("subject_italic", checked).apply();
            }
        });

        spSubjectEllipsize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String[] values = getResources().getStringArray(R.array.ellipsizeValues);
                prefs.edit().putString("subject_ellipsize", values[position]).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                prefs.edit().remove("subject_ellipsize").apply();
            }
        });

        swFlags.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("flags", checked).apply();
            }
        });

        swPreview.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("preview", checked).apply();
                swPreviewItalic.setEnabled(checked);
            }
        });

        swPreviewItalic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("preview_italic", checked).apply();
            }
        });

        swAddresses.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("addresses", checked).apply();
            }
        });

        swAttachmentsAlt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("attachments_alt", checked).apply();
            }
        });

        swContrast.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("contrast", checked).apply();
            }
        });

        swMonospaced.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("monospaced", checked).apply();
            }
        });

        swTextColor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("text_color", checked).apply();
            }
        });

        swCollapseQuotes.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("collapse_quotes", checked).apply();
            }
        });

        swImagesInline.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("inline_images", checked).apply();
            }
        });

        swRemoteContent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("autocontent", checked).apply();
            }
        });

        swSeekbar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("seekbar", checked).apply();
            }
        });

        swActionbar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("actionbar", checked).apply();
            }
        });

        PreferenceManager.getDefaultSharedPreferences(getContext()).registerOnSharedPreferenceChangeListener(this);

        return view;
    }

    @Override
    public void onDestroyView() {
        PreferenceManager.getDefaultSharedPreferences(getContext()).unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroyView();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
            setOptions();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_options, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_default:
                onMenuDefault();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onMenuDefault() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = prefs.edit();
        for (String option : RESET_OPTIONS)
            editor.remove(option);
        editor.apply();
        ToastEx.makeText(getContext(), R.string.title_setup_done, Toast.LENGTH_LONG).show();
    }

    private void setOptions() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        String startup = prefs.getString("startup", "unified");
        String[] startupValues = getResources().getStringArray(R.array.startupValues);
        for (int pos = 0; pos < startupValues.length; pos++)
            if (startupValues[pos].equals(startup)) {
                spStartup.setSelection(pos);
                break;
            }

        swCards.setChecked(prefs.getBoolean("cards", true));
        swDate.setChecked(prefs.getBoolean("date", true));
        swThreading.setChecked(prefs.getBoolean("threading", true));
        swHighlightUnread.setChecked(prefs.getBoolean("highlight_unread", false));
        swAvatars.setChecked(prefs.getBoolean("avatars", true));
        swGeneratedIcons.setChecked(prefs.getBoolean("generated_icons", true));
        swIdenticons.setChecked(prefs.getBoolean("identicons", false));
        swIdenticons.setEnabled(swGeneratedIcons.isChecked());
        swCircular.setChecked(prefs.getBoolean("circular", true));
        swNameEmail.setChecked(prefs.getBoolean("name_email", false));
        swAuthentication.setChecked(prefs.getBoolean("authentication", true));
        swSubjectTop.setChecked(prefs.getBoolean("subject_top", false));
        swSubjectItalic.setChecked(prefs.getBoolean("subject_italic", true));

        String subject_ellipsize = prefs.getString("subject_ellipsize", "middle");
        String[] ellipsizeValues = getResources().getStringArray(R.array.ellipsizeValues);
        for (int pos = 0; pos < startupValues.length; pos++)
            if (ellipsizeValues[pos].equals(subject_ellipsize)) {
                spSubjectEllipsize.setSelection(pos);
                break;
            }

        swFlags.setChecked(prefs.getBoolean("flags", true));
        swPreview.setChecked(prefs.getBoolean("preview", false));
        swPreviewItalic.setChecked(prefs.getBoolean("preview_italic", true));
        swPreviewItalic.setEnabled(swPreview.isChecked());
        swAddresses.setChecked(prefs.getBoolean("addresses", false));
        swAttachmentsAlt.setChecked(prefs.getBoolean("attachments_alt", false));
        swContrast.setChecked(prefs.getBoolean("contrast", false));
        swMonospaced.setChecked(prefs.getBoolean("monospaced", false));
        swTextColor.setChecked(prefs.getBoolean("text_color", true));
        swCollapseQuotes.setChecked(prefs.getBoolean("collapse_quotes", false));
        swImagesInline.setChecked(prefs.getBoolean("inline_images", false));
        swRemoteContent.setChecked(prefs.getBoolean("autocontent", false));
        swSeekbar.setChecked(prefs.getBoolean("seekbar", false));
        swActionbar.setChecked(prefs.getBoolean("actionbar", true));
    }

    public static class FragmentDialogTheme extends FragmentDialogBase {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            View dview = LayoutInflater.from(getContext()).inflate(R.layout.dialog_theme, null);
            final RadioGroup rgTheme = dview.findViewById(R.id.rgTheme);

            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            String theme = prefs.getString("theme", "light");

            switch (theme) {
                case "dark":
                    rgTheme.check(R.id.rbThemeDark);
                    break;
                case "black":
                    rgTheme.check(R.id.rbThemeBlack);
                    break;
                case "grey_light":
                    rgTheme.check(R.id.rbThemeGreyLight);
                    break;
                case "grey_dark":
                    rgTheme.check(R.id.rbThemeGreyDark);
                    break;
                case "system":
                    rgTheme.check(R.id.rbThemeSystem);
                    break;
                case "grey_system":
                    rgTheme.check(R.id.rbThemeGreySystem);
                    break;
                default:
                    rgTheme.check(R.id.rbThemeLight);
            }

            rgTheme.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    getActivity().getIntent().putExtra("tab", "display");

                    switch (checkedId) {
                        case R.id.rbThemeLight:
                            prefs.edit().putString("theme", "light").apply();
                            break;
                        case R.id.rbThemeDark:
                            prefs.edit().putString("theme", "dark").apply();
                            break;
                        case R.id.rbThemeBlack:
                            prefs.edit().putString("theme", "black").apply();
                            break;
                        case R.id.rbThemeGreyLight:
                            prefs.edit().putString("theme", "grey_light").apply();
                            break;
                        case R.id.rbThemeGreyDark:
                            prefs.edit().putString("theme", "grey_dark").apply();
                            break;
                        case R.id.rbThemeSystem:
                            prefs.edit().putString("theme", "system").apply();
                            break;
                        case R.id.rbThemeGreySystem:
                            prefs.edit().putString("theme", "grey_system").apply();
                            break;
                    }
                }
            });

            return new AlertDialog.Builder(getContext())
                    .setView(dview)
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
        }
    }
}
