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
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.preference.PreferenceManager;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

class ImageHelper {
    private static final float MIN_LUMINANCE = 0.33f;

    private static final ExecutorService executor =
            Executors.newSingleThreadExecutor(Helper.backgroundThreadFactory);

    static Bitmap generateIdenticon(@NonNull String email, int size, int pixels, boolean dark) {
        byte[] hash = getHash(email);

        int color = Color.argb(255, hash[0], hash[1], hash[2]);
        color = Helper.adjustLuminance(color, dark, MIN_LUMINANCE);

        Paint paint = new Paint();
        paint.setColor(color);

        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.TRANSPARENT);

        float psize = (float) size / pixels;

        for (int x = 0; x < pixels; x++) {
            int i = (x > pixels / 2 ? pixels - x - 1 : x);
            for (int y = 0; y < pixels; y++) {
                if ((hash[i] >> y & 1) == 1) {
                    RectF rect = new RectF(x * psize, y * psize, (x + 1) * psize, (y + 1) * psize);
                    canvas.drawRect(rect, paint);
                }
            }
        }

        return bitmap;
    }

    static Bitmap generateLetterIcon(@NonNull String email, int size, boolean dark) {
        String text = null;
        for (int i = 0; i < email.length(); i++) {
            char kar = email.charAt(i);
            if (Character.isAlphabetic(kar)) {
                text = email.substring(i, i + 1).toUpperCase();
                break;
            }
        }
        if (text == null)
            return null;

        byte[] hash = getHash(email);

        int color = Color.argb(255, hash[0], hash[1], hash[2]);
        color = Helper.adjustLuminance(color, dark, MIN_LUMINANCE);

        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(color);

        double lum = ColorUtils.calculateLuminance(color);

        Paint paint = new Paint();
        paint.setColor(lum < 0.5 ? Color.WHITE : Color.BLACK);
        paint.setTextSize(size / 2f);
        paint.setTypeface(Typeface.DEFAULT_BOLD);

        canvas.drawText(
                text,
                size / 2f - paint.measureText(text) / 2,
                size / 2f - (paint.descent() + paint.ascent()) / 2, paint);

        return bitmap;
    }

    private static byte[] getHash(String email) {
        try {
            return MessageDigest.getInstance("MD5").digest(email.getBytes());
        } catch (NoSuchAlgorithmException ignored) {
            return email.getBytes();
        }
    }

    static Drawable decodeImage(final Context context, final long id, String source, boolean show, final TextView view) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean compact = prefs.getBoolean("compact", false);
        int zoom = prefs.getInt("zoom", compact ? 0 : 1);
        boolean inline = prefs.getBoolean("inline_images", false);

        final int px = Helper.dp2pixels(context, (zoom + 1) * 24);
        final Resources.Theme theme = context.getTheme();
        final Resources res = context.getResources();

        try {
            final AnnotatedSource a = new AnnotatedSource(source);

            if (TextUtils.isEmpty(a.source)) {
                Drawable d = res.getDrawable(R.drawable.baseline_broken_image_24, theme);
                d.setBounds(0, 0, px, px);
                return d;
            }

            boolean embedded = a.source.startsWith("cid:");
            boolean data = a.source.startsWith("data:");

            if (BuildConfig.DEBUG)
                Log.i("Image show=" + show + " inline=" + inline +
                        " embedded=" + embedded + " data=" + data + " source=" + a.source);

            // Embedded images
            if (embedded && (show || inline)) {
                DB db = DB.getInstance(context);
                String cid = "<" + a.source.substring(4) + ">";
                EntityAttachment attachment = db.attachment().getAttachment(id, cid);
                if (attachment == null) {
                    Log.i("Image not found CID=" + cid);
                    Drawable d = res.getDrawable(R.drawable.baseline_broken_image_24, theme);
                    d.setBounds(0, 0, px, px);
                    return d;
                } else if (!attachment.available) {
                    Log.i("Image not available CID=" + cid);
                    Drawable d = res.getDrawable(R.drawable.baseline_hourglass_empty_24, theme);
                    d.setBounds(0, 0, px, px);
                    return d;
                } else {
                    int scaleToPixels = res.getDisplayMetrics().widthPixels;
                    if ("image/gif".equals(attachment.type) &&
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        ImageDecoder.Source isource = ImageDecoder.createSource(attachment.getFile(context));
                        Drawable gif;
                        try {
                            gif = ImageDecoder.decodeDrawable(isource, new ImageDecoder.OnHeaderDecodedListener() {
                                @Override
                                public void onHeaderDecoded(
                                        @NonNull ImageDecoder decoder,
                                        @NonNull ImageDecoder.ImageInfo info,
                                        @NonNull ImageDecoder.Source source) {
                                    int factor = 1;
                                    while (info.getSize().getWidth() / factor > scaleToPixels)
                                        factor *= 2;

                                    decoder.setTargetSampleSize(factor);
                                }
                            });
                        } catch (IOException ex) {
                            Log.w(ex);
                            gif = null;
                        }
                        if (gif == null) {
                            Log.i("GIF not decodable CID=" + cid);
                            Drawable d = res.getDrawable(R.drawable.baseline_broken_image_24, theme);
                            d.setBounds(0, 0, px, px);
                            return d;
                        } else {
                            if (view != null)
                                fitDrawable(gif, a, view);
                            return gif;
                        }
                    } else {
                        Bitmap bm = Helper.decodeImage(attachment.getFile(context), scaleToPixels);
                        if (bm == null) {
                            Log.i("Image not decodable CID=" + cid);
                            Drawable d = res.getDrawable(R.drawable.baseline_broken_image_24, theme);
                            d.setBounds(0, 0, px, px);
                            return d;
                        } else {
                            Drawable d = new BitmapDrawable(res, bm);
                            DisplayMetrics dm = context.getResources().getDisplayMetrics();
                            d.setBounds(0, 0, Math.round(bm.getWidth() * dm.density), Math.round(bm.getHeight() * dm.density));
                            if (view != null)
                                fitDrawable(d, a, view);
                            return d;
                        }
                    }
                }
            }

            // Data URI
            if (data && (show || inline || a.tracking))
                try {
                    Drawable d = getDataDrawable(context, a.source);
                    if (view != null)
                        fitDrawable(d, a, view);
                    return d;
                } catch (IllegalArgumentException ex) {
                    Log.w(ex);
                    Drawable d = res.getDrawable(R.drawable.baseline_broken_image_24, theme);
                    d.setBounds(0, 0, px, px);
                    return d;
                }

            if (!show) {
                // Show placeholder icon
                int resid = (embedded || data ? R.drawable.baseline_photo_library_24 : R.drawable.baseline_image_24);
                Drawable d = res.getDrawable(resid, theme);
                d.setBounds(0, 0, px, px);
                return d;
            }

            // Get cache file name
            File dir = new File(context.getCacheDir(), "images");
            if (!dir.exists())
                dir.mkdir();
            final File file = new File(dir, id + "_" + Math.abs(a.source.hashCode()) + ".png");

            Drawable cached = getCachedImage(context, file);
            if (cached != null || view == null) {
                if (view == null)
                    if (cached == null) {
                        Drawable d = res.getDrawable(R.drawable.baseline_hourglass_empty_24, theme);
                        d.setBounds(0, 0, px, px);
                        return d;
                    } else
                        return cached;
                else
                    fitDrawable(cached, a, view);
                return cached;
            }

            final LevelListDrawable lld = new LevelListDrawable();
            Drawable wait = res.getDrawable(R.drawable.baseline_hourglass_empty_24, theme);
            lld.addLevel(1, 1, wait);
            lld.setBounds(0, 0, px, px);
            lld.setLevel(1);

            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        Drawable cached = getCachedImage(context, file);
                        if (cached != null) {
                            fitDrawable(cached, a, view);
                            post(cached, a.source);
                            return;
                        }

                        BitmapFactory.Options options = new BitmapFactory.Options();
                        Log.i("Probe " + a.source);
                        try (InputStream probe = new URL(a.source).openStream()) {
                            options.inJustDecodeBounds = true;
                            BitmapFactory.decodeStream(probe, null, options);
                        }

                        Log.i("Download " + a.source);
                        Bitmap bm;
                        try (InputStream is = new URL(a.source).openStream()) {
                            int scaleTo = res.getDisplayMetrics().widthPixels;
                            int factor = 1;
                            while (options.outWidth / factor > scaleTo)
                                factor *= 2;

                            if (factor > 1) {
                                Log.i("Download image factor=" + factor);
                                options.inJustDecodeBounds = false;
                                options.inSampleSize = factor;
                                bm = BitmapFactory.decodeStream(is, null, options);
                            } else
                                bm = BitmapFactory.decodeStream(is);
                        }

                        if (bm == null)
                            throw new FileNotFoundException("Download image failed source=" + a.source);

                        Log.i("Downloaded image source=" + a.source);

                        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
                            bm.compress(Bitmap.CompressFormat.PNG, 90, os);
                        }

                        // Create drawable from bitmap
                        Drawable d = new BitmapDrawable(res, bm);
                        DisplayMetrics dm = context.getResources().getDisplayMetrics();
                        d.setBounds(0, 0, Math.round(bm.getWidth() * dm.density), Math.round(bm.getHeight() * dm.density));
                        fitDrawable(d, a, view);
                        post(d, a.source);
                    } catch (Throwable ex) {
                        // Show broken icon
                        Log.w(ex);
                        int resid = (ex instanceof IOException && !(ex instanceof FileNotFoundException)
                                ? R.drawable.baseline_cloud_off_24
                                : R.drawable.baseline_broken_image_24);
                        Drawable d = res.getDrawable(resid, theme);
                        d.setBounds(0, 0, px, px);
                        post(d, a.source);
                    }
                }

                private void post(final Drawable d, String source) {
                    Log.i("Posting image=" + source);

                    new Handler(context.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Rect bounds = d.getBounds();

                            lld.addLevel(0, 0, d);
                            lld.setBounds(0, 0, bounds.width(), bounds.height());
                            lld.setLevel(0);

                            view.setText(view.getText());
                        }
                    });
                }
            });

            return lld;
        } catch (Throwable ex) {
            Log.e(ex);

            Drawable d = res.getDrawable(R.drawable.baseline_broken_image_24, theme);
            d.setBounds(0, 0, px, px);
            return d;
        }
    }

    private static void fitDrawable(final Drawable d, AnnotatedSource a, final View view) {
        Rect bounds = d.getBounds();
        int w = bounds.width();
        int h = bounds.height();

        if (a.width == 0 && a.height != 0)
            a.width = Math.round(a.height * w / (float) h);
        if (a.height == 0 && a.width != 0)
            a.height = Math.round(a.width * h / (float) w);

        if (a.width != 0 && a.height != 0) {
            w = Helper.dp2pixels(view.getContext(), a.width);
            h = Helper.dp2pixels(view.getContext(), a.height);
            d.setBounds(0, 0, w, h);
        }

        final Semaphore semaphore = new Semaphore(0);

        view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                view.removeOnLayoutChangeListener(this);

                Rect bounds = d.getBounds();
                int w = bounds.width();
                int h = bounds.height();

                float width = view.getWidth();
                if (w > width) {
                    float scale = width / w;
                    w = Math.round(w * scale);
                    h = Math.round(h * scale);
                    d.setBounds(0, 0, w, h);
                }

                semaphore.release();
            }
        });

        try {
            semaphore.tryAcquire(2500, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            Log.e(ex);
        }
    }

    private static Drawable getDataDrawable(Context context, String source) {
        // "<img src=\"data:image/png;base64,iVBORw0KGgoAAA" +
        // "ANSUhEUgAAAAUAAAAFCAYAAACNbyblAAAAHElEQVQI12P4" +
        // "//8/w38GIAXDIBKE0DHxgljNBAAO9TXL0Y4OHwAAAABJRU" +
        // "5ErkJggg==\" alt=\"Red dot\" />";

        String base64 = source.substring(source.indexOf(',') + 1);
        byte[] bytes = Base64.decode(base64.getBytes(), 0);

        Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        if (bm == null)
            throw new IllegalArgumentException("decode byte array failed");

        Drawable d = new BitmapDrawable(context.getResources(), bm);

        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        d.setBounds(0, 0, Math.round(bm.getWidth() * dm.density), Math.round(bm.getHeight() * dm.density));

        return d;
    }

    private static Drawable getCachedImage(Context context, File file) {
        if (file.exists()) {
            Log.i("Using cached " + file);
            Bitmap bm = BitmapFactory.decodeFile(file.getAbsolutePath());
            if (bm != null) {
                Drawable d = new BitmapDrawable(context.getResources(), bm);

                DisplayMetrics dm = context.getResources().getDisplayMetrics();
                d.setBounds(0, 0, Math.round(bm.getWidth() * dm.density), Math.round(bm.getHeight() * dm.density));

                return d;
            }
        }

        return null;
    }

    static class AnnotatedSource {
        private String source;
        private int width = 0;
        private int height = 0;
        private boolean tracking = false;

        // Encapsulate some ugliness

        AnnotatedSource(String source) {
            this.source = source;

            if (source != null && source.endsWith("###")) {
                int pos = source.substring(0, source.length() - 3).lastIndexOf("###");
                if (pos > 0) {
                    int x = source.indexOf("x", pos + 3);
                    int s = source.indexOf(":", pos + 3);
                    if (x > 0 && s > x)
                        try {
                            this.width = Integer.parseInt(source.substring(pos + 3, x));
                            this.height = Integer.parseInt(source.substring(x + 1, s));
                            this.tracking = Boolean.parseBoolean(source.substring(s + 1, source.length() - 3));
                            this.source = source.substring(0, pos);
                        } catch (NumberFormatException ex) {
                            Log.e(ex);
                        }
                }
            }
        }

        AnnotatedSource(String source, int width, int height, boolean tracking) {
            this.source = source;
            this.width = width;
            this.height = height;
            this.tracking = tracking;
        }

        public String getSource() {
            return this.source;
        }

        String getAnnotated() {
            return (width == 0 && height == 0
                    ? source
                    : source + "###" + width + "x" + height + ":" + tracking + "###");
        }
    }
}
