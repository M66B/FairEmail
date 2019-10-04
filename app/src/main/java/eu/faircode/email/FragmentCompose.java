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

    Copyright 2018-2019 by Marcel Bokhorst (M66B)
*/

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.LocaleList;
import android.os.Looper;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.text.style.QuoteSpan;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageButton;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.FileProvider;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;
import com.google.android.material.snackbar.Snackbar;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;
import org.openintents.openpgp.OpenPgpError;
import org.openintents.openpgp.util.OpenPgpApi;
import org.openintents.openpgp.util.OpenPgpServiceConnection;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.MessageRemovedException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.widget.AdapterView.INVALID_POSITION;

public class FragmentCompose extends FragmentBase {
    private enum State {NONE, LOADING, LOADED}

    private ViewGroup view;
    private Spinner spIdentity;
    private EditText etExtra;
    private TextView tvDomain;
    private MultiAutoCompleteTextView etTo;
    private ImageButton ibToAdd;
    private MultiAutoCompleteTextView etCc;
    private ImageButton ibCcAdd;
    private MultiAutoCompleteTextView etBcc;
    private ImageButton ibBccAdd;
    private EditText etSubject;
    private ImageButton ibCcBcc;
    private RecyclerView rvAttachment;
    private TextView tvNoInternetAttachments;
    private EditTextCompose etBody;
    private TextView tvNoInternet;
    private TextView tvSignature;
    private CheckBox cbSignature;
    private TextView tvReference;
    private ImageButton ibCloseRefHint;
    private ImageButton ibReferenceDelete;
    private ImageButton ibReferenceEdit;
    private ImageButton ibReferenceImages;
    private BottomNavigationView style_bar;
    private BottomNavigationView media_bar;
    private BottomNavigationView bottom_navigation;
    private ContentLoadingProgressBar pbWait;
    private Group grpHeader;
    private Group grpExtra;
    private Group grpAddresses;
    private Group grpAttachments;
    private Group grpBody;
    private Group grpSignature;
    private Group grpReferenceHint;

    private ContentResolver resolver;
    private AdapterAttachment adapter;

    private boolean prefix_once = false;
    private boolean monospaced = false;
    private boolean encrypt = false;
    private boolean media = true;
    private boolean compact = false;

    private long working = -1;
    private State state = State.NONE;
    private boolean show_images = false;
    private boolean autosave = false;
    private boolean busy = false;

    private Uri photoURI = null;

    private OpenPgpServiceConnection pgpService;
    private long[] pgpKeyIds;
    private long pgpSignKeyId;

    static final int REDUCED_IMAGE_SIZE = 1440; // pixels
    static final int REDUCED_IMAGE_QUALITY = 90; // percent

    private static final int ADDRESS_ELLIPSIZE = 50;

    private static final int REQUEST_CONTACT_TO = 1;
    private static final int REQUEST_CONTACT_CC = 2;
    private static final int REQUEST_CONTACT_BCC = 3;
    private static final int REQUEST_IMAGE = 4;
    private static final int REQUEST_ATTACHMENT = 5;
    private static final int REQUEST_TAKE_PHOTO = 6;
    private static final int REQUEST_RECORD_AUDIO = 7;
    private static final int REQUEST_ENCRYPT = 8;
    private static final int REQUEST_COLOR = 9;
    private static final int REQUEST_REF_DELETE = 10;
    private static final int REQUEST_REF_EDIT = 11;
    private static final int REQUEST_CONTACT_GROUP = 12;
    private static final int REQUEST_ANSWER = 13;
    private static final int REQUEST_LINK = 14;
    private static final int REQUEST_DISCARD = 15;
    private static final int REQUEST_SEND = 16;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefix_once = prefs.getBoolean("prefix_once", true);
        monospaced = prefs.getBoolean("monospaced", false);
        media = prefs.getBoolean("compose_media", true);
        compact = prefs.getBoolean("compose_compact", false);

        setTitle(R.string.page_compose);
        setSubtitle(getResources().getQuantityString(R.plurals.page_message, 1));
    }

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = (ViewGroup) inflater.inflate(R.layout.fragment_compose, container, false);

        // Get controls
        spIdentity = view.findViewById(R.id.spIdentity);
        etExtra = view.findViewById(R.id.etExtra);
        tvDomain = view.findViewById(R.id.tvDomain);
        etTo = view.findViewById(R.id.etTo);
        ibToAdd = view.findViewById(R.id.ivToAdd);
        etCc = view.findViewById(R.id.etCc);
        ibCcAdd = view.findViewById(R.id.ivCcAdd);
        etBcc = view.findViewById(R.id.etBcc);
        ibBccAdd = view.findViewById(R.id.ivBccAdd);
        etSubject = view.findViewById(R.id.etSubject);
        ibCcBcc = view.findViewById(R.id.ivCcBcc);
        rvAttachment = view.findViewById(R.id.rvAttachment);
        tvNoInternetAttachments = view.findViewById(R.id.tvNoInternetAttachments);
        etBody = view.findViewById(R.id.etBody);
        tvNoInternet = view.findViewById(R.id.tvNoInternet);
        tvSignature = view.findViewById(R.id.tvSignature);
        cbSignature = view.findViewById(R.id.cbSignature);
        tvReference = view.findViewById(R.id.tvReference);
        ibCloseRefHint = view.findViewById(R.id.ibCloseRefHint);
        ibReferenceDelete = view.findViewById(R.id.ibReferenceDelete);
        ibReferenceEdit = view.findViewById(R.id.ibReferenceEdit);
        ibReferenceImages = view.findViewById(R.id.ibReferenceImages);
        style_bar = view.findViewById(R.id.style_bar);
        media_bar = view.findViewById(R.id.media_bar);
        bottom_navigation = view.findViewById(R.id.bottom_navigation);

        pbWait = view.findViewById(R.id.pbWait);
        grpHeader = view.findViewById(R.id.grpHeader);
        grpExtra = view.findViewById(R.id.grpExtra);
        grpAddresses = view.findViewById(R.id.grpAddresses);
        grpAttachments = view.findViewById(R.id.grpAttachments);
        grpBody = view.findViewById(R.id.grpBody);
        grpSignature = view.findViewById(R.id.grpSignature);
        grpReferenceHint = view.findViewById(R.id.grpReferenceHint);

        resolver = getContext().getContentResolver();

        // Wire controls
        spIdentity.setOnItemSelectedListener(identitySelected);

        etTo.setMaxLines(Integer.MAX_VALUE);
        etTo.setHorizontallyScrolling(false);

        etCc.setMaxLines(Integer.MAX_VALUE);
        etCc.setHorizontallyScrolling(false);

        etBcc.setMaxLines(Integer.MAX_VALUE);
        etBcc.setHorizontallyScrolling(false);

        etSubject.setMaxLines(Integer.MAX_VALUE);
        etSubject.setHorizontallyScrolling(false);

        ibCcBcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMenuAddresses();
            }
        });

        View.OnClickListener onPick = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int request;
                switch (view.getId()) {
                    case R.id.ivToAdd:
                        request = REQUEST_CONTACT_TO;
                        break;
                    case R.id.ivCcAdd:
                        request = REQUEST_CONTACT_CC;
                        break;
                    case R.id.ivBccAdd:
                        request = REQUEST_CONTACT_BCC;
                        break;
                    default:
                        return;
                }

                Intent pick = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Email.CONTENT_URI);
                if (pick.resolveActivity(getContext().getPackageManager()) == null)
                    Snackbar.make(view, R.string.title_no_contacts, Snackbar.LENGTH_LONG).show();
                else
                    startActivityForResult(Helper.getChooser(getContext(), pick), request);
            }
        };

        ibToAdd.setOnClickListener(onPick);
        ibCcAdd.setOnClickListener(onPick);
        ibBccAdd.setOnClickListener(onPick);

        setZoom();

        etBody.setInputContentListener(new EditTextCompose.IInputContentListener() {
            @Override
            public void onInputContent(Uri uri) {
                onAddAttachment(uri, true);
            }
        });

        etBody.setSelectionListener(new EditTextCompose.ISelection() {
            @Override
            public void onSelected(boolean selection) {
                style_bar.setVisibility(selection ? View.VISIBLE : View.GONE);
            }
        });

        cbSignature.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                Bundle args = new Bundle();
                args.putLong("id", working);
                args.putBoolean("signature", checked);

                new SimpleTask<Integer>() {
                    @Override
                    protected Integer onExecute(Context context, Bundle args) {
                        long id = args.getLong("id");
                        boolean signature = args.getBoolean("signature");

                        DB db = DB.getInstance(context);
                        return db.message().setMessageSignature(id, signature);
                    }

                    @Override
                    protected void onExecuted(Bundle args, Integer count) {
                        if (count > 0) {
                            boolean signature = args.getBoolean("signature");
                            tvSignature.setAlpha(signature ? 1.0f : Helper.LOW_LIGHT);
                        }
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Helper.unexpectedError(getFragmentManager(), ex);
                    }
                }.execute(FragmentCompose.this, args, "draft:signature");
            }
        });

        ibCloseRefHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                prefs.edit().putBoolean("compose_reference", false).apply();
                grpReferenceHint.setVisibility(View.GONE);
            }
        });

        ibReferenceDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onReferenceDelete();
            }
        });

        ibReferenceEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onReferenceEdit();
            }
        });

        ibReferenceImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ibReferenceImages.setVisibility(View.GONE);
                onReferenceImages();
            }
        });

        etBody.setTypeface(monospaced ? Typeface.MONOSPACE : Typeface.DEFAULT);
        tvReference.setTypeface(monospaced ? Typeface.MONOSPACE : Typeface.DEFAULT);

        style_bar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                return onActionStyle(item.getItemId());
            }
        });

        PackageManager pm = getContext().getPackageManager();
        Intent take_photo = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Intent record_audio = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
        media_bar.getMenu().findItem(R.id.menu_take_photo).setVisible(take_photo.resolveActivity(pm) != null);
        media_bar.getMenu().findItem(R.id.menu_record_audio).setVisible(record_audio.resolveActivity(pm) != null);

        media_bar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int action = item.getItemId();
                switch (action) {
                    case R.id.menu_record_audio:
                        onActionRecordAudio();
                        return true;
                    case R.id.menu_take_photo:
                        onActionTakePhoto();
                        return true;
                    case R.id.menu_image:
                        onActionImage();
                        return true;
                    case R.id.menu_attachment:
                        onActionAttachment();
                        return true;
                    case R.id.menu_link:
                        onActionLink();
                        return true;
                    default:
                        return false;
                }
            }
        });

        setCompact(compact);

        bottom_navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                final int action = item.getItemId();
                switch (action) {
                    case R.id.action_delete:
                        onActionDiscard();
                        break;
                    case R.id.action_send:
                        onActionCheck(false);
                        break;
                    default:
                        onAction(action);
                }
                return true;
            }
        });

        //view.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);

        addBackPressedListener(onBackPressedListener);

        // Initialize
        setHasOptionsMenu(true);

        etExtra.setHint("");
        tvDomain.setText(null);

        grpHeader.setVisibility(View.GONE);
        grpExtra.setVisibility(View.GONE);
        grpAddresses.setVisibility(View.GONE);
        ibCcBcc.setVisibility(View.GONE);
        grpAttachments.setVisibility(View.GONE);
        tvNoInternet.setVisibility(View.GONE);
        grpBody.setVisibility(View.GONE);
        grpSignature.setVisibility(View.GONE);
        grpReferenceHint.setVisibility(View.GONE);
        ibReferenceDelete.setVisibility(View.GONE);
        ibReferenceEdit.setVisibility(View.GONE);
        ibReferenceImages.setVisibility(View.GONE);
        tvReference.setVisibility(View.GONE);
        style_bar.setVisibility(View.GONE);
        media_bar.setVisibility(View.GONE);
        bottom_navigation.setVisibility(View.GONE);
        pbWait.setVisibility(View.VISIBLE);

        getActivity().invalidateOptionsMenu();
        Helper.setViewsEnabled(view, false);

        final DB db = DB.getInstance(getContext());

        SimpleCursorAdapter cadapter = new SimpleCursorAdapter(
                getContext(),
                R.layout.spinner_item2_dropdown,
                null,
                new String[]{"name", "email"},
                new int[]{android.R.id.text1, android.R.id.text2},
                0);

        cadapter.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
            public CharSequence convertToString(Cursor cursor) {
                int colName = cursor.getColumnIndex("name");
                int colEmail = cursor.getColumnIndex("email");
                String name = cursor.getString(colName);
                String email = cursor.getString(colEmail);
                StringBuilder sb = new StringBuilder();
                if (name == null)
                    sb.append(email);
                else {
                    sb.append("\"").append(name).append("\" ");
                    sb.append("<").append(email).append(">");
                }
                return sb.toString();
            }
        });

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean suggest_sent = prefs.getBoolean("suggest_sent", false);
        boolean suggest_received = prefs.getBoolean("suggest_received", false);

        cadapter.setFilterQueryProvider(new FilterQueryProvider() {
            public Cursor runQuery(CharSequence typed) {
                Log.i("Suggest contact=" + typed);

                String wildcard = "%" + typed + "%";
                List<Cursor> cursors = new ArrayList<>();

                MatrixCursor provided = new MatrixCursor(new String[]{"_id", "name", "email"});
                boolean contacts = Helper.hasPermission(getContext(), Manifest.permission.READ_CONTACTS);
                if (contacts) {
                    Cursor cursor = resolver.query(
                            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                            new String[]{
                                    ContactsContract.CommonDataKinds.Email.CONTACT_ID,
                                    ContactsContract.Contacts.DISPLAY_NAME,
                                    ContactsContract.CommonDataKinds.Email.DATA
                            },
                            ContactsContract.CommonDataKinds.Email.DATA + " <> ''" +
                                    " AND (" + ContactsContract.Contacts.DISPLAY_NAME + " LIKE ?" +
                                    " OR " + ContactsContract.CommonDataKinds.Email.DATA + " LIKE ?)",
                            new String[]{wildcard, wildcard},
                            "CASE WHEN " + ContactsContract.Contacts.DISPLAY_NAME + " NOT LIKE '%@%' THEN 0 ELSE 1 END" +
                                    ", " + ContactsContract.Contacts.DISPLAY_NAME + " COLLATE NOCASE" +
                                    ", " + ContactsContract.CommonDataKinds.Email.DATA + " COLLATE NOCASE");

                    while (cursor != null && cursor.moveToNext())
                        provided.newRow()
                                .add(cursor.getLong(0))
                                .add(cursor.getString(1))
                                .add(cursor.getString(2));
                }
                cursors.add(provided);

                if (suggest_sent)
                    cursors.add(db.contact().searchContacts(null, EntityContact.TYPE_TO, wildcard));

                if (suggest_received)
                    cursors.add(db.contact().searchContacts(null, EntityContact.TYPE_FROM, wildcard));

                if (cursors.size() == 1)
                    return cursors.get(0);
                else
                    return new MergeCursor(cursors.toArray(new Cursor[0]));
            }
        });

        etTo.setAdapter(cadapter);
        etCc.setAdapter(cadapter);
        etBcc.setAdapter(cadapter);

        etTo.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        etCc.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        etBcc.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

        rvAttachment.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rvAttachment.setLayoutManager(llm);
        rvAttachment.setItemAnimator(null);

        adapter = new AdapterAttachment(this, false);
        rvAttachment.setAdapter(adapter);

        tvNoInternetAttachments.setVisibility(View.GONE);

        pgpService = new OpenPgpServiceConnection(getContext(), "org.sufficientlysecure.keychain");
        pgpService.bindToService();

        return view;
    }

    private void onReferenceDelete() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (prefs.getBoolean("delete_ref_confirmed", false)) {
            onReferenceDeleteConfirmed();
            return;
        }

        Bundle args = new Bundle();
        args.putString("question", getString(R.string.title_ask_delete_ref));
        args.putString("notagain", "delete_ref_confirmed");

        FragmentDialogAsk fragment = new FragmentDialogAsk();
        fragment.setArguments(args);
        fragment.setTargetFragment(this, REQUEST_REF_DELETE);
        fragment.show(getFragmentManager(), "compose:refdelete");
    }

    private void onReferenceDeleteConfirmed() {
        Bundle args = new Bundle();
        args.putLong("id", working);

        new SimpleTask<EntityMessage>() {
            @Override
            protected void onPreExecute(Bundle args) {
                ibReferenceDelete.setEnabled(false);
                ibReferenceEdit.setEnabled(false);
            }

            @Override
            protected void onPostExecute(Bundle args) {
                ibReferenceDelete.setEnabled(true);
                ibReferenceEdit.setEnabled(true);
            }

            @Override
            protected EntityMessage onExecute(Context context, Bundle args) throws Throwable {
                long id = args.getLong("id");

                DB db = DB.getInstance(context);
                EntityMessage draft = db.message().getMessage(id);
                if (draft != null) {
                    File refFile = draft.getRefFile(context);
                    refFile.delete();
                }

                return draft;
            }

            @Override
            protected void onExecuted(Bundle args, EntityMessage draft) {
                if (draft != null) {
                    tvReference.setVisibility(View.GONE);
                    grpReferenceHint.setVisibility(View.GONE);
                    ibReferenceDelete.setVisibility(View.GONE);
                    ibReferenceEdit.setVisibility(View.GONE);
                    ibReferenceImages.setVisibility(View.GONE);
                }
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(getFragmentManager(), ex);
            }
        }.execute(this, args, "compose:refdelete");
    }

    private void onReferenceEdit() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (prefs.getBoolean("edit_ref_confirmed", false)) {
            onReferenceEditConfirmed();
            return;
        }

        Bundle args = new Bundle();
        args.putString("question", getString(R.string.title_ask_edit_ref));
        args.putString("notagain", "edit_ref_confirmed");

        FragmentDialogAsk fragment = new FragmentDialogAsk();
        fragment.setArguments(args);
        fragment.setTargetFragment(this, REQUEST_REF_EDIT);
        fragment.show(getFragmentManager(), "compose:refedit");
    }

    private void onReferenceEditConfirmed() {
        Bundle args = new Bundle();
        args.putLong("id", working);
        args.putString("body", HtmlHelper.toHtml(etBody.getText()));

        new SimpleTask<EntityMessage>() {
            @Override
            protected void onPreExecute(Bundle args) {
                ibReferenceDelete.setEnabled(false);
                ibReferenceEdit.setEnabled(false);
            }

            @Override
            protected void onPostExecute(Bundle args) {
                ibReferenceDelete.setEnabled(true);
                ibReferenceEdit.setEnabled(true);
            }

            @Override
            protected EntityMessage onExecute(Context context, Bundle args) throws Throwable {
                long id = args.getLong("id");
                String body = args.getString("body");

                DB db = DB.getInstance(context);
                EntityMessage draft = db.message().getMessage(id);
                if (draft == null || !draft.content)
                    throw new IllegalArgumentException(context.getString(R.string.title_no_body));

                File file = draft.getFile(context);
                File refFile = draft.getRefFile(context);

                String ref = Helper.readText(refFile);
                String plain = HtmlHelper.getText(ref);
                String html = "<p>" + plain.replaceAll("\\r?\\n", "<br>") + "</p>";

                try (BufferedWriter out = new BufferedWriter(new FileWriter(file))) {
                    out.write(body);
                    out.write(html);
                }

                refFile.delete();

                draft.plain_only = true;
                draft.revision = null;
                draft.revisions = null;

                db.message().setMessagePlainOnly(draft.id, true);
                db.message().setMessageRevision(draft.id, null);
                db.message().setMessageRevisions(draft.id, null);

                return draft;
            }

            @Override
            protected void onExecuted(Bundle args, EntityMessage draft) {
                getActivity().invalidateOptionsMenu();
                showDraft(draft);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(getFragmentManager(), ex);
            }
        }.execute(this, args, "compose:refedit");
    }

    private void onReferenceImages() {
        show_images = true;
        showDraft(working);
    }

    @Override
    public void onDestroyView() {
        adapter = null;

        if (pgpService != null)
            pgpService.unbindFromService();

        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putLong("fair:working", working);
        outState.putBoolean("fair:show_images", show_images);
        outState.putParcelable("fair:photo", photoURI);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        state = State.NONE;

        if (savedInstanceState == null) {
            if (working < 0) {
                Bundle a = getArguments();
                if (a == null) {
                    a = new Bundle();
                    a.putString("action", "new");
                    setArguments(a);
                }

                Bundle args = new Bundle();

                args.putString("action", a.getString("action"));
                args.putLong("id", a.getLong("id", -1));
                args.putLong("account", a.getLong("account", -1));
                args.putLong("identity", a.getLong("identity", -1));
                args.putLong("reference", a.getLong("reference", -1));
                args.putSerializable("ics", a.getSerializable("ics"));
                args.putString("status", a.getString("status"));
                args.putBoolean("raw", a.getBoolean("raw", false));
                args.putLong("answer", a.getLong("answer", -1));
                args.putString("to", a.getString("to"));
                args.putString("cc", a.getString("cc"));
                args.putString("bcc", a.getString("bcc"));
                args.putString("subject", a.getString("subject"));
                args.putString("body", a.getString("body"));
                args.putParcelableArrayList("attachments", a.getParcelableArrayList("attachments"));
                draftLoader.execute(this, args, "compose:new");
            } else {
                Bundle args = new Bundle();
                args.putString("action", "edit");
                args.putLong("id", working);
                draftLoader.execute(this, args, "compose:edit");
            }
        } else {
            working = savedInstanceState.getLong("fair:working");
            show_images = savedInstanceState.getBoolean("fair:show_images");
            photoURI = savedInstanceState.getParcelable("fair:photo");

            Bundle args = new Bundle();
            args.putString("action", working < 0 ? "new" : "edit");
            args.putLong("id", working);
            draftLoader.execute(this, args, "compose:instance");
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        cm.registerNetworkCallback(builder.build(), networkCallback);

        if (!pgpService.isBound())
            pgpService.bindToService();
    }

    @Override
    public void onPause() {
        if (autosave && state == State.LOADED)
            onAction(R.id.action_save);

        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        cm.unregisterNetworkCallback(networkCallback);

        super.onPause();
    }

    ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            check();
        }

        @Override
        public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
            check();
        }

        @Override
        public void onLost(Network network) {
            check();
        }

        private void check() {
            Activity activity = getActivity();
            if (activity != null)
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                            checkInternet();
                    }
                });
        }
    };

    private void checkInternet() {
        boolean suitable = ConnectionHelper.getNetworkState(getContext()).isSuitable();

        Boolean content = (Boolean) tvNoInternet.getTag();
        tvNoInternet.setVisibility(!suitable && content != null && !content ? View.VISIBLE : View.GONE);

        Boolean downloading = (Boolean) rvAttachment.getTag();
        tvNoInternetAttachments.setVisibility(!suitable && downloading != null && downloading ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_compose, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        menu.findItem(R.id.menu_encrypt).setVisible(state == State.LOADED);
        menu.findItem(R.id.menu_zoom).setVisible(state == State.LOADED);
        menu.findItem(R.id.menu_media).setVisible(state == State.LOADED);
        menu.findItem(R.id.menu_compact).setVisible(state == State.LOADED);
        menu.findItem(R.id.menu_clear).setVisible(state == State.LOADED);
        menu.findItem(R.id.menu_contact_group).setVisible(state == State.LOADED);
        menu.findItem(R.id.menu_answer).setVisible(state == State.LOADED);
        menu.findItem(R.id.menu_send).setVisible(state == State.LOADED);

        menu.findItem(R.id.menu_encrypt).setEnabled(!busy);
        menu.findItem(R.id.menu_zoom).setEnabled(!busy);
        menu.findItem(R.id.menu_media).setEnabled(!busy);
        menu.findItem(R.id.menu_compact).setEnabled(!busy);
        menu.findItem(R.id.menu_clear).setEnabled(!busy);
        menu.findItem(R.id.menu_contact_group).setEnabled(!busy && hasPermission(Manifest.permission.READ_CONTACTS));
        menu.findItem(R.id.menu_answer).setEnabled(!busy);
        menu.findItem(R.id.menu_send).setEnabled(!busy);

        menu.findItem(R.id.menu_encrypt).setIcon(encrypt ? R.drawable.baseline_lock_open_24 : R.drawable.baseline_lock_24);
        menu.findItem(R.id.menu_media).setChecked(media);
        menu.findItem(R.id.menu_compact).setChecked(compact);

        bottom_navigation.getMenu().findItem(R.id.action_send)
                .setTitle(encrypt ? R.string.title_encrypt : R.string.title_send);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_encrypt:
                onMenuEncrypt();
                return true;
            case R.id.menu_zoom:
                onMenuZoom();
                return true;
            case R.id.menu_media:
                onMenuMediabar();
                return true;
            case R.id.menu_compact:
                onMenuCompact();
                return true;
            case R.id.menu_clear:
                StyleHelper.apply(R.id.menu_clear, etBody);
                return true;
            case R.id.menu_contact_group:
                onMenuContactGroup();
                return true;
            case R.id.menu_answer:
                onMenuAnswer();
                return true;
            case R.id.menu_send:
                onActionCheck(true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onMenuAddresses() {
        grpAddresses.setVisibility(grpAddresses.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (grpAddresses.getVisibility() == View.GONE)
                    etSubject.requestFocus();
                else
                    etCc.requestFocus();
            }
        });
    }

    private void onMenuEncrypt() {
        encrypt = !encrypt;
        getActivity().invalidateOptionsMenu();

        Bundle args = new Bundle();
        args.putLong("id", working);
        args.putBoolean("encrypt", encrypt);

        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) {
                long id = args.getLong("id");
                boolean encrypt = args.getBoolean("encrypt");

                DB db = DB.getInstance(context);
                db.message().setMessageEncrypt(id, encrypt);

                return null;
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(getFragmentManager(), ex);
            }
        }.execute(this, args, "compose:encrypt");
    }

    private void onMenuZoom() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean compact = prefs.getBoolean("compact", false);
        int zoom = prefs.getInt("zoom", compact ? 0 : 1);
        zoom = ++zoom % 3;
        prefs.edit().putInt("zoom", zoom).apply();
        setZoom();
    }

    private void setZoom() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean compact = prefs.getBoolean("compact", false);
        int zoom = prefs.getInt("zoom", compact ? 0 : 1);
        setZoom(zoom);
    }

    private void setZoom(int zoom) {
        float textSize = Helper.getTextSize(getContext(), zoom);
        if (textSize != 0) {
            etBody.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            tvReference.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        }
    }

    private void onMenuMediabar() {
        media = !media;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefs.edit().putBoolean("compose_media", media).apply();
        media_bar.setVisibility(media ? View.VISIBLE : View.GONE);
    }

    private void onMenuCompact() {
        compact = !compact;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefs.edit().putBoolean("compose_compact", compact).apply();
        setCompact(compact);
    }

    private void setCompact(boolean compact) {
        bottom_navigation.setLabelVisibilityMode(compact
                ? LabelVisibilityMode.LABEL_VISIBILITY_UNLABELED
                : LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
        ViewGroup.LayoutParams params = bottom_navigation.getLayoutParams();
        params.height = Helper.dp2pixels(view.getContext(), compact ? 36 : 56);
        bottom_navigation.setLayoutParams(params);
    }

    private void onMenuContactGroup() {
        Bundle args = new Bundle();
        args.putLong("working", working);

        FragmentDialogContactGroup fragment = new FragmentDialogContactGroup();
        fragment.setArguments(args);
        fragment.setTargetFragment(this, REQUEST_CONTACT_GROUP);
        fragment.show(getFragmentManager(), "compose:groups");
    }

    private void onMenuAnswer() {
        if (!ActivityBilling.isPro(getContext())) {
            getContext().startActivity(new Intent(getContext(), ActivityBilling.class));
            return;
        }

        FragmentDialogAnswer fragment = new FragmentDialogAnswer();
        fragment.setArguments(new Bundle());
        fragment.setTargetFragment(this, REQUEST_ANSWER);
        fragment.show(getFragmentManager(), "compose:answer");
    }

    private boolean onActionStyle(int action) {
        Log.i("Style action=" + action);

        if (action == R.id.menu_color) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
            if (imm != null)
                imm.hideSoftInputFromWindow(etBody.getWindowToken(), 0);

            Bundle args = new Bundle();
            args.putInt("color", Color.TRANSPARENT);
            args.putString("title", getString(R.string.title_style_color));

            FragmentDialogColor fragment = new FragmentDialogColor();
            fragment.setArguments(args);
            fragment.setTargetFragment(FragmentCompose.this, REQUEST_COLOR);
            fragment.show(getFragmentManager(), "account:color");
            return true;
        } else
            return StyleHelper.apply(action, etBody);
    }

    private void onActionRecordAudio() {
        Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
        startActivityForResult(intent, REQUEST_RECORD_AUDIO);
    }

    private void onActionTakePhoto() {
        File dir = new File(getContext().getCacheDir(), "photo");
        if (!dir.exists())
            dir.mkdir();
        File file = new File(dir, new Date().getTime() + ".jpg");

        // https://developer.android.com/training/camera/photobasics
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        photoURI = FileProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID, file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        startActivityForResult(intent, REQUEST_TAKE_PHOTO);
    }

    private void onActionImage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        PackageManager pm = getContext().getPackageManager();
        if (intent.resolveActivity(pm) == null)
            Snackbar.make(view, R.string.title_no_saf, Snackbar.LENGTH_LONG).show();
        else
            startActivityForResult(Helper.getChooser(getContext(), intent), REQUEST_IMAGE);
    }

    private void onActionAttachment() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        PackageManager pm = getContext().getPackageManager();
        if (intent.resolveActivity(pm) == null)
            Snackbar.make(view, R.string.title_no_saf, Snackbar.LENGTH_LONG).show();
        else
            startActivityForResult(Helper.getChooser(getContext(), intent), REQUEST_ATTACHMENT);
    }

    private void onActionLink() {
        Uri uri = null;

        ClipboardManager cbm = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (cbm.hasPrimaryClip()) {
            String link = cbm.getPrimaryClip().getItemAt(0).coerceToText(getContext()).toString();
            uri = Uri.parse(link);
            if (uri.getScheme() == null)
                uri = null;
        }

        Bundle args = new Bundle();
        args.putParcelable("uri", uri);

        FragmentDialogLink fragment = new FragmentDialogLink();
        fragment.setArguments(args);
        fragment.setTargetFragment(this, REQUEST_LINK);
        fragment.show(getFragmentManager(), "compose:link");
    }

    private void onActionDiscard() {
        if (isEmpty())
            onAction(R.id.action_delete);
        else {
            Bundle args = new Bundle();
            args.putString("question", getString(R.string.title_ask_discard));

            FragmentDialogAsk fragment = new FragmentDialogAsk();
            fragment.setArguments(args);
            fragment.setTargetFragment(this, REQUEST_DISCARD);
            fragment.show(getFragmentManager(), "compose:discard");
        }
    }

    private void onActionCheck(boolean dialog) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean send_dialog = prefs.getBoolean("send_dialog", true);

        Bundle extras = new Bundle();
        extras.putBoolean("dialog", dialog || send_dialog);
        onAction(R.id.action_check, extras);
    }

    private void onEncrypt() {
        if (pgpService.isBound())
            try {
                String to = etTo.getText().toString();
                InternetAddress ato[] = (TextUtils.isEmpty(to) ? new InternetAddress[0] : InternetAddress.parse(to));
                if (ato.length == 0)
                    throw new IllegalArgumentException(getString(R.string.title_to_missing));

                String[] tos = new String[ato.length];
                for (int i = 0; i < ato.length; i++)
                    tos[i] = ato[i].getAddress().toLowerCase(Locale.ROOT);

                Intent intent = new Intent(OpenPgpApi.ACTION_GET_KEY_IDS);
                intent.putExtra(OpenPgpApi.EXTRA_USER_IDS, tos);
                onPgp(intent);
            } catch (Throwable ex) {
                if (ex instanceof IllegalArgumentException)
                    Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG).show();
                else {
                    Log.e(ex);
                    Helper.unexpectedError(getFragmentManager(), ex);
                }
            }
        else {
            Snackbar snackbar = Snackbar.make(view, R.string.title_no_openpgp, Snackbar.LENGTH_LONG);
            if (Helper.getIntentOpenKeychain().resolveActivity(getContext().getPackageManager()) != null)
                snackbar.setAction(R.string.title_fix, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(Helper.getIntentOpenKeychain());
                    }
                });
            snackbar.show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            switch (requestCode) {
                case REQUEST_CONTACT_TO:
                case REQUEST_CONTACT_CC:
                case REQUEST_CONTACT_BCC:
                    if (resultCode == RESULT_OK && data != null)
                        onPickContact(requestCode, data);
                    break;
                case REQUEST_IMAGE:
                    if (resultCode == RESULT_OK && data != null) {
                        Uri uri = data.getData();
                        if (uri != null)
                            onAddAttachment(uri, true);
                    }
                    break;
                case REQUEST_ATTACHMENT:
                case REQUEST_RECORD_AUDIO:
                case REQUEST_TAKE_PHOTO:
                    if (resultCode == RESULT_OK)
                        if (requestCode == REQUEST_TAKE_PHOTO)
                            onAddMedia(new Intent().setData(photoURI));
                        else if (data != null)
                            onAddMedia(data);
                    break;
                case REQUEST_ENCRYPT:
                    if (resultCode == RESULT_OK && data != null) {
                        if (BuildConfig.DEBUG || BuildConfig.BETA_RELEASE)
                            Log.logExtras(data);
                        onPgp(data);
                    }
                    break;
                case REQUEST_REF_DELETE:
                    if (resultCode == RESULT_OK)
                        onReferenceDeleteConfirmed();
                    break;
                case REQUEST_REF_EDIT:
                    if (resultCode == RESULT_OK)
                        onReferenceEditConfirmed();
                    break;
                case REQUEST_CONTACT_GROUP:
                    if (resultCode == RESULT_OK && data != null)
                        onContactGroupSelected(data.getBundleExtra("args"));
                    break;
                case REQUEST_ANSWER:
                    if (resultCode == RESULT_OK && data != null)
                        onAnswerSelected(data.getBundleExtra("args"));
                    break;
                case REQUEST_COLOR:
                    if (resultCode == RESULT_OK && data != null)
                        onColorSelected(data.getBundleExtra("args"));
                    break;
                case REQUEST_LINK:
                    if (resultCode == RESULT_OK && data != null)
                        onLinkSelected(data.getBundleExtra("args"));
                    break;
                case REQUEST_DISCARD:
                    if (resultCode == RESULT_OK)
                        onActionDiscardConfirmed();
                    break;
                case REQUEST_SEND:
                    if (resultCode == RESULT_OK && data != null)
                        onActionSend(data.getBundleExtra("args"));
                    break;
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    private void onPickContact(int requestCode, Intent data) {
        Uri uri = data.getData();
        if (uri == null)
            return;

        Bundle args = new Bundle();
        args.putLong("id", working);
        args.putInt("requestCode", requestCode);
        args.putParcelable("uri", uri);

        new SimpleTask<EntityMessage>() {
            @Override
            protected EntityMessage onExecute(Context context, Bundle args) throws Throwable {
                long id = args.getLong("id");
                int requestCode = args.getInt("requestCode");
                Uri uri = args.getParcelable("uri");

                EntityMessage draft = null;
                DB db = DB.getInstance(context);

                try (Cursor cursor = context.getContentResolver().query(
                        uri,
                        new String[]{
                                ContactsContract.CommonDataKinds.Email.ADDRESS,
                                ContactsContract.Contacts.DISPLAY_NAME
                        },
                        null, null, null)) {
                    if (cursor != null && cursor.moveToFirst()) {
                        int colEmail = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS);
                        int colName = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                        String email = cursor.getString(colEmail);
                        String name = cursor.getString(colName);

                        try {
                            db.beginTransaction();

                            draft = db.message().getMessage(id);
                            if (draft == null)
                                return null;

                            Address[] address = null;
                            if (requestCode == REQUEST_CONTACT_TO)
                                address = draft.to;
                            else if (requestCode == REQUEST_CONTACT_CC)
                                address = draft.cc;
                            else if (requestCode == REQUEST_CONTACT_BCC)
                                address = draft.bcc;

                            List<Address> list = new ArrayList<>();
                            if (address != null)
                                list.addAll(Arrays.asList(address));

                            list.add(new InternetAddress(email, name));

                            if (requestCode == REQUEST_CONTACT_TO)
                                draft.to = list.toArray(new Address[0]);
                            else if (requestCode == REQUEST_CONTACT_CC)
                                draft.cc = list.toArray(new Address[0]);
                            else if (requestCode == REQUEST_CONTACT_BCC)
                                draft.bcc = list.toArray(new Address[0]);

                            db.message().updateMessage(draft);

                            db.setTransactionSuccessful();
                        } finally {
                            db.endTransaction();
                        }
                    }
                }

                return draft;
            }

            @Override
            protected void onExecuted(Bundle args, EntityMessage draft) {
                if (draft != null) {
                    etTo.setText(MessageHelper.formatAddressesCompose(draft.to));
                    etCc.setText(MessageHelper.formatAddressesCompose(draft.cc));
                    etBcc.setText(MessageHelper.formatAddressesCompose(draft.bcc));
                }
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(getFragmentManager(), ex);
            }
        }.execute(this, args, "compose:picked");
    }

    private void onAddAttachment(Uri uri, final boolean image) {
        Bundle args = new Bundle();
        args.putLong("id", working);
        args.putParcelable("uri", uri);

        new SimpleTask<EntityAttachment>() {
            @Override
            protected EntityAttachment onExecute(Context context, Bundle args) throws IOException {
                Long id = args.getLong("id");
                Uri uri = args.getParcelable("uri");
                return addAttachment(context, id, uri, image);
            }

            @Override
            protected void onExecuted(Bundle args, final EntityAttachment attachment) {
                if (image) {
                    File file = attachment.getFile(getContext());
                    Drawable d = Drawable.createFromPath(file.getAbsolutePath());
                    d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());

                    int start = etBody.getSelectionStart();
                    etBody.getText().insert(start, " ");
                    SpannableString s = new SpannableString(etBody.getText());
                    ImageSpan is = new ImageSpan(getContext(), Uri.parse("cid:" + BuildConfig.APPLICATION_ID + "." + attachment.id), ImageSpan.ALIGN_BASELINE);
                    s.setSpan(is, start, start + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    String html = HtmlHelper.toHtml(s);
                    etBody.setText(HtmlHelper.fromHtml(html, cidGetter, null));
                }

                // Save text & update remote draft
                onAction(R.id.action_save);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                // External app sending absolute file
                if (ex instanceof SecurityException)
                    handleFileShare();
                else
                    Helper.unexpectedError(getFragmentManager(), ex);
            }
        }.execute(this, args, "compose:attachment:add");
    }

    private void onAddMedia(Intent data) {
        Log.i("Add media data=" + data);
        Log.logExtras(data);

        ClipData clipData = data.getClipData();
        if (clipData == null) {
            Uri uri = data.getData();
            if (uri != null)
                onAddAttachment(uri, false);
        } else {
            for (int i = 0; i < clipData.getItemCount(); i++) {
                ClipData.Item item = clipData.getItemAt(i);
                Uri uri = item.getUri();
                if (uri != null)
                    onAddAttachment(uri, false);
            }
        }
    }

    private void onPgp(Intent data) {
        final Bundle args = new Bundle();
        args.putLong("id", working);
        args.putParcelable("data", data);

        new SimpleTask<Object>() {
            @Override
            protected Object onExecute(Context context, Bundle args) throws Throwable {
                // Get arguments
                long id = args.getLong("id");
                Intent data = args.getParcelable("data");

                DB db = DB.getInstance(context);

                // Get data
                EntityMessage message = db.message().getMessage(id);
                if (message == null)
                    throw new MessageRemovedException();
                EntityIdentity identity = db.identity().getIdentity(message.identity);
                if (identity == null)
                    throw new IllegalArgumentException(getString(R.string.title_from_missing));

                List<EntityAttachment> attachments = db.attachment().getAttachments(id);
                for (EntityAttachment attachment : new ArrayList<>(attachments))
                    if (attachment.encryption != null) {
                        if (OpenPgpApi.ACTION_GET_KEY_IDS.equals(data.getAction()))
                            db.attachment().deleteAttachment(attachment.id);
                        attachments.remove(attachment);
                    }

                // Build message
                Properties props = MessageHelper.getSessionProperties();
                Session isession = Session.getInstance(props, null);
                MimeMessage imessage = new MimeMessage(isession);
                MessageHelper.build(context, message, attachments, identity, imessage);

                // Serialize message
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                imessage.writeTo(os);
                ByteArrayInputStream decrypted = new ByteArrayInputStream(os.toByteArray());
                ByteArrayOutputStream encrypted = null;
                if (OpenPgpApi.ACTION_GET_KEY.equals(data.getAction()) ||
                        OpenPgpApi.ACTION_SIGN_AND_ENCRYPT.equals(data.getAction()))
                    encrypted = new ByteArrayOutputStream();

                if (BuildConfig.DEBUG || BuildConfig.BETA_RELEASE) {
                    Log.i("Execute " + data);
                    Log.logExtras(data);
                }

                // Encrypt message
                OpenPgpApi api = new OpenPgpApi(context, pgpService.getService());
                Intent result = api.executeApi(data, decrypted, encrypted);

                if (BuildConfig.DEBUG || BuildConfig.BETA_RELEASE) {
                    Log.i("Result " + result);
                    Log.logExtras(result);
                }

                int resultCode = result.getIntExtra(OpenPgpApi.RESULT_CODE, OpenPgpApi.RESULT_CODE_ERROR);
                switch (resultCode) {
                    case OpenPgpApi.RESULT_CODE_SUCCESS:
                        // Attach encrypted data / signature
                        if (OpenPgpApi.ACTION_GET_KEY.equals(data.getAction()) ||
                                OpenPgpApi.ACTION_SIGN_AND_ENCRYPT.equals(data.getAction()) ||
                                OpenPgpApi.ACTION_DETACHED_SIGN.equals(data.getAction()))
                            try {
                                db.beginTransaction();

                                String name;
                                int encryption;
                                if (OpenPgpApi.ACTION_GET_KEY.equals(data.getAction())) {
                                    name = "keydata.asc";
                                    encryption = EntityAttachment.PGP_KEY;
                                } else if (OpenPgpApi.ACTION_SIGN_AND_ENCRYPT.equals(data.getAction())) {
                                    name = "encrypted.asc";
                                    encryption = EntityAttachment.PGP_MESSAGE;
                                } else if (OpenPgpApi.ACTION_DETACHED_SIGN.equals(data.getAction())) {
                                    name = "signature.asc";
                                    encryption = EntityAttachment.PGP_SIGNATURE;
                                } else
                                    throw new IllegalStateException(data.getAction());

                                EntityAttachment attachment = new EntityAttachment();
                                attachment.message = id;
                                attachment.sequence = db.attachment().getAttachmentSequence(id) + 1;
                                attachment.name = name;
                                attachment.type = "application/octet-stream";
                                attachment.disposition = Part.INLINE;
                                attachment.encryption = encryption;
                                attachment.id = db.attachment().insertAttachment(attachment);

                                byte[] bytes;
                                if (OpenPgpApi.ACTION_DETACHED_SIGN.equals(data.getAction()))
                                    bytes = result.getByteArrayExtra(OpenPgpApi.RESULT_DETACHED_SIGNATURE);
                                else
                                    bytes = encrypted.toByteArray();

                                File file = attachment.getFile(context);
                                if (BuildConfig.DEBUG || BuildConfig.BETA_RELEASE)
                                    Log.i("Writing " + file + " size=" + bytes.length);
                                try (OutputStream out = new FileOutputStream(file)) {
                                    out.write(bytes);
                                }
                                db.attachment().setDownloaded(attachment.id, (long) bytes.length);

                                db.setTransactionSuccessful();
                            } finally {
                                db.endTransaction();
                            }

                        if (OpenPgpApi.ACTION_GET_KEY_IDS.equals(data.getAction())) {
                            pgpKeyIds = result.getLongArrayExtra(OpenPgpApi.EXTRA_KEY_IDS);
                            if (BuildConfig.DEBUG || BuildConfig.BETA_RELEASE)
                                Log.i("Keys=" + pgpKeyIds.length);

                            // Send without encryption
                            if (pgpKeyIds.length == 0)
                                return null;

                            // Get encrypt key
                            Intent intent = new Intent(OpenPgpApi.ACTION_GET_KEY);
                            intent.putExtra(OpenPgpApi.EXTRA_KEY_ID, pgpKeyIds[0]);
                            intent.putExtra(OpenPgpApi.EXTRA_REQUEST_ASCII_ARMOR, true);
                            return intent;
                        } else if (OpenPgpApi.ACTION_GET_KEY.equals(data.getAction())) {
                            if (identity != null && identity.sign_key != null) {
                                // Encrypt message
                                Intent intent = new Intent(OpenPgpApi.ACTION_SIGN_AND_ENCRYPT);
                                intent.putExtra(OpenPgpApi.EXTRA_KEY_IDS, pgpKeyIds);
                                intent.putExtra(OpenPgpApi.EXTRA_SIGN_KEY_ID, identity.sign_key);
                                intent.putExtra(OpenPgpApi.EXTRA_REQUEST_ASCII_ARMOR, true);
                                return intent;
                            } else {
                                // Get sign key
                                return new Intent(OpenPgpApi.ACTION_GET_SIGN_KEY_ID);
                            }
                        } else if (OpenPgpApi.ACTION_GET_SIGN_KEY_ID.equals(data.getAction())) {
                            pgpSignKeyId = result.getLongExtra(OpenPgpApi.EXTRA_SIGN_KEY_ID, -1);
                            if (identity != null)
                                db.identity().setIdentitySignKey(identity.id, pgpSignKeyId);

                            // Encrypt message
                            Intent intent = new Intent(OpenPgpApi.ACTION_SIGN_AND_ENCRYPT);
                            intent.putExtra(OpenPgpApi.EXTRA_KEY_IDS, pgpKeyIds);
                            intent.putExtra(OpenPgpApi.EXTRA_SIGN_KEY_ID, pgpSignKeyId);
                            intent.putExtra(OpenPgpApi.EXTRA_REQUEST_ASCII_ARMOR, true);
                            return intent;
                        } else if (OpenPgpApi.ACTION_SIGN_AND_ENCRYPT.equals(data.getAction())) {
                            // Get signature
                            Intent intent = new Intent(OpenPgpApi.ACTION_DETACHED_SIGN);
                            intent.putExtra(OpenPgpApi.EXTRA_SIGN_KEY_ID, pgpSignKeyId);
                            return null;
                        } else {
                            // send message
                            return null;
                        }

                    case OpenPgpApi.RESULT_CODE_USER_INTERACTION_REQUIRED:
                        return result.getParcelableExtra(OpenPgpApi.RESULT_INTENT);

                    case OpenPgpApi.RESULT_CODE_ERROR:
                        if (identity != null)
                            db.identity().setIdentitySignKey(identity.id, null);
                        OpenPgpError error = result.getParcelableExtra(OpenPgpApi.RESULT_ERROR);
                        if (error == null)
                            throw new IllegalArgumentException("Unknown error");
                        else
                            throw new IllegalArgumentException(error.getMessage() + " (" + error.getErrorId() + ")");

                    default:
                        throw new IllegalArgumentException("Unknown result code=" + resultCode);
                }
            }

            @Override
            protected void onExecuted(Bundle args, Object result) {
                if (result == null)
                    onAction(R.id.action_send);
                else if (result instanceof Intent) {
                    Intent intent = (Intent) result;
                    onPgp(intent);
                } else if (result instanceof PendingIntent)
                    try {
                        PendingIntent pi = (PendingIntent) result;
                        startIntentSenderForResult(
                                pi.getIntentSender(),
                                REQUEST_ENCRYPT,
                                null, 0, 0, 0, null);
                    } catch (IntentSender.SendIntentException ex) {
                        Log.e(ex);
                        Helper.unexpectedError(getFragmentManager(), ex);
                    }
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (ex instanceof IllegalArgumentException)
                    Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG).show();
                else
                    Helper.unexpectedError(getFragmentManager(), ex);
            }
        }.execute(this, args, "compose:encrypt");
    }

    private void onContactGroupSelected(Bundle args) {
        if (args.getInt("target") > 0)
            grpAddresses.setVisibility(View.VISIBLE);

        new SimpleTask<EntityMessage>() {
            @Override
            protected EntityMessage onExecute(Context context, Bundle args) throws Throwable {
                long id = args.getLong("id");
                int target = args.getInt("target");
                long group = args.getLong("group");

                List<Address> selected = new ArrayList<>();

                try (Cursor cursor = context.getContentResolver().query(
                        ContactsContract.Data.CONTENT_URI,
                        new String[]{ContactsContract.Data.CONTACT_ID},
                        ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + "= ?" + " AND "
                                + ContactsContract.CommonDataKinds.GroupMembership.MIMETYPE + "='"
                                + ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE + "'",
                        new String[]{String.valueOf(group)}, null)) {
                    while (cursor != null && cursor.moveToNext()) {
                        try (Cursor contact = getContext().getContentResolver().query(
                                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                                new String[]{
                                        ContactsContract.Contacts.DISPLAY_NAME,
                                        ContactsContract.CommonDataKinds.Email.DATA
                                },
                                ContactsContract.Data.CONTACT_ID + " = ?",
                                new String[]{cursor.getString(0)},
                                null)) {
                            if (contact != null && contact.moveToNext()) {
                                String name = contact.getString(0);
                                String email = contact.getString(1);
                                selected.add(new InternetAddress(email, name));
                            }
                        }
                    }
                }

                EntityMessage draft;
                DB db = DB.getInstance(context);

                try {
                    db.beginTransaction();

                    draft = db.message().getMessage(id);
                    if (draft == null)
                        return null;

                    Address[] address = null;
                    if (target == 0)
                        address = draft.to;
                    else if (target == 1)
                        address = draft.cc;
                    else if (target == 2)
                        address = draft.bcc;

                    List<Address> list = new ArrayList<>();
                    if (address != null)
                        list.addAll(Arrays.asList(address));

                    list.addAll(selected);

                    if (target == 0)
                        draft.to = list.toArray(new Address[0]);
                    else if (target == 1)
                        draft.cc = list.toArray(new Address[0]);
                    else if (target == 2)
                        draft.bcc = list.toArray(new Address[0]);

                    db.message().updateMessage(draft);

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                return draft;
            }

            @Override
            protected void onExecuted(Bundle args, EntityMessage draft) {
                if (draft != null) {
                    etTo.setText(MessageHelper.formatAddressesCompose(draft.to));
                    etCc.setText(MessageHelper.formatAddressesCompose(draft.cc));
                    etBcc.setText(MessageHelper.formatAddressesCompose(draft.bcc));
                }
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(getFragmentManager(), ex);
            }
        }.execute(this, args, "compose:picked");
    }

    private void onAnswerSelected(Bundle args) {
        String answer = args.getString("answer");

        InternetAddress[] to = null;
        try {
            to = InternetAddress.parse(etTo.getText().toString());
        } catch (AddressException ignored) {
        }

        String text = EntityAnswer.replacePlaceholders(answer, to);

        Spanned spanned = HtmlHelper.fromHtml(text);
        etBody.getText().insert(etBody.getSelectionStart(), spanned);
    }

    private void onColorSelected(Bundle args) {
        int color = args.getInt("color");
        StyleHelper.apply(R.id.menu_color, etBody, color);
    }

    private void onLinkSelected(Bundle args) {
        String link = args.getString("link");
        StyleHelper.apply(R.id.menu_link, etBody, link);
    }

    private void onActionDiscardConfirmed() {
        onAction(R.id.action_delete);
    }

    private void onActionSend(Bundle data) {
        if (data.getBoolean("encrypt"))
            onEncrypt();
        else
            onAction(R.id.action_send);
    }

    private void onExit() {
        if (state != State.LOADED)
            finish();
        else if (isEmpty())
            onAction(R.id.action_delete);
        else {
            autosave = false;
            onAction(R.id.action_save);
            finish();
        }
    }

    private boolean isEmpty() {
        if (!TextUtils.isEmpty(JsoupEx.parse(HtmlHelper.toHtml(etBody.getText())).text().trim()))
            return false;
        if (rvAttachment.getAdapter().getItemCount() > 0)
            return false;
        return true;
    }

    private void onAction(int action) {
        onAction(action, null);
    }

    private void onAction(int action, Bundle extras) {
        EntityIdentity identity = (EntityIdentity) spIdentity.getSelectedItem();
        if (identity == null)
            throw new IllegalArgumentException(getString(R.string.title_from_missing));

        // Workaround underlines left by Android
        etBody.clearComposingText();

        Bundle args = new Bundle();
        args.putLong("id", working);
        args.putInt("action", action);
        args.putLong("account", identity.account);
        args.putLong("identity", identity.id);
        args.putString("extra", etExtra.getText().toString().trim());
        args.putString("to", etTo.getText().toString().trim());
        args.putString("cc", etCc.getText().toString().trim());
        args.putString("bcc", etBcc.getText().toString().trim());
        args.putString("subject", etSubject.getText().toString().trim());
        args.putString("body", HtmlHelper.toHtml(etBody.getText()));
        args.putBoolean("empty", isEmpty());
        if (extras != null)
            args.putBundle("extras", extras);

        Log.i("Run execute id=" + working);
        actionLoader.execute(this, args, "compose:action:" + action);
    }

    private static EntityAttachment addAttachment(Context context, long id, Uri uri,
                                                  boolean image) throws IOException {
        Log.w("Add attachment uri=" + uri);

        if ("file".equals(uri.getScheme()) &&
                !Helper.hasPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Log.w("Add attachment uri=" + uri);
            throw new SecurityException("Add attachment with file scheme");
        }

        EntityAttachment attachment = new EntityAttachment();

        String name = uri.getLastPathSegment();
        String s = null;

        try {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int colName = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    int colSize = cursor.getColumnIndex(OpenableColumns.SIZE);
                    if (colName >= 0)
                        name = cursor.getString(colName);
                    if (colSize >= 0)
                        s = cursor.getString(colSize);
                }
            }
        } catch (SecurityException ex) {
            Log.w(ex);
        }

        DB db = DB.getInstance(context);
        try {
            db.beginTransaction();

            EntityMessage draft = db.message().getMessage(id);
            Log.i("Attaching to id=" + id);

            attachment.message = draft.id;
            attachment.sequence = db.attachment().getAttachmentSequence(draft.id) + 1;
            attachment.name = name;

            String extension = Helper.getExtension(attachment.name);
            if (extension != null)
                attachment.type = MimeTypeMap.getSingleton()
                        .getMimeTypeFromExtension(extension.toLowerCase(Locale.ROOT));
            if (attachment.type == null)
                attachment.type = "application/octet-stream";
            attachment.disposition = (image ? Part.INLINE : Part.ATTACHMENT);

            attachment.size = (s == null ? null : Long.parseLong(s));
            attachment.progress = 0;

            attachment.id = db.attachment().insertAttachment(attachment);
            Log.i("Created attachment=" + attachment.name + ":" + attachment.sequence + " type=" + attachment.type);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        long size = 0;
        int lastProgress = 0;
        try {
            File file = attachment.getFile(context);

            InputStream is = null;
            OutputStream os = null;
            try {
                is = context.getContentResolver().openInputStream(uri);
                os = new FileOutputStream(file);

                byte[] buffer = new byte[Helper.BUFFER_SIZE];
                for (int len = is.read(buffer); len != -1; len = is.read(buffer)) {
                    size += len;
                    os.write(buffer, 0, len);

                    // Update progress
                    if (attachment.size != null && attachment.size > 0) {
                        int progress = (int) (size * 100 / attachment.size / 20 * 20);
                        if (progress != lastProgress) {
                            lastProgress = progress;
                            db.attachment().setProgress(attachment.id, progress);
                        }
                    }
                }

                if (image) {
                    attachment.cid = "<" + BuildConfig.APPLICATION_ID + "." + attachment.id + ">";
                    db.attachment().setCid(attachment.id, attachment.cid);
                }
            } finally {
                try {
                    if (is != null)
                        is.close();
                } finally {
                    if (os != null)
                        os.close();
                }
            }

            db.attachment().setDownloaded(attachment.id, size);

            if ("eu.faircode.email".equals(uri.getAuthority())) {
                // content://eu.faircode.email/photo/nnn.jpg
                File tmp = new File(context.getCacheDir(), uri.getPath());
                Log.i("Deleting " + tmp);
                if (!tmp.delete())
                    Log.w("Error deleting " + tmp);
            } else
                Log.i("Authority=" + uri.getAuthority());

            resizeAttachment(context, attachment);

        } catch (Throwable ex) {
            // Reset progress on failure
            Log.e(ex);
            db.attachment().setError(attachment.id, Helper.formatThrowable(ex, false));
            throw ex;
        }

        return attachment;
    }

    private static void resizeAttachment(Context context, EntityAttachment attachment) throws IOException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean autoresize = prefs.getBoolean("autoresize", true);

        File file = attachment.getFile(context);

        if (autoresize && file.exists() /* upload cancelled */ &&
                ("image/jpeg".equals(attachment.type) || "image/png".equals(attachment.type))) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(file.getAbsolutePath(), options);

            int resize = prefs.getInt("resize", REDUCED_IMAGE_SIZE);

            int factor = 1;
            while (options.outWidth / factor > resize ||
                    options.outHeight / factor > resize)
                factor *= 2;

            Matrix rotation = ("image/jpeg".equals(attachment.type) ? Helper.getImageRotation(file) : null);
            Log.i("Image type=" + attachment.type + " rotation=" + rotation);

            if (factor > 1 || rotation != null) {
                options.inJustDecodeBounds = false;
                options.inSampleSize = factor;

                Bitmap resized = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
                if (resized != null) {
                    Log.i("Image target size=" + resized.getWidth() + "x" + resized.getHeight() + " rotation=" + rotation);

                    if (rotation != null) {
                        Bitmap rotated = Bitmap.createBitmap(resized, 0, 0, resized.getWidth(), resized.getHeight(), rotation, true);
                        resized.recycle();
                        resized = rotated;
                    }

                    File tmp = File.createTempFile("image", ".resized", context.getCacheDir());
                    try (OutputStream out = new BufferedOutputStream(new FileOutputStream(tmp))) {
                        resized.compress("image/jpeg".equals(attachment.type)
                                        ? Bitmap.CompressFormat.JPEG
                                        : Bitmap.CompressFormat.PNG,
                                REDUCED_IMAGE_QUALITY, out);
                    } finally {
                        resized.recycle();
                    }

                    file.delete();
                    tmp.renameTo(file);

                    DB db = DB.getInstance(context);
                    db.attachment().setDownloaded(attachment.id, file.length());
                }
            }
        }
    }

    private SimpleTask<DraftData> draftLoader = new SimpleTask<DraftData>() {
        @Override
        protected DraftData onExecute(Context context, Bundle args) throws Throwable {
            String action = args.getString("action");
            long id = args.getLong("id", -1);
            long reference = args.getLong("reference", -1);
            File ics = (File) args.getSerializable("ics");
            String status = args.getString("status");
            long answer = args.getLong("answer", -1);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            boolean plain_only = prefs.getBoolean("plain_only", false);
            boolean encrypt_default = prefs.getBoolean("encrypt_default", false);
            boolean receipt_default = prefs.getBoolean("receipt_default", false);

            Log.i("Load draft action=" + action + " id=" + id + " reference=" + reference);

            Map<String, String> crumb = new HashMap<>();
            crumb.put("draft", Long.toString(id));
            crumb.put("reference", Long.toString(reference));
            crumb.put("action", action);
            Log.breadcrumb("compose", crumb);

            DraftData data = new DraftData();

            DB db = DB.getInstance(context);
            try {
                db.beginTransaction();

                data.identities = db.identity().getComposableIdentities(null);
                if (data.identities == null || data.identities.size() == 0)
                    throw new IllegalStateException(getString(R.string.title_no_identities));

                data.draft = db.message().getMessage(id);
                if (data.draft == null || data.draft.ui_hide) {
                    // New draft
                    if ("edit".equals(action))
                        throw new MessageRemovedException("Draft for edit was deleted hide=" + (data.draft != null));

                    EntityMessage ref = db.message().getMessage(reference);

                    String body = "";

                    data.draft = new EntityMessage();
                    data.draft.msgid = EntityMessage.generateMessageId();

                    if (plain_only)
                        data.draft.plain_only = true;
                    if (encrypt_default)
                        data.draft.encrypt = true;
                    if (receipt_default)
                        data.draft.receipt_request = true;

                    if (ref == null) {
                        data.draft.thread = data.draft.msgid;

                        try {
                            String to = args.getString("to");
                            data.draft.to = (TextUtils.isEmpty(to) ? null : InternetAddress.parse(to));
                        } catch (AddressException ex) {
                            Log.w(ex);
                        }

                        try {
                            String cc = args.getString("cc");
                            data.draft.cc = (TextUtils.isEmpty(cc) ? null : InternetAddress.parse(cc));
                        } catch (AddressException ex) {
                            Log.w(ex);
                        }

                        try {
                            String bcc = args.getString("bcc");
                            data.draft.bcc = (TextUtils.isEmpty(bcc) ? null : InternetAddress.parse(bcc));
                        } catch (AddressException ex) {
                            Log.w(ex);
                        }

                        data.draft.subject = args.getString("subject", "");
                        body = args.getString("body", "");
                        if (!TextUtils.isEmpty(body))
                            body = HtmlHelper.sanitize(context, body, false);

                        if (answer > 0) {
                            EntityAnswer a = db.answer().getAnswer(answer);
                            if (a != null) {
                                data.draft.subject = a.name;
                                body = a.getText(null) + body;
                            }
                        }
                    } else {
                        // Actions:
                        // - reply
                        // - reply_all
                        // - forward
                        // - editasnew
                        // - list
                        // - receipt
                        // - participation
                        if ("reply".equals(action) || "reply_all".equals(action) ||
                                "list".equals(action) ||
                                "receipt".equals(action) ||
                                "participation".equals(action)) {
                            data.draft.references = (ref.references == null ? "" : ref.references + " ") + ref.msgid;
                            data.draft.inreplyto = ref.msgid;
                            data.draft.thread = ref.thread;

                            if ("list".equals(action) && ref.list_post != null)
                                data.draft.to = ref.list_post;
                            else if ("receipt".equals(action) && ref.receipt_to != null)
                                data.draft.to = ref.receipt_to;
                            else {
                                // Prevent replying to self
                                if (ref.replySelf(data.identities, ref.account)) {
                                    data.draft.from = ref.from;
                                    data.draft.to = ref.to;
                                } else {
                                    data.draft.from = ref.to;
                                    data.draft.to = (ref.reply == null || ref.reply.length == 0 ? ref.from : ref.reply);
                                }
                            }

                            if ("reply_all".equals(action))
                                data.draft.cc = ref.getAllRecipients(data.identities, ref.account);
                            else if ("receipt".equals(action)) {
                                data.draft.receipt = true;
                                data.draft.receipt_request = false;
                            }

                        } else if ("forward".equals(action) || "editasnew".equals(action))
                            data.draft.thread = data.draft.msgid; // new thread

                        String subject = (ref.subject == null ? "" : ref.subject);
                        if ("reply".equals(action) || "reply_all".equals(action)) {
                            if (prefix_once) {
                                String re = context.getString(R.string.title_subject_reply, "");
                                subject = unprefix(subject, re);
                            }
                            data.draft.subject = context.getString(R.string.title_subject_reply, subject);
                        } else if ("forward".equals(action)) {
                            if (prefix_once) {
                                String fwd = context.getString(R.string.title_subject_forward, "");
                                subject = unprefix(subject, fwd);
                            }
                            data.draft.subject = context.getString(R.string.title_subject_forward, subject);
                        } else if ("editasnew".equals(action)) {
                            data.draft.subject = ref.subject;
                            if (ref.content) {
                                String html = Helper.readText(ref.getFile(context));
                                body = HtmlHelper.sanitize(context, html, true);
                            }
                        } else if ("list".equals(action)) {
                            data.draft.subject = ref.subject;
                        } else if ("receipt".equals(action)) {
                            data.draft.subject = context.getString(R.string.title_receipt_subject, subject);

                            Configuration configuration = new Configuration(context.getResources().getConfiguration());
                            configuration.setLocale(new Locale("en"));
                            Resources res = context.createConfigurationContext(configuration).getResources();

                            body = "<p>" + context.getString(R.string.title_receipt_text) + "</p>";
                            if (!Locale.getDefault().getLanguage().equals("en"))
                                body += "<p>" + res.getString(R.string.title_receipt_text) + "</p>";
                        } else if ("participation".equals(action))
                            data.draft.subject = status + ": " + ref.subject;

                        if (ref.plain_only != null && ref.plain_only)
                            data.draft.plain_only = ref.plain_only;

                        if (answer > 0) {
                            EntityAnswer a = db.answer().getAnswer(answer);
                            if (a != null)
                                body = a.getText(data.draft.to) + body;
                        }
                    }

                    // Select identity matching from address
                    Address from = null;
                    EntityIdentity selected = null;
                    long aid = args.getLong("account", -1);
                    long iid = args.getLong("identity", -1);

                    if (iid >= 0)
                        for (EntityIdentity identity : data.identities)
                            if (identity.id.equals(iid)) {
                                selected = identity;
                                break;
                            }

                    if (data.draft.from != null && data.draft.from.length > 0) {
                        if (selected == null)
                            for (Address sender : data.draft.from)
                                for (EntityIdentity identity : data.identities)
                                    if (identity.account.equals(aid) &&
                                            identity.sameAddress(sender)) {
                                        from = sender;
                                        selected = identity;
                                        break;
                                    }

                        if (selected == null)
                            for (Address sender : data.draft.from)
                                for (EntityIdentity identity : data.identities)
                                    if (identity.account.equals(aid) &&
                                            identity.similarAddress(sender)) {
                                        from = sender;
                                        selected = identity;
                                        break;
                                    }

                        if (selected == null)
                            for (Address sender : data.draft.from)
                                for (EntityIdentity identity : data.identities)
                                    if (identity.sameAddress(sender)) {
                                        from = sender;
                                        selected = identity;
                                        break;
                                    }

                        if (selected == null)
                            for (Address sender : data.draft.from)
                                for (EntityIdentity identity : data.identities)
                                    if (identity.similarAddress(sender)) {
                                        from = sender;
                                        selected = identity;
                                        break;
                                    }
                    }

                    if (selected == null)
                        for (EntityIdentity identity : data.identities)
                            if (identity.account.equals(aid) && identity.primary) {
                                selected = identity;
                                break;
                            }

                    if (selected == null)
                        for (EntityIdentity identity : data.identities)
                            if (identity.account.equals(aid)) {
                                selected = identity;
                                break;
                            }

                    if (selected == null)
                        for (EntityIdentity identity : data.identities)
                            if (identity.primary) {
                                selected = identity;
                                break;
                            }

                    if (selected == null)
                        for (EntityIdentity identity : data.identities) {
                            selected = identity;
                            break;
                        }

                    if (selected == null)
                        throw new IllegalArgumentException(context.getString(R.string.title_no_identities));

                    EntityFolder drafts = db.folder().getFolderByType(selected.account, EntityFolder.DRAFTS);
                    if (drafts == null)
                        throw new IllegalArgumentException(context.getString(R.string.title_no_primary_drafts));

                    data.draft.account = drafts.account;
                    data.draft.folder = drafts.id;
                    data.draft.identity = selected.id;
                    data.draft.from = new InternetAddress[]{new InternetAddress(selected.email, selected.name)};

                    String extra = (from == null ? selected.email : ((InternetAddress) from).getAddress());
                    if (extra != null && extra.contains("@"))
                        data.draft.extra = extra.substring(0, extra.indexOf("@"));
                    else
                        data.draft.extra = null;

                    data.draft.sender = MessageHelper.getSortKey(data.draft.from);
                    Uri lookupUri = ContactInfo.getLookupUri(context, data.draft.from);
                    data.draft.avatar = (lookupUri == null ? null : lookupUri.toString());

                    data.draft.received = new Date().getTime();
                    data.draft.seen = true;
                    data.draft.ui_seen = true;

                    data.draft.id = db.message().insertMessage(data.draft);
                    Helper.writeText(data.draft.getFile(context), body);

                    db.message().setMessageContent(data.draft.id,
                            true,
                            data.draft.plain_only,
                            HtmlHelper.getPreview(body),
                            null);

                    if ("participation".equals(action)) {
                        EntityAttachment attachment = new EntityAttachment();
                        attachment.message = data.draft.id;
                        attachment.sequence = 1;
                        attachment.name = "meeting.ics";
                        attachment.type = "text/calendar";
                        attachment.disposition = Part.ATTACHMENT;
                        attachment.size = ics.length();
                        attachment.progress = null;
                        attachment.available = true;
                        attachment.id = db.attachment().insertAttachment(attachment);
                        ics.renameTo(attachment.getFile(context));
                    }

                    // Write reference text
                    if (ref != null && ref.content &&
                            !"editasnew".equals(action) &&
                            !"list".equals(action) &&
                            !"receipt".equals(action)) {
                        String refText = Helper.readText(ref.getFile(context));

                        boolean usenet = prefs.getBoolean("usenet_signature", false);
                        if (usenet) {
                            Document rdoc = JsoupEx.parse(refText);

                            List<Node> tbd = new ArrayList<>();

                            NodeTraversor.traverse(new NodeVisitor() {
                                boolean found = false;

                                public void head(Node node, int depth) {
                                    if (node instanceof TextNode &&
                                            "-- ".equals(((TextNode) node).getWholeText()) &&
                                            node.nextSibling() != null &&
                                            "br".equals(node.nextSibling().nodeName()))
                                        found = true;
                                    if (found)
                                        tbd.add(node);
                                }

                                public void tail(Node node, int depth) {
                                    // Do nothing
                                }
                            }, rdoc);

                            if (tbd.size() > 0) {
                                for (Node node : tbd)
                                    node.remove();

                                refText = (rdoc.body() == null ? "" : rdoc.body().html());
                            }
                        }

                        if ("reply".equals(action) || "reply_all".equals(action))
                            refText = "<blockquote>" + refText + "</blockquote>";

                        String refBody = String.format("<p>%s %s:</p>\n%s",
                                Html.escapeHtml(new Date(ref.received).toString()),
                                Html.escapeHtml(MessageHelper.formatAddresses(ref.from)),
                                refText);
                        Helper.writeText(data.draft.getRefFile(context), refBody);
                    }

                    if ("new".equals(action)) {
                        ArrayList<Uri> uris = args.getParcelableArrayList("attachments");
                        if (uris != null)
                            for (Uri uri : uris)
                                addAttachment(context, data.draft.id, uri, false);
                    } else if (ref != null &&
                            ("reply".equals(action) || "reply_all".equals(action) ||
                                    "forward".equals(action) || "editasnew".equals(action))) {
                        int sequence = 0;
                        List<EntityAttachment> attachments = db.attachment().getAttachments(ref.id);
                        for (EntityAttachment attachment : attachments)
                            if (attachment.encryption != null &&
                                    attachment.encryption.equals(EntityAttachment.PGP_MESSAGE)) {
                                data.draft.encrypt = true;
                                db.message().setMessageEncrypt(data.draft.id, true);

                            } else if (attachment.encryption == null &&
                                    ("forward".equals(action) || "editasnew".equals(action) ||
                                            (attachment.isInline() && attachment.isImage()))) {
                                if (attachment.available) {
                                    File source = attachment.getFile(context);

                                    attachment.id = null;
                                    attachment.message = data.draft.id;
                                    attachment.sequence = ++sequence;
                                    attachment.id = db.attachment().insertAttachment(attachment);

                                    File target = attachment.getFile(context);
                                    Helper.copy(source, target);

                                    if (!"forward".equals(action))
                                        resizeAttachment(context, attachment);
                                } else
                                    args.putBoolean("incomplete", true);
                            }
                    }

                    EntityOperation.queue(context, data.draft, EntityOperation.ADD);
                } else {
                    if (data.draft.content) {
                        File file = data.draft.getFile(context);
                        String html = Helper.readText(file);
                        html = HtmlHelper.sanitize(context, html, true);
                        Helper.writeText(file, html);
                    } else {
                        if (data.draft.uid == null)
                            throw new IllegalStateException("Draft without uid");
                        EntityOperation.queue(context, data.draft, EntityOperation.BODY);
                    }

                    List<EntityAttachment> attachments = db.attachment().getAttachments(data.draft.id);
                    for (EntityAttachment attachment : attachments)
                        if (!attachment.available)
                            EntityOperation.queue(context, data.draft, EntityOperation.ATTACHMENT, attachment.id);
                }

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }

            return data;
        }

        @Override
        protected void onExecuted(Bundle args, final DraftData data) {
            final String action = getArguments().getString("action");
            Log.i("Loaded draft id=" + data.draft.id + " action=" + action);

            working = data.draft.id;
            encrypt = (data.draft.encrypt != null && data.draft.encrypt);
            getActivity().invalidateOptionsMenu();

            // Show identities
            AdapterIdentitySelect iadapter = new AdapterIdentitySelect(getContext(), data.identities);
            spIdentity.setAdapter(iadapter);

            // Select identity
            if (data.draft.identity != null)
                for (int pos = 0; pos < data.identities.size(); pos++) {
                    if (data.identities.get(pos).id.equals(data.draft.identity)) {
                        spIdentity.setSelection(pos);
                        break;
                    }
                }

            etExtra.setText(data.draft.extra);
            etTo.setText(MessageHelper.formatAddressesCompose(data.draft.to));
            etCc.setText(MessageHelper.formatAddressesCompose(data.draft.cc));
            etBcc.setText(MessageHelper.formatAddressesCompose(data.draft.bcc));
            etSubject.setText(data.draft.subject);

            long reference = args.getLong("reference", -1);
            etTo.setTag(reference < 0 ? "" : etTo.getText().toString());
            etSubject.setTag(reference < 0 ? "" : etSubject.getText().toString());

            grpHeader.setVisibility(View.VISIBLE);
            grpAddresses.setVisibility("reply_all".equals(action) ? View.VISIBLE : View.GONE);
            ibCcBcc.setVisibility(View.VISIBLE);

            bottom_navigation.getMenu().findItem(R.id.action_undo).setVisible(
                    data.draft.revision != null && data.draft.revision > 1);
            bottom_navigation.getMenu().findItem(R.id.action_redo).setVisible(
                    data.draft.revision != null && !data.draft.revision.equals(data.draft.revisions));

            if (args.getBoolean("incomplete"))
                Snackbar.make(view, R.string.title_attachments_incomplete, Snackbar.LENGTH_LONG).show();

            DB db = DB.getInstance(getContext());

            db.attachment().liveAttachments(data.draft.id).observe(getViewLifecycleOwner(),
                    new Observer<List<EntityAttachment>>() {
                        private int last_available = 0;

                        @Override
                        public void onChanged(@Nullable List<EntityAttachment> attachments) {
                            if (attachments == null)
                                attachments = new ArrayList<>();

                            adapter.set(attachments);
                            grpAttachments.setVisibility(attachments.size() > 0 ? View.VISIBLE : View.GONE);

                            int available = 0;
                            boolean downloading = false;
                            for (EntityAttachment attachment : attachments) {
                                if (attachment.available)
                                    available++;
                                if (attachment.progress != null)
                                    downloading = true;
                            }

                            Log.i("Attachments=" + attachments.size() +
                                    " available=" + available + " downloading=" + downloading);

                            // Attachment deleted: update remote draft
                            if (available < last_available)
                                onAction(R.id.action_save);

                            last_available = available;

                            rvAttachment.setTag(downloading);
                            checkInternet();
                        }
                    });

            db.message().liveMessage(data.draft.id).observe(getViewLifecycleOwner(), new Observer<EntityMessage>() {
                @Override
                public void onChanged(EntityMessage draft) {
                    // Draft was deleted
                    if (draft == null || draft.ui_hide)
                        finish();
                    else {
                        encrypt = (draft.encrypt != null && draft.encrypt);
                        getActivity().invalidateOptionsMenu();

                        Log.i("Draft content=" + draft.content);
                        if (draft.content && state == State.NONE)
                            showDraft(draft);

                        tvNoInternet.setTag(draft.content);
                        checkInternet();
                    }
                }
            });
        }

        @Override
        protected void onException(Bundle args, Throwable ex) {
            pbWait.setVisibility(View.GONE);

            // External app sending absolute file
            if (ex instanceof MessageRemovedException)
                finish();
            else if (ex instanceof SecurityException)
                handleFileShare();
            else if (ex instanceof IllegalArgumentException)
                Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG).show();
            else if (ex instanceof IllegalStateException) {
                Snackbar snackbar = Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction(R.string.title_fix, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(getContext(), ActivitySetup.class));
                        getActivity().finish();
                    }
                });
                snackbar.show();
            } else
                Helper.unexpectedError(getFragmentManager(), ex);
        }
    };

    private void handleFileShare() {
        Snackbar sb = Snackbar.make(view, R.string.title_no_stream, Snackbar.LENGTH_INDEFINITE);
        sb.setAction(R.string.title_info, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(getContext(), 49);
            }
        });
        sb.show();
    }

    private SimpleTask<EntityMessage> actionLoader = new SimpleTask<EntityMessage>() {
        int last_available = 0;

        @Override
        protected void onPreExecute(Bundle args) {
            setBusy(true);
        }

        @Override
        protected void onPostExecute(Bundle args) {
            int action = args.getInt("action");
            if (action != R.id.action_check)
                setBusy(false);
        }

        @Override
        protected EntityMessage onExecute(final Context context, Bundle args) throws Throwable {
            // Get data
            long id = args.getLong("id");
            int action = args.getInt("action");
            long aid = args.getLong("account");
            long iid = args.getLong("identity");
            String extra = args.getString("extra");
            String to = args.getString("to");
            String cc = args.getString("cc");
            String bcc = args.getString("bcc");
            String subject = args.getString("subject");
            String body = args.getString("body");
            boolean empty = args.getBoolean("empty");

            EntityMessage draft;

            DB db = DB.getInstance(context);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            try {
                db.beginTransaction();

                // Get draft & selected identity
                draft = db.message().getMessage(id);
                EntityIdentity identity = db.identity().getIdentity(iid);

                // Draft deleted by server
                if (draft == null || draft.ui_hide)
                    throw new MessageRemovedException("Draft for action was deleted hide=" + (draft != null));

                Log.i("Load action id=" + draft.id + " action=" + getActionName(action));

                if (action == R.id.action_delete) {
                    boolean discard_delete = prefs.getBoolean("discard_delete", false);
                    EntityFolder trash = db.folder().getFolderByType(draft.account, EntityFolder.TRASH);
                    if (empty || trash == null || discard_delete)
                        EntityOperation.queue(context, draft, EntityOperation.DELETE);
                    else
                        EntityOperation.queue(context, draft, EntityOperation.MOVE, trash.id);

                    if (!empty) {
                        Handler handler = new Handler(context.getMainLooper());
                        handler.post(new Runnable() {
                            public void run() {
                                ToastEx.makeText(context, R.string.title_draft_deleted, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } else {
                    // Move draft to new account
                    if (draft.account != aid && aid >= 0) {
                        Log.i("Account changed");

                        Long uid = draft.uid;
                        String msgid = draft.msgid;
                        boolean content = draft.content;
                        Boolean ui_hide = draft.ui_hide;

                        // To prevent violating constraints
                        draft.uid = null;
                        draft.msgid = null;
                        db.message().updateMessage(draft);

                        // Create copy to delete
                        draft.id = null;
                        draft.uid = uid;
                        draft.msgid = msgid;
                        draft.content = false;
                        draft.ui_hide = true;
                        draft.id = db.message().insertMessage(draft);
                        EntityOperation.queue(context, draft, EntityOperation.DELETE);

                        // Restore original with new account, no uid and new msgid
                        draft.id = id;
                        draft.account = aid;
                        draft.folder = db.folder().getFolderByType(aid, EntityFolder.DRAFTS).id;
                        draft.uid = null;
                        draft.msgid = EntityMessage.generateMessageId();
                        draft.content = content;
                        draft.ui_hide = ui_hide;
                        db.message().updateMessage(draft);

                        if (draft.content)
                            EntityOperation.queue(context, draft, EntityOperation.ADD);
                    }

                    Map<String, String> crumb = new HashMap<>();
                    crumb.put("draft", draft.folder + ":" + draft.id);
                    crumb.put("content", Boolean.toString(draft.content));
                    crumb.put("file", Boolean.toString(draft.getFile(context).exists()));
                    crumb.put("action", getActionName(action));
                    Log.breadcrumb("compose", crumb);

                    List<EntityAttachment> attachments = db.attachment().getAttachments(draft.id);

                    // Get data
                    InternetAddress afrom[] = (identity == null ? null : new InternetAddress[]{new InternetAddress(identity.email, identity.name)});

                    InternetAddress ato[] = null;
                    InternetAddress acc[] = null;
                    InternetAddress abcc[] = null;

                    boolean lookup_mx = prefs.getBoolean("lookup_mx", false);

                    if (!TextUtils.isEmpty(to))
                        try {
                            ato = InternetAddress.parse(to);
                            if (action == R.id.action_send) {
                                for (InternetAddress address : ato)
                                    address.validate();
                                if (lookup_mx)
                                    ConnectionHelper.lookupMx(ato, context);
                            }
                        } catch (AddressException ex) {
                            throw new AddressException(context.getString(R.string.title_address_parse_error,
                                    Helper.ellipsize(to, ADDRESS_ELLIPSIZE), ex.getMessage()));
                        }

                    if (!TextUtils.isEmpty(cc))
                        try {
                            acc = InternetAddress.parse(cc);
                            if (action == R.id.action_send) {
                                for (InternetAddress address : acc)
                                    address.validate();
                                if (lookup_mx)
                                    ConnectionHelper.lookupMx(acc, context);
                            }
                        } catch (AddressException ex) {
                            throw new AddressException(context.getString(R.string.title_address_parse_error,
                                    Helper.ellipsize(cc, ADDRESS_ELLIPSIZE), ex.getMessage()));
                        }

                    if (!TextUtils.isEmpty(bcc))
                        try {
                            abcc = InternetAddress.parse(bcc);
                            if (action == R.id.action_send) {
                                for (InternetAddress address : abcc)
                                    address.validate();
                                if (lookup_mx)
                                    ConnectionHelper.lookupMx(abcc, context);
                            }
                        } catch (AddressException ex) {
                            throw new AddressException(context.getString(R.string.title_address_parse_error,
                                    Helper.ellipsize(bcc, ADDRESS_ELLIPSIZE), ex.getMessage()));
                        }

                    if (TextUtils.isEmpty(extra))
                        extra = null;

                    int available = 0;
                    for (EntityAttachment attachment : attachments)
                        if (attachment.available)
                            available++;

                    Long ident = (identity == null ? null : identity.id);
                    boolean dirty = (!Objects.equals(draft.identity, ident) ||
                            !Objects.equals(draft.extra, extra) ||
                            !MessageHelper.equal(draft.from, afrom) ||
                            !MessageHelper.equal(draft.to, ato) ||
                            !MessageHelper.equal(draft.cc, acc) ||
                            !MessageHelper.equal(draft.bcc, abcc) ||
                            !Objects.equals(draft.subject, subject) ||
                            last_available != available);

                    last_available = available;

                    if (dirty) {
                        // Update draft
                        draft.identity = ident;
                        draft.extra = extra;
                        draft.from = afrom;
                        draft.to = ato;
                        draft.cc = acc;
                        draft.bcc = abcc;
                        draft.subject = subject;
                        draft.sender = MessageHelper.getSortKey(draft.from);
                        Uri lookupUri = ContactInfo.getLookupUri(context, draft.from);
                        draft.avatar = (lookupUri == null ? null : lookupUri.toString());
                        db.message().updateMessage(draft);
                    }

                    if (action == R.id.action_undo || action == R.id.action_redo) {
                        if (draft.revision != null && draft.revisions != null) {
                            dirty = true;

                            if (action == R.id.action_undo) {
                                if (draft.revision > 1)
                                    draft.revision--;
                            } else {
                                if (draft.revision < draft.revisions)
                                    draft.revision++;
                            }

                            body = Helper.readText(draft.getFile(context, draft.revision));
                            Helper.writeText(draft.getFile(context), body);

                            db.message().setMessageRevision(draft.id, draft.revision);

                            db.message().setMessageContent(draft.id,
                                    true,
                                    draft.plain_only, // unchanged
                                    HtmlHelper.getPreview(body),
                                    null);
                        }
                    } else {
                        File file = draft.getFile(context);
                        if (!file.exists())
                            Helper.writeText(file, body);
                        String previous = Helper.readText(file);
                        if (!body.equals(previous)) {
                            dirty = true;

                            if (draft.revisions == null)
                                draft.revisions = 1;
                            else
                                draft.revisions++;
                            draft.revision = draft.revisions;

                            Helper.writeText(draft.getFile(context), body);
                            Helper.writeText(draft.getFile(context, draft.revisions), body);

                            db.message().setMessageRevision(draft.id, draft.revision);
                            db.message().setMessageRevisions(draft.id, draft.revisions);

                            db.message().setMessageContent(draft.id,
                                    true,
                                    draft.plain_only,
                                    HtmlHelper.getPreview(body),
                                    null);
                        }
                    }

                    if (dirty) {
                        draft.received = new Date().getTime();
                        db.message().setMessageReceived(draft.id, draft.received);
                    }

                    // Remove unused inline images
                    StringBuilder sb = new StringBuilder();
                    sb.append(body);
                    File rfile = draft.getRefFile(context);
                    if (rfile.exists())
                        sb.append(Helper.readText(rfile));
                    List<String> cids = new ArrayList<>();
                    for (Element element : JsoupEx.parse(sb.toString()).select("img")) {
                        String src = element.attr("src");
                        if (src.startsWith("cid:"))
                            cids.add("<" + src.substring(4) + ">");
                    }

                    for (EntityAttachment attachment : new ArrayList<>(attachments))
                        if (attachment.isInline() && !cids.contains(attachment.cid)) {
                            Log.i("Removing unused inline attachment cid=" + attachment.cid);
                            db.attachment().deleteAttachment(attachment.id);
                        }

                    // Execute action
                    if (action == R.id.action_save ||
                            action == R.id.action_undo ||
                            action == R.id.action_redo ||
                            action == R.id.action_check) {
                        if (BuildConfig.DEBUG || dirty)
                            EntityOperation.queue(context, draft, EntityOperation.ADD);

                        if (action == R.id.action_check) {
                            // Check data
                            if (draft.identity == null)
                                throw new IllegalArgumentException(context.getString(R.string.title_from_missing));

                            if (draft.to == null && draft.cc == null && draft.bcc == null)
                                throw new IllegalArgumentException(context.getString(R.string.title_to_missing));

                            if (TextUtils.isEmpty(draft.subject))
                                args.putBoolean("remind_subject", true);

                            if (empty)
                                args.putBoolean("remind_text", true);

                            int attached = 0;
                            for (EntityAttachment attachment : attachments)
                                if (!attachment.available)
                                    throw new IllegalArgumentException(context.getString(R.string.title_attachments_missing));
                                else if (!attachment.isInline() && attachment.encryption == null)
                                    attached++;

                            // Check for missing attachments
                            if (attached == 0) {
                                List<String> keywords = new ArrayList<>();

                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                                    String[] k = context.getString(R.string.title_attachment_keywords).split(",");
                                    keywords.addAll(Arrays.asList(k));
                                } else {
                                    Configuration config = context.getResources().getConfiguration();
                                    LocaleList ll = context.getResources().getConfiguration().getLocales();
                                    for (int i = 0; i < ll.size(); i++) {
                                        Configuration lconf = new Configuration(config);
                                        lconf.setLocale(ll.get(i));
                                        Context lcontext = context.createConfigurationContext(lconf);
                                        String[] k = lcontext.getString(R.string.title_attachment_keywords).split(",");
                                        keywords.addAll(Arrays.asList(k));
                                    }
                                }

                                String plain = HtmlHelper.getText(body);
                                for (String keyword : keywords)
                                    if (plain.matches("(?si).*\\b" + Pattern.quote(keyword.trim()) + "\\b.*")) {
                                        args.putBoolean("remind_attachment", true);
                                        break;
                                    }
                            }
                        } else {
                            Handler handler = new Handler(context.getMainLooper());
                            handler.post(new Runnable() {
                                public void run() {
                                    ToastEx.makeText(context, R.string.title_draft_saved, Toast.LENGTH_LONG).show();
                                }
                            });
                        }

                    } else if (action == R.id.action_send) {
                        // Delete draft (cannot move to outbox)
                        EntityOperation.queue(context, draft, EntityOperation.DELETE);

                        File refDraftFile = draft.getRefFile(context);

                        // Copy message to outbox
                        draft.id = null;
                        draft.folder = db.folder().getOutbox().id;
                        draft.uid = null;
                        draft.ui_hide = false;
                        draft.id = db.message().insertMessage(draft);
                        Helper.writeText(draft.getFile(context), body);
                        if (refDraftFile.exists()) {
                            File refFile = draft.getRefFile(context);
                            refDraftFile.renameTo(refFile);
                        }

                        // Move attachments
                        for (EntityAttachment attachment : attachments)
                            db.attachment().setMessage(attachment.id, draft.id);

                        // Delay sending message
                        int send_delayed = prefs.getInt("send_delayed", 0);
                        if (draft.ui_snoozed == null && send_delayed != 0) {
                            draft.ui_snoozed = new Date().getTime() + send_delayed * 1000L;
                            db.message().setMessageSnoozed(draft.id, draft.ui_snoozed);
                        }

                        // Send message
                        if (draft.ui_snoozed == null)
                            EntityOperation.queue(context, draft, EntityOperation.SEND);

                        final String feedback;
                        if (draft.ui_snoozed == null)
                            feedback = context.getString(R.string.title_queued);
                        else {
                            DateFormat DTF = Helper.getDateTimeInstance(context);
                            feedback = context.getString(R.string.title_queued_at, DTF.format(draft.ui_snoozed));
                        }

                        Handler handler = new Handler(context.getMainLooper());
                        handler.post(new Runnable() {
                            public void run() {
                                ToastEx.makeText(context, feedback, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }

            if (action == R.id.action_send && draft.ui_snoozed != null) {
                Log.i("Delayed send id=" + draft.id + " at " + new Date(draft.ui_snoozed));
                EntityMessage.snooze(context, draft.id, draft.ui_snoozed);
            }

            return draft;
        }

        @Override
        protected void onExecuted(Bundle args, EntityMessage draft) {
            int action = args.getInt("action");
            Log.i("Loaded action id=" + (draft == null ? null : draft.id) + " action=" + getActionName(action));

            etTo.setText(MessageHelper.formatAddressesCompose(draft.to));
            etCc.setText(MessageHelper.formatAddressesCompose(draft.cc));
            etBcc.setText(MessageHelper.formatAddressesCompose(draft.bcc));

            bottom_navigation.getMenu().findItem(R.id.action_undo).setVisible(draft.revision != null && draft.revision > 1);
            bottom_navigation.getMenu().findItem(R.id.action_redo).setVisible(draft.revision != null && !draft.revision.equals(draft.revisions));

            if (action == R.id.action_delete) {
                autosave = false;
                finish();

            } else if (action == R.id.action_undo || action == R.id.action_redo) {
                showDraft(draft);

            } else if (action == R.id.action_save) {
                // Do nothing

            } else if (action == R.id.action_check) {
                boolean dialog = args.getBundle("extras").getBoolean("dialog");
                boolean remind_subject = args.getBoolean("remind_subject", false);
                boolean remind_text = args.getBoolean("remind_text", false);
                boolean remind_attachment = args.getBoolean("remind_attachment", false);

                if (dialog || remind_subject || remind_text || remind_attachment) {
                    setBusy(false);

                    FragmentDialogSend fragment = new FragmentDialogSend();
                    fragment.setArguments(args);
                    fragment.setTargetFragment(FragmentCompose.this, REQUEST_SEND);
                    fragment.show(getFragmentManager(), "compose:send");
                } else {
                    if (encrypt)
                        onEncrypt();
                    else
                        onAction(R.id.action_send);
                }

            } else if (action == R.id.action_send) {
                autosave = false;
                finish();
            }
        }

        @Override
        protected void onException(Bundle args, Throwable ex) {
            int action = args.getInt("action");
            if (action == R.id.action_check)
                setBusy(false);

            if (ex instanceof MessageRemovedException)
                finish();
            else if (ex instanceof IllegalArgumentException ||
                    ex instanceof AddressException || ex instanceof UnknownHostException)
                Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG).show();
            else
                Helper.unexpectedError(getFragmentManager(), ex);
        }

        private String getActionName(int id) {
            switch (id) {
                case R.id.action_delete:
                    return "delete";
                case R.id.action_undo:
                    return "undo";
                case R.id.action_redo:
                    return "redo";
                case R.id.action_save:
                    return "save";
                case R.id.action_check:
                    return "check";
                case R.id.action_send:
                    return "send";
                default:
                    return Integer.toString(id);
            }
        }


        private void setBusy(boolean busy) {
            FragmentCompose.this.busy = busy;
            Helper.setViewsEnabled(view, !busy);
            getActivity().invalidateOptionsMenu();
        }
    };

    private String unprefix(String subject, String prefix) {
        subject = subject.trim();
        prefix = prefix.trim().toLowerCase(Locale.ROOT);
        while (subject.toLowerCase(Locale.ROOT).startsWith(prefix))
            subject = subject.substring(prefix.length()).trim();
        return subject;
    }

    private void showDraft(long id) {
        Bundle args = new Bundle();
        args.putLong("id", id);

        new SimpleTask<EntityMessage>() {
            @Override
            protected EntityMessage onExecute(Context context, Bundle args) {
                long id = args.getLong("id");
                return DB.getInstance(context).message().getMessage(id);
            }

            @Override
            protected void onExecuted(Bundle args, EntityMessage draft) {
                showDraft(draft);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(getFragmentManager(), ex);
            }
        }.execute(this, args, "compose:show");
    }

    private void showDraft(final EntityMessage draft) {
        Bundle args = new Bundle();
        args.putLong("id", draft.id);
        args.putBoolean("show_images", show_images);

        new SimpleTask<Spanned[]>() {
            @Override
            protected void onPostExecute(Bundle args) {
                state = State.LOADED;
                autosave = true;

                pbWait.setVisibility(View.GONE);
                media_bar.setVisibility(media ? View.VISIBLE : View.GONE);
                bottom_navigation.getMenu().findItem(R.id.action_undo).setVisible(draft.revision != null && draft.revision > 1);
                bottom_navigation.getMenu().findItem(R.id.action_redo).setVisible(draft.revision != null && !draft.revision.equals(draft.revisions));
                bottom_navigation.setVisibility(View.VISIBLE);

                Helper.setViewsEnabled(view, true);

                getActivity().invalidateOptionsMenu();
            }

            @Override
            protected Spanned[] onExecute(final Context context, Bundle args) throws Throwable {
                final long id = args.getLong("id");
                final boolean show_images = args.getBoolean("show_images", false);

                int colorPrimary = Helper.resolveColor(context, R.attr.colorPrimary);

                DB db = DB.getInstance(context);
                EntityMessage draft = db.message().getMessage(id);
                if (draft == null || !draft.content)
                    throw new IllegalArgumentException(context.getString(R.string.title_no_body));

                String body = Helper.readText(draft.getFile(context));
                Spanned spannedBody = HtmlHelper.fromHtml(body, cidGetter, null);

                SpannableStringBuilder bodyBuilder = new SpannableStringBuilder(spannedBody);
                QuoteSpan[] bodySpans = bodyBuilder.getSpans(0, bodyBuilder.length(), QuoteSpan.class);
                for (QuoteSpan quoteSpan : bodySpans) {
                    bodyBuilder.setSpan(
                            new StyledQuoteSpan(context, colorPrimary),
                            bodyBuilder.getSpanStart(quoteSpan),
                            bodyBuilder.getSpanEnd(quoteSpan),
                            bodyBuilder.getSpanFlags(quoteSpan));
                    bodyBuilder.removeSpan(quoteSpan);
                }

                spannedBody = bodyBuilder;

                Spanned spannedRef = null;
                File refFile = draft.getRefFile(context);
                if (refFile.exists()) {
                    String quote = HtmlHelper.sanitize(context, Helper.readText(refFile), show_images);
                    Spanned spannedQuote = HtmlHelper.fromHtml(quote,
                            new Html.ImageGetter() {
                                @Override
                                public Drawable getDrawable(String source) {
                                    return ImageHelper.decodeImage(context, id, source, show_images, tvReference);
                                }
                            },
                            null);

                    SpannableStringBuilder refBuilder = new SpannableStringBuilder(spannedQuote);
                    QuoteSpan[] refSpans = refBuilder.getSpans(0, refBuilder.length(), QuoteSpan.class);
                    for (QuoteSpan quoteSpan : refSpans) {
                        refBuilder.setSpan(
                                new StyledQuoteSpan(context, colorPrimary),
                                refBuilder.getSpanStart(quoteSpan),
                                refBuilder.getSpanEnd(quoteSpan),
                                refBuilder.getSpanFlags(quoteSpan));
                        refBuilder.removeSpan(quoteSpan);
                    }

                    spannedRef = refBuilder;
                }

                args.putBoolean("ref_has_images", spannedRef != null &&
                        spannedRef.getSpans(0, spannedRef.length(), ImageSpan.class).length > 0);

                return new Spanned[]{spannedBody, spannedRef};
            }

            @Override
            protected void onExecuted(Bundle args, Spanned[] text) {
                etBody.setText(text[0]);
                etBody.setSelection(0);
                grpBody.setVisibility(View.VISIBLE);

                cbSignature.setChecked(draft.signature);
                tvSignature.setAlpha(draft.signature ? 1.0f : Helper.LOW_LIGHT);

                boolean ref_has_images = args.getBoolean("ref_has_images");

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                boolean ref_hint = prefs.getBoolean("compose_reference", true);

                tvReference.setText(text[1]);
                tvReference.setVisibility(text[1] == null ? View.GONE : View.VISIBLE);
                grpReferenceHint.setVisibility(text[1] == null || !ref_hint ? View.GONE : View.VISIBLE);
                ibReferenceDelete.setVisibility(text[1] == null ? View.GONE : View.VISIBLE);
                ibReferenceEdit.setVisibility(text[1] == null ? View.GONE : View.VISIBLE);
                ibReferenceImages.setVisibility(ref_has_images && !show_images ? View.VISIBLE : View.GONE);

                final Context context = getContext();

                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        View target;
                        if (TextUtils.isEmpty(etTo.getText().toString().trim()))
                            target = etTo;
                        else if (TextUtils.isEmpty(etSubject.getText().toString()))
                            target = etSubject;
                        else
                            target = etBody;

                        target.requestFocus();

                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                        boolean keyboard = prefs.getBoolean("keyboard", true);
                        if (keyboard) {
                            InputMethodManager imm =
                                    (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                            if (imm != null)
                                imm.showSoftInput(target, InputMethodManager.SHOW_IMPLICIT);
                        }
                    }
                });
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(getFragmentManager(), ex);
            }
        }.execute(this, args, "compose:show");
    }

    private Html.ImageGetter cidGetter = new Html.ImageGetter() {
        @Override
        public Drawable getDrawable(final String source) {
            Log.i("Loading source=" + source);
            final LevelListDrawable lld = new LevelListDrawable();

            Resources res = getContext().getResources();
            int px = Helper.dp2pixels(getContext(), 48);

            // Level 0: broken image
            Drawable broken = res.getDrawable(R.drawable.baseline_broken_image_24, getContext().getTheme());
            broken.setBounds(0, 0, px, px);
            lld.addLevel(0, 0, broken);

            // Level 1: place holder
            Drawable placeholder = res.getDrawable(R.drawable.baseline_image_24, getContext().getTheme());
            placeholder.setBounds(0, 0, px, px);
            lld.addLevel(1, 1, placeholder);

            lld.setBounds(0, 0, px, px);

            if (source != null && source.startsWith("cid:")) {
                lld.setLevel(1); // placeholder

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Bundle args = new Bundle();
                        args.putLong("id", working);
                        args.putString("cid", "<" + source.substring(4) + ">");
                        args.putInt("scaleToPixels", res.getDisplayMetrics().widthPixels);

                        new SimpleTask<Bitmap>() {
                            @Override
                            protected Bitmap onExecute(Context context, Bundle args) {
                                long id = args.getLong("id");
                                String cid = args.getString("cid");
                                int scaleToPixels = args.getInt("scaleToPixels");

                                DB db = DB.getInstance(context);
                                EntityAttachment attachment = db.attachment().getAttachment(id, cid);
                                if (attachment == null)
                                    return null;

                                File file = attachment.getFile(context);
                                return Helper.decodeImage(file, scaleToPixels);
                            }

                            @Override
                            protected void onExecuted(Bundle args, Bitmap bm) {
                                if (bm == null)
                                    lld.setLevel(0); // broken
                                else {
                                    Drawable image = new BitmapDrawable(res, bm);

                                    DisplayMetrics dm = res.getDisplayMetrics();
                                    image.setBounds(0, 0,
                                            Math.round(bm.getWidth() * dm.density),
                                            Math.round(bm.getHeight() * dm.density));

                                    lld.addLevel(2, 2, image);
                                    lld.setLevel(2); // image

                                    float scale = 1.0f;
                                    float width = etBody.getWidth();
                                    if (image.getIntrinsicWidth() > width)
                                        scale = width / image.getIntrinsicWidth();

                                    lld.setBounds(0, 0,
                                            Math.round(image.getIntrinsicWidth() * scale),
                                            Math.round(image.getIntrinsicHeight() * scale));
                                }
                                etBody.requestLayout();
                            }

                            @Override
                            protected void onException(Bundle args, Throwable ex) {
                                Helper.unexpectedError(getFragmentManager(), ex);
                            }
                        }.execute(FragmentCompose.this, args, "compose:cid:" + source);
                    }
                });
            } else
                lld.setLevel(1); // image place holder

            return lld;
        }
    };

    private AdapterView.OnItemSelectedListener identitySelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            EntityIdentity identity = (EntityIdentity) parent.getAdapter().getItem(position);

            int at = (identity == null ? -1 : identity.email.indexOf('@'));
            etExtra.setHint(at < 0 ? null : identity.email.substring(0, at));
            tvDomain.setText(at < 0 ? null : identity.email.substring(at));
            grpExtra.setVisibility(identity != null && identity.sender_extra ? View.VISIBLE : View.GONE);

            Spanned signature = null;
            if (identity != null && !TextUtils.isEmpty(identity.signature))
                signature = HtmlHelper.fromHtml(identity.signature, new Html.ImageGetter() {
                    @Override
                    public Drawable getDrawable(String source) {
                        int px = Helper.dp2pixels(getContext(), 24);
                        Drawable d = getContext().getResources()
                                .getDrawable(R.drawable.baseline_image_24, getContext().getTheme());
                        d.setBounds(0, 0, px, px);
                        return d;
                    }
                }, null);
            tvSignature.setText(signature);
            grpSignature.setVisibility(signature == null ? View.GONE : View.VISIBLE);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            etExtra.setHint("");
            tvDomain.setText(null);

            tvSignature.setText(null);
            grpSignature.setVisibility(View.GONE);
        }
    };

    private ActivityBase.IBackPressedListener onBackPressedListener = new ActivityBase.IBackPressedListener() {
        @Override
        public boolean onBackPressed() {
            if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                onExit();
            return true;
        }
    };

    public static class FragmentDialogContactGroup extends FragmentDialogBase {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            final long working = getArguments().getLong("working");

            View dview = LayoutInflater.from(getContext()).inflate(R.layout.dialog_contact_group, null);
            final Spinner spGroup = dview.findViewById(R.id.spGroup);
            final Spinner spTarget = dview.findViewById(R.id.spTarget);

            Cursor groups = getContext().getContentResolver().query(
                    ContactsContract.Groups.CONTENT_SUMMARY_URI,
                    new String[]{
                            ContactsContract.Groups._ID,
                            ContactsContract.Groups.TITLE,
                            ContactsContract.Groups.SUMMARY_COUNT
                    },
                    ContactsContract.Groups.DELETED + " = 0" +
                            " AND " + ContactsContract.Groups.SUMMARY_COUNT + " > 0",
                    null,
                    ContactsContract.Groups.TITLE
            );

            SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                    getContext(),
                    R.layout.spinner_item1_dropdown,
                    groups,
                    new String[]{ContactsContract.Groups.TITLE},
                    new int[]{android.R.id.text1},
                    0);
            spGroup.setAdapter(adapter);

            return new AlertDialog.Builder(getContext())
                    .setView(dview)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int target = spTarget.getSelectedItemPosition();
                            Cursor cursor = (Cursor) spGroup.getSelectedItem();
                            if (target != INVALID_POSITION && cursor != null) {
                                long group = cursor.getLong(0);

                                Bundle args = getArguments();
                                args.putLong("id", working);
                                args.putInt("target", target);
                                args.putLong("group", group);

                                sendResult(RESULT_OK);
                            } else
                                sendResult(RESULT_CANCELED);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
        }
    }

    public static class FragmentDialogAnswer extends FragmentDialogBase {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            final ArrayAdapter<EntityAnswer> adapter =
                    new ArrayAdapter<>(getContext(), R.layout.spinner_item1, android.R.id.text1);

            // TODO: spinner
            new SimpleTask<List<EntityAnswer>>() {
                @Override
                protected List<EntityAnswer> onExecute(Context context, Bundle args) {
                    DB db = DB.getInstance(getContext());
                    return db.answer().getAnswers(false);
                }

                @Override
                protected void onExecuted(Bundle args, List<EntityAnswer> answers) {
                    adapter.addAll(answers);
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Helper.unexpectedError(getFragmentManager(), ex);
                }
            }.execute(this, new Bundle(), "compose:answer");

            return new AlertDialog.Builder(getContext())
                    .setTitle(R.string.title_insert_template)
                    .setAdapter(adapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            EntityAnswer answer = adapter.getItem(which);
                            getArguments().putString("answer", answer.text);

                            sendResult(RESULT_OK);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
        }
    }

    public static class FragmentDialogSend extends FragmentDialogBase {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            long id = getArguments().getLong("id");

            Bundle args = getArguments();
            boolean dialog = args.getBundle("extras").getBoolean("dialog");
            boolean remind_subject = args.getBoolean("remind_subject", false);
            boolean remind_text = args.getBoolean("remind_text", false);
            boolean remind_attachment = args.getBoolean("remind_attachment", false);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            int send_delayed = prefs.getInt("send_delayed", 0);
            boolean send_dialog = prefs.getBoolean("send_dialog", true);

            final int[] sendDelayedValues = getResources().getIntArray(R.array.sendDelayedValues);
            final String[] sendDelayedNames = getResources().getStringArray(R.array.sendDelayedNames);

            final ViewGroup dview = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.dialog_send, null);
            final TextView tvRemindSubject = dview.findViewById(R.id.tvRemindSubject);
            final TextView tvRemindText = dview.findViewById(R.id.tvRemindText);
            final TextView tvRemindAttachment = dview.findViewById(R.id.tvRemindAttachment);
            final TextView tvTo = dview.findViewById(R.id.tvTo);
            final TextView tvVia = dview.findViewById(R.id.tvVia);
            final CheckBox cbPlainOnly = dview.findViewById(R.id.cbPlainOnly);
            final CheckBox cbEncrypt = dview.findViewById(R.id.cbEncrypt);
            final CheckBox cbReceipt = dview.findViewById(R.id.cbReceipt);
            final TextView tvReceipt = dview.findViewById(R.id.tvReceipt);
            final Spinner spPriority = dview.findViewById(R.id.spPriority);
            final TextView tvSendAt = dview.findViewById(R.id.tvSendAt);
            final ImageButton ibSendAt = dview.findViewById(R.id.ibSendAt);
            final CheckBox cbNotAgain = dview.findViewById(R.id.cbNotAgain);
            final TextView tvNotAgain = dview.findViewById(R.id.tvNotAgain);

            tvRemindSubject.setVisibility(remind_subject ? View.VISIBLE : View.GONE);
            tvRemindText.setVisibility(remind_text ? View.VISIBLE : View.GONE);
            tvRemindAttachment.setVisibility(remind_attachment ? View.VISIBLE : View.GONE);
            tvTo.setText(null);
            tvVia.setText(null);
            tvReceipt.setVisibility(View.GONE);
            spPriority.setTag(1);
            spPriority.setSelection(1);
            tvSendAt.setText(null);
            cbNotAgain.setChecked(!send_dialog);
            cbNotAgain.setVisibility(dialog ? View.VISIBLE : View.GONE);
            tvNotAgain.setVisibility(cbNotAgain.isChecked() && send_dialog ? View.VISIBLE : View.GONE);

            Helper.setViewsEnabled(dview, false);

            cbNotAgain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    prefs.edit().putBoolean("send_dialog", !isChecked).apply();
                    tvNotAgain.setVisibility(isChecked && send_dialog ? View.VISIBLE : View.GONE);
                }
            });

            cbPlainOnly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                    Bundle args = new Bundle();
                    args.putLong("id", id);
                    args.putBoolean("plain_only", checked);

                    new SimpleTask<Void>() {
                        @Override
                        protected Void onExecute(Context context, Bundle args) {
                            long id = args.getLong("id");
                            boolean plain_only = args.getBoolean("plain_only");

                            DB db = DB.getInstance(context);
                            db.message().setMessagePlainOnly(id, plain_only);

                            return null;
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Helper.unexpectedError(getFragmentManager(), ex);
                        }
                    }.execute(FragmentDialogSend.this, args, "compose:plain_only");
                }
            });

            cbEncrypt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                    Bundle args = new Bundle();
                    args.putLong("id", id);
                    args.putBoolean("encrypt", checked);

                    new SimpleTask<Void>() {
                        @Override
                        protected Void onExecute(Context context, Bundle args) {
                            long id = args.getLong("id");
                            boolean encrypt = args.getBoolean("encrypt");

                            DB db = DB.getInstance(context);
                            db.message().setMessageEncrypt(id, encrypt);

                            return null;
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Helper.unexpectedError(getFragmentManager(), ex);
                        }
                    }.execute(FragmentDialogSend.this, args, "compose:encrypt");
                }
            });

            cbReceipt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                    tvReceipt.setVisibility(checked ? View.VISIBLE : View.GONE);

                    Bundle args = new Bundle();
                    args.putLong("id", id);
                    args.putBoolean("receipt", checked);

                    new SimpleTask<Void>() {
                        @Override
                        protected Void onExecute(Context context, Bundle args) {
                            long id = args.getLong("id");
                            boolean receipt = args.getBoolean("receipt");

                            DB db = DB.getInstance(context);
                            db.message().setMessageReceiptRequest(id, receipt);

                            return null;
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Helper.unexpectedError(getFragmentManager(), ex);
                        }
                    }.execute(FragmentDialogSend.this, args, "compose:receipt");
                }
            });

            spPriority.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    int last = (int) spPriority.getTag();
                    if (last != position) {
                        spPriority.setTag(position);
                        setPriority(position);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    spPriority.setTag(1);
                    setPriority(1);
                }

                private void setPriority(int priority) {
                    Bundle args = new Bundle();
                    args.putLong("id", id);
                    args.putInt("priority", priority);

                    new SimpleTask<Void>() {
                        @Override
                        protected Void onExecute(Context context, Bundle args) {
                            long id = args.getLong("id");
                            int priority = args.getInt("priority");

                            DB db = DB.getInstance(context);
                            db.message().setMessagePriority(id, priority);

                            return null;
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Helper.unexpectedError(getFragmentManager(), ex);
                        }
                    }.execute(FragmentDialogSend.this, args, "compose:priority");
                }
            });

            ibSendAt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle args = new Bundle();
                    args.putString("title", getString(R.string.title_send_at));
                    args.putLong("id", id);

                    FragmentDialogDuration fragment = new FragmentDialogDuration();
                    fragment.setArguments(args);
                    fragment.setTargetFragment(FragmentDialogSend.this, 1);
                    fragment.show(getFragmentManager(), "send:snooze");
                }
            });

            DB db = DB.getInstance(getContext());
            db.message().liveMessage(id).observe(getViewLifecycleOwner(), new Observer<TupleMessageEx>() {
                @Override
                public void onChanged(TupleMessageEx draft) {
                    if (draft == null) {
                        dismiss();
                        return;
                    }

                    int plus = (draft.cc == null ? 0 : draft.cc.length) +
                            (draft.bcc == null ? 0 : draft.bcc.length);
                    tvTo.setText(MessageHelper.formatAddressesShort(draft.to) + (plus > 0 ? " +" + plus : ""));
                    tvVia.setText(draft.identityEmail);

                    cbPlainOnly.setChecked(draft.plain_only != null && draft.plain_only);
                    cbEncrypt.setChecked(draft.encrypt != null && draft.encrypt);
                    cbReceipt.setChecked(draft.receipt_request != null && draft.receipt_request);

                    cbPlainOnly.setVisibility(draft.receipt != null && draft.receipt ? View.GONE : View.VISIBLE);
                    cbEncrypt.setVisibility(draft.receipt != null && draft.receipt ? View.GONE : View.VISIBLE);
                    cbReceipt.setVisibility(draft.receipt != null && draft.receipt ? View.GONE : View.VISIBLE);

                    int priority = (draft.priority == null ? 1 : draft.priority);
                    spPriority.setTag(priority);
                    spPriority.setSelection(priority);

                    if (draft.ui_snoozed == null) {
                        if (send_delayed == 0)
                            tvSendAt.setText(getString(R.string.title_now));
                        else
                            for (int pos = 0; pos < sendDelayedValues.length; pos++)
                                if (sendDelayedValues[pos] == send_delayed) {
                                    tvSendAt.setText(getString(R.string.title_after, sendDelayedNames[pos]));
                                    break;
                                }
                    } else {
                        DateFormat DTF = Helper.getDateTimeInstance(getContext(), SimpleDateFormat.MEDIUM, SimpleDateFormat.SHORT);
                        DateFormat D = new SimpleDateFormat("E");
                        tvSendAt.setText(D.format(draft.ui_snoozed) + " " + DTF.format(draft.ui_snoozed));
                    }

                    Helper.setViewsEnabled(dview, true);
                }
            });

            return new AlertDialog.Builder(getContext())
                    .setView(dview)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getArguments().putBoolean("encrypt", cbEncrypt.isChecked());
                            sendResult(Activity.RESULT_OK);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
            super.onActivityResult(requestCode, resultCode, intent);

            if (resultCode == RESULT_OK && intent != null) {
                Bundle data = intent.getBundleExtra("args");
                long id = data.getLong("id");
                long duration = data.getLong("duration");
                long time = data.getLong("time");

                Bundle args = new Bundle();
                args.putLong("id", id);
                args.putLong("wakeup", duration == 0 ? -1 : time);

                new SimpleTask<Void>() {
                    @Override
                    protected Void onExecute(Context context, Bundle args) {
                        long id = args.getLong("id");
                        Long wakeup = args.getLong("wakeup");

                        DB db = DB.getInstance(context);
                        db.message().setMessageSnoozed(id, wakeup < 0 ? null : wakeup);

                        return null;
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Helper.unexpectedError(getFragmentManager(), ex);
                    }
                }.execute(this, args, "compose:snooze");
            }
        }
    }

    private class DraftData {
        private EntityMessage draft;
        private List<TupleIdentityEx> identities;
    }
}
