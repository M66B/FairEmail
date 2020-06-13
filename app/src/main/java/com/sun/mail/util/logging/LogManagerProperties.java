/*
 * Copyright (c) 2009, 2019 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2009, 2019 Jason Mehrens. All rights reserved.
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

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.UndeclaredThrowableException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.logging.*;
import java.util.logging.Formatter;

/**
 * An adapter class to allow the Mail API to access the LogManager properties.
 * The LogManager properties are treated as the root of all properties. First,
 * the parent properties are searched. If no value is found, then, the
 * LogManager is searched with prefix value. If not found, then, just the key
 * itself is searched in the LogManager. If a value is found in the LogManager
 * it is then copied to this properties object with no key prefix. If no value
 * is found in the LogManager or the parent properties, then this properties
 * object is searched only by passing the key value.
 *
 * <p>
 * This class also emulates the LogManager functions for creating new objects
 * from string class names. This is to support initial setup of objects such as
 * log filters, formatters, error managers, etc.
 *
 * <p>
 * This class should never be exposed outside of this package. Keep this class
 * package private (default access).
 *
 * @author Jason Mehrens
 * @since JavaMail 1.4.3
 */
final class LogManagerProperties extends Properties {

    /**
     * Generated serial id.
     */
    private static final long serialVersionUID = -2239983349056806252L;
    /**
     * Holds the method used to get the LogRecord instant if running on JDK 9 or
     * later.
     */
    private static final Method LR_GET_INSTANT;

    /**
     * Holds the method used to get the default time zone if running on JDK 9 or
     * later.
     */
    private static final Method ZI_SYSTEM_DEFAULT;

    /**
     * Holds the method used to convert and instant to a zoned date time if
     * running on JDK 9 later.
     */
    private static final Method ZDT_OF_INSTANT;

    static {
        Method lrgi = null;
        Method zisd = null;
        Method zdtoi = null;
        try {
            lrgi = LogRecord.class.getMethod("getInstant");
            assert Comparable.class
                    .isAssignableFrom(lrgi.getReturnType()) : lrgi;
            zisd = findClass("java.time.ZoneId")
                    .getMethod("systemDefault");
            if (!Modifier.isStatic(zisd.getModifiers())) {
                throw new NoSuchMethodException(zisd.toString());
            }

            zdtoi = findClass("java.time.ZonedDateTime")
                    .getMethod("ofInstant", findClass("java.time.Instant"),
                            findClass("java.time.ZoneId"));
            if (!Modifier.isStatic(zdtoi.getModifiers())) {
                throw new NoSuchMethodException(zdtoi.toString());
            }

            if (!Comparable.class.isAssignableFrom(zdtoi.getReturnType())) {
                throw new NoSuchMethodException(zdtoi.toString());
            }
        } catch (final RuntimeException ignore) {
        } catch (final Exception ignore) { //No need for specific catch.
        } catch (final LinkageError ignore) {
        } finally {
            if (lrgi == null || zisd == null || zdtoi == null) {
                lrgi = null; //If any are null then clear all.
                zisd = null;
                zdtoi = null;
            }
        }

        LR_GET_INSTANT = lrgi;
        ZI_SYSTEM_DEFAULT = zisd;
        ZDT_OF_INSTANT = zdtoi;
    }
    /**
     * Caches the read only reflection class names string array. Declared
     * volatile for safe publishing only. The VO_VOLATILE_REFERENCE_TO_ARRAY
     * warning is a false positive.
     */
    @SuppressWarnings("VolatileArrayField")
    private static volatile String[] REFLECT_NAMES;
    /**
     * Caches the LogManager or Properties so we only read the configuration
     * once.
     */
    private static final Object LOG_MANAGER = loadLogManager();

    /**
     * Get the LogManager or loads a Properties object to use as the LogManager.
     *
     * @return the LogManager or a loaded Properties object.
     * @since JavaMail 1.5.3
     */
    private static Object loadLogManager() {
        Object m;
        try {
            m = LogManager.getLogManager();
        } catch (final LinkageError restricted) {
            m = readConfiguration();
        } catch (final RuntimeException unexpected) {
            m = readConfiguration();
        }
        return m;
    }

    /**
     * Create a properties object from the default logging configuration file.
     * Since the LogManager is not available in restricted environments, only
     * the default configuration is applicable.
     *
     * @return a properties object loaded with the default configuration.
     * @since JavaMail 1.5.3
     */
    private static Properties readConfiguration() {
        /**
         * Load the properties file so the default settings are available when
         * user code creates a logging object. The class loader for the
         * restricted LogManager can't access these classes to attach them to a
         * logger or handler on startup. Creating logging objects at this point
         * is both useless and risky.
         */
        final Properties props = new Properties();
        try {
            String n = System.getProperty("java.util.logging.config.file");
            if (n != null) {
                final File f = new File(n).getCanonicalFile();
                final InputStream in = new FileInputStream(f);
                try {
                    props.load(in);
                } finally {
                    in.close();
                }
            }
        } catch (final RuntimeException permissionsOrMalformed) {
        } catch (final Exception ioe) {
        } catch (final LinkageError unexpected) {
        }
        return props;
    }

    /**
     * Gets LogManger property for the running JVM. If the LogManager doesn't
     * exist then the default LogManger properties are used.
     *
     * @param name the property name.
     * @return the LogManager.
     * @throws NullPointerException if the given name is null.
     * @since JavaMail 1.5.3
     */
    static String fromLogManager(final String name) {
        if (name == null) {
            throw new NullPointerException();
        }

        final Object m = LOG_MANAGER;
        try {
            if (m instanceof Properties) {
                return ((Properties) m).getProperty(name);
            }
        } catch (final RuntimeException unexpected) {
        }

        if (m != null) {
            try {
                if (m instanceof LogManager) {
                    return ((LogManager) m).getProperty(name);
                }
            } catch (final LinkageError restricted) {
            } catch (final RuntimeException unexpected) {
            }
        }
        return null;
    }

    /**
     * Check that the current context is trusted to modify the logging
     * configuration. This requires LoggingPermission("control").
     * @throws SecurityException  if a security manager exists and the caller
     * does not have {@code LoggingPermission("control")}.
     * @since JavaMail 1.5.3
     */
    static void checkLogManagerAccess() {
        boolean checked = false;
        final Object m = LOG_MANAGER;
        if (m != null) {
            try {
                if (m instanceof LogManager) {
                    checked = true;
                    ((LogManager) m).checkAccess();
                }
            } catch (final SecurityException notAllowed) {
                if (checked) {
                    throw notAllowed;
                }
            } catch (final LinkageError restricted) {
            } catch (final RuntimeException unexpected) {
            }
        }

        if (!checked) {
            checkLoggingAccess();
        }
    }

    /**
     * Check that the current context is trusted to modify the logging
     * configuration when the LogManager is not present. This requires
     * LoggingPermission("control").
     * @throws SecurityException  if a security manager exists and the caller
     * does not have {@code LoggingPermission("control")}.
     * @since JavaMail 1.5.3
     */
    private static void checkLoggingAccess() {
        /**
         * Some environments selectively enforce logging permissions by allowing
         * access to loggers but not allowing access to handlers. This is an
         * indirect way of checking for LoggingPermission when the LogManager is
         * not present. The root logger will lazy create handlers so the global
         * logger is used instead as it is a known named logger with well
         * defined behavior. If the global logger is a subclass then fallback to
         * using the SecurityManager.
         */
        boolean checked = false;
        final Logger global = Logger.getLogger("global");
        try {
            if (Logger.class == global.getClass()) {
                global.removeHandler((Handler) null);
                checked = true;
            }
        } catch (final NullPointerException unexpected) {
        }

        if (!checked) {
            final SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                sm.checkPermission(new LoggingPermission("control", null));
            }
        }
    }

    /**
     * Determines if access to the {@code java.util.logging.LogManager} class is
     * restricted by the class loader.
     *
     * @return true if a LogManager is present.
     * @since JavaMail 1.5.3
     */
    static boolean hasLogManager() {
        final Object m = LOG_MANAGER;
        return m != null && !(m instanceof Properties);
    }

    /**
     * Gets the ZonedDateTime from the given log record.
     *
     * @param record used to generate the zoned date time.
     * @return null if LogRecord doesn't support nanoseconds otherwise a new
     * zoned date time is returned.
     * @throws NullPointerException if record is null.
     * @since JavaMail 1.5.6
     */
    @SuppressWarnings("UseSpecificCatch")
    static Comparable<?> getZonedDateTime(LogRecord record) {
        if (record == null) {
           throw new NullPointerException();
        }
        final Method m = ZDT_OF_INSTANT;
        if (m != null) {
            try {
                return (Comparable<?>) m.invoke((Object) null,
                        LR_GET_INSTANT.invoke(record),
                        ZI_SYSTEM_DEFAULT.invoke((Object) null));
            } catch (final RuntimeException ignore) {
                assert LR_GET_INSTANT != null
                        && ZI_SYSTEM_DEFAULT != null : ignore;
            } catch (final InvocationTargetException ite) {
                final Throwable cause = ite.getCause();
                if (cause instanceof Error) {
                    throw (Error) cause;
                } else if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                } else { //Should never happen.
                    throw new UndeclaredThrowableException(ite);
                }
            } catch (final Exception ignore) {
            }
        }
        return null;
    }

    /**
     * Gets the local host name from the given service.
     *
     * @param s the service to examine.
     * @return the local host name or null.
     * @throws IllegalAccessException if the method is inaccessible.
     * @throws InvocationTargetException if the method throws an exception.
     * @throws LinkageError if the linkage fails.
     * @throws NullPointerException if the given service is null.
     * @throws ExceptionInInitializerError if the static initializer fails.
     * @throws Exception if there is a problem.
     * @throws NoSuchMethodException if the given service does not have a method
     * to get the local host name as a string.
     * @throws SecurityException if unable to inspect properties of object.
     * @since JavaMail 1.5.3
     */
    static String getLocalHost(final Object s) throws Exception {
        try {
            final Method m = s.getClass().getMethod("getLocalHost");
            if (!Modifier.isStatic(m.getModifiers())
                    && m.getReturnType() == String.class) {
                return (String) m.invoke(s);
            } else {
                throw new NoSuchMethodException(m.toString());
            }
        } catch (final ExceptionInInitializerError EIIE) {
            throw wrapOrThrow(EIIE);
        } catch (final InvocationTargetException ite) {
            throw paramOrError(ite);
        }
    }

    /**
     * Used to parse an ISO-8601 duration format of {@code PnDTnHnMn.nS}.
     *
     * @param value an ISO-8601 duration character sequence.
     * @return the number of milliseconds parsed from the duration.
     * @throws ClassNotFoundException if the java.time classes are not present.
     * @throws IllegalAccessException if the method is inaccessible.
     * @throws InvocationTargetException if the method throws an exception.
     * @throws LinkageError if the linkage fails.
     * @throws NullPointerException if the given duration is null.
     * @throws ExceptionInInitializerError if the static initializer fails.
     * @throws Exception if there is a problem.
     * @throws NoSuchMethodException if the correct time methods are missing.
     * @throws SecurityException if reflective access to the java.time classes
     * are not allowed.
     * @since JavaMail 1.5.5
     */
    static long parseDurationToMillis(final CharSequence value) throws Exception {
        try {
            final Class<?> k = findClass("java.time.Duration");
            final Method parse = k.getMethod("parse", CharSequence.class);
            if (!k.isAssignableFrom(parse.getReturnType())
                    || !Modifier.isStatic(parse.getModifiers())) {
               throw new NoSuchMethodException(parse.toString());
            }

            final Method toMillis = k.getMethod("toMillis");
            if (!Long.TYPE.isAssignableFrom(toMillis.getReturnType())
                    || Modifier.isStatic(toMillis.getModifiers())) {
                throw new NoSuchMethodException(toMillis.toString());
            }
            return (Long) toMillis.invoke(parse.invoke(null, value));
        } catch (final ExceptionInInitializerError EIIE) {
            throw wrapOrThrow(EIIE);
        } catch (final InvocationTargetException ite) {
            throw paramOrError(ite);
        }
    }

    /**
     * Converts a locale to a language tag.
     *
     * @param locale the locale to convert.
     * @return the language tag.
     * @throws NullPointerException if the given locale is null.
     * @since JavaMail 1.4.5
     */
    static String toLanguageTag(final Locale locale) {
        final String l = locale.getLanguage();
        final String c = locale.getCountry();
        final String v = locale.getVariant();
        final char[] b = new char[l.length() + c.length() + v.length() + 2];
        int count = l.length();
        l.getChars(0, count, b, 0);
        if (c.length() != 0 || (l.length() != 0 && v.length() != 0)) {
            b[count] = '-';
            ++count; //be nice to the client compiler.
            c.getChars(0, c.length(), b, count);
            count += c.length();
        }

        if (v.length() != 0 && (l.length() != 0 || c.length() != 0)) {
            b[count] = '-';
            ++count; //be nice to the client compiler.
            v.getChars(0, v.length(), b, count);
            count += v.length();
        }
        return String.valueOf(b, 0, count);
    }

    /**
     * Creates a new filter from the given class name.
     *
     * @param name the fully qualified class name.
     * @return a new filter.
     * @throws ClassCastException if class name does not match the type.
     * @throws ClassNotFoundException if the class name was not found.
     * @throws IllegalAccessException if the constructor is inaccessible.
     * @throws InstantiationException if the given class name is abstract.
     * @throws InvocationTargetException if the constructor throws an exception.
     * @throws LinkageError if the linkage fails.
     * @throws ExceptionInInitializerError if the static initializer fails.
     * @throws Exception to match the error method of the ErrorManager.
     * @throws NoSuchMethodException if the class name does not have a no
     * argument constructor.
     * @since JavaMail 1.4.5
     */
    static Filter newFilter(String name) throws Exception {
        return newObjectFrom(name, Filter.class);
    }

    /**
     * Creates a new formatter from the given class name.
     *
     * @param name the fully qualified class name.
     * @return a new formatter.
     * @throws ClassCastException if class name does not match the type.
     * @throws ClassNotFoundException if the class name was not found.
     * @throws IllegalAccessException if the constructor is inaccessible.
     * @throws InstantiationException if the given class name is abstract.
     * @throws InvocationTargetException if the constructor throws an exception.
     * @throws LinkageError if the linkage fails.
     * @throws ExceptionInInitializerError if the static initializer fails.
     * @throws Exception to match the error method of the ErrorManager.
     * @throws NoSuchMethodException if the class name does not have a no
     * argument constructor.
     * @since JavaMail 1.4.5
     */
    static Formatter newFormatter(String name) throws Exception {
        return newObjectFrom(name, Formatter.class);
    }

    /**
     * Creates a new log record comparator from the given class name.
     *
     * @param name the fully qualified class name.
     * @return a new comparator.
     * @throws ClassCastException if class name does not match the type.
     * @throws ClassNotFoundException if the class name was not found.
     * @throws IllegalAccessException if the constructor is inaccessible.
     * @throws InstantiationException if the given class name is abstract.
     * @throws InvocationTargetException if the constructor throws an exception.
     * @throws LinkageError if the linkage fails.
     * @throws ExceptionInInitializerError if the static initializer fails.
     * @throws Exception to match the error method of the ErrorManager.
     * @throws NoSuchMethodException if the class name does not have a no
     * argument constructor.
     * @since JavaMail 1.4.5
     * @see java.util.logging.LogRecord
     */
    @SuppressWarnings("unchecked")
    static Comparator<? super LogRecord> newComparator(String name) throws Exception {
        return newObjectFrom(name, Comparator.class);
    }

    /**
     * Returns a comparator that imposes the reverse ordering of the specified
     * {@link Comparator}. If the given comparator declares a public
     * reverseOrder method that method is called first and the return value is
     * used. If that method is not declared or the caller does not have access
     * then a comparator wrapping the given comparator is returned.
     *
     * @param <T> the element type to be compared
     * @param c a comparator whose ordering is to be reversed by the returned
     * comparator
     * @return A comparator that imposes the reverse ordering of the specified
     * comparator.
     * @throws NullPointerException if the given comparator is null.
     * @since JavaMail 1.5.0
     */
    @SuppressWarnings({"unchecked", "ThrowableResultIgnored"})
    static <T> Comparator<T> reverseOrder(final Comparator<T> c) {
        if (c == null) {
            throw new NullPointerException();
        }

        Comparator<T> reverse = null;
        //Comparator in Java 1.8 has 'reversed' as a default method.
        //This code calls that method first to allow custom
        //code to define what reverse order means.
        try {
            //assert Modifier.isPublic(c.getClass().getModifiers()) :
            //        Modifier.toString(c.getClass().getModifiers());
            final Method m = c.getClass().getMethod("reversed");
            if (!Modifier.isStatic(m.getModifiers())
                    && Comparator.class.isAssignableFrom(m.getReturnType())) {
                try {
                    reverse = (Comparator<T>) m.invoke(c);
                } catch (final ExceptionInInitializerError eiie) {
                    throw wrapOrThrow(eiie);
                }
            }
        } catch (final NoSuchMethodException ignore) {
        } catch (final IllegalAccessException ignore) {
        } catch (final RuntimeException ignore) {
        } catch (final InvocationTargetException ite) {
            paramOrError(ite); //Ignore invocation bugs (returned values).
        }

        if (reverse == null) {
            reverse = Collections.reverseOrder(c);
        }
        return reverse;
    }

    /**
     * Creates a new error manager from the given class name.
     *
     * @param name the fully qualified class name.
     * @return a new error manager.
     * @throws ClassCastException if class name does not match the type.
     * @throws ClassNotFoundException if the class name was not found.
     * @throws IllegalAccessException if the constructor is inaccessible.
     * @throws InstantiationException if the given class name is abstract.
     * @throws InvocationTargetException if the constructor throws an exception.
     * @throws LinkageError if the linkage fails.
     * @throws ExceptionInInitializerError if the static initializer fails.
     * @throws Exception to match the error method of the ErrorManager.
     * @throws NoSuchMethodException if the class name does not have a no
     * argument constructor.
     * @since JavaMail 1.4.5
     */
    static ErrorManager newErrorManager(String name) throws Exception {
        return newObjectFrom(name, ErrorManager.class);
    }

    /**
     * Determines if the given class name identifies a utility class.
     *
     * @param name the fully qualified class name.
     * @return true if the given class name
     * @throws ClassNotFoundException if the class name was not found.
     * @throws IllegalAccessException if the constructor is inaccessible.
     * @throws LinkageError if the linkage fails.
     * @throws ExceptionInInitializerError if the static initializer fails.
     * @throws Exception to match the error method of the ErrorManager.
     * @throws SecurityException if unable to inspect properties of class.
     * @since JavaMail 1.5.2
     */
    static boolean isStaticUtilityClass(String name) throws Exception {
        final Class<?> c = findClass(name);
        final Class<?> obj = Object.class;
        Method[] methods;
        boolean util;
        if (c != obj && (methods = c.getMethods()).length != 0) {
            util = true;
            for (Method m : methods) {
                if (m.getDeclaringClass() != obj
                        && !Modifier.isStatic(m.getModifiers())) {
                    util = false;
                    break;
                }
            }
        } else {
            util = false;
        }
        return util;
    }

    /**
     * Determines if the given class name is a reflection class name responsible
     * for invoking methods and or constructors.
     *
     * @param name the fully qualified class name.
     * @return true if the given class name
     * @throws ClassNotFoundException if the class name was not found.
     * @throws IllegalAccessException if the constructor is inaccessible.
     * @throws LinkageError if the linkage fails.
     * @throws ExceptionInInitializerError if the static initializer fails.
     * @throws Exception to match the error method of the ErrorManager.
     * @throws SecurityException if unable to inspect properties of class.
     * @since JavaMail 1.5.2
     */
    static boolean isReflectionClass(String name) throws Exception {
        String[] names = REFLECT_NAMES;
        if (names == null) { //Benign data race.
            REFLECT_NAMES = names = reflectionClassNames();
        }

        for (String rf : names) { //The set of names is small.
            if (name.equals(rf)) {
                return true;
            }
        }

        findClass(name); //Fail late instead of normal return.
        return false;
    }

    /**
     * Determines all of the reflection class names used to invoke methods.
     *
     * This method performs indirect and direct calls on a throwable to capture
     * the standard class names and the implementation class names.
     *
     * @return a string array containing the fully qualified class names.
     * @throws Exception if there is a problem.
     */
    private static String[] reflectionClassNames() throws Exception {
        final Class<?> thisClass = LogManagerProperties.class;
        assert Modifier.isFinal(thisClass.getModifiers()) : thisClass;
        try {
            final HashSet<String> traces = new HashSet<>();
            Throwable t = Throwable.class.getConstructor().newInstance();
            for (StackTraceElement ste : t.getStackTrace()) {
                if (!thisClass.getName().equals(ste.getClassName())) {
                    traces.add(ste.getClassName());
                } else {
                    break;
                }
            }

            Throwable.class.getMethod("fillInStackTrace").invoke(t);
            for (StackTraceElement ste : t.getStackTrace()) {
                if (!thisClass.getName().equals(ste.getClassName())) {
                    traces.add(ste.getClassName());
                } else {
                    break;
                }
            }
            return traces.toArray(new String[traces.size()]);
        } catch (final InvocationTargetException ITE) {
            throw paramOrError(ITE);
        }
    }

    /**
     * Creates a new object from the given class name.
     *
     * @param <T> The generic class type.
     * @param name the fully qualified class name.
     * @param type the assignable type for the given name.
     * @return a new object assignable to the given type.
     * @throws ClassCastException if class name does not match the type.
     * @throws ClassNotFoundException if the class name was not found.
     * @throws IllegalAccessException if the constructor is inaccessible.
     * @throws InstantiationException if the given class name is abstract.
     * @throws InvocationTargetException if the constructor throws an exception.
     * @throws LinkageError if the linkage fails.
     * @throws ExceptionInInitializerError if the static initializer fails.
     * @throws Exception to match the error method of the ErrorManager.
     * @throws NoSuchMethodException if the class name does not have a no
     * argument constructor.
     * @since JavaMail 1.4.5
     */
    static <T> T newObjectFrom(String name, Class<T> type) throws Exception {
        try {
            final Class<?> clazz = LogManagerProperties.findClass(name);
            //This check avoids additional side effects when the name parameter
            //is a literal name and not a class name.
            if (type.isAssignableFrom(clazz)) {
                try {
                    return type.cast(clazz.getConstructor().newInstance());
                } catch (final InvocationTargetException ITE) {
                    throw paramOrError(ITE);
                }
            } else {
                throw new ClassCastException(clazz.getName()
                        + " cannot be cast to " + type.getName());
            }
        } catch (final NoClassDefFoundError NCDFE) {
            //No class def found can occur on filesystems that are
            //case insensitive (BUG ID 6196068).  In some cases, we allow class
            //names or literal names, this code guards against the case where a
            //literal name happens to match a class name in a different case.
            //This is also a nice way to adapt this error for the error manager.
            throw new ClassNotFoundException(NCDFE.toString(), NCDFE);
        } catch (final ExceptionInInitializerError EIIE) {
            throw wrapOrThrow(EIIE);
        }
    }

    /**
     * Returns the given exception or throws the escaping cause.
     *
     * @param ite any invocation target.
     * @return the exception.
     * @throws VirtualMachineError if present as cause.
     * @throws ThreadDeath if present as cause.
     * @since JavaMail 1.4.5
     */
    private static Exception paramOrError(InvocationTargetException ite) {
        final Throwable cause = ite.getCause();
        if (cause != null) {
            //Bitwise inclusive OR produces tighter bytecode for instanceof
            //and matches with multicatch syntax.
            if (cause instanceof VirtualMachineError
                    | cause instanceof ThreadDeath) {
                throw (Error) cause;
            }
        }
        return ite;
    }

    /**
     * Throws the given error if the cause is an error otherwise the given error
     * is wrapped.
     *
     * @param eiie the error.
     * @return an InvocationTargetException.
     * @since JavaMail 1.5.0
     */
    private static InvocationTargetException wrapOrThrow(
            ExceptionInInitializerError eiie) {
        //This linkage error will escape the constructor new instance call.
        //If the cause is an error, rethrow to skip any error manager.
        if (eiie.getCause() instanceof Error) {
            throw eiie;
        } else {
            //Considered a bug in the code, wrap the error so it can be
            //reported to the error manager.
            return new InvocationTargetException(eiie);
        }
    }

    /**
     * This code is modified from the LogManager, which explictly states
     * searching the system class loader first, then the context class loader.
     * There is resistance (compatibility) to change this behavior to simply
     * searching the context class loader.
     *
     * @param name full class name
     * @return the class.
     * @throws LinkageError if the linkage fails.
     * @throws ClassNotFoundException if the class name was not found.
     * @throws ExceptionInInitializerError if static initializer fails.
     */
    private static Class<?> findClass(String name) throws ClassNotFoundException {
        ClassLoader[] loaders = getClassLoaders();
        assert loaders.length == 2 : loaders.length;
        Class<?> clazz;
        if (loaders[0] != null) {
            try {
                clazz = Class.forName(name, false, loaders[0]);
            } catch (ClassNotFoundException tryContext) {
                clazz = tryLoad(name, loaders[1]);
            }
        } else {
            clazz = tryLoad(name, loaders[1]);
        }
        return clazz;
    }

    /**
     * Loads a class using the given loader or the class loader of this class.
     *
     * @param name the class name.
     * @param l any class loader or null.
     * @return the raw class.
     * @throws ClassNotFoundException if not found.
     */
    private static Class<?> tryLoad(String name, ClassLoader l) throws ClassNotFoundException {
        if (l != null) {
            return Class.forName(name, false, l);
        } else {
            return Class.forName(name);
        }
    }

    /**
     * Gets the class loaders using elevated privileges.
     *
     * @return any array of class loaders. Indexes may be null.
     */
    private static ClassLoader[] getClassLoaders() {
        return AccessController.doPrivileged(new PrivilegedAction<ClassLoader[]>() {

            @SuppressWarnings("override") //JDK-6954234
            public ClassLoader[] run() {
                final ClassLoader[] loaders = new ClassLoader[2];
                try {
                    loaders[0] = ClassLoader.getSystemClassLoader();
                } catch (SecurityException ignore) {
                    loaders[0] = null;
                }

                try {
                    loaders[1] = Thread.currentThread().getContextClassLoader();
                } catch (SecurityException ignore) {
                    loaders[1] = null;
                }
                return loaders;
            }
        });
    }
    /**
     * The namespace prefix to search LogManager and defaults.
     */
    private final String prefix;

    /**
     * Creates a log manager properties object.
     *
     * @param parent the parent properties.
     * @param prefix the namespace prefix.
     * @throws NullPointerException if <code>prefix</code> or
     * <code>parent</code> is <code>null</code>.
     */
    LogManagerProperties(final Properties parent, final String prefix) {
        super(parent);
        if (parent == null || prefix == null) {
            throw new NullPointerException();
        }
        this.prefix = prefix;
    }

    /**
     * Returns a properties object that contains a snapshot of the current
     * state. This method violates the clone contract so that no instances of
     * LogManagerProperties is exported for public use.
     *
     * @return the snapshot.
     * @since JavaMail 1.4.4
     */
    @Override
    @SuppressWarnings("CloneDoesntCallSuperClone")
    public synchronized Object clone() {
        return exportCopy(defaults);
    }

    /**
     * Searches defaults, then searches the log manager if available or the
     * system properties by the prefix property, and then by the key itself.
     *
     * @param key a non null key.
     * @return the value for that key.
     */
    @Override
    public synchronized String getProperty(final String key) {
        String value = defaults.getProperty(key);
        if (value == null) {
            if (key.length() > 0) {
                value = fromLogManager(prefix + '.' + key);
            }

            if (value == null) {
                value = fromLogManager(key);
            }

            /**
             * Copy the log manager properties as we read them. If a value is no
             * longer present in the LogManager read it from here. The reason
             * this works is because LogManager.reset() closes all attached
             * handlers therefore, stale values only exist in closed handlers.
             */
            if (value != null) {
                super.put(key, value);
            } else {
                Object v = super.get(key); //defaults are not used.
                value = v instanceof String ? (String) v : null;
            }
        }
        return value;
    }

    /**
     * Calls getProperty directly. If getProperty returns null the default value
     * is returned.
     *
     * @param key a key to search for.
     * @param def the default value to use if not found.
     * @return the value for the key.
     * @since JavaMail 1.4.4
     */
    @Override
    public String getProperty(final String key, final String def) {
        final String value = this.getProperty(key);
        return value == null ? def : value;
    }

    /**
     * Required to work with PropUtil. Calls getProperty directly if the given
     * key is a string. Otherwise, performs a get operation on the defaults
     * followed by the normal hash table get.
     *
     * @param key any key.
     * @return the value for the key or null.
     * @since JavaMail 1.4.5
     */
    @Override
    public synchronized Object get(final Object key) {
        Object value;
        if (key instanceof String) {
            value = getProperty((String) key);
        } else {
            value = null;
        }

        //Search for non-string value.
        if (value == null) {
            value = defaults.get(key);
            if (value == null && !defaults.containsKey(key)) {
                value = super.get(key);
            }
        }
        return value;
    }

    /**
     * Required to work with PropUtil. An updated copy of the key is fetched
     * from the log manager if the key doesn't exist in this properties.
     *
     * @param key any key.
     * @return the value for the key or the default value for the key.
     * @since JavaMail 1.4.5
     */
    @Override
    public synchronized Object put(final Object key, final Object value) {
        if (key instanceof String && value instanceof String) {
            final Object def = preWrite(key);
            final Object man = super.put(key, value);
            return man == null ? def : man;
        } else {
            return super.put(key, value);
        }
    }

    /**
     * Calls the put method directly.
     *
     * @param key any key.
     * @return the value for the key or the default value for the key.
     * @since JavaMail 1.4.5
     */
    @Override
    public Object setProperty(String key, String value) {
        return this.put(key, value);
    }

    /**
     * Required to work with PropUtil. An updated copy of the key is fetched
     * from the log manager prior to returning.
     *
     * @param key any key.
     * @return the value for the key or null.
     * @since JavaMail 1.4.5
     */
    @Override
    public synchronized boolean containsKey(final Object key) {
        boolean found = key instanceof String
                && getProperty((String) key) != null;
        if (!found) {
            found = defaults.containsKey(key) || super.containsKey(key);
        }
        return found;
    }

    /**
     * Required to work with PropUtil. An updated copy of the key is fetched
     * from the log manager if the key doesn't exist in this properties.
     *
     * @param key any key.
     * @return the value for the key or the default value for the key.
     * @since JavaMail 1.4.5
     */
    @Override
    public synchronized Object remove(final Object key) {
        final Object def = preWrite(key);
        final Object man = super.remove(key);
        return man == null ? def : man;
    }

    /**
     * It is assumed that this method will never be called. No way to get the
     * property names from LogManager.
     *
     * @return the property names
     */
    @Override
    public Enumeration<?> propertyNames() {
        assert false;
        return super.propertyNames();
    }

    /**
     * It is assumed that this method will never be called. The prefix value is
     * not used for the equals method.
     *
     * @param o any object or null.
     * @return true if equal, otherwise false.
     */
    @Override
    public boolean equals(final Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (o instanceof Properties == false) {
            return false;
        }
        assert false : prefix;
        return super.equals(o);
    }

    /**
     * It is assumed that this method will never be called. See equals.
     *
     * @return the hash code.
     */
    @Override
    public int hashCode() {
        assert false : prefix.hashCode();
        return super.hashCode();
    }

    /**
     * Called before a write operation of a key. Caches a key read from the log
     * manager in this properties object. The key is only cached if it is an
     * instance of a String and this properties doesn't contain a copy of the
     * key.
     *
     * @param key the key to search.
     * @return the default value for the key.
     */
    private Object preWrite(final Object key) {
        assert Thread.holdsLock(this);
        return get(key);
    }

    /**
     * Creates a public snapshot of this properties object using the given
     * parent properties.
     *
     * @param parent the defaults to use with the snapshot.
     * @return the safe snapshot.
     */
    private Properties exportCopy(final Properties parent) {
        Thread.holdsLock(this);
        final Properties child = new Properties(parent);
        child.putAll(this);
        return child;
    }

    /**
     * It is assumed that this method will never be called. We return a safe
     * copy for export to avoid locking this properties object or the defaults
     * during write.
     *
     * @return the parent properties.
     * @throws ObjectStreamException if there is a problem.
     */
    private synchronized Object writeReplace() throws ObjectStreamException {
        assert false;
        return exportCopy((Properties) defaults.clone());
    }
}
