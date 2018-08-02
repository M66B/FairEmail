package eu.faircode.email;

/*
    This file is part of Safe email.

    Safe email is free software: you can redistribute it and/or modify
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

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(
        tableName = EntityIdentity.TABLE_NAME,
        indices = {
        }
)
public class EntityIdentity {
    static final String TABLE_NAME = "identity";

    @PrimaryKey(autoGenerate = true)
    public Long id;
    @NonNull
    public String name;
    @NonNull
    public String email;
    public String replyto;
    @NonNull
    public String host; // SMTP
    @NonNull
    public Integer port;
    @NonNull
    public Boolean starttls;
    @NonNull
    public String user;
    @NonNull
    public String password;
    @NonNull
    public Boolean primary;
    @NonNull
    public Boolean synchronize;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EntityIdentity) {
            EntityIdentity other = (EntityIdentity) obj;
            return (this.name.equals(other.name) &&
                    this.email.equals(other.email) &&
                    this.replyto == null ? other.replyto == null : this.replyto.equals(other.replyto) &&
                    this.host.equals(other.host) &&
                    this.port.equals(other.port) &&
                    this.starttls.equals(other.starttls) &&
                    this.user.equals(other.user) &&
                    this.password.equals(other.password) &&
                    this.primary.equals(other.primary) &&
                    this.synchronize.equals(other.synchronize));
        } else
            return false;
    }

    @Override
    public String toString() {
        return name;
    }
}
