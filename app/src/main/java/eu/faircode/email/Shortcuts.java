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
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ShortcutManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.core.app.Person;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.preference.PreferenceManager;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.mail.internet.InternetAddress;

class Shortcuts {
    private static final int MAX_SHORTCUTS = 4;

    static void update(final Context context, final LifecycleOwner owner) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N_MR1)
            return;

        new SimpleTask<List<ShortcutInfoCompat>>() {
            @Override
            @TargetApi(Build.VERSION_CODES.N_MR1)
            protected List<ShortcutInfoCompat> onExecute(Context context, Bundle args) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                boolean enabled = prefs.getBoolean("shortcuts", true);

                ShortcutManager sm = (ShortcutManager) context.getSystemService(Context.SHORTCUT_SERVICE);
                int app = sm.getMaxShortcutCountPerActivity();
                int manifest = sm.getManifestShortcuts().size();
                int count = Math.min(app - manifest, MAX_SHORTCUTS);
                EntityLog.log(context, "Shortcuts count=" + count +
                        " app=" + app + " manifest=" + manifest + " enabled=" + enabled);

                List<ShortcutInfoCompat> shortcuts = new ArrayList<>();
                if (!enabled)
                    return shortcuts;

                DB db = DB.getInstance(context);
                List<String> emails = new ArrayList<>();
                try (Cursor cursor = db.contact().getFrequentlyContacted()) {
                    int colEmail = cursor.getColumnIndex("email");
                    int colName = cursor.getColumnIndex("name");
                    int colAvatar = cursor.getColumnIndex("avatar");
                    while (shortcuts.size() < count && cursor.moveToNext()) {
                        String email = cursor.getString(colEmail);
                        String name = (cursor.isNull(colName) ? null : cursor.getString(colName));
                        String avatar = (cursor.isNull(colAvatar) ? null : cursor.getString(colAvatar));

                        if (emails.contains(email))
                            continue;
                        emails.add(email);

                        EntityLog.log(context, "Shortcut email=" + email);
                        ShortcutInfoCompat.Builder builder = getShortcut(context, email, name, avatar);
                        builder.setRank(shortcuts.size() + 1);
                        shortcuts.add(builder.build());
                    }
                }

                return shortcuts;
            }

            @Override
            @TargetApi(Build.VERSION_CODES.N_MR1)
            protected void onExecuted(Bundle args, List<ShortcutInfoCompat> shortcuts) {
                ShortcutManagerCompat.removeAllDynamicShortcuts(context);
                ShortcutManagerCompat.addDynamicShortcuts(context, shortcuts);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                ToastEx.makeText(context, Log.formatThrowable(ex, false), Toast.LENGTH_LONG).show();
            }
        }.execute(context, owner, new Bundle(), "shortcuts:update");
    }

    @NotNull
    static ShortcutInfoCompat.Builder getShortcut(Context context, InternetAddress address) {
        String name = address.getPersonal();
        String email = address.getAddress();

        Uri lookupUri = null;
        boolean contacts = Helper.hasPermission(context, Manifest.permission.READ_CONTACTS);
        if (contacts) {
            ContentResolver resolver = context.getContentResolver();
            try (Cursor cursor = resolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                    new String[]{
                            ContactsContract.CommonDataKinds.Photo.CONTACT_ID,
                            ContactsContract.Contacts.LOOKUP_KEY,
                            ContactsContract.Contacts.DISPLAY_NAME
                    },
                    ContactsContract.CommonDataKinds.Email.ADDRESS + " = ?",
                    new String[]{email}, null)) {
                if (cursor != null && cursor.moveToNext()) {
                    int colContactId = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Photo.CONTACT_ID);
                    int colLookupKey = cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY);
                    int colDisplayName = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);

                    long contactId = cursor.getLong(colContactId);
                    String lookupKey = cursor.getString(colLookupKey);
                    String displayName = cursor.getString(colDisplayName);

                    lookupUri = ContactsContract.Contacts.getLookupUri(contactId, lookupKey);
                    if (!TextUtils.isEmpty(displayName))
                        name = displayName;
                }
            }
        }

        return getShortcut(context, email, name, lookupUri);
    }

    @NotNull
    static ShortcutInfoCompat.Builder getShortcut(Context context, EntityContact contact) {
        return getShortcut(context, contact.email, contact.name, contact.avatar);
    }

    @NotNull
    private static ShortcutInfoCompat.Builder getShortcut(Context context, String email, String name, String avatar) {
        return getShortcut(context, email, name, avatar == null ? null : Uri.parse(avatar));
    }

    @NotNull
    private static ShortcutInfoCompat.Builder getShortcut(Context context, String email, String name, Uri avatar) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean identicons = prefs.getBoolean("identicons", false);
        boolean circular = prefs.getBoolean("circular", true);

        Intent intent = new Intent(context, ActivityCompose.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setAction(Intent.ACTION_SEND);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setData(Uri.parse("mailto:" + email));

        Bitmap bitmap = null;
        if (avatar != null &&
                Helper.hasPermission(context, Manifest.permission.READ_CONTACTS)) {
            // Create icon from bitmap because launcher might not have contacts permission
            InputStream is = ContactsContract.Contacts.openContactPhotoInputStream(
                    context.getContentResolver(), avatar);
            bitmap = BitmapFactory.decodeStream(is);
        }

        boolean identicon = false;
        if (bitmap == null) {
            int dp = Helper.dp2pixels(context, 96);
            if (identicons) {
                identicon = true;
                bitmap = ImageHelper.generateIdenticon(email, dp, 5, context);
            } else
                bitmap = ImageHelper.generateLetterIcon(email, name, dp, context);
        }

        bitmap = ImageHelper.makeCircular(bitmap,
                circular && !identicon ? null : Helper.dp2pixels(context, 3));

        IconCompat icon = IconCompat.createWithBitmap(bitmap);
        String id = (name == null ? email : "\"" + name + "\" <" + email + ">");
        Set<String> categories = new HashSet<>(Arrays.asList("eu.faircode.email.TEXT_SHARE_TARGET"));
        ShortcutInfoCompat.Builder builder = new ShortcutInfoCompat.Builder(context, id)
                .setIcon(icon)
                .setShortLabel(name == null ? email : name)
                .setLongLabel(name == null ? email : name)
                .setCategories(categories)
                .setIntent(intent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Person.Builder person = new Person.Builder()
                    .setIcon(icon)
                    .setName(name == null ? email : name)
                    .setImportant(true);
            if (avatar != null)
                person.setUri(avatar.toString());
            builder.setPerson(person.build());
        }

        return builder;
    }
}
