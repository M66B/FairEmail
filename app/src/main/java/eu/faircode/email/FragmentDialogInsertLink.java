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

import static android.app.Activity.RESULT_OK;

import android.app.Dialog;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.Group;
import androidx.documentfile.provider.DocumentFile;
import androidx.preference.PreferenceManager;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;

public class FragmentDialogInsertLink extends FragmentDialogBase {
    private EditText etLink;
    private CheckBox cbImage;
    private EditText etTitle;
    private Button btnUpload;
    private ProgressBar pbUpload;
    private TextView tvDLimit;
    private SeekBar sbDLimit;
    private TextView tvTLimit;
    private SeekBar sbTLimit;

    private static final int METADATA_CONNECT_TIMEOUT = 10 * 1000; // milliseconds
    private static final int METADATA_READ_TIMEOUT = 15 * 1000; // milliseconds
    private static final int REQUEST_SEND = 1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lockOrientation();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("fair:link", etLink == null ? null : etLink.getText().toString());
        outState.putString("fair:text", etTitle == null ? null : etTitle.getText().toString());
        super.onSaveInstanceState(outState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        Uri uri = args.getParcelable("uri");
        String title = args.getString("title");

        final Context context = getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_insert_link, null);
        etLink = view.findViewById(R.id.etLink);
        cbImage = view.findViewById(R.id.cbImage);
        final TextView tvInsecure = view.findViewById(R.id.tvInsecure);
        etTitle = view.findViewById(R.id.etTitle);
        final Button btnMetadata = view.findViewById(R.id.btnMetadata);
        final ProgressBar pbWait = view.findViewById(R.id.pbWait);
        btnUpload = view.findViewById(R.id.btnUpload);
        pbUpload = view.findViewById(R.id.pbUpload);
        tvDLimit = view.findViewById(R.id.tvDLimit);
        sbDLimit = view.findViewById(R.id.sbDLimit);
        tvTLimit = view.findViewById(R.id.tvTLimit);
        sbTLimit = view.findViewById(R.id.sbTLimit);
        Group grpUpload = view.findViewById(R.id.grpUpload);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean send_enabled = prefs.getBoolean("send_enabled", false);

        etLink.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (tvInsecure == null || btnMetadata == null)
                    return;

                Uri uri = Uri.parse(editable.toString());
                tvInsecure.setVisibility(
                        !UriHelper.isHyperLink(uri) || UriHelper.isSecure(uri)
                                ? View.GONE : View.VISIBLE);
                btnMetadata.setEnabled(UriHelper.isSecure(uri));
            }
        });

        btnMetadata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putString("url", etLink.getText().toString());

                new SimpleTask<OpenGraph>() {
                    @Override
                    protected void onPreExecute(Bundle args) {
                        btnMetadata.setEnabled(false);
                        pbWait.setVisibility(View.VISIBLE);
                    }

                    @Override
                    protected void onPostExecute(Bundle args) {
                        btnMetadata.setEnabled(true);
                        pbWait.setVisibility(View.GONE);
                    }

                    @Override
                    protected OpenGraph onExecute(Context context, Bundle args) throws Throwable {
                        String url = args.getString("url");
                        URL base = new URL(url);

                        OpenGraph og = new OpenGraph();

                        HttpsURLConnection connection = (HttpsURLConnection) base.openConnection();
                        connection.setRequestMethod("GET");
                        connection.setReadTimeout(METADATA_READ_TIMEOUT);
                        connection.setConnectTimeout(METADATA_CONNECT_TIMEOUT);
                        connection.setInstanceFollowRedirects(true);
                        ConnectionHelper.setUserAgent(context, connection);
                        connection.connect();

                        try {
                            int status = connection.getResponseCode();
                            if (status != HttpsURLConnection.HTTP_OK) {
                                String error = "Error " + status + ": " + connection.getResponseMessage();
                                try {
                                    InputStream is = connection.getErrorStream();
                                    if (is != null)
                                        error += "\n" + Helper.readStream(is);
                                } catch (Throwable ex) {
                                    Log.w(ex);
                                }
                                throw new IOException(error);
                            }

                            // <title>...
                            // <meta name="description" content="...
                            // https://ogp.me/
                            Document doc = JsoupEx.parse(connection.getInputStream(), StandardCharsets.UTF_8.name(), url);

                            Element ogTitle = doc.select("meta[property=og:title]").first();
                            if (ogTitle != null)
                                og.title = ogTitle.attr("content");

                            // Fallback
                            if (TextUtils.isEmpty(og.title)) {
                                Element title = doc.select("title").first();
                                if (title != null)
                                    og.title = title.text();
                            }

                            Element ogDescription = doc.select("meta[property=og:description]").first();
                            if (ogDescription != null)
                                og.description = ogDescription.attr("content");

                            // Fallback
                            if (TextUtils.isEmpty(og.description)) {
                                Element description = doc.select("meta[name=description]").first();
                                if (description != null)
                                    og.description = description.attr("content");
                            }

                            Element ogSiteName = doc.select("meta[property=og:site_name]").first();
                            if (ogSiteName != null)
                                og.site_name = ogSiteName.attr("content");

                            Element ogImage = doc.select("meta[property=og:image]").first();
                            if (ogImage != null)
                                og.image = ogImage.attr("content");

                            Element ogUrl = doc.select("meta[property=og:url]").first();
                            if (ogUrl != null)
                                og.url = ogUrl.attr("content");

                            return og;
                        } finally {
                            connection.disconnect();
                        }
                    }

                    @Override
                    protected void onExecuted(Bundle args, OpenGraph og) {
                        if (og == null)
                            return;

                        // Canonical URL
                        if (!TextUtils.isEmpty(og.url))
                            etLink.setText(og.url);

                        // Link title
                        String text = og.title;
                        if (TextUtils.isEmpty(text))
                            text = og.description;
                        if (TextUtils.isEmpty(text))
                            text = og.site_name;

                        if (TextUtils.isEmpty(text))
                            etTitle.setText(null);
                        else
                            etTitle.setText(text.replaceAll("\\s+", " "));
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Log.unexpectedError(getParentFragmentManager(), ex, !(ex instanceof IOException));
                    }
                }.execute(FragmentDialogInsertLink.this, args, "link:meta");
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setType("*/*");
                startActivityForResult(Helper.getChooser(getContext(), intent), REQUEST_SEND);
            }
        });

        sbDLimit.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                prefs.edit().putInt("send_dlimit", progress).apply();

                progress++;

                tvDLimit.setText(getString(R.string.title_style_link_send_dlimit, Integer.toString(progress)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }
        });

        sbTLimit.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                prefs.edit().putInt("send_tlimit", progress).apply();

                progress++;

                if (progress < 24)
                    tvTLimit.setText(getString(R.string.title_style_link_send_tlimit,
                            getResources().getQuantityString(R.plurals.title_hours, progress, progress)));
                else {
                    progress = (progress - 24 + 1);
                    tvTLimit.setText(getString(R.string.title_style_link_send_tlimit,
                            getResources().getQuantityString(R.plurals.title_days, progress, progress)));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }
        });

        if (savedInstanceState == null) {
            String link = (uri == null ? "https://" : uri.toString());
            etLink.setText(link);
            etTitle.setText(link.equals(title) ? null : title);
        } else {
            etLink.setText(savedInstanceState.getString("fair:link"));
            etTitle.setText(savedInstanceState.getString("fair:text"));
        }

        int dlimit = prefs.getInt("send_dlimit", Send.DEFAULT_DLIMIT - 1);
        int tlimit = prefs.getInt("send_tlimit", Send.DEFAULT_TLIMIT - 1);
        sbDLimit.setProgress(dlimit == 0 ? 1 : 0);
        sbTLimit.setProgress(tlimit == 0 ? 1 : 0);
        sbDLimit.setProgress(dlimit);
        sbTLimit.setProgress(tlimit);

        pbWait.setVisibility(View.GONE);
        pbUpload.setVisibility(View.GONE);
        grpUpload.setVisibility(send_enabled && !BuildConfig.PLAY_STORE_RELEASE
                ? View.VISIBLE : View.GONE);

        return new AlertDialog.Builder(context)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        args.putString("link", etLink.getText().toString());
                        args.putBoolean("image", cbImage.isChecked());
                        args.putString("title", etTitle.getText().toString());
                        sendResult(RESULT_OK);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                //.setNeutralButton(R.string.title_reset, new DialogInterface.OnClickListener() {
                //    @Override
                //    public void onClick(DialogInterface dialog, int which) {
                //        sendResult(RESULT_OK);
                //    }
                //})
                .create();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            switch (requestCode) {
                case REQUEST_SEND:
                    if (resultCode == RESULT_OK && data != null)
                        onSend(data.getData());
                    break;
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    private void onSend(Uri uri) {
        int dlimit = sbDLimit.getProgress() + 1;
        int tlimit = sbTLimit.getProgress() + 1;

        if (tlimit >= 24)
            tlimit = (tlimit - 24 + 1) * 24;

        Bundle args = new Bundle();
        args.putParcelable("uri", uri);
        args.putInt("dlimit", dlimit);
        args.putInt("tlimit", tlimit);

        new SimpleTask<String>() {
            @Override
            protected void onPreExecute(Bundle args) {
                btnUpload.setEnabled(false);
                sbDLimit.setEnabled(false);
                sbTLimit.setEnabled(false);
                pbUpload.setProgress(0);
                pbUpload.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onPostExecute(Bundle args) {
                btnUpload.setEnabled(true);
                sbDLimit.setEnabled(true);
                sbTLimit.setEnabled(true);
                pbUpload.setVisibility(View.GONE);
            }

            @Override
            protected String onExecute(Context context, Bundle args) throws Throwable {
                Uri uri = args.getParcelable("uri");
                int dlimit = args.getInt("dlimit");
                int tlimit = args.getInt("tlimit");

                if (uri == null)
                    throw new FileNotFoundException("uri");

                if (!"content".equals(uri.getScheme()))
                    throw new FileNotFoundException("content");

                DocumentFile dfile = DocumentFile.fromSingleUri(context, uri);
                if (dfile == null)
                    throw new FileNotFoundException("dfile");

                if (dlimit == 0)
                    dlimit = Send.DEFAULT_DLIMIT;
                if (tlimit == 0)
                    tlimit = Send.DEFAULT_TLIMIT;

                Log.i("Send uri=" + uri + " dlimit=" + dlimit + " tlimit=" + tlimit);

                args.putString("title", dfile.getName());

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                String send_host = prefs.getString("send_host", Send.DEFAULT_SERVER);

                ContentResolver resolver = context.getContentResolver();
                try (InputStream is = resolver.openInputStream(uri)) {
                    return Send.upload(is, dfile, dlimit, tlimit * 60 * 60, send_host, new Send.IProgress() {
                        @Override
                        public void onProgress(int percentage) {
                            Bundle args = new Bundle();
                            args.putInt("progress", percentage);
                            postProgress(null, args);
                        }

                        @Override
                        public boolean isRunning() {
                            return (pbUpload != null);
                        }
                    });
                }
            }

            @Override
            protected void onProgress(CharSequence status, Bundle data) {
                int progress = data.getInt("progress");
                Log.i("Send progress=" + progress);
                if (pbUpload != null)
                    pbUpload.setProgress(progress);
            }

            @Override
            protected void onExecuted(Bundle args, String link) {
                etLink.setText(link);
                etTitle.setText(args.getString("title"));
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "send");
    }

    private static class OpenGraph {
        private String title;
        private String description;
        private String site_name;
        private String image;
        private String url;
    }

    static Bundle getArguments(EditText etBody) {
        Uri uri = null;

        int start = etBody.getSelectionStart();
        int end = etBody.getSelectionEnd();
        Editable edit = etBody.getText();

        if (start >= 0 && start == end && start < edit.length()) {
            int s = start;
            int e = end;
            while (s - 1 >= 0 && !Character.isWhitespace(edit.charAt(s - 1)))
                s--;
            while (e < edit.length() && !Character.isWhitespace(edit.charAt(e)))
                e++;
            if (s < e) {
                start = s;
                end = e;
                String link = edit.subSequence(start, end).toString();
                if (Helper.EMAIL_ADDRESS.matcher(link).matches())
                    uri = Uri.parse("mailto:" + link);
                else
                    uri = Uri.parse(link);
                if (uri.getScheme() == null)
                    uri = null;
            }
        }

        if (uri == null) {
            URLSpan[] spans = edit.getSpans(start, start, URLSpan.class);
            if (spans != null && spans.length > 0) {
                start = edit.getSpanStart(spans[0]);
                end = edit.getSpanEnd(spans[0]);

                String link = spans[0].getURL();
                if (link != null) {
                    if (Helper.EMAIL_ADDRESS.matcher(link).matches())
                        uri = Uri.parse("mailto:" + link);
                    else
                        uri = Uri.parse(link);
                    if (uri.getScheme() == null)
                        uri = null;
                }
            }
        }

        if (uri == null)
            try {
                ClipboardManager cbm = Helper.getSystemService(etBody.getContext(), ClipboardManager.class);
                if (cbm != null && cbm.hasPrimaryClip()) {
                    String link = cbm.getPrimaryClip().getItemAt(0).coerceToText(etBody.getContext()).toString();
                    if (Helper.EMAIL_ADDRESS.matcher(link).matches())
                        uri = Uri.parse("mailto:" + link);
                    else
                        uri = Uri.parse(link);
                    if (uri.getScheme() == null)
                        uri = null;
                }
            } catch (Throwable ex) {
                Log.w(ex);
                /*
                    java.lang.SecurityException: Permission Denial: opening provider org.chromium.chrome.browser.util.ChromeFileProvider from ProcessRecord{43c6094 11175:eu.faircode.email/u0a73} (pid=11175, uid=10073) that is not exported from uid 10080
                      at android.os.Parcel.readException(Parcel.java:1692)
                      at android.os.Parcel.readException(Parcel.java:1645)
                      at android.app.ActivityManagerProxy.getContentProvider(ActivityManagerNative.java:4214)
                      at android.app.ActivityThread.acquireProvider(ActivityThread.java:5584)
                      at android.app.ContextImpl$ApplicationContentResolver.acquireUnstableProvider(ContextImpl.java:2239)
                      at android.content.ContentResolver.acquireUnstableProvider(ContentResolver.java:1520)
                      at android.content.ContentResolver.openTypedAssetFileDescriptor(ContentResolver.java:1133)
                      at android.content.ContentResolver.openTypedAssetFileDescriptor(ContentResolver.java:1093)
                      at android.content.ClipData$Item.coerceToText(ClipData.java:340)
                 */
            }

        String title = (start >= 0 && end > start ? edit.subSequence(start, end).toString() : "");

        Bundle args = new Bundle();
        args.putParcelable("uri", uri);
        args.putInt("start", start);
        args.putInt("end", end);
        args.putString("title", title);

        return args;
    }
}
