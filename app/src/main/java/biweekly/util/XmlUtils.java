package biweekly.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

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

 The views and conclusions contained in the software and documentation are those
 of the authors and should not be interpreted as representing official policies, 
 either expressed or implied, of the FreeBSD Project.
 */

/**
 * Generic XML utility methods.
 * @author Michael Angstadt
 */
public final class XmlUtils {
	/**
	 * Creates a new XML document.
	 * @return the XML document
	 */
	public static Document createDocument() {
		try {
			DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
			fact.setNamespaceAware(true);
			DocumentBuilder db = fact.newDocumentBuilder();
			return db.newDocument();
		} catch (ParserConfigurationException e) {
			//will probably never be thrown because we're not doing anything fancy with the configuration
			throw new RuntimeException(e);
		}
	}

	/**
	 * Parses an XML string into a DOM.
	 * @param xml the XML string
	 * @return the parsed DOM
	 * @throws SAXException if the string is not valid XML
	 */
	public static Document toDocument(String xml) throws SAXException {
		try {
			return toDocument(new StringReader(xml));
		} catch (IOException e) {
			//reading from string
			throw new RuntimeException(e);
		}
	}

	/**
	 * Parses an XML document from a file.
	 * @param file the file
	 * @return the parsed DOM
	 * @throws SAXException if the XML is not valid
	 * @throws IOException if there is a problem reading from the file
	 */
	public static Document toDocument(File file) throws SAXException, IOException {
		InputStream in = new BufferedInputStream(new FileInputStream(file));
		try {
			return XmlUtils.toDocument(in);
		} finally {
			in.close();
		}
	}

	/**
	 * Parses an XML document from an input stream.
	 * @param in the input stream
	 * @return the parsed DOM
	 * @throws SAXException if the XML is not valid
	 * @throws IOException if there is a problem reading from the input stream
	 */
	public static Document toDocument(InputStream in) throws SAXException, IOException {
		return toDocument(new InputSource(in));
	}

	/**
	 * <p>
	 * Parses an XML document from a reader.
	 * </p>
	 * <p>
	 * Note that use of this method is discouraged. It ignores the character
	 * encoding that is defined within the XML document itself, and should only
	 * be used if the encoding is undefined or if the encoding needs to be
	 * ignored for whatever reason. The {@link #toDocument(InputStream)} method
	 * should be used instead, since it takes the XML document's character
	 * encoding into account when parsing.
	 * </p>
	 * @param reader the reader
	 * @return the parsed DOM
	 * @throws SAXException if the XML is not valid
	 * @throws IOException if there is a problem reading from the reader
	 * @see <a
	 * href="http://stackoverflow.com/q/3482494/13379">http://stackoverflow.com/q/3482494/13379</a>
	 */
	public static Document toDocument(Reader reader) throws SAXException, IOException {
		return toDocument(new InputSource(reader));
	}

	private static Document toDocument(InputSource in) throws SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setIgnoringComments(true);
		applyXXEProtection(factory);

		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			//should never be thrown because we're not doing anything fancy with the configuration
			throw new RuntimeException(e);
		}

		return builder.parse(in);
	}

	/**
	 * Configures a {@link DocumentBuilderFactory} to protect it against XML
	 * External Entity attacks.
	 * @param factory the factory
	 * @see <a href=
	 * "https://www.owasp.org/index.php/XML_External_Entity_%28XXE%29_Prevention_Cheat_Sheet#Java">
	 * XXE Cheat Sheet</a>
	 */
	public static void applyXXEProtection(DocumentBuilderFactory factory) {
		Map<String, Boolean> features = new HashMap<String, Boolean>();
		features.put("http://apache.org/xml/features/disallow-doctype-decl", true);
		features.put("http://xml.org/sax/features/external-general-entities", false);
		features.put("http://xml.org/sax/features/external-parameter-entities", false);
		features.put("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

		for (Map.Entry<String, Boolean> entry : features.entrySet()) {
			String feature = entry.getKey();
			Boolean value = entry.getValue();
			try {
				factory.setFeature(feature, value);
			} catch (ParserConfigurationException e) {
				//feature is not supported by the local XML engine, skip it
			}
		}

		factory.setXIncludeAware(false);
		factory.setExpandEntityReferences(false);
	}

	/**
	 * Configures a {@link TransformerFactory} to protect it against XML
	 * External Entity attacks.
	 * @param factory the factory
	 * @see <a href=
	 * "https://www.owasp.org/index.php/XML_External_Entity_%28XXE%29_Prevention_Cheat_Sheet#Java">
	 * XXE Cheat Sheet</a>
	 */
	public static void applyXXEProtection(TransformerFactory factory) {
		//@formatter:off
		String[] attributes = {
			//XMLConstants.ACCESS_EXTERNAL_DTD (Java 7 only)
			"http://javax.xml.XMLConstants/property/accessExternalDTD",

			//XMLConstants.ACCESS_EXTERNAL_STYLESHEET (Java 7 only)
			"http://javax.xml.XMLConstants/property/accessExternalStylesheet"
		};
		//@formatter:on

		for (String attribute : attributes) {
			try {
				factory.setAttribute(attribute, "");
			} catch (IllegalArgumentException e) {
				//attribute is not supported by the local XML engine, skip it
			}
		}
	}

	/**
	 * Converts an XML node to a string.
	 * @param node the XML node
	 * @return the string
	 */
	public static String toString(Node node) {
		return toString(node, new HashMap<String, String>());
	}

	/**
	 * Converts an XML node to a string.
	 * @param node the XML node
	 * @param prettyPrint true to pretty print, false not to
	 * @return the string
	 */
	public static String toString(Node node, boolean prettyPrint) {
		Map<String, String> properties = new HashMap<String, String>();
		if (prettyPrint) {
			properties.put(OutputKeys.INDENT, "yes");
			properties.put("{http://xml.apache.org/xslt}indent-amount", "2");
		}
		return toString(node, properties);
	}

	/**
	 * Converts an XML node to a string.
	 * @param node the XML node
	 * @param outputProperties the output properties
	 * @return the string
	 */
	public static String toString(Node node, Map<String, String> outputProperties) {
		try {
			StringWriter writer = new StringWriter();
			toWriter(node, writer, outputProperties);
			return writer.toString();
		} catch (TransformerException e) {
			//should never be thrown because we're writing to string
			throw new RuntimeException(e);
		}
	}

	/**
	 * Writes an XML node to a writer.
	 * @param node the XML node
	 * @param writer the writer
	 * @throws TransformerException if there's a problem writing to the writer
	 */
	public static void toWriter(Node node, Writer writer) throws TransformerException {
		toWriter(node, writer, new HashMap<String, String>());
	}

	/**
	 * Writes an XML node to a writer.
	 * @param node the XML node
	 * @param writer the writer
	 * @param outputProperties the output properties
	 * @throws TransformerException if there's a problem writing to the writer
	 */
	public static void toWriter(Node node, Writer writer, Map<String, String> outputProperties) throws TransformerException {
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			for (Map.Entry<String, String> property : outputProperties.entrySet()) {
				try {
					transformer.setOutputProperty(property.getKey(), property.getValue());
				} catch (IllegalArgumentException e) {
					//ignore invalid output properties
				}
			}

			DOMSource source = new DOMSource(node);
			StreamResult result = new StreamResult(writer);
			transformer.transform(source, result);
		} catch (TransformerConfigurationException e) {
			//no complex configurations
		} catch (TransformerFactoryConfigurationError e) {
			//no complex configurations
		}
	}

	/**
	 * Gets all the elements out of a {@link NodeList}.
	 * @param nodeList the node list
	 * @return the elements
	 */
	public static List<Element> toElementList(NodeList nodeList) {
		List<Element> elements = new ArrayList<Element>();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node instanceof Element) {
				elements.add((Element) node);
			}
		}
		return elements;
	}

	/**
	 * Gets the first child element of an element.
	 * @param parent the parent element
	 * @return the first child element or null if there are no child elements
	 */
	public static Element getFirstChildElement(Element parent) {
		return getFirstChildElement((Node) parent);
	}

	/**
	 * Gets the first child element of a node.
	 * @param parent the node
	 * @return the first child element or null if there are no child elements
	 */
	private static Element getFirstChildElement(Node parent) {
		NodeList nodeList = parent.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node instanceof Element) {
				return (Element) node;
			}
		}
		return null;
	}

	/**
	 * Determines if a node has a particular qualified name.
	 * @param node the node
	 * @param qname the qualified name
	 * @return true if the node has the given qualified name, false if not
	 */
	public static boolean hasQName(Node node, QName qname) {
		return qname.getNamespaceURI().equals(node.getNamespaceURI()) && qname.getLocalPart().equals(node.getLocalName());
	}

	private XmlUtils() {
		//hide
	}
}
