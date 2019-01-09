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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class FragmentAnswers extends FragmentEx {
    private RecyclerView rvAnswer;
    private ContentLoadingProgressBar pbWait;
    private Group grpReady;
    private FloatingActionButton fab;

    private AdapterAnswer adapter;

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_answers, container, false);

        setHasOptionsMenu(true);

        // Get controls
        rvAnswer = view.findViewById(R.id.rvAnswer);
        pbWait = view.findViewById(R.id.pbWait);
        grpReady = view.findViewById(R.id.grpReady);
        fab = view.findViewById(R.id.fab);

        // Wire controls

        rvAnswer.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rvAnswer.setLayoutManager(llm);

        adapter = new AdapterAnswer(getContext(), getViewLifecycleOwner());
        rvAnswer.setAdapter(adapter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.content_frame, new FragmentAnswer()).addToBackStack("answer");
                fragmentTransaction.commit();
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
        db.answer().liveAnswers().observe(getViewLifecycleOwner(), new Observer<List<EntityAnswer>>() {
            @Override
            public void onChanged(List<EntityAnswer> answers) {
                adapter.set(answers);
                pbWait.setVisibility(View.GONE);
                grpReady.setVisibility(View.VISIBLE);
            }
        });
    }
}
