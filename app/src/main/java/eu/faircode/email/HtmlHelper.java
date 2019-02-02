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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Base64;

import org.jsoup.Jsoup;
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
import java.util.regex.Pattern;

public class HtmlHelper {
    private static final int PREVIEW_SIZE = 250;
    private static Pattern pattern = Pattern.compile("([http|https]+://[\\w\\S(\\.|:|/)]+)");
    private static final List<String> heads = Arrays.asList("p", "h1", "h2", "h3", "h4", "h5", "tr");
    private static final List<String> tails = Arrays.asList("br", "dd", "dt", "p", "h1", "h2", "h3", "h4", "h5");

    static String getBody(String html) {
        return Jsoup.parse(html).body().html();
    }

    static String sanitize(String html, boolean quotes) {
        Document document = Jsoup.parse(Jsoup.clean(html, Whitelist
                .relaxed()
                .addProtocols("img", "src", "cid")
                .addProtocols("img", "src", "data")));

        for (Element tr : document.select("tr"))
            tr.after("<br>");

        for (Element img : document.select("img")) {
            boolean linked = false;
            for (Element parent : img.parents())
                if ("a".equals(parent.tagName())) {
                    linked = true;
                    break;
                }
            if (!linked) {
                String src = img.attr("src");
                if (src.startsWith("http://") || src.startsWith("https://")) {
                    Element a = document.createElement("a");
                    a.attr("href", src);
                    img.replaceWith(a);
                    a.appendChild(img);
                }
            }
        }

        if (!quotes)
            for (Element quote : document.select("blockquote"))
                quote.text("&#8230;");

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

    static Drawable decodeImage(String source, Context context, long id, boolean show) {
        int px = Helper.dp2pixels(context, 48);

        if (TextUtils.isEmpty(source)) {
            Drawable d = context.getResources().getDrawable(R.drawable.baseline_broken_image_24, context.getTheme());
            d.setBounds(0, 0, px, px);
            return d;
        }

        boolean embedded = source.startsWith("cid:");
        boolean data = source.startsWith("data:");
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
                    d.setBounds(0, 0, bm.getWidth(), bm.getHeight());
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
            InputStream probe = null;
            BitmapFactory.Options options = new BitmapFactory.Options();
            try {
                Log.i("Probe " + source);
                probe = new URL(source).openStream();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(probe, null, options);
            } finally {
                if (probe != null)
                    probe.close();
            }

            Bitmap bm;
            InputStream is = null;
            try {
                Log.i("Download " + source);
                is = new URL(source).openStream();

                int scaleTo = context.getResources().getDisplayMetrics().widthPixels;
                int factor = Math.min(options.outWidth / scaleTo, options.outWidth / scaleTo);
                if (factor > 1) {
                    Log.i("Download image factor=" + factor);
                    options.inJustDecodeBounds = false;
                    options.inSampleSize = factor;
                    bm = BitmapFactory.decodeStream(is, null, options);
                } else
                    bm = BitmapFactory.decodeStream(is);
            } finally {
                if (is != null)
                    is.close();
            }

            if (bm == null)
                throw new FileNotFoundException("Download image failed");

            Log.i("Downloaded image");

            OutputStream os = null;
            try {
                os = new BufferedOutputStream(new FileOutputStream(file));
                bm.compress(Bitmap.CompressFormat.PNG, 100, os);
            } finally {
                if (os != null)
                    os.close();
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
            public void head(Node node, int depth) {
                if (node instanceof TextNode)
                    sb.append(((TextNode) node).text());
                else {
                    String name = node.nodeName();
                    if (name.equals("li"))
                        sb.append("\n * ");
                    else if (name.equals("dt"))
                        sb.append("  ");
                    else if (heads.contains(name))
                        sb.append("\n");
                }
            }

            public void tail(Node node, int depth) {
                String name = node.nodeName();
                if (tails.contains(name))
                    sb.append("\n");
                else if (name.equals("a"))
                    sb.append(" <").append(node.absUrl("href")).append(">");
            }
        }, Jsoup.parse(html));

        return sb.toString();
    }
}
