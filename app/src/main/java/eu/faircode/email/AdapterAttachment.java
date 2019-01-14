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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.lifecycle.LifecycleOwner;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterAttachment extends RecyclerView.Adapter<AdapterAttachment.ViewHolder> {
    private Context context;
    private LayoutInflater inflater;
    private LifecycleOwner owner;

    private boolean readonly;
    private boolean confirm;
    private boolean debug;

    private List<EntityAttachment> all = new ArrayList<>();
    private List<EntityAttachment> filtered = new ArrayList<>();

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View itemView;
        ImageView ivDelete;
        TextView tvName;
        TextView tvType;
        TextView tvSize;
        ImageView ivStatus;
        ImageView ivSave;
        TextView tvDebug;
        ProgressBar progressbar;

        ViewHolder(View itemView) {
            super(itemView);

            this.itemView = itemView;
            ivDelete = itemView.findViewById(R.id.ivDelete);
            tvName = itemView.findViewById(R.id.tvName);
            tvType = itemView.findViewById(R.id.tvType);
            tvSize = itemView.findViewById(R.id.tvSize);
            ivStatus = itemView.findViewById(R.id.ivStatus);
            ivSave = itemView.findViewById(R.id.ivSave);
            tvDebug = itemView.findViewById(R.id.tvDebug);
            progressbar = itemView.findViewById(R.id.progressbar);
        }

        private void wire() {
            itemView.setOnClickListener(this);
            ivDelete.setOnClickListener(this);
            ivSave.setOnClickListener(this);
        }

        private void unwire() {
            itemView.setOnClickListener(null);
            ivDelete.setOnClickListener(null);
            ivSave.setOnClickListener(null);
        }

        private void bindTo(EntityAttachment attachment) {
            itemView.setAlpha(attachment.isInline() ? 0.6f : 1.0f);

            ivDelete.setVisibility(readonly ? View.GONE : attachment.isInline() ? View.INVISIBLE : View.VISIBLE);
            tvName.setText(attachment.name);
            tvType.setText(attachment.type);

            if (attachment.size != null)
                tvSize.setText(Helper.humanReadableByteCount(attachment.size, true));
            tvSize.setVisibility(attachment.size == null ? View.GONE : View.VISIBLE);

            if (attachment.available) {
                ivStatus.setImageResource(R.drawable.baseline_visibility_24);
                ivStatus.setVisibility(View.VISIBLE);
            } else {
                if (attachment.progress == null) {
                    ivStatus.setImageResource(R.drawable.baseline_cloud_download_24);
                    ivStatus.setVisibility(View.VISIBLE);
                } else
                    ivStatus.setVisibility(View.GONE);
            }

            ivSave.setVisibility(readonly && attachment.available ? View.VISIBLE : View.GONE);

            if (attachment.progress != null)
                progressbar.setProgress(attachment.progress);
            progressbar.setVisibility(
                    attachment.progress == null || attachment.available ? View.GONE : View.VISIBLE);

            tvDebug.setText(attachment.type + " " + attachment.disposition + " " + attachment.cid + " " + attachment.encryption);
            tvDebug.setVisibility(debug ? View.VISIBLE : View.GONE);
        }

        @Override
        public void onClick(View view) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return;
            final EntityAttachment attachment = filtered.get(pos);

            if (view.getId() == R.id.ivDelete) {
                Bundle args = new Bundle();
                args.putLong("id", attachment.id);

                new SimpleTask<Void>() {
                    @Override
                    protected Void onExecute(Context context, Bundle args) {
                        DB.getInstance(context).attachment().deleteAttachment(attachment.id);
                        EntityAttachment.getFile(context, attachment.id).delete();
                        return null;
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Helper.unexpectedError(context, owner, ex);
                    }
                }.execute(context, owner, args, "attachment:delete");

            } else if (view.getId() == R.id.ivSave) {
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                lbm.sendBroadcast(
                        new Intent(ActivityView.ACTION_STORE_ATTACHMENT)
                                .putExtra("id", attachment.id)
                                .putExtra("name", attachment.name)
                                .putExtra("type", attachment.type));

            } else {
                if (attachment.available) {
                    // Build file name
                    File file = EntityAttachment.getFile(context, attachment.id);

                    // https://developer.android.com/reference/android/support/v4/content/FileProvider
                    final Uri uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID, file);
                    Log.i("uri=" + uri);

                    // Build intent
                    final Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(uri, attachment.type);
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    if (!TextUtils.isEmpty(attachment.name))
                        intent.putExtra(Intent.EXTRA_TITLE, attachment.name);
                    Log.i("Sharing " + file + " type=" + attachment.type);
                    Log.i("Intent=" + intent);

                    // Get targets
                    PackageManager pm = context.getPackageManager();
                    List<NameResolveInfo> targets = new ArrayList<>();
                    List<ResolveInfo> ris = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                    for (ResolveInfo ri : ris) {
                        if ("com.adobe.reader".equals(ri.activityInfo.packageName))
                            Toast.makeText(context, R.string.title_no_adobe, Toast.LENGTH_LONG).show();
                        Log.i("Target=" + ri);
                        context.grantUriPermission(ri.activityInfo.packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        targets.add(new NameResolveInfo(
                                pm.getApplicationIcon(ri.activityInfo.applicationInfo),
                                pm.getApplicationLabel(ri.activityInfo.applicationInfo).toString(),
                                ri));
                    }

                    // Check if viewer available
                    if (ris.size() == 0) {
                        Toast.makeText(context, context.getString(R.string.title_no_viewer, attachment.type), Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (confirm) {
                        View dview = LayoutInflater.from(context).inflate(R.layout.dialog_attachment, null);
                        final AlertDialog dialog = new DialogBuilderLifecycle(context, owner)
                                .setView(dview)
                                .setNegativeButton(android.R.string.cancel, null)
                                .create();

                        TextView tvName = dview.findViewById(R.id.tvName);
                        TextView tvType = dview.findViewById(R.id.tvType);
                        ListView lvApp = dview.findViewById(R.id.lvApp);

                        tvName.setText(attachment.name);
                        tvType.setText(attachment.type);

                        lvApp.setAdapter(new TargetAdapter(context, R.layout.item_target, targets));
                        lvApp.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                NameResolveInfo selected = (NameResolveInfo) parent.getItemAtPosition(position);
                                intent.setPackage(selected.info.activityInfo.packageName);
                                context.startActivity(intent);
                                dialog.dismiss();
                            }
                        });

                        dialog.show();
                    } else
                        context.startActivity(intent);
                } else {
                    if (attachment.progress == null) {
                        Bundle args = new Bundle();
                        args.putLong("id", attachment.id);
                        args.putLong("message", attachment.message);
                        args.putInt("sequence", attachment.sequence);

                        new SimpleTask<Void>() {
                            @Override
                            protected Void onExecute(Context context, Bundle args) {
                                long id = args.getLong("id");
                                long message = args.getLong("message");
                                long sequence = args.getInt("sequence");

                                DB db = DB.getInstance(context);
                                try {
                                    db.beginTransaction();

                                    db.attachment().setProgress(id, 0);

                                    EntityMessage msg = db.message().getMessage(message);
                                    EntityOperation.queue(context, db, msg, EntityOperation.ATTACHMENT, sequence);

                                    db.setTransactionSuccessful();
                                } finally {
                                    db.endTransaction();
                                }

                                return null;
                            }

                            @Override
                            protected void onException(Bundle args, Throwable ex) {
                                Helper.unexpectedError(context, owner, ex);
                            }
                        }.execute(context, owner, args, "attachment:fetch");
                    }
                }
            }
        }

        private class NameResolveInfo {
            Drawable icon;
            String name;
            ResolveInfo info;

            NameResolveInfo(Drawable icon, String name, ResolveInfo info) {
                this.icon = icon;
                this.name = name;
                this.info = info;
            }
        }

        public class TargetAdapter extends ArrayAdapter<NameResolveInfo> {
            private Context context;

            TargetAdapter(Context context, int resid, List<NameResolveInfo> items) {
                super(context, resid, items);
                this.context = context;
            }

            public View getView(int position, View convertView, ViewGroup parent) {
                NameResolveInfo item = getItem(position);

                View view = LayoutInflater.from(context).inflate(R.layout.item_target, null);
                ImageView ivIcon = view.findViewById(R.id.ivIcon);
                TextView tvName = view.findViewById(R.id.tvName);

                ivIcon.setImageDrawable(item.icon);
                tvName.setText(item.name);

                return view;
            }
        }
    }

    AdapterAttachment(Context context, LifecycleOwner owner, boolean readonly) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.owner = owner;
        this.readonly = readonly;
        this.confirm = prefs.getBoolean("confirm", false);
        this.debug = prefs.getBoolean("debug", false);
        setHasStableIds(true);
    }

    public void set(@NonNull List<EntityAttachment> attachments) {
        Log.i("Set attachments=" + attachments.size());

        Collections.sort(attachments, new Comparator<EntityAttachment>() {
            @Override
            public int compare(EntityAttachment a1, EntityAttachment a2) {
                return a1.sequence.compareTo(a2.sequence);
            }
        });

        all = attachments;

        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new MessageDiffCallback(filtered, all));

        filtered.clear();
        filtered.addAll(all);

        diff.dispatchUpdatesTo(new ListUpdateCallback() {
            @Override
            public void onInserted(int position, int count) {
                Log.i("Inserted @" + position + " #" + count);
            }

            @Override
            public void onRemoved(int position, int count) {
                Log.i("Removed @" + position + " #" + count);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                Log.i("Moved " + fromPosition + ">" + toPosition);
            }

            @Override
            public void onChanged(int position, int count, Object payload) {
                Log.i("Changed @" + position + " #" + count);
            }
        });
        diff.dispatchUpdatesTo(this);
    }

    private class MessageDiffCallback extends DiffUtil.Callback {
        private List<EntityAttachment> prev;
        private List<EntityAttachment> next;

        MessageDiffCallback(List<EntityAttachment> prev, List<EntityAttachment> next) {
            this.prev = prev;
            this.next = next;
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
        return filtered.get(position).id;
    }

    @Override
    public int getItemCount() {
        return filtered.size();
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.item_attachment, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.unwire();

        EntityAttachment attachment = filtered.get(position);
        holder.bindTo(attachment);

        holder.wire();
    }
}
