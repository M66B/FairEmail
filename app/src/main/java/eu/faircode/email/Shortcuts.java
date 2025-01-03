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
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ShortcutManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.Person;
import androidx.core.content.ContextCompat;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.preference.PreferenceManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.mail.internet.InternetAddress;

// https://developer.android.com/guide/topics/ui/shortcuts/creating-shortcuts
// https://developer.android.com/guide/topics/ui/shortcuts/managing-shortcuts

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

                ShortcutManager sm = Helper.getSystemService(context, ShortcutManager.class);
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
                        builder.setLongLived(true);
                        builder.setRank(shortcuts.size() + 1);
                        shortcuts.add(builder.build());
                    }
                }

                return shortcuts;
            }

            @Override
            @TargetApi(Build.VERSION_CODES.N_MR1)
            protected void onExecuted(Bundle args, List<ShortcutInfoCompat> shortcuts) {
                List<ShortcutInfoCompat> add = new ArrayList<>();
                List<String> remove = new ArrayList<>();

                if (BuildConfig.DEBUG && false)
                    ShortcutManagerCompat.removeAllDynamicShortcuts(context.getApplicationContext());

                List<ShortcutInfoCompat> existing = ShortcutManagerCompat.getDynamicShortcuts(context.getApplicationContext());

                for (ShortcutInfoCompat shortcut : shortcuts) {
                    boolean exists = false;
                    for (ShortcutInfoCompat current : existing)
                        if (Objects.equals(shortcut.getId(), current.getId())) {
                            Log.i("Found shortcut=" + current.getId());
                            exists = true;
                            break;
                        }
                    if (!exists)
                        add.add(shortcut);
                }

                for (ShortcutInfoCompat current : existing) {
                    boolean found = false;
                    for (ShortcutInfoCompat shortcut : shortcuts)
                        if (Objects.equals(shortcut.getId(), current.getId())) {
                            found = true;
                            break;
                        }
                    if (!found) {
                        Log.i("Not found shortcut=" + current.getId());
                        remove.add(current.getId());
                    }
                }

                Log.i("Shortcuts count=" + shortcuts.size() +
                        " add=" + add.size() +
                        " remove=" + remove.size());

                if (remove.size() > 0)
                    ShortcutManagerCompat.removeDynamicShortcuts(context.getApplicationContext(), remove);

                for (ShortcutInfoCompat shortcut : add) {
                    Log.i("Push shortcut id=" + shortcut.getId());
                    ShortcutManagerCompat.pushDynamicShortcut(context.getApplicationContext(), shortcut);
                }
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                ToastEx.makeText(context, Log.formatThrowable(ex, false), Toast.LENGTH_LONG).show();
            }
        }.execute(context, owner, new Bundle(), "shortcuts:update");
    }

    @NonNull
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
                    new String[]{email}, ContactsContract.Contacts.DISPLAY_NAME)) {
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

    @NonNull
    static ShortcutInfoCompat.Builder getShortcut(Context context, EntityContact contact) {
        return getShortcut(context, contact.email, contact.name, contact.avatar);
    }

    @NonNull
    private static ShortcutInfoCompat.Builder getShortcut(Context context, String email, String name, String avatar) {
        return getShortcut(context, email, name, avatar == null ? null : Uri.parse(avatar));
    }

    @NonNull
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
            ContentResolver resolver = context.getContentResolver();
            try (InputStream is = ContactsContract.Contacts
                    .openContactPhotoInputStream(resolver, avatar)) {
                bitmap = BitmapFactory.decodeStream(is);
            } catch (IOException ex) {
                Log.e(ex);
            }
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
        Set<String> categories = new HashSet<>(Arrays.asList(BuildConfig.APPLICATION_ID + ".TEXT_SHARE_TARGET"));
        ShortcutInfoCompat.Builder builder = new ShortcutInfoCompat.Builder(context, id)
                .setIcon(icon)
                .setShortLabel(name == null ? email : name)
                .setLongLabel(name == null ? email : name)
                .setCategories(categories)
                .setIntent(intent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Person.Builder person = new Person.Builder()
                    .setIcon(icon)
                    .setKey(email)
                    .setName(name == null ? email : name)
                    .setImportant(true);
            if (avatar != null)
                person.setUri(avatar.toString());
            builder.setPerson(person.build());
        }

        return builder;
    }

    @NonNull
    static ShortcutInfoCompat.Builder getShortcut(Context context, TupleMessageEx message, String label, ContactInfo[] contactInfo) {
        Intent thread = new Intent(context, ActivityView.class);
        thread.setAction("thread:" + message.id);
        thread.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        thread.putExtra("account", message.account);
        thread.putExtra("folder", message.folder);
        thread.putExtra("type", message.folderType);
        thread.putExtra("thread", message.thread);
        thread.putExtra("filter_archive", !EntityFolder.ARCHIVE.equals(message.folderType));
        thread.putExtra("pinned", true);
        thread.putExtra("msgid", message.msgid);

        Bitmap bm;
        if (contactInfo[0].hasPhoto())
            bm = contactInfo[0].getPhotoBitmap();
        else {
            int resid = R.drawable.baseline_mail_24;
            Drawable d = ContextCompat.getDrawable(context, resid);
            bm = Bitmap.createBitmap(
                    d.getIntrinsicWidth(),
                    d.getIntrinsicHeight(),
                    Bitmap.Config.ARGB_8888);
        }

        IconCompat icon = IconCompat.createWithBitmap(bm);
        String id = "message:" + message.id;
        return new ShortcutInfoCompat.Builder(context, id)
                .setIcon(icon)
                .setShortLabel(label)
                .setLongLabel(label)
                .setIntent(thread);
    }

    @NonNull
    static ShortcutInfoCompat.Builder getShortcut(Context context, EntityFolder folder) {
        Intent view = new Intent(context, ActivityView.class);
        view.setAction("folder:" + folder.id);
        view.putExtra("account", folder.account);
        view.putExtra("type", folder.type);
        view.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        int resid = EntityFolder.getIcon(folder.type);
        Drawable d = ContextCompat.getDrawable(context, resid);
        Bitmap bm = Bitmap.createBitmap(
                d.getIntrinsicWidth(),
                d.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);
        d.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        d.setTint(folder.color == null ? Color.DKGRAY : folder.color);
        d.draw(canvas);

        IconCompat icon = IconCompat.createWithBitmap(bm);
        String id = "folder:" + folder.id;
        return new ShortcutInfoCompat.Builder(context, id)
                .setIcon(icon)
                .setShortLabel(folder.getDisplayName(context))
                .setLongLabel(folder.getDisplayName(context))
                .setIntent(view);
    }

    static boolean can(Context context) {
        try {
            return ShortcutManagerCompat.isRequestPinShortcutSupported(context.getApplicationContext());
        } catch (Throwable ex) {
            Log.e(ex);
            /*
                java.lang.IllegalStateException: User 10 is locked or not running
                        at android.os.Parcel.createExceptionOrNull(Parcel.java:2394)
                        at android.os.Parcel.createException(Parcel.java:2370)
                        at android.os.Parcel.readException(Parcel.java:2353)
                        at android.os.Parcel.readException(Parcel.java:2295)
                        at android.content.pm.IShortcutService$Stub$Proxy.isRequestPinItemSupported(IShortcutService.java:1253)
                        at android.content.pm.ShortcutManager.isRequestPinShortcutSupported(ShortcutManager.java:542)
                        at androidx.core.content.pm.ShortcutManagerCompat$$InternalSyntheticApiModelOutline$1$8e6ce95527e1069e0571cafad12c473a04f9df49417a7f35f83879c4b29aa5f0$2.m(ShortcutManagerCompat)
                        at androidx.core.content.pm.ShortcutManagerCompat.isRequestPinShortcutSupported(ShortcutManagerCompat:155)
                        at eu.faircode.email.Shortcuts.can(Shortcuts:342)
             */
            return false;
        }
    }

    static void requestPinShortcut(Context context, ShortcutInfoCompat info) {
        try {
            ShortcutManagerCompat.requestPinShortcut(context.getApplicationContext(), info, null);
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    static void cleanup(Context context) {
        if (!BuildConfig.DEBUG)
            return;

        DB db = DB.getInstance(context);
        List<ShortcutInfoCompat> pinned =
                ShortcutManagerCompat.getShortcuts(context, ShortcutManagerCompat.FLAG_MATCH_PINNED);
        for (ShortcutInfoCompat shortcut : pinned) {
            String[] id = shortcut.getId().split(":");
            if (id.length == 2 && "message".equals(id[0])) {
                Intent intent = shortcut.getIntent();
                long account = intent.getLongExtra("account", -1L);
                String thread = intent.getStringExtra("thread");
                List<EntityMessage> messages = db.message().getMessagesByThread(account, thread, null, null);
                if (messages != null && messages.size() == 0)
                    ; // Delete the shortcut, if only this was possible ...
            }
        }
    }
}
