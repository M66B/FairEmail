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
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
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

    private static Map<String, Lookup> emailLookup = new ConcurrentHashMap<>();
    private static final Map<String, ContactInfo> emailContactInfo = new HashMap<>();
    private static final Map<String, Avatar> emailGravatar = new HashMap<>();
    private static final ExecutorService executor =
            Helper.getBackgroundExecutor(1, "contact");

    private static final int GRAVATAR_TIMEOUT = 5 * 1000; // milliseconds
    private static final long CACHE_CONTACT_DURATION = 2 * 60 * 1000L; // milliseconds
    private static final long CACHE_GRAVATAR_DURATION = 2 * 60 * 60 * 1000L; // milliseconds

    private ContactInfo() {
    }

    boolean hasPhoto() {
        return (bitmap != null);
    }

    Bitmap getPhotoBitmap() {
        return bitmap;
    }

    String getEmailAddress() {
        return email;
    }

    String getDisplayName() {
        return displayName;
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

    @NonNull
    static ContactInfo[] get(Context context, long account, Address[] addresses) {
        return get(context, account, addresses, false);
    }

    static ContactInfo[] getCached(Context context, long account, Address[] addresses) {
        return get(context, account, addresses, true);
    }

    private static ContactInfo[] get(Context context, long account, Address[] addresses, boolean cacheOnly) {
        if (addresses == null || addresses.length == 0)
            return new ContactInfo[]{new ContactInfo()};

        ContactInfo[] result = new ContactInfo[addresses.length];
        for (int i = 0; i < addresses.length; i++) {
            result[i] = _get(context, account, (InternetAddress) addresses[i], cacheOnly);
            if (result[i] == null)
                return null;
        }

        return result;
    }

    private static ContactInfo _get(Context context, long account, InternetAddress address, boolean cacheOnly) {
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
        boolean avatars = prefs.getBoolean("avatars", true);
        boolean gravatars = prefs.getBoolean("gravatars", false);
        boolean generated = prefs.getBoolean("generated_icons", true);
        boolean identicons = prefs.getBoolean("identicons", false);
        boolean circular = prefs.getBoolean("circular", true);

        if (Helper.hasPermission(context, Manifest.permission.READ_CONTACTS)) {
            ContentResolver resolver = context.getContentResolver();
            Uri uri = Uri.withAppendedPath(
                    ContactsContract.CommonDataKinds.Email.CONTENT_LOOKUP_URI,
                    Uri.encode(address.getAddress()));
            try (Cursor cursor = resolver.query(uri,
                    new String[]{
                            ContactsContract.CommonDataKinds.Photo.CONTACT_ID,
                            ContactsContract.Contacts.LOOKUP_KEY,
                            ContactsContract.Contacts.DISPLAY_NAME
                    },
                    null, null, null)) {

                if (cursor != null && cursor.moveToNext()) {
                    int colContactId = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Photo.CONTACT_ID);
                    int colLookupKey = cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY);
                    int colDisplayName = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);

                    long contactId = cursor.getLong(colContactId);
                    String lookupKey = cursor.getString(colLookupKey);
                    Uri lookupUri = ContactsContract.Contacts.getLookupUri(contactId, lookupKey);

                    if (avatars)
                        try (InputStream is = ContactsContract.Contacts.openContactPhotoInputStream(
                                resolver, lookupUri, false)) {
                            info.bitmap = BitmapFactory.decodeStream(is);
                        } catch (Throwable ex) {
                            Log.e(ex);
                        }

                    info.displayName = cursor.getString(colDisplayName);
                    info.lookupUri = lookupUri;
                    info.known = true;
                }
            } catch (Throwable ex) {
                Log.e(ex);
            }
        }

        if (info.bitmap == null) {
            if (gravatars) {
                String gkey = address.getAddress().toLowerCase(Locale.ROOT);
                boolean lookup;
                synchronized (emailGravatar) {
                    Avatar avatar = emailGravatar.get(gkey);
                    lookup = (avatar == null || avatar.isExpired() || avatar.isAvailable());
                }

                if (lookup) {
                    HttpURLConnection urlConnection = null;
                    try {
                        String hash = Helper.md5(gkey.getBytes());
                        URL url = new URL("https://www.gravatar.com/avatar/" + hash + "?d=404");
                        Log.i("Gravatar key=" + gkey + " url=" + url);

                        urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setRequestMethod("GET");
                        urlConnection.setDoOutput(false);
                        urlConnection.setReadTimeout(GRAVATAR_TIMEOUT);
                        urlConnection.setConnectTimeout(GRAVATAR_TIMEOUT);
                        urlConnection.connect();

                        int status = urlConnection.getResponseCode();
                        if (status == HttpURLConnection.HTTP_OK) {
                            info.bitmap = BitmapFactory.decodeStream(urlConnection.getInputStream());
                            // Positive reply
                            synchronized (emailGravatar) {
                                emailGravatar.put(gkey, new Avatar(true));
                            }
                        } else if (status == HttpURLConnection.HTTP_NOT_FOUND) {
                            // Negative reply
                            synchronized (emailGravatar) {
                                emailGravatar.put(gkey, new Avatar(false));
                            }
                        } else
                            throw new IOException("HTTP status=" + status);

                    } catch (Throwable ex) {
                        Log.w(ex);
                    } finally {
                        if (urlConnection != null)
                            urlConnection.disconnect();
                    }
                }
            }
        }

        boolean identicon = false;
        if (info.bitmap == null) {
            int dp = Helper.dp2pixels(context, 96);
            if (generated) {
                if (identicons) {
                    identicon = true;
                    info.bitmap = ImageHelper.generateIdenticon(
                            address.getAddress(), dp, 5, context);
                } else
                    info.bitmap = ImageHelper.generateLetterIcon(
                            address.getAddress(), address.getPersonal(), dp, context);
            }
        }

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

    static Uri getLookupUri(Address[] addresses) {
        if (addresses == null)
            return null;

        for (Address from : addresses) {
            String email = ((InternetAddress) from).getAddress();
            if (emailLookup.containsKey(email))
                return emailLookup.get(email).uri;
        }

        return null;
    }

    static Address[] fillIn(Address[] addresses, boolean prefer_contact) {
        if (addresses == null)
            return null;

        Address[] modified = new Address[addresses.length];
        for (int i = 0; i < addresses.length; i++) {
            InternetAddress address = (InternetAddress) addresses[i];
            String email = address.getAddress();
            String personal = address.getPersonal();
            if (!TextUtils.isEmpty(email) && emailLookup.containsKey(email)) {
                Lookup lookup = emailLookup.get(email);
                if (TextUtils.isEmpty(personal) ||
                        (prefer_contact && !personal.equals(lookup.displayName)))
                    personal = lookup.displayName;
            }
            try {
                modified[i] = new InternetAddress(email, personal, StandardCharsets.UTF_8.name());
            } catch (UnsupportedEncodingException ex) {
                Log.e(ex);
                modified[i] = address;
            }
        }

        return modified;
    }

    private static Map<String, Lookup> getEmailLookup(Context context) {
        Map<String, Lookup> all = new ConcurrentHashMap<>();

        if (Helper.hasPermission(context, Manifest.permission.READ_CONTACTS)) {
            Log.i("Reading email/uri");
            ContentResolver resolver = context.getContentResolver();

            try (Cursor cursor = resolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                    new String[]{
                            ContactsContract.CommonDataKinds.Photo.CONTACT_ID,
                            ContactsContract.Contacts.LOOKUP_KEY,
                            ContactsContract.CommonDataKinds.Email.ADDRESS,
                            ContactsContract.Contacts.DISPLAY_NAME
                    },
                    ContactsContract.CommonDataKinds.Email.ADDRESS + " <> ''",
                    null, null)) {
                while (cursor != null && cursor.moveToNext()) {
                    long contactId = cursor.getLong(0);
                    String lookupKey = cursor.getString(1);
                    String email = cursor.getString(2);
                    String displayName = cursor.getString(3);

                    Lookup lookup = new Lookup();
                    lookup.uri = ContactsContract.Contacts.getLookupUri(contactId, lookupKey);
                    lookup.displayName = displayName;
                    all.put(email, lookup);
                }
            } catch (Throwable ex) {
                Log.e(ex);
            }
        }

        Log.i("Read email/uri=" + all.size());
        return all;
    }

    private static class Lookup {
        Uri uri;
        String displayName;
    }

    private static class Avatar {
        private boolean available;
        private long time;

        Avatar(boolean available) {
            this.available = available;
            this.time = new Date().getTime();
        }

        boolean isAvailable() {
            return available;
        }

        boolean isExpired() {
            return (new Date().getTime() - time > CACHE_GRAVATAR_DURATION);
        }
    }
}