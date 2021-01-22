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

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.preference.PreferenceManager;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.text.NumberFormat;

public class FragmentOptionsDisplay extends FragmentBase implements SharedPreferences.OnSharedPreferenceChangeListener {
    private Button btnTheme;
    private Spinner spStartup;
    private SwitchCompat swCards;
    private SwitchCompat swBeige;
    private SwitchCompat swDate;
    private SwitchCompat swNavBarColorize;
    private SwitchCompat swPortrait2;
    private SwitchCompat swLandscape;
    private SwitchCompat swLandscape3;

    private SwitchCompat swThreading;
    private SwitchCompat swThreadingUnread;
    private SwitchCompat swIndentation;
    private SwitchCompat swSeekbar;
    private SwitchCompat swActionbar;
    private SwitchCompat swActionbarColor;

    private SwitchCompat swHighlightUnread;
    private ViewButtonColor btnHighlightColor;
    private SwitchCompat swColorStripe;
    private SwitchCompat swAvatars;
    private TextView tvGravatarsHint;
    private SwitchCompat swGravatars;
    private SwitchCompat swFavicons;
    private SwitchCompat swGeneratedIcons;
    private SwitchCompat swIdenticons;
    private SwitchCompat swCircular;
    private ImageView ivRed;
    private ImageView ivGreen;
    private ImageView ivBlue;
    private TextView tvSaturation;
    private SeekBar sbSaturation;
    private TextView tvBrightness;
    private SeekBar sbBrightness;
    private TextView tvThreshold;
    private SeekBar sbThreshold;
    private SwitchCompat swNameEmail;
    private SwitchCompat swPreferContact;
    private SwitchCompat swDistinguishContacts;
    private SwitchCompat swShowRecipients;
    private SwitchCompat swSubjectTop;
    private Spinner spFontSizeSender;
    private Spinner spFontSizeSubject;
    private SwitchCompat swSubjectItalic;
    private SwitchCompat swHighlightSubject;
    private Spinner spSubjectEllipsize;
    private SwitchCompat swKeywords;
    private SwitchCompat swLabels;
    private SwitchCompat swFlags;
    private SwitchCompat swFlagsBackground;
    private SwitchCompat swPreview;
    private SwitchCompat swPreviewItalic;
    private Spinner spPreviewLines;

    private SwitchCompat swAddresses;
    private EditText etMessageZoom;
    private SwitchCompat swOverviewMode;

    private SwitchCompat swContrast;
    private SwitchCompat swMonospaced;
    private SwitchCompat swMonospacedPre;
    private SwitchCompat swTextColor;
    private SwitchCompat swTextSize;
    private SwitchCompat swTextFont;
    private SwitchCompat swTextAlign;
    private SwitchCompat swTextSeparators;
    private SwitchCompat swCollapseQuotes;
    private SwitchCompat swImagesPlaceholders;
    private SwitchCompat swImagesInline;
    private SwitchCompat swAttachmentsAlt;
    private SwitchCompat swThumbnails;

    private SwitchCompat swParseClasses;
    private SwitchCompat swAuthentication;

    private NumberFormat NF = NumberFormat.getNumberInstance();

    private final static String[] RESET_OPTIONS = new String[]{
            "theme", "startup", "cards", "beige", "date", "navbar_colorize", "portrait2", "landscape", "landscape3",
            "threading", "threading_unread", "indentation", "seekbar", "actionbar", "actionbar_color",
            "highlight_unread", "highlight_color", "color_stripe",
            "avatars", "gravatars", "favicons", "generated_icons", "identicons", "circular", "saturation", "brightness", "threshold",
            "name_email", "prefer_contact", "distinguish_contacts", "show_recipients",
            "subject_top", "font_size_sender", "font_size_subject", "subject_italic", "highlight_subject", "subject_ellipsize",
            "keywords_header", "labels_header", "flags", "flags_background",
            "preview", "preview_italic", "preview_lines",
            "addresses",
            "message_zoom", "overview_mode", "contrast", "monospaced", "monospaced_pre",
            "text_color", "text_size", "text_font", "text_align", "text_separators",
            "collapse_quotes", "image_placeholders", "inline_images", "attachments_alt", "thumbnails",
            "parse_classes", "authentication"
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
        swBeige = view.findViewById(R.id.swBeige);
        swDate = view.findViewById(R.id.swDate);
        swNavBarColorize = view.findViewById(R.id.swNavBarColorize);
        swPortrait2 = view.findViewById(R.id.swPortrait2);
        swLandscape = view.findViewById(R.id.swLandscape);
        swLandscape3 = view.findViewById(R.id.swLandscape3);

        swThreading = view.findViewById(R.id.swThreading);
        swThreadingUnread = view.findViewById(R.id.swThreadingUnread);
        swIndentation = view.findViewById(R.id.swIndentation);
        swSeekbar = view.findViewById(R.id.swSeekbar);
        swActionbar = view.findViewById(R.id.swActionbar);
        swActionbarColor = view.findViewById(R.id.swActionbarColor);

        swHighlightUnread = view.findViewById(R.id.swHighlightUnread);
        btnHighlightColor = view.findViewById(R.id.btnHighlightColor);
        swColorStripe = view.findViewById(R.id.swColorStripe);
        swAvatars = view.findViewById(R.id.swAvatars);
        swGravatars = view.findViewById(R.id.swGravatars);
        tvGravatarsHint = view.findViewById(R.id.tvGravatarsHint);
        swFavicons = view.findViewById(R.id.swFavicons);
        swGeneratedIcons = view.findViewById(R.id.swGeneratedIcons);
        swIdenticons = view.findViewById(R.id.swIdenticons);
        swCircular = view.findViewById(R.id.swCircular);
        ivRed = view.findViewById(R.id.ivRed);
        ivGreen = view.findViewById(R.id.ivGreen);
        ivBlue = view.findViewById(R.id.ivBlue);
        tvSaturation = view.findViewById(R.id.tvSaturation);
        sbSaturation = view.findViewById(R.id.sbSaturation);
        tvBrightness = view.findViewById(R.id.tvBrightness);
        sbBrightness = view.findViewById(R.id.sbBrightness);
        tvThreshold = view.findViewById(R.id.tvThreshold);
        sbThreshold = view.findViewById(R.id.sbThreshold);
        swNameEmail = view.findViewById(R.id.swNameEmail);
        swPreferContact = view.findViewById(R.id.swPreferContact);
        swDistinguishContacts = view.findViewById(R.id.swDistinguishContacts);
        swShowRecipients = view.findViewById(R.id.swShowRecipients);
        swSubjectTop = view.findViewById(R.id.swSubjectTop);
        spFontSizeSender = view.findViewById(R.id.spFontSizeSender);
        spFontSizeSubject = view.findViewById(R.id.spFontSizeSubject);
        swSubjectItalic = view.findViewById(R.id.swSubjectItalic);
        swHighlightSubject = view.findViewById(R.id.swHighlightSubject);
        spSubjectEllipsize = view.findViewById(R.id.spSubjectEllipsize);
        swKeywords = view.findViewById(R.id.swKeywords);
        swLabels = view.findViewById(R.id.swLabels);
        swFlags = view.findViewById(R.id.swFlags);
        swFlagsBackground = view.findViewById(R.id.swFlagsBackground);
        swPreview = view.findViewById(R.id.swPreview);
        swPreviewItalic = view.findViewById(R.id.swPreviewItalic);
        spPreviewLines = view.findViewById(R.id.spPreviewLines);
        swAddresses = view.findViewById(R.id.swAddresses);
        etMessageZoom = view.findViewById(R.id.etMessageZoom);
        swOverviewMode = view.findViewById(R.id.swOverviewMode);
        swContrast = view.findViewById(R.id.swContrast);
        swMonospaced = view.findViewById(R.id.swMonospaced);
        swMonospacedPre = view.findViewById(R.id.swMonospacedPre);
        swTextColor = view.findViewById(R.id.swTextColor);
        swTextSize = view.findViewById(R.id.swTextSize);
        swTextFont = view.findViewById(R.id.swTextFont);
        swTextAlign = view.findViewById(R.id.swTextAlign);
        swTextSeparators = view.findViewById(R.id.swTextSeparators);
        swCollapseQuotes = view.findViewById(R.id.swCollapseQuotes);
        swImagesPlaceholders = view.findViewById(R.id.swImagesPlaceholders);
        swImagesInline = view.findViewById(R.id.swImagesInline);
        swAttachmentsAlt = view.findViewById(R.id.swAttachmentsAlt);
        swThumbnails = view.findViewById(R.id.swThumbnails);
        swParseClasses = view.findViewById(R.id.swParseClasses);
        swAuthentication = view.findViewById(R.id.swAuthentication);

        setOptions();

        // Wire controls

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        btnTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new FragmentDialogTheme().show(getParentFragmentManager(), "setup:theme");
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
                swBeige.setEnabled(checked);
                swIndentation.setEnabled(checked);
            }
        });

        swBeige.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("beige", checked).apply();
            }
        });

        swDate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("date", checked).apply();
            }
        });

        swNavBarColorize.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("navbar_colorize", checked).apply();
                setNavigationBarColor(
                        checked ? Helper.resolveColor(getContext(), R.attr.colorPrimaryVariant) : Color.BLACK);
            }
        });

        swPortrait2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("portrait2", checked).apply();
            }
        });

        swLandscape.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("landscape", checked).apply();
            }
        });

        swLandscape3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("landscape3", checked).apply();
            }
        });

        swThreading.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("threading", checked).apply();
                swThreadingUnread.setEnabled(checked);
                WidgetUnified.updateData(getContext());
            }
        });

        swThreadingUnread.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("threading_unread", checked).apply();
            }
        });

        swIndentation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("indentation", checked).apply();
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
                swActionbarColor.setEnabled(checked);
            }
        });

        swActionbarColor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("actionbar_color", checked).apply();
            }
        });

        swHighlightUnread.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("highlight_unread", checked).apply();
            }
        });

        btnHighlightColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getContext();
                int editTextColor = Helper.resolveColor(context, android.R.attr.editTextColor);
                int highlightColor = prefs.getInt("highlight_color", Helper.resolveColor(context, R.attr.colorSecondary));

                ColorPickerDialogBuilder builder = ColorPickerDialogBuilder
                        .with(context)
                        .setTitle(R.string.title_advanced_highlight_color)
                        .initialColor(highlightColor)
                        .showColorEdit(true)
                        .setColorEditTextColor(editTextColor)
                        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                        .density(6)
                        .lightnessSliderOnly()
                        .setPositiveButton(android.R.string.ok, new ColorPickerClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                prefs.edit().putInt("highlight_color", selectedColor).apply();
                                btnHighlightColor.setColor(selectedColor);
                            }
                        })
                        .setNegativeButton(R.string.title_reset, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                prefs.edit().remove("highlight_color").apply();
                                btnHighlightColor.setColor(Helper.resolveColor(context, R.attr.colorSecondary));
                            }
                        });

                builder.build().show();
            }
        });

        swColorStripe.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("color_stripe", checked).apply();
                WidgetUnified.updateData(getContext());
            }
        });

        swAvatars.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("avatars", checked).apply();
                ContactInfo.clearCache(getContext());
            }
        });

        swGravatars.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("gravatars", checked).apply();
                ContactInfo.clearCache(getContext());
            }
        });

        swFavicons.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("favicons", checked).apply();
                ContactInfo.clearCache(getContext());
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
                ContactInfo.clearCache(getContext());
            }
        });

        swIdenticons.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("identicons", checked).apply();
                ContactInfo.clearCache(getContext());
            }
        });

        swCircular.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("circular", checked).apply();
                updateColor();
                ContactInfo.clearCache(getContext());
            }
        });

        sbSaturation.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                prefs.edit().putInt("saturation", progress).apply();
                updateColor();
                ContactInfo.clearCache(getContext());
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
                ContactInfo.clearCache(getContext());
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
                ContactInfo.clearCache(getContext());
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

        swPreferContact.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("prefer_contact", checked).apply();
            }
        });

        swDistinguishContacts.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("distinguish_contacts", checked).apply();
            }
        });

        swShowRecipients.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("show_recipients", checked).apply();
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

        swHighlightSubject.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("highlight_subject", checked).apply();
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

        swLabels.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("labels_header", checked).apply();
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

        etMessageZoom.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    int zoom = (s.length() > 0 ? Integer.parseInt(s.toString()) : 0);
                    if (zoom == 0)
                        prefs.edit().remove("message_zoom").apply();
                    else
                        prefs.edit().putInt("message_zoom", zoom).apply();
                } catch (NumberFormatException ex) {
                    Log.e(ex);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Do nothing
            }
        });

        swOverviewMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("overview_mode", checked).apply();
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

        swMonospacedPre.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("monospaced_pre", checked).apply();
            }
        });

        swTextColor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("text_color", checked).apply();
            }
        });

        String theme = prefs.getString("theme", "blue_orange_system");
        swTextColor.setEnabled(!"black_and_white".equals(theme));

        swTextSize.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("text_size", checked).apply();
            }
        });

        swTextFont.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("text_font", checked).apply();
            }
        });

        swTextAlign.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("text_align", checked).apply();
            }
        });

        swTextSeparators.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("text_separators", checked).apply();
            }
        });

        swCollapseQuotes.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("collapse_quotes", checked).apply();
            }
        });

        swImagesPlaceholders.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("image_placeholders", checked).apply();
            }
        });

        swImagesInline.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("inline_images", checked).apply();
            }
        });

        swAttachmentsAlt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("attachments_alt", checked).apply();
            }
        });

        swThumbnails.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("thumbnails", checked).apply();
            }
        });

        swParseClasses.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("parse_classes", checked).apply();
            }
        });

        swAuthentication.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("authentication", checked).apply();
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
        if ("message_zoom".equals(key))
            return;

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

        setNavigationBarColor(Color.BLACK);

        ToastEx.makeText(getContext(), R.string.title_setup_done, Toast.LENGTH_LONG).show();
    }

    private void setOptions() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        boolean normal = getResources().getConfiguration()
                .isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_NORMAL);

        String startup = prefs.getString("startup", "unified");
        String[] startupValues = getResources().getStringArray(R.array.startupValues);
        for (int pos = 0; pos < startupValues.length; pos++)
            if (startupValues[pos].equals(startup)) {
                spStartup.setSelection(pos);
                break;
            }

        swCards.setChecked(prefs.getBoolean("cards", true));
        swBeige.setChecked(prefs.getBoolean("beige", true));
        swBeige.setEnabled(swCards.isChecked());
        swDate.setChecked(prefs.getBoolean("date", true));
        swNavBarColorize.setChecked(prefs.getBoolean("navbar_colorize", false));
        swPortrait2.setChecked(prefs.getBoolean("portrait2", false));
        swLandscape.setChecked(prefs.getBoolean("landscape", true));
        swLandscape.setEnabled(normal);
        swLandscape3.setChecked(prefs.getBoolean("landscape3", false));
        swLandscape3.setEnabled(normal);

        swThreading.setChecked(prefs.getBoolean("threading", true));
        swThreadingUnread.setChecked(prefs.getBoolean("threading_unread", false));
        swThreadingUnread.setEnabled(swThreading.isChecked());
        swIndentation.setChecked(prefs.getBoolean("indentation", false));
        swIndentation.setEnabled(swCards.isChecked());
        swSeekbar.setChecked(prefs.getBoolean("seekbar", false));
        swActionbar.setChecked(prefs.getBoolean("actionbar", true));
        swActionbarColor.setChecked(prefs.getBoolean("actionbar_color", false));
        swActionbarColor.setEnabled(swActionbar.isChecked());

        swHighlightUnread.setChecked(prefs.getBoolean("highlight_unread", true));

        btnHighlightColor.setColor(prefs.getInt("highlight_color",
                Helper.resolveColor(getContext(), R.attr.colorUnreadHighlight)));

        swColorStripe.setChecked(prefs.getBoolean("color_stripe", true));
        swAvatars.setChecked(prefs.getBoolean("avatars", true));
        swGravatars.setChecked(prefs.getBoolean("gravatars", false));
        swFavicons.setChecked(prefs.getBoolean("favicons", false));
        swGeneratedIcons.setChecked(prefs.getBoolean("generated_icons", true));
        swIdenticons.setChecked(prefs.getBoolean("identicons", false));
        swIdenticons.setEnabled(swGeneratedIcons.isChecked());
        swCircular.setChecked(prefs.getBoolean("circular", true));

        int saturation = prefs.getInt("saturation", 100);
        tvSaturation.setText(getString(R.string.title_advanced_color_saturation, NF.format(saturation)));
        sbSaturation.setProgress(saturation);
        sbSaturation.setEnabled(swGeneratedIcons.isChecked());

        int brightness = prefs.getInt("brightness", 100);
        tvBrightness.setText(getString(R.string.title_advanced_color_value, NF.format(brightness)));
        sbBrightness.setProgress(brightness);
        sbBrightness.setEnabled(swGeneratedIcons.isChecked());

        int threshold = prefs.getInt("threshold", 50);
        tvThreshold.setText(getString(R.string.title_advanced_color_threshold, NF.format(threshold)));
        sbThreshold.setProgress(threshold);
        sbThreshold.setEnabled(swGeneratedIcons.isChecked());

        swNameEmail.setChecked(prefs.getBoolean("name_email", false));
        swPreferContact.setChecked(prefs.getBoolean("prefer_contact", false));
        swDistinguishContacts.setChecked(prefs.getBoolean("distinguish_contacts", false));
        swShowRecipients.setChecked(prefs.getBoolean("show_recipients", false));
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
        swHighlightSubject.setChecked(prefs.getBoolean("highlight_subject", false));

        String subject_ellipsize = prefs.getString("subject_ellipsize", "full");
        String[] ellipsizeValues = getResources().getStringArray(R.array.ellipsizeValues);
        for (int pos = 0; pos < startupValues.length; pos++)
            if (ellipsizeValues[pos].equals(subject_ellipsize)) {
                spSubjectEllipsize.setSelection(pos);
                break;
            }

        swKeywords.setChecked(prefs.getBoolean("keywords_header", false));
        swLabels.setChecked(prefs.getBoolean("labels_header", true));
        swFlags.setChecked(prefs.getBoolean("flags", true));
        swFlagsBackground.setChecked(prefs.getBoolean("flags_background", false));
        swPreview.setChecked(prefs.getBoolean("preview", false));
        swPreviewItalic.setChecked(prefs.getBoolean("preview_italic", true));
        swPreviewItalic.setEnabled(swPreview.isChecked());
        spPreviewLines.setSelection(prefs.getInt("preview_lines", 2) - 1);
        spPreviewLines.setEnabled(swPreview.isChecked());

        swAddresses.setChecked(prefs.getBoolean("addresses", false));

        int message_zoom = prefs.getInt("message_zoom", 0);
        etMessageZoom.setText(message_zoom == 0 ? null : Integer.toString(message_zoom));
        swOverviewMode.setChecked(prefs.getBoolean("overview_mode", false));

        swContrast.setChecked(prefs.getBoolean("contrast", false));
        swMonospaced.setChecked(prefs.getBoolean("monospaced", false));
        swMonospacedPre.setChecked(prefs.getBoolean("monospaced_pre", false));
        swTextColor.setChecked(prefs.getBoolean("text_color", true));
        swTextSize.setChecked(prefs.getBoolean("text_size", true));
        swTextFont.setChecked(prefs.getBoolean("text_font", true));
        swTextAlign.setChecked(prefs.getBoolean("text_align", true));
        swTextSeparators.setChecked(prefs.getBoolean("text_separators", true));
        swCollapseQuotes.setChecked(prefs.getBoolean("collapse_quotes", false));
        swImagesPlaceholders.setChecked(prefs.getBoolean("image_placeholders", true));
        swImagesInline.setChecked(prefs.getBoolean("inline_images", false));
        swAttachmentsAlt.setChecked(prefs.getBoolean("attachments_alt", false));
        swThumbnails.setChecked(prefs.getBoolean("thumbnails", true));

        swParseClasses.setChecked(prefs.getBoolean("parse_classes", true));
        swAuthentication.setChecked(prefs.getBoolean("authentication", true));

        updateColor();
    }

    private void setNavigationBarColor(int color) {
        FragmentActivity activity = getActivity();
        if (activity == null)
            return;
        Window window = activity.getWindow();
        if (window == null)
            return;
        window.setNavigationBarColor(color);
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
}
