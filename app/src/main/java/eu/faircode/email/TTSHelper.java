package eu.faircode.email;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

public class TTSHelper {
    private static boolean initialized;
    private static TextToSpeech instance;

    static void speak(Context context, final String utteranceId, final String text, final String language) {
        // https://developer.android.com/reference/android/speech/tts/TextToSpeech
        // https://android-developers.googleblog.com/2009/09/introduction-to-text-to-speech-in.html

        final Runnable speak = new Runnable() {
            @Override
            public void run() {
                if (language != null) {
                    Locale loc = new Locale(language);
                    if (instance.setLanguage(loc) < 0)
                        EntityLog.log(context, "TTS unavailable language=" + loc);
                }

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
