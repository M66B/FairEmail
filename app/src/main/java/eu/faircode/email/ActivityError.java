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

    Copyright 2018-2023 by Marcel Bokhorst (M66B)
*/

import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class ActivityError extends ActivityBase {
    static final int PI_ERROR = 1;
    static final int PI_ALERT = 2;

    private TextView tvTitle;
    private TextView tvMessage;
    private Button btnPassword;
    private ImageButton ibSetting;
    private ImageButton ibInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setSubtitle(getString(R.string.title_setup_error));
        setContentView(R.layout.activity_error);

        tvTitle = findViewById(R.id.tvTitle);
        tvMessage = findViewById(R.id.tvMessage);
        btnPassword = findViewById(R.id.btnPassword);
        ibSetting = findViewById(R.id.ibSetting);
        ibInfo = findViewById(R.id.ibInfo);

        load();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        load();
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
        boolean authorize = intent.getBooleanExtra("authorize", false);
        String personal = intent.getStringExtra("personal");
        String address = intent.getStringExtra("address");
        int faq = intent.getIntExtra("faq", -1);

        tvTitle.setText(title);
        tvMessage.setMovementMethod(LinkMovementMethod.getInstance());
        tvMessage.setText(message);

        btnPassword.setText(authorize ? R.string.title_setup_oauth_authorize : R.string.title_password);
        btnPassword.setCompoundDrawablesRelativeWithIntrinsicBounds(
                0, 0,
                authorize ? R.drawable.twotone_check_24 : R.drawable.twotone_edit_24, 0);

        btnPassword.setVisibility(account < 0 ? View.GONE : View.VISIBLE);
        btnPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (authorize)
                    startActivity(new Intent(ActivityError.this, ActivitySetup.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            .putExtra("target", "oauth")
                            .putExtra("id", provider)
                            .putExtra("name", "Outlook")
                            .putExtra("askAccount", true)
                            .putExtra("askTenant", true)
                            .putExtra("personal", personal)
                            .putExtra("address", address));
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
                Helper.viewFAQ(view.getContext(), faq);
            }
        });
    }
}
