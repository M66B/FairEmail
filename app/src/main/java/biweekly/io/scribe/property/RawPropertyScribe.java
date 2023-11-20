package biweekly.io.scribe.property;

import java.util.List;

import biweekly.ICalDataType;
import biweekly.ICalVersion;
import biweekly.io.ParseContext;
import biweekly.io.WriteContext;
import biweekly.io.json.JCalValue;
import biweekly.io.json.JsonValue;
import biweekly.io.xml.XCalElement;
import biweekly.io.xml.XCalElement.XCalValue;
import biweekly.parameter.ICalParameters;
import biweekly.property.RawProperty;

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
 * Marshals {@link RawProperty} properties.
 * @author Michael Angstadt
 */
/*
 * Note concerning escaping and unescaping special characters:
 * 
 * Values are not escaped and unescaped for the following reason: If the
 * experimental property's value is a list or structured list, then the escaping
 * must be preserved or else escaped special characters will be lost.
 * 
 * This is an inconvenience, considering the fact that most experimental
 * properties contain simple text values. But it is necessary in order to
 * prevent data loss.
 */
public class RawPropertyScribe extends ICalPropertyScribe<RawProperty> {
	public RawPropertyScribe(String propertyName) {
		super(RawProperty.class, propertyName, null);
	}

	@Override
	protected ICalDataType _dataType(RawProperty property, ICalVersion version) {
		return property.getDataType();
	}

	@Override
	protected String _writeText(RawProperty property, WriteContext context) {
		String value = property.getValue();
		return (value == null) ? "" : value;
	}

	@Override
	protected RawProperty _parseText(String value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
		return new RawProperty(propertyName, dataType, value);
	}

	@Override
	protected RawProperty _parseXml(XCalElement element, ICalParameters parameters, ParseContext context) {
		XCalValue firstValue = element.firstValue();
		ICalDataType dataType = firstValue.getDataType();
		String value = firstValue.getValue();

		return new RawProperty(propertyName, dataType, value);
	}

	@Override
	protected RawProperty _parseJson(JCalValue value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
		String valueStr = jcardValueToString(value);

		return new RawProperty(propertyName, dataType, valueStr);
	}

	private static String jcardValueToString(JCalValue value) {
		/*
		 * ICalPropertyScribe.jcardValueToString() cannot be used because it
		 * escapes single values.
		 */
		List<JsonValue> values = value.getValues();
		if (values.size() > 1) {
			List<String> multi = value.asMulti();
			if (!multi.isEmpty()) {
				return VObjectPropertyValues.writeList(multi);
			}
		}

		if (!values.isEmpty() && values.get(0).getArray() != null) {
			List<List<String>> structured = value.asStructured();
			if (!structured.isEmpty()) {
				return VObjectPropertyValues.writeStructured(structured, true);
			}
		}

		return value.asSingle();
	}
}
