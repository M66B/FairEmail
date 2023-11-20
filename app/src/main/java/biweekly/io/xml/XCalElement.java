package biweekly.io.xml;

import static biweekly.io.xml.XCalNamespaceContext.XCAL_NS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import biweekly.ICalDataType;
import biweekly.util.XmlUtils;

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
 * Wraps xCal functionality around an XML {@link Element} object.
 * @author Michael Angstadt
 */
public class XCalElement {
	private final Element element;
	private final Document document;

	/**
	 * Creates a new xCal element.
	 * @param element the XML element to wrap
	 */
	public XCalElement(Element element) {
		this.element = element;
		document = element.getOwnerDocument();
	}

	/**
	 * Gets the first value of the given data type.
	 * @param dataType the data type to look for or null for the "unknown" data
	 * type
	 * @return the value or null if not found
	 */
	public String first(ICalDataType dataType) {
		String dataTypeStr = toLocalName(dataType);
		return first(dataTypeStr);
	}

	/**
	 * Gets the value of the first child element with the given name.
	 * @param localName the name of the element
	 * @return the element's text or null if not found
	 */
	public String first(String localName) {
		for (Element child : children()) {
			if (localName.equals(child.getLocalName()) && XCAL_NS.equals(child.getNamespaceURI())) {
				return child.getTextContent();
			}
		}
		return null;
	}

	/**
	 * Gets all the values of a given data type.
	 * @param dataType the data type to look for or null for the "unknown" data
	 * type
	 * @return the values
	 */
	public List<String> all(ICalDataType dataType) {
		String dataTypeStr = toLocalName(dataType);
		return all(dataTypeStr);
	}

	/**
	 * Gets the values of all child elements that have the given name.
	 * @param localName the element name
	 * @return the values of the child elements
	 */
	public List<String> all(String localName) {
		List<String> childrenText = new ArrayList<String>();
		for (Element child : children()) {
			if (localName.equals(child.getLocalName()) && XCAL_NS.equals(child.getNamespaceURI())) {
				String text = child.getTextContent();
				childrenText.add(text);
			}
		}
		return childrenText;
	}

	/**
	 * Adds a value.
	 * @param dataType the data type or null for the "unknown" data type
	 * @param value the value
	 * @return the created element
	 */
	public Element append(ICalDataType dataType, String value) {
		String dataTypeStr = toLocalName(dataType);
		return append(dataTypeStr, value);
	}

	/**
	 * Adds a child element.
	 * @param name the name of the child element
	 * @param value the value of the child element.
	 * @return the created element
	 */
	public Element append(String name, String value) {
		Element child = document.createElementNS(XCAL_NS, name);
		child.setTextContent(value);
		element.appendChild(child);
		return child;
	}

	/**
	 * Adds a child element.
	 * @param name the name of the child element
	 * @return the created element
	 */
	public XCalElement append(String name) {
		return new XCalElement(append(name, (String) null));
	}

	/**
	 * Adds an empty value.
	 * @param dataType the data type
	 * @return the created element
	 */
	public XCalElement append(ICalDataType dataType) {
		return append(dataType.getName().toLowerCase());
	}

	/**
	 * Adds multiple child elements, each with the same name.
	 * @param name the name for all the child elements
	 * @param values the values of each child element
	 * @return the created elements
	 */
	public List<Element> append(String name, Collection<String> values) {
		List<Element> elements = new ArrayList<Element>(values.size());
		for (String value : values) {
			elements.add(append(name, value));
		}
		return elements;
	}

	/**
	 * Gets the owner document.
	 * @return the owner document
	 */
	public Document document() {
		return document;
	}

	/**
	 * Gets the wrapped XML element.
	 * @return the wrapped XML element
	 */
	public Element getElement() {
		return element;
	}

	/**
	 * Gets the child elements of the wrapped XML element.
	 * @return the child elements
	 */
	private List<Element> children() {
		return XmlUtils.toElementList(element.getChildNodes());
	}

	/**
	 * Gets all child elements with the given data type.
	 * @param dataType the data type
	 * @return the child elements
	 */
	public List<XCalElement> children(ICalDataType dataType) {
		String localName = dataType.getName().toLowerCase();
		List<XCalElement> children = new ArrayList<XCalElement>();
		for (Element child : children()) {
			if (localName.equals(child.getLocalName()) && XCAL_NS.equals(child.getNamespaceURI())) {
				children.add(new XCalElement(child));
			}
		}
		return children;
	}

	/**
	 * Gets the first child element with the given data type.
	 * @param dataType the data type
	 * @return the child element or null if not found
	 */
	public XCalElement child(ICalDataType dataType) {
		String localName = dataType.getName().toLowerCase();
		for (Element child : children()) {
			if (localName.equals(child.getLocalName()) && XCAL_NS.equals(child.getNamespaceURI())) {
				return new XCalElement(child);
			}
		}
		return null;
	}

	/**
	 * Finds the first child element that has the xCard namespace and returns
	 * its data type and value. If no such element is found, the parent
	 * {@link XCalElement}'s text content, along with a null data type, is
	 * returned.
	 * @return the value and data type
	 */
	public XCalValue firstValue() {
		for (Element child : children()) {
			String childNamespace = child.getNamespaceURI();
			if (XCAL_NS.equals(childNamespace)) {
				ICalDataType dataType = toDataType(child.getLocalName());
				String value = child.getTextContent();
				return new XCalValue(dataType, value);
			}
		}

		return new XCalValue(null, element.getTextContent());
	}

	/**
	 * Gets the appropriate XML local name of a {@link ICalDataType} object.
	 * @param dataType the data type or null for "unknown"
	 * @return the local name (e.g. "text")
	 */
	private String toLocalName(ICalDataType dataType) {
		return (dataType == null) ? "unknown" : dataType.getName().toLowerCase();
	}

	/**
	 * Converts an XML local name to the appropriate {@link ICalDataType}
	 * object.
	 * @param localName the local name (e.g. "text")
	 * @return the data type or null for "unknown"
	 */
	private static ICalDataType toDataType(String localName) {
		return "unknown".equals(localName) ? null : ICalDataType.get(localName);
	}

	/**
	 * Represents the data type and value of a child element under an
	 * {@link XCalElement}.
	 */
	public static class XCalValue {
		private final ICalDataType dataType;
		private final String value;

		/**
		 * @param dataType the data type or null if "unknown"
		 * @param value the value
		 */
		public XCalValue(ICalDataType dataType, String value) {
			this.dataType = dataType;
			this.value = value;
		}

		/**
		 * Gets the data type
		 * @return the data type or null if "unknown"
		 */
		public ICalDataType getDataType() {
			return dataType;
		}

		/**
		 * Get the value.
		 * @return the value
		 */
		public String getValue() {
			return value;
		}
	}
}
