package biweekly;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

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
 * Singleton for accessing the i18n resource bundle.
 * @author Michael Angstadt
 */
public enum Messages {
	INSTANCE;

	private final transient ResourceBundle messages;

	Messages() {
		messages = ResourceBundle.getBundle("biweekly/messages");
	}

	/**
	 * Gets a validation warning message.
	 * @param code the message code
	 * @param args the message arguments
	 * @return the message
	 */
	public String getValidationWarning(int code, Object... args) {
		return getMessage("validate." + code, args);
	}

	/**
	 * Gets a parser warning message.
	 * @param code the message code
	 * @param args the message arguments
	 * @return the message
	 */
	public String getParseMessage(int code, Object... args) {
		return getMessage("parse." + code, args);
	}

	/**
	 * Gets an exception message.
	 * @param code the message code
	 * @param args the message arguments
	 * @return the message or null if not found
	 */
	public String getExceptionMessage(int code, Object... args) {
		return getMessage("exception." + code, args);
	}

	/**
	 * Builds an {@link IllegalArgumentException} from an exception message.
	 * @param code the message code
	 * @param args the message arguments
	 * @return the exception or null if the message was not found
	 */
	public IllegalArgumentException getIllegalArgumentException(int code, Object... args) {
		String message = getExceptionMessage(code, args);
		return (message == null) ? null : new IllegalArgumentException(message);
	}

	/**
	 * Gets a message.
	 * @param key the message key
	 * @param args the message arguments
	 * @return the message or null if not found
	 */
	public String getMessage(String key, Object... args) {
		try {
			String message = messages.getString(key);
			return MessageFormat.format(message, args);
		} catch (MissingResourceException e) {
			return null;
		}
	}
}
