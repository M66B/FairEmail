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

import androidx.annotation.NonNull;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.RecyclerView;

public class SelectionPredicateMessage extends SelectionTracker.SelectionPredicate<Long> {
    private RecyclerView recyclerView;
    private long account = -1;

    SelectionPredicateMessage(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    void clearAccount() {
        account = -1;
    }

    @Override
    public boolean canSetStateForKey(@NonNull Long key, boolean nextState) {
        AdapterMessage adapter = (AdapterMessage) recyclerView.getAdapter();
        TupleMessageEx message = adapter.getItemForKey(key);

        if (message != null && message.uid != null && (account < 0 || account == message.account)) {
            account = message.account;
            return true;
        }

        return false;
    }

    @Override
    public boolean canSetStateAtPosition(int position, boolean nextState) {
        AdapterMessage adapter = (AdapterMessage) recyclerView.getAdapter();
        TupleMessageEx message = adapter.getItemAtPosition(position);

        if (message != null && message.uid != null && (account < 0 || account == message.account)) {
            account = message.account;
            return true;
        }

        return false;
    }

    @Override
    public boolean canSelectMultiple() {
        return true;
    }
}