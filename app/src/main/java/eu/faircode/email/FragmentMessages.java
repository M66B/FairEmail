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
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.Group;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

public class FragmentMessages extends Fragment {
    private RecyclerView rvMessage;
    private TextView tvNoEmail;
    private ProgressBar pbWait;
    private Group grpReady;
    private FloatingActionButton fab;

    private AdapterMessage adapter;
    private LiveData<TupleFolderEx> liveFolder;

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages, container, false);

        // Get arguments
        long folder = getArguments().getLong("folder", -1);
        long thread = getArguments().getLong("thread", -1); // message ID

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

        DB db = DB.getInstance(getContext());

        // Observe folder
        liveFolder = (thread < 0 ? DB.getInstance(getContext()).folder().liveFolderEx(folder) : null);

        // Observe messages
        if (thread < 0)
            if (folder < 0)
                db.message().liveUnifiedInbox().observe(this, messagesObserver);
            else
                db.message().liveMessages(folder).observe(this, messagesObserver);
        else {
            db.message().liveThread(thread).observe(this, messagesObserver);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (liveFolder == null)
            ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(R.string.title_folder_thread);
        else
            liveFolder.observe(this, folderObserver);

        getLoaderManager().restartLoader(ActivityView.LOADER_MESSAGES_INIT, new Bundle(), initLoaderCallbacks).forceLoad();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (liveFolder != null)
            liveFolder.removeObservers(this);
    }

    Observer<TupleFolderEx> folderObserver = new Observer<TupleFolderEx>() {
        @Override
        public void onChanged(@Nullable TupleFolderEx folder) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(folder.name == null
                    ? getString(R.string.title_folder_unified)
                    : Helper.localizeFolderName(getContext(), folder));
        }
    };

    Observer<List<TupleMessageEx>> messagesObserver = new Observer<List<TupleMessageEx>>() {
        @Override
        public void onChanged(@Nullable List<TupleMessageEx> messages) {
            adapter.set(messages);

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
    };

    private static class InitLoader extends AsyncTaskLoader<Bundle> {
        public InitLoader(@NonNull Context context) {
            super(context);
        }

        @Nullable
        @Override
        public Bundle loadInBackground() {
            Bundle result = new Bundle();
            EntityFolder drafts = DB.getInstance(getContext()).folder().getPrimaryDraftFolder();
            result.putBoolean("drafts", drafts != null);
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
