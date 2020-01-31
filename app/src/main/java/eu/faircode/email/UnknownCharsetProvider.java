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

            name = name.trim();

            if (TextUtils.isEmpty(name))
                return StandardCharsets.ISO_8859_1;

            if ("x-IA5".equalsIgnoreCase(name))
                return StandardCharsets.ISO_8859_1;
            if ("ASCII".equalsIgnoreCase(name))
                return StandardCharsets.ISO_8859_1;
            if ("ISO8859-16".equalsIgnoreCase(name))
                return StandardCharsets.ISO_8859_1;
            if ("UTF-8//TRANSLIT".equalsIgnoreCase(name))
                return StandardCharsets.UTF_8;

            // Android will prevent recursion
            String jname = MimeUtility.javaCharset(name);
            return Charset.forName(jname);
        } catch (Throwable ex) {
            Log.e("Unknown charset " + name, ex);
            return null;
        }
    }
}
