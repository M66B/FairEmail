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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

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
        final Button btnUnsubscribe = view.findViewById(R.id.btnUnsubscribe);
        final ProgressBar pbUnsubscribe = view.findViewById(R.id.pbUnsubscribe);

        tvSender.setText(from);
        tvUri.setText(uri);
        pbUnsubscribe.setVisibility(View.GONE);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .setNeutralButton(android.R.string.cancel, null)
                .create();

        btnUnsubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SimpleTask<String>() {
                    @Override
                    protected void onPreExecute(Bundle args) {
                        btnUnsubscribe.setEnabled(false);
                        pbUnsubscribe.setVisibility(View.VISIBLE);
                    }

                    @Override
                    protected void onPostExecute(Bundle args) {
                        btnUnsubscribe.setEnabled(true);
                        pbUnsubscribe.setVisibility(View.GONE);
                    }

                    @Override
                    protected String onExecute(Context context, Bundle args) throws Throwable {
                        final String uri = args.getString("uri");
                        final String request = "List-Unsubscribe=One-Click";

                        // https://datatracker.ietf.org/doc/html/rfc8058

                        int redirects = 0;
                        URL url = new URL(uri);
                        do {
                            Log.i("Unsubscribe request=" + request + " uri=" + uri);

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
                                if (status == HttpURLConnection.HTTP_MOVED_PERM ||
                                        status == HttpURLConnection.HTTP_MOVED_TEMP ||
                                        status == HttpURLConnection.HTTP_SEE_OTHER ||
                                        status == 307 /* Temporary redirect */ ||
                                        status == 308 /* Permanent redirect */) {
                                    String header = connection.getHeaderField("Location");
                                    if (header != null) {
                                        String location = URLDecoder.decode(header, StandardCharsets.UTF_8.name());
                                        Log.i("Unsubscribe redirect=" + location);
                                        url = new URL(url, location);
                                        continue;
                                    }
                                }

                                if (status >= 300) {
                                    String error = status + ": " + connection.getResponseMessage();
                                    Log.i("Unsubscribe error=" + error);
                                    throw new IllegalArgumentException(error);
                                } else
                                    Log.i("Unsubscribe status=" + status);

                                return Helper.readStream(connection.getInputStream());
                            } finally {
                                connection.disconnect();
                            }
                        } while (++redirects <= ConnectionHelper.MAX_REDIRECTS);

                        return null;
                    }

                    @Override
                    protected void onExecuted(Bundle args, String output) {
                        ToastEx.makeText(context, R.string.title_completed, Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        dialog.dismiss();
                        if (ex instanceof IllegalArgumentException || ex instanceof ConnectException)
                            ToastEx.makeText(context,
                                    context.getString(R.string.title_unsubscribe_error, ex.getMessage()),
                                    Toast.LENGTH_LONG).show();
                        else
                            Log.unexpectedError(getParentFragmentManager(), ex);
                    }
                }.execute(FragmentDialogUnsubscribe.this, args, "unsubscribe");
            }
        });


        return dialog;
    }
}
