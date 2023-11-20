package biweekly.io.scribe.property;

import biweekly.ICalDataType;
import biweekly.ICalVersion;
import biweekly.io.WriteContext;
import biweekly.io.json.JCalValue;
import biweekly.io.xml.XCalElement;
import biweekly.property.Attachment;

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
 * Marshals {@link Attachment} properties.
 * @author Michael Angstadt
 */
public class AttachmentScribe extends BinaryPropertyScribe<Attachment> {
	public AttachmentScribe() {
		super(Attachment.class, "ATTACH");
	}

	@Override
	protected ICalDataType _dataType(Attachment property, ICalVersion version) {
		if (property.getContentId() != null) {
			return (version == ICalVersion.V1_0) ? ICalDataType.CONTENT_ID : ICalDataType.URI;
		}
		return super._dataType(property, version);
	}

	@Override
	protected Attachment newInstance(byte[] data) {
		/*
		 * Note: "formatType" will be set when the parameters are assigned to
		 * the property object.
		 */
		return new Attachment(null, data);
	}

	@Override
	protected Attachment newInstance(String value, ICalDataType dataType) {
		/*
		 * Note: "formatType" will be set when the parameters are assigned to
		 * the property object.
		 */

		if (dataType == ICalDataType.CONTENT_ID) {
			String contentId = getCidUriValue(value);
			if (contentId == null) {
				contentId = value;
			}
			Attachment attach = new Attachment(null, (String) null);
			attach.setContentId(contentId);
			return attach;
		}

		String contentId = getCidUriValue(value);
		if (contentId != null) {
			Attachment attach = new Attachment(null, (String) null);
			attach.setContentId(contentId);
			return attach;
		}

		return new Attachment(null, value);
	}

	@Override
	protected String _writeText(Attachment property, WriteContext context) {
		String contentId = property.getContentId();
		if (contentId != null) {
			return (context.getVersion() == ICalVersion.V1_0) ? '<' + contentId + '>' : "cid:" + contentId;
		}

		return super._writeText(property, context);
	}

	@Override
	protected void _writeXml(Attachment property, XCalElement element, WriteContext context) {
		String contentId = property.getContentId();
		if (contentId != null) {
			element.append(ICalDataType.URI, "cid:" + contentId);
			return;
		}

		super._writeXml(property, element, context);
	}

	@Override
	protected JCalValue _writeJson(Attachment property, WriteContext context) {
		String contentId = property.getContentId();
		if (contentId != null) {
			return JCalValue.single("cid:" + contentId);
		}

		return super._writeJson(property, context);
	}

	/**
	 * Gets the value of the given "cid" URI.
	 * @param uri the "cid" URI
	 * @return the URI value or null if the given string is not a "cid" URI
	 */
	private static String getCidUriValue(String uri) {
		int colon = uri.indexOf(':');
		if (colon == 3) {
			String scheme = uri.substring(0, colon);
			return "cid".equalsIgnoreCase(scheme) ? uri.substring(colon + 1) : null;
		}

		if (uri.length() > 0 && uri.charAt(0) == '<' && uri.charAt(uri.length() - 1) == '>') {
			return uri.substring(1, uri.length() - 1);
		}

		return null;
	}
}
