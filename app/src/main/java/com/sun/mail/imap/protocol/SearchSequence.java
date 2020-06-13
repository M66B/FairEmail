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
import java.io.IOException;

import javax.mail.*;
import javax.mail.search.*;
import com.sun.mail.iap.*;
import com.sun.mail.imap.OlderTerm;
import com.sun.mail.imap.YoungerTerm;
import com.sun.mail.imap.ModifiedSinceTerm;

/**
 * This class traverses a search-tree and generates the 
 * corresponding IMAP search sequence. 
 *
 * Each IMAPProtocol instance contains an instance of this class,
 * which might be subclassed by subclasses of IMAPProtocol to add
 * support for additional product-specific search terms.
 *
 * @author	John Mani
 * @author	Bill Shannon
 */
public class SearchSequence {

    private IMAPProtocol protocol;	// for hasCapability checks; may be null

    /**
     * Create a SearchSequence for this IMAPProtocol.
     *
     * @param	p	the IMAPProtocol object for the server
     * @since	JavaMail 1.6.0
     */
    public SearchSequence(IMAPProtocol p) {
	protocol = p;
    }

    /**
     * Create a SearchSequence.
     */
    @Deprecated
    public SearchSequence() {
    }

    /**
     * Generate the IMAP search sequence for the given search expression. 
     *
     * @param	term	the search term
     * @param	charset	charset for the search
     * @return		the SEARCH Argument
     * @exception	SearchException	for failures
     * @exception	IOException	for I/O errors
     */
    public Argument generateSequence(SearchTerm term, String charset) 
		throws SearchException, IOException {
	/*
	 * Call the appropriate handler depending on the type of
	 * the search-term ...
	 */
	if (term instanceof AndTerm) 		// AND
	    return and((AndTerm)term, charset);
	else if (term instanceof OrTerm) 	// OR
	    return or((OrTerm)term, charset);
	else if (term instanceof NotTerm) 	// NOT
	    return not((NotTerm)term, charset);
	else if (term instanceof HeaderTerm) 	// HEADER
	    return header((HeaderTerm)term, charset);
	else if (term instanceof FlagTerm) 	// FLAG
	    return flag((FlagTerm)term);
	else if (term instanceof FromTerm) {	// FROM
	    FromTerm fterm = (FromTerm)term;
	    return from(fterm.getAddress().toString(), charset);
	}
	else if (term instanceof FromStringTerm) { // FROM
	    FromStringTerm fterm = (FromStringTerm)term;
	    return from(fterm.getPattern(), charset);
	}
	else if (term instanceof RecipientTerm)	{ // RECIPIENT
	    RecipientTerm rterm = (RecipientTerm)term;
	    return recipient(rterm.getRecipientType(), 
			     rterm.getAddress().toString(),
			     charset);
	}
	else if (term instanceof RecipientStringTerm) { // RECIPIENT
	    RecipientStringTerm rterm = (RecipientStringTerm)term;
	    return recipient(rterm.getRecipientType(),
			     rterm.getPattern(),
			     charset);
	}
	else if (term instanceof SubjectTerm)	// SUBJECT
	    return subject((SubjectTerm)term, charset);
	else if (term instanceof BodyTerm)	// BODY
	    return body((BodyTerm)term, charset);
	else if (term instanceof SizeTerm)	// SIZE
	    return size((SizeTerm)term);
	else if (term instanceof SentDateTerm)	// SENTDATE
	    return sentdate((SentDateTerm)term);
	else if (term instanceof ReceivedDateTerm) // INTERNALDATE
	    return receiveddate((ReceivedDateTerm)term);
	else if (term instanceof OlderTerm)	// RFC 5032 OLDER
	    return older((OlderTerm)term);
	else if (term instanceof YoungerTerm)	// RFC 5032 YOUNGER
	    return younger((YoungerTerm)term);
	else if (term instanceof MessageIDTerm) // MessageID
	    return messageid((MessageIDTerm)term, charset);
	else if (term instanceof ModifiedSinceTerm)	// RFC 4551 MODSEQ
	    return modifiedSince((ModifiedSinceTerm)term);
	else
	    throw new SearchException("Search too complex");
    }

    /**
     * Check if the "text" terms in the given SearchTerm contain
     * non US-ASCII characters.
     *
     * @param	term	the search term
     * @return		true if only ASCII
     */
    public static boolean isAscii(SearchTerm term) {
	if (term instanceof AndTerm)
	    return isAscii(((AndTerm)term).getTerms());
	else if (term instanceof OrTerm)
	    return isAscii(((OrTerm)term).getTerms());
	else if (term instanceof NotTerm)
	    return isAscii(((NotTerm)term).getTerm());
	else if (term instanceof StringTerm)
	    return isAscii(((StringTerm)term).getPattern());
	else if (term instanceof AddressTerm)
	    return isAscii(((AddressTerm)term).getAddress().toString());
	
	// Any other term returns true.
	return true;
    }

    /**
     * Check if any of the "text" terms in the given SearchTerms contain
     * non US-ASCII characters.
     *
     * @param	terms	the search terms
     * @return		true if only ASCII
     */
    public static boolean isAscii(SearchTerm[] terms) {
	for (int i = 0; i < terms.length; i++)
	    if (!isAscii(terms[i])) // outta here !
		return false;
	return true;
    }

    /**
     * Does this string contain only ASCII characters?
     *
     * @param	s	the string
     * @return		true if only ASCII
     */
    public static boolean isAscii(String s) {
	int l = s.length();

	for (int i=0; i < l; i++) {
	    if ((int)s.charAt(i) > 0177) // non-ascii
		return false;
	}
	return true;
    }

    protected Argument and(AndTerm term, String charset) 
			throws SearchException, IOException {
	// Combine the sequences for both terms
	SearchTerm[] terms = term.getTerms();
	// Generate the search sequence for the first term
	Argument result = generateSequence(terms[0], charset);
	// Append other terms
	for (int i = 1; i < terms.length; i++)
	    result.append(generateSequence(terms[i], charset));
	return result;
    }

    protected Argument or(OrTerm term, String charset) 
			throws SearchException, IOException {
	SearchTerm[] terms = term.getTerms();

	/* The IMAP OR operator takes only two operands. So if
	 * we have more than 2 operands, group them into 2-operand
	 * OR Terms.
	 */
	if (terms.length > 2) {
	    SearchTerm t = terms[0];

	    // Include rest of the terms
	    for (int i = 1; i < terms.length; i++)
		t = new OrTerm(t, terms[i]);

	    term = (OrTerm)t; 	// set 'term' to the new jumbo OrTerm we
				// just created
	    terms = term.getTerms();
	}

	// 'term' now has only two operands
	Argument result = new Argument();

	// Add the OR search-key, if more than one term
	if (terms.length > 1)
	    result.writeAtom("OR");

	/* If this term is an AND expression, we need to enclose it
	 * within paranthesis.
	 *
	 * AND expressions are either AndTerms or FlagTerms 
	 */
	if (terms[0] instanceof AndTerm || terms[0] instanceof FlagTerm)
	    result.writeArgument(generateSequence(terms[0], charset));
	else
	    result.append(generateSequence(terms[0], charset));

	// Repeat the above for the second term, if there is one
	if (terms.length > 1) {
	    if (terms[1] instanceof AndTerm || terms[1] instanceof FlagTerm)
		result.writeArgument(generateSequence(terms[1], charset));
	    else
		result.append(generateSequence(terms[1], charset));
	}

	return result;
    }

    protected Argument not(NotTerm term, String charset) 
			throws SearchException, IOException {
	Argument result = new Argument();

	// Add the NOT search-key
	result.writeAtom("NOT");

	/* If this term is an AND expression, we need to enclose it
	 * within paranthesis. 
	 *
	 * AND expressions are either AndTerms or FlagTerms 
	 */
	SearchTerm nterm = term.getTerm();
	if (nterm instanceof AndTerm || nterm instanceof FlagTerm)
	    result.writeArgument(generateSequence(nterm, charset));
	else
	    result.append(generateSequence(nterm, charset));

	return result;
    }

    protected Argument header(HeaderTerm term, String charset) 
			throws SearchException, IOException {
	Argument result = new Argument();
	result.writeAtom("HEADER");
	result.writeString(term.getHeaderName());
	result.writeString(term.getPattern(), charset);
	return result;
    }

    protected Argument messageid(MessageIDTerm term, String charset) 
			throws SearchException, IOException {
	Argument result = new Argument();
	result.writeAtom("HEADER");
	result.writeString("Message-ID");
	// XXX confirm that charset conversion ought to be done
	result.writeString(term.getPattern(), charset); 
	return result;
    }

    protected Argument flag(FlagTerm term) throws SearchException {
	boolean set = term.getTestSet();

	Argument result = new Argument();

	Flags flags = term.getFlags();
	Flags.Flag[] sf = flags.getSystemFlags();
	String[] uf = flags.getUserFlags();
	if (sf.length == 0 && uf.length == 0)
	    throw new SearchException("Invalid FlagTerm");

	for (int i = 0; i < sf.length; i++) {
	    if (sf[i] == Flags.Flag.DELETED)
		result.writeAtom(set ? "DELETED": "UNDELETED");
	    else if (sf[i] == Flags.Flag.ANSWERED)
		result.writeAtom(set ? "ANSWERED": "UNANSWERED");
	    else if (sf[i] == Flags.Flag.DRAFT)
		result.writeAtom(set ? "DRAFT": "UNDRAFT");
	    else if (sf[i] == Flags.Flag.FLAGGED)
		result.writeAtom(set ? "FLAGGED": "UNFLAGGED");
	    else if (sf[i] == Flags.Flag.RECENT)
		result.writeAtom(set ? "RECENT": "OLD");
	    else if (sf[i] == Flags.Flag.SEEN)
		result.writeAtom(set ? "SEEN": "UNSEEN");
	}

	for (int i = 0; i < uf.length; i++) {
	    result.writeAtom(set ? "KEYWORD" : "UNKEYWORD");
	    result.writeAtom(uf[i]);
	}
	
	return result;
    }

    protected Argument from(String address, String charset) 
			throws SearchException, IOException {
	Argument result = new Argument();
	result.writeAtom("FROM");
	result.writeString(address, charset);
	return result;
    }

    protected Argument recipient(Message.RecipientType type,
				      String address, String charset)
			throws SearchException, IOException {
	Argument result = new Argument();

	if (type == Message.RecipientType.TO)
	    result.writeAtom("TO");
	else if (type == Message.RecipientType.CC)
	    result.writeAtom("CC");
	else if (type == Message.RecipientType.BCC)
	    result.writeAtom("BCC");
	else
	    throw new SearchException("Illegal Recipient type");

	result.writeString(address, charset);
	return result;
    }

    protected Argument subject(SubjectTerm term, String charset) 
			throws SearchException, IOException {
	Argument result = new Argument();
	
	result.writeAtom("SUBJECT");
	result.writeString(term.getPattern(), charset);
	return result;
    }

    protected Argument body(BodyTerm term, String charset) 
			throws SearchException, IOException {
	Argument result = new Argument();

	result.writeAtom("BODY");
	result.writeString(term.getPattern(), charset);
	return result;
    }

    protected Argument size(SizeTerm term) 
			throws SearchException {
	Argument result = new Argument();

	switch (term.getComparison()) {
	    case ComparisonTerm.GT:
		result.writeAtom("LARGER");
		break;
	    case ComparisonTerm.LT:
		result.writeAtom("SMALLER");
		break;
	    default:
		// GT and LT is all we get from IMAP for size
	    	throw new SearchException("Cannot handle Comparison");
	}

	result.writeNumber(term.getNumber());
	return result;
    }

    // Date SEARCH stuff ...

    // NOTE: The built-in IMAP date comparisons are equivalent to
    //       "<" (BEFORE), "=" (ON), and ">=" (SINCE)!!!
    //       There is no built-in greater-than comparison!

    /**
     * Print an IMAP Date string, that is suitable for the Date
     * SEARCH commands.
     *
     * The IMAP Date string is :
     *	date ::= date_day "-" date_month "-" date_year	
     *
     * Note that this format does not contain the TimeZone
     */
    private static String monthTable[] = { 
	  "Jan", "Feb", "Mar", "Apr", "May", "Jun",
	  "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };

    // A GregorianCalendar object in the current timezone
    protected Calendar cal = new GregorianCalendar();

    protected String toIMAPDate(Date date) {
	StringBuilder s = new StringBuilder();

	cal.setTime(date);

	s.append(cal.get(Calendar.DATE)).append("-");
	s.append(monthTable[cal.get(Calendar.MONTH)]).append('-');
	s.append(cal.get(Calendar.YEAR));

	return s.toString();
    }

    protected Argument sentdate(DateTerm term) 
			throws SearchException {
	Argument result = new Argument();
	String date = toIMAPDate(term.getDate());

	switch (term.getComparison()) {
	    case ComparisonTerm.GT:
		result.writeAtom("NOT SENTON " + date + " SENTSINCE " + date);
		break;
	    case ComparisonTerm.EQ:
		result.writeAtom("SENTON " + date);
		break;
	    case ComparisonTerm.LT:
		result.writeAtom("SENTBEFORE " + date);
		break;
	    case ComparisonTerm.GE:
		result.writeAtom("SENTSINCE " + date);
		break;
	    case ComparisonTerm.LE:
		result.writeAtom("OR SENTBEFORE " + date + " SENTON " + date);
		break;
	    case ComparisonTerm.NE:
		result.writeAtom("NOT SENTON " + date);
		break;
	    default:
	    	throw new SearchException("Cannot handle Date Comparison");
	}

	return result;
    }

    protected Argument receiveddate(DateTerm term) 
			throws SearchException {
	Argument result = new Argument();
	String date = toIMAPDate(term.getDate());

	switch (term.getComparison()) {
	    case ComparisonTerm.GT:
		result.writeAtom("NOT ON " + date + " SINCE " + date);
		break;
	    case ComparisonTerm.EQ:
		result.writeAtom("ON " + date);
		break;
	    case ComparisonTerm.LT:
		result.writeAtom("BEFORE " + date);
		break;
	    case ComparisonTerm.GE:
		result.writeAtom("SINCE " + date);
		break;
	    case ComparisonTerm.LE:
		result.writeAtom("OR BEFORE " + date + " ON " + date);
		break;
	    case ComparisonTerm.NE:
		result.writeAtom("NOT ON " + date);
		break;
	    default:
	    	throw new SearchException("Cannot handle Date Comparison");
	}

	return result;
    }

    /**
     * Generate argument for OlderTerm.
     *
     * @param	term	the search term
     * @return		the SEARCH Argument
     * @exception	SearchException	for failures
     * @since	JavaMail 1.5.1
     */
    protected Argument older(OlderTerm term) throws SearchException {
	if (protocol != null && !protocol.hasCapability("WITHIN"))
	    throw new SearchException("Server doesn't support OLDER searches");
	Argument result = new Argument();
	result.writeAtom("OLDER");
	result.writeNumber(term.getInterval());
	return result;
    }

    /**
     * Generate argument for YoungerTerm.
     *
     * @param	term	the search term
     * @return		the SEARCH Argument
     * @exception	SearchException	for failures
     * @since	JavaMail 1.5.1
     */
    protected Argument younger(YoungerTerm term) throws SearchException {
	if (protocol != null && !protocol.hasCapability("WITHIN"))
	    throw new SearchException("Server doesn't support YOUNGER searches");
	Argument result = new Argument();
	result.writeAtom("YOUNGER");
	result.writeNumber(term.getInterval());
	return result;
    }

    /**
     * Generate argument for ModifiedSinceTerm.
     *
     * @param	term	the search term
     * @return		the SEARCH Argument
     * @exception	SearchException	for failures
     * @since	JavaMail 1.5.1
     */
    protected Argument modifiedSince(ModifiedSinceTerm term)
				throws SearchException {
	if (protocol != null && !protocol.hasCapability("CONDSTORE"))
	    throw new SearchException("Server doesn't support MODSEQ searches");
	Argument result = new Argument();
	result.writeAtom("MODSEQ");
	result.writeNumber(term.getModSeq());
	return result;
    }
}
