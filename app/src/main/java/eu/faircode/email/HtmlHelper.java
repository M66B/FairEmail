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
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.text.Html;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.BulletSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.QuoteSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.SubscriptSpan;
import android.text.style.SuperscriptSpan;
import android.text.style.TypefaceSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.util.Base64;
import android.view.textclassifier.TextClassificationManager;
import android.view.textclassifier.TextLanguage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.ColorUtils;
import androidx.core.text.HtmlCompat;
import androidx.core.util.PatternsCompat;
import androidx.preference.PreferenceManager;

import com.steadystate.css.dom.CSSMediaRuleImpl;
import com.steadystate.css.dom.CSSStyleRuleImpl;
import com.steadystate.css.dom.MediaListImpl;
import com.steadystate.css.parser.CSSOMParser;
import com.steadystate.css.parser.SACParserCSS3;
import com.steadystate.css.parser.selectors.ClassConditionImpl;
import com.steadystate.css.parser.selectors.ConditionalSelectorImpl;
import com.steadystate.css.parser.selectors.ElementSelectorImpl;

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
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.ErrorHandler;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.Selector;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleSheet;
import org.w3c.dom.stylesheets.MediaList;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static androidx.core.text.HtmlCompat.FROM_HTML_SEPARATOR_LINE_BREAK_LIST_ITEM;
import static androidx.core.text.HtmlCompat.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE;
import static org.w3c.css.sac.Condition.SAC_CLASS_CONDITION;

public class HtmlHelper {
    private static final int PREVIEW_SIZE = 500; // characters

    private static final int DEFAULT_FONT_SIZE = 16; // pixels
    private static final int DEFAULT_FONT_SIZE_PT = 12; // points
    private static final float FONT_SMALL = 0.8f;
    private static final float FONT_LARGE = 1.25f;
    private static final float MIN_LUMINANCE = 0.5f;
    private static final int TAB_SIZE = 2;
    private static final int MAX_AUTO_LINK = 250;
    private static final int MAX_FORMAT_TEXT_SIZE = 50 * 1024; // characters
    private static final int MAX_FULL_TEXT_SIZE = 1024 * 1024; // characters
    private static final int TRACKING_PIXEL_SURFACE = 25; // pixels
    private static final float[] HEADING_SIZES = {1.5f, 1.4f, 1.3f, 1.2f, 1.1f, 1f};
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

    static Document sanitizeCompose(Context context, String html, boolean show_images) {
        try {
            Document parsed = JsoupEx.parse(html);
            return sanitize(context, parsed, false, show_images);
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

    static Document sanitizeView(Context context, Document parsed, boolean show_images) {
        try {
            return sanitize(context, parsed, true, show_images);
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

    private static Document sanitize(Context context, Document parsed, boolean view, boolean show_images) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean text_color = prefs.getBoolean("text_color", true);
        boolean text_size = prefs.getBoolean("text_size", true);
        boolean display_hidden = prefs.getBoolean("display_hidden", false);
        boolean disable_tracking = prefs.getBoolean("disable_tracking", true);
        boolean parse_classes = prefs.getBoolean("parse_classes", false);

        // https://chromium.googlesource.com/chromium/blink/+/master/Source/core/css/html.css

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

        // Limit length
        if (view && truncate(parsed, true)) {
            parsed.body()
                    .appendElement("br")
                    .appendElement("p")
                    .appendElement("em")
                    .text(context.getString(R.string.title_too_large));
            parsed.body()
                    .appendElement("p")
                    .appendElement("big")
                    .appendElement("a")
                    .attr("href", "full:")
                    .text(context.getString(R.string.title_show_full));
        }

        // https://developer.mozilla.org/en-US/docs/Web/HTML/Element/style
        List<CSSStyleSheet> sheets = new ArrayList<>();
        if (parse_classes)
            for (Element style : parsed.head().select("style")) {
                if (BuildConfig.DEBUG)
                    Log.i("Style=" + style.data());
                try {
                    InputSource source = new InputSource(new StringReader(style.data()));
                    String media = style.attr("media");
                    if (!TextUtils.isEmpty(media))
                        source.setMedia(media);

                    CSSOMParser parser = new CSSOMParser(new SACParserCSS3());
                    parser.setErrorHandler(new ErrorHandler() {
                        @Override
                        public void warning(CSSParseException ex) throws CSSException {
                            Log.i("CSS warning=" + ex.getMessage());
                        }

                        @Override
                        public void error(CSSParseException ex) throws CSSException {
                            Log.i("CSS error=" + ex.getMessage());
                        }

                        @Override
                        public void fatalError(CSSParseException ex) throws CSSException {
                            Log.w(ex);
                        }
                    });

                    long start = new Date().getTime();
                    sheets.add(parser.parseStyleSheet(source, null, null));
                    long elapsed = new Date().getTime() - start;
                    Log.i("Style parse=" + elapsed + " ms");
                } catch (Throwable ex) {
                    Log.w(ex);
                }
            }

        Whitelist whitelist = Whitelist.relaxed()
                .addTags("hr", "abbr", "big", "font", "dfn", "del", "s", "tt")
                .addAttributes(":all", "class")
                .addAttributes(":all", "style")
                .addAttributes("font", "size")
                .removeTags("col", "colgroup", "thead", "tbody")
                .removeAttributes("table", "width")
                .removeAttributes("td", "colspan", "rowspan", "width")
                .removeAttributes("th", "colspan", "rowspan", "width")
                .addProtocols("img", "src", "cid")
                .addProtocols("img", "src", "data")
                .addProtocols("a", "href", "full");
        if (text_color)
            whitelist.addAttributes("font", "color");
        if (!view)
            whitelist.addProtocols("img", "src", "content");

        final Document document = new Cleaner(whitelist).clean(parsed);

        boolean dark = Helper.isDarkTheme(context);

        // Font
        for (Element font : document.select("font")) {
            // https://developer.mozilla.org/en-US/docs/Web/HTML/Element/font
            String style = font.attr("style");
            String color = font.attr("color");
            String size = font.attr("size");

            style = style.trim();
            if (!TextUtils.isEmpty(style) && !style.endsWith(";"))
                style += ";";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                font.removeAttr("color");
            font.removeAttr("size");

            StringBuilder sb = new StringBuilder(style);

            if (!TextUtils.isEmpty(color))
                sb.append("color:").append(color).append(";");

            if (!TextUtils.isEmpty(size)) {
                try {
                    int s = Integer.parseInt(size);
                    if (s < 3)
                        size = "small";
                    else if (s > 3)
                        size = "large";
                    else
                        size = "medium";
                    sb.append("font-size:").append(size).append(";");
                } catch (NumberFormatException ex) {
                    Log.w(ex);
                }
            }

            font.attr("style", sb.toString());

            font.tagName("span");
        }

        // Sanitize styles
        for (Element element : document.select("*")) {
            String style = null;
            String clazz = element.attr("class");

            // Class style
            style = processStyles(element.tagName(), clazz, style, sheets);

            // Element style
            style = mergeStyles(style, element.attr("style"));

            // Process style
            if (!TextUtils.isEmpty(style)) {
                StringBuilder sb = new StringBuilder();

                String[] params = style.split(";");
                for (String param : params) {
                    int colon = param.indexOf(':');
                    if (colon > 0) {
                        String key = param.substring(0, colon).trim().toLowerCase(Locale.ROOT);
                        String value = param.substring(colon + 1).toLowerCase(Locale.ROOT)
                                .replace("!important", "")
                                .trim()
                                .replaceAll("\\s+", " ");
                        switch (key) {
                            case "color":
                                // https://developer.mozilla.org/en-US/docs/Web/CSS/color
                                if (!text_color)
                                    continue;

                                Integer color = parseColor(value, dark);
                                if (color != null) {
                                    // fromHtml does not support transparency
                                    String c = String.format("#%06x", color);
                                    sb.append("color:").append(c).append(";");
                                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
                                        element.attr("color", c);
                                }

                                break;

                            case "font-size":
                                // https://developer.mozilla.org/en-US/docs/Web/CSS/font-size
                                if (!text_size)
                                    continue;

                                float current = 1.0f;
                                Element parent = element.parent();
                                while (parent != null) {
                                    String xFontSize = parent.attr("x-font-size");
                                    if (!TextUtils.isEmpty(xFontSize)) {
                                        current = Float.parseFloat(xFontSize);
                                        break;
                                    }
                                    parent = parent.parent();
                                }

                                Float fsize = getFontSize(value, current);
                                if (fsize != null && fsize != 0) {
                                    element.attr("x-font-size", Float.toString(fsize));
                                    element.attr("x-font-size-rel", Float.toString(fsize / current));
                                }
                                break;

                            case "font-weight":
                                if (element.parent() != null) {
                                    Integer fweight = getFontWeight(value);
                                    if (fweight != null && fweight >= 600) {
                                        Element strong = new Element("strong");
                                        element.replaceWith(strong);
                                        strong.appendChild(element);
                                    }
                                }
                                break;

                            case "text-decoration":
                                // https://developer.mozilla.org/en-US/docs/Web/CSS/text-decoration
                                if (value.contains("line-through"))
                                    sb.append("text-decoration:line-through;");
                                break;

                            case "display":
                                // https://developer.mozilla.org/en-US/docs/Web/CSS/display
                                if (element.parent() != null &&
                                        !display_hidden && "none".equals(value)) {
                                    Log.i("Removing display none " + element.tagName());
                                    element.remove();
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
                                if (element.parent() != null && !display_hidden) {
                                    Float s = getFontSize(value, 1.0f);
                                    if (s != null && s == 0) {
                                        Log.i("Removing no height/width " + element.tagName());
                                        element.remove();
                                    }
                                }
                                break;

                            case "margin":
                            case "padding":
                            case "margin-top":
                            case "margin-bottom":
                            case "padding-top":
                            case "padding-bottom":
                                // https://developer.mozilla.org/en-US/docs/Web/CSS/margin
                                // https://developer.mozilla.org/en-US/docs/Web/CSS/padding
                                if (element.isBlock() && hasVisibleContent(element.childNodes())) {
                                    Float[] p = new Float[4];

                                    String[] v = value.split(" ");
                                    for (int i = 0; i < v.length; i++)
                                        p[i] = getFontSize(v[i], 1.0f);

                                    if (v.length == 1) {
                                        p[1] = p[0];
                                        p[2] = p[0];
                                        p[3] = p[0];
                                    } else if (v.length == 2) {
                                        p[2] = p[0];
                                        p[3] = p[1];
                                    }

                                    if (key.endsWith("top"))
                                        p[2] = null;
                                    else if (key.endsWith("bottom"))
                                        p[0] = null;

                                    if (p[0] != null && p[0] > 0.5)
                                        element.attr("line-before", "true");
                                    if (p[2] != null && p[2] > 0.5)
                                        element.attr("line-after", "true");
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

        // Remove trailing br from div
        for (Element div : document.select("div"))
            if (div.children().select("div").size() == 0 &&
                    hasVisibleContent(div.childNodes())) {
                Node last = div.childNode(div.childNodeSize() - 1);
                if (last != null && "br".equals(last.nodeName()))
                    last.remove();
            }

        // Replace headings
        if (!text_size)
            for (Element h : document.select("h1,h2,h3,h4,h5,h6")) {
                h.appendElement("br");
                h.appendElement("br");
                h.tagName("strong");
            }

        // Paragraphs
        for (Element p : document.select("p")) {
            p.appendElement("br");
            p.tagName("div");
        }

        // Short inline quotes
        // https://developer.mozilla.org/en-US/docs/Web/HTML/Element/q
        for (Element q : document.select("q")) {
            q.tagName("a");
            q.attr("href", q.attr("cite"));
            q.removeAttr("cite");
        }

        // Citation
        // https://developer.mozilla.org/en-US/docs/Web/HTML/Element/cite
        for (Element cite : document.select("cite")) {
            cite.prependText("\"");
            cite.appendText("\"");
            cite.tagName("em");
        }

        // Definition
        // https://developer.mozilla.org/en-US/docs/Web/HTML/Element/dfn
        for (Element dfn : document.select("dfn"))
            dfn.tagName("em");

        // Pre formatted text
        // https://developer.mozilla.org/en-US/docs/Web/HTML/Element/pre
        for (Element pre : document.select("pre")) {
            pre.html(formatPre(pre.wholeText()));
            pre.tagName("div");
        }

        // Code
        // https://developer.mozilla.org/en-US/docs/Web/HTML/Element/code
        document.select("code").tagName("strong");

        // Lines
        // https://developer.mozilla.org/en-US/docs/Web/HTML/Element/hr
        for (Element hr : document.select("hr")) {
            hr.tagName("div");
            hr.text("----------------------------------------");
        }

        // Descriptions
        // https://developer.mozilla.org/en-US/docs/Web/HTML/Element/dl
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
        // https://developer.mozilla.org/en-US/docs/Web/HTML/Element/abbr
        document.select("abbr").tagName("u");

        // Subscript/Superscript
        // https://developer.mozilla.org/en-US/docs/Web/HTML/Element/sub
        // https://developer.mozilla.org/en-US/docs/Web/HTML/Element/sup
        if (!view)
            for (Element subp : document.select("sub,sup"))
                subp.tagName("small");

        // Lists
        // https://developer.mozilla.org/en-US/docs/Web/HTML/Element/li
        if (!view) {
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
        }

        // Tables
        // https://developer.mozilla.org/en-US/docs/Web/HTML/Element/table
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
        for (Element hf : document.select("thead,tfoot"))
            hf.tagName("span");

        // Remove tracking pixels
        if (disable_tracking)
            removeTrackingPixels(context, document);

        // Images
        // https://developer.mozilla.org/en-US/docs/Web/HTML/Element/img
        for (Element img : document.select("img")) {
            String alt = img.attr("alt");
            String src = img.attr("src");
            String tracking = img.attr("tracking");

            if (!show_images && !TextUtils.isEmpty(alt))
                if (TextUtils.isEmpty(tracking))
                    img.appendText("[" + alt + "]");
                else {
                    Element a = document.createElement("a");
                    a.attr("href", tracking);
                    a.text("[" + alt + "]");
                    img.appendChild(a);
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
        if (view) {
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

        for (Element e : document.select("*[line-before],*[line-after]")) {
            if (!TextUtils.isEmpty(e.attr("line-before"))) {
                Element prev = e.previousElementSibling();
                if (prev == null || !"br".equals(prev.tagName()))
                    e.prependElement("br");
            }

            if (!TextUtils.isEmpty(e.attr("line-after"))) {
                Element next = e.nextElementSibling();
                if (next == null || !"br".equals(next.tagName()))
                    e.appendElement("br");
            }
        }

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

    private static String processStyles(String tag, String clazz, String style, List<CSSStyleSheet> sheets) {
        for (CSSStyleSheet sheet : sheets)
            if (isScreenMedia(sheet.getMedia())) {
                style = processStyles(null, clazz, style, sheet.getCssRules(), Selector.SAC_ELEMENT_NODE_SELECTOR);
                style = processStyles(tag, clazz, style, sheet.getCssRules(), Selector.SAC_ELEMENT_NODE_SELECTOR);
                style = processStyles(tag, clazz, style, sheet.getCssRules(), Selector.SAC_CONDITIONAL_SELECTOR);
            }
        return style;
    }

    private static String processStyles(String tag, String clazz, String style, CSSRuleList rules, int stype) {
        for (int i = 0; rules != null && i < rules.getLength(); i++) {
            CSSRule rule = rules.item(i);
            switch (rule.getType()) {
                case CSSRule.STYLE_RULE:
                    CSSStyleRuleImpl srule = (CSSStyleRuleImpl) rule;
                    for (int j = 0; j < srule.getSelectors().getLength(); j++) {
                        Selector selector = srule.getSelectors().item(j);
                        if (selector.getSelectorType() != stype)
                            continue;
                        switch (selector.getSelectorType()) {
                            case Selector.SAC_ELEMENT_NODE_SELECTOR:
                                ElementSelectorImpl eselector = (ElementSelectorImpl) selector;
                                if (tag == null
                                        ? eselector.getLocalName() == null
                                        : tag.equals(eselector.getLocalName()))
                                    style = mergeStyles(style, srule.getStyle().getCssText());
                                break;
                            case Selector.SAC_CONDITIONAL_SELECTOR:
                                ConditionalSelectorImpl cselector = (ConditionalSelectorImpl) selector;
                                if (cselector.getCondition().getConditionType() == SAC_CLASS_CONDITION) {
                                    ClassConditionImpl ccondition = (ClassConditionImpl) cselector.getCondition();
                                    if (clazz.equals(ccondition.getValue()))
                                        style = mergeStyles(style, srule.getStyle().getCssText());
                                }
                                break;
                        }
                    }
                    break;

                case CSSRule.MEDIA_RULE:
                    CSSMediaRuleImpl mrule = (CSSMediaRuleImpl) rule;
                    if (isScreenMedia(mrule.getMedia()))
                        style = processStyles(tag, clazz, style, mrule.getCssRules(), stype);
                    break;
            }
        }
        return style;
    }

    private static boolean isScreenMedia(MediaList media) {
        // https://developer.mozilla.org/en-US/docs/Web/CSS/Media_Queries/Using_media_queries
        // https://developers.google.com/gmail/design/reference/supported_css#supported_types
        if (media instanceof MediaListImpl) {
            MediaListImpl _media = (MediaListImpl) media;
            for (int i = 0; i < _media.getLength(); i++) {
                String query = _media.mediaQuery(i).getCssText(null);
                if ("all".equals(query) ||
                        "screen".equals(query) || "only screen".equals(query))
                    return true;
            }
        } else
            Log.e("Media class=" + media.getClass().getName());
        return false;
    }

    private static String mergeStyles(String base, String style) {
        return mergeStyles(base, style, null);
    }

    private static String mergeStyles(String base, String style, String selector) {
        Map<String, String> result = new HashMap<>();

        List<String> params = new ArrayList<>();
        if (!TextUtils.isEmpty(base))
            params.addAll(Arrays.asList(base.split(";")));
        if (!TextUtils.isEmpty(style))
            params.addAll(Arrays.asList(style.split(";")));

        for (String param : params) {
            int colon = param.indexOf(':');
            if (colon > 0) {
                String key = param.substring(0, colon).trim().toLowerCase(Locale.ROOT);
                if (selector == null || selector.equals(key))
                    result.put(key, param);
            } else
                Log.w("Invalid style param=" + param);
        }

        return TextUtils.join(";", result.values());
    }

    private static Integer getFontWeight(String value) {
        // https://developer.mozilla.org/en-US/docs/Web/CSS/font-weight
        if (TextUtils.isEmpty(value))
            return null;

        value = value.toLowerCase(Locale.ROOT).trim();

        switch (value) {
            case "thin":
                return 100;
            case "light":
            case "lighter":
                return 300;
            case "normal":
            case "regular":
                return 400;
            case "bolder":
            case "strong":
                return 600;
            case "bold":
                return 700;
            case "heavy":
                return 900;
            case "none":
            case "auto":
            case "initial":
            case "inherit":
                return null;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            Log.w(ex);
        }

        return null;
    }

    private static Float getFontSize(String value, float current) {
        // https://developer.mozilla.org/en-US/docs/Web/CSS/font-size
        if (TextUtils.isEmpty(value))
            return null;

        if (value.contains("calc") ||
                "none".equals(value) ||
                "auto".equals(value) ||
                "initial".equals(value) ||
                "inherit".equals(value))
            return null;

        // Absolute
        switch (value) {
            case "xx-small":
            case "x-small":
            case "small":
                return FONT_SMALL;
            case "medium":
                return 1.0f;
            case "large":
            case "x-large":
            case "xx-large":
            case "xxx-large":
                return FONT_LARGE;
        }

        // Relative
        switch (value) {
            case "smaller":
                return FONT_SMALL * current;
            case "larger":
                return FONT_LARGE * current;
        }

        try {
            if (value.endsWith("%"))
                return Float.parseFloat(value.substring(0, value.length() - 1).trim()) / 100 * current;
            if (value.endsWith("em"))
                return Float.parseFloat(value.substring(0, value.length() - 2).trim()) * current;
            if (value.endsWith("rem"))
                return Float.parseFloat(value.substring(0, value.length() - 3).trim());
            if (value.endsWith("pt"))
                return Float.parseFloat(value.substring(0, value.length() - 2).trim()) / DEFAULT_FONT_SIZE_PT;
            if (value.endsWith("px"))
                return Float.parseFloat(value.substring(0, value.length() - 2).trim()) / DEFAULT_FONT_SIZE;
            if (value.endsWith("cm") || value.endsWith("in"))
                return null;
            return Float.parseFloat(value.trim()) / DEFAULT_FONT_SIZE;
        } catch (NumberFormatException ex) {
            Log.i(ex);
            return null;
        }
    }

    private static Integer parseColor(@NonNull String value, boolean dark) {
        // https://developer.mozilla.org/en-US/docs/Web/CSS/color_value
        String c = value
                .replace("null", "")
                .replace("none", "")
                .replace("unset", "")
                .replace("auto", "")
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
                        color = Long.decode(c).intValue();
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
                    color = Long.decode("#" + c).intValue();
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

    // https://tools.ietf.org/html/rfc3676
    static String flow(String text) {
        boolean continuation = false;
        StringBuilder flowed = new StringBuilder();
        for (String line : text.split("\\r?\\n")) {
            if (continuation)
                while (line.startsWith(">")) {
                    line = line.substring(1);
                    if (line.startsWith(" "))
                        line = line.substring(1);
                }

            continuation = (line.endsWith(" ") && !"-- ".equals(line));

            flowed.append(line);
            if (!continuation)
                flowed.append("\r\n");
        }
        return flowed.toString();
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
                    l.append('\u00A0');
                    while (l.length() % TAB_SIZE != 0)
                        l.append('\u00A0');
                } else
                    l.append(kar);
            }
            line = l.toString();

            // Html characters
            // This will handle spaces / word wrapping as well
            line = Html.escapeHtml(line);

            sb.append(line);
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
                if (host != null && !hosts.contains(host)) {
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

    static void embedInlineImages(Context context, long id, Document document, boolean local) throws IOException {
        DB db = DB.getInstance(context);
        for (Element img : document.select("img")) {
            String src = img.attr("src");
            if (src.startsWith("cid:")) {
                String cid = '<' + src.substring(4) + '>';
                EntityAttachment attachment = db.attachment().getAttachment(id, cid);
                if (attachment != null && attachment.available) {
                    File file = attachment.getFile(context);
                    if (local) {
                        Uri uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID, file);
                        img.attr("src", uri.toString());
                        Log.i("Inline image uri=" + uri);
                    } else {
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
    }

    static void setViewport(Document document) {
        // https://developer.mozilla.org/en-US/docs/Mozilla/Mobile/Viewport_meta_tag
        document.head().select("meta").select("[name=viewport]").remove();

        document.head().prependChild(document.createElement("meta")
                .attr("name", "viewport")
                .attr("content", "width=device-width, initial-scale=1.0"));

        Log.i(document.head().html());
    }

    static String getLanguage(Context context, String body) {
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            boolean language_detection = prefs.getBoolean("language_detection", false);
            if (!language_detection)
                return null;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                TextClassificationManager tcm =
                        (TextClassificationManager) context.getSystemService(Context.TEXT_CLASSIFICATION_SERVICE);
                if (tcm == null)
                    return null;

                String text = getPreview(body);
                if (body == null)
                    return null;

                TextLanguage.Request trequest = new TextLanguage.Request.Builder(text).build();
                TextLanguage tlanguage = tcm.getTextClassifier().detectLanguage(trequest);
                if (tlanguage.getLocaleHypothesisCount() > 0)
                    return tlanguage.getLocale(0).toLocale().getLanguage();
            }

            return null;
        } catch (Throwable ex) {
            Log.e(ex);
            return null;
        }
    }

    static String getPreview(String body) {
        try {
            if (body == null)
                return null;
            Document d = JsoupEx.parse(body);
            return _getText(d, false);
        } catch (OutOfMemoryError ex) {
            Log.e(ex);
            return null;
        }
    }

    @Deprecated
    static String getFullText(String body) {
        try {
            if (body == null)
                return null;
            Document d = JsoupEx.parse(body);
            return _getText(d, true);
        } catch (OutOfMemoryError ex) {
            Log.e(ex);
            return null;
        }
    }

    static String getFullText(File file) throws IOException {
        try {
            Document d = JsoupEx.parse(file);
            return _getText(d, true);
        } catch (OutOfMemoryError ex) {
            Log.e(ex);
            return null;
        }
    }

    private static String _getText(Document d, boolean full) {
        truncate(d, !full);

        for (Element bq : d.select("blockquote")) {
            bq.prependChild(new TextNode("["));
            bq.appendChild(new TextNode("]"));
        }

        String text = d.text();
        if (full)
            return text;

        String preview = text.substring(0, Math.min(text.length(), PREVIEW_SIZE));
        if (preview.length() < text.length())
            preview += "…";

        return preview;
    }

    static String getText(Context context, String html) {
        final StringBuilder sb = new StringBuilder();

        Document d = JsoupEx.parse(html);

        truncate(d, true);

        SpannableStringBuilder ssb = fromDocument(context, d, false, null, null);

        for (URLSpan span : ssb.getSpans(0, ssb.length(), URLSpan.class)) {
            String url = span.getURL();
            if (TextUtils.isEmpty(url))
                continue;
            if (url.toLowerCase(Locale.ROOT).startsWith("mailto:"))
                url = url.substring("mailto:".length());
            int start = ssb.getSpanStart(span);
            int end = ssb.getSpanEnd(span);
            String text = ssb.subSequence(start, end).toString();
            if (!text.contains(url))
                ssb.insert(end, "[" + url + "]");
        }

        for (ImageSpan span : ssb.getSpans(0, ssb.length(), ImageSpan.class)) {
            String source = span.getSource();
            if (TextUtils.isEmpty(source))
                continue;
            int start = ssb.getSpanStart(span);
            int end = ssb.getSpanEnd(span);
            for (int i = start; i < end; i++)
                if (ssb.charAt(i) == '\uFFFC')
                    ssb.replace(i, i + 1, " ");
            ssb.insert(end, "[" + source + "]");
        }

        for (QuoteSpan span : ssb.getSpans(0, ssb.length(), QuoteSpan.class)) {
            int start = ssb.getSpanStart(span);
            int end = ssb.getSpanEnd(span);
            for (int i = end - 1; i >= start; i--)
                if (ssb.charAt(i) == '\n')
                    ssb.insert(i + 1, "> ");
            if (ssb.charAt(start) != '\n')
                ssb.insert(start, "> ");
        }

        for (BulletSpan span : ssb.getSpans(0, ssb.length(), BulletSpan.class)) {
            int start = ssb.getSpanStart(span);
            ssb.insert(start, "* ");
        }

        for (NumberSpan span : ssb.getSpans(0, ssb.length(), NumberSpan.class)) {
            int start = ssb.getSpanStart(span);
            ssb.insert(start, "- ");
        }

        return ssb.toString();
    }

    static void convertLists(Document document) {
        for (Element span : document.select("span")) {
            // Skip signature and referenced message
            boolean body = true;
            Element parent = span.parent();
            while (parent != null) {
                if ("div".equals(parent.tagName()) &&
                        !TextUtils.isEmpty(parent.attr("fairemail"))) {
                    body = false;
                    break;
                }
                parent = parent.parent();
            }
            if (!body)
                continue;

            Element list = null;
            for (int i = 0; i < span.childNodeSize(); i++) {
                boolean item = false;
                Node node = span.childNode(i);
                if (node instanceof TextNode) {
                    String text = ((TextNode) node).text().trim();
                    Node next = node.nextSibling();
                    if ((text.startsWith("* ") || text.startsWith("- ")) &&
                            (next == null || "br".equals(next.nodeName()))) {
                        item = true;
                        String type = (text.startsWith("* ") ? "ul" : "ol");

                        Element li = document.createElement("li");
                        li.text(text.substring(2));

                        if (list == null || !list.tagName().equals(type)) {
                            Node before = node.previousSibling();
                            if (before != null && "br".equals(before.nodeName())) {
                                before.remove();
                                i--;
                            }

                            list = document.createElement(type);
                            list.appendChild(li);
                            node.replaceWith(list);

                        } else {
                            list.appendChild(li);
                            node.remove();
                            i--;
                        }

                        if (next != null)
                            next.remove();
                    }
                } else {
                    if (list != null && "br".equals(node.nodeName())) {
                        node.remove();
                        i--;
                    }
                }
                if (!item)
                    list = null;
            }
        }
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

    static void cleanup(Document d) {
        // https://www.chromestatus.com/feature/5756335865987072
        // Some messages contain 100 thousands of Apple spaces
        for (Element aspace : d.select(".Apple-converted-space")) {
            Node next = aspace.nextSibling();
            if (next instanceof TextNode) {
                TextNode tnode = (TextNode) next;
                tnode.text(" " + tnode.text());
                aspace.remove();
            } else
                aspace.replaceWith(new TextNode(" "));
        }
    }

    static boolean truncate(Document d, boolean reformat) {
        int max = (reformat ? MAX_FORMAT_TEXT_SIZE : MAX_FULL_TEXT_SIZE);

        int length = 0;
        int images = 0;
        for (Element elm : d.select("*")) {
            if ("img".equals(elm.tagName()))
                images++;

            boolean skip = false;
            for (Node child : elm.childNodes()) {
                if (child instanceof TextNode) {
                    TextNode tnode = ((TextNode) child);
                    String text = tnode.getWholeText();

                    if (length < max) {
                        if (length + text.length() >= max) {
                            text = text.substring(0, max - length) + " ...";
                            tnode.text(text);
                            skip = true;
                        }
                    } else {
                        if (skip)
                            tnode.text("");
                    }

                    length += text.length();
                }
            }

            if (length >= max && !skip)
                elm.remove();
        }

        Log.i("Message size=" + length + " images=" + images);

        return (length >= max);
    }

    static boolean contains(Document d, String[] texts) {
        Map<String, Boolean> condition = new HashMap<>();
        for (String t : texts)
            condition.put(t, false);

        for (Element elm : d.select("*"))
            for (Node child : elm.childNodes()) {
                if (child instanceof TextNode) {
                    TextNode tnode = ((TextNode) child);
                    String text = tnode.getWholeText();
                    for (String t : texts)
                        if (!condition.get(t) && text.contains(t)) {
                            condition.put(t, true);

                            boolean found = true;
                            for (String c : texts)
                                if (!condition.get(c)) {
                                    found = false;
                                    break;
                                }

                            if (found)
                                return true;
                        }
                }
            }

        return false;
    }

    static SpannableStringBuilder fromDocument(Context context, @NonNull Document document) {
        return fromDocument(context, document, null, null);
    }

    static SpannableStringBuilder fromDocument(Context context, @NonNull Document document, @Nullable Html.ImageGetter imageGetter, @Nullable Html.TagHandler tagHandler) {
        return fromDocument(context, document, true, imageGetter, tagHandler);
    }

    private static SpannableStringBuilder fromDocument(Context context, @NonNull Document document, final boolean warn, @Nullable Html.ImageGetter imageGetter, @Nullable Html.TagHandler tagHandler) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean debug = prefs.getBoolean("debug", false);

        int colorAccent = Helper.resolveColor(context, R.attr.colorAccent);
        int dp3 = Helper.dp2pixels(context, 3);
        int dp6 = Helper.dp2pixels(context, 6);
        int dp24 = Helper.dp2pixels(context, 24);

        // https://developer.mozilla.org/en-US/docs/Web/HTML/Block-level_elements
        NodeTraversor.traverse(new NodeVisitor() {
            private Element element;
            private List<TextNode> block = new ArrayList<>();

            private String WHITESPACE = " \t\f";
            private String WHITESPACE_NL = WHITESPACE + "\r\n";
            private Pattern TRIM_WHITESPACE_NL =
                    Pattern.compile("[" + WHITESPACE + "]*\\r?\\n[" + WHITESPACE + "]*");

            private List<String> BLOCK_START = Collections.unmodifiableList(Arrays.asList(
                    "body", "blockquote", "h1", "h2", "h3", "h4", "h5", "h6", "li", "ol", "ul"
            ));
            private List<String> BLOCK_END = Collections.unmodifiableList(Arrays.asList(
                    "body", "blockquote", "br", "h1", "h2", "h3", "h4", "h5", "h6", "li", "ol", "ul"
            ));

            @Override
            public void head(Node node, int depth) {
                if (node instanceof TextNode)
                    block.add((TextNode) node);
                else if (node instanceof Element) {
                    element = (Element) node;
                    if (BLOCK_START.contains(element.tagName())) {
                        normalizeText(block);
                        block.clear();
                    }
                }
            }

            @Override
            public void tail(Node node, int depth) {
                if (node instanceof Element) {
                    element = (Element) node;
                    if (BLOCK_END.contains(element.tagName())) {
                        normalizeText(block);
                        block.clear();
                    }
                }
            }

            private void normalizeText(List<TextNode> block) {
                // https://developer.mozilla.org/en-US/docs/Web/API/Document_Object_Model/Whitespace
                TextNode tnode;
                String text;
                for (int i = 0; i < block.size(); ) {
                    tnode = block.get(i);
                    text = tnode.getWholeText();

                    if ("-- ".equals(text)) {
                        i++;
                        continue;
                    }

                    // Remove whitespace before/after newlines
                    TRIM_WHITESPACE_NL.matcher(text).replaceAll(" ");

                    if (i == 0 || endsWithWhitespace(block.get(i - 1).text()))
                        while (startsWithWhiteSpace(text))
                            text = text.substring(1);

                    if (i == block.size() - 1)
                        while (endsWithWhitespace(text))
                            text = text.substring(0, text.length() - 1);

                    tnode.text(text);

                    if (TextUtils.isEmpty(text))
                        block.remove(i);
                    else
                        i++;
                }

                if (debug) {
                    if (block.size() > 0) {
                        block.get(0).text("(" + block.get(0));
                        block.get(block.size() - 1).text(block.get(block.size() - 1) + ")");
                    }
                }
            }

            boolean startsWithWhiteSpace(String text) {
                int len = text.length();
                if (len == 0)
                    return false;
                return WHITESPACE_NL.contains(text.substring(0, 1));
            }

            boolean endsWithWhitespace(String text) {
                int len = text.length();
                if (len == 0)
                    return false;
                return WHITESPACE_NL.contains(text.substring(len - 1));
            }
        }, document.body());

        // https://developer.android.com/guide/topics/text/spans
        SpannableStringBuilder ssb = new SpannableStringBuilder();

        NodeTraversor.traverse(new NodeVisitor() {
            private Element element;
            private TextNode tnode;

            @Override
            public void head(Node node, int depth) {
                if (node instanceof Element) {
                    element = (Element) node;
                    element.attr("start-index", Integer.toString(ssb.length()));
                    if (debug)
                        ssb.append("[" + element.tagName() + ":" + element.attr("x-font-size-rel") + "]");
                } else if (node instanceof TextNode) {
                    tnode = (TextNode) node;
                    ssb.append(tnode.getWholeText());
                }
            }

            @Override
            public void tail(Node node, int depth) {
                if (node instanceof Element) {
                    element = (Element) node;
                    int start = Integer.parseInt(element.attr("start-index"));
                    if (debug)
                        ssb.append("[/" + element.tagName() + "]");

                    // Apply style
                    String style = element.attr("style");
                    if (!TextUtils.isEmpty(style)) {
                        String[] params = style.split(";");
                        for (String param : params) {
                            int semi = param.indexOf(":");
                            if (semi < 0)
                                continue;
                            String key = param.substring(0, semi);
                            String value = param.substring(semi + 1);
                            switch (key) {
                                case "color":
                                    try {
                                        int color = Integer.parseInt(value.substring(1), 16) | 0xFF000000;
                                        ssb.setSpan(new ForegroundColorSpan(color), start, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    } catch (NumberFormatException ex) {
                                        Log.w(ex);
                                    }
                                    break;
                                case "text-decoration":
                                    if ("line-through".equals(value))
                                        ssb.setSpan(new StrikethroughSpan(), start, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    break;
                            }
                        }
                    }

                    // Apply calculated font size
                    String xFontSize = element.attr("x-font-size-rel");
                    if (!TextUtils.isEmpty(xFontSize)) {
                        Float fsize = Float.parseFloat(xFontSize);
                        ssb.setSpan(new RelativeSizeSpan(fsize), start, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    // Apply element
                    switch (element.tagName()) {
                        case "a":
                            String href = element.attr("href");
                            if (!TextUtils.isEmpty(href))
                                ssb.setSpan(new URLSpan(href), start, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            break;
                        case "body":
                            // Do nothing
                            break;
                        case "big":
                            ssb.setSpan(new RelativeSizeSpan(FONT_LARGE), start, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            break;
                        case "blockquote":
                            if (start > 0 && ssb.charAt(start - 1) != '\n')
                                ssb.insert(start++, "\n");
                            ssb.setSpan(new QuoteSpan(), start, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            if (ssb.length() > 1 && ssb.charAt(ssb.length() - 1) != '\n')
                                ssb.append("\n");
                            break;
                        case "br":
                            newline(ssb.length());
                            break;
                        case "i":
                        case "em":
                            ssb.setSpan(new StyleSpan(Typeface.ITALIC), start, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            break;
                        case "font":
                            // Do nothing
                            break;
                        case "h1":
                        case "h2":
                        case "h3":
                        case "h4":
                        case "h5":
                        case "h6":
                            int level = element.tagName().charAt(1) - '1';
                            ssb.setSpan(new RelativeSizeSpan(HEADING_SIZES[level]), start, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            ssb.setSpan(new StyleSpan(Typeface.BOLD), start, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            newline(start);
                            newline(ssb.length());
                            break;
                        case "img":
                            String src = element.attr("src");
                            Drawable d = (imageGetter == null
                                    ? context.getDrawable(R.drawable.baseline_broken_image_24)
                                    : imageGetter.getDrawable(src));
                            ssb.insert(start, "\uFFFC"); // Object replacement character
                            ssb.setSpan(new ImageSpan(d, src), start, start + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            break;
                        case "li":
                            newline(ssb.length());
                            Element parent = element.parent();
                            if (parent == null || "ul".equals(parent.tagName()))
                                // TODO BulletSpanCompat
                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P)
                                    ssb.setSpan(new BulletSpan(dp6, colorAccent), start, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                else
                                    ssb.setSpan(new BulletSpan(dp6, colorAccent, dp3), start, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            else {
                                int index = 0;
                                for (Node child : parent.childNodes()) {
                                    if (child instanceof Element &&
                                            child.nodeName().equals(element.tagName())) {
                                        index++;
                                        if (child == element)
                                            break;
                                    }
                                }

                                float textSize = Helper.getTextSize(context, 0);
                                ssb.setSpan(new NumberSpan(dp6, colorAccent, textSize, index), start, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }
                            break;
                        case "ol":
                        case "ul":
                            int llevel = 0;
                            Element lparent = element.parent();
                            while (lparent != null) {
                                if (lparent.tagName().equals(element.tagName()))
                                    llevel++;
                                lparent = lparent.parent();
                            }
                            if (llevel > 0)
                                ssb.setSpan(new LeadingMarginSpan.Standard(llevel * dp24), start, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            newline(start);
                            newline(ssb.length());
                            break;
                        case "small":
                            ssb.setSpan(new RelativeSizeSpan(FONT_SMALL), start, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            break;
                        case "span":
                            // Do nothing
                            break;
                        case "sub":
                            ssb.setSpan(new SubscriptSpan(), start, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            ssb.setSpan(new RelativeSizeSpan(FONT_SMALL), start, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            break;
                        case "sup":
                            ssb.setSpan(new SuperscriptSpan(), start, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            ssb.setSpan(new RelativeSizeSpan(FONT_SMALL), start, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            break;
                        case "b":
                        case "strong":
                            ssb.setSpan(new StyleSpan(Typeface.BOLD), start, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            break;
                        case "s":
                        case "del":
                        case "strike":
                            ssb.setSpan(new StrikethroughSpan(), start, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            break;
                        case "tt":
                            ssb.setSpan(new TypefaceSpan("monospace"), start, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            break;
                        case "u":
                            ssb.setSpan(new UnderlineSpan(), start, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            break;
                        default:
                            if (warn)
                                Log.e("Unknown tag=" + element.tagName());
                    }
                }
            }

            private void newline(int index) {
                int len = ssb.length();
                if (len > 2 &&
                        ssb.charAt(len - 1) == '\n' &&
                        ssb.charAt(len - 2) == '\n')
                    return;
                ssb.insert(index, "\n");
            }
        }, document.body());

        if (debug)
            for (int i = ssb.length() - 1; i >= 0; i--)
                if (ssb.charAt(i) == '\n')
                    ssb.insert(i, "|");

        Object[] spans = ssb.getSpans(0, ssb.length(), Object.class);
        Map<Object, Integer> start = new HashMap<>();
        Map<Object, Integer> end = new HashMap<>();
        Map<Object, Integer> flags = new HashMap<>();
        for (Object span : spans) {
            start.put(span, ssb.getSpanStart(span));
            end.put(span, ssb.getSpanEnd(span));
            flags.put(span, ssb.getSpanFlags(span));
            ssb.removeSpan(span);
        }
        for (int i = spans.length - 1; i >= 0; --i)
            ssb.setSpan(spans[i], start.get(spans[i]), end.get(spans[i]), flags.get(spans[i]));

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

        return reverseSpans(spanned);
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

    private static Spanned reverseSpans(Spanned spanned) {
        Object[] spans = spanned.getSpans(0, spanned.length(), Object.class);
        Spannable reverse = Spannable.Factory.getInstance().newSpannable(spanned.toString());
        if (spans != null && spans.length > 0)
            for (int i = spans.length - 1; i >= 0; --i)
                reverse.setSpan(
                        spans[i],
                        spanned.getSpanStart(spans[i]),
                        spanned.getSpanEnd(spans[i]),
                        spanned.getSpanFlags(spans[i]));
        return reverse;
    }

    private static class NumberSpan implements LeadingMarginSpan {
        private TextPaint tp;
        private String number;
        private int margin;

        public NumberSpan(int gapWidth, int color, float textSize, int index) {
            tp = new TextPaint();
            tp.setStyle(Paint.Style.FILL);
            tp.setColor(color);
            tp.setTypeface(Typeface.MONOSPACE);
            tp.setTextSize(textSize);

            number = index + ".";
            margin = Math.round(tp.measureText(number) + gapWidth);
        }

        @Override
        public int getLeadingMargin(boolean first) {
            // https://issuetracker.google.com/issues/36956124
            // This is called before drawLeadingMargin to justify the text
            return margin;
        }

        @Override
        public void drawLeadingMargin(Canvas c, Paint p, int x, int dir, int top, int baseline, int bottom, CharSequence text, int start, int end, boolean first, Layout layout) {
            if (text instanceof Spanned &&
                    ((Spanned) text).getSpanStart(this) == start) {
                float textSize = tp.getTextSize();
                if (textSize > p.getTextSize())
                    tp.setTextSize(p.getTextSize());
                c.drawText(number, x + dir, baseline, tp);
                tp.setTextSize(textSize);
            }
        }
    }
}
