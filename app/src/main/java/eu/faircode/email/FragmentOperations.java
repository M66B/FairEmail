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

    Copyright 2018-2025 by Marcel Bokhorst (M66B)
*/

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FragmentOperations extends FragmentBase {
    private TextView tvNoOperation;
    private RecyclerView rvOperation;
    private ContentLoadingProgressBar pbWait;
    private Group grpReady;

    private AdapterOperation adapter;

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.menu_operations);
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_operations, container, false);

        // Get controls
        tvNoOperation = view.findViewById(R.id.tvNoOperation);
        rvOperation = view.findViewById(R.id.rvOperation);
        pbWait = view.findViewById(R.id.pbWait);
        grpReady = view.findViewById(R.id.grpReady);

        // Wire controls

        rvOperation.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rvOperation.setLayoutManager(llm);

        adapter = new AdapterOperation(this);
        rvOperation.setAdapter(adapter);

        // Initialize
        tvNoOperation.setVisibility(View.GONE);
        grpReady.setVisibility(View.GONE);
        pbWait.setVisibility(View.VISIBLE);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Observe folders
        DB db = DB.getInstance(getContext());
        db.operation().liveOperations().observe(getViewLifecycleOwner(), new Observer<List<TupleOperationEx>>() {
            @Override
            public void onChanged(@Nullable List<TupleOperationEx> operations) {
                if (operations == null)
                    operations = new ArrayList<>();

                tvNoOperation.setVisibility(operations.size() == 0 ? View.VISIBLE : View.GONE);
                adapter.set(operations);

                pbWait.setVisibility(View.GONE);
                grpReady.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_operations, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_help) {
            onMenuHelp();
            return true;
        } else if (itemId == R.id.menu_delete) {
            new FragmentDialogOperationsDelete().show(getParentFragmentManager(), "operations:delete");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onMenuHelp() {
        Helper.viewFAQ(getContext(), 3);
    }
}
