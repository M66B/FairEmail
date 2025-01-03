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

    Copyright 2018-2025 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.ColorUtils;

public class ViewButtonColor extends AppCompatButton {
    private int color = Color.TRANSPARENT;
    private boolean circle = false;
    private int colorSeparator;

    public ViewButtonColor(Context context) {
        super(context);
        init(context);
    }

    public ViewButtonColor(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ViewButtonColor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.colorSeparator = Helper.resolveColor(context, R.attr.colorSeparator);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState, this.color, this.circle);
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        setColor(savedState.getColor(), savedState.getCircle());
    }

    void setColor(Integer color) {
        setColor(color, false);
    }

    void setColor(Integer color, boolean circle) {
        if (color == null)
            color = Color.TRANSPARENT;
        this.color = color;
        this.circle = circle;

        if (circle) {
            ShapeDrawable shape = new ShapeDrawable(new OvalShape());
            shape.setColorFilter(color == Color.TRANSPARENT ? colorSeparator : color, PorterDuff.Mode.SRC_ATOP);
            setBackground(shape);
        } else {
            GradientDrawable background = new GradientDrawable();
            background.setColor(color);
            background.setStroke(
                    Helper.dp2pixels(getContext(), 1),
                    Helper.resolveColor(getContext(), R.attr.colorSeparator));
            setBackground(background);

            if (color == Color.TRANSPARENT)
                setTextColor(Helper.resolveColor(getContext(), android.R.attr.textColorPrimary));
            else {
                double lum = ColorUtils.calculateLuminance(color);
                setTextColor(lum < 0.5 ? Color.WHITE : Color.BLACK);
            }
        }
    }

    int getColor() {
        return this.color;
    }

    static class SavedState extends View.BaseSavedState {
        private int color;
        private boolean circle;

        private SavedState(Parcelable superState, int color, boolean circle) {
            super(superState);
            this.color = color;
            this.circle = circle;
        }

        private SavedState(Parcel in) {
            super(in);
            this.color = in.readInt();
            this.circle = (in.readInt() != 0);
        }

        public int getColor() {
            return this.color;
        }

        public boolean getCircle() {
            return this.circle;
        }

        @Override
        public void writeToParcel(Parcel destination, int flags) {
            super.writeToParcel(destination, flags);
            destination.writeInt(color);
            destination.writeInt(circle ? 1 : 0);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
