package eu.faircode.email;

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
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FilterQueryProvider;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.preference.PreferenceManager;

public class FragmentDialogSearch extends FragmentDialogBase {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View dview = LayoutInflater.from(getContext()).inflate(R.layout.dialog_search, null);

        final AutoCompleteTextView etQuery = dview.findViewById(R.id.etQuery);
        final ImageButton ibInfo = dview.findViewById(R.id.ibInfo);
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

        boolean pro = ActivityBilling.isPro(getContext());

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean fts = prefs.getBoolean("fts", false);
        boolean filter_seen = prefs.getBoolean("filter_seen", false);
        boolean filter_unflagged = prefs.getBoolean("filter_unflagged", false);
        boolean last_search_senders = prefs.getBoolean("last_search_senders", true);
        boolean last_search_recipients = prefs.getBoolean("last_search_recipients", true);
        boolean last_search_subject = prefs.getBoolean("last_search_subject", true);
        boolean last_search_keywords = prefs.getBoolean("last_search_keywords", false);
        boolean last_search_message = prefs.getBoolean("last_search_message", true);
        String last_search = prefs.getString("last_search", null);

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
                if (TextUtils.isEmpty(typed))
                    return cursor;

                String query = "%" + typed + "%";
                DB db = DB.getInstance(getContext());
                return db.message().getSuggestions("%" + query + "%");
            }
        });

        adapter.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
            @Override
            public CharSequence convertToString(Cursor cursor) {
                return cursor.getString(cursor.getColumnIndex("suggestion"));
            }
        });

        etQuery.setAdapter(adapter);

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

        cbSearchIndex.setChecked(fts && pro);
        cbSearchIndex.setEnabled(pro);
        cbSenders.setChecked(last_search_senders);
        cbRecipients.setChecked(last_search_recipients);
        cbSubject.setChecked(last_search_subject);
        cbKeywords.setChecked(last_search_keywords);
        cbMessage.setChecked(last_search_message);
        cbUnseen.setChecked(filter_seen);
        cbFlagged.setChecked(filter_unflagged);

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
                        long account = getArguments().getLong("account", -1);
                        long folder = getArguments().getLong("folder", -1);

                        BoundaryCallbackMessages.SearchCriteria criteria = new BoundaryCallbackMessages.SearchCriteria();

                        criteria.query = etQuery.getText().toString();
                        if (TextUtils.isEmpty(criteria.query))
                            criteria.query = null;
                        else
                            prefs.edit().putString("last_search", criteria.query).apply();

                        if (!cbSearchIndex.isChecked()) {
                            criteria.in_senders = cbSenders.isChecked();
                            criteria.in_receipients = cbRecipients.isChecked();
                            criteria.in_subject = cbSubject.isChecked();
                            criteria.in_keywords = cbKeywords.isChecked();
                            criteria.in_message = cbMessage.isChecked();
                            criteria.with_unseen = cbUnseen.isChecked();
                            criteria.with_flagged = cbFlagged.isChecked();
                            criteria.with_hidden = cbHidden.isChecked();
                            criteria.with_encrypted = cbEncrypted.isChecked();
                            criteria.with_attachments = cbAttachments.isChecked();
                        }

                        imm.hideSoftInputFromWindow(etQuery.getWindowToken(), 0);

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
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        imm.hideSoftInputFromWindow(etQuery.getWindowToken(), 0);
                    }
                })
                .create();

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
}
