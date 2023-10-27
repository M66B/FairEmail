package biweekly.io.xml;

import java.util.HashMap;

import javax.xml.transform.OutputKeys;

import biweekly.Messages;

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
 * Helper class for setting commonly-used JAXP output properties.
 * @author Michael Angstadt
 */
public class XCalOutputProperties extends HashMap<String, String> {
	private static final long serialVersionUID = -1038397031136827278L;
	private static final String INDENT_AMT = "{http://xml.apache.org/xslt}indent-amount";

	public XCalOutputProperties() {
		put(OutputKeys.METHOD, "xml");
	}

	/**
	 * @param indent the number of indent spaces to use for pretty-printing or
	 * null to disable pretty-printing (disabled by default)
	 * @param xmlVersion the XML version to use (defaults to "1.0") (Note: Many
	 * JDKs only support 1.0 natively. For XML 1.1 support, add a JAXP library
	 * like <a href=
	 * "http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22xalan%22%20AND%20a%3A%22xalan%22"
	 * >xalan</a> to your project)
	 * @throws IllegalArgumentException if the indent amount is less than zero
	 */
	public XCalOutputProperties(Integer indent, String xmlVersion) {
		this();
		setIndent(indent);
		setXmlVersion(xmlVersion);
	}

	/**
	 * Gets the number of indent spaces to use for pretty-printing.
	 * @return the number of indent spaces or null if pretty-printing is
	 * disabled
	 */
	public Integer getIndent() {
		if (!"yes".equals(get(OutputKeys.INDENT))) {
			return null;
		}

		String value = get(INDENT_AMT);
		return (value == null) ? null : Integer.valueOf(value);
	}

	/**
	 * Sets the number of indent spaces to use for pretty-printing (disabled by
	 * default).
	 * @param indent the number of indent spaces to use or null to disable
	 * pretty-printing
	 * @throws IllegalArgumentException if the indent amount is less than zero
	 */
	public void setIndent(Integer indent) {
		if (indent == null) {
			remove(OutputKeys.INDENT);
			remove(INDENT_AMT);
			return;
		}

		if (indent < 0) {
			throw Messages.INSTANCE.getIllegalArgumentException(11);
		}

		put(OutputKeys.INDENT, "yes");
		put(INDENT_AMT, indent.toString());
	}

	/**
	 * Gets the XML version to use.
	 * @return the XML version or null if not set
	 */
	public String getXmlVersion() {
		return get(OutputKeys.VERSION);
	}

	/**
	 * <p>
	 * Sets the XML version to use (defaults to "1.0").
	 * </p>
	 * <p>
	 * Note: Many JDKs only support 1.0 natively. For XML 1.1 support, add a
	 * JAXP library like <a href=
	 * "http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22xalan%22%20AND%20a%3A%22xalan%22"
	 * >xalan</a> to your project.
	 * </p>
	 * @param version the XML version or null to remove
	 */
	public void setXmlVersion(String version) {
		if (version == null) {
			remove(OutputKeys.VERSION);
			return;
		}

		put(OutputKeys.VERSION, version);
	}
}
