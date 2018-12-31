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

import androidx.lifecycle.ViewModel;
import androidx.paging.PagedList;

public class ViewModelMessages extends ViewModel {
    private PagedList<TupleMessageEx> messages = null;

    void setMessages(PagedList<TupleMessageEx> messages) {
        this.messages = messages;
    }

    @Override
    protected void onCleared() {
        messages = null;
    }

    Target[] getPrevNext(String thread) {
        if (messages == null)
            return new Target[]{null, null};

        boolean found = false;
        TupleMessageEx prev = null;
        TupleMessageEx next = null;
        for (int i = 0; i < messages.size(); i++) {
            TupleMessageEx item = messages.get(i);
            if (item == null)
                continue;
            if (found) {
                prev = item;
                messages.loadAround(i);
                break;
            }
            if (thread.equals(item.thread))
                found = true;
            else
                next = item;
        }
        return new Target[]{
                prev == null ? null : new Target(prev.account, prev.thread, prev.id, prev.ui_found),
                next == null ? null : new Target(next.account, next.thread, next.id, next.ui_found)};
    }

    class Target {
        long account;
        String thread;
        long id;
        boolean found;

        Target(long account, String thread, long id, boolean found) {
            this.account = account;
            this.thread = thread;
            this.id = id;
            this.found = found;
        }
    }
}
