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

    Copyright 2018-2020 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.File;
import java.util.concurrent.TimeUnit;

import io.requery.android.database.sqlite.SQLiteDatabase;

public class WorkerFts extends Worker {
    private static final int INDEX_DELAY = 30; // seconds

    public WorkerFts(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        Log.i("Instance " + getName());
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Log.i("FTS index");

            int indexed = 0;
            FtsDbHelper ftsDb = new FtsDbHelper(getApplicationContext());
            try (SQLiteDatabase sdb = ftsDb.getWritableDatabase()) {
                DB db = DB.getInstance(getApplicationContext());
                try (Cursor cursor = db.message().getMessageFts()) {
                    while (cursor.moveToNext()) {
                        EntityMessage message = db.message().getMessage(cursor.getLong(0));
                        if (message != null)
                            try {
                                Log.i("FTS index=" + message.id);
                                File file = message.getFile(getApplicationContext());
                                String html = Helper.readText(file);
                                ftsDb.insert(sdb, message, HtmlHelper.getText(html));
                                db.message().setMessageFts(message.id, true);
                                indexed++;
                            } catch (Throwable ex) {
                                Log.e(ex);
                            }
                    }
                }
            }

            Log.i("FTS indexed=" + indexed);
            return Result.success();
        } catch (Throwable ex) {
            Log.e(ex);
            return Result.failure();
        }
    }

    static void init(Context context, boolean immediately) {
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            boolean fts = prefs.getBoolean("fts", true);
            if (fts) {
                Log.i("Queuing " + getName());

                OneTimeWorkRequest.Builder builder = new OneTimeWorkRequest.Builder(WorkerFts.class);
                if (!immediately)
                    builder.setInitialDelay(INDEX_DELAY, TimeUnit.SECONDS);
                OneTimeWorkRequest workRequest = builder.build();

                WorkManager.getInstance(context)
                        .enqueueUniqueWork(getName(), ExistingWorkPolicy.REPLACE, workRequest);

                Log.i("Queued " + getName());
            } else if (immediately)
                cancel(context);
        } catch (IllegalStateException ex) {
            // https://issuetracker.google.com/issues/138465476
            Log.w(ex);
        }
    }

    static void cancel(Context context) {
        Log.i("Cancelling " + getName());
        WorkManager.getInstance(context).cancelUniqueWork(getName());
        Log.i("Cancelled " + getName());
    }

    private static String getName() {
        return WorkerFts.class.getSimpleName();
    }
}
