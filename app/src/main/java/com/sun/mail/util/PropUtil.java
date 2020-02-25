/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.util.*;
import javax.mail.Session;

/**
 * Utilities to make it easier to get property values.
 * Properties can be strings or type-specific value objects.
 *
 * @author Bill Shannon
 */
public class PropUtil {

    // No one should instantiate this class.
    private PropUtil() {
    }

    /**
     * Get an integer valued property.
     *
     * @param	props	the properties
     * @param	name	the property name
     * @param	def	default value if property not found
     * @return		the property value
     */
    public static int getIntProperty(Properties props, String name, int def) {
	return getInt(getProp(props, name), def);
    }

    /**
     * Get a boolean valued property.
     *
     * @param	props	the properties
     * @param	name	the property name
     * @param	def	default value if property not found
     * @return		the property value
     */
    public static boolean getBooleanProperty(Properties props,
				String name, boolean def) {
	return getBoolean(getProp(props, name), def);
    }

    /**
     * Get an integer valued property.
     *
     * @param	session	the Session
     * @param	name	the property name
     * @param	def	default value if property not found
     * @return		the property value
     */
    @Deprecated
    public static int getIntSessionProperty(Session session,
				String name, int def) {
	return getInt(getProp(session.getProperties(), name), def);
    }

    /**
     * Get a boolean valued property.
     *
     * @param	session	the Session
     * @param	name	the property name
     * @param	def	default value if property not found
     * @return		the property value
     */
    @Deprecated
    public static boolean getBooleanSessionProperty(Session session,
				String name, boolean def) {
	return getBoolean(getProp(session.getProperties(), name), def);
    }

    /**
     * Get a boolean valued System property.
     *
     * @param	name	the property name
     * @param	def	default value if property not found
     * @return		the property value
     */
    public static boolean getBooleanSystemProperty(String name, boolean def) {
	try {
	    return getBoolean(getProp(System.getProperties(), name), def);
	} catch (SecurityException sex) {
	    // fall through...
	}

	/*
	 * If we can't get the entire System Properties object because
	 * of a SecurityException, just ask for the specific property.
	 */
	try {
	    String value = System.getProperty(name);
	    if (value == null)
		return def;
	    if (def)
		return !value.equalsIgnoreCase("false");
	    else
		return value.equalsIgnoreCase("true");
	} catch (SecurityException sex) {
	    return def;
	}
    }

    /**
     * Get the value of the specified property.
     * If the "get" method returns null, use the getProperty method,
     * which might cascade to a default Properties object.
     */
    private static Object getProp(Properties props, String name) {
	Object val = props.get(name);
	if (val != null)
	    return val;
	else
	    return props.getProperty(name);
    }

    /**
     * Interpret the value object as an integer,
     * returning def if unable.
     */
    private static int getInt(Object value, int def) {
	if (value == null)
	    return def;
	if (value instanceof String) {
	    try {
		String s = (String)value;
		if (s.startsWith("0x"))
		    return Integer.parseInt(s.substring(2), 16);
		else
		    return Integer.parseInt(s);
	    } catch (NumberFormatException nfex) { }
	}
	if (value instanceof Integer)
	    return ((Integer)value).intValue();
	return def;
    }

    /**
     * Interpret the value object as a boolean,
     * returning def if unable.
     */
    private static boolean getBoolean(Object value, boolean def) {
	if (value == null)
	    return def;
	if (value instanceof String) {
	    /*
	     * If the default is true, only "false" turns it off.
	     * If the default is false, only "true" turns it on.
	     */
	    if (def)
		return !((String)value).equalsIgnoreCase("false");
	    else
		return ((String)value).equalsIgnoreCase("true");
	}
	if (value instanceof Boolean)
	    return ((Boolean)value).booleanValue();
	return def;
    }
}
