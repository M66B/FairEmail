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

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.FilterQueryProvider;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.Group;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.preference.PreferenceManager;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class FragmentDialogSearch extends FragmentDialogBase {
    private static final int MAX_SUGGESTIONS = 3;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final long account = getArguments().getLong("account", -1);
        final long folder = getArguments().getLong("folder", -1);

        final Context context = getContext();
        boolean pro = ActivityBilling.isPro(context);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean fts = prefs.getBoolean("fts", false);
        boolean last_search_senders = prefs.getBoolean("last_search_senders", true);
        boolean last_search_recipients = prefs.getBoolean("last_search_recipients", true);
        boolean last_search_subject = prefs.getBoolean("last_search_subject", true);
        boolean last_search_keywords = prefs.getBoolean("last_search_keywords", false);
        boolean last_search_message = prefs.getBoolean("last_search_message", true);
        boolean last_search_notes = prefs.getBoolean("last_search_notes", true);

        View dview = LayoutInflater.from(context).inflate(R.layout.dialog_search, null);

        final AutoCompleteTextView etQuery = dview.findViewById(R.id.etQuery);
        final TextView tvSearch1 = dview.findViewById(R.id.tvSearch1);
        final TextView tvSearch2 = dview.findViewById(R.id.tvSearch2);
        final TextView tvSearch3 = dview.findViewById(R.id.tvSearch3);
        final ImageButton ibResetSearches = dview.findViewById(R.id.ibResetSearches);

        final ImageButton ibInfo = dview.findViewById(R.id.ibInfo);
        final ImageButton ibFlagged = dview.findViewById(R.id.ibFlagged);
        final ImageButton ibUnseen = dview.findViewById(R.id.ibUnseen);
        final ImageButton ibInvite = dview.findViewById(R.id.ibInvite);
        final ImageButton ibAttachment = dview.findViewById(R.id.ibAttachment);
        final ImageButton ibNotes = dview.findViewById(R.id.ibNotes);
        final ImageButton ibMore = dview.findViewById(R.id.ibMore);
        final TextView tvMore = dview.findViewById(R.id.tvMore);
        final CheckBox cbSearchIndex = dview.findViewById(R.id.cbSearchIndex);
        final CheckBox cbSenders = dview.findViewById(R.id.cbSenders);
        final CheckBox cbRecipients = dview.findViewById(R.id.cbRecipients);
        final CheckBox cbSubject = dview.findViewById(R.id.cbSubject);
        final CheckBox cbKeywords = dview.findViewById(R.id.cbKeywords);
        final CheckBox cbMessage = dview.findViewById(R.id.cbMessage);
        final CheckBox cbNotes = dview.findViewById(R.id.cbNotes);
        final CheckBox cbHeaders = dview.findViewById(R.id.cbHeaders);
        final CheckBox cbHtml = dview.findViewById(R.id.cbHtml);
        final CheckBox cbUnseen = dview.findViewById(R.id.cbUnseen);
        final CheckBox cbFlagged = dview.findViewById(R.id.cbFlagged);
        final CheckBox cbHidden = dview.findViewById(R.id.cbHidden);
        final CheckBox cbEncrypted = dview.findViewById(R.id.cbEncrypted);
        final CheckBox cbAttachments = dview.findViewById(R.id.cbAttachments);
        final Spinner spMessageSize = dview.findViewById(R.id.spMessageSize);
        final Button btnBefore = dview.findViewById(R.id.btnBefore);
        final Button btnAfter = dview.findViewById(R.id.btnAfter);
        final TextView tvBefore = dview.findViewById(R.id.tvBefore);
        final TextView tvAfter = dview.findViewById(R.id.tvAfter);
        final Group grpMore = dview.findViewById(R.id.grpMore);

        ibInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.hideKeyboard(etQuery);
                Helper.viewFAQ(v.getContext(), 13);
            }
        });

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                context,
                R.layout.search_suggestion,
                null,
                new String[]{"suggestion"},
                new int[]{android.R.id.text1},
                0);

        adapter.setFilterQueryProvider(new FilterQueryProvider() {
            public Cursor runQuery(CharSequence typed) {
                Log.i("Search suggest=" + typed);

                MatrixCursor cursor = new MatrixCursor(new String[]{"_id", "suggestion"});
                if (TextUtils.isEmpty(typed))
                    return cursor;
                if (fts && pro)
                    return cursor;

                String query = "%" + typed + "%";
                DB db = DB.getInstance(context);
                return db.message().getSuggestions(
                        account < 0 ? null : account,
                        folder < 0 ? null : folder,
                        "%" + query + "%",
                        MAX_SUGGESTIONS);
            }
        });

        adapter.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
            @Override
            public CharSequence convertToString(Cursor cursor) {
                return cursor.getString(cursor.getColumnIndex("suggestion"));
            }
        });

        etQuery.setAdapter(adapter);

        View.OnClickListener onSearch = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etQuery.setText(((TextView) v).getText());
                ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_POSITIVE).performClick();
            }
        };

        boolean hasSearches = false;
        TextView[] views = new TextView[]{tvSearch1, tvSearch2, tvSearch3};
        for (int i = 1; i <= 3; i++) {
            boolean has = prefs.contains("last_search" + i);
            if (has) {
                hasSearches = true;
                String search = prefs.getString("last_search" + i, null);
                views[i - 1].setText(search);
                views[i - 1].setOnClickListener(onSearch);
            }
            views[i - 1].setVisibility(has ? View.VISIBLE : View.GONE);
        }
        ibResetSearches.setVisibility(hasSearches ? View.VISIBLE : View.GONE);

        ibResetSearches.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = prefs.edit();
                for (int i = 1; i <= 3; i++) {
                    editor.remove("last_search" + i);
                    views[i - 1].setVisibility(View.GONE);
                }
                editor.apply();
                ibResetSearches.setVisibility(View.GONE);
            }
        });

        View.OnClickListener onMore = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.hideKeyboard(etQuery);

                if (grpMore.getVisibility() == View.VISIBLE) {
                    ibMore.setImageLevel(1);
                    grpMore.setVisibility(View.GONE);
                    cbHeaders.setVisibility(View.GONE);
                    cbHtml.setVisibility(View.GONE);
                } else {
                    ibMore.setImageLevel(0);
                    grpMore.setVisibility(View.VISIBLE);
                    if (BuildConfig.DEBUG) {
                        cbHeaders.setVisibility(View.VISIBLE);
                        cbHtml.setVisibility(View.VISIBLE);
                    }
                }
            }
        };

        ibMore.setOnClickListener(onMore);

        tvMore.setOnClickListener(onMore);

        cbSearchIndex.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                cbSenders.setEnabled(!isChecked);
                cbRecipients.setEnabled(!isChecked);
                cbSubject.setEnabled(!isChecked);
                cbKeywords.setEnabled(!isChecked);
                cbMessage.setEnabled(!isChecked);
                cbNotes.setEnabled(!isChecked);
                cbHeaders.setEnabled(!isChecked);
                cbHtml.setEnabled(!isChecked);
                cbUnseen.setEnabled(!isChecked);
                cbFlagged.setEnabled(!isChecked);
                cbHidden.setEnabled(!isChecked);
                cbEncrypted.setEnabled(!isChecked);
                cbAttachments.setEnabled(!isChecked);
                spMessageSize.setEnabled(!isChecked);
            }
        });

        cbSenders.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("last_search_senders", isChecked).apply();
            }
        });

        cbRecipients.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("last_search_recipients", isChecked).apply();
            }
        });

        cbSubject.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("last_search_subject", isChecked).apply();
            }
        });

        cbKeywords.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("last_search_keywords", isChecked).apply();
            }
        });

        cbMessage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("last_search_message", isChecked).apply();
            }
        });

        cbNotes.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("last_search_notes", isChecked).apply();
            }
        });

        spMessageSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                parent.post(new Runnable() {
                    @Override
                    public void run() {
                        //parent.requestFocusFromTouch();
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        btnAfter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickDate(tvAfter);
            }
        });

        btnBefore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickDate(tvBefore);
            }
        });

        ibMore.setImageLevel(1);
        cbSearchIndex.setChecked(fts && pro);
        cbSearchIndex.setEnabled(pro);
        cbSenders.setChecked(last_search_senders);
        cbRecipients.setChecked(last_search_recipients);
        cbSubject.setChecked(last_search_subject);
        cbKeywords.setChecked(last_search_keywords);
        cbMessage.setChecked(last_search_message);
        cbNotes.setChecked(last_search_notes);
        tvAfter.setText(null);
        tvBefore.setText(null);

        grpMore.setVisibility(View.GONE);
        cbHeaders.setVisibility(View.GONE);
        cbHtml.setVisibility(View.GONE);

        final AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dview)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BoundaryCallbackMessages.SearchCriteria criteria = new BoundaryCallbackMessages.SearchCriteria();

                        criteria.query = etQuery.getText().toString().trim();

                        if (!TextUtils.isEmpty(criteria.query)) {
                            List<String> searches = new ArrayList<>();
                            for (int i = 1; i <= 3; i++)
                                if (prefs.contains("last_search" + i)) {
                                    String search = prefs.getString("last_search" + i, null);
                                    searches.add(search);
                                }

                            if (!searches.contains(criteria.query))
                                searches.add(0, criteria.query);

                            SharedPreferences.Editor editor = prefs.edit();
                            for (int i = 1; i <= Math.min(3, searches.size()); i++)
                                editor.putString("last_search" + i, searches.get(i - 1));
                            editor.apply();
                        }

                        if (TextUtils.isEmpty(criteria.query))
                            criteria.query = null;

                        criteria.fts = cbSearchIndex.isChecked();
                        if (!criteria.fts) {
                            criteria.in_senders = cbSenders.isChecked();
                            criteria.in_recipients = cbRecipients.isChecked();
                            criteria.in_subject = cbSubject.isChecked();
                            criteria.in_keywords = cbKeywords.isChecked();
                            criteria.in_message = cbMessage.isChecked();
                            criteria.in_notes = cbNotes.isChecked();
                            criteria.in_headers = cbHeaders.isChecked();
                            criteria.in_html = cbHtml.isChecked();
                            criteria.with_unseen = cbUnseen.isChecked();
                            criteria.with_flagged = cbFlagged.isChecked();
                            criteria.with_hidden = cbHidden.isChecked();
                            criteria.with_encrypted = cbEncrypted.isChecked();
                            criteria.with_attachments = cbAttachments.isChecked();

                            int pos = spMessageSize.getSelectedItemPosition();
                            if (pos > 0) {
                                int[] sizes = getResources().getIntArray(R.array.sizeValues);
                                criteria.with_size = sizes[pos];
                            }
                        }

                        Object after = tvAfter.getTag();
                        Object before = tvBefore.getTag();

                        if (after != null)
                            criteria.after = ((Calendar) after).getTimeInMillis();
                        if (before != null)
                            criteria.before = ((Calendar) before).getTimeInMillis();

                        Helper.hideKeyboard(etQuery);

                        if (criteria.query != null && criteria.query.startsWith("raw:"))
                            new SimpleTask<EntityFolder>() {
                                @Override
                                protected EntityFolder onExecute(Context context, Bundle args) {
                                    long aid = args.getLong("account", -1);

                                    DB db = DB.getInstance(context);
                                    EntityAccount account = null;
                                    if (aid < 0) {
                                        List<EntityAccount> accounts = db.account().getSynchronizingAccounts();
                                        if (accounts == null)
                                            return null;
                                        for (EntityAccount a : accounts)
                                            if (a.isGmail())
                                                if (account == null)
                                                    account = a;
                                                else
                                                    return null;
                                    } else
                                        account = db.account().getAccount(aid);

                                    if (account == null || !account.isGmail())
                                        return null;

                                    return db.folder().getFolderByType(account.id, EntityFolder.ARCHIVE);
                                }

                                @Override
                                protected void onExecuted(Bundle args, EntityFolder archive) {
                                    FragmentMessages.search(
                                            context, getViewLifecycleOwner(), getParentFragmentManager(),
                                            account,
                                            archive == null ? folder : archive.id,
                                            archive != null,
                                            criteria);
                                }

                                @Override
                                protected void onException(Bundle args, Throwable ex) {
                                    Log.e(ex);
                                }
                            }.execute(context, getViewLifecycleOwner(), getArguments(), "search:raw");
                        else
                            FragmentMessages.search(
                                    context, getViewLifecycleOwner(), getParentFragmentManager(),
                                    account, folder, false, criteria);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                    }
                })
                .create();

        View.OnClickListener onClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

                BoundaryCallbackMessages.SearchCriteria criteria = new BoundaryCallbackMessages.SearchCriteria();
                int id = v.getId();
                if (id == R.id.ibFlagged)
                    criteria.with_flagged = true;
                else if (id == R.id.ibUnseen)
                    criteria.with_unseen = true;
                else if (id == R.id.ibInvite) {
                    criteria.with_attachments = true;
                    criteria.with_types = new String[]{"text/calendar"};
                } else if (id == R.id.ibAttachment)
                    criteria.with_attachments = true;
                else if (id == R.id.ibNotes)
                    criteria.with_notes = true;

                FragmentMessages.search(
                        context, getViewLifecycleOwner(), getParentFragmentManager(),
                        account, folder, false, criteria);
            }
        };

        ibNotes.setOnClickListener(onClick);
        ibAttachment.setOnClickListener(onClick);
        ibInvite.setOnClickListener(onClick);
        ibUnseen.setOnClickListener(onClick);
        ibFlagged.setOnClickListener(onClick);

        etQuery.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
                    return true;
                }
                return false;
            }
        });

        return dialog;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void pickDate(TextView tv) {
        Object tag = tv.getTag();
        final Calendar cal = (tag == null ? Calendar.getInstance() : (Calendar) tag);

        DatePickerDialog picker = new DatePickerDialog(getContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        cal.set(Calendar.YEAR, year);
                        cal.set(Calendar.MONTH, month);
                        cal.set(Calendar.DAY_OF_MONTH, day);

                        DateFormat DF = Helper.getDateInstance(getContext());

                        tv.setTag(cal);
                        tv.setText(DF.format(cal.getTime()));
                    }
                },
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

        picker.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                tv.setTag(null);
                tv.setText(null);
            }
        });

        picker.show();
    }
}
