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

    Copyright 2018-2019 by Marcel Bokhorst (M66B)
*/

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class DialogFolder {
    static void show(
            final Context context, final LifecycleOwner owner, View parentView, int title,
            long account, final List<Long> disabled,
            final IDialogFolder intf) {
        final View dview = LayoutInflater.from(context).inflate(R.layout.dialog_folder_select, null);
        final RecyclerView rvFolder = dview.findViewById(R.id.rvFolder);
        final ContentLoadingProgressBar pbWait = dview.findViewById(R.id.pbWait);

        final Dialog dialog = new DialogBuilderLifecycle(context, owner)
                .setTitle(title)
                .setView(dview)
                .create();

        rvFolder.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(context);
        rvFolder.setLayoutManager(llm);

        final AdapterFolder adapter = new AdapterFolder(context, owner, parentView, account, false,
                new AdapterFolder.IFolderSelectedListener() {
                    @Override
                    public void onFolderSelected(TupleFolderEx folder) {
                        dialog.dismiss();
                        intf.onFolderSelected(folder);
                    }
                });

        rvFolder.setAdapter(adapter);

        rvFolder.setVisibility(View.GONE);
        pbWait.setVisibility(View.VISIBLE);
        dialog.show();

        Bundle args = new Bundle();
        args.putLong("account", account);

        new SimpleTask<List<TupleFolderEx>>() {
            @Override
            protected List<TupleFolderEx> onExecute(Context context, Bundle args) {
                long account = args.getLong("account");

                DB db = DB.getInstance(context);
                return db.folder().getFoldersEx(account);
            }

            @Override
            protected void onExecuted(final Bundle args, List<TupleFolderEx> folders) {
                if (folders == null)
                    folders = new ArrayList<>();

                adapter.setDisabled(disabled);
                adapter.set(folders);
                pbWait.setVisibility(View.GONE);
                rvFolder.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(context, owner, ex);
            }
        }.execute(context, owner, args, "folder:select");
    }

    interface IDialogFolder {
        void onFolderSelected(TupleFolderEx folder);
    }
}
