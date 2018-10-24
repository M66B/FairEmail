package eu.faircode.email;

import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemDetailsLookup;

public class ItemDetailsMessage extends ItemDetailsLookup.ItemDetails<Long> {
    private int pos;
    private Long key;

    ItemDetailsMessage(int pos, Long id) {
        this.pos = pos;
        this.key = id;
    }

    @Override
    public int getPosition() {
        return pos;
    }

    @Nullable
    @Override
    public Long getSelectionKey() {
        return key;
    }
}
