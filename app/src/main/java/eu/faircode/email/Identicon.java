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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class Identicon {
    private static final float MIN_LUMINANCE = 0.33f;

    static Bitmap icon(@NonNull String email, int size, int pixels, boolean dark) {
        byte[] hash = getHash(email);

        int color = Color.argb(255, hash[0], hash[1], hash[2]);
        color = ColorUtils.blendARGB(color,
                dark ? Color.WHITE : Color.BLACK, MIN_LUMINANCE);

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

    static Bitmap letter(@NonNull String email, int size, boolean dark) {
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
        color = ColorUtils.blendARGB(color, dark ? Color.WHITE : Color.BLACK, MIN_LUMINANCE);

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
}
