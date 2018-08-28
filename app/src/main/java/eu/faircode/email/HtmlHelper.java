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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.TextUtils;

import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlHelper implements NodeVisitor {
    private Context context;
    private String newline;
    private List<String> refs = new ArrayList<>();
    private StringBuilder sb = new StringBuilder();
    private Pattern pattern = Pattern.compile("([http|https]+://[\\w\\S(\\.|:|/)]+)");

    private HtmlHelper(Context context, boolean reply) {
        this.context = context;
        this.newline = (reply ? "<br>> " : "<br>");
    }

    public void head(Node node, int depth) {
        String name = node.nodeName();
        if (node instanceof TextNode) {
            String text = ((TextNode) node).text();
            text = Html.escapeHtml(text);
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                String ref = matcher.group();
                if (!refs.contains(ref))
                    refs.add(ref);
                String alt = context.getString(R.string.title_link);
                text = text.replace(ref, String.format("<a href=\"%s\">%s [%d]</a>", ref, alt, refs.size()));
            }
            sb.append(text);
        } else if (name.equals("li"))
            sb.append(newline).append(" * ");
        else if (name.equals("dt"))
            sb.append("  ");
        else if (StringUtil.in(name, "p", "h1", "h2", "h3", "h4", "h5", "tr", "div"))
            sb.append(newline);
    }

    public void tail(Node node, int depth) {
        String name = node.nodeName();
        if (StringUtil.in(name, "br", "dd", "dt", "p", "h1", "h2", "h3", "h4", "h5", "div"))
            sb.append(newline);
        else if (name.equals("a")) {
            String ref = node.absUrl("href");
            if (!TextUtils.isEmpty(ref)) {
                if (!refs.contains(ref))
                    refs.add(ref);
                String alt = node.attr("alt");
                if (TextUtils.isEmpty(alt))
                    alt = context.getString(R.string.title_link);
                alt = Html.escapeHtml(alt);
                sb.append(" ").append(String.format("<a href=\"%s\">%s [%d]</a>", ref, alt, refs.size()));
            }
        } else if (name.equals("img")) {
            String ref = node.absUrl("src");
            if (!TextUtils.isEmpty(ref)) {
                if (!refs.contains(ref))
                    refs.add(ref);
                String alt = node.attr("alt");
                if (TextUtils.isEmpty(alt))
                    alt = context.getString(R.string.title_image);
                alt = Html.escapeHtml(alt);
                sb.append(" ").append(String.format("<a href=\"%s\">%s [%d]</a>", ref, alt, refs.size()));
                sb.append("<img src=\"" + ref + "\" alt=\"" + alt + "\">");
            }
        }
    }

    @Override
    public String toString() {
        if (refs.size() > 0)
            sb.append(newline).append(newline);
        for (int i = 0; i < refs.size(); i++)
            sb.append(String.format("[%d] %s ", i + 1, refs.get(i))).append(newline);
        return sb.toString();
    }

    public static String sanitize(Context context, String html, boolean reply) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        if (prefs.getBoolean("sanitize", false)) {
            Document document = Jsoup.parse(html);
            HtmlHelper visitor = new HtmlHelper(context, reply);
            NodeTraversor.traverse(visitor, document.body());
            return visitor.toString();
        } else {
            Document document = Jsoup.parse(Jsoup.clean(html, Whitelist.relaxed()));
            for (Element tr : document.select("tr"))
                tr.after("<br>");
            return document.body().html();
        }
    }
}
