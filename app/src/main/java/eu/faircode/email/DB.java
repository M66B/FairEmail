package eu.faircode.email;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverter;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.util.Log;

/*
    This file is part of Safe email.

    Safe email is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    NetGuard is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with NetGuard.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2018 by Marcel Bokhorst (M66B)
*/

// https://developer.android.com/topic/libraries/architecture/room.html

@Database(
        entities = {
                EntityIdentity.class,
                EntityAccount.class,
                EntityFolder.class,
                EntityMessage.class,
                EntityOperation.class
        },
        version = 1,
        exportSchema = true
)

@TypeConverters({DB.Converters.class})
public abstract class DB extends RoomDatabase {
    public abstract DaoIdentity identity();

    public abstract DaoAccount account();

    public abstract DaoFolder folder();

    public abstract DaoMessage message();

    public abstract DaoOperation operation();

    private static DB sInstance;

    private static final String DB_NAME = "email.db";

    public static synchronized DB getInstance(Context context) {
        if (sInstance == null)
            sInstance = migrate(Room.databaseBuilder(context.getApplicationContext(), DB.class, DB_NAME));
        return sInstance;
    }

    private static DB migrate(RoomDatabase.Builder<DB> builder) {
        return builder
                //.addMigrations(MIGRATION_1_2)
                .build();
    }

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase db) {
            Log.i(Helper.TAG, "DB migration from version " + startVersion + " to " + endVersion);
            db.execSQL("ALTER TABLE message ADD COLUMN error TEXT");
        }
    };

    public static class Converters {
        @TypeConverter
        public static byte[] fromString(String value) {
            return null;
        }

        @TypeConverter
        public static String fromBytes(byte[] value) {
            return null;
        }
    }
}

