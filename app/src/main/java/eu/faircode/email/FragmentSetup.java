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

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;

import static android.app.Activity.RESULT_OK;

public class FragmentSetup extends FragmentEx {
    private ViewGroup view;

    private ImageButton ibHelp;

    private Button btnAccount;
    private TextView tvAccountDone;

    private Button btnIdentity;
    private TextView tvIdentityDone;

    private Button btnPermissions;
    private TextView tvPermissionsDone;

    private Button btnDoze;
    private TextView tvDozeDone;

    private Button btnData;

    private ToggleButton tbDarkTheme;

    private Button btnOptions;

    private Drawable check;

    private static final String[] permissions = new String[]{
            Manifest.permission.READ_CONTACTS
    };

    static final List<String> EXPORT_SETTINGS = Arrays.asList(
            "enabled",
            "avatars",
            "light",
            "browse",
            "swipe",
            "sort"
    );

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_setup);
        setHasOptionsMenu(true);

        check = getResources().getDrawable(R.drawable.baseline_check_24, getContext().getTheme());

        view = (ViewGroup) inflater.inflate(R.layout.fragment_setup, container, false);

        // Get controls
        ibHelp = view.findViewById(R.id.ibHelp);

        btnAccount = view.findViewById(R.id.btnAccount);
        tvAccountDone = view.findViewById(R.id.tvAccountDone);

        btnIdentity = view.findViewById(R.id.btnIdentity);
        tvIdentityDone = view.findViewById(R.id.tvIdentityDone);

        btnPermissions = view.findViewById(R.id.btnPermissions);
        tvPermissionsDone = view.findViewById(R.id.tvPermissionsDone);

        btnDoze = view.findViewById(R.id.btnDoze);
        tvDozeDone = view.findViewById(R.id.tvDozeDone);

        btnData = view.findViewById(R.id.btnData);

        tbDarkTheme = view.findViewById(R.id.tbDarkTheme);
        btnOptions = view.findViewById(R.id.btnOptions);

        // Wire controls

        ibHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(getIntentHelp());
            }
        });

        btnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.content_frame, new FragmentAccounts()).addToBackStack("accounts");
                fragmentTransaction.commit();
            }
        });

        btnIdentity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.content_frame, new FragmentIdentities()).addToBackStack("identities");
                fragmentTransaction.commit();
            }
        });

        btnPermissions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnPermissions.setEnabled(false);
                requestPermissions(permissions, 1);
            }
        });

        btnDoze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DialogBuilderLifecycle(getContext(), getViewLifecycleOwner())
                        .setMessage(R.string.title_setup_doze_instructions)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
                                } catch (Throwable ex) {
                                    Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                                }
                            }
                        })
                        .create()
                        .show();
            }
        });

        btnData.setOnClickListener(new View.OnClickListener() {
            @Override
            @TargetApi(Build.VERSION_CODES.N)
            public void onClick(View v) {
                try {
                    startActivity(new Intent(Settings.ACTION_IGNORE_BACKGROUND_DATA_RESTRICTIONS_SETTINGS,
                            Uri.parse("package:" + BuildConfig.APPLICATION_ID)));
                } catch (Throwable ex) {
                    Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                }
            }
        });

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        String theme = prefs.getString("theme", "light");
        boolean dark = "dark".equals(theme);
        tbDarkTheme.setTag(dark);
        tbDarkTheme.setChecked(dark);
        tbDarkTheme.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton button, boolean checked) {
                if (Helper.isPro(getContext())) {
                    if (checked != (Boolean) button.getTag()) {
                        button.setTag(checked);
                        tbDarkTheme.setChecked(checked);
                        prefs.edit().putString("theme", checked ? "dark" : "light").apply();
                    }
                } else {
                    prefs.edit().remove("theme").apply();
                    if (checked) {
                        tbDarkTheme.setChecked(false);
                        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.content_frame, new FragmentPro()).addToBackStack("pro");
                        fragmentTransaction.commit();
                    }
                }
            }
        });

        btnOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.content_frame, new FragmentOptions()).addToBackStack("options");
                fragmentTransaction.commit();
            }
        });

        // Initialize
        ibHelp.setVisibility(View.GONE);

        tvAccountDone.setText(null);
        tvAccountDone.setCompoundDrawables(null, null, null, null);

        btnIdentity.setEnabled(false);
        tvIdentityDone.setText(null);
        tvIdentityDone.setCompoundDrawables(null, null, null, null);

        tvPermissionsDone.setText(null);
        tvPermissionsDone.setCompoundDrawables(null, null, null, null);

        btnDoze.setEnabled(false);
        tvDozeDone.setText(null);
        tvDozeDone.setCompoundDrawables(null, null, null, null);

        btnData.setVisibility(View.GONE);

        int[] grantResults = new int[permissions.length];
        for (int i = 0; i < permissions.length; i++)
            grantResults[i] = ContextCompat.checkSelfPermission(getActivity(), permissions[i]);

        onRequestPermissionsResult(0, permissions, grantResults);

        // Create outbox
        new SimpleTask<Void>() {
            @Override
            protected Void onLoad(Context context, Bundle args) {
                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    EntityFolder outbox = db.folder().getOutbox();
                    if (outbox == null) {
                        outbox = new EntityFolder();
                        outbox.name = "OUTBOX";
                        outbox.type = EntityFolder.OUTBOX;
                        outbox.synchronize = false;
                        outbox.after = 0;
                        outbox.id = db.folder().insertFolder(outbox);
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                return null;
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(getContext(), ex);
            }
        }.load(this, new Bundle());

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        PackageManager pm = getContext().getPackageManager();
        ibHelp.setVisibility(getIntentHelp().resolveActivity(pm) == null ? View.GONE : View.VISIBLE);

        DB db = DB.getInstance(getContext());

        db.account().liveAccounts(true).observe(getViewLifecycleOwner(), new Observer<List<EntityAccount>>() {
            @Override
            public void onChanged(@Nullable List<EntityAccount> accounts) {
                boolean done = (accounts != null && accounts.size() > 0);
                btnIdentity.setEnabled(done);
                tvAccountDone.setText(done ? R.string.title_setup_done : R.string.title_setup_to_do);
                tvAccountDone.setCompoundDrawablesWithIntrinsicBounds(done ? check : null, null, null, null);
            }
        });

        db.identity().liveIdentities(true).observe(getViewLifecycleOwner(), new Observer<List<EntityIdentity>>() {
            @Override
            public void onChanged(@Nullable List<EntityIdentity> identities) {
                boolean done = (identities != null && identities.size() > 0);
                tvIdentityDone.setText(done ? R.string.title_setup_done : R.string.title_setup_to_do);
                tvIdentityDone.setCompoundDrawablesWithIntrinsicBounds(done ? check : null, null, null, null);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        PowerManager pm = getContext().getSystemService(PowerManager.class);
        boolean ignoring = pm.isIgnoringBatteryOptimizations(BuildConfig.APPLICATION_ID);
        btnDoze.setEnabled(!ignoring);
        tvDozeDone.setText(ignoring ? R.string.title_setup_done : R.string.title_setup_to_do);
        tvDozeDone.setCompoundDrawablesWithIntrinsicBounds(ignoring ? check : null, null, null, null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ConnectivityManager cm = getContext().getSystemService(ConnectivityManager.class);
            boolean saving = (cm.getRestrictBackgroundStatus() == ConnectivityManager.RESTRICT_BACKGROUND_STATUS_ENABLED);
            btnData.setVisibility(saving ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_setup, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        PackageManager pm = getContext().getPackageManager();
        menu.findItem(R.id.menu_export).setEnabled(getIntentExport().resolveActivity(pm) != null);
        menu.findItem(R.id.menu_import).setEnabled(getIntentImport().resolveActivity(pm) != null);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_legend:
                onMenuLegend();
                return true;

            case R.id.menu_export:
                onMenuExport();
                return true;

            case R.id.menu_import:
                onMenuImport();
                return true;

            case R.id.menu_privacy:
                onMenuPrivacy();
                return true;

            case R.id.menu_about:
                onMenuAbout();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean has = (grantResults.length > 0);
        for (int result : grantResults)
            if (result != PackageManager.PERMISSION_GRANTED) {
                has = false;
                break;
            }

        btnPermissions.setEnabled(!has);
        tvPermissionsDone.setText(has ? R.string.title_setup_done : R.string.title_setup_to_do);
        tvPermissionsDone.setCompoundDrawablesWithIntrinsicBounds(has ? check : null, null, null, null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(Helper.TAG, "Request=" + requestCode + " result=" + resultCode + " data=" + data);

        if (requestCode == ActivitySetup.REQUEST_EXPORT) {
            if (resultCode == RESULT_OK && data != null)
                handleExport(data);

        } else if (requestCode == ActivitySetup.REQUEST_IMPORT) {
            if (resultCode == RESULT_OK && data != null)
                handleImport(data);
        }
    }

    private void onMenuPrivacy() {
        Helper.view(getContext(), Helper.getIntentPrivacy());
    }

    private void onMenuLegend() {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new FragmentLegend()).addToBackStack("legend");
        fragmentTransaction.commit();
    }

    private void onMenuExport() {
        if (Helper.isPro(getContext()))
            new DialogBuilderLifecycle(getContext(), getViewLifecycleOwner())
                    .setMessage(R.string.title_setup_export_do)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                startActivityForResult(getIntentExport(), ActivitySetup.REQUEST_EXPORT);
                            } catch (Throwable ex) {
                                Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                            }
                        }
                    })
                    .create()
                    .show();
        else {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content_frame, new FragmentPro()).addToBackStack("pro");
            fragmentTransaction.commit();
        }
    }

    private void onMenuImport() {
        new DialogBuilderLifecycle(getContext(), getViewLifecycleOwner())
                .setMessage(R.string.title_setup_import_do)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            startActivityForResult(getIntentImport(), ActivitySetup.REQUEST_IMPORT);
                        } catch (Throwable ex) {
                            Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                        }
                    }
                })
                .create()
                .show();
    }

    private void onMenuAbout() {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new FragmentAbout()).addToBackStack("about");
        fragmentTransaction.commit();
    }

    private Intent getIntentHelp() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://github.com/M66B/open-source-email/blob/master/SETUP.md"));
        return intent;
    }

    private static Intent getIntentExport() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_TITLE, "fairemail_backup_" +
                new SimpleDateFormat("yyyyMMdd").format(new Date().getTime()) + ".json");
        return intent;
    }

    private static Intent getIntentImport() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        return intent;
    }

    private void handleExport(Intent data) {
        Bundle args = new Bundle();
        args.putParcelable("uri", data.getData());

        new SimpleTask<Void>() {
            @Override
            protected Void onLoad(Context context, Bundle args) throws Throwable {
                Uri uri = args.getParcelable("uri");

                OutputStream out = null;
                try {
                    Log.i(Helper.TAG, "Writing URI=" + uri);
                    out = getContext().getContentResolver().openOutputStream(uri);

                    DB db = DB.getInstance(context);

                    // Accounts
                    JSONArray jaccounts = new JSONArray();
                    for (EntityAccount account : db.account().getAccounts()) {
                        // Account
                        JSONObject jaccount = account.toJSON();

                        // Identities
                        JSONArray jidentities = new JSONArray();
                        for (EntityIdentity identity : db.identity().getIdentities(account.id))
                            jidentities.put(identity.toJSON());
                        jaccount.put("identities", jidentities);

                        // Folders
                        JSONArray jfolders = new JSONArray();
                        for (EntityFolder folder : db.folder().getFolders(account.id))
                            jfolders.put(folder.toJSON());
                        jaccount.put("folders", jfolders);

                        jaccounts.put(jaccount);
                    }

                    // Answers
                    JSONArray janswers = new JSONArray();
                    for (EntityAnswer answer : db.answer().getAnswers())
                        janswers.put(answer.toJSON());

                    // Settings
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                    JSONArray jsettings = new JSONArray();
                    for (String key : prefs.getAll().keySet())
                        if (EXPORT_SETTINGS.contains(key)) {
                            JSONObject jsetting = new JSONObject();
                            jsetting.put("key", key);
                            jsetting.put("value", prefs.getAll().get(key));
                            jsettings.put(jsetting);
                        }

                    JSONObject jexport = new JSONObject();
                    jexport.put("accounts", jaccounts);
                    jexport.put("answers", janswers);
                    jexport.put("settings", jsettings);

                    out.write(jexport.toString(2).getBytes());

                    Log.i(Helper.TAG, "Exported data");
                } finally {
                    if (out != null)
                        out.close();
                }

                return null;
            }

            @Override
            protected void onLoaded(Bundle args, Void data) {
                Snackbar.make(view, R.string.title_setup_exported, Snackbar.LENGTH_LONG).show();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(getContext(), ex);
            }
        }.load(this, args);
    }

    private void handleImport(Intent data) {
        Bundle args = new Bundle();
        args.putParcelable("uri", data.getData());

        new SimpleTask<Void>() {
            @Override
            protected Void onLoad(Context context, Bundle args) throws Throwable {
                Uri uri = args.getParcelable("uri");

                InputStream in = null;
                try {
                    Log.i(Helper.TAG, "Reading URI=" + uri);
                    ContentResolver resolver = getContext().getContentResolver();
                    AssetFileDescriptor descriptor = resolver.openTypedAssetFileDescriptor(uri, "*/*", null);
                    in = descriptor.createInputStream();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null)
                        response.append(line);
                    Log.i(Helper.TAG, "Importing " + resolver.toString());

                    JSONObject jimport = new JSONObject(response.toString());

                    DB db = DB.getInstance(context);
                    try {
                        db.beginTransaction();

                        JSONArray jaccounts = jimport.getJSONArray("accounts");
                        for (int a = 0; a < jaccounts.length(); a++) {
                            JSONObject jaccount = (JSONObject) jaccounts.get(a);
                            EntityAccount account = EntityAccount.fromJSON(jaccount);
                            account.store_sent = false;
                            account.id = db.account().insertAccount(account);
                            Log.i(Helper.TAG, "Imported account=" + account.name);

                            JSONArray jidentities = (JSONArray) jaccount.get("identities");
                            for (int i = 0; i < jidentities.length(); i++) {
                                JSONObject jidentity = (JSONObject) jidentities.get(i);
                                EntityIdentity identity = EntityIdentity.fromJSON(jidentity);
                                identity.account = account.id;
                                identity.id = db.identity().insertIdentity(identity);
                                Log.i(Helper.TAG, "Imported identity=" + identity.email);
                            }

                            JSONArray jfolders = (JSONArray) jaccount.get("folders");
                            for (int f = 0; f < jfolders.length(); f++) {
                                JSONObject jfolder = (JSONObject) jfolders.get(f);
                                EntityFolder folder = EntityFolder.fromJSON(jfolder);
                                folder.account = account.id;
                                folder.id = db.folder().insertFolder(folder);
                                Log.i(Helper.TAG, "Imported folder=" + folder.name);
                            }
                        }

                        JSONArray janswers = jimport.getJSONArray("answers");
                        for (int a = 0; a < janswers.length(); a++) {
                            JSONObject janswer = (JSONObject) janswers.get(a);
                            EntityAnswer answer = EntityAnswer.fromJSON(janswer);
                            answer.id = db.answer().insertAnswer(answer);
                            Log.i(Helper.TAG, "Imported answer=" + answer.name);
                        }

                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                        SharedPreferences.Editor editor = prefs.edit();
                        JSONArray jsettings = jimport.getJSONArray("settings");
                        for (int s = 0; s < jsettings.length(); s++) {
                            JSONObject jsetting = (JSONObject) jsettings.get(s);
                            String key = jsetting.getString("key");
                            if (EXPORT_SETTINGS.contains(key)) {
                                Object value = jsetting.get("value");
                                if (value instanceof Boolean)
                                    editor.putBoolean(key, (Boolean) value);
                                else if (value instanceof String)
                                    editor.putString(key, (String) value);
                                else
                                    throw new IllegalArgumentException("Unknown settings type key=" + key);
                                Log.i(Helper.TAG, "Imported setting=" + key);
                            }
                        }
                        editor.apply();

                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }

                    Log.i(Helper.TAG, "Imported data");
                } finally {
                    if (in != null)
                        in.close();
                }

                return null;
            }

            @Override
            protected void onLoaded(Bundle args, Void data) {
                Snackbar.make(view, R.string.title_setup_imported, Snackbar.LENGTH_LONG).show();
                ServiceSynchronize.reload(getContext(), "import");
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(getContext(), ex);
            }
        }.load(this, args);
    }
}
