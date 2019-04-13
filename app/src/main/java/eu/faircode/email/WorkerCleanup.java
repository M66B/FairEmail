package eu.faircode.email;

import android.content.Context;
import android.net.Uri;
import android.os.Build;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

public class WorkerCleanup extends Worker {
    private static final int CLEANUP_INTERVAL = 4; // hours
    private static final long CACHE_IMAGE_DURATION = 3 * 24 * 3600 * 1000L; // milliseconds
    private static final long KEEP_CONTACTS_DURATION = 180 * 24 * 3600 * 1000L; // milliseconds
    private static final long KEEP_LOG_DURATION = 24 * 3600 * 1000L; // milliseconds

    public WorkerCleanup(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        cleanup(getApplicationContext(), false);
        return Result.success();
    }

    static void cleanup(Context context, boolean manual) {
        DB db = DB.getInstance(context);
        try {
            Thread.currentThread().setPriority(THREAD_PRIORITY_BACKGROUND);
            Log.i("Start cleanup manual=" + manual);

            // Cleanup folders
            Log.i("Cleanup kept messages");
            for (EntityFolder folder : db.folder().getFolders()) {
                Calendar cal_keep = Calendar.getInstance();
                cal_keep.add(Calendar.DAY_OF_MONTH, -folder.keep_days);
                cal_keep.set(Calendar.HOUR_OF_DAY, 12);
                cal_keep.set(Calendar.MINUTE, 0);
                cal_keep.set(Calendar.SECOND, 0);
                cal_keep.set(Calendar.MILLISECOND, 0);

                long keep_time = cal_keep.getTimeInMillis();
                if (keep_time < 0)
                    keep_time = 0;

                int messages = db.message().deleteMessagesBefore(folder.id, keep_time);
                if (messages > 0)
                    Log.i("Cleanup folder=" + folder.account + "/" + folder.name +
                            " before=" + new Date(keep_time) + " deleted=" + messages);
            }

            long now = new Date().getTime();

            List<File> files = new ArrayList<>();
            File[] messages = new File(context.getFilesDir(), "messages").listFiles();
            File[] references = new File(context.getFilesDir(), "references").listFiles();
            File[] raws = new File(context.getFilesDir(), "raw").listFiles();

            if (messages != null)
                files.addAll(Arrays.asList(messages));
            if (references != null)
                files.addAll(Arrays.asList(references));
            if (raws != null)
                files.addAll(Arrays.asList(raws));

            // Cleanup message files
            Log.i("Cleanup message files");
            for (File file : files) {
                long id = Long.parseLong(file.getName());
                if (db.message().countMessage(id) == 0) {
                    Log.i("Deleting " + file);
                    if (!file.delete())
                        Log.w("Error deleting " + file);
                }
            }

            // Cleanup attachment files
            Log.i("Cleanup attachment files");
            File[] attachments = new File(context.getFilesDir(), "attachments").listFiles();
            if (attachments != null)
                for (File file : attachments) {
                    long id = Long.parseLong(file.getName().split("\\.")[0]);
                    if (db.attachment().countAttachment(id) == 0) {
                        Log.i("Deleting " + file);
                        if (!file.delete())
                            Log.w("Error deleting " + file);
                    }
                }

            // Cleanup cached images
            Log.i("Cleanup cached image files");
            File[] images = new File(context.getCacheDir(), "images").listFiles();
            if (images != null)
                for (File file : images)
                    if (file.isFile())
                        if (manual || now - file.lastModified() > CACHE_IMAGE_DURATION) {
                            Log.i("Deleting " + file);
                            if (!file.delete())
                                Log.w("Error deleting " + file);
                        }

            Log.i("Cleanup contacts");
            int contacts = db.contact().deleteContacts(now - KEEP_CONTACTS_DURATION);
            Log.i("Deleted contacts=" + contacts);

            Log.i("Cleanup log");
            int logs = db.log().deleteLogs(now - KEEP_LOG_DURATION);
            Log.i("Deleted logs=" + logs);

            Log.i("Update lookup URIs");
            ContactInfo.clearCache();
            List<EntityFolder> folders = db.folder().getSynchronizingFolders();
            for (EntityFolder folder : folders) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_MONTH, -folder.sync_days);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                long sync_time = cal.getTimeInMillis();
                if (sync_time < 0)
                    sync_time = 0;
                Log.i("Update lookup URIs " + folder.name + " before " + new Date(sync_time));

                List<TupleMessageLookup> avatars = db.message().getAvatars(folder.id, sync_time);
                for (TupleMessageLookup message : avatars) {
                    Uri uri = (message.avatar == null ? null : Uri.parse(message.avatar));
                    Uri lookup = ContactInfo.getLookupUri(context, message.from);
                    if (!Objects.equals(uri, lookup)) {
                        Log.i("Updating email=" + MessageHelper.formatAddresses(message.from) + " uri=" + lookup);
                        db.message().setMessageAvatar(message.id, lookup == null ? null : lookup.toString());
                    }
                }
            }
            Log.i("Updated lookup URIs");
        } catch (Throwable ex) {
            Log.e(ex);
        } finally {
            Log.i("End cleanup");
        }
    }

    static void queue() {
        String tag = WorkerCleanup.class.getSimpleName();
        Log.i("Queuing " + tag);

        try {
            for (WorkInfo info : WorkManager.getInstance().getWorkInfosByTag(tag).get())
                if (!info.getState().isFinished()) {
                    Log.i("Already queued " + tag);
                    return;
                }
        } catch (Throwable ex) {
            Log.w(ex);
        }

        Constraints.Builder constraints = new Constraints.Builder();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !BuildConfig.DEBUG)
            constraints.setRequiresDeviceIdle(true);

        PeriodicWorkRequest workRequest =
                new PeriodicWorkRequest.Builder(WorkerCleanup.class, CLEANUP_INTERVAL, TimeUnit.HOURS)
                        .addTag(tag)
                        .setConstraints(constraints.build())
                        .build();
        WorkManager.getInstance().enqueue(workRequest);

        Log.i("Queued " + tag);
    }

    static void cancel() {
        String tag = WorkerCleanup.class.getSimpleName();
        Log.i("Cancelling " + tag);
        WorkManager.getInstance().cancelAllWorkByTag(tag);
    }
}
