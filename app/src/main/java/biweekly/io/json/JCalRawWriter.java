package biweekly.io.json;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import biweekly.ICalDataType;
import biweekly.Messages;
import biweekly.parameter.ICalParameters;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.core.PrettyPrinter;

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
 * Writes data to an iCalendar JSON data stream (jCal).
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc7265">RFC 7265</a>
 */
public class JCalRawWriter implements Closeable, Flushable {
	private final Writer writer;
	private final boolean wrapInArray;
	private final LinkedList<Info> stack = new LinkedList<Info>();
	private JsonGenerator generator;
	private boolean prettyPrint = false;
	private boolean componentEnded = false;
	private boolean closeGenerator = true;
	private PrettyPrinter prettyPrinter;

	/**
	 * @param writer the writer to wrap
	 * @param wrapInArray true to wrap everything in an array, false not to
	 * (useful when writing more than one iCalendar object)
	 */
	public JCalRawWriter(Writer writer, boolean wrapInArray) {
		this.writer = writer;
		this.wrapInArray = wrapInArray;
	}

	/**
	 * @param generator the generator to write to
	 */
	public JCalRawWriter(JsonGenerator generator) {
		this.writer = null;
		this.generator = generator;
		this.closeGenerator = false;
		this.wrapInArray = false;
	}

	/**
	 * Gets whether or not the JSON will be pretty-printed.
	 * @return true if it will be pretty-printed, false if not (defaults to
	 * false)
	 */
	public boolean isPrettyPrint() {
		return prettyPrint;
	}

	/**
	 * Sets whether or not to pretty-print the JSON.
	 * @param prettyPrint true to pretty-print it, false not to (defaults to
	 * false)
	 */
	public void setPrettyPrint(boolean prettyPrint) {
		this.prettyPrint = prettyPrint;
	}

	/**
	 * Sets the pretty printer to pretty-print the JSON with. Note that this
	 * method implicitly enables indenting, so {@code setPrettyPrint(true)} does
	 * not also need to be called.
	 * @param prettyPrinter the custom pretty printer (defaults to an instance
	 * of {@link JCalPrettyPrinter}, if {@code setPrettyPrint(true)} has been
	 * called)
	 */
	public void setPrettyPrinter(PrettyPrinter prettyPrinter) {
		prettyPrint = true;
		this.prettyPrinter = prettyPrinter;
	}

	/**
	 * Writes the beginning of a new component array.
	 * @param componentName the component name (e.g. "vevent")
	 * @throws IOException if there's an I/O problem
	 */
	public void writeStartComponent(String componentName) throws IOException {
		if (generator == null) {
			init();
		}

		componentEnded = false;

		if (!stack.isEmpty()) {
			Info parent = stack.getLast();
			if (!parent.wroteEndPropertiesArray) {
				generator.writeEndArray();
				parent.wroteEndPropertiesArray = true;
			}
			if (!parent.wroteStartSubComponentsArray) {
				generator.writeStartArray();
				parent.wroteStartSubComponentsArray = true;
			}
		}

		generator.writeStartArray();
		generator.writeString(componentName);
		generator.writeStartArray(); //start properties array

		stack.add(new Info());
	}

	/**
	 * Closes the current component array.
	 * @throws IllegalStateException if there are no open components (
	 * {@link #writeStartComponent(String)} must be called first)
	 * @throws IOException if there's an I/O problem
	 */
	public void writeEndComponent() throws IOException {
		if (stack.isEmpty()) {
			throw new IllegalStateException(Messages.INSTANCE.getExceptionMessage(2));
		}
		Info cur = stack.removeLast();

		if (!cur.wroteEndPropertiesArray) {
			generator.writeEndArray();
		}
		if (!cur.wroteStartSubComponentsArray) {
			generator.writeStartArray();
		}

		generator.writeEndArray(); //end sub-components array
		generator.writeEndArray(); //end the array of this component

		componentEnded = true;
	}

	/**
	 * Writes a property to the current component.
	 * @param propertyName the property name (e.g. "version")
	 * @param dataType the property's data type (e.g. "text")
	 * @param value the property value
	 * @throws IllegalStateException if there are no open components (
	 * {@link #writeStartComponent(String)} must be called first) or if the last
	 * method called was {@link #writeEndComponent()}.
	 * @throws IOException if there's an I/O problem
	 */
	public void writeProperty(String propertyName, ICalDataType dataType, JCalValue value) throws IOException {
		writeProperty(propertyName, new ICalParameters(), dataType, value);
	}

	/**
	 * Writes a property to the current component.
	 * @param propertyName the property name (e.g. "version")
	 * @param parameters the parameters
	 * @param dataType the property's data type (e.g. "text")
	 * @param value the property value
	 * @throws IllegalStateException if there are no open components (
	 * {@link #writeStartComponent(String)} must be called first) or if the last
	 * method called was {@link #writeEndComponent()}.
	 * @throws IOException if there's an I/O problem
	 */
	public void writeProperty(String propertyName, ICalParameters parameters, ICalDataType dataType, JCalValue value) throws IOException {
		if (stack.isEmpty()) {
			throw new IllegalStateException(Messages.INSTANCE.getExceptionMessage(2));
		}
		if (componentEnded) {
			throw new IllegalStateException(Messages.INSTANCE.getExceptionMessage(3));
		}

		generator.setCurrentValue(JCalPrettyPrinter.PROPERTY_VALUE);

		generator.writeStartArray();

		//write the property name
		generator.writeString(propertyName);

		//write parameters
		generator.writeStartObject();
		for (Map.Entry<String, List<String>> entry : parameters) {
			String name = entry.getKey().toLowerCase();
			List<String> values = entry.getValue();
			if (values.isEmpty()) {
				continue;
			}

			if (values.size() == 1) {
				generator.writeStringField(name, values.get(0));
			} else {
				generator.writeArrayFieldStart(name);
				for (String paramValue : values) {
					generator.writeString(paramValue);
				}
				generator.writeEndArray();
			}
		}
		generator.writeEndObject();

		//write data type
		generator.writeString((dataType == null) ? "unknown" : dataType.getName().toLowerCase());

		//write value
		for (JsonValue jsonValue : value.getValues()) {
			writeValue(jsonValue);
		}

		generator.writeEndArray();

		generator.setCurrentValue(null);
	}

	private void writeValue(JsonValue jsonValue) throws IOException {
		if (jsonValue.isNull()) {
			generator.writeNull();
			return;
		}

		Object val = jsonValue.getValue();
		if (val != null) {
			if (val instanceof Byte) {
				generator.writeNumber((Byte) val);
			} else if (val instanceof Short) {
				generator.writeNumber((Short) val);
			} else if (val instanceof Integer) {
				generator.writeNumber((Integer) val);
			} else if (val instanceof Long) {
				generator.writeNumber((Long) val);
			} else if (val instanceof Float) {
				generator.writeNumber((Float) val);
			} else if (val instanceof Double) {
				generator.writeNumber((Double) val);
			} else if (val instanceof Boolean) {
				generator.writeBoolean((Boolean) val);
			} else {
				generator.writeString(val.toString());
			}
			return;
		}

		List<JsonValue> array = jsonValue.getArray();
		if (array != null) {
			generator.writeStartArray();
			for (JsonValue element : array) {
				writeValue(element);
			}
			generator.writeEndArray();
			return;
		}

		Map<String, JsonValue> object = jsonValue.getObject();
		if (object != null) {
			generator.writeStartObject();
			for (Map.Entry<String, JsonValue> entry : object.entrySet()) {
				generator.writeFieldName(entry.getKey());
				writeValue(entry.getValue());
			}
			generator.writeEndObject();
			return;
		}
	}

	/**
	 * Flushes the JSON stream.
	 * @throws IOException if there's a problem flushing the stream
	 */
	public void flush() throws IOException {
		if (generator == null) {
			return;
		}

		generator.flush();
	}

	/**
	 * Finishes writing the JSON document so that it is syntactically correct.
	 * No more data can be written once this method is called.
	 * @throws IOException if there's a problem closing the stream
	 */
	public void closeJsonStream() throws IOException {
		if (generator == null) {
			return;
		}

		while (!stack.isEmpty()) {
			writeEndComponent();
		}

		if (wrapInArray) {
			generator.writeEndArray();
		}

		if (closeGenerator) {
			generator.close();
		}
	}

	/**
	 * Finishes writing the JSON document and closes the underlying
	 * {@link Writer}.
	 * @throws IOException if there's a problem closing the stream
	 */
	public void close() throws IOException {
		if (generator == null) {
			return;
		}

		closeJsonStream();

		if (writer != null) {
			writer.close();
		}
	}

	private void init() throws IOException {
		JsonFactory factory = new JsonFactory();
		factory.configure(Feature.AUTO_CLOSE_TARGET, false);
		generator = factory.createGenerator(writer);

		if (prettyPrint) {
			if (prettyPrinter == null) {
				prettyPrinter = new JCalPrettyPrinter();
			}
			generator.setPrettyPrinter(prettyPrinter);
		}

		if (wrapInArray) {
			generator.writeStartArray();
		}
	}

	private static class Info {
		public boolean wroteEndPropertiesArray = false;
		public boolean wroteStartSubComponentsArray = false;
	}
}
