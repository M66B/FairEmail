package biweekly.property;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import biweekly.ICalDataType;
import biweekly.ICalVersion;
import biweekly.ValidationWarning;
import biweekly.component.ICalComponent;

import com.github.mangstadt.vinnie.SyntaxStyle;
import com.github.mangstadt.vinnie.validate.AllowedCharacters;
import com.github.mangstadt.vinnie.validate.VObjectValidator;

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
 * Represents a property that does not have a scribe associated with it.
 * @author Michael Angstadt
 */
public class RawProperty extends ICalProperty {
	private String name;
	private ICalDataType dataType;
	private String value;

	/**
	 * Creates a raw property.
	 * @param name the property name (e.g. "X-MS-ANNIVERSARY")
	 * @param value the property value
	 */
	public RawProperty(String name, String value) {
		this(name, null, value);
	}

	/**
	 * Creates a raw property.
	 * @param name the property name (e.g. "X-MS-ANNIVERSARY")
	 * @param dataType the property value's data type
	 * @param value the property value
	 */
	public RawProperty(String name, ICalDataType dataType, String value) {
		this.name = name;
		this.dataType = dataType;
		this.value = value;
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public RawProperty(RawProperty original) {
		super(original);
		name = original.name;
		dataType = original.dataType;
		value = original.value;
	}

	/**
	 * Gets the property value.
	 * @return the property value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Sets the property value.
	 * @param value the property value
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * Gets the property value's data type.
	 * @return the data type
	 */
	public ICalDataType getDataType() {
		return dataType;
	}

	/**
	 * Sets the property value's data type.
	 * @param dataType the data type
	 */
	public void setDataType(ICalDataType dataType) {
		this.dataType = dataType;
	}

	/**
	 * Gets the property name.
	 * @return the property name (e.g. "X-MS-ANNIVERSARY")
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the property name.
	 * @param name the property name (e.g. "X-MS-ANNIVERSARY")
	 */
	public void setName(String name) {
		this.name = name;
	}

	@Override
	protected void validate(List<ICalComponent> components, ICalVersion version, List<ValidationWarning> warnings) {
		SyntaxStyle syntax = version.getSyntaxStyle();
		AllowedCharacters allowed = VObjectValidator.allowedCharactersParameterName(syntax, true);
		if (!allowed.check(name)) {
			if (syntax == SyntaxStyle.OLD) {
				AllowedCharacters notAllowed = allowed.flip();
				warnings.add(new ValidationWarning(59, name, notAllowed.toString(true)));
			} else {
				warnings.add(new ValidationWarning(52, name));
			}
		}
	}

	@Override
	protected Map<String, Object> toStringValues() {
		Map<String, Object> values = new LinkedHashMap<String, Object>();
		values.put("name", name);
		values.put("value", value);
		values.put("dataType", dataType);
		return values;
	}

	@Override
	public RawProperty copy() {
		return new RawProperty(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((dataType == null) ? 0 : dataType.hashCode());
		result = prime * result + ((name == null) ? 0 : name.toLowerCase().hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		RawProperty other = (RawProperty) obj;
		if (dataType != other.dataType) return false;
		if (name == null) {
			if (other.name != null) return false;
		} else if (!name.equalsIgnoreCase(other.name)) return false;
		if (value == null) {
			if (other.value != null) return false;
		} else if (!value.equals(other.value)) return false;
		return true;
	}

}
