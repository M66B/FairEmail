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

import android.app.Application;
import android.content.Intent;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;
import leakcanary.AppWatcher;
import leakcanary.LeakCanary;
import shark.HeapField;
import shark.HeapObject;
import shark.ObjectInspector;
import shark.ObjectReporter;

public class CoalMine {
    static void install(@NonNull Application app) {
        AppWatcher.INSTANCE.manualInstall(app, TimeUnit.SECONDS.toMillis(5));
    }

    static void setup(boolean enabled) {
        List<ObjectInspector> inspectors = new ArrayList<>(LeakCanary.getConfig().getObjectInspectors());

        // https://square.github.io/leakcanary/recipes/#identifying-leaking-objects-and-labeling-objects
        for (Class<?> clazz : new Class<?>[]{SimpleTask.class, TwoStateOwner.class, RunnableEx.class})
            inspectors.add(new ObjectInspector() {
                @Override
                public void inspect(@NonNull ObjectReporter reporter) {
                    String className = clazz.getName();
                    reporter.whenInstanceOf(className, new Function2<ObjectReporter, HeapObject.HeapInstance, Unit>() {
                        @Override
                        public Unit invoke(ObjectReporter reporter, HeapObject.HeapInstance instance) {
                            HeapField hfName = instance.get(className, "name");
                            if (hfName != null) {
                                String label = hfName.getValue().readAsJavaString();
                                reporter.getLabels().add("name=" + label);
                            }

                            // Could be different class loader
                            if (className.equals(SimpleTask.class.getName())) {
                                HeapField hfStarted = instance.get(className, "started");
                                if (hfStarted != null) {
                                    Long started = hfStarted.getValue().getAsLong();
                                    if (started != null) {
                                        String label = (started == 0 ? null : new Date(started).toString());
                                        reporter.getLabels().add("started=" + label);
                                    }
                                }
                                HeapField hfDestroyed = instance.get(className, "destroyed");
                                if (hfDestroyed != null) {
                                    Boolean destroyed = hfDestroyed.getValue().getAsBoolean();
                                    if (destroyed != null)
                                        reporter.getLabels().add("destroyed=" + destroyed);
                                }
                            } else if (className.equals(TwoStateOwner.class.getName())) {
                                HeapField hfState = instance.get(className, "state");
                                if (hfState != null) {
                                    String state = hfState.getValue().readAsJavaString();
                                    reporter.getLabels().add("state=" + state);
                                }
                                HeapField hfOwned = instance.get(className, "owned");
                                if (hfOwned != null) {
                                    Boolean owned = hfOwned.getValue().getAsBoolean();
                                    reporter.getLabels().add("owned=" + owned);
                                }
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