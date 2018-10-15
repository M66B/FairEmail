package eu.faircode.email;

/*
    This file is part of FairEmail.

    FairEmail is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    NetGuard is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with NetGuard.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2018 by Marcel Bokhorst (M66B)
*/

public class TupleMessageEx extends EntityMessage {
    public String accountName;
    public Integer accountColor;
    public String folderName;
    public String folderDisplay;
    public String folderType;
    public int count;
    public int unseen;
    public int unflagged;
    public int attachments;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TupleMessageEx) {
            TupleMessageEx other = (TupleMessageEx) obj;
            return (super.equals(obj) &&
                    (this.accountName == null ? other.accountName == null : this.accountName.equals(other.accountName)) &&
                    (this.accountColor == null ? other.accountColor == null : this.accountColor.equals(other.accountColor)) &&
                    this.folderName.equals(other.folderName) &&
                    (this.folderDisplay == null ? other.folderDisplay == null : this.folderDisplay.equals(other.folderDisplay)) &&
                    this.folderType.equals(other.folderType) &&
                    this.count == other.count &&
                    this.unseen == other.unseen &&
                    this.unflagged == other.unflagged &&
                    this.attachments == other.attachments);
        }
        return super.equals(obj);
    }
}
