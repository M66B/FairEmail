package biweekly.io;

import biweekly.util.ICalDateFormat;

import java.util.TimeZone;

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
 * <p>
 * Default implementation of {@link GlobalTimezoneIdResolver}.
 * </p>
 * <p>
 * The following are examples of the kinds of TZID formats this class is able to
 * handle.
 * </p>
 * <ul>
 * <li>"TZID=/America/New_York" resolves to
 * {@code TimeZone.getTimeZone("America/New_York")}</li>
 * <li>"TZID=/mozilla.org/20050126_1/America/New_York" resolves to
 * {@code TimeZone.getTimeZone("America/New_York")}</li>
 * </ul>
 * @author Michael Angstadt
 */
public class DefaultGlobalTimezoneIdResolver implements GlobalTimezoneIdResolver {
	@Override
	public TimeZone resolve(String globalId) {
		globalId = removeMozillaPrefixIfPresent(globalId);
		return ICalDateFormat.parseTimeZoneId(globalId);
	}

	/**
	 * Checks for, and removes, a global ID prefix that Mozilla software adds to
	 * its iCal files. Googling this prefix returns several search results,
	 * suggesting it is frequently encountered in the wild.
	 * @param globalId the global ID (may or may not contain the Mozilla prefix)
	 * @return the sanitized global ID, or the unchanged ID if it does not
	 * contain the prefix
	 */
	private String removeMozillaPrefixIfPresent(String globalId) {
		String prefix = "mozilla.org/20050126_1/";
		return globalId.startsWith(prefix) ? globalId.substring(prefix.length()) : globalId;
	}
}
