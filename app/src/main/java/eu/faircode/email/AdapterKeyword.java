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
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class AdapterKeyword extends RecyclerView.Adapter<AdapterKeyword.ViewHolder> {
    private Context context;
    private LifecycleOwner owner;
    private LayoutInflater inflater;
    private boolean pro;

    private long id;
    private List<TupleKeyword> all = new ArrayList<>();

    public class ViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {
        private View view;
        private CheckBox cbKeyword;
        private ViewButtonColor btnColor;

        ViewHolder(View itemView) {
            super(itemView);

            view = itemView.findViewById(R.id.clItem);
            cbKeyword = itemView.findViewById(R.id.cbKeyword);
            btnColor = itemView.findViewById(R.id.btnColor);
        }

        private void wire() {
            cbKeyword.setOnCheckedChangeListener(this);
            btnColor.setOnClickListener(this);
        }

        private void unwire() {
            cbKeyword.setOnCheckedChangeListener(null);
            btnColor.setOnClickListener(null);
        }

        private void bindTo(TupleKeyword keyword) {
            cbKeyword.setText(keyword.name);
            cbKeyword.setChecked(keyword.selected);
            cbKeyword.setEnabled(pro);
            btnColor.setColor(keyword.color);
            btnColor.setEnabled(pro);
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return;

            TupleKeyword keyword = all.get(pos);
            keyword.selected = isChecked;

            Bundle args = new Bundle();
            args.putLong("id", id);
            args.putString("name", keyword.name);
            args.putBoolean("set", keyword.selected);

            new SimpleTask<Void>() {
                @Override
                protected Void onExecute(Context context, Bundle args) {
                    long id = args.getLong("id");
                    String name = args.getString("name");
                    boolean set = args.getBoolean("set");

                    DB db = DB.getInstance(context);

                    EntityMessage message = db.message().getMessage(id);
                    if (message == null)
                        return null;

                    EntityOperation.queue(context, message, EntityOperation.KEYWORD, name, set);

                    return null;
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.e(ex);
                }
            }.execute(context, owner, args, "keyword:set");
        }

        @Override
        public void onClick(View view) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return;

            final TupleKeyword keyword = all.get(pos);

            ColorPickerDialogBuilder builder = ColorPickerDialogBuilder
                    .with(context)
                    .setTitle(context.getString(R.string.title_color))
                    .showColorEdit(true)
                    .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                    .density(6)
                    .lightnessSliderOnly()
                    .setNegativeButton(R.string.title_reset, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            update(keyword, null);
                        }
                    })
                    .setPositiveButton(android.R.string.ok, new ColorPickerClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                            update(keyword, selectedColor);
                        }
                    });

            if (keyword.color != null)
                builder.initialColor(keyword.color);

            builder.build().show();
        }

        private void update(TupleKeyword keyword, Integer color) {
            btnColor.setColor(color);
            keyword.color = color;

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            if (color == null)
                prefs.edit().remove("keyword." + keyword.name).apply();
            else
                prefs.edit().putInt("keyword." + keyword.name, keyword.color).apply();

            Bundle args = new Bundle();
            args.putLong("id", id);

            new SimpleTask<Void>() {
                @Override
                protected Void onExecute(Context context, Bundle args) {
                    long id = args.getLong("id");

                    DB db = DB.getInstance(context);

                    EntityMessage message = db.message().getMessage(id);
                    if (message == null)
                        return null;

                    // Update keyword colors
                    try {
                        db.beginTransaction();

                        db.message().setMessageKeywords(message.id, DB.Converters.fromStringArray(null));
                        db.message().setMessageKeywords(message.id, DB.Converters.fromStringArray(message.keywords));

                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }

                    return null;
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.e(ex);
                }
            }.execute(context, owner, args, "keyword:set");
        }
    }

    AdapterKeyword(Context context, LifecycleOwner owner) {
        this.context = context;
        this.owner = owner;
        this.inflater = LayoutInflater.from(context);
        this.pro = ActivityBilling.isPro(context);

        setHasStableIds(false);
    }

    public void set(long id, @NonNull List<TupleKeyword> keywords) {
        Log.i("Set id=" + id + " keywords=" + keywords.size());

        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffCallback(all, keywords), false);

        this.id = id;
        this.all = keywords;

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
        private List<TupleKeyword> prev = new ArrayList<>();
        private List<TupleKeyword> next = new ArrayList<>();

        DiffCallback(List<TupleKeyword> prev, List<TupleKeyword> next) {
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
            TupleKeyword k1 = prev.get(oldItemPosition);
            TupleKeyword k2 = next.get(newItemPosition);
            return k1.name.equals(k2.name);
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            TupleKeyword k1 = prev.get(oldItemPosition);
            TupleKeyword k2 = next.get(newItemPosition);
            return k1.equals(k2);
        }
    }

    @Override
    public int getItemCount() {
        return all.size();
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.item_keyword, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.unwire();
        TupleKeyword contact = all.get(position);
        holder.bindTo(contact);
        holder.wire();
    }
}
