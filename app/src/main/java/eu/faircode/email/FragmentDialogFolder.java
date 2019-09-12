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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import static android.app.Activity.RESULT_OK;

public class FragmentDialogFolder extends FragmentDialogBase {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final String title = getArguments().getString("title");
        final long account = getArguments().getLong("account");
        final long[] disabled = getArguments().getLongArray("disabled");

        final View dview = LayoutInflater.from(getContext()).inflate(R.layout.dialog_folder_select, null);
        final TextView tvNoFolder = dview.findViewById(R.id.tvNoFolder);
        final RecyclerView rvFolder = dview.findViewById(R.id.rvFolder);
        final ContentLoadingProgressBar pbWait = dview.findViewById(R.id.pbWait);

        rvFolder.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rvFolder.setLayoutManager(llm);

        final AdapterFolder adapter = new AdapterFolder(getContext(), getViewLifecycleOwner(),
                account, false, new AdapterFolder.IFolderSelectedListener() {
            @Override
            public void onFolderSelected(TupleFolderEx folder) {
                Bundle args = getArguments();
                args.putLong("folder", folder.id);

                sendResult(RESULT_OK);
                dismiss();
            }
        });

        rvFolder.setAdapter(adapter);

        Bundle args = new Bundle();
        args.putLong("account", account);

        new SimpleTask<List<TupleFolderEx>>() {
            @Override
            protected void onPreExecute(Bundle args) {
                tvNoFolder.setVisibility(View.GONE);
                rvFolder.setVisibility(View.GONE);
                pbWait.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onPostExecute(Bundle args) {
                pbWait.setVisibility(View.GONE);
            }

            @Override
            protected List<TupleFolderEx> onExecute(Context context, Bundle args) {
                long account = args.getLong("account");

                DB db = DB.getInstance(context);
                return db.folder().getFoldersEx(account);
            }

            @Override
            protected void onExecuted(final Bundle args, List<TupleFolderEx> folders) {
                if (folders == null || folders.size() == 0)
                    tvNoFolder.setVisibility(View.VISIBLE);
                else {
                    adapter.setDisabled(Helper.fromLongArray(disabled));
                    adapter.set(folders);
                    rvFolder.setVisibility(View.VISIBLE);
                }
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(getFragmentManager(), ex);
            }
        }.execute(this, args, "folder:select");

        return new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setView(dview)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }
}
