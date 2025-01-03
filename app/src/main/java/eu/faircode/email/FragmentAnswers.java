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

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.Observer;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FragmentAnswers extends FragmentBase {
    private boolean cards;

    private View view;
    private RecyclerView rvAnswer;
    private ContentLoadingProgressBar pbWait;
    private Group grpReady;
    private FloatingActionButton fab;

    private String searching = null;
    private AdapterAnswer adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        cards = prefs.getBoolean("cards", true);
    }

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.menu_answers);
        setHasOptionsMenu(true);

        view = inflater.inflate(R.layout.fragment_answers, container, false);

        // Get controls
        rvAnswer = view.findViewById(R.id.rvAnswer);
        pbWait = view.findViewById(R.id.pbWait);
        grpReady = view.findViewById(R.id.grpReady);
        fab = view.findViewById(R.id.fab);

        // Wire controls

        rvAnswer.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rvAnswer.setLayoutManager(llm);

        if (!cards) {
            DividerItemDecoration itemDecorator = new DividerItemDecoration(getContext(), llm.getOrientation());
            itemDecorator.setDrawable(getContext().getDrawable(R.drawable.divider));
            rvAnswer.addItemDecoration(itemDecorator);
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        DividerItemDecoration categoryDecorator = new DividerItemDecoration(getContext(), llm.getOrientation()) {
            @Override
            public void onDraw(@NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                int count = parent.getChildCount();
                for (int i = 0; i < count; i++) {
                    View view = parent.getChildAt(i);
                    int pos = parent.getChildAdapterPosition(view);

                    View header = getView(view, parent, pos);
                    if (header != null) {
                        canvas.save();
                        canvas.translate(0, parent.getChildAt(i).getTop() - header.getMeasuredHeight());
                        header.draw(canvas);
                        canvas.restore();
                    }
                }
            }

            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int pos = parent.getChildAdapterPosition(view);
                View header = getView(view, parent, pos);
                if (header == null)
                    outRect.setEmpty();
                else
                    outRect.top = header.getMeasuredHeight();
            }

            private View getView(View view, RecyclerView parent, int pos) {
                if (pos == NO_POSITION)
                    return null;

                if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                    return null;

                String sort = prefs.getString("answer_sort", "name");
                if ("last_applied".equals(sort) || "applied".equals(sort))
                    return null;

                EntityAnswer prev = adapter.getItemAtPosition(pos - 1);
                EntityAnswer account = adapter.getItemAtPosition(pos);
                if (pos > 0 && prev == null)
                    return null;
                if (account == null)
                    return null;

                if (pos > 0) {
                    if (Objects.equals(prev.group, account.group))
                        return null;
                } else {
                    if (account.group == null)
                        return null;
                }

                View header = inflater.inflate(R.layout.item_group, parent, false);
                TextView tvCategory = header.findViewById(R.id.tvCategory);
                TextView tvDate = header.findViewById(R.id.tvDate);

                if (cards) {
                    View vSeparator = header.findViewById(R.id.vSeparator);
                    vSeparator.setVisibility(View.GONE);
                }

                tvCategory.setText(account.group);
                tvDate.setVisibility(View.GONE);

                header.measure(View.MeasureSpec.makeMeasureSpec(parent.getWidth(), View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                header.layout(0, 0, header.getMeasuredWidth(), header.getMeasuredHeight());

                return header;
            }
        };

        rvAnswer.addItemDecoration(categoryDecorator);

        adapter = new AdapterAnswer(this);
        rvAnswer.setAdapter(adapter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
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
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("fair:searching", searching);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null)
            searching = savedInstanceState.getString("fair:searching");
        adapter.search(searching);

        final Context context = getContext();
        DB db = DB.getInstance(context);
        db.answer().liveAnswers().observe(getViewLifecycleOwner(), new Observer<List<EntityAnswer>>() {
            @Override
            public void onChanged(List<EntityAnswer> answers) {
                if (answers == null)
                    answers = new ArrayList<>();

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                String sort = prefs.getString("answer_sort", "name");

                adapter.set(sort, answers);

                pbWait.setVisibility(View.GONE);
                grpReady.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_answers, menu);

        MenuItem menuSearch = menu.findItem(R.id.menu_search);
        SearchView searchView = (SearchView) menuSearch.getActionView();

        if (searchView != null)
            searchView.setQueryHint(getString(R.string.title_rules_search_hint));

        final String search = searching;
        view.post(new RunnableEx("answers:search") {
            @Override
            public void delegate() {
                if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                    return;

                if (TextUtils.isEmpty(search))
                    menuSearch.collapseActionView();
                else {
                    menuSearch.expandActionView();
                    searchView.setQuery(search, true);
                }
            }
        });

        getViewLifecycleOwner().getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            public void onDestroyed() {
                menuSearch.collapseActionView();
                getViewLifecycleOwner().getLifecycle().removeObserver(this);
            }
        });

        if (searchView != null)
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextChange(String newText) {
                    if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                        searching = newText;
                        adapter.search(newText);
                    }
                    return true;
                }

                @Override
                public boolean onQueryTextSubmit(String query) {
                    if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                        searching = query;
                        adapter.search(query);
                    }
                    return true;
                }
            });

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String sort = prefs.getString("answer_sort", "order");

        if ("last_applied".equals(sort))
            menu.findItem(R.id.menu_sort_on_last_applied).setChecked(true);
        else if ("applied".equals(sort))
            menu.findItem(R.id.menu_sort_on_applied).setChecked(true);
        else
            menu.findItem(R.id.menu_sort_on_name).setChecked(true);

        Menu smenu = menu.findItem(R.id.menu_placeholders).getSubMenu();

        List<String> names = EntityAnswer.getCustomPlaceholders(getContext());
        for (int i = 0; i < names.size(); i++)
            smenu.add(Menu.FIRST, i + 1, i + 1, names.get(i));

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getGroupId() == Menu.FIRST) {
            onMenuDefine(item.getTitle().toString());
            return true;
        } else {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_sort_on_name) {
                item.setChecked(true);
                onMenuSort("name");
                return true;
            } else if (itemId == R.id.menu_sort_on_applied) {
                item.setChecked(true);
                onMenuSort("applied");
                return true;
            } else if (itemId == R.id.menu_sort_on_last_applied) {
                item.setChecked(true);
                onMenuSort("last_applied");
                return true;
            } else if (itemId == R.id.menu_define) {
                onMenuDefine(null);
                return true;
            } else
                return super.onOptionsItemSelected(item);
        }
    }

    private void onMenuSort(String sort) {
        final Context context = getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString("answer_sort", sort).apply();
        adapter.setSort(sort);
    }

    private void onMenuDefine(String name) {
        final Context context = getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_placeholder, null);
        final EditText etName = view.findViewById(R.id.etName);
        final EditText etValue = view.findViewById(R.id.etValue);

        etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                String value = EntityAnswer.getCustomPlaceholder(context, s.toString().trim());
                if (!TextUtils.isEmpty(value))
                    etValue.setText(value);
            }
        });

        etName.setText(name);

        new AlertDialog.Builder(context)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = etName.getText().toString().trim();
                        String value = etValue.getText().toString();
                        if (TextUtils.isEmpty(name))
                            return;
                        EntityAnswer.setCustomPlaceholder(context, name, value);
                        invalidateOptionsMenu();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
