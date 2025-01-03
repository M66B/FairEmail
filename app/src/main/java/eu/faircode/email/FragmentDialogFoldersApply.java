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

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import java.util.List;

public class FragmentDialogFoldersApply extends FragmentDialogBase {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_folder_all, null);
        final RadioGroup rgSynchronize = view.findViewById(R.id.rgSynchronize);
        final EditText etSyncDays = view.findViewById(R.id.etSyncDays);
        final EditText etKeepDays = view.findViewById(R.id.etKeepDays);
        final CheckBox cbKeepAll = view.findViewById(R.id.cbKeepAll);
        final CheckBox cbPollSystem = view.findViewById(R.id.cbPollSystem);
        final CheckBox cbPollUser = view.findViewById(R.id.cbPollUser);

        cbKeepAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                etKeepDays.setEnabled(!isChecked);
            }
        });

        return new AlertDialog.Builder(getContext())
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Bundle args = getArguments();
                        int optionId = rgSynchronize.getCheckedRadioButtonId();
                        if (optionId == R.id.rbEnable)
                            args.putBoolean("enable", true);
                        else if (optionId == R.id.rbDisable)
                            args.putBoolean("enable", false);
                        args.putString("sync", etSyncDays.getText().toString());
                        args.putString("keep", cbKeepAll.isChecked()
                                ? Integer.toString(Integer.MAX_VALUE)
                                : etKeepDays.getText().toString());
                        args.putBoolean("system", cbPollSystem.isChecked());
                        args.putBoolean("user", cbPollUser.isChecked());

                        new SimpleTask<Void>() {
                            @Override
                            protected Void onExecute(Context context, Bundle args) throws Throwable {
                                long aid = args.getLong("account");
                                Boolean enable = null;
                                if (args.containsKey("enable"))
                                    enable = args.getBoolean("enable");
                                String sync = args.getString("sync");
                                String keep = args.getString("keep");
                                boolean system = args.getBoolean("system");
                                boolean user = args.getBoolean("user");

                                if (TextUtils.isEmpty(sync))
                                    sync = "7";
                                if (TextUtils.isEmpty(keep))
                                    keep = "30";

                                DB db = DB.getInstance(context);
                                try {
                                    db.beginTransaction();

                                    EntityAccount account = db.account().getAccount(aid);
                                    if (account == null)
                                        return null;

                                    if (system && account.poll_interval > 15)
                                        db.account().setAccountKeepAliveInterval(account.id, 15);

                                    List<EntityFolder> folders = db.folder().getFolders(aid, false, true);
                                    if (folders == null)
                                        return null;

                                    for (EntityFolder folder : folders) {
                                        if (EntityFolder.USER.equals(folder.type)) {
                                            if (enable != null) {
                                                folder.synchronize = enable;
                                                db.folder().setFolderSynchronize(folder.id, folder.synchronize);
                                            }

                                            db.folder().setFolderProperties(
                                                    folder.id,
                                                    Integer.parseInt(sync),
                                                    Integer.parseInt(keep));
                                        }

                                        if (folder.synchronize && !folder.poll)
                                            if (EntityFolder.USER.equals(folder.type)
                                                    ? user
                                                    : system && !EntityFolder.INBOX.equals(folder.type))
                                                db.folder().setFolderPoll(folder.id, true);
                                    }

                                    db.setTransactionSuccessful();
                                } finally {
                                    db.endTransaction();
                                }

                                ServiceSynchronize.reload(context, aid, false, "Apply");

                                return null;
                            }

                            @Override
                            protected void onException(Bundle args, Throwable ex) {
                                Log.unexpectedError(getParentFragmentManager(), ex);
                            }
                        }.execute(FragmentDialogFoldersApply.this, args, "folders:all");
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }
}
