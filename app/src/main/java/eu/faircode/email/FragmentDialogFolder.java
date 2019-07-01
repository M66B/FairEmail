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
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class FragmentDialogFolder extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        String title = getArguments().getString("title");

        final View dview = LayoutInflater.from(getContext()).inflate(R.layout.dialog_folder_select, null);
        final RecyclerView rvFolder = dview.findViewById(R.id.rvFolder);
        final ContentLoadingProgressBar pbWait = dview.findViewById(R.id.pbWait);

        rvFolder.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rvFolder.setLayoutManager(llm);

        rvFolder.setVisibility(View.GONE);
        pbWait.setVisibility(View.VISIBLE);

        Bundle args = new Bundle();
        args.putLong("account", getArguments().getLong("account"));

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

                long account = args.getLong("account");
                AdapterFolder adapter = new AdapterFolder(
                        getContext(), getActivity(),
                        account, false, new AdapterFolder.IFolderSelectedListener() {
                    @Override
                    public void onFolderSelected(TupleFolderEx folder) {
                        dismiss();
                        sendResult(RESULT_OK, folder.id);
                    }
                });

                rvFolder.setAdapter(adapter);

                adapter.setDisabled(Helper.fromLongArray(getArguments().getLongArray("disabled")));
                adapter.set(folders);

                pbWait.setVisibility(View.GONE);
                rvFolder.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(getFragmentManager(), ex);
            }
        }.execute(getContext(), getActivity(), args, "folder:select");

        return new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setView(dview)
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        sendResult(RESULT_CANCELED, -1);
                    }
                })
                .create();
    }

    private void sendResult(int result, long folder) {
        Bundle args = getArguments();
        args.putLong("folder", folder);

        Fragment target = getTargetFragment();
        if (target != null) {
            Intent data = new Intent();
            data.putExtra("args", args);
            target.onActivityResult(getTargetRequestCode(), result, data);
        }
    }
}
