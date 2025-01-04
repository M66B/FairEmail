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
import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RoomWarnings;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DaoMessage {

    // About 'dummy': "When the min() or max() aggregate functions are used in an aggregate query,
    // all bare columns in the result set take values from the input row which also contains the minimum or maximum."
    // https://www.sqlite.org/lang_select.html

    String is_drafts = "folder.type = '" + EntityFolder.DRAFTS + "'";
    String is_outbox = "folder.type = '" + EntityFolder.OUTBOX + "'";
    String is_sent = "folder.type = '" + EntityFolder.SENT + "'";

    @Transaction
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT message.*" +
            ", account.pop AS accountProtocol, account.name AS accountName, account.category AS accountCategory, COALESCE(identity.color, folder.color, account.color) AS accountColor" +
            ", account.notify AS accountNotify, account.summary AS accountSummary, account.leave_on_server AS accountLeaveOnServer, account.leave_deleted AS accountLeaveDeleted, account.auto_seen AS accountAutoSeen" +
            ", folder.name AS folderName, folder.color AS folderColor, folder.display AS folderDisplay, folder.type AS folderType, NULL AS folderInheritedType, folder.unified AS folderUnified, folder.read_only AS folderReadOnly" +
            ", IFNULL(identity.display, identity.name) AS identityName, identity.email AS identityEmail, identity.color AS identityColor, identity.synchronize AS identitySynchronize" +
            ", '[' || substr(group_concat(message.`from`, ','), 0, 2048) || ']' AS senders" +
            ", '[' || substr(group_concat(message.`to`, ','), 0, 2048) || ']' AS recipients" +
            ", COUNT(message.id) AS count" +
            ", SUM(1 - message.ui_seen) AS unseen" +
            ", SUM(1 - message.ui_flagged) AS unflagged" +
            ", SUM(folder.type = '" + EntityFolder.DRAFTS + "') AS drafts" +
            ", COUNT(DISTINCT" +
            "   CASE WHEN NOT message.hash IS NULL THEN message.hash" +
            "   WHEN NOT message.msgid IS NULL THEN message.msgid" +
            "   ELSE message.id END) AS visible" +
            ", COUNT(DISTINCT" +
            "   CASE WHEN message.ui_seen THEN NULL" +
            "   WHEN NOT message.hash IS NULL THEN message.hash" +
            "   WHEN NOT message.msgid IS NULL THEN message.msgid" +
            "   ELSE message.id END) AS visible_unseen" +
            ", SUM(message.attachments) AS totalAttachments" +
            ", SUM(message.total) AS totalSize" +
            ", message.priority AS ui_priority" +
            ", message.importance AS ui_importance" +
            ", MAX(CASE WHEN" +
            "   (:found AND folder.type <> '" + EntityFolder.ARCHIVE + "')" +
            "   OR (NOT :found AND :type IS NULL AND folder.unified)" +
            "   OR (NOT :found AND folder.type = :type)" +
            "   THEN message.received ELSE 0 END) AS dummy" +
            " FROM message" +
            " JOIN account_view AS account ON account.id = message.account" +
            " LEFT JOIN identity_view AS identity ON identity.id = message.identity" +
            " JOIN folder_view AS folder ON folder.id = message.folder" +
            " WHERE account.`synchronize`" +
            " AND (:category IS NULL OR account.category = :category)" +
            " AND (:threading OR (:type IS NULL AND (folder.unified OR :found)) OR (:type IS NOT NULL AND folder.type = :type))" +
            " AND (NOT message.ui_hide OR :debug)" +
            " AND (NOT :found OR message.ui_found = :found)" +
            " GROUP BY account.id, CASE WHEN message.thread IS NULL OR NOT :threading THEN message.id ELSE message.thread END" +
            " HAVING (SUM((:found AND message.ui_found)" +
            " OR (NOT :found AND :type IS NULL AND folder.unified)" +
            " OR (NOT :found AND :type IS NOT NULL AND folder.type = :type)) > 0)" + // thread can be the same in different accounts
            " AND SUM(NOT message.ui_hide OR :debug) > 0" +
            " AND (NOT (:filter_seen AND NOT :filter_unflagged) OR SUM(1 - message.ui_seen) > 0)" +
            " AND (NOT (:filter_unflagged AND NOT :filter_seen) OR COUNT(message.id) - SUM(1 - message.ui_flagged) > 0)" +
            " AND (NOT (:filter_seen AND :filter_unflagged) OR SUM(1 - message.ui_seen) > 0 OR COUNT(message.id) - SUM(1 - message.ui_flagged) > 0)" +
            " AND (NOT :filter_unknown OR SUM(message.avatar IS NOT NULL AND message.sender <> identity.email) > 0)" +
            " AND (NOT :filter_snoozed OR message.ui_snoozed IS NULL OR " + is_drafts + ")" +
            " AND (NOT :filter_deleted OR NOT message.ui_deleted)" +
            " AND (:filter_language IS NULL OR SUM(message.language = :filter_language) > 0)" +
            " ORDER BY CASE WHEN :found THEN 0 ELSE -IFNULL(message.importance, 1) END" +
            ", CASE WHEN :group_category THEN account.category ELSE '' END COLLATE NOCASE" +
            ", CASE" +
            "   WHEN 'unread' = :sort1 THEN SUM(1 - message.ui_seen) = 0" +
            "   WHEN 'starred' = :sort1 THEN COUNT(message.id) - SUM(1 - message.ui_flagged) = 0" +
            "   WHEN 'priority' = :sort1 THEN -IFNULL(message.priority, 1)" +
            "   WHEN 'sender' = :sort1 THEN LOWER(message.sender)" +
            "   WHEN 'subject' = :sort1 THEN LOWER(message.subject)" +
            "   WHEN 'size' = :sort1 THEN -SUM(message.total)" +
            "   WHEN 'attachments' = :sort1 THEN -SUM(message.attachments)" +
            "   WHEN 'snoozed' = :sort1 THEN SUM(CASE WHEN message.ui_snoozed IS NULL THEN 0 ELSE 1 END) = 0" +
            "   WHEN 'touched' = :sort1 THEN IFNULL(-message.last_touched, 0)" +
            "   ELSE 0" +
            "  END" +
            ", CASE" +
            "   WHEN 'unread' = :sort2 THEN SUM(1 - message.ui_seen) = 0" +
            "   WHEN 'starred' = :sort2 THEN COUNT(message.id) - SUM(1 - message.ui_flagged) = 0" +
            "   ELSE 0" +
            "  END" +
            ", CASE WHEN :ascending THEN message.received ELSE -message.received END")
    DataSource.Factory<Integer, TupleMessageEx> pagedUnified(
            String type, String category,
            boolean threading, boolean group_category,
            String sort1, String sort2, boolean ascending,
            boolean filter_seen, boolean filter_unflagged, boolean filter_unknown, boolean filter_snoozed, boolean filter_deleted, String filter_language,
            boolean found,
            boolean debug);

    @Transaction
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT message.*" +
            ", account.pop AS accountProtocol, account.name AS accountName, account.category AS accountCategory, COALESCE(identity.color, folder.color, account.color) AS accountColor" +
            ", account.notify AS accountNotify, account.summary AS accountSummary, account.leave_on_server AS accountLeaveOnServer, account.leave_deleted AS accountLeaveDeleted, account.auto_seen AS accountAutoSeen" +
            ", folder.name AS folderName, folder.color AS folderColor, folder.display AS folderDisplay, folder.type AS folderType, f.inherited_type AS folderInheritedType, folder.unified AS folderUnified, folder.read_only AS folderReadOnly" +
            ", IFNULL(identity.display, identity.name) AS identityName, identity.email AS identityEmail, identity.color AS identityColor, identity.synchronize AS identitySynchronize" +
            ", '[' || substr(group_concat(message.`from`, ','), 0, 2048) || ']' AS senders" +
            ", '[' || substr(group_concat(message.`to`, ','), 0, 2048) || ']' AS recipients" +
            ", COUNT(message.id) AS count" +
            ", SUM(1 - message.ui_seen) AS unseen" +
            ", SUM(1 - message.ui_flagged) AS unflagged" +
            ", SUM(folder.type = '" + EntityFolder.DRAFTS + "') AS drafts" +
            ", COUNT(DISTINCT" +
            "   CASE WHEN NOT message.hash IS NULL THEN message.hash" +
            "   WHEN NOT message.msgid IS NULL THEN message.msgid" +
            "   ELSE message.id END) AS visible" +
            ", COUNT(DISTINCT" +
            "   CASE WHEN message.ui_seen THEN NULL" +
            "   WHEN NOT message.hash IS NULL THEN message.hash" +
            "   WHEN NOT message.msgid IS NULL THEN message.msgid" +
            "   ELSE message.id END) AS visible_unseen" +
            ", SUM(message.attachments) AS totalAttachments" +
            ", SUM(message.total) AS totalSize" +
            ", message.priority AS ui_priority" +
            ", message.importance AS ui_importance" +
            ", MAX(CASE WHEN" +
            "   (:found AND folder.type <> '" + EntityFolder.ARCHIVE + "')" +
            "   OR (NOT :found AND folder.id = :folder)" +
            "   THEN message.received ELSE 0 END) AS dummy" +
            " FROM message" +
            " JOIN account_view AS account ON account.id = message.account" +
            " LEFT JOIN identity_view AS identity ON identity.id = message.identity" +
            " JOIN folder_view AS folder ON folder.id = message.folder" +
            " JOIN folder_view AS f ON f.id = :folder" +
            " WHERE (message.account = f.account OR message.account = identity.account OR " + is_outbox + ")" +
            " AND (:threading OR folder.id = :folder)" +
            " AND (NOT message.ui_hide OR :debug)" +
            " AND (NOT :found OR message.ui_found = :found)" +
            " GROUP BY CASE WHEN message.thread IS NULL OR NOT :threading THEN message.id ELSE message.thread END" +
            " HAVING (SUM((:found AND message.ui_found)" +
            " OR (NOT :found AND message.folder = :folder)) > 0)" +
            " AND SUM(NOT message.ui_hide OR :debug) > 0" +
            " AND (NOT (:filter_seen AND NOT :filter_unflagged) OR SUM(1 - message.ui_seen) > 0 OR " + is_outbox + ")" +
            " AND (NOT (:filter_unflagged AND NOT :filter_seen) OR COUNT(message.id) - SUM(1 - message.ui_flagged) > 0 OR " + is_outbox + ")" +
            " AND (NOT (:filter_seen AND :filter_unflagged) OR SUM(1 - message.ui_seen) > 0 OR COUNT(message.id) - SUM(1 - message.ui_flagged) > 0 OR " + is_outbox + ")" +
            " AND (NOT :filter_unknown OR SUM(message.avatar IS NOT NULL AND message.sender <> identity.email) > 0" +
            "   OR " + is_outbox + " OR " + is_drafts + " OR " + is_sent + ")" +
            " AND (NOT :filter_snoozed OR message.ui_snoozed IS NULL OR " + is_outbox + " OR " + is_drafts + ")" +
            " AND (NOT :filter_deleted OR NOT message.ui_deleted)" +
            " AND (:filter_language IS NULL OR SUM(message.language = :filter_language) > 0 OR " + is_outbox + ")" +
            " ORDER BY CASE WHEN :found THEN 0 ELSE -IFNULL(message.importance, 1) END" +
            ", CASE" +
            "   WHEN 'unread' = :sort1 THEN SUM(1 - message.ui_seen) = 0" +
            "   WHEN 'starred' = :sort1 THEN COUNT(message.id) - SUM(1 - message.ui_flagged) = 0" +
            "   WHEN 'priority' = :sort1 THEN -IFNULL(message.priority, 1)" +
            "   WHEN 'sender' = :sort1 THEN LOWER(message.sender)" +
            "   WHEN 'subject' = :sort1 THEN LOWER(message.subject)" +
            "   WHEN 'size' = :sort1 THEN -SUM(message.total)" +
            "   WHEN 'attachments' = :sort1 THEN -SUM(message.attachments)" +
            "   WHEN 'snoozed' = :sort1 THEN SUM(CASE WHEN message.ui_snoozed IS NULL THEN 0 ELSE 1 END) = 0" +
            "   WHEN 'touched' = :sort1 THEN IFNULL(-message.last_touched, 0)" +
            "   ELSE 0" +
            "  END" +
            ", CASE" +
            "   WHEN 'unread' = :sort2 THEN SUM(1 - message.ui_seen) = 0" +
            "   WHEN 'starred' = :sort2 THEN COUNT(message.id) - SUM(1 - message.ui_flagged) = 0" +
            "   ELSE 0" +
            "  END" +
            ", CASE WHEN :ascending THEN message.received ELSE -message.received END")
    DataSource.Factory<Integer, TupleMessageEx> pagedFolder(
            long folder, boolean threading,
            String sort1, String sort2, boolean ascending,
            boolean filter_seen, boolean filter_unflagged, boolean filter_unknown, boolean filter_snoozed, boolean filter_deleted, String filter_language,
            boolean found,
            boolean debug);

    @Transaction
    @Query("SELECT message.*" +
            ", account.pop AS accountProtocol, account.name AS accountName, account.category AS accountCategory, COALESCE(identity.color, folder.color, account.color) AS accountColor" +
            ", account.notify AS accountNotify, account.summary AS accountSummary, account.leave_on_server AS accountLeaveOnServer, account.leave_deleted AS accountLeaveDeleted, account.auto_seen AS accountAutoSeen" +
            ", folder.name AS folderName, folder.color AS folderColor, folder.display AS folderDisplay, folder.type AS folderType, NULL AS folderInheritedType, folder.unified AS folderUnified, folder.read_only AS folderReadOnly" +
            ", IFNULL(identity.display, identity.name) AS identityName, identity.email AS identityEmail, identity.color AS identityColor, identity.synchronize AS identitySynchronize" +
            ", message.`from` AS senders" +
            ", message.`to` AS recipients" +
            ", 1 AS count" +
            ", CASE WHEN message.ui_seen THEN 0 ELSE 1 END AS unseen" +
            ", CASE WHEN message.ui_flagged THEN 0 ELSE 1 END AS unflagged" +
            ", (folder.type = '" + EntityFolder.DRAFTS + "') AS drafts" +
            ", 1 AS visible" +
            ", NOT message.ui_seen AS visible_unseen" +
            ", attachments AS totalAttachments" +
            ", message.total AS totalSize" +
            ", message.priority AS ui_priority" +
            ", message.importance AS ui_importance" +
            " FROM message" +
            " JOIN account_view AS account ON account.id = message.account" +
            " LEFT JOIN identity_view AS identity ON identity.id = message.identity" +
            " JOIN folder_view AS folder ON folder.id = message.folder" +
            " WHERE message.account = :account" +
            " AND message.thread = :thread" +
            " AND (:id IS NULL OR message.id = :id)" +
            " AND (NOT :filter_archive" +
            "  OR folder.type <> '" + EntityFolder.ARCHIVE + "'" +
            "  OR NOT EXISTS" +
            "   (SELECT * FROM message m" +
            "   WHERE m.id <> message.id" +
            "   AND m.account = message.account" +
            "   AND m.thread = message.thread" +
            "   AND (m.hash = message.hash OR m.msgid = message.msgid)" +
            "   AND NOT m.ui_hide))" +
            " AND (NOT message.ui_hide OR :debug)" +
            " ORDER BY CASE WHEN :ascending THEN message.received ELSE -message.received END" +
            ", CASE" +
            " WHEN folder.type = '" + EntityFolder.INBOX + "' THEN 1" +
            " WHEN folder.type = '" + EntityFolder.OUTBOX + "' THEN 2" +
            " WHEN folder.type = '" + EntityFolder.DRAFTS + "' THEN 3" +
            " WHEN folder.type = '" + EntityFolder.SENT + "' THEN 4" +
            " WHEN folder.type = '" + EntityFolder.TRASH + "' THEN 5" +
            " WHEN folder.type = '" + EntityFolder.JUNK + "' THEN 6" +
            " WHEN folder.type = '" + EntityFolder.SYSTEM + "' THEN 7" +
            " WHEN folder.type = '" + EntityFolder.USER + "' THEN 8" +
            " WHEN folder.type = '" + EntityFolder.ARCHIVE + "' THEN 9" +
            " ELSE 999 END")
    DataSource.Factory<Integer, TupleMessageEx> pagedThread(
            long account, String thread, Long id,
            boolean filter_archive,
            boolean ascending, boolean debug);

    @Query("SELECT COUNT(*) AS pending, SUM(message.error IS NOT NULL) AS errors" +
            " FROM message" +
            " JOIN folder_view AS folder ON folder.id = message.folder" +
            " WHERE " + is_outbox +
            " AND NOT message.ui_hide" +
            " AND (NOT message.ui_snoozed IS NULL OR message.error IS NOT NULL)")
    LiveData<TupleOutboxStats> liveOutboxPending();

    @Query("SELECT account.name AS accountName" +
            ", COUNT(message.id) AS count" +
            ", SUM(message.ui_seen) AS seen" +
            " FROM message" +
            " JOIN account_view AS account ON account.id = message.account" +
            " JOIN folder_view AS folder ON folder.id = message.folder" +
            " WHERE message.account = :account" +
            " AND message.thread = :thread" +
            " AND (:id IS NULL OR message.id = :id)" +
            " AND (NOT :filter_archive OR folder.type <> '" + EntityFolder.ARCHIVE +
            "' OR (SELECT COUNT(m.id) FROM message m" +
            "   WHERE m.account = message.account" +
            "   AND (m.hash = message.hash OR m.msgid = message.msgid)) = 1)" +
            " AND NOT message.ui_hide" +
            " GROUP BY account.id")
    LiveData<TupleThreadStats> liveThreadStats(long account, String thread, Long id, boolean filter_archive);

    @Query("SELECT id FROM message" +
            " WHERE account = :account" +
            " AND thread = :thread" +
            " AND ui_hide")
    LiveData<List<Long>> liveHiddenThread(long account, String thread);

    @Query("SELECT message.id FROM message" +
            " JOIN folder_view AS folder ON folder.id = message.folder" +
            " WHERE message.account = :account" +
            " AND message.thread = :thread" +
            " AND folder.type <> '" + EntityFolder.DRAFTS + "'" +
            " AND folder.type <> '" + EntityFolder.OUTBOX + "'" +
            " AND folder.type <> '" + EntityFolder.SENT + "'" +
            " AND folder.type <> '" + EntityFolder.ARCHIVE + "'" +
            " AND NOT ui_seen" +
            " AND NOT ui_hide")
    LiveData<List<Long>> liveUnreadThread(long account, String thread);

    static String FTS_STATS = "SELECT SUM(fts) AS fts, COUNT(*) AS total FROM message" +
            " JOIN folder_view AS folder ON folder.id = message.folder" +
            " JOIN account_view AS account ON account.id = message.account" +
            " WHERE content" +
            " AND account.synchronize" +
            " AND folder.type <> '" + EntityFolder.OUTBOX + "'";

    @Query(FTS_STATS)
    LiveData<TupleFtsStats> liveFts();

    @Query(FTS_STATS)
    TupleFtsStats getFts();

    @Query("SELECT COUNT(*) FROM message" +
            " WHERE id IN (:ids)" +
            " AND (raw IS NULL OR NOT raw)")
    LiveData<Integer> liveRaw(long[] ids);

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

    @Query("SELECT id" +
            " FROM message" +
            " WHERE (:folder IS NULL OR folder = :folder)" +
            " AND NOT ui_hide" +
            " ORDER BY message.received DESC")
    List<Long> getMessageIdsByFolder(Long folder);

    @Query("SELECT `to` FROM message" +
            " JOIN folder_view AS folder ON folder.id = message.folder" +
            " WHERE message.account = :account" +
            " AND folder.type = '" + EntityFolder.SENT + "'" +
            " AND message.sent > :after" +
            " AND message.wasforwardedfrom IS NOT NULL" +
            " GROUP BY `to`" +
            " ORDER BY COUNT(*) DESC" +
            " LIMIT 20")
    List<String> getForwardAddresses(long account, long after);

    @Query("SELECT identity, COUNT(*) AS count" +
            " FROM message" +
            " WHERE folder = :folder" +
            " GROUP BY identity")
    List<TupleIdentityCount> getIdentitiesByFolder(long folder);

    @Transaction
    @Query("SELECT message.id FROM message" +
            " JOIN folder_view AS folder ON folder.id = message.folder" +
            " JOIN account_view AS account ON account.id = message.account" +
            " WHERE NOT fts" +
            " AND account.synchronize" +
            " AND folder.type <> '" + EntityFolder.OUTBOX + "'" +
            " ORDER BY message.received")
    Cursor getMessageFts();

    @Query("SELECT message.id, account, thread, (:find IS NULL" +
            //" OR (:senders AND `from` LIKE :find COLLATE NOCASE)" + // no index
            //" OR (:recipients AND `to` LIKE :find COLLATE NOCASE)" + // no index
            //" OR (:recipients AND `cc` LIKE :find COLLATE NOCASE)" + // no index
            //" OR (:recipients AND `bcc` LIKE :find COLLATE NOCASE)" + // no index
            //" OR (:subject AND `subject` LIKE :find COLLATE NOCASE)" + // unsuitable index
            //" OR (:keywords AND `keywords` LIKE :find COLLATE NOCASE)" + // no index
            //" OR (:message AND `preview` LIKE :find COLLATE NOCASE)" + // no index
            //" OR (:notes AND `notes` LIKE :find COLLATE NOCASE)" + // no index
            //" OR (:headers AND `headers` LIKE :find COLLATE NOCASE)" + // no index
            //" OR (attachment.name LIKE :find COLLATE NOCASE)" + // no index
            //" OR (attachment.type LIKE :find COLLATE NOCASE)" +
            ") AS matched" + // no index
            " FROM message" +
            " LEFT JOIN attachment ON attachment.message = message.id" +
            " WHERE NOT ui_hide" +
            " AND (:account IS NULL OR account = :account)" +
            " AND (:folder IS NULL OR folder = :folder)" +
            " AND (NOT :unseen OR NOT ui_seen)" +
            " AND (NOT :flagged OR ui_flagged)" +
            " AND (NOT :hidden OR NOT ui_snoozed IS NULL OR ui_unsnoozed)" +
            " AND (NOT :encrypted OR ui_encrypt > 0)" +
            " AND (NOT :with_attachments OR attachments > 0)" +
            " AND (NOT :with_notes OR NOT `notes` IS NULL)" +
            " AND (:type_count = 0 OR attachment.type IN (:types))" +
            " AND (:size IS NULL OR total > :size)" +
            " AND (:after IS NULL OR received > :after)" +
            " AND (:before IS NULL OR received < :before)" +
            " AND (:touched IS NULL OR last_touched > :touched)" +
            " AND NOT message.folder IN (:exclude)" +
            " GROUP BY message.id" +
            " ORDER BY CASE WHEN :touched IS NULL THEN received ELSE last_touched END DESC" +
            " LIMIT :limit OFFSET :offset")
    List<TupleMatch> matchMessages(
            Long account, Long folder, long[] exclude, String find,
            //boolean senders, boolean recipients, boolean subject, boolean keywords, boolean message, boolean notes, boolean headers,
            boolean unseen, boolean flagged, boolean hidden, boolean encrypted, boolean with_attachments, boolean with_notes,
            int type_count, String[] types,
            Integer size,
            Long after, Long before, Long touched,
            int limit, int offset);

    @Query("SELECT id" +
            " FROM message" +
            " WHERE content" +
            " ORDER BY message.received DESC")
    Cursor getMessageWithContent();

    @Query("SELECT message.id" +
            " FROM message" +
            " JOIN account ON account.id = message.account" +
            " LEFT JOIN identity_view AS identity ON identity.id = message.identity" +
            " JOIN folder_view AS folder ON folder.id = message.folder" +
            " WHERE account.`synchronize`" +
            " AND CASE" +
            "  WHEN :folder IS NOT NULL THEN folder.id = :folder" +
            "  WHEN :type IS NOT NULL THEN folder.type = :type" +
            "  ELSE folder.unified" +
            " END" +
            " AND NOT ui_seen" +
            " AND (NOT :filter_unflagged OR message.ui_flagged)" +
            " AND (NOT :filter_unknown OR (message.avatar IS NOT NULL AND message.sender <> identity.email))" +
            " AND (NOT :filter_snoozed OR message.ui_snoozed IS NULL OR " + is_drafts + ")" +
            " AND (:filter_language IS NULL OR message.language = :filter_language)")
    List<Long> getMessageUnseen(
            Long folder, String type,
            boolean filter_unflagged, boolean filter_unknown, boolean filter_snoozed, String filter_language);

    @Query("SELECT message.*" +
            " FROM message" +
            " LEFT JOIN account_view AS account ON account.id = message.account" +
            " WHERE account = :account" +
            " AND thread = :thread" +
            " AND (:id IS NULL OR message.id = :id)" +
            " AND (:folder IS NULL OR message.folder = :folder)" +
            " AND (NOT uid IS NULL OR account.pop <> " + EntityAccount.TYPE_IMAP + ")" +
            " AND NOT ui_hide")
    List<EntityMessage> getMessagesByThread(long account, String thread, Long id, Long folder);

    @Query("SELECT * FROM message" +
            " WHERE account = :account" +
            " AND msgid = :msgid")
    List<EntityMessage> getMessagesByMsgId(long account, String msgid);

    @Query("SELECT message.* FROM message" +
            " JOIN folder ON folder.id = message.folder" +
            " WHERE message.account = :account" +
            " AND folder.type = :folderType" +
            " AND message.msgid = :msgid")
    EntityMessage getMessage(long account, String folderType, String msgid);

    @Query("SELECT * FROM message" +
            " WHERE account = :account" +
            " AND inreplyto = :inreplyto")
    List<EntityMessage> getMessagesByInReplyTo(long account, String inreplyto);

    @Query("SELECT thread, msgid, hash, inreplyto FROM message" +
            " WHERE account = :account" +
            " AND (msgid IN (:msgids) OR inreplyto IN (:msgids))" +
            " AND (:from IS NULL OR received IS NULL OR received > :from)" +
            " AND (:to IS NULL OR received IS NULL OR received < :to)")
    List<TupleThreadInfo> getThreadInfo(long account, List<String> msgids, Long from, Long to);

    @Query("SELECT * FROM message" +
            " WHERE account = :account" +
            " AND sender = :sender" +
            " AND subject = :subject" +
            " AND received >= :since")
    List<EntityMessage> getMessagesBySubject(long account, String sender, String subject, long since);

    @Query("SELECT message.* FROM message" +
            " LEFT JOIN message AS base ON base.id = :id" +
            " WHERE message.account = :account" +
            " AND (message.id = :id" +
            " OR (message.msgid = :msgid AND message.folder <> base.folder)" +
            " OR (NOT :hash IS NULL AND message.hash IS :hash))")
    List<EntityMessage> getMessagesBySimilarity(long account, long id, String msgid, String hash);

    @Query("SELECT COUNT(*) FROM message" +
            " WHERE folder = :folder" +
            " AND msgid = :msgid" +
            " AND (:hidden OR NOT message.ui_hide)")
    int countMessageByMsgId(long folder, String msgid, boolean hidden);

    @Query("SELECT COUNT(*) FROM message" +
            " JOIN folder_view AS folder ON folder.id = message.folder" +
            " WHERE message.id = :id" +
            " AND NOT message.ui_hide" +
            " AND (NOT :filter_seen OR NOT message.ui_seen)" +
            " AND (NOT :filter_unflagged OR message.ui_flagged)" +
            " AND (NOT :filter_snoozed OR message.ui_snoozed IS NULL OR " + is_drafts + ")")
    int countVisible(long id, boolean filter_seen, boolean filter_unflagged, boolean filter_snoozed);

    @Query("SELECT COUNT(id)" +
            " FROM message" +
            " WHERE folder = :folder" +
            " AND sender = :sender")
    int countSender(long folder, String sender);

    @Query("SELECT COUNT(*) FROM message" +
            " JOIN folder ON folder.id = message.folder" +
            " WHERE folder.account IS NULL")
    int countOutbox();

    @Query("SELECT COUNT(*) FROM message")
    int countTotal();

    @Query("SELECT COUNT(*) FROM message" +
            " WHERE folder = :folder" +
            " AND NOT ui_seen")
    int countUnseen(long folder);

    @Query("SELECT COUNT(*) FROM message" +
            " WHERE folder = :folder" +
            " AND ui_hide")
    int countHidden(long folder);

    @Query("SELECT COUNT(*)" +
            " FROM message" +
            " WHERE folder = :folder" +
            " AND notifying <> 0" +
            " AND notifying <> " + EntityMessage.NOTIFYING_IGNORE +
            " AND NOT (message.ui_seen OR message.ui_ignored OR message.ui_hide)")
    int countNotifying(long folder);

    String FETCH_MESSAGE = "SELECT message.*" +
            ", account.pop AS accountProtocol, account.name AS accountName, account.category AS accountCategory, identity.color AS accountColor" +
            ", account.notify AS accountNotify, account.summary AS accountSummary, account.leave_on_server AS accountLeaveOnServer, account.leave_deleted AS accountLeaveDeleted, account.auto_seen AS accountAutoSeen" +
            ", folder.name AS folderName, folder.color AS folderColor, folder.display AS folderDisplay, folder.type AS folderType, NULL AS folderInheritedType, folder.unified AS folderUnified, folder.read_only AS folderReadOnly" +
            ", IFNULL(identity.display, identity.name) AS identityName, identity.email AS identityEmail, identity.color AS identityColor, identity.synchronize AS identitySynchronize" +
            ", message.`from` AS senders" +
            ", message.`to` AS recipients" +
            ", 1 AS count" +
            ", CASE WHEN message.ui_seen THEN 0 ELSE 1 END AS unseen" +
            ", CASE WHEN message.ui_flagged THEN 0 ELSE 1 END AS unflagged" +
            ", (folder.type = '" + EntityFolder.DRAFTS + "') AS drafts" +
            ", 1 AS visible" +
            ", NOT message.ui_seen AS visible_unseen" +
            ", message.attachments AS totalAttachments" +
            ", message.total AS totalSize" +
            ", message.priority AS ui_priority" +
            ", message.importance AS ui_importance" +
            " FROM message" +
            " JOIN account_view AS account ON account.id = message.account" +
            " LEFT JOIN identity_view AS identity ON identity.id = message.identity" +
            " JOIN folder_view AS folder ON folder.id = message.folder" +
            " WHERE message.id = :id";

    @Query(FETCH_MESSAGE)
    LiveData<TupleMessageEx> liveMessage(long id);

    @Query(FETCH_MESSAGE)
    TupleMessageEx getMessageEx(long id);

    String FETCH_KEYWORDS = "SELECT message.keywords AS selected, folder.keywords AS available" +
            " FROM message" +
            " JOIN folder ON folder.id = message.folder" +
            " WHERE message.id = :id";

    @Query(FETCH_KEYWORDS)
    LiveData<TupleKeyword.Persisted> liveMessageKeywords(long id);

    @Query(FETCH_KEYWORDS)
    TupleKeyword.Persisted getMessageKeywords(long id);

    @Transaction
    @Query("SELECT message.*" +
            ", account.pop AS accountProtocol, account.name AS accountName, account.category AS accountCategory, COALESCE(identity.color, folder.color, account.color) AS accountColor" +
            ", account.notify AS accountNotify, account.summary AS accountSummary, account.leave_on_server AS accountLeaveOnServer, account.leave_deleted AS accountLeaveDeleted, account.auto_seen AS accountAutoSeen" +
            ", folder.name AS folderName, folder.color AS folderColor, folder.display AS folderDisplay, folder.type AS folderType, NULL AS folderInheritedType, folder.unified AS folderUnified, folder.read_only AS folderReadOnly" +
            ", IFNULL(identity.display, identity.name) AS identityName, identity.email AS identityEmail, identity.color AS identityColor, identity.synchronize AS identitySynchronize" +
            ", message.`from` AS senders" +
            ", message.`to` AS recipients" +
            ", 1 AS count" +
            ", 1 AS unseen" +
            ", 0 AS unflagged" +
            ", 0 AS drafts" +
            ", 1 AS visible" +
            ", NOT message.ui_seen AS visible_unseen" +
            ", message.attachments AS totalAttachments" +
            ", message.total AS totalSize" +
            ", message.priority AS ui_priority" +
            ", message.importance AS ui_importance" +
            " FROM message" +
            " JOIN account_view AS account ON account.id = message.account" +
            " LEFT JOIN identity_view AS identity ON identity.id = message.identity" +
            " JOIN folder_view AS folder ON folder.id = message.folder" +
            " WHERE account.`synchronize`" +
            " AND folder.notify" +
            " AND (account.created IS NULL" +
            "  OR message.received > account.created" +
            "  OR message.sent > account.created" +
            "  OR message.ui_unsnoozed)" +
            " AND (uid IS NOT NULL OR account.pop <> " + EntityAccount.TYPE_IMAP + ")" +
            " AND message.notifying <> " + EntityMessage.NOTIFYING_IGNORE +
            " AND (message.notifying <> 0 OR NOT (message.ui_seen OR message.ui_ignored OR message.ui_hide))" +
            " ORDER BY message.received DESC")
    LiveData<List<TupleMessageEx>> liveUnseenNotify();

    @Transaction
    @Query("SELECT account.id AS account, folder.id AS folder," +
            " COUNT(message.id) AS unseen," +
            " SUM(CASE WHEN account.created IS NULL OR message.received > account.created OR message.sent > account.created THEN NOT ui_ignored ELSE 0 END) AS notifying" +
            " FROM message" +
            " JOIN account_view AS account ON account.id = message.account" +
            " JOIN folder_view AS folder ON folder.id = message.folder" +
            " WHERE (:account IS NULL OR account.id = :account)" +
            " AND account.`synchronize`" +
            " AND folder.notify" +
            " AND message.notifying <> " + EntityMessage.NOTIFYING_IGNORE +
            " AND NOT (message.ui_seen OR message.ui_hide)" +
            " GROUP BY folder.id" +
            " ORDER BY folder.id")
    LiveData<List<TupleMessageStats>> liveWidgetUnseen(Long account);

    @Query("SELECT :account AS account, folder.id AS folder," +
            " COUNT(message.id) AS unseen," +
            " SUM(CASE WHEN account.created IS NULL OR message.received > account.created OR message.sent > account.created THEN NOT ui_ignored ELSE 0 END) AS notifying" +
            " FROM message" +
            " JOIN account_view AS account ON account.id = message.account" +
            " JOIN folder_view AS folder ON folder.id = message.folder" +
            " WHERE (:account IS NULL OR account.id = :account)" +
            " AND account.`synchronize`" +
            " AND (:folder IS NULL OR folder.id = :folder)" +
            " AND folder.notify" +
            " AND message.notifying <> " + EntityMessage.NOTIFYING_IGNORE +
            " AND NOT (message.ui_seen OR message.ui_hide)")
    TupleMessageStats getWidgetUnseen(Long account, Long folder);

    @Transaction
    @Query("SELECT folder, COUNT(*) AS total" +
            ", SUM(ui_seen) AS seen" +
            ", SUM(ui_flagged) AS flagged" +
            " FROM message" +
            " WHERE NOT ui_hide" +
            " AND message.ui_snoozed IS NULL" +
            " GROUP BY folder" +
            " ORDER BY folder")
    LiveData<List<TupleMessageWidgetCount>> liveWidgetUnified();

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT message.*" +
            ", account.name AS accountName, COALESCE(identity.color, folder.color, account.color) AS accountColor" +
            ", SUM(1 - message.ui_seen) AS unseen" +
            ", COUNT(message.id) - SUM(message.ui_flagged) AS unflagged" +
            ", MAX(message.received) AS dummy" +
            " FROM message" +
            " JOIN account_view AS account ON account.id = message.account" +
            " JOIN folder_view AS folder ON folder.id = message.folder" +
            " LEFT JOIN identity ON identity.id = message.identity" +
            " WHERE account.`synchronize`" +
            " AND (:account IS NULL OR account.id = :account)" +
            " AND ((:folder IS NULL AND folder.unified) OR folder.id = :folder)" +
            " AND NOT message.ui_hide" +
            " AND message.ui_snoozed IS NULL" +
            " AND (NOT :unseen OR NOT message.ui_seen)" +
            " AND (NOT :flagged OR message.ui_flagged)" +
            " GROUP BY account.id" +
            ", CASE WHEN message.thread IS NULL OR NOT :threading THEN message.id ELSE message.thread END" +
            " ORDER BY message.received DESC" +
            " LIMIT :limit")
    List<TupleMessageWidget> getWidgetUnified(Long account, Long folder, boolean threading, boolean unseen, boolean flagged, int limit);

    @Query("SELECT uid FROM message" +
            " WHERE folder = :folder" +
            " AND (:received IS NULL OR received >= :received)" +
            " AND NOT uid IS NULL" +
            " ORDER BY uid")
    List<Long> getUids(long folder, Long received);

    @Query("SELECT * FROM message" +
            " WHERE folder = :folder" +
            " AND (:received IS NULL OR received >= :received)" +
            " AND NOT uid IS NULL" +
            " AND NOT content")
    List<EntityMessage> getMessagesWithoutContent(long folder, Long received);

    @Query("SELECT uid FROM message" +
            " WHERE folder = :folder" +
            " AND ui_deleted" +
            " AND NOT uid IS NULL")
    List<Long> getDeletedUids(long folder);

    @Query("SELECT uid FROM message" +
            " WHERE folder = :folder" +
            " AND NOT uid IS NULL" +
            " AND ((NOT ui_busy IS NULL AND ui_busy > :time)" +
            " OR (EXISTS (SELECT * FROM operation WHERE operation.id = message.id)))")
    List<Long> getBusyUids(long folder, long time);

    @Query("SELECT id, uidl, msgid, ui_hide, ui_busy, ui_flagged FROM message" +
            " WHERE folder = :folder")
    List<TupleUidl> getUidls(long folder);

    @Query("SELECT * FROM message" +
            " WHERE (:folder IS NULL OR folder = :folder)" +
            " AND NOT ui_snoozed IS NULL")
    List<EntityMessage> getSnoozed(Long folder);

    @Query("SELECT COUNT(*) FROM message" +
            " WHERE NOT ui_snoozed IS NULL" +
            " AND ui_snoozed <> " + Long.MAX_VALUE)
    int getSnoozedCount();

    @Query("SELECT id AS _id, subject AS suggestion FROM message" +
            " WHERE :subject" +
            " AND (:account IS NULL OR message.account = :account)" +
            " AND (:folder IS NULL OR message.folder = :folder)" +
            " AND subject LIKE :query" +
            " AND NOT message.ui_hide" +
            " GROUP BY subject" +

            " UNION" +

            " SELECT id AS _id, sender AS suggestion FROM message" +
            " WHERE :senders" +
            " AND (:account IS NULL OR message.account = :account)" +
            " AND (:folder IS NULL OR message.folder = :folder)" +
            " AND sender LIKE :query" +
            " AND NOT message.ui_hide" +
            " GROUP BY sender" +

            " ORDER BY sender, subject" +
            " LIMIT :limit")
    Cursor getSuggestions(boolean subject, boolean senders, Long account, Long folder, String query, int limit);

    @Query("SELECT language FROM message" +
            " WHERE (:account IS NULL OR message.account = :account)" +
            " AND (:folder IS NULL OR message.folder = :folder)" +
            " AND NOT message.ui_hide" +
            " AND NOT message.language IS NULL" +
            " GROUP BY language")
    List<String> getLanguages(Long account, Long folder);

    @Insert
    long insertMessage(EntityMessage message);

    @Update
    int updateMessage(EntityMessage message);

    @Query("UPDATE message SET thread = :thread" +
            " WHERE account = :account" +
            " AND thread = :old AND NOT (:old IS :thread)" +
            " AND (:since IS NULL OR received >= :since)")
    int updateMessageThread(long account, String old, String thread, Long since);

    @Query("UPDATE message SET uid = :uid WHERE id = :id AND NOT (uid IS :uid)")
    int setMessageUid(long id, Long uid);

    @Query("UPDATE message SET msgid = :msgid WHERE id = :id AND NOT (msgid IS :msgid)")
    int setMessageMsgId(long id, String msgid);

    @Query("UPDATE message SET hash = :hash WHERE id = :id AND NOT (hash IS :hash)")
    int setMessageHash(long id, String hash);

    @Query("UPDATE message SET priority = :priority WHERE id = :id AND NOT (priority IS :priority)")
    int setMessagePriority(long id, Integer priority);

    @Query("UPDATE message SET sensitivity = :sensitivity WHERE id = :id AND NOT (sensitivity IS :sensitivity)")
    int setMessageSensitivity(long id, Integer sensitivity);

    @Query("UPDATE message SET importance = :importance WHERE id = :id AND NOT (importance IS :importance)")
    int setMessageImportance(long id, Integer importance);

    @Query("UPDATE message SET receipt_request = :receipt_request WHERE id = :id AND NOT (receipt_request IS :receipt_request)")
    int setMessageReceiptRequest(long id, Boolean receipt_request);

    @Transaction
    @Query("UPDATE message SET notifying = :notifying WHERE id = :id AND NOT (notifying IS :notifying)")
    int setMessageNotifying(long id, int notifying);

    @Query("UPDATE message SET fts = :fts WHERE id = :id AND NOT (fts IS :fts)")
    int setMessageFts(long id, boolean fts);

    @Query("UPDATE message SET `to` = :to WHERE id = :id AND NOT (`to` IS :to)")
    int setMessageTo(long id, String to);

    @Query("UPDATE message SET `cc` = :cc WHERE id = :id AND NOT (`cc` IS :cc)")
    int setMessageCc(long id, String cc);

    @Query("UPDATE message SET `bcc` = :bcc WHERE id = :id AND NOT (`bcc` IS :bcc)")
    int setMessageBcc(long id, String bcc);

    @Query("UPDATE message SET received = :received WHERE id = :id AND NOT (received IS :received)")
    int setMessageReceived(long id, long received);

    @Query("UPDATE message SET subject = :subject WHERE id = :id AND NOT (subject IS :subject)")
    int setMessageSubject(long id, String subject);

    @Query("UPDATE message SET recent = :recent WHERE id = :id AND NOT (recent IS :recent)")
    int setMessageRecent(long id, boolean recent);

    @Query("UPDATE message SET seen = :seen WHERE id = :id AND NOT (seen IS :seen)")
    int setMessageSeen(long id, boolean seen);

    @Query("UPDATE message SET flagged = :flagged WHERE id = :id AND NOT (flagged IS :flagged)")
    int setMessageFlagged(long id, boolean flagged);

    @Query("UPDATE message SET deleted = :deleted WHERE id = :id AND NOT (deleted IS :deleted)")
    int setMessageDeleted(long id, boolean deleted);

    @Query("UPDATE message SET answered = :answered WHERE id = :id AND NOT (answered IS :answered)")
    int setMessageAnswered(long id, boolean answered);

    @Query("UPDATE message SET keywords = :keywords WHERE id = :id AND NOT (keywords IS :keywords)")
    int setMessageKeywords(long id, String keywords);

    @Query("UPDATE message SET labels = :labels WHERE id = :id AND NOT (labels IS :labels)")
    int setMessageLabels(long id, String labels);

    @Query("UPDATE message SET ui_seen = :ui_seen WHERE id = :id AND NOT (ui_seen IS :ui_seen)")
    int setMessageUiSeen(long id, boolean ui_seen);

    @Query("UPDATE message" +
            " SET ui_flagged = :ui_flagged, color = :color" +
            " WHERE id = :id" +
            " AND (NOT (ui_flagged IS :ui_flagged) OR NOT (color IS :color))")
    int setMessageUiFlagged(long id, boolean ui_flagged, Integer color);

    @Query("UPDATE message SET ui_deleted = :ui_deleted WHERE id = :id AND NOT (ui_deleted IS :ui_deleted)")
    int setMessageUiDeleted(long id, boolean ui_deleted);

    @Query("UPDATE message SET ui_answered = :ui_answered WHERE id = :id AND NOT (ui_answered IS :ui_answered)")
    int setMessageUiAnswered(long id, boolean ui_answered);

    @Query("UPDATE message SET ui_hide = :ui_hide WHERE id = :id AND NOT (ui_hide IS :ui_hide)")
    int setMessageUiHide(long id, Boolean ui_hide);

    @Transaction
    @Query("UPDATE message SET ui_hide = 1" +
            " WHERE folder = :folder" +
            " AND NOT ui_flagged" +
            " AND id NOT IN (" +
            "    SELECT id FROM message" +
            "    WHERE folder = :folder" +
            "    ORDER BY received DESC" +
            "    LIMIT :keep)")
    int setMessagesUiHide(long folder, int keep);

    @Transaction
    @Query("UPDATE message SET ui_ignored = :ui_ignored WHERE id = :id AND NOT (ui_ignored IS :ui_ignored)")
    int setMessageUiIgnored(long id, boolean ui_ignored);

    @Transaction
    @Query("UPDATE message SET ui_silent = :ui_silent WHERE id = :id AND NOT (ui_silent IS :ui_silent)")
    int setMessageUiSilent(long id, boolean ui_silent);

    @Query("UPDATE message SET ui_local_only = :ui_local_only WHERE id = :id AND NOT (ui_local_only IS :ui_local_only)")
    int setMessageUiLocalOnly(long id, boolean ui_local_only);

    @Query("UPDATE message SET ui_busy = :busy WHERE id = :id AND NOT (ui_busy IS :busy)")
    int setMessageUiBusy(long id, Long busy);

    @Query("UPDATE message" +
            " SET received = :sent, sent = :sent" +
            " WHERE id = :id" +
            " AND (NOT (received IS :sent) OR NOT (sent IS :sent))")
    int setMessageSent(long id, Long sent);

    @Query("UPDATE message SET warning = :warning WHERE id = :id AND NOT (warning IS :warning)")
    int setMessageWarning(long id, String warning);

    @Query("UPDATE message SET error = :error WHERE id = :id AND NOT (error IS :error)")
    int setMessageError(long id, String error);

    @Query("UPDATE message SET identity = :identity WHERE id = :id AND NOT (identity IS :identity)")
    int setMessageIdentity(long id, Long identity);

    @Query("UPDATE message SET revision = :revision WHERE id = :id AND NOT (revision IS :revision)")
    int setMessageRevision(long id, Integer revision);

    @Query("UPDATE message SET revisions = :revisions WHERE id = :id AND NOT (revisions IS :revisions)")
    int setMessageRevisions(long id, Integer revisions);

    @Query("UPDATE message" +
            " SET content = 0, fts = 0, language = NULL, plain_only = NULL, preview = NULL" +
            " WHERE id = :id" +
            " AND (NOT (content IS 0)" +
            "  OR NOT (fts IS 0)" +
            "  OR language IS NOT NULL" +
            "  OR plain_only IS NOT NULL" +
            "  OR preview IS NOT NULL)")
    int resetMessageContent(long id);

    @Query("UPDATE message" +
            " SET content = :content" +
            ", fts = 0" +
            ", language = :language" +
            ", plain_only = :plain_only" +
            ", preview = :preview" +
            ", warning = :warning" +
            " WHERE id = :id" +
            " AND (NOT (content IS :content)" +
            "  OR NOT (fts IS 0)" +
            "  OR NOT (language IS :language)" +
            "  OR NOT (plain_only IS :plain_only)" +
            "  OR NOT (preview IS :preview)" +
            "  OR NOT (warning IS :warning))")
    int setMessageContent(long id, boolean content, String language, Integer plain_only, String preview, String warning);

    @Query("UPDATE message" +
            " SET notes = :notes, notes_color = :color, fts = 0" +
            " WHERE id = :id" +
            " AND NOT (notes IS :notes AND notes_color IS :color)")
    int setMessageNotes(long id, String notes, Integer color);

    @Query("UPDATE message" +
            " SET size = :size, total = :total" +
            " WHERE id = :id" +
            " AND (NOT (size IS :size) OR NOT (total IS :total))")
    int setMessageSize(long id, Long size, Long total);

    @Query("UPDATE message SET headers = :headers WHERE id = :id AND NOT (headers IS :headers)")
    int setMessageHeaders(long id, String headers);

    @Query("UPDATE message SET raw = :raw WHERE id = :id AND NOT (raw IS :raw)")
    int setMessageRaw(long id, Boolean raw);

    @Query("UPDATE message SET stored = :stored WHERE id = :id AND NOT (stored IS :stored)")
    int setMessageStored(long id, long stored);

    @Query("UPDATE message SET plain_only = :plain_only WHERE id = :id AND NOT (plain_only IS :plain_only)")
    int setMessagePlainOnly(long id, Integer plain_only);

    @Query("UPDATE message SET write_below = :write_below WHERE id = :id AND NOT (write_below IS :write_below)")
    int setMessageWriteBelow(long id, Boolean write_below);

    @Query("UPDATE message SET encrypt = :encrypt WHERE id = :id AND NOT (encrypt IS :encrypt)")
    int setMessageEncrypt(long id, Integer encrypt);

    @Query("UPDATE message SET ui_encrypt = :ui_encrypt WHERE id = :id AND NOT (ui_encrypt IS :ui_encrypt)")
    int setMessageUiEncrypt(long id, Integer ui_encrypt);

    @Query("UPDATE message SET verified = :verified WHERE id = :id AND NOT (verified IS :verified)")
    int setMessageVerified(long id, boolean verified);

    @Query("UPDATE message SET last_attempt = :last_attempt WHERE id = :id AND NOT (last_attempt IS :last_attempt)")
    int setMessageLastAttempt(long id, Long last_attempt);

    @Query("UPDATE message SET last_touched = :last_touched WHERE id = :id AND NOT (last_touched IS :last_touched)")
    int setMessageLastTouched(long id, Long last_touched);

    @Query("UPDATE message SET ui_ignored = 1" +
            " WHERE NOT ui_ignored" +
            " AND account IN (" +
            "  SELECT id FROM account" +
            "   WHERE :folder IS NOT NULL" +
            "   OR :type IS NOT NULL" +
            "   OR id = :account" +
            "   OR (:account IS NULL AND NOT notify))" +
            " AND folder IN (" +
            "  SELECT id FROM folder" +
            "   WHERE notify" +
            "   AND (id = :folder" +
            "   OR (type = :type AND type <> '" + EntityFolder.OUTBOX + "')" +
            "   OR (:folder IS NULL AND :type IS NULL AND unified)))")
    int ignoreAll(Long account, Long folder, String type);

    @Query("UPDATE message SET ui_found = :found WHERE id = :id AND NOT (ui_found IS :found)")
    int setMessageFound(long id, boolean found);

    @Query("UPDATE message SET ui_found = 0 WHERE NOT (ui_found IS 0)")
    int resetSearch();

    @Query("UPDATE message SET ui_snoozed = :wakeup WHERE id = :id AND NOT (ui_snoozed IS :wakeup)")
    int setMessageSnoozed(long id, Long wakeup);

    @Query("UPDATE message SET ui_unsnoozed = :unsnoozed WHERE id = :id AND NOT (ui_unsnoozed IS :unsnoozed)")
    int setMessageUnsnoozed(long id, boolean unsnoozed);

    @Query("UPDATE message SET show_images = :show_images WHERE id = :id AND NOT (show_images IS :show_images)")
    int setMessageShowImages(long id, boolean show_images);

    @Query("UPDATE message SET show_full = :show_full WHERE id = :id AND NOT (show_full IS :show_full)")
    int setMessageShowFull(long id, boolean show_full);

    @Query("UPDATE message SET notifying = 0 WHERE NOT (notifying IS 0)")
    int clearNotifyingMessages();

    @Query("UPDATE message SET headers = NULL" +
            " WHERE headers IS NOT NULL" +
            " AND account IN (SELECT id FROM account WHERE pop = " + EntityAccount.TYPE_IMAP + ")")
    int clearMessageHeaders();

    @Query("UPDATE message SET raw = NULL" +
            " WHERE raw IS NOT NULL" +
            " AND account IN (SELECT id FROM account WHERE pop = " + EntityAccount.TYPE_IMAP + ")")
    int clearRawMessages();

    @Query("UPDATE message SET fts = 0 WHERE NOT (fts IS 0)")
    int resetFts();

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
            " AND (ui_browsed OR received < :before)" +
            " AND NOT uid IS NULL")
    int deleteBrowsedMessages(long folder, long before);

    @Query("DELETE FROM message WHERE id IN (" +
            " SELECT id FROM message" +
            " WHERE folder = :folder" +
            " AND ui_hide" +
            " LIMIT :limit)")
    int deleteHiddenMessages(long folder, int limit);

    @Query("DELETE FROM message" +
            " WHERE folder = :folder" +
            " AND uid IS NULL" +
            " AND (ui_busy IS NULL OR ui_busy < :now)" +
            " AND NOT EXISTS" +
            "  (SELECT * FROM operation" +
            "  WHERE operation.message = message.id" +
            "  AND operation.name = '" + EntityOperation.ADD + "')" +
            " AND NOT EXISTS" +
            "  (SELECT * FROM operation o" +
            "  JOIN message m ON m.id = o.message" +
            "  WHERE o.account = message.account" +
            "  AND o.name IN ('" + EntityOperation.MOVE + "', '" + EntityOperation.COPY + "')" +
            "  AND m.msgid = message.msgid)"
    )
    int deleteOrphans(long folder, long now);

    @Query("SELECT * FROM message" +
            " WHERE folder = :folder" +
            " AND uid IS NULL" +
            " AND NOT EXISTS" +
            "  (SELECT * FROM operation" +
            "  WHERE operation.message = message.id)")
    List<EntityMessage> getDraftOrphans(long folder);

    @Query("SELECT * FROM message" +
            " WHERE folder = :folder" +
            " AND uid IS NULL" +
            " AND NOT EXISTS" +
            "  (SELECT * FROM operation" +
            "  WHERE operation.message = message.id" +
            "  AND operation.name = '" + EntityOperation.EXISTS + "')")
    List<EntityMessage> getSentOrphans(long folder);

    @Query("SELECT id FROM message" +
            " WHERE folder = :folder" +
            " AND received < :keep_time" +
            " AND NOT uid IS NULL" +
            " AND (ui_seen OR received < :keep_unread_time)" +
            " AND NOT ui_flagged" +
            " AND stored < :sync_time" + // moved, browsed
            " AND (ui_snoozed IS NULL OR ui_snoozed =" + Long.MAX_VALUE + ")")
    List<Long> getMessagesBefore(long folder, long sync_time, long keep_time, long keep_unread_time);

    @Query("DELETE FROM message" +
            " WHERE folder = :folder" +
            " AND received < :keep_time" +
            " AND NOT uid IS NULL" +
            " AND (ui_seen OR received < :keep_unread_time)" +
            " AND NOT ui_flagged" +
            " AND stored < :sync_time" + // moved, browsed
            " AND (ui_snoozed IS NULL OR ui_snoozed = " + Long.MAX_VALUE + ")")
    int deleteMessagesBefore(long folder, long sync_time, long keep_time, long keep_unread_time);

    @Transaction
    @Query("DELETE FROM message" +
            " WHERE folder = :folder" +
            " AND NOT ui_flagged" +
            " AND id NOT IN (" +
            "    SELECT id FROM message" +
            "    WHERE folder = :folder" +
            "    ORDER BY received DESC" +
            "    LIMIT :keep)")
    int deleteMessagesKeep(long folder, int keep);

    @Transaction
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT message.*" +
            ", account.pop AS accountProtocol, account.name AS accountName, account.category AS accountCategory, COALESCE(identity.color, folder.color, account.color) AS accountColor" +
            ", account.notify AS accountNotify, account.summary AS accountSummary, account.leave_on_server AS accountLeaveOnServer, account.leave_deleted AS accountLeaveDeleted, account.auto_seen AS accountAutoSeen" +
            ", folder.name AS folderName, folder.color AS folderColor, folder.display AS folderDisplay, folder.type AS folderType, NULL AS folderInheritedType, folder.unified AS folderUnified, folder.read_only AS folderReadOnly" +
            ", IFNULL(identity.display, identity.name) AS identityName, identity.email AS identityEmail, identity.color AS identityColor, identity.synchronize AS identitySynchronize" +
            ", '[' || substr(group_concat(message.`from`, ','), 0, 2048) || ']' AS senders" +
            ", '[' || substr(group_concat(message.`to`, ','), 0, 2048) || ']' AS recipients" +
            ", COUNT(message.id) AS count" +
            ", SUM(1 - message.ui_seen) AS unseen" +
            ", SUM(1 - message.ui_flagged) AS unflagged" +
            ", SUM(folder.type = '" + EntityFolder.DRAFTS + "') AS drafts" +
            ", COUNT(DISTINCT" +
            "   CASE WHEN NOT message.hash IS NULL THEN message.hash" +
            "   WHEN NOT message.msgid IS NULL THEN message.msgid" +
            "   ELSE message.id END) AS visible" +
            ", COUNT(DISTINCT" +
            "   CASE WHEN message.ui_seen THEN NULL" +
            "   WHEN NOT message.hash IS NULL THEN message.hash" +
            "   WHEN NOT message.msgid IS NULL THEN message.msgid" +
            "   ELSE message.id END) AS visible_unseen" +
            ", SUM(message.attachments) AS totalAttachments" +
            ", SUM(message.total) AS totalSize" +
            ", message.priority AS ui_priority" +
            ", message.importance AS ui_importance" +
            ", MAX(CASE WHEN" +
            "   (:found AND folder.type <> '" + EntityFolder.ARCHIVE + "')" +
            "   OR (NOT :found AND :type IS NULL AND folder.unified)" +
            "   OR (NOT :found AND folder.type = :type)" +
            "   THEN message.received ELSE 0 END) AS dummy" +
            " FROM message" +
            " JOIN account_view AS account ON account.id = message.account" +
            " LEFT JOIN identity_view AS identity ON identity.id = message.identity" +
            " JOIN folder_view AS folder ON folder.id = message.folder" +
            " WHERE account.`synchronize`" +
            " AND (:category IS NULL OR account.category = :category)" +
            " AND (:threading OR (:type IS NULL AND (folder.unified OR :found)) OR (:type IS NOT NULL AND folder.type = :type))" +
            " AND (NOT message.ui_hide OR :debug)" +
            " AND (NOT :found OR message.ui_found = :found)" +
            " GROUP BY account.id, CASE WHEN message.thread IS NULL OR NOT :threading THEN message.id ELSE message.thread END" +
            " HAVING (SUM((:found AND message.ui_found)" +
            " OR (NOT :found AND :type IS NULL AND folder.unified)" +
            " OR (NOT :found AND :type IS NOT NULL AND folder.type = :type)) > 0)" + // thread can be the same in different accounts
            " AND SUM(NOT message.ui_hide OR :debug) > 0" +
            " AND (NOT (:filter_seen AND NOT :filter_unflagged) OR SUM(1 - message.ui_seen) > 0)" +
            " AND (NOT (:filter_unflagged AND NOT :filter_seen) OR COUNT(message.id) - SUM(1 - message.ui_flagged) > 0)" +
            " AND (NOT (:filter_seen AND :filter_unflagged) OR SUM(1 - message.ui_seen) > 0 OR COUNT(message.id) - SUM(1 - message.ui_flagged) > 0)" +
            " AND (NOT :filter_unknown OR SUM(message.avatar IS NOT NULL AND message.sender <> identity.email) > 0)" +
            " AND (NOT :filter_snoozed OR message.ui_snoozed IS NULL OR " + is_drafts + ")" +
            " AND (NOT :filter_deleted OR NOT message.ui_deleted)" +
            " AND (:filter_language IS NULL OR SUM(message.language = :filter_language) > 0)" +
            " ORDER BY CASE WHEN :found THEN 0 ELSE -IFNULL(message.importance, 1) END" +
            ", CASE WHEN :group_category THEN account.category ELSE '' END COLLATE NOCASE" +
            ", CASE" +
            "   WHEN 'unread' = :sort1 THEN SUM(1 - message.ui_seen) = 0" +
            "   WHEN 'starred' = :sort1 THEN COUNT(message.id) - SUM(1 - message.ui_flagged) = 0" +
            "   WHEN 'priority' = :sort1 THEN -IFNULL(message.priority, 1)" +
            "   WHEN 'sender' = :sort1 THEN LOWER(message.sender)" +
            "   WHEN 'sender_name' = :sort1 THEN LOWER(COALESCE(json_extract(message.`from`, '$[0].personal'), json_extract(message.`from`, '$[0].address')))" +
            "   WHEN 'subject' = :sort1 THEN LOWER(message.subject)" +
            "   WHEN 'size' = :sort1 THEN -SUM(message.total)" +
            "   WHEN 'attachments' = :sort1 THEN -SUM(message.attachments)" +
            "   WHEN 'snoozed' = :sort1 THEN SUM(CASE WHEN message.ui_snoozed IS NULL THEN 0 ELSE 1 END) = 0" +
            "   WHEN 'touched' = :sort1 THEN IFNULL(-message.last_touched, 0)" +
            "   ELSE 0" +
            "  END" +
            ", CASE" +
            "   WHEN 'unread' = :sort2 THEN SUM(1 - message.ui_seen) = 0" +
            "   WHEN 'starred' = :sort2 THEN COUNT(message.id) - SUM(1 - message.ui_flagged) = 0" +
            "   ELSE 0" +
            "  END" +
            ", CASE WHEN :ascending THEN message.received ELSE -message.received END")
    DataSource.Factory<Integer, TupleMessageEx> pagedUnifiedJson(
            String type, String category,
            boolean threading, boolean group_category,
            String sort1, String sort2, boolean ascending,
            boolean filter_seen, boolean filter_unflagged, boolean filter_unknown, boolean filter_snoozed, boolean filter_deleted, String filter_language,
            boolean found,
            boolean debug);

    @Transaction
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT message.*" +
            ", account.pop AS accountProtocol, account.name AS accountName, account.category AS accountCategory, COALESCE(identity.color, folder.color, account.color) AS accountColor" +
            ", account.notify AS accountNotify, account.summary AS accountSummary, account.leave_on_server AS accountLeaveOnServer, account.leave_deleted AS accountLeaveDeleted, account.auto_seen AS accountAutoSeen" +
            ", folder.name AS folderName, folder.color AS folderColor, folder.display AS folderDisplay, folder.type AS folderType, f.inherited_type AS folderInheritedType, folder.unified AS folderUnified, folder.read_only AS folderReadOnly" +
            ", IFNULL(identity.display, identity.name) AS identityName, identity.email AS identityEmail, identity.color AS identityColor, identity.synchronize AS identitySynchronize" +
            ", '[' || substr(group_concat(message.`from`, ','), 0, 2048) || ']' AS senders" +
            ", '[' || substr(group_concat(message.`to`, ','), 0, 2048) || ']' AS recipients" +
            ", COUNT(message.id) AS count" +
            ", SUM(1 - message.ui_seen) AS unseen" +
            ", SUM(1 - message.ui_flagged) AS unflagged" +
            ", SUM(folder.type = '" + EntityFolder.DRAFTS + "') AS drafts" +
            ", COUNT(DISTINCT" +
            "   CASE WHEN NOT message.hash IS NULL THEN message.hash" +
            "   WHEN NOT message.msgid IS NULL THEN message.msgid" +
            "   ELSE message.id END) AS visible" +
            ", COUNT(DISTINCT" +
            "   CASE WHEN message.ui_seen THEN NULL" +
            "   WHEN NOT message.hash IS NULL THEN message.hash" +
            "   WHEN NOT message.msgid IS NULL THEN message.msgid" +
            "   ELSE message.id END) AS visible_unseen" +
            ", SUM(message.attachments) AS totalAttachments" +
            ", SUM(message.total) AS totalSize" +
            ", message.priority AS ui_priority" +
            ", message.importance AS ui_importance" +
            ", MAX(CASE WHEN" +
            "   (:found AND folder.type <> '" + EntityFolder.ARCHIVE + "')" +
            "   OR (NOT :found AND folder.id = :folder)" +
            "   THEN message.received ELSE 0 END) AS dummy" +
            " FROM message" +
            " JOIN account_view AS account ON account.id = message.account" +
            " LEFT JOIN identity_view AS identity ON identity.id = message.identity" +
            " JOIN folder_view AS folder ON folder.id = message.folder" +
            " JOIN folder_view AS f ON f.id = :folder" +
            " WHERE (message.account = f.account OR message.account = identity.account OR " + is_outbox + ")" +
            " AND (:threading OR folder.id = :folder)" +
            " AND (NOT message.ui_hide OR :debug)" +
            " AND (NOT :found OR message.ui_found = :found)" +
            " GROUP BY CASE WHEN message.thread IS NULL OR NOT :threading THEN message.id ELSE message.thread END" +
            " HAVING (SUM((:found AND message.ui_found)" +
            " OR (NOT :found AND message.folder = :folder)) > 0)" +
            " AND SUM(NOT message.ui_hide OR :debug) > 0" +
            " AND (NOT (:filter_seen AND NOT :filter_unflagged) OR SUM(1 - message.ui_seen) > 0 OR " + is_outbox + ")" +
            " AND (NOT (:filter_unflagged AND NOT :filter_seen) OR COUNT(message.id) - SUM(1 - message.ui_flagged) > 0 OR " + is_outbox + ")" +
            " AND (NOT (:filter_seen AND :filter_unflagged) OR SUM(1 - message.ui_seen) > 0 OR COUNT(message.id) - SUM(1 - message.ui_flagged) > 0 OR " + is_outbox + ")" +
            " AND (NOT :filter_unknown OR SUM(message.avatar IS NOT NULL AND message.sender <> identity.email) > 0" +
            "   OR " + is_outbox + " OR " + is_drafts + " OR " + is_sent + ")" +
            " AND (NOT :filter_snoozed OR message.ui_snoozed IS NULL OR " + is_outbox + " OR " + is_drafts + ")" +
            " AND (NOT :filter_deleted OR NOT message.ui_deleted)" +
            " AND (:filter_language IS NULL OR SUM(message.language = :filter_language) > 0 OR " + is_outbox + ")" +
            " ORDER BY CASE WHEN :found THEN 0 ELSE -IFNULL(message.importance, 1) END" +
            ", CASE" +
            "   WHEN 'unread' = :sort1 THEN SUM(1 - message.ui_seen) = 0" +
            "   WHEN 'starred' = :sort1 THEN COUNT(message.id) - SUM(1 - message.ui_flagged) = 0" +
            "   WHEN 'priority' = :sort1 THEN -IFNULL(message.priority, 1)" +
            "   WHEN 'sender' = :sort1 THEN LOWER(message.sender)" +
            "   WHEN 'sender_name' = :sort1 THEN LOWER(COALESCE(json_extract(message.`from`, '$[0].personal'), json_extract(message.`from`, '$[0].address')))" +
            "   WHEN 'subject' = :sort1 THEN LOWER(message.subject)" +
            "   WHEN 'size' = :sort1 THEN -SUM(message.total)" +
            "   WHEN 'attachments' = :sort1 THEN -SUM(message.attachments)" +
            "   WHEN 'snoozed' = :sort1 THEN SUM(CASE WHEN message.ui_snoozed IS NULL THEN 0 ELSE 1 END) = 0" +
            "   WHEN 'touched' = :sort1 THEN IFNULL(-message.last_touched, 0)" +
            "   ELSE 0" +
            "  END" +
            ", CASE" +
            "   WHEN 'unread' = :sort2 THEN SUM(1 - message.ui_seen) = 0" +
            "   WHEN 'starred' = :sort2 THEN COUNT(message.id) - SUM(1 - message.ui_flagged) = 0" +
            "   ELSE 0" +
            "  END" +
            ", CASE WHEN :ascending THEN message.received ELSE -message.received END")
    DataSource.Factory<Integer, TupleMessageEx> pagedFolderJson(
            long folder, boolean threading,
            String sort1, String sort2, boolean ascending,
            boolean filter_seen, boolean filter_unflagged, boolean filter_unknown, boolean filter_snoozed, boolean filter_deleted, String filter_language,
            boolean found,
            boolean debug);
}