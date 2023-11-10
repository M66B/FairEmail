package eu.faircode.email;

import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.text.style.UpdateAppearance;

public class HighlightSpan extends CharacterStyle implements UpdateAppearance {
    private int color;

    public HighlightSpan(int color) {
        this.color = color;
    }

    @Override
    public void updateDrawState(TextPaint tp) {
        tp.bgColor = color;
    }
}
