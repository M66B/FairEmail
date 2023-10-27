package biweekly.io.scribe.property;

import java.util.EnumSet;
import java.util.Set;

import biweekly.ICalDataType;
import biweekly.ICalVersion;
import biweekly.io.ParseContext;
import biweekly.io.WriteContext;
import biweekly.io.json.JCalValue;
import biweekly.io.xml.XCalElement;
import biweekly.parameter.ICalParameters;
import biweekly.property.RequestStatus;

import com.github.mangstadt.vinnie.io.VObjectPropertyValues.SemiStructuredValueBuilder;
import com.github.mangstadt.vinnie.io.VObjectPropertyValues.SemiStructuredValueIterator;
import com.github.mangstadt.vinnie.io.VObjectPropertyValues.StructuredValueIterator;

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
 * Marshals {@link RequestStatus} properties.
 * @author Michael Angstadt
 */
public class RequestStatusScribe extends ICalPropertyScribe<RequestStatus> {
	public RequestStatusScribe() {
		super(RequestStatus.class, "REQUEST-STATUS", ICalDataType.TEXT);
	}

	@Override
	protected String _writeText(RequestStatus property, WriteContext context) {
		SemiStructuredValueBuilder builder = new SemiStructuredValueBuilder();
		builder.append(property.getStatusCode());
		builder.append(property.getDescription());
		builder.append(property.getExceptionText());
		boolean escapeCommas = (context.getVersion() != ICalVersion.V1_0);
		return builder.build(escapeCommas, true);
	}

	@Override
	protected RequestStatus _parseText(String value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
		SemiStructuredValueIterator it = new SemiStructuredValueIterator(value);

		RequestStatus requestStatus = new RequestStatus(it.next());
		requestStatus.setDescription(it.next());
		requestStatus.setExceptionText(it.next());
		return requestStatus;
	}

	@Override
	protected void _writeXml(RequestStatus property, XCalElement element, WriteContext context) {
		String code = property.getStatusCode();
		element.append("code", code);

		String description = property.getDescription();
		element.append("description", description);

		String data = property.getExceptionText();
		if (data != null) {
			element.append("data", data);
		}
	}

	@Override
	protected RequestStatus _parseXml(XCalElement element, ICalParameters parameters, ParseContext context) {
		String code = element.first("code");
		if (code == null) {
			throw missingXmlElements("code");
		}

		RequestStatus requestStatus = new RequestStatus(s(code));
		requestStatus.setDescription(s(element.first("description"))); //optional field
		requestStatus.setExceptionText(s(element.first("data"))); //optional field
		return requestStatus;
	}

	@Override
	protected JCalValue _writeJson(RequestStatus property, WriteContext context) {
		return JCalValue.structured(property.getStatusCode(), property.getDescription(), property.getExceptionText());
	}

	@Override
	protected RequestStatus _parseJson(JCalValue value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
		StructuredValueIterator it = new StructuredValueIterator(value.asStructured());

		RequestStatus requestStatus = new RequestStatus(it.nextValue());
		requestStatus.setDescription(it.nextValue());
		requestStatus.setExceptionText(it.nextValue());
		return requestStatus;
	}

	private static String s(String str) {
		return (str == null || str.isEmpty()) ? null : str;
	}

	@Override
	public Set<ICalVersion> getSupportedVersions() {
		return EnumSet.of(ICalVersion.V2_0_DEPRECATED, ICalVersion.V2_0);
	}
}