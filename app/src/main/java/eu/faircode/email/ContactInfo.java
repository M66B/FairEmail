package eu.faircode.email;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract;

import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

import androidx.core.content.ContextCompat;

public class ContactInfo {
    private InputStream is;
    private Drawable photo;
    private String displayName;
    private Uri lookupUri;
    private long time;

    private static Map<String, ContactInfo> emailContactInfo = new HashMap<>();

    private static final long CACHE_DURATION = 60 * 1000L;

    ContactInfo() {
    }

    ContactInfo(String displayName) {
        this.displayName = displayName;
    }

    ContactInfo(Drawable photo, String displayName) {
        this.photo = photo;
        this.displayName = displayName;
    }

    Bitmap getPhotoBitmap() {
        return BitmapFactory.decodeStream(is);
    }

    Drawable getPhotoDrawable() {
        if (photo != null)
            return photo;

        if (is == null)
            return null;

        return Drawable.createFromStream(is, displayName == null ? "Photo" : displayName);
    }

    boolean hasPhoto() {
        return (is != null || photo != null);
    }

    String getDisplayName() {
        return displayName;
    }

    boolean hasDisplayName() {
        return (displayName != null);
    }

    Uri getLookupUri() {
        return lookupUri;
    }

    boolean hasLookupUri() {
        return (lookupUri != null);
    }

    private boolean isExpired() {
        return (new Date().getTime() - time > CACHE_DURATION);
    }

    static ContactInfo get(Context context, Address[] addresses, boolean cached) {
        if (addresses == null || addresses.length == 0)
            return null;

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED)
            return null;

        String email = ((InternetAddress) addresses[0]).getAddress();

        synchronized (emailContactInfo) {
            ContactInfo info = emailContactInfo.get(email);
            if (info != null && !info.isExpired())
                return info;
        }
        if (cached)
            return null;

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

                    ContactInfo info = new ContactInfo();
                    info.is = ContactsContract.Contacts.openContactPhotoInputStream(resolver, lookupUri);
                    info.displayName = cursor.getString(colDisplayName);
                    info.lookupUri = lookupUri;
                    info.time = new Date().getTime();

                    synchronized (emailContactInfo) {
                        emailContactInfo.put(email, info);
                    }

                    return info;
                }
            } finally {
                if (cursor != null)
                    cursor.close();
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }

        return null;
    }
}
