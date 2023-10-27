package biweekly;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import org.w3c.dom.Document;

import biweekly.io.chain.ChainingJsonParser;
import biweekly.io.chain.ChainingJsonStringParser;
import biweekly.io.chain.ChainingJsonWriter;
import biweekly.io.chain.ChainingTextParser;
import biweekly.io.chain.ChainingTextStringParser;
import biweekly.io.chain.ChainingTextWriter;
import biweekly.io.chain.ChainingXmlMemoryParser;
import biweekly.io.chain.ChainingXmlParser;
import biweekly.io.chain.ChainingXmlWriter;
import biweekly.io.json.JCalReader;
import biweekly.io.json.JCalWriter;
import biweekly.io.text.ICalReader;
import biweekly.io.text.ICalWriter;
import biweekly.io.xml.XCalDocument;
import biweekly.util.IOUtils;

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
 * <p>
 * Contains static chaining factory methods for reading/writing iCalendar
 * objects.
 * </p>
 * 
 * <p>
 * <b>Writing an iCalendar object</b>
 * </p>
 * 
 * <pre class="brush:java">
 * ICalendar ical = new ICalendar();
 * 
 * //string
 * String icalString = Biweekly.write(ical).go();
 * 
 * //file
 * File file = new File("meeting.ics");
 * Biweekly.write(ical).go(file);
 * 
 * //output stream
 * OutputStream out = ...
 * Biweekly.write(ical).go(out);
 * out.close();
 * 
 * //writer (should be configured to use UTF-8 encoding)
 * Writer writer = ...
 * Biweekly.write(ical).go(writer);
 * writer.close();
 * </pre>
 * 
 * <p>
 * <b>Writing multiple iCalendar objects</b>
 * </p>
 * 
 * <pre class="brush:java">
 * ICalendar ical1 = new ICalendar();
 * ICalendar ical2 = new ICalendar();
 * 
 * String icalString = Biweekly.write(ical1, ical2).go();
 * </pre>
 * 
 * <p>
 * <b>Writing an XML-encoded iCalendar object (xCal)</b>
 * </p>
 * 
 * <pre class="brush:java">
 * //Call writeXml() instead of write()
 * ICalendar ical = new ICalendar();
 * String xml = Biweekly.writeXml(ical).indent(2).go();
 * </pre>
 * 
 * <p>
 * <b>Writing a JSON-encoded iCalendar object (jCal)</b>
 * </p>
 * 
 * <pre class="brush:java">
 * //Call writeJson() instead of write()
 * ICalendar ical = new ICalendar();
 * String json = Biweekly.writeJson(ical).go();
 * </pre>
 * 
 * <p>
 * <b>Reading an iCalendar object</b>
 * </p>
 * 
 * <pre class="brush:java">
 * ICalendar ical;
 * 
 * //string
 * String icalStr = ...
 * ical = Biweekly.parse(icalStr).first();
 * 
 * //file
 * File file = new File("meeting.ics");
 * ical = Biweekly.parse(file).first();
 * 
 * //input stream
 * InputStream in = ...
 * ical = Biweekly.parse(in).first();
 * in.close();  
 * 
 * //reader (should be configured to read UTF-8)
 * Reader reader = ...
 * ical = Biweekly.parse(reader).first();
 * reader.close();
 * </pre>
 * 
 * <p>
 * <b>Reading multiple iCalendar objects</b>
 * </p>
 * 
 * <pre class="brush:java">
 * String icalStr = ...
 * List&lt;ICalendar&gt; icals = Biweekly.parse(icalStr).all();
 * </pre>
 * 
 * <p>
 * <b>Reading an XML-encoded iCalendar object (xCal)</b>
 * </p>
 * 
 * <pre class="brush:java">
 * //Call parseXml() instead of parse()
 * String xml = ...
 * ICalendar ical = Biweekly.parseXml(xml).first();
 * </pre>
 * 
 * <p>
 * <b>Reading a JSON-encoded iCalendar object (Cal)</b>
 * </p>
 * 
 * <pre class="brush:java">
 * //Call parseJson() instead of parse()
 * String json = ...
 * ICalendar ical = Biweekly.parseJson(json).first();
 * </pre>
 * 
 * <p>
 * <b>Retrieving parser warnings</b>
 * </p>
 * 
 * <pre class="brush:java">
 * String icalStr = ...
 * List&lt;List&lt;String&gt;&gt; warnings = new ArrayList&lt;List&lt;String&gt;&gt;();
 * List&lt;ICalendar&gt; icals = Biweekly.parse(icalStr).warnings(warnings).all();
 * int i = 0;
 * for (List&lt;String&gt; icalWarnings : warnings) {
 *   System.out.println("iCal #" + (i++) + " warnings:");
 *   for (String warning : icalWarnings) {
 *     System.out.println(warning);
 *   }
 * }
 * </pre>
 * 
 * <p>
 * The methods in this class make use of the following classes. These classes
 * can be used if greater control over the read/write operation is required:
 * </p>
 * 
 * <table class="simpleTable">
 * <caption>Classes used by this class</caption>
 * <tr>
 * <th></th>
 * <th>Classes</th>
 * <th>Supports<br>
 * streaming?</th>
 * </tr>
 * <tr>
 * <th>Text</th>
 * <td>{@link ICalReader} / {@link ICalWriter}</td>
 * <td>yes</td>
 * </tr>
 * <tr>
 * <th>XML</th>
 * <td>{@link XCalDocument}</td>
 * <td>no</td>
 * </tr>
 * <tr>
 * <th>JSON</th>
 * <td>{@link JCalReader} / {@link JCalWriter}</td>
 * <td>yes</td>
 * </tr>
 * </table>
 * @author Michael Angstadt
 */
public final class Biweekly {
	/**
	 * The version of the library.
	 */
	public static final String VERSION;

	/**
	 * The Maven group ID.
	 */
	public static final String GROUP_ID;

	/**
	 * The Maven artifact ID.
	 */
	public static final String ARTIFACT_ID;

	/**
	 * The project webpage.
	 */
	public static final String URL;

	static {
		InputStream in = null;
		try {
			in = Biweekly.class.getResourceAsStream("biweekly.properties");
			Properties props = new Properties();
			props.load(in);

			VERSION = props.getProperty("version");
			GROUP_ID = props.getProperty("groupId");
			ARTIFACT_ID = props.getProperty("artifactId");
			URL = props.getProperty("url");
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	/**
	 * Parses an iCalendar object string.
	 * @param ical the iCalendar data
	 * @return chainer object for completing the parse operation
	 */
	public static ChainingTextStringParser parse(String ical) {
		return new ChainingTextStringParser(ical);
	}

	/**
	 * Parses an iCalendar file.
	 * @param file the iCalendar file
	 * @return chainer object for completing the parse operation
	 */
	public static ChainingTextParser<ChainingTextParser<?>> parse(File file) {
		return new ChainingTextParser<ChainingTextParser<?>>(file);
	}

	/**
	 * Parses an iCalendar data stream.
	 * @param in the input stream
	 * @return chainer object for completing the parse operation
	 */
	public static ChainingTextParser<ChainingTextParser<?>> parse(InputStream in) {
		return new ChainingTextParser<ChainingTextParser<?>>(in);
	}

	/**
	 * Parses an iCalendar data stream.
	 * @param reader the reader
	 * @return chainer object for completing the parse operation
	 */
	public static ChainingTextParser<ChainingTextParser<?>> parse(Reader reader) {
		return new ChainingTextParser<ChainingTextParser<?>>(reader);
	}

	/**
	 * Writes multiple iCalendar objects to a data stream.
	 * @param icals the iCalendar objects to write
	 * @return chainer object for completing the write operation
	 */
	public static ChainingTextWriter write(ICalendar... icals) {
		return write(Arrays.asList(icals));
	}

	/**
	 * Writes multiple iCalendar objects to a data stream.
	 * @param icals the iCalendar objects to write
	 * @return chainer object for completing the write operation
	 */
	public static ChainingTextWriter write(Collection<ICalendar> icals) {
		return new ChainingTextWriter(icals);
	}

	/**
	 * Parses an xCal document (XML-encoded iCalendar objects) from a string.
	 * @param xml the XML string
	 * @return chainer object for completing the parse operation
	 */
	public static ChainingXmlMemoryParser parseXml(String xml) {
		return new ChainingXmlMemoryParser(xml);
	}

	/**
	 * Parses an xCal document (XML-encoded iCalendar objects) from a file.
	 * @param file the XML file
	 * @return chainer object for completing the parse operation
	 */
	public static ChainingXmlParser<ChainingXmlParser<?>> parseXml(File file) {
		return new ChainingXmlParser<ChainingXmlParser<?>>(file);
	}

	/**
	 * Parses an xCal document (XML-encoded iCalendar objects) from an input
	 * stream.
	 * @param in the input stream
	 * @return chainer object for completing the parse operation
	 */
	public static ChainingXmlParser<ChainingXmlParser<?>> parseXml(InputStream in) {
		return new ChainingXmlParser<ChainingXmlParser<?>>(in);
	}

	/**
	 * <p>
	 * Parses an xCal document (XML-encoded iCalendar objects) from a reader.
	 * </p>
	 * <p>
	 * Note that use of this method is discouraged. It ignores the character
	 * encoding that is defined within the XML document itself, and should only
	 * be used if the encoding is undefined or if the encoding needs to be
	 * ignored for whatever reason. The {@link #parseXml(InputStream)} method
	 * should be used instead, since it takes the XML document's character
	 * encoding into account when parsing.
	 * </p>
	 * @param reader the reader
	 * @return chainer object for completing the parse operation
	 */
	public static ChainingXmlParser<ChainingXmlParser<?>> parseXml(Reader reader) {
		return new ChainingXmlParser<ChainingXmlParser<?>>(reader);
	}

	/**
	 * Parses an xCal document (XML-encoded iCalendar objects).
	 * @param document the XML document
	 * @return chainer object for completing the parse operation
	 */
	public static ChainingXmlMemoryParser parseXml(Document document) {
		return new ChainingXmlMemoryParser(document);
	}

	/**
	 * Writes an xCal document (XML-encoded iCalendar objects).
	 * @param icals the iCalendar object(s) to write
	 * @return chainer object for completing the write operation
	 */
	public static ChainingXmlWriter writeXml(ICalendar... icals) {
		return writeXml(Arrays.asList(icals));
	}

	/**
	 * Writes an xCal document (XML-encoded iCalendar objects).
	 * @param icals the iCalendar objects to write
	 * @return chainer object for completing the write operation
	 */
	public static ChainingXmlWriter writeXml(Collection<ICalendar> icals) {
		return new ChainingXmlWriter(icals);
	}

	/**
	 * Parses a jCal data stream (JSON-encoded iCalendar objects).
	 * @param json the JSON data
	 * @return chainer object for completing the parse operation
	 */
	public static ChainingJsonStringParser parseJson(String json) {
		return new ChainingJsonStringParser(json);
	}

	/**
	 * Parses a jCal data stream (JSON-encoded iCalendar objects).
	 * @param file the JSON file
	 * @return chainer object for completing the parse operation
	 */
	public static ChainingJsonParser<ChainingJsonParser<?>> parseJson(File file) {
		return new ChainingJsonParser<ChainingJsonParser<?>>(file);
	}

	/**
	 * Parses a jCal data stream (JSON-encoded iCalendar objects).
	 * @param in the input stream
	 * @return chainer object for completing the parse operation
	 */
	public static ChainingJsonParser<ChainingJsonParser<?>> parseJson(InputStream in) {
		return new ChainingJsonParser<ChainingJsonParser<?>>(in);
	}

	/**
	 * Parses a jCal data stream (JSON-encoded iCalendar objects).
	 * @param reader the reader
	 * @return chainer object for completing the parse operation
	 */
	public static ChainingJsonParser<ChainingJsonParser<?>> parseJson(Reader reader) {
		return new ChainingJsonParser<ChainingJsonParser<?>>(reader);
	}

	/**
	 * Writes an xCal document (XML-encoded iCalendar objects).
	 * @param icals the iCalendar object(s) to write
	 * @return chainer object for completing the write operation
	 */
	public static ChainingJsonWriter writeJson(ICalendar... icals) {
		return writeJson(Arrays.asList(icals));
	}

	/**
	 * Writes an xCal document (XML-encoded iCalendar objects).
	 * @param icals the iCalendar objects to write
	 * @return chainer object for completing the write operation
	 */
	public static ChainingJsonWriter writeJson(Collection<ICalendar> icals) {
		return new ChainingJsonWriter(icals);
	}

	private Biweekly() {
		//hide
	}
}
