package biweekly.property;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import biweekly.component.VAlarm;
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
 * Defines an alarm property that is part of the vCalendar (1.0) standard (such
 * as {@link AudioAlarm}).
 * </p>
 * <p>
 * Classes that extend this class are used internally by this library for
 * parsing purposes. If you are creating a new iCalendar object and need to
 * define an alarm, it is recommended that you use the {@link VAlarm} component
 * to create a new alarm.
 * </p>
 * @author Michael Angstadt
 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0</a>
 */
public class VCalAlarmProperty extends ICalProperty {
	protected Date start;
	protected Duration snooze;
	protected Integer repeat;

	public VCalAlarmProperty() {
		//empty
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public VCalAlarmProperty(VCalAlarmProperty original) {
		super(original);
		start = new Date(original.start.getTime());
		snooze = original.snooze;
		repeat = original.repeat;
	}

	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public Duration getSnooze() {
		return snooze;
	}

	public void setSnooze(Duration snooze) {
		this.snooze = snooze;
	}

	public Integer getRepeat() {
		return repeat;
	}

	public void setRepeat(Integer repeat) {
		this.repeat = repeat;
	}

	@Override
	protected Map<String, Object> toStringValues() {
		Map<String, Object> values = new LinkedHashMap<String, Object>();
		values.put("start", start);
		values.put("snooze", snooze);
		values.put("repeat", repeat);
		return values;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((repeat == null) ? 0 : repeat.hashCode());
		result = prime * result + ((snooze == null) ? 0 : snooze.hashCode());
		result = prime * result + ((start == null) ? 0 : start.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		VCalAlarmProperty other = (VCalAlarmProperty) obj;
		if (repeat == null) {
			if (other.repeat != null) return false;
		} else if (!repeat.equals(other.repeat)) return false;
		if (snooze == null) {
			if (other.snooze != null) return false;
		} else if (!snooze.equals(other.snooze)) return false;
		if (start == null) {
			if (other.start != null) return false;
		} else if (!start.equals(other.start)) return false;
		return true;
	}
}
