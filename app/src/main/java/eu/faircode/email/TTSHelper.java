package eu.faircode.email;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import androidx.annotation.NonNull;

import java.util.Locale;

public class TTSHelper {
    private static boolean initialized;
    private static TextToSpeech instance;

    static void speak(
            @NonNull final Context context,
            @NonNull final String utteranceId,
            @NonNull final String text,
            @NonNull final Locale locale) {
        // https://developer.android.com/reference/android/speech/tts/TextToSpeech
        // https://android-developers.googleblog.com/2009/09/introduction-to-text-to-speech-in.html

        final Runnable speak = new Runnable() {
            @Override
            public void run() {
                boolean available = (instance.setLanguage(locale) >= 0);
                EntityLog.log(context, "TTS queued language=" + locale + " available=" + available + " text=" + text);
                instance.speak(text, TextToSpeech.QUEUE_ADD, null, utteranceId);
            }
        };

        if (initialized) {
            speak.run();
            return;
        }

        instance = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                initialized = (status == TextToSpeech.SUCCESS);
                Log.i("TTS status=" + status + " ok=" + initialized);
                if (initialized)
                    speak.run();
            }
        });
    }
}
