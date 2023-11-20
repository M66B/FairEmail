package biweekly.parameter;

import java.util.Collection;

import biweekly.property.Conference;

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
 * Defines the features of a {@link Conference}.
 * @author Michael Angstadt
 * @see <a
 * href="http://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-15">draft-ietf-calext-extensions-01
 * p.15</a>
 */
public class Feature extends EnumParameterValue {
	private static final ICalParameterCaseClasses<Feature> enums = new ICalParameterCaseClasses<Feature>(Feature.class);

	/**
	 * The conference has audio.
	 */
	public static final Feature AUDIO = new Feature("AUDIO");

	/**
	 * The conference has chat or instant messaging.
	 */
	public static final Feature CHAT = new Feature("CHAT");

	/**
	 * The conference has some kind of feed, such as an Atom or RSS feed.
	 */
	public static final Feature FEED = new Feature("FEED");

	/**
	 * Indicates that the property value is specific to the owner of the
	 * conference.
	 */
	public static final Feature MODERATOR = new Feature("MODERATOR");

	/**
	 * The conference is a phone conference.
	 */
	public static final Feature PHONE = new Feature("PHONE");

	/**
	 * The conference supports screen sharing.
	 */
	public static final Feature SCREEN = new Feature("SCREEN");

	/**
	 * The conference is a video conference.
	 */
	public static final Feature VIDEO = new Feature("VIDEO");

	private Feature(String value) {
		super(value);
	}

	/**
	 * Searches for a parameter value that is defined as a static constant in
	 * this class.
	 * @param value the parameter value
	 * @return the object or null if not found
	 */
	public static Feature find(String value) {
		return enums.find(value);
	}

	/**
	 * Searches for a parameter value and creates one if it cannot be found. All
	 * objects are guaranteed to be unique, so they can be compared with
	 * {@code ==} equality.
	 * @param value the parameter value
	 * @return the object
	 */
	public static Feature get(String value) {
		return enums.get(value);
	}

	/**
	 * Gets all of the parameter values that are defined as static constants in
	 * this class.
	 * @return the parameter values
	 */
	public static Collection<Feature> all() {
		return enums.all();
	}
}
