package biweekly.component;

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
 * Defines the date range of a timezone's standard time.
 * </p>
 * <p>
 * <b>Examples:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VTimezone timezone = new VTimezone("Eastern Standard Time");
 * StandardTime standard = new StandardTime();
 * DateTimeComponents components = new DateTimeComponents(1998, 10, 25, 2, 0, 0, false);
 * standard.setDateStart(components);
 * standard.setTimezoneOffsetFrom(-4, 0);
 * standard.setTimezoneOffsetTo(-5, 0);
 * timezone.addStandardTime(standard);
 * </pre>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5545#page-62">RFC 5545
 * p.62-71</a>
 * @see <a href="http://tools.ietf.org/html/rfc2445#page-60">RFC 2445 p.60-7</a>
 */
public class StandardTime extends Observance {
	public StandardTime() {
		//empty
	}

	/**
	 * Copy constructor.
	 * @param original the component to make a copy of
	 */
	public StandardTime(StandardTime original) {
		super(original);
	}

	@Override
	public StandardTime copy() {
		return new StandardTime(this);
	}
}
