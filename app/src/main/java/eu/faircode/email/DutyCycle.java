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

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

public class DutyCycle {
    private final String name;
    private final long interval;
    private final long duration;

    private Long last = null;
    private long start;
    private long idle = 0;
    private long busy = 0;

    private static final long YIELD_INTERVAL = 10 * 1000L; // milliseconds
    private static final long YIELD_DURATION = 2000L; // milliseconds

    public DutyCycle(String name) {
        this(name, YIELD_INTERVAL, YIELD_DURATION);
    }

    public DutyCycle(String name, long interval, long duration) {
        this.name = name;
        this.interval = interval;
        this.duration = duration;
    }

    public void start() {
        start = new Date().getTime();
    }

    public void stop(boolean foreground, ExecutorService executor) {
        boolean done = false;
        try {
            done = (executor instanceof ThreadPoolExecutor &&
                    ((ThreadPoolExecutor) executor).getQueue().size() == 0);
        } catch (Throwable ex) {
            Log.e(ex);
        } finally {
            stop(foreground, done);
        }
    }

    public void stop(boolean foreground, boolean done) {
        long end = new Date().getTime();

        if (last != null)
            idle += (start - last);
        last = end;

        busy += (end - start);

        if (busy + idle > interval) {
            long wait = (duration - idle);
            Log.i(name + " busy=" + busy + " idle=" + idle +
                    " wait=" + wait + " foreground=" + foreground + " done=" + done);
            if (wait > 0 && foreground && !done) {
                try {
                    Thread.sleep(wait);
                } catch (InterruptedException ex) {
                    Log.w(ex);
                }
                last += wait;
            }
            idle = 0;
            busy = 0;
        }
    }
}
