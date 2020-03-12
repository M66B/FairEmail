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

    Copyright 2018-2020 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.util.Pair;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.preference.PreferenceManager;

public class WebViewEx extends WebView implements DownloadListener, View.OnLongClickListener {
    private int height;
    private IWebView intf;

    public WebViewEx(Context context) {
        super(context);

        setVerticalScrollBarEnabled(false);
        setHorizontalScrollBarEnabled(false);

        setDownloadListener(this);
        setOnLongClickListener(this);

        WebSettings settings = getSettings();
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);

        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);

        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);

        settings.setAllowFileAccess(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            boolean safe_browsing = prefs.getBoolean("safe_browsing", true);
            settings.setSafeBrowsingEnabled(safe_browsing);
        }
    }

    void init(
            int height, float size, Pair<Integer, Integer> position,
            float textSize, boolean monospaced,
            boolean show_images, boolean inline,
            IWebView intf) {
        Log.i("Init height=" + height + " size=" + size);

        this.height = (height == 0 ? getMinimumHeight() : height);

        setInitialScale(size == 0 ? 1 : Math.round(size * 100));

        if (position != null) {
            setScrollX(position.first);
            setScrollY(position.second);
        }

        WebSettings settings = getSettings();
        if (textSize != 0) {
            int dp = Helper.pixels2dp(getContext(), textSize);
            settings.setDefaultFontSize(Math.round(dp));
            settings.setDefaultFixedFontSize(Math.round(dp));
        }
        if (monospaced)
            settings.setStandardFontFamily("monospace");

        settings.setLoadsImagesAutomatically(show_images || inline);
        settings.setBlockNetworkLoads(!show_images);
        settings.setBlockNetworkImage(!show_images);

        this.intf = intf;

        setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.i("Open url=" + url);
                return intf.onOpenLink(url);
            }

            @Override
            public void onScaleChanged(WebView view, float oldScale, float newScale) {
                Log.i("Changed scale=" + newScale);
                intf.onScaleChanged(newScale);
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    Log.i("Scroll (x,y)=" + scrollX + "," + scrollY);
                    intf.onScrollChange(scrollX, scrollY);
                }
            });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (height > getMinimumHeight())
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST));
        else
            super.onMeasure(widthMeasureSpec, heightMeasureSpec); // Unspecified

        int mh = getMeasuredHeight();
        Log.i("Measured height=" + mh + " last=" + height);
        if (mh == 0)
            setMeasuredDimension(getMeasuredWidth(), height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);
        Log.i("Size changed height=" + h);
        this.intf.onSizeChanged(w, h, ow, oh);
    }

    @Override
    public void onDownloadStart(
            String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
        Log.i("Download url=" + url + " mime type=" + mimetype);

        Uri uri = Uri.parse(url);
        if ("cid".equals(uri.getScheme()) || "data".equals(uri.getScheme()))
            return;

        Helper.view(getContext(), uri, true);
    }

    @Override
    public boolean onLongClick(View view) {
        WebView.HitTestResult result = ((WebView) view).getHitTestResult();
        if (result.getType() == WebView.HitTestResult.IMAGE_TYPE ||
                result.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
            Log.i("Long press url=" + result.getExtra());

            Uri uri = Uri.parse(result.getExtra());
            if ("cid".equals(uri.getScheme()) || "data".equals(uri.getScheme()))
                return false;

            Helper.view(getContext(), uri, true);

            return true;
        }
        return false;
    }

    interface IWebView {
        void onSizeChanged(int w, int h, int ow, int oh);

        void onScaleChanged(float newScale);

        void onScrollChange(int scrollX, int scrollY);

        boolean onOpenLink(String url);
    }
}
