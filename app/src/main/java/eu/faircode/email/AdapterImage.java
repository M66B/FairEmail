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

    Copyright 2018-2022 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimatedImageDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AdapterImage extends RecyclerView.Adapter<AdapterImage.ViewHolder> {
    private Fragment parentFragment;
    private Context context;
    private LayoutInflater inflater;
    private LifecycleOwner owner;

    private List<EntityAttachment> items = new ArrayList<>();

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private View view;
        private ImageView ivImage;
        private TextView tvCaption;
        private TextView tvProperties;

        ViewHolder(View itemView) {
            super(itemView);

            view = itemView.findViewById(R.id.clItem);
            ivImage = itemView.findViewById(R.id.ivImage);
            tvCaption = itemView.findViewById(R.id.tvCaption);
            tvProperties = itemView.findViewById(R.id.tvProperties);
        }

        private void wire() {
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        private void unwire() {
            view.setOnClickListener(null);
            view.setOnLongClickListener(null);
        }

        private void bindTo(EntityAttachment attachment) {
            tvCaption.setText(attachment.name);
            tvCaption.setVisibility(TextUtils.isEmpty(attachment.name) ? View.GONE : View.VISIBLE);
            tvProperties.setVisibility(View.GONE);

            if (attachment.available) {
                Bundle args = new Bundle();
                args.putSerializable("file", attachment.getFile(context));
                args.putString("type", attachment.getMimeType());
                args.putInt("max", context.getResources().getDisplayMetrics().widthPixels);

                new SimpleTask<Drawable>() {
                    @Override
                    protected void onPreExecute(Bundle args) {
                        ivImage.setImageResource(R.drawable.twotone_hourglass_top_24);
                    }

                    @Override
                    protected Drawable onExecute(Context context, Bundle args) throws Throwable {
                        File file = (File) args.getSerializable("file");
                        String type = args.getString("type");
                        int max = args.getInt("max");

                        args.putLong("size", file.length());

                        try {
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inJustDecodeBounds = true;
                            BitmapFactory.decodeFile(file.getAbsolutePath(), options);
                            args.putInt("width", options.outWidth);
                            args.putInt("height", options.outHeight);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                if (options.outColorSpace != null)
                                    args.putString("color", options.outColorSpace.getModel().name());
                                if (options.outConfig != null)
                                    args.putString("config", options.outConfig.name());
                            }
                        } catch (Throwable ex) {
                            Log.w(ex);
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
                                !"image/svg+xml".equals(type) &&
                                !"svg".equals(Helper.getExtension(file.getName())))
                            try {
                                return ImageHelper.getScaledDrawable(context, file, type, max);
                            } catch (Throwable ex) {
                                Log.w(ex);
                            }

                        Bitmap bm = ImageHelper.decodeImage(file, type, max);
                        if (bm == null)
                            return null;
                        return new BitmapDrawable(context.getResources(), bm);
                    }

                    @Override
                    protected void onExecuted(Bundle args, Drawable image) {
                        if (image == null)
                            ivImage.setImageResource(R.drawable.twotone_broken_image_24);
                        else
                            ivImage.setImageDrawable(image);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
                                image instanceof AnimatedImageDrawable)
                            ((AnimatedImageDrawable) image).start();

                        StringBuilder sb = new StringBuilder();

                        int width = args.getInt("width");
                        int height = args.getInt("height");
                        if (width > 0 && height > 0)
                            sb.append(width)
                                    .append("\u00d7") // ×
                                    .append(height);

                        if (BuildConfig.DEBUG) {
                            String color = args.getString("color");
                            if (color != null) {
                                if (sb.length() > 0)
                                    sb.append(' ');
                                sb.append(color);
                            }

                            String config = args.getString("config");
                            if (config != null) {
                                if (sb.length() > 0)
                                    sb.append(' ');
                                sb.append(config);
                            }
                        }

                        long size = args.getLong("size");
                        if (size > 0) {
                            if (sb.length() > 0)
                                sb.append(" \u2013 "); // –
                            sb.append(Helper.humanReadableByteCount(size));
                        }

                        if (sb.length() > 0) {
                            tvProperties.setText(sb);
                            tvProperties.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        tvCaption.setText(Log.formatThrowable(ex));
                        tvCaption.setVisibility(View.VISIBLE);
                        ivImage.setImageResource(R.drawable.twotone_broken_image_24);
                    }
                }.execute(context, owner, args, "image:load");
            } else
                ivImage.setImageResource(attachment.progress == null
                        ? R.drawable.twotone_image_24 : R.drawable.twotone_hourglass_top_24);
        }

        @Override
        public void onClick(View view) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return;

            EntityAttachment attachment = items.get(pos);
            if (attachment.available)
                Helper.share(context, attachment.getFile(context), attachment.getMimeType(), attachment.name);
            else {
                if (attachment.progress == null) {
                    Bundle args = new Bundle();
                    args.putLong("id", attachment.id);
                    args.putLong("message", attachment.message);

                    new SimpleTask<Void>() {
                        @Override
                        protected Void onExecute(Context context, Bundle args) {
                            long id = args.getLong("id");
                            long mid = args.getLong("message");

                            Long reload = null;

                            DB db = DB.getInstance(context);
                            try {
                                db.beginTransaction();

                                EntityMessage message = db.message().getMessage(mid);
                                if (message == null || message.uid == null)
                                    return null;

                                EntityAccount account = db.account().getAccount(message.account);
                                if (account == null)
                                    return null;

                                if (!"connected".equals(account.state) && !account.isTransient(context))
                                    reload = account.id;

                                EntityAttachment attachment = db.attachment().getAttachment(id);
                                if (attachment == null || attachment.progress != null || attachment.available)
                                    return null;

                                EntityOperation.queue(context, message, EntityOperation.ATTACHMENT, id);

                                db.setTransactionSuccessful();
                            } finally {
                                db.endTransaction();
                            }

                            if (reload == null)
                                ServiceSynchronize.eval(context, "image");
                            else
                                ServiceSynchronize.reload(context, reload, true, "image");

                            return null;
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                        }
                    }.execute(context, owner, args, "image:fetch");
                }
            }
        }

        @Override
        public boolean onLongClick(View v) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return false;

            EntityAttachment attachment = items.get(pos);
            if (!attachment.available)
                return false;

            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
            lbm.sendBroadcast(
                    new Intent(FragmentBase.ACTION_STORE_ATTACHMENT)
                            .putExtra("id", attachment.id)
                            .putExtra("name", Helper.sanitizeFilename(attachment.name))
                            .putExtra("type", attachment.getMimeType()));
            return true;
        }
    }

    AdapterImage(Fragment parentFragment) {
        this.parentFragment = parentFragment;
        this.context = parentFragment.getContext();
        this.owner = parentFragment.getViewLifecycleOwner();
        this.inflater = LayoutInflater.from(context);

        setHasStableIds(true);

        owner.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            public void onDestroyed() {
                Log.d(AdapterImage.this + " parent destroyed");
                AdapterImage.this.parentFragment = null;
                owner.getLifecycle().removeObserver(this);
            }
        });
    }

    public void set(@NonNull List<EntityAttachment> attachments) {
        Log.i("Set images=" + attachments.size());

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

        try {
            diff.dispatchUpdatesTo(this);
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    private static class DiffCallback extends DiffUtil.Callback {
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
        return new ViewHolder(inflater.inflate(R.layout.item_image, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.unwire();

        EntityAttachment attachment = items.get(position);
        holder.bindTo(attachment);

        holder.wire();
    }

    @Override
    public void onViewRecycled(@NonNull AdapterImage.ViewHolder holder) {
        holder.ivImage.setImageDrawable(null);
    }
}
