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
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;

import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FragmentDialogEditImage extends FragmentDialogBase {
    private int resize = 0;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();

        if (savedInstanceState != null)
            resize = savedInstanceState.getInt("fair:resize", 0);

        final int[] resizeValues = getResources().getIntArray(R.array.resizeValues);

        Context context = getContext();
        View dview = LayoutInflater.from(context).inflate(R.layout.dialog_edit_image, null);
        ImageButton ibRotate = dview.findViewById(R.id.ibRotate);
        ImageButton ibFlipHorizontally = dview.findViewById(R.id.ibFlipHorizontally);
        ImageButton ibFlipVertically = dview.findViewById(R.id.ibFlipVertically);
        ImageButton ibResize = dview.findViewById(R.id.ibResize);
        ImageButton ibCancel = dview.findViewById(R.id.ibCancel);
        ImageButton ibSave = dview.findViewById(R.id.ibSave);
        CropImageView civ = dview.findViewById(R.id.civ);
        ProgressBar pbWait = dview.findViewById(R.id.pbWait);

        Dialog dialog = new AlertDialog.Builder(context)
                .setView(dview)
                .create();

        ibRotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                civ.rotateImage(90);
            }
        });

        ibFlipHorizontally.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                civ.flipImageHorizontally();
            }
        });

        ibFlipVertically.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                civ.flipImageVertically();
            }
        });

        ibResize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(context, FragmentDialogEditImage.this, v);

                Size expected = civ.expectedImageSize();
                int s = (expected == null
                        ? Integer.MAX_VALUE
                        : Math.max(expected.getWidth(), expected.getHeight()));

                SpannableStringBuilder ssb = new SpannableStringBuilderEx("---");
                if (resize == 0)
                    ssb.setSpan(new StyleSpan(Typeface.BOLD), 0, ssb.length(), 0);
                popupMenu.getMenu().add(Menu.NONE, 1, 1, ssb)
                        .setIntent(new Intent().putExtra("size", 0));

                int order = 1;
                for (int size : resizeValues) {
                    order++;
                    ssb = new SpannableStringBuilderEx(getString(R.string.title_add_resize_pixels, size));
                    if (size == resize)
                        ssb.setSpan(new StyleSpan(Typeface.BOLD), 0, ssb.length(), 0);
                    popupMenu.getMenu().add(Menu.NONE, order, order, ssb)
                            .setEnabled(size < s)
                            .setIntent(new Intent().putExtra("size", size));
                }

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        Intent intent = item.getIntent();
                        if (intent == null)
                            return false;

                        resize = intent.getIntExtra("size", 0);
                        return true;
                    }
                });

                popupMenu.show();

            }
        });

        ibCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        ibSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SimpleTask<Void>() {
                    @Override
                    protected void onPreExecute(Bundle args) {
                        ibSave.setEnabled(true);
                        pbWait.setVisibility(View.VISIBLE);
                    }

                    @Override
                    protected void onPostExecute(Bundle args) {
                        ibSave.setEnabled(true);
                        pbWait.setVisibility(View.GONE);
                    }

                    @Override
                    protected Void onExecute(Context context, Bundle args) throws Throwable {
                        long id = args.getLong("id");

                        DB db = DB.getInstance(context);
                        EntityAttachment attachment = db.attachment().getAttachment(id);
                        if (attachment == null)
                            return null;

                        Bitmap bm = civ.getCroppedImage(resize, resize, CropImageView.RequestSizeOptions.RESIZE_INSIDE);
                        if (bm == null)
                            return null;

                        File file = attachment.getFile(context);

                        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
                            bm.compress(Bitmap.CompressFormat.PNG, ImageHelper.DEFAULT_PNG_COMPRESSION, os);
                        }

                        long len = file.length();

                        if (attachment.name != null) {
                            int index = attachment.name.lastIndexOf(".");
                            if (index > 0) {
                                String ext = attachment.name.substring(index + 1);
                                if (!TextUtils.isEmpty(ext) && !ext.equalsIgnoreCase("png")) {
                                    File old = attachment.getFile(context);
                                    attachment.name = attachment.name.substring(0, index) + ".png";
                                    if (!old.renameTo(attachment.getFile(context)))
                                        throw new IOException("Error renaming " + file.getAbsolutePath());
                                }
                            }
                        }

                        db.attachment().setName(id, attachment.name, "image/png", len);

                        return null;
                    }

                    @Override
                    protected void onExecuted(Bundle args, Void data) {
                        sendResult(RESULT_OK);
                        dismiss();
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Log.unexpectedError(getParentFragment(), ex);
                    }
                }.execute(FragmentDialogEditImage.this, args, "save:image");
            }
        });

        CropImageOptions options = new CropImageOptions();
        options.borderCornerThickness = Helper.dp2pixels(context, 5);
        civ.setImageCropOptions(options);

        new SimpleTask<EntityAttachment>() {
            @Override
            protected void onPreExecute(Bundle args) {
                pbWait.setVisibility(View.VISIBLE);
                ibSave.setEnabled(false);
            }

            @Override
            protected void onPostExecute(Bundle args) {
                pbWait.setVisibility(View.GONE);
                ibSave.setEnabled(true);
            }

            @Override
            protected EntityAttachment onExecute(Context context, Bundle args) {
                long id = args.getLong("id");

                DB db = DB.getInstance(context);
                return db.attachment().getAttachment(id);
            }

            @Override
            protected void onExecuted(Bundle args, EntityAttachment attachment) {
                if (attachment == null)
                    return;

                civ.setImageUriAsync(attachment.getUri(context));
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "edit:image");

        return dialog;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt("fair:resize", resize);
        super.onSaveInstanceState(outState);
    }
}
