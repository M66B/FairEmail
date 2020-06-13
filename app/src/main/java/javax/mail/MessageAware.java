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
 * An interface optionally implemented by <code>DataSources</code> to
 * supply information to a <code>DataContentHandler</code> about the
 * message context in which the data content object is operating.
 *
 * @see javax.mail.MessageContext
 * @see javax.activation.DataSource
 * @see javax.activation.DataContentHandler
 * @since	JavaMail 1.1
 */
public interface MessageAware {
    /**
     * Return the message context.
     *
     * @return	the message context
     */
    public MessageContext getMessageContext();
}
