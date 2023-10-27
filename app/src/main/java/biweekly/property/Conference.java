package biweekly.property;

import java.util.List;

import biweekly.parameter.Feature;

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
 * Contains information used for accessing a conference system.
 * </p>
 * <p>
 * <b>Code sample:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VEvent event = new VEvent();
 * 
 * Conference conference = new Conference("tel:+1-412-555-0123,,,654321");
 * conference.setLabel("Audio conference, access code=77869");
 * conference.getFeatures().add(Feature.AUDIO);
 * conference.getFeatures().add(Feature.PHONE);
 * event.addConference(conference);
 * </pre>
 * @author Michael Angstadt
 * @see <a
 * href="http://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-11">draft-ietf-calext-extensions-01
 * p.11</a>
 */
public class Conference extends ICalProperty {
	private String uri, text;

	/**
	 * Creates a conference property.
	 * @param uri the uri
	 */
	public Conference(String uri) {
		this.uri = uri;
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public Conference(Conference original) {
		super(original);
		uri = original.uri;
		text = original.text;
	}

	/**
	 * Gets the URI value of this property.
	 * @return the URI value or null if not set
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * Sets the value of this property to a URI.
	 * @param uri the URI
	 */
	public void setUri(String uri) {
		this.uri = uri;
		text = null;
	}

	/**
	 * Gets the plain text value of this property.
	 * @return the plain text value or null if not set
	 */
	public String getText() {
		return text;
	}

	/**
	 * Sets the value of this property to a plain text value.
	 * @param text the plain text value
	 */
	public void setText(String text) {
		this.text = text;
		uri = null;
	}

	/**
	 * Gets the list that holds the features that this conference supports (for
	 * example, audio and video).
	 * @return the features (this list is mutable)
	 */
	public List<Feature> getFeatures() {
		return parameters.getFeatures();
	}

	@Override
	public String getLabel() {
		return super.getLabel();
	}

	@Override
	public void setLabel(String label) {
		super.setLabel(label);
	}

	@Override
	public Conference copy() {
		return new Conference(this);
	}
}
