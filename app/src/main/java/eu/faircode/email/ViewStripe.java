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
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.preference.PreferenceManager;

public class ViewStripe extends CardView {
    public ViewStripe(@NonNull Context context) {
        super(context);
    }

    public ViewStripe(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ViewStripe(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean circular = prefs.getBoolean("circular", true);
        setRadius(circular ? Helper.dp2pixels(getContext(), 3) / 2f : 0f);
        setElevation(0);
    }

    @Override
    public void setBackgroundColor(int color) {
        setCardBackgroundColor(color);
    }
}
