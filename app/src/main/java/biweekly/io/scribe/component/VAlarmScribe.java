package biweekly.io.scribe.component;

import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import biweekly.ICalVersion;
import biweekly.component.ICalComponent;
import biweekly.component.VAlarm;
import biweekly.io.DataModelConversionException;
import biweekly.parameter.Related;
import biweekly.property.Action;
import biweekly.property.Attachment;
import biweekly.property.Attendee;
import biweekly.property.AudioAlarm;
import biweekly.property.DateEnd;
import biweekly.property.DateStart;
import biweekly.property.Description;
import biweekly.property.DisplayAlarm;
import biweekly.property.DurationProperty;
import biweekly.property.EmailAlarm;
import biweekly.property.ProcedureAlarm;
import biweekly.property.Repeat;
import biweekly.property.Trigger;
import biweekly.property.VCalAlarmProperty;
import biweekly.property.ValuedProperty;
import biweekly.util.Duration;
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
 * @author Michael Angstadt
 */
public class VAlarmScribe extends ICalComponentScribe<VAlarm> {
	public VAlarmScribe() {
		super(VAlarm.class, "VALARM");
	}

	@Override
	protected VAlarm _newInstance() {
		return new VAlarm(null, null);
	}

	@Override
	public void checkForDataModelConversions(VAlarm component, ICalComponent parent, ICalVersion version) {
		if (version != ICalVersion.V1_0) {
			return;
		}

		VCalAlarmProperty vcalAlarm = convert(component, parent);
		if (vcalAlarm == null) {
			return;
		}

		DataModelConversionException e = new DataModelConversionException(null);
		e.getProperties().add(vcalAlarm);
		throw e;
	}

	@Override
	public Set<ICalVersion> getSupportedVersions() {
		return EnumSet.of(ICalVersion.V2_0_DEPRECATED, ICalVersion.V2_0);
	}

	/**
	 * Converts a {@link VAlarm} component to a vCal alarm property.
	 * @param valarm the component
	 * @param parent the component's parent
	 * @return the vCal alarm property or null if it cannot be converted
	 */
	private static VCalAlarmProperty convert(VAlarm valarm, ICalComponent parent) {
		VCalAlarmProperty property = create(valarm);
		if (property == null) {
			return null;
		}

		property.setStart(determineStartDate(valarm, parent));

		DurationProperty duration = valarm.getDuration();
		if (duration != null) {
			property.setSnooze(duration.getValue());
		}

		Repeat repeat = valarm.getRepeat();
		if (repeat != null) {
			property.setRepeat(repeat.getValue());
		}

		return property;
	}

	/**
	 * Creates a new {@link VCalAlarmProperty} based on the given {@link VAlarm}
	 * component, setting fields that are common to all
	 * {@link VCalAlarmProperty} classes.
	 * @param valarm the source component
	 * @return the property or null if it cannot be created
	 */
	private static VCalAlarmProperty create(VAlarm valarm) {
		Action action = valarm.getAction();
		if (action == null) {
			return null;
		}

		if (action.isAudio()) {
			AudioAlarm aalarm = new AudioAlarm();

			List<Attachment> attaches = valarm.getAttachments();
			if (!attaches.isEmpty()) {
				Attachment attach = attaches.get(0);

				String formatType = attach.getFormatType();
				aalarm.setParameter("TYPE", formatType);

				byte[] data = attach.getData();
				if (data != null) {
					aalarm.setData(data);
				}

				String uri = attach.getUri();
				if (uri != null) {
					String contentId = StringUtils.afterPrefixIgnoreCase(uri, "cid:");
					if (contentId == null) {
						aalarm.setUri(uri);
					} else {
						aalarm.setContentId(contentId);
					}
				}
			}

			return aalarm;
		}

		if (action.isDisplay()) {
			Description description = valarm.getDescription();
			String text = ValuedProperty.getValue(description);
			return new DisplayAlarm(text);
		}

		if (action.isEmail()) {
			List<Attendee> attendees = valarm.getAttendees();
			String email = attendees.isEmpty() ? null : attendees.get(0).getEmail();
			EmailAlarm malarm = new EmailAlarm(email);

			Description description = valarm.getDescription();
			String note = ValuedProperty.getValue(description);
			malarm.setNote(note);

			return malarm;
		}

		if (action.isProcedure()) {
			Description description = valarm.getDescription();
			String path = ValuedProperty.getValue(description);
			return new ProcedureAlarm(path);
		}

		return null;
	}

	/**
	 * Determines what the alarm property's start date should be.
	 * @param valarm the component that is being converted to a vCal alarm
	 * property
	 * @param parent the component's parent
	 * @return the start date or null if it cannot be determined
	 */
	private static Date determineStartDate(VAlarm valarm, ICalComponent parent) {
		Trigger trigger = valarm.getTrigger();
		if (trigger == null) {
			return null;
		}

		Date triggerStart = trigger.getDate();
		if (triggerStart != null) {
			return triggerStart;
		}

		Duration triggerDuration = trigger.getDuration();
		if (triggerDuration == null) {
			return null;
		}

		if (parent == null) {
			return null;
		}

		Related related = trigger.getRelated();
		Date date = null;
		if (related == Related.START) {
			date = ValuedProperty.getValue(parent.getProperty(DateStart.class));
		} else if (related == Related.END) {
			date = ValuedProperty.getValue(parent.getProperty(DateEnd.class));
			if (date == null) {
				Date dateStart = ValuedProperty.getValue(parent.getProperty(DateStart.class));
				Duration duration = ValuedProperty.getValue(parent.getProperty(DurationProperty.class));
				if (duration != null && dateStart != null) {
					date = duration.add(dateStart);
				}
			}
		}

		return (date == null) ? null : triggerDuration.add(date);
	}
}
