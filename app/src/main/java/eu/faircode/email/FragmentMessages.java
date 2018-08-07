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

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.Group;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

public class FragmentMessages extends FragmentEx {
    private RecyclerView rvMessage;
    private TextView tvNoEmail;
    private ProgressBar pbWait;
    private Group grpReady;
    private FloatingActionButton fab;

    private AdapterMessage adapter;

    private static final int PAGE_SIZE = 100;

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages, container, false);

        // Get arguments
        Bundle args = getArguments();
        long folder = (args == null ? -1 : args.getLong("folder", -1));
        long thread = (args == null ? -1 : args.getLong("thread", -1)); // message ID

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

        adapter = new AdapterMessage(getContext(),
                thread < 0
                        ? AdapterMessage.ViewType.FOLDER
                        : AdapterMessage.ViewType.THREAD);
        rvMessage.setAdapter(adapter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), ActivityCompose.class));
            }
        });

        // Initialize
        tvNoEmail.setVisibility(View.GONE);
        grpReady.setVisibility(View.GONE);
        pbWait.setVisibility(View.VISIBLE);
        fab.setVisibility(View.GONE);

        // Observe folder/messages
        DB db = DB.getInstance(getContext());
        LiveData<PagedList<TupleMessageEx>> messages;
        boolean debug = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("debug", false);
        if (thread < 0)
            if (folder < 0) {
                setSubtitle(R.string.title_folder_unified);
                messages = new LivePagedListBuilder<>(db.message().pagedUnifiedInbox(debug), PAGE_SIZE).build();
            } else {
                db.folder().liveFolderEx(folder).observe(this, new Observer<TupleFolderEx>() {
                    @Override
                    public void onChanged(@Nullable TupleFolderEx folder) {
                        setSubtitle(folder == null ? null : Helper.localizeFolderName(getContext(), folder.name));
                    }
                });
                messages = new LivePagedListBuilder<>(db.message().pagedFolder(folder, debug), PAGE_SIZE).build();
            }
        else {
            setSubtitle(R.string.title_folder_thread);
            messages = new LivePagedListBuilder<>(db.message().pagedThread(thread, debug), PAGE_SIZE).build();
        }

        messages.observe(this, new Observer<PagedList<TupleMessageEx>>() {
            @Override
            public void onChanged(@Nullable PagedList<TupleMessageEx> messages) {
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

        getLoaderManager().restartLoader(ActivityView.LOADER_MESSAGES_INIT, new Bundle(), initLoaderCallbacks).forceLoad();

        return view;
    }

    private static class InitLoader extends AsyncTaskLoader<Bundle> {
        InitLoader(@NonNull Context context) {
            super(context);
        }

        @Nullable
        @Override
        public Bundle loadInBackground() {
            Bundle result = new Bundle();
            try {
                EntityFolder drafts = DB.getInstance(getContext()).folder().getPrimaryFolder(EntityFolder.TYPE_DRAFTS);
                result.putBoolean("drafts", drafts != null);
            } catch (Throwable ex) {
                Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                result.putBoolean("drafts", false);
            }
            return result;
        }
    }

    private LoaderManager.LoaderCallbacks initLoaderCallbacks = new LoaderManager.LoaderCallbacks<Bundle>() {
        @NonNull
        @Override
        public Loader<Bundle> onCreateLoader(int id, @Nullable Bundle args) {
            return new InitLoader(getContext());
        }

        @Override
        public void onLoadFinished(@NonNull Loader<Bundle> loader, Bundle data) {
            fab.setVisibility(data.getBoolean("drafts", false) ? View.VISIBLE : View.GONE);
        }

        @Override
        public void onLoaderReset(@NonNull Loader<Bundle> loader) {
        }
    };
}
