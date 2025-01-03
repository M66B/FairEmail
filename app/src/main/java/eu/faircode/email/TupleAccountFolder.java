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

import android.content.Context;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class TupleAccountFolder extends EntityAccount {
    public int identities; // synchronizing
    public Long drafts;
    public Long sent;

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
    public int unexposed;

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

    static void sort(List<TupleAccountFolder> accounts, boolean nav_categories, Context context) {
        final Collator collator = Collator.getInstance(Locale.getDefault());
        collator.setStrength(Collator.SECONDARY); // Case insensitive, process accents etc

        Collections.sort(accounts, new Comparator<TupleAccountFolder>() {
            @Override
            public int compare(TupleAccountFolder a1, TupleAccountFolder a2) {
                // Account

                if (nav_categories) {
                    int c = collator.compare(
                            a1.category == null ? "" : a1.category,
                            a2.category == null ? "" : a2.category);
                    if (c != 0)
                        return c;
                }

                int a = Integer.compare(
                        a1.order == null ? -1 : a1.order,
                        a2.order == null ? -1 : a2.order);
                if (a != 0)
                    return a;

                int p = -Boolean.compare(a1.primary, a2.primary);
                if (p != 0)
                    return p;

                int n = collator.compare(a1.name, a2.name);
                if (n != 0)
                    return n;

                // Folder

                int o = Integer.compare(
                        a1.folderOrder == null ? -1 : a1.folderOrder,
                        a2.folderOrder == null ? -1 : a2.folderOrder);
                if (o != 0)
                    return o;

                int t1 = EntityFolder.FOLDER_SORT_ORDER.indexOf(a1.folderType);
                int t2 = EntityFolder.FOLDER_SORT_ORDER.indexOf(a2.folderType);
                int t = Integer.compare(t1, t2);
                if (t != 0)
                    return t;

                int s = -Boolean.compare(a1.folderSync, a2.folderSync);
                if (s != 0)
                    return s;

                if (a1.folderName == null && a2.folderName == null)
                    return 0;
                else if (a1.folderName == null)
                    return -1;
                else if (a2.folderName == null)
                    return 1;

                return collator.compare(a1.getName(context), a2.getName(context));
            }
        });
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TupleAccountFolder) {
            TupleAccountFolder other = (TupleAccountFolder) obj;
            return (super.equals(obj) &&
                    this.identities == other.identities &&
                    Objects.equals(this.drafts, other.drafts) &&
                    Objects.equals(this.sent, other.sent) &&

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
