package biweekly.property;

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
 * Defines the suggested minimum polling interval for checking for updates to
 * the calendar data.
 * </p>
 * <p>
 * <b>Code sample:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * ICalendar ical = new ICalendar()
 * 
 * Duration duration = Duration.builder().weeks(1).build();
 * RefreshInterval refreshInterval = new RefreshInterval(duration);
 * ical.setRefreshInterval(refreshInterval);
 * </pre>
 * @author Michael Angstadt
 * @see <a
 * href="http://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-7">draft-ietf-calext-extensions-01
 * p.7</a>
 */
public class RefreshInterval extends ValuedProperty<Duration> {
	/**
	 * Creates a refresh interval property.
	 * @param duration the duration value (e.g. "2 hours and 30 minutes")
	 */
	public RefreshInterval(Duration duration) {
		super(duration);
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public RefreshInterval(RefreshInterval original) {
		super(original);
	}

	@Override
	public RefreshInterval copy() {
		return new RefreshInterval(this);
	}
}
