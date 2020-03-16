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
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.Toast;

import androidx.core.app.Person;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.lifecycle.LifecycleOwner;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Shortcuts {
    static void update(final Context context, final LifecycleOwner owner) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N_MR1)
            return;

        new SimpleTask<List<ShortcutInfoCompat>>() {
            @Override
            @TargetApi(Build.VERSION_CODES.N_MR1)
            protected List<ShortcutInfoCompat> onExecute(Context context, Bundle args) {
                ShortcutManager sm = (ShortcutManager) context.getSystemService(Context.SHORTCUT_SERVICE);
                int app = ShortcutManagerCompat.getMaxShortcutCountPerActivity(context);
                int manifest = sm.getManifestShortcuts().size();
                int count = app - manifest;
                Log.i("Shortcuts count=" + count + " app=" + app + " manifest=" + manifest);

                DB db = DB.getInstance(context);
                List<String> emails = new ArrayList<>();
                List<ShortcutInfoCompat> shortcuts = new ArrayList<>();
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

                        Intent intent = new Intent(context, ActivityMain.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.setAction(Intent.ACTION_SEND);
                        intent.addCategory(Intent.CATEGORY_DEFAULT);
                        intent.setData(Uri.parse("mailto:" + email));

                        IconCompat icon = null;
                        if (avatar != null &&
                                Helper.hasPermission(context, Manifest.permission.READ_CONTACTS)) {
                            // Create icon from bitmap because launcher might not have contacts permission
                            InputStream is = ContactsContract.Contacts.openContactPhotoInputStream(
                                    context.getContentResolver(), Uri.parse(avatar));
                            Bitmap bitmap = BitmapFactory.decodeStream(is);
                            if (bitmap != null)
                                icon = IconCompat.createWithBitmap(bitmap);
                        }
                        if (icon == null)
                            icon = IconCompat.createWithResource(context, R.drawable.ic_shortcut_email);

                        Set<String> categories = new HashSet<>(Arrays.asList("eu.faircode.email.TEXT_SHARE_TARGET"));

                        String id = (name == null ? email : "\"" + name + "\" <" + email + ">");
                        ShortcutInfoCompat.Builder builder = new ShortcutInfoCompat.Builder(context, id)
                                .setIcon(icon)
                                .setRank(shortcuts.size() + 1)
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
                                person.setUri(avatar);
                            builder.setPerson(person.build());
                        }

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
}
