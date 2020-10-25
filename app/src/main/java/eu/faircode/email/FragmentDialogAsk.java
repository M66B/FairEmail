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

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;

public class FragmentDialogAsk extends FragmentDialogBase {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        String question = args.getString("question");
        String remark = args.getString("remark");
        String notagain = args.getString("notagain");
        boolean warning = args.getBoolean("warning");

        final Context context = getContext();
        final int colorError = Helper.resolveColor(context, R.attr.colorError);

        View dview = LayoutInflater.from(context).inflate(R.layout.dialog_ask_again, null);
        TextView tvMessage = dview.findViewById(R.id.tvMessage);
        TextView tvRemark = dview.findViewById(R.id.tvRemark);
        CheckBox cbNotAgain = dview.findViewById(R.id.cbNotAgain);

        tvMessage.setText(question);
        tvRemark.setText(remark);
        tvRemark.setVisibility(remark == null ? View.GONE : View.VISIBLE);
        cbNotAgain.setVisibility(notagain == null ? View.GONE : View.VISIBLE);

        if (warning) {
            Drawable w = context.getResources().getDrawable(R.drawable.twotone_warning_24, context.getTheme());
            w.setBounds(0, 0, w.getIntrinsicWidth(), w.getIntrinsicHeight());
            w.setTint(colorError);
            tvMessage.setCompoundDrawablesRelative(w, null, null, null);
            tvMessage.setCompoundDrawablePadding(Helper.dp2pixels(context, 12));
        }

        if (notagain != null)
            cbNotAgain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                    prefs.edit().putBoolean(notagain, isChecked).apply();
                }
            });

        return new AlertDialog.Builder(getContext())
                .setView(dview)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendResult(Activity.RESULT_OK);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sendResult(Activity.RESULT_CANCELED);
                    }
                })
                .create();
    }
}
