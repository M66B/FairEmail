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

    Copyright 2018-2023 by Marcel Bokhorst (M66B)
*/

import static android.system.OsConstants.ENOSPC;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.system.ErrnoException;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;
import androidx.exifinterface.media.ExifInterface;
import androidx.preference.PreferenceManager;

import com.google.android.material.snackbar.Snackbar;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.mail.Part;
import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;

public class ComposeHelper {
    static final int REDUCED_IMAGE_SIZE = 1440; // pixels

    private static final int REDUCED_IMAGE_QUALITY = 90; // percent
    // http://regex.info/blog/lightroom-goodies/jpeg-quality
    private static final int COPY_ATTACHMENT_TIMEOUT = 60; // seconds

    static EntityAttachment addAttachment(
            Context context, long id, Uri uri, String type, boolean image, int resize, boolean privacy) throws IOException {
        Log.w("Add attachment uri=" + uri + " image=" + image + " resize=" + resize + " privacy=" + privacy);

        NoStreamException.check(uri, context);

        EntityAttachment attachment = new EntityAttachment();
        ComposeHelper.UriInfo info = ComposeHelper.getUriInfo(uri, context);

        EntityLog.log(context, "Add attachment" +
                " uri=" + uri + " type=" + type + " image=" + image + " resize=" + resize + " privacy=" + privacy +
                " name=" + info.name + " type=" + info.type + " size=" + info.size);

        if (type == null)
            type = info.type;

        String ext = Helper.getExtension(info.name);
        if (info.name != null && ext == null && type != null) {
            String guessed = MimeTypeMap.getSingleton()
                    .getExtensionFromMimeType(type.toLowerCase(Locale.ROOT));
            if (!TextUtils.isEmpty(guessed)) {
                ext = guessed;
                info.name += '.' + ext;
            }
        }

        DB db = DB.getInstance(context);
        try {
            db.beginTransaction();

            EntityMessage draft = db.message().getMessage(id);
            if (draft == null)
                return null;

            Log.i("Attaching to id=" + id);

            attachment.message = draft.id;
            attachment.sequence = db.attachment().getAttachmentSequence(draft.id) + 1;
            if (privacy)
                attachment.name = "img" + attachment.sequence + (ext == null ? "" : "." + ext);
            else
                attachment.name = info.name;
            attachment.type = type;
            attachment.disposition = (image ? Part.INLINE : Part.ATTACHMENT);
            attachment.size = info.size;
            attachment.progress = 0;

            attachment.id = db.attachment().insertAttachment(attachment);
            Log.i("Created attachment=" + attachment.name + ":" + attachment.sequence + " type=" + attachment.type);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        long size = 0;
        int lastProgress = 0;
        try {
            File file = attachment.getFile(context);

            InputStream is = null;
            OutputStream os = null;
            try {
                is = context.getContentResolver().openInputStream(uri);
                os = new FileOutputStream(file);

                if (is == null)
                    throw new FileNotFoundException(uri.toString());

                final InputStream reader = is;
                byte[] buffer = new byte[Helper.BUFFER_SIZE];
                Callable<Integer> readTask = new Callable<Integer>() {
                    @Override
                    public Integer call() throws Exception {
                        return reader.read(buffer);
                    }
                };

                while (true) {
                    Future<Integer> future = Helper.getDownloadTaskExecutor().submit(readTask);
                    int len = future.get(COPY_ATTACHMENT_TIMEOUT, TimeUnit.SECONDS);
                    if (len == -1)
                        break;
                    if (len == 0) {
                        Thread.sleep(500L);
                        continue;
                    }

                    size += len;
                    os.write(buffer, 0, len);

                    // Update progress
                    if (attachment.size != null && attachment.size > 0) {
                        int progress = (int) (size * 100 / attachment.size / 20 * 20);
                        if (progress != lastProgress) {
                            lastProgress = progress;
                            db.attachment().setProgress(attachment.id, progress);
                        }
                    }
                }

                if (image) {
                    attachment.cid = "<" + BuildConfig.APPLICATION_ID + "." + attachment.id + ">";
                    attachment.related = true;
                    db.attachment().setCid(attachment.id, attachment.cid, attachment.related);
                }
            } finally {
                try {
                    if (is != null)
                        is.close();
                } finally {
                    if (os != null)
                        os.close();
                }
            }

            db.attachment().setDownloaded(attachment.id, size);

            if (BuildConfig.APPLICATION_ID.equals(uri.getAuthority()) &&
                    uri.getPathSegments().size() > 0 &&
                    "photo".equals(uri.getPathSegments().get(0))) {
                // content://eu.faircode.email/photo/nnn.jpg
                File tmp = new File(context.getFilesDir(), uri.getPath());
                Log.i("Deleting " + tmp);
                Helper.secureDelete(tmp);
            } else
                Log.i("Authority=" + uri.getAuthority());

            if (resize > 0)
                resizeAttachment(context, attachment, resize);

            if (privacy && resize == 0)
                try {
                    ExifInterface exif = new ExifInterface(file);

                    exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, null);
                    exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, null);
                    exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, null);
                    exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, null);
                    exif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF, null);
                    exif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE, null);
                    exif.setAttribute(ExifInterface.TAG_GPS_SPEED_REF, null);
                    exif.setAttribute(ExifInterface.TAG_GPS_SPEED, null);

                    exif.setAttribute(ExifInterface.TAG_GPS_DEST_LATITUDE_REF, null);
                    exif.setAttribute(ExifInterface.TAG_GPS_DEST_LATITUDE, null);
                    exif.setAttribute(ExifInterface.TAG_GPS_DEST_LONGITUDE_REF, null);
                    exif.setAttribute(ExifInterface.TAG_GPS_DEST_LONGITUDE, null);
                    exif.setAttribute(ExifInterface.TAG_GPS_DEST_BEARING_REF, null);
                    exif.setAttribute(ExifInterface.TAG_GPS_DEST_BEARING, null);
                    exif.setAttribute(ExifInterface.TAG_GPS_DEST_DISTANCE_REF, null);
                    exif.setAttribute(ExifInterface.TAG_GPS_DEST_DISTANCE, null);

                    exif.setAttribute(ExifInterface.TAG_GPS_IMG_DIRECTION_REF, null);
                    exif.setAttribute(ExifInterface.TAG_GPS_IMG_DIRECTION, null);

                    exif.setAttribute(ExifInterface.TAG_GPS_TRACK_REF, null);
                    exif.setAttribute(ExifInterface.TAG_GPS_TRACK, null);

                    exif.setAttribute(ExifInterface.TAG_GPS_AREA_INFORMATION, null);

                    exif.setAttribute(ExifInterface.TAG_DATETIME, null);
                    exif.setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, null);
                    exif.setAttribute(ExifInterface.TAG_DATETIME_DIGITIZED, null);

                    exif.setAttribute(ExifInterface.TAG_XMP, null);
                    exif.setAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION, null);
                    //exif.setAttribute(ExifInterface.TAG_MAKE, null);
                    //exif.setAttribute(ExifInterface.TAG_MODEL, null);
                    //exif.setAttribute(ExifInterface.TAG_SOFTWARE, null);
                    exif.setAttribute(ExifInterface.TAG_ARTIST, null);
                    exif.setAttribute(ExifInterface.TAG_COPYRIGHT, null);
                    exif.setAttribute(ExifInterface.TAG_USER_COMMENT, null);
                    exif.setAttribute(ExifInterface.TAG_IMAGE_UNIQUE_ID, null);
                    exif.setAttribute(ExifInterface.TAG_CAMERA_OWNER_NAME, null);
                    exif.setAttribute(ExifInterface.TAG_BODY_SERIAL_NUMBER, null);
                    exif.setAttribute(ExifInterface.TAG_LENS_SERIAL_NUMBER, null);

                    exif.saveAttributes();
                } catch (IOException ex) {
                    Log.i(ex);
                }

            // https://www.rfc-editor.org/rfc/rfc2231
            if (attachment.name != null && attachment.name.length() > 60)
                db.attachment().setWarning(attachment.id, context.getString(R.string.title_attachment_filename));

        } catch (Throwable ex) {
            // Reset progress on failure
            Log.e(ex);
            db.attachment().setError(attachment.id, Log.formatThrowable(ex, false));
            return null;
        }

        return attachment;
    }

    static void resizeAttachment(Context context, EntityAttachment attachment, int resize) throws IOException {
        File file = attachment.getFile(context);
        if (file.exists() /* upload cancelled */ &&
                ("image/jpeg".equals(attachment.type) ||
                        "image/png".equals(attachment.type) ||
                        "image/webp".equals(attachment.type))) {
            ExifInterface exifSaved;
            try {
                exifSaved = new ExifInterface(file);
            } catch (Throwable ex) {
                Log.w(ex);
                exifSaved = null;
            }

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(file.getAbsolutePath(), options);

            int factor = 1;
            while (options.outWidth / factor > resize ||
                    options.outHeight / factor > resize)
                factor *= 2;

            Matrix rotation = ("image/jpeg".equals(attachment.type) ? ImageHelper.getImageRotation(file) : null);
            Log.i("Image type=" + attachment.type + " rotation=" + rotation);
            if (factor > 1 || rotation != null) {
                options.inJustDecodeBounds = false;
                options.inSampleSize = factor;

                Log.i("Image target size=" + resize + " factor=" + factor + " source=" + options.outWidth + "x" + options.outHeight);
                Bitmap resized = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
                if (resized != null) {
                    Log.i("Image result size=" + resized.getWidth() + "x" + resized.getHeight() + " rotation=" + rotation);

                    if (rotation != null) {
                        Bitmap rotated = Bitmap.createBitmap(resized, 0, 0, resized.getWidth(), resized.getHeight(), rotation, true);
                        resized.recycle();
                        resized = rotated;
                    }

                    Bitmap.CompressFormat format;
                    if ("image/jpeg".equals(attachment.type))
                        format = Bitmap.CompressFormat.JPEG;
                    else if ("image/png".equals(attachment.type))
                        format = Bitmap.CompressFormat.PNG;
                    else if ("image/webp".equals(attachment.type))
                        format = Bitmap.CompressFormat.WEBP;
                    else
                        throw new IllegalArgumentException("Invalid format type=" + attachment.type);

                    File tmp = new File(file.getAbsolutePath() + ".tmp");
                    try (OutputStream out = new BufferedOutputStream(new FileOutputStream(tmp))) {
                        if (!resized.compress(format, REDUCED_IMAGE_QUALITY, out))
                            throw new IOException("compress");
                    } catch (Throwable ex) {
                        Log.w(ex);
                        Helper.secureDelete(tmp);
                    } finally {
                        resized.recycle();
                    }

                    if (tmp.exists() && tmp.length() > 0) {
                        Helper.secureDelete(file);
                        tmp.renameTo(file);
                    }

                    DB db = DB.getInstance(context);
                    db.attachment().setDownloaded(attachment.id, file.length());

                    if (exifSaved != null)
                        try {
                            ExifInterface exif = new ExifInterface(file);

                            // Preserve time
                            if (exifSaved.hasAttribute(ExifInterface.TAG_DATETIME_ORIGINAL))
                                exif.setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL,
                                        exifSaved.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL));
                            if (exifSaved.hasAttribute(ExifInterface.TAG_GPS_DATESTAMP))
                                exif.setAttribute(ExifInterface.TAG_GPS_DATESTAMP,
                                        exifSaved.getAttribute(ExifInterface.TAG_GPS_DATESTAMP));

                            // Preserve location
                            double[] latlong = exifSaved.getLatLong();
                            if (latlong != null)
                                exif.setLatLong(latlong[0], latlong[1]);

                            // Preserve altitude
                            if (exifSaved.hasAttribute(ExifInterface.TAG_GPS_ALTITUDE) &&
                                    exifSaved.hasAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF))
                                exif.setAltitude(exifSaved.getAltitude(0));

                            exif.saveAttributes();
                        } catch (Throwable ex) {
                            Log.w(ex);
                        }
                }
            }
        }
    }

    static void addSignature(Context context, Document document, EntityMessage draft, EntityIdentity identity) {
        if (!draft.signature ||
                identity == null || TextUtils.isEmpty(identity.signature))
            return;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int signature_location = prefs.getInt("signature_location", 1);
        boolean usenet = prefs.getBoolean("usenet_signature", false);
        boolean write_below = prefs.getBoolean("write_below", false);
        String compose_font = prefs.getString("compose_font", "");

        boolean wb = (draft == null || draft.write_below == null ? write_below : draft.write_below);

        Element div = document.createElement("div");
        div.attr("fairemail", "signature");
        if (!TextUtils.isEmpty(compose_font))
            div.attr("style", "font-family: " + StyleHelper.getFamily(compose_font));

        if (usenet) {
            // https://datatracker.ietf.org/doc/html/rfc3676#section-4.3
            Element span = document.createElement("span");
            span.text("-- ");
            span.prependElement("br");
            span.appendElement("br");
            div.appendChild(span);
        }

        div.append(identity.signature);

        Elements ref = document.select("div[fairemail=reference]");
        if (signature_location == 0) // top
            document.body().prependChild(div);
        else if (ref.size() == 0 || signature_location == 2) // bottom
            document.body().appendChild(div);
        else if (signature_location == 1) // below text
            if (wb && draft.wasforwardedfrom == null)
                document.body().appendChild(div);
            else
                ref.first().before(div);
    }

    static void handleException(FragmentBase fragment, View view, Throwable ex) {
        // External app sending absolute file
        if (ex instanceof NoStreamException)
            ((NoStreamException) ex).report(fragment.getActivity());
        else if (ex instanceof FileNotFoundException ||
                ex instanceof IllegalArgumentException ||
                ex instanceof IllegalStateException) {
                    /*
                        java.lang.IllegalStateException: Failed to mount
                          at android.os.Parcel.createException(Parcel.java:2079)
                          at android.os.Parcel.readException(Parcel.java:2039)
                          at android.database.DatabaseUtils.readExceptionFromParcel(DatabaseUtils.java:188)
                          at android.database.DatabaseUtils.readExceptionWithFileNotFoundExceptionFromParcel(DatabaseUtils.java:151)
                          at android.content.ContentProviderProxy.openTypedAssetFile(ContentProviderNative.java:705)
                          at android.content.ContentResolver.openTypedAssetFileDescriptor(ContentResolver.java:1687)
                          at android.content.ContentResolver.openAssetFileDescriptor(ContentResolver.java:1503)
                          at android.content.ContentResolver.openInputStream(ContentResolver.java:1187)
                          at eu.faircode.email.FragmentCompose.addAttachment(SourceFile:27)
                     */
            Snackbar.make(view, ex.toString(), Snackbar.LENGTH_LONG)
                    .setGestureInsetBottomIgnored(true).show();
        } else {
            if (ex instanceof IOException &&
                    ex.getCause() instanceof ErrnoException &&
                    ((ErrnoException) ex.getCause()).errno == ENOSPC)
                ex = new IOException(fragment.getContext().getString(R.string.app_cake), ex);

            // External app didn't grant URI permissions
            if (ex instanceof SecurityException)
                ex = new Throwable(fragment.getString(R.string.title_no_permissions), ex);

            Log.unexpectedError(fragment, ex,
                    !(ex instanceof IOException || ex.getCause() instanceof IOException));
                    /*
                        java.lang.IllegalStateException: java.io.IOException: Failed to redact /storage/emulated/0/Download/97203830-piston-vecteur-icône-simple-symbole-plat-sur-fond-blanc.jpg
                          at android.os.Parcel.createExceptionOrNull(Parcel.java:2381)
                          at android.os.Parcel.createException(Parcel.java:2357)
                          at android.os.Parcel.readException(Parcel.java:2340)
                          at android.database.DatabaseUtils.readExceptionFromParcel(DatabaseUtils.java:190)
                          at android.database.DatabaseUtils.readExceptionWithFileNotFoundExceptionFromParcel(DatabaseUtils.java:153)
                          at android.content.ContentProviderProxy.openTypedAssetFile(ContentProviderNative.java:804)
                          at android.content.ContentResolver.openTypedAssetFileDescriptor(ContentResolver.java:2002)
                          at android.content.ContentResolver.openAssetFileDescriptor(ContentResolver.java:1817)
                          at android.content.ContentResolver.openInputStream(ContentResolver.java:1494)
                          at eu.faircode.email.FragmentCompose.addAttachment(SourceFile:27)
                     */
        }
    }

    static void noStorageAccessFramework(View view) {
        Snackbar snackbar = Snackbar.make(view, R.string.title_no_saf, Snackbar.LENGTH_LONG)
                .setGestureInsetBottomIgnored(true);
        snackbar.setAction(R.string.title_fix, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 25);
            }
        });
        snackbar.show();
    }

    static List<Uri> getUris(Intent data) {
        List<Uri> result = new ArrayList<>();

        ClipData clipData = data.getClipData();
        if (clipData == null) {
            Uri uri = data.getData();
            if (uri != null)
                result.add(uri);
        } else {
            for (int i = 0; i < clipData.getItemCount(); i++) {
                ClipData.Item item = clipData.getItemAt(i);
                Uri uri = item.getUri();
                if (uri != null)
                    result.add(uri);
            }
        }

        // media-uri-list=[content://media/external_primary/images/media/nnn] (ArrayList)
        // media-file-list=[/storage/emulated/0/Pictures/...]
        // (ArrayList) media-id-list=[nnn] (ArrayList)
        if (result.size() == 0 && data.hasExtra("media-uri-list"))
            try {
                List<Uri> uris = data.getParcelableArrayListExtra("media-uri-list");
                result.addAll(uris);
            } catch (Throwable ex) {
                Log.e(ex);
            }

        return result;
    }

    @NonNull
    static UriInfo getUriInfo(Uri uri, Context context) {
        UriInfo result = new UriInfo();

        // https://stackoverflow.com/questions/76094229/android-13-photo-video-picker-file-name-from-the-uri-is-garbage
        DocumentFile dfile = null;
        try {
            dfile = DocumentFile.fromSingleUri(context, uri);
            if (dfile != null) {
                result.name = dfile.getName();
                result.type = dfile.getType();
                result.size = dfile.length();
                EntityLog.log(context, "UriInfo dfile " + result + " uri=" + uri);
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }

        // Check name
        if (TextUtils.isEmpty(result.name))
            result.name = uri.getLastPathSegment();

        // Check type
        if (!TextUtils.isEmpty(result.type))
            try {
                new ContentType(result.type);
            } catch (ParseException ex) {
                Log.w(new Throwable(result.type, ex));
                result.type = null;
            }

        if (TextUtils.isEmpty(result.type) ||
                "*/*".equals(result.type) ||
                "application/*".equals(result.type) ||
                "application/octet-stream".equals(result.type))
            result.type = Helper.guessMimeType(result.name);

        if (result.size != null && result.size <= 0)
            result.size = null;

        EntityLog.log(context, "UriInfo result " + result + " uri=" + uri);

        return result;
    }

    static class UriInfo {
        String name;
        String type;
        Long size;

        boolean isImage() {
            return ImageHelper.isImage(type);
        }

        @NonNull
        @Override
        public String toString() {
            return "name=" + name + " type=" + type + " size=" + size;
        }
    }

    static class DraftData {
        EntityMessage draft;
        List<TupleIdentityEx> identities;
    }
}
