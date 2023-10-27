package biweekly.util;

import java.text.NumberFormat;
import java.util.Locale;

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
 * Formats floating-point values for iCalendar objects. This ensures that numbers
 * are rendered the same, no matter the default locale.
 * </p>
 * <ul>
 * <li>Decimal separator can differ by locale (e.g. Germany uses ",")</li>
 * <li>Number characters can differ by locale (e.g. "1.0" is "۱٫۰" in Iran)</li>
 * </ul>
 * @author Michael Angstadt
 */
public class ICalFloatFormatter {
	private final NumberFormat nf = NumberFormat.getNumberInstance(Locale.ROOT);

	/**
	 * Creates a new formatter with a max of 6 decimals.
	 */
	public ICalFloatFormatter() {
		this(6);
	}

	/**
	 * Creates a new formatter.
	 * @param decimals the max number of decimal places
	 */
	public ICalFloatFormatter(int decimals) {
		nf.setMaximumFractionDigits(decimals);
		if (decimals > 0) {
			nf.setMinimumFractionDigits(1);
		}
	}

	/**
	 * Formats a number for inclusion in an iCalendar object.
	 * @param number the number
	 * @return the formatted number
	 */
	public String format(double number) {
		return nf.format(number);
	}
}
