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

import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.ins.InsExtension;
import org.commonmark.ext.task.list.items.TaskListItemsExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Markdown {
    static String toHtml(String markdown) {
        // https://github.com/commonmark/commonmark-java#usage
        // https://github.com/commonmark/commonmark-java/issues/294
        markdown = markdown.replace('\u00a0', ' ');

        List<Extension> extensions = Arrays.asList(
                InsExtension.create(),
                TaskListItemsExtension.create(),
                TablesExtension.create(),
                StrikethroughExtension.create());
        Parser p = Parser.builder()
                .extensions(extensions)
                .build();
        Node d = p.parse(markdown);
        HtmlRenderer r = HtmlRenderer.builder()
                .extensions(extensions)
                .build();
        String html = r.render(d);
        if (BuildConfig.DEBUG) {
            Log.i("Markdown md=" + markdown.replace('\n', '|'));
            Log.i("Markdown html=" + html.replace('\n', '|'));
        }
        return html;
    }

    static String fromHtml(String html) {
        // https://github.com/vsch/flexmark-java/wiki/Extensions#html-to-markdown
        Map<String, String> specialCharsMap = new HashMap<>();
        //specialCharsMap.put("“", "\"");
        //specialCharsMap.put("”", "\"");
        specialCharsMap.put("&ldquo;", "\"");
        specialCharsMap.put("&rdquo;", "\"");
        //specialCharsMap.put("‘", "'");
        //specialCharsMap.put("’", "'");
        specialCharsMap.put("&lsquo;", "'");
        specialCharsMap.put("&rsquo;", "'");
        specialCharsMap.put("&apos;", "'");
        //specialCharsMap.put("«", "<<");
        specialCharsMap.put("&laquo;", "<<");
        //specialCharsMap.put("»", ">>");
        specialCharsMap.put("&raquo;", ">>");
        //specialCharsMap.put("…", "...");
        specialCharsMap.put("&hellip;", "...");
        //specialCharsMap.put("–", "--");
        specialCharsMap.put("&endash;", "--");
        //specialCharsMap.put("—", "---");
        specialCharsMap.put("&emdash;", "---");

        DataHolder options = new MutableDataSet()
                .set(FlexmarkHtmlConverter.SETEXT_HEADINGS, false)
                .set(FlexmarkHtmlConverter.OUTPUT_ATTRIBUTES_ID, false)
                .set(FlexmarkHtmlConverter.TYPOGRAPHIC_REPLACEMENT_MAP, specialCharsMap)
                .toImmutable();

        // Remove nested/empty tables
        Document doc = JsoupEx.parse(html);
        for (Element table : doc.select("table")) {
            boolean empty = false;
            Elements children = table.children().select("table");
            if (children.size() == 0)
                for (Element tr : table.children()) {
                    if (tr.children().size() == 1) {
                        empty = true;
                        break;
                    }
                    if (empty)
                        break;
                }
            if (children.size() > 0 || empty) {
                table.tagName("div");
                for (Element child : table.children())
                    if ("tr".equals(child.tagName()))
                        child.tagName("div");
                    else if ("td".equals(child.tagName()))
                        child.tagName("span");
            }
        }

        String markdown = FlexmarkHtmlConverter.builder(options)
                .build()
                .convert(doc.html());

        if (BuildConfig.DEBUG) {
            Log.i("Markdown html=" + html.replace('\n', '|'));
            Log.i("Markdown md=" + markdown.replace('\n', '|'));
        }

        return markdown
                .replaceAll("(?m)^( *)(\\d+)\\.( +)", "$1$2\\\\.$3")
                .replaceAll("<br />", "")
                .replaceAll("\n\n\\s+<!-- -->\n", "");
    }
}
