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

import android.Manifest;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.preference.PreferenceManager;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class ActivityBase extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
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
                " version=" + BuildConfig.VERSION_NAME +
                " process=" + android.os.Process.myPid());
        Intent intent = getIntent();
        if (intent != null)
            EntityLog.log(this, intent +
                    " extras=" + TextUtils.join(", ", Log.getExtras(intent.getExtras())));

        this.contacts = hasPermission(Manifest.permission.READ_CONTACTS);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        boolean secure = prefs.getBoolean("secure", false);
        if (secure)
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);

        if (!this.getClass().equals(ActivityMain.class)) {
            String theme = prefs.getString("theme", "blue_orange_system");

            boolean night = Helper.isNight(this);
            EntityLog.log(this, "Activity theme=" + theme + " night=" + night);

            switch (theme) {
                // Light
                case "light":
                case "blue_orange_light":
                    setTheme(R.style.AppThemeBlueOrangeLight);
                    break;
                case "orange_blue_light":
                    setTheme(R.style.AppThemeOrangeBlueLight);
                    break;

                case "yellow_purple_light":
                    setTheme(R.style.AppThemeYellowPurpleLight);
                    break;
                case "purple_yellow_light":
                    setTheme(R.style.AppThemePurpleYellowLight);
                    break;

                case "red_green_light":
                    setTheme(R.style.AppThemeRedGreenLight);
                    break;
                case "green_red_light":
                    setTheme(R.style.AppThemeGreenRedLight);
                    break;

                // Dark
                case "dark":
                case "blue_orange_dark":
                    setTheme(R.style.AppThemeBlueOrangeDark);
                    break;
                case "orange_blue_dark":
                    setTheme(R.style.AppThemeOrangeBlueDark);
                    break;

                case "yellow_purple_dark":
                    setTheme(R.style.AppThemeYellowPurpleDark);
                    break;
                case "purple_yellow_dark":
                    setTheme(R.style.AppThemePurpleYellowDark);
                    break;

                case "red_green_dark":
                    setTheme(R.style.AppThemeRedGreenDark);
                    break;
                case "green_red_dark":
                    setTheme(R.style.AppThemeGreenRedDark);
                    break;

                // Black
                case "blue_orange_black":
                    setTheme(R.style.AppThemeBlueOrangeBlack);
                    break;
                case "orange_blue_black":
                    setTheme(R.style.AppThemeOrangeBlueBlack);
                    break;
                case "yellow_purple_black":
                    setTheme(R.style.AppThemeYellowPurpleBlack);
                    break;
                case "purple_yellow_black":
                    setTheme(R.style.AppThemePurpleYellowBlack);
                    break;
                case "red_green_black":
                    setTheme(R.style.AppThemeRedGreenBlack);
                    break;
                case "green_red_black":
                    setTheme(R.style.AppThemeGreenRedBlack);
                    break;

                // Grey
                case "grey_light":
                    setTheme(R.style.AppThemeGreySteelBlueLight);
                    break;
                case "grey_dark":
                    setTheme(R.style.AppThemeGreySteelBlueDark);
                    break;

                // Black
                case "black":
                    setTheme(R.style.AppThemeBlack);
                    break;

                case "black_and_white":
                    setTheme(R.style.AppThemeBlackAndWhite);
                    break;

                // System
                case "system":
                case "blue_orange_system":
                    setTheme(night
                            ? R.style.AppThemeBlueOrangeDark : R.style.AppThemeBlueOrangeLight);
                    break;
                case "blue_orange_system_black":
                    setTheme(night
                            ? R.style.AppThemeBlueOrangeBlack : R.style.AppThemeBlueOrangeLight);
                    break;
                case "orange_blue_system":
                    setTheme(night
                            ? R.style.AppThemeOrangeBlueDark : R.style.AppThemeOrangeBlueLight);
                    break;
                case "orange_blue_system_black":
                    setTheme(night
                            ? R.style.AppThemeOrangeBlueBlack : R.style.AppThemeOrangeBlueLight);
                    break;
                case "yellow_purple_system":
                    setTheme(night
                            ? R.style.AppThemeYellowPurpleDark : R.style.AppThemeYellowPurpleLight);
                    break;
                case "yellow_purple_system_black":
                    setTheme(night
                            ? R.style.AppThemeYellowPurpleBlack : R.style.AppThemeYellowPurpleLight);
                    break;
                case "purple_yellow_system":
                    setTheme(night
                            ? R.style.AppThemePurpleYellowDark : R.style.AppThemePurpleYellowLight);
                    break;
                case "purple_yellow_system_black":
                    setTheme(night
                            ? R.style.AppThemePurpleYellowBlack : R.style.AppThemePurpleYellowLight);
                    break;
                case "red_green_system":
                    setTheme(night
                            ? R.style.AppThemeRedGreenDark : R.style.AppThemeRedGreenLight);
                    break;
                case "red_green_system_black":
                    setTheme(night
                            ? R.style.AppThemeRedGreenBlack : R.style.AppThemeRedGreenLight);
                    break;
                case "green_red_system":
                    setTheme(night
                            ? R.style.AppThemeGreenRedDark : R.style.AppThemeGreenRedLight);
                    break;
                case "green_red_system_black":
                    setTheme(night
                            ? R.style.AppThemeGreenRedBlack : R.style.AppThemeGreenRedLight);
                    break;
                case "grey_system":
                    setTheme(night
                            ? R.style.AppThemeGreySteelBlueDark : R.style.AppThemeGreySteelBlueLight);
                    break;
            }

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

        prefs.registerOnSharedPreferenceChangeListener(this);

        checkAuthentication();

        int colorPrimaryDark = Helper.resolveColor(this, R.attr.colorPrimaryDark);
        int colorActionForeground = Helper.resolveColor(this, R.attr.colorActionForeground);

        Drawable d = getDrawable(R.drawable.baseline_mail_24);
        Bitmap bm = Bitmap.createBitmap(
                d.getIntrinsicWidth(),
                d.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);
        d.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        d.setTint(colorActionForeground);
        d.draw(canvas);

        ActivityManager.TaskDescription td = new ActivityManager.TaskDescription(null, bm, colorPrimaryDark);
        setTaskDescription(td);

        boolean navbar_colorize = prefs.getBoolean("navbar_colorize", false);
        if (navbar_colorize) {
            Window window = getWindow();
            if (window != null)
                window.setNavigationBarColor(Helper.resolveColor(this, R.attr.colorPrimaryDark));
        }

        super.onCreate(savedInstanceState);
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
        crumb.put("free", Integer.toString(Log.getFreeMemMb()));
        Log.breadcrumb("onSaveInstanceState", crumb);

        for (String key : outState.keySet())
            Log.d("Saved " + this + " " + key + "=" + outState.get(key));
    }

    @Override
    protected void onResume() {
        Log.d("Resume " + this.getClass().getName());
        super.onResume();

        visible = true;

        boolean contacts = hasPermission(Manifest.permission.READ_CONTACTS);
        if (this.contacts != contacts &&
                !this.getClass().equals(ActivitySetup.class) &&
                !this.getClass().equals(ActivityCompose.class)) {
            Log.i("Contacts permission=" + contacts);
            finish();
            startActivity(getIntent());
        } else
            checkAuthentication();
    }

    @Override
    protected void onPause() {
        Log.d("Pause " + this.getClass().getName());
        super.onPause();

        visible = false;

        if (!this.getClass().equals(ActivityMain.class) && Helper.shouldAuthenticate(this))
            finishAndRemoveTask();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d("Config " + this.getClass().getName());
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onUserInteraction() {
        Log.d("User interaction");

        if (!this.getClass().equals(ActivityMain.class) && Helper.shouldAuthenticate(this)) {
            finishAndRemoveTask();
            Intent main = new Intent(this, ActivityMain.class);
            main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(main);
        }
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
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            boolean biometrics = prefs.getBoolean("biometrics", false);
            String pin = prefs.getString("pin", null);
            if (biometrics || !TextUtils.isEmpty(pin)) {
                Helper.clearAuthentication(this);
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        Log.i("Destroy " + this.getClass().getName());
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        String action = (data == null ? null : data.getAction());
        Log.i("Result class=" + this.getClass().getSimpleName() +
                " action=" + action + " request=" + requestCode + " result=" + resultCode);
        Log.logExtras(data);
        if (data != null)
            Log.i("data=" + data.getData());
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void checkAuthentication() {
        if (!this.getClass().equals(ActivityMain.class) && Helper.shouldAuthenticate(this)) {
            Intent intent = getIntent();
            finishAndRemoveTask();
            processStreams(intent);
            Intent main = new Intent(this, ActivityMain.class)
                    .putExtra("intent", intent);
            main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(main);
        }
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

            File dir = new File(getCacheDir(), "shared");
            if (!dir.exists())
                dir.mkdir();

            File file = new File(dir, fname);

            Log.i("Copying shared file to " + file);
            Helper.copy(getContentResolver().openInputStream(uri), new FileOutputStream(file));

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
        } catch (ActivityNotFoundException ex) {
            Log.w(ex);
            Helper.reportNoViewer(this, intent);
        } catch (Throwable ex) {
            Log.e(ex);
            ToastEx.makeText(this, Log.formatThrowable(ex), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        try {
            Log.i("Start intent=" + intent + " request=" + requestCode);
            Log.logExtras(intent);
            super.startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException ex) {
            Log.w(ex);
            if (Helper.isTnef(intent.getType(), null))
                Helper.viewFAQ(this, 155);
            else
                Helper.reportNoViewer(this, intent);
        } catch (Throwable ex) {
            Log.e(ex);
            ToastEx.makeText(this, Log.formatThrowable(ex), Toast.LENGTH_LONG).show();
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
        if ("theme".equals(key)) {
            finish();
            if (this.getClass().equals(ActivitySetup.class) || this.getClass().equals(ActivityView.class))
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
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (backHandled())
            return;
        super.onBackPressed();
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
            if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected boolean backHandled() {
        for (IKeyPressedListener listener : keyPressedListeners)
            if (listener.onBackPressed())
                return true;
        return false;
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

    public interface IKeyPressedListener {
        boolean onKeyPressed(KeyEvent event);

        boolean onBackPressed();
    }
}
