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

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.Group;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.mail.Address;

public class FragmentDialogSend extends FragmentDialogBase {
    static final int MAX_SHOW_RECIPIENTS = 5;
    static final int RECIPIENTS_WARNING = 10;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Bundle args = getArguments();
        final long id = args.getLong("id");
        final boolean sent_missing = args.getBoolean("sent_missing", false);
        final String address_error = args.getString("address_error");
        final String mx_error = args.getString("mx_error");
        final boolean remind_dsn = args.getBoolean("remind_dsn", false);
        final boolean remind_size = args.getBoolean("remind_size", false);
        final boolean remind_pgp = args.getBoolean("remind_pgp", false);
        final boolean remind_smime = args.getBoolean("remind_smime", false);
        final boolean remind_to = args.getBoolean("remind_to", false);
        final boolean remind_extra = args.getBoolean("remind_extra", false);
        final boolean remind_noreply = args.getBoolean("remind_noreply", false);
        final boolean remind_external = args.getBoolean("remind_external", false);
        final boolean remind_subject = args.getBoolean("remind_subject", false);
        final boolean remind_text = args.getBoolean("remind_text", false);
        final boolean remind_attachment = args.getBoolean("remind_attachment", false);
        final String remind_extension = args.getString("remind_extension");
        final boolean remind_internet = args.getBoolean("remind_internet", false);
        final boolean styled = args.getBoolean("styled", false);
        final long size = args.getLong("size", -1);
        final long max_size = args.getLong("max_size", -1);

        final Context context = getContext();

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final boolean send_reminders = prefs.getBoolean("send_reminders", true);
        final int send_delayed = prefs.getInt("send_delayed", 0);
        final boolean send_dialog = prefs.getBoolean("send_dialog", true);
        final boolean send_more = prefs.getBoolean("send_more", false);
        final boolean send_archive = prefs.getBoolean("send_archive", false);
        final MessageHelper.AddressFormat email_format = MessageHelper.getAddressFormat(getContext());

        final int colorWarning = Helper.resolveColor(context, R.attr.colorWarning);
        final int textColorSecondary = Helper.resolveColor(context, android.R.attr.textColorSecondary);
        final int[] encryptValues = getResources().getIntArray(R.array.encryptValues);
        final int[] sendDelayedValues = getResources().getIntArray(R.array.sendDelayedValues);
        final String[] sendDelayedNames = getResources().getStringArray(R.array.sendDelayedNames);

        final ViewGroup dview = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.dialog_send, null);
        final Button btnFixSent = dview.findViewById(R.id.btnFixSent);
        final TextView tvAddressError = dview.findViewById(R.id.tvAddressError);
        final TextView tvRemindDsn = dview.findViewById(R.id.tvRemindDsn);
        final TextView tvRemindSize = dview.findViewById(R.id.tvRemindSize);
        final TextView tvRemindPgp = dview.findViewById(R.id.tvRemindPgp);
        final TextView tvRemindSmime = dview.findViewById(R.id.tvRemindSmime);
        final TextView tvRemindTo = dview.findViewById(R.id.tvRemindTo);
        final TextView tvRemindExtra = dview.findViewById(R.id.tvRemindExtra);
        final TextView tvRemindNoReply = dview.findViewById(R.id.tvRemindNoReply);
        final TextView tvRemindExternal = dview.findViewById(R.id.tvRemindExternal);
        final TextView tvRemindSubject = dview.findViewById(R.id.tvRemindSubject);
        final TextView tvRemindText = dview.findViewById(R.id.tvRemindText);
        final TextView tvRemindAttachment = dview.findViewById(R.id.tvRemindAttachment);
        final TextView tvRemindExtension = dview.findViewById(R.id.tvRemindExtension);
        final TextView tvRemindInternet = dview.findViewById(R.id.tvRemindInternet);
        final SwitchCompat swSendReminders = dview.findViewById(R.id.swSendReminders);
        final TextView tvSendRemindersHint = dview.findViewById(R.id.tvSendRemindersHint);
        final TextView tvTo = dview.findViewById(R.id.tvTo);
        final TextView tvViaTitle = dview.findViewById(R.id.tvViaTitle);
        final TextView tvVia = dview.findViewById(R.id.tvVia);
        final ImageButton ibMore = dview.findViewById(R.id.ibMore);
        final TextView tvMore = dview.findViewById(R.id.tvMore);
        final CheckBox cbPlainOnly = dview.findViewById(R.id.cbPlainOnly);
        final TextView tvPlainHint = dview.findViewById(R.id.tvPlainHint);
        final CheckBox cbReceipt = dview.findViewById(R.id.cbReceipt);
        final TextView tvReceiptHint = dview.findViewById(R.id.tvReceiptHint);
        final TextView tvEncrypt = dview.findViewById(R.id.tvEncrypt);
        final Spinner spEncrypt = dview.findViewById(R.id.spEncrypt);
        final ImageButton ibEncryption = dview.findViewById(R.id.ibEncryption);
        final Spinner spPriority = dview.findViewById(R.id.spPriority);
        final Spinner spSensitivity = dview.findViewById(R.id.spSensitivity);
        final ImageButton ibSensitivity = dview.findViewById(R.id.ibSensitivity);
        final TextView tvSendAt = dview.findViewById(R.id.tvSendAt);
        final ImageButton ibSendAt = dview.findViewById(R.id.ibSendAt);
        final CheckBox cbArchive = dview.findViewById(R.id.cbArchive);
        final CheckBox cbNotAgain = dview.findViewById(R.id.cbNotAgain);
        final TextView tvNotAgain = dview.findViewById(R.id.tvNotAgain);
        final Group grpSentMissing = dview.findViewById(R.id.grpSentMissing);
        final Group grpMore = dview.findViewById(R.id.grpMore);

        final int[] dsnids = new int[]{
                R.id.cbPlainOnly, R.id.cbReceipt,
                R.id.tvEncrypt, R.id.spEncrypt,
                R.id.tvPriority, R.id.spPriority,
                R.id.tvSensitivity, R.id.spSensitivity
        };

        btnFixSent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.getContext().startActivity(new Intent(v.getContext(), ActivitySetup.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .putExtra("target", "accounts"));
            }
        });

        grpSentMissing.setVisibility(sent_missing ? View.VISIBLE : View.GONE);

        tvAddressError.setText(address_error == null ? mx_error : address_error);
        tvAddressError.setVisibility(address_error == null && mx_error == null ? View.GONE : View.VISIBLE);

        tvRemindDsn.setVisibility(remind_dsn ? View.VISIBLE : View.GONE);

        tvRemindSize.setText(getString(R.string.title_size_reminder,
                Helper.humanReadableByteCount(size),
                Helper.humanReadableByteCount(max_size)));
        tvRemindSize.setVisibility(remind_size ? View.VISIBLE : View.GONE);

        tvRemindPgp.setVisibility(remind_pgp ? View.VISIBLE : View.GONE);
        tvRemindSmime.setVisibility(remind_smime ? View.VISIBLE : View.GONE);

        tvRemindTo.setVisibility(remind_to ? View.VISIBLE : View.GONE);
        tvRemindExtra.setVisibility(send_reminders && remind_extra ? View.VISIBLE : View.GONE);
        tvRemindNoReply.setVisibility(remind_noreply ? View.VISIBLE : View.GONE);
        tvRemindExternal.setVisibility(remind_external ? View.VISIBLE : View.GONE);
        tvRemindSubject.setVisibility(send_reminders && remind_subject ? View.VISIBLE : View.GONE);
        tvRemindText.setVisibility(send_reminders && remind_text ? View.VISIBLE : View.GONE);
        tvRemindAttachment.setVisibility(send_reminders && remind_attachment ? View.VISIBLE : View.GONE);

        tvRemindExtension.setText(getString(R.string.title_attachment_warning, remind_extension));
        tvRemindExtension.setVisibility(send_reminders && remind_extension != null ? View.VISIBLE : View.GONE);

        tvRemindInternet.setVisibility(send_reminders && remind_internet ? View.VISIBLE : View.GONE);

        tvTo.setText(null);
        tvVia.setText(null);
        ibMore.setImageLevel(send_more ? 0 : 1);
        tvPlainHint.setVisibility(View.GONE);
        tvReceiptHint.setVisibility(View.GONE);
        spEncrypt.setTag(0);
        spEncrypt.setSelection(0);
        spPriority.setTag(1);
        spPriority.setSelection(1);
        spSensitivity.setTag(0);
        spSensitivity.setSelection(0);
        tvSendAt.setText(null);
        cbArchive.setEnabled(false);
        grpMore.setVisibility(send_more ? View.VISIBLE : View.GONE);
        cbNotAgain.setChecked(!send_dialog);
        cbNotAgain.setVisibility(send_dialog ? View.VISIBLE : View.GONE);
        tvNotAgain.setVisibility(cbNotAgain.isChecked() ? View.VISIBLE : View.GONE);

        Helper.setViewsEnabled(dview, false);
        for (int dsnid : dsnids)
            dview.findViewById(dsnid).setEnabled(false);

        boolean reminder = (remind_extra || remind_subject || remind_text ||
                remind_attachment || remind_extension != null || remind_internet);
        swSendReminders.setChecked(send_reminders);
        swSendReminders.setVisibility(send_reminders && reminder ? View.VISIBLE : View.GONE);
        tvSendRemindersHint.setVisibility(View.GONE);
        swSendReminders.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("send_reminders", checked).apply();
                tvRemindExtra.setVisibility(checked && remind_extra ? View.VISIBLE : View.GONE);
                tvRemindSubject.setVisibility(checked && remind_subject ? View.VISIBLE : View.GONE);
                tvRemindText.setVisibility(checked && remind_text ? View.VISIBLE : View.GONE);
                tvRemindAttachment.setVisibility(checked && remind_attachment ? View.VISIBLE : View.GONE);
                tvRemindExtension.setVisibility(checked && remind_extension != null ? View.VISIBLE : View.GONE);
                tvRemindInternet.setVisibility(checked && remind_internet ? View.VISIBLE : View.GONE);
                tvSendRemindersHint.setVisibility(checked ? View.GONE : View.VISIBLE);
            }
        });

        Runnable evalMore = new RunnableEx("more") {
            @Override
            protected void delegate() {
                boolean warning = (grpMore.getVisibility() != View.VISIBLE &&
                        cbPlainOnly.isChecked() && styled);
                int color = Helper.resolveColor(tvMore.getContext(), warning ? R.attr.colorWarning : android.R.attr.textColorSecondary);
                ibMore.setImageTintList(ColorStateList.valueOf(color));
                tvMore.setTextColor(color);
                tvMore.setTypeface(warning ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
                tvMore.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        0, 0,
                        warning ? R.drawable.twotone_warning_24 : 0, 0);
            }
        };

        evalMore.run();

        View.OnClickListener onMore = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (grpMore.getVisibility() == View.VISIBLE) {
                    ibMore.setImageLevel(1);
                    tvPlainHint.setVisibility(View.GONE);
                    tvReceiptHint.setVisibility(View.GONE);
                    grpMore.setVisibility(View.GONE);
                } else {
                    ibMore.setImageLevel(0);
                    tvPlainHint.setVisibility(cbPlainOnly.isChecked() && styled ? View.VISIBLE : View.GONE);
                    tvReceiptHint.setVisibility(cbReceipt.isChecked() ? View.VISIBLE : View.GONE);
                    grpMore.setVisibility(View.VISIBLE);
                }
                evalMore.run();
                prefs.edit().putBoolean("send_more", grpMore.getVisibility() == View.VISIBLE).apply();
            }
        };

        ibMore.setOnClickListener(onMore);
        tvMore.setOnClickListener(onMore);

        cbPlainOnly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                boolean more = (grpMore.getVisibility() == View.VISIBLE);
                tvPlainHint.setVisibility(checked && styled && more ? View.VISIBLE : View.GONE);
                evalMore.run();

                Bundle args = new Bundle();
                args.putLong("id", id);
                args.putBoolean("plain_only", checked);

                new SimpleTask<Void>() {
                    @Override
                    protected Void onExecute(Context context, Bundle args) {
                        long id = args.getLong("id");
                        boolean plain_only = args.getBoolean("plain_only");

                        DB db = DB.getInstance(context);
                        db.message().setMessagePlainOnly(id, plain_only ? 1 : 0);

                        return null;
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Log.unexpectedError(getParentFragmentManager(), ex);
                    }
                }.serial().execute(FragmentDialogSend.this, args, "compose:plain_only");
            }
        });

        cbReceipt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                boolean more = (grpMore.getVisibility() == View.VISIBLE);
                tvReceiptHint.setVisibility(checked && more ? View.VISIBLE : View.GONE);

                Bundle args = new Bundle();
                args.putLong("id", id);
                args.putBoolean("receipt", checked);

                new SimpleTask<Void>() {
                    @Override
                    protected Void onExecute(Context context, Bundle args) {
                        long id = args.getLong("id");
                        boolean receipt = args.getBoolean("receipt");

                        DB db = DB.getInstance(context);
                        db.message().setMessageReceiptRequest(id, receipt);

                        return null;
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Log.unexpectedError(getParentFragmentManager(), ex);
                    }
                }.serial().execute(FragmentDialogSend.this, args, "compose:receipt");
            }
        });

        spEncrypt.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int last = (int) spEncrypt.getTag();
                if (last != position) {
                    spEncrypt.setTag(position);
                    setEncrypt(encryptValues[position]);

                    tvEncrypt.setPaintFlags(tvEncrypt.getPaintFlags() & ~Paint.UNDERLINE_TEXT_FLAG);
                    tvEncrypt.setOnClickListener(null);

                    if ((encryptValues[position] == EntityMessage.PGP_SIGNONLY ||
                            encryptValues[position] == EntityMessage.PGP_ENCRYPTONLY ||
                            encryptValues[position] == EntityMessage.PGP_SIGNENCRYPT))
                        if (PgpHelper.isOpenKeychainInstalled(context)) {
                            tvEncrypt.setPaintFlags(tvEncrypt.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                            tvEncrypt.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String pkg = PgpHelper.getPackageName(v.getContext());
                                    PackageManager pm = v.getContext().getPackageManager();
                                    v.getContext().startActivity(pm.getLaunchIntentForPackage(pkg));
                                }
                            });
                        } else
                            ToastEx.makeText(context, R.string.title_no_openpgp, ToastEx.LENGTH_LONG).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                spEncrypt.setTag(0);
                setEncrypt(encryptValues[0]);
            }

            private void setEncrypt(int encrypt) {
                Bundle args = new Bundle();
                args.putLong("id", id);
                args.putInt("encrypt", encrypt);

                new SimpleTask<Void>() {
                    @Override
                    protected Void onExecute(Context context, Bundle args) {
                        long id = args.getLong("id");
                        int encrypt = args.getInt("encrypt");

                        DB db = DB.getInstance(context);
                        try {
                            db.beginTransaction();

                            EntityMessage message = db.message().getMessage(id);
                            if (message == null)
                                return null;

                            db.message().setMessageUiEncrypt(message.id, encrypt);

                            List<EntityAttachment> attachments = db.attachment().getAttachments(message.id);
                            if (attachments == null)
                                return null;
                            for (EntityAttachment attachment : attachments)
                                if (attachment.isEncryption())
                                    db.attachment().deleteAttachment(attachment.id);

                            if (encrypt != EntityMessage.ENCRYPT_NONE &&
                                    message.identity != null) {
                                int iencrypt =
                                        (encrypt == EntityMessage.SMIME_SIGNONLY ||
                                                encrypt == EntityMessage.SMIME_ENCRYPTONLY ||
                                                encrypt == EntityMessage.SMIME_SIGNENCRYPT
                                                ? 1 : 0);
                                db.identity().setIdentityEncrypt(message.identity, iencrypt);
                            }

                            db.setTransactionSuccessful();
                        } finally {
                            db.endTransaction();
                        }

                        return null;
                    }

                    @Override
                    protected void onExecuted(Bundle args, Void data) {
                        int encrypt = args.getInt("encrypt");

                        boolean none = EntityMessage.ENCRYPT_NONE.equals(encrypt);
                        tvRemindPgp.setVisibility(remind_pgp && none ? View.VISIBLE : View.GONE);
                        tvRemindSmime.setVisibility(remind_smime && none ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Log.unexpectedError(getParentFragmentManager(), ex);
                    }
                }.serial().execute(FragmentDialogSend.this, args, "compose:encrypt");
            }
        });

        ibEncryption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 12);
            }
        });

        spPriority.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int last = (int) spPriority.getTag();
                if (last != position) {
                    spPriority.setTag(position);
                    setPriority(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                spPriority.setTag(1);
                setPriority(1);
            }

            private void setPriority(int priority) {
                Bundle args = new Bundle();
                args.putLong("id", id);
                args.putInt("priority", priority);

                new SimpleTask<Void>() {
                    @Override
                    protected Void onExecute(Context context, Bundle args) {
                        long id = args.getLong("id");
                        int priority = args.getInt("priority");

                        DB db = DB.getInstance(context);
                        db.message().setMessagePriority(id, priority);

                        return null;
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Log.unexpectedError(getParentFragmentManager(), ex);
                    }
                }.serial().execute(FragmentDialogSend.this, args, "compose:priority");
            }
        });

        spSensitivity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int last = (int) spSensitivity.getTag();
                if (last != position) {
                    spSensitivity.setTag(position);
                    setSensitivity(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                spSensitivity.setTag(0);
                setSensitivity(0);
            }

            private void setSensitivity(int sensitivity) {
                Bundle args = new Bundle();
                args.putLong("id", id);
                args.putInt("sensitivity", sensitivity);

                new SimpleTask<Void>() {
                    @Override
                    protected Void onExecute(Context context, Bundle args) {
                        long id = args.getLong("id");
                        int sensitivity = args.getInt("sensitivity");

                        DB db = DB.getInstance(context);
                        db.message().setMessageSensitivity(id, sensitivity < 1 ? null : sensitivity);

                        return null;
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Log.unexpectedError(getParentFragmentManager(), ex);
                    }
                }.serial().execute(FragmentDialogSend.this, args, "compose:sensitivity");
            }
        });

        ibSensitivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 177);
            }
        });

        View.OnClickListener sendAt = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle args = new Bundle();
                args.putString("title", getString(R.string.title_send_at));
                args.putLong("id", id);

                Bundle a = getArguments();
                if (a != null && a.containsKey("sendAt"))
                    args.putLong("time", a.getLong("sendAt"));
                else {
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(new Date().getTime());
                    cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE) + 30);
                    args.putLong("time", cal.getTimeInMillis());
                }

                FragmentDialogDuration fragment = new FragmentDialogDuration();
                fragment.setArguments(args);
                fragment.setTargetFragment(FragmentDialogSend.this, 1);
                fragment.show(getParentFragmentManager(), "send:snooze");
            }
        };

        tvSendAt.setOnClickListener(sendAt);
        ibSendAt.setOnClickListener(sendAt);

        cbArchive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("send_archive", isChecked).apply();
            }
        });

        cbNotAgain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("send_dialog", !isChecked).apply();
                tvNotAgain.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });

        DB db = DB.getInstance(context);
        db.message().liveMessage(id).observe(getViewLifecycleOwner(), new Observer<TupleMessageEx>() {
            @Override
            public void onChanged(TupleMessageEx draft) {
                if (draft == null) {
                    dismiss();
                    return;
                }

                boolean dsn = (draft.dsn != null && !EntityMessage.DSN_NONE.equals(draft.dsn));
                int to = (draft.to == null ? 0 : draft.to.length);
                int extra = (draft.cc == null ? 0 : draft.cc.length) + (draft.bcc == null ? 0 : draft.bcc.length);

                List<Address> t = new ArrayList<>();
                if (draft.to != null)
                    if (to <= MAX_SHOW_RECIPIENTS)
                        t.addAll(Arrays.asList(draft.to));
                    else {
                        t.addAll((Arrays.asList(Arrays.copyOf(draft.to, MAX_SHOW_RECIPIENTS))));
                        extra += draft.to.length - MAX_SHOW_RECIPIENTS;
                    }
                Address[] tos = t.toArray(new Address[0]);

                if (extra == 0)
                    tvTo.setText(MessageHelper.formatAddresses(tos, email_format, false));
                else
                    tvTo.setText(getString(R.string.title_name_plus,
                            MessageHelper.formatAddresses(tos, email_format, false), extra));
                tvTo.setTextColor(Helper.resolveColor(context,
                        to + extra > RECIPIENTS_WARNING ? R.attr.colorWarning : android.R.attr.textColorPrimary));
                if (draft.identityColor != null && draft.identityColor != Color.TRANSPARENT)
                    tvViaTitle.setTextColor(draft.identityColor);
                tvVia.setText(draft.identityEmail);

                cbPlainOnly.setChecked(draft.isPlainOnly() && !dsn);
                cbReceipt.setChecked(draft.receipt_request != null && draft.receipt_request && !dsn);

                int encrypt = (draft.ui_encrypt == null || dsn ? EntityMessage.ENCRYPT_NONE : draft.ui_encrypt);
                for (int i = 0; i < encryptValues.length; i++)
                    if (encryptValues[i] == encrypt) {
                        spEncrypt.setTag(i);
                        spEncrypt.setSelection(i);
                        break;
                    }

                int priority = (draft.priority == null ? 1 : draft.priority);
                spPriority.setTag(priority);
                spPriority.setSelection(priority);

                int sensitivity = (draft.sensitivity == null ? 0 : draft.sensitivity);
                spSensitivity.setTag(sensitivity);
                spSensitivity.setSelection(sensitivity);

                if (draft.ui_snoozed == null) {
                    if (send_delayed == 0)
                        tvSendAt.setText(getString(R.string.title_now));
                    else
                        for (int pos = 0; pos < sendDelayedValues.length; pos++)
                            if (sendDelayedValues[pos] == send_delayed) {
                                tvSendAt.setText(getString(R.string.title_after, sendDelayedNames[pos]));
                                break;
                            }
                    tvSendAt.setTextColor(textColorSecondary);
                } else {
                    long now = new Date().getTime();
                    DateFormat DTF = Helper.getDateTimeInstance(context, SimpleDateFormat.MEDIUM, SimpleDateFormat.SHORT);
                    DateFormat D = new SimpleDateFormat("E");
                    tvSendAt.setText(D.format(draft.ui_snoozed) + " " + DTF.format(draft.ui_snoozed));
                    tvSendAt.setTextColor(draft.ui_snoozed < now ? colorWarning : textColorSecondary);
                }

                Helper.setViewsEnabled(dview, true);
                for (int dsnid : dsnids)
                    dview.findViewById(dsnid).setEnabled(!dsn);
            }
        });

        Bundle aargs = new Bundle();
        aargs.putLong("id", id);

        new SimpleTask<Boolean>() {
            @Override
            protected @NonNull
            Boolean onExecute(Context context, Bundle args) {
                long id = args.getLong("id");

                DB db = DB.getInstance(context);
                EntityMessage draft = db.message().getMessage(id);
                if (draft == null) {
                    args.putString("reason", "Draft gone");
                    return false;
                }

                if (draft.ui_snoozed != null)
                    args.putLong("sendAt", draft.ui_snoozed);

                if (TextUtils.isEmpty(draft.inreplyto)) {
                    args.putString("reason", "No in-reply-to");
                    return false;
                }

                EntityFolder archive = db.folder().getFolderByType(draft.account, EntityFolder.ARCHIVE);
                if (archive == null) {
                    args.putString("reason", "No archive");
                    return false;
                }

                List<EntityMessage> messages = db.message().getMessagesByMsgId(draft.account, draft.inreplyto);
                if (messages == null || messages.size() == 0) {
                    args.putString("reason", "In-reply-to gone");
                    return false;
                }

                for (EntityMessage message : messages) {
                    EntityFolder folder = db.folder().getFolder(message.folder);
                    if (folder == null)
                        continue;
                    if (EntityFolder.INBOX.equals(folder.type) || EntityFolder.USER.equals(folder.type))
                        return true;
                }

                args.putString("reason", "Not in inbox or unread");
                return false;
            }

            @Override
            protected void onExecuted(Bundle args, Boolean data) {
                if (args.containsKey("sendAt"))
                    getArguments().putLong("sendAt", args.getLong("sendAt"));

                if (!data) {
                    String reason = args.getString("reason");
                    if (BuildConfig.DEBUG)
                        cbArchive.setText(reason);
                    else
                        Log.i("Auto archive reason=" + reason);
                }
                if (send_archive && data)
                    cbArchive.setChecked(true);
                cbArchive.setEnabled(data);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                // Ignored
            }
        }.serial().execute(FragmentDialogSend.this, aargs, "send:archive");

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setView(dview)
                .setNegativeButton(android.R.string.cancel, null);

        if (address_error == null && !remind_to && !remind_size) {
            if (send_delayed != 0)
                builder.setNeutralButton(R.string.title_send_now, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getArguments().putBoolean("archive", cbArchive.isChecked());
                        sendResult(Activity.RESULT_FIRST_USER);
                    }
                });
            builder.setPositiveButton(R.string.title_send, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    getArguments().putBoolean("archive", cbArchive.isChecked());
                    sendResult(Activity.RESULT_OK);
                }
            });
        }

        return builder.create();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == RESULT_OK && intent != null) {
            Bundle data = intent.getBundleExtra("args");
            long id = data.getLong("id");
            long time = data.getLong("time");

            getArguments().putLong("sendAt", time);

            Bundle args = new Bundle();
            args.putLong("id", id);
            args.putLong("time", time);

            new SimpleTask<Void>() {
                @Override
                protected Void onExecute(Context context, Bundle args) {
                    long id = args.getLong("id");
                    long time = args.getLong("time");

                    DB db = DB.getInstance(context);
                    db.message().setMessageSnoozed(id, time);

                    return null;
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(getParentFragmentManager(), ex);
                }
            }.serial().execute(this, args, "compose:snooze");
        }
    }
}
