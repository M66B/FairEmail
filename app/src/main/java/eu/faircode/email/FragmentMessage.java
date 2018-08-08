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
import android.text.Html;
import android.text.Layout;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.Collator;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class FragmentMessage extends FragmentEx {
    private TextView tvTime;
    private TextView tvFrom;
    private TextView tvTo;
    private TextView tvCc;
    private TextView tvBcc;
    private RecyclerView rvAttachment;
    private TextView tvSubject;
    private TextView tvCount;
    private BottomNavigationView top_navigation;
    private TextView tvBody;
    private BottomNavigationView bottom_navigation;
    private ProgressBar pbWait;
    private Group grpAddress;
    private Group grpAttachments;
    private Group grpReady;

    private AdapterAttachment adapter;

    private ExecutorService executor = Executors.newCachedThreadPool();
    private DateFormat df = SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message, container, false);

        // Get arguments
        Bundle args = getArguments();
        final long id = (args == null ? -1 : args.getLong("id"));

        // Get controls
        tvFrom = view.findViewById(R.id.tvFrom);
        tvTo = view.findViewById(R.id.tvTo);
        tvCc = view.findViewById(R.id.tvCc);
        tvBcc = view.findViewById(R.id.tvBcc);
        rvAttachment = view.findViewById(R.id.rvAttachment);
        tvTime = view.findViewById(R.id.tvTime);
        tvSubject = view.findViewById(R.id.tvSubject);
        tvCount = view.findViewById(R.id.tvCount);
        top_navigation = view.findViewById(R.id.top_navigation);
        tvBody = view.findViewById(R.id.tvBody);
        bottom_navigation = view.findViewById(R.id.bottom_navigation);
        pbWait = view.findViewById(R.id.pbWait);
        grpAddress = view.findViewById(R.id.grpAddress);
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
                    case R.id.action_seen:
                        onActionSeen(id);
                        return true;
                    case R.id.action_thread:
                        onActionThread(id);
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
                    case R.id.action_trash:
                        onActionDelete(id);
                        return true;
                    case R.id.action_spam:
                        onActionSpam(id);
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
        grpAddress.setVisibility(View.GONE);
        grpAttachments.setVisibility(View.GONE);
        bottom_navigation.setVisibility(View.GONE);
        grpReady.setVisibility(View.GONE);
        pbWait.setVisibility(View.VISIBLE);

        rvAttachment.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rvAttachment.setLayoutManager(llm);

        adapter = new AdapterAttachment(getContext());
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
                if (message == null || message.ui_hide) {
                    // Message gone (moved, deleted)
                    if (FragmentMessage.this.isVisible())
                        getFragmentManager().popBackStack();
                } else {
                    setSubtitle(Helper.localizeFolderName(getContext(), message.folderName));

                    tvFrom.setText(message.from == null ? null : TextUtils.join(", ", message.from));
                    tvTo.setText(message.to == null ? null : TextUtils.join(", ", message.to));
                    tvCc.setText(message.cc == null ? null : TextUtils.join(", ", message.cc));
                    tvBcc.setText(message.bcc == null ? null : TextUtils.join(", ", message.bcc));
                    tvTime.setText(message.sent == null ? null : df.format(new Date(message.sent)));
                    tvSubject.setText(message.subject);
                    tvCount.setText(Integer.toString(message.count));

                    int typeface = (message.ui_seen ? Typeface.NORMAL : Typeface.BOLD);
                    tvFrom.setTypeface(null, typeface);
                    tvTime.setTypeface(null, typeface);
                    tvSubject.setTypeface(null, typeface);
                    tvCount.setTypeface(null, typeface);

                    int colorUnseen = Helper.resolveColor(getContext(), message.ui_seen
                            ? android.R.attr.textColorSecondary : R.attr.colorUnread);
                    tvFrom.setTextColor(colorUnseen);
                    tvTime.setTextColor(colorUnseen);

                    db.attachment().liveAttachments(id).removeObservers(getViewLifecycleOwner());
                    db.attachment().liveAttachments(id).observe(getViewLifecycleOwner(),
                            new Observer<List<TupleAttachment>>() {
                                @Override
                                public void onChanged(@Nullable List<TupleAttachment> attachments) {
                                    adapter.set(attachments);
                                    grpAttachments.setVisibility(attachments.size() > 0 ? View.VISIBLE : View.GONE);
                                }
                            });

                    top_navigation.getMenu().findItem(R.id.action_thread).setVisible(message.count > 1);

                    MenuItem actionSeen = top_navigation.getMenu().findItem(R.id.action_seen);
                    actionSeen.setIcon(message.ui_seen
                            ? R.drawable.baseline_visibility_off_24
                            : R.drawable.baseline_visibility_24);
                    actionSeen.setTitle(message.ui_seen ? R.string.title_unseen : R.string.title_seen);

                    tvBody.setText(message.body == null
                            ? null
                            : Html.fromHtml(HtmlHelper.sanitize(getContext(), message.body, false)));

                    bottom_navigation.setTag(message.folderType);

                    db.folder().liveFolders(message.account).removeObservers(getViewLifecycleOwner());
                    db.folder().liveFolders(message.account).observe(getViewLifecycleOwner(), new Observer<List<TupleFolderEx>>() {
                        @Override
                        public void onChanged(@Nullable final List<TupleFolderEx> folders) {
                            boolean hasTrash = false;
                            boolean hasJunk = false;
                            boolean hasArchive = false;
                            boolean hasUser = false;
                            for (EntityFolder folder : folders) {
                                if (EntityFolder.TYPE_TRASH.equals(folder.type))
                                    hasTrash = true;
                                else if (EntityFolder.TYPE_JUNK.equals(folder.type))
                                    hasJunk = true;
                                else if (EntityFolder.TYPE_ARCHIVE.equals(folder.type))
                                    hasArchive = true;
                                else if (EntityFolder.TYPE_USER.equals(folder.type))
                                    hasUser = true;
                            }

                            final boolean inbox = EntityFolder.TYPE_INBOX.equals(message.folderType);
                            final boolean outbox = EntityFolder.TYPE_OUTBOX.equals(message.folderType);

                            bottom_navigation.getMenu().findItem(R.id.action_trash).setVisible(hasTrash);
                            bottom_navigation.getMenu().findItem(R.id.action_spam).setVisible(!outbox && hasJunk);
                            bottom_navigation.getMenu().findItem(R.id.action_move).setVisible(!outbox && (!inbox || hasUser));
                            bottom_navigation.getMenu().findItem(R.id.action_archive).setVisible(!outbox && hasArchive);
                            bottom_navigation.setVisibility(View.VISIBLE);
                        }
                    });
                }

                pbWait.setVisibility(View.GONE);
                grpReady.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_message, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_address:
                onMenuAddress();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onMenuAddress() {
        if (grpReady.getVisibility() == View.VISIBLE)
            grpAddress.setVisibility(grpAddress.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
    }

    private void onActionSeen(final long id) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    DB db = DB.getInstance(getContext());
                    EntityMessage message = db.message().getMessage(id);
                    message.ui_seen = !message.ui_seen;
                    db.message().updateMessage(message);
                    EntityOperation.queue(getContext(), message, EntityOperation.SEEN, message.ui_seen);
                    EntityOperation.process(getContext());
                } catch (Throwable ex) {
                    Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                }
            }
        });
    }

    private void onActionThread(long id) {
        Bundle args = new Bundle();
        args.putLong("thread", id); // message ID

        FragmentMessages fragment = new FragmentMessages();
        fragment.setArguments(args);

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("thread");
        fragmentTransaction.commit();
    }

    private void onActionForward(long id) {
        startActivity(new Intent(getContext(), ActivityCompose.class)
                .putExtra("id", id)
                .putExtra("action", "forward"));
    }

    private void onActionReplyAll(long id) {
        startActivity(new Intent(getContext(), ActivityCompose.class)
                .putExtra("id", id)
                .putExtra("action", "reply_all"));
    }

    private void onActionDelete(final long id) {
        String folderType = (String) bottom_navigation.getTag();
        if (EntityFolder.TYPE_TRASH.equals(folderType)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder
                    .setMessage(R.string.title_ask_delete)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            executor.submit(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        DB db = DB.getInstance(getContext());
                                        EntityMessage message = db.message().getMessage(id);
                                        message.ui_hide = true;
                                        db.message().updateMessage(message);

                                        EntityOperation.queue(getContext(), message, EntityOperation.DELETE);
                                        EntityOperation.process(getContext());
                                    } catch (Throwable ex) {
                                        Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                                    }
                                }
                            });
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null).show();
        } else {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        DB db = DB.getInstance(getContext());
                        EntityMessage message = db.message().getMessage(id);
                        message.ui_hide = true;
                        db.message().updateMessage(message);

                        EntityFolder trash = db.folder().getFolderByType(message.account, EntityFolder.TYPE_TRASH);
                        EntityOperation.queue(getContext(), message, EntityOperation.MOVE, trash.id);
                        EntityOperation.process(getContext());
                    } catch (Throwable ex) {
                        Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                    }
                }
            });
        }
    }

    private void onActionSpam(final long id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder
                .setMessage(R.string.title_ask_spam)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        executor.submit(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    DB db = DB.getInstance(getContext());
                                    EntityMessage message = db.message().getMessage(id);
                                    message.ui_hide = true;
                                    db.message().updateMessage(message);

                                    EntityFolder spam = db.folder().getFolderByType(message.account, EntityFolder.TYPE_JUNK);
                                    EntityOperation.queue(getContext(), message, EntityOperation.MOVE, spam.id);
                                    EntityOperation.process(getContext());
                                } catch (Throwable ex) {
                                    Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                                }
                            }
                        });
                    }
                })
                .setNegativeButton(android.R.string.cancel, null).show();
    }

    private void onActionMove(final long id) {
        Bundle args = new Bundle();
        args.putLong("id", id);
        LoaderManager.getInstance(this)
                .restartLoader(ActivityView.LOADER_MESSAGE_MOVE, args, moveLoaderCallbacks).forceLoad();
    }

    private void onActionArchive(final long id) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    DB db = DB.getInstance(getContext());
                    EntityMessage message = db.message().getMessage(id);
                    message.ui_hide = true;
                    db.message().updateMessage(message);

                    EntityFolder archive = db.folder().getFolderByType(message.account, EntityFolder.TYPE_ARCHIVE);
                    EntityOperation.queue(getContext(), message, EntityOperation.MOVE, archive.id);
                    EntityOperation.process(getContext());
                } catch (Throwable ex) {
                    Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                }
            }
        });
    }

    private void onActionReply(long id) {
        startActivity(new Intent(getContext(), ActivityCompose.class)
                .putExtra("id", id)
                .putExtra("action", "reply"));
    }

    private static class MoveLoader extends AsyncTaskLoader<List<EntityFolder>> {
        private Bundle args;

        MoveLoader(Context context) {
            super(context);
        }

        void setArgs(Bundle args) {
            this.args = args;
        }

        @Override
        public List<EntityFolder> loadInBackground() {
            DB db = DB.getInstance(getContext());
            EntityMessage message = db.message().getMessage(args.getLong("id"));
            List<EntityFolder> folders = db.folder().getUserFolders(message.account);

            for (int i = 0; i < folders.size(); i++)
                if (folders.get(i).id.equals(message.folder)) {
                    folders.remove(i);
                    break;
                }

            final Collator collator = Collator.getInstance(Locale.getDefault());
            collator.setStrength(Collator.SECONDARY); // Case insensitive, process accents etc

            Collections.sort(folders, new Comparator<EntityFolder>() {
                @Override
                public int compare(EntityFolder f1, EntityFolder f2) {
                    return collator.compare(f1.name, f2.name);
                }
            });

            EntityFolder inbox = db.folder().getFolderByType(message.account, EntityFolder.TYPE_INBOX);
            if (!message.folder.equals(inbox.id))
                folders.add(0, inbox);

            return folders;
        }
    }

    private LoaderManager.LoaderCallbacks moveLoaderCallbacks = new LoaderManager.LoaderCallbacks<List<EntityFolder>>() {
        Bundle args;

        @NonNull
        @Override
        public Loader<List<EntityFolder>> onCreateLoader(int id, Bundle args) {
            this.args = args;
            MoveLoader loader = new MoveLoader(getContext());
            loader.setArgs(args);
            return loader;
        }

        @Override
        public void onLoadFinished(@NonNull Loader<List<EntityFolder>> loader, List<EntityFolder> folders) {
            LoaderManager.getInstance(FragmentMessage.this).destroyLoader(loader.getId());

            View anchor = top_navigation.findViewById(R.id.action_thread);
            PopupMenu popupMenu = new PopupMenu(getContext(), anchor);
            int order = 0;
            for (EntityFolder folder : folders)
                popupMenu.getMenu().add(Menu.NONE, folder.id.intValue(), order++,
                        Helper.localizeFolderName(getContext(), folder.name));

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    final long folder = item.getItemId();
                    executor.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                DB db = DB.getInstance(getContext());
                                EntityMessage message = db.message().getMessage(args.getLong("id"));
                                message.ui_hide = true;
                                db.message().updateMessage(message);

                                EntityOperation.queue(getContext(), message, EntityOperation.MOVE, folder);
                                EntityOperation.process(getContext());
                            } catch (Throwable ex) {
                                Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                            }
                        }
                    });

                    return true;
                }
            });

            popupMenu.show();
        }

        @Override
        public void onLoaderReset(@NonNull Loader<List<EntityFolder>> loader) {
        }
    };
}
