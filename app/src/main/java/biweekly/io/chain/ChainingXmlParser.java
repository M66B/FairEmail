package biweekly.io.chain;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.w3c.dom.Document;

import biweekly.Biweekly;
import biweekly.io.StreamReader;
import biweekly.io.xml.XCalReader;

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
 * Chainer class for parsing xCals (XML-encoded iCalendar objects).
 * @see Biweekly#parseXml(InputStream)
 * @see Biweekly#parseXml(File)
 * @see Biweekly#parseXml(Reader)
 * @author Michael Angstadt
 */
public class ChainingXmlParser<T extends ChainingXmlParser<?>> extends ChainingParser<T> {
	private Document dom;

	public ChainingXmlParser(String string) {
		super(string);
	}

	public ChainingXmlParser(InputStream in) {
		super(in);
	}

	public ChainingXmlParser(File file) {
		super(file);
	}

	public ChainingXmlParser(Reader reader) {
		super(reader);
	}

	public ChainingXmlParser(Document dom) {
		this.dom = dom;
	}

	@Override
	StreamReader constructReader() throws IOException {
		if (string != null) {
			return new XCalReader(string);
		}
		if (in != null) {
			return new XCalReader(in);
		}
		if (reader != null) {
			return new XCalReader(reader);
		}
		if (file != null) {
			return new XCalReader(file);
		}
		return new XCalReader(dom);
	}
}
