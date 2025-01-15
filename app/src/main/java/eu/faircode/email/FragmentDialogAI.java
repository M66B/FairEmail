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

import static android.app.Activity.RESULT_OK;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

public class FragmentDialogAI extends FragmentDialogBase {
    private Spinner spPrompt;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Context context = getContext();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String systemPrompt = prefs.getString("openai_system", null);

        Bundle args = getArguments();
        boolean has_body = args.getBoolean("has_body");
        boolean has_reply = args.getBoolean("has_reply");
        boolean has_system = (OpenAI.isAvailable(context) && !TextUtils.isEmpty(systemPrompt));

        final View view = LayoutInflater.from(context).inflate(R.layout.dialog_ai, null);
        final ImageButton ibInfo = view.findViewById(R.id.ibInfo);
        spPrompt = view.findViewById(R.id.spPrompt);
        final EditText etPrompt = view.findViewById(R.id.etPrompt);
        final CheckBox cbInputSystem = view.findViewById(R.id.cbInputSystem);
        final CheckBox cbInputBody = view.findViewById(R.id.cbInputBody);
        final CheckBox cbInputReply = view.findViewById(R.id.cbInputReply);
        final ContentLoadingProgressBar pbWait = view.findViewById(R.id.pbWait);

        ibInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 190);
            }
        });

        ArrayAdapter<Pair<String, String>> adapter = new ArrayAdapter<Pair<String, String>>(context, android.R.layout.simple_spinner_item, android.R.id.text1) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                return _getView(position, super.getView(position, convertView, parent));
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                return _getView(position, super.getDropDownView(position, convertView, parent));
            }

            private View _getView(int position, View view) {
                Pair<String, String> prompt = getItem(position);
                TextView tv = view.findViewById(android.R.id.text1);
                tv.setText(prompt == null ? null : prompt.first);
                return view;
            }

        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPrompt.setAdapter(adapter);

        spPrompt.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Object tag = spPrompt.getTag();
                if (tag != null && tag.equals(position))
                    return;
                spPrompt.setTag(position);
                Pair<String, String> selected = adapter.getItem(position);
                etPrompt.setText(selected == null ? null : selected.second);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                etPrompt.setText(null);
            }
        });

        cbInputSystem.setChecked(has_system);
        cbInputBody.setChecked(has_body);

        if (BuildConfig.DEBUG) {
            cbInputSystem.setEnabled(has_system);
            cbInputBody.setEnabled(has_body);
            cbInputReply.setEnabled(has_reply);
        } else {
            cbInputSystem.setVisibility(has_system ? View.VISIBLE : View.GONE);
            cbInputBody.setVisibility(has_body ? View.VISIBLE : View.GONE);
            cbInputReply.setVisibility(has_reply ? View.VISIBLE : View.GONE);
        }

        new SimpleTask<List<Pair<String, String>>>() {
            @Override
            protected void onPreExecute(Bundle args) {
                spPrompt.setEnabled(false);
                etPrompt.setEnabled(false);
                pbWait.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onPostExecute(Bundle args) {
                spPrompt.setEnabled(true);
                etPrompt.setEnabled(true);
                pbWait.setVisibility(View.GONE);
            }

            @Override
            protected List<Pair<String, String>> onExecute(Context context, Bundle args) throws Throwable {
                DB db = DB.getInstance(context);

                List<Pair<String, String>> prompts = new ArrayList<>();
                List<EntityAnswer> answers = db.answer().getAiPrompts();
                if (answers != null)
                    for (EntityAnswer answer : answers) {
                        String html = answer.getData(context, null).getHtml();
                        prompts.add(new Pair<>(answer.name, JsoupEx.parse(html).body().text()));
                    }

                return prompts;
            }

            @Override
            protected void onExecuted(Bundle args, List<Pair<String, String>> prompts) {
                prompts.add(0, new Pair<>(context.getString(R.string.title_advanced_default_prompt), AI.getDefaultPrompt(getContext())));
                adapter.addAll(prompts);

                if (savedInstanceState != null) {
                    int prompt = savedInstanceState.getInt("fair:prompt");
                    spPrompt.setTag(prompt);
                    spPrompt.setSelection(prompt);
                }
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }

        }.execute(this, new Bundle(), "ai:prompts");

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setView(view)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Bundle args = getArguments();
                        args.putString("prompt", etPrompt.getText().toString());
                        args.putBoolean("input_system", has_system && cbInputSystem.isChecked());
                        args.putBoolean("input_body", has_body && cbInputBody.isChecked());
                        args.putBoolean("input_reply", has_reply && cbInputReply.isChecked());
                        sendResult(RESULT_OK);
                    }
                });

        return builder.create();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt("fair:prompt", spPrompt == null ? 0 : spPrompt.getSelectedItemPosition());
        super.onSaveInstanceState(outState);
    }
}
