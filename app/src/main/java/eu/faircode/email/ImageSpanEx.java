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

import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;

import androidx.annotation.NonNull;

import org.jsoup.nodes.Element;

public class ImageSpanEx extends ImageSpan {
    private final int width;
    private final int height;
    private final String tracking;

    public ImageSpanEx(@NonNull Drawable drawable, @NonNull Element img) {
        super(drawable, img.attr("src"));

        int _width = 0;
        int _height = 0;

        // Relative sizes (%) = use image size

        String awidth = img.attr("width").replace(" ", "");
        for (int i = 0; i < awidth.length(); i++)
            if (Character.isDigit(awidth.charAt(i)))
                _width = _width * 10 + (byte) awidth.charAt(i) - (byte) '0';
            else {
                _width = 0;
                break;
            }

        String aheight = img.attr("height").replace(" ", "");
        for (int i = 0; i < aheight.length(); i++)
            if (Character.isDigit(aheight.charAt(i)))
                _height = _height * 10 + (byte) aheight.charAt(i) - (byte) '0';
            else {
                _height = 0;
                break;
            }

        this.width = _width;
        this.height = _height;
        this.tracking = img.attr("x-tracking");
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public String getTracking() {
        return this.tracking;
    }
}
