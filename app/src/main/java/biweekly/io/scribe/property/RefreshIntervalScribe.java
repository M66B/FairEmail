package biweekly.io.scribe.property;

import biweekly.ICalDataType;
import biweekly.io.CannotParseException;
import biweekly.io.ParseContext;
import biweekly.io.WriteContext;
import biweekly.io.json.JCalValue;
import biweekly.io.xml.XCalElement;
import biweekly.parameter.ICalParameters;
import biweekly.property.RefreshInterval;
import biweekly.util.Duration;

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
 * Marshals {@link RefreshInterval} properties.
 * @author Michael Angstadt
 */
public class RefreshIntervalScribe extends ICalPropertyScribe<RefreshInterval> {
	public RefreshIntervalScribe() {
		super(RefreshInterval.class, "REFRESH-INTERVAL", ICalDataType.DURATION);
	}

	@Override
	protected String _writeText(RefreshInterval property, WriteContext context) {
		Duration duration = property.getValue();
		if (duration != null) {
			return duration.toString();
		}

		return "";
	}

	@Override
	protected RefreshInterval _parseText(String value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
		value = VObjectPropertyValues.unescape(value);
		return parse(value);
	}

	@Override
	protected void _writeXml(RefreshInterval property, XCalElement element, WriteContext context) {
		String durationStr = null;

		Duration duration = property.getValue();
		if (duration != null) {
			durationStr = duration.toString();
		}

		element.append(dataType(property, null), durationStr);
	}

	@Override
	protected RefreshInterval _parseXml(XCalElement element, ICalParameters parameters, ParseContext context) {
		ICalDataType dataType = defaultDataType(context.getVersion());
		String value = element.first(dataType);
		if (value != null) {
			return parse(value);
		}

		throw missingXmlElements(dataType);
	}

	@Override
	protected JCalValue _writeJson(RefreshInterval property, WriteContext context) {
		Duration value = property.getValue();
		if (value != null) {
			return JCalValue.single(value.toString());
		}

		return JCalValue.single("");
	}

	@Override
	protected RefreshInterval _parseJson(JCalValue value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
		String valueStr = value.asSingle();
		return parse(valueStr);
	}

	private RefreshInterval parse(String value) {
		if (value == null) {
			return new RefreshInterval((Duration) null);
		}

		try {
			Duration duration = Duration.parse(value);
			return new RefreshInterval(duration);
		} catch (IllegalArgumentException e) {
			throw new CannotParseException(18);
		}
	}
}