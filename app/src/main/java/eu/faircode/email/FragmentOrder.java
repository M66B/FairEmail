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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FragmentOrder extends FragmentBase {
    private int title;
    private String clazz;

    private RecyclerView rvOrder;
    private ContentLoadingProgressBar pbWait;
    private Group grpReady;

    private boolean dirty = false;
    private AdapterOrder adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get arguments
        Bundle args = getArguments();
        title = args.getInt("title", -1);
        clazz = args.getString("class");
        Log.i("Order class=" + clazz);
    }

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(title);
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_order, container, false);

        // Get controls
        rvOrder = view.findViewById(R.id.rvOrder);
        pbWait = view.findViewById(R.id.pbWait);
        grpReady = view.findViewById(R.id.grpReady);

        // Wire controls
        rvOrder.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rvOrder.setLayoutManager(llm);

        adapter = new AdapterOrder(this);
        rvOrder.setAdapter(adapter);
        new ItemTouchHelper(touchHelper).attachToRecyclerView(rvOrder);

        // Initialize
        grpReady.setVisibility(View.GONE);
        pbWait.setVisibility(View.VISIBLE);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (EntityAccount.class.getName().equals(clazz))
            new SimpleTask<List<EntityAccount>>() {
                @Override
                protected List<EntityAccount> onExecute(Context context, Bundle args) {
                    DB db = DB.getInstance(context);
                    return db.account().getSynchronizingAccounts(null);
                }

                @Override
                protected void onExecuted(Bundle args, List<EntityAccount> accounts) {
                    if (accounts == null)
                        accounts = new ArrayList<>();

                    Log.i("Order " + clazz + "=" + accounts.size());

                    adapter.set((List<EntityOrder>) (List<?>) accounts);

                    pbWait.setVisibility(View.GONE);
                    grpReady.setVisibility(View.VISIBLE);
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(getParentFragmentManager(), ex);
                }
            }.execute(this, new Bundle(), "order:accounts");
        else if (TupleFolderSort.class.getName().equals(clazz))
            new SimpleTask<List<TupleFolderSort>>() {
                @Override
                protected List<TupleFolderSort> onExecute(Context context, Bundle args) {
                    DB db = DB.getInstance(context);
                    return db.folder().getSortedFolders();
                }

                @Override
                protected void onExecuted(Bundle args, List<TupleFolderSort> folders) {
                    if (folders == null)
                        folders = new ArrayList<>();

                    Log.i("Order " + clazz + "=" + folders.size());

                    adapter.set((List<EntityOrder>) (List<?>) folders);

                    pbWait.setVisibility(View.GONE);
                    grpReady.setVisibility(View.VISIBLE);
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(getParentFragmentManager(), ex);
                }
            }.execute(this, new Bundle(), "order:folders");
        else
            throw new IllegalArgumentException("Unknown class=" + clazz);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (dirty)
            update(false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_sort, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_reset_order) {
            onMenuResetOrder();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onMenuResetOrder() {
        adapter.onReset();
        dirty = false;
        update(true);
    }

    private void update(boolean reset) {
        List<EntityOrder> items = adapter.getItems();

        List<Long> order = new ArrayList<>();
        for (int i = 0; i < items.size(); i++)
            order.add(items.get(i).getSortId());

        Bundle args = new Bundle();
        args.putString("class", clazz);
        args.putBoolean("reset", reset);
        args.putLongArray("ids", Helper.toLongArray(order));

        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) {
                final String clazz = args.getString("class");
                final boolean reset = args.getBoolean("reset");
                final long[] ids = args.getLongArray("ids");

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    for (int i = 0; i < ids.length; i++)
                        if (EntityAccount.class.getName().equals(clazz))
                            db.account().setAccountOrder(ids[i], reset ? null : i);
                        else if (TupleFolderSort.class.getName().equals(clazz))
                            db.folder().setFolderOrder(ids[i], reset ? null : i);
                        else
                            throw new IllegalArgumentException("Unknown class=" + clazz);

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                return null;
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "order:set");
    }

    private ItemTouchHelper.Callback touchHelper = new ItemTouchHelper.Callback() {
        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            int flags = 0;
            int pos = viewHolder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                if (pos - 1 >= 0)
                    flags |= ItemTouchHelper.UP;
                if (pos + 1 < rvOrder.getAdapter().getItemCount())
                    flags |= ItemTouchHelper.DOWN;
            }

            return makeMovementFlags(flags, 0);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder source, @NonNull RecyclerView.ViewHolder target) {
            dirty = true;
            ((AdapterOrder) rvOrder.getAdapter()).onMove(source.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        }
    };
}
