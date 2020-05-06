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
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.adapter.FragmentViewHolder;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.List;

public class FragmentOptions extends FragmentBase {
    private ViewPager2 pager;
    private FragmentStateAdapter adapter;

    static String[] OPTIONS_RESTART = new String[]{
            "first", "app_support", "notify_archive", "message_swipe", "message_select", "folder_actions", "folder_sync",
            "subscriptions",
            "landscape", "landscape3", "startup", "cards", "indentation", "date", "threading",
            "highlight_unread", "color_stripe",
            "avatars", "gravatars", "generated_icons", "identicons", "circular", "saturation", "brightness", "threshold",
            "name_email", "prefer_contact", "distinguish_contacts", "show_recipients", "authentication",
            "subject_top", "font_size_sender", "font_size_subject", "subject_italic", "highlight_subject", "subject_ellipsize",
            "keywords_header", "flags", "flags_background", "preview", "preview_italic", "preview_lines",
            "addresses", "button_archive_trash", "button_move", "attachments_alt",
            "contrast", "monospaced", "text_color", "text_size",
            "inline_images", "collapse_quotes", "seekbar", "actionbar", "actionbar_color", "navbar_colorize",
            "autoscroll", "swipenav", "autoexpand", "autoclose", "onclose",
            "language_detection",
            "quick_filter", "quick_scroll",
            "experiments", "debug",
            "biometrics"
    };

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_options, container, false);

        pager = view.findViewById(R.id.pager);

        adapter = new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
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
            public int getItemCount() {
                return 10;
            }

            @Override
            public void onBindViewHolder(@NonNull FragmentViewHolder holder, int position, @NonNull List<Object> payloads) {
                super.onBindViewHolder(holder, position, payloads);

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
        };

        pager.setAdapter(adapter);

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
        new TabLayoutMediator(tabLayout, pager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position) {
                    case 0:
                        tab.setText(R.string.title_advanced_section_main);
                        break;
                    case 1:
                        tab.setText(R.string.title_advanced_section_synchronize);
                        break;
                    case 2:
                        tab.setText(R.string.title_advanced_section_send);
                        break;
                    case 3:
                        tab.setText(R.string.title_advanced_section_connection);
                        break;
                    case 4:
                        tab.setText(R.string.title_advanced_section_display);
                        break;
                    case 5:
                        tab.setText(R.string.title_advanced_section_behavior);
                        break;
                    case 6:
                        tab.setText(R.string.title_advanced_section_privacy);
                        break;
                    case 7:
                        tab.setText(R.string.title_advanced_section_encryption);
                        break;
                    case 8:
                        tab.setText(R.string.title_advanced_section_notifications);
                        break;
                    case 9:
                        tab.setText(R.string.title_advanced_section_misc);
                        break;
                    default:
                        throw new IllegalArgumentException();
                }
            }
        }).attach();

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
}
