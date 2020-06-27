package eu.faircode.email;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.languageid.LanguageIdentification;
import com.google.mlkit.nl.languageid.LanguageIdentificationOptions;
import com.google.mlkit.nl.languageid.LanguageIdentifier;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import static android.app.Activity.RESULT_OK;

public class FragmentDialogTranslate extends FragmentDialogBase {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final String text = getArguments().getString("text");

        Map<String, String> map = new TreeMap<>();
        for (String lc : TranslateLanguage.getAllLanguages())
            map.put(new Locale(lc).getDisplayLanguage(), lc);

        String[] items = map.keySet().toArray(new String[0]);

        return new AlertDialog.Builder(getContext())
                .setTitle(R.string.title_translate)
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String targetLanguage = map.get(items[which]);

                        LanguageIdentifier languageIdentifier = LanguageIdentification.getClient(
                                new LanguageIdentificationOptions.Builder()
                                        .setConfidenceThreshold(0.34f)
                                        .build());
                        languageIdentifier.identifyLanguage(text)
                                .addOnSuccessListener(
                                        new OnSuccessListener<String>() {
                                            @Override
                                            public void onSuccess(@Nullable String sourceLanguage) {
                                                Log.i("Translate source=" + sourceLanguage);
                                                if (sourceLanguage.equals("und"))
                                                    sourceLanguage = TranslateLanguage.ENGLISH;

                                                TranslatorOptions options = new TranslatorOptions.Builder()
                                                        .setSourceLanguage(sourceLanguage)
                                                        .setTargetLanguage(targetLanguage)
                                                        .build();
                                                Translator translator = Translation.getClient(options);
                                                DownloadConditions conditions = new DownloadConditions.Builder()
                                                        .requireWifi()
                                                        .build();
                                                translator.downloadModelIfNeeded(conditions)
                                                        .addOnSuccessListener(
                                                                new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void v) {
                                                                        translator.translate(text)
                                                                                .addOnSuccessListener(
                                                                                        new OnSuccessListener<String>() {
                                                                                            @Override
                                                                                            public void onSuccess(@NonNull String translatedText) {
                                                                                                getArguments().putString("translated", translatedText);
                                                                                                sendResult(RESULT_OK);
                                                                                            }
                                                                                        })
                                                                                .addOnFailureListener(
                                                                                        new OnFailureListener() {
                                                                                            @Override
                                                                                            public void onFailure(@NonNull Exception ex) {
                                                                                                Log.unexpectedError(getParentFragmentManager(), ex);
                                                                                            }
                                                                                        });
                                                                    }
                                                                })
                                                        .addOnFailureListener(
                                                                new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception ex) {
                                                                        Log.unexpectedError(getParentFragmentManager(), ex);
                                                                    }
                                                                });
                                            }
                                        })
                                .addOnFailureListener(
                                        new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception ex) {
                                                Log.unexpectedError(getParentFragmentManager(), ex);
                                            }
                                        });
                    }
                })
                .create();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
    }
}
