package biweekly.property;

import biweekly.component.VAlarm;
import biweekly.component.VEvent;
import biweekly.component.VTodo;
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
 * Defines a duration of time (for example, "2 hours and 30 minutes"). This
 * property has different meanings depending on the component it belongs to:
 * </p>
 * <ul>
 * <li>{@link VEvent} - The duration of the event (used in place of a
 * {@link DateEnd} property).</li>
 * <li>{@link VTodo} - The duration of the to-do task (used in place of a
 * {@link DateEnd} property).</li>
 * <li>{@link VAlarm} - The pause between alarm repetitions.</li>
 * </ul>
 * <p>
 * <b>Code sample:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VEvent event = new VEvent();
 * 
 * Duration duration = Duration.builder().hours(2).minutes(30).build();
 * DurationProperty prop = new DurationProperty(duration);
 * event.setDuration(prop);
 * </pre>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5545#page-99">RFC 5545 p.99</a>
 * @see <a href="http://tools.ietf.org/html/rfc2445#page-94">RFC 2445 p.94-5</a>
 */
public class DurationProperty extends ValuedProperty<Duration> {
	/**
	 * Creates a duration property.
	 * @param duration the duration value (e.g. "2 hours and 30 minutes")
	 */
	public DurationProperty(Duration duration) {
		super(duration);
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public DurationProperty(DurationProperty original) {
		super(original);
	}

	@Override
	public DurationProperty copy() {
		return new DurationProperty(this);
	}
}
