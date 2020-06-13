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


import javax.mail.*;
import javax.mail.internet.*;

import com.sun.mail.imap.protocol.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class 
 *
 * @author  John Mani
 */

public class IMAPMultipartDataSource extends MimePartDataSource
				     implements MultipartDataSource {
    private List<IMAPBodyPart> parts;

    protected IMAPMultipartDataSource(MimePart part, BODYSTRUCTURE[] bs, 
				      String sectionId, IMAPMessage msg) {
	super(part);

	parts = new ArrayList<>(bs.length);
	for (int i = 0; i < bs.length; i++)
	    parts.add(
		new IMAPBodyPart(bs[i], 
				 sectionId == null ? 
				   Integer.toString(i+1) : 
				   sectionId + "." + Integer.toString(i+1),
				 msg)
	    );
    }

    @Override
    public int getCount() {
	return parts.size();
    }

    @Override
    public BodyPart getBodyPart(int index) throws MessagingException {
	return parts.get(index);
    }
}
