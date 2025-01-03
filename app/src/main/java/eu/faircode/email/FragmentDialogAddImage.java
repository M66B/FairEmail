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

import static android.app.Activity.RESULT_OK;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;

public class FragmentDialogAddImage extends FragmentDialogBase {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        int title = getArguments().getInt("title");

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean add_inline = prefs.getBoolean("add_inline", true);
        boolean resize_images = prefs.getBoolean("resize_images", true);
        int resize = prefs.getInt("resize", FragmentCompose.REDUCED_IMAGE_SIZE);
        boolean privacy_images = prefs.getBoolean("privacy_images", false);
        boolean image_dialog = prefs.getBoolean("image_dialog", true);

        final ViewGroup dview = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_image, null);
        final ImageView ivType = dview.findViewById(R.id.ivType);
        final RadioGroup rgAction = dview.findViewById(R.id.rgAction);
        final ImageButton ibSettings = dview.findViewById(R.id.ibSettings);
        final CheckBox cbResize = dview.findViewById(R.id.cbResize);
        final ImageButton ibResize = dview.findViewById(R.id.ibResize);
        final Spinner spResize = dview.findViewById(R.id.spResize);
        final TextView tvResize = dview.findViewById(R.id.tvResize);
        final TextView tvResizeRemark = dview.findViewById(R.id.tvResizeRemark);
        final CheckBox cbPrivacy = dview.findViewById(R.id.cbPrivacy);
        final CheckBox cbNotAgain = dview.findViewById(R.id.cbNotAgain);
        final TextView tvNotAgain = dview.findViewById(R.id.tvNotAgain);

        ivType.setImageResource(title == R.string.title_attachment_photo
                ? R.drawable.twotone_photo_camera_24 : R.drawable.twotone_image_24);
        rgAction.check(add_inline ? R.id.rbInline : R.id.rbAttach);
        cbResize.setChecked(resize_images);
        spResize.setEnabled(resize_images);
        tvResizeRemark.setText(getString(R.string.title_add_image_resize_remark, "JPEG, PNG, WebP"));
        cbPrivacy.setChecked(privacy_images);

        final int[] resizeValues = getResources().getIntArray(R.array.resizeValues);
        for (int pos = 0; pos < resizeValues.length; pos++)
            if (resizeValues[pos] == resize) {
                spResize.setSelection(pos);
                tvResize.setText(getString(R.string.title_add_resize_pixels, resizeValues[pos]));
                break;
            }

        rgAction.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                prefs.edit().putBoolean("add_inline", checkedId == R.id.rbInline).apply();
            }
        });

        // https://developer.android.com/reference/android/provider/MediaStore#ACTION_PICK_IMAGES_SETTINGS
        PackageManager pm = getContext().getPackageManager();
        Intent settings = new Intent(MediaStore.ACTION_PICK_IMAGES_SETTINGS);
        ibSettings.setVisibility(settings.resolveActivity(pm) == null ? View.GONE : View.VISIBLE);
        ibSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.getContext().startActivity(settings);
            }
        });

        cbResize.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("resize_images", isChecked).apply();
                spResize.setEnabled(isChecked);
            }
        });

        ibResize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 63);
            }
        });

        spResize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                prefs.edit().putInt("resize", resizeValues[position]).apply();
                tvResize.setText(getString(R.string.title_add_resize_pixels, resizeValues[position]));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                prefs.edit().remove("resize").apply();
            }
        });

        cbPrivacy.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("privacy_images", isChecked).apply();
            }
        });

        cbNotAgain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("image_dialog", !isChecked).apply();
                tvNotAgain.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });

        cbNotAgain.setChecked(!image_dialog);
        tvNotAgain.setVisibility(cbNotAgain.isChecked() ? View.VISIBLE : View.GONE);

        return new AlertDialog.Builder(getContext())
                .setView(dview)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(title,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sendResult(RESULT_OK);
                            }
                        })
                .create();
    }
}
