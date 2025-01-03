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
import java.util.Comparator;
import java.util.Locale;

public class TupleFolderSort extends EntityFolder {
    public Integer accountOrder;
    public String accountName;

    @Override
    String[] getSortTitle(Context context) {
        return new String[]{getDisplayName(context), accountName};
    }

    @Override
    Comparator getComparator(final Context context) {
        final Collator collator = Collator.getInstance(Locale.getDefault());
        collator.setStrength(Collator.SECONDARY); // Case insensitive, process accents etc

        final Comparator base = super.getComparator(context);

        return new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                TupleFolderSort f1 = (TupleFolderSort) o1;
                TupleFolderSort f2 = (TupleFolderSort) o2;

                // Outbox
                if (f1.accountName == null && f2.accountName == null)
                    return 0;
                else if (f1.accountName == null)
                    return 1;
                else if (f2.accountName == null)
                    return -1;

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
}
