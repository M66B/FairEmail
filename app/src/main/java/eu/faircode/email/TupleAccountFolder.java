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

    Copyright 2018-2022 by Marcel Bokhorst (M66B)
*/

import android.content.Context;

import java.util.Objects;

public class TupleAccountFolder extends EntityAccount {
    public Long folderId;
    public Character folderSeparator;
    public String folderType;
    public Integer folderOrder;
    public String folderName;
    public String folderDisplay;
    public Integer folderColor;
    public boolean folderSync;
    public String folderState;
    public String folderSyncState;
    public int executing;
    public int messages;
    public int unseen;

    public String getName(Context context) {
        if (folderName == null)
            return name; // account name

        if (folderDisplay != null)
            return folderDisplay;

        if (EntityFolder.INBOX.equals(folderType))
            return EntityFolder.localizeName(context, folderName);

        if (folderSeparator == null)
            return folderName;

        int s = folderName.lastIndexOf(folderSeparator);
        if (s < 0)
            return folderName;

        return folderName.substring(s + 1);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TupleAccountFolder) {
            TupleAccountFolder other = (TupleAccountFolder) obj;
            return (super.equals(obj) &&
                    Objects.equals(this.folderId, other.folderId) &&
                    Objects.equals(this.folderSeparator, other.folderSeparator) &&
                    Objects.equals(this.folderType, other.folderType) &&
                    Objects.equals(this.folderOrder, other.folderOrder) &&
                    Objects.equals(this.folderName, other.folderName) &&
                    Objects.equals(this.folderDisplay, other.folderDisplay) &&
                    Objects.equals(this.folderColor, other.folderColor) &&
                    this.folderSync == other.folderSync &&
                    Objects.equals(this.folderState, other.folderState) &&
                    Objects.equals(this.folderSyncState, other.folderSyncState) &&
                    this.executing == other.executing &&
                    this.messages == other.messages &&
                    this.unseen == other.unseen);
        } else
            return false;
    }
}
