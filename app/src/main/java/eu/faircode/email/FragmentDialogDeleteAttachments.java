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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class FragmentDialogDeleteAttachments extends FragmentDialogBase {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Context context = getContext();

        final View dview = LayoutInflater.from(context).inflate(R.layout.dialog_delete_attachments, null);
        final RecyclerView rvAttachment = dview.findViewById(R.id.rvAttachment);
        final ProgressBar pbWait = dview.findViewById(R.id.pbWait);

        rvAttachment.setHasFixedSize(false);
        rvAttachment.setLayoutManager(new LinearLayoutManager(context));

        AdapterAttachmentSelect adapter = new AdapterAttachmentSelect(context);
        rvAttachment.setAdapter(adapter);

        new SimpleTask<List<EntityAttachment>>() {
            @Override
            protected void onPreExecute(Bundle args) {
                pbWait.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onPostExecute(Bundle args) {
                pbWait.setVisibility(View.GONE);
            }

            @Override
            protected List<EntityAttachment> onExecute(Context context, Bundle args) {
                long id = args.getLong("id");

                DB db = DB.getInstance(context);
                return db.attachment().getAttachments(id);
            }

            @Override
            protected void onExecuted(final Bundle args, List<EntityAttachment> attachments) {
                if (attachments == null)
                    attachments = new ArrayList<>();
                for (EntityAttachment attachment : attachments)
                    if (attachment.encryption == null &&
                            (attachment.size == null || attachment.size > 0))
                        attachment.selected = true;
                adapter.set(attachments);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, getArguments(), "attachments:delete");

        return new AlertDialog.Builder(context)
                .setIcon(R.drawable.twotone_attachment_24)
                .setTitle(R.string.title_delete_attachments)
                .setView(dview)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Bundle args = new Bundle();
                        args.putLong("id", getArguments().getLong("id"));
                        args.putLongArray("ids", Helper.toLongArray(adapter.getSelectedItems()));

                        new SimpleTask<Void>() {
                            @Override
                            protected Void onExecute(Context context, Bundle args) throws Throwable {
                                long id = args.getLong("id");
                                long[] ids = args.getLongArray("ids");

                                DB db = DB.getInstance(context);
                                EntityMessage message = db.message().getMessage(id);
                                if (message == null)
                                    return null;

                                JSONArray jids = new JSONArray();
                                for (int i = 0; i < ids.length; i++)
                                    jids.put(i, ids[i]);

                                EntityOperation.queue(context, message, EntityOperation.DETACH, jids);

                                return null;
                            }

                            @Override
                            protected void onException(Bundle args, Throwable ex) {
                                Log.unexpectedError(getParentFragmentManager(), ex);
                            }
                        }.execute(FragmentDialogDeleteAttachments.this, args, "delete:attachments");
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    public static class AdapterAttachmentSelect extends RecyclerView.Adapter<AdapterAttachmentSelect.ViewHolder> {
        private Context context;
        private LayoutInflater inflater;

        private List<EntityAttachment> items = new ArrayList<>();

        public class ViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener {
            private CheckBox cbEnabled;
            private TextView tvInfo;

            ViewHolder(View itemView) {
                super(itemView);
                cbEnabled = itemView.findViewById(R.id.cbEnabled);
                tvInfo = itemView.findViewById(R.id.tvInfo);
            }

            private void wire() {
                cbEnabled.setOnCheckedChangeListener(this);
            }

            private void unwire() {
                cbEnabled.setOnCheckedChangeListener(null);
            }

            private void bindTo(EntityAttachment attachment) {
                cbEnabled.setText(attachment.name);
                cbEnabled.setChecked(attachment.selected);
                cbEnabled.setEnabled(attachment.encryption == null &&
                        (attachment.size == null || attachment.size > 0));

                StringBuilder sb = new StringBuilder();
                if (!TextUtils.isEmpty(attachment.type))
                    sb.append(attachment.type);
                if (attachment.size != null) {
                    if (sb.length() > 0)
                        sb.append(' ');
                    sb.append(Helper.humanReadableByteCount(attachment.size));
                }
                tvInfo.setText(sb.toString());
            }

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int pos = getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION)
                    return;

                items.get(pos).selected = isChecked;
            }
        }

        AdapterAttachmentSelect(Context context) {
            this.context = context;
            this.inflater = LayoutInflater.from(context);

            setHasStableIds(true);
        }

        public void set(List<EntityAttachment> attachments) {
            Log.i("Set attachments=" + attachments.size());

            DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffCallback(items, attachments), false);
            items = attachments;

            try {
                diff.dispatchUpdatesTo(this);
            } catch (Throwable ex) {
                Log.e(ex);
            }
        }

        List<Long> getSelectedItems() {
            List<Long> result = new ArrayList<>();
            for (EntityAttachment attachment : items)
                if (attachment.selected)
                    result.add(attachment.id);
            return result;
        }

        private class DiffCallback extends DiffUtil.Callback {
            private List<EntityAttachment> prev = new ArrayList<>();
            private List<EntityAttachment> next = new ArrayList<>();

            DiffCallback(List<EntityAttachment> prev, List<EntityAttachment> next) {
                this.prev.addAll(prev);
                this.next.addAll(next);
            }

            @Override
            public int getOldListSize() {
                return prev.size();
            }

            @Override
            public int getNewListSize() {
                return next.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                EntityAttachment a1 = prev.get(oldItemPosition);
                EntityAttachment a2 = next.get(newItemPosition);
                return a1.id.equals(a2.id);
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                EntityAttachment a1 = prev.get(oldItemPosition);
                EntityAttachment a2 = next.get(newItemPosition);
                return a1.equals(a2);
            }
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
        public AdapterAttachmentSelect.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new AdapterAttachmentSelect.ViewHolder(inflater.inflate(R.layout.item_attachment_delete, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull AdapterAttachmentSelect.ViewHolder holder, int position) {
            holder.unwire();
            EntityAttachment attachment = items.get(position);
            holder.bindTo(attachment);
            holder.wire();
        }
    }
}
