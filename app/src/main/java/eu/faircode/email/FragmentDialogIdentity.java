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

    Copyright 2018-2021 by Marcel Bokhorst (M66B)
*/

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LifecycleOwner;
import androidx.preference.PreferenceManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class FragmentDialogIdentity extends FragmentDialogBase {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View dview = LayoutInflater.from(getContext()).inflate(R.layout.dialog_identity, null);
        final Spinner spIdentity = dview.findViewById(R.id.spIdentity);
        final CheckBox cbNotAgain = dview.findViewById(R.id.cbNotAgain);
        final Button btnFix = dview.findViewById(R.id.btnFix);
        final Group grpIdentities = dview.findViewById(R.id.grpIdentities);
        final Group grpNoIdentities = dview.findViewById(R.id.grpNoIdentities);
        final ContentLoadingProgressBar pbWait = dview.findViewById(R.id.pbWait);

        cbNotAgain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                prefs.edit().putBoolean("identities_asked", isChecked).apply();
            }
        });

        btnFix.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), ActivitySetup.class));
                getActivity().finish();
                dismiss();
            }
        });

        grpIdentities.setVisibility(View.GONE);
        grpNoIdentities.setVisibility(View.GONE);

        new SimpleTask<List<TupleIdentityEx>>() {
            @Override
            protected void onPreExecute(Bundle args) {
                pbWait.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onPostExecute(Bundle args) {
                pbWait.setVisibility(View.GONE);
            }

            @Override
            protected List<TupleIdentityEx> onExecute(Context context, Bundle args) {
                DB db = DB.getInstance(context);
                return db.identity().getComposableIdentities(null);
            }

            @Override
            protected void onExecuted(Bundle args, List<TupleIdentityEx> identities) {
                AdapterIdentitySelect iadapter = new AdapterIdentitySelect(getContext(), identities);
                spIdentity.setAdapter(iadapter);

                Integer fallback = null;
                long account = getArguments().getLong("account");
                for (int pos = 0; pos < identities.size(); pos++) {
                    EntityIdentity identity = identities.get(pos);
                    if (identity.account.equals(account)) {
                        if (identity.primary) {
                            fallback = null;
                            spIdentity.setSelection(pos);
                            break;
                        }
                        if (fallback == null)
                            fallback = pos;
                    }
                }
                if (fallback != null)
                    spIdentity.setSelection(fallback);

                grpIdentities.setVisibility(identities.size() > 0 ? View.VISIBLE : View.GONE);
                grpNoIdentities.setVisibility(identities.size() > 0 ? View.GONE : View.VISIBLE);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, new Bundle(), "identity:select");

        return new AlertDialog.Builder(getContext())
                .setView(dview)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TupleIdentityEx identity = (TupleIdentityEx) spIdentity.getSelectedItem();
                        if (identity != null)
                            startActivity(new Intent(getContext(), ActivityCompose.class)
                                    .putExtra("action", "new")
                                    .putExtra("account", identity.account)
                                    .putExtra("identity", identity.id)
                            );
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    static void onCompose(Context context, LifecycleOwner owner, FragmentManager manager, FloatingActionButton fabCompose, long account) {
        Bundle args = new Bundle();
        args.putLong("account", account);

        new SimpleTask<Boolean>() {
            @Override
            protected void onPreExecute(Bundle args) {
                fabCompose.setEnabled(false);
            }

            @Override
            protected void onPostExecute(Bundle args) {
                fabCompose.setEnabled(true);
            }

            @Override
            protected Boolean onExecute(Context context, Bundle args) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                boolean identities_asked = prefs.getBoolean("identities_asked", false);
                if (identities_asked)
                    return false;

                DB db = DB.getInstance(context);
                List<TupleIdentityEx> identities = db.identity().getComposableIdentities(null);
                return (identities != null && identities.size() > 1);
            }

            @Override
            protected void onExecuted(Bundle args, Boolean ask) {
                if (ask) {
                    FragmentDialogIdentity fragment = new FragmentDialogIdentity();
                    fragment.setArguments(args);
                    fragment.show(manager, "identity:select");
                } else
                    context.startActivity(new Intent(context, ActivityCompose.class)
                            .putExtra("action", "new")
                            .putExtra("account", account));
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(manager, ex);
            }
        }.execute(context, owner, args, "identity:compose");
    }
}
