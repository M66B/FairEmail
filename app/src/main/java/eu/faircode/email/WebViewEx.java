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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.util.Pair;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewCompat;
import androidx.webkit.WebViewFeature;

import java.util.Objects;

public class WebViewEx extends WebView implements DownloadListener, View.OnLongClickListener {
    private int height;
    private int maxHeight;
    private boolean legacy;
    private IWebView intf;
    private Runnable onPageLoaded;
    private String hash;

    private static String userAgent = null;

    private static final long PAGE_LOADED_FALLBACK_DELAY = 1500L; // milliseconds

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
        settings.setUserAgentString(getUserAgent(context, this));
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(overview_mode);

        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);

        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);

        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(true); // default
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        if (WebViewEx.isFeatureSupported(context, WebViewFeature.SAFE_BROWSING_ENABLE))
            WebSettingsCompat.setSafeBrowsingEnabled(settings, safe_browsing);
    }

    void init(int height, int maxHeight, float size, Pair<Integer, Integer> position, boolean force_light, IWebView intf) {
        Log.i("Init height=" + height + "/" + maxHeight + " size=" + size);

        if (maxHeight == 0) {
            Log.e("WebView max height zero");
            maxHeight = getResources().getDisplayMetrics().heightPixels;
        }

        this.height = (height == 0 ? getMinimumHeight() : height);
        this.maxHeight = maxHeight;

        setInitialScale(size == 0 ? 0 : Math.round(size * 100));

        if (position != null) {
            setScrollX(position.first);
            setScrollY(position.second);
        }

        final Context context = getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean compact = prefs.getBoolean("compact", false);
        int zoom = prefs.getInt("view_zoom", compact ? 0 : 1);
        boolean browser_zoom = (prefs.getBoolean("browser_zoom", false) && BuildConfig.DEBUG);
        int message_zoom = prefs.getInt("message_zoom", 100);
        boolean monospaced = prefs.getBoolean("monospaced", false);
        legacy = (prefs.getBoolean("webview_legacy", false) && BuildConfig.DEBUG);

        WebSettings settings = getSettings();

        boolean dark = Helper.isDarkTheme(context);

        // https://developer.android.com/reference/android/webkit/WebSettings#setAlgorithmicDarkeningAllowed(boolean)
        // https://developer.mozilla.org/en-US/docs/Web/CSS/@media/prefers-color-scheme
        boolean canDarken = WebViewEx.isFeatureSupported(context, WebViewFeature.ALGORITHMIC_DARKENING);
        if (canDarken && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            WebSettingsCompat.setAlgorithmicDarkeningAllowed(settings, dark && !force_light);
        setBackgroundColor(canDarken && dark && !force_light ? Color.TRANSPARENT : Color.WHITE);

        float fontSize = 16f /* Default */ *
                (browser_zoom ? 1f : message_zoom / 100f);
        if (zoom == 0 /* small */)
            fontSize *= HtmlHelper.FONT_SMALL;
        else if (zoom == 2 /* large */)
            fontSize *= HtmlHelper.FONT_LARGE;

        settings.setDefaultFontSize(Math.round(fontSize));
        settings.setDefaultFixedFontSize(Math.round(fontSize));

        if (monospaced)
            settings.setStandardFontFamily("monospace");

        this.intf = intf;

        setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.i("Started url=" + url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                Log.i("Finished url=" + url);
            }

            @Override
            public void onPageCommitVisible(WebView view, String url) {
                Log.i("Commit url=" + url + " runnable=" + (onPageLoaded != null));
                if (onPageLoaded != null) {
                    ApplicationEx.getMainHandler().post(onPageLoaded);
                    onPageLoaded = null;
                }
            }

            @Override
            public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
                Log.e("Render process gone");
                if (onPageLoaded != null) {
                    ApplicationEx.getMainHandler().post(onPageLoaded);
                    onPageLoaded = null;
                }
                return super.onRenderProcessGone(view, detail);
            }

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

    void setOnPageLoaded(Runnable runnable) {
        Log.i("Set on page finished");
        onPageLoaded = runnable;

        ApplicationEx.getMainHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    if (onPageLoaded != null) {
                        Log.i("Page loaded fallback");
                        onPageLoaded.run();
                        onPageLoaded = null;
                    }
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }
        }, PAGE_LOADED_FALLBACK_DELAY);
    }

    void setImages(boolean show_images, boolean inline) {
        WebSettings settings = getSettings();
        settings.setLoadsImagesAutomatically(show_images || inline);
        settings.setBlockNetworkLoads(!show_images);
        settings.setBlockNetworkImage(!show_images);
    }

    @Override
    public void loadDataWithBaseURL(String baseUrl, String data, String mimeType, String encoding, String historyUrl) {
        try {
            // Prevent flickering
            String h = (data == null ? null : Helper.md5(data.getBytes()));
            if (Objects.equals(hash, h))
                return;
            this.hash = h;
        } catch (Throwable ex) {
            Log.w(ex);
        }

        super.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl);
    }

    @Override
    public void setMinimumHeight(int minHeight) {
        super.setMinimumHeight(minHeight);
        Log.i("Set min height=" + minHeight);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Unable to create layer for WebViewEx, size 1088x16384 max size 16383 color type 4 has context 1)
        int limitHeight = MeasureSpec.makeMeasureSpec(16000, MeasureSpec.AT_MOST);
        if (legacy) {
            if (height > getMinimumHeight())
                super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST));
            else
                super.onMeasure(widthMeasureSpec, limitHeight);
        } else {
            super.onMeasure(widthMeasureSpec, limitHeight);
        }

        int mh = getMeasuredHeight();
        Log.i("Measured height=" + mh + " last=" + height + "/" + maxHeight + " ch=" + getContentHeight());
        if (mh == 0 && legacy)
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

        Log.i("onOverScrolled" +
                " clampedY=" + clampedY +
                " scrollY=" + scrollY +
                " deltaY=" + deltaY +
                " RangeY=" + scrollRangeY +
                " maxY=" + maxOverScrollY +
                " newY=" + (scrollY + deltaY) + "/" + newScrollY +
                " dy=" + deltaY +
                " top=" + top +
                " bottom=" + bottom);

        if (Math.abs(deltaY) > bottom - top)
            deltaY = (deltaY > 0 ? 1 : -1) * (bottom - top);

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

            Helper.view(view.getContext(), uri, true);

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

    public boolean isZoomedY() {
/*
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int ch = Math.round(getContentHeight() * dm.density);
        int dp6 = Math.round(6 * dm.density);
        return (ch - dp6 > getHeight());
*/
        int ytend = computeVerticalScrollExtent();
        if (ytend == 0)
            return false;

        float yscale = computeVerticalScrollRange() / (float) ytend;
        return (yscale > 1.01);
    }

    public static boolean isFeatureSupported(Context context, String feature) {
        if (WebViewFeature.ALGORITHMIC_DARKENING.equals(feature)) {
            if (BuildConfig.DEBUG) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                boolean fake_dark = prefs.getBoolean("fake_dark", false);
                if (fake_dark)
                    return false;
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
                return false;

            try {
                PackageInfo pkg = WebViewCompat.getCurrentWebViewPackage(context);
                if (pkg != null && pkg.versionCode / 100000 < 5005) // Version 102.*
                    return false;
            } catch (Throwable ex) {
                Log.e(ex);
            }
        }

        try {
            return WebViewFeature.isFeatureSupported(feature);
        } catch (Throwable ex) {
            /*
                java.lang.ExceptionInInitializerError
                  at androidx.webkit.internal.WebViewGlueCommunicator.getFactory(SourceFile:1)
                  at androidx.webkit.internal.WebViewFeatureInternal$LAZY_HOLDER.<clinit>(SourceFile:2)
                  at androidx.webkit.internal.WebViewFeatureInternal.isSupportedByWebView(SourceFile:1)
                  at androidx.webkit.internal.WebViewFeatureInternal.isSupported(SourceFile:13)
                  at androidx.webkit.internal.WebViewFeatureInternal.isSupported(SourceFile:11)
                  at androidx.webkit.internal.WebViewFeatureInternal.isSupported(SourceFile:4)
                  at androidx.webkit.WebViewFeature.isFeatureSupported(SourceFile:1)
             */
            Log.w(ex);
            return false;
        }
    }

    @NonNull
    static String getUserAgent(Context context) {
        return getUserAgent(context, null);
    }

    @NonNull
    static String getUserAgent(Context context, WebView webView) {
        // https://developer.chrome.com/docs/multidevice/user-agent/#chrome-for-android
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean generic_ua = prefs.getBoolean("generic_ua", false);
        if (generic_ua)
            return getGenericUserAgent(context);

        try {
            if (userAgent == null)
                userAgent = WebSettings.getDefaultUserAgent(context);
            return userAgent;
        } catch (Throwable ex) {
            Log.w(ex);
            return getGenericUserAgent(context);
        }
    }

    @NonNull
    private static String getGenericUserAgent(Context context) {
        boolean large = context.getResources().getConfiguration()
                .isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_LARGE);
        return (large ? "Mozilla/5.0" : "Mozilla/5.0 (Mobile)");
    }

    interface IWebView {
        void onSizeChanged(int w, int h, int ow, int oh);

        void onScaleChanged(float newScale);

        void onScrollChange(int scrollX, int scrollY);

        void onOverScrolled(int scrollX, int scrollY, int dx, int dy, boolean clampedX, boolean clampedY);

        boolean onOpenLink(String url);
    }
}
