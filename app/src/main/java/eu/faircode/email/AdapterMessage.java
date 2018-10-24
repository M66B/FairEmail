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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.Html;
import android.text.Layout;
import android.text.Spannable;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import org.xml.sax.XMLReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.Collator;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterMessage extends PagedListAdapter<TupleMessageEx, AdapterMessage.ViewHolder> {
    private Context context;
    private LifecycleOwner owner;
    private FragmentManager fragmentManager;
    private ViewType viewType;
    private IProperties properties;

    private boolean contacts;
    private boolean avatars;
    private boolean compact;
    private boolean debug;

    private SelectionTracker<Long> selectionTracker = null;

    private DateFormat df = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.LONG, SimpleDateFormat.LONG);

    enum ViewType {UNIFIED, FOLDER, THREAD, SEARCH}

    private static final long CACHE_IMAGE_DURATION = 3 * 24 * 3600 * 1000L;

    public class ViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener, BottomNavigationView.OnNavigationItemSelectedListener {
        private View itemView;
        private View vwColor;
        private ImageView ivExpander;
        private ImageView ivFlagged;
        private ImageView ivAvatar;
        private TextView tvFrom;
        private ImageView ivAddContact;
        private TextView tvSize;
        private TextView tvTime;
        private TextView tvTimeEx;
        private ImageView ivAttachments;
        private TextView tvSubject;
        private TextView tvFolder;
        private TextView tvCount;
        private ImageView ivThread;
        private TextView tvError;
        private ProgressBar pbLoading;

        private TextView tvFromEx;
        private TextView tvTo;
        private TextView tvReplyTo;
        private TextView tvCc;
        private TextView tvBcc;
        private TextView tvSubjectEx;

        private TextView tvHeaders;
        private ProgressBar pbHeaders;

        private BottomNavigationView bnvActions;

        private View vSeparatorBody;
        private Button btnImages;
        private TextView tvBody;
        private ProgressBar pbBody;

        private RecyclerView rvAttachment;
        private AdapterAttachment adapter;

        private Group grpHeaders;
        private Group grpAttachments;
        private Group grpExpanded;

        private ItemDetailsMessage itemDetails = null;

        ViewHolder(View itemView) {
            super(itemView);

            this.itemView = itemView.findViewById(R.id.clItem);
            vwColor = itemView.findViewById(R.id.vwColor);
            ivExpander = itemView.findViewById(R.id.ivExpander);
            ivFlagged = itemView.findViewById(R.id.ivFlagged);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvFrom = itemView.findViewById(R.id.tvFrom);
            ivAddContact = itemView.findViewById(R.id.ivAddContact);
            tvSize = itemView.findViewById(R.id.tvSize);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvTimeEx = itemView.findViewById(R.id.tvTimeEx);
            ivAttachments = itemView.findViewById(R.id.ivAttachments);
            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvFolder = itemView.findViewById(R.id.tvFolder);
            tvCount = itemView.findViewById(R.id.tvCount);
            ivThread = itemView.findViewById(R.id.ivThread);
            tvError = itemView.findViewById(R.id.tvError);
            pbLoading = itemView.findViewById(R.id.pbLoading);

            tvFromEx = itemView.findViewById(R.id.tvFromEx);
            tvTo = itemView.findViewById(R.id.tvTo);
            tvReplyTo = itemView.findViewById(R.id.tvReplyTo);
            tvCc = itemView.findViewById(R.id.tvCc);
            tvBcc = itemView.findViewById(R.id.tvBcc);
            tvSubjectEx = itemView.findViewById(R.id.tvSubjectEx);

            tvHeaders = itemView.findViewById(R.id.tvHeaders);
            pbHeaders = itemView.findViewById(R.id.pbHeaders);

            bnvActions = itemView.findViewById(R.id.bnvActions);

            vSeparatorBody = itemView.findViewById(R.id.vSeparatorBody);
            btnImages = itemView.findViewById(R.id.btnImages);
            tvBody = itemView.findViewById(R.id.tvBody);
            pbBody = itemView.findViewById(R.id.pbBody);

            rvAttachment = itemView.findViewById(R.id.rvAttachment);
            rvAttachment.setHasFixedSize(false);
            LinearLayoutManager llm = new LinearLayoutManager(context);
            rvAttachment.setLayoutManager(llm);
            rvAttachment.setItemAnimator(null);

            adapter = new AdapterAttachment(context, owner, true);
            rvAttachment.setAdapter(adapter);

            grpHeaders = itemView.findViewById(R.id.grpHeaders);
            grpAttachments = itemView.findViewById(R.id.grpAttachments);
            grpExpanded = itemView.findViewById(R.id.grpExpanded);

            tvBody.setMovementMethod(new UrlHandler());
        }

        private void wire() {
            itemView.setOnClickListener(this);
            ivAddContact.setOnClickListener(this);
            bnvActions.setOnNavigationItemSelectedListener(this);
            btnImages.setOnClickListener(this);
        }

        private void unwire() {
            itemView.setOnClickListener(null);
            ivAddContact.setOnClickListener(null);
            bnvActions.setOnNavigationItemSelectedListener(null);
            btnImages.setOnClickListener(null);
        }

        private void clear() {
            vwColor.setVisibility(View.GONE);
            ivExpander.setVisibility(View.GONE);
            ivFlagged.setVisibility(View.GONE);
            ivAvatar.setVisibility(View.GONE);
            tvFrom.setText(null);
            ivAddContact.setVisibility(View.GONE);
            tvSize.setText(null);
            tvTime.setText(null);
            ivAttachments.setVisibility(View.GONE);
            tvSubject.setText(null);
            tvFolder.setText(null);
            tvCount.setText(null);
            ivThread.setVisibility(View.GONE);
            tvError.setVisibility(View.GONE);
            pbLoading.setVisibility(View.VISIBLE);
            pbHeaders.setVisibility(View.GONE);
            btnImages.setVisibility(View.GONE);
            pbBody.setVisibility(View.GONE);
            grpHeaders.setVisibility(View.GONE);
            grpAttachments.setVisibility(View.GONE);
            grpExpanded.setVisibility(View.GONE);
        }

        private void bindTo(int position, final TupleMessageEx message) {
            final DB db = DB.getInstance(context);
            final boolean show_expanded = properties.isExpanded(message.id);
            boolean show_headers = properties.showHeaders(message.id);

            pbLoading.setVisibility(View.GONE);

            boolean photo = false;
            if (avatars && message.avatar != null) {
                ContentResolver resolver = context.getContentResolver();
                InputStream is = ContactsContract.Contacts.openContactPhotoInputStream(resolver, Uri.parse(message.avatar));
                if (is != null) {
                    photo = true;
                    ivAvatar.setImageDrawable(Drawable.createFromStream(is, "avatar"));
                }
            }
            ivAvatar.setVisibility(photo ? View.VISIBLE : View.GONE);

            vwColor.setBackgroundColor(message.accountColor == null ? Color.TRANSPARENT : message.accountColor);
            vwColor.setVisibility(viewType == ViewType.UNIFIED && message.accountColor != null ? View.VISIBLE : View.GONE);

            ivExpander.setImageResource(show_expanded ? R.drawable.baseline_expand_less_24 : R.drawable.baseline_expand_more_24);
            ivExpander.setVisibility(viewType == ViewType.THREAD ? View.VISIBLE : View.GONE);

            if (viewType == ViewType.THREAD)
                ivFlagged.setVisibility(message.unflagged == 1 ? View.GONE : View.VISIBLE);
            else
                ivFlagged.setVisibility(message.count - message.unflagged > 0 ? View.VISIBLE : View.GONE);

            if (EntityFolder.DRAFTS.equals(message.folderType) ||
                    EntityFolder.OUTBOX.equals(message.folderType) ||
                    EntityFolder.SENT.equals(message.folderType)) {
                tvFrom.setText(MessageHelper.getFormattedAddresses(message.to, !compact));
                tvTime.setText(DateUtils.getRelativeTimeSpanString(context, message.sent == null ? message.received : message.sent));
            } else {
                tvFrom.setText(MessageHelper.getFormattedAddresses(message.from, !compact));
                tvTime.setText(DateUtils.getRelativeTimeSpanString(context, message.received));
            }

            tvSize.setText(message.size == null ? null : Helper.humanReadableByteCount(message.size, true));
            tvSize.setTypeface(null, message.content ? Typeface.NORMAL : Typeface.BOLD);
            tvSize.setVisibility(message.size == null ? View.GONE : View.VISIBLE);

            ivAttachments.setVisibility(message.attachments > 0 ? View.VISIBLE : View.GONE);
            tvSubject.setText(message.subject);

            if (viewType == ViewType.UNIFIED)
                tvFolder.setText(message.accountName);
            else
                tvFolder.setText(message.folderDisplay == null
                        ? Helper.localizeFolderName(context, message.folderName)
                        : message.folderDisplay);
            tvFolder.setVisibility(viewType == ViewType.FOLDER ? View.GONE : View.VISIBLE);

            if (viewType == ViewType.THREAD) {
                tvCount.setVisibility(View.GONE);
                ivThread.setVisibility(View.GONE);
            } else {
                tvCount.setText(Integer.toString(message.count));
                ivThread.setVisibility(View.VISIBLE);
            }

            if (debug) {
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
            } else {
                tvError.setText(message.error);
                tvError.setVisibility(message.error == null ? View.GONE : View.VISIBLE);
            }

            int typeface = (message.unseen > 0 ? Typeface.BOLD : Typeface.NORMAL);
            tvFrom.setTypeface(null, typeface);
            tvTime.setTypeface(null, typeface);
            tvSubject.setTypeface(null, typeface);
            tvCount.setTypeface(null, typeface);

            int colorUnseen = Helper.resolveColor(context, message.unseen > 0
                    ? R.attr.colorUnread : android.R.attr.textColorSecondary);
            tvFrom.setTextColor(colorUnseen);
            tvTime.setTextColor(colorUnseen);

            grpExpanded.setVisibility(viewType == ViewType.THREAD && show_expanded ? View.VISIBLE : View.GONE);
            ivAddContact.setVisibility(viewType == ViewType.THREAD && show_expanded && contacts && message.from != null ? View.VISIBLE : View.GONE);
            pbHeaders.setVisibility(View.GONE);
            grpHeaders.setVisibility(show_headers && show_expanded ? View.VISIBLE : View.GONE);
            bnvActions.setVisibility(View.GONE);
            vSeparatorBody.setVisibility(View.GONE);
            btnImages.setVisibility(View.GONE);
            pbBody.setVisibility(View.GONE);
            grpAttachments.setVisibility(message.attachments > 0 && show_expanded ? View.VISIBLE : View.GONE);

            db.folder().liveSystemFolders(message.account).removeObservers(owner);
            db.attachment().liveAttachments(message.id).removeObservers(owner);

            bnvActions.setTag(null);

            if (show_expanded) {
                if (EntityFolder.DRAFTS.equals(message.folderType) ||
                        EntityFolder.OUTBOX.equals(message.folderType) ||
                        EntityFolder.SENT.equals(message.folderType))
                    tvTimeEx.setText(df.format(new Date(message.sent == null ? message.received : message.sent)));
                else
                    tvTimeEx.setText(df.format(new Date(message.received)));

                tvFromEx.setText(MessageHelper.getFormattedAddresses(message.from, true));
                tvTo.setText(MessageHelper.getFormattedAddresses(message.to, true));
                tvReplyTo.setText(MessageHelper.getFormattedAddresses(message.reply, true));
                tvCc.setText(MessageHelper.getFormattedAddresses(message.cc, true));
                tvBcc.setText(MessageHelper.getFormattedAddresses(message.bcc, true));
                tvSubjectEx.setText(message.subject);

                tvHeaders.setText(show_headers ? message.headers : null);

                vSeparatorBody.setVisibility(View.VISIBLE);
                tvBody.setText(null);
                pbBody.setVisibility(View.VISIBLE);

                if (message.content) {
                    Bundle args = new Bundle();
                    args.putSerializable("message", message);
                    bodyTask.load(context, owner, args);
                }

                if (!EntityFolder.OUTBOX.equals(message.folderType)) {
                    bnvActions.setHasTransientState(true);
                    db.folder().liveSystemFolders(message.account).observe(owner, new Observer<List<EntityFolder>>() {
                        @Override
                        public void onChanged(@Nullable List<EntityFolder> folders) {
                            if (bnvActions.hasTransientState()) {
                                boolean hasJunk = false;
                                boolean hasTrash = false;
                                boolean hasArchive = false;
                                boolean hasUser = false;

                                if (folders != null)
                                    for (EntityFolder folder : folders) {
                                        if (EntityFolder.JUNK.equals(folder.type))
                                            hasJunk = true;
                                        else if (EntityFolder.TRASH.equals(folder.type))
                                            hasTrash = true;
                                        else if (EntityFolder.ARCHIVE.equals(folder.type))
                                            hasArchive = true;
                                        else if (EntityFolder.USER.equals(folder.type))
                                            hasUser = true;
                                    }

                                boolean inInbox = EntityFolder.INBOX.equals(message.folderType);
                                boolean inOutbox = EntityFolder.OUTBOX.equals(message.folderType);
                                boolean inArchive = EntityFolder.ARCHIVE.equals(message.folderType);
                                boolean inTrash = EntityFolder.TRASH.equals(message.folderType);

                                ActionData data = new ActionData();
                                data.hasJunk = hasJunk;
                                data.delete = (inTrash || !hasTrash || inOutbox);
                                data.message = message;
                                bnvActions.setTag(data);

                                bnvActions.getMenu().findItem(R.id.action_delete).setVisible((message.uid != null && hasTrash) || (inOutbox && !TextUtils.isEmpty(message.error)));
                                bnvActions.getMenu().findItem(R.id.action_move).setVisible(message.uid != null && (!inInbox || hasUser));
                                bnvActions.getMenu().findItem(R.id.action_archive).setVisible(message.uid != null && !inArchive && hasArchive);

                                bnvActions.getMenu().findItem(R.id.action_reply).setEnabled(message.content);
                                bnvActions.getMenu().findItem(R.id.action_reply).setVisible(!inOutbox);

                                bnvActions.setVisibility(View.VISIBLE);
                                vSeparatorBody.setVisibility(View.GONE);

                                bnvActions.setHasTransientState(false);
                            }
                        }
                    });
                }

                // Observe attachments
                db.attachment().liveAttachments(message.id).observe(owner,
                        new Observer<List<EntityAttachment>>() {
                            @Override
                            public void onChanged(@Nullable List<EntityAttachment> attachments) {
                                if (attachments == null)
                                    attachments = new ArrayList<>();

                                adapter.set(attachments);

                                if (message.content) {
                                    Bundle args = new Bundle();
                                    args.putSerializable("message", message);
                                    bodyTask.load(context, owner, args);
                                }
                            }
                        });
            }

            itemDetails = new ItemDetailsMessage(position, message.id);
            itemView.setActivated(selectionTracker != null && selectionTracker.isSelected(message.id));
        }

        @Override
        public void onClick(View view) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return;

            TupleMessageEx message = getItem(pos);

            if (view.getId() == R.id.ivAddContact)
                onAddContact(message);
            else if (viewType == ViewType.THREAD) {
                if (view.getId() == R.id.btnImages)
                    onShowImages(message);
                else
                    onExpandMessage(pos, message);
            } else {
                if (EntityFolder.DRAFTS.equals(message.folderType))
                    context.startActivity(
                            new Intent(context, ActivityCompose.class)
                                    .putExtra("action", "edit")
                                    .putExtra("id", message.id));
                else {
                    LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                    lbm.sendBroadcast(
                            new Intent(ActivityView.ACTION_VIEW_THREAD)
                                    .putExtra("account", message.account)
                                    .putExtra("thread", message.thread));
                }
            }
        }

        private void onAddContact(EntityMessage message) {
            for (Address address : message.from) {
                InternetAddress ia = (InternetAddress) address;
                String name = ia.getPersonal();
                String email = ia.getAddress();

                // https://developer.android.com/training/contacts-provider/modify-data
                Intent edit = new Intent();
                if (!TextUtils.isEmpty(name))
                    edit.putExtra(ContactsContract.Intents.Insert.NAME, name);
                if (!TextUtils.isEmpty(email))
                    edit.putExtra(ContactsContract.Intents.Insert.EMAIL, email);

                Cursor cursor = null;
                try {
                    ContentResolver resolver = context.getContentResolver();
                    cursor = resolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                            new String[]{
                                    ContactsContract.CommonDataKinds.Photo.CONTACT_ID,
                                    ContactsContract.Contacts.LOOKUP_KEY
                            },
                            ContactsContract.CommonDataKinds.Email.ADDRESS + " = ?",
                            new String[]{email}, null);
                    if (cursor.moveToNext()) {
                        int colContactId = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Photo.CONTACT_ID);
                        int colLookupKey = cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY);

                        long contactId = cursor.getLong(colContactId);
                        String lookupKey = cursor.getString(colLookupKey);

                        Uri lookupUri = ContactsContract.Contacts.getLookupUri(contactId, lookupKey);

                        edit.setAction(Intent.ACTION_EDIT);
                        edit.setDataAndType(lookupUri, ContactsContract.Contacts.CONTENT_ITEM_TYPE);
                    } else {
                        edit.setAction(Intent.ACTION_INSERT);
                        edit.setType(ContactsContract.Contacts.CONTENT_TYPE);
                    }
                } finally {
                    if (cursor != null)
                        cursor.close();
                }

                context.startActivity(edit);
            }
        }

        private void onShowImages(EntityMessage message) {
            properties.setImages(message.id, !properties.showImages(message.id));

            Bundle args = new Bundle();
            args.putSerializable("message", message);
            bodyTask.load(context, owner, args);
        }

        private void onExpandMessage(int pos, EntityMessage message) {
            boolean expanded = !properties.isExpanded(message.id);
            properties.setExpanded(message.id, expanded);
            notifyItemChanged(pos);
        }

        private SimpleTask<Spanned> bodyTask = new SimpleTask<Spanned>() {
            @Override
            protected void onInit(Bundle args) {
                btnImages.setHasTransientState(true);
                tvBody.setHasTransientState(true);
                pbBody.setHasTransientState(true);
            }

            @Override
            protected Spanned onLoad(final Context context, final Bundle args) throws Throwable {
                TupleMessageEx message = (TupleMessageEx) args.getSerializable("message");
                String body = message.read(context);
                return decodeHtml(message, body);
            }

            @Override
            protected void onLoaded(Bundle args, Spanned body) {
                TupleMessageEx message = (TupleMessageEx) args.getSerializable("message");

                SpannedString ss = new SpannedString(body);
                boolean has_images = (ss.getSpans(0, ss.length(), ImageSpan.class).length > 0);
                boolean show_expanded = properties.isExpanded(message.id);
                boolean show_images = properties.showImages(message.id);

                btnImages.setVisibility(has_images && show_expanded && !show_images ? View.VISIBLE : View.GONE);
                tvBody.setText(body);
                pbBody.setVisibility(View.GONE);

                btnImages.setHasTransientState(false);
                tvBody.setHasTransientState(false);
                pbBody.setHasTransientState(false);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                btnImages.setHasTransientState(false);
                tvBody.setHasTransientState(false);
                pbBody.setHasTransientState(false);

                Helper.unexpectedError(context, ex);
            }
        };

        private Spanned decodeHtml(final EntityMessage message, String body) {
            return Html.fromHtml(HtmlHelper.sanitize(body), new Html.ImageGetter() {
                @Override
                public Drawable getDrawable(String source) {
                    float scale = context.getResources().getDisplayMetrics().density;
                    int px = (int) (24 * scale + 0.5f);

                    if (source != null && source.startsWith("cid")) {
                        String cid = "<" + source.split(":")[1] + ">";
                        EntityAttachment attachment = DB.getInstance(context).attachment().getAttachment(message.id, cid);
                        if (attachment == null || !attachment.available) {
                            Drawable d = context.getResources().getDrawable(R.drawable.baseline_warning_24, context.getTheme());
                            d.setBounds(0, 0, px, px);
                            return d;
                        } else {
                            File file = EntityAttachment.getFile(context, attachment.id);
                            Drawable d = Drawable.createFromPath(file.getAbsolutePath());
                            d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
                            return d;
                        }
                    }

                    if (properties.showImages(message.id)) {
                        // Get cache folder
                        File dir = new File(context.getCacheDir(), "images");
                        dir.mkdir();

                        // Cleanup cache
                        long now = new Date().getTime();
                        File[] images = dir.listFiles();
                        if (images != null)
                            for (File image : images)
                                if (image.isFile() && image.lastModified() + CACHE_IMAGE_DURATION < now) {
                                    Log.i(Helper.TAG, "Deleting from image cache " + image.getName());
                                    image.delete();
                                }

                        InputStream is = null;
                        FileOutputStream os = null;
                        try {
                            if (source == null)
                                throw new IllegalArgumentException("Html.ImageGetter.getDrawable(source == null)");

                            // Create unique file name
                            File file = new File(dir, message.id + "_" + source.hashCode());

                            // Get input stream
                            if (file.exists()) {
                                Log.i(Helper.TAG, "Using cached " + file);
                                is = new FileInputStream(file);
                            } else {
                                Log.i(Helper.TAG, "Downloading " + source);
                                is = new URL(source).openStream();
                            }

                            // Decode image from stream
                            Bitmap bm = BitmapFactory.decodeStream(is);
                            if (bm == null)
                                throw new IllegalArgumentException();

                            // Cache bitmap
                            if (!file.exists()) {
                                os = new FileOutputStream(file);
                                bm.compress(Bitmap.CompressFormat.PNG, 100, os);
                            }

                            // Create drawable from bitmap
                            Drawable d = new BitmapDrawable(context.getResources(), bm);
                            d.setBounds(0, 0, bm.getWidth(), bm.getHeight());
                            return d;
                        } catch (Throwable ex) {
                            // Show warning icon
                            Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                            Drawable d = context.getResources().getDrawable(R.drawable.baseline_warning_24, context.getTheme());
                            d.setBounds(0, 0, px, px);
                            return d;
                        } finally {
                            // Close streams
                            if (is != null) {
                                try {
                                    is.close();
                                } catch (IOException e) {
                                    Log.w(Helper.TAG, e + "\n" + Log.getStackTraceString(e));
                                }
                            }
                            if (os != null) {
                                try {
                                    os.close();
                                } catch (IOException e) {
                                    Log.w(Helper.TAG, e + "\n" + Log.getStackTraceString(e));
                                }
                            }
                        }
                    } else {
                        // Show placeholder icon
                        Drawable d = context.getResources().getDrawable(R.drawable.baseline_image_24, context.getTheme());
                        d.setBounds(0, 0, px, px);
                        return d;
                    }
                }
            }, new Html.TagHandler() {
                @Override
                public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
                    if (BuildConfig.DEBUG)
                        Log.i(Helper.TAG, "HTML tag=" + tag + " opening=" + opening);
                }
            });
        }

        private class UrlHandler extends LinkMovementMethod {
            public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
                if (event.getAction() != MotionEvent.ACTION_UP)
                    return false;

                int x = (int) event.getX();
                int y = (int) event.getY();

                x -= widget.getTotalPaddingLeft();
                y -= widget.getTotalPaddingTop();

                x += widget.getScrollX();
                y += widget.getScrollY();

                Layout layout = widget.getLayout();
                int line = layout.getLineForVertical(y);
                int off = layout.getOffsetForHorizontal(line, x);

                URLSpan[] link = buffer.getSpans(off, off, URLSpan.class);
                if (link.length != 0) {
                    String url = link[0].getURL();
                    Uri uri = Uri.parse(url);

                    if (!"http".equals(uri.getScheme()) && !"https".equals(uri.getScheme())) {
                        Toast.makeText(context, context.getString(R.string.title_no_viewer, uri.toString()), Toast.LENGTH_LONG).show();
                        return true;
                    }

                    if (BuildConfig.APPLICATION_ID.equals(uri.getHost()) && "/activate/".equals(uri.getPath())) {
                        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                        lbm.sendBroadcast(
                                new Intent(ActivityView.ACTION_ACTIVATE_PRO)
                                        .putExtra("uri", uri));

                    } else {
                        View view = LayoutInflater.from(context).inflate(R.layout.dialog_link, null);
                        final EditText etLink = view.findViewById(R.id.etLink);
                        etLink.setText(url);
                        new DialogBuilderLifecycle(context, owner)
                                .setView(view)
                                .setPositiveButton(R.string.title_yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Uri uri = Uri.parse(etLink.getText().toString());

                                        if (!"http".equals(uri.getScheme()) && !"https".equals(uri.getScheme())) {
                                            Toast.makeText(context, context.getString(R.string.title_no_viewer, uri.toString()), Toast.LENGTH_LONG).show();
                                            return;
                                        }

                                        Helper.view(context, uri);
                                    }
                                })
                                .setNegativeButton(R.string.title_no, null)
                                .show();
                    }
                }

                return true;
            }
        }

        private class ActionData {
            boolean hasJunk;
            boolean delete;
            TupleMessageEx message;
        }

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            ActionData data = (ActionData) bnvActions.getTag();
            if (data == null)
                return false;

            switch (item.getItemId()) {
                case R.id.action_more:
                    onMore(data);
                    return true;
                case R.id.action_delete:
                    onDelete(data);
                    return true;
                case R.id.action_move:
                    onMove(data);
                    return true;
                case R.id.action_archive:
                    onArchive(data);
                    return true;
                case R.id.action_reply:
                    onReply(data);
                    return true;
                default:
                    return false;
            }
        }

        private void onJunk(final ActionData data) {
            new DialogBuilderLifecycle(context, owner)
                    .setMessage(R.string.title_ask_spam)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Bundle args = new Bundle();
                            args.putLong("id", data.message.id);

                            new SimpleTask<Void>() {
                                @Override
                                protected Void onLoad(Context context, Bundle args) {
                                    long id = args.getLong("id");

                                    DB db = DB.getInstance(context);
                                    try {
                                        db.beginTransaction();

                                        db.message().setMessageUiHide(id, true);

                                        EntityMessage message = db.message().getMessage(id);
                                        EntityFolder spam = db.folder().getFolderByType(message.account, EntityFolder.JUNK);
                                        EntityOperation.queue(db, message, EntityOperation.MOVE, spam.id);

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
        }

        private void onForward(final ActionData data) {
            Bundle args = new Bundle();
            args.putLong("id", data.message.id);

            new SimpleTask<Boolean>() {
                @Override
                protected Boolean onLoad(Context context, Bundle args) {
                    long id = args.getLong("id");
                    List<EntityAttachment> attachments = DB.getInstance(context).attachment().getAttachments(id);
                    for (EntityAttachment attachment : attachments)
                        if (!attachment.available)
                            return false;
                    return true;
                }

                @Override
                protected void onLoaded(Bundle args, Boolean available) {
                    final Intent forward = new Intent(context, ActivityCompose.class)
                            .putExtra("action", "forward")
                            .putExtra("reference", data.message.id);
                    if (available)
                        context.startActivity(forward);
                    else
                        new DialogBuilderLifecycle(context, owner)
                                .setMessage(R.string.title_attachment_unavailable)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        context.startActivity(forward);
                                    }
                                })
                                .setNegativeButton(android.R.string.cancel, null)
                                .show();
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Helper.unexpectedError(context, ex);
                }
            }.load(context, owner, args);
        }

        private void onReplyAll(ActionData data) {
            context.startActivity(new Intent(context, ActivityCompose.class)
                    .putExtra("action", "reply_all")
                    .putExtra("reference", data.message.id));
        }

        private void onAnswer(final ActionData data) {
            final DB db = DB.getInstance(context);
            db.answer().liveAnswers().observe(owner, new Observer<List<EntityAnswer>>() {
                @Override
                public void onChanged(List<EntityAnswer> answers) {
                    if (answers == null || answers.size() == 0) {
                        Snackbar snackbar = Snackbar.make(
                                itemView,
                                context.getString(R.string.title_no_answers),
                                Snackbar.LENGTH_LONG);
                        snackbar.setAction(R.string.title_fix, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                fragmentTransaction.replace(R.id.content_frame, new FragmentAnswers()).addToBackStack("answers");
                                fragmentTransaction.commit();
                            }
                        });
                        snackbar.show();
                    } else {
                        final Collator collator = Collator.getInstance(Locale.getDefault());
                        collator.setStrength(Collator.SECONDARY); // Case insensitive, process accents etc

                        Collections.sort(answers, new Comparator<EntityAnswer>() {
                            @Override
                            public int compare(EntityAnswer a1, EntityAnswer a2) {
                                return collator.compare(a1.name, a2.name);
                            }
                        });

                        View anchor = bnvActions.findViewById(R.id.action_more);
                        PopupMenu popupMenu = new PopupMenu(context, anchor);

                        int order = 0;
                        for (EntityAnswer answer : answers)
                            popupMenu.getMenu().add(Menu.NONE, answer.id.intValue(), order++, answer.name);

                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem target) {
                                if (Helper.isPro(context))
                                    context.startActivity(new Intent(context, ActivityCompose.class)
                                            .putExtra("action", "reply")
                                            .putExtra("reference", data.message.id)
                                            .putExtra("answer", (long) target.getItemId()));
                                else {
                                    LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                                    lbm.sendBroadcast(new Intent(ActivityView.ACTION_SHOW_PRO));
                                }
                                return true;
                            }
                        });

                        popupMenu.show();
                    }

                    db.answer().liveAnswers().removeObservers(owner);
                }
            });
        }

        private void onUnseen(final ActionData data) {
            Bundle args = new Bundle();
            args.putLong("id", data.message.id);

            new SimpleTask<Void>() {
                @Override
                protected Void onLoad(Context context, Bundle args) throws Throwable {
                    long id = args.getLong("id");

                    DB db = DB.getInstance(context);
                    try {
                        db.beginTransaction();

                        EntityMessage message = db.message().getMessage(id);
                        db.message().setMessageUiSeen(message.id, false);
                        EntityOperation.queue(db, message, EntityOperation.SEEN, false);

                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }

                    EntityOperation.process(context);

                    return null;
                }

                @Override
                protected void onLoaded(Bundle args, Void ignored) {
                    properties.setExpanded(data.message.id, false);
                    notifyDataSetChanged();
                }
            }.load(context, owner, args);
        }

        private void onFlag(ActionData data) {
            Bundle args = new Bundle();
            args.putLong("id", data.message.id);
            args.putBoolean("flagged", !data.message.ui_flagged);
            Log.i(Helper.TAG, "Set message id=" + data.message.id + " flagged=" + !data.message.ui_flagged);

            new SimpleTask<Void>() {
                @Override
                protected Void onLoad(Context context, Bundle args) throws Throwable {
                    long id = args.getLong("id");
                    boolean flagged = args.getBoolean("flagged");
                    DB db = DB.getInstance(context);
                    EntityMessage message = db.message().getMessage(id);
                    db.message().setMessageUiFlagged(message.id, flagged);
                    EntityOperation.queue(db, message, EntityOperation.FLAG, flagged);
                    EntityOperation.process(context);
                    return null;
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Helper.unexpectedError(context, ex);
                }
            }.load(context, owner, args);
        }

        private void onShowHeaders(ActionData data) {
            boolean show_headers = !properties.showHeaders(data.message.id);
            properties.setHeaders(data.message.id, show_headers);
            if (show_headers) {
                grpHeaders.setVisibility(View.VISIBLE);
                pbHeaders.setVisibility(View.VISIBLE);

                Bundle args = new Bundle();
                args.putLong("id", data.message.id);

                new SimpleTask<Void>() {
                    @Override
                    protected Void onLoad(Context context, Bundle args) {
                        Long id = args.getLong("id");
                        DB db = DB.getInstance(context);
                        EntityMessage message = db.message().getMessage(id);
                        EntityOperation.queue(db, message, EntityOperation.HEADERS);
                        EntityOperation.process(context);
                        return null;
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Helper.unexpectedError(context, ex);
                    }
                }.load(context, owner, args);
            } else
                notifyDataSetChanged();
        }

        private void onShowHtml(ActionData data) {
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
            lbm.sendBroadcast(
                    new Intent(ActivityView.ACTION_VIEW_FULL)
                            .putExtra("id", data.message.id)
                            .putExtra("from", MessageHelper.getFormattedAddresses(data.message.from, true)));
        }

        private void onMore(final ActionData data) {
            boolean inOutbox = EntityFolder.OUTBOX.equals(data.message.folderType);
            boolean show_headers = properties.showHeaders(data.message.id);

            View anchor = bnvActions.findViewById(R.id.action_more);
            PopupMenu popupMenu = new PopupMenu(context, anchor);
            popupMenu.inflate(R.menu.menu_message);
            popupMenu.getMenu().findItem(R.id.menu_junk).setVisible(data.message.uid != null && data.hasJunk && !inOutbox);

            popupMenu.getMenu().findItem(R.id.menu_forward).setEnabled(data.message.content);
            popupMenu.getMenu().findItem(R.id.menu_forward).setVisible(!inOutbox);

            popupMenu.getMenu().findItem(R.id.menu_reply_all).setEnabled(data.message.content);
            popupMenu.getMenu().findItem(R.id.menu_reply_all).setVisible(!inOutbox);

            popupMenu.getMenu().findItem(R.id.menu_show_headers).setChecked(show_headers);
            popupMenu.getMenu().findItem(R.id.menu_show_headers).setVisible(data.message.uid != null);

            popupMenu.getMenu().findItem(R.id.menu_show_html).setEnabled(data.message.content && Helper.classExists("android.webkit.WebView"));

            popupMenu.getMenu().findItem(R.id.menu_flag).setChecked(data.message.unflagged != 1);
            popupMenu.getMenu().findItem(R.id.menu_flag).setVisible(data.message.uid != null && !inOutbox);

            popupMenu.getMenu().findItem(R.id.menu_unseen).setVisible(data.message.uid != null && !inOutbox);

            popupMenu.getMenu().findItem(R.id.menu_answer).setEnabled(data.message.content);
            popupMenu.getMenu().findItem(R.id.menu_answer).setVisible(!inOutbox);

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem target) {
                    switch (target.getItemId()) {
                        case R.id.menu_junk:
                            onJunk(data);
                            return true;
                        case R.id.menu_forward:
                            onForward(data);
                            return true;
                        case R.id.menu_reply_all:
                            onReplyAll(data);
                            return true;
                        case R.id.menu_show_headers:
                            onShowHeaders(data);
                            return true;
                        case R.id.menu_show_html:
                            onShowHtml(data);
                            return true;
                        case R.id.menu_flag:
                            onFlag(data);
                            return true;
                        case R.id.menu_unseen:
                            onUnseen(data);
                            return true;
                        case R.id.menu_answer:
                            onAnswer(data);
                            return true;
                        default:
                            return false;
                    }
                }
            });
            popupMenu.show();
        }

        private void onDelete(final ActionData data) {
            if (data.delete) {
                // No trash or is trash
                new DialogBuilderLifecycle(context, owner)
                        .setMessage(R.string.title_ask_delete)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Bundle args = new Bundle();
                                args.putLong("id", data.message.id);

                                new SimpleTask<Void>() {
                                    @Override
                                    protected Void onLoad(Context context, Bundle args) {
                                        long id = args.getLong("id");

                                        DB db = DB.getInstance(context);
                                        try {
                                            db.beginTransaction();

                                            EntityMessage message = db.message().getMessage(id);
                                            if (message.uid == null && !TextUtils.isEmpty(message.error)) // outbox
                                                db.message().deleteMessage(id);
                                            else {
                                                db.message().setMessageUiHide(message.id, true);
                                                EntityOperation.queue(db, message, EntityOperation.DELETE);
                                            }

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
            } else {
                Bundle args = new Bundle();
                args.putLong("id", data.message.id);

                new SimpleTask<Void>() {
                    @Override
                    protected Void onLoad(Context context, Bundle args) {
                        long id = args.getLong("id");
                        DB db = DB.getInstance(context);
                        try {
                            db.beginTransaction();

                            db.message().setMessageUiHide(id, true);

                            EntityMessage message = db.message().getMessage(id);
                            EntityFolder trash = db.folder().getFolderByType(message.account, EntityFolder.TRASH);
                            EntityOperation.queue(db, message, EntityOperation.MOVE, trash.id);

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
        }

        private void onMove(ActionData data) {
            Bundle args = new Bundle();
            args.putLong("id", data.message.id);

            new SimpleTask<List<EntityFolder>>() {
                @Override
                protected List<EntityFolder> onLoad(Context context, Bundle args) {
                    EntityMessage message;
                    List<EntityFolder> folders;

                    DB db = DB.getInstance(context);
                    try {
                        db.beginTransaction();

                        message = db.message().getMessage(args.getLong("id"));
                        folders = db.folder().getUserFolders(message.account);

                        for (int i = 0; i < folders.size(); i++)
                            if (folders.get(i).id.equals(message.folder)) {
                                folders.remove(i);
                                break;
                            }

                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }

                    final Collator collator = Collator.getInstance(Locale.getDefault());
                    collator.setStrength(Collator.SECONDARY); // Case insensitive, process accents etc

                    Collections.sort(folders, new Comparator<EntityFolder>() {
                        @Override
                        public int compare(EntityFolder f1, EntityFolder f2) {
                            return collator.compare(f1.name, f2.name);
                        }
                    });

                    EntityFolder sent = db.folder().getFolderByType(message.account, EntityFolder.SENT);
                    if (sent != null && !message.folder.equals(sent.id))
                        folders.add(0, sent);

                    EntityFolder inbox = db.folder().getFolderByType(message.account, EntityFolder.INBOX);
                    if (inbox != null && !message.folder.equals(inbox.id))
                        folders.add(0, inbox);

                    return folders;
                }

                @Override
                protected void onLoaded(final Bundle args, List<EntityFolder> folders) {
                    View anchor = bnvActions.findViewById(R.id.action_move);
                    PopupMenu popupMenu = new PopupMenu(context, anchor);

                    int order = 0;
                    for (EntityFolder folder : folders) {
                        String name = (folder.display == null
                                ? Helper.localizeFolderName(context, folder.name)
                                : folder.display);
                        popupMenu.getMenu().add(Menu.NONE, folder.id.intValue(), order++, name);
                    }

                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(final MenuItem target) {
                            args.putLong("target", target.getItemId());

                            new SimpleTask<Void>() {
                                @Override
                                protected Void onLoad(Context context, Bundle args) {
                                    long id = args.getLong("id");
                                    long target = args.getLong("target");

                                    DB db = DB.getInstance(context);
                                    try {
                                        db.beginTransaction();

                                        db.message().setMessageUiHide(id, true);

                                        EntityMessage message = db.message().getMessage(id);
                                        EntityOperation.queue(db, message, EntityOperation.MOVE, target);

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

                            return true;
                        }
                    });

                    popupMenu.show();
                }
            }.load(context, owner, args);
        }

        private void onArchive(ActionData data) {
            Bundle args = new Bundle();
            args.putLong("id", data.message.id);

            new SimpleTask<Void>() {
                @Override
                protected Void onLoad(Context context, Bundle args) {
                    long id = args.getLong("id");

                    DB db = DB.getInstance(context);
                    try {
                        db.beginTransaction();

                        db.message().setMessageUiHide(id, true);

                        EntityMessage message = db.message().getMessage(id);
                        EntityFolder archive = db.folder().getFolderByType(message.account, EntityFolder.ARCHIVE);
                        EntityOperation.queue(db, message, EntityOperation.MOVE, archive.id);

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

        private void onReply(ActionData data) {
            context.startActivity(new Intent(context, ActivityCompose.class)
                    .putExtra("action", "reply")
                    .putExtra("reference", data.message.id));
        }

        ItemDetailsLookup.ItemDetails<Long> getItemDetails(@NonNull MotionEvent motionEvent) {
            return itemDetails;
        }
    }

    AdapterMessage(Context context, LifecycleOwner owner, FragmentManager fragmentManager, ViewType viewType, IProperties properties) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.owner = owner;
        this.fragmentManager = fragmentManager;
        this.viewType = viewType;
        this.properties = properties;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);


        this.contacts = (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED);
        this.avatars = (prefs.getBoolean("avatars", true) && this.contacts);
        this.compact = prefs.getBoolean("compact", false);
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
        return new ViewHolder(LayoutInflater.from(context).inflate(
                compact ? R.layout.item_message_compact : R.layout.item_message_normal,
                parent,
                false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.unwire();

        TupleMessageEx message = getItem(position);
        if (message == null)
            holder.clear();
        else {
            holder.bindTo(position, message);
            holder.wire();
        }
    }

    void setSelectionTracker(SelectionTracker<Long> selectionTracker) {
        this.selectionTracker = selectionTracker;
    }

    interface IProperties {
        void setExpanded(long id, boolean expand);

        void setHeaders(long id, boolean show);

        void setImages(long id, boolean show);

        boolean isExpanded(long id);

        boolean showHeaders(long id);

        boolean showImages(long id);
    }
}
