package biweekly.property;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import biweekly.ICalVersion;
import biweekly.component.VEvent;
import biweekly.component.VJournal;
import biweekly.component.VTodo;

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
 * Defines the status of the component that this property belongs to, such as a
 * to-do task being "completed".
 * </p>
 * 
 * <p>
 * <b>Code sample (creating):</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VTodo todo = new VTodo();
 * 
 * Status status = Status.completed();
 * todo.setStatus(status);
 * </pre>
 * 
 * <p>
 * <b>Code sample (retrieving):</b>
 * </p>
 * 
 * <pre class="brush:java">
 * ICalendar ical = ...
 * for (VTodo todo : ical.getTodos()) {
 *   Status status = todo.getStatus();
 *   if (action.isCompleted()) {
 *     //...
 *   } else if (action.isDraft()) {
 *     //...
 *   }
 *   //etc.
 * }
 * </pre>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5545#page-92">RFC 5545 p.92-3</a>
 * @see <a href="http://tools.ietf.org/html/rfc2445#page-88">RFC 2445 p.88-9</a>
 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.35-6</a>
 */
public class Status extends EnumProperty {
	//2.0
	public static final String CANCELLED = "CANCELLED";
	public static final String DRAFT = "DRAFT";
	public static final String FINAL = "FINAL";
	public static final String IN_PROGRESS = "IN-PROGRESS";

	//1.0
	public static final String ACCEPTED = "ACCEPTED";
	public static final String DECLINED = "DECLINED";
	public static final String DELEGATED = "DELEGATED";
	public static final String SENT = "SENT";

	//1.0 and 2.0
	public static final String COMPLETED = "COMPLETED";
	public static final String CONFIRMED = "CONFIRMED";
	public static final String NEEDS_ACTION = "NEEDS-ACTION";
	public static final String TENTATIVE = "TENTATIVE";

	/**
	 * Creates a status property. Use of this constructor is discouraged and may
	 * put the property in an invalid state. Use one of the static factory
	 * methods instead.
	 * @param status the status (e.g. "TENTATIVE")
	 */
	public Status(String status) {
		super(status);
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public Status(Status original) {
		super(original);
	}

	/**
	 * Creates a "tentative" status property (only valid for event components).
	 * @return the property
	 */
	public static Status tentative() {
		return create(TENTATIVE);
	}

	/**
	 * Determines if the status is set to "tentative".
	 * @return true if it is, false if not
	 */
	public boolean isTentative() {
		return is(TENTATIVE);
	}

	/**
	 * Creates a "confirmed" status property (only valid for event components).
	 * @return the property
	 */
	public static Status confirmed() {
		return create(CONFIRMED);
	}

	/**
	 * Determines if the status is set to "confirmed".
	 * @return true if it is, false if not
	 */
	public boolean isConfirmed() {
		return is(CONFIRMED);
	}

	/**
	 * Creates a "cancelled" status property (only valid in iCalendar 2.0 in
	 * {@link VEvent}, {@link VTodo}, and {@link VJournal} components).
	 * @return the property
	 */
	public static Status cancelled() {
		return create(CANCELLED);
	}

	/**
	 * Determines if the status is set to "cancelled".
	 * @return true if it is, false if not
	 */
	public boolean isCancelled() {
		return is(CANCELLED);
	}

	/**
	 * Creates a "needs-action" status property.
	 * @return the property
	 */
	public static Status needsAction() {
		return create(NEEDS_ACTION);
	}

	/**
	 * Determines if the status is set to "needs-action".
	 * @return true if it is, false if not
	 */
	public boolean isNeedsAction() {
		return is(NEEDS_ACTION);
	}

	/**
	 * Creates a "completed" status property (only valid in {@link VTodo}
	 * components).
	 * @return the property
	 */
	public static Status completed() {
		return create(COMPLETED);
	}

	/**
	 * Determines if the status is set to "completed".
	 * @return true if it is, false if not
	 */
	public boolean isCompleted() {
		return is(COMPLETED);
	}

	/**
	 * Creates a "in-progress" status property (only valid in iCalendar 2.0 in
	 * {@link VTodo} components).
	 * @return the property
	 */
	public static Status inProgress() {
		return create(IN_PROGRESS);
	}

	/**
	 * Determines if the status is set to "in-progress".
	 * @return true if it is, false if not
	 */
	public boolean isInProgress() {
		return is(IN_PROGRESS);
	}

	/**
	 * Creates a "draft" status property (only valid in iCalendar 2.0 in
	 * {@link VJournal} components).
	 * @return the property
	 */
	public static Status draft() {
		return create(DRAFT);
	}

	/**
	 * Determines if the status is set to "draft".
	 * @return true if it is, false if not
	 */
	public boolean isDraft() {
		return is(DRAFT);
	}

	/**
	 * Creates a "final" status property (only valid in iCalendar 2.0 in
	 * {@link VJournal} components).
	 * @return the property
	 */
	public static Status final_() {
		return create(FINAL);
	}

	/**
	 * Determines if the status is set to "final".
	 * @return true if it is, false if not
	 */
	public boolean isFinal() {
		return is(FINAL);
	}

	/**
	 * Creates an "accepted" status property (only valid in vCal 1.0 in
	 * {@link VTodo} components).
	 * @return the property
	 */
	public static Status accepted() {
		return create(ACCEPTED);
	}

	/**
	 * Determines if the status is set to "accepted".
	 * @return true if it is, false if not
	 */
	public boolean isAccepted() {
		return is(ACCEPTED);
	}

	/**
	 * Creates a "declined" status property (only valid in vCal 1.0).
	 * @return the property
	 */
	public static Status declined() {
		return create(DECLINED);
	}

	/**
	 * Determines if the status is set to "declined".
	 * @return true if it is, false if not
	 */
	public boolean isDeclined() {
		return is(DECLINED);
	}

	/**
	 * Creates a "delegated" status property (only valid in vCal 1.0).
	 * @return the property
	 */
	public static Status delegated() {
		return create(DELEGATED);
	}

	/**
	 * Determines if the status is set to "delegated".
	 * @return true if it is, false if not
	 */
	public boolean isDelegated() {
		return is(DELEGATED);
	}

	/**
	 * Creates a "sent" status property (only valid in vCal 1.0).
	 * @return the property
	 */
	public static Status sent() {
		return create(SENT);
	}

	/**
	 * Determines if the status is set to "sent".
	 * @return true if it is, false if not
	 */
	public boolean isSent() {
		return is(SENT);
	}

	public static Status create(String status) {
		return new Status(status);
	}

	@Override
	protected Collection<String> getStandardValues(ICalVersion version) {
		switch (version) {
		case V1_0:
			return Arrays.asList(ACCEPTED, COMPLETED, CONFIRMED, DECLINED, DELEGATED, NEEDS_ACTION, SENT, TENTATIVE);
		default:
			return Arrays.asList(CANCELLED, COMPLETED, CONFIRMED, DRAFT, FINAL, IN_PROGRESS, NEEDS_ACTION, TENTATIVE);
		}
	}

	@Override
	protected Collection<ICalVersion> getValueSupportedVersions() {
		if (value == null) {
			return Collections.emptyList();
		}

		if (isCompleted() || isConfirmed() || isNeedsAction() || isTentative()) {
			return Arrays.asList(ICalVersion.values());
		}
		if (isCancelled() || isDraft() || isFinal() || isInProgress()) {
			return Arrays.asList(ICalVersion.V2_0_DEPRECATED, ICalVersion.V2_0);
		}
		if (isAccepted() || isDeclined() || isDelegated() || isSent()) {
			return Collections.singletonList(ICalVersion.V1_0);
		}

		return Collections.emptyList();
	}

	@Override
	public Status copy() {
		return new Status(this);
	}
}
