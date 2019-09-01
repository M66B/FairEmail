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

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;

import androidx.preference.PreferenceManager;

import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

public class ContactInfo {
    private String email;
    private Bitmap bitmap;
    private String displayName;
    private Uri lookupUri;
    private long time;

    private static Map<String, Uri> emailLookup = new ConcurrentHashMap<>();
    private static Map<String, ContactInfo> emailContactInfo = new HashMap<>();

    private static final ExecutorService executor =
            Executors.newSingleThreadExecutor(Helper.backgroundThreadFactory);

    private static final long CACHE_CONTACT_DURATION = 120 * 1000L;

    private ContactInfo() {
    }

    boolean hasPhoto() {
        return (bitmap != null);
    }

    Bitmap getPhotoBitmap() {
        return bitmap;
    }

    String getDisplayName(boolean name_email) {
        if (!name_email && displayName != null)
            return displayName;
        else if (displayName == null)
            return (email == null ? "" : email);
        else
            return displayName + " <" + email + ">";
    }

    boolean hasLookupUri() {
        return (lookupUri != null);
    }

    Uri getLookupUri() {
        return lookupUri;
    }

    private boolean isExpired() {
        return (new Date().getTime() - time > CACHE_CONTACT_DURATION);
    }

    static void clearCache() {
        synchronized (emailContactInfo) {
            emailContactInfo.clear();
        }
    }

    static ContactInfo get(Context context, Address[] addresses, boolean cacheOnly) {
        if (addresses == null || addresses.length == 0)
            return new ContactInfo();
        InternetAddress address = (InternetAddress) addresses[0];

        String key = MessageHelper.formatAddresses(new Address[]{address});
        synchronized (emailContactInfo) {
            ContactInfo info = emailContactInfo.get(key);
            if (info != null && !info.isExpired())
                return info;
        }

        if (cacheOnly)
            return null;

        ContactInfo info = new ContactInfo();
        info.email = address.getAddress();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        if (Helper.hasPermission(context, Manifest.permission.READ_CONTACTS)) {
            ContentResolver resolver = context.getContentResolver();
            try (Cursor cursor = resolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                    new String[]{
                            ContactsContract.CommonDataKinds.Photo.CONTACT_ID,
                            ContactsContract.Contacts.LOOKUP_KEY,
                            ContactsContract.Contacts.DISPLAY_NAME
                    },
                    ContactsContract.CommonDataKinds.Email.ADDRESS + " = ? COLLATE NOCASE",
                    new String[]{
                            address.getAddress()
                    }, null)) {

                if (cursor != null && cursor.moveToNext()) {
                    int colContactId = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Photo.CONTACT_ID);
                    int colLookupKey = cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY);
                    int colDisplayName = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);

                    long contactId = cursor.getLong(colContactId);
                    String lookupKey = cursor.getString(colLookupKey);
                    Uri lookupUri = ContactsContract.Contacts.getLookupUri(contactId, lookupKey);

                    boolean avatars = prefs.getBoolean("avatars", true);
                    if (avatars) {
                        InputStream is = ContactsContract.Contacts.openContactPhotoInputStream(resolver, lookupUri);
                        info.bitmap = BitmapFactory.decodeStream(is);
                    }

                    info.displayName = cursor.getString(colDisplayName);
                    info.lookupUri = lookupUri;
                }
            } catch (Throwable ex) {
                Log.e(ex);
            }
        }

        if (info.bitmap == null) {
            int dp = Helper.dp2pixels(context, 48);
            boolean dark = Helper.isDarkTheme(context);
            boolean generated = prefs.getBoolean("generated_icons", true);
            if (generated) {
                boolean identicons = prefs.getBoolean("identicons", false);
                if (identicons)
                    info.bitmap = Identicon.icon(key, dp, 5, dark);
                else
                    info.bitmap = Identicon.letter(key, dp, dark);
            }
        }

        boolean circular = prefs.getBoolean("circular", true);
        if (info.bitmap != null) {
            int w = info.bitmap.getWidth();
            int h = info.bitmap.getHeight();

            Rect source;
            if (w > h) {
                int off = (w - h) / 2;
                source = new Rect(off, 0, w - off, h);
            } else if (w < h) {
                int off = (h - w) / 2;
                source = new Rect(0, off, w, h - off);
            } else
                source = new Rect(0, 0, w, h);

            Rect dest = new Rect(0, 0, source.width(), source.height());

            Bitmap round = Bitmap.createBitmap(source.width(), source.height(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(round);

            Paint paint = new Paint();
            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(Color.GRAY);
            if (circular)
                canvas.drawOval(new RectF(dest), paint);
            else {
                float radius = Helper.dp2pixels(context, 3);
                canvas.drawRoundRect(new RectF(dest), radius, radius, paint);
            }
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(info.bitmap, source, dest, paint);

            info.bitmap.recycle();
            info.bitmap = round;
        }

        if (info.displayName == null)
            info.displayName = address.getPersonal();

        synchronized (emailContactInfo) {
            emailContactInfo.put(key, info);
        }

        info.time = new Date().getTime();
        return info;
    }

    static void init(final Context context) {
        if (Helper.hasPermission(context, Manifest.permission.READ_CONTACTS)) {
            Handler handler = new Handler(Looper.getMainLooper());

            ContentObserver observer = new ContentObserver(handler) {
                @Override
                public void onChange(boolean selfChange, Uri uri) {
                    Log.i("Contact changed uri=" + uri);
                    executor.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                emailLookup = getEmailLookup(context);
                            } catch (Throwable ex) {
                                Log.e(ex);
                            }
                        }
                    });
                }
            };

            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        emailLookup = getEmailLookup(context);
                    } catch (Throwable ex) {
                        Log.e(ex);
                    }
                }
            });

            Uri uri = ContactsContract.CommonDataKinds.Email.CONTENT_URI;
            Log.i("Observing uri=" + uri);
            context.getContentResolver().registerContentObserver(uri, true, observer);
        }
    }

    static Uri getLookupUri(Context context, Address[] addresses) {
        if (addresses == null)
            return null;

        for (Address from : addresses) {
            String email = ((InternetAddress) from).getAddress();
            if (emailLookup.containsKey(email))
                return emailLookup.get(email);
        }

        return null;
    }

    private static Map<String, Uri> getEmailLookup(Context context) {
        Map<String, Uri> all = new ConcurrentHashMap<>();

        if (Helper.hasPermission(context, Manifest.permission.READ_CONTACTS)) {
            Log.i("Reading email/uri");
            ContentResolver resolver = context.getContentResolver();

            try (Cursor cursor = resolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                    new String[]{
                            ContactsContract.CommonDataKinds.Photo.CONTACT_ID,
                            ContactsContract.Contacts.LOOKUP_KEY,
                            ContactsContract.CommonDataKinds.Email.ADDRESS
                    },
                    ContactsContract.CommonDataKinds.Email.ADDRESS + " <> ''",
                    null, null)) {
                while (cursor != null && cursor.moveToNext()) {
                    long contactId = cursor.getLong(0);
                    String lookupKey = cursor.getString(1);
                    String email = cursor.getString(2);

                    Uri uri = ContactsContract.Contacts.getLookupUri(contactId, lookupKey);
                    all.put(email, uri);
                }
            } catch (Throwable ex) {
                Log.e(ex);
            }
        }

        Log.i("Read email/uri=" + all.size());
        return all;
    }
}