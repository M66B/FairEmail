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
import androidx.room.DatabaseView;

import java.util.Objects;

@DatabaseView(
        viewName = "folder_view",
        value = TupleFolderView.query
)
public class TupleFolderView {
    static final String query = "SELECT id, account, name, type, inherited_type, display, color, unified, notify, read_only FROM folder";

    @NonNull
    public Long id;
    public Long account;
    @NonNull
    public String name;
    @NonNull
    public String type;
    public String inherited_type;
    public String display;
    public Integer color;
    @NonNull
    public Boolean unified = false;
    @NonNull
    public Boolean notify = false;
    @NonNull
    public Boolean read_only = false;

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof TupleFolderView) {
            TupleFolderView other = (TupleFolderView) obj;
            return (Objects.equals(this.id, other.id) &&
                    Objects.equals(this.account, other.account) &&
                    this.name.equals(other.name) &&
                    this.type.equals(other.type) &&
                    Objects.equals(this.inherited_type, other.inherited_type) &&
                    Objects.equals(this.display, other.display) &&
                    Objects.equals(this.color, other.color) &&
                    this.unified == other.unified &&
                    this.notify == other.notify &&
                    this.read_only == other.read_only);
        } else
            return false;
    }
}
