package eu.faircode.email;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.InputStream;

import io.noties.markwon.Markwon;

public class FragmentDialogMarkdown extends FragmentDialogEx {
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
                try (InputStream is = context.getAssets().open(name)) {
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
                Helper.unexpectedError(getFragmentManager(), ex);
            }
        }.execute(getContext(), getActivity(), getArguments(), "markdown:read");

        return dialog;
    }
}
