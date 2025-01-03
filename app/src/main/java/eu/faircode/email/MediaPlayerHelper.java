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

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.util.Pair;

import androidx.core.app.NotificationCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class MediaPlayerHelper {
    static final int DEFAULT_SOUND_DURATION = 30; // seconds
    static final int DEFAULT_ALARM_DURATION = 30; // seconds

    private static Semaphore sem;
    private static final Object lock = new Object();

    private static MediaPlayer player = null;
    private static Uri uri = null;
    private static Runnable onCompleted = null;

    static void stop(Context context) {
        EntityLog.log(context, "Alarm stop");
        synchronized (lock) {
            if (sem != null)
                sem.release();
        }
    }

    static void queue(Context context, String uri) {
        try {
            queue(context, Uri.parse(uri), false, DEFAULT_SOUND_DURATION);
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    static void queue(Context context, Uri uri, boolean alarm, int duration) {
        Log.i("Queuing sound=" + uri);

        Helper.getMediaTaskExecutor().submit(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!alarm && (isInCall(context) || isDnd(context)))
                        return;
                    play(context, uri, alarm, duration);
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }
        });
    }

    private static void play(Context context, Uri uri, boolean alarm, int duration) throws IOException {
        synchronized (lock) {
            sem = new Semaphore(0);
        }

        Log.i("Playing sound=" + uri);

        AudioAttributes attrs = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(alarm ? AudioAttributes.USAGE_ALARM : AudioAttributes.USAGE_NOTIFICATION)
                .build();

        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setAudioAttributes(attrs);
            mediaPlayer.setDataSource(context.getApplicationContext(), uri);
            mediaPlayer.setLooping(false);
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    sem.release();
                }
            });
            mediaPlayer.prepareAsync();

            NotificationManager nm = Helper.getSystemService(context, NotificationManager.class);
            try {
                if (alarm) {
                    Intent intent = new Intent(context, ServiceUI.class)
                            .setAction("alarm");
                    PendingIntent piStop = PendingIntentCompat.getService(
                            context, ServiceUI.PI_ALARM, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                    NotificationCompat.Action.Builder actionStop = new NotificationCompat.Action.Builder(
                            R.drawable.twotone_stop_24,
                            context.getString(R.string.title_rule_alarm_stop),
                            piStop)
                            .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_MUTE)
                            .setShowsUserInterface(false)
                            .setAllowGeneratedReplies(false);

                    NotificationCompat.Builder builder =
                            new NotificationCompat.Builder(context, "alerts")
                                    .setSmallIcon(R.drawable.baseline_warning_white_24)
                                    .setContentTitle(context.getString(R.string.title_rule_alarm_title))
                                    .setSilent(true)
                                    .setAutoCancel(false)
                                    .addAction(actionStop.build())
                                    .setShowWhen(true)
                                    .setPriority(NotificationCompat.PRIORITY_MAX)
                                    .setOnlyAlertOnce(true)
                                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

                    nm.notify("alarm", 1, builder.build());
                }

                boolean acquired = sem.tryAcquire(duration, TimeUnit.SECONDS);
                EntityLog.log(context, "Alarm acquired=" + acquired);
                mediaPlayer.stop();
            } catch (Throwable ex) {
                Log.w(ex);
            } finally {
                if (alarm)
                    nm.cancel("alarm", 1);
            }
        } finally {
            mediaPlayer.release();
        }

        Log.i("Played sound=" + uri);

        synchronized (lock) {
            sem = null;
        }
    }

    static void startMusic(Context context, Uri uri, Runnable onCompleted) throws IOException {
        synchronized (lock) {
            stopMusic(context);

            MediaPlayerHelper.uri = uri;
            MediaPlayerHelper.onCompleted = onCompleted;

            MediaPlayerHelper.player = new MediaPlayer();
            MediaPlayerHelper.player.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
            );
            MediaPlayerHelper.player.setDataSource(context, uri);
            MediaPlayerHelper.player.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK);
            MediaPlayerHelper.player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    // https://issuetracker.google.com/issues/36921987
                    mp.start();
                }
            });
            MediaPlayerHelper.player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopMusic(context);
                }
            });
            MediaPlayerHelper.player.prepareAsync();
        }
    }

    static void stopMusic(Context context) {
        synchronized (lock) {
            if (MediaPlayerHelper.player != null)
                try {
                    MediaPlayerHelper.player.stop();
                } catch (Throwable ex) {
                    Log.e(ex);
                } finally {
                    MediaPlayerHelper.player = null;
                }
            MediaPlayerHelper.uri = null;
            if (MediaPlayerHelper.onCompleted != null) {
                MediaPlayerHelper.onCompleted.run();
                MediaPlayerHelper.onCompleted = null;
            }
        }
    }

    static boolean isPlaying(Uri uri) {
        synchronized (lock) {
            return (Objects.equals(MediaPlayerHelper.uri, uri));
        }
    }

    static Pair<Integer, Integer> getPosition(Uri uri) {
        synchronized (lock) {
            if (!isPlaying(uri))
                return null;
            return new Pair<>(
                    MediaPlayerHelper.player.getCurrentPosition(),
                    MediaPlayerHelper.player.getDuration()
            );
        }
    }

    static void liveInCall(Context context, LifecycleOwner owner, IInCall intf) {
        AudioManager am = Helper.getSystemService(context, AudioManager.class);
        if (am == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            intf.onChanged(false);
            Log.i("Audio mode legacy");
        } else {
            AudioManager.OnModeChangedListener listener = new AudioManager.OnModeChangedListener() {
                @Override
                public void onModeChanged(int mode) {
                    ApplicationEx.getMainHandler().post(new RunnableEx("AudioMode") {
                        @Override
                        public void delegate() {
                            intf.onChanged(isInCall(mode));
                        }
                    });
                }
            };
            listener.onModeChanged(am.getMode()); // Init

            owner.getLifecycle().addObserver(new LifecycleObserver() {
                private boolean registered = false;

                @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
                public void onStateChanged() {
                    try {
                        if (owner.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                            if (!registered) {
                                am.addOnModeChangedListener(Helper.getParallelExecutor(), listener);
                                registered = true;
                            }
                        } else {
                            if (registered) {
                                am.removeOnModeChangedListener(listener);
                                registered = false;
                            }
                        }
                        Log.i("Audio mode registered=" + registered);
                    } catch (Throwable ex) {
                        Log.e(ex);
                    }
                }
            });
        }
    }

    static boolean isInCall(Context context) {
        AudioManager am = Helper.getSystemService(context, AudioManager.class);
        if (am == null)
            return false;

        try {
            // This doesn't require READ_PHONE_STATE permission
            int mode = am.getMode();
            Log.i("Audio mode=" + mode);
            return isInCall(mode);
        } catch (Throwable ex) {
            Log.e(ex);
            return false;
        }
    }

    static boolean isDnd(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return false;
        NotificationManager nm = Helper.getSystemService(context, NotificationManager.class);
        int filter = nm.getCurrentInterruptionFilter();
        // All: no notifications are suppressed
        return (filter != NotificationManager.INTERRUPTION_FILTER_ALL);
    }

    static boolean isInCall(int mode) {
        return (mode == AudioManager.MODE_RINGTONE ||
                mode == AudioManager.MODE_IN_CALL ||
                mode == AudioManager.MODE_IN_COMMUNICATION);
    }

    interface IInCall {
        void onChanged(boolean inCall);
    }
}
