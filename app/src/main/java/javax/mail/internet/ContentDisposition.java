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
import com.sun.mail.util.PropUtil;

/**
 * This class represents a MIME ContentDisposition value. It provides
 * methods to parse a ContentDisposition string into individual components
 * and to generate a MIME style ContentDisposition string.
 *
 * @author  John Mani
 */

public class ContentDisposition {

    private static final boolean contentDispositionStrict =
        PropUtil.getBooleanSystemProperty("mail.mime.contentdisposition.strict", true);

    private String disposition; // disposition
    private ParameterList list;	// parameter list

    /**
     * No-arg Constructor.
     */
    public ContentDisposition() { }

    /**
     * Constructor.
     *
     * @param	disposition	disposition
     * @param	list	ParameterList
     * @since		JavaMail 1.2
     */
    public ContentDisposition(String disposition, ParameterList list) {
	this.disposition = disposition;
	this.list = list;
    }

    /**
     * Constructor that takes a ContentDisposition string. The String
     * is parsed into its constituents: dispostion and parameters. 
     * A ParseException is thrown if the parse fails. 
     *
     * @param	s	the ContentDisposition string.
     * @exception	ParseException if the parse fails.
     * @since		JavaMail 1.2
     */
    public ContentDisposition(String s) throws ParseException {
	HeaderTokenizer h = new HeaderTokenizer(s, HeaderTokenizer.MIME);
	HeaderTokenizer.Token tk;

	// First "disposition" ..
	tk = h.next();
	if (tk.getType() != HeaderTokenizer.Token.ATOM) {
            if (contentDispositionStrict) {
	        throw new ParseException("Expected disposition, got " +
				    tk.getValue());
            }
        } else {
	    disposition = tk.getValue();
        }

	// Then parameters ..
	String rem = h.getRemainder();
	if (rem != null) {
            try {
                list = new ParameterList(rem);
            } catch (ParseException px) {
                if (contentDispositionStrict) {
                    throw px;
                }
            }
        }
    }

    /**
     * Return the disposition value.
     * @return the disposition
     * @since		JavaMail 1.2
     */
    public String getDisposition() {
	return disposition;
    }

    /**
     * Return the specified parameter value. Returns <code>null</code>
     * if this parameter is absent.
     *
     * @param	name	the parameter name
     * @return	parameter value
     * @since		JavaMail 1.2
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
     * @since		JavaMail 1.2
     */
    public ParameterList getParameterList() {
	return list;
    }

    /**
     * Set the disposition.  Replaces the existing disposition.
     * @param	disposition	the disposition
     * @since		JavaMail 1.2
     */
    public void setDisposition(String disposition) {
	this.disposition = disposition;
    }

    /**
     * Set the specified parameter. If this parameter already exists,
     * it is replaced by this new value.
     *
     * @param	name	parameter name
     * @param	value	parameter value
     * @since		JavaMail 1.2
     */
    public void setParameter(String name, String value) {
	if (list == null)
	    list = new ParameterList();

	list.set(name, value);
    }

    /**
     * Set a new ParameterList.
     * @param	list	ParameterList
     * @since		JavaMail 1.2
     */
    public void setParameterList(ParameterList list) {
	this.list = list;
    }

    /**
     * Retrieve a RFC2045 style string representation of
     * this ContentDisposition. Returns an empty string if
     * the conversion failed.
     *
     * @return	RFC2045 style string
     * @since		JavaMail 1.2
     */
    @Override
    public String toString() {
	if (disposition == null)
	    return "";

	if (list == null)
	    return disposition;

	StringBuilder sb = new StringBuilder(disposition);

        // append the parameter list  
        // use the length of the string buffer + the length of 
        // the header name formatted as follows "Content-Disposition: "
	sb.append(list.toString(sb.length() + 21));
	return sb.toString();
    }
}
