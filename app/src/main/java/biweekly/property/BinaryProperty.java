package biweekly.property;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import biweekly.ICalVersion;
import biweekly.ValidationWarning;
import biweekly.component.ICalComponent;
import biweekly.util.Gobble;

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
 * A property whose value is a binary resource (such as an image or document).
 * @author Michael Angstadt
 */
public class BinaryProperty extends ICalProperty {
	protected byte[] data;
	protected String uri;

	/**
	 * Creates a new binary property.
	 * @param file a file containing the binary data
	 * @throws IOException if there's a problem reading from the file
	 */
	public BinaryProperty(File file) throws IOException {
		this.data = new Gobble(file).asByteArray();
	}

	/**
	 * Creates a new binary property.
	 * @param data the binary data
	 */
	public BinaryProperty(byte[] data) {
		this.data = data;
	}

	/**
	 * Creates a new binary property.
	 * @param uri a URL pointing to the resource (e.g.
	 * "http://example.com/image.png")
	 */
	public BinaryProperty(String uri) {
		this.uri = uri;
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public BinaryProperty(BinaryProperty original) {
		super(original);
		data = (original.data == null) ? null : original.data.clone();
		uri = original.uri;
	}

	/**
	 * Gets the property's binary data.
	 * @return the binary data or null if not set
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * Sets the property's binary data.
	 * @param data the binary data
	 */
	public void setData(byte[] data) {
		this.data = data;
		uri = null;
	}

	/**
	 * Gets the property's URI.
	 * @return the URI (e.g. "http://example.com/image.png") or null if not set
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * Sets the property's URI.
	 * @param uri the URI (e.g. "http://example.com/image.png")
	 */
	public void setUri(String uri) {
		this.uri = uri;
		data = null;
	}

	@Override
	public String getFormatType() {
		return super.getFormatType();
	}

	@Override
	public void setFormatType(String formatType) {
		super.setFormatType(formatType);
	}

	@Override
	protected void validate(List<ICalComponent> components, ICalVersion version, List<ValidationWarning> warnings) {
		if (uri == null && data == null) {
			warnings.add(new ValidationWarning(26));
		}
	}

	@Override
	protected Map<String, Object> toStringValues() {
		Map<String, Object> values = new LinkedHashMap<String, Object>();
		values.put("data", (data == null) ? "null" : "length: " + data.length);
		values.put("uri", uri);
		return values;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.hashCode(data);
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		BinaryProperty other = (BinaryProperty) obj;
		if (uri == null) {
			if (other.uri != null) return false;
		} else if (!uri.equals(other.uri)) return false;
		if (!Arrays.equals(data, other.data)) return false;
		return true;
	}
}
