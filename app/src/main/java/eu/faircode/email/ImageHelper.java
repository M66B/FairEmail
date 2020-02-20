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
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
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
import androidx.exifinterface.media.ExifInterface;
import androidx.preference.PreferenceManager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

class ImageHelper {
    private static final ExecutorService executor =
            Helper.getBackgroundExecutor(1, "image");

    private static final int DOWNLOAD_TIMEOUT = 15 * 1000; // milliseconds
    private static final int MAX_REDIRECTS = 10;
    private static final long FIT_DRAWABLE_WARNING = 10 * 1000L; // milliseconds
    private static final long FIT_DRAWABLE_TIMEOUT = 20 * 1000L; // milliseconds

    static Bitmap generateIdenticon(@NonNull String email, int size, int pixels, Context context) {
        byte[] hash = getHash(email);
        float h = Math.abs(email.hashCode()) % 360;
        return generateIdenticon(hash, h, size, pixels, context);
    }

    static Bitmap generateIdenticon(byte[] hash, float h, int size, int pixels, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int s = prefs.getInt("saturation", 100);
        int v = prefs.getInt("brightness", 100);

        int bg = Color.HSVToColor(new float[]{h, s / 100f, v / 100f});

        Paint paint = new Paint();
        paint.setColor(bg);

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

    static Bitmap generateLetterIcon(@NonNull String email, String name, int size, Context context) {
        if (TextUtils.isEmpty(name))
            name = email;

        String letter = null;
        int len = name.length();
        for (int i = 0; i < len; i++) {
            char kar = name.charAt(i);
            if (Character.isLetter(kar)) {
                letter = name.substring(i, i + 1).toUpperCase();
                break;
            }
        }
        if (letter == null)
            letter = (len > 0 ? name.substring(0, 1) : "?");

        float h = Math.abs(email.hashCode()) % 360f;
        return generateLetterIcon(letter, h, size, context);
    }

    static Bitmap generateLetterIcon(String letter, float h, int size, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        float s = prefs.getInt("saturation", 100) / 100f;
        float v = prefs.getInt("brightness", 100) / 100f;
        float t = prefs.getInt("threshold", 50) / 100f;

        int bg = Color.HSVToColor(new float[]{h, s, v});
        double lum = ColorUtils.calculateLuminance(bg);

        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(bg);

        Paint paint = new Paint();
        paint.setColor(lum < t ? Color.WHITE : Color.BLACK);
        paint.setTextSize(size / 2f);
        paint.setTypeface(Typeface.DEFAULT_BOLD);

        canvas.drawText(letter,
                size / 2f - paint.measureText(letter) / 2,
                size / 2f - (paint.descent() + paint.ascent()) / 2, paint);

        return bitmap;
    }

    static byte[] getHash(String email) {
        try {
            return MessageDigest.getInstance("MD5").digest(email.getBytes());
        } catch (NoSuchAlgorithmException ignored) {
            return email.getBytes();
        }
    }

    static Bitmap makeCircular(Bitmap bitmap, Integer radius) {
        if (bitmap == null)
            return null;

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Rect source;
        if (w > h) {
            int off = (w - h) / 2;
            source = new Rect(off, 0, w - off, h);
        } else if (w < h) {
            int off = (h - w) / 2;
            source = new Rect(0, off, w, h - off);
        } else
            source = new Rect(0, 0, w, h);

        Rect dest = new Rect(0, 0, source.width(), source.height());

        Bitmap round = Bitmap.createBitmap(source.width(), source.height(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(round);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(Color.GRAY);
        if (radius == null)
            canvas.drawOval(new RectF(dest), paint); // round
        else
            canvas.drawRoundRect(new RectF(dest), radius, radius, paint); // rounded
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, source, dest, paint);

        bitmap.recycle();
        return round;
    }

    static Drawable decodeImage(final Context context, final long id, String source, boolean show, int zoom, final TextView view) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
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

            Log.d("Image show=" + show + " inline=" + inline +
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
                        Bitmap bm = ImageHelper.decodeImage(attachment.getFile(context), scaleToPixels);
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

            // Check cache
            Drawable cached = getCachedImage(context, id, a.source);
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
                        // Check cache again
                        Drawable cached = getCachedImage(context, id, a.source);
                        if (cached != null) {
                            fitDrawable(cached, a, view);
                            post(cached, a.source);
                            return;
                        }

                        // Download image
                        Drawable d = downloadImage(context, id, a.source);
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

                            view.requestLayout();
                            view.invalidate();
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

    private static void fitDrawable(final Drawable d, final AnnotatedSource a, final View view) {
        Semaphore semaphore = new Semaphore(0);

        long start = new Date().getTime();

        view.post(new Runnable() {
            @Override
            public void run() {
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
            if (semaphore.tryAcquire(FIT_DRAWABLE_TIMEOUT, TimeUnit.MILLISECONDS)) {
                long elapsed = new Date().getTime() - start;
                if (elapsed > FIT_DRAWABLE_WARNING)
                    Log.i("fitDrawable failed elapsed=" + elapsed);
            } else
                Log.i("fitDrawable failed timeout=" + FIT_DRAWABLE_TIMEOUT);
        } catch (InterruptedException ex) {
            Log.w(ex);
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

    private static Drawable getCachedImage(Context context, long id, String source) {
        File file = getCacheFile(context, id, source);
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

    @NonNull
    private static Drawable downloadImage(Context context, long id, String source) throws IOException {
        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();

        Bitmap bm;
        HttpURLConnection urlConnection = null;
        try {
            int redirects = 0;
            URL url = new URL(source);
            while (true) {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoOutput(false);
                urlConnection.setReadTimeout(DOWNLOAD_TIMEOUT);
                urlConnection.setConnectTimeout(DOWNLOAD_TIMEOUT);
                urlConnection.setInstanceFollowRedirects(true);
                urlConnection.connect();

                int status = urlConnection.getResponseCode();

                if (status == HttpURLConnection.HTTP_MOVED_PERM ||
                        status == HttpURLConnection.HTTP_MOVED_TEMP ||
                        status == HttpURLConnection.HTTP_SEE_OTHER ||
                        status == 307 /* Temporary redirect */ ||
                        status == 308 /* Permanent redirect */) {
                    if (++redirects > MAX_REDIRECTS)
                        throw new IOException("Too many redirects");

                    String header = urlConnection.getHeaderField("Location");
                    if (header == null)
                        throw new IOException("Location header missing");

                    String location = URLDecoder.decode(header, StandardCharsets.UTF_8.name());
                    url = new URL(url, location);
                    Log.i("Redirect #" + redirects + " to " + url);

                    urlConnection.disconnect();
                    continue;
                }

                if (status != HttpURLConnection.HTTP_OK)
                    throw new IOException("HTTP status=" + status);

                break;
            }

            BufferedInputStream is = new BufferedInputStream(urlConnection.getInputStream());

            Log.i("Probe " + source);
            is.mark(64 * 1024);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, options);

            int scaleToPixels = dm.widthPixels;
            int factor = 1;
            while (options.outWidth / factor > scaleToPixels)
                factor *= 2;

            Log.i("Download " + source + " factor=" + factor);
            is.reset();
            if (factor > 1) {
                options.inJustDecodeBounds = false;
                options.inSampleSize = factor;
                bm = BitmapFactory.decodeStream(is, null, options);
            } else
                bm = BitmapFactory.decodeStream(is);
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }

        if (bm == null)
            throw new FileNotFoundException("Download image failed source=" + source);

        Log.i("Downloaded image source=" + source);

        File file = getCacheFile(context, id, source);
        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
            bm.compress(Bitmap.CompressFormat.PNG, 90, os);
        }

        Drawable d = new BitmapDrawable(res, bm);
        d.setBounds(0, 0, Math.round(bm.getWidth() * dm.density), Math.round(bm.getHeight() * dm.density));
        return d;
    }

    @NonNull
    private static File getCacheFile(Context context, long id, String source) {
        File dir = new File(context.getCacheDir(), "images");
        if (!dir.exists())
            dir.mkdir();
        return new File(dir, id + "_" + Math.abs(source.hashCode()) + ".png");
    }

    static Bitmap decodeImage(File file, int scaleToPixels) {
        try {
            return _decodeImage(file, scaleToPixels);
        } catch (OutOfMemoryError ex) {
            Log.e(ex);
            return null;
        }
    }

    private static Bitmap _decodeImage(File file, int scaleToPixels) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);

        int factor = 1;
        while (options.outWidth / factor > scaleToPixels)
            factor *= 2;

        Matrix rotation = null;
        try {
            rotation = getImageRotation(file);
        } catch (IOException ex) {
            Log.w(ex);
        }

        if (factor > 1 || rotation != null) {
            Log.i("Decode image factor=" + factor);
            options.inJustDecodeBounds = false;
            options.inSampleSize = factor;
            Bitmap scaled = BitmapFactory.decodeFile(file.getAbsolutePath(), options);

            if (scaled != null && rotation != null) {
                Bitmap rotated = Bitmap.createBitmap(scaled, 0, 0, scaled.getWidth(), scaled.getHeight(), rotation, true);
                scaled.recycle();
                scaled = rotated;
            }

            return scaled;
        }

        return BitmapFactory.decodeFile(file.getAbsolutePath());
    }

    static Matrix getImageRotation(File file) throws IOException {
        ExifInterface exif = new ExifInterface(file.getAbsolutePath());
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return null;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                return matrix;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                return matrix;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                return matrix;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                return matrix;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                return matrix;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                return matrix;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                return matrix;
            default:
                return null;
        }
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
