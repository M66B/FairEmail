package eu.faircode.email;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;

import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class MediaPlayerHelper {
    private static final int MAX_DURATION = 30; // seconds

    static void play(Context context, Uri uri, boolean alarm) throws IOException {
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
            if (!sem.tryAcquire(MAX_DURATION, TimeUnit.SECONDS)) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }
        } catch (Throwable ex) {
            Log.w(ex);
        }
    }
}
