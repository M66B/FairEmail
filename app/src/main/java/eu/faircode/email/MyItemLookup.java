package eu.faircode.email;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.widget.RecyclerView;

public class MyItemLookup extends ItemDetailsLookup<Long> {

    private final RecyclerView recyclerView;

    public MyItemLookup(RecyclerView recyclerView) {
        Log.i(Helper.TAG, "MyItemLookup");
        this.recyclerView = recyclerView;
    }

    @Nullable
    @Override
    public ItemDetails<Long> getItemDetails(@NonNull MotionEvent e) {
        Log.i(Helper.TAG, "MyItemLookup.getItemDetails");
        View view = recyclerView.findChildViewUnder(e.getX(), e.getY());
        if (view != null) {
            RecyclerView.ViewHolder viewHolder = recyclerView.getChildViewHolder(view);
            if (viewHolder instanceof AdapterMessage.ViewHolder) {
                return ((AdapterMessage.ViewHolder) viewHolder).getItemDetails();
            }
        }

        return null;
    }
}
