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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdapterAttachmentEML extends RecyclerView.Adapter<AdapterAttachmentEML.ViewHolder> {
    private LayoutInflater inflater;

    private IEML intf;
    private List<MessageHelper.AttachmentPart> aparts;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private View view;
        private TextView tvName;
        private TextView tvSize;
        private TextView tvType;
        private ImageButton ibSave;

        ViewHolder(View itemView) {
            super(itemView);

            view = itemView.findViewById(R.id.clItem);
            tvName = itemView.findViewById(R.id.tvName);
            tvSize = itemView.findViewById(R.id.tvSize);
            tvType = itemView.findViewById(R.id.tvType);
            ibSave = itemView.findViewById(R.id.ibSave);
        }

        private void wire() {
            view.setOnClickListener(this);
            ibSave.setOnClickListener(this);
        }

        private void unwire() {
            view.setOnClickListener(null);
            ibSave.setOnClickListener(null);
        }

        private void bindTo(MessageHelper.AttachmentPart apart) {
            tvName.setText(apart.attachment.name);

            if (apart.attachment.size != null)
                tvSize.setText(Helper.humanReadableByteCount(apart.attachment.size));
            tvSize.setVisibility(apart.attachment.size == null ? View.GONE : View.VISIBLE);

            StringBuilder sb = new StringBuilder();
            sb.append(apart.attachment.type);
            if (apart.attachment.disposition != null)
                sb.append(' ').append(apart.attachment.disposition);
            tvType.setText(sb.toString());
        }

        @Override
        public void onClick(View view) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return;

            MessageHelper.AttachmentPart apart = aparts.get(pos);
            if (apart != null)
                if (view.getId() == R.id.ibSave)
                    intf.onSave(apart);
                else
                    intf.onShare(apart);
        }
    }

    AdapterAttachmentEML(Context context, List<MessageHelper.AttachmentPart> aparts, IEML intf) {
        this.inflater = LayoutInflater.from(context);
        this.aparts = aparts;
        this.intf = intf;

        setHasStableIds(false);
    }

    @Override
    public int getItemCount() {
        return aparts.size();
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.item_attachment_eml, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.unwire();

        MessageHelper.AttachmentPart apart = aparts.get(position);
        holder.bindTo(apart);

        holder.wire();
    }

    interface IEML {
        void onShare(MessageHelper.AttachmentPart apart);

        void onSave(MessageHelper.AttachmentPart apart);
    }
}
