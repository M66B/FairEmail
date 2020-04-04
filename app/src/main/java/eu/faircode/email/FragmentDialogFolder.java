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

    Copyright 2018-2020 by Marcel Bokhorst (M66B)
*/

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class FragmentDialogFolder extends FragmentDialogBase {
    private int result = 0;

    private static final int MAX_SELECTED_FOLDERS = 5;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final String title = getArguments().getString("title");
        final long account = getArguments().getLong("account");
        final long[] disabled = getArguments().getLongArray("disabled");

        List<String> selected_folders = new ArrayList<>();

        final Context context = getContext();
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String json = prefs.getString("selected_folders", "[]");

        try {
            JSONArray jarray = new JSONArray(json);
            for (int i = 0; i < jarray.length(); i++)
                selected_folders.add((String) jarray.get(i));
        } catch (JSONException ex) {
            Log.e(ex);
        }

        final View dview = LayoutInflater.from(context).inflate(R.layout.dialog_folder_select, null);
        final TextView tvNoFolder = dview.findViewById(R.id.tvNoFolder);
        final AutoCompleteTextView etSearch = dview.findViewById(R.id.etSearch);
        final ImageButton ibNext = dview.findViewById(R.id.ibNext);
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
                account, false, false, new AdapterFolder.IFolderSelectedListener() {
            @Override
            public void onFolderSelected(TupleFolderEx folder) {
                String name = folder.getDisplayName(context, folder.parent_ref);
                selected_folders.remove(name);
                selected_folders.add(0, name);
                while (selected_folders.size() > MAX_SELECTED_FOLDERS)
                    selected_folders.remove(MAX_SELECTED_FOLDERS);
                JSONArray jarray = new JSONArray(selected_folders);
                prefs.edit().putString("selected_folders", jarray.toString()).apply();

                Bundle args = getArguments();
                args.putLong("folder", folder.id);

                sendResult(RESULT_OK);
                dismiss();
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

        new SimpleTask<List<TupleFolderEx>>() {
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
            protected List<TupleFolderEx> onExecute(Context context, Bundle args) {
                long account = args.getLong("account");

                DB db = DB.getInstance(context);
                return db.folder().getFoldersEx(account);
            }

            @Override
            protected void onExecuted(final Bundle args, List<TupleFolderEx> folders) {
                if (folders == null || folders.size() == 0)
                    tvNoFolder.setVisibility(View.VISIBLE);
                else {
                    adapter.setDisabled(Helper.fromLongArray(disabled));
                    adapter.set(folders);
                    grpReady.setVisibility(View.VISIBLE);
                }
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "folder:select");

        return new AlertDialog.Builder(context)
                .setTitle(title)
                .setView(dview)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }
}
