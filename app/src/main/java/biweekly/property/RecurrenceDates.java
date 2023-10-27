package biweekly.property;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import biweekly.ICalVersion;
import biweekly.ValidationWarning;
import biweekly.component.ICalComponent;
import biweekly.util.ICalDate;
import biweekly.util.Period;

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
 * Defines a list of dates or time periods that help define a recurrence rule.
 * It must contain either dates or time periods. It cannot contain a combination
 * of both.
 * </p>
 * <p>
 * <b>Code sample:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VEvent event = new VEvent();
 * 
 * //date-time values
 * Date datetime = ...
 * RecurrenceDates rdate = new RecurrenceDates();
 * rdate.getDates().add(new ICalDate(datetime, true));
 * event.addRecurrenceDates(rdate);
 * 
 * //date values
 * Date date = ...
 * RecurrenceDates rdate = new RecurrenceDates();
 * rdate.getDates().add(new ICalDate(date, false));
 * event.addRecurrenceDates(rdate);
 * 
 * //periods
 * Period period = ...
 * rdate = new RecurrenceDates();
 * rdate.getPeriods().add(period);
 * event.addRecurrenceDates(rdate);
 * </pre>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5545#page-120">RFC 5545
 * p.120-2</a>
 * @see <a href="http://tools.ietf.org/html/rfc2445#page-115">RFC 2445
 * p.115-7</a>
 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.34</a>
 */
public class RecurrenceDates extends ICalProperty {
	private final List<ICalDate> dates;
	private final List<Period> periods;

	public RecurrenceDates() {
		dates = new ArrayList<ICalDate>();
		periods = new ArrayList<Period>();
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public RecurrenceDates(RecurrenceDates original) {
		super(original);

		dates = new ArrayList<ICalDate>(original.dates.size());
		for (ICalDate date : original.dates) {
			dates.add(new ICalDate(date));
		}

		periods = new ArrayList<Period>(original.periods.size());
		for (Period period : original.periods) {
			periods.add(new Period(period));
		}
	}

	/**
	 * Gets the list that stores this property's recurrence dates.
	 * @return the dates (this list is mutable)
	 */
	public List<ICalDate> getDates() {
		return dates;
	}

	/**
	 * Gets the list that stores this property's time periods.
	 * @return the time periods (this list is mutable)
	 */
	public List<Period> getPeriods() {
		return periods;
	}

	@Override
	protected void validate(List<ICalComponent> components, ICalVersion version, List<ValidationWarning> warnings) {
		if (dates.isEmpty() && periods.isEmpty()) {
			//no value
			warnings.add(new ValidationWarning(26));
		}

		if (!dates.isEmpty() && !periods.isEmpty()) {
			//can't mix dates and periods
			warnings.add(new ValidationWarning(49));
		}

		if (version == ICalVersion.V1_0 && !periods.isEmpty()) {
			//1.0 doesn't support periods
			warnings.add(new ValidationWarning(51));
		}

		if (!dates.isEmpty()) {
			//can't mix date and date-time values
			boolean hasTime = dates.get(0).hasTime();
			for (ICalDate date : dates.subList(1, dates.size())) {
				if (date.hasTime() != hasTime) {
					warnings.add(new ValidationWarning(50));
					break;
				}
			}
		}
	}

	@Override
	protected Map<String, Object> toStringValues() {
		Map<String, Object> values = new LinkedHashMap<String, Object>();
		values.put("dates", dates);
		values.put("periods", periods);
		return values;
	}

	@Override
	public RecurrenceDates copy() {
		return new RecurrenceDates(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + dates.hashCode();
		result = prime * result + periods.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		RecurrenceDates other = (RecurrenceDates) obj;
		if (!dates.equals(other.dates)) return false;
		if (!periods.equals(other.periods)) return false;
		return true;
	}
}
