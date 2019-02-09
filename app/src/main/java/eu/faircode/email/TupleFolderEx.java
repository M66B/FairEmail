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

public class TupleFolderEx extends EntityFolder {
    public String accountName;
    public Integer accountColor;
    public boolean accountPop;
    public String accountState;
    public int messages;
    public int content;
    public int unseen;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TupleFolderEx) {
            TupleFolderEx other = (TupleFolderEx) obj;
            return (super.equals(obj) &&
                    (this.accountName == null ? other.accountName == null : accountName.equals(other.accountName)) &&
                    (this.accountColor == null ? other.accountColor == null : this.accountColor.equals(other.accountColor)) &&
                    this.accountPop == other.accountPop &&
                    (this.accountState == null ? other.accountState == null : accountState.equals(other.accountState)) &&
                    this.messages == other.messages &&
                    this.content == other.content &&
                    this.unseen == other.unseen);
        } else
            return false;
    }
}
