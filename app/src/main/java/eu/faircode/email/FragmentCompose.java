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

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.text.Html;
import android.text.TextUtils;
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

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;

import static android.app.Activity.RESULT_OK;

public class FragmentCompose extends FragmentEx {
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
        setSubtitle(R.string.title_compose);

        View view = inflater.inflate(R.layout.fragment_compose, container, false);

        // Get arguments
        Bundle args = getArguments();
        String action = (args == null ? null : args.getString("action"));
        final long id = (TextUtils.isEmpty(action) ? (args == null ? -1 : args.getLong("id", -1)) : -1);

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
                switch (item.getItemId()) {
                    case R.id.action_trash:
                        actionPut(id, "trash");
                        return true;
                    case R.id.action_save:
                        actionPut(id, "save");
                        return true;
                    case R.id.action_send:
                        actionPut(id, "send");
                        return true;
                }

                return false;
            }
        });

        setHasOptionsMenu(true);

        // Initialize
        spFrom.setVisibility(View.GONE);
        ivIdentityAdd.setVisibility(View.GONE);
        grpCc.setVisibility(View.GONE);
        grpReady.setVisibility(View.GONE);
        pbWait.setVisibility(View.VISIBLE);
        bottom_navigation.getMenu().setGroupEnabled(0, false);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        DB.getInstance(getContext()).identity().liveIdentities(true).observe(getViewLifecycleOwner(), new Observer<List<EntityIdentity>>() {
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

                // Select primary identity
                for (int pos = 0; pos < identities.size(); pos++)
                    if (identities.get(pos).primary) {
                        spFrom.setSelection(pos);
                        break;
                    }

                spFrom.setVisibility(View.VISIBLE);
                ivIdentityAdd.setVisibility(View.VISIBLE);

                // Get might select another identity
                LoaderManager.getInstance(FragmentCompose.this)
                        .restartLoader(ActivityCompose.LOADER_COMPOSE_GET, getArguments(), getLoaderCallbacks).forceLoad();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_message, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_address:
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

    private void actionPut(long id, String action) {
        Log.i(Helper.TAG, "Put id=" + id + " action=" + action);
        bottom_navigation.getMenu().setGroupEnabled(0, false);

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
        args.putString("action", action);

        LoaderManager.getInstance(this)
                .restartLoader(ActivityCompose.LOADER_COMPOSE_PUT, args, putLoaderCallbacks).forceLoad();
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

                result.putString("action", action);

                EntityMessage msg = DB.getInstance(getContext()).message().getMessage(id);
                if (msg != null) {
                    if (msg.identity != null)
                        result.putLong("iid", msg.identity);
                    if (msg.replying != null)
                        result.putLong("rid", msg.replying);
                    result.putSerializable("cc", msg.cc);
                    result.putSerializable("bcc", msg.bcc);
                    result.putString("thread", msg.thread);
                    result.putString("subject", msg.subject);
                    result.putString("body", msg.body);
                }

                if (TextUtils.isEmpty(action)) {
                    if (msg != null) {
                        result.putSerializable("from", msg.from);
                        result.putSerializable("to", msg.to);
                    }
                } else if ("reply".equals(action)) {
                    Address[] to = null;
                    if (msg != null)
                        to = (msg.reply == null || msg.reply.length == 0 ? msg.from : msg.reply);
                    result.putLong("rid", msg.id);
                    result.putSerializable("from", msg.to);
                    result.putSerializable("to", to);
                } else if ("reply_all".equals(action)) {
                    Address[] to = null;
                    if (msg != null) {
                        List<Address> addresses = new ArrayList<>();
                        if (msg.reply != null)
                            addresses.addAll(Arrays.asList(msg.reply));
                        else if (msg.from != null)
                            addresses.addAll(Arrays.asList(msg.from));
                        if (msg.cc != null)
                            addresses.addAll(Arrays.asList(msg.cc));
                        to = addresses.toArray(new Address[0]);
                    }
                    result.putLong("rid", msg.id);
                    result.putSerializable("from", msg.to);
                    result.putSerializable("to", to);
                } else if ("forward".equals(action)) {
                    Address[] to = null;
                    if (msg != null)
                        to = (msg.reply == null || msg.reply.length == 0 ? msg.from : msg.reply);
                    result.putSerializable("from", msg.to);
                    result.putSerializable("to", to);
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
        public void onLoadFinished(@NonNull Loader<Bundle> loader, Bundle result) {
            LoaderManager.getInstance(FragmentCompose.this).destroyLoader(loader.getId());

            long iid = result.getLong("iid", -1);
            long rid = result.getLong("rid", -1);
            String thread = result.getString("thread");
            Address[] from = (Address[]) result.getSerializable("from");
            Address[] to = (Address[]) result.getSerializable("to");
            Address[] cc = (Address[]) result.getSerializable("cc");
            Address[] bcc = (Address[]) result.getSerializable("bcc");
            String subject = result.getString("subject");
            String body = result.getString("body");
            String action = result.getString("action");

            pbWait.setVisibility(View.GONE);
            grpCc.setVisibility("reply_all".equals(action) ? View.VISIBLE : View.GONE);
            grpReady.setVisibility(View.VISIBLE);

            FragmentCompose.this.thread = thread;
            FragmentCompose.this.rid = rid;

            ArrayAdapter adapter = (ArrayAdapter) spFrom.getAdapter();
            if (adapter != null) {
                for (int pos = 0; pos < adapter.getCount(); pos++) {
                    EntityIdentity identity = (EntityIdentity) adapter.getItem(pos);
                    if (iid < 0
                            ? from != null && from.length > 0 && ((InternetAddress) from[0]).getAddress().equals(identity.email)
                            : iid == identity.id) {
                        spFrom.setSelection(pos);
                        break;
                    }
                }
            }

            if (!once) {
                // Prevent changed fields from being overwritten
                once = true;

                Handler handler = new Handler();

                etCc.setText(cc == null ? null : TextUtils.join(", ", cc));
                etBcc.setText(bcc == null ? null : TextUtils.join(", ", bcc));

                if (action == null) {
                    etTo.setText(to == null ? null : TextUtils.join(", ", to));
                    etSubject.setText(subject);
                    if (body != null)
                        etBody.setText(Html.fromHtml(HtmlHelper.sanitize(getContext(), body, false)));
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            etTo.requestFocus();
                        }
                    });
                } else if ("reply".equals(action) || "reply_all".equals(action)) {
                    etTo.setText(to == null ? null : TextUtils.join(", ", to));
                    String text = String.format("<br><br>%s %s:<br><br>%s",
                            Html.escapeHtml(new Date().toString()),
                            Html.escapeHtml(to == null ? "" : TextUtils.join(", ", to)),
                            HtmlHelper.sanitize(getContext(), body, true));
                    etSubject.setText(getContext().getString(R.string.title_subject_reply, subject));
                    etBody.setText(Html.fromHtml(text));
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            etBody.requestFocus();
                        }
                    });
                } else if ("forward".equals(action)) {
                    String text = String.format("<br><br>%s %s:<br><br>%s",
                            Html.escapeHtml(new Date().toString()),
                            Html.escapeHtml(to == null ? "" : TextUtils.join(", ", to)),
                            HtmlHelper.sanitize(getContext(), body, true));
                    etSubject.setText(getContext().getString(R.string.title_subject_forward, subject));
                    etBody.setText(Html.fromHtml(text));
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            etTo.requestFocus();
                        }
                    });
                }
            }

            bottom_navigation.getMenu().setGroupEnabled(0, true);
        }

        @Override
        public void onLoaderReset(@NonNull Loader<Bundle> loader) {
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
            try {
                long id = args.getLong("id");
                String action = args.getString("action");
                Log.i(Helper.TAG, "Put load id=" + id + " action=" + action);

                DB db = DB.getInstance(getContext());
                DaoMessage message = db.message();
                DaoIdentity identity = db.identity();
                DaoFolder folder = db.folder();

                // Get data
                EntityMessage draft = message.getMessage(id);
                EntityIdentity ident = identity.getIdentity(args.getLong("iid"));
                if (ident == null)
                    throw new IllegalArgumentException(getContext().getString(R.string.title_from_missing));

                EntityFolder drafts = EntityFolder.getDrafts(getContext(), db, ident.account);

                long rid = args.getLong("rid", -1);
                String thread = args.getString("thread");
                String to = args.getString("to");
                String cc = args.getString("cc");
                String bcc = args.getString("bcc");
                String body = args.getString("body");
                String subject = args.getString("subject");

                Address afrom[] = (ident == null ? null : new Address[]{new InternetAddress(ident.email, ident.name)});
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
                draft.from = afrom;
                draft.to = ato;
                draft.cc = acc;
                draft.bcc = abcc;
                draft.subject = subject;
                draft.body = "<pre>" + body.replaceAll("\\r?\\n", "<br />") + "</pre>";
                draft.received = new Date().getTime();
                draft.seen = false;
                draft.ui_seen = false;
                draft.ui_hide = false;

                // Store draft
                if (!update)
                    draft.id = message.insertMessage(draft);

                // Check data
                try {
                    db.beginTransaction();


                    if ("save".equals(action)) {
                        // Delete previous draft
                        draft.ui_hide = true;
                        db.message().updateMessage(draft);
                        EntityOperation.queue(db, draft, EntityOperation.DELETE);

                        // Create new draft
                        draft.id = null;
                        draft.uid = null;
                        draft.ui_hide = false;
                        draft.id = db.message().insertMessage(draft);
                        EntityOperation.queue(db, draft, EntityOperation.ADD);

                    } else if ("trash".equals(action)) {
                        EntityFolder trash = db.folder().getFolderByType(ident.account, EntityFolder.TRASH);

                        boolean move = (draft.uid != null);
                        if (move)
                            EntityOperation.queue(db, draft, EntityOperation.MOVE, trash.id, draft.uid);

                        draft.folder = trash.id;
                        draft.uid = null;
                        db.message().updateMessage(draft);

                        if (!move)
                            EntityOperation.queue(db, draft, EntityOperation.ADD);

                    } else if ("send".equals(action)) {
                        if (draft.to == null && draft.cc == null && draft.bcc == null)
                            throw new IllegalArgumentException(getContext().getString(R.string.title_to_missing));

                        // Delete draft (cannot move to outbox)
                        draft.ui_hide = true;
                        db.message().updateMessage(draft);
                        EntityOperation.queue(db, draft, EntityOperation.DELETE);

                        // Copy message to outbox
                        draft.id = null;
                        draft.folder = folder.getOutbox().id;
                        draft.uid = null;
                        draft.ui_hide = false;
                        draft.id = db.message().insertMessage(draft);

                        EntityOperation.queue(db, draft, EntityOperation.SEND);
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                EntityOperation.process(getContext());

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
            LoaderManager.getInstance(FragmentCompose.this).destroyLoader(loader.getId());

            String action = args.getString("action");
            Log.i(Helper.TAG, "Put finished action=" + action + " ex=" + ex);

            bottom_navigation.getMenu().setGroupEnabled(0, true);

            if (ex == null) {
                if ("trash".equals(action)) {
                    getFragmentManager().popBackStack();
                    Toast.makeText(getContext(), R.string.title_draft_trashed, Toast.LENGTH_LONG).show();
                } else if ("save".equals(action))
                    Toast.makeText(getContext(), R.string.title_draft_saved, Toast.LENGTH_LONG).show();
                else if ("send".equals(action)) {
                    getFragmentManager().popBackStack();
                    Toast.makeText(getContext(), R.string.title_queued, Toast.LENGTH_LONG).show();
                }
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