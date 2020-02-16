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
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorChangedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.lang.reflect.Field;

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

        ColorPickerDialogBuilder builder = ColorPickerDialogBuilder
                .with(getContext())
                .setTitle(title)
                .showColorEdit(true)
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

        Dialog dialog = builder.build();

        try {
            Field fColorEdit = builder.getClass().getDeclaredField("colorEdit");
            fColorEdit.setAccessible(true);
            EditText colorEdit = (EditText) fColorEdit.get(builder);
            colorEdit.setTextColor(Helper.resolveColor(getContext(), android.R.attr.textColorPrimary));
        } catch (Throwable ex) {
            Log.w(ex);
        }

        return dialog;
    }
}
