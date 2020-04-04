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

import android.content.Intent;
import android.net.MailTo;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spanned;
import android.text.TextUtils;

import androidx.core.app.TaskStackBuilder;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

public class ActivityCompose extends ActivityBase implements FragmentManager.OnBackStackChangedListener {
    static final int PI_REPLY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportFragmentManager().addOnBackStackChangedListener(this);

        if (getSupportFragmentManager().getFragments().size() == 0) {
            Bundle args;
            Intent intent = getIntent();
            String action = intent.getAction();
            if (Intent.ACTION_VIEW.equals(action) ||
                    Intent.ACTION_SENDTO.equals(action) ||
                    Intent.ACTION_SEND.equals(action) ||
                    Intent.ACTION_SEND_MULTIPLE.equals(action)) {

                args = new Bundle();
                args.putString("action", "new");
                args.putLong("account", -1);

                Uri uri = intent.getData();
                if (uri != null && "mailto".equals(uri.getScheme())) {
                    // https://www.ietf.org/rfc/rfc2368.txt
                    String url = uri.toString();
                    int query = url.indexOf('?', MailTo.MAILTO_SCHEME.length());
                    if (query > 0)
                        url = url.substring(0, query) + url.substring(query).replace(":", "%3A");

                    MailTo mailto = MailTo.parse(url);

                    String to = mailto.getTo();
                    if (to != null)
                        try {
                            InternetAddress.parse(to);
                            args.putString("to", to);
                        } catch (AddressException ex) {
                            Log.w(ex);
                        }

                    String cc = mailto.getCc();
                    if (cc != null)
                        try {
                            InternetAddress.parse(cc);
                            args.putString("cc", cc);
                        } catch (AddressException ex) {
                            Log.w(ex);
                        }

                    String subject = mailto.getSubject();
                    if (subject != null)
                        args.putString("subject", subject);

                    String body = mailto.getBody();
                    if (body != null)
                        args.putString("body", body);
                }

                if (intent.hasExtra(Intent.EXTRA_SHORTCUT_ID)) {
                    String to = intent.getStringExtra(Intent.EXTRA_SHORTCUT_ID);
                    if (to != null)
                        try {
                            InternetAddress.parse(to);
                            args.putString("to", to);
                        } catch (AddressException ex) {
                            Log.w(ex);
                        }
                }

                if (intent.hasExtra(Intent.EXTRA_EMAIL)) {
                    String[] to = intent.getStringArrayExtra(Intent.EXTRA_EMAIL);
                    if (to != null)
                        try {
                            InternetAddress.parse(TextUtils.join(", ", to));
                            args.putString("to", TextUtils.join(", ", to));
                        } catch (AddressException ex) {
                            Log.w(ex);
                        }
                }

                if (intent.hasExtra(Intent.EXTRA_CC)) {
                    String[] cc = intent.getStringArrayExtra(Intent.EXTRA_CC);
                    if (cc != null)
                        try {
                            InternetAddress.parse(TextUtils.join(", ", cc));
                            args.putString("cc", TextUtils.join(", ", cc));
                        } catch (AddressException ex) {
                            Log.w(ex);
                        }
                }

                if (intent.hasExtra(Intent.EXTRA_BCC)) {
                    String[] bcc = intent.getStringArrayExtra(Intent.EXTRA_BCC);
                    if (bcc != null)
                        try {
                            InternetAddress.parse(TextUtils.join(", ", bcc));
                            args.putString("bcc", TextUtils.join(", ", bcc));
                        } catch (AddressException ex) {
                            Log.w(ex);
                        }
                }

                if (intent.hasExtra(Intent.EXTRA_SUBJECT)) {
                    String subject = intent.getStringExtra(Intent.EXTRA_SUBJECT);
                    if (subject != null)
                        args.putString("subject", subject);
                }

                if (intent.hasExtra(Intent.EXTRA_HTML_TEXT)) {
                    String html = intent.getStringExtra(Intent.EXTRA_HTML_TEXT);
                    if (!TextUtils.isEmpty(html))
                        args.putString("body", html);
                } else if (intent.hasExtra(Intent.EXTRA_TEXT)) {
                    CharSequence body = intent.getCharSequenceExtra(Intent.EXTRA_TEXT);
                    if (body != null)
                        if (body instanceof Spanned)
                            args.putString("body", HtmlHelper.toHtml((Spanned) body));
                        else {
                            String text = body.toString();
                            if (!TextUtils.isEmpty(text)) {
                                String html = "<span>" + text.replaceAll("\\r?\\n", "<br>") + "</span>";
                                args.putString("body", html);
                            }
                        }
                }

                if (intent.hasExtra(Intent.EXTRA_STREAM))
                    if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
                        ArrayList<Uri> uris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                        if (uris != null)
                            args.putParcelableArrayList("attachments", uris);
                    } else {
                        Uri stream = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                        if (stream != null) {
                            ArrayList<Uri> uris = new ArrayList<>();
                            uris.add(stream);
                            args.putParcelableArrayList("attachments", uris);
                        }
                    }
            } else
                args = intent.getExtras();

            FragmentCompose fragment = new FragmentCompose();
            fragment.setArguments(args);

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("compose");
            fragmentTransaction.commit();
        }
    }

    @Override
    public void onBackStackChanged() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            Intent parent = getParentActivityIntent();
            if (parent != null)
                if (shouldUpRecreateTask(parent))
                    TaskStackBuilder.create(this)
                            .addNextIntentWithParentStack(parent)
                            .startActivities();
                else {
                    parent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(parent);
                }

            finishAndRemoveTask();
        }
    }
}
