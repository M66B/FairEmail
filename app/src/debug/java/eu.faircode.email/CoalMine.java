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

import leakcanary.AppWatcher;
import leakcanary.LeakCanary;

public class CoalMine {
    static void setup(boolean enabled) {
        LeakCanary.Config config = LeakCanary.getConfig().newBuilder()
                .dumpHeap(enabled && BuildConfig.DEBUG)
                .build();
        LeakCanary.setConfig(config);
        LeakCanary.INSTANCE.showLeakDisplayActivityLauncherIcon(BuildConfig.DEBUG);
    }

    static void check() {
        LeakCanary.INSTANCE.dumpHeap();
    }

    static void watch(Object object, String reason) {
        //AppWatcher.INSTANCE.getObjectWatcher().expectWeaklyReachable(object, reason);
    }
}