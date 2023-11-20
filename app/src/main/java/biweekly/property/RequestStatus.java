package biweekly.property;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import biweekly.ICalVersion;
import biweekly.ValidationWarning;
import biweekly.component.ICalComponent;

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
 * Represents a response to a scheduling request.
 * </p>
 * <p>
 * This property must have a status code defined. The iCalendar specification
 * defines the following status code families. However, these can have different
 * meanings depending upon the type of scheduling request system being used
 * (such as <a href="http://tools.ietf.org/html/rfc5546">iTIP</a>).
 * </p>
 * <ul>
 * <li><b>1.x</b> - The request has been received, but is still being processed.
 * </li>
 * <li><b>2.x</b> - The request was processed successfully.</li>
 * <li><b>3.x</b> - There is a client-side problem with the request (such as
 * some incorrect syntax).</li>
 * <li><b>4.x</b> - A scheduling error occurred on the server that prevented the
 * request from being processed.</li>
 * </ul>
 * <p>
 * <b>Code sample:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VEvent event = new VEvent();
 * 
 * RequestStatus requestStatus = new RequestStatus("2.0");
 * requestStatus.setDescription("Success");
 * event.setRequestStatus(requestStatus);
 * </pre>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5546#section-3.6">RFC 5546
 * Section 3.6</a>
 * @see <a href="http://tools.ietf.org/html/rfc5545#page-141">RFC 5545
 * p.141-3</a>
 * @see <a href="http://tools.ietf.org/html/rfc2445#page-134">RFC 2445
 * p.134-6</a>
 */
public class RequestStatus extends ICalProperty {
	private String statusCode, description, exceptionText;

	/**
	 * Creates a request status property.
	 * @param statusCode the status code (e.g. "1.1.3")
	 */
	public RequestStatus(String statusCode) {
		setStatusCode(statusCode);
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public RequestStatus(RequestStatus original) {
		super(original);
		statusCode = original.statusCode;
		description = original.description;
		exceptionText = original.exceptionText;
	}

	/**
	 * Gets the status code. The following status code families are defined:
	 * <ul>
	 * <li><b>1.x</b> - The request has been received, but is still being
	 * processed.</li>
	 * <li><b>2.x</b> - The request was processed successfully.</li>
	 * <li><b>3.x</b> - There is a client-side problem with the request (such as
	 * some incorrect syntax).</li>
	 * <li><b>4.x</b> - A server-side error occurred.</li>
	 * </ul>
	 * @return the status code (e.g. "1.1.3")
	 */
	public String getStatusCode() {
		return statusCode;
	}

	/**
	 * Sets a status code. The following status code families are defined:
	 * <ul>
	 * <li><b>1.x</b> - The request has been received, but is still being
	 * processed.</li>
	 * <li><b>2.x</b> - The request was processed successfully.</li>
	 * <li><b>3.x</b> - There is a client-side problem with the request (such as
	 * some incorrect syntax).</li>
	 * <li><b>4.x</b> - A server-side error occurred.</li>
	 * </ul>
	 * @param statusCode the status code (e.g. "1.1.3")
	 */
	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}

	/**
	 * Gets the human-readable description of the status.
	 * @return the description (e.g. "Success") or null if not set
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets a human-readable description of the status.
	 * @param description the description (e.g. "Success") or null to remove
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Gets any additional data related to the response.
	 * @return the additional data or null if not set
	 */
	public String getExceptionText() {
		return exceptionText;
	}

	/**
	 * Sets any additional data related to the response.
	 * @param exceptionText the additional data or null to remove
	 */
	public void setExceptionText(String exceptionText) {
		this.exceptionText = exceptionText;
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
	protected void validate(List<ICalComponent> components, ICalVersion version, List<ValidationWarning> warnings) {
		if (statusCode == null) {
			warnings.add(new ValidationWarning(36));
		}
	}

	@Override
	protected Map<String, Object> toStringValues() {
		Map<String, Object> values = new LinkedHashMap<String, Object>();
		values.put("statusCode", statusCode);
		values.put("description", description);
		values.put("exceptionText", exceptionText);
		return values;
	}

	@Override
	public RequestStatus copy() {
		return new RequestStatus(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((exceptionText == null) ? 0 : exceptionText.hashCode());
		result = prime * result + ((statusCode == null) ? 0 : statusCode.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		RequestStatus other = (RequestStatus) obj;
		if (description == null) {
			if (other.description != null) return false;
		} else if (!description.equals(other.description)) return false;
		if (exceptionText == null) {
			if (other.exceptionText != null) return false;
		} else if (!exceptionText.equals(other.exceptionText)) return false;
		if (statusCode == null) {
			if (other.statusCode != null) return false;
		} else if (!statusCode.equals(other.statusCode)) return false;
		return true;
	}
}
