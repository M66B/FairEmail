/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package javax.activation;

import java.util.*;
import java.io.*;
import java.net.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
import com.sun.activation.registries.MailcapFile;
import com.sun.activation.registries.LogSupport;

/**
 * MailcapCommandMap extends the CommandMap
 * abstract class. It implements a CommandMap whose configuration
 * is based on mailcap files
 * (<A HREF="http://www.ietf.org/rfc/rfc1524.txt">RFC 1524</A>).
 * The MailcapCommandMap can be configured both programmatically
 * and via configuration files.
 * <p>
 * <b>Mailcap file search order:</b><p>
 * The MailcapCommandMap looks in various places in the user's
 * system for mailcap file entries. When requests are made
 * to search for commands in the MailcapCommandMap, it searches  
 * mailcap files in the following order:
 * <ol>
 * <li> Programatically added entries to the MailcapCommandMap instance.
 * <li> The file <code>.mailcap</code> in the user's home directory.
 * <li> The file <code>mailcap</code> in the Java runtime.
 * <li> The file or resources named <code>META-INF/mailcap</code>.
 * <li> The file or resource named <code>META-INF/mailcap.default</code>
 * (usually found only in the <code>activation.jar</code> file).
 * </ol>
 * <p>
 * (The current implementation looks for the <code>mailcap</code> file
 * in the Java runtime in the directory <code><i>java.home</i>/conf</code>
 * if it exists, and otherwise in the directory
 * <code><i>java.home</i>/lib</code>, where <i>java.home</i> is the value
 * of the "java.home" System property.  Note that the "conf" directory was
 * introduced in JDK 9.)
 * <p>
 * <b>Mailcap file format:</b><p>
 *
 * Mailcap files must conform to the mailcap
 * file specification (RFC 1524, <i>A User Agent Configuration Mechanism
 * For Multimedia Mail Format Information</i>). 
 * The file format consists of entries corresponding to
 * particular MIME types. In general, the specification 
 * specifies <i>applications</i> for clients to use when they
 * themselves cannot operate on the specified MIME type. The 
 * MailcapCommandMap extends this specification by using a parameter mechanism
 * in mailcap files that allows JavaBeans(tm) components to be specified as
 * corresponding to particular commands for a MIME type.<p>
 *
 * When a mailcap file is
 * parsed, the MailcapCommandMap recognizes certain parameter signatures,
 * specifically those parameter names that begin with <code>x-java-</code>.
 * The MailcapCommandMap uses this signature to find
 * command entries for inclusion into its registries.
 * Parameter names with the form <code>x-java-&lt;name&gt;</code>
 * are read by the MailcapCommandMap as identifying a command
 * with the name <i>name</i>. When the <i>name</i> is <code>
 * content-handler</code> the MailcapCommandMap recognizes the class
 * signified by this parameter as a <i>DataContentHandler</i>.
 * All other commands are handled generically regardless of command 
 * name. The command implementation is specified by a fully qualified
 * class name of a JavaBean(tm) component. For example; a command for viewing
 * some data can be specified as: <code>x-java-view=com.foo.ViewBean</code>.<p>
 *
 * When the command name is <code>fallback-entry</code>, the value of
 * the command may be <code>true</code> or <code>false</code>.  An
 * entry for a MIME type that includes a parameter of
 * <code>x-java-fallback-entry=true</code> defines fallback commands
 * for that MIME type that will only be used if no non-fallback entry
 * can be found.  For example, an entry of the form <code>text/*; ;
 * x-java-fallback-entry=true; x-java-view=com.sun.TextViewer</code>
 * specifies a view command to be used for any text MIME type.  This
 * view command would only be used if a non-fallback view command for
 * the MIME type could not be found.<p>
 * 
 * MailcapCommandMap aware mailcap files have the 
 * following general form:<p>
 * <code>
 * # Comments begin with a '#' and continue to the end of the line.<br>
 * &lt;mime type&gt;; ; &lt;parameter list&gt;<br>
 * # Where a parameter list consists of one or more parameters,<br>
 * # where parameters look like: x-java-view=com.sun.TextViewer<br>
 * # and a parameter list looks like: <br>
 * text/plain; ; x-java-view=com.sun.TextViewer; x-java-edit=com.sun.TextEdit
 * <br>
 * # Note that mailcap entries that do not contain 'x-java' parameters<br>
 * # and comply to RFC 1524 are simply ignored:<br>
 * image/gif; /usr/dt/bin/sdtimage %s<br>
 *
 * </code>
 * <p>
 *
 * @author Bart Calder
 * @author Bill Shannon
 */

public class MailcapCommandMap extends CommandMap {
    /*
     * We manage a collection of databases, searched in order.
     */
    private MailcapFile[] DB;
    private static final int PROG = 0;	// programmatically added entries

    private static final String confDir;

    static {
	String dir = null;
	try {
	    dir = (String)AccessController.doPrivileged(
		new PrivilegedAction() {
		    public Object run() {
			String home = System.getProperty("java.home");
			String newdir = home + File.separator + "conf";
			File conf = new File(newdir);
			if (conf.exists())
			    return newdir + File.separator;
			else
			    return home + File.separator + "lib" + File.separator;
		    }
		});
	} catch (Exception ex) {
	    // ignore any exceptions
	}
	confDir = dir;
    }

    /**
     * The default Constructor.
     */
    public MailcapCommandMap() {
	super();
	List dbv = new ArrayList(5);	// usually 5 or less databases
	MailcapFile mf = null;
	dbv.add(null);		// place holder for PROG entry

	LogSupport.log("MailcapCommandMap: load HOME");
	try {
	    String user_home = System.getProperty("user.home");

	    if (user_home != null) {
		String path = user_home + File.separator + ".mailcap";
		mf = loadFile(path);
		if (mf != null)
		    dbv.add(mf);
	    }
	} catch (SecurityException ex) {}

	LogSupport.log("MailcapCommandMap: load SYS");
	try {
	    // check system's home
	    if (confDir != null) {
		mf = loadFile(confDir + "mailcap");
		if (mf != null)
		    dbv.add(mf);
	    }
	} catch (SecurityException ex) {}

	LogSupport.log("MailcapCommandMap: load JAR");
	// load from the app's jar file
	loadAllResources(dbv, "META-INF/mailcap");

	LogSupport.log("MailcapCommandMap: load DEF");
	mf = loadResource("/META-INF/mailcap.default");

	if (mf != null)
	    dbv.add(mf);

	DB = new MailcapFile[dbv.size()];
	DB = (MailcapFile[])dbv.toArray(DB);
    }

    /**
     * Load from the named resource.
     */
    private MailcapFile loadResource(String name) {
	InputStream clis = null;
	try {
	    clis = SecuritySupport.getResourceAsStream(this.getClass(), name);
	    if (clis != null) {
		MailcapFile mf = new MailcapFile(clis);
		if (LogSupport.isLoggable())
		    LogSupport.log("MailcapCommandMap: successfully loaded " +
			"mailcap file: " + name);
		return mf;
	    } else {
		if (LogSupport.isLoggable())
		    LogSupport.log("MailcapCommandMap: not loading " +
			"mailcap file: " + name);
	    }
	} catch (IOException e) {
	    if (LogSupport.isLoggable())
		LogSupport.log("MailcapCommandMap: can't load " + name, e);
	} catch (SecurityException sex) {
	    if (LogSupport.isLoggable())
		LogSupport.log("MailcapCommandMap: can't load " + name, sex);
	} finally {
	    try {
		if (clis != null)
		    clis.close();
	    } catch (IOException ex) { }	// ignore it
	}
	return null;
    }

    /**
     * Load all of the named resource.
     */
    private void loadAllResources(List v, String name) {
	boolean anyLoaded = false;
	try {
	    URL[] urls;
	    ClassLoader cld = null;
	    // First try the "application's" class loader.
	    cld = SecuritySupport.getContextClassLoader();
	    if (cld == null)
		cld = this.getClass().getClassLoader();
	    if (cld != null)
		urls = SecuritySupport.getResources(cld, name);
	    else
		urls = SecuritySupport.getSystemResources(name);
	    if (urls != null) {
		if (LogSupport.isLoggable())
		    LogSupport.log("MailcapCommandMap: getResources");
		for (int i = 0; i < urls.length; i++) {
		    URL url = urls[i];
		    InputStream clis = null;
		    if (LogSupport.isLoggable())
			LogSupport.log("MailcapCommandMap: URL " + url);
		    try {
			clis = SecuritySupport.openStream(url);
			if (clis != null) {
			    v.add(new MailcapFile(clis));
			    anyLoaded = true;
			    if (LogSupport.isLoggable())
				LogSupport.log("MailcapCommandMap: " +
				    "successfully loaded " +
				    "mailcap file from URL: " +
				    url);
			} else {
			    if (LogSupport.isLoggable())
				LogSupport.log("MailcapCommandMap: " +
				    "not loading mailcap " +
				    "file from URL: " + url);
			}
		    } catch (IOException ioex) {
			if (LogSupport.isLoggable())
			    LogSupport.log("MailcapCommandMap: can't load " +
						url, ioex);
		    } catch (SecurityException sex) {
			if (LogSupport.isLoggable())
			    LogSupport.log("MailcapCommandMap: can't load " +
						url, sex);
		    } finally {
			try {
			    if (clis != null)
				clis.close();
			} catch (IOException cex) { }
		    }
		}
	    }
	} catch (Exception ex) {
	    if (LogSupport.isLoggable())
		LogSupport.log("MailcapCommandMap: can't load " + name, ex);
	}

	// if failed to load anything, fall back to old technique, just in case
	if (!anyLoaded) {
	    if (LogSupport.isLoggable())
		LogSupport.log("MailcapCommandMap: !anyLoaded");
	    MailcapFile mf = loadResource("/" + name);
	    if (mf != null)
		v.add(mf);
	}
    }

    /**
     * Load from the named file.
     */
    private MailcapFile loadFile(String name) {
	MailcapFile mtf = null;

	try {
	    mtf = new MailcapFile(name);
	} catch (IOException e) {
	    //	e.printStackTrace();
	}
	return mtf;
    }

    /**
     * Constructor that allows the caller to specify the path
     * of a <i>mailcap</i> file.
     *
     * @param fileName The name of the <i>mailcap</i> file to open
     * @exception	IOException	if the file can't be accessed
     */
    public MailcapCommandMap(String fileName) throws IOException {
	this();

	if (LogSupport.isLoggable())
	    LogSupport.log("MailcapCommandMap: load PROG from " + fileName);
	if (DB[PROG] == null) {
	    DB[PROG] = new MailcapFile(fileName);
	}
    }


    /**
     * Constructor that allows the caller to specify an <i>InputStream</i>
     * containing a mailcap file.
     *
     * @param is	InputStream of the <i>mailcap</i> file to open
     */
    public MailcapCommandMap(InputStream is) {
	this();

	LogSupport.log("MailcapCommandMap: load PROG");
	if (DB[PROG] == null) {
	    try {
		DB[PROG] = new MailcapFile(is);
	    } catch (IOException ex) {
		// XXX - should throw it
	    }
	}
    }

    /**
     * Get the preferred command list for a MIME Type. The MailcapCommandMap
     * searches the mailcap files as described above under
     * <i>Mailcap file search order</i>.<p>
     *
     * The result of the search is a proper subset of available
     * commands in all mailcap files known to this instance of 
     * MailcapCommandMap.  The first entry for a particular command
     * is considered the preferred command.
     *
     * @param mimeType	the MIME type
     * @return the CommandInfo objects representing the preferred commands.
     */
    public synchronized CommandInfo[] getPreferredCommands(String mimeType) {
	List cmdList = new ArrayList();
	if (mimeType != null)
	    mimeType = mimeType.toLowerCase(Locale.ENGLISH);

	for (int i = 0; i < DB.length; i++) {
	    if (DB[i] == null)
		continue;
	    Map cmdMap = DB[i].getMailcapList(mimeType);
	    if (cmdMap != null)
		appendPrefCmdsToList(cmdMap, cmdList);
	}

	// now add the fallback commands
	for (int i = 0; i < DB.length; i++) {
	    if (DB[i] == null)
		continue;
	    Map cmdMap = DB[i].getMailcapFallbackList(mimeType);
	    if (cmdMap != null)
		appendPrefCmdsToList(cmdMap, cmdList);
	}

	CommandInfo[] cmdInfos = new CommandInfo[cmdList.size()];
	cmdInfos = (CommandInfo[])cmdList.toArray(cmdInfos);

	return cmdInfos;
    }

    /**
     * Put the commands that are in the hash table, into the list.
     */
    private void appendPrefCmdsToList(Map cmdHash, List cmdList) {
	Iterator verb_enum = cmdHash.keySet().iterator();

	while (verb_enum.hasNext()) {
	    String verb = (String)verb_enum.next();
	    if (!checkForVerb(cmdList, verb)) {
		List cmdList2 = (List)cmdHash.get(verb); // get the list
		String className = (String)cmdList2.get(0);
		cmdList.add(new CommandInfo(verb, className));
	    }
	}
    }

    /**
     * Check the cmdList to see if this command exists, return
     * true if the verb is there.
     */
    private boolean checkForVerb(List cmdList, String verb) {
	Iterator ee = cmdList.iterator();
	while (ee.hasNext()) {
	    String enum_verb =
		(String)((CommandInfo)ee.next()).getCommandName();
	    if (enum_verb.equals(verb))
		return true;
	}
	return false;
    }

    /**
     * Get all the available commands in all mailcap files known to
     * this instance of MailcapCommandMap for this MIME type.
     *
     * @param mimeType	the MIME type
     * @return the CommandInfo objects representing all the commands.
     */
    public synchronized CommandInfo[] getAllCommands(String mimeType) {
	List cmdList = new ArrayList();
	if (mimeType != null)
	    mimeType = mimeType.toLowerCase(Locale.ENGLISH);

	for (int i = 0; i < DB.length; i++) {
	    if (DB[i] == null)
		continue;
	    Map cmdMap = DB[i].getMailcapList(mimeType);
	    if (cmdMap != null)
		appendCmdsToList(cmdMap, cmdList);
	}

	// now add the fallback commands
	for (int i = 0; i < DB.length; i++) {
	    if (DB[i] == null)
		continue;
	    Map cmdMap = DB[i].getMailcapFallbackList(mimeType);
	    if (cmdMap != null)
		appendCmdsToList(cmdMap, cmdList);
	}

	CommandInfo[] cmdInfos = new CommandInfo[cmdList.size()];
	cmdInfos = (CommandInfo[])cmdList.toArray(cmdInfos);

	return cmdInfos;
    }

    /**
     * Put the commands that are in the hash table, into the list.
     */
    private void appendCmdsToList(Map typeHash, List cmdList) {
	Iterator verb_enum = typeHash.keySet().iterator();

	while (verb_enum.hasNext()) {
	    String verb = (String)verb_enum.next();
	    List cmdList2 = (List)typeHash.get(verb);
	    Iterator cmd_enum = ((List)cmdList2).iterator();

	    while (cmd_enum.hasNext()) {
		String cmd = (String)cmd_enum.next();
		cmdList.add(new CommandInfo(verb, cmd));
		// cmdList.add(0, new CommandInfo(verb, cmd));
	    }
	}
    }

    /**
     * Get the command corresponding to <code>cmdName</code> for the MIME type.
     *
     * @param mimeType	the MIME type
     * @param cmdName	the command name
     * @return the CommandInfo object corresponding to the command.
     */
    public synchronized CommandInfo getCommand(String mimeType,
							String cmdName) {
	if (mimeType != null)
	    mimeType = mimeType.toLowerCase(Locale.ENGLISH);

	for (int i = 0; i < DB.length; i++) {
	    if (DB[i] == null)
		continue;
	    Map cmdMap = DB[i].getMailcapList(mimeType);
	    if (cmdMap != null) {
		// get the cmd list for the cmd
		List v = (List)cmdMap.get(cmdName);
		if (v != null) {
		    String cmdClassName = (String)v.get(0);

		    if (cmdClassName != null)
			return new CommandInfo(cmdName, cmdClassName);
		}
	    }
	}

	// now try the fallback list
	for (int i = 0; i < DB.length; i++) {
	    if (DB[i] == null)
		continue;
	    Map cmdMap = DB[i].getMailcapFallbackList(mimeType);
	    if (cmdMap != null) {
		// get the cmd list for the cmd
		List v = (List)cmdMap.get(cmdName);
		if (v != null) {
		    String cmdClassName = (String)v.get(0);

		    if (cmdClassName != null)
			return new CommandInfo(cmdName, cmdClassName);
		}
	    }
	}
	return null;
    }

    /**
     * Add entries to the registry.  Programmatically 
     * added entries are searched before other entries.<p>
     *
     * The string that is passed in should be in mailcap
     * format.
     *
     * @param mail_cap a correctly formatted mailcap string
     */
    public synchronized void addMailcap(String mail_cap) {
	// check to see if one exists
	LogSupport.log("MailcapCommandMap: add to PROG");
	if (DB[PROG] == null)
	    DB[PROG] = new MailcapFile();

	DB[PROG].appendToMailcap(mail_cap);
    }

    /**
     * Return the DataContentHandler for the specified MIME type.
     *
     * @param mimeType	the MIME type
     * @return		the DataContentHandler
     */
    public synchronized DataContentHandler createDataContentHandler(
							String mimeType) {
	if (LogSupport.isLoggable())
	    LogSupport.log(
		"MailcapCommandMap: createDataContentHandler for " + mimeType);
	if (mimeType != null)
	    mimeType = mimeType.toLowerCase(Locale.ENGLISH);

	for (int i = 0; i < DB.length; i++) {
	    if (DB[i] == null)
		continue;
	    if (LogSupport.isLoggable())
		LogSupport.log("  search DB #" + i);
	    Map cmdMap = DB[i].getMailcapList(mimeType);
	    if (cmdMap != null) {
		List v = (List)cmdMap.get("content-handler");
		if (v != null) {
		    String name = (String)v.get(0);
		    DataContentHandler dch = getDataContentHandler(name);
		    if (dch != null)
			return dch;
		}
	    }
	}

	// now try the fallback entries
	for (int i = 0; i < DB.length; i++) {
	    if (DB[i] == null)
		continue;
	    if (LogSupport.isLoggable())
		LogSupport.log("  search fallback DB #" + i);
	    Map cmdMap = DB[i].getMailcapFallbackList(mimeType);
	    if (cmdMap != null) {
		List v = (List)cmdMap.get("content-handler");
		if (v != null) {
		    String name = (String)v.get(0);
		    DataContentHandler dch = getDataContentHandler(name);
		    if (dch != null)
			return dch;
		}
	    }
	}
	return null;
    }

    private DataContentHandler getDataContentHandler(String name) {
	if (LogSupport.isLoggable())
	    LogSupport.log("    got content-handler");
	if (LogSupport.isLoggable())
	    LogSupport.log("      class " + name);
	try {
	    ClassLoader cld = null;
	    // First try the "application's" class loader.
	    cld = SecuritySupport.getContextClassLoader();
	    if (cld == null)
		cld = this.getClass().getClassLoader();
	    Class cl = null;
	    try {
		cl = cld.loadClass(name);
	    } catch (Exception ex) {
		// if anything goes wrong, do it the old way
		cl = Class.forName(name);
	    }
	    if (cl != null)		// XXX - always true?
		return (DataContentHandler)cl.newInstance();
	} catch (IllegalAccessException e) {
	    if (LogSupport.isLoggable())
		LogSupport.log("Can't load DCH " + name, e);
	} catch (ClassNotFoundException e) {
	    if (LogSupport.isLoggable())
		LogSupport.log("Can't load DCH " + name, e);
	} catch (InstantiationException e) {
	    if (LogSupport.isLoggable())
		LogSupport.log("Can't load DCH " + name, e);
	}
	return null;
    }

    /**
     * Get all the MIME types known to this command map.
     *
     * @return		array of MIME types as strings
     * @since	JAF 1.1
     */
    public synchronized String[] getMimeTypes() {
	List mtList = new ArrayList();

	for (int i = 0; i < DB.length; i++) {
	    if (DB[i] == null)
		continue;
	    String[] ts = DB[i].getMimeTypes();
	    if (ts != null) {
		for (int j = 0; j < ts.length; j++) {
		    // eliminate duplicates
		    if (!mtList.contains(ts[j]))
			mtList.add(ts[j]);
		}
	    }
	}

	String[] mts = new String[mtList.size()];
	mts = (String[])mtList.toArray(mts);

	return mts;
    }

    /**
     * Get the native commands for the given MIME type.
     * Returns an array of strings where each string is
     * an entire mailcap file entry.  The application
     * will need to parse the entry to extract the actual
     * command as well as any attributes it needs. See
     * <A HREF="http://www.ietf.org/rfc/rfc1524.txt">RFC 1524</A>
     * for details of the mailcap entry syntax.  Only mailcap
     * entries that specify a view command for the specified
     * MIME type are returned.
     *
     * @param	mimeType	the MIME type
     * @return		array of native command entries
     * @since	JAF 1.1
     */
    public synchronized String[] getNativeCommands(String mimeType) {
	List cmdList = new ArrayList();
	if (mimeType != null)
	    mimeType = mimeType.toLowerCase(Locale.ENGLISH);

	for (int i = 0; i < DB.length; i++) {
	    if (DB[i] == null)
		continue;
	    String[] cmds = DB[i].getNativeCommands(mimeType);
	    if (cmds != null) {
		for (int j = 0; j < cmds.length; j++) {
		    // eliminate duplicates
		    if (!cmdList.contains(cmds[j]))
			cmdList.add(cmds[j]);
		}
	    }
	}

	String[] cmds = new String[cmdList.size()];
	cmds = (String[])cmdList.toArray(cmds);

	return cmds;
    }

    /**
     * for debugging...
     *
    public static void main(String[] argv) throws Exception {
	MailcapCommandMap map = new MailcapCommandMap();
	CommandInfo[] cmdInfo;

	cmdInfo = map.getPreferredCommands(argv[0]);
	System.out.println("Preferred Commands:");
	for (int i = 0; i < cmdInfo.length; i++)
	    System.out.println("Command " + cmdInfo[i].getCommandName() + " [" +
					    cmdInfo[i].getCommandClass() + "]");
	cmdInfo = map.getAllCommands(argv[0]);
	System.out.println();
	System.out.println("All Commands:");
	for (int i = 0; i < cmdInfo.length; i++)
	    System.out.println("Command " + cmdInfo[i].getCommandName() + " [" +
					    cmdInfo[i].getCommandClass() + "]");
	DataContentHandler dch = map.createDataContentHandler(argv[0]);
	if (dch != null)
	    System.out.println("DataContentHandler " +
						dch.getClass().toString());
	System.exit(0);
    }
    */
}
