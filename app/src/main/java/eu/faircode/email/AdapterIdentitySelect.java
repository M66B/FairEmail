package eu.faircode.email;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class AdapterIdentitySelect extends ArrayAdapter<TupleIdentityEx> {
    private Context context;
    private List<TupleIdentityEx> identities;

    AdapterIdentitySelect(@NonNull Context context, List<TupleIdentityEx> identities) {
        super(context, 0, identities);
        this.context = context;
        this.identities = identities;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getLayout(position, convertView, parent, R.layout.spinner_identity);
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        return getLayout(position, convertView, parent, R.layout.spinner_identity_dropdown);
    }

    private View getLayout(int position, View convertView, ViewGroup parent, int resid) {
        View view = LayoutInflater.from(context).inflate(resid, parent, false);

        TupleIdentityEx identity = identities.get(position);

        View vwColor = view.findViewById(R.id.vwColor);
        TextView text1 = view.findViewById(android.R.id.text1);
        TextView text2 = view.findViewById(android.R.id.text2);

        vwColor.setBackgroundColor(identity.color == null ? Color.TRANSPARENT : identity.color);
        text1.setText(identity.getDisplayName() + (identity.primary ? " ★" : ""));
        text2.setText(identity.accountName + "/" + identity.email);

        return view;
    }
}
