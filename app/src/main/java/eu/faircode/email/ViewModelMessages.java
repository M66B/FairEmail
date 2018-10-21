package eu.faircode.email;

import androidx.lifecycle.ViewModel;
import androidx.paging.PagedList;

public class ViewModelMessages extends ViewModel {
    private PagedList<TupleMessageEx> messages = null;

    void setMessages(PagedList<TupleMessageEx> messages) {
        this.messages = messages;
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
                prev == null ? null : new Target(prev.account, prev.thread),
                next == null ? null : new Target(next.account, next.thread)};
    }

    class Target {
        long account;
        String thread;

        Target(long account, String thread) {
            this.account = account;
            this.thread = thread;
        }
    }
}
