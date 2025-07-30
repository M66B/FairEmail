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

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LifecycleOwner;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class FragmentDialogIdentity extends FragmentDialogBase {
    private static final int MIN_IDENTITY_MESSAGES = 20;
    private static final float MIN_IDENTITY_THRESHOLD = 0.66f; // percentage

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Context context = getContext();
        View dview = LayoutInflater.from(context).inflate(R.layout.dialog_identity, null);
        final Spinner spIdentity = dview.findViewById(R.id.spIdentity);
        final TextView tvPrimaryHint = dview.findViewById(R.id.tvPrimaryHint);
        final CheckBox cbNotAgain = dview.findViewById(R.id.cbNotAgain);
        final Button btnFix = dview.findViewById(R.id.btnFix);
        final Group grpIdentities = dview.findViewById(R.id.grpIdentities);
        final Group grpNoIdentities = dview.findViewById(R.id.grpNoIdentities);
        final ContentLoadingProgressBar pbWait = dview.findViewById(R.id.pbWait);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean identities_primary_hint = prefs.getBoolean("identities_primary_hint", false);

        spIdentity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                Object tag = spIdentity.getTag();
                if (tag != null && !tag.equals(position)) {
                    TupleIdentityEx identity = (TupleIdentityEx) spIdentity.getAdapter().getItem(position);
                    startActivity(new Intent(v.getContext(), ActivityCompose.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            .putExtra("action", "new")
                            .putExtra("account", identity.account)
                            .putExtra("identity", identity.id)
                    );
                    dismiss();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        tvPrimaryHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prefs.edit().putBoolean("identities_primary_hint", true).apply();
                v.getContext().startActivity(new Intent(v.getContext(), ActivitySetup.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .putExtra("manual", true));
                tvPrimaryHint.setVisibility(View.GONE);
            }
        });

        cbNotAgain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("identities_asked", isChecked).apply();
            }
        });

        btnFix.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.getContext().startActivity(new Intent(v.getContext(), ActivitySetup.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .putExtra("manual", true));
                getActivity().finish();
                dismiss();
            }
        });

        tvPrimaryHint.setVisibility(View.GONE);
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
                if (identities == null)
                    identities = new ArrayList<>();

                AdapterIdentitySelect iadapter = new AdapterIdentitySelect(context, identities);
                spIdentity.setAdapter(iadapter);

                long aid = args.getLong("account");
                long iid = args.getLong("identity", -1L);

                Integer selected = null;
                for (int pos = 0; pos < identities.size(); pos++) {
                    EntityIdentity identity = identities.get(pos);
                    if (iid < 0) {
                        if (identity.account.equals(aid)) {
                            if (identity.primary) {
                                selected = pos;
                                break;
                            }
                            if (selected == null)
                                selected = pos;
                        }
                    } else {
                        if (identity.id.equals(iid)) {
                            selected = pos;
                            break;
                        }
                    }
                }

                if (selected == null && identities.size() > 0)
                    selected = 0;

                if (selected != null) {
                    spIdentity.setTag(selected);
                    spIdentity.setSelection(selected);
                }

                if (identities.size() == 0) {
                    AlertDialog dialog = ((AlertDialog) getDialog());
                    if (dialog != null)
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }
                tvPrimaryHint.setVisibility(identities_primary_hint || identities.size() == 0 ? View.GONE : View.VISIBLE);
                grpIdentities.setVisibility(identities.size() > 0 ? View.VISIBLE : View.GONE);
                grpNoIdentities.setVisibility(identities.size() > 0 ? View.GONE : View.VISIBLE);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, getArguments(), "identity:select");

        return new AlertDialog.Builder(context)
                .setView(dview)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TupleIdentityEx identity = (TupleIdentityEx) spIdentity.getSelectedItem();
                        if (identity != null)
                            startActivity(new Intent(context, ActivityCompose.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    .putExtra("action", "new")
                                    .putExtra("account", identity.account)
                                    .putExtra("identity", identity.id)
                            );
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    static void onCompose(Context context, LifecycleOwner owner, FragmentManager manager, FloatingActionButton fabCompose, long account, long folder) {
        Bundle args = new Bundle();
        args.putLong("account", account);
        args.putLong("folder", folder);

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
                DB db = DB.getInstance(context);

                long folder = args.getLong("folder");
                if (folder >= 0) {
                    List<TupleIdentityCount> counts = db.message().getIdentitiesByFolder(folder);
                    if (counts != null) {
                        int total = 0;
                        TupleIdentityCount first = null;
                        for (TupleIdentityCount entry : counts) {
                            total += entry.count;
                            if (first == null && entry.identity != null)
                                first = entry;
                            Log.i("Dominant identity=" + entry.identity + " count=" + entry.count);
                        }
                        Log.i("Dominant " + counts.size() + " identities " + total + " messages");
                        if (first != null && first.count >= MIN_IDENTITY_MESSAGES) {
                            float percentage = first.count / (float) total;
                            Log.i("Dominant identity percentage=" + percentage);
                            if (percentage > MIN_IDENTITY_THRESHOLD)
                                args.putLong("identity", first.identity);
                        }
                    }
                }

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                boolean identities_asked = prefs.getBoolean("identities_asked", false);
                if (identities_asked)
                    return false;

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
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            .putExtra("action", "new")
                            .putExtra("account", account)
                            .putExtra("identity", args.getLong("identity", -1L)));
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(manager, ex);
            }
        }.execute(context, owner, args, "identity:compose");
    }

    static void onDrafts(Context context, LifecycleOwner owner, FragmentManager manager, FloatingActionButton fabCompose, long account) {
        Bundle args = new Bundle();
        args.putLong("account", account);

        new SimpleTask<EntityFolder>() {
            @Override
            protected void onPreExecute(Bundle args) {
                fabCompose.setEnabled(false);
            }

            @Override
            protected void onPostExecute(Bundle args) {
                fabCompose.setEnabled(true);
            }

            @Override
            protected EntityFolder onExecute(Context context, Bundle args) {
                long account = args.getLong("account");

                DB db = DB.getInstance(context);
                if (account < 0)
                    return db.folder().getFolderPrimary(EntityFolder.DRAFTS);
                else
                    return db.folder().getFolderByType(account, EntityFolder.DRAFTS);
            }

            @Override
            protected void onExecuted(Bundle args, EntityFolder drafts) {
                if (drafts == null)
                    return;

                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                lbm.sendBroadcast(
                        new Intent(ActivityView.ACTION_VIEW_MESSAGES)
                                .putExtra("account", drafts.account)
                                .putExtra("folder", drafts.id)
                                .putExtra("type", drafts.type));
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(manager, ex);
            }
        }.execute(context, owner, args, "view:drafts");
    }
}
