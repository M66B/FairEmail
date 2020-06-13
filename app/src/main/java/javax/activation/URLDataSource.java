/*
 * Copyright (c) 1997, 2019 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package javax.activation;

import java.net.URL;
import java.net.URLConnection;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
 * The URLDataSource class provides an object that wraps a <code>URL</code>
 * object in a DataSource interface. URLDataSource simplifies the handling
 * of data described by URLs within Jakarta Activation
 * because this class can be used to create new DataHandlers. <i>NOTE: The
 * DataHandler object creates a URLDataSource internally,
 * when it is constructed with a URL.</i>
 *
 * @see javax.activation.DataSource
 * @see javax.activation.DataHandler
 */
public class URLDataSource implements DataSource {
    private URL url = null;
    private URLConnection url_conn = null;

    /**
     * URLDataSource constructor. The URLDataSource class will
     * not open a connection to the URL until a method requiring it
     * to do so is called.
     *
     * @param url The URL to be encapsulated in this object.
     */
    public URLDataSource(URL url) {
	this.url = url;
    }

    /**
     * Returns the value of the URL content-type header field.
     * It calls the URL's <code>URLConnection.getContentType</code> method
     * after retrieving a URLConnection object.
     * <i>Note: this method attempts to call the <code>openConnection</code>
     * method on the URL. If this method fails, or if a content type is not
     * returned from the URLConnection, getContentType returns
     * "application/octet-stream" as the content type.</i>
     *
     * @return the content type.
     */
    public String getContentType() {
	String type = null;

	try {
	    if (url_conn == null)
		url_conn = url.openConnection();
	} catch (IOException e) { }
	
	if (url_conn != null)
	    type = url_conn.getContentType();

	if (type == null)
	    type = "application/octet-stream";
	
	return type;
    }

    /**
     * Calls the <code>getFile</code> method on the URL used to
     * instantiate the object.
     *
     * @return the result of calling the URL's getFile method.
     */
    public String getName() {
	return url.getFile();
    }

    /**
     * The getInputStream method from the URL. Calls the
     * <code>openStream</code> method on the URL.
     *
     * @return the InputStream.
     */
    public InputStream getInputStream() throws IOException {
	return url.openStream();
    }

    /**
     * The getOutputStream method from the URL. First an attempt is
     * made to get the URLConnection object for the URL. If that
     * succeeds, the getOutputStream method on the URLConnection
     * is returned.
     *
     * @return the OutputStream.
     */
    public OutputStream getOutputStream() throws IOException {
	// get the url connection if it is available
	url_conn = url.openConnection();
	
	if (url_conn != null) {
	    url_conn.setDoOutput(true);
	    return url_conn.getOutputStream();
	} else
	    return null;
    }

    /**
     * Return the URL used to create this DataSource.
     *
     * @return The URL.
     */
    public URL getURL() {
	return url;
    }
}
