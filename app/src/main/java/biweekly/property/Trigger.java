package biweekly.property;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import biweekly.ICalVersion;
import biweekly.ValidationWarning;
import biweekly.component.ICalComponent;
import biweekly.parameter.Related;
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
 * Defines when to trigger an alarm.
 * </p>
 * <p>
 * <b>Code sample:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * //15 minutes before the start time
 * Duration duration = Duration.builder().prior(true).minutes(15).build();
 * Trigger trigger = new Trigger(duration, Related.START);
 * VAlarm alarm = VAlarm.display(trigger, "Meeting in 15 minutes");
 * </pre>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5545#page-133">RFC 5545
 * p.133-6</a>
 * @see <a href="http://tools.ietf.org/html/rfc2445#page-127">RFC 2445
 * p.127-9</a>
 */
public class Trigger extends ICalProperty {
	private Duration duration;
	private Date date;

	/**
	 * Creates a trigger property.
	 * @param duration the relative time
	 * @param related the date-time field that the duration is relative to
	 */
	public Trigger(Duration duration, Related related) {
		setDuration(duration, related);
	}

	/**
	 * Creates a trigger property.
	 * @param date the date-time the alarm will trigger.
	 */
	public Trigger(Date date) {
		setDate(date);
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public Trigger(Trigger original) {
		super(original);
		date = (original.date == null) ? null : new Date(original.date.getTime());
		duration = original.duration;
	}

	/**
	 * Gets the relative time at which the alarm will trigger.
	 * @return the relative time or null if an absolute time is set
	 */
	public Duration getDuration() {
		return duration;
	}

	/**
	 * Sets a relative time at which the alarm will trigger.
	 * @param duration the relative time
	 * @param related the date-time field that the duration is relative to
	 */
	public void setDuration(Duration duration, Related related) {
		this.date = null;
		this.duration = duration;
		setRelated(related);
	}

	/**
	 * Gets the date-time that the alarm will trigger.
	 * @return the date-time or null if a relative duration is set
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * Sets the date-time that the alarm will trigger.
	 * @param date the date-time the alarm will trigger.
	 */
	public void setDate(Date date) {
		this.date = date;
		this.duration = null;
		setRelated(null);
	}

	/**
	 * Gets the date-time field that the duration is relative to.
	 * @return the field or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-24">RFC 5545
	 * p.24</a>
	 */
	public Related getRelated() {
		return parameters.getRelated();
	}

	/**
	 * Sets the date-time field that the duration is relative to.
	 * @param related the field or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-24">RFC 5545
	 * p.24</a>
	 */
	public void setRelated(Related related) {
		parameters.setRelated(related);
	}

	@Override
	protected void validate(List<ICalComponent> components, ICalVersion version, List<ValidationWarning> warnings) {
		if (duration == null && date == null) {
			warnings.add(new ValidationWarning(33));
		}

		Related related = getRelated();
		if (duration != null && related == null) {
			warnings.add(new ValidationWarning(10));
		}
	}

	@Override
	protected Map<String, Object> toStringValues() {
		Map<String, Object> values = new LinkedHashMap<String, Object>();
		values.put("duration", duration);
		values.put("date", date);
		return values;
	}

	@Override
	public Trigger copy() {
		return new Trigger(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + ((duration == null) ? 0 : duration.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		Trigger other = (Trigger) obj;
		if (date == null) {
			if (other.date != null) return false;
		} else if (!date.equals(other.date)) return false;
		if (duration == null) {
			if (other.duration != null) return false;
		} else if (!duration.equals(other.duration)) return false;
		return true;
	}
}
