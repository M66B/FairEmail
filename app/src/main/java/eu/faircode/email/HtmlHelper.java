package eu.faircode.email;

/*
    This file is part of FairEmail.

    FairEmail is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    NetGuard is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with NetGuard.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2018 by Marcel Bokhorst (M66B)
*/

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlHelper {
    private static Pattern pattern = Pattern.compile("([http|https]+://[\\w\\S(\\.|:|/)]+)");

    public static String sanitize(String html) {
        Document document = Jsoup.parse(Jsoup.clean(html, Whitelist.relaxed().addProtocols("img", "src", "cid")));
        for (Element tr : document.select("tr"))
            tr.after("<br>");
        NodeTraversor.traverse(new NodeVisitor() {
            @Override
            public void head(Node node, int depth) {
                if (node instanceof TextNode) {
                    String text = ((TextNode) node).text();
                    Matcher matcher = pattern.matcher(text);
                    while (matcher.find()) {
                        String ref = matcher.group();
                        text = text.replace(ref, String.format("<a href=\"%s\">%s</a>", ref, ref));
                    }
                    node.before(text);
                    ((TextNode) node).text("");
                }
            }

            @Override
            public void tail(Node node, int depth) {
            }
        }, document.body());
        return document.body().html();
    }
}
