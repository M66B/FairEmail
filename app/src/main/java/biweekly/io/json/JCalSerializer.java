package biweekly.io.json;

import java.io.IOException;

import biweekly.ICalendar;
import biweekly.io.TimezoneAssignment;
import biweekly.io.scribe.ScribeIndex;
import biweekly.io.scribe.property.ICalPropertyScribe;
import biweekly.property.ICalProperty;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

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
 * Serializes jCals within the jackson-databind framework.
 * @author Buddy Gorven
 * @author Michael Angstadt
 */
@JsonFormat
public class JCalSerializer extends StdSerializer<ICalendar> {
	private static final long serialVersionUID = 8964681078186049817L;
	private ScribeIndex index = new ScribeIndex();
	private TimezoneAssignment globalTimezone;

	public JCalSerializer() {
		super(ICalendar.class);
	}

	@Override
	public void serialize(ICalendar value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
		@SuppressWarnings("resource")
		JCalWriter writer = new JCalWriter(gen);
		writer.setScribeIndex(getScribeIndex());
		writer.setGlobalTimezone(globalTimezone);
		writer.write(value);
	}

	/**
	 * <p>
	 * Registers a property scribe. This is the same as calling:
	 * </p>
	 * <p>
	 * {@code getScribeIndex().register(scribe)}
	 * </p>
	 * @param scribe the scribe to register
	 */
	public void registerScribe(ICalPropertyScribe<? extends ICalProperty> scribe) {
		index.register(scribe);
	}

	/**
	 * Gets the scribe index.
	 * @return the scribe index
	 */
	public ScribeIndex getScribeIndex() {
		return index;
	}

	/**
	 * Sets the scribe index.
	 * @param index the scribe index
	 */
	public void setScribeIndex(ScribeIndex index) {
		this.index = index;
	}

	/**
	 * Gets the timezone that all date/time property values will be formatted
	 * in. If set, this setting will override the timezone information
	 * associated with each {@link ICalendar} object.
	 * @return the global timezone or null if not set (defaults to null)
	 */
	public TimezoneAssignment getGlobalTimezone() {
		return globalTimezone;
	}

	/**
	 * Sets the timezone that all date/time property values will be formatted
	 * in. This is a convenience method that overrides the timezone information
	 * associated with each {@link ICalendar} object that is passed into this
	 * writer.
	 * @param globalTimezone the global timezone or null not to set a global
	 * timezone (defaults to null)
	 */
	public void setGlobalTimezone(TimezoneAssignment globalTimezone) {
		this.globalTimezone = globalTimezone;
	}
}
