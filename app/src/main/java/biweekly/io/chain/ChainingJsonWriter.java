package biweekly.io.chain;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.TimeZone;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.ICalComponent;
import biweekly.io.json.JCalWriter;
import biweekly.io.scribe.component.ICalComponentScribe;
import biweekly.io.scribe.property.ICalPropertyScribe;
import biweekly.property.ICalProperty;

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
 * Chainer class for writing jCals (JSON-encoded iCalendar objects).
 * @see Biweekly#writeJson(Collection)
 * @see Biweekly#writeJson(ICalendar...)
 * @author Michael Angstadt
 */
public class ChainingJsonWriter extends ChainingWriter<ChainingJsonWriter> {
	private boolean prettyPrint = false;

	/**
	 * @param icals the iCalendar objects to write
	 */
	public ChainingJsonWriter(Collection<ICalendar> icals) {
		super(icals);
	}

	/**
	 * Sets whether or not to pretty-print the JSON.
	 * @param prettyPrint true to pretty-print it, false not to (defaults to
	 * false)
	 * @return this
	 */
	public ChainingJsonWriter prettyPrint(boolean prettyPrint) {
		this.prettyPrint = prettyPrint;
		return this;
	}

	@Override
	public ChainingJsonWriter tz(TimeZone defaultTimeZone, boolean outlookCompatible) {
		return super.tz(defaultTimeZone, outlookCompatible);
	}

	@Override
	public ChainingJsonWriter register(ICalPropertyScribe<? extends ICalProperty> scribe) {
		return super.register(scribe);
	}

	@Override
	public ChainingJsonWriter register(ICalComponentScribe<? extends ICalComponent> scribe) {
		return super.register(scribe);
	}

	/**
	 * Writes the iCalendar objects to a string.
	 * @return the JSON string
	 */
	public String go() {
		StringWriter sw = new StringWriter();
		try {
			go(sw);
		} catch (IOException e) {
			//should never be thrown because we're writing to a string
			throw new RuntimeException(e);
		}
		return sw.toString();
	}

	/**
	 * Writes the iCalendar objects to an output stream.
	 * @param out the output stream to write to
	 * @throws IOException if there's a problem writing to the output stream
	 */
	public void go(OutputStream out) throws IOException {
		go(new JCalWriter(out, wrapInArray()));
	}

	/**
	 * Writes the iCalendar objects to a file.
	 * @param file the file to write to
	 * @throws IOException if there's a problem writing to the file
	 */
	public void go(File file) throws IOException {
		JCalWriter writer = new JCalWriter(file, wrapInArray());
		try {
			go(writer);
		} finally {
			writer.close();
		}
	}

	/**
	 * Writes the iCalendar objects to a writer.
	 * @param writer the writer to write to
	 * @throws IOException if there's a problem writing to the writer
	 */
	public void go(Writer writer) throws IOException {
		go(new JCalWriter(writer, wrapInArray()));
	}

	private void go(JCalWriter writer) throws IOException {
		if (defaultTimeZone != null) {
			writer.setGlobalTimezone(defaultTimeZone);
		}
		writer.setPrettyPrint(prettyPrint);
		if (index != null) {
			writer.setScribeIndex(index);
		}
		try {
			for (ICalendar ical : icals) {
				writer.write(ical);
				writer.flush();
			}
		} finally {
			writer.closeJsonStream();
		}
	}

	private boolean wrapInArray() {
		return icals.size() > 1;
	}
}
