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
import android.widget.TextView;
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
    private Button btnLocalContacts;
    private SwitchCompat swPrefixOnce;
    private SwitchCompat swExtendedReply;
    private SwitchCompat swQuoteReply;
    private SwitchCompat swPlainOnly;
    private Spinner spSignatureLocation;
    private SwitchCompat swUsenetSignature;
    private SwitchCompat swRemoveSignatures;
    private SwitchCompat swResizeImages;
    private SwitchCompat swResizeAttachments;
    private Spinner spAutoResize;
    private TextView tvAutoResize;
    private SwitchCompat swSendReminders;
    private SwitchCompat swReceipt;
    private SwitchCompat swLookupMx;
    private Spinner spSendDelayed;

    private final static String[] RESET_OPTIONS = new String[]{
            "keyboard", "suggest_sent", "suggested_received",
            "prefix_once", "extended_reply", "quote_reply",
            "plain_only", "signature_location", "usenet_signature", "remove_signatures",
            "resize_images", "resize_attachments", "send_reminders", "receipt_default", "resize", "lookup_mx", "send_delayed"
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
        btnLocalContacts = view.findViewById(R.id.btnLocalContacts);
        swPrefixOnce = view.findViewById(R.id.swPrefixOnce);
        swExtendedReply = view.findViewById(R.id.swExtendedReply);
        swQuoteReply = view.findViewById(R.id.swQuoteReply);
        swPlainOnly = view.findViewById(R.id.swPlainOnly);
        spSignatureLocation = view.findViewById(R.id.spSignatureLocation);
        swUsenetSignature = view.findViewById(R.id.swUsenetSignature);
        swRemoveSignatures = view.findViewById(R.id.swRemoveSignatures);
        swResizeImages = view.findViewById(R.id.swResizeImages);
        swResizeAttachments = view.findViewById(R.id.swResizeAttachments);
        spAutoResize = view.findViewById(R.id.spAutoResize);
        tvAutoResize = view.findViewById(R.id.tvAutoResize);
        swSendReminders = view.findViewById(R.id.swSendReminders);
        swReceipt = view.findViewById(R.id.swReceipt);
        swLookupMx = view.findViewById(R.id.swLookupMx);
        spSendDelayed = view.findViewById(R.id.spSendDelayed);

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
            }
        });

        swSuggestReceived.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("suggest_received", checked).apply();
            }
        });

        btnLocalContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
                lbm.sendBroadcast(new Intent(ActivitySetup.ACTION_MANAGE_LOCAL_CONTACTS));
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

        swPlainOnly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("plain_only", checked).apply();
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

        swResizeImages.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("resize_images", checked).apply();
            }
        });

        swResizeAttachments.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("resize_attachments", checked).apply();
                spAutoResize.setEnabled(swResizeImages.isChecked() || swResizeAttachments.isChecked());
            }
        });

        spAutoResize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                int[] values = getResources().getIntArray(R.array.resizeValues);
                prefs.edit().putInt("resize", values[position]).apply();
                tvAutoResize.setText(getString(R.string.title_advanced_resize_pixels, values[position]));
                spAutoResize.setEnabled(swResizeImages.isChecked() || swResizeAttachments.isChecked());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                prefs.edit().remove("resize").apply();
            }
        });

        swReceipt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("receipt_default", checked).apply();
            }
        });

        swSendReminders.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("send_reminders", checked).apply();
            }
        });

        swLookupMx.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("lookup_mx", checked).apply();
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
        swPrefixOnce.setChecked(prefs.getBoolean("prefix_once", true));
        swExtendedReply.setChecked(prefs.getBoolean("extended_reply", false));
        swQuoteReply.setChecked(prefs.getBoolean("quote_reply", true));
        swPlainOnly.setChecked(prefs.getBoolean("plain_only", false));

        int signature_location = prefs.getInt("signature_location", 1);
        spSignatureLocation.setSelection(signature_location);

        swUsenetSignature.setChecked(prefs.getBoolean("usenet_signature", false));
        swRemoveSignatures.setChecked(prefs.getBoolean("remove_signatures", false));

        swResizeImages.setChecked(prefs.getBoolean("resize_images", true));
        swResizeAttachments.setChecked(prefs.getBoolean("resize_attachments", true));

        int resize = prefs.getInt("resize", FragmentCompose.REDUCED_IMAGE_SIZE);
        int[] resizeValues = getResources().getIntArray(R.array.resizeValues);
        for (int pos = 0; pos < resizeValues.length; pos++)
            if (resizeValues[pos] == resize) {
                spAutoResize.setSelection(pos);
                tvAutoResize.setText(getString(R.string.title_advanced_resize_pixels, resizeValues[pos]));
                break;
            }
        spAutoResize.setEnabled(swResizeImages.isChecked() || swResizeAttachments.isChecked());

        swSendReminders.setChecked(prefs.getBoolean("send_reminders", true));
        swReceipt.setChecked(prefs.getBoolean("receipt_default", false));
        swLookupMx.setChecked(prefs.getBoolean("lookup_mx", false));

        int send_delayed = prefs.getInt("send_delayed", 0);
        int[] sendDelayedValues = getResources().getIntArray(R.array.sendDelayedValues);
        for (int pos = 0; pos < sendDelayedValues.length; pos++)
            if (sendDelayedValues[pos] == send_delayed) {
                spSendDelayed.setSelection(pos);
                break;
            }
    }
}
