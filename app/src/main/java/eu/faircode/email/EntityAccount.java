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

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(
        tableName = EntityAccount.TABLE_NAME,
        indices = {
        }
)
public class EntityAccount implements Serializable {
    static final String TABLE_NAME = "account";

    @PrimaryKey(autoGenerate = true)
    public Long id;

    @NonNull
    public Integer auth_type;
    @NonNull
    public String host; // IMAP
    @NonNull
    public Boolean starttls;
    @NonNull
    public Boolean insecure;
    @NonNull
    public Integer port;
    @NonNull
    public String user;
    @NonNull
    public String password;
    public String realm;

    public String name;
    public String signature; // obsolete
    public Integer color;

    @NonNull
    public Boolean synchronize;
    @NonNull
    public Boolean primary;
    @NonNull
    public Boolean notify;
    @NonNull
    public Boolean browse = true;
    public Long swipe_left;
    public Long swipe_right;
    @NonNull
    public Integer poll_interval; // keep-alive interval
    public String prefix; // namespace

    public Long created;
    public Boolean tbd;
    public String state;
    public String error;
    public Long last_connected;

    static String getNotificationChannelName(long account) {
        return "notification." + account;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    void createNotificationChannel(Context context) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel notification = new NotificationChannel(
                getNotificationChannelName(id), name,
                NotificationManager.IMPORTANCE_HIGH);
        notification.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        nm.createNotificationChannel(notification);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    void deleteNotificationChannel(Context context) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.deleteNotificationChannel(getNotificationChannelName(id));
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("auth_type", auth_type);
        json.put("host", host);
        json.put("starttls", starttls);
        json.put("insecure", insecure);
        json.put("port", port);
        json.put("user", user);
        json.put("password", password);
        json.put("realm", password);

        json.put("name", name);
        json.put("color", color);

        json.put("synchronize", synchronize);
        json.put("primary", primary);
        json.put("notify", notify);
        json.put("browse", browse);

        json.put("swipe_left", swipe_left);
        json.put("swipe_right", swipe_right);

        json.put("poll_interval", poll_interval);
        json.put("prefix", prefix);
        // not created
        // not state
        // not error
        // not last connected
        return json;
    }

    public static EntityAccount fromJSON(JSONObject json) throws JSONException {
        EntityAccount account = new EntityAccount();
        account.auth_type = json.getInt("auth_type");
        account.host = json.getString("host");
        account.starttls = (json.has("starttls") && json.getBoolean("starttls"));
        account.insecure = (json.has("insecure") && json.getBoolean("insecure"));
        account.port = json.getInt("port");
        account.user = json.getString("user");
        account.password = json.getString("password");
        if (json.has("realm"))
            account.realm = json.getString("realm");

        if (json.has("name"))
            account.name = json.getString("name");
        if (json.has("color"))
            account.color = json.getInt("color");

        account.synchronize = json.getBoolean("synchronize");
        account.primary = json.getBoolean("primary");
        if (json.has("notify"))
            account.notify = json.getBoolean("notify");
        if (json.has("browse"))
            account.browse = json.getBoolean("browse");

        if (json.has("swipe_left"))
            account.swipe_left = json.getLong("swipe_left");
        if (json.has("swipe_right"))
            account.swipe_right = json.getLong("swipe_right");

        account.poll_interval = json.getInt("poll_interval");
        if (json.has("prefix"))
            account.prefix = json.getString("prefix");

        return account;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EntityAccount) {
            EntityAccount other = (EntityAccount) obj;
            return (this.auth_type.equals(other.auth_type) &&
                    this.host.equals(other.host) &&
                    this.starttls == other.starttls &&
                    this.insecure == other.insecure &&
                    this.port.equals(other.port) &&
                    this.user.equals(other.user) &&
                    this.password.equals(other.password) &&
                    (this.realm == null ? other.realm == null : this.realm.equals(other.realm)) &&
                    (this.name == null ? other.name == null : this.name.equals(other.name)) &&
                    (this.color == null ? other.color == null : this.color.equals(other.color)) &&
                    this.synchronize.equals(other.synchronize) &&
                    this.primary.equals(other.primary) &&
                    this.notify.equals(other.notify) &&
                    this.browse.equals(other.browse) &&
                    (this.swipe_left == null ? other.swipe_left == null : this.swipe_left.equals(other.swipe_left)) &&
                    (this.swipe_right == null ? other.swipe_right == null : this.swipe_right.equals(other.swipe_right)) &&
                    this.poll_interval.equals(other.poll_interval) &&
                    (this.prefix == null ? other.prefix == null : this.prefix.equals(other.prefix)) &&
                    (this.created == null ? other.created == null : this.created.equals(other.created)) &&
                    (this.tbd == null ? other.tbd == null : this.tbd.equals(other.tbd)) &&
                    (this.state == null ? other.state == null : this.state.equals(other.state)) &&
                    (this.error == null ? other.error == null : this.error.equals(other.error)) &&
                    (this.last_connected == null ? other.last_connected == null : this.last_connected.equals(other.last_connected)));
        } else
            return false;
    }

    @NonNull
    @Override
    public String toString() {
        return name + (primary ? " ★" : "");
    }
}
