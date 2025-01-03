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
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import org.tinylog.Level;
import org.tinylog.core.TinylogLoggingProvider;
import org.tinylog.format.MessageFormatter;
import org.tinylog.provider.ProviderRegistry;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

// https://tinylog.org/v2/configuration/
// https://github.com/tinylog-org/tinylog-android-example/blob/v2/app/src/main/resources/tinylog.properties

public class FairEmailLoggingProvider extends TinylogLoggingProvider {
    private Level activeLevel = Level.WARN;

    public void setLevel(Level level) {
        activeLevel = level;
    }

    @Override
    public boolean isEnabled(int depth, String tag, Level level) {
        return (activeLevel.ordinal() <= level.ordinal() &&
                super.isEnabled(depth + 1, tag, level));
    }

    @Override
    public void log(int depth, String tag, Level level, Throwable exception, MessageFormatter formatter, Object obj, Object... arguments) {
        if (activeLevel.ordinal() <= level.ordinal())
            super.log(depth, tag, level, exception, formatter, obj, arguments);
    }

    @Override
    public void log(String loggerClassName, String tag, Level level, Throwable exception, MessageFormatter formatter, Object obj, Object... arguments) {
        if (activeLevel.ordinal() <= level.ordinal())
            super.log(loggerClassName, tag, level, exception, formatter, obj, arguments);
    }

    static void setup(Context context) {
        try {
            System.setProperty("tinylog.directory",
                    Helper.ensureExists(context, "logs").getAbsolutePath());

            setLevel(context);
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    static void setLevel(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean debug = prefs.getBoolean("debug", false);

        FairEmailLoggingProvider provider = (FairEmailLoggingProvider) ProviderRegistry.getLoggingProvider();
        if (provider == null)
            return;

        if (debug)
            provider.activeLevel = Level.DEBUG;
        else {
            int _level = prefs.getInt("log_level", Log.getDefaultLogLevel());
            if (_level == android.util.Log.VERBOSE)
                provider.activeLevel = Level.TRACE;
            else if (_level == android.util.Log.DEBUG)
                provider.activeLevel = Level.DEBUG;
            else if (_level == android.util.Log.INFO)
                provider.activeLevel = Level.INFO;
            else if (_level == android.util.Log.WARN)
                provider.activeLevel = Level.WARN;
            else if (_level == android.util.Log.ERROR)
                provider.activeLevel = Level.ERROR;
        }
    }

    static File[] getLogFiles(Context context) {
        File[] files = Helper.ensureExists(context, "logs").listFiles();

        if (files == null)
            return new File[0];

        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                return f1.getName().compareTo(f2.getName());
            }
        });

        return files;
    }
}
