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

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

public class AdapterContact extends RecyclerView.Adapter<AdapterContact.ViewHolder> {
    private Fragment parentFragment;

    private Context context;
    private LifecycleOwner owner;
    private LayoutInflater inflater;
    private boolean contacts;
    private int colorAccent;
    private int textColorSecondary;

    private String search = null;
    private Long account;
    private List<Integer> types = new ArrayList<>();

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

        Rect getItemRect() {
            return new Rect(
                    super.itemView.getLeft(),
                    super.itemView.getTop(),
                    super.itemView.getRight(),
                    super.itemView.getBottom());
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
            } else if (contact.type == EntityContact.TYPE_JUNK) {
                ivType.setImageResource(R.drawable.twotone_report_24);
                ivType.setContentDescription(context.getString(R.string.title_legend_junk));
            } else if (contact.type == EntityContact.TYPE_NO_JUNK) {
                ivType.setImageResource(R.drawable.twotone_report_off_24);
                ivType.setContentDescription(context.getString(R.string.title_no_junk));
            } else {
                ivType.setImageDrawable(null);
                ivType.setContentDescription(null);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                ivType.setTooltipText(ivType.getContentDescription());

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

            tvName.setText(contact.name == null ? "-" : contact.name + (contact.group == null ? "" : "/" + contact.group));
            tvEmail.setText(contact.email + "/" + contact.accountName);
            tvTimes.setText(NF.format(contact.times_contacted));
            tvLast.setText(contact.last_contacted == null ? null
                    : Helper.getRelativeTimeSpanString(context, contact.last_contacted));

            ivFavorite.setImageResource(contact.state == EntityContact.STATE_FAVORITE
                    ? R.drawable.baseline_star_24 : R.drawable.twotone_star_border_24);
            ivFavorite.setImageTintList(ColorStateList.valueOf(
                    contact.state == EntityContact.STATE_FAVORITE ? colorAccent : textColorSecondary));
            ivFavorite.setContentDescription(contact.state == EntityContact.STATE_FAVORITE
                    ? context.getString(R.string.title_accessibility_flagged) : null);
            ivFavorite.setVisibility(contact.type == EntityContact.TYPE_JUNK ||
                    contact.type == EntityContact.TYPE_NO_JUNK
                    ? View.GONE : View.VISIBLE);

            //view.requestLayout();
        }

        @Override
        public void onClick(View view) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return;

            TupleContactEx contact = selected.get(pos);
            if (contact.state == EntityContact.STATE_DEFAULT)
                contact.state = EntityContact.STATE_FAVORITE;
            else if (contact.state == EntityContact.STATE_FAVORITE)
                contact.state = EntityContact.STATE_IGNORE;
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

            int order = 0;
            SpannableString ss = new SpannableString(contact.email);
            ss.setSpan(new StyleSpan(Typeface.ITALIC), 0, ss.length(), 0);
            ss.setSpan(new RelativeSizeSpan(0.9f), 0, ss.length(), 0);
            popupMenu.getMenu().add(Menu.NONE, 0, order++, ss).setEnabled(false);

            if (!TextUtils.isEmpty(contact.identityEmail)) {
                String via = context.getString(R.string.title_via, contact.identityEmail);
                popupMenu.getMenu().add(Menu.NONE, 0, order++, via).setEnabled(false);
            }

            if (contact.state != EntityContact.STATE_IGNORE &&
                    contact.type != EntityContact.TYPE_JUNK &&
                    contact.type != EntityContact.TYPE_NO_JUNK)
                popupMenu.getMenu().add(Menu.NONE, R.string.title_advanced_never_favorite, order++, R.string.title_advanced_never_favorite);
            popupMenu.getMenu().add(Menu.NONE, R.string.title_share, order++, R.string.title_share); // should be system whitelisted
            popupMenu.getMenu().add(Menu.NONE, R.string.title_clipboard_copy, order++, R.string.title_clipboard_copy); // should be system whitelisted
            if (Shortcuts.can(context))
                popupMenu.getMenu().add(Menu.NONE, R.string.title_pin, order++, R.string.title_pin);
            popupMenu.getMenu().add(Menu.NONE, R.string.title_edit_contact, order++, R.string.title_edit_contact);
            popupMenu.getMenu().add(Menu.NONE, R.string.title_search, order++, R.string.title_search);
            popupMenu.getMenu().add(Menu.NONE, R.string.title_delete, order++, R.string.title_delete);

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int itemId = item.getItemId();
                    if (itemId == R.string.title_advanced_never_favorite) {
                        onActionNeverFavorite();
                        return true;
                    } else if (itemId == R.string.title_share) {
                        onActionShare();
                        return true;
                    } else if (itemId == R.string.title_clipboard_copy) {
                        onActionCopy();
                        return true;
                    } else if (itemId == R.string.title_pin) {
                        onActionPin();
                        return true;
                    } else if (itemId == R.string.title_edit_contact) {
                        onActionEdit();
                        return true;
                    } else if (itemId == R.string.title_search) {
                        onActionSearch();
                        return true;
                    } else if (itemId == R.string.title_delete) {
                        onActionDelete();
                        return true;
                    }
                    return false;
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
                        Helper.reportNoViewer(context, share, ex);
                    }
                }

                private void onActionCopy() {
                    ClipboardManager clipboard = Helper.getSystemService(context, ClipboardManager.class);
                    if (clipboard == null)
                        return;

                    String copy;
                    try {
                        InternetAddress address = new InternetAddress(contact.email, contact.name, StandardCharsets.UTF_8.name());
                        copy = MessageHelper.formatAddresses(new Address[]{address});
                    } catch (UnsupportedEncodingException ex) {
                        Log.e(ex);
                        copy = contact.email;
                    }

                    ClipData clip = ClipData.newPlainText(context.getString(R.string.app_name), copy);
                    clipboard.setPrimaryClip(clip);

                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
                        ToastEx.makeText(context, R.string.title_clipboard_copied, Toast.LENGTH_LONG).show();
                }

                private void onActionPin() {
                    ShortcutInfoCompat.Builder builder = Shortcuts.getShortcut(context, contact);
                    Shortcuts.requestPinShortcut(context, builder.build());
                }

                private void onActionEdit() {
                    Bundle args = new Bundle();
                    args.putLong("id", contact.id);
                    args.putLong("account", contact.account);
                    args.putInt("type", contact.type);
                    args.putString("email", contact.email);
                    args.putString("name", contact.name);
                    args.putString("group", contact.group);

                    FragmentDialogContactEdit fragment = new FragmentDialogContactEdit();
                    fragment.setArguments(args);
                    fragment.setTargetFragment(parentFragment, FragmentContacts.REQUEST_EDIT_CONTACT);
                    fragment.show(parentFragment.getParentFragmentManager(), "contact:edit");
                }

                private void onActionSearch() {
                    Intent search = new Intent(context, ActivityView.class)
                            .putExtra(Intent.EXTRA_PROCESS_TEXT, contact.email)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    context.startActivity(search);
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
        this.colorAccent = Helper.resolveColor(context, androidx.appcompat.R.attr.colorAccent);
        this.textColorSecondary = Helper.resolveColor(context, android.R.attr.textColorSecondary);

        setHasStableIds(true);

        owner.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            public void onDestroyed() {
                Log.d(AdapterContact.this + " parent destroyed");
                AdapterContact.this.parentFragment = null;
                owner.getLifecycle().removeObserver(this);
            }
        });
    }

    public void set(@NonNull List<TupleContactEx> contacts) {
        Log.i("Set contacts=" + contacts.size() +
                " search=" + search +
                " account=" + account +
                " types=" + types.size());

        all = contacts;

        new SimpleTask<List<TupleContactEx>>() {
            @Override
            protected List<TupleContactEx> onExecute(Context context, Bundle args) {
                List<TupleContactEx> filtered;
                if (account == null && types.size() == 0)
                    filtered = contacts;
                else {
                    filtered = new ArrayList<>();
                    for (TupleContactEx contact : contacts)
                        if ((account == null || contact.account.equals(account)) &&
                                (types.size() == 0 || types.contains(contact.type)))
                            filtered.add(contact);
                }

                List<TupleContactEx> items;
                if (TextUtils.isEmpty(search))
                    items = filtered;
                else {
                    items = new ArrayList<>();
                    String query = search.toLowerCase().trim();
                    for (TupleContactEx contact : filtered)
                        if (contact.email.toLowerCase().contains(query) ||
                                (contact.name != null && contact.name.toLowerCase().contains(query)))
                            items.add(contact);
                }

                return items;
            }

            @Override
            protected void onExecuted(Bundle args, List<TupleContactEx> items) {
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

                try {
                    diff.dispatchUpdatesTo(AdapterContact.this);
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(parentFragment.getParentFragmentManager(), ex);
            }
        }.serial().execute(context, owner, new Bundle(), "contacts:filter");
    }

    public void search(String query) {
        Log.i("Contacts query=" + query);
        search = query;
        set(all);
    }

    public void filter(Long account, boolean junk) {
        this.account = account;
        this.types = junk
                ? Arrays.asList(EntityContact.TYPE_JUNK, EntityContact.TYPE_NO_JUNK)
                : Arrays.asList(EntityContact.TYPE_TO, EntityContact.TYPE_FROM);
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
        if (position < 0 || position >= selected.size())
            return -1L;
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
}
