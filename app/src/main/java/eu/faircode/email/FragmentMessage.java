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

import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.Group;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Layout;
import android.text.Spannable;
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

public class FragmentMessage extends Fragment {
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
        final long id = getArguments().getLong("id");

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
                    Bundle args = new Bundle();
                    args.putString("link", link[0].getURL());

                    FragmentWebView fragment = new FragmentWebView();
                    fragment.setArguments(args);

                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("webview");
                    fragmentTransaction.commit();
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
                    case R.id.action_delete:
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

        final DB db = DB.getInstance(getContext());

        // Observe message
        db.message().liveMessage(id).observe(this, new Observer<TupleMessageEx>() {
            @Override
            public void onChanged(@Nullable TupleMessageEx message) {
                if (message == null || message.ui_hide) {
                    // Message gone (moved, deleted)
                    if (FragmentMessage.this.isVisible())
                        getFragmentManager().popBackStack();
                } else {
                    tvFrom.setText(MessageHelper.getFormattedAddresses(message.from));
                    tvTo.setText(MessageHelper.getFormattedAddresses(message.to));
                    tvCc.setText(MessageHelper.getFormattedAddresses(message.cc));
                    tvBcc.setText(MessageHelper.getFormattedAddresses(message.bcc));
                    tvTime.setText(message.sent == null ? null : df.format(new Date(message.sent)));
                    tvSubject.setText(message.subject);
                    tvCount.setText(Integer.toString(message.count));

                    int visibility = (message.ui_seen ? Typeface.NORMAL : Typeface.BOLD);
                    tvFrom.setTypeface(null, visibility);
                    tvTime.setTypeface(null, visibility);
                    tvSubject.setTypeface(null, visibility);
                    tvCount.setTypeface(null, visibility);

                    DB.getInstance(getContext()).attachment().liveAttachments(id).removeObservers(FragmentMessage.this);
                    DB.getInstance(getContext()).attachment().liveAttachments(id).observe(FragmentMessage.this,
                            new Observer<List<EntityAttachment>>() {
                                @Override
                                public void onChanged(@Nullable List<EntityAttachment> attachments) {
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
                }

                pbWait.setVisibility(View.GONE);
                grpReady.setVisibility(View.VISIBLE);
            }
        });

        // Setup attachments and bottom toolbar
        getLoaderManager().restartLoader(ActivityView.LOADER_MESSAGE_INIT, getArguments(), metaLoaderCallbacks).forceLoad();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Set subtitle
        getLoaderManager().restartLoader(ActivityView.LOADER_MESSAGE_INIT, getArguments(), metaLoaderCallbacks).forceLoad();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_address, menu);
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
        getLoaderManager().restartLoader(ActivityView.LOADER_MESSAGE_INIT, args, folderLoaderCallbacks).forceLoad();
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

    private static class MetaLoader extends AsyncTaskLoader<MetaData> {
        private Bundle args;

        MetaLoader(Context context) {
            super(context);
        }

        void setArgs(Bundle args) {
            this.args = args;
        }

        @Override
        public MetaData loadInBackground() {
            MetaData result = new MetaData();
            try {
                long id = args.getLong("id"); // message

                DB db = DB.getInstance(getContext());
                EntityMessage message = db.message().getMessage(id);
                result.folder = db.folder().getFolder(message.folder);
                result.hasTrash = (db.folder().getFolderByType(message.account, EntityFolder.TYPE_TRASH) != null);
                result.hasJunk = (db.folder().getFolderByType(message.account, EntityFolder.TYPE_JUNK) != null);
                result.hasArchive = (db.folder().getFolderByType(message.account, EntityFolder.TYPE_ARCHIVE) != null);
            } catch (Throwable ex) {
                Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                result.ex = ex;
            }
            return result;
        }
    }

    private LoaderManager.LoaderCallbacks metaLoaderCallbacks = new LoaderManager.LoaderCallbacks<MetaData>() {
        @NonNull
        @Override
        public Loader<MetaData> onCreateLoader(int id, Bundle args) {
            MetaLoader loader = new MetaLoader(getContext());
            loader.setArgs(args);
            return loader;
        }

        @Override
        public void onLoadFinished(@NonNull Loader<MetaData> loader, MetaData data) {
            getLoaderManager().destroyLoader(loader.getId());

            if (data.ex == null) {
                ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(data.folder == null
                        ? null
                        : Helper.localizeFolderName(getContext(), data.folder.name));

                boolean outbox = EntityFolder.TYPE_OUTBOX.equals(data.folder.type);

                bottom_navigation.setTag(data.folder.type); // trash or delete
                bottom_navigation.getMenu().findItem(R.id.action_delete).setVisible(data.hasJunk);
                bottom_navigation.getMenu().findItem(R.id.action_spam).setVisible(!outbox && data.hasJunk);
                bottom_navigation.getMenu().findItem(R.id.action_archive).setVisible(!outbox && data.hasArchive);
                bottom_navigation.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onLoaderReset(@NonNull Loader<MetaData> loader) {
        }
    };

    private static class MetaData {
        Throwable ex;
        EntityFolder folder;
        boolean hasTrash;
        boolean hasJunk;
        boolean hasArchive;
    }

    private static class FolderLoader extends AsyncTaskLoader<List<EntityFolder>> {
        private Bundle args;

        FolderLoader(Context context) {
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
                if (folders.get(i).id == message.folder) {
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
            if (message.folder != inbox.id)
                folders.add(0, inbox);

            return folders;
        }
    }

    private LoaderManager.LoaderCallbacks folderLoaderCallbacks = new LoaderManager.LoaderCallbacks<List<EntityFolder>>() {
        Bundle args;

        @NonNull
        @Override
        public Loader<List<EntityFolder>> onCreateLoader(int id, Bundle args) {
            this.args = args;
            FolderLoader loader = new FolderLoader(getContext());
            loader.setArgs(args);
            return loader;
        }

        @Override
        public void onLoadFinished(@NonNull Loader<List<EntityFolder>> loader, List<EntityFolder> folders) {
            getLoaderManager().destroyLoader(loader.getId());

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
