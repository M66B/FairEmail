package eu.faircode.email;

import androidx.documentfile.provider.DocumentFile;

import java.io.InputStream;

public class Send {
    static final int DEFAULT_DLIMIT = 0;
    static final int DEFAULT_TLIMIT = 0;
    static final String DEFAULT_SERVER = "";

    public static String upload(InputStream is, DocumentFile dfile, int dLimit, int timeLimit, String host, IProgress intf) {
        return null;
    }

    public interface IProgress {
        void onProgress(int percentage);

        boolean isRunning();
    }
}
