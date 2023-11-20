package biweekly.io.scribe.property;

import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import biweekly.ICalDataType;
import biweekly.ICalVersion;
import biweekly.component.VAlarm;
import biweekly.io.CannotParseException;
import biweekly.io.DataModelConversionException;
import biweekly.io.ParseContext;
import biweekly.io.WriteContext;
import biweekly.parameter.ICalParameters;
import biweekly.property.Action;
import biweekly.property.Trigger;
import biweekly.property.VCalAlarmProperty;
import biweekly.util.Duration;

import com.github.mangstadt.vinnie.io.VObjectPropertyValues;
import com.github.mangstadt.vinnie.io.VObjectPropertyValues.SemiStructuredValueIterator;

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
 * Marshals {@link VCalAlarmProperty} properties.
 * @author Michael Angstadt
 */
public abstract class VCalAlarmPropertyScribe<T extends VCalAlarmProperty> extends ICalPropertyScribe<T> {
	public VCalAlarmPropertyScribe(Class<T> clazz, String propertyName) {
		super(clazz, propertyName);
	}

	public VCalAlarmPropertyScribe(Class<T> clazz, String propertyName, ICalDataType defaultDataType) {
		super(clazz, propertyName, defaultDataType);
	}

	@Override
	protected String _writeText(T property, WriteContext context) {
		List<String> values = new ArrayList<String>();

		Date start = property.getStart();
		String value = date(start, property, context).extended(false).write();
		values.add(value);

		Duration snooze = property.getSnooze();
		value = (snooze == null) ? "" : snooze.toString();
		values.add(value);

		Integer repeat = property.getRepeat();
		value = (repeat == null) ? "" : repeat.toString();
		values.add(value);

		List<String> dataValues = writeData(property);
		values.addAll(dataValues);

		boolean escapeCommas = (context.getVersion() != ICalVersion.V1_0);
		return VObjectPropertyValues.writeSemiStructured(values, escapeCommas, true);
	}

	@Override
	protected T _parseText(String value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
		SemiStructuredValueIterator it = new SemiStructuredValueIterator(value);

		String next = next(it);
		Date start;
		try {
			start = (next == null) ? null : date(next).parse();
		} catch (IllegalArgumentException e) {
			throw new CannotParseException(27, next);
		}

		next = next(it);
		Duration snooze;
		try {
			snooze = (next == null) ? null : Duration.parse(next);
		} catch (IllegalArgumentException e) {
			throw new CannotParseException(26, next);
		}

		next = next(it);
		Integer repeat;
		try {
			repeat = (next == null) ? null : Integer.valueOf(next);
		} catch (IllegalArgumentException e) {
			throw new CannotParseException(24, next);
		}

		T property = create(dataType, it);
		property.setStart(start);
		property.setSnooze(snooze);
		property.setRepeat(repeat);
		property.setParameters(parameters);

		DataModelConversionException conversionException = new DataModelConversionException(property);
		VAlarm valarm = toVAlarm(property);
		conversionException.getComponents().add(valarm);
		throw conversionException;
	}

	private String next(SemiStructuredValueIterator it) {
		String next = it.next();
		if (next == null) {
			return null;
		}

		next = next.trim();
		return next.isEmpty() ? null : next;
	}

	/**
	 * Converts an instance of a vCal alarm property into a {@link VAlarm}
	 * component.
	 * @param property the property to convert
	 * @return the component
	 */
	protected VAlarm toVAlarm(T property) {
		Trigger trigger = new Trigger(property.getStart());
		VAlarm valarm = new VAlarm(action(), trigger);
		valarm.setDuration(property.getSnooze());
		valarm.setRepeat(property.getRepeat());

		toVAlarm(valarm, property);
		return valarm;
	}

	/**
	 * Generates the part of the property value that will be included after the
	 * part of the value that is common to all vCal alarm properties.
	 * @param property the property
	 * @return the values
	 */
	protected abstract List<String> writeData(T property);

	/**
	 * Creates a new instance of the property and populates it with the portion
	 * of data that is specific to this vCal alarm property.
	 * @param dataType the data type
	 * @param it an iterator to the property value that is positioned at the
	 * "value" portion of the property value (after the values that are common
	 * to all vCal alarm properties)
	 * @return the new property
	 */
	protected abstract T create(ICalDataType dataType, SemiStructuredValueIterator it);

	/**
	 * Determines what kind of {@link Action} property this vCal alarm property
	 * maps to, and returns a new instance of this property.
	 * @return a new {@link Action} property
	 */
	protected abstract Action action();

	/**
	 * Populates a {@link VAlarm} component with data that is unique to this
	 * specific kind of vCal alarm property.
	 * @param valarm the component
	 * @param property the property
	 */
	protected abstract void toVAlarm(VAlarm valarm, T property);

	@Override
	public Set<ICalVersion> getSupportedVersions() {
		return EnumSet.of(ICalVersion.V1_0);
	}
}
