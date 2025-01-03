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

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

public class FragmentDialogBase extends DialogFragment {
    private boolean hasResult = false;
    private LifecycleOwner owner;
    private LifecycleRegistry registry;
    private String targetRequestKey;
    private int targetRequestCode;
    private Integer orientation = null;

    public String getRequestKey() {
        return Helper.getRequestKey(this);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        owner = new LifecycleOwner() {
            @NonNull
            @Override
            public Lifecycle getLifecycle() {
                return registry;
            }
        };
        registry = new LifecycleRegistry(owner);
        registry.setCurrentState(Lifecycle.State.CREATED);

        if (savedInstanceState != null) {
            targetRequestKey = savedInstanceState.getString("fair:key");
            targetRequestCode = savedInstanceState.getInt("fair:code");
        }

        String requestKey = getRequestKey();
        if (!BuildConfig.PLAY_STORE_RELEASE)
            EntityLog.log(getContext(), "Listening key=" + requestKey);
        getParentFragmentManager().setFragmentResultListener(requestKey, this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                try {
                    result.setClassLoader(ApplicationEx.class.getClassLoader());
                    int requestCode = result.getInt("requestCode");
                    int resultCode = result.getInt("resultCode");

                    Intent data = new Intent();
                    data.putExtra("args", result);
                    onActivityResult(requestCode, resultCode, data);
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }
        });

        Log.i("Create " + this);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("fair:key", targetRequestKey);
        outState.putInt("fair:code", targetRequestCode);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        registry.setCurrentState(Lifecycle.State.RESUMED);
        super.onResume();
        Log.d("Resume " + this);
    }

    @Override
    public void onPause() {
        registry.setCurrentState(Lifecycle.State.STARTED);
        super.onPause();
        Log.d("Pause " + this);
    }

    @Override
    public void onStart() {
        registry.setCurrentState(Lifecycle.State.STARTED);
        try {
            super.onStart();
        } catch (Throwable ex) {
            Log.e(ex);
        }
        Log.d("Start " + this);
    }

    @Override
    public void onStop() {
        registry.setCurrentState(Lifecycle.State.CREATED);
        super.onStop();
        Log.d("Stop " + this);
    }

    @Override
    public void onDestroy() {
        registry.setCurrentState(Lifecycle.State.DESTROYED);
        if (orientation != null) {
            Activity activity = getActivity();
            if (activity != null)
                activity.setRequestedOrientation(orientation);
        }
        super.onDestroy();
        Log.i("Destroy " + this);
    }

    protected void lockOrientation() {
        Activity activity = getActivity();
        if (activity != null) {
            orientation = activity.getRequestedOrientation();
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        String action = (data == null ? null : data.getAction());
        Log.i("Result class=" + this.getClass().getSimpleName() +
                " action=" + action + " request=" + requestCode + " result=" + resultCode);
        Log.logExtras(data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @NonNull
    @Override
    public LifecycleOwner getViewLifecycleOwner() {
        return owner;
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
    public void dismiss() {
        try {
            super.dismiss();
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        sendResult(RESULT_CANCELED);
        super.onDismiss(dialog);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void setTargetFragment(@Nullable Fragment fragment, int requestCode) {
        if (fragment instanceof FragmentBase)
            targetRequestKey = ((FragmentBase) fragment).getRequestKey();
        else if (fragment instanceof FragmentDialogBase)
            targetRequestKey = ((FragmentDialogBase) fragment).getRequestKey();
        else {
            Log.e("setTargetFragment=" + fragment.getClass().getName());
            throw new IllegalArgumentException();
        }

        hasResult = false;
        targetRequestCode = requestCode;
    }

    public void setTargetActivity(ActivityBase activity, int requestCode) {
        targetRequestKey = activity.getRequestKey();
        targetRequestCode = requestCode;
    }

    protected void sendResult(int resultCode) {
        EntityLog.log(getContext(), "Sending key=" + targetRequestKey +
                " request=" + targetRequestCode +
                " result=" + resultCode +
                " has=" + hasResult);

        if (!hasResult || resultCode == RESULT_OK) {
            hasResult = true;

            if (targetRequestKey != null)
                try {
                    Bundle args = getArguments();
                    if (args == null) // onDismiss
                        args = new Bundle();
                    args.putInt("requestCode", targetRequestCode);
                    args.putInt("resultCode", resultCode);
                    getParentFragmentManager().setFragmentResult(targetRequestKey, args);
                } catch (Throwable ex) {
                    Log.w(ex);
                    /*
                        java.lang.IllegalStateException: Fragment FragmentDialog... not associated with a fragment manager.
                            at androidx.fragment.app.Fragment.getParentFragmentManager(SourceFile:2)
                            at eu.faircode.email.FragmentDialogBase.sendResult(SourceFile:9)
                     */
                }
        }
    }

    @Override
    public void startActivity(Intent intent) {
        try {
            super.startActivity(intent);
        } catch (Throwable ex) {
            Helper.reportNoViewer(getContext(), intent, ex);
        }
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        try {
            super.startActivityForResult(intent, requestCode);
        } catch (Throwable ex) {
            Helper.reportNoViewer(getContext(), intent, ex);
        }
    }
}
