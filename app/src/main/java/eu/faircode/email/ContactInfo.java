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
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
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
    private String type; // contact, vmc, favicon, identicon, letter, unknown
    private boolean verified;
    private String displayName;
    private Uri lookupUri;
    private boolean known;
    private long time;

    static final int FAVICON_READ_BYTES = 50 * 1024;

    private static final Object lock = new Object();
    private static ContactInfo anonymous = null;
    private static Map<String, Lookup> emailLookup = new ConcurrentHashMap<>();
    private static final Map<String, ContactInfo> emailContactInfo = new HashMap<>();

    private static final int CONTACT_ICON_SIZE = 64; // dp
    private static final int GENERATED_ICON_SIZE = 48; // dp
    private static final int FAVICON_ICON_SIZE = 64; // dp
    private static final int FAVICON_CONNECT_TIMEOUT = 5 * 1000; // milliseconds
    private static final int FAVICON_READ_TIMEOUT = 10 * 1000; // milliseconds
    private static final long CACHE_CONTACT_DURATION = 2 * 60 * 1000L; // milliseconds
    private static final long CACHE_FAVICON_DURATION = 2 * 7 * 24 * 60 * 60 * 1000L; // milliseconds
    private static final float MIN_FAVICON_LUMINANCE = 0.2f;

    static final int AVATAR_SIZE = FAVICON_ICON_SIZE;

    // https://realfavicongenerator.net/faq
    private static final String[] FIXED_FAVICONS = new String[]{
            "apple-touch-icon.png", // 57x57
            "apple-touch-icon-precomposed.png", // 57x57
            "favicon.ico"
    };

    // https://css-tricks.com/prefetching-preloading-prebrowsing/
    // https://developer.mozilla.org/en-US/docs/Web/Performance/dns-prefetch
    private static final List<String> REL_EXCLUDE = Collections.unmodifiableList(Arrays.asList(
            "dns-prefetch", "preconnect", "prefetch", "preload", "prerender", "subresource",
            "respond-redirect"
    ));

    private ContactInfo() {
    }

    String getType() {
        return type;
    }

    boolean isEmailBased() {
        return ("gravatar".equals(type) || "libravatar".equals(type));
    }

    boolean isVerified() {
        return (bitmap != null && verified);
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
        Log.i("Cleanup avatars");
        for (String type : new String[]{"notcontact", "favicons", "generated"}) {
            File[] favicons = Helper.ensureExists(context, type).listFiles();
            if (favicons != null)
                for (File file : favicons)
                    if (file.lastModified() + CACHE_FAVICON_DURATION < now) {
                        Log.i("Deleting " + file);
                        Helper.secureDelete(file);
                    }
        }
    }

    static void clearCache(Context context) {
        clearCache(context, true);
    }

    static void clearCache(Context context, boolean files) {
        synchronized (emailContactInfo) {
            emailContactInfo.clear();
        }

        if (!files)
            return;

        for (String type : new String[]{"notcontact", "favicons", "generated"}) {
            final File dir = Helper.ensureExists(context, type);
            Helper.getParallelExecutor().submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        File[] favicons = dir.listFiles();
                        if (favicons != null)
                            for (File favicon : favicons)
                                Helper.secureDelete(favicon);
                    } catch (Throwable ex) {
                        Log.w(ex);
                    }
                }
            });
        }
    }

    @NonNull
    static ContactInfo[] get(Context context, long account, String folderType, String selector, boolean dmarc, Address[] addresses) {
        return get(context, account, folderType, selector, dmarc, addresses, false);
    }

    static ContactInfo[] getCached(Context context, long account, String folderType, String selector, boolean dmarc, Address[] addresses) {
        return get(context, account, folderType, selector, dmarc, addresses, true);
    }

    private static ContactInfo[] get(Context context, long account, String folderType, String selector, boolean dmarc, Address[] addresses, boolean cacheOnly) {
        if (addresses == null || addresses.length == 0) {
            ContactInfo anonymous = getAnonymous(context);
            return new ContactInfo[]{anonymous == null ? new ContactInfo() : anonymous};
        }

        ContactInfo[] result = new ContactInfo[addresses.length];
        for (int i = 0; i < addresses.length; i++) {
            result[i] = _get(context, account, folderType, selector, dmarc, (InternetAddress) addresses[i], cacheOnly);
            if (result[i] == null) {
                if (cacheOnly)
                    return null;
                ContactInfo anonymous = getAnonymous(context);
                if (anonymous == null)
                    return null;
                result[i] = anonymous;
            } else if (result[i].bitmap == null) {
                if (!cacheOnly) {
                    ContactInfo anonymous = getAnonymous(context);
                    result[i].bitmap = (anonymous == null ? null : anonymous.bitmap);
                }
            }
        }

        return result;
    }

    private static ContactInfo _get(
            Context context,
            long account, String folderType,
            String selector, boolean dmarc, InternetAddress address, boolean cacheOnly) {
        String key = MessageHelper.formatAddresses(new Address[]{address});
        synchronized (emailContactInfo) {
            ContactInfo info = emailContactInfo.get(key);
            if (info != null && !info.isExpired())
                return info;
        }

        if (cacheOnly)
            return null;

        boolean cached = false;
        ContactInfo info = new ContactInfo();
        info.email = address.getAddress();

        final String ekey = (TextUtils.isEmpty(info.email) ? null : getKey(info.email));

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean favicons_dmarc = prefs.getBoolean("favicons_dmarc", false);
        boolean avatars = prefs.getBoolean("avatars", true);
        boolean prefer_contact = prefs.getBoolean("prefer_contact", false);
        boolean distinguish_contacts = prefs.getBoolean("distinguish_contacts", false);
        boolean bimi = (prefs.getBoolean("bimi", false) && (!favicons_dmarc || dmarc) && !BuildConfig.PLAY_STORE_RELEASE);
        boolean gravatars = (prefs.getBoolean("gravatars", false) && (!favicons_dmarc || dmarc) && !BuildConfig.PLAY_STORE_RELEASE);
        boolean libravatars = (prefs.getBoolean("libravatars", false) && (!favicons_dmarc || dmarc) && !BuildConfig.PLAY_STORE_RELEASE);
        boolean favicons = (prefs.getBoolean("favicons", false) && (!favicons_dmarc || dmarc));
        boolean ddg_icons = (prefs.getBoolean("ddg_icons", false) && (!favicons_dmarc || dmarc) && !BuildConfig.PLAY_STORE_RELEASE);
        String favicon_uri = prefs.getString("favicon_uri", null);
        boolean generated = prefs.getBoolean("generated_icons", true);
        boolean identicons = prefs.getBoolean("identicons", false);
        boolean circular = prefs.getBoolean("circular", true);

        // Contact photo
        if (!TextUtils.isEmpty(info.email) &&
                (avatars || prefer_contact || distinguish_contacts) &&
                Helper.hasPermission(context, Manifest.permission.READ_CONTACTS)) {
            File dir = Helper.ensureExists(context, "notcontact");
            File file = new File(dir, ekey);
            if (file.exists()) {
                file.setLastModified(new Date().getTime());
                Log.i("Contact negative cache key=" + ekey);
            } else {
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
                        null, null, ContactsContract.Contacts.DISPLAY_NAME)) {

                    if (cursor != null && cursor.moveToNext()) {
                        int colContactId = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Photo.CONTACT_ID);
                        int colLookupKey = cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY);
                        int colDisplayName = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);

                        long contactId = cursor.getLong(colContactId);
                        String lookupKey = cursor.getString(colLookupKey);
                        Uri lookupUri = ContactsContract.Contacts.getLookupUri(contactId, lookupKey);
                        int px = Helper.dp2pixels(context, CONTACT_ICON_SIZE);

                        if (avatars)
                            try (InputStream is = ContactsContract.Contacts.openContactPhotoInputStream(
                                    resolver, lookupUri, false)) {
                                info.bitmap = ImageHelper.getScaledBitmap(is, lookupUri.toString(), null, px);
                                info.type = "contact";
                            } catch (Throwable ex) {
                                Log.e(ex);
                            }

                        info.displayName = cursor.getString(colDisplayName);
                        info.lookupUri = lookupUri;
                        info.known = true;
                    } else {
                        file.createNewFile();
                        Log.i("Contact negative cached key=" + ekey);
                    }
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }
        }

        // Favicon
        if (info.bitmap == null &&
                (bimi || gravatars || libravatars || favicons) &&
                !EntityFolder.JUNK.equals(folderType)) {
            String d = UriHelper.getEmailDomain(info.email);
            if (d != null) {
                // Prevent using Doodles
                if ("google.com".equals(d) ||
                        "gmail.com".equals(d) ||
                        "googlemail.com".equals(d))
                    d = "support.google.com";

                // https://yahoo.fr/co.uk redirects unsafely to http://fr.yahoo.com/favicon.ico
                for (String yahoo : d.split("\\."))
                    if ("yahoo".equals(yahoo)) {
                        d = "yahoo.com";
                        break;
                    }

                final String domain = d.toLowerCase(Locale.ROOT);

                File dir = Helper.ensureExists(context, "favicons");

                try {
                    // check cache
                    File[] files = null;
                    if (gravatars) {
                        File f = new File(dir, ekey + ".gravatar");
                        if (f.exists())
                            files = new File[]{f};
                    }
                    if (files == null && libravatars) {
                        File f = new File(dir, ekey + ".libravatar");
                        if (f.exists())
                            files = new File[]{f};
                    }
                    if (files == null)
                        files = dir.listFiles(new FilenameFilter() {
                            @Override
                            public boolean accept(File file, String name) {
                                return name.startsWith(domain);
                            }
                        });
                    if (files != null && files.length == 1) {
                        files[0].setLastModified(new Date().getTime());
                        if (files[0].length() == 0)
                            Log.i("Avatar blacklisted cache" + files[0].getName());
                        else {
                            Log.i("Avatar from cache=" + files[0].getName());
                            String ext = Helper.getExtension(files[0].getName());
                            String[] data = (ext == null ? null : ext.split("_"));
                            info.bitmap = BitmapFactory.decodeFile(files[0].getAbsolutePath());
                            if (data != null) {
                                info.type = (data.length > 0 ? data[0] : "unknown");
                                info.verified = (data.length > 1 && "verified".equals(data[1]));
                            }
                            cached = true;
                        }
                    } else {
                        final int scaleToPixels = Helper.dp2pixels(context, FAVICON_ICON_SIZE);

                        List<Future<Favicon>> futures = new ArrayList<>();

                        if (bimi)
                            futures.add(Helper.getDownloadTaskExecutor().submit(new Callable<Favicon>() {
                                @Override
                                public Favicon call() throws Exception {
                                    Pair<Bitmap, Boolean> bimi =
                                            Bimi.get(context, domain, selector, scaleToPixels);
                                    return (bimi == null ? null : new Favicon(bimi.first, "vmc", bimi.second));
                                }
                            }));

                        String email = info.email.toLowerCase(Locale.ROOT);
                        if (gravatars)
                            futures.add(Helper.getDownloadTaskExecutor()
                                    .submit(Avatar.getGravatar(email, scaleToPixels, context)));
                        if (libravatars)
                            futures.add(Helper.getDownloadTaskExecutor()
                                    .submit(Avatar.getLibravatar(email, scaleToPixels, context)));

                        if (favicons) {
                            String host = domain;
                            while (host.indexOf('.') > 0) {
                                final URL base = new URL("https://" + host);
                                futures.add(Helper.getDownloadTaskExecutor().submit(new Callable<Favicon>() {
                                    @Override
                                    public Favicon call() throws Exception {
                                        return parseFavicon(base, scaleToPixels, context);
                                    }
                                }));

                                if (!host.startsWith("www.")) {
                                    final URL www = new URL("https://www." + host);
                                    futures.add(Helper.getDownloadTaskExecutor().submit(new Callable<Favicon>() {
                                        @Override
                                        public Favicon call() throws Exception {
                                            return parseFavicon(www, scaleToPixels, context);
                                        }
                                    }));
                                }

                                int dot = host.indexOf('.');
                                host = host.substring(dot + 1);
                            }

                            host = domain;
                            while (host.indexOf('.') > 0) {
                                final URL base = new URL("https://" + host);
                                for (String name : FIXED_FAVICONS)
                                    futures.add(Helper.getDownloadTaskExecutor().submit(new Callable<Favicon>() {
                                        @Override
                                        public Favicon call() throws Exception {
                                            return getFavicon(new URL(base, name), null, scaleToPixels, context);
                                        }
                                    }));

                                if (!host.startsWith("www.")) {
                                    final URL www = new URL("https://www." + host);
                                    for (String name : FIXED_FAVICONS)
                                        futures.add(Helper.getDownloadTaskExecutor().submit(new Callable<Favicon>() {
                                            @Override
                                            public Favicon call() throws Exception {
                                                return getFavicon(new URL(www, name), null, scaleToPixels, context);
                                            }
                                        }));
                                }

                                int dot = host.indexOf('.');
                                host = host.substring(dot + 1);
                            }
                        }

                        if (ddg_icons && !TextUtils.isEmpty(Avatar.DDG_URI))
                            futures.add(Helper.getDownloadTaskExecutor().submit(new Callable<Favicon>() {
                                @Override
                                public Favicon call() throws Exception {
                                    String parent = UriHelper.getRootDomain(context, domain);
                                    String uri = Avatar.DDG_URI + Uri.encode(parent) + ".ico";
                                    Favicon ddg = getFavicon(new URL(uri), null, scaleToPixels, context);
                                    ddg.type = "ddg";
                                    return ddg;
                                }
                            }));

                        if (!TextUtils.isEmpty(favicon_uri) && (!favicons_dmarc || dmarc))
                            futures.add(Helper.getDownloadTaskExecutor().submit(new Callable<Favicon>() {
                                @Override
                                public Favicon call() throws Exception {
                                    String parent = UriHelper.getRootDomain(context, domain);
                                    String uri = favicon_uri.replace("{domain}", Uri.encode(parent));
                                    Log.i("Favicon uri=" + uri);
                                    Favicon alt = getFavicon(new URL(uri), null, scaleToPixels, context);
                                    alt.type = "uri";
                                    return alt;
                                }
                            }));

                        Throwable ex = null;
                        for (Future<Favicon> future : futures)
                            try {
                                Favicon favicon = future.get();
                                Log.i("Using favicon source=" + (favicon == null ? null : favicon.source));

                                if (favicon == null)
                                    continue;

                                float lum = 0; // ImageHelper.getLuminance(favicon.bitmap);
                                if (lum < MIN_FAVICON_LUMINANCE) {
                                    Bitmap bitmap = Bitmap.createBitmap(
                                            favicon.bitmap.getWidth(),
                                            favicon.bitmap.getHeight(),
                                            favicon.bitmap.getConfig());
                                    bitmap.eraseColor(Color.WHITE);
                                    Canvas canvas = new Canvas(bitmap);
                                    canvas.drawBitmap(favicon.bitmap, 0, 0, null);
                                    favicon.bitmap.recycle();
                                    favicon.bitmap = bitmap;
                                }

                                info.bitmap = favicon.bitmap;
                                info.type = favicon.type;
                                info.verified = favicon.verified;
                                break;
                            } catch (ExecutionException exex) {
                                ex = exex.getCause();
                            } catch (Throwable exex) {
                                ex = exex;
                            }

                        if (info.bitmap == null)
                            if (ex == null)
                                throw new FileNotFoundException();
                            else
                                throw ex;

                        // Add to cache
                        File output = new File(dir,
                                (info.isEmailBased() ? ekey : domain) +
                                        "." + info.type +
                                        (info.verified ? "_verified" : ""));
                        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(output))) {
                            info.bitmap.compress(Bitmap.CompressFormat.PNG, 90, os);
                        }
                        Log.i("Avatar to cache=" + output.getName());
                    }
                } catch (Throwable ex) {
                    if (isRecoverable(ex, context))
                        Log.i(ex);
                    else {
                        if (ex instanceof FileNotFoundException ||
                                ex instanceof CertificateException ||
                                ex instanceof CertPathValidatorException ||
                                ex.getCause() instanceof CertPathValidatorException ||
                                ex.getCause() instanceof CertificateException ||
                                ex instanceof SSLHandshakeException ||
                                (ex instanceof SSLException &&
                                        "Unable to parse TLS packet header".equals(ex.getMessage())) ||
                                (ex instanceof IOException &&
                                        "Resetting to invalid mark".equals(ex.getMessage())))
                            Log.i(ex);
                        else
                            Log.e(ex);
                        try {
                            new File(dir, domain).createNewFile();
                        } catch (IOException ex1) {
                            Log.e(ex1);
                        }
                    }
                }
            }
        }

        // Generated
        boolean identicon = false;
        String name = address.getPersonal();
        String tag = (TextUtils.isEmpty(info.email) ? name : info.email);
        String etag = (TextUtils.isEmpty(info.email) ? Helper.sanitizeFilename(name + "@name") : ekey);
        if (info.bitmap == null && generated && !TextUtils.isEmpty(tag)) {
            File dir = Helper.ensureExists(context, "generated");
            File[] files = dir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File file, String name) {
                    return name.startsWith(etag);
                }
            });
            if (files != null && files.length == 1) {
                Log.i("Generated from cache=" + files[0].getName());
                files[0].setLastModified(new Date().getTime());
                info.bitmap = BitmapFactory.decodeFile(files[0].getAbsolutePath());
                info.type = Helper.getExtension(files[0].getName());
                cached = true;
            } else {
                int dp = Helper.dp2pixels(context, GENERATED_ICON_SIZE);
                if (identicons) {
                    identicon = true;
                    info.bitmap = ImageHelper.generateIdenticon(
                            tag, dp, 5, context);
                    info.type = "identicon";
                } else {
                    info.bitmap = ImageHelper.generateLetterIcon(
                            tag, address.getPersonal(), dp, context);
                    info.type = "letter";
                }

                // Add to cache
                File output = new File(dir, etag + "." + info.type);
                try (OutputStream os = new BufferedOutputStream(new FileOutputStream(output))) {
                    info.bitmap.compress(Bitmap.CompressFormat.PNG, 90, os);
                } catch (IOException ex) {
                    Log.e(ex);
                }
                Log.i("Generated to cache=" + output.getName());
            }
        }

        info.bitmap = ImageHelper.makeCircular(info.bitmap,
                circular && !identicon ? null : Helper.dp2pixels(context, 3));

        if (info.displayName == null)
            info.displayName = address.getPersonal();

        if (!info.known && !TextUtils.isEmpty(info.email))
            try {
                DB db = DB.getInstance(context);
                EntityContact contact = db.contact().getContact(account, EntityContact.TYPE_TO, info.email);
                info.known = (contact != null);
            } catch (Throwable ex) {
                Log.e(ex);
            }

        if (info.bitmap != null) {
            int abc = info.bitmap.getAllocationByteCount();
            if (!cached && (abc > 1024 * 1024 || BuildConfig.DEBUG)) {
                String msg = "Avatar type=" + info.type +
                        " domain=" + UriHelper.getEmailDomain(info.email) +
                        " size=" + Helper.humanReadableByteCount(abc) +
                        "/" + Helper.humanReadableByteCount(info.bitmap.getByteCount()) +
                        " " + info.bitmap.getWidth() + "x" + info.bitmap.getHeight() + " " + info.bitmap.getConfig() +
                        " play=" + BuildConfig.PLAY_STORE_RELEASE;
                if (!BuildConfig.DEBUG)
                    Log.e(msg);
                EntityLog.log(context, EntityLog.Type.Debug4, msg);
            }
        }

        synchronized (emailContactInfo) {
            emailContactInfo.put(key, info);
        }

        info.time = new Date().getTime();
        return info;
    }

    private static ContactInfo getAnonymous(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean avatars = prefs.getBoolean("avatars", true);
        boolean bimi = (prefs.getBoolean("bimi", false) && !BuildConfig.PLAY_STORE_RELEASE);
        boolean gravatars = (prefs.getBoolean("gravatars", false) && !BuildConfig.PLAY_STORE_RELEASE);
        boolean libravatars = (prefs.getBoolean("libravatars", false) && !BuildConfig.PLAY_STORE_RELEASE);
        boolean favicons = prefs.getBoolean("favicons", false);
        boolean generated = prefs.getBoolean("generated_icons", true);
        boolean identicons = prefs.getBoolean("identicons", false);
        if (avatars || bimi || gravatars || libravatars || favicons || generated || identicons) {
            synchronized (lock) {
                if (anonymous == null) {
                    Drawable d = context.getDrawable(R.drawable.twotone_person_24);
                    Bitmap bitmap = Bitmap.createBitmap(d.getIntrinsicWidth(), d.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    d.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                    d.setTint(Helper.resolveColor(context, R.attr.colorSeparator));
                    d.draw(canvas);

                    anonymous = new ContactInfo();
                    anonymous.bitmap = bitmap;
                }
                return anonymous;
            }
        }

        return null;
    }

    private static Favicon parseFavicon(URL base, int scaleToPixels, Context context) throws IOException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean favicons_partial = prefs.getBoolean("favicons_partial", true);
        boolean favicons_manifest = prefs.getBoolean("favicons_manifest", false);

        Log.i("PARSE favicon " + base);
        HttpURLConnection connection = ConnectionHelper
                .openConnectionUnsafe(context, base, FAVICON_CONNECT_TIMEOUT, FAVICON_READ_TIMEOUT);

        Document doc;
        try {
            Log.i("Favicon partial=" + favicons_partial);
            if (favicons_partial) {
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
                doc = JsoupEx.parse(new String(buffer, 0, len, StandardCharsets.UTF_8.name()));
            } else
                doc = JsoupEx.parse(connection.getInputStream(), StandardCharsets.UTF_8.name(), base.toString());
        } finally {
            connection.disconnect();
        }

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
        if (imgs.size() == 0 || favicons_manifest)
            for (Element manifest : doc.head().select("link[rel=manifest]"))
                try {
                    String href = manifest.attr("href");
                    if (TextUtils.isEmpty(href))
                        continue;

                    URL url = new URL(base, href);
                    if (!"https".equals(url.getProtocol()))
                        throw new FileNotFoundException(url.toString());
                    Log.i("GET favicon manifest " + url);

                    HttpsURLConnection m = (HttpsURLConnection) url.openConnection();
                    m.setRequestMethod("GET");
                    m.setReadTimeout(FAVICON_READ_TIMEOUT);
                    m.setConnectTimeout(FAVICON_CONNECT_TIMEOUT);
                    m.setInstanceFollowRedirects(true);
                    ConnectionHelper.setUserAgent(context, m);
                    m.connect();

                    try {
                        String json = Helper.readStream(m.getInputStream());
                        JSONObject jroot = new JSONObject(json);
                        if (jroot.has("icons")) {
                            JSONArray jicons = jroot.getJSONArray("icons");
                            for (int i = 0; i < jicons.length(); i++) {
                                JSONObject jicon = jicons.getJSONObject(i);
                                String src = jicon.optString("src");
                                String sizes = jicon.optString("sizes", "");
                                String type = jicon.optString("type", "");
                                String purpose = jicon.optString("purpose", "");
                                if (!TextUtils.isEmpty(src)) {
                                    Element img = doc.createElement("link")
                                            .attr("rel", "manifest")
                                            .attr("href", src)
                                            .attr("sizes", sizes)
                                            .attr("type", type)
                                            .attr("purpose", purpose);
                                    imgs.add(img);
                                }
                            }
                        }
                    } finally {
                        m.disconnect();
                    }
                } catch (Throwable ex) {
                    Log.w(ex);
                }

        // https://docs.microsoft.com/en-us/previous-versions/windows/internet-explorer/ie-developer/platform-apis/dn320426(v=vs.85)
        /*
        if (imgs.size() == 0 || BuildConfig.DEBUG) {
            String cfg = "/browserconfig.xml";
            Element meta = doc.head().select("meta[name=msapplication-config]").first();
            if (meta != null) {
                String content = meta.attr("content");
                if (!TextUtils.isEmpty(content))
                    cfg = content;
            }
            try {
                URL url = new URL(base, cfg);
                if (!"https".equals(url.getProtocol()))
                    throw new FileNotFoundException(url.toString());
                Log.i("GET browserconfig " + url);

                HttpsURLConnection m = (HttpsURLConnection) url.openConnection();
                m.setRequestMethod("GET");
                m.setReadTimeout(FAVICON_READ_TIMEOUT);
                m.setConnectTimeout(FAVICON_CONNECT_TIMEOUT);
                m.setInstanceFollowRedirects(true);
                m.setRequestProperty("User-Agent", WebViewEx.getUserAgent(context));
                m.connect();

                try {
                    XmlPullParser xml = Xml.newPullParser();
                    xml.setInput(m.getInputStream(), null);
                    int eventType = xml.getEventType();
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        if (eventType == XmlPullParser.START_TAG) {
                            String name = xml.getName();
                            if ("square70x70logo".equals(name) ||
                                    "square150x150logo".equals(name) ||
                                    "square310x310logo".equals(name)) {
                                String src = xml.getAttributeValue(null, "src");
                                if (!TextUtils.isEmpty(src)) {
                                    Element img = doc.createElement("link")
                                            .attr("rel", "browserconfig")
                                            .attr("href", new URL(base, src).toString())
                                            .attr("sizes", name
                                                    .replace("square", "")
                                                    .replace("logo", ""));
                                    Log.i("GOT browserconfig " + img);
                                    imgs.add(img);
                                }
                            }
                        }
                        eventType = xml.next();
                    }
                } finally {
                    m.disconnect();
                }
            } catch (Throwable ex) {
                Log.w(ex);
            }
        }
        */

        String host = base.getHost();

        Collections.sort(imgs, new Comparator<Element>() {
            @Override
            public int compare(Element img1, Element img2) {
                int t1 = getOrder(host, img1);
                int t2 = getOrder(host, img2);
                int t = Integer.compare(t1, t2);
                if (t != 0)
                    return -t;

                int s1 = getSize(img1.attr("sizes"));
                int s2 = getSize(img2.attr("sizes"));
                return Integer.compare(
                        Math.abs(s1 - scaleToPixels),
                        Math.abs(s2 - scaleToPixels));
            }
        });

        Log.i("Favicons " + base + "=" + imgs.size());
        for (int i = 0; i < imgs.size(); i++)
            Log.i("Favicon #" + getOrder(host, imgs.get(i)) + " " + i + "=" + imgs.get(i) + " @" + base);

        for (Element img : imgs) {
            String rel = img.attr("rel").trim().toLowerCase(Locale.ROOT);
            if (REL_EXCLUDE.contains(rel)) // dns-prefetch: gmx.net
                continue;

            String favicon = ("link".equals(img.tagName())
                    ? img.attr("href")
                    : img.attr("content"));
            if (TextUtils.isEmpty(favicon))
                continue;

            try {
                URL url = new URL(base, favicon);
                Favicon f = getFavicon(url, img.attr("type"), scaleToPixels, context);
                Log.i("Using favicon=" + url);
                return f;
            } catch (Throwable ex) {
                if (ex instanceof FileNotFoundException ||
                        ex.getCause() instanceof FileNotFoundException ||
                        ex.getCause() instanceof CertPathValidatorException)
                    Log.i(ex);
                else
                    Log.e(ex);
            }
        }

        return null;
    }

    private static int getOrder(String host, Element img) {
        // https://en.wikipedia.org/wiki/Favicon#How_to_use
        String href = img.attr("href")
                .toLowerCase(Locale.ROOT)
                .trim();
        String rel = img.attr("rel")
                .toLowerCase(Locale.ROOT)
                .replace("shortcut", "") // "shortcut icon"
                .trim();
        String type = img.attr("type")
                .trim();
        String purpose = img.attr("purpose")
                .toLowerCase(Locale.ROOT)
                .trim();

        int order = 0;
        if ("link".equals(img.tagName()))
            order += 100;

        boolean isIco = (href.endsWith(".ico") || "image/x-icon".equals(type));
        boolean isSvg = (href.endsWith(".svg") || "image/svg+xml".equals(type));
        boolean isMask = ("mask-icon".equals(rel) || img.hasAttr("mask") || purpose.contains("mask"));
        boolean isMonochrome = purpose.contains("monochrome");

        if (isMask || isMonochrome)
            order = -10; // Safari: "mask-icon"
        else if ("icon".equals(rel) && !isIco)
            order += 20;
        else if ("manifest".equals(rel))
            order += 20;
        else if ("apple-touch-icon".equals(rel) ||
                "apple-touch-icon-precomposed".equals(rel)) {
            // "apple-touch-startup-image"
            if ("mailbox.org".equals(host))
                order += 30;
            else
                order += 20;
        }

        if (isIco)
            order += 1;
        else if (isSvg)
            order += 2;
        else
            order += 5;

        return order;
    }

    private static int getSize(String sizes) {
        int q = sizes.indexOf('?');
        if (q >= 0)
            sizes = sizes.substring(0, q);

        int max = 0;
        for (String size : sizes.split(" ")) {
            int min = Integer.MAX_VALUE;
            for (String p : size.trim().split("[×|x|X]")) {
                if (TextUtils.isEmpty(p) || "any".equalsIgnoreCase(p))
                    continue;

                try {
                    int x = Integer.parseInt(p);
                    if (x < min)
                        min = x;
                } catch (NumberFormatException ex) {
                    Log.w(ex);
                }
            }
            if (min != Integer.MAX_VALUE && min > max)
                max = min;
        }

        return max;
    }

    @NonNull
    private static Favicon getFavicon(URL url, String mimeType, int scaleToPixels, Context context) throws IOException {
        Log.i("GET favicon " + url);

        if (!"https".equals(url.getProtocol()))
            throw new FileNotFoundException(url.toString());

        HttpURLConnection connection = ConnectionHelper
                .openConnectionUnsafe(context, url, FAVICON_CONNECT_TIMEOUT, FAVICON_READ_TIMEOUT);

        try {
            int status = connection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK)
                throw new FileNotFoundException("Error " + status + ": " + connection.getResponseMessage());

            Bitmap bitmap = ImageHelper.getScaledBitmap(connection.getInputStream(), url.toString(), mimeType, scaleToPixels);
            if (bitmap == null)
                throw new FileNotFoundException("decodeStream");
            if (bitmap.getWidth() <= 1 || bitmap.getHeight() <= 1)
                throw new IOException("Too small");
            Log.i("GOT favicon " + url);
            return new Favicon(bitmap, url.toString());
        } catch (Throwable ex) {
            Log.i("GET favicon " + url + " error=" + ex.getMessage());
            throw ex;
        } finally {
            connection.disconnect();
        }
    }

    private static boolean isRecoverable(Throwable ex, Context context) {
        if (ex instanceof SocketTimeoutException) {
            ConnectivityManager cm = Helper.getSystemService(context, ConnectivityManager.class);
            NetworkInfo ni = (cm == null ? null : cm.getActiveNetworkInfo());
            return (ni == null || !ni.isConnected());
        }

        return !(ex instanceof ConnectException ||
                (ex instanceof UnknownHostException &&
                        ex.getMessage() != null &&
                        ex.getMessage().contains("No address associated with hostname")) ||
                ex instanceof EOFException ||
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
                    EntityLog.log(context, EntityLog.Type.Notification, "Contact changed uri=" + uri);
                    Helper.getSerialExecutor().submit(new Runnable() {
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

            Helper.getSerialExecutor().submit(new Runnable() {
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
                EntityLog.log(context, EntityLog.Type.Notification, "Observing uri=" + uri);
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

    static String getKey(String email) {
        // Maximum file name length: 255
        // Maximum email address length: 320 (<local part = 64> @ <domain part = 255>)
        return (email.length() > 255
                ? email.substring(0, 255)
                : email).toLowerCase(Locale.ROOT);
    }

    static Uri getLookupUri(List<Address> addresses) {
        return getLookupUri(addresses.toArray(new Address[0]));
    }

    static Uri getLookupUri(Address[] addresses) {
        if (addresses == null)
            return null;

        for (Address from : addresses) {
            String email = ((InternetAddress) from).getAddress();
            if (TextUtils.isEmpty(email))
                continue;

            Lookup lookup = emailLookup.get(email.toLowerCase(Locale.ROOT));
            if (lookup != null)
                return lookup.uri;
        }

        return null;
    }

    static Uri getLookupUri(String email) {
        if (TextUtils.isEmpty(email))
            return null;

        Lookup lookup = emailLookup.get(email.toLowerCase(Locale.ROOT));
        return (lookup == null ? null : lookup.uri);
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

    private static Map<String, Lookup> getEmailLookup(Context context) {
        Map<String, Lookup> all = new ConcurrentHashMap<>();

        if (Helper.hasPermission(context, Manifest.permission.READ_CONTACTS)) {
            Log.i("Reading email/uri");
            File dir = Helper.ensureExists(context, "notcontact");

            ContentResolver resolver = context.getContentResolver();
            try (Cursor cursor = resolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                    new String[]{
                            ContactsContract.CommonDataKinds.Photo.CONTACT_ID,
                            ContactsContract.Contacts.LOOKUP_KEY,
                            ContactsContract.CommonDataKinds.Email.ADDRESS,
                            ContactsContract.Contacts.DISPLAY_NAME
                    },
                    ContactsContract.CommonDataKinds.Email.ADDRESS + " <> ''",
                    null, ContactsContract.Contacts.DISPLAY_NAME)) {
                while (cursor != null && cursor.moveToNext()) {
                    long contactId = cursor.getLong(0);
                    String lookupKey = cursor.getString(1);
                    String email = cursor.getString(2);
                    String displayName = cursor.getString(3);

                    Lookup lookup = new Lookup();
                    lookup.uri = ContactsContract.Contacts.getLookupUri(contactId, lookupKey);
                    lookup.displayName = displayName;
                    all.put(email.toLowerCase(Locale.ROOT), lookup);

                    if (!TextUtils.isEmpty(email)) {
                        String ekey = getKey(email);
                        File file = new File(dir, ekey);
                        if (file.exists()) {
                            Log.i("Contact delete cached key=" + ekey);
                            Helper.secureDelete(file);
                        }
                    }
                }
            } catch (Throwable ex) {
                Log.e(ex);
            }
        }

        Log.i("Read email/uri=" + all.size());
        return all;
    }

    static int[] getStats() {
        synchronized (emailContactInfo) {
            return new int[]{emailLookup.size(), emailContactInfo.size()};
        }
    }

    private static class Lookup {
        Uri uri;
        String displayName;
    }

    static class Favicon {
        private Bitmap bitmap;
        private String type;
        private boolean verified;
        private String source;

        private Favicon(@NonNull Bitmap bitmap, String source) {
            this(bitmap, "favicon", false);
            this.source = source;
        }

        Favicon(@NonNull Bitmap bitmap, String type, boolean verified) {
            this.bitmap = bitmap;
            this.type = type;
            this.verified = verified;
            this.source = type;
        }
    }
}
