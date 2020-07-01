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

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.Group;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.Lifecycle;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

public class FragmentOptions extends FragmentBase {
    private ViewPager pager;
    private PagerAdapter adapter;
    private String searching = null;

    private static int[] TAB_PAGES = {
            R.layout.fragment_setup,
            R.layout.fragment_options_synchronize,
            R.layout.fragment_options_send,
            R.layout.fragment_options_connection,
            R.layout.fragment_options_display,
            R.layout.fragment_options_behavior,
            R.layout.fragment_options_privacy,
            R.layout.fragment_options_encryption,
            R.layout.fragment_options_notifications,
            R.layout.fragment_options_misc
    };

    static String[] OPTIONS_RESTART = new String[]{
            "first", "app_support", "notify_archive", "message_swipe", "message_select", "folder_actions", "folder_sync",
            "subscriptions",
            "portrait2", "landscape", "landscape3", "startup", "cards", "indentation", "date", "threading", "threading_unread",
            "highlight_unread", "color_stripe",
            "avatars", "gravatars", "favicons", "generated_icons", "identicons", "circular", "saturation", "brightness", "threshold",
            "name_email", "prefer_contact", "distinguish_contacts", "show_recipients", "authentication",
            "subject_top", "font_size_sender", "font_size_subject", "subject_italic", "highlight_subject", "subject_ellipsize",
            "keywords_header", "labels_header", "flags", "flags_background", "preview", "preview_italic", "preview_lines",
            "addresses", "attachments_alt", "thumbnails",
            "contrast", "monospaced", "text_color", "text_size", "text_font", "text_align",
            "inline_images", "collapse_quotes", "seekbar", "actionbar", "actionbar_color", "navbar_colorize",
            "autoscroll", "swipenav", "autoexpand", "autoclose", "onclose",
            "language_detection",
            "quick_filter", "quick_scroll",
            "experiments", "debug",
            "biometrics"
    };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("fair:searching", searching);
        super.onSaveInstanceState(outState);
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
                if (position > 0) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                    boolean setup_advanced = prefs.getBoolean("setup_advanced", false);
                    if (!setup_advanced) {
                        prefs.edit().putBoolean("setup_advanced", true).apply();
                        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
                        lbm.sendBroadcast(new Intent(ActivitySetup.ACTION_SETUP_ADVANCED));
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // Do nothing
            }
        });

        addKeyPressedListener(new ActivityBase.IKeyPressedListener() {
            @Override
            public boolean onKeyPressed(KeyEvent event) {
                return false;
            }

            @Override
            public boolean onBackPressed() {
                if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                    onExit();
                    return true;
                } else
                    return false;

            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(pager);

        String tab = getActivity().getIntent().getStringExtra("tab");
        if ("connection".equals(tab))
            pager.setCurrentItem(3);
        else if ("display".equals(tab))
            pager.setCurrentItem(4);
        else if ("encryption".equals(tab))
            pager.setCurrentItem(7);
        getActivity().getIntent().removeExtra("tab");
    }

    @Override
    protected void finish() {
        onExit();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_setup, menu);

        final String saved = searching;
        final MenuItem menuSearch = menu.findItem(R.id.menu_search);
        final SearchView searchView = (SearchView) menuSearch.getActionView();

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
                    fragment.scrollTo(resid);
                    menuSearch.collapseActionView();
                }

                return true;
            }
        };

        searchView.setOnSuggestionListener(onSuggestionListener);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            private String[] titles = null;
            private View[] views = null;

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
                searching = newText;
                suggest(newText);
                return false;
            }

            private void suggest(String query) {
                MatrixCursor cursor = new MatrixCursor(new String[]{"_id", "tab", "resid", "title"});

                if (query != null && query.length() > 1) {
                    if (titles == null || views == null) {
                        titles = new String[TAB_PAGES.length];
                        views = new View[TAB_PAGES.length];
                        LayoutInflater inflater = LayoutInflater.from(searchView.getContext());
                        for (int tab = 0; tab < TAB_PAGES.length; tab++) {
                            titles[tab] = (String) adapter.getPageTitle(tab);
                            views[tab] = inflater.inflate(TAB_PAGES[tab], null);
                        }
                    }

                    int id = 0;
                    for (int tab = 0; tab < TAB_PAGES.length; tab++)
                        id = getSuggestions(query.toLowerCase(), id, tab, titles[tab], views[tab], cursor);
                }

                searchView.setSuggestionsAdapter(new SimpleCursorAdapter(
                        searchView.getContext(),
                        R.layout.spinner_item1_dropdown,
                        cursor,
                        new String[]{"title"},
                        new int[]{android.R.id.text1},
                        0
                ));
            }

            private int getSuggestions(String query, int id, int tab, String title, View view, MatrixCursor cursor) {
                if (view instanceof ViewGroup) {
                    ViewGroup group = (ViewGroup) view;
                    for (int i = 0; i <= group.getChildCount(); i++)
                        id = getSuggestions(query, id, tab, title, group.getChildAt(i), cursor);
                } else if (view instanceof TextView) {
                    String description = ((TextView) view).getText().toString();
                    if (description.toLowerCase().contains(query)) {
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
            searchView.setQuery(saved, false);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            switch (requestCode) {
                case ActivitySetup.REQUEST_STILL:
                    if (resultCode == Activity.RESULT_OK)
                        pager.setCurrentItem(0);
                    else
                        super.finish();
                    break;
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    private void onExit() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean setup_reminder = prefs.getBoolean("setup_reminder", true);

        boolean hasPermissions = hasPermission(Manifest.permission.READ_CONTACTS);
        Boolean isIgnoring = Helper.isIgnoringOptimizations(getContext());

        if (!setup_reminder ||
                (hasPermissions && (isIgnoring == null || isIgnoring)))
            super.finish();
        else {
            FragmentDialogStill fragment = new FragmentDialogStill();
            fragment.setTargetFragment(this, ActivitySetup.REQUEST_STILL);
            fragment.show(getParentFragmentManager(), "setup:still");
        }
    }

    public static class FragmentDialogStill extends FragmentDialogBase {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            View dview = LayoutInflater.from(getContext()).inflate(R.layout.dialog_setup, null);
            CheckBox cbNotAgain = dview.findViewById(R.id.cbNotAgain);
            Group grp3 = dview.findViewById(R.id.grp3);
            Group grp4 = dview.findViewById(R.id.grp4);

            cbNotAgain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                    prefs.edit().putBoolean("setup_reminder", !isChecked).apply();
                }
            });

            boolean hasPermissions = Helper.hasPermission(getContext(), Manifest.permission.READ_CONTACTS);
            Boolean isIgnoring = Helper.isIgnoringOptimizations(getContext());

            grp3.setVisibility(hasPermissions ? View.GONE : View.VISIBLE);
            grp4.setVisibility(isIgnoring == null || isIgnoring ? View.GONE : View.VISIBLE);

            return new AlertDialog.Builder(getContext())
                    .setView(dview)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sendResult(Activity.RESULT_OK);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
        }
    }

    private class PagerAdapter extends FragmentStatePagerAdapter {
        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 10;
        }

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
                    return new FragmentOptionsMisc();
                default:
                    throw new IllegalArgumentException();
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.title_advanced_section_main);
                case 1:
                    return getString(R.string.title_advanced_section_synchronize);
                case 2:
                    return getString(R.string.title_advanced_section_send);
                case 3:
                    return getString(R.string.title_advanced_section_connection);
                case 4:
                    return getString(R.string.title_advanced_section_display);
                case 5:
                    return getString(R.string.title_advanced_section_behavior);
                case 6:
                    return getString(R.string.title_advanced_section_privacy);
                case 7:
                    return getString(R.string.title_advanced_section_encryption);
                case 8:
                    return getString(R.string.title_advanced_section_notifications);
                case 9:
                    return getString(R.string.title_advanced_section_misc);
                default:
                    throw new IllegalArgumentException();
            }
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE; // always recreate fragment
        }
    }
}
