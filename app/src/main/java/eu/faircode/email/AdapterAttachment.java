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

    Copyright 2018-2020 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.mail.Part;

public class AdapterAttachment extends RecyclerView.Adapter<AdapterAttachment.ViewHolder> {
    private Fragment parentFragment;

    private Context context;
    private LifecycleOwner owner;
    private LayoutInflater inflater;

    private boolean readonly;
    private boolean debug;
    private int dp12;

    private List<EntityAttachment> items = new ArrayList<>();

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private View view;
        private ImageButton ibDelete;
        private ImageView ivType;
        private ImageView ivDisposition;
        private TextView tvName;
        private TextView tvSize;
        private ImageView ivStatus;
        private ImageButton ibSave;
        private TextView tvType;
        private TextView tvError;
        private ProgressBar progressbar;

        ViewHolder(View itemView) {
            super(itemView);

            view = itemView.findViewById(R.id.clItem);
            ibDelete = itemView.findViewById(R.id.ibDelete);
            ivType = itemView.findViewById(R.id.ivType);
            tvName = itemView.findViewById(R.id.tvName);
            tvSize = itemView.findViewById(R.id.tvSize);
            ivStatus = itemView.findViewById(R.id.ivStatus);
            ibSave = itemView.findViewById(R.id.ibSave);
            tvType = itemView.findViewById(R.id.tvType);
            ivDisposition = itemView.findViewById(R.id.ivDisposition);
            tvError = itemView.findViewById(R.id.tvError);
            progressbar = itemView.findViewById(R.id.progressbar);
        }

        private void wire() {
            view.setOnClickListener(this);
            ibDelete.setOnClickListener(this);
            ibSave.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        private void unwire() {
            view.setOnClickListener(null);
            ibDelete.setOnClickListener(null);
            ibSave.setOnClickListener(null);
            view.setOnLongClickListener(null);
        }

        private void bindTo(EntityAttachment attachment) {
            view.setAlpha(!attachment.isAttachment() ? Helper.LOW_LIGHT : 1.0f);

            ViewGroup.MarginLayoutParams lparam = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            lparam.setMarginStart(attachment.subsequence == null ? 0 : dp12);
            view.setLayoutParams(lparam);

            ibDelete.setVisibility(readonly ? View.GONE : View.VISIBLE);

            int resid = 0;
            String extension = Helper.guessExtension(attachment.getMimeType());
            if (extension != null)
                resid = context.getResources().getIdentifier("file_" + extension, "drawable", context.getPackageName());
            if (resid == 0)
                ivType.setImageDrawable(null);
            else
                ivType.setImageResource(resid);

            ivDisposition.setImageLevel(Part.INLINE.equals(attachment.disposition) ? 1 : 0);
            ivDisposition.setVisibility(
                    Part.ATTACHMENT.equals(attachment.disposition) ||
                            Part.INLINE.equals(attachment.disposition)
                            ? View.VISIBLE : View.INVISIBLE);

            tvName.setText(attachment.name);

            if (attachment.size != null)
                tvSize.setText(Helper.humanReadableByteCount(attachment.size));
            tvSize.setVisibility(attachment.size == null ? View.GONE : View.VISIBLE);

            if (attachment.available) {
                ivStatus.setImageResource(R.drawable.twotone_visibility_24);
                ivStatus.setVisibility(View.VISIBLE);
            } else {
                if (attachment.progress == null) {
                    ivStatus.setImageResource(R.drawable.twotone_cloud_download_24);
                    ivStatus.setVisibility(View.VISIBLE);
                } else
                    ivStatus.setVisibility(View.GONE);
            }

            ibSave.setVisibility(attachment.available ? View.VISIBLE : View.GONE);

            if (attachment.progress != null)
                progressbar.setProgress(attachment.progress);
            progressbar.setVisibility(
                    attachment.progress == null || attachment.available ? View.GONE : View.VISIBLE);

            StringBuilder sb = new StringBuilder();
            sb.append(attachment.type);
            if (debug || BuildConfig.DEBUG) {
                if (attachment.cid != null)
                    sb.append(' ').append(attachment.cid);
                if (attachment.isEncryption())
                    sb.append(' ').append(attachment.encryption);
            }
            tvType.setText(sb.toString());

            tvError.setText(attachment.error);
            tvError.setVisibility(attachment.error == null ? View.GONE : View.VISIBLE);
        }

        @Override
        public void onClick(View view) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return;

            EntityAttachment attachment = items.get(pos);
            if (attachment == null)
                return;

            if (view.getId() == R.id.ibDelete)
                onDelete(attachment);
            else if (view.getId() == R.id.ibSave)
                onSave(attachment);
            else {
                if (attachment.available)
                    onShare(attachment);
                else {
                    if (attachment.progress == null)
                        onDownload(attachment);
                }
            }
        }

        @Override
        public boolean onLongClick(View view) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return false;

            EntityAttachment attachment = items.get(pos);
            if (attachment == null || !attachment.available)
                return false;

            File file = attachment.getFile(context);
            Uri uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID, file);

            Intent send = new Intent();
            send.setAction(Intent.ACTION_SEND);
            send.putExtra(Intent.EXTRA_STREAM, uri);
            send.setType(attachment.getMimeType());
            send.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(Intent.createChooser(send, context.getString(R.string.title_select_app)));

            return true;
        }

        private void onDelete(final EntityAttachment attachment) {
            Bundle args = new Bundle();
            args.putLong("id", attachment.id);

            new SimpleTask<Void>() {
                @Override
                protected Void onExecute(Context context, Bundle args) {
                    long id = args.getLong("id");

                    DB db = DB.getInstance(context);
                    try {
                        db.beginTransaction();

                        EntityAttachment attachment = db.attachment().getAttachment(id);
                        if (attachment == null)
                            return null;

                        db.attachment().deleteAttachment(attachment.id);

                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }

                    attachment.getFile(context).delete();

                    return null;
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                }
            }.execute(context, owner, args, "attachment:delete");
        }

        private void onSave(EntityAttachment attachment) {
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
            lbm.sendBroadcast(
                    new Intent(FragmentMessages.ACTION_STORE_ATTACHMENT)
                            .putExtra("id", attachment.id)
                            .putExtra("name", Helper.sanitizeFilename(attachment.name))
                            .putExtra("type", attachment.getMimeType()));
        }

        private void onShare(EntityAttachment attachment) {
            Helper.share(context, attachment.getFile(context), attachment.getMimeType(), attachment.name);
        }

        private void onDownload(EntityAttachment attachment) {
            Bundle args = new Bundle();
            args.putLong("id", attachment.id);
            args.putLong("message", attachment.message);
            args.putInt("sequence", attachment.sequence);

            new SimpleTask<Void>() {
                @Override
                protected Void onExecute(Context context, Bundle args) {
                    long id = args.getLong("id");
                    long mid = args.getLong("message");

                    DB db = DB.getInstance(context);
                    try {
                        db.beginTransaction();

                        EntityMessage message = db.message().getMessage(mid);
                        if (message == null || message.uid == null)
                            return null;

                        EntityAttachment attachment = db.attachment().getAttachment(id);
                        if (attachment == null || attachment.progress != null || attachment.available)
                            return null;

                        EntityOperation.queue(context, message, EntityOperation.ATTACHMENT, id);

                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }

                    ServiceSynchronize.eval(context, "attachment");

                    return null;
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                }
            }.execute(context, owner, args, "attachment:fetch");
        }
    }

    AdapterAttachment(Fragment parentFragment, boolean readonly) {
        this.parentFragment = parentFragment;
        this.readonly = readonly;

        this.context = parentFragment.getContext();
        this.owner = parentFragment.getViewLifecycleOwner();
        this.inflater = LayoutInflater.from(context);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.debug = prefs.getBoolean("debug", false);
        this.dp12 = Helper.dp2pixels(context, 12);

        setHasStableIds(true);

        owner.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            public void onDestroyed() {
                Log.d(AdapterAttachment.this + " parent destroyed");
                AdapterAttachment.this.parentFragment = null;
            }
        });
    }

    public void set(@NonNull List<EntityAttachment> attachments) {
        Log.i("Set attachments=" + attachments.size());

        Collections.sort(attachments, new Comparator<EntityAttachment>() {
            @Override
            public int compare(EntityAttachment a1, EntityAttachment a2) {
                return a1.sequence.compareTo(a2.sequence);
            }
        });

        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffCallback(items, attachments), false);

        items = attachments;

        diff.dispatchUpdatesTo(new ListUpdateCallback() {
            @Override
            public void onInserted(int position, int count) {
                Log.d("Inserted @" + position + " #" + count);
            }

            @Override
            public void onRemoved(int position, int count) {
                Log.d("Removed @" + position + " #" + count);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                Log.d("Moved " + fromPosition + ">" + toPosition);
            }

            @Override
            public void onChanged(int position, int count, Object payload) {
                Log.d("Changed @" + position + " #" + count);
            }
        });
        diff.dispatchUpdatesTo(this);
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
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.item_attachment, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.unwire();

        EntityAttachment attachment = items.get(position);
        holder.bindTo(attachment);

        holder.wire();
    }
}
