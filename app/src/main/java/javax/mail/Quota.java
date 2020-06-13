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
 * This class represents a set of quotas for a given quota root.
 * Each quota root has a set of resources, represented by the
 * <code>Quota.Resource</code> class.  Each resource has a name
 * (for example, "STORAGE"), a current usage, and a usage limit.
 * See RFC 2087.
 *
 * @since JavaMail 1.4
 * @author  Bill Shannon
 */

public class Quota {

    /**
     * An individual resource in a quota root.
     *
     * @since JavaMail 1.4
     */
    public static class Resource {
	/** The name of the resource. */
	public String name;
	/** The current usage of the resource. */
	public long usage;
	/** The usage limit for the resource. */
	public long limit;

	/**
	 * Construct a Resource object with the given name,
	 * usage, and limit.
	 *
	 * @param	name	the resource name
	 * @param	usage	the current usage of the resource
	 * @param	limit	the usage limit for the resource
	 */
	public Resource(String name, long usage, long limit) {
	    this.name = name;
	    this.usage = usage;
	    this.limit = limit;
	}
    }

    /**
     * The name of the quota root.
     */
    public String quotaRoot;

    /**
     * The set of resources associated with this quota root.
     */
    public Quota.Resource[] resources;

    /**
     * Create a Quota object for the named quotaroot with no associated
     * resources.
     *
     * @param	quotaRoot	the name of the quota root
     */
    public Quota(String quotaRoot) {
	this.quotaRoot = quotaRoot;
    }

    /**
     * Set a resource limit for this quota root.
     *
     * @param	name	the name of the resource
     * @param	limit	the resource limit
     */
    public void setResourceLimit(String name, long limit) {
	if (resources == null) {
	    resources = new Quota.Resource[1];
	    resources[0] = new Quota.Resource(name, 0, limit);
	    return;
	}
	for (int i = 0; i < resources.length; i++) {
	    if (resources[i].name.equalsIgnoreCase(name)) {
		resources[i].limit = limit;
		return;
	    }
	}
	Quota.Resource[] ra = new Quota.Resource[resources.length + 1];
	System.arraycopy(resources, 0, ra, 0, resources.length);
	ra[ra.length - 1] = new Quota.Resource(name, 0, limit);
	resources = ra;
    }
}
