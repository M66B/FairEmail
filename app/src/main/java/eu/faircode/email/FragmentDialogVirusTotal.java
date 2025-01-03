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

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class FragmentDialogVirusTotal extends FragmentDialogBase {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        String apiKey = args.getString("apiKey");
        String name = args.getString("name");

        final Context context = getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_virus_total, null);
        final TextView tvName = view.findViewById(R.id.tvName);
        final TextView tvError = view.findViewById(R.id.tvError);
        final TextView tvUnknown = view.findViewById(R.id.tvUnknown);
        final TextView tvSummary = view.findViewById(R.id.tvSummary);
        final TextView tvLabel = view.findViewById(R.id.tvLabel);
        final TextView tvReport = view.findViewById(R.id.tvReport);
        final RecyclerView rvScan = view.findViewById(R.id.rvScan);
        final Button btnUpload = view.findViewById(R.id.btnUpload);
        final ProgressBar pbUpload = view.findViewById(R.id.pbUpload);
        final TextView tvAnalyzing = view.findViewById(R.id.tvAnalyzing);
        final TextView tvPrivacy = view.findViewById(R.id.tvPrivacy);
        final ProgressBar pbWait = view.findViewById(R.id.pbWait);

        tvName.setText(name);
        tvName.setVisibility(TextUtils.isEmpty(name) ? View.GONE : View.VISIBLE);
        tvError.setVisibility(View.GONE);
        tvUnknown.setVisibility(View.GONE);
        tvSummary.setVisibility(View.GONE);
        tvLabel.setVisibility(View.GONE);
        tvReport.setVisibility(View.GONE);
        tvReport.getPaint().setUnderlineText(true);

        rvScan.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rvScan.setLayoutManager(llm);

        final AdapterVirusTotal adapter = new AdapterVirusTotal(getContext(), getViewLifecycleOwner());
        rvScan.setAdapter(adapter);

        rvScan.setVisibility(View.GONE);

        btnUpload.setVisibility(View.GONE);
        pbUpload.setVisibility(View.GONE);
        tvAnalyzing.setVisibility(View.GONE);
        tvPrivacy.setVisibility(View.GONE);
        tvPrivacy.getPaint().setUnderlineText(true);
        pbWait.setVisibility(View.GONE);

        final SimpleTask<Bundle> taskLookup = new SimpleTask<Bundle>() {
            @Override
            protected void onPreExecute(Bundle args) {
                tvError.setVisibility(View.GONE);
                pbWait.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onPostExecute(Bundle args) {
                pbWait.setVisibility(View.GONE);
            }

            @Override
            protected Bundle onExecute(Context context, Bundle args) throws Throwable {
                String apiKey = args.getString("apiKey");
                File file = (File) args.getSerializable("file");
                return VirusTotal.lookup(context, file, apiKey);
            }

            @Override
            protected void onExecuted(Bundle args, Bundle result) {
                List<VirusTotal.ScanResult> scans = result.getParcelableArrayList("scans");
                String label = result.getString("label");
                String analysis = args.getString("analysis");

                int malicious = 0;
                if (scans != null)
                    for (VirusTotal.ScanResult scan : scans)
                        if ("malicious".equals(scan.category))
                            malicious++;

                NumberFormat NF = NumberFormat.getNumberInstance();

                tvUnknown.setVisibility(scans == null ? View.VISIBLE : View.GONE);
                tvSummary.setText(getString(R.string.title_vt_summary, NF.format(malicious)));
                tvSummary.setTextColor(Helper.resolveColor(context,
                        malicious == 0 ? android.R.attr.textColorPrimary : R.attr.colorWarning));
                tvSummary.setTypeface(malicious == 0 ? Typeface.DEFAULT : Typeface.DEFAULT_BOLD);
                tvSummary.setVisibility(scans == null ? View.GONE : View.VISIBLE);
                tvLabel.setText(label);
                tvReport.setVisibility(scans == null ? View.GONE : View.VISIBLE);
                adapter.set(scans == null ? new ArrayList<>() : scans);
                rvScan.setVisibility(scans == null ? View.GONE : View.VISIBLE);
                btnUpload.setVisibility(scans == null && !TextUtils.isEmpty(apiKey) ? View.VISIBLE : View.GONE);
                tvPrivacy.setVisibility(btnUpload.getVisibility());

                if (analysis != null && args.getBoolean("init")) {
                    args.remove("init");
                    btnUpload.callOnClick();
                }
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                tvError.setText(Log.formatThrowable(ex, false));
                tvError.setVisibility(View.VISIBLE);
            }
        };

        final SimpleTask<Void> taskUpload = new SimpleTask<Void>() {
            @Override
            protected void onPreExecute(Bundle args) {
                btnUpload.setEnabled(false);
                pbUpload.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onPostExecute(Bundle args) {
                btnUpload.setEnabled(true);
                tvAnalyzing.setVisibility(View.GONE);
                pbUpload.setVisibility(View.GONE);
            }

            @Override
            protected Void onExecute(Context context, Bundle args) throws Throwable {
                String apiKey = args.getString("apiKey");
                File file = (File) args.getSerializable("file");

                String analysis = args.getString("analysis");
                if (analysis == null) {
                    analysis = VirusTotal.upload(context, file, apiKey);
                    args.putString("analysis", analysis);
                }
                postProgress(analysis);
                VirusTotal.waitForAnalysis(context, analysis, apiKey);
                return null;
            }

            @Override
            protected void onProgress(CharSequence status, Bundle data) {
                tvAnalyzing.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onExecuted(Bundle args, Void data) {
                taskLookup.execute(FragmentDialogVirusTotal.this, args, "attachment:lookup");
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        };

        final SimpleTask<String> taskUrl = new SimpleTask<String>() {
            @Override
            protected String onExecute(Context context, Bundle args) throws Throwable {
                File file = (File) args.getSerializable("file");
                return VirusTotal.getUrl(file);
            }

            @Override
            protected void onExecuted(Bundle args, String uri) {
                Helper.view(context, Uri.parse(uri), true);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        };

        tvReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                taskUrl.execute(FragmentDialogVirusTotal.this, args, "attachment:report");
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                taskUpload.execute(FragmentDialogVirusTotal.this, args, "attachment:upload");
            }
        });

        tvPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.view(v.getContext(), Uri.parse(VirusTotal.URI_PRIVACY), true);
            }
        });

        if (TextUtils.isEmpty(apiKey))
            pbWait.setVisibility(View.GONE);
        else {
            args.putBoolean("init", true);
            taskLookup.execute(this, args, "attachment:lookup");
        }

        return new AlertDialog.Builder(context)
                .setView(view)
                .setNegativeButton(R.string.title_setup_done, null)
                .create();
    }
}
