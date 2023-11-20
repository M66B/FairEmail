package biweekly;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.TransformerException;

import biweekly.ValidationWarnings.WarningsGroup;
import biweekly.component.ICalComponent;
import biweekly.component.VEvent;
import biweekly.component.VFreeBusy;
import biweekly.component.VJournal;
import biweekly.component.VTodo;
import biweekly.io.TimezoneInfo;
import biweekly.io.json.JCalWriter;
import biweekly.io.text.ICalWriter;
import biweekly.io.xml.XCalDocument;
import biweekly.io.xml.XCalWriter;
import biweekly.property.CalendarScale;
import biweekly.property.Categories;
import biweekly.property.Color;
import biweekly.property.Description;
import biweekly.property.Geo;
import biweekly.property.ICalProperty;
import biweekly.property.Image;
import biweekly.property.LastModified;
import biweekly.property.Method;
import biweekly.property.Name;
import biweekly.property.ProductId;
import biweekly.property.RefreshInterval;
import biweekly.property.Source;
import biweekly.property.Uid;
import biweekly.property.Url;
import biweekly.util.Duration;

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
 * Represents an iCalendar object.
 * </p>
 * 
 * <p>
 * <b>Examples:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * ICalendar ical = new ICalendar();
 * 
 * VEvent event = new VEvent();
 * event.setSummary("Team Meeting");
 * Date start = ...;
 * event.setDateStart(start);
 * Date end = ...;
 * event.setDateEnd(end);
 * ical.addEvent(event);
 * </pre>
 * 
 * <p>
 * <b>Getting timezone information from parsed iCalendar objects:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * //The timezone information associated with an ICalendar object is stored in its TimezoneInfo object.
 * ICalReader reader = ...
 * ICalendar ical = reader.readNext();
 * TimezoneInfo tzinfo = ical.getTimezoneInfo();
 * 
 * //You can use this object to get the VTIMEZONE components that were parsed from the input stream.
 * //Note that the VTIMEZONE components will NOT be in the ICalendar object itself
 * Collection&lt;VTimezone&gt; vtimezones = tzinfo.getComponents();
 * 
 * //You can also get the timezone that a specific property was originally formatted in.
 * DateStart dtstart = ical.getEvents().get(0).getDateStart();
 * TimeZone tz = tzinfo.getTimezone(dtstart).getTimeZone();
 * 
 * //This is useful for calculating recurrence rule dates.
 * RecurrenceRule rrule = ical.getEvents(0).getRecurrenceRule();
 * DateIterator it = rrule.getDateIterator(dtstart.getValue(), tz);
 * </pre>
 * 
 * <p>
 * <b>Setting timezone information when writing iCalendar objects:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * //The TimezoneInfo field is used to determine what timezone to format each date-time value in when the ICalendar object is written.
 * //Appropriate VTIMEZONE components are automatically added to the written iCalendar object.
 * ICalendar ical = ...
 * TimezoneInfo tzinfo = ical.getTimezoneInfo();
 * 
 * //biweekly uses the TimezoneAssignment class to define timezones.
 * //This class groups together a Java TimeZone object, which is used to format/parse the date-time values, and its equivalent VTIMEZONE component definition.
 * 
 * //biweekly can auto-generate the VTIMEZONE definitions by downloading them from tzurl.org.
 * //If you want the generated VTIMEZONE components to be tailored for Microsoft Outlook email clients, pass "true" into this method.
 * TimezoneAssignment timezone = TimezoneAssignment.download(TimeZone.getTimeZone("America/New_York"), true);
 * 
 * //Using the TimezoneAssignment class, you can specify what timezone you'd like to format all date-time values in.
 * tzinfo.setDefaultTimezone(timezone);
 * 
 * //You can also specify what timezone to use for individual properties if you want.
 * DateStart dtstart = ical.getEvents(0).getDateStart();
 * TimezoneAssignment losAngeles = TimezoneAssignment.download(TimeZone.getTimeZone("America/Los_Angeles"), true);
 * tzinfo.setTimezone(dtstart, losAngeles);
 * 
 * //The writer object will use this information to determine what timezone to format each date-time value in.
 * //Date-time values are formatted in UTC by default.
 * ICalWriter writer = ...
 * writer.write(ical);
 * </pre>
 * 
 * <p>
 * For more information on working with timezones, see this page: <a
 * href="https://github.com/mangstadt/biweekly/wiki/Timezones">https://github.
 * com/mangstadt/biweekly/wiki/Timezones</a>
 * </p>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5545">RFC 5545</a>
 * @see <a href="http://tools.ietf.org/html/rfc2445">RFC 2445</a>
 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0</a>
 * @see <a
 * href="http://tools.ietf.org/html/draft-ietf-calext-extensions-01">draft-ietf-calext-extensions-01</a>
 */
public class ICalendar extends ICalComponent {
	private ICalVersion version;
	private TimezoneInfo tzinfo = new TimezoneInfo();

	/**
	 * <p>
	 * Creates a new iCalendar object.
	 * </p>
	 * <p>
	 * The following properties are added to the component when it is created:
	 * </p>
	 * <ul>
	 * <li>{@link ProductId}: Set to a value that represents this library.</li>
	 * </ul>
	 */
	public ICalendar() {
		setProductId(ProductId.biweekly());
	}

	/**
	 * Copy constructor.
	 * @param original the iCalendar object to make a copy of
	 */
	public ICalendar(ICalendar original) {
		super(original);
		version = original.version;
	}

	/**
	 * Gets the version of this iCalendar object.
	 * @return the version
	 */
	public ICalVersion getVersion() {
		return version;
	}

	/**
	 * Sets the version of this iCalendar object.
	 * @param version the version
	 */
	public void setVersion(ICalVersion version) {
		this.version = version;
	}

	/**
	 * <p>
	 * Gets the timezone information associated with this iCalendar object.
	 * </p>
	 * <p>
	 * When an iCalendar object is parsed from an input stream, the
	 * {@link TimezoneInfo} object remembers the original timezone definitions
	 * that each property was associated with. One use for this is when you want
	 * to calculate the dates in a recurrence rule. The recurrence rule needs to
	 * know what timezone its associated date values were originally formatted
	 * in in order to work correctly.
	 * </p>
	 * <p>
	 * When an {@link ICalendar} object is written to an output stream, its
	 * {@link TimezoneInfo} object tells the writer what timezone to format each
	 * property in.
	 * </p>
	 * @return the timezone info
	 */
	public TimezoneInfo getTimezoneInfo() {
		return tzinfo;
	}

	/**
	 * <p>
	 * Sets the timezone information associated with this iCalendar object.
	 * </p>
	 * <p>
	 * When an iCalendar object is parsed from an input stream, the
	 * {@link TimezoneInfo} object remembers the original timezone definitions
	 * that each property was associated with. One use for this is when you want
	 * to calculate the dates in a recurrence rule. The recurrence rule needs to
	 * know what timezone its associated date values were originally formatted
	 * in in order to work correctly.
	 * </p>
	 * <p>
	 * When an {@link ICalendar} object is written to an output stream, its
	 * {@link TimezoneInfo} object tells the writer what timezone to format each
	 * property in.
	 * </p>
	 * @param tzinfo the timezone info (cannot be null)
	 * @throws NullPointerException if the timezone info object is null
	 */
	public void setTimezoneInfo(TimezoneInfo tzinfo) {
		if (tzinfo == null) {
			throw new NullPointerException();
		}
		this.tzinfo = tzinfo;
	}

	/**
	 * Gets the name of the application that created the iCalendar object. All
	 * {@link ICalendar} objects are initialized with a product ID representing
	 * this library.
	 * @return the property instance or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-78">RFC 5545
	 * p.78-9</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-75">RFC 2445
	 * p.75-6</a>
	 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.24</a>
	 */
	public ProductId getProductId() {
		return getProperty(ProductId.class);
	}

	/**
	 * Sets the name of the application that created the iCalendar object. All
	 * {@link ICalendar} objects are initialized with a product ID representing
	 * this library.
	 * @param prodId the property instance or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-78">RFC 5545
	 * p.78-9</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-75">RFC 2445
	 * p.75-6</a>
	 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.24</a>
	 */
	public void setProductId(ProductId prodId) {
		setProperty(ProductId.class, prodId);
	}

	/**
	 * Sets the application that created the iCalendar object. All
	 * {@link ICalendar} objects are initialized with a product ID representing
	 * this library.
	 * @param prodId a unique string representing the application (e.g.
	 * "-//Company//Application//EN") or null to remove
	 * @return the property that was created
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-78">RFC 5545
	 * p.78-9</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-75">RFC 2445
	 * p.75-6</a>
	 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.24</a>
	 */
	public ProductId setProductId(String prodId) {
		ProductId property = (prodId == null) ? null : new ProductId(prodId);
		setProductId(property);
		return property;
	}

	/**
	 * Gets the calendar system that this iCalendar object uses. If none is
	 * specified, then the calendar is assumed to be in Gregorian format.
	 * @return the calendar system or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-76">RFC 5545
	 * p.76-7</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-73">RFC 2445
	 * p.73-4</a>
	 */
	public CalendarScale getCalendarScale() {
		return getProperty(CalendarScale.class);
	}

	/**
	 * Sets the calendar system that this iCalendar object uses. If none is
	 * specified, then the calendar is assumed to be in Gregorian format.
	 * @param calendarScale the calendar system or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-76">RFC 5545
	 * p.76-7</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-73">RFC 2445
	 * p.73-4</a>
	 */
	public void setCalendarScale(CalendarScale calendarScale) {
		setProperty(CalendarScale.class, calendarScale);
	}

	/**
	 * Gets the type of <a href="http://tools.ietf.org/html/rfc5546">iTIP</a>
	 * request that this iCalendar object represents, or the value of the
	 * "Content-Type" header's "method" parameter if the iCalendar object is
	 * defined as a MIME message entity.
	 * @return the property or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5546">RFC 5546</a>
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-77">RFC 5545
	 * p.77-8</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-74">RFC 2445
	 * p.74-5</a>
	 */
	public Method getMethod() {
		return getProperty(Method.class);
	}

	/**
	 * Sets the type of <a href="http://tools.ietf.org/html/rfc5546">iTIP</a>
	 * request that this iCalendar object represents, or the value of the
	 * "Content-Type" header's "method" parameter if the iCalendar object is
	 * defined as a MIME message entity.
	 * @param method the property or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5546">RFC 5546</a>
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-77">RFC 5545
	 * p.77-8</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-74">RFC 2445
	 * p.74-5</a>
	 */
	public void setMethod(Method method) {
		setProperty(Method.class, method);
	}

	/**
	 * Sets the type of <a href="http://tools.ietf.org/html/rfc5546">iTIP</a>
	 * request that this iCalendar object represents, or the value of the
	 * "Content-Type" header's "method" parameter if the iCalendar object is
	 * defined as a MIME message entity.
	 * @param method the method or null to remove
	 * @return the property that was created
	 * @see <a href="http://tools.ietf.org/html/rfc5546">RFC 5546</a>
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-77">RFC 5545
	 * p.77-8</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-74">RFC 2445
	 * p.74-5</a>
	 */
	public Method setMethod(String method) {
		Method property = (method == null) ? null : new Method(method);
		setMethod(property);
		return property;
	}

	/**
	 * <p>
	 * Gets the human-readable name of the calendar as a whole.
	 * </p>
	 * <p>
	 * An iCalendar object can only have one name, but multiple {@link Name}
	 * properties can exist in order to specify the name in multiple languages.
	 * In this case, each property instance must be assigned a LANGUAGE
	 * parameter.
	 * </p>
	 * @return the names (any changes made this list will affect the parent
	 * component object and vice versa)
	 * @see <a
	 * href="http://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-5">draft-ietf-calext-extensions-01
	 * p.5</a>
	 */
	public List<Name> getNames() {
		return getProperties(Name.class);
	}

	/**
	 * Sets the human-readable name of the calendar as a whole.
	 * @param name the name or null to remove
	 * @see <a
	 * href="http://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-5">draft-ietf-calext-extensions-01
	 * p.5</a>
	 */
	public void setName(Name name) {
		setProperty(Name.class, name);
	}

	/**
	 * Sets the human-readable name of the calendar as a whole.
	 * @param name the name or null to remove
	 * @return the property that was created
	 * @see <a
	 * href="http://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-5">draft-ietf-calext-extensions-01
	 * p.5</a>
	 */
	public Name setName(String name) {
		Name property = (name == null) ? null : new Name(name);
		setName(property);
		return property;
	}

	/**
	 * <p>
	 * Assigns a human-readable name to the calendar as a whole.
	 * </p>
	 * <p>
	 * An iCalendar object can only have one name, but multiple {@link Name}
	 * properties can exist in order to specify the name in multiple languages.
	 * In this case, each property instance must be assigned a LANGUAGE
	 * parameter.
	 * </p>
	 * @param name the name
	 * @see <a
	 * href="http://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-5">draft-ietf-calext-extensions-01
	 * p.5</a>
	 */
	public void addName(Name name) {
		addProperty(name);
	}

	/**
	 * <p>
	 * Assigns a human-readable name to the calendar as a whole.
	 * </p>
	 * <p>
	 * An iCalendar object can only have one name, but multiple {@link Name}
	 * properties can exist in order to specify the name in multiple languages.
	 * In this case, each property instance must be assigned a LANGUAGE
	 * parameter.
	 * </p>
	 * @param name the name (e.g. "Company Vacation Days")
	 * @return the property object that was created
	 * @see <a
	 * href="http://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-5">draft-ietf-calext-extensions-01
	 * p.5</a>
	 */
	public Name addName(String name) {
		Name property = new Name(name);
		addProperty(property);
		return property;
	}

	/**
	 * <p>
	 * Gets the human-readable description of the calendar as a whole.
	 * </p>
	 * <p>
	 * An iCalendar object can only have one description, but multiple
	 * {@link Description} properties can exist in order to specify the
	 * description in multiple languages. In this case, each property instance
	 * must be assigned a LANGUAGE parameter.
	 * </p>
	 * @return the descriptions (any changes made this list will affect the
	 * parent component object and vice versa)
	 * @see <a
	 * href="http://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-6">draft-ietf-calext-extensions-01
	 * p.6</a>
	 */
	public List<Description> getDescriptions() {
		return getProperties(Description.class);
	}

	/**
	 * Sets the human-readable description of the calendar as a whole.
	 * @param description the description or null to remove
	 * @see <a
	 * href="http://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-6">draft-ietf-calext-extensions-01
	 * p.6</a>
	 */
	public void setDescription(Description description) {
		setProperty(Description.class, description);
	}

	/**
	 * Sets the human-readable description of the calendar as a whole.
	 * @param description the description or null to remove
	 * @return the property that was created
	 * @see <a
	 * href="http://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-6">draft-ietf-calext-extensions-01
	 * p.6</a>
	 */
	public Description setDescription(String description) {
		Description property = (description == null) ? null : new Description(description);
		setDescription(property);
		return property;
	}

	/**
	 * <p>
	 * Assigns a human-readable description to the calendar as a whole.
	 * </p>
	 * <p>
	 * An iCalendar object can only have one description, but multiple
	 * {@link Description} properties can exist in order to specify the
	 * description in multiple languages. In this case, each property instance
	 * must be assigned a LANGUAGE parameter.
	 * </p>
	 * @param description the description
	 * @see <a
	 * href="http://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-6">draft-ietf-calext-extensions-01
	 * p.6</a>
	 */
	public void addDescription(Description description) {
		addProperty(description);
	}

	/**
	 * <p>
	 * Assigns a human-readable description to the calendar as a whole.
	 * </p>
	 * <p>
	 * An iCalendar object can only have one description, but multiple
	 * {@link Description} properties can exist in order to specify the
	 * description in multiple languages. In this case, each property instance
	 * must be assigned a LANGUAGE parameter.
	 * </p>
	 * @param description the description
	 * @return the property object that was created
	 * @see <a
	 * href="http://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-6">draft-ietf-calext-extensions-01
	 * p.6</a>
	 */
	public Description addDescription(String description) {
		Description property = new Description(description);
		addProperty(property);
		return property;
	}

	/**
	 * Gets the calendar's unique identifier.
	 * @return the unique identifier or null if not set
	 * @see <a
	 * href="http://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-6">draft-ietf-calext-extensions-01
	 * p.6</a>
	 */
	public Uid getUid() {
		return getProperty(Uid.class);
	}

	/**
	 * Sets the calendar's unique identifier.
	 * @param uid the unique identifier or null to remove
	 * @see <a
	 * href="http://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-6">draft-ietf-calext-extensions-01
	 * p.6</a>
	 */
	public void setUid(Uid uid) {
		setProperty(Uid.class, uid);
	}

	/**
	 * Sets the calendar's unique identifier.
	 * @param uid the unique identifier or null to remove
	 * @return the property object that was created
	 * @see <a
	 * href="http://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-6">draft-ietf-calext-extensions-01
	 * p.6</a>
	 */
	public Uid setUid(String uid) {
		Uid property = (uid == null) ? null : new Uid(uid);
		setUid(property);
		return property;
	}

	/**
	 * Gets the date and time that the information in this calendar object was
	 * last revised.
	 * @return the last modified time or null if not set
	 * @see <a
	 * href="http://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-7">draft-ietf-calext-extensions-01
	 * p.7</a>
	 */
	public LastModified getLastModified() {
		return getProperty(LastModified.class);
	}

	/**
	 * Sets the date and time that the information in this calendar object was
	 * last revised.
	 * @param lastModified the last modified time or null to remove
	 * @see <a
	 * href="http://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-7">draft-ietf-calext-extensions-01
	 * p.7</a>
	 */
	public void setLastModified(LastModified lastModified) {
		setProperty(LastModified.class, lastModified);
	}

	/**
	 * Sets the date and time that the information in this calendar object was
	 * last revised.
	 * @param lastModified the date and time or null to remove
	 * @return the property object that was created
	 * @see <a
	 * href="http://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-7">draft-ietf-calext-extensions-01
	 * p.7</a>
	 */
	public LastModified setLastModified(Date lastModified) {
		LastModified property = (lastModified == null) ? null : new LastModified(lastModified);
		setLastModified(property);
		return property;
	}

	/**
	 * Gets the location of a more dynamic, alternate representation of the
	 * calendar (such as a website that allows you to interact with the calendar
	 * data).
	 * @return the URL or null if not set
	 * @see <a
	 * href="http://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-7">draft-ietf-calext-extensions-01
	 * p.7</a>
	 */
	public Url getUrl() {
		return getProperty(Url.class);
	}

	/**
	 * Sets the location of a more dynamic, alternate representation of the
	 * calendar (such as a website that allows you to interact with the calendar
	 * data).
	 * @param url the URL or null to remove
	 * @see <a
	 * href="http://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-7">draft-ietf-calext-extensions-01
	 * p.7</a>
	 */
	public void setUrl(Url url) {
		setProperty(Url.class, url);
	}

	/**
	 * Sets the location of a more dynamic, alternate representation of the
	 * calendar (such as a website that allows you to interact with the calendar
	 * data).
	 * @param url the URL or null to remove
	 * @return the property object that was created
	 * @see <a
	 * href="http://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-7">draft-ietf-calext-extensions-01
	 * p.7</a>
	 */
	public Url setUrl(String url) {
		Url property = (url == null) ? null : new Url(url);
		setUrl(property);
		return property;
	}

	/**
	 * Gets the keywords that describe the calendar.
	 * @return the categories (any changes made this list will affect the parent
	 * component object and vice versa)
	 * @see <a
	 * href="http://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-7">draft-ietf-calext-extensions-01
	 * p.7</a>
	 */
	public List<Categories> getCategories() {
		return getProperties(Categories.class);
	}

	/**
	 * Adds a list of keywords that describe the calendar.
	 * @param categories the categories to add
	 * @see <a
	 * href="http://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-7">draft-ietf-calext-extensions-01
	 * p.7</a>
	 */
	public void addCategories(Categories categories) {
		addProperty(categories);
	}

	/**
	 * Adds a list of keywords that describe the calendar.
	 * @param categories the categories to add
	 * @return the property object that was created
	 * @see <a
	 * href="http://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-7">draft-ietf-calext-extensions-01
	 * p.7</a>
	 */
	public Categories addCategories(String... categories) {
		Categories prop = new Categories(categories);
		addProperty(prop);
		return prop;
	}

	/**
	 * Gets the suggested minimum polling interval for checking for updates to
	 * the calendar data.
	 * @return the refresh interval or null if not set
	 * @see <a
	 * href="http://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-7">draft-ietf-calext-extensions-01
	 * p.7</a>
	 */
	public RefreshInterval getRefreshInterval() {
		return getProperty(RefreshInterval.class);
	}

	/**
	 * Sets the suggested minimum polling interval for checking for updates to
	 * the calendar data.
	 * @param refreshInterval the refresh interval or null to remove
	 * @see <a
	 * href="http://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-7">draft-ietf-calext-extensions-01
	 * p.7</a>
	 */
	public void setRefreshInterval(RefreshInterval refreshInterval) {
		setProperty(RefreshInterval.class, refreshInterval);
	}

	/**
	 * Sets the suggested minimum polling interval for checking for updates to
	 * the calendar data.
	 * @param refreshInterval the refresh interval or null to remove
	 * @return the property object that was created
	 * @see <a
	 * href="http://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-7">draft-ietf-calext-extensions-01
	 * p.7</a>
	 */
	public RefreshInterval setRefreshInterval(Duration refreshInterval) {
		RefreshInterval property = (refreshInterval == null) ? null : new RefreshInterval(refreshInterval);
		setRefreshInterval(property);
		return property;
	}

	/**
	 * Gets the location that the calendar data can be refreshed from.
	 * @return the source or null if not set
	 * @see <a
	 * href="http://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-8">draft-ietf-calext-extensions-01
	 * p.8</a>
	 */
	public Source getSource() {
		return getProperty(Source.class);
	}

	/**
	 * Sets the location that the calendar data can be refreshed from.
	 * @param source the source or null to remove
	 * @see <a
	 * href="http://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-8">draft-ietf-calext-extensions-01
	 * p.8</a>
	 */
	public void setSource(Source source) {
		setProperty(Source.class, source);
	}

	/**
	 * Sets the location that the calendar data can be refreshed from.
	 * @param url the source or null to remove
	 * @return the property object that was created
	 * @see <a
	 * href="http://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-8">draft-ietf-calext-extensions-01
	 * p.8</a>
	 */
	public Source setSource(String url) {
		Source property = (url == null) ? null : new Source(url);
		setSource(property);
		return property;
	}

	/**
	 * Gets the color that clients may use when displaying the calendar (for
	 * example, a background color).
	 * @return the color or null if not set
	 * @see <a
	 * href="http://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-9">draft-ietf-calext-extensions-01
	 * p.9</a>
	 */
	public Color getColor() {
		return getProperty(Color.class);
	}

	/**
	 * Sets the color that clients may use when displaying the calendar (for
	 * example, a background color).
	 * @param color the color or null to remove
	 * @see <a
	 * href="http://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-9">draft-ietf-calext-extensions-01
	 * p.9</a>
	 */
	public void setColor(Color color) {
		setProperty(Color.class, color);
	}

	/**
	 * Sets the color that clients may use when displaying the calendar (for
	 * example, a background color).
	 * @param color the color name (case insensitive) or null to remove.
	 * Acceptable values are defined in <a
	 * href="https://www.w3.org/TR/2011/REC-css3-color-20110607/#svg-color"
	 * >Section 4.3 of the CSS Color Module Level 3 Recommendation</a>. For
	 * example, "aliceblue", "green", "navy".
	 * @return the property object that was created
	 * @see <a
	 * href="http://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-9">draft-ietf-calext-extensions-01
	 * p.9</a>
	 */
	public Color setColor(String color) {
		Color property = (color == null) ? null : new Color(color);
		setColor(property);
		return property;
	}

	/**
	 * Gets the images that are associated with the calendar.
	 * @return the images (any changes made this list will affect the parent
	 * component object and vice versa)
	 * @see <a
	 * href="http://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-10">draft-ietf-calext-extensions-01
	 * p.10</a>
	 */
	public List<Image> getImages() {
		return getProperties(Image.class);
	}

	/**
	 * Adds an image that is associated with the calendar.
	 * @param image the image
	 * @see <a
	 * href="http://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-10">draft-ietf-calext-extensions-01
	 * p.10</a>
	 */
	public void addImage(Image image) {
		addProperty(image);
	}

	/**
	 * Gets the calendar's events.
	 * @return the events (any changes made this list will affect the parent
	 * component object and vice versa)
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-52">RFC 5545
	 * p.52-5</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-52">RFC 2445
	 * p.52-4</a>
	 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.13</a>
	 */
	public List<VEvent> getEvents() {
		return getComponents(VEvent.class);
	}

	/**
	 * Adds an event to the calendar.
	 * @param event the event
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-52">RFC 5545
	 * p.52-5</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-52">RFC 2445
	 * p.52-4</a>
	 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.13</a>
	 */
	public void addEvent(VEvent event) {
		addComponent(event);
	}

	/**
	 * Gets the calendar's to-do tasks.
	 * @return the to-do tasks (any changes made this list will affect the
	 * parent component object and vice versa)
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-55">RFC 5545
	 * p.55-7</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-55">RFC 2445
	 * p.55-6</a>
	 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.14</a>
	 */
	public List<VTodo> getTodos() {
		return getComponents(VTodo.class);
	}

	/**
	 * Adds a to-do task to the calendar.
	 * @param todo the to-do task
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-55">RFC 5545
	 * p.55-7</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-55">RFC 2445
	 * p.55-6</a>
	 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.14</a>
	 */
	public void addTodo(VTodo todo) {
		addComponent(todo);
	}

	/**
	 * Gets the calendar's journal entries.
	 * @return the journal entries (any changes made this list will affect the
	 * parent component object and vice versa)
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-55">RFC 5545
	 * p.57-9</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-56">RFC 2445
	 * p.56-7</a>
	 */
	public List<VJournal> getJournals() {
		return getComponents(VJournal.class);
	}

	/**
	 * Adds a journal entry to the calendar.
	 * @param journal the journal entry
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-55">RFC 5545
	 * p.57-9</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-56">RFC 2445
	 * p.56-7</a>
	 */
	public void addJournal(VJournal journal) {
		addComponent(journal);
	}

	/**
	 * Gets the calendar's free/busy entries.
	 * @return the free/busy entries (any changes made this list will affect the
	 * parent component object and vice versa)
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-59">RFC 5545
	 * p.59-62</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-58">RFC 2445
	 * p.58-60</a>
	 */
	public List<VFreeBusy> getFreeBusies() {
		return getComponents(VFreeBusy.class);
	}

	/**
	 * Adds a free/busy entry to the calendar.
	 * @param freeBusy the free/busy entry
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-59">RFC 5545
	 * p.59-62</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-58">RFC 2445
	 * p.58-60</a>
	 */
	public void addFreeBusy(VFreeBusy freeBusy) {
		addComponent(freeBusy);
	}

	/**
	 * <p>
	 * Checks this iCalendar object for data consistency problems or deviations
	 * from the specifications.
	 * </p>
	 * <p>
	 * The existence of validation warnings will not prevent the iCalendar
	 * object from being written to a data stream. Syntactically-correct output
	 * will still be produced. However, the consuming application may have
	 * trouble interpreting some of the data due to the presence of these
	 * warnings.
	 * </p>
	 * <p>
	 * These problems can largely be avoided by reading the Javadocs of the
	 * component and property classes, or by being familiar with the iCalendar
	 * standard.
	 * </p>
	 * @param version the version to validate against
	 * @return the validation warnings
	 */
	public ValidationWarnings validate(ICalVersion version) {
		List<WarningsGroup> warnings = validate(new ArrayList<ICalComponent>(0), version);
		return new ValidationWarnings(warnings);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void validate(List<ICalComponent> components, ICalVersion version, List<ValidationWarning> warnings) {
		if (version != ICalVersion.V1_0) {
			checkRequiredCardinality(warnings, ProductId.class);

			if (this.components.isEmpty()) {
				warnings.add(new ValidationWarning(4));
			}

			if (getProperty(Geo.class) != null) {
				warnings.add(new ValidationWarning(44));
			}
		}

		checkOptionalCardinality(warnings, Uid.class, LastModified.class, Url.class, RefreshInterval.class, Color.class, Source.class);
		checkUniqueLanguages(warnings, Name.class);
		checkUniqueLanguages(warnings, Description.class);
	}

	private void checkUniqueLanguages(List<ValidationWarning> warnings, Class<? extends ICalProperty> clazz) {
		List<? extends ICalProperty> properties = getProperties(clazz);
		if (properties.size() <= 1) {
			return;
		}

		Set<String> languages = new HashSet<String>(properties.size());
		for (ICalProperty property : properties) {
			String language = property.getParameters().getLanguage();
			if (language != null) {
				language = language.toLowerCase();
			}

			boolean added = languages.add(language);
			if (!added) {
				warnings.add(new ValidationWarning(55, clazz.getSimpleName()));
				break;
			}
		}
	}

	/**
	 * <p>
	 * Marshals this iCalendar object to its traditional, plain-text
	 * representation.
	 * </p>
	 * <p>
	 * If this iCalendar object contains user-defined property or component
	 * objects, you must use the {@link Biweekly} or {@link ICalWriter} classes
	 * instead in order to register the scribe classes.
	 * </p>
	 * @return the plain text representation
	 * @throws IllegalArgumentException if this iCalendar object contains
	 * user-defined property or component objects
	 */
	public String write() {
		ICalVersion version = (this.version == null) ? ICalVersion.V2_0 : this.version;
		return Biweekly.write(this).version(version).go();
	}

	/**
	 * <p>
	 * Marshals this iCalendar object to its traditional, plain-text
	 * representation.
	 * </p>
	 * <p>
	 * If this iCalendar object contains user-defined property or component
	 * objects, you must use the {@link Biweekly} or {@link ICalWriter} classes
	 * instead in order to register the scribe classes.
	 * </p>
	 * @param file the file to write to
	 * @throws IllegalArgumentException if this iCalendar object contains
	 * user-defined property or component objects
	 * @throws IOException if there's an problem writing to the file
	 */
	public void write(File file) throws IOException {
		ICalVersion version = (this.version == null) ? ICalVersion.V2_0 : this.version;
		Biweekly.write(this).version(version).go(file);
	}

	/**
	 * <p>
	 * Marshals this iCalendar object to its traditional, plain-text
	 * representation.
	 * </p>
	 * <p>
	 * If this iCalendar object contains user-defined property or component
	 * objects, you must use the {@link Biweekly} or {@link ICalWriter} classes
	 * instead in order to register the scribe classes.
	 * </p>
	 * @param out the output stream to write to
	 * @throws IllegalArgumentException if this iCalendar object contains
	 * user-defined property or component objects
	 * @throws IOException if there's a problem writing to the output stream
	 */
	public void write(OutputStream out) throws IOException {
		ICalVersion version = (this.version == null) ? ICalVersion.V2_0 : this.version;
		Biweekly.write(this).version(version).go(out);
	}

	/**
	 * <p>
	 * Marshals this iCalendar object to its traditional, plain-text
	 * representation.
	 * </p>
	 * <p>
	 * If this iCalendar object contains user-defined property or component
	 * objects, you must use the {@link Biweekly} or {@link ICalWriter} classes
	 * instead in order to register the scribe classes.
	 * </p>
	 * @param writer the writer to write to
	 * @throws IllegalArgumentException if this iCalendar object contains
	 * user-defined property or component objects
	 * @throws IOException if there's a problem writing to the writer
	 */
	public void write(Writer writer) throws IOException {
		ICalVersion version = (this.version == null) ? ICalVersion.V2_0 : this.version;
		Biweekly.write(this).version(version).go(writer);
	}

	/**
	 * <p>
	 * Marshals this iCalendar object to its XML representation (xCal).
	 * </p>
	 * <p>
	 * If this iCalendar object contains user-defined property or component
	 * objects, you must use the {@link Biweekly}, {@link XCalWriter}, or
	 * {@link XCalDocument} classes instead in order to register the scribe
	 * classes.
	 * </p>
	 * @return the XML document
	 * @throws IllegalArgumentException if this iCalendar object contains
	 * user-defined property or component objects
	 */
	public String writeXml() {
		return Biweekly.writeXml(this).indent(2).go();
	}

	/**
	 * <p>
	 * Marshals this iCalendar object to its XML representation (xCal).
	 * </p>
	 * <p>
	 * If this iCalendar object contains user-defined property or component
	 * objects, you must use the {@link Biweekly}, {@link XCalWriter}, or
	 * {@link XCalDocument} classes instead in order to register the scribe
	 * classes.
	 * </p>
	 * @param file the file to write to
	 * @throws IllegalArgumentException if this iCalendar object contains
	 * user-defined property or component objects
	 * @throws TransformerException if there's a problem writing to the file
	 * @throws IOException if there's a problem opening the file
	 */
	public void writeXml(File file) throws TransformerException, IOException {
		Biweekly.writeXml(this).indent(2).go(file);
	}

	/**
	 * <p>
	 * Marshals this iCalendar object to its XML representation (xCal).
	 * </p>
	 * <p>
	 * If this iCalendar object contains user-defined property or component
	 * objects, you must use the {@link Biweekly}, {@link XCalWriter}, or
	 * {@link XCalDocument} classes instead in order to register the scribe
	 * classes.
	 * </p>
	 * @param out the output stream to write to
	 * @throws IllegalArgumentException if this iCalendar object contains
	 * user-defined property or component objects
	 * @throws TransformerException if there's a problem writing to the output
	 * stream
	 */
	public void writeXml(OutputStream out) throws TransformerException {
		Biweekly.writeXml(this).indent(2).go(out);
	}

	/**
	 * <p>
	 * Marshals this iCalendar object to its XML representation (xCal).
	 * </p>
	 * <p>
	 * If this iCalendar object contains user-defined property or component
	 * objects, you must use the {@link Biweekly}, {@link XCalWriter}, or
	 * {@link XCalDocument} classes instead in order to register the scribe
	 * classes.
	 * </p>
	 * @param writer the writer to write to
	 * @throws IllegalArgumentException if this iCalendar object contains
	 * user-defined property or component objects
	 * @throws TransformerException if there's a problem writing to the writer
	 */
	public void writeXml(Writer writer) throws TransformerException {
		Biweekly.writeXml(this).indent(2).go(writer);
	}

	/**
	 * <p>
	 * Marshals this iCalendar object to its JSON representation (jCal).
	 * </p>
	 * <p>
	 * If this iCalendar object contains user-defined property or component
	 * objects, you must use the {@link Biweekly} or {@link JCalWriter} classes
	 * instead in order to register the scribe classes.
	 * </p>
	 * @return the JSON string
	 * @throws IllegalArgumentException if this iCalendar object contains
	 * user-defined property or component objects
	 */
	public String writeJson() {
		return Biweekly.writeJson(this).go();
	}

	/**
	 * <p>
	 * Marshals this iCalendar object to its JSON representation (jCal).
	 * </p>
	 * <p>
	 * If this iCalendar object contains user-defined property or component
	 * objects, you must use the {@link Biweekly} or {@link JCalWriter} classes
	 * instead in order to register the scribe classes.
	 * </p>
	 * @param file the file to write to
	 * @throws IllegalArgumentException if this iCalendar object contains
	 * user-defined property or component objects
	 * @throws IOException if there's a problem writing to the file
	 */
	public void writeJson(File file) throws IOException {
		Biweekly.writeJson(this).go(file);
	}

	/**
	 * <p>
	 * Marshals this iCalendar object to its JSON representation (jCal).
	 * </p>
	 * <p>
	 * If this iCalendar object contains user-defined property or component
	 * objects, you must use the {@link Biweekly} or {@link JCalWriter} classes
	 * instead in order to register the scribe classes.
	 * </p>
	 * @param out the output stream to write to
	 * @throws IllegalArgumentException if this iCalendar object contains
	 * user-defined property or component objects
	 * @throws IOException if there's a problem writing to the output stream
	 */
	public void writeJson(OutputStream out) throws IOException {
		Biweekly.writeJson(this).go(out);
	}

	/**
	 * <p>
	 * Marshals this iCalendar object to its JSON representation (jCal).
	 * </p>
	 * <p>
	 * If this iCalendar object contains user-defined property or component
	 * objects, you must use the {@link Biweekly} or {@link JCalWriter} classes
	 * instead in order to register the scribe classes.
	 * </p>
	 * @param writer the writer to write to
	 * @throws IllegalArgumentException if this iCalendar object contains
	 * user-defined property or component objects
	 * @throws IOException if there's a problem writing to the writer
	 */
	public void writeJson(Writer writer) throws IOException {
		Biweekly.writeJson(this).go(writer);
	}

	@Override
	protected Map<String, Object> toStringValues() {
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put("version", version);
		return fields;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj)) return false;
		ICalendar other = (ICalendar) obj;
		if (version != other.version) return false;
		return true;
	}
}
