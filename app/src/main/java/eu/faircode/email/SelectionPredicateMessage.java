package eu.faircode.email;

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