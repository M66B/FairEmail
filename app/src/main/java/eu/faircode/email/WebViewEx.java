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
import android.view.InputDevice;
import android.view.MotionEvent;
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

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean overview_mode = prefs.getBoolean("overview_mode", false);
        boolean safe_browsing = prefs.getBoolean("safe_browsing", false);

        setVerticalScrollBarEnabled(false);
        setHorizontalScrollBarEnabled(false);
        setOverScrollMode(View.OVER_SCROLL_NEVER);

        setDownloadListener(this);
        setOnLongClickListener(this);

        WebSettings settings = getSettings();
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(overview_mode);

        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);

        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);

        settings.setAllowFileAccess(false);
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            settings.setSafeBrowsingEnabled(safe_browsing);
    }

    void init(
            int height, float size, Pair<Integer, Integer> position,
            float textSize, boolean monospaced,
            IWebView intf) {
        Log.i("Init height=" + height + " size=" + size);

        this.height = (height == 0 ? getMinimumHeight() : height);

        setInitialScale(size == 0 ? 0 : Math.round(size * 100));

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

    void setImages(boolean show_images, boolean inline) {
        WebSettings settings = getSettings();
        settings.setLoadsImagesAutomatically(show_images || inline);
        settings.setBlockNetworkLoads(!show_images);
        settings.setBlockNetworkImage(!show_images);
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
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        final int overScrollMode = getOverScrollMode();
        final boolean canScrollHorizontal =
                computeHorizontalScrollRange() > computeHorizontalScrollExtent();
        final boolean canScrollVertical =
                computeVerticalScrollRange() > computeVerticalScrollExtent();
        final boolean overScrollHorizontal = overScrollMode == OVER_SCROLL_ALWAYS ||
                (overScrollMode == OVER_SCROLL_IF_CONTENT_SCROLLS && canScrollHorizontal);
        final boolean overScrollVertical = overScrollMode == OVER_SCROLL_ALWAYS ||
                (overScrollMode == OVER_SCROLL_IF_CONTENT_SCROLLS && canScrollVertical);

        int newScrollX = scrollX + deltaX;
        if (!overScrollHorizontal) {
            maxOverScrollX = 0;
        }

        int newScrollY = scrollY + deltaY;
        if (!overScrollVertical) {
            maxOverScrollY = 0;
        }

        // Clamp values if at the limits and record
        final int left = -maxOverScrollX;
        final int right = maxOverScrollX + scrollRangeX;
        final int top = -maxOverScrollY;
        final int bottom = maxOverScrollY + scrollRangeY;

        boolean clampedX = false;
        if (newScrollX > right) {
            newScrollX = right;
            clampedX = true;
        } else if (newScrollX < left) {
            newScrollX = left;
            clampedX = true;
        }

        boolean clampedY = false;
        if (newScrollY > bottom) {
            newScrollY = bottom;
            clampedY = true;
        } else if (newScrollY < top) {
            newScrollY = top;
            clampedY = true;
        }

        Log.i("MMM clamped=" + clampedY + " new=" + newScrollY + " dy=" + deltaY + " mode=" + overScrollMode);
        intf.onOverScrolled(scrollX, scrollY, deltaX, deltaY, clampedX, clampedY);

        return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);
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

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_SCROLL &&
                (event.getSource() & InputDevice.SOURCE_MOUSE) != 0)
            return false;
        return super.onGenericMotionEvent(event);
    }

    public boolean isZoomed() {
        int xtend = computeHorizontalScrollExtent();
        if (xtend != 0) {
            float xscale = computeHorizontalScrollRange() / (float) xtend;
            if (xscale > 1.2)
                return true;
        }

        int ytend = computeVerticalScrollExtent();
        if (ytend != 0) {
            float yscale = computeVerticalScrollRange() / (float) ytend;
            if (yscale > 1.2)
                return true;
        }

        return false;
    }

    interface IWebView {
        void onSizeChanged(int w, int h, int ow, int oh);

        void onScaleChanged(float newScale);

        void onScrollChange(int scrollX, int scrollY);

        void onOverScrolled(int scrollX, int scrollY, int dx, int dy, boolean clampedX, boolean clampedY);

        boolean onOpenLink(String url);
    }
}
