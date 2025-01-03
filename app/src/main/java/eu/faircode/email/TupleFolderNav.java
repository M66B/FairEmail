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

import java.io.Serializable;
import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;

public class TupleFolderNav extends EntityFolder implements Serializable {
    public Integer accountOrder;
    public String accountName;
    public Integer accountColor;
    public int messages;
    public int unseen;
    public int operations;
    public int executing;

    @Override
    Comparator getComparator(final Context context) {
        final Collator collator = Collator.getInstance(Locale.getDefault());
        collator.setStrength(Collator.SECONDARY); // Case insensitive, process accents etc

        final Comparator base = super.getComparator(context);

        return new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                TupleFolderNav f1 = (TupleFolderNav) o1;
                TupleFolderNav f2 = (TupleFolderNav) o2;

                // Outbox
                if (f1.accountName == null && f2.accountName == null)
                    return 0;
                else if (f1.accountName == null)
                    return -1;
                else if (f2.accountName == null)
                    return 1;

                int fo = Integer.compare(
                        f1.order == null ? -1 : f1.order,
                        f2.order == null ? -1 : f2.order);
                if (fo != 0)
                    return fo;

                int ao = Integer.compare(
                        f1.accountOrder == null ? -1 : f1.accountOrder,
                        f2.accountOrder == null ? -1 : f2.accountOrder);
                if (ao != 0)
                    return ao;

                int a = collator.compare(f1.accountName, f2.accountName);
                if (a != 0)
                    return a;

                return base.compare(o1, o2);
            }
        };
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TupleFolderNav) {
            TupleFolderNav other = (TupleFolderNav) obj;
            return (super.equals(other) &&
                    Objects.equals(this.accountOrder, other.accountOrder) &&
                    Objects.equals(this.accountName, other.accountName) &&
                    Objects.equals(this.accountColor, other.accountColor) &&
                    this.messages == other.messages &&
                    this.unseen == other.unseen &&
                    this.operations == other.operations &&
                    this.executing == other.executing);
        } else
            return false;
    }
}
