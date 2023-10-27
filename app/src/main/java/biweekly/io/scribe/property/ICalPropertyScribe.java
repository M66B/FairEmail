package biweekly.io.scribe.property;

import static biweekly.io.xml.XCalNamespaceContext.XCAL_NS;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import biweekly.ICalDataType;
import biweekly.ICalVersion;
import biweekly.ICalendar;
import biweekly.ValidationWarning;
import biweekly.component.Observance;
import biweekly.component.VTimezone;
import biweekly.io.CannotParseException;
import biweekly.io.DataModelConversionException;
import biweekly.io.ParseContext;
import biweekly.io.SkipMeException;
import biweekly.io.TimezoneAssignment;
import biweekly.io.TimezoneInfo;
import biweekly.io.WriteContext;
import biweekly.io.json.JCalValue;
import biweekly.io.json.JsonValue;
import biweekly.io.xml.XCalElement;
import biweekly.io.xml.XCalElement.XCalValue;
import biweekly.parameter.ICalParameters;
import biweekly.property.ICalProperty;
import biweekly.property.ValuedProperty;
import biweekly.util.DateTimeComponents;
import biweekly.util.ICalDate;
import biweekly.util.ICalDateFormat;
import biweekly.util.ListMultimap;

import com.github.mangstadt.vinnie.io.VObjectPropertyValues;

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
 * Base class for iCalendar property scribes.
 * @param <T> the property class
 * @author Michael Angstadt
 */
public abstract class ICalPropertyScribe<T extends ICalProperty> {
	private static final Set<ICalVersion> allVersions = Collections.unmodifiableSet(EnumSet.allOf(ICalVersion.class));

	protected final Class<T> clazz;
	protected final String propertyName;
	private final ICalDataType defaultDataType;
	protected final QName qname;

	/**
	 * Creates a new scribe.
	 * @param clazz the property class
	 * @param propertyName the property name (e.g. "VERSION")
	 */
	public ICalPropertyScribe(Class<T> clazz, String propertyName) {
		this(clazz, propertyName, null);
	}

	/**
	 * Creates a new scribe.
	 * @param clazz the property class
	 * @param propertyName the property name (e.g. "VERSION")
	 * @param defaultDataType the property's default data type (e.g. "text") or
	 * null if unknown
	 */
	public ICalPropertyScribe(Class<T> clazz, String propertyName, ICalDataType defaultDataType) {
		this(clazz, propertyName, defaultDataType, new QName(XCAL_NS, propertyName.toLowerCase()));
	}

	/**
	 * Creates a new scribe.
	 * @param clazz the property class
	 * @param propertyName the property name (e.g. "VERSION")
	 * @param defaultDataType the property's default data type (e.g. "text") or
	 * null if unknown
	 * @param qname the XML element name and namespace to use for xCal documents
	 * (by default, the XML element name is set to the lower-cased property
	 * name, and the element namespace is set to the xCal namespace)
	 */
	public ICalPropertyScribe(Class<T> clazz, String propertyName, ICalDataType defaultDataType, QName qname) {
		this.clazz = clazz;
		this.propertyName = propertyName;
		this.defaultDataType = defaultDataType;
		this.qname = qname;
	}

	/**
	 * Gets the iCalendar versions that support this property. This method
	 * returns all iCalendar versions unless overridden by the child scribe.
	 * @return the iCalendar versions
	 */
	public Set<ICalVersion> getSupportedVersions() {
		return allVersions;
	}

	/**
	 * Gets the property class.
	 * @return the property class
	 */
	public Class<T> getPropertyClass() {
		return clazz;
	}

	/**
	 * Gets the property name. Child classes should override this method if the
	 * property's name differs between versions.
	 * @param version the iCalendar version (a few properties have different
	 * names under different versions)
	 * @return the property name (e.g. "DTSTART")
	 */
	public String getPropertyName(ICalVersion version) {
		return propertyName;
	}

	/**
	 * Gets this property's local name and namespace for xCal documents.
	 * @return the XML local name and namespace
	 */
	public QName getQName() {
		return qname;
	}

	/**
	 * Sanitizes a property's parameters before the property is written.
	 * @param property the property to write
	 * @param context the context
	 * @return the sanitized parameters
	 */
	public final ICalParameters prepareParameters(T property, WriteContext context) {
		return _prepareParameters(property, context);
	}

	/**
	 * Determines the default data type of this property.
	 * @param version the version of the iCalendar object being generated
	 * @return the data type or null if unknown
	 */
	public final ICalDataType defaultDataType(ICalVersion version) {
		return _defaultDataType(version);
	}

	/**
	 * Determines the data type of a property instance.
	 * @param property the property
	 * @param version the version of the iCalendar object being generated
	 * @return the data type or null if unknown
	 */
	public final ICalDataType dataType(T property, ICalVersion version) {
		return _dataType(property, version);
	}

	/**
	 * Marshals a property's value to a string.
	 * @param property the property
	 * @param context the context
	 * @return the marshalled property value
	 * @throws SkipMeException if the property should not be written to the data
	 * stream
	 * @throws DataModelConversionException if the property needs to be
	 * converted to something different in order to adhere to the data model of
	 * the iCalendar version being written (only applicable when writing 1.0
	 * vCals)
	 */
	public final String writeText(T property, WriteContext context) {
		return _writeText(property, context);
	}

	/**
	 * Marshals a property's value to an XML element (xCal).
	 * @param property the property
	 * @param element the property's XML element
	 * @param context the context
	 * @throws SkipMeException if the property should not be written to the data
	 * stream
	 */
	public final void writeXml(T property, Element element, WriteContext context) {
		XCalElement xcalElement = new XCalElement(element);
		_writeXml(property, xcalElement, context);
	}

	/**
	 * Marshals a property's value to a JSON data stream (jCal).
	 * @param property the property
	 * @param context the context
	 * @return the marshalled value
	 * @throws SkipMeException if the property should not be written to the data
	 * stream
	 */
	public final JCalValue writeJson(T property, WriteContext context) {
		return _writeJson(property, context);
	}

	/**
	 * Unmarshals a property from a plain-text iCalendar data stream.
	 * @param value the value as read off the wire
	 * @param dataType the data type of the property value. The property's VALUE
	 * parameter is used to determine the data type. If the property has no
	 * VALUE parameter, then this parameter will be set to the property's
	 * default datatype. Note that the VALUE parameter is removed from the
	 * property's parameter list after it has been read.
	 * @param parameters the parsed parameters
	 * @param context the parse context
	 * @return the unmarshalled property
	 * @throws CannotParseException if the scribe could not parse the property's
	 * value
	 * @throws SkipMeException if the property should not be added to the final
	 * {@link ICalendar} object
	 * @throws DataModelConversionException if the property should be converted
	 * to something different in order to adhere to the 2.0 data model (only
	 * thrown when parsing 1.0 vCals)
	 */
	public final T parseText(String value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
		T property = _parseText(value, dataType, parameters, context);
		property.setParameters(parameters);
		return property;
	}

	/**
	 * Unmarshals a property's value from an XML document (xCal).
	 * @param element the property's XML element
	 * @param parameters the property's parameters
	 * @param context the context
	 * @return the unmarshalled property
	 * @throws CannotParseException if the scribe could not parse the property's
	 * value
	 * @throws SkipMeException if the property should not be added to the final
	 * {@link ICalendar} object
	 */
	public final T parseXml(Element element, ICalParameters parameters, ParseContext context) {
		T property = _parseXml(new XCalElement(element), parameters, context);
		property.setParameters(parameters);
		return property;
	}

	/**
	 * Unmarshals a property's value from a JSON data stream (jCal).
	 * @param value the property's JSON value
	 * @param dataType the data type
	 * @param parameters the parsed parameters
	 * @param context the context
	 * @return the unmarshalled property
	 * @throws CannotParseException if the scribe could not parse the property's
	 * value
	 * @throws SkipMeException if the property should not be added to the final
	 * {@link ICalendar} object
	 */
	public final T parseJson(JCalValue value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
		T property = _parseJson(value, dataType, parameters, context);
		property.setParameters(parameters);
		return property;
	}

	/**
	 * <p>
	 * Sanitizes a property's parameters before the property is written.
	 * </p>
	 * <p>
	 * This method should be overridden by child classes that wish to tweak the
	 * property's parameters before the property is written. The default
	 * implementation of this method returns the property's parameters
	 * unmodified.
	 * </p>
	 * @param property the property to write
	 * @param context the context
	 * @return the sanitized parameters (this should be a *copy* of the
	 * property's parameters if modifications were made)
	 */
	protected ICalParameters _prepareParameters(T property, WriteContext context) {
		return property.getParameters();
	}

	/**
	 * <p>
	 * Determines the default data type of this property.
	 * </p>
	 * <p>
	 * This method should be overridden by child classes if a property's default
	 * data type changes depending the iCalendar version. The default
	 * implementation of this method returns the data type that was passed into
	 * the {@link #ICalPropertyScribe(Class, String, ICalDataType)} constructor.
	 * Null is returned if this constructor was not invoked.
	 * </p>
	 * @param version the version of the iCalendar object being generated
	 * @return the data type or null if unknown
	 */
	protected ICalDataType _defaultDataType(ICalVersion version) {
		return defaultDataType;
	}

	/**
	 * <p>
	 * Determines the data type of a property instance.
	 * </p>
	 * <p>
	 * This method should be overridden by child classes if a property's data
	 * type changes depending on its value. The default implementation of this
	 * method returns the property's default data type.
	 * </p>
	 * @param property the property
	 * @param version the version of the iCalendar object being generated
	 * @return the data type or null if unknown
	 */
	protected ICalDataType _dataType(T property, ICalVersion version) {
		return defaultDataType(version);
	}

	/**
	 * Marshals a property's value to a string.
	 * @param property the property
	 * @param context the write context
	 * @return the marshalled value
	 * @throws SkipMeException if the property should not be written to the data
	 * stream
	 * @throws DataModelConversionException if the property needs to be
	 * converted to something different in order to adhere to the data model of
	 * the iCalendar version being written (only applicable when writing 1.0
	 * vCals)
	 */
	protected abstract String _writeText(T property, WriteContext context);

	/**
	 * <p>
	 * Marshals a property's value to an XML element (xCal).
	 * </p>
	 * <p>
	 * This method should be overridden by child classes that wish to support
	 * xCal. The default implementation of this method will append one child
	 * element to the property's XML element. The child element's name will be
	 * that of the property's data type (retrieved using the {@link #dataType}
	 * method), and the child element's text content will be set to the
	 * property's marshalled plain-text value (retrieved using the
	 * {@link #writeText} method).
	 * </p>
	 * @param property the property
	 * @param element the property's XML element
	 * @param context the context
	 * @throws SkipMeException if the property should not be written to the data
	 * stream
	 */
	protected void _writeXml(T property, XCalElement element, WriteContext context) {
		String value = writeText(property, context);
		ICalDataType dataType = dataType(property, ICalVersion.V2_0);
		element.append(dataType, value);
	}

	/**
	 * <p>
	 * Marshals a property's value to a JSON data stream (jCal).
	 * </p>
	 * <p>
	 * This method should be overridden by child classes that wish to support
	 * jCal. The default implementation of this method will create a jCard
	 * property that has a single JSON string value (generated by the
	 * {@link #writeText} method).
	 * </p>
	 * @param property the property
	 * @param context the context
	 * @return the marshalled value
	 * @throws SkipMeException if the property should not be written to the data
	 * stream
	 */
	protected JCalValue _writeJson(T property, WriteContext context) {
		String value = writeText(property, context);
		return JCalValue.single(value);
	}

	/**
	 * Unmarshals a property from a plain-text iCalendar data stream.
	 * @param value the value as read off the wire
	 * @param dataType the data type of the property value. The property's VALUE
	 * parameter is used to determine the data type. If the property has no
	 * VALUE parameter, then this parameter will be set to the property's
	 * default datatype. Note that the VALUE parameter is removed from the
	 * property's parameter list after it has been read.
	 * @param parameters the parsed parameters. These parameters will be
	 * assigned to the property object once this method returns. Therefore, do
	 * not assign any parameters to the property object itself whilst inside of
	 * this method, or else they will be overwritten.
	 * @param context the parse context
	 * @return the unmarshalled property object
	 * @throws CannotParseException if the scribe could not parse the property's
	 * value
	 * @throws SkipMeException if the property should not be added to the final
	 * {@link ICalendar} object
	 * @throws DataModelConversionException if the property should be converted
	 * to something different in order to adhere to the 2.0 data model (only
	 * thrown when parsing 1.0 vCals)
	 */
	protected abstract T _parseText(String value, ICalDataType dataType, ICalParameters parameters, ParseContext context);

	/**
	 * <p>
	 * Unmarshals a property from an XML document (xCal).
	 * </p>
	 * <p>
	 * This method should be overridden by child classes that wish to support
	 * xCal. The default implementation of this method will find the first child
	 * element with the xCal namespace. The element's name will be used as the
	 * property's data type and its text content will be passed into the
	 * {@link #_parseText} method. If no such child element is found, then the
	 * parent element's text content will be passed into {@link #_parseText} and
	 * the data type will be null.
	 * </p>
	 * @param element the property's XML element
	 * @param parameters the parsed parameters. These parameters will be
	 * assigned to the property object once this method returns. Therefore, do
	 * not assign any parameters to the property object itself whilst inside of
	 * this method, or else they will be overwritten.
	 * @param context the context
	 * @return the unmarshalled property object
	 * @throws CannotParseException if the scribe could not parse the property's
	 * value
	 * @throws SkipMeException if the property should not be added to the final
	 * {@link ICalendar} object
	 */
	protected T _parseXml(XCalElement element, ICalParameters parameters, ParseContext context) {
		XCalValue firstValue = element.firstValue();
		ICalDataType dataType = firstValue.getDataType();
		String value = VObjectPropertyValues.escape(firstValue.getValue());
		return _parseText(value, dataType, parameters, context);
	}

	/**
	 * <p>
	 * Unmarshals a property from a JSON data stream (jCal).
	 * </p>
	 * <p>
	 * This method should be overridden by child classes that wish to support
	 * jCal. The default implementation of this method will convert the jCal
	 * property value to a string and pass it into the {@link #_parseText}
	 * method.
	 * </p>
	 * 
	 * <hr>
	 * 
	 * <p>
	 * The following paragraphs describe the way in which this method's default
	 * implementation converts a jCal value to a string:
	 * </p>
	 * <p>
	 * If the jCal value consists of a single, non-array, non-object value, then
	 * the value is converted to a string. Special characters (backslashes,
	 * commas, and semicolons) are escaped in order to simulate what the value
	 * might look like in a plain-text iCalendar object.<br>
	 * <code>["x-foo", {}, "text", "the;value"] --&gt; "the\;value"</code><br>
	 * <code>["x-foo", {}, "text", 2] --&gt; "2"</code>
	 * </p>
	 * <p>
	 * If the jCal value consists of multiple, non-array, non-object values,
	 * then all the values are appended together in a single string, separated
	 * by commas. Special characters (backslashes, commas, and semicolons) are
	 * escaped for each value in order to prevent commas from being treated as
	 * delimiters, and to simulate what the value might look like in a
	 * plain-text iCalendar object.<br>
	 * <code>["x-foo", {}, "text", "one", "two,three"] --&gt;
	 * "one,two\,three"</code>
	 * </p>
	 * <p>
	 * If the jCal value is a single array, then this array is treated as a
	 * "structured value", and converted its plain-text representation. Special
	 * characters (backslashes, commas, and semicolons) are escaped for each
	 * value in order to prevent commas and semicolons from being treated as
	 * delimiters.<br>
	 * <code>["x-foo", {}, "text", ["one", ["two", "three"], "four;five"]]
	 * --&gt; "one;two,three;four\;five"</code>
	 * </p>
	 * <p>
	 * If the jCal value starts with a JSON object, then the object is converted
	 * to a format identical to the one used in the RRULE and EXRULE properties.
	 * Special characters (backslashes, commas, semicolons, and equal signs) are
	 * escaped for each value in order to preserve the syntax of the string
	 * value.<br>
	 * <code>["x-foo", {}, "text", {"one": 1, "two": [2, 2.5]}] --&gt; "ONE=1;TWO=2,2.5"</code>
	 * </p>
	 * <p>
	 * For all other cases, behavior is undefined.
	 * </p>
	 * @param value the property's JSON value
	 * @param dataType the data type
	 * @param parameters the parsed parameters. These parameters will be
	 * assigned to the property object once this method returns. Therefore, do
	 * not assign any parameters to the property object itself whilst inside of
	 * this method, or else they will be overwritten.
	 * @param context the context
	 * @return the unmarshalled property object
	 * @throws CannotParseException if the scribe could not parse the property's
	 * value
	 * @throws SkipMeException if the property should not be added to the final
	 * {@link ICalendar} object
	 */
	protected T _parseJson(JCalValue value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
		String valueStr = jcalValueToString(value);
		return _parseText(valueStr, dataType, parameters, context);
	}

	/**
	 * Converts a jCal value to its plain-text format representation.
	 * @param value the jCal value
	 * @return the plain-text format representation (for example, "1,2,3" for a
	 * list of values)
	 */
	private static String jcalValueToString(JCalValue value) {
		List<JsonValue> values = value.getValues();
		if (values.size() > 1) {
			List<String> multi = value.asMulti();
			if (!multi.isEmpty()) {
				return VObjectPropertyValues.writeList(multi);
			}
		}

		if (!values.isEmpty() && values.get(0).getArray() != null) {
			List<List<String>> structured = value.asStructured();
			if (!structured.isEmpty()) {
				return VObjectPropertyValues.writeStructured(structured, true);
			}
		}

		if (values.get(0).getObject() != null) {
			ListMultimap<String, String> object = value.asObject();
			if (!object.isEmpty()) {
				return VObjectPropertyValues.writeMultimap(object.getMap());
			}
		}

		return VObjectPropertyValues.escape(value.asSingle());
	}

	/**
	 * Determines if the property is within an observance component.
	 * @param context the write context
	 * @return true if the property is within an observance, false if not
	 */
	protected static boolean isInObservance(WriteContext context) {
		return context.getParent() instanceof Observance;
	}

	/**
	 * Parses a date string.
	 * @param value the date string
	 * @return the factory object
	 */
	protected static DateParser date(String value) {
		return new DateParser(value);
	}

	/**
	 * Factory class for parsing dates.
	 */
	protected static class DateParser {
		private String value;
		private Boolean hasTime;

		/**
		 * Creates a new date writer object.
		 * @param value the date string to parse
		 */
		public DateParser(String value) {
			this.value = value;
		}

		/**
		 * Forces the value to be parsed as a date-time or date value.
		 * @param hasTime true to parsed as a date-time value, false to parse as
		 * a date value, null to parse as whatever value it is (defaults to
		 * null)
		 * @return this
		 */
		public DateParser hasTime(Boolean hasTime) {
			this.hasTime = hasTime;
			return this;
		}

		/**
		 * Parses the date string.
		 * @return the parsed date
		 * @throws IllegalArgumentException if the date string is invalid
		 */
		public ICalDate parse() {
			DateTimeComponents components = DateTimeComponents.parse(value, hasTime);
			Date date = components.toDate();
			boolean hasTime = components.hasTime();

			return new ICalDate(date, components, hasTime);
		}
	}

	/**
	 * Formats a date as a string.
	 * @param date the date
	 * @return the factory object
	 */
	protected static DateWriter date(Date date) {
		return date((date == null) ? null : new ICalDate(date));
	}

	/**
	 * Formats a date as a string.
	 * @param date the date
	 * @return the factory object
	 */
	protected static DateWriter date(ICalDate date) {
		return new DateWriter(date);
	}

	protected static DateWriter date(Date date, ICalProperty property, WriteContext context) {
		return date((date == null) ? null : new ICalDate(date), property, context);
	}

	protected static DateWriter date(ICalDate date, ICalProperty property, WriteContext context) {
		boolean floating;
		TimeZone tz;
		TimezoneAssignment globalTz = context.getGlobalTimezone();
		if (globalTz == null) {
			TimezoneInfo tzinfo = context.getTimezoneInfo();
			floating = tzinfo.isFloating(property);
			TimezoneAssignment assignment = tzinfo.getTimezoneToWriteIn(property);
			tz = (assignment == null) ? null : assignment.getTimeZone();
		} else {
			floating = false;
			tz = globalTz.getTimeZone();
		}

		context.addDate(date, floating, tz);
		return date(date).tz(floating, tz);
	}

	/**
	 * Factory class for writing dates.
	 */
	protected static class DateWriter {
		private ICalDate date;
		private TimeZone timezone;
		private boolean observance = false;
		private boolean extended = false;
		private boolean utc = false;

		/**
		 * Creates a new date writer object.
		 * @param date the date to format
		 */
		public DateWriter(ICalDate date) {
			this.date = date;
		}

		/**
		 * Sets whether the property is within an observance or not.
		 * @param observance true if it's in an observance, false if not
		 * (defaults to false)
		 * @return this
		 */
		public DateWriter observance(boolean observance) {
			this.observance = observance;
			return this;
		}

		/**
		 * Sets whether to write the value in UTC or not.
		 * @param utc true to write in UTC, false not to
		 * @return this
		 */
		public DateWriter utc(boolean utc) {
			this.utc = utc;
			return this;
		}

		/**
		 * Sets the timezone.
		 * @param floating true to use floating time, false not to
		 * @param timezone the timezone
		 * @return this
		 */
		public DateWriter tz(boolean floating, TimeZone timezone) {
			if (floating) {
				timezone = TimeZone.getDefault();
			}
			this.timezone = timezone;
			return this;
		}

		/**
		 * Sets whether to use extended format or basic.
		 * @param extended true to use extended format, false to use basic
		 * (defaults to "false")
		 * @return this
		 */
		public DateWriter extended(boolean extended) {
			this.extended = extended;
			return this;
		}

		/**
		 * Creates the date string.
		 * @return the date string
		 */
		public String write() {
			if (date == null) {
				return "";
			}

			if (observance) {
				DateTimeComponents components = date.getRawComponents();
				if (components == null) {
					ICalDateFormat format = extended ? ICalDateFormat.DATE_TIME_EXTENDED_WITHOUT_TZ : ICalDateFormat.DATE_TIME_BASIC_WITHOUT_TZ;
					return format.format(date);
				}

				return components.toString(true, extended);
			}

			if (utc) {
				ICalDateFormat format = extended ? ICalDateFormat.UTC_TIME_EXTENDED : ICalDateFormat.UTC_TIME_BASIC;
				return format.format(date);
			}

			ICalDateFormat format;
			TimeZone timezone = this.timezone;
			if (date.hasTime()) {
				if (timezone == null) {
					format = extended ? ICalDateFormat.UTC_TIME_EXTENDED : ICalDateFormat.UTC_TIME_BASIC;
				} else {
					format = extended ? ICalDateFormat.DATE_TIME_EXTENDED_WITHOUT_TZ : ICalDateFormat.DATE_TIME_BASIC_WITHOUT_TZ;
				}
			} else {
				format = extended ? ICalDateFormat.DATE_EXTENDED : ICalDateFormat.DATE_BASIC;
				timezone = null;
			}

			return format.format(date, timezone);
		}
	}

	/**
	 * Adds a TZID parameter to a property's parameter list if necessary.
	 * @param property the property
	 * @param hasTime true if the property value has a time component, false if
	 * not
	 * @param context the write context
	 * @return the property's new set of parameters
	 */
	protected static ICalParameters handleTzidParameter(ICalProperty property, boolean hasTime, WriteContext context) {
		ICalParameters parameters = property.getParameters();

		//date values don't have timezones
		if (!hasTime) {
			return parameters;
		}

		//vCal doesn't use the TZID parameter
		if (context.getVersion() == ICalVersion.V1_0) {
			return parameters;
		}

		//floating values don't have timezones
		TimezoneInfo tzinfo = context.getTimezoneInfo();
		boolean floating = tzinfo.isFloating(property);
		if (floating) {
			return parameters;
		}

		TimezoneAssignment tz;
		TimezoneAssignment globalTz = context.getGlobalTimezone();
		if (globalTz == null) {
			tz = tzinfo.getTimezoneToWriteIn(property);
			if (tz == null) {
				//write in UTC
				return parameters;
			}
		} else {
			tz = globalTz;
		}

		String tzid = null;
		VTimezone component = tz.getComponent();
		String globalId = tz.getGlobalId();
		if (component != null) {
			tzid = ValuedProperty.getValue(component.getTimezoneId());
		} else if (globalId != null) {
			tzid = '/' + globalId;
		}

		if (tzid == null) {
			//should never happen
			tzid = tz.getTimeZone().getID();
		}

		parameters = new ICalParameters(parameters);
		parameters.setTimezoneId(tzid);
		return parameters;
	}

	/**
	 * Creates a {@link CannotParseException}, indicating that the XML elements
	 * that the parser expected to find are missing from the property's XML
	 * element.
	 * @param dataTypes the expected data types (null for "unknown")
	 * @return the exception
	 */
	protected static CannotParseException missingXmlElements(ICalDataType... dataTypes) {
		String[] elements = new String[dataTypes.length];
		for (int i = 0; i < dataTypes.length; i++) {
			ICalDataType dataType = dataTypes[i];
			elements[i] = (dataType == null) ? "unknown" : dataType.getName().toLowerCase();
		}
		return missingXmlElements(elements);
	}

	/**
	 * Creates a {@link CannotParseException}, indicating that the XML elements
	 * that the parser expected to find are missing from property's XML element.
	 * @param elements the names of the expected XML elements
	 * @return the exception
	 */
	protected static CannotParseException missingXmlElements(String... elements) {
		return new CannotParseException(23, Arrays.toString(elements));
	}

	/**
	 * Represents the result of an unmarshal operation.
	 * @author Michael Angstadt
	 * @param <T> the unmarshalled property class
	 */
	public static class Result<T> {
		private final T property;
		private final List<ValidationWarning> warnings;

		/**
		 * Creates a new result.
		 * @param property the property object
		 * @param warnings the warnings
		 */
		public Result(T property, List<ValidationWarning> warnings) {
			this.property = property;
			this.warnings = warnings;
		}

		/**
		 * Gets the warnings.
		 * @return the warnings
		 */
		public List<ValidationWarning> getWarnings() {
			return warnings;
		}

		/**
		 * Gets the property object.
		 * @return the property object
		 */
		public T getProperty() {
			return property;
		}
	}
}
