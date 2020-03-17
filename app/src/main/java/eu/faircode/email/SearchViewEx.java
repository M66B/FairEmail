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

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.lifecycle.LifecycleOwner;
import androidx.preference.PreferenceManager;

public class SearchViewEx extends SearchView {
    private String _searching = null;

    public SearchViewEx(Context context) {
        super(context);
    }

    public SearchViewEx(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SearchViewEx(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    void setup(LifecycleOwner owner, MenuItem menuSearch, String searching, ISearch intf) {
        _searching = searching;

        setQueryHint(getContext().getString(R.string.title_search));

        if (!TextUtils.isEmpty(_searching)) {
            menuSearch.expandActionView();
            setQuery(_searching, false);
        }

        AutoCompleteTextView autoCompleteTextView = findViewById(androidx.appcompat.R.id.search_src_text);
        autoCompleteTextView.setThreshold(0);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                _searching = newText;
                intf.onSave(_searching);

                if (TextUtils.isEmpty(newText)) {
                    MatrixCursor cursor = new MatrixCursor(new String[]{"_id", "suggestion"});

                    String last_search = prefs.getString("last_search", null);
                    if (!TextUtils.isEmpty(last_search))
                        cursor.addRow(new Object[]{-1, last_search});

                    String prefix = getContext().getString(R.string.title_search_special_prefix);
                    cursor.addRow(new Object[]{-2, prefix + ":" + getContext().getString(R.string.title_search_special_unseen)});
                    cursor.addRow(new Object[]{-3, prefix + ":" + getContext().getString(R.string.title_search_special_flagged)});
                    cursor.addRow(new Object[]{-4, prefix + ":" + getContext().getString(R.string.title_search_special_snoozed)});
                    cursor.addRow(new Object[]{-5, prefix + ":" + getContext().getString(R.string.title_search_special_encrypted)});
                    cursor.addRow(new Object[]{-6, prefix + ":" + getContext().getString(R.string.title_search_special_attachments)});
                    SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                            getContext(),
                            R.layout.search_suggestion,
                            cursor,
                            new String[]{"suggestion"},
                            new int[]{android.R.id.text1},
                            0);
                    setSuggestionsAdapter(adapter);
                    adapter.notifyDataSetChanged();
                } else {
                    Bundle args = new Bundle();
                    args.putString("query", newText);

                    new SimpleTask<Cursor>() {
                        @Override
                        protected Cursor onExecute(Context context, Bundle args) {
                            String query = args.getString("query");

                            DB db = DB.getInstance(context);
                            return db.message().getSuggestions("%" + query + "%");
                        }

                        @Override
                        protected void onExecuted(Bundle args, Cursor cursor) {
                            Log.i("Suggestions=" + cursor.getCount());
                            SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                                    getContext(),
                                    R.layout.search_suggestion,
                                    cursor,
                                    new String[]{"suggestion"},
                                    new int[]{android.R.id.text1},
                                    0);
                            setSuggestionsAdapter(adapter);
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            ToastEx.makeText(getContext(), Log.formatThrowable(ex), Toast.LENGTH_LONG).show();
                        }
                    }.execute(getContext(), owner, args, "messages:suggestions");
                }

                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                _searching = null;
                intf.onSave(query);
                menuSearch.collapseActionView();
                intf.onSearch(query);

                String prefix = getContext().getString(R.string.title_search_special_prefix);
                if (query != null && !query.startsWith(prefix + ":"))
                    prefs.edit().putString("last_search", query).apply();
                return true;
            }
        });

        setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                Cursor cursor = (Cursor) getSuggestionsAdapter().getItem(position);
                long id = cursor.getInt(0);
                setQuery(cursor.getString(1), id != -1);
                return (id == -1);
            }
        });
    }

    interface ISearch {
        void onSave(String query);

        void onSearch(String query);
    }
}
