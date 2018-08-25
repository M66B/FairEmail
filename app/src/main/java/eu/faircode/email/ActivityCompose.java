package eu.faircode.email;

/*
    This file is part of FairEmail.

    FairEmail is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    NetGuard is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with NetGuard.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2018 by Marcel Bokhorst (M66B)
*/

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;

import java.util.ArrayList;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;

public class ActivityCompose extends ActivityBase implements FragmentManager.OnBackStackChangedListener {
    static final int REQUEST_CONTACT_TO = 1;
    static final int REQUEST_CONTACT_CC = 2;
    static final int REQUEST_CONTACT_BCC = 3;
    static final int REQUEST_ATTACHMENT = 4;
    static final int REQUEST_OPENPGP = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportFragmentManager().addOnBackStackChangedListener(this);

        if (getSupportFragmentManager().getFragments().size() == 0) {
            Bundle args;
            if (Intent.ACTION_SEND.equals(getIntent().getAction()) ||
                    Intent.ACTION_SENDTO.equals(getIntent().getAction()) ||
                    Intent.ACTION_SEND_MULTIPLE.equals(getIntent().getAction())) {
                args = new Bundle();

                args.putString("action", "new");
                args.putLong("account", -1);

                if (getIntent().hasExtra(Intent.EXTRA_EMAIL))
                    args.putString("to", TextUtils.join(", ", getIntent().getStringArrayExtra(Intent.EXTRA_EMAIL)));

                if (getIntent().hasExtra(Intent.EXTRA_CC))
                    args.putString("cc", TextUtils.join(", ", getIntent().getStringArrayExtra(Intent.EXTRA_CC)));

                if (getIntent().hasExtra(Intent.EXTRA_BCC))
                    args.putString("bcc", TextUtils.join(", ", getIntent().getStringArrayExtra(Intent.EXTRA_BCC)));

                if (getIntent().hasExtra(Intent.EXTRA_SUBJECT))
                    args.putString("subject", getIntent().getStringExtra(Intent.EXTRA_SUBJECT));

                if (getIntent().hasExtra(Intent.EXTRA_TEXT))
                    args.putString("body", getIntent().getStringExtra(Intent.EXTRA_TEXT)); // Intent.EXTRA_HTML_TEXT

                if (getIntent().hasExtra(Intent.EXTRA_STREAM))
                    if (Intent.ACTION_SEND_MULTIPLE.equals(getIntent().getAction()))
                        args.putParcelableArrayList("attachments", getIntent().getParcelableArrayListExtra(Intent.EXTRA_STREAM));
                    else {
                        ArrayList<Uri> uris = new ArrayList<>();
                        uris.add((Uri) getIntent().getParcelableExtra(Intent.EXTRA_STREAM));
                        args.putParcelableArrayList("attachments", uris);
                    }

            } else
                args = getIntent().getExtras();

            FragmentCompose fragment = new FragmentCompose();
            fragment.setArguments(args);

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("compose");
            fragmentTransaction.commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED))
                    getSupportFragmentManager().popBackStack();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackStackChanged() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0)
            finish();
    }
}
