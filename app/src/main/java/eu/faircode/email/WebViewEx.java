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

    Copyright 2018-2021 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
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
import androidx.webkit.WebViewFeature;

import static androidx.webkit.WebSettingsCompat.FORCE_DARK_OFF;
import static androidx.webkit.WebSettingsCompat.FORCE_DARK_ON;

public class WebViewEx extends WebView implements DownloadListener, View.OnLongClickListener {
    private int height;
    private IWebView intf;
    private Runnable onPageLoaded;

    private static String userAgent = null;

    private static final long PAGE_LOADED_FALLBACK_DELAY = 1500L; // milliseconds

    public WebViewEx(Context context) {
        super(context);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean overview_mode = prefs.getBoolean("overview_mode", false);
        boolean safe_browsing = prefs.getBoolean("safe_browsing", false);
        boolean confirm_html = prefs.getBoolean("confirm_html", true);
        boolean html_dark = prefs.getBoolean("html_dark", confirm_html);

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
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        if (WebViewEx.isFeatureSupported(WebViewFeature.SAFE_BROWSING_ENABLE))
            WebSettingsCompat.setSafeBrowsingEnabled(settings, safe_browsing);

        if (html_dark &&
                WebViewEx.isFeatureSupported(WebViewFeature.FORCE_DARK))
            WebSettingsCompat.setForceDark(settings,
                    Helper.isDarkTheme(context) ? FORCE_DARK_ON : FORCE_DARK_OFF);
    }

    void init(int height, float size, Pair<Integer, Integer> position, IWebView intf) {
        Log.i("Init height=" + height + " size=" + size);

        this.height = (height == 0 ? getMinimumHeight() : height);

        setInitialScale(size == 0 ? 0 : Math.round(size * 100));

        if (position != null) {
            setScrollX(position.first);
            setScrollY(position.second);
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean compact = prefs.getBoolean("compact", false);
        int zoom = prefs.getInt("view_zoom", compact ? 0 : 1);
        int message_zoom = prefs.getInt("message_zoom", 100);
        boolean monospaced = prefs.getBoolean("monospaced", false);

        WebSettings settings = getSettings();

        float fontSize = 16f /* Default */ * message_zoom / 100f;
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

        Log.i("onOverScrolled clamped=" + clampedY + " new=" + newScrollY + " dy=" + deltaY);
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

    public boolean isZoomedX() {
        int xtend = computeHorizontalScrollExtent();
        if (xtend != 0) {
            float xscale = computeHorizontalScrollRange() / (float) xtend;
            if (xscale > 1.2)
                return true;
        }

        return false;
    }

    public boolean isZoomedY() {
        int ytend = computeVerticalScrollExtent();
        if (ytend != 0) {
            float yscale = computeVerticalScrollRange() / (float) ytend;
            if (yscale > 1.2)
                return true;
        }

        return false;
    }

    public static boolean isFeatureSupported(String feature) {
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
        boolean generic_ua = prefs.getBoolean("generic_ua", true);
        if (generic_ua)
            return getGenericUserAgent(context);

        try {
            if (userAgent == null) {
                if (webView == null)
                    webView = new WebView(context);
                userAgent = webView.getSettings().getUserAgentString();
            }
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
