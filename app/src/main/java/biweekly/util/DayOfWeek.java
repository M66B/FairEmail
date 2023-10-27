package biweekly.util;

import java.util.Calendar;

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
 * Represents each of the seven days of the week.
 * @author Michael Angstadt
 */
public enum DayOfWeek {
	/*
	 * These constants are defined in the order that the days of the week are
	 * arranged on a calendar. Do not rearrange them. Other parts of the code
	 * base rely on this ordering (i.e. this class's ordinal() and values()
	 * methods are used).
	 */
	//@formatter:off
	SUNDAY("SU", Calendar.SUNDAY),
	MONDAY("MO", Calendar.MONDAY),
	TUESDAY("TU", Calendar.TUESDAY),
	WEDNESDAY("WE", Calendar.WEDNESDAY),
	THURSDAY("TH", Calendar.THURSDAY),
	FRIDAY("FR", Calendar.FRIDAY),
	SATURDAY("SA", Calendar.SATURDAY);
	//@formatter:on

	private final String abbr;
	private final int calendarConstant;

	DayOfWeek(String abbr, int calendarConstant) {
		this.abbr = abbr;
		this.calendarConstant = calendarConstant;
	}

	/**
	 * Gets the day's abbreviation.
	 * @return the abbreviation (e.g. "MO" for Monday)
	 */
	public String getAbbr() {
		return abbr;
	}

	/**
	 * Gets the integer constant the {@link Calendar} class uses for this day.
	 * @return the constant
	 */
	public int getCalendarConstant() {
		return calendarConstant;
	}

	/**
	 * Gets a day by its abbreviation.
	 * @param abbr the abbreviation (case-insensitive, e.g. "MO" for Monday)
	 * @return the day or null if not found
	 */
	public static DayOfWeek valueOfAbbr(String abbr) {
		for (DayOfWeek day : values()) {
			if (day.abbr.equalsIgnoreCase(abbr)) {
				return day;
			}
		}
		return null;
	}
}