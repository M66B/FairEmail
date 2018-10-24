package eu.faircode.email;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.PagedList;
import androidx.recyclerview.selection.ItemKeyProvider;
import androidx.recyclerview.widget.RecyclerView;

public class ItemKeyProviderMessage extends ItemKeyProvider<Long> {
    private RecyclerView recyclerView;

    ItemKeyProviderMessage(RecyclerView recyclerView) {
        super(ItemKeyProvider.SCOPE_CACHED);
        this.recyclerView = recyclerView;
    }

    @Nullable
    @Override
    public Long getKey(int pos) {
        AdapterMessage adapter = (AdapterMessage) recyclerView.getAdapter();
        return adapter.getCurrentList().get(pos).id;
    }

    @Override
    public int getPosition(@NonNull Long key) {
        AdapterMessage adapter = (AdapterMessage) recyclerView.getAdapter();
        PagedList<TupleMessageEx> messages = adapter.getCurrentList();
        if (messages != null)
            for (int i = 0; i < messages.size(); i++) {
                TupleMessageEx message = messages.get(i);
                if (message != null && message.id.equals(key))
                    return i;
            }
        return RecyclerView.NO_POSITION;
    }
}
