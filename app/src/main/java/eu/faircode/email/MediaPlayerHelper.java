package eu.faircode.email;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class MediaPlayerHelper {
    static final int DEFAULT_SOUND_DURATION = 30; // seconds
    static final int DEFAULT_ALARM_DURATION = 30; // seconds

    private static ExecutorService executor = Helper.getBackgroundExecutor(1, "media");

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
                    if (MediaPlayerHelper.isInCall(context))
                        return;
                    MediaPlayerHelper.play(context, uri, alarm, duration);
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

    static boolean isInCall(Context context) {
        AudioManager am =
                (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (am == null)
            return false;

        // This doesn't require READ_PHONE_STATE permission
        int mode = am.getMode();
        EntityLog.log(context, "Audio mode=" + mode);
        return (mode == AudioManager.MODE_RINGTONE ||
                mode == AudioManager.MODE_IN_CALL ||
                mode == AudioManager.MODE_IN_COMMUNICATION);
    }
}
