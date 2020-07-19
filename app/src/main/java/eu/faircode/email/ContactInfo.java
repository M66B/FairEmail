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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLProtocolException;

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

    private static final ExecutorService executorLookup =
            Helper.getBackgroundExecutor(1, "contact");

    private static final ExecutorService executorFavicon =
            Helper.getBackgroundExecutor(0, "favicon");

    private static final int GRAVATAR_TIMEOUT = 5 * 1000; // milliseconds
    private static final int FAVICON_CONNECT_TIMEOUT = 5 * 1000; // milliseconds
    private static final int FAVICON_READ_TIMEOUT = 10 * 1000; // milliseconds
    private static final int FAVICON_READ_BYTES = 2048;
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

    static void clearCache(Context context) {
        synchronized (emailContactInfo) {
            emailContactInfo.clear();
        }

        final File dir = new File(context.getCacheDir(), "favicons");

        executorFavicon.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    File[] favicons = dir.listFiles();
                    if (favicons != null)
                        for (File favicon : favicons)
                            favicon.delete();
                } catch (Throwable ex) {
                    Log.w(ex);
                }
            }
        });
    }

    @NonNull
    static ContactInfo[] get(Context context, long account, String folderType, Address[] addresses) {
        return get(context, account, folderType, addresses, false);
    }

    static ContactInfo[] getCached(Context context, long account, String folderType, Address[] addresses) {
        return get(context, account, folderType, addresses, true);
    }

    private static ContactInfo[] get(Context context, long account, String folderType, Address[] addresses, boolean cacheOnly) {
        if (addresses == null || addresses.length == 0)
            return new ContactInfo[]{new ContactInfo()};

        ContactInfo[] result = new ContactInfo[addresses.length];
        for (int i = 0; i < addresses.length; i++) {
            result[i] = _get(context, account, folderType, (InternetAddress) addresses[i], cacheOnly);
            if (result[i] == null)
                return null;
        }

        return result;
    }

    private static ContactInfo _get(Context context, long account, String folderType, InternetAddress address, boolean cacheOnly) {
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
        boolean favicons = prefs.getBoolean("favicons", false);
        boolean generated = prefs.getBoolean("generated_icons", true);
        boolean identicons = prefs.getBoolean("identicons", false);
        boolean circular = prefs.getBoolean("circular", true);

        // Contact photo
        if (!TextUtils.isEmpty(info.email) &&
                Helper.hasPermission(context, Manifest.permission.READ_CONTACTS)) {
            ContentResolver resolver = context.getContentResolver();
            Uri uri = Uri.withAppendedPath(
                    ContactsContract.CommonDataKinds.Email.CONTENT_LOOKUP_URI,
                    Uri.encode(info.email.toLowerCase(Locale.ROOT)));
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

        // Gravatar
        if (info.bitmap == null && gravatars) {
            if (!TextUtils.isEmpty(info.email)) {
                String gkey = info.email.toLowerCase(Locale.ROOT);
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

        // Favicon
        if (info.bitmap == null && favicons &&
                !EntityFolder.JUNK.equals(folderType)) {
            int at = (info.email == null ? -1 : info.email.indexOf('@'));
            String domain = (at < 0 ? null : info.email.substring(at + 1).toLowerCase(Locale.ROOT));

            if (domain != null) {
                if ("gmail.com".equals(domain) || "googlemail.com".equals(domain))
                    domain = "google.com";

                File dir = new File(context.getCacheDir(), "favicons");
                if (!dir.exists())
                    dir.mkdir();
                File file = new File(dir, domain);

                try {
                    // check cache
                    if (file.exists())
                        if (file.length() == 0)
                            Log.i("Favicon blacklisted domain=" + domain);
                        else
                            info.bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    else {
                        final URL base = new URL("https://" + domain);
                        final URL www = new URL("https://www." + domain);

                        List<Future<Bitmap>> futures = new ArrayList<>();

                        futures.add(executorFavicon.submit(new Callable<Bitmap>() {
                            @Override
                            public Bitmap call() throws Exception {
                                return parseFavicon(base);
                            }
                        }));

                        futures.add(executorFavicon.submit(new Callable<Bitmap>() {
                            @Override
                            public Bitmap call() throws Exception {
                                return parseFavicon(www);
                            }
                        }));

                        futures.add(executorFavicon.submit(new Callable<Bitmap>() {
                            @Override
                            public Bitmap call() throws Exception {
                                return getFavicon(new URL(base, "favicon.ico"));
                            }
                        }));

                        futures.add(executorFavicon.submit(new Callable<Bitmap>() {
                            @Override
                            public Bitmap call() throws Exception {
                                return getFavicon(new URL(www, "favicon.ico"));
                            }
                        }));

                        Throwable ex = null;
                        for (Future<Bitmap> future : futures)
                            try {
                                info.bitmap = future.get();
                                if (info.bitmap != null)
                                    break;
                            } catch (ExecutionException exex) {
                                ex = exex.getCause();
                            } catch (Throwable exex) {
                                ex = exex;
                            }

                        if (info.bitmap == null)
                            throw ex;

                        // Add to cache
                        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
                            info.bitmap.compress(Bitmap.CompressFormat.PNG, 90, os);
                        }
                    }
                } catch (Throwable ex) {
                    if (isRecoverable(ex, context))
                        Log.i(ex);
                    else {
                        Log.w(ex);
                        try {
                            file.createNewFile();
                        } catch (IOException ex1) {
                            Log.e(ex1);
                        }
                    }
                }
            }
        }

        // Generated
        boolean identicon = false;
        if (info.bitmap == null && generated) {
            int dp = Helper.dp2pixels(context, 96);
            if (!TextUtils.isEmpty(info.email)) {
                if (identicons) {
                    identicon = true;
                    info.bitmap = ImageHelper.generateIdenticon(
                            info.email, dp, 5, context);
                } else
                    info.bitmap = ImageHelper.generateLetterIcon(
                            info.email, address.getPersonal(), dp, context);
            }
        }

        info.bitmap = ImageHelper.makeCircular(info.bitmap,
                circular && !identicon ? null : Helper.dp2pixels(context, 3));

        if (info.displayName == null)
            info.displayName = address.getPersonal();

        if (!info.known && !TextUtils.isEmpty(info.email)) {
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

    private static Bitmap parseFavicon(URL base) throws IOException {
        Log.i("GET favicon " + base);
        HttpsURLConnection connection = (HttpsURLConnection) base.openConnection();
        connection.setRequestMethod("GET");
        connection.setReadTimeout(FAVICON_READ_TIMEOUT);
        connection.setConnectTimeout(FAVICON_CONNECT_TIMEOUT);
        connection.setInstanceFollowRedirects(true);
        connection.connect();

        String response;
        try {
            byte[] buffer = new byte[FAVICON_READ_BYTES];
            int len = connection.getInputStream().read(buffer);
            if (len < 0)
                throw new IOException("length");
            response = new String(buffer, 0, len, StandardCharsets.UTF_8.name());
        } finally {
            connection.disconnect();
        }

        Document doc = JsoupEx.parse(response);

        Element link = doc.head().select("link[href~=.*\\.(ico|png|gif|svg)]").first();
        String favicon = (link == null ? null : link.attr("href"));

        if (TextUtils.isEmpty(favicon)) {
            Element meta = doc.head().select("meta[itemprop=image]").first();
            favicon = (meta == null ? null : meta.attr("content"));
        }

        if (!TextUtils.isEmpty(favicon))
            return getFavicon(new URL(base, favicon));

        return null;
    }

    @NonNull
    private static Bitmap getFavicon(URL url) throws IOException {
        Log.i("GET favicon " + url);

        if (!"https".equals(url.getProtocol()))
            throw new FileNotFoundException("http");

        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setReadTimeout(FAVICON_READ_TIMEOUT);
        connection.setConnectTimeout(FAVICON_CONNECT_TIMEOUT);
        connection.setInstanceFollowRedirects(true);
        connection.connect();

        try {
            Bitmap bitmap = BitmapFactory.decodeStream(connection.getInputStream());
            if (bitmap == null)
                throw new FileNotFoundException("decodeStream");
            else
                return bitmap;
        } finally {
            connection.disconnect();
        }
    }

    private static boolean isRecoverable(Throwable ex, Context context) {
        if (ex instanceof SocketTimeoutException) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = (cm == null ? null : cm.getActiveNetworkInfo());
            return (ni == null || !ni.isConnected());
        }

        return !(ex instanceof ConnectException ||
                (ex instanceof UnknownHostException &&
                        ex.getMessage() != null &&
                        ex.getMessage().contains("No address associated with hostname")) ||
                ex instanceof FileNotFoundException ||
                ex instanceof SSLPeerUnverifiedException ||
                (ex instanceof SSLException &&
                        "Unable to parse TLS packet header".equals(ex.getMessage())) ||
                (ex instanceof SSLHandshakeException &&
                        ("connection closed".equals(ex.getMessage())) ||
                        "Connection closed by peer".equals(ex.getMessage())) ||
                (ex instanceof SSLHandshakeException &&
                        ex.getMessage() != null &&
                        (ex.getMessage().contains("usually a protocol error") ||
                                ex.getMessage().contains("Unacceptable certificate"))) ||
                (ex instanceof SSLHandshakeException &&
                        (ex.getCause() instanceof SSLProtocolException ||
                                ex.getCause() instanceof CertificateException ||
                                ex.getCause() instanceof CertPathValidatorException)));
    }

    static void init(final Context context) {
        if (Helper.hasPermission(context, Manifest.permission.READ_CONTACTS)) {
            Handler handler = new Handler(Looper.getMainLooper());

            ContentObserver observer = new ContentObserver(handler) {
                @Override
                public void onChange(boolean selfChange, Uri uri) {
                    Log.i("Contact changed uri=" + uri);
                    executorLookup.submit(new Runnable() {
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

            executorLookup.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        emailLookup = getEmailLookup(context);
                    } catch (Throwable ex) {
                        Log.e(ex);
                    }
                }
            });

            try {
                Uri uri = ContactsContract.CommonDataKinds.Email.CONTENT_URI;
                Log.i("Observing uri=" + uri);
                context.getContentResolver().registerContentObserver(uri, true, observer);
            } catch (SecurityException ex) {
                Log.w(ex);
                /*
                    Should never happen, but:
                    Caused by: android.os.RemoteException:
                      at com.android.server.content.ContentService.registerContentObserver (ContentService.java:340)
                      at android.content.IContentService$Stub.onTransact (IContentService.java:76)
                      at com.android.server.content.ContentService.onTransact (ContentService.java:262)
                      at android.os.Binder.execTransact (Binder.java:731)
                 */
            }
        }
    }

    static Uri getLookupUri(Address[] addresses) {
        if (addresses == null)
            return null;

        for (Address from : addresses) {
            String email = ((InternetAddress) from).getAddress();
            if (!TextUtils.isEmpty(email)) {
                Lookup lookup = emailLookup.get(email.toLowerCase(Locale.ROOT));
                if (lookup != null)
                    return lookup.uri;
            }
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
            if (!TextUtils.isEmpty(email)) {
                Lookup lookup = emailLookup.get(email.toLowerCase(Locale.ROOT));
                if (lookup != null &&
                        (TextUtils.isEmpty(personal) || prefer_contact))
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
                    all.put(email.toLowerCase(Locale.ROOT), lookup);
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