package eu.faircode.email;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.style.ReplacementSpan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ChipSpan extends ReplacementSpan {
    private int bg;
    private int fg;
    private int radius;

    public ChipSpan(int bg, int fg, int radius) {
        super();
        this.bg = bg;
        this.fg = fg;
        this.radius = radius;
    }

    @Override
    public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, @Nullable Paint.FontMetricsInt fm) {
        return Math.round(paint.measureText(text, start, end) + 2 * radius);
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, @NonNull Paint paint) {
        RectF rect = new RectF(x, top, x + paint.measureText(text, start, end) + 2 * radius, bottom);
        paint.setColor(bg);
        canvas.drawRoundRect(rect, radius, radius, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(fg);
        canvas.drawText(text, start, end, x + radius, y, paint);
    }
}
