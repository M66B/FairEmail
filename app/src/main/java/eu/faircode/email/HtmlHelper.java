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

    Copyright 2018-2021 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.text.Html;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextDirectionHeuristics;
import android.text.TextUtils;
import android.text.style.AlignmentSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.BulletSpan;
import android.text.style.CharacterStyle;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
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
import android.util.Patterns;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.core.graphics.ColorUtils;
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MailDateFormat;
import javax.mail.internet.MimeUtility;

import static androidx.core.text.HtmlCompat.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL;
import static org.w3c.css.sac.Condition.SAC_CLASS_CONDITION;

public class HtmlHelper {
    static final int PREVIEW_SIZE = 500; // characters

    static final float FONT_SMALL = 0.8f;
    static final float FONT_LARGE = 1.25f;
    static final String QUOTE_STYLE = "border-left:3px solid #ccc; padding-left:3px;";

    private static final int DEFAULT_FONT_SIZE = 16; // pixels
    private static final int DEFAULT_FONT_SIZE_PT = 12; // points
    private static final int GRAY_THRESHOLD = Math.round(255 * 0.2f);
    private static final int COLOR_THRESHOLD = Math.round(255 * 0.1f);
    private static final float MIN_LUMINANCE = 0.7f;
    private static final int TAB_SIZE = 2;
    private static final int MAX_ALT = 250;
    private static final int MAX_AUTO_LINK = 250;
    private static final int MAX_FORMAT_TEXT_SIZE = 200 * 1024; // characters
    static final int MAX_FULL_TEXT_SIZE = 1024 * 1024; // characters
    private static final int SMALL_IMAGE_SIZE = 5; // pixels
    private static final int TRACKING_PIXEL_SURFACE = 25; // pixels
    private static final float[] HEADING_SIZES = {1.5f, 1.4f, 1.3f, 1.2f, 1.1f, 1f};
    private static final String LINE = "----------------------------------------";

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

    // http://www.alanwood.net/demos/wingdings.html
    static int[] WINGDING_TO_UNICODE = {
            0x0020, 0x1F589, 0x2702, 0x2701, 0x1F453, 0x1F56D, 0x1F56E, 0x1F56F,
            0x1F57F, 0x2706, 0x1F582, 0x1F583, 0x1F4EA, 0x1F4EB, 0x1F4EC, 0x1F4ED,
            0x1F4C1, 0x1F4C2, 0x1F4C4, 0x1F5CF, 0x1F5D0, 0x1F5C4, 0x231B, 0x1F5AE,
            0x1F5B0, 0x1F5B2, 0x1F5B3, 0x1F5B4, 0x1F5AB, 0x1F5AC, 0x2707, 0x270D,
            0x1F58E, 0x270C, 0x1F44C, 0x1F44D, 0x1F44E, 0x261C, 0x261E, 0x261D,
            0x261F, 0x1F590, 0x263A, 0x1F610, 0x2639, 0x1F4A3, 0x2620, 0x1F3F3,
            0x1F3F1, 0x2708, 0x263C, 0x1F4A7, 0x2744, 0x1F546, 0x271E, 0x1F548,
            0x2720, 0x2721, 0x262A, 0x262F, 0x0950, 0x2638, 0x2648, 0x2649,
            0x264A, 0x264B, 0x264C, 0x264D, 0x264E, 0x264F, 0x2650, 0x2651,
            0x2652, 0x2653, 0x1F670, 0x1F675, 0x25CF, 0x1F53E, 0x25A0, 0x25A1,
            0x1F790, 0x2751, 0x2752, 0x2B27, 0x29EB, 0x25C6, 0x2756, 0x2B25,
            0x2327, 0x2BB9, 0x2318, 0x1F3F5, 0x1F3F6, 0x1F676, 0x1F677, 0x003F,
            0x24EA, 0x2460, 0x2461, 0x2462, 0x2463, 0x2464, 0x2465, 0x2466,
            0x2467, 0x2468, 0x2469, 0x24FF, 0x2776, 0x2777, 0x2778, 0x2779,
            0x277A, 0x277B, 0x277C, 0x277D, 0x277E, 0x277F, 0x1F662, 0x1F660,
            0x1F661, 0x1F663, 0x1F65E, 0x1F65C, 0x1F65D, 0x1F65F, 0x00B7, 0x2022,
            0x25AA, 0x26AA, 0x1F786, 0x1F788, 0x25C9, 0x25CE, 0x1F53F, 0x25AA,
            0x25FB, 0x1F7C2, 0x2726, 0x2605, 0x2736, 0x2734, 0x2739, 0x2735,
            0x2BD0, 0x2316, 0x27E1, 0x2311, 0x2BD1, 0x272A, 0x2730, 0x1F550,
            0x1F551, 0x1F552, 0x1F553, 0x1F554, 0x1F555, 0x1F556, 0x1F557, 0x1F558,
            0x1F559, 0x1F55A, 0x1F55B, 0x2BB0, 0x2BB1, 0x2BB2, 0x2BB3, 0x2BB4,
            0x2BB5, 0x2BB6, 0x2BB7, 0x1F66A, 0x1F66B, 0x1F655, 0x1F654, 0x1F657,
            0x1F656, 0x1F650, 0x1F651, 0x1F652, 0x1F653, 0x232B, 0x2326, 0x2B98,
            0x2B9A, 0x2B99, 0x2B9B, 0x2B88, 0x2B8A, 0x2B89, 0x2B8B, 0x1F868,
            0x1F86A, 0x1F869, 0x1F86B, 0x1F86C, 0x1F86D, 0x1F86F, 0x1F86E, 0x1F878,
            0x1F87A, 0x1F879, 0x1F87B, 0x1F87C, 0x1F87D, 0x1F87F, 0x1F87E, 0x21E6,
            0x21E8, 0x21E7, 0x21E9, 0x2B04, 0x21F3, 0x2B00, 0x2B01, 0x2B03,
            0x2B02, 0x1F8AC, 0x1F8AD, 0x1F5F6, 0x2714, 0x1F5F7, 0x1F5F9, 0x0077
    };

    private static final List<String> TRACKING_HOSTS = Collections.unmodifiableList(Arrays.asList(
            "www.google-analytics.com"
    ));

    static Document sanitizeCompose(Context context, String html, boolean show_images) {
        try {
            return sanitize(context, JsoupEx.parse(html), false, show_images);
        } catch (Throwable ex) {
            // OutOfMemoryError
            Log.e(ex);
            Document document = Document.createShell("");
            Element strong = document.createElement("strong");
            strong.text(android.util.Log.getStackTraceString(ex));
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
            strong.text(android.util.Log.getStackTraceString(ex));
            document.body().appendChild(strong);
            return document;
        }
    }

    private static Document sanitize(Context context, Document parsed, boolean view, boolean show_images) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String theme = prefs.getString("theme", "blue_orange_system");
        boolean bw = "black_and_white".equals(theme);
        boolean background_color = (!view || (!bw && prefs.getBoolean("background_color", false)));
        boolean text_color = (!view || (!bw && prefs.getBoolean("text_color", true)));
        boolean text_size = (!view || prefs.getBoolean("text_size", true));
        boolean text_font = (!view || prefs.getBoolean("text_font", true));
        boolean text_align = prefs.getBoolean("text_align", true);
        boolean display_hidden = prefs.getBoolean("display_hidden", false);
        boolean disable_tracking = prefs.getBoolean("disable_tracking", true);
        boolean parse_classes = prefs.getBoolean("parse_classes", true);
        boolean inline_images = prefs.getBoolean("inline_images", false);
        boolean text_separators = prefs.getBoolean("text_separators", true);
        boolean image_placeholders = prefs.getBoolean("image_placeholders", true);

        boolean dark = Helper.isDarkTheme(context);
        int textColorPrimary = Helper.resolveColor(context, android.R.attr.textColorPrimary);
        int textColorPrimaryInverse = Helper.resolveColor(context, android.R.attr.textColorPrimaryInverse);

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
        normalizeNamespaces(parsed, display_hidden);

        // Limit length
        if (view && truncate(parsed, MAX_FORMAT_TEXT_SIZE)) {
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

        Whitelist whitelist = Whitelist.relaxed()
                .addTags("hr", "abbr", "big", "font", "dfn", "del", "s", "tt")
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
                .removeAttributes("td", "colspan", "rowspan", "width")
                .removeAttributes("th", "colspan", "rowspan", "width")
                .addProtocols("img", "src", "cid")
                .addProtocols("img", "src", "data")
                .removeTags("a").addAttributes("a", "href", "title");
        if (text_color)
            whitelist.addAttributes("font", "color");
        if (text_size)
            whitelist.addAttributes("font", "size");
        if (text_font)
            whitelist.addAttributes("font", "face");
        if (text_align)
            whitelist.addTags("center").addAttributes(":all", "align");
        if (!view)
            whitelist.addProtocols("img", "src", "content");

        final Document document = new Cleaner(whitelist).clean(parsed);

        // Remove tracking pixels
        if (disable_tracking)
            removeTrackingPixels(context, document);

        // Font
        for (Element font : document.select("font")) {
            // https://developer.mozilla.org/en-US/docs/Web/HTML/Element/font
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
                    } else if (s < 3)
                        size = "small";
                    else if (s > 3)
                        size = "large";
                    else
                        size = "medium";
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
            String style = processStyles(tag, clazz, null, sheets);

            // Element style
            style = mergeStyles(style, element.attr("style"));

            if (text_align) {
                // https://developer.mozilla.org/en-US/docs/Web/HTML/Element/center
                if ("center".equals(element.tagName())) {
                    style = mergeStyles(style, "text-align:center");
                    element.tagName("div");
                } else if ("table".equals(element.tagName()))
                    style = mergeStyles(style, "text-align:left");
                else {
                    // https://developer.mozilla.org/en-US/docs/Web/HTML/Attributes
                    String align = element.attr("align");
                    if (!TextUtils.isEmpty(align))
                        style = mergeStyles(style, "text-align:" + align);
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
                    String key = param.substring(0, colon).trim().toLowerCase(Locale.ROOT);
                    String value = param.substring(colon + 1).toLowerCase(Locale.ROOT)
                            .replace("!important", "")
                            .trim()
                            .replaceAll("\\s+", " ");
                    kv.put(key, value);
                }

                List<String> keys = new ArrayList<>(kv.keySet());
                Collections.sort(keys); // background-color first

                for (String key : keys) {
                    String value = kv.get(key);
                    switch (key) {
                        case "color":
                        case "background-color":
                            // https://developer.mozilla.org/en-US/docs/Web/CSS/color
                            if ("color".equals(key) && !text_color)
                                continue;
                            if ("background-color".equals(key) && !background_color)
                                continue;

                            Integer color = parseColor(value);

                            if ("color".equals(key)) {
                                boolean bg = false;
                                if (background_color) {
                                    Element e = element;
                                    while (e != null && !bg)
                                        if (e.hasAttr("x-background"))
                                            bg = true;
                                        else
                                            e = e.parent();
                                }

                                // Background color set:
                                //   keep text color as-is
                                if (!bg) {
                                    // Special case:
                                    //   external draft / dark background / dark font
                                    if (color != null && !view && dark) {
                                        float lum = (float) ColorUtils.calculateLuminance(color);
                                        if (lum < 0.1f)
                                            color = null;
                                    }

                                    if (color != null && view)
                                        color = adjustColor(dark, textColorPrimary, color);
                                }

                                if (color != null)
                                    element.attr("x-color", "true");
                            } else /* background */ {
                                if (color != null && !hasColor(color))
                                    continue;

                                if (color != null)
                                    element.attr("x-background", "true");

                                if (dark) {
                                    boolean fg = false;
                                    if (text_color) {
                                        fg = (parseColor(kv.get("color")) != null);
                                        Element e = element;
                                        while (e != null && !fg)
                                            if (e.hasAttr("x-color"))
                                                fg = true;
                                            else
                                                e = e.parent();
                                    }

                                    // Dark theme, background color with no text color:
                                    //   force text color
                                    if (!fg)
                                        sb.append("color")
                                                .append(':')
                                                .append(encodeWebColor(textColorPrimaryInverse))
                                                .append(";");
                                }
                            }

                            if (color == null) {
                                element.removeAttr(key);
                                continue;
                            }

                            String c = encodeWebColor(color);
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

                            Float fsize = getFontSize(value, current);
                            if (fsize != null && fsize != 0) {
                                if (!view) {
                                    if (fsize < 1)
                                        fsize = FONT_SMALL;
                                    else if (fsize > 1)
                                        fsize = FONT_LARGE;
                                }
                                element.attr("x-font-size", Float.toString(fsize));
                                element.attr("x-font-size-rel", Float.toString(fsize / current));
                            }
                            break;

                        case "font-weight":
                            if (element.parent() != null) {
                                Integer fweight = getFontWeight(value);
                                if (fweight != null && fweight >= 600) {
                                    Element strong = new Element("strong");
                                    for (Node child : new ArrayList<>(element.childNodes())) {
                                        child.remove();
                                        strong.appendChild(child);
                                    }
                                    element.appendChild(strong);
                                }
                            }
                            break;

                        case "font-family":
                            if (!text_font)
                                continue;

                            // https://developer.mozilla.org/en-US/docs/Web/CSS/font-family
                            sb.append(key).append(":").append(value).append(";");
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

                            if ("block".equals(value) || "inline-block".equals(value))
                                element.attr("x-block", "true");

                            if ("inline".equals(value) || "inline-block".equals(value)) {
                                if (element.nextSibling() != null)
                                    element.attr("x-inline", "true");
                            }
                            break;

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

                        case "margin":
                        case "padding":
                        case "margin-top":
                        case "margin-bottom":
                        case "padding-top":
                        case "padding-bottom":
                            // https://developer.mozilla.org/en-US/docs/Web/CSS/margin
                            // https://developer.mozilla.org/en-US/docs/Web/CSS/padding
                            if (element.isBlock()) {
                                Float[] p = new Float[4];

                                String[] v = value.split(" ");
                                for (int i = 0; i < v.length && i < p.length; i++)
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

                                if (p[0] != null)
                                    if (p[0] == 0)
                                        element.attr("x-line-before", "false");
                                    else if (p[0] > 0.5)
                                        element.attr("x-line-before", "true");
                                if (p[2] != null)
                                    if (p[2] == 0)
                                        element.attr("x-line-after", "false");
                                    else if (p[2] > 0.5)
                                        element.attr("x-line-after", "true");
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
                                Float border = getFontSize(value.trim().split("\\s+")[0], 1.0f);
                                if (border != null && border > 0) {
                                    element.attr("x-border", "true");
                                    if (!view) {
                                        sb.append("border-left").append(':').append("3px solid #ccc").append(';');
                                        sb.append("padding-left").append(':').append("3px").append(';');
                                    }
                                }
                            }
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

        // Paragraphs
        for (Element p : document.select("p")) {
            p.tagName("div");
            if (p.childNodeSize() != 0) {
                if (p.childNodeSize() == 1) {
                    Node lonely = p.childNode(0);
                    if (lonely instanceof TextNode &&
                            "\u00a0".equals(((TextNode) lonely).getWholeText()))
                        continue;
                }
                p.attr("x-paragraph", "true");
            }
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
            pre.attr("x-plain", "true");
        }

        // Code
        // https://developer.mozilla.org/en-US/docs/Web/HTML/Element/code
        document.select("code").tagName("strong");

        // Lines
        // https://developer.mozilla.org/en-US/docs/Web/HTML/Element/hr
        if (!view)
            for (Element hr : document.select("hr")) {
                hr.tagName("div");
                hr.text(LINE);
            }

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

        // Subscript/Superscript
        // https://developer.mozilla.org/en-US/docs/Web/HTML/Element/sub
        // https://developer.mozilla.org/en-US/docs/Web/HTML/Element/sup
        if (!view)
            for (Element subp : document.select("sub,sup"))
                subp.tagName("small");

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
            if (view) {
                Element parent = e.parent();
                if ("blockquote".equals(e.tagName()) || parent == null ||
                        !("ol".equals(parent.tagName()) || "ul".equals(parent.tagName()))) {
                    if (!"false".equals(e.attr("x-line-before")))
                        e.attr("x-line-before", "true");
                    if (!"false".equals(e.attr("x-line-after")))
                        e.attr("x-line-after", "true");
                }
            } else {
                String style = e.attr("style");
                e.attr("style",
                        mergeStyles(style, "margin-top:0;margin-bottom:0"));

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
                Log.i("Removing small image");
                Integer width = Helper.parseInt(img.attr("width").trim());
                Integer height = Helper.parseInt(img.attr("height").trim());
                if ((width != null && width <= SMALL_IMAGE_SIZE) ||
                        (height != null && height <= SMALL_IMAGE_SIZE)) {
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
                                if (TextUtils.isEmpty(href) || href.equals("#"))
                                    break;
                                linked = true;
                            } else
                                p = p.parent();
                        if (linked)
                            alt = context.getString(R.string.title_image_link);
                    }
                    if (!TextUtils.isEmpty(alt))
                        img.appendText("[" + alt + "]");
                } else if (!TextUtils.isEmpty(alt)) {
                    Element a = document.createElement("a");
                    a.attr("href", tracking);
                    a.text("[" + alt + "]");
                    img.appendChild(a);
                }

            // Annotate source with width and height
            if (!TextUtils.isEmpty(src)) {
                int width = 0;
                int height = 0;

                // Relative sizes (%) = use image size

                String awidth = img.attr("width").replace(" ", "");
                for (int i = 0; i < awidth.length(); i++)
                    if (Character.isDigit(awidth.charAt(i)))
                        width = width * 10 + (byte) awidth.charAt(i) - (byte) '0';
                    else {
                        width = 0;
                        break;
                    }

                String aheight = img.attr("height").replace(" ", "");
                for (int i = 0; i < aheight.length(); i++)
                    if (Character.isDigit(aheight.charAt(i)))
                        height = height * 10 + (byte) aheight.charAt(i) - (byte) '0';
                    else {
                        height = 0;
                        break;
                    }

                if (width != 0 || height != 0) {
                    ImageHelper.AnnotatedSource a = new ImageHelper.AnnotatedSource(
                            src, width, height, !TextUtils.isEmpty(tracking));
                    img.attr("src", a.getAnnotated());
                }
            }
        }

        // Autolink
        if (view)
            autoLink(document);

        // Selective new lines
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

    static void autoLink(Document document) {
        // https://en.wikipedia.org/wiki/List_of_URI_schemes
        // xmpp:[<user>]@<host>[:<port>]/[<resource>][?<query>]
        // geo:<lat>,<lon>[,<alt>][;u=<uncertainty>]
        // tel:<phonenumber>
        final Pattern pattern = Pattern.compile(
                "(((?i:mailto):)?" + PatternsCompat.AUTOLINK_EMAIL_ADDRESS.pattern() + ")|" +
                        PatternsCompat.AUTOLINK_WEB_URL.pattern()
                                .replace("(?i:http|https|rtsp)://",
                                        "(((?i:http|https)://)|((?i:xmpp):))") + "|" +
                        "(?i:geo:\\d+,\\d+(,\\d+)?(;u=\\d+)?)|" +
                        "(?i:tel:" + Patterns.PHONE.pattern() + ")");

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

                            String group = matcher.group();
                            int start = matcher.start();
                            int end = matcher.end();

                            // Workarounds
                            if (group.endsWith(".")) {
                                end--;
                                group = group.substring(0, group.length() - 1);
                            }
                            if (group.startsWith("(")) {
                                start++;
                                group = group.substring(1);
                            }
                            if (group.endsWith(")")) {
                                end--;
                                group = group.substring(0, group.length() - 1);
                            }

                            boolean email = group.contains("@") && !group.contains(":");
                            Log.i("Web url=" + group + " " + start + "..." + end + "/" + text.length() +
                                    " linked=" + linked + " email=" + email + " count=" + links);

                            if (linked)
                                span.appendText(text.substring(pos, end));
                            else {
                                span.appendText(text.substring(pos, start));

                                Element a = document.createElement("a");
                                a.attr("href", (email ? "mailto:" : "") + group);
                                a.text(group);
                                span.appendChild(a);

                                links++;
                            }

                            pos = end;
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

    static void normalizeNamespaces(Document parsed, boolean display_hidden) {
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
                if (display_hidden ||
                        "body".equals(tag) ||
                        ns == null || tag.startsWith(ns)) {
                    String[] nstag = tag.split(":");
                    e.tagName(nstag[nstag.length > 1 ? 1 : 0]);
                    Log.i("Updated tag=" + tag + " to=" + e.tagName());
                } else {
                    e.remove();
                    Log.i("Removed tag=" + tag);
                }
            }
        }
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

    static String processStyles(String tag, String clazz, String style, List<CSSStyleSheet> sheets) {
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

    static String mergeStyles(String base, String style) {
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
            case "unset":
            case "initial":
            case "inherit":
                return null;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            Log.i(ex);
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
                "unset".equals(value) ||
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
            if (value.endsWith("rem"))
                return Float.parseFloat(value.substring(0, value.length() - 3).trim());
            if (value.endsWith("em"))
                return Float.parseFloat(value.substring(0, value.length() - 2).trim()) * current;
            if (value.endsWith("ex")) // 1 ex = 0.5 em
                return Float.parseFloat(value.substring(0, value.length() - 2).trim()) / 2 * current;
            if (value.endsWith("pt"))
                return Float.parseFloat(value.substring(0, value.length() - 2).trim()) / DEFAULT_FONT_SIZE_PT;
            if (value.endsWith("px"))
                return Float.parseFloat(value.substring(0, value.length() - 2).trim()) / DEFAULT_FONT_SIZE;

            // https://www.w3.org/Style/Examples/007/units.en.html
            if (value.endsWith("pc")) // 6 pc = 72 pt
                return Float.parseFloat(value.substring(0, value.length() - 2).trim()) / 12 / DEFAULT_FONT_SIZE_PT;
            if (value.endsWith("cm")) // 1 inch = 2.54 cm
                return Float.parseFloat(value.substring(0, value.length() - 2).trim()) / 2.54f * 72 / DEFAULT_FONT_SIZE_PT;
            if (value.endsWith("mm")) // 1 inch = 25.4 mm
                return Float.parseFloat(value.substring(0, value.length() - 2).trim()) / 25.4f * 72 / DEFAULT_FONT_SIZE_PT;
            if (value.endsWith("in")) // 1 inch = 72pt
                return Float.parseFloat(value.substring(0, value.length() - 2).trim()) * 72 / DEFAULT_FONT_SIZE_PT;
            return Float.parseFloat(value.trim()) / DEFAULT_FONT_SIZE;
        } catch (NumberFormatException ex) {
            Log.i(ex);
            return null;
        }
    }

    private static Integer parseColor(String value) {
        if (TextUtils.isEmpty(value))
            return null;

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
        boolean hasAlpha = false;
        try {
            if (TextUtils.isEmpty(c))
                return null;
            else if (c.startsWith("#")) {
                if (c.length() > 1) {
                    String code = c.substring(1);
                    if (x11ColorMap.containsKey(code)) // workaround
                        color = x11ColorMap.get(code);
                    else {
                        color = parseWebColor(c);
                        hasAlpha = true;
                    }
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

                    if (color != null && component.length >= 4) {
                        int alpha = Math.round(Float.parseFloat(component[3]) * 255);
                        color = ColorUtils.setAlphaComponent(color, alpha);
                        hasAlpha = true;
                    }
                }
            } else if (x11ColorMap.containsKey(c))
                color = x11ColorMap.get(c);
            else {
                color = parseWebColor(c);
                hasAlpha = true;
            }

            if (color != null && !hasAlpha)
                color = ColorUtils.setAlphaComponent(color, 255);

            if (BuildConfig.DEBUG)
                Log.i("Color " + c + "=" + (color == null ? null : encodeWebColor(color)));

        } catch (Throwable ex) {
            Log.i("Color=" + c + ": " + ex);
        }

        return color;
    }

    private static int parseWebColor(@NonNull String value) {
        if (value.startsWith("#"))
            value = value.substring(1);

        if (value.length() == 3)
            value = "FF" +
                    value.charAt(0) + value.charAt(0) +
                    value.charAt(1) + value.charAt(1) +
                    value.charAt(2) + value.charAt(2);
        else if (value.length() == 6)
            value = "FF" + value;
        else if (value.length() == 8)
            value = value.substring(6, 8) + value.substring(0, 6);
        else
            throw new IllegalArgumentException("Unknown color=" + value);

        return (int) Long.parseLong(value, 16);
    }

    static String encodeWebColor(int color) {
        int alpha = Color.alpha(color);
        int rgb = 0xFFFFFF & color;
        if (alpha == 0)
            return String.format("#%06X", rgb);
        else
            return String.format("#%06X%02X", rgb, alpha);
    }

    private static Integer adjustColor(boolean dark, int textColorPrimary, Integer color) {
        // Special case:
        //   shades of gray
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        float a = Color.alpha(color) / 255f;
        if (r == g && r == b && (dark ? 255 - r : r) * a < GRAY_THRESHOLD)
            color = textColorPrimary;

        return adjustLuminance(color, dark, MIN_LUMINANCE);
    }

    static int adjustLuminance(int color, boolean dark, float min) {
        int c = ColorUtils.compositeColors(color, dark ? Color.BLACK : Color.WHITE);
        float lum = (float) ColorUtils.calculateLuminance(c);
        if (dark ? lum < min : lum > 1 - min)
            color = ColorUtils.blendARGB(color,
                    dark ? Color.WHITE : Color.BLACK,
                    dark ? min - lum : lum - (1 - min));
        return color;
    }

    private static boolean hasColor(int color) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        return (Math.abs(r - g) >= COLOR_THRESHOLD ||
                Math.abs(r - b) >= COLOR_THRESHOLD);
    }

    // https://tools.ietf.org/html/rfc3676
    static String flow(String text) {
        boolean continuation = false;
        StringBuilder flowed = new StringBuilder();
        String[] lines = text.split("\\r?\\n");
        for (int l = 0; l < lines.length; l++) {
            String line = lines[l];
            lines[l] = null;

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
        return formatPre(text, true);
    }

    static String formatPre(String text, boolean view) {
        int level = 0;
        StringBuilder sb = new StringBuilder();
        String[] lines = text.split("\\r?\\n");
        for (int l = 0; l < lines.length; l++) {
            String line = lines[l];
            lines[l] = null;

            // Opening quotes
            // https://tools.ietf.org/html/rfc3676#section-4.5
            if (view) {
                int tlevel = 0;
                while (line.startsWith(">")) {
                    tlevel++;
                    if (tlevel > level)
                        sb.append("<blockquote>");

                    line = line.substring(1); // >

                    if (line.startsWith(" >"))
                        line = line.substring(1);
                }
                if (tlevel > 0)
                    if (line.length() > 0 && line.charAt(0) == ' ')
                        line = line.substring(1);

                // Closing quotes
                for (int i = 0; i < level - tlevel; i++)
                    sb.append("</blockquote>");
                level = tlevel;
            }

            // Tabs characters
            StringBuilder sbl = new StringBuilder();
            for (int j = 0; j < line.length(); j++) {
                char kar = line.charAt(j);
                if (kar == '\t') {
                    sbl.append(' ');
                    while (sbl.length() % TAB_SIZE != 0)
                        sbl.append(' ');
                } else
                    sbl.append(kar);
            }
            line = sbl.toString();

            // Html characters
            // This will handle spaces / word wrapping as well
            line = Html.escapeHtml(line);

            sb.append(line);
            if (view ||
                    l + 1 < lines.length ||
                    text.endsWith("\n"))
                sb.append("<br>");
        }

        // Closing quotes
        for (int i = 0; i < level; i++)
            sb.append("</blockquote>");

        return sb.toString();
    }

    static void removeTrackingPixels(Context context, Document document) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean disconnect_images = prefs.getBoolean("disconnect_images", false);

        Drawable d = context.getDrawable(R.drawable.twotone_my_location_24);
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
                if (host != null && !hosts.contains(host) &&
                        !isTrackingHost(host, disconnect_images))
                    hosts.add(host);
            }
        }

        // Images
        for (Element img : document.select("img")) {
            img.removeAttr("x-tracking");

            String src = img.attr("src");
            if (TextUtils.isEmpty(src))
                continue;

            Uri uri = Uri.parse(src);
            String host = uri.getHost();
            if (host == null || hosts.contains(host))
                continue;

            if (isTrackingPixel(img) || isTrackingHost(host, disconnect_images)) {
                img.attr("src", sb.toString());
                img.attr("alt", context.getString(R.string.title_legend_tracking_pixel));
                img.attr("height", "24");
                img.attr("width", "24");
                img.attr("style", "display:block !important; width:24px !important; height:24px !important;");
                img.attr("x-tracking", src);
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

    private static boolean isTrackingHost(String host, boolean disconnect_images) {
        if (TRACKING_HOSTS.contains(host))
            return true;
        if (disconnect_images && DisconnectBlacklist.isTracking(host))
            return true;
        return false;
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

    static void setViewport(Document document, boolean overview) {
        // https://developer.mozilla.org/en-US/docs/Mozilla/Mobile/Viewport_meta_tag
        // https://drafts.csswg.org/css-device-adapt/#viewport-meta
        Elements meta = document.select("meta").select("[name=viewport]");
        // Note that the browser will recognize meta elements in the body too
        if (overview) // fit width
            meta.remove();
        else {
            if (meta.size() == 1) {
                String content = meta.attr("content");
                String[] param = content.split("[;,]");
                for (int i = 0; i < param.length; i++) {
                    String[] kv = param[i].split("=");
                    if (kv.length == 2) {
                        String key = kv[0]
                                .replaceAll("\\s+", "")
                                .toLowerCase(Locale.ROOT);
                        switch (key) {
                            case "user-scalable":
                                kv[1] = "yes";
                                param[i] = TextUtils.join("=", kv);
                                break;
                            case "minimum-scale":
                            case "maximum-scale":
                                kv[0] = "disabled-scaling";
                                param[i] = TextUtils.join("=", kv);
                                break;
                        }
                    }
                }
                meta.attr("content", TextUtils.join(",", param));
            } else {
                meta.remove();
                document.head().prependElement("meta")
                        .attr("name", "viewport")
                        .attr("content", "width=device-width, initial-scale=1.0");
            }
        }

        if (BuildConfig.DEBUG)
            Log.i(document.head().html());
    }

    static String getLanguage(Context context, String subject, String text) {
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            boolean language_detection = prefs.getBoolean("language_detection", false);
            if (!language_detection)
                return null;

            StringBuilder sb = new StringBuilder();
            if (!TextUtils.isEmpty(subject))
                sb.append(subject).append('\n');
            if (!TextUtils.isEmpty(text))
                sb.append(text);
            if (sb.length() == 0)
                return null;

            Locale locale = TextHelper.detectLanguage(context, sb.toString());
            return (locale == null ? null : locale.getLanguage());
        } catch (Throwable ex) {
            Log.e(ex);
            return null;
        }
    }

    static String getPreview(String text) {
        if (text == null)
            return null;

        String preview = text
                .replace("\u200C", "") // Zero-width non-joiner
                .replaceAll("\\s+", " ");
        return truncate(preview, PREVIEW_SIZE);
    }

    static String getFullText(String body) {
        try {
            if (body == null)
                return null;
            Document d = JsoupEx.parse(body);
            return _getText(d);
        } catch (OutOfMemoryError ex) {
            Log.e(ex);
            return null;
        }
    }

    static String getFullText(File file) throws IOException {
        try {
            Document d = JsoupEx.parse(file);
            return _getText(d);
        } catch (OutOfMemoryError ex) {
            Log.e(ex);
            return null;
        }
    }

    private static String _getText(Document d) {
        truncate(d, MAX_FULL_TEXT_SIZE);

        for (Element bq : d.select("blockquote"))
            bq.prependChild(new TextNode("> "));

        return d.text();
    }

    static boolean hasBorder(Element e) {
        return "true".equals(e.attr("x-border"));
    }

    static void collapseQuotes(Document document) {
        document.body().filter(new NodeFilter() {
            private int level = 0;

            @Override
            public FilterResult head(Node node, int depth) {
                if (level > 0)
                    return FilterResult.REMOVE;

                if (node instanceof Element) {
                    Element element = (Element) node;
                    if ("blockquote".equals(element.tagName()) && hasBorder(element)) {
                        Element prev = element.previousElementSibling();
                        if (prev != null &&
                                "blockquote".equals(prev.tagName()) && hasBorder(prev))
                            return FilterResult.REMOVE;
                        else {
                            level++;
                            element.html("&#8230;");
                        }
                    }
                }

                return FilterResult.CONTINUE;
            }

            @Override
            public FilterResult tail(Node node, int depth) {
                if ("blockquote".equals(node.nodeName()))
                    level--;

                return FilterResult.CONTINUE;
            }
        });
    }

    static String truncate(String text, int at) {
        if (text.length() < at)
            return text;

        String preview = text.substring(0, at);
        int space = preview.lastIndexOf(' ');
        if (space > 0)
            preview = preview.substring(0, space + 1);
        return preview + "…";
    }

    @NonNull
    static String getText(Context context, String html) {
        Document d = sanitizeCompose(context, html, false);

        truncate(d, MAX_FORMAT_TEXT_SIZE);

        SpannableStringBuilder ssb = fromDocument(context, d, null, null);

        for (StyleSpan span : ssb.getSpans(0, ssb.length(), StyleSpan.class)) {
            int start = ssb.getSpanStart(span);
            int end = ssb.getSpanEnd(span);
            if (span.getStyle() == Typeface.ITALIC) {
                ssb.insert(end, "/");
                ssb.insert(start, "/");
            } else if (span.getStyle() == Typeface.BOLD) {
                ssb.insert(end, "*");
                ssb.insert(start, "*");
            }
        }

        for (UnderlineSpan span : ssb.getSpans(0, ssb.length(), UnderlineSpan.class)) {
            int start = ssb.getSpanStart(span);
            int end = ssb.getSpanEnd(span);
            ssb.insert(end, "_");
            ssb.insert(start, "_");
        }

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

        // https://tools.ietf.org/html/rfc3676#section-4.5
        for (QuoteSpan span : ssb.getSpans(0, ssb.length(), QuoteSpan.class)) {
            int start = ssb.getSpanStart(span);
            int end = ssb.getSpanEnd(span);

            for (int i = end - 2; i >= start; i--)
                if (ssb.charAt(i) == '\n')
                    if (i + 1 < ssb.length() && ssb.charAt(i + 1) == '>')
                        ssb.insert(i + 1, ">");
                    else
                        ssb.insert(i + 1, "> ");

            if (start < ssb.length())
                ssb.insert(start, ssb.charAt(start) == '>' ? ">" : "> ");
        }

        for (BulletSpan span : ssb.getSpans(0, ssb.length(), BulletSpan.class)) {
            int start = ssb.getSpanStart(span);
            if (span instanceof NumberSpan) {
                NumberSpan ns = (NumberSpan) span;
                ssb.insert(start, ns.getIndex() + ". ");
                int level = ns.getLevel();
                for (int l = 1; l <= level; l++)
                    ssb.insert(start, "\t");
            } else {
                ssb.insert(start, "* ");
                if (span instanceof BulletSpanEx) {
                    BulletSpanEx bs = (BulletSpanEx) span;
                    int level = bs.getLevel();
                    for (int l = 1; l <= level; l++)
                        ssb.insert(start, "\t");
                }
            }
        }

        return ssb.toString();
    }

    static Spanned highlightHeaders(Context context, String headers, boolean blocklist) {
        SpannableStringBuilder ssb = new SpannableStringBuilder(headers);
        int textColorLink = Helper.resolveColor(context, android.R.attr.textColorLink);

        int index = 0;
        for (String line : headers.split("\n")) {
            if (line.length() > 0 && !Character.isWhitespace(line.charAt(0))) {
                int colon = line.indexOf(':');
                if (colon > 0)
                    ssb.setSpan(new ForegroundColorSpan(textColorLink), index, index + colon, 0);
            }
            index += line.length() + 1;
        }

        try {
            // https://datatracker.ietf.org/doc/html/rfc2821#section-4.4
            final List<String> words = Collections.unmodifiableList(Arrays.asList(
                    "from", "by", "via", "with", "id", "for"));
            final DateFormat DTF = Helper.getDateTimeInstance(context, DateFormat.SHORT, DateFormat.MEDIUM);

            ByteArrayInputStream bis = new ByteArrayInputStream(headers.getBytes());
            String[] received = new InternetHeaders(bis).getHeader("Received");
            if (received.length > 0) {
                ssb.append('\n');
                for (int i = received.length - 1; i >= 0; i--) {
                    String h = MimeUtility.unfold(received[i]);

                    int semi = h.lastIndexOf(';');
                    Date date = null;
                    if (semi > 0) {
                        MailDateFormat mdf = new MailDateFormat();
                        date = mdf.parse(h, new ParsePosition(semi + 1));
                        h = h.substring(0, semi);
                    }

                    int s = ssb.length();
                    ssb.append('#').append(Integer.toString(received.length - i));
                    if (date != null)
                        ssb.append(' ').append(DTF.format(date));
                    ssb.setSpan(new StyleSpan(Typeface.BOLD), s, ssb.length(), 0);

                    if (blocklist && i == received.length - 1) {
                        Drawable d = context.getDrawable(R.drawable.twotone_flag_24);

                        int iconSize = context.getResources().getDimensionPixelSize(R.dimen.menu_item_icon_size);
                        d.setBounds(0, 0, iconSize, iconSize);

                        int colorWarning = Helper.resolveColor(context, R.attr.colorWarning);
                        d.setTint(colorWarning);

                        ssb.append(" \uFFFC"); // Object replacement character
                        ssb.setSpan(new ImageSpan(d), ssb.length() - 1, ssb.length(), 0);

                        if (!TextUtils.isEmpty(BuildConfig.MXTOOLBOX_URI)) {
                            final String header = received[i];
                            ClickableSpan click = new ClickableSpan() {
                                @Override
                                public void onClick(@NonNull View widget) {
                                    DnsBlockList.show(widget.getContext(), header);
                                }
                            };
                            ssb.setSpan(click, ssb.length() - 1, ssb.length(), 0);
                        }
                    }

                    ssb.append('\n');

                    int j = 0;
                    String[] w = h.split("\\s+");
                    while (j < w.length) {
                        if (j > 0)
                            ssb.append(' ');

                        s = ssb.length();
                        ssb.append(w[j]);
                        if (words.contains(w[j].toLowerCase(Locale.ROOT)))
                            ssb.setSpan(new ForegroundColorSpan(textColorLink), s, ssb.length(), 0);
                        j++;
                    }
                    ssb.append("\n\n");
                }
            }
        } catch (Throwable ex) {
            Log.w(ex);
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

    static void quoteLimit(Document d, int maxLevel) {
        for (Element bq : d.select("blockquote")) {
            int level = 1;
            Element parent = bq.parent();
            while (parent != null) {
                if ("blockquote".equals(parent.tagName())) // TODO: indentation
                    level++;
                parent = parent.parent();
            }
            if (level >= maxLevel)
                bq.html("&#8230;");
        }
    }

    static boolean truncate(Document d, int max) {
        final int[] length = new int[1];

        NodeTraversor.filter(new NodeFilter() {
            @Override
            public FilterResult head(Node node, int depth) {
                if (length[0] >= max)
                    return FilterResult.REMOVE;
                else if (node instanceof TextNode) {
                    TextNode tnode = ((TextNode) node);
                    String text = tnode.getWholeText();
                    if (length[0] + text.length() >= max) {
                        text = text.substring(0, max - length[0]) + " ...";
                        tnode.text(text);
                        length[0] += text.length();
                        return FilterResult.SKIP_ENTIRELY;
                    } else
                        length[0] += text.length();
                }
                return FilterResult.CONTINUE;
            }

            @Override
            public FilterResult tail(Node node, int depth) {
                return FilterResult.CONTINUE;
            }
        }, d.body());

        Log.i("Message size=" + length[0]);
        return (length[0] > max);
    }

    static boolean contains(Document d, String[] texts) {
        Map<String, Boolean> condition = new HashMap<>();
        for (String t : texts)
            condition.put(t, false);

        for (Element elm : d.body().select("*"))
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

    static SpannableStringBuilder fromDocument(
            Context context, @NonNull Document document,
            @Nullable Html.ImageGetter imageGetter, @Nullable Html.TagHandler tagHandler) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean debug = prefs.getBoolean("debug", false);
        boolean monospaced_pre = prefs.getBoolean("monospaced_pre", false);

        final int colorPrimary = Helper.resolveColor(context, R.attr.colorPrimary);
        final int colorAccent = Helper.resolveColor(context, R.attr.colorAccent);
        final int colorBlockquote = Helper.resolveColor(context, R.attr.colorBlockquote, colorPrimary);
        final int colorSeparator = Helper.resolveColor(context, R.attr.colorSeparator);
        int bulletGap = context.getResources().getDimensionPixelSize(R.dimen.bullet_gap_size);
        int bulletRadius = context.getResources().getDimensionPixelSize(R.dimen.bullet_radius_size);
        int bulletIndent = context.getResources().getDimensionPixelSize(R.dimen.bullet_indent_size);
        int intentSize = context.getResources().getDimensionPixelSize(R.dimen.indent_size);
        int quoteGap = context.getResources().getDimensionPixelSize(R.dimen.quote_gap_size);
        int quoteStripe = context.getResources().getDimensionPixelSize(R.dimen.quote_stripe_width);
        int line_dash_length = context.getResources().getDimensionPixelSize(R.dimen.line_dash_length);

        int message_zoom = prefs.getInt("message_zoom", 100);
        float textSize = Helper.getTextSize(context, 0) * message_zoom / 100f;

        // https://developer.mozilla.org/en-US/docs/Web/HTML/Block-level_elements
        NodeTraversor.traverse(new NodeVisitor() {
            private Element element;
            private int plain = 0;
            private List<TextNode> block = new ArrayList<>();

            private final Pattern FOLD_WHITESPACE = Pattern.compile("[ \t\f\r\n]+");

            // https://developer.mozilla.org/en-US/docs/Web/HTML/Block-level_elements

            @Override
            public void head(Node node, int depth) {
                if (node instanceof TextNode) {
                    if (plain == 0)
                        block.add((TextNode) node);
                } else if (node instanceof Element) {
                    element = (Element) node;
                    if ("true".equals(element.attr("x-plain")))
                        plain++;
                    if (element.isBlock() /* x-block is never false */ ||
                            "true".equals(element.attr("x-block"))) {
                        normalizeText(block);
                        block.clear();
                    }
                }
            }

            @Override
            public void tail(Node node, int depth) {
                if (node instanceof Element) {
                    element = (Element) node;
                    if ("true".equals(element.attr("x-plain")))
                        plain--;
                    if (element.isBlock() /* x-block is never false */ ||
                            "true".equals(element.attr("x-block")) ||
                            "br".equals(element.tagName())) {
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
                        tnode.text(text);
                        i++;
                        continue;
                    }

                    // Fold white space
                    text = FOLD_WHITESPACE.matcher(text).replaceAll(" ");

                    // Conditionally remove leading whitespace
                    if (isSpace(text, 0) &&
                            (i == 0 || endsWithSpace(block.get(i - 1).text())))
                        text = text.substring(1);

                    tnode.text(text);

                    if (TextUtils.isEmpty(text))
                        block.remove(i);
                    else
                        i++;
                }

                // Remove trailing whitespace
                while (block.size() > 0) {
                    tnode = block.get(block.size() - 1);
                    text = tnode.getWholeText();
                    if (endsWithSpace(text)) {
                        text = text.substring(0, text.length() - 1);
                        tnode.text(text);
                    }
                    if (TextUtils.isEmpty(text))
                        block.remove(block.size() - 1);
                    else
                        break;
                }

                // Remove all blank blocks
                boolean blank = true;
                for (int i = 0; i < block.size(); i++) {
                    text = block.get(i).getWholeText();
                    for (int j = 0; j < text.length(); j++) {
                        char kar = text.charAt(j);
                        if (kar != ' ') {
                            blank = false;
                            break;
                        }
                    }
                }
                if (blank)
                    for (int i = 0; i < block.size(); i++)
                        block.get(i).text("");

                if (debug) {
                    if (block.size() > 0) {
                        TextNode first = block.get(0);
                        TextNode last = block.get(block.size() - 1);
                        first.text("(" + first.getWholeText());
                        last.text(last.getWholeText() + ")");
                    }
                }
            }

            boolean isSpace(String text, int index) {
                if (index < 0 || index >= text.length())
                    return false;
                return (text.charAt(index) == ' ');
            }

            boolean endsWithSpace(String text) {
                return isSpace(text, text.length() - 1);
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

                    if ("true".equals(element.attr("x-block")))
                        if (ssb.length() > 0 && ssb.charAt(ssb.length() - 1) != '\n')
                            ssb.append('\n');

                    if ("true".equals(element.attr("x-paragraph")) &&
                            !"false".equals(element.attr("x-line-before")))
                        if (ssb.length() > 1 &&
                                (ssb.charAt(ssb.length() - 2) != '\n' ||
                                        ssb.charAt(ssb.length() - 1) != '\n'))
                            ssb.append('\n');

                    if ("true".equals(element.attr("x-line-before")) &&
                            ssb.length() > 0 && ssb.charAt(ssb.length() - 1) == '\n')
                        ssb.append('\n');

                    element.attr("start-index", Integer.toString(ssb.length()));

                    if (debug)
                        ssb.append("[" + element.tagName() + "/" + element.className() +
                                ":" + "bl=" + element.attr("x-block") +
                                ":" + "pa=" + element.attr("x-paragraph") +
                                ":" + "fo=" + element.attr("x-line-before") +
                                ":" + "af=" + element.attr("x-line-after") +
                                ":" + element.attr("style") + "]");
                } else if (node instanceof TextNode) {
                    tnode = (TextNode) node;
                    String text = tnode.getWholeText();
                    ssb.append(text);
                }
            }

            @Override
            public void tail(Node node, int depth) {
                if (node instanceof Element) {
                    element = (Element) node;
                    int start = Integer.parseInt(element.attr("start-index"));

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
                                case "background-color":
                                    if (!TextUtils.isEmpty(value))
                                        try {
                                            int color = parseWebColor(value);
                                            CharacterStyle span;
                                            if ("color".equals(key))
                                                span = new ForegroundColorSpan(color);
                                            else
                                                span = new BackgroundColorSpan(color);
                                            setSpan(ssb, span, start, ssb.length());
                                        } catch (NumberFormatException ex) {
                                            Log.i(ex);
                                        }
                                    break;
                                case "font-family":
                                    if ("wingdings".equalsIgnoreCase(value)) {
                                        for (int i = start; i < ssb.length(); i++) {
                                            int kar = ssb.charAt(i);
                                            if (kar >= 0x20 && kar < 0x20 + WINGDING_TO_UNICODE.length) {
                                                int codepoint = WINGDING_TO_UNICODE[kar - 0x20];
                                                String replacement = new String(Character.toChars(codepoint));
                                                if (replacement.length() == 1)
                                                    ssb.replace(i, i + 1, replacement);
                                            }
                                        }
                                    } else
                                        setSpan(ssb, StyleHelper.getTypefaceSpan(value, context), start, ssb.length());
                                    break;
                                case "text-decoration":
                                    if ("line-through".equals(value))
                                        setSpan(ssb, new StrikethroughSpan(), start, ssb.length());
                                    break;
                                case "text-align":
                                    // https://developer.mozilla.org/en-US/docs/Web/CSS/text-align
                                    Layout.Alignment alignment = null;
                                    boolean rtl = TextDirectionHeuristics.FIRSTSTRONG_LTR.isRtl(ssb, start, ssb.length() - start);
                                    switch (value) {
                                        case "left":
                                        case "start":
                                            alignment = (rtl ? Layout.Alignment.ALIGN_OPPOSITE : Layout.Alignment.ALIGN_NORMAL);
                                            break;
                                        case "center":
                                            alignment = Layout.Alignment.ALIGN_CENTER;
                                            break;
                                        case "right":
                                        case "end":
                                            alignment = (rtl ? Layout.Alignment.ALIGN_NORMAL : Layout.Alignment.ALIGN_OPPOSITE);
                                            break;
                                        case "justify":
                                            // Not supported by Android
                                            break;
                                    }
                                    if (alignment != null)
                                        setSpan(ssb, new AlignmentSpan.Standard(alignment), start, ssb.length());
                                    break;
                            }
                        }
                    }

                    // Apply calculated font size
                    String xFontSize = element.attr("x-font-size-rel");
                    if (!TextUtils.isEmpty(xFontSize)) {
                        Float fsize = Float.parseFloat(xFontSize);
                        if (fsize != 1.0f)
                            setSpan(ssb, new RelativeSizeSpan(fsize), start, ssb.length());
                    }

                    // Apply element
                    try {
                        String tag = element.tagName();
                        int semi = tag.indexOf(':');
                        if (semi >= 0)
                            tag = tag.substring(semi + 1);

                        switch (tag) {
                            case "a":
                                String href = element.attr("href");
                                if (!TextUtils.isEmpty(href))
                                    setSpan(ssb, new URLSpan(href), start, ssb.length());
                                break;
                            case "big":
                                setSpan(ssb, new RelativeSizeSpan(FONT_LARGE), start, ssb.length());
                                break;
                            case "blockquote":
                                if (start == 0 || ssb.charAt(start - 1) != '\n')
                                    ssb.insert(start++, "\n");
                                if (start == ssb.length())
                                    ssb.append(' ');
                                if (ssb.length() == 0 || ssb.charAt(ssb.length() - 1) != '\n')
                                    ssb.append("\n");

                                if (hasBorder(element)) {
                                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P)
                                        setSpan(ssb, new QuoteSpan(colorBlockquote), start, ssb.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                                    else
                                        setSpan(ssb, new QuoteSpan(colorBlockquote, quoteStripe, quoteGap), start, ssb.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                                } else
                                    setSpan(ssb, new IndentSpan(intentSize), start, ssb.length());
                                break;
                            case "br":
                                ssb.append('\n');
                                break;
                            case "body":
                                // Do nothing
                                break;
                            case "div": // signature
                                // Do nothing
                                break;
                            case "i":
                            case "em":
                                setSpan(ssb, new StyleSpan(Typeface.ITALIC), start, ssb.length());
                                break;
                            case "font":
                                String face = element.attr("face");
                                if (!TextUtils.isEmpty(face))
                                    setSpan(ssb, new TypefaceSpan(face), start, ssb.length());
                                break;
                            case "h1":
                            case "h2":
                            case "h3":
                            case "h4":
                            case "h5":
                            case "h6":
                                // Font size is already set
                                setSpan(ssb, new StyleSpan(Typeface.BOLD), start, ssb.length());
                                break;
                            case "hr":
                                // Suppress successive lines
                                LineSpan[] lines = ssb.getSpans(0, ssb.length(), LineSpan.class);
                                int last = -1;
                                if (lines != null)
                                    for (LineSpan line : lines) {
                                        int e = ssb.getSpanEnd(line);
                                        if (e > last)
                                            last = e;
                                    }
                                if (last >= 0) {
                                    boolean blank = true;
                                    for (int i = last; i < ssb.length(); i++) {
                                        char kar = ssb.charAt(i);
                                        if (kar != ' ' && kar != '\n' && kar != '\u00a0') {
                                            blank = false;
                                            break;
                                        }
                                    }

                                    if (blank)
                                        break;
                                }

                                boolean dashed = "true".equals(element.attr("x-dashed"));
                                float stroke = context.getResources().getDisplayMetrics().density;
                                float dash = (dashed ? line_dash_length : 0f);
                                ssb.append(LINE);
                                setSpan(ssb, new LineSpan(colorSeparator, stroke, dash), start, ssb.length());
                                break;
                            case "img":
                                String src = element.attr("src");
                                if (!TextUtils.isEmpty(src)) {
                                    Drawable d = (imageGetter == null
                                            ? context.getDrawable(R.drawable.twotone_broken_image_24)
                                            : imageGetter.getDrawable(src));
                                    ssb.insert(start, "\uFFFC"); // Object replacement character
                                    setSpan(ssb, new ImageSpan(d, src), start, start + 1);
                                }
                                break;
                            case "li":
                                if (start == 0 || ssb.charAt(start - 1) != '\n')
                                    ssb.insert(start++, "\n");
                                if (ssb.length() == 0 || ssb.charAt(ssb.length() - 1) != '\n')
                                    ssb.append("\n");

                                int level = 0;
                                Element type = null;
                                Element parent = element.parent();
                                while (parent != null) {
                                    if ("ol".equals(parent.tagName()) || "ul".equals(parent.tagName())) {
                                        level++;
                                        if (type == null)
                                            type = parent;
                                    }
                                    parent = parent.parent();
                                }
                                if (level > 0)
                                    level--;

                                if (type == null || "ul".equals(type.tagName())) {
                                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P)
                                        setSpan(ssb, new BulletSpanEx(bulletIndent, bulletGap, colorAccent, level), start, ssb.length());
                                    else
                                        setSpan(ssb, new BulletSpanEx(bulletIndent, bulletGap, colorAccent, bulletRadius, level), start, ssb.length());
                                } else {
                                    // https://developer.mozilla.org/en-US/docs/Web/HTML/Element/ol
                                    int index = 0;
                                    String s = type.attr("start");
                                    if (!TextUtils.isEmpty(s) && TextUtils.isDigitsOnly(s))
                                        index = Integer.parseInt(s) - 1;
                                    for (Node child : type.childNodes()) {
                                        if (child instanceof Element &&
                                                child.nodeName().equals(element.tagName())) {
                                            index++;
                                            if (child == element)
                                                break;
                                        }
                                    }

                                    setSpan(ssb, new NumberSpan(bulletIndent, bulletGap, colorAccent, textSize, level, index), start, ssb.length());
                                }

                                break;
                            case "pre":
                                // Signature
                                setSpan(ssb, new TypefaceSpan("monospace"), start, ssb.length());
                                break;
                            case "style":
                                // signatures
                                break;
                            case "ol":
                            case "ul":
                                break;
                            case "meta":
                                // Signature
                                break;
                            case "p":
                                // Signature
                                break;
                            case "small":
                                setSpan(ssb, new RelativeSizeSpan(FONT_SMALL), start, ssb.length());
                                break;
                            case "span":
                                // Do nothing
                                break;
                            case "sub":
                                setSpan(ssb, new SubscriptSpan(), start, ssb.length());
                                setSpan(ssb, new RelativeSizeSpan(FONT_SMALL), start, ssb.length());
                                break;
                            case "sup":
                                setSpan(ssb, new SuperscriptSpan(), start, ssb.length());
                                setSpan(ssb, new RelativeSizeSpan(FONT_SMALL), start, ssb.length());
                                break;
                            case "table":
                            case "thead":
                            case "tbody":
                            case "tfoot":
                            case "tr":
                            case "th":
                            case "td":
                                // Signature
                                break;
                            case "b":
                            case "code": // Signature
                            case "strong":
                                setSpan(ssb, new StyleSpan(Typeface.BOLD), start, ssb.length());
                                break;
                            case "s":
                            case "del":
                            case "strike":
                                setSpan(ssb, new StrikethroughSpan(), start, ssb.length());
                                break;
                            case "title":
                                // Signature, etc
                                break;
                            case "tt":
                                setSpan(ssb, new TypefaceSpan("monospace"), start, ssb.length());
                                break;
                            case "u":
                                setSpan(ssb, new UnderlineSpan(), start, ssb.length());
                                break;
                            default:
                                Log.w("Unknown tag=" + element.tagName());
                        }

                        if (monospaced_pre &&
                                "true".equals(element.attr("x-plain")))
                            setSpan(ssb, new TypefaceSpan("monospace"), start, ssb.length());
                    } catch (Throwable ex) {
                        Log.e(ex);
                    }

                    if ("true".equals(element.attr("x-block")))
                        if (ssb.length() > 0 && ssb.charAt(ssb.length() - 1) != '\n')
                            ssb.append('\n');

                    if ("true".equals(element.attr("x-paragraph")) &&
                            !"false".equals(element.attr("x-line-after")))
                        if (ssb.length() > 1 &&
                                (ssb.charAt(ssb.length() - 2) != '\n' ||
                                        ssb.charAt(ssb.length() - 1) != '\n'))
                            ssb.append('\n');

                    if ("true".equals(element.attr("x-line-after")) &&
                            ssb.length() > 0 && ssb.charAt(ssb.length() - 1) == '\n')
                        ssb.append('\n');

                    if ("true".equals(element.attr("x-column")) &&
                            ssb.length() > 1 &&
                            ssb.charAt(ssb.length() - 1) != '\n' &&
                            ssb.charAt(ssb.length() - 1) != '\u00a0' &&
                            ssb.charAt(ssb.length() - 1) != '\u2002')
                        ssb.append('\u2002'); // ensp

                    if (debug)
                        ssb.append("[/" + element.tagName() + "]");
                }
            }

            private void setSpan(SpannableStringBuilder ssb, Object span, int start, int end) {
                setSpan(ssb, span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            private void setSpan(SpannableStringBuilder ssb, Object span, int start, int end, int flags) {
                if (start == end)
                    return;
                int len = ssb.length();
                if (start >= 0 && start < len && end <= len)
                    ssb.setSpan(span, start, end, flags);
                else
                    Log.e("Invalid span " + start + "..." + end + " len=" + len + " type=" + span.getClass().getName());
            }
        }, document.body());

        if (debug)
            for (int i = ssb.length() - 1; i >= 0; i--) {
                char kar = ssb.charAt(i);
                if (kar == '\n')
                    ssb.insert(i, "|");
                else if (kar == ' ')
                    ssb.replace(i, i + 1, "_");
                else if (kar == '\u00A0')
                    ssb.replace(i, i + 1, "•");
                else if (!Helper.isPrintableChar(kar))
                    ssb.replace(i, i + 1, "{" + Integer.toHexString(kar) + "}");
            }

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
        for (int i = spans.length - 1; i >= 0; i--) {
            int s = start.get(spans[i]);
            int e = end.get(spans[i]);
            int f = flags.get(spans[i]);
            if (spans[i] instanceof AlignmentSpan ||
                    spans[i] instanceof BulletSpan ||
                    spans[i] instanceof NumberSpan) {
                if (spans[i] instanceof AlignmentSpan &&
                        !(e > 0 && ssb.charAt(e - 1) == '\n') &&
                        e < ssb.length() && ssb.charAt(e) == '\n')
                    e++;
                if (s > 0 && ssb.charAt(s - 1) == '\n' &&
                        e > 0 && ssb.charAt(e - 1) == '\n')
                    f |= Spanned.SPAN_PARAGRAPH;
            }
            ssb.setSpan(spans[i], s, e, f);
        }

        return ssb;
    }

    static void clearAnnotations(Document d) {
        d.select("*")
                .removeAttr("x-background")
                .removeAttr("x-color")
                .removeAttr("x-block")
                .removeAttr("x-inline")
                .removeAttr("x-paragraph")
                .removeAttr("x-font-size")
                .removeAttr("x-font-size-rel")
                .removeAttr("x-line-before")
                .removeAttr("x-line-after")
                .removeAttr("x-align")
                .removeAttr("x-column")
                .removeAttr("x-dashed")
                .removeAttr("x-tracking")
                .removeAttr("x-border");
    }

    static Spanned fromHtml(@NonNull String html, Context context) {
        return fromHtml(html, null, null, context);
    }

    static Spanned fromHtml(@NonNull String html, @Nullable Html.ImageGetter imageGetter, @Nullable Html.TagHandler tagHandler, Context context) {
        Document document = JsoupEx.parse(html);
        return fromDocument(context, document, imageGetter, tagHandler);
    }

    static String toHtml(Spanned spanned, Context context) {
        HtmlEx converter = new HtmlEx(context);
        String html = converter.toHtml(spanned, TO_HTML_PARAGRAPH_LINES_INDIVIDUAL);

        Document doc = JsoupEx.parse(html);
        for (Element span : doc.select("span")) {
            String style = span.attr("style");
            if (TextUtils.isEmpty(style))
                continue;

            StringBuilder sb = new StringBuilder();

            String[] params = style.split(";");
            for (String param : params) {
                int semi = param.indexOf(":");
                if (semi < 0) {
                    sb.append(param).append(';');
                    continue;
                }

                String key = param.substring(0, semi).trim();
                String value = param.substring(semi + 1).trim();

                switch (key) {
                    case "font-size":
                        // @Google: why convert size to and from in a different way?
                        String v = value.replace(',', '.');
                        Float size = getFontSize(v, 1.0f);
                        if (size != null) {
                            if (size < 1.0f)
                                span.tagName("small");
                            else if (size > 1.0f)
                                span.tagName("big");
                        }
                        break;
                    case "text-align":
                        sb.append(" display:block;");
                        // fall through
                    default:
                        sb.append(param).append(';');
                }

                if (sb.length() == 0)
                    span.removeAttr("style");
                else
                    span.attr("style", sb.toString());
            }
        }

        for (Element quote : doc.select("blockquote")) {
            Element prev = quote.previousElementSibling();
            if (prev != null && "br".equals(prev.tagName()))
                prev.remove();

            Element last = quote.children().last();
            if (last != null && "br".equals(last.tagName()))
                last.remove();
        }

        Log.i("MMM " + doc.html());

        return doc.html();
    }

    private static Spanned reverseSpans(Spanned spanned) {
        Object[] spans = spanned.getSpans(0, spanned.length(), Object.class);
        Spannable reverse = Spannable.Factory.getInstance().newSpannable(spanned.toString());
        if (spans != null && spans.length > 0)
            for (int i = spans.length - 1; i >= 0; i--)
                reverse.setSpan(
                        spans[i],
                        spanned.getSpanStart(spans[i]),
                        spanned.getSpanEnd(spans[i]),
                        spanned.getSpanFlags(spans[i]));
        return reverse;
    }
}
