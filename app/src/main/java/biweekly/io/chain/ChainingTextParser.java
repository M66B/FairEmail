package biweekly.io.chain;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import biweekly.Biweekly;
import biweekly.io.StreamReader;
import biweekly.io.text.ICalReader;

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
 * Chainer class for parsing traditional, plain-text iCalendar objects.
 * @see Biweekly#parse(InputStream)
 * @see Biweekly#parse(File)
 * @see Biweekly#parse(Reader)
 * @author Michael Angstadt
 */
public class ChainingTextParser<T extends ChainingTextParser<?>> extends ChainingParser<T> {
	private boolean caretDecoding = true;

	public ChainingTextParser(String string) {
		super(string);
	}

	public ChainingTextParser(InputStream in) {
		super(in);
	}

	public ChainingTextParser(Reader reader) {
		super(reader);
	}

	public ChainingTextParser(File file) {
		super(file);
	}

	/**
	 * Sets whether the reader will decode characters in parameter values that
	 * use circumflex accent encoding (enabled by default). This only applies to
	 * version 2.0 iCalendar objects.
	 * 
	 * @param enable true to use circumflex accent decoding, false not to
	 * @return this
	 * @see ICalReader#setCaretDecodingEnabled(boolean)
	 * @see <a href="http://tools.ietf.org/html/rfc6868">RFC 6868</a>
	 */
	public T caretDecoding(boolean enable) {
		caretDecoding = enable;
		return this_;
	}

	@Override
	StreamReader constructReader() throws IOException {
		ICalReader reader = newReader();
		reader.setCaretDecodingEnabled(caretDecoding);
		return reader;
	}

	private ICalReader newReader() throws IOException {
		if (string != null) {
			return new ICalReader(string);
		}
		if (in != null) {
			return new ICalReader(in);
		}
		if (reader != null) {
			return new ICalReader(reader);
		}
		return new ICalReader(file);
	}
}
