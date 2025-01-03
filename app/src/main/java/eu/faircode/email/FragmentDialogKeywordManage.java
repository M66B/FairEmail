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

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FragmentDialogKeywordManage extends FragmentDialogBase {
    private View dview;
    private RecyclerView rvKeyword;
    private FloatingActionButton fabAdd;
    private ContentLoadingProgressBar pbWait;
    private AdapterKeyword adapter;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Bundle args = getArguments();

        final Context context = getContext();
        dview = LayoutInflater.from(context).inflate(R.layout.dialog_keyword_manage, null);
        rvKeyword = dview.findViewById(R.id.rvKeyword);
        fabAdd = dview.findViewById(R.id.fabAdd);
        pbWait = dview.findViewById(R.id.pbWait);

        rvKeyword.setHasFixedSize(false);
        final LinearLayoutManager llm = new LinearLayoutManager(context);
        rvKeyword.setLayoutManager(llm);

        adapter = new AdapterKeyword(context, getViewLifecycleOwner());
        rvKeyword.setAdapter(adapter);

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentDialogKeywordAdd fragment = new FragmentDialogKeywordAdd();
                fragment.setArguments(args);
                fragment.setTargetFragment(FragmentDialogKeywordManage.this, 1);
                fragment.show(getParentFragmentManager(), "keyword:add");
            }
        });

        pbWait.setVisibility(View.VISIBLE);

        long[] ids = args.getLongArray("ids");
        if (ids.length == 1) {
            DB db = DB.getInstance(context);
            db.message().liveMessageKeywords(ids[0]).observe(getViewLifecycleOwner(), new Observer<TupleKeyword.Persisted>() {
                @Override
                public void onChanged(TupleKeyword.Persisted data) {
                    if (data == null)
                        data = new TupleKeyword.Persisted();

                    pbWait.setVisibility(View.GONE);
                    adapter.set(ids, TupleKeyword.from(context, data));
                }
            });
        } else {
            task.execute(context, getViewLifecycleOwner(), args, "keywords:get");
        }

        return new AlertDialog.Builder(context)
                .setIcon(R.drawable.twotone_label_important_24)
                .setTitle(R.string.title_manage_keywords)
                .setView(dview)
                .setNegativeButton(R.string.title_setup_done, null)
                .create();
    }

    @Override
    public void onResume() {
        super.onResume();
        Dialog dialog = getDialog();
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        //dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        task.execute(getContext(), getViewLifecycleOwner(), getArguments(), "keywords:get");
    }

    final SimpleTask<TupleKeyword.Persisted> task = new SimpleTask<TupleKeyword.Persisted>() {
        @Override
        protected TupleKeyword.Persisted onExecute(Context context, Bundle args) {
            long[] ids = args.getLongArray("ids");

            List<String> all = new ArrayList<>();
            List<String> selected = new ArrayList<>();
            List<String> available = new ArrayList<>();

            DB db = DB.getInstance(context);
            if (ids != null)
                for (long id : ids) {
                    TupleKeyword.Persisted kws = db.message().getMessageKeywords(id);
                    List<String> list = (kws == null || kws.selected == null
                            ? Collections.emptyList() : Arrays.asList(kws.selected));
                    if (id == ids[0]) // First
                        selected.addAll(list);
                    else // Check if all message have all keywords
                        for (String kw : new ArrayList<>(selected))
                            if (!list.contains(kw))
                                selected.remove(kw);

                    for (String kw : list)
                        if (!all.contains(kw))
                            all.add(kw);

                    if (kws != null && kws.available != null)
                        for (String kw : kws.available)
                            if (!available.contains(kw))
                                available.add(kw);
                }

            return new TupleKeyword.Persisted(selected, all, available);
        }

        @Override
        protected void onExecuted(Bundle args, TupleKeyword.Persisted data) {
            pbWait.setVisibility(View.GONE);
            adapter.set(args.getLongArray("ids"), TupleKeyword.from(getContext(), data));
        }

        @Override
        protected void onException(Bundle args, Throwable ex) {
            Log.unexpectedError(getParentFragmentManager(), ex);
        }
    };
}
