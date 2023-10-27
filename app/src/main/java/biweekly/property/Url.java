package biweekly.property;

import biweekly.ICalendar;

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
 * Defines a website that contains additional information about a component.
 * </p>
 * <p>
 * If defined in the top-level {@link ICalendar} component, this property
 * defines the location of a more dynamic, alternate representation of the
 * calendar (for example, a Google Calendar).
 * </p>
 * <p>
 * <b>Code sample:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VEvent event = new VEvent();
 * 
 * Url url = new Url("http://example.com");
 * event.setUrl(url);
 * </pre>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5545#page-116">RFC 5545
 * p.116-7</a>
 * @see <a href="http://tools.ietf.org/html/rfc2445#page-110">RFC 2445
 * p.110-1</a>
 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.37</a>
 * @see <a
 * href="http://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-6">draft-ietf-calext-extensions-01
 * p.6</a>
 */
public class Url extends TextProperty {
	/**
	 * Creates a URL property.
	 * @param url the URL (e.g. "http://example.com/resource.ics")
	 */
	public Url(String url) {
		super(url);
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public Url(Url original) {
		super(original);
	}

	@Override
	public Url copy() {
		return new Url(this);
	}
}
