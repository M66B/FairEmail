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

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

        // Get arguments
        Bundle args = getArguments();
        long folder = (args == null ? -1 : args.getLong("folder", -1));
        long thread = (args == null ? -1 : args.getLong("thread", -1)); // message ID

        // Observe folder/messages
        DB db = DB.getInstance(getContext());
        LiveData<PagedList<TupleMessageEx>> messages;
        boolean debug = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("debug", false);
        if (thread < 0)
            if (folder < 0) {
                setSubtitle(R.string.title_folder_unified);
                messages = new LivePagedListBuilder<>(db.message().pagedUnifiedInbox(debug), PAGE_SIZE).build();
            } else {
                db.folder().liveFolderEx(folder).observe(getViewLifecycleOwner(), new Observer<TupleFolderEx>() {
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

        messages.observe(getViewLifecycleOwner(), new Observer<PagedList<TupleMessageEx>>() {
            @Override
            public void onChanged(@Nullable PagedList<TupleMessageEx> messages) {
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

        new SimpleLoader() {
            @Override
            public Object onLoad(Bundle args) throws Throwable {
                long folder = (args == null ? -1 : args.getLong("folder", -1));
                long thread = (args == null ? -1 : args.getLong("thread", -1)); // message ID

                DB db = DB.getInstance(getContext());

                if (thread < 0)
                    if (folder < 0)
                        return db.folder().getPrimaryDrafts().account;
                    else
                        return db.folder().getFolder(folder).account;
                else
                    return db.message().getMessage(thread).account;
            }

            @Override
            public void onLoaded(Bundle args, Result result) {
                if (result.ex == null) {
                    fab.setTag(result.data);
                    fab.setVisibility(View.VISIBLE);
                } else
                    Toast.makeText(getContext(), result.ex.toString(), Toast.LENGTH_LONG).show();
            }
        }.load(this, ActivityView.LOADER_MESSAGE_ACCOUNT, getArguments());
    }
}
