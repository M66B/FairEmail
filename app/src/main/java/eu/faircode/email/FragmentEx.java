package eu.faircode.email;

/*
    This file is part of FairEmail.

    FairEmail is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    NetGuard is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with NetGuard.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2018 by Marcel Bokhorst (M66B)
*/

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;

public class FragmentEx extends Fragment {
    private String subtitle = " ";
    private boolean finish = false;

    protected void setSubtitle(int resid) {
        setSubtitle(getString(resid));
    }

    protected void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
        updateSubtitle();
    }

    protected void finish() {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED))
            getFragmentManager().popBackStack();
        else
            finish = true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(Helper.TAG, "Create " + this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(Helper.TAG, "Create view " + this);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(Helper.TAG, "Activity " + this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        Log.i(Helper.TAG, "Resume " + this);
        super.onResume();
        updateSubtitle();
        if (finish) {
            getFragmentManager().popBackStack();
            finish = false;
        }
    }

    @Override
    public void onPause() {
        Log.i(Helper.TAG, "Pause " + this);
        super.onPause();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        InputMethodManager im = getContext().getSystemService(InputMethodManager.class);
        View focused = getActivity().getCurrentFocus();
        if (focused != null)
            im.hideSoftInputFromWindow(focused.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.i(Helper.TAG, "Config " + this);
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onDestroy() {
        Log.i(Helper.TAG, "Destroy " + this);
        super.onDestroy();
    }

    private void updateSubtitle() {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            ActionBar actionbar = activity.getSupportActionBar();
            if (actionbar != null)
                actionbar.setSubtitle(subtitle);
        }
    }
}
