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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

public class FragmentDialogSwipes extends FragmentDialogBase {
    private Spinner spLeft;
    private Spinner spRight;
    private ArrayAdapter<EntityFolder> adapter;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Context context = getContext();
        View dview = LayoutInflater.from(context).inflate(R.layout.dialog_swipes, null);
        spLeft = dview.findViewById(R.id.spLeft);
        spRight = dview.findViewById(R.id.spRight);

        adapter = new ArrayAdapter<>(context, R.layout.spinner_item1, android.R.id.text1, new ArrayList<EntityFolder>());
        adapter.setDropDownViewResource(R.layout.spinner_item1_dropdown);

        spLeft.setAdapter(adapter);
        spRight.setAdapter(adapter);

        adapter.addAll(getFolderActions(context));

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int leftPos = prefs.getInt("swipe_left_default", 2); // Trash
        int rightPos = prefs.getInt("swipe_right_default", 1); // Archive

        spLeft.setSelection(leftPos);
        spRight.setSelection(rightPos);

        return new AlertDialog.Builder(context)
                .setView(dview)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        prefs.edit()
                                .putInt("swipe_left_default", spLeft.getSelectedItemPosition())
                                .putInt("swipe_right_default", spRight.getSelectedItemPosition())
                                .apply();

                        EntityFolder left = (EntityFolder) spLeft.getSelectedItem();
                        EntityFolder right = (EntityFolder) spRight.getSelectedItem();

                        if ((left != null && EntityMessage.SWIPE_ACTION_HIDE.equals(left.id)) ||
                                (right != null && EntityMessage.SWIPE_ACTION_HIDE.equals(right.id)))
                            prefs.edit()
                                    .putBoolean("message_tools", true)
                                    .putBoolean("button_hide", true)
                                    .apply();

                        new SimpleTask<Void>() {
                            @Override
                            protected Void onExecute(Context context, Bundle args) {
                                DB db = DB.getInstance(context);
                                try {
                                    db.beginTransaction();

                                    List<EntityAccount> accounts = db.account().getAccounts();
                                    for (EntityAccount account : accounts)
                                        if (account.protocol == EntityAccount.TYPE_IMAP)
                                            setDefaultFolderActions(context, account);

                                    db.setTransactionSuccessful();
                                } finally {
                                    db.endTransaction();
                                }

                                return null;
                            }

                            @Override
                            protected void onExecuted(Bundle args, Void data) {
                                ToastEx.makeText(context, R.string.title_completed, Toast.LENGTH_LONG).show();
                            }

                            @Override
                            protected void onException(Bundle args, Throwable ex) {
                                Log.unexpectedError(getParentFragmentManager(), ex);
                            }
                        }.execute(context, getViewLifecycleOwner(), new Bundle(), "dialog:swipe");
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    static void setDefaultFolderActions(Context context, @NonNull EntityAccount account) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final int leftPos = prefs.getInt("swipe_left_default", 2); // Trash
        final int rightPos = prefs.getInt("swipe_right_default", 1); // Archive

        List<EntityFolder> actions = getFolderActions(context);
        EntityFolder left = (leftPos < 0 || leftPos >= actions.size() ? null : actions.get(leftPos));
        EntityFolder right = (rightPos < 0 || rightPos >= actions.size() ? null : actions.get(rightPos));

        account.swipe_left = getAction(context, left == null ? 0 : left.id, account.id);
        account.swipe_right = getAction(context, right == null ? 0 : right.id, account.id);

        DB db = DB.getInstance(context);
        db.account().setAccountSwipes(account.id, account.swipe_left, account.swipe_right);
    }

    static List<EntityFolder> getFolderActions(Context context) {
        List<EntityFolder> folders = FragmentAccount.getFolderActions(context);

        EntityFolder trash = new EntityFolder();
        trash.id = 2L;
        trash.name = context.getString(R.string.title_trash);
        folders.add(1, trash);

        EntityFolder archive = new EntityFolder();
        archive.id = 1L;
        archive.name = context.getString(R.string.title_archive);
        folders.add(1, archive);

        return folders;
    }

    static String getActionTitle(Context context, long id) {
        for (EntityFolder action : getFolderActions(context))
            if (action.id.equals(id))
                return action.name;

        return "???";
    }

    private static Long getAction(Context context, long selection, long account) {
        if (selection < 0)
            return selection;
        else if (selection == 0)
            return null;
        else {
            DB db = DB.getInstance(context);
            String type = (selection == 2 ? EntityFolder.TRASH : EntityFolder.ARCHIVE);
            EntityFolder folder = db.folder().getFolderByType(account, type);
            return (folder == null ? null : folder.id);
        }
    }
}
