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

import androidx.annotation.Nullable;

public class TupleFtsStats {
    public long fts;
    public long total;

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof TupleFtsStats) {
            TupleFtsStats other = (TupleFtsStats) obj;
            return (this.fts == other.fts && this.total == other.total);
        } else
            return false;
    }
}
