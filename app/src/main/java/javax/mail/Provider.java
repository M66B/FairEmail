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

package javax.mail;

/**
 * The Provider is a class that describes a protocol 
 * implementation.  The values typically come from the
 * javamail.providers and javamail.default.providers
 * resource files.  An application may also create and
 * register a Provider object to dynamically add support
 * for a new provider.
 *
 * @author Max Spivak
 * @author Bill Shannon
 */
public class Provider {

    /**
     * This inner class defines the Provider type.
     * Currently, STORE and TRANSPORT are the only two provider types 
     * supported.
     */

    public static class Type {
	public static final Type STORE     = new Type("STORE");
	public static final Type TRANSPORT = new Type("TRANSPORT");

	private String type;

	private Type(String type) {
	    this.type = type;
	}

	@Override
	public String toString() {
	    return type;
	}
    }

    private Type type;
    private String protocol, className, vendor, version;

    /**
     * Create a new provider of the specified type for the specified
     * protocol.  The specified class implements the provider.
     *
     * @param type      Type.STORE or Type.TRANSPORT
     * @param protocol  valid protocol for the type
     * @param classname class name that implements this protocol
     * @param vendor    optional string identifying the vendor (may be null)
     * @param version   optional implementation version string (may be null)
     * @since JavaMail 1.4
     */
    public Provider(Type type, String protocol, String classname, 
	     String vendor, String version) {
	this.type = type;
	this.protocol = protocol;
	this.className = classname;
	this.vendor = vendor;
	this.version = version;
    }

    /**
     * Returns the type of this Provider.
     *
     * @return	the provider type
     */
    public Type getType() {
	return type;
    }

    /**
     * Returns the protocol supported by this Provider.
     *
     * @return	the protocol
     */
    public String getProtocol() {
	return protocol;
    }

    /**
     * Returns the name of the class that implements the protocol.
     *
     * @return	the class name
     */
    public String getClassName() {
	return className;
    }

    /**
     * Returns the name of the vendor associated with this implementation
     * or null.
     *
     * @return	the vendor
     */
    public String getVendor() {
	return vendor;
    }

    /**
     * Returns the version of this implementation or null if no version.
     *
     * @return	the version
     */
    public String getVersion() {
	return version;
    }

    /** Overrides Object.toString() */
    @Override
    public String toString() {
	String s = "javax.mail.Provider[" + type + "," +
		    protocol + "," + className;

	if (vendor != null)
	    s += "," + vendor;

	if (version != null)
	    s += "," + version;

	s += "]";
	return s;
    }
}
