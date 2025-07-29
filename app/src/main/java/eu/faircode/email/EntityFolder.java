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

import static androidx.room.ForeignKey.CASCADE;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.Collator;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

@Entity(
        tableName = EntityFolder.TABLE_NAME,
        foreignKeys = {
                @ForeignKey(childColumns = "account", entity = EntityAccount.class, parentColumns = "id", onDelete = CASCADE)
        },
        indices = {
                @Index(value = {"account", "name"}, unique = true),
                @Index(value = {"account"}),
                @Index(value = {"name"}),
                @Index(value = {"type"}),
                @Index(value = {"unified"})
        }
)

public class EntityFolder extends EntityOrder implements Serializable {
    static final String TABLE_NAME = "folder";

    @PrimaryKey(autoGenerate = true)
    public Long id;
    public Long account; // Outbox = null
    public Long parent;
    public Long uidv; // UIDValidity
    public Long modseq;
    public String namespace;
    public Character separator;
    @NonNull
    public String name;
    @NonNull
    public String type;
    public String inherited_type;
    public String subtype;
    @NonNull
    public Integer level = 0; // obsolete
    @NonNull
    public Boolean local = false;
    @NonNull
    public Boolean synchronize;
    @NonNull
    public Boolean poll = false;
    @NonNull
    public Integer poll_factor = 1;
    @NonNull
    public Integer poll_count = 0;
    @NonNull
    public Boolean download = true;
    @NonNull
    public Boolean auto_classify = false; // Obsolete
    @NonNull
    public Boolean auto_classify_source = false;
    @NonNull
    public Boolean auto_classify_target = false;
    public Boolean subscribed;
    @NonNull
    public Integer sync_days;
    @NonNull
    public Integer keep_days;
    @NonNull
    public Boolean auto_delete = false;
    public Boolean auto_add; // sent messages
    public String display;
    public Integer color;
    @NonNull
    public Boolean hide = false;
    @NonNull
    public Boolean hide_seen = false;
    @NonNull
    public Boolean collapsed = false;
    @NonNull
    public Boolean unified = false;
    @NonNull
    public Boolean navigation = false;
    @NonNull
    public Boolean count_unread = true;
    @NonNull
    public Boolean notify = false;

    public Integer total; // messages on server
    public String[] flags; // system flags
    public String[] keywords; // user flags

    @NonNull
    public Long selected_last = 0L;
    @NonNull
    public Integer selected_count = 0;

    @NonNull
    public Integer initialize = DEFAULT_KEEP;
    public Boolean tbc; // to be created
    public Boolean tbd; // to be deleted
    public String rename;
    public String state;
    public String sync_state;
    @NonNull
    public Boolean read_only = false;
    @NonNull
    public Boolean selectable = true; // Can contain messages
    @NonNull
    public Boolean inferiors = true; // Can have child folders
    public String error;
    public Long last_sync;
    public Long last_sync_foreground;
    public Integer last_sync_count; // POP3
    public Long last_view;

    static final String INBOX = "Inbox";
    static final String OUTBOX = "Outbox";
    static final String ARCHIVE = "All";
    static final String DRAFTS = "Drafts";
    static final String TRASH = "Trash";
    static final String JUNK = "Junk";
    static final String SENT = "Sent";
    static final String SYSTEM = "System";
    static final String USER = "User";

    static final String FLAGGED = "flagged";

    // https://tools.ietf.org/html/rfc6154
    // https://www.iana.org/assignments/imap-mailbox-name-attributes/imap-mailbox-name-attributes.xhtml
    private static final List<String> SYSTEM_FOLDER_ATTR = Collections.unmodifiableList(Arrays.asList(
            "all",
            "archive",
            "drafts",
            "trash",
            "junk",
            "sent",
            "important",
            "flagged"
    ));
    private static final List<String> SYSTEM_FOLDER_TYPE = Collections.unmodifiableList(Arrays.asList(
            ARCHIVE, // All
            ARCHIVE,
            DRAFTS,
            TRASH,
            JUNK,
            SENT,
            SYSTEM, // Important
            SYSTEM // Flagged
    )); // MUST match SYSTEM_FOLDER_ATTR

    static final List<String> FOLDER_SORT_ORDER = Collections.unmodifiableList(Arrays.asList(
            INBOX,
            OUTBOX,
            DRAFTS,
            SENT,
            TRASH,
            JUNK,
            SYSTEM,
            USER,
            ARCHIVE
    ));

    private static Map<String, TypeScore> GUESS_FOLDER_TYPE = new HashMap<String, TypeScore>() {{
        // Contains:
        //put("all", new TypeScore(EntityFolder.ARCHIVE, 50));
        //put("Tous", new TypeScore(EntityFolder.ARCHIVE, 50));
        put("Archiv", new TypeScore(EntityFolder.ARCHIVE, 100)); // German
        put("archive", new TypeScore(EntityFolder.ARCHIVE, 100));
        put("archief", new TypeScore(EntityFolder.ARCHIVE, 100)); // Dutch
        put("Архив", new TypeScore(EntityFolder.ARCHIVE, 100));
        put("Wszystkie", new TypeScore(EntityFolder.ARCHIVE, 100)); // Polish
        put("Arkiv", new TypeScore(EntityFolder.ARCHIVE, 100)); // Norwegian

        put("draft", new TypeScore(EntityFolder.DRAFTS, 100));
        put("concept", new TypeScore(EntityFolder.DRAFTS, 100));
        put("Entwurf", new TypeScore(EntityFolder.DRAFTS, 100));
        put("brouillon", new TypeScore(EntityFolder.DRAFTS, 100));
        put("Черновики", new TypeScore(EntityFolder.DRAFTS, 100));
        put("Bozze", new TypeScore(EntityFolder.DRAFTS, 100));
        put("Szkice lokalne", new TypeScore(EntityFolder.DRAFTS, 100)); // Polish
        put("Wersje robocze", new TypeScore(EntityFolder.DRAFTS, 100)); // Polish

        put("trash", new TypeScore(EntityFolder.TRASH, 100));
        put("Deleted", new TypeScore(EntityFolder.TRASH, 100));
        //put("Bin", new TypeScore(EntityFolder.TRASH, 50));
        put("Papierkorb", new TypeScore(EntityFolder.TRASH, 100));
        put("corbeille", new TypeScore(EntityFolder.TRASH, 100));
        put("Корзина", new TypeScore(EntityFolder.TRASH, 100));
        put("Удаленные", new TypeScore(EntityFolder.TRASH, 50));
        put("Eliminata", new TypeScore(EntityFolder.TRASH, 100));
        put("Kosz", new TypeScore(EntityFolder.TRASH, 100)); // Polish
        put("supprimé", new TypeScore(EntityFolder.TRASH, 100));

        put("junk", new TypeScore(EntityFolder.JUNK, 100));
        put("spam", new TypeScore(EntityFolder.JUNK, 100));
        put("bulk", new TypeScore(EntityFolder.JUNK, 100));
        put("pourriel", new TypeScore(EntityFolder.JUNK, 100));
        put("quarantaine", new TypeScore(EntityFolder.JUNK, 50));
        put("Спам", new TypeScore(EntityFolder.JUNK, 100));
        put("Cestino", new TypeScore(EntityFolder.JUNK, 100));
        put("Indesiderata", new TypeScore(EntityFolder.JUNK, 100));
        put("indésirable", new TypeScore(EntityFolder.JUNK, 100));
        put("Wiadomości-śmieci", new TypeScore(EntityFolder.JUNK, 100)); // Polish

        put("sent", new TypeScore(EntityFolder.SENT, 100));
        put("Gesendet", new TypeScore(EntityFolder.SENT, 100));
        put("envoyé", new TypeScore(EntityFolder.SENT, 100));
        put("OUTBOX", new TypeScore(EntityFolder.SENT, 100, "imap.laposte.net"));
        put("Отправленные", new TypeScore(EntityFolder.SENT, 100));
        put("Inviata", new TypeScore(EntityFolder.SENT, 100));
        put("wysłane", new TypeScore(EntityFolder.SENT, 100)); // Polish
    }};

    static final int DEFAULT_SYNC = 7; // days
    static final int DEFAULT_KEEP = 30; // days
    static final int DEFAULT_KEEP_DRAFTS = 365; // days

    private static final List<String> SYSTEM_FOLDER_SYNC = Collections.unmodifiableList(Arrays.asList(
            INBOX,
            DRAFTS,
            SENT,
            ARCHIVE,
            TRASH,
            JUNK
    ));
    private static final List<Boolean> SYSTEM_FOLDER_POLL = Collections.unmodifiableList(Arrays.asList(
            false, // inbox = push messages
            true, // drafts
            true, // sent
            true, // archive
            true, // trash
            true // junk
    )); // MUST match SYSTEM_FOLDER_SYNC
    private static final List<Boolean> SYSTEM_FOLDER_DOWNLOAD = Collections.unmodifiableList(Arrays.asList(
            true, // inbox
            true, // drafts
            true, // sent
            true, // archive
            false, // trash
            false // junk
    )); // MUST match SYSTEM_FOLDER_SYNC

    public EntityFolder() {
    }

    public EntityFolder(String fullName, String type) {
        this.name = fullName;
        this.type = type;
        setProperties();
    }

    void setProperties() {
        int sync = EntityFolder.SYSTEM_FOLDER_SYNC.indexOf(type);
        this.synchronize = (sync >= 0);
        this.poll = (sync < 0 || EntityFolder.SYSTEM_FOLDER_POLL.get(sync));
        this.download = (sync < 0 || EntityFolder.SYSTEM_FOLDER_DOWNLOAD.get(sync));

        this.sync_days = EntityFolder.DEFAULT_SYNC;
        this.keep_days = EntityFolder.DEFAULT_KEEP;

        if (EntityFolder.INBOX.equals(type)) {
            this.unified = true;
            this.notify = true;
            this.auto_classify_source = true;
        }

        if (EntityFolder.DRAFTS.equals(type)) {
            this.initialize = EntityFolder.DEFAULT_KEEP_DRAFTS;
            this.keep_days = EntityFolder.DEFAULT_KEEP_DRAFTS;
        }

        if (EntityFolder.JUNK.equals(type))
            this.auto_classify_source = true;
    }

    void setSpecials(EntityAccount account) {
        if ("imap.web.de".equals(account.host) && "Unbekannt".equals(name)) {
            // In den Ordner Unbekannt werden E-Mails einsortiert,
            // die nicht als Spam erkannt werden
            // und deren Absender nicht in Ihrem Adressbuch oder auf Ihrer Erwünschtliste stehen.
            synchronize = true;
            unified = true;
            notify = true;
        }

        if ("poczta.o2.pl".equals(account.host) && INBOX.equals(name))
            poll = true;

        if ("imap.laposte.net".equals(account.host) && "INBOX/OUTBOX".equals(name))
            display = "Envoyés";
    }

    void inheritFrom(EntityFolder parent) {
        if (parent == null)
            return;
        if (!EntityFolder.USER.equals(type))
            return;
        if (!EntityFolder.USER.equals(parent.type))
            return;

        this.synchronize = parent.synchronize;
        this.download = parent.download;
        this.sync_days = parent.sync_days;
        this.keep_days = parent.keep_days;
        this.notify = parent.notify;
    }

    static boolean shouldPoll(String type) {
        int sync = EntityFolder.SYSTEM_FOLDER_SYNC.indexOf(type);
        return (sync < 0 || EntityFolder.SYSTEM_FOLDER_POLL.get(sync));
    }

    static List<EntityFolder> getPopFolders(Context context) {
        List<EntityFolder> result = new ArrayList<>();

        EntityFolder inbox = new EntityFolder();
        inbox.name = "INBOX";
        inbox.type = EntityFolder.INBOX;
        inbox.synchronize = true;
        inbox.unified = true;
        inbox.notify = true;
        inbox.sync_days = Integer.MAX_VALUE;
        inbox.keep_days = Integer.MAX_VALUE;
        inbox.initialize = 0;
        result.add(inbox);

        EntityFolder drafts = new EntityFolder();
        drafts.name = context.getString(R.string.title_folder_drafts);
        drafts.type = EntityFolder.DRAFTS;
        drafts.synchronize = false;
        drafts.unified = false;
        drafts.notify = false;
        drafts.sync_days = Integer.MAX_VALUE;
        drafts.keep_days = Integer.MAX_VALUE;
        drafts.initialize = 0;
        result.add(drafts);

        EntityFolder sent = new EntityFolder();
        sent.name = context.getString(R.string.title_folder_sent);
        sent.type = EntityFolder.SENT;
        sent.synchronize = false;
        sent.unified = false;
        sent.notify = false;
        sent.sync_days = Integer.MAX_VALUE;
        sent.keep_days = Integer.MAX_VALUE;
        sent.initialize = 0;
        result.add(sent);

        EntityFolder trash = new EntityFolder();
        trash.name = context.getString(R.string.title_folder_trash);
        trash.type = EntityFolder.TRASH;
        trash.synchronize = false;
        trash.unified = false;
        trash.notify = false;
        trash.sync_days = Integer.MAX_VALUE;
        trash.keep_days = Integer.MAX_VALUE;
        trash.initialize = 0;
        result.add(trash);

        return result;
    }

    @NonNull
    static EntityFolder getOutbox(Context context) {
        DB db = DB.getInstance(context);
        EntityFolder outbox = db.folder().getOutbox();
        if (outbox != null)
            return outbox;

        Log.w("Outbox missing");

        outbox = new EntityFolder();
        outbox.name = "OUTBOX";
        outbox.type = EntityFolder.OUTBOX;
        outbox.synchronize = false;
        outbox.sync_days = 0;
        outbox.keep_days = 0;
        outbox.id = db.folder().insertFolder(outbox);

        return outbox;
    }

    static List<EntityFolder> getChildFolders(Context context, long id) {
        DB db = DB.getInstance(context);
        List<EntityFolder> children = db.folder().getChildFolders(id);
        if (children == null)
            children = new ArrayList<>();
        for (EntityFolder child : new ArrayList<>(children))
            children.addAll(getChildFolders(context, child.id));
        return children;
    }

    static String getNotificationChannelId(long id) {
        return "notification.folder." + id;
    }

    JSONArray getSyncArgs(boolean force) {
        int days = sync_days;
        if (last_sync != null) {
            int ago_days = (int) ((new Date().getTime() - last_sync) / (24 * 3600 * 1000L)) + 1;
            if (ago_days > days)
                days = ago_days;
        }

        JSONArray jargs = new JSONArray();
        jargs.put(initialize == 0 ? Math.min(days, keep_days) : Math.min(keep_days, initialize));
        jargs.put(keep_days);
        jargs.put(download);
        jargs.put(auto_delete);
        jargs.put(initialize);
        jargs.put(force);

        return jargs;
    }

    static boolean isSyncForced(String args) throws JSONException {
        JSONArray jargs = new JSONArray(args);
        return jargs.optBoolean(5, false);
    }

    static int getIcon(String type) {
        if (EntityFolder.INBOX.equals(type))
            return R.drawable.twotone_inbox_24;
        if (EntityFolder.OUTBOX.equals(type))
            return R.drawable.twotone_outbox_24;
        if (EntityFolder.DRAFTS.equals(type))
            return R.drawable.twotone_drafts_24;
        if (EntityFolder.SENT.equals(type))
            return R.drawable.twotone_send_24;
        if (EntityFolder.ARCHIVE.equals(type))
            return R.drawable.twotone_archive_24;
        if (EntityFolder.TRASH.equals(type))
            return R.drawable.twotone_delete_24;
        if (EntityFolder.JUNK.equals(type))
            return R.drawable.twotone_report_24;
        if (EntityFolder.SYSTEM.equals(type))
            return R.drawable.twotone_folder_special_24;
        return R.drawable.twotone_folder_24;
    }

    static Integer getDefaultColor(String type, Context context) {
        if (EntityFolder.TRASH.equals(type) || EntityFolder.JUNK.equals(type))
            return Helper.resolveColor(context, androidx.appcompat.R.attr.colorError);
        return null;
    }

    static Integer getDefaultColor(Long action, String type, Context context) {
        if (EntityMessage.SWIPE_ACTION_DELETE.equals(action))
            return Helper.resolveColor(context, androidx.appcompat.R.attr.colorError);
        return getDefaultColor(type, context);
    }

    String getDisplayName(Context context) {
        return (display == null ? localizeName(context, name) : display);
    }

    String getDisplayName(Context context, EntityFolder parent) {
        String n = name;
        if (parent != null &&
                n.startsWith(parent.name) &&
                n.length() > parent.name.length() + 1)
            n = n.substring(parent.name.length() + 1);
        return (display == null ? localizeName(context, n) : display);
    }

    @Override
    Long getSortId() {
        return id;
    }

    @Override
    String[] getSortTitle(Context context) {
        return new String[]{getDisplayName(context), null};
    }

    boolean isOutgoing() {
        return isOutgoing(this.type);
    }

    static boolean isOutgoing(String type) {
        return DRAFTS.equals(type) || OUTBOX.equals(type) || SENT.equals(type);
    }

    static String getType(String[] attrs, String fullName, boolean selectable) {
        // https://tools.ietf.org/html/rfc3501#section-5.1
        if ("INBOX".equals(fullName.toUpperCase(Locale.ROOT)))
            return INBOX;

        // https://www.iana.org/assignments/imap-mailbox-name-attributes/imap-mailbox-name-attributes.xhtml
        for (String attr : attrs) {
            if ((selectable && "\\NoSelect".equalsIgnoreCase(attr)) || "\\NonExistent".equalsIgnoreCase(attr))
                return null;

            if (attr.startsWith("\\")) {
                int index = SYSTEM_FOLDER_ATTR.indexOf(attr.substring(1).toLowerCase(Locale.ROOT));
                if (index >= 0)
                    return SYSTEM_FOLDER_TYPE.get(index);
            }
        }

        return USER;
    }

    static String getSubtype(String[] attrs, String fullname) {
        for (String attr : attrs)
            if ("\\Flagged".equals(attr)) // Gmail
                return FLAGGED;
        return null;
    }

    private static class TypeScore {
        @NonNull
        private String type;
        private int score;
        private String host;

        TypeScore(@NonNull String type, int score) {
            this.score = score;
            this.type = type;
        }

        TypeScore(@NonNull String type, int score, String host) {
            this.score = score;
            this.type = type;
            this.host = host;
        }
    }

    private static class FolderScore {
        @NonNull
        private EntityFolder folder;
        private int score;

        FolderScore(@NonNull EntityFolder folder, int score) {
            this.score = score;
            this.folder = folder;
        }

        @NonNull
        @Override
        public String toString() {
            return folder.name + ":" + score;
        }
    }

    static void guessTypes(List<EntityFolder> folders, String host) {
        List<String> types = new ArrayList<>();
        Map<String, List<FolderScore>> typeFolderScore = new HashMap<>();

        for (EntityFolder folder : folders)
            if (EntityFolder.USER.equals(folder.type)) {
                for (String guess : GUESS_FOLDER_TYPE.keySet())
                    if (folder.name.toLowerCase(Locale.ROOT).contains(guess.toLowerCase(Locale.ROOT))) {
                        TypeScore score = GUESS_FOLDER_TYPE.get(guess);
                        if (score.host != null && !score.host.equals(host))
                            continue;
                        if (!typeFolderScore.containsKey(score.type))
                            typeFolderScore.put(score.type, new ArrayList<>());
                        typeFolderScore.get(score.type).add(new FolderScore(folder, score.score));
                        break;
                    }
            } else
                types.add(folder.type);

        for (String type : typeFolderScore.keySet()) {
            List<FolderScore> candidates = typeFolderScore.get(type);
            Log.i("Guess type=" + type + " candidates=" + TextUtils.join(", ", candidates));
            if (!types.contains(type)) {
                Collections.sort(candidates, new Comparator<FolderScore>() {
                    @Override
                    public int compare(FolderScore fs1, FolderScore fs2) {
                        int r = Boolean.compare(fs1.folder.read_only, fs2.folder.read_only);
                        if (r != 0)
                            return r;

                        int s = Integer.compare(fs1.score, fs2.score);
                        if (s == 0) {
                            int len1 = (fs1.folder.separator == null ? 1
                                    : fs1.folder.name.split(Pattern.quote(String.valueOf(fs1.folder.separator))).length);
                            int len2 = (fs2.folder.separator == null ? 1
                                    : fs2.folder.name.split(Pattern.quote(String.valueOf(fs2.folder.separator))).length);
                            return Integer.compare(len1, len2);
                        } else
                            return s;
                    }
                });

                candidates.get(0).folder.type = type;
                candidates.get(0).folder.setProperties();
                Log.i(candidates.get(0).folder.name + " guessed type=" + type);
                types.add(type);
            }
        }
    }

    String getParentName() {
        if (separator == null)
            return null;
        else {
            int p = name.lastIndexOf(separator);
            if (p < 0)
                return null;
            else
                return name.substring(0, p);
        }
    }

    static String localizeType(Context context, String type) {
        int resid = context.getResources().getIdentifier(
                "title_folder_" + type.toLowerCase(Locale.ROOT),
                "string",
                context.getPackageName());
        return (resid > 0 ? context.getString(resid) : type);
    }

    static String localizeName(Context context, String name) {
        if (name != null && "INBOX".equals(name.toUpperCase(Locale.ROOT)))
            return context.getString(R.string.title_folder_inbox);
        else if ("OUTBOX".equals(name))
            return context.getString(R.string.title_folder_outbox);
        else
            return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EntityFolder) {
            EntityFolder other = (EntityFolder) obj;
            return (this.id.equals(other.id) &&
                    Objects.equals(this.account, other.account) &&
                    Objects.equals(this.parent, other.parent) &&
                    Objects.equals(this.uidv, other.uidv) &&
                    Objects.equals(this.namespace, other.namespace) &&
                    Objects.equals(this.separator, other.separator) &&
                    this.name.equals(other.name) &&
                    this.type.equals(other.type) &&
                    this.level.equals(other.level) &&
                    this.synchronize.equals(other.synchronize) &&
                    this.poll.equals(other.poll) &&
                    this.download.equals(other.download) &&
                    this.auto_classify_source.equals(other.auto_classify_source) &&
                    this.auto_classify_target.equals(other.auto_classify_target) &&
                    Objects.equals(this.subscribed, other.subscribed) &&
                    this.sync_days.equals(other.sync_days) &&
                    this.keep_days.equals(other.keep_days) &&
                    this.auto_delete.equals(other.auto_delete) &&
                    Objects.equals(this.display, other.display) &&
                    Objects.equals(this.color, other.color) &&
                    Objects.equals(this.order, other.order) &&
                    this.hide == other.hide &&
                    this.hide_seen == other.hide_seen &&
                    this.collapsed == other.collapsed &&
                    this.unified == other.unified &&
                    this.navigation == other.navigation &&
                    this.count_unread == other.count_unread &&
                    this.notify == other.notify &&
                    Objects.equals(this.total, other.total) &&
                    Helper.equal(this.keywords, other.keywords) &&
                    this.initialize.equals(other.initialize) &&
                    Objects.equals(this.tbc, other.tbc) &&
                    Objects.equals(this.tbd, other.tbd) &&
                    Objects.equals(this.rename, other.rename) &&
                    Objects.equals(this.state, other.state) &&
                    Objects.equals(this.sync_state, other.sync_state) &&
                    this.read_only == other.read_only &&
                    this.selectable == other.selectable &&
                    this.inferiors == other.inferiors &&
                    Objects.equals(this.error, other.error) &&
                    Objects.equals(this.last_sync, other.last_sync));
        } else
            return false;
    }

    @Override
    public String toString() {
        return name;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("order", order);
        json.put("namespace", namespace);
        if (separator != null)
            json.put("separator", (int) separator);
        json.put("name", name);
        json.put("type", type);
        json.put("synchronize", synchronize);
        json.put("poll", poll);
        json.put("poll_factor", poll_factor);
        json.put("download", download);
        json.put("auto_classify_source", auto_classify_source);
        json.put("auto_classify_target", auto_classify_target);
        json.put("sync_days", sync_days);
        json.put("keep_days", keep_days);
        json.put("auto_delete", auto_delete);
        json.put("display", display);
        json.put("color", color);
        json.put("hide", hide);
        json.put("hide_seen", hide_seen);
        json.put("collapsed", collapsed);
        json.put("unified", unified);
        json.put("navigation", navigation);
        json.put("count_unread", count_unread);
        json.put("notify", notify);
        return json;
    }

    public static EntityFolder fromJSON(JSONObject json) throws JSONException {
        EntityFolder folder = new EntityFolder();
        if (json.has("id"))
            folder.id = json.getLong("id");

        if (json.has("order"))
            folder.order = json.getInt("order");

        if (json.has("namespace"))
            folder.namespace = json.getString("namespace");
        if (json.has("separator"))
            folder.separator = (char) json.getInt("separator");

        folder.name = json.getString("name");
        folder.type = json.getString("type");

        folder.synchronize = json.getBoolean("synchronize");

        if (json.has("poll"))
            folder.poll = json.getBoolean("poll");
        if (json.has("poll_factor"))
            folder.poll_factor = json.getInt("poll_factor");

        if (json.has("download"))
            folder.download = json.getBoolean("download");

        if (json.has("auto_classify_source"))
            folder.auto_classify_source = json.getBoolean("auto_classify_source");
        else
            folder.auto_classify_source =
                    (EntityFolder.INBOX.equals(folder.type) ||
                            EntityFolder.JUNK.equals(folder.type));
        if (json.has("auto_classify_target"))
            folder.auto_classify_target = json.getBoolean("auto_classify_target");

        if (json.has("after"))
            folder.sync_days = json.getInt("after");
        else
            folder.sync_days = json.getInt("sync_days");

        if (json.has("keep_days"))
            folder.keep_days = json.getInt("keep_days");
        else
            folder.keep_days = folder.sync_days;

        if (json.has("auto_delete"))
            folder.auto_delete = json.getBoolean("auto_delete");

        if (json.has("display") && !json.isNull("display"))
            folder.display = json.getString("display");

        if (json.has("color") && !json.isNull("color"))
            folder.color = json.getInt("color");

        if (json.has("hide"))
            folder.hide = json.getBoolean("hide");

        if (json.has("hide_seen"))
            folder.hide_seen = json.getBoolean("hide_seen");

        if (json.has("collapsed"))
            folder.collapsed = json.getBoolean("collapsed");

        folder.unified = json.getBoolean("unified");

        if (json.has("navigation"))
            folder.navigation = json.getBoolean("navigation");

        if (json.has("count_unread"))
            folder.count_unread = json.getBoolean("count_unread");

        if (json.has("notify"))
            folder.notify = json.getBoolean("notify");

        return folder;
    }

    @Override
    Comparator getComparator(final Context context) {
        final Collator collator = Collator.getInstance(Locale.getDefault());
        collator.setStrength(Collator.SECONDARY); // Case insensitive, process accents etc

        return new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                EntityFolder f1 = (EntityFolder) o1;
                EntityFolder f2 = (EntityFolder) o2;

                int o = Integer.compare(
                        f1.order == null ? -1 : f1.order,
                        f2.order == null ? -1 : f2.order);
                if (o != 0)
                    return o;

                int i1 = EntityFolder.FOLDER_SORT_ORDER.indexOf(f1.type);
                int i2 = EntityFolder.FOLDER_SORT_ORDER.indexOf(f2.type);
                int s = Integer.compare(i1, i2);
                if (s != 0)
                    return s;

                int c = -f1.synchronize.compareTo(f2.synchronize);
                if (c != 0)
                    return c;

                String name1 = (context == null ? f1.name : f1.getDisplayName(context));
                String name2 = (context == null ? f2.name : f2.getDisplayName(context));
                return Normalizer.normalize(name1, Normalizer.Form.NFD).compareToIgnoreCase(
                        Normalizer.normalize(name2, Normalizer.Form.NFD));
            }
        };
    }
}
