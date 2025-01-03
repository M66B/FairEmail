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

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.service.quicksettings.TileService;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.N)
public class ServiceTileBase extends TileService {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(ApplicationEx.getLocalizedContext(base));
    }

    @Override
    public IBinder onBind(Intent intent) {
        try {
            return super.onBind(intent);
        } catch (Throwable ex) {
            /*
                Exception java.lang.RuntimeException:
                  at android.app.ActivityThread.handleBindService (ActivityThread.java:4202)
                  at android.app.ActivityThread.access$2500 (ActivityThread.java:273)
                  at android.app.ActivityThread$H.handleMessage (ActivityThread.java:2060)
                  at android.os.Handler.dispatchMessage (Handler.java:112)
                  at android.os.Looper.loop (Looper.java:216)
                  at android.app.ActivityThread.main (ActivityThread.java:7625)
                  at java.lang.reflect.Method.invoke (Method.java)
                  at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run (RuntimeInit.java:524)
                  at com.android.internal.os.ZygoteInit.main (ZygoteInit.java:987)
                Caused by java.lang.RuntimeException: Unable to reach IQSService
                  at android.service.quicksettings.TileService.onBind (TileService.java:333)
                  at eu.faircode.email.ServiceTileUnseen.onBind (ServiceTileUnseen.java:83)
                  at android.app.ActivityThread.handleBindService (ActivityThread.java:4184)
                Caused by android.os.DeadObjectException:
                  at android.os.BinderProxy.transactNative (BinderProxy.java)
                  at android.os.BinderProxy.transact (BinderProxy.java:1149)
                  at android.service.quicksettings.IQSService$Stub$Proxy.getTile (IQSService.java:189)
                  at android.service.quicksettings.TileService.onBind (TileService.java:331)
             */
            Log.w(ex);
            return null;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        CoalMine.watch(this, this.getClass().getName() + "#onDestroy");
    }
}
