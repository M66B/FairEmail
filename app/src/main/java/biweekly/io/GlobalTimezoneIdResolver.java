package biweekly.io;

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
 * Gets Java {@link TimeZone} objects that correspond with TZID parameters that
 * contain global timezone IDs (as opposed to IDs that correspond with a
 * VTIMEZONE component).
 * @author Michael Angstadt
 * @see <a href="https://tools.ietf.org/html/rfc5545#section-3.8.3.1">RFC 5545
 * section 3.8.3.1</a>
 * @see <a href="https://tools.ietf.org/html/rfc2445#section-4.2.19">RFC 2445
 * section 4.2.19</a>
 */
public interface GlobalTimezoneIdResolver {
	/**
	 * Returns an appropriate Java {@link TimeZone} object that corresponds to
	 * the given global ID.
	 * @param globalId the global ID (the value of the TZID parameter, without
	 * the forward slash prefix)
	 * @return the corresponding {@link TimeZone} object or null if the global
	 * ID is not recognized
	 */
	TimeZone resolve(String globalId);
}
