package eu.faircode.email;

/*
    This file is part of FairEmail.
    FairEmail is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
    FairEmail is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
    You should have received a copy of the GNU General Public License
    along with FairEmail.  If not, see <http://www.gnu.org/licenses/>.
    Copyright 2018-2020 by Marcel Bokhorst (M66B)
*/

import android.text.TextUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.spi.CharsetProvider;
import java.util.Collections;
import java.util.Iterator;

import javax.mail.internet.MimeUtility;

public class UnknownCharsetProvider extends CharsetProvider {
    @Override
    public Iterator<Charset> charsets() {
        return Collections.emptyIterator();
    }

    @Override
    public Charset charsetForName(String name) {
        try {
            Charset charset = charsetForMime(name);
            return (charset == null ? StandardCharsets.ISO_8859_1 : charset);
        } catch (Throwable ex) {
            Log.e(ex);
            return StandardCharsets.ISO_8859_1;
        }
    }

    public static Charset charsetForMime(String name) {
        // x-binaryenc
        // UseInqueCodePage
        // none
        // unknown-8bit
        // X-UNKNOWN
        // https://javaee.github.io/javamail/FAQ#unsupen
        // https://github.com/javaee/javamail/blob/master/mail/src/main/resources/META-INF/javamail.charset.map
        try {
            if (name == null)
                name = "";

            name = name.replace("\"", "");

            int sp = name.indexOf(" ");
            if (sp > 0)
                name = name.substring(0, sp);

            name = name.trim().toUpperCase();

            if (name.contains("UTF8") || name.contains("UTF-8")) // //TRANSLIT
                return StandardCharsets.UTF_8;

            if (TextUtils.isEmpty(name) ||
                    name.contains("ASCII") ||
                    name.startsWith("ISO8859") ||
                    name.startsWith("ISO-8859") ||
                    "x-IA5".equalsIgnoreCase(name) ||
                    "BASE64".equalsIgnoreCase(name) ||
                    "ISO".equalsIgnoreCase(name) ||
                    "latin".equalsIgnoreCase(name) ||
                    "windows-1252".equalsIgnoreCase(name) ||
                    "X-UNKNOWN".equalsIgnoreCase(name) ||
                    "8bit".equalsIgnoreCase(name) ||
                    "unknown-8bit".equalsIgnoreCase(name))
                return StandardCharsets.ISO_8859_1;

            // Android will prevent recursion
            String jname = MimeUtility.javaCharset(name);
            return Charset.forName(jname);
        } catch (Throwable ex) {
            Log.e("Unknown charset " + name, ex);
            return null;
        }
    }
}
