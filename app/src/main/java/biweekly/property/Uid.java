package biweekly.property;

import java.util.UUID;

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
 * Defines a unique identifier for a component. Note that all components that
 * require UID properties are automatically given a random one on creation.
 * </p>
 * <p>
 * <b>Code sample:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VEvent event = new VEvent();
 * 
 * Uid uid = new Uid("19970610T172345Z-AF23B2@example.com");
 * event.setUid(uid);
 * </pre>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5545#page-117">RFC 5545
 * p.117-8</a>
 * @see <a href="http://tools.ietf.org/html/rfc2445#page-111">RFC 2445
 * p.111-2</a>
 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.37</a>
 * @see <a
 * href="http://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-6">draft-ietf-calext-extensions-01
 * p.6</a>
 */
public class Uid extends TextProperty {
	/**
	 * Creates a UID property.
	 * @param uid the UID (can be anything)
	 */
	public Uid(String uid) {
		super(uid);
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public Uid(Uid original) {
		super(original);
	}

	/**
	 * Creates a UID property that contains a {@link UUID universally unique
	 * identifier}.
	 * @return the property
	 */
	public static Uid random() {
		String uuid = UUID.randomUUID().toString();
		return new Uid(uuid);
	}

	@Override
	public Uid copy() {
		return new Uid(this);
	}
}
