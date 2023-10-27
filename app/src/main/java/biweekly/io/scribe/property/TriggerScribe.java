package biweekly.io.scribe.property;

import java.util.Date;
import java.util.EnumSet;
import java.util.Set;

import biweekly.ICalDataType;
import biweekly.ICalVersion;
import biweekly.io.CannotParseException;
import biweekly.io.ParseContext;
import biweekly.io.WriteContext;
import biweekly.io.json.JCalValue;
import biweekly.io.xml.XCalElement;
import biweekly.parameter.ICalParameters;
import biweekly.property.Trigger;
import biweekly.util.Duration;
import biweekly.util.ICalDate;

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
 * Marshals {@link Trigger} properties.
 * @author Michael Angstadt
 */
public class TriggerScribe extends ICalPropertyScribe<Trigger> {
	public TriggerScribe() {
		super(Trigger.class, "TRIGGER", ICalDataType.DURATION);
	}

	@Override
	protected ICalDataType _dataType(Trigger property, ICalVersion version) {
		return (property.getDate() == null) ? ICalDataType.DURATION : ICalDataType.DATE_TIME;
	}

	@Override
	protected String _writeText(Trigger property, WriteContext context) {
		Duration duration = property.getDuration();
		if (duration != null) {
			return duration.toString();
		}

		Date date = property.getDate();
		return date(date, property, context).extended(false).write();
	}

	@Override
	protected Trigger _parseText(String value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
		value = VObjectPropertyValues.unescape(value);

		try {
			ICalDate date = date(value).parse();
			Trigger property = new Trigger(date);
			context.addDate(date, property, parameters);
			return property;
		} catch (IllegalArgumentException e) {
			//unable to parse value as date, must be a duration
		}

		try {
			return new Trigger(Duration.parse(value), parameters.getRelated());
		} catch (IllegalArgumentException e) {
			//unable to parse duration
		}

		throw new CannotParseException(25);
	}

	@Override
	protected void _writeXml(Trigger property, XCalElement element, WriteContext context) {
		Duration duration = property.getDuration();
		if (duration != null) {
			element.append(ICalDataType.DURATION, duration.toString());
			return;
		}

		Date date = property.getDate();
		if (date != null) {
			String dateStr = date(date, property, context).extended(true).write();
			element.append(ICalDataType.DATE_TIME, dateStr);
			return;
		}

		element.append(defaultDataType(context.getVersion()), "");
	}

	@Override
	protected Trigger _parseXml(XCalElement element, ICalParameters parameters, ParseContext context) {
		String value = element.first(ICalDataType.DURATION);
		if (value != null) {
			try {
				return new Trigger(Duration.parse(value), parameters.getRelated());
			} catch (IllegalArgumentException e) {
				throw new CannotParseException(26, value);
			}
		}

		value = element.first(ICalDataType.DATE_TIME);
		if (value != null) {
			try {
				ICalDate date = date(value).parse();
				Trigger property = new Trigger(date);
				context.addDate(date, property, parameters);
				return property;
			} catch (IllegalArgumentException e) {
				throw new CannotParseException(27, value);
			}
		}

		throw missingXmlElements(ICalDataType.DURATION, ICalDataType.DATE_TIME);
	}

	@Override
	protected JCalValue _writeJson(Trigger property, WriteContext context) {
		Duration duration = property.getDuration();
		if (duration != null) {
			return JCalValue.single(duration.toString());
		}

		Date date = property.getDate();
		if (date != null) {
			String dateStr = date(date, property, context).extended(true).write();
			return JCalValue.single(dateStr);
		}

		return JCalValue.single("");
	}

	@Override
	protected Trigger _parseJson(JCalValue value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
		String valueStr = value.asSingle();

		try {
			ICalDate date = date(valueStr).parse();
			Trigger property = new Trigger(date);
			context.addDate(date, property, parameters);
			return property;
		} catch (IllegalArgumentException e) {
			//must be a duration
		}

		try {
			return new Trigger(Duration.parse(valueStr), parameters.getRelated());
		} catch (IllegalArgumentException e) {
			throw new CannotParseException(25);
		}
	}

	@Override
	public Set<ICalVersion> getSupportedVersions() {
		return EnumSet.of(ICalVersion.V2_0_DEPRECATED, ICalVersion.V2_0);
	}
}
