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

    Copyright 2018-2024 by Marcel Bokhorst (M66B)
*/

import org.commonmark.Extension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jsoup.nodes.Document;

import java.util.Arrays;
import java.util.List;

public class Markdown {
    static String toHtml(String markdown) {
        markdown = markdown.replace('\u00a0', ' ');
        List<Extension> extensions = Arrays.asList(
                TablesExtension.create(),
                StrikethroughExtension.create());
        Parser p = Parser.builder()
                .extensions(extensions)
                .build();
        Node d = p.parse(markdown);
        HtmlRenderer r = HtmlRenderer.builder()
                .extensions(extensions)
                .build();
        return r.render(d);
    }

    static String fromHtml(String html) {
        // TODO: HTML to Markdown
        throw new IllegalArgumentException("Not implemented");
    }
}
