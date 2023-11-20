package biweekly;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import biweekly.ValidationWarnings.WarningsGroup;
import biweekly.component.ICalComponent;
import biweekly.property.ICalProperty;
import biweekly.util.StringUtils;
import biweekly.util.StringUtils.JoinCallback;

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
 * Holds the validation warnings of an iCalendar object.
 * </p>
 * <p>
 * <b>Examples:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * //validate an iCalendar object
 * ValidationWarnings warnings = ical.validate();
 * 
 * //print all warnings to a string:
 * System.out.println(warnings.toString());
 * //sample output:
 * //[ICalendar]: ProductId is not set (it is a required property).
 * //[ICalendar &gt; VEvent &gt; DateStart]: DateStart must come before DateEnd.
 * //[ICalendar &gt; VEvent &gt; VAlarm]: The trigger must specify which date field its duration is relative to.
 * 
 * //iterate over each warnings group
 * //this gives you access to the property/component object and its parent components
 * for (WarningsGroup group : warnings) {
 * ICalProperty prop = group.getProperty();
 *   if (prop == null) {
 *     //then it was a component that caused the warnings
 *     ICalComponent comp = group.getComponent();
 *   }
 * 
 *   //get parent components
 *   List&lt;ICalComponent&gt; hierarchy = group.getComponentHierarchy();
 * 
 *   //get warning messages
 *   List&lt;String&gt; messages = group.getMessages();
 * }
 * 
 * //you can also get the warnings of specific properties/components
 * List&lt;WarningsGroup&gt; dtstartWarnings = warnings.getByProperty(DateStart.class);
 * List&lt;WarningsGroup&gt; veventWarnings = warnings.getByComponent(VEvent.class);
 * </pre>
 * @author Michael Angstadt
 * @see ICalendar#validate(ICalVersion)
 */
public class ValidationWarnings implements Iterable<WarningsGroup> {
	private final List<WarningsGroup> warnings;

	/**
	 * Creates a new validation warnings list.
	 * @param warnings the validation warnings
	 */
	public ValidationWarnings(List<WarningsGroup> warnings) {
		this.warnings = warnings;
	}

	/**
	 * Gets all validation warnings of a given property.
	 * @param propertyClass the property (e.g. {@code DateStart.class})
	 * @return the validation warnings
	 */
	public List<WarningsGroup> getByProperty(Class<? extends ICalProperty> propertyClass) {
		List<WarningsGroup> warnings = new ArrayList<WarningsGroup>();
		for (WarningsGroup group : this.warnings) {
			ICalProperty property = group.getProperty();
			if (property == null) {
				continue;
			}

			if (propertyClass == property.getClass()) {
				warnings.add(group);
			}
		}
		return warnings;
	}

	/**
	 * Gets all validation warnings of a given component.
	 * @param componentClass the component (e.g. {@code VEvent.class})
	 * @return the validation warnings
	 */
	public List<WarningsGroup> getByComponent(Class<? extends ICalComponent> componentClass) {
		List<WarningsGroup> warnings = new ArrayList<WarningsGroup>();
		for (WarningsGroup group : this.warnings) {
			ICalComponent component = group.getComponent();
			if (component == null) {
				continue;
			}

			if (componentClass == component.getClass()) {
				warnings.add(group);
			}
		}
		return warnings;
	}

	/**
	 * Gets all the validation warnings.
	 * @return the validation warnings
	 */
	public List<WarningsGroup> getWarnings() {
		return warnings;
	}

	/**
	 * Determines whether there are any validation warnings.
	 * @return true if there are none, false if there are one or more
	 */
	public boolean isEmpty() {
		return warnings.isEmpty();
	}

	/**
	 * <p>
	 * Outputs all validation warnings as a newline-delimited string. For
	 * example:
	 * </p>
	 * 
	 * <pre>
	 * [ICalendar]: ProductId is not set (it is a required property).
	 * [ICalendar &gt; VEvent &gt; DateStart]: DateStart must come before DateEnd.
	 * [ICalendar &gt; VEvent &gt; VAlarm]: The trigger must specify which date field its duration is relative to.
	 * </pre>
	 */
	@Override
	public String toString() {
		return StringUtils.join(warnings, StringUtils.NEWLINE);
	}

	/**
	 * Iterates over each warning group (same as calling
	 * {@code getWarnings().iterator()}).
	 * @return the iterator
	 */
	public Iterator<WarningsGroup> iterator() {
		return warnings.iterator();
	}

	/**
	 * Holds the validation warnings of a property or component.
	 * @author Michael Angstadt
	 */
	public static class WarningsGroup {
		private final ICalProperty property;
		private final ICalComponent component;
		private final List<ICalComponent> componentHierarchy;
		private final List<ValidationWarning> warnings;

		/**
		 * Creates a new set of validation warnings for a property.
		 * @param property the property that caused the warnings
		 * @param componentHierarchy the hierarchy of components that the
		 * property belongs to
		 * @param warning the warnings
		 */
		public WarningsGroup(ICalProperty property, List<ICalComponent> componentHierarchy, List<ValidationWarning> warning) {
			this(null, property, componentHierarchy, warning);
		}

		/**
		 * Creates a new set of validation warnings for a component.
		 * @param component the component that caused the warnings
		 * @param componentHierarchy the hierarchy of components that the
		 * component belongs to
		 * @param warning the warnings
		 */
		public WarningsGroup(ICalComponent component, List<ICalComponent> componentHierarchy, List<ValidationWarning> warning) {
			this(component, null, componentHierarchy, warning);
		}

		private WarningsGroup(ICalComponent component, ICalProperty property, List<ICalComponent> componentHierarchy, List<ValidationWarning> warning) {
			this.component = component;
			this.property = property;
			this.componentHierarchy = componentHierarchy;
			this.warnings = warning;
		}

		/**
		 * Gets the property object that caused the validation warnings.
		 * @return the property object or null if a component caused the
		 * warnings.
		 */
		public ICalProperty getProperty() {
			return property;
		}

		/**
		 * Gets the component object that caused the validation warnings.
		 * @return the component object or null if a property caused the
		 * warnings.
		 */
		public ICalComponent getComponent() {
			return component;
		}

		/**
		 * Gets the hierarchy of components that the property or component
		 * belongs to.
		 * @return the component hierarchy
		 */
		public List<ICalComponent> getComponentHierarchy() {
			return componentHierarchy;
		}

		/**
		 * Gets the warnings that belong to the property or component.
		 * @return the warnings
		 */
		public List<ValidationWarning> getWarnings() {
			return warnings;
		}

		/**
		 * <p>
		 * Outputs each message in this warnings group as a newline-delimited
		 * string. Each line includes the component hierarchy and the name of
		 * the property/component. For example:
		 * </p>
		 * 
		 * <pre>
		 * [ICalendar &gt; VEvent &gt; VAlarm]: Email alarms must have at least one attendee.
		 * [ICalendar &gt; VEvent &gt; VAlarm]: The trigger must specify which date field its duration is relative to.
		 * </pre>
		 */
		@Override
		public String toString() {
			final String prefix = "[" + buildPath() + "]: ";
			return StringUtils.join(warnings, StringUtils.NEWLINE, new JoinCallback<ValidationWarning>() {
				public void handle(StringBuilder sb, ValidationWarning warning) {
					sb.append(prefix).append(warning);
				}
			});
		}

		private String buildPath() {
			StringBuilder sb = new StringBuilder();

			if (!componentHierarchy.isEmpty()) {
				String delimitor = " > ";

				StringUtils.join(componentHierarchy, delimitor, sb, new JoinCallback<ICalComponent>() {
					public void handle(StringBuilder sb, ICalComponent component) {
						sb.append(component.getClass().getSimpleName());
					}
				});
				sb.append(delimitor);
			}

			Object obj = (property == null) ? component : property;
			sb.append(obj.getClass().getSimpleName());

			return sb.toString();
		}
	}
}
