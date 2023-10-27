package biweekly.parameter;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import biweekly.ICalVersion;
import biweekly.util.CaseClasses;

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
 * Defines what level of participation is expected from a calendar user. Note
 * that this class does not correspond to a particular parameter. The parameter
 * varies depending on the iCalendar version.
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5545#page-25">RFC 5545 p.25-6</a>
 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.26-7</a>
 */
public class ParticipationLevel {
	private static final CaseClasses<ParticipationLevel, String> enums = new CaseClasses<ParticipationLevel, String>(ParticipationLevel.class) {
		@Override
		protected ParticipationLevel create(String value) {
			return new ParticipationLevel(value);
		}

		@Override
		protected boolean matches(ParticipationLevel object, String value) {
			for (String v : object.values.values()) {
				if (v.equalsIgnoreCase(value)) {
					return true;
				}
			}
			return false;
		}
	};

	/**
	 * Indicates that the user's participation is required.
	 */
	public static final ParticipationLevel REQUIRED;
	static {
		Map<ICalVersion, String> values = new HashMap<ICalVersion, String>();
		values.put(ICalVersion.V1_0, "REQUIRE");
		values.put(ICalVersion.V2_0_DEPRECATED, "REQ-PARTICIPANT");
		values.put(ICalVersion.V2_0, values.get(ICalVersion.V2_0_DEPRECATED));
		REQUIRED = new ParticipationLevel(values);
	}

	/**
	 * Indicates that the user's participation is optional.
	 */
	public static final ParticipationLevel OPTIONAL;
	static {
		Map<ICalVersion, String> values = new HashMap<ICalVersion, String>();
		values.put(ICalVersion.V1_0, "REQUEST");
		values.put(ICalVersion.V2_0_DEPRECATED, "OPT-PARTICIPANT");
		values.put(ICalVersion.V2_0, values.get(ICalVersion.V2_0_DEPRECATED));
		OPTIONAL = new ParticipationLevel(values);
	}

	/**
	 * Indicates that the user has been notified about the event for
	 * informational purposes only and does not need to attend.
	 */
	public static final ParticipationLevel FYI;
	static {
		Map<ICalVersion, String> values = new HashMap<ICalVersion, String>();
		values.put(ICalVersion.V1_0, "FYI");
		values.put(ICalVersion.V2_0_DEPRECATED, "NON-PARTICIPANT");
		values.put(ICalVersion.V2_0, values.get(ICalVersion.V2_0_DEPRECATED));
		FYI = new ParticipationLevel(values);
	}

	private final Map<ICalVersion, String> values;

	private ParticipationLevel(Map<ICalVersion, String> values) {
		this.values = Collections.unmodifiableMap(values);
	}

	private ParticipationLevel(String value) {
		Map<ICalVersion, String> values = new HashMap<ICalVersion, String>();
		for (ICalVersion version : ICalVersion.values()) {
			values.put(version, value);
		}
		this.values = Collections.unmodifiableMap(values);
	}

	/**
	 * Gets the value of the parameter
	 * @param version the version
	 * @return the parameter value
	 */
	public String getValue(ICalVersion version) {
		return values.get(version);
	}

	@Override
	public String toString() {
		return getValue(ICalVersion.V2_0);
	}

	/**
	 * Searches for a parameter value that is defined as a static constant in
	 * this class.
	 * @param value the parameter value
	 * @return the object or null if not found
	 */
	public static ParticipationLevel find(String value) {
		return enums.find(value);
	}

	/**
	 * Searches for a parameter value and creates one if it cannot be found. All
	 * objects are guaranteed to be unique, so they can be compared with
	 * {@code ==} equality.
	 * @param value the parameter value
	 * @return the object
	 */
	public static ParticipationLevel get(String value) {
		return enums.get(value);
	}

	/**
	 * Gets all of the parameter values that are defined as static constants in
	 * this class.
	 * @return the parameter values
	 */
	public static Collection<ParticipationLevel> all() {
		return enums.all();
	}
}
