package biweekly.io;

import java.util.ArrayList;
import java.util.List;

import biweekly.component.ICalComponent;
import biweekly.component.VAlarm;
import biweekly.property.AudioAlarm;
import biweekly.property.ICalProperty;

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
 * Thrown when a component or property needs to be converted to a different
 * component or property when being read or written. For example, converting a
 * vCal {@link AudioAlarm} property to a {@link VAlarm} component when parsing a
 * vCal file.
 * @author Michael Angstadt
 */
public class DataModelConversionException extends RuntimeException {
	private static final long serialVersionUID = -4789186852509057375L;
	private final ICalProperty originalProperty;
	private final List<ICalComponent> components = new ArrayList<ICalComponent>();
	private final List<ICalProperty> properties = new ArrayList<ICalProperty>();

	/**
	 * Creates a conversion exception.
	 * @param originalProperty the original property object that was parsed or
	 * null if not applicable
	 */
	public DataModelConversionException(ICalProperty originalProperty) {
		this.originalProperty = originalProperty;
	}

	/**
	 * Gets the original property object that was parsed.
	 * @return the original property object or null if not applicable
	 */
	public ICalProperty getOriginalProperty() {
		return originalProperty;
	}

	/**
	 * Gets the components that were converted from the original property.
	 * @return the components
	 */
	public List<ICalComponent> getComponents() {
		return components;
	}

	/**
	 * Gets the properties that were converted from the original property.
	 * @return the properties
	 */
	public List<ICalProperty> getProperties() {
		return properties;
	}
}
