package eu.faircode.email;

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

public class FragmentEx extends Fragment {
    private String subtitle = " ";

    protected void setSubtitle(int resid) {
        setSubtitle(getString(resid));
    }

    protected void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
        updateSubtitle();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(Helper.TAG, "Create " + this.getClass().getName());
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(Helper.TAG, "Create view " + this.getClass().getName());
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(Helper.TAG, "Activity " + this.getClass().getName());
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        Log.i(Helper.TAG, "Resume " + this.getClass().getName());
        super.onResume();
        updateSubtitle();
    }

    @Override
    public void onPause() {
        Log.i(Helper.TAG, "Pause " + this.getClass().getName());
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
        Log.i(Helper.TAG, "Config " + this.getClass().getName());
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onDestroy() {
        Log.i(Helper.TAG, "Destroy " + this.getClass().getName());
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
