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

    Copyright 2018 by Marcel Bokhorst (M66B)
*/

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RoomWarnings;
import androidx.room.Update;

@Dao
public interface DaoMessage {

    // About 'dummy': "When the min() or max() aggregate functions are used in an aggregate query,
    // all bare columns in the result set take values from the input row which also contains the minimum or maximum."
    // https://www.sqlite.org/lang_select.html

    String unseen_unified = "SUM(CASE WHEN message.ui_seen" +
            "    OR folder.type = '" + EntityFolder.ARCHIVE + "'" +
            "    OR folder.type = '" + EntityFolder.OUTBOX + "'" +
            "    OR folder.type = '" + EntityFolder.DRAFTS + "' THEN 0 ELSE 1 END)";

    String unflagged_unified = "SUM(CASE WHEN message.ui_flagged" +
            "    AND NOT folder.type = '" + EntityFolder.ARCHIVE + "'" +
            "    AND NOT folder.type = '" + EntityFolder.OUTBOX + "'" +
            "    AND NOT folder.type = '" + EntityFolder.DRAFTS + "' THEN 0 ELSE 1 END)";

    @Query("SELECT message.*" +
            ", account.name AS accountName, IFNULL(identity.color, account.color) AS accountColor, account.notify AS accountNotify" +
            ", folder.name AS folderName, folder.display AS folderDisplay, folder.type AS folderType" +
            ", COUNT(DISTINCT message.msgid) AS count" +
            ", " + unseen_unified + " AS unseen" +
            ", " + unflagged_unified + " AS unflagged" +
            ", (SELECT COUNT(a.id) FROM attachment a WHERE a.message = message.id) AS attachments" +
            ", 0 AS duplicate" +
            ", MAX(CASE WHEN folder.unified THEN message.received ELSE 0 END) AS dummy" +
            " FROM message" +
            " JOIN account ON account.id = message.account" +
            " LEFT JOIN identity ON identity.id = message.identity" +
            " JOIN folder ON folder.id = message.folder" +
            " WHERE account.`synchronize`" +
            " AND (NOT message.ui_hide OR :debug)" +
            " GROUP BY account.id, CASE WHEN message.thread IS NULL OR NOT :threading THEN message.id ELSE message.thread END" +
            " HAVING SUM(unified) > 0" +
            " ORDER BY" +
            " CASE WHEN 'sender' = :sort THEN message.sender ELSE '' END," +
            " CASE" +
            "  WHEN 'unread' = :sort THEN " + unseen_unified + " > 0" +
            "  WHEN 'starred' = :sort THEN COUNT(message.id) - " + unflagged_unified + " > 0" +
            "  ELSE 0" +
            " END DESC, message.received DESC")
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    DataSource.Factory<Integer, TupleMessageEx> pagedUnifiedInbox(boolean threading, String sort, boolean debug);

    String unseen_folder = "SUM(CASE WHEN message.ui_seen" +
            "    OR (folder.id <> :folder AND folder.type = '" + EntityFolder.ARCHIVE + "')" +
            "    OR (folder.id <> :folder AND folder.type = '" + EntityFolder.OUTBOX + "')" +
            "    OR (folder.id <> :folder AND folder.type = '" + EntityFolder.DRAFTS + "') THEN 0 ELSE 1 END)";
    String unflagged_folder = "SUM(CASE WHEN message.ui_flagged" +
            "    AND NOT (folder.id <> :folder AND folder.type = '" + EntityFolder.ARCHIVE + "')" +
            "    AND NOT (folder.id <> :folder AND folder.type = '" + EntityFolder.OUTBOX + "')" +
            "    AND NOT (folder.id <> :folder AND folder.type = '" + EntityFolder.DRAFTS + "') THEN 0 ELSE 1 END)";

    @Query("SELECT message.*" +
            ", account.name AS accountName, IFNULL(identity.color, account.color) AS accountColor, account.notify AS accountNotify" +
            ", folder.name AS folderName, folder.display AS folderDisplay, folder.type AS folderType" +
            ", COUNT(DISTINCT message.msgid) AS count" +
            ", " + unseen_folder + " AS unseen" +
            ", " + unflagged_folder + " AS unflagged" +
            ", (SELECT COUNT(a.id) FROM attachment a WHERE a.message = message.id) AS attachments" +
            ", 0 AS duplicate" +
            ", MAX(CASE WHEN folder.id = :folder THEN message.received ELSE 0 END) AS dummy" +
            " FROM message" +
            " JOIN account ON account.id = message.account" +
            " LEFT JOIN identity ON identity.id = message.identity" +
            " JOIN folder ON folder.id = message.folder" +
            " JOIN folder f ON f.id = :folder" +
            " WHERE (message.account = f.account OR folder.type = '" + EntityFolder.OUTBOX + "')" +
            " AND (NOT message.ui_hide OR :debug)" +
            " AND (NOT :found OR ui_found = :found)" +
            " GROUP BY CASE WHEN message.thread IS NULL OR NOT :threading THEN message.id ELSE message.thread END" +
            " HAVING SUM(CASE WHEN folder.id = :folder THEN 1 ELSE 0 END) > 0" +
            " ORDER BY" +
            " CASE WHEN 'sender' = :sort THEN message.sender ELSE '' END," +
            " CASE" +
            "  WHEN 'unread' = :sort THEN " + unseen_folder + " > 0" +
            "  WHEN 'starred' = :sort THEN COUNT(message.id) - " + unflagged_folder + " > 0" +
            "  ELSE 0" +
            " END DESC, message.received DESC")
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    DataSource.Factory<Integer, TupleMessageEx> pagedFolder(long folder, boolean threading, String sort, boolean found, boolean debug);

    @Query("SELECT message.*" +
            ", account.name AS accountName, IFNULL(identity.color, account.color) AS accountColor, account.notify AS accountNotify" +
            ", folder.name AS folderName, folder.display AS folderDisplay, folder.type AS folderType" +
            ", (SELECT COUNT(m1.id) FROM message m1 WHERE m1.account = message.account AND m1.thread = message.thread AND NOT m1.ui_hide) AS count" +
            ", CASE WHEN message.ui_seen THEN 0 ELSE 1 END AS unseen" +
            ", CASE WHEN message.ui_flagged THEN 0 ELSE 1 END AS unflagged" +
            ", (SELECT COUNT(a.id) FROM attachment a WHERE a.message = message.id) AS attachments" +

            ", ((folder.type = '" + EntityFolder.ARCHIVE + "' OR folder.type = '" + EntityFolder.SENT + "')" +
            " AND EXISTS (" +
            "    SELECT * FROM message m1" +
            "    JOIN folder f1 ON f1.id = m1.folder" +
            "    WHERE m1.id <> message.id" +
            "    AND m1.msgid = message.msgid" +
            "    AND f1.type <> folder.type" +
            "    AND f1.type <> '" + EntityFolder.ARCHIVE + "'" +
            "  )) AS duplicate" +

            " FROM message" +
            " JOIN account ON account.id = message.account" +
            " LEFT JOIN identity ON identity.id = message.identity" +
            " JOIN folder ON folder.id = message.folder" +
            " WHERE message.account = :account" +
            " AND message.thread = :thread" +
            " AND (:id IS NULL OR message.id = :id)" +
            " AND (NOT message.ui_hide OR :debug)" +
            " ORDER BY message.received DESC" +
            ", CASE WHEN folder.type = '" + EntityFolder.ARCHIVE + "' THEN 1 ELSE 0 END")
    DataSource.Factory<Integer, TupleMessageEx> pagedThread(long account, String thread, Long id, boolean debug);

    @Query("SELECT COUNT(id)" +
            " FROM message" +
            " WHERE id = :id")
    int countMessage(long id);

    @Query("SELECT *" +
            " FROM message" +
            " WHERE id = :id")
    EntityMessage getMessage(long id);

    @Query("SELECT *" +
            " FROM message" +
            " WHERE folder = :folder" +
            " AND uid = :uid")
    EntityMessage getMessageByUid(long folder, long uid);

    @Query("SELECT id" +
            " FROM message" +
            " WHERE folder = :folder" +
            " AND NOT ui_hide" +
            " ORDER BY message.received DESC")
    List<Long> getMessageByFolder(long folder);

    @Query("SELECT *" +
            " FROM message" +
            " WHERE account = :account" +
            " AND thread = :thread" +
            " AND (:id IS NULL OR message.id = :id)" +
            " AND (:folder IS NULL OR message.folder = :folder)" +
            " AND NOT uid IS NULL" +
            " AND NOT ui_hide")
    List<EntityMessage> getMessageByThread(long account, String thread, Long id, Long folder);

    @Query("SELECT message.* FROM message" +
            " JOIN folder ON folder.id = message.folder" +
            " WHERE message.account = :account" +
            " AND message.msgid = :msgid")
    List<EntityMessage> getMessageByMsgId(long account, String msgid);

    @Query("SELECT * FROM message" +
            " WHERE folder = :folder" +
            " AND ui_seen" +
            " AND NOT ui_hide")
    List<EntityMessage> getMessageSeen(long folder);

    @Query("SELECT id FROM message" +
            " WHERE content" +
            " AND (preview IS NULL OR preview = '')")
    List<Long> getMessageWithoutPreview();

    @Query("SELECT message.*" +
            ", account.name AS accountName, identity.color AS accountColor, account.notify AS accountNotify" +
            ", folder.name AS folderName, folder.display AS folderDisplay, folder.type AS folderType" +
            ", (SELECT COUNT(m1.id) FROM message m1 WHERE m1.account = message.account AND m1.thread = message.thread AND NOT m1.ui_hide) AS count" +
            ", CASE WHEN message.ui_seen THEN 0 ELSE 1 END AS unseen" +
            ", CASE WHEN message.ui_flagged THEN 0 ELSE 1 END AS unflagged" +
            ", (SELECT COUNT(a.id) FROM attachment a WHERE a.message = message.id) AS attachments" +
            ", 0 AS duplicate" +
            " FROM message" +
            " JOIN account ON account.id = message.account" +
            " LEFT JOIN identity ON identity.id = message.identity" +
            " JOIN folder ON folder.id = message.folder" +
            " WHERE message.id = :id")
    LiveData<TupleMessageEx> liveMessage(long id);

    @Query("SELECT message.*" +
            ", account.name AS accountName, IFNULL(identity.color, account.color) AS accountColor, account.notify AS accountNotify" +
            ", folder.name AS folderName, folder.display AS folderDisplay, folder.type AS folderType" +
            ", 1 AS count" +
            ", 1 AS unseen" +
            ", 0 AS unflagged" +
            ", 0 AS attachments" +
            ", 0 AS duplicate" +
            " FROM message" +
            " JOIN account ON account.id = message.account" +
            " LEFT JOIN identity ON identity.id = message.identity" +
            " JOIN folder ON folder.id = message.folder" +
            " WHERE account.`synchronize`" +
            " AND folder.notify" +
            " AND (account.created IS NULL OR message.received > account.created)" +
            " AND NOT message.ui_seen" +
            " AND NOT message.ui_hide" +
            " AND NOT message.ui_ignored" +
            " ORDER BY message.received")
    LiveData<List<TupleMessageEx>> liveUnseenNotify();

    @Query("SELECT COUNT(message.id) FROM message" +
            " JOIN account ON account.id = message.account" +
            " JOIN folder ON folder.id = message.folder" +
            " WHERE account.`synchronize`" +
            " AND folder.unified" +
            " AND NOT message.ui_seen" +
            " AND NOT message.ui_hide" +
            " AND NOT message.ui_ignored" +
            " ORDER BY message.received")
    int getUnseenUnified();

    @Query("SELECT uid FROM message" +
            " WHERE folder = :folder" +
            " AND received >= :received" +
            " AND NOT uid IS NULL")
    List<Long> getUids(long folder, long received);

    @Insert
    long insertMessage(EntityMessage message);

    @Update
    int updateMessage(EntityMessage message);

    @Query("UPDATE message SET seen = :seen WHERE id = :id")
    int setMessageSeen(long id, boolean seen);

    @Query("UPDATE message SET flagged = :flagged WHERE id = :id")
    int setMessageFlagged(long id, boolean flagged);

    @Query("UPDATE message SET answered = :answered WHERE id = :id")
    int setMessageAnswered(long id, boolean answered);

    @Query("UPDATE message SET keywords = :keywords WHERE id = :id")
    int setMessageKeywords(long id, String keywords);

    @Query("UPDATE message SET ui_seen = :ui_seen WHERE id = :id")
    int setMessageUiSeen(long id, boolean ui_seen);

    @Query("UPDATE message SET ui_flagged = :ui_flagged WHERE id = :id")
    int setMessageUiFlagged(long id, boolean ui_flagged);

    @Query("UPDATE message SET ui_answered = :ui_answered WHERE id = :id")
    int setMessageUiAnswered(long id, boolean ui_answered);

    @Query("UPDATE message SET ui_hide = :ui_hide WHERE id = :id")
    int setMessageUiHide(long id, boolean ui_hide);

    @Query("UPDATE message SET ui_ignored = :ui_ignored WHERE id = :id")
    int setMessageUiIgnored(long id, boolean ui_ignored);

    @Query("UPDATE message SET error = :error WHERE id = :id")
    int setMessageError(long id, String error);

    @Query("UPDATE message SET content = :content, preview = :preview WHERE id = :id")
    int setMessageContent(long id, boolean content, String preview);

    @Query("UPDATE message SET headers = :headers WHERE id = :id")
    int setMessageHeaders(long id, String headers);

    @Query("UPDATE message SET stored = :stored WHERE id = :id")
    int setMessageStored(long id, long stored);

    @Query("UPDATE message SET last_attempt = :last_attempt WHERE id = :id")
    int setMessageLastAttempt(long id, long last_attempt);

    @Query("UPDATE message SET ui_ignored = 1" +
            " WHERE NOT ui_ignored" +
            " AND folder IN (SELECT id FROM folder WHERE type = '" + EntityFolder.INBOX + "')")
    int ignoreAll();

    @Query("UPDATE message SET ui_found = 1" +
            " WHERE account = :account" +
            " AND thread = :thread")
    int setMessageFound(long account, String thread);

    @Query("UPDATE message SET ui_found = 0")
    int resetSearch();

    @Query("DELETE FROM message WHERE id = :id")
    int deleteMessage(long id);

    @Query("DELETE FROM message" +
            " WHERE folder = :folder" +
            " AND uid = :uid")
    int deleteMessage(long folder, long uid);

    @Query("DELETE FROM message" +
            " WHERE folder = :folder" +
            " AND NOT uid IS NULL")
    int deleteLocalMessages(long folder);

    @Query("DELETE FROM message" +
            " WHERE folder = :folder" +
            " AND seen")
    int deleteSeenMessages(long folder);

    @Query("DELETE FROM message" +
            " WHERE folder = :folder" +
            " AND received < :received" +
            " AND NOT uid IS NULL" +
            " AND (NOT ui_browsed OR :browsed)" +
            " AND NOT ui_flagged")
    int deleteMessagesBefore(long folder, long received, boolean browsed);
}