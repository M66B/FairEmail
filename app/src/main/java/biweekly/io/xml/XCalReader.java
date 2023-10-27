package biweekly.io.xml;

import static biweekly.io.xml.XCalNamespaceContext.XCAL_NS;
import static biweekly.io.xml.XCalQNames.COMPONENTS;
import static biweekly.io.xml.XCalQNames.ICALENDAR;
import static biweekly.io.xml.XCalQNames.PARAMETERS;
import static biweekly.io.xml.XCalQNames.PROPERTIES;
import static biweekly.io.xml.XCalQNames.VCALENDAR;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.xml.namespace.QName;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import biweekly.ICalVersion;
import biweekly.ICalendar;
import biweekly.component.ICalComponent;
import biweekly.io.CannotParseException;
import biweekly.io.ParseContext;
import biweekly.io.ParseWarning;
import biweekly.io.SkipMeException;
import biweekly.io.StreamReader;
import biweekly.io.scribe.component.ICalComponentScribe;
import biweekly.io.scribe.property.ICalPropertyScribe;
import biweekly.parameter.ICalParameters;
import biweekly.property.ICalProperty;
import biweekly.property.Version;
import biweekly.property.Xml;
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

 The views and conclusions contained in the software and documentation are those
 of the authors and should not be interpreted as representing official policies, 
 either expressed or implied, of the FreeBSD Project.
 */

/**
 * <p>
 * Reads xCals (XML-encoded iCalendar objects) in a streaming fashion.
 * </p>
 * <p>
 * <b>Example:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * File file = new File("icals.xml");
 * XCalReader reader = null;
 * try {
 *   reader = new XCalReader(file);
 *   ICalendar ical;
 *   while ((ical = reader.readNext()) != null) {
 *     //...
 *   }
 * } finally {
 *   if (reader != null) reader.close();
 * }
 * </pre>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc6321">RFC 6321</a>
 */
public class XCalReader extends StreamReader {
	private final Source source;
	private final Closeable stream;

	private volatile ICalendar readICal;
	private volatile TransformerException thrown;

	private final ReadThread thread = new ReadThread();
	private final Object lock = new Object();
	private final BlockingQueue<Object> readerBlock = new ArrayBlockingQueue<Object>(1);
	private final BlockingQueue<Object> threadBlock = new ArrayBlockingQueue<Object>(1);

	/**
	 * @param str the string to read from
	 */
	public XCalReader(String str) {
		this(new StringReader(str));
	}

	/**
	 * @param in the input stream to read from
	 */
	public XCalReader(InputStream in) {
		source = new StreamSource(in);
		stream = in;
	}

	/**
	 * @param file the file to read from
	 * @throws FileNotFoundException if the file doesn't exist
	 */
	public XCalReader(File file) throws FileNotFoundException {
		this(new BufferedInputStream(new FileInputStream(file)));
	}

	/**
	 * @param reader the reader to read from
	 */
	public XCalReader(Reader reader) {
		source = new StreamSource(reader);
		stream = reader;
	}

	/**
	 * @param node the DOM node to read from
	 */
	public XCalReader(Node node) {
		source = new DOMSource(node);
		stream = null;
	}

	@Override
	protected ICalendar _readNext() throws IOException {
		readICal = null;
		warnings.clear();
		context = new ParseContext();
		thrown = null;

		if (!thread.started) {
			thread.start();
		} else {
			if (thread.finished || thread.closed) {
				return null;
			}

			try {
				threadBlock.put(lock);
			} catch (InterruptedException e) {
				return null;
			}
		}

		//wait until thread reads xCard
		try {
			readerBlock.take();
		} catch (InterruptedException e) {
			return null;
		}

		if (thrown != null) {
			throw new IOException(thrown);
		}

		return readICal;
	}

	private class ReadThread extends Thread {
		private final SAXResult result;
		private final Transformer transformer;
		private volatile boolean finished = false, started = false, closed = false;

		public ReadThread() {
			setName(getClass().getSimpleName());

			//create the transformer
			try {
				TransformerFactory factory = TransformerFactory.newInstance();
				XmlUtils.applyXXEProtection(factory);

				transformer = factory.newTransformer();
			} catch (TransformerConfigurationException e) {
				//shouldn't be thrown because it's a simple configuration
				throw new RuntimeException(e);
			}

			//prevent error messages from being printed to stderr
			transformer.setErrorListener(new NoOpErrorListener());

			result = new SAXResult(new ContentHandlerImpl());
		}

		@Override
		public void run() {
			started = true;

			try {
				transformer.transform(source, result);
			} catch (TransformerException e) {
				if (!thread.closed) {
					thrown = e;
				}
			} finally {
				finished = true;
				try {
					readerBlock.put(lock);
				} catch (InterruptedException e) {
					//ignore
				}
			}
		}
	}

	private class ContentHandlerImpl extends DefaultHandler {
		private final Document DOC = XmlUtils.createDocument();
		private final XCalStructure structure = new XCalStructure();
		private final StringBuilder characterBuffer = new StringBuilder();
		private final LinkedList<ICalComponent> componentStack = new LinkedList<ICalComponent>();

		private Element propertyElement, parent;
		private QName paramName;
		private ICalComponent curComponent;
		private ICalParameters parameters;

		@Override
		public void characters(char[] buffer, int start, int length) throws SAXException {
			characterBuffer.append(buffer, start, length);
		}

		@Override
		public void startElement(String namespace, String localName, String qName, Attributes attributes) throws SAXException {
			QName qname = new QName(namespace, localName);
			String textContent = emptyCharacterBuffer();

			if (structure.isEmpty()) {
				//<icalendar>
				if (ICALENDAR.equals(qname)) {
					structure.push(ElementType.icalendar);
				}
				return;
			}

			ElementType parentType = structure.peek();
			ElementType typeToPush = null;
			if (parentType != null) {
				switch (parentType) {

				case icalendar:
					//<vcalendar>
					if (VCALENDAR.equals(qname)) {
						ICalComponentScribe<? extends ICalComponent> scribe = index.getComponentScribe(localName, ICalVersion.V2_0);
						ICalComponent component = scribe.emptyInstance();

						curComponent = component;
						readICal = (ICalendar) component;
						typeToPush = ElementType.component;
					}
					break;

				case component:
					if (PROPERTIES.equals(qname)) {
						//<properties>
						typeToPush = ElementType.properties;
					} else if (COMPONENTS.equals(qname)) {
						//<components>
						componentStack.add(curComponent);
						curComponent = null;

						typeToPush = ElementType.components;
					}
					break;

				case components:
					//start component element
					if (XCAL_NS.equals(namespace)) {
						ICalComponentScribe<? extends ICalComponent> scribe = index.getComponentScribe(localName, ICalVersion.V2_0);
						curComponent = scribe.emptyInstance();

						ICalComponent parent = componentStack.getLast();
						parent.addComponent(curComponent);

						typeToPush = ElementType.component;
					}
					break;

				case properties:
					//start property element
					propertyElement = createElement(namespace, localName, attributes);
					parameters = new ICalParameters();
					parent = propertyElement;
					typeToPush = ElementType.property;
					break;

				case property:
					//<parameters>
					if (PARAMETERS.equals(qname)) {
						typeToPush = ElementType.parameters;
					}
					break;

				case parameters:
					//inside of <parameters>
					if (XCAL_NS.equals(namespace)) {
						paramName = qname;
						typeToPush = ElementType.parameter;
					}
					break;

				case parameter:
					//inside of a parameter element
					if (XCAL_NS.equals(namespace)) {
						typeToPush = ElementType.parameterValue;
					}
					break;
				case parameterValue:
					//should never have child elements
					break;
				}
			}

			//append element to property element
			if (propertyElement != null && typeToPush != ElementType.property && typeToPush != ElementType.parameters && !structure.isUnderParameters()) {
				if (textContent.length() > 0) {
					parent.appendChild(DOC.createTextNode(textContent));
				}

				Element element = createElement(namespace, localName, attributes);
				parent.appendChild(element);
				parent = element;
			}

			structure.push(typeToPush);
		}

		@Override
		public void endElement(String namespace, String localName, String qName) throws SAXException {
			String textContent = emptyCharacterBuffer();

			if (structure.isEmpty()) {
				//no <icalendar> elements were read yet
				return;
			}

			ElementType type = structure.pop();
			if (type == null && (propertyElement == null || structure.isUnderParameters())) {
				//it's a non-xCal element
				return;
			}

			if (type != null) {
				switch (type) {
				case parameterValue:
					parameters.put(paramName.getLocalPart(), textContent);
					break;

				case parameter:
					//do nothing
					break;

				case parameters:
					//do nothing
					break;

				case property:
					context.getWarnings().clear();
					context.setPropertyName(localName);

					propertyElement.appendChild(DOC.createTextNode(textContent));

					//unmarshal property and add to parent component
					QName propertyQName = new QName(propertyElement.getNamespaceURI(), propertyElement.getLocalName());
					ICalPropertyScribe<? extends ICalProperty> scribe = index.getPropertyScribe(propertyQName);
					try {
						ICalProperty property = scribe.parseXml(propertyElement, parameters, context);
						if (property instanceof Version && curComponent instanceof ICalendar) {
							Version versionProp = (Version) property;
							ICalVersion version = versionProp.toICalVersion();
							if (version != null) {
								ICalendar ical = (ICalendar) curComponent;
								ical.setVersion(version);
								context.setVersion(version);

								propertyElement = null;
								break;
							}
						}

						curComponent.addProperty(property);
						warnings.addAll(context.getWarnings());
					} catch (SkipMeException e) {
						//@formatter:off
						warnings.add(new ParseWarning.Builder(context)
							.message(0, e.getMessage())
							.build()
						);
						//@formatter:on
					} catch (CannotParseException e) {
						//@formatter:off
						warnings.add(new ParseWarning.Builder(context)
							.message(e)
							.build()
						);
						//@formatter:on

						scribe = index.getPropertyScribe(Xml.class);
						ICalProperty property = scribe.parseXml(propertyElement, parameters, context);
						curComponent.addProperty(property);
					}

					propertyElement = null;
					break;

				case component:
					curComponent = null;

					//</vcalendar>
					if (VCALENDAR.getNamespaceURI().equals(namespace) && VCALENDAR.getLocalPart().equals(localName)) {
						//wait for readNext() to be called again
						try {
							readerBlock.put(lock);
							threadBlock.take();
						} catch (InterruptedException e) {
							throw new SAXException(e);
						}
						return;
					}
					break;

				case properties:
					break;

				case components:
					curComponent = componentStack.removeLast();
					break;

				case icalendar:
					break;
				}
			}

			//append element to property element
			if (propertyElement != null && type != ElementType.property && type != ElementType.parameters && !structure.isUnderParameters()) {
				if (textContent.length() > 0) {
					parent.appendChild(DOC.createTextNode(textContent));
				}
				parent = (Element) parent.getParentNode();
			}
		}

		private String emptyCharacterBuffer() {
			String textContent = characterBuffer.toString();
			characterBuffer.setLength(0);
			return textContent;
		}

		private Element createElement(String namespace, String localName, Attributes attributes) {
			Element element = DOC.createElementNS(namespace, localName);
			applyAttributesTo(element, attributes);
			return element;
		}

		private void applyAttributesTo(Element element, Attributes attributes) {
			for (int i = 0; i < attributes.getLength(); i++) {
				String qname = attributes.getQName(i);
				if (qname.startsWith("xmlns:")) {
					continue;
				}

				String name = attributes.getLocalName(i);
				String value = attributes.getValue(i);
				element.setAttribute(name, value);
			}
		}
	}

	private enum ElementType {
		/*
		 * Note: A value is missing for "vcalendar" because it is treated as a
		 * "component".
		 * 
		 * Note: These enum values are in lower-case to make them stand out from
		 * the "XCalQNames" variable names, many of which are identically named.
		 */
		icalendar, components, properties, component, property, parameters, parameter, parameterValue;
	}

	/**
	 * <p>
	 * Keeps track of the structure of an xCal XML document.
	 * </p>
	 * 
	 * <p>
	 * Note that this class is here because you can't just do QName comparisons
	 * on a one-by-one basis. The location of an XML element within the XML
	 * document is important too. It's possible for two elements to have the
	 * same QName, but be treated differently depending on their location (e.g.
	 * the {@code <duration>} property has a {@code <duration>} data type)
	 * </p>
	 */
	private static class XCalStructure {
		private final List<ElementType> stack = new ArrayList<ElementType>();

		/**
		 * Pops the top element type off the stack.
		 * @return the element type or null if the stack is empty
		 */
		public ElementType pop() {
			return isEmpty() ? null : stack.remove(stack.size() - 1);
		}

		/**
		 * Looks at the top element type.
		 * @return the top element type or null if the stack is empty
		 */
		public ElementType peek() {
			return isEmpty() ? null : stack.get(stack.size() - 1);
		}

		/**
		 * Adds an element type to the stack.
		 * @param type the type to add or null if the XML element is not an xCal
		 * element
		 */
		public void push(ElementType type) {
			stack.add(type);
		}

		/**
		 * Determines if the leaf node is under a {@code <parameters>} element.
		 * @return true if it is, false if not
		 */
		public boolean isUnderParameters() {
			//get the first non-null type
			ElementType nonNull = null;
			for (int i = stack.size() - 1; i >= 0; i--) {
				ElementType type = stack.get(i);
				if (type != null) {
					nonNull = type;
					break;
				}
			}

			//@formatter:off
			return
			nonNull == ElementType.parameters ||
			nonNull == ElementType.parameter ||
			nonNull == ElementType.parameterValue;
			//@formatter:on
		}

		/**
		 * Determines if the stack is empty
		 * @return true if the stack is empty, false if not
		 */
		public boolean isEmpty() {
			return stack.isEmpty();
		}
	}

	/**
	 * An implementation of {@link ErrorListener} that doesn't do anything.
	 */
	private static class NoOpErrorListener implements ErrorListener {
		public void error(TransformerException e) {
			//do nothing
		}

		public void fatalError(TransformerException e) {
			//do nothing
		}

		public void warning(TransformerException e) {
			//do nothing
		}
	}

	/**
	 * Closes the underlying input stream.
	 */
	public void close() throws IOException {
		if (thread.isAlive()) {
			thread.closed = true;
			thread.interrupt();
		}

		if (stream != null) {
			stream.close();
		}
	}
}
