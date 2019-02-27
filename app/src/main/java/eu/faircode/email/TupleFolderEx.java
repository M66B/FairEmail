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

public class TupleFolderEx extends EntityFolder {
    public String accountName;
    public Integer accountColor;
    public String accountState;
    public Boolean accountOnDemand;
    public int messages;
    public int content;
    public int unseen;

    boolean isSynchronizing() {
        return (sync_state != null &&
                (EntityFolder.OUTBOX.equals(type) ||
                        accountOnDemand || "connected".equals(accountState)));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TupleFolderEx) {
            TupleFolderEx other = (TupleFolderEx) obj;
            return (super.equals(obj) &&
                    Objects.equals(accountName, other.accountName) &&
                    Objects.equals(this.accountColor, other.accountColor) &&
                    Objects.equals(accountState, other.accountState) &&
                    Objects.equals(this.accountOnDemand, other.accountOnDemand) &&
                    this.messages == other.messages &&
                    this.content == other.content &&
                    this.unseen == other.unseen);
        } else
            return false;
    }
}
