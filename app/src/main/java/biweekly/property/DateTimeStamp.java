package biweekly.property;

import java.util.Date;

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
 * The meaning of this property varies depending on whether the iCalendar object
 * has a {@link Method} property.
 * </p>
 * <ul>
 * <li><b>Has a {@link Method} property:</b> Defines the creation date of the
 * iCalendar object itself (<u>not</u> the creation date of the actual calendar
 * data from the originating data store). Use the {@link Created} property to
 * define the creation date of the actual calendar data.</li>
 * <li><b>Does not have a {@link Method} property:</b> Defines the date that the
 * calendar data was last updated (the {@link LastModified} property also holds
 * this information).</li>
 * </ul>
 * <p>
 * <b>Code sample:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VEvent event = new VEvent();
 * 
 * Date datetime = ... 
 * DateTimeStamp dtstamp = new DateTimeStamp(datetime);
 * event.setDateTimeStamp(dtstamp);
 * </pre>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5545#page-137">RFC 5545
 * p.137-8</a>
 * @see <a href="http://tools.ietf.org/html/rfc2445#page-130">RFC 2445
 * p.130-1</a>
 */
public class DateTimeStamp extends DateTimeProperty {
	/**
	 * Creates a date time stamp property.
	 * @param date the date
	 */
	public DateTimeStamp(Date date) {
		super(date);
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public DateTimeStamp(DateTimeStamp original) {
		super(original);
	}

	@Override
	public DateTimeStamp copy() {
		return new DateTimeStamp(this);
	}
}
