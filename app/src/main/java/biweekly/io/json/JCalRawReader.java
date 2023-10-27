package biweekly.io.json;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import biweekly.ICalDataType;
import biweekly.io.scribe.ScribeIndex;
import biweekly.parameter.ICalParameters;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

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
 * Parses an iCalendar JSON data stream (jCal).
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc7265">RFC 7265</a>
 */
public class JCalRawReader implements Closeable {
	private static final String VCALENDAR_COMPONENT_NAME = ScribeIndex.getICalendarScribe().getComponentName().toLowerCase(); //"vcalendar"

	private final Reader reader;
	private JsonParser parser;
	private boolean eof = false;
	private JCalDataStreamListener listener;
	private boolean strict = false;

	/**
	 * @param reader the reader to wrap
	 */
	public JCalRawReader(Reader reader) {
		this.reader = reader;
	}

	/**
	 * @param parser the parser to read from
	 * @param strict true if the parser's current token is expected to be
	 * positioned at the start of a jCard, false if not. If this is true, and
	 * the parser is not positioned at the beginning of a jCard, a
	 * {@link JCalParseException} will be thrown. If this if false, the parser
	 * will consume input until it reaches the beginning of a jCard.
	 */
	public JCalRawReader(JsonParser parser, boolean strict) {
		reader = null;
		this.parser = parser;
		this.strict = strict;
	}

	/**
	 * Gets the current line number.
	 * @return the line number
	 */
	public int getLineNum() {
		return (parser == null) ? 0 : parser.getCurrentLocation().getLineNr();
	}

	/**
	 * Reads the next iCalendar object from the jCal data stream.
	 * @param listener handles the iCalendar data as it is read off the wire
	 * @throws JCalParseException if the jCal syntax is incorrect (the JSON
	 * syntax may be valid, but it is not in the correct jCal format).
	 * @throws JsonParseException if the JSON syntax is incorrect
	 * @throws IOException if there is a problem reading from the data stream
	 */
	public void readNext(JCalDataStreamListener listener) throws IOException {
		if (parser == null) {
			JsonFactory factory = new JsonFactory();
			parser = factory.createParser(reader);
		}

		if (parser.isClosed()) {
			return;
		}

		this.listener = listener;

		//find the next iCalendar object
		JsonToken prev = parser.getCurrentToken();
		JsonToken cur;
		while ((cur = parser.nextToken()) != null) {
			if (prev == JsonToken.START_ARRAY && cur == JsonToken.VALUE_STRING && VCALENDAR_COMPONENT_NAME.equals(parser.getValueAsString())) {
				//found
				break;
			}

			if (strict) {
				//the parser was expecting the jCal to be there 
				if (prev != JsonToken.START_ARRAY) {
					throw new JCalParseException(JsonToken.START_ARRAY, prev);
				}

				if (cur != JsonToken.VALUE_STRING) {
					throw new JCalParseException(JsonToken.VALUE_STRING, cur);
				}

				throw new JCalParseException("Invalid value for first token: expected \"vcalendar\" , was \"" + parser.getValueAsString() + "\"", JsonToken.VALUE_STRING, cur);
			}

			prev = cur;
		}

		if (cur == null) {
			//EOF
			eof = true;
			return;
		}

		parseComponent(new ArrayList<String>());
	}

	private void parseComponent(List<String> components) throws IOException {
		checkCurrent(JsonToken.VALUE_STRING);
		String componentName = parser.getValueAsString();
		listener.readComponent(components, componentName);
		components.add(componentName);

		//start properties array
		checkNext(JsonToken.START_ARRAY);

		//read properties
		while (parser.nextToken() != JsonToken.END_ARRAY) { //until we reach the end properties array
			checkCurrent(JsonToken.START_ARRAY);
			parser.nextToken();
			parseProperty(components);
		}

		//start sub-components array
		checkNext(JsonToken.START_ARRAY);

		//read sub-components
		while (parser.nextToken() != JsonToken.END_ARRAY) { //until we reach the end sub-components array
			checkCurrent(JsonToken.START_ARRAY);
			parser.nextToken();
			parseComponent(new ArrayList<String>(components));
		}

		//read the end of the component array (e.g. the last bracket in this example: ["comp", [ /* props */ ], [ /* comps */] ])
		checkNext(JsonToken.END_ARRAY);
	}

	private void parseProperty(List<String> components) throws IOException {
		//get property name
		checkCurrent(JsonToken.VALUE_STRING);
		String propertyName = parser.getValueAsString().toLowerCase();

		ICalParameters parameters = parseParameters();

		//get data type
		checkNext(JsonToken.VALUE_STRING);
		String dataTypeStr = parser.getText();
		ICalDataType dataType = "unknown".equals(dataTypeStr) ? null : ICalDataType.get(dataTypeStr);

		//get property value(s)
		List<JsonValue> values = parseValues();

		JCalValue value = new JCalValue(values);
		listener.readProperty(components, propertyName, parameters, dataType, value);
	}

	private ICalParameters parseParameters() throws IOException {
		checkNext(JsonToken.START_OBJECT);

		ICalParameters parameters = new ICalParameters();
		while (parser.nextToken() != JsonToken.END_OBJECT) {
			String parameterName = parser.getText();

			if (parser.nextToken() == JsonToken.START_ARRAY) {
				//multi-valued parameter
				while (parser.nextToken() != JsonToken.END_ARRAY) {
					parameters.put(parameterName, parser.getText());
				}
			} else {
				parameters.put(parameterName, parser.getValueAsString());
			}
		}

		return parameters;
	}

	private List<JsonValue> parseValues() throws IOException {
		List<JsonValue> values = new ArrayList<JsonValue>();
		while (parser.nextToken() != JsonToken.END_ARRAY) { //until we reach the end of the property array
			JsonValue value = parseValue();
			values.add(value);
		}
		return values;
	}

	private Object parseValueElement() throws IOException {
		switch (parser.getCurrentToken()) {
		case VALUE_FALSE:
		case VALUE_TRUE:
			return parser.getBooleanValue();
		case VALUE_NUMBER_FLOAT:
			return parser.getDoubleValue();
		case VALUE_NUMBER_INT:
			return parser.getLongValue();
		case VALUE_NULL:
			return null;
		default:
			return parser.getText();
		}
	}

	private List<JsonValue> parseValueArray() throws IOException {
		List<JsonValue> array = new ArrayList<JsonValue>();

		while (parser.nextToken() != JsonToken.END_ARRAY) {
			JsonValue value = parseValue();
			array.add(value);
		}

		return array;
	}

	private Map<String, JsonValue> parseValueObject() throws IOException {
		Map<String, JsonValue> object = new HashMap<String, JsonValue>();

		parser.nextToken();
		while (parser.getCurrentToken() != JsonToken.END_OBJECT) {
			checkCurrent(JsonToken.FIELD_NAME);

			String key = parser.getText();
			parser.nextToken();
			JsonValue value = parseValue();
			object.put(key, value);

			parser.nextToken();
		}

		return object;
	}

	private JsonValue parseValue() throws IOException {
		switch (parser.getCurrentToken()) {
		case START_ARRAY:
			return new JsonValue(parseValueArray());
		case START_OBJECT:
			return new JsonValue(parseValueObject());
		default:
			return new JsonValue(parseValueElement());
		}
	}

	private void checkNext(JsonToken expected) throws IOException {
		JsonToken actual = parser.nextToken();
		check(expected, actual);
	}

	private void checkCurrent(JsonToken expected) throws JCalParseException {
		JsonToken actual = parser.getCurrentToken();
		check(expected, actual);
	}

	private void check(JsonToken expected, JsonToken actual) throws JCalParseException {
		if (actual != expected) {
			throw new JCalParseException(expected, actual);
		}
	}

	/**
	 * Determines whether the end of the data stream has been reached.
	 * @return true if the end has been reached, false if not
	 */
	public boolean eof() {
		return eof;
	}

	/**
	 * Handles the iCalendar data as it is read off the data stream.
	 * @author Michael Angstadt
	 */
	public interface JCalDataStreamListener {
		/**
		 * Called when the parser begins to read a component.
		 * @param parentHierarchy the component's parent components
		 * @param componentName the component name (e.g. "vevent")
		 */
		void readComponent(List<String> parentHierarchy, String componentName);

		/**
		 * Called when a property is read.
		 * @param componentHierarchy the hierarchy of components that the
		 * property belongs to
		 * @param propertyName the property name (e.g. "summary")
		 * @param parameters the parameters
		 * @param dataType the data type (e.g. "text")
		 * @param value the property value
		 */
		void readProperty(List<String> componentHierarchy, String propertyName, ICalParameters parameters, ICalDataType dataType, JCalValue value);
	}

	/**
	 * Closes the underlying {@link Reader} object.
	 */
	public void close() throws IOException {
		if (parser != null) {
			parser.close();
		}
		if (reader != null) {
			reader.close();
		}
	}
}
