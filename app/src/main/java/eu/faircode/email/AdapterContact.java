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

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.ContactsContract;
import android.text.SpannableString;
import android.text.TextUtils;
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
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class AdapterContact extends RecyclerView.Adapter<AdapterContact.ViewHolder> {
    private Fragment parentFragment;

    private Context context;
    private LifecycleOwner owner;
    private LayoutInflater inflater;
    private boolean contacts;
    private int colorAccent;
    private int textColorSecondary;

    private String search = null;
    private List<TupleContactEx> all = new ArrayList<>();
    private List<TupleContactEx> selected = new ArrayList<>();

    private NumberFormat NF = NumberFormat.getNumberInstance();

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private View view;
        private ImageView ivType;
        private ImageView ivAvatar;
        private TextView tvName;
        private TextView tvEmail;
        private TextView tvTimes;
        private TextView tvLast;
        private ImageView ivFavorite;

        private TwoStateOwner powner = new TwoStateOwner(owner, "ContactPopup");

        ViewHolder(View itemView) {
            super(itemView);

            view = itemView.findViewById(R.id.clItem);
            ivType = itemView.findViewById(R.id.ivType);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvTimes = itemView.findViewById(R.id.tvTimes);
            tvLast = itemView.findViewById(R.id.tvLast);
            ivFavorite = itemView.findViewById(R.id.ivFavorite);
        }

        private void wire() {
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        private void unwire() {
            view.setOnClickListener(null);
            view.setOnLongClickListener(null);
        }

        private void bindTo(TupleContactEx contact) {
            view.setAlpha(contact.state == EntityContact.STATE_IGNORE ? Helper.LOW_LIGHT : 1.0f);

            if (contact.type == EntityContact.TYPE_FROM) {
                ivType.setImageResource(R.drawable.baseline_call_received_24);
                ivType.setContentDescription(context.getString(R.string.title_accessibility_from));
            } else if (contact.type == EntityContact.TYPE_TO) {
                ivType.setImageResource(R.drawable.baseline_call_made_24);
                ivType.setContentDescription(context.getString(R.string.title_accessibility_to));
            } else {
                ivType.setImageDrawable(null);
                ivType.setContentDescription(null);
            }

            if (contact.avatar == null || !contacts)
                ivAvatar.setImageDrawable(null);
            else
                try {
                    Uri uri = Uri.parse(contact.avatar + "/photo");

                    /*
                        java.lang.NullPointerException: Attempt to invoke virtual method 'java.io.FileDescriptor android.content.res.AssetFileDescriptor.getFileDescriptor()' on a null object reference
                        java.lang.NullPointerException: Attempt to invoke virtual method 'java.io.FileDescriptor android.content.res.AssetFileDescriptor.getFileDescriptor()' on a null object reference
                        at android.graphics.ImageDecoder$ContentResolverSource.createImageDecoder(ImageDecoder.java:286)
                        at android.graphics.ImageDecoder.decodeDrawableImpl(ImageDecoder.java:1652)
                        at android.graphics.ImageDecoder.decodeDrawable(ImageDecoder.java:1645)
                        at android.widget.ImageView.getDrawableFromUri(ImageView.java:952)
                        at android.widget.ImageView.resolveUri(ImageView.java:921)

                        at android.widget.ImageView.setImageURI(ImageView.java:532)
                        at androidx.appcompat.widget.AppCompatImageView.setImageURI(SourceFile:116)

                        at android.widget.ImageView.onMeasure(ImageView.java:1056)
                        at android.view.View.measure(View.java:23188)
                        at androidx.constraintlayout.widget.ConstraintLayout$Measurer.measure(SourceFile:806)
                     */
                    ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                    if (pfd == null)
                        throw new IllegalArgumentException("Contact inaccessible");
                    pfd.close();

                    ivAvatar.setImageURI(uri);
                } catch (Throwable ex) {
                    Log.e(ex);
                    ivAvatar.setImageResource(R.drawable.baseline_broken_image_24);
                }

            tvName.setText(contact.name == null ? contact.email : contact.name);
            tvEmail.setText(contact.accountName + "/" + contact.email);
            tvTimes.setText(NF.format(contact.times_contacted));
            tvLast.setText(contact.last_contacted == null ? null
                    : Helper.getRelativeTimeSpanString(context, contact.last_contacted));

            ivFavorite.setImageResource(contact.state == EntityContact.STATE_FAVORITE
                    ? R.drawable.baseline_star_24 : R.drawable.baseline_star_border_24);
            ivFavorite.setImageTintList(ColorStateList.valueOf(
                    contact.state == EntityContact.STATE_FAVORITE ? colorAccent : textColorSecondary));
            ivFavorite.setContentDescription(contact.state == EntityContact.STATE_FAVORITE
                    ? context.getString(R.string.title_accessibility_flagged) : null);

            view.requestLayout();
        }

        @Override
        public void onClick(View view) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return;

            TupleContactEx contact = selected.get(pos);
            if (contact.state == EntityContact.STATE_DEFAULT)
                contact.state = EntityContact.STATE_FAVORITE;
            else
                contact.state = EntityContact.STATE_DEFAULT;

            notifyItemChanged(pos);

            Bundle args = new Bundle();
            args.putLong("id", contact.id);
            args.putInt("state", contact.state);

            new SimpleTask<Void>() {
                @Override
                protected Void onExecute(Context context, Bundle args) {
                    long id = args.getLong("id");
                    int state = args.getInt("state");

                    DB db = DB.getInstance(context);
                    db.contact().setContactState(id, state);

                    return null;
                }

                @Override
                protected void onExecuted(Bundle args, Void data) {
                    Shortcuts.update(context, owner);
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                }
            }.execute(context, owner, args, "contact:state");
        }

        @Override
        public boolean onLongClick(View view) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return false;

            final TupleContactEx contact = selected.get(pos);

            final Intent share = new Intent(Intent.ACTION_INSERT);
            share.setType(ContactsContract.Contacts.CONTENT_TYPE);
            share.putExtra(ContactsContract.Intents.Insert.NAME, contact.name);
            share.putExtra(ContactsContract.Intents.Insert.EMAIL, contact.email);

            PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(context, powner, view);

            SpannableString ss = new SpannableString(contact.email);
            ss.setSpan(new StyleSpan(Typeface.ITALIC), 0, ss.length(), 0);
            ss.setSpan(new RelativeSizeSpan(0.9f), 0, ss.length(), 0);
            popupMenu.getMenu().add(Menu.NONE, 0, 0, ss).setEnabled(false);

            if (contact.state != EntityContact.STATE_IGNORE)
                popupMenu.getMenu().add(Menu.NONE, R.string.title_advanced_never_favorite, 1, R.string.title_advanced_never_favorite);
            if (share.resolveActivity(context.getPackageManager()) != null)
                popupMenu.getMenu().add(Menu.NONE, R.string.title_share, 2, R.string.title_share);
            popupMenu.getMenu().add(Menu.NONE, R.string.title_delete, 3, R.string.title_delete);

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.string.title_advanced_never_favorite:
                            onActionNeverFavorite();
                            return true;
                        case R.string.title_share:
                            onActionShare();
                            return true;
                        case R.string.title_delete:
                            onActionDelete();
                            return true;
                        default:
                            return false;
                    }
                }

                private void onActionNeverFavorite() {
                    Bundle args = new Bundle();
                    args.putLong("id", contact.id);

                    new SimpleTask<Void>() {
                        @Override
                        protected Void onExecute(Context context, Bundle args) {
                            long id = args.getLong("id");

                            DB db = DB.getInstance(context);
                            db.contact().setContactState(id, EntityContact.STATE_IGNORE);

                            return null;
                        }

                        @Override
                        protected void onExecuted(Bundle args, Void data) {
                            Shortcuts.update(context, owner);
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                        }
                    }.execute(context, owner, args, "contact:favorite");
                }

                private void onActionShare() {
                    try {
                        context.startActivity(share);
                    } catch (Throwable ex) {
                        Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                    }
                }

                private void onActionDelete() {
                    Bundle args = new Bundle();
                    args.putLong("id", contact.id);

                    new SimpleTask<Void>() {
                        @Override
                        protected Void onExecute(Context context, Bundle args) {
                            long id = args.getLong("id");

                            DB db = DB.getInstance(context);
                            db.contact().deleteContact(id);

                            return null;
                        }

                        @Override
                        protected void onExecuted(Bundle args, Void data) {
                            Shortcuts.update(context, owner);
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
                        }
                    }.execute(context, owner, args, "contact:delete");
                }
            });

            popupMenu.show();

            return true;
        }
    }

    AdapterContact(Fragment parentFragment) {
        this.parentFragment = parentFragment;

        this.context = parentFragment.getContext();
        this.owner = parentFragment.getViewLifecycleOwner();
        this.inflater = LayoutInflater.from(context);

        this.contacts = Helper.hasPermission(context, Manifest.permission.READ_CONTACTS);
        this.colorAccent = Helper.resolveColor(context, R.attr.colorAccent);
        this.textColorSecondary = Helper.resolveColor(context, android.R.attr.textColorSecondary);

        setHasStableIds(true);

        owner.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            public void onDestroyed() {
                Log.d(AdapterContact.this + " parent destroyed");
                AdapterContact.this.parentFragment = null;
            }
        });
    }

    public void set(@NonNull List<TupleContactEx> contacts) {
        Log.i("Set contacts=" + contacts.size());

        all = contacts;

        List<TupleContactEx> items;
        if (TextUtils.isEmpty(search))
            items = all;
        else {
            items = new ArrayList<>();
            String query = search.toLowerCase().trim();
            for (TupleContactEx contact : contacts)
                if (contact.email.toLowerCase().contains(query) ||
                        (contact.name != null && contact.name.toLowerCase().contains(query)))
                    items.add(contact);
        }

        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffCallback(selected, items), false);

        selected = items;

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

    public void search(String query) {
        search = query;
        set(all);
    }

    private class DiffCallback extends DiffUtil.Callback {
        private List<TupleContactEx> prev = new ArrayList<>();
        private List<TupleContactEx> next = new ArrayList<>();

        DiffCallback(List<TupleContactEx> prev, List<TupleContactEx> next) {
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
            TupleContactEx c1 = prev.get(oldItemPosition);
            TupleContactEx c2 = next.get(newItemPosition);
            return c1.id.equals(c2.id);
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            TupleContactEx c1 = prev.get(oldItemPosition);
            TupleContactEx c2 = next.get(newItemPosition);
            return c1.equals(c2);
        }
    }

    @Override
    public long getItemId(int position) {
        return selected.get(position).id;
    }

    @Override
    public int getItemCount() {
        return selected.size();
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.item_contact, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.unwire();
        TupleContactEx contact = selected.get(position);
        holder.bindTo(contact);
        holder.wire();
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        holder.powner.recreate();
    }
}
