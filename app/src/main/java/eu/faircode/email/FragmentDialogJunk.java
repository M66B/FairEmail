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

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import org.json.JSONObject;

import java.util.List;
import java.util.Locale;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

public class FragmentDialogJunk extends FragmentDialogBase {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Bundle args = getArguments();
        final long account = args.getLong("account");
        final int protocol = args.getInt("protocol");
        final long folder = args.getLong("folder");
        final String type = args.getString("type");
        final String from = args.getString("from");
        final boolean inJunk = args.getBoolean("inJunk");
        final boolean canBlock = args.getBoolean("canBlock");

        final Context context = getContext();
        final View view = LayoutInflater.from(context).inflate(R.layout.dialog_junk, null);
        final TextView tvMessage = view.findViewById(R.id.tvMessage);
        final ImageButton ibInfoProvider = view.findViewById(R.id.ibInfoProvider);
        final CheckBox cbBlockSender = view.findViewById(R.id.cbBlockSender);
        final CheckBox cbBlockDomain = view.findViewById(R.id.cbBlockDomain);
        final ImageButton ibMore = view.findViewById(R.id.ibMore);
        final TextView tvMore = view.findViewById(R.id.tvMore);
        final CheckBox cbJunkFilter = view.findViewById(R.id.cbJunkFilter);
        final ImageButton ibInfoFilter = view.findViewById(R.id.ibInfoFilter);
        final CheckBox cbBlocklist = view.findViewById(R.id.cbBlocklist);
        final TextView tvBlocklist = view.findViewById(R.id.tvBlocklist);
        final ImageButton ibInfoBlocklist = view.findViewById(R.id.ibInfoBlocklist);
        final Button btnClear = view.findViewById(R.id.btnClear);
        final Group grpInJunk = view.findViewById(R.id.grpInJunk);
        final Group grpMore = view.findViewById(R.id.grpMore);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean check_blocklist = prefs.getBoolean("check_blocklist", false);
        boolean use_blocklist = prefs.getBoolean("use_blocklist", false);

        // Wire controls

        ibInfoProvider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 92);
            }
        });

        cbBlockSender.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                cbBlockDomain.setEnabled(isChecked && ActivityBilling.isPro(context));
            }
        });

        View.OnClickListener onMore = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (grpMore.getVisibility() == View.VISIBLE) {
                    ibMore.setImageLevel(1);
                    grpMore.setVisibility(View.GONE);
                } else {
                    ibMore.setImageLevel(0);
                    grpMore.setVisibility(View.VISIBLE);
                }
            }
        };

        ibMore.setOnClickListener(onMore);
        tvMore.setOnClickListener(onMore);

        cbJunkFilter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                args.putBoolean("filter", isChecked);

                new SimpleTask<Void>() {
                    @Override
                    protected Void onExecute(Context context, Bundle args) throws Throwable {
                        long aid = args.getLong("account");
                        long fid = args.getLong("folder");
                        boolean filter = args.getBoolean("filter");

                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

                        DB db = DB.getInstance(context);

                        EntityFolder folder = db.folder().getFolder(fid);
                        if (folder == null)
                            return null;

                        EntityFolder junk = db.folder().getFolderByType(aid, EntityFolder.JUNK);
                        if (junk == null)
                            return null;

                        try {
                            db.beginTransaction();

                            db.folder().setFolderDownload(folder.id,
                                    folder.download || filter);
                            db.folder().setFolderAutoClassify(folder.id,
                                    folder.auto_classify_source || filter,
                                    folder.auto_classify_target);

                            db.folder().setFolderDownload(junk.id,
                                    junk.download || filter);
                            db.folder().setFolderAutoClassify(junk.id,
                                    junk.auto_classify_source || filter,
                                    filter);

                            db.setTransactionSuccessful();
                        } finally {
                            db.endTransaction();
                        }

                        prefs.edit().putBoolean("classification", true).apply();

                        return null;
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Log.unexpectedError(getParentFragmentManager(), ex);
                    }
                }.execute(FragmentDialogJunk.this, args, "junk:filter");
            }
        });

        ibInfoFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 163);
            }
        });

        cbBlocklist.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit()
                        .putBoolean("check_blocklist", isChecked)
                        .putBoolean("use_blocklist", isChecked)
                        .apply();
            }
        });

        ibInfoBlocklist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 168, true);
            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(v.getContext())
                        .setTitle(R.string.title_junk_clear)
                        .setMessage(R.string.title_junk_clear_hint)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Bundle args = new Bundle();
                                args.putLong("folder", folder);

                                new SimpleTask<Void>() {
                                    @Override
                                    protected Void onExecute(Context context, Bundle args) throws Throwable {
                                        long fid = args.getLong("folder");

                                        DB db = DB.getInstance(context);
                                        EntityFolder folder = db.folder().getFolder(fid);
                                        if (folder == null)
                                            return null;

                                        EntityFolder junk = db.folder().getFolderByType(folder.account, EntityFolder.JUNK);
                                        if (junk == null)
                                            return null;

                                        List<EntityRule> rules = db.rule().getRules(fid);
                                        if (rules == null)
                                            return null;

                                        for (EntityRule rule : rules) {
                                            JSONObject jaction = new JSONObject(rule.action);
                                            int type = jaction.optInt("type", -1);
                                            long target = jaction.optLong("target", -1);
                                            if (type == EntityRule.TYPE_MOVE && target == junk.id) {
                                                EntityLog.log(context, "Deleting junk rule=" + rule.id);
                                                db.rule().deleteRule(rule.id);
                                            }
                                        }

                                        int count = db.contact().deleteContact(account, EntityContact.TYPE_JUNK);
                                        EntityLog.log(context, "Deleted junk contacts=" + count);

                                        return null;
                                    }

                                    @Override
                                    protected void onExecuted(Bundle args, Void data) {
                                        ToastEx.makeText(getContext(), R.string.title_completed, Toast.LENGTH_LONG).show();
                                    }

                                    @Override
                                    protected void onException(Bundle args, Throwable ex) {
                                        Log.unexpectedError(getParentFragmentManager(), ex);
                                    }
                                }.execute(FragmentDialogJunk.this, args, "junk:clear");
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing
                            }
                        })
                        .show();
            }
        });

        try {
            boolean common = false;
            Address[] froms = InternetAddress.parseHeader(from, false);
            String email = (froms.length == 0 ? null : ((InternetAddress) froms[0]).getAddress());
            int at = (email == null ? -1 : email.indexOf('@'));
            String domain = (at > 0 ? email.substring(at + 1).toLowerCase(Locale.ROOT) : null);

            if (domain != null) {
                List<String> domains = EmailProvider.getDomainNames(context);
                for (String d : domains)
                    if (domain.matches(d)) {
                        common = true;
                        break;
                    }
            }

            if (common) {
                int dp6 = Helper.dp2pixels(context, 6);
                int colorWarning = Helper.resolveColor(context, R.attr.colorWarning);
                cbBlockDomain.setTextColor(colorWarning);
                cbBlockDomain.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.twotone_warning_24, 0);
                cbBlockDomain.setCompoundDrawablePadding(dp6);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    cbBlockDomain.setCompoundDrawableTintList(ColorStateList.valueOf(colorWarning));
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }

        // Initialize
        tvMessage.setText(inJunk
                ? getString(R.string.title_folder_junk)
                : getString(R.string.title_ask_spam_who, from));
        cbBlockSender.setEnabled(canBlock);
        cbBlockDomain.setEnabled(false);
        cbBlockSender.setChecked(canBlock);
        ibMore.setImageLevel(1);
        cbBlocklist.setChecked(check_blocklist && use_blocklist);
        tvBlocklist.setText(TextUtils.join(", ", DnsBlockList.getNamesEnabled(context)));
        grpInJunk.setVisibility(inJunk ? View.GONE : View.VISIBLE);
        grpMore.setVisibility(inJunk ? View.VISIBLE : View.GONE);

        new SimpleTask<Boolean>() {
            @Override
            protected void onPreExecute(Bundle args) {
                cbJunkFilter.setEnabled(false);
            }

            @Override
            protected Boolean onExecute(Context context, Bundle args) {
                long aid = args.getLong("account");
                long fid = args.getLong("folder");

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                boolean classification = prefs.getBoolean("classification", false);

                DB db = DB.getInstance(context);
                EntityFolder folder = db.folder().getFolder(fid);
                if (folder == null)
                    return null;

                EntityFolder junk = db.folder().getFolderByType(aid, EntityFolder.JUNK);
                if (junk == null)
                    return null;

                return (classification &&
                        folder.download && folder.auto_classify_source &&
                        junk.download && junk.auto_classify_source && junk.auto_classify_target);
            }

            @Override
            protected void onExecuted(Bundle args, Boolean filter) {
                if (filter != null) {
                    cbJunkFilter.setChecked(filter);
                    cbJunkFilter.setEnabled(true);
                }
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(FragmentDialogJunk.this, args, "junk:filter");

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setView(view)
                .setNegativeButton(android.R.string.cancel, null);

        if (!inJunk)
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    getArguments().putBoolean("block_sender", cbBlockSender.isChecked());
                    getArguments().putBoolean("block_domain", cbBlockDomain.isChecked());
                    sendResult(Activity.RESULT_OK);
                }
            });

        return builder.create();
    }
}
