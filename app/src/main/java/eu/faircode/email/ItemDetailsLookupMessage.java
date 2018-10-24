package eu.faircode.email;

import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.widget.RecyclerView;

public class ItemDetailsLookupMessage extends ItemDetailsLookup<Long> {
    private RecyclerView recyclerView;

    ItemDetailsLookupMessage(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    @Nullable
    @Override
    public ItemDetails<Long> getItemDetails(@NonNull MotionEvent motionEvent) {
        View view = recyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());
        if (view != null) {
            RecyclerView.ViewHolder viewHolder = recyclerView.getChildViewHolder(view);
            if (viewHolder instanceof AdapterMessage.ViewHolder)
                return ((AdapterMessage.ViewHolder) viewHolder).getItemDetails(motionEvent);
        }
        return null;
    }
}