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

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.preference.PreferenceManager;

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
        Log.i("Create " + this.getClass().getName() + " version=" + BuildConfig.VERSION_NAME);
        Intent intent = getIntent();
        if (intent != null) {
            Log.i(intent.toString());
            Log.logBundle(intent.getExtras());
        }

        this.contacts = hasPermission(Manifest.permission.READ_CONTACTS);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        boolean secure = prefs.getBoolean("secure", false);
        if (secure)
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);

        if (!this.getClass().equals(ActivityMain.class)) {
            String theme = prefs.getString("theme", "light");

            // https://developer.android.com/guide/topics/ui/look-and-feel/darktheme#configuration_changes
            int uiMode = getResources().getConfiguration().uiMode;
            boolean night = ((uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES);
            Log.i("theme=" + theme + " UI mode=" + uiMode + " night=" + night);

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

                // System
                case "system":
                case "blue_orange_system":
                    setTheme(night
                            ? R.style.AppThemeBlueOrangeDark : R.style.AppThemeBlueOrangeLight);
                    break;
                case "orange_blue_system":
                    setTheme(night
                            ? R.style.AppThemeOrangeBlueDark : R.style.AppThemeOrangeBlueLight);
                    break;
                case "yellow_purple_system":
                    setTheme(night
                            ? R.style.AppThemeYellowPurpleDark : R.style.AppThemeYellowPurpleLight);
                    break;
                case "purple_yellow_system":
                    setTheme(night
                            ? R.style.AppThemePurpleYellowDark : R.style.AppThemePurpleYellowLight);
                    break;
                case "red_green_system":
                    setTheme(night
                            ? R.style.AppThemeRedGreenDark : R.style.AppThemeRedGreenLight);
                    break;
                case "green_red_system":
                    setTheme(night
                            ? R.style.AppThemeGreenRedDark : R.style.AppThemeGreenRedLight);
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
        Log.breadcrumb("onSaveInstanceState", crumb);

        for (String key : outState.keySet())
            Log.d("Saved " + this + " " + key + "=" + outState.get(key));
    }

    @Override
    protected void onResume() {
        Log.d("Resume " + this.getClass().getName());

        visible = true;

        boolean contacts = hasPermission(Manifest.permission.READ_CONTACTS);
        if (!this.getClass().equals(ActivitySetup.class) && this.contacts != contacts) {
            Log.i("Contacts permission=" + contacts);
            finish();
            startActivity(getIntent());
        } else
            checkAuthentication();

        try {
            super.onResume();
        } catch (Throwable ex) {
            Log.w(ex);
            /*
                java.lang.RuntimeException: Unable to resume activity {eu.faircode.email/eu.faircode.email.ActivityView}: java.lang.IllegalArgumentException
                  at android.app.ActivityThread.performResumeActivity(ActivityThread.java:4025)
                  at android.app.ActivityThread.handleResumeActivity(ActivityThread.java:4057)
                  at android.app.servertransaction.ResumeActivityItem.execute(ResumeActivityItem.java:51)
                  at android.app.servertransaction.TransactionExecutor.executeLifecycleState(TransactionExecutor.java:145)
                  at android.app.servertransaction.TransactionExecutor.execute(TransactionExecutor.java:70)
                  at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1959)
                  at android.os.Handler.dispatchMessage(Handler.java:106)
                  at android.os.Looper.loop(Looper.java:214)
                  at android.app.ActivityThread.main(ActivityThread.java:7100)
                  at java.lang.reflect.Method.invoke(Native Method)
                  at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:494)
                  at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:964)
                Caused by: java.lang.IllegalArgumentException
                  at android.os.Parcel.createException(Parcel.java:1970)
                  at android.os.Parcel.readException(Parcel.java:1934)
                  at android.os.Parcel.readException(Parcel.java:1884)
                  at android.app.IActivityManager$Stub$Proxy.isTopOfTask(IActivityManager.java:7835)
                  at android.app.Activity.isTopOfTask(Activity.java:6544)
                  at android.app.Activity.onResume(Activity.java:1404)
                  at androidx.fragment.app.FragmentActivity.onResume(SourceFile:458)
                  at eu.faircode.email.ActivityBase.onResume(SourceFile:263)
                  at eu.faircode.email.ActivityBilling.onResume(SourceFile:125)
                  at eu.faircode.email.ActivityView.onResume(SourceFile:588)
                  at android.app.Instrumentation.callActivityOnResume(Instrumentation.java:1416)
                  at android.app.Activity.performResume(Activity.java:7612)
                  at android.app.ActivityThread.performResumeActivity(ActivityThread.java:4017)
                  ... 11 more
                Caused by: android.os.RemoteException: Remote stack trace:
                  at com.android.server.am.ActivityManagerService.isTopOfTask(ActivityManagerService.java:18313)
                  at android.app.IActivityManager$Stub.onTransact(IActivityManager.java:2028)
                  at com.android.server.am.ActivityManagerService.onTransact(ActivityManagerService.java:4166)
                  at android.os.Binder.execTransact(Binder.java:739)
             */
        }
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
            Intent main = new Intent(this, ActivityMain.class)
                    .putExtra("intent", intent);
            main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(main);
        }
    }

    @Override
    public void startActivity(Intent intent) {
        try {
            super.startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Log.w(ex);
            ToastEx.makeText(this, getString(R.string.title_no_viewer, intent), Toast.LENGTH_LONG).show();
        } catch (Throwable ex) {
            Log.e(ex);
            ToastEx.makeText(this, Log.formatThrowable(ex), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        try {
            super.startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException ex) {
            Log.w(ex);
            ToastEx.makeText(this, getString(R.string.title_no_viewer, intent), Toast.LENGTH_LONG).show();
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
            if (this.getClass().equals(ActivitySetup.class))
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                    onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

    public interface IKeyPressedListener {
        boolean onKeyPressed(KeyEvent event);

        boolean onBackPressed();
    }
}
