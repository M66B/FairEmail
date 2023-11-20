package biweekly.io.json;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;

import biweekly.ICalDataType;
import biweekly.ICalVersion;
import biweekly.ICalendar;
import biweekly.component.ICalComponent;
import biweekly.io.CannotParseException;
import biweekly.io.ParseWarning;
import biweekly.io.SkipMeException;
import biweekly.io.StreamReader;
import biweekly.io.json.JCalRawReader.JCalDataStreamListener;
import biweekly.io.scribe.ScribeIndex;
import biweekly.io.scribe.component.ICalComponentScribe;
import biweekly.io.scribe.component.ICalendarScribe;
import biweekly.io.scribe.property.ICalPropertyScribe;
import biweekly.io.scribe.property.RawPropertyScribe;
import biweekly.parameter.ICalParameters;
import biweekly.property.ICalProperty;
import biweekly.property.RawProperty;
import biweekly.property.Version;
import biweekly.util.Utf8Reader;

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
 * Parses {@link ICalendar} objects from a jCal data stream (JSON).
 * </p>
 * <p>
 * <b>Example:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * File file = new File("icals.json");
 * JCalReader reader = null;
 * try {
 *   reader = new JCalReader(file);
 *   ICalendar ical;
 *   while ((ical = reader.readNext()) != null) {
 *     //...
 *   }
 * } finally {
 *   if (reader != null) reader.close();
 * }
 * </pre>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc7265">RFC 7265</a>
 */
public class JCalReader extends StreamReader {
	private static final ICalendarScribe icalScribe = ScribeIndex.getICalendarScribe();
	private final JCalRawReader reader;

	/**
	 * @param json the JSON string to read from
	 */
	public JCalReader(String json) {
		this(new StringReader(json));
	}

	/**
	 * @param in the input stream to read from
	 */
	public JCalReader(InputStream in) {
		this(new Utf8Reader(in));
	}

	/**
	 * @param file the file to read from
	 * @throws FileNotFoundException if the file doesn't exist
	 */
	public JCalReader(File file) throws FileNotFoundException {
		this(new BufferedReader(new Utf8Reader(file)));
	}

	/**
	 * @param reader the reader to read from
	 */
	public JCalReader(Reader reader) {
		this.reader = new JCalRawReader(reader);
	}

	/**
	 * @param parser the parser to read from
	 */
	public JCalReader(JsonParser parser) {
		this.reader = new JCalRawReader(parser, true);
	}

	/**
	 * Reads the next iCalendar object from the JSON data stream.
	 * @return the iCalendar object or null if there are no more
	 * @throws JCalParseException if the jCal syntax is incorrect (the JSON
	 * syntax may be valid, but it is not in the correct jCal format).
	 * @throws JsonParseException if the JSON syntax is incorrect
	 * @throws IOException if there is a problem reading from the data stream
	 */
	@Override
	public ICalendar _readNext() throws IOException {
		if (reader.eof()) {
			return null;
		}

		context.setVersion(ICalVersion.V2_0);

		JCalDataStreamListenerImpl listener = new JCalDataStreamListenerImpl();
		reader.readNext(listener);

		return listener.getICalendar();
	}

	//@Override
	public void close() throws IOException {
		reader.close();
	}

	private class JCalDataStreamListenerImpl implements JCalDataStreamListener {
		private final Map<List<String>, ICalComponent> components = new HashMap<List<String>, ICalComponent>();

		public void readProperty(List<String> componentHierarchy, String propertyName, ICalParameters parameters, ICalDataType dataType, JCalValue value) {
			context.getWarnings().clear();
			context.setLineNumber(reader.getLineNum());
			context.setPropertyName(propertyName);

			//get the component that the property belongs to
			ICalComponent parent = components.get(componentHierarchy);

			//unmarshal the property
			ICalPropertyScribe<? extends ICalProperty> scribe = index.getPropertyScribe(propertyName, ICalVersion.V2_0);
			try {
				ICalProperty property = scribe.parseJson(value, dataType, parameters, context);
				warnings.addAll(context.getWarnings());

				//set "ICalendar.version" if the value of the VERSION property is recognized
				//otherwise, unmarshal VERSION like a normal property
				if (parent instanceof ICalendar && property instanceof Version) {
					Version version = (Version) property;
					ICalVersion icalVersion = version.toICalVersion();
					if (icalVersion != null) {
						context.setVersion(icalVersion);
						return;
					}
				}

				parent.addProperty(property);
			} catch (SkipMeException e) {
				//@formatter:off
				warnings.add(new ParseWarning.Builder(context)
					.message(0, e.getMessage())
					.build()
				);
				//@formatter:on
			} catch (CannotParseException e) {
				RawProperty property = new RawPropertyScribe(propertyName).parseJson(value, dataType, parameters, context);
				parent.addProperty(property);

				//@formatter:off
				warnings.add(new ParseWarning.Builder(context)
					.message(e)
					.build()
				);
				//@formatter:on
			}
		}

		public void readComponent(List<String> parentHierarchy, String componentName) {
			ICalComponentScribe<? extends ICalComponent> scribe = index.getComponentScribe(componentName, ICalVersion.V2_0);
			ICalComponent component = scribe.emptyInstance();

			ICalComponent parent = components.get(parentHierarchy);
			if (parent != null) {
				parent.addComponent(component);
			}

			List<String> hierarchy = new ArrayList<String>(parentHierarchy);
			hierarchy.add(componentName);
			components.put(hierarchy, component);
		}

		public ICalendar getICalendar() {
			if (components.isEmpty()) {
				//EOF
				return null;
			}

			ICalComponent component = components.get(Collections.singletonList(icalScribe.getComponentName().toLowerCase()));
			if (component == null) {
				//should never happen because the parser always looks for a "vcalendar" component
				return null;
			}

			if (component instanceof ICalendar) {
				//should happen every time
				return (ICalendar) component;
			}

			//this will only happen if the user decides to override the ICalendarScribe for some reason
			ICalendar ical = icalScribe.emptyInstance();
			ical.addComponent(component);
			return ical;
		}
	}
}
