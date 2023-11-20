package biweekly.property;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import biweekly.ICalVersion;
import biweekly.ValidationWarning;
import biweekly.component.ICalComponent;
import biweekly.util.ICalDate;
import biweekly.util.UtcOffset;

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
 * Represents daylight savings time information.
 * @author Michael Angstadt
 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.23</a>
 */
public class Daylight extends ICalProperty {
	private boolean daylight;
	private UtcOffset offset;
	private ICalDate start, end;
	private String standardName, daylightName;

	/**
	 * Creates a daylight savings property which states that the timezone does
	 * not observe daylight savings time.
	 */
	public Daylight() {
		this.daylight = false;
	}

	/**
	 * Creates a daylight savings property.
	 * @param daylight true if the timezone observes daylight savings time,
	 * false if not
	 * @param offset the UTC offset of daylight savings time
	 * @param start the start date of daylight savings time
	 * @param end the end date of daylight savings time
	 * @param standardName the timezone's name for standard time (e.g. "EST")
	 * @param daylightName the timezone's name for daylight savings time (e.g.
	 * "EDT")
	 */
	public Daylight(boolean daylight, UtcOffset offset, ICalDate start, ICalDate end, String standardName, String daylightName) {
		this.daylight = daylight;
		this.offset = offset;
		this.start = start;
		this.end = end;
		this.standardName = standardName;
		this.daylightName = daylightName;
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public Daylight(Daylight original) {
		super(original);
		daylight = original.daylight;
		offset = original.offset;
		start = (original.start == null) ? null : new ICalDate(original.start);
		end = (original.end == null) ? null : new ICalDate(original.end);
		standardName = original.standardName;
		daylightName = original.daylightName;
	}

	/**
	 * Gets whether this timezone observes daylight savings time.
	 * @return true if it observes daylight savings time, false if not
	 */
	public boolean isDaylight() {
		return daylight;
	}

	/**
	 * Sets whether this timezone observes daylight savings time.
	 * @param daylight true if it observes daylight savings time, false if not
	 */
	public void setDaylight(boolean daylight) {
		this.daylight = daylight;
	}

	/**
	 * Gets the UTC offset of daylight savings time.
	 * @return the UTC offset
	 */
	public UtcOffset getOffset() {
		return offset;
	}

	/**
	 * Sets the UTC offset of daylight savings time.
	 * @param offset the UTC offset
	 */
	public void setOffset(UtcOffset offset) {
		this.offset = offset;
	}

	/**
	 * Gets the start date of dayight savings time.
	 * @return the start date
	 */
	public ICalDate getStart() {
		return start;
	}

	/**
	 * Sets the start date of dayight savings time.
	 * @param start the start date
	 */
	public void setStart(ICalDate start) {
		this.start = start;
	}

	/**
	 * Gets the end date of daylight savings time.
	 * @return the end date
	 */
	public ICalDate getEnd() {
		return end;
	}

	/**
	 * Sets the end date of daylight savings time.
	 * @param end the end date
	 */
	public void setEnd(ICalDate end) {
		this.end = end;
	}

	/**
	 * Gets the name for standard time.
	 * @return the name (e.g. "EST")
	 */
	public String getStandardName() {
		return standardName;
	}

	/**
	 * Sets the name for standard time.
	 * @param name the name (e.g. "EST")
	 */
	public void setStandardName(String name) {
		this.standardName = name;
	}

	/**
	 * Gets the name of daylight savings time.
	 * @return the name (e.g. "EDT")
	 */
	public String getDaylightName() {
		return daylightName;
	}

	/**
	 * Sets the name of daylight savings time.
	 * @param name the name (e.g. "EDT")
	 */
	public void setDaylightName(String name) {
		this.daylightName = name;
	}

	@Override
	protected void validate(List<ICalComponent> components, ICalVersion version, List<ValidationWarning> warnings) {
		if (daylight && (offset == null || start == null || end == null || standardName == null || daylightName == null)) {
			warnings.add(new ValidationWarning(43));
		}
	}

	@Override
	protected Map<String, Object> toStringValues() {
		Map<String, Object> values = new LinkedHashMap<String, Object>();
		values.put("daylight", daylight);
		values.put("offset", offset);
		values.put("start", start);
		values.put("end", end);
		values.put("standardName", standardName);
		values.put("daylightName", daylightName);
		return values;
	}

	@Override
	public Daylight copy() {
		return new Daylight(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (daylight ? 1231 : 1237);
		result = prime * result + ((daylightName == null) ? 0 : daylightName.hashCode());
		result = prime * result + ((end == null) ? 0 : end.hashCode());
		result = prime * result + ((offset == null) ? 0 : offset.hashCode());
		result = prime * result + ((standardName == null) ? 0 : standardName.hashCode());
		result = prime * result + ((start == null) ? 0 : start.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		Daylight other = (Daylight) obj;
		if (daylight != other.daylight) return false;
		if (daylightName == null) {
			if (other.daylightName != null) return false;
		} else if (!daylightName.equals(other.daylightName)) return false;
		if (end == null) {
			if (other.end != null) return false;
		} else if (!end.equals(other.end)) return false;
		if (offset == null) {
			if (other.offset != null) return false;
		} else if (!offset.equals(other.offset)) return false;
		if (standardName == null) {
			if (other.standardName != null) return false;
		} else if (!standardName.equals(other.standardName)) return false;
		if (start == null) {
			if (other.start != null) return false;
		} else if (!start.equals(other.start)) return false;
		return true;
	}
}
