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

    Copyright 2018-2020 by Marcel Bokhorst (M66B)
*/

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.DatabaseView;

import java.util.Objects;

@DatabaseView(
        viewName = "operation_view",
        value = TupleOperationView.query
)
public class TupleOperationView {
    static final String query = "SELECT id, folder, state FROM operation";

    @NonNull
    public Long id;
    @NonNull
    public Long folder;
    public String state;

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof TupleOperationView) {
            TupleOperationView other = (TupleOperationView) obj;
            return (this.id.equals(other.id) &&
                    this.folder.equals(other.folder) &&
                    Objects.equals(this.state, other.state));
        } else
            return false;
    }
}
