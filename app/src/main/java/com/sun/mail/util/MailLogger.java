/*
 * Copyright (c) 2012, 2019 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.mail.util;

import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Session;

/**
 * A simplified logger used by Jakarta Mail to handle logging to a
 * PrintStream and logging through a java.util.logging.Logger.
 * If debug is set, messages are written to the PrintStream and
 * prefixed by the specified prefix (which is not included in
 * Logger messages).
 * Messages are logged by the Logger based on the configuration
 * of the logging system.
 */

/*
 * It would be so much simpler to just subclass Logger and override
 * the log(LogRecord) method, as the javadocs suggest, but that doesn't
 * work because Logger makes the decision about whether to log the message
 * or not before calling the log(LogRecord) method.  Instead, we just
 * provide the few log methods we need here.
 */

public final class MailLogger {
    /**
     * For log messages.
     */
    private final Logger logger;
    /**
     * For debug output.
     */
    private final String prefix;
    /**
     * Produce debug output?
     */
    private final boolean debug;
    /**
     * Stream for debug output.
     */
    private final PrintStream out;

    /**
     * Construct a new MailLogger using the specified Logger name,
     * debug prefix (e.g., "DEBUG"), debug flag, and PrintStream.
     *
     * @param	name	the Logger name
     * @param	prefix	the prefix for debug output, or null for none
     * @param	debug	if true, write to PrintStream
     * @param	out	the PrintStream to write to
     */
    public MailLogger(String name, String prefix, boolean debug,
				PrintStream out) {
	logger = Logger.getLogger(name);
	this.prefix = prefix;
	this.debug = debug;
	this.out = out != null ? out : System.out;
    }

    /**
     * Construct a new MailLogger using the specified class' package
     * name as the Logger name,
     * debug prefix (e.g., "DEBUG"), debug flag, and PrintStream.
     *
     * @param	clazz	the Logger name is the package name of this class
     * @param	prefix	the prefix for debug output, or null for none
     * @param	debug	if true, write to PrintStream
     * @param	out	the PrintStream to write to
     */
    public MailLogger(Class<?> clazz, String prefix, boolean debug,
				PrintStream out) {
	String name = packageOf(clazz);
	logger = Logger.getLogger(name);
	this.prefix = prefix;
	this.debug = debug;
	this.out = out != null ? out : System.out;
    }

    /**
     * Construct a new MailLogger using the specified class' package
     * name combined with the specified subname as the Logger name,
     * debug prefix (e.g., "DEBUG"), debug flag, and PrintStream.
     *
     * @param	clazz	the Logger name is the package name of this class
     * @param	subname	the Logger name relative to this Logger name
     * @param	prefix	the prefix for debug output, or null for none
     * @param	debug	if true, write to PrintStream
     * @param	out	the PrintStream to write to
     */
    public MailLogger(Class<?> clazz, String subname, String prefix, boolean debug,
				PrintStream out) {
	String name = packageOf(clazz) + "." + subname;
	logger = Logger.getLogger(name);
	this.prefix = prefix;
	this.debug = debug;
	this.out = out != null ? out : System.out;
    }

    /**
     * Construct a new MailLogger using the specified Logger name and
     * debug prefix (e.g., "DEBUG").  Get the debug flag and PrintStream
     * from the Session.
     *
     * @param	name	the Logger name
     * @param	prefix	the prefix for debug output, or null for none
     * @param	session	where to get the debug flag and PrintStream
     */
    @Deprecated
    public MailLogger(String name, String prefix, Session session) {
	this(name, prefix, session.getDebug(), session.getDebugOut());
    }

    /**
     * Construct a new MailLogger using the specified class' package
     * name as the Logger name and the specified
     * debug prefix (e.g., "DEBUG").  Get the debug flag and PrintStream
     * from the Session.
     *
     * @param	clazz	the Logger name is the package name of this class
     * @param	prefix	the prefix for debug output, or null for none
     * @param	session	where to get the debug flag and PrintStream
     */
    @Deprecated
    public MailLogger(Class<?> clazz, String prefix, Session session) {
	this(clazz, prefix, session.getDebug(), session.getDebugOut());
    }

    /**
     * Create a MailLogger that uses a Logger with the specified name
     * and prefix.  The new MailLogger uses the same debug flag and
     * PrintStream as this MailLogger.
     *
     * @param	name	the Logger name
     * @param	prefix	the prefix for debug output, or null for none
     * @return a MailLogger for the given name and prefix.
     */
    public MailLogger getLogger(String name, String prefix) {
	return new MailLogger(name, prefix, debug, out);
    }

    /**
     * Create a MailLogger using the specified class' package
     * name as the Logger name and the specified prefix.
     * The new MailLogger uses the same debug flag and
     * PrintStream as this MailLogger.
     *
     * @param	clazz	the Logger name is the package name of this class
     * @param	prefix	the prefix for debug output, or null for none
     * @return a MailLogger for the given name and prefix.
     */
    public MailLogger getLogger(Class<?> clazz, String prefix) {
	return new MailLogger(clazz, prefix, debug, out);
    }

    /**
     * Create a MailLogger that uses a Logger whose name is composed
     * of this MailLogger's name plus the specified sub-name, separated
     * by a dot.  The new MailLogger uses the new prefix for debug output.
     * This is used primarily by the protocol trace code that wants a
     * different prefix (none).
     *
     * @param	subname	the Logger name relative to this Logger name
     * @param	prefix	the prefix for debug output, or null for none
     * @return a MailLogger for the given name and prefix.
     */
    public MailLogger getSubLogger(String subname, String prefix) {
	return new MailLogger(logger.getName() + "." + subname, prefix,
				debug, out);
    }

    /**
     * Create a MailLogger that uses a Logger whose name is composed
     * of this MailLogger's name plus the specified sub-name, separated
     * by a dot.  The new MailLogger uses the new prefix for debug output.
     * This is used primarily by the protocol trace code that wants a
     * different prefix (none).
     *
     * @param	subname	the Logger name relative to this Logger name
     * @param	prefix	the prefix for debug output, or null for none
     * @param	debug	the debug flag for the sub-logger
     * @return a MailLogger for the given name and prefix.
     */
    public MailLogger getSubLogger(String subname, String prefix,
				boolean debug) {
	return new MailLogger(logger.getName() + "." + subname, prefix,
				debug, out);
    }

    /**
     * Log the message at the specified level.
     * @param level the log level.
     * @param msg the message.
     */
    public void log(Level level, String msg) {
	ifDebugOut(msg);
	if (logger.isLoggable(level)) {
	    final StackTraceElement frame = inferCaller();
	    logger.logp(level, frame.getClassName(), frame.getMethodName(), msg);
	}
    }

    /**
     * Log the message at the specified level.
     * @param level the log level.
     * @param msg the message.
     * @param param1 the additional parameter.
     */
    public void log(Level level, String msg, Object param1) {
	if (debug) {
	    msg = MessageFormat.format(msg, new Object[] { param1 });
	    debugOut(msg);
	}
	
	if (logger.isLoggable(level)) {
	    final StackTraceElement frame = inferCaller();
	    logger.logp(level, frame.getClassName(), frame.getMethodName(), msg, param1);
	}
    }

    /**
     * Log the message at the specified level.
     * @param level the log level.
     * @param msg the message.
     * @param params the message parameters.
     */
    public void log(Level level, String msg, Object... params) {
	if (debug) {
	    msg = MessageFormat.format(msg, params);
	    debugOut(msg);
	}
	
	if (logger.isLoggable(level)) {
	    final StackTraceElement frame = inferCaller();
	    logger.logp(level, frame.getClassName(), frame.getMethodName(), msg, params);
	}
    }

    /**
     * Log the message at the specified level using a format string.
     * @param level the log level.
     * @param msg the message format string.
     * @param params the message parameters.
     *
     * @since	JavaMail 1.5.4
     */
    public void logf(Level level, String msg, Object... params) {
	msg = String.format(msg, params);
	ifDebugOut(msg);
	logger.log(level, msg);
    }

    /**
     * Log the message at the specified level.
     * @param level the log level.
     * @param msg the message.
     * @param thrown the throwable to log.
     */
    public void log(Level level, String msg, Throwable thrown) {
	if (debug) {
	    if (thrown != null) {
		debugOut(msg + ", THROW: ");
		thrown.printStackTrace(out);
	    } else {
		debugOut(msg);
	    }
	}
 
	if (logger.isLoggable(level)) {
	    final StackTraceElement frame = inferCaller();
	    logger.logp(level, frame.getClassName(), frame.getMethodName(), msg, thrown);
	}
    }

    /**
     * Log a message at the CONFIG level.
     * @param msg the message.
     */
    public void config(String msg) {
	log(Level.CONFIG, msg);
    }

    /**
     * Log a message at the FINE level.
     * @param msg the message.
     */
    public void fine(String msg) {
	log(Level.FINE, msg);
    }

    /**
     * Log a message at the FINER level.
     * @param msg the message.
     */
    public void finer(String msg) {
	log(Level.FINER, msg);
    }

    /**
     * Log a message at the FINEST level.
     * @param msg the message.
     */
    public void finest(String msg) {
	log(Level.FINEST, msg);
    }

    /**
     * If "debug" is set, or our embedded Logger is loggable at the
     * given level, return true.
     * @param level the log level.
     * @return true if loggable.
     */
    public boolean isLoggable(Level level) {
	return debug || logger.isLoggable(level);
    }

    /**
     * Common code to conditionally log debug statements.
     * @param msg the message to log.
     */
    private void ifDebugOut(String msg) {
	if (debug)
	    debugOut(msg);
    }

    /**
     * Common formatting for debug output.
     * @param msg the message to log.
     */
    private void debugOut(String msg) {
	if (prefix != null)
	    out.println(prefix + ": " + msg);
	else
	    out.println(msg);
    }

    /**
     * Return the package name of the class.
     * Sometimes there will be no Package object for the class,
     * e.g., if the class loader hasn't created one (see Class.getPackage()).
     * @param clazz the class source.
     * @return the package name or an empty string.
     */
    private String packageOf(Class<?> clazz) {
	Package p = clazz.getPackage();
	if (p != null)
	    return p.getName();		// hopefully the common case
	String cname = clazz.getName();
	int i = cname.lastIndexOf('.');
	if (i > 0)
	    return cname.substring(0, i);
	// no package name, now what?
	return "";
    }

    /**
     * A disadvantage of not being able to use Logger directly in Jakarta Mail
     * code is that the "source class" information that Logger guesses will
     * always refer to this class instead of our caller.  This method
     * duplicates what Logger does to try to find *our* caller, so that
     * Logger doesn't have to do it (and get the wrong answer), and because
     * our caller is what's wanted.
     * @return StackTraceElement that logged the message.  Treat as read-only.
     */
    private StackTraceElement inferCaller() {
	// Get the stack trace.
	StackTraceElement stack[] = (new Throwable()).getStackTrace();
	// First, search back to a method in the Logger class.
	int ix = 0;
	while (ix < stack.length) {
	    StackTraceElement frame = stack[ix];
	    String cname = frame.getClassName();
	    if (isLoggerImplFrame(cname)) {
		break;
	    }
	    ix++;
	}
	// Now search for the first frame before the "Logger" class.
	while (ix < stack.length) {
	    StackTraceElement frame = stack[ix];
	    String cname = frame.getClassName();
	    if (!isLoggerImplFrame(cname)) {
		// We've found the relevant frame.
		return frame;
	    }
	    ix++;
	}
	// We haven't found a suitable frame, so just punt.  This is
	// OK as we are only committed to making a "best effort" here.
	return new StackTraceElement(MailLogger.class.getName(), "log",
                             MailLogger.class.getName(), -1);
    }
    
    /**
     * Frames to ignore as part of the MailLogger to JUL bridge.
     * @param cname the class name.
     * @return true if the class name is part of the MailLogger bridge.
     */
    private boolean isLoggerImplFrame(String cname) {
	return MailLogger.class.getName().equals(cname);
    }
}
