package biweekly.property;

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
 * Defines a short, one line summary of the component that this property belongs
 * to. The summary should be a more concise version of the text provided by the
 * {@link Description} property.
 * </p>
 * <p>
 * <b>Code sample:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VEvent event = new VEvent();
 * 
 * Summary summary = new Summary("Team Meeting");
 * event.setSummary(summary);
 * </pre>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5545#page-93">RFC 5545 p.93-4</a>
 * @see <a href="http://tools.ietf.org/html/rfc2445#page-89">RFC 2445
 * p.89-90</a>
 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.36</a>
 */
public class Summary extends TextProperty {
	/**
	 * Creates a new summary property.
	 * @param summary the summary
	 */
	public Summary(String summary) {
		super(summary);
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public Summary(Summary original) {
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
	public Summary copy() {
		return new Summary(this);
	}
}
