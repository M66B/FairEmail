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
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Base64;
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
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class AdapterCertificate extends RecyclerView.Adapter<AdapterCertificate.ViewHolder> {
    private Fragment parentFragment;

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
        private TextView tvKeyUsage;
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
            tvKeyUsage = itemView.findViewById(R.id.tvKeyUsage);
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

            popupMenu.getMenu().add(Menu.NONE, R.string.title_share, 1, R.string.title_share);
            if (!Helper.isPlayStoreInstall())
                popupMenu.getMenu().add(Menu.NONE, R.string.title_analyze, 2, R.string.title_analyze);
            popupMenu.getMenu().add(Menu.NONE, R.string.title_delete, 3, R.string.title_delete);

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int id = item.getItemId();
                    if (id == R.string.title_share) {
                        onActionShare();
                        return true;
                    } else if (id == R.string.title_analyze) {
                        onActionAnalyze();
                        return true;
                    } else if (id == R.string.title_delete) {
                        onActionDelete();
                        return true;
                    }
                    return false;
                }

                private void onActionShare() {
                    Bundle args = new Bundle();
                    args.putLong("id", certificate.id);

                    new SimpleTask<File>() {
                        @Override
                        protected File onExecute(Context context, Bundle args) throws CertificateException, IOException {
                            long id = args.getLong("id");

                            DB db = DB.getInstance(context);
                            EntityCertificate certificate = db.certificate().getCertificate(id);
                            if (certificate == null)
                                return null;

                            File dir = Helper.ensureExists(context, "shared");
                            String name = Helper.sanitizeFilename(certificate.email);
                            File file = new File(dir, name + ".pem");
                            Helper.writeText(file, certificate.getPem());

                            return file;
                        }

                        @Override
                        protected void onExecuted(Bundle args, File file) {
                            if (file == null)
                                return;
                            // application/x-pem-file is generally unsupported
                            Helper.share(context, file, "application/*", file.getName());
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                        }
                    }.execute(context, owner, args, "certificate:share");
                }

                private void onActionAnalyze() {
                    Bundle args = new Bundle();
                    args.putLong("id", certificate.id);

                    new SimpleTask<String>() {
                        @Override
                        protected String onExecute(Context context, Bundle args) throws CertificateException, IOException {
                            long id = args.getLong("id");

                            DB db = DB.getInstance(context);
                            EntityCertificate certificate = db.certificate().getCertificate(id);
                            if (certificate == null)
                                return null;

                            return Base64.encodeToString(certificate.getCertificate().getEncoded(), Base64.URL_SAFE);
                        }

                        @Override
                        protected void onExecuted(Bundle args, String cert) {
                            Helper.view(context, Uri.parse("https://lapo.it/asn1js/#" + cert), true);
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                        }
                    }.execute(context, owner, args, "certificate:share");
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
                            Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
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
            tvEmail.setTypeface(certificate.intermediate ? Typeface.DEFAULT : Typeface.DEFAULT_BOLD);
            ivIntermediate.setVisibility(certificate.intermediate ? View.VISIBLE : View.INVISIBLE);

            String subject = certificate.subject;
            String algo = certificate.getSigAlgName();
            if (algo != null)
                subject = algo.replaceAll("(?i)With", "/") + " " + subject;
            tvSubject.setText(subject);

            List<String> keyUsage = certificate.getKeyUsage();
            StringBuilder sb = new StringBuilder();
            if (certificate.isSelfSigned())
                sb.append("(selfSigned)");
            for (String usage : keyUsage) {
                if (sb.length() > 0)
                    sb.append(' ');
                sb.append('(').append(usage).append(')');
            }
            tvKeyUsage.setText(sb.toString());
            tvKeyUsage.setVisibility(sb.length() > 0 ? View.VISIBLE : View.GONE);

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

        owner.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            public void onDestroyed() {
                Log.d(AdapterCertificate.this + " parent destroyed");
                AdapterCertificate.this.parentFragment = null;
                owner.getLifecycle().removeObserver(this);
            }
        });
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

        try {
            diff.dispatchUpdatesTo(this);
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    private static class DiffCallback extends DiffUtil.Callback {
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
        EntityCertificate certificate = items.get(position);
        holder.powner.recreate(certificate == null ? null : certificate.id);

        holder.unwire();
        holder.bindTo(certificate);
        holder.wire();
    }
}
