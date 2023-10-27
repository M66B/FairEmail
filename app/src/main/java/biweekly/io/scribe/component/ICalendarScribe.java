package biweekly.io.scribe.component;

import java.util.ArrayList;
import java.util.List;

import biweekly.ICalendar;
import biweekly.property.ICalProperty;
import biweekly.property.ProductId;
import biweekly.property.Version;

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
 * @author Michael Angstadt
 */
public class ICalendarScribe extends ICalComponentScribe<ICalendar> {
	public ICalendarScribe() {
		super(ICalendar.class, "VCALENDAR");
	}

	@Override
	protected ICalendar _newInstance() {
		return new ICalendar();
	}

	@Override
	public List<ICalProperty> getProperties(ICalendar component) {
		List<ICalProperty> properties = new ArrayList<ICalProperty>(component.getProperties().values());

		/*
		 * Move VERSION properties to the front (if any are present), followed
		 * by PRODID properties. This is not required by the specs, but may help
		 * with interoperability because all the examples in the specs put the
		 * VERSION and PRODID at the very beginning of the iCalendar.
		 */
		moveToFront(ProductId.class, component, properties);
		moveToFront(Version.class, component, properties);

		return properties;
	}

	private <T extends ICalProperty> void moveToFront(Class<T> clazz, ICalendar component, List<ICalProperty> properties) {
		List<T> toMove = component.getProperties(clazz);
		properties.removeAll(toMove);
		properties.addAll(0, toMove);
	}
}
