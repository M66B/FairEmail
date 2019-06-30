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
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.android.colorpicker.ColorPickerDialog;

import static android.app.Activity.RESULT_OK;

public class FragmentDialogColor extends ColorPickerDialog {
    private Bundle args;

    public void initialize(int title, int color, Bundle args, Context context) {
        this.args = args;
        int[] colors = context.getResources().getIntArray(R.array.colorPicker);
        super.initialize(title, colors, color, 4, colors.length);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null)
            this.args = savedInstanceState.getBundle("fair:extra");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBundle("fair:extra", args);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onColorSelected(int color) {
        Fragment target = getTargetFragment();
        if (target != null) {
            args.putInt("color", color);

            Intent data = new Intent();
            data.putExtra("args", args);
            target.onActivityResult(getTargetRequestCode(), RESULT_OK, data);
        }

        dismiss();
    }
}
