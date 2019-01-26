package eu.faircode.email;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;

import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

import androidx.core.content.ContextCompat;

public class ContactInfo {
    private String email;
    private Bitmap bitmap;
    private String displayName;
    private Uri lookupUri;
    private long time;

    private static Map<String, ContactInfo> emailContactInfo = new HashMap<>();

    private static final long CACHE_DURATION = 60 * 1000L;

    private ContactInfo() {
    }

    boolean hasPhoto() {
        return (bitmap != null);
    }

    Bitmap getPhotoBitmap() {
        return bitmap;
    }

    String getDisplayName(boolean compact) {
        if (compact && displayName != null)
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
        return (new Date().getTime() - time > CACHE_DURATION);
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

        String email = address.getAddress();
        synchronized (emailContactInfo) {
            ContactInfo info = emailContactInfo.get(email);
            if (info != null && !info.isExpired())
                return info;
        }

        if (cacheOnly)
            return null;

        ContactInfo info = new ContactInfo();
        info.email = email;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED)
            try {
                Cursor cursor = null;
                try {
                    ContentResolver resolver = context.getContentResolver();
                    cursor = resolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                            new String[]{
                                    ContactsContract.CommonDataKinds.Photo.CONTACT_ID,
                                    ContactsContract.Contacts.LOOKUP_KEY,
                                    ContactsContract.Contacts.DISPLAY_NAME
                            },
                            ContactsContract.CommonDataKinds.Email.ADDRESS + " = ?",
                            new String[]{
                                    email
                            }, null);

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
                } finally {
                    if (cursor != null)
                        cursor.close();
                }
            } catch (Throwable ex) {
                Log.e(ex);
            }

        if (info.bitmap == null) {
            boolean identicons = prefs.getBoolean("identicons", false);
            if (identicons) {
                String theme = prefs.getString("theme", "light");
                int dp = Helper.dp2pixels(context, 48);
                info.bitmap = Identicon.generate(email, dp, 5, "light".equals(theme));
            }
        }

        if (info.displayName == null)
            info.displayName = address.getPersonal();

        synchronized (emailContactInfo) {
            emailContactInfo.put(email, info);
        }

        info.time = new Date().getTime();
        return info;
    }
}
