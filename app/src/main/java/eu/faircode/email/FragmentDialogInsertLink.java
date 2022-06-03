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

    Copyright 2018-2022 by Marcel Bokhorst (M66B)
*/

import static android.app.Activity.RESULT_OK;

import android.app.Dialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class FragmentDialogInsertLink extends FragmentDialogBase {
    private EditText etLink;
    private EditText etTitle;

    private static final int METADATA_CONNECT_TIMEOUT = 10 * 1000; // milliseconds
    private static final int METADATA_READ_TIMEOUT = 15 * 1000; // milliseconds

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
        final TextView tvInsecure = view.findViewById(R.id.tvInsecure);
        etTitle = view.findViewById(R.id.etTitle);
        final Button btnMetadata = view.findViewById(R.id.btnMetadata);
        final ProgressBar pbWait = view.findViewById(R.id.pbWait);

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
                btnMetadata.setEnabled(UriHelper.isHyperLink(uri));
            }
        });

        btnMetadata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putString("url", etLink.getText().toString());

                new SimpleTask<String>() {
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
                    protected String onExecute(Context context, Bundle args) throws Throwable {
                        String url = args.getString("url");
                        URL base = new URL(url);

                        HttpURLConnection connection = (HttpURLConnection) base.openConnection();
                        connection.setRequestMethod("GET");
                        connection.setReadTimeout(METADATA_READ_TIMEOUT);
                        connection.setConnectTimeout(METADATA_CONNECT_TIMEOUT);
                        connection.setInstanceFollowRedirects(true);
                        connection.setRequestProperty("User-Agent", WebViewEx.getUserAgent(context));
                        connection.connect();

                        try {
                            int status = connection.getResponseCode();
                            if (status != HttpURLConnection.HTTP_OK) {
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
                            // <meta property="og:title" content="...
                            // <meta property="twitter:title" content="...
                            Document doc = JsoupEx.parse(connection.getInputStream(), StandardCharsets.UTF_8.name(), url);

                            Element title = doc.select("title").first();
                            if (title != null && !TextUtils.isEmpty(title.text()))
                                return title.text();

                            Element ogTitle = doc.select("meta[property=og:title]").first();
                            if (ogTitle != null && !TextUtils.isEmpty(ogTitle.attr("content")))
                                return ogTitle.attr("content");

                            Element twitterTitle = doc.select("meta[property=twitter:title]").first();
                            if (twitterTitle != null && !TextUtils.isEmpty(twitterTitle.attr("content")))
                                return twitterTitle.attr("content");

                            Element ogSiteName = doc.select("meta[property=og:site_name]").first();
                            if (ogSiteName != null && !TextUtils.isEmpty(ogSiteName.attr("content")))
                                return ogSiteName.attr("content");

                            Element description = doc.select("meta[name=description]").first();
                            if (description != null && !TextUtils.isEmpty(description.attr("content")))
                                return description.attr("content");

                            return null;
                        } finally {
                            connection.disconnect();
                        }
                    }

                    @Override
                    protected void onExecuted(Bundle args, String text) {
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

        if (savedInstanceState == null) {
            String link = (uri == null ? "https://" : uri.toString());
            etLink.setText(link);
            etTitle.setText(link.equals(title) ? null : title);
        } else {
            etLink.setText(savedInstanceState.getString("fair:link"));
            etTitle.setText(savedInstanceState.getString("fair:text"));
        }

        pbWait.setVisibility(View.GONE);

        return new AlertDialog.Builder(context)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        args.putString("link", etLink.getText().toString());
                        args.putString("title", etTitle.getText().toString());
                        sendResult(RESULT_OK);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .setNeutralButton(R.string.title_reset, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendResult(RESULT_OK);
                    }
                })
                .create();
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
