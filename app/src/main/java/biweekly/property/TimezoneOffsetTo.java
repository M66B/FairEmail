package biweekly.property;

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
 * <p>
 * Defines the timezone offset that a timezone observance uses.
 * </p>
 * <p>
 * <b>Code sample:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VTimezone timezone = ...
 * StandardTime standard = new StandardTime();
 * standard.setTimezoneOffsetTo(-4, 0);
 * ...
 * timezone.addStandardTime(standard);
 * </pre>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5545#page-105">RFC 5545
 * p.105-6</a>
 * @see <a href="http://tools.ietf.org/html/rfc2445#page-100">RFC 2445
 * p.100-1</a>
 */
public class TimezoneOffsetTo extends UtcOffsetProperty {
	/**
	 * Creates a new timezone offset to property.
	 * @param offset the UTC offset
	 */
	public TimezoneOffsetTo(UtcOffset offset) {
		super(offset);
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public TimezoneOffsetTo(TimezoneOffsetTo original) {
		super(original);
	}

	@Override
	public TimezoneOffsetTo copy() {
		return new TimezoneOffsetTo(this);
	}
}
