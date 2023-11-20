package biweekly.io.chain;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import biweekly.Biweekly;
import biweekly.io.StreamReader;
import biweekly.io.json.JCalReader;

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
 * Chainer class for parsing jCals (JSON-encoded iCalendar objects).
 * @see Biweekly#parseJson(InputStream)
 * @see Biweekly#parseJson(File)
 * @see Biweekly#parseJson(Reader)
 * @author Michael Angstadt
 */
public class ChainingJsonParser<T extends ChainingJsonParser<?>> extends ChainingParser<T> {
	public ChainingJsonParser(String string) {
		super(string);
	}

	public ChainingJsonParser(InputStream in) {
		super(in);
	}

	public ChainingJsonParser(Reader reader) {
		super(reader);
	}

	public ChainingJsonParser(File file) {
		super(file);
	}

	@Override
	StreamReader constructReader() throws IOException {
		if (string != null) {
			return new JCalReader(string);
		}
		if (in != null) {
			return new JCalReader(in);
		}
		if (reader != null) {
			return new JCalReader(reader);
		}
		return new JCalReader(file);
	}
}
