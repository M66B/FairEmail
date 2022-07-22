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

    Copyright 2018-2022 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LifecycleOwner;

import java.io.FileInputStream;
import java.io.InputStream;

public class Check {
    static final String URI_PRIVACY = "https://support.virustotal.com/hc/en-us/articles/115002168385-Privacy-Policy";
    private static final String URI_VIRUS_TOTAL = "https://www.virustotal.com/";

    static void virus(Context context, LifecycleOwner owner, FragmentManager fm, long id) {
        Bundle args = new Bundle();
        args.putLong("id", id);

        new SimpleTask<String>() {
            @Override
            protected String onExecute(Context context, Bundle args) throws Throwable {
                long id = args.getLong("id");

                DB db = DB.getInstance(context);
                EntityAttachment attachment = db.attachment().getAttachment(id);
                if (attachment == null)
                    return null;

                try (InputStream is = new FileInputStream(attachment.getFile(context))) {
                    return Helper.getHash(is, "SHA-256");
                }
            }

            @Override
            protected void onExecuted(Bundle args, String hash) {
                if (hash == null)
                    return;

                Uri uri = Uri.parse(URI_VIRUS_TOTAL + "gui/file/" + hash);
                Helper.view(context, uri, false);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(fm, ex);
            }
        }.execute(context, owner, args, "attachment:scan");
    }
}
