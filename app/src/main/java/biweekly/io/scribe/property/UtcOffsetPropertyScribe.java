package biweekly.io.scribe.property;

import biweekly.ICalDataType;
import biweekly.io.CannotParseException;
import biweekly.io.ParseContext;
import biweekly.io.WriteContext;
import biweekly.io.json.JCalValue;
import biweekly.io.xml.XCalElement;
import biweekly.parameter.ICalParameters;
import biweekly.property.UtcOffsetProperty;
import biweekly.util.UtcOffset;

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
 * Marshals properties that have UTC offset values.
 * @param <T> the property class
 * @author Michael Angstadt
 */
public abstract class UtcOffsetPropertyScribe<T extends UtcOffsetProperty> extends ICalPropertyScribe<T> {
	public UtcOffsetPropertyScribe(Class<T> clazz, String propertyName) {
		super(clazz, propertyName, ICalDataType.UTC_OFFSET);
	}

	@Override
	protected String _writeText(T property, WriteContext context) {
		UtcOffset offset = property.getValue();
		if (offset != null) {
			return offset.toString(false);
		}

		return "";
	}

	@Override
	protected T _parseText(String value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
		value = VObjectPropertyValues.unescape(value);
		return parse(value);
	}

	@Override
	protected void _writeXml(T property, XCalElement element, WriteContext context) {
		String offsetStr = null;

		UtcOffset offset = property.getValue();
		if (offset != null) {
			offsetStr = offset.toString(true);
		}

		element.append(dataType(property, null), offsetStr);
	}

	@Override
	protected T _parseXml(XCalElement element, ICalParameters parameters, ParseContext context) {
		ICalDataType dataType = defaultDataType(context.getVersion());
		String value = element.first(dataType);
		if (value != null) {
			return parse(value);
		}

		throw missingXmlElements(dataType);
	}

	@Override
	protected JCalValue _writeJson(T property, WriteContext context) {
		UtcOffset offset = property.getValue();
		if (offset != null) {
			return JCalValue.single(offset.toString(true));
		}

		return JCalValue.single("");
	}

	@Override
	protected T _parseJson(JCalValue value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
		return parse(value.asSingle());
	}

	protected abstract T newInstance(UtcOffset offset);

	private T parse(String value) {
		if (value == null) {
			return newInstance(null);
		}

		try {
			return newInstance(UtcOffset.parse(value));
		} catch (IllegalArgumentException e) {
			throw new CannotParseException(28);
		}
	}
}