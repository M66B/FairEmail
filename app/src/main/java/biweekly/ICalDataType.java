package biweekly;

import java.util.Collection;

import biweekly.util.CaseClasses;

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
 * Defines the data type of a property's value.
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5545#page-29">RFC 5545
 * p.29-50</a>
 */
public class ICalDataType {
	private static final CaseClasses<ICalDataType, String> enums = new CaseClasses<ICalDataType, String>(ICalDataType.class) {
		@Override
		protected ICalDataType create(String value) {
			return new ICalDataType(value);
		}

		@Override
		protected boolean matches(ICalDataType dataType, String value) {
			return dataType.name.equalsIgnoreCase(value);
		}
	};

	/**
	 * Binary data (such as an image or word-processing document).
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-30">RFC 5545
	 * p.30-1</a>
	 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.18</a>
	 */
	public static final ICalDataType BINARY = new ICalDataType("BINARY");

	/**
	 * Boolean value ("true" or "false").
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-31">RFC 5545
	 * p.31</a>
	 */
	public static final ICalDataType BOOLEAN = new ICalDataType("BOOLEAN");

	/**
	 * A URI containing a calendar user address (typically, a "mailto" URI).
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-30">RFC 5545
	 * p.30-1</a>
	 */
	public static final ICalDataType CAL_ADDRESS = new ICalDataType("CAL-ADDRESS");

	/**
	 * The property value is located in a separate MIME entity (vCal 1.0 only).
	 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.17</a>
	 */
	public static final ICalDataType CONTENT_ID = new ICalDataType("CONTENT-ID"); //1.0 only

	/**
	 * A date (for example, "2014-03-12").
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-32">RFC 5545
	 * p.32</a>
	 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.16-7</a>
	 */
	public static final ICalDataType DATE = new ICalDataType("DATE");

	/**
	 * A date/time value (for example, "2014-03-12 13:30:00").
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-32">RFC 5545
	 * p.32-4</a>
	 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.16-7</a>
	 */
	public static final ICalDataType DATE_TIME = new ICalDataType("DATE-TIME");

	/**
	 * A duration of time (for example, "2 hours, 30 minutes").
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-35">RFC 5545
	 * p.35-6</a>
	 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.17</a>
	 */
	public static final ICalDataType DURATION = new ICalDataType("DURATION");

	/**
	 * A floating point value (for example, "3.14")
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-36">RFC 5545
	 * p.36</a>
	 */
	public static final ICalDataType FLOAT = new ICalDataType("FLOAT");

	/**
	 * An integer value (for example, "42")
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-37">RFC 5545
	 * p.37</a>
	 */
	public static final ICalDataType INTEGER = new ICalDataType("INTEGER");

	/**
	 * A period of time (for example, "October 3 through October 5").
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-37-8">RFC 5545
	 * p.37-8</a>
	 */
	public static final ICalDataType PERIOD = new ICalDataType("PERIOD");

	/**
	 * A recurrence rule (for example, "every Monday at 2pm").
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-38">RFC 5545
	 * p.38-45</a>
	 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.18-23</a>
	 */
	public static final ICalDataType RECUR = new ICalDataType("RECUR");

	/**
	 * A plain text value.
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-45">RFC 5545
	 * p.45-6</a>
	 */
	public static final ICalDataType TEXT = new ICalDataType("TEXT");

	/**
	 * A time value (for example, "2pm").
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-47">RFC 5545
	 * p.47-8</a>
	 */
	public static final ICalDataType TIME = new ICalDataType("TIME");

	/**
	 * A URI value.
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-49">RFC 5545
	 * p.49</a>
	 */
	public static final ICalDataType URI = new ICalDataType("URI");

	/**
	 * A URL (for example, "http://example.com/picture.jpg") (vCal 1.0 only).
	 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.17-8</a>
	 */
	public static final ICalDataType URL = new ICalDataType("URL");

	/**
	 * A UTC-offset (for example, "+0500").
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-49">RFC 5545
	 * p.49-50</a>
	 */
	public static final ICalDataType UTC_OFFSET = new ICalDataType("UTC-OFFSET");

	private final String name;

	private ICalDataType(String name) {
		this.name = name;
	}

	/**
	 * Gets the name of the data type.
	 * @return the name of the data type (e.g. "TEXT")
	 */
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * Searches for a parameter value that is defined as a static constant in
	 * this class.
	 * @param value the parameter value
	 * @return the object or null if not found
	 */
	public static ICalDataType find(String value) {
		if ("CID".equalsIgnoreCase(value)) {
			//"CID" is an alias for "CONTENT-ID" (vCal 1.0, p.17)
			return CONTENT_ID;
		}
		return enums.find(value);
	}

	/**
	 * Searches for a parameter value and creates one if it cannot be found. All
	 * objects are guaranteed to be unique, so they can be compared with
	 * {@code ==} equality.
	 * @param value the parameter value
	 * @return the object
	 */
	public static ICalDataType get(String value) {
		if ("CID".equalsIgnoreCase(value)) {
			//"CID" is an alias for "CONTENT-ID" (vCal 1.0, p.17)
			return CONTENT_ID;
		}
		return enums.get(value);
	}

	/**
	 * Gets all of the parameter values that are defined as static constants in
	 * this class.
	 * @return the parameter values
	 */
	public static Collection<ICalDataType> all() {
		return enums.all();
	}
}
