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

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.preference.PreferenceManager;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Entity(
        tableName = EntityAccount.TABLE_NAME,
        indices = {
                @Index(value = {"synchronize"}),
                @Index(value = {"category"})
        }
)
public class EntityAccount extends EntityOrder implements Serializable {
    static final String TABLE_NAME = "account";

    // https://tools.ietf.org/html/rfc2177
    static final int DEFAULT_KEEP_ALIVE_INTERVAL = 15; // minutes
    static final int DEFAULT_POLL_INTERVAL = 15; // minutes

    static final int QUOTA_WARNING = 95; // percent

    static final int TYPE_IMAP = 0;
    static final int TYPE_POP = 1;

    @PrimaryKey(autoGenerate = true)
    public Long id;

    @NonNull
    public String uuid = UUID.randomUUID().toString();

    @NonNull
    @ColumnInfo(name = "pop")
    public Integer protocol = TYPE_IMAP;
    @NonNull
    public Boolean dnssec = false;
    @NonNull
    public String host; // POP3/IMAP
    @NonNull
    @ColumnInfo(name = "starttls")
    public Integer encryption;
    @NonNull
    public Boolean insecure = false;
    @NonNull
    public Boolean dane = false;
    @NonNull
    public Integer port;
    @NonNull
    public Integer auth_type;
    public String provider;
    @NonNull
    public String user;
    @NonNull
    public String password;
    @NonNull
    public Boolean certificate = false; // obsolete
    public String certificate_alias;
    public String realm;
    public String fingerprint;

    public String name;
    public String category;
    public String signature; // obsolete
    public Integer color;
    @ColumnInfo(name = "prefix")
    public String avatar;
    public String calendar;

    @NonNull
    public Boolean synchronize;
    @NonNull
    public Boolean ondemand = false;
    @NonNull
    public Boolean poll_exempted = false;
    @NonNull
    public Boolean primary;
    @NonNull
    public Boolean notify = false;
    @NonNull
    public Boolean summary = false;
    @NonNull
    public Boolean browse = true;
    @NonNull
    public Boolean leave_on_server = true;
    @NonNull
    public Boolean client_delete = false;
    @NonNull
    public Boolean leave_deleted = true;
    @NonNull
    public Boolean leave_on_device = true;
    public Integer max_messages = null; // POP3
    @NonNull
    public Boolean auto_seen = true;
    @ColumnInfo(name = "separator")
    public Character _separator; // obsolete
    public Long swipe_left;
    public Long swipe_right;
    public Long move_to;
    @NonNull
    public Integer poll_interval = DEFAULT_KEEP_ALIVE_INTERVAL;
    @NonNull
    public Boolean keep_alive_noop = false;
    @NonNull
    public Boolean keep_alive_ok = false;
    @NonNull
    public Integer keep_alive_failed = 0;
    @NonNull
    public Integer keep_alive_succeeded = 0;
    @NonNull
    public Boolean partial_fetch = true;
    @NonNull
    public Boolean raw_fetch = false;
    @NonNull
    public Boolean ignore_size = false;
    @NonNull
    public Boolean use_date = false; // Date header
    @NonNull
    public Boolean use_received = false; // Received header
    @NonNull
    public Boolean unicode = false;

    public String conditions;

    public Long quota_usage;
    public Long quota_limit;

    public Long created = 0L;
    public Boolean tbd;
    public Long thread;
    public String state;
    public String warning;
    public String error;
    public Long last_connected;
    public Long backoff_until;
    public Long max_size;
    public String capabilities;
    public Boolean capability_idle;
    public Boolean capability_utf8;
    public Boolean capability_uidl;
    public Long last_modified; // sync

    boolean isGmail() {
        return "imap.gmail.com".equalsIgnoreCase(host) ||
                "imap.googlemail.com".equalsIgnoreCase(host);
    }

    boolean isOutlook() {
        return ("outlook.office365.com".equalsIgnoreCase(host) ||
                "imap-mail.outlook.com".equalsIgnoreCase(host));
    }

    static boolean isOutlook(String id) {
        return ("office365".equals(id) || "office365pcke".equals(id) || "outlook".equals(id) || "outlookgraph".equals(id));
    }

    boolean isYahooJp() {
        return "imap.mail.yahoo.co.jp".equalsIgnoreCase(host);
    }

    boolean isSeznam() {
        return "imap.seznam.cz".equalsIgnoreCase(host);
    }

    boolean isZoho() {
        return (host != null && host.toLowerCase(Locale.ROOT).startsWith("imap.zoho."));
    }

    boolean isYahoo() {
        return "imap.mail.yahoo.com".equalsIgnoreCase(host);
    }

    boolean isAol() {
        return "imap.aol.com".equalsIgnoreCase(host);
    }

    boolean isWpPl() {
        return "imap.wp.pl".equalsIgnoreCase(host);
    }

    boolean isWebDe() {
        return "imap.web.de".equalsIgnoreCase(host);
    }

    boolean isICloud() {
        return "imap.mail.me.com".equalsIgnoreCase(host);
    }

    boolean isTransient(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean enabled = prefs.getBoolean("enabled", true);
        int pollInterval = ServiceSynchronize.getPollInterval(context);
        return (!enabled || this.ondemand || (pollInterval > 0 && !isExempted(context)));
    }

    boolean isExempted(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean poll_metered = prefs.getBoolean("poll_metered", false);
        boolean poll_unmetered = prefs.getBoolean("poll_unmetered", false);

        if (poll_metered || poll_unmetered) {
            ConnectionHelper.NetworkState state = ConnectionHelper.getNetworkState(context);
            if (poll_metered && state.isConnected() && !state.isUnmetered())
                return false;
            if (poll_unmetered && state.isConnected() && state.isUnmetered())
                return false;
        }

        return this.poll_exempted;
    }

    String getProtocol() {
        switch (protocol) {
            case TYPE_IMAP:
                if (isGmail())
                    return "gimaps";
                else
                    return "imap" + (encryption == EmailService.ENCRYPTION_SSL ? "s" : "");
            case TYPE_POP:
                return "pop3" + (encryption == EmailService.ENCRYPTION_SSL ? "s" : "");
            default:
                throw new IllegalArgumentException("Unknown protocol=" + protocol);
        }
    }

    static String getNotificationChannelId(long id) {
        return "notification" + (id == 0 ? "" : "." + id);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    void createNotificationChannel(Context context) {
        NotificationManager nm = Helper.getSystemService(context, NotificationManager.class);

        NotificationChannelGroup group = new NotificationChannelGroup("group." + id, name);
        nm.createNotificationChannelGroup(group);

        NotificationChannel channel = new NotificationChannel(
                getNotificationChannelId(id), name,
                NotificationManager.IMPORTANCE_HIGH);
        channel.setGroup(group.getId());
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        channel.setBypassDnd(true);
        channel.enableLights(true);
        nm.createNotificationChannel(channel);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    void deleteNotificationChannel(Context context) {
        NotificationManager nm = Helper.getSystemService(context, NotificationManager.class);
        nm.deleteNotificationChannel(getNotificationChannelId(id));
    }

    @Override
    Long getSortId() {
        return id;
    }

    Integer getQuotaPercentage() {
        if (quota_usage == null || quota_limit == null)
            return null;

        int percent = Math.round(quota_usage * 100f / quota_limit);
        return (percent > 100 ? null : percent);
    }

    @Override
    String[] getSortTitle(Context context) {
        return new String[]{name, null};
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("uuid", uuid);
        json.put("order", order);
        json.put("protocol", protocol);
        json.put("host", host);
        json.put("encryption", encryption);
        json.put("insecure", insecure);
        json.put("port", port);
        json.put("auth_type", auth_type);
        json.put("provider", provider);
        json.put("user", user);
        json.put("password", password);
        json.put("certificate_alias", certificate_alias);
        json.put("realm", realm);
        json.put("fingerprint", fingerprint);

        json.put("name", name);
        json.put("category", category);
        json.put("color", color);
        json.put("calendar", calendar);

        json.put("synchronize", synchronize);
        json.put("ondemand", ondemand);
        json.put("poll_exempted", poll_exempted);
        json.put("primary", primary);
        json.put("notify", notify);
        json.put("browse", browse);
        json.put("leave_on_server", leave_on_server);
        json.put("client_delete", client_delete);
        json.put("leave_deleted", leave_deleted);
        json.put("leave_on_device", leave_on_device);
        json.put("max_messages", max_messages);
        json.put("auto_seen", auto_seen);
        // not separator

        json.put("swipe_left", swipe_left);
        json.put("swipe_right", swipe_right);

        json.put("move_to", move_to);

        json.put("poll_interval", poll_interval);
        json.put("keep_alive_noop", keep_alive_noop);
        json.put("partial_fetch", partial_fetch);
        json.put("raw_fetch", raw_fetch);
        json.put("ignore_size", ignore_size);
        json.put("use_date", use_date);
        json.put("use_received", use_received);
        json.put("unicode", unicode);
        json.put("conditions", conditions);
        // not prefix
        // not created
        // not tbd
        // not state
        // not warning
        // not error
        // not last connected
        return json;
    }

    public static EntityAccount fromJSON(JSONObject json) throws JSONException {
        EntityAccount account = new EntityAccount();
        if (json.has("id"))
            account.id = json.getLong("id");

        if (json.has("uuid") && !json.isNull("uuid"))
            account.uuid = json.getString("uuid");

        if (json.has("order"))
            account.order = json.getInt("order");

        if (json.has("protocol"))
            account.protocol = json.getInt("protocol");
        else if (json.has("pop"))
            account.protocol = (json.getBoolean("pop") ? TYPE_POP : TYPE_IMAP);

        account.host = json.getString("host");
        if (json.has("starttls"))
            account.encryption = (json.getBoolean("starttls")
                    ? EmailService.ENCRYPTION_STARTTLS : EmailService.ENCRYPTION_SSL);
        else
            account.encryption = json.getInt("encryption");
        account.insecure = (json.has("insecure") && json.getBoolean("insecure"));
        account.port = json.getInt("port");
        account.auth_type = json.getInt("auth_type");
        if (json.has("provider") && !json.isNull("provider"))
            account.provider = json.getString("provider");
        account.user = json.getString("user");
        account.password = json.getString("password");
        if (json.has("certificate_alias") && !json.isNull("certificate_alias"))
            account.certificate_alias = json.getString("certificate_alias");
        if (json.has("realm") && !json.isNull("realm"))
            account.realm = json.getString("realm");
        if (json.has("fingerprint") && !json.isNull("fingerprint"))
            account.fingerprint = json.getString("fingerprint");

        if (json.has("name") && !json.isNull("name"))
            account.name = json.getString("name");
        if (json.has("category") && !json.isNull("category"))
            account.category = json.getString("category");
        if (json.has("color"))
            account.color = json.getInt("color");
        if (json.has("calendar") && !json.isNull("calendar"))
            account.calendar = json.getString("calendar");

        account.synchronize = json.getBoolean("synchronize");
        if (json.has("ondemand"))
            account.ondemand = json.getBoolean("ondemand");
        if (json.has("poll_exempted"))
            account.poll_exempted = json.getBoolean("poll_exempted");
        account.primary = json.getBoolean("primary");
        if (json.has("notify"))
            account.notify = json.getBoolean("notify");
        if (json.has("browse"))
            account.browse = json.getBoolean("browse");
        if (json.has("leave_on_server"))
            account.leave_on_server = json.getBoolean("leave_on_server");
        account.client_delete = json.optBoolean("client_delete", false);
        if (json.has("leave_deleted"))
            account.leave_deleted = json.getBoolean("leave_deleted");
        if (json.has("leave_on_device"))
            account.leave_on_device = json.getBoolean("leave_on_device");
        if (json.has("max_messages"))
            account.max_messages = json.getInt("max_messages");
        if (json.has("auto_seen"))
            account.auto_seen = json.getBoolean("auto_seen");

        if (json.has("swipe_left"))
            account.swipe_left = json.getLong("swipe_left");
        if (json.has("swipe_right"))
            account.swipe_right = json.getLong("swipe_right");

        if (json.has("move_to"))
            account.move_to = json.getLong("move_to");

        account.poll_interval = json.getInt("poll_interval");
        account.keep_alive_noop = json.optBoolean("keep_alive_noop");

        account.partial_fetch = json.optBoolean("partial_fetch", true);
        account.raw_fetch = json.optBoolean("raw_fetch", false);
        account.ignore_size = json.optBoolean("ignore_size", false);
        account.use_date = json.optBoolean("use_date", false);
        account.use_received = json.optBoolean("use_received", false);
        account.unicode = json.optBoolean("unicode", false);
        account.conditions = json.optString("conditions", null);

        return account;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EntityAccount) {
            EntityAccount other = (EntityAccount) obj;
            return areEqual(this, other, true, true);
        } else
            return false;
    }

    public static boolean areEqual(EntityAccount a1, EntityAccount other, boolean auth, boolean state) {
        return (Objects.equals(a1.order, other.order) &&
                Objects.equals(a1.uuid, other.uuid) &&
                a1.protocol.equals(other.protocol) &&
                a1.host.equals(other.host) &&
                a1.encryption.equals(other.encryption) &&
                a1.insecure == other.insecure &&
                a1.port.equals(other.port) &&
                a1.auth_type.equals(other.auth_type) &&
                Objects.equals(a1.provider, other.provider) &&
                a1.user.equals(other.user) &&
                (!auth || a1.password.equals(other.password)) &&
                // certificate
                Objects.equals(a1.certificate_alias, other.certificate_alias) &&
                Objects.equals(a1.realm, other.realm) &&
                Objects.equals(a1.fingerprint, other.fingerprint) &&
                Objects.equals(a1.name, other.name) &&
                Objects.equals(a1.category, other.category) &&
                // signature
                Objects.equals(a1.color, other.color) &&
                Objects.equals(a1.avatar, other.avatar) &&
                Objects.equals(a1.calendar, other.calendar) &&
                a1.synchronize.equals(other.synchronize) &&
                Objects.equals(a1.ondemand, other.ondemand) &&
                Objects.equals(a1.poll_exempted, other.poll_exempted) &&
                a1.primary.equals(other.primary) &&
                a1.notify.equals(other.notify) &&
                a1.browse.equals(other.browse) &&
                a1.leave_on_server.equals(other.leave_on_server) &&
                a1.client_delete.equals(other.client_delete) &&
                Objects.equals(a1.leave_deleted, other.leave_deleted) &&
                a1.leave_on_device.equals(other.leave_on_device) &&
                Objects.equals(a1.max_messages, other.max_messages) &&
                a1.auto_seen.equals(other.auto_seen) &&
                // separator
                Objects.equals(a1.swipe_left, other.swipe_left) &&
                Objects.equals(a1.swipe_right, other.swipe_right) &&
                Objects.equals(a1.move_to, other.move_to) &&
                a1.poll_interval.equals(other.poll_interval) &&
                Objects.equals(a1.keep_alive_noop, other.keep_alive_noop) &&
                (!state || Objects.equals(a1.keep_alive_ok, other.keep_alive_ok)) &&
                (!state || Objects.equals(a1.keep_alive_failed, other.keep_alive_failed)) &&
                (!state || Objects.equals(a1.keep_alive_succeeded, other.keep_alive_succeeded)) &&
                a1.partial_fetch == other.partial_fetch &&
                a1.raw_fetch == other.raw_fetch &&
                a1.ignore_size == other.ignore_size &&
                a1.use_date == other.use_date &&
                a1.use_received == other.use_received &&
                a1.unicode == other.unicode &&
                Objects.equals(a1.conditions, other.conditions) &&
                (!state || Objects.equals(a1.quota_usage, other.quota_usage)) &&
                (!state || Objects.equals(a1.quota_limit, other.quota_limit)) &&
                Objects.equals(a1.created, other.created) &&
                Objects.equals(a1.tbd, other.tbd) &&
                // thread
                (!state || Objects.equals(a1.state, other.state)) &&
                (!state || Objects.equals(a1.warning, other.warning)) &&
                (!state || Objects.equals(a1.error, other.error)) &&
                (!state || Objects.equals(a1.last_connected, other.last_connected)) &&
                (!state || Objects.equals(a1.backoff_until, other.backoff_until)) &&
                (!state || Objects.equals(a1.max_size, other.max_size)) &&
                (!state || Objects.equals(a1.capabilities, other.capabilities)) &&
                (!state || Objects.equals(a1.capability_idle, other.capability_idle)) &&
                (!state || Objects.equals(a1.capability_utf8, other.capability_utf8)) &&
                (!state || Objects.equals(a1.capability_uidl, other.capability_uidl)) &&
                (!state || Objects.equals(a1.last_modified, other.last_modified)));
    }

    @Override
    Comparator getComparator(final Context context) {
        final Collator collator = Collator.getInstance(Locale.getDefault());
        collator.setStrength(Collator.SECONDARY); // Case insensitive, process accents etc

        return new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                EntityAccount a1 = (EntityAccount) o1;
                EntityAccount a2 = (EntityAccount) o2;

                int o = Integer.compare(
                        a1.order == null ? -1 : a1.order,
                        a2.order == null ? -1 : a2.order);
                if (o != 0)
                    return o;

                String name1 = (a1.name == null ? "" : a1.name);
                String name2 = (a2.name == null ? "" : a2.name);
                return collator.compare(name1, name2);
            }
        };
    }

    @NonNull
    @Override
    public String toString() {
        return name + (primary ? " ★" : "");
    }
}
