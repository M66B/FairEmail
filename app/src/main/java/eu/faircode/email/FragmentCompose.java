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

import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.Group;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

import static android.app.Activity.RESULT_OK;

public class FragmentCompose extends Fragment {
    private boolean once = false;
    private String thread = null;
    private long rid = -1;

    private Spinner spFrom;
    private ImageView ivIdentityAdd;
    private EditText etTo;
    private ImageView ivToAdd;
    private EditText etCc;
    private ImageView ivCcAdd;
    private EditText etBcc;
    private ImageView ivBccAdd;
    private EditText etSubject;
    private EditText etBody;
    private BottomNavigationView bottom_navigation;
    private ProgressBar pbWait;
    private Group grpCc;
    private Group grpReady;

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_compose, container, false);

        // Get arguments
        Bundle args = getArguments();
        String action = args.getString("action");
        final long id = (TextUtils.isEmpty(action) ? args.getLong("id") : -1);

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
        etBody = view.findViewById(R.id.etBody);
        bottom_navigation = view.findViewById(R.id.bottom_navigation);
        pbWait = view.findViewById(R.id.pbWait);
        grpCc = view.findViewById(R.id.grpCc);
        grpReady = view.findViewById(R.id.grpReady);

        grpCc.setVisibility(View.GONE);
        etBody.setMovementMethod(LinkMovementMethod.getInstance());

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
                bottom_navigation.setEnabled(false);

                switch (item.getItemId()) {
                    case R.id.action_delete:
                        actionDelete(id);
                        return true;
                    case R.id.action_save:
                        actionPut(id, false);
                        return true;
                    case R.id.action_send:
                        actionPut(id, true);
                        return true;
                }

                return false;
            }
        });

        setHasOptionsMenu(true);

        // Initialize
        grpReady.setVisibility(View.GONE);
        pbWait.setVisibility(View.VISIBLE);
        bottom_navigation.getMenu().findItem(R.id.action_delete).setEnabled(id > 0);
        bottom_navigation.setEnabled(false);

        DB.getInstance(getContext()).identity().liveIdentities(true).observe(getActivity(), new Observer<List<EntityIdentity>>() {
            @Override
            public void onChanged(@Nullable final List<EntityIdentity> identities) {
                Collections.sort(identities, new Comparator<EntityIdentity>() {
                    @Override
                    public int compare(EntityIdentity i1, EntityIdentity i2) {
                        return i1.name.compareTo(i2.name);
                    }
                });

                ArrayAdapter<EntityIdentity> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, identities);
                adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                spFrom.setAdapter(adapter);

                // Select primary identity, also for saved drafts
                for (int pos = 0; pos < identities.size(); pos++)
                    if (identities.get(pos).primary) {
                        spFrom.setSelection(pos);
                        break;
                    }
            }
        });

        getLoaderManager().restartLoader(ActivityCompose.LOADER_COMPOSE_GET, getArguments(), getLoaderCallbacks).forceLoad();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(R.string.title_compose);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_cc, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_cc:
                onMenuCc();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onMenuCc() {
        grpCc.setVisibility(grpCc.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Cursor cursor = null;
            try {
                cursor = getContext().getContentResolver().query(data.getData(),
                        new String[]{
                                ContactsContract.CommonDataKinds.Email.ADDRESS,
                                ContactsContract.Contacts.DISPLAY_NAME
                        },
                        null, null, null);
                if (cursor.moveToFirst()) {
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
                        sb.append("; ");
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
                Toast.makeText(getContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
            } finally {
                if (cursor != null)
                    cursor.close();
            }
        }
    }

    private void actionDelete(final long id) {
        Bundle args = new Bundle();
        args.putLong("id", id);
        getLoaderManager().restartLoader(ActivityCompose.LOADER_COMPOSE_DELETE, args, deleteLoaderCallbacks).forceLoad();
    }

    private void actionPut(long id, boolean send) {
        bottom_navigation.setEnabled(false);

        EntityIdentity identity = (EntityIdentity) spFrom.getSelectedItem();

        Bundle args = new Bundle();
        args.putLong("id", id);
        args.putLong("iid", identity == null ? -1 : identity.id);
        args.putString("thread", FragmentCompose.this.thread);
        args.putLong("rid", FragmentCompose.this.rid);
        args.putString("to", etTo.getText().toString());
        args.putString("cc", etCc.getText().toString());
        args.putString("bcc", etBcc.getText().toString());
        args.putString("subject", etSubject.getText().toString());
        args.putString("body", etBody.getText().toString());
        args.putBoolean("send", send);

        getLoaderManager().restartLoader(ActivityCompose.LOADER_COMPOSE_PUT, args, putLoaderCallbacks).forceLoad();
    }

    private static class GetLoader extends AsyncTaskLoader<Bundle> {
        private Bundle args;

        GetLoader(Context context) {
            super(context);
        }

        void setArgs(Bundle args) {
            this.args = args;
        }

        @Nullable
        @Override
        public Bundle loadInBackground() {
            Bundle result = new Bundle();
            try {
                String action = args.getString("action");
                long id = args.getLong("id", -1);
                EntityMessage msg = DB.getInstance(getContext()).message().getMessage(id);

                result.putString("action", action);

                if (msg != null) {
                    if (msg.identity != null)
                        result.putLong("iid", msg.identity);
                    if (msg.replying != null)
                        result.putLong("rid", msg.replying);
                    result.putString("cc", msg.cc);
                    result.putString("bcc", msg.bcc);
                    result.putString("thread", msg.thread);
                    result.putString("subject", msg.subject);
                    result.putString("body", msg.body);
                }

                if (TextUtils.isEmpty(action)) {
                    if (msg != null) {
                        result.putString("from", msg.from);
                        result.putString("to", msg.to);
                    }
                } else if ("reply".equals(action)) {
                    String to = null;
                    if (msg != null)
                        try {
                            Address[] reply = MessageHelper.decodeAddresses(msg.reply);
                            to = (reply.length == 0 ? msg.from : msg.reply);
                        } catch (Throwable ex) {
                            Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                        }
                    result.putLong("rid", msg.id);
                    result.putString("from", msg.to);
                    result.putString("to", to);
                } else if ("reply_all".equals(action)) {
                    String to = null;
                    if (msg != null) {
                        try {
                            Address[] from = MessageHelper.decodeAddresses(msg.from);
                            Address[] reply = MessageHelper.decodeAddresses(msg.reply);
                            Address[] cc = MessageHelper.decodeAddresses(msg.cc);
                            List<Address> addresses = new ArrayList<>();
                            addresses.addAll(Arrays.asList(reply.length == 0 ? from : reply));
                            addresses.addAll(Arrays.asList(cc));
                            to = MessageHelper.encodeAddresses(addresses.toArray(new Address[0]));
                        } catch (Throwable ex) {
                            Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                        }
                    }
                    result.putLong("rid", msg.id);
                    result.putString("from", msg.to);
                    result.putString("to", to);
                } else if ("forward".equals(action)) {
                    result.putString("from", msg.to);
                    result.putString("to", null);
                }
            } catch (Throwable ex) {
                Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
            }
            return result;
        }
    }

    private LoaderManager.LoaderCallbacks getLoaderCallbacks = new LoaderManager.LoaderCallbacks<Bundle>() {
        @NonNull
        @Override
        public Loader<Bundle> onCreateLoader(int id, @Nullable Bundle args) {
            GetLoader loader = new GetLoader(getContext());
            loader.setArgs(args);
            return loader;
        }

        @Override
        public void onLoadFinished(@NonNull Loader<Bundle> loader, final Bundle result) {
            getLoaderManager().destroyLoader(loader.getId());

            long iid = result.getLong("iid", -1);
            long rid = result.getLong("rid", -1);
            String thread = result.getString("thread");
            String from = result.getString("from");
            String to = result.getString("to");
            String cc = result.getString("cc");
            String bcc = result.getString("bcc");
            String subject = result.getString("subject");
            String body = result.getString("body");
            String action = result.getString("action");

            pbWait.setVisibility(View.GONE);
            grpReady.setVisibility(View.VISIBLE);

            FragmentCompose.this.thread = thread;
            FragmentCompose.this.rid = rid;

            ArrayAdapter adapter = (ArrayAdapter) spFrom.getAdapter();
            if (adapter != null)
                for (int pos = 0; pos < adapter.getCount(); pos++) {
                    EntityIdentity identity = (EntityIdentity) adapter.getItem(pos);
                    if (iid < 0 ? identity.primary : iid == identity.id) {
                        spFrom.setSelection(pos);
                        break;
                    }
                }

            if (!once) {
                // Prevent changed fields from being overwritten
                once = true;

                etCc.setText(TextUtils.join(", ", MessageHelper.decodeAddresses(cc)));
                etBcc.setText(TextUtils.join(", ", MessageHelper.decodeAddresses(bcc)));

                if (action == null) {
                    etTo.setText(TextUtils.join(", ", MessageHelper.decodeAddresses(to)));
                    etSubject.setText(subject);
                    if (body != null)
                        etBody.setText(Html.fromHtml(HtmlHelper.sanitize(getContext(), body, false)));
                } else if ("reply".equals(action) || "reply_all".equals(action)) {
                    etTo.setText(TextUtils.join(", ", MessageHelper.decodeAddresses(to)));
                    String text = String.format("<br><br>%s %s:<br><br>%s",
                            Html.escapeHtml(new Date().toString()),
                            Html.escapeHtml(TextUtils.join(", ", MessageHelper.decodeAddresses(from))),
                            HtmlHelper.sanitize(getContext(), body, true));
                    etSubject.setText(getContext().getString(R.string.title_subject_reply, subject));
                    etBody.setText(Html.fromHtml(text));
                } else if ("forward".equals(action)) {
                    String text = String.format("<br><br>%s %s:<br><br>%s",
                            Html.escapeHtml(new Date().toString()),
                            Html.escapeHtml(TextUtils.join(", ", MessageHelper.decodeAddresses(from))),
                            HtmlHelper.sanitize(getContext(), body, true));
                    etSubject.setText(getContext().getString(R.string.title_subject_forward, subject));
                    etBody.setText(Html.fromHtml(text));
                }
            }

            bottom_navigation.setEnabled(true);
        }

        @Override
        public void onLoaderReset(@NonNull Loader<Bundle> loader) {
        }
    };

    private static class DeleteLoader extends AsyncTaskLoader<Throwable> {
        private Bundle args;

        DeleteLoader(Context context) {
            super(context);
        }

        void setArgs(Bundle args) {
            this.args = args;
        }

        @Override
        public Throwable loadInBackground() {
            try {
                long id = args.getLong("id");
                DaoMessage message = DB.getInstance(getContext()).message();
                EntityMessage draft = message.getMessage(id);
                if (draft != null) {
                    draft.ui_hide = true;
                    message.updateMessage(draft);
                    EntityOperation.queue(getContext(), draft, EntityOperation.DELETE);
                }
                return null;
            } catch (Throwable ex) {
                return ex;
            }
        }
    }

    private LoaderManager.LoaderCallbacks deleteLoaderCallbacks = new LoaderManager.LoaderCallbacks<Throwable>() {
        @NonNull
        @Override
        public Loader<Throwable> onCreateLoader(int id, @Nullable Bundle args) {
            DeleteLoader loader = new DeleteLoader(getContext());
            loader.setArgs(args);
            return loader;
        }

        @Override
        public void onLoadFinished(@NonNull Loader<Throwable> loader, Throwable ex) {
            getLoaderManager().destroyLoader(loader.getId());

            if (ex == null) {
                getFragmentManager().popBackStack();
                Toast.makeText(getContext(), R.string.title_draft_deleted, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onLoaderReset(@NonNull Loader<Throwable> loader) {
        }
    };

    private static class PutLoader extends AsyncTaskLoader<Throwable> {
        private Bundle args;

        PutLoader(Context context) {
            super(context);
        }

        void setArgs(Bundle args) {
            this.args = args;
        }

        @Override
        public Throwable loadInBackground() {
            long id = args.getLong("id");
            boolean send = args.getBoolean("send", false);
            Log.i(Helper.TAG, "Put load id=" + id + " send=" + send);
            try {
                DB db = DB.getInstance(getContext());
                DaoMessage message = db.message();
                DaoIdentity identity = db.identity();
                DaoFolder folder = db.folder();

                // Get data
                EntityMessage draft = message.getMessage(id);
                EntityIdentity ident = identity.getIdentity(args.getLong("iid"));
                EntityFolder drafts = db.folder().getPrimaryDraftFolder();
                if (drafts == null)
                    throw new Throwable(getContext().getString(R.string.title_no_primary_drafts));

                long rid = args.getLong("rid", -1);
                String thread = args.getString("thread");
                String to = args.getString("to");
                String cc = args.getString("cc");
                String bcc = args.getString("bcc");
                String body = args.getString("body");
                String subject = args.getString("subject");

                Address afrom = (ident == null ? null : new InternetAddress(ident.email, ident.name));
                Address ato[] = (TextUtils.isEmpty(to) ? null : InternetAddress.parse(to));
                Address acc[] = (TextUtils.isEmpty(cc) ? null : InternetAddress.parse(cc));
                Address abcc[] = (TextUtils.isEmpty(bcc) ? null : InternetAddress.parse(bcc));

                // Build draft
                boolean update = (draft != null);
                if (draft == null)
                    draft = new EntityMessage();
                draft.account = drafts.account;
                draft.folder = drafts.id;
                draft.identity = (ident == null ? null : ident.id);
                draft.replying = (rid < 0 ? null : rid);
                draft.thread = thread;
                draft.from = MessageHelper.encodeAddresses(new Address[]{afrom});
                draft.to = MessageHelper.encodeAddresses(ato);
                draft.cc = MessageHelper.encodeAddresses(acc);
                draft.bcc = MessageHelper.encodeAddresses(abcc);
                draft.subject = subject;
                draft.body = "<pre>" + body.replaceAll("\\r?\\n", "<br />") + "</pre>";
                draft.received = new Date().getTime();
                draft.seen = false;
                draft.ui_seen = false;
                draft.ui_hide = send;

                // Store draft
                if (update)
                    message.updateMessage(draft);
                else
                    draft.id = message.insertMessage(draft);

                // Check data
                if (send) {
                    if (draft.identity == null)
                        throw new MessagingException(getContext().getString(R.string.title_from_missing));
                    if (draft.to == null && draft.cc == null && draft.bcc == null)
                        throw new MessagingException(getContext().getString(R.string.title_to_missing));

                    // Get outbox
                    EntityFolder outbox = folder.getOutbox();
                    if (outbox == null) {
                        outbox = new EntityFolder();
                        outbox.name = "OUTBOX";
                        outbox.type = EntityFolder.TYPE_OUTBOX;
                        outbox.synchronize = false;
                        outbox.after = 0;
                        outbox.id = folder.insertFolder(outbox);
                    }

                    // Build outgoing message
                    EntityMessage out = new EntityMessage();
                    out.folder = outbox.id;
                    out.identity = draft.identity;
                    out.replying = draft.replying;
                    out.thread = draft.thread;
                    out.from = draft.from;
                    out.to = draft.to;
                    out.cc = draft.cc;
                    out.bcc = draft.bcc;
                    out.subject = draft.subject;
                    out.body = draft.body;
                    out.received = draft.received;
                    out.seen = draft.seen;
                    out.ui_seen = draft.ui_seen;
                    out.ui_hide = false;
                    out.id = message.insertMessage(out);

                    EntityOperation.queue(getContext(), out, EntityOperation.SEND);
                    EntityOperation.queue(getContext(), draft, EntityOperation.DELETE);
                } else
                    EntityOperation.queue(getContext(), draft, EntityOperation.ADD);

                return null;
            } catch (Throwable ex) {
                Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                return ex;
            }
        }
    }

    private LoaderManager.LoaderCallbacks putLoaderCallbacks = new LoaderManager.LoaderCallbacks<Throwable>() {
        private Bundle args;

        @NonNull
        @Override
        public Loader<Throwable> onCreateLoader(int id, Bundle args) {
            this.args = args;
            PutLoader loader = new PutLoader(getContext());
            loader.setArgs(args);
            return loader;
        }

        @Override
        public void onLoadFinished(@NonNull Loader<Throwable> loader, Throwable ex) {
            getLoaderManager().destroyLoader(loader.getId());

            boolean send = args.getBoolean("send", false);
            Log.i(Helper.TAG, "Put finished send=" + send + " ex=" + ex);

            if (ex == null) {
                getFragmentManager().popBackStack();
                Toast.makeText(getContext(), send ? R.string.title_queued : R.string.title_draft_saved, Toast.LENGTH_LONG).show();
            } else {
                Log.w(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                Toast.makeText(getContext(), Helper.formatThrowable(ex), Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onLoaderReset(@NonNull Loader<Throwable> loader) {
        }
    };
}