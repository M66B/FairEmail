package biweekly.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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
 * Writes characters that are UTF-8 encoded.
 * @author Michael Angstadt
 */
public class Utf8Writer extends OutputStreamWriter {
	/**
	 * Creates a new UTF-8 writer.
	 * @param out the output stream to write to
	 */
	public Utf8Writer(OutputStream out) {
		super(out, Charset.forName("UTF-8"));
	}

	/**
	 * Creates a new UTF-8 writer.
	 * @param file the file to write to
	 * @throws FileNotFoundException if the file cannot be written to
	 */
	public Utf8Writer(File file) throws FileNotFoundException {
		this(file, false);
	}

	/**
	 * Creates a new UTF-8 writer.
	 * @param file the file to write to
	 * @param append true to append to the file, false to overwrite it (this
	 * parameter has no effect if the file does not exist)
	 * @throws FileNotFoundException if the file cannot be written to
	 */
	public Utf8Writer(File file, boolean append) throws FileNotFoundException {
		this(new FileOutputStream(file, append));
	}
}
