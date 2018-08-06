package eu.faircode.email;

import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class FragmentEx extends Fragment {
    private String subtitle = "";

    protected void setSubtitle(int resid) {
        setSubtitle(getString(resid));
    }

    protected void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
        updateSubtitle();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateSubtitle();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        InputMethodManager im = getContext().getSystemService(InputMethodManager.class);
        View focussed = getActivity().getCurrentFocus();
        if (focussed != null)
            im.hideSoftInputFromWindow(focussed.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
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
