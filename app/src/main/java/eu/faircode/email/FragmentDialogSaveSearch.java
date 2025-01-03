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
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

public class FragmentDialogSaveSearch extends FragmentDialogBase {
    private ViewButtonColor btnColor;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Bundle args = getArguments();

        BoundaryCallbackMessages.SearchCriteria criteria =
                (BoundaryCallbackMessages.SearchCriteria) args.getSerializable("criteria");
        if (criteria == null)
            criteria = new BoundaryCallbackMessages.SearchCriteria();

        final Context context = getContext();
        View dview = LayoutInflater.from(context).inflate(R.layout.dialog_save_search, null);
        EditText etName = dview.findViewById(R.id.etName);
        EditText etOrder = dview.findViewById(R.id.etOrder);
        btnColor = dview.findViewById(R.id.btnColor);

        btnColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.hideKeyboard(etName);

                Bundle args = new Bundle();
                args.putInt("color", btnColor.getColor());
                args.putString("title", getString(R.string.title_color));
                args.putBoolean("reset", true);

                FragmentDialogColor fragment = new FragmentDialogColor();
                fragment.setArguments(args);
                fragment.setTargetFragment(FragmentDialogSaveSearch.this, 1234);
                fragment.show(getParentFragmentManager(), "search:color");
            }
        });

        etName.setText(criteria.name == null ? criteria.getTitle(context) : criteria.name);
        etOrder.setText(criteria.order == null ? null : Integer.toString(criteria.order));
        btnColor.setColor(criteria.color);

        AlertDialog.Builder dialog = new AlertDialog.Builder(context)
                .setView(dview)
                .setPositiveButton(R.string.title_save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String order = etOrder.getText().toString();
                        args.putString("name", etName.getText().toString());
                        args.putInt("order",
                                !TextUtils.isEmpty(order) && TextUtils.isDigitsOnly(order)
                                        ? Integer.parseInt(order) : -1);
                        args.putInt("color", btnColor.getColor());
                        sendResult(Activity.RESULT_OK);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sendResult(Activity.RESULT_CANCELED);
                    }
                });

        if (criteria.id != null)
            dialog.setNeutralButton(R.string.title_delete, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    sendResult(Activity.RESULT_FIRST_USER);
                }
            });

        return dialog.create();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            if (resultCode == RESULT_OK && data != null) {
                Bundle args = data.getBundleExtra("args");
                int color = args.getInt("color");
                btnColor.setColor(color);
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }
}
