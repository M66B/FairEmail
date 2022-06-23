package com.bugsnag.android;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

abstract class FileStore {

    interface Delegate {

        /**
         * Invoked when an error report is not (de)serialized correctly
         *
         * @param exception the error encountered reading/delivering the file
         * @param errorFile file which could not be (de)serialized correctly
         * @param context   the context used to group the exception
         */
        void onErrorIOFailure(Exception exception, File errorFile, String context);
    }

    private final File storageDir;
    private final int maxStoreCount;
    private final Comparator<File> comparator;

    private final Lock lock = new ReentrantLock();
    private final Collection<File> queuedFiles = new ConcurrentSkipListSet<>();
    protected final Logger logger;
    private final EventStore.Delegate delegate;

    FileStore(@NonNull File storageDir,
              int maxStoreCount,
              Comparator<File> comparator,
              Logger logger,
              Delegate delegate) {
        this.maxStoreCount = maxStoreCount;
        this.comparator = comparator;
        this.logger = logger;
        this.delegate = delegate;
        this.storageDir = storageDir;
        isStorageDirValid(storageDir);
    }

    /**
     * Checks whether the storage directory is a writable directory. If it is not,
     * this method will attempt to create the directory.
     *
     * If the directory could not be created then an error will be logged.
     */
    private boolean isStorageDirValid(@NonNull File storageDir) {
        try {
            storageDir.mkdirs();
        } catch (Exception exception) {
            this.logger.e("Could not prepare file storage directory", exception);
            return false;
        }
        return true;
    }

    void enqueueContentForDelivery(String content, String filename) {
        if (!isStorageDirValid(storageDir)) {
            return;
        }
        discardOldestFileIfNeeded();

        lock.lock();
        Writer out = null;
        String filePath = new File(storageDir, filename).getAbsolutePath();
        try {
            FileOutputStream fos = new FileOutputStream(filePath);
            out = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
            out.write(content);
        } catch (Exception exc) {
            File eventFile = new File(filePath);

            if (delegate != null) {
                delegate.onErrorIOFailure(exc, eventFile, "NDK Crash report copy");
            }

            IOUtils.deleteFile(eventFile, logger);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception exception) {
                logger.w("Failed to close unsent payload writer: " + filename, exception);
            }
            lock.unlock();
        }
    }

    @Nullable
    String write(@NonNull JsonStream.Streamable streamable) {
        if (!isStorageDirValid(storageDir)) {
            return null;
        }
        if (maxStoreCount == 0) {
            return null;
        }
        discardOldestFileIfNeeded();
        String filename = new File(storageDir, getFilename(streamable)).getAbsolutePath();

        JsonStream stream = null;
        lock.lock();

        try {
            FileOutputStream fos = new FileOutputStream(filename);
            Writer out = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
            stream = new JsonStream(out);
            stream.value(streamable);
            logger.i("Saved unsent payload to disk: '" + filename + '\'');
            return filename;
        } catch (FileNotFoundException exc) {
            logger.w("Ignoring FileNotFoundException - unable to create file", exc);
        } catch (Exception exc) {
            File eventFile = new File(filename);

            if (delegate != null) {
                delegate.onErrorIOFailure(exc, eventFile, "Crash report serialization");
            }

            IOUtils.deleteFile(eventFile, logger);
        } finally {
            IOUtils.closeQuietly(stream);
            lock.unlock();
        }
        return null;
    }

    void discardOldestFileIfNeeded() {
        // Limit number of saved payloads to prevent disk space issues
        if (isStorageDirValid(storageDir)) {
            File[] listFiles = storageDir.listFiles();

            if (listFiles == null) {
                return;
            }

            List<File> files = new ArrayList<>(Arrays.asList(listFiles));

            if (files.size() >= maxStoreCount) {
                // Sort files then delete the first one (oldest timestamp)
                Collections.sort(files, comparator);

                for (int k = 0; k < files.size() && files.size() >= maxStoreCount; k++) {
                    File oldestFile = files.get(k);

                    if (!queuedFiles.contains(oldestFile)) {
                        logger.w("Discarding oldest error as stored "
                                + "error limit reached: '" + oldestFile.getPath() + '\'');
                        deleteStoredFiles(Collections.singleton(oldestFile));
                        files.remove(k);
                        k--;
                    }
                }
            }
        }
    }

    @NonNull
    abstract String getFilename(Object object);

    List<File> findStoredFiles() {
        lock.lock();
        try {
            List<File> files = new ArrayList<>();

            if (isStorageDirValid(storageDir)) {
                File[] values = storageDir.listFiles();

                if (values != null) {
                    for (File value : values) {
                        // delete any tombstoned/empty files, as they contain no useful info
                        if (value.length() == 0) {
                            if (!value.delete()) {
                                value.deleteOnExit();
                            }
                        } else if (value.isFile() && !queuedFiles.contains(value)) {
                            files.add(value);
                        }
                    }
                }
            }
            queuedFiles.addAll(files);
            return files;
        } finally {
            lock.unlock();
        }
    }

    void cancelQueuedFiles(Collection<File> files) {
        lock.lock();
        try {
            if (files != null) {
                queuedFiles.removeAll(files);
            }
        } finally {
            lock.unlock();
        }
    }

    void deleteStoredFiles(Collection<File> storedFiles) {
        lock.lock();
        try {
            if (storedFiles != null) {
                queuedFiles.removeAll(storedFiles);

                for (File storedFile : storedFiles) {
                    if (!storedFile.delete()) {
                        storedFile.deleteOnExit();
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

}
