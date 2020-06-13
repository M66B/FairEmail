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

package javax.mail.search;

import java.io.IOException;
import javax.mail.*;

/**
 * This class implements searches on a message body.
 * All parts of the message that are of MIME type "text/*" are searched.
 * The pattern is a simple string that must appear as a substring in
 * the message body.
 * 
 * @author Bill Shannon
 * @author John Mani
 */
public final class BodyTerm extends StringTerm {

    private static final long serialVersionUID = -4888862527916911385L;

    /**
     * Constructor
     * @param pattern	The String to search for
     */
    public BodyTerm(String pattern) {
	// Note: comparison is case-insensitive
	super(pattern);
    }

    /**
     * The match method.
     *
     * @param msg	The pattern search is applied on this Message's body
     * @return		true if the pattern is found; otherwise false 
     */
    @Override
    public boolean match(Message msg) {
	return matchPart(msg);
    }

    /**
     * Search all the parts of the message for any text part
     * that matches the pattern.
     */
    private boolean matchPart(Part p) {
	try {
	    /*
	     * Using isMimeType to determine the content type avoids
	     * fetching the actual content data until we need it.
	     */
	    if (p.isMimeType("text/*")) {
		String s = (String)p.getContent();
		if (s == null)
		    return false;
		/*
		 * We invoke our superclass' (i.e., StringTerm) match method.
		 * Note however that StringTerm.match() is not optimized 
		 * for substring searches in large string buffers. We really
		 * need to have a StringTerm subclass, say BigStringTerm, 
		 * with its own match() method that uses a better algorithm ..
		 * and then subclass BodyTerm from BigStringTerm.
		 */ 
		return super.match(s);
	    } else if (p.isMimeType("multipart/*")) {
		Multipart mp = (Multipart)p.getContent();
		int count = mp.getCount();
		for (int i = 0; i < count; i++)
		    if (matchPart(mp.getBodyPart(i)))
			return true;
	    } else if (p.isMimeType("message/rfc822")) {
		return matchPart((Part)p.getContent());
	    }
	} catch (MessagingException ex) {
	} catch (IOException ex) {
	} catch (RuntimeException ex) {
	}
	return false;
    }

    /**
     * Equality comparison.
     */
    @Override
    public boolean equals(Object obj) {
	if (!(obj instanceof BodyTerm))
	    return false;
	return super.equals(obj);
    }
}
