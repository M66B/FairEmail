package eu.faircode.email;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Date;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.event.StoreEvent;
import javax.mail.event.StoreListener;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class WorkerOperations extends Worker {
    public WorkerOperations(@NonNull Context context, @NonNull WorkerParameters args) {
        super(context, args);
    }

    @NonNull
    @Override
    public Result doWork() {
        long fid = getInputData().getLong("folder", -1);
        Log.i("Work folder=" + fid);

        final DB db = DB.getInstance(getApplicationContext());

        final EntityFolder folder = db.folder().getFolder(fid);
        if (folder == null)
            return Result.success();
        final EntityAccount account = db.account().getAccount(folder.account);
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

            // Listen for store events
            istore.addStoreListener(new StoreListener() {
                @Override
                public void notification(StoreEvent e) {
                    if (e.getMessageType() == StoreEvent.ALERT) {
                        db.account().setAccountError(account.id, e.getMessage());
                        Core.reportError(
                                getApplicationContext(), account, null,
                                new Core.AlertException(e.getMessage()));
                    } else
                        Log.i(account.name + " notice: " + e.getMessage());
                }
            });

            // Connect folder
            Log.i(folder.name + " connecting");
            db.folder().setFolderState(folder.id, "connecting");
            Folder ifolder = istore.getFolder(folder.name);
            ifolder.open(Folder.READ_WRITE);
            db.folder().setFolderState(folder.id, "connected");
            db.folder().setFolderError(folder.id, null);
            Log.i(folder.name + " connected");

            // Process operations
            Core.processOperations(getApplicationContext(), account, folder, isession, istore, ifolder, new Core.State());

        } catch (Throwable ex) {
            Log.w(ex);
            Core.reportError(getApplicationContext(), account, folder, ex);
            db.account().setAccountError(account.id, Helper.formatThrowable(ex));
            db.folder().setFolderError(folder.id, Helper.formatThrowable(ex, false));
        } finally {
            if (istore != null)
                try {
                    Log.i(account.name + " closing");
                    db.account().setAccountState(account.id, "closing");
                    db.folder().setFolderState(folder.id, "closing");
                    istore.close();
                } catch (Throwable ex) {
                    Log.w(ex);
                } finally {
                    Log.i(account.name + " closed");
                    db.account().setAccountState(account.id, "closed");
                    db.folder().setFolderState(folder.id, null);
                    db.folder().setFolderSyncState(folder.id, null);
                }
        }

        return Result.success();
    }

    static void queue(long fid) {
        String tag = WorkerOperations.class.getSimpleName() + ":" + fid;
        Log.i("Queuing " + tag);

        Data data = new Data.Builder().putLong("folder", fid).build();
        OneTimeWorkRequest workRequest =
                new OneTimeWorkRequest.Builder(WorkerOperations.class)
                        .addTag(tag)
                        .setInputData(data)
                        .build();
        WorkManager.getInstance().enqueueUniqueWork(tag, ExistingWorkPolicy.KEEP, workRequest);

        Log.i("Queued " + tag);
    }
}
