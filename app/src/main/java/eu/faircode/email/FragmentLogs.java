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

    Copyright 2018-2020 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FragmentLogs extends FragmentBase {
    private RecyclerView rvLog;
    private ContentLoadingProgressBar pbWait;
    private Group grpReady;

    private boolean autoScroll = true;
    private AdapterLog adapter;

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_log);
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_logs, container, false);

        // Get controls
        rvLog = view.findViewById(R.id.rvLog);
        pbWait = view.findViewById(R.id.pbWait);
        grpReady = view.findViewById(R.id.grpReady);

        // Wire controls

        rvLog.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rvLog.setLayoutManager(llm);

        adapter = new AdapterLog(this);
        rvLog.setAdapter(adapter);

        // Initialize
        grpReady.setVisibility(View.GONE);
        pbWait.setVisibility(View.VISIBLE);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        long from = new Date().getTime() - 24 * 3600 * 1000L;

        DB db = DB.getInstance(getContext());
        db.log().liveLogs(from).observe(getViewLifecycleOwner(), new Observer<List<EntityLog>>() {
            @Override
            public void onChanged(List<EntityLog> logs) {
                if (logs == null)
                    logs = new ArrayList<>();

                adapter.set(logs);
                if (autoScroll)
                    rvLog.scrollToPosition(0);

                pbWait.setVisibility(View.GONE);
                grpReady.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_logs, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_auto_scroll).setChecked(autoScroll);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_auto_scroll:
                autoScroll = !item.isChecked();
                item.setChecked(autoScroll);
                return true;

            case R.id.menu_clear:
                onMenuClear();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onMenuClear() {
        Bundle args = new Bundle();

        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) {
                DB db = DB.getInstance(context);

                db.log().deleteLogs(new Date().getTime());

                return null;
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "log:clear");
    }
}
