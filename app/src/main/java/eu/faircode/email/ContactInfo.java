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

    Copyright 2018-2021 by Marcel Bokhorst (M66B)
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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONObject;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLProtocolException;
import javax.net.ssl.SSLSession;

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

    private static final int GENERATED_ICON_SIZE = 96; // dp
    private static final int FAVICON_ICON_SIZE = 64; // dp
    private static final int GRAVATAR_TIMEOUT = 5 * 1000; // milliseconds
    private static final int FAVICON_CONNECT_TIMEOUT = 5 * 1000; // milliseconds
    private static final int FAVICON_READ_TIMEOUT = 10 * 1000; // milliseconds
    private static final int FAVICON_READ_BYTES = 4096;
    private static final long CACHE_CONTACT_DURATION = 2 * 60 * 1000L; // milliseconds
    private static final long CACHE_GRAVATAR_DURATION = 2 * 60 * 60 * 1000L; // milliseconds
    private static final long CACHE_FAVICON_DURATION = 2 * 7 * 24 * 60 * 60 * 1000L; // milliseconds

    // https://css-tricks.com/prefetching-preloading-prebrowsing/
    // https://developer.mozilla.org/en-US/docs/Web/Performance/dns-prefetch
    private static final List<String> REL_EXCLUDE = Collections.unmodifiableList(Arrays.asList(
            "dns-prefetch", "preconnect", "prefetch", "preload", "prerender", "subresource",
            "respond-redirect"
    ));

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

    static void cleanup(Context context) {
        long now = new Date().getTime();

        // Favicons
        Log.i("Cleanup favicons");
        File[] favicons = new File(context.getCacheDir(), "favicons").listFiles();
        if (favicons != null)
            for (File file : favicons)
                if (file.lastModified() + CACHE_FAVICON_DURATION < now) {
                    Log.i("Deleting " + file);
                    if (!file.delete())
                        Log.w("Error deleting " + file);
                }
    }

    static void clearCache(Context context) {
        clearCache(context, true);
    }

    static void clearCache(Context context, boolean files) {
        synchronized (emailContactInfo) {
            emailContactInfo.clear();
        }

        synchronized (emailGravatar) {
            emailGravatar.clear();
        }

        if (!files)
            return;

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
        if (info.bitmap == null && gravatars && !BuildConfig.PLAY_STORE_RELEASE) {
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
                        URL url = new URL(BuildConfig.GRAVATAR_URI + hash + "?d=404");
                        Log.i("Gravatar key=" + gkey + " url=" + url);

                        urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setRequestMethod("GET");
                        urlConnection.setDoOutput(false);
                        urlConnection.setReadTimeout(GRAVATAR_TIMEOUT);
                        urlConnection.setConnectTimeout(GRAVATAR_TIMEOUT);
                        urlConnection.setRequestProperty("User-Agent", WebViewEx.getUserAgent(context));
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
                        final int scaleToPixels = Helper.dp2pixels(context, FAVICON_ICON_SIZE);

                        List<Future<Bitmap>> futures = new ArrayList<>();

                        String host = domain;
                        while (host.indexOf('.') > 0) {
                            final URL base = new URL("https://" + host);
                            final URL www = new URL("https://www." + host);

                            futures.add(executorFavicon.submit(new Callable<Bitmap>() {
                                @Override
                                public Bitmap call() throws Exception {
                                    return parseFavicon(base, scaleToPixels, context);
                                }
                            }));

                            futures.add(executorFavicon.submit(new Callable<Bitmap>() {
                                @Override
                                public Bitmap call() throws Exception {
                                    return parseFavicon(www, scaleToPixels, context);
                                }
                            }));

                            futures.add(executorFavicon.submit(new Callable<Bitmap>() {
                                @Override
                                public Bitmap call() throws Exception {
                                    return getFavicon(new URL(base, "favicon.ico"), null, scaleToPixels, context);
                                }
                            }));

                            futures.add(executorFavicon.submit(new Callable<Bitmap>() {
                                @Override
                                public Bitmap call() throws Exception {
                                    return getFavicon(new URL(www, "favicon.ico"), null, scaleToPixels, context);
                                }
                            }));

                            int dot = host.indexOf('.');
                            host = host.substring(dot + 1);
                        }

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
                        if (ex instanceof FileNotFoundException ||
                                ex instanceof CertPathValidatorException)
                            Log.i(ex);
                        else
                            Log.e(ex);
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
            int dp = Helper.dp2pixels(context, GENERATED_ICON_SIZE);
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

    private static Bitmap parseFavicon(URL base, int scaleToPixels, Context context) throws IOException {
        Log.i("PARSE favicon " + base);
        HttpsURLConnection connection = (HttpsURLConnection) base.openConnection();
        connection.setRequestMethod("GET");
        connection.setReadTimeout(FAVICON_READ_TIMEOUT);
        connection.setConnectTimeout(FAVICON_CONNECT_TIMEOUT);
        connection.setInstanceFollowRedirects(true);
        connection.setHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
        connection.setRequestProperty("User-Agent", WebViewEx.getUserAgent(context));
        connection.connect();

        String response;
        try {
            byte[] buffer = new byte[FAVICON_READ_BYTES];
            int len = 0;
            while (len < buffer.length) {
                int read = connection.getInputStream().read(buffer, len, buffer.length - len);
                if (read < 0)
                    break;
                else
                    len += read;
            }
            if (len < 0)
                throw new IOException("length");
            response = new String(buffer, 0, len, StandardCharsets.UTF_8.name());
        } finally {
            connection.disconnect();
        }

        Document doc = JsoupEx.parse(response);

        // Use canonical address
        Element canonical = doc.head().select("link[rel=canonical]").first();
        if (canonical != null) {
            String href = canonical.attr("href");
            if (!TextUtils.isEmpty(href))
                base = new URL(href);
        }

        // https://en.wikipedia.org/wiki/Favicon
        Elements imgs = new Elements();
        imgs.addAll(doc.head().select("link[href~=.+\\.(ico|png|gif|svg)]"));
        imgs.addAll(doc.head().select("meta[itemprop=image]"));

        // https://developer.mozilla.org/en-US/docs/Web/Manifest/icons
        if (imgs.size() == 0 || BuildConfig.DEBUG)
            for (Element manifest : doc.head().select("link[rel=manifest]"))
                try {
                    String href = manifest.attr("href");
                    if (TextUtils.isEmpty(href))
                        continue;

                    URL url = new URL(base, href);
                    Log.i("GET favicon manifest " + url);

                    HttpsURLConnection m = (HttpsURLConnection) url.openConnection();
                    m.setRequestMethod("GET");
                    m.setReadTimeout(FAVICON_READ_TIMEOUT);
                    m.setConnectTimeout(FAVICON_CONNECT_TIMEOUT);
                    m.setInstanceFollowRedirects(true);
                    m.setHostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    });
                    m.setRequestProperty("User-Agent", WebViewEx.getUserAgent(context));
                    m.connect();

                    try {
                        String json = Helper.readStream(m.getInputStream());
                        JSONObject jroot = new JSONObject(json);
                        JSONArray jicons = jroot.getJSONArray("icons");
                        for (int i = 0; i < jicons.length(); i++) {
                            JSONObject jicon = jicons.getJSONObject(i);
                            String src = jicon.getString("src");
                            String sizes = jicon.optString("sizes", "");
                            String type = jicon.optString("type", "");
                            if (!TextUtils.isEmpty(src)) {
                                Element img = doc.createElement("link")
                                        .attr("href", src)
                                        .attr("sizes", sizes)
                                        .attr("type", type);
                                imgs.add(img);
                            }
                        }
                    } finally {
                        m.disconnect();
                    }
                } catch (Throwable ex) {
                    Log.w(ex);
                }

        Collections.sort(imgs, new Comparator<Element>() {
            @Override
            public int compare(Element img1, Element img2) {
                boolean l1 = "link".equals(img1.tagName());
                boolean l2 = "link".equals(img2.tagName());
                int l = Boolean.compare(l1, l2);
                if (l != 0)
                    return -l;

                boolean i1 = "icon".equalsIgnoreCase(img1.attr("rel")
                        .replace("shortcut", "").trim());
                boolean i2 = "icon".equalsIgnoreCase(img2.attr("rel")
                        .replace("shortcut", "").trim());
                int i = Boolean.compare(i1, i2);
                if (i != 0)
                    return -i;

                int t1 = (img1.attr("href").toLowerCase(Locale.ROOT).endsWith("ico") ? 1 : -1);
                int t2 = (img2.attr("href").toLowerCase(Locale.ROOT).endsWith("ico") ? 1 : -1);
                int t = Integer.compare(t1, t2);
                if (t != 0)
                    return t;

                int s1 = getSize(img1.attr("sizes"));
                int s2 = getSize(img2.attr("sizes"));
                return Integer.compare(
                        Math.abs(s1 - scaleToPixels),
                        Math.abs(s2 - scaleToPixels));
            }
        });

        for (int i = 0; i < imgs.size(); i++)
            Log.i("Favicon " + i + "=" + imgs.get(i) + " @" + base);

        List<Future<Bitmap>> futures = new ArrayList<>();
        for (Element img : imgs) {
            String rel = img.attr("rel").trim().toLowerCase(Locale.ROOT);
            if (REL_EXCLUDE.contains(rel)) // dns-prefetch: gmx.net
                continue;

            String favicon = ("link".equals(img.tagName())
                    ? img.attr("href")
                    : img.attr("content"));
            if (TextUtils.isEmpty(favicon))
                continue;

            final URL url = new URL(base, favicon);
            futures.add(executorFavicon.submit(new Callable<Bitmap>() {
                @Override
                public Bitmap call() throws Exception {
                    return getFavicon(url, img.attr("type"), scaleToPixels, context);
                }
            }));
        }

        for (Future<Bitmap> future : futures)
            try {
                return future.get();
            } catch (Throwable ex) {
                if (ex.getCause() instanceof FileNotFoundException ||
                        ex.getCause() instanceof CertPathValidatorException)
                    Log.i(ex);
                else
                    Log.e(ex);
            }

        return null;
    }

    private static int getSize(String sizes) {
        int max = 0;
        for (String size : sizes.split(" ")) {
            int min = Integer.MAX_VALUE;
            for (String p : size.trim().split("[x|X]"))
                try {
                    int x = Integer.parseInt(p);
                    if (x < min)
                        min = x;
                } catch (NumberFormatException ex) {
                    Log.w(ex);
                }
            if (min != Integer.MAX_VALUE && min > max)
                max = min;
        }

        return max;
    }

    @NonNull
    private static Bitmap getFavicon(URL url, String type, int scaleToPixels, Context context) throws IOException {
        Log.i("GET favicon " + url);

        if (!"https".equals(url.getProtocol()))
            throw new FileNotFoundException("http");

        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setReadTimeout(FAVICON_READ_TIMEOUT);
        connection.setConnectTimeout(FAVICON_CONNECT_TIMEOUT);
        connection.setInstanceFollowRedirects(true);
        connection.setHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
        connection.setRequestProperty("User-Agent", WebViewEx.getUserAgent(context));
        connection.connect();

        try {
            int status = connection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK)
                throw new FileNotFoundException("Error " + status + ":" + connection.getResponseMessage());

            if ("image/svg+xml".equals(type) || url.getPath().endsWith(".svg"))
                ; // Android does not support SVG

            Bitmap bitmap = ImageHelper.getScaledBitmap(connection.getInputStream(), url.toString(), scaleToPixels);
            if (bitmap == null)
                throw new FileNotFoundException("decodeStream");
            else {
                Bitmap favicon = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
                favicon.eraseColor(Color.WHITE);
                Canvas canvas = new Canvas(favicon);
                canvas.drawBitmap(bitmap, 0, 0, null);
                bitmap.recycle();
                return favicon;
            }
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
            ContentObserver observer = new ContentObserver(ApplicationEx.getMainHandler()) {
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

    static Address[] fillIn(Address[] addresses, boolean prefer_contact, boolean only_contact) {
        if (addresses == null)
            return null;

        Address[] modified = new Address[addresses.length];
        for (int i = 0; i < addresses.length; i++) {
            InternetAddress address = (InternetAddress) addresses[i];
            String email = address.getAddress();
            String personal = (only_contact ? null : address.getPersonal());
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

    static void update(
            Context context, EntityAccount account, final EntityFolder folder, final EntityMessage message) {
        long sync_time = (folder.sync_days == Integer.MAX_VALUE ? 0 : folder.sync_days) * 24 * 3600 * 1000L;
        if (message.received < account.created - sync_time)
            return;

        if (EntityFolder.DRAFTS.equals(folder.type) ||
                EntityFolder.ARCHIVE.equals(folder.type) ||
                EntityFolder.TRASH.equals(folder.type) ||
                EntityFolder.JUNK.equals(folder.type))
            return;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean suggest_sent = prefs.getBoolean("suggest_sent", true);
        boolean suggest_received = prefs.getBoolean("suggest_received", false);
        if (!suggest_sent && !suggest_received)
            return;

        DB db = DB.getInstance(context);

        int type = (folder.isOutgoing() ? EntityContact.TYPE_TO : EntityContact.TYPE_FROM);

        // Check if from self
        if (type == EntityContact.TYPE_FROM) {
            if (message.from != null) {
                List<EntityIdentity> identities = Core.getIdentities(folder.account, context);
                if (identities != null) {
                    for (Address sender : message.from) {
                        for (EntityIdentity identity : identities)
                            if (identity.similarAddress(sender)) {
                                type = EntityContact.TYPE_TO;
                                break;
                            }
                        if (type == EntityContact.TYPE_TO)
                            break;
                    }
                }
            }
        }

        if (type == EntityContact.TYPE_TO && !suggest_sent)
            return;
        if (type == EntityContact.TYPE_FROM && !suggest_received)
            return;

        List<Address> addresses = new ArrayList<>();
        if (type == EntityContact.TYPE_FROM) {
            if (message.reply == null || message.reply.length == 0) {
                if (message.from != null)
                    addresses.addAll(Arrays.asList(message.from));
            } else
                addresses.addAll(Arrays.asList(message.reply));
        } else if (type == EntityContact.TYPE_TO) {
            if (message.to != null)
                addresses.addAll(Arrays.asList(message.to));
            if (message.cc != null)
                addresses.addAll(Arrays.asList(message.cc));
        }

        for (Address address : addresses) {
            String email = ((InternetAddress) address).getAddress();
            String name = ((InternetAddress) address).getPersonal();
            Uri avatar = ContactInfo.getLookupUri(new Address[]{address});

            if (TextUtils.isEmpty(email))
                continue;
            if (TextUtils.isEmpty(name))
                name = null;

            try {
                db.beginTransaction();

                EntityContact contact = db.contact().getContact(folder.account, type, email);
                if (contact == null) {
                    contact = new EntityContact();
                    contact.account = folder.account;
                    contact.type = type;
                    contact.email = email;
                    contact.name = name;
                    contact.avatar = (avatar == null ? null : avatar.toString());
                    contact.times_contacted = 1;
                    contact.first_contacted = message.received;
                    contact.last_contacted = message.received;
                    contact.id = db.contact().insertContact(contact);
                    Log.i("Inserted contact=" + contact + " type=" + type);
                } else {
                    if (contact.name == null && name != null)
                        contact.name = name;
                    contact.avatar = (avatar == null ? null : avatar.toString());
                    contact.times_contacted++;
                    contact.first_contacted = Math.min(contact.first_contacted, message.received);
                    contact.last_contacted = message.received;
                    db.contact().updateContact(contact);
                    Log.i("Updated contact=" + contact + " type=" + type);
                }

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }
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
