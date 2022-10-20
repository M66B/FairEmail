package eu.faircode.email;

import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class MediaPlayerHelper {
    static final int DEFAULT_SOUND_DURATION = 30; // seconds
    static final int DEFAULT_ALARM_DURATION = 30; // seconds

    private static final ExecutorService executor = Helper.getBackgroundExecutor(1, "media");

    static void queue(Context context, String uri) {
        try {
            queue(context, Uri.parse(uri), false, DEFAULT_SOUND_DURATION);
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    static void queue(Context context, Uri uri, boolean alarm, int duration) {
        Log.i("Queuing sound=" + uri);

        executor.submit(new Runnable() {
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
        Semaphore sem = new Semaphore(0);

        AudioAttributes attrs = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(alarm ? AudioAttributes.USAGE_ALARM : AudioAttributes.USAGE_NOTIFICATION)
                .build();

        MediaPlayer mediaPlayer = new MediaPlayer();
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
                mp.stop();
                mp.release();
                sem.release();
            }
        });
        mediaPlayer.prepareAsync();

        try {
            if (!sem.tryAcquire(duration, TimeUnit.SECONDS)) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }
        } catch (Throwable ex) {
            Log.w(ex);
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
                                am.addOnModeChangedListener(executor, listener);
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
