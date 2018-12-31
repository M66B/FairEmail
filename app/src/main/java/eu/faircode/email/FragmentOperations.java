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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class FragmentOperations extends FragmentEx {
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
        rvOperation = view.findViewById(R.id.rvOperation);
        pbWait = view.findViewById(R.id.pbWait);
        grpReady = view.findViewById(R.id.grpReady);

        // Wire controls

        rvOperation.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rvOperation.setLayoutManager(llm);

        adapter = new AdapterOperation(getContext(), getViewLifecycleOwner());
        rvOperation.setAdapter(adapter);

        // Initialize
        grpReady.setVisibility(View.GONE);
        pbWait.setVisibility(View.VISIBLE);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Observe folders
        DB.getInstance(getContext()).operation().liveOperations().observe(getViewLifecycleOwner(), new Observer<List<TupleOperationEx>>() {
            @Override
            public void onChanged(@Nullable List<TupleOperationEx> operations) {
                if (operations == null)
                    operations = new ArrayList<>();

                adapter.set(operations);

                pbWait.setVisibility(View.GONE);
                grpReady.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_operations, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        PackageManager pm = getContext().getPackageManager();
        menu.findItem(R.id.menu_help).setVisible(getFAQIntent().resolveActivity(pm) != null);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_help:
                onMenuHelp();
                return true;
            case R.id.menu_delete:
                onMenuDelete();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onMenuHelp() {
        startActivity(getFAQIntent());
    }

    private void onMenuDelete() {
        new DialogBuilderLifecycle(getContext(), getViewLifecycleOwner())
                .setMessage(R.string.title_delete_operation)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new SimpleTask<Void>() {
                            @Override
                            protected Void onExecute(Context context, Bundle args) {
                                DB db = DB.getInstance(context);
                                List<EntityOperation> ops = db.operation().getOperationsError();
                                Log.i("Operations with error count=" + ops.size());
                                for (EntityOperation op : ops) {
                                    Log.w("Deleting operation=" + op.id + " error=" + op.error);
                                    if (op.message != null)
                                        db.message().setMessageUiHide(op.message, false);
                                    db.operation().deleteOperation(op.id);
                                }
                                return null;
                            }

                            @Override
                            protected void onException(Bundle args, Throwable ex) {
                                Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
                            }
                        }.execute(FragmentOperations.this, new Bundle());
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private Intent getFAQIntent() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://github.com/M66B/open-source-email/blob/master/FAQ.md#user-content-faq3"));
        return intent;
    }
}
