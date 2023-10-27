package biweekly.property;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import biweekly.ICalVersion;
import biweekly.ValidationWarning;
import biweekly.component.ICalComponent;
import biweekly.util.Frequency;
import biweekly.util.Google2445Utils;
import biweekly.util.ICalDate;
import biweekly.util.Recurrence;
import biweekly.util.com.google.ical.compat.javautil.DateIterator;

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
 * Represents a property whose value is a recurrence rule.
 * @author Michael Angstadt
 */
public class RecurrenceProperty extends ValuedProperty<Recurrence> {
	/**
	 * Creates a new recurrence property.
	 * @param recur the recurrence value
	 */
	public RecurrenceProperty(Recurrence recur) {
		super(recur);
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public RecurrenceProperty(RecurrenceProperty original) {
		super(original);
	}

	/**
	 * Creates an iterator that computes the dates defined by this property.
	 * @param startDate the date that the recurrence starts (typically, the
	 * value of the accompanying {@link DateStart} property)
	 * @param timezone the timezone to iterate in (typically, the timezone
	 * associated with the {@link DateStart} property). This is needed in order
	 * to adjust for when the iterator passes over a daylight savings boundary.
	 * @return the iterator
	 * @see <a
	 * href="https://code.google.com/p/google-rfc-2445/">google-rfc-2445</a>
	 */
	public DateIterator getDateIterator(Date startDate, TimeZone timezone) {
		return getDateIterator(new ICalDate(startDate), timezone);
	}

	/**
	 * Creates an iterator that computes the dates defined by this property.
	 * @param startDate the date that the recurrence starts (typically, the
	 * value of the accompanying {@link DateStart} property)
	 * @param timezone the timezone to iterate in (typically, the timezone
	 * associated with the {@link DateStart} property). This is needed in order
	 * to adjust for when the iterator passes over a daylight savings boundary.
	 * @return the iterator
	 * @see <a
	 * href="https://code.google.com/p/google-rfc-2445/">google-rfc-2445</a>
	 */
	public DateIterator getDateIterator(ICalDate startDate, TimeZone timezone) {
		Recurrence recur = getValue();
		return (recur == null) ? new Google2445Utils.EmptyDateIterator() : recur.getDateIterator(startDate, timezone);
	}

	@Override
	protected void validate(List<ICalComponent> components, ICalVersion version, List<ValidationWarning> warnings) {
		super.validate(components, version, warnings);
		if (value == null) {
			return;
		}

		if (value.getFrequency() == null) {
			warnings.add(new ValidationWarning(30));
		}

		if (value.getUntil() != null && value.getCount() != null) {
			warnings.add(new ValidationWarning(31));
		}

		switch (version) {
		case V1_0:
			if (!value.getXRules().isEmpty()) {
				warnings.add(new ValidationWarning("X-Rules are not supported by vCal."));
			}
			if (!value.getBySetPos().isEmpty()) {
				warnings.add(new ValidationWarning("BYSETPOS is not supported by vCal."));
			}
			if (value.getFrequency() == Frequency.SECONDLY) {
				warnings.add(new ValidationWarning(Frequency.SECONDLY.name() + " frequency is not supported by vCal."));
			}
			break;

		case V2_0_DEPRECATED:
			//empty
			break;

		case V2_0:
			if (!value.getXRules().isEmpty()) {
				warnings.add(new ValidationWarning(32));
			}

			break;
		}
	}
}
