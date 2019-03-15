package eu.faircode.email;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.LifecycleOwner;

public class Shortcuts {
    static void update(final Context context, final LifecycleOwner owner) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N_MR1)
            return;

        new SimpleTask<List<ShortcutInfo>>() {
            @Override
            @TargetApi(Build.VERSION_CODES.N_MR1)
            protected List<ShortcutInfo> onExecute(Context context, Bundle args) {
                ShortcutManager sm = (ShortcutManager) context.getSystemService(Context.SHORTCUT_SERVICE);
                int count = sm.getMaxShortcutCountPerActivity() - sm.getManifestShortcuts().size();
                Log.i("Shortcuts count=" + count +
                        " app=" + sm.getMaxShortcutCountPerActivity() +
                        " manifest=" + sm.getManifestShortcuts().size());

                List<ShortcutInfo> shortcuts = new ArrayList<>();
                if (count > 0) {
                    DB db = DB.getInstance(context);
                    List<EntityContact> frequently = db.contact().getFrequentlyContacted(count);
                    for (EntityContact contact : frequently) {
                        Intent intent = new Intent(context, ActivityCompose.class);
                        intent.setAction(Intent.ACTION_SEND);
                        intent.setData(Uri.parse("mailto:" + contact.email));

                        Icon icon = null;
                        if (contact.avatar != null &&
                                Helper.hasPermission(context, Manifest.permission.READ_CONTACTS)) {
                            // Create icon from bitmap because launcher might not have contacts permission
                            InputStream is = ContactsContract.Contacts.openContactPhotoInputStream(
                                    context.getContentResolver(), Uri.parse(contact.avatar));
                            Bitmap bitmap = BitmapFactory.decodeStream(is);
                            if (bitmap != null)
                                icon = Icon.createWithBitmap(bitmap);
                        }
                        if (icon == null)
                            icon = Icon.createWithResource(context, R.drawable.ic_shortcut_email);

                        shortcuts.add(
                                new ShortcutInfo.Builder(context, Long.toString(contact.id))
                                        .setIcon(icon)
                                        .setRank(shortcuts.size() + 1)
                                        .setShortLabel(contact.name == null ? contact.email : contact.name)
                                        .setIntent(intent)
                                        .build());
                    }
                }

                return shortcuts;
            }

            @Override
            @TargetApi(Build.VERSION_CODES.N_MR1)
            protected void onExecuted(Bundle args, List<ShortcutInfo> shortcuts) {
                ShortcutManager sm = (ShortcutManager) context.getSystemService(Context.SHORTCUT_SERVICE);
                sm.setDynamicShortcuts(shortcuts);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(context, owner, ex);
            }
        }.execute(context, owner, new Bundle(), "shortcuts:update");
    }
}
