package biweekly.property;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
 * Represents a property that has a defined set of acceptable values (for
 * example, the {@link Action} property).
 * @author Michael Angstadt
 */
public abstract class EnumProperty extends TextProperty {
	/**
	 * Creates an enum property.
	 * @param value the property value
	 */
	public EnumProperty(String value) {
		super(value);
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public EnumProperty(EnumProperty original) {
		super(original);
	}

	/**
	 * Compares the property's value with a given string (case-insensitive).
	 * @param value the string
	 * @return true if it's equal, false if not
	 */
	protected boolean is(String value) {
		return value.equalsIgnoreCase(this.value);
	}

	/**
	 * Gets the list of acceptable values for this property.
	 * @param version the version
	 * @return the list of acceptable values
	 */
	protected abstract Collection<String> getStandardValues(ICalVersion version);

	/**
	 * Gets the iCalendar versions that this property's value is supported in.
	 * Meant to be overridden by the child class.
	 * @return the supported versions
	 */
	protected Collection<ICalVersion> getValueSupportedVersions() {
		return (value == null) ? Collections.<ICalVersion> emptyList() : Arrays.asList(ICalVersion.values());
	}

	@Override
	protected void validate(List<ICalComponent> components, ICalVersion version, List<ValidationWarning> warnings) {
		super.validate(components, version, warnings);
		if (value == null) {
			return;
		}

		Collection<ICalVersion> supportedVersions = getValueSupportedVersions();
		if (supportedVersions.isEmpty()) {
			//it's a non-standard value
			warnings.add(new ValidationWarning(28, value, getStandardValues(version)));
			return;
		}

		boolean supported = supportedVersions.contains(version);
		if (!supported) {
			warnings.add(new ValidationWarning(46, value, supportedVersions));
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + parameters.hashCode();
		result = prime * result + ((value == null) ? 0 : value.toLowerCase().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		EnumProperty other = (EnumProperty) obj;
		if (!parameters.equals(other.parameters)) return false;
		if (value == null) {
			if (other.value != null) return false;
		} else if (!value.equalsIgnoreCase(other.value)) return false;
		return true;
	}
}
