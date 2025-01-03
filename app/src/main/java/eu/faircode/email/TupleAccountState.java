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

import java.util.Objects;

public class TupleAccountState extends EntityAccount {
    // TODO: folder property changes (name, poll)
    public int folders;
    public int operations;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TupleAccountState) {
            TupleAccountState other = (TupleAccountState) obj;
            return (Objects.equals(this.provider, other.provider) &&
                    this.host.equals(other.host) &&
                    this.encryption.equals(other.encryption) &&
                    this.insecure.equals(other.insecure) &&
                    this.port.equals(other.port) &&
                    this.user.equals(other.user) &&
                    Objects.equals(this.auth_type, other.auth_type) &&
                    this.password.equals(other.password) &&
                    Objects.equals(this.certificate_alias, other.certificate_alias) &&
                    Objects.equals(this.realm, other.realm) &&
                    Objects.equals(this.fingerprint, other.fingerprint) &&
                    this.notify.equals(other.notify) &&
                    this.leave_on_server == other.leave_on_server &&
                    this.leave_on_device == other.leave_on_device &&
                    Objects.equals(this.max_messages, other.max_messages) &&
                    this.keep_alive_noop.equals(other.keep_alive_noop) &&
                    this.poll_interval.equals(other.poll_interval) &&
                    this.poll_exempted.equals(other.poll_exempted) &&
                    this.partial_fetch.equals(other.partial_fetch) &&
                    this.raw_fetch.equals(other.raw_fetch) &&
                    this.ignore_size.equals(other.ignore_size) &&
                    this.use_date.equals(other.use_date) &&
                    this.use_received.equals(other.use_received) &&
                    Objects.equals(this.conditions, other.conditions) &&
                    this.folders == other.folders &&
                    Objects.equals(this.tbd, other.tbd));
        } else
            return false;
    }

    boolean isEnabled(boolean enabled) {
        return (enabled && synchronize && !ondemand && folders > 0 && tbd == null);
    }

    boolean shouldRun(boolean enabled) {
        return (isEnabled(enabled) || (operations > 0 && synchronize && tbd == null));
    }
}
