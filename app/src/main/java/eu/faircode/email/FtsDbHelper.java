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

// https://www.sqlite.org/fts5.html
public class FtsDbHelper extends SQLiteOpenHelper {
    private static FtsDbHelper instance = null;

    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "fts.db";

    private FtsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    static SQLiteDatabase getInstance(Context context) {
        if (instance == null)
            instance = new FtsDbHelper(context);
        return instance.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i("FTS create");
        db.execSQL("CREATE VIRTUAL TABLE `message`" +
                " USING fts5" +
                " (`account` UNINDEXED" +
                ", `folder` UNINDEXED" +
                ", `time` UNINDEXED" +
                ", `address`" +
                ", `subject`" +
                ", `keyword`" +
                ", `text`" +
                ", tokenize = \"unicode61 remove_diacritics 2\")");
        // https://www.sqlite.org/fts5.html#unicode61_tokenizer
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i("FTS upgrade from " + oldVersion + " to " + newVersion);
        db.execSQL("DROP TABLE `message`");
        onCreate(db);
    }

    static void insert(SQLiteDatabase db, EntityMessage message, String text) {
        Log.i("FTS insert id=" + message.id);
        List<Address> address = new ArrayList<>();
        if (message.from != null)
            address.addAll(Arrays.asList(message.from));
        if (message.to != null)
            address.addAll(Arrays.asList(message.to));
        if (message.cc != null)
            address.addAll(Arrays.asList(message.cc));
        if (message.bcc != null)
            address.addAll(Arrays.asList(message.bcc));

        delete(db, message.id);

        ContentValues cv = new ContentValues();
        cv.put("rowid", message.id);
        cv.put("account", message.account);
        cv.put("folder", message.folder);
        cv.put("time", message.received);
        cv.put("address", MessageHelper.formatAddresses(address.toArray(new Address[0]), true, false));
        cv.put("subject", message.subject == null ? "" : message.subject);
        cv.put("keyword", TextUtils.join(", ", message.keywords));
        cv.put("text", text);
        db.insert("message", SQLiteDatabase.CONFLICT_FAIL, cv);
    }

    static void delete(SQLiteDatabase db) {
        db.delete("message", null, null);
    }

    static void delete(SQLiteDatabase db, long id) {
        db.delete("message", "rowid = ?", new Object[]{id});
    }

    static List<Long> match(
            SQLiteDatabase db,
            Long account, Long folder,
            BoundaryCallbackMessages.SearchCriteria criteria) {
        StringBuilder sb = new StringBuilder();
        for (String or : criteria.query.split(",")) {
            if (sb.length() > 0)
                sb.append(" OR ");

            boolean first = true;
            sb.append("(");
            for (String and : or.trim().split("\\s+")) {
                if (first)
                    first = false;
                else
                    sb.append(" AND ");

                // Escape quotes
                String term = and.replaceAll("\"", "\"\"");

                // Quote search term
                sb.append("\"").append(term).append("\"");
            }
            sb.append(")");
        }

        String search = sb.toString();

        String select = "";
        if (account != null)
            select += "account = " + account + " AND ";
        if (folder != null)
            select += "folder = " + folder + " AND ";
        if (criteria.after != null)
            select += "time > " + criteria.after + " AND ";
        if (criteria.before != null)
            select += "time < " + criteria.before + " AND ";

        Log.i("FTS select=" + select + " search=" + search);
        List<Long> result = new ArrayList<>();
        try (Cursor cursor = db.query(
                "message", new String[]{"rowid"},
                select + "message MATCH ?",
                new Object[]{search},
                null, null, "time DESC", null)) {
            while (cursor != null && cursor.moveToNext())
                result.add(cursor.getLong(0));
        }
        Log.i("FTS result=" + result.size());
        return result;
    }

    static Cursor getIds(SQLiteDatabase db) {
        return db.query(
                "message", new String[]{"rowid"},
                null, null,
                null, null, "time");
    }

    static long size(Context context) {
        return context.getDatabasePath(DATABASE_NAME).length();
    }

    static void optimize(SQLiteDatabase db) {
        Log.i("FTS optimize");
        db.execSQL("INSERT INTO message (message) VALUES ('optimize')");
    }
}
