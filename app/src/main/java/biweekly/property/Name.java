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
 * Defines a human-readable name for the calendar as a whole.
 * </p>
 * <p>
 * An {@link ICalendar} component can only have one name, but multiple Name
 * properties can exist in order to specify the name in multiple languages. In
 * this case, each property instance must be assigned a LANGUAGE parameter.
 * </p>
 * <p>
 * <b>Single language:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * ICalendar ical = new ICalendar();
 * 
 * Name name = new Name("Company Vacation Days");
 * ical.addName(name);
 * </pre>
 * 
 * <p>
 * <b>Multiple languages:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * ICalendar ical = new ICalendar();
 * 
 * Name englishName = new Name("Company Vacation Days");
 * englishName.setLanguage("en");
 * ical.addName(englishName);
 * 
 * Name frenchName = new Name("Société Jours de Vacances");
 * frenchName.setLanguage("fr");
 * ical.addName(frenchName);
 * </pre>
 * @author Michael Angstadt
 * @see <a
 * href="http://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-5">draft-ietf-calext-extensions-01
 * p.5</a>
 */
public class Name extends TextProperty {
	/**
	 * Creates a name property.
	 * @param name the name of the calendar
	 */
	public Name(String name) {
		super(name);
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public Name(Name original) {
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
	public Name copy() {
		return new Name(this);
	}
}
