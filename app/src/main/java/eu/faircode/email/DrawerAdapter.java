package eu.faircode.email;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class DrawerAdapter extends ArrayAdapter<DrawerItem> {
    DrawerAdapter(@NonNull Context context) {
        super(context, -1);
    }

    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        DrawerItem item = getItem(position);
        View row = LayoutInflater.from(getContext()).inflate(item.getLayout(), null);

        ImageView iv = row.findViewById(R.id.ivItem);
        TextView tv = row.findViewById(R.id.tvItem);

        if (iv != null) {
            iv.setImageResource(item.getIcon());
            if (item.getColor() != null)
                iv.setColorFilter(item.getColor());
        }

        if (tv != null) {
            tv.setText(item.getTitle());

            tv.setTextColor(Helper.resolveColor(getContext(),
                    item.getHighlight() ? R.attr.colorUnread : android.R.attr.textColorSecondary));
        }

        return row;
    }

    @Override
    public boolean isEnabled(int position) {
        DrawerItem item = getItem(position);
        return (item != null && item.getId() != 0);
    }
}
