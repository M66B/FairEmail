package biweekly.component;

import java.util.Arrays;
import java.util.List;

import biweekly.ICalVersion;
import biweekly.ValidationWarning;
import biweekly.parameter.Related;
import biweekly.property.Action;
import biweekly.property.Attachment;
import biweekly.property.Attendee;
import biweekly.property.DateDue;
import biweekly.property.DateEnd;
import biweekly.property.DateStart;
import biweekly.property.Description;
import biweekly.property.DurationProperty;
import biweekly.property.Repeat;
import biweekly.property.Summary;
import biweekly.property.Trigger;
import biweekly.util.Duration;

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
 * Defines a reminder for an event or to-do task. This class contains static
 * factory methods to aid in the construction of valid alarms.
 * </p>
 * <p>
 * <b>Examples:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * //audio alarm
 * Trigger trigger = ...
 * Attachment sound = ...
 * VAlarm audio = VAlarm.audio(trigger, sound);
 * 
 * //display alarm
 * Trigger trigger = ...
 * String message = "Meeting at 1pm";
 * VAlarm display = VAlarm.display(trigger, message);
 * 
 * //email alarm
 * Trigger trigger = ...
 * String subject = "Reminder: Meeting at 1pm";
 * String body = "Team,\n\nThe team meeting scheduled for 1pm is about to start.  Snacks will be served!\n\nThanks,\nJohn";
 * List&lt;String&gt; to = Arrays.asList("janedoe@example.com", "bobsmith@example.com");
 * VAlarm email = VAlarm.email(trigger, subject, body, to);
 * </pre>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5545#page-71">RFC 5545 p.71-6</a>
 * @see <a href="http://tools.ietf.org/html/rfc2445#page-67">RFC 2445
 * p.67-73</a>
 */
/*
 * Note: References to the vCal 1.0 spec are omitted from the property
 * getter/setter method Javadocs because vCal does not use the VALARM component.
 */
public class VAlarm extends ICalComponent {
	/**
	 * Creates a new alarm. Consider using one of the static factory methods
	 * instead.
	 * @param action the alarm action (e.g. "email")
	 * @param trigger the trigger
	 */
	public VAlarm(Action action, Trigger trigger) {
		setAction(action);
		setTrigger(trigger);
	}

	/**
	 * Copy constructor.
	 * @param original the component to make a copy of
	 */
	public VAlarm(VAlarm original) {
		super(original);
	}

	/**
	 * Creates an audio alarm.
	 * @param trigger the trigger
	 * @return the alarm
	 */
	public static VAlarm audio(Trigger trigger) {
		return audio(trigger, null);
	}

	/**
	 * Creates an audio alarm.
	 * @param trigger the trigger
	 * @param sound a sound to play when the alarm triggers
	 * @return the alarm
	 */
	public static VAlarm audio(Trigger trigger, Attachment sound) {
		VAlarm alarm = new VAlarm(Action.audio(), trigger);
		if (sound != null) {
			alarm.addAttachment(sound);
		}
		return alarm;
	}

	/**
	 * Creates a display alarm.
	 * @param trigger the trigger
	 * @param displayText the display text
	 * @return the alarm
	 */
	public static VAlarm display(Trigger trigger, String displayText) {
		VAlarm alarm = new VAlarm(Action.display(), trigger);
		alarm.setDescription(displayText);
		return alarm;
	}

	/**
	 * Creates an email alarm.
	 * @param trigger the trigger
	 * @param subject the email subject
	 * @param body the email body
	 * @param recipients the email address(es) to send the alert to
	 * @return the alarm
	 */
	public static VAlarm email(Trigger trigger, String subject, String body, String... recipients) {
		return email(trigger, subject, body, Arrays.asList(recipients));
	}

	/**
	 * Creates a procedure alarm (vCal 1.0 only).
	 * @param trigger the trigger
	 * @param path the path or name of the procedure
	 * @return the alarm
	 */
	public static VAlarm procedure(Trigger trigger, String path) {
		VAlarm alarm = new VAlarm(Action.procedure(), trigger);
		alarm.setDescription(path);
		return alarm;
	}

	/**
	 * Creates an email alarm.
	 * @param trigger the trigger
	 * @param subject the email subject
	 * @param body the email body
	 * @param recipients the email address(es) to send the alert to
	 * @return the alarm
	 */
	public static VAlarm email(Trigger trigger, String subject, String body, List<String> recipients) {
		VAlarm alarm = new VAlarm(Action.email(), trigger);
		alarm.setSummary(subject);
		alarm.setDescription(body);
		for (String recipient : recipients) {
			alarm.addAttendee(new Attendee(null, recipient));
		}
		return alarm;
	}

	/**
	 * Gets any attachments that are associated with the alarm.
	 * @return the attachments (any changes made this list will affect the
	 * parent component object and vice versa)
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-80">RFC 5545
	 * p.80-1</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-77">RFC 2445
	 * p.77-8</a>
	 */
	public List<Attachment> getAttachments() {
		return getProperties(Attachment.class);
	}

	/**
	 * Adds an attachment to the alarm. Note that AUDIO alarms should only have
	 * 1 attachment.
	 * @param attachment the attachment to add
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-80">RFC 5545
	 * p.80-1</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-77">RFC 2445
	 * p.77-8</a>
	 */
	public void addAttachment(Attachment attachment) {
		addProperty(attachment);
	}

	/**
	 * <p>
	 * Gets a detailed description of the alarm. The description should be more
	 * detailed than the one provided by the {@link Summary} property.
	 * </p>
	 * <p>
	 * This property has different meanings, depending on the alarm action:
	 * </p>
	 * <ul>
	 * <li>DISPLAY - the display text</li>
	 * <li>EMAIL - the body of the email message</li>
	 * <li>all others - a general description of the alarm</li>
	 * </ul>
	 * @return the description or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-84">RFC 5545
	 * p.84-5</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-81">RFC 2445
	 * p.81-2</a>
	 */
	public Description getDescription() {
		return getProperty(Description.class);
	}

	/**
	 * <p>
	 * Sets a detailed description of the alarm. The description should be more
	 * detailed than the one provided by the {@link Summary} property.
	 * </p>
	 * <p>
	 * This property has different meanings, depending on the alarm action:
	 * </p>
	 * <ul>
	 * <li>DISPLAY - the display text</li>
	 * <li>EMAIL - the body of the email message</li>
	 * <li>all others - a general description of the alarm</li>
	 * </ul>
	 * @param description the description or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-84">RFC 5545
	 * p.84-5</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-81">RFC 2445
	 * p.81-2</a>
	 */
	public void setDescription(Description description) {
		setProperty(Description.class, description);
	}

	/**
	 * <p>
	 * Sets a detailed description of the alarm. The description should be more
	 * detailed than the one provided by the {@link Summary} property.
	 * </p>
	 * <p>
	 * This property has different meanings, depending on the alarm action:
	 * </p>
	 * <ul>
	 * <li>DISPLAY - the display text</li>
	 * <li>EMAIL - the body of the email message</li>
	 * <li>all others - a general description of the alarm</li>
	 * </ul>
	 * @param description the description or null to remove
	 * @return the property that was created
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-84">RFC 5545
	 * p.84-5</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-81">RFC 2445
	 * p.81-2</a>
	 */
	public Description setDescription(String description) {
		Description prop = (description == null) ? null : new Description(description);
		setDescription(prop);
		return prop;
	}

	/**
	 * <p>
	 * Gets the summary of the alarm.
	 * </p>
	 * <p>
	 * This property has different meanings, depending on the alarm action:
	 * </p>
	 * <ul>
	 * <li>EMAIL - the subject line of the email</li>
	 * <li>all others - a one-line summary of the alarm</li>
	 * </ul>
	 * @return the summary or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-93">RFC 5545
	 * p.93-4</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-89">RFC 2445
	 * p.89-90</a>
	 */
	public Summary getSummary() {
		return getProperty(Summary.class);
	}

	/**
	 * <p>
	 * Sets the summary of the alarm.
	 * </p>
	 * <p>
	 * This property has different meanings, depending on the alarm action:
	 * </p>
	 * <ul>
	 * <li>EMAIL - the subject line of the email</li>
	 * <li>all others - a one-line summary of the alarm</li>
	 * </ul>
	 * @param summary the summary or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-93">RFC 5545
	 * p.93-4</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-89">RFC 2445
	 * p.89-90</a>
	 */
	public void setSummary(Summary summary) {
		setProperty(Summary.class, summary);
	}

	/**
	 * <p>
	 * Sets the summary of the alarm.
	 * </p>
	 * <p>
	 * This property has different meanings, depending on the alarm action:
	 * </p>
	 * <ul>
	 * <li>EMAIL - the subject line of the email</li>
	 * <li>all others - a one-line summary of the alarm</li>
	 * </ul>
	 * @param summary the summary or null to remove
	 * @return the property that was created
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-93">RFC 5545
	 * p.93-4</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-89">RFC 2445
	 * p.89-90</a>
	 */
	public Summary setSummary(String summary) {
		Summary prop = (summary == null) ? null : new Summary(summary);
		setSummary(prop);
		return prop;
	}

	/**
	 * Gets the people who will be emailed when the alarm fires (only applicable
	 * for EMAIL alarms).
	 * @return the email recipients (any changes made this list will affect the
	 * parent component object and vice versa)
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-107">RFC 5545
	 * p.107-9</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-102">RFC 2445
	 * p.102-4</a>
	 */
	public List<Attendee> getAttendees() {
		return getProperties(Attendee.class);
	}

	/**
	 * Adds a person who will be emailed when the alarm fires (only applicable
	 * for EMAIL alarms).
	 * @param attendee the email recipient
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-107">RFC 5545
	 * p.107-9</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-102">RFC 2445
	 * p.102-4</a>
	 */
	public void addAttendee(Attendee attendee) {
		addProperty(attendee);
	}

	/**
	 * Gets the type of action to invoke when the alarm is triggered.
	 * @return the action or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-132">RFC 5545
	 * p.132-3</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-126">RFC 2445
	 * p.126</a>
	 */
	public Action getAction() {
		return getProperty(Action.class);
	}

	/**
	 * Sets the type of action to invoke when the alarm is triggered.
	 * @param action the action or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-132">RFC 5545
	 * p.132-3</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-126">RFC 2445
	 * p.126</a>
	 */
	public void setAction(Action action) {
		setProperty(Action.class, action);
	}

	/**
	 * Gets the length of the pause between alarm repetitions.
	 * @return the duration or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-99">RFC 5545
	 * p.99</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-94">RFC 2445
	 * p.94-5</a>
	 */
	public DurationProperty getDuration() {
		return getProperty(DurationProperty.class);
	}

	/**
	 * Sets the length of the pause between alarm repetitions.
	 * @param duration the duration or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-99">RFC 5545
	 * p.99</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-94">RFC 2445
	 * p.94-5</a>
	 */
	public void setDuration(DurationProperty duration) {
		setProperty(DurationProperty.class, duration);
	}

	/**
	 * Sets the length of the pause between alarm repetitions.
	 * @param duration the duration or null to remove
	 * @return the property that was created
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-99">RFC 5545
	 * p.99</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-94">RFC 2445
	 * p.94-5</a>
	 */
	public DurationProperty setDuration(Duration duration) {
		DurationProperty prop = (duration == null) ? null : new DurationProperty(duration);
		setDuration(prop);
		return prop;
	}

	/**
	 * Gets the number of times an alarm should be repeated after its initial
	 * trigger.
	 * @return the repeat count or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-133">RFC 5545
	 * p.133</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-126">RFC 2445
	 * p.126-7</a>
	 */
	public Repeat getRepeat() {
		return getProperty(Repeat.class);
	}

	/**
	 * Sets the number of times an alarm should be repeated after its initial
	 * trigger.
	 * @param repeat the repeat count or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-133">RFC 5545
	 * p.133</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-126">RFC 2445
	 * p.126-7</a>
	 */
	public void setRepeat(Repeat repeat) {
		setProperty(Repeat.class, repeat);
	}

	/**
	 * Sets the number of times an alarm should be repeated after its initial
	 * trigger.
	 * @param count the repeat count (e.g. "2" to repeat it two more times after
	 * it was initially triggered, for a total of three times) or null to remove
	 * @return the property that was created
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-133">RFC 5545
	 * p.133</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-126">RFC 2445
	 * p.126-7</a>
	 */
	public Repeat setRepeat(Integer count) {
		Repeat prop = (count == null) ? null : new Repeat(count);
		setRepeat(prop);
		return prop;
	}

	/**
	 * Sets the repetition information for the alarm.
	 * @param count the repeat count (e.g. "2" to repeat it two more times after
	 * it was initially triggered, for a total of three times)
	 * @param pauseDuration the length of the pause between repeats
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-133">RFC 5545
	 * p.133</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-126">RFC 2445
	 * p.126-7</a>
	 */
	public void setRepeat(int count, Duration pauseDuration) {
		Repeat repeat = new Repeat(count);
		DurationProperty duration = new DurationProperty(pauseDuration);
		setRepeat(repeat);
		setDuration(duration);
	}

	/**
	 * Gets when the alarm will be triggered.
	 * @return the trigger time or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-133">RFC 5545
	 * p.133-6</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-127">RFC 2445
	 * p.127-9</a>
	 */
	public Trigger getTrigger() {
		return getProperty(Trigger.class);
	}

	/**
	 * Sets when the alarm will be triggered.
	 * @param trigger the trigger time or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-133">RFC 5545
	 * p.133-6</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-127">RFC 2445
	 * p.127-9</a>
	 */
	public void setTrigger(Trigger trigger) {
		setProperty(Trigger.class, trigger);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void validate(List<ICalComponent> components, ICalVersion version, List<ValidationWarning> warnings) {
		checkRequiredCardinality(warnings, Action.class, Trigger.class);

		Action action = getAction();
		if (action != null) {
			//AUDIO alarms should not have more than 1 attachment
			if (action.isAudio()) {
				if (getAttachments().size() > 1) {
					warnings.add(new ValidationWarning(7));
				}
			}

			//DESCRIPTION is required for DISPLAY alarms 
			if (action.isDisplay()) {
				checkRequiredCardinality(warnings, Description.class);
			}

			if (action.isEmail()) {
				//SUMMARY and DESCRIPTION is required for EMAIL alarms
				checkRequiredCardinality(warnings, Summary.class, Description.class);

				//EMAIL alarms must have at least 1 ATTENDEE
				if (getAttendees().isEmpty()) {
					warnings.add(new ValidationWarning(8));
				}
			} else {
				//only EMAIL alarms can have ATTENDEEs
				if (!getAttendees().isEmpty()) {
					warnings.add(new ValidationWarning(9));
				}
			}

			if (action.isProcedure()) {
				checkRequiredCardinality(warnings, Description.class);
			}
		}

		Trigger trigger = getTrigger();
		if (trigger != null) {
			Related related = trigger.getRelated();
			if (related != null) {
				ICalComponent parent = components.get(components.size() - 1);

				//if the TRIGGER is relative to DTSTART, confirm that DTSTART exists
				if (related == Related.START && parent.getProperty(DateStart.class) == null) {
					warnings.add(new ValidationWarning(11));
				}

				//if the TRIGGER is relative to DTEND, confirm that DTEND (or DUE) exists
				if (related == Related.END) {
					boolean noEndDate = false;

					if (parent instanceof VEvent) {
						noEndDate = (parent.getProperty(DateEnd.class) == null && (parent.getProperty(DateStart.class) == null || parent.getProperty(DurationProperty.class) == null));
					} else if (parent instanceof VTodo) {
						noEndDate = (parent.getProperty(DateDue.class) == null && (parent.getProperty(DateStart.class) == null || parent.getProperty(DurationProperty.class) == null));
					}

					if (noEndDate) {
						warnings.add(new ValidationWarning(12));
					}
				}
			}
		}
	}

	@Override
	public VAlarm copy() {
		return new VAlarm(this);
	}
}
