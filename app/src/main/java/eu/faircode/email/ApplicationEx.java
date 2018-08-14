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

import android.app.Application;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ApplicationEx extends Application {
    private Thread.UncaughtExceptionHandler prev = null;

    @Override
    public void onCreate() {
        super.onCreate();

        prev = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                Log.e(Helper.TAG, ex + "\r\n" + Log.getStackTraceString(ex));

                File file = new File(getCacheDir(), "crash.log");
                Log.w(Helper.TAG, "Writing exception to " + file);

                FileWriter out = null;
                try {
                    out = new FileWriter(file);
                    out.write(ex.toString() + "\n" + Log.getStackTraceString(ex));
                } catch (IOException e) {
                    Log.e(Helper.TAG, e + "\n" + Log.getStackTraceString(ex));
                } finally {
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e) {
                            Log.e(Helper.TAG, e + "\n" + Log.getStackTraceString(ex));
                        }
                    }
                }

                if (prev != null)
                    prev.uncaughtException(thread, ex);
            }
        });
    }
}
