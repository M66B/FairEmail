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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class FragmentLegend extends FragmentBase {
    private int layout = -1;
    private ViewPager2 pager;
    private FragmentStateAdapter adapter;

    private FragmentLegend setLayout(int layout) {
        this.layout = layout;
        return this;
    }

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.menu_legend);

        if (savedInstanceState != null)
            layout = savedInstanceState.getInt("fair:layout");

        View view;
        if (layout < 0) {
            view = inflater.inflate(R.layout.fragment_legend, container, false);

            pager = view.findViewById(R.id.pager);

            adapter = new FragmentStateAdapter(this) {
                @Override
                public int getItemCount() {
                    return 5;
                }

                @NonNull
                @Override
                public Fragment createFragment(int position) {
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
            };

            pager.setAdapter(adapter);
        } else
            view = inflater.inflate(layout, container, false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null)
            layout = savedInstanceState.getInt("fair:layout");

        if (layout < 0) {
            TabLayout tabLayout = view.findViewById(R.id.tab_layout);
            new TabLayoutMediator(tabLayout, pager, new TabLayoutMediator.TabConfigurationStrategy() {
                @Override
                public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                    switch (position) {
                        case 0:
                            tab.setText(R.string.title_legend_section_synchronize);
                            break;
                        case 1:
                            tab.setText(R.string.title_legend_section_folders);
                            break;
                        case 2:
                            tab.setText(R.string.title_legend_section_messages);
                            break;
                        case 3:
                            tab.setText(R.string.title_legend_section_compose);
                            break;
                        case 4:
                            tab.setText(R.string.title_legend_section_keyboard);
                            break;
                        default:
                            throw new IllegalArgumentException("Position=" + position);
                    }
                }
            }).attach();

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
}
