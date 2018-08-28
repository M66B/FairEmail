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
import android.content.SharedPreferences;
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
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class FragmentMessages extends FragmentEx {
    private ViewGroup view;
    private Button btnHintSwipe;
    private RecyclerView rvMessage;
    private TextView tvNoEmail;
    private ProgressBar pbWait;
    private Group grpHintSwipe;
    private Group grpReady;
    private FloatingActionButton fab;

    private long folder = -1;
    private long thread = -1;
    private String search = null;

    private SearchDataSource sds = null;

    private long primary = -1;
    private AdapterMessage adapter;

    private static final int MESSAGES_PAGE_SIZE = 50;
    private static final int SEARCH_PAGE_SIZE = 10;

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
        view = (ViewGroup) inflater.inflate(R.layout.fragment_messages, container, false);

        setHasOptionsMenu(true);

        // Get controls
        btnHintSwipe = view.findViewById(R.id.btnHintSwipe);
        rvMessage = view.findViewById(R.id.rvFolder);
        tvNoEmail = view.findViewById(R.id.tvNoEmail);
        pbWait = view.findViewById(R.id.pbWait);
        grpReady = view.findViewById(R.id.grpReady);
        grpHintSwipe = view.findViewById(R.id.grpHintSwipe);
        fab = view.findViewById(R.id.fab);

        // Wire controls

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        btnHintSwipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prefs.edit().putBoolean("understood_swipe", true).apply();
                grpHintSwipe.setVisibility(View.GONE);
            }
        });

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

        new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int pos = viewHolder.getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION)
                    return 0;

                TupleMessageEx message = ((AdapterMessage) rvMessage.getAdapter()).getCurrentList().get(pos);
                if (EntityFolder.OUTBOX.equals(message.folderType))
                    return 0;

                return makeMovementFlags(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    TupleMessageEx message = ((AdapterMessage) rvMessage.getAdapter()).getCurrentList().get(pos);
                    Log.i(Helper.TAG, "Swiped dir=" + direction + " message=" + message.id);

                    Bundle args = new Bundle();
                    args.putLong("id", message.id);
                    args.putInt("direction", direction);
                    new SimpleTask<String>() {
                        @Override
                        protected String onLoad(Context context, Bundle args) throws Throwable {
                            long id = args.getLong("id");
                            int direction = args.getInt("direction");
                            EntityFolder target = null;

                            DB db = DB.getInstance(context);
                            try {
                                db.beginTransaction();
                                EntityMessage message = db.message().getMessage(id);
                                EntityFolder folder = db.folder().getFolder(message.folder);

                                if (EntityFolder.ARCHIVE.equals(folder.type) || EntityFolder.TRASH.equals(folder.type))
                                    target = db.folder().getFolderByType(message.account, EntityFolder.INBOX);
                                else {
                                    if (direction == ItemTouchHelper.RIGHT)
                                        target = db.folder().getFolderByType(message.account, EntityFolder.ARCHIVE);
                                    if (direction == ItemTouchHelper.LEFT || target == null)
                                        target = db.folder().getFolderByType(message.account, EntityFolder.TRASH);
                                }

                                db.message().setMessageUiHide(message.id, true);
                                EntityOperation.queue(db, message, EntityOperation.MOVE, target.id);

                                db.setTransactionSuccessful();
                            } finally {
                                db.endTransaction();
                            }

                            EntityOperation.process(context);

                            return target.name;
                        }

                        @Override
                        protected void onLoaded(Bundle args, String folder) {
                            Snackbar.make(
                                    view,
                                    getString(R.string.title_moving, Helper.localizeFolderName(getContext(), folder)),
                                    Snackbar.LENGTH_SHORT).show();
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Toast.makeText(getContext(), ex.toString(), Toast.LENGTH_LONG).show();
                        }
                    }.load(FragmentMessages.this, args);
                }
            }
        }).attachToRecyclerView(rvMessage);

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

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        grpHintSwipe.setVisibility(prefs.getBoolean("understood_swipe", false) ? View.GONE : View.VISIBLE);

        final DB db = DB.getInstance(getContext());

        db.account().livePrimaryAccount().observe(getViewLifecycleOwner(), new Observer<EntityAccount>() {
            @Override
            public void onChanged(EntityAccount account) {
                primary = (account == null ? -1 : account.id);
                getActivity().invalidateOptionsMenu();
            }
        });

        LiveData<PagedList<TupleMessageEx>> messages;

        // Observe folder/messages/search
        if (TextUtils.isEmpty(search)) {
            boolean debug = prefs.getBoolean("debug", false);
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

                    messages = new LivePagedListBuilder<>(db.message().pagedUnifiedInbox(debug), MESSAGES_PAGE_SIZE).build();
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

                    messages = new LivePagedListBuilder<>(db.message().pagedFolder(folder, debug), MESSAGES_PAGE_SIZE).build();
                }
            else {
                setSubtitle(R.string.title_folder_thread);
                messages = new LivePagedListBuilder<>(db.message().pagedThread(thread, debug), MESSAGES_PAGE_SIZE).build();
            }
        } else {
            setSubtitle(getString(R.string.title_searching, search));

            // Searching is expensive:
            // - reuse existing data source
            // - use fragment lifecycle (instead of getViewLifecycleOwner)
            // - saving state is not feasible
            if (sds == null)
                sds = new SearchDataSource(getContext(), this, folder, search);

            messages = new LivePagedListBuilder<>(
                    new DataSource.Factory<Integer, TupleMessageEx>() {
                        @Override
                        public DataSource<Integer, TupleMessageEx> create() {
                            return sds;
                        }
                    },
                    new PagedList.Config.Builder()
                            .setEnablePlaceholders(true)
                            .setInitialLoadSizeHint(SEARCH_PAGE_SIZE)
                            .setPageSize(SEARCH_PAGE_SIZE)
                            .build()
            ).build();
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

        final MenuItem menuSearch = menu.findItem(R.id.menu_search);
        final SearchView searchView = (SearchView) menuSearch.getActionView();
        searchView.setQueryHint(getString(R.string.title_search_hint));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                menuSearch.collapseActionView();

                if (PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("pro", false)) {
                    Intent intent = new Intent();
                    intent.putExtra("folder", folder);
                    intent.putExtra("search", query);

                    FragmentMessages fragment = new FragmentMessages();
                    fragment.setArguments(intent.getExtras());
                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("search");
                    fragmentTransaction.commit();
                } else {
                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.content_frame, new FragmentPro()).addToBackStack("pro");
                    fragmentTransaction.commit();
                }

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
        menu.findItem(R.id.menu_search).setVisible(folder >= 0 && search == null);
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
