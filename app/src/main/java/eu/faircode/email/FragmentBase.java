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

    Copyright 2018-2022 by Marcel Bokhorst (M66B)
*/

import static android.app.ActionBar.DISPLAY_SHOW_CUSTOM;
import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.app.RecoverableSecurityException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FragmentBase extends Fragment {
    private CharSequence count = null;
    private CharSequence title = null;
    private CharSequence subtitle = " ";
    private boolean finish = false;
    private boolean finished = false;

    private int scrollToResid = 0;
    private int scrollToOffset = 0;

    private Integer orientation = null;

    private static final int REQUEST_ATTACHMENT = 51;
    private static final int REQUEST_ATTACHMENTS = 52;
    private static final int REQUEST_RECOVERABLE_PERMISSION = 53;

    static final int REQUEST_PERMISSIONS = 1000;

    static final String ACTION_STORE_ATTACHMENT = BuildConfig.APPLICATION_ID + ".STORE_ATTACHMENT";
    static final String ACTION_STORE_ATTACHMENTS = BuildConfig.APPLICATION_ID + ".STORE_ATTACHMENTS";

    protected ActionBar getSupportActionBar() {
        FragmentActivity activity = getActivity();
        if (activity instanceof ActivityBase)
            return ((ActivityBase) activity).getSupportActionBar();
        else
            return null;
    }

    protected void setCount(String count) {
        this.count = count;
        updateSubtitle();
    }

    protected void setTitle(int resid) {
        setTitle(getString(resid));
    }

    protected void setTitle(CharSequence title) {
        this.title = title;
        updateSubtitle();
    }

    protected void setSubtitle(int resid) {
        setSubtitle(getString(resid));
    }

    protected void setSubtitle(CharSequence subtitle) {
        this.subtitle = subtitle;
        updateSubtitle();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        onPrepareOptionsMenu(menu);
    }

    void invalidateOptionsMenu() {
        FragmentActivity activity = getActivity();
        if (activity != null)
            activity.invalidateOptionsMenu();
    }

    void scrollTo(int resid, int offset) {
        scrollToResid = resid;
        scrollToOffset = offset;
        scrollTo();
    }

    private void scrollTo() {
        if (scrollToResid == 0)
            return;

        View view = getView();
        if (view == null)
            return;

        final ScrollView scroll = view.findViewById(R.id.scroll);
        if (scroll == null)
            return;

        final View child = scroll.findViewById(scrollToResid);
        if (child == null)
            return;

        scrollToResid = 0;
        final int dy = Helper.dp2pixels(scroll.getContext(), scrollToOffset);

        scroll.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Rect rect = new Rect();
                    child.getDrawingRect(rect);
                    scroll.offsetDescendantRectToMyCoords(child, rect);
                    int y = rect.top - scroll.getPaddingTop() + dy;
                    if (y < 0)
                        y = 0;
                    scroll.scrollTo(0, y);
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }
        });
    }

    @Override
    public void startActivity(Intent intent) {
        try {
            Log.i("Start intent=" + intent);
            Log.logExtras(intent);
            super.startActivity(intent);
        } catch (Throwable ex) {
            Helper.reportNoViewer(getContext(), intent, ex);
        }
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        try {
            Log.i("Start intent=" + intent + " request=" + requestCode);
            Log.logExtras(intent);
            super.startActivityForResult(intent, requestCode);
        } catch (Throwable ex) {
            Helper.reportNoViewer(getContext(), intent, ex);
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d("Save instance " + this);
        int before = Helper.getSize(outState);
        outState.putCharSequence("fair:title", title);
        outState.putCharSequence("fair:subtitle", subtitle);
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
        return Helper.getRequestKey(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i("Create " + this + " saved=" + (savedInstanceState != null));
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            Bundle args = getArguments();
            if (args == null && !isStateSaved())
                setArguments(new Bundle());
        } else {
            title = savedInstanceState.getCharSequence("fair:title");
            subtitle = savedInstanceState.getCharSequence("fair:subtitle");
        }

        // https://developer.android.com/training/basics/fragments/pass-data-between
        String requestKey = getRequestKey();
        if (!BuildConfig.PLAY_STORE_RELEASE)
            EntityLog.log(getContext(), "Listening key=" + requestKey);
        getParentFragmentManager().setFragmentResultListener(requestKey, this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                try {
                    result.setClassLoader(ApplicationEx.class.getClassLoader());
                    int requestCode = result.getInt("requestCode");
                    int resultCode = result.getInt("resultCode");

                    EntityLog.log(getContext(), "Received key=" + requestKey +
                            " request=" + requestCode +
                            " result=" + resultCode);

                    Intent data = new Intent();
                    data.putExtra("args", result);
                    onActivityResult(requestCode, resultCode, data);
                } catch (Throwable ex) {
                    Log.e(ex);
                    /*
                        android.os.BadParcelableException: ClassNotFoundException when unmarshalling: eu.faircode.email.FragmentMessages$MessageTarget
                                at android.os.Parcel.readParcelableCreator(Parcel.java:2839)
                                at android.os.Parcel.readParcelable(Parcel.java:2765)
                                at android.os.Parcel.readValue(Parcel.java:2668)
                                at android.os.Parcel.readListInternal(Parcel.java:3098)
                                at android.os.Parcel.readArrayList(Parcel.java:2319)
                                at android.os.Parcel.readValue(Parcel.java:2689)
                                at android.os.Parcel.readArrayMapInternal(Parcel.java:3037)
                                at android.os.BaseBundle.initializeFromParcelLocked(BaseBundle.java:288)
                                at android.os.BaseBundle.unparcel(BaseBundle.java:232)
                                at android.os.BaseBundle.getInt(BaseBundle.java:1017)
                     */
                }
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            FragmentActivity activity = getActivity();
            if (activity != null) {
                activity.getOnBackPressedDispatcher().onBackPressed();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        EntityLog.log(getContext(), "Result class=" + this.getClass().getSimpleName() +
                " action=" + (data == null ? null : data.getAction()) +
                " request=" + requestCode +
                " result=" + resultCode + " ok=" + (resultCode == RESULT_OK) +
                " data=" + (data == null ? null : data.getData()) +
                (data == null ? "" : " " + TextUtils.join(" ", Log.getExtras(data.getExtras()))));
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

        try {
            FragmentActivity activity = getActivity();
            if (activity != null) {
                InputMethodManager imm = Helper.getSystemService(activity, InputMethodManager.class);
                View focused = activity.getCurrentFocus();
                if (imm != null && focused != null)
                    imm.hideSoftInputFromWindow(focused.getWindowToken(), 0);
            }
        } catch (Throwable ex) {
            Log.w(ex);
            /*
                Caused by: java.lang.NullPointerException: Attempt to read from field 'com.android.internal.view.IInputMethodClient com.android.server.inputmethod.InputMethodManagerService$ClientState.client' on a null object reference
                  at android.os.Parcel.createException(Parcel.java:2077)
                  at android.os.Parcel.readException(Parcel.java:2039)
                  at android.os.Parcel.readException(Parcel.java:1987)
                  at com.android.internal.view.IInputMethodManager$Stub$Proxy.hideSoftInput(IInputMethodManager.java:615)
                  at android.view.inputmethod.InputMethodManager.hideSoftInputFromWindow(InputMethodManager.java:1523)
                  at android.view.inputmethod.InputMethodManager.hideSoftInputFromWindow(InputMethodManager.java:1485)
                  at eu.faircode.email.FragmentBase.onDetach(SourceFile:5)
                  at androidx.fragment.app.Fragment.performDetach(SourceFile:3)
             */
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d("Config " + this);
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onDestroy() {
        Log.i("Destroy " + this);
        if (orientation != null) {
            Activity activity = getActivity();
            if (activity != null)
                activity.setRequestedOrientation(orientation);
        }
        super.onDestroy();
    }

    protected void lockOrientation() {
        Activity activity = getActivity();
        if (activity != null) {
            orientation = activity.getRequestedOrientation();
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        }
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
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
                    boolean list_count = prefs.getBoolean("list_count", false);

                    View custom = actionbar.getCustomView();
                    TextView tvCount = custom.findViewById(R.id.count);
                    TextView tvTitle = custom.findViewById(R.id.title);
                    TextView tvSubtitle = custom.findViewById(R.id.subtitle);
                    if (tvCount != null) {
                        tvCount.setText(count);
                        tvCount.setVisibility(!list_count || TextUtils.isEmpty(count)
                                ? View.GONE : View.VISIBLE);
                    }
                    if (tvTitle != null)
                        tvTitle.setText(title == null ? getString(R.string.app_name) : title);
                    if (tvSubtitle != null)
                        tvSubtitle.setText(subtitle);
                }
        }
    }

    protected void setBackPressedCallback(OnBackPressedCallback backPressedCallback) {
        FragmentActivity activity = getActivity();
        if (activity == null)
            return;
        backPressedCallback.setEnabled(true);
        getViewLifecycleOwner().getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
            public void onAny() {
                Lifecycle.State state = getViewLifecycleOwner().getLifecycle().getCurrentState();
                if (state.isAtLeast(Lifecycle.State.STARTED))
                    activity.getOnBackPressedDispatcher().addCallback(backPressedCallback);
                else
                    backPressedCallback.remove();
            }
        });
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
        long attachment = intent.getLongExtra("id", -1L);
        getArguments().putLong("selected_attachment", attachment);
        Log.i("Save attachment id=" + attachment);

        Intent create = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        create.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        create.setType(intent.getStringExtra("type"));
        create.putExtra(Intent.EXTRA_TITLE, intent.getStringExtra("name"));
        Helper.openAdvanced(create);
        PackageManager pm = getContext().getPackageManager();
        if (create.resolveActivity(pm) == null) { // system whitelisted
            Log.w("SAF missing");
            ToastEx.makeText(getContext(), R.string.title_no_saf, Toast.LENGTH_LONG).show();
        } else
            startActivityForResult(Helper.getChooser(getContext(), create), REQUEST_ATTACHMENT);
    }

    private void onStoreAttachments(Intent intent) {
        long message = intent.getLongExtra("id", -1L);
        getArguments().putLong("selected_message", message);
        Log.i("Save attachments message=" + message);

        Intent tree = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        Helper.openAdvanced(tree);
        PackageManager pm = getContext().getPackageManager();
        if (tree.resolveActivity(pm) == null) { // system whitelisted
            Log.w("SAF missing");
            ToastEx.makeText(getContext(), R.string.title_no_saf, Toast.LENGTH_LONG).show();
        } else
            startActivityForResult(Helper.getChooser(getContext(), tree), REQUEST_ATTACHMENTS);
    }

    private void onSaveAttachment(Intent data) {
        long attachment = getArguments().getLong("selected_attachment", -1L);

        Bundle args = new Bundle();
        args.putLong("id", attachment);
        args.putParcelable("uri", data.getData());

        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) throws Throwable {
                long id = args.getLong("id");
                Uri uri = args.getParcelable("uri");

                if (uri == null)
                    throw new FileNotFoundException();

                if (!"content".equals(uri.getScheme())) {
                    Log.w("Save attachment uri=" + uri);
                    throw new IllegalArgumentException(context.getString(R.string.title_no_stream));
                }

                DB db = DB.getInstance(context);
                EntityAttachment attachment = db.attachment().getAttachment(id);
                if (attachment == null)
                    return null;
                File file = attachment.getFile(context);

                OutputStream os = null;
                InputStream is = null;
                try {
                    os = context.getContentResolver().openOutputStream(uri);
                    is = new FileInputStream(file);

                    if (os == null)
                        throw new FileNotFoundException(uri.toString());

                    byte[] buffer = new byte[Helper.BUFFER_SIZE];
                    int read;
                    while ((read = is.read(buffer)) != -1)
                        os.write(buffer, 0, read);
                } finally {
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
        long message = getArguments().getLong("selected_message", -1L);

        Bundle args = new Bundle();
        args.putLong("id", message);
        args.putParcelable("uri", data.getData());

        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) throws Throwable {
                long id = args.getLong("id");
                Uri uri = args.getParcelable("uri");

                if (uri == null)
                    throw new FileNotFoundException();

                if (!"content".equals(uri.getScheme())) {
                    Log.w("Save attachment uri=" + uri);
                    throw new IllegalArgumentException(context.getString(R.string.title_no_stream));
                }

                DB db = DB.getInstance(context);
                DocumentFile tree = DocumentFile.fromTreeUri(context, uri);
                List<EntityAttachment> attachments = db.attachment().getAttachments(id);
                for (EntityAttachment attachment : attachments)
                    if (attachment.subsequence == null) {
                        File file = attachment.getFile(context);

                        String name = Helper.sanitizeFilename(attachment.name);
                        if (TextUtils.isEmpty(name))
                            name = Long.toString(attachment.id);
                        DocumentFile document = tree.createFile(attachment.getMimeType(), name);
                        if (document == null)
                            throw new FileNotFoundException("Could not save " + uri + ":" + name);

                        OutputStream os = null;
                        InputStream is = null;
                        try {
                            os = context.getContentResolver().openOutputStream(document.getUri());
                            is = new FileInputStream(file);

                            if (os == null)
                                throw new FileNotFoundException(uri.toString());

                            byte[] buffer = new byte[Helper.BUFFER_SIZE];
                            int read;
                            while ((read = is.read(buffer)) != -1)
                                os.write(buffer, 0, read);
                        } finally {
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
                Log.w(ex);
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
