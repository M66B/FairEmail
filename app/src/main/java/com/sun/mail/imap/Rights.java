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

package com.sun.mail.imap;

import java.util.*;

/**
 * The Rights class represents the set of rights for an authentication
 * identifier (for instance, a user or a group). <p>
 *
 * A right is represented by the <code>Rights.Right</code> 
 * inner class. <p>
 *
 * A set of standard rights are predefined (see RFC 2086).  Most folder
 * implementations are expected to support these rights.  Some
 * implementations may also support site-defined rights. <p>
 *
 * The following code sample illustrates how to examine your
 * rights for a folder.
 * <pre>
 *
 * Rights rights = folder.myRights();
 *
 * // Check if I can write this folder
 * if (rights.contains(Rights.Right.WRITE))
 *	System.out.println("Can write folder");
 *
 * // Now give Joe all my rights, except the ability to write the folder
 * rights.remove(Rights.Right.WRITE);
 * ACL acl = new ACL("joe", rights);
 * folder.setACL(acl);
 * </pre>
 * <p>
 *
 * @author Bill Shannon
 */

public class Rights implements Cloneable {

    private boolean[] rights = new boolean[128];	// XXX

    /**
     * This inner class represents an individual right. A set
     * of standard rights objects are predefined here.
     */
    public static final class Right {
	private static Right[] cache = new Right[128];

	// XXX - initialization order?
	/**
	 * Lookup - mailbox is visible to LIST/LSUB commands.
	 */
	public static final Right LOOKUP = getInstance('l');

	/**
	 * Read - SELECT the mailbox, perform CHECK, FETCH, PARTIAL,
	 * SEARCH, COPY from mailbox
	 */
	public static final Right READ = getInstance('r');

	/**
	 * Keep seen/unseen information across sessions - STORE \SEEN flag.
	 */
	public static final Right KEEP_SEEN = getInstance('s');

	/**
	 * Write - STORE flags other than \SEEN and \DELETED.
	 */
	public static final Right WRITE = getInstance('w');

	/**
	 * Insert - perform APPEND, COPY into mailbox.
	 */
	public static final Right INSERT = getInstance('i');

	/**
	 * Post - send mail to submission address for mailbox,
	 * not enforced by IMAP4 itself.
	 */
	public static final Right POST = getInstance('p');

	/**
	 * Create - CREATE new sub-mailboxes in any implementation-defined
	 * hierarchy, RENAME or DELETE mailbox.
	 */
	public static final Right CREATE = getInstance('c');

	/**
	 * Delete - STORE \DELETED flag, perform EXPUNGE.
	 */
	public static final Right DELETE = getInstance('d');

	/**
	 * Administer - perform SETACL.
	 */
	public static final Right ADMINISTER = getInstance('a');

	char right;	// the right represented by this Right object

	/**
	 * Private constructor used only by getInstance.
	 */
	private Right(char right) {
	    if ((int)right >= 128)
		throw new IllegalArgumentException("Right must be ASCII");
	    this.right = right;
	}

	/**
	 * Get a Right object representing the specified character.
	 * Characters are assigned per RFC 2086.
	 *
	 * @param	right	the character representing the right
	 * @return		the Right object
	 */
	public static synchronized Right getInstance(char right) {
	    if ((int)right >= 128)
		throw new IllegalArgumentException("Right must be ASCII");
	    if (cache[(int)right] == null)
		cache[(int)right] = new Right(right);
	    return cache[(int)right];
	}

	@Override
	public String toString() {
	    return String.valueOf(right);
	}
    }


    /**
     * Construct an empty Rights object.
     */
    public Rights() { }

    /**
     * Construct a Rights object initialized with the given rights.
     *
     * @param rights	the rights for initialization
     */
    public Rights(Rights rights) {
	System.arraycopy(rights.rights, 0, this.rights, 0, this.rights.length);
    }

    /**
     * Construct a Rights object initialized with the given rights.
     *
     * @param rights	the rights for initialization
     */
    public Rights(String rights) {
	for (int i = 0; i < rights.length(); i++)
	    add(Right.getInstance(rights.charAt(i)));
    }

    /**
     * Construct a Rights object initialized with the given right.
     *
     * @param right	the right for initialization
     */
    public Rights(Right right) {
	this.rights[(int)right.right] = true;
    }

    /**
     * Add the specified right to this Rights object.
     *
     * @param right	the right to add
     */
    public void add(Right right) {
	this.rights[(int)right.right] = true;
    }

    /**
     * Add all the rights in the given Rights object to this
     * Rights object.
     *
     * @param rights	Rights object
     */
    public void add(Rights rights) {
	for (int i = 0; i < rights.rights.length; i++)
	    if (rights.rights[i])
		this.rights[i] = true;
    }

    /**
     * Remove the specified right from this Rights object.
     *
     * @param	right 	the right to be removed
     */
    public void remove(Right right) {
	this.rights[(int)right.right] = false;
    }

    /**
     * Remove all rights in the given Rights object from this 
     * Rights object.
     *
     * @param	rights 	the rights to be removed
     */
    public void remove(Rights rights) {
	for (int i = 0; i < rights.rights.length; i++)
	    if (rights.rights[i])
		this.rights[i] = false;
    }

    /**
     * Check whether the specified right is present in this Rights object.
     *
     * @param	right	the Right to check
     * @return 		true of the given right is present, otherwise false.
     */
    public boolean contains(Right right) {
	return this.rights[(int)right.right];
    }

    /**
     * Check whether all the rights in the specified Rights object are
     * present in this Rights object.
     *
     * @param	rights	the Rights to check
     * @return	true if all rights in the given Rights object are present, 
     *		otherwise false.
     */
    public boolean contains(Rights rights) {
	for (int i = 0; i < rights.rights.length; i++)
	    if (rights.rights[i] && !this.rights[i])
		return false;

	// If we've made it till here, return true
	return true;
    }

    /**
     * Check whether the two Rights objects are equal.
     *
     * @return	true if they're equal
     */
    @Override
    public boolean equals(Object obj) {
	if (!(obj instanceof Rights))
	    return false;

	Rights rights = (Rights)obj;

	for (int i = 0; i < rights.rights.length; i++)
	    if (rights.rights[i] != this.rights[i])
		return false;

	return true;
    }

    /**
     * Compute a hash code for this Rights object.
     *
     * @return	the hash code
     */
    @Override
    public int hashCode() {
	int hash = 0;
	for (int i = 0; i < this.rights.length; i++)
	    if (this.rights[i])
		hash++;
	return hash;
    }

    /**
     * Return all the rights in this Rights object.  Returns
     * an array of size zero if no rights are set.
     *
     * @return	array of Rights.Right objects representing rights
     */
    public Right[] getRights() {
	List<Right> v = new ArrayList<>();
	for (int i = 0; i < this.rights.length; i++)
	    if (this.rights[i])
		v.add(Right.getInstance((char)i));
	return v.toArray(new Right[v.size()]);
    }

    /**
     * Returns a clone of this Rights object.
     */
    @Override
    public Object clone() {
	Rights r = null;
	try {
	    r = (Rights)super.clone();
	    r.rights = new boolean[128];
	    System.arraycopy(this.rights, 0, r.rights, 0, this.rights.length);
	} catch (CloneNotSupportedException cex) {
	    // ignore, can't happen
	}
	return r;
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();
	for (int i = 0; i < this.rights.length; i++)
	    if (this.rights[i])
		sb.append((char)i);
	return sb.toString();
    }

    /*****
    public static void main(String argv[]) throws Exception {
	// a new rights object
	Rights f1 = new Rights();
	f1.add(Rights.Right.READ);
	f1.add(Rights.Right.WRITE);
	f1.add(Rights.Right.CREATE);
	f1.add(Rights.Right.DELETE);

	// check copy constructor
	Rights fc = new Rights(f1);
	if (f1.equals(fc) && fc.equals(f1))
	    System.out.println("success");
	else
	    System.out.println("fail");

	// check clone
	fc = (Rights)f1.clone();
	if (f1.equals(fc) && fc.equals(f1))
	    System.out.println("success");
	else
	    System.out.println("fail");

	// add a right and make sure it still works right
	f1.add(Rights.Right.ADMINISTER);

	// shouldn't be equal here
	if (!f1.equals(fc) && !fc.equals(f1))
	    System.out.println("success");
	else
	    System.out.println("fail");

	// check clone
	fc = (Rights)f1.clone();
	if (f1.equals(fc) && fc.equals(f1))
	    System.out.println("success");
	else
	    System.out.println("fail");

	fc.add(Rights.Right.INSERT);
	if (!f1.equals(fc) && !fc.equals(f1))
	    System.out.println("success");
	else
	    System.out.println("fail");

	// check copy constructor
	fc = new Rights(f1);
	if (f1.equals(fc) && fc.equals(f1))
	    System.out.println("success");
	else
	    System.out.println("fail");

	// another new rights object
	Rights f2 = new Rights(Rights.Right.READ);
	f2.add(Rights.Right.WRITE);

	if (f1.contains(Rights.Right.READ))
	    System.out.println("success");
	else
	    System.out.println("fail");
		
	if (f1.contains(Rights.Right.WRITE))
	    System.out.println("success");
	else
	    System.out.println("fail");

	if (f1.contains(Rights.Right.CREATE))
	    System.out.println("success");
	else
	    System.out.println("fail");

	if (f1.contains(Rights.Right.DELETE))
	    System.out.println("success");
	else
	    System.out.println("fail");

	if (f2.contains(Rights.Right.WRITE))
	    System.out.println("success");
	else
	    System.out.println("fail");


	System.out.println("----------------");

	Right[] r = f1.getRights();
	for (int i = 0; i < r.length; i++)
	    System.out.println(r[i]);
	System.out.println("----------------");

	if (f1.contains(f2)) // this should be true
	    System.out.println("success");
	else
	    System.out.println("fail");

	if (!f2.contains(f1)) // this should be false
	    System.out.println("success");
	else
	    System.out.println("fail");

	Rights f3 = new Rights();
	f3.add(Rights.Right.READ);
	f3.add(Rights.Right.WRITE);
	f3.add(Rights.Right.CREATE);
	f3.add(Rights.Right.DELETE);
	f3.add(Rights.Right.ADMINISTER);
	f3.add(Rights.Right.LOOKUP);

	f1.add(Rights.Right.LOOKUP);

	if (f1.equals(f3))
	    System.out.println("equals success");
	else
	    System.out.println("fail");
	if (f3.equals(f1))
	    System.out.println("equals success");
	else
	    System.out.println("fail");
	System.out.println("f1 hash code " + f1.hashCode());
	System.out.println("f3 hash code " + f3.hashCode());
	if (f1.hashCode() == f3.hashCode())
	    System.out.println("success");
	else
	    System.out.println("fail");
    }
    ****/
}
