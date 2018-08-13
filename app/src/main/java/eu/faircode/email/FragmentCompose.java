package eu.faircode.email;

/*
    This file is part of Safe email.

    Safe email is free software: you can redistribute it and/or modify
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
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.OpenableColumns;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.mail.Address;
import javax.mail.MessageRemovedException;
import javax.mail.internet.InternetAddress;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.ContextCompat;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.app.Activity.RESULT_OK;

public class FragmentCompose extends FragmentEx {
    private ViewGroup view;
    private Spinner spFrom;
    private ImageView ivIdentityAdd;
    private AutoCompleteTextView etTo;
    private ImageView ivToAdd;
    private AutoCompleteTextView etCc;
    private ImageView ivCcAdd;
    private AutoCompleteTextView etBcc;
    private ImageView ivBccAdd;
    private EditText etSubject;
    private RecyclerView rvAttachment;
    private EditText etBody;
    private BottomNavigationView bottom_navigation;
    private ProgressBar pbWait;
    private Group grpAddresses;
    private Group grpAttachments;
    private Group grpReady;

    private AdapterAttachment adapter;

    private boolean autosave = true;
    private EntityMessage draft = null;

    private static final int ATTACHMENT_BUFFER_SIZE = 8192; // bytes

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
        grpAddresses = view.findViewById(R.id.grpAddresses);
        grpAttachments = view.findViewById(R.id.grpAttachments);
        grpReady = view.findViewById(R.id.grpReady);

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

        bottom_navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                onAction(item.getItemId());

                return false;
            }
        });

        setHasOptionsMenu(true);

        // Initialize
        spFrom.setVisibility(View.GONE);
        ivIdentityAdd.setVisibility(View.GONE);
        grpAddresses.setVisibility(View.GONE);
        grpAttachments.setVisibility(View.GONE);
        grpReady.setVisibility(View.GONE);
        pbWait.setVisibility(View.VISIBLE);
        bottom_navigation.getMenu().setGroupEnabled(0, false);

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
                    return cursor.getString(colName) + "<" + cursor.getString(colEmail) + ">";
                }
            });
        }

        rvAttachment.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rvAttachment.setLayoutManager(llm);

        adapter = new AdapterAttachment(getContext(), getViewLifecycleOwner());
        rvAttachment.setAdapter(adapter);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (draft == null) {
            Bundle args = new Bundle();
            args.putString("action", getArguments().getString("action"));
            args.putLong("id", getArguments().getLong("id", -1));
            args.putLong("account", getArguments().getLong("account", -1));
            args.putLong("reference", getArguments().getLong("reference", -1));
            draftLoader.load(FragmentCompose.this, args);
        } else {
            Bundle args = new Bundle();
            args.putString("action", "edit");
            args.putLong("id", draft.id);
            args.putLong("account", draft.account);
            args.putLong("reference", -1);
            draftLoader.load(FragmentCompose.this, args);
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
        menu.findItem(R.id.menu_attachment).setVisible(draft != null);
        menu.findItem(R.id.menu_addresses).setVisible(draft != null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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

    private void onMenuAttachment() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, ActivityCompose.REQUEST_ATTACHMENT);
    }

    private void onMenuAddresses() {
        grpAddresses.setVisibility(grpAddresses.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == ActivityCompose.REQUEST_ATTACHMENT) {
                if (data != null)
                    handleAddAttachment(data);
            } else
                handlePickContact(requestCode, data);
        }
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
                if (sb.length() > 0)
                    sb.append(", ");
                sb.append(address.toString());

                if (requestCode == ActivityCompose.REQUEST_CONTACT_TO)
                    etTo.setText(sb.toString());
                else if (requestCode == ActivityCompose.REQUEST_CONTACT_CC)
                    etCc.setText(sb.toString());
                else if (requestCode == ActivityCompose.REQUEST_CONTACT_BCC)
                    etBcc.setText(sb.toString());
            }
        } catch (Throwable ex) {
            Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
            Toast.makeText(getContext(), ex.toString(), Toast.LENGTH_LONG).show();
        } finally {
            if (cursor != null)
                cursor.close();
        }
    }

    private void handleAddAttachment(Intent data) {
        Bundle args = new Bundle();
        args.putString("msgid", draft.msgid);
        args.putParcelable("uri", data.getData());

        new SimpleTask<Void>() {
            @Override
            protected Void onLoad(Context context, Bundle args) throws IOException {
                Cursor cursor = null;
                try {
                    Uri uri = args.getParcelable("uri");
                    if (uri != null)
                        cursor = context.getContentResolver().query(uri, null, null, null, null, null);
                    if (cursor == null || !cursor.moveToFirst())
                        return null;

                    String msgid = args.getString("msgid");
                    EntityAttachment attachment = new EntityAttachment();

                    DB db = DB.getInstance(context);
                    try {
                        db.beginTransaction();

                        EntityMessage draft = db.message().getMessageByMsgId(msgid);
                        Log.i(Helper.TAG, "Attaching to id=" + draft.id);

                        attachment.message = draft.id;
                        attachment.sequence = db.attachment().getAttachmentCount(draft.id) + 1;
                        attachment.name = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));

                        String extension = MimeTypeMap.getFileExtensionFromUrl(attachment.name.toLowerCase());
                        if (extension != null)
                            attachment.type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                        if (attachment.type == null)
                            attachment.type = "application/octet-stream";

                        String size = cursor.getString(cursor.getColumnIndex(OpenableColumns.SIZE));

                        attachment.size = (size == null ? null : Integer.parseInt(size));
                        attachment.progress = 0;

                        attachment.id = db.attachment().insertAttachment(attachment);
                        Log.i(Helper.TAG, "Created attachment seq=" + attachment.sequence + " name=" + attachment.name);

                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }

                    InputStream is = null;
                    try {
                        is = context.getContentResolver().openInputStream(uri);
                        ByteArrayOutputStream os = new ByteArrayOutputStream();

                        int len;
                        byte[] buffer = new byte[ATTACHMENT_BUFFER_SIZE];
                        while ((len = is.read(buffer)) > 0) {
                            os.write(buffer, 0, len);

                            // Update progress
                            if (attachment.size != null) {
                                attachment.progress = os.size() * 100 / attachment.size;
                                db.attachment().updateAttachment(attachment);
                            }
                        }

                        attachment.size = os.size();
                        attachment.progress = null;
                        attachment.content = os.toByteArray();
                        db.attachment().updateAttachment(attachment);
                    } finally {
                        if (is != null)
                            is.close();
                    }

                    return null;
                } finally {
                    if (cursor != null)
                        cursor.close();
                }
            }

            @Override
            protected void onLoaded(Bundle args, Void data) {
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Toast.makeText(getContext(), ex.toString(), Toast.LENGTH_LONG).show();
            }
        }.load(this, args);
    }

    private void onAction(int action) {
        Helper.setViewsEnabled(view, false);
        bottom_navigation.getMenu().setGroupEnabled(0, false);

        EntityIdentity identity = (EntityIdentity) spFrom.getSelectedItem();

        Bundle args = new Bundle();
        args.putString("msgid", draft.msgid);
        args.putInt("action", action);
        args.putLong("identity", identity == null ? -1 : identity.id);
        args.putString("to", etTo.getText().toString());
        args.putString("cc", etCc.getText().toString());
        args.putString("bcc", etBcc.getText().toString());
        args.putString("subject", etSubject.getText().toString());
        args.putString("body", etBody.getText().toString());

        Log.i(Helper.TAG, "Run load id=" + draft.id + " msgid=" + draft.msgid);
        actionLoader.load(this, args);
    }

    private SimpleTask<EntityMessage> draftLoader = new SimpleTask<EntityMessage>() {
        @Override
        protected EntityMessage onLoad(Context context, Bundle args) {
            String action = args.getString("action");
            long id = args.getLong("id", -1);
            long account = args.getLong("account", -1);
            long reference = args.getLong("reference", -1);

            Log.i(Helper.TAG, "Load draft action=" + action + " id=" + id + " account=" + account + " reference=" + reference);

            EntityMessage draft;

            DB db = DB.getInstance(context);
            try {
                db.beginTransaction();

                draft = db.message().getMessage(id);
                if (draft == null) {
                    if ("edit".equals(action))
                        throw new IllegalStateException("Message to edit not found");
                } else
                    return draft;

                EntityMessage ref = db.message().getMessage(reference);
                if (ref != null)
                    account = ref.account;

                EntityFolder drafts;
                drafts = db.folder().getFolderByType(account, EntityFolder.DRAFTS);
                if (drafts == null)
                    drafts = db.folder().getPrimaryDrafts();

                draft = new EntityMessage();
                draft.account = account;
                draft.folder = drafts.id;
                draft.msgid = draft.generateMessageId();

                if (ref != null) {
                    draft.thread = ref.thread;

                    if ("reply".equals(action)) {
                        draft.replying = ref.id;
                        draft.to = (ref.reply == null || ref.reply.length == 0 ? ref.from : ref.reply);
                        draft.from = ref.to;

                    } else if ("reply_all".equals(action)) {
                        draft.replying = ref.id;
                        List<Address> addresses = new ArrayList<>();
                        if (draft.reply != null && ref.reply.length > 0)
                            addresses.addAll(Arrays.asList(ref.reply));
                        else if (draft.from != null)
                            addresses.addAll(Arrays.asList(ref.from));
                        if (draft.cc != null)
                            addresses.addAll(Arrays.asList(ref.cc));
                        draft.to = addresses.toArray(new Address[0]);
                        draft.from = ref.to;

                    } else if ("forward".equals(action)) {
                        //msg.replying = ref.id;
                        draft.from = ref.to;
                    }

                    if ("reply".equals(action) || "reply_all".equals(action)) {
                        draft.subject = context.getString(R.string.title_subject_reply, ref.subject);
                        draft.body = String.format("<br><br>%s %s:<br><br>%s",
                                Html.escapeHtml(new Date().toString()),
                                Html.escapeHtml(TextUtils.join(", ", draft.to)),
                                HtmlHelper.sanitize(context, ref.body, true));
                    } else if ("forward".equals(action)) {
                        draft.subject = context.getString(R.string.title_subject_forward, ref.subject);
                        draft.body = String.format("<br><br>%s %s:<br><br>%s",
                                Html.escapeHtml(new Date().toString()),
                                Html.escapeHtml(TextUtils.join(", ", ref.from)),
                                HtmlHelper.sanitize(context, ref.body, true));
                    }
                }

                if ("new".equals(action))
                    draft.body = "";

                draft.received = new Date().getTime();
                draft.seen = false;
                draft.ui_seen = false;
                draft.ui_hide = false;

                draft.id = db.message().insertMessage(draft);
                draft.msgid = draft.generateMessageId();
                db.message().updateMessage(draft);
                args.putLong("id", draft.id);

                EntityOperation.queue(db, draft, EntityOperation.ADD);

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }

            EntityOperation.process(context);

            return draft;
        }


        @Override
        protected void onLoaded(Bundle args, EntityMessage draft) {
            FragmentCompose.this.draft = draft;
            Log.i(Helper.TAG, "Loaded draft id=" + draft.id + " msgid=" + draft.msgid);

            DB db = DB.getInstance(getContext());

            db.attachment().liveAttachments(draft.folder, draft.msgid).observe(getViewLifecycleOwner(),
                    new Observer<List<TupleAttachment>>() {
                        @Override
                        public void onChanged(@Nullable List<TupleAttachment> attachments) {
                            if (attachments != null)
                                adapter.set(attachments);
                            grpAttachments.setVisibility(attachments != null && attachments.size() > 0 ? View.VISIBLE : View.GONE);
                        }
                    });

            db.message().liveMessageByMsgId(draft.folder, draft.msgid).observe(getViewLifecycleOwner(), new Observer<EntityMessage>() {
                boolean observed = false;

                @Override
                public void onChanged(final EntityMessage draft) {
                    // Message was deleted
                    if (draft == null) {
                        getFragmentManager().popBackStack();
                        return;
                    }

                    // New working copy
                    FragmentCompose.this.draft = draft;

                    DB db = DB.getInstance(getContext());

                    // Set controls only once
                    if (observed)
                        return;
                    observed = true;

                    String action = getArguments().getString("action");

                    getActivity().invalidateOptionsMenu();
                    pbWait.setVisibility(View.GONE);
                    grpAddresses.setVisibility("reply_all".equals(action) ? View.VISIBLE : View.GONE);
                    grpReady.setVisibility(View.VISIBLE);

                    ArrayAdapter aa = (ArrayAdapter) spFrom.getAdapter();
                    if (aa != null) {
                        for (int pos = 0; pos < aa.getCount(); pos++) {
                            EntityIdentity identity = (EntityIdentity) aa.getItem(pos);
                            if (draft.identity == null
                                    ? draft.from != null && draft.from.length > 0 && ((InternetAddress) draft.from[0]).getAddress().equals(identity.email)
                                    : draft.identity.equals(identity.id)) {
                                spFrom.setSelection(pos);
                                break;
                            }
                        }
                    }

                    etTo.setText(draft.to == null ? null : TextUtils.join(", ", draft.to));
                    etCc.setText(draft.cc == null ? null : TextUtils.join(", ", draft.cc));
                    etBcc.setText(draft.bcc == null ? null : TextUtils.join(", ", draft.bcc));
                    etSubject.setText(draft.subject);

                    etBody.setText(TextUtils.isEmpty(draft.body) ? null : Html.fromHtml(draft.body));

                    if ("edit".equals(action))
                        etTo.requestFocus();
                    else if ("reply".equals(action) || "reply_all".equals(action))
                        etBody.requestFocus();
                    else if ("forward".equals(action))
                        etTo.requestFocus();

                    bottom_navigation.getMenu().setGroupEnabled(0, true);

                    db.identity().liveIdentities(true).removeObservers(getViewLifecycleOwner());
                    db.identity().liveIdentities(true).observe(getViewLifecycleOwner(), new Observer<List<EntityIdentity>>() {
                        @Override
                        public void onChanged(@Nullable final List<EntityIdentity> identities) {
                            if (identities == null)
                                return;

                            Log.i(Helper.TAG, "Set identities=" + identities.size());

                            // Sort identities
                            Collections.sort(identities, new Comparator<EntityIdentity>() {
                                @Override
                                public int compare(EntityIdentity i1, EntityIdentity i2) {
                                    return i1.name.compareTo(i2.name);
                                }
                            });

                            // Show identities
                            ArrayAdapter<EntityIdentity> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, identities);
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
                                String from = ((InternetAddress) draft.from[0]).getAddress();
                                for (int pos = 0; pos < identities.size(); pos++) {
                                    if (identities.get(pos).email.equals(from)) {
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

                            spFrom.setVisibility(View.VISIBLE);
                            ivIdentityAdd.setVisibility(View.VISIBLE);

                        }
                    });
                }
            });
        }

        @Override
        protected void onException(Bundle args, Throwable ex) {
            Toast.makeText(getContext(), ex.toString(), Toast.LENGTH_LONG).show();
        }
    };

    private SimpleTask<EntityMessage> actionLoader = new SimpleTask<EntityMessage>() {
        @Override
        protected EntityMessage onLoad(Context context, Bundle args) throws Throwable {
            // Get data
            String id = args.getString("msgid");
            int action = args.getInt("action");
            long iid = args.getLong("identity");
            String to = args.getString("to");
            String cc = args.getString("cc");
            String bcc = args.getString("bcc");
            String subject = args.getString("subject");
            String body = args.getString("body");

            EntityMessage draft;

            // Get draft & selected identity
            DB db = DB.getInstance(context);
            try {
                db.beginTransaction();

                draft = db.message().getMessageByMsgId(id);
                EntityIdentity identity = db.identity().getIdentity(iid);

                // Draft deleted by server
                // TODO: better handling of remote deleted message
                if (draft == null)
                    throw new MessageRemovedException();

                Log.i(Helper.TAG, "Load action id=" + draft.id + " msgid=" + draft.msgid + " action=" + action);

                // Convert data
                Address afrom[] = (identity == null ? null : new Address[]{new InternetAddress(identity.email, identity.name)});
                Address ato[] = (TextUtils.isEmpty(to) ? null : InternetAddress.parse(to));
                Address acc[] = (TextUtils.isEmpty(cc) ? null : InternetAddress.parse(cc));
                Address abcc[] = (TextUtils.isEmpty(bcc) ? null : InternetAddress.parse(bcc));

                // Update draft
                draft.identity = (identity == null ? null : identity.id);
                draft.from = afrom;
                draft.to = ato;
                draft.cc = acc;
                draft.bcc = abcc;
                draft.subject = subject;
                draft.body = "<pre>" + body.replaceAll("\\r?\\n", "<br />") + "</pre>";
                draft.received = new Date().getTime();

                db.message().updateMessage(draft);

                // Execute action
                if (action == R.id.action_trash) {
                    draft.ui_hide = true;
                    db.message().updateMessage(draft);

                    EntityFolder trash = db.folder().getFolderByType(draft.account, EntityFolder.TRASH);
                    EntityOperation.queue(db, draft, EntityOperation.MOVE, trash.id);

                } else if (action == R.id.action_save) {
                    if (draft.uid == null)
                        db.message().updateMessage(draft);
                    else {
                        // Save message ID
                        String msgid = draft.msgid;

                        // Save attachments
                        List<EntityAttachment> attachments = db.attachment().getAttachments(draft.id);
                        for (EntityAttachment attachment : attachments)
                            attachment.content = db.attachment().getContent(attachment.id);

                        // Delete previous draft
                        draft.msgid = null;
                        draft.ui_hide = true;
                        db.message().updateMessage(draft);

                        EntityOperation.queue(db, draft, EntityOperation.DELETE);

                        // Create new draft
                        draft.id = null;
                        draft.uid = null;
                        draft.msgid = msgid;
                        draft.ui_hide = false;
                        draft.id = db.message().insertMessage(draft);

                        // Restore attachments
                        for (EntityAttachment attachment : attachments) {
                            attachment.id = null;
                            attachment.message = draft.id;
                            db.attachment().insertAttachment(attachment);
                        }

                        EntityOperation.queue(db, draft, EntityOperation.ADD);
                    }

                } else if (action == R.id.action_send) {
                    // Check data
                    if (draft.identity == null)
                        throw new IllegalArgumentException(context.getString(R.string.title_from_missing));

                    if (draft.to == null && draft.cc == null && draft.bcc == null)
                        throw new IllegalArgumentException(context.getString(R.string.title_to_missing));

                    if (db.attachment().getAttachmentCountWithoutContent(draft.id) > 0)
                        throw new IllegalArgumentException(context.getString(R.string.title_attachments_missing));

                    // Save message ID
                    String msgid = draft.msgid;

                    // Save attachments
                    List<EntityAttachment> attachments = db.attachment().getAttachments(draft.id);
                    for (EntityAttachment attachment : attachments)
                        attachment.content = db.attachment().getContent(attachment.id);

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

                    // Restore attachments
                    for (EntityAttachment attachment : attachments) {
                        attachment.id = null;
                        attachment.message = draft.id;
                        db.attachment().insertAttachment(attachment);
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
            FragmentCompose.this.draft = draft;

            int action = args.getInt("action");
            Log.i(Helper.TAG, "Loaded action id=" + draft.id + " msgid=" + draft.msgid + " action=" + action);

            Helper.setViewsEnabled(view, true);
            bottom_navigation.getMenu().setGroupEnabled(0, true);

            if (action == R.id.action_trash) {
                autosave = false;
                getFragmentManager().popBackStack();
                Toast.makeText(getContext(), R.string.title_draft_trashed, Toast.LENGTH_LONG).show();
            } else if (action == R.id.action_save)
                Toast.makeText(getContext(), R.string.title_draft_saved, Toast.LENGTH_LONG).show();
            else if (action == R.id.action_send) {
                autosave = false;
                getFragmentManager().popBackStack();
                Toast.makeText(getContext(), R.string.title_queued, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onException(Bundle args, Throwable ex) {
            bottom_navigation.getMenu().setGroupEnabled(0, true);

            if (ex instanceof IllegalArgumentException)
                Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG).show();
            else
                Toast.makeText(getContext(), ex.toString(), Toast.LENGTH_LONG).show();
        }
    };
}