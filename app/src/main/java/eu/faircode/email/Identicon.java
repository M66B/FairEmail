package eu.faircode.email;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import androidx.core.graphics.ColorUtils;

class Identicon {
    static Bitmap generate(String email, int size, int pixels, boolean dark) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(email.getBytes());
        } catch (NoSuchAlgorithmException ignored) {
            hash = email.getBytes();
        }

        int color = Color.argb(255, hash[0], hash[1], hash[2]);
        color = ColorUtils.blendARGB(color, dark ? Color.BLACK : Color.WHITE, 0.2f);

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
}
