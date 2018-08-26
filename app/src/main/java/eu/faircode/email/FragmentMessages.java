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
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.UIDFolder;
import javax.mail.internet.MimeMessage;
import javax.mail.search.BodyTerm;
import javax.mail.search.OrTerm;
import javax.mail.search.SubjectTerm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.paging.DataSource;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;
import androidx.paging.PositionalDataSource;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class FragmentMessages extends FragmentEx {
    private RecyclerView rvMessage;
    private TextView tvNoEmail;
    private ProgressBar pbWait;
    private Group grpReady;
    private FloatingActionButton fab;

    private long folder = -1;
    private long thread = -1;
    private String search = null;
    private long primary = -1;
    private AdapterMessage adapter;

    private static final int PAGE_SIZE = 50;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get arguments
        Bundle args = getArguments();
        if (args != null) {
            folder = args.getLong("folder", -1);
            thread = args.getLong("thread", -1); // message ID
            search = args.getString("search");
        }
    }

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages, container, false);

        setHasOptionsMenu(true);

        // Get controls
        rvMessage = view.findViewById(R.id.rvFolder);
        tvNoEmail = view.findViewById(R.id.tvNoEmail);
        pbWait = view.findViewById(R.id.pbWait);
        grpReady = view.findViewById(R.id.grpReady);
        fab = view.findViewById(R.id.fab);

        // Wire controls

        rvMessage.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rvMessage.setLayoutManager(llm);

        AdapterMessage.ViewType viewType;
        if (TextUtils.isEmpty(search))
            if (thread < 0)
                if (folder < 0)
                    viewType = AdapterMessage.ViewType.UNIFIED;
                else
                    viewType = AdapterMessage.ViewType.FOLDER;
            else
                viewType = AdapterMessage.ViewType.THREAD;
        else
            viewType = AdapterMessage.ViewType.SEARCH;

        adapter = new AdapterMessage(getContext(), getViewLifecycleOwner(), viewType);
        rvMessage.setAdapter(adapter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), ActivityCompose.class)
                        .putExtra("action", "new")
                        .putExtra("account", (Long) fab.getTag())
                );
            }
        });

        // Initialize
        tvNoEmail.setVisibility(View.GONE);
        grpReady.setVisibility(View.GONE);
        pbWait.setVisibility(View.VISIBLE);
        fab.setVisibility(View.GONE);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final DB db = DB.getInstance(getContext());

        db.account().livePrimaryAccount().observe(getViewLifecycleOwner(), new Observer<EntityAccount>() {
            @Override
            public void onChanged(EntityAccount account) {
                primary = (account == null ? -1 : account.id);
                getActivity().invalidateOptionsMenu();
            }
        });

        // Observe folder/messages/search
        LiveData<PagedList<TupleMessageEx>> messages;
        if (TextUtils.isEmpty(search)) {
            boolean debug = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("debug", false);
            if (thread < 0)
                if (folder < 0) {
                    db.folder().liveUnified().observe(getViewLifecycleOwner(), new Observer<List<TupleFolderEx>>() {
                        @Override
                        public void onChanged(List<TupleFolderEx> folders) {
                            int unseen = 0;
                            if (folders != null)
                                for (TupleFolderEx folder : folders)
                                    unseen += folder.unseen;
                            String name = getString(R.string.title_folder_unified);
                            if (unseen > 0)
                                setSubtitle(getString(R.string.title_folder_unseen, name, unseen));
                            else
                                setSubtitle(name);
                        }
                    });

                    messages = new LivePagedListBuilder<>(db.message().pagedUnifiedInbox(debug), PAGE_SIZE).build();
                } else {
                    db.folder().liveFolderEx(folder).observe(getViewLifecycleOwner(), new Observer<TupleFolderEx>() {
                        @Override
                        public void onChanged(@Nullable TupleFolderEx folder) {
                            if (folder == null)
                                setSubtitle(null);
                            else {
                                String name = Helper.localizeFolderName(getContext(), folder.name);
                                if (folder.unseen > 0)
                                    setSubtitle(getString(R.string.title_folder_unseen, name, folder.unseen));
                                else
                                    setSubtitle(name);
                            }
                        }
                    });

                    messages = new LivePagedListBuilder<>(db.message().pagedFolder(folder, debug), PAGE_SIZE).build();
                }
            else {
                setSubtitle(R.string.title_folder_thread);
                messages = new LivePagedListBuilder<>(db.message().pagedThread(thread, debug), PAGE_SIZE).build();
            }
        } else {
            setSubtitle(getString(R.string.title_searching, search));

            DataSource.Factory<Integer, TupleMessageEx> dsf = new DataSource.Factory<Integer, TupleMessageEx>() {
                @Override
                public DataSource<Integer, TupleMessageEx> create() {
                    return new PositionalDataSource<TupleMessageEx>() {
                        @Override
                        public void loadInitial(LoadInitialParams params, LoadInitialCallback<TupleMessageEx> callback) {
                            Log.i(Helper.TAG, "loadInitial(" + params.requestedStartPosition + ", " + params.requestedLoadSize + ")");
                            callback.onResult(search(search, params.requestedStartPosition, params.requestedLoadSize), params.requestedStartPosition);
                        }

                        @Override
                        public void loadRange(LoadRangeParams params, LoadRangeCallback<TupleMessageEx> callback) {
                            Log.i(Helper.TAG, "loadRange(" + params.startPosition + ", " + params.loadSize + ")");
                            callback.onResult(search(search, params.startPosition, params.loadSize));
                        }

                        private List<TupleMessageEx> search(String term, int from, int count) {
                            List<TupleMessageEx> list = new ArrayList<>();
                            IMAPStore istore = null;
                            try {
                                DB db = DB.getInstance(getContext());

                                EntityFolder f = db.folder().getFolder(folder);
                                EntityAccount account = db.account().getAccount(f.account);

                                Properties props = MessageHelper.getSessionProperties();
                                Session isession = Session.getInstance(props, null);
                                Log.i(Helper.TAG, "Connecting to account=" + account.name);
                                istore = (IMAPStore) isession.getStore("imaps");
                                istore.connect(account.host, account.port, account.user, account.password);

                                Log.i(Helper.TAG, "Opening folder=" + f.name);
                                IMAPFolder ifolder = (IMAPFolder) istore.getFolder(f.name);
                                ifolder.open(Folder.READ_WRITE);

                                Log.i(Helper.TAG, "Search for term=" + term);
                                Message[] imessages = ifolder.search(
                                        new OrTerm(
                                                new SubjectTerm(term),
                                                new BodyTerm(term)));
                                Log.i(Helper.TAG, "Found messages=" + imessages.length);

                                List<Message> selected = new ArrayList<>();
                                int base = imessages.length - 1 - from;
                                for (int i = base; i >= 0 && i >= base - count - 1; i--)
                                    selected.add(imessages[i]);
                                Log.i(Helper.TAG, "Selected messages=" + selected.size());

                                FetchProfile fp = new FetchProfile();
                                fp.add(UIDFolder.FetchProfileItem.UID);
                                fp.add(IMAPFolder.FetchProfileItem.FLAGS);
                                fp.add(FetchProfile.Item.ENVELOPE);
                                fp.add(FetchProfile.Item.CONTENT_INFO);
                                fp.add(IMAPFolder.FetchProfileItem.HEADERS);
                                fp.add(IMAPFolder.FetchProfileItem.MESSAGE);
                                ifolder.fetch(selected.toArray(new Message[0]), fp);

                                for (Message imessage : selected) {
                                    long uid = ifolder.getUID(imessage);
                                    Log.i(Helper.TAG, "Get uid=" + uid);

                                    MessageHelper helper = new MessageHelper((MimeMessage) imessage);
                                    boolean seen = helper.getSeen();

                                    TupleMessageEx message = new TupleMessageEx();
                                    message.id = uid;
                                    message.account = f.account;
                                    message.folder = f.id;
                                    message.uid = uid;
                                    message.msgid = helper.getMessageID();
                                    message.references = TextUtils.join(" ", helper.getReferences());
                                    message.inreplyto = helper.getInReplyTo();
                                    message.thread = helper.getThreadId(uid);
                                    message.from = helper.getFrom();
                                    message.to = helper.getTo();
                                    message.cc = helper.getCc();
                                    message.bcc = helper.getBcc();
                                    message.reply = helper.getReply();
                                    message.subject = imessage.getSubject();
                                    message.received = imessage.getReceivedDate().getTime();
                                    message.sent = (imessage.getSentDate() == null ? null : imessage.getSentDate().getTime());
                                    message.seen = seen;
                                    message.ui_seen = seen;
                                    message.ui_hide = false;

                                    message.accountName = account.name;
                                    message.folderName = f.name;
                                    message.folderType = f.type;
                                    message.count = 1;
                                    message.unseen = (seen ? 0 : 1);
                                    message.attachments = 0;

                                    list.add(message);
                                }
                            } catch (Throwable ex) {
                                Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                            } finally {
                                if (istore != null)
                                    try {
                                        istore.close();
                                    } catch (MessagingException ex) {
                                        Log.w(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                                    }
                            }

                            return list;
                        }

                    };
                }
            };

            PagedList.Config.Builder plcb = new PagedList.Config.Builder()
                    .setEnablePlaceholders(false)
                    .setInitialLoadSizeHint(10)
                    .setPageSize(PAGE_SIZE);

            messages = new LivePagedListBuilder<>(dsf, plcb.build()).build();
        }

        messages.observe(getViewLifecycleOwner(), new Observer<PagedList<TupleMessageEx>>() {
            @Override
            public void onChanged(@Nullable PagedList<TupleMessageEx> messages) {
                if (messages == null) {
                    finish();
                    return;
                }

                Log.i(Helper.TAG, "Submit messages=" + messages.size());
                adapter.submitList(messages);

                pbWait.setVisibility(View.GONE);
                grpReady.setVisibility(View.VISIBLE);

                if (messages.size() == 0) {
                    tvNoEmail.setVisibility(View.VISIBLE);
                    rvMessage.setVisibility(View.GONE);
                } else {
                    tvNoEmail.setVisibility(View.GONE);
                    rvMessage.setVisibility(View.VISIBLE);
                }
            }
        });

        Bundle args = new Bundle();
        args.putLong("folder", folder);
        args.putLong("thread", thread);

        new SimpleTask<Long>() {
            @Override
            protected Long onLoad(Context context, Bundle args) {
                long folder = args.getLong("folder", -1);
                long thread = args.getLong("thread", -1); // message ID

                DB db = DB.getInstance(context);

                Long account = null;
                if (thread < 0) {
                    if (folder >= 0)
                        account = db.folder().getFolder(folder).account;
                } else
                    account = db.message().getMessage(thread).account;

                if (account == null) {
                    // outbox
                    EntityFolder primary = db.folder().getPrimaryDrafts();
                    if (primary != null)
                        account = primary.account;
                }

                return account;
            }

            @Override
            protected void onLoaded(Bundle args, Long account) {
                if (account != null) {
                    fab.setTag(account);
                    fab.setVisibility(View.VISIBLE);
                }
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Toast.makeText(getContext(), ex.toString(), Toast.LENGTH_LONG).show();
            }
        }.load(this, args);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_list, menu);

        // TODO: search hint
        final MenuItem menuSearch = menu.findItem(R.id.menu_search);
        final SearchView searchView = (SearchView) menuSearch.getActionView();
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                menuSearch.collapseActionView();

                Intent intent = new Intent();
                intent.putExtra("folder", folder);
                intent.putExtra("search", query);

                FragmentMessages fragment = new FragmentMessages();
                fragment.setArguments(intent.getExtras());
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("search");
                fragmentTransaction.commit();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_search).setVisible(folder >= 0);
        menu.findItem(R.id.menu_folders).setVisible(primary >= 0);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_folders:
                onMenuFolders();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onMenuFolders() {
        getFragmentManager().popBackStack("unified", 0);

        Bundle args = new Bundle();
        args.putLong("account", primary);

        FragmentFolders fragment = new FragmentFolders();
        fragment.setArguments(args);

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("folders");
        fragmentTransaction.commit();
    }
}
