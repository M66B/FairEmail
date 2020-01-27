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

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.lifecycle.Lifecycle;
import androidx.preference.PreferenceManager;

public class FragmentOptionsDisplay extends FragmentBase implements SharedPreferences.OnSharedPreferenceChangeListener {
    private Button btnTheme;
    private SwitchCompat swLandscape;
    private SwitchCompat swLandscape3;
    private Spinner spStartup;
    private SwitchCompat swCards;
    private SwitchCompat swDate;
    private SwitchCompat swThreading;
    private SwitchCompat swIndentation;
    private SwitchCompat swHighlightUnread;
    private SwitchCompat swColorStripe;
    private SwitchCompat swAvatars;
    private TextView tvGravatarsHint;
    private SwitchCompat swGravatars;
    private SwitchCompat swGeneratedIcons;
    private SwitchCompat swIdenticons;
    private SwitchCompat swCircular;
    private ImageView ivRed;
    private ImageView ivGreen;
    private ImageView ivBlue;
    private SeekBar sbSaturation;
    private SeekBar sbBrightness;
    private SeekBar sbThreshold;
    private SwitchCompat swNameEmail;
    private SwitchCompat swDistinguishContacts;
    private SwitchCompat swAuthentication;
    private SwitchCompat swSubjectTop;
    private Spinner spFontSizeSender;
    private Spinner spFontSizeSubject;
    private SwitchCompat swSubjectItalic;
    private Spinner spSubjectEllipsize;
    private SwitchCompat swKeywords;
    private SwitchCompat swFlags;
    private SwitchCompat swFlagsBackground;
    private SwitchCompat swPreview;
    private SwitchCompat swPreviewItalic;
    private Spinner spPreviewLines;
    private SwitchCompat swAddresses;
    private SwitchCompat swAttachmentsAlt;

    private SwitchCompat swContrast;
    private SwitchCompat swMonospaced;
    private SwitchCompat swTextColor;
    private SwitchCompat swCollapseQuotes;
    private SwitchCompat swImagesInline;
    private SwitchCompat swSeekbar;
    private SwitchCompat swActionbar;

    private final static String[] RESET_OPTIONS = new String[]{
            "theme", "landscape", "landscape3", "startup", "cards", "indentation", "date", "threading", "highlight_unread", "color_stripe",
            "avatars", "gravatars", "generated_icons", "identicons", "circular", "saturation", "brightness", "threshold",
            "name_email", "distinguish_contacts", "authentication",
            "subject_top", "font_size_sender", "font_size_subject", "subject_italic", "subject_ellipsize", "keywords_header",
            "flags", "flags_background", "preview", "preview_italic", "preview_lines", "addresses", "attachments_alt",
            "contrast", "monospaced", "text_color",
            "inline_images", "collapse_quotes", "seekbar", "actionbar",
    };

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_setup);
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_options_display, container, false);

        // Get controls

        btnTheme = view.findViewById(R.id.btnTheme);
        swLandscape = view.findViewById(R.id.swLandscape);
        swLandscape3 = view.findViewById(R.id.swLandscape3);
        spStartup = view.findViewById(R.id.spStartup);
        swCards = view.findViewById(R.id.swCards);
        swIndentation = view.findViewById(R.id.swIndentation);
        swDate = view.findViewById(R.id.swDate);
        swThreading = view.findViewById(R.id.swThreading);
        swHighlightUnread = view.findViewById(R.id.swHighlightUnread);
        swColorStripe = view.findViewById(R.id.swColorStripe);
        swAvatars = view.findViewById(R.id.swAvatars);
        swGravatars = view.findViewById(R.id.swGravatars);
        tvGravatarsHint = view.findViewById(R.id.tvGravatarsHint);
        swGeneratedIcons = view.findViewById(R.id.swGeneratedIcons);
        swIdenticons = view.findViewById(R.id.swIdenticons);
        swCircular = view.findViewById(R.id.swCircular);
        ivRed = view.findViewById(R.id.ivRed);
        ivGreen = view.findViewById(R.id.ivGreen);
        ivBlue = view.findViewById(R.id.ivBlue);
        sbSaturation = view.findViewById(R.id.sbSaturation);
        sbBrightness = view.findViewById(R.id.sbBrightness);
        sbThreshold = view.findViewById(R.id.sbThreshold);
        swNameEmail = view.findViewById(R.id.swNameEmail);
        swDistinguishContacts = view.findViewById(R.id.swDistinguishContacts);
        swAuthentication = view.findViewById(R.id.swAuthentication);
        swSubjectTop = view.findViewById(R.id.swSubjectTop);
        spFontSizeSender = view.findViewById(R.id.spFontSizeSender);
        spFontSizeSubject = view.findViewById(R.id.spFontSizeSubject);
        swSubjectItalic = view.findViewById(R.id.swSubjectItalic);
        spSubjectEllipsize = view.findViewById(R.id.spSubjectEllipsize);
        swKeywords = view.findViewById(R.id.swKeywords);
        swFlags = view.findViewById(R.id.swFlags);
        swFlagsBackground = view.findViewById(R.id.swFlagsBackground);
        swPreview = view.findViewById(R.id.swPreview);
        swPreviewItalic = view.findViewById(R.id.swPreviewItalic);
        spPreviewLines = view.findViewById(R.id.spPreviewLines);
        swAddresses = view.findViewById(R.id.swAddresses);
        swAttachmentsAlt = view.findViewById(R.id.swAttachmentsAlt);
        swContrast = view.findViewById(R.id.swContrast);
        swMonospaced = view.findViewById(R.id.swMonospaced);
        swTextColor = view.findViewById(R.id.swTextColor);
        swCollapseQuotes = view.findViewById(R.id.swCollapseQuotes);
        swImagesInline = view.findViewById(R.id.swImagesInline);
        swSeekbar = view.findViewById(R.id.swSeekbar);
        swActionbar = view.findViewById(R.id.swActionbar);

        setOptions();

        // Wire controls

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        btnTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new FragmentDialogTheme().show(getParentFragmentManager(), "setup:theme");
            }
        });

        swLandscape.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("landscape", checked).apply();
                swLandscape3.setEnabled(checked);
            }
        });

        swLandscape3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("landscape3", checked).apply();
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
                swIndentation.setEnabled(checked);
            }
        });

        swIndentation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("indentation", checked).apply();
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
                WidgetUnified.updateData(getContext());
            }
        });

        swHighlightUnread.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("highlight_unread", checked).apply();
            }
        });

        swColorStripe.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("color_stripe", checked).apply();
            }
        });

        swAvatars.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("avatars", checked).apply();
                ContactInfo.clearCache();
            }
        });

        swGravatars.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("gravatars", checked).apply();
                ContactInfo.clearCache();
            }
        });

        tvGravatarsHint.getPaint().setUnderlineText(true);
        tvGravatarsHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.view(getContext(), Uri.parse(Helper.GRAVATAR_PRIVACY_URI), true);
            }
        });

        swGeneratedIcons.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("generated_icons", checked).apply();
                swIdenticons.setEnabled(checked);
                sbSaturation.setEnabled(checked);
                sbBrightness.setEnabled(checked);
                sbThreshold.setEnabled(checked);
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
                updateColor();
                ContactInfo.clearCache();
            }
        });

        sbSaturation.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                prefs.edit().putInt("saturation", progress).apply();
                updateColor();
                ContactInfo.clearCache();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }
        });

        sbBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                prefs.edit().putInt("brightness", progress).apply();
                updateColor();
                ContactInfo.clearCache();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }
        });

        sbThreshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                prefs.edit().putInt("threshold", progress).apply();
                updateColor();
                ContactInfo.clearCache();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }
        });

        swNameEmail.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("name_email", checked).apply();
            }
        });

        swDistinguishContacts.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("distinguish_contacts", checked).apply();
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
                WidgetUnified.updateData(getContext());
            }
        });

        spFontSizeSender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                int[] values = getResources().getIntArray(R.array.fontSizeValues);
                prefs.edit().putInt("font_size_sender", values[position]).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                prefs.edit().remove("font_size_sender").apply();
            }
        });

        spFontSizeSubject.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                int[] values = getResources().getIntArray(R.array.fontSizeValues);
                prefs.edit().putInt("font_size_subject", values[position]).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                prefs.edit().remove("font_size_subject").apply();
            }
        });

        swSubjectItalic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("subject_italic", checked).apply();
                WidgetUnified.updateData(getContext());
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

        swKeywords.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("keywords_header", checked).apply();
            }
        });

        swFlags.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("flags", checked).apply();
            }
        });

        swFlagsBackground.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("flags_background", checked).apply();
            }
        });

        swPreview.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("preview", checked).apply();
                swPreviewItalic.setEnabled(checked);
                spPreviewLines.setEnabled(checked);
            }
        });

        swPreviewItalic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("preview_italic", checked).apply();
            }
        });

        spPreviewLines.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                prefs.edit().putInt("preview_lines", position + 1).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                prefs.edit().remove("preview_lines").apply();
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

        boolean normal = getResources().getConfiguration()
                .isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_NORMAL);

        swLandscape.setChecked(prefs.getBoolean("landscape", true));
        swLandscape.setEnabled(normal);
        swLandscape3.setChecked(prefs.getBoolean("landscape3", true));
        swLandscape3.setEnabled(normal && swLandscape.isChecked());

        String startup = prefs.getString("startup", "unified");
        String[] startupValues = getResources().getStringArray(R.array.startupValues);
        for (int pos = 0; pos < startupValues.length; pos++)
            if (startupValues[pos].equals(startup)) {
                spStartup.setSelection(pos);
                break;
            }

        swCards.setChecked(prefs.getBoolean("cards", true));
        swIndentation.setChecked(prefs.getBoolean("indentation", false));
        swIndentation.setEnabled(swCards.isChecked());
        swDate.setChecked(prefs.getBoolean("date", true));
        swThreading.setChecked(prefs.getBoolean("threading", true));
        swHighlightUnread.setChecked(prefs.getBoolean("highlight_unread", false));
        swColorStripe.setChecked(prefs.getBoolean("color_stripe", true));
        swAvatars.setChecked(prefs.getBoolean("avatars", true));
        swGravatars.setChecked(prefs.getBoolean("gravatars", false));
        swGeneratedIcons.setChecked(prefs.getBoolean("generated_icons", true));
        swIdenticons.setChecked(prefs.getBoolean("identicons", false));
        swIdenticons.setEnabled(swGeneratedIcons.isChecked());
        swCircular.setChecked(prefs.getBoolean("circular", true));

        sbSaturation.setProgress(prefs.getInt("saturation", 100));
        sbSaturation.setEnabled(swGeneratedIcons.isChecked());
        sbBrightness.setProgress(prefs.getInt("brightness", 100));
        sbBrightness.setEnabled(swGeneratedIcons.isChecked());
        sbThreshold.setProgress(prefs.getInt("threshold", 50));
        sbThreshold.setEnabled(swGeneratedIcons.isChecked());

        swNameEmail.setChecked(prefs.getBoolean("name_email", false));
        swDistinguishContacts.setChecked(prefs.getBoolean("distinguish_contacts", false));
        swAuthentication.setChecked(prefs.getBoolean("authentication", true));

        swSubjectTop.setChecked(prefs.getBoolean("subject_top", false));

        int[] fontSizeValues = getResources().getIntArray(R.array.fontSizeValues);

        int font_size_sender = prefs.getInt("font_size_sender", -1);
        for (int pos = 0; pos < fontSizeValues.length; pos++)
            if (fontSizeValues[pos] == font_size_sender) {
                spFontSizeSender.setSelection(pos);
                break;
            }

        int font_size_subject = prefs.getInt("font_size_subject", -1);
        for (int pos = 0; pos < fontSizeValues.length; pos++)
            if (fontSizeValues[pos] == font_size_subject) {
                spFontSizeSubject.setSelection(pos);
                break;
            }

        swSubjectItalic.setChecked(prefs.getBoolean("subject_italic", true));

        String subject_ellipsize = prefs.getString("subject_ellipsize", "middle");
        String[] ellipsizeValues = getResources().getStringArray(R.array.ellipsizeValues);
        for (int pos = 0; pos < startupValues.length; pos++)
            if (ellipsizeValues[pos].equals(subject_ellipsize)) {
                spSubjectEllipsize.setSelection(pos);
                break;
            }

        swKeywords.setChecked(prefs.getBoolean("keywords_header", false));
        swFlags.setChecked(prefs.getBoolean("flags", true));
        swFlagsBackground.setChecked(prefs.getBoolean("flags_background", false));
        swPreview.setChecked(prefs.getBoolean("preview", false));
        swPreviewItalic.setChecked(prefs.getBoolean("preview_italic", true));
        swPreviewItalic.setEnabled(swPreview.isChecked());
        spPreviewLines.setSelection(prefs.getInt("preview_lines", 2) - 1);
        spPreviewLines.setEnabled(swPreview.isChecked());
        swAddresses.setChecked(prefs.getBoolean("addresses", false));
        swAttachmentsAlt.setChecked(prefs.getBoolean("attachments_alt", false));
        swContrast.setChecked(prefs.getBoolean("contrast", false));
        swMonospaced.setChecked(prefs.getBoolean("monospaced", false));
        swTextColor.setChecked(prefs.getBoolean("text_color", true));
        swCollapseQuotes.setChecked(prefs.getBoolean("collapse_quotes", false));
        swImagesInline.setChecked(prefs.getBoolean("inline_images", false));
        swSeekbar.setChecked(prefs.getBoolean("seekbar", false));
        swActionbar.setChecked(prefs.getBoolean("actionbar", true));

        updateColor();
    }

    private void updateColor() {
        Context context = getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean identicons = prefs.getBoolean("identicons", false);
        boolean circular = prefs.getBoolean("circular", true);

        int size = Helper.dp2pixels(context, 36);
        byte[] ahash = ImageHelper.getHash("abc@example.com");
        byte[] bhash = ImageHelper.getHash("bcd@example.com");
        byte[] chash = ImageHelper.getHash("cde@example.com");
        Integer radius = (circular && !identicons ? null : Helper.dp2pixels(context, 3));

        Bitmap red = (identicons
                ? ImageHelper.generateIdenticon(ahash, 0f, size, 5, context)
                : ImageHelper.generateLetterIcon("A", 0f, size, context));

        Bitmap green = (identicons
                ? ImageHelper.generateIdenticon(bhash, 120f, size, 5, context)
                : ImageHelper.generateLetterIcon("B", 120f, size, context));

        Bitmap blue = (identicons
                ? ImageHelper.generateIdenticon(chash, 240f, size, 5, context)
                : ImageHelper.generateLetterIcon("C", 240f, size, context));

        ivRed.setImageBitmap(ImageHelper.makeCircular(red, radius));
        ivGreen.setImageBitmap(ImageHelper.makeCircular(green, radius));
        ivBlue.setImageBitmap(ImageHelper.makeCircular(blue, radius));
    }

    public static class FragmentDialogTheme extends FragmentDialogBase {
        private ImageButton itten;
        private RadioGroup rgTheme;
        private SwitchCompat swReverse;
        private SwitchCompat swDark;
        private SwitchCompat swBlack;
        private SwitchCompat swSystem;

        private void eval() {
            int checkedId = rgTheme.getCheckedRadioButtonId();

            swReverse.setEnabled(checkedId == R.id.rbThemeBlueOrange ||
                    checkedId == R.id.rbThemeYellowPurple ||
                    checkedId == R.id.rbThemeRedGreen);
            swDark.setEnabled(checkedId == R.id.rbThemeBlueOrange ||
                    checkedId == R.id.rbThemeYellowPurple ||
                    checkedId == R.id.rbThemeRedGreen ||
                    checkedId == R.id.rbThemeGrey);
            swBlack.setEnabled(checkedId == R.id.rbThemeBlueOrange ||
                    checkedId == R.id.rbThemeYellowPurple ||
                    checkedId == R.id.rbThemeRedGreen);
            swSystem.setEnabled(swDark.isEnabled());

            boolean reverse = (swReverse.isEnabled() && swReverse.isChecked());
            boolean dark = (swDark.isEnabled() && swDark.isChecked());
            boolean black = (swBlack.isEnabled() && swBlack.isChecked());
            boolean system = (swSystem.isEnabled() && swSystem.isChecked());

            swReverse.setEnabled(swReverse.isEnabled() && !system);
            swDark.setEnabled(swDark.isEnabled() && !system);
            swBlack.setEnabled(swBlack.isEnabled() && dark);
            swSystem.setEnabled(swSystem.isEnabled() && !reverse && !dark && !black);
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            View dview = LayoutInflater.from(getContext()).inflate(R.layout.dialog_theme, null);
            itten = dview.findViewById(R.id.itten);
            rgTheme = dview.findViewById(R.id.rgTheme);
            swReverse = dview.findViewById(R.id.swReverse);
            swDark = dview.findViewById(R.id.swDark);
            swBlack = dview.findViewById(R.id.swBlack);
            swSystem = dview.findViewById(R.id.swSystem);

            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            String theme = prefs.getString("theme", "light");

            itten.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri uri = Uri.parse("https://en.wikipedia.org/wiki/Johannes_Itten");
                    Helper.view(getContext(), uri, false);
                }
            });

            swReverse.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    eval();
                }
            });

            swDark.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    eval();
                }
            });

            swBlack.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    eval();
                }
            });

            swSystem.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    eval();
                }
            });

            rgTheme.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    eval();
                }
            });

            boolean colored =
                    (theme.startsWith("orange_blue") ||
                            theme.startsWith("purple_yellow") ||
                            theme.startsWith("green_red"));
            boolean dark = theme.endsWith("dark");
            boolean black = (!"black".equals(theme) && theme.endsWith("black"));
            boolean system = theme.endsWith("system");

            swReverse.setChecked(colored);
            swDark.setChecked(dark || black);
            swBlack.setChecked(black);
            swSystem.setChecked(system);

            switch (theme) {
                case "light":
                case "dark":
                case "system":
                case "blue_orange_system":
                case "blue_orange_light":
                case "blue_orange_dark":
                case "blue_orange_black":
                case "orange_blue_light":
                case "orange_blue_dark":
                case "orange_blue_black":
                    rgTheme.check(R.id.rbThemeBlueOrange);
                    break;
                case "yellow_purple_system":
                case "yellow_purple_light":
                case "yellow_purple_dark":
                case "yellow_purple_black":
                case "purple_yellow_light":
                case "purple_yellow_dark":
                case "purple_yellow_black":
                    rgTheme.check(R.id.rbThemeYellowPurple);
                    break;
                case "red_green_system":
                case "red_green_light":
                case "red_green_dark":
                case "red_green_black":
                case "green_red_light":
                case "green_red_dark":
                case "green_red_black":
                    rgTheme.check(R.id.rbThemeRedGreen);
                    break;
                case "grey_system":
                case "grey_light":
                case "grey_dark":
                    rgTheme.check(R.id.rbThemeGrey);
                    break;

                case "black":
                    rgTheme.check(R.id.rbThemeBlack);
                    break;
            }

            return new AlertDialog.Builder(getContext())
                    .setView(dview)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getActivity().getIntent().putExtra("tab", "display");

                            ContactInfo.clearCache();

                            boolean reverse = (swReverse.isEnabled() && swReverse.isChecked());
                            boolean dark = (swDark.isEnabled() && swDark.isChecked());
                            boolean black = (swBlack.isEnabled() && swBlack.isChecked());
                            boolean system = (swSystem.isEnabled() && swSystem.isChecked());

                            switch (rgTheme.getCheckedRadioButtonId()) {
                                case R.id.rbThemeBlueOrange:
                                    if (system)
                                        prefs.edit().putString("theme", "blue_orange_system").apply();
                                    else
                                        prefs.edit().putString("theme",
                                                (reverse ? "orange_blue" : "blue_orange") +
                                                        (black ? "_black" : dark ? "_dark" : "_light")).apply();
                                    break;
                                case R.id.rbThemeYellowPurple:
                                    if (system)
                                        prefs.edit().putString("theme", "yellow_purple_system").apply();
                                    else
                                        prefs.edit().putString("theme",
                                                (reverse ? "purple_yellow" : "yellow_purple") +
                                                        (black ? "_black" : dark ? "_dark" : "_light")).apply();
                                    break;
                                case R.id.rbThemeRedGreen:
                                    if (system)
                                        prefs.edit().putString("theme", "red_green_system").apply();
                                    else
                                        prefs.edit().putString("theme",
                                                (reverse ? "green_red" : "red_green") +
                                                        (black ? "_black" : dark ? "_dark" : "_light")).apply();
                                    break;
                                case R.id.rbThemeGrey:
                                    if (system)
                                        prefs.edit().putString("theme", "grey_system").apply();
                                    else
                                        prefs.edit().putString("theme",
                                                "grey" + (dark ? "_dark" : "_light")).apply();
                                    break;
                                case R.id.rbThemeBlack:
                                    prefs.edit().putString("theme", "black").apply();
                                    break;
                            }
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
        }
    }
}
