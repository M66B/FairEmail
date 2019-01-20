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

import java.util.HashMap;
import java.util.Map;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ViewModel;
import androidx.paging.PagedList;

public class ViewModelMessages extends ViewModel {
    private Map<Boolean, LiveData<PagedList<TupleMessageEx>>> messages = new HashMap<>();

    void setMessages(AdapterMessage.ViewType viewType, LifecycleOwner owner, final LiveData<PagedList<TupleMessageEx>> messages) {
        final boolean thread = (viewType == AdapterMessage.ViewType.THREAD);
        this.messages.put(thread, messages);

        // Keep list up-to-date for previous/next navigation
        messages.observe(owner, new Observer<PagedList<TupleMessageEx>>() {
            @Override
            public void onChanged(PagedList<TupleMessageEx> messages) {
            }
        });

        owner.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            public void onDestroyed() {
                Log.i("Removed model thread=" + thread);
                ViewModelMessages.this.messages.remove(thread);
            }
        });
    }

    void observe(AdapterMessage.ViewType viewType, LifecycleOwner owner, Observer<PagedList<TupleMessageEx>> observer) {
        if (owner.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.INITIALIZED)) {
            final boolean thread = (viewType == AdapterMessage.ViewType.THREAD);
            messages.get(thread).observe(owner, observer);
        }
    }

    void removeObservers(AdapterMessage.ViewType viewType, LifecycleOwner owner) {
        if (owner.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.INITIALIZED)) {
            boolean thread = (viewType == AdapterMessage.ViewType.THREAD);
            LiveData<PagedList<TupleMessageEx>> list = messages.get(thread);
            if (list != null)
                list.removeObservers(owner);
        }
    }

    boolean isEmpty(AdapterMessage.ViewType viewType) {
        boolean thread = (viewType == AdapterMessage.ViewType.THREAD);
        LiveData<PagedList<TupleMessageEx>> list = messages.get(thread);
        return (list == null || list.getValue() == null || list.getValue().size() == 0);
    }

    @Override
    protected void onCleared() {
        messages.clear();
    }

    Target[] getPrevNext(String thread) {
        LiveData<PagedList<TupleMessageEx>> list = messages.get(false);
        if (list == null || list.getValue() == null || list.getValue().size() == 0)
            return new Target[]{null, null};

        boolean found = false;
        TupleMessageEx prev = null;
        TupleMessageEx next = null;
        for (int i = 0; i < list.getValue().size(); i++) {
            TupleMessageEx item = list.getValue().get(i);
            if (item == null)
                continue;
            if (found) {
                prev = item;
                list.getValue().loadAround(i);
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
