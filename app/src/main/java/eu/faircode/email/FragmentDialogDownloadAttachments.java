package eu.faircode.email;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class FragmentDialogDownloadAttachments extends FragmentDialogBase {
    private List<Long> remaining;

    private static final long AUTO_CONFIRM_DELAY = 2 * 1000L;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        long id = args.getLong("id");
        long size = args.getLong("size");
        long[] download = args.getLongArray("download");
        Intent intent = args.getParcelable("intent");

        if (savedInstanceState == null)
            remaining = Helper.fromLongArray(download);
        else {
            long[] r = savedInstanceState.getLongArray("fair:remaining");
            remaining = (r == null ? new ArrayList<>() : Helper.fromLongArray(r));
        }

        final Context context = getContext();

        NumberFormat NF = NumberFormat.getNumberInstance();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean attachments_auto_confirm = prefs.getBoolean("attachments_auto_confirm", false);

        View dview = LayoutInflater.from(context).inflate(R.layout.dialog_download_attachments, null);
        TextView tvRemark = dview.findViewById(R.id.tvRemark);
        Button btnDownload = dview.findViewById(R.id.btnDownload);
        TextView tvSize = dview.findViewById(R.id.tvSize);
        ProgressBar pbDownloaded = dview.findViewById(R.id.pbDownloaded);
        TextView tvRemaining = dview.findViewById(R.id.tvRemaining);
        CheckBox cbAutoConfirm = dview.findViewById(R.id.cbAutoConfirm);
        CheckBox cbNotAgain = dview.findViewById(R.id.cbNotAgain);
        TextView tvNotAgainRemark = dview.findViewById(R.id.tvNotAgainRemark);

        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SimpleTask<Void>() {
                    @Override
                    protected void onPreExecute(Bundle args) {
                        tvRemark.setVisibility(View.GONE);
                        btnDownload.setVisibility(View.GONE);
                        pbDownloaded.setVisibility(View.VISIBLE);
                        tvRemaining.setVisibility(View.VISIBLE);
                    }

                    @Override
                    protected Void onExecute(Context context, Bundle args) {
                        long id = args.getLong("id");
                        List<Long> download = Helper.fromLongArray(args.getLongArray("download"));

                        DB db = DB.getInstance(context);
                        EntityMessage message = db.message().getMessage(id);
                        if (message == null)
                            return null;

                        List<EntityAttachment> attachments = db.attachment().getAttachments(message.id);
                        if (attachments != null)
                            for (EntityAttachment attachment : attachments)
                                if (download.contains(attachment.id))
                                    EntityOperation.queue(context, message, EntityOperation.ATTACHMENT, attachment.id);

                        return null;
                    }

                    @Override
                    protected void onExecuted(Bundle args, Void data) {
                        long id = args.getLong("id");
                        long[] download = args.getLongArray("download");

                        DB db = DB.getInstance(context);
                        LiveData<List<EntityAttachment>> live = db.attachment().liveAttachments(id);
                        live.observe(getViewLifecycleOwner(), new Observer<List<EntityAttachment>>() {
                            @Override
                            public void onChanged(List<EntityAttachment> attachments) {
                                if (attachments != null)
                                    for (EntityAttachment attachment : attachments)
                                        if (attachment.available && remaining.contains(attachment.id))
                                            remaining.remove(attachment.id);

                                pbDownloaded.setProgress(download.length - remaining.size());

                                String of = getString(R.string.title_of,
                                        NF.format(download.length - remaining.size()),
                                        NF.format(download.length));
                                tvRemaining.setText(getString(R.string.title_attachments_download, of));

                                updateButton();

                                if (remaining.isEmpty()) {
                                    live.removeObserver(this);

                                    boolean attachments_auto_confirm = prefs.getBoolean("attachments_auto_confirm", false);
                                    if (attachments_auto_confirm)
                                        dview.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                                                        return;
                                                    startActivity(intent);
                                                    dismiss();
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
                        Log.unexpectedError(getParentFragment(), ex);
                    }
                }.execute(FragmentDialogDownloadAttachments.this, args, "download");
            }
        });

        tvSize.setText("~ " + Helper.humanReadableByteCount(size));
        tvSize.setVisibility(size > 0 ? View.VISIBLE : View.GONE);

        cbAutoConfirm.setChecked(attachments_auto_confirm);
        cbAutoConfirm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("attachments_auto_confirm", isChecked).apply();
            }
        });

        cbNotAgain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("attachments_asked", isChecked).apply();
                tvNotAgainRemark.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });

        pbDownloaded.setMax(download.length);
        pbDownloaded.setProgress(download.length - remaining.size());

        String of = getString(R.string.title_of,
                NF.format(download.length - remaining.size()),
                NF.format(download.length));
        tvRemaining.setText(getString(R.string.title_attachments_download, of));

        tvRemark.setVisibility(remaining.isEmpty() ? View.GONE : View.VISIBLE);
        btnDownload.setVisibility(remaining.isEmpty() ? View.GONE : View.VISIBLE);
        pbDownloaded.setVisibility(remaining.isEmpty() ? View.VISIBLE : View.GONE);
        tvRemaining.setVisibility(remaining.isEmpty() ? View.VISIBLE : View.GONE);
        tvNotAgainRemark.setVisibility(View.GONE);

        final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(intent);
            }
        };

        return new AlertDialog.Builder(context)
                .setView(dview)
                .setPositiveButton(android.R.string.ok, listener)
                .setNegativeButton(R.string.title_dismiss, listener)
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
        updateButton();
    }

    private void updateButton() {
        Button btn = ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE);
        if (btn != null)
            btn.setEnabled(remaining.isEmpty());
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
        outState.putLongArray("fair:remaining", Helper.toLongArray(remaining));
        super.onSaveInstanceState(outState);
    }

    private final ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
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

    private final Runnable checkInternet = new Runnable() {
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
