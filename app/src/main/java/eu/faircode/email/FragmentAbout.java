package eu.faircode.email;

/*
    This file is part of Safe email.

    Safe email is free software: you can redistribute it and/or modify
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

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class FragmentAbout extends FragmentEx {
    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.menu_about);

        View view = inflater.inflate(R.layout.fragment_about, container, false);

        TextView tvVersion = view.findViewById(R.id.tvVersion);
        final Button btnDebugInfo = view.findViewById(R.id.btnDebugInfo);

        tvVersion.setText(getString(R.string.title_version, BuildConfig.VERSION_NAME));

        btnDebugInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnDebugInfo.setEnabled(false);
                new SimpleLoader<Long>() {
                    @Override
                    public Long onLoad(Bundle args) throws UnsupportedEncodingException {
                        DB db = DB.getInstance(getContext());

                        EntityFolder drafts = db.folder().getPrimaryDrafts();
                        if (drafts == null)
                            throw new IllegalArgumentException(getString(R.string.title_no_drafts));

                        StringBuilder info = Helper.getDebugInfo();
                        info.insert(0, getString(R.string.title_debug_info_remark) + "\n\n\n\n");

                        Address to = new InternetAddress("marcel+email@faircode.eu", "FairCode");

                        EntityMessage draft = new EntityMessage();
                        draft.account = drafts.account;
                        draft.folder = drafts.id;
                        draft.to = new Address[]{to};
                        draft.subject = BuildConfig.APPLICATION_ID + " debug info";
                        draft.body = "<pre>" + info.toString().replaceAll("\\r?\\n", "<br />") + "</pre>";
                        draft.received = new Date().getTime();
                        draft.seen = false;
                        draft.ui_seen = false;
                        draft.ui_hide = false;
                        draft.id = db.message().insertMessage(draft);

                        return draft.id;
                    }

                    @Override
                    public void onLoaded(Bundle args, Long id) {
                        btnDebugInfo.setEnabled(true);
                        startActivity(new Intent(getContext(), ActivityCompose.class)
                                .putExtra("action", "edit")
                                .putExtra("id", id));
                    }

                    @Override
                    public void onException(Bundle args, Throwable ex) {
                        btnDebugInfo.setEnabled(true);
                        Toast.makeText(getContext(), ex.toString(), Toast.LENGTH_LONG).show();
                    }
                }.load(FragmentAbout.this, ActivityView.LOADER_DEBUG_INFO, new Bundle());
            }
        });

        return view;
    }
}
