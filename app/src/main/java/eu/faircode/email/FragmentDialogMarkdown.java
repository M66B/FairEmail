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

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import io.noties.markwon.Markwon;

public class FragmentDialogMarkdown extends FragmentDialogBase {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final View dview = LayoutInflater.from(getContext()).inflate(R.layout.dialog_markdown, null);
        final TextView tvMarkdown = dview.findViewById(R.id.tvMarkdown);
        final ContentLoadingProgressBar pbWait = dview.findViewById(R.id.pbWait);

        tvMarkdown.setText(null);

        Dialog dialog = new Dialog(getContext());
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dview);
        dialog.getWindow().setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT);

        new SimpleTask<Spanned>() {
            @Override
            protected void onPreExecute(Bundle args) {
                tvMarkdown.setVisibility(View.GONE);
                pbWait.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onPostExecute(Bundle args) {
                tvMarkdown.setVisibility(View.VISIBLE);
                pbWait.setVisibility(View.GONE);
            }

            @Override
            protected Spanned onExecute(Context context, Bundle args) throws Throwable {
                String name = args.getString("name");
                if (name == null || !name.contains("."))
                    throw new IllegalArgumentException(name);

                List<String> names = new ArrayList<>();
                String[] c = name.split("\\.");
                List<String> assets = Arrays.asList(getResources().getAssets().list(""));

                List<Locale> locales = new ArrayList<>();
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
                    locales.add(Locale.getDefault());
                else {
                    LocaleList ll = context.getResources().getConfiguration().getLocales();
                    for (int i = 0; i < ll.size(); i++)
                        locales.add(ll.get(i));
                }

                for (Locale locale : locales) {
                    String language = locale.getLanguage();
                    String country = locale.getCountry();
                    if ("en".equals(language) && "US".equals(country))
                        names.add(name);
                    else {
                        String localized = c[0] + "-" + language + "-r" + country + "." + c[1];
                        if (assets.contains(localized))
                            names.add(localized);
                    }
                }

                for (Locale locale : locales) {
                    String prefix = c[0] + "-" + locale.getLanguage();
                    for (String asset : assets)
                        if (asset.startsWith(prefix))
                            names.add(asset);
                }

                names.add(name);
                String asset = names.get(0);

                Log.i("Using " + asset +
                        " of " + TextUtils.join(",", names) +
                        " (" + TextUtils.join(",", locales) + ")");
                try (InputStream is = context.getAssets().open(asset)) {
                    byte[] buffer = new byte[is.available()];
                    is.read(buffer);
                    Markwon markwon = Markwon.create(context);
                    return markwon.toMarkdown(new String(buffer));
                }
            }

            @Override
            protected void onExecuted(Bundle args, Spanned markdown) {
                tvMarkdown.setText(markdown);
                tvMarkdown.setMovementMethod(LinkMovementMethod.getInstance());
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, getArguments(), "markdown:read");

        return dialog;
    }
}
