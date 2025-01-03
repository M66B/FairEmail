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

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.preference.PreferenceManager;

import com.google.android.material.textfield.TextInputLayout;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FragmentOptionsIntegrations extends FragmentBase implements SharedPreferences.OnSharedPreferenceChangeListener {
    private View view;
    private ImageButton ibHelp;

    private SwitchCompat swLanguageTool;
    private TextView tvLanguageToolPrivacy;
    private SwitchCompat swLanguageToolSentence;
    private SwitchCompat swLanguageToolAuto;
    private SwitchCompat swLanguageToolPicky;
    private SwitchCompat swLanguageToolHighlight;
    private SwitchCompat swLanguageToolDescription;
    private EditText etLanguageTool;
    private EditText etLanguageToolUser;
    private TextInputLayout tilLanguageToolKey;
    private ImageButton ibLanguageTool;
    private SwitchCompat swDeepL;
    private TextView tvDeepLPrivacy;
    private ImageButton ibDeepL;
    private SwitchCompat swVirusTotal;
    private TextView tvVirusTotalPrivacy;
    private TextInputLayout tilVirusTotal;
    private ImageButton ibVirusTotal;
    private SwitchCompat swSend;
    private EditText etSend;
    private ImageButton ibSend;
    private SwitchCompat swOpenAi;
    private TextView tvOpenAiPrivacy;
    private EditText etOpenAi;
    private TextInputLayout tilOpenAi;
    private EditText etOpenAiModel;
    private SwitchCompat swOpenMultiModal;
    private TextView tvOpenAiTemperature;
    private SeekBar sbOpenAiTemperature;
    private EditText etOpenAiSummarize;
    private EditText etOpenAiAnswer;
    private EditText etOpenAiSystem;
    private ImageButton ibOpenAi;
    private SwitchCompat swGemini;
    private TextView tvGeminiPrivacy;
    private EditText etGemini;
    private TextInputLayout tilGemini;
    private EditText etGeminiModel;
    private TextView tvGeminiTemperature;
    private SeekBar sbGeminiTemperature;
    private EditText etGeminiSummarize;
    private EditText etGeminiAnswer;
    private ImageButton ibGemini;

    private CardView cardVirusTotal;
    private CardView cardSend;
    private CardView cardOpenAi;
    private CardView cardGemini;

    private NumberFormat NF = NumberFormat.getNumberInstance();

    private final static List<String> RESET_OPTIONS = Collections.unmodifiableList(Arrays.asList(
            "lt_enabled", "lt_sentence", "lt_auto", "lt_picky", "lt_highlight", "lt_description", "lt_uri", "lt_user",
            "deepl_enabled",
            "vt_enabled",
            "send_enabled", "send_host", "send_dlimit", "send_tlimit",
            "openai_enabled", "openai_uri", "openai_model", "openai_multimodal", "openai_temperature", "openai_summarize", "openai_answer", "openai_system",
            "gemini_enabled", "gemini_uri", "gemini_model", "gemini_temperature", "gemini_summarize", "gemini_answer"
    ));

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_setup);
        setHasOptionsMenu(true);

        view = inflater.inflate(R.layout.fragment_options_integrations, container, false);

        // Get controls

        ibHelp = view.findViewById(R.id.ibHelp);

        swLanguageTool = view.findViewById(R.id.swLanguageTool);
        tvLanguageToolPrivacy = view.findViewById(R.id.tvLanguageToolPrivacy);
        swLanguageToolSentence = view.findViewById(R.id.swLanguageToolSentence);
        swLanguageToolAuto = view.findViewById(R.id.swLanguageToolAuto);
        swLanguageToolPicky = view.findViewById(R.id.swLanguageToolPicky);
        swLanguageToolHighlight = view.findViewById(R.id.swLanguageToolHighlight);
        swLanguageToolDescription = view.findViewById(R.id.swLanguageToolDescription);
        etLanguageTool = view.findViewById(R.id.etLanguageTool);
        etLanguageToolUser = view.findViewById(R.id.etLanguageToolUser);
        tilLanguageToolKey = view.findViewById(R.id.tilLanguageToolKey);
        ibLanguageTool = view.findViewById(R.id.ibLanguageTool);

        swDeepL = view.findViewById(R.id.swDeepL);
        tvDeepLPrivacy = view.findViewById(R.id.tvDeepLPrivacy);
        ibDeepL = view.findViewById(R.id.ibDeepL);

        swVirusTotal = view.findViewById(R.id.swVirusTotal);
        tvVirusTotalPrivacy = view.findViewById(R.id.tvVirusTotalPrivacy);
        tilVirusTotal = view.findViewById(R.id.tilVirusTotal);
        ibVirusTotal = view.findViewById(R.id.ibVirusTotal);

        swSend = view.findViewById(R.id.swSend);
        etSend = view.findViewById(R.id.etSend);
        ibSend = view.findViewById(R.id.ibSend);

        swOpenAi = view.findViewById(R.id.swOpenAi);
        tvOpenAiPrivacy = view.findViewById(R.id.tvOpenAiPrivacy);
        etOpenAi = view.findViewById(R.id.etOpenAi);
        tilOpenAi = view.findViewById(R.id.tilOpenAi);
        etOpenAiModel = view.findViewById(R.id.etOpenAiModel);
        swOpenMultiModal = view.findViewById(R.id.swOpenMultiModal);
        tvOpenAiTemperature = view.findViewById(R.id.tvOpenAiTemperature);
        sbOpenAiTemperature = view.findViewById(R.id.sbOpenAiTemperature);
        etOpenAiSummarize = view.findViewById(R.id.etOpenAiSummarize);
        etOpenAiAnswer = view.findViewById(R.id.etOpenAiAnswer);
        etOpenAiSystem = view.findViewById(R.id.etOpenAiSystem);
        ibOpenAi = view.findViewById(R.id.ibOpenAi);

        swGemini = view.findViewById(R.id.swGemini);
        tvGeminiPrivacy = view.findViewById(R.id.tvGeminiPrivacy);
        etGemini = view.findViewById(R.id.etGemini);
        tilGemini = view.findViewById(R.id.tilGemini);
        etGeminiModel = view.findViewById(R.id.etGeminiModel);
        tvGeminiTemperature = view.findViewById(R.id.tvGeminiTemperature);
        sbGeminiTemperature = view.findViewById(R.id.sbGeminiTemperature);
        etGeminiSummarize = view.findViewById(R.id.etGeminiSummarize);
        etGeminiAnswer = view.findViewById(R.id.etGeminiAnswer);
        ibGemini = view.findViewById(R.id.ibGemini);

        cardVirusTotal = view.findViewById(R.id.cardVirusTotal);
        cardSend = view.findViewById(R.id.cardSend);
        cardOpenAi = view.findViewById(R.id.cardOpenAi);
        cardGemini = view.findViewById(R.id.cardGemini);

        setOptions();

        // Wire controls

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        ibHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.view(v.getContext(), Helper.getSupportUri(v.getContext(), "Options:misc"), false);
            }
        });


        swLanguageTool.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("lt_enabled", checked).apply();
                swLanguageToolSentence.setEnabled(checked);
                swLanguageToolAuto.setEnabled(checked);
                swLanguageToolPicky.setEnabled(checked);
                swLanguageToolHighlight.setEnabled(checked);
                swLanguageToolDescription.setEnabled(checked);
            }
        });

        tvLanguageToolPrivacy.getPaint().setUnderlineText(true);
        tvLanguageToolPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.view(v.getContext(), Uri.parse(Helper.LT_PRIVACY_URI), true);
            }
        });

        swLanguageToolSentence.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("lt_sentence", checked).apply();
            }
        });

        swLanguageToolAuto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("lt_auto", checked).apply();
            }
        });

        swLanguageToolPicky.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("lt_picky", checked).apply();
            }
        });

        swLanguageToolHighlight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("lt_highlight", checked).apply();
            }
        });

        swLanguageToolDescription.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("lt_description", checked).apply();
            }
        });

        etLanguageTool.setHint(LanguageTool.LT_URI);
        etLanguageTool.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                String apikey = s.toString().trim();
                if (TextUtils.isEmpty(apikey))
                    prefs.edit().remove("lt_uri").apply();
                else
                    prefs.edit().putString("lt_uri", apikey).apply();
            }
        });

        etLanguageToolUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                String apikey = s.toString().trim();
                if (TextUtils.isEmpty(apikey))
                    prefs.edit().remove("lt_user").apply();
                else
                    prefs.edit().putString("lt_user", apikey).apply();
            }
        });

        tilLanguageToolKey.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                String apikey = s.toString().trim();
                if (TextUtils.isEmpty(apikey))
                    prefs.edit().remove("lt_key").apply();
                else
                    prefs.edit().putString("lt_key", apikey).apply();
            }
        });

        ibLanguageTool.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 180);
            }
        });

        tvDeepLPrivacy.getPaint().setUnderlineText(true);
        tvDeepLPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.view(v.getContext(), Uri.parse(DeepL.PRIVACY_URI), true);
            }
        });

        swDeepL.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("deepl_enabled", checked).apply();
            }
        });

        ibDeepL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeepL.FragmentDialogDeepL fragment = new DeepL.FragmentDialogDeepL();
                fragment.show(getParentFragmentManager(), "deepl:configure");
            }
        });

        swVirusTotal.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("vt_enabled", checked).apply();
            }
        });

        tvVirusTotalPrivacy.getPaint().setUnderlineText(true);
        tvVirusTotalPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.view(v.getContext(), Uri.parse(VirusTotal.URI_PRIVACY), true);
            }
        });

        tilVirusTotal.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                String apikey = s.toString().trim();
                if (TextUtils.isEmpty(apikey))
                    prefs.edit().remove("vt_apikey").apply();
                else
                    prefs.edit().putString("vt_apikey", apikey).apply();
            }
        });

        ibVirusTotal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 181);
            }
        });

        swSend.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("send_enabled", checked).apply();
            }
        });

        etSend.setHint(Send.DEFAULT_SERVER);
        etSend.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                String apikey = s.toString().trim();
                if (TextUtils.isEmpty(apikey))
                    prefs.edit().remove("send_host").apply();
                else
                    prefs.edit().putString("send_host", apikey).apply();
            }
        });

        ibSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 183);
            }
        });

        swOpenAi.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("openai_enabled", checked).apply();
                etOpenAiModel.setEnabled(checked);
                swOpenMultiModal.setEnabled(checked);
                sbOpenAiTemperature.setEnabled(checked);
                etOpenAiSummarize.setEnabled(checked);
                etOpenAiAnswer.setEnabled(checked);
                etOpenAiSystem.setEnabled(checked);
                if (checked)
                    swGemini.setChecked(false);
            }
        });

        tvOpenAiPrivacy.getPaint().setUnderlineText(true);
        tvOpenAiPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.view(v.getContext(), Uri.parse(BuildConfig.OPENAI_PRIVACY), true);
            }
        });

        etOpenAi.setHint(BuildConfig.OPENAI_ENDPOINT);
        etOpenAi.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                String apikey = s.toString().trim();
                if (TextUtils.isEmpty(apikey))
                    prefs.edit().remove("openai_uri").apply();
                else
                    prefs.edit().putString("openai_uri", apikey).apply();
            }
        });

        tilOpenAi.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                String apikey = s.toString().trim();
                if (TextUtils.isEmpty(apikey))
                    prefs.edit().remove("openai_apikey").apply();
                else
                    prefs.edit().putString("openai_apikey", apikey).apply();
            }
        });

        etOpenAiModel.setHint(OpenAI.DEFAULT_MODEL);
        etOpenAiModel.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                String model = s.toString().trim();
                if (TextUtils.isEmpty(model))
                    prefs.edit().remove("openai_model").apply();
                else
                    prefs.edit().putString("openai_model", model).apply();
            }
        });

        swOpenMultiModal.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
                prefs.edit().putBoolean("openai_multimodal", checked).apply();
            }
        });

        sbOpenAiTemperature.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float temp = progress / 10f;
                prefs.edit().putFloat("openai_temperature", temp).apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }
        });

        etOpenAiSummarize.setHint(OpenAI.DEFAULT_SUMMARY_PROMPT);
        etOpenAiSummarize.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                String prompt = s.toString().trim();
                if (TextUtils.isEmpty(prompt))
                    prefs.edit().remove("openai_summarize").apply();
                else
                    prefs.edit().putString("openai_summarize", prompt).apply();
            }
        });

        etOpenAiAnswer.setHint(OpenAI.DEFAULT_ANSWER_PROMPT);
        etOpenAiAnswer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                String prompt = s.toString().trim();
                if (TextUtils.isEmpty(prompt))
                    prefs.edit().remove("openai_answer").apply();
                else
                    prefs.edit().putString("openai_answer", prompt).apply();
            }
        });

        etOpenAiSystem.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                String prompt = s.toString().trim();
                if (TextUtils.isEmpty(prompt))
                    prefs.edit().remove("openai_system").apply();
                else
                    prefs.edit().putString("openai_system", prompt).apply();
            }
        });

        ibOpenAi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 190);
            }
        });

        swGemini.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("gemini_enabled", checked).apply();
                etGeminiModel.setEnabled(checked);
                sbGeminiTemperature.setEnabled(checked);
                etGeminiSummarize.setEnabled(checked);
                etGeminiAnswer.setEnabled(checked);
                if (checked)
                    swOpenAi.setChecked(false);
            }
        });

        tvGeminiPrivacy.getPaint().setUnderlineText(true);
        tvGeminiPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.view(v.getContext(), Uri.parse(BuildConfig.GEMINI_PRIVACY), true);
            }
        });

        etGemini.setHint(BuildConfig.GEMINI_ENDPOINT);
        etGemini.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                String apikey = s.toString().trim();
                if (TextUtils.isEmpty(apikey))
                    prefs.edit().remove("gemini_uri").apply();
                else
                    prefs.edit().putString("gemini_uri", apikey).apply();
            }
        });

        tilGemini.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                String apikey = s.toString().trim();
                if (TextUtils.isEmpty(apikey))
                    prefs.edit().remove("gemini_apikey").apply();
                else
                    prefs.edit().putString("gemini_apikey", apikey).apply();
            }
        });

        etGeminiModel.setHint(Gemini.DEFAULT_MODEL);
        etGeminiModel.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                String model = s.toString().trim();
                if (TextUtils.isEmpty(model))
                    prefs.edit().remove("gemini_model").apply();
                else
                    prefs.edit().putString("gemini_model", model).apply();
            }
        });

        sbGeminiTemperature.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float temp = progress / 10f;
                prefs.edit().putFloat("gemini_temperature", temp).apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }
        });

        etGeminiSummarize.setHint(Gemini.DEFAULT_SUMMARY_PROMPT);
        etGeminiSummarize.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                String prompt = s.toString().trim();
                if (TextUtils.isEmpty(prompt))
                    prefs.edit().remove("gemini_summarize").apply();
                else
                    prefs.edit().putString("gemini_summarize", prompt).apply();
            }
        });

        etGeminiAnswer.setHint(Gemini.DEFAULT_ANSWER_PROMPT);
        etGeminiAnswer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                String prompt = s.toString().trim();
                if (TextUtils.isEmpty(prompt))
                    prefs.edit().remove("gemini_answer").apply();
                else
                    prefs.edit().putString("gemini_answer", prompt).apply();
            }
        });

        ibGemini.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 204);
            }
        });

        // Initialize
        cardVirusTotal.setVisibility(BuildConfig.PLAY_STORE_RELEASE ? View.GONE : View.VISIBLE);
        cardSend.setVisibility(BuildConfig.PLAY_STORE_RELEASE ? View.GONE : View.VISIBLE);
        cardOpenAi.setVisibility(TextUtils.isEmpty(BuildConfig.OPENAI_ENDPOINT) ? View.GONE : View.VISIBLE);
        cardGemini.setVisibility(TextUtils.isEmpty(BuildConfig.GEMINI_ENDPOINT) ? View.GONE : View.VISIBLE);

        PreferenceManager.getDefaultSharedPreferences(getContext()).registerOnSharedPreferenceChangeListener(this);

        return view;
    }

    @Override
    public void onDestroyView() {
        PreferenceManager.getDefaultSharedPreferences(getContext()).unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroyView();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (!RESET_OPTIONS.contains(key))
            return;

        if ("lt_uri".equals(key) ||
                "lt_user".equals(key) ||
                "lt_key".equals(key) ||
                "vt_apikey".equals(key) ||
                "send_host".equals(key) ||
                "openai_uri".equals(key) ||
                "openai_apikey".equals(key) ||
                "openai_model".equals(key) ||
                "openai_summarize".equals(key) ||
                "openai_answer".equals(key) ||
                "openai_system".equals(key) ||
                "gemini_uri".equals(key) ||
                "gemini_apikey".equals(key) ||
                "gemini_model".equals(key) ||
                "gemini_summarize".equals(key) ||
                "gemini_answer".equals(key))
            return;

        getMainHandler().removeCallbacks(update);
        getMainHandler().postDelayed(update, FragmentOptions.DELAY_SETOPTIONS);
    }

    private Runnable update = new RunnableEx("misc") {
        @Override
        protected void delegate() {
            setOptions();
        }
    };

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_options, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_default) {
            FragmentOptions.reset(getContext(), RESET_OPTIONS, null);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setOptions() {
        try {
            if (view == null || getContext() == null)
                return;

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

            swLanguageTool.setChecked(prefs.getBoolean("lt_enabled", false));
            swLanguageToolSentence.setChecked(prefs.getBoolean("lt_sentence", false));
            swLanguageToolSentence.setEnabled(swLanguageTool.isChecked());
            swLanguageToolAuto.setChecked(prefs.getBoolean("lt_auto", true));
            swLanguageToolAuto.setEnabled(swLanguageTool.isChecked());
            swLanguageToolPicky.setChecked(prefs.getBoolean("lt_picky", false));
            swLanguageToolPicky.setEnabled(swLanguageTool.isChecked());
            swLanguageToolHighlight.setChecked(prefs.getBoolean("lt_highlight", !BuildConfig.PLAY_STORE_RELEASE));
            swLanguageToolHighlight.setEnabled(swLanguageTool.isChecked());
            swLanguageToolDescription.setChecked(prefs.getBoolean("lt_description", false));
            swLanguageToolDescription.setEnabled(swLanguageTool.isChecked());
            etLanguageTool.setText(prefs.getString("lt_uri", null));
            etLanguageToolUser.setText(prefs.getString("lt_user", null));
            tilLanguageToolKey.getEditText().setText(prefs.getString("lt_key", null));

            swDeepL.setChecked(prefs.getBoolean("deepl_enabled", false));

            swVirusTotal.setChecked(prefs.getBoolean("vt_enabled", false));
            tilVirusTotal.getEditText().setText(prefs.getString("vt_apikey", null));

            swSend.setChecked(prefs.getBoolean("send_enabled", false));
            etSend.setText(prefs.getString("send_host", null));

            swOpenAi.setChecked(prefs.getBoolean("openai_enabled", false));
            etOpenAi.setText(prefs.getString("openai_uri", null));
            tilOpenAi.getEditText().setText(prefs.getString("openai_apikey", null));
            etOpenAiModel.setText(prefs.getString("openai_model", null));
            etOpenAiModel.setEnabled(swOpenAi.isChecked());

            swOpenMultiModal.setChecked(prefs.getBoolean("openai_multimodal", false));
            swOpenMultiModal.setEnabled(swOpenAi.isChecked());

            float temperature = prefs.getFloat("openai_temperature", OpenAI.DEFAULT_TEMPERATURE);
            tvOpenAiTemperature.setText(getString(R.string.title_advanced_openai_temperature, NF.format(temperature)));
            sbOpenAiTemperature.setProgress(Math.round(temperature * 10));
            sbOpenAiTemperature.setEnabled(swOpenAi.isChecked());
            etOpenAiSummarize.setText(prefs.getString("openai_summarize", null));
            etOpenAiSummarize.setEnabled(swOpenAi.isChecked());
            etOpenAiAnswer.setText(prefs.getString("openai_answer", null));
            etOpenAiAnswer.setEnabled(swOpenAi.isChecked());
            etOpenAiSystem.setText(prefs.getString("openai_system", null));
            etOpenAiSystem.setEnabled(swOpenAi.isChecked());

            swGemini.setChecked(prefs.getBoolean("gemini_enabled", false));
            etGemini.setText(prefs.getString("gemini_uri", null));
            tilGemini.getEditText().setText(prefs.getString("gemini_apikey", null));
            etGeminiModel.setText(prefs.getString("gemini_model", null));
            etGeminiModel.setEnabled(swGemini.isChecked());

            temperature = prefs.getFloat("gemini_temperature", Gemini.DEFAULT_TEMPERATURE);
            tvGeminiTemperature.setText(getString(R.string.title_advanced_openai_temperature, NF.format(temperature)));
            sbGeminiTemperature.setProgress(Math.round(temperature * 10));
            sbGeminiTemperature.setEnabled(swGemini.isChecked());

            etGeminiSummarize.setText(prefs.getString("gemini_summarize", null));
            etGeminiSummarize.setEnabled(swGemini.isChecked());
            etGeminiAnswer.setText(prefs.getString("gemini_answer", null));
            etGeminiAnswer.setEnabled(swGemini.isChecked());
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }
}
