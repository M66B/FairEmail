package biweekly.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

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
 * Gets the entire contents of an input stream or a file.
 * @author Michael Angstadt
 */
public class Gobble {
	private final File file;
	private final InputStream in;
	private final Reader reader;

	/**
	 * Gets the contents of a file.
	 * @param file the file
	 */
	public Gobble(File file) {
		this(file, null, null);
	}

	/**
	 * Gets the contents of an input stream.
	 * @param in the input stream
	 */
	public Gobble(InputStream in) {
		this(null, in, null);
	}

	/**
	 * Gets the contents of a reader.
	 * @param reader the reader
	 */
	public Gobble(Reader reader) {
		this(null, null, reader);
	}

	private Gobble(File file, InputStream in, Reader reader) {
		this.file = file;
		this.in = in;
		this.reader = reader;
	}

	/**
	 * Gets the stream contents as a string. If something other than a
	 * {@link Reader} was passed into this class's constructor, this method
	 * decodes the stream data using the system's default character encoding.
	 * @return the string
	 * @throws IOException if there was a problem reading from the stream
	 */
	public String asString() throws IOException {
		return asString(Charset.defaultCharset().name());
	}

	/**
	 * Gets the stream contents as a string.
	 * @param charset the character set to decode the stream data with (this
	 * parameter is ignored if a {@link Reader} was passed into this class's
	 * constructor)
	 * @return the string
	 * @throws IOException if there was a problem reading from the stream
	 */
	public String asString(String charset) throws IOException {
		Reader reader = buildReader(charset);
		return consumeReader(reader);
	}

	/**
	 * Gets the stream contents as a byte array.
	 * @return the byte array
	 * @throws IOException if there was a problem reading from the stream
	 * @throws IllegalStateException if a {@link Reader} object was passed into
	 * this class's constructor
	 */
	public byte[] asByteArray() throws IOException {
		if (reader != null) {
			throw new IllegalStateException("Cannot get raw bytes from a Reader object.");
		}

		InputStream in = buildInputStream();
		return consumeInputStream(in);
	}

	private Reader buildReader(String charset) throws IOException {
		return (reader == null) ? new InputStreamReader(buildInputStream(), charset) : reader;
	}

	private InputStream buildInputStream() throws IOException {
		return (in == null) ? new BufferedInputStream(new FileInputStream(file)) : in;
	}

	private String consumeReader(Reader reader) throws IOException {
		StringBuilder sb = new StringBuilder();
		char[] buffer = new char[4096];
		int read;
		try {
			while ((read = reader.read(buffer)) != -1) {
				sb.append(buffer, 0, read);
			}
		} finally {
			reader.close();
		}
		return sb.toString();
	}

	private byte[] consumeInputStream(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[4096];
		int read;
		try {
			while ((read = in.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}
		} finally {
			in.close();
		}
		return out.toByteArray();
	}
}
