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

    Copyright 2018-2019 by Marcel Bokhorst (M66B)
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
import androidx.core.text.HtmlCompat;
import androidx.core.util.PatternsCompat;
import androidx.preference.PreferenceManager;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Whitelist;
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
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static androidx.core.text.HtmlCompat.FROM_HTML_SEPARATOR_LINE_BREAK_LIST_ITEM;
import static androidx.core.text.HtmlCompat.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE;

public class HtmlHelper {
    private static final int PREVIEW_SIZE = 500; // characters

    private static final float MIN_LUMINANCE = 0.5f;
    private static final int MAX_AUTO_LINK = 250;
    private static final int TRACKING_PIXEL_SURFACE = 25; // pixels

    private static final List<String> heads = Collections.unmodifiableList(Arrays.asList(
            "h1", "h2", "h3", "h4", "h5", "h6", "p", "ol", "ul", "table", "br", "hr"));
    private static final List<String> tails = Collections.unmodifiableList(Arrays.asList(
            "h1", "h2", "h3", "h4", "h5", "h6", "p", "ol", "ul", "li"));

    static String sanitize(Context context, String html, boolean show_images) {
        try {
            return _sanitize(context, html, show_images);
        } catch (Throwable ex) {
            // OutOfMemoryError
            Log.e(ex);
            return Helper.formatThrowable(ex);
        }
    }

    private static String _sanitize(Context context, String html, boolean show_images) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean text_color = prefs.getBoolean("text_color", true);
        boolean display_hidden = prefs.getBoolean("display_hidden", false);
        boolean disable_tracking = prefs.getBoolean("disable_tracking", true);

        Document parsed = JsoupEx.parse(html);

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
                                .replace(" ", "");
                        switch (key) {
                            case "color":
                                String c = value
                                        .replace("inherit", "")
                                        .replace("initial", "")
                                        .replace("windowtext", "")
                                        .replace("transparent", "")
                                        .replace("!important", "");

                                Integer color = null;
                                try {
                                    if (TextUtils.isEmpty(c))
                                        ; // Do nothing
                                    else if (c.startsWith("#"))
                                        color = Integer.decode(c) | 0xFF000000;
                                    else if (c.startsWith("rgb")) {
                                        int s = c.indexOf("(");
                                        int e = c.indexOf(")");
                                        if (s > 0 && e > s) {
                                            String[] rgb = c.substring(s + 1, e).split(",");
                                            if (rgb.length == 3)
                                                color = Color.rgb(
                                                        Integer.parseInt(rgb[0]),
                                                        Integer.parseInt(rgb[1]),
                                                        Integer.parseInt(rgb[2])
                                                );
                                        }
                                    } else if (c.equals("orange"))
                                        color = 0Xffa500; // CSS Level 2
                                    else
                                        color = Color.parseColor(c);
                                } catch (Throwable ex) {
                                    Log.e("Color=" + c);
                                }

                                if (color != null && !(dark && color == Color.BLACK)) {
                                    color = Helper.adjustLuminance(color, dark, MIN_LUMINANCE);
                                    c = String.format("#%06x", color & 0xFFFFFF);
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
                                if ("none".equals(value) && !display_hidden) {
                                    Log.i("Removing element " + element.tagName());
                                    element.empty();
                                }

                                if ("inline".equals(value) || "inline-block".equals(value))
                                    element.attr("inline", "true");

                                break;
                        }
                    }
                }

                if (sb.length() == 0)
                    element.removeAttr("style");
                else
                    element.attr("style", sb.toString());
            }
        }

        // Remove new lines without surrounding content
        for (Element br : document.select("br"))
            if (br.parent() != null && !hasVisibleContent(br.parent().childNodes()))
                br.tagName("span");

        // Paragraphs
        for (Element p : document.select("p"))
            p.tagName("div");

        // Short quotes
        for (Element q : document.select("q")) {
            q.prependText("\"");
            q.appendText("\"");
            q.tagName("em");
        }

        // Pre formatted text
        for (Element pre : document.select("pre")) {
            Element div = document.createElement("font");
            div.attr("face", "monospace");

            String[] lines = pre.wholeText().split("\\r?\\n");
            for (String line : lines) {
                line = Html.escapeHtml(line);

                StringBuilder sb = new StringBuilder();
                int len = line.length();
                for (int j = 0; j < len; j++) {
                    char kar = line.charAt(j);
                    if (kar == ' ' &&
                            j + 1 < len && line.charAt(j + 1) == ' ')
                        sb.append("&nbsp;");
                    else
                        sb.append(kar);
                }

                Element span = document.createElement("span");
                span.html(sb.toString());
                div.appendChild(span);
                div.appendElement("br");
            }

            pre.replaceWith(div);
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
            li.prependText("* ");
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
                            if (BuildConfig.DEBUG)
                                Log.i("Web url=" + matcher.group() +
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

        // Selective new lines
        for (Element div : document.select("div"))
            if (!Boolean.parseBoolean(div.attr("inline")) &&
                    div.children().select("div").size() == 0 &&
                    hasVisibleContent(div.childNodes())) {
                div.appendElement("br");
                div.appendElement("br");
            }

        for (Element div : document.select("div"))
            div.tagName("span");

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
            for (Element span : document.select("span"))
                if (!TextUtils.isEmpty(span.attr("color")))
                    span.tagName("font");

        Element body = document.body();
        return (body == null ? "" : body.html());
    }

    private static boolean hasVisibleContent(List<Node> nodes) {
        for (Node node : nodes)
            if (node instanceof TextNode && !((TextNode) node).isBlank())
                return true;
            else if (node instanceof Element) {
                Element element = (Element) node;
                if (!element.isBlock() &&
                        (element.hasText() || element.selectFirst("img") != null))
                    return true;
            }
        return false;
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

            public void head(Node node, int depth) {
                if (node instanceof TextNode)
                    if (plevel > 0) {
                        String[] lines = ((TextNode) node).getWholeText().split("\\r?\\n");
                        for (String line : lines) {
                            append(line);
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
                if (tlevel != qlevel) {
                    newline();
                    tlevel = qlevel;
                }
                sb.append(text);
            }

            private void newline() {
                sb.append("\n");
                for (int i = 0; i < qlevel; i++)
                    sb.append('>');
                if (qlevel > 0)
                    sb.append(' ');
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
