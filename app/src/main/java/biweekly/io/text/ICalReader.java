package biweekly.io.text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import biweekly.ICalDataType;
import biweekly.ICalVersion;
import biweekly.ICalendar;
import biweekly.component.ICalComponent;
import biweekly.io.CannotParseException;
import biweekly.io.DataModelConversionException;
import biweekly.io.ParseWarning;
import biweekly.io.SkipMeException;
import biweekly.io.StreamReader;
import biweekly.io.scribe.ScribeIndex;
import biweekly.io.scribe.component.ICalComponentScribe;
import biweekly.io.scribe.property.ICalPropertyScribe;
import biweekly.io.scribe.property.RawPropertyScribe;
import biweekly.parameter.Encoding;
import biweekly.parameter.ICalParameters;
import biweekly.property.ICalProperty;
import biweekly.util.Utf8Reader;

import com.github.mangstadt.vinnie.VObjectProperty;
import com.github.mangstadt.vinnie.io.Context;
import com.github.mangstadt.vinnie.io.SyntaxRules;
import com.github.mangstadt.vinnie.io.VObjectDataListener;
import com.github.mangstadt.vinnie.io.VObjectReader;

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
 * Parses {@link ICalendar} objects from a plain-text iCalendar data stream.
 * </p>
 * <p>
 * <b>Example:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * File file = new File("icals.ics");
 * ICalReader reader = null;
 * try {
 *   reader = new ICalReader(file);
 *   ICalendar ical;
 *   while ((ical = reader.readNext()) != null) {
 *     //...
 *   }
 * } finally {
 *   if (reader != null) reader.close();
 * }
 * </pre>
 * @author Michael Angstadt
 * @see <a href="http://www.imc.org/pdi/pdiproddev.html">1.0 specs</a>
 * @see <a href="https://tools.ietf.org/html/rfc2445">RFC 2445</a>
 * @see <a href="http://tools.ietf.org/html/rfc5545">RFC 5545</a>
 */
public class ICalReader extends StreamReader {
	private static final String VCALENDAR_COMPONENT_NAME = ScribeIndex.getICalendarScribe().getComponentName(); //"VCALENDAR"

	private final VObjectReader reader;
	private final ICalVersion defaultVersion;

	/**
	 * Creates a new iCalendar reader.
	 * @param str the string to read from
	 */
	public ICalReader(String str) {
		this(str, ICalVersion.V2_0);
	}

	/**
	 * Creates a new iCalendar reader.
	 * @param str the string to read from
	 * @param defaultVersion the version to assume the iCalendar object is in
	 * until a VERSION property is encountered (defaults to 2.0)
	 */
	public ICalReader(String str, ICalVersion defaultVersion) {
		this(new StringReader(str), defaultVersion);
	}

	/**
	 * Creates a new iCalendar reader.
	 * @param in the input stream to read from
	 */
	public ICalReader(InputStream in) {
		this(in, ICalVersion.V2_0);
	}

	/**
	 * Creates a new iCalendar reader.
	 * @param defaultVersion the version to assume the iCalendar object is in
	 * until a VERSION property is encountered (defaults to 2.0)
	 * @param in the input stream to read from
	 */
	public ICalReader(InputStream in, ICalVersion defaultVersion) {
		this(new Utf8Reader(in), defaultVersion);
	}

	/**
	 * Creates a new iCalendar reader.
	 * @param file the file to read from
	 * @throws FileNotFoundException if the file doesn't exist
	 */
	public ICalReader(File file) throws FileNotFoundException {
		this(file, ICalVersion.V2_0);
	}

	/**
	 * Creates a new iCalendar reader.
	 * @param file the file to read from
	 * @param defaultVersion the version to assume the iCalendar object is in
	 * until a VERSION property is encountered (defaults to 2.0)
	 * @throws FileNotFoundException if the file doesn't exist
	 */
	public ICalReader(File file, ICalVersion defaultVersion) throws FileNotFoundException {
		this(new BufferedReader(new Utf8Reader(file)), defaultVersion);
	}

	/**
	 * Creates a new iCalendar reader.
	 * @param reader the reader to read from
	 */
	public ICalReader(Reader reader) {
		this(reader, ICalVersion.V2_0);
	}

	/**
	 * Creates a new iCalendar reader.
	 * @param reader the reader to read from
	 * @param defaultVersion the version to assume the iCalendar object is in
	 * until a VERSION property is encountered (defaults to 2.0)
	 */
	public ICalReader(Reader reader, ICalVersion defaultVersion) {
		SyntaxRules rules = SyntaxRules.iCalendar();
		rules.setDefaultSyntaxStyle(defaultVersion.getSyntaxStyle());
		this.reader = new VObjectReader(reader, rules);
		this.defaultVersion = defaultVersion;
	}

	/**
	 * Gets whether the reader will decode parameter values that use circumflex
	 * accent encoding (enabled by default). This escaping mechanism allows
	 * newlines and double quotes to be included in parameter values.
	 * @return true if circumflex accent decoding is enabled, false if not
	 * @see VObjectReader#isCaretDecodingEnabled()
	 */
	public boolean isCaretDecodingEnabled() {
		return reader.isCaretDecodingEnabled();
	}

	/**
	 * Sets whether the reader will decode parameter values that use circumflex
	 * accent encoding (enabled by default). This escaping mechanism allows
	 * newlines and double quotes to be included in parameter values. This only
	 * applies to version 2.0 iCalendar objects.
	 * @param enable true to use circumflex accent decoding, false not to
	 * @see VObjectReader#setCaretDecodingEnabled(boolean)
	 */
	public void setCaretDecodingEnabled(boolean enable) {
		reader.setCaretDecodingEnabled(enable);
	}

	/**
	 * <p>
	 * Gets the character set to use when decoding quoted-printable values if
	 * the property has no CHARSET parameter, or if the CHARSET parameter is not
	 * a valid character set.
	 * </p>
	 * <p>
	 * By default, the Reader's character encoding will be used. If the Reader
	 * has no character encoding, then the system's default character encoding
	 * will be used.
	 * </p>
	 * @return the character set
	 */
	public Charset getDefaultQuotedPrintableCharset() {
		return reader.getDefaultQuotedPrintableCharset();
	}

	/**
	 * <p>
	 * Sets the character set to use when decoding quoted-printable values if
	 * the property has no CHARSET parameter, or if the CHARSET parameter is not
	 * a valid character set.
	 * </p>
	 * <p>
	 * By default, the Reader's character encoding will be used. If the Reader
	 * has no character encoding, then the system's default character encoding
	 * will be used.
	 * </p>
	 * @param charset the character set
	 */
	public void setDefaultQuotedPrintableCharset(Charset charset) {
		reader.setDefaultQuotedPrintableCharset(charset);
	}

	/**
	 * <p>
	 * Gets the iCalendar version that this reader will assume each iCalendar
	 * object is formatted in up until a VERSION property is encountered.
	 * </p>
	 * <p>
	 * All standards-compliant iCalendar objects contain a VERSION property at
	 * the very beginning of the object, so for the vast majority of iCalendar
	 * objects, this setting does nothing. This setting is needed for when the
	 * iCalendar object does not have a VERSION property or for when the VERSION
	 * property is not located at the beginning of the object.
	 * </p>
	 * @return the default version (defaults to "2.0")
	 */
	public ICalVersion getDefaultVersion() {
		return defaultVersion;
	}

	@Override
	protected ICalendar _readNext() throws IOException {
		VObjectDataListenerImpl listener = new VObjectDataListenerImpl();
		reader.parse(listener);
		return listener.ical;
	}

	private class VObjectDataListenerImpl implements VObjectDataListener {
		private ICalendar ical = null;
		private ICalVersion version = defaultVersion;
		private ComponentStack stack = new ComponentStack();

		public void onComponentBegin(String name, Context vobjectContext) {
			//ignore everything until a VCALENDAR component is read
			if (ical == null && !isVCalendarComponent(name)) {
				return;
			}

			ICalComponent parentComponent = stack.peek();

			ICalComponentScribe<? extends ICalComponent> scribe = index.getComponentScribe(name, version);
			ICalComponent component = scribe.emptyInstance();
			stack.push(component);

			if (parentComponent == null) {
				ical = (ICalendar) component;
				context.setVersion(version);
			} else {
				parentComponent.addComponent(component);
			}
		}

		public void onComponentEnd(String name, Context vobjectContext) {
			//VCALENDAR component not read yet, ignore
			if (ical == null) {
				return;
			}

			/*
			 * VObjectDataListener guarantees correct ordering of component
			 * begin/end callback invocations (see javadocs), so we can pop
			 * blindly without checking if the component name matches.
			 */
			stack.pop();

			//stop reading when "END:VCALENDAR" is reached
			if (stack.isEmpty()) {
				vobjectContext.stop();
			}
		}

		public void onProperty(VObjectProperty vobjectProperty, Context vobjectContext) {
			//VCALENDAR component not read yet, ignore
			if (ical == null) {
				return;
			}

			String propertyName = vobjectProperty.getName();
			ICalParameters parameters = new ICalParameters(vobjectProperty.getParameters().getMap());
			String value = vobjectProperty.getValue();
			
			context.getWarnings().clear();
			context.setLineNumber(vobjectContext.getLineNumber());
			context.setPropertyName(propertyName);

			ICalPropertyScribe<? extends ICalProperty> scribe = index.getPropertyScribe(propertyName, version);

			//process nameless parameters
			processNamelessParameters(parameters, version);

			//get the data type (VALUE parameter)
			ICalDataType dataType = parameters.getValue();
			parameters.setValue(null);
			if (dataType == null) {
				//use the property's default data type if there is no VALUE parameter
				dataType = scribe.defaultDataType(version);
			}

			ICalComponent parentComponent = stack.peek();
			try {
				ICalProperty property = scribe.parseText(value, dataType, parameters, context);
				parentComponent.addProperty(property);
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
				ICalProperty property = new RawPropertyScribe(propertyName).parseText(value, dataType, parameters, context);
				parentComponent.addProperty(property);
			} catch (DataModelConversionException e) {
				for (ICalProperty property : e.getProperties()) {
					parentComponent.addProperty(property);
				}
				for (ICalComponent component : e.getComponents()) {
					parentComponent.addComponent(component);
				}
			}

			warnings.addAll(context.getWarnings());
		}

		public void onVersion(String value, Context vobjectContext) {
			//ignore if we are not directly under the root VCALENDAR component
			if (stack.size() != 1) {
				return;
			}

			version = ICalVersion.get(value);
			context.setVersion(version);
		}

		public void onWarning(com.github.mangstadt.vinnie.io.Warning warning, VObjectProperty property, Exception thrown, Context vobjectContext) {
			//VCALENDAR component not read yet, ignore
			if (ical == null) {
				return;
			}

			//@formatter:off
			warnings.add(new ParseWarning.Builder()
				.lineNumber(vobjectContext.getLineNumber())
				.propertyName((property == null) ? null : property.getName())
				.message(warning.getMessage())
				.build()
			);
			//@formatter:on
		}

		private boolean isVCalendarComponent(String componentName) {
			return VCALENDAR_COMPONENT_NAME.equals(componentName);
		}

		/**
		 * Assigns names to all nameless parameters. v2.0 requires all
		 * parameters to have names, but v1.0 does not.
		 * @param parameters the parameters
		 * @param version the iCal version
		 */
		private void processNamelessParameters(ICalParameters parameters, ICalVersion version) {
			List<String> namelessParamValues = parameters.removeAll(null);
			if (namelessParamValues.isEmpty()) {
				return;
			}

			if (version != ICalVersion.V1_0) {
				//@formatter:off
				warnings.add(new ParseWarning.Builder(context)
					.message(4, namelessParamValues)
					.build()
				);
				//@formatter:on
			}

			for (String paramValue : namelessParamValues) {
				String paramName = guessParameterName(paramValue);
				parameters.put(paramName, paramValue);
			}
		}

		/**
		 * Makes a guess as to what a parameter value's name should be.
		 * @param value the parameter value
		 * @return the guessed name
		 */
		private String guessParameterName(String value) {
			if (ICalDataType.find(value) != null) {
				return ICalParameters.VALUE;
			}

			if (Encoding.find(value) != null) {
				return ICalParameters.ENCODING;
			}

			//otherwise, assume it's a TYPE
			return ICalParameters.TYPE;
		}
	}

	/**
	 * Keeps track of the hierarchy of nested components.
	 */
	private static class ComponentStack {
		private final List<ICalComponent> components = new ArrayList<ICalComponent>();

		/**
		 * Gets the top component from the stack.
		 * @return the component or null if the stack is empty
		 */
		public ICalComponent peek() {
			return isEmpty() ? null : components.get(components.size() - 1);
		}

		/**
		 * Adds a component to the stack
		 * @param component the component
		 */
		public void push(ICalComponent component) {
			components.add(component);
		}

		/**
		 * Removes the top component from the stack and returns it.
		 * @return the top component or null if the stack is empty
		 */
		public ICalComponent pop() {
			return isEmpty() ? null : components.remove(components.size() - 1);
		}

		/**
		 * Determines if the stack is empty.
		 * @return true if it's empty, false if not
		 */
		public boolean isEmpty() {
			return components.isEmpty();
		}

		/**
		 * Gets the size of the stack.
		 * @return the size
		 */
		public int size() {
			return components.size();
		}
	}

	/**
	 * Closes the input stream.
	 * @throws IOException if there's a problem closing the input stream
	 */
	public void close() throws IOException {
		reader.close();
	}
}
