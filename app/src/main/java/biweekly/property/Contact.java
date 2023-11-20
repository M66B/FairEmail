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
 * Defines the contact information for a person or other entity (for example,
 * the name of a business and its phone number).
 * </p>
 * <p>
 * <b>Code sample:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VEvent event = new VEvent();
 * 
 * Contact contact = new Contact("Acme Co: (212) 555-1234");
 * event.addContact(contact);
 * </pre>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5545#page-109">RFC 5545
 * p.109-11</a>
 * @see <a href="http://tools.ietf.org/html/rfc2445#page-104">RFC 2445
 * p.104-6</a>
 */
public class Contact extends TextProperty {
	/**
	 * Creates a contact property.
	 * @param contact the contact information (e.g. "Acme Co: (212) 555-1234")
	 */
	public Contact(String contact) {
		super(contact);
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public Contact(Contact original) {
		super(original);
	}

	/**
	 * @return the URI (such as a URL to a vCard) or null if not set
	 */
	@Override
	public String getAltRepresentation() {
		return super.getAltRepresentation();
	}

	/**
	 * @param uri the URI (such as a URL to a vCard) or null to remove
	 */
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
	public Contact copy() {
		return new Contact(this);
	}
}
