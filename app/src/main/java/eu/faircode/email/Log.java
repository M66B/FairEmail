package eu.faircode.email;

public class Log {
    static final String TAG = "fairemail";

    public static int i(String msg) {
        if (BuildConfig.BETA_RELEASE)
            return android.util.Log.i(TAG, msg);
        else
            return 0;
    }

    public static int w(String msg) {
        return android.util.Log.w(TAG, msg);
    }

    public static int e(String msg) {
        return android.util.Log.e(TAG, msg);
    }

    public static int w(Throwable ex) {
        return android.util.Log.w(TAG, ex + "\n" + android.util.Log.getStackTraceString(ex));
    }

    public static int e(Throwable ex) {
        return android.util.Log.e(TAG, ex + "\n" + android.util.Log.getStackTraceString(ex));
    }

    public static int w(String prefix, Throwable ex) {
        return android.util.Log.w(TAG, prefix + " " + ex + "\n" + android.util.Log.getStackTraceString(ex));
    }

    public static int e(String prefix, Throwable ex) {
        return android.util.Log.e(TAG, prefix + " " + ex + "\n" + android.util.Log.getStackTraceString(ex));
    }
}