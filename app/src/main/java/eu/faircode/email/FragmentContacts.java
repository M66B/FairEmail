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

    Copyright 2018-2022 by Marcel Bokhorst (M66B)
*/

import static android.app.Activity.RESULT_OK;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.Group;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.Observer;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.io.text.VCardReader;
import ezvcard.io.text.VCardWriter;
import ezvcard.property.Email;
import ezvcard.property.FormattedName;
import ezvcard.property.RawProperty;

public class FragmentContacts extends FragmentBase {
    private View view;
    private RecyclerView rvContacts;
    private ContentLoadingProgressBar pbWait;
    private Group grpReady;
    private FloatingActionButton fabAdd;

    private Long account = null;
    private boolean junk = false;
    private String searching = null;
    private long selected_account;

    private AdapterContact adapter;

    private static final int REQUEST_FILTER = 1;
    private static final int REQUEST_ACCOUNT = 2;
    private static final int REQUEST_IMPORT = 3;
    private static final int REQUEST_EXPORT = 4;
    static final int REQUEST_EDIT_ACCOUNT = 5;
    static final int REQUEST_EDIT_CONTACT = 6;

    private static final String VCF_TYPE = "X-FAIREMAIL-TYPE";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args == null)
            args = new Bundle();

        this.account = (args.containsKey("account") ? args.getLong("account") : null);
        this.junk = args.getBoolean("junk");
    }

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(junk ? R.string.title_blocked_senders : R.string.menu_contacts);
        setHasOptionsMenu(true);

        view = inflater.inflate(R.layout.fragment_contacts, container, false);

        // Get controls
        rvContacts = view.findViewById(R.id.rvContacts);
        pbWait = view.findViewById(R.id.pbWait);
        grpReady = view.findViewById(R.id.grpReady);
        fabAdd = view.findViewById(R.id.fabAdd);

        // Wire controls

        rvContacts.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rvContacts.setLayoutManager(llm);

        adapter = new AdapterContact(this);
        rvContacts.setAdapter(adapter);

        // Initialize
        grpReady.setVisibility(View.GONE);
        pbWait.setVisibility(View.VISIBLE);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putLong("fair:account", account == null ? -1L : account);
        outState.putBoolean("fair:junk", junk);
        outState.putString("fair:searching", searching);
        outState.putLong("fair:selected_account", selected_account);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            account = savedInstanceState.getLong("fair:account");
            junk = savedInstanceState.getBoolean("fair:junk");
            searching = savedInstanceState.getString("fair:searching");
            selected_account = savedInstanceState.getLong("fair:selected_account");

            if (account < 0)
                account = null;
        }

        adapter.filter(account, junk);
        adapter.search(searching);

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (account == null) {
                    FragmentDialogSelectAccount fragment = new FragmentDialogSelectAccount();
                    fragment.setArguments(new Bundle());
                    fragment.setTargetFragment(FragmentContacts.this, REQUEST_EDIT_ACCOUNT);
                    fragment.show(getParentFragmentManager(), "contact:account");
                } else
                    onAdd(account);
            }
        });

        final Context context = getContext();
        DB db = DB.getInstance(context);

        db.contact().liveContacts(null).observe(getViewLifecycleOwner(), new Observer<List<TupleContactEx>>() {
            @Override
            public void onChanged(List<TupleContactEx> contacts) {
                if (contacts == null)
                    contacts = new ArrayList<>();

                adapter.set(contacts);

                pbWait.setVisibility(View.GONE);
                grpReady.setVisibility(View.VISIBLE);
            }
        });

        Shortcuts.update(context, getViewLifecycleOwner());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_contacts, menu);

        MenuItem menuSearch = menu.findItem(R.id.menu_search);
        SearchView searchView = (SearchView) menuSearch.getActionView();
        searchView.setQueryHint(getString(R.string.title_search));

        final String search = searching;
        view.post(new RunnableEx("contacts:search") {
            @Override
            public void delegate() {
                if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                    return;

                if (TextUtils.isEmpty(search))
                    menuSearch.collapseActionView();
                else {
                    menuSearch.expandActionView();
                    searchView.setQuery(search, true);
                }
            }
        });

        getViewLifecycleOwner().getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            public void onDestroyed() {
                menuSearch.collapseActionView();
                getViewLifecycleOwner().getLifecycle().removeObserver(this);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                    searching = newText;
                    adapter.search(newText);
                }
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                    searching = query;
                    adapter.search(query);
                }
                return true;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.menu_junk).setChecked(junk);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_help) {
            onMenuHelp();
            return true;
        } else if (itemId == R.id.menu_junk) {
            item.setChecked(!item.isChecked());
            onMenuJunk(item.isChecked());
            return true;
        } else if (itemId == R.id.menu_account) {
            onMenuAccount();
            return true;
        } else if (itemId == R.id.menu_import) {
            onMenuVcard(false);
            return true;
        } else if (itemId == R.id.menu_export) {
            onMenuVcard(true);
            return true;
        } else if (itemId == R.id.menu_delete_all) {
            onMenuDeleteAll();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onMenuHelp() {
        Helper.viewFAQ(getContext(), 84);
    }

    private void onMenuJunk(boolean junk) {
        this.junk = junk;
        setSubtitle(junk ? R.string.title_blocked_senders : R.string.menu_contacts);
        adapter.filter(account, junk);
    }

    private void onMenuAccount() {
        FragmentDialogSelectAccount fragment = new FragmentDialogSelectAccount();
        fragment.setArguments(new Bundle());
        fragment.setTargetFragment(this, REQUEST_FILTER);
        fragment.show(getParentFragmentManager(), "contacts:select");
    }

    private void onMenuVcard(boolean export) {
        Bundle args = new Bundle();
        args.putBoolean("export", export);
        if (account == null) {
            FragmentDialogSelectAccount fragment = new FragmentDialogSelectAccount();
            fragment.setArguments(args);
            fragment.setTargetFragment(this, REQUEST_ACCOUNT);
            fragment.show(getParentFragmentManager(), "contacts:vcard");
        } else {
            args.putLong("account", account);
            onAccountSelected(args);
        }
    }

    private void onMenuDeleteAll() {
        Bundle args = new Bundle();
        args.putLong("account", account == null ? -1L : account);
        args.putBoolean("junk", junk);

        FragmentDelete fragment = new FragmentDelete();
        fragment.setArguments(args);
        fragment.show(getParentFragmentManager(), "contacts:delete");
    }

    private void onAdd(long account) {
        Bundle args = new Bundle();
        args.putInt("type", junk ? EntityContact.TYPE_JUNK : EntityContact.TYPE_TO);
        args.putLong("account", account);

        FragmentDialogEditContact fragment = new FragmentDialogEditContact();
        fragment.setArguments(args);
        fragment.setTargetFragment(this, REQUEST_EDIT_CONTACT);
        fragment.show(getParentFragmentManager(), "contacts:add");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            switch (requestCode) {
                case REQUEST_FILTER:
                    if (resultCode == RESULT_OK && data != null)
                        onAccountFilter(data.getBundleExtra("args"));
                    break;
                case REQUEST_ACCOUNT:
                    if (resultCode == RESULT_OK && data != null)
                        onAccountSelected(data.getBundleExtra("args"));
                    break;
                case REQUEST_IMPORT:
                    if (resultCode == RESULT_OK && data != null)
                        handleImport(data);
                    break;
                case REQUEST_EXPORT:
                    if (resultCode == RESULT_OK && data != null)
                        handleExport(data);
                    break;
                case REQUEST_EDIT_ACCOUNT:
                    if (resultCode == RESULT_OK && data != null)
                        onAdd(data.getBundleExtra("args").getLong("account"));
                    break;
                case REQUEST_EDIT_CONTACT:
                    if (resultCode == RESULT_OK && data != null)
                        onEditContact(data.getBundleExtra("args"));
                    break;
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    private void onAccountFilter(Bundle args) {
        account = args.getLong("account");
        adapter.filter(account, junk);
    }

    private void onAccountSelected(Bundle args) {
        selected_account = args.getLong("account");
        boolean export = args.getBoolean("export");

        final Context context = getContext();
        PackageManager pm = context.getPackageManager();

        if (export) {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_TITLE, "fairemail.vcf");
            Helper.openAdvanced(intent);
            startActivityForResult(Helper.getChooser(context, intent), REQUEST_EXPORT);
        } else {
            Intent open = new Intent(Intent.ACTION_GET_CONTENT);
            open.addCategory(Intent.CATEGORY_OPENABLE);
            open.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            open.setType("*/*");
            if (open.resolveActivity(pm) == null)  // system whitelisted
                ToastEx.makeText(context, R.string.title_no_saf, Toast.LENGTH_LONG).show();
            else
                startActivityForResult(Helper.getChooser(context, open), REQUEST_IMPORT);
        }
    }

    private void handleImport(Intent data) {
        Uri uri = data.getData();

        Bundle args = new Bundle();
        args.putParcelable("uri", uri);
        args.putLong("account", selected_account);

        new SimpleTask<Void>() {
            private Toast toast = null;

            @Override
            protected void onPreExecute(Bundle args) {
                toast = ToastEx.makeText(getContext(), R.string.title_executing, Toast.LENGTH_LONG);
                toast.show();
            }

            @Override
            protected void onPostExecute(Bundle args) {
                if (toast != null)
                    toast.cancel();
            }

            @Override
            protected Void onExecute(Context context, Bundle args) throws Throwable {
                Uri uri = args.getParcelable("uri");
                long account = args.getLong("account");

                NoStreamException.check(uri, context);

                long now = new Date().getTime();

                EntityLog.log(context, "Importing " + uri +
                        " junk=" + junk + " account=" + account);

                int count = 0;
                ContentResolver resolver = context.getContentResolver();
                InputStream is = resolver.openInputStream(uri);
                if (is == null)
                    throw new FileNotFoundException(uri.toString());
                try (InputStream bis = new BufferedInputStream(is)) {
                    VCardReader reader = new VCardReader(bis);
                    VCard vcard;
                    while ((vcard = reader.readNext()) != null) {
                        Integer type = null;
                        RawProperty xtype = vcard.getExtendedProperty(VCF_TYPE);
                        if (xtype != null)
                            type = Helper.parseInt(xtype.getValue());
                        if (type == null)
                            type = EntityContact.TYPE_TO;

                        List<Email> emails = vcard.getEmails();
                        if (emails == null)
                            continue;

                        FormattedName fn = vcard.getFormattedName();
                        String name = (fn == null ? null : fn.getValue());

                        List<String> categories = new ArrayList<>();
                        if (vcard.getCategories() != null)
                            categories.addAll(vcard.getCategories().getValues());
                        String group = (categories.size() < 1 ? null : categories.get(0));

                        List<Address> addresses = new ArrayList<>();
                        for (Email email : emails) {
                            String address = email.getValue();
                            if (address == null)
                                continue;
                            addresses.add(new InternetAddress(address, name, StandardCharsets.UTF_8.name()));
                        }

                        EntityContact.update(context,
                                account,
                                null,
                                addresses.toArray(new Address[0]),
                                group,
                                type,
                                now);

                        count += addresses.size();
                    }
                }

                Log.i("Imported contacts=" + count);

                return null;
            }

            @Override
            protected void onExecuted(Bundle args, Void data) {
                ToastEx.makeText(getContext(), R.string.title_completed, Toast.LENGTH_LONG).show();
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
                if (ex instanceof NoStreamException)
                    ((NoStreamException) ex).report(getActivity());
                else
                    Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "contacts:import");
    }

    private void handleExport(Intent data) {
        Bundle args = new Bundle();
        args.putParcelable("uri", data.getData());
        args.putBoolean("junk", junk);
        args.putLong("account", selected_account);

        new SimpleTask<Void>() {
            private Toast toast = null;

            @Override
            protected void onPreExecute(Bundle args) {
                toast = ToastEx.makeText(getContext(), R.string.title_executing, Toast.LENGTH_LONG);
                toast.show();
            }

            @Override
            protected void onPostExecute(Bundle args) {
                if (toast != null)
                    toast.cancel();
            }

            @Override
            protected Void onExecute(Context context, Bundle args) throws Throwable {
                Uri uri = args.getParcelable("uri");
                boolean junk = args.getBoolean("junk");
                long account = args.getLong("account");

                if (uri == null)
                    throw new FileNotFoundException();

                EntityLog.log(context, "Exporting " + uri +
                        " junk=" + junk + " account=" + account);

                if (!"content".equals(uri.getScheme())) {
                    Log.w("Export uri=" + uri);
                    throw new IllegalArgumentException(context.getString(R.string.title_no_stream));
                }

                List<Integer> types = new ArrayList<>();
                if (junk) {
                    types.add(EntityContact.TYPE_JUNK);
                    types.add(EntityContact.TYPE_NO_JUNK);
                } else {
                    types.add(EntityContact.TYPE_TO);
                    types.add(EntityContact.TYPE_FROM);
                }

                List<VCard> vcards = new ArrayList<>();

                DB db = DB.getInstance(context);
                List<EntityContact> contacts = db.contact().getContacts(account);
                for (EntityContact contact : contacts)
                    if (contact.account.equals(account) &&
                            types.contains(contact.type)) {
                        VCard vcard = new VCard();
                        vcard.addExtendedProperty(VCF_TYPE, Integer.toString(contact.type));
                        vcard.addEmail(contact.email);
                        if (!TextUtils.isEmpty(contact.name))
                            vcard.setFormattedName(contact.name);
                        if (!TextUtils.isEmpty(contact.group))
                            vcard.setCategories(contact.group);
                        vcards.add(vcard);
                    }

                ContentResolver resolver = context.getContentResolver();
                try (OutputStream os = resolver.openOutputStream(uri)) {
                    if (os == null)
                        throw new FileNotFoundException(uri.toString());
                    try (VCardWriter writer = new VCardWriter(os, VCardVersion.V3_0)) {
                        for (VCard vcard : vcards)
                            writer.write(vcard);
                    }
                }

                EntityLog.log(context, "Exported contact=" + vcards.size() + "/" + contacts.size());

                return null;
            }

            @Override
            protected void onExecuted(Bundle args, Void data) {
                ToastEx.makeText(getContext(), R.string.title_completed, Toast.LENGTH_LONG).show();
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
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "contacts:export");
    }

    private void onEditContact(Bundle args) {
        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) {
                long id = args.getLong("id");
                long account = args.getLong("account");
                int type = args.getInt("type");
                String email = args.getString("email");
                String name = args.getString("name");
                String group = args.getString("group");

                if (TextUtils.isEmpty(email))
                    throw new IllegalArgumentException(context.getString(R.string.title_no_email));
                if (!Helper.EMAIL_ADDRESS.matcher(email).matches())
                    throw new IllegalArgumentException(context.getString(R.string.title_email_invalid, email));
                if (TextUtils.isEmpty(name))
                    name = null;
                if (TextUtils.isEmpty(group))
                    group = null;

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    boolean update = false;
                    EntityContact contact = db.contact().getContact(id);
                    EntityContact existing = db.contact().getContact(account, type, email);

                    if (contact == null) {
                        if (existing == null)
                            contact = new EntityContact();
                        else {
                            update = true;
                            contact = existing;
                        }
                    } else {
                        update = true;
                        if (existing != null && !existing.id.equals(contact.id))
                            db.contact().deleteContact(existing.id);
                    }

                    contact.account = account;
                    contact.type = type;
                    contact.email = email;
                    contact.name = name;
                    contact.group = group;
                    contact.times_contacted = 0;
                    contact.first_contacted = new Date().getTime();
                    contact.last_contacted = contact.first_contacted;

                    if (update)
                        db.contact().updateContact(contact);
                    else
                        contact.id = db.contact().insertContact(contact);

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                return null;
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (ex instanceof IllegalArgumentException)
                    ToastEx.makeText(getContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                else
                    Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "contacts:name");
    }

    public static class FragmentDelete extends FragmentDialogBase {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            return new AlertDialog.Builder(getContext())
                    .setIcon(R.drawable.twotone_warning_24)
                    .setTitle(getString(R.string.title_delete_contacts))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new SimpleTask<Void>() {
                                @Override
                                protected Void onExecute(Context context, Bundle args) {
                                    Long account = args.getLong("account");
                                    boolean junk = args.getBoolean("junk");

                                    if (account < 0)
                                        account = null;
                                    int[] types = (junk
                                            ? new int[]{EntityContact.TYPE_JUNK, EntityContact.TYPE_NO_JUNK}
                                            : new int[]{EntityContact.TYPE_FROM, EntityContact.TYPE_TO});

                                    DB db = DB.getInstance(context);
                                    int count = db.contact().clearContacts(account, types);
                                    Log.i("Cleared contacts=" + count);
                                    return null;
                                }

                                @Override
                                protected void onException(Bundle args, Throwable ex) {
                                    Log.unexpectedError(getParentFragmentManager(), ex);
                                }
                            }.execute(getContext(), getActivity(), getArguments(), "contacts:delete");
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
        }
    }

    public static class FragmentDialogEditContact extends FragmentDialogBase {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            final Context context = getContext();
            View view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_contact, null);
            final Spinner spType = view.findViewById(R.id.spType);
            final EditText etEmail = view.findViewById(R.id.etEmail);
            final EditText etName = view.findViewById(R.id.etName);
            final EditText etGroup = view.findViewById(R.id.etGroup);

            final Bundle args = getArguments();
            int type = args.getInt("type");

            boolean junk = (type == EntityContact.TYPE_JUNK || type == EntityContact.TYPE_NO_JUNK);
            String[] values = getResources().getStringArray(R.array.contactTypes);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.spinner_item1, android.R.id.text1, values) {
                @Override
                public boolean isEnabled(int position) {
                    if (junk)
                        return (position == EntityContact.TYPE_JUNK || position == EntityContact.TYPE_NO_JUNK);
                    else
                        return (position == EntityContact.TYPE_TO || position == EntityContact.TYPE_FROM);
                }

                @Override
                public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    View view = super.getDropDownView(position, convertView, parent);
                    TextView tv = view.findViewById(android.R.id.text1);
                    tv.setEnabled(isEnabled(position));
                    return view;
                }
            };
            adapter.setDropDownViewResource(R.layout.spinner_item1_dropdown);
            spType.setAdapter(adapter);

            spType.setSelection(args.getInt("type"));
            etEmail.setText(args.getString("email"));
            etName.setText(args.getString("name"));
            etGroup.setText(args.getString("group"));

            return new AlertDialog.Builder(context)
                    .setView(view)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            args.putInt("type", spType.getSelectedItemPosition());
                            args.putString("email", etEmail.getText().toString().trim());
                            args.putString("name", etName.getText().toString());
                            args.putString("group", etGroup.getText().toString().trim());
                            sendResult(RESULT_OK);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
        }
    }
}
