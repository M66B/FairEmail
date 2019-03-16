package eu.faircode.email;

/*
    This file is part of FairEmail.

    FairEmail is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FairEmail is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with FairEmail.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2018-2019 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.Group;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class FragmentContacts extends FragmentBase {
    private RecyclerView rvContacts;
    private ContentLoadingProgressBar pbWait;
    private Group grpReady;

    private String search = null;
    private AdapterContact adapter;

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.menu_contacts);
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_contacts, container, false);

        // Get controls
        rvContacts = view.findViewById(R.id.rvContacts);
        pbWait = view.findViewById(R.id.pbWait);
        grpReady = view.findViewById(R.id.grpReady);

        // Wire controls

        rvContacts.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rvContacts.setLayoutManager(llm);

        adapter = new AdapterContact(getContext(), getViewLifecycleOwner());
        rvContacts.setAdapter(adapter);

        // Initialize
        grpReady.setVisibility(View.GONE);
        pbWait.setVisibility(View.VISIBLE);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            search = savedInstanceState.getString("fair:search");
            adapter.search(search);
        }

        DB db = DB.getInstance(getContext());
        db.contact().liveContacts().observe(getViewLifecycleOwner(), new Observer<List<EntityContact>>() {
            @Override
            public void onChanged(List<EntityContact> contacts) {
                if (contacts == null)
                    contacts = new ArrayList<>();

                adapter.set(contacts);

                pbWait.setVisibility(View.GONE);
                grpReady.setVisibility(View.VISIBLE);
            }
        });

        Shortcuts.update(getContext(), getViewLifecycleOwner());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("fair:search", search);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_contacts, menu);

        MenuItem menuSearch = menu.findItem(R.id.menu_search);
        SearchView searchView = (SearchView) menuSearch.getActionView();

        if (!TextUtils.isEmpty(search)) {
            menuSearch.expandActionView();
            searchView.setQuery(search, true);
            searchView.clearFocus();
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                search = query;
                adapter.search(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                search = newText;
                adapter.search(newText);
                return true;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_delete:
                onDelete();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onDelete() {
        final View dview = LayoutInflater.from(getContext()).inflate(R.layout.dialog_message, null);
        final TextView tvMessage = dview.findViewById(R.id.tvMessage);

        tvMessage.setText(getText(R.string.title_delete_contacts));

        new DialogBuilderLifecycle(getContext(), getViewLifecycleOwner())
                .setView(dview)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new SimpleTask<Void>() {
                            @Override
                            protected Void onExecute(Context context, Bundle args) {
                                int count = DB.getInstance(context).contact().clearContacts();
                                Log.i("Cleared contacts=" + count);
                                return null;
                            }

                            @Override
                            protected void onException(Bundle args, Throwable ex) {
                                Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
                            }
                        }.execute(getContext(), getViewLifecycleOwner(), new Bundle(), "setup:privacy");
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
