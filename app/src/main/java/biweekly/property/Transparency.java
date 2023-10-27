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
 * Defines whether an event is visible to free/busy time searches or not. If an
 * event does not have this property, the event should be considered opaque
 * (visible) to searches.
 * </p>
 * <p>
 * <b>Code sample (creating):</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VEvent event = new VEvent();
 * 
 * Transparency transp = Transparency.opaque();
 * event.setTransparency(transp);
 * 
 * event.setTransparency(true); //hidden from searches
 * event.setTransparency(false); //visible to searches
 * </pre>
 * 
 * <p>
 * <b>Code sample (retrieving):</b>
 * </p>
 * 
 * <pre class="brush:java">
 * ICalendar ical = ...
 * for (VEvent event : ical.getEvents()) {
 *   Transparency transp = event.getTransparency();
 *   if (transp.isOpaque()) {
 *     //...
 *   } else if (transp.isTransparent()) {
 *     //...
 *   }
 * }
 * </pre>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5545#page-101">RFC 5545
 * p.101-2</a>
 * @see <a href="http://tools.ietf.org/html/rfc2445#page-96">RFC 2445 p.96-7</a>
 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.36-7</a>
 */
public class Transparency extends EnumProperty {
	public static final String OPAQUE = "OPAQUE";
	public static final String TRANSPARENT = "TRANSPARENT";

	/**
	 * Creates a new transparency property.
	 * @param value the value
	 */
	public Transparency(String value) {
		super(value);
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public Transparency(Transparency original) {
		super(original);
	}

	/**
	 * Creates a property that marks the event as being visible to free/busy
	 * time searches.
	 * @return the property
	 */
	public static Transparency opaque() {
		return create(OPAQUE);
	}

	/**
	 * Determines if the event is visible to free/busy time searches.
	 * @return true if it's visible, false if not
	 */
	public boolean isOpaque() {
		return is(OPAQUE);
	}

	/**
	 * Creates a property that marks the event as being hidden from free/busy
	 * time searches.
	 * @return the property
	 */
	public static Transparency transparent() {
		return create(TRANSPARENT);
	}

	/**
	 * Determines if the event is hidden from free/busy time searches.
	 * @return true if it's hidden, false if not
	 */
	public boolean isTransparent() {
		return is(TRANSPARENT);
	}

	private static Transparency create(String value) {
		return new Transparency(value);
	}

	@Override
	protected Collection<String> getStandardValues(ICalVersion version) {
		return Arrays.asList(OPAQUE, TRANSPARENT);
	}

	@Override
	public Transparency copy() {
		return new Transparency(this);
	}
}
