package biweekly.io.chain;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;

import biweekly.Biweekly;
import biweekly.ICalDataType;
import biweekly.ICalendar;
import biweekly.component.ICalComponent;
import biweekly.io.scribe.component.ICalComponentScribe;
import biweekly.io.scribe.property.ICalPropertyScribe;
import biweekly.io.xml.XCalDocument;
import biweekly.io.xml.XCalDocument.XCalDocumentStreamWriter;
import biweekly.io.xml.XCalOutputProperties;
import biweekly.property.ICalProperty;

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
 * Chainer class for writing xCal (XML-encoded iCalendar objects).
 * @see Biweekly#writeXml(Collection)
 * @see Biweekly#writeXml(ICalendar...)
 * @author Michael Angstadt
 */
public class ChainingXmlWriter extends ChainingWriter<ChainingXmlWriter> {
	private final XCalOutputProperties outputProperties = new XCalOutputProperties();
	private final Map<String, ICalDataType> parameterDataTypes = new HashMap<String, ICalDataType>(0);

	/**
	 * @param icals the iCValendar objects to write
	 */
	public ChainingXmlWriter(Collection<ICalendar> icals) {
		super(icals);
	}

	/**
	 * Sets the number of indent spaces to use for pretty-printing. If not set,
	 * then the XML will not be pretty-printed.
	 * @param indent the number of spaces in the indent string or "null" not to
	 * pretty-print (disabled by default)
	 * @return this
	 */
	public ChainingXmlWriter indent(Integer indent) {
		outputProperties.setIndent(indent);
		return this;
	}

	/**
	 * Sets the XML version to use. Note that many JDKs only support 1.0
	 * natively. For XML 1.1 support, add a JAXP library like <a href=
	 * "http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22xalan%22%20AND%20a%3A%22xalan%22"
	 * >xalan</a> to your project.
	 * @param xmlVersion the XML version (defaults to "1.0")
	 * @return this
	 */
	public ChainingXmlWriter xmlVersion(String xmlVersion) {
		outputProperties.setXmlVersion(xmlVersion);
		return this;
	}

	/**
	 * Assigns an output property to the JAXP transformer (see
	 * {@link Transformer#setOutputProperty}).
	 * @param name the property name
	 * @param value the property value
	 * @return this
	 */
	public ChainingXmlWriter outputProperty(String name, String value) {
		outputProperties.put(name, value);
		return this;
	}

	/**
	 * Assigns all of the given output properties to the JAXP transformer (see
	 * {@link Transformer#setOutputProperty}).
	 * @param outputProperties the properties
	 * @return this
	 */
	public ChainingXmlWriter outputProperties(Map<String, String> outputProperties) {
		this.outputProperties.putAll(outputProperties);
		return this;
	}

	@Override
	public ChainingXmlWriter tz(TimeZone defaultTimeZone, boolean outlookCompatible) {
		return super.tz(defaultTimeZone, outlookCompatible);
	}

	@Override
	public ChainingXmlWriter register(ICalPropertyScribe<? extends ICalProperty> scribe) {
		return super.register(scribe);
	}

	@Override
	public ChainingXmlWriter register(ICalComponentScribe<? extends ICalComponent> scribe) {
		return super.register(scribe);
	}

	/**
	 * Registers the data type of a non-standard parameter. Non-standard
	 * parameters use the "unknown" data type by default.
	 * @param parameterName the parameter name (e.g. "x-foo")
	 * @param dataType the data type
	 * @return this
	 */
	public ChainingXmlWriter register(String parameterName, ICalDataType dataType) {
		parameterDataTypes.put(parameterName, dataType);
		return this;
	}

	/**
	 * Writes the iCalendar objects to a string.
	 * @return the XML document
	 */
	public String go() {
		return createXCalDocument().write(outputProperties);
	}

	/**
	 * Writes the iCalendar objects to an output stream.
	 * @param out the output stream to write to
	 * @throws TransformerException if there's a problem writing to the output
	 * stream
	 */
	public void go(OutputStream out) throws TransformerException {
		createXCalDocument().write(out, outputProperties);
	}

	/**
	 * Writes the iCalendar objects to a file.
	 * @param file the file to write to
	 * @throws IOException if the file can't be opened
	 * @throws TransformerException if there's a problem writing to the file
	 */
	public void go(File file) throws IOException, TransformerException {
		createXCalDocument().write(file, outputProperties);
	}

	/**
	 * Writes the iCalendar objects to a writer.
	 * @param writer the writer to write to
	 * @throws TransformerException if there's a problem writing to the writer
	 */
	public void go(Writer writer) throws TransformerException {
		createXCalDocument().write(writer, outputProperties);
	}

	/**
	 * Generates an XML document object model (DOM) containing the iCalendar
	 * objects.
	 * @return the DOM
	 */
	public Document dom() {
		return createXCalDocument().getDocument();
	}

	private XCalDocument createXCalDocument() {
		XCalDocument document = new XCalDocument();

		XCalDocumentStreamWriter writer = document.writer();
		if (defaultTimeZone != null) {
			writer.setGlobalTimezone(defaultTimeZone);
		}
		for (Map.Entry<String, ICalDataType> entry : parameterDataTypes.entrySet()) {
			String parameterName = entry.getKey();
			ICalDataType dataType = entry.getValue();
			writer.registerParameterDataType(parameterName, dataType);
		}
		if (index != null) {
			writer.setScribeIndex(index);
		}

		for (ICalendar ical : icals) {
			writer.write(ical);
		}

		return document;
	}
}
