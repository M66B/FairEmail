package biweekly.property;

import java.util.Map;

import biweekly.component.VAlarm;

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
 * Defines an alarm that displays a message when triggered. It is recommended
 * that the {@link VAlarm} component be used to created alarms.
 * @author Michael Angstadt
 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.30</a>
 * @see VAlarm#display
 */
public class DisplayAlarm extends VCalAlarmProperty {
	private String text;

	public DisplayAlarm(String text) {
		this.text = text;
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public DisplayAlarm(DisplayAlarm original) {
		super(original);
		text = original.text;
	}

	/**
	 * Gets the text to display when the alarm is triggered.
	 * @return the display text
	 */
	public String getText() {
		return text;
	}

	/**
	 * Sets the text to display when the alarm is triggered.
	 * @param text the display text
	 */
	public void setText(String text) {
		this.text = text;
	}

	@Override
	protected Map<String, Object> toStringValues() {
		Map<String, Object> values = super.toStringValues();
		values.put("text", text);
		return values;
	}

	@Override
	public DisplayAlarm copy() {
		return new DisplayAlarm(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		DisplayAlarm other = (DisplayAlarm) obj;
		if (text == null) {
			if (other.text != null) return false;
		} else if (!text.equals(other.text)) return false;
		return true;
	}
}
