package biweekly.property;

import biweekly.component.VTimezone;

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
 * Defines a unique identifier for a {@link VTimezone} component. The identifier
 * must be unique within the scope of the iCalendar object.
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5545#page-102">RFC 5545
 * p.102-3</a>
 * @see <a href="http://tools.ietf.org/html/rfc2445#page-97">RFC 2445 p.97-8</a>
 */
public class TimezoneId extends TextProperty {
	/**
	 * Creates a timezone identifier property.
	 * @param timezone the timezone identifier
	 */
	public TimezoneId(String timezone) {
		super(timezone);
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public TimezoneId(TimezoneId original) {
		super(original);
	}

	@Override
	public TimezoneId copy() {
		return new TimezoneId(this);
	}
}
