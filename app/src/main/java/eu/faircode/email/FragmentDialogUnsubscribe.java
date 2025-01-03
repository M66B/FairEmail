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

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import java.io.IOException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class FragmentDialogUnsubscribe extends FragmentDialogBase {
    private static final int UNSUBSCRIBE_TIMEOUT = 20 * 1000;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Bundle args = getArguments();
        final String uri = args.getString("uri");
        final String from = args.getString("from");

        final Context context = getContext();

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_unsubscribe, null);
        final TextView tvSender = view.findViewById(R.id.tvSender);
        final TextView tvUri = view.findViewById(R.id.tvUri);

        tvSender.setText(from);
        tvUri.setVisibility(BuildConfig.DEBUG ? View.VISIBLE : View.GONE);
        tvUri.setText(uri);

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new SimpleTask<String>() {
                            @Override
                            protected String onExecute(Context context, Bundle args) throws Throwable {
                                final String uri = args.getString("uri");
                                final String request = "List-Unsubscribe=One-Click";

                                // https://datatracker.ietf.org/doc/html/rfc8058

                                URL url = new URL(uri);
                                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                                connection.setRequestMethod("POST");
                                connection.setDoInput(true);
                                connection.setDoOutput(true);
                                connection.setReadTimeout(UNSUBSCRIBE_TIMEOUT);
                                connection.setConnectTimeout(UNSUBSCRIBE_TIMEOUT);
                                ConnectionHelper.setUserAgent(context, connection);
                                connection.setRequestProperty("Content-Length", Integer.toString(request.length()));
                                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                                connection.connect();

                                try {
                                    connection.getOutputStream().write(request.getBytes());

                                    int status = connection.getResponseCode();
                                    if (status != HttpsURLConnection.HTTP_OK) {
                                        String error = "Error " + status + ": " + connection.getResponseMessage();
                                        String detail = Helper.readStream(connection.getErrorStream());
                                        throw new IOException(error + " " + detail);
                                    }

                                    return Helper.readStream(connection.getInputStream());
                                } finally {
                                    connection.disconnect();
                                }
                            }

                            @Override
                            protected void onExecuted(Bundle args, String output) {
                                ToastEx.makeText(context, R.string.title_completed, Toast.LENGTH_LONG).show();
                            }

                            @Override
                            protected void onException(Bundle args, Throwable ex) {
                                Log.unexpectedError(getParentFragment(), ex);
                            }
                        }.execute(FragmentDialogUnsubscribe.this, args, "unsubscribe");
                    }
                })
                .setNegativeButton(android.R.string.cancel, null);

        return builder.create();
    }
}
