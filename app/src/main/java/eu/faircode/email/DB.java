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

    Copyright 2018-2023 by Marcel Bokhorst (M66B)
*/

import static eu.faircode.email.ServiceAuthenticator.AUTH_TYPE_PASSWORD;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;
import androidx.room.Database;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

// https://developer.android.com/topic/libraries/architecture/room.html

@Database(
        version = 285,
        entities = {
                EntityIdentity.class,
                EntityAccount.class,
                EntityFolder.class,
                EntityMessage.class,
                EntityAttachment.class,
                EntityOperation.class,
                EntityContact.class,
                EntityCertificate.class,
                EntityAnswer.class,
                EntityRule.class,
                EntitySearch.class,
                EntityLog.class
        },
        views = {
                TupleAccountView.class,
                TupleIdentityView.class,
                TupleFolderView.class
        }
)

@TypeConverters({DB.Converters.class})
public abstract class DB extends RoomDatabase {
    public abstract DaoAccount account();

    public abstract DaoIdentity identity();

    public abstract DaoFolder folder();

    public abstract DaoMessage message();

    public abstract DaoAttachment attachment();

    public abstract DaoOperation operation();

    public abstract DaoContact contact();

    public abstract DaoCertificate certificate();

    public abstract DaoAnswer answer();

    public abstract DaoRule rule();

    public abstract DaoSearch search();

    public abstract DaoLog log();

    private static int sPid;
    private static Context sContext;
    private static DB sInstance;

    static final String DB_NAME = "fairemail";
    static final int DEFAULT_QUERY_THREADS = 4; // AndroidX default thread count: 4
    static final int DEFAULT_CACHE_SIZE = 20; // percentage of memory class
    private static final int DB_JOURNAL_SIZE_LIMIT = 1048576; // requery/sqlite-android default
    private static final int DB_CHECKPOINT = 1000; // requery/sqlite-android default

    private static ExecutorService executor =
            Helper.getBackgroundExecutor(0, "db");

    private static final String[] DB_TABLES = new String[]{
            "identity", "account", "folder", "message", "attachment", "operation", "contact", "certificate", "answer", "rule", "search", "log"};

    private static final List<String> DB_PRAGMAS = Collections.unmodifiableList(Arrays.asList(
            "synchronous", "journal_mode",
            "wal_checkpoint", "wal_autocheckpoint", "journal_size_limit",
            "page_count", "page_size", "max_page_count", "freelist_count",
            "cache_size", "cache_spill",
            "soft_heap_limit", "hard_heap_limit", "mmap_size",
            "foreign_keys", "auto_vacuum",
            "recursive_triggers",
            "compile_options"
    ));

    @Override
    public void init(@NonNull DatabaseConfiguration configuration) {
        File dbfile = configuration.context.getDatabasePath(DB_NAME);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(configuration.context);
        boolean sqlite_integrity_check = prefs.getBoolean("sqlite_integrity_check", true);

        // https://www.sqlite.org/pragma.html#pragma_integrity_check
        if (sqlite_integrity_check && dbfile.exists()) {
            String check = (Helper.isRedmiNote() || Helper.isOnePlus() || Helper.isOppo() || BuildConfig.DEBUG
                    ? "integrity_check" : "quick_check");
            try (SQLiteDatabase db = SQLiteDatabase.openDatabase(dbfile.getPath(), null, SQLiteDatabase.OPEN_READWRITE)) {
                Log.i("PRAGMA " + check);
                try (Cursor cursor = db.rawQuery("PRAGMA " + check + ";", null)) {
                    while (cursor.moveToNext()) {
                        String line = cursor.getString(0);
                        if ("ok".equals(line))
                            Log.i("PRAGMA " + check + "=" + line);
                        else
                            Log.e("PRAGMA " + check + "=" + line);
                    }
                }
            } catch (SQLiteDatabaseCorruptException ex) {
                Log.e(ex);
                Helper.secureDelete(dbfile);
            } catch (Throwable ex) {
                Log.e(ex);
                /*
                    java.lang.String, java.lang.String, android.os.Bundle)' on a null object reference
                        at android.provider.Settings$NameValueCache.getStringForUser(Settings.java:3002)
                        at android.provider.Settings$Global.getStringForUser(Settings.java:16253)
                        at android.provider.Settings$Global.getString(Settings.java:16241)
                        at android.database.sqlite.SQLiteCompatibilityWalFlags.initIfNeeded(SQLiteCompatibilityWalFlags.java:105)
                        at android.database.sqlite.SQLiteCompatibilityWalFlags.isLegacyCompatibilityWalEnabled(SQLiteCompatibilityWalFlags.java:57)
                        at android.database.sqlite.SQLiteDatabase.<init>(SQLiteDatabase.java:321)
                        at android.database.sqlite.SQLiteDatabase.openDatabase(SQLiteDatabase.java:788)
                        at android.database.sqlite.SQLiteDatabase.openDatabase(SQLiteDatabase.java:737)
                        at eu.faircode.email.DB.init(SourceFile:61)
                        at androidx.room.RoomDatabase$Builder.build(SourceFile:274)
                        at eu.faircode.email.DB.getInstance(SourceFile:106)
                        at eu.faircode.email.DB.setupViewInvalidation(SourceFile:1)
                        at eu.faircode.email.ApplicationEx.onCreate(SourceFile:140)
                        at android.app.Instrumentation.callApplicationOnCreate(Instrumentation.java:1229)
                        at android.app.ActivityThread.handleBindApplication(ActivityThread.java:6719)
                 */
            }
        }

        // https://www.sqlite.org/pragma.html#pragma_wal_autocheckpoint
        if (BuildConfig.DEBUG && dbfile.exists()) {
            try (SQLiteDatabase db = SQLiteDatabase.openDatabase(dbfile.getPath(), null, SQLiteDatabase.OPEN_READWRITE)) {
                Log.i("Set PRAGMA wal_autocheckpoint=" + DB_CHECKPOINT);
                try (Cursor cursor = db.rawQuery("PRAGMA wal_autocheckpoint=" + DB_CHECKPOINT + ";", null)) {
                    cursor.moveToNext(); // required
                }
            } catch (Throwable ex) {
                Log.e(ex);
            }
        }

        super.init(configuration);
    }

    static void setupViewInvalidation(Context context) {
        // This needs to be done on a foreground thread
        DB db = DB.getInstance(context);

        db.account().liveAccountView().observeForever(new Observer<List<TupleAccountView>>() {
            private List<TupleAccountView> last = null;

            @Override
            public void onChanged(List<TupleAccountView> accounts) {
                if (accounts == null)
                    accounts = new ArrayList<>();

                boolean changed = false;
                if (last == null || last.size() != accounts.size())
                    changed = true;
                else
                    for (int i = 0; i < accounts.size(); i++)
                        if (!accounts.get(i).equals(last.get(i))) {
                            changed = true;
                            last = accounts;
                        }

                if (changed) {
                    Log.i("Invalidating account view");
                    last = accounts;
                    db.getInvalidationTracker().notifyObserversByTableNames("message");
                }
            }
        });

        db.identity().liveIdentityView().observeForever(new Observer<List<TupleIdentityView>>() {
            private List<TupleIdentityView> last = null;

            @Override
            public void onChanged(List<TupleIdentityView> identities) {
                if (identities == null)
                    identities = new ArrayList<>();

                boolean changed = false;
                if (last == null || last.size() != identities.size())
                    changed = true;
                else
                    for (int i = 0; i < identities.size(); i++)
                        if (!identities.get(i).equals(last.get(i))) {
                            changed = true;
                            last = identities;
                        }

                if (changed) {
                    Log.i("Invalidating identity view");
                    last = identities;
                    db.getInvalidationTracker().notifyObserversByTableNames("message");
                }
            }
        });

        db.folder().liveFolderView().observeForever(new Observer<List<TupleFolderView>>() {
            private List<TupleFolderView> last = null;

            @Override
            public void onChanged(List<TupleFolderView> folders) {
                if (folders == null)
                    folders = new ArrayList<>();

                boolean changed = false;
                if (last == null || last.size() != folders.size())
                    changed = true;
                else
                    for (int i = 0; i < folders.size(); i++)
                        if (!folders.get(i).equals(last.get(i))) {
                            changed = true;
                            last = folders;
                        }

                if (changed) {
                    Log.i("Invalidating folder view");
                    last = folders;
                    db.getInvalidationTracker().notifyObserversByTableNames("account", "message");
                }
            }
        });
    }

    static void createEmergencyBackup(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean emergency_file = prefs.getBoolean("emergency_file", true);

        File emergency = new File(context.getFilesDir(), "emergency.json");

        if (emergency_file) {
            Log.i("Creating emergency backup");
            try {
                DB db = DB.getInstance(context);

                JSONArray jaccounts = new JSONArray();
                List<EntityAccount> accounts = db.account().getAccounts();
                for (EntityAccount account : accounts) {
                    JSONObject jaccount = account.toJSON();

                    JSONArray jfolders = new JSONArray();
                    List<EntityFolder> folders = db.folder().getFolders(account.id, false, true);
                    for (EntityFolder folder : folders)
                        jfolders.put(folder.toJSON());
                    jaccount.put("folders", jfolders);

                    JSONArray jidentities = new JSONArray();
                    List<EntityIdentity> identities = db.identity().getIdentities(account.id);
                    for (EntityIdentity identity : identities)
                        jidentities.put(identity.toJSON());
                    jaccount.put("identities", jidentities);

                    jaccounts.put(jaccount);
                }

                Helper.writeText(emergency, jaccounts.toString(2));
            } catch (Throwable ex) {
                Log.e(ex);
            }
        } else
            Helper.secureDelete(emergency);
    }

    private static void checkEmergencyBackup(Context context) {
        try {
            File dbfile = context.getDatabasePath(DB_NAME);
            if (dbfile.exists()) {
                Log.i("Emergency restore /dbfile");
                return;
            }

            File emergency = new File(context.getFilesDir(), "emergency.json");
            if (!emergency.exists()) {
                Log.i("Emergency restore /json");
                return;
            }

            DB db = DB.getInstance(context);
            if (db.account().getAccounts().size() > 0) {
                Log.e("Emergency restore /accounts");
                return;
            }

            Log.e("Emergency restore");

            String json = Helper.readText(emergency);
            JSONArray jaccounts = new JSONArray(json);
            for (int a = 0; a < jaccounts.length(); a++) {
                JSONObject jaccount = jaccounts.getJSONObject(a);
                EntityAccount account = EntityAccount.fromJSON(jaccount);
                account.created = new Date().getTime();
                account.id = db.account().insertAccount(account);

                JSONArray jfolders = jaccount.getJSONArray("folders");
                for (int f = 0; f < jfolders.length(); f++) {
                    EntityFolder folder = EntityFolder.fromJSON(jfolders.getJSONObject(f));
                    folder.account = account.id;
                    db.folder().insertFolder(folder);
                }

                JSONArray jidentities = jaccount.getJSONArray("identities");
                for (int i = 0; i < jidentities.length(); i++) {
                    EntityIdentity identity = EntityIdentity.fromJSON(jidentities.getJSONObject(i));
                    identity.account = account.id;
                    db.identity().insertIdentity(identity);
                }
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    public static synchronized DB getInstance(Context context) {
        int apid = android.os.Process.myPid();
        Context acontext = context.getApplicationContext();
        if (sInstance != null &&
                (sPid != apid || !Objects.equals(sContext, acontext)))
            try {
                Log.e("Orphan database instance pid=" + apid + "/" + sPid);
                sInstance = null;
            } catch (Throwable ex) {
                Log.e(ex);
            }
        sPid = apid;
        sContext = acontext;

        if (sInstance == null) {
            Log.i("Creating database instance pid=" + sPid);

            sInstance = DBMigration.migrate(sContext, getBuilder(sContext)).build();

            Helper.getSerialExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    checkEmergencyBackup(sContext);
                }
            });

            try {
                Log.i("Disabling view invalidation");

                Field fmViewTables = InvalidationTracker.class.getDeclaredField("mViewTables");
                fmViewTables.setAccessible(true);

                Map<String, Set<String>> mViewTables = (Map) fmViewTables.get(sInstance.getInvalidationTracker());
                mViewTables.get("account_view").clear();
                mViewTables.get("identity_view").clear();
                mViewTables.get("folder_view").clear();

                Log.i("Disabled view invalidation");
            } catch (ReflectiveOperationException ex) {
                Log.e(ex);
            }

            sInstance.getInvalidationTracker().addObserver(new InvalidationTracker.Observer(DB_TABLES) {
                @Override
                public void onInvalidated(@NonNull Set<String> tables) {
                    Log.d("ROOM invalidated=" + TextUtils.join(",", tables));
                }
            });
        }

        return sInstance;
    }

    private static RoomDatabase.Builder<DB> getBuilder(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean wal = prefs.getBoolean("wal", true);
        Log.i("DB wal=" + wal);

        RoomDatabase.Builder<DB> builder = Room
                .databaseBuilder(context, DB.class, DB_NAME)
                //.openHelperFactory(new RequerySQLiteOpenHelperFactory())
                //.setQueryExecutor()
                .setTransactionExecutor(executor)
                .setJournalMode(wal ? JournalMode.WRITE_AHEAD_LOGGING : JournalMode.TRUNCATE) // using the latest sqlite
                .addCallback(new Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        defaultSearches(db, context);
                    }

                    @Override
                    public void onOpen(@NonNull SupportSQLiteDatabase db) {
                        Map<String, String> crumb = new HashMap<>();
                        crumb.put("version", Integer.toString(db.getVersion()));
                        crumb.put("WAL", Boolean.toString(db.isWriteAheadLoggingEnabled()));
                        Log.breadcrumb("Database", crumb);

                        // https://www.sqlite.org/pragma.html#pragma_auto_vacuum
                        // https://android.googlesource.com/platform/external/sqlite.git/+/6ab557bdc070f11db30ede0696888efd19800475%5E!/
                        boolean sqlite_auto_vacuum = prefs.getBoolean("sqlite_auto_vacuum", false);
                        String mode = (sqlite_auto_vacuum ? "FULL" : "INCREMENTAL");
                        Log.i("Set PRAGMA auto_vacuum=" + mode);
                        try (Cursor cursor = db.query("PRAGMA auto_vacuum=" + mode + ";")) {
                            cursor.moveToNext(); // required
                        }

                        // https://sqlite.org/pragma.html#pragma_synchronous
                        boolean sqlite_sync_extra = prefs.getBoolean("sqlite_sync_extra", true);
                        String sync = (sqlite_sync_extra ? "EXTRA" : "NORMAL");
                        Log.i("Set PRAGMA synchronous=" + sync);
                        try (Cursor cursor = db.query("PRAGMA synchronous=" + sync + ";")) {
                            cursor.moveToNext(); // required
                        }

                        Log.i("Set PRAGMA journal_size_limit=" + DB_JOURNAL_SIZE_LIMIT);
                        try (Cursor cursor = db.query("PRAGMA journal_size_limit=" + DB_JOURNAL_SIZE_LIMIT + ";")) {
                            cursor.moveToNext(); // required
                        }

                        // https://www.sqlite.org/pragma.html#pragma_cache_size
                        Integer cache_size = getCacheSizeKb(context);
                        if (cache_size != null) {
                            cache_size = -cache_size; // kibibytes
                            Log.i("Set PRAGMA cache_size=" + cache_size);
                            try (Cursor cursor = db.query("PRAGMA cache_size=" + cache_size + ";")) {
                                cursor.moveToNext(); // required
                            }
                        }

                        // Prevent long running operations from getting an exclusive lock
                        // https://www.sqlite.org/pragma.html#pragma_cache_spill
                        Log.i("Set PRAGMA cache_spill=0");
                        try (Cursor cursor = db.query("PRAGMA cache_spill=0;")) {
                            cursor.moveToNext(); // required
                        }

                        Log.i("Set PRAGMA recursive_triggers=off");
                        try (Cursor cursor = db.query("PRAGMA recursive_triggers=off;")) {
                            cursor.moveToNext(); // required
                        }

                        // https://www.sqlite.org/pragma.html
                        for (String pragma : DB_PRAGMAS)
                            if (!"compile_options".equals(pragma) || BuildConfig.DEBUG)
                                try (Cursor cursor = db.query("PRAGMA " + pragma + ";")) {
                                    boolean has = false;
                                    while (cursor.moveToNext()) {
                                        has = true;
                                        Log.i("Get PRAGMA " + pragma + "=" + (cursor.isNull(0) ? "<null>" : cursor.getString(0)));
                                    }
                                    if (!has)
                                        Log.i("Get PRAGMA " + pragma + "=<?>");
                                }

                        if (BuildConfig.DEBUG && false)
                            dropTriggers(db);

                        createTriggers(db);
                    }
                });

        if (BuildConfig.DEBUG && false)
            builder.setQueryCallback(new QueryCallback() {
                @Override
                public void onQuery(@NonNull String sqlQuery, @NonNull List<Object> bindArgs) {
                    Log.i("query=" + sqlQuery);
                }
            }, Helper.getParallelExecutor());

        return builder;
    }

    static Integer getCacheSizeKb(Context context) {
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            int sqlite_cache = prefs.getInt("sqlite_cache", DEFAULT_CACHE_SIZE);

            ActivityManager am = Helper.getSystemService(context, ActivityManager.class);
            int class_mb = am.getMemoryClass();
            int cache_size = sqlite_cache * class_mb * 1024 / 100;

            return (cache_size > 2000 ? cache_size : null);
        } catch (Throwable ex) {
            Log.e(ex);
            return null;
        }
    }

    static void dropTriggers(@NonNull SupportSQLiteDatabase db) {
        db.execSQL("DROP TRIGGER IF EXISTS `attachment_insert`");
        db.execSQL("DROP TRIGGER IF EXISTS `attachment_delete`");

        db.execSQL("DROP TRIGGER IF EXISTS `account_update`");
        db.execSQL("DROP TRIGGER IF EXISTS `identity_update`");
    }

    static void createTriggers(@NonNull SupportSQLiteDatabase db) {
        List<String> image = new ArrayList<>();
        for (String img : ImageHelper.IMAGE_TYPES)
            image.add("'" + img + "'");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            for (String img : ImageHelper.IMAGE_TYPES8)
                image.add("'" + img + "'");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            for (String img : ImageHelper.IMAGE_TYPES12)
                image.add("'" + img + "'");
        String images = TextUtils.join(",", image);

        db.execSQL("CREATE TRIGGER IF NOT EXISTS attachment_insert" +
                " AFTER INSERT ON attachment" +
                " BEGIN" +
                "  UPDATE message SET attachments = attachments + 1" +
                "  WHERE message.id = NEW.message" +
                "  AND NEW.encryption IS NULL" +
                "  AND NOT ((NEW.disposition = 'inline' OR (NEW.related IS NOT 0 AND NEW.cid IS NOT NULL)) AND NEW.type IN (" + images + "));" +
                " END");
        db.execSQL("CREATE TRIGGER IF NOT EXISTS attachment_delete" +
                " AFTER DELETE ON attachment" +
                " BEGIN" +
                "  UPDATE message SET attachments = attachments - 1" +
                "  WHERE message.id = OLD.message" +
                "  AND OLD.encryption IS NULL" +
                "  AND NOT ((OLD.disposition = 'inline' OR (OLD.related IS NOT 0 AND OLD.cid IS NOT NULL)) AND OLD.type IN (" + images + "));" +
                " END");

        db.execSQL("CREATE TRIGGER IF NOT EXISTS account_update" +
                " AFTER UPDATE ON account" +
                " BEGIN" +
                "  UPDATE account SET last_modified = strftime('%s') * 1000" +
                "  WHERE OLD.id = NEW.id" +
                "  AND OLD.last_modified = NEW.last_modified" +
                "  AND (NEW.auth_type = " + AUTH_TYPE_PASSWORD + " OR OLD.password = NEW.password)" +
                "  AND OLD.keep_alive_ok IS NEW.keep_alive_ok" +
                "  AND OLD.keep_alive_failed IS NEW.keep_alive_failed" +
                "  AND OLD.keep_alive_succeeded IS NEW.keep_alive_succeeded" +
                "  AND OLD.quota_usage IS NEW.quota_usage" +
                "  AND OLD.thread IS NEW.thread" +
                "  AND OLD.state IS NEW.state" +
                "  AND OLD.warning IS NEW.warning" +
                "  AND OLD.error IS NEW.error" +
                "  AND OLD.last_connected IS NEW.last_connected" +
                "  AND OLD.backoff_until IS NEW.backoff_until;" +
                " END");

        db.execSQL("CREATE TRIGGER IF NOT EXISTS identity_update" +
                " AFTER UPDATE ON identity" +
                " BEGIN" +
                "  UPDATE identity SET last_modified = strftime('%s') * 1000" +
                "  WHERE OLD.id = NEW.id" +
                "  AND OLD.last_modified = NEW.last_modified" +
                "  AND OLD.state IS NEW.state" +
                "  AND OLD.error IS NEW.error" +
                "  AND OLD.last_connected IS NEW.last_connected" +
                "  AND (NEW.auth_type = " + AUTH_TYPE_PASSWORD + " OR OLD.password = NEW.password);" +
                " END");
    }

    public static void defaultSearches(SupportSQLiteDatabase db, Context context) {
        try {
            BoundaryCallbackMessages.SearchCriteria criteria;

            criteria = new BoundaryCallbackMessages.SearchCriteria();
            criteria.with_flagged = true;

            db.execSQL("INSERT INTO `search` (`name`, `order`, `data`) VALUES (?, ?, ?)",
                    new Object[]{
                            context.getString(R.string.title_search_with_flagged),
                            0,
                            criteria.toJsonData().toString()
                    });

            criteria = new BoundaryCallbackMessages.SearchCriteria();
            criteria.with_unseen = true;
            db.execSQL("INSERT INTO `search` (`name`, `order`, `data`) VALUES (?, ?, ?)",
                    new Object[]{
                            context.getString(R.string.title_search_with_unseen),
                            0,
                            criteria.toJsonData().toString()
                    });
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    public static void checkpoint(Context context) {
        // https://www.sqlite.org/pragma.html#pragma_wal_checkpoint
        DB db = getInstance(context);
        db.getQueryExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    long start = new Date().getTime();
                    StringBuilder sb = new StringBuilder();
                    SupportSQLiteDatabase sdb = db.getOpenHelper().getWritableDatabase();
                    String mode = (true ? "RESTART" : "PASSIVE");
                    try (Cursor cursor = sdb.query("PRAGMA wal_checkpoint(" + mode + ");")) {
                        if (cursor.moveToNext()) {
                            for (int i = 0; i < cursor.getColumnCount(); i++) {
                                if (i > 0)
                                    sb.append(",");
                                sb.append(cursor.getInt(i));
                            }
                        }
                    }

                    long elapse = new Date().getTime() - start;
                    EntityLog.log(context, "PRAGMA wal_checkpoint(" + mode + ")=" + sb +
                            " elapse=" + elapse + " ms");
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }
        });
    }

    public static void shrinkMemory(Context context) {
        DB db = getInstance(context);
        db.getQueryExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    SupportSQLiteDatabase sdb = db.getOpenHelper().getWritableDatabase();
                    try (Cursor cursor = sdb.query("PRAGMA shrink_memory;")) {
                        cursor.moveToNext();
                    }
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }
        });
    }

    @Override
    @SuppressWarnings("deprecation")
    public void beginTransaction() {
        super.beginTransaction();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void setTransactionSuccessful() {
        super.setTransactionSuccessful();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void endTransaction() {
        try {
            super.endTransaction();
        } catch (Throwable ex) {
            String msg = ex.getMessage();
            if (TextUtils.isEmpty(msg))
                throw ex;

            if (msg.contains("no current transaction")) {
                // java.lang.IllegalStateException: Cannot perform this operation because there is no current transaction.
                Log.w(ex);
                return;
            }

            if (msg.contains("no transaction is active")) {
                // Moto e⁶ plus - Android 9
                /*
                    android.database.sqlite.SQLiteException: cannot rollback - no transaction is active (code 1 SQLITE_ERROR)
                            at android.database.sqlite.SQLiteConnection.nativeExecute(SQLiteConnection.java:-2)
                            at android.database.sqlite.SQLiteConnection.execute(SQLiteConnection.java:569)
                            at android.database.sqlite.SQLiteSession.endTransactionUnchecked(SQLiteSession.java:439)
                            at android.database.sqlite.SQLiteSession.endTransaction(SQLiteSession.java:401)
                            at android.database.sqlite.SQLiteDatabase.endTransaction(SQLiteDatabase.java:566)
                            at androidx.sqlite.db.framework.FrameworkSQLiteDatabase.endTransaction(FrameworkSQLiteDatabase:75)
                            at androidx.room.RoomDatabase.internalEndTransaction(RoomDatabase:594)
                            at androidx.room.RoomDatabase.endTransaction(RoomDatabase:584)
                            at eu.faircode.email.DB.endTransaction(DB:2842)
                            at androidx.room.paging.LimitOffsetDataSource.loadInitial(LimitOffsetDataSource:181)
                            at androidx.paging.PositionalDataSource.dispatchLoadInitial(PositionalDataSource:286)
                            at androidx.paging.TiledPagedList.<init>(TiledPagedList:107)
                            at androidx.paging.PagedList.create(PagedList:229)
                            at androidx.paging.PagedList$Builder.build(PagedList:388)
                            at androidx.paging.LivePagedListBuilder$1.compute(LivePagedListBuilder:206)
                            at androidx.paging.LivePagedListBuilder$1.compute(LivePagedListBuilder:171)
                            at androidx.lifecycle.ComputableLiveData$2.run(ComputableLiveData:110)
                            at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1167)
                            at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:641)
                            at java.lang.Thread.run(Thread.java:764)
                 */
                Log.w(ex);
                return;
            }

            throw ex;
        }
    }

    public static class Converters {
        @TypeConverter
        public static String[] toStringArray(String value) {
            if (value == null)
                return new String[0];
            else {
                String[] result = TextUtils.split(value, " ");
                for (int i = 0; i < result.length; i++)
                    result[i] = Uri.decode(result[i]);
                return result;
            }
        }

        @TypeConverter
        public static String fromStringArray(String[] value) {
            if (value == null || value.length == 0)
                return null;
            else {
                String[] copy = new String[value.length];
                System.arraycopy(value, 0, copy, 0, value.length);
                for (int i = 0; i < copy.length; i++)
                    copy[i] = Uri.encode(copy[i]);
                return TextUtils.join(" ", copy);
            }
        }

        @TypeConverter
        public static String encodeAddresses(Address[] addresses) {
            if (addresses == null)
                return null;
            JSONArray jaddresses = new JSONArray();
            for (Address address : addresses)
                try {
                    if (address instanceof InternetAddress) {
                        String a = ((InternetAddress) address).getAddress();
                        String p = ((InternetAddress) address).getPersonal();
                        JSONObject jaddress = new JSONObject();
                        if (a != null)
                            jaddress.put("address", a);
                        if (p != null)
                            jaddress.put("personal", p);
                        jaddresses.put(jaddress);
                    } else {
                        JSONObject jaddress = new JSONObject();
                        jaddress.put("address", address.toString());
                        jaddresses.put(jaddress);
                    }
                } catch (JSONException ex) {
                    Log.e(ex);
                }
            return jaddresses.toString();
        }

        @TypeConverter
        public static Address[] decodeAddresses(String json) {
            if (json == null)
                return null;

            List<Address> result = new ArrayList<>();
            try {
                JSONArray jroot = new JSONArray(json);
                for (int i = 0; i < jroot.length(); i++) {
                    Object item = jroot.get(i);
                    if (jroot.get(i) instanceof JSONArray)
                        for (int j = 0; j < ((JSONArray) item).length(); j++)
                            result.add(InternetAddressJson.from((JSONObject) ((JSONArray) item).get(j)));
                    else
                        result.add(InternetAddressJson.from((JSONObject) item));
                }
            } catch (Throwable ex) {
                // Compose can store invalid addresses
                Log.w(ex);
            }
            return result.toArray(new Address[0]);
        }

        @TypeConverter
        public static EntityLog.Type toLogType(int ordinal) {
            return EntityLog.Type.values()[ordinal];
        }

        @TypeConverter
        public static int fromLogType(EntityLog.Type type) {
            if (type == null)
                type = EntityLog.Type.General;
            return type.ordinal();
        }
    }
}
