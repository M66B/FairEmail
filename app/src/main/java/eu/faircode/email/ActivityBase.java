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

import android.Manifest;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.ColorUtils;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.preference.PreferenceManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class ActivityBase extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private int themeId;
    private Context originalContext;
    private boolean visible;
    private boolean contacts;
    private List<IKeyPressedListener> keyPressedListeners = new ArrayList<>();

    @Override
    protected void attachBaseContext(Context base) {
        originalContext = base;
        super.attachBaseContext(ApplicationEx.getLocalizedContext(base));
    }

    Context getOriginalContext() {
        return originalContext;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EntityLog.log(this, "Activity create " + this.getClass().getName() +
                " version=" + BuildConfig.VERSION_NAME + BuildConfig.REVISION +
                " process=" + android.os.Process.myPid());
        Intent intent = getIntent();
        if (intent != null)
            EntityLog.log(this, intent +
                    " extras=" + TextUtils.join(", ", Log.getExtras(intent.getExtras())));

        getSupportFragmentManager().registerFragmentLifecycleCallbacks(lifecycleCallbacks, true);

        this.contacts = hasPermission(Manifest.permission.READ_CONTACTS);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        boolean secure = prefs.getBoolean("secure", false);
        if (secure)
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);

        if (!this.getClass().equals(ActivityMain.class)) {
            themeId = FragmentDialogTheme.getTheme(this);
            setTheme(themeId);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                boolean dark = Helper.isDarkTheme(this);
                Window window = getWindow();
                View view = window.getDecorView();
                int flags = view.getSystemUiVisibility();
                if (dark)
                    flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                view.setSystemUiVisibility(flags);
            }
        }

        String requestKey = getRequestKey();
        if (!BuildConfig.PLAY_STORE_RELEASE)
            EntityLog.log(this, "Listening key=" + requestKey);
        getSupportFragmentManager().setFragmentResultListener(requestKey, this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                try {
                    result.setClassLoader(ApplicationEx.class.getClassLoader());
                    int requestCode = result.getInt("requestCode");
                    int resultCode = result.getInt("resultCode");

                    EntityLog.log(ActivityBase.this, "Received key=" + requestKey +
                            " request=" + requestCode +
                            " result=" + resultCode);

                    Intent data = new Intent();
                    data.putExtra("args", result);
                    onActivityResult(requestCode, resultCode, data);
                } catch (Throwable ex) {
                    Log.e(ex);
                }
            }
        });

        prefs.registerOnSharedPreferenceChangeListener(this);

        int colorPrimaryDark = Helper.resolveColor(this, R.attr.colorPrimaryDark);

        try {
            Drawable d = getDrawable(R.drawable.baseline_mail_24);
            Bitmap bm = Bitmap.createBitmap(
                    d.getIntrinsicWidth(),
                    d.getIntrinsicHeight(),
                    Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bm);
            d.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            d.setTint(Color.WHITE);
            d.draw(canvas);

            int colorPrimary = colorPrimaryDark;
            if (colorPrimary != 0 && Color.alpha(colorPrimary) != 255) {
                Log.w("Task color primary=" + Integer.toHexString(colorPrimary));
                colorPrimary = ColorUtils.setAlphaComponent(colorPrimary, 255);
            }

            ActivityManager.TaskDescription td = new ActivityManager.TaskDescription(
                    null, bm, colorPrimary);
            setTaskDescription(td);
        } catch (Throwable ex) {
            Log.e(ex);
        }

        boolean navbar_colorize = prefs.getBoolean("navbar_colorize", false);
        if (navbar_colorize) {
            Window window = getWindow();
            if (window != null)
                window.setNavigationBarColor(colorPrimaryDark);
        }

        Fragment bfragment = getSupportFragmentManager()
                .findFragmentByTag("androidx.biometric.BiometricFragment");
        if (bfragment != null) {
            Log.e("Orphan BiometricFragment");
            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(bfragment)
                    .commitNowAllowingStateLoss();
            /*
                java.lang.RuntimeException: Unable to start activity ComponentInfo{eu.faircode.email/eu.faircode.email.ActivitySetup}: androidx.fragment.app.Fragment$InstantiationException: Unable to instantiate fragment androidx.biometric.FingerprintDialogFragment: could not find Fragment constructor
                  at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:2957)
                  at android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:3032)
                  at android.app.ActivityThread.handleRelaunchActivity(ActivityThread.java:4921)
                  at android.app.ActivityThread.-wrap19(Unknown Source:0)
                  at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1702)
                  at android.os.Handler.dispatchMessage(Handler.java:105)
                  at android.os.Looper.loop(Looper.java:164)
                  at android.app.ActivityThread.main(ActivityThread.java:6944)
                  at java.lang.reflect.Method.invoke(Native Method)
                  at com.android.internal.os.Zygote$MethodAndArgsCaller.run(Zygote.java:327)
                  at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:1374)
                Caused by: androidx.fragment.app.Fragment$InstantiationException: Unable to instantiate fragment androidx.biometric.FingerprintDialogFragment: could not find Fragment constructor
                  at androidx.fragment.app.Fragment.instantiate(SourceFile:8)
                  at androidx.fragment.app.FragmentContainer.instantiate(SourceFile:1)
                  at androidx.fragment.app.FragmentManager$3.instantiate(SourceFile:1)
                  at androidx.fragment.app.FragmentStateManager.<init>(SourceFile:12)
                  at androidx.fragment.app.FragmentManager.restoreSaveState(SourceFile:11)
                  at androidx.fragment.app.FragmentController.restoreSaveState(SourceFile:2)
                  at androidx.fragment.app.FragmentActivity$2.onContextAvailable(SourceFile:5)
                  at androidx.activity.contextaware.ContextAwareHelper.dispatchOnContextAvailable(SourceFile:3)
                  at androidx.activity.ComponentActivity.onCreate(SourceFile:2)
                  at androidx.fragment.app.FragmentActivity.onCreate(SourceFile:1)
                  at eu.faircode.email.ActivityBase.onCreate(SourceFile:37)
                  at eu.faircode.email.ActivitySetup.onCreate(SourceFile:1)
                  at android.app.Activity.performCreate(Activity.java:7183)
                  at android.app.Instrumentation.callActivityOnCreate(Instrumentation.java:1220)
                  at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:2910)
                  ... 10 more
                Caused by: java.lang.NoSuchMethodException: <init> []
                  at java.lang.Class.getConstructor0(Class.java:2320)
                  at java.lang.Class.getConstructor(Class.java:1725)
                  at androidx.fragment.app.Fragment.instantiate(SourceFile:4)
             */
        }

        checkAuthentication(true);

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent != null)
            EntityLog.log(this, "New " + intent +
                    " extras=" + TextUtils.join(", ", Log.getExtras(intent.getExtras())));
        super.onNewIntent(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        int before = Helper.getSize(outState);
        super.onSaveInstanceState(outState);
        int after = Helper.getSize(outState);
        Log.d("Saved instance " + this + " size=" + before + "/" + after);

        Map<String, String> crumb = new HashMap<>();
        crumb.put("name", this.getClass().getName());
        crumb.put("before", Integer.toString(before));
        crumb.put("after", Integer.toString(after));
        Log.breadcrumb("onSaveInstanceState", crumb);

        for (String key : outState.keySet())
            Log.d("Saved " + this + " " + key + "=" + outState.get(key));
    }

    @Override
    protected void onResume() {
        Log.d("Resume " + this.getClass().getName());
        super.onResume();

        visible = true;

        if (!(this instanceof ActivityMain)) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            prefs.edit().putString("last_activity", this.getClass().getName()).apply();
        }

        boolean contacts = hasPermission(Manifest.permission.READ_CONTACTS);
        if (this.contacts != contacts &&
                !this.getClass().equals(ActivitySetup.class) &&
                !this.getClass().equals(ActivityCompose.class)) {
            Log.i("Contacts permission=" + contacts);
            finish();
            startActivity(getIntent());
        } else
            checkAuthentication(true);
    }

    @Override
    protected void onPause() {
        Log.d("Pause " + this.getClass().getName());
        super.onPause();

        visible = false;

        checkAuthentication(false);
    }

    @Override
    public boolean onPreparePanel(int featureId, @Nullable View view, @NonNull Menu menu) {
        try {
            return super.onPreparePanel(featureId, view, menu);
        } catch (Throwable ex) {
            /*
                This should never happen, but ...
                java.lang.NullPointerException: Attempt to invoke interface method 'android.view.MenuItem android.view.MenuItem.setEnabled(boolean)' on a null object reference
                    at eu.faircode.email.FragmentCompose.onPrepareOptionsMenu(SourceFile:3)
                    at androidx.fragment.app.Fragment.performPrepareOptionsMenu(SourceFile:3)
                    at androidx.fragment.app.FragmentManager.dispatchPrepareOptionsMenu(SourceFile:3)
                    at androidx.fragment.app.FragmentManager$2.onPrepareMenu(Unknown Source:2)
                    at androidx.core.view.MenuHostHelper.onPrepareMenu(SourceFile:2)
                    at androidx.activity.ComponentActivity.onPrepareOptionsMenu(SourceFile:2)
                    at android.app.Activity.onPreparePanel(Activity.java:3391)
                    at androidx.appcompat.view.WindowCallbackWrapper.onPreparePanel(Unknown Source:2)
                    at androidx.appcompat.app.AppCompatDelegateImpl$AppCompatWindowCallback.onPreparePanel(SourceFile:4)
                    at androidx.appcompat.app.AppCompatDelegateImpl.preparePanel(SourceFile:28)
                    at androidx.appcompat.app.AppCompatDelegateImpl.doInvalidatePanelMenu(SourceFile:14)
                    at androidx.appcompat.app.AppCompatDelegateImpl$2.run(SourceFile:2)
                    at android.view.Choreographer$CallbackRecord.run(Choreographer.java:948)
                    at android.view.Choreographer.doCallbacks(Choreographer.java:750)
                    at android.view.Choreographer.doFrame(Choreographer.java:679)
                    at android.view.Choreographer$FrameDisplayEventReceiver.run(Choreographer.java:934)
                    at android.os.Handler.handleCallback(Handler.java:869)
             */
            Log.e(ex);
            return false;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d("Config " + this.getClass().getName());
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onUserInteraction() {
        Log.d("User interaction");
        checkAuthentication(true);
    }

    @Override
    protected void onUserLeaveHint() {
        Log.d("User leaving");
    }

    @Override
    protected void onStop() {
        super.onStop();

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm != null && !pm.isInteractive()) {
            Log.i("Stop with screen off");
            if (Helper.shouldAutoLock(this)) {
                Helper.clearAuthentication(this);
                lock();
            }
        }
    }

    @Override
    protected void onDestroy() {
        Log.i("Destroy " + this.getClass().getName());
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
        try {
            getSupportFragmentManager().unregisterFragmentLifecycleCallbacks(lifecycleCallbacks);
            super.onDestroy();
            originalContext = null;
        } catch (Throwable ex) {
            Log.w(ex);
            /*
                java.lang.RuntimeException: Unable to destroy activity {eu.faircode.email/eu.faircode.email.ActivityView}: java.lang.IndexOutOfBoundsException: setSpan (-1 ... -1) starts before 0
                  at android.app.ActivityThread.performDestroyActivity(ActivityThread.java:4605)
                  at android.app.ActivityThread.handleDestroyActivity(ActivityThread.java:4623)
                  at android.app.ActivityThread.-wrap5(Unknown Source:0)
                  at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1757)
                  at android.os.Handler.dispatchMessage(Handler.java:105)
                  at android.os.Looper.loop(Looper.java:164)
                  at android.app.ActivityThread.main(ActivityThread.java:6944)
                  at java.lang.reflect.Method.invoke(Native Method)
                  at com.android.internal.os.Zygote$MethodAndArgsCaller.run(Zygote.java:327)
                  at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:1374)
                Caused by: java.lang.IndexOutOfBoundsException: setSpan (-1 ... -1) starts before 0
                  at android.text.SpannableStringInternal.checkRange(SpannableStringInternal.java:442)
                  at android.text.SpannableStringInternal.setSpan(SpannableStringInternal.java:163)
                  at android.text.SpannableStringInternal.setSpan(SpannableStringInternal.java:152)
                  at android.text.SpannableString.setSpan(SpannableString.java:46)
                  at android.text.Selection.setSelection(Selection.java:76)
                  at android.widget.TextView.semSetSelection(TextView.java:13204)
                  at android.widget.Editor$SelectionModifierCursorController.resetDragAcceleratorState(Editor.java:6547)
                  at android.widget.Editor$SelectionModifierCursorController.resetTouchOffsets(Editor.java:6537)
                  at android.widget.Editor$SelectionModifierCursorController.<init>(Editor.java:6172)
                  at android.widget.Editor.getSelectionController(Editor.java:2449)
                  at android.widget.Editor.onDetachedFromWindow(Editor.java:470)
                  at android.widget.TextView.onDetachedFromWindowInternal(TextView.java:7232)
                  at android.view.View.dispatchDetachedFromWindow(View.java:18677)
                  at android.view.ViewGroup.dispatchDetachedFromWindow(ViewGroup.java:3878)
                  at android.view.ViewGroup.dispatchDetachedFromWindow(ViewGroup.java:3878)
                  at android.view.ViewGroup.dispatchDetachedFromWindow(ViewGroup.java:3878)
                  at android.view.ViewGroup.dispatchDetachedFromWindow(ViewGroup.java:3878)
                  at android.view.ViewGroup.dispatchDetachedFromWindow(ViewGroup.java:3878)
                  at android.view.ViewGroup.dispatchDetachedFromWindow(ViewGroup.java:3878)
                  at android.view.ViewGroup.dispatchDetachedFromWindow(ViewGroup.java:3878)
                  at android.view.ViewGroup.dispatchDetachedFromWindow(ViewGroup.java:3878)
                  at android.view.ViewGroup.removeViewInternal(ViewGroup.java:5432)
                  at android.view.ViewGroup.removeViewInternal(ViewGroup.java:5403)
                  at android.view.ViewGroup.removeView(ViewGroup.java:5334)
                  at androidx.fragment.app.SpecialEffectsController$Operation$State.applyState(SourceFile:14)
                  at androidx.fragment.app.SpecialEffectsController$1.run(SourceFile:2)
                  at androidx.fragment.app.SpecialEffectsController$Operation.complete(SourceFile:6)
                  at androidx.fragment.app.SpecialEffectsController$FragmentStateManagerOperation.complete(SourceFile:1)
                  at androidx.fragment.app.SpecialEffectsController$Operation.cancel(SourceFile:4)
                  at androidx.fragment.app.SpecialEffectsController.forceCompleteAllOperations(SourceFile:21)
                  at androidx.fragment.app.FragmentManager.dispatchStateChange(SourceFile:6)
                  at androidx.fragment.app.FragmentManager.dispatchDestroy(SourceFile:4)
                  at androidx.fragment.app.FragmentController.dispatchDestroy(SourceFile:1)
                  at androidx.fragment.app.FragmentActivity.onDestroy(SourceFile:2)
                  at androidx.appcompat.app.AppCompatActivity.onDestroy(SourceFile:1)
                  at eu.faircode.email.ActivityBase.onDestroy(SourceFile:3)
                  at eu.faircode.email.ActivityBilling.onDestroy(SourceFile:3)
                  at eu.faircode.email.ActivityView.onDestroy(SourceFile:3)
                  at android.app.Activity.performDestroy(Activity.java:7522)
             */
        }
    }

    public int getThemeId() {
        return this.themeId;
    }

    public String getRequestKey() {
        return this.getClass().getName() + ":activity";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        EntityLog.log(this, "Result class=" + this.getClass().getSimpleName() +
                " action=" + (data == null ? null : data.getAction()) +
                " request=" + requestCode +
                " result=" + resultCode + " ok=" + (resultCode == RESULT_OK) +
                " data=" + (data == null ? null : data.getData()) +
                (data == null ? "" : " " + TextUtils.join(" ", Log.getExtras(data.getExtras()))));
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void checkAuthentication(boolean auth) {
        if (this.getClass().equals(ActivityMain.class))
            return;

        if (!Helper.shouldAuthenticate(this, !auth))
            return;

        lock();

        if (auth) {
            if (this instanceof ActivityWidget ||
                    this instanceof ActivityWidgetSync ||
                    this instanceof ActivityWidgetUnified) {
                Toast.makeText(this, R.string.title_notification_redacted, Toast.LENGTH_LONG).show();
            } else {
                Intent intent = getIntent();
                processStreams(intent);
                Intent main = new Intent(this, ActivityMain.class)
                        .putExtra("intent", intent);
                main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(main);
            }
        }
    }

    private void lock() {
        finishAndRemoveTask();
        setResult(RESULT_CANCELED);
        finishAffinity();
    }

    private void processStreams(Intent intent) {
        intent.setClipData(null);

        if (intent.hasExtra(Intent.EXTRA_STREAM)) {
            if (Intent.ACTION_SEND_MULTIPLE.equals(intent.getAction())) {
                ArrayList<Uri> uris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                if (uris == null)
                    intent.removeExtra(Intent.EXTRA_STREAM);
                else {
                    ArrayList<Uri> processed = new ArrayList<>();
                    for (Uri uri : uris)
                        if (uri != null)
                            processed.add(processUri(uri));
                    intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, processed);
                }
            } else {
                Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (uri == null)
                    intent.removeExtra(Intent.EXTRA_STREAM);
                else
                    intent.putExtra(Intent.EXTRA_STREAM, processUri(uri));
            }
        }
    }

    private Uri processUri(Uri uri) {
        try {
            String fname = null;
            try {
                DocumentFile dfile = DocumentFile.fromSingleUri(this, uri);
                if (dfile != null)
                    fname = dfile.getName();
            } catch (SecurityException ex) {
                Log.e(ex);
            }

            if (TextUtils.isEmpty(fname))
                fname = uri.getLastPathSegment();

            if (TextUtils.isEmpty(fname))
                return uri;

            File dir = Helper.ensureExists(new File(getFilesDir(), "shared"));
            File file = new File(dir, fname);

            Log.i("Copying shared file to " + file);
            InputStream is = getContentResolver().openInputStream(uri);
            if (is == null)
                throw new FileNotFoundException(uri.toString());

            Helper.copy(is, new FileOutputStream(file));

            return FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID, file);
        } catch (Throwable ex) {
            Log.w(ex);
            return uri;
        }
    }

    @Override
    public void startActivity(Intent intent) {
        try {
            Log.i("Start intent=" + intent);
            Log.logExtras(intent);
            super.startActivity(intent);
        } catch (Throwable ex) {
            if (this instanceof ActivityMain)
                throw ex;
            if (intent.getPackage() == null)
                Helper.reportNoViewer(this, intent, ex);
            else {
                intent.setPackage(null);
                try {
                    super.startActivity(intent);
                } catch (Throwable exex) {
                    Helper.reportNoViewer(this, intent, exex);
                }
            }
        }
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        try {
            Log.i("Start intent=" + intent + " request=" + requestCode);
            Log.logExtras(intent);
            super.startActivityForResult(intent, requestCode);
        } catch (Throwable ex) {
            if (intent.getPackage() == null)
                Helper.reportNoViewer(this, intent, ex);
            else {
                intent.setPackage(null);
                try {
                    super.startActivityForResult(intent, requestCode);
                } catch (Throwable exex) {
                    Helper.reportNoViewer(this, intent, exex);
                }
            }
        }
    }

    @Override
    public ComponentName startService(Intent service) {
        try {
            return super.startService(service);
        } catch (Throwable ex) {
            Log.w(ex);
            /*
                Caused by: java.lang.IllegalStateException: Not allowed to start service Intent { act=clear:0 cmp=eu.faircode.email/.ServiceUI }: app is in background uid UidRecord{cb19b35 u0a286 TRNB idle change:uncached procs:1 proclist:1344, seq(0,0,0)}
                        at android.app.ContextImpl.startServiceCommon(ContextImpl.java:1666)
                        at android.app.ContextImpl.startService(ContextImpl.java:1611)
                        at android.content.ContextWrapper.startService(ContextWrapper.java:677)
                        at android.content.ContextWrapper.startService(ContextWrapper.java:677)
                        at eu.faircode.email.ActivityView.checkIntent(SourceFile:873)
                        at eu.faircode.email.ActivityView.onResume(SourceFile:595)
             */
            return null;
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        Log.i("Preference " + key + "=" + prefs.getAll().get(key));
        if ("theme".equals(key) || "beige".equals(key)) {
            finish();
            if (visible &&
                    (this.getClass().equals(ActivitySetup.class) ||
                            this.getClass().equals(ActivityView.class)))
                startActivity(getIntent());
        } else if (!this.getClass().equals(ActivitySetup.class) && !visible &&
                Arrays.asList(FragmentOptions.OPTIONS_RESTART).contains(key))
            finish();
    }

    public boolean hasPermission(String name) {
        return Helper.hasPermission(this, name);
    }

    void addKeyPressedListener(final IKeyPressedListener listener, LifecycleOwner owner) {
        Log.d("Adding back listener=" + listener);
        keyPressedListeners.add(listener);

        owner.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            public void onDestroyed() {
                Log.d("Removing back listener=" + listener);
                keyPressedListeners.remove(listener);
                owner.getLifecycle().removeObserver(this);
            }
        });
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        for (IKeyPressedListener listener : keyPressedListeners)
            if (listener.onKeyPressed(event))
                return true;
        try {
            return super.dispatchKeyEvent(event);
        } catch (Throwable ex) {
            Log.w(ex);
            /*
            java.lang.IllegalArgumentException
                    at com.android.internal.util.Preconditions.checkArgument(Preconditions.java:33)
                    at android.widget.SelectionActionModeHelper$TextClassificationHelper.init(SelectionActionModeHelper.java:974)
                    at android.widget.SelectionActionModeHelper.resetTextClassificationHelper(SelectionActionModeHelper.java:462)
                    at android.widget.SelectionActionModeHelper.resetTextClassificationHelper(SelectionActionModeHelper.java:470)
                    at android.widget.SelectionActionModeHelper.startSelectionActionModeAsync(SelectionActionModeHelper.java:118)
                    at android.widget.Editor.startSelectionActionModeAsync(Editor.java:2131)
                    at android.widget.Editor.refreshTextActionMode(Editor.java:2076)
                    at android.widget.TextView.spanChange(TextView.java:9903)
                    at android.widget.TextView$ChangeWatcher.onSpanChanged(TextView.java:12534)
                    at android.text.SpannableStringBuilder.sendSpanChanged(SpannableStringBuilder.java:1303)
                    at android.text.SpannableStringBuilder.setSpan(SpannableStringBuilder.java:750)
                    at android.text.SpannableStringBuilder.setSpan(SpannableStringBuilder.java:674)
                    at android.text.Selection.extendSelection(Selection.java:174)
                    at android.text.Selection.setSelectionAndMemory(Selection.java:256)
                    at android.text.Selection.extendUp(Selection.java:357)
                    at android.text.method.ArrowKeyMovementMethod.up(ArrowKeyMovementMethod.java:92)
                    at android.text.method.BaseMovementMethod.handleMovementKey(BaseMovementMethod.java:189)
                    at android.text.method.ArrowKeyMovementMethod.handleMovementKey(ArrowKeyMovementMethod.java:65)
                    at android.text.method.BaseMovementMethod.onKeyDown(BaseMovementMethod.java:42)
                    at android.widget.TextView.doKeyDown(TextView.java:7691)
                    at android.widget.TextView.onKeyDown(TextView.java:7442)
                    at android.view.KeyEvent.dispatch(KeyEvent.java:2692)
                    at android.view.View.dispatchKeyEvent(View.java:12471)
                    at android.view.ViewGroup.dispatchKeyEvent(ViewGroup.java:1896)
                    at android.view.ViewGroup.dispatchKeyEvent(ViewGroup.java:1896)
                    at android.widget.ScrollView.dispatchKeyEvent(ScrollView.java:389)
                    at android.view.ViewGroup.dispatchKeyEvent(ViewGroup.java:1896)
                    at android.view.ViewGroup.dispatchKeyEvent(ViewGroup.java:1896)
                    at android.view.ViewGroup.dispatchKeyEvent(ViewGroup.java:1896)
                    at android.view.ViewGroup.dispatchKeyEvent(ViewGroup.java:1896)
                    at android.view.ViewGroup.dispatchKeyEvent(ViewGroup.java:1896)
                    at android.view.ViewGroup.dispatchKeyEvent(ViewGroup.java:1896)
                    at android.view.ViewGroup.dispatchKeyEvent(ViewGroup.java:1896)
                    at android.view.ViewGroup.dispatchKeyEvent(ViewGroup.java:1896)
                    at com.android.internal.policy.DecorView.superDispatchKeyEvent(DecorView.java:451)
                    at com.android.internal.policy.PhoneWindow.superDispatchKeyEvent(PhoneWindow.java:1830)
                    at android.app.Activity.dispatchKeyEvent(Activity.java:3385)
                    at androidx.core.app.ComponentActivity.superDispatchKeyEvent(SourceFile:122)
                    at androidx.core.view.KeyEventDispatcher.dispatchKeyEvent(SourceFile:84)
                    at androidx.core.app.ComponentActivity.dispatchKeyEvent(SourceFile:140)
                    at androidx.appcompat.app.AppCompatActivity.dispatchKeyEvent(SourceFile:559)
             */
            return false;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        try {
            return super.dispatchTouchEvent(ev);
        } catch (Throwable ex) {
            Log.w(ex);
            /*
                java.lang.IllegalArgumentException: captureChildView: parameter must be a descendant of the ViewDragHelper's tracked parent view (androidx.coordinatorlayout.widget.CoordinatorLayout{35ad956 V.E...... ........ 0,0-1080,2100})
                        at androidx.customview.widget.ViewDragHelper.captureChildView(ViewDragHelper:472)
                        at androidx.customview.widget.ViewDragHelper.tryCaptureViewForDrag(ViewDragHelper:914)
                        at androidx.customview.widget.ViewDragHelper.processTouchEvent(ViewDragHelper:1155)
                        at com.google.android.material.behavior.SwipeDismissBehavior.onTouchEvent(SwipeDismissBehavior:215)
                        at androidx.coordinatorlayout.widget.CoordinatorLayout.onTouchEvent(CoordinatorLayout:563)
                        at android.view.View.dispatchTouchEvent(View.java:13483)
                        at android.view.ViewGroup.dispatchTransformedTouchEvent(ViewGroup.java:3082)
                        at android.view.ViewGroup.dispatchTouchEvent(ViewGroup.java:2767)
                        at android.view.ViewGroup.dispatchTransformedTouchEvent(ViewGroup.java:3088)
                        at android.view.ViewGroup.dispatchTouchEvent(ViewGroup.java:2781)
                        at android.view.ViewGroup.dispatchTransformedTouchEvent(ViewGroup.java:3088)
                        at android.view.ViewGroup.dispatchTouchEvent(ViewGroup.java:2781)
                        at android.view.ViewGroup.dispatchTransformedTouchEvent(ViewGroup.java:3088)
                        at android.view.ViewGroup.dispatchTouchEvent(ViewGroup.java:2781)
                        at android.view.ViewGroup.dispatchTransformedTouchEvent(ViewGroup.java:3088)
                        at android.view.ViewGroup.dispatchTouchEvent(ViewGroup.java:2781)
                        at android.view.ViewGroup.dispatchTransformedTouchEvent(ViewGroup.java:3088)
                        at android.view.ViewGroup.dispatchTouchEvent(ViewGroup.java:2781)
                        at android.view.ViewGroup.dispatchTransformedTouchEvent(ViewGroup.java:3088)
                        at android.view.ViewGroup.dispatchTouchEvent(ViewGroup.java:2781)
                        at android.view.ViewGroup.dispatchTransformedTouchEvent(ViewGroup.java:3088)
                        at android.view.ViewGroup.dispatchTouchEvent(ViewGroup.java:2781)
                        at com.android.internal.policy.DecorView.superDispatchTouchEvent(DecorView.java:496)
                        at com.android.internal.policy.PhoneWindow.superDispatchTouchEvent(PhoneWindow.java:1853)
                        at android.app.Activity.dispatchTouchEvent(Activity.java:4059)
                        at androidx.appcompat.view.WindowCallbackWrapper.dispatchTouchEvent(WindowCallbackWrapper:69)
                        at com.android.internal.policy.DecorView.dispatchTouchEvent(DecorView.java:454)
                        at android.view.View.dispatchPointerEvent(View.java:13744)
                        at android.view.ViewRootImpl$ViewPostImeInputStage.processPointerEvent(ViewRootImpl.java:5635)
                        at android.view.ViewRootImpl$ViewPostImeInputStage.onProcess(ViewRootImpl.java:5435)
                        at android.view.ViewRootImpl$InputStage.deliver(ViewRootImpl.java:4936)
                        at android.view.ViewRootImpl$InputStage.onDeliverToNext(ViewRootImpl.java:4989)
                        at android.view.ViewRootImpl$InputStage.forward(ViewRootImpl.java:4955)
                        at android.view.ViewRootImpl$AsyncInputStage.forward(ViewRootImpl.java:5095)
                        at android.view.ViewRootImpl$InputStage.apply(ViewRootImpl.java:4963)
                        at android.view.ViewRootImpl$AsyncInputStage.apply(ViewRootImpl.java:5152)
                        at android.view.ViewRootImpl$InputStage.deliver(ViewRootImpl.java:4936)
                        at android.view.ViewRootImpl$InputStage.onDeliverToNext(ViewRootImpl.java:4989)
                        at android.view.ViewRootImpl$InputStage.forward(ViewRootImpl.java:4955)
                        at android.view.ViewRootImpl$InputStage.apply(ViewRootImpl.java:4963)
                        at android.view.ViewRootImpl$InputStage.deliver(ViewRootImpl.java:4936)
                        at android.view.ViewRootImpl.deliverInputEvent(ViewRootImpl.java:7688)
                        at android.view.ViewRootImpl.doProcessInputEvents(ViewRootImpl.java:7657)
                        at android.view.ViewRootImpl.enqueueInputEvent(ViewRootImpl.java:7618)
                        at android.view.ViewRootImpl$WindowInputEventReceiver.onInputEvent(ViewRootImpl.java:7818)
                        at android.view.InputEventReceiver.dispatchInputEvent(InputEventReceiver.java:251)
                        at android.os.MessageQueue.nativePollOnce(MessageQueue.java:-2)
                        at android.os.MessageQueue.next(MessageQueue.java:336)
                        at android.os.Looper.loop(Looper.java:181)
                        at android.app.ActivityThread.main(ActivityThread.java:7562)
             */
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            return super.onTouchEvent(event);
        } catch (Throwable ex) {
            Log.w(ex);
            /*
                java.lang.IllegalArgumentException: pointerIndex out of range
                        at android.view.MotionEvent.nativeGetAxisValue(MotionEvent.java:-2)
                        at android.view.MotionEvent.getX(MotionEvent.java:2379)
                        at androidx.viewpager.widget.ViewPager.onTouchEvent(SourceFile:2259)
                        at android.view.View.dispatchTouchEvent(View.java:14002)
                        at android.view.ViewGroup.dispatchTransformedTouchEvent(ViewGroup.java:3136)
                        at android.view.ViewGroup.dispatchTouchEvent(ViewGroup.java:2820)
                        at android.view.ViewGroup.dispatchTransformedTouchEvent(ViewGroup.java:3142)
                        at android.view.ViewGroup.dispatchTouchEvent(ViewGroup.java:2834)
                        at com.android.internal.policy.DecorView.superDispatchTouchEvent(DecorView.java:495)
                        at com.android.internal.policy.PhoneWindow.superDispatchTouchEvent(PhoneWindow.java:1868)
                        at android.app.Activity.dispatchTouchEvent(Activity.java:4022)
                        at androidx.appcompat.view.WindowCallbackWrapper.dispatchTouchEvent(SourceFile:69)
                        at com.android.internal.policy.DecorView.dispatchTouchEvent(DecorView.java:453)
                        at android.view.View.dispatchPointerEvent(View.java:14261)
             */
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Delegate to fragment first
            if (super.onOptionsItemSelected(item))
                return true;
            performBack();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void performBack() {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
            // https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/app/Activity.java#3896
            ActionBar ab = getSupportActionBar();
            if (ab != null && ab.collapseActionView())
                return;
            FragmentManager fm = getSupportFragmentManager();
            if (!fm.isStateSaved() && fm.popBackStackImmediate())
                return;
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        try {
            super.onBackPressed();
        } catch (Throwable ex) {
            Log.w(ex);
            /*
                java.lang.NullPointerException: Attempt to invoke virtual method 'android.os.Handler android.app.FragmentHostCallback.getHandler()' on a null object reference
                        at android.app.FragmentManagerImpl.ensureExecReady(FragmentManager.java:2008)
                        at android.app.FragmentManagerImpl.execPendingActions(FragmentManager.java:2061)
                        at android.app.FragmentManagerImpl.popBackStackImmediate(FragmentManager.java:874)
                        at android.app.FragmentManagerImpl.popBackStackImmediate(FragmentManager.java:835)
                        at android.app.Activity.onBackPressed(Activity.java:3963)
                        at androidx.activity.ComponentActivity.access$001(Unknown)
                        at androidx.activity.ComponentActivity$1.run(SourceFile:1)
                        at androidx.activity.OnBackPressedDispatcher.onBackPressed(SourceFile:8)
                        at androidx.activity.f.run(Unknown:2)
                        at androidx.activity.g.onBackInvoked(Unknown:2)
                        at android.window.WindowOnBackInvokedDispatcher$OnBackInvokedCallbackWrapper.lambda$onBackInvoked$3$android-window-WindowOnBackInvokedDispatcher$OnBackInvokedCallbackWrapper(WindowOnBackInvokedDispatcher.java:267)
                        at android.window.WindowOnBackInvokedDispatcher$OnBackInvokedCallbackWrapper$$ExternalSyntheticLambda0.run(Unknown:2)
                        at android.os.Handler.handleCallback(Handler.java:942)
                        at android.os.Handler.dispatchMessage(Handler.java:99)
             */
        }
    }

    public void onBackPressedFragment() {
        performBack();
    }

    @Override
    public boolean shouldUpRecreateTask(Intent targetIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ComponentName cn = targetIntent.getComponent();
            if (cn != null && BuildConfig.APPLICATION_ID.equals(cn.getPackageName()))
                return false;
        }
        return super.shouldUpRecreateTask(targetIntent);
    }

    Handler getMainHandler() {
        return ApplicationEx.getMainHandler();
    }

    private final FragmentManager.FragmentLifecycleCallbacks lifecycleCallbacks = new FragmentManager.FragmentLifecycleCallbacks() {
        private long last = 0;

        @Override
        public void onFragmentPreAttached(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull Context context) {
            log(fm, f, "onFragmentPreAttached");
        }

        @Override
        public void onFragmentAttached(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull Context context) {
            log(fm, f, "onFragmentAttached");
        }

        @Override
        public void onFragmentPreCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
            log(fm, f, "onFragmentPreCreated");
        }

        @Override
        public void onFragmentCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
            log(fm, f, "onFragmentCreated");
        }

        @Override
        public void onFragmentActivityCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
            log(fm, f, "onFragmentActivityCreated");
        }

        @Override
        public void onFragmentViewCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull View v, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
            log(fm, f, "onFragmentViewCreated");
        }

        @Override
        public void onFragmentStarted(@NonNull FragmentManager fm, @NonNull Fragment f) {
            log(fm, f, "onFragmentStarted");
        }

        @Override
        public void onFragmentResumed(@NonNull FragmentManager fm, @NonNull Fragment f) {
            log(fm, f, "onFragmentResumed");
        }

        @Override
        public void onFragmentPaused(@NonNull FragmentManager fm, @NonNull Fragment f) {
            log(fm, f, "onFragmentPaused");
        }

        @Override
        public void onFragmentStopped(@NonNull FragmentManager fm, @NonNull Fragment f) {
            log(fm, f, "onFragmentStopped");
        }

        @Override
        public void onFragmentSaveInstanceState(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull Bundle outState) {
            log(fm, f, "onFragmentSaveInstanceState");
        }

        @Override
        public void onFragmentViewDestroyed(@NonNull FragmentManager fm, @NonNull Fragment f) {
            log(fm, f, "onFragmentViewDestroyed");
            Helper.clearViews(f);
        }

        @Override
        public void onFragmentDestroyed(@NonNull FragmentManager fm, @NonNull Fragment f) {
            log(fm, f, "onFragmentDestroyed");
        }

        @Override
        public void onFragmentDetached(@NonNull FragmentManager fm, @NonNull Fragment f) {
            log(fm, f, "onFragmentDetached");
        }

        private void log(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull String what) {
            long start = last;
            last = SystemClock.elapsedRealtime();
            long elapsed = (start == 0 ? 0 : last - start);
            Log.i(f.getClass().getSimpleName() + " " + what + " " + elapsed + " ms");
        }
    };

    public interface IKeyPressedListener {
        boolean onKeyPressed(KeyEvent event);
    }
}
