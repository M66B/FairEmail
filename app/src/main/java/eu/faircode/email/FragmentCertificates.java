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

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.lifecycle.Observer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class FragmentCertificates extends FragmentBase {
    private RecyclerView rvCertificate;
    private ContentLoadingProgressBar pbWait;
    private Group grpReady;
    private FloatingActionButton fab;

    private AdapterCertificate adapter;

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_advanced_manage_certificates);
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_certificates, container, false);

        // Get controls
        rvCertificate = view.findViewById(R.id.rvCertificate);
        pbWait = view.findViewById(R.id.pbWait);
        grpReady = view.findViewById(R.id.grpReady);
        fab = view.findViewById(R.id.fab);

        // Wire controls

        rvCertificate.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rvCertificate.setLayoutManager(llm);

        DividerItemDecoration itemDecorator = new DividerItemDecoration(getContext(), llm.getOrientation());
        itemDecorator.setDrawable(getContext().getDrawable(R.drawable.divider));
        rvCertificate.addItemDecoration(itemDecorator);

        adapter = new AdapterCertificate(this);
        rvCertificate.setAdapter(adapter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
                lbm.sendBroadcast(new Intent(ActivitySetup.ACTION_IMPORT_CERTIFICATE));
            }
        });

        // Initialize
        grpReady.setVisibility(View.GONE);
        pbWait.setVisibility(View.VISIBLE);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        DB db = DB.getInstance(getContext());
        db.certificate().liveCertificates().observe(getViewLifecycleOwner(), new Observer<List<EntityCertificate>>() {
            @Override
            public void onChanged(List<EntityCertificate> certificates) {
                if (certificates == null)
                    certificates = new ArrayList<>();

                adapter.set(certificates);

                pbWait.setVisibility(View.GONE);
                grpReady.setVisibility(View.VISIBLE);
            }
        });
    }
}
