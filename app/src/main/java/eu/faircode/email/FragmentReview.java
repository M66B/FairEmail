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
import android.content.DialogInterface;
import android.content.Intent;
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

import java.util.List;

import static android.app.Activity.RESULT_OK;

public class FragmentReview extends FragmentDialogBase {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View dview = LayoutInflater.from(getContext()).inflate(R.layout.dialog_review_account, null);
        TextView tvLeft = dview.findViewById(R.id.tvLeft);
        TextView tvRight = dview.findViewById(R.id.tvRight);
        Button btnAccount = dview.findViewById(R.id.btnAccount);

        tvLeft.setText("");
        tvRight.setText("");

        Bundle args = getArguments();
        final long account = args.getLong("account");

        btnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
                lbm.sendBroadcast(
                        new Intent(ActivitySetup.ACTION_EDIT_ACCOUNT)
                                .putExtra("id", account)
                                .putExtra("protocol", EntityAccount.TYPE_IMAP));
                dismiss();
            }
        });

        DB db = DB.getInstance(getContext());
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

        return new AlertDialog.Builder(getContext())
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