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
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class FragmentDialogForwardRaw extends FragmentDialogBase {
    private boolean enabled;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null)
            enabled = savedInstanceState.getBoolean("fair:enabled");

        View dview = LayoutInflater.from(getContext()).inflate(R.layout.dialog_forward_raw, null);
        TextView tvRemaining = dview.findViewById(R.id.tvRemaining);
        TextView tvOption = dview.findViewById(R.id.tvOption);

        tvRemaining.setText(getString(R.string.title_eml_downloaded, "-"));

        tvOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.getContext().startActivity(new Intent(v.getContext(), ActivitySetup.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putExtra("tab", "connection"));
            }
        });

        NumberFormat NF = NumberFormat.getNumberInstance();

        new SimpleTask<long[]>() {
            @Override
            protected long[] onExecute(Context context, Bundle args) {
                long[] ids = args.getLongArray("ids");
                boolean threads = args.getBoolean("threads");

                List<Long> result = new ArrayList<>();

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    List<String> msgids = new ArrayList<>();

                    for (long id : ids) {
                        EntityMessage message = db.message().getMessage(id);
                        if (message == null)
                            continue;

                        List<EntityMessage> messages = db.message().getMessagesByThread(
                                message.account, message.thread, threads ? null : id, null);
                        if (messages == null)
                            continue;

                        for (EntityMessage thread : messages) {
                            if (msgids.contains(thread.msgid))
                                continue;
                            msgids.add(thread.msgid);

                            result.add(thread.id);

                            if (thread.raw == null || !thread.raw)
                                EntityOperation.queue(context, thread, EntityOperation.RAW);
                        }
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                return Helper.toLongArray(result);
            }

            @Override
            protected void onExecuted(Bundle args, long[] ids) {
                DB db = DB.getInstance(getContext());
                LiveData<Integer> ld = db.message().liveRaw(ids);
                ld.observe(getViewLifecycleOwner(), new Observer<Integer>() {
                    @Override
                    public void onChanged(Integer remaining) {
                        if (remaining == null)
                            return;

                        String of = getString(R.string.title_of,
                                NF.format(ids.length - remaining),
                                NF.format(ids.length));
                        tvRemaining.setText(getString(R.string.title_eml_downloaded, of));

                        if (remaining == 0) {
                            ld.removeObserver(this);
                            getArguments().putLongArray("ids", ids);
                            enabled = true;
                            setButtonEnabled(enabled);
                        }
                    }
                });
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, getArguments(), "messages:forward");

        return new AlertDialog.Builder(getContext())
                .setView(dview)
                .setPositiveButton(R.string.title_send, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        send(getArguments().getLongArray("ids"));
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    @Override
    public void onStart() {
        super.onStart();
        setButtonEnabled(enabled);
    }

    void setButtonEnabled(boolean enabled) {
        ((AlertDialog) getDialog())
                .getButton(AlertDialog.BUTTON_POSITIVE)
                .setEnabled(enabled);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean("fair:enabled", enabled);
        super.onSaveInstanceState(outState);
    }

    private void send(long[] ids) {
        try {
            ArrayList<Uri> uris = new ArrayList<>();
            for (long id : ids) {
                File file = EntityMessage.getRawFile(getContext(), id);
                Uri uri = FileProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID, file);
                uris.add(uri);
            }

            Intent send = new Intent(Intent.ACTION_SEND_MULTIPLE);
            send.setPackage(BuildConfig.APPLICATION_ID);
            send.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            send.setType("message/rfc822");
            send.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(send);
        } catch (Throwable ex) {
            // java.lang.IllegalArgumentException: Failed to resolve canonical path for ...
            Log.unexpectedError(getParentFragmentManager(), ex);
        }
    }
}
