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

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.provider.Settings;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.Group;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.SortedMap;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import io.requery.android.database.sqlite.SQLiteDatabase;

public class FragmentOptionsMisc extends FragmentBase implements SharedPreferences.OnSharedPreferenceChangeListener {
    private boolean resumed = false;
    private List<Pair<String, String>> languages = new ArrayList<>();

    private SwitchCompat swExternalSearch;
    private SwitchCompat swShortcuts;
    private SwitchCompat swFts;
    private SwitchCompat swClassification;
    private TextView tvClassMinProbability;
    private SeekBar sbClassMinProbability;
    private TextView tvClassMinDifference;
    private SeekBar sbClassMinDifference;
    private ImageButton ibClassification;
    private TextView tvFtsIndexed;
    private TextView tvFtsPro;
    private Spinner spLanguage;
    private ImageButton ibResetLanguage;
    private SwitchCompat swWatchdog;
    private SwitchCompat swUpdates;
    private SwitchCompat swExperiments;
    private TextView tvExperimentsHint;
    private SwitchCompat swCrashReports;
    private TextView tvUuid;
    private Button btnReset;
    private SwitchCompat swCleanupAttachments;
    private Button btnCleanup;
    private TextView tvLastCleanup;
    private Button btnApp;
    private Button btnMore;

    private SwitchCompat swProtocol;
    private SwitchCompat swLogInfo;
    private SwitchCompat swDebug;
    private SwitchCompat swQueries;
    private SwitchCompat swWal;
    private SwitchCompat swExpunge;
    private SwitchCompat swAuthPlain;
    private SwitchCompat swAuthLogin;
    private SwitchCompat swAuthNtlm;
    private SwitchCompat swAuthSasl;
    private TextView tvProcessors;
    private TextView tvMemoryClass;
    private TextView tvMemoryUsage;
    private TextView tvStorageUsage;
    private TextView tvFingerprint;
    private Button btnGC;
    private Button btnCharsets;
    private Button btnCiphers;
    private Button btnFiles;

    private Group grpDebug;

    private NumberFormat NF = NumberFormat.getNumberInstance();

    private final static long MIN_FILE_SIZE = 1024 * 1024L;

    private final static String[] RESET_OPTIONS = new String[]{
            "shortcuts", "fts",
            "classification", "class_min_probability", "class_min_difference",
            "language", "watchdog", "updates",
            "experiments", "wal", "query_threads", "crash_reports", "cleanup_attachments",
            "protocol", "debug", "log_level", "perform_expunge", "auth_plain", "auth_login", "auth_ntlm", "auth_sasl"
    };

    private final static String[] RESET_QUESTIONS = new String[]{
            "first", "app_support", "notify_archive", "message_swipe", "message_select", "folder_actions", "folder_sync",
            "crash_reports_asked", "review_asked", "review_later", "why",
            "reply_hint", "html_always_images", "open_full_confirmed",
            "print_html_confirmed", "print_html_header", "print_html_images",
            "reformatted_hint",
            "selected_folders", "move_1_confirmed", "move_n_confirmed",
            "last_search_senders", "last_search_recipients", "last_search_subject", "last_search_keywords", "last_search_message", "last_search",
            "identities_asked", "identities_primary_hint",
            "cc_bcc", "inline_image_hint", "compose_reference", "send_dialog",
            "setup_reminder", "setup_advanced"
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        for (String tag : getResources().getAssets().getLocales())
            languages.add(new Pair<>(tag, Locale.forLanguageTag(tag).getDisplayName()));

        Collections.sort(languages, new Comparator<Pair<String, String>>() {
            @Override
            public int compare(Pair<String, String> l1, Pair<String, String> l2) {
                return l1.second.compareTo(l2.second);
            }
        });
    }

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_setup);
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_options_misc, container, false);

        // Get controls

        swExternalSearch = view.findViewById(R.id.swExternalSearch);
        swShortcuts = view.findViewById(R.id.swShortcuts);
        swFts = view.findViewById(R.id.swFts);
        swClassification = view.findViewById(R.id.swClassification);
        ibClassification = view.findViewById(R.id.ibClassification);
        tvClassMinProbability = view.findViewById(R.id.tvClassMinProbability);
        sbClassMinProbability = view.findViewById(R.id.sbClassMinProbability);
        tvClassMinDifference = view.findViewById(R.id.tvClassMinDifference);
        sbClassMinDifference = view.findViewById(R.id.sbClassMinDifference);
        tvFtsIndexed = view.findViewById(R.id.tvFtsIndexed);
        tvFtsPro = view.findViewById(R.id.tvFtsPro);
        spLanguage = view.findViewById(R.id.spLanguage);
        ibResetLanguage = view.findViewById(R.id.ibResetLanguage);
        swWatchdog = view.findViewById(R.id.swWatchdog);
        swUpdates = view.findViewById(R.id.swUpdates);
        swExperiments = view.findViewById(R.id.swExperiments);
        tvExperimentsHint = view.findViewById(R.id.tvExperimentsHint);
        swCrashReports = view.findViewById(R.id.swCrashReports);
        tvUuid = view.findViewById(R.id.tvUuid);
        btnReset = view.findViewById(R.id.btnReset);
        swCleanupAttachments = view.findViewById(R.id.swCleanupAttachments);
        btnCleanup = view.findViewById(R.id.btnCleanup);
        tvLastCleanup = view.findViewById(R.id.tvLastCleanup);
        btnApp = view.findViewById(R.id.btnApp);
        btnMore = view.findViewById(R.id.btnMore);

        swProtocol = view.findViewById(R.id.swProtocol);
        swLogInfo = view.findViewById(R.id.swLogInfo);
        swDebug = view.findViewById(R.id.swDebug);
        swQueries = view.findViewById(R.id.swQueries);
        swWal = view.findViewById(R.id.swWal);
        swExpunge = view.findViewById(R.id.swExpunge);
        swAuthPlain = view.findViewById(R.id.swAuthPlain);
        swAuthLogin = view.findViewById(R.id.swAuthLogin);
        swAuthNtlm = view.findViewById(R.id.swAuthNtlm);
        swAuthSasl = view.findViewById(R.id.swAuthSasl);
        tvProcessors = view.findViewById(R.id.tvProcessors);
        tvMemoryClass = view.findViewById(R.id.tvMemoryClass);
        tvMemoryUsage = view.findViewById(R.id.tvMemoryUsage);
        tvStorageUsage = view.findViewById(R.id.tvStorageUsage);
        tvFingerprint = view.findViewById(R.id.tvFingerprint);
        btnGC = view.findViewById(R.id.btnGC);
        btnCharsets = view.findViewById(R.id.btnCharsets);
        btnCiphers = view.findViewById(R.id.btnCiphers);
        btnFiles = view.findViewById(R.id.btnFiles);

        grpDebug = view.findViewById(R.id.grpDebug);

        setOptions();

        // Wire controls

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        swExternalSearch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                Helper.enableComponent(getContext(), ActivitySearch.class, checked);
            }
        });

        swShortcuts.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("shortcuts", checked).commit(); // apply won't work here
            }
        });

        swFts.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("fts", checked).apply();

                WorkerFts.init(getContext(), true);

                if (!checked) {
                    Bundle args = new Bundle();

                    new SimpleTask<Void>() {
                        @Override
                        protected Void onExecute(Context context, Bundle args) {
                            try {
                                SQLiteDatabase sdb = FtsDbHelper.getInstance(context);
                                FtsDbHelper.delete(sdb);
                                FtsDbHelper.optimize(sdb);
                            } catch (SQLiteDatabaseCorruptException ex) {
                                Log.e(ex);
                                FtsDbHelper.delete(context);
                            }

                            DB db = DB.getInstance(context);
                            db.message().resetFts();

                            return null;
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Log.unexpectedError(getParentFragmentManager(), ex);
                        }
                    }.execute(FragmentOptionsMisc.this, args, "fts:reset");
                }
            }
        });

        Helper.linkPro(tvFtsPro);

        swClassification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            private int count = 0;

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
                prefs.edit().putBoolean("classification", checked).apply();
                if (!checked) {
                    count++;
                    if (count >= 3) {
                        count = 0;
                        MessageClassifier.clear(buttonView.getContext());
                        ToastEx.makeText(buttonView.getContext(), R.string.title_reset, Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        ibClassification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 163);
            }
        });

        sbClassMinProbability.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                prefs.edit().putInt("class_min_probability", progress).apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }
        });

        sbClassMinDifference.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                prefs.edit().putInt("class_min_difference", progress).apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }
        });

        spLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                if (position == 0)
                    onNothingSelected(adapterView);
                else {
                    String tag = languages.get(position - 1).first;
                    prefs.edit().putString("language", tag).commit(); // apply won't work here
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                prefs.edit().remove("language").commit(); // apply won't work here
            }
        });

        ibResetLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prefs.edit().remove("language").commit(); // apply won't work here
            }
        });

        swWatchdog.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("watchdog", checked).apply();
            }
        });

        swUpdates.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("updates", checked).apply();
                if (!checked) {
                    NotificationManager nm = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    nm.cancel(Helper.NOTIFICATION_UPDATE);
                }
            }
        });

        tvExperimentsHint.setPaintFlags(tvExperimentsHint.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvExperimentsHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 125);
            }
        });

        swExperiments.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("experiments", checked).apply();
            }
        });

        swCrashReports.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit()
                        .remove("crash_reports_asked")
                        .putBoolean("crash_reports", checked)
                        .apply();
                Log.setCrashReporting(checked);
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onResetQuestions();
            }
        });

        swCleanupAttachments.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("cleanup_attachments", checked).apply();
            }
        });

        btnCleanup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCleanup();
            }
        });

        final Intent app = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        app.setData(Uri.parse("package:" + getContext().getPackageName()));
        btnApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    getContext().startActivity(app);
                } catch (Throwable ex) {
                    Log.w(ex);
                    Helper.reportNoViewer(getContext(), app);
                }
            }
        });

        btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
                lbm.sendBroadcast(new Intent(ActivitySetup.ACTION_SETUP_MORE));
            }
        });

        swLogInfo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putInt("log_level", checked ? android.util.Log.INFO : android.util.Log.WARN).apply();
            }
        });

        swDebug.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("debug", checked).apply();
                grpDebug.setVisibility(checked || BuildConfig.DEBUG ? View.VISIBLE : View.GONE);
            }
        });

        swQueries.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked)
                    prefs.edit().putInt("query_threads", 2).commit(); // apply won't work here
                else
                    prefs.edit().remove("query_threads").commit(); // apply won't work here
            }
        });

        swWal.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("wal", checked).commit(); // apply won't work here
            }
        });

        swProtocol.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("protocol", checked).apply();
            }
        });

        swExpunge.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("perform_expunge", checked).apply();
                ServiceSynchronize.reload(getContext(), null, true, "perform_expunge");
            }
        });

        swAuthPlain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("auth_plain", checked).apply();
            }
        });

        swAuthLogin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("auth_login", checked).apply();
            }
        });

        swAuthNtlm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("auth_ntlm", checked).apply();
            }
        });

        swAuthSasl.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("auth_sasl", checked).apply();
            }
        });

        btnGC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Runtime.getRuntime().gc();
            }
        });

        btnCharsets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SimpleTask<SortedMap<String, Charset>>() {
                    @Override
                    protected void onPreExecute(Bundle args) {
                        btnCharsets.setEnabled(false);
                    }

                    @Override
                    protected void onPostExecute(Bundle args) {
                        btnCharsets.setEnabled(true);
                    }

                    @Override
                    protected SortedMap<String, Charset> onExecute(Context context, Bundle args) {
                        return Charset.availableCharsets();
                    }

                    @Override
                    protected void onExecuted(Bundle args, SortedMap<String, Charset> charsets) {
                        StringBuilder sb = new StringBuilder();
                        for (String key : charsets.keySet())
                            sb.append(charsets.get(key).displayName()).append("\r\n");
                        new AlertDialog.Builder(getContext())
                                .setTitle(R.string.title_advanced_charsets)
                                .setMessage(sb.toString())
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Do nothing
                                    }
                                })
                                .show();
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Log.unexpectedError(getParentFragmentManager(), ex);
                    }
                }.execute(FragmentOptionsMisc.this, new Bundle(), "setup:charsets");
            }
        });

        btnCiphers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuilder sb = new StringBuilder();
                try {
                    SSLSocket socket = (SSLSocket) SSLSocketFactory.getDefault().createSocket();

                    List<String> protocols = new ArrayList<>();
                    protocols.addAll(Arrays.asList(socket.getEnabledProtocols()));

                    List<String> ciphers = new ArrayList<>();
                    ciphers.addAll(Arrays.asList(socket.getEnabledCipherSuites()));

                    for (String p : socket.getSupportedProtocols()) {
                        boolean enabled = protocols.contains(p);
                        if (!enabled)
                            sb.append("(");
                        sb.append(p);
                        if (!enabled)
                            sb.append(")");
                        sb.append("\r\n");
                    }
                    sb.append("\r\n");

                    for (String c : socket.getSupportedCipherSuites()) {
                        boolean enabled = ciphers.contains(c);
                        if (!enabled)
                            sb.append("(");
                        sb.append(c);
                        if (!enabled)
                            sb.append(")");
                        sb.append("\r\n");
                    }
                    sb.append("\r\n");
                } catch (IOException ex) {
                    sb.append(ex.toString());
                }

                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.title_advanced_ciphers)
                        .setMessage(sb.toString())
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing
                            }
                        })
                        .show();
            }
        });

        final String title = getString(R.string.title_advanced_files, Helper.humanReadableByteCount(MIN_FILE_SIZE));
        btnFiles.setText(title);

        btnFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SimpleTask<List<File>>() {
                    @Override
                    protected void onPreExecute(Bundle args) {
                        btnFiles.setEnabled(false);
                    }

                    @Override
                    protected void onPostExecute(Bundle args) {
                        btnFiles.setEnabled(true);
                    }

                    @Override
                    protected List<File> onExecute(Context context, Bundle args) {
                        List<File> files = new ArrayList<>();
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                            files.addAll(getFiles(context.getFilesDir(), MIN_FILE_SIZE));
                            files.addAll(getFiles(context.getCacheDir(), MIN_FILE_SIZE));
                        } else
                            files.addAll(getFiles(context.getDataDir(), MIN_FILE_SIZE));
                        Collections.sort(files, new Comparator<File>() {
                            @Override
                            public int compare(File f1, File f2) {
                                return -Long.compare(f1.length(), f2.length());
                            }
                        });
                        return files;
                    }

                    private List<File> getFiles(File dir, long minSize) {
                        List<File> files = new ArrayList<>();
                        File[] listed = dir.listFiles();
                        if (listed != null)
                            for (File file : listed)
                                if (file.isDirectory())
                                    files.addAll(getFiles(file, minSize));
                                else if (file.length() > minSize)
                                    files.add(file);
                        return files;
                    }

                    @Override
                    protected void onExecuted(Bundle args, List<File> files) {
                        StringBuilder sb = new StringBuilder();

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                            sb.append("Data: ").append(getContext().getDataDir()).append("\r\n");
                        sb.append("Files: ").append(getContext().getFilesDir()).append("\r\n");
                        sb.append("Cache: ").append(getContext().getCacheDir()).append("\r\n");

                        for (File file : files)
                            sb.append(file.getAbsolutePath())
                                    .append(' ')
                                    .append(Helper.humanReadableByteCount(file.length()))
                                    .append("\r\n");

                        new AlertDialog.Builder(getContext())
                                .setTitle(title)
                                .setMessage(sb.toString())
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Do nothing
                                    }
                                })
                                .show();
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Log.unexpectedError(getParentFragmentManager(), ex);
                    }
                }.execute(FragmentOptionsMisc.this, new Bundle(), "setup:files");
            }
        });

        tvFtsIndexed.setText(null);

        DB db = DB.getInstance(getContext());
        db.message().liveFts().observe(getViewLifecycleOwner(), new Observer<TupleFtsStats>() {
            private TupleFtsStats last = null;

            @Override
            public void onChanged(TupleFtsStats stats) {
                if (stats == null)
                    tvFtsIndexed.setText(null);
                else if (last == null || !last.equals(stats))
                    tvFtsIndexed.setText(getString(R.string.title_advanced_fts_indexed,
                            stats.fts,
                            stats.total,
                            Helper.humanReadableByteCount(FtsDbHelper.size(getContext()))));
                last = stats;
            }
        });

        setLastCleanup(prefs.getLong("last_cleanup", -1));

        PreferenceManager.getDefaultSharedPreferences(getContext()).registerOnSharedPreferenceChangeListener(this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        resumed = true;

        View view = getView();
        if (view != null)
            view.post(new Runnable() {
                @Override
                public void run() {
                    updateUsage();
                }
            });
    }

    @Override
    public void onPause() {
        super.onPause();
        resumed = false;
    }

    @Override
    public void onDestroyView() {
        PreferenceManager.getDefaultSharedPreferences(getContext()).unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroyView();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
            setOptions();
            if ("last_cleanup".equals(key))
                setLastCleanup(prefs.getLong(key, -1));
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_options, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_default) {
            onMenuDefault();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onMenuDefault() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = prefs.edit();
        for (String option : RESET_OPTIONS)
            editor.remove(option);
        editor.apply();
        ToastEx.makeText(getContext(), R.string.title_setup_done, Toast.LENGTH_LONG).show();
    }

    private void onResetQuestions() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = prefs.edit();
        for (String option : RESET_QUESTIONS)
            editor.remove(option);
        for (String key : prefs.getAll().keySet())
            if (key.endsWith(".show_full") || key.endsWith(".show_images") || key.endsWith(".confirm_link"))
                editor.remove(key);
        editor.apply();
        ToastEx.makeText(getContext(), R.string.title_setup_done, Toast.LENGTH_LONG).show();
    }

    private void onCleanup() {
        new SimpleTask<Void>() {
            @Override
            protected void onPreExecute(Bundle args) {
                btnCleanup.setEnabled(false);
                ToastEx.makeText(getContext(), R.string.title_executing, Toast.LENGTH_LONG).show();
            }

            @Override
            protected void onPostExecute(Bundle args) {
                btnCleanup.setEnabled(true);
                ToastEx.makeText(getContext(), R.string.title_completed, Toast.LENGTH_LONG).show();
            }

            @Override
            protected Void onExecute(Context context, Bundle args) {
                WorkerCleanup.cleanup(context, true);
                return null;
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, new Bundle(), "cleanup:run");
    }

    private void setOptions() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        swExternalSearch.setChecked(Helper.isComponentEnabled(getContext(), ActivitySearch.class));
        swShortcuts.setChecked(prefs.getBoolean("shortcuts", true));
        swFts.setChecked(prefs.getBoolean("fts", false));

        swClassification.setChecked(prefs.getBoolean("classification", false));

        int class_min_chance = prefs.getInt("class_min_probability", 15);
        tvClassMinProbability.setText(getString(R.string.title_advanced_class_min_chance, NF.format(class_min_chance)));
        sbClassMinProbability.setProgress(class_min_chance);

        int class_min_difference = prefs.getInt("class_min_difference", 50);
        tvClassMinDifference.setText(getString(R.string.title_advanced_class_min_difference, NF.format(class_min_difference)));
        sbClassMinDifference.setProgress(class_min_difference);

        int selected = -1;
        String language = prefs.getString("language", null);
        List<String> display = new ArrayList<>();
        display.add(getString(R.string.title_advanced_language_system));
        for (int pos = 0; pos < languages.size(); pos++) {
            Pair<String, String> lang = languages.get(pos);
            display.add(lang.second);
            if (lang.first.equals(language))
                selected = pos + 1;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, android.R.id.text1, display);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spLanguage.setAdapter(adapter);
        if (selected >= 0)
            spLanguage.setSelection(selected);

        swWatchdog.setChecked(prefs.getBoolean("watchdog", true));
        swUpdates.setChecked(prefs.getBoolean("updates", true));
        swUpdates.setVisibility(
                Helper.isPlayStoreInstall() || !Helper.hasValidFingerprint(getContext())
                        ? View.GONE : View.VISIBLE);
        swExperiments.setChecked(prefs.getBoolean("experiments", false));
        swCrashReports.setChecked(prefs.getBoolean("crash_reports", false));
        tvUuid.setText(prefs.getString("uuid", null));
        swCleanupAttachments.setChecked(prefs.getBoolean("cleanup_attachments", false));

        swProtocol.setChecked(prefs.getBoolean("protocol", false));
        swLogInfo.setChecked(prefs.getInt("log_level", Log.getDefaultLogLevel()) <= android.util.Log.INFO);
        swDebug.setChecked(prefs.getBoolean("debug", false));
        swQueries.setChecked(prefs.getInt("query_threads", 4) < 4);
        swWal.setChecked(prefs.getBoolean("wal", true));
        swExpunge.setChecked(prefs.getBoolean("perform_expunge", true));
        swAuthPlain.setChecked(prefs.getBoolean("auth_plain", true));
        swAuthLogin.setChecked(prefs.getBoolean("auth_login", true));
        swAuthNtlm.setChecked(prefs.getBoolean("auth_ntlm", true));
        swAuthSasl.setChecked(prefs.getBoolean("auth_sasl", true));

        tvProcessors.setText(getString(R.string.title_advanced_processors, Runtime.getRuntime().availableProcessors()));

        ActivityManager am = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
        int class_mb = am.getMemoryClass();
        int class_large_mb = am.getLargeMemoryClass();
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        tvMemoryClass.setText(getString(R.string.title_advanced_memory_class,
                class_mb + " MB",
                class_large_mb + " MB",
                Helper.humanReadableByteCount(mi.totalMem)));

        tvFingerprint.setText(Helper.getFingerprint(getContext()));

        grpDebug.setVisibility(swDebug.isChecked() || BuildConfig.DEBUG ? View.VISIBLE : View.GONE);
    }

    private void updateUsage() {
        if (!resumed)
            return;

        try {
            Log.i("Update usage");

            new SimpleTask<StorageData>() {
                @Override
                protected StorageData onExecute(Context context, Bundle args) throws Throwable {
                    StorageData data = new StorageData();
                    Runtime rt = Runtime.getRuntime();
                    data.hused = rt.totalMemory() - rt.freeMemory();
                    data.hmax = rt.maxMemory();
                    data.nheap = Debug.getNativeHeapAllocatedSize();
                    data.available = Helper.getAvailableStorageSpace();
                    data.total = Helper.getTotalStorageSpace();
                    data.used = Helper.getSize(context.getFilesDir());
                    return data;
                }

                @Override
                protected void onExecuted(Bundle args, StorageData data) {
                    tvMemoryUsage.setText(getString(R.string.title_advanced_memory_usage,
                            Helper.humanReadableByteCount(data.hused),
                            Helper.humanReadableByteCount(data.hmax),
                            Helper.humanReadableByteCount(data.nheap)));

                    tvStorageUsage.setText(getString(R.string.title_advanced_storage_usage,
                            Helper.humanReadableByteCount(data.available),
                            Helper.humanReadableByteCount(data.total),
                            Helper.humanReadableByteCount(data.used)));

                    getView().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            updateUsage();
                        }
                    }, 2500);
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.e(ex);
                }
            }.execute(this, null, "usage");
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    private void setLastCleanup(long time) {
        java.text.DateFormat DTF = Helper.getDateTimeInstance(getContext());
        tvLastCleanup.setText(
                getString(R.string.title_advanced_last_cleanup,
                        time < 0 ? "-" : DTF.format(time)));
    }

    private static class StorageData {
        private long hused;
        private long hmax;
        private long nheap;
        private long available;
        private long total;
        private long used;
    }
}
