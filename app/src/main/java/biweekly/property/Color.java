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
 * Defines a color that clients may use when displaying the component data.
 * Clients may use this color in any way they wish. For example, they can use it
 * as a background color.
 * </p>
 * <p>
 * Acceptable values are defined in <a
 * href="https://www.w3.org/TR/2011/REC-css3-color-20110607/#svg-color">Section
 * 4.3 of the CSS Color Module Level 3 Recommendation</a>. For example,
 * "aliceblue", "green", "navy".
 * </p>
 * <p>
 * <b>Code sample:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VEvent event = new VEvent();
 * 
 * Color color = new Color("mistyrose");
 * event.setColor(color);
 * </pre>
 * @author Michael Angstadt
 * @see <a
 * href="http://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-9">draft-ietf-calext-extensions-01
 * p.9</a>
 */
public class Color extends TextProperty {
	/**
	 * Creates a color property.
	 * @param color the color name (case insensitive). Acceptable values are
	 * defined in <a
	 * href="https://www.w3.org/TR/2011/REC-css3-color-20110607/#svg-color"
	 * >Section 4.3 of the CSS Color Module Level 3 Recommendation</a>. For
	 * example, "aliceblue", "green", "navy".
	 */
	public Color(String color) {
		super(color);
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public Color(Color original) {
		super(original);
	}

	/**
	 * Gets the value of this property.
	 * @return the value (case insensitive). Acceptable values are defined in <a
	 * href="https://www.w3.org/TR/2011/REC-css3-color-20110607/#svg-color"
	 * >Section 4.3 of the CSS Color Module Level 3 Recommendation</a>. For
	 * example, "aliceblue", "green", "navy".
	 */
	@Override
	public String getValue() {
		return super.getValue();
	}

	/**
	 * Sets the value of this property.
	 * @param value the value (case insensitive). Acceptable values are defined
	 * in <a
	 * href="https://www.w3.org/TR/2011/REC-css3-color-20110607/#svg-color"
	 * >Section 4.3 of the CSS Color Module Level 3 Recommendation</a>. For
	 * example, "aliceblue", "green", "navy".
	 */
	@Override
	public void setValue(String value) {
		super.setValue(value);
	}

	@Override
	public Color copy() {
		return new Color(this);
	}

	@Override
	protected int valueHashCode() {
		return value.toLowerCase().hashCode();
	}

	@Override
	protected boolean valueEquals(String otherValue) {
		return value.equalsIgnoreCase(otherValue);
	}
}
