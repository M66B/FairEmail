/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package javax.activation;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * The CommandInfo class is used by CommandMap implementations to
 * describe the results of command requests. It provides the requestor
 * with both the verb requested, as well as an instance of the
 * bean. There is also a method that will return the name of the
 * class that implements the command but <i>it is not guaranteed to
 * return a valid value</i>. The reason for this is to allow CommandMap
 * implmentations that subclass CommandInfo to provide special
 * behavior. For example a CommandMap could dynamically generate
 * JavaBeans. In this case, it might not be possible to create an
 * object with all the correct state information solely from the class
 * name.
 */

public class CommandInfo {
    private String verb;
    private String className;

    /**
     * The Constructor for CommandInfo.
     * @param verb The command verb this CommandInfo decribes.
     * @param className The command's fully qualified class name.
     */
    public CommandInfo(String verb, String className) {
	this.verb = verb;
	this.className = className;
    }

    /**
     * Return the command verb.
     *
     * @return the command verb.
     */
    public String getCommandName() {
	return verb;
    }

    /**
     * Return the command's class name. <i>This method MAY return null in
     * cases where a CommandMap subclassed CommandInfo for its
     * own purposes.</i> In other words, it might not be possible to
     * create the correct state in the command by merely knowing
     * its class name. <b>DO NOT DEPEND ON THIS METHOD RETURNING
     * A VALID VALUE!</b>
     *
     * @return The class name of the command, or <i>null</i>
     */
    public String getCommandClass() {
	return className;
    }

    /**
     * Return the instantiated JavaBean component.
     * <p>
     * If the current runtime environment supports
     * {@link java.beans.Beans#instantiate Beans.instantiate},
     * use it to instantiate the JavaBeans component.  Otherwise, use
     * {@link java.lang.Class#forName Class.forName}.
     * <p>
     * The component class needs to be public.
     * On Java SE 9 and newer, if the component class is in a named module,
     * it needs to be in an exported package.
     * <p>
     * If the bean implements the <code>javax.activation.CommandObject</code>
     * interface, call its <code>setCommandContext</code> method.
     * <p>
     * If the DataHandler parameter is null, then the bean is
     * instantiated with no data. NOTE: this may be useful
     * if for some reason the DataHandler that is passed in
     * throws IOExceptions when this method attempts to
     * access its InputStream. It will allow the caller to
     * retrieve a reference to the bean if it can be
     * instantiated.
     * <p>
     * If the bean does NOT implement the CommandObject interface,
     * this method will check if it implements the
     * java.io.Externalizable interface. If it does, the bean's
     * readExternal method will be called if an InputStream
     * can be acquired from the DataHandler.<p>
     *
     * @param dh	The DataHandler that describes the data to be
     *			passed to the command.
     * @param loader	The ClassLoader to be used to instantiate the bean.
     * @return The bean
     * @exception	IOException	for failures reading data
     * @exception	ClassNotFoundException	if command object class can't
     *						be found
     * @see java.beans.Beans#instantiate
     * @see javax.activation.CommandObject
     */
    public Object getCommandObject(DataHandler dh, ClassLoader loader)
			throws IOException, ClassNotFoundException {
	Object new_bean = null;

	// try to instantiate the bean
	new_bean = Beans.instantiate(loader, className);

	// if we got one and it is a CommandObject
	if (new_bean != null) {
	    if (new_bean instanceof CommandObject) {
		((CommandObject)new_bean).setCommandContext(verb, dh);
	    } else if (new_bean instanceof Externalizable) {
		if (dh != null) {
		    InputStream is = dh.getInputStream();
		    if (is != null) {
			((Externalizable)new_bean).readExternal(
					       new ObjectInputStream(is));
		    }
		}
	    }
	}

	return new_bean;
    }

    /**
     * Helper class to invoke Beans.instantiate reflectively or the equivalent
     * with core reflection when module java.desktop is not readable.
     */
    private static final class Beans {
        static final Method instantiateMethod;

        static {
            Method m;
            try {
                Class<?> c = Class.forName("java.beans.Beans");
                m = c.getDeclaredMethod("instantiate", ClassLoader.class, String.class);
            } catch (ClassNotFoundException e) {
                m = null;
            } catch (NoSuchMethodException e) {
                m = null;
            }
            instantiateMethod = m;
        }

        /**
         * Equivalent to invoking java.beans.Beans.instantiate(loader, cn)
         */
        static Object instantiate(ClassLoader loader, String cn)
                throws IOException, ClassNotFoundException {

            Exception exception;

            if (instantiateMethod != null) {

                // invoke Beans.instantiate
                try {
                    return instantiateMethod.invoke(null, loader, cn);
                } catch (InvocationTargetException e) {
                    exception = e;
                } catch (IllegalAccessException e) {
                    exception = e;
                }

            } else {

		SecurityManager security = System.getSecurityManager();
		if (security != null) {
		    // if it's ok with the SecurityManager, it's ok with me.
		    String cname = cn.replace('/', '.');
		    if (cname.startsWith("[")) {
			int b = cname.lastIndexOf('[') + 2;
			if (b > 1 && b < cname.length()) {
			    cname = cname.substring(b);
			}
		    }
		    int i = cname.lastIndexOf('.');
		    if (i != -1) {
			security.checkPackageAccess(cname.substring(0, i));
		    }
		}

                // Beans.instantiate specified to use SCL when loader is null
                if (loader == null) {
                    loader = (ClassLoader)
		        AccessController.doPrivileged(new PrivilegedAction() {
			    public Object run() {
				ClassLoader cl = null;
				try {
				    cl = ClassLoader.getSystemClassLoader();
				} catch (SecurityException ex) { }
				return cl;
			    }
			});
                }
                Class<?> beanClass = Class.forName(cn, true, loader);
                try {
                    return beanClass.newInstance();
                } catch (Exception ex) {
                    throw new ClassNotFoundException(beanClass + ": " + ex, ex);
                }

            }
            return null;
        }
    }
}
