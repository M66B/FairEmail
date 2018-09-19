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

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

// https://developer.android.com/reference/android/webkit/WebView

public class FragmentWebView extends FragmentEx {
    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_webview, container, false);

        final ProgressBar progressBar = view.findViewById(R.id.progressbar);
        final WebView webview = view.findViewById(R.id.webview);

        progressBar.setProgress(0);
        progressBar.setVisibility(View.VISIBLE);

        WebSettings settings = webview.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        webview.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (prefs.getBoolean("webview", false)) {
                    view.loadUrl(url);
                    setSubtitle(url);
                    return false;
                } else {
                    Helper.view(getContext(), Uri.parse(url));
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

        Bundle args = getArguments();
        if (args.containsKey("url")) {
            String url = args.getString("url");
            webview.loadUrl(url);
            setSubtitle(url);
        } else if (args.containsKey("html")) {
            String html = args.getString("html");
            String from = args.getString("from");
            webview.loadDataWithBaseURL("email://", html, "text/html", "UTF-8", null);
            setSubtitle(from);
        }

        ((ActivityBase) getActivity()).addBackPressedListener(new ActivityBase.IBackPressedListener() {
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
        });

        return view;
    }
}
