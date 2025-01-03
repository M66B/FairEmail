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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.core.text.method.LinkMovementMethodCompat;
import androidx.preference.PreferenceManager;

import java.io.InputStream;

import io.noties.markwon.Markwon;
import io.noties.markwon.html.HtmlPlugin;

public class FragmentDialogMarkdown extends FragmentDialogBase {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Context context = getContext();

        String option = getArguments().getString("option");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        final View dview = LayoutInflater.from(context).inflate(R.layout.dialog_markdown, null);
        final TextView tvMarkdown = dview.findViewById(R.id.tvMarkdown);
        final CheckBox cbNotAgain = dview.findViewById(R.id.cbNotAgain);
        final ImageButton ibCancel = dview.findViewById(R.id.ibCancel);
        final ContentLoadingProgressBar pbWait = dview.findViewById(R.id.pbWait);
        final Group grpReady = dview.findViewById(R.id.grpReady);

        tvMarkdown.setText(null);

        cbNotAgain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
                prefs.edit().putBoolean(option, !checked).apply();
            }
        });

        ibCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        Dialog dialog = new Dialog(context);
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dview);

        new SimpleTask<Spanned>() {
            @Override
            protected void onPreExecute(Bundle args) {
                grpReady.setVisibility(View.GONE);
                cbNotAgain.setVisibility(View.GONE);
                pbWait.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onPostExecute(Bundle args) {
                grpReady.setVisibility(View.VISIBLE);
                cbNotAgain.setVisibility(TextUtils.isEmpty(option) ? View.GONE : View.VISIBLE);
                pbWait.setVisibility(View.GONE);
            }

            @Override
            protected Spanned onExecute(Context context, Bundle args) throws Throwable {
                String name = args.getString("name");

                String markdown;
                String asset = Helper.getLocalizedAsset(context, name);
                try (InputStream is = context.getAssets().open(asset)) {
                    markdown = Helper.readStream(is);
                }

                if ("CHANGELOG.md".equals(name)) {
                    int pos = markdown.indexOf("<!-- truncate here -->");
                    if (pos > 0)
                        markdown = markdown.substring(0, pos);
                }

                markdown = markdown
                        .replace("/FAQ.md#FAQ", "/FAQ.md#faq")
                        .replace("/FAQ.md#user-content-faq", "/FAQ.md#faq")
                        .replace(
                                "https://github.com/M66B/FairEmail/blob/master/FAQ.md",
                                "https://m66b.github.io/FairEmail/");

                Markwon markwon = Markwon.builder(context)
                        .usePlugin(HtmlPlugin.create())
                        .build();
                return markwon.toMarkdown(markdown);
            }

            @Override
            protected void onExecuted(Bundle args, Spanned markdown) {
                tvMarkdown.setText(markdown);
                tvMarkdown.setMovementMethod(LinkMovementMethodCompat.getInstance());
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, getArguments(), "markdown:read");

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }
}