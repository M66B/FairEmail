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
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FragmentDialogForwardRaw extends FragmentDialogBase {
    private boolean enabled;

    private static final long AUTO_CONFIRM_DELAY = 2 * 1000L;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        long[] ids = args.getLongArray("ids");

        if (savedInstanceState != null)
            enabled = savedInstanceState.getBoolean("fair:enabled");

        final Context context = getContext();
        View dview = LayoutInflater.from(context).inflate(R.layout.dialog_forward_raw, null);
        ProgressBar pbDownloaded = dview.findViewById(R.id.pbDownloaded);
        TextView tvRemaining = dview.findViewById(R.id.tvRemaining);
        TextView tvOption = dview.findViewById(R.id.tvOption);
        TextView tvNoInternet = dview.findViewById(R.id.tvNoInternet);
        CheckBox cbAutoConfirm = dview.findViewById(R.id.cbAutoConfirm);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean eml_auto_confirm = prefs.getBoolean("eml_auto_confirm", false);

        pbDownloaded.setProgress(0);
        pbDownloaded.setMax(ids.length);
        tvRemaining.setText(getString(R.string.title_eml_downloaded, "-"));
        cbAutoConfirm.setChecked(eml_auto_confirm);

        tvOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.getContext().startActivity(new Intent(v.getContext(), ActivitySetup.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .putExtra("tab", "connection"));
            }
        });

        cbAutoConfirm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("eml_auto_confirm", checked).apply();
            }
        });

        NumberFormat NF = NumberFormat.getNumberInstance();

        new SimpleTask<long[]>() {
            @Override
            protected long[] onExecute(Context context, Bundle args) {
                long[] ids = args.getLongArray("ids");
                boolean threads = args.getBoolean("threads");

                Long aid = null;
                List<Long> result = new ArrayList<>();

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    List<String> hashes = new ArrayList<>();

                    for (long id : ids) {
                        EntityMessage message = db.message().getMessage(id);
                        if (message == null)
                            continue;

                        EntityFolder folder = db.folder().getFolder(message.folder);
                        if (folder == null)
                            continue;

                        EntityAccount account = db.account().getAccount(message.account);
                        if (account == null)
                            continue;
                        if (aid == null)
                            aid = account.id;
                        else if (!Objects.equals(aid, account.id))
                            aid = -1L;

                        if (account.protocol == EntityAccount.TYPE_IMAP) {
                            if (message.uid == null)
                                continue;
                        } else if (account.protocol == EntityAccount.TYPE_POP) {
                            if (!EntityFolder.INBOX.equals(folder.type))
                                continue;
                        }

                        List<EntityMessage> messages = db.message().getMessagesByThread(
                                message.account, message.thread, threads ? null : id, null);
                        if (messages == null)
                            continue;

                        for (EntityMessage thread : messages) {
                            EntityLog.log(context, "Fetching raw id=" + thread.id + " subject=" + thread.subject);
                            if (threads) {
                                String hash = (message.hash == null ? message.msgid : message.hash);
                                if (hashes.contains(hash))
                                    continue;
                                hashes.add(hash);
                            }

                            result.add(thread.id);

                            if (thread.raw == null || !thread.raw)
                                EntityOperation.queue(context, thread, EntityOperation.RAW);
                        }
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                ServiceSynchronize.eval(context, "raw");

                args.putLong("account", aid == null ? -1L : aid);
                return Helper.toLongArray(result);
            }

            @Override
            protected void onExecuted(Bundle args, long[] ids) {
                DB db = DB.getInstance(context);
                LiveData<Integer> ld = db.message().liveRaw(ids);
                ld.observe(getViewLifecycleOwner(), new Observer<Integer>() {
                    @Override
                    public void onChanged(Integer remaining) {
                        if (remaining == null)
                            return;

                        pbDownloaded.setProgress(ids.length - remaining);

                        String of = getString(R.string.title_of,
                                NF.format(ids.length - remaining),
                                NF.format(ids.length));
                        tvRemaining.setText(getString(R.string.title_eml_downloaded, of));

                        if (remaining == 0) {
                            ld.removeObserver(this);
                            getArguments().putLong("account", args.getLong("account"));
                            getArguments().putLongArray("ids", ids);

                            enabled = true;

                            Button ok = getPositiveButton();
                            if (ok == null)
                                return;

                            ok.setEnabled(enabled);

                            boolean eml_auto_confirm = prefs.getBoolean("eml_auto_confirm", false);
                            if (eml_auto_confirm)
                                ok.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                                                return;
                                            Button ok = getPositiveButton();
                                            if (ok != null)
                                                ok.performClick();
                                        } catch (Throwable ex) {
                                            Log.e(ex);
                                        }
                                    }
                                }, AUTO_CONFIRM_DELAY);
                        }
                    }
                });
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, getArguments(), "messages:forward");

        return new AlertDialog.Builder(context)
                .setView(dview)
                .setPositiveButton(R.string.title_send, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        long account = getArguments().getLong("account", -1L);
                        long[] ids = getArguments().getLongArray("ids");
                        send(account, ids);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        checkInternet.run();
    }

    @Override
    public void onStart() {
        super.onStart();
        Button ok = getPositiveButton();
        if (ok != null)
            ok.setEnabled(enabled);
    }

    private Button getPositiveButton() {
        return ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE);
    }

    @Override
    public void onResume() {
        super.onResume();

        ConnectivityManager cm = Helper.getSystemService(getContext(), ConnectivityManager.class);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        cm.registerNetworkCallback(builder.build(), networkCallback);
    }

    @Override
    public void onPause() {
        super.onPause();

        ConnectivityManager cm = Helper.getSystemService(getContext(), ConnectivityManager.class);
        cm.unregisterNetworkCallback(networkCallback);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean("fair:enabled", enabled);
        super.onSaveInstanceState(outState);
    }

    private void send(long account, long[] ids) {
        try {
            final Context context = getContext();
            ArrayList<Uri> uris = new ArrayList<>();
            for (long id : ids) {
                EntityLog.log(context, "Sending raw id=" + id);
                File file = EntityMessage.getRawFile(context, id);
                Uri uri = FileProviderEx.getUri(context, BuildConfig.APPLICATION_ID, file);
                uris.add(uri);
            }

            Intent send = new Intent(Intent.ACTION_SEND_MULTIPLE);
            send.setPackage(BuildConfig.APPLICATION_ID);
            send.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            send.setType("message/rfc822");
            send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            send.putExtra("fair:account", account);

            startActivity(send);
        } catch (Throwable ex) {
            // java.lang.IllegalArgumentException: Failed to resolve canonical path for ...
            Log.unexpectedError(FragmentDialogForwardRaw.this, ex);
        }
    }

    private ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            check();
        }

        @Override
        public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
            check();
        }

        @Override
        public void onLost(Network network) {
            check();
        }

        private void check() {
            ApplicationEx.getMainHandler().post(new Runnable() {
                @Override
                public void run() {
                    if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                        checkInternet.run();
                }
            });
        }
    };

    private Runnable checkInternet = new Runnable() {
        @Override
        public void run() {
            try {
                ConnectionHelper.NetworkState state =
                        ConnectionHelper.getNetworkState(getContext());
                getDialog().findViewById(R.id.tvNoInternet).setVisibility(
                        state.isSuitable() ? View.GONE : View.VISIBLE);
            } catch (Throwable ex) {
                Log.e(ex);
            }
        }
    };
}
