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

    Copyright 2018-2023 by Marcel Bokhorst (M66B)
*/

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FragmentDialogKeywordManage extends FragmentDialogBase {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final long id = getArguments().getLong("id");

        final Context context = getContext();
        final View dview = LayoutInflater.from(context).inflate(R.layout.dialog_keyword_manage, null);
        final RecyclerView rvKeyword = dview.findViewById(R.id.rvKeyword);
        final FloatingActionButton fabAdd = dview.findViewById(R.id.fabAdd);
        final ContentLoadingProgressBar pbWait = dview.findViewById(R.id.pbWait);

        rvKeyword.setHasFixedSize(false);
        final LinearLayoutManager llm = new LinearLayoutManager(context);
        rvKeyword.setLayoutManager(llm);

        final AdapterKeyword adapter = new AdapterKeyword(context, getViewLifecycleOwner());
        rvKeyword.setAdapter(adapter);

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putLong("id", id);

                FragmentDialogKeywordAdd fragment = new FragmentDialogKeywordAdd();
                fragment.setArguments(args);
                fragment.show(getParentFragmentManager(), "keyword:add");
            }
        });

        pbWait.setVisibility(View.VISIBLE);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        DB db = DB.getInstance(context);
        db.message().liveMessageKeywords(id).observe(getViewLifecycleOwner(), new Observer<TupleKeyword.Persisted>() {
            @Override
            public void onChanged(TupleKeyword.Persisted data) {
                if (data == null)
                    data = new TupleKeyword.Persisted();

                String global = prefs.getString("global_keywords", null);
                if (global != null) {
                    List<String> available = new ArrayList<>();
                    available.addAll(Arrays.asList(global.split(" ")));
                    if (data.available != null)
                        available.addAll(Arrays.asList(data.available));
                    data.available = available.toArray(new String[0]);
                }

                pbWait.setVisibility(View.GONE);
                adapter.set(id, TupleKeyword.from(context, data));
            }
        });

        return new AlertDialog.Builder(context)
                .setIcon(R.drawable.twotone_label_important_24)
                .setTitle(R.string.title_manage_keywords)
                .setView(dview)
                .setPositiveButton(android.R.string.ok, null)
                .create();
    }

    @Override
    public void onResume() {
        super.onResume();
        Dialog dialog = getDialog();
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        //dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }
}
