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

import android.app.Notification;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class FragmentOptions extends FragmentBase {
    private ViewPager pager;
    private PagerAdapter adapter;
    private String searching = null;
    private SuggestData data = null;

    private int dp24;

    static final long DELAY_SETOPTIONS = 20; // ms

    private static final int[] TAB_PAGES = {
            R.layout.fragment_setup,
            R.layout.fragment_options_synchronize,
            R.layout.fragment_options_send,
            R.layout.fragment_options_connection,
            R.layout.fragment_options_display,
            R.layout.fragment_options_behavior,
            R.layout.fragment_options_privacy,
            R.layout.fragment_options_encryption,
            R.layout.fragment_options_notifications,
            R.layout.fragment_options_integrations,
            R.layout.fragment_options_misc,
            R.layout.fragment_options_backup
    };

    static final int[] PAGE_TITLES = {
            R.string.title_advanced_section_main,
            R.string.title_advanced_section_synchronize,
            R.string.title_advanced_section_send,
            R.string.title_advanced_section_connection,
            R.string.title_advanced_section_display,
            R.string.title_advanced_section_behavior,
            R.string.title_advanced_section_privacy,
            R.string.title_advanced_section_encryption,
            R.string.title_advanced_section_notifications,
            R.string.title_advanced_caption_integrations,
            R.string.title_advanced_section_misc,
            R.string.title_advanced_section_backup
    };

    static final int[] PAGE_ICONS = {
            R.drawable.twotone_home_24,
            R.drawable.twotone_sync_24,
            R.drawable.twotone_send_24,
            R.drawable.twotone_cloud_24,
            R.drawable.twotone_monitor_24,
            R.drawable.twotone_psychology_24,
            R.drawable.twotone_account_circle_24,
            R.drawable.twotone_lock_24,
            R.drawable.twotone_notifications_24,
            R.drawable.twotone_extension_24,
            R.drawable.twotone_more_24,
            R.drawable.twotone_save_alt_24
    };

    static final List<String> TAB_LABELS = Collections.unmodifiableList(Arrays.asList(
            "main",
            "sync",
            "send",
            "connection",
            "display",
            "behavior",
            "privacy",
            "encryption",
            "notifications",
            "integrations",
            "misc",
            "backup"
    ));

    static String[] OPTIONS_RESTART = new String[]{
            "first", "app_support", "notify_archive",
            "message_swipe", "message_select", "message_junk",
            "folder_actions", "folder_sync",
            "subscriptions",
            "check_authentication", "check_tls", "check_reply_domain", "check_mx", "check_blocklist",
            "send_pending",
            "startup", "answer_single",
            "cards", "beige", "tabular_card_bg", "shadow_unread", "shadow_border", "shadow_highlight", "dividers", "tabular_unread_bg",
            "portrait2", "portrait2c", "portrait_min_size", "landscape", "landscape_min_size",
            "column_width",
            "hide_toolbar", "edge_to_edge", "nav_categories", "nav_last_sync", "nav_count", "nav_unseen_drafts", "nav_count_pinned", "show_unexposed",
            "indentation", "date", "date_week", "date_fixed", "date_bold", "date_time", "threading", "threading_unread",
            "show_filtered",
            "highlight_unread", "highlight_color", "account_color", "account_color_size",
            "avatars", "bimi", "gravatars", "libravatars", "favicons", "favicons_partial", "favicons_manifest", "generated_icons", "identicons",
            "circular", "saturation", "brightness", "threshold",
            "authentication", "authentication_indicator",
            "email_format", "prefer_contact", "only_contact", "distinguish_contacts", "show_recipients", "reverse_addresses",
            "font_size_sender", "sender_ellipsize",
            "subject_top", "subject_italic", "highlight_subject", "font_size_subject", "subject_ellipsize",
            "keywords_header", "labels_header", "flags", "flags_background", "preview", "preview_italic", "preview_lines", "align_header",
            "message_zoom", "overview_mode", "addresses", "button_extra", "attachments_alt",
            "thumbnails", "pdf_preview", "video_preview", "audio_preview", "barcode_preview",
            "contrast", "hyphenation", "display_font", "monospaced_pre",
            "list_count", "bundled_fonts", "narrow_fonts", "parse_classes",
            "background_color", "text_color", "text_size", "text_font", "text_align", "text_titles", "text_separators",
            "collapse_quotes", "image_placeholders", "inline_images",
            "seekbar", "actionbar", "actionbar_swap", "actionbar_color", "group_category",
            "autoscroll", "swipenav", "updown", "reversed", "swipe_close", "swipe_move", "autoexpand", "autoclose", "onclose",
            "keyboard_margin", "auto_hide_answer", "swipe_reply",
            "move_thread_all", "move_thread_sent",
            "language_detection",
            "quick_filter", "quick_scroll", "quick_actions",
            "experiments", "debug", "log_level", "test1", "test2", "test3", "test4", "test5",
            "webview_legacy", "browser_zoom",
            "show_recent",
            "biometrics",
            "default_light",
            "vt_enabled", "vt_apikey",
            "pdf_preview", "webp",
            "email_junk"
    };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("fair:searching", searching);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getContext();
        this.dp24 = (context == null ? -48 : Helper.dp2pixels(context, 24));
    }

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_options, container, false);
        setHasOptionsMenu(true);

        if (savedInstanceState != null)
            searching = savedInstanceState.getString("fair:searching");

        pager = view.findViewById(R.id.pager);
        adapter = new PagerAdapter(getChildFragmentManager());
        pager.setAdapter(adapter);

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Do nothing
            }

            @Override
            public void onPageSelected(int position) {
                if (position > 0 && position < PAGE_TITLES.length &&
                        PAGE_TITLES[position] != R.string.title_advanced_section_backup) {
                    final Context context = getContext();
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                    boolean setup_advanced = prefs.getBoolean("setup_advanced", false);
                    if (!setup_advanced) {
                        View dview = LayoutInflater.from(context).inflate(R.layout.dialog_advanced, null);
                        new AlertDialog.Builder(context)
                                .setView(dview)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        prefs.edit().putBoolean("setup_advanced", true).apply();
                                    }
                                })
                                .setNegativeButton(R.string.title_go_back, null)
                                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialog) {
                                        if (pager != null && !prefs.getBoolean("setup_advanced", false))
                                            pager.setCurrentItem(0);
                                    }
                                })
                                .show();
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // Do nothing
            }
        });

        getParentFragmentManager().setFragmentResultListener("options:tab", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                int page = result.getInt("page");
                pager.setCurrentItem(page);
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        final Context context = getContext();

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        tabLayout.setBackgroundColor(Helper.resolveColor(context, R.attr.colorCardBackground));
        tabLayout.setupWithViewPager(pager);

        int colorAccent = Helper.resolveColor(context, androidx.appcompat.R.attr.colorAccent);
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            Drawable d = ContextCompat.getDrawable(context, PAGE_ICONS[i]);
            d.setColorFilter(colorAccent, PorterDuff.Mode.SRC_ATOP);
            SpannableStringBuilder title = new SpannableStringBuilderEx(getString(PAGE_TITLES[i]));
            if (i > 0)
                title.setSpan(new RelativeSizeSpan(0.85f), 0, title.length(), 0);
            tabLayout.getTabAt(i)
                    .setIcon(d)
                    .setText(title);
        }

        Intent intent = getActivity().getIntent();
        if (intent != null) {
            String tab;

            Set<String> categories = intent.getCategories();
            if (categories != null && categories.contains(Notification.INTENT_CATEGORY_NOTIFICATION_PREFERENCES))
                tab = "notifications";
            else
                tab = getActivity().getIntent().getStringExtra("tab");
            if (!TextUtils.isEmpty(tab)) {
                int index = TAB_LABELS.indexOf(tab);
                if (index >= 0)
                    pager.setCurrentItem(index);
                getActivity().getIntent().removeExtra("tab");
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_setup, menu);

        final String saved = searching;
        final MenuItem menuSearch = menu.findItem(R.id.menu_search);
        final SearchView searchView = (SearchView) menuSearch.getActionView();

        if (searchView != null)
            searchView.setQueryHint(getString(R.string.title_search));

        final SearchView.OnSuggestionListener onSuggestionListener = new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                Cursor cursor = searchView.getSuggestionsAdapter().getCursor();

                if (cursor != null && cursor.moveToPosition(position)) {
                    int tab = cursor.getInt(cursor.getColumnIndex("tab"));
                    int resid = cursor.getInt(cursor.getColumnIndex("resid"));

                    pager.setCurrentItem(tab);
                    FragmentBase fragment = (FragmentBase) adapter.instantiateItem(pager, tab);
                    if (fragment instanceof FragmentSetup)
                        ((FragmentSetup) fragment).prepareSearch();
                    fragment.scrollTo(resid, -dp24);
                    menuSearch.collapseActionView();

                    // Blink found text
                    View view = fragment.getView();
                    if (view != null) {
                        View child = view.findViewById(resid);
                        if (child != null) {
                            int c = Helper.resolveColor(view.getContext(), R.attr.colorHighlight);
                            Drawable b = child.getBackground();
                            child.post(new Runnable() {
                                private int count = 0;

                                @Override
                                public void run() {
                                    if (count % 2 == 1)
                                        child.setBackground(b);
                                    else
                                        child.setBackgroundColor(c);
                                    if (++count <= 7)
                                        child.postDelayed(this, 250);
                                }
                            });
                        }
                    }
                }

                return true;
            }
        };

        if (searchView != null)
            searchView.setOnSuggestionListener(onSuggestionListener);

        if (searchView != null)
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    searching = query;

                    CursorAdapter adapter = searchView.getSuggestionsAdapter();
                    if (adapter != null && adapter.getCount() > 0)
                        onSuggestionListener.onSuggestionClick(0);

                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if (newText != null)
                        newText = newText.trim();
                    searching = newText;
                    suggest(newText);
                    return false;
                }

                private void suggest(String query) {
                    Bundle args = new Bundle();
                    args.putString("query", query);

                    new SimpleTask<SuggestData>() {
                        @Override
                        protected SuggestData onExecute(Context context, Bundle args) {
                            if (TextUtils.isEmpty(args.getString("query")))
                                return data;

                            return (data == null ? getSuggestData(context) : data);
                        }

                        @Override
                        protected void onExecuted(Bundle args, SuggestData result) {
                            data = result;
                            _suggest(args.getString("query"));
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Log.w(ex);
                            try {
                                // Fallback to UI thread (Android 5.1.1)
                                data = getSuggestData(getContext());
                                _suggest(args.getString("query"));
                            } catch (Throwable exex) {
                                Log.unexpectedError(getParentFragmentManager(), exex);
                            }
                        }

                        private SuggestData getSuggestData(Context context) {
                            SuggestData data = new SuggestData();
                            data.titles = new String[TAB_PAGES.length];
                            data.views = new View[TAB_PAGES.length];

                            LayoutInflater inflater = LayoutInflater.from(context);
                            for (int tab = 0; tab < TAB_PAGES.length; tab++) {
                                data.titles[tab] = context.getString(PAGE_TITLES[tab]);
                                data.views[tab] = inflater.inflate(TAB_PAGES[tab], null);
                            }

                            return data;
                        }
                    }.serial().execute(FragmentOptions.this, args, "option:suggest");
                }

                private void _suggest(String query) {
                    MatrixCursor cursor = new MatrixCursor(new String[]{"_id", "tab", "resid", "title"});

                    if (data != null &&
                            query != null && query.length() > 1) {
                        int id = 0;
                        for (int tab = 0; tab < TAB_PAGES.length; tab++)
                            id = getSuggestions(query.toLowerCase(), id, tab, data.titles[tab], data.views[tab], cursor);
                    }

                    searchView.setSuggestionsAdapter(new SimpleCursorAdapter(
                            pager.getContext(),
                            R.layout.spinner_item1_dropdown,
                            cursor,
                            new String[]{"title"},
                            new int[]{android.R.id.text1},
                            0
                    ));
                    searchView.getSuggestionsAdapter().notifyDataSetChanged();
                }

                private int getSuggestions(String query, int id, int tab, String title, View view, MatrixCursor cursor) {
                    if (view == null ||
                            ("nosuggest".equals(view.getTag()) && !BuildConfig.DEBUG))
                        return id;
                    else if (view instanceof ViewGroup) {
                        ViewGroup group = (ViewGroup) view;
                        for (int i = 0; i <= group.getChildCount(); i++)
                            id = getSuggestions(query, id, tab, title, group.getChildAt(i), cursor);
                    } else if (view instanceof TextView) {
                        boolean extra = false;
                        if (tab == 0 && view.getId() == R.id.tvManual) {
                            for (int e : new int[]{R.string.title_host, R.string.title_port, R.string.title_cc, R.string.title_bcc}) {
                                String text = view.getContext().getString(e);
                                if (text.toLowerCase().contains(query)) {
                                    extra = true;
                                    break;
                                }
                            }
                        }

                        String description = ((TextView) view).getText().toString();
                        if (description.toLowerCase().contains(query) || extra) {
                            description = description
                                    .replace("%%", "%")
                                    .replaceAll("%([0-9]\\$)?[sd]", "#");
                            String text = view.getContext().getString(R.string.title_title_description, title, description);
                            cursor.newRow()
                                    .add(id++)
                                    .add(tab)
                                    .add(view.getId())
                                    .add(text);
                        }
                    }

                    return id;
                }
            });

        if (!TextUtils.isEmpty(saved)) {
            menuSearch.expandActionView();
            if (searchView != null)
                searchView.setQuery(saved, false);
        }

        getViewLifecycleOwner().getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            public void onDestroyed() {
                menuSearch.collapseActionView();
                getViewLifecycleOwner().getLifecycle().removeObserver(this);
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    static void reset(Context context, List<String> options, Runnable confirmed) {
        new AlertDialog.Builder(context)
                .setIcon(R.drawable.twotone_help_24)
                .setTitle(R.string.title_setup_defaults)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                        SharedPreferences.Editor editor = prefs.edit();
                        for (String option : options)
                            editor.remove(option);
                        editor.apply();

                        if (confirmed != null)
                            confirmed.run();

                        ToastEx.makeText(context, R.string.title_setup_done, Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private class PagerAdapter extends FragmentStatePagerAdapter {
        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return TAB_PAGES.length;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new FragmentSetup();
                case 1:
                    return new FragmentOptionsSynchronize();
                case 2:
                    return new FragmentOptionsSend();
                case 3:
                    return new FragmentOptionsConnection();
                case 4:
                    return new FragmentOptionsDisplay();
                case 5:
                    return new FragmentOptionsBehavior();
                case 6:
                    return new FragmentOptionsPrivacy();
                case 7:
                    return new FragmentOptionsEncryption();
                case 8:
                    return new FragmentOptionsNotifications();
                case 9:
                    return new FragmentOptionsIntegrations();
                case 10:
                    return new FragmentOptionsMisc();
                case 11:
                    return new FragmentOptionsBackup();
                default:
                    throw new IllegalArgumentException();
            }
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE; // always recreate fragment
        }
    }

    private static class SuggestData {
        private String[] titles;
        private View[] views;
    }
}
