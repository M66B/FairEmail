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
                    this.synchronize.equals(other.synchronize));
        } else
            return false;
    }
}
