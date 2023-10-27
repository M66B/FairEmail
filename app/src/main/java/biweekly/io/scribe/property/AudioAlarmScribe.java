package biweekly.io.scribe.property;

import java.util.Collections;
import java.util.List;

import biweekly.ICalDataType;
import biweekly.ICalVersion;
import biweekly.component.VAlarm;
import biweekly.property.Action;
import biweekly.property.Attachment;
import biweekly.property.AudioAlarm;
import biweekly.util.org.apache.commons.codec.binary.Base64;

import com.github.mangstadt.vinnie.io.VObjectPropertyValues.SemiStructuredValueIterator;

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
 * Marshals {@link AudioAlarm} properties.
 * @author Michael Angstadt
 */
public class AudioAlarmScribe extends VCalAlarmPropertyScribe<AudioAlarm> {
	public AudioAlarmScribe() {
		super(AudioAlarm.class, "AALARM");
	}

	@Override
	protected ICalDataType _dataType(AudioAlarm property, ICalVersion version) {
		if (property.getUri() != null) {
			return ICalDataType.URL;
		}
		if (property.getData() != null) {
			return ICalDataType.BINARY;
		}
		if (property.getContentId() != null) {
			return ICalDataType.CONTENT_ID;
		}
		return null;
	}

	@Override
	protected List<String> writeData(AudioAlarm property) {
		String uri = property.getUri();
		if (uri != null) {
			return Collections.singletonList(uri);
		}

		byte[] data = property.getData();
		if (data != null) {
			String base64Str = Base64.encodeBase64String(data);
			return Collections.singletonList(base64Str);
		}

		String contentId = property.getContentId();
		if (contentId != null) {
			return Collections.singletonList(contentId);
		}

		return Collections.emptyList();
	}

	@Override
	protected AudioAlarm create(ICalDataType dataType, SemiStructuredValueIterator it) {
		AudioAlarm aalarm = new AudioAlarm();
		String next = it.next();
		if (next == null) {
			return aalarm;
		}

		if (dataType == ICalDataType.BINARY) {
			byte[] data = Base64.decodeBase64(next);
			aalarm.setData(data);
		} else if (dataType == ICalDataType.URL) {
			aalarm.setUri(next);
		} else if (dataType == ICalDataType.CONTENT_ID) {
			aalarm.setContentId(next);
		} else {
			aalarm.setUri(next);
		}

		return aalarm;
	}

	@Override
	protected void toVAlarm(VAlarm valarm, AudioAlarm property) {
		Attachment attach = buildAttachment(property);
		if (attach != null) {
			valarm.addAttachment(attach);
		}
	}

	private static Attachment buildAttachment(AudioAlarm aalarm) {
		String type = aalarm.getType();
		String contentType = (type == null) ? null : "audio/" + type.toLowerCase();
		Attachment attach = new Attachment(contentType, (String) null);

		byte[] data = aalarm.getData();
		if (data != null) {
			attach.setData(data);
			return attach;
		}

		String contentId = aalarm.getContentId();
		if (contentId != null) {
			attach.setContentId(contentId);
			return attach;
		}

		String uri = aalarm.getUri();
		if (uri != null) {
			attach.setUri(uri);
			return attach;
		}

		return null;
	}

	@Override
	protected Action action() {
		return Action.audio();
	}
}
