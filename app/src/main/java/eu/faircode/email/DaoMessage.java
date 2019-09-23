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

    Copyright 2018-2019 by Marcel Bokhorst (M66B)
*/

import android.database.Cursor;

import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RoomWarnings;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DaoMessage {

    // About 'dummy': "When the min() or max() aggregate functions are used in an aggregate query,
    // all bare columns in the result set take values from the input row which also contains the minimum or maximum."
    // https://www.sqlite.org/lang_select.html

    String is_drafts = "folder.type = '" + EntityFolder.DRAFTS + "'";
    String is_outbox = "folder.type = '" + EntityFolder.OUTBOX + "'";

    @Query("SELECT message.*" +
            ", account.pop AS accountPop, account.name AS accountName, IFNULL(identity.color, account.color) AS accountColor, account.notify AS accountNotify" +
            ", folder.name AS folderName, folder.display AS folderDisplay, folder.type AS folderType, folder.read_only AS folderReadOnly" +
            ", identity.name AS identityName, identity.email AS identityEmail, identity.synchronize AS identitySynchronize" +
            ", '[' || group_concat(message.`from`, ',') || ']' AS senders" +
            ", COUNT(message.id) AS count" +
            ", SUM(1 - message.ui_seen) AS unseen" +
            ", SUM(1 - message.ui_flagged) AS unflagged" +
            ", SUM(CASE WHEN folder.type = '" + EntityFolder.DRAFTS + "' THEN 1 ELSE 0 END) AS drafts" +
            ", COUNT(DISTINCT CASE WHEN message.msgid IS NULL THEN message.id ELSE message.msgid END) AS visible" +
            ", SUM(message.size) AS totalSize" +
            ", MAX(CASE WHEN" +
            "   ((:found AND folder.type <> '" + EntityFolder.ARCHIVE + "' AND folder.type <> '" + EntityFolder.DRAFTS + "')" +
            "   OR (NOT :found AND :type IS NULL AND folder.unified)" +
            "   OR (NOT :found AND folder.type = :type))" +
            "   THEN message.received ELSE 0 END) AS dummy" +
            " FROM (SELECT * FROM message ORDER BY received DESC) AS message" +
            " JOIN account ON account.id = message.account" +
            " LEFT JOIN identity ON identity.id = message.identity" +
            " JOIN folder ON folder.id = message.folder" +
            " WHERE account.`synchronize`" +
            " AND (message.ui_hide = 0 OR :debug)" +
            " AND (NOT :found OR ui_found = :found)" +
            " GROUP BY account.id, CASE WHEN message.thread IS NULL OR NOT :threading THEN message.id ELSE message.thread END" +
            " HAVING (:found OR" +
            "   CASE WHEN :type IS NULL THEN SUM(folder.unified) > 0" +
            "   ELSE SUM(CASE WHEN folder.type = :type THEN 1 ELSE 0 END) > 0 END)" +
            " AND (NOT :filter_seen OR SUM(1 - message.ui_seen) > 0)" +
            " AND (NOT :filter_unflagged OR COUNT(message.id) - SUM(1 - message.ui_flagged) > 0)" +
            " AND (NOT :filter_snoozed OR message.ui_snoozed IS NULL OR " + is_drafts + ")" +
            " ORDER BY CASE" +
            "  WHEN 'unread' = :sort THEN SUM(1 - message.ui_seen) = 0" +
            "  WHEN 'starred' = :sort THEN COUNT(message.id) - SUM(1 - message.ui_flagged) = 0" +
            "  WHEN 'sender' = :sort THEN LOWER(message.sender)" +
            "  WHEN 'subject' = :sort THEN LOWER(message.subject)" +
            "  WHEN 'size' = :sort THEN -SUM(message.size)" +
            "  WHEN 'snoozed' = :sort THEN SUM(CASE WHEN message.ui_snoozed IS NULL THEN 0 ELSE 1 END) = 0" +
            "  ELSE 0" +
            " END, CASE WHEN :ascending THEN message.received ELSE -message.received END")
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    DataSource.Factory<Integer, TupleMessageEx> pagedUnified(
            String type,
            boolean threading,
            String sort, boolean ascending,
            boolean filter_seen, boolean filter_unflagged, boolean filter_snoozed,
            boolean found,
            boolean debug);

    @Query("SELECT message.*" +
            ", account.pop AS accountPop, account.name AS accountName, IFNULL(identity.color, account.color) AS accountColor, account.notify AS accountNotify" +
            ", folder.name AS folderName, folder.display AS folderDisplay, folder.type AS folderType, folder.read_only AS folderReadOnly" +
            ", identity.name AS identityName, identity.email AS identityEmail, identity.synchronize AS identitySynchronize" +
            ", '[' || group_concat(message.`from`, ',') || ']' AS senders" +
            ", COUNT(message.id) AS count" +
            ", SUM(1 - message.ui_seen) AS unseen" +
            ", SUM(1 - message.ui_flagged) AS unflagged" +
            ", SUM(CASE WHEN folder.type = '" + EntityFolder.DRAFTS + "' THEN 1 ELSE 0 END) AS drafts" +
            ", COUNT(DISTINCT CASE WHEN message.msgid IS NULL THEN message.id ELSE message.msgid END) AS visible" +
            ", SUM(message.size) AS totalSize" +
            ", MAX(CASE WHEN folder.id = :folder THEN message.received ELSE 0 END) AS dummy" +
            " FROM (SELECT * FROM message ORDER BY received DESC) AS message" +
            " JOIN account ON account.id = message.account" +
            " LEFT JOIN identity ON identity.id = message.identity" +
            " JOIN folder ON folder.id = message.folder" +
            " JOIN folder AS f ON f.id = :folder" +
            " WHERE (message.account = f.account OR " + is_outbox + ")" +
            " AND (message.ui_hide = 0 OR :debug)" +
            " AND (NOT :found OR ui_found = :found)" +
            " GROUP BY CASE WHEN message.thread IS NULL OR NOT :threading THEN message.id ELSE message.thread END" +
            " HAVING SUM(CASE WHEN folder.id = :folder THEN 1 ELSE 0 END) > 0" +
            " AND (NOT :filter_seen OR SUM(1 - message.ui_seen) > 0 OR " + is_outbox + ")" +
            " AND (NOT :filter_unflagged OR COUNT(message.id) - SUM(1 - message.ui_flagged) > 0 OR " + is_outbox + ")" +
            " AND (NOT :filter_snoozed OR message.ui_snoozed IS NULL OR " + is_outbox + " OR " + is_drafts + ")" +
            " ORDER BY CASE" +
            "  WHEN 'unread' = :sort THEN SUM(1 - message.ui_seen) = 0" +
            "  WHEN 'starred' = :sort THEN COUNT(message.id) - SUM(1 - message.ui_flagged) = 0" +
            "  WHEN 'sender' = :sort THEN LOWER(message.sender)" +
            "  WHEN 'subject' = :sort THEN LOWER(message.subject)" +
            "  WHEN 'size' = :sort THEN -SUM(message.size)" +
            "  WHEN 'snoozed' = :sort THEN SUM(CASE WHEN message.ui_snoozed IS NULL THEN 0 ELSE 1 END) = 0" +
            "  ELSE 0" +
            " END, CASE WHEN :ascending THEN message.received ELSE -message.received END")
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    DataSource.Factory<Integer, TupleMessageEx> pagedFolder(
            long folder, boolean threading,
            String sort, boolean ascending,
            boolean filter_seen, boolean filter_unflagged, boolean filter_snoozed,
            boolean found,
            boolean debug);

    @Query("SELECT message.*" +
            ", account.pop AS accountPop, account.name AS accountName, IFNULL(identity.color, account.color) AS accountColor, account.notify AS accountNotify" +
            ", folder.name AS folderName, folder.display AS folderDisplay, folder.type AS folderType, folder.read_only AS folderReadOnly" +
            ", identity.name AS identityName, identity.email AS identityEmail, identity.synchronize AS identitySynchronize" +
            ", message.`from` AS senders" +
            ", 1 AS count" +
            ", CASE WHEN message.ui_seen THEN 0 ELSE 1 END AS unseen" +
            ", CASE WHEN message.ui_flagged THEN 0 ELSE 1 END AS unflagged" +
            ", CASE WHEN folder.type = '" + EntityFolder.DRAFTS + "' THEN 1 ELSE 0 END AS drafts" +
            ", 1 AS visible" +
            ", message.size AS totalSize" +
            " FROM message" +
            " JOIN account ON account.id = message.account" +
            " LEFT JOIN identity ON identity.id = message.identity" +
            " JOIN folder ON folder.id = message.folder" +
            " WHERE message.account = :account" +
            " AND message.thread = :thread" +
            " AND (:id IS NULL OR message.id = :id)" +
            " AND (message.ui_hide = 0 OR :debug)" +
            " ORDER BY CASE WHEN :ascending THEN message.received ELSE -message.received END" +
            ", CASE WHEN folder.type = '" + EntityFolder.ARCHIVE + "' THEN 1 ELSE 0 END")
    DataSource.Factory<Integer, TupleMessageEx> pagedThread(long account, String thread, Long id, boolean ascending, boolean debug);

    @Query("SELECT account.name AS accountName" +
            ", COUNT(message.id) AS count" +
            ", SUM(message.ui_seen) AS seen" +
            " FROM message" +
            " JOIN account ON account.id = message.account" +
            " WHERE message.account = :account" +
            " AND message.thread = :thread" +
            " AND (:id IS NULL OR message.id = :id)" +
            " AND message.ui_hide = 0" +
            " GROUP BY account.id")
    LiveData<TupleThreadStats> liveThreadStats(long account, String thread, Long id);

    @Query("SELECT message.id FROM folder" +
            " JOIN message ON message.folder = folder.id" +
            " WHERE ((:folder IS NULL AND :type IS NULL AND folder.unified)" +
            " OR folder.type = :type OR folder.id = :folder)" +
            " AND ui_hide <> 0")
    LiveData<List<Long>> liveHiddenFolder(Long folder, String type);

    @Query("SELECT id FROM message" +
            " WHERE account = :account" +
            " AND thread = :thread" +
            " AND ui_hide <> 0")
    LiveData<List<Long>> liveHiddenThread(long account, String thread);

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
            " AND ui_hide = 0" +
            " ORDER BY message.received DESC")
    List<Long> getMessageByFolder(long folder);

    @Query("SELECT id" +
            " FROM message" +
            " WHERE (:folder IS NULL OR folder = :folder)" +
            " AND ui_hide = 0" +
            " ORDER BY message.received DESC")
    List<Long> getMessageIdsByFolder(Long folder);

    @Query("SELECT id" +
            " FROM message" +
            " WHERE content" +
            " ORDER BY message.received DESC")
    List<Long> getMessageWithContent();

    @Query("SELECT *" +
            " FROM message" +
            " WHERE account = :account" +
            " AND thread = :thread" +
            " AND (:id IS NULL OR message.id = :id)" +
            " AND (:folder IS NULL OR message.folder = :folder)" +
            " AND NOT uid IS NULL" +
            " AND ui_hide = 0")
    List<EntityMessage> getMessagesByThread(long account, String thread, Long id, Long folder);

    @Query("SELECT * FROM message" +
            " WHERE account = :account" +
            " AND msgid = :msgid")
    List<EntityMessage> getMessageByMsgId(long account, String msgid);

    @Query("SELECT COUNT(*) FROM message" +
            " WHERE folder = :folder" +
            " AND msgid = :msgid")
    int countMessageByMsgId(long folder, String msgid);

    @Query("SELECT message.*" +
            ", account.pop AS accountPop, account.name AS accountName, identity.color AS accountColor, account.notify AS accountNotify" +
            ", folder.name AS folderName, folder.display AS folderDisplay, folder.type AS folderType, folder.read_only AS folderReadOnly" +
            ", identity.name AS identityName, identity.email AS identityEmail, identity.synchronize AS identitySynchronize" +
            ", message.`from` AS senders" +
            ", 1 AS count" +
            ", CASE WHEN message.ui_seen THEN 0 ELSE 1 END AS unseen" +
            ", CASE WHEN message.ui_flagged THEN 0 ELSE 1 END AS unflagged" +
            ", CASE WHEN folder.type = '" + EntityFolder.DRAFTS + "' THEN 1 ELSE 0 END AS drafts" +
            ", 1 AS visible" +
            ", message.size AS totalSize" +
            " FROM message" +
            " JOIN account ON account.id = message.account" +
            " LEFT JOIN identity ON identity.id = message.identity" +
            " JOIN folder ON folder.id = message.folder" +
            " WHERE message.id = :id")
    LiveData<TupleMessageEx> liveMessage(long id);

    String widget = "SELECT COUNT(message.id) AS unseen, SUM(ABS(notifying)) AS notifying" +
            " FROM message" +
            " JOIN account ON account.id = message.account" +
            " JOIN folder ON folder.id = message.folder" +
            " WHERE account.`synchronize`" +
            " AND folder.notify" +
            " AND NOT (message.ui_seen OR message.ui_hide <> 0)";

    @Query(widget)
    LiveData<TupleMessageStats> liveUnseenWidget();

    @Query(widget)
    TupleMessageStats getUnseenWidget();

    @Query("SELECT message.*" +
            ", account.pop AS accountPop, account.name AS accountName, IFNULL(identity.color, account.color) AS accountColor, account.notify AS accountNotify" +
            ", folder.name AS folderName, folder.display AS folderDisplay, folder.type AS folderType, folder.read_only AS folderReadOnly" +
            ", identity.name AS identityName, identity.email AS identityEmail, identity.synchronize AS identitySynchronize" +
            ", message.`from` AS senders" +
            ", 1 AS count" +
            ", 1 AS unseen" +
            ", 0 AS unflagged" +
            ", 0 AS drafts" +
            ", 1 AS visible" +
            ", message.size AS totalSize" +
            " FROM message" +
            " JOIN account ON account.id = message.account" +
            " LEFT JOIN identity ON identity.id = message.identity" +
            " JOIN folder ON folder.id = message.folder" +
            " WHERE account.`synchronize`" +
            " AND folder.notify" +
            " AND (account.created IS NULL OR message.received > account.created)" +
            " AND (notifying <> 0 OR NOT (message.ui_seen OR message.ui_hide <> 0))" +
            " ORDER BY message.received")
    LiveData<List<TupleMessageEx>> liveUnseenNotify();

    String widget_unified = "SELECT message.*, account.name AS accountName" +
            ", SUM(1 - message.ui_seen) AS unseen" +
            ", COUNT(message.id) - SUM(message.ui_flagged) AS unflagged" +
            ", MAX(message.received) AS dummy" +
            " FROM message" +
            " JOIN account ON account.id = message.account" +
            " JOIN folder ON folder.id = message.folder" +
            " WHERE account.`synchronize`" +
            " AND folder.unified" +
            " AND message.ui_hide = 0" +
            " AND message.ui_snoozed IS NULL" +
            " AND (NOT :unseen OR NOT message.ui_seen)" +
            " AND (NOT :flagged OR message.ui_flagged)" +
            " GROUP BY account.id" +
            ", CASE WHEN message.thread IS NULL OR NOT :threading THEN message.id ELSE message.thread END" +
            " ORDER BY message.received DESC" +
            " LIMIT 100";

    @Query(widget_unified)
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    LiveData<List<TupleMessageWidget>> liveWidgetUnified(boolean threading, boolean unseen, boolean flagged);

    @Query(widget_unified)
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    List<TupleMessageWidget> getWidgetUnified(boolean threading, boolean unseen, boolean flagged);

    @Query("SELECT uid FROM message" +
            " WHERE folder = :folder" +
            " AND (:received IS NULL OR received >= :received)" +
            " AND NOT uid IS NULL")
    List<Long> getUids(long folder, Long received);

    @Query("SELECT msgid FROM message" +
            " WHERE folder = :folder")
    List<String> getMsgIds(long folder);

    @Query("SELECT * FROM message" +
            " WHERE folder = :folder" +
            " AND uid IS NULL" +
            " AND NOT EXISTS" +
            "  (SELECT * FROM operation" +
            "  WHERE operation.message = message.id" +
            "  AND operation.name = '" + EntityOperation.ADD + "')")
    List<EntityMessage> getOrphans(long folder);

    @Query("SELECT * FROM message WHERE NOT ui_snoozed IS NULL")
    List<EntityMessage> getSnoozed();

    @Query("SELECT id AS _id, subject AS suggestion FROM message" +
            " WHERE subject LIKE :query" +
            " GROUP BY subject" +
            " UNION" +
            " SELECT id AS _id, sender AS suggestion FROM message" +
            " WHERE sender LIKE :query" +
            " GROUP BY sender" +
            " ORDER BY sender, subject")
    Cursor getSuggestions(String query);

    @Query("SELECT MIN(received)" +
            " FROM message" +
            " WHERE folder = :folder")
    Long getMessageOldest(long folder);

    @Insert
    long insertMessage(EntityMessage message);

    @Update
    int updateMessage(EntityMessage message);

    @Query("UPDATE message SET uid = :uid WHERE id = :id")
    int setMessageUid(long id, Long uid);

    @Query("UPDATE message SET msgid = :msgid WHERE id = :id")
    int setMessageMsgId(long id, String msgid);

    @Query("UPDATE message SET notifying = :notifying WHERE id = :id")
    int setMessageNotifying(long id, int notifying);

    @Query("UPDATE message SET received = :received WHERE id = :id")
    int setMessageReceived(long id, long received);

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
    int setMessageUiHide(long id, long ui_hide);

    @Query("UPDATE message SET ui_ignored = :ui_ignored WHERE id = :id")
    int setMessageUiIgnored(long id, boolean ui_ignored);

    @Query("UPDATE message SET color = :color WHERE id = :id")
    int setMessageColor(long id, Integer color);

    @Query("UPDATE message SET received = :sent, sent = :sent WHERE id = :id")
    int setMessageSent(long id, Long sent);

    @Query("UPDATE message SET error = :error WHERE id = :id")
    int setMessageError(long id, String error);

    @Query("UPDATE message SET signature = :signature WHERE id = :id")
    int setMessageSignature(long id, boolean signature);

    @Query("UPDATE message SET revision = :revision WHERE id = :id")
    int setMessageRevision(long id, Integer revision);

    @Query("UPDATE message SET revisions = :revisions WHERE id = :id")
    int setMessageRevisions(long id, Integer revisions);

    @Query("UPDATE message SET content = :content WHERE id = :id")
    int setMessageContent(long id, boolean content);

    @Query("UPDATE message SET content = :content, plain_only = :plain_only, preview = :preview, warning = :warning WHERE id = :id")
    int setMessageContent(long id, boolean content, Boolean plain_only, String preview, String warning);

    @Query("UPDATE message SET size = :size WHERE id = :id")
    int setMessageSize(long id, Long size);

    @Query("UPDATE message SET headers = :headers WHERE id = :id")
    int setMessageHeaders(long id, String headers);

    @Query("UPDATE message SET raw = :raw WHERE id = :id")
    int setMessageRaw(long id, Boolean raw);

    @Query("UPDATE message SET stored = :stored WHERE id = :id")
    int setMessageStored(long id, long stored);

    @Query("UPDATE message SET plain_only = :plain_only WHERE id = :id")
    int setMessagePlainOnly(long id, boolean plain_only);

    @Query("UPDATE message SET encrypt = :encrypt WHERE id = :id")
    int setMessageEncrypt(long id, boolean encrypt);

    @Query("UPDATE message SET last_attempt = :last_attempt WHERE id = :id")
    int setMessageLastAttempt(long id, long last_attempt);

    @Query("UPDATE message SET ui_ignored = 1" +
            " WHERE (:account IS NULL OR account = :account)" +
            " AND NOT ui_ignored" +
            " AND folder IN (SELECT id FROM folder WHERE folder.unified)")
    int ignoreAll(Long account);

    @Query("UPDATE message SET ui_found = 1" +
            " WHERE account = :account" +
            " AND thread = :thread")
    int setMessageFound(long account, String thread);

    @Query("UPDATE message SET ui_found = 0")
    int resetSearch();

    @Query("UPDATE message SET ui_snoozed = :wakeup" +
            " WHERE id = :id")
    int setMessageSnoozed(long id, Long wakeup);

    @Query("UPDATE message SET notifying = 0")
    int clearNotifyingMessages();

    @Query("DELETE FROM message WHERE id = :id")
    int deleteMessage(long id);

    @Query("DELETE FROM message" +
            " WHERE folder = :folder" +
            " AND uid = :uid")
    int deleteMessage(long folder, long uid);

    @Query("DELETE FROM message" +
            " WHERE folder = :folder" +
            " AND msgid = :msgid")
    int deleteMessage(long folder, String msgid);

    @Query("DELETE FROM message" +
            " WHERE folder = :folder" +
            " AND NOT uid IS NULL")
    int deleteLocalMessages(long folder);

    @Query("DELETE FROM message" +
            " WHERE folder = :folder" +
            " AND ui_browsed" +
            " AND NOT uid IS NULL")
    int deleteBrowsedMessages(long folder);

    @Query("DELETE FROM message" +
            " WHERE folder = :folder" +
            " AND uid IS NULL" +
            " AND NOT EXISTS" +
            "  (SELECT * FROM operation" +
            "  WHERE operation.message = message.id" +
            "  AND operation.name = '" + EntityOperation.ADD + "')")
    int deleteOrphans(long folder);

    @Query("SELECT id FROM message" +
            " WHERE folder = :folder" +
            " AND received < :received" +
            " AND NOT uid IS NULL" +
            " AND (ui_seen OR :unseen)" +
            " AND NOT ui_flagged" +
            " AND NOT ui_browsed")
    List<Long> getMessagesBefore(long folder, long received, boolean unseen);

    @Query("DELETE FROM message" +
            " WHERE folder = :folder" +
            " AND received < :received" +
            " AND NOT uid IS NULL" +
            " AND (ui_seen OR :unseen)" +
            " AND NOT ui_flagged" +
            " AND (NOT ui_browsed OR stored < :received)")
    int deleteMessagesBefore(long folder, long received, boolean unseen);
}