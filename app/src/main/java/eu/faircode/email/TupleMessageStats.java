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

import java.util.Objects;

public class TupleMessageStats {
    public Long account;
    public Long folder;
    public Integer unseen;
    public Integer notifying;

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof TupleMessageStats) {
            TupleMessageStats other = (TupleMessageStats) obj;
            return (Objects.equals(this.account, other.account) &&
                    Objects.equals(this.folder, other.folder) &&
                    Objects.equals(this.unseen, other.unseen) &&
                    Objects.equals(this.notifying, other.notifying));
        } else
            return false;
    }

    @NonNull
    @Override
    public String toString() {
        return "unseen=" + unseen + " notify=" + notifying;
    }
}
