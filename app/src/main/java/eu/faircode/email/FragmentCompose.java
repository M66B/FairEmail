package eu.faircode.email;

/*
    This file is part of FairEmail.

    FairEmail is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    NetGuard is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with NetGuard.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2018 by Marcel Bokhorst (M66B)
*/

import android.Manifest;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.provider.OpenableColumns;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.mail.Address;
import javax.mail.MessageRemovedException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.ContextCompat;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.app.Activity.RESULT_OK;

public class FragmentCompose extends FragmentEx {
    private ViewGroup view;
    private Spinner spFrom;
    private ImageView ivIdentityAdd;
    private MultiAutoCompleteTextView etTo;
    private ImageView ivToAdd;
    private MultiAutoCompleteTextView etCc;
    private ImageView ivCcAdd;
    private MultiAutoCompleteTextView etBcc;
    private ImageView ivBccAdd;
    private EditText etSubject;
    private RecyclerView rvAttachment;
    private EditText etBody;
    private BottomNavigationView bottom_navigation;
    private ProgressBar pbWait;
    private Group grpHeader;
    private Group grpAddresses;
    private Group grpAttachments;
    private Group grpMessage;

    private AdapterAttachment adapter;

    private long working = -1;
    private boolean free = false;
    private boolean addresses;
    private boolean autosave = false;

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_compose);

        view = (ViewGroup) inflater.inflate(R.layout.fragment_compose, container, false);

        // Get controls
        spFrom = view.findViewById(R.id.spFrom);
        ivIdentityAdd = view.findViewById(R.id.ivIdentityAdd);
        etTo = view.findViewById(R.id.etTo);
        ivToAdd = view.findViewById(R.id.ivToAdd);
        etCc = view.findViewById(R.id.etCc);
        ivCcAdd = view.findViewById(R.id.ivCcAdd);
        etBcc = view.findViewById(R.id.etBcc);
        ivBccAdd = view.findViewById(R.id.ivBccAdd);
        etSubject = view.findViewById(R.id.etSubject);
        rvAttachment = view.findViewById(R.id.rvAttachment);
        etBody = view.findViewById(R.id.etBody);
        bottom_navigation = view.findViewById(R.id.bottom_navigation);
        pbWait = view.findViewById(R.id.pbWait);
        grpHeader = view.findViewById(R.id.grpHeader);
        grpAddresses = view.findViewById(R.id.grpAddresses);
        grpAttachments = view.findViewById(R.id.grpAttachments);
        grpMessage = view.findViewById(R.id.grpMessage);

        // Wire controls

        ivIdentityAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle args = new Bundle();
                args.putLong("id", -1);

                FragmentIdentity fragment = new FragmentIdentity();
                fragment.setArguments(args);

                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("identity");
                fragmentTransaction.commit();
            }
        });

        ivToAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Email.CONTENT_URI);
                startActivityForResult(intent, ActivityCompose.REQUEST_CONTACT_TO);
            }
        });

        ivCcAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Email.CONTENT_URI);
                startActivityForResult(intent, ActivityCompose.REQUEST_CONTACT_CC);
            }
        });

        ivBccAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Email.CONTENT_URI);
                startActivityForResult(intent, ActivityCompose.REQUEST_CONTACT_BCC);
            }
        });

        etBody.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                free = hasFocus;

                getActivity().invalidateOptionsMenu();

                grpHeader.setVisibility(hasFocus ? View.GONE : View.VISIBLE);
                if (hasFocus) {
                    addresses = (grpAddresses.getVisibility() != View.GONE);
                    grpAddresses.setVisibility(View.GONE);
                    grpAttachments.setVisibility(View.GONE);
                }
            }
        });

        etBody.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_BACK:
                            if (grpHeader.getVisibility() == View.GONE) {
                                free = false;

                                getActivity().invalidateOptionsMenu();
                                grpHeader.setVisibility(View.VISIBLE);
                                if (addresses)
                                    grpAddresses.setVisibility(View.VISIBLE);
                                if (rvAttachment.getAdapter().getItemCount() > 0)
                                    grpAttachments.setVisibility(View.VISIBLE);

                                new Handler().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        etTo.requestFocus();
                                    }
                                });

                                return true;
                            }
                    }
                }
                return false;
            }
        });

        bottom_navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int action = item.getItemId();
                if (action == R.id.action_delete) {
                    new DialogBuilderLifecycle(getContext(), getViewLifecycleOwner())
                            .setMessage(R.string.title_ask_delete)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    onAction(R.id.action_delete);
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, null)
                            .show();

                } else
                    onAction(action);
                return false;
            }
        });

        ((ActivityBase) getActivity()).addBackPressedListener(new ActivityBase.IBackPressedListener() {
            @Override
            public boolean onBackPressed() {
                handleExit();
                return true;
            }
        });

        setHasOptionsMenu(true);

        // Initialize
        grpHeader.setVisibility(View.GONE);
        grpAddresses.setVisibility(View.GONE);
        grpAttachments.setVisibility(View.GONE);
        grpMessage.setVisibility(View.GONE);
        pbWait.setVisibility(View.VISIBLE);

        getActivity().invalidateOptionsMenu();
        spFrom.setEnabled(false);
        Helper.setViewsEnabled(view, false);

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                    getContext(),
                    android.R.layout.simple_list_item_2,
                    null,
                    new String[]{
                            ContactsContract.Contacts.DISPLAY_NAME,
                            ContactsContract.CommonDataKinds.Email.DATA
                    },
                    new int[]{
                            android.R.id.text1,
                            android.R.id.text2
                    },
                    0);

            etTo.setAdapter(adapter);
            etCc.setAdapter(adapter);
            etBcc.setAdapter(adapter);

            etTo.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
            etCc.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
            etBcc.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

            adapter.setFilterQueryProvider(new FilterQueryProvider() {
                public Cursor runQuery(CharSequence typed) {
                    return getContext().getContentResolver().query(
                            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                            new String[]{
                                    ContactsContract.RawContacts._ID,
                                    ContactsContract.Contacts.DISPLAY_NAME,
                                    ContactsContract.CommonDataKinds.Email.DATA
                            },
                            ContactsContract.CommonDataKinds.Email.DATA + " <> ''" +
                                    " AND (" + ContactsContract.Contacts.DISPLAY_NAME + " LIKE '%" + typed + "%'" +
                                    " OR " + ContactsContract.CommonDataKinds.Email.DATA + " LIKE '%" + typed + "%')",
                            null,
                            "CASE WHEN " + ContactsContract.Contacts.DISPLAY_NAME + " NOT LIKE '%@%' THEN 0 ELSE 1 END" +
                                    ", " + ContactsContract.Contacts.DISPLAY_NAME +
                                    ", " + ContactsContract.CommonDataKinds.Email.DATA + " COLLATE NOCASE");
                }
            });

            adapter.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
                public CharSequence convertToString(Cursor cursor) {
                    int colName = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                    int colEmail = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);
                    String name = cursor.getString(colName);
                    String email = cursor.getString(colEmail);
                    StringBuilder sb = new StringBuilder();
                    if (name == null)
                        sb.append(email);
                    else {
                        sb.append(name.replace(",", "")).append(" ");
                        sb.append("<").append(email).append(">");
                    }
                    return sb.toString();
                }
            });
        }

        rvAttachment.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rvAttachment.setLayoutManager(llm);
        rvAttachment.setItemAnimator(null);

        adapter = new AdapterAttachment(getContext(), getViewLifecycleOwner(), false);
        rvAttachment.setAdapter(adapter);

        return view;
    }

    @Override
    public void onDestroyView() {
        adapter = null;
        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("working", working);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState == null) {
            if (working < 0) {
                Bundle args = new Bundle();
                args.putString("action", getArguments().getString("action"));
                args.putLong("id", getArguments().getLong("id", -1));
                args.putLong("account", getArguments().getLong("account", -1));
                args.putLong("reference", getArguments().getLong("reference", -1));
                args.putLong("answer", getArguments().getLong("answer", -1));
                args.putString("to", getArguments().getString("to"));
                args.putString("cc", getArguments().getString("cc"));
                args.putString("bcc", getArguments().getString("bcc"));
                args.putString("subject", getArguments().getString("subject"));
                args.putString("body", getArguments().getString("body"));
                args.putParcelableArrayList("attachments", getArguments().getParcelableArrayList("attachments"));
                draftLoader.load(this, args);
            } else {
                Bundle args = new Bundle();
                args.putString("action", "edit");
                args.putLong("id", working);
                args.putLong("account", -1);
                args.putLong("reference", -1);
                args.putLong("answer", -1);
                draftLoader.load(this, args);
            }
        } else {
            working = savedInstanceState.getLong("working");
            Bundle args = new Bundle();
            args.putString("action", working < 0 ? "new" : "edit");
            args.putLong("id", working);
            args.putLong("account", -1);
            args.putLong("reference", -1);
            args.putLong("answer", -1);
            draftLoader.load(this, args);
        }
    }

    @Override
    public void onPause() {
        if (autosave)
            onAction(R.id.action_save);
        super.onPause();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_compose, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.menu_bold).setVisible(free && working >= 0);
        menu.findItem(R.id.menu_italic).setVisible(free && working >= 0);
        menu.findItem(R.id.menu_link).setVisible(free && working >= 0);
        menu.findItem(R.id.menu_image).setVisible(free && working >= 0);
        menu.findItem(R.id.menu_attachment).setVisible(!free && working >= 0);
        menu.findItem(R.id.menu_attachment).setEnabled(etBody.isEnabled());
        menu.findItem(R.id.menu_addresses).setVisible(!free && working >= 0);

        PackageManager pm = getContext().getPackageManager();
        menu.findItem(R.id.menu_image).setEnabled(getImageIntent().resolveActivity(pm) != null);
        menu.findItem(R.id.menu_attachment).setEnabled(getAttachmentIntent().resolveActivity(pm) != null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED))
                    handleExit();
                return true;
            case R.id.menu_bold:
            case R.id.menu_italic:
            case R.id.menu_link:
                onMenuStyle(item.getItemId());
                return true;
            case R.id.menu_image:
                onMenuImage();
                return true;
            case R.id.menu_attachment:
                onMenuAttachment();
                return true;
            case R.id.menu_addresses:
                onMenuAddresses();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onMenuStyle(int id) {
        int start = etBody.getSelectionStart();
        int end = etBody.getSelectionEnd();
        if (start > end) {
            int tmp = start;
            start = end;
            end = tmp;
        }
        if (start != end) {
            SpannableString s = new SpannableString(etBody.getText());
            switch (id) {
                case R.id.menu_bold:
                    s.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    break;
                case R.id.menu_italic:
                    s.setSpan(new StyleSpan(Typeface.ITALIC), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    break;
                case R.id.menu_link:
                    Uri uri = null;
                    ClipboardManager cbm = getContext().getSystemService(ClipboardManager.class);
                    if (cbm.hasPrimaryClip()) {
                        String link = cbm.getPrimaryClip().getItemAt(0).coerceToText(getContext()).toString();
                        uri = Uri.parse(link);
                        if (uri.getScheme() == null)
                            uri = null;
                    }
                    if (uri == null)
                        Snackbar.make(view, R.string.title_clipboard_empty, Snackbar.LENGTH_LONG).show();
                    else
                        s.setSpan(new URLSpan(uri.toString()), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    break;
            }
            etBody.setText(s);
            etBody.setSelection(end);
        }
    }

    private void onMenuImage() {
        startActivityForResult(getImageIntent(), ActivityCompose.REQUEST_IMAGE);
    }

    private void onMenuAttachment() {
        startActivityForResult(getAttachmentIntent(), ActivityCompose.REQUEST_ATTACHMENT);
    }

    private void onMenuAddresses() {
        grpAddresses.setVisibility(grpAddresses.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(Helper.TAG, "Compose onActivityResult request=" + requestCode + " result=" + resultCode + " data=" + data);
        if (resultCode == RESULT_OK) {
            if (requestCode == ActivityCompose.REQUEST_IMAGE) {
                if (data != null)
                    handleAddAttachment(data, true);
            } else if (requestCode == ActivityCompose.REQUEST_ATTACHMENT) {
                if (data != null)
                    handleAddAttachment(data, false);
            } else {
                if (data != null)
                    handlePickContact(requestCode, data);
            }
        }
    }

    private Intent getImageIntent() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        return intent;
    }

    private Intent getAttachmentIntent() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        return intent;
    }

    private void handlePickContact(int requestCode, Intent data) {
        Cursor cursor = null;
        try {
            Uri uri = data.getData();
            if (uri != null)
                cursor = getContext().getContentResolver().query(uri,
                        new String[]{
                                ContactsContract.CommonDataKinds.Email.ADDRESS,
                                ContactsContract.Contacts.DISPLAY_NAME
                        },
                        null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int colEmail = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS);
                int colName = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                String email = cursor.getString(colEmail);
                String name = cursor.getString(colName);

                String text = null;
                if (requestCode == ActivityCompose.REQUEST_CONTACT_TO)
                    text = etTo.getText().toString();
                else if (requestCode == ActivityCompose.REQUEST_CONTACT_CC)
                    text = etCc.getText().toString();
                else if (requestCode == ActivityCompose.REQUEST_CONTACT_BCC)
                    text = etBcc.getText().toString();

                InternetAddress address = new InternetAddress(email, name);
                StringBuilder sb = new StringBuilder(text);
                sb.append(address.toString().replace(",", "")).append(", ");

                if (requestCode == ActivityCompose.REQUEST_CONTACT_TO)
                    etTo.setText(sb.toString());
                else if (requestCode == ActivityCompose.REQUEST_CONTACT_CC)
                    etCc.setText(sb.toString());
                else if (requestCode == ActivityCompose.REQUEST_CONTACT_BCC)
                    etBcc.setText(sb.toString());
            }
        } catch (Throwable ex) {
            Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
            Helper.unexpectedError(getContext(), ex);
        } finally {
            if (cursor != null)
                cursor.close();
        }
    }

    private void handleAddAttachment(Intent data, final boolean image) {
        Uri uri = data.getData();
        if (uri == null)
            return;

        Bundle args = new Bundle();
        args.putLong("id", working);
        args.putParcelable("uri", data.getData());

        new SimpleTask<EntityAttachment>() {
            @Override
            protected EntityAttachment onLoad(Context context, Bundle args) throws IOException {
                Long id = args.getLong("id");
                Uri uri = args.getParcelable("uri");
                return addAttachment(context, id, uri, image);
            }

            @Override
            protected void onLoaded(Bundle args, EntityAttachment attachment) {
                if (image) {
                    File file = EntityAttachment.getFile(getContext(), attachment.id);
                    Drawable d = Drawable.createFromPath(file.getAbsolutePath());
                    d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());

                    int start = etBody.getSelectionStart();
                    etBody.getText().insert(start, " ");
                    SpannableString s = new SpannableString(etBody.getText());
                    ImageSpan is = new ImageSpan(getContext(), Uri.parse("cid:" + BuildConfig.APPLICATION_ID + "." + attachment.id), ImageSpan.ALIGN_BASELINE);
                    s.setSpan(is, start, start + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    String html = Html.toHtml(s);
                    Log.i(Helper.TAG, "html=" + html);

                    etBody.setText(Html.fromHtml(html, cidGetter, null));
                }
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(getContext(), ex);
            }
        }.load(this, args);
    }

    private void handleExit() {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED))
            new DialogBuilderLifecycle(getContext(), getViewLifecycleOwner())
                    .setMessage(R.string.title_ask_delete)
                    .setPositiveButton(R.string.title_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            onAction(R.id.action_delete);
                        }
                    })
                    .setNegativeButton(R.string.title_no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .show();
    }

    private void onAction(int action) {
        Helper.setViewsEnabled(view, false);
        getActivity().invalidateOptionsMenu();

        EntityIdentity identity = (EntityIdentity) spFrom.getSelectedItem();

        Bundle args = new Bundle();
        args.putLong("id", working);
        args.putInt("action", action);
        args.putLong("identity", identity == null ? -1 : identity.id);
        args.putString("to", etTo.getText().toString());
        args.putString("cc", etCc.getText().toString());
        args.putString("bcc", etBcc.getText().toString());
        args.putString("subject", etSubject.getText().toString());

        Spannable spannable = etBody.getText();
        UnderlineSpan[] uspans = spannable.getSpans(0, spannable.length(), UnderlineSpan.class);
        for (UnderlineSpan uspan : uspans)
            spannable.removeSpan(uspan);
        args.putString("body", Html.toHtml(spannable));

        Log.i(Helper.TAG, "Run load id=" + working);
        actionLoader.load(this, args);
    }

    private static EntityAttachment addAttachment(Context context, long id, Uri uri, boolean image) throws IOException {
        EntityAttachment attachment = new EntityAttachment();

        String name = null;
        String s = null;

        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                name = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                s = cursor.getString(cursor.getColumnIndex(OpenableColumns.SIZE));
            }

        } finally {
            if (cursor != null)
                cursor.close();
        }

        DB db = DB.getInstance(context);
        try {
            db.beginTransaction();

            EntityMessage draft = db.message().getMessage(id);
            Log.i(Helper.TAG, "Attaching to id=" + id);

            attachment.message = draft.id;
            attachment.sequence = db.attachment().getAttachmentCount(draft.id) + 1;
            attachment.name = name;

            String extension = Helper.getExtension(attachment.name);
            if (extension != null)
                attachment.type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
            if (attachment.type == null)
                attachment.type = "application/octet-stream";

            attachment.size = (s == null ? null : Integer.parseInt(s));
            attachment.progress = 0;

            attachment.id = db.attachment().insertAttachment(attachment);
            Log.i(Helper.TAG, "Created attachment=" + attachment.name + ":" + attachment.sequence + " type=" + attachment.type);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        try {
            File file = EntityAttachment.getFile(context, attachment.id);

            InputStream is = null;
            OutputStream os = null;
            try {
                is = context.getContentResolver().openInputStream(uri);
                os = new BufferedOutputStream(new FileOutputStream(file));

                int size = 0;
                byte[] buffer = new byte[EntityAttachment.ATTACHMENT_BUFFER_SIZE];
                for (int len = is.read(buffer); len != -1; len = is.read(buffer)) {
                    size += len;
                    os.write(buffer, 0, len);

                    // Update progress
                    if (attachment.size != null)
                        db.attachment().setProgress(attachment.id, size * 100 / attachment.size);
                }

                if (image)
                    attachment.cid = "<" + BuildConfig.APPLICATION_ID + "." + attachment.id + ">";

                attachment.size = size;
                attachment.progress = null;
                attachment.available = true;
                db.attachment().updateAttachment(attachment);
            } finally {
                try {
                    if (is != null)
                        is.close();
                } finally {
                    if (os != null)
                        os.close();
                }
            }
        } catch (IOException ex) {
            // Reset progress on failure
            attachment.progress = null;
            db.attachment().updateAttachment(attachment);
            throw ex;
        }

        return attachment;
    }

    private SimpleTask<EntityMessage> draftLoader = new SimpleTask<EntityMessage>() {
        @Override
        protected EntityMessage onLoad(Context context, Bundle args) throws IOException {
            String action = args.getString("action");
            long id = args.getLong("id", -1);
            long reference = args.getLong("reference", -1);
            long answer = args.getLong("answer", -1);
            boolean pro = Helper.isPro(getContext());

            Log.i(Helper.TAG, "Load draft action=" + action + " id=" + id + " reference=" + reference);

            EntityMessage draft;

            DB db = DB.getInstance(context);
            try {
                db.beginTransaction();

                draft = db.message().getMessage(id);
                if (draft == null || draft.ui_hide) {
                    if ("edit".equals(action))
                        throw new IllegalStateException("Message to edit not found");
                } else
                    return draft;

                EntityAccount account;
                EntityMessage ref = db.message().getMessage(reference);
                if (ref == null) {
                    long aid = args.getLong("account", -1);
                    if (aid < 0) {
                        account = db.account().getPrimaryAccount();
                        if (account == null)
                            throw new IllegalArgumentException(context.getString(R.string.title_no_account));
                    } else
                        account = db.account().getAccount(aid);
                } else {
                    account = db.account().getAccount(ref.account);

                    // Reply to recipient, not to known self
                    List<EntityIdentity> identities = db.identity().getIdentities();

                    if (ref.reply != null && ref.reply.length > 0) {
                        String reply = Helper.canonicalAddress(((InternetAddress) ref.reply[0]).getAddress());
                        for (EntityIdentity identity : identities) {
                            String email = Helper.canonicalAddress(identity.email);
                            if (reply.equals(email)) {
                                ref.reply = null;
                                break;
                            }
                        }
                    }

                    if (ref.deliveredto != null && (ref.to == null || ref.to.length == 0)) {
                        try {
                            Log.i(Helper.TAG, "Setting delivered to=" + ref.deliveredto);
                            ref.to = InternetAddress.parse(ref.deliveredto);
                        } catch (AddressException ex) {
                            Log.w(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                        }
                    }

                    if (ref.from != null && ref.from.length > 0) {
                        String from = Helper.canonicalAddress(((InternetAddress) ref.from[0]).getAddress());
                        Log.i(Helper.TAG, "From=" + from + " to=" + MessageHelper.getFormattedAddresses(ref.to, false));
                        for (EntityIdentity identity : identities) {
                            String email = Helper.canonicalAddress(identity.email);
                            if (from.equals(email)) {
                                Log.i(Helper.TAG, "Swapping from/to");
                                Address[] tmp = ref.to;
                                ref.to = ref.from;
                                ref.from = tmp;
                                break;
                            }
                        }
                    }
                }

                EntityFolder drafts;
                drafts = db.folder().getFolderByType(account.id, EntityFolder.DRAFTS);
                if (drafts == null)
                    drafts = db.folder().getPrimaryDrafts();
                if (drafts == null)
                    throw new IllegalArgumentException("no drafts folder");

                String body = "";

                draft = new EntityMessage();
                draft.account = account.id;
                draft.folder = drafts.id;
                draft.msgid = EntityMessage.generateMessageId();

                if (ref == null) {
                    try {
                        String to = args.getString("to");
                        draft.to = (TextUtils.isEmpty(to) ? null : InternetAddress.parse(to));
                    } catch (AddressException ex) {
                        Log.w(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                    }

                    try {
                        String cc = args.getString("cc");
                        draft.cc = (TextUtils.isEmpty(cc) ? null : InternetAddress.parse(cc));
                    } catch (AddressException ex) {
                        Log.w(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                    }

                    try {
                        String bcc = args.getString("bcc");
                        draft.bcc = (TextUtils.isEmpty(bcc) ? null : InternetAddress.parse(bcc));
                    } catch (AddressException ex) {
                        Log.w(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                    }

                    draft.subject = args.getString("subject");
                    body = args.getString("body");
                    if (body == null)
                        body = "";
                    else
                        body = body.replaceAll("\\r?\\n", "<br />");

                    if (pro && !TextUtils.isEmpty(account.signature))
                        body += account.signature;

                } else {
                    draft.thread = ref.thread;

                    if ("reply".equals(action) || "reply_all".equals(action)) {
                        draft.replying = ref.id;
                        draft.to = (ref.reply == null || ref.reply.length == 0 ? ref.from : ref.reply);
                        draft.from = ref.to;

                        if ("reply_all".equals(action)) {
                            List<Address> addresses = new ArrayList<>();
                            if (ref.to != null)
                                addresses.addAll(Arrays.asList(ref.to));
                            if (ref.cc != null)
                                addresses.addAll(Arrays.asList(ref.cc));
                            List<EntityIdentity> identities = db.identity().getIdentities();
                            for (Address address : new ArrayList<>(addresses)) {
                                String cc = Helper.canonicalAddress(((InternetAddress) address).getAddress());
                                for (EntityIdentity identity : identities) {
                                    String email = Helper.canonicalAddress(identity.email);
                                    if (cc.equals(email))
                                        addresses.remove(address);
                                }
                            }
                            draft.cc = addresses.toArray(new Address[0]);
                        }

                    } else if ("forward".equals(action)) {
                        //msg.replying = ref.id;
                        draft.from = ref.to;
                    }

                    if ("reply".equals(action) || "reply_all".equals(action)) {
                        draft.subject = context.getString(R.string.title_subject_reply, ref.subject);
                        body = String.format("<p>%s %s:</p><blockquote>%s</blockquote>",
                                Html.escapeHtml(new Date().toString()),
                                Html.escapeHtml(MessageHelper.getFormattedAddresses(draft.to, true)),
                                HtmlHelper.sanitize(ref.read(context)));
                    } else if ("forward".equals(action)) {
                        draft.subject = context.getString(R.string.title_subject_forward, ref.subject);
                        body = String.format("<p>%s %s:</p><blockquote>%s</blockquote>",
                                Html.escapeHtml(new Date().toString()),
                                Html.escapeHtml(MessageHelper.getFormattedAddresses(ref.from, true)),
                                HtmlHelper.sanitize(ref.read(context)));
                    }

                    if (pro && !TextUtils.isEmpty(account.signature))
                        body = account.signature + body;

                    if (answer > 0 && ("reply".equals(action) || "reply_all".equals(action))) {
                        String text = db.answer().getAnswer(answer).text;

                        String name = null;
                        String email = null;
                        if (draft.to != null && draft.to.length > 0) {
                            name = ((InternetAddress) draft.to[0]).getPersonal();
                            email = ((InternetAddress) draft.to[0]).getAddress();
                        }
                        text = text.replace("$name$", name == null ? "" : name);
                        text = text.replace("$email$", email == null ? "" : email);

                        body = text + body;
                    } else
                        body = "<br><br>" + body;
                }

                draft.content = true;
                draft.received = new Date().getTime();
                draft.seen = false;
                draft.ui_seen = false;
                draft.flagged = false;
                draft.ui_flagged = false;
                draft.ui_hide = false;
                draft.ui_found = false;
                draft.ui_ignored = false;

                draft.id = db.message().insertMessage(draft);
                draft.write(context, body == null ? "" : body);

                if ("new".equals(action)) {
                    ArrayList<Uri> uris = args.getParcelableArrayList("attachments");
                    if (uris != null)
                        for (Uri uri : uris)
                            addAttachment(context, draft.id, uri, false);
                } else if ("forward".equals(action)) {
                    int sequence = 0;
                    List<EntityAttachment> attachments = db.attachment().getAttachments(ref.id);
                    for (EntityAttachment attachment : attachments)
                        if (attachment.available) {
                            EntityAttachment copy = new EntityAttachment();
                            copy.message = draft.id;
                            copy.sequence = ++sequence;
                            copy.name = attachment.name;
                            copy.type = attachment.type;
                            copy.cid = attachment.cid;
                            copy.size = attachment.size;
                            copy.progress = attachment.progress;
                            copy.available = attachment.available;
                            copy.id = db.attachment().insertAttachment(copy);

                            File source = EntityAttachment.getFile(context, attachment.id);
                            File target = EntityAttachment.getFile(context, copy.id);
                            Helper.copy(source, target);
                        }
                }

                EntityOperation.queue(db, draft, EntityOperation.ADD);

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }

            EntityOperation.process(context);

            return draft;
        }

        @Override
        protected void onLoaded(Bundle args, final EntityMessage draft) {
            working = draft.id;
            autosave = true;

            final String action = getArguments().getString("action");
            Log.i(Helper.TAG, "Loaded draft id=" + draft.id + " action=" + action);

            etTo.setText(MessageHelper.getFormattedAddresses(draft.to, true));
            etCc.setText(MessageHelper.getFormattedAddresses(draft.cc, true));
            etBcc.setText(MessageHelper.getFormattedAddresses(draft.bcc, true));
            etSubject.setText(draft.subject);

            etBody.setText(null);

            Bundle a = new Bundle();
            a.putLong("id", draft.id);

            new SimpleTask<Spanned>() {
                @Override
                protected Spanned onLoad(final Context context, Bundle args) throws Throwable {
                    final long id = args.getLong("id");
                    String body = EntityMessage.read(context, id);
                    return Html.fromHtml(body, cidGetter, null);
                }

                @Override
                protected void onLoaded(Bundle args, Spanned body) {
                    getActivity().invalidateOptionsMenu();
                    etBody.setText(body);
                    etBody.setSelection(0);
                }
            }.load(FragmentCompose.this, a);

            getActivity().invalidateOptionsMenu();
            Helper.setViewsEnabled(view, true);

            pbWait.setVisibility(View.GONE);
            grpHeader.setVisibility(View.VISIBLE);
            grpAddresses.setVisibility("reply_all".equals(action) ? View.VISIBLE : View.GONE);
            grpMessage.setVisibility(View.VISIBLE);

            DB db = DB.getInstance(getContext());

            db.identity().liveIdentities(true).removeObservers(getViewLifecycleOwner());
            db.identity().liveIdentities(true).observe(getViewLifecycleOwner(), new Observer<List<EntityIdentity>>() {
                @Override
                public void onChanged(@Nullable List<EntityIdentity> identities) {
                    if (identities == null)
                        identities = new ArrayList<>();

                    Log.i(Helper.TAG, "Set identities=" + identities.size());

                    // Sort identities
                    Collections.sort(identities, new Comparator<EntityIdentity>() {
                        @Override
                        public int compare(EntityIdentity i1, EntityIdentity i2) {
                            return i1.name.compareTo(i2.name);
                        }
                    });

                    // Show identities
                    IdentityAdapter adapter = new IdentityAdapter(getContext(), identities);
                    adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                    spFrom.setAdapter(adapter);

                    boolean found = false;

                    // Select earlier selected identity
                    if (draft.identity != null)
                        for (int pos = 0; pos < identities.size(); pos++) {
                            if (identities.get(pos).id.equals(draft.identity)) {
                                spFrom.setSelection(pos);
                                found = true;
                                break;
                            }
                        }

                    // Select identity matching from address
                    if (!found && draft.from != null && draft.from.length > 0) {
                        String from = Helper.canonicalAddress(((InternetAddress) draft.from[0]).getAddress());
                        for (int pos = 0; pos < identities.size(); pos++) {
                            String email = Helper.canonicalAddress(identities.get(pos).email);
                            if (email.equals(from)) {
                                spFrom.setSelection(pos);
                                found = true;
                                break;
                            }
                        }
                    }

                    // Select primary identity
                    if (!found)
                        for (int pos = 0; pos < identities.size(); pos++)
                            if (identities.get(pos).primary) {
                                spFrom.setSelection(pos);
                                break;
                            }

                    spFrom.setEnabled(true);
                }
            });

            db.attachment().liveAttachments(draft.id).removeObservers(getViewLifecycleOwner());
            db.attachment().liveAttachments(draft.id).observe(getViewLifecycleOwner(),
                    new Observer<List<EntityAttachment>>() {
                        @Override
                        public void onChanged(@Nullable List<EntityAttachment> attachments) {
                            if (attachments == null)
                                attachments = new ArrayList<>();

                            adapter.set(attachments);
                            grpAttachments.setVisibility(!free && attachments.size() > 0 ? View.VISIBLE : View.GONE);
                        }
                    });

            db.message().liveMessage(draft.id).removeObservers(getViewLifecycleOwner());
            db.message().liveMessage(draft.id).observe(getViewLifecycleOwner(), new Observer<EntityMessage>() {
                @Override
                public void onChanged(final EntityMessage draft) {
                    // Draft was deleted
                    if (draft == null || draft.ui_hide) {
                        finish();
                        return;
                    }
                }
            });
        }

        @Override
        protected void onException(Bundle args, Throwable ex) {
            Helper.unexpectedError(getContext(), ex);
        }
    };

    private SimpleTask<EntityMessage> actionLoader = new SimpleTask<EntityMessage>() {
        @Override
        protected EntityMessage onLoad(final Context context, Bundle args) throws Throwable {
            // Get data
            long id = args.getLong("id");
            int action = args.getInt("action");
            long iid = args.getLong("identity");
            String to = args.getString("to");
            String cc = args.getString("cc");
            String bcc = args.getString("bcc");
            String subject = args.getString("subject");
            String body = args.getString("body");

            EntityMessage draft;

            DB db = DB.getInstance(context);
            try {
                db.beginTransaction();

                // Get draft & selected identity
                draft = db.message().getMessage(id);
                EntityIdentity identity = db.identity().getIdentity(iid);

                // Draft deleted by server
                if (draft == null)
                    throw new MessageRemovedException("Draft for action was deleted");

                Log.i(Helper.TAG, "Load action id=" + draft.id + " action=" + action);

                // Convert data
                InternetAddress afrom[] = (identity == null ? null : new InternetAddress[]{new InternetAddress(identity.email, identity.name)});
                InternetAddress ato[] = (TextUtils.isEmpty(to) ? null : InternetAddress.parse(to));
                InternetAddress acc[] = (TextUtils.isEmpty(cc) ? null : InternetAddress.parse(cc));
                InternetAddress abcc[] = (TextUtils.isEmpty(bcc) ? null : InternetAddress.parse(bcc));

                // Update draft
                draft.identity = (identity == null ? null : identity.id);
                draft.from = afrom;
                draft.to = ato;
                draft.cc = acc;
                draft.bcc = abcc;
                draft.subject = subject;
                draft.received = new Date().getTime();

                // Execute action
                if (action == R.id.action_delete) {
                    draft.msgid = null;
                    draft.ui_hide = true;
                    db.message().updateMessage(draft);

                    EntityOperation.queue(db, draft, EntityOperation.DELETE);

                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(context, R.string.title_draft_deleted, Toast.LENGTH_LONG).show();
                        }
                    });
                } else if (action == R.id.action_save) {
                    db.message().updateMessage(draft);
                    draft.write(context, body);

                    EntityOperation.queue(db, draft, EntityOperation.ADD);

                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(context, R.string.title_draft_saved, Toast.LENGTH_LONG).show();
                        }
                    });

                } else if (action == R.id.action_send) {
                    db.message().updateMessage(draft);
                    draft.write(context, body);

                    // Check data
                    if (draft.identity == null) {
                        db.setTransactionSuccessful();
                        throw new IllegalArgumentException(context.getString(R.string.title_from_missing));
                    }

                    if (draft.to == null && draft.cc == null && draft.bcc == null) {
                        db.setTransactionSuccessful();
                        throw new IllegalArgumentException(context.getString(R.string.title_to_missing));
                    }

                    // Save message ID
                    String msgid = draft.msgid;

                    // Save attachments
                    List<EntityAttachment> attachments = db.attachment().getAttachments(draft.id);
                    for (EntityAttachment attachment : attachments)
                        if (!attachment.available) {
                            db.setTransactionSuccessful();
                            throw new IllegalArgumentException(context.getString(R.string.title_attachments_missing));
                        }

                    // Delete draft (cannot move to outbox)
                    draft.msgid = null;
                    draft.ui_hide = true;
                    db.message().updateMessage(draft);
                    EntityOperation.queue(db, draft, EntityOperation.DELETE);

                    // Copy message to outbox
                    draft.id = null;
                    draft.folder = db.folder().getOutbox().id;
                    draft.uid = null;
                    draft.msgid = msgid;
                    draft.ui_hide = false;
                    draft.id = db.message().insertMessage(draft);
                    draft.write(getContext(), body);

                    // Restore attachments
                    for (EntityAttachment attachment : attachments) {
                        File file = EntityAttachment.getFile(context, attachment.id);
                        attachment.id = null;
                        attachment.message = draft.id;
                        attachment.id = db.attachment().insertAttachment(attachment);
                        Helper.copy(file, EntityAttachment.getFile(context, attachment.id));
                    }

                    EntityOperation.queue(db, draft, EntityOperation.SEND);
                }

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }

            EntityOperation.process(context);

            return draft;
        }

        @Override
        protected void onLoaded(Bundle args, EntityMessage draft) {
            int action = args.getInt("action");
            Log.i(Helper.TAG, "Loaded action id=" + (draft == null ? null : draft.id) + " action=" + action);

            Helper.setViewsEnabled(view, true);
            getActivity().invalidateOptionsMenu();

            if (action == R.id.action_delete) {
                autosave = false;
                getFragmentManager().popBackStack();

            } else if (action == R.id.action_save) {
                // Do nothing

            } else if (action == R.id.action_send) {
                autosave = false;
                getFragmentManager().popBackStack();
                Toast.makeText(getContext(), R.string.title_queued, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onException(Bundle args, Throwable ex) {
            Helper.setViewsEnabled(view, true);
            getActivity().invalidateOptionsMenu();

            if (ex instanceof IllegalArgumentException)
                Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG).show();
            else
                Helper.unexpectedError(getContext(), ex);
        }
    };

    private Html.ImageGetter cidGetter = new Html.ImageGetter() {
        @Override
        public Drawable getDrawable(String source) {
            if (source != null && source.startsWith("cid")) {
                String[] cid = source.split(":");
                if (cid.length == 2 && cid[1].startsWith(BuildConfig.APPLICATION_ID)) {
                    long id = Long.parseLong(cid[1].replace(BuildConfig.APPLICATION_ID + ".", ""));
                    File file = EntityAttachment.getFile(getContext(), id);
                    Drawable d = Drawable.createFromPath(file.getAbsolutePath());
                    if (d != null) {
                        d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
                        return d;
                    }
                }
            }

            float scale = getContext().getResources().getDisplayMetrics().density;
            int px = (int) (24 * scale + 0.5f);
            Drawable d = getContext().getResources().getDrawable(R.drawable.baseline_warning_24, getContext().getTheme());
            d.setBounds(0, 0, px, px);
            return d;
        }
    };

    public class IdentityAdapter extends ArrayAdapter<EntityIdentity> {
        private Context context;
        private List<EntityIdentity> identities;

        IdentityAdapter(@NonNull Context context, List<EntityIdentity> identities) {
            super(context, 0, identities);
            this.context = context;
            this.identities = identities;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            return getLayout(position, convertView, parent);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getLayout(position, convertView, parent);
        }

        View getLayout(int position, View convertView, ViewGroup parent) {
            View view = LayoutInflater.from(context).inflate(R.layout.spinner_item2, parent, false);

            EntityIdentity identity = identities.get(position);

            TextView name = view.findViewById(android.R.id.text1);
            name.setText(identity.name);

            TextView email = view.findViewById(android.R.id.text2);
            email.setText(identity.email);

            return view;
        }
    }
}