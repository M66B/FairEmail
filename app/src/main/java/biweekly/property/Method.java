package biweekly.property;

import java.util.Arrays;
import java.util.Collection;

import biweekly.ICalVersion;

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
 * Specifies the type of <a href="http://tools.ietf.org/html/rfc5546">iTIP</a>
 * request that the iCalendar object represents. If the iCalendar object is just
 * being used as a container to hold calendar information, then this property
 * does not need to be defined.
 * </p>
 * <p>
 * If the iCalendar object is defined as a MIME message entity, this property
 * MUST be set to the value of the "Content-Type" header's "method" parameter,
 * if present.
 * </p>
 * <p>
 * <b>Code sample:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * ICalendar ical = new ICalendar();
 * 
 * Method method = Method.request();
 * ical.setMethod(method);
 * </pre>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5546">RFC 5546</a>
 * @see <a href="http://tools.ietf.org/html/rfc5545#page-77">RFC 5545 p.77-8</a>
 * @see <a href="http://tools.ietf.org/html/rfc2445#page-74">RFC 2445 p.74-5</a>
 */
public class Method extends EnumProperty {
	public static final String ADD = "ADD";
	public static final String CANCEL = "CANCEL";
	public static final String COUNTER = "COUNTER";
	public static final String DECLINECOUNTER = "DECLINECOUNTER";
	public static final String PUBLISH = "PUBLISH";
	public static final String REFRESH = "REFRESH";
	public static final String REPLY = "REPLY";
	public static final String REQUEST = "REQUEST";

	/**
	 * Creates a new method property.
	 * @param value the property value
	 */
	public Method(String value) {
		super(value);
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public Method(Method original) {
		super(original);
	}

	/**
	 * Constructs a METHOD property whose value is "ADD".
	 * @return the property
	 */
	public static Method add() {
		return create(ADD);
	}

	/**
	 * Determines if this property's value is "ADD".
	 * @return true if the value is "ADD", false if not
	 */
	public boolean isAdd() {
		return is(ADD);
	}

	/**
	 * Constructs a METHOD property whose value is "CANCEL".
	 * @return the property
	 */
	public static Method cancel() {
		return create(CANCEL);
	}

	/**
	 * Determines if this property's value is "CANCEL".
	 * @return true if the value is "CANCEL", false if not
	 */
	public boolean isCancel() {
		return is(CANCEL);
	}

	/**
	 * Constructs a METHOD property whose value is "COUNTER".
	 * @return the property
	 */
	public static Method counter() {
		return create(COUNTER);
	}

	/**
	 * Determines if this property's value is "COUNTER".
	 * @return true if the value is "COUNTER", false if not
	 */
	public boolean isCounter() {
		return is(COUNTER);
	}

	/**
	 * Constructs a METHOD property whose value is "DECLINECOUNTER".
	 * @return the property
	 */
	public static Method declineCounter() {
		return create(DECLINECOUNTER);
	}

	/**
	 * Determines if this property's value is "DECLINECOUNTER".
	 * @return true if the value is "DECLINECOUNTER", false if not
	 */
	public boolean isDeclineCounter() {
		return is(DECLINECOUNTER);
	}

	/**
	 * Constructs a METHOD property whose value is "PUBLISH".
	 * @return the property
	 */
	public static Method publish() {
		return create(PUBLISH);
	}

	/**
	 * Determines if this property's value is "PUBLISH".
	 * @return true if the value is "PUBLISH", false if not
	 */
	public boolean isPublish() {
		return is(PUBLISH);
	}

	/**
	 * Constructs a METHOD property whose value is "REFRESH".
	 * @return the property
	 */
	public static Method refresh() {
		return create(REFRESH);
	}

	/**
	 * Determines if this property's value is "REFRESH".
	 * @return true if the value is "REFRESH", false if not
	 */
	public boolean isRefresh() {
		return is(REFRESH);
	}

	/**
	 * Constructs a METHOD property whose value is "REPLY".
	 * @return the property
	 */
	public static Method reply() {
		return create(REPLY);
	}

	/**
	 * Determines if this property's value is "REPLY".
	 * @return true if the value is "REPLY", false if not
	 */
	public boolean isReply() {
		return is(REPLY);
	}

	/**
	 * Constructs a METHOD property whose value is "REQUEST".
	 * @return the property
	 */
	public static Method request() {
		return create(REQUEST);
	}

	/**
	 * Determines if this property's value is "REQUEST".
	 * @return true if the value is "REQUEST", false if not
	 */
	public boolean isRequest() {
		return is(REQUEST);
	}

	private static Method create(String value) {
		return new Method(value);
	}

	@Override
	protected Collection<String> getStandardValues(ICalVersion version) {
		return Arrays.asList(ADD, CANCEL, COUNTER, DECLINECOUNTER, PUBLISH, REFRESH, REPLY, REQUEST);
	}

	@Override
	public Method copy() {
		return new Method(this);
	}
}
