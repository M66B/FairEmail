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
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

public class ToolbarEx extends Toolbar {
    public ToolbarEx(@NonNull Context context) {
        super(context);
    }

    public ToolbarEx(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ToolbarEx(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(null);
        TextView tv = findViewById(R.id.title);
        tv.setText(title);
        tv.setVisibility(title == null ? GONE : VISIBLE);
        findViewById(R.id.count).setVisibility(GONE);
    }

    @Override
    public void setSubtitle(CharSequence subtitle) {
        super.setSubtitle(null);
        TextView tv = findViewById(R.id.subtitle);
        tv.setText(subtitle);
        tv.setVisibility(subtitle == null ? GONE : VISIBLE);
        findViewById(R.id.count).setVisibility(GONE);
    }
}
