package biweekly.io.scribe.property;

import biweekly.ICalDataType;
import biweekly.io.WriteContext;
import biweekly.io.json.JCalValue;
import biweekly.io.xml.XCalElement;
import biweekly.parameter.ICalParameters;
import biweekly.property.DateStart;
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
 * Marshals {@link DateStart} properties.
 * @author Michael Angstadt
 */
public class DateStartScribe extends DateOrDateTimePropertyScribe<DateStart> {
	public DateStartScribe() {
		super(DateStart.class, "DTSTART");
	}

	@Override
	protected ICalParameters _prepareParameters(DateStart property, WriteContext context) {
		if (isInObservance(context)) {
			return property.getParameters();
		}
		return super._prepareParameters(property, context);
	}

	@Override
	protected String _writeText(DateStart property, WriteContext context) {
		if (isInObservance(context)) {
			return write(property, false);
		}
		return super._writeText(property, context);
	}

	@Override
	protected void _writeXml(DateStart property, XCalElement element, WriteContext context) {
		if (isInObservance(context)) {
			String dateStr = write(property, true);
			ICalDataType dataType = dataType(property, null);
			element.append(dataType, dateStr);
			return;
		}

		super._writeXml(property, element, context);
	}

	@Override
	protected JCalValue _writeJson(DateStart property, WriteContext context) {
		if (isInObservance(context)) {
			return JCalValue.single(write(property, true));
		}
		return super._writeJson(property, context);
	}

	private String write(DateStart property, boolean extended) {
		ICalDate value = property.getValue();
		return date(value).observance(true).extended(extended).write();
	}

	@Override
	protected DateStart newInstance(ICalDate date) {
		return new DateStart(date);
	}
}
