package biweekly.util;

import java.util.Collection;

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
 * Helper class for dealing with strings.
 * @author Michael Angstadt
 */
public final class StringUtils {
	/**
	 * The local computer's newline character sequence.
	 */
	public static final String NEWLINE = System.getProperty("line.separator");

	/**
	 * <p>
	 * Returns a substring of the given string that comes after the given
	 * prefix. Prefix matching is case-insensitive.
	 * </p>
	 * <p>
	 * <b>Example:</b>
	 * </p>
	 * 
	 * <pre class="brush:java">
	 * String result = StringUtils.afterPrefixIgnoreCase("MAILTO:email@example.com", "mailto:");
	 * assertEquals("email@example.com", result);
	 * 
	 * result = StringUtils.afterPrefixIgnoreCase("http://www.google.com", "mailto:");
	 * assertNull(result);
	 * </pre>
	 * 
	 * @param string the string
	 * @param prefix the prefix
	 * @return the string or null if the prefix was not found
	 */
	public static String afterPrefixIgnoreCase(String string, String prefix) {
		if (string.length() < prefix.length()) {
			return null;
		}

		for (int i = 0; i < prefix.length(); i++) {
			char a = Character.toUpperCase(prefix.charAt(i));
			char b = Character.toUpperCase(string.charAt(i));
			if (a != b) {
				return null;
			}
		}

		return string.substring(prefix.length());
	}

	/**
	 * Creates a string consisting of "count" occurrences of char "c".
	 * @param c the character to repeat
	 * @param count the number of times to repeat the character
	 * @param sb the character sequence to append the characters to
	 */
	public static void repeat(char c, int count, StringBuilder sb) {
		for (int i = 0; i < count; i++) {
			sb.append(c);
		}
	}

	/**
	 * Joins a collection of values into a delimited list.
	 * @param collection the collection of values
	 * @param delimiter the delimiter (e.g. ",")
	 * @param <T> the value class
	 * @return the final string
	 */
	public static <T> String join(Collection<T> collection, String delimiter) {
		StringBuilder sb = new StringBuilder();
		join(collection, delimiter, sb);
		return sb.toString();
	}

	/**
	 * Joins a collection of values into a delimited list.
	 * @param collection the collection of values
	 * @param delimiter the delimiter (e.g. ",")
	 * @param sb the string builder to append onto
	 * @param <T> the value class
	 */
	public static <T> void join(Collection<T> collection, String delimiter, StringBuilder sb) {
		join(collection, delimiter, sb, new JoinCallback<T>() {
			public void handle(StringBuilder sb, T value) {
				sb.append(value);
			}
		});
	}

	/**
	 * Joins a collection of values into a delimited list.
	 * @param collection the collection of values
	 * @param delimiter the delimiter (e.g. ",")
	 * @param join callback function to call on every element in the collection
	 * @param <T> the value class
	 * @return the final string
	 */
	public static <T> String join(Collection<T> collection, String delimiter, JoinCallback<T> join) {
		StringBuilder sb = new StringBuilder();
		join(collection, delimiter, sb, join);
		return sb.toString();
	}

	/**
	 * Joins a collection of values into a delimited list.
	 * @param collection the collection of values
	 * @param delimiter the delimiter (e.g. ",")
	 * @param sb the string builder to append onto
	 * @param join callback function to call on every element in the collection
	 * @param <T> the value class
	 */
	public static <T> void join(Collection<T> collection, String delimiter, StringBuilder sb, JoinCallback<T> join) {
		boolean first = true;
		for (T element : collection) {
			if (!first) {
				sb.append(delimiter);
			}

			join.handle(sb, element);
			first = false;
		}
	}

	/**
	 * Callback interface used with various {@code StringUtils.join()} methods.
	 * @author Michael Angstadt
	 * @param <T> the value class
	 */
	public interface JoinCallback<T> {
		void handle(StringBuilder sb, T value);
	}

	private StringUtils() {
		//hide
	}
}
