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

    Copyright 2018-2021 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

public class ActivityError extends ActivityBase {
    static final int PI_ERROR = 1;
    static final int PI_ALERT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setSubtitle(getString(R.string.title_setup_error));

        View view = LayoutInflater.from(this).inflate(R.layout.activity_error, null);
        setContentView(view);

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvMessage = view.findViewById(R.id.tvMessage);
        Button btnPassword = view.findViewById(R.id.btnPassword);
        ImageButton ibSetting = view.findViewById(R.id.ibSetting);
        ImageButton ibInfo = view.findViewById(R.id.ibInfo);

        Intent intent = getIntent();
        String type = intent.getStringExtra("type");
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");
        String provider = intent.getStringExtra("provider");
        long account = intent.getLongExtra("account", -1L);
        int protocol = intent.getIntExtra("protocol", -1);
        int auth_type = intent.getIntExtra("auth_type", -1);
        int faq = intent.getIntExtra("faq", -1);

        tvTitle.setText(title);
        tvMessage.setMovementMethod(LinkMovementMethod.getInstance());
        tvMessage.setText(message);

        boolean outlook = (auth_type == ServiceAuthenticator.AUTH_TYPE_OAUTH &&
                ("office365".equals(provider) || "outlook".equals(provider)));
        btnPassword.setVisibility(outlook && BuildConfig.DEBUG ? View.VISIBLE : View.GONE);
        btnPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle args = new Bundle();
                args.putLong("id", account);

                new SimpleTask<Void>() {
                    @Override
                    protected Void onExecute(Context context, Bundle args) throws Throwable {
                        long id = args.getLong("id");

                        DB db = DB.getInstance(context);
                        try {
                            db.beginTransaction();

                            EntityAccount account = db.account().getAccount(id);
                            if (account == null)
                                return null;

                            if (account.auth_type == ServiceAuthenticator.AUTH_TYPE_OAUTH &&
                                    ("office365".equals(account.provider) ||
                                            "outlook".equals(account.provider))) {
                                account.auth_type = ServiceAuthenticator.AUTH_TYPE_PASSWORD;
                                account.password = "";
                                db.account().updateAccount(account);

                                List<EntityIdentity> identities = db.identity().getIdentities(account.id);
                                if (identities != null)
                                    for (EntityIdentity identity : identities)
                                        if (identity.auth_type == ServiceAuthenticator.AUTH_TYPE_OAUTH) {
                                            identity.auth_type = ServiceAuthenticator.AUTH_TYPE_PASSWORD;
                                            identity.password = "";
                                            db.identity().updateIdentity(identity);
                                        }
                            }

                            db.setTransactionSuccessful();
                        } finally {
                            db.endTransaction();
                        }

                        return null;
                    }

                    @Override
                    protected void onExecuted(Bundle args, Void data) {
                        startActivity(new Intent(ActivityError.this, ActivitySetup.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                .putExtra("target", "accounts")
                                .putExtra("id", account)
                                .putExtra("protocol", protocol));
                        finish();
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Log.unexpectedError(getSupportFragmentManager(), ex);
                    }
                }.execute(ActivityError.this, args, "error:password");
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
                Helper.viewFAQ(view.getContext(), faq);
            }
        });
    }
}
