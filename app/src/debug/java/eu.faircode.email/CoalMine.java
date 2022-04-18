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

import android.content.Intent;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;
import leakcanary.AppWatcher;
import leakcanary.LeakCanary;
import shark.HeapField;
import shark.HeapObject;
import shark.ObjectInspector;
import shark.ObjectReporter;

public class CoalMine {
    static void setup(boolean enabled) {
        List<ObjectInspector> inspectors = new ArrayList<>(LeakCanary.getConfig().getObjectInspectors());

        inspectors.add(new ObjectInspector() {
            @Override
            public void inspect(@NonNull ObjectReporter reporter) {
                String clazz = SimpleTask.class.getName();
                reporter.whenInstanceOf(clazz, new Function2<ObjectReporter, HeapObject.HeapInstance, Unit>() {
                    @Override
                    public Unit invoke(ObjectReporter reporter, HeapObject.HeapInstance instance) {
                        HeapField fname = instance.get(clazz, "name");
                        if (fname != null) {
                            String name = fname.getValue().readAsJavaString();
                            reporter.getNotLeakingReasons().add("name=" + name);
                        }
                        HeapField fstarted = instance.get(clazz, "started");
                        if (fstarted != null) {
                            Long started = fstarted.getValue().getAsLong();
                            if (started != null)
                                reporter.getNotLeakingReasons().add("started=" + new Date(started));
                        }
                        return null;
                    }
                });
            }
        });

        inspectors.add(new ObjectInspector() {
            @Override
            public void inspect(@NonNull ObjectReporter reporter) {
                String clazz = TwoStateOwner.class.getName();
                reporter.whenInstanceOf(clazz, new Function2<ObjectReporter, HeapObject.HeapInstance, Unit>() {
                    @Override
                    public Unit invoke(ObjectReporter reporter, HeapObject.HeapInstance instance) {
                        HeapField fname = instance.get(clazz, "name");
                        if (fname != null) {
                            String name = fname.getValue().readAsJavaString();
                            reporter.getNotLeakingReasons().add("name=" + name);
                        }
                        return null;
                    }
                });
            }
        });

        inspectors.add(new ObjectInspector() {
            @Override
            public void inspect(@NonNull ObjectReporter reporter) {
                String clazz = RunnableEx.class.getName();
                reporter.whenInstanceOf(clazz, new Function2<ObjectReporter, HeapObject.HeapInstance, Unit>() {
                    @Override
                    public Unit invoke(ObjectReporter reporter, HeapObject.HeapInstance instance) {
                        HeapField fname = instance.get(clazz, "name");
                        if (fname != null) {
                            String name = fname.getValue().readAsJavaString();
                            reporter.getNotLeakingReasons().add("name=" + name);
                        }
                        return null;
                    }
                });
            }
        });

        LeakCanary.Config config = LeakCanary.getConfig().newBuilder()
                .dumpHeap(enabled && BuildConfig.DEBUG)
                .objectInspectors(inspectors)
                .build();
        LeakCanary.setConfig(config);
        LeakCanary.INSTANCE.showLeakDisplayActivityLauncherIcon(true);
    }

    static void check() {
        LeakCanary.INSTANCE.dumpHeap();
    }

    static void watch(@NonNull Object object, String reason) {
        Log.i("Watching " + object.getClass() + " because " + reason);
        AppWatcher.INSTANCE.getObjectWatcher().expectWeaklyReachable(object, reason);
    }

    static Intent getIntent() {
        return LeakCanary.INSTANCE.newLeakDisplayActivityIntent();
    }
}