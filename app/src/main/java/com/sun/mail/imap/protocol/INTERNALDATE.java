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

import java.util.Date;
import java.util.TimeZone;
import java.util.Locale;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.text.FieldPosition;

import javax.mail.internet.MailDateFormat;

import com.sun.mail.iap.*; 


/**
 * An INTERNALDATE FETCH item.
 *
 * @author  John Mani
 */

public class INTERNALDATE implements Item {

    static final char[] name =
	{'I','N','T','E','R','N','A','L','D','A','T','E'};
    public int msgno;
    protected Date date;

    /*
     * Used to parse dates only.  The parse method is thread safe
     * so we only need to create a single object for use by all
     * instances.  We depend on the fact that the MailDateFormat
     * class will parse dates in INTERNALDATE format as well as
     * dates in RFC 822 format.
     */
    private static final MailDateFormat mailDateFormat = new MailDateFormat();

    /**
     * Constructor.
     *
     * @param	r	the FetchResponse
     * @exception	ParsingException	for parsing failures
     */
    public INTERNALDATE(FetchResponse r) throws ParsingException {
	msgno = r.getNumber();
	r.skipSpaces();
	String s = r.readString();
	if (s == null)
	    throw new ParsingException("INTERNALDATE is NIL");
	try {
        synchronized (mailDateFormat) {
            date = mailDateFormat.parse(s);
        }
	} catch (ParseException pex) {
	    throw new ParsingException("INTERNALDATE parse error");
	}
    }

    public Date getDate() {
	return date;
    }

    // INTERNALDATE formatter

    private static SimpleDateFormat df = 
	// Need Locale.US, the "MMM" field can produce unexpected values
	// in non US locales !
	new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss ", Locale.US);

    /**
     * Format given Date object into INTERNALDATE string
     *
     * @param	d	the Date
     * @return		INTERNALDATE string
     */
    public static String format(Date d) {
	/*
	 * SimpleDateFormat objects aren't thread safe, so rather
	 * than create a separate such object for each request,
	 * we create one object and synchronize its use here
	 * so that only one thread is using it at a time.  This
	 * trades off some potential concurrency for speed in the
	 * common case.
	 *
	 * This method is only used when formatting the date in a
	 * message that's being appended to a folder.
	 */
	StringBuffer sb = new StringBuffer();
	synchronized (df) {
	    df.format(d, sb, new FieldPosition(0));
	}

	// compute timezone offset string
	TimeZone tz = TimeZone.getDefault();
	int offset = tz.getOffset(d.getTime());	// get offset from GMT
	int rawOffsetInMins = offset / 60 / 1000; // offset from GMT in mins
	if (rawOffsetInMins < 0) {
	    sb.append('-');
	    rawOffsetInMins = (-rawOffsetInMins);
	} else
	    sb.append('+');
	
	int offsetInHrs = rawOffsetInMins / 60;
	int offsetInMins = rawOffsetInMins % 60;

	sb.append(Character.forDigit((offsetInHrs/10), 10));
	sb.append(Character.forDigit((offsetInHrs%10), 10));
	sb.append(Character.forDigit((offsetInMins/10), 10));
	sb.append(Character.forDigit((offsetInMins%10), 10));

	return sb.toString();
    }
}
