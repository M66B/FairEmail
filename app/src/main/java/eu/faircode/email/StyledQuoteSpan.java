package eu.faircode.email;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Layout;
import android.text.style.QuoteSpan;

import androidx.annotation.NonNull;

public class StyledQuoteSpan extends QuoteSpan {
    StyledQuoteSpan(int color) {
        super(color);
    }

    @Override
    public int getLeadingMargin(boolean first) {
        return 6 /* stripeWidth */ + 12 /* gapWidth */;
    }

    @Override
    public void drawLeadingMargin(
            @NonNull Canvas c, @NonNull Paint p,
            int x, int dir, int top, int baseline, int bottom,
            @NonNull CharSequence text, int start, int end, boolean first,
            @NonNull Layout layout) {
        Paint.Style style = p.getStyle();
        int color = p.getColor();

        p.setStyle(Paint.Style.FILL);
        p.setColor(getColor());

        c.drawRect(x, top, x + dir * 6 /* stripeWidth */, bottom, p);

        p.setStyle(style);
        p.setColor(color);
    }
}
