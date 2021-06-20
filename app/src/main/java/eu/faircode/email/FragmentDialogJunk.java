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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.Group;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

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
        final Button btnEditRules = view.findViewById(R.id.btnEditRules);
        final CheckBox cbJunkFilter = view.findViewById(R.id.cbJunkFilter);
        final ImageButton ibInfoFilter = view.findViewById(R.id.ibInfoFilter);
        final CheckBox cbBlocklist = view.findViewById(R.id.cbBlocklist);
        final ImageButton ibInfoBlocklist = view.findViewById(R.id.ibInfoBlocklist);
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
                cbBlockDomain.setEnabled(isChecked);
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

        btnEditRules.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inJunk) {
                    new SimpleTask<EntityFolder>() {
                        @Override
                        protected EntityFolder onExecute(Context context, Bundle args) throws Throwable {
                            long account = args.getLong("account");

                            DB db = DB.getInstance(context);
                            EntityFolder inbox = db.folder().getFolderByType(account, EntityFolder.INBOX);

                            if (inbox == null)
                                throw new IllegalArgumentException(context.getString(R.string.title_no_inbox));

                            return inbox;
                        }

                        @Override
                        protected void onExecuted(Bundle args, EntityFolder inbox) {
                            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                            lbm.sendBroadcast(
                                    new Intent(ActivityView.ACTION_EDIT_RULES)
                                            .putExtra("account", account)
                                            .putExtra("protocol", protocol)
                                            .putExtra("folder", inbox.id)
                                            .putExtra("type", inbox.type));
                            dismiss();
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Log.unexpectedError(getParentFragmentManager(), ex);
                        }
                    }.execute(FragmentDialogJunk.this, args, "junk:rules");
                } else {
                    LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                    lbm.sendBroadcast(
                            new Intent(ActivityView.ACTION_EDIT_RULES)
                                    .putExtra("account", account)
                                    .putExtra("protocol", protocol)
                                    .putExtra("folder", folder)
                                    .putExtra("type", type));
                    dismiss();
                }
            }
        });

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

        // Initialize
        tvMessage.setText(inJunk
                ? getString(R.string.title_folder_junk)
                : getString(R.string.title_ask_spam_who, from));
        cbBlockSender.setEnabled(canBlock && ActivityBilling.isPro(context));
        cbBlockDomain.setEnabled(false);
        ibMore.setImageLevel(1);
        cbBlocklist.setChecked(check_blocklist && use_blocklist);
        grpInJunk.setVisibility(inJunk ? View.GONE : View.VISIBLE);
        grpMore.setVisibility(inJunk ? View.VISIBLE : View.GONE);

        new SimpleTask<Boolean>() {
            @Override
            protected void onPreExecute(Bundle args) {
                cbJunkFilter.setEnabled(false);
            }

            @Override
            protected Boolean onExecute(Context context, Bundle args) throws Throwable {
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
