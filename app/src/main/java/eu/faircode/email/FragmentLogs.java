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

    Copyright 2018 by Marcel Bokhorst (M66B)
*/

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class FragmentLogs extends FragmentEx {
    private RecyclerView rvLog;
    private ContentLoadingProgressBar pbWait;
    private Group grpReady;

    private AdapterLog adapter;

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_log);

        View view = inflater.inflate(R.layout.fragment_logs, container, false);

        // Get controls
        rvLog = view.findViewById(R.id.rvLog);
        pbWait = view.findViewById(R.id.pbWait);
        grpReady = view.findViewById(R.id.grpReady);

        // Wire controls

        rvLog.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rvLog.setLayoutManager(llm);

        adapter = new AdapterLog(getContext());
        rvLog.setAdapter(adapter);

        // Initialize
        grpReady.setVisibility(View.GONE);
        pbWait.show();

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

                pbWait.hide();
                grpReady.setVisibility(View.VISIBLE);
            }
        });
    }
}
