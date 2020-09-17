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

import android.app.RecoverableSecurityException;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.Lifecycle;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.ActionBar.DISPLAY_SHOW_CUSTOM;
import static android.app.Activity.RESULT_OK;

public class FragmentBase extends Fragment {
    private String title = null;
    private String subtitle = " ";
    private boolean finish = false;
    private boolean finished = false;
    private String requestKey = null;

    private long message = -1;
    private long attachment = -1;
    private int scrollTo = 0;

    private static int requestSequence = 0;

    private static final int REQUEST_ATTACHMENT = 51;
    private static final int REQUEST_ATTACHMENTS = 52;
    private static final int REQUEST_RECOVERABLE_PERMISSION = 53;

    static final String ACTION_STORE_ATTACHMENT = BuildConfig.APPLICATION_ID + ".STORE_ATTACHMENT";
    static final String ACTION_STORE_ATTACHMENTS = BuildConfig.APPLICATION_ID + ".STORE_ATTACHMENTS";

    protected void setTitle(int resid) {
        setTitle(getString(resid));
    }

    protected void setTitle(String title) {
        this.title = title;
        updateSubtitle();
    }

    protected void setSubtitle(int resid) {
        setSubtitle(getString(resid));
    }

    protected void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
        updateSubtitle();
    }

    void scrollTo(int resid) {
        scrollTo = resid;
        scrollTo();
    }

    private void scrollTo() {
        if (scrollTo == 0)
            return;

        View view = getView();
        if (view == null)
            return;

        final ScrollView scroll = view.findViewById(R.id.scroll);
        if (scroll == null)
            return;

        final View child = scroll.findViewById(scrollTo);
        if (child == null)
            return;

        scrollTo = 0;

        scroll.post(new Runnable() {
            @Override
            public void run() {
                scroll.scrollTo(0, child.getTop());
            }
        });
    }

    @Override
    public void startActivity(Intent intent) {
        try {
            super.startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Log.w(ex);
            ToastEx.makeText(getContext(), getString(R.string.title_no_viewer, intent), Toast.LENGTH_LONG).show();
        } catch (Throwable ex) {
            Log.e(ex);
            ToastEx.makeText(getContext(), Log.formatThrowable(ex), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        try {
            super.startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException ex) {
            Log.w(ex);
            ToastEx.makeText(getContext(), getString(R.string.title_no_viewer, intent), Toast.LENGTH_LONG).show();
        } catch (Throwable ex) {
            Log.e(ex);
            ToastEx.makeText(getContext(), Log.formatThrowable(ex), Toast.LENGTH_LONG).show();
        }
    }

    protected void finish() {
        if (finished)
            return;
        finished = true;

        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
            getParentFragmentManager().popBackStack();
        else
            finish = true;
    }

    protected void restart() {
        Intent intent = new Intent(getContext(), ActivityMain.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        Runtime.getRuntime().exit(0);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d("Save instance " + this);
        int before = Helper.getSize(outState);
        outState.putString("fair:subtitle", subtitle);
        outState.putString("fair:requestKey", requestKey);
        super.onSaveInstanceState(outState);
        int after = Helper.getSize(outState);
        Log.d("Saved instance " + this + " size=" + before + "/" + after);

        Map<String, String> crumb = new HashMap<>();
        crumb.put("name", this.getClass().getName());
        crumb.put("before", Integer.toString(before));
        crumb.put("after", Integer.toString(after));
        for (String key : outState.keySet()) {
            Object value = outState.get(key);
            crumb.put(key, value == null ? "" : value.getClass().getName());
        }
        Log.breadcrumb("onSaveInstanceState", crumb);

        for (String key : outState.keySet())
            Log.d("Saved " + this + " " + key + "=" + outState.get(key));
    }

    public String getRequestKey() {
        if (requestKey == null)
            requestKey = getClass().getName() + "_" + (++requestSequence);
        return requestKey;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i("Create " + this + " saved=" + (savedInstanceState != null));
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            subtitle = savedInstanceState.getString("fair:subtitle");
            requestKey = savedInstanceState.getString("fair:requestKey");
        }

        // https://developer.android.com/training/basics/fragments/pass-data-between
        getParentFragmentManager().setFragmentResultListener(getRequestKey(), this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                int requestCode = result.getInt("requestCode");
                int resultCode = result.getInt("resultCode");

                Intent data = new Intent();
                data.putExtra("args", result);
                onActivityResult(requestCode, resultCode, data);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("Create view " + this + (savedInstanceState != null));
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d("Activity " + this + " saved=" + (savedInstanceState != null));
        super.onActivityCreated(savedInstanceState);
        scrollTo();
    }

    @Override
    public void onResume() {
        Log.d("Resume " + this);
        super.onResume();
        updateSubtitle();
        if (finish) {
            getParentFragmentManager().popBackStack();
            finish = false;
        }

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
        IntentFilter iff = new IntentFilter();
        iff.addAction(ACTION_STORE_ATTACHMENT);
        iff.addAction(ACTION_STORE_ATTACHMENTS);
        lbm.registerReceiver(receiver, iff);
    }

    @Override
    public void onPause() {
        Log.d("Pause " + this);
        super.onPause();

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
        lbm.unregisterReceiver(receiver);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        String action = (data == null ? null : data.getAction());
        Log.i("Result class=" + this.getClass().getSimpleName() +
                " action=" + action + " request=" + requestCode + " result=" + resultCode);
        Log.logExtras(data);
        if (data != null)
            Log.i("data=" + data.getData());
        super.onActivityResult(requestCode, resultCode, data);

        try {
            switch (requestCode) {
                case REQUEST_ATTACHMENT:
                    if (resultCode == RESULT_OK && data != null)
                        onSaveAttachment(data);
                    break;
                case REQUEST_ATTACHMENTS:
                    if (resultCode == RESULT_OK && data != null)
                        onSaveAttachments(data);
                    break;
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    @Override
    public void onDetach() {
        Log.d("Detach " + this);
        super.onDetach();

        InputMethodManager im = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        View focused = getActivity().getCurrentFocus();
        if (focused != null)
            im.hideSoftInputFromWindow(focused.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d("Config " + this);
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onDestroy() {
        Log.i("Destroy " + this);
        super.onDestroy();
    }

    @Override
    public void setHasOptionsMenu(boolean hasMenu) {
        super.setHasOptionsMenu(!isPane() && hasMenu);
    }

    private void updateSubtitle() {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null && !isPane()) {
            ActionBar actionbar = activity.getSupportActionBar();
            if (actionbar != null)
                if ((actionbar.getDisplayOptions() & DISPLAY_SHOW_CUSTOM) == 0) {
                    actionbar.setTitle(title == null ? getString(R.string.app_name) : title);
                    actionbar.setSubtitle(subtitle);
                } else {
                    View custom = actionbar.getCustomView();
                    TextView tvTitle = custom.findViewById(R.id.title);
                    TextView tvSubtitle = custom.findViewById(R.id.subtitle);
                    tvTitle.setText(title == null ? getString(R.string.app_name) : title);
                    tvSubtitle.setText(subtitle);
                }
        }
    }

    private boolean isPane() {
        Bundle args = getArguments();
        return (args != null && args.getBoolean("pane"));
    }

    boolean hasPermission(String name) {
        ActivityBase activity = (ActivityBase) getActivity();
        if (activity == null)
            return false;
        return activity.hasPermission(name);
    }

    void addKeyPressedListener(ActivityBase.IKeyPressedListener listener) {
        ((ActivityBase) getActivity()).addKeyPressedListener(listener, getViewLifecycleOwner());
    }

    void addBillingListener(ActivityBilling.IBillingListener listener) {
        ((ActivityBilling) getActivity()).addBillingListener(listener, getViewLifecycleOwner());
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                String action = intent.getAction();

                if (ACTION_STORE_ATTACHMENT.equals(action))
                    onStoreAttachment(intent);
                if (ACTION_STORE_ATTACHMENTS.equals(action))
                    onStoreAttachments(intent);
            }
        }
    };

    private void onStoreAttachment(Intent intent) {
        attachment = intent.getLongExtra("id", -1);
        Intent create = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        create.addCategory(Intent.CATEGORY_OPENABLE);
        create.setType(intent.getStringExtra("type"));
        create.putExtra(Intent.EXTRA_TITLE, intent.getStringExtra("name"));
        Helper.openAdvanced(create);
        PackageManager pm = getContext().getPackageManager();
        if (create.resolveActivity(pm) == null) // system whitelisted
            ToastEx.makeText(getContext(), R.string.title_no_saf, Toast.LENGTH_LONG).show();
        else
            startActivityForResult(Helper.getChooser(getContext(), create), REQUEST_ATTACHMENT);
    }

    private void onStoreAttachments(Intent intent) {
        message = intent.getLongExtra("id", -1);
        Intent tree = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        Helper.openAdvanced(tree);
        PackageManager pm = getContext().getPackageManager();
        if (tree.resolveActivity(pm) == null) // system whitelisted
            ToastEx.makeText(getContext(), R.string.title_no_saf, Toast.LENGTH_LONG).show();
        else
            startActivityForResult(Helper.getChooser(getContext(), tree), REQUEST_ATTACHMENTS);
    }

    private void onSaveAttachment(Intent data) {
        Bundle args = new Bundle();
        args.putLong("id", attachment);
        args.putParcelable("uri", data.getData());

        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) throws Throwable {
                long id = args.getLong("id");
                Uri uri = args.getParcelable("uri");

                if (!"content".equals(uri.getScheme())) {
                    Log.w("Save attachment uri=" + uri);
                    throw new IllegalArgumentException(context.getString(R.string.title_no_stream));
                }

                DB db = DB.getInstance(context);
                EntityAttachment attachment = db.attachment().getAttachment(id);
                if (attachment == null)
                    return null;
                File file = attachment.getFile(context);

                ParcelFileDescriptor pfd = null;
                OutputStream os = null;
                InputStream is = null;
                try {
                    pfd = context.getContentResolver().openFileDescriptor(uri, "w");
                    if (pfd == null)
                        throw new FileNotFoundException(uri.toString());
                    os = new FileOutputStream(pfd.getFileDescriptor());
                    is = new FileInputStream(file);

                    byte[] buffer = new byte[Helper.BUFFER_SIZE];
                    int read;
                    while ((read = is.read(buffer)) != -1)
                        os.write(buffer, 0, read);
                } finally {
                    try {
                        if (pfd != null)
                            pfd.close();
                    } catch (Throwable ex) {
                        Log.w(ex);
                    }
                    try {
                        if (os != null)
                            os.close();
                    } catch (Throwable ex) {
                        Log.w(ex);
                    }
                    try {
                        if (is != null)
                            is.close();
                    } catch (Throwable ex) {
                        Log.w(ex);
                    }
                }

                return null;
            }

            @Override
            protected void onExecuted(Bundle args, Void data) {
                ToastEx.makeText(getContext(), R.string.title_attachment_saved, Toast.LENGTH_LONG).show();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    if (ex instanceof RecoverableSecurityException) {
                        handle((RecoverableSecurityException) ex);
                        return;
                    }

                if (ex instanceof IllegalArgumentException ||
                        ex instanceof FileNotFoundException ||
                        ex instanceof SecurityException)
                    ToastEx.makeText(getContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                else
                    Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "attachment:save");
    }

    private void onSaveAttachments(Intent data) {
        Bundle args = new Bundle();
        args.putLong("id", message);
        args.putParcelable("uri", data.getData());

        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) throws Throwable {
                long id = args.getLong("id");
                Uri uri = args.getParcelable("uri");

                if (!"content".equals(uri.getScheme())) {
                    Log.w("Save attachment uri=" + uri);
                    throw new IllegalArgumentException(context.getString(R.string.title_no_stream));
                }

                DB db = DB.getInstance(context);
                DocumentFile tree = DocumentFile.fromTreeUri(context, uri);
                List<EntityAttachment> attachments = db.attachment().getAttachments(id);
                for (EntityAttachment attachment : attachments) {
                    File file = attachment.getFile(context);

                    String name = Helper.sanitizeFilename(attachment.name);
                    if (TextUtils.isEmpty(name))
                        name = Long.toString(attachment.id);
                    DocumentFile document = tree.createFile(attachment.getMimeType(), name);
                    if (document == null)
                        throw new FileNotFoundException("Could not save " + uri + ":" + name);

                    ParcelFileDescriptor pfd = null;
                    OutputStream os = null;
                    InputStream is = null;
                    try {
                        pfd = context.getContentResolver().openFileDescriptor(document.getUri(), "w");
                        if (pfd == null)
                            throw new FileNotFoundException(name);
                        os = new FileOutputStream(pfd.getFileDescriptor());
                        is = new FileInputStream(file);

                        byte[] buffer = new byte[Helper.BUFFER_SIZE];
                        int read;
                        while ((read = is.read(buffer)) != -1)
                            os.write(buffer, 0, read);
                    } finally {
                        try {
                            if (pfd != null)
                                pfd.close();
                        } catch (Throwable ex) {
                            Log.w(ex);
                        }
                        try {
                            if (os != null)
                                os.close();
                        } catch (Throwable ex) {
                            Log.w(ex);
                        }
                        try {
                            if (is != null)
                                is.close();
                        } catch (Throwable ex) {
                            Log.w(ex);
                        }
                    }
                }

                return null;
            }

            @Override
            protected void onExecuted(Bundle args, Void data) {
                ToastEx.makeText(getContext(), R.string.title_attachments_saved, Toast.LENGTH_LONG).show();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    if (ex instanceof RecoverableSecurityException) {
                        handle((RecoverableSecurityException) ex);
                        return;
                    }

                if (ex instanceof IllegalArgumentException ||
                        ex instanceof FileNotFoundException ||
                        ex instanceof SecurityException)
                    ToastEx.makeText(getContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                else
                    Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "attachments:save");
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void handle(RecoverableSecurityException ex) {
        new AlertDialog.Builder(getContext())
                .setMessage(ex.getMessage())
                .setPositiveButton(ex.getUserAction().getTitle(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            startIntentSenderForResult(
                                    ex.getUserAction().getActionIntent().getIntentSender(),
                                    REQUEST_RECOVERABLE_PERMISSION,
                                    null, 0, 0, 0, null);
                        } catch (IntentSender.SendIntentException ex) {
                            Log.w(ex);
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    Handler getMainHandler() {
        return ApplicationEx.getMainHandler();
    }
}
