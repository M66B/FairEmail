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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class JsoupEx {
    static Document parse(String html) {

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
        try {
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
}
