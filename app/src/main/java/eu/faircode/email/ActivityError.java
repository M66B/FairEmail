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
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.core.text.method.LinkMovementMethodCompat;

import java.util.List;

public class ActivityError extends ActivityBase {
    static final int PI_ERROR = 1;
    static final int PI_ALERT = 2;

    private TextView tvTitle;
    private TextView tvMessage;
    private TextView tvCertificate;
    private Button btnPassword;
    private ImageButton ibSetting;
    private ImageButton ibInfo;
    private Button btnReload;
    private Button btnSupport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_error);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setSubtitle(getString(R.string.title_setup_error));

        tvTitle = findViewById(R.id.tvTitle);
        tvMessage = findViewById(R.id.tvMessage);
        tvCertificate = findViewById(R.id.tvCertificate);
        btnPassword = findViewById(R.id.btnPassword);
        ibSetting = findViewById(R.id.ibSetting);
        ibInfo = findViewById(R.id.ibInfo);
        btnReload = findViewById(R.id.btnReload);
        btnSupport = findViewById(R.id.btnSupport);

        load();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        load();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void load() {
        Intent intent = getIntent();
        String type = intent.getStringExtra("type");
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");
        String provider = intent.getStringExtra("provider");
        long account = intent.getLongExtra("account", -1L);
        long identity = intent.getLongExtra("identity", -1L);
        int protocol = intent.getIntExtra("protocol", -1);
        int auth_type = intent.getIntExtra("auth_type", -1);
        String host = intent.getStringExtra("host");
        int faq = intent.getIntExtra("faq", -1);

        boolean isCertificateException = (message != null && message.contains("CertificateException"));

        tvTitle.setText(title);
        tvMessage.setMovementMethod(LinkMovementMethodCompat.getInstance());
        tvMessage.setText(message);

        tvCertificate.setVisibility(
                isCertificateException && !SSLHelper.customTrustManager()
                        ? View.VISIBLE : View.GONE);

        boolean password = (auth_type == ServiceAuthenticator.AUTH_TYPE_PASSWORD);
        boolean outlook = ("outlook.office365.com".equalsIgnoreCase(host) ||
                "smtp.office365.com".equalsIgnoreCase(host) ||
                "imap-mail.outlook.com".equalsIgnoreCase(host) ||
                "smtp-mail.outlook.com".equalsIgnoreCase(host));

        btnPassword.setText(password && !outlook ? R.string.title_password : R.string.title_setup_oauth_authorize);
        btnPassword.setCompoundDrawablesRelativeWithIntrinsicBounds(
                0, 0,
                password ? R.drawable.twotone_edit_24 : R.drawable.twotone_check_24, 0);

        btnPassword.setVisibility(account < 0 ? View.GONE : View.VISIBLE);
        btnPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (auth_type == ServiceAuthenticator.AUTH_TYPE_GMAIL)
                    startActivity(new Intent(ActivityError.this, ActivitySetup.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            .putExtra("target", "gmail")
                            .putExtra("personal", intent.getStringExtra("personal"))
                            .putExtra("address", intent.getStringExtra("address")));
                else if (auth_type == ServiceAuthenticator.AUTH_TYPE_OAUTH) {
                    try {
                        EmailProvider eprovider = EmailProvider.getProvider(ActivityError.this, provider);
                        startActivity(new Intent(ActivityError.this, ActivitySetup.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                .putExtra("target", "oauth")
                                .putExtra("id", eprovider.id)
                                .putExtra("name", eprovider.description)
                                .putExtra("privacy", eprovider.oauth.privacy)
                                .putExtra("askAccount", eprovider.oauth.askAccount)
                                .putExtra("askTenant", eprovider.oauth.askTenant())
                                .putExtra("personal", intent.getStringExtra("personal"))
                                .putExtra("address", intent.getStringExtra("address")));
                    } catch (Throwable ex) {
                        Log.e(ex);
                        startActivity(new Intent(ActivityError.this, ActivitySetup.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    }
                } else if (auth_type == ServiceAuthenticator.AUTH_TYPE_GRAPH ||
                        (auth_type == ServiceAuthenticator.AUTH_TYPE_PASSWORD && outlook))
                    startActivity(new Intent(ActivityError.this, ActivitySetup.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            .putExtra("target", "oauth")
                            .putExtra("id", "outlookgraph")
                            .putExtra("name", "Outlook")
                            .putExtra("askAccount", true)
                            .putExtra("askTenant", true)
                            .putExtra("personal", intent.getStringExtra("personal"))
                            .putExtra("address", intent.getStringExtra("address")));
                else
                    startActivity(new Intent(ActivityError.this, ActivitySetup.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            .putExtra("target", "accounts")
                            .putExtra("id", account)
                            .putExtra("protocol", protocol));
                finish();
            }
        });

        ibSetting.setVisibility(account < 0 ? View.GONE : View.VISIBLE);
        ibSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.getContext().startActivity(new Intent(v.getContext(), ActivitySetup.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .putExtra("target", "accounts")
                        .putExtra("id", account)
                        .putExtra("protocol", protocol));
            }
        });

        ibInfo.setVisibility(faq > 0 ? View.VISIBLE : View.GONE);
        ibInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Helper.viewFAQ(view.getContext(), isCertificateException ? 4 : faq);
            }
        });

        btnReload.setVisibility(account > 0 ? View.VISIBLE : View.GONE);
        btnReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ServiceSynchronize.reload(v.getContext(), account, true, "retry");
                finish();
            }
        });

        btnSupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuilder sb = new StringBuilder();

                sb.append("auth_type=")
                        .append(ServiceAuthenticator.getAuthTypeName(auth_type))
                        .append("\n");

                if (!TextUtils.isEmpty(host))
                    sb.append("host=")
                            .append(host)
                            .append("\n");

                if (account > 0)
                    sb.append("protocol=")
                            .append(protocol == EntityAccount.TYPE_IMAP ? "imap" : "pop3")
                            .append("\n");

                if (!TextUtils.isEmpty(provider))
                    sb.append("provider=")
                            .append(provider)
                            .append("\n");

                if (!TextUtils.isEmpty(message))
                    sb.append(Helper.limit(message, 384));

                Uri uri = Helper.getSupportUri(v.getContext(), "Sync:error")
                        .buildUpon()
                        .appendQueryParameter("message", sb.toString())
                        .build();
                Helper.view(v.getContext(), uri, true);
            }
        });

        Bundle args = new Bundle();
        args.putLong("account", account);

        new SimpleTask<EntityIdentity>() {
            @Override
            protected EntityIdentity onExecute(Context context, Bundle args) throws Throwable {
                long account = args.getLong("account");

                DB db = DB.getInstance(context);
                List<EntityIdentity> identities = db.identity().getIdentities(account);
                if (identities == null)
                    return null;
                if (identities.size() == 1)
                    return identities.get(0);
                for (EntityIdentity identity : identities)
                    if (identity.primary)
                        return identity;
                return null;
            }

            @Override
            protected void onExecuted(Bundle args, EntityIdentity identity) {
                if (identity == null)
                    return;
                intent.putExtra("personal", identity.name);
                intent.putExtra("address", identity.email);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                // Ignored
            }
        }.execute(this, args, "error:details");
    }
}
