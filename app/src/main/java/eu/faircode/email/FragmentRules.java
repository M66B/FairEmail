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

import static android.app.Activity.RESULT_OK;
import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.Group;
import androidx.core.view.MenuCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.Observer;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
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
import java.util.Objects;

public class FragmentRules extends FragmentBase {
    private long account;
    private int protocol;
    private long folder;
    private String type;

    private boolean cards;
    private boolean dividers;

    private View view;
    private RecyclerView rvRule;
    private ContentLoadingProgressBar pbWait;
    private Group grpReady;
    private FloatingActionButton fab;

    private String searching = null;
    private AdapterRule adapter;

    private static final int REQUEST_EXPORT = 1;
    private static final int REQUEST_IMPORT = 2;
    static final int REQUEST_GROUP = 3;
    static final int REQUEST_MOVE = 4;
    static final int REQUEST_RULE_COPY_ACCOUNT = 5;
    static final int REQUEST_RULE_COPY_FOLDER = 6;
    private static final int REQUEST_CLEAR = 7;

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
        dividers = prefs.getBoolean("dividers", true);
    }

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_edit_rules);
        setHasOptionsMenu(true);

        view = inflater.inflate(R.layout.fragment_rules, container, false);

        // Get controls
        rvRule = view.findViewById(R.id.rvRule);
        pbWait = view.findViewById(R.id.pbWait);
        grpReady = view.findViewById(R.id.grpReady);
        fab = view.findViewById(R.id.fab);

        // Wire controls

        rvRule.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rvRule.setLayoutManager(llm);

        DividerItemDecoration groupDecorator = new DividerItemDecoration(getContext(), llm.getOrientation()) {
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

                EntityRule prev = adapter.getItemAtPosition(pos - 1);
                EntityRule rule = adapter.getItemAtPosition(pos);
                if (pos > 0 && prev == null)
                    return null;
                if (rule == null)
                    return null;

                if (pos > 0) {
                    if (Objects.equals(prev.group, rule.group))
                        return null;
                } else {
                    if (rule.group == null)
                        return null;
                }

                View header = inflater.inflate(R.layout.item_group, parent, false);
                TextView tvCategory = header.findViewById(R.id.tvCategory);
                TextView tvDate = header.findViewById(R.id.tvDate);

                if (cards || !dividers) {
                    View vSeparator = header.findViewById(R.id.vSeparator);
                    vSeparator.setVisibility(View.GONE);
                }

                tvCategory.setText(rule.group);
                tvDate.setVisibility(View.GONE);

                header.measure(View.MeasureSpec.makeMeasureSpec(parent.getWidth(), View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                header.layout(0, 0, header.getMeasuredWidth(), header.getMeasuredHeight());

                return header;
            }
        };
        rvRule.addItemDecoration(groupDecorator);

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
                args.putString("type", type);

                FragmentRule fragment = new FragmentRule();
                fragment.setArguments(args);

                FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("rule");
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
        db.rule().liveRules(folder).observe(getViewLifecycleOwner(), new Observer<List<TupleRuleEx>>() {
            @Override
            public void onChanged(List<TupleRuleEx> rules) {
                if (rules == null)
                    rules = new ArrayList<>();

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                String sort = prefs.getString("rule_sort", "order");

                adapter.set(protocol, type, sort, rules);
                rvRule.invalidateItemDecorations();

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
                case REQUEST_GROUP:
                    if (resultCode == RESULT_OK && data != null)
                        onGroup(data.getBundleExtra("args"));
                    break;
                case REQUEST_MOVE:
                    if (resultCode == RESULT_OK && data != null)
                        onMove(data.getBundleExtra("args"));
                    break;
                case REQUEST_RULE_COPY_ACCOUNT:
                    if (resultCode == RESULT_OK && data != null)
                        onRuleCopyAccount(data.getBundleExtra("args"));
                    break;
                case REQUEST_RULE_COPY_FOLDER:
                    if (resultCode == RESULT_OK && data != null)
                        onRuleCopyFolder(data.getBundleExtra("args"));
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

        if (searchView != null)
            searchView.setQueryHint(getString(R.string.title_rules_search_hint));

        final String search = searching;
        view.post(new RunnableEx("rules:search") {
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

        MenuCompat.setGroupDividerEnabled(menu, true);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String sort = prefs.getString("rule_sort", "order");

        if ("last_applied".equals(sort))
            menu.findItem(R.id.menu_sort_on_last_applied).setChecked(true);
        else if ("applied".equals(sort))
            menu.findItem(R.id.menu_sort_on_applied).setChecked(true);
        else
            menu.findItem(R.id.menu_sort_on_order).setChecked(true);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_sort_on_order) {
            item.setChecked(true);
            onMenuSort("order");
            return true;
        } else if (itemId == R.id.menu_sort_on_applied) {
            item.setChecked(true);
            onMenuSort("applied");
            return true;
        } else if (itemId == R.id.menu_sort_on_last_applied) {
            item.setChecked(true);
            onMenuSort("last_applied");
            return true;
        } else if (itemId == R.id.menu_export) {
            onMenuExport();
            return true;
        } else if (itemId == R.id.menu_import) {
            onMenuImport();
            return true;
        } else if (itemId == R.id.menu_delete_all) {
            onMenuDelete();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onMenuSort(String sort) {
        final Context context = getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString("rule_sort", sort).apply();
        adapter.setSort(sort);
    }

    private void onMenuExport() {
        final Context context = getContext();
        if (!ActivityBilling.isPro(context)) {
            startActivity(new Intent(context, ActivityBilling.class));
            return;
        }

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_TITLE, "fairemail_" +
                new SimpleDateFormat("yyyyMMdd").format(new Date().getTime()) + ".rules");
        Helper.openAdvanced(context, intent);
        startActivityForResult(intent, REQUEST_EXPORT);
    }

    private void onMenuImport() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("*/*");
        startActivityForResult(intent, REQUEST_IMPORT);
    }

    private void onMenuDelete() {
        Bundle aargs = new Bundle();
        aargs.putString("question", getString(R.string.title_rules_delete_all_confirm));
        aargs.putLong("folder", folder);

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
            private Toast toast = null;

            @Override
            protected void onPreExecute(Bundle args) {
                toast = ToastEx.makeText(getContext(), R.string.title_executing, Toast.LENGTH_LONG);
                toast.show();
            }

            @Override
            protected void onPostExecute(Bundle args) {
                if (toast != null)
                    toast.cancel();
            }

            @Override
            protected Void onExecute(Context context, Bundle args) throws Throwable {
                long fid = args.getLong("folder");
                Uri uri = args.getParcelable("uri");

                if (uri == null)
                    throw new FileNotFoundException();

                if (!"content".equals(uri.getScheme())) {
                    Log.w("Export uri=" + uri);
                    throw new IllegalArgumentException(context.getString(R.string.title_no_stream));
                }

                DB db = DB.getInstance(context);
                JSONArray jrules = new JSONArray();
                for (EntityRule rule : db.rule().getRules(fid)) {
                    JSONObject jaction = new JSONObject(rule.action);

                    int type = jaction.getInt("type");
                    switch (type) {
                        case EntityRule.TYPE_MOVE:
                        case EntityRule.TYPE_COPY:
                            long target = jaction.optLong("target", -1);
                            EntityFolder f = db.folder().getFolder(target);
                            EntityAccount a = (f == null ? null : db.account().getAccount(f.account));
                            if (a != null)
                                jaction.put("targetAccountUuid", a.uuid);
                            if (f != null) {
                                jaction.put("targetFolderType", f.type);
                                jaction.put("targetFolderName", f.name);
                            }
                            break;
                        case EntityRule.TYPE_ANSWER:
                            long identity = jaction.getLong("identity");
                            long answer = jaction.optLong("answer", -1L);
                            EntityIdentity i = db.identity().getIdentity(identity);
                            EntityAnswer t = db.answer().getAnswer(answer);
                            if (i != null)
                                jaction.put("identityUuid", i.uuid);
                            if (t != null)
                                jaction.put("answerUuid", t.uuid);
                            break;
                    }

                    rule.action = jaction.toString();

                    jrules.put(rule.toJSON());
                }

                ContentResolver resolver = context.getContentResolver();
                try (OutputStream os = resolver.openOutputStream(uri)) {
                    Log.i("Writing URI=" + uri);
                    if (os == null)
                        throw new FileNotFoundException(uri.toString());
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
                boolean report = !(ex instanceof IllegalArgumentException || ex instanceof FileNotFoundException);
                Log.unexpectedError(getParentFragmentManager(), ex, report);
            }
        }.execute(this, args, "rules:export");
    }

    private void onImport(Intent data) {
        Bundle args = new Bundle();
        args.putLong("folder", folder);
        args.putParcelable("uri", data.getData());

        new SimpleTask<Void>() {
            private Toast toast = null;

            @Override
            protected void onPreExecute(Bundle args) {
                toast = ToastEx.makeText(getContext(), R.string.title_executing, Toast.LENGTH_LONG);
                toast.show();
            }

            @Override
            protected void onPostExecute(Bundle args) {
                if (toast != null)
                    toast.cancel();
            }

            @Override
            protected Void onExecute(Context context, Bundle args) throws Throwable {
                long fid = args.getLong("folder");
                Uri uri = args.getParcelable("uri");

                NoStreamException.check(uri, context);

                StringBuilder data = new StringBuilder();

                Log.i("Reading URI=" + uri);
                ContentResolver resolver = context.getContentResolver();
                try (InputStream is = resolver.openInputStream(uri)) {
                    if (is == null)
                        throw new FileNotFoundException(uri.toString());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    String line;
                    while ((line = reader.readLine()) != null)
                        data.append(line);
                }

                JSONArray jrules = new JSONArray(data.toString());

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    EntityFolder folder = db.folder().getFolder(fid);
                    if (folder == null)
                        return null;

                    for (int r = 0; r < jrules.length(); r++) {
                        JSONObject jrule = jrules.getJSONObject(r);
                        EntityRule rule = EntityRule.fromJSON(jrule);

                        JSONObject jaction = new JSONObject(rule.action);

                        int type = jaction.getInt("type");
                        switch (type) {
                            case EntityRule.TYPE_MOVE:
                            case EntityRule.TYPE_COPY:
                                String targetAccountUuid = jaction.optString("targetAccountUuid");
                                String targetFolderName = jaction.optString("targetFolderName");
                                if (!TextUtils.isEmpty(targetAccountUuid) && !TextUtils.isEmpty(targetFolderName)) {
                                    EntityAccount a = db.account().getAccountByUUID(targetAccountUuid);
                                    if (a != null) {
                                        EntityFolder f = db.folder().getFolderByName(a.id, targetFolderName);
                                        if (f != null) {
                                            jaction.put("target", f.id);
                                            break;
                                        }
                                    }
                                }

                                String folderType = jaction.optString("targetFolderType");
                                if (TextUtils.isEmpty(folderType))
                                    folderType = jaction.optString("folderType"); // legacy
                                if (!EntityFolder.SYSTEM.equals(folderType) &&
                                        !EntityFolder.USER.equals(folderType)) {
                                    EntityFolder f = db.folder().getFolderByType(folder.account, folderType);
                                    if (f != null)
                                        jaction.put("target", f.id);
                                }
                                break;

                            case EntityRule.TYPE_ANSWER:
                                String identityUuid = jaction.optString("identityUuid");
                                String answerUuid = jaction.optString("answerUuid");
                                if (!TextUtils.isEmpty(identityUuid) && !TextUtils.isEmpty(answerUuid)) {
                                    EntityIdentity i = db.identity().getIdentityByUUID(identityUuid);
                                    EntityAnswer a = db.answer().getAnswerByUUID(answerUuid);
                                    if (i != null && a != null) {
                                        jaction.put("identity", i.id);
                                        jaction.put("answer", a.id);
                                        break;
                                    }
                                }
                        }

                        rule.action = jaction.toString();

                        rule.folder = fid;
                        rule.applied = 0;
                        rule.last_applied = null;
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
                if (ex instanceof NoStreamException)
                    ((NoStreamException) ex).report(getActivity());
                else {
                    boolean report = !(ex instanceof FileNotFoundException || ex instanceof JSONException);
                    Log.unexpectedError(getParentFragmentManager(), ex, report);
                }
            }
        }.execute(this, args, "rules:import");
    }

    private void onGroup(Bundle args) {
        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) {
                long id = args.getLong("rule");
                String name = args.getString("name");

                DB db = DB.getInstance(context);
                db.rule().setRuleGroup(id, name);
                return null;
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "rule:group");
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

    private void onRuleCopyAccount(Bundle args) {
        args.putString("title", getString(R.string.title_copy_to));
        args.putLongArray("disabled", new long[0]);
        args.putBoolean("cancopy", false);

        FragmentDialogSelectFolder fragment = new FragmentDialogSelectFolder();
        fragment.setArguments(args);
        fragment.setTargetFragment(this, REQUEST_RULE_COPY_FOLDER);
        fragment.show(getParentFragmentManager(), "rule:copy:folder");
    }

    private void onRuleCopyFolder(Bundle args) {
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
        lbm.sendBroadcast(
                new Intent(ActivityView.ACTION_EDIT_RULE)
                        .putExtra("id", args.getLong("rule"))
                        .putExtra("account", args.getLong("account"))
                        .putExtra("protocol", args.getInt("protocol"))
                        .putExtra("folder", args.getLong("folder"))
                        .putExtra("type", args.getString("type"))
                        .putExtra("copy", true));
    }

    private void onClear(Bundle args) {
        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) throws Throwable {
                long fid = args.getLong("folder");

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    db.rule().deleteRules(fid);

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
