package biweekly.property;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import biweekly.ICalVersion;
import biweekly.ValidationWarning;
import biweekly.component.ICalComponent;

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
 * Represents a property whose data model consists of a single Java object (such
 * as a String).
 * @author Michael Angstadt
 * @param <T> the value class (e.g. String)
 */
public class ValuedProperty<T> extends ICalProperty {
	protected T value;

	/**
	 * Creates a new valued property.
	 * @param value the property's value
	 */
	public ValuedProperty(T value) {
		setValue(value);
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public ValuedProperty(ValuedProperty<T> original) {
		super(original);
		value = original.value;
	}

	/**
	 * Gets the value of this property.
	 * @return the value
	 */
	public T getValue() {
		return value;
	}

	/**
	 * Sets the value of this property.
	 * @param value the value
	 */
	public void setValue(T value) {
		this.value = value;
	}

	@Override
	protected void validate(List<ICalComponent> components, ICalVersion version, List<ValidationWarning> warnings) {
		if (value == null) {
			warnings.add(new ValidationWarning(26));
		}
	}

	/**
	 * Utility method that gets the value of a {@link ValuedProperty} object.
	 * @param property the property object (may be null)
	 * @param <T> the value class
	 * @return the property value (may be null), or null if the property object
	 * itself is null
	 */
	public static <T> T getValue(ValuedProperty<T> property) {
		return (property == null) ? null : property.getValue();
	}

	@Override
	protected Map<String, Object> toStringValues() {
		Map<String, Object> values = new LinkedHashMap<String, Object>();
		values.put("value", value);
		return values;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((value == null) ? 0 : valueHashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;

		/*
		 * This cast will not fail because each property's Class objects are
		 * checked for equality in super.equals().
		 */
		@SuppressWarnings("unchecked")
		ValuedProperty<T> other = (ValuedProperty<T>) obj;

		if (value == null) {
			if (other.value != null) return false;
		} else if (!valueEquals(other.value)) return false;
		return true;
	}

	/**
	 * <p>
	 * Calculates the hash code of this property's value.
	 * </p>
	 * <p>
	 * This method is meant by to overridden by child classes whose value's hash
	 * code cannot be calculated by just invoking {@code hashCode()}. For
	 * example, a property whose value is case insensitive. The default
	 * implementation of this method calls {@code value.hashCode()}.
	 * </p>
	 * <p>
	 * This method is only invoked when this property's value is not null.
	 * </p>
	 * @return the value's hash code
	 */
	protected int valueHashCode() {
		return value.hashCode();
	}

	/**
	 * <p>
	 * Compares this property's value with another property's value for
	 * equality.
	 * </p>
	 * <p>
	 * This method is meant by to overridden by child classes when their value's
	 * equality cannot be calculated by just invoking {@code equals()}. For
	 * example, a property whose value is case insensitive. The default
	 * implementation of this method calls {@code value.equals(otherValue)}.
	 * </p>
	 * <p>
	 * This method is only invoked when this property's value is not null.
	 * </p>
	 * @param otherValue the other property's value
	 * @return true if this property's value is equal to the other property's
	 * value, false if not
	 */
	protected boolean valueEquals(T otherValue) {
		return value.equals(otherValue);
	}
}
