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

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.room.Ignore;

import java.io.Serializable;
import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class TupleFolderEx extends EntityFolder implements Serializable {
    public Long accountId;
    public Integer accountProtocol;
    public Integer accountOrder;
    public String accountName;
    public String accountCategory;
    public Integer accountColor;
    public String accountState;
    public String accountError;
    public int rules;
    public int messages;
    public int content;
    public int unseen;
    public int unexposed;
    public int flagged;
    public int executing;

    @Ignore
    public int indentation = 0;
    @Ignore
    public boolean expander = true;
    @Ignore
    public TupleFolderEx parent_ref;
    @Ignore
    public List<TupleFolderEx> child_refs;
    @Ignore
    public int childs_unseen = 0;

    boolean isHidden(boolean selecting) {
        return (!selecting && this.hide_seen && this.unseen + this.childs_unseen == 0);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TupleFolderEx) {
            TupleFolderEx other = (TupleFolderEx) obj;
            return (super.equals(obj) &&
                    Objects.equals(this.accountId, other.accountId) &&
                    Objects.equals(this.accountProtocol, other.accountProtocol) &&
                    Objects.equals(this.accountOrder, other.accountOrder) &&
                    Objects.equals(this.accountName, other.accountName) &&
                    Objects.equals(this.accountCategory, other.accountCategory) &&
                    Objects.equals(this.accountColor, other.accountColor) &&
                    Objects.equals(this.accountState, other.accountState) &&
                    Objects.equals(this.accountError, other.accountError) &&
                    this.rules == other.rules &&
                    this.messages == other.messages &&
                    this.content == other.content &&
                    this.unseen == other.unseen &&
                    this.unexposed == other.unexposed &&
                    this.flagged == other.flagged &&
                    this.executing == other.executing &&
                    this.indentation == other.indentation &&
                    this.childs_unseen == other.childs_unseen);
        } else
            return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    void createNotificationChannel(Context context) {
        NotificationManager nm = Helper.getSystemService(context, NotificationManager.class);

        NotificationChannelGroup group = new NotificationChannelGroup("group." + accountId, accountName);
        nm.createNotificationChannelGroup(group);

        NotificationChannel channel = new NotificationChannel(
                getNotificationChannelId(id), getDisplayName(context),
                NotificationManager.IMPORTANCE_HIGH);
        channel.setGroup(group.getId());
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        channel.setBypassDnd(true);
        channel.enableLights(true);
        nm.createNotificationChannel(channel);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    void deleteNotificationChannel(Context context) {
        NotificationManager nm = Helper.getSystemService(context, NotificationManager.class);
        nm.deleteNotificationChannel(getNotificationChannelId(id));
    }

    @Override
    Comparator getComparator(final Context context) {
        final Collator collator = Collator.getInstance(Locale.getDefault());
        collator.setStrength(Collator.SECONDARY); // Case insensitive, process accents etc

        final Comparator base = super.getComparator(context);

        return new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                TupleFolderEx f1 = (TupleFolderEx) o1;
                TupleFolderEx f2 = (TupleFolderEx) o2;

                int c = collator.compare(
                        f1.accountCategory == null ? "" : f1.accountCategory,
                        f2.accountCategory == null ? "" : f2.accountCategory);
                if (c != 0)
                    return c;

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
