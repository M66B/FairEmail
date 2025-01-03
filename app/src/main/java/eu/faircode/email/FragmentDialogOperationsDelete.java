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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;

public class FragmentDialogOperationsDelete extends FragmentDialogBase {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Context context = getContext();
        final View dview = LayoutInflater.from(context).inflate(R.layout.dialog_delete_operations, null);
        final CheckBox cbError = dview.findViewById(R.id.cbError);
        final CheckBox cbFetch = dview.findViewById(R.id.cbFetch);
        final CheckBox cbMove = dview.findViewById(R.id.cbMove);
        final CheckBox cbFlag = dview.findViewById(R.id.cbFlag);
        final CheckBox cbDelete = dview.findViewById(R.id.cbDelete);
        final CheckBox cbSend = dview.findViewById(R.id.cbSend);

        return new AlertDialog.Builder(context)
                .setView(dview)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Bundle args = new Bundle();
                        args.putBoolean("error", cbError.isChecked());
                        args.putBoolean("fetch", cbFetch.isChecked());
                        args.putBoolean("move", cbMove.isChecked());
                        args.putBoolean("flag", cbFlag.isChecked());
                        args.putBoolean("delete", cbDelete.isChecked());
                        args.putBoolean("send", cbSend.isChecked());

                        new SimpleTask<Integer>() {
                            private Toast toast = null;

                            @Override
                            protected void onPostExecute(Bundle args) {
                                toast = ToastEx.makeText(context, R.string.title_executing, Toast.LENGTH_LONG);
                                toast.show();
                            }

                            @Override
                            protected void onPreExecute(Bundle args) {
                                if (toast != null)
                                    toast.cancel();
                            }

                            @Override
                            protected Integer onExecute(Context context, Bundle args) {
                                boolean error = args.getBoolean("error");
                                boolean fetch = args.getBoolean("fetch");
                                boolean move = args.getBoolean("move");
                                boolean flag = args.getBoolean("flag");
                                boolean delete = args.getBoolean("delete");
                                boolean send = args.getBoolean("send");

                                int deleted = 0;
                                DB db = DB.getInstance(context);
                                try {
                                    db.beginTransaction();

                                    List<EntityOperation> ops = new ArrayList<>();

                                    // ADD, SEND, EXISTS, SUBSCRIBE

                                    if (error)
                                        addAll(ops, db.operation().getOperationsError());

                                    if (fetch) {
                                        addAll(ops, db.operation().getOperations(EntityOperation.FETCH));
                                        addAll(ops, db.operation().getOperations(EntityOperation.DOWNLOAD));
                                        addAll(ops, db.operation().getOperations(EntityOperation.RAW));
                                        addAll(ops, db.operation().getOperations(EntityOperation.BODY));
                                        addAll(ops, db.operation().getOperations(EntityOperation.ATTACHMENT));
                                        addAll(ops, db.operation().getOperations(EntityOperation.HEADERS));
                                        addAll(ops, db.operation().getOperations(EntityOperation.RULE));
                                        addAll(ops, db.operation().getOperations(EntityOperation.SYNC));
                                    }

                                    if (move) {
                                        addAll(ops, db.operation().getOperations(EntityOperation.MOVE));
                                        addAll(ops, db.operation().getOperations(EntityOperation.COPY));
                                    }

                                    if (flag) {
                                        addAll(ops, db.operation().getOperations(EntityOperation.SEEN));
                                        addAll(ops, db.operation().getOperations(EntityOperation.ANSWERED));
                                        addAll(ops, db.operation().getOperations(EntityOperation.FLAG));
                                        addAll(ops, db.operation().getOperations(EntityOperation.KEYWORD));
                                        addAll(ops, db.operation().getOperations(EntityOperation.LABEL));
                                        addAll(ops, db.operation().getOperations(EntityOperation.REPORT));
                                    }

                                    if (delete) {
                                        addAll(ops, db.operation().getOperations(EntityOperation.DELETE));
                                        addAll(ops, db.operation().getOperations(EntityOperation.PURGE));
                                        addAll(ops, db.operation().getOperations(EntityOperation.EXPUNGE));
                                        addAll(ops, db.operation().getOperations(EntityOperation.DETACH));
                                    }

                                    for (EntityOperation op : ops) {
                                        EntityLog.log(context, "Deleting operation=" + op.id + ":" + op.name + " error=" + op.error);

                                        if (db.operation().deleteOperation(op.id) > 0) {
                                            op.cleanup(context, false);
                                            deleted++;
                                        }

                                        if (EntityOperation.SYNC.equals(op.name))
                                            db.folder().setFolderSyncState(op.folder, null);
                                    }

                                    db.setTransactionSuccessful();
                                } finally {
                                    db.endTransaction();
                                }

                                if (send) {
                                    List<EntityOperation> ops = db.operation().getOperations(EntityOperation.SEND);
                                    for (EntityOperation op : ops) {
                                        ActivityCompose.undoSend(op.message, context);
                                        deleted++;
                                    }
                                }

                                if (deleted > 0)
                                    ServiceSynchronize.reload(context, null, true, "deleted operations");

                                return deleted;
                            }

                            @Override
                            protected void onExecuted(Bundle args, Integer deleted) {
                                if (deleted == null)
                                    deleted = -1;
                                Context context = getContext();
                                if (context == null)
                                    return;
                                ToastEx.makeText(
                                        context,
                                        getString(R.string.title_delete_operation_deleted, deleted),
                                        Toast.LENGTH_LONG).show();
                            }

                            @Override
                            protected void onException(Bundle args, Throwable ex) {
                                Log.unexpectedError(getParentFragmentManager(), ex);
                            }

                            private void addAll(List<EntityOperation> list, List<EntityOperation> sublist) {
                                if (sublist != null)
                                    list.addAll(sublist);
                            }
                        }.execute(context, getActivity(), args, "operations:delete");
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }
}
