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

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorChangedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import static android.app.Activity.RESULT_OK;

public class FragmentDialogColor extends FragmentDialogBase {
    private int color;

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt("fair:color", color);
        super.onSaveInstanceState(outState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        color = (savedInstanceState == null
                ? args.getInt("color")
                : savedInstanceState.getInt("fair:color"));
        String title = args.getString("title");
        boolean reset = args.getBoolean("reset", false);

        Context context = getContext();
        int editTextColor = Helper.resolveColor(context, android.R.attr.editTextColor);

        ColorPickerDialogBuilder builder = ColorPickerDialogBuilder
                .with(context)
                .setTitle(title)
                .showColorEdit(true)
                .setColorEditTextColor(editTextColor)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(6)
                .lightnessSliderOnly()
                .setOnColorChangedListener(new OnColorChangedListener() {
                    @Override
                    public void onColorChanged(int selectedColor) {
                        color = selectedColor;
                    }
                })
                .setPositiveButton(android.R.string.ok, new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                        getArguments().putInt("color", selectedColor);
                        sendResult(RESULT_OK);
                    }
                });

        if (color != Color.TRANSPARENT)
            builder.initialColor(color);

        if (reset)
            builder.setNegativeButton(R.string.title_reset, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    getArguments().putInt("color", Color.TRANSPARENT);
                    sendResult(RESULT_OK);
                }
            });

        return builder.build();
    }
}
