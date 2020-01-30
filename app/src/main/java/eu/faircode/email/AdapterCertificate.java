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

    Copyright 2018-2020 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class AdapterCertificate extends RecyclerView.Adapter<AdapterCertificate.ViewHolder> {
    private Context context;
    private LifecycleOwner owner;
    private LayoutInflater inflater;

    private List<EntityCertificate> items = new ArrayList<>();

    private DateFormat TF;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        private View view;
        private TextView tvEmail;
        private ImageView ivIntermediate;
        private TextView tvSubject;
        private TextView tvAfter;
        private TextView tvBefore;
        private TextView tvExpired;

        private TwoStateOwner powner = new TwoStateOwner(owner, "CertificatePopup");

        ViewHolder(View itemView) {
            super(itemView);

            view = itemView.findViewById(R.id.clItem);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            ivIntermediate = itemView.findViewById(R.id.ivIntermediate);
            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvAfter = itemView.findViewById(R.id.tvAfter);
            tvBefore = itemView.findViewById(R.id.tvBefore);
            tvExpired = itemView.findViewById(R.id.tvExpired);
        }

        @Override
        public boolean onLongClick(View v) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return false;

            final EntityCertificate certificate = items.get(pos);

            PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(context, powner, view);

            SpannableString ss = new SpannableString(certificate.email);
            ss.setSpan(new StyleSpan(Typeface.ITALIC), 0, ss.length(), 0);
            ss.setSpan(new RelativeSizeSpan(0.9f), 0, ss.length(), 0);
            popupMenu.getMenu().add(Menu.NONE, 0, 0, ss).setEnabled(false);

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
            ivIntermediate.setVisibility(certificate.intermediate ? View.VISIBLE : View.INVISIBLE);
            tvSubject.setText(certificate.subject);
            tvAfter.setText(certificate.after == null ? null : TF.format(certificate.after));
            tvBefore.setText(certificate.before == null ? null : TF.format(certificate.before));
            tvExpired.setVisibility(certificate.isExpired() ? View.VISIBLE : View.GONE);
        }
    }

    AdapterCertificate(Fragment parentFragment) {
        this.context = parentFragment.getContext();
        this.owner = parentFragment.getViewLifecycleOwner();
        this.inflater = LayoutInflater.from(parentFragment.getContext());

        this.TF = Helper.getDateTimeInstance(context, SimpleDateFormat.SHORT, SimpleDateFormat.SHORT);

        setHasStableIds(true);
    }

    public void set(@NonNull List<EntityCertificate> certificates) {
        Log.i("Set certificates=" + certificates.size());

        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffCallback(items, certificates), false);

        this.items = certificates;

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
