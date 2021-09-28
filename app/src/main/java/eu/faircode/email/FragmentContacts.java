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

    Copyright 2018-2021 by Marcel Bokhorst (M66B)
*/

import static android.app.Activity.RESULT_OK;

import android.Manifest;
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
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.Group;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.io.text.VCardReader;
import ezvcard.property.Email;
import ezvcard.property.FormattedName;

public class FragmentContacts extends FragmentBase {
    private RecyclerView rvContacts;
    private ContentLoadingProgressBar pbWait;
    private Group grpReady;

    private long account;
    private boolean junk = false;
    private String searching = null;
    private AdapterContact adapter;

    private static final int REQUEST_ACCOUNT = 1;
    private static final int REQUEST_IMPORT = 2;

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.menu_contacts);
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_contacts, container, false);

        // Get controls
        rvContacts = view.findViewById(R.id.rvContacts);
        pbWait = view.findViewById(R.id.pbWait);
        grpReady = view.findViewById(R.id.grpReady);

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
        outState.putLong("fair:account", account);
        outState.putBoolean("fair:junk", junk);
        outState.putString("fair:searching", searching);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            account = savedInstanceState.getLong("fair:account");
            junk = savedInstanceState.getBoolean("fair:junk");
            searching = savedInstanceState.getString("fair:searching");
        }
        onMenuJunk(junk);
        adapter.search(searching);

        DB db = DB.getInstance(getContext());
        db.contact().liveContacts().observe(getViewLifecycleOwner(), new Observer<List<TupleContactEx>>() {
            @Override
            public void onChanged(List<TupleContactEx> contacts) {
                if (contacts == null)
                    contacts = new ArrayList<>();

                adapter.set(contacts);

                pbWait.setVisibility(View.GONE);
                grpReady.setVisibility(View.VISIBLE);
            }
        });

        Shortcuts.update(getContext(), getViewLifecycleOwner());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_contacts, menu);

        MenuItem menuSearch = menu.findItem(R.id.menu_search);
        SearchView searchView = (SearchView) menuSearch.getActionView();
        searchView.setQueryHint(getString(R.string.title_search));

        if (!TextUtils.isEmpty(searching)) {
            menuSearch.expandActionView();
            searchView.setQuery(searching, true);
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                if (getView() != null) {
                    searching = newText;
                    adapter.search(newText);
                }
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                searching = query;
                adapter.search(query);
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
        } else if (itemId == R.id.menu_import) {
            onMenuImport();
            return true;
        } else if (itemId == R.id.menu_delete) {
            onMenuDelete();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onMenuHelp() {
        Helper.viewFAQ(getContext(), 84);
    }

    private void onMenuJunk(boolean junk) {
        this.junk = junk;
        adapter.filter(junk
                ? Arrays.asList(EntityContact.TYPE_JUNK, EntityContact.TYPE_NO_JUNK)
                : new ArrayList<>());
    }

    private void onMenuImport() {
        FragmentDialogSelectAccount fragment = new FragmentDialogSelectAccount();
        fragment.setArguments(new Bundle());
        fragment.setTargetFragment(this, REQUEST_ACCOUNT);
        fragment.show(getParentFragmentManager(), "messages:accounts");
    }

    private void onMenuDelete() {
        new FragmentDelete().show(getParentFragmentManager(), "contacts:delete");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            switch (requestCode) {
                case REQUEST_ACCOUNT:
                    if (resultCode == RESULT_OK && data != null)
                        onAccountSelected(data.getBundleExtra("args"));
                    break;
                case REQUEST_IMPORT:
                    if (resultCode == RESULT_OK && data != null)
                        handleImport(data);
                    break;
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    private void onAccountSelected(Bundle args) {
        account = args.getLong("account");

        final Context context = getContext();
        PackageManager pm = context.getPackageManager();

        Intent open = new Intent(Intent.ACTION_GET_CONTENT);
        open.addCategory(Intent.CATEGORY_OPENABLE);
        open.setType("*/*");
        if (open.resolveActivity(pm) == null)  // system whitelisted
            ToastEx.makeText(context, R.string.title_no_saf, Toast.LENGTH_LONG).show();
        else
            startActivityForResult(Helper.getChooser(context, open), REQUEST_IMPORT);
    }

    private void handleImport(Intent data) {
        Uri uri = data.getData();

        Bundle args = new Bundle();
        args.putParcelable("uri", uri);
        args.putLong("account", account);

        new SimpleTask<Void>() {
            @Override
            protected void onPreExecute(Bundle args) {
                ToastEx.makeText(getContext(), R.string.title_executing, Toast.LENGTH_LONG).show();
            }

            @Override
            protected Void onExecute(Context context, Bundle args) throws Throwable {
                Uri uri = args.getParcelable("uri");
                long account = args.getLong("account");

                if (uri == null)
                    throw new FileNotFoundException();

                if (!"content".equals(uri.getScheme()) &&
                        !Helper.hasPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Log.w("Import uri=" + uri);
                    throw new IllegalArgumentException(context.getString(R.string.title_no_stream));
                }

                long now = new Date().getTime();
                DB db = DB.getInstance(context);
                List<EntityAccount> accounts = db.account().getSynchronizingAccounts();

                Log.i("Reading URI=" + uri);
                ContentResolver resolver = context.getContentResolver();
                try (InputStream is = new BufferedInputStream(resolver.openInputStream(uri))) {
                    VCardReader reader = new VCardReader(is);
                    VCard vcard;
                    while ((vcard = reader.readNext()) != null) {
                        List<Email> emails = vcard.getEmails();
                        if (emails == null)
                            continue;

                        FormattedName fn = vcard.getFormattedName();
                        String name = (fn == null) ? null : fn.getValue();

                        List<Address> addresses = new ArrayList<>();
                        for (Email email : emails) {
                            String address = email.getValue();
                            if (address == null)
                                continue;
                            addresses.add(new InternetAddress(address, name, StandardCharsets.UTF_8.name()));
                        }

                        EntityContact.update(context,
                                account,
                                addresses.toArray(new Address[0]),
                                EntityContact.TYPE_TO,
                                now);
                    }
                }

                Log.i("Imported contacts");

                return null;
            }

            @Override
            protected void onExecuted(Bundle args, Void data) {
                ToastEx.makeText(getContext(), R.string.title_completed, Toast.LENGTH_LONG).show();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "setup:import");
    }

    public static class FragmentDelete extends FragmentDialogBase {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            return new AlertDialog.Builder(getContext())
                    .setMessage(getString(R.string.title_delete_contacts))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new SimpleTask<Void>() {
                                @Override
                                protected Void onExecute(Context context, Bundle args) {
                                    DB db = DB.getInstance(context);
                                    int count = db.contact().clearContacts();
                                    Log.i("Cleared contacts=" + count);
                                    return null;
                                }

                                @Override
                                protected void onException(Bundle args, Throwable ex) {
                                    Log.unexpectedError(getParentFragmentManager(), ex);
                                }
                            }.execute(getContext(), getActivity(), new Bundle(), "contacts:delete");
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
        }
    }
}
