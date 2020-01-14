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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.mail.Address;

import io.requery.android.database.sqlite.SQLiteDatabase;
import io.requery.android.database.sqlite.SQLiteOpenHelper;

public class FtsDbHelper extends SQLiteOpenHelper {
    // https://www.sqlite.org/fts3.html
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "fts.db";

    public FtsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i("FTS create");
        db.execSQL("CREATE VIRTUAL TABLE `message` USING fts4(`folder`, `time`, `address`, `subject`, `keyword`, `text`)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Do nothing
    }

    void insert(SQLiteDatabase db, EntityMessage message, String text) {
        Log.i("FTS insert id=" + message.id + " subject=" + message.subject + " text=" + text);
        List<Address> address = new ArrayList<>();
        if (message.from != null)
            address.addAll(Arrays.asList(message.from));
        if (message.to != null)
            address.addAll(Arrays.asList(message.to));
        if (message.cc != null)
            address.addAll(Arrays.asList(message.cc));

        try {
            db.beginTransaction();

            db.delete("message", "docid = ?", new Object[]{message.id});

            ContentValues cv = new ContentValues();
            cv.put("docid", message.id);
            cv.put("folder", message.folder);
            cv.put("time", message.received);
            cv.put("address", MessageHelper.formatAddresses(address.toArray(new Address[0]), true, false));
            cv.put("subject", message.subject == null ? "" : message.subject);
            cv.put("keyword", TextUtils.join(", ", message.keywords));
            cv.put("text", text);
            db.insert("message", SQLiteDatabase.CONFLICT_FAIL, cv);

            db.setTransactionSuccessful();
        } catch (Throwable ex) {
            Log.e(ex);
        } finally {
            db.endTransaction();
        }
    }

    List<Long> match(SQLiteDatabase db, Long folder, String search) {
        Log.i("FTS folder=" + folder + " search=" + search);
        List<Long> result = new ArrayList<>();
        try (Cursor cursor = db.query(
                "message", new String[]{"docid"},
                folder == null ? "message MATCH ?" : "folder = ? AND message MATCH ?",
                folder == null ? new Object[]{search} : new Object[]{folder, search},
                null, null, "time DESC", null)) {
            while (cursor != null && cursor.moveToNext())
                result.add(cursor.getLong(0));
        }
        Log.i("FTS result=" + result.size());
        return result;
    }
}
