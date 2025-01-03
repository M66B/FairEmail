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
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.core.net.MailTo;

public class ActivitySendSelf extends ActivityBase {
    @Override
    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new SimpleTask<EntityIdentity>() {
            @Override
            protected EntityIdentity onExecute(Context context, Bundle args) {
                DB db = DB.getInstance(context);
                return db.identity().getPrimaryIdentity();
            }

            @Override
            protected void onExecuted(Bundle args, EntityIdentity identity) {
                Intent intent = getIntent();

                if (identity != null) {
                    Uri uri = intent.getData();
                    if (uri != null && "mailto".equals(uri.getScheme())) {
                        String mailto = uri.toString();
                        int s = mailto.indexOf(':');
                        int q = mailto.indexOf('?', s);
                        if (s > 0) {
                            String query = (q < 0 ? mailto.substring(s + 1) : mailto.substring(s + 1, q));
                            intent.setData(Uri.parse(MailTo.MAILTO_SCHEME + Uri.encode(identity.email) + query));
                        }
                    } else
                        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{identity.email});
                }

                intent.setClass(ActivitySendSelf.this, ActivityCompose.class);
                startActivity(intent);
                finish();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getSupportFragmentManager(), ex);
                //finish();
            }
        }.execute(this, new Bundle(), "send:self");
    }
}
