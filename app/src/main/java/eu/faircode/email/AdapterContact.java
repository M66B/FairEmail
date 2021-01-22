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

    Copyright 2018-2021 by Marcel Bokhorst (M66B)
*/

import android.Manifest;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

import java.io.InputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class AdapterContact extends RecyclerView.Adapter<AdapterContact.ViewHolder> {
    private Fragment parentFragment;

    private Context context;
    private LifecycleOwner owner;
    private LayoutInflater inflater;
    private boolean contacts;
    private int colorSecondary;
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
                ivType.setImageResource(R.drawable.twotone_call_received_24);
                ivType.setContentDescription(context.getString(R.string.title_accessibility_from));
            } else if (contact.type == EntityContact.TYPE_TO) {
                ivType.setImageResource(R.drawable.twotone_call_made_24);
                ivType.setContentDescription(context.getString(R.string.title_accessibility_to));
            } else {
                ivType.setImageDrawable(null);
                ivType.setContentDescription(null);
            }

            if (contact.avatar == null || !contacts)
                ivAvatar.setImageDrawable(null);
            else {
                ContentResolver resolver = context.getContentResolver();
                Uri lookupUri = Uri.parse(contact.avatar);
                try (InputStream is = ContactsContract.Contacts.openContactPhotoInputStream(
                        resolver, lookupUri, false)) {
                    ivAvatar.setImageBitmap(BitmapFactory.decodeStream(is));
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }

            tvName.setText(contact.name == null ? contact.email : contact.name);
            tvEmail.setText(contact.accountName + "/" + contact.email);
            tvTimes.setText(NF.format(contact.times_contacted));
            tvLast.setText(contact.last_contacted == null ? null
                    : Helper.getRelativeTimeSpanString(context, contact.last_contacted));

            ivFavorite.setImageResource(contact.state == EntityContact.STATE_FAVORITE
                    ? R.drawable.twotone_star_24 : R.drawable.twotone_star_border_24);
            ivFavorite.setImageTintList(ColorStateList.valueOf(
                    contact.state == EntityContact.STATE_FAVORITE ? colorSecondary : textColorSecondary));
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
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R && // should be system whitelisted
                    share.resolveActivity(context.getPackageManager()) != null)
                popupMenu.getMenu().add(Menu.NONE, R.string.title_share, 2, R.string.title_share);
            if (ShortcutManagerCompat.isRequestPinShortcutSupported(context))
                popupMenu.getMenu().add(Menu.NONE, R.string.title_pin, 3, R.string.title_pin);
            popupMenu.getMenu().add(Menu.NONE, R.string.title_advanced_edit_name, 4, R.string.title_advanced_edit_name);
            popupMenu.getMenu().add(Menu.NONE, R.string.title_delete, 5, R.string.title_delete);

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
                        case R.string.title_pin:
                            onActionPin();
                            return true;
                        case R.string.title_advanced_edit_name:
                            onActionEdit();
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
                    } catch (ActivityNotFoundException ex) {
                        Log.w(ex);
                        ToastEx.makeText(context, context.getString(R.string.title_no_viewer, share), Toast.LENGTH_LONG).show();
                    }
                }

                private void onActionPin() {
                    ShortcutInfoCompat.Builder builder = Shortcuts.getShortcut(context, contact);
                    ShortcutManagerCompat.requestPinShortcut(context, builder.build(), null);
                }

                private void onActionEdit() {
                    Bundle args = new Bundle();
                    args.putLong("id", contact.id);
                    args.putString("name", contact.name);

                    FragmentEditName fragment = new FragmentEditName();
                    fragment.setArguments(args);
                    fragment.show(parentFragment.getParentFragmentManager(), "contact:edit");
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
        this.colorSecondary = Helper.resolveColor(context, R.attr.colorSecondary);
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

    private static class DiffCallback extends DiffUtil.Callback {
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
        TupleContactEx contact = selected.get(position);
        holder.powner.recreate(contact == null ? null : contact.id);

        holder.unwire();
        holder.bindTo(contact);
        holder.wire();
    }

    public static class FragmentEditName extends FragmentDialogBase {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_name, null);
            final EditText etName = view.findViewById(R.id.etName);
            etName.setText(getArguments().getString("name"));

            return new AlertDialog.Builder(getContext())
                    .setView(view)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Bundle args = new Bundle();
                            args.putLong("id", getArguments().getLong("id"));
                            args.putString("name", etName.getText().toString());

                            new SimpleTask<Void>() {
                                @Override
                                protected Void onExecute(Context context, Bundle args) {
                                    long id = args.getLong("id");
                                    String name = args.getString("name");

                                    if (TextUtils.isEmpty(name))
                                        name = null;

                                    DB db = DB.getInstance(context);
                                    db.contact().setContactName(id, name);

                                    return null;
                                }

                                @Override
                                protected void onException(Bundle args, Throwable ex) {
                                    Log.unexpectedError(getParentFragmentManager(), ex);
                                }
                            }.execute(getContext(), getActivity(), args, "edit:name");
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
        }
    }
}
