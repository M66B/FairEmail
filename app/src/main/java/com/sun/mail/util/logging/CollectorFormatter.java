/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2013, 2019 Jason Mehrens. All rights reserved.
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
import java.lang.reflect.UndeclaredThrowableException;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * A LogRecord formatter that takes a sequence of LogRecords and combines them
 * into a single summary result. Formating of the head, LogRecord, and tail are
 * delegated to the wrapped formatter.
 *
 * <p>
 * By default each <code>CollectorFormatter</code> is initialized using the
 * following LogManager configuration properties where
 * <code>&lt;formatter-name&gt;</code> refers to the fully qualified class name
 * or the fully qualified derived class name of the formatter.  If properties
 * are not defined, or contain invalid values, then the specified default values
 * are used.
 * <ul>
 * <li>&lt;formatter-name&gt;.comparator name of a
 * {@linkplain java.util.Comparator} class used to choose the collected
 * <code>LogRecord</code>. If a comparator is specified then the max
 * <code>LogRecord</code> is chosen. If comparator is set to the string literal
 * null, then the last record is chosen. (defaults to
 * {@linkplain SeverityComparator})
 *
 * <li>&lt;formatter-name&gt;.comparator.reverse a boolean
 * <code>true</code> to collect the min <code>LogRecord</code> or
 * <code>false</code> to collect the max <code>LogRecord</code>.
 * (defaults to <code>false</code>)
 *
 * <li>&lt;formatter-name&gt;.format the
 * {@linkplain java.text.MessageFormat MessageFormat} string used to format the
 * collected summary statistics. The arguments are explained in detail in the
 * {@linkplain #getTail(java.util.logging.Handler) getTail} documentation.
 * (defaults to <code>{0}{1}{2}{4,choice,-1#|0#|0&lt;... {4,number,integer}
 * more}\n</code>)
 *
 * <li>&lt;formatter-name&gt;.formatter name of a <code>Formatter</code> class
 * used to format the collected LogRecord.
 * (defaults to {@linkplain CompactFormatter})
 *
 * </ul>
 *
 * @author Jason Mehrens
 * @since JavaMail 1.5.2
 */
public class CollectorFormatter extends Formatter {
    /**
     * Avoid depending on JMX runtime bean to get the start time.
     */
    private static final long INIT_TIME = System.currentTimeMillis();
    /**
     * The message format string used as the formatted output.
     */
    private final String fmt;
    /**
     * The formatter used to format the chosen log record.
     */
    private final Formatter formatter;
    /**
     * The comparator used to pick the log record to format.
     */
    private final Comparator<? super LogRecord> comparator;
    /**
     * The last accepted record. Synchronized access is preferred over volatile
     * for this class.
     */
    private LogRecord last;
    /**
     * The number of log records that have been formatted.
     */
    private long count;
    /**
     * The number of log produced containing at least one log record.
     * Only incremented when this formatter is reset.
     */
    private long generation = 1L;
    /**
     * The number of log records that have been formatted with a thrown object.
     */
    private long thrown;
    /**
     * The eldest log record time or eldest time possible for this instance.
     */
    private long minMillis = INIT_TIME;
    /**
     * The newest log record time.
     */
    private long maxMillis = Long.MIN_VALUE;

    /**
     * Creates the formatter using the LogManager defaults.
     *
     * @throws SecurityException if a security manager exists and the caller
     * does not have <code>LoggingPermission("control")</code>.
     * @throws UndeclaredThrowableException if there are problems loading from
     * the LogManager.
     */
    public CollectorFormatter() {
        final String p = getClass().getName();
        this.fmt = initFormat(p);
        this.formatter = initFormatter(p);
        this.comparator = initComparator(p);
    }

    /**
     * Creates the formatter using the given format.
     *
     * @param format the message format or null to use the LogManager default.
     * @throws SecurityException if a security manager exists and the caller
     * does not have <code>LoggingPermission("control")</code>.
     * @throws UndeclaredThrowableException if there are problems loading from
     * the LogManager.
     */
    public CollectorFormatter(String format) {
        final String p = getClass().getName();
        this.fmt = format == null ? initFormat(p) : format;
        this.formatter = initFormatter(p);
        this.comparator = initComparator(p);
    }

    /**
     * Creates the formatter using the given values.
     *
     * @param format the format string or null to use the LogManager default.
     * @param f the formatter used on the collected log record or null to
     * specify no formatter.
     * @param c the comparator used to determine which log record to format or
     * null to specify no comparator.
     * @throws SecurityException if a security manager exists and the caller
     * does not have <code>LoggingPermission("control")</code>.
     * @throws UndeclaredThrowableException if there are problems loading from
     * the LogManager.
     */
    public CollectorFormatter(String format, Formatter f,
            Comparator<? super LogRecord> c) {
        final String p = getClass().getName();
        this.fmt = format == null ? initFormat(p) : format;
        this.formatter = f;
        this.comparator = c;
    }

    /**
     * Accumulates log records which will be used to produce the final output.
     * The output is generated using the {@link #getTail} method which also
     * resets this formatter back to its original state.
     *
     * @param record the record to store.
     * @return an empty string.
     * @throws NullPointerException if the given record is null.
     */
    @Override
    public String format(final LogRecord record) {
        if (record == null) {
            throw new NullPointerException();
        }

        boolean accepted;
        do {
            final LogRecord peek = peek();
            //The self compare of the first record acts like a type check.
            LogRecord update = apply(peek != null ? peek : record, record);
            if (peek != update) { //Not identical.
                update.getSourceMethodName(); //Infer caller, null check.
                accepted = acceptAndUpdate(peek, update);
            } else {
                accepted = accept(peek, record);
            }
        } while (!accepted);
        return "";
    }

    /**
     * Formats the collected LogRecord and summary statistics. The collected
     * results are reset after calling this method. The
     * {@linkplain java.text.MessageFormat java.text} argument indexes are
     * assigned to the following properties:
     *
     * <ol start='0'>
     * <li>{@code head} the
     * {@linkplain Formatter#getHead(java.util.logging.Handler) head} string
     * returned from the target formatter and
     * {@linkplain #finish(java.lang.String) finished} by this formatter.
     * <li>{@code formatted} the current log record
     * {@linkplain Formatter#format(java.util.logging.LogRecord) formatted} by
     * the target formatter and {@linkplain #finish(java.lang.String) finished}
     * by this formatter.  If the formatter is null then record is formatted by
     * this {@linkplain #formatMessage(java.util.logging.LogRecord) formatter}.
     * <li>{@code tail} the
     * {@linkplain Formatter#getTail(java.util.logging.Handler) tail} string
     * returned from the target formatter and
     * {@linkplain #finish(java.lang.String) finished} by this formatter.
     * <li>{@code count} the total number of log records
     * {@linkplain #format consumed} by this formatter.
     * <li>{@code remaining} the count minus one.
     * <li>{@code thrown} the total number of log records
     * {@linkplain #format consumed} by this formatter with an assigned
     * {@linkplain java.util.logging.LogRecord#getThrown throwable}.
     * <li>{@code normal messages} the count minus the thrown.
     * <li>{@code minMillis} the eldest log record
     * {@linkplain java.util.logging.LogRecord#getMillis event time}
     * {@linkplain #format consumed} by this formatter. If the count is zero
     * then this is set to the previous max or approximate start time if there
     * was no previous max. By default this parameter is defined as a number.
     * The format type and format style rules from the
     * {@linkplain java.text.MessageFormat} should be used to convert this from
     * milliseconds to a date or time.
     * <li>{@code maxMillis} the most recent log record
     * {@linkplain java.util.logging.LogRecord#getMillis event time}
     * {@linkplain #format consumed} by this formatter. If the count is zero
     * then this is set to the {@linkplain System#currentTimeMillis() current time}.
     * By default this parameter is defined as a number. The format type and
     * format style rules from the {@linkplain java.text.MessageFormat} should
     * be used to convert this from milliseconds to a date or time.
     * <li>{@code elapsed} the elapsed time in milliseconds between the
     * {@code maxMillis} and {@code minMillis}.
     * <li>{@code startTime} the approximate start time in milliseconds.  By
     * default this parameter is defined as a number. The format type and format
     * style rules from the {@linkplain java.text.MessageFormat} should be used
     * to convert this from milliseconds to a date or time.
     * <li>{@code currentTime} the
     * {@linkplain System#currentTimeMillis() current time} in milliseconds.  By
     * default this parameter is defined as a number. The format type and format
     * style rules from the {@linkplain java.text.MessageFormat} should be used
     * to convert this from milliseconds to a date or time.
     * <li>{@code uptime} the elapsed time in milliseconds between the
     * {@code currentTime} and {@code startTime}.
     * <li>{@code generation} the number times this method produced output with
     * at least one {@linkplain #format consumed} log record.  This can be used
     * to track the number of complete reports this formatter has produced.
     * </ol>
     *
     * <p>
     * Some example formats:<br>
     * <ul>
     * <li>{@code com.sun.mail.util.logging.CollectorFormatter.format={0}{1}{2}{4,choice,-1#|0#|0<... {4,number,integer} more}\n}
     * <p>
     * This prints the head ({@code {0}}), format ({@code {1}}), and tail
     * ({@code {2}}) from the target formatter followed by the number of
     * remaining ({@code {4}}) log records consumed by this formatter if there
     * are any remaining records.
     * <pre>
     * Encoding failed.|NullPointerException: null String.getBytes(:913)... 3 more
     * </pre>
     * <li>{@code com.sun.mail.util.logging.CollectorFormatter.format=These {3} messages occurred between\n{7,date,EEE, MMM dd HH:mm:ss:S ZZZ yyyy} and {8,time,EEE, MMM dd HH:mm:ss:S ZZZ yyyy}\n}
     * <p>
     * This prints the count ({@code {3}}) followed by the date and time of the
     * eldest log record ({@code {7}}) and the date and time of the most recent
     * log record ({@code {8}}).
     * <pre>
     * These 292 messages occurred between
     * Tue, Jul 21 14:11:42:449 -0500 2009 and Fri, Nov 20 07:29:24:0 -0600 2009
     * </pre>
     * <li>{@code com.sun.mail.util.logging.CollectorFormatter.format=These {3} messages occurred between {9,choice,86400000#{7,date} {7,time} and {8,time}|86400000<{7,date} and {8,date}}\n}
     * <p>
     * This prints the count ({@code {3}}) and then chooses the format based on
     * the elapsed time ({@code {9}}). If the elapsed time is less than one day
     * then the eldest log record ({@code {7}}) date and time is formatted
     * followed by just the time of the most recent log record ({@code {8}}.
     * Otherwise, the just the date of the eldest log record ({@code {7}}) and
     * just the date of most recent log record ({@code {8}} is formatted.
     * <pre>
     * These 73 messages occurred between Jul 21, 2009 2:11:42 PM and 2:13:32 PM
     *
     * These 116 messages occurred between Jul 21, 2009 and Aug 20, 2009
     * </pre>
     * <li>{@code com.sun.mail.util.logging.CollectorFormatter.format={13} alert reports since {10,date}.\n}
     * <p>
     * This prints the generation ({@code {13}}) followed by the start time
     * ({@code {10}}) formatted as a date.
     * <pre>
     * 4,320 alert reports since Jul 21, 2012.
     * </pre>
     * </ul>
     *
     * @param h the handler or null.
     * @return the output string.
     */
    @Override
    public String getTail(final Handler h) {
        super.getTail(h);  //Be forward compatible with super.getHead.
        return formatRecord(h, true);
    }

    /**
     * Formats the collected LogRecord and summary statistics. The LogRecord and
     * summary statistics are not changed by calling this method.
     *
     * @return the current record formatted or the default toString.
     * @see #getTail(java.util.logging.Handler)
     */
    @Override
    public String toString() {
        String result;
        try {
            result = formatRecord((Handler) null, false);
        } catch (final RuntimeException ignore) {
            result = super.toString();
        }
        return result;
    }

    /**
     * Used to choose the collected LogRecord. This implementation returns the
     * greater of two LogRecords.
     *
     * @param t the current record.
     * @param u the record that could replace the current.
     * @return the greater of the given log records.
     * @throws NullPointerException may occur if either record is null.
     */
    protected LogRecord apply(final LogRecord t, final LogRecord u) {
        if (t == null || u == null) {
            throw new NullPointerException();
        }

        if (comparator != null) {
            return comparator.compare(t, u) >= 0 ? t : u;
        } else {
            return u;
        }
    }

    /**
     * Updates the summary statistics only if the expected record matches the
     * last record.  The update record is not stored.
     *
     * @param e the LogRecord that is expected.
     * @param u the LogRecord used to collect statistics.
     * @return true if the last record was the expected record.
     * @throws NullPointerException if the update record is null.
     */
    private synchronized boolean accept(final LogRecord e, final LogRecord u) {
        /**
         * LogRecord methods must be called before the check of the last stored
         * record to guard against subclasses of LogRecord that might attempt to
         * reset the state by triggering a call to getTail.
         */
        final long millis = u.getMillis(); //Null check.
        final Throwable ex = u.getThrown();
        if (last == e) {  //Only if the exact same reference.
            if (++count != 1L) {
                minMillis = Math.min(minMillis, millis);
            } else { //Show single records as instant and not a time period.
                minMillis = millis;
            }
            maxMillis = Math.max(maxMillis, millis);

            if (ex != null) {
                ++thrown;
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Resets all of the collected summary statistics including the LogRecord.
     * @param min the current min milliseconds.
     */
    private synchronized void reset(final long min) {
        if (last != null) {
            last = null;
            ++generation;
        }

        count = 0L;
        thrown = 0L;
        minMillis = min;
        maxMillis = Long.MIN_VALUE;
    }

    /**
     * Formats the given record with the head and tail.
     *
     * @param h the Handler or null.
     * @param reset true if the summary statistics and LogRecord should be reset
     * back to initial values.
     * @return the formatted string.
     * @see #getTail(java.util.logging.Handler)
     */
    private String formatRecord(final Handler h, final boolean reset) {
        final LogRecord record;
        final long c;
        final long t;
        final long g;
        long msl;
        long msh;
        long now;
        synchronized (this) {
            record = last;
            c = count;
            g = generation;
            t = thrown;
            msl = minMillis;
            msh = maxMillis;
            now = System.currentTimeMillis();
            if (c == 0L) {
                msh = now;
            }

            if (reset) { //BUG ID 6351685
                reset(msh);
            }
        }

        final String head;
        final String msg;
        final String tail;
        final Formatter f = this.formatter;
        if (f != null) {
            synchronized (f) {
                head = f.getHead(h);
                msg = record != null ? f.format(record) : "";
                tail = f.getTail(h);
            }
        } else {
            head = "";
            msg = record != null ? formatMessage(record) : "";
            tail = "";
        }

        Locale l = null;
        if (record != null) {
            ResourceBundle rb = record.getResourceBundle();
            l = rb == null ? null : rb.getLocale();
        }

        final MessageFormat mf;
        if (l == null) { //BUG ID 8039165
            mf = new MessageFormat(fmt);
        } else {
            mf = new MessageFormat(fmt, l);
        }

        /**
         * These arguments are described in the getTail documentation.
         */
        return mf.format(new Object[]{finish(head), finish(msg), finish(tail),
            c, (c - 1L), t, (c - t), msl, msh, (msh - msl), INIT_TIME, now,
            (now - INIT_TIME), g});
    }

    /**
     * Applied to the head, format, and tail returned by the target formatter.
     * This implementation trims all input strings.
     *
     * @param s the string to transform.
     * @return the transformed string.
     * @throws NullPointerException if the given string is null.
     */
    protected String finish(String s) {
        return s.trim();
    }

    /**
     * Peek at the current log record.
     *
     * @return null or the current log record.
     */
    private synchronized LogRecord peek() {
        return this.last;
    }

    /**
     * Updates the summary statistics and stores given LogRecord if the expected
     * record matches the current record.
     *
     * @param e the expected record.
     * @param u the update record.
     * @return true if the update was performed.
     * @throws NullPointerException if the update record is null.
     */
    private synchronized boolean acceptAndUpdate(LogRecord e, LogRecord u) {
        if (accept(e, u)) {
            this.last = u;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Gets the message format string from the LogManager or creates the default
     * message format string.
     *
     * @param p the class name prefix.
     * @return the format string.
     * @throws NullPointerException if the given argument is null.
     */
    private String initFormat(final String p) {
        String v = fromLogManager(p.concat(".format"));
        if (v == null || v.length() == 0) {
            v = "{0}{1}{2}{4,choice,-1#|0#|0<... {4,number,integer} more}\n";
        }
        return v;
    }

    /**
     * Gets and creates the formatter from the LogManager or creates the default
     * formatter.
     *
     * @param p the class name prefix.
     * @return the formatter.
     * @throws NullPointerException if the given argument is null.
     * @throws UndeclaredThrowableException if the formatter can not be created.
     */
    private Formatter initFormatter(final String p) {
        Formatter f;
        String v = fromLogManager(p.concat(".formatter"));
        if (v != null && v.length() != 0) {
            if (!"null".equalsIgnoreCase(v)) {
                try {
                    f = LogManagerProperties.newFormatter(v);
                } catch (final RuntimeException re) {
                    throw re;
                } catch (final Exception e) {
                    throw new UndeclaredThrowableException(e);
                }
            } else {
                f = null;
            }
        } else {
            //Don't force the byte code verifier to load the formatter.
            f = Formatter.class.cast(new CompactFormatter());
        }
        return f;
    }

    /**
     * Gets and creates the comparator from the LogManager or returns the
     * default comparator.
     *
     * @param p the class name prefix.
     * @return the comparator or null.
     * @throws IllegalArgumentException if it was specified that the comparator
     * should be reversed but no initial comparator was specified.
     * @throws NullPointerException if the given argument is null.
     * @throws UndeclaredThrowableException if the comparator can not be
     * created.
     */
    @SuppressWarnings("unchecked")
    private Comparator<? super LogRecord> initComparator(final String p) {
        Comparator<? super LogRecord> c;
        final String name = fromLogManager(p.concat(".comparator"));
        final String reverse = fromLogManager(p.concat(".comparator.reverse"));
        try {
            if (name != null && name.length() != 0) {
                if (!"null".equalsIgnoreCase(name)) {
                    c = LogManagerProperties.newComparator(name);
                    if (Boolean.parseBoolean(reverse)) {
                        assert c != null;
                        c = LogManagerProperties.reverseOrder(c);
                    }
                } else {
                    if (reverse != null) {
                        throw new IllegalArgumentException(
                                "No comparator to reverse.");
                    } else {
                        c = null; //No ordering.
                    }
                }
            } else {
                if (reverse != null) {
                    throw new IllegalArgumentException(
                            "No comparator to reverse.");
                } else {
                    //Don't force the byte code verifier to load the comparator.
                    c = Comparator.class.cast(SeverityComparator.getInstance());
                }
            }
        } catch (final RuntimeException re) {
            throw re; //Avoid catch all.
        } catch (final Exception e) {
            throw new UndeclaredThrowableException(e);
        }
        return c;
    }
}
