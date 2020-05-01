/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.mail.gimap;

import java.io.IOException;

import javax.mail.*;

import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.ListInfo;
import com.sun.mail.gimap.protocol.GmailProtocol;

/**
 * A Gmail Store.  Defaults to imap.gmail.com with SSL.
 * Uses a GmailProtocol and Gmail Folder to support Gmail extensions.
 *
 * @since JavaMail 1.4.6
 * @author Bill Shannon
 */

public class GmailStore extends IMAPStore {
    /**
     * Constructor that takes a Session object and a URLName that
     * represents a specific IMAP server.
     *
     * @param	session	the Session
     * @param	url	the URLName of this store
     */
    public GmailStore(Session session, URLName url) {
	this(session, url, "gimap", true);
    }

    /**
     * Constructor used by GmailSSLStore subclass.
     *
     * @param	session	the Session
     * @param	url	the URLName of this store
     * @param	name	the protocol name
     * @param	isSSL	use SSL to connect?
     */
    protected GmailStore(Session session, URLName url,
                                String name, boolean isSSL) {
	super(session, url, name, true);	// Gmail requires SSL
    }

    protected boolean protocolConnect(String host, int pport,
				String user, String password)
				throws MessagingException {
	if (host == null)
	    host = "imap.gmail.com";		// default to Gmail host
	return super.protocolConnect(host, pport, user, password);
    }

    protected IMAPProtocol newIMAPProtocol(String host, int port)
				throws IOException, ProtocolException {
	return new GmailProtocol(name, host, port, 
					    session.getProperties(),
					    isSSL,
					    logger
					   );
    }

    /**
     * Create an IMAPFolder object.
     */
    protected IMAPFolder newIMAPFolder(String fullName, char separator,
				Boolean isNamespace) {
	return new GmailFolder(fullName, separator, this, isNamespace);
    }

    /**
     * Create an IMAPFolder object.
     */
    protected IMAPFolder newIMAPFolder(ListInfo li) {
	return new GmailFolder(li, this);
    }
}
