package eu.faircode.email;

import static org.w3c.css.sac.Condition.SAC_CLASS_CONDITION;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import androidx.core.graphics.ColorUtils;
import androidx.preference.PreferenceManager;

import com.steadystate.css.dom.CSSMediaRuleImpl;
import com.steadystate.css.dom.CSSStyleRuleImpl;
import com.steadystate.css.dom.MediaListImpl;
import com.steadystate.css.dom.Property;
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
import org.jsoup.safety.Safelist;
import org.jsoup.select.Elements;
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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HtmlSanitize {
    private static final int MAX_ALT = 250;
    private static final int SMALL_IMAGE_SIZE = 5; // pixels
    private static final float[] HEADING_SIZES = {1.5f, 1.4f, 1.3f, 1.2f, 1.1f, 1f};

    static Document sanitize(Context context, Document parsed, boolean view, boolean show_images) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String theme = prefs.getString("theme", "blue_orange_system");
        boolean bw = "black_and_white".equals(theme);
        boolean background_color = (!view || (!bw && prefs.getBoolean("background_color", false)));
        boolean text_color = (!view || (!bw && prefs.getBoolean("text_color", true)));
        boolean text_size = (!view || prefs.getBoolean("text_size", true));
        boolean text_font = (!view || prefs.getBoolean("text_font", true));
        boolean text_align = prefs.getBoolean("text_align", true);
        boolean text_titles = prefs.getBoolean("text_titles", false);
        boolean display_hidden = prefs.getBoolean("display_hidden", false);
        boolean disable_tracking = prefs.getBoolean("disable_tracking", true);
        boolean parse_classes = prefs.getBoolean("parse_classes", true);
        boolean inline_images = prefs.getBoolean("inline_images", false);
        boolean text_separators = prefs.getBoolean("text_separators", true);
        boolean image_placeholders = prefs.getBoolean("image_placeholders", true);

        boolean dark = Helper.isDarkTheme(context);
        int textColorPrimary = Helper.resolveColor(context, android.R.attr.textColorPrimary);
        int textColorPrimaryInverse = Helper.resolveColor(context, android.R.attr.textColorPrimaryInverse);

        int textSizeSmall;
        TypedArray ta = context.obtainStyledAttributes(
                androidx.appcompat.R.style.TextAppearance_AppCompat_Small, new int[]{android.R.attr.textSize});
        if (ta == null)
            textSizeSmall = Helper.dp2pixels(context, 6);
        else {
            textSizeSmall = ta.getDimensionPixelSize(0, 0);
            ta.recycle();
        }

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

        // Fix Microsoft namespaces
        HtmlHelper.normalizeNamespaces(parsed, display_hidden);

        // Limit length
        if (view && HtmlHelper.truncate(parsed, HtmlHelper.getMaxFormatTextSize(context))) {
            parsed.body()
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
            sheets = parseStyles(parsed.head().select("style"));

        Safelist safelist = Safelist.relaxed()
                .addTags("hr", "abbr", "big", "font", "dfn", "del", "s", "tt", "mark", "address")
                .addAttributes(":all", "class")
                .addAttributes(":all", "style")
                .addAttributes("span", "dir")
                .addAttributes("li", "dir")
                .addAttributes("div", "x-plain")
                .removeTags("col", "colgroup")
                .removeTags("thead", "tbody", "tfoot")
                .addAttributes("td", "width")
                .addAttributes("td", "height")
                .addAttributes("tr", "width")
                .addAttributes("tr", "height")
                .addAttributes(":all", "title")
                .addAttributes("blockquote", "type")
                .removeAttributes("td", "colspan", "rowspan", "width")
                .removeAttributes("th", "colspan", "rowspan", "width")
                .addProtocols("img", "src", "cid")
                .addProtocols("img", "src", "data")
                .removeTags("a").addAttributes("a", "href", "title");
        if (text_color)
            safelist.addAttributes("font", "color");
        if (text_size)
            safelist.addAttributes("font", "size");
        if (text_font)
            safelist.addAttributes("font", "face");
        if (text_align)
            safelist.addTags("center").addAttributes(":all", "align");
        if (!view)
            safelist.addProtocols("img", "src", "content");
        if (BuildConfig.DEBUG)
            safelist.addAttributes(":all", "x-computed");

        final Document document = new Cleaner(safelist).clean(parsed);

        if (BuildConfig.DEBUG)
            for (Element e : document.select("span:matchesOwn(^UUID: " + Helper.REGEX_UUID + ")")) {
                String t = e.text();
                int sp = t.indexOf(' ');
                if (sp < 0)
                    continue;
                String uuid = t.substring(sp + 1);
                e.html("UUID: <a href='" + BuildConfig.BUGSNAG_URI + uuid + "'>" + uuid + "</a>");
            }

        // Remove tracking pixels
        if (disable_tracking)
            HtmlHelper.removeTrackingPixels(context, document);

        // https://developer.mozilla.org/en-US/docs/Web/HTML/Element/font
        for (Element font : document.select("font")) {
            String style = font.attr("style");
            String color = font.attr("color").trim();
            String size = font.attr("size").trim();
            String face = font.attr("face").trim();

            style = style.trim();
            if (!TextUtils.isEmpty(style) && !style.endsWith(";"))
                style += ";";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                font.removeAttr("color");
            font.removeAttr("size");
            font.removeAttr("face");

            StringBuilder sb = new StringBuilder(style);

            if (!TextUtils.isEmpty(color))
                sb.append("color:").append(color).append(";");

            if (!TextUtils.isEmpty(size))
                try {
                    int s = Integer.parseInt(size);
                    if (size.startsWith("-")) {
                        if (s < 0)
                            size = "smaller";
                        else
                            throw new NumberFormatException("size=" + size);
                    } else if (size.startsWith("+")) {
                        if (s > 0)
                            size = "larger";
                        else
                            throw new NumberFormatException("size=" + size);
                    } else {
                        if (s < 2)
                            size = "x-small";
                        else if (s < 3)
                            size = "small";
                        else if (s > 4)
                            size = "x-large";
                        else if (s > 3)
                            size = "large";
                        else
                            size = "medium";
                    }
                    sb.append("font-size:").append(size).append(";");
                } catch (NumberFormatException ex) {
                    Log.i(ex);
                }

            if (!TextUtils.isEmpty(face)) {
                sb.append("font-family:");
                String[] faces = face.split(",");
                for (int i = 0; i < faces.length; i++) {
                    if (i > 0)
                        sb.append(',');
                    String f = faces[i].trim();
                    if (f.contains(" ") && !f.startsWith("\"") && !f.endsWith("\""))
                        sb.append('"').append(f).append('"');
                    else
                        sb.append(f);
                }
                sb.append(";");
            }

            font.attr("style", sb.toString());

            font.tagName("span");
        }

        // Sanitize styles
        for (Element element : document.select("*")) {
            // Class style
            String tag = element.tagName();
            String clazz = element.className();
            String style = processStyles(context, tag, clazz, null, sheets);

            // Element style
            style = HtmlHelper.mergeStyles(style, element.attr("style"));

            if ("fairemail_debug_info".equals(clazz))
                style = HtmlHelper.mergeStyles(style, "font-size: smaller");

            if (text_align) {
                // https://developer.mozilla.org/en-US/docs/Web/HTML/Element/center
                if ("center".equals(element.tagName())) {
                    style = HtmlHelper.mergeStyles(style, "text-align:center");
                    element.tagName("div");
                } else if ("table".equals(element.tagName())) {
                    if (!element.attr("style").contains("text-align"))
                        style = HtmlHelper.mergeStyles(style, "text-align:left");
                } else {
                    // https://developer.mozilla.org/en-US/docs/Web/HTML/Attributes
                    String align = element.attr("align");
                    if (!TextUtils.isEmpty(align))
                        style = HtmlHelper.mergeStyles("text-align:" + align, style);
                }
            }

            // Process style
            if (!TextUtils.isEmpty(style)) {
                boolean block = false;
                StringBuilder sb = new StringBuilder();
                if (!view &&
                        "span".equals(element.tagName()) &&
                        "rtl".equals(element.attr("dir")))
                    block = true;

                Map<String, String> kv = new LinkedHashMap<>();
                String[] params = style.split(";");
                for (String param : params) {
                    int colon = param.indexOf(':');
                    if (colon <= 0)
                        continue;
                    String key = param.substring(0, colon)
                            .trim()
                            .toLowerCase(Locale.ROOT);
                    String value = param.substring(colon + 1)
                            .replace("!important", "")
                            .trim()
                            .toLowerCase(Locale.ROOT)
                            .replaceAll("\\s+", " ");
                    kv.put(key, value);
                }

                List<String> keys = new ArrayList<>(kv.keySet());
                Collections.sort(keys); // background-color first

                for (String key : keys) {
                    String value = kv.get(key);
                    switch (key) {
                        case "background-image":
                            // https://developer.mozilla.org/en-US/docs/Web/CSS/background-image
                            String url = value.replace(" ", "");
                            int us = url.indexOf("url(");
                            int ue = url.indexOf(')', us + 4);
                            if (us >= 0 && ue > us) {
                                url = url.substring(us + 4, ue);
                                if ((url.startsWith("'") && url.endsWith("'")) ||
                                        (url.startsWith("\"") && url.endsWith("\"")))
                                    url = url.substring(1, url.length() - 1);
                                Element img = document.createElement("img")
                                        .attr("src", url);
                                element.prependElement("br");
                                element.prependChild(img);
                            }
                            break;
                        case "color":
                        case "background":
                        case "background-color":
                            // https://developer.mozilla.org/en-US/docs/Web/CSS/color
                            // https://developer.mozilla.org/en-US/docs/Web/CSS/background
                            // https://developer.mozilla.org/en-US/docs/Web/CSS/background-color
                            if ("color".equals(key)) {
                                if (!text_color)
                                    continue;
                            } else {
                                if (!background_color)
                                    continue;
                            }

                            Integer color = HtmlHelper.parseColor(value);

                            if (color != null && color == Color.TRANSPARENT) {
                                if ("color".equals(key) && BuildConfig.DEBUG)
                                    if (display_hidden)
                                        sb.append("text-decoration:line-through;");
                                    else if (false) {
                                        Log.i("Removing color transparent " + element.tagName());
                                        element.remove();
                                        continue;
                                    }
                                color = null;
                            }

                            if ("color".equals(key)) {
                                Integer bg = null;
                                if (background_color) {
                                    Element e = element;
                                    while (e != null && bg == null)
                                        if (e.hasAttr("x-background"))
                                            bg = HtmlHelper.parseWebColor(e.attr("x-background"));
                                        else
                                            e = e.parent();
                                }

                                if (!view &&
                                        color != null && (bg == null || bg == Color.TRANSPARENT)) {
                                    // Special case:
                                    //   external draft: very dark/light font
                                    double lum = ColorUtils.calculateLuminance(color);
                                    if (dark ? lum < 1 - HtmlHelper.MIN_LUMINANCE_COMPOSE : lum > HtmlHelper.MIN_LUMINANCE_COMPOSE)
                                        color = null;
                                }

                                if (bg == null) {
                                    if (color != null && view)
                                        color = HtmlHelper.adjustColor(dark, textColorPrimary, color);
                                } else if (bg == Color.TRANSPARENT) {
                                    // Background color was suppressed because "no color"
                                    if (color != null) {
                                        double lum = ColorUtils.calculateLuminance(color);
                                        if (dark ? lum < 1 - HtmlHelper.MIN_LUMINANCE_VIEW : lum > HtmlHelper.MIN_LUMINANCE_VIEW)
                                            color = textColorPrimary;
                                    }
                                }

                                if (color != null)
                                    element.attr("x-color", "true");
                            } else /* background */ {
                                if (color != null && !HtmlHelper.hasColor(color))
                                    color = Color.TRANSPARENT;

                                if (color != null)
                                    element.attr("x-background", HtmlHelper.encodeWebColor(color));

                                if (color != null && dark) {
                                    boolean fg = false;
                                    if (text_color) {
                                        fg = (HtmlHelper.parseColor(kv.get("color")) != null);
                                        Element e = element;
                                        while (e != null && !fg)
                                            if (e.hasAttr("x-color"))
                                                fg = true;
                                            else
                                                e = e.parent();
                                    }

                                    // Dark theme, background color with no text color:
                                    //   force (inverse) text color
                                    if (!fg) {
                                        double lum = (color == Color.TRANSPARENT ? 0 : ColorUtils.calculateLuminance(color));
                                        int c = (lum < 0.5 ? textColorPrimary : textColorPrimaryInverse);
                                        sb.append("color")
                                                .append(':')
                                                .append(HtmlHelper.encodeWebColor(c))
                                                .append(";");
                                    }
                                }
                            }

                            if (color == null) {
                                element.removeAttr(key);
                                continue;
                            }

                            String c = HtmlHelper.encodeWebColor(color);
                            sb.append(key).append(':').append(c).append(";");
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
                                element.attr(key, c);
                            break;

                        case "font-size":
                            // https://developer.mozilla.org/en-US/docs/Web/CSS/font-size
                            if (!text_size)
                                continue;

                            float current;
                            if (tag.length() == 2 &&
                                    tag.charAt(0) == 'h' &&
                                    Character.isDigit(tag.charAt(1)))
                                current = HEADING_SIZES[tag.charAt(1) - '1'];
                            else
                                current = 1.0f;

                            Element parent = element.parent();
                            while (parent != null) {
                                String xFontSize = parent.attr("x-font-size");
                                if (!TextUtils.isEmpty(xFontSize)) {
                                    current = Float.parseFloat(xFontSize);
                                    break;
                                }
                                parent = parent.parent();
                            }

                            Float fsize = HtmlHelper.getFontSize(value, current);
                            if (fsize != null)
                                if (fsize == 0) {
                                    if (BuildConfig.DEBUG)
                                        if (display_hidden && false)
                                            sb.append("text-decoration:line-through;");
                                        else if (false) {
                                            Log.i("Removing font size zero " + element.tagName());
                                            element.remove();
                                        }
                                } else {
                                    if (!view) {
                                        if (fsize < 1)
                                            fsize = (fsize < HtmlHelper.FONT_SMALL
                                                    ? HtmlHelper.FONT_XSMALL : HtmlHelper.FONT_SMALL);
                                        else if (fsize > 1)
                                            fsize = (fsize > HtmlHelper.FONT_LARGE
                                                    ? HtmlHelper.FONT_XLARGE : HtmlHelper.FONT_LARGE);
                                    }
                                    element.attr("x-font-size", Float.toString(fsize));
                                    element.attr("x-font-size-rel", Float.toString(fsize / current));
                                }
                            break;

                        case "font-weight":
                            // https://developer.mozilla.org/en-US/docs/Web/CSS/font-weight
                            sb.append(key).append(":").append(value).append(";");
                            break;

                        case "font-family":
                            if (!text_font)
                                continue;

                            // https://developer.mozilla.org/en-US/docs/Web/CSS/font-family
                            sb.append(key).append(":").append(value).append(";");
                            break;

                        case "font-style":
                            // https://developer.mozilla.org/en-US/docs/Web/CSS/font-style
                            if (value.contains("italic") || value.contains("oblique"))
                                sb.append(key).append(":").append("italic").append(";");
                            break;

                        case "text-decoration":
                        case "text-decoration-line":
                            // https://developer.mozilla.org/en-US/docs/Web/CSS/text-decoration
                            // https://developer.mozilla.org/en-US/docs/Web/CSS/text-decoration-line
                            if (value.contains("line-through"))
                                sb.append("text-decoration:line-through;");
                            else if (value.contains("underline"))
                                sb.append("text-decoration:underline;");
                            break;

                        case "text-transform":
                            // https://developer.mozilla.org/en-US/docs/Web/CSS/text-transform
                            NodeTraversor.traverse(new NodeVisitor() {
                                @Override
                                public void head(Node node, int depth) {
                                    if (node instanceof TextNode) {
                                        TextNode tnode = (TextNode) node;
                                        String text = tnode.getWholeText();
                                        switch (value) {
                                            case "capitalize":
                                                // TODO: capitalize
                                                break;
                                            case "uppercase":
                                                tnode.text(text.toUpperCase(Locale.ROOT));
                                                break;
                                            case "lowercase":
                                                tnode.text(text.toLowerCase(Locale.ROOT));
                                                break;
                                        }
                                    }
                                }

                                @Override
                                public void tail(Node node, int depth) {
                                    // Do nothing
                                }
                            }, element);
                            break;

                        case "display":
                            // https://developer.mozilla.org/en-US/docs/Web/CSS/display
                            if (element.parent() != null && "none".equals(value)) {
                                if (display_hidden)
                                    sb.append("text-decoration:line-through;");
                                else {
                                    Log.i("Removing display none " + element.tagName());
                                    element.remove();
                                }
                            }

                            if ("block".equals(value) || "inline-block".equals(value))
                                element.attr("x-block", "true");

                            if ("inline".equals(value) || "inline-block".equals(value)) {
                                if (element.nextSibling() != null)
                                    element.attr("x-inline", "true");
                            }
                            break;
/*
                        case "height":
                        case "width":
                            //case "font-size":
                            //case "line-height":
                            if (element.parent() != null && !display_hidden) {
                                Float s = getFontSize(value, 1.0f);
                                if (s != null && s == 0) {
                                    if (!"table".equals(element.tagName())) {
                                        Log.i("Removing no height/width " + element.tagName());
                                        element.remove();
                                    }
                                }
                            }
                            break;
*/
                        case "margin":
                        case "padding":
                        case "margin-top":
                        case "margin-bottom":
                        case "padding-top":
                        case "padding-bottom":
                            // https://developer.mozilla.org/en-US/docs/Web/CSS/margin
                            // https://developer.mozilla.org/en-US/docs/Web/CSS/padding
                            if (element.isBlock()) {
                                Float[] p = new Float[4]; // top, right, bottom, left

                                String[] v = value.split(" ");
                                for (int i = 0; i < v.length && i < p.length; i++)
                                    p[i] = HtmlHelper.getFontSize(v[i], 1.0f);

                                if (v.length == 1) {
                                    p[1] = p[0];
                                    p[2] = p[0];
                                    p[3] = p[0];
                                } else if (v.length == 2) {
                                    // top and bottom, left and right
                                    p[2] = p[0];
                                    p[3] = p[1];
                                } else if (v.length == 3) {
                                    // top, right and left, bottom
                                    p[3] = p[1];
                                }

                                if (key.endsWith("top"))
                                    p[2] = null;
                                else if (key.endsWith("bottom"))
                                    p[0] = null;

                                // Both margin and padding can be set
                                if (p[0] != null && !"true".equals(element.attr("x-line-before")))
                                    element.attr("x-line-before", Boolean.toString(p[0] > 0.5));
                                if (p[2] != null && !"true".equals(element.attr("x-line-after")))
                                    element.attr("x-line-after", Boolean.toString(p[2] > 0.5));
                            }
                            break;

                        case "text-align":
                            // https://developer.mozilla.org/en-US/docs/Web/CSS/text-align
                            if (text_align) {
                                if (!element.isBlock())
                                    block = true;
                                element.attr("x-align", value);
                                sb.append(key).append(':').append(value).append(';');
                            }
                            break;

                        case "border-left":
                        case "border-right":
                            if (value != null) {
                                // 1px solid rgb(204,204,204)
                                Float border = HtmlHelper.getFontSize(value.trim().split("\\s+")[0], 1.0f);
                                if (border != null && border > 0) {
                                    element.attr("x-border", "true");
                                    if (!view) {
                                        sb.append("border-left").append(':').append("3px solid #ccc").append(';');
                                        sb.append("padding-left").append(':').append("3px").append(';');
                                    }
                                }
                            }
                            break;

                        case "list-style-type":
                            // https://developer.mozilla.org/en-US/docs/Web/CSS/list-style-type
                            element.attr("x-list-style", value);
                            if (!view)
                                sb.append(key).append(':').append(value).append(';');
                            break;

                        case "visibility":
                            // https://developer.mozilla.org/en-US/docs/Web/CSS/visibility
                            if (element.parent() != null &&
                                    ("hidden".equals(value) || "collapse".equals(value)))
                                if (display_hidden)
                                    sb.append("text-decoration:line-through;");
                                else
                                    sb.append(key).append(':').append("hidden").append(';');
                            break;
                    }
                }

                if (block) {
                    sb.append("display:block;");
                    Element next = element.nextElementSibling();
                    if (next != null && "br".equals(next.tagName()))
                        next.remove();
                }

                if (sb.length() == 0)
                    element.removeAttr("style");
                else {
                    element.attr("style", sb.toString());
                    if (BuildConfig.DEBUG)
                        Log.i("Style=" + sb);
                }
            }

            if (element.isBlock() &&
                    !"true".equals(element.attr("x-inline")))
                element.attr("x-block", "true");
        }

        // Insert titles
        if (text_titles)
            for (Element e : document.select("[title]")) {
                String title = e.attr("title");
                if (TextUtils.isEmpty(title))
                    continue;
                if ("img".equals(e.tagName()) &&
                        title.equals(e.attr("alt")))
                    continue;
                e.prependChild(document.createElement("span").text("{" + title + "}"));
            }

        // Replace headings
        Elements hs = document.select("h1,h2,h3,h4,h5,h6");
        hs.attr("x-line-before", "true");
        if (text_size) {
            if (text_separators && view)
                for (Element h : hs)
                    h.appendElement("hr")
                            .attr("x-block", "true");
            else
                hs.attr("x-line-after", "true");
        } else {
            hs.tagName("strong");
            hs.attr("x-line-after", "true");
        }

        // Replace addresses by link
        // https://developer.mozilla.org/en-US/docs/Web/HTML/Element/address
        // https://en.wikipedia.org/wiki/Geo_URI_scheme
        for (Element address : document.select("address"))
            if (address.select("a").size() == 0)
                address.tagName("a")
                        .attr("href", "geo:0,0?q=" + Uri.encode(address.text()));

        // Paragraphs
        for (Element p : document.select("p")) {
            p.tagName("div");

            Node last = p.lastChild();
            if (last != null && "br".equals(last.nodeName()))
                last.remove();

            if (TextUtils.isEmpty(p.text())) {
                p.attr("x-line-before", "false");
                p.attr("x-line-after", "true");
            } else
                p.attr("x-paragraph", "true");
        }

        // Short inline quotes
        // https://developer.mozilla.org/en-US/docs/Web/HTML/Element/q
        for (Element q : document.select("q")) {
            q.tagName("a");
            String cite = q.attr("cite");
            if (!TextUtils.isEmpty(cite) && !cite.trim().startsWith("#"))
                q.attr("href", cite);
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
            NodeTraversor.traverse(new NodeVisitor() {
                private int index = 0;
                private boolean inElement = false;

                @Override
                public void head(Node node, int depth) {
                    if (node instanceof Element)
                        inElement = true;
                    else if (node instanceof TextNode) {
                        if (inElement) {
                            TextNode tnode = (TextNode) node;
                            StringBuilder sb = new StringBuilder();
                            for (Character c : tnode.getWholeText().toCharArray()) {
                                if (c == '\t')
                                    do {
                                        index++;
                                        sb.append(' ');
                                    }
                                    while ((index % HtmlHelper.TAB_SIZE) != 0);
                                else {
                                    if (c == '\n')
                                        index = 0;
                                    else
                                        index++;
                                    sb.append(c);
                                }
                            }
                            tnode.text(sb.toString());
                        }
                    }
                }

                @Override
                public void tail(Node node, int depth) {
                    if (node instanceof Element)
                        inElement = false;
                }
            }, pre);

            pre.tagName("div");
            pre.attr("x-plain", "true");
        }

        // Code
        // https://developer.mozilla.org/en-US/docs/Web/HTML/Element/code
        document.select("code").tagName("strong");

        // Lines
        // https://developer.mozilla.org/en-US/docs/Web/HTML/Element/hr
        for (Element hr : document.select("hr"))
            hr.attr("x-keep-line", "true");

        // Descriptions
        // https://developer.mozilla.org/en-US/docs/Web/HTML/Element/dl
        document.select("dl").tagName("div");
        for (Element dt : document.select("dt"))
            dt.tagName("strong");
        for (Element dd : document.select("dd")) {
            dd.tagName("em");
            dd.attr("x-line-after", "true");
        }

        // Abbreviations
        // https://developer.mozilla.org/en-US/docs/Web/HTML/Element/abbr
        document.select("abbr").tagName("u");

        // Tables
        // https://developer.mozilla.org/en-US/docs/Web/HTML/Element/table
        for (Element table : document.select("table")) {
            table.tagName("div");
            // Ignore summary attribute
            for (Element row : table.children()) {
                row.tagName("div");

                Element separate = null;
                List<Node> merge = new ArrayList<>();
                for (Element col : row.children()) {
                    Element next = col.nextElementSibling();

                    // Get nodes with content
                    List<Node> nodes = new ArrayList<>(col.childNodes());
                    while (nodes.size() > 0) {
                        Node first = nodes.get(0);
                        if (first instanceof TextNode && ((TextNode) first).isBlank()) {
                            nodes.remove(0);
                            continue;
                        }

                        Node last = nodes.get(nodes.size() - 1);
                        if (last instanceof TextNode && ((TextNode) last).isBlank()) {
                            nodes.remove(nodes.size() - 1);
                            continue;
                        }

                        break;
                    }

                    // Merge single images into next column
                    if (nodes.size() == 1) {
                        Node lonely = nodes.get(0);

                        // prevent extra newlines
                        lonely.removeAttr("x-paragraph");

                        if (next == null ||
                                next.attr("x-align")
                                        .equals(col.attr("x-align"))) {
                            if (lonely instanceof Element &&
                                    "img".equals(lonely.nodeName())) {
                                lonely.remove();
                                lonely.removeAttr("x-block");
                                merge.add(lonely);
                                if (next != null)
                                    continue;
                            }
                        }

                        if (lonely instanceof TextNode &&
                                "\u00a0".equals(((TextNode) lonely).getWholeText()))
                            lonely.remove(); // -> column separator
                    }

                    if (merge.size() > 0) {
                        for (int m = merge.size() - 1; m >= 0; m--)
                            col.prependChild(merge.get(m));
                        merge.clear();
                    }

                    if ("th".equals(col.tagName())) {
                        Element strong = new Element("strong");
                        for (Node child : new ArrayList<>(col.childNodes())) {
                            child.remove();
                            strong.appendChild(child);
                        }
                        col.appendChild(strong);
                    }

                    // Flow not / left aligned columns
                    String align = col.attr("x-align");
                    //if (next == null && row.childrenSize() == 2) {
                    //    align = "end";
                    //    String style = col.attr("style");
                    //    col.attr("style",
                    //            mergeStyles(style, "text-align:" + align));
                    //}
                    if (TextUtils.isEmpty(align) ||
                            "left".equals(align) ||
                            "start".equals(align)) {
                        col.removeAttr("x-block");
                        if (separate != null)
                            separate.attr("x-column", "true");
                        separate = col;
                    } else {
                        separate = null;
                        if ("true".equals(col.attr("x-line-before")))
                            col.removeAttr("x-line-before");
                    }

                    col.tagName("div");
                }

                if (merge.size() != 0)
                    throw new AssertionError("merge");

                if (text_separators && view)
                    row.appendElement("hr")
                            .attr("x-block", "true")
                            .attr("x-dashed", "true");
            }
        }

        // Fix dangling table elements
        document.select("tr,th,td").tagName("div");

        // Lists
        for (Element e : document.select("ol,ul,blockquote")) {
            Element parent = e.parent();
            if (view) {
                if ("blockquote".equals(e.tagName()) || parent == null ||
                        !("li".equals(parent.tagName()) ||
                                "ol".equals(parent.tagName()) ||
                                "ul".equals(parent.tagName()))) {
                    if (!"false".equals(e.attr("x-line-before")))
                        e.attr("x-line-before", "true");
                    if (!"false".equals(e.attr("x-line-after")))
                        e.attr("x-line-after", "true");
                }

                // Unflatten list for viewing
                if ((parent != null && "li".equals(parent.tagName())) &&
                        ("ol".equals(e.tagName()) || "ul".equals(e.tagName())))
                    e.attr("x-list-level", "false");
            } else {
                if (!BuildConfig.DEBUG) {
                    String style = e.attr("style");
                    e.attr("style",
                            HtmlHelper.mergeStyles(style, "margin-top:0;margin-bottom:0"));

                    int ltr = 0;
                    int rtl = 0;
                    for (Element li : e.children()) {
                        if ("rtl".equals(li.attr("dir")))
                            rtl++;
                        else
                            ltr++;
                        li.removeAttr("dir");
                    }
                    e.attr("dir", rtl > ltr ? "rtl" : "ltr");
                }

                // Flatten list for editor
                if (parent != null && "li".equals(parent.tagName())) {
                    List<Node> children = parent.childNodes();
                    for (Node child : children) {
                        child.remove();
                        if (child instanceof Element &&
                                "ol".equals(child.nodeName()) || "ul".equals(child.nodeName()))
                            parent.before(child);
                        else
                            parent.before(parent.shallowClone().appendChild(child));
                    }
                    parent.remove();
                }
            }
        }

        // Images
        // https://developer.mozilla.org/en-US/docs/Web/HTML/Element/img
        for (Element img : document.select("img")) {
            String alt = img.attr("alt");
            String src = img.attr("src");
            String tracking = img.attr("x-tracking");
            boolean isInline = src.startsWith("cid:");

            if (TextUtils.isEmpty(src)) {
                Log.i("Removing empty image");
                img.remove();
                continue;
            }

            if (!show_images && !(inline_images && isInline) && !image_placeholders) {
                Log.i("Removing placeholder");
                img.removeAttr("src");
                continue;
            }

            // Remove spacer, etc
            if (!show_images && !(inline_images && isInline) &&
                    TextUtils.isEmpty(img.attr("x-tracking"))) {
                Integer width = Helper.parseInt(img.attr("width").trim());
                Integer height = Helper.parseInt(img.attr("height").trim());
                if (width != null && height != null) {
                    if (width == 0 && height != 0)
                        width = height;
                    if (width != 0 && height == 0)
                        height = width;
                }
                if ((width != null && width <= SMALL_IMAGE_SIZE) ||
                        (height != null && height <= SMALL_IMAGE_SIZE)) {
                    Log.i("Removing small image src=" + src);
                    img.remove();
                    continue;
                }
            }

            if (alt.length() > MAX_ALT)
                alt = alt.substring(0, MAX_ALT) + "…";

            if (!show_images && !(inline_images && isInline))
                if (TextUtils.isEmpty(tracking)) {
                    if (TextUtils.isEmpty(alt)) {
                        boolean linked = false;
                        Element p = img.parent();
                        while (p != null && !linked)
                            if ("a".equals(p.tagName())) {
                                String href = p.attr("href");
                                if (TextUtils.isEmpty(href))
                                    break;
                                if (!TextUtils.isEmpty(p.text()))
                                    break;
                                linked = true;
                            } else
                                p = p.parent();
                        if (linked)
                            alt = context.getString(R.string.title_image_link);
                    }
                    if (!TextUtils.isEmpty(alt)) {
                        Element a = document.createElement("a")
                                .attr("href", src)
                                .text("[" + alt + "]")
                                .attr("x-font-size-abs", Integer.toString(textSizeSmall));
                        img.appendChild(a);
                    }
                } else if (!TextUtils.isEmpty(alt)) {
                    Element a = document.createElement("a")
                            .attr("href", tracking)
                            .text("[" + alt + "]")
                            .attr("x-font-size-abs", Integer.toString(textSizeSmall));
                    img.appendChild(a);
                }
        }

        // Selective new lines
        for (Element div : document.select("div"))
            div.tagName("span");

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
            for (Element span : document.select("span"))
                if (!TextUtils.isEmpty(span.attr("color")))
                    span.tagName("font");

        document.body(); // Normalise document

        return document;
    }

    static List<CSSStyleSheet> parseStyles(Elements styles) {
        List<CSSStyleSheet> sheets = new ArrayList<>();
        for (Element style : styles) {
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
        return sheets;
    }

    static String processStyles(Context context, String tag, String clazz, String style, List<CSSStyleSheet> sheets) {
        for (CSSStyleSheet sheet : sheets)
            if (isScreenMedia(context, sheet.getMedia())) {
                style = processStyles(context, null, clazz, style, sheet.getCssRules(), Selector.SAC_ELEMENT_NODE_SELECTOR);
                style = processStyles(context, tag, clazz, style, sheet.getCssRules(), Selector.SAC_ELEMENT_NODE_SELECTOR);
                style = processStyles(context, tag, clazz, style, sheet.getCssRules(), Selector.SAC_CONDITIONAL_SELECTOR);
            }
        return style;
    }

    private static String processStyles(Context context, String tag, String clazz, String style, CSSRuleList rules, int stype) {
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
                                        : tag.equalsIgnoreCase(eselector.getLocalName()))
                                    style = HtmlHelper.mergeStyles(style, srule.getStyle().getCssText(), false);
                                break;
                            case Selector.SAC_CONDITIONAL_SELECTOR:
                                if (!TextUtils.isEmpty(clazz)) {
                                    ConditionalSelectorImpl cselector = (ConditionalSelectorImpl) selector;
                                    if (cselector.getCondition().getConditionType() == SAC_CLASS_CONDITION) {
                                        ClassConditionImpl ccondition = (ClassConditionImpl) cselector.getCondition();
                                        String value = ccondition.getValue();
                                        for (String cls : clazz.split("\\s+"))
                                            if (cls.equalsIgnoreCase(value)) {
                                                style = HtmlHelper.mergeStyles(style, srule.getStyle().getCssText(), false);
                                                break;
                                            }

                                    }
                                }
                                break;
                        }
                    }
                    break;

                case CSSRule.MEDIA_RULE:
                    CSSMediaRuleImpl mrule = (CSSMediaRuleImpl) rule;
                    if (isScreenMedia(context, mrule.getMedia()))
                        style = processStyles(context, tag, clazz, style, mrule.getCssRules(), stype);
                    break;
            }
        }
        return style;
    }

    private static boolean isScreenMedia(Context context, MediaList media) {
        // https://developer.mozilla.org/en-US/docs/Web/CSS/Media_Queries/Using_media_queries
        // https://developers.google.com/gmail/design/reference/supported_css#supported_types
        if (media instanceof MediaListImpl) {
            MediaListImpl _media = (MediaListImpl) media;
            for (int i = 0; i < _media.getLength(); i++) {
                String type = _media.mediaQuery(i).getMedia();

                boolean hasMaxWidth = false;
                List<Property> props = _media.mediaQuery(i).getProperties();
                if (props != null)
                    for (Property prop : props) {
                        if ("max-width".equals(prop.getName()) ||
                                "max-device-width".equals(prop.getName())) {
                            hasMaxWidth = true;
                            break;
                        }
                    }
                if (!hasMaxWidth)
                    if ("all".equals(type) || "screen".equals(type) || _media.mediaQuery(i).isNot()) {
                        Log.i("Using media=" + media.getMediaText());
                        return true;
                    }
            }
            Log.i("Not using media=" + media.getMediaText());
        } else
            Log.e("Media class=" + media.getClass().getName());
        return false;
    }
}
