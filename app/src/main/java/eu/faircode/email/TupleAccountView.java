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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.DatabaseView;

import java.util.Objects;

@DatabaseView(
        viewName = "account_view",
        value = TupleAccountView.query
)
public class TupleAccountView {
    static final String query = "SELECT id, pop, name, category, color, synchronize, notify, summary, leave_on_server, leave_deleted, auto_seen, created FROM account";

    @NonNull
    public Long id;
    @NonNull
    @ColumnInfo(name = "pop")
    public Integer protocol;
    public String name;
    public String category;
    public Integer color;
    @NonNull
    public Boolean synchronize;
    @NonNull
    public Boolean notify = false;
    @NonNull
    public Boolean summary = false;
    @NonNull
    public Boolean leave_on_server = true;
    @NonNull
    public Boolean leave_deleted = false;
    @NonNull
    public Boolean auto_seen = true;
    public Long created;

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof TupleAccountView) {
            TupleAccountView other = (TupleAccountView) obj;
            return (this.id.equals(other.id) &&
                    this.protocol.equals(other.protocol) &&
                    Objects.equals(this.name, other.name) &&
                    Objects.equals(this.color, other.color) &&
                    this.synchronize.equals(other.synchronize) &&
                    this.notify.equals(other.notify) &&
                    this.summary.equals(other.summary) &&
                    this.leave_on_server.equals(other.leave_on_server) &&
                    this.leave_deleted.equals(other.leave_deleted) &&
                    this.auto_seen.equals(other.auto_seen) &&
                    Objects.equals(this.created, other.created));
        } else
            return false;
    }
}
