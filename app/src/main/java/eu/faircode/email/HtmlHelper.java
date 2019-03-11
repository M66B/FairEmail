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
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Base64;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;
import androidx.core.util.PatternsCompat;

import static androidx.core.text.HtmlCompat.FROM_HTML_SEPARATOR_LINE_BREAK_LIST_ITEM;
import static androidx.core.text.HtmlCompat.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE;

public class HtmlHelper {
    static final int PREVIEW_SIZE = 250;

    private static final List<String> heads = Arrays.asList("h1", "h2", "h3", "h4", "h5", "h6", "p", "table", "ol", "ul", "br", "hr");
    private static final List<String> tails = Arrays.asList("h1", "h2", "h3", "h4", "h5", "h6", "p", "ol", "ul", "li");

    static String removeTracking(Context context, String html) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean remove_tracking = prefs.getBoolean("remove_tracking", true);
        if (!remove_tracking)
            return html;

        Document document = Jsoup.parse(html);

        // Remove tracking pixels
        for (Element img : document.select("img")) {
            String src = img.attr("src");
            String height = img.attr("height").trim();
            String width = img.attr("width").trim();
            if ("1".equals(height) && "1".equals(width) && !TextUtils.isEmpty(src))
                img.removeAttr("src");
        }

        // Remove Javascript
        for (Element e : document.select("*"))
            for (Attribute a : e.attributes()) {
                String v = a.getValue();
                if (v != null && v.trim().toLowerCase().startsWith("javascript:"))
                    e.removeAttr(a.getKey());
            }

        // Remove scripts
        document.select("script").remove();

        return document.outerHtml();
    }

    static String sanitize(Context context, String html, boolean showQuotes) {
        final Document document = Jsoup.parse(Jsoup.clean(html, Whitelist
                .relaxed()
                .addTags("hr")
                .removeTags("col", "colgroup", "thead", "tbody")
                .addProtocols("img", "src", "cid")
                .addProtocols("img", "src", "data")));

        // Quotes
        if (!showQuotes)
            for (Element quote : document.select("blockquote"))
                quote.html("&#8230;");

        // Tables
        for (Element col : document.select("th,td")) {
            // prevent line breaks
            col.select("br").tagName("span").html(" ");
            col.select("div").tagName("span");

            // separate columns by a space
            if (col.nextElementSibling() != null)
                col.append(" ");

            if ("th".equals(col.tagName()))
                col.tagName("strong");
            else
                col.tagName("span");
        }

        for (Element row : document.select("tr"))
            row.tagName("span").appendElement("br");

        document.select("caption").tagName("p");
        document.select("table").tagName("div");

        // Lists
        for (Element li : document.select("li")) {
            li.tagName("span");
            li.prependText("* ");
            li.appendElement("br"); // line break after list item
        }
        document.select("ol").tagName("div");
        document.select("ul").tagName("div");

        // Short quotes
        for (Element q : document.select("q")) {
            q.prependText("\"");
            q.appendText("\"");
            q.tagName("em");
        }

        // Pre formatted text
        for (Element code : document.select("pre")) {
            code.html(code.html().replaceAll("\\r?\\n", "<br />"));
            code.tagName("div");
        }

        // Code
        document.select("code").tagName("div");

        // Lines
        for (Element hr : document.select("hr")) {
            hr.tagName("div");
            hr.text("--------------------");
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

        // Images
        for (Element img : document.select("img")) {
            String src = img.attr("src");
            String alt = img.attr("alt");
            String height = img.attr("height").trim();
            String width = img.attr("width").trim();

            Element div = document.createElement("div");

            Uri uri = Uri.parse(src);
            if ("http".equals(uri.getScheme()) || "https".equals(uri.getScheme())) {
                boolean linked = false;
                for (Element parent : img.parents())
                    if ("a".equals(parent.tagName())) {
                        if (TextUtils.isEmpty(parent.attr("href")))
                            parent.attr("href", uri.toString());
                        linked = true;
                        break;
                    }

                if (linked)
                    div.appendChild(img);
                else {
                    Element a = document.createElement("a");
                    a.attr("href", uri.toString());
                    a.appendChild(img.clone());
                    div.appendChild(a);
                }
            }

            if (!TextUtils.isEmpty(alt)) {
                div.appendElement("br");
                div.appendElement("em").text(alt);
            }

            // Tracking image
            if ("1".equals(height) && "1".equals(width) && !TextUtils.isEmpty(src)) {
                div.appendElement("br");
                div.appendElement("strong").text(context.getString(R.string.title_hint_tracking_image));
            }

            img.replaceWith(div);
        }

        // Autolink
        NodeTraversor.traverse(new NodeVisitor() {
            @Override
            public void head(Node node, int depth) {
                if (node instanceof TextNode) {
                    TextNode tnode = (TextNode) node;
                    Element span = document.createElement("span");

                    int pos = 0;
                    String text = tnode.text();
                    Matcher matcher = PatternsCompat.WEB_URL.matcher(text);
                    while (matcher.find()) {
                        boolean linked = false;
                        Node parent = tnode.parent();
                        while (parent != null) {
                            if ("a".equals(parent.nodeName())) {
                                linked = true;
                                break;
                            }
                            parent = parent.parent();
                        }

                        String scheme = Uri.parse(matcher.group()).getScheme();

                        if (BuildConfig.DEBUG)
                            Log.i("Web url=" + matcher.group() + " linked=" + linked + " scheme=" + scheme);

                        if (linked || scheme == null)
                            span.appendText(text.substring(pos, matcher.end()));
                        else {
                            span.appendText(text.substring(pos, matcher.start()));

                            Element a = document.createElement("a");
                            a.attr("href", matcher.group());
                            a.text(matcher.group());
                            span.appendChild(a);
                        }

                        pos = matcher.end();
                    }
                    span.appendText(text.substring(pos));

                    tnode.before(span);
                    tnode.text("");
                }
            }

            @Override
            public void tail(Node node, int depth) {
            }
        }, document.body());

        return document.body().html();
    }

    static Drawable decodeImage(String source, Context context, long id, boolean show) {
        int px = Helper.dp2pixels(context, 48);

        if (TextUtils.isEmpty(source)) {
            Drawable d = context.getResources().getDrawable(R.drawable.baseline_broken_image_24, context.getTheme());
            d.setBounds(0, 0, px, px);
            return d;
        }

        boolean embedded = source.startsWith("cid:");
        boolean data = source.startsWith("data:");

        if (BuildConfig.DEBUG)
            Log.i("Image show=" + show + " embedded=" + embedded + " data=" + data + " source=" + source);

        if (!show) {
            // Show placeholder icon
            int resid = (embedded || data ? R.drawable.baseline_photo_library_24 : R.drawable.baseline_image_24);
            Drawable d = context.getResources().getDrawable(resid, context.getTheme());
            d.setBounds(0, 0, px, px);
            return d;
        }

        // Embedded images
        if (embedded) {
            String cid = "<" + source.substring(4) + ">";
            EntityAttachment attachment = DB.getInstance(context).attachment().getAttachment(id, cid);
            if (attachment == null) {
                Drawable d = context.getResources().getDrawable(R.drawable.baseline_broken_image_24, context.getTheme());
                d.setBounds(0, 0, px, px);
                return d;
            } else if (!attachment.available) {
                Drawable d = context.getResources().getDrawable(R.drawable.baseline_photo_library_24, context.getTheme());
                d.setBounds(0, 0, px, px);
                return d;
            } else {
                Bitmap bm = Helper.decodeImage(
                        EntityAttachment.getFile(context, attachment.id),
                        context.getResources().getDisplayMetrics().widthPixels);
                if (bm == null) {
                    Drawable d = context.getResources().getDrawable(R.drawable.baseline_broken_image_24, context.getTheme());
                    d.setBounds(0, 0, px, px);
                    return d;
                } else {
                    Drawable d = new BitmapDrawable(bm);
                    d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
                    return d;
                }
            }
        }

        // Data URI
        if (data)
            try {
                // "<img src=\"data:image/png;base64,iVBORw0KGgoAAA" +
                // "ANSUhEUgAAAAUAAAAFCAYAAACNbyblAAAAHElEQVQI12P4" +
                // "//8/w38GIAXDIBKE0DHxgljNBAAO9TXL0Y4OHwAAAABJRU" +
                // "5ErkJggg==\" alt=\"Red dot\" />";

                String base64 = source.substring(source.indexOf(',') + 1);
                byte[] bytes = Base64.decode(base64.getBytes(), 0);

                Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                if (bm == null)
                    throw new IllegalArgumentException("decode byte array failed");

                Drawable d = new BitmapDrawable(context.getResources(), bm);
                d.setBounds(0, 0, bm.getWidth(), bm.getHeight());
                return d;
            } catch (IllegalArgumentException ex) {
                Log.w(ex);
                Drawable d = context.getResources().getDrawable(R.drawable.baseline_broken_image_24, context.getTheme());
                d.setBounds(0, 0, px, px);
                return d;
            }

        // Get cache file name
        File dir = new File(context.getCacheDir(), "images");
        if (!dir.exists())
            dir.mkdir();
        File file = new File(dir, id + "_" + Math.abs(source.hashCode()) + ".png");

        if (file.exists()) {
            Log.i("Using cached " + file);
            Bitmap bm = BitmapFactory.decodeFile(file.getAbsolutePath());
            if (bm == null) {
                Drawable d = context.getResources().getDrawable(R.drawable.baseline_broken_image_24, context.getTheme());
                d.setBounds(0, 0, px, px);
                return d;
            } else {
                Drawable d = new BitmapDrawable(bm);
                d.setBounds(0, 0, bm.getWidth(), bm.getHeight());
                return d;
            }
        }

        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            Log.i("Probe " + source);
            try (InputStream probe = new URL(source).openStream()) {
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(probe, null, options);
            }

            Log.i("Download " + source);
            Bitmap bm;
            try (InputStream is = new URL(source).openStream()) {
                int scaleTo = context.getResources().getDisplayMetrics().widthPixels;
                int factor = 1;
                while (options.outWidth / factor > scaleTo)
                    factor *= 2;

                if (factor > 1) {
                    Log.i("Download image factor=" + factor);
                    options.inJustDecodeBounds = false;
                    options.inSampleSize = factor;
                    bm = BitmapFactory.decodeStream(is, null, options);
                } else
                    bm = BitmapFactory.decodeStream(is);
            }

            if (bm == null)
                throw new FileNotFoundException("Download image failed");

            Log.i("Downloaded image");

            try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
                bm.compress(Bitmap.CompressFormat.PNG, 90, os);
            }

            // Create drawable from bitmap
            Drawable d = new BitmapDrawable(context.getResources(), bm);
            d.setBounds(0, 0, bm.getWidth(), bm.getHeight());
            return d;
        } catch (Throwable ex) {
            // Show warning icon
            Log.w(ex);
            int res = (ex instanceof IOException && !(ex instanceof FileNotFoundException)
                    ? R.drawable.baseline_cloud_off_24
                    : R.drawable.baseline_broken_image_24);
            Drawable d = context.getResources().getDrawable(res, context.getTheme());
            d.setBounds(0, 0, px, px);
            return d;
        }
    }

    static String getPreview(String body) {
        String text = (body == null ? null : Jsoup.parse(body).text());
        return (text == null ? null : text.substring(0, Math.min(text.length(), PREVIEW_SIZE)));
    }

    static String getText(String html) {
        final StringBuilder sb = new StringBuilder();

        NodeTraversor.traverse(new NodeVisitor() {
            private int qlevel = 0;
            private int tlevel = 0;

            public void head(Node node, int depth) {
                if (node instanceof TextNode) {
                    append(((TextNode) node).text());
                    append(" ");
                } else {
                    String name = node.nodeName();
                    if ("li".equals(name))
                        append("* ");
                    else if ("blockquote".equals(name))
                        qlevel++;

                    if (heads.contains(name))
                        newline();
                }
            }

            public void tail(Node node, int depth) {
                String name = node.nodeName();
                if ("a".equals(name)) {
                    append("[");
                    append(node.absUrl("href"));
                    append("] ");
                } else if ("img".equals(name)) {
                    append("[");
                    append(node.absUrl("src"));
                    append("] ");
                } else if ("th".equals(name) || "td".equals(name)) {
                    Node next = node.nextSibling();
                    if (next == null || !("th".equals(next.nodeName()) || "td".equals(next.nodeName())))
                        newline();
                } else if ("blockquote".equals(name))
                    qlevel--;

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
                trimEnd(sb);
                sb.append("\n");
                for (int i = 0; i < qlevel; i++)
                    sb.append('>');
                if (qlevel > 0)
                    sb.append(' ');
            }
        }, Jsoup.parse(html));

        trimEnd(sb);
        sb.append("\n");

        return sb.toString();
    }

    static void trimEnd(StringBuilder sb) {
        int length = sb.length();
        while (length > 0 && sb.charAt(length - 1) == ' ')
            length--;
        sb.setLength(length);
    }

    static Spanned fromHtml(@NonNull String html) {
        return fromHtml(html, null, null);
    }

    static Spanned fromHtml(@NonNull String html, @Nullable Html.ImageGetter imageGetter, @Nullable Html.TagHandler tagHandler) {
        return HtmlCompat.fromHtml(html, FROM_HTML_SEPARATOR_LINE_BREAK_LIST_ITEM, imageGetter, tagHandler);
    }

    static String toHtml(Spanned spanned) {
        return HtmlCompat.toHtml(spanned, TO_HTML_PARAGRAPH_LINES_CONSECUTIVE);
    }
}
