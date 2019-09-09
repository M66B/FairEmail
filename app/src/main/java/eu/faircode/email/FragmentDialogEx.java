package eu.faircode.email;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import static android.app.Activity.RESULT_CANCELED;

public class FragmentDialogEx extends DialogFragment {
    private boolean once = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("Create " + this);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i("Resume " + this);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i("Pause " + this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("Destroy " + this);
    }

    @Override
    public void onStart() {
        try {
            super.onStart();
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    @Override
    public void show(@NonNull FragmentManager manager, @Nullable String tag) {
        try {
            super.show(manager, tag);
        } catch (Throwable ex) {
            // IllegalStateException Can not perform this action after onSaveInstanceState
            // Should not happen, but still happened in AdapterMessage.onOpenLink
            Log.e(ex);
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        sendResult(RESULT_CANCELED);
        super.onDismiss(dialog);
    }

    @Override
    public void setTargetFragment(@Nullable Fragment fragment, int requestCode) {
        super.setTargetFragment(fragment, requestCode);
        Log.i("Set target " + this + " " + fragment);

        fragment.getViewLifecycleOwner().getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            public void onDestroy() {
                Log.i("Reset target " + FragmentDialogEx.this);
                FragmentDialogEx.super.setTargetFragment(null, requestCode);
            }
        });
    }

    protected void sendResult(int result) {
        if (!once) {
            once = true;
            Fragment target = getTargetFragment();
            Log.i("Dialog target=" + target + " result=" + result);
            if (target != null) {
                Intent data = new Intent();
                data.putExtra("args", getArguments());
                target.onActivityResult(getTargetRequestCode(), result, data);
            }
        }
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
}
