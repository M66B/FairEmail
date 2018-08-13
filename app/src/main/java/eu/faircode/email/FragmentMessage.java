package eu.faircode.email;

/*
    This file is part of Safe email.

    Safe email is free software: you can redistribute it and/or modify
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
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Layout;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.Collator;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class FragmentMessage extends FragmentEx {
    private ViewGroup view;
    private TextView tvFrom;
    private TextView tvTime;
    private TextView tvSubject;
    private TextView tvCount;
    private TextView tvTo;
    private TextView tvCc;
    private TextView tvBcc;
    private RecyclerView rvAttachment;
    private TextView tvError;
    private BottomNavigationView top_navigation;
    private TextView tvBody;
    private BottomNavigationView bottom_navigation;
    private ProgressBar pbWait;
    private Group grpAddresses;
    private Group grpAttachments;
    private Group grpReady;

    private AdapterAttachment adapter;

    private boolean debug;
    private DateFormat df = SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = (ViewGroup) inflater.inflate(R.layout.fragment_message, container, false);

        // Get arguments
        Bundle args = getArguments();
        final long id = (args == null ? -1 : args.getLong("id"));
        debug = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("debug", false);

        // Get controls
        tvFrom = view.findViewById(R.id.tvFrom);
        tvTime = view.findViewById(R.id.tvTime);
        tvSubject = view.findViewById(R.id.tvSubject);
        tvCount = view.findViewById(R.id.tvCount);
        tvTo = view.findViewById(R.id.tvTo);
        tvCc = view.findViewById(R.id.tvCc);
        tvBcc = view.findViewById(R.id.tvBcc);
        rvAttachment = view.findViewById(R.id.rvAttachment);
        tvError = view.findViewById(R.id.tvError);
        top_navigation = view.findViewById(R.id.top_navigation);
        tvBody = view.findViewById(R.id.tvBody);
        bottom_navigation = view.findViewById(R.id.bottom_navigation);
        pbWait = view.findViewById(R.id.pbWait);
        grpAddresses = view.findViewById(R.id.grpAddresses);
        grpAttachments = view.findViewById(R.id.grpAttachments);
        grpReady = view.findViewById(R.id.grpReady);

        setHasOptionsMenu(true);

        tvBody.setMovementMethod(new LinkMovementMethod() {
            public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
                if (event.getAction() != MotionEvent.ACTION_UP)
                    return super.onTouchEvent(widget, buffer, event);

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

                    if (true) {
                        // https://developer.chrome.com/multidevice/android/customtabs
                        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                        builder.setToolbarColor(Helper.resolveColor(getContext(), R.attr.colorPrimary));

                        CustomTabsIntent customTabsIntent = builder.build();
                        customTabsIntent.launchUrl(getContext(), Uri.parse(url));
                    } else {
                        Bundle args = new Bundle();
                        args.putString("link", url);

                        FragmentWebView fragment = new FragmentWebView();
                        fragment.setArguments(args);

                        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("webview");
                        fragmentTransaction.commit();
                    }
                }
                return true;
            }
        });

        // Wire controls

        top_navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_thread:
                        onActionThread(id);
                        return true;
                    case R.id.action_seen:
                        onActionSeen(id);
                        return true;
                    case R.id.action_edit:
                        onActionEdit(id);
                        return true;
                    case R.id.action_forward:
                        onActionForward(id);
                        return true;
                    case R.id.action_reply_all:
                        onActionReplyAll(id);
                        return true;
                }
                return false;
            }
        });

        bottom_navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_spam:
                        onActionSpam(id);
                        return true;
                    case R.id.action_trash:
                        onActionDelete(id);
                        return true;
                    case R.id.action_move:
                        onActionMove(id);
                        return true;
                    case R.id.action_archive:
                        onActionArchive(id);
                        return true;
                    case R.id.action_reply:
                        onActionReply(id);
                        return true;
                }
                return false;
            }
        });

        // Initialize
        grpAddresses.setVisibility(View.GONE);
        grpAttachments.setVisibility(View.GONE);
        top_navigation.setVisibility(View.GONE);
        bottom_navigation.setVisibility(View.GONE);
        grpReady.setVisibility(View.GONE);
        pbWait.setVisibility(View.VISIBLE);

        rvAttachment.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rvAttachment.setLayoutManager(llm);

        adapter = new AdapterAttachment(getContext(), getViewLifecycleOwner());
        rvAttachment.setAdapter(adapter);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Get arguments
        Bundle args = getArguments();
        final long id = (args == null ? -1 : args.getLong("id"));

        final DB db = DB.getInstance(getContext());

        // Observe message
        db.message().liveMessage(id).observe(getViewLifecycleOwner(), new Observer<TupleMessageEx>() {
            @Override
            public void onChanged(@Nullable final TupleMessageEx message) {
                if (message == null || (!(debug && BuildConfig.DEBUG) && message.ui_hide)) {
                    // Message gone (moved, deleted)
                    if (FragmentMessage.this.isVisible())
                        getFragmentManager().popBackStack();
                } else {
                    setSubtitle(Helper.localizeFolderName(getContext(), message.folderName));

                    tvFrom.setText(message.from == null ? null : MessageHelper.getFormattedAddresses(message.from, true));
                    tvTime.setText(message.sent == null ? null : df.format(new Date(message.sent)));
                    tvSubject.setText(message.subject);

                    tvCount.setText(Integer.toString(message.count));
                    tvCount.setVisibility(message.count > 1 ? View.VISIBLE : View.GONE);

                    tvTo.setText(message.to == null ? null : MessageHelper.getFormattedAddresses(message.to, true));
                    tvCc.setText(message.cc == null ? null : MessageHelper.getFormattedAddresses(message.cc, true));
                    tvBcc.setText(message.bcc == null ? null : MessageHelper.getFormattedAddresses(message.bcc, true));

                    int typeface = (message.ui_seen ? Typeface.NORMAL : Typeface.BOLD);
                    tvFrom.setTypeface(null, typeface);
                    tvTime.setTypeface(null, typeface);
                    tvSubject.setTypeface(null, typeface);
                    tvCount.setTypeface(null, typeface);

                    int colorUnseen = Helper.resolveColor(getContext(), message.ui_seen
                            ? android.R.attr.textColorSecondary : R.attr.colorUnread);
                    tvFrom.setTextColor(colorUnseen);
                    tvTime.setTextColor(colorUnseen);

                    tvError.setText(message.error);
                    tvError.setVisibility(message.error == null ? View.GONE : View.VISIBLE);

                    MenuItem actionSeen = top_navigation.getMenu().findItem(R.id.action_seen);
                    actionSeen.setIcon(message.ui_seen
                            ? R.drawable.baseline_visibility_off_24
                            : R.drawable.baseline_visibility_24);
                    actionSeen.setTitle(message.ui_seen ? R.string.title_unseen : R.string.title_seen);

                    tvBody.setText(message.body == null
                            ? null
                            : Html.fromHtml(HtmlHelper.sanitize(getContext(), message.body, false)));

                    db.folder().liveFolders(message.account).removeObservers(getViewLifecycleOwner());
                    db.folder().liveFolders(message.account).observe(getViewLifecycleOwner(), new Observer<List<TupleFolderEx>>() {
                        @Override
                        public void onChanged(@Nullable List<TupleFolderEx> folders) {
                            if (folders == null)
                                folders = new ArrayList<>();

                            boolean inInbox = EntityFolder.INBOX.equals(message.folderType);
                            boolean inOutbox = EntityFolder.OUTBOX.equals(message.folderType);
                            boolean inArchive = EntityFolder.ARCHIVE.equals(message.folderType);
                            //boolean inDafts = EntityFolder.DRAFTS.equals(message.folderType);
                            boolean inTrash = EntityFolder.TRASH.equals(message.folderType);
                            boolean inJunk = EntityFolder.JUNK.equals(message.folderType);
                            //boolean inSent = EntityFolder.SENT.equals(message.folderType);

                            boolean hasTrash = false;
                            boolean hasJunk = false;
                            boolean hasArchive = false;
                            boolean hasUser = false;
                            if (folders != null)
                                for (EntityFolder folder : folders) {
                                    if (EntityFolder.TRASH.equals(folder.type))
                                        hasTrash = true;
                                    else if (EntityFolder.JUNK.equals(folder.type))
                                        hasJunk = true;
                                    else if (EntityFolder.ARCHIVE.equals(folder.type))
                                        hasArchive = true;
                                    else if (EntityFolder.USER.equals(folder.type))
                                        hasUser = true;
                                }

                            bottom_navigation.setTag(inTrash || !hasTrash);

                            top_navigation.getMenu().findItem(R.id.action_thread).setVisible(message.count > 1);
                            top_navigation.getMenu().findItem(R.id.action_seen).setVisible(!inOutbox);
                            top_navigation.getMenu().findItem(R.id.action_edit).setVisible(inTrash);
                            top_navigation.getMenu().findItem(R.id.action_forward).setVisible(!inOutbox);
                            top_navigation.getMenu().findItem(R.id.action_reply_all).setVisible(!inOutbox && message.cc != null);
                            top_navigation.setVisibility(View.VISIBLE);

                            bottom_navigation.getMenu().findItem(R.id.action_spam).setVisible(!inOutbox && !inArchive && !inJunk && hasJunk);
                            bottom_navigation.getMenu().findItem(R.id.action_trash).setVisible(!inOutbox && hasTrash);
                            bottom_navigation.getMenu().findItem(R.id.action_move).setVisible(!inOutbox && (!inInbox || hasUser));
                            bottom_navigation.getMenu().findItem(R.id.action_archive).setVisible(!inOutbox && !inArchive && hasArchive);
                            bottom_navigation.getMenu().findItem(R.id.action_reply).setVisible(!inOutbox);
                            bottom_navigation.setVisibility(View.VISIBLE);
                        }
                    });
                }

                pbWait.setVisibility(View.GONE);
                grpReady.setVisibility(View.VISIBLE);
            }
        });

        // Observe attachments
        db.attachment().liveAttachments(id).observe(getViewLifecycleOwner(),
                new Observer<List<EntityAttachment>>() {
                    @Override
                    public void onChanged(@Nullable List<EntityAttachment> attachments) {
                        if (attachments == null)
                            attachments = new ArrayList<>();

                        adapter.set(attachments);
                        grpAttachments.setVisibility(attachments.size() > 0 ? View.VISIBLE : View.GONE);
                    }
                });

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_view, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_addresses:
                onMenuAddresses();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onMenuAddresses() {
        if (grpReady.getVisibility() == View.VISIBLE)
            grpAddresses.setVisibility(grpAddresses.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
    }

    private void onActionThread(long id) {
        getFragmentManager().popBackStack("thread", FragmentManager.POP_BACK_STACK_INCLUSIVE);

        Bundle args = new Bundle();
        args.putLong("thread", id); // message ID

        FragmentMessages fragment = new FragmentMessages();
        fragment.setArguments(args);

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("thread");
        fragmentTransaction.commit();
    }

    private void onActionSeen(long id) {
        Helper.setViewsEnabled(view, false);

        Bundle args = new Bundle();
        args.putLong("id", id);

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

                        EntityOperation.queue(db, tmessage, EntityOperation.SEEN, tmessage.ui_seen);
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                EntityOperation.process(context);

                return null;
            }

            @Override
            protected void onLoaded(Bundle args, Void data) {
                Helper.setViewsEnabled(view, true);
            }

            @Override
            public void onException(Bundle args, Throwable ex) {
                Helper.setViewsEnabled(view, true);
                Toast.makeText(getContext(), ex.toString(), Toast.LENGTH_LONG).show();
            }
        }.load(this, args);
    }

    private void onActionEdit(final long id) {
        Helper.setViewsEnabled(view, false);

        Bundle args = new Bundle();
        args.putLong("id", id);

        new SimpleTask<Void>() {
            @Override
            protected Void onLoad(Context context, Bundle args) {
                long id = args.getLong("id");

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    EntityMessage draft = db.message().getMessage(id);
                    EntityFolder drafts = db.folder().getFolderByType(draft.account, EntityFolder.DRAFTS);
                    draft.id = null;
                    draft.folder = drafts.id;
                    draft.uid = null;
                    draft.id = db.message().insertMessage(draft);

                    EntityOperation.queue(db, draft, EntityOperation.ADD);

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                EntityOperation.process(context);

                return null;
            }

            @Override
            protected void onLoaded(Bundle args, Void data) {
                Helper.setViewsEnabled(view, true);
                getContext().startActivity(
                        new Intent(getContext(), ActivityCompose.class)
                                .putExtra("action", "edit")
                                .putExtra("id", id));
            }

            @Override
            public void onException(Bundle args, Throwable ex) {
                Helper.setViewsEnabled(view, true);
                Toast.makeText(getContext(), ex.toString(), Toast.LENGTH_LONG).show();
            }
        }.load(this, args);
    }

    private void onActionForward(long id) {
        startActivity(new Intent(getContext(), ActivityCompose.class)
                .putExtra("action", "forward")
                .putExtra("reference", id));
    }

    private void onActionReplyAll(long id) {
        startActivity(new Intent(getContext(), ActivityCompose.class)
                .putExtra("action", "reply_all")
                .putExtra("reference", id));
    }

    private void onActionSpam(final long id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder
                .setMessage(R.string.title_ask_spam)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Helper.setViewsEnabled(view, false);

                        Bundle args = new Bundle();
                        args.putLong("id", id);

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
                            protected void onLoaded(Bundle args, Void result) {
                                Helper.setViewsEnabled(view, true);
                            }

                            @Override
                            protected void onException(Bundle args, Throwable ex) {
                                Helper.setViewsEnabled(view, true);
                                Toast.makeText(getContext(), ex.toString(), Toast.LENGTH_LONG).show();
                            }
                        }.load(FragmentMessage.this, args);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null).show();
    }

    private void onActionDelete(final long id) {
        boolean delete = (Boolean) bottom_navigation.getTag();
        if (delete) {
            // No trash or is trash
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder
                    .setMessage(R.string.title_ask_delete)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Helper.setViewsEnabled(view, false);

                            Bundle args = new Bundle();
                            args.putLong("id", id);

                            new SimpleTask<Void>() {
                                @Override
                                protected Void onLoad(Context context, Bundle args) {
                                    long id = args.getLong("id");

                                    DB db = DB.getInstance(context);
                                    try {
                                        db.beginTransaction();

                                        db.message().setMessageUiHide(id, true);

                                        EntityMessage message = db.message().getMessage(id);
                                        EntityOperation.queue(db, message, EntityOperation.DELETE);

                                        db.setTransactionSuccessful();
                                    } finally {
                                        db.endTransaction();
                                    }

                                    EntityOperation.process(context);

                                    return null;
                                }

                                @Override
                                protected void onLoaded(Bundle args, Void result) {
                                    Helper.setViewsEnabled(view, true);
                                }

                                @Override
                                protected void onException(Bundle args, Throwable ex) {
                                    Helper.setViewsEnabled(view, true);
                                    Toast.makeText(getContext(), ex.toString(), Toast.LENGTH_LONG).show();
                                }
                            }.load(FragmentMessage.this, args);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null).show();
        } else {
            Helper.setViewsEnabled(view, false);

            Bundle args = new Bundle();
            args.putLong("id", id);

            new SimpleTask<Void>() {
                @Override
                protected Void onLoad(Context context, Bundle args) {
                    long id = args.getLong("id");
                    DB db = DB.getInstance(context);
                    try {
                        db.beginTransaction();

                        if (debug && BuildConfig.DEBUG)
                            db.message().deleteMessage(id);
                        else {
                            db.message().setMessageUiHide(id, true);

                            EntityMessage message = db.message().getMessage(id);
                            EntityFolder trash = db.folder().getFolderByType(message.account, EntityFolder.TRASH);
                            EntityOperation.queue(db, message, EntityOperation.MOVE, trash.id);
                        }

                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }

                    EntityOperation.process(context);

                    return null;
                }

                @Override
                protected void onLoaded(Bundle args, Void result) {
                    Helper.setViewsEnabled(view, true);
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Helper.setViewsEnabled(view, true);
                    Toast.makeText(getContext(), ex.toString(), Toast.LENGTH_LONG).show();
                }
            }.load(FragmentMessage.this, args);
        }
    }

    private void onActionMove(long id) {
        Bundle args = new Bundle();
        args.putLong("id", id);

        new SimpleTask<List<EntityFolder>>() {
            @Override
            protected List<EntityFolder> onLoad(Context context, Bundle args) {
                EntityMessage message;
                List<EntityFolder> folders;

                DB db = DB.getInstance(getContext());
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

                EntityFolder inbox = db.folder().getFolderByType(message.account, EntityFolder.INBOX);
                if (!message.folder.equals(inbox.id))
                    folders.add(0, inbox);

                return folders;
            }

            @Override
            protected void onLoaded(final Bundle args, List<EntityFolder> folders) {
                View anchor = bottom_navigation.findViewById(R.id.action_move);
                PopupMenu popupMenu = new PopupMenu(getContext(), anchor);

                int order = 0;
                for (EntityFolder folder : folders)
                    popupMenu.getMenu().add(Menu.NONE, folder.id.intValue(), order++,
                            Helper.localizeFolderName(getContext(), folder.name));

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(final MenuItem target) {
                        Helper.setViewsEnabled(view, false);

                        args.putLong("target", target.getItemId());

                        new SimpleTask<Void>() {
                            @Override
                            protected Void onLoad(Context context, Bundle args) {
                                long id = args.getLong("id");
                                long target = args.getLong("target");

                                DB db = DB.getInstance(context);
                                try {
                                    db.beginTransaction();

                                    EntityMessage message = db.message().getMessage(id);
                                    EntityFolder folder = db.folder().getFolder(message.folder);
                                    if (!EntityFolder.ARCHIVE.equals(folder.type))
                                        db.message().setMessageUiHide(message.id, true);

                                    EntityOperation.queue(db, message, EntityOperation.MOVE, target);

                                    db.setTransactionSuccessful();
                                } finally {
                                    db.endTransaction();
                                }

                                EntityOperation.process(context);

                                return null;
                            }

                            @Override
                            protected void onLoaded(Bundle args, Void result) {
                                Helper.setViewsEnabled(view, true);
                            }

                            @Override
                            protected void onException(Bundle args, Throwable ex) {
                                Helper.setViewsEnabled(view, true);
                                Toast.makeText(getContext(), ex.toString(), Toast.LENGTH_LONG).show();
                            }
                        }.load(FragmentMessage.this, args);

                        return true;
                    }
                });

                popupMenu.show();
            }
        }.load(FragmentMessage.this, args);
    }

    private void onActionArchive(long id) {
        Helper.setViewsEnabled(view, false);

        Bundle args = new Bundle();
        args.putLong("id", id);

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
            protected void onLoaded(Bundle args, Void result) {
                Helper.setViewsEnabled(view, true);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.setViewsEnabled(view, true);
                Toast.makeText(getContext(), ex.toString(), Toast.LENGTH_LONG).show();
            }
        }.load(FragmentMessage.this, args);
    }

    private void onActionReply(long id) {
        startActivity(new Intent(getContext(), ActivityCompose.class)
                .putExtra("action", "reply")
                .putExtra("reference", id));
    }
}
