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

    Copyright 2018-2023 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import org.tinylog.Level;
import org.tinylog.configuration.PropertiesConfigurationLoader;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Properties;

// https://tinylog.org/v2/configuration/
// https://github.com/tinylog-org/tinylog-android-example/blob/v2/app/src/main/resources/tinylog.properties

public class TinyLogConfigurationLoader extends PropertiesConfigurationLoader {
    private static Level level = Level.TRACE;

    @Override
    public Properties load() {
        Properties props = super.load();
        props.setProperty("level", level.name());
        return props;
    }

    static void setup(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean debug = prefs.getBoolean("debug", false); // Changing requires force stop

        if (debug)
            level = Level.DEBUG;
        else {
            int def = (BuildConfig.DEBUG || BuildConfig.TEST_RELEASE ? android.util.Log.INFO : android.util.Log.WARN);
            int _level = prefs.getInt("log_level", def);
            if (_level == android.util.Log.VERBOSE)
                level = Level.TRACE;
            else if (_level == android.util.Log.DEBUG)
                level = Level.DEBUG;
            else if (_level == android.util.Log.INFO)
                level = Level.INFO;
            else if (_level == android.util.Log.WARN)
                level = Level.WARN;
            else if (_level == android.util.Log.ERROR)
                level = Level.ERROR;
        }

        System.setProperty("tinylog.configurationloader",
                TinyLogConfigurationLoader.class.getName());

        System.setProperty("tinylog.directory",
                new File(context.getFilesDir(), "logs").getAbsolutePath());
    }

    static File[] getFiles(Context context) {
        File[] files = new File(context.getFilesDir(), "logs").listFiles();

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
