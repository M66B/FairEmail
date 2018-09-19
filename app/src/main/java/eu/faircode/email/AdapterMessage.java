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

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
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

    private boolean avatars;
    private boolean debug;

    private ExecutorService executor = Executors.newCachedThreadPool(Helper.backgroundThreadFactory);
    private DateFormat df = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.LONG);

    enum ViewType {UNIFIED, FOLDER, THREAD, SEARCH}

    public class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {
        View itemView;
        View vwColor;
        ImageView ivAvatar;
        ImageView ivFlagged;
        TextView tvFrom;
        TextView tvSize;
        TextView tvTime;
        ImageView ivAttachments;
        TextView tvSubject;
        TextView tvFolder;
        TextView tvCount;
        ImageView ivThread;
        TextView tvError;
        ProgressBar pbLoading;

        private static final int action_flag = 1;
        private static final int action_seen = 2;
        private static final int action_delete = 3;

        ViewHolder(View itemView) {
            super(itemView);

            this.itemView = itemView;
            vwColor = itemView.findViewById(R.id.vwColor);
            ivFlagged = itemView.findViewById(R.id.ivFlagged);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvFrom = itemView.findViewById(R.id.tvFrom);
            tvSize = itemView.findViewById(R.id.tvSize);
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
            ivFlagged.setVisibility(View.GONE);
            ivAvatar.setVisibility(View.GONE);
            tvFrom.setText(null);
            tvSize.setText(null);
            tvTime.setText(null);
            ivAttachments.setVisibility(View.GONE);
            tvSubject.setText(null);
            tvFolder.setText(null);
            tvCount.setText(null);
            ivThread.setVisibility(View.GONE);
            tvError.setVisibility(View.GONE);
            pbLoading.setVisibility(View.VISIBLE);
        }

        private void bindTo(final TupleMessageEx message) {
            pbLoading.setVisibility(View.GONE);

            if (avatars && message.avatar != null) {
                ContentResolver resolver = context.getContentResolver();
                InputStream is = ContactsContract.Contacts.openContactPhotoInputStream(resolver, Uri.parse(message.avatar));
                ivAvatar.setImageDrawable(Drawable.createFromStream(is, "avatar"));
            }
            ivAvatar.setVisibility(!avatars || message.avatar == null ? View.GONE : View.VISIBLE);

            if (avatars && message.from != null && message.from.length > 0) {
                final long id = message.id;
                final Address[] froms = message.from;
                executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            for (Address from : froms) {
                                String email = ((InternetAddress) from).getAddress();
                                Cursor cursor = null;
                                try {
                                    ContentResolver resolver = context.getContentResolver();
                                    cursor = resolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                                            new String[]{ContactsContract.CommonDataKinds.Photo.CONTACT_ID},
                                            ContactsContract.CommonDataKinds.Email.ADDRESS + " = ?",
                                            new String[]{email}, null);
                                    if (cursor.moveToNext()) {
                                        int colContactId = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Photo.CONTACT_ID);
                                        long contactId = cursor.getLong(colContactId);
                                        Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
                                        DB.getInstance(context).message().setMessageAvatar(id, uri.toString());
                                        break;
                                    }
                                } finally {
                                    if (cursor != null)
                                        cursor.close();
                                }
                            }
                        } catch (Throwable ex) {
                            Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                        }
                    }
                });
            }

            vwColor.setBackgroundColor(message.accountColor == null ? Color.TRANSPARENT : message.accountColor);
            vwColor.setVisibility(viewType == ViewType.UNIFIED ? View.VISIBLE : View.GONE);

            ivFlagged.setVisibility(message.ui_flagged ? View.VISIBLE : View.GONE);

            if (EntityFolder.DRAFTS.equals(message.folderType) ||
                    EntityFolder.OUTBOX.equals(message.folderType) ||
                    EntityFolder.SENT.equals(message.folderType)) {
                tvFrom.setText(MessageHelper.getFormattedAddresses(message.to, false));
                tvTime.setText(DateUtils.getRelativeTimeSpanString(context, message.sent == null ? message.received : message.sent));
            } else {
                tvFrom.setText(MessageHelper.getFormattedAddresses(message.from, false));
                tvTime.setText(DateUtils.getRelativeTimeSpanString(context, message.received));
            }

            tvSize.setText(message.size == null ? null : Helper.humanReadableByteCount(message.size, true));
            tvSize.setVisibility(message.size == null ? View.GONE : View.VISIBLE);

            ivAttachments.setVisibility(message.attachments > 0 ? View.VISIBLE : View.GONE);
            tvSubject.setText(message.subject);

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

            final TupleMessageEx message = getItem(pos);

            PopupMenu popupMenu = new PopupMenu(context, itemView);
            popupMenu.getMenu().add(Menu.NONE, action_flag, 1, message.ui_flagged ? R.string.title_unflag : R.string.title_flag);
            popupMenu.getMenu().add(Menu.NONE, action_seen, 2, message.ui_seen ? R.string.title_unseen : R.string.title_seen);
            if (EntityFolder.TRASH.equals(message.folderType))
                popupMenu.getMenu().add(Menu.NONE, action_delete, 3, R.string.title_delete);
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem target) {
                    Bundle args = new Bundle();
                    args.putLong("id", message.id);
                    args.putInt("action", target.getItemId());

                    if (target.getItemId() == action_delete) {
                        new AlertDialog.Builder(context)
                                .setMessage(R.string.title_ask_delete)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
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
                                                    db.message().setMessageUiHide(id, true);
                                                    EntityOperation.queue(db, message, EntityOperation.DELETE);

                                                    db.setTransactionSuccessful();
                                                } finally {
                                                    db.endTransaction();
                                                }

                                                EntityOperation.process(context);

                                                return null;
                                            }

                                            @Override
                                            protected void onException(Bundle args, Throwable ex) {
                                                Helper.unexpectedError(context, ex);
                                            }
                                        }.load(context, owner, args);
                                    }
                                })
                                .setNegativeButton(android.R.string.cancel, null)
                                .show();
                    } else
                        new SimpleTask<Void>() {
                            @Override
                            protected Void onLoad(final Context context, Bundle args) {
                                long id = args.getLong("id");
                                int action = args.getInt("action");

                                DB db = DB.getInstance(context);
                                try {
                                    db.beginTransaction();

                                    EntityMessage message = db.message().getMessage(id);
                                    for (EntityMessage tmessage : db.message().getMessageByThread(message.account, message.thread))
                                        if (action == action_flag) {
                                            db.message().setMessageUiFlagged(tmessage.id, !message.ui_flagged);
                                            EntityOperation.queue(db, tmessage, EntityOperation.FLAG, !tmessage.ui_flagged);
                                        } else if (action == action_seen) {
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
                                Helper.unexpectedError(context, ex);
                            }
                        }.load(context, owner, args);

                    return true;
                }
            });

            popupMenu.show();

            return true;
        }
    }

    AdapterMessage(Context context, LifecycleOwner owner, ViewType viewType) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.owner = owner;
        this.viewType = viewType;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        this.avatars = (prefs.getBoolean("avatars", true) &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
                        == PackageManager.PERMISSION_GRANTED);
        this.debug = prefs.getBoolean("debug", false);
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
