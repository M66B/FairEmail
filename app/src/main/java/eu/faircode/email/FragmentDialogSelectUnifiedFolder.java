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

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.Group;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FragmentDialogSelectUnifiedFolder extends FragmentDialogBase {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Context context = getContext();

        final View dview = LayoutInflater.from(context).inflate(R.layout.dialog_account_select, null);
        RecyclerView rvSelect = dview.findViewById(R.id.rvSelect);
        final ContentLoadingProgressBar pbWait = dview.findViewById(R.id.pbWait);
        final Group grpReady = dview.findViewById(R.id.grpReady);

        rvSelect.setHasFixedSize(false);
        rvSelect.setLayoutManager(new LinearLayoutManager(context));

        Dialog dialog = new AlertDialog.Builder(context)
                .setIcon(R.drawable.twotone_folder_open_24)
                .setTitle(R.string.title_folders_unified)
                .setView(dview)
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        new SimpleTask<List<TupleFolderEx>>() {
            @Override
            protected void onPreExecute(Bundle args) {
                pbWait.setVisibility(View.VISIBLE);
                grpReady.setVisibility(View.GONE);
            }

            @Override
            protected void onPostExecute(Bundle args) {
                pbWait.setVisibility(View.GONE);
                grpReady.setVisibility(View.VISIBLE);
            }

            @Override
            protected List<TupleFolderEx> onExecute(Context context, Bundle args) {
                DB db = DB.getInstance(context);
                return db.folder().getUnified(null, null);
            }

            @Override
            protected void onExecuted(Bundle args, List<TupleFolderEx> folders) {
                if (folders == null)
                    folders = new ArrayList<>();
                else if (folders.size() > 0)
                    Collections.sort(folders, folders.get(0).getComparator(context));

                TupleFolderEx unified = new TupleFolderEx();
                unified.id = -1L;
                unified.name = context.getString(R.string.title_folder_unified);
                folders.add(0, unified);

                AdapterFolder adapter = new AdapterFolder(context, folders, new AdapterFolder.IListener() {
                    @Override
                    public void onSelected(TupleFolderEx folder) {
                        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                        if (folder.id < 0)
                            lbm.sendBroadcast(
                                    new Intent(ActivityView.ACTION_VIEW_MESSAGES)
                                            .putExtra("type", (String) null)
                                            .putExtra("unified", true));
                        else
                            lbm.sendBroadcast(
                                    new Intent(ActivityView.ACTION_VIEW_MESSAGES)
                                            .putExtra("account", folder.account)
                                            .putExtra("folder", folder.id)
                                            .putExtra("type", folder.type));

                        sendResult(RESULT_OK);
                        dialog.dismiss();
                    }
                });

                rvSelect.setAdapter(adapter);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, new Bundle(), "select:folder");

        return dialog;
    }

    public static class AdapterFolder extends RecyclerView.Adapter<AdapterFolder.ViewHolder> {
        private Context context;
        private LayoutInflater inflater;

        private int dp6;
        private int dp12;
        private List<TupleFolderEx> items;
        private IListener listener;

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private View vwColor;
            private TextView tv;

            ViewHolder(View itemView) {
                super(itemView);
                vwColor = itemView.findViewById(R.id.vwColor);
                tv = itemView.findViewById(android.R.id.text1);
            }

            private void wire() {
                itemView.setOnClickListener(this);
            }

            private void unwire() {
                itemView.setOnClickListener(null);
            }

            private void bindTo(TupleFolderEx folder) {
                int vpad = (getItemCount() > 10 ? dp6 : dp12);
                tv.setPadding(0, vpad, 0, vpad);

                vwColor.setBackgroundColor(folder.color == null ? Color.TRANSPARENT : folder.color);
                if (folder.accountName == null)
                    tv.setText(folder.getDisplayName(context));
                else
                    tv.setText(folder.accountName + "/" + folder.getDisplayName(context));
            }

            @Override
            public void onClick(View v) {
                int pos = getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION)
                    return;

                TupleFolderEx folder = items.get(pos);
                listener.onSelected(folder);
            }
        }

        AdapterFolder(Context context, List<TupleFolderEx> folders, IListener listener) {
            this.context = context;
            this.inflater = LayoutInflater.from(context);
            this.dp6 = Helper.dp2pixels(context, 6);
            this.dp12 = Helper.dp2pixels(context, 12);

            setHasStableIds(true);
            this.items = folders;
            this.listener = listener;
        }

        @Override
        public long getItemId(int position) {
            return items.get(position).id;
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        @NonNull
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(inflater.inflate(R.layout.item_account_select, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.unwire();
            TupleFolderEx folder = items.get(position);
            holder.bindTo(folder);
            holder.wire();
        }

        public interface IListener {
            void onSelected(TupleFolderEx folder);
        }
    }
}
