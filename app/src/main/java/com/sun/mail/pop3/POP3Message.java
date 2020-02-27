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

package com.sun.mail.pop3;

import java.io.*;
import java.util.Enumeration;
import java.util.logging.Level;
import java.lang.ref.SoftReference;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.event.*;
import com.sun.mail.util.ReadableMime;

/**
 * A POP3 Message.  Just like a MimeMessage except that
 * some things are not supported.
 *
 * @author      Bill Shannon
 */
public class POP3Message extends MimeMessage implements ReadableMime {

    /*
     * Our locking strategy is to always lock the POP3Folder before the
     * POP3Message so we have to be careful to drop our lock before calling
     * back to the folder to close it and notify of connection lost events.
     */

    // flag to indicate we haven't tried to fetch the UID yet
    static final String UNKNOWN = "UNKNOWN";

    private POP3Folder folder;	// overrides folder in MimeMessage
    private int hdrSize = -1;
    private int msgSize = -1;
    String uid = UNKNOWN;	// controlled by folder lock

    // rawData itself is never null
    private SoftReference<InputStream> rawData
	    = new SoftReference<>(null);

    public POP3Message(Folder folder, int msgno)
			throws MessagingException {
	super(folder, msgno);
	assert folder instanceof POP3Folder;
	this.folder = (POP3Folder)folder;
    }

    /**
     * Set the specified flags on this message to the specified value.
     *
     * @param newFlags	the flags to be set
     * @param set	the value to be set
     */
    @Override
    public synchronized void setFlags(Flags newFlags, boolean set)
				throws MessagingException {
	Flags oldFlags = (Flags)flags.clone();
	super.setFlags(newFlags, set);
	if (!flags.equals(oldFlags))
	    folder.notifyMessageChangedListeners(
				MessageChangedEvent.FLAGS_CHANGED, this);
    }

    /**
     * Return the size of the content of this message in bytes. 
     * Returns -1 if the size cannot be determined. <p>
     *
     * Note that this number may not be an exact measure of the
     * content size and may or may not account for any transfer
     * encoding of the content. <p>
     *
     * @return          size of content in bytes
     * @exception	MessagingException for failures
     */  
    @Override
    public int getSize() throws MessagingException {
	try {
	    synchronized (this) {
		// if we already have the size, return it
		if (msgSize > 0)
		    return msgSize;
	    }

	    /*
	     * Use LIST to determine the entire message
	     * size and subtract out the header size
	     * (which may involve loading the headers,
	     * which may load the content as a side effect).
	     * If the content is loaded as a side effect of
	     * loading the headers, it will set the size.
	     *
	     * Make sure to call loadHeaders() outside of the
	     * synchronization block.  There's a potential race
	     * condition here but synchronization will occur in
	     * loadHeaders() to make sure the headers are only
	     * loaded once, and again in the following block to
	     * only compute msgSize once.
	     */
	    if (headers == null)
		loadHeaders();

	    synchronized (this) {
		if (msgSize < 0)
		    msgSize = folder.getProtocol().list(msgnum) - hdrSize;
		return msgSize;
	    }
	} catch (EOFException eex) {
	    folder.close(false);
	    throw new FolderClosedException(folder, eex.toString());
	} catch (IOException ex) {
	    throw new MessagingException("error getting size", ex);
	}
    }

    /**
     * Produce the raw bytes of the message.  The data is fetched using
     * the POP3 RETR command.  If skipHeader is true, just the content
     * is returned.
     */
    private InputStream getRawStream(boolean skipHeader)
				throws MessagingException {
	InputStream rawcontent = null;
	try {
	synchronized(this) {
	    rawcontent = rawData.get();
	    if (rawcontent == null) {
		TempFile cache = folder.getFileCache();
		if (cache != null) {
		    if (folder.logger.isLoggable(Level.FINE))
			folder.logger.fine("caching message #" + msgnum +
					    " in temp file");
		    AppendStream os = cache.getAppendStream();
		    BufferedOutputStream bos = new BufferedOutputStream(os);
		    try {
			folder.getProtocol().retr(msgnum, bos);
		    } finally {
			bos.close();
		    }
		    rawcontent = os.getInputStream();
		} else {
		    rawcontent = folder.getProtocol().retr(msgnum,
					msgSize > 0 ? msgSize + hdrSize : 0);
		}
		if (rawcontent == null) {
		    expunged = true;
		    throw new MessageRemovedException(
			"can't retrieve message #" + msgnum +
			" in POP3Message.getContentStream"); // XXX - what else?
		}

		if (headers == null ||
			((POP3Store)(folder.getStore())).forgetTopHeaders) {
		    headers = new InternetHeaders(rawcontent);
		    hdrSize =
			(int)((SharedInputStream)rawcontent).getPosition();
		} else {
		    /*
		     * Already have the headers, have to skip the headers
		     * in the content array and return the body.
		     *
		     * XXX - It seems that some mail servers return slightly
		     * different headers in the RETR results than were returned
		     * in the TOP results, so we can't depend on remembering
		     * the size of the headers from the TOP command and just
		     * skipping that many bytes.  Instead, we have to process
		     * the content, skipping over the header until we come to
		     * the empty line that separates the header from the body.
		     */
		    int offset = 0;
		    for (;;) {
			int len = 0;	// number of bytes in this line
			int c1;
			while ((c1 = rawcontent.read()) >= 0) {
			    if (c1 == '\n')	// end of line
				break;
			    else if (c1 == '\r') {
				// got CR, is the next char LF?
				if (rawcontent.available() > 0) {
				    rawcontent.mark(1);
				    if (rawcontent.read() != '\n')
					rawcontent.reset();
				}
				break;	// in any case, end of line
			    }

			    // not CR, NL, or CRLF, count the byte
			    len++;
			}
			// here when end of line or out of data

			// if out of data, we're done
			if (rawcontent.available() == 0)
			    break;
			
			// if it was an empty line, we're done
			if (len == 0)
			    break;
		    }
		    hdrSize =
			(int)((SharedInputStream)rawcontent).getPosition();
		}

		// skipped the header, the message is what's left
		msgSize = rawcontent.available();

		rawData = new SoftReference<>(rawcontent);
	    }
	}
	} catch (EOFException eex) {
	    folder.close(false);
	    throw new FolderClosedException(folder, eex.toString());
	} catch (IOException ex) {
	    throw new MessagingException("error fetching POP3 content", ex);
	}

	/*
	 * We have a cached stream, but we need to return
	 * a fresh stream to read from the beginning and
	 * that can be safely closed.
	 */
	rawcontent = ((SharedInputStream)rawcontent).newStream(
						skipHeader ? hdrSize : 0, -1);
	return rawcontent;
    }

    /**
     * Produce the raw bytes of the content.  The data is fetched using
     * the POP3 RETR command.
     *
     * @see #contentStream
     */
    @Override
    protected synchronized InputStream getContentStream()
				throws MessagingException {
	if (contentStream != null)
	    return ((SharedInputStream)contentStream).newStream(0, -1);

	InputStream cstream = getRawStream(true);

	/*
	 * Keep a hard reference to the data if we're using a file
	 * cache or if the "mail.pop3.keepmessagecontent" prop is set.
	 */
	TempFile cache = folder.getFileCache();
	if (cache != null ||
		((POP3Store)(folder.getStore())).keepMessageContent)
	    contentStream = ((SharedInputStream)cstream).newStream(0, -1);
	return cstream;
    }

    /**
     * Return the MIME format stream corresponding to this message part.
     *
     * @return	the MIME format stream
     * @since	JavaMail 1.4.5
     */
    @Override
    public InputStream getMimeStream() throws MessagingException {
	return getRawStream(false);
    }

    /**
     * Invalidate the cache of content for this message object, causing 
     * it to be fetched again from the server the next time it is needed.
     * If <code>invalidateHeaders</code> is true, invalidate the headers
     * as well.
     *
     * @param	invalidateHeaders	invalidate the headers as well?
     */
    public synchronized void invalidate(boolean invalidateHeaders) {
	content = null;
	InputStream rstream = rawData.get();
	if (rstream != null) {
	    // note that if the content is in the file cache, it will be lost
	    // and fetched from the server if it's needed again
	    try {
		rstream.close();
	    } catch (IOException ex) {
		// ignore it
	    }
	    rawData = new SoftReference<>(null);
	}
	if (contentStream != null) {
	    try {
		contentStream.close();
	    } catch (IOException ex) {
		// ignore it
	    }
	    contentStream = null;
	}
	msgSize = -1;
	if (invalidateHeaders) {
	    headers = null;
	    hdrSize = -1;
	}
    }

    /**
     * Fetch the header of the message and the first <code>n</code> lines
     * of the raw content of the message.  The headers and data are
     * available in the returned InputStream.
     *
     * @param	n	number of lines of content to fetch
     * @return	InputStream containing the message headers and n content lines
     * @exception	MessagingException for failures
     */
    public InputStream top(int n) throws MessagingException {
	try {
	    synchronized (this) {
		return folder.getProtocol().top(msgnum, n);
	    }
	} catch (EOFException eex) {
	    folder.close(false);
	    throw new FolderClosedException(folder, eex.toString());
	} catch (IOException ex) {
	    throw new MessagingException("error getting size", ex);
	}
    }

    /**
     * Get all the headers for this header_name. Note that certain
     * headers may be encoded as per RFC 2047 if they contain 
     * non US-ASCII characters and these should be decoded. <p>
     *
     * @param	name	name of header
     * @return	array of headers
     * @exception	MessagingException for failures
     * @see 	javax.mail.internet.MimeUtility
     */
    @Override
    public String[] getHeader(String name)
			throws MessagingException {
	if (headers == null)
	    loadHeaders();
	return headers.getHeader(name);
    }

    /**
     * Get all the headers for this header name, returned as a single
     * String, with headers separated by the delimiter. If the
     * delimiter is <code>null</code>, only the first header is 
     * returned.
     *
     * @param name		the name of this header
     * @param delimiter		delimiter between returned headers
     * @return                  the value fields for all headers with 
     *				this name
     * @exception		MessagingException for failures
     */
    @Override
    public String getHeader(String name, String delimiter)
				throws MessagingException {
	if (headers == null)
	    loadHeaders();
	return headers.getHeader(name, delimiter);
    }

    /**
     * Set the value for this header_name.  Throws IllegalWriteException
     * because POP3 messages are read-only.
     *
     * @param	name 	header name
     * @param	value	header value
     * @see 	javax.mail.internet.MimeUtility
     * @exception	IllegalWriteException because the underlying
     *			implementation does not support modification
     * @exception	IllegalStateException if this message is
     *			obtained from a READ_ONLY folder.
     * @exception	MessagingException for other failures
     */
    @Override
    public void setHeader(String name, String value)
                                throws MessagingException {
	// XXX - should check for read-only folder?
	throw new IllegalWriteException("POP3 messages are read-only");
    }

    /**
     * Add this value to the existing values for this header_name.
     * Throws IllegalWriteException because POP3 messages are read-only.
     *
     * @param	name 	header name
     * @param	value	header value
     * @see 	javax.mail.internet.MimeUtility
     * @exception	IllegalWriteException because the underlying
     *			implementation does not support modification
     * @exception	IllegalStateException if this message is
     *			obtained from a READ_ONLY folder.
     */
    @Override
    public void addHeader(String name, String value)
                                throws MessagingException {
	// XXX - should check for read-only folder?
	throw new IllegalWriteException("POP3 messages are read-only");
    }

    /**
     * Remove all headers with this name.
     * Throws IllegalWriteException because POP3 messages are read-only.
     *
     * @exception	IllegalWriteException because the underlying
     *			implementation does not support modification
     * @exception	IllegalStateException if this message is
     *			obtained from a READ_ONLY folder.
     */
    @Override
    public void removeHeader(String name)
                                throws MessagingException {
	// XXX - should check for read-only folder?
	throw new IllegalWriteException("POP3 messages are read-only");
    }

    /**
     * Return all the headers from this Message as an enumeration
     * of Header objects. <p>
     *
     * Note that certain headers may be encoded as per RFC 2047 
     * if they contain non US-ASCII characters and these should 
     * be decoded. <p>
     *
     * @return	array of header objects
     * @exception	MessagingException for failures
     * @see 	javax.mail.internet.MimeUtility
     */
    @Override
    public Enumeration<Header> getAllHeaders() throws MessagingException {
	if (headers == null)
	    loadHeaders();
	return headers.getAllHeaders();	
    }

    /**
     * Return matching headers from this Message as an Enumeration of
     * Header objects.
     *
     * @exception	MessagingException for failures
     */
    @Override
    public Enumeration<Header> getMatchingHeaders(String[] names)
			throws MessagingException {
	if (headers == null)
	    loadHeaders();
	return headers.getMatchingHeaders(names);
    }

    /**
     * Return non-matching headers from this Message as an
     * Enumeration of Header objects.
     *
     * @exception	MessagingException for failures
     */
    @Override
    public Enumeration<Header> getNonMatchingHeaders(String[] names)
			throws MessagingException {
	if (headers == null)
	    loadHeaders();
	return headers.getNonMatchingHeaders(names);
    }

    /**
     * Add a raw RFC822 header-line. 
     * Throws IllegalWriteException because POP3 messages are read-only.
     *
     * @exception	IllegalWriteException because the underlying
     *			implementation does not support modification
     * @exception	IllegalStateException if this message is
     *			obtained from a READ_ONLY folder.
     */
    @Override
    public void addHeaderLine(String line) throws MessagingException {
	// XXX - should check for read-only folder?
	throw new IllegalWriteException("POP3 messages are read-only");
    }

    /**
     * Get all header lines as an Enumeration of Strings. A Header
     * line is a raw RFC822 header-line, containing both the "name" 
     * and "value" field. 
     *
     * @exception	MessagingException for failures
     */
    @Override
    public Enumeration<String> getAllHeaderLines() throws MessagingException {
	if (headers == null)
	    loadHeaders();
	return headers.getAllHeaderLines();
    }

    /**
     * Get matching header lines as an Enumeration of Strings. 
     * A Header line is a raw RFC822 header-line, containing both 
     * the "name" and "value" field.
     *
     * @exception	MessagingException for failures
     */
    @Override
    public Enumeration<String> getMatchingHeaderLines(String[] names)
                                        throws MessagingException {
	if (headers == null)
	    loadHeaders();
	return headers.getMatchingHeaderLines(names);
    }

    /**
     * Get non-matching header lines as an Enumeration of Strings. 
     * A Header line is a raw RFC822 header-line, containing both 
     * the "name" and "value" field.
     *
     * @exception	MessagingException for failures
     */
    @Override
    public Enumeration<String> getNonMatchingHeaderLines(String[] names)
                                        throws MessagingException {
	if (headers == null)
	    loadHeaders();
	return headers.getNonMatchingHeaderLines(names);
    }

    /**
     * POP3 message can't be changed.  This method throws
     * IllegalWriteException.
     *
     * @exception	IllegalWriteException because the underlying
     *			implementation does not support modification
     */
    @Override
    public void saveChanges() throws MessagingException {
	// POP3 Messages are read-only
	throw new IllegalWriteException("POP3 messages are read-only");
    }

    /**
     * Output the message as an RFC 822 format stream, without
     * specified headers.  If the property "mail.pop3.cachewriteto"
     * is set to "true", and ignoreList is null, and the message hasn't
     * already been cached as a side effect of other operations, the message
     * content is cached before being written.  Otherwise, the message is
     * streamed directly to the output stream without being cached.
     *
     * @exception IOException	if an error occurs writing to the stream
     *				or if an error is generated by the
     *				javax.activation layer.
     * @exception MessagingException for other failures
     * @see javax.activation.DataHandler#writeTo
     */
    @Override
    public synchronized void writeTo(OutputStream os, String[] ignoreList)
				throws IOException, MessagingException {
	InputStream rawcontent = rawData.get();
	if (rawcontent == null && ignoreList == null &&
			!((POP3Store)(folder.getStore())).cacheWriteTo) {
	    if (folder.logger.isLoggable(Level.FINE))
		folder.logger.fine("streaming msg " + msgnum);
	    if (!folder.getProtocol().retr(msgnum, os)) {
		expunged = true;
		throw new MessageRemovedException("can't retrieve message #" +
		    msgnum + " in POP3Message.writeTo");    // XXX - what else?
	    }
	} else if (rawcontent != null && ignoreList == null) {
	    // can just copy the cached data
	    InputStream in = ((SharedInputStream)rawcontent).newStream(0, -1);
	    try {
		byte[] buf = new byte[16*1024];
		int len;
		while ((len = in.read(buf)) > 0)
		    os.write(buf, 0, len); 
	    } finally {
		try {
		    if (in != null)
			in.close();
		} catch (IOException ex) { }
	    }
	} else
	    super.writeTo(os, ignoreList);
    }

    /**
     * Load the headers for this message into the InternetHeaders object.
     * The headers are fetched using the POP3 TOP command.
     */
    private void loadHeaders() throws MessagingException {
	assert !Thread.holdsLock(this);
	try {
	    boolean fetchContent = false;
	    synchronized (this) {
		if (headers != null)    // check again under lock
		    return;
		InputStream hdrs = null;
		if (((POP3Store)(folder.getStore())).disableTop ||
			(hdrs = folder.getProtocol().top(msgnum, 0)) == null) {
		    // possibly because the TOP command isn't supported,
		    // load headers as a side effect of loading the entire
		    // content.
		    fetchContent = true;
		} else {
		    try {
			hdrSize = hdrs.available();
			headers = new InternetHeaders(hdrs);
		    } finally {
			hdrs.close();
		    }
		}
	    }

	    /*
	     * Outside the synchronization block...
	     *
	     * Do we need to fetch the entire mesage content in order to
	     * load the headers as a side effect?  Yes, there's a race
	     * condition here - multiple threads could decide that the
	     * content needs to be fetched.  Fortunately, they'll all
	     * synchronize in the getContentStream method and the content
	     * will only be loaded once.
	     */
	    if (fetchContent) {
		InputStream cs = null;
		try {
		    cs = getContentStream();
		} finally {
		    if (cs != null)
			cs.close();
		}
	    }
	} catch (EOFException eex) {
	    folder.close(false);
	    throw new FolderClosedException(folder, eex.toString());
	} catch (IOException ex) {
	    throw new MessagingException("error loading POP3 headers", ex);
	}
    }
}
