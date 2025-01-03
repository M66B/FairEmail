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
        viewName = "identity_view",
        value = TupleIdentityView.query
)
public class TupleIdentityView {
    static final String query = "SELECT id, name, email, account, display, color, synchronize FROM identity";

    @NonNull
    public Long id;
    @NonNull
    public String name;
    @NonNull
    public String email;
    @NonNull
    public Long account;
    public String display;
    public Integer color;
    @NonNull
    public Boolean synchronize;

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof TupleIdentityView) {
            TupleIdentityView other = (TupleIdentityView) obj;
            return (this.id.equals(other.id) &&
                    Objects.equals(this.name, other.name) &&
                    Objects.equals(this.email, other.email) &&
                    Objects.equals(this.display, other.display) &&
                    Objects.equals(this.color, other.color) &&
                    Objects.equals(this.synchronize, other.synchronize));
        } else
            return false;
    }
}
