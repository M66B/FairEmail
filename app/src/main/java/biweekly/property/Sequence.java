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
 * Defines a revision number for an event, to-do task, or journal entry. This
 * number can be incremented every time a significant change is made to the
 * component.
 * </p>
 * <p>
 * <b>Code sample:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VEvent event = ...
 * event.setSequence(2);
 * </pre>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5545#page-138">RFC 5545
 * p.138-9</a>
 * @see <a href="http://tools.ietf.org/html/rfc2445#page-131">RFC 2445
 * p.131-3</a>
 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.35</a>
 */
public class Sequence extends IntegerProperty {
	/**
	 * Creates a sequence property.
	 * @param sequence the sequence number (e.g. "0" for the initial version,
	 * "1" for the first revision, etc)
	 */
	public Sequence(Integer sequence) {
		super(sequence);
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public Sequence(Sequence original) {
		super(original);
	}

	/**
	 * Increments the sequence number.
	 */
	public void increment() {
		if (value == null) {
			value = 1;
		} else {
			value++;
		}
	}

	@Override
	public Sequence copy() {
		return new Sequence(this);
	}
}
