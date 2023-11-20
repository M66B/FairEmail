package biweekly.io.scribe.property;

import java.util.EnumSet;
import java.util.Set;

import biweekly.ICalDataType;
import biweekly.ICalVersion;
import biweekly.io.DataModelConversionException;
import biweekly.io.ParseContext;
import biweekly.io.WriteContext;
import biweekly.parameter.ICalParameters;
import biweekly.parameter.Role;
import biweekly.property.Attendee;
import biweekly.property.Organizer;

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
 * Marshals {@link Organizer} properties.
 * @author Michael Angstadt
 */
public class OrganizerScribe extends ICalPropertyScribe<Organizer> {
	public OrganizerScribe() {
		super(Organizer.class, "ORGANIZER", ICalDataType.CAL_ADDRESS);
	}

	@Override
	protected ICalParameters _prepareParameters(Organizer property, WriteContext context) {
		//CN parameter
		String name = property.getCommonName();
		if (name != null) {
			ICalParameters copy = new ICalParameters(property.getParameters());
			copy.put(ICalParameters.CN, name);
			return copy;
		}

		return super._prepareParameters(property, context);
	}

	@Override
	protected Organizer _parseText(String value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
		String name = parameters.getCommonName();
		if (name != null) {
			parameters.remove(ICalParameters.CN, name);
		}

		String uri = null, email = null;
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

		Organizer organizer = new Organizer(name, email);
		organizer.setUri(uri);
		return organizer;
	}

	@Override
	protected String _writeText(Organizer property, WriteContext context) {
		if (context.getVersion() == ICalVersion.V1_0) {
			Attendee attendee = new Attendee(property.getCommonName(), property.getEmail());
			attendee.setRole(Role.ORGANIZER);
			attendee.setUri(property.getUri());
			attendee.setParameters(property.getParameters());

			DataModelConversionException conversionException = new DataModelConversionException(property);
			conversionException.getProperties().add(attendee);
			throw conversionException;
		}

		String uri = property.getUri();
		if (uri != null) {
			return uri;
		}

		String email = property.getEmail();
		if (email != null) {
			return "mailto:" + email;
		}

		return "";
	}

	@Override
	public Set<ICalVersion> getSupportedVersions() {
		return EnumSet.of(ICalVersion.V2_0_DEPRECATED, ICalVersion.V2_0);
	}
}