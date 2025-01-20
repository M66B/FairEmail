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

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.FilterQueryProvider;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.Group;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class FragmentDialogSearch extends FragmentDialogBase {
    private static final int MAX_SUGGESTIONS = 3;
    private static final int RECENTLY_TOUCHED = 7 * 24; // hours

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Bundle args = getArguments();
        final long account = args.getLong("account", -1);
        final long folder = args.getLong("folder", -1);

        final Context context = getContext();
        final boolean pro = ActivityBilling.isPro(context);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final boolean fts = prefs.getBoolean("fts", false);
        final boolean last_fts = prefs.getBoolean("last_fts", true);
        final boolean last_search_senders = prefs.getBoolean("last_search_senders", true);
        final boolean last_search_recipients = prefs.getBoolean("last_search_recipients", true);
        final boolean last_search_subject = prefs.getBoolean("last_search_subject", true);
        final boolean last_search_keywords = prefs.getBoolean("last_search_keywords", false);
        final boolean last_search_message = prefs.getBoolean("last_search_message", true);
        final boolean last_search_notes = prefs.getBoolean("last_search_notes", true);
        final boolean last_search_filenames = prefs.getBoolean("last_search_filenames", false);
        final boolean last_search_trash = prefs.getBoolean("last_search_trash", true);
        final boolean last_search_junk = prefs.getBoolean("last_search_junk", true);
        final boolean last_search_device = prefs.getBoolean("last_search_device", true);

        final View dview = LayoutInflater.from(context).inflate(R.layout.dialog_search, null);

        final TextViewAutoCompleteAction etQuery = dview.findViewById(R.id.etQuery);
        final Button btnSearch1 = dview.findViewById(R.id.btnSearch1);
        final Button btnSearch2 = dview.findViewById(R.id.btnSearch2);
        final Button btnSearch3 = dview.findViewById(R.id.btnSearch3);
        final ImageButton ibResetSearches = dview.findViewById(R.id.ibResetSearches);

        final ImageButton ibInfo = dview.findViewById(R.id.ibInfo);
        final ImageButton ibFlagged = dview.findViewById(R.id.ibFlagged);
        final ImageButton ibUnseen = dview.findViewById(R.id.ibUnseen);
        final ImageButton ibHidden = dview.findViewById(R.id.ibHidden);
        final ImageButton ibInvite = dview.findViewById(R.id.ibInvite);
        final ImageButton ibAttachment = dview.findViewById(R.id.ibAttachment);
        final ImageButton ibNotes = dview.findViewById(R.id.ibNotes);
        final TextView tvHintFts = dview.findViewById(R.id.tvHintFts);
        final ImageButton ibMore = dview.findViewById(R.id.ibMore);
        final TextView tvMore = dview.findViewById(R.id.tvMore);
        final CheckBox cbSearchIndex = dview.findViewById(R.id.cbSearchIndex);
        final CheckBox cbSenders = dview.findViewById(R.id.cbSenders);
        final CheckBox cbRecipients = dview.findViewById(R.id.cbRecipients);
        final CheckBox cbSubject = dview.findViewById(R.id.cbSubject);
        final CheckBox cbKeywords = dview.findViewById(R.id.cbKeywords);
        final CheckBox cbMessage = dview.findViewById(R.id.cbMessage);
        final TextView tvSearchTextUnsupported = dview.findViewById(R.id.tvSearchTextUnsupported);
        final CheckBox cbNotes = dview.findViewById(R.id.cbNotes);
        final CheckBox cbFileNames = dview.findViewById(R.id.cbFileNames);
        final CheckBox cbHeaders = dview.findViewById(R.id.cbHeaders);
        final CheckBox cbHtml = dview.findViewById(R.id.cbHtml);
        final CheckBox cbSearchTrash = dview.findViewById(R.id.cbSearchTrash);
        final CheckBox cbSearchJunk = dview.findViewById(R.id.cbSearchJunk);
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
        final CheckBox cbSearchDevice = dview.findViewById(R.id.cbSearchDevice);
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

                int i = 0;

                String keyword = TupleKeyword.getKeyword(context, typed.toString());
                if (!TextUtils.isEmpty(keyword))
                    cursor.addRow(new Object[]{i++ + 1, keyword});

                if (cbSearchIndex.isEnabled() && cbSearchIndex.isChecked()) {
                    SQLiteDatabase db = Fts4DbHelper.getInstance(context);
                    List<String> suggestions = Fts4DbHelper.getSuggestions(
                            db,
                            typed + "%",
                            MAX_SUGGESTIONS);
                    for (; i < suggestions.size(); i++)
                        cursor.addRow(new Object[]{i + 1, suggestions.get(i)});
                    return cursor;
                }

                DB db = DB.getInstance(context);
                return db.message().getSuggestions(
                        cbSubject.isChecked(),
                        cbSenders.isChecked(),
                        account < 0 ? null : account,
                        folder < 0 ? null : folder,
                        "%" + typed + "%",
                        MAX_SUGGESTIONS);
            }
        });

        adapter.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
            @Override
            public CharSequence convertToString(Cursor cursor) {
                int colSuggestion = cursor.getColumnIndex("suggestion");
                return cursor.getString(colSuggestion);
            }
        });

        etQuery.setActionRunnable(new Runnable() {
            @Override
            public void run() {
                etQuery.setText(null);
            }
        });
        etQuery.setActionEnabled(true);

        etQuery.setAdapter(adapter);

        View.OnClickListener onSearch = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etQuery.setText(((TextView) v).getText());
                ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_POSITIVE).performClick();
            }
        };

        View.OnLongClickListener onCopy = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String text = ((TextView) v).getText().toString();

                etQuery.setText(text);

                ClipboardManager cbm = Helper.getSystemService(v.getContext(), ClipboardManager.class);
                cbm.setPrimaryClip(ClipData.newPlainText(getString(R.string.app_name), text));

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
                    ToastEx.makeText(context, R.string.title_clipboard_copied, Toast.LENGTH_LONG).show();

                return true;
            }
        };

        Button[] btn = new Button[]{btnSearch1, btnSearch2, btnSearch3};

        int searches = 0;
        for (int i = 0; i < btn.length; i++)
            if (prefs.contains("last_search" + (i + 1)))
                searches++;

        for (int i = 0; i < btn.length; i++) {
            if (prefs.contains("last_search" + (i + 1))) {
                String search = prefs.getString("last_search" + (i + 1), null);
                btn[i].setText(search);
                btn[i].setOnClickListener(onSearch);
                btn[i].setOnLongClickListener(onCopy);
                btn[i].setVisibility(View.VISIBLE);
            } else
                btn[i].setVisibility(searches > 0 ? View.INVISIBLE : View.GONE);
        }

        ibResetSearches.setVisibility(searches > 0 ? View.VISIBLE : View.GONE);

        ibResetSearches.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = prefs.edit();
                for (int i = 1; i <= 3; i++) {
                    editor.remove("last_search" + i);
                    btn[i - 1].setVisibility(View.GONE);
                }
                editor.apply();
                ibResetSearches.setVisibility(View.GONE);
            }
        });

        tvHintFts.setVisibility(last_fts && fts && pro ? View.VISIBLE : View.GONE);

        View.OnClickListener onMore = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.hideKeyboard(etQuery);

                if (grpMore.getVisibility() == View.VISIBLE) {
                    ibMore.setImageLevel(1);
                    grpMore.setVisibility(View.GONE);
                    cbHeaders.setVisibility(View.GONE);
                    cbHtml.setVisibility(View.GONE);
                    cbSearchTrash.setVisibility(View.GONE);
                    cbSearchJunk.setVisibility(View.GONE);
                } else {
                    ibMore.setImageLevel(0);
                    grpMore.setVisibility(View.VISIBLE);
                    if (BuildConfig.DEBUG) {
                        cbHeaders.setVisibility(View.VISIBLE);
                        cbHtml.setVisibility(View.VISIBLE);
                    }
                    if (folder < 0) {
                        cbSearchTrash.setVisibility(View.VISIBLE);
                        cbSearchJunk.setVisibility(View.VISIBLE);
                    }
                }
            }
        };

        ibMore.setOnClickListener(onMore);
        tvMore.setOnClickListener(onMore);

        Runnable evalMore = new RunnableEx("more") {
            @Override
            protected void delegate() {
                boolean last_search_senders = prefs.getBoolean("last_search_senders", true);
                boolean last_search_recipients = prefs.getBoolean("last_search_recipients", true);
                boolean last_search_subject = prefs.getBoolean("last_search_subject", true);
                boolean last_search_message = prefs.getBoolean("last_search_message", true);

                boolean all = (last_search_senders && last_search_recipients && last_search_subject && last_search_message);
                int color = Helper.resolveColor(context, all ? android.R.attr.textColorSecondary : R.attr.colorWarning);
                ibMore.setImageTintList(ColorStateList.valueOf(color));
                tvMore.setTextColor(color);
                tvMore.setTypeface(all ? Typeface.DEFAULT : Typeface.DEFAULT_BOLD);
            }
        };

        evalMore.run();

        cbSearchIndex.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("last_fts", isChecked).apply();
                tvHintFts.setVisibility(isChecked && fts && pro ? View.VISIBLE : View.GONE);
            }
        });

        cbSenders.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("last_search_senders", isChecked).apply();
                evalMore.run();
            }
        });

        cbRecipients.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("last_search_recipients", isChecked).apply();
                evalMore.run();
            }
        });

        cbSubject.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("last_search_subject", isChecked).apply();
                evalMore.run();
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
                evalMore.run();
            }
        });

        cbNotes.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("last_search_notes", isChecked).apply();
            }
        });

        cbFileNames.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("last_search_filenames", isChecked).apply();
            }
        });

        cbSearchTrash.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("last_search_trash", isChecked).apply();
            }
        });

        cbSearchJunk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("last_search_junk", isChecked).apply();
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

        cbSearchDevice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                prefs.edit().putBoolean("last_search_device", isChecked).apply();
            }
        });

        ibMore.setImageLevel(1);
        cbSearchIndex.setChecked(last_fts && fts && pro);
        cbSearchIndex.setEnabled(fts && pro);
        cbSenders.setChecked(last_search_senders);
        cbRecipients.setChecked(last_search_recipients);
        cbSubject.setChecked(last_search_subject);
        cbKeywords.setChecked(last_search_keywords);
        cbMessage.setChecked(last_search_message);
        tvSearchTextUnsupported.setText(getString(R.string.title_search_text_unsupported,
                "full text search not supported"));
        cbNotes.setChecked(last_search_notes);
        cbFileNames.setChecked(last_search_filenames);
        cbSearchTrash.setChecked(last_search_trash);
        cbSearchJunk.setChecked(last_search_junk);
        tvAfter.setText(null);
        tvBefore.setText(null);
        cbSearchDevice.setChecked(last_search_device);
        cbSearchDevice.setEnabled(account > 0 && folder > 0);

        grpMore.setVisibility(View.GONE);
        cbHeaders.setVisibility(View.GONE);
        cbHtml.setVisibility(View.GONE);
        cbSearchTrash.setVisibility(View.GONE);
        cbSearchJunk.setVisibility(View.GONE);

        final AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dview)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BoundaryCallbackMessages.SearchCriteria criteria = new BoundaryCallbackMessages.SearchCriteria();

                        criteria.query = etQuery.getText().toString().trim();
                        if (TextUtils.isEmpty(criteria.query))
                            criteria.query = null;

                        criteria.fts = (cbSearchIndex.isChecked() && cbSearchIndex.isEnabled());
                        criteria.in_senders = cbSenders.isChecked();
                        criteria.in_recipients = cbRecipients.isChecked();
                        criteria.in_subject = cbSubject.isChecked();
                        criteria.in_keywords = cbKeywords.isChecked();
                        criteria.in_message = cbMessage.isChecked();
                        criteria.in_notes = cbNotes.isChecked();
                        criteria.in_filenames = cbFileNames.isChecked();
                        criteria.in_headers = cbHeaders.isChecked();
                        criteria.in_html = cbHtml.isChecked();
                        criteria.with_unseen = cbUnseen.isChecked();
                        criteria.with_flagged = cbFlagged.isChecked();
                        criteria.with_hidden = cbHidden.isChecked();
                        criteria.with_encrypted = cbEncrypted.isChecked();
                        criteria.with_attachments = cbAttachments.isChecked();

                        if (!criteria.fts) {
                            int pos = spMessageSize.getSelectedItemPosition();
                            if (pos > 0) {
                                int[] sizes = getResources().getIntArray(R.array.sizeValues);
                                criteria.with_size = sizes[pos];
                            }
                        }

                        criteria.in_trash = cbSearchTrash.isChecked();
                        criteria.in_junk = cbSearchJunk.isChecked();

                        Object after = tvAfter.getTag();
                        Object before = tvBefore.getTag();

                        if (after != null)
                            criteria.after = ((Calendar) after).getTimeInMillis();
                        if (before != null)
                            criteria.before = ((Calendar) before).getTimeInMillis();

                        boolean device = (cbSearchDevice.isChecked() || !cbSearchDevice.isEnabled());

                        if (criteria.query != null) {
                            List<String> searches = new ArrayList<>();
                            for (int i = 1; i <= 3; i++)
                                if (prefs.contains("last_search" + i)) {
                                    String search = prefs.getString("last_search" + i, null);
                                    searches.add(search);
                                }

                            searches.remove(criteria.query);
                            searches.add(0, criteria.query);

                            SharedPreferences.Editor editor = prefs.edit();
                            for (int i = 1; i <= Math.min(3, searches.size()); i++)
                                editor.putString("last_search" + i, searches.get(i - 1));
                            editor.apply();
                        }

                        Helper.hideKeyboard(etQuery);

                        if (criteria.query != null && criteria.query.startsWith("raw:"))
                            new SimpleTask<EntityFolder>() {
                                @Override
                                protected EntityFolder onExecute(Context context, Bundle args) {
                                    long aid = args.getLong("account", -1);

                                    DB db = DB.getInstance(context);
                                    EntityAccount account = null;
                                    if (aid < 0) {
                                        List<EntityAccount> accounts = db.account().getSynchronizingAccounts(EntityAccount.TYPE_IMAP);
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
                                            archive != null || !device,
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
                                    account, folder,
                                    !device,
                                    criteria);
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
                else if (id == R.id.ibHidden)
                    criteria.with_hidden = true;
                else if (id == R.id.ibInvite) {
                    criteria.with_attachments = true;
                    criteria.with_types = new String[]{"text/calendar"};
                } else if (id == R.id.ibAttachment)
                    criteria.with_attachments = true;
                else if (id == R.id.ibNotes)
                    criteria.with_notes = true;

                boolean device = (cbSearchDevice.isChecked() || !cbSearchDevice.isEnabled());

                FragmentMessages.search(
                        context, getViewLifecycleOwner(), getParentFragmentManager(),
                        account, folder,
                        !device,
                        criteria);
            }
        };

        ibNotes.setOnClickListener(onClick);
        ibAttachment.setOnClickListener(onClick);
        ibInvite.setOnClickListener(onClick);
        ibHidden.setOnClickListener(onClick);
        ibUnseen.setOnClickListener(onClick);
        ibFlagged.setOnClickListener(onClick);

        ibFlagged.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                dialog.dismiss();

                FragmentDialogColor fragment = new FragmentDialogColor();
                fragment.setArguments(args);
                fragment.setTargetActivity((ActivityBase) getActivity(), ActivityView.REQUEST_FLAG_COLOR);
                fragment.show(getParentFragmentManager(), "search:color");

                return true;
            }
        });

        ibHidden.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                dialog.dismiss();

                BoundaryCallbackMessages.SearchCriteria criteria = new BoundaryCallbackMessages.SearchCriteria();
                criteria.touched = RECENTLY_TOUCHED;

                FragmentMessages.search(
                        context, getViewLifecycleOwner(), getParentFragmentManager(),
                        account, -1L,
                        false,
                        criteria);

                return true;
            }
        });

        etQuery.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO ||
                        (event != null &&
                                (event.getKeyCode() == KeyEvent.KEYCODE_ENTER ||
                                        event.getKeyCode() == KeyEvent.KEYCODE_NUMPAD_ENTER))) {
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
                    return true;
                }
                return false;
            }
        });

        DB db = DB.getInstance(context);
        db.message().liveFts().observe(getViewLifecycleOwner(), new Observer<TupleFtsStats>() {
            private TupleFtsStats last = null;

            @Override
            public void onChanged(TupleFtsStats stats) {
                if (stats == null)
                    cbSearchIndex.setText(R.string.title_search_use_index);
                else if (last == null || !last.equals(stats)) {
                    int perc = (int) (100 * stats.fts / (float) stats.total);
                    cbSearchIndex.setText(getString(R.string.title_name_count,
                            getString(R.string.title_search_use_index), perc + "%"));
                }
                last = stats;
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
                        cal.set(Calendar.MILLISECOND, 0);
                        cal.set(Calendar.SECOND, 0);
                        cal.set(Calendar.MINUTE, 0);
                        cal.set(Calendar.HOUR_OF_DAY, 0);

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
