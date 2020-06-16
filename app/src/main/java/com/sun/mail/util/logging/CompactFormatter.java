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

import java.util.*;
import java.util.logging.LogRecord;

/**
 * A plain text formatter that can produce fixed width output. By default this
 * formatter will produce output no greater than 160 characters wide plus the
 * separator and newline characters. Only specified fields support an
 * {@linkplain #toAlternate(java.lang.String) alternate} fixed width format.
 * <p>
 * By default each <code>CompactFormatter</code> is initialized using the
 * following LogManager configuration properties where
 * <code>&lt;formatter-name&gt;</code> refers to the fully qualified class name
 * or the fully qualified derived class name of the formatter. If properties are
 * not defined, or contain invalid values, then the specified default values are
 * used.
 * <ul>
 * <li>&lt;formatter-name&gt;.format - the {@linkplain java.util.Formatter
 *     format} string used to transform the output. The format string can be
 * used to fix the output size. (defaults to <code>%7$#.160s%n</code>)</li>
 * </ul>
 *
 * @author Jason Mehrens
 * @since JavaMail 1.5.2
 */
public class CompactFormatter extends java.util.logging.Formatter {

    /**
     * Load any declared classes to workaround GLASSFISH-21258.
     */
    static {
        loadDeclaredClasses();
    }

    /**
     * Used to load declared classes encase class loader doesn't allow loading
     * during JVM termination. This method is used with unit testing.
     *
     * @return an array of classes never null.
     */
    private static Class<?>[] loadDeclaredClasses() {
        return new Class<?>[]{Alternate.class};
    }

    /**
     * Holds the java.util.Formatter pattern.
     */
    private final String fmt;

    /**
     * Creates an instance with a default format pattern.
     */
    public CompactFormatter() {
        String p = getClass().getName();
        this.fmt = initFormat(p);
    }

    /**
     * Creates an instance with the given format pattern.
     *
     * @param format the {@linkplain java.util.Formatter pattern} or null to use
     * the LogManager default. The arguments are described in the
     * {@linkplain #format(java.util.logging.LogRecord) format} method.
     */
    public CompactFormatter(final String format) {
        String p = getClass().getName();
        this.fmt = format == null ? initFormat(p) : format;
    }

    /**
     * Format the given log record and returns the formatted string. The
     * {@linkplain java.util.Formatter#format(java.lang.String, java.lang.Object...)
     * java.util} argument indexes are assigned to the following properties:
     *
     * <ol start='0'>
     * <li>{@code format} - the {@linkplain java.util.Formatter
     *     java.util.Formatter} format string specified in the
     * &lt;formatter-name&gt;.format property or the format that was given when
     * this formatter was created.</li>
     * <li>{@code date} - if the log record supports nanoseconds then a
     * ZonedDateTime object representing the event time of the log record in the
     * system time zone. Otherwise, a {@linkplain Date} object representing
     * {@linkplain LogRecord#getMillis event time} of the log record.</li>
     * <li>{@code source} - a string representing the caller, if available;
     * otherwise, the logger's name.</li>
     * <li>{@code logger} - the logger's
     * {@linkplain Class#getSimpleName() simple}
     * {@linkplain LogRecord#getLoggerName() name}.</li>
     * <li>{@code level} - the
     * {@linkplain java.util.logging.Level#getLocalizedName log level}.</li>
     * <li>{@code message} - the formatted log message returned from the
     * {@linkplain #formatMessage(LogRecord)} method.</li>
     * <li>{@code thrown} - a string representing the
     * {@linkplain LogRecord#getThrown throwable} associated with the log record
     * and a relevant stack trace element if available; otherwise, an empty
     * string is used.</li>
     * <li>{@code message|thrown} The message and the thrown properties joined
     * as one parameter. This parameter supports
     * {@linkplain #toAlternate(java.lang.String) alternate} form.</li>
     * <li>{@code thrown|message} The thrown and message properties joined as
     * one parameter. This parameter supports
     * {@linkplain #toAlternate(java.lang.String) alternate} form.</li>
     * <li>{@code sequence} the
     * {@linkplain LogRecord#getSequenceNumber() sequence number} if the given
     * log record.</li>
     * <li>{@code thread id} the {@linkplain LogRecord#getThreadID() thread id}
     * of the given log record. By default this is formatted as a {@code long}
     * by an unsigned conversion.</li>
     * <li>{@code error} the throwable
     * {@linkplain Class#getSimpleName() simple class name} and
     * {@linkplain #formatError(LogRecord) error message} without any stack
     * trace.</li>
     * <li>{@code message|error} The message and error properties joined as one
     * parameter. This parameter supports
     * {@linkplain #toAlternate(java.lang.String) alternate} form.</li>
     * <li>{@code error|message} The error and message properties joined as one
     * parameter. This parameter supports
     * {@linkplain #toAlternate(java.lang.String) alternate} form.</li>
     * <li>{@code backtrace} only the
     * {@linkplain #formatBackTrace(LogRecord) stack trace} of the given
     * throwable.</li>
     * <li>{@code bundlename} the resource bundle
     * {@linkplain LogRecord#getResourceBundleName() name} of the given log
     * record.</li>
     * <li>{@code key} the {@linkplain LogRecord#getMessage() raw message}
     * before localization or formatting.</li>
     * </ol>
     *
     * <p>
     * Some example formats:<br>
     * <ul>
     * <li>{@code com.sun.mail.util.logging.CompactFormatter.format=%7$#.160s%n}
     * <p>
     * This prints only 160 characters of the message|thrown ({@code 7$}) using
     * the {@linkplain #toAlternate(java.lang.String) alternate} form. The
     * separator is not included as part of the total width.
     * <pre>
     * Encoding failed.|NullPointerException: null String.getBytes(:913)
     * </pre>
     *
     * <li>{@code com.sun.mail.util.logging.CompactFormatter.format=%7$#.20s%n}
     * <p>
     * This prints only 20 characters of the message|thrown ({@code 7$}) using
     * the {@linkplain #toAlternate(java.lang.String) alternate} form. This will
     * perform a weighted truncation of both the message and thrown properties
     * of the log record. The separator is not included as part of the total
     * width.
     * <pre>
     * Encoding|NullPointerE
     * </pre>
     *
     * <li>{@code com.sun.mail.util.logging.CompactFormatter.format=%1$tc %2$s%n%4$s: %5$s%6$s%n}
     * <p>
     * This prints the timestamp ({@code 1$}) and the source ({@code 2$}) on the
     * first line. The second line is the log level ({@code 4$}), log message
     * ({@code 5$}), and the throwable with a relevant stack trace element
     * ({@code 6$}) if one is available.
     * <pre>
     * Fri Nov 20 07:29:24 CST 2009 MyClass fatal
     * SEVERE: Encoding failed.NullPointerException: null String.getBytes(:913)
     * </pre>
     *
     * <li>{@code com.sun.mail.util.logging.CompactFormatter.format=%4$s: %12$#.160s%n}
     * <p>
     * This prints the log level ({@code 4$}) and only 160 characters of the
     * message|error ({@code 12$}) using the alternate form.
     * <pre>
     * SEVERE: Unable to send notification.|SocketException: Permission denied: connect
     * </pre>
     *
     * <li>{@code com.sun.mail.util.logging.CompactFormatter.format=[%9$d][%1$tT][%10$d][%2$s] %5$s%n%6$s%n}
     * <p>
     * This prints the sequence ({@code 9$}), event time ({@code 1$}) as 24 hour
     * time, thread id ({@code 10$}), source ({@code 2$}), log message
     * ({@code 5$}), and the throwable with back trace ({@code 6$}).
     * <pre>
     * [125][14:11:42][38][MyClass fatal] Unable to send notification.
     * SocketException: Permission denied: connect SMTPTransport.openServer(:1949)
     * </pre>
     *
     * </ul>
     *
     * @param record the log record to format.
     * @return the formatted record.
     * @throws NullPointerException if the given record is null.
     */
    @Override
    public String format(final LogRecord record) {
        //LogRecord is mutable so define local vars.
        ResourceBundle rb = record.getResourceBundle();
        Locale l = rb == null ? null : rb.getLocale();

        String msg = formatMessage(record);
        String thrown = formatThrown(record);
        String err = formatError(record);
        Object[] params = {
            formatZonedDateTime(record),
            formatSource(record),
            formatLoggerName(record),
            formatLevel(record),
            msg,
            thrown,
            new Alternate(msg, thrown),
            new Alternate(thrown, msg),
            record.getSequenceNumber(),
            formatThreadID(record),
            err,
            new Alternate(msg, err),
            new Alternate(err, msg),
            formatBackTrace(record),
            record.getResourceBundleName(),
            record.getMessage()};

        if (l == null) { //BUG ID 6282094
            return String.format(fmt, params);
        } else {
            return String.format(l, fmt, params);
        }
    }

    /**
     * Formats message for the log record. This method removes any fully
     * qualified throwable class names from the message.
     *
     * @param record the log record.
     * @return the formatted message string.
     */
    @Override
    public String formatMessage(final LogRecord record) {
        String msg = super.formatMessage(record);
        msg = replaceClassName(msg, record.getThrown());
        msg = replaceClassName(msg, record.getParameters());
        return msg;
    }

    /**
     * Formats the message from the thrown property of the log record. This
     * method replaces fully qualified throwable class names from the message
     * cause chain with simple class names.
     *
     * @param t the throwable to format or null.
     * @return the empty string if null was given or the formatted message
     * string from the throwable which may be null.
     */
    public String formatMessage(final Throwable t) {
        String r;
        if (t != null) {
            final Throwable apply = apply(t);
            final String m = apply.getLocalizedMessage();
            final String s = apply.toString();
            final String sn = simpleClassName(apply.getClass());
            if (!isNullOrSpaces(m)) {
                if (s.contains(m)) {
                    if (s.startsWith(apply.getClass().getName())
                            || s.startsWith(sn)) {
                        r = replaceClassName(m, t);
                    } else {
                        r = replaceClassName(simpleClassName(s), t);
                    }
                } else {
                    r = replaceClassName(simpleClassName(s) + ": " + m, t);
                }
            } else {
                r = replaceClassName(simpleClassName(s), t);
            }

            if (!r.contains(sn)) {
                r = sn + ": " + r;
            }
        } else {
            r = "";
        }
        return r;
    }

    /**
     * Formats the level property of the given log record.
     *
     * @param record the record.
     * @return the formatted logger name.
     * @throws NullPointerException if the given record is null.
     */
    public String formatLevel(final LogRecord record) {
        return record.getLevel().getLocalizedName();
    }

    /**
     * Formats the source from the given log record.
     *
     * @param record the record.
     * @return the formatted source of the log record.
     * @throws NullPointerException if the given record is null.
     */
    public String formatSource(final LogRecord record) {
        String source = record.getSourceClassName();
        if (source != null) {
            if (record.getSourceMethodName() != null) {
                source = simpleClassName(source) + " "
                        + record.getSourceMethodName();
            } else {
                source = simpleClassName(source);
            }
        } else {
            source = simpleClassName(record.getLoggerName());
        }
        return source;
    }

    /**
     * Formats the logger name property of the given log record.
     *
     * @param record the record.
     * @return the formatted logger name.
     * @throws NullPointerException if the given record is null.
     */
    public String formatLoggerName(final LogRecord record) {
        return simpleClassName(record.getLoggerName());
    }

    /**
     * Formats the thread id property of the given log record. By default this
     * is formatted as a {@code long} by an unsigned conversion.
     *
     * @param record the record.
     * @return the formatted thread id as a number.
     * @throws NullPointerException if the given record is null.
     * @since JavaMail 1.5.4
     */
    public Number formatThreadID(final LogRecord record) {
        /**
         * Thread.getID is defined as long and LogRecord.getThreadID is defined
         * as int. Convert to unsigned as a means to better map the two types of
         * thread identifiers.
         */
        return (((long) record.getThreadID()) & 0xffffffffL);
    }

    /**
     * Formats the thrown property of a LogRecord. The returned string will
     * contain a throwable message with a back trace.
     *
     * @param record the record.
     * @return empty string if nothing was thrown or formatted string.
     * @throws NullPointerException if the given record is null.
     * @see #apply(java.lang.Throwable)
     * @see #formatBackTrace(java.util.logging.LogRecord)
     */
    public String formatThrown(final LogRecord record) {
        String msg;
        final Throwable t = record.getThrown();
        if (t != null) {
            String site = formatBackTrace(record);
            msg = formatMessage(t) + (isNullOrSpaces(site) ? "" : ' ' + site);
        } else {
            msg = "";
        }
        return msg;
    }

    /**
     * Formats the thrown property of a LogRecord as an error message. The
     * returned string will not contain a back trace.
     *
     * @param record the record.
     * @return empty string if nothing was thrown or formatted string.
     * @throws NullPointerException if the given record is null.
     * @see Throwable#toString()
     * @see #apply(java.lang.Throwable)
     * @see #formatMessage(java.lang.Throwable)
     * @since JavaMail 1.5.4
     */
    public String formatError(final LogRecord record) {
        return formatMessage(record.getThrown());
    }

    /**
     * Formats the back trace for the given log record.
     *
     * @param record the log record to format.
     * @return the formatted back trace.
     * @throws NullPointerException if the given record is null.
     * @see #apply(java.lang.Throwable)
     * @see #formatThrown(java.util.logging.LogRecord)
     * @see #ignore(java.lang.StackTraceElement)
     */
    public String formatBackTrace(final LogRecord record) {
        String site = "";
        final Throwable t = record.getThrown();
        if (t != null) {
            final Throwable root = apply(t);
            StackTraceElement[] trace = root.getStackTrace();
            site = findAndFormat(trace);
            if (isNullOrSpaces(site)) {
                int limit = 0;
                for (Throwable c = t; c != null; c = c.getCause()) {
                    StackTraceElement[] ste = c.getStackTrace();
                    site = findAndFormat(ste);
                    if (!isNullOrSpaces(site)) {
                        break;
                    } else {
                        if (trace.length == 0) {
                           trace = ste;
                        }
                    }

                    //Deal with excessive cause chains
                    //and cyclic throwables.
                    if (++limit == (1 << 16)) {
                        break; //Give up.
                    }
                }

                //Punt.
                if (isNullOrSpaces(site) && trace.length != 0) {
                    site = formatStackTraceElement(trace[0]);
                }
            }
        }
        return site;
    }

    /**
     * Finds and formats the first stack frame of interest.
     *
     * @param trace the fill stack to examine.
     * @return a String that best describes the call site.
     * @throws NullPointerException if stack trace element array is null.
     */
    private String findAndFormat(final StackTraceElement[] trace) {
        String site = "";
        for (StackTraceElement s : trace) {
            if (!ignore(s)) {
                site = formatStackTraceElement(s);
                break;
            }
        }

        //Check if all code was compiled with no debugging info.
        if (isNullOrSpaces(site)) {
            for (StackTraceElement s : trace) {
                if (!defaultIgnore(s)) {
                    site = formatStackTraceElement(s);
                    break;
                }
            }
        }
        return site;
    }

    /**
     * Formats a stack trace element into a simple call site.
     *
     * @param s the stack trace element to format.
     * @return the formatted stack trace element.
     * @throws NullPointerException if stack trace element is null.
     * @see #formatThrown(java.util.logging.LogRecord)
     */
    private String formatStackTraceElement(final StackTraceElement s) {
        String v = simpleClassName(s.getClassName());
        String result;
        if (v != null) {
            result = s.toString().replace(s.getClassName(), v);
        } else {
            result = s.toString();
        }

        //If the class name contains the simple file name then remove file name.
        v = simpleFileName(s.getFileName());
        if (v != null && result.startsWith(v)) {
            result = result.replace(s.getFileName(), "");
        }
        return result;
    }

    /**
     * Chooses a single throwable from the cause chain that will be formatted.
     * This implementation chooses the throwable that best describes the chain.
     * Subclasses can override this method to choose an alternate throwable for
     * formatting.
     *
     * @param t the throwable from the log record.
     * @return the chosen throwable or null only if the given argument is null.
     * @see #formatThrown(java.util.logging.LogRecord)
     */
    protected Throwable apply(final Throwable t) {
        return SeverityComparator.getInstance().apply(t);
    }

    /**
     * Determines if a stack frame should be ignored as the cause of an error.
     *
     * @param s the stack trace element.
     * @return true if this frame should be ignored.
     * @see #formatThrown(java.util.logging.LogRecord)
     */
    protected boolean ignore(final StackTraceElement s) {
        return isUnknown(s) || defaultIgnore(s);
    }

    /**
     * Defines the alternate format. This implementation removes all control
     * characters from the given string.
     *
     * @param s any string or null.
     * @return null if the argument was null otherwise, an alternate string.
     */
    protected String toAlternate(final String s) {
        return s != null ? s.replaceAll("[\\x00-\\x1F\\x7F]+", "") : null;
    }

    /**
     * Gets the zoned date time from the given log record.
     *
     * @param record the current log record.
     * @return a zoned date time or a legacy date object.
     * @throws NullPointerException if the given record is null.
     * @since JavaMail 1.5.6
     */
    private Comparable<?> formatZonedDateTime(final LogRecord record) {
        Comparable<?> zdt = LogManagerProperties.getZonedDateTime(record);
        if (zdt == null) {
            zdt = new java.util.Date(record.getMillis());
        }
        return zdt;
    }

    /**
     * Determines if a stack frame should be ignored as the cause of an error.
     * This does not check for unknown line numbers because code can be compiled
     * without debugging info.
     *
     * @param s the stack trace element.
     * @return true if this frame should be ignored.
     */
    private boolean defaultIgnore(final StackTraceElement s) {
        return isSynthetic(s) || isStaticUtility(s) || isReflection(s);
    }

    /**
     * Determines if a stack frame is for a static utility class.
     *
     * @param s the stack trace element.
     * @return true if this frame should be ignored.
     */
    private boolean isStaticUtility(final StackTraceElement s) {
        try {
            return LogManagerProperties.isStaticUtilityClass(s.getClassName());
        } catch (RuntimeException ignore) {
        } catch (Exception | LinkageError ignore) {
        }
        final String cn = s.getClassName();
        return (cn.endsWith("s") && !cn.endsWith("es"))
                || cn.contains("Util") || cn.endsWith("Throwables");
    }

    /**
     * Determines if a stack trace element is for a synthetic method.
     *
     * @param s the stack trace element.
     * @return true if synthetic.
     * @throws NullPointerException if stack trace element is null.
     */
    private boolean isSynthetic(final StackTraceElement s) {
        return s.getMethodName().indexOf('$') > -1;
    }

    /**
     * Determines if a stack trace element has an unknown line number or a
     * native line number.
     *
     * @param s the stack trace element.
     * @return true if the line number is unknown.
     * @throws NullPointerException if stack trace element is null.
     */
    private boolean isUnknown(final StackTraceElement s) {
        return s.getLineNumber() < 0;
    }

    /**
     * Determines if a stack trace element represents a reflection frame.
     *
     * @param s the stack trace element.
     * @return true if the line number is unknown.
     * @throws NullPointerException if stack trace element is null.
     */
    private boolean isReflection(final StackTraceElement s) {
        try {
            return LogManagerProperties.isReflectionClass(s.getClassName());
        } catch (RuntimeException ignore) {
        } catch (Exception | LinkageError ignore) {
        }
        return s.getClassName().startsWith("java.lang.reflect.")
                || s.getClassName().startsWith("sun.reflect.");
    }

    /**
     * Creates the format pattern for this formatter.
     *
     * @param p the class name prefix.
     * @return the java.util.Formatter format string.
     * @throws NullPointerException if the given class name is null.
     */
    private String initFormat(final String p) {
        String v = LogManagerProperties.fromLogManager(p.concat(".format"));
        if (isNullOrSpaces(v)) {
            v = "%7$#.160s%n"; //160 chars split between message and thrown.
        }
        return v;
    }

    /**
     * Searches the given message for all instances fully qualified class name
     * with simple class name based off of the types contained in the given
     * parameter array.
     *
     * @param msg the message.
     * @param t the throwable cause chain to search or null.
     * @return the modified message string.
     */
    private static String replaceClassName(String msg, Throwable t) {
        if (!isNullOrSpaces(msg)) {
            int limit = 0;
            for (Throwable c = t; c != null; c = c.getCause()) {
                final Class<?> k = c.getClass();
                msg = msg.replace(k.getName(), simpleClassName(k));

                //Deal with excessive cause chains and cyclic throwables.
                if (++limit == (1 << 16)) {
                    break; //Give up.
                }
            }
        }
        return msg;
    }

    /**
     * Searches the given message for all instances fully qualified class name
     * with simple class name based off of the types contained in the given
     * parameter array.
     *
     * @param msg the message or null.
     * @param p the parameter array or null.
     * @return the modified message string.
     */
    private static String replaceClassName(String msg, Object[] p) {
        if (!isNullOrSpaces(msg) && p != null) {
            for (Object o : p) {
                if (o != null) {
                    final Class<?> k = o.getClass();
                    msg = msg.replace(k.getName(), simpleClassName(k));
                }
            }
        }
        return msg;
    }

    /**
     * Gets the simple class name from the given class. This is a workaround for
     * BUG ID JDK-8057919.
     *
     * @param k the class object.
     * @return the simple class name or null.
     * @since JavaMail 1.5.3
     */
    private static String simpleClassName(final Class<?> k) {
        try {
            return k.getSimpleName();
        } catch (final InternalError JDK8057919) {
        }
        return simpleClassName(k.getName());
    }

    /**
     * Converts a fully qualified class name to a simple class name. If the
     * leading part of the given string is not a legal class name then the given
     * string is returned.
     *
     * @param name the fully qualified class name prefix or null.
     * @return the simple class name or given input.
     */
    private static String simpleClassName(String name) {
        if (name != null) {
            int cursor = 0;
            int sign = -1;
            int dot = -1;
            for (int c, prev = dot; cursor < name.length();
                    cursor += Character.charCount(c)) {
                c = name.codePointAt(cursor);
                if (!Character.isJavaIdentifierPart(c)) {
                    if (c == ((int) '.')) {
                        if ((dot + 1) != cursor && (dot + 1) != sign) {
                            prev = dot;
                            dot = cursor;
                        } else {
                            return name;
                        }
                    } else {
                        if ((dot + 1) == cursor) {
                            dot = prev;
                        }
                        break;
                    }
                } else {
                    if (c == ((int) '$')) {
                        sign = cursor;
                    }
                }
            }

            if (dot > -1 && ++dot < cursor && ++sign < cursor) {
                name = name.substring(sign > dot ? sign : dot);
            }
        }
        return name;
    }

    /**
     * Converts a file name with an extension to a file name without an
     * extension.
     *
     * @param name the full file name or null.
     * @return the simple file name or null.
     */
    private static String simpleFileName(String name) {
        if (name != null) {
            final int index = name.lastIndexOf('.');
            name = index > -1 ? name.substring(0, index) : name;
        }
        return name;
    }

    /**
     * Determines is the given string is null or spaces.
     *
     * @param s the string or null.
     * @return true if null or spaces.
     */
    private static boolean isNullOrSpaces(final String s) {
        return s == null || s.trim().length() == 0;
    }

    /**
     * Used to format two arguments as fixed length message.
     */
    private class Alternate implements java.util.Formattable {

        /**
         * The left side of the output.
         */
        private final String left;
        /**
         * The right side of the output.
         */
        private final String right;

        /**
         * Creates an alternate output.
         *
         * @param left the left side or null.
         * @param right the right side or null.
         */
        Alternate(final String left, final String right) {
            this.left = String.valueOf(left);
            this.right = String.valueOf(right);
        }

        @SuppressWarnings("override") //JDK-6954234
        public void formatTo(java.util.Formatter formatter, int flags,
                int width, int precision) {

            String l = left;
            String r = right;
            if ((flags & java.util.FormattableFlags.UPPERCASE)
                    == java.util.FormattableFlags.UPPERCASE) {
                l = l.toUpperCase(formatter.locale());
                r = r.toUpperCase(formatter.locale());
            }

            if ((flags & java.util.FormattableFlags.ALTERNATE)
                    == java.util.FormattableFlags.ALTERNATE) {
                l = toAlternate(l);
                r = toAlternate(r);
            }

            if (precision <= 0) {
                precision = Integer.MAX_VALUE;
            }

            int fence = Math.min(l.length(), precision);
            if (fence > (precision >> 1)) {
                fence = Math.max(fence - r.length(), fence >> 1);
            }

            if (fence > 0) {
                if (fence > l.length()
                        && Character.isHighSurrogate(l.charAt(fence - 1))) {
                    --fence;
                }
                l = l.substring(0, fence);
            }
            r = r.substring(0, Math.min(precision - fence, r.length()));

            if (width > 0) {
                final int half = width >> 1;
                if (l.length() < half) {
                    l = pad(flags, l, half);
                }

                if (r.length() < half) {
                    r = pad(flags, r, half);
                }
            }

            Object[] empty = Collections.emptySet().toArray();
            formatter.format(l, empty);
            if (l.length() != 0 && r.length() != 0) {
                formatter.format("|", empty);
            }
            formatter.format(r, empty);
        }

        /**
         * Pad the given input string.
         *
         * @param flags the formatter flags.
         * @param s the string to pad.
         * @param length the final string length.
         * @return the padded string.
         */
        private String pad(int flags, String s, int length) {
            final int padding = length - s.length();
            final StringBuilder b = new StringBuilder(length);
            if ((flags & java.util.FormattableFlags.LEFT_JUSTIFY)
                    == java.util.FormattableFlags.LEFT_JUSTIFY) {
                for (int i = 0; i < padding; ++i) {
                    b.append('\u0020');
                }
                b.append(s);
            } else {
                b.append(s);
                for (int i = 0; i < padding; ++i) {
                    b.append('\u0020');
                }
            }
            return b.toString();
        }
    }
}
