package biweekly.io;

import biweekly.Messages;

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
 * Thrown during the unmarshalling of an iCalendar property to signal that the
 * property's value could not be parsed (for example, being unable to parse a
 * date string).
 * @author Michael Angstadt
 */
public class CannotParseException extends RuntimeException {
	private static final long serialVersionUID = 8299420302297241326L;
	private final Integer code;
	private final Object args[];

	/**
	 * Creates a new "cannot parse" exception.
	 * @param code the warning message code
	 * @param args the warning message arguments
	 */
	public CannotParseException(int code, Object... args) {
		this.code = code;
		this.args = args;
	}

	/**
	 * Creates a new "cannot parse" exception.
	 * @param reason the reason why the property value cannot be parsed
	 */
	public CannotParseException(String reason) {
		this(1, reason);
	}

	/**
	 * Gets the warning message code.
	 * @return the message code
	 */
	public Integer getCode() {
		return code;
	}

	/**
	 * Gets the warning message arguments.
	 * @return the message arguments
	 */
	public Object[] getArgs() {
		return args;
	}

	@Override
	public String getMessage() {
		return Messages.INSTANCE.getParseMessage(code, args);
	}
}
