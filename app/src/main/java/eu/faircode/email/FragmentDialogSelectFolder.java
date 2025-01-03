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

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.Group;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.mail.internet.InternetAddress;

public class FragmentDialogSelectFolder extends FragmentDialogBase {
    private int result = 0;
    private AdapterFolder adapter;
    private LinearLayoutManager llm;

    private static final int MAX_SELECTED_FOLDERS = 5;
    private static final int REQUEST_FOLDER_NAME = 1;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Bundle aargs = getArguments();
        final int icon = aargs.getInt("icon", R.drawable.twotone_folder_open_24);
        final String title = aargs.getString("title");
        final long account = aargs.getLong("account");
        final long[] disabled = aargs.getLongArray("disabled");
        final boolean cancopy = aargs.getBoolean("cancopy");

        long[] messages = null;
        if (aargs.containsKey("message"))
            messages = new long[]{aargs.getLong("message")};
        else if (aargs.containsKey("messages"))
            messages = aargs.getLongArray("messages");

        final Context context = getContext();
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final InputMethodManager imm = Helper.getSystemService(context, InputMethodManager.class);

        List<String> selected_folders = new ArrayList<>();
        String json = prefs.getString("selected_folders", "[]");
        try {
            JSONArray jarray = new JSONArray(json);
            for (int i = 0; i < jarray.length(); i++)
                selected_folders.add((String) jarray.get(i));
        } catch (JSONException ex) {
            Log.e(ex);
        }

        final View dview = LayoutInflater.from(context).inflate(R.layout.dialog_folder_select, null);
        final AutoCompleteTextView etSearch = dview.findViewById(R.id.etSearch);
        final ImageButton ibNext = dview.findViewById(R.id.ibNext);
        final TextView tvNoFolder = dview.findViewById(R.id.tvNoFolder);
        final Button btnFavorite1 = dview.findViewById(R.id.btnFavorite1);
        final Button btnFavorite2 = dview.findViewById(R.id.btnFavorite2);
        final Button btnFavorite3 = dview.findViewById(R.id.btnFavorite3);
        final ImageButton ibResetFavorites = dview.findViewById(R.id.ibResetFavorites);
        final RecyclerView rvFolder = dview.findViewById(R.id.rvFolder);
        final FloatingActionButton fabAdd = dview.findViewById(R.id.fabAdd);
        final ContentLoadingProgressBar pbWait = dview.findViewById(R.id.pbWait);
        final Group grpReady = dview.findViewById(R.id.grpReady);

        etSearch.setThreshold(1);
        etSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    try {
                        etSearch.showDropDown();
                    } catch (Throwable ex) {
                        Log.w(ex);
                        /*
                            Caused by: android.view.WindowManager$BadTokenException: Unable to add window -- token null is not valid; is your activity running?
                                    at android.view.ViewRootImpl.setView(ViewRootImpl.java:958)
                                    at android.view.WindowManagerGlobal.addView(WindowManagerGlobal.java:381)
                                    at android.view.WindowManagerImpl.addView(WindowManagerImpl.java:100)
                                    at android.widget.PopupWindow.invokePopup(PopupWindow.java:1467)
                                    at android.widget.PopupWindow.showAsDropDown(PopupWindow.java:1316)
                                    at android.widget.ListPopupWindow.show(ListPopupWindow.java:740)
                                    at android.widget.AutoCompleteTextView.showDropDown(AutoCompleteTextView.java:1226)
                                    at eu.faircode.email.FragmentDialogFolder$1.onFocusChange(SourceFile:91)
                                    at android.view.View.onFocusChanged(View.java:7573)
                                    at android.widget.TextView.onFocusChanged(TextView.java:10958)
                                    at android.widget.EditText.onFocusChanged(EditText.java:277)
                                    at android.widget.AutoCompleteTextView.onFocusChanged(AutoCompleteTextView.java:1125)
                         */
                    }
            }
        });

        ArrayAdapter<String> frequent =
                new ArrayAdapter<>(context, R.layout.spinner_item1_dropdown, android.R.id.text1, selected_folders);
        etSearch.setAdapter(frequent);

        rvFolder.setHasFixedSize(false);
        llm = new LinearLayoutManager(context);
        rvFolder.setLayoutManager(llm);

        adapter = new AdapterFolder(context, getViewLifecycleOwner(),
                account, false, false, false, false, false, new AdapterFolder.IFolderSelectedListener() {
            @Override
            public void onFolderSelected(@NonNull TupleFolderEx folder) {
                String name = folder.getDisplayName(context, folder.parent_ref);
                selected_folders.remove(name);
                selected_folders.add(0, name);
                while (selected_folders.size() > MAX_SELECTED_FOLDERS)
                    selected_folders.remove(MAX_SELECTED_FOLDERS);
                JSONArray jarray = new JSONArray(selected_folders);
                prefs.edit().putString("selected_folders", jarray.toString()).apply();

                increaseSelectedCount(folder.id, context);

                Bundle args = getArguments();
                args.putLong("folder", folder.id);
                args.putString("type", folder.type);

                sendResult(RESULT_OK);
                dismiss();
            }

            @Override
            public boolean onFolderLongPress(@NonNull TupleFolderEx folder) {
                if (cancopy) {
                    getArguments().putBoolean("copy", true);
                    onFolderSelected(folder);
                    return true;
                } else
                    return false;
            }
        });

        btnFavorite1.setVisibility(View.GONE);
        btnFavorite2.setVisibility(View.GONE);
        btnFavorite3.setVisibility(View.GONE);
        ibResetFavorites.setVisibility(View.GONE);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Long id = (Long) v.getTag();
                if (id == null)
                    return;

                increaseSelectedCount(id, context);

                Bundle args = getArguments();
                args.putLong("folder", id);

                sendResult(RESULT_OK);
                dismiss();
            }
        };

        View.OnLongClickListener llistener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Long id = (Long) v.getTag();
                if (id == null)
                    return false;

                Bundle args = getArguments();
                args.putLong("folder", id);
                args.putBoolean("copy", true);

                sendResult(RESULT_OK);
                dismiss();

                return true;
            }
        };

        btnFavorite1.setOnClickListener(listener);
        btnFavorite2.setOnClickListener(listener);
        btnFavorite3.setOnClickListener(listener);

        if (cancopy) {
            btnFavorite1.setOnLongClickListener(llistener);
            btnFavorite2.setOnLongClickListener(llistener);
            btnFavorite3.setOnLongClickListener(llistener);
        }

        ibResetFavorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnFavorite1.setVisibility(View.GONE);
                btnFavorite2.setVisibility(View.GONE);
                btnFavorite3.setVisibility(View.GONE);
                ibResetFavorites.setVisibility(View.GONE);

                final DB db = DB.getInstance(context);

                Helper.getParallelExecutor().submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            db.folder().resetSelectedCount(account);
                            db.contact().clearContactFolders();
                        } catch (Throwable ex) {
                            Log.e(ex);
                        }
                    }
                });
            }
        });

        rvFolder.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                result = 0;
                String query = s.toString().toLowerCase();
                adapter.search(query, result, new AdapterFolder.ISearchResult() {
                    @Override
                    public void onFound(int pos, boolean hasNext) {
                        ibNext.setVisibility(hasNext ? View.VISIBLE : View.INVISIBLE);
                        llm.scrollToPositionWithOffset(pos, 0);
                    }

                    @Override
                    public void onNotFound() {
                        ibNext.setVisibility(View.INVISIBLE);
                    }
                });
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        etSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
            }
        });

        ibNext.setVisibility(View.INVISIBLE);
        ibNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                result++;
                String query = etSearch.getText().toString();
                adapter.search(query, result, new AdapterFolder.ISearchResult() {
                    @Override
                    public void onFound(int pos, boolean hasNext) {
                        ibNext.setVisibility(hasNext ? View.VISIBLE : View.INVISIBLE);
                        llm.scrollToPositionWithOffset(pos, 0);
                    }

                    @Override
                    public void onNotFound() {
                        ibNext.setVisibility(View.INVISIBLE);
                    }
                });
            }
        });

        fabAdd.hide();
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etSearch.setText(null);

                Bundle args = new Bundle();
                args.putLong("account", account);

                FragmentDialogEditName fragment = new FragmentDialogEditName();
                fragment.setArguments(args);
                fragment.setTargetFragment(FragmentDialogSelectFolder.this, REQUEST_FOLDER_NAME);
                fragment.show(getParentFragmentManager(), "folder:name");
            }
        });

        Bundle args = new Bundle();
        args.putLong("account", account);
        args.putLongArray("disabled", disabled);
        if (messages != null)
            args.putLongArray("messages", messages);

        new SimpleTask<Data>() {
            @Override
            protected void onPreExecute(Bundle args) {
                tvNoFolder.setVisibility(View.GONE);
                grpReady.setVisibility(View.GONE);
                pbWait.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onPostExecute(Bundle args) {
                pbWait.setVisibility(View.GONE);
            }

            @Override
            protected Data onExecute(Context context, Bundle args) {
                long account = args.getLong("account");
                long[] disabled = args.getLongArray("disabled");
                long[] messages = args.getLongArray("messages");

                if (disabled == null)
                    disabled = new long[0];

                List<EntityFolder> favorites = new ArrayList<>();

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                boolean suggest_received = prefs.getBoolean("suggest_received", false);

                DB db = DB.getInstance(context);

                Map<Long, Integer> frequency = new HashMap<>();
                if (suggest_received &&
                        messages != null && messages.length < 100) {
                    List<Long> list = Helper.fromLongArray(disabled);
                    for (Long id : messages) {
                        EntityMessage message = db.message().getMessage(id);
                        if (message != null && message.from != null && message.from[0] != null) {
                            String email = ((InternetAddress) message.from[0]).getAddress();
                            if (!TextUtils.isEmpty(email)) {
                                EntityContact contact =
                                        db.contact().getContact(message.account, EntityContact.TYPE_FROM, email);
                                if (contact != null &&
                                        contact.folder != null &&
                                        !list.contains(contact.folder)) {
                                    Integer freq = frequency.get(contact.folder);
                                    freq = (freq == null ? 1 : freq + 1);
                                    frequency.put(contact.folder, freq);
                                }
                            }
                        }
                    }
                }

                List<Long> fs = new ArrayList<>(frequency.keySet());
                Collections.sort(fs, new Comparator<Long>() {
                    @Override
                    public int compare(Long f1, Long f2) {
                        return -Integer.compare(frequency.get(f1), frequency.get(f2));
                    }
                });
                for (long fid : fs) {
                    EntityFolder f = db.folder().getFolder(fid);
                    if (f != null)
                        favorites.add(f);
                }

                List<EntityFolder> ffolders = db.folder().getFavoriteFolders(account, 3, disabled);
                if (ffolders != null)
                    for (EntityFolder folder : ffolders)
                        if (!frequency.containsKey(folder.id))
                            favorites.add(folder);

                Data data = new Data();
                data.account = db.account().getAccount(account);
                data.folders = db.folder().getFoldersEx(account);
                data.favorites = favorites;

                return data;
            }

            @Override
            protected void onExecuted(final Bundle args, Data data) {
                if (data.folders == null || data.folders.size() == 0)
                    tvNoFolder.setVisibility(View.VISIBLE);
                else {
                    if (data.favorites != null && data.favorites.size() > 0) {
                        Integer textColor = null;
                        if (BuildConfig.DEBUG)
                            try {
                                TypedValue tv = new TypedValue();
                                Resources.Theme theme = context.getTheme();
                                theme.resolveAttribute(android.R.attr.textAppearanceButton, tv, true);
                                int[] attr = new int[]{android.R.attr.textColor};
                                TypedArray ta = theme.obtainStyledAttributes(tv.resourceId, attr);
                                textColor = ta.getColor(0, Color.BLACK);
                            } catch (Throwable ex) {
                                Log.e(ex);
                            }

                        Button[] btn = new Button[]{btnFavorite1, btnFavorite2, btnFavorite3};
                        for (int i = 0; i < btn.length; i++)
                            if (i < data.favorites.size()) {
                                EntityFolder favorite = data.favorites.get(i);
                                btn[i].setTag(favorite.id);
                                btn[i].setText(favorite.getDisplayName(context));
                                if (textColor != null)
                                    btn[i].setTextColor(
                                            favorite.color == null || !HtmlHelper.hasColor(favorite.color)
                                                    ? textColor : favorite.color);
                                btn[i].setVisibility(View.VISIBLE);
                            } else
                                btn[i].setVisibility(View.INVISIBLE);

                        ibResetFavorites.setVisibility(View.VISIBLE);
                    }

                    adapter.setDisabled(Helper.fromLongArray(disabled));
                    adapter.set(data.folders);

                    if (data.account.protocol == EntityAccount.TYPE_IMAP)
                        fabAdd.show();
                    else
                        fabAdd.hide();

                    grpReady.setVisibility(View.VISIBLE);
                }
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                tvNoFolder.setText(Log.formatThrowable(ex));
                tvNoFolder.setVisibility(View.VISIBLE);
            }
        }.execute(this, args, "folder:select");

        return new AlertDialog.Builder(context)
                .setIcon(icon)
                .setTitle(title)
                .setView(dview)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    private static void increaseSelectedCount(Long id, Context context) {
        final DB db = DB.getInstance(context);

        Helper.getParallelExecutor().submit(new Runnable() {
            @Override
            public void run() {
                try {
                    EntityFolder folder = db.folder().getFolder(id);
                    if (folder != null && EntityFolder.USER.equals(folder.type))
                        db.folder().increaseSelectedCount(folder.id, new Date().getTime());
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            switch (requestCode) {
                case REQUEST_FOLDER_NAME:
                    if (resultCode == RESULT_OK && data != null)
                        onFolderName(data.getBundleExtra("args"));
                    break;
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    private void onFolderName(Bundle args) {
        new SimpleTask<List<TupleFolderEx>>() {
            @Override
            protected List<TupleFolderEx> onExecute(Context context, Bundle args) throws Throwable {
                long aid = args.getLong("account");
                String name = args.getString("name");
                String pid = args.getString("parent");

                DB db = DB.getInstance(context);

                try {
                    db.beginTransaction();

                    EntityAccount account = db.account().getAccount(aid);
                    if (account == null)
                        return null;

                    EntityFolder parent = (TextUtils.isEmpty(pid) ? null : db.folder().getFolderByName(account.id, pid));

                    if (parent != null)
                        name = parent.name + parent.separator + name;

                    EntityFolder folder = db.folder().getFolderByName(account.id, name);
                    if (folder == null) {
                        folder = new EntityFolder();
                        folder.tbc = true;
                        folder.account = account.id;
                        folder.name = name;
                        folder.type = EntityFolder.USER;
                        folder.parent = (parent == null ? null : parent.id);
                        folder.setProperties();
                        folder.inheritFrom(parent);
                        folder.id = db.folder().insertFolder(folder);
                    }

                    args.putLong("folder", folder.id);

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                ServiceSynchronize.reload(context, aid, true, "create folder");

                return db.folder().getFoldersEx(aid);
            }

            @Override
            protected void onExecuted(Bundle args, List<TupleFolderEx> folders) {
                if (folders == null)
                    return;

                adapter.set(folders);

                long fid = args.getLong("folder");
                int pos = adapter.getPositionForKey(fid);
                if (pos == RecyclerView.NO_POSITION)
                    return;

                llm.scrollToPositionWithOffset(pos, 0);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "folder:add");
    }

    public static class FragmentDialogEditName extends FragmentDialogBase {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            final Context context = getContext();
            View view = LayoutInflater.from(context).inflate(R.layout.dialog_folder_add, null);
            final EditText etName = view.findViewById(R.id.etName);
            final Spinner spParent = view.findViewById(R.id.spParent);

            etName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId != EditorInfo.IME_ACTION_DONE)
                        return false;
                    AlertDialog dialog = (AlertDialog) getDialog();
                    if (dialog == null)
                        return false;
                    Button btnOk = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    if (btnOk == null)
                        return false;
                    btnOk.performClick();
                    return true;
                }
            });

            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.spinner_item1, android.R.id.text1);
            adapter.setDropDownViewResource(R.layout.spinner_item1_dropdown);
            spParent.setAdapter(adapter);

            etName.setText(null);

            view.post(new Runnable() {
                @Override
                public void run() {
                    etName.requestFocus();
                    Helper.showKeyboard(etName);
                }
            });

            new SimpleTask<List<String>>() {
                @Override
                protected List<String> onExecute(Context context, Bundle args) throws Throwable {
                    long account = args.getLong("account");

                    DB db = DB.getInstance(context);
                    List<EntityFolder> folders = db.folder().getFolders(account, false, false);
                    if (folders == null)
                        return null;

                    Collator collator = Collator.getInstance(Locale.getDefault());
                    collator.setStrength(Collator.SECONDARY); // Case insensitive, process accents etc

                    Collections.sort(folders, new Comparator<EntityFolder>() {
                        @Override
                        public int compare(EntityFolder f1, EntityFolder f2) {
                            return collator.compare(f1.name, f2.name);
                        }
                    });

                    List<String> result = new ArrayList<>();
                    result.add("-");
                    for (EntityFolder folder : folders)
                        result.add(folder.name);

                    return result;
                }

                @Override
                protected void onExecuted(Bundle args, List<String> parents) {
                    adapter.clear();
                    adapter.addAll(parents);
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(getParentFragmentManager(), ex);
                }
            }.execute(this, getArguments(), "folder:parents");

            return new AlertDialog.Builder(context)
                    .setView(view)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String parent = (spParent.getSelectedItemPosition() == 0
                                    ? null
                                    : (String) spParent.getSelectedItem());
                            String name = etName.getText().toString().trim();
                            if (TextUtils.isEmpty(name))
                                sendResult(RESULT_CANCELED);
                            else {
                                getArguments().putString("parent", parent);
                                getArguments().putString("name", name);
                                sendResult(RESULT_OK);
                            }
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
        }
    }

    private static class Data {
        private EntityAccount account;
        private List<TupleFolderEx> folders;
        private List<EntityFolder> favorites;
    }
}
