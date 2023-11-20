package biweekly.io.scribe.property;

import java.util.Date;

import biweekly.ICalDataType;
import biweekly.io.CannotParseException;
import biweekly.io.ParseContext;
import biweekly.io.WriteContext;
import biweekly.io.json.JCalValue;
import biweekly.io.xml.XCalElement;
import biweekly.parameter.ICalParameters;
import biweekly.property.DateTimeProperty;
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
 * Marshals properties that have "date-time" values. These values will always be
 * formatted in the UTC timezone.
 * @param <T> the property class
 * @author Michael Angstadt
 */
public abstract class DateTimePropertyScribe<T extends DateTimeProperty> extends ICalPropertyScribe<T> {
	public DateTimePropertyScribe(Class<T> clazz, String propertyName) {
		super(clazz, propertyName, ICalDataType.DATE_TIME);
	}

	@Override
	protected String _writeText(T property, WriteContext context) {
		Date value = property.getValue();
		return date(value).utc(true).extended(false).write();
	}

	@Override
	protected T _parseText(String value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
		value = VObjectPropertyValues.unescape(value);
		return parse(value, parameters, context);
	}

	@Override
	protected void _writeXml(T property, XCalElement element, WriteContext context) {
		ICalDataType dataType = dataType(property, null);
		Date value = property.getValue();
		String dateStr = date(value).utc(true).extended(true).write();

		element.append(dataType, dateStr);
	}

	@Override
	protected T _parseXml(XCalElement element, ICalParameters parameters, ParseContext context) {
		ICalDataType dataType = defaultDataType(context.getVersion());
		String value = element.first(dataType);
		if (value != null) {
			return parse(value, parameters, context);
		}

		throw missingXmlElements(dataType);
	}

	@Override
	protected JCalValue _writeJson(T property, WriteContext context) {
		Date value = property.getValue();
		String dateStr = date(value).utc(true).extended(true).write();
		return JCalValue.single(dateStr);
	}

	@Override
	protected T _parseJson(JCalValue value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
		String valueStr = value.asSingle();
		return parse(valueStr, parameters, context);
	}

	private T parse(String value, ICalParameters parameters, ParseContext context) {
		ICalDate date;
		try {
			date = date(value).parse();
		} catch (IllegalArgumentException e) {
			throw new CannotParseException(17);
		}

		T property = newInstance(date);
		context.addDate(date, property, parameters);
		return property;
	}

	protected abstract T newInstance(Date date);
}
