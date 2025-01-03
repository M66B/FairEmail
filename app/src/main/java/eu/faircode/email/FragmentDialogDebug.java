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

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

public class FragmentDialogDebug extends FragmentDialogBase {
    private boolean enabled;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null)
            enabled = savedInstanceState.getBoolean("fair:enabled");

        final Context context = getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_debug, null);
        final ImageButton ibInfo = view.findViewById(R.id.ibInfo);
        final EditText etIssue = view.findViewById(R.id.etIssue);
        final Spinner spAccount = view.findViewById(R.id.spAccount);
        final CheckBox cbContact = view.findViewById(R.id.cbContact);
        final CheckBox cbSend = view.findViewById(R.id.cbSend);
        final CheckBox cbCrashReports = view.findViewById(R.id.cbCrashReports);
        final ImageButton ibCrashReports = view.findViewById(R.id.ibCrashReports);

        ibInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.getContext().startActivity(Helper.getIntentIssue(v.getContext(), "debug"));
            }
        });

        boolean crash_reports = prefs.getBoolean("crash_reports", false);
        cbCrashReports.setVisibility(crash_reports ? View.GONE : View.VISIBLE);

        ibCrashReports.setVisibility(crash_reports ? View.GONE : View.VISIBLE);
        ibCrashReports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 104);
            }
        });

        final ArrayAdapter<EntityAccount> adapterAccount;
        etIssue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Do nothing
            }

            @Override
            public void afterTextChanged(Editable editable) {
                enabled = !TextUtils.isEmpty(editable.toString().trim());
                setEnabled(enabled);
            }
        });

        etIssue.post(new Runnable() {
            @Override
            public void run() {
                etIssue.requestFocus();
                Helper.showKeyboard(etIssue);
            }
        });

        adapterAccount = new ArrayAdapter<>(context, R.layout.spinner_item1, android.R.id.text1, new ArrayList<EntityAccount>());
        adapterAccount.setDropDownViewResource(R.layout.spinner_item1_dropdown);
        spAccount.setAdapter(adapterAccount);

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

                EntityAccount none = new EntityAccount();
                none.id = -1L;
                none.name = "-";
                none.primary = false;
                accounts.add(0, none);

                EntityAccount all = new EntityAccount();
                all.id = 0L;
                all.name = getString(R.string.title_widget_account_all);
                all.primary = false;
                accounts.add(1, all);

                adapterAccount.addAll(accounts);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, new Bundle(), "debug:accounts");

        return new AlertDialog.Builder(context)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Bundle args = getArguments();
                        args.putString("issue", etIssue.getText().toString());
                        args.putBoolean("contact", cbContact.isChecked());
                        args.putBoolean("send", cbSend.isChecked());

                        EntityAccount account = (EntityAccount) spAccount.getSelectedItem();
                        if (account != null)
                            args.putString("account", account.id + "/" + account.name + "/" + account.user);

                        if (cbCrashReports.isChecked()) {
                            prefs.edit()
                                    .remove("crash_report_count")
                                    .putBoolean("crash_reports", true)
                                    .apply();
                            Log.setCrashReporting(true);
                        }

                        sendResult(RESULT_OK);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    @Override
    public void onStart() {
        super.onStart();
        setEnabled(enabled);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean("fair:enabled", enabled);
        super.onSaveInstanceState(outState);
    }

    private void setEnabled(boolean value) {
        Button ok = ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE);
        if (ok != null)
            ok.setEnabled(value || BuildConfig.DEBUG);
    }
}
