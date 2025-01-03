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

import static android.app.ActionBar.DISPLAY_SHOW_CUSTOM;
import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.app.RecoverableSecurityException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import androidx.appcompat.widget.Toolbar;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
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

    protected void setActionBarListener(final LifecycleOwner owner, final View.OnClickListener listener) {
        final AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity == null)
            return;

        final ActionBar actionbar = activity.getSupportActionBar();
        if (actionbar == null)
            return;

        Toolbar toolbar = activity.findViewById(R.id.toolbar);

        if ((actionbar.getDisplayOptions() & DISPLAY_SHOW_CUSTOM) == 0 && toolbar == null)
            return;

        View custom = (toolbar == null ? actionbar.getCustomView() : toolbar);
        if (custom == null)
            return;

        owner.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
            public void onAny() {
                Lifecycle.State state = owner.getLifecycle().getCurrentState();
                custom.setOnClickListener(state.isAtLeast(Lifecycle.State.STARTED) ? listener : null);
                if (Lifecycle.State.DESTROYED.equals(state))
                    owner.getLifecycle().removeObserver(this);
            }
        });
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
                try {
                    setArguments(new Bundle());
                } catch (Throwable ex) {
                    Log.e(ex);
                    /*
                        java.lang.IllegalStateException: Fragment already added and state has been saved
                                at androidx.fragment.app.Fragment.setArguments(SourceFile:2)
                                at eu.faircode.email.FragmentBase.onCreate(SourceFile:4)
                                at androidx.fragment.app.Fragment.performCreate(SourceFile:7)
                                at androidx.fragment.app.FragmentStateManager.create(SourceFile:5)
                                at androidx.fragment.app.FragmentStateManager.moveToExpectedState(SourceFile:21)
                                at androidx.fragment.app.FragmentManager.executeOpsTogether(SourceFile:34)
                                at androidx.fragment.app.FragmentManager.removeRedundantOperationsAndExecute(SourceFile:10)
                                at androidx.fragment.app.FragmentManager.execSingleAction(SourceFile:5)
                                at androidx.fragment.app.BackStackRecord.commitNowAllowingStateLoss(SourceFile:2)
                                at androidx.fragment.app.FragmentStatePagerAdapter.finishUpdate(SourceFile:4)
                                at androidx.viewpager.widget.ViewPager.populate(SourceFile:51)
                                at androidx.viewpager.widget.ViewPager.populate(SourceFile:1)
                                at androidx.viewpager.widget.ViewPager.onMeasure(SourceFile:25)
                                at android.view.View.measure(View.java:26114)
                                at androidx.constraintlayout.widget.ConstraintLayout$Measurer.measure(SourceFile:62)
                                at androidx.constraintlayout.core.widgets.analyzer.BasicMeasure.measure(SourceFile:15)
                                at androidx.constraintlayout.core.widgets.analyzer.BasicMeasure.measureChildren(SourceFile:18)
                                at androidx.constraintlayout.core.widgets.analyzer.BasicMeasure.solverMeasure(SourceFile:31)
                                at androidx.constraintlayout.core.widgets.ConstraintWidgetContainer.measure(SourceFile:3)
                                at androidx.constraintlayout.widget.ConstraintLayout.resolveSystem(SourceFile:16)
                                at androidx.constraintlayout.widget.ConstraintLayout.onMeasure(SourceFile:15)
                                at android.view.View.measure(View.java:26114)
                                at android.view.ViewGroup.measureChildWithMargins(ViewGroup.java:7118)
                                at android.widget.FrameLayout.onMeasure(FrameLayout.java:194)
                                at android.view.View.measure(View.java:26114)
                                at androidx.drawerlayout.widget.DrawerLayout.onMeasure(SourceFile:43)
                                at android.view.View.measure(View.java:26114)
                                at android.view.ViewGroup.measureChildWithMargins(ViewGroup.java:7118)
                                at android.widget.FrameLayout.onMeasure(FrameLayout.java:194)
                                at androidx.appcompat.widget.ContentFrameLayout.onMeasure(SourceFile:21)
                                at android.view.View.measure(View.java:26114)
                                at android.view.ViewGroup.measureChildWithMargins(ViewGroup.java:7118)
                                at androidx.appcompat.widget.ActionBarOverlayLayout.onMeasure(SourceFile:44)
                                at android.view.View.measure(View.java:26114)
                                at android.view.ViewGroup.measureChildWithMargins(ViewGroup.java:7118)
                                at android.widget.FrameLayout.onMeasure(FrameLayout.java:194)
                                at android.view.View.measure(View.java:26114)
                                at android.view.ViewGroup.measureChildWithMargins(ViewGroup.java:7118)
                                at android.widget.LinearLayout.measureChildBeforeLayout(LinearLayout.java:1552)
                                at android.widget.LinearLayout.measureVertical(LinearLayout.java:842)
                                at android.widget.LinearLayout.onMeasure(LinearLayout.java:721)
                                at android.view.View.measure(View.java:26114)
                                at android.view.ViewGroup.measureChildWithMargins(ViewGroup.java:7118)
                                at com.android.internal.policy.DecorView.measureChildWithMargins(DecorView.java:3446)
                                at android.widget.FrameLayout.onMeasure(FrameLayout.java:194)
                                at com.android.internal.policy.DecorView.onMeasure(DecorView.java:968)
                                at android.view.View.measure(View.java:26114)
                                at android.view.ViewRootImpl.performMeasure(ViewRootImpl.java:3985)
                                at android.view.ViewRootImpl.measureHierarchy(ViewRootImpl.java:2679)
                                at android.view.ViewRootImpl.performTraversals(ViewRootImpl.java:3052)
                                at android.view.ViewRootImpl.doTraversal(ViewRootImpl.java:2391)
                                at android.view.ViewRootImpl$TraversalRunnable.run(ViewRootImpl.java:9325)
                                at android.view.Choreographer$CallbackRecord.run(Choreographer.java:1186)
                                at android.view.Choreographer.doCallbacks(Choreographer.java:986)
                                at android.view.Choreographer.doFrame(Choreographer.java:912)
                                at android.view.Choreographer$FrameDisplayEventReceiver.run(Choreographer.java:1171)
                                at android.os.Handler.handleCallback(Handler.java:938)
                                at android.os.Handler.dispatchMessage(Handler.java:99)
                     */
                }
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
    }

    @Override
    public void onPause() {
        Log.d("Pause " + this);
        super.onPause();
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
            if (actionbar != null) {
                Toolbar toolbar = activity.findViewById(R.id.toolbar);
                if ((actionbar.getDisplayOptions() & DISPLAY_SHOW_CUSTOM) == 0 && toolbar == null) {
                    actionbar.setTitle(title == null ? getString(R.string.app_name) : title);
                    actionbar.setSubtitle(subtitle);
                } else {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
                    boolean list_count = prefs.getBoolean("list_count", false);

                    View custom = (toolbar == null ? actionbar.getCustomView() : toolbar);
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

    protected void onStoreAttachment(EntityAttachment attachment) {
        getArguments().putLong("selected_attachment", attachment.id);
        Log.i("Save attachment id=" + attachment.id);

        final Context context = getContext();

        Intent create = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        create.addCategory(Intent.CATEGORY_OPENABLE);
        create.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        create.setType(attachment.getMimeType());
        create.putExtra(Intent.EXTRA_TITLE, attachment.name);
        Helper.openAdvanced(context, create);
        PackageManager pm = context.getPackageManager();
        if (create.resolveActivity(pm) == null) // system whitelisted
            Log.unexpectedError(getParentFragmentManager(),
                    new IllegalArgumentException(context.getString(R.string.title_no_saf)), 25);
        else
            startActivityForResult(Helper.getChooser(context, create), REQUEST_ATTACHMENT);
    }

    protected void onStoreAttachments(long message) {
        getArguments().putLong("selected_message", message);
        Log.i("Save attachments message=" + message);

        final Context context = getContext();

        Intent tree = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        Helper.openAdvanced(context, tree);
        PackageManager pm = context.getPackageManager();
        if (tree.resolveActivity(pm) == null) // system whitelisted
            Log.unexpectedError(getParentFragmentManager(),
                    new IllegalArgumentException(context.getString(R.string.title_no_saf)), 25);
        else
            startActivityForResult(Helper.getChooser(context, tree), REQUEST_ATTACHMENTS);
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

                boolean report = !(ex instanceof IllegalArgumentException ||
                        ex instanceof FileNotFoundException ||
                        ex instanceof SecurityException);
                Log.unexpectedError(getParentFragmentManager(), ex, report);
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

                boolean report = !(ex instanceof IllegalArgumentException ||
                        ex instanceof FileNotFoundException ||
                        ex instanceof SecurityException);
                Log.unexpectedError(getParentFragmentManager(), ex, report);
            }
        }.execute(this, args, "attachments:save");
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void handle(RecoverableSecurityException ex) {
        new AlertDialog.Builder(getContext())
                .setMessage(new ThrowableWrapper(ex).getSafeMessage())
                .setPositiveButton(ex.getUserAction().getTitle(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            startIntentSenderForResult(
                                    ex.getUserAction().getActionIntent().getIntentSender(),
                                    REQUEST_RECOVERABLE_PERMISSION,
                                    null, 0, 0, 0,
                                    Helper.getBackgroundActivityOptions());
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
