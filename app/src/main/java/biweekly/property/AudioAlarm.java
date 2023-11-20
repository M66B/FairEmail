package biweekly.property;

import java.util.Arrays;
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
 * Defines an alarm that will play an audio file when triggered. It is
 * recommended that the {@link VAlarm} component be used to create alarms.
 * @author Michael Angstadt
 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.27-8</a>
 * @see VAlarm#audio
 */
public class AudioAlarm extends VCalAlarmProperty {
	private String contentId, uri;
	private byte[] data;

	public AudioAlarm() {
		//empty
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public AudioAlarm(AudioAlarm original) {
		super(original);
		data = (original.data == null) ? null : original.data.clone();
		uri = original.uri;
		contentId = original.contentId;
	}

	public String getContentId() {
		return contentId;
	}

	public void setContentId(String contentId) {
		this.contentId = contentId;
		this.uri = null;
		this.data = null;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
		this.contentId = null;
		this.data = null;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
		this.uri = null;
		this.contentId = null;
	}

	public String getType() {
		return parameters.getType();
	}

	public void setType(String type) {
		parameters.setType(type);
	}

	@Override
	protected Map<String, Object> toStringValues() {
		Map<String, Object> values = super.toStringValues();
		values.put("data", (data == null) ? "null" : "length: " + data.length);
		values.put("uri", uri);
		values.put("contentId", contentId);
		return values;
	}

	@Override
	public AudioAlarm copy() {
		return new AudioAlarm(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((contentId == null) ? 0 : contentId.hashCode());
		result = prime * result + Arrays.hashCode(data);
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		AudioAlarm other = (AudioAlarm) obj;
		if (contentId == null) {
			if (other.contentId != null) return false;
		} else if (!contentId.equals(other.contentId)) return false;
		if (uri == null) {
			if (other.uri != null) return false;
		} else if (!uri.equals(other.uri)) return false;
		if (!Arrays.equals(data, other.data)) return false;
		return true;
	}
}
