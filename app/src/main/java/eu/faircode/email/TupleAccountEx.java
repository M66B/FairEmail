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

import java.util.Objects;

public class TupleAccountEx extends EntityAccount {
    public int unseen;
    public int identities; // synchronizing
    public Long drafts;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TupleAccountEx) {
            TupleAccountEx other = (TupleAccountEx) obj;
            return (super.equals(obj) &&
                    this.unseen == other.unseen &&
                    this.identities == other.identities &&
                    Objects.equals(this.drafts, other.drafts));
        } else
            return false;
    }
}
