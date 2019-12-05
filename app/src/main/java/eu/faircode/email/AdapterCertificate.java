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

    Copyright 2018-2019 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AdapterCertificate extends RecyclerView.Adapter<AdapterCertificate.ViewHolder> {
    private Context context;
    private LifecycleOwner owner;
    private LayoutInflater inflater;

    private String email;
    private List<EntityCertificate> items = new ArrayList<>();

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        private View view;
        private TextView tvEmail;
        private TextView tvSubject;

        private TwoStateOwner powner = new TwoStateOwner(owner, "CertificatePopup");

        ViewHolder(View itemView) {
            super(itemView);

            view = itemView.findViewById(R.id.clItem);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvSubject = itemView.findViewById(R.id.tvSubject);
        }

        @Override
        public boolean onLongClick(View v) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return false;

            final EntityCertificate certificate = items.get(pos);

            PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(context, powner, view);

            popupMenu.getMenu().add(Menu.NONE, 0, 0, certificate.email).setEnabled(false);

            popupMenu.getMenu().add(Menu.NONE, R.string.title_delete, 1, R.string.title_delete);

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.string.title_delete:
                            onActionDelete();
                            return true;

                        default:
                            return false;
                    }
                }

                private void onActionDelete() {
                    Bundle args = new Bundle();
                    args.putLong("id", certificate.id);

                    new SimpleTask<Void>() {
                        @Override
                        protected Void onExecute(Context context, Bundle args) {
                            long id = args.getLong("id");

                            DB db = DB.getInstance(context);
                            db.certificate().deleteCertificate(id);

                            return null;
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            // TODO: report error
                        }
                    }.execute(context, owner, args, "certificate:delete");
                }
            });

            popupMenu.show();

            return true;
        }

        private void wire() {
            view.setOnLongClickListener(this);
        }

        private void unwire() {
            view.setOnLongClickListener(null);
        }

        private void bindTo(EntityCertificate certificate) {
            tvEmail.setText(certificate.email);
            tvSubject.setText(certificate.subject);

            boolean preferred = Objects.equals(email, certificate.email);
            tvEmail.setTypeface(preferred ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            tvSubject.setTypeface(preferred ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        }
    }

    AdapterCertificate(Fragment parentFragment) {
        this.context = parentFragment.getContext();
        this.owner = parentFragment.getViewLifecycleOwner();
        this.inflater = LayoutInflater.from(parentFragment.getContext());

        setHasStableIds(true);
    }

    public void set(String email, @NonNull List<EntityCertificate> certificates) {
        Log.i("Set email=" + email + " certificates=" + certificates.size());

        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffCallback(items, certificates), false);

        this.email = email;
        this.items = certificates;

        diff.dispatchUpdatesTo(new ListUpdateCallback() {
            @Override
            public void onInserted(int position, int count) {
                Log.i("Inserted @" + position + " #" + count);
            }

            @Override
            public void onRemoved(int position, int count) {
                Log.i("Removed @" + position + " #" + count);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                Log.i("Moved " + fromPosition + ">" + toPosition);
            }

            @Override
            public void onChanged(int position, int count, Object payload) {
                Log.i("Changed @" + position + " #" + count);
            }
        });
        diff.dispatchUpdatesTo(this);
    }

    private class DiffCallback extends DiffUtil.Callback {
        private List<EntityCertificate> prev = new ArrayList<>();
        private List<EntityCertificate> next = new ArrayList<>();

        DiffCallback(List<EntityCertificate> prev, List<EntityCertificate> next) {
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
            EntityCertificate c1 = prev.get(oldItemPosition);
            EntityCertificate c2 = next.get(newItemPosition);
            return c1.id.equals(c2.id);
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            EntityCertificate c1 = prev.get(oldItemPosition);
            EntityCertificate c2 = next.get(newItemPosition);
            return c1.equals(c2);
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
        return new ViewHolder(inflater.inflate(R.layout.item_certificate, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.unwire();

        EntityCertificate certificate = items.get(position);
        holder.bindTo(certificate);

        holder.wire();
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        holder.powner.recreate();
    }
}
