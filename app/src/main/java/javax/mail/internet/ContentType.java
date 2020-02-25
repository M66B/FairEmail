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

package javax.mail.internet;

import javax.mail.*;
import java.util.*;
import java.io.*;

/**
 * This class represents a MIME Content-Type value. It provides
 * methods to parse a Content-Type string into individual components
 * and to generate a MIME style Content-Type string.
 *
 * @author  John Mani
 */

public class ContentType {

    private String primaryType;	// primary type
    private String subType;	// subtype
    private ParameterList list;	// parameter list

    /**
     * No-arg Constructor.
     */
    public ContentType() { }

    /**
     * Constructor.
     *
     * @param	primaryType	primary type
     * @param	subType	subType
     * @param	list	ParameterList
     */
    public ContentType(String primaryType, String subType, 
			ParameterList list) {
	this.primaryType = primaryType;
	this.subType = subType;
	this.list = list;
    }

    /**
     * Constructor that takes a Content-Type string. The String
     * is parsed into its constituents: primaryType, subType
     * and parameters. A ParseException is thrown if the parse fails. 
     *
     * @param	s	the Content-Type string.
     * @exception	ParseException if the parse fails.
     */
    public ContentType(String s) throws ParseException {
	HeaderTokenizer h = new HeaderTokenizer(s, HeaderTokenizer.MIME);
	HeaderTokenizer.Token tk;

	// First "type" ..
	tk = h.next();
	if (tk.getType() != HeaderTokenizer.Token.ATOM)
	    throw new ParseException("In Content-Type string <" + s + ">" +
					", expected MIME type, got " +
					tk.getValue());
	primaryType = tk.getValue();

	// The '/' separator ..
	tk = h.next();
	if ((char)tk.getType() != '/')
	    throw new ParseException("In Content-Type string <" + s + ">" +
				", expected '/', got " + tk.getValue());

	// Then "subType" ..
	tk = h.next();
	if (tk.getType() != HeaderTokenizer.Token.ATOM)
	    throw new ParseException("In Content-Type string <" + s + ">" +
					", expected MIME subtype, got " +
					tk.getValue());
	subType = tk.getValue();

	// Finally parameters ..
	String rem = h.getRemainder();
	if (rem != null)
	    list = new ParameterList(rem);
    }

    /**
     * Return the primary type.
     * @return the primary type
     */
    public String getPrimaryType() {
	return primaryType;
    }

    /**
     * Return the subType.
     * @return the subType
     */
    public String getSubType() {
	return subType;
    }

    /**
     * Return the MIME type string, without the parameters.
     * The returned value is basically the concatenation of
     * the primaryType, the '/' character and the secondaryType.
     *
     * @return the type
     */
    public String getBaseType() {
	if (primaryType == null || subType == null)
	    return "";
	return primaryType + '/' + subType;
    }

    /**
     * Return the specified parameter value. Returns <code>null</code>
     * if this parameter is absent.
     *
     * @param	name	the parameter name
     * @return	parameter value
     */
    public String getParameter(String name) {
	if (list == null)
	    return null;

	return list.get(name);
    }

    /**
     * Return a ParameterList object that holds all the available 
     * parameters. Returns null if no parameters are available.
     *
     * @return	ParameterList
     */
    public ParameterList getParameterList() {
	return list;
    }

    /**
     * Set the primary type. Overrides existing primary type.
     * @param	primaryType	primary type
     */
    public void setPrimaryType(String primaryType) {
	this.primaryType = primaryType;
    }

    /**
     * Set the subType.  Replaces the existing subType.
     * @param	subType	the subType
     */
    public void setSubType(String subType) {
	this.subType = subType;
    }

    /**
     * Set the specified parameter. If this parameter already exists,
     * it is replaced by this new value.
     *
     * @param	name	parameter name
     * @param	value	parameter value
     */
    public void setParameter(String name, String value) {
	if (list == null)
	    list = new ParameterList();

	list.set(name, value);
    }

    /**
     * Set a new ParameterList.
     * @param	list	ParameterList
     */
    public void setParameterList(ParameterList list) {
	this.list = list;
    }

    /**
     * Retrieve a RFC2045 style string representation of
     * this Content-Type. Returns an empty string if
     * the conversion failed.
     *
     * @return	RFC2045 style string
     */
    @Override
    public String toString() {
	if (primaryType == null || subType == null) // need both
	    return "";

	StringBuilder sb = new StringBuilder();
	sb.append(primaryType).append('/').append(subType);
	if (list != null)
            // append the parameter list 
            // use the length of the string buffer + the length of
            // the header name formatted as follows "Content-Type: "
	    sb.append(list.toString(sb.length() + 14));
	
	return sb.toString();
    }

    /**
     * Match with the specified ContentType object. This method
     * compares <strong>only the <code>primaryType</code> and 
     * <code>subType</code> </strong>. The parameters of both operands
     * are ignored. <p>
     *
     * For example, this method will return <code>true</code> when
     * comparing the ContentTypes for <strong>"text/plain"</strong>
     * and <strong>"text/plain; charset=foobar"</strong>.
     *
     * If the <code>subType</code> of either operand is the special
     * character '*', then the subtype is ignored during the match. 
     * For example, this method will return <code>true</code> when 
     * comparing the ContentTypes for <strong>"text/plain"</strong> 
     * and <strong>"text/*" </strong>
     *
     * @param   cType	ContentType to compare this against
     * @return	true if it matches
     */
    public boolean match(ContentType cType) {
	// Match primaryType
	if (!((primaryType == null && cType.getPrimaryType() == null) ||
		(primaryType != null &&
		    primaryType.equalsIgnoreCase(cType.getPrimaryType()))))
	    return false;
	
	String sType = cType.getSubType();

	// If either one of the subTypes is wildcarded, return true
	if ((subType != null && subType.startsWith("*")) ||
	    (sType != null && sType.startsWith("*")))
	    return true;
	
	// Match subType
	return (subType == null && sType == null) ||
	    (subType != null && subType.equalsIgnoreCase(sType));
    }

    /**
     * Match with the specified content-type string. This method
     * compares <strong>only the <code>primaryType</code> and 
     * <code>subType</code> </strong>.
     * The parameters of both operands are ignored. <p>
     *
     * For example, this method will return <code>true</code> when
     * comparing the ContentType for <strong>"text/plain"</strong>
     * with <strong>"text/plain; charset=foobar"</strong>.
     *
     * If the <code>subType</code> of either operand is the special 
     * character '*', then the subtype is ignored during the match. 
     * For example, this method will return <code>true</code> when 
     * comparing the ContentType for <strong>"text/plain"</strong> 
     * with <strong>"text/*" </strong>
     *
     * @param	s	the content-type string to match
     * @return	true if it matches
     */
    public boolean match(String s) {
	try {
	    return match(new ContentType(s));
	} catch (ParseException pex) {
	    return false;
	}
    }
}
