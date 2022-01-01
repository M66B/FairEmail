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

    Copyright 2018-2022 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.speech.tts.TextToSpeech;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TTSHelper {
    private static Integer status = null;
    private static TextToSpeech instance = null;
    private static List<Runnable> queue = new ArrayList<>();
    private static final Object lock = new Object();

    // https://developer.android.com/reference/android/speech/tts/TextToSpeech
    // https://android-developers.googleblog.com/2009/09/introduction-to-text-to-speech-in.html

    static void speak(
            @NonNull final Context context,
            @NonNull final String utteranceId,
            @NonNull final String text,
            @NonNull final Locale locale) {

        Runnable speak = new Runnable() {
            @Override
            public void run() {
                try {
                    boolean available = (instance.setLanguage(locale) >= 0);
                    EntityLog.log(context, "TTS queued" +
                            " language=" + locale +
                            " available=" + available +
                            " utterance=" + utteranceId +
                            " text=" + text);
                    instance.speak(text, TextToSpeech.QUEUE_ADD, null, utteranceId);
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }
        };

        synchronized (lock) {
            if (status == null) {
                queue.add(speak);
                if (instance == null)
                    try {
                        Log.i("TTS init");
                        instance = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
                            @Override
                            public void onInit(int initStatus) {
                                synchronized (lock) {
                                    status = initStatus;
                                    boolean ok = (status == TextToSpeech.SUCCESS);
                                    Log.i("TTS status=" + status + " ok=" + ok + " queued=" + queue.size());
                                    if (ok)
                                        for (Runnable speak : queue)
                                            speak.run();
                                    queue.clear();
                                }
                            }
                        });
                    } catch (Throwable ex) {
                        Log.e(ex);
                        status = TextToSpeech.ERROR;
                    }
            } else if (status == TextToSpeech.SUCCESS)
                speak.run();
        }
    }

    static void shutdown() {
        synchronized (lock) {
            if (instance != null)
                try {
                    Log.i("TTS shutdown");
                    instance.shutdown();
                    instance = null;
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            status = null;
        }
    }
}
