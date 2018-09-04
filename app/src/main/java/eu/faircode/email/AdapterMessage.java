package eu.faircode.email;

/*
    This file is part of FairEmail.

    FairEmail is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    NetGuard is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with NetGuard.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2018 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterMessage extends PagedListAdapter<TupleMessageEx, AdapterMessage.ViewHolder> {
    private Context context;
    private LifecycleOwner owner;
    private ViewType viewType;

    private boolean debug;
    private DateFormat df = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.LONG);

    enum ViewType {UNIFIED, FOLDER, THREAD, SEARCH}

    public class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {
        View itemView;
        TextView tvFrom;
        TextView tvTime;
        ImageView ivAttachments;
        TextView tvSubject;
        TextView tvFolder;
        TextView tvCount;
        ImageView ivThread;
        TextView tvError;
        ProgressBar pbLoading;

        ViewHolder(View itemView) {
            super(itemView);

            this.itemView = itemView;
            tvFrom = itemView.findViewById(R.id.tvFrom);
            tvTime = itemView.findViewById(R.id.tvTime);
            ivAttachments = itemView.findViewById(R.id.ivAttachments);
            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvFolder = itemView.findViewById(R.id.tvFolder);
            tvCount = itemView.findViewById(R.id.tvCount);
            ivThread = itemView.findViewById(R.id.ivThread);
            tvError = itemView.findViewById(R.id.tvError);
            pbLoading = itemView.findViewById(R.id.pbLoading);
        }

        private void wire() {
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        private void unwire() {
            itemView.setOnClickListener(null);
            itemView.setOnLongClickListener(null);
        }

        private void clear() {
            tvFrom.setText(null);
            tvTime.setText(null);
            tvSubject.setText(null);
            ivAttachments.setVisibility(View.GONE);
            tvFolder.setText(null);
            tvCount.setText(null);
            ivThread.setVisibility(View.GONE);
            tvError.setText(null);
            pbLoading.setVisibility(View.VISIBLE);
        }

        private void bindTo(final TupleMessageEx message) {
            pbLoading.setVisibility(View.GONE);

            if (EntityFolder.DRAFTS.equals(message.folderType) ||
                    EntityFolder.OUTBOX.equals(message.folderType) ||
                    EntityFolder.SENT.equals(message.folderType)) {
                tvFrom.setText(MessageHelper.getFormattedAddresses(message.to, false));
                tvTime.setText(DateUtils.getRelativeTimeSpanString(context, message.sent == null ? message.received : message.sent));
            } else {
                tvFrom.setText(MessageHelper.getFormattedAddresses(message.from, false));
                tvTime.setText(DateUtils.getRelativeTimeSpanString(context, message.received));
            }

            tvSubject.setText(message.subject);
            ivAttachments.setVisibility(message.attachments > 0 ? View.VISIBLE : View.GONE);

            if (viewType == ViewType.UNIFIED)
                tvFolder.setText(message.accountName);
            else if (viewType == ViewType.FOLDER)
                tvFolder.setVisibility(View.GONE);
            else
                tvFolder.setText(Helper.localizeFolderName(context, message.folderName));

            if (viewType == ViewType.THREAD) {
                tvCount.setVisibility(View.GONE);
                ivThread.setVisibility(View.GONE);
            } else {
                tvCount.setText(Integer.toString(message.count));
                ivThread.setVisibility(View.VISIBLE);
            }

            if (debug) {
                DB db = DB.getInstance(context);
                db.operation().getOperationsByMessage(message.id).removeObservers(owner);
                db.operation().getOperationsByMessage(message.id).observe(owner, new Observer<List<EntityOperation>>() {
                    @Override
                    public void onChanged(List<EntityOperation> operations) {
                        String text = message.error +
                                "\n" + message.id + " " + df.format(new Date(message.received)) +
                                "\n" + (message.ui_hide ? "HIDDEN " : "") +
                                "seen=" + message.seen + "/" + message.ui_seen + "/" + message.unseen +
                                " " + message.uid + "/" + message.id +
                                "\n" + message.msgid;
                        if (operations != null)
                            for (EntityOperation op : operations)
                                text += "\n" + op.id + ":" + op.name + " " + df.format(new Date(op.created));

                        tvError.setText(text);
                        tvError.setVisibility(View.VISIBLE);

                    }
                });
            }

            tvError.setText(message.error);
            tvError.setVisibility(message.error == null ? View.GONE : View.VISIBLE);

            int typeface = (message.unseen > 0 ? Typeface.BOLD : Typeface.NORMAL);
            tvFrom.setTypeface(null, typeface);
            tvTime.setTypeface(null, typeface);
            tvSubject.setTypeface(null, typeface);
            tvCount.setTypeface(null, typeface);

            int colorUnseen = Helper.resolveColor(context, message.unseen > 0
                    ? R.attr.colorUnread : android.R.attr.textColorSecondary);
            tvFrom.setTextColor(colorUnseen);
            tvTime.setTextColor(colorUnseen);
        }

        @Override
        public void onClick(View view) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return;
            TupleMessageEx message = getItem(pos);

            if (EntityFolder.DRAFTS.equals(message.folderType))
                context.startActivity(
                        new Intent(context, ActivityCompose.class)
                                .putExtra("action", "edit")
                                .putExtra("id", message.id));
            else {
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                lbm.sendBroadcast(
                        new Intent(ActivityView.ACTION_VIEW_MESSAGE)
                                .putExtra("message", message));
            }
        }

        @Override
        public boolean onLongClick(View view) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return false;

            TupleMessageEx message = getItem(pos);

            Bundle args = new Bundle();
            args.putLong("id", message.id);

            new SimpleTask<Void>() {
                @Override
                protected Void onLoad(Context context, Bundle args) {
                    long id = args.getLong("id");
                    DB db = DB.getInstance(context);
                    try {
                        db.beginTransaction();

                        EntityMessage message = db.message().getMessage(id);
                        for (EntityMessage tmessage : db.message().getMessageByThread(message.account, message.thread)) {
                            db.message().setMessageUiSeen(tmessage.id, !message.ui_seen);
                            EntityOperation.queue(db, tmessage, EntityOperation.SEEN, !tmessage.ui_seen);
                        }

                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }

                    EntityOperation.process(context);

                    return null;
                }

                @Override
                public void onException(Bundle args, Throwable ex) {
                    Toast.makeText(context, ex.toString(), Toast.LENGTH_LONG).show();
                }
            }.load(context, owner, args);

            return true;
        }
    }

    AdapterMessage(Context context, LifecycleOwner owner, ViewType viewType) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.owner = owner;
        this.viewType = viewType;
        this.debug = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("debug", false);
    }

    private static final DiffUtil.ItemCallback<TupleMessageEx> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<TupleMessageEx>() {
                @Override
                public boolean areItemsTheSame(
                        @NonNull TupleMessageEx prev, @NonNull TupleMessageEx next) {
                    return prev.id.equals(next.id);
                }

                @Override
                public boolean areContentsTheSame(
                        @NonNull TupleMessageEx prev, @NonNull TupleMessageEx next) {
                    return prev.equals(next);
                }
            };

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_message, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.unwire();

        TupleMessageEx message = getItem(position);
        if (message == null)
            holder.clear();
        else {
            holder.bindTo(message);
            holder.wire();
        }
    }
}
