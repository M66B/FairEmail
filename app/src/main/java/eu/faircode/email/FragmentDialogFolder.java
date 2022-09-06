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
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.Group;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class FragmentDialogFolder extends FragmentDialogBase {
    private int result = 0;

    private static final int MAX_SELECTED_FOLDERS = 5;

    private static final ExecutorService executor =
            Helper.getBackgroundExecutor(1, "folder");

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Bundle aargs = getArguments();
        final int icon = aargs.getInt("icon", R.drawable.twotone_folder_open_24);
        final String title = aargs.getString("title");
        final long account = aargs.getLong("account");
        final long[] disabled = aargs.getLongArray("disabled");
        final boolean cancopy = aargs.getBoolean("cancopy");

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
        final LinearLayoutManager llm = new LinearLayoutManager(context);
        rvFolder.setLayoutManager(llm);

        final AdapterFolder adapter = new AdapterFolder(context, getViewLifecycleOwner(),
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
        btnFavorite1.setOnClickListener(listener);
        btnFavorite2.setOnClickListener(listener);
        btnFavorite3.setOnClickListener(listener);

        ibResetFavorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnFavorite1.setVisibility(View.GONE);
                btnFavorite2.setVisibility(View.GONE);
                btnFavorite3.setVisibility(View.GONE);
                ibResetFavorites.setVisibility(View.GONE);

                final DB db = DB.getInstance(context);

                executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            db.folder().resetSelectedCount(account);
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
                        ibNext.setEnabled(hasNext);
                        llm.scrollToPositionWithOffset(pos, 0);
                    }

                    @Override
                    public void onNotFound() {
                        ibNext.setEnabled(false);
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

        ibNext.setEnabled(false);
        ibNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                result++;
                String query = etSearch.getText().toString();
                adapter.search(query, result, new AdapterFolder.ISearchResult() {
                    @Override
                    public void onFound(int pos, boolean hasNext) {
                        ibNext.setEnabled(hasNext);
                        llm.scrollToPositionWithOffset(pos, 0);
                    }

                    @Override
                    public void onNotFound() {
                        ibNext.setEnabled(false);
                    }
                });
            }
        });

        Bundle args = new Bundle();
        args.putLong("account", account);
        args.putLongArray("disabled", disabled);

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

                DB db = DB.getInstance(context);

                Data data = new Data();
                data.folders = db.folder().getFoldersEx(account);
                data.favorites = db.folder().getFavoriteFolders(account, 3, disabled);

                return data;
            }

            @Override
            protected void onExecuted(final Bundle args, Data data) {
                if (data.folders == null || data.folders.size() == 0)
                    tvNoFolder.setVisibility(View.VISIBLE);
                else {
                    if (data.favorites != null && data.favorites.size() > 0) {
                        Button[] btn = new Button[]{btnFavorite1, btnFavorite2, btnFavorite3};
                        for (int i = 0; i < btn.length; i++)
                            if (i < data.favorites.size()) {
                                EntityFolder favorite = data.favorites.get(i);
                                btn[i].setTag(favorite.id);
                                btn[i].setText(favorite.getDisplayName(context));
                                btn[i].setVisibility(View.VISIBLE);
                            } else
                                btn[i].setVisibility(View.INVISIBLE);

                        ibResetFavorites.setVisibility(View.VISIBLE);
                    }

                    adapter.setDisabled(Helper.fromLongArray(disabled));
                    adapter.set(data.folders);

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

        executor.submit(new Runnable() {
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

    private static class Data {
        private List<TupleFolderEx> folders;
        private List<EntityFolder> favorites;
    }
}
