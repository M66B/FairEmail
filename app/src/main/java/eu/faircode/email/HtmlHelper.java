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

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.text.HtmlCompat;
import androidx.core.util.PatternsCompat;
import androidx.preference.PreferenceManager;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.NodeFilter;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static androidx.core.text.HtmlCompat.FROM_HTML_SEPARATOR_LINE_BREAK_LIST_ITEM;
import static androidx.core.text.HtmlCompat.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE;

public class HtmlHelper {
    private static final int PREVIEW_SIZE = 500; // characters

    private static final float MIN_LUMINANCE = 0.5f;
    private static final int TAB_SIZE = 2;
    private static final int MAX_AUTO_LINK = 250;
    private static final int TRACKING_PIXEL_SURFACE = 25; // pixels

    private static final List<String> heads = Collections.unmodifiableList(Arrays.asList(
            "h1", "h2", "h3", "h4", "h5", "h6", "p", "ol", "ul", "table", "br", "hr"));
    private static final List<String> tails = Collections.unmodifiableList(Arrays.asList(
            "h1", "h2", "h3", "h4", "h5", "h6", "p", "ol", "ul", "li"));

    private static final HashMap<String, Integer> x11ColorMap = new HashMap<>();

    static {
        // https://www.w3.org/TR/css-color-3/
        x11ColorMap.put("aliceblue", 0xF0F8FF);
        x11ColorMap.put("antiquewhite", 0xFAEBD7);
        x11ColorMap.put("aqua", 0x00FFFF);
        x11ColorMap.put("aquamarine", 0x7FFFD4);
        x11ColorMap.put("azure", 0xF0FFFF);
        x11ColorMap.put("beige", 0xF5F5DC);
        x11ColorMap.put("bisque", 0xFFE4C4);
        x11ColorMap.put("black", 0x000000);
        x11ColorMap.put("blanchedalmond", 0xFFEBCD);
        x11ColorMap.put("blue", 0x0000FF);
        x11ColorMap.put("blueviolet", 0x8A2BE2);
        x11ColorMap.put("brown", 0xA52A2A);
        x11ColorMap.put("burlywood", 0xDEB887);
        x11ColorMap.put("cadetblue", 0x5F9EA0);
        x11ColorMap.put("chartreuse", 0x7FFF00);
        x11ColorMap.put("chocolate", 0xD2691E);
        x11ColorMap.put("coral", 0xFF7F50);
        x11ColorMap.put("cornflowerblue", 0x6495ED);
        x11ColorMap.put("cornsilk", 0xFFF8DC);
        x11ColorMap.put("crimson", 0xDC143C);
        x11ColorMap.put("cyan", 0x00FFFF);
        x11ColorMap.put("darkblue", 0x00008B);
        x11ColorMap.put("darkcyan", 0x008B8B);
        x11ColorMap.put("darkgoldenrod", 0xB8860B);
        x11ColorMap.put("darkgray", 0xA9A9A9);
        x11ColorMap.put("darkgreen", 0x006400);
        x11ColorMap.put("darkgrey", 0xA9A9A9);
        x11ColorMap.put("darkkhaki", 0xBDB76B);
        x11ColorMap.put("darkmagenta", 0x8B008B);
        x11ColorMap.put("darkolivegreen", 0x556B2F);
        x11ColorMap.put("darkorange", 0xFF8C00);
        x11ColorMap.put("darkorchid", 0x9932CC);
        x11ColorMap.put("darkred", 0x8B0000);
        x11ColorMap.put("darksalmon", 0xE9967A);
        x11ColorMap.put("darkseagreen", 0x8FBC8F);
        x11ColorMap.put("darkslateblue", 0x483D8B);
        x11ColorMap.put("darkslategray", 0x2F4F4F);
        x11ColorMap.put("darkslategrey", 0x2F4F4F);
        x11ColorMap.put("darkturquoise", 0x00CED1);
        x11ColorMap.put("darkviolet", 0x9400D3);
        x11ColorMap.put("deeppink", 0xFF1493);
        x11ColorMap.put("deepskyblue", 0x00BFFF);
        x11ColorMap.put("dimgray", 0x696969);
        x11ColorMap.put("dimgrey", 0x696969);
        x11ColorMap.put("dodgerblue", 0x1E90FF);
        x11ColorMap.put("firebrick", 0xB22222);
        x11ColorMap.put("floralwhite", 0xFFFAF0);
        x11ColorMap.put("forestgreen", 0x228B22);
        x11ColorMap.put("fuchsia", 0xFF00FF);
        x11ColorMap.put("gainsboro", 0xDCDCDC);
        x11ColorMap.put("ghostwhite", 0xF8F8FF);
        x11ColorMap.put("gold", 0xFFD700);
        x11ColorMap.put("goldenrod", 0xDAA520);
        x11ColorMap.put("gray", 0x808080);
        x11ColorMap.put("green", 0x008000);
        x11ColorMap.put("greenyellow", 0xADFF2F);
        x11ColorMap.put("grey", 0x808080);
        x11ColorMap.put("honeydew", 0xF0FFF0);
        x11ColorMap.put("hotpink", 0xFF69B4);
        x11ColorMap.put("indianred", 0xCD5C5C);
        x11ColorMap.put("indigo", 0x4B0082);
        x11ColorMap.put("ivory", 0xFFFFF0);
        x11ColorMap.put("khaki", 0xF0E68C);
        x11ColorMap.put("lavender", 0xE6E6FA);
        x11ColorMap.put("lavenderblush", 0xFFF0F5);
        x11ColorMap.put("lawngreen", 0x7CFC00);
        x11ColorMap.put("lemonchiffon", 0xFFFACD);
        x11ColorMap.put("lightblue", 0xADD8E6);
        x11ColorMap.put("lightcoral", 0xF08080);
        x11ColorMap.put("lightcyan", 0xE0FFFF);
        x11ColorMap.put("lightgoldenrodyellow", 0xFAFAD2);
        x11ColorMap.put("lightgray", 0xD3D3D3);
        x11ColorMap.put("lightgreen", 0x90EE90);
        x11ColorMap.put("lightgrey", 0xD3D3D3);
        x11ColorMap.put("lightpink", 0xFFB6C1);
        x11ColorMap.put("lightsalmon", 0xFFA07A);
        x11ColorMap.put("lightseagreen", 0x20B2AA);
        x11ColorMap.put("lightskyblue", 0x87CEFA);
        x11ColorMap.put("lightslategray", 0x778899);
        x11ColorMap.put("lightslategrey", 0x778899);
        x11ColorMap.put("lightsteelblue", 0xB0C4DE);
        x11ColorMap.put("lightyellow", 0xFFFFE0);
        x11ColorMap.put("lime", 0x00FF00);
        x11ColorMap.put("limegreen", 0x32CD32);
        x11ColorMap.put("linen", 0xFAF0E6);
        x11ColorMap.put("magenta", 0xFF00FF);
        x11ColorMap.put("maroon", 0x800000);
        x11ColorMap.put("mediumaquamarine", 0x66CDAA);
        x11ColorMap.put("mediumblue", 0x0000CD);
        x11ColorMap.put("mediumorchid", 0xBA55D3);
        x11ColorMap.put("mediumpurple", 0x9370DB);
        x11ColorMap.put("mediumseagreen", 0x3CB371);
        x11ColorMap.put("mediumslateblue", 0x7B68EE);
        x11ColorMap.put("mediumspringgreen", 0x00FA9A);
        x11ColorMap.put("mediumturquoise", 0x48D1CC);
        x11ColorMap.put("mediumvioletred", 0xC71585);
        x11ColorMap.put("midnightblue", 0x191970);
        x11ColorMap.put("mintcream", 0xF5FFFA);
        x11ColorMap.put("mistyrose", 0xFFE4E1);
        x11ColorMap.put("moccasin", 0xFFE4B5);
        x11ColorMap.put("navajowhite", 0xFFDEAD);
        x11ColorMap.put("navy", 0x000080);
        x11ColorMap.put("oldlace", 0xFDF5E6);
        x11ColorMap.put("olive", 0x808000);
        x11ColorMap.put("olivedrab", 0x6B8E23);
        x11ColorMap.put("orange", 0xFFA500);
        x11ColorMap.put("orangered", 0xFF4500);
        x11ColorMap.put("orchid", 0xDA70D6);
        x11ColorMap.put("palegoldenrod", 0xEEE8AA);
        x11ColorMap.put("palegreen", 0x98FB98);
        x11ColorMap.put("paleturquoise", 0xAFEEEE);
        x11ColorMap.put("palevioletred", 0xDB7093);
        x11ColorMap.put("papayawhip", 0xFFEFD5);
        x11ColorMap.put("peachpuff", 0xFFDAB9);
        x11ColorMap.put("peru", 0xCD853F);
        x11ColorMap.put("pink", 0xFFC0CB);
        x11ColorMap.put("plum", 0xDDA0DD);
        x11ColorMap.put("powderblue", 0xB0E0E6);
        x11ColorMap.put("purple", 0x800080);
        x11ColorMap.put("red", 0xFF0000);
        x11ColorMap.put("rosybrown", 0xBC8F8F);
        x11ColorMap.put("royalblue", 0x4169E1);
        x11ColorMap.put("saddlebrown", 0x8B4513);
        x11ColorMap.put("salmon", 0xFA8072);
        x11ColorMap.put("sandybrown", 0xF4A460);
        x11ColorMap.put("seagreen", 0x2E8B57);
        x11ColorMap.put("seashell", 0xFFF5EE);
        x11ColorMap.put("sienna", 0xA0522D);
        x11ColorMap.put("silver", 0xC0C0C0);
        x11ColorMap.put("skyblue", 0x87CEEB);
        x11ColorMap.put("slateblue", 0x6A5ACD);
        x11ColorMap.put("slategray", 0x708090);
        x11ColorMap.put("slategrey", 0x708090);
        x11ColorMap.put("snow", 0xFFFAFA);
        x11ColorMap.put("springgreen", 0x00FF7F);
        x11ColorMap.put("steelblue", 0x4682B4);
        x11ColorMap.put("tan", 0xD2B48C);
        x11ColorMap.put("teal", 0x008080);
        x11ColorMap.put("thistle", 0xD8BFD8);
        x11ColorMap.put("tomato", 0xFF6347);
        x11ColorMap.put("turquoise", 0x40E0D0);
        x11ColorMap.put("violet", 0xEE82EE);
        x11ColorMap.put("wheat", 0xF5DEB3);
        x11ColorMap.put("white", 0xFFFFFF);
        x11ColorMap.put("whitesmoke", 0xF5F5F5);
        x11ColorMap.put("yellow", 0xFFFF00);
        x11ColorMap.put("yellowgreen", 0x9ACD32);
    }

    static Document sanitize(Context context, String html, boolean show_images, boolean autolink) {
        try {
            return _sanitize(context, html, show_images, autolink);
        } catch (Throwable ex) {
            // OutOfMemoryError
            Log.e(ex);
            Document document = Document.createShell("");
            Element strong = document.createElement("strong");
            strong.text(Log.formatThrowable(ex));
            document.body().appendChild(strong);
            return document;
        }
    }

    private static Document _sanitize(Context context, String html, boolean show_images, boolean autolink) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean text_color = prefs.getBoolean("text_color", true);
        boolean display_hidden = prefs.getBoolean("display_hidden", false);
        boolean disable_tracking = prefs.getBoolean("disable_tracking", true);

        Document parsed = JsoupEx.parse(html);

        // <!--[if ...]><!--> ... <!--<![endif]-->
        // https://docs.microsoft.com/en-us/previous-versions/windows/internet-explorer/ie-developer/compatibility/hh801214(v=vs.85)
        if (!display_hidden && false)
            parsed.filter(new NodeFilter() {
                private boolean remove = false;

                @Override
                public FilterResult head(Node node, int depth) {
                    if (node instanceof Comment) {
                        String data = ((Comment) node).getData().trim();
                        if (data.startsWith("[if") && !data.endsWith("endif]")) {
                            remove = true;
                            return FilterResult.REMOVE;
                        } else if (remove && data.endsWith("endif]")) {
                            remove = false;
                            return FilterResult.REMOVE;
                        }
                    }
                    return (remove ? FilterResult.REMOVE : FilterResult.CONTINUE);
                }

                @Override
                public FilterResult tail(Node node, int depth) {
                    return FilterResult.CONTINUE;
                }
            });

        // <html xmlns:v="urn:schemas-microsoft-com:vml"
        //   xmlns:o="urn:schemas-microsoft-com:office:office"
        //   xmlns:w="urn:schemas-microsoft-com:office:word"
        //   xmlns:m="http://schemas.microsoft.com/office/2004/12/omml"
        //   xmlns="http://www.w3.org/TR/REC-html40">

        // <o:p>&nbsp;</o:p></span>

        // Default XHTML namespace: http://www.w3.org/1999/xhtml

        String ns = null;
        for (Element h : parsed.select("html"))
            for (Attribute a : h.attributes()) {
                String key = a.getKey();
                String value = a.getValue();
                if (value != null &&
                        key.startsWith("xmlns:") &&
                        value.startsWith("http://www.w3.org/")) {
                    ns = key.split(":")[1];
                    break;
                }
            }
        for (Element e : parsed.select("*")) {
            String tag = e.tagName();
            if (tag.contains(":")) {
                if (ns != null && tag.startsWith(ns)) {
                    e.tagName(tag.split(":")[1]);
                    Log.i("Updated tag=" + tag + " to=" + e.tagName());
                } else {
                    e.remove();
                    Log.i("Removed tag=" + tag);
                }
            }
        }

        Whitelist whitelist = Whitelist.relaxed()
                .addTags("hr", "abbr", "big", "font")
                .removeTags("col", "colgroup", "thead", "tbody")
                .removeAttributes("table", "width")
                .removeAttributes("td", "colspan", "rowspan", "width")
                .removeAttributes("th", "colspan", "rowspan", "width")
                .addProtocols("img", "src", "cid")
                .addProtocols("img", "src", "data");
        if (text_color)
            whitelist
                    .addAttributes(":all", "style")
                    .addAttributes("font", "color");

        final Document document = new Cleaner(whitelist).clean(parsed);

        boolean dark = Helper.isDarkTheme(context);

        // Font
        for (Element font : document.select("font")) {
            String color = font.attr("color");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                font.removeAttr("color");
            font.removeAttr("face");
            font.attr("style", "color:" + color + ";");
            font.tagName("span");
        }

        // Sanitize styles
        for (Element element : document.select("*")) {
            String style = element.attr("style");
            if (!TextUtils.isEmpty(style)) {
                StringBuilder sb = new StringBuilder();

                String[] params = style.split(";");
                for (String param : params) {
                    String[] kv = param.split(":");
                    if (kv.length == 2) {
                        String key = kv[0].trim().toLowerCase(Locale.ROOT);
                        String value = kv[1].toLowerCase(Locale.ROOT)
                                .replace("!important", "")
                                .trim()
                                .replaceAll("\\s+", " ");
                        switch (key) {
                            case "color":
                                Integer color = parseColor(value, dark);
                                if (color != null) {
                                    // fromHtml does not support transparency
                                    String c = String.format("#%08x", color | 0xFF000000);
                                    sb.append("color:").append(c).append(";");
                                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
                                        element.attr("color", c);
                                }
                                break;

                            case "background":
                            case "background-color":
                                break;

                            case "line-through":
                                sb.append(param).append(";");
                                break;

                            case "display":
                                if (!display_hidden && "none".equals(value)) {
                                    Log.i("Removing hidden element " + element.tagName());
                                    element.empty();
                                }
                                if ("inline".equals(value) || "inline-block".equals(value)) {
                                    if (element.nextSibling() != null)
                                        element.attr("inline", "true");
                                }
                                break;

                            case "height":
                            case "width":
                                //case "font-size":
                                //case "line-height":
                                if (!display_hidden &&
                                        ("0".equals(value) || "0px".equals(value))) {
                                    Log.i("Removing hidden element " + element.tagName());
                                    element.empty();
                                }
                                break;
                        }
                    }
                }

                if (sb.length() == 0)
                    element.removeAttr("style");
                else {
                    element.attr("style", sb.toString());
                    if (BuildConfig.DEBUG)
                        Log.i("Style=" + sb);
                }
            }
        }

        // Remove new lines without surrounding content
        //for (Element br : document.select("br"))
        //    if (br.parent() != null && !hasVisibleContent(br.parent().childNodes()))
        //        br.tagName("span");

        for (Element div : document.select("div"))
            if (div.children().select("div").size() == 0 &&
                    hasVisibleContent(div.childNodes())) {
                Node last = div.childNode(div.childNodeSize() - 1);
                if (last != null && "br".equals(last.nodeName()))
                    last.remove();
            }

        // Paragraphs
        for (Element p : document.select("p")) {
            p.appendElement("br");
            p.tagName("div");
        }

        // Short quotes
        for (Element q : document.select("q")) {
            q.prependText("\"");
            q.appendText("\"");
            q.tagName("em");
        }

        // Pre formatted text
        for (Element pre : document.select("pre")) {
            pre.html(formatPre(pre.wholeText()));
            pre.tagName("div");
        }

        // Code
        document.select("code").tagName("strong");

        // Lines
        for (Element hr : document.select("hr")) {
            hr.tagName("div");
            hr.text("----------------------------------------");
        }

        // Descriptions
        document.select("dl").tagName("div");
        for (Element dt : document.select("dt")) {
            dt.tagName("strong");
            dt.appendElement("br");
        }
        for (Element dd : document.select("dd")) {
            dd.tagName("em");
            dd.appendElement("br").appendElement("br");
        }

        // Abbreviations
        document.select("abbr").tagName("u");

        // Subscript/Superscript
        for (Element subp : document.select("sub,sup")) {
            Element small = document.createElement("small");
            small.html(subp.html());
            subp.html(small.outerHtml());
        }

        // Lists
        for (Element li : document.select("li")) {
            li.tagName("span");
            Element parent = li.parent();
            if (parent == null || "ul".equals(parent.tagName()))
                li.prependText("• ");
            else
                li.prependText((li.elementSiblingIndex() + 1) + ". ");
            li.appendElement("br"); // line break after list item
        }
        document.select("ol").tagName("div");
        document.select("ul").tagName("div");

        // Tables
        for (Element col : document.select("th,td")) {
            // separate columns
            if (hasVisibleContent(col.childNodes()))
                if (col.nextElementSibling() != null)
                    col.appendText(" ");

            if ("th".equals(col.tagName()))
                col.tagName("strong");
            else
                col.tagName("span");
        }

        for (Element row : document.select("tr")) {
            row.tagName("span");
            if (hasVisibleContent(row.childNodes())) {
                Element next = row.nextElementSibling();
                if (next != null && "tr".equals(next.tagName()))
                    row.appendElement("br");
            }
        }

        document.select("caption").tagName("div");

        for (Element table : document.select("table"))
            if (table.parent() != null && "a".equals(table.parent().tagName()))
                table.tagName("span"); // Links cannot contain tables
            else
                table.tagName("div");

        // Remove tracking pixels
        if (disable_tracking)
            removeTrackingPixels(context, document);

        // Images
        for (Element img : document.select("img")) {
            String alt = img.attr("alt");
            String src = img.attr("src");
            String tracking = img.attr("tracking");

            if (!show_images && !TextUtils.isEmpty(alt))
                if (TextUtils.isEmpty(tracking))
                    img.appendText(" " + alt + " ");
                else {
                    img.append("&nbsp;");
                    Element a = document.createElement("a");
                    a.attr("href", tracking);
                    a.text(alt);
                    img.appendChild(a);
                    img.appendText(" ");
                }

            // Annotate source with width and height
            if (!TextUtils.isEmpty(src)) {
                int width = 0;
                int height = 0;

                String awidth = img.attr("width");
                for (int i = 0; i < awidth.length(); i++)
                    if (Character.isDigit(awidth.charAt(i)))
                        width = width * 10 + (byte) awidth.charAt(i) - (byte) '0';
                    else
                        break;

                String aheight = img.attr("height");
                for (int i = 0; i < aheight.length(); i++)
                    if (Character.isDigit(aheight.charAt(i)))
                        height = height * 10 + (byte) aheight.charAt(i) - (byte) '0';
                    else
                        break;

                if (width != 0 || height != 0) {
                    ImageHelper.AnnotatedSource a = new ImageHelper.AnnotatedSource(
                            src, width, height, !TextUtils.isEmpty(tracking));
                    img.attr("src", a.getAnnotated());
                }
            }
        }

        // Autolink
        if (autolink) {
            final Pattern pattern = Pattern.compile(
                    PatternsCompat.AUTOLINK_EMAIL_ADDRESS.pattern() + "|" +
                            PatternsCompat.AUTOLINK_WEB_URL.pattern());

            NodeTraversor.traverse(new NodeVisitor() {
                private int links = 0;

                @Override
                public void head(Node node, int depth) {
                    if (links < MAX_AUTO_LINK && node instanceof TextNode) {
                        TextNode tnode = (TextNode) node;
                        String text = tnode.getWholeText();

                        Matcher matcher = pattern.matcher(text);
                        if (matcher.find()) {
                            Element span = document.createElement("span");

                            int pos = 0;
                            do {
                                boolean linked = false;
                                Node parent = tnode.parent();
                                while (parent != null) {
                                    if ("a".equals(parent.nodeName())) {
                                        linked = true;
                                        break;
                                    }
                                    parent = parent.parent();
                                }

                                boolean email = matcher.group().contains("@") && !matcher.group().contains(":");
                                Log.d("Web url=" + matcher.group() +
                                        " " + matcher.start() + "..." + matcher.end() + "/" + text.length() +
                                        " linked=" + linked + " email=" + email + " count=" + links);

                                if (linked)
                                    span.appendText(text.substring(pos, matcher.end()));
                                else {
                                    span.appendText(text.substring(pos, matcher.start()));

                                    Element a = document.createElement("a");
                                    a.attr("href", (email ? "mailto:" : "") + matcher.group());
                                    a.text(matcher.group());
                                    span.appendChild(a);

                                    links++;
                                }

                                pos = matcher.end();
                            } while (links < MAX_AUTO_LINK && matcher.find());

                            span.appendText(text.substring(pos));

                            tnode.before(span);
                            tnode.text("");
                        }
                    }
                }

                @Override
                public void tail(Node node, int depth) {
                }
            }, document);
        }

        for (Element div : document.select("div")) {
            boolean inline = Boolean.parseBoolean(div.attr("inline"));
            if (inline)
                div.tagName("span");
        }

        // Selective new lines
        for (Element div : document.select("div")) {
            Node prev = div.previousSibling();
            if (prev != null && hasVisibleContent(Arrays.asList(prev)))
                div.prependElement("br");

            if (hasVisibleContent(div.childNodes()))
                div.appendElement("br");
        }

        for (Element div : document.select("div"))
            div.tagName("span");

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
            for (Element span : document.select("span"))
                if (!TextUtils.isEmpty(span.attr("color")))
                    span.tagName("font");

        if (document.body() == null) {
            Log.e("Sanitize without body");
            document.normalise();
        }

        return document;
    }

    private static Integer parseColor(@NonNull String value, boolean dark) {
        // https://developer.mozilla.org/en-US/docs/Web/CSS/color_value
        String c = value
                .replace("null", "")
                .replace("none", "")
                .replace("unset", "")
                .replace("inherit", "")
                .replace("initial", "")
                .replace("windowtext", "")
                .replace("currentcolor", "")
                .replace("transparent", "")
                .replaceAll("[^a-z0-9(),.%#]", "")
                .replaceAll("#+", "#");

        Integer color = null;
        try {
            if (TextUtils.isEmpty(c))
                return null;
            else if (c.startsWith("#")) {
                if (c.length() > 1) {
                    String code = c.substring(1);
                    if (x11ColorMap.containsKey(code)) // workaround
                        color = x11ColorMap.get(code);
                    else
                        color = Integer.decode(c);
                }
            } else if (c.startsWith("rgb") || c.startsWith("hsl")) {
                int s = c.indexOf("(");
                int e = c.indexOf(")");
                if (s > 0 && e > s) {
                    String[] component = c.substring(s + 1, e).split(",");

                    for (int i = 0; i < component.length; i++)
                        if (component[i].endsWith("%"))
                            if (c.startsWith("rgb")) {
                                int percent = Integer.parseInt(component[i].replace("%", ""));
                                component[i] = Integer.toString(Math.round(255 * (percent / 100f)));
                            } else
                                component[i] = component[i].replace("%", "");

                    if (c.startsWith("rgb") && component.length >= 3)
                        color = Color.rgb(
                                Integer.parseInt(component[0]),
                                Integer.parseInt(component[1]),
                                Integer.parseInt(component[2]));
                    else if (c.startsWith("hsl") && component.length >= 3)
                        color = ColorUtils.HSLToColor(new float[]{
                                Float.parseFloat(component[0]),
                                Integer.parseInt(component[1]) / 100f,
                                Integer.parseInt(component[2]) / 100f});
                }
            } else if (x11ColorMap.containsKey(c))
                color = x11ColorMap.get(c);
            else
                try {
                    color = Color.parseColor(c);
                } catch (IllegalArgumentException ex) {
                    // Workaround
                    color = Integer.decode("#" + c);
                }

            if (BuildConfig.DEBUG)
                Log.i("Color " + c + "=" + (color == null ? null : Long.toHexString(color)));

        } catch (Throwable ex) {
            Log.e("Color=" + c + ": " + ex);
        }

        if (color != null) {
            if (dark || color != Color.BLACK)
                color = Helper.adjustLuminance(color, dark, MIN_LUMINANCE);

            color &= 0xFFFFFF;
        }

        return color;
    }

    private static boolean hasVisibleContent(List<Node> nodes) {
        for (Node node : nodes)
            if (node instanceof TextNode && !((TextNode) node).isBlank())
                return true;
            else if (node instanceof Element) {
                Element element = (Element) node;
                if (!element.isBlock() &&
                        (element.hasText() ||
                                element.selectFirst("a") != null ||
                                element.selectFirst("img") != null))
                    return true;
            }
        return false;
    }

    static String formatPre(String text) {
        int level = 0;
        StringBuilder sb = new StringBuilder();
        String[] lines = text.split("\\r?\\n");
        for (String line : lines) {
            // Opening quotes
            int tlevel = 0;
            while (line.startsWith(">")) {
                tlevel++;
                if (tlevel > level)
                    sb.append("<blockquote>");

                line = line.substring(1); // >

                if (line.startsWith(" "))
                    line = line.substring(1);
            }

            // Closing quotes
            for (int i = 0; i < level - tlevel; i++)
                sb.append("</blockquote>");
            level = tlevel;

            // Tabs characters
            StringBuilder l = new StringBuilder();
            for (int j = 0; j < line.length(); j++) {
                char kar = line.charAt(j);
                if (kar == '\t') {
                    l.append(' ');
                    while (l.length() % TAB_SIZE != 0)
                        l.append(' ');
                } else
                    l.append(kar);
            }
            line = l.toString();

            // Html characters
            line = Html.escapeHtml(line);

            // Space characters
            int len = line.length();
            for (int j = 0; j < len; j++) {
                char kar = line.charAt(j);
                if (kar == ' ') {
                    // Prevent trimming start
                    // Keep one space for word wrapping
                    if (j == 0 || (j + 1 < len && line.charAt(j + 1) == ' '))
                        sb.append("&nbsp;");
                    else
                        sb.append(' ');
                } else
                    sb.append(kar);
            }

            sb.append("<br>");
        }

        // Closing quotes
        for (int i = 0; i < level; i++)
            sb.append("</blockquote>");

        return sb.toString();
    }

    static void removeTrackingPixels(Context context, Document document) {
        Drawable d = ContextCompat.getDrawable(context, R.drawable.baseline_my_location_24);
        d.setTint(Helper.resolveColor(context, R.attr.colorWarning));

        Bitmap bm = Bitmap.createBitmap(d.getIntrinsicWidth(), d.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bm);
        d.setBounds(0, 0, c.getWidth(), c.getHeight());
        d.draw(c);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, bos);

        StringBuilder sb = new StringBuilder();
        sb.append("data:image/png;base64,");
        sb.append(Base64.encodeToString(bos.toByteArray(), Base64.NO_WRAP));

        // Build list of allowed hosts
        List<String> hosts = new ArrayList<>();
        for (Element img : document.select("img")) {
            String src = img.attr("src");
            if (!TextUtils.isEmpty(src) && !isTrackingPixel(img)) {
                Uri uri = Uri.parse(img.attr("src"));
                String host = uri.getHost();
                if (host != null && !hosts.contains(host))
                    hosts.add(host);
            }
        }

        // Images
        for (Element img : document.select("img")) {
            img.removeAttr("tracking");
            String src = img.attr("src");
            if (!TextUtils.isEmpty(src) && isTrackingPixel(img)) {
                Uri uri = Uri.parse(src);
                String host = uri.getHost();
                if (host == null || !hosts.contains(host)) {
                    img.attr("src", sb.toString());
                    img.attr("alt", context.getString(R.string.title_legend_tracking_pixel));
                    img.attr("height", "24");
                    img.attr("width", "24");
                    img.attr("style", "display:block !important; width:24px !important; height:24px !important;");
                    img.attr("tracking", src);
                }
            }
        }
    }

    private static boolean isTrackingPixel(Element img) {
        String width = img.attr("width").trim();
        String height = img.attr("height").trim();

        if (TextUtils.isEmpty(width) || TextUtils.isEmpty(height))
            return false;

        try {
            return (Integer.parseInt(width) * Integer.parseInt(height) <= TRACKING_PIXEL_SURFACE);
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    static void embedInlineImages(Context context, long id, Document document) throws IOException {
        DB db = DB.getInstance(context);
        for (Element img : document.select("img")) {
            String src = img.attr("src");
            if (src.startsWith("cid:")) {
                String cid = '<' + src.substring(4) + '>';
                EntityAttachment attachment = db.attachment().getAttachment(id, cid);
                if (attachment != null && attachment.available) {
                    File file = attachment.getFile(context);
                    try (InputStream is = new FileInputStream(file)) {
                        byte[] bytes = new byte[(int) file.length()];
                        if (is.read(bytes) != bytes.length)
                            throw new IOException("length");

                        StringBuilder sb = new StringBuilder();
                        sb.append("data:");
                        sb.append(attachment.type);
                        sb.append(";base64,");
                        sb.append(Base64.encodeToString(bytes, Base64.NO_WRAP));

                        img.attr("src", sb.toString());
                    }
                }
            }
        }
    }

    static void setViewport(Document document) {
        // https://developer.mozilla.org/en-US/docs/Mozilla/Mobile/Viewport_meta_tag
        document.head().select("meta").select("[name=viewport]").remove();

        document.head().prependChild(document.createElement("meta")
                .attr("name", "viewport")
                .attr("content", "width=device-width, initial-scale=1.0"));

        Log.i(document.head().html());
    }

    static String getPreview(String body) {
        try {
            return _getPreview(body);
        } catch (OutOfMemoryError ex) {
            Log.e(ex);
            return null;
        }
    }

    private static String _getPreview(String body) {
        if (body == null)
            return null;

        String text = JsoupEx.parse(body).text();

        String preview = text.substring(0, Math.min(text.length(), PREVIEW_SIZE));
        if (preview.length() < text.length())
            preview += "…";

        return preview;
    }

    static String getText(String html) {
        final StringBuilder sb = new StringBuilder();

        html = html.replace("<br> ", "<br>");

        NodeTraversor.traverse(new NodeVisitor() {
            private int qlevel = 0;
            private int tlevel = 0;
            private int plevel = 0;
            private int lindex = 0;

            public void head(Node node, int depth) {
                if (node instanceof TextNode)
                    if (plevel > 0) {
                        String[] lines = ((TextNode) node).getWholeText().split("\\r?\\n");
                        for (String line : lines) {
                            append(line, true);
                            newline();
                        }
                    } else
                        append(((TextNode) node).text());
                else {
                    String name = node.nodeName();
                    if ("li".equals(name))
                        append("*");
                    else if ("blockquote".equals(name))
                        qlevel++;
                    else if ("pre".equals(name))
                        plevel++;

                    if (heads.contains(name))
                        newline();
                }
            }

            public void tail(Node node, int depth) {
                String name = node.nodeName();
                if ("a".equals(name))
                    append("[" + node.attr("href") + "]");
                else if ("img".equals(name))
                    append("[" + node.attr("src") + "]");
                else if ("th".equals(name) || "td".equals(name)) {
                    Node next = node.nextSibling();
                    if (next == null || !("th".equals(next.nodeName()) || "td".equals(next.nodeName())))
                        newline();
                    else
                        append(" ");
                } else if ("blockquote".equals(name))
                    qlevel--;
                else if ("pre".equals(name))
                    plevel--;

                if (tails.contains(name))
                    newline();
            }

            private void append(String text) {
                append(text, false);
            }

            private void append(String text, boolean raw) {
                if (tlevel != qlevel) {
                    newline();
                    tlevel = qlevel;
                }

                if (!raw && !"-- ".equals(text)) {
                    text = text.trim();
                    if (lindex > 0)
                        text = " " + text;
                }

                sb.append(text);
                lindex += text.length();
            }

            private void newline() {
                lindex = 0;
                sb.append("\n");

                for (int i = 0; i < qlevel; i++)
                    sb.append("> ");
            }
        }, JsoupEx.parse(html));

        sb.append("\n");

        return sb.toString();
    }

    static Spanned highlightHeaders(Context context, String headers) {
        int colorAccent = Helper.resolveColor(context, R.attr.colorAccent);
        SpannableStringBuilder ssb = new SpannableStringBuilder(headers);
        int index = 0;
        for (String line : headers.split("\n")) {
            if (line.length() > 0 && !Character.isWhitespace(line.charAt(0))) {
                int colon = line.indexOf(':');
                if (colon > 0)
                    ssb.setSpan(new ForegroundColorSpan(colorAccent), index, index + colon, 0);
            }
            index += line.length() + 1;
        }
        return ssb;
    }

    static Spanned fromHtml(@NonNull String html) {
        return fromHtml(html, null, null);
    }

    static Spanned fromHtml(@NonNull String html, @Nullable Html.ImageGetter imageGetter, @Nullable Html.TagHandler tagHandler) {
        Spanned spanned = HtmlCompat.fromHtml(html, FROM_HTML_SEPARATOR_LINE_BREAK_LIST_ITEM, imageGetter, tagHandler);

        int i = spanned.length();
        while (i > 1 && spanned.charAt(i - 2) == '\n' && spanned.charAt(i - 1) == '\n')
            i--;
        if (i != spanned.length())
            spanned = (Spanned) spanned.subSequence(0, i);

        return spanned;
    }

    static String toHtml(Spanned spanned) {
        String html = HtmlCompat.toHtml(spanned, TO_HTML_PARAGRAPH_LINES_CONSECUTIVE);

        // @Google: why convert size to and from in a different way?
        Document doc = JsoupEx.parse(html);
        for (Element element : doc.select("span")) {
            String style = element.attr("style");
            if (style.startsWith("font-size:")) {
                int colon = style.indexOf(':');
                int semi = style.indexOf("em;", colon);
                if (semi > colon)
                    try {
                        String hsize = style.substring(colon + 1, semi).replace(',', '.');
                        float size = Float.parseFloat(hsize);
                        element.tagName(size < 1.0f ? "small" : "big");
                        element.attributes().remove("style");
                    } catch (NumberFormatException ex) {
                        Log.e(ex);
                    }
            }
        }

        return doc.html();
    }
}
