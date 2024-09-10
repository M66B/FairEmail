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

    Copyright 2018-2024 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.os.OperationCanceledException;
import android.os.PowerManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TTSHelper {
    private static Integer status = null;
    private static TextToSpeech instance = null;
    private static PowerManager.WakeLock wl = null;
    private static final List<Runnable> queue = new ArrayList<>();
    private static final Map<String, Runnable> listeners = new HashMap<>();
    private static final Object lock = new Object();

    private static final long MAX_WAKELOCK_DURATION = 3 * 60 * 1000L; // milliseconds

    // https://developer.android.com/reference/android/speech/tts/TextToSpeech
    // https://android-developers.googleblog.com/2009/09/introduction-to-text-to-speech-in.html

    static void speak(
            @NonNull final Context context,
            @NonNull final String utteranceId,
            @NonNull final String text,
            final String language,
            final boolean flush,
            final Runnable listener) {

        if (wl == null) {
            PowerManager pm = Helper.getSystemService(context, PowerManager.class);
            wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, BuildConfig.APPLICATION_ID + ":tts");
        }

        Locale locale = (language == null ? Locale.getDefault() : new Locale(language));

        final Runnable speak = new Runnable() {
            @Override
            public void run() {
                try {
                    boolean available = (instance.setLanguage(locale) >= 0);
                    EntityLog.log(context, "TTS queued" +
                            " language=" + locale +
                            " available=" + available +
                            " utterance=" + utteranceId +
                            " text=" + text);
                    int error = instance.speak(text, flush ? TextToSpeech.QUEUE_FLUSH : TextToSpeech.QUEUE_ADD, null, utteranceId);
                    if (error != TextToSpeech.SUCCESS)
                        throw new OperationCanceledException("TTS error=" + error);
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }
        };

        synchronized (lock) {
            if (listener != null)
                listeners.put(utteranceId, listener);

            if (status == null) {
                queue.add(speak);
                if (instance == null)
                    try {
                        Log.i("TTS init");
                        instance = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
                            @Override
                            public void onInit(int initStatus) {
                                instance.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                                    @Override
                                    public void onStart(String utteranceId) {
                                        Log.i("TTS start=" + utteranceId);
                                    }

                                    @Override
                                    public void onDone(String utteranceId) {
                                        Log.i("TTS done=" + utteranceId);
                                        report(utteranceId);
                                        synchronized (lock) {
                                            if (queue.isEmpty())
                                                try {
                                                    Log.i("TTS shutdown");
                                                    instance.shutdown();
                                                } catch (Throwable ex) {
                                                    Log.e(ex);
                                                } finally {
                                                    status = null;
                                                    instance = null;
                                                    wl.release();
                                                }
                                        }
                                    }

                                    @Override
                                    public void onError(String utteranceId) {
                                        Log.i("TTS error=" + utteranceId);
                                        report(utteranceId);
                                    }

                                    private void report(String utteranceId) {
                                        synchronized (lock) {
                                            Runnable listener = listeners.remove(utteranceId);
                                            if (listener != null)
                                                ApplicationEx.getMainHandler().post(listener);
                                        }
                                    }
                                });

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
                        wl.acquire(MAX_WAKELOCK_DURATION);
                    } catch (Throwable ex) {
                        Log.e(ex);
                        status = TextToSpeech.ERROR;
                    }
            } else if (status == TextToSpeech.SUCCESS)
                speak.run();
        }
    }

    static int getMaxTextSize() {
        return TextToSpeech.getMaxSpeechInputLength();
    }
}
