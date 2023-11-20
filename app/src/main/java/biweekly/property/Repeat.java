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
 * Defines the number of times an alarm should be repeated after its initial
 * trigger. Used in conjunction with {@link DurationProperty}, which defines the
 * length of the pause between repeats.
 * </p>
 * <p>
 * <b>Code sample:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * //repeat 5 more times after the first time
 * VAlarm alarm = ...;
 * alarm.setRepeat(5);
 * </pre>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5545#page-133">RFC 5545 p.133</a>
 * @see <a href="http://tools.ietf.org/html/rfc2445#page-126">RFC 2445
 * p.126-7</a>
 */
public class Repeat extends IntegerProperty {
	/**
	 * Creates a repeat property.
	 * @param count the number of times to repeat the alarm (e.g. "2" to repeat
	 * it two more times after it was initially triggered, for a total of three
	 * times)
	 */
	public Repeat(Integer count) {
		super(count);
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public Repeat(Repeat original) {
		super(original);
	}

	@Override
	public Repeat copy() {
		return new Repeat(this);
	}
}
