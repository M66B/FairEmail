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

    Copyright 2018-2024 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.Group;
import androidx.lifecycle.LifecycleOwner;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
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
    private SharedPreferences prefs;

    private long[] ids;
    private List<TupleKeyword> all = new ArrayList<>();

    public class ViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {
        private View view;
        private CheckBox cbKeyword;
        private ImageButton ibEdit;
        private EditText etKeyword;
        private ImageButton ibSave;
        private ViewButtonColor btnColor;
        private Group grpNotEdit;
        private Group grpEdit;

        ViewHolder(View itemView) {
            super(itemView);

            view = itemView.findViewById(R.id.clItem);
            cbKeyword = itemView.findViewById(R.id.cbKeyword);
            ibEdit = itemView.findViewById(R.id.ibEdit);
            etKeyword = itemView.findViewById(R.id.etKeyword);
            ibSave = itemView.findViewById(R.id.ibSave);
            btnColor = itemView.findViewById(R.id.btnColor);
            grpNotEdit = itemView.findViewById(R.id.grpNotEdit);
            grpEdit = itemView.findViewById(R.id.grpEdit);
        }

        private void wire() {
            cbKeyword.setOnCheckedChangeListener(this);
            ibEdit.setOnClickListener(this);
            ibSave.setOnClickListener(this);
            btnColor.setOnClickListener(this);
        }

        private void unwire() {
            cbKeyword.setOnCheckedChangeListener(null);
            ibEdit.setOnClickListener(null);
            ibSave.setOnClickListener(null);
            btnColor.setOnClickListener(null);
        }

        private void bindTo(TupleKeyword keyword) {
            cbKeyword.setText(getTitle(keyword.name));
            cbKeyword.setChecked(keyword.selected);
            if (keyword.partial)
                cbKeyword.setButtonDrawable(R.drawable.ic_indeterminate);
            else
                cbKeyword.setButtonDrawable(keyword.selected ? R.drawable.ic_checked : R.drawable.ic_unchecked);
            btnColor.setColor(keyword.color, true);
            grpNotEdit.setVisibility(View.VISIBLE);
            grpEdit.setVisibility(View.GONE);
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return;

            TupleKeyword keyword = all.get(pos);
            keyword.selected = isChecked;
            cbKeyword.setButtonDrawable(keyword.selected ? R.drawable.ic_checked : R.drawable.ic_unchecked);

            Bundle args = new Bundle();
            args.putLongArray("ids", ids);
            args.putString("name", keyword.name);
            args.putBoolean("set", keyword.selected);

            new SimpleTask<Void>() {
                @Override
                protected Void onExecute(Context context, Bundle args) {
                    long[] ids = args.getLongArray("ids");
                    String name = args.getString("name");
                    boolean set = args.getBoolean("set");

                    DB db = DB.getInstance(context);

                    try {
                        db.beginTransaction();

                        if (ids != null)
                            for (long id : ids) {
                                EntityMessage message = db.message().getMessage(id);
                                if (message != null)
                                    EntityOperation.queue(context, message, EntityOperation.KEYWORD, name, set);
                            }

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

        @Override
        public void onClick(View view) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return;

            final TupleKeyword keyword = all.get(pos);

            int itemId = view.getId();
            if (itemId == R.id.ibEdit) {
                String key = "kwtitle." + keyword.name;
                etKeyword.setText(prefs.getString(key, null));
                etKeyword.setHint(keyword.name);
                grpNotEdit.setVisibility(View.GONE);
                grpEdit.setVisibility(View.VISIBLE);
                etKeyword.post(new Runnable() {
                    @Override
                    public void run() {
                        etKeyword.requestFocus();
                        Helper.showKeyboard(etKeyword);
                    }
                });
            } else if (itemId == R.id.ibSave) {
                updateTitle(keyword, etKeyword.getText().toString().trim());
                Helper.hideKeyboard(etKeyword);
                grpNotEdit.setVisibility(View.VISIBLE);
                grpEdit.setVisibility(View.GONE);
            } else if (itemId == R.id.btnColor) {
                int editTextColor = Helper.resolveColor(context, android.R.attr.editTextColor);

                ColorPickerDialogBuilder builder = ColorPickerDialogBuilder
                        .with(context)
                        .setTitle(context.getString(R.string.title_color))
                        .showColorEdit(true)
                        .setColorEditTextColor(editTextColor)
                        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                        .density(6)
                        .lightnessSliderOnly()
                        .setNegativeButton(R.string.title_reset, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                updateColor(keyword, null);
                            }
                        })
                        .setPositiveButton(android.R.string.ok, new ColorPickerClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                updateColor(keyword, selectedColor);
                            }
                        });

                if (keyword.color != null)
                    builder.initialColor(keyword.color);

                builder.build().show();
            }
        }

        private String getTitle(String keyword) {
            String keyTitle = "kwtitle." + keyword;
            String def = TupleKeyword.getDefaultKeywordAlias(context, keyword);
            return prefs.getString(keyTitle, def);
        }

        private void updateTitle(TupleKeyword keyword, String title) {
            String key = "kwtitle." + keyword.name;
            if (TextUtils.isEmpty(title))
                prefs.edit().remove(key).apply();
            else
                prefs.edit().putString(key, title).apply();

            cbKeyword.setText(getTitle(keyword.name));

            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
            lbm.sendBroadcast(new Intent(FragmentMessages.ACTION_KEYWORDS));
        }

        private void updateColor(TupleKeyword keyword, Integer color) {
            btnColor.setColor(color, true);
            keyword.color = color;

            String key = "kwcolor." + keyword.name;
            if (color == null)
                prefs.edit().remove(key).apply();
            else
                prefs.edit().putInt(key, keyword.color).apply();

            prefs.edit().remove("keyword." + keyword.name);

            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
            lbm.sendBroadcast(new Intent(FragmentMessages.ACTION_KEYWORDS));
        }
    }

    AdapterKeyword(Context context, LifecycleOwner owner) {
        this.context = context;
        this.owner = owner;
        this.inflater = LayoutInflater.from(context);
        this.prefs = PreferenceManager.getDefaultSharedPreferences(context);

        setHasStableIds(false);
    }

    public void set(long[] ids, @NonNull List<TupleKeyword> keywords) {
        Log.i("Set ids=" + ids.length + " keywords=" + keywords.size());

        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffCallback(all, keywords), false);

        this.ids = ids;
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

        try {
            diff.dispatchUpdatesTo(this);
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }


    private static class DiffCallback extends DiffUtil.Callback {
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
