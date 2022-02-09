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

    Copyright 2018-2022 by Marcel Bokhorst (M66B)
*/

import static android.app.Activity.RESULT_OK;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import java.util.List;

public class FragmentDialogSelectAccount extends FragmentDialogBase {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Context context = getContext();

        final int dp6 = Helper.dp2pixels(context, 6);
        final int iconSize = context.getResources().getDimensionPixelSize(R.dimen.menu_item_icon_size);

        final ArrayAdapter<EntityAccount> adapter = new ArrayAdapter<EntityAccount>(context, R.layout.spinner_item1, android.R.id.text1) {

            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                if (getCount() > 10)
                    view.setPadding(dp6, dp6, dp6, dp6);

                TextView tv = view.findViewById(android.R.id.text1);

                EntityAccount account = (EntityAccount) getItem(position);
                SpannableStringBuilder ssb = new SpannableStringBuilderEx(account.name);

                Drawable d = new ColorDrawable(account.color == null ? Color.TRANSPARENT : account.color);
                d.setBounds(0, 0, iconSize / 4, iconSize);

                ImageSpan imageSpan = new CenteredImageSpan(d);
                ssb.insert(0, "\uFFFC\u2002"); // object replacement character, en space
                ssb.setSpan(imageSpan, 0, 1, 0);

                tv.setText(ssb);

                return view;
            }
        };

        // TODO: spinner
        new SimpleTask<List<EntityAccount>>() {
            @Override
            protected List<EntityAccount> onExecute(Context context, Bundle args) {
                boolean all = (args != null && args.getBoolean("all"));

                DB db = DB.getInstance(context);
                return (all
                        ? db.account().getAccounts()
                        : db.account().getSynchronizingAccounts(null));
            }

            @Override
            protected void onExecuted(Bundle args, List<EntityAccount> accounts) {
                adapter.addAll(accounts);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, getArguments(), "messages:accounts");

        return new AlertDialog.Builder(context)
                .setIcon(R.drawable.twotone_account_circle_24)
                .setTitle(R.string.title_list_accounts)
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EntityAccount account = adapter.getItem(which);
                        Bundle args = getArguments();
                        args.putLong("account", account.id);
                        args.putString("name", account.name);
                        sendResult(RESULT_OK);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }
}