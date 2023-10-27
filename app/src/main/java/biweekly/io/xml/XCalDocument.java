package biweekly.io.xml;

import static biweekly.io.xml.XCalNamespaceContext.XCAL_NS;
import static biweekly.io.xml.XCalQNames.COMPONENTS;
import static biweekly.io.xml.XCalQNames.ICALENDAR;
import static biweekly.io.xml.XCalQNames.PARAMETERS;
import static biweekly.io.xml.XCalQNames.PROPERTIES;
import static biweekly.io.xml.XCalQNames.VCALENDAR;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import biweekly.ICalDataType;
import biweekly.ICalVersion;
import biweekly.ICalendar;
import biweekly.component.ICalComponent;
import biweekly.component.VTimezone;
import biweekly.io.CannotParseException;
import biweekly.io.ParseWarning;
import biweekly.io.SkipMeException;
import biweekly.io.StreamReader;
import biweekly.io.StreamWriter;
import biweekly.io.scribe.ScribeIndex;
import biweekly.io.scribe.component.ICalComponentScribe;
import biweekly.io.scribe.component.ICalendarScribe;
import biweekly.io.scribe.property.ICalPropertyScribe;
import biweekly.parameter.ICalParameters;
import biweekly.property.ICalProperty;
import biweekly.property.Version;
import biweekly.property.Xml;
import biweekly.util.Utf8Writer;
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

//@formatter:off
/**
 * <p>
 * Represents an XML document that contains iCalendar objects ("xCal" standard).
 * This class can be used to read and write xCal documents.
 * </p>
 * <p>
 * <b>Examples:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * String xml =
 * "&lt;?xml version=\"1.0\" encoding=\"utf-8\" ?&gt;" +
 * "&lt;icalendar xmlns=\"urn:ietf:params:xml:ns:icalendar-2.0\"&gt;" +
 *   "&lt;vcalendar&gt;" +
 *     "&lt;properties&gt;" +
 *       "&lt;prodid&gt;&lt;text&gt;-//Example Inc.//Example Client//EN&lt;/text&gt;&lt;/prodid&gt;" +
 *       "&lt;version&gt;&lt;text&gt;2.0&lt;/text&gt;&lt;/version&gt;" +
 *     "&lt;/properties&gt;" +
 *     "&lt;components&gt;" +
 *       "&lt;vevent&gt;" +
 *         "&lt;properties&gt;" +
 *           "&lt;dtstart&gt;&lt;date-time&gt;2013-06-27T13:00:00Z&lt;/date-time&gt;&lt;/dtstart&gt;" +
 *           "&lt;dtend&gt;&lt;date-time&gt;2013-06-27T15:00:00Z&lt;/date-time&gt;&lt;/dtend&gt;" +
 *           "&lt;summary&gt;&lt;text&gt;Team Meeting&lt;/text&gt;&lt;/summary&gt;" +
 *         "&lt;/properties&gt;" +
 *       "&lt;/vevent&gt;" +
 *     "&lt;/components&gt;" +
 *   "&lt;/vcalendar&gt;" +
 * "&lt;/icalendar&gt;";
 *     
 * //parsing an existing xCal document
 * XCalDocument xcal = new XCalDocument(xml);
 * List&lt;ICalendar&gt; icals = xcal.getICalendars();
 * 
 * //creating an empty xCal document
 * XCalDocument xcal = new XCalDocument();
 * 
 * //ICalendar objects can be added at any time
 * ICalendar ical = new ICalendar();
 * xcal.addICalendar(ical);
 * 
 * //retrieving the raw XML DOM
 * Document document = xcal.getDocument();
 * 
 * //call one of the "write()" methods to output the xCal document
 * File file = new File("meeting.xml");
 * xcal.write(file);
 * </pre>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc6321">RFC 6321</a>
 */
//@formatter:on
public class XCalDocument {
	private static final ICalendarScribe icalMarshaller = ScribeIndex.getICalendarScribe();
	private static final XCalNamespaceContext nsContext = new XCalNamespaceContext("xcal");

	private final Document document;
	private Element icalendarRootElement;

	/**
	 * Parses an xCal document from a string.
	 * @param xml the xCal document in the form of a string
	 * @throws SAXException if there's a problem parsing the XML
	 */
	public XCalDocument(String xml) throws SAXException {
		this(XmlUtils.toDocument(xml));
	}

	/**
	 * Parses an xCal document from an input stream.
	 * @param in the input stream to read the the xCal document from
	 * @throws IOException if there's a problem reading from the input stream
	 * @throws SAXException if there's a problem parsing the XML
	 */
	public XCalDocument(InputStream in) throws SAXException, IOException {
		this(XmlUtils.toDocument(in));
	}

	/**
	 * Parses an xCal document from a file.
	 * @param file the file containing the xCal document
	 * @throws IOException if there's a problem reading from the file
	 * @throws SAXException if there's a problem parsing the XML
	 */
	public XCalDocument(File file) throws SAXException, IOException {
		this(XmlUtils.toDocument(file));
	}

	/**
	 * <p>
	 * Parses an xCal document from a reader.
	 * </p>
	 * <p>
	 * Note that use of this constructor is discouraged. It ignores the
	 * character encoding that is defined within the XML document itself, and
	 * should only be used if the encoding is undefined or if the encoding needs
	 * to be ignored for whatever reason. The {@link #XCalDocument(InputStream)}
	 * constructor should be used instead, since it takes the XML document's
	 * character encoding into account when parsing.
	 * </p>
	 * @param reader the reader to read the xCal document from
	 * @throws IOException if there's a problem reading from the reader
	 * @throws SAXException if there's a problem parsing the XML
	 */
	public XCalDocument(Reader reader) throws SAXException, IOException {
		this(XmlUtils.toDocument(reader));
	}

	/**
	 * Wraps an existing XML DOM object.
	 * @param document the XML DOM that contains the xCal document
	 */
	public XCalDocument(Document document) {
		this.document = document;

		XPath xpath = XPathFactory.newInstance().newXPath();
		xpath.setNamespaceContext(nsContext);

		try {
			//find the <icalendar> element
			String prefix = nsContext.getPrefix();
			icalendarRootElement = (Element) xpath.evaluate("//" + prefix + ":" + ICALENDAR.getLocalPart(), document, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			//never thrown, xpath expression is hard coded
		}
	}

	/**
	 * Creates an empty xCal document.
	 */
	public XCalDocument() {
		document = XmlUtils.createDocument();
		icalendarRootElement = document.createElementNS(ICALENDAR.getNamespaceURI(), ICALENDAR.getLocalPart());
		document.appendChild(icalendarRootElement);
	}

	/**
	 * Gets the raw XML DOM object.
	 * @return the XML DOM
	 */
	public Document getDocument() {
		return document;
	}

	/**
	 * Parses all iCalendar objects from this XML document.
	 * @return the parsed iCalendar objects
	 */
	public List<ICalendar> getICalendars() {
		try {
			return reader().readAll();
		} catch (IOException e) {
			//not thrown because reading from DOM
			throw new RuntimeException(e);
		}
	}

	/**
	 * Adds an iCalendar object to this XML document.
	 * @param ical the iCalendar object to add
	 */
	public void addICalendar(ICalendar ical) {
		writer().write(ical);
	}

	/**
	 * Creates a {@link StreamReader} object that parses iCalendar objects from
	 * this XML document.
	 * @return the reader
	 */
	public StreamReader reader() {
		return new XCalDocumentStreamReader();
	}

	/**
	 * Creates a {@link StreamWriter} object that adds iCalendar objects to this
	 * XML document.
	 * @return the writer
	 */
	public XCalDocumentStreamWriter writer() {
		return new XCalDocumentStreamWriter();
	}

	/**
	 * Writes the xCal document to a string.
	 * @return the XML string
	 */
	public String write() {
		return write((Integer) null);
	}

	/**
	 * Writes the xCal document to a string.
	 * @param indent the number of indent spaces to use for pretty-printing or
	 * "null" to disable pretty-printing (disabled by default)
	 * @return the XML string
	 */
	public String write(Integer indent) {
		return write(indent, null);
	}

	/**
	 * Writes the xCal document to a string.
	 * @param indent the number of indent spaces to use for pretty-printing or
	 * "null" to disable pretty-printing (disabled by default)
	 * @param xmlVersion the XML version to use (defaults to "1.0") (Note: Many
	 * JDKs only support 1.0 natively. For XML 1.1 support, add a JAXP library
	 * like <a href=
	 * "http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22xalan%22%20AND%20a%3A%22xalan%22"
	 * >xalan</a> to your project)
	 * @return the XML string
	 */
	public String write(Integer indent, String xmlVersion) {
		return write(new XCalOutputProperties(indent, xmlVersion));
	}

	/**
	 * Writes the xCal document to a string.
	 * @param outputProperties properties to assign to the JAXP transformer (see
	 * {@link Transformer#setOutputProperty})
	 * @return the XML string
	 */
	public String write(Map<String, String> outputProperties) {
		StringWriter sw = new StringWriter();
		try {
			write(sw, outputProperties);
		} catch (TransformerException e) {
			//shouldn't be thrown because we're writing to a string
			throw new RuntimeException(e);
		}
		return sw.toString();
	}

	/**
	 * Writes the xCal document to an output stream.
	 * @param out the output stream to write to (UTF-8 encoding will be used)
	 * @throws TransformerException if there's a problem writing to the output
	 * stream
	 */
	public void write(OutputStream out) throws TransformerException {
		write(out, (Integer) null);
	}

	/**
	 * Writes the xCal document to an output stream.
	 * @param out the output stream to write to (UTF-8 encoding will be used)
	 * @param indent the number of indent spaces to use for pretty-printing or
	 * "null" to disable pretty-printing (disabled by default)
	 * @throws TransformerException if there's a problem writing to the output
	 * stream
	 */
	public void write(OutputStream out, Integer indent) throws TransformerException {
		write(out, indent, null);
	}

	/**
	 * Writes the xCal document to an output stream.
	 * @param out the output stream to write to (UTF-8 encoding will be used)
	 * @param indent the number of indent spaces to use for pretty-printing or
	 * "null" to disable pretty-printing (disabled by default)
	 * @param xmlVersion the XML version to use (defaults to "1.0") (Note: Many
	 * JDKs only support 1.0 natively. For XML 1.1 support, add a JAXP library
	 * like <a href=
	 * "http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22xalan%22%20AND%20a%3A%22xalan%22"
	 * >xalan</a> to your project)
	 * @throws TransformerException if there's a problem writing to the output
	 * stream
	 */
	public void write(OutputStream out, Integer indent, String xmlVersion) throws TransformerException {
		write(out, new XCalOutputProperties(indent, xmlVersion));
	}

	/**
	 * Writes the xCal document to an output stream.
	 * @param out the output stream to write to (UTF-8 encoding will be used)
	 * @param outputProperties properties to assign to the JAXP transformer (see
	 * {@link Transformer#setOutputProperty})
	 * @throws TransformerException if there's a problem writing to the output
	 * stream
	 */
	public void write(OutputStream out, Map<String, String> outputProperties) throws TransformerException {
		write(new Utf8Writer(out), outputProperties);
	}

	/**
	 * Writes the xCal document to a file.
	 * @param file the file to write to (UTF-8 encoding will be used)
	 * @throws IOException if there's a problem writing to the file
	 * @throws TransformerException if there's a problem writing the XML
	 */
	public void write(File file) throws TransformerException, IOException {
		write(file, (Integer) null);
	}

	/**
	 * Writes the xCal document to a file.
	 * @param file the file to write to (UTF-8 encoding will be used)
	 * @param indent the number of indent spaces to use for pretty-printing or
	 * "null" to disable pretty-printing (disabled by default)
	 * @throws IOException if there's a problem writing to the file
	 * @throws TransformerException if there's a problem writing the XML
	 */
	public void write(File file, Integer indent) throws TransformerException, IOException {
		write(file, indent, null);
	}

	/**
	 * Writes the xCal document to a file.
	 * @param file the file to write to (UTF-8 encoding will be used)
	 * @param indent the number of indent spaces to use for pretty-printing or
	 * "null" to disable pretty-printing (disabled by default)
	 * @param xmlVersion the XML version to use (defaults to "1.0") (Note: Many
	 * JDKs only support 1.0 natively. For XML 1.1 support, add a JAXP library
	 * like <a href=
	 * "http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22xalan%22%20AND%20a%3A%22xalan%22"
	 * >xalan</a> to your project)
	 * @throws IOException if there's a problem writing to the file
	 * @throws TransformerException if there's a problem writing the XML
	 */
	public void write(File file, Integer indent, String xmlVersion) throws TransformerException, IOException {
		write(file, new XCalOutputProperties(indent, xmlVersion));
	}

	/**
	 * Writes the xCal document to a file.
	 * @param file the file to write to (UTF-8 encoding will be used)
	 * @param outputProperties properties to assign to the JAXP transformer (see
	 * {@link Transformer#setOutputProperty})
	 * @throws IOException if there's a problem writing to the file
	 * @throws TransformerException if there's a problem writing the XML
	 */
	public void write(File file, Map<String, String> outputProperties) throws TransformerException, IOException {
		Writer writer = new Utf8Writer(file);
		try {
			write(writer, outputProperties);
		} finally {
			writer.close();
		}
	}

	/**
	 * Writes the xCal document to a writer.
	 * @param writer the writer
	 * @throws TransformerException if there's a problem writing to the writer
	 */
	public void write(Writer writer) throws TransformerException {
		write(writer, (Integer) null);
	}

	/**
	 * Writes the xCal document to a writer.
	 * @param writer the writer
	 * @param indent the number of indent spaces to use for pretty-printing or
	 * "null" to disable pretty-printing (disabled by default)
	 * @throws TransformerException if there's a problem writing to the writer
	 */
	public void write(Writer writer, Integer indent) throws TransformerException {
		write(writer, indent, null);
	}

	/**
	 * Writes the xCal document to a writer.
	 * @param writer the writer
	 * @param indent the number of indent spaces to use for pretty-printing or
	 * "null" to disable pretty-printing (disabled by default)
	 * @param xmlVersion the XML version to use (defaults to "1.0") (Note: Many
	 * JDKs only support 1.0 natively. For XML 1.1 support, add a JAXP library
	 * like <a href=
	 * "http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22xalan%22%20AND%20a%3A%22xalan%22"
	 * >xalan</a> to your project)
	 * @throws TransformerException if there's a problem writing to the writer
	 */
	public void write(Writer writer, Integer indent, String xmlVersion) throws TransformerException {
		write(writer, new XCalOutputProperties(indent, xmlVersion));
	}

	/**
	 * Writes the xCal document to a writer.
	 * @param writer the writer
	 * @param outputProperties properties to assign to the JAXP transformer (see
	 * {@link Transformer#setOutputProperty})
	 * @throws TransformerException if there's a problem writing to the writer
	 */
	public void write(Writer writer, Map<String, String> outputProperties) throws TransformerException {
		Transformer transformer;
		try {
			transformer = TransformerFactory.newInstance().newTransformer();
		} catch (TransformerConfigurationException e) {
			//should never be thrown because we're not doing anything fancy with the configuration
			throw new RuntimeException(e);
		} catch (TransformerFactoryConfigurationError e) {
			//should never be thrown because we're not doing anything fancy with the configuration
			throw new RuntimeException(e);
		}

		/*
		 * Using Transformer#setOutputProperties(Properties) doesn't work for
		 * some reason for setting the number of indentation spaces.
		 */
		for (Map.Entry<String, String> entry : outputProperties.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			transformer.setOutputProperty(key, value);
		}

		DOMSource source = new DOMSource(document);
		StreamResult result = new StreamResult(writer);
		transformer.transform(source, result);
	}

	@Override
	public String toString() {
		return write(2);
	}

	private class XCalDocumentStreamReader extends StreamReader {
		private final Iterator<Element> vcalendarElements = getVCalendarElements().iterator();

		@Override
		protected ICalendar _readNext() throws IOException {
			if (!vcalendarElements.hasNext()) {
				return null;
			}

			context.setVersion(ICalVersion.V2_0);
			Element vcalendarElement = vcalendarElements.next();
			return parseICal(vcalendarElement);
		}

		private ICalendar parseICal(Element icalElement) {
			ICalComponent root = parseComponent(icalElement);
			if (root instanceof ICalendar) {
				return (ICalendar) root;
			}

			//shouldn't happen, since only <vcalendar> elements are passed into this method
			ICalendar ical = icalMarshaller.emptyInstance();
			ical.addComponent(root);
			return ical;
		}

		private ICalComponent parseComponent(Element componentElement) {
			//create the component object
			ICalComponentScribe<? extends ICalComponent> scribe = index.getComponentScribe(componentElement.getLocalName(), ICalVersion.V2_0);
			ICalComponent component = scribe.emptyInstance();
			boolean isICalendar = component instanceof ICalendar;

			//parse properties
			for (Element propertyWrapperElement : getChildElements(componentElement, PROPERTIES)) { //there should be only one <properties> element, but parse them all incase there are more
				for (Element propertyElement : XmlUtils.toElementList(propertyWrapperElement.getChildNodes())) {
					ICalProperty property = parseProperty(propertyElement);
					if (property == null) {
						continue;
					}

					//set "ICalendar.version" if the value of the VERSION property is recognized
					//otherwise, unmarshal VERSION like a normal property
					if (isICalendar && property instanceof Version) {
						Version version = (Version) property;
						ICalVersion icalVersion = version.toICalVersion();
						if (icalVersion != null) {
							context.setVersion(icalVersion);
							continue;
						}
					}

					component.addProperty(property);
				}
			}

			//parse sub-components
			for (Element componentWrapperElement : getChildElements(componentElement, COMPONENTS)) { //there should be only one <components> element, but parse them all incase there are more
				for (Element subComponentElement : XmlUtils.toElementList(componentWrapperElement.getChildNodes())) {
					if (!XCAL_NS.equals(subComponentElement.getNamespaceURI())) {
						continue;
					}

					ICalComponent subComponent = parseComponent(subComponentElement);
					component.addComponent(subComponent);
				}
			}

			return component;
		}

		private ICalProperty parseProperty(Element propertyElement) {
			ICalParameters parameters = parseParameters(propertyElement);
			String propertyName = propertyElement.getLocalName();
			QName qname = new QName(propertyElement.getNamespaceURI(), propertyName);

			context.getWarnings().clear();
			context.setPropertyName(propertyName);
			ICalPropertyScribe<? extends ICalProperty> scribe = index.getPropertyScribe(qname);
			try {
				ICalProperty property = scribe.parseXml(propertyElement, parameters, context);
				warnings.addAll(context.getWarnings());
				return property;
			} catch (SkipMeException e) {
				//@formatter:off
				warnings.add(new ParseWarning.Builder(context)
					.message(0, e.getMessage())
					.build()
				);
				//@formatter:on
				return null;
			} catch (CannotParseException e) {
				//@formatter:off
				warnings.add(new ParseWarning.Builder(context)
					.message(e)
					.build()
				);
				//@formatter:on

				scribe = index.getPropertyScribe(Xml.class);
				return scribe.parseXml(propertyElement, parameters, context);
			}
		}

		private ICalParameters parseParameters(Element propertyElement) {
			ICalParameters parameters = new ICalParameters();

			for (Element parametersElement : getChildElements(propertyElement, PARAMETERS)) { //there should be only one <parameters> element, but parse them all incase there are more
				List<Element> paramElements = XmlUtils.toElementList(parametersElement.getChildNodes());
				for (Element paramElement : paramElements) {
					if (!XCAL_NS.equals(paramElement.getNamespaceURI())) {
						continue;
					}

					String name = paramElement.getLocalName().toUpperCase();
					List<Element> valueElements = XmlUtils.toElementList(paramElement.getChildNodes());
					if (valueElements.isEmpty()) {
						//this should never be true if the xCal follows the specs
						String value = paramElement.getTextContent();
						parameters.put(name, value);
						continue;
					}

					for (Element valueElement : valueElements) {
						if (!XCAL_NS.equals(valueElement.getNamespaceURI())) {
							continue;
						}

						String value = valueElement.getTextContent();
						parameters.put(name, value);
					}
				}
			}

			return parameters;
		}

		private List<Element> getVCalendarElements() {
			return (icalendarRootElement == null) ? Collections.<Element> emptyList() : getChildElements(icalendarRootElement, VCALENDAR);
		}

		private List<Element> getChildElements(Element parent, QName qname) {
			List<Element> elements = new ArrayList<Element>();
			for (Element child : XmlUtils.toElementList(parent.getChildNodes())) {
				QName childQName = new QName(child.getNamespaceURI(), child.getLocalName());
				if (qname.equals(childQName)) {
					elements.add(child);
				}
			}
			return elements;
		}

		public void close() {
			//do nothing
		}
	}

	public class XCalDocumentStreamWriter extends XCalWriterBase {
		@Override
		public void write(ICalendar ical) {
			try {
				super.write(ical);
			} catch (IOException e) {
				//won't be thrown because we're writing to DOM
			}
		}

		@Override
		protected void _write(ICalendar ical) {
			Element element = buildComponentElement(ical);

			if (icalendarRootElement == null) {
				icalendarRootElement = buildElement(ICALENDAR);
				Element documentRoot = document.getDocumentElement();
				if (documentRoot == null) {
					document.appendChild(icalendarRootElement);
				} else {
					documentRoot.appendChild(icalendarRootElement);
				}
			}
			icalendarRootElement.appendChild(element);
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		private Element buildComponentElement(ICalComponent component) {
			ICalComponentScribe componentScribe = index.getComponentScribe(component);
			Element componentElement = buildElement(componentScribe.getComponentName().toLowerCase());

			Element propertiesWrapperElement = buildElement(PROPERTIES);
			List propertyObjs = componentScribe.getProperties(component);
			if (component instanceof ICalendar && component.getProperty(Version.class) == null) {
				//add a version property
				propertyObjs.add(0, new Version(targetVersion));
			}

			for (Object propertyObj : propertyObjs) {
				context.setParent(component); //set parent here incase a scribe resets the parent
				ICalProperty property = (ICalProperty) propertyObj;

				//create property element
				Element propertyElement = buildPropertyElement(property);
				if (propertyElement != null) {
					propertiesWrapperElement.appendChild(propertyElement);
				}
			}
			if (propertiesWrapperElement.hasChildNodes()) {
				componentElement.appendChild(propertiesWrapperElement);
			}

			List subComponents = componentScribe.getComponents(component);
			if (component instanceof ICalendar) {
				//add the VTIMEZONE components that were auto-generated by TimezoneOptions
				Collection<VTimezone> tzs = getTimezoneComponents();
				for (VTimezone tz : tzs) {
					if (!subComponents.contains(tz)) {
						subComponents.add(0, tz);
					}
				}
			}
			Element componentsWrapperElement = buildElement(COMPONENTS);
			for (Object subComponentObj : subComponents) {
				ICalComponent subComponent = (ICalComponent) subComponentObj;
				Element subComponentElement = buildComponentElement(subComponent);
				if (subComponentElement != null) {
					componentsWrapperElement.appendChild(subComponentElement);
				}
			}
			if (componentsWrapperElement.hasChildNodes()) {
				componentElement.appendChild(componentsWrapperElement);
			}

			return componentElement;
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		private Element buildPropertyElement(ICalProperty property) {
			Element propertyElement;
			ICalPropertyScribe scribe = index.getPropertyScribe(property);

			if (property instanceof Xml) {
				Xml xml = (Xml) property;

				Document value = xml.getValue();
				if (value == null) {
					return null;
				}

				//import the XML element into the xCal DOM
				propertyElement = value.getDocumentElement();
				propertyElement = (Element) document.importNode(propertyElement, true);
			} else {
				propertyElement = buildElement(scribe.getQName());

				//marshal value
				try {
					scribe.writeXml(property, propertyElement, context);
				} catch (SkipMeException e) {
					return null;
				}
			}

			//build parameters
			ICalParameters parameters = scribe.prepareParameters(property, context);
			if (!parameters.isEmpty()) {
				Element parametersElement = buildParametersElement(parameters);
				propertyElement.insertBefore(parametersElement, propertyElement.getFirstChild());
			}

			return propertyElement;
		}

		private Element buildParametersElement(ICalParameters parameters) {
			Element parametersWrapperElement = buildElement(PARAMETERS);

			for (Map.Entry<String, List<String>> parameter : parameters) {
				String name = parameter.getKey().toLowerCase();
				ICalDataType dataType = parameterDataTypes.get(name);
				String dataTypeStr = (dataType == null) ? "unknown" : dataType.getName().toLowerCase();

				Element parameterElement = buildAndAppendElement(name, parametersWrapperElement);
				for (String parameterValue : parameter.getValue()) {
					Element parameterValueElement = buildAndAppendElement(dataTypeStr, parameterElement);
					parameterValueElement.setTextContent(parameterValue);
				}
			}

			return parametersWrapperElement;
		}

		private Element buildElement(String localName) {
			return buildElement(new QName(XCAL_NS, localName));
		}

		private Element buildElement(QName qname) {
			return document.createElementNS(qname.getNamespaceURI(), qname.getLocalPart());
		}

		private Element buildAndAppendElement(String localName, Element parent) {
			return buildAndAppendElement(new QName(XCAL_NS, localName), parent);
		}

		private Element buildAndAppendElement(QName qname, Element parent) {
			Element child = document.createElementNS(qname.getNamespaceURI(), qname.getLocalPart());
			parent.appendChild(child);
			return child;
		}

		public void close() {
			//do nothing
		}
	}
}
