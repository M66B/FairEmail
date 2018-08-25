package eu.faircode.email;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.PagedList;
import androidx.recyclerview.selection.ItemKeyProvider;
import androidx.recyclerview.widget.RecyclerView;

public class MyItemKeyProvider extends ItemKeyProvider<Long> {
    private final PagedList<TupleMessageEx> messages;

    public MyItemKeyProvider(PagedList<TupleMessageEx> messages) {
        super(ItemKeyProvider.SCOPE_MAPPED);
        this.messages = messages;
        Log.i(Helper.TAG, "MyItemKeyProvider");
    }

    @Nullable
    @Override
    public Long getKey(int position) {
        Log.i(Helper.TAG, "MyItemKeyProvider.getKey pos=" + position + " key=" + messages.get(position).id);
        return messages.get(position).id;
    }

    @Override
    public int getPosition(@NonNull Long key) {
        Log.i(Helper.TAG, "MyItemKeyProvider.getPosition key=" + key);
        int pos = RecyclerView.NO_POSITION;
        for (int i = 0; i < messages.size(); i++)
            if (messages.get(i).id.equals(key))
                pos = i;
        Log.i(Helper.TAG, "MyItemKeyProvider.getPosition key=" + key + " pos=" + pos);
        return pos;
    }
}