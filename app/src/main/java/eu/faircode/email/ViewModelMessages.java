package eu.faircode.email;

import androidx.lifecycle.ViewModel;
import androidx.paging.PagedList;

public class ViewModelMessages extends ViewModel {
    private PagedList<TupleMessageEx> messages = null;

    void setMessages(PagedList<TupleMessageEx> messages) {
        this.messages = messages;
    }

    AccountThread[] getPrevNext(String thread) {
        if (messages == null)
            return new AccountThread[]{null, null};

        boolean found = false;
        TupleMessageEx prev = null;
        TupleMessageEx next = null;
        for (int i = 0; i < messages.size(); i++) {
            TupleMessageEx item = messages.get(i);
            if (item == null)
                continue;
            if (found) {
                next = item;
                messages.loadAround(i);
                break;
            }
            if (thread.equals(item.thread))
                found = true;
            else
                prev = item;
        }
        return new AccountThread[]{
                prev == null ? null : new AccountThread(prev.account, prev.thread),
                next == null ? null : new AccountThread(next.account, next.thread)};
    }

    class AccountThread {
        long account;
        String thread;

        AccountThread(long account, String thread) {
            this.account = account;
            this.thread = thread;
        }
    }
}
