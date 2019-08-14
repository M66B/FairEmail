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

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.preference.PreferenceManager;

public class ViewCardOptional extends CardView {
    public ViewCardOptional(@NonNull Context context) {
        super(context);
    }

    public ViewCardOptional(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ViewCardOptional(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean cards = prefs.getBoolean("cards", true);
        boolean compact = prefs.getBoolean("compact", false);
        if (cards) {
            int dp6 = Helper.dp2pixels(getContext(), 6);
            int color = Helper.resolveColor(getContext(), R.attr.colorCardBackground);

            FrameLayout.LayoutParams lparam = (FrameLayout.LayoutParams) getLayoutParams();
            lparam.setMargins(dp6, compact ? 0 : dp6, dp6, dp6);
            setLayoutParams(lparam);

            setRadius(dp6);
            setElevation(compact ? dp6 / 2f : dp6);
            setCardBackgroundColor(color);

            getChildAt(0).setPadding(dp6, dp6, dp6, dp6);
        } else {
            setRadius(0);
            setElevation(0);
            setCardBackgroundColor(Color.TRANSPARENT);
        }
    }
}
