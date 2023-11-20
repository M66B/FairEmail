package biweekly.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
 * Represents a software version number (e.g. "1.8.14").
 * @author Michael Angstadt
 */
public class VersionNumber implements Comparable<VersionNumber> {
	private final List<Integer> parts;

	/**
	 * Creates a new version number.
	 * @param version the version string (e.g. "1.8.14")
	 * @throws IllegalArgumentException if the version string is invalid
	 */
	public VersionNumber(String version) {
		parts = new ArrayList<Integer>();

		int start = 0;
		for (int i = 0; i < version.length(); i++) {
			char c = version.charAt(i);
			if (c == '.') {
				addNumber(version, start, i);
				start = i + 1;
			}
		}
		addNumber(version, start, version.length());
	}

	private void addNumber(String version, int fromIndex, int toIndex) {
		String numberStr = version.substring(fromIndex, toIndex);
		Integer number = Integer.valueOf(numberStr);
		parts.add(number);
	}

	public int compareTo(VersionNumber that) {
		Iterator<Integer> it = parts.iterator();
		Iterator<Integer> it2 = that.parts.iterator();
		while (it.hasNext() || it2.hasNext()) {
			int number = it.hasNext() ? it.next() : 0;
			int number2 = it2.hasNext() ? it2.next() : 0;

			if (number < number2) {
				return -1;
			}
			if (number > number2) {
				return 1;
			}
		}
		return 0;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + parts.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		VersionNumber other = (VersionNumber) obj;
		if (!parts.equals(other.parts)) return false;
		return true;
	}

	@Override
	public String toString() {
		return StringUtils.join(parts, ".");
	}
}
