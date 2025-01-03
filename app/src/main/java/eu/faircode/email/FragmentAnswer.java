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

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.text.style.SuggestionSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import org.github.DetectHtml;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FragmentAnswer extends FragmentBase {
    private ViewGroup view;
    private EditText etName;
    private EditText etLabel;
    private AutoCompleteTextView etGroup;
    private CheckBox cbStandard;
    private CheckBox cbReceipt;
    private CheckBox cbAI;
    private CheckBox cbFavorite;
    private CheckBox cbSnippet;
    private CheckBox cbHide;
    private CheckBox cbExternal;
    private ViewButtonColor btnColor;
    private EditTextCompose etText;
    private HorizontalScrollView style_bar;
    private BottomNavigationView bottom_navigation;
    private ContentLoadingProgressBar pbWait;
    private Group grpReady;

    private ArrayAdapter<String> adapterGroup;

    private long id = -1;
    private long copy = -1;

    private static final int REQUEST_COLOR = 1;
    private static final int REQUEST_IMAGE = 2;
    private static final int REQUEST_FILE = 3;
    private static final int REQUEST_LINK = 4;
    private final static int REQUEST_DELETE = 5;

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
        final Context context = getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String compose_font = prefs.getString("compose_font", "");
        boolean compact = prefs.getBoolean("compose_compact", false);
        int zoom = prefs.getInt("compose_zoom", compact ? 0 : 1);
        boolean editor_zoom = prefs.getBoolean("editor_zoom", true);
        int message_zoom = (editor_zoom ? prefs.getInt("message_zoom", 100) : 100);

        setSubtitle(R.string.title_answer_caption);
        setHasOptionsMenu(true);

        view = (ViewGroup) inflater.inflate(R.layout.fragment_answer, container, false);

        // Get controls
        etName = view.findViewById(R.id.etName);
        etLabel = view.findViewById(R.id.etLabel);
        etGroup = view.findViewById(R.id.etGroup);
        cbStandard = view.findViewById(R.id.cbStandard);
        cbReceipt = view.findViewById(R.id.cbReceipt);
        cbAI = view.findViewById(R.id.cbAI);
        cbFavorite = view.findViewById(R.id.cbFavorite);
        cbSnippet = view.findViewById(R.id.cbSnippet);
        cbHide = view.findViewById(R.id.cbHide);
        cbExternal = view.findViewById(R.id.cbExternal);
        btnColor = view.findViewById(R.id.btnColor);
        etText = view.findViewById(R.id.etText);

        style_bar = view.findViewById(R.id.style_bar);
        bottom_navigation = view.findViewById(R.id.bottom_navigation);

        pbWait = view.findViewById(R.id.pbWait);
        grpReady = view.findViewById(R.id.grpReady);

        adapterGroup = new ArrayAdapter<>(getContext(), R.layout.spinner_item1_dropdown, android.R.id.text1);
        etGroup.setThreshold(1);
        etGroup.setAdapter(adapterGroup);

        btnColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putInt("color", btnColor.getColor());
                args.putString("title", getString(R.string.title_color));
                args.putBoolean("reset", true);

                FragmentDialogColor fragment = new FragmentDialogColor();
                fragment.setArguments(args);
                fragment.setTargetFragment(FragmentAnswer.this, REQUEST_COLOR);
                fragment.show(getParentFragmentManager(), "account:color");
            }
        });

        etText.setTypeface(StyleHelper.getTypeface(compose_font, context));

        float textSize = Helper.getTextSize(context, zoom);
        if (textSize != 0)
            etText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize * message_zoom / 100f);

        etText.setSelectionListener(new EditTextCompose.ISelection() {
            @Override
            public void onSelected(boolean selection) {
                style_bar.setVisibility(selection ? View.VISIBLE : View.GONE);
            }
        });

        etText.addTextChangedListener(StyleHelper.getTextWatcher(etText));

        StyleHelper.wire(getViewLifecycleOwner(), view, etText);

        bottom_navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.action_insert_image) {
                    onInsertImage();
                    return true;
                } else if (itemId == R.id.action_attach_file) {
                    onAttachFile();
                    return true;
                } else if (itemId == R.id.action_insert_link) {
                    onInsertLink();
                    return true;
                } else if (itemId == R.id.action_delete) {
                    onActionDelete();
                    return true;
                } else if (itemId == R.id.action_save) {
                    onActionSave();
                    return true;
                }
                return false;
            }
        });

        // Initialize
        etLabel.setVisibility(BuildConfig.DEBUG ? View.VISIBLE : View.GONE);
        cbAI.setVisibility(View.GONE);
        cbExternal.setVisibility(View.GONE);
        cbSnippet.setVisibility(View.GONE);
        grpReady.setVisibility(View.GONE);
        style_bar.setVisibility(View.GONE);
        bottom_navigation.setVisibility(View.GONE);

        pbWait.setVisibility(View.VISIBLE);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle args = new Bundle();
        args.putLong("id", copy < 0 ? id : copy);

        Bundle a = getArguments();
        if (a != null) {
            args.putString("subject", a.getString("subject"));
            args.putString("html", a.getString("html"));
        }

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

                DB db = DB.getInstance(context);
                EntityAnswer answer = db.answer().getAnswer(id);

                String html = (answer == null ? args.getString("html") : answer.text);
                if (html != null) {
                    Document d = HtmlHelper.sanitizeCompose(context, html, true);
                    Spanned spanned = HtmlHelper.fromDocument(context, d, new HtmlHelper.ImageGetterEx() {
                        @Override
                        public Drawable getDrawable(Element element) {
                            String source = element.attr("src");
                            if (source.startsWith("cid:"))
                                element.attr("src", "cid:");
                            return ImageHelper.decodeImage(context,
                                    -1, element, true, 0, 1.0f, etText);
                        }
                    }, null);
                    args.putCharSequence("spanned", spanned);
                }

                args.putStringArrayList("groups", new ArrayList<>(db.answer().getGroups()));

                return answer;
            }

            @Override
            protected void onExecuted(Bundle args, EntityAnswer answer) {
                final Context context = getContext();

                if (copy > 0 && answer != null) {
                    answer.applied = 0;
                    answer.last_applied = null;
                }

                if (savedInstanceState == null) {
                    etName.setText(answer == null ? args.getString("subject") : answer.name);
                    etLabel.setText(answer == null ? null : answer.label);
                    etGroup.setText(answer == null ? null : answer.group);
                    cbStandard.setChecked(answer == null ? false : answer.standard);
                    cbReceipt.setChecked(answer == null ? false : answer.receipt);
                    cbAI.setChecked(answer == null ? false : answer.ai);
                    cbFavorite.setChecked(answer == null ? false : answer.favorite);
                    cbSnippet.setChecked(answer == null ? false : answer.snippet);
                    cbHide.setChecked(answer == null ? false : answer.hide);
                    cbExternal.setChecked(answer == null ? false : answer.external);
                    btnColor.setColor(answer == null ? null : answer.color);
                    etText.setText((Spanned) args.getCharSequence("spanned"));
                }

                adapterGroup.clear();
                adapterGroup.addAll(args.getStringArrayList("groups"));

                if (answer == null)
                    bottom_navigation.getMenu().removeItem(R.id.action_delete);

                if (ActivityAnswer.canAnswer(context))
                    cbExternal.setVisibility(View.VISIBLE);
                cbAI.setVisibility(AI.isAvailable(context) ? View.VISIBLE : View.GONE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    cbSnippet.setVisibility(View.VISIBLE);
                grpReady.setVisibility(View.VISIBLE);
                bottom_navigation.setVisibility(View.VISIBLE);
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

        Menu smenu = menu.findItem(R.id.menu_placeholders).getSubMenu();

        List<String> names = EntityAnswer.getCustomPlaceholders(getContext());
        for (int i = 0; i < names.size(); i++)
            smenu.add(Menu.FIRST, i + 1, i + 1, names.get(i));

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        menu.findItem(R.id.menu_placeholder_firstname).setVisible(BuildConfig.DEBUG);
        menu.findItem(R.id.menu_placeholder_lastname).setVisible(BuildConfig.DEBUG);

        bottom_navigation.findViewById(R.id.action_save).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (LanguageTool.isEnabled(v.getContext())) {
                    onLanguageTool();
                    return true;
                } else
                    return false;
            }
        });

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getGroupId() == Menu.FIRST) {
            String name = item.getTitle().toString();
            onMenuPlaceholder("$" + name + "$");
            return true;
        } else {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_help) {
                onMenuHelp();
                return true;
            } else if (itemId == R.id.menu_placeholder_name) {
                onMenuPlaceholder("$name$");
                return true;
            } else if (itemId == R.id.menu_placeholder_email) {
                onMenuPlaceholder("$email$");
                return true;
            } else if (itemId == R.id.menu_placeholder_firstname) {
                onMenuPlaceholder("$firstname$");
                return true;
            } else if (itemId == R.id.menu_placeholder_lastname) {
                onMenuPlaceholder("$lastname$");
                return true;
            } else if (itemId == R.id.menu_placeholder_date) {
                onMenuPlaceholder("$date$");
                return true;
            } else if (itemId == R.id.menu_placeholder_weekday) {
                onMenuPlaceholder("$weekday$");
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void onMenuHelp() {
        Helper.viewFAQ(getContext(), 179);
    }

    private void onMenuPlaceholder(String name) {
        int start = etText.getSelectionStart();
        int end = etText.getSelectionEnd();
        if (start > end) {
            int tmp = start;
            start = end;
            end = tmp;
        }

        if (start >= 0 && start < end)
            etText.getText().replace(start, end, name);
        else {
            if (start < 0) {
                start = etText.length() - 1;
                if (start < 0)
                    start = 0;
            }

            etText.getText().insert(start, name);
        }
    }

    private void onInsertImage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("image/*");
        Helper.openAdvanced(getContext(), intent);
        startActivityForResult(intent, REQUEST_IMAGE);
    }

    private void onAttachFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("*/*");
        Helper.openAdvanced(getContext(), intent);
        startActivityForResult(intent, REQUEST_FILE);
    }

    private void onInsertLink() {
        FragmentDialogInsertLink fragment = new FragmentDialogInsertLink();
        fragment.setArguments(FragmentDialogInsertLink.getArguments(etText));
        fragment.setTargetFragment(this, REQUEST_LINK);
        fragment.show(getParentFragmentManager(), "answer:link");
    }

    private void onActionDelete() {
        Bundle args = new Bundle();
        args.putString("question", getString(R.string.title_ask_delete_answer));
        args.putBoolean("warning", true);

        FragmentDialogAsk fragment = new FragmentDialogAsk();
        fragment.setArguments(args);
        fragment.setTargetFragment(FragmentAnswer.this, REQUEST_DELETE);
        fragment.show(getParentFragmentManager(), "answer:delete");
    }

    private void onActionSave() {
        HtmlHelper.clearComposingText(etText);

        // Prevent splitting placeholders
        Editable edit = etText.getText();
        SuggestionSpan[] suggestions = edit.getSpans(0, etText.length(), SuggestionSpan.class);
        for (SuggestionSpan suggestion : suggestions)
            edit.removeSpan(suggestion);

        Spanned spanned = etText.getText();
        String html = (!BuildConfig.PLAY_STORE_RELEASE && DetectHtml.isHtml(spanned.toString())
                ? spanned.toString()
                : HtmlHelper.toHtml(spanned, getContext()));

        Bundle args = new Bundle();
        args.putLong("id", id);
        args.putString("name", etName.getText().toString().trim());
        args.putString("label", etLabel.getText().toString().trim());
        args.putString("group", etGroup.getText().toString().trim());
        args.putBoolean("standard", cbStandard.isChecked());
        args.putBoolean("receipt", cbReceipt.isChecked());
        args.putBoolean("ai", cbAI.isChecked());
        args.putBoolean("favorite", cbFavorite.isChecked());
        args.putBoolean("snippet", cbSnippet.isChecked());
        args.putBoolean("hide", cbHide.isChecked());
        args.putBoolean("external", cbExternal.isChecked());
        args.putInt("color", btnColor.getColor());
        args.putString("html", html);

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
                String label = args.getString("label");
                String group = args.getString("group");
                boolean standard = args.getBoolean("standard");
                boolean receipt = args.getBoolean("receipt");
                boolean ai = args.getBoolean("ai");
                boolean favorite = args.getBoolean("favorite");
                boolean snippet = args.getBoolean("snippet");
                boolean hide = args.getBoolean("hide");
                boolean external = args.getBoolean("external");
                Integer color = args.getInt("color");
                String html = args.getString("html");

                if (TextUtils.isEmpty(name))
                    throw new IllegalArgumentException(context.getString(R.string.title_no_name));
                if (TextUtils.isEmpty(label))
                    label = null;
                if (TextUtils.isEmpty(group))
                    group = null;
                if (color == Color.TRANSPARENT)
                    color = null;

                Document document = JsoupEx.parse(html);

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    if (standard)
                        db.answer().resetStandard();
                    if (receipt)
                        db.answer().resetReceipt();

                    EntityAnswer answer;
                    if (id < 0)
                        answer = new EntityAnswer();
                    else
                        answer = db.answer().getAnswer(id);

                    answer.name = name;
                    answer.label = label;
                    answer.group = group;
                    answer.standard = standard;
                    answer.receipt = receipt;
                    answer.ai = ai;
                    answer.favorite = favorite;
                    answer.snippet = snippet;
                    answer.hide = hide;
                    answer.external = external;
                    answer.color = color;
                    answer.text = document.body().html();

                    if (id < 0)
                        answer.id = db.answer().insertAnswer(answer);
                    else
                        db.answer().updateAnswer(answer);

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
                    Helper.setSnackbarOptions(
                                    Snackbar.make(view, new ThrowableWrapper(ex).getSafeMessage(), Snackbar.LENGTH_LONG))
                            .show();
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
                case REQUEST_COLOR:
                    if (resultCode == RESULT_OK && data != null) {
                        Bundle args = data.getBundleExtra("args");
                        btnColor.setColor(args.getInt("color"));
                    }
                    break;
                case REQUEST_IMAGE:
                    if (resultCode == RESULT_OK && data != null)
                        onImageSelected(data.getData());
                    break;
                case REQUEST_FILE:
                    if (resultCode == RESULT_OK && data != null)
                        onFileSelected(data.getData());
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
            NoStreamException.check(uri, getContext());

            getContext().getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if (!Helper.isPersisted(getContext(), uri, true, false))
                throw new IllegalStateException("No permission granted to access selected image " + uri);

            int start = etText.getSelectionStart();
            SpannableStringBuilder ssb = new SpannableStringBuilderEx(etText.getText());
            ssb.insert(start, "\n\uFFFC\n"); // Object replacement character
            String source = uri.toString();
            Drawable d = ImageHelper.decodeImage(getContext(), -1, source, true, 0, 1.0f, etText);
            ImageSpan is = new ImageSpan(d, source);
            ssb.setSpan(is, start + 1, start + 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            etText.setText(ssb);
            etText.setSelection(start + 3);
        } catch (NoStreamException ex) {
            ex.report(getActivity());
        } catch (Throwable ex) {
            Log.unexpectedError(getParentFragmentManager(), ex);
        }
    }

    private void onFileSelected(Uri uri) {
        try {
            NoStreamException.check(uri, getContext());

            getContext().getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if (!Helper.isPersisted(getContext(), uri, true, false))
                throw new IllegalStateException("No permission granted to access selected file " + uri);

            Editable edit = etText.getText();
            if (edit.length() > 0 && edit.charAt(edit.length() - 1) != '\n')
                edit.append("\n");
            edit.append(EntityAnswer.ATTACHMENT_PREFIX + uri + EntityAnswer.ATTACHMENT_SUFFIX + "\n");
            etText.setSelection(edit.length());
        } catch (NoStreamException ex) {
            ex.report(getActivity());
        } catch (Throwable ex) {
            Log.unexpectedError(getParentFragmentManager(), ex);
        }
    }

    private void onLinkSelected(Bundle args) {
        String link = args.getString("link");
        boolean image = args.getBoolean("image");
        int start = args.getInt("start");
        int end = args.getInt("end");
        String title = args.getString("title");
        etText.setSelection(start, end);
        StyleHelper.apply(R.id.menu_link, getViewLifecycleOwner(), null, etText, -1L, 0, link, image, title);
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

    private void onLanguageTool() {
        HtmlHelper.clearComposingText(etText);

        Bundle args = new Bundle();
        args.putCharSequence("text", etText.getText());

        new SimpleTask<List<LanguageTool.Suggestion>>() {
            private Toast toast = null;

            @Override
            protected void onPreExecute(Bundle args) {
                toast = ToastEx.makeText(getContext(), R.string.title_suggestions_check, Toast.LENGTH_LONG);
                toast.show();
            }

            @Override
            protected void onPostExecute(Bundle args) {
                if (toast != null)
                    toast.cancel();
            }

            @Override
            protected List<LanguageTool.Suggestion> onExecute(Context context, Bundle args) throws Throwable {
                CharSequence text = args.getCharSequence("text").toString();
                return LanguageTool.getSuggestions(context, text);
            }

            @Override
            protected void onExecuted(Bundle args, List<LanguageTool.Suggestion> suggestions) {
                LanguageTool.applySuggestions(etText, 0, etText.length(), suggestions);

                if (suggestions == null || suggestions.size() == 0)
                    ToastEx.makeText(getContext(), R.string.title_suggestions_none, Toast.LENGTH_LONG).show();
            }

            @Override
            protected void onDestroyed(Bundle args) {
                if (toast != null) {
                    toast.cancel();
                    toast = null;
                }
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Throwable exex = new Throwable("LanguageTool", ex);
                Log.unexpectedError(getParentFragmentManager(), exex, !(ex instanceof IOException));
            }
        }.execute(this, args, "answer:lt");
    }
}
