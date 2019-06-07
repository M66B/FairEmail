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
import android.database.CursorWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.QuoteSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.FileProvider;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.exifinterface.media.ExifInterface;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.colorpicker.ColorPickerDialog;
import com.android.colorpicker.ColorPickerSwatch;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;
import com.google.android.material.snackbar.Snackbar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.MessageRemovedException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import static android.app.Activity.RESULT_OK;

public class FragmentCompose extends FragmentBase {
    private enum State {NONE, LOADING, LOADED}

    private ViewGroup view;
    private Spinner spIdentity;
    private EditText etExtra;
    private TextView tvDomain;
    private MultiAutoCompleteTextView etTo;
    private ImageView ivToAdd;
    private MultiAutoCompleteTextView etCc;
    private ImageView ivCcAdd;
    private MultiAutoCompleteTextView etBcc;
    private ImageView ivBccAdd;
    private EditText etSubject;
    private ImageView ivCcBcc;
    private RecyclerView rvAttachment;
    private TextView tvNoInternetAttachments;
    private EditTextCompose etBody;
    private TextView tvNoInternet;
    private TextView tvSignature;
    private TextView tvReference;
    private ImageButton ibReferenceEdit;
    private ImageButton ibReferenceImages;
    private BottomNavigationView edit_bar;
    private BottomNavigationView bottom_navigation;
    private ContentLoadingProgressBar pbWait;
    private Group grpHeader;
    private Group grpExtra;
    private Group grpAddresses;
    private Group grpAttachments;
    private Group grpBody;
    private Group grpSignature;
    private Group grpReference;

    private ContentResolver resolver;
    private AdapterAttachment adapter;

    private boolean prefix_once = false;
    private boolean monospaced = false;
    private boolean style = true;
    private boolean plain_only = false;
    private boolean encrypt = false;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefix_once = prefs.getBoolean("prefix_once", false);
        monospaced = prefs.getBoolean("monospaced", false);
        style = prefs.getBoolean("style_toolbar", true);
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
        ivToAdd = view.findViewById(R.id.ivToAdd);
        etCc = view.findViewById(R.id.etCc);
        ivCcAdd = view.findViewById(R.id.ivCcAdd);
        etBcc = view.findViewById(R.id.etBcc);
        ivBccAdd = view.findViewById(R.id.ivBccAdd);
        etSubject = view.findViewById(R.id.etSubject);
        ivCcBcc = view.findViewById(R.id.ivCcBcc);
        rvAttachment = view.findViewById(R.id.rvAttachment);
        tvNoInternetAttachments = view.findViewById(R.id.tvNoInternetAttachments);
        etBody = view.findViewById(R.id.etBody);
        tvNoInternet = view.findViewById(R.id.tvNoInternet);
        tvSignature = view.findViewById(R.id.tvSignature);
        tvReference = view.findViewById(R.id.tvReference);
        ibReferenceEdit = view.findViewById(R.id.ibReferenceEdit);
        ibReferenceImages = view.findViewById(R.id.ibReferenceImages);
        edit_bar = view.findViewById(R.id.edit_bar);
        bottom_navigation = view.findViewById(R.id.bottom_navigation);
        pbWait = view.findViewById(R.id.pbWait);
        grpHeader = view.findViewById(R.id.grpHeader);
        grpExtra = view.findViewById(R.id.grpExtra);
        grpAddresses = view.findViewById(R.id.grpAddresses);
        grpAttachments = view.findViewById(R.id.grpAttachments);
        grpBody = view.findViewById(R.id.grpBody);
        grpSignature = view.findViewById(R.id.grpSignature);
        grpReference = view.findViewById(R.id.grpReference);

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

        ivCcBcc.setOnClickListener(new View.OnClickListener() {
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
                        request = ActivityCompose.REQUEST_CONTACT_TO;
                        break;
                    case R.id.ivCcAdd:
                        request = ActivityCompose.REQUEST_CONTACT_CC;
                        break;
                    case R.id.ivBccAdd:
                        request = ActivityCompose.REQUEST_CONTACT_BCC;
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

        ivToAdd.setOnClickListener(onPick);
        ivCcAdd.setOnClickListener(onPick);
        ivBccAdd.setOnClickListener(onPick);

        setZoom();

        etBody.setCustomSelectionActionModeCallback(actionCallback);

        etBody.setInputContentListener(new EditTextCompose.IInputContentListener() {
            @Override
            public void onInputContent(Uri uri) {
                handleAddAttachment(uri, true);
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

        PackageManager pm = getContext().getPackageManager();
        Intent take_photo = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Intent record_audio = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
        edit_bar.getMenu().findItem(R.id.menu_take_photo).setVisible(take_photo.resolveActivity(pm) != null);
        edit_bar.getMenu().findItem(R.id.menu_record_audio).setVisible(record_audio.resolveActivity(pm) != null);


        edit_bar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
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

        bottom_navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                final int action = item.getItemId();
                switch (action) {
                    case R.id.action_delete:
                        onActionDelete();
                        break;
                    case R.id.action_send:
                        onActionSend();
                        break;
                    default:
                        onAction(action);
                }
                return true;
            }
        });

        view.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);

        addBackPressedListener(onBackPressedListener);

        // Initialize
        setSubtitle(R.string.title_compose);
        setHasOptionsMenu(true);

        etExtra.setHint("");
        tvDomain.setText(null);

        grpHeader.setVisibility(View.GONE);
        grpExtra.setVisibility(View.GONE);
        grpAddresses.setVisibility(View.GONE);
        grpAttachments.setVisibility(View.GONE);
        tvNoInternet.setVisibility(View.GONE);
        grpBody.setVisibility(View.GONE);
        grpSignature.setVisibility(View.GONE);
        grpReference.setVisibility(View.GONE);
        ibReferenceEdit.setVisibility(View.GONE);
        ibReferenceImages.setVisibility(View.GONE);
        edit_bar.setVisibility(View.GONE);
        bottom_navigation.setVisibility(View.GONE);
        pbWait.setVisibility(View.VISIBLE);

        getActivity().invalidateOptionsMenu();
        Helper.setViewsEnabled(view, false);

        final DB db = DB.getInstance(getContext());
        final boolean contacts = Helper.hasPermission(getContext(), Manifest.permission.READ_CONTACTS);

        SimpleCursorAdapter cadapter = new SimpleCursorAdapter(
                getContext(),
                R.layout.spinner_item2_dropdown,
                null,
                contacts
                        ? new String[]{
                        ContactsContract.Contacts.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Email.DATA}
                        : new String[]{"name", "email"},
                new int[]{android.R.id.text1, android.R.id.text2},
                0);

        cadapter.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
            public CharSequence convertToString(Cursor cursor) {
                int colName = cursor.getColumnIndex(contacts ? ContactsContract.Contacts.DISPLAY_NAME : "name");
                int colEmail = cursor.getColumnIndex(contacts ? ContactsContract.CommonDataKinds.Email.DATA : "email");
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

        etTo.setAdapter(cadapter);
        etCc.setAdapter(cadapter);
        etBcc.setAdapter(cadapter);

        etTo.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        etCc.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        etBcc.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

        if (contacts)
            cadapter.setFilterQueryProvider(new FilterQueryProvider() {
                public Cursor runQuery(CharSequence typed) {
                    Log.i("Searching provided contact=" + typed);
                    String wildcard = "%" + typed + "%";
                    return new CursorWrapper(resolver.query(
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
                                    ", " + ContactsContract.CommonDataKinds.Email.DATA + " COLLATE NOCASE")) {

                        @Override
                        public String[] getColumnNames() {
                            String[] names = super.getColumnNames();
                            names[0] = "_id";
                            return names;
                        }

                        @Override
                        public String getColumnName(int index) {
                            if (index == 0)
                                return "_id";
                            return super.getColumnName(index);
                        }

                        @Override
                        public int getColumnIndex(String name) {
                            if ("_id".equals(name))
                                return 0;
                            return super.getColumnIndex(name);
                        }

                        @Override
                        public int getColumnIndexOrThrow(String name) throws IllegalArgumentException {
                            if ("_id".equals(name))
                                return 0;
                            return super.getColumnIndexOrThrow(name);
                        }
                    };
                }
            });
        else
            cadapter.setFilterQueryProvider(new FilterQueryProvider() {
                @Override
                public Cursor runQuery(CharSequence typed) {
                    Log.i("Searching local contact=" + typed);
                    String wildcard = "%" + typed + "%";
                    return db.contact().searchContacts(null, null, wildcard);
                }
            });

        rvAttachment.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rvAttachment.setLayoutManager(llm);
        rvAttachment.setItemAnimator(null);

        adapter = new AdapterAttachment(getContext(), getViewLifecycleOwner(), false);
        rvAttachment.setAdapter(adapter);

        tvNoInternetAttachments.setVisibility(View.GONE);

        pgpService = new OpenPgpServiceConnection(getContext(), "org.sufficientlysecure.keychain");
        pgpService.bindToService();

        return view;
    }

    private void onReferenceEdit() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (prefs.getBoolean("edit_ref_confirmed", false)) {
            onReferenceEditConfirmed();
            return;
        }

        final View dview = LayoutInflater.from(getContext()).inflate(R.layout.dialog_ask_again, null);
        final TextView tvMessage = dview.findViewById(R.id.tvMessage);
        final CheckBox cbNotAgain = dview.findViewById(R.id.cbNotAgain);

        tvMessage.setText(getText(R.string.title_ask_edit_ref));

        new DialogBuilderLifecycle(getContext(), getViewLifecycleOwner())
                .setView(dview)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (cbNotAgain.isChecked())
                            prefs.edit().putBoolean("edit_ref_confirmed", true).apply();
                        onReferenceEditConfirmed();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void onReferenceEditConfirmed() {
        Bundle args = new Bundle();
        args.putLong("id", working);
        args.putString("body", HtmlHelper.toHtml(etBody.getText()));

        new SimpleTask<EntityMessage>() {
            @Override
            protected void onPreExecute(Bundle args) {
                ibReferenceEdit.setEnabled(false);
            }

            @Override
            protected void onPostExecute(Bundle args) {
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
                String html = "<p>" + plain.replaceAll("\\r?\\n", "<br />" + "</p>");

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
                plain_only = true;
                getActivity().invalidateOptionsMenu();
                showDraft(draft);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
            }
        }.execute(FragmentCompose.this, args, "compose:refedit");
    }

    private void onReferenceImages() {
        show_images = true;
        onAction(R.id.action_save);
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
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        state = State.NONE;

        if (savedInstanceState == null) {
            if (working < 0) {
                Bundle args = new Bundle();
                args.putString("action", getArguments().getString("action"));
                args.putLong("id", getArguments().getLong("id", -1));
                args.putLong("account", getArguments().getLong("account", -1));
                args.putLong("reference", getArguments().getLong("reference", -1));
                args.putSerializable("ics", getArguments().getSerializable("ics"));
                args.putBoolean("raw", getArguments().getBoolean("raw", false));
                args.putLong("answer", getArguments().getLong("answer", -1));
                args.putString("to", getArguments().getString("to"));
                args.putString("cc", getArguments().getString("cc"));
                args.putString("bcc", getArguments().getString("bcc"));
                args.putString("subject", getArguments().getString("subject"));
                args.putString("body", getArguments().getString("body"));
                args.putParcelableArrayList("attachments", getArguments().getParcelableArrayList("attachments"));
                draftLoader.execute(this, args, "compose:new");
            } else {
                Bundle args = new Bundle();
                args.putString("action", "edit");
                args.putLong("id", working);
                args.putLong("account", -1);
                args.putLong("reference", -1);
                args.putLong("answer", -1);
                draftLoader.execute(this, args, "compose:edit");
            }
        } else {
            working = savedInstanceState.getLong("fair:working");
            show_images = savedInstanceState.getBoolean("fair:show_images");

            Bundle args = new Bundle();
            args.putString("action", working < 0 ? "new" : "edit");
            args.putLong("id", working);
            args.putLong("account", -1);
            args.putLong("reference", -1);
            args.putLong("answer", -1);
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
                        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED))
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

        menu.findItem(R.id.menu_zoom).setVisible(state == State.LOADED);
        menu.findItem(R.id.menu_media_toolbar).setVisible(state == State.LOADED);
        menu.findItem(R.id.menu_clear).setVisible(state == State.LOADED);
        menu.findItem(R.id.menu_contact_group).setVisible(state == State.LOADED);
        menu.findItem(R.id.menu_answer).setVisible(state == State.LOADED);
        menu.findItem(R.id.menu_plain_only).setVisible(state == State.LOADED);
        menu.findItem(R.id.menu_encrypt).setVisible(state == State.LOADED);
        menu.findItem(R.id.menu_send_after).setVisible(state == State.LOADED);

        menu.findItem(R.id.menu_zoom).setEnabled(!busy);
        menu.findItem(R.id.menu_clear).setEnabled(!busy);
        menu.findItem(R.id.menu_contact_group).setEnabled(!busy && hasPermission(Manifest.permission.READ_CONTACTS));
        menu.findItem(R.id.menu_answer).setEnabled(!busy);
        menu.findItem(R.id.menu_plain_only).setEnabled(!busy);
        menu.findItem(R.id.menu_encrypt).setEnabled(!busy);
        menu.findItem(R.id.menu_send_after).setEnabled(!busy);

        menu.findItem(R.id.menu_media_toolbar).setChecked(style);

        menu.findItem(R.id.menu_plain_only).setChecked(plain_only);
        menu.findItem(R.id.menu_encrypt).setChecked(encrypt);
        bottom_navigation.getMenu().findItem(R.id.action_send)
                .setTitle(encrypt ? R.string.title_encrypt : R.string.title_send);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED))
                    handleExit();
                return true;
            case R.id.menu_zoom:
                onMenuZoom();
                return true;
            case R.id.menu_media_toolbar:
                onMenuStyleToolbar();
                return true;
            case R.id.menu_clear:
                onMenuClear();
                return true;
            case R.id.menu_contact_group:
                onMenuContactGroup();
                return true;
            case R.id.menu_answer:
                onMenuAnswer();
                return true;
            case R.id.menu_plain_only:
                onMenuPlainOnly();
                return true;
            case R.id.menu_encrypt:
                onMenuEncrypt();
                return true;
            case R.id.menu_send_after:
                onMenuSendAfter();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onMenuAddresses() {
        grpAddresses.setVisibility(grpAddresses.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
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

    private void onMenuStyleToolbar() {
        style = !style;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefs.edit().putBoolean("style_toolbar", style).apply();
        edit_bar.setVisibility(style ? View.VISIBLE : View.GONE);
    }

    private void onMenuClear() {
        int end = etBody.getSelectionEnd();
        if (end < 0)
            end = 0;

        SpannableString ss = new SpannableString(etBody.getText());

        for (Object span : ss.getSpans(0, ss.length(), Object.class))
            if (!(span instanceof ImageSpan))
                ss.removeSpan(span);

        etBody.setText(ss);
        etBody.setSelection(end);
    }

    private void onMenuContactGroup() {
        View dview = LayoutInflater.from(getContext()).inflate(R.layout.dialog_contact_group, null);
        final ListView lvGroup = dview.findViewById(R.id.lvGroup);
        final Spinner spTarget = dview.findViewById(R.id.spTarget);

        Cursor groups = resolver.query(
                ContactsContract.Groups.CONTENT_URI,
                new String[]{
                        ContactsContract.Groups._ID,
                        ContactsContract.Groups.TITLE
                },
                null, null, ContactsContract.Groups.TITLE
        );

        final SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                getContext(),
                R.layout.spinner_item1_dropdown,
                groups,
                new String[]{ContactsContract.Groups.TITLE},
                new int[]{android.R.id.text1},
                0);

        lvGroup.setAdapter(adapter);

        final AlertDialog dialog = new DialogBuilderLifecycle(getContext(), getViewLifecycleOwner())
                .setView(dview)
                .create();

        lvGroup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dialog.dismiss();

                int target = spTarget.getSelectedItemPosition();
                Cursor cursor = (Cursor) adapter.getItem(position);
                long group = cursor.getLong(0);

                if (target > 0)
                    grpAddresses.setVisibility(View.VISIBLE);

                Bundle args = new Bundle();
                args.putLong("id", working);
                args.putInt("target", target);
                args.putLong("group", group);

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
                                try (Cursor contact = resolver.query(
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
                        Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
                    }
                }.execute(getContext(), getViewLifecycleOwner(), args, "compose:picked");
            }
        });

        dialog.show();
    }

    private void onMenuAnswer() {
        if (!Helper.isPro(getContext())) {
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
            lbm.sendBroadcast(new Intent(ActivityCompose.ACTION_SHOW_PRO));
            return;
        }

        new SimpleTask<List<EntityAnswer>>() {
            @Override
            protected List<EntityAnswer> onExecute(Context context, Bundle args) {
                DB db = DB.getInstance(getContext());
                return db.answer().getAnswers(false);
            }

            @Override
            protected void onExecuted(Bundle args, List<EntityAnswer> answers) {
                final ArrayAdapter<EntityAnswer> adapter =
                        new ArrayAdapter<>(getContext(), R.layout.spinner_item1, android.R.id.text1, answers);

                new DialogBuilderLifecycle(getContext(), getViewLifecycleOwner())
                        .setAdapter(adapter, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                EntityAnswer answer = adapter.getItem(which);

                                String text = EntityAnswer.replacePlaceholders(answer.text, null, null, null, null);
                                Spanned spanned = HtmlHelper.fromHtml(text);

                                etBody.getText().insert(etBody.getSelectionStart(), spanned);
                            }
                        })
                        .show();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
            }
        }.execute(this, new Bundle(), "compose:answer");
    }

    private void onMenuPlainOnly() {
        plain_only = !plain_only;
        getActivity().invalidateOptionsMenu();
        onAction(R.id.action_save);
    }

    private void onMenuEncrypt() {
        encrypt = !encrypt;
        getActivity().invalidateOptionsMenu();
    }

    private void onMenuSendAfter() {
        DialogDuration.show(getContext(), getViewLifecycleOwner(), R.string.title_send_at,
                new DialogDuration.IDialogDuration() {
                    @Override
                    public void onDurationSelected(long duration, long time) {
                        if (!Helper.isPro(getContext())) {
                            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
                            lbm.sendBroadcast(new Intent(ActivityCompose.ACTION_SHOW_PRO));
                            return;
                        }

                        Bundle args = new Bundle();
                        args.putLong("id", working);
                        args.putLong("wakeup", time);

                        new SimpleTask<Void>() {
                            @Override
                            protected Void onExecute(Context context, Bundle args) {
                                long id = args.getLong("id");
                                Long wakeup = args.getLong("wakeup");

                                DB db = DB.getInstance(context);
                                db.message().setMessageSnoozed(id, wakeup);

                                return null;
                            }

                            @Override
                            protected void onExecuted(Bundle args, Void data) {
                                onAction(R.id.action_send);
                            }

                            @Override
                            protected void onException(Bundle args, Throwable ex) {
                                Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
                            }
                        }.execute(FragmentCompose.this, args, "compose:send:after");
                    }

                    @Override
                    public void onDismiss() {

                    }
                });
    }

    private void onActionRecordAudio() {
        Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
        startActivityForResult(intent, ActivityCompose.REQUEST_RECORD_AUDIO);
    }

    private void onActionTakePhoto() {
        File dir = new File(getContext().getFilesDir(), "temporary");
        if (!dir.exists())
            dir.mkdir();
        File file = new File(dir, new Date().getTime() + ".jpg");

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        photoURI = FileProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID, file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        startActivityForResult(intent, ActivityCompose.REQUEST_TAKE_PHOTO);
    }

    private void onActionImage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        PackageManager pm = getContext().getPackageManager();
        if (intent.resolveActivity(pm) == null)
            Snackbar.make(view, R.string.title_no_saf, Snackbar.LENGTH_LONG).show();
        else
            startActivityForResult(Helper.getChooser(getContext(), intent), ActivityCompose.REQUEST_IMAGE);
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
            startActivityForResult(Helper.getChooser(getContext(), intent), ActivityCompose.REQUEST_ATTACHMENT);
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

        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_insert_link, null);
        final EditText etLink = view.findViewById(R.id.etLink);
        final TextView tvInsecure = view.findViewById(R.id.tvInsecure);

        etLink.setText(uri == null ? "https://" : uri.toString());
        tvInsecure.setVisibility(View.GONE);

        etLink.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvInsecure.setVisibility("http".equals(Uri.parse(s.toString()).getScheme()) ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        new DialogBuilderLifecycle(getContext(), getViewLifecycleOwner())
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int start = etBody.getSelectionStart();
                        int end = etBody.getSelectionEnd();

                        if (start < 0)
                            start = 0;
                        if (end < 0)
                            end = 0;

                        if (start > end) {
                            int tmp = start;
                            start = end;
                            end = tmp;
                        }

                        String link = etLink.getText().toString();
                        if (start == end) {
                            etBody.setText(etBody.getText().insert(start, link));
                            end = start + link.length();
                        }

                        SpannableString ss = new SpannableString(etBody.getText());

                        for (URLSpan span : ss.getSpans(start, end, URLSpan.class))
                            ss.removeSpan(span);

                        ss.setSpan(new URLSpan(link), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        etBody.setText(ss);
                        etBody.setSelection(end);
                    }
                })
                .show();
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                etLink.requestFocus();
            }
        });
    }

    private void onActionDelete() {
        new DialogBuilderLifecycle(getContext(), getViewLifecycleOwner())
                .setMessage(R.string.title_ask_discard)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onAction(R.id.action_delete);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void onActionSend() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean autosend = prefs.getBoolean("autosend", false);
        if (autosend) {
            onActionSendConfirmed();
            return;
        }

        try {
            EntityIdentity ident = (EntityIdentity) spIdentity.getSelectedItem();
            if (ident == null)
                throw new IllegalArgumentException(getString(R.string.title_from_missing));

            String to = etTo.getText().toString();
            String cc = etCc.getText().toString();
            String bcc = etBcc.getText().toString();

            InternetAddress ato[] = new InternetAddress[0];
            InternetAddress acc[] = new InternetAddress[0];
            InternetAddress abcc[] = new InternetAddress[0];

            if (!TextUtils.isEmpty(to))
                ato = InternetAddress.parse(to);

            if (!TextUtils.isEmpty(cc))
                acc = InternetAddress.parse(cc);

            if (!TextUtils.isEmpty(bcc))
                abcc = InternetAddress.parse(bcc);

            if (ato.length == 0)
                throw new IllegalArgumentException(getString(R.string.title_to_missing));

            final View dview = LayoutInflater.from(getContext()).inflate(R.layout.dialog_ask_again, null);
            final TextView tvMessage = dview.findViewById(R.id.tvMessage);
            final CheckBox cbNotAgain = dview.findViewById(R.id.cbNotAgain);

            int plus = acc.length + abcc.length;

            tvMessage.setText(getString(R.string.title_ask_send_via,
                    MessageHelper.formatAddressesShort(ato) + (plus > 0 ? " +" + plus : ""), ident.email));

            new DialogBuilderLifecycle(getContext(), getViewLifecycleOwner())
                    .setView(dview)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (cbNotAgain.isChecked())
                                prefs.edit().putBoolean("autosend", true).apply();
                            onActionSendConfirmed();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        } catch (Throwable ex) {
            onActionSendConfirmed();
        }
    }

    private void onActionSendConfirmed() {
        if (encrypt)
            onAction(R.id.menu_encrypt);
        else
            onAction(R.id.action_send);
    }

    private void onEncrypt() {
        if (!Helper.isPro(getContext())) {
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
            lbm.sendBroadcast(new Intent(ActivityCompose.ACTION_SHOW_PRO));
            return;
        }

        if (pgpService.isBound())
            try {
                String to = etTo.getText().toString();
                InternetAddress ato[] = (TextUtils.isEmpty(to) ? new InternetAddress[0] : InternetAddress.parse(to));
                if (ato.length == 0)
                    throw new IllegalArgumentException(getString(R.string.title_to_missing));

                String[] tos = new String[ato.length];
                for (int i = 0; i < ato.length; i++)
                    tos[i] = ato[i].getAddress();

                Intent intent = new Intent(OpenPgpApi.ACTION_GET_KEY_IDS);
                intent.putExtra(OpenPgpApi.EXTRA_USER_IDS, tos);
                doPgp(intent);
            } catch (Throwable ex) {
                if (ex instanceof IllegalArgumentException)
                    Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG).show();
                else
                    Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
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

    private void doPgp(Intent data) {
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

                List<EntityAttachment> attachments = db.attachment().getAttachments(id);
                for (EntityAttachment attachment : new ArrayList<>(attachments))
                    if (attachment.encryption != null) {
                        if (OpenPgpApi.ACTION_GET_KEY_IDS.equals(data.getAction()))
                            db.attachment().deleteAttachment(attachment.id);
                        attachments.remove(attachment);
                    }

                EntityIdentity identity =
                        (message.identity == null ? null : db.identity().getIdentity(message.identity));

                // Build message
                Properties props = MessageHelper.getSessionProperties(
                        ConnectionHelper.AUTH_TYPE_PASSWORD, null, false);
                Session isession = Session.getInstance(props, null);
                MimeMessage imessage = new MimeMessage(isession);
                MessageHelper.build(context, message, attachments, identity, imessage);

                // Serialize message
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                imessage.writeTo(os);
                ByteArrayInputStream decrypted = new ByteArrayInputStream(os.toByteArray());
                ByteArrayOutputStream encrypted = (OpenPgpApi.ACTION_SIGN_AND_ENCRYPT.equals(data.getAction())
                        ? new ByteArrayOutputStream() : null);

                if (BuildConfig.BETA_RELEASE) {
                    Log.i("Execute " + data);
                    Log.logExtras(data);
                }

                // Encrypt message
                OpenPgpApi api = new OpenPgpApi(context, pgpService.getService());
                Intent result = api.executeApi(data, decrypted, encrypted);

                if (BuildConfig.BETA_RELEASE) {
                    Log.i("Result " + result);
                    Log.logExtras(result);
                }

                int resultCode = result.getIntExtra(OpenPgpApi.RESULT_CODE, OpenPgpApi.RESULT_CODE_ERROR);
                switch (resultCode) {
                    case OpenPgpApi.RESULT_CODE_SUCCESS:
                        // Attach encrypted data / signature
                        if (OpenPgpApi.ACTION_SIGN_AND_ENCRYPT.equals(data.getAction()) ||
                                OpenPgpApi.ACTION_DETACHED_SIGN.equals(data.getAction()))
                            try {
                                db.beginTransaction();

                                EntityAttachment attachment = new EntityAttachment();
                                attachment.message = id;
                                attachment.sequence = db.attachment().getAttachmentSequence(id) + 1;
                                attachment.name = (OpenPgpApi.ACTION_SIGN_AND_ENCRYPT.equals(data.getAction())
                                        ? "encrypted.asc" : "signature.asc");
                                attachment.type = "application/octet-stream";
                                attachment.encryption = (OpenPgpApi.ACTION_SIGN_AND_ENCRYPT.equals(data.getAction())
                                        ? EntityAttachment.PGP_MESSAGE : EntityAttachment.PGP_SIGNATURE);
                                attachment.id = db.attachment().insertAttachment(attachment);

                                byte[] bytes = (OpenPgpApi.ACTION_SIGN_AND_ENCRYPT.equals(data.getAction())
                                        ? encrypted.toByteArray()
                                        : result.getByteArrayExtra(OpenPgpApi.RESULT_DETACHED_SIGNATURE));

                                File file = attachment.getFile(context);
                                if (BuildConfig.BETA_RELEASE)
                                    Log.i("Writing " + file + " size=" + bytes.length);
                                try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
                                    out.write(bytes);
                                    db.attachment().setDownloaded(attachment.id, (long) bytes.length);
                                }

                                db.setTransactionSuccessful();
                            } finally {
                                db.endTransaction();
                            }

                        if (OpenPgpApi.ACTION_GET_KEY_IDS.equals(data.getAction())) {
                            pgpKeyIds = result.getLongArrayExtra(OpenPgpApi.EXTRA_KEY_IDS);

                            // Encrypt message
                            Intent intent = new Intent(OpenPgpApi.ACTION_GET_SIGN_KEY_ID);
                            return intent;
                        } else if (OpenPgpApi.ACTION_GET_SIGN_KEY_ID.equals(data.getAction())) {
                            pgpSignKeyId = result.getLongExtra(OpenPgpApi.EXTRA_SIGN_KEY_ID, -1);

                            // Encrypt message
                            Intent intent = new Intent(OpenPgpApi.ACTION_SIGN_AND_ENCRYPT);
                            intent.putExtra(OpenPgpApi.EXTRA_KEY_IDS, pgpKeyIds);
                            intent.putExtra(OpenPgpApi.EXTRA_SIGN_KEY_ID, pgpSignKeyId);
                            intent.putExtra(OpenPgpApi.EXTRA_REQUEST_ASCII_ARMOR, true);
                            return intent;
                        } else if (OpenPgpApi.ACTION_SIGN_AND_ENCRYPT.equals(data.getAction())) {
                            // Sign message
                            Intent intent = new Intent(OpenPgpApi.ACTION_DETACHED_SIGN);
                            intent.putExtra(OpenPgpApi.EXTRA_SIGN_KEY_ID, pgpSignKeyId);
                            return intent;
                        } else {
                            // send message
                            return null;
                        }

                    case OpenPgpApi.RESULT_CODE_USER_INTERACTION_REQUIRED:
                        return result.getParcelableExtra(OpenPgpApi.RESULT_INTENT);

                    case OpenPgpApi.RESULT_CODE_ERROR:
                        OpenPgpError error = result.getParcelableExtra(OpenPgpApi.RESULT_ERROR);
                        if (error == null)
                            throw new IllegalArgumentException("Unknown error");
                        else
                            throw new IllegalArgumentException(error.getMessage());

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
                    doPgp(intent);
                } else if (result instanceof PendingIntent)
                    try {
                        PendingIntent pi = (PendingIntent) result;
                        startIntentSenderForResult(
                                pi.getIntentSender(),
                                ActivityCompose.REQUEST_ENCRYPT,
                                null, 0, 0, 0, null);
                    } catch (IntentSender.SendIntentException ex) {
                        Log.e(ex);
                        Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
                    }
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (ex instanceof IllegalArgumentException)
                    Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG).show();
                else
                    Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
            }
        }.execute(this, args, "compose:encrypt");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == ActivityCompose.REQUEST_IMAGE) {
                if (data != null) {
                    Uri uri = data.getData();
                    if (uri != null)
                        handleAddAttachment(uri, true);
                }
            } else if (requestCode == ActivityCompose.REQUEST_ATTACHMENT ||
                    requestCode == ActivityCompose.REQUEST_RECORD_AUDIO ||
                    requestCode == ActivityCompose.REQUEST_TAKE_PHOTO) {

                if (requestCode == ActivityCompose.REQUEST_TAKE_PHOTO)
                    data = new Intent().setData(photoURI);

                if (data != null) {
                    ClipData clipData = data.getClipData();
                    if (clipData == null) {
                        Uri uri = data.getData();
                        if (uri != null)
                            handleAddAttachment(uri, false);
                    } else {
                        for (int i = 0; i < clipData.getItemCount(); i++) {
                            ClipData.Item item = clipData.getItemAt(i);
                            Uri uri = item.getUri();
                            if (uri != null)
                                handleAddAttachment(uri, false);
                        }
                    }
                }
            } else if (requestCode == ActivityCompose.REQUEST_ENCRYPT) {
                if (data != null) {
                    if (BuildConfig.BETA_RELEASE)
                        Log.logExtras(data);
                    doPgp(data);
                }
            } else {
                if (data != null)
                    handlePickContact(requestCode, data);
            }
        }
    }

    private void handlePickContact(int requestCode, Intent data) {
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
                            if (requestCode == ActivityCompose.REQUEST_CONTACT_TO)
                                address = draft.to;
                            else if (requestCode == ActivityCompose.REQUEST_CONTACT_CC)
                                address = draft.cc;
                            else if (requestCode == ActivityCompose.REQUEST_CONTACT_BCC)
                                address = draft.bcc;

                            List<Address> list = new ArrayList<>();
                            if (address != null)
                                list.addAll(Arrays.asList(address));

                            list.add(new InternetAddress(email, name));

                            if (requestCode == ActivityCompose.REQUEST_CONTACT_TO)
                                draft.to = list.toArray(new Address[0]);
                            else if (requestCode == ActivityCompose.REQUEST_CONTACT_CC)
                                draft.cc = list.toArray(new Address[0]);
                            else if (requestCode == ActivityCompose.REQUEST_CONTACT_BCC)
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
                Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
            }
        }.execute(this, args, "compose:picked");
    }

    private void handleAddAttachment(Uri uri, final boolean image) {
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

                onAction(R.id.action_save);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                // External app sending absolute file
                if (ex instanceof SecurityException)
                    handleFileShare();
                else
                    Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
            }
        }.execute(this, args, "compose:attachment:add");
    }

    private void handleExit() {
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
        if (!TextUtils.isEmpty(etExtra.getText().toString().trim()))
            return false;
        if (!etTo.getText().toString().trim().equals(etTo.getTag()))
            return false;
        if (!TextUtils.isEmpty(etCc.getText().toString().trim()))
            return false;
        if (!TextUtils.isEmpty(etBcc.getText().toString().trim()))
            return false;
        if (!etSubject.getText().toString().trim().equals(etSubject.getTag()))
            return false;
        if (!TextUtils.isEmpty(Jsoup.parse(HtmlHelper.toHtml(etBody.getText())).text().trim()))
            return false;
        if (rvAttachment.getAdapter().getItemCount() > 0)
            return false;
        return true;
    }

    private void onAction(int action) {
        EntityIdentity identity = (EntityIdentity) spIdentity.getSelectedItem();

        // Workaround underlines left by Android
        etBody.clearComposingText();

        Bundle args = new Bundle();
        args.putLong("id", working);
        args.putInt("action", action);
        args.putLong("account", identity == null ? -1 : identity.account);
        args.putLong("identity", identity == null ? -1 : identity.id);
        args.putString("extra", etExtra.getText().toString().trim());
        args.putString("to", etTo.getText().toString().trim());
        args.putString("cc", etCc.getText().toString().trim());
        args.putString("bcc", etBcc.getText().toString().trim());
        args.putString("subject", etSubject.getText().toString().trim());
        args.putString("body", HtmlHelper.toHtml(etBody.getText()));
        args.putBoolean("plain_only", plain_only);
        args.putBoolean("empty", isEmpty());

        Log.i("Run execute id=" + working);
        actionLoader.execute(this, args, "compose:action:" + action);
    }

    private static EntityAttachment addAttachment(Context context, long id, Uri uri,
                                                  boolean image) throws IOException {
        Log.w("Add attachment uri=" + uri);

        if ("file".equals(uri.getScheme()) &&
                !Helper.hasPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Log.w("Add attachment uri=" + uri);
            throw new SecurityException();
        }

        EntityAttachment attachment = new EntityAttachment();

        String name = uri.getLastPathSegment();
        String s = null;

        try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                name = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                s = cursor.getString(cursor.getColumnIndex(OpenableColumns.SIZE));
            }

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
                attachment.type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
            if (attachment.type == null)
                attachment.type = "application/octet-stream";
            if (image)
                attachment.disposition = Part.INLINE;

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
                os = new BufferedOutputStream(new FileOutputStream(file));

                byte[] buffer = new byte[MessageHelper.ATTACHMENT_BUFFER_SIZE];
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

            if ("eu.faircode.email".equals(uri.getAuthority())) {
                // content://eu.faircode.email/temporary/nnn.jpg
                File tmp = new File(context.getFilesDir(), uri.getPath());
                Log.i("Deleting " + tmp);
                if (!tmp.delete())
                    Log.w("Error deleting " + tmp);
            } else
                Log.i("Authority=" + uri.getAuthority());

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            boolean autoresize = prefs.getBoolean("autoresize", true);

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

                Log.i("Image type=" + attachment.type + " rotation=" + getImageRotation(file));
                Matrix rotation = ("image/jpeg".equals(attachment.type) ? getImageRotation(file) : null);

                if (factor > 1 || rotation != null) {
                    options.inJustDecodeBounds = false;
                    options.inSampleSize = factor;

                    Bitmap scaled = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
                    if (scaled != null) {
                        Log.i("Image target size=" + scaled.getWidth() + "x" + scaled.getHeight() + " rotation=" + rotation);

                        if (rotation != null) {
                            Bitmap rotated = Bitmap.createBitmap(scaled, 0, 0, scaled.getWidth(), scaled.getHeight(), rotation, true);
                            scaled.recycle();
                            scaled = rotated;
                        }

                        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
                            scaled.compress("image/jpeg".equals(attachment.type)
                                            ? Bitmap.CompressFormat.JPEG
                                            : Bitmap.CompressFormat.PNG,
                                    REDUCED_IMAGE_QUALITY, out);
                        } finally {
                            scaled.recycle();
                        }

                        size = file.length();
                    }
                }
            }

            db.attachment().setDownloaded(attachment.id, size);

        } catch (IOException ex) {
            // Reset progress on failure
            db.attachment().setProgress(attachment.id, null);
            throw ex;
        }

        return attachment;
    }

    private static Matrix getImageRotation(File file) throws IOException {
        ExifInterface exif = new ExifInterface(file.getAbsolutePath());
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return null;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                return matrix;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                return matrix;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                return matrix;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                return matrix;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                return matrix;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                return matrix;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                return matrix;
            default:
                return null;
        }
    }

    private SimpleTask<EntityMessage> draftLoader = new SimpleTask<EntityMessage>() {
        @Override
        protected EntityMessage onExecute(Context context, Bundle args) throws Throwable {
            String action = args.getString("action");
            long id = args.getLong("id", -1);
            long reference = args.getLong("reference", -1);
            File ics = (File) args.getSerializable("ics");
            long answer = args.getLong("answer", -1);

            Log.i("Load draft action=" + action + " id=" + id + " reference=" + reference);

            EntityMessage draft;

            DB db = DB.getInstance(context);
            try {
                db.beginTransaction();

                draft = db.message().getMessage(id);
                if (draft == null || draft.ui_hide) {
                    // New draft
                    if ("edit".equals(action))
                        throw new MessageRemovedException("Draft for edit was deleted hide=" + (draft != null));

                    EntityFolder drafts;
                    EntityMessage ref = db.message().getMessage(reference);
                    if (ref == null) {
                        long aid = args.getLong("account", -1);
                        if (aid < 0)
                            drafts = db.folder().getPrimaryDrafts();
                        else {
                            drafts = db.folder().getFolderByType(aid, EntityFolder.DRAFTS);
                            if (drafts == null)
                                drafts = db.folder().getPrimaryDrafts();
                        }
                        if (drafts == null)
                            throw new IllegalArgumentException(context.getString(R.string.title_no_primary_drafts));
                    } else {
                        drafts = db.folder().getFolderByType(ref.account, EntityFolder.DRAFTS);
                        if (drafts == null)
                            drafts = db.folder().getPrimaryDrafts();
                        if (drafts == null)
                            throw new IllegalArgumentException(context.getString(R.string.title_no_primary_drafts));
                    }

                    String body = "";

                    draft = new EntityMessage();
                    draft.account = drafts.account;
                    draft.folder = drafts.id;
                    draft.msgid = EntityMessage.generateMessageId();

                    if (ref == null) {
                        draft.thread = draft.msgid;

                        try {
                            String to = args.getString("to");
                            draft.to = (TextUtils.isEmpty(to) ? null : InternetAddress.parse(to));
                        } catch (AddressException ex) {
                            Log.w(ex);
                        }

                        try {
                            String cc = args.getString("cc");
                            draft.cc = (TextUtils.isEmpty(cc) ? null : InternetAddress.parse(cc));
                        } catch (AddressException ex) {
                            Log.w(ex);
                        }

                        try {
                            String bcc = args.getString("bcc");
                            draft.bcc = (TextUtils.isEmpty(bcc) ? null : InternetAddress.parse(bcc));
                        } catch (AddressException ex) {
                            Log.w(ex);
                        }

                        draft.subject = args.getString("subject", "");
                        body = args.getString("body", "");
                        body = body.replaceAll("\\r?\\n", "<br />");

                        if (answer > 0)
                            body = EntityAnswer.getAnswerText(db, answer, null) + body;
                    } else {
                        if ("reply".equals(action) || "reply_all".equals(action) ||
                                "list".equals(action) || "receipt".equals(action) ||
                                "participation".equals(action)) {
                            if (ref.to != null && ref.to.length > 0) {
                                String to = ((InternetAddress) ref.to[0]).getAddress();
                                int at = to.indexOf('@');
                                if (at > 0)
                                    draft.extra = to.substring(0, at);
                            }

                            draft.references = (ref.references == null ? "" : ref.references + " ") + ref.msgid;
                            draft.inreplyto = ref.msgid;
                            draft.thread = ref.thread;

                            String via = null;
                            if (ref.identity != null) {
                                EntityIdentity identity = db.identity().getIdentity(ref.identity);
                                draft.from = new Address[]{new InternetAddress(identity.email, identity.name)};
                                via = MessageHelper.canonicalAddress(identity.email);
                            }

                            if ("list".equals(action) && ref.list_post != null)
                                draft.to = ref.list_post;
                            else if ("receipt".equals(action) && ref.receipt_to != null)
                                draft.to = ref.receipt_to;
                            else {
                                // Prevent replying to self
                                if (ref.replySelf(via)) {
                                    draft.to = ref.to;
                                    draft.from = ref.from;
                                } else
                                    draft.to = (ref.reply == null || ref.reply.length == 0 ? ref.from : ref.reply);
                            }

                            if ("reply_all".equals(action))
                                draft.cc = ref.getAllRecipients(via);
                            else if ("receipt".equals(action))
                                draft.receipt_request = true;

                        } else if ("forward".equals(action))
                            draft.thread = draft.msgid; // new thread

                        String subject = (ref.subject == null ? "" : ref.subject);
                        if ("reply".equals(action) || "reply_all".equals(action) ||
                                "participation".equals(action)) {
                            String re = context.getString(R.string.title_subject_reply, "");
                            if (!prefix_once || !subject.startsWith(re))
                                draft.subject = context.getString(R.string.title_subject_reply, subject);
                            else
                                draft.subject = ref.subject;
                        } else if ("list".equals(action)) {
                            draft.subject = ref.subject;
                        } else if ("receipt".equals(action)) {
                            draft.subject = context.getString(R.string.title_receipt_subject, subject);

                            Configuration configuration = new Configuration(context.getResources().getConfiguration());
                            configuration.setLocale(new Locale("en"));
                            Resources res = context.createConfigurationContext(configuration).getResources();

                            body = "<p>" + context.getString(R.string.title_receipt_text) + "</p>";
                            if (!Locale.getDefault().getLanguage().equals("en"))
                                body += "<p>" + res.getString(R.string.title_receipt_text) + "</p>";
                        } else if ("forward".equals(action)) {
                            String fwd = context.getString(R.string.title_subject_forward, "");
                            if (!prefix_once || !subject.startsWith(fwd.trim()))
                                draft.subject = context.getString(R.string.title_subject_forward, subject);
                            else
                                draft.subject = ref.subject;
                        }

                        draft.plain_only = ref.plain_only;
                        if (answer > 0)
                            body = EntityAnswer.getAnswerText(db, answer, draft.to) + body;

                        EntityOperation.queue(context, ref, EntityOperation.SEEN, true);
                    }

                    // Select identity matching from address
                    int icount = 0;
                    EntityIdentity first = null;
                    EntityIdentity primary = null;
                    List<TupleIdentityEx> identities = db.identity().getComposableIdentities(null);

                    int iindex = -1;
                    do {
                        String from = null;
                        if (iindex >= 0)
                            from = MessageHelper.canonicalAddress(((InternetAddress) draft.from[iindex]).getAddress());
                        for (EntityIdentity identity : identities) {
                            String email = MessageHelper.canonicalAddress(identity.email);
                            if (email.equals(from)) {
                                draft.identity = identity.id;
                                draft.from = new InternetAddress[]{new InternetAddress(identity.email, identity.name)};
                                break;
                            }
                            if (identity.account.equals(draft.account)) {
                                icount++;
                                if (identity.primary)
                                    primary = identity;
                                if (first == null)
                                    first = identity;
                            }
                        }
                        if (draft.identity != null)
                            break;

                        iindex++;
                    } while (iindex < (draft.from == null ? -1 : draft.from.length));

                    // Select identity
                    if (draft.identity == null) {
                        if (primary != null) {
                            draft.identity = primary.id;
                            draft.from = new InternetAddress[]{new InternetAddress(primary.email, primary.name)};
                        } else if (first != null && icount == 1) {
                            draft.identity = first.id;
                            draft.from = new InternetAddress[]{new InternetAddress(first.email, first.name)};
                        }
                    }

                    draft.sender = MessageHelper.getSortKey(draft.from);
                    Uri lookupUri = ContactInfo.getLookupUri(context, draft.from);
                    draft.avatar = (lookupUri == null ? null : lookupUri.toString());

                    draft.received = new Date().getTime();
                    draft.seen = true;
                    draft.ui_seen = true;

                    draft.id = db.message().insertMessage(draft);
                    Helper.writeText(draft.getFile(context), body);

                    db.message().setMessageContent(draft.id,
                            true,
                            draft.plain_only,
                            HtmlHelper.getPreview(body),
                            null);

                    if ("participation".equals(action)) {
                        EntityAttachment attachment = new EntityAttachment();
                        attachment.message = draft.id;
                        attachment.sequence = 1;
                        attachment.name = ics.getName();
                        attachment.type = "text/calendar";
                        attachment.size = ics.length();
                        attachment.progress = null;
                        attachment.available = true;
                        attachment.id = db.attachment().insertAttachment(attachment);
                        ics.renameTo(attachment.getFile(context));
                    }

                    // Write reference text
                    if (ref != null && ref.content && !"list".equals(action) && !"receipt".equals(action)) {
                        String refBody = String.format("<p>%s %s:</p>\n<blockquote>%s</blockquote>",
                                Html.escapeHtml(new Date(ref.received).toString()),
                                Html.escapeHtml(MessageHelper.formatAddresses(ref.from)),
                                Helper.readText(ref.getFile(context)));
                        Helper.writeText(draft.getRefFile(context), refBody);
                    }

                    if ("new".equals(action)) {
                        ArrayList<Uri> uris = args.getParcelableArrayList("attachments");
                        if (uris != null)
                            for (Uri uri : uris)
                                addAttachment(context, draft.id, uri, false);
                    } else {
                        int sequence = 0;
                        List<EntityAttachment> attachments = db.attachment().getAttachments(ref.id);
                        for (EntityAttachment attachment : attachments)
                            if (attachment.encryption == null &&
                                    ("forward".equals(action) ||
                                            (attachment.isInline() && attachment.isImage()))) {
                                if (attachment.available) {
                                    File source = attachment.getFile(context);

                                    attachment.id = null;
                                    attachment.message = draft.id;
                                    attachment.sequence = ++sequence;
                                    attachment.id = db.attachment().insertAttachment(attachment);

                                    File target = attachment.getFile(context);
                                    Helper.copy(source, target);
                                } else
                                    args.putBoolean("incomplete", true);
                            }
                    }

                    EntityOperation.queue(context, draft, EntityOperation.ADD);
                } else {
                    if (!draft.content) {
                        if (draft.uid == null)
                            throw new IllegalStateException("Draft without uid");
                        EntityOperation.queue(context, draft, EntityOperation.BODY);
                    }

                    List<EntityAttachment> attachments = db.attachment().getAttachments(draft.id);
                    for (EntityAttachment attachment : attachments)
                        if (!attachment.available)
                            EntityOperation.queue(context, draft, EntityOperation.ATTACHMENT, attachment.id);
                }

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }

            return draft;
        }

        @Override
        protected void onExecuted(Bundle args, final EntityMessage draft) {
            working = draft.id;

            final String action = getArguments().getString("action");
            Log.i("Loaded draft id=" + draft.id + " action=" + action);

            etExtra.setText(draft.extra);
            etTo.setText(MessageHelper.formatAddressesCompose(draft.to));
            etCc.setText(MessageHelper.formatAddressesCompose(draft.cc));
            etBcc.setText(MessageHelper.formatAddressesCompose(draft.bcc));
            etSubject.setText(draft.subject);

            long reference = args.getLong("reference", -1);
            etTo.setTag(reference < 0 ? "" : etTo.getText().toString());
            etSubject.setTag(reference < 0 ? "" : etSubject.getText().toString());

            grpHeader.setVisibility(View.VISIBLE);
            grpAddresses.setVisibility("reply_all".equals(action) ? View.VISIBLE : View.GONE);

            bottom_navigation.getMenu().findItem(R.id.action_undo).setVisible(draft.revision != null && draft.revision > 1);
            bottom_navigation.getMenu().findItem(R.id.action_redo).setVisible(draft.revision != null && !draft.revision.equals(draft.revisions));

            plain_only = (draft.plain_only != null && draft.plain_only);
            getActivity().invalidateOptionsMenu();

            if (args.getBoolean("incomplete"))
                Snackbar.make(view, R.string.title_attachments_incomplete, Snackbar.LENGTH_LONG).show();

            new SimpleTask<List<TupleIdentityEx>>() {
                @Override
                protected List<TupleIdentityEx> onExecute(Context context, Bundle args) {
                    DB db = DB.getInstance(context);
                    List<TupleIdentityEx> identities = db.identity().getComposableIdentities(null);
                    if (identities == null)
                        identities = new ArrayList<>();

                    return identities;
                }

                @Override
                protected void onExecuted(Bundle args, List<TupleIdentityEx> identities) {
                    Log.i("Set identities=" + identities.size());

                    // Show identities
                    IdentityAdapter adapter = new IdentityAdapter(getContext(), identities);
                    spIdentity.setAdapter(adapter);

                    // Select identity
                    if (draft.identity != null)
                        for (int pos = 0; pos < identities.size(); pos++) {
                            if (identities.get(pos).id.equals(draft.identity)) {
                                spIdentity.setSelection(pos);
                                break;
                            }
                        }
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
                }
            }.execute(FragmentCompose.this, new Bundle(), "compose:identities");

            DB db = DB.getInstance(getContext());

            db.attachment().liveAttachments(draft.id).observe(getViewLifecycleOwner(),
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
                                if (attachment.progress != null) {
                                    downloading = true;
                                    break;
                                }
                            }

                            // Attachment deleted
                            if (available < last_available)
                                onAction(R.id.action_save);

                            last_available = available;

                            rvAttachment.setTag(downloading);
                            checkInternet();

                            checkDraft(draft.id);
                        }
                    });

            db.message().liveMessage(draft.id).observe(getViewLifecycleOwner(), new Observer<EntityMessage>() {
                @Override
                public void onChanged(EntityMessage draft) {
                    // Draft was deleted
                    if (draft == null || draft.ui_hide)
                        finish();
                    else {
                        tvNoInternet.setTag(draft.content);
                        checkInternet();

                        checkDraft(draft.id);
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
            else
                Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
        }
    };

    private void handleFileShare() {
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse(Helper.FAQ_URI + "#user-content-faq49"));
        boolean resolves = (intent.resolveActivity(getContext().getPackageManager()) != null);

        Snackbar sb = Snackbar.make(view,
                R.string.title_no_stream,
                resolves ? Snackbar.LENGTH_INDEFINITE : Snackbar.LENGTH_LONG);
        if (resolves)
            sb.setAction(R.string.title_info, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(intent);
                    finish();
                }
            });
        sb.show();
    }

    private SimpleTask<EntityMessage> actionLoader = new SimpleTask<EntityMessage>() {
        int last_available = 0;

        @Override
        protected void onPreExecute(Bundle args) {
            busy = true;
            Helper.setViewsEnabled(view, false);
            getActivity().invalidateOptionsMenu();
        }

        @Override
        protected void onPostExecute(Bundle args) {
            busy = false;
            Helper.setViewsEnabled(view, true);
            getActivity().invalidateOptionsMenu();
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
            boolean plain_only = args.getBoolean("plain_only");
            boolean empty = args.getBoolean("empty");

            EntityMessage draft;

            DB db = DB.getInstance(context);
            try {
                db.beginTransaction();

                // Get draft & selected identity
                draft = db.message().getMessage(id);
                EntityIdentity identity = db.identity().getIdentity(iid);

                // Draft deleted by server
                if (draft == null || draft.ui_hide)
                    throw new MessageRemovedException("Draft for action was deleted hide=" + (draft != null));

                Log.i("Load action id=" + draft.id + " action=" + getActionName(action));

                // Move draft to new account
                if (draft.account != aid && aid >= 0) {
                    Log.i("Account changed");
                    Long uid = draft.uid;
                    String msgid = draft.msgid;

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
                    EntityOperation.queue(context, draft, EntityOperation.DELETE); // by msgid

                    // Restore original with new account, no uid and new msgid
                    draft.id = id;
                    draft.account = aid;
                    draft.folder = db.folder().getFolderByType(aid, EntityFolder.DRAFTS).id;
                    draft.uid = null;
                    draft.msgid = EntityMessage.generateMessageId();
                    draft.content = true;
                    draft.ui_hide = false;
                    db.message().updateMessage(draft);
                    EntityOperation.queue(context, draft, EntityOperation.ADD);
                }

                List<EntityAttachment> attachments = db.attachment().getAttachments(draft.id);

                // Get data
                InternetAddress afrom[] = (identity == null ? null : new InternetAddress[]{new InternetAddress(identity.email, identity.name)});

                InternetAddress ato[] = null;
                InternetAddress acc[] = null;
                InternetAddress abcc[] = null;

                if (!TextUtils.isEmpty(to))
                    try {
                        ato = InternetAddress.parse(to);
                        if (action == R.id.action_send)
                            for (InternetAddress address : ato)
                                address.validate();
                    } catch (AddressException ex) {
                        throw new AddressException(context.getString(R.string.title_address_parse_error,
                                Helper.ellipsize(to, ADDRESS_ELLIPSIZE), ex.getMessage()));
                    }

                if (!TextUtils.isEmpty(cc))
                    try {
                        acc = InternetAddress.parse(cc);
                        if (action == R.id.action_send)
                            for (InternetAddress address : acc)
                                address.validate();
                    } catch (AddressException ex) {
                        throw new AddressException(context.getString(R.string.title_address_parse_error,
                                Helper.ellipsize(cc, ADDRESS_ELLIPSIZE), ex.getMessage()));
                    }

                if (!TextUtils.isEmpty(bcc))
                    try {
                        abcc = InternetAddress.parse(bcc);
                        if (action == R.id.action_send)
                            for (InternetAddress address : abcc)
                                address.validate();
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
                    draft.received = new Date().getTime();
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
                    String previous = Helper.readText(draft.getFile(context));
                    if (!body.equals(previous) ||
                            plain_only != (draft.plain_only != null && draft.plain_only)) {
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

                        draft.plain_only = plain_only;
                        db.message().setMessageContent(draft.id,
                                true,
                                draft.plain_only,
                                HtmlHelper.getPreview(body),
                                null);
                    }
                }

                // Remove unused inline images
                StringBuilder sb = new StringBuilder();
                sb.append(body);
                File rfile = draft.getRefFile(context);
                if (rfile.exists())
                    sb.append(Helper.readText(rfile));
                List<String> cids = new ArrayList<>();
                for (Element element : Jsoup.parse(sb.toString()).select("img")) {
                    String src = element.attr("src");
                    if (src.startsWith("cid:"))
                        cids.add("<" + src.substring(4) + ">");
                }

                for (EntityAttachment attachment : new ArrayList<>(attachments))
                    if (attachment.isInline() && attachment.cid != null && !cids.contains(attachment.cid)) {
                        Log.i("Removing unused inline attachment cid=" + attachment.cid);
                        db.attachment().deleteAttachment(attachment.id);
                    }

                // Execute action
                if (action == R.id.action_delete) {
                    EntityFolder trash = db.folder().getFolderByType(draft.account, EntityFolder.TRASH);
                    if (empty || trash == null)
                        EntityOperation.queue(context, draft, EntityOperation.DELETE);
                    else
                        EntityOperation.queue(context, draft, EntityOperation.MOVE, trash.id);

                    if (!empty) {
                        Handler handler = new Handler(context.getMainLooper());
                        handler.post(new Runnable() {
                            public void run() {
                                Toast.makeText(context, R.string.title_draft_deleted, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } else if (action == R.id.action_save ||
                        action == R.id.action_undo ||
                        action == R.id.action_redo ||
                        action == R.id.menu_encrypt) {
                    if (BuildConfig.DEBUG || dirty)
                        EntityOperation.queue(context, draft, EntityOperation.ADD);

                    Handler handler = new Handler(context.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(context, R.string.title_draft_saved, Toast.LENGTH_LONG).show();
                        }
                    });

                } else if (action == R.id.action_send) {
                    // Check data
                    if (draft.identity == null)
                        throw new IllegalArgumentException(context.getString(R.string.title_from_missing));

                    if (draft.to == null && draft.cc == null && draft.bcc == null)
                        throw new IllegalArgumentException(context.getString(R.string.title_to_missing));

                    // Save attachments
                    for (EntityAttachment attachment : attachments)
                        if (!attachment.available)
                            throw new IllegalArgumentException(context.getString(R.string.title_attachments_missing));

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
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
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
                        DateFormat df = SimpleDateFormat.getDateTimeInstance();
                        feedback = context.getString(R.string.title_queued_at, df.format(draft.ui_snoozed));
                    }

                    Handler handler = new Handler(context.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(context, feedback, Toast.LENGTH_LONG).show();
                        }
                    });
                }

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }

            if (action == R.id.action_send && draft.ui_snoozed != null) {
                Log.i("Delayed send id=" + draft.id + " at " + new Date(draft.ui_snoozed));
                EntityMessage.snooze(getContext(), draft.id, draft.ui_snoozed);
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

            } else if (action == R.id.menu_encrypt) {
                onEncrypt();

            } else if (action == R.id.action_send) {
                autosave = false;
                finish();
            }
        }

        @Override
        protected void onException(Bundle args, Throwable ex) {
            if (ex instanceof MessageRemovedException)
                finish();
            else if (ex instanceof IllegalArgumentException || ex instanceof AddressException)
                Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG).show();
            else
                Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
        }

        String getActionName(int id) {
            switch (id) {
                case R.id.action_delete:
                    return "delete";
                case R.id.action_undo:
                    return "undo";
                case R.id.action_redo:
                    return "redo";
                case R.id.action_save:
                    return "save";
                case R.id.menu_encrypt:
                    return "encrypt";
                case R.id.action_send:
                    return "send";
                default:
                    return Integer.toString(id);
            }
        }
    };

    private void checkDraft(long id) {
        Bundle args = new Bundle();
        args.putLong("id", id);

        new SimpleTask<EntityMessage>() {
            @Override
            protected EntityMessage onExecute(Context context, Bundle args) {
                long id = args.getLong("id");

                DB db = DB.getInstance(context);

                EntityMessage draft = db.message().getMessage(id);
                if (draft == null || !draft.content)
                    return null;

                List<EntityAttachment> attachments = db.attachment().getAttachments(id);
                for (EntityAttachment attachment : attachments)
                    if (!attachment.available)
                        return null;

                return draft;
            }

            @Override
            protected void onExecuted(Bundle args, EntityMessage draft) {
                if (draft != null && state == State.NONE)
                    showDraft(draft);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
            }
        }.execute(this, args, "compose:check");
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
                Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
            }
        }.execute(FragmentCompose.this, args, "compose:show");
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
                edit_bar.setVisibility(style ? View.VISIBLE : View.GONE);
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

                DB db = DB.getInstance(context);
                EntityMessage draft = db.message().getMessage(id);
                if (draft == null || !draft.content)
                    throw new IllegalArgumentException(context.getString(R.string.title_no_body));

                String body = Helper.readText(draft.getFile(context));
                Spanned spannedBody = HtmlHelper.fromHtml(body, cidGetter, null);

                Spanned spannedRef = null;
                File refFile = draft.getRefFile(context);
                if (refFile.exists()) {
                    String quote = HtmlHelper.sanitize(context, Helper.readText(refFile));
                    Spanned spannedQuote = HtmlHelper.fromHtml(quote,
                            new Html.ImageGetter() {
                                @Override
                                public Drawable getDrawable(String source) {
                                    Drawable image = HtmlHelper.decodeImage(source, id, show_images, tvReference);

                                    ConstraintLayout.LayoutParams params =
                                            (ConstraintLayout.LayoutParams) tvReference.getLayoutParams();
                                    float width = context.getResources().getDisplayMetrics().widthPixels
                                            - params.leftMargin - params.rightMargin;
                                    if (image.getIntrinsicWidth() > width) {
                                        float scale = width / image.getIntrinsicWidth();
                                        image.setBounds(0, 0,
                                                Math.round(image.getIntrinsicWidth() * scale),
                                                Math.round(image.getIntrinsicHeight() * scale));
                                    }

                                    return image;
                                }
                            },
                            null);

                    int colorPrimary = Helper.resolveColor(context, R.attr.colorPrimary);
                    SpannableStringBuilder builder = new SpannableStringBuilder(spannedQuote);
                    QuoteSpan[] quoteSpans = builder.getSpans(0, builder.length(), QuoteSpan.class);
                    for (QuoteSpan quoteSpan : quoteSpans) {
                        builder.setSpan(
                                new StyledQuoteSpan(colorPrimary),
                                builder.getSpanStart(quoteSpan),
                                builder.getSpanEnd(quoteSpan),
                                builder.getSpanFlags(quoteSpan));
                        builder.removeSpan(quoteSpan);
                    }

                    spannedRef = builder;
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

                boolean ref_has_images = args.getBoolean("ref_has_images");

                tvReference.setText(text[1]);
                grpReference.setVisibility(text[1] == null ? View.GONE : View.VISIBLE);
                ibReferenceEdit.setVisibility(text[1] == null ? View.GONE : View.VISIBLE);
                ibReferenceImages.setVisibility(ref_has_images && !show_images ? View.VISIBLE : View.GONE);

                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        if (TextUtils.isEmpty(etTo.getText().toString().trim()))
                            etTo.requestFocus();
                        else if (TextUtils.isEmpty(etSubject.getText().toString()))
                            etSubject.requestFocus();
                        else
                            etBody.requestFocus();
                    }
                });
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
            }
        }.execute(FragmentCompose.this, args, "compose:show");
    }

    private Html.ImageGetter cidGetter = new Html.ImageGetter() {
        @Override
        public Drawable getDrawable(final String source) {
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

                        new SimpleTask<Drawable>() {
                            @Override
                            protected Drawable onExecute(Context context, Bundle args) {
                                long id = args.getLong("id");
                                String cid = args.getString("cid");

                                DB db = DB.getInstance(context);
                                EntityAttachment attachment = db.attachment().getAttachment(id, cid);
                                if (attachment == null)
                                    return null;

                                File file = attachment.getFile(context);
                                return Drawable.createFromPath(file.getAbsolutePath());
                            }

                            @Override
                            protected void onExecuted(Bundle args, Drawable image) {
                                if (image == null)
                                    lld.setLevel(0); // broken
                                else {
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
                                Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
                            }
                        }.execute(FragmentCompose.this, args, "compose:cid:" + source);
                    }
                });
            } else
                lld.setLevel(1); // image place holder

            return lld;
        }
    };

    public class IdentityAdapter extends ArrayAdapter<TupleIdentityEx> {
        private Context context;
        private List<TupleIdentityEx> identities;

        IdentityAdapter(@NonNull Context context, List<TupleIdentityEx> identities) {
            super(context, 0, identities);
            this.context = context;
            this.identities = identities;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            return getLayout(position, convertView, parent, R.layout.spinner_item2);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getLayout(position, convertView, parent, R.layout.spinner_item2_dropdown);
        }

        View getLayout(int position, View convertView, ViewGroup parent, int resid) {
            View view = LayoutInflater.from(context).inflate(resid, parent, false);

            TupleIdentityEx identity = identities.get(position);

            TextView text1 = view.findViewById(android.R.id.text1);
            text1.setText(identity.accountName + "/" + identity.getDisplayName() + (identity.primary ? " ★" : ""));

            TextView text2 = view.findViewById(android.R.id.text2);
            text2.setText(identity.email);

            return view;
        }
    }

    private AdapterView.OnItemSelectedListener identitySelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            EntityIdentity identity = (EntityIdentity) parent.getAdapter().getItem(position);

            encrypt = (identity != null && identity.encrypt);
            getActivity().invalidateOptionsMenu();

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
            encrypt = false;
            getActivity().invalidateOptionsMenu();

            etExtra.setHint("");
            tvDomain.setText(null);

            tvSignature.setText(null);
            grpSignature.setVisibility(View.GONE);
        }
    };

    private ActionMode.Callback actionCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            menu.add(1, R.string.title_style_bold, 1, R.string.title_style_bold).setIcon(R.drawable.baseline_format_bold_24);
            menu.add(1, R.string.title_style_italic, 2, R.string.title_style_italic).setIcon(R.drawable.baseline_format_italic_24);
            menu.add(1, R.string.title_style_underline, 3, R.string.title_style_underline).setIcon(R.drawable.baseline_format_underlined_24);
            menu.add(1, R.string.title_style_size, 4, R.string.title_style_size).setIcon(R.drawable.baseline_format_size_24);
            menu.add(1, R.string.title_style_color, 5, R.string.title_style_color).setIcon(R.drawable.baseline_format_color_text_24);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            Log.i("Action=" + item.getGroupId() + ":" + item.getItemId());

            if (item.getGroupId() != 1)
                return false;

            int start = etBody.getSelectionStart();
            int end = etBody.getSelectionEnd();

            if (start < 0)
                start = 0;
            if (end < 0)
                end = 0;

            if (start > end) {
                int tmp = start;
                start = end;
                end = tmp;
            }

            final SpannableString ss = new SpannableString(etBody.getText());

            switch (item.getItemId()) {
                case R.string.title_style_bold:
                case R.string.title_style_italic: {
                    int style = (item.getItemId() == R.string.title_style_bold ? Typeface.BOLD : Typeface.ITALIC);
                    boolean has = false;
                    for (StyleSpan span : ss.getSpans(start, end, StyleSpan.class))
                        if (span.getStyle() == style) {
                            has = true;
                            ss.removeSpan(span);
                        }

                    if (!has)
                        ss.setSpan(new StyleSpan(style), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    etBody.setText(ss);
                    etBody.setSelection(end);
                    return true;
                }

                case R.string.title_style_underline: {
                    boolean has = false;
                    for (UnderlineSpan span : ss.getSpans(start, end, UnderlineSpan.class)) {
                        has = true;
                        ss.removeSpan(span);
                    }

                    if (!has)
                        ss.setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    etBody.setText(ss);
                    etBody.setSelection(end);
                    return true;
                }

                case R.string.title_style_size: {
                    RelativeSizeSpan[] spans = ss.getSpans(start, end, RelativeSizeSpan.class);
                    float size = (spans.length > 0 ? spans[0].getSizeChange() : 1.0f);

                    // Match small/big
                    if (size == 0.8f)
                        size = 1.0f;
                    else if (size == 1.0)
                        size = 1.25f;
                    else
                        size = 0.8f;

                    for (RelativeSizeSpan span : spans)
                        ss.removeSpan(span);

                    if (size != 1.0f)
                        ss.setSpan(new RelativeSizeSpan(size), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    etBody.setText(ss);
                    etBody.setSelection(end);
                    return true;
                }

                case R.string.title_style_color: {
                    final int s = start;
                    final int e = end;

                    ForegroundColorSpan[] spans = ss.getSpans(start, end, ForegroundColorSpan.class);
                    int color = (spans.length > 0 ? spans[0].getForegroundColor() : Color.TRANSPARENT);

                    int[] colors = getContext().getResources().getIntArray(R.array.colorPicker);
                    ColorPickerDialog colorPickerDialog = new ColorPickerDialog();
                    colorPickerDialog.initialize(R.string.title_style_color, colors, color, 4, colors.length);
                    colorPickerDialog.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener() {
                        @Override
                        public void onColorSelected(int color) {
                            for (ForegroundColorSpan span : ss.getSpans(s, e, ForegroundColorSpan.class))
                                ss.removeSpan(span);
                            ss.setSpan(new ForegroundColorSpan(color), s, e, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            etBody.setText(ss);
                            etBody.setSelection(e);
                        }
                    });
                    colorPickerDialog.show(getFragmentManager(), "colorpicker");

                    return true;
                }

                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
        }
    };

    private ViewTreeObserver.OnGlobalLayoutListener layoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            int bottom = view.getBottom()
                    - edit_bar.getHeight()
                    - Helper.dp2pixels(view.getContext(), 56); // full bottom navigation
            int remain = bottom - etBody.getTop();
            int threshold = Helper.dp2pixels(view.getContext(), 100);
            Log.i("Reduce remain=" + remain + " threshold=" + threshold);

            boolean reduce = (remain < threshold);
            boolean reduced = (bottom_navigation.getLabelVisibilityMode() == LabelVisibilityMode.LABEL_VISIBILITY_UNLABELED);
            if (reduce != reduced) {
                bottom_navigation.setLabelVisibilityMode(reduce
                        ? LabelVisibilityMode.LABEL_VISIBILITY_UNLABELED
                        : LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
                ViewGroup.LayoutParams params = bottom_navigation.getLayoutParams();
                params.height = Helper.dp2pixels(view.getContext(), reduce ? 36 : 56);
                bottom_navigation.setLayoutParams(params);
            }
        }
    };

    private ActivityBase.IBackPressedListener onBackPressedListener = new ActivityBase.IBackPressedListener() {
        @Override
        public boolean onBackPressed() {
            if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED))
                handleExit();
            return true;
        }
    };
}
