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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class FragmentDialogUnblockAll extends FragmentDialogBase {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Context context = getContext();
        final View dview = LayoutInflater.from(context).inflate(R.layout.dialog_unblock, null);

        return new AlertDialog.Builder(context)
                .setView(dview)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new SimpleTask<Void>() {
                            @Override
                            protected Void onExecute(Context context, Bundle args) throws JSONException {
                                DB db = DB.getInstance(context);
                                int cleared = db.contact().clearContacts(null, new int[]{EntityContact.TYPE_JUNK});
                                EntityLog.log(context, "Unblocked senders=" + cleared);

                                List<EntityAccount> accounts = db.account().getSynchronizingAccounts(EntityAccount.TYPE_IMAP);
                                for (EntityAccount account : accounts) {
                                    EntityFolder inbox = db.folder().getFolderByType(account.id, EntityFolder.INBOX);
                                    EntityFolder junk = db.folder().getFolderByType(account.id, EntityFolder.JUNK);

                                    if (junk != null && junk.auto_classify_target) {
                                        db.folder().setFolderAutoClassify(junk.id, junk.auto_classify_source, false);
                                        EntityLog.log(context, "Disabled classification folder=" + account.name + ":" + junk.type);
                                    }

                                    if (inbox != null && junk != null) {
                                        List<EntityRule> rules = db.rule().getRules(inbox.id);
                                        for (EntityRule rule : rules) {
                                            JSONObject jaction = new JSONObject(rule.action);
                                            if (jaction.optInt("type") == EntityRule.TYPE_MOVE &&
                                                    jaction.optLong("target") == junk.id) {
                                                db.rule().setRuleEnabled(rule.id, false);
                                                EntityLog.log(context, "Disabled rule=" + rule.name);
                                            }
                                        }
                                    }
                                }

                                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                                SharedPreferences.Editor editor = prefs.edit();
                                for (String pref : new String[]{"check_blocklist", "auto_block_sender"})
                                    if (prefs.getBoolean(pref, false)) {
                                        editor.putBoolean(pref, false);
                                        EntityLog.log(context, "Disabled option=" + pref);
                                    }
                                editor.apply();

                                return null;
                            }

                            @Override
                            protected void onExecuted(Bundle args, Void data) {
                                ToastEx.makeText(getContext(), R.string.title_completed, Toast.LENGTH_LONG).show();
                            }

                            @Override
                            protected void onException(Bundle args, Throwable ex) {
                                Log.unexpectedError(getParentFragment(), ex);
                            }
                        }.execute(FragmentDialogUnblockAll.this, new Bundle(), "unblock");
                    }
                })
                .create();
    }
}
