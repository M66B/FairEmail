package biweekly.property;

import java.io.File;
import java.io.IOException;
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
 * Defines an file attachment (such as an image or document) that is associated
 * with the component to which it belongs.
 * </p>
 * 
 * <p>
 * <b>Code sample:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VEvent event = new VEvent();
 * 
 * //from a byte array
 * byte[] data = ...
 * Attachment attach = new Attachment("image/png", data);
 * event.addAttachment(attach);
 * 
 * //from a file 
 * File file = new File("image.png");
 * attach = new Attachment("image/png", file);
 * event.addAttachment(attach);
 * 
 * //referencing a URL
 * attach = new Attachment("image/png", "http://example.com/image.png");
 * event.addAttachment(attach);
 * </pre>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5545#page-80">RFC 5545 p.80-1</a>
 * @see <a href="http://tools.ietf.org/html/rfc2445#page-77">RFC 2445 p.77-8</a>
 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.25</a>
 */
public class Attachment extends BinaryProperty {
	private String contentId;

	/**
	 * Creates a new attachment.
	 * @param formatType the content-type of the data (e.g. "image/png")
	 * @param file the file to attach
	 * @throws IOException if there's a problem reading from the file
	 */
	public Attachment(String formatType, File file) throws IOException {
		super(file);
		setFormatType(formatType);
	}

	/**
	 * Creates a new attachment.
	 * @param formatType the content-type of the data (e.g. "image/png")
	 * @param data the binary data
	 */
	public Attachment(String formatType, byte[] data) {
		super(data);
		setFormatType(formatType);
	}

	/**
	 * Creates a new attachment.
	 * @param formatType the content-type of the data (e.g. "image/png")
	 * @param uri a URL pointing to the resource (e.g.
	 * "http://example.com/image.png")
	 */
	public Attachment(String formatType, String uri) {
		super(uri);
		setFormatType(formatType);
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public Attachment(Attachment original) {
		super(original);
		contentId = original.contentId;
	}

	@Override
	public void setData(byte[] data) {
		super.setData(data);
		contentId = null;
	}

	@Override
	public void setUri(String uri) {
		super.setUri(uri);
		contentId = null;
	}

	/**
	 * Sets the content ID.
	 * @return the content ID or null if not set
	 */
	public String getContentId() {
		return contentId;
	}

	/**
	 * Sets the content ID.
	 * @param contentId the content ID
	 */
	public void setContentId(String contentId) {
		this.contentId = contentId;
		uri = null;
		data = null;
	}

	@Override
	protected void validate(List<ICalComponent> components, ICalVersion version, List<ValidationWarning> warnings) {
		if (uri == null && data == null && contentId == null) {
			warnings.add(new ValidationWarning(26));
		}
	}

	@Override
	protected Map<String, Object> toStringValues() {
		Map<String, Object> values = super.toStringValues();
		values.put("contentId", contentId);
		return values;
	}

	@Override
	public Attachment copy() {
		return new Attachment(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((contentId == null) ? 0 : contentId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		Attachment other = (Attachment) obj;
		if (contentId == null) {
			if (other.contentId != null) return false;
		} else if (!contentId.equals(other.contentId)) return false;
		return true;
	}
}
