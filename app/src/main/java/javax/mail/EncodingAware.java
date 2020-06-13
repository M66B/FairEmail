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

package javax.mail;

/**
 * A {@link javax.activation.DataSource DataSource} that also implements
 * <code>EncodingAware</code> may specify the Content-Transfer-Encoding
 * to use for its data.  Valid Content-Transfer-Encoding values specified
 * by RFC 2045 are "7bit", "8bit", "quoted-printable", "base64", and "binary".
 * <p>
 * For example, a {@link javax.activation.FileDataSource FileDataSource}
 * could be created that forces all files to be base64 encoded:
 * <blockquote><pre>
 *  public class Base64FileDataSource extends FileDataSource
 *					implements EncodingAware {
 *	public Base64FileDataSource(File file) {
 *	    super(file);
 *	}
 *
 *	// implements EncodingAware.getEncoding()
 *	public String getEncoding() {
 *	    return "base64";
 *	}
 *  }
 * </pre></blockquote><p>
 *
 * @since	JavaMail 1.5
 * @author	Bill Shannon
 */

public interface EncodingAware {

    /**
     * Return the MIME Content-Transfer-Encoding to use for this data,
     * or null to indicate that an appropriate value should be chosen
     * by the caller.
     *
     * @return		the Content-Transfer-Encoding value, or null
     */
    public String getEncoding();
}
