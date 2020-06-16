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

package com.sun.mail.imap.protocol;

import java.util.*;
import com.sun.mail.iap.*;

/**
 * This class represents the response to the ID command. <p>
 *
 * See <A HREF="http://www.ietf.org/rfc/rfc2971.txt">RFC 2971</A>.
 *
 * @since JavaMail 1.5.1
 * @author Bill Shannon
 */

public class ID {

    private Map<String, String> serverParams = null;

    /**
     * Parse the server parameter list out of the response.
     *
     * @param	r	the response
     * @exception	ProtocolException	for protocol failures
     */
    public ID(Response r) throws ProtocolException {
	// id_response ::= "ID" SPACE id_params_list
	// id_params_list ::= "(" #(string SPACE nstring) ")" / nil
	//       ;; list of field value pairs

	r.skipSpaces();
	int c = r.peekByte();
	if (c == 'N' || c == 'n')	// assume NIL
	    return;

	if (c != '(')
	    throw new ProtocolException("Missing '(' at start of ID");

	serverParams = new HashMap<>();

	String[] v = r.readStringList();
	if (v != null) {
	    for (int i = 0; i < v.length; i += 2) {
		String name = v[i];
		if (name == null)
		    throw new ProtocolException("ID field name null");
		if (i + 1 >= v.length)
		    throw new ProtocolException("ID field without value: " +
									name);
		String value = v[i + 1];
		serverParams.put(name, value);
	    }
	}
	serverParams = Collections.unmodifiableMap(serverParams);
    }

    /**
     * Return the parsed server params.
     */
    Map<String, String> getServerParams() {
	return serverParams;
    }

    /**
     * Convert the client parameters into an argument list for the ID command.
     */
    static Argument getArgumentList(Map<String,String> clientParams) {
	Argument arg = new Argument();
	if (clientParams == null) {
	    arg.writeAtom("NIL");
	    return arg;
	}
	Argument list = new Argument();
	// add params to list
	for (Map.Entry<String, String> e : clientParams.entrySet()) {
	    list.writeNString(e.getKey());	// assume these are ASCII only
	    list.writeNString(e.getValue());
	}
	arg.writeArgument(list);
	return arg;
    }
}
