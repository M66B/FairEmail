package eu.faircode.email;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Date;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.Store;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class WorkerFolderSync extends Worker {
    public WorkerFolderSync(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        long aid = getInputData().getLong("account", -1);
        Log.i("Work account=" + aid);

        DB db = DB.getInstance(getApplicationContext());
        EntityAccount account = db.account().getAccount(aid);
        if (account == null)
            return Result.success();

        Store istore = null;
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            boolean debug = (prefs.getBoolean("debug", false) || BuildConfig.BETA_RELEASE);

            String protocol = account.getProtocol();

            // Get properties
            Properties props = MessageHelper.getSessionProperties(account.auth_type, account.realm, account.insecure);
            props.put("mail." + protocol + ".separatestoreconnection", "true");

            // Create session
            Session isession = Session.getInstance(props, null);
            isession.setDebug(debug);

            // Connect account
            Log.i(account.name + " connecting");
            db.account().setAccountState(account.id, "connecting");
            istore = isession.getStore(protocol);
            Helper.connect(getApplicationContext(), istore, account);
            db.account().setAccountState(account.id, "connected");
            db.account().setAccountConnected(account.id, new Date().getTime());
            db.account().setAccountError(account.id, null);
            Log.i(account.name + " connected");

            // Synchronize folders
            Core.onSynchronizeFolders(getApplicationContext(), account, istore, new Core.State());

        } catch (Throwable ex) {
            Log.w(ex);
            Core.reportError(getApplicationContext(), account, null, ex);
            db.account().setAccountError(account.id, Helper.formatThrowable(ex));
        } finally {
            if (istore != null)
                try {
                    Log.i(account.name + " closing");
                    db.account().setAccountState(account.id, "closing");
                    istore.close();
                } catch (Throwable ex) {
                    Log.e(ex);
                } finally {
                    Log.i(account.name + " closed");
                    db.account().setAccountState(account.id, null);
                }
        }

        return Result.success();
    }

    static void queue(long aid) {
        String tag = WorkerFolderSync.class.getSimpleName() + ":" + aid;
        Log.i("Queuing " + tag);

        Data data = new Data.Builder().putLong("account", aid).build();
        OneTimeWorkRequest workRequest =
                new OneTimeWorkRequest.Builder(WorkerFolderSync.class)
                        .addTag(tag)
                        .setInputData(data)
                        .build();
        WorkManager.getInstance().enqueueUniqueWork(tag, ExistingWorkPolicy.KEEP, workRequest);

        Log.i("Queued " + tag);
    }
}
