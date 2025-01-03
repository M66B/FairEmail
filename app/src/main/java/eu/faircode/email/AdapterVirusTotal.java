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

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AdapterVirusTotal extends RecyclerView.Adapter<AdapterVirusTotal.ViewHolder> {
    private Context context;
    private LifecycleOwner owner;
    private LayoutInflater inflater;
    private int colorWarning;
    private int textColorSecondary;

    private List<VirusTotal.ScanResult> items = new ArrayList<>();

    public class ViewHolder extends RecyclerView.ViewHolder {
        private View view;
        private TextView tvName;
        private TextView tvCategory;

        ViewHolder(View itemView) {
            super(itemView);

            view = itemView.findViewById(R.id.clItem);
            tvName = itemView.findViewById(R.id.tvName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
        }

        private void wire() {
        }

        private void unwire() {
        }

        private void bindTo(VirusTotal.ScanResult scan) {
            boolean malicious = "malicious".equals(scan.category);
            tvName.setText(scan.name);
            tvCategory.setText(scan.category);
            tvCategory.setTextColor(malicious ? colorWarning : textColorSecondary);
            tvCategory.setTypeface(malicious ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        }
    }

    AdapterVirusTotal(Context context, LifecycleOwner owner) {
        this.context = context;
        this.owner = owner;
        this.inflater = LayoutInflater.from(context);
        this.colorWarning = Helper.resolveColor(context, R.attr.colorWarning);
        this.textColorSecondary = Helper.resolveColor(context, android.R.attr.textColorSecondary);

        setHasStableIds(true);

        owner.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            public void onDestroyed() {
                Log.d(AdapterVirusTotal.this + " parent destroyed");
                owner.getLifecycle().removeObserver(this);
            }
        });
    }

    public void set(@NonNull List<VirusTotal.ScanResult> scans) {
        Log.i("Set scans=" + scans.size());

        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffCallback(items, scans), false);

        items = scans;

        diff.dispatchUpdatesTo(new ListUpdateCallback() {
            @Override
            public void onInserted(int position, int count) {
                Log.d("Inserted @" + position + " #" + count);
            }

            @Override
            public void onRemoved(int position, int count) {
                Log.d("Removed @" + position + " #" + count);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                Log.d("Moved " + fromPosition + ">" + toPosition);
            }

            @Override
            public void onChanged(int position, int count, Object payload) {
                Log.d("Changed @" + position + " #" + count);
            }
        });

        try {
            diff.dispatchUpdatesTo(this);
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    private static class DiffCallback extends DiffUtil.Callback {
        private List<VirusTotal.ScanResult> prev = new ArrayList<>();
        private List<VirusTotal.ScanResult> next = new ArrayList<>();

        DiffCallback(List<VirusTotal.ScanResult> prev, List<VirusTotal.ScanResult> next) {
            this.prev.addAll(prev);
            this.next.addAll(next);
        }

        @Override
        public int getOldListSize() {
            return prev.size();
        }

        @Override
        public int getNewListSize() {
            return next.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            VirusTotal.ScanResult m1 = prev.get(oldItemPosition);
            VirusTotal.ScanResult m2 = next.get(newItemPosition);
            return m1.name.equals(m2.name);
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            VirusTotal.ScanResult m1 = prev.get(oldItemPosition);
            VirusTotal.ScanResult m2 = next.get(newItemPosition);
            return (m1.name.equals(m2.name) &&
                    Objects.equals(m1.category, m2.category));
        }
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).name.hashCode();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.item_virus_total, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.unwire();
        VirusTotal.ScanResult scan = items.get(position);
        holder.bindTo(scan);
        holder.wire();
    }
}