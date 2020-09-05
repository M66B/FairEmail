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
import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static eu.faircode.email.EntityRule.TYPE_MOVE;

public class FragmentRules extends FragmentBase {
    private long account;
    private int protocol;
    private long folder;
    private String type;

    private boolean cards;

    private RecyclerView rvRule;
    private ContentLoadingProgressBar pbWait;
    private Group grpReady;
    private FloatingActionButton fab;

    private AdapterRule adapter;

    static final int REQUEST_MOVE = 1;
    private static final int REQUEST_CLEAR = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get arguments
        Bundle args = getArguments();
        account = args.getLong("account", -1);
        protocol = args.getInt("protocol", -1);
        folder = args.getLong("folder", -1);
        type = args.getString("type");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        cards = prefs.getBoolean("cards", true);
    }

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_edit_rules);
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_rules, container, false);

        // Get controls
        rvRule = view.findViewById(R.id.rvRule);
        pbWait = view.findViewById(R.id.pbWait);
        grpReady = view.findViewById(R.id.grpReady);
        fab = view.findViewById(R.id.fab);

        // Wire controls

        rvRule.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rvRule.setLayoutManager(llm);

        adapter = new AdapterRule(this);
        rvRule.setAdapter(adapter);

        if (!cards) {
            DividerItemDecoration itemDecorator = new DividerItemDecoration(getContext(), llm.getOrientation());
            itemDecorator.setDrawable(getContext().getDrawable(R.drawable.divider));
            rvRule.addItemDecoration(itemDecorator);
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle args = new Bundle();
                args.putLong("account", account);
                args.putInt("protocol", protocol);
                args.putLong("folder", folder);

                FragmentRule fragment = new FragmentRule();
                fragment.setArguments(args);

                FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("rule");
                fragmentTransaction.commit();
            }
        });

        // Initialize

        if (cards && !Helper.isDarkTheme(getContext()))
            view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.lightColorBackground_cards));

        grpReady.setVisibility(View.GONE);
        pbWait.setVisibility(View.VISIBLE);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        DB db = DB.getInstance(getContext());
        db.rule().liveRules(folder).observe(getViewLifecycleOwner(), new Observer<List<TupleRuleEx>>() {
            @Override
            public void onChanged(List<TupleRuleEx> rules) {
                if (rules == null)
                    rules = new ArrayList<>();

                adapter.set(protocol, rules);

                pbWait.setVisibility(View.GONE);
                grpReady.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            switch (requestCode) {
                case REQUEST_MOVE:
                    if (resultCode == RESULT_OK && data != null)
                        onMove(data.getBundleExtra("args"));
                    break;
                case REQUEST_CLEAR:
                    if (resultCode == RESULT_OK && data != null)
                        onClear(data.getBundleExtra("args"));
                    break;
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_rules, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.menu_clear).setVisible(!EntityFolder.JUNK.equals(type));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_clear:
                onMenuClear();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onMenuClear() {
        Bundle aargs = new Bundle();
        aargs.putString("question", getString(R.string.title_rules_clear_confirm));
        aargs.putLong("folder", folder);

        FragmentDialogAsk ask = new FragmentDialogAsk();
        ask.setArguments(aargs);
        ask.setTargetFragment(this, REQUEST_CLEAR);
        ask.show(getParentFragmentManager(), "rules:clear");
    }

    private void onMove(Bundle args) {
        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) {
                long id = args.getLong("rule");
                long folder = args.getLong("folder");

                DB db = DB.getInstance(context);
                db.rule().setRuleFolder(id, folder);
                return null;
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "rule:move");
    }

    private void onClear(Bundle args) {
        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) throws Throwable {
                long fid = args.getLong("folder");

                DB db = DB.getInstance(context);
                EntityFolder folder = db.folder().getFolder(fid);
                if (folder == null)
                    return null;

                EntityFolder junk = db.folder().getFolderByType(folder.account, EntityFolder.JUNK);
                if (junk == null)
                    return null;

                List<EntityRule> rules = db.rule().getRules(fid);
                if (rules == null)
                    return null;

                for (EntityRule rule : rules) {
                    JSONObject jaction = new JSONObject(rule.action);
                    if (jaction.optInt("type", -1) == TYPE_MOVE &&
                            jaction.optInt("target", -1) == junk.id)
                        db.rule().deleteRule(rule.id);
                }

                return null;
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "rules:clear");
    }
}
