package biweekly.property;

import java.util.List;

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
 * Defines a list of keywords that describe the component to which it belongs.
 * </p>
 * <p>
 * <b>Code sample:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VEvent event = new VEvent();
 * 
 * Categories categories = new Categories("conference", "meeting");
 * event.addCategories(categories);
 * </pre>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5545#page-81">RFC 5545 p.81-2</a>
 * @see <a href="http://tools.ietf.org/html/rfc2445#page-78">RFC 2445 p.78-9</a>
 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.28</a>
 * @see <a
 * href="http://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-7">draft-ietf-calext-extensions-01
 * p.7</a>
 */
public class Categories extends ListProperty<String> {
	/**
	 * Creates a new categories property.
	 */
	public Categories() {
		super();
	}

	/**
	 * Creates a new categories property.
	 * @param categories the categories to initialize the property with
	 */
	public Categories(String... categories) {
		super(categories);
	}

	/**
	 * Creates a new categories property.
	 * @param categories the categories to initialize the property with
	 */
	public Categories(List<String> categories) {
		super(categories);
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public Categories(Categories original) {
		super(original);
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
	public Categories copy() {
		return new Categories(this);
	}
}
