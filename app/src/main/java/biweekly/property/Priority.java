package biweekly.property;

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
 * Defines the priority of an event or to-do task.
 * </p>
 * <p>
 * <b>Code sample:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VEvent event = new VEvent();
 * 
 * event.setPriority(1); //highest
 * event.setPriority(9); //lowest
 * </pre>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5545#page-89">RFC 5545
 * p.89-90</a>
 * @see <a href="http://tools.ietf.org/html/rfc2445#page-85">RFC 2445 p.85-7</a>
 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.33</a>
 */
public class Priority extends IntegerProperty {
	/**
	 * Creates a priority property.
	 * @param priority the priority ("0" is undefined, "1" is the highest, "9"
	 * is the lowest)
	 */
	public Priority(Integer priority) {
		super(priority);
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public Priority(Priority original) {
		super(original);
	}

	/**
	 * Determines if this priority is considered "high" priority.
	 * @return true if the priority is between 1 and 4, false if not
	 */
	public boolean isHigh() {
		return value != null && value >= 1 && value <= 4;
	}

	/**
	 * Determines if this priority is considered "medium" priority.
	 * @return true if the priority is "5", false if not
	 */
	public boolean isMedium() {
		return value != null && value == 5;
	}

	/**
	 * Determines if this priority is considered "low" priority.
	 * @return true if the priority is between 6 and 9, false if not
	 */
	public boolean isLow() {
		return value != null && value >= 6 && value <= 9;
	}

	/**
	 * Determines if this priority has an "undefined" value.
	 * @return true if the priority is "0", false if not
	 */
	public boolean isUndefined() {
		return value != null && value == 0;
	}

	/**
	 * Converts this priority to its two-character CUA code.
	 * @return the CUA code (e.g. "B1" for "4") or null if the priority cannot
	 * be converted to a CUA code
	 */
	public String toCuaPriority() {
		if (value == null || value < 1 || value > 9) {
			return null;
		}
		int letter = ((value - 1) / 3) + 'A';
		int number = ((value - 1) % 3) + 1;
		return (char) letter + "" + number;
	}

	@Override
	public Priority copy() {
		return new Priority(this);
	}
}
