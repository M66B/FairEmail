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

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class FragmentDialogPrint extends FragmentDialogBase {
    private static WebView printWebView = null;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Context context = getContext();
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        View dview = LayoutInflater.from(context).inflate(R.layout.dialog_print, null);
        CheckBox cbHeader = dview.findViewById(R.id.cbHeader);
        CheckBox cbImages = dview.findViewById(R.id.cbImages);
        CheckBox cbBlockQuotes = dview.findViewById(R.id.cbBlockQuotes);
        CheckBox cbMargin = dview.findViewById(R.id.cbMargin);
        CheckBox cbNotAgain = dview.findViewById(R.id.cbNotAgain);

        cbHeader.setChecked(prefs.getBoolean("print_html_header", true));
        cbHeader.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("print_html_header", isChecked).apply();
            }
        });

        cbImages.setChecked(prefs.getBoolean("print_html_images", true));
        cbImages.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("print_html_images", isChecked).apply();
            }
        });

        cbBlockQuotes.setChecked(prefs.getBoolean("print_html_block_quotes", true));
        cbBlockQuotes.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("print_html_block_quotes", isChecked).apply();
            }
        });

        cbMargin.setChecked(prefs.getBoolean("print_html_margins", true));
        cbMargin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("print_html_margins", isChecked).apply();
            }
        });

        cbNotAgain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("print_html_confirmed", isChecked).apply();
            }
        });

        return new AlertDialog.Builder(getContext())
                .setView(dview)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendResult(Activity.RESULT_OK);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sendResult(Activity.RESULT_CANCELED);
                    }
                })
                .create();
    }

    static void print(ActivityBase activity, FragmentManager fm, Bundle args) {
        if (activity == null) {
            Log.w("Print no activity");
            return;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        boolean print_html_header = prefs.getBoolean("print_html_header", true);
        boolean print_html_images = prefs.getBoolean("print_html_images", true);
        boolean print_html_block_quotes = prefs.getBoolean("print_html_block_quotes", true);
        boolean print_html_margins = prefs.getBoolean("print_html_margins", true);

        args.putBoolean("print_html_header", print_html_header);
        args.putBoolean("print_html_images", print_html_images);
        args.putBoolean("print_html_block_quotes", print_html_block_quotes);
        args.putBoolean("print_html_margins", print_html_margins);

        new SimpleTask<String[]>() {
            @Override
            protected String[] onExecute(Context context, Bundle args) throws IOException {
                long id = args.getLong("id");
                boolean headers = args.getBoolean("headers");
                boolean print_html_header = args.getBoolean("print_html_header");
                boolean print_html_images = args.getBoolean("print_html_images");
                boolean print_html_block_quotes = args.getBoolean("print_html_block_quotes");
                boolean print_html_margins = args.getBoolean("print_html_margins");
                CharSequence selected = args.getCharSequence("selected");
                boolean draft = args.getBoolean("draft");

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                int timeout = prefs.getInt("timeout", ImageHelper.DOWNLOAD_TIMEOUT) * 1000;

                DB db = DB.getInstance(context);
                EntityMessage message = db.message().getMessage(id);
                if (message == null || !message.content)
                    return null;

                EntityFolder folder = db.folder().getFolder(message.folder);
                if (folder == null)
                    return null;

                File file = message.getFile(context);
                if (!file.exists())
                    return null;

                List<EntityAttachment> attachments = db.attachment().getAttachments(message.id);
                if (attachments == null)
                    return null;

                Document document;
                if (!TextUtils.isEmpty(selected) && selected instanceof Spanned)
                    document = JsoupEx.parse(HtmlHelper.toHtml((Spanned) selected, context));
                else
                    document = JsoupEx.parse(file);

                boolean monospaced_pre = prefs.getBoolean("monospaced_pre", false);
                if (message.isPlainOnly() && monospaced_pre)
                    HtmlHelper.restorePre(document);

                if (!print_html_block_quotes)
                    for (Element bq : document.select("blockquote")) {
                        String style = bq.attr("style");
                        bq.attr("style", HtmlHelper.mergeStyles(style,
                                "border: none !important;" +
                                        "margin-left: 0; margin-right: 0;" +
                                        "padding-left: 0; padding-right: 0;"));
                    }

                if (print_html_margins)
                    document.head().appendElement("style")
                            .text("@page { margin: 1cm; }");

                HtmlHelper.markText(document);

                HtmlHelper.embedInlineImages(context, id, document, true);

                // onPageFinished will not be called if not all images can be loaded
                File dir = Helper.ensureExists(context, "images");
                List<Future<Void>> futures = new ArrayList<>();
                Elements imgs = document.select("img");
                for (int i = 0; i < imgs.size(); i++) {
                    Element img = imgs.get(i);
                    String src = img.attr("src");
                    if (src.startsWith("http:") || src.startsWith("https:")) {
                        final File out = new File(dir, id + "." + i + ".print");
                        img.attr("src", "file:" + out.getAbsolutePath());

                        if (print_html_images) {
                            if (out.exists() && out.length() > 0)
                                continue;
                        } else {
                            Helper.secureDelete(out);
                            continue;
                        }

                        futures.add(Helper.getDownloadTaskExecutor().submit(new Callable<Void>() {
                            @Override
                            public Void call() throws Exception {
                                try (OutputStream os = new FileOutputStream(out)) {
                                    URL url = new URL(src);
                                    Log.i("Caching url=" + url);

                                    HttpURLConnection connection = null;
                                    try {
                                        connection = ConnectionHelper.openConnectionUnsafe(context, src, timeout, timeout);
                                        Helper.copy(connection.getInputStream(), os);
                                    } finally {
                                        if (connection != null)
                                            connection.disconnect();
                                    }
                                } catch (Throwable ex) {
                                    Log.w(ex);
                                }

                                return null;
                            }
                        }));
                    }
                }

                for (Future<Void> future : futures)
                    try {
                        future.get();
                    } catch (Throwable ex) {
                        Log.w(ex);
                    }

                // @page WordSection1 {size:612.0pt 792.0pt; margin:70.85pt 70.85pt 70.85pt 70.85pt;}
                // div.WordSection1 {page:WordSection1;}
                // <body><div class=WordSection1>

                for (Element element : document.body().select("div[class]")) {
                    String clazz = element.attr("class");
                    if (clazz.startsWith("WordSection"))
                        element.removeClass(clazz);
                }

                if (print_html_header) {
                    Element header = document.createElement("p");

                    if (draft) {
                        Element div = document.createElement("div");
                        div.attr("style", "text-align: center;");
                        Element strong = document.createElement("strong");
                        strong.text(context.getString(R.string.title_compose));
                        strong.attr("style", "text-transform: uppercase;");
                        div.appendChild(strong);
                        header.appendChild(div);
                        header.appendElement("hr");
                    }

                    if (message.from != null && message.from.length > 0) {
                        Element span = document.createElement("span");
                        Element strong = document.createElement("strong");
                        strong.text(context.getString(R.string.title_from));
                        span.appendChild(strong);
                        span.appendText(" " + MessageHelper.formatAddresses(message.from));
                        span.appendElement("br");
                        header.appendChild(span);
                    }

                    if (message.to != null && message.to.length > 0) {
                        Element span = document.createElement("span");
                        Element strong = document.createElement("strong");
                        strong.text(context.getString(R.string.title_to));
                        span.appendChild(strong);
                        span.appendText(" " + MessageHelper.formatAddresses(message.to));
                        span.appendElement("br");
                        header.appendChild(span);
                    }

                    if (message.cc != null && message.cc.length > 0) {
                        Element span = document.createElement("span");
                        Element strong = document.createElement("strong");
                        strong.text(context.getString(R.string.title_cc));
                        span.appendChild(strong);
                        span.appendText(" " + MessageHelper.formatAddresses(message.cc));
                        span.appendElement("br");
                        header.appendChild(span);
                    }

                    if (message.received != null && !draft) {
                        DateFormat DTF = Helper.getDateTimeInstance(context, SimpleDateFormat.LONG, SimpleDateFormat.LONG);

                        boolean sent = (EntityFolder.SENT.equals(folder.type) && message.sent != null);

                        Element span = document.createElement("span");
                        Element strong = document.createElement("strong");
                        strong.text(context.getString(sent ? R.string.title_sent : R.string.title_received));
                        span.appendChild(strong);
                        span.appendText(" " + DTF.format(sent ? message.sent : message.received));
                        span.appendElement("br");
                        header.appendChild(span);
                    }

                    if (!TextUtils.isEmpty(message.subject)) {
                        Element span = document.createElement("span");
                        span.appendText(message.subject);
                        span.appendElement("br");
                        header.appendChild(span);
                    }

                    if (headers && message.headers != null) {
                        header.appendElement("hr");
                        Element pre = document.createElement("pre");
                        pre.text(message.headers);
                        header.appendChild(pre);
                    }

                    for (EntityAttachment attachment : attachments)
                        if (attachment.isAttachment()) {
                            String uri = null;
                            if (print_html_images)
                                try {
                                    int resid = 0;
                                    String extension = Helper.guessExtension(attachment.getMimeType());
                                    if (extension != null)
                                        resid = context.getResources().getIdentifier("file_" + extension, "drawable", context.getPackageName());
                                    Drawable d = ContextCompat.getDrawable(context, resid == 0 ? R.drawable.file_bin : resid);
                                    if (d != null) {
                                        int h = Helper.dp2pixels(context, 12);
                                        int w = h * d.getIntrinsicWidth() / d.getIntrinsicHeight();
                                        Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                                        Canvas canvas = new Canvas(bm);
                                        d.setBounds(0, 0, w, h);
                                        d.draw(canvas);

                                        Helper.ByteArrayInOutStream bos = new Helper.ByteArrayInOutStream();
                                        bm.compress(Bitmap.CompressFormat.PNG, ImageHelper.DEFAULT_PNG_COMPRESSION, bos);
                                        uri = ImageHelper.getDataUri(bos.getInputStream(), "image/png");
                                    }
                                } catch (Throwable ex) {
                                    Log.e(ex);
                                }

                            Element span = document.createElement("span");
                            if (uri == null) {
                                Element strong = document.createElement("strong");
                                strong.text(context.getString(R.string.title_attachment));
                                span.appendChild(strong);
                            } else {
                                Element img = document.createElement("img");
                                img.attr("src", uri);
                                img.attr("style", "vertical-align: middle; padding-top: 3px; padding-bottom: 3px;");
                                span.appendChild(img);
                            }
                            if (!TextUtils.isEmpty(attachment.name))
                                span.appendText(" " + attachment.name);
                            if (attachment.size != null)
                                span.appendText(" " + Helper.humanReadableByteCount(attachment.size));
                            span.appendElement("br");
                            header.appendChild(span);
                        }

                    header.appendElement("hr").appendElement("br");

                    document.body().prependChild(header);
                }

                args.putLong("received", message.received);
                return new String[]{message.subject, document.html()};
            }

            @Override
            protected void onExecuted(Bundle args, final String[] data) {
                if (data == null) {
                    Log.w("Print no data");
                    return;
                }

                final Context context = activity.getOriginalContext();
                boolean print_html_images = args.getBoolean("print_html_images");

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                boolean safe_browsing = prefs.getBoolean("safe_browsing", false);

                // https://developer.android.com/training/printing/html-docs.html
                printWebView = new WebView(context);

                WebSettings settings = printWebView.getSettings();
                settings.setUserAgentString(WebViewEx.getUserAgent(context, printWebView));
                settings.setLoadsImagesAutomatically(print_html_images);
                settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

                if (WebViewEx.isFeatureSupported(context, WebViewFeature.SAFE_BROWSING_ENABLE))
                    WebSettingsCompat.setSafeBrowsingEnabled(settings, safe_browsing);
                if (WebViewEx.isFeatureSupported(context, WebViewFeature.ATTRIBUTION_REGISTRATION_BEHAVIOR))
                    WebSettingsCompat.setAttributionRegistrationBehavior(settings, WebSettingsCompat.ATTRIBUTION_BEHAVIOR_DISABLED);

                settings.setJavaScriptEnabled(false);
                settings.setAllowFileAccess(true);

                printWebView.setWebViewClient(new WebViewClient() {
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        return false;
                    }

                    @Override
                    public void onPageFinished(WebView view, String url) {
                        Log.i("Print page finished");

                        try {
                            if (printWebView == null) {
                                Log.w("Print no view");
                                return;
                            }

                            PrintManager printManager = (PrintManager) context.getSystemService(Context.PRINT_SERVICE);
                            String jobName = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                    .format(args.getLong("received"));
                            if (!TextUtils.isEmpty(data[0]))
                                jobName += " " + data[0];

                            Log.i("Print queue job=" + jobName);
                            PrintDocumentAdapter adapter = printWebView.createPrintDocumentAdapter(jobName);
                            PrintJob job = printManager.print(jobName, adapter, new PrintAttributes.Builder().build());
                            EntityLog.log(context, "Print queued job=" + job.getInfo());
                        } catch (Throwable ex) {
                            try {
                                // android.content.ActivityNotFoundException: No Activity found to handle null
                                // 	at android.app.Instrumentation.checkStartActivityResult(Instrumentation.java:2206)
                                // 	at android.app.Activity.startIntentSenderForResultInner(Activity.java:6020)
                                // 	at android.app.Activity.startIntentSenderForResult(Activity.java:5983)
                                // 	at androidx.activity.ComponentActivity.startIntentSenderForResult(SourceFile:2)
                                // 	at android.app.Activity.startIntentSenderForResult(Activity.java:5938)
                                // 	at androidx.activity.ComponentActivity.startIntentSenderForResult(SourceFile:1)
                                // 	at android.app.Activity.startIntentSender(Activity.java:6186)
                                // 	at android.app.Activity.startIntentSender(Activity.java:6152)
                                // 	at android.print.PrintManager.print(PrintManager.java:538)
                                // 	at eu.faircode.email.FragmentDialogPrint$7$2.onPageFinished(SourceFile:127)
                                boolean report = !(ex instanceof ActivityNotFoundException);
                                if (ex instanceof ActivityNotFoundException)
                                    ex = new Throwable("A system app or component required for printing is missing." +
                                            " Is the print spooler still enabled?", ex);
                                Log.unexpectedError(fm, ex, report);
                            } catch (Throwable exex) {
                                Log.e(exex);
                            }
                        } finally {
                            printWebView = null;
                        }
                    }

                    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                        Log.w("Print error " + errorCode + ":" + description);
                    }
                });

                Log.i("Print load data");
                printWebView.loadDataWithBaseURL("about:blank", data[1], "text/html", StandardCharsets.UTF_8.name(), null);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(fm, ex);
            }
        }.execute(activity, args, "print");
    }
}
