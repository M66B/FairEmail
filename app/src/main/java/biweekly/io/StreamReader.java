package biweekly.io;

import static biweekly.io.DataModelConverter.convert;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import biweekly.ICalendar;
import biweekly.component.ICalComponent;
import biweekly.component.VTimezone;
import biweekly.io.ParseContext.TimezonedDate;
import biweekly.io.scribe.ScribeIndex;
import biweekly.io.scribe.component.ICalComponentScribe;
import biweekly.io.scribe.property.ICalPropertyScribe;
import biweekly.property.Daylight;
import biweekly.property.ICalProperty;
import biweekly.property.Timezone;
import biweekly.property.ValuedProperty;
import biweekly.util.ICalDate;

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
 * Parses iCalendar objects from a data stream.
 * @author Michael Angstadt
 */
public abstract class StreamReader implements Closeable {
	protected final List<ParseWarning> warnings = new ArrayList<ParseWarning>();
	protected ScribeIndex index = new ScribeIndex();
	protected ParseContext context;
	private TimeZone defaultTimezone = TimeZone.getDefault();
	private GlobalTimezoneIdResolver globalTimezoneIdResolver = new DefaultGlobalTimezoneIdResolver();

	/**
	 * <p>
	 * Registers an experimental property scribe. Can also be used to override
	 * the scribe of a standard property (such as DTSTART). Calling this method
	 * is the same as calling:
	 * </p>
	 * <p>
	 * {@code getScribeIndex().register(scribe)}.
	 * </p>
	 * @param scribe the scribe to register
	 */
	public void registerScribe(ICalPropertyScribe<? extends ICalProperty> scribe) {
		index.register(scribe);
	}

	/**
	 * <p>
	 * Registers an experimental component scribe. Can also be used to override
	 * the scribe of a standard component (such as VEVENT). Calling this method
	 * is the same as calling:
	 * </p>
	 * <p>
	 * {@code getScribeIndex().register(scribe)}.
	 * </p>
	 * @param scribe the scribe to register
	 */
	public void registerScribe(ICalComponentScribe<? extends ICalComponent> scribe) {
		index.register(scribe);
	}

	/**
	 * Gets the object that manages the component/property scribes.
	 * @return the scribe index
	 */
	public ScribeIndex getScribeIndex() {
		return index;
	}

	/**
	 * Sets the object that manages the component/property scribes.
	 * @param index the scribe index
	 */
	public void setScribeIndex(ScribeIndex index) {
		this.index = index;
	}

	/**
	 * Gets the warnings from the last iCalendar object that was read.
	 * @return the warnings or empty list if there were no warnings
	 */
	public List<ParseWarning> getWarnings() {
		return new ArrayList<ParseWarning>(warnings);
	}

	/**
	 * Gets the timezone that will be used for parsing date property values that
	 * are floating or that have invalid timezone definitions assigned to them.
	 * Defaults to {@link TimeZone#getDefault}.
	 * @return the default timezone
	 */
	public TimeZone getDefaultTimezone() {
		return defaultTimezone;
	}

	/**
	 * Sets the timezone that will be used for parsing date property values that
	 * are floating or that have invalid timezone definitions assigned to them.
	 * Defaults to {@link TimeZone#getDefault}.
	 * @param defaultTimezone the default timezone
	 */
	public void setDefaultTimezone(TimeZone defaultTimezone) {
		this.defaultTimezone = defaultTimezone;
	}

	/**
	 * Gets the resolver that maps global timezone IDs to Java {@link TimeZone}
	 * objects. Defaults to {@link DefaultGlobalTimezoneIdResolver}.
	 * @return the resolver
	 */
	public GlobalTimezoneIdResolver getGlobalTimezoneIdResolver() {
		return globalTimezoneIdResolver;
	}

	/**
	 * Sets the resolver that maps global timezone IDs to Java {@link TimeZone}
	 * objects. Defaults to {@link DefaultGlobalTimezoneIdResolver}.
	 * @param globalTimezoneIdResolver the resolver
	 */
	public void setGlobalTimezoneIdResolver(GlobalTimezoneIdResolver globalTimezoneIdResolver) {
		this.globalTimezoneIdResolver = globalTimezoneIdResolver;
	}

	/**
	 * Reads all iCalendar objects from the data stream.
	 * @return the iCalendar objects
	 * @throws IOException if there's a problem reading from the stream
	 */
	public List<ICalendar> readAll() throws IOException {
		List<ICalendar> icals = new ArrayList<ICalendar>();
		ICalendar ical;
		while ((ical = readNext()) != null) {
			icals.add(ical);
		}
		return icals;
	}

	/**
	 * Reads the next iCalendar object from the data stream.
	 * @return the next iCalendar object or null if there are no more
	 * @throws IOException if there's a problem reading from the stream
	 */
	public ICalendar readNext() throws IOException {
		warnings.clear();
		context = new ParseContext();
		ICalendar ical = _readNext();
		if (ical == null) {
			return null;
		}

		ical.setVersion(context.getVersion());
		handleTimezones(ical);
		return ical;
	}

	/**
	 * Reads the next iCalendar object from the data stream.
	 * @return the next iCalendar object or null if there are no more
	 * @throws IOException if there's a problem reading from the stream
	 */
	protected abstract ICalendar _readNext() throws IOException;

	private void handleTimezones(ICalendar ical) {
		TimezoneInfo tzinfo = ical.getTimezoneInfo();

		//convert vCalendar DAYLIGHT and TZ properties to a VTIMEZONE component
		TimezoneAssignment vcalTimezone = extractVCalTimezone(ical);

		//assign a TimeZone object to each VTIMEZONE component.
		Iterator<VTimezone> it = ical.getComponents(VTimezone.class).iterator();
		while (it.hasNext()) {
			VTimezone component = it.next();

			//make sure the component has an ID
			String id = ValuedProperty.getValue(component.getTimezoneId());
			if (id == null || id.trim().isEmpty()) {
				//note: do not remove invalid VTIMEZONE components from the ICalendar object
				warnings.add(new ParseWarning.Builder().message(39).build());
				continue;
			}

			TimeZone timezone = new ICalTimeZone(component);
			tzinfo.getTimezones().add(new TimezoneAssignment(timezone, component));

			//remove the component from the ICalendar object
			it.remove();
		}

		boolean userChangedTheDefaultTimezone = !defaultTimezone.equals(TimeZone.getDefault());

		if (vcalTimezone != null) {
			//vCal: parse floating dates according to the DAYLIGHT and TZ properties (which were converted to a VTIMEZONE component)
			Calendar cal = Calendar.getInstance(vcalTimezone.getTimeZone());
			for (TimezonedDate timezonedDate : context.getFloatingDates()) {
				reparseDateUnderDifferentTimezone(timezonedDate, cal);
			}
		} else {
			//iCal: treat floating dates as floating dates
			for (TimezonedDate timezonedDate : context.getFloatingDates()) {
				tzinfo.setFloating(timezonedDate.getProperty(), true);
			}

			//convert all floating dates to the default timezone
			if (userChangedTheDefaultTimezone) {
				Calendar cal = Calendar.getInstance(defaultTimezone);
				for (TimezonedDate timezonedDate : context.getFloatingDates()) {
					reparseDateUnderDifferentTimezone(timezonedDate, cal);
				}
			}
		}

		//convert all date values to their appropriate timezone
		for (Map.Entry<String, List<TimezonedDate>> entry : context.getTimezonedDates()) {
			String tzid = entry.getKey();

			//determine which timezone is associated with the given TZID
			TimezoneAssignment assignment = determineTimezoneAssignment(tzid, tzinfo);

			/*
			 * If a timezone assignment could not be found for the given TZID
			 * and the user did not change the default timezone, then there is
			 * no need to further process the properties that are assigned to
			 * this TZID--the date value should remain unchanged (parsed under
			 * the local machine's default timezone), and its TZID parameter
			 * should also remain.
			 */
			if (assignment == null && !userChangedTheDefaultTimezone) {
				continue;
			}

			//convert each property to the timezone
			TimeZone tz = (assignment == null) ? defaultTimezone : assignment.getTimeZone();
			Calendar cal = Calendar.getInstance(tz);
			for (TimezonedDate timezonedDate : entry.getValue()) {
				ICalProperty property = timezonedDate.getProperty();

				if (assignment != null) {
					tzinfo.setTimezone(property, assignment);

					/*
					 * Only remove the TZID parameter if the TZID is *valid*.
					 * Invalid TZID parameters should remain so that user can
					 * inspect the invalid information.
					 */
					property.getParameters().setTimezoneId(null);
				}

				reparseDateUnderDifferentTimezone(timezonedDate, cal);
			}
		}
	}

	private void reparseDateUnderDifferentTimezone(TimezonedDate timezonedDate, Calendar cal) {
		ICalDate date = timezonedDate.getDate();

		//parse its raw date components under its real timezone
		Date realDate = date.getRawComponents().toDate(cal);

		//update the Date object with the new timestamp
		date.setTime(realDate.getTime());
	}

	/**
	 * Determines the timezone definition that is associated with the given ID.
	 * @param tzid the timezone ID
	 * @param tzinfo the timezone settings of the iCalendar object
	 * @return the timezone definition or null to use the default timezone
	 */
	private TimezoneAssignment determineTimezoneAssignment(String tzid, TimezoneInfo tzinfo) {
		boolean isOlsenId = tzid.startsWith("/");

		//HANDLE OLSEN IDS======================================================

		if (isOlsenId) {
			String globalId = tzid.substring(1);
			TimeZone timezone = globalTimezoneIdResolver.resolve(globalId);
			if (timezone != null) {
				/*
				 * Olsen ID is valid. Everything is Ok.
				 */
				TimezoneAssignment assignment = new TimezoneAssignment(timezone, globalId);
				tzinfo.getTimezones().add(assignment);
				return assignment;
			}

			/*
			 * Even though the TZID is marked as an Olsen ID, and the timezone
			 * isn't recognized by Java, try looking for a VTIMEZONE component
			 * that matches it.
			 *
			 * This is done as a courtesy and is not required by the specs.
			 */
			TimezoneAssignment assignment = tzinfo.getTimezoneById(tzid);
			int warning;
			if (assignment == null) {
				/*
				 * TZID does not match any VTIMEZONE components, use the default
				 * timezone.
				 */
				warning = 38;
			} else {
				warning = 43;
			}

			warnings.add(new ParseWarning.Builder().message(warning, tzid).build());
			return assignment;
		}

		//HANDLE VTIMEZONE COMPONENT IDS========================================

		TimezoneAssignment assignment = tzinfo.getTimezoneById(tzid);
		if (assignment != null) {
			/*
			 * VTIMEZONE component with the given TZID was found.
			 * Everything is Ok.
			 */
			return assignment;
		}

		/*
		 * Try treating the TZID as an Olsen timezone ID, even though it does
		 * not start with a forward slash.
		 *
		 * This is done as a courtesy for users who do not know they must prefix
		 * Olsen IDs with a forward slash. It is not required by the specs.
		 */
		String globalId = tzid;
		TimeZone timezone = globalTimezoneIdResolver.resolve(globalId);
		int warning;
		if (timezone == null) {
			/*
			 * TZID is not a valid Olsen ID, use the default timezone.
			 */
			warning = 38;
			assignment = null;
		} else {
			/*
			 * TZID was successfully parsed as an Olsen ID.
			 */
			warning = 37;
			assignment = new TimezoneAssignment(timezone, globalId);
			tzinfo.getTimezones().add(assignment);
		}

		warnings.add(new ParseWarning.Builder().message(warning, globalId).build());
		return assignment;
	}

	private TimezoneAssignment extractVCalTimezone(ICalendar ical) {
		List<Daylight> daylights = ical.removeProperties(Daylight.class);
		List<Timezone> timezones = ical.removeProperties(Timezone.class);

		Timezone timezone = timezones.isEmpty() ? null : timezones.get(0);
		VTimezone vcalComponent = convert(daylights, timezone);
		if (vcalComponent == null) {
			return null;
		}

		TimeZone icalTimezone = new ICalTimeZone(vcalComponent);
		TimezoneInfo tzinfo = ical.getTimezoneInfo();
		TimezoneAssignment assignment = new TimezoneAssignment(icalTimezone, vcalComponent);
		tzinfo.setDefaultTimezone(assignment);

		return assignment;
	}
}
