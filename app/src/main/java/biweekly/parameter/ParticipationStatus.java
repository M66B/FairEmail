package biweekly.parameter;

import java.util.Collection;

import biweekly.ICalVersion;
import biweekly.component.VEvent;
import biweekly.component.VJournal;
import biweekly.component.VTodo;
import biweekly.property.Attendee;

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
 * Defines a calendar user's level of participation. Used with the
 * {@link Attendee} property.
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5545#page-22">RFC 5545 p.22-3</a>
 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.25-6</a>
 */
public class ParticipationStatus extends VersionedEnumParameterValue {
	private static final ICalParameterCaseClasses<ParticipationStatus> enums = new ICalParameterCaseClasses<ParticipationStatus>(ParticipationStatus.class);

	/**
	 * Indicates that the user needs to make a decision about the item. Valid
	 * within the {@link VEvent}, {@link VTodo}, {@link VJournal} components.
	 */
	public static final ParticipationStatus NEEDS_ACTION = new ParticipationStatus("NEEDS-ACTION");

	/**
	 * Indicates that the user has accepted the item. Valid within the
	 * {@link VEvent}, {@link VTodo}, {@link VJournal} components.
	 */
	public static final ParticipationStatus ACCEPTED = new ParticipationStatus("ACCEPTED");

	/**
	 * Indicates that the user has declined the item. Valid within the
	 * {@link VEvent}, {@link VTodo}, {@link VJournal} components.
	 */
	public static final ParticipationStatus DECLINED = new ParticipationStatus("DECLINED");

	/**
	 * Indicates that the user has tentatively accepted the item. Valid within
	 * the {@link VEvent} and {@link VJournal} components.
	 */
	public static final ParticipationStatus TENTATIVE = new ParticipationStatus("TENTATIVE");

	/**
	 * Indicates that the user has delegated the item to someone else. Valid
	 * within the {@link VEvent} and {@link VTodo} components.
	 */
	public static final ParticipationStatus DELEGATED = new ParticipationStatus("DELEGATED");

	/**
	 * Indicates that the user has completed the item. Only valid within the
	 * {@link VTodo} component.
	 */
	public static final ParticipationStatus COMPLETED = new ParticipationStatus("COMPLETED");

	/**
	 * Indicates that the user is in the process of completing the item. Only
	 * valid within the {@link VTodo} component.
	 */
	public static final ParticipationStatus IN_PROCESS = new ParticipationStatus("IN_PROCESS", ICalVersion.V2_0_DEPRECATED, ICalVersion.V2_0);

	/**
	 * Indicates that the user confirmed attendance. Only valid within the
	 * {@link VEvent} component of vCalendar version 1.0.
	 */
	public static final ParticipationStatus CONFIRMED = new ParticipationStatus("CONFIRMED", ICalVersion.V1_0);

	/**
	 * Indicates that the item was sent out to the user. Valid within
	 * {@link VEvent} and {@link VTodo} components of vCalendar version 1.0.
	 */
	public static final ParticipationStatus SENT = new ParticipationStatus("SENT", ICalVersion.V1_0);

	private ParticipationStatus(String value, ICalVersion... supportedVersions) {
		super(value, supportedVersions);
	}

	/**
	 * Searches for a parameter value that is defined as a static constant in
	 * this class.
	 * @param value the parameter value
	 * @return the object or null if not found
	 */
	public static ParticipationStatus find(String value) {
		if ("NEEDS ACTION".equalsIgnoreCase(value)) { //vCal
			return NEEDS_ACTION;
		}
		return enums.find(value);
	}

	/**
	 * Searches for a parameter value and creates one if it cannot be found. All
	 * objects are guaranteed to be unique, so they can be compared with
	 * {@code ==} equality.
	 * @param value the parameter value
	 * @return the object
	 */
	public static ParticipationStatus get(String value) {
		if ("NEEDS ACTION".equalsIgnoreCase(value)) { //vCal
			return NEEDS_ACTION;
		}
		return enums.get(value);
	}

	/**
	 * Gets all of the parameter values that are defined as static constants in
	 * this class.
	 * @return the parameter values
	 */
	public static Collection<ParticipationStatus> all() {
		return enums.all();
	}
}
