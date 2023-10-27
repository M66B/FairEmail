package biweekly.io.scribe.property;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import biweekly.ICalDataType;
import biweekly.ICalVersion;
import biweekly.io.CannotParseException;
import biweekly.io.ParseContext;
import biweekly.io.WriteContext;
import biweekly.parameter.ICalParameters;
import biweekly.property.Daylight;
import biweekly.util.ICalDate;
import biweekly.util.UtcOffset;

import com.github.mangstadt.vinnie.io.VObjectPropertyValues;
import com.github.mangstadt.vinnie.io.VObjectPropertyValues.SemiStructuredValueIterator;

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
 * Marshals {@link Daylight} properties.
 * @author Michael Angstadt
 */
public class DaylightScribe extends ICalPropertyScribe<Daylight> {
	public DaylightScribe() {
		super(Daylight.class, "DAYLIGHT");
	}

	@Override
	protected String _writeText(Daylight property, WriteContext context) {
		if (!property.isDaylight()) {
			return "FALSE";
		}

		List<String> values = new ArrayList<String>();
		values.add("TRUE");

		UtcOffset offset = property.getOffset();
		values.add((offset == null) ? "" : offset.toString());

		ICalDate start = property.getStart();
		values.add((start == null || start.getRawComponents() == null) ? "" : start.getRawComponents().toString(true, false));

		ICalDate end = property.getEnd();
		values.add((end == null || end.getRawComponents() == null) ? "" : end.getRawComponents().toString(true, false));

		String standardName = property.getStandardName();
		values.add((standardName == null) ? "" : standardName);

		String daylightName = property.getDaylightName();
		values.add((daylightName == null) ? "" : daylightName);

		return VObjectPropertyValues.writeSemiStructured(values, false, true);
	}

	@Override
	protected Daylight _parseText(String value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
		SemiStructuredValueIterator it = new SemiStructuredValueIterator(value);

		String next = it.next();
		boolean flag = (next == null) ? false : Boolean.parseBoolean(next);

		UtcOffset offset = null;
		next = it.next();
		if (next != null) {
			try {
				offset = UtcOffset.parse(next);
			} catch (IllegalArgumentException e) {
				throw new CannotParseException(33, next);
			}
		}

		ICalDate start = null;
		next = it.next();
		if (next != null) {
			try {
				start = date(next).parse();
			} catch (IllegalArgumentException e) {
				throw new CannotParseException(34, next);
			}
		}

		ICalDate end = null;
		next = it.next();
		if (next != null) {
			try {
				end = date(next).parse();
			} catch (IllegalArgumentException e) {
				throw new CannotParseException(35, next);
			}
		}

		String standardName = it.next();
		String daylightName = it.next();

		return new Daylight(flag, offset, start, end, standardName, daylightName);
	}

	@Override
	public Set<ICalVersion> getSupportedVersions() {
		return EnumSet.of(ICalVersion.V1_0);
	}
}
