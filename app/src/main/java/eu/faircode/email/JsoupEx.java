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

    Copyright 2018-2025 by Marcel Bokhorst (M66B)
*/

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class JsoupEx {
    static Document parse(String html) {
        try {
/*
        org.jsoup.UncheckedIOException: java.io.IOException: Input is binary and unsupported
                at org.jsoup.parser.CharacterReader.<init>(SourceFile:38)
                at org.jsoup.parser.CharacterReader.<init>(SourceFile:43)
                at org.jsoup.parser.TreeBuilder.initialiseParse(SourceFile:38)
                at org.jsoup.parser.HtmlTreeBuilder.initialiseParse(SourceFile:65)
                at org.jsoup.parser.TreeBuilder.parse(SourceFile:46)
                at org.jsoup.parser.Parser.parse(SourceFile:107)
                at org.jsoup.Jsoup.parse(SourceFile:58)
*/
            return Jsoup.parse(html.replace("\0", ""));
        } catch (OutOfMemoryError ex) {
            Log.e(ex);
            Document document = Document.createShell("");
            Element strong = document.createElement("strong");
            strong.text(Log.formatThrowable(ex));
            document.body().appendChild(strong);
            return document;
        }
    }

    static Document parse(InputStream stream, String charset, String baseUri) throws IOException {
        try {
            return Jsoup.parse(stream, charset, baseUri);
        } catch (OutOfMemoryError ex) {
            Log.e(ex);
            return Document.createShell("");
        }
    }

    static Document parse(File in) throws IOException {
        try (InputStream is = new FileInputStream(in)) {
            return Jsoup.parse(new FilteredStream(is), StandardCharsets.UTF_8.name(), "");
        }
    }

    private static class FilteredStream extends FilterInputStream {
        protected FilteredStream(InputStream in) {
            super(in);
        }

        @Override
        public int read() throws IOException {
            int b = super.read();
            while (b == 0)
                b = super.read();
            return b;
        }

        @Override
        public int read(byte[] buffer) throws IOException {
            return read(buffer, 0, buffer.length);
        }

        @Override
        public int read(byte[] buffer, int off, int len) throws IOException {
            int b;
            int c = 0;
            while (c < len) {
                b = read();
                if (b < 0)
                    return (c == 0 ? -1 : c);
                buffer[off + c++] = (byte) b;
            }
            return c;
        }
    }
}
