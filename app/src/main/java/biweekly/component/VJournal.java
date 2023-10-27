package biweekly.component;

import static biweekly.property.ValuedProperty.getValue;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import biweekly.ICalVersion;
import biweekly.ValidationWarning;
import biweekly.property.Attachment;
import biweekly.property.Attendee;
import biweekly.property.Categories;
import biweekly.property.Classification;
import biweekly.property.Color;
import biweekly.property.Comment;
import biweekly.property.Contact;
import biweekly.property.Created;
import biweekly.property.DateStart;
import biweekly.property.DateTimeStamp;
import biweekly.property.Description;
import biweekly.property.ExceptionDates;
import biweekly.property.ExceptionRule;
import biweekly.property.Image;
import biweekly.property.LastModified;
import biweekly.property.Method;
import biweekly.property.Organizer;
import biweekly.property.RecurrenceDates;
import biweekly.property.RecurrenceId;
import biweekly.property.RecurrenceRule;
import biweekly.property.RelatedTo;
import biweekly.property.RequestStatus;
import biweekly.property.Sequence;
import biweekly.property.Status;
import biweekly.property.Summary;
import biweekly.property.Uid;
import biweekly.property.Url;
import biweekly.util.Google2445Utils;
import biweekly.util.ICalDate;
import biweekly.util.Period;
import biweekly.util.Recurrence;
import biweekly.util.com.google.ical.compat.javautil.DateIterator;

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
 * Defines a journal entry.
 * </p>
 * <p>
 * <b>Examples:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VJournal journal = new VJournal();
 * journal.setSummary("Team Meeting");
 * journal.setDescription("The following items were discussed: ...");
 * byte[] slides = ...
 * journal.addAttachment(new Attachment("application/vnd.ms-powerpoint", slides));
 * </pre>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5545#page-57">RFC 5545 p.57-9</a>
 * @see <a href="http://tools.ietf.org/html/rfc2445#page-56">RFC 2445 p.56-7</a>
 */
/*
 * Note: References to the vCal 1.0 spec are omitted from the property
 * getter/setter method Javadocs because vCal does not use the VJOURNAL
 * component.
 */
public class VJournal extends ICalComponent {
	/**
	 * <p>
	 * Creates a new journal entry.
	 * </p>
	 * <p>
	 * The following properties are added to the component when it is created:
	 * </p>
	 * <ul>
	 * <li>{@link Uid}: Set to a UUID.</li>
	 * <li>{@link DateTimeStamp}: Set to the current time.</li>
	 * </ul>
	 */
	public VJournal() {
		setUid(Uid.random());
		setDateTimeStamp(new Date());
	}

	/**
	 * Copy constructor.
	 * @param original the component to make a copy of
	 */
	public VJournal(VJournal original) {
		super(original);
	}

	/**
	 * Gets the unique identifier for this journal entry. This component object
	 * comes populated with a UID on creation. This is a <b>required</b>
	 * property.
	 * @return the UID or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-117">RFC 5545
	 * p.117-8</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-111">RFC 2445
	 * p.111-2</a>
	 */
	public Uid getUid() {
		return getProperty(Uid.class);
	}

	/**
	 * Sets the unique identifier for this journal entry. This component object
	 * comes populated with a UID on creation. This is a <b>required</b>
	 * property.
	 * @param uid the UID or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-117">RFC 5545
	 * p.117-8</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-111">RFC 2445
	 * p.111-2</a>
	 */
	public void setUid(Uid uid) {
		setProperty(Uid.class, uid);
	}

	/**
	 * Sets the unique identifier for this journal entry. This component object
	 * comes populated with a UID on creation. This is a <b>required</b>
	 * property.
	 * @param uid the UID or null to remove
	 * @return the property that was created
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-117">RFC 5545
	 * p.117-8</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-111">RFC 2445
	 * p.111-2</a>
	 */
	public Uid setUid(String uid) {
		Uid prop = (uid == null) ? null : new Uid(uid);
		setUid(prop);
		return prop;
	}

	/**
	 * Gets either (a) the creation date of the iCalendar object (if the
	 * {@link Method} property is defined) or (b) the date that the journal
	 * entry was last modified (the {@link LastModified} property also holds
	 * this information). This journal entry object comes populated with a
	 * {@link DateTimeStamp} property that is set to the current time. This is a
	 * <b>required</b> property.
	 * @return the date time stamp or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-137">RFC 5545
	 * p.137-8</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-130">RFC 2445
	 * p.130-1</a>
	 */
	public DateTimeStamp getDateTimeStamp() {
		return getProperty(DateTimeStamp.class);
	}

	/**
	 * Sets either (a) the creation date of the iCalendar object (if the
	 * {@link Method} property is defined) or (b) the date that the journal
	 * entry was last modified (the {@link LastModified} property also holds
	 * this information). This journal entry object comes populated with a
	 * {@link DateTimeStamp} property that is set to the current time. This is a
	 * <b>required</b> property.
	 * @param dateTimeStamp the date time stamp or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-137">RFC 5545
	 * p.137-8</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-130">RFC 2445
	 * p.130-1</a>
	 */
	public void setDateTimeStamp(DateTimeStamp dateTimeStamp) {
		setProperty(DateTimeStamp.class, dateTimeStamp);
	}

	/**
	 * Sets either (a) the creation date of the iCalendar object (if the
	 * {@link Method} property is defined) or (b) the date that the journal
	 * entry was last modified (the {@link LastModified} property also holds
	 * this information). This journal entry object comes populated with a
	 * {@link DateTimeStamp} property that is set to the current time. This is a
	 * <b>required</b> property.
	 * @param dateTimeStamp the date time stamp or null to remove
	 * @return the property that was created
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-137">RFC 5545
	 * p.137-8</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-130">RFC 2445
	 * p.130-1</a>
	 */
	public DateTimeStamp setDateTimeStamp(Date dateTimeStamp) {
		DateTimeStamp prop = (dateTimeStamp == null) ? null : new DateTimeStamp(dateTimeStamp);
		setDateTimeStamp(prop);
		return prop;
	}

	/**
	 * Gets the level of sensitivity of the journal entry. If not specified, the
	 * data within the journal entry should be considered "public".
	 * @return the classification level or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-82">RFC 5545
	 * p.82-3</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-79">RFC 2445
	 * p.79-80</a>
	 */
	public Classification getClassification() {
		return getProperty(Classification.class);
	}

	/**
	 * Sets the level of sensitivity of the journal entry. If not specified, the
	 * data within the journal entry should be considered "public".
	 * @param classification the classification level or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-82">RFC 5545
	 * p.82-3</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-79">RFC 2445
	 * p.79-80</a>
	 */
	public void setClassification(Classification classification) {
		setProperty(Classification.class, classification);
	}

	/**
	 * Sets the level of sensitivity of the journal entry. If not specified, the
	 * data within the journal entry should be considered "public".
	 * @param classification the classification level (e.g. "CONFIDENTIAL") or
	 * null to remove
	 * @return the property that was created
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-82">RFC 5545
	 * p.82-3</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-79">RFC 2445
	 * p.79-80</a>
	 */
	public Classification setClassification(String classification) {
		Classification prop = (classification == null) ? null : new Classification(classification);
		setClassification(prop);
		return prop;
	}

	/**
	 * Gets the date-time that the journal entry was initially created.
	 * @return the creation date-time or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-136">RFC 5545
	 * p.136</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-129">RFC 2445
	 * p.129-30</a>
	 */
	public Created getCreated() {
		return getProperty(Created.class);
	}

	/**
	 * Sets the date-time that the journal entry was initially created.
	 * @param created the creation date-time or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-136">RFC 5545
	 * p.136</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-129">RFC 2445
	 * p.129-30</a>
	 */
	public void setCreated(Created created) {
		setProperty(Created.class, created);
	}

	/**
	 * Sets the date-time that the journal entry was initially created.
	 * @param created the creation date-time or null to remove
	 * @return the property that was created
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-136">RFC 5545
	 * p.136</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-129">RFC 2445
	 * p.129-30</a>
	 */
	public Created setCreated(Date created) {
		Created prop = (created == null) ? null : new Created(created);
		setCreated(prop);
		return prop;
	}

	/**
	 * Gets the date that the journal entry starts.
	 * @return the start date or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-97">RFC 5545
	 * p.97-8</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-93">RFC 2445
	 * p.93-4</a>
	 */
	public DateStart getDateStart() {
		return getProperty(DateStart.class);
	}

	/**
	 * Sets the date that the journal entry starts.
	 * @param dateStart the start date or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-97">RFC 5545
	 * p.97-8</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-93">RFC 2445
	 * p.93-4</a>
	 */
	public void setDateStart(DateStart dateStart) {
		setProperty(DateStart.class, dateStart);
	}

	/**
	 * Sets the date that the journal entry starts.
	 * @param dateStart the start date or null to remove
	 * @return the property that was created
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-97">RFC 5545
	 * p.97-8</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-93">RFC 2445
	 * p.93-4</a>
	 */
	public DateStart setDateStart(Date dateStart) {
		return setDateStart(dateStart, true);
	}

	/**
	 * Sets the date that the journal entry starts.
	 * @param dateStart the start date or null to remove
	 * @param hasTime true if the date has a time component, false if it is
	 * strictly a date (if false, the given Date object should be created by a
	 * {@link java.util.Calendar Calendar} object that uses the JVM's default
	 * timezone)
	 * @return the property that was created
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-97">RFC 5545
	 * p.97-8</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-93">RFC 2445
	 * p.93-4</a>
	 */
	public DateStart setDateStart(Date dateStart, boolean hasTime) {
		DateStart prop = (dateStart == null) ? null : new DateStart(dateStart, hasTime);
		setDateStart(prop);
		return prop;
	}

	/**
	 * Gets the date-time that the journal entry was last changed.
	 * @return the last modified date or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-138">RFC 5545
	 * p.138</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-131">RFC 2445
	 * p.131</a>
	 */
	public LastModified getLastModified() {
		return getProperty(LastModified.class);
	}

	/**
	 * Sets the date-time that the journal entry was last changed.
	 * @param lastModified the last modified date or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-138">RFC 5545
	 * p.138</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-131">RFC 2445
	 * p.131</a>
	 */
	public void setLastModified(LastModified lastModified) {
		setProperty(LastModified.class, lastModified);
	}

	/**
	 * Sets the date-time that the journal entry was last changed.
	 * @param lastModified the last modified date or null to remove
	 * @return the property that was created
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-138">RFC 5545
	 * p.138</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-131">RFC 2445
	 * p.131</a>
	 */
	public LastModified setLastModified(Date lastModified) {
		LastModified prop = (lastModified == null) ? null : new LastModified(lastModified);
		setLastModified(prop);
		return prop;
	}

	/**
	 * Gets the organizer of the journal entry.
	 * @return the organizer or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-111">RFC 5545
	 * p.111-2</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-106">RFC 2445
	 * p.106-7</a>
	 */
	public Organizer getOrganizer() {
		return getProperty(Organizer.class);
	}

	/**
	 * Sets the organizer of the journal entry.
	 * @param organizer the organizer or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-111">RFC 5545
	 * p.111-2</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-106">RFC 2445
	 * p.106-7</a>
	 */
	public void setOrganizer(Organizer organizer) {
		setProperty(Organizer.class, organizer);
	}

	/**
	 * Sets the organizer of the journal entry.
	 * @param email the organizer's email address (e.g. "johndoe@example.com")
	 * or null to remove
	 * @return the property that was created
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-111">RFC 5545
	 * p.111-2</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-106">RFC 2445
	 * p.106-7</a>
	 */
	public Organizer setOrganizer(String email) {
		Organizer prop = (email == null) ? null : new Organizer(null, email);
		setOrganizer(prop);
		return prop;
	}

	/**
	 * Gets the original value of the {@link DateStart} property if the event is
	 * recurring and has been modified. Used in conjunction with the {@link Uid}
	 * and {@link Sequence} properties to uniquely identify a recurrence
	 * instance.
	 * @return the recurrence ID or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-112">RFC 5545
	 * p.112-4</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-107">RFC 2445
	 * p.107-9</a>
	 */
	public RecurrenceId getRecurrenceId() {
		return getProperty(RecurrenceId.class);
	}

	/**
	 * Sets the original value of the {@link DateStart} property if the event is
	 * recurring and has been modified. Used in conjunction with the {@link Uid}
	 * and {@link Sequence} properties to uniquely identify a recurrence
	 * instance.
	 * @param recurrenceId the recurrence ID or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-112">RFC 5545
	 * p.112-4</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-107">RFC 2445
	 * p.107-9</a>
	 */
	public void setRecurrenceId(RecurrenceId recurrenceId) {
		setProperty(RecurrenceId.class, recurrenceId);
	}

	/**
	 * Sets the original value of the {@link DateStart} property if the journal
	 * entry is recurring and has been modified. Used in conjunction with the
	 * {@link Uid} and {@link Sequence} properties to uniquely identify a
	 * recurrence instance.
	 * @param originalStartDate the original start date or null to remove
	 * @return the property that was created
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-112">RFC 5545
	 * p.112-4</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-107">RFC 2445
	 * p.107-9</a>
	 */
	public RecurrenceId setRecurrenceId(Date originalStartDate) {
		RecurrenceId prop = (originalStartDate == null) ? null : new RecurrenceId(originalStartDate);
		setRecurrenceId(prop);
		return prop;
	}

	/**
	 * Gets the revision number of the journal entry. The organizer can
	 * increment this number every time he or she makes a significant change.
	 * @return the sequence number
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-138">RFC 5545
	 * p.138-9</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-131">RFC 2445
	 * p.131-3</a>
	 */
	public Sequence getSequence() {
		return getProperty(Sequence.class);
	}

	/**
	 * Sets the revision number of the journal entry. The organizer can
	 * increment this number every time he or she makes a significant change.
	 * @param sequence the sequence number
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-138">RFC 5545
	 * p.138-9</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-131">RFC 2445
	 * p.131-3</a>
	 */
	public void setSequence(Sequence sequence) {
		setProperty(Sequence.class, sequence);
	}

	/**
	 * Sets the revision number of the journal entry. The organizer can
	 * increment this number every time he or she makes a significant change.
	 * @param sequence the sequence number
	 * @return the property that was created
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-138">RFC 5545
	 * p.138-9</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-131">RFC 2445
	 * p.131-3</a>
	 */
	public Sequence setSequence(Integer sequence) {
		Sequence prop = (sequence == null) ? null : new Sequence(sequence);
		setSequence(prop);
		return prop;
	}

	/**
	 * Increments the revision number of the journal entry. The organizer can
	 * increment this number every time he or she makes a significant change.
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-138">RFC 5545
	 * p.138-9</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-131">RFC 2445
	 * p.131-3</a>
	 */
	public void incrementSequence() {
		Sequence sequence = getSequence();
		if (sequence == null) {
			setSequence(1);
		} else {
			sequence.increment();
		}
	}

	/**
	 * Gets the status of the journal entry.
	 * @return the status or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-92">RFC 5545
	 * p.92-3</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-88">RFC 2445
	 * p.88-9</a>
	 */
	public Status getStatus() {
		return getProperty(Status.class);
	}

	/**
	 * <p>
	 * Sets the status of the journal entry.
	 * </p>
	 * <p>
	 * Valid journal status codes are:
	 * </p>
	 * <ul>
	 * <li>DRAFT</li>
	 * <li>FINAL</li>
	 * <li>CANCELLED</li>
	 * </ul>
	 * @param status the status or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-92">RFC 5545
	 * p.92-3</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-88">RFC 2445
	 * p.88-9</a>
	 */
	public void setStatus(Status status) {
		setProperty(Status.class, status);
	}

	/**
	 * Gets the summary of the journal entry.
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
	 * Sets the summary of the journal entry.
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
	 * Sets the summary of the journal entry.
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
	 * Gets a URL to a resource that contains additional information about the
	 * journal entry.
	 * @return the URL or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-116">RFC 5545
	 * p.116-7</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-110">RFC 2445
	 * p.110-1</a>
	 */
	public Url getUrl() {
		return getProperty(Url.class);
	}

	/**
	 * Sets a URL to a resource that contains additional information about the
	 * journal entry.
	 * @param url the URL or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-116">RFC 5545
	 * p.116-7</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-110">RFC 2445
	 * p.110-1</a>
	 */
	public void setUrl(Url url) {
		setProperty(Url.class, url);
	}

	/**
	 * Sets a URL to a resource that contains additional information about the
	 * journal entry.
	 * @param url the URL (e.g. "http://example.com/resource.ics") or null to
	 * remove
	 * @return the property that was created
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-116">RFC 5545
	 * p.116-7</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-110">RFC 2445
	 * p.110-1</a>
	 */
	public Url setUrl(String url) {
		Url prop = (url == null) ? null : new Url(url);
		setUrl(prop);
		return prop;
	}

	/**
	 * Gets how often the journal entry repeats.
	 * @return the recurrence rule or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-122">RFC 5545
	 * p.122-32</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-117">RFC 2445
	 * p.117-25</a>
	 */
	public RecurrenceRule getRecurrenceRule() {
		return getProperty(RecurrenceRule.class);
	}

	/**
	 * Sets how often the journal entry repeats.
	 * @param recur the recurrence rule or null to remove
	 * @return the property that was created
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-122">RFC 5545
	 * p.122-32</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-117">RFC 2445
	 * p.117-25</a>
	 */
	public RecurrenceRule setRecurrenceRule(Recurrence recur) {
		RecurrenceRule prop = (recur == null) ? null : new RecurrenceRule(recur);
		setRecurrenceRule(prop);
		return prop;
	}

	/**
	 * Sets how often the journal entry repeats.
	 * @param recurrenceRule the recurrence rule or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-122">RFC 5545
	 * p.122-32</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-117">RFC 2445
	 * p.117-25</a>
	 */
	public void setRecurrenceRule(RecurrenceRule recurrenceRule) {
		setProperty(RecurrenceRule.class, recurrenceRule);
	}

	/**
	 * Gets any attachments that are associated with the journal entry.
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
	 * Adds an attachment to the journal entry.
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
	 * Gets the people who are involved in the journal entry.
	 * @return the attendees (any changes made this list will affect the parent
	 * component object and vice versa)
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-107">RFC 5545
	 * p.107-9</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-102">RFC 2445
	 * p.102-4</a>
	 */
	public List<Attendee> getAttendees() {
		return getProperties(Attendee.class);
	}

	/**
	 * Adds a person who is involved in the journal entry.
	 * @param attendee the attendee
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-107">RFC 5545
	 * p.107-9</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-102">RFC 2445
	 * p.102-4</a>
	 */
	public void addAttendee(Attendee attendee) {
		addProperty(attendee);
	}

	/**
	 * Adds a person who is involved in the journal entry.
	 * @param email the attendee's email address
	 * @return the property that was created
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-107">RFC 5545
	 * p.107-9</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-102">RFC 2445
	 * p.102-4</a>
	 */
	public Attendee addAttendee(String email) {
		Attendee prop = new Attendee(null, email);
		addAttendee(prop);
		return prop;
	}

	/**
	 * Gets a list of "tags" or "keywords" that describe the journal entry.
	 * @return the categories (any changes made this list will affect the parent
	 * component object and vice versa)
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-81">RFC 5545
	 * p.81-2</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-78">RFC 2445
	 * p.78-9</a>
	 */
	public List<Categories> getCategories() {
		return getProperties(Categories.class);
	}

	/**
	 * Adds a list of "tags" or "keywords" that describe the journal entry. Note
	 * that a single property can hold multiple keywords.
	 * @param categories the categories to add
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-81">RFC 5545
	 * p.81-2</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-78">RFC 2445
	 * p.78-9</a>
	 */
	public void addCategories(Categories categories) {
		addProperty(categories);
	}

	/**
	 * Adds a list of "tags" or "keywords" that describe the journal entry.
	 * @param categories the categories to add
	 * @return the property that was created
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-81">RFC 5545
	 * p.81-2</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-78">RFC 2445
	 * p.78-9</a>
	 */
	public Categories addCategories(String... categories) {
		Categories prop = new Categories(categories);
		addCategories(prop);
		return prop;
	}

	/**
	 * Adds a list of "tags" or "keywords" that describe the journal entry.
	 * @param categories the categories to add
	 * @return the property that was created
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-81">RFC 5545
	 * p.81-2</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-78">RFC 2445
	 * p.78-9</a>
	 */
	public Categories addCategories(List<String> categories) {
		Categories prop = new Categories(categories);
		addCategories(prop);
		return prop;
	}

	/**
	 * Gets the comments attached to the journal entry.
	 * @return the comments (any changes made this list will affect the parent
	 * component object and vice versa)
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-83">RFC 5545
	 * p.83-4</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-80">RFC 2445
	 * p.80-1</a>
	 */
	public List<Comment> getComments() {
		return getProperties(Comment.class);
	}

	/**
	 * Adds a comment to the journal entry.
	 * @param comment the comment to add
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-83">RFC 5545
	 * p.83-4</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-80">RFC 2445
	 * p.80-1</a>
	 */
	public void addComment(Comment comment) {
		addProperty(comment);
	}

	/**
	 * Adds a comment to the journal entry.
	 * @param comment the comment to add
	 * @return the property that was created
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-83">RFC 5545
	 * p.83-4</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-80">RFC 2445
	 * p.80-1</a>
	 */
	public Comment addComment(String comment) {
		Comment prop = new Comment(comment);
		addComment(prop);
		return prop;
	}

	/**
	 * Gets the contacts associated with the journal entry.
	 * @return the contacts (any changes made this list will affect the parent
	 * component object and vice versa)
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-109">RFC 5545
	 * p.109-11</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-104">RFC 2445
	 * p.104-6</a>
	 */
	public List<Contact> getContacts() {
		return getProperties(Contact.class);
	}

	/**
	 * Adds a contact to the journal entry.
	 * @param contact the contact
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-109">RFC 5545
	 * p.109-11</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-104">RFC 2445
	 * p.104-6</a>
	 */
	public void addContact(Contact contact) {
		addProperty(contact);
	}

	/**
	 * Adds a contact to the journal entry.
	 * @param contact the contact (e.g. "ACME Co - (123) 555-1234")
	 * @return the property that was created
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-109">RFC 5545
	 * p.109-11</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-104">RFC 2445
	 * p.104-6</a>
	 */
	public Contact addContact(String contact) {
		Contact prop = new Contact(contact);
		addContact(prop);
		return prop;
	}

	/**
	 * Gets the detailed descriptions to the journal entry. The descriptions
	 * should be a more detailed version of the one provided by the
	 * {@link Summary} property.
	 * @return the descriptions (any changes made this list will affect the
	 * parent component object and vice versa)
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-84">RFC 5545
	 * p.84-5</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-81">RFC 2445
	 * p.81-2</a>
	 */
	public List<Description> getDescriptions() {
		return getProperties(Description.class);
	}

	/**
	 * Adds a detailed description to the journal entry. The description should
	 * be a more detailed version of the one provided by the {@link Summary}
	 * property.
	 * @param description the description
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-84">RFC 5545
	 * p.84-5</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-81">RFC 2445
	 * p.81-2</a>
	 */
	public void addDescription(Description description) {
		addProperty(description);
	}

	/**
	 * Adds a detailed description to the journal entry. The description should
	 * be a more detailed version of the one provided by the {@link Summary}
	 * property.
	 * @param description the description
	 * @return the property that was created
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-84">RFC 5545
	 * p.84-5</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-81">RFC 2445
	 * p.81-2</a>
	 */
	public Description addDescription(String description) {
		Description prop = new Description(description);
		addDescription(prop);
		return prop;
	}

	/**
	 * Gets the list of exceptions to the recurrence rule defined in the journal
	 * entry (if one is defined).
	 * @return the list of exceptions (any changes made this list will affect
	 * the parent component object and vice versa)
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-118">RFC 5545
	 * p.118-20</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-112">RFC 2445
	 * p.112-4</a>
	 */
	public List<ExceptionDates> getExceptionDates() {
		return getProperties(ExceptionDates.class);
	}

	/**
	 * Adds a list of exceptions to the recurrence rule defined in the journal
	 * entry (if one is defined). Note that this property can contain multiple
	 * dates.
	 * @param exceptionDates the list of exceptions
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-118">RFC 5545
	 * p.118-20</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-112">RFC 2445
	 * p.112-4</a>
	 */
	public void addExceptionDates(ExceptionDates exceptionDates) {
		addProperty(exceptionDates);
	}

	/**
	 * Gets the components that the journal entry is related to.
	 * @return the relationships (any changes made this list will affect the
	 * parent component object and vice versa)
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-115">RFC 5545
	 * p.115-6</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-109-10">RFC 2445
	 * p.109-10</a>
	 */
	public List<RelatedTo> getRelatedTo() {
		return getProperties(RelatedTo.class);
	}

	/**
	 * Adds a component that the journal entry is related to.
	 * @param relatedTo the relationship
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-115">RFC 5545
	 * p.115-6</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-109-10">RFC 2445
	 * p.109-10</a>
	 */
	public void addRelatedTo(RelatedTo relatedTo) {
		//TODO create a method that accepts a component and make the RelatedTo property invisible to the user
		//@formatter:off
		/*
		 * addRelation(RelationshipType relType, ICalComponent component) {
		 *   RelatedTo prop = new RelatedTo(component.getUid().getValue());
		 *   prop.setRelationshipType(relType);
		 *   addProperty(prop);
		 * }
		 */
		//@formatter:on
		addProperty(relatedTo);
	}

	/**
	 * Adds a component that the journal entry is related to.
	 * @param uid the UID of the other component
	 * @return the property that was created
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-115">RFC 5545
	 * p.115-6</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-109-10">RFC 2445
	 * p.109-10</a>
	 */
	public RelatedTo addRelatedTo(String uid) {
		RelatedTo prop = new RelatedTo(uid);
		addRelatedTo(prop);
		return prop;
	}

	/**
	 * Gets the list of dates/periods that help define the recurrence rule of
	 * this journal entry (if one is defined).
	 * @return the recurrence dates (any changes made this list will affect the
	 * parent component object and vice versa)
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-120">RFC 5545
	 * p.120-2</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-115">RFC 2445
	 * p.115-7</a>
	 */
	public List<RecurrenceDates> getRecurrenceDates() {
		return getProperties(RecurrenceDates.class);
	}

	/**
	 * Adds a list of dates/periods that help define the recurrence rule of this
	 * journal entry (if one is defined).
	 * @param recurrenceDates the recurrence dates
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-120">RFC 5545
	 * p.120-2</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-115">RFC 2445
	 * p.115-7</a>
	 */
	public void addRecurrenceDates(RecurrenceDates recurrenceDates) {
		addProperty(recurrenceDates);
	}

	/**
	 * Gets the response to a scheduling request.
	 * @return the response
	 * @see <a href="http://tools.ietf.org/html/rfc5546#section-3.6">RFC 5546
	 * Section 3.6</a>
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-141">RFC 5545
	 * p.141-3</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-134">RFC 2445
	 * p.134-6</a>
	 */
	public RequestStatus getRequestStatus() {
		return getProperty(RequestStatus.class);
	}

	/**
	 * Sets the response to a scheduling request.
	 * @param requestStatus the response
	 * @see <a href="http://tools.ietf.org/html/rfc5546#section-3.6">RFC 5546
	 * Section 3.6</a>
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-141">RFC 5545
	 * p.141-3</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-134">RFC 2445
	 * p.134-6</a>
	 */
	public void setRequestStatus(RequestStatus requestStatus) {
		setProperty(RequestStatus.class, requestStatus);
	}

	/**
	 * <p>
	 * Gets the exceptions for the {@link RecurrenceRule} property.
	 * </p>
	 * <p>
	 * Note that this property has been removed from the latest version of the
	 * iCal specification. Its use should be avoided.
	 * </p>
	 * @return the exception rules (any changes made this list will affect the
	 * parent component object and vice versa)
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-114">RFC 2445
	 * p.114-15</a>
	 */
	public List<ExceptionRule> getExceptionRules() {
		return getProperties(ExceptionRule.class);
	}

	/**
	 * <p>
	 * Adds an exception for the {@link RecurrenceRule} property.
	 * </p>
	 * <p>
	 * Note that this property has been removed from the latest version of the
	 * iCal specification. Its use should be avoided.
	 * </p>
	 * @param recur the exception rule to add
	 * @return the property that was created
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-114">RFC 2445
	 * p.114-15</a>
	 */
	public ExceptionRule addExceptionRule(Recurrence recur) {
		ExceptionRule prop = new ExceptionRule(recur);
		addExceptionRule(prop);
		return prop;
	}

	/**
	 * <p>
	 * Adds an exception for the {@link RecurrenceRule} property.
	 * </p>
	 * <p>
	 * Note that this property has been removed from the latest version of the
	 * iCal specification. Its use should be avoided.
	 * </p>
	 * @param exceptionRule the exception rule to add
	 * @see <a href="http://tools.ietf.org/html/rfc2445#page-114">RFC 2445
	 * p.114-15</a>
	 */
	public void addExceptionRule(ExceptionRule exceptionRule) {
		addProperty(exceptionRule);
	}

	/**
	 * Gets the color that clients may use when displaying the journal entry
	 * (for example, a background color).
	 * @return the property or null if not set
	 * @see <a
	 * href="http://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-9">draft-ietf-calext-extensions-01
	 * p.9</a>
	 */
	public Color getColor() {
		return getProperty(Color.class);
	}

	/**
	 * Sets the color that clients may use when displaying the journal entry
	 * (for example, a background color).
	 * @param color the property or null to remove
	 * @see <a
	 * href="http://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-9">draft-ietf-calext-extensions-01
	 * p.79</a>
	 */
	public void setColor(Color color) {
		setProperty(Color.class, color);
	}

	/**
	 * Sets the color that clients may use when displaying the journal entry
	 * (for example, a background color).
	 * @param color the color name (case insensitive) or null to remove.
	 * Acceptable values are defined in <a
	 * href="https://www.w3.org/TR/2011/REC-css3-color-20110607/#svg-color"
	 * >Section 4.3 of the CSS Color Module Level 3 Recommendation</a>. For
	 * example, "aliceblue", "green", "navy".
	 * @return the property object that was created
	 * @see <a
	 * href="http://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-9">draft-ietf-calext-extensions-01
	 * p.9</a>
	 */
	public Color setColor(String color) {
		Color prop = (color == null) ? null : new Color(color);
		setColor(prop);
		return prop;
	}

	/**
	 * Gets the images that are associated with the journal entry.
	 * @return the images (any changes made this list will affect the parent
	 * component object and vice versa)
	 * @see <a
	 * href="http://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-10">draft-ietf-calext-extensions-01
	 * p.10</a>
	 */
	public List<Image> getImages() {
		return getProperties(Image.class);
	}

	/**
	 * Adds an image that is associated with the journal entry.
	 * @param image the property to add
	 * @see <a
	 * href="http://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-10">draft-ietf-calext-extensions-01
	 * p.10</a>
	 */
	public void addImage(Image image) {
		addProperty(image);
	}

	/**
	 * <p>
	 * Creates an iterator that computes the dates defined by the
	 * {@link RecurrenceRule} and {@link RecurrenceDates} properties (if
	 * present), and excludes those dates which are defined by the
	 * {@link ExceptionRule} and {@link ExceptionDates} properties (if present).
	 * </p>
	 * <p>
	 * In order for {@link RecurrenceRule} and {@link ExceptionRule} properties
	 * to be included in this iterator, a {@link DateStart} property must be
	 * defined.
	 * </p>
	 * <p>
	 * {@link Period} values in {@link RecurrenceDates} properties are not
	 * supported and are ignored.
	 * </p>
	 * @param timezone the timezone to iterate in. This is needed in order to
	 * adjust for when the iterator passes over a daylight savings boundary.
	 * This parameter is ignored if the start date does not have a time
	 * component.
	 * @return the iterator
	 */
	public DateIterator getDateIterator(TimeZone timezone) {
		return Google2445Utils.getDateIterator(this, timezone);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void validate(List<ICalComponent> components, ICalVersion version, List<ValidationWarning> warnings) {
		if (version == ICalVersion.V1_0) {
			warnings.add(new ValidationWarning(48, version));
		}

		checkRequiredCardinality(warnings, Uid.class, DateTimeStamp.class);
		checkOptionalCardinality(warnings, Classification.class, Created.class, DateStart.class, LastModified.class, Organizer.class, RecurrenceId.class, Sequence.class, Status.class, Summary.class, Url.class, Color.class);
		checkStatus(warnings, Status.draft(), Status.final_(), Status.cancelled());

		//DTSTART and RECURRENCE-ID must have the same data type
		ICalDate recurrenceId = getValue(getRecurrenceId());
		ICalDate dateStart = getValue(getDateStart());
		if (recurrenceId != null && dateStart != null && dateStart.hasTime() != recurrenceId.hasTime()) {
			warnings.add(new ValidationWarning(19));
		}

		//BYHOUR, BYMINUTE, and BYSECOND cannot be specified in RRULE if DTSTART's data type is "date"
		//RFC 5545 p. 167
		Recurrence rrule = getValue(getRecurrenceRule());
		if (dateStart != null && rrule != null) {
			if (!dateStart.hasTime() && (!rrule.getByHour().isEmpty() || !rrule.getByMinute().isEmpty() || !rrule.getBySecond().isEmpty())) {
				warnings.add(new ValidationWarning(5));
			}
		}

		//there *should* be only 1 instance of RRULE
		//RFC 5545 p. 167
		if (getProperties(RecurrenceRule.class).size() > 1) {
			warnings.add(new ValidationWarning(6));
		}
	}

	@Override
	public VJournal copy() {
		return new VJournal(this);
	}
}
