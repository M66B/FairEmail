package biweekly.property;

import java.util.Date;

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
 * <p>
 * Defines the start date of an event, free/busy component, or timezone
 * component.
 * </p>
 * <p>
 * <b>Code sample (creating):</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VEvent event = new VEvent();
 * 
 * //date and time
 * Date datetime = ...
 * DateStart dtstart = new DateStart(datetime);
 * event.setDateStart(dtstart);
 * 
 * //date (without time component)
 * Date date = ...
 * dtstart = new DateStart(date, false);
 * event.setDateStart(dtstart);
 * </pre>
 * 
 * <p>
 * <b>Code sample (reading):</b>
 * </p>
 * 
 * <pre class="brush:java">
 * ICalendar ical = ...
 * for (VEvent event : ical.getEvents()) {
 *   DateStart dtstart = event.getDateStart();
 *   
 *   //get property value (ICalDate extends java.util.Date)
 *   ICalDate value = dtstart.getValue();
 *   
 *   if (value.hasTime()) {
 *     //the value includes a time component
 *   } else {
 *     //the value is just a date
 *     //date object's time is set to "00:00:00" under local computer's default timezone
 *   }
 *   
 *   //gets the timezone that the property value was parsed under if you are curious about that
 *   TimeZone tz = tzinfo.getTimeZone(dtstart);
 * }
 * </pre>
 * 
 * <p>
 * <b>Code sample (using timezones):</b>
 * </p>
 * 
 * <pre class="brush:java">
 * ICalendar ical = new ICalendar();
 * VEvent event = new VEvent();
 * Date datetime = ...
 * DateStart dtstart = new DateStart(datetime);
 * event.setDateStart(dtstart);
 * ical.addEvent(event);
 * 
 * TimezoneAssignment tz = ...
 * 
 * //set the timezone of all date-time property values
 * //date-time property values are written in UTC by default
 * ical.getTimezoneInfo().setDefaultTimezone(tz);
 * 
 * //or set the timezone just for this property
 * ical.getTimezoneInfo().setTimezone(dtstart, tz);
 * 
 * //finally, write the iCalendar object
 * ICalWriter writer = ...
 * writer.write(ical);
 * </pre>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5545#page-97">RFC 5545 p.97-8</a>
 * @see <a href="http://tools.ietf.org/html/rfc2445#page-93">RFC 2445 p.93-4</a>
 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.35</a>
 */
public class DateStart extends DateOrDateTimeProperty {
	/**
	 * Creates a start date property.
	 * @param startDate the start date
	 */
	public DateStart(Date startDate) {
		super(startDate);
	}

	/**
	 * Creates a start date property.
	 * @param startDate the start date
	 * @param hasTime true if the value has a time component, false if it is
	 * strictly a date
	 */
	public DateStart(Date startDate, boolean hasTime) {
		super(startDate, hasTime);
	}

	/**
	 * Creates a start date property.
	 * @param startDate the start date
	 */
	public DateStart(ICalDate startDate) {
		super(startDate);
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public DateStart(DateStart original) {
		super(original);
	}

	@Override
	public DateStart copy() {
		return new DateStart(this);
	}
}