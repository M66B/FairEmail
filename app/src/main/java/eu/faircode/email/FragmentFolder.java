package eu.faircode.email;

/*
    This file is part of FairEmail.

    FairEmail is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    NetGuard is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with NetGuard.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2018 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

public class FragmentFolder extends FragmentEx {
    private ViewGroup view;
    private CheckBox cbSynchronize;
    private EditText etAfter;
    private Button btnSave;
    private ProgressBar pbSave;
    private ProgressBar pbWait;

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_edit_folder);

        view = (ViewGroup) inflater.inflate(R.layout.fragment_folder, container, false);

        // Get arguments
        Bundle args = getArguments();
        final long id = (args == null ? -1 : args.getLong("id"));

        // Get controls
        cbSynchronize = view.findViewById(R.id.cbSynchronize);
        etAfter = view.findViewById(R.id.etAfter);
        pbSave = view.findViewById(R.id.pbSave);
        btnSave = view.findViewById(R.id.btnSave);
        pbWait = view.findViewById(R.id.pbWait);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.setViewsEnabled(view, false);
                btnSave.setEnabled(false);
                pbSave.setVisibility(View.VISIBLE);

                Bundle args = new Bundle();
                args.putLong("id", id);
                args.putBoolean("synchronize", cbSynchronize.isChecked());
                args.putString("after", etAfter.getText().toString());

                new SimpleTask<Void>() {
                    @Override
                    protected Void onLoad(Context context, Bundle args) {
                        try {
                            ServiceSynchronize.stopSynchroneous(getContext(), "save folder");

                            long id = args.getLong("id");
                            boolean synchronize = args.getBoolean("synchronize");
                            String after = args.getString("after");
                            int days = (TextUtils.isEmpty(after) ? 7 : Integer.parseInt(after));

                            DB db = DB.getInstance(getContext());
                            try {
                                db.beginTransaction();

                                db.folder().setFolderProperties(id, synchronize, days);

                                EntityFolder folder = db.folder().getFolder(id);
                                if (!folder.synchronize)
                                    db.message().deleteMessages(folder.id);

                                db.setTransactionSuccessful();
                            } finally {
                                db.endTransaction();
                            }

                            return null;
                        } finally {
                            ServiceSynchronize.start(getContext());
                        }
                    }

                    @Override
                    protected void onLoaded(Bundle args, Void data) {
                        getFragmentManager().popBackStack();
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Helper.setViewsEnabled(view, true);
                        btnSave.setEnabled(true);
                        pbSave.setVisibility(View.GONE);

                        Toast.makeText(getContext(), ex.toString(), Toast.LENGTH_LONG).show();
                    }
                }.load(FragmentFolder.this, args);
            }
        });

        // Initialize
        Helper.setViewsEnabled(view, false);
        btnSave.setEnabled(false);
        pbSave.setVisibility(View.GONE);
        pbWait.setVisibility(View.VISIBLE);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Get arguments
        Bundle args = getArguments();
        long id = (args == null ? -1 : args.getLong("id"));

        // Observe
        DB.getInstance(getContext()).folder().liveFolder(id).observe(getViewLifecycleOwner(), new Observer<EntityFolder>() {
            @Override
            public void onChanged(@Nullable EntityFolder folder) {
                if (folder == null) {
                    getFragmentManager().popBackStack();
                    return;
                }

                cbSynchronize.setChecked(folder.synchronize);
                etAfter.setText(Integer.toString(folder.after));

                pbWait.setVisibility(View.GONE);
                Helper.setViewsEnabled(view, true);
                btnSave.setEnabled(true);
            }
        });
    }
}
