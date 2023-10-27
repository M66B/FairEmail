package biweekly.property;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import biweekly.ICalVersion;

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
 * Defines the type of action to invoke when an alarm is triggered.
 * </p>
 * 
 * <p>
 * <b>Code sample (creating):</b>
 * </p>
 * 
 * <pre class="brush:java">
 * Action action = Action.audio();
 * </pre>
 * 
 * <p>
 * <b>Code sample (retrieving):</b>
 * </p>
 * 
 * <pre class="brush:java">
 * ICalendar ical = ...
 * for (VAlarm alarm : ical.getAlarms()) {
 *   Action action = alarm.getAction();
 *   if (action.isAudio()) {
 *     //...
 *   } else if (action.isEmail()) {
 *     //...
 *   } else if (action.isDisplay()) {
 *     //...
 *   }
 * }
 * </pre>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5545#page-132">RFC 5545
 * p.132-3</a>
 * @see <a href="http://tools.ietf.org/html/rfc2445#page-126">RFC 2445 p.126</a>
 */
public class Action extends EnumProperty {
	public static final String AUDIO = "AUDIO";
	public static final String DISPLAY = "DISPLAY";
	public static final String EMAIL = "EMAIL";
	public static final String PROCEDURE = "PROCEDURE";

	/**
	 * Creates an action property. Use of this constructor is discouraged and
	 * may put the property in an invalid state. Use one of the static factory
	 * methods instead.
	 * @param value the value (e.g. "AUDIO")
	 */
	public Action(String value) {
		super(value);
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public Action(Action original) {
		super(original);
	}

	/**
	 * Creates an "audio" action property.
	 * @return the property
	 */
	public static Action audio() {
		return create(AUDIO);
	}

	/**
	 * Determines if this property is an "audio" action.
	 * @return true if it's an "audio" action, false if not
	 */
	public boolean isAudio() {
		return is(AUDIO);
	}

	/**
	 * Creates an "display" action property.
	 * @return the property
	 */
	public static Action display() {
		return create(DISPLAY);
	}

	/**
	 * Determines if this property is an "display" action.
	 * @return true if it's an "display" action, false if not
	 */
	public boolean isDisplay() {
		return is(DISPLAY);
	}

	/**
	 * Creates an "email" action property.
	 * @return the property
	 */
	public static Action email() {
		return create(EMAIL);
	}

	/**
	 * Determines if this property is an "email" action.
	 * @return true if it's an "email" action, false if not
	 */
	public boolean isEmail() {
		return is(EMAIL);
	}

	/**
	 * Creates a "procedure" action property (vCal 1.0 only).
	 * @return the property
	 */
	public static Action procedure() {
		return create(PROCEDURE);
	}

	/**
	 * Determines if this property is a "procedure" action (vCal 1.0 only).
	 * @return true if it's a "procedure" action, false if not
	 */
	public boolean isProcedure() {
		return is(PROCEDURE);
	}

	private static Action create(String value) {
		return new Action(value);
	}

	@Override
	protected Collection<String> getStandardValues(ICalVersion version) {
		switch (version) {
		case V1_0:
			return Arrays.asList(AUDIO, DISPLAY, EMAIL, PROCEDURE);
		default:
			return Arrays.asList(AUDIO, DISPLAY, EMAIL);
		}
	}

	@Override
	protected Collection<ICalVersion> getValueSupportedVersions() {
		if (value == null) {
			return Collections.emptyList();
		}

		if (isAudio() || isDisplay() || isEmail()) {
			return Arrays.asList(ICalVersion.values());
		}
		if (isProcedure()) {
			return Collections.singletonList(ICalVersion.V1_0);
		}

		return Collections.emptyList();
	}

	@Override
	public Action copy() {
		return new Action(this);
	}
}
