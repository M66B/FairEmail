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
 * Defines a detailed description of the component that this property belongs
 * to. The description should be a more detailed version of the text provided by
 * the {@link Summary} property.
 * </p>
 * <p>
 * If defined in the top-level {@link ICalendar} component, it contains a
 * detailed description of the calendar as a whole. An iCalendar object can only
 * have one description, but multiple Description properties can exist in order
 * to specify the description in multiple languages. In this case, each property
 * instance must be assigned a LANGUAGE parameter.
 * </p>
 * <p>
 * <b>Code sample:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VEvent event = new VEvent();
 * 
 * Description description = new Description("During this meeting, we will discuss...");
 * event.setDescription(description);
 * </pre>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5545#page-84">RFC 5545 p.84-5</a>
 * @see <a href="http://tools.ietf.org/html/rfc2445#page-81">RFC 2445 p.81-2</a>
 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.30</a>
 * @see <a
 * href="http://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-6">draft-ietf-calext-extensions-01
 * p.6</a>
 */
public class Description extends TextProperty {
	/**
	 * Creates a description property.
	 * @param description the description
	 */
	public Description(String description) {
		super(description);
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public Description(Description original) {
		super(original);
	}

	@Override
	public String getAltRepresentation() {
		return super.getAltRepresentation();
	}

	@Override
	public void setAltRepresentation(String uri) {
		super.setAltRepresentation(uri);
	}

	@Override
	public String getLanguage() {
		return super.getLanguage();
	}

	@Override
	public void setLanguage(String language) {
		super.setLanguage(language);
	}

	@Override
	public Description copy() {
		return new Description(this);
	}
}
