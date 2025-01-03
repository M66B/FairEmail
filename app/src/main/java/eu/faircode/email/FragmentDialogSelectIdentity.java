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

import static android.app.Activity.RESULT_OK;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.Group;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FragmentDialogSelectIdentity extends FragmentDialogBase {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Context context = getContext();

        final Bundle args = getArguments();

        final View dview = LayoutInflater.from(context).inflate(R.layout.dialog_account_select, null);
        RecyclerView rvSelect = dview.findViewById(R.id.rvSelect);
        final ContentLoadingProgressBar pbWait = dview.findViewById(R.id.pbWait);
        final Group grpReady = dview.findViewById(R.id.grpReady);

        rvSelect.setHasFixedSize(false);
        rvSelect.setLayoutManager(new LinearLayoutManager(context));

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setIcon(R.drawable.twotone_person_24)
                .setTitle(R.string.title_list_identities)
                .setView(dview)
                .setNegativeButton(android.R.string.cancel, null);

        if (args != null && args.getBoolean("add"))
            builder.setNeutralButton(R.string.title_add, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    args.putLong("id", -1L);
                    sendResult(RESULT_OK);
                }
            });

        Dialog dialog = builder.create();

        new SimpleTask<List<TupleIdentityEx>>() {
            @Override
            protected void onPreExecute(Bundle args) {
                pbWait.setVisibility(View.VISIBLE);
                grpReady.setVisibility(View.GONE);
            }

            @Override
            protected void onPostExecute(Bundle args) {
                pbWait.setVisibility(View.GONE);
                grpReady.setVisibility(View.VISIBLE);
            }

            @Override
            protected List<TupleIdentityEx> onExecute(Context context, Bundle args) {
                DB db = DB.getInstance(context);
                return db.identity().getComposableIdentities(null);
            }

            @Override
            protected void onExecuted(Bundle args, List<TupleIdentityEx> identities) {
                AdapterIdentity adapter = new AdapterIdentity(context, identities, new AdapterIdentity.IListener() {
                    @Override
                    public void onSelected(TupleIdentityEx identity) {
                        Bundle args = getArguments();
                        args.putLong("id", identity.id);
                        args.putString("html", identity.signature);
                        sendResult(RESULT_OK);
                        dialog.dismiss();
                    }
                });
                rvSelect.setAdapter(adapter);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, getArguments(), "select:identity");

        return dialog;
    }

    public static class AdapterIdentity extends RecyclerView.Adapter<AdapterIdentity.ViewHolder> {
        private Context context;
        private LayoutInflater inflater;

        private int dp6;
        private int dp12;
        private List<TupleIdentityEx> items;
        private IListener listener;

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private View vwColor;
            private TextView text1;
            private TextView text2;

            ViewHolder(View itemView) {
                super(itemView);
                vwColor = itemView.findViewById(R.id.vwColor);
                text1 = itemView.findViewById(android.R.id.text1);
                text2 = itemView.findViewById(android.R.id.text2);
            }

            private void wire() {
                itemView.setOnClickListener(this);
            }

            private void unwire() {
                itemView.setOnClickListener(null);
            }

            private void bindTo(TupleIdentityEx identity) {
                int vpad = (getItemCount() > 10 ? dp6 : dp12);
                text1.setPadding(0, vpad, 0, 0);
                text2.setPadding(0, 0, 0, vpad);

                Integer color = (identity.color == null ? identity.accountColor : identity.color);
                vwColor.setBackgroundColor(color == null ? Color.TRANSPARENT : color);

                String name = identity.getDisplayName();
                text1.setText(TextUtils.isEmpty(name) ? "-" : name);
                text2.setText(identity.email);
            }

            @Override
            public void onClick(View v) {
                int pos = getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION)
                    return;

                TupleIdentityEx identity = items.get(pos);
                listener.onSelected(identity);
            }
        }

        AdapterIdentity(Context context, List<TupleIdentityEx> identities, IListener listener) {
            this.context = context;
            this.inflater = LayoutInflater.from(context);
            this.dp6 = Helper.dp2pixels(context, 6);
            this.dp12 = Helper.dp2pixels(context, 12);

            setHasStableIds(true);
            this.items = identities;
            this.listener = listener;
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
            return new ViewHolder(inflater.inflate(R.layout.item_identity_select, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.unwire();
            TupleIdentityEx identity = items.get(position);
            holder.bindTo(identity);
            holder.wire();
        }

        public interface IListener {
            void onSelected(TupleIdentityEx identity);
        }
    }
}
