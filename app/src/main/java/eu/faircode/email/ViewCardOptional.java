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
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.preference.PreferenceManager;

public class ViewCardOptional extends CardView {
    private boolean cards;
    private boolean compact;
    private int padding;
    private int margin;
    private int ident;
    private Integer color = null;

    public ViewCardOptional(@NonNull Context context) {
        super(context);
        init(context);
    }

    public ViewCardOptional(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ViewCardOptional(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        cards = prefs.getBoolean("cards", true);
        compact = prefs.getBoolean("compact", false);
        padding = prefs.getInt("view_padding", compact || !cards ? 0 : 1);

        margin = Helper.dp2pixels(context, (padding + (cards ? 1 : 0)) * 3);

        setRadius(cards ? margin : 0);
        setCardElevation(0);
        setCardBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    protected void onAttachedToWindow() {
        if (cards) {
            ViewGroup.MarginLayoutParams lparam = (ViewGroup.MarginLayoutParams) getLayoutParams();
            lparam.setMargins(margin, margin, margin, margin);
            setLayoutParams(lparam);
        }

        setContentPadding(margin, margin, margin, margin);

        super.onAttachedToWindow();
    }

    @Override
    public void setCardBackgroundColor(int color) {
        if (this.color == null || this.color != color) {
            this.color = color;

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            boolean cards = prefs.getBoolean("cards", true);
            if (cards && color == Color.TRANSPARENT)
                color = Helper.resolveColor(getContext(), R.attr.colorCardBackground);

            super.setCardBackgroundColor(color);
        }
    }
}
