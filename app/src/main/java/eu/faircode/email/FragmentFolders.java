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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class FragmentFolders extends FragmentEx {
    private RecyclerView rvFolder;
    private ProgressBar pbWait;
    private Group grpReady;
    private FloatingActionButton fab;

    private AdapterFolder adapter;

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_folders, container, false);

        // Get controls
        rvFolder = view.findViewById(R.id.rvFolder);
        pbWait = view.findViewById(R.id.pbWait);
        grpReady = view.findViewById(R.id.grpReady);
        fab = view.findViewById(R.id.fab);

        // Wire controls

        rvFolder.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rvFolder.setLayoutManager(llm);

        adapter = new AdapterFolder(getContext());
        rvFolder.setAdapter(adapter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
        fab.setVisibility(View.GONE);

        // Initialize
        grpReady.setVisibility(View.GONE);
        pbWait.setVisibility(View.VISIBLE);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Get arguments
        Bundle args = getArguments();
        long account = (args == null ? -1 : args.getLong("account"));

        DB db = DB.getInstance(getContext());

        // Observe account
        db.account().liveAccount(account).observe(getViewLifecycleOwner(), new Observer<EntityAccount>() {
            @Override
            public void onChanged(@Nullable EntityAccount account) {
                setSubtitle(account.name);
            }
        });

        // Observe folders
        db.folder().liveFolders(account).observe(getViewLifecycleOwner(), new Observer<List<TupleFolderEx>>() {
            @Override
            public void onChanged(@Nullable List<TupleFolderEx> folders) {
                adapter.set(folders);

                pbWait.setVisibility(View.GONE);
                grpReady.setVisibility(View.VISIBLE);
            }
        });

        // Show hint
        // TODO: find better solution
        Toast.makeText(getContext(), R.string.title_item_edit_hint, Toast.LENGTH_SHORT).show();
    }
}
