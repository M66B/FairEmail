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

import android.content.Intent;
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
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.lifecycle.Lifecycle;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

public class FragmentOptionsSend extends FragmentBase implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SwitchCompat swKeyboard;
    private SwitchCompat swSuggestSent;
    private SwitchCompat swSuggestReceived;
    private SwitchCompat swSuggestFrequently;
    private Button btnLocalContacts;
    private SwitchCompat swSendReminders;
    private Spinner spSendDelayed;

    private Spinner spComposeFont;
    private SwitchCompat swAutoList;
    private SwitchCompat swPrefixOnce;
    private SwitchCompat swExtendedReply;
    private SwitchCompat swQuoteReply;
    private SwitchCompat swResizeReply;
    private Spinner spSignatureLocation;
    private SwitchCompat swSignatureReply;
    private SwitchCompat swSignatureForward;
    private SwitchCompat swDiscardDelete;

    private SwitchCompat swPlainOnly;
    private SwitchCompat swFormatFlowed;
    private SwitchCompat swUsenetSignature;
    private SwitchCompat swRemoveSignatures;
    private SwitchCompat swReceipt;
    private Spinner spReceiptType;
    private SwitchCompat swLookupMx;

    private final static String[] RESET_OPTIONS = new String[]{
            "keyboard", "suggest_sent", "suggested_received", "suggest_frequently",
            "send_reminders", "send_delayed",
            "compose_font", "autolist", "prefix_once", "extended_reply", "quote_reply", "resize_reply",
            "signature_location", "signature_reply", "signature_forward",
            "discard_delete",
            "plain_only", "format_flowed", "usenet_signature", "remove_signatures",
            "receipt_default", "receipt_type", "lookup_mx"
    };

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_setup);
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_options_send, container, false);

        // Get controls

        swKeyboard = view.findViewById(R.id.swKeyboard);
        swSuggestSent = view.findViewById(R.id.swSuggestSent);
        swSuggestReceived = view.findViewById(R.id.swSuggestReceived);
        swSuggestFrequently = view.findViewById(R.id.swSuggestFrequently);
        btnLocalContacts = view.findViewById(R.id.btnLocalContacts);
        swSendReminders = view.findViewById(R.id.swSendReminders);
        spSendDelayed = view.findViewById(R.id.spSendDelayed);
        spComposeFont = view.findViewById(R.id.spComposeFont);

        swAutoList = view.findViewById(R.id.swAutoList);
        swPrefixOnce = view.findViewById(R.id.swPrefixOnce);
        swExtendedReply = view.findViewById(R.id.swExtendedReply);
        swQuoteReply = view.findViewById(R.id.swQuoteReply);
        swResizeReply = view.findViewById(R.id.swResizeReply);
        spSignatureLocation = view.findViewById(R.id.spSignatureLocation);
        swSignatureReply = view.findViewById(R.id.swSignatureReply);
        swSignatureForward = view.findViewById(R.id.swSignatureForward);
        swDiscardDelete = view.findViewById(R.id.swDiscardDelete);

        swPlainOnly = view.findViewById(R.id.swPlainOnly);
        swFormatFlowed = view.findViewById(R.id.swFormatFlowed);
        swUsenetSignature = view.findViewById(R.id.swUsenetSignature);
        swRemoveSignatures = view.findViewById(R.id.swRemoveSignatures);
        swReceipt = view.findViewById(R.id.swReceipt);
        spReceiptType = view.findViewById(R.id.spReceiptType);
        swLookupMx = view.findViewById(R.id.swLookupMx);

        setOptions();

        // Wire controls

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        swKeyboard.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("keyboard", checked).apply();
            }
        });

        swSuggestSent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("suggest_sent", checked).apply();
                swSuggestFrequently.setEnabled(swSuggestSent.isChecked() || swSuggestReceived.isChecked());
            }
        });

        swSuggestReceived.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("suggest_received", checked).apply();
                swSuggestFrequently.setEnabled(swSuggestSent.isChecked() || swSuggestReceived.isChecked());
            }
        });

        swSuggestFrequently.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("suggest_frequently", checked).apply();
            }
        });

        btnLocalContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
                lbm.sendBroadcast(new Intent(ActivitySetup.ACTION_MANAGE_LOCAL_CONTACTS));
            }
        });

        swSendReminders.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("send_reminders", checked).apply();
            }
        });

        spSendDelayed.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                int[] values = getResources().getIntArray(R.array.sendDelayedValues);
                prefs.edit().putInt("send_delayed", values[position]).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                prefs.edit().remove("send_delayed").apply();
            }
        });

        spComposeFont.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String[] values = getResources().getStringArray(R.array.fontNameValues);
                String value = values[position];
                boolean monospaced = prefs.getBoolean("monospaced", false);
                if (value.equals(monospaced ? "monospace" : "sans-serif"))
                    prefs.edit().remove("compose_font").apply();
                else
                    prefs.edit().putString("compose_font", values[position]).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                prefs.edit().remove("compose_font").apply();
            }
        });

        swAutoList.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("autolist", checked).apply();
            }
        });

        swPrefixOnce.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("prefix_once", checked).apply();
            }
        });

        swExtendedReply.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("extended_reply", checked).apply();
            }
        });

        swQuoteReply.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("quote_reply", checked).apply();
            }
        });

        swResizeReply.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("resize_reply", checked).apply();
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

        swSignatureReply.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("signature_reply", checked).apply();
            }
        });

        swSignatureForward.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("signature_forward", checked).apply();
            }
        });

        swDiscardDelete.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("discard_delete", checked).apply();
            }
        });

        swPlainOnly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("plain_only", checked).apply();
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

        swLookupMx.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("lookup_mx", checked).apply();
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

        swKeyboard.setChecked(prefs.getBoolean("keyboard", true));
        swSuggestSent.setChecked(prefs.getBoolean("suggest_sent", true));
        swSuggestReceived.setChecked(prefs.getBoolean("suggest_received", false));
        swSuggestFrequently.setChecked(prefs.getBoolean("suggest_frequently", false));
        swSuggestFrequently.setEnabled(swSuggestSent.isChecked() || swSuggestReceived.isChecked());
        swSendReminders.setChecked(prefs.getBoolean("send_reminders", true));

        int send_delayed = prefs.getInt("send_delayed", 0);
        int[] sendDelayedValues = getResources().getIntArray(R.array.sendDelayedValues);
        for (int pos = 0; pos < sendDelayedValues.length; pos++)
            if (sendDelayedValues[pos] == send_delayed) {
                spSendDelayed.setSelection(pos);
                break;
            }

        boolean monospaced = prefs.getBoolean("monospaced", false);
        String compose_font = prefs.getString("compose_font", monospaced ? "monospace" : "sans-serif");
        String[] fontNameValues = getResources().getStringArray(R.array.fontNameValues);
        for (int pos = 0; pos < fontNameValues.length; pos++)
            if (fontNameValues[pos].equals(compose_font)) {
                spComposeFont.setSelection(pos);
                break;
            }

        swAutoList.setChecked(prefs.getBoolean("autolist", true));
        swPrefixOnce.setChecked(prefs.getBoolean("prefix_once", true));
        swExtendedReply.setChecked(prefs.getBoolean("extended_reply", false));
        swQuoteReply.setChecked(prefs.getBoolean("quote_reply", true));
        swResizeReply.setChecked(prefs.getBoolean("resize_reply", true));

        int signature_location = prefs.getInt("signature_location", 1);
        spSignatureLocation.setSelection(signature_location);

        swSignatureReply.setChecked(prefs.getBoolean("signature_reply", true));
        swSignatureForward.setChecked(prefs.getBoolean("signature_forward", true));
        swDiscardDelete.setChecked(prefs.getBoolean("discard_delete", false));

        swPlainOnly.setChecked(prefs.getBoolean("plain_only", false));
        swFormatFlowed.setChecked(prefs.getBoolean("format_flowed", false));
        swUsenetSignature.setChecked(prefs.getBoolean("usenet_signature", false));
        swRemoveSignatures.setChecked(prefs.getBoolean("remove_signatures", false));
        swReceipt.setChecked(prefs.getBoolean("receipt_default", false));

        int receipt_type = prefs.getInt("receipt_type", 2);
        spReceiptType.setSelection(receipt_type);

        swLookupMx.setChecked(prefs.getBoolean("lookup_mx", false));
    }
}
