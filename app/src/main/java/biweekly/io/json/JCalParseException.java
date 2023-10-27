package biweekly.io.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonToken;

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
 * Thrown during the parsing of a JSON-encoded iCalendar object (jCal) when the
 * jCal object is not formatted in the correct way (the JSON syntax is valid,
 * but it's not in the correct jCal format).
 * @author Michael Angstadt
 */
public class JCalParseException extends IOException {
	private static final long serialVersionUID = -2447563507966434472L;
	private final JsonToken expected, actual;

	/**
	 * Creates a jCal parse exception.
	 * @param expected the JSON token that the parser was expecting
	 * @param actual the actual JSON token
	 */
	public JCalParseException(JsonToken expected, JsonToken actual) {
		super("Expected " + expected + " but was " + actual + ".");
		this.expected = expected;
		this.actual = actual;
	}

	/**
	 * Creates a jCal parse exception.
	 * @param message the detail message
	 * @param expected the JSON token that the parser was expecting
	 * @param actual the actual JSON token
	 */
	public JCalParseException(String message, JsonToken expected, JsonToken actual) {
		super(message);
		this.expected = expected;
		this.actual = actual;
	}

	/**
	 * Gets the JSON token that the parser was expected.
	 * @return the expected token
	 */
	public JsonToken getExpectedToken() {
		return expected;
	}

	/**
	 * Gets the JSON token that was read.
	 * @return the actual token
	 */
	public JsonToken getActualToken() {
		return actual;
	}
}
