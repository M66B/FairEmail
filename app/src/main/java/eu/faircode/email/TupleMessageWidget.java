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

public class TupleMessageWidget extends EntityMessage {
    public String accountName;
    public boolean folderUnified;
    public int unseen;
    public int unflagged;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TupleMessageWidget) {
            TupleMessageWidget other = (TupleMessageWidget) obj;
            return (this.id.equals(other.id) &&
                    this.account.equals(other.account) &&
                    Objects.equals(this.accountName, other.accountName) &&
                    this.folder.equals(other.folder) &&
                    this.folderUnified == other.folderUnified &&
                    MessageHelper.equal(this.from, other.from) &&
                    this.received.equals(other.received) &&
                    Objects.equals(this.subject, other.subject) &&
                    this.unseen == other.unseen &&
                    this.unflagged == other.unflagged);
        }
        return false;
    }
}
