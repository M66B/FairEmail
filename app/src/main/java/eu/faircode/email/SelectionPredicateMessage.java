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

import androidx.annotation.NonNull;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.RecyclerView;

public class SelectionPredicateMessage extends SelectionTracker.SelectionPredicate<Long> {
    private boolean enabled;
    private RecyclerView recyclerView;

    SelectionPredicateMessage(RecyclerView recyclerView) {
        this.enabled = true;
        this.recyclerView = recyclerView;
    }

    void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean canSetStateForKey(@NonNull Long key, boolean nextState) {
        if (!enabled)
            return false;

        AdapterMessage adapter = (AdapterMessage) recyclerView.getAdapter();
        TupleMessageEx message = adapter.getItemForKey(key);
        if (message == null) // happens when restoring state
            return true;

        if (message.accountProtocol != EntityAccount.TYPE_IMAP)
            return true;

        if (message.uid != null)
            return true;

        return false;
    }

    @Override
    public boolean canSetStateAtPosition(int position, boolean nextState) {
        if (!enabled)
            return false;

        AdapterMessage adapter = (AdapterMessage) recyclerView.getAdapter();
        TupleMessageEx message = adapter.getItemAtPosition(position);
        if (message == null) // happens when restoring state
            return true;

        if (message.accountProtocol != EntityAccount.TYPE_IMAP)
            return true;

        if (message.uid != null && !message.folderReadOnly)
            return true;

        return false;
    }

    @Override
    public boolean canSelectMultiple() {
        return true;
    }
}