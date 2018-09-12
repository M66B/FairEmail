package eu.faircode.email;

/*
    This file is part of FairEmail.

    FairEmail is free software: you can redistribute it and/or modify
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

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.mail.Address;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;

public class FragmentAbout extends FragmentEx {
    private TextView tvVersion;
    private Button btnLog;
    private Button btnDebugInfo;

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.menu_about);

        View view = inflater.inflate(R.layout.fragment_about, container, false);

        tvVersion = view.findViewById(R.id.tvVersion);
        btnLog = view.findViewById(R.id.btnLog);
        btnDebugInfo = view.findViewById(R.id.btnDebugInfo);

        tvVersion.setText(getString(R.string.title_version, BuildConfig.VERSION_NAME));

        btnLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.content_frame, new FragmentLogs()).addToBackStack("logs");
                fragmentTransaction.commit();
            }
        });

        btnDebugInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnDebugInfo.setEnabled(false);
                new SimpleTask<Long>() {
                    @Override
                    protected Long onLoad(Context context, Bundle args) throws UnsupportedEncodingException {
                        StringBuilder sb = new StringBuilder();

                        sb.append(context.getString(R.string.title_debug_info_remark) + "\n\n\n\n");

                        // Get version info
                        sb.append(String.format("%s: %s\r\n", context.getString(R.string.app_name), BuildConfig.VERSION_NAME));
                        sb.append(String.format("Android: %s (SDK %d)\r\n", Build.VERSION.RELEASE, Build.VERSION.SDK_INT));
                        sb.append("\r\n");

                        // Get device info
                        sb.append(String.format("Brand: %s\r\n", Build.BRAND));
                        sb.append(String.format("Manufacturer: %s\r\n", Build.MANUFACTURER));
                        sb.append(String.format("Model: %s\r\n", Build.MODEL));
                        sb.append(String.format("Product: %s\r\n", Build.PRODUCT));
                        sb.append(String.format("Device: %s\r\n", Build.DEVICE));
                        sb.append(String.format("Host: %s\r\n", Build.HOST));
                        sb.append(String.format("Display: %s\r\n", Build.DISPLAY));
                        sb.append(String.format("Id: %s\r\n", Build.ID));
                        sb.append("\r\n");

                        String body = "<pre>" + sb.toString().replaceAll("\\r?\\n", "<br />") + "</pre>";

                        EntityMessage draft;
                        DB db = DB.getInstance(context);
                        try {
                            db.beginTransaction();

                            EntityFolder drafts = db.folder().getPrimaryDrafts();
                            if (drafts == null)
                                throw new IllegalArgumentException(context.getString(R.string.title_no_drafts));

                            draft = new EntityMessage();
                            draft.account = drafts.account;
                            draft.folder = drafts.id;
                            draft.msgid = EntityMessage.generateMessageId();
                            draft.to = new Address[]{Helper.myAddress()};
                            draft.subject = context.getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME + " debug info";
                            draft.received = new Date().getTime();
                            draft.seen = false;
                            draft.ui_seen = false;
                            draft.flagged = false;
                            draft.ui_flagged = false;
                            draft.ui_hide = false;
                            draft.ui_found = false;
                            draft.id = db.message().insertMessage(draft);
                            draft.write(context, body);

                            // Attach recent log
                            {
                                EntityAttachment log = new EntityAttachment();
                                log.message = draft.id;
                                log.sequence = 1;
                                log.name = "log.txt";
                                log.type = "text/plain";
                                log.size = null;
                                log.progress = 0;
                                log.id = db.attachment().insertAttachment(log);

                                OutputStream os = null;
                                File file = EntityAttachment.getFile(context, log.id);
                                try {
                                    os = new BufferedOutputStream(new FileOutputStream(file));

                                    int size = 0;
                                    long from = new Date().getTime() - 24 * 3600 * 1000L;
                                    DateFormat DF = SimpleDateFormat.getTimeInstance();
                                    for (EntityLog entry : db.log().getLogs(from)) {
                                        String line = String.format("%s %s\r\n", DF.format(entry.time), entry.data);
                                        byte[] bytes = line.getBytes();
                                        os.write(bytes);
                                        size += bytes.length;
                                    }

                                    log.size = size;
                                    log.progress = null;
                                    log.available = true;
                                    db.attachment().updateAttachment(log);
                                } finally {
                                    if (os != null)
                                        os.close();
                                }
                            }

                            // Attach logcat
                            {
                                EntityAttachment logcat = new EntityAttachment();
                                logcat.message = draft.id;
                                logcat.sequence = 2;
                                logcat.name = "logcat.txt";
                                logcat.type = "text/plain";
                                logcat.size = null;
                                logcat.progress = 0;
                                logcat.id = db.attachment().insertAttachment(logcat);

                                Process proc = null;
                                BufferedReader br = null;
                                OutputStream os = null;
                                File file = EntityAttachment.getFile(context, logcat.id);
                                try {
                                    os = new BufferedOutputStream(new FileOutputStream(file));

                                    String[] cmd = new String[]{"logcat",
                                            "-d",
                                            "-v", "threadtime",
                                            //"-t", "1000",
                                            Helper.TAG + ":I"};
                                    proc = Runtime.getRuntime().exec(cmd);
                                    br = new BufferedReader(new InputStreamReader(proc.getInputStream()));

                                    int size = 0;
                                    String line;
                                    while ((line = br.readLine()) != null) {
                                        line += "\r\n";
                                        byte[] bytes = line.getBytes();
                                        os.write(bytes);
                                        size += bytes.length;
                                    }

                                    logcat.size = size;
                                    logcat.progress = null;
                                    logcat.available = true;
                                    db.attachment().updateAttachment(logcat);
                                } finally {
                                    if (os != null)
                                        os.close();
                                    if (br != null)
                                        br.close();
                                    if (proc != null)
                                        proc.destroy();
                                }
                            }

                            EntityOperation.queue(db, draft, EntityOperation.ADD);

                            db.setTransactionSuccessful();
                        } catch (IOException ex) {
                            Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                            return null;
                        } finally {
                            db.endTransaction();
                        }

                        EntityOperation.process(context);

                        return draft.id;
                    }

                    @Override
                    protected void onLoaded(Bundle args, Long id) {
                        btnDebugInfo.setEnabled(true);
                        if (id != null)
                            startActivity(new Intent(getContext(), ActivityCompose.class)
                                    .putExtra("action", "edit")
                                    .putExtra("id", id));
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        btnDebugInfo.setEnabled(true);
                        Toast.makeText(getContext(), ex.toString(), Toast.LENGTH_LONG).show();
                    }
                }.load(FragmentAbout.this, new Bundle());
            }
        });

        return view;
    }
}
