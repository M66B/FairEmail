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

    Copyright 2018-2019 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

// https://developer.android.com/reference/android/webkit/WebView

public class FragmentWebView extends FragmentBase {
    private ProgressBar progressBar;
    private WebView webview;

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_webview, container, false);

        progressBar = view.findViewById(R.id.progressbar);
        webview = view.findViewById(R.id.webview);

        progressBar.setProgress(0);
        progressBar.setVisibility(View.VISIBLE);

        WebSettings settings = webview.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        webview.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (prefs.getBoolean("webview", false)) {
                    view.loadUrl(url);
                    setSubtitle(url);
                    return false;
                } else {
                    Helper.view(getContext(), getViewLifecycleOwner(), Uri.parse(url), true);
                    return true;
                }
            }
        });

        webview.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                progressBar.setProgress(progress);
                if (progress == 100)
                    progressBar.setVisibility(View.GONE);
            }
        });

        webview.setDownloadListener(new DownloadListener() {
            public void onDownloadStart(
                    String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                Log.i("Download url=" + url + " mime type=" + mimetype);

                Uri uri = Uri.parse(url);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(uri);

                PackageManager pm = getContext().getPackageManager();
                if (intent.resolveActivity(pm) == null)
                    Toast.makeText(getContext(), getString(R.string.title_no_viewer, uri.toString()), Toast.LENGTH_LONG).show();
                else
                    startActivity(intent);
            }
        });

        registerForContextMenu(webview);

        onLoad();

        ((ActivityBase) getActivity()).addBackPressedListener(onBackPressedListener);

        return view;
    }

    private void onLoad() {
        Bundle args = getArguments();
        if (args.containsKey("url")) {
            String url = args.getString("url");
            webview.loadUrl(url);
            setSubtitle(url);
        } else if (args.containsKey("id")) {
            new SimpleTask<String>() {
                @Override
                protected String onExecute(Context context, Bundle args) throws Throwable {
                    long id = args.getLong("id");

                    String html = EntityMessage.read(context, id);

                    Document doc = Jsoup.parse(html);
                    for (Element img : doc.select("img"))
                        try {
                            String src = img.attr("src");
                            if (src.startsWith("cid")) {
                                String[] cids = src.split(":");
                                if (cids.length > 1) {
                                    String cid = "<" + cids[1] + ">";
                                    EntityAttachment attachment = DB.getInstance(context).attachment().getAttachment(id, cid);
                                    if (attachment != null && attachment.available) {
                                        InputStream is = null;
                                        try {
                                            File file = EntityAttachment.getFile(context, attachment.id);

                                            is = new BufferedInputStream(new FileInputStream(file));
                                            byte[] bytes = new byte[(int) file.length()];
                                            if (is.read(bytes) != bytes.length)
                                                throw new IOException("length");

                                            StringBuilder sb = new StringBuilder();
                                            sb.append("data:");
                                            sb.append(attachment.type);
                                            sb.append(";base64,");
                                            sb.append(Base64.encodeToString(bytes, Base64.DEFAULT));

                                            img.attr("src", sb.toString());
                                        } finally {
                                            if (is != null)
                                                is.close();
                                        }
                                    }
                                }
                            }
                        } catch (Throwable ex) {
                            Log.e(ex);
                        }

                    return doc.html();
                }

                @Override
                protected void onExecuted(Bundle args, String html) {
                    String from = args.getString("from");
                    webview.loadDataWithBaseURL("email://", html, "text/html", "UTF-8", null);
                    setSubtitle(from);
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
                }
            }.execute(this, args, "webview:format");
        }
    }

    @Override
    public void onDestroyView() {
        ((ActivityBase) getActivity()).removeBackPressedListener(onBackPressedListener);
        super.onDestroyView();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);

        if (view instanceof WebView) {
            final WebView.HitTestResult result = ((WebView) view).getHitTestResult();
            if (result.getType() == WebView.HitTestResult.IMAGE_TYPE ||
                    result.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                Log.i("Context menu url=" + result.getExtra());

                menu.add(Menu.NONE, 1, 0, R.string.title_view)
                        .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                Uri uri = Uri.parse(result.getExtra());

                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(uri);

                                PackageManager pm = getContext().getPackageManager();
                                if (intent.resolveActivity(pm) == null)
                                    Toast.makeText(getContext(), getString(R.string.title_no_viewer, uri.toString()), Toast.LENGTH_LONG).show();
                                else
                                    startActivity(intent);

                                return true;
                            }
                        });
            }
        }
    }

    ActivityBase.IBackPressedListener onBackPressedListener = new ActivityBase.IBackPressedListener() {
        @Override
        public boolean onBackPressed() {
            boolean can = webview.canGoBack();
            if (can)
                webview.goBack();

            Bundle args = getArguments();
            if (args.containsKey("from") && !webview.canGoBack())
                setSubtitle(args.getString("from"));

            return can;
        }
    };
}
