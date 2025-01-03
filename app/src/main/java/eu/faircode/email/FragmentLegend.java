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
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

public class FragmentLegend extends FragmentBase {
    private int layout = -1;
    private ViewPager pager;
    private PagerAdapter adapter;

    private FragmentLegend setLayout(int layout) {
        this.layout = layout;
        return this;
    }

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.menu_legend);
        setHasOptionsMenu(true); // To intercept back

        if (savedInstanceState != null)
            layout = savedInstanceState.getInt("fair:layout");

        View view;
        if (layout < 0) {
            view = inflater.inflate(R.layout.fragment_legend, container, false);

            pager = view.findViewById(R.id.pager);
            adapter = new PagerAdapter(getChildFragmentManager());
            pager.setAdapter(adapter);
        } else {
            view = inflater.inflate(layout, container, false);

            if (layout == R.layout.fragment_legend_synchronization) {
                view.findViewById(R.id.ibInfoBackoff).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Helper.viewFAQ(v.getContext(), 123);
                    }
                });
            }
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null)
            layout = savedInstanceState.getInt("fair:layout");

        if (layout < 0) {
            final Context context = getContext();

            TabLayout tabLayout = view.findViewById(R.id.tab_layout);
            tabLayout.setBackgroundColor(Helper.resolveColor(context, R.attr.colorCardBackground));
            tabLayout.setupWithViewPager(pager);

            Bundle args = getArguments();
            if (args != null) {
                String tab = args.getString("tab");
                if ("compose".equals(tab))
                    pager.setCurrentItem(3);

                args.remove("tab");
                setArguments(args);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("fair:layout", layout);
        super.onSaveInstanceState(outState);
    }

    private class PagerAdapter extends FragmentStatePagerAdapter {
        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new FragmentLegend().setLayout(R.layout.fragment_legend_synchronization);
                case 1:
                    return new FragmentLegend().setLayout(R.layout.fragment_legend_folders);
                case 2:
                    return new FragmentLegend().setLayout(R.layout.fragment_legend_messages);
                case 3:
                    return new FragmentLegend().setLayout(R.layout.fragment_legend_compose);
                case 4:
                    return new FragmentLegend().setLayout(R.layout.fragment_legend_keyboard);
                default:
                    throw new IllegalArgumentException();
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.title_legend_section_synchronize);
                case 1:
                    return getString(R.string.title_legend_section_folders);
                case 2:
                    return getString(R.string.title_legend_section_messages);
                case 3:
                    return getString(R.string.title_legend_section_compose);
                case 4:
                    return getString(R.string.title_legend_section_keyboard);
                default:
                    throw new IllegalArgumentException();
            }
        }
    }
}
