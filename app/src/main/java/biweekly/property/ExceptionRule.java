package biweekly.property;

import java.util.List;

import biweekly.ICalVersion;
import biweekly.ValidationWarning;
import biweekly.component.ICalComponent;
import biweekly.util.Recurrence;

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
 * Defines a list of exceptions to the dates specified in the
 * {@link RecurrenceRule} property.
 * </p>
 * <p>
 * Note that this property has been removed from the latest version of the iCal
 * specification. Its use should be avoided.
 * </p>
 * <p>
 * <b>Code sample:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VEvent event = new VEvent();
 * 
 * //"bi-weekly"
 * Recurrence recur = new Recurrence.Builder(Frequency.WEEKLY).interval(2).build();
 * ExceptionRule exrule = new ExceptionRule(recur);
 * event.addExceptionRule(exrule);
 * </pre>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc2445#page-114">RFC 2445
 * p.114-15</a>
 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.31</a>
 */
public class ExceptionRule extends RecurrenceProperty {
	/**
	 * Creates a new exception rule property.
	 * @param recur the recurrence rule
	 */
	public ExceptionRule(Recurrence recur) {
		super(recur);
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public ExceptionRule(ExceptionRule original) {
		super(original);
	}

	@Override
	protected void validate(List<ICalComponent> components, ICalVersion version, List<ValidationWarning> warnings) {
		super.validate(components, version, warnings);

		if (version == ICalVersion.V2_0) {
			warnings.add(new ValidationWarning(37));
		}
	}

	@Override
	public ExceptionRule copy() {
		return new ExceptionRule(this);
	}
}
