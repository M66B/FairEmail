package eu.faircode.email;

/*
    This file is part of FairEmail.

    FairEmail is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FairEmail is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with FairEmail.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2018-2025 by Marcel Bokhorst (M66B)
*/

import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemDetailsLookup;

public class ItemDetailsMessage extends ItemDetailsLookup.ItemDetails<Long> {
    private AdapterMessage.ViewHolder viewHolder;

    ItemDetailsMessage(AdapterMessage.ViewHolder viewHolder) {
        this.viewHolder = viewHolder;
    }

    @Override
    public int getPosition() {
        int pos = viewHolder.getAdapterPosition();
        Log.d("ItemDetails pos=" + pos);
        return pos;
    }

    @Nullable
    @Override
    public Long getSelectionKey() {
        int pos = viewHolder.getAdapterPosition();
        Long key = viewHolder.getKey();
        Log.d("ItemDetails pos=" + pos + " key=" + key);
        return key;
    }
}
