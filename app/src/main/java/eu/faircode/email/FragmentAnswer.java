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
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.Group;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import org.jsoup.nodes.Document;

import static android.app.Activity.RESULT_OK;

public class FragmentAnswer extends FragmentBase {
    private ViewGroup view;
    private EditText etName;
    private EditText etGroup;
    private CheckBox cbStandard;
    private CheckBox cbFavorite;
    private CheckBox cbHide;
    private EditTextCompose etText;
    private BottomNavigationView style_bar;
    private BottomNavigationView bottom_navigation;
    private ContentLoadingProgressBar pbWait;
    private Group grpReady;

    private long id = -1;
    private long copy = -1;

    private static final int REQUEST_IMAGE = 1;
    private static final int REQUEST_LINK = 2;
    private final static int REQUEST_DELETE = 3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get arguments
        Bundle args = getArguments();
        if (args != null)
            if (args.getBoolean("copy"))
                copy = args.getLong("id", -1);
            else
                id = args.getLong("id", -1);
    }

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_answer_caption);
        setHasOptionsMenu(true);

        view = (ViewGroup) inflater.inflate(R.layout.fragment_answer, container, false);

        // Get controls
        etName = view.findViewById(R.id.etName);
        etGroup = view.findViewById(R.id.etGroup);
        cbStandard = view.findViewById(R.id.cbStandard);
        cbFavorite = view.findViewById(R.id.cbFavorite);
        cbHide = view.findViewById(R.id.cbHide);
        etText = view.findViewById(R.id.etText);

        style_bar = view.findViewById(R.id.style_bar);
        bottom_navigation = view.findViewById(R.id.bottom_navigation);

        pbWait = view.findViewById(R.id.pbWait);
        grpReady = view.findViewById(R.id.grpReady);

        etText.setSelectionListener(new EditTextCompose.ISelection() {
            @Override
            public void onSelected(boolean selection) {
                style_bar.setVisibility(selection ? View.VISIBLE : View.GONE);
            }
        });

        style_bar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                return onActionStyle(item.getItemId());
            }
        });

        bottom_navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_insert_image:
                        onInsertImage();
                        return true;
                    case R.id.action_delete:
                        onActionDelete();
                        return true;
                    case R.id.action_save:
                        onActionSave();
                        return true;
                    default:
                        return false;
                }
            }
        });

        // Initialize
        grpReady.setVisibility(View.GONE);
        style_bar.setVisibility(View.GONE);

        pbWait.setVisibility(View.VISIBLE);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle args = new Bundle();
        args.putLong("id", copy < 0 ? id : copy);

        new SimpleTask<EntityAnswer>() {
            @Override
            protected void onPreExecute(Bundle args) {
                pbWait.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onPostExecute(Bundle args) {
                pbWait.setVisibility(View.GONE);
            }

            @Override
            protected EntityAnswer onExecute(Context context, Bundle args) {
                long id = args.getLong("id");
                return DB.getInstance(context).answer().getAnswer(id);
            }

            @Override
            protected void onExecuted(Bundle args, EntityAnswer answer) {
                if (savedInstanceState == null) {
                    etName.setText(answer == null ? null : answer.name);
                    etGroup.setText(answer == null ? null : answer.group);
                    cbStandard.setChecked(answer == null ? false : answer.standard);
                    cbFavorite.setChecked(answer == null ? false : answer.favorite);
                    cbHide.setChecked(answer == null ? false : answer.hide);
                    if (answer == null)
                        etText.setText(null);
                    else
                        etText.setText(HtmlHelper.fromHtml(answer.text, false, new Html.ImageGetter() {
                            @Override
                            public Drawable getDrawable(String source) {
                                return ImageHelper.decodeImage(getContext(), -1, source, true, 0, 1.0f, etText);
                            }
                        }, null, getContext()));
                }

                bottom_navigation.findViewById(R.id.action_delete).setVisibility(answer == null ? View.GONE : View.VISIBLE);

                grpReady.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "answer:get");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_answer, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_help:
                onMenuHelp();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onMenuHelp() {
        new FragmentInfo().show(getParentFragmentManager(), "answer:info");
    }

    private void onInsertImage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("image/*");
        Helper.openAdvanced(intent);
        startActivityForResult(intent, REQUEST_IMAGE);
    }

    private void onActionDelete() {
        Bundle args = new Bundle();
        args.putString("question", getString(R.string.title_ask_delete_answer));

        FragmentDialogAsk fragment = new FragmentDialogAsk();
        fragment.setArguments(args);
        fragment.setTargetFragment(FragmentAnswer.this, REQUEST_DELETE);
        fragment.show(getParentFragmentManager(), "answer:delete");
    }

    private void onActionSave() {
        etText.clearComposingText();

        Bundle args = new Bundle();
        args.putLong("id", id);
        args.putString("name", etName.getText().toString().trim());
        args.putString("group", etGroup.getText().toString().trim());
        args.putBoolean("standard", cbStandard.isChecked());
        args.putBoolean("favorite", cbFavorite.isChecked());
        args.putBoolean("hide", cbHide.isChecked());
        args.putString("html", HtmlHelper.toHtml(etText.getText(), getContext()));

        new SimpleTask<Void>() {
            @Override
            protected void onPreExecute(Bundle args) {
                Helper.setViewsEnabled(view, false);
            }

            @Override
            protected void onPostExecute(Bundle args) {
                Helper.setViewsEnabled(view, true);
            }

            @Override
            protected Void onExecute(Context context, Bundle args) {
                long id = args.getLong("id");
                String name = args.getString("name");
                String group = args.getString("group");
                boolean standard = args.getBoolean("standard");
                boolean favorite = args.getBoolean("favorite");
                boolean hide = args.getBoolean("hide");
                String html = args.getString("html");

                if (TextUtils.isEmpty(name))
                    throw new IllegalArgumentException(context.getString(R.string.title_no_name));
                if (TextUtils.isEmpty(group))
                    group = null;

                Document document = JsoupEx.parse(html);

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    if (standard)
                        db.answer().resetStandard();

                    if (id < 0) {
                        EntityAnswer answer = new EntityAnswer();
                        answer.name = name;
                        answer.group = group;
                        answer.standard = standard;
                        answer.favorite = favorite;
                        answer.hide = hide;
                        answer.text = document.body().html();
                        answer.id = db.answer().insertAnswer(answer);
                    } else {
                        EntityAnswer answer = db.answer().getAnswer(id);
                        answer.name = name;
                        answer.group = group;
                        answer.standard = standard;
                        answer.favorite = favorite;
                        answer.hide = hide;
                        answer.text = document.body().html();
                        db.answer().updateAnswer(answer);
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                return null;
            }

            @Override
            protected void onExecuted(Bundle args, Void data) {
                finish();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (ex instanceof IllegalArgumentException)
                    Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG)
                            .setGestureInsetBottomIgnored(true).show();
                else
                    Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "answer:save");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            switch (requestCode) {
                case REQUEST_IMAGE:
                    if (resultCode == RESULT_OK && data != null)
                        onImageSelected(data.getData());
                    break;
                case REQUEST_LINK:
                    if (resultCode == RESULT_OK && data != null)
                        onLinkSelected(data.getBundleExtra("args"));
                    break;
                case REQUEST_DELETE:
                    if (resultCode == RESULT_OK)
                        onDelete();
                    break;
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    private void onImageSelected(Uri uri) {
        try {
            getContext().getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

            int start = etText.getSelectionStart();
            SpannableStringBuilder ssb = new SpannableStringBuilder(etText.getText());
            ssb.insert(start, " \uFFFC"); // Object replacement character
            String source = uri.toString();
            Drawable d = ImageHelper.decodeImage(getContext(), -1, source, true, 0, 1.0f, etText);
            ImageSpan is = new ImageSpan(d, source);
            ssb.setSpan(is, start + 1, start + 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            etText.setText(ssb);
            etText.setSelection(start + 2);
        } catch (SecurityException ex) {
            Snackbar sb = Snackbar.make(view, R.string.title_no_stream, Snackbar.LENGTH_INDEFINITE)
                    .setGestureInsetBottomIgnored(true);
            sb.setAction(R.string.title_info, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Helper.viewFAQ(getContext(), 49);
                }
            });
            sb.show();
        } catch (Throwable ex) {
            Log.unexpectedError(getParentFragmentManager(), ex);
        }
    }

    private void onLinkSelected(Bundle args) {
        String link = args.getString("link");
        int start = args.getInt("start");
        int end = args.getInt("end");
        etText.setSelection(start, end);
        StyleHelper.apply(R.id.menu_link, null, etText, link);
    }

    private void onDelete() {
        Bundle args = new Bundle();
        args.putLong("id", id);

        new SimpleTask<Void>() {
            @Override
            protected void onPreExecute(Bundle args) {
                Helper.setViewsEnabled(view, false);
            }

            @Override
            protected void onPostExecute(Bundle args) {
                Helper.setViewsEnabled(view, true);
            }

            @Override
            protected Void onExecute(Context context, Bundle args) {
                long id = args.getLong("id");
                DB.getInstance(context).answer().deleteAnswer(id);
                return null;
            }

            @Override
            protected void onExecuted(Bundle args, Void data) {
                finish();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "answer:delete");
    }

    private boolean onActionStyle(int action) {
        Log.i("Style action=" + action);

        if (action == R.id.menu_link) {
            Uri uri = null;

            ClipboardManager cbm = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            if (cbm != null && cbm.hasPrimaryClip()) {
                String link = cbm.getPrimaryClip().getItemAt(0).coerceToText(getContext()).toString();
                uri = Uri.parse(link);
                if (uri.getScheme() == null)
                    uri = null;
            }

            Bundle args = new Bundle();
            args.putParcelable("uri", uri);
            args.putInt("start", etText.getSelectionStart());
            args.putInt("end", etText.getSelectionEnd());

            FragmentDialogLink fragment = new FragmentDialogLink();
            fragment.setArguments(args);
            fragment.setTargetFragment(this, REQUEST_LINK);
            fragment.show(getParentFragmentManager(), "answer:link");

            return true;
        } else
            return StyleHelper.apply(action, view.findViewById(action), etText);
    }

    public static class FragmentInfo extends FragmentDialogBase {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            Spanned spanned = HtmlHelper.fromHtml("<p>" +
                    getString(R.string.title_answer_template_name) +
                    "<br>" +
                    getString(R.string.title_answer_template_email) +
                    "</p>", false, getContext());

            View dview = LayoutInflater.from(getContext()).inflate(R.layout.dialog_ask_again, null);
            TextView tvMessage = dview.findViewById(R.id.tvMessage);
            CheckBox cbNotAgain = dview.findViewById(R.id.cbNotAgain);

            tvMessage.setText(spanned);
            cbNotAgain.setVisibility(View.GONE);

            return new AlertDialog.Builder(getContext())
                    .setView(dview)
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
        }
    }
}
