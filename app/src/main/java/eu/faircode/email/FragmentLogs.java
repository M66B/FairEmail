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
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.Group;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FragmentLogs extends FragmentBase {
    private RecyclerView rvLog;
    private ContentLoadingProgressBar pbWait;
    private Group grpReady;

    private Long account = null;
    private Long folder = null;
    private Long message = null;
    private boolean autoScroll = true;

    private AdapterLog adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args == null)
            args = new Bundle();

        account = args.getLong("account", -1L);
        folder = args.getLong("folder", -1L);
        message = args.getLong("message", -1L);

        if (account < 0)
            account = null;
        if (folder < 0)
            folder = null;
        if (message < 0)
            message = null;

        if (savedInstanceState != null)
            autoScroll = savedInstanceState.getBoolean("fair:scroll");
    }

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_log);
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_logs, container, false);

        // Get controls
        rvLog = view.findViewById(R.id.rvLog);
        pbWait = view.findViewById(R.id.pbWait);
        grpReady = view.findViewById(R.id.grpReady);

        // Wire controls

        rvLog.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rvLog.setLayoutManager(llm);

        adapter = new AdapterLog(this);
        rvLog.setAdapter(adapter);

        rvLog.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                try {
                    autoScroll = (llm.findFirstVisibleItemPosition() <= 0);
                } catch (Throwable ex) {
                    Log.e(ex);
                }
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

        long from = new Date().getTime() - 24 * 3600 * 1000L;
        int limit = (BuildConfig.DEBUG ? 5000 : 2000);

        DB db = DB.getInstance(getContext());
        db.log().liveLogs(from, limit, null).observe(getViewLifecycleOwner(), new Observer<List<EntityLog>>() {
            @Override
            public void onChanged(List<EntityLog> logs) {
                if (logs == null)
                    logs = new ArrayList<>();

                adapter.set(logs, account, folder, message, getTypes(), new Runnable() {
                    @Override
                    public void run() {
                        if (autoScroll)
                            rvLog.scrollToPosition(0);
                    }
                });

                pbWait.setVisibility(View.GONE);
                grpReady.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("fair:scroll", autoScroll);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_logs, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean main_log = prefs.getBoolean("main_log", true);

        boolean all = (account == null && folder == null && message == null);

        menu.findItem(R.id.menu_enabled).setChecked(main_log);
        menu.findItem(R.id.menu_show).setVisible(all);
        menu.findItem(R.id.menu_clear).setVisible(all);

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_enabled) {
            boolean enabled = !item.isChecked();
            item.setChecked(enabled);
            onMenuEnable(enabled);
            return true;
        } else if (itemId == R.id.menu_show) {
            onMenuShow();
        } else if (itemId == R.id.menu_clear) {
            onMenuClear();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onMenuEnable(boolean enabled) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefs.edit().putBoolean("main_log", enabled).apply();
    }

    private void onMenuShow() {
        final Context context = getContext();

        int len = EntityLog.Type.values().length;
        if (!BuildConfig.DEBUG)
            len -= 5;

        SpannableStringBuilder[] titles = new SpannableStringBuilder[len];
        boolean[] states = new boolean[len];
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        for (int i = 0; i < len; i++) {
            EntityLog.Type type = EntityLog.Type.values()[i];
            titles[i] = new SpannableStringBuilderEx(type.toString());
            Integer color = EntityLog.getColor(context, type);
            if (color != null)
                titles[i].setSpan(new ForegroundColorSpan(color), 0, titles[i].length(), 0);
            titles[i].setSpan(new StyleSpan(Typeface.BOLD), 0, titles[i].length(), 0);
            String name = type.toString().toLowerCase(Locale.ROOT);
            states[i] = prefs.getBoolean("show_log_" + name, true);
        }

        new AlertDialog.Builder(context)
                .setIcon(R.drawable.twotone_visibility_24)
                .setTitle(R.string.title_unhide)
                .setMultiChoiceItems(titles, states, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int pos, boolean checked) {
                        EntityLog.Type type = EntityLog.Type.values()[pos];
                        prefs.edit().putBoolean(getKey(type), checked).apply();
                        adapter.setTypes(getTypes());
                    }
                })
                .setPositiveButton(R.string.title_setup_done, null)
                .show();
    }

    private List<EntityLog.Type> getTypes() {
        List<EntityLog.Type> types = new ArrayList<>();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        for (EntityLog.Type type : EntityLog.Type.values())
            if (prefs.getBoolean(getKey(type), true))
                types.add(type);

        return types;
    }

    private String getKey(EntityLog.Type type) {
        return "show_log_" + type.toString().toLowerCase(Locale.ROOT);
    }

    private void onMenuClear() {
        EntityLog.clear(getContext());
    }
}
