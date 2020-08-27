package eu.faircode.email;

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
import android.view.inputmethod.InputMethodManager;
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
import java.util.Calendar;
import java.util.List;

public class FragmentDialogSearch extends FragmentDialogBase {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final long account = getArguments().getLong("account", -1);
        final long folder = getArguments().getLong("folder", -1);

        boolean pro = ActivityBilling.isPro(getContext());

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean fts = prefs.getBoolean("fts", false);
        boolean last_search_senders = prefs.getBoolean("last_search_senders", true);
        boolean last_search_recipients = prefs.getBoolean("last_search_recipients", true);
        boolean last_search_subject = prefs.getBoolean("last_search_subject", true);
        boolean last_search_keywords = prefs.getBoolean("last_search_keywords", false);
        boolean last_search_message = prefs.getBoolean("last_search_message", true);
        String last_search = prefs.getString("last_search", null);

        View dview = LayoutInflater.from(getContext()).inflate(R.layout.dialog_search, null);

        final AutoCompleteTextView etQuery = dview.findViewById(R.id.etQuery);
        final ImageButton ibAttachment = dview.findViewById(R.id.ibAttachment);
        final ImageButton ibEvent = dview.findViewById(R.id.ibInvite);
        final ImageButton ibUnseen = dview.findViewById(R.id.ibUnseen);
        final ImageButton ibFlagged = dview.findViewById(R.id.ibFlagged);
        final ImageButton ibInfo = dview.findViewById(R.id.ibInfo);
        final ImageButton ibMore = dview.findViewById(R.id.ibMore);
        final TextView tvMore = dview.findViewById(R.id.tvMore);
        final CheckBox cbSearchIndex = dview.findViewById(R.id.cbSearchIndex);
        final CheckBox cbSenders = dview.findViewById(R.id.cbSenders);
        final CheckBox cbRecipients = dview.findViewById(R.id.cbRecipients);
        final CheckBox cbSubject = dview.findViewById(R.id.cbSubject);
        final CheckBox cbKeywords = dview.findViewById(R.id.cbKeywords);
        final CheckBox cbMessage = dview.findViewById(R.id.cbMessage);
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

        final InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        ibInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imm.hideSoftInputFromWindow(etQuery.getWindowToken(), 0);
                Helper.viewFAQ(getContext(), 13);
            }
        });

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                getContext(),
                R.layout.search_suggestion,
                null,
                new String[]{"suggestion"},
                new int[]{android.R.id.text1},
                0);

        adapter.setFilterQueryProvider(new FilterQueryProvider() {
            public Cursor runQuery(CharSequence typed) {
                Log.i("Search suggest=" + typed);

                MatrixCursor cursor = new MatrixCursor(new String[]{"_id", "suggestion"});
                if (TextUtils.isEmpty(typed) || cbSearchIndex.isChecked())
                    return cursor;

                String query = "%" + typed + "%";
                DB db = DB.getInstance(getContext());
                return db.message().getSuggestions(
                        account < 0 ? null : account,
                        folder < 0 ? null : folder,
                        "%" + query + "%");
            }
        });

        adapter.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
            @Override
            public CharSequence convertToString(Cursor cursor) {
                return cursor.getString(cursor.getColumnIndex("suggestion"));
            }
        });

        etQuery.setAdapter(adapter);

        View.OnClickListener onMore = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imm.hideSoftInputFromWindow(etQuery.getWindowToken(), 0);

                if (grpMore.getVisibility() == View.VISIBLE) {
                    ibMore.setImageLevel(1);
                    grpMore.setVisibility(View.GONE);
                } else {
                    ibMore.setImageLevel(0);
                    grpMore.setVisibility(View.VISIBLE);
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
        tvAfter.setText(null);
        tvBefore.setText(null);

        grpMore.setVisibility(View.GONE);

        if (!TextUtils.isEmpty(last_search)) {
            etQuery.setText(last_search);
            etQuery.setSelection(0, last_search.length());
        }

        etQuery.requestFocus();
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        final AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dview)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BoundaryCallbackMessages.SearchCriteria criteria = new BoundaryCallbackMessages.SearchCriteria();

                        criteria.query = etQuery.getText().toString();
                        if (TextUtils.isEmpty(criteria.query))
                            criteria.query = null;
                        else
                            prefs.edit().putString("last_search", criteria.query).apply();

                        if (!cbSearchIndex.isChecked()) {
                            criteria.in_senders = cbSenders.isChecked();
                            criteria.in_recipients = cbRecipients.isChecked();
                            criteria.in_subject = cbSubject.isChecked();
                            criteria.in_keywords = cbKeywords.isChecked();
                            criteria.in_message = cbMessage.isChecked();
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

                        imm.hideSoftInputFromWindow(etQuery.getWindowToken(), 0);

                        if (criteria.query != null && criteria.query.startsWith("raw:"))
                            new SimpleTask<EntityFolder>() {
                                @Override
                                protected EntityFolder onExecute(Context context, Bundle args) throws Throwable {
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
                                            getContext(), getViewLifecycleOwner(), getParentFragmentManager(),
                                            account,
                                            archive == null ? folder : archive.id,
                                            archive != null,
                                            criteria);
                                }

                                @Override
                                protected void onException(Bundle args, Throwable ex) {
                                    Log.unexpectedError(getParentFragmentManager(), ex);
                                }
                            }.execute(getContext(), getViewLifecycleOwner(), getArguments(), "search:raw");
                        else
                            FragmentMessages.search(
                                    getContext(), getViewLifecycleOwner(), getParentFragmentManager(),
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
                switch (v.getId()) {
                    case R.id.ibAttachment:
                        criteria.with_attachments = true;
                        break;
                    case R.id.ibInvite:
                        criteria.with_attachments = true;
                        criteria.with_types = new String[]{"text/calendar"};
                        break;
                    case R.id.ibUnseen:
                        criteria.with_unseen = true;
                        break;
                    case R.id.ibFlagged:
                        criteria.with_flagged = true;
                        break;
                }

                FragmentMessages.search(
                        getContext(), getViewLifecycleOwner(), getParentFragmentManager(),
                        account, folder, false, criteria);
            }
        };

        ibAttachment.setOnClickListener(onClick);
        ibEvent.setOnClickListener(onClick);
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
