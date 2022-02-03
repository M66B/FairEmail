package com.bugsnag.android.repackaged.dslplatform.json;

import androidx.annotation.Nullable;

import org.w3c.dom.*;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;

@SuppressWarnings({"rawtypes", "unchecked"}) // suppress pre-existing warnings
public abstract class XmlConverter {

	static final JsonReader.ReadObject<Element> Reader = new JsonReader.ReadObject<Element>() {
		@Nullable
		@Override
		public Element read(JsonReader reader) throws IOException {
			return reader.wasNull() ? null : deserialize(reader);
		}
	};
	static final JsonWriter.WriteObject<Element> Writer = new JsonWriter.WriteObject<Element>() {
		@Override
		public void write(JsonWriter writer, @Nullable Element value) {
			serializeNullable(value, writer);
		}
	};

	private static final DocumentBuilder documentBuilder;

	static {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		try {
			documentBuilder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	public static void serializeNullable(@Nullable final Element value, final JsonWriter sw) {
		if (value == null)
			sw.writeNull();
		else
			serialize(value, sw);
	}

	public static void serialize(final Element value, final JsonWriter sw) {
		Document document = value.getOwnerDocument();
		DOMImplementationLS domImplLS = (DOMImplementationLS) document.getImplementation();
		LSSerializer serializer = domImplLS.createLSSerializer();
		LSOutput lsOutput = domImplLS.createLSOutput();
		lsOutput.setEncoding("UTF-8");
		StringWriter writer = new StringWriter();
		lsOutput.setCharacterStream(writer);
		serializer.write(document, lsOutput);
		StringConverter.serialize(writer.toString(), sw);
	}

	public static Element deserialize(final JsonReader reader) throws IOException {
		if (reader.last() == '"') {
			try {
				InputSource source = new InputSource(new StringReader(reader.readString()));
				return documentBuilder.parse(source).getDocumentElement();
			} catch (SAXException ex) {
				throw reader.newParseErrorAt("Invalid XML value", 0, ex);
			}
		} else {
			final Map<String, Object> map = ObjectConverter.deserializeMap(reader);
			return mapToXml(map);
		}
	}

	public static Element mapToXml(final Map<String, Object> map) throws IOException {
		final Set<String> xmlRootElementNames = map.keySet();
		if (xmlRootElementNames.size() > 1) {
			throw ParsingException.create("Invalid XML. Expecting root element", true);
		}
		final String rootName = xmlRootElementNames.iterator().next();
		final Document document = createDocument();
		final Element rootElement = document.createElement(rootName);
		document.appendChild(rootElement);
		buildXmlFromHashMap(document, rootElement, map.get(rootName));
		return rootElement;
	}

	private static synchronized Document createDocument() {
		try {
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder builder = factory.newDocumentBuilder();
			return builder.newDocument();
		} catch (ParserConfigurationException e) {
			throw new ConfigurationException(e);
		}
	}

	private static final String TEXT_NODE_TAG = "#text";
	private static final String COMMENT_NODE_TAG = "#comment";
	private static final String CDATA_NODE_TAG = "#cdata-section";

	@SuppressWarnings("unchecked")
	private static void buildXmlFromHashMap(
			final Document doc,
			final Element subtreeRootElement,
			@Nullable final Object elementContent) {
		if (elementContent instanceof HashMap) {
			final HashMap<String, Object> elementContentMap = (HashMap<String, Object>) elementContent;
			for (final Map.Entry<String, Object> childEntry : elementContentMap.entrySet()) {
				final String key = childEntry.getKey();
				if (key.startsWith("@")) {
					subtreeRootElement.setAttribute(key.substring(1), childEntry.getValue().toString());
				} else if (key.startsWith("#")) {
					if (key.equals(TEXT_NODE_TAG)) {
						if (childEntry.getValue() instanceof List) {
							buildTextNodeList(doc, subtreeRootElement, (List<String>) childEntry.getValue());
						} else {
							final Node textNode = doc.createTextNode(childEntry.getValue().toString());
							subtreeRootElement.appendChild(textNode);
						}
					} else if (key.equals(CDATA_NODE_TAG)) {
						if (childEntry.getValue() instanceof List) {
							buildCDataList(doc, subtreeRootElement, (List<String>) childEntry.getValue());
						} else {
							final Node cDataNode = doc.createCDATASection(childEntry.getValue().toString());
							subtreeRootElement.appendChild(cDataNode);
						}
					} else if (key.equals(COMMENT_NODE_TAG)) {
						if (childEntry.getValue() instanceof List) {
							buildCommentList(doc, subtreeRootElement, (List<String>) childEntry.getValue());
						} else {
							final Node commentNode = doc.createComment(childEntry.getValue().toString());
							subtreeRootElement.appendChild(commentNode);
						}
					} //else if (key.equals(WHITESPACE_NODE_TAG)
					//	|| key.equals(SIGNIFICANT_WHITESPACE_NODE_TAG)) {
					// Ignore
					//} else {
						/*
						 * All other nodes whose name starts with a '#' are invalid XML
						 * nodes, and thus ignored:
						 */
					//}
				} else {
					final Element newElement = doc.createElement(key);
					subtreeRootElement.appendChild(newElement);
					buildXmlFromHashMap(doc, newElement, childEntry.getValue());
				}
			}
		} else if (elementContent instanceof List) {
			buildXmlFromJsonArray(doc, subtreeRootElement, (List<Object>) elementContent);
		} else {
			if (elementContent != null) {
				subtreeRootElement.setTextContent(elementContent.toString());
			}
		}
	}

	private static void buildTextNodeList(final Document doc, final Node subtreeRoot, final List<String> nodeValues) {
		final StringBuilder sb = new StringBuilder();
		for (final String nodeValue : nodeValues) {
			sb.append(nodeValue);
		}
		subtreeRoot.appendChild(doc.createTextNode(sb.toString()));
	}

	private static void buildCDataList(final Document doc, final Node subtreeRoot, final List<String> nodeValues) {
		for (final String nodeValue : nodeValues) {
			subtreeRoot.appendChild(doc.createCDATASection(nodeValue));
		}
	}

	private static void buildCommentList(final Document doc, final Node subtreeRoot, final List<String> nodeValues) {
		for (final String nodeValue : nodeValues) {
			subtreeRoot.appendChild(doc.createComment(nodeValue));
		}
	}

	private static void buildXmlFromJsonArray(
			final Document doc,
			final Node listHeadNode,
			final List<Object> elementContentList) {
		final Node subtreeRootNode = listHeadNode.getParentNode();
		/* The head node (already exists) */
		buildXmlFromHashMap(doc, (Element) listHeadNode, elementContentList.get(0));
		/* The rest of the list */
		for (final Object elementContent : elementContentList.subList(1, elementContentList.size())) {
			final Element newElement = doc.createElement(listHeadNode.getNodeName());
			subtreeRootNode.appendChild(newElement);
			buildXmlFromHashMap(doc, newElement, elementContent);
		}
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<Element> deserializeCollection(final JsonReader reader) throws IOException {
		return reader.deserializeCollection(Reader);
	}

	public static void deserializeCollection(final JsonReader reader, final Collection<Element> res) throws IOException {
		reader.deserializeCollection(Reader, res);
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<Element> deserializeNullableCollection(final JsonReader reader) throws IOException {
		return reader.deserializeNullableCollection(Reader);
	}

	public static void deserializeNullableCollection(final JsonReader reader, final Collection<Element> res) throws IOException {
		reader.deserializeNullableCollection(Reader, res);
	}
}
