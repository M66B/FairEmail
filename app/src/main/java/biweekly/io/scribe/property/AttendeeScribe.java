package biweekly.io.scribe.property;

import java.util.Iterator;

import biweekly.ICalDataType;
import biweekly.ICalVersion;
import biweekly.io.DataModelConversionException;
import biweekly.io.ParseContext;
import biweekly.io.WriteContext;
import biweekly.parameter.ICalParameters;
import biweekly.parameter.ParticipationLevel;
import biweekly.parameter.ParticipationStatus;
import biweekly.parameter.Role;
import biweekly.property.Attendee;
import biweekly.property.Organizer;

import com.github.mangstadt.vinnie.io.VObjectPropertyValues;

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
 * Marshals {@link Attendee} properties.
 * @author Michael Angstadt
 */
public class AttendeeScribe extends ICalPropertyScribe<Attendee> {
	public AttendeeScribe() {
		super(Attendee.class, "ATTENDEE");
	}

	@Override
	protected ICalDataType _defaultDataType(ICalVersion version) {
		switch (version) {
		case V1_0:
			return null;
		default:
			return ICalDataType.CAL_ADDRESS;
		}
	}

	@Override
	protected ICalDataType _dataType(Attendee property, ICalVersion version) {
		if (version == ICalVersion.V1_0 && property.getUri() != null) {
			return ICalDataType.URL;
		}
		return defaultDataType(version);
	}

	@Override
	protected ICalParameters _prepareParameters(Attendee property, WriteContext context) {
		/*
		 * Note: Parameter values are assigned using "put()" instead of the
		 * appropriate "setter" methods so that any existing parameter values
		 * are not overwritten.
		 */

		ICalParameters copy = new ICalParameters(property.getParameters());

		//RSVP parameter
		//1.0 - Uses the values "YES" and "NO"
		//2.0 - Uses the values "TRUE" and "FALSE"
		Boolean rsvp = property.getRsvp();
		if (rsvp != null) {
			String value;
			switch (context.getVersion()) {
			case V1_0:
				value = rsvp ? "YES" : "NO";
				break;

			default:
				value = rsvp ? "TRUE" : "FALSE";
				break;
			}

			copy.put(ICalParameters.RSVP, value);
		}

		//ROLE and EXPECT parameters
		//1.0 - Uses ROLE and EXPECT
		//2.0 - Uses only ROLE
		Role role = property.getRole();
		ParticipationLevel level = property.getParticipationLevel();
		switch (context.getVersion()) {
		case V1_0:
			if (role != null) {
				copy.put(ICalParameters.ROLE, role.getValue());
			}
			if (level != null) {
				copy.put(ICalParameters.EXPECT, level.getValue(context.getVersion()));
			}
			break;

		default:
			String value = null;
			if (role == Role.CHAIR) {
				value = role.getValue();
			} else if (level != null) {
				value = level.getValue(context.getVersion());
			} else if (role != null) {
				value = role.getValue();
			}

			if (value != null) {
				copy.put(ICalParameters.ROLE, value);
			}
			break;
		}

		//PARTSTAT vs STATUS
		//1.0 - Calls the parameter "STATUS"
		//2.0 - Calls the parameter "PARTSTAT"
		ParticipationStatus partStat = property.getParticipationStatus();
		if (partStat != null) {
			String paramName;
			String paramValue;

			switch (context.getVersion()) {
			case V1_0:
				paramName = ICalParameters.STATUS;
				paramValue = (partStat == ParticipationStatus.NEEDS_ACTION) ? "NEEDS ACTION" : partStat.getValue();
				break;

			default:
				paramName = ICalParameters.PARTSTAT;
				paramValue = partStat.getValue();
				break;
			}

			copy.put(paramName, paramValue);
		}

		//CN parameter
		String name = property.getCommonName();
		if (name != null && context.getVersion() != ICalVersion.V1_0) {
			copy.put(ICalParameters.CN, name);
		}

		//EMAIL parameter
		String uri = property.getUri();
		String email = property.getEmail();
		if (uri != null && email != null && context.getVersion() != ICalVersion.V1_0) {
			copy.put(ICalParameters.EMAIL, email);
		}

		return copy;
	}

	@Override
	protected Attendee _parseText(String value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
		String uri = null, name = null, email = null;
		Boolean rsvp = null;
		Role role = null;
		ParticipationLevel participationLevel = null;
		ParticipationStatus participationStatus = null;

		switch (context.getVersion()) {
		case V1_0:
			Iterator<String> it = parameters.get(ICalParameters.RSVP).iterator();
			while (it.hasNext()) {
				String rsvpStr = it.next();

				if ("YES".equalsIgnoreCase(rsvpStr)) {
					rsvp = Boolean.TRUE;
					it.remove();
					break;
				}

				if ("NO".equalsIgnoreCase(rsvpStr)) {
					rsvp = Boolean.FALSE;
					it.remove();
					break;
				}
			}

			String roleStr = parameters.first(ICalParameters.ROLE);
			if (roleStr != null) {
				role = Role.get(roleStr);
				parameters.remove(ICalParameters.ROLE, roleStr);
			}

			String expectStr = parameters.getExpect();
			if (expectStr != null) {
				participationLevel = ParticipationLevel.get(expectStr);
				parameters.remove(ICalParameters.EXPECT, expectStr);
			}

			String statusStr = parameters.getStatus();
			if (statusStr != null) {
				participationStatus = ParticipationStatus.get(statusStr);
				parameters.remove(ICalParameters.STATUS, statusStr);
			}

			int bracketStart = value.lastIndexOf('<');
			int bracketEnd = value.lastIndexOf('>');
			if (bracketStart >= 0 && bracketEnd >= 0 && bracketStart < bracketEnd) {
				name = value.substring(0, bracketStart).trim();
				email = value.substring(bracketStart + 1, bracketEnd).trim();
			} else if (dataType == ICalDataType.URL) {
				uri = value;
			} else {
				email = value;
			}

			break;

		default:
			it = parameters.get(ICalParameters.RSVP).iterator();
			while (it.hasNext()) {
				String rsvpStr = it.next();

				if ("TRUE".equalsIgnoreCase(rsvpStr)) {
					rsvp = Boolean.TRUE;
					it.remove();
					break;
				}

				if ("FALSE".equalsIgnoreCase(rsvpStr)) {
					rsvp = Boolean.FALSE;
					it.remove();
					break;
				}
			}

			roleStr = parameters.first(ICalParameters.ROLE);
			if (roleStr != null) {
				if (roleStr.equalsIgnoreCase(Role.CHAIR.getValue())) {
					role = Role.CHAIR;
				} else {
					ParticipationLevel l = ParticipationLevel.find(roleStr);
					if (l == null) {
						role = Role.get(roleStr);
					} else {
						participationLevel = l;
					}
				}
				parameters.remove(ICalParameters.ROLE, roleStr);
			}

			String participationStatusStr = parameters.getParticipationStatus();
			if (participationStatusStr != null) {
				participationStatus = ParticipationStatus.get(participationStatusStr);
				parameters.remove(ICalParameters.PARTSTAT, participationStatusStr);
			}

			name = parameters.getCommonName();
			if (name != null) {
				parameters.remove(ICalParameters.CN, name);
			}

			email = parameters.getEmail();
			if (email == null) {
				int colon = value.indexOf(':');
				if (colon == 6) {
					String scheme = value.substring(0, colon);
					if (scheme.equalsIgnoreCase("mailto")) {
						email = value.substring(colon + 1);
					} else {
						uri = value;
					}
				} else {
					uri = value;
				}
			} else {
				uri = value;
				parameters.remove(ICalParameters.EMAIL, email);
			}

			break;
		}

		Attendee attendee = new Attendee(name, email, uri);
		attendee.setParticipationStatus(participationStatus);
		attendee.setParticipationLevel(participationLevel);
		attendee.setRole(role);
		attendee.setRsvp(rsvp);

		if (context.getVersion() == ICalVersion.V1_0 && attendee.getRole() == Role.ORGANIZER) {
			Organizer organizer = new Organizer(attendee.getCommonName(), attendee.getEmail());
			organizer.setUri(attendee.getUri());
			organizer.setParameters(parameters);

			attendee.setParameters(parameters);
			DataModelConversionException conversionException = new DataModelConversionException(attendee);
			conversionException.getProperties().add(organizer);
			throw conversionException;
		}

		return attendee;
	}

	@Override
	protected String _writeText(Attendee property, WriteContext context) {
		String uri = property.getUri();
		if (uri != null) {
			return uri;
		}

		String name = property.getCommonName();
		String email = property.getEmail();
		switch (context.getVersion()) {
		case V1_0:
			if (email != null) {
				String value = (name == null) ? email : name + " <" + email + ">";
				return VObjectPropertyValues.escape(value);
			}

			break;

		default:
			if (email != null) {
				return "mailto:" + email;
			}
			break;
		}

		return "";
	}
}
