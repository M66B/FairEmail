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

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Observer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class FragmentDialogAccount extends FragmentDialogBase {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Context context = getContext();

        final View dview = LayoutInflater.from(context).inflate(R.layout.dialog_review_account, null);
        final TextView tvName = dview.findViewById(R.id.tvName);
        final TextView tvInbox = dview.findViewById(R.id.tvInbox);
        final TextView tvDrafts = dview.findViewById(R.id.tvDrafts);
        final TextView tvSent = dview.findViewById(R.id.tvSent);
        final TextView tvSentWarning = dview.findViewById(R.id.tvSentWarning);
        final TextView tvTrash = dview.findViewById(R.id.tvTrash);
        final TextView tvJunk = dview.findViewById(R.id.tvJunk);
        final TextView tvArchive = dview.findViewById(R.id.tvArchive);
        final TextView tvLeft = dview.findViewById(R.id.tvLeft);
        final TextView tvRight = dview.findViewById(R.id.tvRight);
        final Button btnAccount = dview.findViewById(R.id.btnAccount);

        final Drawable check = context.getDrawable(R.drawable.twotone_check_24);
        final Drawable close = context.getDrawable(R.drawable.twotone_close_24);

        check.setBounds(0, 0, check.getIntrinsicWidth(), check.getIntrinsicHeight());
        close.setBounds(0, 0, close.getIntrinsicWidth(), close.getIntrinsicHeight());

        tvInbox.setCompoundDrawablesRelative(null, null, null, null);
        tvDrafts.setCompoundDrawablesRelative(null, null, null, null);
        tvSent.setCompoundDrawablesRelative(null, null, null, null);
        tvTrash.setCompoundDrawablesRelative(null, null, null, null);
        tvJunk.setCompoundDrawablesRelative(null, null, null, null);
        tvArchive.setCompoundDrawablesRelative(null, null, null, null);

        tvName.setText(null);
        tvLeft.setText(null);
        tvRight.setText(null);

        tvSentWarning.setVisibility(View.GONE);

        Bundle args = getArguments();
        final long account = args.getLong("account");

        btnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                lbm.sendBroadcast(
                        new Intent(ActivitySetup.ACTION_EDIT_ACCOUNT)
                                .putExtra("id", account)
                                .putExtra("protocol", EntityAccount.TYPE_IMAP));
                dismiss();
            }
        });

        DB db = DB.getInstance(context);

        db.account().liveAccount(account).observe(this, new Observer<EntityAccount>() {
            @Override
            public void onChanged(EntityAccount account) {
                tvName.setText(account.name);
            }
        });

        db.account().liveAccountSwipes(account).observe(this, new Observer<List<TupleAccountSwipes>>() {
            @Override
            public void onChanged(List<TupleAccountSwipes> swipes) {
                if (swipes != null && swipes.size() == 1) {
                    String left = swipes.get(0).left_name;
                    String right = swipes.get(0).right_name;
                    tvLeft.setText(left == null ? "-" : left);
                    tvRight.setText(right == null ? "-" : right);
                } else {
                    tvLeft.setText("?");
                    tvRight.setText("?");
                }
            }
        });

        db.folder().liveSystemFolders(account).observe(this, new Observer<List<EntityFolder>>() {
            @Override
            public void onChanged(List<EntityFolder> folders) {
                if (folders == null)
                    folders = new ArrayList<>();

                List<String> types = new ArrayList<>();
                for (EntityFolder folder : folders)
                    if (!folder.local)
                        types.add(folder.type);

                tvInbox.setCompoundDrawablesRelative(
                        types.contains(EntityFolder.INBOX) ? check : close, null, null, null);
                tvDrafts.setCompoundDrawablesRelative(
                        types.contains(EntityFolder.DRAFTS) ? check : close, null, null, null);
                tvSent.setCompoundDrawablesRelative(
                        types.contains(EntityFolder.SENT) ? check : close, null, null, null);
                tvSentWarning.setVisibility(types.contains(EntityFolder.SENT) ? View.GONE : View.VISIBLE);
                tvTrash.setCompoundDrawablesRelative(
                        types.contains(EntityFolder.TRASH) ? check : close, null, null, null);
                tvJunk.setCompoundDrawablesRelative(
                        types.contains(EntityFolder.JUNK) ? check : close, null, null, null);
                tvArchive.setCompoundDrawablesRelative(
                        types.contains(EntityFolder.ARCHIVE) ? check : close, null, null, null);

                int textColorPrimary = Helper.resolveColor(context, android.R.attr.textColorPrimary);
                int colorWarning = Helper.resolveColor(context, R.attr.colorWarning);

                tvInbox.setTextColor(types.contains(EntityFolder.INBOX) ? textColorPrimary : colorWarning);
                tvDrafts.setTextColor(types.contains(EntityFolder.DRAFTS) ? textColorPrimary : colorWarning);
                tvSent.setTextColor(types.contains(EntityFolder.SENT) ? textColorPrimary : colorWarning);
                tvTrash.setTextColor(types.contains(EntityFolder.TRASH) ? textColorPrimary : colorWarning);
                tvJunk.setTextColor(types.contains(EntityFolder.JUNK) ? textColorPrimary : colorWarning);
                tvArchive.setTextColor(types.contains(EntityFolder.ARCHIVE) ? textColorPrimary : colorWarning);
            }
        });

        return new AlertDialog.Builder(context)
                .setView(dview)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendResult(RESULT_OK);
                    }
                })
                .create();
    }
}