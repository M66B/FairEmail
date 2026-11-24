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

class SearchSuggestionAdapter extends RecyclerView.Adapter<SearchSuggestionAdapter.Holder> {
    interface OnSelect {
        void onSelect(String suggestion);
    }

    private final Context context;
    private final List<String> suggestions = new ArrayList<>();
    private OnSelect listener;

    SearchSuggestionAdapter(Context context) {
        this.context = context;
    }

    void setOnSelect(OnSelect listener) {
        this.listener = listener;
    }

    void setSuggestions(List<String> items) {
        suggestions.clear();
        suggestions.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TextView view = new TextView(context);
        float density = context.getResources().getDisplayMetrics().density;
        view.setLayoutParams(new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Math.round(48 * density)));
        view.setPadding(Math.round(12 * density), 0, Math.round(12 * density), 0);
        view.setGravity(Gravity.CENTER_VERTICAL);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        view.setSingleLine(true);
        view.setEllipsize(android.text.TextUtils.TruncateAt.END);
        view.setTextColor(Helper.resolveColor(context, android.R.attr.textColorPrimary));
        TypedValue ripple = new TypedValue();
        if (context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, ripple, true))
            view.setBackgroundResource(ripple.resourceId);
        view.setFocusable(false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.text.setText(suggestions.get(position));
        holder.text.setOnClickListener(v -> {
            int current = holder.getAdapterPosition();
            if (current != RecyclerView.NO_POSITION && listener != null)
                listener.onSelect(suggestions.get(current));
        });
    }

    @Override
    public int getItemCount() {
        return suggestions.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView text;
        Holder(@NonNull TextView view) {
            super(view);
            text = view;
        }
    }
}
