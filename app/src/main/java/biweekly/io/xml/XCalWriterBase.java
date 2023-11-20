package biweekly.io.xml;

import java.util.HashMap;
import java.util.Map;

import biweekly.ICalDataType;
import biweekly.ICalVersion;
import biweekly.io.StreamWriter;
import biweekly.parameter.ICalParameters;

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
 * Base class for xCal writers.
 * @author Michael Angstadt
 */
abstract class XCalWriterBase extends StreamWriter {
	protected final ICalVersion targetVersion = ICalVersion.V2_0; //xCal only supports 2.0

	/**
	 * Defines the names of the XML elements that are used to hold each
	 * parameter's value.
	 */
	protected final Map<String, ICalDataType> parameterDataTypes = new HashMap<String, ICalDataType>();
	{
		registerParameterDataType(ICalParameters.ALTREP, ICalDataType.URI);
		//registerParameterDataType(ICalParameters.CHARSET, ICalDataType.TEXT); //not used by 2.0
		registerParameterDataType(ICalParameters.CN, ICalDataType.TEXT);
		registerParameterDataType(ICalParameters.CUTYPE, ICalDataType.TEXT);
		registerParameterDataType(ICalParameters.DELEGATED_FROM, ICalDataType.CAL_ADDRESS);
		registerParameterDataType(ICalParameters.DELEGATED_TO, ICalDataType.CAL_ADDRESS);
		registerParameterDataType(ICalParameters.DIR, ICalDataType.URI);
		registerParameterDataType(ICalParameters.DISPLAY, ICalDataType.TEXT);
		registerParameterDataType(ICalParameters.EMAIL, ICalDataType.TEXT);
		registerParameterDataType(ICalParameters.ENCODING, ICalDataType.TEXT);
		//registerParameterDataType(ICalParameters.EXPECT, ICalDataType.TEXT); //not used by 2.0
		registerParameterDataType(ICalParameters.FEATURE, ICalDataType.TEXT);
		registerParameterDataType(ICalParameters.FMTTYPE, ICalDataType.TEXT);
		registerParameterDataType(ICalParameters.FBTYPE, ICalDataType.TEXT);
		registerParameterDataType(ICalParameters.LABEL, ICalDataType.TEXT);
		registerParameterDataType(ICalParameters.LANGUAGE, ICalDataType.TEXT);
		registerParameterDataType(ICalParameters.MEMBER, ICalDataType.CAL_ADDRESS);
		registerParameterDataType(ICalParameters.PARTSTAT, ICalDataType.TEXT);
		registerParameterDataType(ICalParameters.RANGE, ICalDataType.TEXT);
		registerParameterDataType(ICalParameters.RELATED, ICalDataType.TEXT);
		registerParameterDataType(ICalParameters.RELTYPE, ICalDataType.TEXT);
		registerParameterDataType(ICalParameters.ROLE, ICalDataType.TEXT);
		registerParameterDataType(ICalParameters.RSVP, ICalDataType.BOOLEAN);
		registerParameterDataType(ICalParameters.SENT_BY, ICalDataType.CAL_ADDRESS);
		//registerParameterDataType(ICalParameters.STATUS, ICalDataType.TEXT); //not used by 2.0
		//registerParameterDataType(ICalParameters.TYPE, ICalDataType.TEXT); //not used by 2.0
		registerParameterDataType(ICalParameters.TZID, ICalDataType.TEXT);
		//registerParameterDataType(ICalParameters.VALUE, ICalDataType.TEXT); //not used in xCal
	}

	@Override
	protected ICalVersion getTargetVersion() {
		return targetVersion;
	}

	/**
	 * Registers the data type of an experimental parameter. Experimental
	 * parameters use the "unknown" data type by default.
	 * @param parameterName the parameter name (e.g. "x-foo")
	 * @param dataType the data type or null to remove
	 */
	public void registerParameterDataType(String parameterName, ICalDataType dataType) {
		parameterName = parameterName.toLowerCase();
		if (dataType == null) {
			parameterDataTypes.remove(parameterName);
		} else {
			parameterDataTypes.put(parameterName, dataType);
		}
	}
}
