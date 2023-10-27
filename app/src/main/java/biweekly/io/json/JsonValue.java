package biweekly.io.json;

import java.util.List;
import java.util.Map;

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
 * Represents a JSON value, array, or object.
 * @author Michael Angstadt
 */
public class JsonValue {
	private final boolean isNull;
	private final Object value;
	private final List<JsonValue> array;
	private final Map<String, JsonValue> object;

	/**
	 * Creates a JSON value (such as a string or integer).
	 * @param value the value
	 */
	public JsonValue(Object value) {
		this.value = value;
		array = null;
		object = null;
		isNull = (value == null);
	}

	/**
	 * Creates a JSON array.
	 * @param array the array elements
	 */
	public JsonValue(List<JsonValue> array) {
		this.array = array;
		value = null;
		object = null;
		isNull = (array == null);
	}

	/**
	 * Creates a JSON object.
	 * @param object the object fields
	 */
	public JsonValue(Map<String, JsonValue> object) {
		this.object = object;
		value = null;
		array = null;
		isNull = (object == null);
	}

	/**
	 * Gets the JSON value.
	 * @return the value or null if it's not a JSON value
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * Gets the JSON array elements.
	 * @return the array elements or null if it's not a JSON array
	 */
	public List<JsonValue> getArray() {
		return array;
	}

	/**
	 * Gets the JSON object.
	 * @return the object or null if it's not a JSON object
	 */
	public Map<String, JsonValue> getObject() {
		return object;
	}

	/**
	 * Determines if the value is "null" or not.
	 * @return true if the value is "null", false if not
	 */
	public boolean isNull() {
		return isNull;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((array == null) ? 0 : array.hashCode());
		result = prime * result + (isNull ? 1231 : 1237);
		result = prime * result + ((object == null) ? 0 : object.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JsonValue other = (JsonValue) obj;
		if (array == null) {
			if (other.array != null)
				return false;
		} else if (!array.equals(other.array))
			return false;
		if (isNull != other.isNull)
			return false;
		if (object == null) {
			if (other.object != null)
				return false;
		} else if (!object.equals(other.object))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		if (isNull) {
			return "NULL";
		}

		if (value != null) {
			return "VALUE = " + value;
		}

		if (array != null) {
			return "ARRAY = " + array;
		}

		if (object != null) {
			return "OBJECT = " + object;
		}

		return "";
	}
}
