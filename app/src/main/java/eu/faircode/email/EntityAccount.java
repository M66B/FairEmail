package eu.faircode.email;

/*
    This file is part of FairEmail.

    FairEmail is free software: you can redistribute it and/or modify
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

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(
        tableName = EntityAccount.TABLE_NAME,
        indices = {
        }
)
public class EntityAccount {
    static final String TABLE_NAME = "account";

    @PrimaryKey(autoGenerate = true)
    public Long id;
    public String name;
    @NonNull
    public String host; // IMAP
    @NonNull
    public Integer port;
    @NonNull
    public String user;
    @NonNull
    public String password;
    @NonNull
    public Boolean primary;
    @NonNull
    public Boolean synchronize;
    @NonNull
    public Boolean store_sent;
    @NonNull
    public Integer poll_interval;
    public Long seen_until;
    public String state;
    public String error;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EntityAccount) {
            EntityAccount other = (EntityAccount) obj;
            return ((this.name == null ? other.name == null : this.name.equals(other.name)) &&
                    this.host.equals(other.host) &&
                    this.port.equals(other.port) &&
                    this.user.equals(other.user) &&
                    this.password.equals(other.password) &&
                    this.primary.equals(other.primary) &&
                    this.synchronize.equals(other.synchronize) &&
                    (this.seen_until == null ? other.seen_until == null : this.seen_until.equals(other.seen_until)) &&
                    (this.state == null ? other.state == null : this.state.equals(other.state)) &&
                    (this.error == null ? other.error == null : this.error.equals(other.error)));
        } else
            return false;
    }

    @Override
    public String toString() {
        return name + (primary ? " ★" : "");
    }
}
