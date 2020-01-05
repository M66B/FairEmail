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

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import androidx.lifecycle.OnLifecycleEvent;

import static android.app.Activity.RESULT_CANCELED;

public class FragmentDialogBase extends DialogFragment {
    private boolean once = false;
    private LifecycleOwner owner;
    private LifecycleRegistry registry;

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
        Log.i("Create " + this);
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
    public void onDestroy() {
        registry.setCurrentState(Lifecycle.State.DESTROYED);
        super.onDestroy();
        Log.i("Destroy " + this);
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
                Log.i("Reset target " + FragmentDialogBase.this);
                FragmentDialogBase.super.setTargetFragment(null, requestCode);
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
        try {
            super.startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Log.e(ex);
            ToastEx.makeText(getContext(), getString(R.string.title_no_viewer, intent.getAction()), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        try {
            super.startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException ex) {
            Log.e(ex);
            ToastEx.makeText(getContext(), getString(R.string.title_no_viewer, intent.getAction()), Toast.LENGTH_LONG).show();
        }
    }
}
