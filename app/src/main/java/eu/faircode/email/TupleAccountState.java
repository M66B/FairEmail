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

import java.util.Objects;

public class TupleAccountState extends EntityAccount {
    // TODO: folder property changes (name, synchronize, poll)
    public int folders;
    public int operations;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TupleAccountState) {
            TupleAccountState other = (TupleAccountState) obj;
            return (this.host.equals(other.host) &&
                    this.starttls == other.starttls &&
                    this.insecure == other.insecure &&
                    this.port.equals(other.port) &&
                    // auth_type
                    this.user.equals(other.user) &&
                    this.password.equals(other.password) &&
                    Objects.equals(this.realm, other.realm) &&
                    this.notify == other.notify &&
                    this.poll_interval.equals(other.poll_interval) &&
                    this.partial_fetch == other.partial_fetch &&
                    this.ignore_size == other.ignore_size &&
                    this.use_date == other.use_date &&
                    this.folders == other.folders);
        } else
            return false;
    }

    boolean shouldRun() {
        return (synchronize && (folders > 0 || operations > 0));
    }
}
