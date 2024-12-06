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

    Copyright 2018-2024 by Marcel Bokhorst (M66B)
*/

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Pair;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewCompat;
import androidx.webkit.WebViewFeature;

import java.util.Objects;

public class WebViewEx extends WebView implements DownloadListener, View.OnLongClickListener {
    private int height;
    private int maxHeight;
    private int viewportHeight;
    private IWebView intf;
    private Runnable onPageLoaded;
    private String hash;
    private Boolean images;

    private static String userAgent = null;

    static final int DEFAULT_VIEWPORT_HEIGHT = 8000;
    private static final long PAGE_LOADED_FALLBACK_DELAY = 1500L; // milliseconds

    public WebViewEx(Context context) {
        super(context);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.viewportHeight = prefs.getInt("viewport_height", getDefaultViewportHeight(context));
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
        settings.setJavaScriptEnabled(false);
        settings.setAllowContentAccess(true); // default
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        if (WebViewEx.isFeatureSupported(context, WebViewFeature.SAFE_BROWSING_ENABLE))
            WebSettingsCompat.setSafeBrowsingEnabled(settings, safe_browsing);
        if (WebViewEx.isFeatureSupported(context, WebViewFeature.ATTRIBUTION_REGISTRATION_BEHAVIOR))
            WebSettingsCompat.setAttributionRegistrationBehavior(settings, WebSettingsCompat.ATTRIBUTION_BEHAVIOR_DISABLED);
    }

    void init(int height, int maxHeight, float size, Pair<Integer, Integer> position, boolean force_light, IWebView intf) {
        Log.i("Init height=" + height + "/" + maxHeight + " size=" + size +
                " viewport=" + viewportHeight +
                " accelerated=" + isHardwareAccelerated());

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

        WebSettings settings = getSettings();

        boolean dark = Helper.isDarkTheme(context);

        // https://developer.android.com/reference/android/webkit/WebSettings#setAlgorithmicDarkeningAllowed(boolean)
        // https://developer.mozilla.org/en-US/docs/Web/CSS/@media/prefers-color-scheme
        boolean canDarken = WebViewEx.isFeatureSupported(context, WebViewFeature.ALGORITHMIC_DARKENING);
        if (canDarken)
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
                return intf.onOpenLink(url, false);
            }

            @Override
            public void onScaleChanged(WebView view, float oldScale, float newScale) {
                Log.i("Changed scale=" + newScale);
                intf.onScaleChanged(newScale);
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Log.w("WebViewEx error " + errorCode + ":" + description);
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    int yrange = computeVerticalScrollRange();
                    int yextend = computeVerticalScrollExtent();
                    int yoff = computeVerticalScrollOffset();
                    boolean canScrollVertical = (yrange > yextend && yoff < yrange - yextend);
                    Log.i("Scroll (x,y)=" + scrollX + "," + scrollY +
                            " yrange=" + yrange + " yextend=" + yextend + " yoff=" + yoff +
                            " can=" + canScrollVertical);
                    intf.onScrollChange(scrollX - oldScrollX, canScrollVertical ? scrollY - oldScrollY : -1, scrollX, scrollY);
                }
            });
    }

    void clearCache() {
        this.hash = null;
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
            boolean i = getSettings().getLoadsImagesAutomatically();
            if (Objects.equals(this.images, i)) {
                String h = (data == null ? null : Helper.md5(data.getBytes()));
                if (Objects.equals(this.hash, h))
                    return;
                this.hash = h;
            } else
                this.images = i;
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
        int limitHeight = MeasureSpec.makeMeasureSpec(viewportHeight, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, limitHeight);

        int mh = getMeasuredHeight();
        Log.i("Measured height=" + mh + " last=" + height + "/" + maxHeight + " ch=" + getContentHeight());
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
        try {
            final Context context = view.getContext();

            WebView.HitTestResult result = ((WebView) view).getHitTestResult();
            int type = result.getType();
            String extra = result.getExtra();
            Log.i("WebView long click type=" + type + " extra=" + extra);

            if (TextUtils.isEmpty(extra))
                return false;

            if (type == HitTestResult.PHONE_TYPE ||
                    type == HitTestResult.GEO_TYPE ||
                    type == HitTestResult.EMAIL_TYPE ||
                    type == HitTestResult.SRC_ANCHOR_TYPE ||
                    type == HitTestResult.EDIT_TEXT_TYPE) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                boolean confirm_links = prefs.getBoolean("confirm_links", true);
                if (confirm_links) {
                    ClipboardManager clipboard = Helper.getSystemService(context, ClipboardManager.class);
                    if (clipboard == null)
                        return false;

                    String title = context.getString(R.string.app_name);

                    ClipData clip = ClipData.newPlainText(title, extra);
                    clipboard.setPrimaryClip(clip);

                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
                        ToastEx.makeText(context, R.string.title_clipboard_copied, Toast.LENGTH_LONG).show();

                    return true;
                } else
                    return intf.onOpenLink(extra, true);
            }

            if (type == WebView.HitTestResult.IMAGE_TYPE ||
                    type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                Uri uri = Uri.parse(extra);
                if ("cid".equals(uri.getScheme()) || "data".equals(uri.getScheme()))
                    return false;

                Helper.view(context, uri, true);

                return true;
            }
        } catch (Throwable ex) {
            Log.e(ex);
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

    private float lastX;
    private float lastY;
    private int lastXoff;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean intercept = false;
        if (event.getAction() == MotionEvent.ACTION_DOWN)
            intercept = true; // Prevent ACTION_CANCEL on fling
        else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            int yrange = computeVerticalScrollRange();
            int yextend = computeVerticalScrollExtent();
            boolean canScrollVertical = (yrange > yextend);
            if (canScrollVertical) {
                int bottom = yrange - yextend;
                int yoff = computeVerticalScrollOffset();
                float dy = lastY - event.getY();
                intercept = (yoff > 0 || dy >= 0) && (yoff < bottom || dy <= 0);
            }

            if (!intercept) {
                int xrange = computeHorizontalScrollRange();
                int xextend = computeHorizontalScrollExtent();
                boolean canScrollHorizontal = (xrange > xextend);
                if (canScrollHorizontal) {
                    int right = xrange - xextend;
                    int xoff = computeHorizontalScrollOffset();
                    int ldx = xoff - lastXoff;
                    float dx = lastX - event.getX();
                    intercept = (xoff > 0 || dx >= 0) &&
                            (xoff < right || dx <= 0) &&
                            (Math.signum(dx) == Math.signum(ldx));
                    lastXoff = xoff;
                }
            }
        }
        getParent().requestDisallowInterceptTouchEvent(intercept || event.getPointerCount() > 1);

        lastX = event.getX();
        lastY = event.getY();
        return super.onTouchEvent(event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        intf.onUserInterAction();
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean dispatchKeyShortcutEvent(KeyEvent event) {
        intf.onUserInterAction();
        return super.dispatchKeyShortcutEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN)
            intf.onUserInterAction();
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean dispatchTrackballEvent(MotionEvent event) {
        intf.onUserInterAction();
        return super.dispatchTrackballEvent(event);
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        intf.onUserInterAction();
        return super.dispatchGenericMotionEvent(event);
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

    static int getDefaultViewportHeight(Context context) {
        return DEFAULT_VIEWPORT_HEIGHT;
    }

    @NonNull
    static String getUserAgent(Context context) {
        return getUserAgent(context, null);
    }

    @NonNull
    static String getUserAgent(Context context, WebView webView) {
        // https://developer.chrome.com/docs/multidevice/user-agent/#chrome-for-android
        // https://android-developers.googleblog.com/2024/12/user-agent-reduction-on-android-webview.html
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

        void onScrollChange(int dx, int dy, int scrollX, int scrollY);

        boolean onOpenLink(String url, boolean always);

        void onUserInterAction();
    }
}
