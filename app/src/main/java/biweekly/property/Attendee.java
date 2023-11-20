package biweekly.property;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import biweekly.ICalVersion;
import biweekly.ValidationWarning;
import biweekly.component.ICalComponent;
import biweekly.component.VAlarm;
import biweekly.parameter.CalendarUserType;
import biweekly.parameter.ParticipationLevel;
import biweekly.parameter.ParticipationStatus;
import biweekly.parameter.Role;

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
 * Defines an attendee (such as a person attending an event). This property has
 * different meanings depending on the component that it belongs to:
 * </p>
 * <ul>
 * <li>{@link VAlarm} (with "EMAIL" action) - An email address that is to
 * receive the alarm.</li>
 * <li>All others - An attendee of the calendar entity.</li>
 * </ul>
 * 
 * <p>
 * <b>Code sample:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VEvent event = new VEvent();
 * 
 * Attendee attendee = Attendee.email("johndoe@example.com")
 * attendee.setCommonName("John Doe");
 * attendee.setRsvp(true);
 * attendee.setRole(Role.CHAIR);
 * attendee.setParticipationStatus(ParticipationStatus.ACCEPTED);
 * event.addAttendee(attendee);
 * </pre>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5545#page-107">RFC 5545
 * p.107-9</a>
 * @see <a href="http://tools.ietf.org/html/rfc2445#page-102">RFC 2445
 * p.102-4</a>
 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.25-7</a>
 */
public class Attendee extends ICalProperty {
	private String name, email, uri;
	private Role role;
	private ParticipationLevel participationLevel;
	private ParticipationStatus status;
	private Boolean rsvp;

	/**
	 * Creates an attendee property.
	 * @param name the attendee's name (e.g. "John Doe")
	 * @param email the attendee's email (e.g. "jdoe@example.com")
	 */
	public Attendee(String name, String email) {
		this(name, email, null);
	}

	/**
	 * Creates an attendee property.
	 * @param name the attendee's name (e.g. "John Doe")
	 * @param email the attendee's email (e.g. "jdoe@example.com")
	 * @param uri a URI representing the attendee
	 */
	public Attendee(String name, String email, String uri) {
		this.name = name;
		this.email = email;
		this.uri = uri;
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public Attendee(Attendee original) {
		super(original);
		name = original.name;
		email = original.email;
		uri = original.uri;
		role = original.role;
		participationLevel = original.participationLevel;
		status = original.status;
		rsvp = original.rsvp;
	}

	/**
	 * Gets the attendee's email
	 * @return the email (e.g. "jdoe@company.com")
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Sets the attendee's email
	 * @param email the email (e.g. "jdoe@company.com")
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * Gets a URI representing the attendee.
	 * @return the URI (e.g. "mailto:jdoe@company.com")
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * Sets a URI representing the attendee.
	 * @param uri the URI (e.g. "mailto:jdoe@company.com")
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}

	/**
	 * <p>
	 * Gets the type of user the attendee is (for example, an "individual" or a
	 * "room").
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.0}
	 * </p>
	 * @return the calendar user type or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-16">RFC 5545
	 * p.16</a>
	 */
	public CalendarUserType getCalendarUserType() {
		return parameters.getCalendarUserType();
	}

	/**
	 * <p>
	 * Sets the type of user the attendee is (for example, an "individual" or a
	 * "room").
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.0}
	 * </p>
	 * @param cutype the calendar user type or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-16">RFC 5545
	 * p.16</a>
	 */
	public void setCalendarUserType(CalendarUserType cutype) {
		parameters.setCalendarUserType(cutype);
	}

	/**
	 * <p>
	 * Gets the list that holds the groups that the attendee is a member of.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.0}
	 * </p>
	 * @return the groups (this list is mutable). Typically, these are email
	 * address URIs (e.g. "mailto:mailinglist@example.com")
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-21">RFC 5545
	 * p.21-2</a>
	 */
	public List<String> getMemberOf() {
		return parameters.getMembers();
	}

	/**
	 * Gets an attendee's role (for example, "chair" or "attendee").
	 * @return the role or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-25">RFC 5545
	 * p.25-6</a>
	 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.25</a>
	 */
	public Role getRole() {
		return role;
	}

	/**
	 * Sets an attendee's role (for example, "chair" or "attendee").
	 * @param role the role or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-25">RFC 5545
	 * p.25-6</a>
	 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.25</a>
	 */
	public void setRole(Role role) {
		this.role = role;
	}

	/**
	 * Gets an attendee's level of participation.
	 * @return the participation level or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-25">RFC 5545
	 * p.25-6</a>
	 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.26-7</a>
	 */
	public ParticipationLevel getParticipationLevel() {
		return participationLevel;
	}

	/**
	 * Sets an attendee's level of participation.
	 * @param level the participation level or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-25">RFC 5545
	 * p.25-6</a>
	 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.26-7</a>
	 */
	public void setParticipationLevel(ParticipationLevel level) {
		this.participationLevel = level;
	}

	/**
	 * Gets an attendee's participation status
	 * @return the participation status or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-22">RFC 5545
	 * p.22-3</a>
	 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.25-6</a>
	 */
	public ParticipationStatus getParticipationStatus() {
		return status;
	}

	/**
	 * Sets an attendee's participation status.
	 * @param status the participation status or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-22">RFC 5545
	 * p.22-3</a>
	 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.25-6</a>
	 */
	public void setParticipationStatus(ParticipationStatus status) {
		this.status = status;
	}

	/**
	 * Gets whether the organizer requests a response from the attendee.
	 * @return true if an RSVP is requested, false if not, null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-26">RFC 5545
	 * p.26-7</a>
	 */
	public Boolean getRsvp() {
		return rsvp;
	}

	/**
	 * Sets whether the organizer requests a response from the attendee.
	 * @param rsvp true if an RSVP has been requested, false if not, null to
	 * remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-26">RFC 5545
	 * p.26-7</a>
	 */
	public void setRsvp(Boolean rsvp) {
		this.rsvp = rsvp;
	}

	/**
	 * <p>
	 * Gets the list containing the people who have delegated their
	 * responsibility to the attendee.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.0}
	 * </p>
	 * @return the delegators (this list is mutable). Typically, these are email
	 * URIs (e.g. "mailto:janedoe@example.com").
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-17">RFC 5545
	 * p.17</a>
	 */
	public List<String> getDelegatedFrom() {
		return parameters.getDelegatedFrom();
	}

	/**
	 * <p>
	 * Gets the list containing the people to which the attendee has delegated
	 * his or her responsibility.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.0}
	 * </p>
	 * @return the delegatees (this list is mutable). Typically, these are email
	 * URIs (e.g. "mailto:janedoe@example.com").
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-17">RFC 5545
	 * p.17-8</a>
	 */
	public List<String> getDelegatedTo() {
		return parameters.getDelegatedTo();
	}

	@Override
	public String getSentBy() {
		return super.getSentBy();
	}

	@Override
	public void setSentBy(String uri) {
		super.setSentBy(uri);
	}

	@Override
	public String getCommonName() {
		return name;
	}

	@Override
	public void setCommonName(String commonName) {
		this.name = commonName;
	}

	@Override
	public String getDirectoryEntry() {
		return super.getDirectoryEntry();
	}

	@Override
	public void setDirectoryEntry(String uri) {
		super.setDirectoryEntry(uri);
	}

	/**
	 * Gets the language that the common name is written in.
	 */
	@Override
	public String getLanguage() {
		return super.getLanguage();
	}

	/**
	 * Sets the language that the common name is written in.
	 */
	@Override
	public void setLanguage(String language) {
		super.setLanguage(language);
	}

	@Override
	protected void validate(List<ICalComponent> components, ICalVersion version, List<ValidationWarning> warnings) {
		//TODO
	}

	@Override
	protected Map<String, Object> toStringValues() {
		Map<String, Object> values = new LinkedHashMap<String, Object>();
		values.put("name", name);
		values.put("email", email);
		values.put("uri", uri);
		values.put("role", role);
		values.put("participationLevel", participationLevel);
		values.put("status", status);
		values.put("rsvp", rsvp);
		return values;
	}

	@Override
	public Attendee copy() {
		return new Attendee(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((participationLevel == null) ? 0 : participationLevel.hashCode());
		result = prime * result + ((role == null) ? 0 : role.hashCode());
		result = prime * result + ((rsvp == null) ? 0 : rsvp.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		Attendee other = (Attendee) obj;
		if (email == null) {
			if (other.email != null) return false;
		} else if (!email.equals(other.email)) return false;
		if (name == null) {
			if (other.name != null) return false;
		} else if (!name.equals(other.name)) return false;
		if (participationLevel != other.participationLevel) return false;
		if (role != other.role) return false;
		if (rsvp == null) {
			if (other.rsvp != null) return false;
		} else if (!rsvp.equals(other.rsvp)) return false;
		if (status != other.status) return false;
		if (uri == null) {
			if (other.uri != null) return false;
		} else if (!uri.equals(other.uri)) return false;
		return true;
	}
}
