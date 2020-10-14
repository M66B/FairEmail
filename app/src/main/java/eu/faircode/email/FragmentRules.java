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

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static eu.faircode.email.EntityRule.TYPE_MOVE;

public class FragmentRules extends FragmentBase {
    private long account;
    private int protocol;
    private long folder;
    private String type;

    private boolean cards;
    private boolean beige;

    private RecyclerView rvRule;
    private ContentLoadingProgressBar pbWait;
    private Group grpReady;
    private FloatingActionButton fab;

    private String searching = null;
    private AdapterRule adapter;

    private static final int REQUEST_EXPORT = 1;
    private static final int REQUEST_IMPORT = 2;
    static final int REQUEST_MOVE = 3;
    private static final int REQUEST_CLEAR = 4;

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
        beige = prefs.getBoolean("beige", true);
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
            view.setBackgroundColor(ContextCompat.getColor(getContext(), beige
                    ? R.color.lightColorBackground_cards_beige
                    : R.color.lightColorBackground_cards));

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

        if (savedInstanceState != null) {
            searching = savedInstanceState.getString("fair:searching");
            adapter.search(searching);
        }

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
                case REQUEST_EXPORT:
                    if (resultCode == RESULT_OK && data != null)
                        onExport(data);
                    break;
                case REQUEST_IMPORT:
                    if (resultCode == RESULT_OK && data != null)
                        onImport(data);
                    break;
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

        MenuItem menuSearch = menu.findItem(R.id.menu_search);
        SearchView searchView = (SearchView) menuSearch.getActionView();
        searchView.setQueryHint(getString(R.string.title_rules_search_hint));

        if (!TextUtils.isEmpty(searching)) {
            menuSearch.expandActionView();
            searchView.setQuery(searching, true);
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                searching = newText;
                adapter.search(newText);
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                searching = query;
                adapter.search(query);
                return true;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.menu_delete_junk).setVisible(!EntityFolder.JUNK.equals(type));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_export:
                onMenuExport();
                return true;
            case R.id.menu_import:
                onMenuImport();
                return true;
            case R.id.menu_delete_all:
                onMenuDelete(true);
                return true;
            case R.id.menu_delete_junk:
                onMenuDelete(false);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onMenuExport() {
        if (!ActivityBilling.isPro(getContext())) {
            startActivity(new Intent(getContext(), ActivityBilling.class));
            return;
        }

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_TITLE, "fairemail_" +
                new SimpleDateFormat("yyyyMMdd").format(new Date().getTime()) + ".rules");
        Helper.openAdvanced(intent);
        startActivityForResult(intent, REQUEST_EXPORT);
    }

    private void onMenuImport() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, REQUEST_IMPORT);
    }

    private void onMenuDelete(boolean all) {
        Bundle aargs = new Bundle();
        aargs.putString("question", getString(all
                ? R.string.title_rules_delete_all_confirm
                : R.string.title_rules_delete_junk_confirm));
        aargs.putLong("folder", folder);
        aargs.putBoolean("all", all);

        FragmentDialogAsk ask = new FragmentDialogAsk();
        ask.setArguments(aargs);
        ask.setTargetFragment(this, REQUEST_CLEAR);
        ask.show(getParentFragmentManager(), "rules:clear");
    }

    private void onExport(Intent data) {
        Bundle args = new Bundle();
        args.putLong("folder", folder);
        args.putParcelable("uri", data.getData());

        new SimpleTask<Void>() {
            @Override
            protected void onPreExecute(Bundle args) {
                ToastEx.makeText(getContext(), R.string.title_executing, Toast.LENGTH_LONG).show();
            }

            @Override
            protected Void onExecute(Context context, Bundle args) throws Throwable {
                long fid = args.getLong("folder");
                Uri uri = args.getParcelable("uri");

                if (!"content".equals(uri.getScheme())) {
                    Log.w("Export uri=" + uri);
                    throw new IllegalArgumentException(context.getString(R.string.title_no_stream));
                }

                DB db = DB.getInstance(context);
                JSONArray jrules = new JSONArray();
                for (EntityRule rule : db.rule().getRules(fid))
                    jrules.put(rule.toJSON());

                ContentResolver resolver = context.getContentResolver();
                try (OutputStream os = resolver.openOutputStream(uri)) {
                    Log.i("Writing URI=" + uri);
                    os.write(jrules.toString(2).getBytes());
                }

                return null;
            }

            @Override
            protected void onExecuted(Bundle args, Void data) {
                ToastEx.makeText(getContext(), R.string.title_completed, Toast.LENGTH_LONG).show();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (ex instanceof IllegalArgumentException ||
                        ex instanceof FileNotFoundException)
                    ToastEx.makeText(getContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                else
                    Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "rules:export");
    }

    private void onImport(Intent data) {
        Bundle args = new Bundle();
        args.putLong("folder", folder);
        args.putParcelable("uri", data.getData());

        new SimpleTask<Void>() {
            @Override
            protected void onPreExecute(Bundle args) {
                ToastEx.makeText(getContext(), R.string.title_executing, Toast.LENGTH_LONG).show();
            }

            @Override
            protected Void onExecute(Context context, Bundle args) throws Throwable {
                long fid = args.getLong("folder");
                Uri uri = args.getParcelable("uri");

                if (!"content".equals(uri.getScheme())) {
                    Log.w("Import uri=" + uri);
                    throw new IllegalArgumentException(context.getString(R.string.title_no_stream));
                }

                StringBuilder data = new StringBuilder();

                Log.i("Reading URI=" + uri);
                ContentResolver resolver = context.getContentResolver();
                try (InputStream is = resolver.openInputStream(uri)) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    String line;
                    while ((line = reader.readLine()) != null)
                        data.append(line);
                }

                JSONArray jrules = new JSONArray(data.toString());

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    for (int i = 0; i < jrules.length(); i++) {
                        JSONObject jrule = jrules.getJSONObject(i);
                        EntityRule rule = EntityRule.fromJSON(jrule);
                        rule.folder = fid;
                        rule.applied = 0;
                        rule.id = db.rule().insertRule(rule);
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                return null;
            }

            @Override
            protected void onExecuted(Bundle args, Void data) {
                ToastEx.makeText(getContext(), R.string.title_completed, Toast.LENGTH_LONG).show();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (ex instanceof IllegalArgumentException ||
                        ex instanceof FileNotFoundException ||
                        ex instanceof JSONException)
                    ToastEx.makeText(getContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                else
                    Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "rules:import");
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
                boolean all = args.getBoolean("all");

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    if (all)
                        db.rule().deleteRules(fid);
                    else {
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
                            int type = jaction.optInt("type", -1);
                            long target = jaction.optLong("target", -1);
                            if (type == TYPE_MOVE && target == junk.id)
                                db.rule().deleteRule(rule.id);
                        }
                    }

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
        }.execute(this, args, "rules:clear");
    }
}
