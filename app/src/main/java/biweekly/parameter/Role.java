package biweekly.parameter;

import java.util.Collection;

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
 * Defines the role that a calendar user holds.
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5545#page-25">RFC 5545 p.25-6</a>
 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.25</a>
 */
public class Role extends VersionedEnumParameterValue {
	private static final ICalParameterCaseClasses<Role> enums = new ICalParameterCaseClasses<Role>(Role.class);

	/**
	 * <p>
	 * Indicates that the user is the chair of the calendar entity.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.0}
	 * </p>
	 */
	public static final Role CHAIR = new Role("CHAIR", ICalVersion.V2_0_DEPRECATED, ICalVersion.V2_0);

	/**
	 * <p>
	 * Indicates that the user is an attendee of the calendar entity.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 1.0}
	 * </p>
	 */
	public static final Role ATTENDEE = new Role("ATTENDEE", ICalVersion.V1_0);

	/**
	 * <p>
	 * Indicates that the user is the organizer of the calendar entity.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 1.0}
	 * </p>
	 */
	public static final Role ORGANIZER = new Role("ORGANIZER", ICalVersion.V1_0);

	/**
	 * <p>
	 * Indicates that the user is the owner of the calendar entity.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 1.0}
	 * </p>
	 */
	public static final Role OWNER = new Role("OWNER", ICalVersion.V1_0);

	/**
	 * <p>
	 * Indicates that the user is a delegate of another attendee.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 1.0}
	 * </p>
	 */
	public static final Role DELEGATE = new Role("DELEGATE", ICalVersion.V1_0);

	private Role(String value, ICalVersion... versions) {
		super(value, versions);
	}

	/**
	 * Searches for a parameter value that is defined as a static constant in
	 * this class.
	 * @param value the parameter value
	 * @return the object or null if not found
	 */
	public static Role find(String value) {
		return enums.find(value);
	}

	/**
	 * Searches for a parameter value and creates one if it cannot be found. All
	 * objects are guaranteed to be unique, so they can be compared with
	 * {@code ==} equality.
	 * @param value the parameter value
	 * @return the object
	 */
	public static Role get(String value) {
		return enums.get(value);
	}

	/**
	 * Gets all of the parameter values that are defined as static constants in
	 * this class.
	 * @return the parameter values
	 */
	public static Collection<Role> all() {
		return enums.all();
	}
}
