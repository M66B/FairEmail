package biweekly.io.chain;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.TimeZone;

import biweekly.Biweekly;
import biweekly.ICalVersion;
import biweekly.ICalendar;
import biweekly.component.ICalComponent;
import biweekly.io.scribe.component.ICalComponentScribe;
import biweekly.io.scribe.property.ICalPropertyScribe;
import biweekly.io.text.ICalWriter;
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
 * Chainer class for writing traditional, plain-text iCalendar objects.
 * @see Biweekly#write(Collection)
 * @see Biweekly#write(ICalendar...)
 * @author Michael Angstadt
 */
public class ChainingTextWriter extends ChainingWriter<ChainingTextWriter> {
	private ICalVersion version;
	private boolean caretEncoding = false;
	private boolean foldLines = true;

	/**
	 * @param icals the iCalendar objects to write
	 */
	public ChainingTextWriter(Collection<ICalendar> icals) {
		super(icals);
	}

	/**
	 * <p>
	 * Sets the version that all the iCalendar objects will be marshalled to.
	 * The version that is attached to each individual {@link ICalendar} object
	 * will be ignored.
	 * </p>
	 * <p>
	 * If no version is passed into this method, the writer will look at the
	 * version attached to each individual {@link ICalendar} object and marshal
	 * it to that version. And if a {@link ICalendar} object has no version
	 * attached to it, then it will be marshalled to version 2.0.
	 * </p>
	 * @param version the version to marshal the iCalendar objects to
	 * @return this
	 */
	public ChainingTextWriter version(ICalVersion version) {
		this.version = version;
		return this;
	}

	/**
	 * Sets whether the writer will use circumflex accent encoding for parameter
	 * values (disabled by default). This only applies to version 2.0 iCalendar
	 * objects.
	 * @param enable true to use circumflex accent encoding, false not to
	 * @return this
	 * @see ICalWriter#setCaretEncodingEnabled(boolean)
	 * @see <a href="http://tools.ietf.org/html/rfc6868">RFC 6868</a>
	 */
	public ChainingTextWriter caretEncoding(boolean enable) {
		this.caretEncoding = enable;
		return this;
	}

	/**
	 * <p>
	 * Sets whether to fold long lines. Line folding is when long lines are
	 * split up into multiple lines. No data is lost or changed when a line is
	 * folded.
	 * </p>
	 * <p>
	 * Line folding is enabled by default. If the iCalendar consumer is not
	 * parsing your iCalendar objects properly, disabling line folding may help.
	 * </p>
	 * @param foldLines true to enable line folding, false to disable it
	 * (defaults to true)
	 * @return this
	 */
	public ChainingTextWriter foldLines(boolean foldLines) {
		this.foldLines = foldLines;
		return this;
	}

	@Override
	public ChainingTextWriter tz(TimeZone defaultTimeZone, boolean outlookCompatible) {
		return super.tz(defaultTimeZone, outlookCompatible);
	}

	@Override
	public ChainingTextWriter register(ICalPropertyScribe<? extends ICalProperty> scribe) {
		return super.register(scribe);
	}

	@Override
	public ChainingTextWriter register(ICalComponentScribe<? extends ICalComponent> scribe) {
		return super.register(scribe);
	}

	/**
	 * Writes the iCalendar objects to a string.
	 * @return the iCalendar string
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
		go(new ICalWriter(out, getICalWriterConstructorVersion()));
	}

	/**
	 * Writes the iCalendar objects to a file. If the file exists, it will be
	 * overwritten.
	 * @param file the file to write to
	 * @throws IOException if there's a problem writing to the file
	 */
	public void go(File file) throws IOException {
		go(file, false);
	}

	/**
	 * Writes the iCalendar objects to a file.
	 * @param file the file to write to
	 * @param append true to append onto the end of the file, false to overwrite
	 * it
	 * @throws IOException if there's a problem writing to the file
	 */
	public void go(File file, boolean append) throws IOException {
		ICalWriter writer = new ICalWriter(file, append, getICalWriterConstructorVersion());
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
		go(new ICalWriter(writer, getICalWriterConstructorVersion()));
	}

	private void go(ICalWriter writer) throws IOException {
		writer.setCaretEncodingEnabled(caretEncoding);
		if (!foldLines) {
			writer.getVObjectWriter().getFoldedLineWriter().setLineLength(null);
		}
		if (defaultTimeZone != null) {
			writer.setGlobalTimezone(defaultTimeZone);
		}
		if (index != null) {
			writer.setScribeIndex(index);
		}

		for (ICalendar ical : icals) {
			if (version == null) {
				//use the version that's assigned to each individual iCalendar object
				ICalVersion icalVersion = ical.getVersion();
				if (icalVersion == null) {
					icalVersion = ICalVersion.V2_0;
				}
				writer.setTargetVersion(icalVersion);
			}
			writer.write(ical);
			writer.flush();
		}
	}

	/**
	 * <p>
	 * Gets the {@link ICalVersion} object to pass into the {@link ICalWriter}
	 * constructor. The constructor does not allow a null version, so this
	 * method ensures that a non-null version is passed in.
	 * </p>
	 * <p>
	 * If the user hasn't chosen a version, the version that is passed into the
	 * constructor doesn't matter. This is because the writer's target version
	 * is reset every time an iCalendar object is written (see the
	 * {@link #go(ICalWriter)} method).
	 * </p>
	 * @return the version to pass into the constructor
	 */
	private ICalVersion getICalWriterConstructorVersion() {
		return (version == null) ? ICalVersion.V2_0 : version;
	}
}
