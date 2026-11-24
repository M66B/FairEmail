package eu.faircode.email;

/*
    This file is part of FairEmail.
    FairEmail is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License, version 3 or later.
*/

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

class OptionsSuggestionAdapter extends RecyclerView.Adapter<OptionsSuggestionAdapter.Holder> {
    static class Item {
        final int tab;
        final int resid;
        final String title;

        Item(int tab, int resid, String title) {
            this.tab = tab;
            this.resid = resid;
            this.title = title;
        }
    }

    interface OnSelect {
        void onSelect(Item item);
    }

    private final Context context;
    private final List<Item> items = new ArrayList<>();
    private OnSelect onSelect;

    OptionsSuggestionAdapter(Context context) {
        this.context = context;
    }

    void setOnSelect(OnSelect callback) {
        onSelect = callback;
    }

    void setItems(List<Item> result) {
        items.clear();
        items.addAll(result);
        notifyDataSetChanged();
    }

    Item get(int position) {
        return position >= 0 && position < items.size() ? items.get(position) : null;
    }

    private TextView createLabel() {
        TextView text = new TextView(context);
        int minimumHeight = Helper.dp2pixels(context, 48);
        int horizontalPadding = Helper.dp2pixels(context, 12);
        int verticalPadding = Helper.dp2pixels(context, 8);
        text.setLayoutParams(new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        text.setMinHeight(minimumHeight);
        text.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);
        text.setGravity(Gravity.CENTER_VERTICAL);
        text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        text.setTextColor(Helper.resolveColor(context, android.R.attr.textColorPrimary));
        text.setSingleLine(false);
        text.setHorizontallyScrolling(false);
        text.setEllipsize(null);
        TypedValue ripple = new TypedValue();
        if (context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, ripple, true))
            text.setBackgroundResource(ripple.resourceId);
        text.setFocusable(false);
        return text;
    }

    // The options can span multiple lines, so a fixed 48dp per row would clip them
    // Measure at the actual popup width, limiting work to the visible six results
    int measureHeight(int width, int maxRows) {
        if (width <= 0 || items.isEmpty())
            return 0;
        TextView label = createLabel();
        int widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int height = 0;
        for (int i = 0; i < Math.min(items.size(), maxRows); i++) {
            label.setText(items.get(i).title);
            label.measure(widthSpec, heightSpec);
            height += label.getMeasuredHeight();
        }
        return height;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(createLabel());
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.label.setText(items.get(position).title);
        holder.label.setOnClickListener(v -> {
            int current = holder.getAdapterPosition();
            if (onSelect != null && current != RecyclerView.NO_POSITION)
                onSelect.onSelect(items.get(current));
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView label;
        Holder(TextView label) {
            super(label);
            this.label = label;
        }
    }
}
