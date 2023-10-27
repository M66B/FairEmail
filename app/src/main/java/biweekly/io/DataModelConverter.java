package biweekly.io;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import biweekly.component.DaylightSavingsTime;
import biweekly.component.Observance;
import biweekly.component.StandardTime;
import biweekly.component.VTimezone;
import biweekly.io.ICalTimeZone.Boundary;
import biweekly.property.Daylight;
import biweekly.property.Timezone;
import biweekly.property.UtcOffsetProperty;
import biweekly.property.ValuedProperty;
import biweekly.util.DateTimeComponents;
import biweekly.util.ICalDate;
import biweekly.util.UtcOffset;
import biweekly.util.com.google.ical.values.DateTimeValue;

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
 * Converts various properties/components into other properties/components for
 * vCalendar-iCalendar compatibility.
 * @author Michael Angstadt
 */
public final class DataModelConverter {
	/**
	 * Converts vCalendar timezone information to an iCalendar {@link VTimezone}
	 * component.
	 * @param daylights the DAYLIGHT properties
	 * @param tz the TZ property
	 * @return the VTIMEZONE component
	 */
	public static VTimezone convert(List<Daylight> daylights, Timezone tz) {
		UtcOffset tzOffset = ValuedProperty.getValue(tz);
		if (daylights.isEmpty() && tzOffset == null) {
			return null;
		}

		VTimezone timezone = new VTimezone("TZ");
		if (daylights.isEmpty() && tzOffset != null) {
			StandardTime st = new StandardTime();
			st.setTimezoneOffsetFrom(tzOffset);
			st.setTimezoneOffsetTo(tzOffset);
			timezone.addStandardTime(st);
			return timezone;
		}

		for (Daylight daylight : daylights) {
			if (!daylight.isDaylight()) {
				continue;
			}

			UtcOffset daylightOffset = daylight.getOffset();
			UtcOffset standardOffset = new UtcOffset(daylightOffset.getMillis() - (1000 * 60 * 60));

			DaylightSavingsTime dst = new DaylightSavingsTime();
			dst.setDateStart(daylight.getStart());
			dst.setTimezoneOffsetFrom(standardOffset);
			dst.setTimezoneOffsetTo(daylightOffset);
			dst.addTimezoneName(daylight.getDaylightName());
			timezone.addDaylightSavingsTime(dst);

			StandardTime st = new StandardTime();
			st.setDateStart(daylight.getEnd());
			st.setTimezoneOffsetFrom(daylightOffset);
			st.setTimezoneOffsetTo(standardOffset);
			st.addTimezoneName(daylight.getStandardName());
			timezone.addStandardTime(st);
		}

		return timezone.getComponents().isEmpty() ? null : timezone;
	}

	/**
	 * Converts an iCalendar {@link VTimezone} component into the appropriate
	 * vCalendar properties.
	 * @param timezone the TIMEZONE component
	 * @param dates the date values in the vCalendar object that are effected by
	 * the timezone.
	 * @return the vCalendar properties
	 */
	public static VCalTimezoneProperties convert(VTimezone timezone, List<Date> dates) {
		List<Daylight> daylights = new ArrayList<Daylight>();
		Timezone tz = null;
		if (dates.isEmpty()) {
			return new VCalTimezoneProperties(daylights, tz);
		}

		ICalTimeZone icalTz = new ICalTimeZone(timezone);
		Collections.sort(dates);
		Set<DateTimeValue> daylightStartDates = new HashSet<DateTimeValue>();
		boolean zeroObservanceUsed = false;
		for (Date date : dates) {
			Boundary boundary = icalTz.getObservanceBoundary(date);
			Observance observance = boundary.getObservanceIn();
			Observance observanceAfter = boundary.getObservanceAfter();
			if (observance == null && observanceAfter == null) {
				continue;
			}

			if (observance == null) {
				//the date comes before the earliest observance
				if (observanceAfter instanceof StandardTime && !zeroObservanceUsed) {
					UtcOffset offset = getOffset(observanceAfter.getTimezoneOffsetFrom());
					DateTimeValue start = null;
					DateTimeValue end = boundary.getObservanceAfterStart();
					String standardName = icalTz.getDisplayName(false, TimeZone.SHORT);
					String daylightName = icalTz.getDisplayName(true, TimeZone.SHORT);

					Daylight daylight = new Daylight(true, offset, convert(start), convert(end), standardName, daylightName);
					daylights.add(daylight);
					zeroObservanceUsed = true;
				}

				if (observanceAfter instanceof DaylightSavingsTime) {
					UtcOffset offset = getOffset(observanceAfter.getTimezoneOffsetFrom());
					if (offset != null) {
						tz = new Timezone(offset);
					}
				}

				continue;
			}

			if (observance instanceof StandardTime) {
				UtcOffset offset = getOffset(observance.getTimezoneOffsetTo());
				if (offset != null) {
					tz = new Timezone(offset);
				}
				continue;
			}

			if (observance instanceof DaylightSavingsTime && !daylightStartDates.contains(boundary.getObservanceInStart())) {
				UtcOffset offset = getOffset(observance.getTimezoneOffsetTo());
				DateTimeValue start = boundary.getObservanceInStart();
				DateTimeValue end = null;
				if (observanceAfter != null) {
					end = boundary.getObservanceAfterStart();
				}

				String standardName = icalTz.getDisplayName(false, TimeZone.SHORT);
				String daylightName = icalTz.getDisplayName(true, TimeZone.SHORT);

				Daylight daylight = new Daylight(true, offset, convert(start), convert(end), standardName, daylightName);
				daylights.add(daylight);
				daylightStartDates.add(start);
				continue;
			}
		}

		if (tz == null) {
			int rawOffset = icalTz.getRawOffset();
			UtcOffset offset = new UtcOffset(rawOffset);
			tz = new Timezone(offset);
		}

		if (daylights.isEmpty()) {
			Daylight daylight = new Daylight();
			daylight.setDaylight(false);
			daylights.add(daylight);
		}

		return new VCalTimezoneProperties(daylights, tz);
	}

	private static UtcOffset getOffset(UtcOffsetProperty property) {
		return (property == null) ? null : property.getValue();
	}

	private static ICalDate convert(DateTimeValue value) {
		if (value == null) {
			return null;
		}

		//@formatter:off
		DateTimeComponents components = new DateTimeComponents(
			value.year(),
			value.month(),
			value.day(),
			value.hour(),
			value.minute(),
			value.second(),
			false
		);
		//@formatter:on

		return new ICalDate(components, true);
	}

	public static class VCalTimezoneProperties {
		private final List<Daylight> daylights;
		private final Timezone tz;

		public VCalTimezoneProperties(List<Daylight> daylights, Timezone tz) {
			this.daylights = daylights;
			this.tz = tz;
		}

		public List<Daylight> getDaylights() {
			return daylights;
		}

		public Timezone getTz() {
			return tz;
		}
	}

	private DataModelConverter() {
		//hide
	}
}
