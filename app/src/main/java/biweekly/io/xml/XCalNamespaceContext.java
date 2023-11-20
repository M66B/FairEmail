package biweekly.io.xml;

import java.util.Collections;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;

/*
 Copyright (c) 2013-2023, Michael Angstadt
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met: 

 1. Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer. 
 2. Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution. 

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * Used for xCal xpath expressions.
 * @see XPath#setNamespaceContext(NamespaceContext)
 * @author Michael Angstadt
 */
public class XCalNamespaceContext implements NamespaceContext {
	/**
	 * The XML namespace for xCal documents.
	 */
	public static final String XCAL_NS = "urn:ietf:params:xml:ns:icalendar-2.0";

	private final String prefix;

	/**
	 * Creates a new namespace context.
	 * @param prefix the prefix to use in xpath expressions
	 */
	public XCalNamespaceContext(String prefix) {
		this.prefix = prefix;
	}

	/**
	 * Gets the prefix to use in xpath expressions.
	 * @return the xpath prefix
	 */
	public String getPrefix() {
		return prefix;
	}

	//@Override
	public String getNamespaceURI(String prefix) {
		if (this.prefix.equals(prefix)) {
			return XCAL_NS;
		}
		return null;
	}

	//@Override
	public String getPrefix(String ns) {
		if (XCAL_NS.equals(ns)) {
			return prefix;
		}
		return null;
	}

	//@Override
	public Iterator<String> getPrefixes(String ns) {
		return XCAL_NS.equals(ns) ? Collections.singletonList(prefix).iterator() : null;
	}
}