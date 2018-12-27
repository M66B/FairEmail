package eu.faircode.email;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ContentLoadingProgressBar extends ProgressBar {
    public ContentLoadingProgressBar(@NonNull Context context) {
        this(context, null);
    }

    public ContentLoadingProgressBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, 0);
    }

    @Override
    public void setVisibility(int visibility) {
        removeCallbacks(delayedShow);
        if (visibility == VISIBLE) {
            super.setVisibility(INVISIBLE);
            postDelayed(delayedShow, 500);
        } else
            super.setVisibility(visibility);
    }

    private final Runnable delayedShow = new Runnable() {
        @Override
        public void run() {
            ContentLoadingProgressBar.super.setVisibility(VISIBLE);
        }
    };
}
