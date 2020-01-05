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
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.preference.PreferenceManager;

public class ViewCardOptional extends CardView {
    private boolean cards;
    private boolean indentation;
    private boolean compact;
    private boolean threading;
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
        indentation = prefs.getBoolean("indentation", false);
        compact = prefs.getBoolean("compact", false);
        threading = prefs.getBoolean("threading", true);

        margin = Helper.dp2pixels(context, compact ? 3 : 6);
        ident = Helper.dp2pixels(context, 12 + (compact ? 3 : 6));

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
            setContentPadding(margin, margin, margin, margin);
        }

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

    public void setOutgoing(boolean outgoing) {
        if (cards && threading && indentation) {
            ViewGroup.MarginLayoutParams lparam = (ViewGroup.MarginLayoutParams) getLayoutParams();
            lparam.setMarginStart(outgoing ? ident : margin);
            lparam.setMarginEnd(outgoing ? margin : ident);
            setLayoutParams(lparam);
        }
    }
}
