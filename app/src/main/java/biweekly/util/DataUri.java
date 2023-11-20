package biweekly.util;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import biweekly.Messages;
import biweekly.util.org.apache.commons.codec.binary.Base64;

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
 * Represents a data URI.
 * </p>
 * <p>
 * Example: {@code data:image/jpeg;base64,[base64 string]}
 * </p>
 * @author Michael Angstadt
 */
public final class DataUri {
	private final byte[] data;
	private final String text;
	private final String contentType;

	/**
	 * Creates a data URI.
	 * @param contentType the content type of the data (e.g. "image/png")
	 * @param data the data
	 */
	public DataUri(String contentType, byte[] data) {
		this(contentType, data, null);
	}

	/**
	 * Creates a data URI.
	 * @param contentType the content type of the text (e.g. "text/html")
	 * @param text the text
	 */
	public DataUri(String contentType, String text) {
		this(contentType, null, text);
	}

	/**
	 * Creates a data URI with a content type of "text/plain".
	 * @param text the text
	 */
	public DataUri(String text) {
		this("text/plain", text);
	}

	/**
	 * Copies a data URI.
	 * @param original the data URI to copy
	 */
	public DataUri(DataUri original) {
		this(original.contentType, (original.data == null) ? null : original.data.clone(), original.text);
	}

	private DataUri(String contentType, byte[] data, String text) {
		this.contentType = (contentType == null) ? "" : contentType.toLowerCase();
		this.data = data;
		this.text = text;
	}

	/**
	 * Parses a data URI string.
	 * @param uri the URI string (e.g. "data:image/jpeg;base64,[base64 string]")
	 * @return the parsed data URI
	 * @throws IllegalArgumentException if the string is not a valid data URI or
	 * it cannot be parsed
	 */
	public static DataUri parse(String uri) {
		//Syntax: data:[<media type>][;charset=<character set>][;base64],<data>

		String scheme = "data:";
		if (uri.length() < scheme.length() || !uri.substring(0, scheme.length()).equalsIgnoreCase(scheme)) {
			//not a data URI
			throw Messages.INSTANCE.getIllegalArgumentException(22);
		}

		String contentType = null;
		String charset = null;
		boolean base64 = false;
		String dataStr = null;
		int tokenStart = scheme.length();
		for (int i = scheme.length(); i < uri.length(); i++) {
			char c = uri.charAt(i);

			if (c == ';') {
				String token = uri.substring(tokenStart, i);
				if (contentType == null) {
					contentType = token.toLowerCase();
				} else {
					String cs = StringUtils.afterPrefixIgnoreCase(token, "charset=");
					if (cs != null) {
						charset = cs;
					} else if ("base64".equalsIgnoreCase(token)) {
						base64 = true;
					}
				}
				tokenStart = i + 1;
				continue;
			}

			if (c == ',') {
				String token = uri.substring(tokenStart, i);
				if (contentType == null) {
					contentType = token.toLowerCase();
				} else {
					String cs = StringUtils.afterPrefixIgnoreCase(token, "charset=");
					if (cs != null) {
						charset = cs;
					} else if ("base64".equalsIgnoreCase(token)) {
						base64 = true;
					}
				}

				dataStr = uri.substring(i + 1);
				break;
			}
		}

		if (dataStr == null) {
			throw Messages.INSTANCE.getIllegalArgumentException(23);
		}

		String text = null;
		byte[] data = null;
		if (base64) {
			dataStr = dataStr.replaceAll("\\s", "");
			data = Base64.decodeBase64(dataStr);
			if (charset != null) {
				try {
					text = new String(data, charset);
				} catch (UnsupportedEncodingException e) {
					throw new IllegalArgumentException(Messages.INSTANCE.getExceptionMessage(24, charset), e);
				}
				data = null;
			}
		} else {
			text = dataStr;
		}

		return new DataUri(contentType, data, text);
	}

	/**
	 * Gets the binary data.
	 * @return the binary data or null if the value was text
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * Gets the content type.
	 * @return the content type (e.g. "image/png")
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * Gets the text value.
	 * @return the text value or null if the value was binary
	 */
	public String getText() {
		return text;
	}

	/**
	 * Creates a data URI string.
	 * @return the data URI (e.g. "data:image/jpeg;base64,[base64 string]")
	 */
	@Override
	public String toString() {
		return toString(null);
	}

	/**
	 * Creates a data URI string.
	 * @param charset only applicable if the data URI's value is text. Defines
	 * the character set to encode the text in, or null not to specify a
	 * character set
	 * @return the data URI (e.g. "data:image/jpeg;base64,[base64 string]")
	 * @throws IllegalArgumentException if the given character set is not
	 * supported by this JVM
	 */
	public String toString(String charset) {
		StringBuilder sb = new StringBuilder();
		sb.append("data:");
		sb.append(contentType);

		if (data != null) {
			sb.append(";base64,");
			sb.append(Base64.encodeBase64String(data));
		} else if (text != null) {
			if (charset == null) {
				sb.append(',').append(text);
			} else {
				byte[] data;
				try {
					data = text.getBytes(charset);
				} catch (UnsupportedEncodingException e) {
					throw new IllegalArgumentException(Messages.INSTANCE.getExceptionMessage(25, charset), e);
				}

				sb.append(";charset=").append(charset);
				sb.append(";base64,");
				sb.append(Base64.encodeBase64String(data));
			}
		} else {
			sb.append(',');
		}

		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + contentType.hashCode();
		result = prime * result + Arrays.hashCode(data);
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		DataUri other = (DataUri) obj;
		if (!contentType.equals(other.contentType)) return false;
		if (!Arrays.equals(data, other.data)) return false;
		if (text == null) {
			if (other.text != null) return false;
		} else if (!text.equals(other.text)) return false;
		return true;
	}
}
