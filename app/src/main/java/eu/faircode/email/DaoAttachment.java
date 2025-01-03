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

    Copyright 2018-2025 by Marcel Bokhorst (M66B)
*/

import android.database.Cursor;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DaoAttachment {
    @Query("SELECT * FROM attachment" +
            " WHERE message = :message" +
            " ORDER BY sequence, subsequence")
    LiveData<List<EntityAttachment>> liveAttachments(long message);

    @Query("SELECT ifnull(MAX(sequence), 0)" +
            " FROM attachment" +
            " WHERE message = :message")
    int getAttachmentSequence(long message);

    @Query("SELECT * FROM attachment" +
            " WHERE message = :message" +
            " ORDER BY sequence, subsequence")
    List<EntityAttachment> getAttachments(long message);

    @Query("SELECT COUNT(*) FROM attachment" +
            " WHERE message = :message")
    int countAttachments(long message);

    @Query("SELECT * FROM attachment" +
            " WHERE id = :id")
    EntityAttachment getAttachment(long id);

    @Query("SELECT * FROM attachment" +
            " WHERE message = :message" +
            " AND sequence = :sequence")
    EntityAttachment getAttachment(long message, int sequence);

    @Query("SELECT * FROM attachment" +
            " WHERE message = :message" +
            " AND cid = :cid" +
            " LIMIT 1")
    EntityAttachment getAttachment(long message, String cid);

    @Query("SELECT id FROM attachment" +
            " WHERE available")
    Cursor getAttachmentAvailable();

    @Query("UPDATE attachment" +
            " SET message = :message" +
            " WHERE id = :id" +
            " AND NOT (message IS :message)")
    void setMessage(long id, long message);

    @Query("UPDATE attachment" +
            " SET error = NULL, progress = :progress, available = 0" +
            " WHERE id = :id" +
            " AND (error IS NOT NULL OR NOT (progress IS :progress) OR NOT (available IS 0))")
    void setProgress(long id, Integer progress);

    @Query("UPDATE attachment" +
            " SET size = :size, error = NULL, progress = NULL, available = 1" +
            " WHERE id = :id" +
            " AND (NOT (size IS :size) OR error IS NOT NULL OR progress IS NOT NULL OR NOT (available IS 1))")
    void setDownloaded(long id, Long size);

    @Query("UPDATE attachment" +
            " SET size = NULL, progress = NULL, available = :available" +
            " WHERE id = :id" +
            " AND (size IS NOT NULL OR progress IS NOT NULL OR NOT (available IS :available))")
    void setAvailable(long id, boolean available);

    @Query("UPDATE attachment" +
            " SET size = NULL, progress = NULL, available = 0" +
            " WHERE message = :message" +
            " AND (size IS NOT NULL OR progress IS NOT NULL OR NOT (available IS 0))")
    void resetAvailable(long message);

    @Query("UPDATE attachment" +
            " SET error = :error, progress = NULL, available = 0" +
            " WHERE id = :id" +
            " AND (NOT (error IS :error) OR progress IS NOT NULL OR NOT (available IS 0))")
    void setError(long id, String error);

    @Query("UPDATE attachment" +
            " SET error = :error" +
            " WHERE id = :id" +
            " AND NOT (error IS :error)")
    void setWarning(long id, String error);

    @Query("UPDATE attachment" +
            " SET name = :name, type = :type, size= :size" +
            " WHERE id = :id" +
            " AND NOT (name IS name AND type IS :type AND size IS :size)")
    void setName(long id, String name, String type, Long size);

    @Query("UPDATE attachment" +
            " SET name = :name" +
            " WHERE id = :id" +
            " AND NOT (name IS :name)")
    void setName(long id, String name);

    @Query("UPDATE attachment" +
            " SET type = :type" +
            " WHERE id = :id" +
            " AND NOT (type IS :type)")
    void setType(long id, String type);

    @Query("UPDATE attachment" +
            " SET disposition = :disposition, cid = :cid" +
            " WHERE id = :id" +
            " AND NOT (disposition IS :disposition AND cid IS :cid)")
    void setDisposition(long id, String disposition, String cid);

    @Query("UPDATE attachment" +
            " SET cid = :cid" +
            " WHERE id = :id" +
            " AND NOT (cid IS :cid)" +
            " AND NOT (related IS :related)")
    void setCid(long id, String cid, Boolean related);

    @Query("UPDATE attachment" +
            " SET encryption = :encryption" +
            " WHERE id = :id" +
            " AND NOT (encryption IS :encryption)")
    void setEncryption(long id, Integer encryption);

    @Query("UPDATE attachment" +
            " SET media_uri = :media_uri" +
            " WHERE id = :id" +
            " AND NOT (media_uri IS :media_uri)")
    void setMediaUri(long id, String media_uri);

    @Query("UPDATE attachment" +
            " SET available = 0" +
            " WHERE NOT (available IS 0)" +
            " AND EXISTS" +
            "  (SELECT * FROM attachment AS a" +
            "   JOIN message ON message.id = a.message" +
            "   JOIN folder ON folder.id = message.folder" +
            "   JOIN account ON account.id = message.account" +
            "   WHERE a.id = attachment.id" +
            "   AND a.available" +
            "   AND message.ui_seen" +
            "   AND NOT message.ui_flagged" +
            "   AND encryption IS NULL" +
            "   AND message.received < :now - (folder.sync_days + 1) * 24 * 3600 * 1000" +
            "   AND account.pop = " + EntityAccount.TYPE_IMAP + ")")
    int purge(long now);

    @Insert
    long insertAttachment(EntityAttachment attachment);

    @Query("DELETE FROM attachment" +
            " WHERE id = :id")
    int deleteAttachment(long id);

    @Query("DELETE FROM attachment" +
            " WHERE message = :message")
    int deleteAttachments(long message);

    @Query("DELETE FROM attachment" +
            " WHERE message = :message" +
            " AND (encryption IS NULL OR encryption NOT IN (:keep))")
    int deleteAttachments(long message, int[] keep);
}
