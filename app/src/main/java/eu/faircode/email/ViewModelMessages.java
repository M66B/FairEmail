package eu.faircode.email;

import androidx.lifecycle.ViewModel;
import androidx.paging.PagedList;

public class ViewModelMessages extends ViewModel {
    private PagedList<TupleMessageEx> messages = null;

    void setMessages(PagedList<TupleMessageEx> messages) {
        this.messages = messages;
    }

    String[] getPrevNext(String thread) {
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
        return new String[]{prev == null ? null : prev.thread, next == null ? null : next.thread};
    }
}
