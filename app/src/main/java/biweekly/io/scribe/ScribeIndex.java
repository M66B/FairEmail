package biweekly.io.scribe;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import biweekly.ICalVersion;
import biweekly.ICalendar;
import biweekly.component.ICalComponent;
import biweekly.component.RawComponent;
import biweekly.io.scribe.component.DaylightSavingsTimeScribe;
import biweekly.io.scribe.component.ICalComponentScribe;
import biweekly.io.scribe.component.ICalendarScribe;
import biweekly.io.scribe.component.RawComponentScribe;
import biweekly.io.scribe.component.StandardTimeScribe;
import biweekly.io.scribe.component.VAlarmScribe;
import biweekly.io.scribe.component.VEventScribe;
import biweekly.io.scribe.component.VFreeBusyScribe;
import biweekly.io.scribe.component.VJournalScribe;
import biweekly.io.scribe.component.VTimezoneScribe;
import biweekly.io.scribe.component.VTodoScribe;
import biweekly.io.scribe.property.ActionScribe;
import biweekly.io.scribe.property.AttachmentScribe;
import biweekly.io.scribe.property.AttendeeScribe;
import biweekly.io.scribe.property.AudioAlarmScribe;
import biweekly.io.scribe.property.CalendarScaleScribe;
import biweekly.io.scribe.property.CategoriesScribe;
import biweekly.io.scribe.property.ClassificationScribe;
import biweekly.io.scribe.property.ColorScribe;
import biweekly.io.scribe.property.CommentScribe;
import biweekly.io.scribe.property.CompletedScribe;
import biweekly.io.scribe.property.ConferenceScribe;
import biweekly.io.scribe.property.ContactScribe;
import biweekly.io.scribe.property.CreatedScribe;
import biweekly.io.scribe.property.DateDueScribe;
import biweekly.io.scribe.property.DateEndScribe;
import biweekly.io.scribe.property.DateStartScribe;
import biweekly.io.scribe.property.DateTimeStampScribe;
import biweekly.io.scribe.property.DaylightScribe;
import biweekly.io.scribe.property.DescriptionScribe;
import biweekly.io.scribe.property.DisplayAlarmScribe;
import biweekly.io.scribe.property.DurationPropertyScribe;
import biweekly.io.scribe.property.EmailAlarmScribe;
import biweekly.io.scribe.property.ExceptionDatesScribe;
import biweekly.io.scribe.property.ExceptionRuleScribe;
import biweekly.io.scribe.property.FreeBusyScribe;
import biweekly.io.scribe.property.GeoScribe;
import biweekly.io.scribe.property.ICalPropertyScribe;
import biweekly.io.scribe.property.ImageScribe;
import biweekly.io.scribe.property.LastModifiedScribe;
import biweekly.io.scribe.property.LocationScribe;
import biweekly.io.scribe.property.MethodScribe;
import biweekly.io.scribe.property.NameScribe;
import biweekly.io.scribe.property.OrganizerScribe;
import biweekly.io.scribe.property.PercentCompleteScribe;
import biweekly.io.scribe.property.PriorityScribe;
import biweekly.io.scribe.property.ProcedureAlarmScribe;
import biweekly.io.scribe.property.ProductIdScribe;
import biweekly.io.scribe.property.RawPropertyScribe;
import biweekly.io.scribe.property.RecurrenceDatesScribe;
import biweekly.io.scribe.property.RecurrenceIdScribe;
import biweekly.io.scribe.property.RecurrenceRuleScribe;
import biweekly.io.scribe.property.RefreshIntervalScribe;
import biweekly.io.scribe.property.RelatedToScribe;
import biweekly.io.scribe.property.RepeatScribe;
import biweekly.io.scribe.property.RequestStatusScribe;
import biweekly.io.scribe.property.ResourcesScribe;
import biweekly.io.scribe.property.SequenceScribe;
import biweekly.io.scribe.property.SourceScribe;
import biweekly.io.scribe.property.StatusScribe;
import biweekly.io.scribe.property.SummaryScribe;
import biweekly.io.scribe.property.TimezoneIdScribe;
import biweekly.io.scribe.property.TimezoneNameScribe;
import biweekly.io.scribe.property.TimezoneOffsetFromScribe;
import biweekly.io.scribe.property.TimezoneOffsetToScribe;
import biweekly.io.scribe.property.TimezoneScribe;
import biweekly.io.scribe.property.TimezoneUrlScribe;
import biweekly.io.scribe.property.TransparencyScribe;
import biweekly.io.scribe.property.TriggerScribe;
import biweekly.io.scribe.property.UidScribe;
import biweekly.io.scribe.property.UrlScribe;
import biweekly.io.scribe.property.VersionScribe;
import biweekly.io.scribe.property.XmlScribe;
import biweekly.io.xml.XCalNamespaceContext;
import biweekly.property.ICalProperty;
import biweekly.property.RawProperty;
import biweekly.property.Xml;

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
 * Manages a listing of component and property scribes. This is useful for
 * injecting the scribes of any experimental components or properties you have
 * defined into a reader or writer object. The same ScribeIndex instance can be
 * reused and injected into multiple reader/writer classes.
 * </p>
 * <p>
 * <b>Example:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * //init the index
 * ScribeIndex index = new ScribeIndex();
 * index.register(new CustomPropertyScribe());
 * index.register(new AnotherCustomPropertyScribe());
 * index.register(new CustomComponentScribe());
 * 
 * //inject into a reader class
 * ICalReader reader = new ICalReader(...);
 * reader.setScribeIndex(index);
 * List&lt;ICalendar&gt; icals = new ArrayList&lt;ICalendar&gt;();
 * ICalendar ical;
 * while ((ical = reader.readNext()) != null) {
 *   icals.add(ical);
 * }
 * 
 * //inject the same instance in another reader/writer class
 * JCalWriter writer = new JCalWriter(...);
 * writer.setScribeIndex(index);
 * for (ICalendar ical : icals) {
 *   writer.write(ical);
 * }
 * </pre>
 * @author Michael Angstadt
 */
public class ScribeIndex {
	//define standard component scribes
	private static final Map<String, ICalComponentScribe<? extends ICalComponent>> standardCompByName = new HashMap<String, ICalComponentScribe<? extends ICalComponent>>();
	private static final Map<Class<? extends ICalComponent>, ICalComponentScribe<? extends ICalComponent>> standardCompByClass = new HashMap<Class<? extends ICalComponent>, ICalComponentScribe<? extends ICalComponent>>();
	static {
		registerStandard(new ICalendarScribe());
		registerStandard(new VAlarmScribe());
		registerStandard(new VEventScribe());
		registerStandard(new VFreeBusyScribe());
		registerStandard(new VJournalScribe());
		registerStandard(new VTodoScribe());
		registerStandard(new VTimezoneScribe());
		registerStandard(new StandardTimeScribe());
		registerStandard(new DaylightSavingsTimeScribe());
	}

	//define standard property scribes
	private static final Map<String, ICalPropertyScribe<? extends ICalProperty>> standardPropByName = new HashMap<String, ICalPropertyScribe<? extends ICalProperty>>();
	private static final Map<Class<? extends ICalProperty>, ICalPropertyScribe<? extends ICalProperty>> standardPropByClass = new HashMap<Class<? extends ICalProperty>, ICalPropertyScribe<? extends ICalProperty>>();
	private static final Map<QName, ICalPropertyScribe<? extends ICalProperty>> standardPropByQName = new HashMap<QName, ICalPropertyScribe<? extends ICalProperty>>();
	static {
		//RFC 5545
		registerStandard(new ActionScribe());
		registerStandard(new AttachmentScribe());
		registerStandard(new AttendeeScribe());
		registerStandard(new CalendarScaleScribe());
		registerStandard(new CategoriesScribe());
		registerStandard(new ClassificationScribe());
		registerStandard(new CommentScribe());
		registerStandard(new CompletedScribe());
		registerStandard(new ContactScribe());
		registerStandard(new CreatedScribe());
		registerStandard(new DateDueScribe());
		registerStandard(new DateEndScribe());
		registerStandard(new DateStartScribe());
		registerStandard(new DateTimeStampScribe());
		registerStandard(new DescriptionScribe());
		registerStandard(new DurationPropertyScribe());
		registerStandard(new ExceptionDatesScribe());
		registerStandard(new FreeBusyScribe());
		registerStandard(new GeoScribe());
		registerStandard(new LastModifiedScribe());
		registerStandard(new LocationScribe());
		registerStandard(new MethodScribe());
		registerStandard(new OrganizerScribe());
		registerStandard(new PercentCompleteScribe());
		registerStandard(new PriorityScribe());
		registerStandard(new ProductIdScribe());
		registerStandard(new RecurrenceDatesScribe());
		registerStandard(new RecurrenceIdScribe());
		registerStandard(new RecurrenceRuleScribe());
		registerStandard(new RelatedToScribe());
		registerStandard(new RepeatScribe());
		registerStandard(new RequestStatusScribe());
		registerStandard(new ResourcesScribe());
		registerStandard(new SequenceScribe());
		registerStandard(new StatusScribe());
		registerStandard(new SummaryScribe());
		registerStandard(new TimezoneIdScribe());
		registerStandard(new TimezoneNameScribe());
		registerStandard(new TimezoneOffsetFromScribe());
		registerStandard(new TimezoneOffsetToScribe());
		registerStandard(new TimezoneUrlScribe());
		registerStandard(new TransparencyScribe());
		registerStandard(new TriggerScribe());
		registerStandard(new UidScribe());
		registerStandard(new UrlScribe());
		registerStandard(new VersionScribe());

		//RFC 6321
		registerStandard(new XmlScribe());

		//RFC 2445
		registerStandard(new ExceptionRuleScribe());

		//vCal
		registerStandard(new AudioAlarmScribe());
		registerStandard(new DaylightScribe());
		registerStandard(new DisplayAlarmScribe());
		registerStandard(new EmailAlarmScribe());
		registerStandard(new ProcedureAlarmScribe());
		registerStandard(new TimezoneScribe());

		//draft-ietf-calext-extensions-01
		registerStandard(new ColorScribe());
		registerStandard(new ConferenceScribe());
		registerStandard(new ImageScribe());
		registerStandard(new NameScribe());
		registerStandard(new SourceScribe());
		registerStandard(new RefreshIntervalScribe());
	}

	private final Map<String, ICalComponentScribe<? extends ICalComponent>> experimentalCompByName = new HashMap<String, ICalComponentScribe<? extends ICalComponent>>(0);
	private final Map<Class<? extends ICalComponent>, ICalComponentScribe<? extends ICalComponent>> experimentalCompByClass = new HashMap<Class<? extends ICalComponent>, ICalComponentScribe<? extends ICalComponent>>(0);

	private final Map<String, ICalPropertyScribe<? extends ICalProperty>> experimentalPropByName = new HashMap<String, ICalPropertyScribe<? extends ICalProperty>>(0);
	private final Map<Class<? extends ICalProperty>, ICalPropertyScribe<? extends ICalProperty>> experimentalPropByClass = new HashMap<Class<? extends ICalProperty>, ICalPropertyScribe<? extends ICalProperty>>(0);
	private final Map<QName, ICalPropertyScribe<? extends ICalProperty>> experimentalPropByQName = new HashMap<QName, ICalPropertyScribe<? extends ICalProperty>>(0);

	/**
	 * Gets a component scribe by name.
	 * @param componentName the component name (e.g. "VEVENT")
	 * @param version the version of the iCalendar object being parsed
	 * @return the component scribe or a {@link RawComponentScribe} if not found
	 */
	public ICalComponentScribe<? extends ICalComponent> getComponentScribe(String componentName, ICalVersion version) {
		componentName = componentName.toUpperCase();

		ICalComponentScribe<? extends ICalComponent> scribe = experimentalCompByName.get(componentName);
		if (scribe == null) {
			scribe = standardCompByName.get(componentName);
		}

		if (scribe == null) {
			return new RawComponentScribe(componentName);
		}

		if (version != null && !scribe.getSupportedVersions().contains(version)) {
			//treat the component as a raw component if the current iCal version doesn't support it
			return new RawComponentScribe(componentName);
		}

		return scribe;
	}

	/**
	 * Gets a property scribe by name.
	 * @param propertyName the property name (e.g. "UID")
	 * @param version the version of the iCalendar object being parsed
	 * @return the property scribe or a {@link RawPropertyScribe} if not found
	 */
	public ICalPropertyScribe<? extends ICalProperty> getPropertyScribe(String propertyName, ICalVersion version) {
		propertyName = propertyName.toUpperCase();

		String key = propertyNameKey(propertyName, version);
		ICalPropertyScribe<? extends ICalProperty> scribe = experimentalPropByName.get(key);
		if (scribe == null) {
			scribe = standardPropByName.get(key);
		}

		if (scribe == null) {
			return new RawPropertyScribe(propertyName);
		}

		if (version != null && !scribe.getSupportedVersions().contains(version)) {
			//treat the property as a raw property if the current iCal version doesn't support it
			return new RawPropertyScribe(propertyName);
		}

		return scribe;
	}

	/**
	 * Gets a component scribe by class.
	 * @param clazz the component class
	 * @return the component scribe or null if not found
	 */
	public ICalComponentScribe<? extends ICalComponent> getComponentScribe(Class<? extends ICalComponent> clazz) {
		ICalComponentScribe<? extends ICalComponent> scribe = experimentalCompByClass.get(clazz);
		if (scribe != null) {
			return scribe;
		}

		return standardCompByClass.get(clazz);
	}

	/**
	 * Gets a property scribe by class.
	 * @param clazz the property class
	 * @return the property scribe or null if not found
	 */
	public ICalPropertyScribe<? extends ICalProperty> getPropertyScribe(Class<? extends ICalProperty> clazz) {
		ICalPropertyScribe<? extends ICalProperty> scribe = experimentalPropByClass.get(clazz);
		if (scribe != null) {
			return scribe;
		}

		return standardPropByClass.get(clazz);
	}

	/**
	 * Gets the appropriate component scribe for a given component instance.
	 * @param component the component instance
	 * @return the component scribe or null if not found
	 */
	public ICalComponentScribe<? extends ICalComponent> getComponentScribe(ICalComponent component) {
		if (component instanceof RawComponent) {
			RawComponent raw = (RawComponent) component;
			return new RawComponentScribe(raw.getName());
		}

		return getComponentScribe(component.getClass());
	}

	/**
	 * Gets the appropriate property scribe for a given property instance.
	 * @param property the property instance
	 * @return the property scribe or null if not found
	 */
	public ICalPropertyScribe<? extends ICalProperty> getPropertyScribe(ICalProperty property) {
		if (property instanceof RawProperty) {
			RawProperty raw = (RawProperty) property;
			return new RawPropertyScribe(raw.getName());
		}

		return getPropertyScribe(property.getClass());
	}

	/**
	 * Gets a property scribe by XML local name and namespace.
	 * @param qname the XML local name and namespace
	 * @return the property scribe or a {@link XmlScribe} if not found
	 */
	public ICalPropertyScribe<? extends ICalProperty> getPropertyScribe(QName qname) {
		ICalPropertyScribe<? extends ICalProperty> scribe = experimentalPropByQName.get(qname);
		if (scribe == null) {
			scribe = standardPropByQName.get(qname);
		}

		if (scribe == null || !scribe.getSupportedVersions().contains(ICalVersion.V2_0)) {
			if (XCalNamespaceContext.XCAL_NS.equals(qname.getNamespaceURI())) {
				return new RawPropertyScribe(qname.getLocalPart().toUpperCase());
			}
			return getPropertyScribe(Xml.class);
		}

		return scribe;
	}

	/**
	 * Registers a component scribe.
	 * @param scribe the scribe to register
	 */
	public void register(ICalComponentScribe<? extends ICalComponent> scribe) {
		experimentalCompByName.put(scribe.getComponentName().toUpperCase(), scribe);
		experimentalCompByClass.put(scribe.getComponentClass(), scribe);
	}

	/**
	 * Registers a property scribe.
	 * @param scribe the scribe to register
	 */
	public void register(ICalPropertyScribe<? extends ICalProperty> scribe) {
		for (ICalVersion version : ICalVersion.values()) {
			experimentalPropByName.put(propertyNameKey(scribe, version), scribe);
		}
		experimentalPropByClass.put(scribe.getPropertyClass(), scribe);
		experimentalPropByQName.put(scribe.getQName(), scribe);
	}

	/**
	 * Unregisters a component scribe.
	 * @param scribe the scribe to unregister
	 */
	public void unregister(ICalComponentScribe<? extends ICalComponent> scribe) {
		experimentalCompByName.remove(scribe.getComponentName().toUpperCase());
		experimentalCompByClass.remove(scribe.getComponentClass());
	}

	/**
	 * Unregisters a property scribe
	 * @param scribe the scribe to unregister
	 */
	public void unregister(ICalPropertyScribe<? extends ICalProperty> scribe) {
		for (ICalVersion version : ICalVersion.values()) {
			experimentalPropByName.remove(propertyNameKey(scribe, version));
		}
		experimentalPropByClass.remove(scribe.getPropertyClass());
		experimentalPropByQName.remove(scribe.getQName());
	}

	/**
	 * Convenience method for getting the scribe of the root iCalendar component
	 * ("VCALENDAR").
	 * @return the scribe
	 */
	public static ICalendarScribe getICalendarScribe() {
		return (ICalendarScribe) standardCompByClass.get(ICalendar.class);
	}

	private static void registerStandard(ICalComponentScribe<? extends ICalComponent> scribe) {
		standardCompByName.put(scribe.getComponentName().toUpperCase(), scribe);
		standardCompByClass.put(scribe.getComponentClass(), scribe);
	}

	private static void registerStandard(ICalPropertyScribe<? extends ICalProperty> scribe) {
		for (ICalVersion version : ICalVersion.values()) {
			standardPropByName.put(propertyNameKey(scribe, version), scribe);
		}
		standardPropByClass.put(scribe.getPropertyClass(), scribe);
		standardPropByQName.put(scribe.getQName(), scribe);
	}

	private static String propertyNameKey(ICalPropertyScribe<? extends ICalProperty> scribe, ICalVersion version) {
		return propertyNameKey(scribe.getPropertyName(version), version);
	}

	private static String propertyNameKey(String propertyName, ICalVersion version) {
		return version.ordinal() + propertyName.toUpperCase();
	}
}
