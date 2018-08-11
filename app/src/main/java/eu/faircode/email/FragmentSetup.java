package eu.faircode.email;

/*
    This file is part of Safe email.

    Safe email is free software: you can redistribute it and/or modify
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

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;

public class FragmentSetup extends FragmentEx {
    private Button btnAccount;
    private ProgressBar pbAccount;
    private TextView tvAccountDone;

    private Button btnIdentity;
    private ProgressBar pbIdentity;
    private TextView tvIdentityDone;

    private Button btnPermissions;
    private TextView tvPermissionsDone;

    private CheckBox cbDarkTheme;
    private CheckBox cbDebug;

    private static final String[] permissions = new String[]{
            Manifest.permission.READ_CONTACTS
    };

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_setup);

        View view = inflater.inflate(R.layout.fragment_setup, container, false);

        // Get controls
        btnAccount = view.findViewById(R.id.btnAccount);
        pbAccount = view.findViewById(R.id.pbAccount);
        tvAccountDone = view.findViewById(R.id.tvAccountDone);

        btnIdentity = view.findViewById(R.id.btnIdentity);
        pbIdentity = view.findViewById(R.id.pbIdentity);
        tvIdentityDone = view.findViewById(R.id.tvIdentityDone);

        btnPermissions = view.findViewById(R.id.btnPermissions);
        tvPermissionsDone = view.findViewById(R.id.tvPermissionsDone);

        cbDarkTheme = view.findViewById(R.id.cbDarkTheme);
        cbDebug = view.findViewById(R.id.cbDebug);

        // Wire controls

        btnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.content_frame, new FragmentAccounts()).addToBackStack("accounts");
                fragmentTransaction.commit();
            }
        });

        btnIdentity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.content_frame, new FragmentIdentities()).addToBackStack("identities");
                fragmentTransaction.commit();
            }
        });

        btnPermissions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnPermissions.setEnabled(false);
                requestPermissions(permissions, 1);
            }
        });

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        String theme = prefs.getString("theme", "light");
        boolean dark = "dark".equals(theme);
        cbDarkTheme.setTag(dark);
        cbDarkTheme.setChecked(dark);
        cbDarkTheme.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton button, boolean checked) {
                if (checked != (Boolean) button.getTag()) {
                    button.setTag(checked);
                    cbDarkTheme.setChecked(checked);
                    prefs.edit().putString("theme", checked ? "dark" : "light").apply();
                }
            }
        });

        cbDebug.setChecked(prefs.getBoolean("debug", false));
        cbDebug.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("debug", checked).apply();
            }
        });

        // Initialize

        pbAccount.setVisibility(View.GONE);
        pbIdentity.setVisibility(View.GONE);
        tvAccountDone.setVisibility(View.INVISIBLE);
        tvIdentityDone.setVisibility(View.INVISIBLE);
        tvPermissionsDone.setVisibility(View.INVISIBLE);

        int[] grantResults = new int[permissions.length];
        for (int i = 0; i < permissions.length; i++)
            grantResults[i] = ContextCompat.checkSelfPermission(getActivity(), permissions[i]);

        onRequestPermissionsResult(0, permissions, grantResults);

        // Create outbox
        new SimpleLoader<Void>() {
            @Override
            public Void onLoad(Bundle args) throws Throwable {
                DB db = DB.getInstance(getContext());
                EntityFolder outbox = db.folder().getOutbox();
                if (outbox == null) {
                    outbox = new EntityFolder();
                    outbox.name = "OUTBOX";
                    outbox.type = EntityFolder.OUTBOX;
                    outbox.synchronize = false;
                    outbox.after = 0;
                    outbox.id = db.folder().insertFolder(outbox);
                }
                return null;
            }

            @Override
            public void onException(Bundle args, Throwable ex) {
                Toast.makeText(getContext(), ex.toString(), Toast.LENGTH_LONG).show();
            }
        }.load(this, ActivitySetup.LOADER_CREATE_OUTBOX, new Bundle());

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        DB db = DB.getInstance(getContext());

        db.account().liveAccounts(true).observe(getViewLifecycleOwner(), new Observer<List<EntityAccount>>() {
            @Override
            public void onChanged(@Nullable List<EntityAccount> accounts) {
                tvAccountDone.setVisibility(accounts.size() > 0 ? View.VISIBLE : View.INVISIBLE);
            }
        });

        db.identity().liveIdentities(true).observe(getViewLifecycleOwner(), new Observer<List<EntityIdentity>>() {
            @Override
            public void onChanged(@Nullable List<EntityIdentity> identities) {
                tvIdentityDone.setVisibility(identities.size() > 0 ? View.VISIBLE : View.INVISIBLE);
            }
        });
    }

    @Override
    public void onDestroy() {
        if (tvAccountDone.getVisibility() == View.VISIBLE)
            startActivity(new Intent(getContext(), ActivityView.class).putExtra("setup", true));
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean has = (grantResults.length > 0);
        for (int result : grantResults)
            if (result != PackageManager.PERMISSION_GRANTED) {
                has = false;
                break;
            }

        btnPermissions.setEnabled(!has);
        tvPermissionsDone.setVisibility(has ? View.VISIBLE : View.INVISIBLE);
    }
}
