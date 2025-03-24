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

import static android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO;
import static android.view.View.IMPORTANT_FOR_ACCESSIBILITY_YES;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AdapterNavSearch extends RecyclerView.Adapter<AdapterNavSearch.ViewHolder> {
    private Context context;
    private LifecycleOwner owner;
    private FragmentManager manager;
    private LayoutInflater inflater;

    private boolean expanded = true;
    private List<EntitySearch> items = new ArrayList<>();

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private View view;
        private ImageView ivItem;
        private ImageView ivBadge;
        private TextView tvCount;
        private TextView tvItem;
        private TextView tvItemExtra;
        private ImageView ivExtra;
        private ImageView ivWarning;

        ViewHolder(View itemView) {
            super(itemView);

            view = itemView.findViewById(R.id.clItem);
            ivItem = itemView.findViewById(R.id.ivItem);
            ivBadge = itemView.findViewById(R.id.ivBadge);
            tvCount = itemView.findViewById(R.id.tvCount);
            tvItem = itemView.findViewById(R.id.tvItem);
            tvItemExtra = itemView.findViewById(R.id.tvItemExtra);
            ivExtra = itemView.findViewById(R.id.ivExtra);
            ivWarning = itemView.findViewById(R.id.ivWarning);
        }

        private void wire() {
            view.setOnClickListener(this);
        }

        private void unwire() {
            view.setOnClickListener(null);
        }

        private void bindTo(EntitySearch search) {
            ivItem.setImageResource(R.drawable.twotone_search_24);
            if (search.color == null)
                ivItem.clearColorFilter();
            else
                ivItem.setColorFilter(search.color);

            ivBadge.setVisibility(View.GONE);
            tvCount.setVisibility(View.GONE);
            tvItem.setText(search.name);

            ivItem.setContentDescription(tvItem.getText());
            ivItem.setImportantForAccessibility(expanded ? IMPORTANT_FOR_ACCESSIBILITY_NO : IMPORTANT_FOR_ACCESSIBILITY_YES);

            tvItemExtra.setVisibility(View.GONE);
            ivExtra.setVisibility(View.GONE);
            ivWarning.setVisibility(View.GONE);
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return;

            EntitySearch search = items.get(pos);
            if (search == null)
                return;

            Bundle args = new Bundle();
            args.putString("account_uuid", search.account_uuid);
            args.putString("folder_name", search.folder_name);

            new SimpleTask<Long[]>() {
                @Override
                protected Long[] onExecute(Context context, Bundle args) throws Throwable {
                    String account_uuid = args.getString("account_uuid");
                    String folder_name = args.getString("folder_name");

                    DB db = DB.getInstance(context);
                    EntityAccount account = db.account().getAccountByUUID(account_uuid);
                    EntityFolder folder = db.folder().getFolderByName(account == null ? -1L : account.id, folder_name);

                    return new Long[]{account == null ? -1L : account.id, folder == null ? -1L : folder.id};
                }

                @Override
                protected void onExecuted(Bundle args, Long[] data) {
                    try {
                        JSONObject json = new JSONObject(search.data);
                        BoundaryCallbackMessages.SearchCriteria criteria =
                                BoundaryCallbackMessages.SearchCriteria.fromJsonData(json);
                        criteria.id = search.id;
                        criteria.name = search.name;
                        criteria.order = search.order;
                        criteria.color = search.color;
                        FragmentMessages.search(
                                context, owner, manager,
                                data[0], data[1], false, criteria);
                    } catch (Throwable ex) {
                        Log.e(ex);
                    }
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(manager, ex);
                }
            }.execute(context, owner, args, "search");
        }
    }

    AdapterNavSearch(Context context, LifecycleOwner owner, FragmentManager manager) {
        this.context = context;
        this.owner = owner;
        this.manager = manager;
        this.inflater = LayoutInflater.from(context);

        setHasStableIds(true);

        owner.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            public void onDestroyed() {
                Log.d(AdapterNavSearch.this + " parent destroyed");
                AdapterNavSearch.this.manager = null;
                owner.getLifecycle().removeObserver(this);
            }
        });
    }

    public void set(@NonNull List<EntitySearch> searches, boolean expanded) {
        Log.i("Set nav search=" + searches.size() + " expanded=" + expanded);

        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffCallback(items, searches), false);

        this.expanded = expanded;
        this.items = searches;

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

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
        notifyDataSetChanged();
    }

    EntitySearch get(int pos) {
        return items.get(pos);
    }

    private static class DiffCallback extends DiffUtil.Callback {
        private List<EntitySearch> prev = new ArrayList<>();
        private List<EntitySearch> next = new ArrayList<>();

        DiffCallback(List<EntitySearch> prev, List<EntitySearch> next) {
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
            EntitySearch s1 = prev.get(oldItemPosition);
            EntitySearch s2 = next.get(newItemPosition);
            return s1.id.equals(s2.id);
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            EntitySearch s1 = prev.get(oldItemPosition);
            EntitySearch s2 = next.get(newItemPosition);
            return s1.equals(s2);
        }
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).id;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.item_nav, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.unwire();
        EntitySearch search = items.get(position);
        holder.bindTo(search);
        holder.wire();
    }
}
