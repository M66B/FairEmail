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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Layout;
import android.text.Spannable;
import android.text.Spanned;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
    private TextView tvSize;
    private TextView tvTime;
    private TextView tvTo;
    private TextView tvSubject;
    private TextView tvCount;
    private TextView tvReplyTo;
    private TextView tvCc;
    private TextView tvBcc;
    private RecyclerView rvAttachment;
    private TextView tvError;
    private View vSeparatorBody;
    private TextView tvBody;
    private FloatingActionButton fab;
    private BottomNavigationView bottom_navigation;
    private ProgressBar pbWait;
    private Group grpHeader;
    private Group grpAddresses;
    private Group grpAttachments;
    private Group grpMessage;

    private TupleMessageEx message = null;
    private boolean free = false;
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

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        debug = prefs.getBoolean("debug", false);

        // Get controls
        tvFrom = view.findViewById(R.id.tvFrom);
        tvSize = view.findViewById(R.id.tvSize);
        tvTime = view.findViewById(R.id.tvTime);
        tvTo = view.findViewById(R.id.tvTo);
        tvSubject = view.findViewById(R.id.tvSubject);
        tvCount = view.findViewById(R.id.tvCount);
        tvReplyTo = view.findViewById(R.id.tvReplyTo);
        tvCc = view.findViewById(R.id.tvCc);
        tvBcc = view.findViewById(R.id.tvBcc);
        rvAttachment = view.findViewById(R.id.rvAttachment);
        tvError = view.findViewById(R.id.tvError);
        vSeparatorBody = view.findViewById(R.id.vSeparatorBody);
        tvBody = view.findViewById(R.id.tvBody);
        fab = view.findViewById(R.id.fab);
        bottom_navigation = view.findViewById(R.id.bottom_navigation);
        pbWait = view.findViewById(R.id.pbWait);
        grpHeader = view.findViewById(R.id.grpHeader);
        grpAddresses = view.findViewById(R.id.grpAddresses);
        grpAttachments = view.findViewById(R.id.grpAttachments);
        grpMessage = view.findViewById(R.id.grpMessage);

        setHasOptionsMenu(true);

        tvCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onMenuThread(message.id);
            }
        });

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

                    if (prefs.getBoolean("webview", false)) {
                        Bundle args = new Bundle();
                        args.putString("link", url);

                        FragmentWebView fragment = new FragmentWebView();
                        fragment.setArguments(args);

                        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("webview");
                        fragmentTransaction.commit();
                    } else {
                        // https://developer.chrome.com/multidevice/android/customtabs
                        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                        builder.setToolbarColor(Helper.resolveColor(getContext(), R.attr.colorPrimary));

                        CustomTabsIntent customTabsIntent = builder.build();
                        customTabsIntent.launchUrl(getContext(), Uri.parse(url));
                    }
                }
                return true;
            }
        });

        // Wire controls

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                free = true;
                getActivity().invalidateOptionsMenu();

                grpHeader.setVisibility(View.GONE);
                vSeparatorBody.setVisibility(View.GONE);
                fab.setVisibility(View.GONE);

                tvCount.setTag(tvCount.getVisibility());
                tvCc.setTag(grpAddresses.getVisibility());
                tvError.setTag(tvError.getVisibility());

                tvCount.setVisibility(View.GONE);
                grpAddresses.setVisibility(View.GONE);
                grpAttachments.setVisibility(View.GONE);
                tvError.setVisibility(View.GONE);
            }
        });

        ((ActivityBase) getActivity()).addBackPressedListener(new ActivityBase.IBackPressedListener() {
            @Override
            public boolean onBackPressed() {
                if (free && isVisible()) {
                    free = false;
                    getActivity().invalidateOptionsMenu();

                    grpHeader.setVisibility(View.VISIBLE);
                    vSeparatorBody.setVisibility(View.VISIBLE);
                    fab.setVisibility(View.VISIBLE);

                    RecyclerView.Adapter adapter = rvAttachment.getAdapter();

                    tvCount.setVisibility((int) tvCount.getTag());
                    grpAddresses.setVisibility((int) tvCc.getTag());
                    grpAttachments.setVisibility(adapter != null && adapter.getItemCount() > 0 ? View.VISIBLE : View.GONE);
                    tvError.setVisibility((int) tvError.getTag());

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
        grpHeader.setVisibility(View.GONE);
        grpAddresses.setVisibility(View.GONE);
        grpAttachments.setVisibility(View.GONE);
        grpMessage.setVisibility(View.GONE);
        bottom_navigation.setVisibility(View.GONE);
        tvCount.setVisibility(View.GONE);
        tvError.setVisibility(View.GONE);
        fab.setVisibility(View.GONE);
        pbWait.setVisibility(View.VISIBLE);

        tvSize.setText(null);

        rvAttachment.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rvAttachment.setLayoutManager(llm);

        adapter = new AdapterAttachment(getContext(), getViewLifecycleOwner(), true);
        rvAttachment.setAdapter(adapter);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("free", free);
        if (free) {
            outState.putInt("tag_count", (int) tvCount.getTag());
            outState.putInt("tag_cc", (int) tvCc.getTag());
            outState.putInt("tag_error", (int) tvError.getTag());
        }
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
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
                    finish();
                    return;
                }

                FragmentMessage.this.message = message;
                setSubtitle(Helper.localizeFolderName(getContext(), message.folderName));

                if (savedInstanceState == null) {
                    tvFrom.setText(message.from == null ? null : MessageHelper.getFormattedAddresses(message.from, true));
                    tvTime.setText(message.sent == null ? null : df.format(new Date(message.sent)));
                    tvTo.setText(message.to == null ? null : MessageHelper.getFormattedAddresses(message.to, true));
                    tvSubject.setText(message.subject);

                    tvCount.setText(Integer.toString(message.count));

                    tvReplyTo.setText(message.reply == null ? null : MessageHelper.getFormattedAddresses(message.reply, true));
                    tvCc.setText(message.cc == null ? null : MessageHelper.getFormattedAddresses(message.cc, true));
                    tvBcc.setText(message.bcc == null ? null : MessageHelper.getFormattedAddresses(message.bcc, true));

                    tvError.setText(message.error);
                } else {
                    free = savedInstanceState.getBoolean("free");
                    if (free) {
                        tvCount.setTag(savedInstanceState.getInt("tag_count"));
                        tvCc.setTag(savedInstanceState.getInt("tag_cc"));
                        rvAttachment.setTag(savedInstanceState.getInt("tag_attachment"));
                        tvError.setTag(savedInstanceState.getInt("tag_error"));
                    }
                }

                getActivity().invalidateOptionsMenu();

                Bundle args = new Bundle();
                args.putLong("id", message.id);

                new SimpleTask<Spanned>() {
                    @Override
                    protected Spanned onLoad(Context context, Bundle args) throws Throwable {
                        String body = EntityMessage.read(context, args.getLong("id"));
                        args.putInt("size", body.length());
                        return Html.fromHtml(HtmlHelper.sanitize(getContext(), body, false));
                    }

                    @Override
                    protected void onLoaded(Bundle args, Spanned body) {
                        tvSize.setText(Helper.humanReadableByteCount(args.getInt("size"), false));
                        tvBody.setText(body);
                        grpMessage.setVisibility(View.VISIBLE);
                        if (!free)
                            fab.setVisibility(View.VISIBLE);
                    }
                }.load(FragmentMessage.this, args);

                int typeface = (message.ui_seen ? Typeface.NORMAL : Typeface.BOLD);
                tvFrom.setTypeface(null, typeface);
                tvTime.setTypeface(null, typeface);
                tvSubject.setTypeface(null, typeface);
                tvCount.setTypeface(null, typeface);

                int colorUnseen = Helper.resolveColor(getContext(), message.ui_seen
                        ? android.R.attr.textColorSecondary : R.attr.colorUnread);
                tvFrom.setTextColor(colorUnseen);
                tvTime.setTextColor(colorUnseen);

                pbWait.setVisibility(View.GONE);

                grpHeader.setVisibility(free ? View.GONE : View.VISIBLE);
                vSeparatorBody.setVisibility(free ? View.GONE : View.VISIBLE);
                fab.setVisibility(free ? View.GONE : View.VISIBLE);

                if (free) {
                    tvCount.setVisibility((int) tvCount.getTag());
                    grpAddresses.setVisibility((int) tvCc.getTag());
                    tvError.setVisibility((int) tvError.getTag());
                } else {
                    tvCount.setVisibility(!free && message.count > 1 ? View.VISIBLE : View.GONE);
                    tvError.setVisibility(free || message.error == null ? View.GONE : View.VISIBLE);
                }

                // Observe attachments
                db.attachment().liveAttachments(id).removeObservers(getViewLifecycleOwner());
                db.attachment().liveAttachments(id).observe(getViewLifecycleOwner(),
                        new Observer<List<EntityAttachment>>() {
                            @Override
                            public void onChanged(@Nullable List<EntityAttachment> attachments) {
                                if (attachments == null)
                                    attachments = new ArrayList<>();

                                adapter.set(attachments);
                                grpAttachments.setVisibility(!free && attachments.size() > 0 ? View.VISIBLE : View.GONE);
                            }
                        });

                db.folder().liveFolders(message.account).removeObservers(getViewLifecycleOwner());
                db.folder().liveFolders(message.account).observe(getViewLifecycleOwner(), new Observer<List<TupleFolderEx>>() {
                    @Override
                    public void onChanged(@Nullable List<TupleFolderEx> folders) {
                        if (folders == null)
                            folders = new ArrayList<>();

                        boolean inInbox = EntityFolder.INBOX.equals(message.folderType);
                        boolean inOutbox = EntityFolder.OUTBOX.equals(message.folderType);
                        boolean inArchive = EntityFolder.ARCHIVE.equals(message.folderType);
                        boolean inTrash = EntityFolder.TRASH.equals(message.folderType);
                        boolean inJunk = EntityFolder.JUNK.equals(message.folderType);

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

                        bottom_navigation.getMenu().findItem(R.id.action_spam).setVisible(message.uid != null && !inOutbox && !inArchive && !inJunk && hasJunk);
                        bottom_navigation.getMenu().findItem(R.id.action_trash).setVisible(message.uid != null && !inOutbox && hasTrash);
                        bottom_navigation.getMenu().findItem(R.id.action_move).setVisible(message.uid != null && !inOutbox && (!inInbox || hasUser));
                        bottom_navigation.getMenu().findItem(R.id.action_archive).setVisible(message.uid != null && !inOutbox && !inArchive && hasArchive);
                        bottom_navigation.getMenu().findItem(R.id.action_reply).setVisible(!inOutbox);
                        bottom_navigation.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_view, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        boolean inOutbox = (message != null && EntityFolder.OUTBOX.equals(message.folderType));

        menu.findItem(R.id.menu_addresses).setVisible(!free);
        menu.findItem(R.id.menu_thread).setVisible(!free && message != null && message.count > 1);
        menu.findItem(R.id.menu_seen).setVisible(!free && message != null && !inOutbox);
        menu.findItem(R.id.menu_forward).setVisible(!free && message != null && !inOutbox);
        menu.findItem(R.id.menu_reply_all).setVisible(!free && message != null && message.cc != null && !inOutbox);

        if (message != null) {
            MenuItem menuSeen = menu.findItem(R.id.menu_seen);
            menuSeen.setIcon(message.ui_seen
                    ? R.drawable.baseline_visibility_off_24
                    : R.drawable.baseline_visibility_24);
            menuSeen.setTitle(message.ui_seen ? R.string.title_unseen : R.string.title_seen);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_addresses:
                onMenuAddresses();
                return true;
            case R.id.menu_thread:
                onMenuThread(message.id);
                return true;
            case R.id.menu_seen:
                onMenuSeen(message.id);
                return true;
            case R.id.menu_forward:
                onMenuForward(message.id);
                return true;
            case R.id.menu_reply_all:
                onMenuReplyAll(message.id);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onMenuAddresses() {
        grpAddresses.setVisibility(grpAddresses.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
    }

    private void onMenuThread(long id) {
        getFragmentManager().popBackStack("thread", FragmentManager.POP_BACK_STACK_INCLUSIVE);

        Bundle args = new Bundle();
        args.putLong("thread", id); // message ID

        FragmentMessages fragment = new FragmentMessages();
        fragment.setArguments(args);

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("thread");
        fragmentTransaction.commit();
    }

    private void onMenuSeen(long id) {
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
                    for (EntityMessage tmessage : db.message().getMessageByThread(message.account, message.thread))
                        if (message.uid != null) { // Skip drafts and outbox
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

    private void onMenuForward(long id) {
        startActivity(new Intent(getContext(), ActivityCompose.class)
                .putExtra("action", "forward")
                .putExtra("reference", id));
    }

    private void onMenuReplyAll(long id) {
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

                        new SimpleTask<Boolean>() {
                            @Override
                            protected Boolean onLoad(Context context, Bundle args) {
                                long id = args.getLong("id");
                                long target = args.getLong("target");

                                boolean close;

                                DB db = DB.getInstance(context);
                                try {
                                    db.beginTransaction();

                                    EntityMessage message = db.message().getMessage(id);
                                    EntityFolder folder = db.folder().getFolder(message.folder);

                                    close = EntityFolder.ARCHIVE.equals(folder.type);
                                    if (!close)
                                        db.message().setMessageUiHide(message.id, true);

                                    EntityOperation.queue(db, message, EntityOperation.MOVE, target);

                                    db.setTransactionSuccessful();
                                } finally {
                                    db.endTransaction();
                                }

                                EntityOperation.process(context);

                                return close;
                            }

                            @Override
                            protected void onLoaded(Bundle args, Boolean close) {
                                Helper.setViewsEnabled(view, true);
                                if (close) // archived message
                                    getFragmentManager().popBackStack();
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
                getFragmentManager().popBackStack();
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
