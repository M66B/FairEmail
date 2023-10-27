package biweekly.property;

import biweekly.parameter.RelationshipType;

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
 * Defines a relationship between the component that this property belongs to
 * and another component.
 * </p>
 * <p>
 * <b>Code samples:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VEvent event = new VEvent();
 * 
 * RelatedTo relatedTo = new RelatedTo("uid-value");
 * event.addRelatedTo(relatedTo);
 * </pre>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5545#page-115">RFC 5545
 * p.115-6</a>
 * @see <a href="http://tools.ietf.org/html/rfc2445#page-109-10">RFC 2445
 * p.109-10</a>
 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.33-4</a>
 */
public class RelatedTo extends TextProperty {
	/**
	 * Creates a related-to property.
	 * @param uid the value of the {@link Uid} property of the component that
	 * this property is referencing
	 */
	public RelatedTo(String uid) {
		super(uid);
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public RelatedTo(RelatedTo original) {
		super(original);
	}

	/**
	 * Gets the relationship type.
	 * @return the relationship type (e.g. "child") or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-25">RFC 5545
	 * p.25</a>
	 */
	public RelationshipType getRelationshipType() {
		return parameters.getRelationshipType();
	}

	/**
	 * Sets the relationship type.
	 * @param relationshipType the relationship type (e.g. "child") or null to
	 * remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-25">RFC 5545
	 * p.25</a>
	 */
	public void setRelationshipType(RelationshipType relationshipType) {
		parameters.setRelationshipType(relationshipType);
	}

	@Override
	public RelatedTo copy() {
		return new RelatedTo(this);
	}
}
