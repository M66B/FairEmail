package eu.faircode.email;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemDetailsLookup;

public class MyItemDetail extends ItemDetailsLookup.ItemDetails<Long> {
    private final int adapterPosition;
    private final Long selectionKey;

    public MyItemDetail(int adapterPosition, Long selectionKey) {
        Log.i(Helper.TAG, "MyItemDetail");
        this.adapterPosition = adapterPosition;
        this.selectionKey = selectionKey;
    }

    @Override
    public int getPosition() {
        Log.i(Helper.TAG, "MyItemDetail.getPosition=" + adapterPosition);
        return adapterPosition;
    }

    @Nullable
    @Override
    public Long getSelectionKey() {
        Log.i(Helper.TAG, "MyItemDetail.getSelectionKey=" + selectionKey);
        return selectionKey;
    }
}
