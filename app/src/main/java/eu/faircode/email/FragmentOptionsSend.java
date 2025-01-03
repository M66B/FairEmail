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

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SwitchCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class FragmentOptionsSend extends FragmentBase implements SharedPreferences.OnSharedPreferenceChangeListener {
    private View view;
    private ImageButton ibHelp;
    private SwitchCompat swKeyboard;
    private SwitchCompat swKeyboardNoFullscreen;
    private SwitchCompat swSuggestNames;
    private SwitchCompat swSuggestSent;
    private SwitchCompat swSuggestReceived;
    private SwitchCompat swSuggestFrequently;
    private SwitchCompat swSuggestAccount;
    private SwitchCompat swAutoIdentity;
    private EditText etAutoDeleteAge;
    private EditText etAutoDeleteFreq;
    private Button btnLocalContacts;
    private SwitchCompat swSendChips;
    private SwitchCompat swNavColor;
    private SwitchCompat swSendReminders;
    private SwitchCompat swSendPending;
    private SwitchCompat swSaveRevisions;
    private SwitchCompat swAutoSaveParagraph;
    private SwitchCompat swAutoSaveDot;
    private SwitchCompat swDiscardDelete;
    private Spinner spSendDelayed;
    private SwitchCompat swSendUndo;
    private Spinner spAnswerActionSingle;
    private Spinner spAnswerActionLong;
    private Button btnSound;

    private ViewButtonColor btnComposeColor;
    private Spinner spComposeFont;
    private Spinner spComposeTextSize;
    private SwitchCompat swComposeMonospaced;
    private SwitchCompat swPrefixOnce;
    private SwitchCompat swPrefixCount;
    private RadioGroup rgRe;
    private RadioGroup rgFwd;
    private SwitchCompat swSeparateReply;
    private SwitchCompat swExtendedReply;
    private EditText etTemplateReply;
    private TextView tvTemplateReplyHint;
    private SwitchCompat swWriteBelow;
    private SwitchCompat swQuoteReply;
    private SwitchCompat swQuoteLimit;
    private SwitchCompat swResizeReply;
    private SwitchCompat swResizePaste;
    private Spinner spSignatureLocation;
    private SwitchCompat swSignatureNew;
    private SwitchCompat swSignatureReply;
    private SwitchCompat swSignatureReplyOnce;
    private SwitchCompat swSignatureForward;
    private Button btnEditSignature;

    private SwitchCompat swSendAtTop;
    private SwitchCompat swAttachNew;
    private SwitchCompat swAutoLink;
    private SwitchCompat swPlainOnly;
    private SwitchCompat swPlainOnlyReply;
    private SwitchCompat swFormatFlowed;
    private SwitchCompat swUsenetSignature;
    private SwitchCompat swRemoveSignatures;
    private SwitchCompat swReceipt;
    private Spinner spReceiptType;
    private SwitchCompat swReceiptLegacy;
    private SwitchCompat swForwardNew;
    private SwitchCompat swLookupMx;
    private SwitchCompat swReplyMove;
    private SwitchCompat swReplyMoveInbox;
    private EditText etSendRetryMax;
    private SwitchCompat swSendPartial;

    final static List<String> RESET_OPTIONS = Collections.unmodifiableList(Arrays.asList(
            "keyboard", "keyboard_no_fullscreen",
            "suggest_names", "suggest_sent", "suggested_received", "suggest_frequently", "suggest_account", "auto_identity",
            "purge_contact_age", "purge_contact_freq",
            "send_reminders", "send_chips", "send_nav_color", "send_pending",
            "save_revisions", "auto_save_paragraph", "auto_save_dot", "discard_delete",
            "send_delayed", "send_undo",
            "answer_single", "answer_action",
            "sound_sent",
            "compose_color", "compose_font", "compose_text_size", "compose_monospaced",
            "prefix_once", "prefix_count", "alt_re", "alt_fwd",
            "separate_reply", "extended_reply", "template_reply", "write_below", "quote_reply", "quote_limit",
            "resize_reply", "resize_paste",
            "signature_location", "signature_new", "signature_reply", "signature_reply_once", "signature_forward",
            "send_at_top", "attach_new", "auto_link", "plain_only", "plain_only_reply",
            "format_flowed", "usenet_signature", "remove_signatures",
            "receipt_default", "receipt_type", "receipt_legacy",
            "forward_new",
            "lookup_mx", "reply_move", "reply_move_inbox",
            "send_retry_max", "send_partial"
    ));

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_setup);
        setHasOptionsMenu(true);

        view = inflater.inflate(R.layout.fragment_options_send, container, false);

        // Get controls

        ibHelp = view.findViewById(R.id.ibHelp);
        swKeyboard = view.findViewById(R.id.swKeyboard);
        swKeyboardNoFullscreen = view.findViewById(R.id.swKeyboardNoFullscreen);
        swSuggestNames = view.findViewById(R.id.swSuggestNames);
        swSuggestSent = view.findViewById(R.id.swSuggestSent);
        swSuggestReceived = view.findViewById(R.id.swSuggestReceived);
        swSuggestFrequently = view.findViewById(R.id.swSuggestFrequently);
        swSuggestAccount = view.findViewById(R.id.swSuggestAccount);
        swAutoIdentity = view.findViewById(R.id.swAutoIdentity);
        etAutoDeleteAge = view.findViewById(R.id.etAutoDeleteAge);
        etAutoDeleteFreq = view.findViewById(R.id.etAutoDeleteFreq);
        btnLocalContacts = view.findViewById(R.id.btnLocalContacts);
        swSendChips = view.findViewById(R.id.swSendChips);
        swNavColor = view.findViewById(R.id.swNavColor);
        swSendReminders = view.findViewById(R.id.swSendReminders);
        swSendPending = view.findViewById(R.id.swSendPending);
        swSaveRevisions = view.findViewById(R.id.swSaveRevisions);
        swAutoSaveParagraph = view.findViewById(R.id.swAutoSaveParagraph);
        swAutoSaveDot = view.findViewById(R.id.swAutoSaveDot);
        swDiscardDelete = view.findViewById(R.id.swDiscardDelete);
        spSendDelayed = view.findViewById(R.id.spSendDelayed);
        swSendUndo = view.findViewById(R.id.swSendUndo);
        spAnswerActionSingle = view.findViewById(R.id.spAnswerActionSingle);
        spAnswerActionLong = view.findViewById(R.id.spAnswerActionLong);
        btnSound = view.findViewById(R.id.btnSound);

        btnComposeColor = view.findViewById(R.id.btnComposeColor);
        spComposeFont = view.findViewById(R.id.spComposeFont);
        spComposeTextSize = view.findViewById(R.id.spComposeTextSize);
        swComposeMonospaced = view.findViewById(R.id.swComposeMonospaced);
        swPrefixOnce = view.findViewById(R.id.swPrefixOnce);
        swPrefixCount = view.findViewById(R.id.swPrefixCount);
        rgRe = view.findViewById(R.id.rgRe);
        rgFwd = view.findViewById(R.id.rgFwd);
        swSeparateReply = view.findViewById(R.id.swSeparateReply);
        swExtendedReply = view.findViewById(R.id.swExtendedReply);
        etTemplateReply = view.findViewById(R.id.etTemplateReply);
        tvTemplateReplyHint = view.findViewById(R.id.tvTemplateReplyHint);
        swWriteBelow = view.findViewById(R.id.swWriteBelow);
        swQuoteReply = view.findViewById(R.id.swQuoteReply);
        swQuoteLimit = view.findViewById(R.id.swQuoteLimit);
        swResizeReply = view.findViewById(R.id.swResizeReply);
        swResizePaste = view.findViewById(R.id.swResizePaste);
        spSignatureLocation = view.findViewById(R.id.spSignatureLocation);
        swSignatureNew = view.findViewById(R.id.swSignatureNew);
        swSignatureReply = view.findViewById(R.id.swSignatureReply);
        swSignatureReplyOnce = view.findViewById(R.id.swSignatureReplyOnce);
        swSignatureForward = view.findViewById(R.id.swSignatureForward);
        btnEditSignature = view.findViewById(R.id.btnEditSignature);

        swSendAtTop = view.findViewById(R.id.swSendAtTop);
        swAttachNew = view.findViewById(R.id.swAttachNew);
        swAutoLink = view.findViewById(R.id.swAutoLink);
        swPlainOnly = view.findViewById(R.id.swPlainOnly);
        swPlainOnlyReply = view.findViewById(R.id.swPlainOnlyReply);
        swFormatFlowed = view.findViewById(R.id.swFormatFlowed);
        swUsenetSignature = view.findViewById(R.id.swUsenetSignature);
        swRemoveSignatures = view.findViewById(R.id.swRemoveSignatures);
        swReceipt = view.findViewById(R.id.swReceipt);
        spReceiptType = view.findViewById(R.id.spReceiptType);
        swReceiptLegacy = view.findViewById(R.id.swReceiptLegacy);
        swForwardNew = view.findViewById(R.id.swForwardNew);
        swLookupMx = view.findViewById(R.id.swLookupMx);
        swReplyMove = view.findViewById(R.id.swReplyMove);
        swReplyMoveInbox = view.findViewById(R.id.swReplyMoveInbox);
        etSendRetryMax = view.findViewById(R.id.etSendRetryMax);
        swSendPartial = view.findViewById(R.id.swSendPartial);

        List<StyleHelper.FontDescriptor> fonts = StyleHelper.getFonts(getContext(), false);

        List<CharSequence> fn = new ArrayList<>();
        fn.add(getString(R.string.title_style_font_default));
        for (int i = 0; i < fonts.size(); i++) {
            StyleHelper.FontDescriptor font = fonts.get(i);
            SpannableStringBuilder ssb = new SpannableStringBuilderEx(font.toString());
            ssb.setSpan(StyleHelper.getTypefaceSpan(font.type, getContext()),
                    0, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            fn.add(ssb);
        }

        ArrayAdapter<CharSequence> fontAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, fn);
        fontAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spComposeFont.setAdapter(fontAdapter);

        int[] textSizeTitles = new int[]{
                R.string.title_style_size_xsmall,
                R.string.title_style_size_small,
                R.string.title_style_size_medium,
                R.string.title_style_size_large,
                R.string.title_style_size_xlarge};

        List<CharSequence> ts = new ArrayList<>();
        ts.add(getString(R.string.title_style_font_default));
        for (int i = 0; i < textSizeTitles.length; i++) {
            SpannableStringBuilder ssb = new SpannableStringBuilderEx(getString(textSizeTitles[i]));
            Float size = HtmlHelper.getFontSize(HtmlHelper.fontSizeNames[i], 1.0f);
            if (size == null)
                size = 1.0f;
            ssb.setSpan(new RelativeSizeSpan(size), 0, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ts.add(ssb);
        }

        ArrayAdapter<CharSequence> sizeAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, ts);
        sizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spComposeTextSize.setAdapter(sizeAdapter);

        setOptions();

        // Wire controls

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        ibHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.view(v.getContext(), Helper.getSupportUri(v.getContext(), "Options:send"), false);
            }
        });

        swKeyboard.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("keyboard", checked).apply();
            }
        });

        swKeyboardNoFullscreen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("keyboard_no_fullscreen", checked).apply();
            }
        });

        swSuggestNames.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("suggest_names", checked).apply();
            }
        });

        swSuggestSent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("suggest_sent", checked).apply();
                swSuggestFrequently.setEnabled(swSuggestSent.isChecked() || swSuggestReceived.isChecked());
                swSuggestAccount.setEnabled(swSuggestSent.isChecked() || swSuggestReceived.isChecked());
                swAutoIdentity.setEnabled(swSuggestSent.isChecked() || swSuggestReceived.isChecked());
            }
        });

        swSuggestReceived.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("suggest_received", checked).apply();
                swSuggestFrequently.setEnabled(swSuggestSent.isChecked() || swSuggestReceived.isChecked());
                swSuggestAccount.setEnabled(swSuggestSent.isChecked() || swSuggestReceived.isChecked());
                swAutoIdentity.setEnabled(swSuggestSent.isChecked() || swSuggestReceived.isChecked());
            }
        });

        swSuggestFrequently.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("suggest_frequently", checked).apply();
            }
        });

        swSuggestAccount.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("suggest_account", checked).apply();
            }
        });

        swAutoIdentity.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("auto_identity", checked).apply();
                swPrefixCount.setEnabled(checked);
            }
        });

        etAutoDeleteAge.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                Integer months = Helper.parseInt(s.toString());
                if (months == null)
                    prefs.edit().remove("purge_contact_age").apply();
                else
                    prefs.edit().putInt("purge_contact_age", months).apply();
            }
        });

        etAutoDeleteFreq.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                Integer months = Helper.parseInt(s.toString());
                if (months == null)
                    prefs.edit().remove("purge_contact_freq").apply();
                else
                    prefs.edit().putInt("purge_contact_freq", months).apply();
            }
        });

        btnLocalContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
                lbm.sendBroadcast(new Intent(ActivitySetup.ACTION_MANAGE_LOCAL_CONTACTS));
            }
        });

        swSendChips.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("send_chips", checked).apply();
            }
        });

        swNavColor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("send_nav_color", checked).apply();
            }
        });

        swSendReminders.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("send_reminders", checked).apply();
            }
        });

        swSendPending.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("send_pending", checked).apply();
            }
        });

        swSaveRevisions.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("save_revisions", checked).apply();
            }
        });

        swAutoSaveParagraph.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("auto_save_paragraph", checked).apply();
            }
        });

        swAutoSaveDot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("auto_save_dot", checked).apply();
            }
        });

        swDiscardDelete.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("discard_delete", checked).apply();
            }
        });

        spSendDelayed.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                int[] values = getResources().getIntArray(R.array.sendDelayedValues);
                int send_delayed = values[position];
                prefs.edit().putInt("send_delayed", send_delayed).apply();
                swSendUndo.setEnabled(send_delayed > 0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                prefs.edit().remove("send_delayed").apply();
            }
        });

        swSendUndo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("send_undo", checked).apply();
            }
        });

        spAnswerActionSingle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String[] values = getResources().getStringArray(R.array.answerValues);
                prefs.edit().putString("answer_single", values[position]).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                prefs.edit().remove("answer_single").apply();
            }
        });

        spAnswerActionLong.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String[] values = getResources().getStringArray(R.array.answerValues);
                prefs.edit().putString("answer_action", values[position]).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                prefs.edit().remove("answer_action").apply();
            }
        });

        btnSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(getContext(), getViewLifecycleOwner(), v);

                popupMenu.getMenu().add(Menu.NONE, R.string.title_rule_select_sound_ringtone, 1, R.string.title_rule_select_sound_ringtone);
                popupMenu.getMenu().add(Menu.NONE, R.string.title_rule_select_sound_audio, 2, R.string.title_rule_select_sound_audio);

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int itemId = item.getItemId();
                        if (itemId == R.string.title_rule_select_sound_ringtone) {
                            String sound = prefs.getString("sound_sent", null);
                            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
                            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.title_advanced_sound));
                            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
                            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, sound == null ? null : Uri.parse(sound));
                            startActivityForResult(Helper.getChooser(getContext(), intent), ActivitySetup.REQUEST_RINGTONE_OUTBOUND);
                            return true;
                        } else if (itemId == R.string.title_rule_select_sound_audio) {
                            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                            intent.addCategory(Intent.CATEGORY_OPENABLE);
                            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            intent.setType("audio/*");
                            Helper.openAdvanced(getContext(), intent);
                            startActivityForResult(Helper.getChooser(getContext(), intent), ActivitySetup.REQUEST_AUDIO_OUTBOUND);
                            return true;
                        }
                        return false;
                    }
                });

                popupMenu.show();
            }
        });

        btnComposeColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getContext();
                int editTextColor = Helper.resolveColor(context, android.R.attr.editTextColor);
                int compose_color = prefs.getInt("compose_color", Color.TRANSPARENT);

                ColorPickerDialogBuilder builder = ColorPickerDialogBuilder
                        .with(context)
                        .setTitle(R.string.title_advanced_compose_color)
                        .initialColor(compose_color == Color.TRANSPARENT ? Color.GRAY : compose_color)
                        .showColorEdit(true)
                        .setColorEditTextColor(editTextColor)
                        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                        .density(6)
                        .lightnessSliderOnly()
                        .setPositiveButton(android.R.string.ok, new ColorPickerClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                prefs.edit().putInt("compose_color", selectedColor).apply();
                                btnComposeColor.setColor(selectedColor);
                            }
                        })
                        .setNegativeButton(R.string.title_reset, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                prefs.edit().remove("compose_color").apply();
                                btnComposeColor.setColor(Color.TRANSPARENT);
                            }
                        });

                builder.build().show();
            }
        });

        spComposeFont.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                if (position == 0)
                    prefs.edit().remove("compose_font").apply();
                else
                    prefs.edit().putString("compose_font", fonts.get(position - 1).type).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                prefs.edit().remove("compose_font").apply();
            }
        });

        spComposeTextSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                if (position == 0)
                    prefs.edit().remove("compose_text_size").apply();
                else
                    prefs.edit().putString("compose_text_size", HtmlHelper.fontSizeNames[position - 1]).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                prefs.edit().remove("compose_text_size").apply();
            }
        });

        swComposeMonospaced.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("compose_monospaced", checked).apply();
                swPrefixCount.setEnabled(checked);
            }
        });

        swPrefixOnce.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("prefix_once", checked).apply();
                swPrefixCount.setEnabled(checked);
            }
        });

        swPrefixCount.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("prefix_count", checked).apply();
            }
        });

        rgRe.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                prefs.edit().putBoolean("alt_re", checkedId == R.id.rbRe2).apply();
            }
        });

        rgFwd.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                prefs.edit().putBoolean("alt_fwd", checkedId == R.id.rbFwd2).apply();
            }
        });

        swSeparateReply.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("separate_reply", checked).apply();
            }
        });

        swExtendedReply.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("extended_reply", checked).apply();
                etTemplateReply.setEnabled(!checked);
            }
        });

        etTemplateReply.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                String template = s.toString();
                if (TextUtils.isEmpty(template.trim()))
                    prefs.edit().remove("template_reply").apply();
                else
                    prefs.edit().putString("template_reply", template).apply();
            }
        });

        tvTemplateReplyHint.setText(getString(R.string.title_advanced_template_reply_hint, "$from$ $to$ $cc$ $time$ $subject$"));

        swWriteBelow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("write_below", checked).apply();
            }
        });

        swQuoteReply.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("quote_reply", checked).apply();
            }
        });

        swQuoteLimit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("quote_limit", checked).apply();
            }
        });

        swResizeReply.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("resize_reply", checked).apply();
            }
        });

        swResizePaste.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("resize_paste", checked).apply();
            }
        });

        spSignatureLocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                prefs.edit().putInt("signature_location", position).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                prefs.edit().remove("signature_location").apply();
            }
        });

        swSignatureNew.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("signature_new", checked).apply();
            }
        });

        swSignatureReply.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("signature_reply", checked).apply();
                swSignatureReplyOnce.setEnabled(checked);
            }
        });

        swSignatureReplyOnce.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("signature_reply_once", checked).apply();
            }
        });

        swSignatureForward.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("signature_forward", checked).apply();
            }
        });

        btnEditSignature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(v.getContext());
                lbm.sendBroadcast(new Intent(ActivitySetup.ACTION_VIEW_IDENTITIES));
            }
        });

        swSendAtTop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("send_at_top", checked).apply();
            }
        });

        swAttachNew.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("attach_new", checked).apply();
            }
        });

        swAutoLink.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("auto_link", checked).apply();
            }
        });

        swPlainOnly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("plain_only", checked).apply();
            }
        });

        swPlainOnlyReply.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("plain_only_reply", checked).apply();
            }
        });

        swFormatFlowed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("format_flowed", checked).apply();
            }
        });

        swUsenetSignature.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("usenet_signature", checked).apply();
                if (checked)
                    prefs.edit().putInt("signature_location", 2).apply();
            }
        });

        swRemoveSignatures.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("remove_signatures", checked).apply();
            }
        });

        swReceipt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("receipt_default", checked).apply();
            }
        });

        spReceiptType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                prefs.edit().putInt("receipt_type", position).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                prefs.edit().remove("receipt_type").apply();
            }
        });

        swReceiptLegacy.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("receipt_legacy", checked).apply();
            }
        });

        swForwardNew.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("forward_new", checked).apply();
            }
        });

        swLookupMx.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("lookup_mx", checked).apply();
            }
        });

        swReplyMove.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("reply_move", checked).apply();
                swReplyMoveInbox.setEnabled(checked);
            }
        });

        swReplyMoveInbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("reply_move_inbox", checked).apply();
            }
        });

        etSendRetryMax.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                Integer count = Helper.parseInt(s.toString());
                if (count == null)
                    prefs.edit().remove("send_retry_max").apply();
                else
                    prefs.edit().putInt("send_retry_max", count).apply();
            }
        });

        swSendPartial.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("send_partial", checked).apply();
            }
        });

        // Initialize
        String re1 = getString(R.string.title_subject_reply, "");
        String re2 = getString(R.string.title_subject_reply_alt, "");
        ((RadioButton) view.findViewById(R.id.rbRe1)).setText(re1);
        ((RadioButton) view.findViewById(R.id.rbRe2)).setText(re2);
        boolean re = !Objects.equals(re1, re2);
        for (int i = 0; i < rgRe.getChildCount(); i++)
            rgRe.getChildAt(i).setEnabled(re);

        String fwd1 = getString(R.string.title_subject_forward, "");
        String fwd2 = getString(R.string.title_subject_forward_alt, "");
        ((RadioButton) view.findViewById(R.id.rbFwd1)).setText(fwd1);
        ((RadioButton) view.findViewById(R.id.rbFwd2)).setText(fwd2);
        boolean fwd = !Objects.equals(fwd1, fwd2);
        for (int i = 0; i < rgFwd.getChildCount(); i++)
            rgFwd.getChildAt(i).setEnabled(fwd);

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
        if (!RESET_OPTIONS.contains(key))
            return;
        if ("template_reply".equals(key))
            return;
        if ("purge_contact_age".equals(key) || "purge_contact_freq".equals(key))
            return;
        if ("send_retry_max".equals(key))
            return;

        getMainHandler().removeCallbacks(update);
        getMainHandler().postDelayed(update, FragmentOptions.DELAY_SETOPTIONS);
    }

    private Runnable update = new RunnableEx("send") {
        @Override
        protected void delegate() {
            setOptions();
        }
    };

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_options, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_default) {
            FragmentOptions.reset(getContext(), RESET_OPTIONS, null);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setOptions() {
        try {
            if (view == null || getContext() == null)
                return;

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

            int purge_contact_age = prefs.getInt("purge_contact_age", 1);
            int purge_contact_freq = prefs.getInt("purge_contact_freq", 0);

            swKeyboard.setChecked(prefs.getBoolean("keyboard", true));
            swKeyboardNoFullscreen.setChecked(prefs.getBoolean("keyboard_no_fullscreen", false));
            swSuggestNames.setChecked(prefs.getBoolean("suggest_names", true));
            swSuggestSent.setChecked(prefs.getBoolean("suggest_sent", true));
            swSuggestReceived.setChecked(prefs.getBoolean("suggest_received", false));
            swSuggestFrequently.setChecked(prefs.getBoolean("suggest_frequently", false));
            swSuggestFrequently.setEnabled(swSuggestSent.isChecked() || swSuggestReceived.isChecked());
            swSuggestAccount.setChecked(prefs.getBoolean("suggest_account", false));
            swSuggestAccount.setEnabled(swSuggestSent.isChecked() || swSuggestReceived.isChecked());
            swAutoIdentity.setChecked(prefs.getBoolean("auto_identity", false));
            swAutoIdentity.setEnabled(swSuggestSent.isChecked() || swSuggestReceived.isChecked());
            etAutoDeleteAge.setText(purge_contact_age > 0 ? Integer.toString(purge_contact_age) : null);
            etAutoDeleteFreq.setText(purge_contact_freq > 0 ? Integer.toString(purge_contact_freq) : null);
            swSendChips.setChecked(prefs.getBoolean("send_chips", true));
            swNavColor.setChecked(prefs.getBoolean("send_nav_color", false));
            swSendReminders.setChecked(prefs.getBoolean("send_reminders", true));
            swSendPending.setChecked(prefs.getBoolean("send_pending", true));
            swSaveRevisions.setChecked(prefs.getBoolean("save_revisions", true));
            swAutoSaveParagraph.setChecked(prefs.getBoolean("auto_save_paragraph", true));
            swAutoSaveDot.setChecked(prefs.getBoolean("auto_save_dot", false));
            swDiscardDelete.setChecked(prefs.getBoolean("discard_delete", true));

            int send_delayed = prefs.getInt("send_delayed", 0);
            int[] sendDelayedValues = getResources().getIntArray(R.array.sendDelayedValues);
            for (int pos = 0; pos < sendDelayedValues.length; pos++)
                if (sendDelayedValues[pos] == send_delayed) {
                    spSendDelayed.setSelection(pos);
                    break;
                }

            swSendUndo.setChecked(prefs.getBoolean("send_undo", false));
            swSendUndo.setEnabled(send_delayed > 0);

            String[] answerValues = getResources().getStringArray(R.array.answerValues);

            String answer_default = prefs.getString("answer_single", "menu");
            for (int pos = 0; pos < answerValues.length; pos++)
                if (answerValues[pos].equals(answer_default)) {
                    spAnswerActionSingle.setSelection(pos);
                    break;
                }

            boolean reply_all = prefs.getBoolean("reply_all", false);
            String answer_action = prefs.getString("answer_action", reply_all ? "reply_all" : "reply");
            for (int pos = 0; pos < answerValues.length; pos++)
                if (answerValues[pos].equals(answer_action)) {
                    spAnswerActionLong.setSelection(pos);
                    break;
                }

            btnComposeColor.setColor(prefs.getInt("compose_color", Color.TRANSPARENT));

            String compose_font = prefs.getString("compose_font", "");
            List<StyleHelper.FontDescriptor> fonts = StyleHelper.getFonts(getContext(), false);
            for (int pos = 0; pos < fonts.size(); pos++) {
                StyleHelper.FontDescriptor font = fonts.get(pos);
                if (font.type.equals(compose_font)) {
                    spComposeFont.setSelection(pos + 1);
                    break;
                }
            }

            String compose_text_size = prefs.getString("compose_text_size", "");
            for (int pos = 0; pos < HtmlHelper.fontSizeNames.length; pos++)
                if (HtmlHelper.fontSizeNames[pos].equals(compose_text_size)) {
                    spComposeTextSize.setSelection(pos + 1);
                    break;
                }

            swComposeMonospaced.setChecked(prefs.getBoolean("compose_monospaced", false));

            swPrefixOnce.setChecked(prefs.getBoolean("prefix_once", true));
            swPrefixCount.setChecked(prefs.getBoolean("prefix_count", false));
            swPrefixCount.setEnabled(swPrefixOnce.isChecked());
            rgRe.check(prefs.getBoolean("alt_re", false) ? R.id.rbRe2 : R.id.rbRe1);
            rgFwd.check(prefs.getBoolean("alt_fwd", false) ? R.id.rbFwd2 : R.id.rbFwd1);

            swSeparateReply.setChecked(prefs.getBoolean("separate_reply", false));
            swExtendedReply.setChecked(prefs.getBoolean("extended_reply", false));
            etTemplateReply.setText(prefs.getString("template_reply", null));
            etTemplateReply.setEnabled(!swExtendedReply.isChecked());
            swWriteBelow.setChecked(prefs.getBoolean("write_below", false));
            swQuoteReply.setChecked(prefs.getBoolean("quote_reply", true));
            swQuoteLimit.setChecked(prefs.getBoolean("quote_limit", true));
            swResizeReply.setChecked(prefs.getBoolean("resize_reply", true));
            swResizePaste.setChecked(prefs.getBoolean("resize_paste", true));

            int signature_location = prefs.getInt("signature_location", 1);
            spSignatureLocation.setSelection(signature_location);

            swSignatureNew.setChecked(prefs.getBoolean("signature_new", true));
            swSignatureReply.setChecked(prefs.getBoolean("signature_reply", true));
            swSignatureReplyOnce.setChecked(prefs.getBoolean("signature_reply_once", false));
            swSignatureReplyOnce.setEnabled(swSignatureReply.isChecked());
            swSignatureForward.setChecked(prefs.getBoolean("signature_forward", true));

            swSendAtTop.setChecked(prefs.getBoolean("send_at_top", false));
            swAttachNew.setChecked(prefs.getBoolean("attach_new", true));
            swAutoLink.setChecked(prefs.getBoolean("auto_link", false));
            swPlainOnly.setChecked(prefs.getBoolean("plain_only", false));
            swPlainOnlyReply.setChecked(prefs.getBoolean("plain_only_reply", false));
            swFormatFlowed.setChecked(prefs.getBoolean("format_flowed", false));
            swUsenetSignature.setChecked(prefs.getBoolean("usenet_signature", false));
            swRemoveSignatures.setChecked(prefs.getBoolean("remove_signatures", false));
            swReceipt.setChecked(prefs.getBoolean("receipt_default", false));

            int receipt_type = prefs.getInt("receipt_type", 2);
            spReceiptType.setSelection(receipt_type);

            swReceiptLegacy.setChecked(prefs.getBoolean("receipt_legacy", false));

            swForwardNew.setChecked(prefs.getBoolean("forward_new", false));
            swLookupMx.setChecked(prefs.getBoolean("lookup_mx", false));
            swReplyMove.setChecked(prefs.getBoolean("reply_move", false));
            swReplyMoveInbox.setChecked(prefs.getBoolean("reply_move_inbox", true));
            swReplyMoveInbox.setEnabled(swReplyMove.isChecked());

            int send_retry_max = prefs.getInt("send_retry_max", 0);
            etSendRetryMax.setText(send_retry_max > 0 ? Integer.toString(send_retry_max) : null);
            etSendRetryMax.setHint(Integer.toString(ServiceSend.RETRY_MAX_DEFAULT));

            swSendPartial.setChecked(prefs.getBoolean("send_partial", false));
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            switch (requestCode) {
                case ActivitySetup.REQUEST_RINGTONE_OUTBOUND:
                    if (resultCode == RESULT_OK && data != null)
                        onSelectSound(data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI));
                    break;
                case ActivitySetup.REQUEST_AUDIO_OUTBOUND:
                    if (resultCode == RESULT_OK && data != null)
                        onSelectSound(data.getData());
                    break;
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    private void onSelectSound(Uri uri) {
        Log.i("Selected ringtone=" + uri);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        if (uri == null) // no/silent sound
            prefs.edit().remove("sound_sent").apply();
        else {
            if ("content".equals(uri.getScheme())) {
                try {
                    getContext().getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    if (!Helper.isPersisted(getContext(), uri, true, false))
                        Log.unexpectedError(getParentFragmentManager(),
                                new IllegalStateException("No permission granted to access selected sound " + uri));
                } catch (Throwable ex) {
                    Log.w(ex);
                }
                prefs.edit().putString("sound_sent", uri.toString()).apply();
            } else
                prefs.edit().remove("sound_sent").apply();
        }
    }
}
