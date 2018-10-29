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

import androidx.annotation.NonNull;
import androidx.paging.PagedList;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.RecyclerView;

public class SelectionPredicateMessage extends SelectionTracker.SelectionPredicate<Long> {

    private RecyclerView recyclerView;

    SelectionPredicateMessage(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    @Override
    public boolean canSetStateForKey(@NonNull Long key, boolean nextState) {
        AdapterMessage adapter = (AdapterMessage) recyclerView.getAdapter();
        PagedList<TupleMessageEx> messages = adapter.getCurrentList();
        if (messages != null)
            for (int i = 0; i < messages.size(); i++) {
                TupleMessageEx message = messages.get(i);
                if (message != null && message.id.equals(key))
                    return (message.uid != null);
            }
        return false;
    }

    @Override
    public boolean canSetStateAtPosition(int position, boolean nextState) {
        AdapterMessage adapter = (AdapterMessage) recyclerView.getAdapter();
        return (adapter.getCurrentList().get(position).uid != null);
    }

    @Override
    public boolean canSelectMultiple() {
        return true;
    }
}