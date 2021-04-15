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

package com.sun.mail.mbox;

import javax.mail.*;
import javax.mail.event.*;
import javax.mail.internet.*;
import javax.mail.util.*;
import java.io.*;
import java.util.*;
import com.sun.mail.util.LineInputStream;

/**
 * This class represents a mailbox file containing RFC822 style email messages. 
 *
 * @author John Mani
 * @author Bill Shannon
 */

public class MboxFolder extends Folder {

    private String name;	// null => the default folder
    private boolean is_inbox = false;
    private int total;		// total number of messages in mailbox
    private volatile boolean opened = false;
    private List<MessageMetadata> messages;
    private TempFile temp;
    private MboxStore mstore;
    private MailFile folder;
    private long file_size;	// the size the last time we read or wrote it
    private long saved_file_size; // size at the last open, close, or expunge
    private boolean special_imap_message;

    private static final boolean homeRelative =
				Boolean.getBoolean("mail.mbox.homerelative");

    /**
     * Metadata for each message, to avoid instantiating MboxMessage
     * objects for messages we're not going to look at. <p>
     *
     * MboxFolder keeps an array of these objects corresponding to
     * each message in the folder.  Access to the array elements is
     * synchronized, but access to contents of this object is not.
     * The metadata stored here is only accessed if the message field
     * is null; otherwise the MboxMessage object contains the metadata.
     */
    static final class MessageMetadata {
	public long start;	// offset in temp file of start of this message
	// public long end;	// offset in temp file of end of this message
	public long dataend;	// offset of end of message data, <= "end"
	public MboxMessage message;	// the message itself
	public boolean recent;	// message is recent?
	public boolean deleted;	// message is marked deleted?
	public boolean imap;	// special imap message?
    }

    public MboxFolder(MboxStore store, String name) {
	super(store);
	this.mstore = store;
	this.name = name;

	if (name != null && name.equalsIgnoreCase("INBOX"))
	    is_inbox = true;

	folder = mstore.getMailFile(name == null ? "~" : name);
	if (folder.exists())
	    saved_file_size = folder.length();
	else
	    saved_file_size = -1;
    }

    public char getSeparator() {
	return File.separatorChar;
    }

    public Folder[] list(String pattern) throws MessagingException {
	if (!folder.isDirectory())
	    throw new MessagingException("not a directory");

	if (name == null)
	    return list(null, pattern, true);
	else
	    return list(name + File.separator, pattern, false);
    }

    /*
     * Version of list shared by MboxStore and MboxFolder.
     */
    protected Folder[] list(String ref, String pattern, boolean fromStore)
					throws MessagingException {
	if (ref != null && ref.length() == 0)
	    ref = null;
	int i;
	String refdir = null;
	String realdir = null;

	pattern = canonicalize(ref, pattern);
	if ((i = indexOfAny(pattern, "%*")) >= 0) {
	    refdir = pattern.substring(0, i);
	} else {
	    refdir = pattern;
	}
	if ((i = refdir.lastIndexOf(File.separatorChar)) >= 0) {
	    // get rid of anything after directory name
	    refdir = refdir.substring(0, i + 1);
	    realdir = mstore.mb.filename(mstore.user, refdir);
	} else if (refdir.length() == 0 || refdir.charAt(0) != '~') {
	    // no separator and doesn't start with "~" => home or cwd
	    refdir = null;
	    if (homeRelative)
		realdir = mstore.home;
	    else
		realdir = ".";
	} else {
	    realdir = mstore.mb.filename(mstore.user, refdir);
	}
	List<String> flist = new ArrayList<String>();
	listWork(realdir, refdir, pattern, fromStore ? 0 : 1, flist);
	if (Match.path("INBOX", pattern, '\0'))
	    flist.add("INBOX");

	Folder fl[] = new Folder[flist.size()];
	for (i = 0; i < fl.length; i++) {
	    fl[i] = createFolder(mstore, flist.get(i));
	}
	return fl;
    }

    public String getName() {
	if (name == null)
	    return "";
	else if (is_inbox)
	    return "INBOX";
	else
	    return folder.getName();
    }

    public String getFullName() {
	if (name == null)
	    return "";
	else
	    return name;
    }

    public Folder getParent() {
	if (name == null)
	    return null;
	else if (is_inbox)
	    return createFolder(mstore, null);
	else
	    // XXX - have to recognize other folders under default folder
	    return createFolder(mstore, folder.getParent());
    }

    public boolean exists() {
	return folder.exists();
    }

    public int getType() {
	if (folder.isDirectory())
	    return HOLDS_FOLDERS;
	else
	    return HOLDS_MESSAGES;
    }

    public Flags getPermanentFlags() {
	return MboxStore.permFlags;
    }

    public synchronized boolean hasNewMessages() {
	if (folder instanceof UNIXFile) {
	    UNIXFile f = (UNIXFile)folder;
	    if (f.length() > 0) {
		long atime = f.lastAccessed();
		long mtime = f.lastModified();
//System.out.println(name + " atime " + atime + " mtime " + mtime);
		return atime < mtime;
	    }
	    return false;
	}
	long current_size;
	if (folder.exists())
	    current_size = folder.length();
	else
	    current_size = -1;
	// if we've never opened the folder, remember the size now
	// (will cause us to return false the first time)
	if (saved_file_size < 0)
	    saved_file_size = current_size;
	return current_size > saved_file_size;
    }

    public synchronized Folder getFolder(String name)
					throws MessagingException {
	if (folder.exists() && !folder.isDirectory())
	    throw new MessagingException("not a directory");
	return createFolder(mstore,
		(this.name == null ? "~" : this.name) + File.separator + name);
    }

    public synchronized boolean create(int type) throws MessagingException {
	switch (type) {
	case HOLDS_FOLDERS:
	    if (!folder.mkdirs()) {
		return false;
	    }
	    break;

	case HOLDS_MESSAGES:
	    if (folder.exists()) {
		return false;
	    }
	    try {
		(new FileOutputStream((File)folder)).close();
	    } catch (FileNotFoundException fe) {
		File parent = new File(folder.getParent());
		if (!parent.mkdirs())
		    throw new
			MessagingException("can't create folder: " + name);
		try {
		    (new FileOutputStream((File)folder)).close();
		} catch (IOException ex3) {
		    throw new
			MessagingException("can't create folder: " + name, ex3);
		}
	    } catch (IOException e) {
		throw new
		    MessagingException("can't create folder: " + name, e);
	    }
	    break;

	default:
	    throw new MessagingException("type not supported");
	}
	notifyFolderListeners(FolderEvent.CREATED);
	return true;
    }

    public synchronized boolean delete(boolean recurse)
					throws MessagingException {
	checkClosed();
	if (name == null)
	    throw new MessagingException("can't delete default folder");
	boolean ret = true;
	if (recurse && folder.isDirectory())
	    ret = delete(new File(folder.getPath()));
	if (ret && folder.delete()) {
	    notifyFolderListeners(FolderEvent.DELETED);
	    return true;
	}
	return false;
    }

    /**
     * Recursively delete the specified file/directory.
     */
    private boolean delete(File f) {
	File[] files = f.listFiles();
	boolean ret = true;
	for (int i = 0; ret && i < files.length; i++) {
	    if (files[i].isDirectory())
		ret = delete(files[i]);
	    else
		ret = files[i].delete();
	}
	return ret;
    }

    public synchronized boolean renameTo(Folder f)
				throws MessagingException {
	checkClosed();
	if (name == null)
	    throw new MessagingException("can't rename default folder");
	if (!(f instanceof MboxFolder))
	    throw new MessagingException("can't rename to: " + f.getName());
	String newname = ((MboxFolder)f).folder.getPath();
	if (folder.renameTo(new File(newname))) {
	    notifyFolderRenamedListeners(f);
	    return true;
	}
	return false;
    }

    /* Ensure the folder is open */
    void checkOpen() throws IllegalStateException {
	if (!opened) 
	    throw new IllegalStateException("Folder is not Open");
    }

    /* Ensure the folder is not open */
    private void checkClosed() throws IllegalStateException {
	if (opened) 
	    throw new IllegalStateException("Folder is Open");
    }

    /*
     * Check that the given message number is within the range
     * of messages present in this folder. If the message
     * number is out of range, we check to see if new messages
     * have arrived.
     */
    private void checkRange(int msgno) throws MessagingException {
	if (msgno < 1) // message-numbers start at 1
	    throw new IndexOutOfBoundsException("message number < 1");

	if (msgno <= total)
	    return;

	// Out of range, let's check if there are any new messages.
	getMessageCount();

	if (msgno > total) // Still out of range ? Throw up ...
	    throw new IndexOutOfBoundsException(msgno + " > " + total);
    }

    /* Ensure the folder is open & readable */
    private void checkReadable() throws IllegalStateException {
	if (!opened || (mode != READ_ONLY && mode != READ_WRITE))
	    throw new IllegalStateException("Folder is not Readable");
    }

    /* Ensure the folder is open & writable */
    private void checkWritable() throws IllegalStateException {
	if (!opened || mode != READ_WRITE)
	    throw new IllegalStateException("Folder is not Writable");
    }

    public boolean isOpen() {
        return opened;
    }

    /*
     * Open the folder in the specified mode.
     */
    public synchronized void open(int mode) throws MessagingException {
	if (opened)
	    throw new IllegalStateException("Folder is already Open");

	if (!folder.exists())
	    throw new FolderNotFoundException(this, "Folder doesn't exist: " +
					    folder.getPath());
	this.mode = mode;
	switch (mode) {
	case READ_WRITE:
	default:
	    if (!folder.canWrite())
		throw new MessagingException("Open Failure, can't write: " +
					    folder.getPath());
	    // fall through...

	case READ_ONLY:
	    if (!folder.canRead())
		throw new MessagingException("Open Failure, can't read: " +
					    folder.getPath());
	    break;
	}

	if (is_inbox && folder instanceof InboxFile) {
	    InboxFile inf = (InboxFile)folder;
	    if (!inf.openLock(mode == READ_WRITE ? "rw" : "r"))
		throw new MessagingException("Failed to lock INBOX");
	}
	if (!folder.lock("r"))
	    throw new MessagingException("Failed to lock folder: " + name);
	messages = new ArrayList<MessageMetadata>();
	total = 0;
	Message[] msglist = null;
	try {
	    temp = new TempFile(null);
	    saved_file_size = folder.length();
	    msglist = load(0L, false);
	} catch (IOException e) {
	    throw new MessagingException("IOException", e);
	} finally {
	    folder.unlock();
	}
	notifyConnectionListeners(ConnectionEvent.OPENED);
	if (msglist != null)
	    notifyMessageAddedListeners(msglist);
	opened = true;		// now really opened
    }

    public synchronized void close(boolean expunge) throws MessagingException {
	checkOpen();

	try {
	    if (mode == READ_WRITE) {
		try {
		    writeFolder(true, expunge);
		} catch (IOException e) {
		    throw new MessagingException("I/O Exception", e);
		}
	    }
	    messages = null;
	} finally {
	    opened = false;
	    if (is_inbox && folder instanceof InboxFile) {
		InboxFile inf = (InboxFile)folder;
		inf.closeLock();
	    }
	    temp.close();
	    temp = null;
	    notifyConnectionListeners(ConnectionEvent.CLOSED);
	}
    }

    /**
     * Re-write the folder with the current contents of the messages.
     * If closing is true, turn off the RECENT flag.  If expunge is
     * true, don't write out deleted messages (only used from close()
     * when the message cache won't be accessed again).
     *
     * Return the number of messages written.
     */
    protected int writeFolder(boolean closing, boolean expunge)
			throws IOException, MessagingException {

	/*
	 * First, see if there have been any changes.
	 */
	int modified = 0, deleted = 0, recent = 0;
	for (int msgno = 1; msgno <= total; msgno++) {
	    MessageMetadata md = messages.get(messageIndexOf(msgno));
	    MboxMessage msg = md.message;
	    if (msg != null) {
		Flags flags = msg.getFlags();
		if (msg.isModified() || !msg.origFlags.equals(flags))
		    modified++;
		if (flags.contains(Flags.Flag.DELETED))
		    deleted++;
		if (flags.contains(Flags.Flag.RECENT))
		    recent++;
	    } else {
		if (md.deleted)
		    deleted++;
		if (md.recent)
		    recent++;
	    }
	}
	if ((!closing || recent == 0) && (!expunge || deleted == 0) &&
		modified == 0)
	    return 0;

	/*
	 * Have to save any new mail that's been appended to the
	 * folder since we last loaded it.
	 */
	if (!folder.lock("rw"))
	    throw new MessagingException("Failed to lock folder: " + name);
	int oldtotal = total;	// XXX
	Message[] msglist = null;
	if (folder.length() != file_size)
	    msglist = load(file_size, !closing);
	// don't use the folder's FD, need to re-open in order to trunc the file
	OutputStream os =
		new BufferedOutputStream(new FileOutputStream((File)folder));
	int wr = 0;
	boolean keep = true;
	try {
	    if (special_imap_message)
		appendStream(getMessageStream(0), os);
	    for (int msgno = 1; msgno <= total; msgno++) {
		MessageMetadata md = messages.get(messageIndexOf(msgno));
		MboxMessage msg = md.message;
		if (msg != null) {
		    if (expunge && msg.isSet(Flags.Flag.DELETED))
			continue;	// skip it;
		    if (closing && msgno <= oldtotal &&
						msg.isSet(Flags.Flag.RECENT))
			msg.setFlag(Flags.Flag.RECENT, false);
		    writeMboxMessage(msg, os);
		} else {
		    if (expunge && md.deleted)
			continue;	// skip it;
		    if (closing && msgno <= oldtotal && md.recent) {
			// have to instantiate message so that we can
			// clear the recent flag
			msg = (MboxMessage)getMessage(msgno);
			msg.setFlag(Flags.Flag.RECENT, false);
			writeMboxMessage(msg, os);
		    } else {
			appendStream(getMessageStream(msgno), os);
		    }
		}
		folder.touchlock();
		wr++;
	    }
	    // If no messages in the mailbox, and we're closing,
	    // maybe we should remove the mailbox.
	    if (wr == 0 && closing) {
		String skeep = ((MboxStore)store).getSession().
					getProperty("mail.mbox.deleteEmpty");
		if (skeep != null && skeep.equalsIgnoreCase("true"))
		    keep = false;
	    }
	} catch (IOException e) {
	    throw e;
	} catch (MessagingException e) {
	    throw e;
	} catch (Exception e) {
e.printStackTrace();
	    throw new MessagingException("unexpected exception " + e);
	} finally {
	    // close the folder, flushing out the data
	    try {
		os.close();
		file_size = saved_file_size = folder.length();
		if (!keep) {
		    folder.delete();
		    file_size = 0;
		}
	    } catch (IOException ex) {}

	    if (keep) {
		// make sure the access time is greater than the mod time
		// XXX - would be nice to have utime()
		try {
		    Thread.sleep(1000);		// sleep for a second
		} catch (InterruptedException ex) {}
		InputStream is = null;
		try {
		    is = new FileInputStream((File)folder);
		    is.read();	// read a byte
		} catch (IOException ex) {}	// ignore errors
		try {
		    if (is != null)
			is.close();
		    is = null;
		} catch (IOException ex) {}	// ignore errors
	    }

	    folder.unlock();
	    if (msglist != null)
		notifyMessageAddedListeners(msglist);
	}
	return wr;
    }

    /**
     * Append the input stream to the output stream, closing the
     * input stream when done.
     */
    private static final void appendStream(InputStream is, OutputStream os)
				throws IOException {
	try {
	    byte[] buf = new byte[64 * 1024];
	    int len;
	    while ((len = is.read(buf)) > 0)
		os.write(buf, 0, len);
	} finally {
	    is.close();
	}
    }

    /**
     * Write a MimeMessage to the specified OutputStream in a
     * format suitable for a UNIX mailbox, i.e., including a correct
     * Content-Length header and with the local platform's line
     * terminating convention. <p>
     *
     * If the message is really a MboxMessage, use its writeToFile
     * method, which has access to the UNIX From line.  Otherwise, do
     * all the work here, creating an appropriate UNIX From line.
     */
    public static void writeMboxMessage(MimeMessage msg, OutputStream os)
				throws IOException, MessagingException {
	try {
	    if (msg instanceof MboxMessage) {
		((MboxMessage)msg).writeToFile(os);
	    } else {
		// XXX - modify the message to preserve the flags in headers
		MboxMessage.setHeadersFromFlags(msg);
		ContentLengthCounter cos = new ContentLengthCounter();
		NewlineOutputStream nos = new NewlineOutputStream(cos);
		msg.writeTo(nos);
		nos.flush();
		os = new NewlineOutputStream(os, true);
		os = new ContentLengthUpdater(os, cos.getSize());
		PrintStream pos = new PrintStream(os, false, "iso-8859-1");
		pos.println(getUnixFrom(msg));
		msg.writeTo(pos);
		pos.flush();
	    }
	} catch (MessagingException me) {
	    throw me;
	} catch (IOException ioe) {
	    throw ioe;
	}
    }

    /**
     * Construct an appropriately formatted UNIX From line using
     * the sender address and the date in the message.
     */
    protected static String getUnixFrom(MimeMessage msg) {
	Address[] afrom;
	String from;
	Date ddate;
	String date;
	try {
	    if ((afrom = msg.getFrom()) == null ||
		    !(afrom[0] instanceof InternetAddress) ||
		    (from = ((InternetAddress)afrom[0]).getAddress()) == null)
		from = "UNKNOWN";
	    if ((ddate = msg.getReceivedDate()) == null ||
		    (ddate = msg.getSentDate()) == null)
		ddate = new Date();
	} catch (MessagingException e) {
	    from = "UNKNOWN";
	    ddate = new Date();
	}
	date = ddate.toString();
	// date is of the form "Sat Aug 12 02:30:00 PDT 1995"
	// need to strip out the timezone
	return "From " + from + " " +
		date.substring(0, 20) + date.substring(24);
    }

    public synchronized int getMessageCount() throws MessagingException {
	if (!opened)
	    return -1;

	boolean locked = false;
	Message[] msglist = null;
	try {
	    if (folder.length() != file_size) {
		if (!folder.lock("r"))
		    throw new MessagingException("Failed to lock folder: " +
							name);
		locked = true;
		msglist = load(file_size, true);
	    }
	} catch (IOException e) {
	    throw new MessagingException("I/O Exception", e);
	} finally {
	    if (locked) {
		folder.unlock();
		if (msglist != null)
		    notifyMessageAddedListeners(msglist);
	    }
	}
	return total;
    }

    /**
     * Get the specified message.  Note that messages are numbered
     * from 1.
     */
    public synchronized Message getMessage(int msgno)
				throws MessagingException {
	checkReadable();
	checkRange(msgno);

	MessageMetadata md = messages.get(messageIndexOf(msgno));
	MboxMessage m = md.message;
	if (m == null) {
	    InputStream is = getMessageStream(msgno);
	    try {
		m = loadMessage(is, msgno, mode == READ_WRITE);
	    } catch (IOException ex) {
		MessagingException mex =
		    new MessageRemovedException("mbox message gone", ex);
		throw mex;
	    }
	    md.message = m;
	}
	return m;
    }

    private final int messageIndexOf(int msgno) {
	return special_imap_message ? msgno : msgno - 1;
    }

    private InputStream getMessageStream(int msgno) {
	int index = messageIndexOf(msgno);
	MessageMetadata md = messages.get(index);
	return temp.newStream(md.start, md.dataend);
    }

    public synchronized void appendMessages(Message[] msgs)
				throws MessagingException {
	if (!folder.lock("rw"))
	    throw new MessagingException("Failed to lock folder: " + name);

	OutputStream os = null;
	boolean err = false;
	try {
	    os = new BufferedOutputStream(
		new FileOutputStream(((File)folder).getPath(), true));
		// XXX - should use getAbsolutePath()?
	    for (int i = 0; i < msgs.length; i++) {
		if (msgs[i] instanceof MimeMessage) {
		    writeMboxMessage((MimeMessage)msgs[i], os);
		} else {
		    err = true;
		    continue;
		}
		folder.touchlock();
	    }
	} catch (IOException e) {
	    throw new MessagingException("I/O Exception", e);
	} catch (MessagingException e) {
	    throw e;
	} catch (Exception e) {
e.printStackTrace();
	    throw new MessagingException("unexpected exception " + e);
	} finally {
	    if (os != null)
		try {
		    os.close();
		} catch (IOException e) {
		    // ignored
		}
	    folder.unlock();
	}
	if (opened)
	    getMessageCount();	// loads new messages as a side effect
	if (err)
	    throw new MessagingException("Can't append non-Mime message");
    }

    public synchronized Message[] expunge() throws MessagingException {
	checkWritable();

	/*
	 * First, write out the folder to make sure we have permission,
	 * disk space, etc.
	 */
	int wr = total;		// number of messages written out
	try {
	    wr = writeFolder(false, true);
	} catch (IOException e) {
	    throw new MessagingException("expunge failed", e);
	}
	if (wr == total)	// wrote them all => nothing expunged
	    return new Message[0];

	/*
	 * Now, actually get rid of the expunged messages.
	 */
	int del = 0;
	Message[] msglist = new Message[total - wr];
	int msgno = 1;
	while (msgno <= total) {
	    MessageMetadata md = messages.get(messageIndexOf(msgno));
	    MboxMessage msg = md.message;
	    if (msg != null) {
		if (msg.isSet(Flags.Flag.DELETED)) {
		    msg.setExpunged(true);
		    msglist[del] = msg;
		    del++;
		    messages.remove(messageIndexOf(msgno));
		    total--;
		} else {
		    msg.setMessageNumber(msgno);	// update message number
		    msgno++;
		}
	    } else {
		if (md.deleted) {
		    // have to instantiate it for the notification
		    msg = (MboxMessage)getMessage(msgno);
		    msg.setExpunged(true);
		    msglist[del] = msg;
		    del++;
		    messages.remove(messageIndexOf(msgno));
		    total--;
		} else {
		    msgno++;
		}
	    }
	}
	if (del != msglist.length)		// this is really an assert
	    throw new MessagingException("expunge delete count wrong");
	notifyMessageRemovedListeners(true, msglist);
	return msglist;
    }

    /*
     * Load more messages from the folder starting at the specified offset.
     */
    private Message[] load(long offset, boolean notify)
				throws MessagingException, IOException {
	int oldtotal = total;
	MessageLoader loader = new MessageLoader(temp);
	int loaded = loader.load(folder.getFD(), offset, messages);
	total += loaded;
	file_size = folder.length();

	if (offset == 0 && loaded > 0) {
	    /*
	     * If the first message is the special message that the
	     * IMAP server adds to the mailbox, remember that we've
	     * seen it so it won't be shown to the user.
	     */
	    MessageMetadata md = messages.get(0);
	    if (md.imap) {
		special_imap_message = true;
		total--;
	    }
	}
	if (notify) {
	    Message[] msglist = new Message[total - oldtotal];
	    for (int i = oldtotal, j = 0; i < total; i++, j++)
		msglist[j] = getMessage(i + 1);
	    return msglist;
	} else
	    return null;
    }

    /**
     * Parse the input stream and return an appropriate message object.
     * The InputStream must be a SharedInputStream.
     */
    private MboxMessage loadMessage(InputStream is, int msgno,
		boolean writable) throws MessagingException, IOException {
	LineInputStream in = new LineInputStream(is);

	/*
	 * Read lines until a UNIX From line,
	 * skipping blank lines.
	 */
	String line;
	String unix_from = null;
	while ((line = in.readLine()) != null) {
	    if (line.trim().length() == 0)
		continue;
	    if (line.startsWith("From ")) {
		/*
		 * A UNIX From line looks like:
		 * From address Day Mon DD HH:MM:SS YYYY
		 */
		unix_from = line;
		int i;
		// find the space after the address, before the date
		i = unix_from.indexOf(' ', 5);
		if (i < 0)
		    continue;	// not a valid UNIX From line
		break;
	    }
	    throw new MessagingException("Garbage in mailbox: " + line);
	}

	if (unix_from == null)
	    throw new EOFException("end of mailbox");

	/*
	 * Now load the RFC822 headers into an InternetHeaders object.
	 */
	InternetHeaders hdrs = new InternetHeaders(is);

	// the rest is the message content
	SharedInputStream sis = (SharedInputStream)is;
	InputStream stream = sis.newStream(sis.getPosition(), -1);
	return new MboxMessage(this, hdrs, stream, msgno, unix_from, writable);
    }

    /*
     * Only here to make accessible to MboxMessage.
     */
    protected void notifyMessageChangedListeners(int type, Message m) {
	super.notifyMessageChangedListeners(type, m);
    }


    /**
     * this is an exact duplicate of the Folder.getURL except it doesn't
     * add a beginning '/' to the URLName.
     */
    public URLName getURLName() {
	// XXX - note:  this should not be done this way with the
	// new javax.mail apis.

	URLName storeURL = getStore().getURLName();
	if (name == null)
	    return storeURL;

	char separator = getSeparator();
	String fullname = getFullName();
	StringBuilder encodedName = new StringBuilder();

	// We need to encode each of the folder's names, and replace
	// the store's separator char with the URL char '/'.
	StringTokenizer tok = new StringTokenizer(
	    fullname, Character.toString(separator), true);

	while (tok.hasMoreTokens()) {
	    String s = tok.nextToken();
	    if (s.charAt(0) == separator)
		encodedName.append("/");
	    else
		// XXX - should encode, but since there's no decoder...
		//encodedName.append(java.net.URLEncoder.encode(s));
		encodedName.append(s);
	}

	return new URLName(storeURL.getProtocol(), storeURL.getHost(),
			    storeURL.getPort(), encodedName.toString(),
			    storeURL.getUsername(),
			    null /* no password */);
    }

    /**
     * Create an MboxFolder object, or a subclass thereof.
     * Can be overridden by subclasses of MboxFolder so that
     * the appropriate subclass is created by the list method.
     */
    protected Folder createFolder(MboxStore store, String name) {
	return new MboxFolder(store, name);
    }

    /*
     * Support routines for list().
     */

    /**
     * Return a canonicalized pattern given a reference name and a pattern.
     */
    private static String canonicalize(String ref, String pat) {
	if (ref == null)
	    return pat;
	try {
	    if (pat.length() == 0) {
		return ref;
	    } else if (pat.charAt(0) == File.separatorChar) {
		return ref.substring(0, ref.indexOf(File.separatorChar)) + pat;
	    } else {
		return ref + pat;
	    }
	} catch (StringIndexOutOfBoundsException e) {
	    return pat;
	}
    }

    /**
     * Return the first index of any of the characters in "any" in "s",
     * or -1 if none are found.
     *
     * This should be a method on String.
     */
    private static int indexOfAny(String s, String any) {
	try {
	    int len = s.length();
	    for (int i = 0; i < len; i++) {
		if (any.indexOf(s.charAt(i)) >= 0)
		    return i;
	    }
	    return -1;
	} catch (StringIndexOutOfBoundsException e) {
	    return -1;
	}
    }

    /**
     * The recursive part of generating the list of mailboxes.
     * realdir is the full pathname to the directory to search.
     * dir is the name the user uses, often a relative name that's
     * relative to the user's home directory.  dir (if not null) always
     * has a trailing file separator character.
     *
     * @param realdir	real pathname of directory to start looking in
     * @param dir	user's name for realdir
     * @param pat	pattern to match against
     * @param level	level of the directory hierarchy we're in
     * @param flist	list to which to add folder names that match
     */
    // Derived from the c-client listWork() function.
    private void listWork(String realdir, String dir, String pat,
					int level, List<String> flist) {
	String sl[];
	File fdir = new File(realdir);
	try {
	    sl = fdir.list();
	} catch (SecurityException e) {
	    return;	// can't read it, ignore it
	}

	if (level == 0 && dir != null &&
		Match.path(dir, pat, File.separatorChar))
	    flist.add(dir);

	if (sl == null)
	    return;	// nothing return, we're done

	if (realdir.charAt(realdir.length() - 1) != File.separatorChar)
	    realdir += File.separator;

	for (int i = 0; i < sl.length; i++) {
	    if (sl[i].charAt(0) == '.')
		continue;	// ignore all "dot" files for now
	    String md = realdir + sl[i];
	    File mf = new File(md);
	    if (!mf.exists())
		continue;
	    String name;
	    if (dir != null)
		name = dir + sl[i];
	    else
		name = sl[i];
	    if (mf.isDirectory()) {
		if (Match.path(name, pat, File.separatorChar)) {
		    flist.add(name);
		    name += File.separator;
		} else {
		    name += File.separator;
		    if (Match.path(name, pat, File.separatorChar))
			flist.add(name);
		}
		if (Match.dir(name, pat, File.separatorChar))
		    listWork(md, name, pat, level + 1, flist);
	    } else {
		if (Match.path(name, pat, File.separatorChar))
		    flist.add(name);
	    }
	}
    }
}

/**
 * Pattern matching support class for list().
 * Should probably be more public.
 */
// Translated from the c-client functions pmatch_full() and dmatch().
class Match {
    /**
     * Pathname pattern match
     *
     * @param s		base string
     * @param pat	pattern string
     * @param delim	delimiter character
     * @return		true if base matches pattern
     */
    static public boolean path(String s, String pat, char delim) {
	try {
	    return path(s, 0, s.length(), pat, 0, pat.length(), delim);
	} catch (StringIndexOutOfBoundsException e) {
	    return false;
	}
    }

    static private boolean path(String s, int s_index, int s_len,
	String pat, int p_index, int p_len, char delim)
	    throws StringIndexOutOfBoundsException {

	while (p_index < p_len) {
	    char c = pat.charAt(p_index);
	    switch (c) {
	    case '%':
		if (++p_index >= p_len)		// % at end of pattern
						// ok if no delimiters
		    return delim == 0 || s.indexOf(delim, s_index) < 0;
		// scan remainder until delimiter
		do {
		    if (path(s, s_index, s_len, pat, p_index, p_len, delim))
			return true;
		} while (s.charAt(s_index) != delim && ++s_index < s_len);
		// ran into a delimiter or ran out of string without a match
		return false;

	    case '*':
		if (++p_index >= p_len)		// end of pattern?
		    return true;		// unconditional match
		do {
		    if (path(s, s_index, s_len, pat, p_index, p_len, delim))
			return true;
		} while (++s_index < s_len);
		// ran out of string without a match
		return false;

	    default:
		// if ran out of string or no match, fail
		if (s_index >= s_len || c != s.charAt(s_index))
		    return false;

		// try the next string and pattern characters
		s_index++;
		p_index++;
	    }
	}
	return s_index >= s_len;
    }

    /**
     * Directory pattern match
     *
     * @param s		base string
     * @param pat	pattern string
     * @return		true if base is a matching directory of pattern
     */
    static public boolean dir(String s, String pat, char delim) {
	try {
	    return dir(s, 0, s.length(), pat, 0, pat.length(), delim);
	} catch (StringIndexOutOfBoundsException e) {
	    return false;
	}
    }

    static private boolean dir(String s, int s_index, int s_len,
	String pat, int p_index, int p_len, char delim)
	    throws StringIndexOutOfBoundsException {

	while (p_index < p_len) {
	    char c = pat.charAt(p_index);
	    switch (c) {
	    case '%':
		if (s_index >= s_len)		// end of base?
		    return true;		// subset match
		if (++p_index >= p_len)		// % at end of pattern?
		    return false;		// no inferiors permitted
		do {
		    if (dir(s, s_index, s_len, pat, p_index, p_len, delim))
			return true;
		} while (s.charAt(s_index) != delim && ++s_index < s_len);

		if (s_index + 1 == s_len)	// s ends with a delimiter
		    return true;		// must be a subset of pattern
		return dir(s, s_index, s_len, pat, p_index, p_len, delim);

	    case '*':
		return true;			// unconditional match

	    default:
		if (s_index >= s_len)		// end of base?
		    return c == delim;		// matched if at delimiter

		if (c != s.charAt(s_index))
		    return false;

		// try the next string and pattern characters
		s_index++;
		p_index++;
	    }
	}
	return s_index >= s_len;
    }
}

/**
 * A ByteArrayOutputStream that allows us to share the byte array
 * rather than copy it.  Eventually could replace this with something
 * that doesn't require a single contiguous byte array.
 */
class SharedByteArrayOutputStream extends ByteArrayOutputStream {
    public SharedByteArrayOutputStream(int size) {
	super(size);
    }

    public InputStream toStream() {
	return new SharedByteArrayInputStream(buf, 0, count);
    }
}
