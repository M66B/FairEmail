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

import android.content.Context;
import android.content.pm.ProviderInfo;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import java.io.File;

public class FileProviderEx extends FileProvider {
    // https://android-review.googlesource.com/c/platform/frameworks/support/+/1978527
    public FileProviderEx() {
        super(R.xml.fileprovider_paths);
    }

    @Override
    public void attachInfo(@NonNull Context context, @NonNull ProviderInfo info) {
        try {
            super.attachInfo(context, info);
        } catch (Throwable ex) {
            /*
                OSCAL C80 (C80) Android 12 (SDK 31)
                Exception java.lang.RuntimeException:
                  at android.app.ActivityThread.installProvider (ActivityThread.java:7537)
                  at android.app.ActivityThread.installContentProviders (ActivityThread.java:7044)
                  at android.app.ActivityThread.handleBindApplication (ActivityThread.java:6811)
                  at android.app.ActivityThread.access$1500 (ActivityThread.java:268)
                  at android.app.ActivityThread$H.handleMessage (ActivityThread.java:2100)
                  at android.os.Handler.dispatchMessage (Handler.java:106)
                  at android.os.Looper.loopOnce (Looper.java:201)
                  at android.os.Looper.loop (Looper.java:288)
                  at android.app.ActivityThread.main (ActivityThread.java:7953)
                  at java.lang.reflect.Method.invoke
                  at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run (RuntimeInit.java:553)
                  at com.android.internal.os.ZygoteInit.main (ZygoteInit.java:1003)
                Caused by java.lang.IllegalArgumentException:
                  at androidx.core.content.FileProvider$SimplePathStrategy.addRoot (FileProvider.java:798)
                  at androidx.core.content.FileProvider.parsePathStrategy (FileProvider.java:734)
                  at androidx.core.content.FileProvider.getPathStrategy (FileProvider.java:645)
                  at androidx.core.content.FileProvider.attachInfo (FileProvider.java:424)
                  at android.app.ActivityThread.installProvider (ActivityThread.java:7531)
                Caused by java.io.IOException: Invalid argument
                  at java.io.UnixFileSystem.canonicalize0
                  at java.io.UnixFileSystem.canonicalize (UnixFileSystem.java:153)
                  at java.io.File.getCanonicalPath (File.java:611)
                  at java.io.File.getCanonicalFile (File.java:636)
                  at androidx.core.content.FileProvider$SimplePathStrategy.addRoot (FileProvider.java:796)
             */
        }
    }

    public static Uri getUri(@NonNull Context context, @NonNull String authority, @NonNull File file) {
        return getUri(context, authority, file, null);
    }

    public static Uri getUri(@NonNull Context context, @NonNull String authority, @NonNull File file, @NonNull String name) {
        Uri uri;
        if (TextUtils.isEmpty(name))
            uri = getUriForFile(context, authority, file);
        else
            uri = getUriForFile(context, authority, file, name);
        return uri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("No external updates");
    }
}
