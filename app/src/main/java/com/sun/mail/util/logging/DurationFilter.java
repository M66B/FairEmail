/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2015, 2018 Jason Mehrens. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */
package com.sun.mail.util.logging;

import static com.sun.mail.util.logging.LogManagerProperties.fromLogManager;
import java.util.logging.*;

/**
 * A filter used to limit log records based on a maximum generation rate.
 *
 * The duration specified is used to compute the record rate and the amount of
 * time the filter will reject records once the rate has been exceeded. Once the
 * rate is exceeded records are not allowed until the duration has elapsed.
 *
 * <p>
 * By default each {@code DurationFilter} is initialized using the following
 * LogManager configuration properties where {@code <filter-name>} refers to the
 * fully qualified class name of the handler. If properties are not defined, or
 * contain invalid values, then the specified default values are used.
 *
 * <ul>
 * <li>{@literal <filter-name>}.records the max number of records per duration.
 * A numeric long integer or a multiplication expression can be used as the
 * value. (defaults to {@code 1000})
 *
 * <li>{@literal <filter-name>}.duration the number of milliseconds to suppress
 * log records from being published. This is also used as duration to determine
 * the log record rate. A numeric long integer or a multiplication expression
 * can be used as the value. If the {@code java.time} package is available then
 * an ISO-8601 duration format of {@code PnDTnHnMn.nS} can be used as the value.
 * The suffixes of "D", "H", "M" and "S" are for days, hours, minutes and
 * seconds. The suffixes must occur in order. The seconds can be specified with
 * a fractional component to declare milliseconds. (defaults to {@code PT15M})
 * </ul>
 *
 * <p>
 * For example, the settings to limit {@code MailHandler} with a default
 * capacity to only send a maximum of two email messages every six minutes would
 * be as follows:
 * <pre>
 * {@code
 *  com.sun.mail.util.logging.MailHandler.filter = com.sun.mail.util.logging.DurationFilter
 *  com.sun.mail.util.logging.MailHandler.capacity = 1000
 *  com.sun.mail.util.logging.DurationFilter.records = 2L * 1000L
 *  com.sun.mail.util.logging.DurationFilter.duration = PT6M
 * }
 * </pre>
 *
 *
 * @author Jason Mehrens
 * @since JavaMail 1.5.5
 */
public class DurationFilter implements Filter {

    /**
     * The number of expected records per duration.
     */
    private final long records;
    /**
     * The duration in milliseconds used to determine the rate. The duration is
     * also used as the amount of time that the filter will not allow records
     * when saturated.
     */
    private final long duration;
    /**
     * The number of records seen for the current duration. This value negative
     * if saturated. Zero is considered saturated but is reserved for recording
     * the first duration.
     */
    private long count;
    /**
     * The most recent record time seen for the current duration.
     */
    private long peak;
    /**
     * The start time for the current duration.
     */
    private long start;

    /**
     * Creates the filter using the default properties.
     */
    public DurationFilter() {
        this.records = checkRecords(initLong(".records"));
        this.duration = checkDuration(initLong(".duration"));
    }

    /**
     * Creates the filter using the given properties. Default values are used if
     * any of the given values are outside the allowed range.
     *
     * @param records the number of records per duration.
     * @param duration the number of milliseconds to suppress log records from
     * being published.
     */
    public DurationFilter(final long records, final long duration) {
        this.records = checkRecords(records);
        this.duration = checkDuration(duration);
    }

    /**
     * Determines if this filter is equal to another filter.
     *
     * @param obj the given object.
     * @return true if equal otherwise false.
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) { //Avoid locks and deal with rapid state changes.
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        final DurationFilter other = (DurationFilter) obj;
        if (this.records != other.records) {
            return false;
        }

        if (this.duration != other.duration) {
            return false;
        }

        final long c;
        final long p;
        final long s;
        synchronized (this) {
            c = this.count;
            p = this.peak;
            s = this.start;
        }

        synchronized (other) {
            if (c != other.count || p != other.peak || s != other.start) {
                return false;
            }
        }
        return true;
    }

    /**
     * Determines if this filter is able to accept the maximum number of log
     * records for this instant in time. The result is a best-effort estimate
     * and should be considered out of date as soon as it is produced. This
     * method is designed for use in monitoring the state of this filter.
     *
     * @return true if the filter is idle; false otherwise.
     */
    public boolean isIdle() {
        return test(0L, System.currentTimeMillis());
    }

    /**
     * Returns a hash code value for this filter.
     *
     * @return hash code for this filter.
     */
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + (int) (this.records ^ (this.records >>> 32));
        hash = 89 * hash + (int) (this.duration ^ (this.duration >>> 32));
        return hash;
    }

    /**
     * Check if the given log record should be published. This method will
     * modify the internal state of this filter.
     *
     * @param record the log record to check.
     * @return true if allowed; false otherwise.
     * @throws NullPointerException if given record is null.
     */
    @SuppressWarnings("override") //JDK-6954234
    public boolean isLoggable(final LogRecord record) {
        return accept(record.getMillis());
    }

    /**
     * Determines if this filter will accept log records for this instant in
     * time. The result is a best-effort estimate and should be considered out
     * of date as soon as it is produced. This method is designed for use in
     * monitoring the state of this filter.
     *
     * @return true if the filter is not saturated; false otherwise.
     */
    public boolean isLoggable() {
        return test(records, System.currentTimeMillis());
    }

    /**
     * Returns a string representation of this filter. The result is a
     * best-effort estimate and should be considered out of date as soon as it
     * is produced.
     *
     * @return a string representation of this filter.
     */
    @Override
    public String toString() {
        boolean idle;
        boolean loggable;
        synchronized (this) {
            final long millis = System.currentTimeMillis();
            idle = test(0L, millis);
            loggable = test(records, millis);
        }

        return getClass().getName() + "{records=" + records
                + ", duration=" + duration
                + ", idle=" + idle
                + ", loggable=" + loggable + '}';
    }

    /**
     * Creates a copy of this filter that retains the filter settings but does
     * not include the current filter state. The newly create clone acts as if
     * it has never seen any records.
     *
     * @return a copy of this filter.
     * @throws CloneNotSupportedException if this filter is not allowed to be
     * cloned.
     */
    @Override
    protected DurationFilter clone() throws CloneNotSupportedException {
        final DurationFilter clone = (DurationFilter) super.clone();
        clone.count = 0L; //Reset the filter state.
        clone.peak = 0L;
        clone.start = 0L;
        return clone;
    }

    /**
     * Checks if this filter is not saturated or bellow a maximum rate.
     *
     * @param limit the number of records allowed to be under the rate.
     * @param millis the current time in milliseconds.
     * @return true if not saturated or bellow the rate.
     */
    private boolean test(final long limit, final long millis) {
        assert limit >= 0L : limit;
        final long c;
        final long s;
        synchronized (this) {
            c = count;
            s = start;
        }

        if (c > 0L) { //If not saturated.
            if ((millis - s) >= duration || c < limit) {
                return true;
            }
        } else {  //Subtraction is used to deal with numeric overflow.
            if ((millis - s) >= 0L || c == 0L) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if the record is loggable by time.
     *
     * @param millis the log record milliseconds.
     * @return true if accepted false otherwise.
     */
    private synchronized boolean accept(final long millis) {
        //Subtraction is used to deal with numeric overflow of millis.
        boolean allow;
        if (count > 0L) { //If not saturated.
            if ((millis - peak) > 0L) {
                peak = millis; //Record the new peak.
            }

            //Under the rate if the count has not been reached.
            if (count != records) {
                ++count;
                allow = true;
            } else {
                if ((peak - start) >= duration) {
                    count = 1L;  //Start a new duration.
                    start = peak;
                    allow = true;
                } else {
                    count = -1L; //Saturate for the duration.
                    start = peak + duration;
                    allow = false;
                }
            }
        } else {
            //If the saturation period has expired or this is the first record
            //then start a new duration and allow records.
            if ((millis - start) >= 0L || count == 0L) {
                count = 1L;
                start = millis;
                peak = millis;
                allow = true;
            } else {
                allow = false; //Remain in a saturated state.
            }
        }
        return allow;
    }

    /**
     * Reads a long value or multiplication expression from the LogManager. If
     * the value can not be parsed or is not defined then Long.MIN_VALUE is
     * returned.
     *
     * @param suffix a dot character followed by the key name.
     * @return a long value or Long.MIN_VALUE if unable to parse or undefined.
     * @throws NullPointerException if suffix is null.
     */
    private long initLong(final String suffix) {
        long result = 0L;
        final String p = getClass().getName();
        String value = fromLogManager(p.concat(suffix));
        if (value != null && value.length() != 0) {
            value = value.trim();
            if (isTimeEntry(suffix, value)) {
                try {
                    result = LogManagerProperties.parseDurationToMillis(value);
                } catch (final RuntimeException ignore) {
                } catch (final Exception ignore) {
                } catch (final LinkageError ignore) {
                }
            }

            if (result == 0L) { //Zero is invalid.
                try {
                    result = 1L;
                    for (String s : tokenizeLongs(value)) {
                        if (s.endsWith("L") || s.endsWith("l")) {
                            s = s.substring(0, s.length() - 1);
                        }
                        result = multiplyExact(result, Long.parseLong(s));
                    }
                } catch (final RuntimeException ignore) {
                    result = Long.MIN_VALUE;
                }
            }
        } else {
            result = Long.MIN_VALUE;
        }
        return result;
    }

    /**
     * Determines if the given suffix can be a time unit and the value is
     * encoded as an ISO ISO-8601 duration format.
     *
     * @param suffix the suffix property.
     * @param value the value of the property.
     * @return true if the entry is a time entry.
     * @throws IndexOutOfBoundsException if value is empty.
     * @throws NullPointerException if either argument is null.
     */
    private boolean isTimeEntry(final String suffix, final String value) {
        return (value.charAt(0) == 'P' || value.charAt(0) == 'p')
                && suffix.equals(".duration");
    }

    /**
     * Parse any long value or multiplication expressions into tokens.
     *
     * @param value the expression or value.
     * @return an array of long tokens, never empty.
     * @throws NullPointerException if the given value is null.
     * @throws NumberFormatException if the expression is invalid.
     */
    private static String[] tokenizeLongs(final String value) {
        String[] e;
        final int i = value.indexOf('*');
        if (i > -1 && (e = value.split("\\s*\\*\\s*")).length != 0) {
            if (i == 0 || value.charAt(value.length() - 1) == '*') {
                throw new NumberFormatException(value);
            }

            if (e.length == 1) {
                throw new NumberFormatException(e[0]);
            }
        } else {
            e = new String[]{value};
        }
        return e;
    }

    /**
     * Multiply and check for overflow. This can be replaced with
     * {@code java.lang.Math.multiplyExact} when Jakarta Mail requires JDK 8.
     *
     * @param x the first value.
     * @param y the second value.
     * @return x times y.
     * @throws ArithmeticException if overflow is detected.
     */
    private static long multiplyExact(final long x, final long y) {
        long r = x * y;
        if (((Math.abs(x) | Math.abs(y)) >>> 31L != 0L)) {
            if (((y != 0L) && (r / y != x))
                    || (x == Long.MIN_VALUE && y == -1L)) {
                throw new ArithmeticException();
            }
        }
        return r;
    }

    /**
     * Converts record count to a valid record count. If the value is out of
     * bounds then the default record count is used.
     *
     * @param records the record count.
     * @return a valid number of record count.
     */
    private static long checkRecords(final long records) {
        return records > 0L ? records : 1000L;
    }

    /**
     * Converts the duration to a valid duration. If the value is out of bounds
     * then the default duration is used.
     *
     * @param duration the duration to check.
     * @return a valid duration.
     */
    private static long checkDuration(final long duration) {
        return duration > 0L ? duration : 15L * 60L * 1000L;
    }
}
