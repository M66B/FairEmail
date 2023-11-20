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
 * Represents a warning that occurred during the parsing of an iCalendar object.
 * @author Michael Angstadt
 */
public class ParseWarning {
	private final Integer code, lineNumber;
	private final String propertyName, message;

	private ParseWarning(Integer lineNumber, String propertyName, Integer code, String message) {
		this.lineNumber = lineNumber;
		this.propertyName = propertyName;
		this.code = code;
		this.message = message;
	}

	/**
	 * Gets the warning code.
	 * @return the warning code or null if no code was specified
	 */
	public Integer getCode() {
		return code;
	}

	/**
	 * Gets the line number the warning occurred on.
	 * @return the line number or null if not applicable
	 */
	public Integer getLineNumber() {
		return lineNumber;
	}

	/**
	 * Gets the warning message
	 * @return the warning message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Gets the name of the property that the warning occurred on.
	 * @return the property name (e.g. "DTSTART") or null if not applicable
	 */
	public String getPropertyName() {
		return propertyName;
	}

	@Override
	public String toString() {
		String message = this.message;
		if (code != null) {
			message = "(" + code + ") " + message;
		}

		if (lineNumber == null && propertyName == null) {
			return message;
		}

		String key = null;
		if (lineNumber != null && propertyName == null) {
			key = "parse.line";
		} else if (lineNumber == null && propertyName != null) {
			key = "parse.prop";
		} else if (lineNumber != null && propertyName != null) {
			key = "parse.lineWithProp";
		}

		return Messages.INSTANCE.getMessage(key, lineNumber, propertyName, message);
	}

	/**
	 * Constructs instances of the {@link ParseWarning} class.
	 * @author Michael Angstadt
	 */
	public static class Builder {
		private Integer lineNumber, code;
		private String propertyName, message;

		/**
		 * Creates an empty builder.
		 */
		public Builder() {
			//empty
		}

		/**
		 * Initializes the builder with data from the parse context.
		 * @param context the parse context
		 */
		public Builder(ParseContext context) {
			lineNumber(context.getLineNumber());
			propertyName(context.getPropertyName());
		}

		/**
		 * Sets the name of the property that the warning occurred on.
		 * @param propertyName the property name (e.g. "DTSTART") or null if not
		 * applicable
		 * @return this
		 */
		public Builder propertyName(String propertyName) {
			this.propertyName = propertyName;
			return this;
		}

		/**
		 * Sets the line number that the warning occurred on.
		 * @param lineNumber the line number or null if not applicable
		 * @return this
		 */
		public Builder lineNumber(Integer lineNumber) {
			this.lineNumber = lineNumber;
			return this;
		}

		/**
		 * Sets the warning message.
		 * @param code the message code
		 * @param args the message arguments
		 * @return this
		 */
		public Builder message(int code, Object... args) {
			this.code = code;
			message = Messages.INSTANCE.getParseMessage(code, args);
			return this;
		}

		/**
		 * Sets the warning message.
		 * @param message the warning message
		 * @return this
		 */
		public Builder message(String message) {
			code = null;
			this.message = message;
			return this;
		}

		/**
		 * Sets the warning message, based on the contents of a
		 * {@link CannotParseException}.
		 * @param exception the exception
		 * @return this
		 */
		public Builder message(CannotParseException exception) {
			return message(exception.getCode(), exception.getArgs());
		}

		/**
		 * Builds the {@link ParseWarning} object.
		 * @return the {@link ParseWarning} object
		 */
		public ParseWarning build() {
			return new ParseWarning(lineNumber, propertyName, code, message);
		}
	}
}
