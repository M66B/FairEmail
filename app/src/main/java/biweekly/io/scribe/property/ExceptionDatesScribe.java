package biweekly.io.scribe.property;

import static biweekly.ICalDataType.DATE;
import static biweekly.ICalDataType.DATE_TIME;

import java.util.ArrayList;
import java.util.List;

import biweekly.ICalDataType;
import biweekly.ICalVersion;
import biweekly.io.CannotParseException;
import biweekly.io.ParseContext;
import biweekly.io.WriteContext;
import biweekly.io.json.JCalValue;
import biweekly.io.xml.XCalElement;
import biweekly.parameter.ICalParameters;
import biweekly.property.ExceptionDates;
import biweekly.util.ICalDate;

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
 * Marshals {@link ExceptionDates} properties.
 * @author Michael Angstadt
 */
public class ExceptionDatesScribe extends ListPropertyScribe<ExceptionDates, ICalDate> {
	public ExceptionDatesScribe() {
		super(ExceptionDates.class, "EXDATE");
	}

	@Override
	protected ICalDataType _defaultDataType(ICalVersion version) {
		return DATE_TIME;
	}

	@Override
	protected ICalParameters _prepareParameters(ExceptionDates property, WriteContext context) {
		if (isInObservance(context)) {
			return property.getParameters();
		}

		boolean hasTime;
		if (property.getValues().isEmpty()) {
			hasTime = false;
		} else {
			hasTime = (dataType(property, context.getVersion()) == DATE_TIME);
		}
		return handleTzidParameter(property, hasTime, context);
	}

	@Override
	protected ICalDataType _dataType(ExceptionDates property, ICalVersion version) {
		List<ICalDate> dates = property.getValues();
		if (!dates.isEmpty()) {
			return dates.get(0).hasTime() ? DATE_TIME : DATE;
		}

		return defaultDataType(version);
	}

	@Override
	protected ExceptionDates newInstance(ICalDataType dataType, ICalParameters parameters) {
		return new ExceptionDates();
	}

	@Override
	protected String writeValue(ExceptionDates property, ICalDate value, WriteContext context) {
		if (isInObservance(context)) {
			return date(value).observance(true).extended(false).write();
		}

		return date(value, property, context).extended(false).write();
	}

	@Override
	protected ICalDate readValue(ExceptionDates property, String value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
		ICalDate date;
		try {
			boolean hasTime = (dataType == DATE_TIME);
			date = date(value).hasTime(hasTime).parse();
		} catch (IllegalArgumentException e) {
			throw new CannotParseException(19);
		}
		context.addDate(date, property, parameters);

		return date;
	}

	@Override
	protected void _writeXml(ExceptionDates property, XCalElement element, WriteContext context) {
		List<ICalDate> values = property.getValues();
		if (values.isEmpty()) {
			element.append(defaultDataType(context.getVersion()), "");
			return;
		}

		if (isInObservance(context)) {
			for (ICalDate value : values) {
				String valueStr = date(value).observance(true).extended(true).write();
				element.append(DATE_TIME, valueStr);
			}
			return;
		}

		for (ICalDate value : values) {
			ICalDataType dataType = value.hasTime() ? DATE_TIME : DATE;
			String dateStr = date(value, property, context).extended(true).write();
			element.append(dataType, dateStr);
		}
	}

	@Override
	protected ExceptionDates _parseXml(XCalElement element, ICalParameters parameters, ParseContext context) {
		List<String> dateTimeElements = element.all(DATE_TIME);
		List<String> dateElements = element.all(DATE);
		if (dateTimeElements.isEmpty() && dateElements.isEmpty()) {
			throw missingXmlElements(DATE_TIME, DATE);
		}

		ExceptionDates property = new ExceptionDates();
		List<ICalDate> values = property.getValues();
		for (String value : dateTimeElements) {
			ICalDate datetime = readValue(property, value, DATE_TIME, parameters, context);
			values.add(datetime);
		}
		for (String value : dateElements) {
			ICalDate date = readValue(property, value, DATE, parameters, context);
			values.add(date);
		}
		return property;
	}

	@Override
	protected JCalValue _writeJson(ExceptionDates property, WriteContext context) {
		List<ICalDate> values = property.getValues();
		if (values.isEmpty()) {
			return JCalValue.single("");
		}

		List<String> valuesStr = new ArrayList<String>();
		if (isInObservance(context)) {
			for (ICalDate value : values) {
				String valueStr = date(value).observance(true).extended(true).write();
				valuesStr.add(valueStr);
			}
			return JCalValue.multi(valuesStr);
		}

		for (ICalDate value : values) {
			String dateStr = date(value, property, context).extended(true).write();
			valuesStr.add(dateStr);
		}
		return JCalValue.multi(valuesStr);
	}

	@Override
	protected ExceptionDates _parseJson(JCalValue value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
		List<String> valueStrs = value.asMulti();

		ExceptionDates property = new ExceptionDates();
		List<ICalDate> values = property.getValues();
		for (String valueStr : valueStrs) {
			ICalDate date = readValue(property, valueStr, dataType, parameters, context);
			values.add(date);
		}
		return property;
	}
}
