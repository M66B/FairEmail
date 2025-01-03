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

import static eu.faircode.email.ServiceAuthenticator.AUTH_TYPE_PASSWORD;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.Group;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class FragmentDialogPwned extends FragmentDialogBase {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Context context = getContext();
        final View dview = LayoutInflater.from(context).inflate(R.layout.dialog_pwned, null);
        final TextView tvCaption = dview.findViewById(R.id.tvCaption);
        final TextView tvPwned = dview.findViewById(R.id.tvPwned);
        final Button btnCheck = dview.findViewById(R.id.btnCheck);
        final ContentLoadingProgressBar pbCheck = dview.findViewById(R.id.pbCheck);
        final ImageButton ibPwned = dview.findViewById(R.id.ibPwned);
        final TextView tvRemark = dview.findViewById(R.id.tvRemark);
        final TextView tvPrivacy = dview.findViewById(R.id.tvPrivacy);
        final Group grpReady = dview.findViewById(R.id.grpReady);

        final int colorError = Helper.resolveColor(context, androidx.appcompat.R.attr.colorError);
        final int colorVerified = Helper.resolveColor(context, R.attr.colorVerified);

        tvCaption.getPaint().setUnderlineText(true);
        tvCaption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.view(v.getContext(), Uri.parse(BuildConfig.PWNED_URI + "Passwords"), true);
            }
        });

        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SimpleTask<SpannableStringBuilder>() {
                    @Override
                    protected void onPreExecute(Bundle args) {
                        btnCheck.setEnabled(false);
                        pbCheck.setVisibility(View.VISIBLE);
                        grpReady.setVisibility(View.GONE);
                    }

                    @Override
                    protected void onPostExecute(Bundle args) {
                        btnCheck.setEnabled(true);
                        pbCheck.setVisibility(View.GONE);
                        grpReady.setVisibility(View.VISIBLE);
                    }

                    @Override
                    protected SpannableStringBuilder onExecute(Context context, Bundle args) throws Throwable {
                        SpannableStringBuilder ssb = new SpannableStringBuilder();

                        DB db = DB.getInstance(context);
                        List<EntityAccount> accounts = db.account().getAccounts();
                        if (accounts != null)
                            for (EntityAccount account : accounts)
                                if (account.auth_type == AUTH_TYPE_PASSWORD &&
                                        !TextUtils.isEmpty(account.password)) {
                                    check(context, account.name, account.password, ssb);
                                    List<EntityIdentity> identities = db.identity().getIdentities(account.id);
                                    if (identities != null)
                                        for (EntityIdentity identity : identities)
                                            if (identity.auth_type == AUTH_TYPE_PASSWORD &&
                                                    !account.password.equals(identity.password))
                                                check(context, account.name + "/" + identity.user, identity.password, ssb);
                                }

                        return ssb;
                    }

                    private void check(Context context, String name, String password, SpannableStringBuilder ssb) throws NoSuchAlgorithmException, IOException {
                        Integer count = HaveIBeenPwned.check(password, context);
                        boolean pwned = (count != null && count != 0);

                        if (ssb.length() > 0)
                            ssb.append('\n');

                        ssb.append(name).append(": ");
                        int start = ssb.length();
                        ssb.append(pwned ? "PWNED!" : "OK");

                        if (pwned) {
                            ssb.setSpan(new ForegroundColorSpan(colorError), start, ssb.length(), 0);
                            ssb.setSpan(new StyleSpan(Typeface.BOLD), start, ssb.length(), 0);
                            ssb.append(' ').append(Integer.toString(count)).append('×');
                            if (BuildConfig.DEBUG)
                                ssb.append(' ').append(password);
                        } else
                            ssb.setSpan(new ForegroundColorSpan(colorVerified), start, ssb.length(), 0);
                    }

                    @Override
                    protected void onExecuted(Bundle args, SpannableStringBuilder ssb) {
                        tvPwned.setText(ssb);
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        tvPwned.setText(Log.formatThrowable(ex));
                    }
                }.execute(FragmentDialogPwned.this, new Bundle(), "pwned");
            }
        });

        ibPwned.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.view(v.getContext(), Uri.parse(BuildConfig.PWNED_URI + "Passwords"), true);
            }
        });

        tvRemark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.view(v.getContext(), Uri.parse("https://en.wikipedia.org/wiki/K-anonymity"), true);
            }
        });

        tvPrivacy.getPaint().setUnderlineText(true);
        tvPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.view(v.getContext(), Uri.parse(BuildConfig.PWNED_URI + "Privacy"), true);
            }
        });

        pbCheck.setVisibility(View.GONE);
        grpReady.setVisibility(View.GONE);

        return new AlertDialog.Builder(context)
                .setView(dview)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                    }
                })
                .create();
    }
}

