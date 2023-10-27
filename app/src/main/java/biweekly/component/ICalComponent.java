package biweekly.component;

import java.lang.reflect.Constructor;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import biweekly.ICalDataType;
import biweekly.ICalVersion;
import biweekly.ICalendar;
import biweekly.Messages;
import biweekly.ValidationWarnings.WarningsGroup;
import biweekly.ValidationWarning;
import biweekly.property.ICalProperty;
import biweekly.property.RawProperty;
import biweekly.property.Status;
import biweekly.util.ListMultimap;
import biweekly.util.StringUtils;

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
 * Base class for all iCalendar component classes.
 * @author Michael Angstadt
 */
public abstract class ICalComponent {
	protected final ListMultimap<Class<? extends ICalComponent>, ICalComponent> components;
	protected final ListMultimap<Class<? extends ICalProperty>, ICalProperty> properties;

	public ICalComponent() {
		components = new ListMultimap<Class<? extends ICalComponent>, ICalComponent>();
		properties = new ListMultimap<Class<? extends ICalProperty>, ICalProperty>();
	}

	/**
	 * Copy constructor. Performs a deep copy of the given component's
	 * properties and sub-components.
	 * @param original the component to make a copy of
	 */
	protected ICalComponent(ICalComponent original) {
		this();
		for (ICalProperty property : original.properties.values()) {
			addProperty(property.copy());
		}
		for (ICalComponent component : original.components.values()) {
			addComponent(component.copy());
		}
	}

	/**
	 * Gets the first property of a given class.
	 * @param clazz the property class
	 * @param <T> the property class
	 * @return the property or null if not found
	 */
	public <T extends ICalProperty> T getProperty(Class<T> clazz) {
		return clazz.cast(properties.first(clazz));
	}

	/**
	 * Gets all properties of a given class. Changes to the returned list will
	 * update the {@link ICalComponent} object, and vice versa.
	 * @param clazz the property class
	 * @param <T> the property class
	 * @return the properties
	 */
	public <T extends ICalProperty> List<T> getProperties(Class<T> clazz) {
		return new ICalPropertyList<T>(clazz);
	}

	/**
	 * Gets all the properties associated with this component.
	 * @return the properties
	 */
	public ListMultimap<Class<? extends ICalProperty>, ICalProperty> getProperties() {
		return properties;
	}

	/**
	 * Adds a property to this component.
	 * @param property the property to add
	 */
	public void addProperty(ICalProperty property) {
		properties.put(property.getClass(), property);
	}

	/**
	 * Replaces all existing properties of the given property instance's class
	 * with the given property instance.
	 * @param property the property
	 * @return the replaced properties (this list is immutable)
	 */
	public List<ICalProperty> setProperty(ICalProperty property) {
		return properties.replace(property.getClass(), property);
	}

	/**
	 * Replaces all existing properties of the given class with a single
	 * property instance. If the property instance is null, then all instances
	 * of that property will be removed.
	 * @param clazz the property class (e.g. "DateStart.class")
	 * @param property the property or null to remove all properties of the
	 * given class
	 * @param <T> the property class
	 * @return the replaced properties (this list is immutable)
	 */
	public <T extends ICalProperty> List<T> setProperty(Class<T> clazz, T property) {
		List<ICalProperty> replaced = properties.replace(clazz, property);
		return castList(replaced, clazz);
	}

	/**
	 * Removes a specific property instance from this component.
	 * @param property the property to remove
	 * @param <T> the property class
	 * @return true if it was removed, false if it wasn't found
	 */
	public <T extends ICalProperty> boolean removeProperty(T property) {
		return properties.remove(property.getClass(), property);
	}

	/**
	 * Removes all properties of a given class from this component.
	 * @param clazz the class of the properties to remove (e.g.
	 * "DateStart.class")
	 * @param <T> the property class
	 * @return the removed properties (this list is immutable)
	 */
	public <T extends ICalProperty> List<T> removeProperties(Class<T> clazz) {
		List<ICalProperty> removed = properties.removeAll(clazz);
		return castList(removed, clazz);
	}

	/**
	 * Removes a specific sub-component instance from this component.
	 * @param component the component to remove
	 * @param <T> the component class
	 * @return true if it was removed, false if it wasn't found
	 */
	public <T extends ICalComponent> boolean removeComponent(T component) {
		return components.remove(component.getClass(), component);
	}

	/**
	 * Removes all sub-components of the given class from this component.
	 * @param clazz the class of the components to remove (e.g. "VEvent.class")
	 * @param <T> the component class
	 * @return the removed components (this list is immutable)
	 */
	public <T extends ICalComponent> List<T> removeComponents(Class<T> clazz) {
		List<ICalComponent> removed = components.removeAll(clazz);
		return castList(removed, clazz);
	}

	/**
	 * Gets the first experimental property with a given name.
	 * @param name the property name (case insensitive, e.g. "X-ALT-DESC")
	 * @return the experimental property or null if none were found
	 */
	public RawProperty getExperimentalProperty(String name) {
		for (RawProperty raw : getExperimentalProperties()) {
			if (raw.getName().equalsIgnoreCase(name)) {
				return raw;
			}
		}
		return null;
	}

	/**
	 * Gets all experimental properties with a given name.
	 * @param name the property name (case insensitive, e.g. "X-ALT-DESC")
	 * @return the experimental properties (this list is immutable)
	 */
	public List<RawProperty> getExperimentalProperties(String name) {
		/*
		 * Note: The returned list is not backed by the parent component because
		 * this would allow RawProperty objects without the specified name to be
		 * added to the list.
		 */
		List<RawProperty> toReturn = new ArrayList<RawProperty>();
		for (RawProperty property : getExperimentalProperties()) {
			if (property.getName().equalsIgnoreCase(name)) {
				toReturn.add(property);
			}
		}
		return Collections.unmodifiableList(toReturn);
	}

	/**
	 * Gets all experimental properties associated with this component. Changes
	 * to the returned list will update the {@link ICalComponent} object, and
	 * vice versa.
	 * @return the experimental properties
	 */
	public List<RawProperty> getExperimentalProperties() {
		return getProperties(RawProperty.class);
	}

	/**
	 * Adds an experimental property to this component.
	 * @param name the property name (e.g. "X-ALT-DESC")
	 * @param value the property value
	 * @return the property object that was created
	 */
	public RawProperty addExperimentalProperty(String name, String value) {
		return addExperimentalProperty(name, null, value);
	}

	/**
	 * Adds an experimental property to this component.
	 * @param name the property name (e.g. "X-ALT-DESC")
	 * @param dataType the property's data type or null if unknown
	 * @param value the property value
	 * @return the property object that was created
	 */
	public RawProperty addExperimentalProperty(String name, ICalDataType dataType, String value) {
		RawProperty raw = new RawProperty(name, dataType, value);
		addProperty(raw);
		return raw;
	}

	/**
	 * Adds an experimental property to this component, removing all existing
	 * properties that have the same name.
	 * @param name the property name (e.g. "X-ALT-DESC")
	 * @param value the property value
	 * @return the property object that was created
	 */
	public RawProperty setExperimentalProperty(String name, String value) {
		return setExperimentalProperty(name, null, value);
	}

	/**
	 * Adds an experimental property to this component, removing all existing
	 * properties that have the same name.
	 * @param name the property name (e.g. "X-ALT-DESC")
	 * @param dataType the property's data type or null if unknown
	 * @param value the property value
	 * @return the property object that was created
	 */
	public RawProperty setExperimentalProperty(String name, ICalDataType dataType, String value) {
		removeExperimentalProperties(name);
		return addExperimentalProperty(name, dataType, value);
	}

	/**
	 * Removes all experimental properties that have the given name.
	 * @param name the component name (e.g. "X-ALT-DESC")
	 * @return the removed properties (this list is immutable)
	 */
	public List<RawProperty> removeExperimentalProperties(String name) {
		List<RawProperty> all = getExperimentalProperties();
		List<RawProperty> toRemove = new ArrayList<RawProperty>();
		for (RawProperty property : all) {
			if (property.getName().equalsIgnoreCase(name)) {
				toRemove.add(property);
			}
		}

		all.removeAll(toRemove);
		return Collections.unmodifiableList(toRemove);
	}

	/**
	 * Gets the first sub-component of a given class.
	 * @param clazz the component class
	 * @param <T> the component class
	 * @return the sub-component or null if not found
	 */
	public <T extends ICalComponent> T getComponent(Class<T> clazz) {
		return clazz.cast(components.first(clazz));
	}

	/**
	 * Gets all sub-components of a given class. Changes to the returned list
	 * will update the parent component object, and vice versa.
	 * @param clazz the component class
	 * @param <T> the component class
	 * @return the sub-components
	 */
	public <T extends ICalComponent> List<T> getComponents(Class<T> clazz) {
		return new ICalComponentList<T>(clazz);
	}

	/**
	 * Gets all the sub-components associated with this component.
	 * @return the sub-components
	 */
	public ListMultimap<Class<? extends ICalComponent>, ICalComponent> getComponents() {
		return components;
	}

	/**
	 * Adds a sub-component to this component.
	 * @param component the component to add
	 */
	public void addComponent(ICalComponent component) {
		components.put(component.getClass(), component);
	}

	/**
	 * Replaces all sub-components of a given class with the given component.
	 * @param component the component
	 * @return the replaced sub-components (this list is immutable)
	 */
	public List<ICalComponent> setComponent(ICalComponent component) {
		return components.replace(component.getClass(), component);
	}

	/**
	 * Replaces all sub-components of a given class with the given component. If
	 * the component instance is null, then all instances of that component will
	 * be removed.
	 * @param clazz the component's class
	 * @param component the component or null to remove all components of the
	 * given class
	 * @param <T> the component class
	 * @return the replaced sub-components (this list is immutable)
	 */
	public <T extends ICalComponent> List<T> setComponent(Class<T> clazz, T component) {
		List<ICalComponent> replaced = components.replace(clazz, component);
		return castList(replaced, clazz);
	}

	/**
	 * Gets the first experimental sub-component with a given name.
	 * @param name the component name (case insensitive, e.g. "X-PARTY")
	 * @return the experimental component or null if none were found
	 */
	public RawComponent getExperimentalComponent(String name) {
		for (RawComponent component : getExperimentalComponents()) {
			if (component.getName().equalsIgnoreCase(name)) {
				return component;
			}
		}
		return null;
	}

	/**
	 * Gets all experimental sub-component with a given name.
	 * @param name the component name (case insensitive, e.g. "X-PARTY")
	 * @return the experimental components (this list is immutable)
	 */
	public List<RawComponent> getExperimentalComponents(String name) {
		/*
		 * Note: The returned list is not backed by the parent component because
		 * this would allow RawComponent objects without the specified name to
		 * be added to the list.
		 */
		List<RawComponent> toReturn = new ArrayList<RawComponent>();
		for (RawComponent component : getExperimentalComponents()) {
			if (component.getName().equalsIgnoreCase(name)) {
				toReturn.add(component);
			}
		}
		return Collections.unmodifiableList(toReturn);
	}

	/**
	 * Gets all experimental sub-components associated with this component.
	 * Changes to the returned list will update the parent {@link ICalComponent}
	 * object, and vice versa.
	 * @return the experimental components
	 */
	public List<RawComponent> getExperimentalComponents() {
		return getComponents(RawComponent.class);
	}

	/**
	 * Adds an experimental sub-component to this component.
	 * @param name the component name (e.g. "X-PARTY")
	 * @return the component object that was created
	 */
	public RawComponent addExperimentalComponent(String name) {
		RawComponent raw = new RawComponent(name);
		addComponent(raw);
		return raw;
	}

	/**
	 * Adds an experimental sub-component to this component, removing all
	 * existing components that have the same name.
	 * @param name the component name (e.g. "X-PARTY")
	 * @return the component object that was created
	 */
	public RawComponent setExperimentalComponent(String name) {
		removeExperimentalComponents(name);
		return addExperimentalComponent(name);
	}

	/**
	 * Removes all experimental sub-components that have the given name.
	 * @param name the component name (e.g. "X-PARTY")
	 * @return the removed sub-components (this list is immutable)
	 */
	public List<RawComponent> removeExperimentalComponents(String name) {
		List<RawComponent> all = getExperimentalComponents();
		List<RawComponent> toRemove = new ArrayList<RawComponent>();
		for (RawComponent property : all) {
			if (property.getName().equalsIgnoreCase(name)) {
				toRemove.add(property);
			}
		}

		all.removeAll(toRemove);
		return Collections.unmodifiableList(toRemove);
	}

	/**
	 * <p>
	 * Checks this component for data consistency problems or deviations from
	 * the specifications.
	 * </p>
	 * <p>
	 * The existence of validation warnings will not prevent the component
	 * object from being written to a data stream. Syntactically-correct output
	 * will still be produced. However, the consuming application may have
	 * trouble interpreting some of the data due to the presence of these
	 * warnings.
	 * </p>
	 * <p>
	 * These problems can largely be avoided by reading the Javadocs of the
	 * component and property classes, or by being familiar with the iCalendar
	 * standard.
	 * </p>
	 * @param hierarchy the hierarchy of components that the component belongs
	 * to
	 * @param version the version to validate against
	 * @see ICalendar#validate(List, ICalVersion)
	 * @return a list of warnings or an empty list if no problems were found
	 */
	public final List<WarningsGroup> validate(List<ICalComponent> hierarchy, ICalVersion version) {
		List<WarningsGroup> warnings = new ArrayList<WarningsGroup>();

		//validate this component
		List<ValidationWarning> warningsBuf = new ArrayList<ValidationWarning>(0);
		validate(hierarchy, version, warningsBuf);
		if (!warningsBuf.isEmpty()) {
			warnings.add(new WarningsGroup(this, hierarchy, warningsBuf));
		}

		//add this component to the hierarchy list
		//copy the list so other validate() calls aren't effected
		hierarchy = new ArrayList<ICalComponent>(hierarchy);
		hierarchy.add(this);

		//validate properties
		for (ICalProperty property : properties.values()) {
			List<ValidationWarning> propWarnings = property.validate(hierarchy, version);
			if (!propWarnings.isEmpty()) {
				warnings.add(new WarningsGroup(property, hierarchy, propWarnings));
			}
		}

		//validate sub-components
		for (ICalComponent component : components.values()) {
			warnings.addAll(component.validate(hierarchy, version));
		}

		return warnings;
	}

	/**
	 * <p>
	 * Checks the component for data consistency problems or deviations from the
	 * spec.
	 * </p>
	 * <p>
	 * This method should be overridden by child classes that wish to provide
	 * validation logic. The default implementation of this method does nothing.
	 * </p>
	 * @param components the hierarchy of components that the component belongs
	 * to
	 * @param version the version to validate against
	 * @param warnings the list to add the warnings to
	 */
	protected void validate(List<ICalComponent> components, ICalVersion version, List<ValidationWarning> warnings) {
		//do nothing
	}

	/**
	 * Utility method for validating that there is exactly one instance of each
	 * of the given properties.
	 * @param warnings the list to add the warnings to
	 * @param classes the properties to check
	 */
	protected void checkRequiredCardinality(List<ValidationWarning> warnings, Class<? extends ICalProperty>... classes) {
		for (Class<? extends ICalProperty> clazz : classes) {
			List<? extends ICalProperty> props = getProperties(clazz);

			if (props.isEmpty()) {
				warnings.add(new ValidationWarning(2, clazz.getSimpleName()));
				continue;
			}

			if (props.size() > 1) {
				warnings.add(new ValidationWarning(3, clazz.getSimpleName()));
				continue;
			}
		}
	}

	/**
	 * Utility method for validating that there is no more than one instance of
	 * each of the given properties.
	 * @param warnings the list to add the warnings to
	 * @param classes the properties to check
	 */
	protected void checkOptionalCardinality(List<ValidationWarning> warnings, Class<? extends ICalProperty>... classes) {
		for (Class<? extends ICalProperty> clazz : classes) {
			List<? extends ICalProperty> props = getProperties(clazz);

			if (props.size() > 1) {
				warnings.add(new ValidationWarning(3, clazz.getSimpleName()));
				continue;
			}
		}
	}

	/**
	 * Utility method for validating the {@link Status} property of a component.
	 * @param warnings the list to add the warnings to
	 * @param allowed the valid statuses
	 */
	protected void checkStatus(List<ValidationWarning> warnings, Status... allowed) {
		Status actual = getProperty(Status.class);
		if (actual == null) {
			return;
		}

		List<String> allowedValues = new ArrayList<String>(allowed.length);
		for (Status status : allowed) {
			String value = status.getValue().toLowerCase();
			allowedValues.add(value);
		}

		String actualValue = actual.getValue().toLowerCase();
		if (!allowedValues.contains(actualValue)) {
			warnings.add(new ValidationWarning(13, actual.getValue(), allowedValues));
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		toString(0, sb);
		return sb.toString();
	}

	/**
	 * <p>
	 * Gets string representations of any additional fields the component has
	 * (other than sub-components and properties) for the {@link #toString}
	 * method.
	 * </p>
	 * <p>
	 * Meant to be overridden by child classes. The default implementation
	 * returns an empty map.
	 * </p>
	 * @return the values of the component's fields (key = field name, value =
	 * field value)
	 */
	protected Map<String, Object> toStringValues() {
		return Collections.emptyMap();
	}

	private void toString(int depth, StringBuilder sb) {
		StringUtils.repeat(' ', depth * 2, sb);
		sb.append(getClass().getName());

		Map<String, Object> fields = toStringValues();
		if (!fields.isEmpty()) {
			sb.append(' ').append(fields.toString());
		}

		sb.append(StringUtils.NEWLINE);

		depth++;
		for (ICalProperty property : properties.values()) {
			StringUtils.repeat(' ', depth * 2, sb);
			sb.append(property).append(StringUtils.NEWLINE);
		}
		for (ICalComponent component : components.values()) {
			component.toString(depth, sb);
		}
	}

	/**
	 * <p>
	 * Creates a deep copy of this component object.
	 * </p>
	 * <p>
	 * The default implementation of this method uses reflection to look for a
	 * copy constructor. Child classes SHOULD override this method to avoid the
	 * performance overhead involved in using reflection.
	 * </p>
	 * <p>
	 * The child class's copy constructor, if present, MUST invoke the
	 * {@link #ICalComponent(ICalComponent)} super constructor to ensure that
	 * the component's properties and sub-components are copied.
	 * </p>
	 * <p>
	 * This method MUST be overridden by the child class if the child class does
	 * not have a copy constructor. Otherwise, an
	 * {@link UnsupportedOperationException} will be thrown when an attempt is
	 * made to copy the component (such as in the
	 * {@link ICalendar#ICalendar(ICalendar) ICalendar class's copy constructor}
	 * ).
	 * </p>
	 * @return the copy
	 * @throws UnsupportedOperationException if the class does not have a copy
	 * constructor or there is a problem invoking it
	 */
	public ICalComponent copy() {
		Class<? extends ICalComponent> clazz = getClass();

		try {
			Constructor<? extends ICalComponent> copyConstructor = clazz.getConstructor(clazz);
			return copyConstructor.newInstance(this);
		} catch (Exception e) {
			throw new UnsupportedOperationException(Messages.INSTANCE.getExceptionMessage(1, clazz.getName()), e);
		}
	}

	/**
	 * Casts all objects in the given list to the given class, adding the casted
	 * objects to a new list.
	 * @param list the list to cast
	 * @param castTo the class to cast to
	 * @param <T> the class to cast to
	 * @return the new list (immutable)
	 */
	private static <T> List<T> castList(List<?> list, Class<T> castTo) {
		List<T> casted = new ArrayList<T>(list.size());
		for (Object object : list) {
			casted.add(castTo.cast(object));
		}
		return Collections.unmodifiableList(casted);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;

		int propertiesHash = 1;
		for (ICalProperty property : properties.values()) {
			propertiesHash += property.hashCode();
		}
		result = prime * result + propertiesHash;

		int componentsHash = 1;
		for (ICalComponent component : components.values()) {
			componentsHash += component.hashCode();
		}
		result = prime * result + componentsHash;

		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		ICalComponent other = (ICalComponent) obj;

		if (properties.size() != other.properties.size()) return false;
		if (components.size() != other.components.size()) return false;

		if (!compareMultimaps(properties, other.properties)) return false;
		if (!compareMultimaps(components, other.components)) return false;

		return true;
	}

	private static <K, V> boolean compareMultimaps(ListMultimap<K, V> map1, ListMultimap<K, V> map2) {
		for (Map.Entry<K, List<V>> entry : map1) {
			K key = entry.getKey();
			List<V> value = entry.getValue();
			List<V> otherValue = map2.get(key);

			if (value.size() != otherValue.size()) {
				return false;
			}

			List<V> otherValueCopy = new ArrayList<V>(otherValue);
			for (V property : value) {
				if (!otherValueCopy.remove(property)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * <p>
	 * A list that automatically casts {@link ICalComponent} instances stored in
	 * this component to a given component class.
	 * </p>
	 * <p>
	 * This list is backed by the {@link ICalComponent} object. Any changes made
	 * to the list will affect the {@link ICalComponent} object and vice versa.
	 * </p>
	 * @param <T> the component class
	 */
	private class ICalComponentList<T extends ICalComponent> extends AbstractList<T> {
		protected final Class<T> componentClass;
		protected final List<ICalComponent> components;

		/**
		 * @param componentClass the component class
		 */
		public ICalComponentList(Class<T> componentClass) {
			this.componentClass = componentClass;
			components = ICalComponent.this.components.get(componentClass);
		}

		@Override
		public void add(int index, T value) {
			components.add(index, value);
		}

		@Override
		public T remove(int index) {
			ICalComponent removed = components.remove(index);
			return cast(removed);
		}

		@Override
		public T get(int index) {
			ICalComponent property = components.get(index);
			return cast(property);
		}

		@Override
		public T set(int index, T value) {
			ICalComponent replaced = components.set(index, value);
			return cast(replaced);
		}

		@Override
		public int size() {
			return components.size();
		}

		protected T cast(ICalComponent value) {
			return componentClass.cast(value);
		}
	}

	/**
	 * <p>
	 * A list that automatically casts {@link ICalProperty} instances stored in
	 * this component to a given property class.
	 * </p>
	 * <p>
	 * This list is backed by the {@link ICalComponent} object. Any changes made
	 * to the list will affect the {@link ICalComponent} object and vice versa.
	 * </p>
	 * @param <T> the property class
	 */
	private class ICalPropertyList<T extends ICalProperty> extends AbstractList<T> {
		protected final Class<T> propertyClass;
		protected final List<ICalProperty> properties;

		/**
		 * @param propertyClass the property class
		 */
		public ICalPropertyList(Class<T> propertyClass) {
			this.propertyClass = propertyClass;
			properties = ICalComponent.this.properties.get(propertyClass);
		}

		@Override
		public void add(int index, T value) {
			properties.add(index, value);
		}

		@Override
		public T remove(int index) {
			ICalProperty removed = properties.remove(index);
			return cast(removed);
		}

		@Override
		public T get(int index) {
			ICalProperty property = properties.get(index);
			return cast(property);
		}

		@Override
		public T set(int index, T value) {
			ICalProperty replaced = properties.set(index, value);
			return cast(replaced);
		}

		@Override
		public int size() {
			return properties.size();
		}

		protected T cast(ICalProperty value) {
			return propertyClass.cast(value);
		}
	}
}
