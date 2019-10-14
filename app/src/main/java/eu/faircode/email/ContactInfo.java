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

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

public class ContactInfo {
    private String email;
    private Bitmap bitmap;
    private String displayName;
    private Uri lookupUri;
    private boolean known;
    private long time;

    private static Map<String, Uri> emailLookup = new ConcurrentHashMap<>();
    private static final Map<String, ContactInfo> emailContactInfo = new HashMap<>();
    private static final ExecutorService executor =
            Helper.getBackgroundExecutor(1, "contact");

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

    boolean isKnown() {
        return known;
    }

    private boolean isExpired() {
        return (new Date().getTime() - time > CACHE_CONTACT_DURATION);
    }

    static void clearCache() {
        synchronized (emailContactInfo) {
            emailContactInfo.clear();
        }
    }

    static ContactInfo get(Context context, long account, Address[] addresses, boolean cacheOnly) {
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
                    info.known = true;
                }
            } catch (Throwable ex) {
                Log.e(ex);
            }
        }

        boolean identicon = false;
        if (info.bitmap == null) {
            int dp = Helper.dp2pixels(context, 96);
            boolean generated = prefs.getBoolean("generated_icons", true);
            if (generated) {
                boolean identicons = prefs.getBoolean("identicons", false);
                if (identicons) {
                    identicon = true;
                    info.bitmap = ImageHelper.generateIdenticon(
                            address.getAddress(), dp, 5, context);
                } else
                    info.bitmap = ImageHelper.generateLetterIcon(
                            address.getAddress(), address.getPersonal(), dp, context);
            }
        }

        boolean circular = prefs.getBoolean("circular", true);
        info.bitmap = ImageHelper.makeCircular(info.bitmap,
                circular && !identicon ? null : Helper.dp2pixels(context, 3));

        if (info.displayName == null)
            info.displayName = address.getPersonal();

        if (!info.known) {
            DB db = DB.getInstance(context);
            EntityContact contact = db.contact().getContact(account, EntityContact.TYPE_TO, info.email);
            info.known = (contact != null);
        }

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