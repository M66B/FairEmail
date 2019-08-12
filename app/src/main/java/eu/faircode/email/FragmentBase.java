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

    Copyright 2018-2019 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;

import java.util.HashMap;
import java.util.Map;

public class FragmentBase extends Fragment {
    private String subtitle = " ";
    private boolean finish = false;

    protected void setSubtitle(int resid) {
        setSubtitle(getString(resid));
    }

    protected void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
        updateSubtitle();
    }

    @Override
    public void startActivity(Intent intent) {
        if (Helper.hasAuthentication(getContext()))
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        super.startActivity(intent);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        if (Helper.hasAuthentication(getContext()))
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        super.startActivityForResult(intent, requestCode);
    }

    protected void finish() {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
            getFragmentManager().popBackStack();
        else
            finish = true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.i("Save instance " + this);
        int before = Helper.getSize(outState);
        outState.putString("fair:subtitle", subtitle);
        super.onSaveInstanceState(outState);
        int after = Helper.getSize(outState);
        Log.i("Saved instance " + this + " size=" + before + "/" + after);

        Map<String, String> crumb = new HashMap<>();
        crumb.put("name", this.getClass().getName());
        crumb.put("before", Integer.toString(before));
        crumb.put("after", Integer.toString(after));
        for (String key : outState.keySet()) {
            Object value = outState.get(key);
            crumb.put(key, value == null ? "" : value.getClass().getName());
        }
        Log.breadcrumb("onSaveInstanceState", crumb);

        for (String key : outState.keySet())
            Log.i("Saved " + this + " " + key + "=" + outState.get(key));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i("Create " + this + " saved=" + (savedInstanceState != null));
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null)
            subtitle = savedInstanceState.getString("fair:subtitle");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i("Create view " + this);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i("Activity " + this + " saved=" + (savedInstanceState != null));
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        Log.i("Resume " + this);
        super.onResume();
        updateSubtitle();
        if (finish) {
            getFragmentManager().popBackStack();
            finish = false;
        }
    }

    @Override
    public void onPause() {
        Log.i("Pause " + this);
        super.onPause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.i("Result class=" + this.getClass().getSimpleName() +
                " request=" + requestCode + " result=" + resultCode);
        Log.logExtras(data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        InputMethodManager im = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        View focused = getActivity().getCurrentFocus();
        if (focused != null)
            im.hideSoftInputFromWindow(focused.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.i("Config " + this);
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onDestroy() {
        Log.i("Destroy " + this);
        super.onDestroy();
    }

    @Override
    public void setHasOptionsMenu(boolean hasMenu) {
        super.setHasOptionsMenu(!isPane() && hasMenu);
    }

    private void updateSubtitle() {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null && !isPane()) {
            ActionBar actionbar = activity.getSupportActionBar();
            if (actionbar != null)
                actionbar.setSubtitle(subtitle);
        }
    }

    private boolean isPane() {
        Bundle args = getArguments();
        return (args != null && args.getBoolean("pane"));
    }

    boolean hasPermission(String name) {
        ActivityBase activity = (ActivityBase) getActivity();
        if (activity == null)
            return false;
        return activity.hasPermission(name);
    }

    void addBackPressedListener(ActivityBase.IBackPressedListener listener) {
        ((ActivityBase) getActivity()).addBackPressedListener(listener, getViewLifecycleOwner());
    }

    void addBillingListener(ActivityBilling.IBillingListener listener) {
        ((ActivityBilling) getActivity()).addBillingListener(listener, getViewLifecycleOwner());
    }
}
