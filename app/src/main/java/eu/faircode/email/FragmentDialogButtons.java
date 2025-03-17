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

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;

public class FragmentDialogButtons extends FragmentDialogBase {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Context context = getContext();
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        final View dview = LayoutInflater.from(context).inflate(R.layout.dialog_buttons, null);
        final CheckBox cbSeen = dview.findViewById(R.id.cbSeen);
        final CheckBox cbHide = dview.findViewById(R.id.cbHide);
        final CheckBox cbImportance = dview.findViewById(R.id.cbImportance);
        final CheckBox cbJunk = dview.findViewById(R.id.cbJunk);
        final CheckBox cbTrash = dview.findViewById(R.id.cbTrash);
        final CheckBox cbArchive = dview.findViewById(R.id.cbArchive);
        final CheckBox cbMove = dview.findViewById(R.id.cbMove);
        final CheckBox cbCopy = dview.findViewById(R.id.cbCopy);
        final CheckBox cbNotes = dview.findViewById(R.id.cbNotes);
        final CheckBox cbRule = dview.findViewById(R.id.cbRule);
        final CheckBox cbKeywords = dview.findViewById(R.id.cbKeywords);
        final CheckBox cbLabels = dview.findViewById(R.id.cbLabels);
        final CheckBox cbSearch = dview.findViewById(R.id.cbSearch);
        final CheckBox cbSearchText = dview.findViewById(R.id.cbSearchText);
        final CheckBox cbTranslate = dview.findViewById(R.id.cbTranslate);
        final CheckBox cbTts = dview.findViewById(R.id.cbTts);
        final CheckBox cbSummarize = dview.findViewById(R.id.cbSummarize);
        final CheckBox cbFullScreen = dview.findViewById(R.id.cbFullScreen);
        final CheckBox cbForceLight = dview.findViewById(R.id.cbForceLight);
        final CheckBox cbEvent = dview.findViewById(R.id.cbEvent);
        final CheckBox cbShare = dview.findViewById(R.id.cbShare);
        final CheckBox cbPin = dview.findViewById(R.id.cbPin);
        final CheckBox cbPrint = dview.findViewById(R.id.cbPrint);
        final CheckBox cbHeaders = dview.findViewById(R.id.cbHeaders);
        final CheckBox cbHtml = dview.findViewById(R.id.cbHtml);
        final CheckBox cbRaw = dview.findViewById(R.id.cbRaw);
        final CheckBox cbUnsubscribe = dview.findViewById(R.id.cbUnsubscribe);
        final CheckBox cbAnswer = dview.findViewById(R.id.cbAnswer);

        cbTranslate.setVisibility(DeepL.isAvailable(context) ? View.VISIBLE : View.GONE);
        cbSummarize.setVisibility(AI.isAvailable(context) ? View.VISIBLE : View.GONE);
        cbPin.setVisibility(Shortcuts.can(context) ? View.VISIBLE : View.GONE);

        cbSeen.setChecked(prefs.getBoolean("button_seen", false));
        cbHide.setChecked(prefs.getBoolean("button_hide", false));
        cbImportance.setChecked(prefs.getBoolean("button_importance", false));
        cbJunk.setChecked(prefs.getBoolean("button_junk", true));
        cbTrash.setChecked(prefs.getBoolean("button_trash", true));
        cbArchive.setChecked(prefs.getBoolean("button_archive", true));
        cbMove.setChecked(prefs.getBoolean("button_move", true));
        cbCopy.setChecked(prefs.getBoolean("button_copy", false));
        cbNotes.setChecked(prefs.getBoolean("button_notes", false));
        cbRule.setChecked(prefs.getBoolean("button_rule", false));
        cbKeywords.setChecked(prefs.getBoolean("button_keywords", false));
        cbLabels.setChecked(prefs.getBoolean("button_labels", true));
        cbSearch.setChecked(prefs.getBoolean("button_search", false));
        cbSearchText.setChecked(prefs.getBoolean("button_search_text", false));
        cbTranslate.setChecked(prefs.getBoolean("button_translate", true));
        cbTts.setChecked(prefs.getBoolean("button_tts", false));
        cbSummarize.setChecked(prefs.getBoolean("button_summarize", false));
        cbFullScreen.setChecked(prefs.getBoolean("button_full_screen", false));
        cbForceLight.setChecked(prefs.getBoolean("button_force_light", true));
        cbEvent.setChecked(prefs.getBoolean("button_event", false));
        cbShare.setChecked(prefs.getBoolean("button_share", false));
        cbPin.setChecked(prefs.getBoolean("button_pin", false));
        cbPrint.setChecked(prefs.getBoolean("button_print", false));
        cbHeaders.setChecked(prefs.getBoolean("button_headers", false));
        cbHtml.setChecked(prefs.getBoolean("button_html", false));
        cbRaw.setChecked(prefs.getBoolean("button_raw", false));
        cbUnsubscribe.setChecked(prefs.getBoolean("button_unsubscribe", true));
        cbAnswer.setChecked(prefs.getBoolean("button_answer", false));

        boolean expand_all = prefs.getBoolean("expand_all", false);
        boolean expand_one = prefs.getBoolean("expand_one", true);
        boolean threading = prefs.getBoolean("threading", true);
        boolean swipe_reply = prefs.getBoolean("swipe_reply", false);
        cbAnswer.setVisibility((!expand_all && expand_one) || !threading || swipe_reply ? View.VISIBLE : View.GONE);

        return new AlertDialog.Builder(getContext())
                .setView(dview)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean("button_seen", cbSeen.isChecked());
                        editor.putBoolean("button_hide", cbHide.isChecked());
                        editor.putBoolean("button_importance", cbImportance.isChecked());
                        editor.putBoolean("button_junk", cbJunk.isChecked());
                        editor.putBoolean("button_trash", cbTrash.isChecked());
                        editor.putBoolean("button_archive", cbArchive.isChecked());
                        editor.putBoolean("button_move", cbMove.isChecked());
                        editor.putBoolean("button_copy", cbCopy.isChecked());
                        editor.putBoolean("button_notes", cbNotes.isChecked());
                        editor.putBoolean("button_rule", cbRule.isChecked());
                        editor.putBoolean("button_keywords", cbKeywords.isChecked());
                        editor.putBoolean("button_labels", cbLabels.isChecked());
                        editor.putBoolean("button_search", cbSearch.isChecked());
                        editor.putBoolean("button_search_text", cbSearchText.isChecked());
                        editor.putBoolean("button_translate", cbTranslate.isChecked());
                        editor.putBoolean("button_tts", cbTts.isChecked());
                        editor.putBoolean("button_summarize", cbSummarize.isChecked());
                        editor.putBoolean("button_full_screen", cbFullScreen.isChecked());
                        editor.putBoolean("button_force_light", cbForceLight.isChecked());
                        editor.putBoolean("button_event", cbEvent.isChecked());
                        editor.putBoolean("button_share", cbShare.isChecked());
                        editor.putBoolean("button_pin", cbPin.isChecked());
                        editor.putBoolean("button_print", cbPrint.isChecked());
                        editor.putBoolean("button_headers", cbHeaders.isChecked());
                        editor.putBoolean("button_html", cbHtml.isChecked());
                        editor.putBoolean("button_raw", cbRaw.isChecked());
                        editor.putBoolean("button_unsubscribe", cbUnsubscribe.isChecked());
                        editor.putBoolean("button_answer", cbAnswer.isChecked());
                        editor.apply();
                        sendResult(Activity.RESULT_OK);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sendResult(Activity.RESULT_CANCELED);
                    }
                })
                .create();
    }
}
