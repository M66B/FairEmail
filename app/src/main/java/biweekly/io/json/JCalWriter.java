package biweekly.io.json;

import java.io.File;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Collection;
import java.util.List;

import biweekly.ICalDataType;
import biweekly.ICalVersion;
import biweekly.ICalendar;
import biweekly.component.ICalComponent;
import biweekly.component.VTimezone;
import biweekly.io.SkipMeException;
import biweekly.io.StreamWriter;
import biweekly.io.scribe.component.ICalComponentScribe;
import biweekly.io.scribe.property.ICalPropertyScribe;
import biweekly.parameter.ICalParameters;
import biweekly.property.ICalProperty;
import biweekly.property.Version;
import biweekly.util.Utf8Writer;

import com.fasterxml.jackson.core.JsonGenerator;

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
 * Writes {@link ICalendar} objects to a JSON data stream (jCal).
 * </p>
 * <p>
 * <b>Example:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * ICalendar ical1 = ...
 * ICalendar ical2 = ...
 * File file = new File("icals.json");
 * JCalWriter writer = null;
 * try {
 *   writer = new JCalWriter(file);
 *   writer.write(ical1);
 *   writer.write(ical2);
 * } finally {
 *   if (writer != null) writer.close();
 * }
 * </pre>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc7265">RFC 7265</a>
 */
public class JCalWriter extends StreamWriter implements Flushable {
	private final JCalRawWriter writer;
	private final ICalVersion targetVersion = ICalVersion.V2_0;

	/**
	 * @param out the output stream to write to (UTF-8 encoding will be used)
	 */
	public JCalWriter(OutputStream out) {
		this(new Utf8Writer(out));
	}

	/**
	 * @param out the output stream to write to (UTF-8 encoding will be used)
	 * @param wrapInArray true to wrap all iCalendar objects in a parent array,
	 * false not to (useful when writing more than one iCalendar object)
	 */
	public JCalWriter(OutputStream out, boolean wrapInArray) {
		this(new Utf8Writer(out), wrapInArray);
	}

	/**
	 * @param file the file to write to (UTF-8 encoding will be used)
	 * @throws IOException if the file cannot be written to
	 */
	public JCalWriter(File file) throws IOException {
		this(new Utf8Writer(file));
	}

	/**
	 * @param file the file to write to (UTF-8 encoding will be used)
	 * @param wrapInArray true to wrap all iCalendar objects in a parent array,
	 * false not to (useful when writing more than one iCalendar object)
	 * @throws IOException if the file cannot be written to
	 */
	public JCalWriter(File file, boolean wrapInArray) throws IOException {
		this(new Utf8Writer(file), wrapInArray);
	}

	/**
	 * @param writer the writer to write to
	 */
	public JCalWriter(Writer writer) {
		this(writer, false);
	}

	/**
	 * @param writer the writer to write to
	 * @param wrapInArray true to wrap all iCalendar objects in a parent array,
	 * false not to (useful when writing more than one iCalendar object)
	 */
	public JCalWriter(Writer writer, boolean wrapInArray) {
		this.writer = new JCalRawWriter(writer, wrapInArray);
	}

	/**
	 * @param generator the generator to write to
	 */
	public JCalWriter(JsonGenerator generator) {
		this.writer = new JCalRawWriter(generator);
	}

	/**
	 * Gets whether or not the JSON will be pretty-printed.
	 * @return true if it will be pretty-printed, false if not (defaults to
	 * false)
	 */
	public boolean isPrettyPrint() {
		return writer.isPrettyPrint();
	}

	/**
	 * Sets whether or not to pretty-print the JSON.
	 * @param prettyPrint true to pretty-print it, false not to (defaults to
	 * false)
	 */
	public void setPrettyPrint(boolean prettyPrint) {
		writer.setPrettyPrint(prettyPrint);
	}

	@Override
	protected void _write(ICalendar ical) throws IOException {
		writeComponent(ical);
	}

	@Override
	protected ICalVersion getTargetVersion() {
		return targetVersion;
	}

	/**
	 * Writes a component to the data stream.
	 * @param component the component to write
	 * @throws IllegalArgumentException if the scribe class for a component or
	 * property object cannot be found (only happens when an experimental
	 * property/component scribe is not registered with the
	 * {@code registerScribe} method.)
	 * @throws IOException if there's a problem writing to the data stream
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void writeComponent(ICalComponent component) throws IOException {
		ICalComponentScribe componentScribe = index.getComponentScribe(component);
		writer.writeStartComponent(componentScribe.getComponentName().toLowerCase());

		List propertyObjs = componentScribe.getProperties(component);
		if (component instanceof ICalendar && component.getProperty(Version.class) == null) {
			propertyObjs.add(0, new Version(targetVersion));
		}

		//write properties
		for (Object propertyObj : propertyObjs) {
			context.setParent(component); //set parent here incase a scribe resets the parent
			ICalProperty property = (ICalProperty) propertyObj;
			ICalPropertyScribe propertyScribe = index.getPropertyScribe(property);

			//marshal property
			ICalParameters parameters;
			JCalValue value;
			try {
				parameters = propertyScribe.prepareParameters(property, context);
				value = propertyScribe.writeJson(property, context);
			} catch (SkipMeException e) {
				continue;
			}

			//write property
			String propertyName = propertyScribe.getPropertyName(targetVersion).toLowerCase();
			ICalDataType dataType = propertyScribe.dataType(property, targetVersion);
			writer.writeProperty(propertyName, parameters, dataType, value);
		}

		//write sub-components
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
		for (Object subComponentObj : subComponents) {
			ICalComponent subComponent = (ICalComponent) subComponentObj;
			writeComponent(subComponent);
		}

		writer.writeEndComponent();
	}

	/**
	 * Flushes the stream.
	 * @throws IOException if there's a problem flushing the stream
	 */
	public void flush() throws IOException {
		writer.flush();
	}

	/**
	 * Finishes writing the JSON document and closes the underlying
	 * {@link Writer} object.
	 * @throws IOException if there's a problem closing the stream
	 */
	public void close() throws IOException {
		writer.close();
	}

	/**
	 * Finishes writing the JSON document so that it is syntactically correct.
	 * No more iCalendar objects can be written once this method is called.
	 * @throws IOException if there's a problem writing to the data stream
	 */
	public void closeJsonStream() throws IOException {
		writer.closeJsonStream();
	}
}
