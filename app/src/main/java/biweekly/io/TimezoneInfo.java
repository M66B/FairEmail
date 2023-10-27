package biweekly.io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import biweekly.component.VTimezone;
import biweekly.property.ICalProperty;
import biweekly.property.TimezoneId;
import biweekly.property.ValuedProperty;

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
 * Holds the timezone-related settings of an iCalendar object.
 * @author Michael Angstadt
 */
public class TimezoneInfo {
	@SuppressWarnings("serial")
	private final Collection<TimezoneAssignment> assignments = new HashSet<TimezoneAssignment>() {
		@Override
		public boolean remove(Object assignment) {
			//remove all property assignments
			Collection<TimezoneAssignment> values = propertyTimezones.values();
			while (values.remove(assignment)) {
				//empty
			}

			return super.remove(assignment);
		}
	};
	private final Map<ICalProperty, TimezoneAssignment> propertyTimezones = new IdentityHashMap<ICalProperty, TimezoneAssignment>();
	private final List<ICalProperty> floatingProperties = new ArrayList<ICalProperty>();

	private TimezoneAssignment defaultTimezone;
	private boolean globalFloatingTime = false;

	/**
	 * Gets all the timezones assigned to this object.
	 * @return the timezones (collection is mutable)
	 */
	public Collection<TimezoneAssignment> getTimezones() {
		return assignments;
	}

	/**
	 * <p>
	 * Gets the timezone to format all date/time values in (by default, all
	 * dates are formatted in UTC).
	 * </p>
	 * <p>
	 * The default timezone is not used for properties that are configured to
	 * use their own timezone (see {@link #setTimezone}).
	 * </p>
	 * @return the timezone or null if using UTC
	 */
	public TimezoneAssignment getDefaultTimezone() {
		return defaultTimezone;
	}

	/**
	 * <p>
	 * Sets the timezone to format all date/time values in (by default, all
	 * dates are formatted in UTC).
	 * </p>
	 * <p>
	 * The default timezone is not used for properties that are configured to
	 * use their own timezone (see {@link #setTimezone}).
	 * </p>
	 * @param timezone the timezone or null to use UTC
	 */
	public void setDefaultTimezone(TimezoneAssignment timezone) {
		if (timezone == null) {
			if (defaultTimezone != null && !propertyTimezones.containsValue(defaultTimezone)) {
				assignments.remove(defaultTimezone);
			}
		} else {
			assignments.add(timezone);
		}

		defaultTimezone = timezone;
	}

	/**
	 * Assigns a timezone to a specific property.
	 * @param property the property
	 * @param timezone the timezone or null to format the property according to
	 * the default timezone (see {@link #setDefaultTimezone}).
	 */
	public void setTimezone(ICalProperty property, TimezoneAssignment timezone) {
		if (timezone == null) {
			TimezoneAssignment existing = propertyTimezones.remove(property);
			if (existing != null && existing != defaultTimezone && !propertyTimezones.containsValue(existing)) {
				assignments.remove(existing);
			}
			return;
		}

		assignments.add(timezone);
		propertyTimezones.put(property, timezone);
	}

	/**
	 * Gets the timezone that is assigned to a property.
	 * @param property the property
	 * @return the timezone or null if no timezone is assigned to the property
	 */
	public TimezoneAssignment getTimezone(ICalProperty property) {
		return propertyTimezones.get(property);
	}

	/**
	 * <p>
	 * Determines the timezone that a particular property should be formatted in
	 * when written to an output stream.
	 * </p>
	 * <p>
	 * Note: You should call {@link #isFloating} first, to determine if the
	 * property's value is floating (without a timezone).
	 * </p>
	 * @param property the property
	 * @return the timezone or null for UTC
	 */
	public TimezoneAssignment getTimezoneToWriteIn(ICalProperty property) {
		TimezoneAssignment assignment = getTimezone(property);
		return (assignment == null) ? defaultTimezone : assignment;
	}

	/**
	 * Gets the timezone whose {@link VTimezone} component contains a
	 * {@link TimezoneId} property with the given value.
	 * @param tzid the value of the {@link TimezoneId} property
	 * @return the timezone or null if not found
	 */
	public TimezoneAssignment getTimezoneById(String tzid) {
		for (TimezoneAssignment assignment : assignments) {
			VTimezone component = assignment.getComponent();
			if (component == null) {
				continue;
			}

			String componentId = ValuedProperty.getValue(component.getTimezoneId());
			if (tzid.equals(componentId)) {
				return assignment;
			}
		}
		return null;
	}

	/**
	 * <p>
	 * Gets whether to format all date/time values as floating times (defaults
	 * to false).
	 * </p>
	 * <p>
	 * This setting does not apply to properties whose floating time settings
	 * are configured individually (see: {@link #setFloating}) or that are
	 * configured to use their own timezone (see {@link #setTimezone}).
	 * </p>
	 * <p>
	 * A floating time value does not have a timezone associated with it, and is
	 * to be interpreted as being in the local timezone of the computer that is
	 * consuming the iCalendar object.
	 * </p>
	 * @return true if enabled, false if disabled
	 */
	public boolean isGlobalFloatingTime() {
		return globalFloatingTime;
	}

	/**
	 * <p>
	 * Sets whether to format all date/time values as floating times (defaults
	 * to false).
	 * </p>
	 * <p>
	 * This setting does not apply to properties whose floating time settings
	 * are configured individually (see: {@link #setFloating}) or that are
	 * configured to use their own timezone (see {@link #setTimezone}).
	 * </p>
	 * <p>
	 * A floating time value does not have a timezone associated with it, and is
	 * to be interpreted as being in the local timezone of the computer that is
	 * consuming the iCalendar object.
	 * </p>
	 * @param enable true to enable, false to disable
	 */
	public void setGlobalFloatingTime(boolean enable) {
		globalFloatingTime = enable;
	}

	/**
	 * Determines if a property value should be formatted in floating time when
	 * written to an output stream.
	 * @param property the property
	 * @return true to format in floating time, false not to
	 */
	public boolean isFloating(ICalProperty property) {
		if (containsIdentity(floatingProperties, property)) {
			return true;
		}

		if (propertyTimezones.containsKey(property)) {
			return false;
		}

		return globalFloatingTime;
	}

	/**
	 * <p>
	 * Sets whether a property value should be formatted in floating time when
	 * written to an output stream (by default, floating time is disabled for
	 * all properties).
	 * </p>
	 * <p>
	 * A floating time value does not have a timezone associated with it, and is
	 * to be interpreted as being in the local timezone of the computer that is
	 * consuming the iCalendar object.
	 * </p>
	 * @param property the property
	 * @param enable true to enable floating time for this property, false to
	 * disable
	 */
	public void setFloating(ICalProperty property, boolean enable) {
		if (enable) {
			floatingProperties.add(property);
		} else {
			removeIdentity(floatingProperties, property);
		}
	}

	/**
	 * Gets all of the iCalendar {@link VTimezone} components that have been
	 * registered with this object.
	 * @return the components (this collection is immutable)
	 */
	public Collection<VTimezone> getComponents() {
		List<VTimezone> components = new ArrayList<VTimezone>(assignments.size());
		for (TimezoneAssignment assignment : assignments) {
			VTimezone component = assignment.getComponent();
			if (component != null) {
				components.add(component);
			}
		}
		return Collections.unmodifiableList(components);
	}

	/**
	 * Removes an object from a list using reference equality.
	 * @param list the list
	 * @param object the object to remove
	 */
	private static <T> void removeIdentity(List<T> list, T object) {
		Iterator<T> it = list.iterator();
		while (it.hasNext()) {
			if (object == it.next()) {
				it.remove();
			}
		}
	}

	/**
	 * Searches for an item in a list using reference equality.
	 * @param list the list
	 * @param object the object to search for
	 * @return true if the object was found, false if not
	 */
	private static <T> boolean containsIdentity(List<T> list, T object) {
		for (T item : list) {
			if (item == object) {
				return true;
			}
		}
		return false;
	}
}