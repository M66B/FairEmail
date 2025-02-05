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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfRenderer;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Pair;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AdapterMedia extends RecyclerView.Adapter<AdapterMedia.ViewHolder> {
    private Fragment parentFragment;
    private final Context context;
    private final LayoutInflater inflater;
    private final LifecycleOwner owner;
    private final int textColorTertiary;
    private final int textColorLink;

    private List<EntityAttachment> items = new ArrayList<>();

    private static final int PDF_WIDTH = 120;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private final View view;
        private final ImageView ivImage;
        private final TextView tvCaption;
        private final TextView tvProperties;
        private final TextView tvContent;

        ViewHolder(View itemView) {
            super(itemView);

            view = itemView.findViewById(R.id.clItem);
            ivImage = itemView.findViewById(R.id.ivImage);
            tvCaption = itemView.findViewById(R.id.tvCaption);
            tvProperties = itemView.findViewById(R.id.tvProperties);
            tvContent = itemView.findViewById(R.id.tvContent);
        }

        private void wire() {
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
            tvContent.setOnClickListener(this);
        }

        private void unwire() {
            view.setOnClickListener(null);
            view.setOnLongClickListener(null);
            tvContent.setOnClickListener(null);
        }

        private void showPlayerState(Uri uri) {
            if (MediaPlayerHelper.isPlaying(uri))
                ivImage.setImageResource(R.drawable.twotone_stop_48);
            else {
                ivImage.setImageResource(R.drawable.twotone_play_arrow_48);
                tvProperties.setVisibility(View.GONE);
            }
        }

        private void bindTo(EntityAttachment attachment) {
            tvCaption.setText(attachment.name);
            tvCaption.setVisibility(TextUtils.isEmpty(attachment.name) ? View.GONE : View.VISIBLE);
            tvProperties.setVisibility(View.GONE);
            tvContent.setVisibility(View.GONE);

            if (attachment.available) {
                Bundle args = new Bundle();
                args.putSerializable("file", attachment.getFile(context));
                args.putString("type", attachment.getMimeType());
                args.putInt("max", context.getResources().getDisplayMetrics().widthPixels);

                new SimpleTask<Drawable>() {
                    @Override
                    protected void onPreExecute(Bundle args) {
                        ivImage.setImageResource(R.drawable.twotone_hourglass_top_24);
                    }

                    @Override
                    protected Drawable onExecute(Context context, Bundle args) {
                        File file = (File) args.getSerializable("file");
                        String type = args.getString("type");
                        int max = args.getInt("max");

                        args.putLong("size", file.length());

                        if (type != null &&
                                (type.startsWith("audio/") || type.startsWith("video")))
                            // https://developer.android.com/reference/android/media/MediaMetadataRetriever
                            try (MediaMetadataRetriever ret = new MediaMetadataRetriever()) {
                                ret.setDataSource(file.getAbsolutePath());

                                String value = ret.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                                Integer duration = Helper.parseInt(value);
                                if (duration != null)
                                    args.putInt("duration", duration);
                            } catch (Throwable ex) {
                                Log.w(ex);
                            }

                        if ("application/pdf".equals(type)) {
                            // https://developer.android.com/reference/android/graphics/pdf/PdfRenderer
                            try (ParcelFileDescriptor pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)) {
                                try (PdfRenderer pdf = new PdfRenderer(pfd)) {
                                    try (PdfRenderer.Page page = pdf.openPage(0)) {
                                        int width = Helper.dp2pixels(context, PDF_WIDTH);
                                        int height = (int) ((float) width / page.getWidth() * page.getHeight());
                                        Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                                        Canvas canvas = new Canvas(bm);
                                        canvas.drawColor(Color.WHITE);
                                        page.render(bm, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                                        return new BitmapDrawable(context.getResources(), bm);
                                    }
                                }
                            } catch (Throwable ex) {
                                Log.w(ex);
                                return null;
                            }
                        } else if (type != null && type.startsWith("video/")) {
                            try {
                                // https://developer.android.com/reference/android/media/ThumbnailUtils
                                Bitmap bm;
                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
                                    bm = ThumbnailUtils.createVideoThumbnail(file.getAbsolutePath(), MediaStore.Images.Thumbnails.MINI_KIND);
                                else
                                    bm = ThumbnailUtils.createVideoThumbnail(file, new Size(max, max), null);
                                if (bm == null)
                                    throw new IllegalArgumentException("Thumbnail generation failed");
                                return new BitmapDrawable(context.getResources(), bm);
                            } catch (Throwable ex) {
                                Log.w(ex);
                                return context.getDrawable(R.drawable.twotone_ondemand_video_24);
                            }
                        } else {
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                            boolean barcode_preview = prefs.getBoolean("barcode_preview", true);

                            try {
                                BitmapFactory.Options options = new BitmapFactory.Options();
                                options.inJustDecodeBounds = true;
                                BitmapFactory.decodeFile(file.getAbsolutePath(), options);
                                args.putInt("width", options.outWidth);
                                args.putInt("height", options.outHeight);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    if (options.outColorSpace != null)
                                        args.putString("color", options.outColorSpace.getModel().name());
                                    if (options.outConfig != null)
                                        args.putString("config", options.outConfig.name());
                                }
                            } catch (Throwable ex) {
                                Log.w(ex);
                            }

                            // https://github.com/zxing/zxing/wiki/Frequently-Asked-Questions#developers
                            if (barcode_preview &&
                                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                                try (InputStream is = new FileInputStream(file)) {
                                    Bitmap bitmap = ImageHelper.getScaledBitmap(is, file.getAbsolutePath(), type, max);
                                    if (bitmap != null) {
                                        int width = bitmap.getWidth(), height = bitmap.getHeight();
                                        int[] pixels = new int[width * height];
                                        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

                                        RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
                                        BinaryBitmap bBitmap = new BinaryBitmap(new HybridBinarizer(source));
                                        MultiFormatReader reader = new MultiFormatReader();
                                        Result result = reader.decode(bBitmap);
                                        args.putString("barcode_text", Helper.getPrintableString(result.getText(), false));
                                        args.putString("barcode_format", result.getBarcodeFormat().name());
                                    }
                                } catch (NotFoundException ex) {
                                    Log.i(ex);
                                } catch (Throwable ex) {
                                    Log.e(ex);
                                }

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
                                    !"image/svg+xml".equalsIgnoreCase(type))
                                try {
                                    return ImageHelper.getScaledDrawable(context, file, type, max);
                                } catch (Throwable ex) {
                                    Log.w(ex);
                                }

                            Bitmap bm = ImageHelper.decodeImage(file, type, max);
                            if (bm == null)
                                return null;
                            return new BitmapDrawable(context.getResources(), bm);
                        }
                    }

                    @Override
                    protected void onExecuted(Bundle args, Drawable image) {
                        if (attachment.isAudio())
                            showPlayerState(attachment.getUri(context));
                        else if (image == null) {
                            String type = args.getString("type");
                            if ("application/pdf".equals(type))
                                ivImage.setImageResource(R.drawable.twotone_article_24);
                            else if (attachment.isVideo())
                                ivImage.setImageResource(R.drawable.twotone_ondemand_video_24);
                            else
                                ivImage.setImageResource(R.drawable.twotone_broken_image_24);
                        } else {
                            ivImage.setImageDrawable(image);
                            ImageHelper.animate(context, image);
                        }

                        StringBuilder sb = new StringBuilder();

                        int width = args.getInt("width");
                        int height = args.getInt("height");
                        if (width > 0 && height > 0)
                            sb.append(width)
                                    .append("\u00d7") // ×
                                    .append(height);

                        if (BuildConfig.DEBUG) {
                            String color = args.getString("color");
                            if (color != null) {
                                if (sb.length() > 0)
                                    sb.append(' ');
                                sb.append(color);
                            }

                            String config = args.getString("config");
                            if (config != null) {
                                if (sb.length() > 0)
                                    sb.append(' ');
                                sb.append(config);
                            }
                        }

                        long size = args.getLong("size");
                        if (size > 0) {
                            if (sb.length() > 0)
                                sb.append(' ');
                            sb.append(Helper.humanReadableByteCount(size));
                        }

                        int duration = args.getInt("duration");
                        if (duration > 0) {
                            if (sb.length() > 0)
                                sb.append(' ');
                            sb.append(Helper.formatDuration(duration));
                        }

                        if (BuildConfig.DEBUG) {
                            String barcode_format = args.getString("barcode_format");
                            if (!TextUtils.isEmpty(barcode_format)) {
                                if (sb.length() > 0)
                                    sb.append(' ');
                                sb.append(barcode_format);
                            }
                        }

                        if (sb.length() > 0) {
                            tvProperties.setText(sb);
                            tvProperties.setVisibility(View.VISIBLE);
                        }

                        String barcode_text = args.getString("barcode_text");
                        if (!TextUtils.isEmpty(barcode_text)) {
                            Uri uri;
                            try {
                                uri = UriHelper.guessScheme(Uri.parse(barcode_text));
                            } catch (Throwable ex) {
                                Log.w(ex);
                                uri = null;
                            }

                            boolean openable = (uri != null &&
                                    !TextUtils.isEmpty(uri.getScheme()) &&
                                    !"tel".equals(uri.getScheme()));

                            tvContent.setTypeface(null, openable ? Typeface.NORMAL : Typeface.BOLD);
                            int flags = tvContent.getPaintFlags();
                            if (openable)
                                flags |= Paint.UNDERLINE_TEXT_FLAG;
                            else
                                flags &= ~Paint.UNDERLINE_TEXT_FLAG;
                            tvContent.setPaintFlags(flags);
                            tvContent.setTextColor(openable ? textColorLink : textColorTertiary);
                            tvContent.setTag(openable ? uri.toString() : null);
                            tvContent.setText(barcode_text);
                            tvContent.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        tvCaption.setText(Log.formatThrowable(ex));
                        tvCaption.setVisibility(View.VISIBLE);
                        ivImage.setImageResource(R.drawable.twotone_warning_24);
                    }
                }.setKill(true).execute(context, owner, args, "media:load");
            } else
                ivImage.setImageResource(attachment.progress == null
                        ? R.drawable.twotone_image_24 : R.drawable.twotone_hourglass_top_24);
        }

        @Override
        public void onClick(View view) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return;

            if (view.getId() == R.id.tvContent && view.getTag() instanceof String) {
                Bundle args = new Bundle();
                args.putParcelable("uri", Uri.parse((String) view.getTag()));
                args.putString("title", ((TextView) view).getText().toString());
                args.putBoolean("always_confirm", true);

                FragmentDialogOpenLink fragment = new FragmentDialogOpenLink();
                fragment.setArguments(args);
                fragment.show(parentFragment.getParentFragmentManager(), "open:barcode");
                return;
            }

            EntityAttachment attachment = items.get(pos);
            if (attachment.available) {
                if (attachment.isAudio()) {
                    try {
                        Uri uri = attachment.getUri(context);
                        if (MediaPlayerHelper.isPlaying(uri))
                            MediaPlayerHelper.stopMusic(context);
                        else {
                            Runnable updatePosition = new RunnableEx("updatePosition") {
                                @Override
                                protected void delegate() {
                                    Pair<Integer, Integer> pos = MediaPlayerHelper.getPosition(uri);
                                    if (pos != null) {
                                        int at = (int) Math.round(pos.first / 1000.0) * 1000;
                                        tvProperties.setText(
                                                Helper.formatDuration(at, false) + " / " +
                                                        Helper.formatDuration(pos.second, true));
                                        view.postDelayed(this, 1000L);
                                    }
                                    tvProperties.setVisibility(pos == null ? View.GONE : View.VISIBLE);
                                }
                            };
                            view.postDelayed(updatePosition, 1000L);

                            MediaPlayerHelper.startMusic(context, uri,
                                    new RunnableEx("onCompleted") {
                                        @Override
                                        public void delegate() {
                                            showPlayerState(uri);
                                        }
                                    });
                        }

                        showPlayerState(uri);
                    } catch (Throwable ex) {
                        ivImage.setImageResource(R.drawable.twotone_warning_24);
                        Log.unexpectedError(parentFragment, ex);
                    }
                } else {
                    try {
                        Helper.share(context, attachment.getFile(context), attachment.getMimeType(), attachment.name);
                    } catch (Throwable ex) {
                        Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                    }
                }
            } else {
                if (attachment.progress == null) {
                    Bundle args = new Bundle();
                    args.putLong("id", attachment.id);
                    args.putLong("message", attachment.message);

                    new SimpleTask<Void>() {
                        @Override
                        protected Void onExecute(Context context, Bundle args) {
                            long id = args.getLong("id");
                            long mid = args.getLong("message");

                            Long reload = null;

                            DB db = DB.getInstance(context);
                            try {
                                db.beginTransaction();

                                EntityMessage message = db.message().getMessage(mid);
                                if (message == null)
                                    return null;

                                EntityAccount account = db.account().getAccount(message.account);
                                if (account == null)
                                    return null;

                                if (account.protocol == EntityAccount.TYPE_IMAP && message.uid == null)
                                    return null;

                                if (!"connected".equals(account.state) && !account.isTransient(context))
                                    reload = account.id;

                                EntityAttachment attachment = db.attachment().getAttachment(id);
                                if (attachment == null || attachment.progress != null || attachment.available)
                                    return null;

                                EntityOperation.queue(context, message, EntityOperation.ATTACHMENT, id);

                                db.setTransactionSuccessful();
                            } finally {
                                db.endTransaction();
                            }

                            if (reload == null)
                                ServiceSynchronize.eval(context, "media");
                            else
                                ServiceSynchronize.reload(context, reload, false, "media");

                            return null;
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                        }
                    }.execute(context, owner, args, "media:fetch");
                }
            }
        }

        @Override
        public boolean onLongClick(View v) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return false;

            EntityAttachment attachment = items.get(pos);
            if (!attachment.available)
                return false;

            ((FragmentBase) parentFragment).onStoreAttachment(attachment);

            return true;
        }
    }

    AdapterMedia(Fragment parentFragment) {
        this.parentFragment = parentFragment;
        this.context = parentFragment.getContext();
        this.owner = parentFragment.getViewLifecycleOwner();
        this.inflater = LayoutInflater.from(context);

        this.textColorTertiary = Helper.resolveColor(context, android.R.attr.textColorTertiary);
        this.textColorLink = Helper.resolveColor(context, android.R.attr.textColorLink);

        setHasStableIds(true);

        owner.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            public void onDestroyed() {
                Log.d(AdapterMedia.this + " parent destroyed");
                MediaPlayerHelper.stopMusic(context);
                AdapterMedia.this.parentFragment = null;
                owner.getLifecycle().removeObserver(this);
            }
        });
    }

    public void set(@NonNull List<EntityAttachment> attachments) {
        Log.i("Set media=" + attachments.size());

        Collections.sort(attachments, new Comparator<EntityAttachment>() {
            @Override
            public int compare(EntityAttachment a1, EntityAttachment a2) {
                return a1.sequence.compareTo(a2.sequence);
            }
        });

        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffCallback(items, attachments), false);

        items = attachments;

        diff.dispatchUpdatesTo(new ListUpdateCallback() {
            @Override
            public void onInserted(int position, int count) {
                Log.d("Inserted @" + position + " #" + count);
            }

            @Override
            public void onRemoved(int position, int count) {
                Log.d("Removed @" + position + " #" + count);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                Log.d("Moved " + fromPosition + ">" + toPosition);
            }

            @Override
            public void onChanged(int position, int count, Object payload) {
                Log.d("Changed @" + position + " #" + count);
            }
        });

        try {
            diff.dispatchUpdatesTo(this);
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    private static class DiffCallback extends DiffUtil.Callback {
        private final List<EntityAttachment> prev = new ArrayList<>();
        private final List<EntityAttachment> next = new ArrayList<>();

        DiffCallback(List<EntityAttachment> prev, List<EntityAttachment> next) {
            this.prev.addAll(prev);
            this.next.addAll(next);
        }

        @Override
        public int getOldListSize() {
            return prev.size();
        }

        @Override
        public int getNewListSize() {
            return next.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            EntityAttachment a1 = prev.get(oldItemPosition);
            EntityAttachment a2 = next.get(newItemPosition);
            return a1.id.equals(a2.id);
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            EntityAttachment a1 = prev.get(oldItemPosition);
            EntityAttachment a2 = next.get(newItemPosition);
            return a1.equals(a2);
        }
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).id;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.item_media, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.unwire();

        EntityAttachment attachment = items.get(position);
        holder.bindTo(attachment);

        holder.wire();
    }

    @Override
    public void onViewRecycled(@NonNull AdapterMedia.ViewHolder holder) {
        holder.ivImage.setImageDrawable(null);
    }
}
