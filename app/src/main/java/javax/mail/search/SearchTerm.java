/*
 * Copyright (c) 1997, 2019 Oracle and/or its affiliates. All rights reserved.
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

import java.io.Serializable;

import javax.mail.Message;

/**
 * Search criteria are expressed as a tree of search-terms, forming
 * a parse-tree for the search expression. <p>
 *
 * Search-terms are represented by this class. This is an abstract
 * class; subclasses implement specific match methods. <p>
 *
 * Search terms are serializable, which allows storing a search term
 * between sessions.
 *
 * <strong>Warning:</strong>
 * Serialized objects of this class may not be compatible with future
 * Jakarta Mail API releases.  The current serialization support is
 * appropriate for short term storage. <p>
 *
 * @author Bill Shannon
 * @author John Mani
 */
public abstract class SearchTerm implements Serializable {

    private static final long serialVersionUID = -6652358452205992789L;

    /**
     * This method applies a specific match criterion to the given
     * message and returns the result.
     *
     * @param msg	The match criterion is applied on this message
     * @return		true, it the match succeeds, false if the match fails
     */

    public abstract boolean match(Message msg);
}
