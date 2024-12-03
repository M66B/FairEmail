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

    Copyright 2018-2024 by Marcel Bokhorst (M66B)
*/

import static androidx.core.text.HtmlCompat.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL;
import static org.w3c.css.sac.Condition.SAC_CLASS_CONDITION;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
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
import android.text.style.AbsoluteSizeSpan;
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
import android.text.style.TypefaceSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.util.PatternsCompat;
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MailDateFormat;
import javax.mail.internet.MimeUtility;

public class HtmlHelper {
    static final int PREVIEW_SIZE = 500; // characters

    // https://drafts.csswg.org/css-fonts/#absolute-size-mapping
    static final float FONT_XSMALL = 0.6f; // 10px=0.625
    static final float FONT_SMALL = 0.8f; // 13px=0.8125
    // 16 px
    static final float FONT_LARGE = 1.25f; // 20px=1.2
    static final float FONT_XLARGE = 1.50f; // 24px=1.5

    static final int MAX_FULL_TEXT_SIZE = 1024 * 1024; // characters
    static final int MAX_SHARE_TEXT_SIZE = 50 * 1024; // characters
    static final int MAX_TRANSLATABLE_TEXT_SIZE = 50 * 1024; // characters

    static final float MIN_LUMINANCE_VIEW = 0.7f;
    static final float MIN_LUMINANCE_COMPOSE = 0.85f;

    private static final int DEFAULT_FONT_SIZE = 16; // pixels
    private static final int DEFAULT_FONT_SIZE_PT = 12; // points
    private static final int GRAY_THRESHOLD = Math.round(255 * 0.2f);
    private static final int COLOR_THRESHOLD = Math.round(255 * 0.1f);
    private static final int TAB_SIZE = 4;
    private static final int MAX_ALT = 250;
    private static final int MAX_AUTO_LINK = 250;
    private static final int MAX_FORMAT_TEXT_SIZE = 100 * 1024; // characters
    private static final int SMALL_IMAGE_SIZE = 5; // pixels
    private static final int TRACKING_PIXEL_SURFACE = 25; // pixels
    private static final float[] HEADING_SIZES = {1.5f, 1.4f, 1.3f, 1.2f, 1.1f, 1f};
    private static final String LINE = "----------------------------------------";
    private static final String W3NS = /* http/https */ "://www.w3.org/";

    private static final HashMap<String, Integer> x11ColorMap = new HashMap<>();

    // https://www.w3.org/TR/CSS21/propidx.html
    private static final List<String> STYLE_NO_INHERIT = Collections.unmodifiableList(Arrays.asList(
            "background-attachment", "background-color", "background-image", "background-position", "background-repeat", "background",
            "border-color", "border-style", "border-top", "border-right", "border-bottom", "border-left",
            "border-top-color", "border-right-color", "border-bottom-color", "border-left-color",
            "border-top-style", "border-right-style", "border-bottom-style", "border-left-style",
            "border-top-width", "border-right-width", "border-bottom-width", "border-left-width",
            "border-width", "border",
            "bottom",
            "clear",
            "clip",
            "content",
            "counter-increment", "counter-reset",
            "cue-after", "cue-before", "cue",
            "display",
            "float",
            "height",
            "left",
            "margin-right", "margin-left", "margin-top", "margin-bottom", "margin",
            "max-height", "max-width", "min-height", "min-width",
            "outline-color", "outline-style", "outline-width", "outline",
            "overflow",
            "padding-top", "padding-right", "padding-bottom", "padding-left",
            "padding", "page-break-after", "page-break-before", "page-break-inside",
            "pause-after", "pause-before", "pause",
            "play-during",
            "position",
            "right",
            "table-layout",
            "text-decoration",
            "top",
            "unicode-bidi",
            "vertical-align",
            "width",
            "z-index"
    ));

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

    private static final List<String> TRACKING_HOSTS = Collections.unmodifiableList(Arrays.asList(
            "www.google-analytics.com"
    ));

    static Map<Integer, Integer> MAP_WINGDINGS;

    static {
        // http://www.alanwood.net/demos/wingdings.html
        // https://unicode.org/L2/L2011/11052r-wingding.pdf
        Map<Integer, Integer> map = new HashMap<>();
        map.put(37, 0x1F514); // Bell
        map.put(39, 0x1F56F); // Candle
        map.put(44, 0x1F4EA); // Closed mailbox with lowered flag
        map.put(45, 0x1F4EB); // Closed mailbox with raised flag
        map.put(46, 0x1F4EC); // Open mailbox with raised flag
        map.put(47, 0x1F4ED); // Open mailbox with lowered flag
        map.put(48, 0x1F4C1); // Folder
        map.put(49, 0x1F4C2); // Open folder
        map.put(53, 0x1F5C4); // File cabinet
        map.put(54, 0x231B); // Hourglass
        map.put(57, 0x1F5B2); // Trackball
        map.put(58, 0x1F5A5); // Computer
        map.put(65, 0x270C); // Victory hand
        map.put(66, 0x1F44C); // OK hand
        map.put(67, 0x1F44D); // Thumb up
        map.put(68, 0x1F44E); // Thumb down
        map.put(69, 0x1F448); // Pointing left
        map.put(70, 0x1F449); // Pointing right
        map.put(71, 0x261D); // Pointing up
        map.put(72, 0x1F447); // Pointing down
        map.put(73, 0x1F590); // Raised hand
        map.put(74, 0x1F642); // Smiling face
        map.put(75, 0x1F610); // Neutral face
        map.put(76, 0x1F641); // Frowning face
        map.put(77, 0x1F4A3); // Bomb
        map.put(83, 0x1F4A7); // Droplet
        map.put(84, 0x2744); // Snowflake
        map.put(94, 0x2648); // Aries
        map.put(95, 0x2649); // Taurus
        map.put(96, 0x264A); // Gemini
        map.put(97, 0x264B); // Cancer
        map.put(98, 0x264C); // Leo
        map.put(99, 0x264D); // Virgo
        map.put(100, 0x264E); // Libra
        map.put(101, 0x264F); // Scorpio
        map.put(102, 0x2650); // Sagittarius
        map.put(103, 0x2651); // Capricorn
        map.put(104, 0x2652); // Aquarius
        map.put(105, 0x2653); // Pisces
        map.put(183, 0x1F550); // Clock 1
        map.put(184, 0x1F551); // Clock 2
        map.put(185, 0x1F552); // Clock 3
        map.put(186, 0x1F553); // Clock 4
        map.put(187, 0x1F554); // Clock 5
        map.put(188, 0x1F555); // Clock 6
        map.put(189, 0x1F556); // Clock 7
        map.put(190, 0x1F557); // Clock 8
        map.put(191, 0x1F558); // Clock 9
        map.put(192, 0x1F559); // Clock 10
        map.put(193, 0x1F55A); // Clock 11
        map.put(194, 0x1F55B); // Clock 12
        map.put(251, 0x274C); // Red cross
        map.put(252, 0x2705); // Green check
        MAP_WINGDINGS = Collections.unmodifiableMap(map);
    }

    static Document sanitizeCompose(Context context, String html, boolean show_images) {
        return sanitizeCompose(context, JsoupEx.parse(html), show_images);
    }

    static Document sanitizeCompose(Context context, Document parsed, boolean show_images) {
        try {
            return sanitize(context, parsed, false, show_images);
        } catch (Throwable ex) {
            // OutOfMemoryError
            Log.e(ex);
            Document document = Document.createShell("");
            Element strong = document.createElement("strong");
            strong.text(new ThrowableWrapper(ex).getSafeStackTraceString());
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
            strong.text(new ThrowableWrapper(ex).getSafeStackTraceString());
            document.body().appendChild(strong);
            return document;
        }
    }

    private static int getMaxFormatTextSize(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean ignore_formatted_size = prefs.getBoolean("ignore_formatted_size", false);
        if (ignore_formatted_size)
            return Integer.MAX_VALUE;

        ActivityManager am = Helper.getSystemService(context, ActivityManager.class);
        int mc = am.getMemoryClass();
        if (mc >= 256)
            return MAX_FORMAT_TEXT_SIZE;
        else
            return mc * MAX_FORMAT_TEXT_SIZE / 256;
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
        normalizeNamespaces(parsed, display_hidden);

        // Limit length
        if (view && truncate(parsed, getMaxFormatTextSize(context))) {
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
                .addTags("hr", "abbr", "big", "font", "dfn", "ins", "del", "s", "tt", "mark", "address", "input", "samp")
                .addAttributes(":all", "class")
                .addAttributes(":all", "style")
                .addAttributes("span", "dir")
                .addAttributes("li", "dir")
                .addAttributes("div", "x-plain")
                .addAttributes("input", "type")
                .addAttributes("input", "checked")
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
            removeTrackingPixels(context, document);

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
            style = mergeStyles(style, element.attr("style"));

            if ("fairemail_debug_info".equals(clazz))
                style = mergeStyles(style, "font-size: smaller");

            if (text_align) {
                // https://developer.mozilla.org/en-US/docs/Web/HTML/Element/center
                if ("center".equals(element.tagName())) {
                    style = mergeStyles(style, "text-align:center");
                    element.tagName("div");
                } else if ("table".equals(element.tagName())) {
                    if (!element.attr("style").contains("text-align"))
                        style = mergeStyles(style, "text-align:left");
                } else {
                    // https://developer.mozilla.org/en-US/docs/Web/HTML/Attributes
                    String align = element.attr("align");
                    if (!TextUtils.isEmpty(align))
                        style = mergeStyles("text-align:" + align, style);
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

                            Integer color = parseColor(value);

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
                                            bg = parseWebColor(e.attr("x-background"));
                                        else
                                            e = e.parent();
                                }

                                if (!view &&
                                        color != null && (bg == null || bg == Color.TRANSPARENT)) {
                                    // Special case:
                                    //   external draft: very dark/light font
                                    double lum = ColorUtils.calculateLuminance(color);
                                    if (dark ? lum < 1 - MIN_LUMINANCE_COMPOSE : lum > MIN_LUMINANCE_COMPOSE)
                                        color = null;
                                }

                                if (bg == null) {
                                    if (color != null && view)
                                        color = adjustColor(dark, textColorPrimary, color);
                                } else if (bg == Color.TRANSPARENT) {
                                    // Background color was suppressed because "no color"
                                    if (color != null) {
                                        double lum = ColorUtils.calculateLuminance(color);
                                        if (dark ? lum < 1 - MIN_LUMINANCE_VIEW : lum > MIN_LUMINANCE_VIEW)
                                            color = textColorPrimary;
                                    }
                                }

                                if (color != null) {
                                    double lum = ColorUtils.calculateLuminance(color);
                                    if (dark ? lum > 1 - MIN_LUMINANCE_VIEW : lum < MIN_LUMINANCE_VIEW)
                                        element.attr("x-color", "true");
                                }
                            } else /* background */ {
                                if (color != null && !hasColor(color))
                                    color = Color.TRANSPARENT;

                                if (color != null)
                                    element.attr("x-background", encodeWebColor(color));

                                if (color != null && dark) {
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
                                    //   force (inverse) text color
                                    if (!fg) {
                                        double lum = (color == Color.TRANSPARENT ? 0 : ColorUtils.calculateLuminance(color));
                                        int c = (lum < 0.5 ? textColorPrimary : textColorPrimaryInverse);
                                        sb.append("color")
                                                .append(':')
                                                .append(encodeWebColor(c))
                                                .append(";");
                                    }
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
                                            fsize = (fsize < FONT_SMALL ? FONT_XSMALL : FONT_SMALL);
                                        else if (fsize > 1)
                                            fsize = (fsize > FONT_LARGE ? FONT_XLARGE : FONT_LARGE);
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
                                                for (int i = 0; i < text.length(); ) {
                                                    int codepoint = text.codePointAt(i);
                                                    if (Character.isLetter(codepoint)) {
                                                        tnode.text(text.substring(0, i) +
                                                                text.substring(i, i + 1).toUpperCase(Locale.ROOT) +
                                                                text.substring(i + 1));
                                                        break;
                                                    } else if (!Character.isWhitespace(codepoint))
                                                        break;
                                                    else
                                                        i += Character.charCount(codepoint);
                                                }
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
                                    p[i] = getFontSize(v[i], 1.0f);

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

                        case "white-space":
                            // https://developer.mozilla.org/en-US/docs/Web/CSS/white-space
                            if ("pre".equals(value) ||
                                    "pre-wrap".equals(value) ||
                                    "break-spaces".equals(value))
                                element.attr("x-plain", "true");
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
        for (Element h : hs)
            if (!"false".equals(h.attr("x-line-before")))
                h.attr("x-line-before", "true");
        if (text_size && view) {
            if (text_separators)
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
                if (!"false".equals(p.attr("x-line-after")))
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
        // https://developer.mozilla.org/en-US/docs/Web/HTML/Element/samp
        for (Element pre : document.select("pre,samp")) {
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
                                    while ((index % TAB_SIZE) != 0);
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

                Elements cols = row.children();
                if (cols.size() == 2 &&
                        "th".equals(cols.get(0).tagName()) &&
                        "td".equals(cols.get(1).tagName())) {
                    for (Element col : cols) {
                        col.attr("x-align", "left");
                        col.attr("style",
                                mergeStyles(col.attr("text-align"), "text-align: left;"));
                    }
                }

                Element separate = null;
                List<Node> merge = new ArrayList<>();
                for (Element col : cols) {
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

                        if (cols.size() > 1 &&
                                lonely instanceof TextNode &&
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

    static void removeRelativeLinks(Document document) {
        // https://developer.mozilla.org/en-US/docs/Web/HTML/Element/base
        Elements b = document.select("base");
        String base = (b.size() > 0 ? b.get(0).attr("href") : null);
        for (Element e : document.select("a,img")) {
            String attr = ("a".equals(e.tagName()) ? "href" : "src");
            String link = e.attr(attr);

            if (!TextUtils.isEmpty(base))
                try {
                    // https://developer.android.com/reference/java/net/URI
                    link = URI.create(base).resolve(link.replace(" ", "%20")).toString();
                    e.attr(attr, link);
                } catch (Throwable ex) {
                    Log.w(ex);
                }

            if ("a".equals(e.tagName()) &&
                    link.trim().startsWith("#")) {
                e.tagName("span");
                e.removeAttr(attr);
            }
        }
    }

    static void autoLink(Document document) {
        autoLink(document, false);
    }

    static void autoLink(Document document, boolean outbound) {
        // https://en.wikipedia.org/wiki/List_of_URI_schemes
        // https://en.wikipedia.org/wiki/Geo_URI_scheme
        // https://developers.google.com/maps/documentation/urls/android-intents
        // xmpp:[<user>]@<host>[:<port>]/[<resource>][?<query>]
        // geo:<lat>,<lon>[,<alt>][;u=<uncertainty>]
        // tel:<phonenumber>
        final Pattern GPA_PATTERN = Pattern.compile("GPA\\.\\d{4}-\\d{4}-\\d{4}-\\d{5}");
        final String BOUNDARY = "(?:\\b|$|^)";
        final Pattern pattern = Pattern.compile(
                "(" + BOUNDARY + "((?i:mailto):)?" + Helper.EMAIL_ADDRESS + "(\\?[^\\s]*)?" + BOUNDARY + ")" +
                        "|" +
                        PatternsCompat.AUTOLINK_WEB_URL.pattern()
                                .replace("(?i:http|https|rtsp)://",
                                        "(((?i:http|https)://)|((?i:xmpp):))") +
                        "|" +
                        "(?i:geo:(-?\\d+(\\.\\d+)?),(-?\\d+(\\.\\d+)?)(,-?\\d+(\\.\\d+)?)?" +
                        "(;u=\\d+)?" + // Uncertainty
                        "(\\?z=\\d+)?" + // Zoom
                        "(\\?q=.+)?)" + // Google Maps query
                        "|" +
                        "(?i:tel:" + Patterns.PHONE.pattern() + ")" +
                        (BuildConfig.DEBUG ? "|(" + GPA_PATTERN + ")" : ""));

        NodeTraversor.traverse(new NodeVisitor() {
            private int links = 0;

            @Override
            public void head(Node node, int depth) {
                if (links < MAX_AUTO_LINK && node instanceof TextNode) {
                    TextNode tnode = (TextNode) node;
                    String text = tnode.getWholeText();

                    if (BuildConfig.DEBUG && node.parentNode() instanceof Element) {
                        Element parent = (Element) node.parentNode();
                        if ("faircode_txn_id".equals(parent.className())) {
                            Element a = document.createElement("a");
                            a.attr("href", BuildConfig.TX_URI + text.trim());
                            a.text(text);
                            tnode.before(a);
                            tnode.text("");
                            return;
                        }
                    }

                    Matcher matcher = pattern.matcher(text);
                    if (matcher.find())
                        try {
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
                                if (start < pos || start > end) {
                                    Log.e("Autolink pos=" + pos +
                                            " start=" + start + " end=" + end +
                                            " len=" + group.length() + "/" + text.length() +
                                            " text=" + text);
                                    return;
                                }

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
                                if (end < text.length() && text.charAt(end) == '$') {
                                    end++;
                                    group += '$';
                                }

                                boolean email = group.contains("@") && !group.contains(":");
                                Log.i("Web url=" + group + " " + start + "..." + end + "/" + text.length() +
                                        " linked=" + linked + " email=" + email + " count=" + links);

                                if (linked)
                                    span.appendText(text.substring(pos, end));
                                else {
                                    span.appendText(text.substring(pos, start));

                                    Element a = document.createElement("a");
                                    if (BuildConfig.DEBUG && GPA_PATTERN.matcher(group).matches())
                                        a.attr("href", BuildConfig.GPA_URI + group);
                                    else {
                                        String url = (email ? "mailto:" : "") + group;
                                        if (outbound)
                                            try {
                                                Uri uri = UriHelper.guessScheme(Uri.parse(url));
                                                a.attr("href", uri.toString());
                                            } catch (Throwable ex) {
                                                Log.e(ex);
                                                a.attr("href", url);
                                            }
                                        else
                                            a.attr("href", url);
                                    }
                                    a.text(group);
                                    span.appendChild(a);

                                    links++;
                                }

                                pos = end;
                            } while (links < MAX_AUTO_LINK && matcher.find());

                            span.appendText(text.substring(pos));

                            tnode.before(span);
                            tnode.text("");
                        } catch (Throwable ex) {
                            Log.e(ex);
                        }
                }
            }

            @Override
            public void tail(Node node, int depth) {
            }
        }, document);
    }

    static boolean embedYouTube(Document document) {
        // https://developers.google.com/youtube/player_parameters
        // Requires: setBlockNetworkLoads and setJavaScriptEnabled
        boolean has = false;
        for (Element a : document.select("a"))
            if (a.attr("href").startsWith("https://www.youtube.com/embed/")) {
                String link = a.attr("href");
                a.tagName("iframe");
                a.attr("id", link.substring(link.lastIndexOf("/") + 1));
                a.attr("type", "text/html");
                a.attr("src", link);
                a.attr("frameborder", "0");
                a.removeAttr("href");
                if (a.text().equals(link))
                    a.text("");
                has = true;
            }
        return has;
    }

    static void guessSchemes(Document document) {
        for (Element e : document.select("a,img"))
            try {
                String attr = ("a".equals(e.tagName()) ? "href" : "src");
                String url = e.attr(attr);
                if (TextUtils.isEmpty(url))
                    continue;
                Uri uri = UriHelper.guessScheme(Uri.parse(url));
                e.attr(attr, uri.toString());
            } catch (Throwable ex) {
                Log.e(ex);
            }
    }

    static void normalizeNamespaces(Document parsed, boolean display_hidden) {
        // <html xmlns:v="urn:schemas-microsoft-com:vml"
        //   xmlns:o="urn:schemas-microsoft-com:office:office"
        //   xmlns:w="urn:schemas-microsoft-com:office:word"
        //   xmlns:m="http://schemas.microsoft.com/office/2004/12/omml"
        //   xmlns="http://www.w3.org/TR/REC-html40">

        // <o:p>&nbsp;</o:p></span>
        // <w:Sdt DocPart="0C3EDB8F875B40C899499DE56A431990" ...

        // Default XHTML namespace: http://www.w3.org/1999/xhtml
        // https://developer.mozilla.org/en-US/docs/Related/IMSC/Namespaces

        String ns = null;
        for (Element h : parsed.select("html"))
            for (Attribute a : h.attributes())
                try {
                    String key = a.getKey();
                    String value = a.getValue().toLowerCase(Locale.ROOT);
                    if ("xmlns".equals(key) && value.contains(W3NS)) {
                        ns = key;
                        break;
                    } else if (key.startsWith("xmlns:") && value.contains(W3NS)) {
                        ns = key.split(":")[1];
                        break;
                    }
                } catch (Throwable ex) {
                    Log.e(ex);
                }

        for (Element e : parsed.select("*"))
            try {
                String tag = e.tagName();
                if (tag.contains(":")) {
                    boolean show = (ns == null || tag.startsWith(ns) ||
                            tag.startsWith("html:") || tag.startsWith("body:") || tag.startsWith("w:"));
                    if (display_hidden || show) {
                        String[] nstag = tag.split(":");
                        String t = nstag[nstag.length > 1 ? 1 : 0];
                        if (!TextUtils.isEmpty(t)) {
                            e.tagName(t);
                            Log.i("Updated tag=" + tag + " to=" + t);
                        }

                        if (!show) {
                            String style = e.attr("style");
                            e.attr("style", mergeStyles(style, "text-decoration:line-through;"));
                        }
                    } else if (TextUtils.isEmpty(e.text()) && !"\u00a0".equals(e.wholeText())) {
                        // <meta name=Generator content="Microsoft Word 15 (filtered medium)">
                        // <p class=MsoNormal>
                        //    <span style='font-family:"Calibri",sans-serif'>
                        //       <o:p>&nbsp;</o:p>
                        //    </span>
                        // </p>
                        e.remove();
                        Log.i("Removed tag=" + tag + " ns=" + ns +
                                " content=" + Helper.getPrintableString(e.wholeText(), true));
                    } else {
                        // Leave tag with unknown namespace to ensure all text is being displayed
                    }
                } else if (!"html".equals(tag) && !"body".equals(tag) && !"w".equals(tag)) {
                    String xmlns = e.attr("xmlns").toLowerCase(Locale.ROOT);
                    if (!TextUtils.isEmpty(xmlns) && !xmlns.contains(W3NS)) {
                        if (display_hidden) {
                            String style = e.attr("style");
                            e.attr("style", mergeStyles(style, "text-decoration:line-through;"));
                        } else {
                            e.remove();
                            Log.i("Removed tag=" + tag + " ns=" + ns + " xmlns=" + xmlns + " content=" + e.text());
                        }
                    }
                }
            } catch (Throwable ex) {
                Log.e(ex);
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
                                    style = mergeStyles(style, srule.getStyle().getCssText(), false);
                                break;
                            case Selector.SAC_CONDITIONAL_SELECTOR:
                                if (!TextUtils.isEmpty(clazz)) {
                                    ConditionalSelectorImpl cselector = (ConditionalSelectorImpl) selector;
                                    if (cselector.getCondition().getConditionType() == SAC_CLASS_CONDITION) {
                                        ClassConditionImpl ccondition = (ClassConditionImpl) cselector.getCondition();
                                        String value = ccondition.getValue();
                                        for (String cls : clazz.split("\\s+"))
                                            if (cls.equalsIgnoreCase(value)) {
                                                style = mergeStyles(style, srule.getStyle().getCssText(), false);
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

    static String mergeStyles(String base, String style) {
        return mergeStyles(base, style, true);
    }

    private static String mergeStyles(String base, String style, boolean element) {
        Map<String, String> result = new HashMap<>();

        // Base style
        Map<String, String> baseParams = new HashMap<>();
        if (!TextUtils.isEmpty(base))
            for (String param : base.split(";")) {
                int colon = param.indexOf(':');
                if (colon < 0) {
                    Log.w("CSS invalid=" + param);
                    continue;
                }

                String key = param.substring(0, colon).trim().toLowerCase(Locale.ROOT);
                baseParams.put(key, param);
            }

        // Element style
        if (!TextUtils.isEmpty(style))
            for (String param : style.split(";")) {
                int colon = param.indexOf(':');
                if (colon < 0) {
                    Log.w("CSS invalid=" + param);
                    continue;
                }

                String key = param.substring(0, colon).trim().toLowerCase(Locale.ROOT);
                String value = param.substring(colon + 1).trim().toUpperCase(Locale.ROOT);

                //https://developer.mozilla.org/en-US/docs/Learn/CSS/Building_blocks/Cascade_and_inheritance#controlling_inheritance
                boolean initial = false; // no value
                boolean inherit = false; // parent value
                if (element)
                    switch (value) {
                        case "inherit":
                            inherit = true;
                            break;
                        case "initial":
                            initial = true;
                            break;
                        case "unset":
                        case "revert":
                            inherit = !STYLE_NO_INHERIT.contains(key);
                            break;
                    }

                if (initial || inherit)
                    Log.i("CSS " + value + "=" + key);

                if (inherit) {
                    param = baseParams.get(key);
                    if (param != null)
                        result.put(key, param);
                } else if (!initial)
                    result.put(key, param);

                baseParams.remove(key);
            }

        for (String key : baseParams.keySet()) {
            String value = baseParams.get(key);
            boolean important = (value != null && value.contains("!important"));
            if (!STYLE_NO_INHERIT.contains(key) || element || important)
                result.put(key, baseParams.get(key));
        }

        if (result.size() == 0)
            return "";

        return TextUtils.join(";", result.values()) + ";";
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
            case "unset":
            case "initial":
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
                return FONT_XSMALL;
            case "small":
                return FONT_SMALL;
            case "medium":
                return 1.0f;
            case "large":
                return FONT_LARGE;
            case "x-large":
            case "xx-large":
            case "xxx-large":
                return FONT_XLARGE;
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

        if ("transparent".equals(value))
            return Color.TRANSPARENT;

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
        if (alpha == 255)
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

        return adjustLuminance(color, dark, MIN_LUMINANCE_VIEW);
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

    static boolean hasColor(int color) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        return (Math.abs(r - g) >= COLOR_THRESHOLD ||
                Math.abs(r - b) >= COLOR_THRESHOLD);
    }

    // https://tools.ietf.org/html/rfc3676
    static String flow(String text, boolean delsp) {
        boolean inquote = false;
        boolean continuation = false;
        StringBuilder flowed = new StringBuilder();
        String[] lines = text.split("\\r?\\n");
        for (int l = 0; l < lines.length; l++) {
            String line = lines[l];
            lines[l] = null;

            if (delsp && line.endsWith(" "))
                line = line.substring(0, line.length() - 1);

            boolean q = line.startsWith(">");
            if (q != inquote) {
                int len = flowed.length();
                if (len > 0 && flowed.charAt(len - 1) != '\n')
                    flowed.append("\n");
                continuation = false;
            }
            inquote = q;

            if (continuation)
                while (line.startsWith(">")) {
                    line = line.substring(1);
                    if (line.startsWith(" "))
                        line = line.substring(1);
                }

            continuation = (line.endsWith(" ") && !"-- ".equals(line));

            flowed.append(line);

            if (!continuation)
                flowed.append("\n");
        }

        return flowed.toString();
    }

    static String formatPlainText(String text) {
        return formatPlainText(text, true);
    }

    static String formatPlainText(String text, boolean view) {
        int level = 0;
        StringBuilder sb = new StringBuilder();
        String[] lines = text
                .replaceAll("\\r(?!\\n)", "\n")
                .split("\\r?\\n");
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
                        sb.append("<blockquote style=\"")
                                .append(getQuoteStyle(line, 0, line.length()))
                                .append("\">");

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

    static void restorePre(Document document) {
        document.select("div[x-plain=true]")
                .tagName("pre")
                .removeAttr("x-plain");
    }

    static void removeTrackingPixels(Context context, Document document) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean disconnect_images = (prefs.getBoolean("disconnect_images", false) && BuildConfig.DEBUG);

        Drawable d = ContextCompat.getDrawable(context, R.drawable.twotone_my_location_24);
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
                        !isTrackingHost(context, host, disconnect_images))
                    hosts.add(host);
            }
        }

        // Images
        List<Uri> uris = new ArrayList<>();
        for (Element img : document.select("img")) {
            img.removeAttr("x-tracking");

            String src = img.attr("src");
            if (TextUtils.isEmpty(src))
                continue;

            Uri uri = Uri.parse(src);
            String host = uri.getHost();
            if (host == null || hosts.contains(host))
                continue;

            if (uris.contains(uri)) {
                Log.i("Removing duplicate tracking image uri=" + uri);
                img.remove();
                continue;
            }

            if (isTrackingPixel(img) || isTrackingHost(context, host, disconnect_images)) {
                uris.add(uri);
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
        // Newton mail
        if ("cloudmagic-smart-beacon".equals(img.className()))
            return true;

        // Canary Mail
        // <img id="..." alt="" width="0px" src="https://receipts.canarymail.io/track/..._....png" height="0px">

        String width = img.attr("width").replace("px", "").trim();
        String height = img.attr("height").replace("px", "").trim();

        if (TextUtils.isEmpty(width) || TextUtils.isEmpty(height))
            return false;

        try {
            int w = Integer.parseInt(width);
            int h = Integer.parseInt(height);
            if (w == 0 && h != 0)
                w = h;
            if (w != 0 && h == 0)
                h = w;
            return (w * h <= TRACKING_PIXEL_SURFACE);
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    private static boolean isTrackingHost(Context context, String host, boolean disconnect_images) {
        if (TRACKING_HOSTS.contains(host))
            return true;
        if (disconnect_images && DisconnectBlacklist.isTrackingImage(context, host))
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
                        Uri uri = FileProviderEx.getUri(context, BuildConfig.APPLICATION_ID, file, attachment.name);
                        img.attr("src", uri.toString());
                        Log.i("Inline image uri=" + uri);
                    } else {
                        img.attr("src", ImageHelper.getDataUri(file, attachment.type));
                        Log.i("Inline image type=" + attachment.type);
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
        if (overview) {
            // fit width
            meta.remove();
            document.head().prependElement("meta")
                    .attr("name", "viewport")
                    .attr("content", "width=device-width");
        } else {
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

    static void overrideWidth(Document document) {
        List<String> tags = new ArrayList<>();
        for (Element e : document.select("*")) {
            String tag = e.tagName();
            if ("img".equals(tag))
                continue;
            if (tags.contains(tag))
                continue;
            tags.add(tag);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<style type=\"text/css\">");
        for (String tag : tags)
            sb.append(tag).append("{width: auto !important; min-width: 0 !important; max-width: 100% !important; overflow: auto !important;}");
        sb.append("</style>");

        document.select("head").append(sb.toString());
    }

    static boolean hasColorScheme(Document document, String name) {
        List<CSSStyleSheet> sheets = parseStyles(document.head().select("style"));
        for (CSSStyleSheet sheet : sheets)
            if (sheet.getCssRules() != null) {
                for (int i = 0; i < sheet.getCssRules().getLength(); i++) {
                    CSSRule rule = sheet.getCssRules().item(i);
                    if (rule instanceof CSSMediaRuleImpl) {
                        MediaList list = ((CSSMediaRuleImpl) rule).getMedia();
                        String media = (list == null ? null : list.getMediaText());
                        if (media != null &&
                                media.toLowerCase(Locale.ROOT).contains("prefers-color-scheme") &&
                                media.toLowerCase(Locale.ROOT).contains(name)) {
                            Log.i("@media=" + media);
                            return true;
                        }
                    }
                }
            }

        return false;
    }

    static void fakeDark(Document document) {
        // https://issuetracker.google.com/issues/237785596
        // https://developer.mozilla.org/en-US/docs/Web/CSS/filter-function/invert
        // https://developer.mozilla.org/en-US/docs/Web/CSS/filter-function/hue-rotate
        // https://developer.mozilla.org/en-US/docs/Web/CSS/@media/prefers-color-scheme
        // https://developer.android.com/reference/android/webkit/WebSettings#setAlgorithmicDarkeningAllowed(boolean)

        if (true || hasColorScheme(document, "dark"))
            return;

        document.head().appendElement("style").html(
                "body { filter: invert(100%) hue-rotate(180deg) !important; background: black !important; }" +
                        "img { filter: invert(100%) hue-rotate(180deg) !important; }");
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

    static String getFullText(Context context, String body) {
        try {
            if (body == null)
                return null;
            Document d = JsoupEx.parse(body);
            return _getText(context, d);
        } catch (OutOfMemoryError ex) {
            Log.e(ex);
            return null;
        }
    }

    static String getFullText(Context context, File file) throws IOException {
        try {
            Document d = JsoupEx.parse(file);
            return _getText(context, d);
        } catch (OutOfMemoryError ex) {
            Log.e(ex);
            return null;
        }
    }

    private static String _getText(Context context, Document d) {
        truncate(d, MAX_FULL_TEXT_SIZE);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean preview_hidden = prefs.getBoolean("preview_hidden", true);
        boolean preview_quotes = prefs.getBoolean("preview_quotes", true);

        if (!preview_hidden)
            for (Element e : d.select("*")) {
                String style = e.attr("style");
                if (TextUtils.isEmpty(style))
                    continue;

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
                    if ("display".equals(key) && "none".equals(value)) {
                        e.remove();
                        break;
                    }
                }
            }

        if (!preview_quotes) {
            if (!removeQuotes(d, false)) {
                Element top = d.select("blockquote").first();
                if (top != null && top.previousElementSibling() == null)
                    top.remove();
            }
        }

        for (Element bq : d.select("blockquote"))
            bq.prependChild(new TextNode("> "));

        return d.body().text();
    }

    static String getQuoteStyle(Element e) {
        CharSequence text = e.text();
        return getQuoteStyle(text, 0, text.length());
    }

    static String getQuoteStyle(CharSequence quoted, int start, int end) {
        String dir = "left";
        try {
            int count = end - start;
            if (TextDirectionHeuristics.FIRSTSTRONG_LTR.isRtl(quoted, start, count))
                dir = "right";
        } catch (Throwable ex) {
            Log.e(new Throwable("getQuoteStyle " + start + "..." + end, ex));
        }
        return "border-" + dir + ":3px solid #ccc; padding-" + dir + ":10px;margin:0;";
    }

    static String getIndentStyle(CharSequence quoted, int start, int end) {
        return "margin-top:0; margin-bottom:0;";
    }

    static boolean hasBorder(Element e) {
        if ("true".equals(e.attr("x-border")))
            return true;

        // https://groups.google.com/g/mozilla.support.thunderbird/c/rwLNk3MU3Gs?pli=1
        if ("cite".equals(e.attr("type")))
            return true;

        String style = e.attr("style");
        String[] params = style.split(";");
        for (String param : params) {
            int colon = param.indexOf(':');
            if (colon < 0)
                continue;
            String key = param.substring(0, colon).trim().toLowerCase(Locale.ROOT);
            String value = param.substring(colon + 1);
            if ("border-left".equals(key) || "border-right".equals(key)) {
                Float border = getFontSize(value.trim().split("\\s+")[0], 1.0f);
                if (border != null && border > 0)
                    return true;
            }
        }

        return false;
    }

    static boolean isStyled(Document d) {
        ObjectHolder<Boolean> result = new ObjectHolder<>(false);

        d.body().filter(new NodeFilter() {
            private final List<String> STRUCTURE = Collections.unmodifiableList(Arrays.asList(
                    "body", "div", "p", "span", "br",
                    "strong", "b", "em", "i", "blockquote", "hr"
            ));

            @Override
            public FilterResult head(Node node, int depth) {
                if (node instanceof Element) {
                    Element e = (Element) node;

                    String style = e.attr("style");
                    if (!TextUtils.isEmpty(style)) {
                        String[] params = style.split(";");
                        for (String param : params) {
                            int colon = param.indexOf(':');
                            if (colon <= 0)
                                continue;
                            String key = param.substring(0, colon).trim();
                            if ("color".equalsIgnoreCase(key) ||
                                    "background-color".equalsIgnoreCase(key) ||
                                    "font-family".equalsIgnoreCase(key) ||
                                    "font-size".equalsIgnoreCase(key) ||
                                    "text-align".equalsIgnoreCase(key) ||
                                    "text-decoration".equalsIgnoreCase(key) /* line-through */) {
                                Log.i("Style element=" + node + " style=" + style);
                                result.value = true;
                                return FilterResult.STOP;
                            }
                        }
                    }

                    if (STRUCTURE.contains(e.tagName()))
                        return FilterResult.CONTINUE;

                    if (!TextUtils.isEmpty(e.attr("fairemail")))
                        return FilterResult.CONTINUE;

                    //Element p = e.parent();
                    //if ("blockquote".equals(e.tagName()) &&
                    //        p != null &&
                    //        !TextUtils.isEmpty(p.attr("fairemail")))
                    //    return FilterResult.CONTINUE;

                    Log.i("Style element=" + node);
                    result.value = true;
                    return FilterResult.STOP;

                } else
                    return FilterResult.CONTINUE;
            }

            @Override
            public FilterResult tail(Node node, int depth) {
                return FilterResult.CONTINUE;
            }
        });

        return result.value;
    }

    static void removeSignatures(Document d) {
        // https://jsoup.org/apidocs/org/jsoup/select/Selector.html

        // <div class="fairemail_signature">
        d.body().select(".fairemail_signature").remove();

        // <div data-smartmail="gmail_signature">
        // <div data-smartmail="gmail_signature" dir="auto">
        // <div dir="ltr" class="gmail_signature" data-smartmail="gmail_signature">
        d.body().select("div[data-smartmail=gmail_signature]").remove();

        // Outlook: <div id="Signature" data-lt-sig-active="">
        d.body().select("div#Signature").select("[data-lt-sig-active]").remove();

        // Outlook/mobile <div id="ms-outlook-mobile-signature" dir="auto">
        //d.body().select("div#ms-outlook-mobile-signature").remove();

        // Yahoo/Android: <div id="ymail_android_signature">
        //d.body().select("div#`ymail_android_signature").remove();

        // Spark: <div name="messageSignatureSection">
        d.body().select("div[name=messageSignatureSection]").remove();

        // BlackBerry: <div id="blackberry_signature_BBPPID" name="BB10" dir="auto">
        d.body().select("div#blackberry_signature_BBPPID").remove();

        // <div class="moz-signature">
        // <pre class="moz-signature" cols="72">
        //d.body().select("div.moz-signature").remove();
        //d.body().select("pre.moz-signature").remove();

        // Apple: <br id="lineBreakAtBeginningOfSignature"> <div dir="ltr">
        for (Element br : d.body().select("br#lineBreakAtBeginningOfSignature")) {
            Element next = br.nextElementSibling();
            if (next != null && "div".equals(next.tagName())) {
                br.remove();
                next.remove();
            }
        }

        // Usenet style signature
        d.body().filter(new NodeFilter() {
            private boolean remove = false;
            private boolean noremove = false;

            @Override
            public FilterResult head(Node node, int depth) {
                if (node instanceof TextNode) {
                    TextNode tnode = (TextNode) node;
                    String text = tnode.getWholeText()
                            .replaceAll("[\r\n]+$", "")
                            .replaceAll("^[\r\n]+", "");
                    if ("-- ".equals(text)) {
                        if (tnode.getWholeText().endsWith("\n"))
                            remove = true;
                        else {
                            Node next = node.nextSibling();
                            if (next == null) {
                                Node parent = node.parent();
                                if (parent != null)
                                    next = parent.nextSibling();
                            }
                            if (next != null && "br".equals(next.nodeName()))
                                remove = true;
                        }
                    }
                } else if (node instanceof Element) {
                    Element element = (Element) node;
                    if (remove && "blockquote".equals(element.tagName()))
                        noremove = true;
                }

                return (remove && !noremove
                        ? FilterResult.REMOVE : FilterResult.CONTINUE);
            }

            @Override
            public FilterResult tail(Node node, int depth) {
                return FilterResult.CONTINUE;
            }
        });
    }

    static boolean removeQuotes(Document d, boolean all) {
        Elements quotes = d.body().select(".fairemail_quote");
        if (!quotes.isEmpty()) {
            quotes.remove();
            return true;
        }

        // Gmail
        quotes = d.body().select(".gmail_quote");
        if (!quotes.isEmpty()) {
            quotes.remove();
            return true;
        }

        // Outlook: <div id="appendonsend">
        quotes = d.body().select("div#appendonsend");
        if (!quotes.isEmpty()) {
            quotes.nextAll().remove();
            quotes.remove();
            return true;
        }

        // ms-outlook-mobile
        quotes = d.body().select("div#divRplyFwdMsg");
        if (!quotes.isEmpty()) {
            quotes.nextAll().remove();
            quotes.remove();
            return true;
        }

        // Microsoft Word 15
        quotes = d.body().select("div#mail-editor-reference-message-container");
        if (!quotes.isEmpty()) {
            quotes.remove();
            return true;
        }

        // Web.de: <div id="aqm-original"
        quotes = d.body().select("div#aqm-original");
        if (!quotes.isEmpty()) {
            quotes.remove();
            return true;
        }

        if (!all)
            return false;

        return !d.select("blockquote").remove().isEmpty();
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

        truncate(d, getMaxFormatTextSize(context));

        SpannableStringBuilder ssb = fromDocument(context, d, null, null);

        for (UnderlineSpan span : ssb.getSpans(0, ssb.length(), UnderlineSpan.class)) {
            int start = ssb.getSpanStart(span);
            int end = ssb.getSpanEnd(span);
            ssb.insert(end, "_");
            ssb.insert(start, "_");
        }

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

            if (!source.toLowerCase(Locale.ROOT).startsWith("data:"))
                ssb.insert(end, "[" + context.getString(R.string.title_avatar) + "]");

            for (int i = start; i < end; i++)
                if (ssb.charAt(i) == '\uFFFC') {
                    ssb.delete(i, i + 1);
                    end--;
                }
        }

        for (BulletSpan span : ssb.getSpans(0, ssb.length(), BulletSpan.class)) {
            int start = ssb.getSpanStart(span);
            if (span instanceof NumberSpan) {
                NumberSpan ns = (NumberSpan) span;
                ssb.insert(start, ns.getIndex() + ". ");
                int level = ns.getLevel();
                for (int l = 1; l <= level; l++)
                    ssb.insert(start, "  ");
            } else {
                if (span instanceof BulletSpanEx) {
                    BulletSpanEx bs = (BulletSpanEx) span;

                    if (!"none".equals(bs.getLType()))
                        ssb.insert(start, "* ");

                    int level = bs.getLevel();
                    for (int l = 1; l <= level; l++)
                        ssb.insert(start, "  ");
                } else
                    ssb.insert(start, "* ");
            }
        }

        for (LineSpan span : ssb.getSpans(0, ssb.length(), LineSpan.class)) {
            int start = ssb.getSpanStart(span);
            int end = ssb.getSpanEnd(span);
            ssb.replace(start, end, LINE);
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

        return ssb.toString();
    }

    static SpannableStringBuilder highlightHeaders(
            Context context, Address[] from, Address[] to, Long time, String headers, boolean blocklist, boolean withReceived) {
        SpannableStringBuilder ssb = new SpannableStringBuilderEx(headers.replaceAll("\\t", " "));
        int textColorLink = Helper.resolveColor(context, android.R.attr.textColorLink);
        int colorVerified = Helper.resolveColor(context, R.attr.colorVerified);
        int colorWarning = Helper.resolveColor(context, R.attr.colorWarning);
        int colorSeparator = Helper.resolveColor(context, R.attr.colorSeparator);
        float stroke = context.getResources().getDisplayMetrics().density;

        int index = 0;
        for (String line : headers.split("\n")) {
            if (line.length() > 0 && !Character.isWhitespace(line.charAt(0))) {
                int colon = line.indexOf(':');
                if (colon > 0)
                    ssb.setSpan(new ForegroundColorSpan(textColorLink), index, index + colon, 0);
            }
            index += line.length() + 1;
        }

        if (withReceived) {
            ssb.append("\n\uFFFC"); // Object replacement character
            ssb.setSpan(new LineSpan(colorSeparator, stroke, 0), ssb.length() - 1, ssb.length(), 0);
            ssb.append('\n');

            try {
                // https://datatracker.ietf.org/doc/html/rfc2821#section-4.4
                final DateFormat DTF = Helper.getDateTimeInstance(context, DateFormat.SHORT, DateFormat.MEDIUM);

                MailDateFormat mdf = new MailDateFormat();
                ByteArrayInputStream bis = new ByteArrayInputStream(headers.getBytes());
                InternetHeaders iheaders = new InternetHeaders(bis, true);

                Date tx = null;

                String dh = iheaders.getHeader("Date", null);
                try {
                    if (dh != null)
                        tx = mdf.parse(dh);
                } catch (ParseException ex) {
                    Log.w(ex);
                }

                if (tx != null) {
                    ssb.append('\n');
                    int s = ssb.length();
                    ssb.append(DTF.format(tx));
                    ssb.setSpan(new StyleSpan(Typeface.BOLD), s, ssb.length(), 0);
                }

                if (from != null) {
                    ssb.append('\n');
                    int s = ssb.length();
                    ssb.append("from");
                    ssb.setSpan(new ForegroundColorSpan(textColorLink), s, ssb.length(), 0);
                    ssb.setSpan(new StyleSpan(Typeface.BOLD), s, ssb.length(), 0);
                    ssb.append(' ').append(MessageHelper.formatAddresses(from, true, false));
                }

                if (tx != null || from != null)
                    ssb.append('\n');

                Date rx = null;
                String[] received = iheaders.getHeader("Received");
                if (received != null && received.length > 0) {
                    for (int i = received.length - 1; i >= 0; i--) {
                        ssb.append('\n');
                        String h = MimeUtility.unfold(received[i]);

                        int semi = h.lastIndexOf(';');
                        if (semi > 0) {
                            rx = mdf.parse(h, new ParsePosition(semi + 1));
                            h = h.substring(0, semi);
                        }

                        int s = ssb.length();
                        ssb.append('#').append(Integer.toString(received.length - i));
                        if (rx != null) {
                            ssb.append(' ').append(DTF.format(rx));
                            if (tx != null)
                                ssb.append(" \u0394")
                                        .append(Helper.formatDuration(rx.getTime() - tx.getTime()));
                        }
                        ssb.setSpan(new StyleSpan(Typeface.BOLD), s, ssb.length(), 0);

                        if (blocklist && i == received.length - 1) {
                            Drawable d = ContextCompat.getDrawable(context, R.drawable.twotone_flag_24);

                            int iconSize = context.getResources().getDimensionPixelSize(R.dimen.menu_item_icon_size);
                            d.setBounds(0, 0, iconSize, iconSize);
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
                        boolean p = false;
                        String[] w = h.split("\\s+");
                        while (j < w.length) {
                            if (w[j].startsWith("("))
                                p = true;

                            if (j > 0)
                                ssb.append(' ');

                            s = ssb.length();
                            ssb.append(w[j]);
                            if (!p && MessageHelper.RECEIVED_WORDS.contains(w[j].toLowerCase(Locale.ROOT))) {
                                ssb.setSpan(new ForegroundColorSpan(textColorLink), s, ssb.length(), 0);
                                ssb.setSpan(new StyleSpan(Typeface.BOLD), s, ssb.length(), 0);
                            }

                            if (w[j].endsWith(")"))
                                p = false;

                            j++;
                        }

                        Boolean tls = MessageHelper.isTLS(h, i == received.length - 1);
                        ssb.append(" TLS=");
                        int t = ssb.length();
                        ssb.append(tls == null ? "?" : Boolean.toString(tls));
                        if (tls != null)
                            ssb.setSpan(new ForegroundColorSpan(tls ? colorVerified : colorWarning), t, ssb.length(), 0);

                        ssb.append("\n");
                    }
                }

                if (time != null) {
                    ssb.append('\n');
                    int s = ssb.length();
                    ssb.append(DTF.format(time));
                    if (rx != null)
                        ssb.append(" \u0394")
                                .append(Helper.formatDuration(time - rx.getTime()));
                    ssb.setSpan(new StyleSpan(Typeface.BOLD), s, ssb.length(), 0);
                }

                if (to != null) {
                    ssb.append('\n');
                    int s = ssb.length();
                    ssb.append("to");
                    ssb.setSpan(new ForegroundColorSpan(textColorLink), s, ssb.length(), 0);
                    ssb.setSpan(new StyleSpan(Typeface.BOLD), s, ssb.length(), 0);
                    ssb.append(' ').append(MessageHelper.formatAddresses(to, true, false));
                }

                if (time != null || to != null)
                    ssb.append('\n');
            } catch (Throwable ex) {
                Log.w(ex);
            }
        }

        return ssb;
    }

    static void highlightSearched(Context context, Document document, String query, boolean partial) {
        try {
            int color = Helper.resolveColor(context, R.attr.colorHighlight);

            StringBuilder sb = new StringBuilder();
            for (String word : query.trim().split("\\s+")) {
                if (word.startsWith("+") || word.startsWith("-"))
                    continue;
                for (String w : Fts4DbHelper.breakText(word).split("\\s+")) {
                    if (sb.length() > 0)
                        sb.append("\\s*");
                    sb.append(Pattern.quote(w));
                }
            }
            if (partial) {
                sb.insert(0, ".*?(");
                sb.append(").*?");
            } else {
                sb.insert(0, ".*?\\b(");
                sb.append(")\\b.*?");
            }

            // TODO: match für for fur
            Pattern p = Pattern.compile(sb.toString(), Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

            NodeTraversor.traverse(new NodeVisitor() {
                @Override
                public void head(Node node, int depth) {
                    if (node instanceof TextNode)
                        try {
                            TextNode tnode = (TextNode) node;
                            String text = tnode.getWholeText();

                            Matcher result = p.matcher(text);

                            int prev = 0;
                            Element holder = document.createElement("span");
                            while (result.find()) {
                                int start = result.start(1);
                                int end = result.end(1);

                                holder.appendText(text.substring(prev, start));

                                Element span = document.createElement("span");
                                span.attr("style", mergeStyles(
                                        span.attr("style"),
                                        "font-size:larger !important;" +
                                                "font-weight:bold !important;" +
                                                "background-color:" + encodeWebColor(color) + " !important"));
                                span.text(text.substring(start, end));
                                holder.appendChild(span);

                                prev = end;
                            }

                            if (prev == 0) // No matches
                                return;

                            if (prev < text.length())
                                holder.appendText(text.substring(prev));

                            tnode.before(holder);
                            tnode.text("");
                        } catch (Throwable ex) {
                            Log.e(ex);
                        }
                }

                @Override
                public void tail(Node node, int depth) {
                }
            }, document);
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    static Document markText(Document document) {
        for (Element mark : document.select("mark")) {
            String style = mark.attr("style");
            mark.attr("style", mergeStyles(style, "font-style: italic;"));
        }

        return document;
    }

    static void cleanup(Document d) {
        // https://www.chromestatus.com/feature/5756335865987072
        // Some messages contain 100 thousands of Apple spaces
        if (false)
            for (Element aspace : d.select(".Apple-converted-space")) {
                Node next = aspace.nextSibling();
                if (next instanceof TextNode) {
                    TextNode tnode = (TextNode) next;
                    tnode.text(" " + tnode.text());
                    aspace.remove();
                } else
                    aspace.replaceWith(new TextNode(" "));
                Log.i("Replaced Apple-converted-space");
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
            @Nullable ImageGetterEx imageGetter, @Nullable Html.TagHandler tagHandler) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean debug = prefs.getBoolean("debug", false);
        boolean monospaced_pre = prefs.getBoolean("monospaced_pre", false);

        final int colorPrimary = Helper.resolveColor(context, androidx.appcompat.R.attr.colorPrimary);
        final int colorAccent = Helper.resolveColor(context, androidx.appcompat.R.attr.colorAccent);
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

                    // Soft hyphen
                    if (text.trim().equals("\u00ad"))
                        text = "";

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
                    if (endsWithSpace(text) && !"-- ".equals(text)) {
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
        SpannableStringBuilder ssb = new SpannableStringBuilderEx();

        NodeTraversor.traverse(new NodeVisitor() {
            private Element element;
            private TextNode tnode;
            private Typeface wingdings = null;

            @Override
            public void head(Node node, int depth) {
                if (node instanceof Element) {
                    element = (Element) node;
                    Element prev = element.previousElementSibling();

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
                            !"true".equals(element.attr("x-paragraph")) &&
                            (prev == null || !"true".equals(prev.attr("x-line-after"))) &&
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
                                case "background":
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
                                        } catch (Throwable ex) {
                                            Log.i(ex);
                                        }
                                    break;
                                case "font-weight":
                                    Integer fweight = getFontWeight(value);
                                    if (fweight != null)
                                        setSpan(ssb, new StyleSpan(fweight >= 600 ? Typeface.BOLD : Typeface.NORMAL), start, ssb.length());
                                    break;
                                case "font-family":
                                    if ("wingdings".equalsIgnoreCase(value)) {
                                        if (wingdings == null)
                                            wingdings = ResourcesCompat.getFont(context.getApplicationContext(), R.font.wingdings);

                                        int from = start;
                                        for (int i = start; i < ssb.length(); i++) {
                                            int kar = ssb.charAt(i);
                                            if (MAP_WINGDINGS.containsKey(kar)) {
                                                if (from < i) {
                                                    TypefaceSpan span = new CustomTypefaceSpan("wingdings", wingdings);
                                                    setSpan(ssb, span, from, i);
                                                }
                                                int codepoint = MAP_WINGDINGS.get(kar);
                                                String replacement = new String(Character.toChars(codepoint));
                                                ssb.replace(i, i + 1, replacement);
                                                i += replacement.length() - 1;
                                                from = i + 1;
                                            }
                                        }

                                        if (from < ssb.length()) {
                                            TypefaceSpan span = new CustomTypefaceSpan("wingdings", wingdings);
                                            setSpan(ssb, span, from, ssb.length());
                                        }
                                    } else
                                        setSpan(ssb, StyleHelper.getTypefaceSpan(value, context), start, ssb.length());
                                    break;
                                case "font-style":
                                    if ("italic".equals(value))
                                        setSpan(ssb, new StyleSpan(Typeface.ITALIC), start, ssb.length());
                                    break;
                                case "text-decoration":
                                    if ("line-through".equals(value))
                                        setSpan(ssb, new StrikethroughSpan(), start, ssb.length());
                                    else if ("underline".equals(value))
                                        setSpan(ssb, new UnderlineSpan(), start, ssb.length());
                                    break;
                                case "text-align":
                                    // https://developer.mozilla.org/en-US/docs/Web/CSS/text-align
                                    Layout.Alignment alignment = null;
                                    boolean rtl;
                                    try {
                                        rtl = TextDirectionHeuristics.FIRSTSTRONG_LTR.isRtl(ssb, start, ssb.length() - start);
                                    } catch (Throwable ex) {
                                        // IllegalArgumentException
                                        Log.e(ex);
                                        rtl = false;
                                    }
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
                                case "visibility":
                                    if ("hidden".equals(value)) {
                                        for (ForegroundColorSpan span : ssb.getSpans(start, ssb.length(), ForegroundColorSpan.class))
                                            ssb.removeSpan(span);
                                        for (BackgroundColorSpan span : ssb.getSpans(start, ssb.length(), BackgroundColorSpan.class))
                                            ssb.removeSpan(span);
                                        setSpan(ssb, new ForegroundColorSpan(Color.TRANSPARENT), start, ssb.length());
                                    }
                                    break;
                            }
                        }
                    }

                    // Apply calculated font size
                    String xFontSizeAbs = element.attr("x-font-size-abs");
                    if (TextUtils.isEmpty(xFontSizeAbs)) {
                        String xFontSizeRel = element.attr("x-font-size-rel");
                        if (!TextUtils.isEmpty(xFontSizeRel)) {
                            float fsize = Float.parseFloat(xFontSizeRel);
                            if (fsize != 1.0f)
                                setSpan(ssb, new RelativeSizeSpan(fsize), start, ssb.length());
                        }
                    } else {
                        int px = Integer.parseInt(xFontSizeAbs);
                        setSpan(ssb, new AbsoluteSizeSpan(px), start, ssb.length());
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
                                if (!TextUtils.isEmpty(href)) {
                                    if (false && BuildConfig.DEBUG) {
                                        Uri uri = UriHelper.guessScheme(Uri.parse(href));
                                        if (UriHelper.isHyperLink(uri))
                                            ssb.append("\uD83D\uDD17"); // 🔗
                                        // Unicode 6.0, supported since Android 4.1
                                        // https://developer.android.com/guide/topics/resources/internationalization
                                    }
                                    setSpan(ssb, new URLSpan(href), start, ssb.length());
                                }
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
                                    setSpan(ssb, new IndentSpan(intentSize), start, ssb.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                                break;
                            case "br":
                                ssb.append('\n');

                                int l = ssb.length() - 1;
                                List<Object> spans = new ArrayList<>();
                                spans.addAll(Arrays.asList(ssb.getSpans(l, l, AbsoluteSizeSpan.class)));
                                spans.addAll(Arrays.asList(ssb.getSpans(l, l, RelativeSizeSpan.class)));
                                for (Object span : spans) {
                                    int s = ssb.getSpanStart(span);
                                    int e = ssb.getSpanEnd(span);
                                    int f = ssb.getSpanFlags(span);
                                    if (e == l) {
                                        ssb.removeSpan(span);
                                        if (span instanceof AbsoluteSizeSpan) {
                                            int size = ((AbsoluteSizeSpan) span).getSize();
                                            setSpan(ssb, new AbsoluteSizeSpan(size), s, e + 1, f);
                                        } else if (span instanceof RelativeSizeSpan) {
                                            float size = ((RelativeSizeSpan) span).getSizeChange();
                                            setSpan(ssb, new RelativeSizeSpan(size), s, e + 1, f);
                                        }
                                    }
                                }
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
                                    setSpan(ssb, StyleHelper.getTypefaceSpan(face, context), start, ssb.length());
                                break;
                            case "h1":
                            case "h2":
                            case "h3":
                            case "h4":
                            case "h5":
                            case "h6":
                                setSpan(ssb, new StyleSpan(Typeface.BOLD), start, ssb.length());
                                int hsize = tag.charAt(1) - '0';
                                if (hsize == 1)
                                    setSpan(ssb, new RelativeSizeSpan(FONT_XLARGE), start, ssb.length());
                                else if (hsize == 2)
                                    setSpan(ssb, new RelativeSizeSpan(FONT_LARGE), start, ssb.length());
                                else if (hsize > 3)
                                    setSpan(ssb, new RelativeSizeSpan(FONT_SMALL), start, ssb.length());
                                break;
                            case "hr":
                                // Suppress successive lines
                                if (!"true".equals(element.attr("x-keep-line"))) {
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
                                }

                                boolean dashed = "true".equals(element.attr("x-dashed"));
                                float stroke = context.getResources().getDisplayMetrics().density;
                                float dash = (dashed ? line_dash_length : 0f);
                                if (ssb.length() > 0 && ssb.charAt(ssb.length() - 1) != '\n')
                                    ssb.append('\n');
                                ssb.append("\uFFFC");  // Object replacement character
                                setSpan(ssb, new LineSpan(colorSeparator, stroke, dash), start, ssb.length());
                                break;
                            case "img":
                                String src = element.attr("src");
                                if (!TextUtils.isEmpty(src)) {
                                    Drawable d = (imageGetter == null
                                            ? ContextCompat.getDrawable(context, R.drawable.twotone_broken_image_24)
                                            : imageGetter.getDrawable(element));
                                    ssb.insert(start, "\uFFFC"); // Object replacement character
                                    setSpan(ssb, new ImageSpanEx(d, element), start, start + 1);
                                }
                                break;
                            case "input":
                                String type = element.attr("type");
                                boolean checked = element.hasAttr("checked");
                                if ("checkbox".equalsIgnoreCase(type))
                                    ssb.append(checked ? "\u2611" : "\u2610");
                                break;
                            case "li":
                                if (start == 0 || ssb.charAt(start - 1) != '\n')
                                    ssb.insert(start++, "\n");
                                if (ssb.length() == 0 || ssb.charAt(ssb.length() - 1) != '\n')
                                    ssb.append("\n");

                                int level = 0;
                                Element list = null;
                                String ltype = element.attr("type");
                                if (TextUtils.isEmpty(ltype))
                                    ltype = element.attr("x-list-style");
                                Element parent = element.parent();
                                while (parent != null) {
                                    if ("ol".equals(parent.tagName()) || "ul".equals(parent.tagName())) {
                                        if (!"false".equals(parent.attr("x-list-level")))
                                            level++;
                                        if (list == null)
                                            list = parent;
                                        if (TextUtils.isEmpty(ltype))
                                            ltype = parent.attr("type");
                                        if (TextUtils.isEmpty(ltype))
                                            ltype = parent.attr("x-list-style");
                                    }
                                    parent = parent.parent();
                                }
                                if (level > 0)
                                    level--;

                                if (list == null ||
                                        ("ul".equals(list.tagName()) &&
                                                !NumberSpan.isSupportedType(ltype))) {
                                    // https://developer.mozilla.org/en-US/docs/Web/HTML/Element/ul
                                    Object ul;
                                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P)
                                        ul = new BulletSpanEx(bulletIndent, bulletGap, colorAccent, level, ltype);
                                    else
                                        ul = new BulletSpanEx(bulletIndent, bulletGap, colorAccent, bulletRadius, level, ltype);
                                    setSpan(ssb, ul, start, ssb.length());
                                } else {
                                    // https://developer.mozilla.org/en-US/docs/Web/HTML/Element/ol
                                    int index = 0;
                                    String s = list.attr("start");
                                    if (!TextUtils.isEmpty(s) && TextUtils.isDigitsOnly(s))
                                        index = Integer.parseInt(s) - 1;
                                    for (Node child : list.childNodes()) {
                                        if (child instanceof Element &&
                                                child.nodeName().equals(element.tagName())) {
                                            index++;
                                            if (child == element)
                                                break;
                                        }
                                    }

                                    setSpan(ssb, new NumberSpan(bulletIndent, bulletGap, colorAccent, textSize, level, index, ltype), start, ssb.length());
                                }

                                break;
                            case "mark":
                                setSpan(ssb, new StyleHelper.MarkSpan(), start, ssb.length());
                                break;
                            case "pre":
                            case "tt":
                            case "samp":
                                // Signature
                                setSpan(ssb, StyleHelper.getTypefaceSpan("monospace", context), start, ssb.length());
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
                                setSpan(ssb, new SubscriptSpanEx(), start, ssb.length());
                                break;
                            case "sup":
                                setSpan(ssb, new SuperscriptSpanEx(), start, ssb.length());
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
                                // https://developer.mozilla.org/en-US/docs/Web/HTML/Element/del
                                setSpan(ssb, new StrikethroughSpan(), start, ssb.length());
                                break;
                            case "title":
                                // Signature, etc
                                break;
                            case "u":
                            case "ins":
                                // https://developer.mozilla.org/en-US/docs/Web/HTML/Element/ins
                                setSpan(ssb, new UnderlineSpan(), start, ssb.length());
                                break;
                            default:
                                Log.w("Unknown tag=" + element.tagName());
                        }

                        if (monospaced_pre &&
                                "true".equals(element.attr("x-plain")))
                            setSpan(ssb, StyleHelper.getTypefaceSpan("monospace", context), start, ssb.length());
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
                            !"true".equals(element.attr("x-paragraph")) &&
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

        for (LineSpan line : ssb.getSpans(0, ssb.length(), LineSpan.class)) {
            int end = ssb.getSpanEnd(line);
            if (end < ssb.length() && ssb.charAt(end) != '\n')
                ssb.insert(end, "\n");
        }

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

        for (Object bold : spans) {
            if (bold instanceof StyleSpan) {
                int style = ((StyleSpan) bold).getStyle();
                if (style == Typeface.BOLD) {
                    int bs = start.get(bold);
                    int be = end.get(bold);

                    List<StyleSpan> normal = new ArrayList<>();
                    for (StyleSpan ss : ssb.getSpans(bs, be, StyleSpan.class))
                        if (ss.getStyle() == Typeface.NORMAL)
                            normal.add(ss);

                    if (normal.size() > 0) {
                        ssb.removeSpan(bold);

                        Collections.sort(normal, new Comparator<StyleSpan>() {
                            @Override
                            public int compare(StyleSpan s1, StyleSpan s2) {
                                int s = Integer.compare(ssb.getSpanStart(s1), ssb.getSpanStart(s2));
                                if (s != 0)
                                    return s;
                                return -Integer.compare(ssb.getSpanEnd(s1), ssb.getSpanEnd(s2));
                            }
                        });

                        for (StyleSpan n : normal) {
                            int ns = start.get(n);
                            if (ns > bs) {
                                ssb.setSpan(new StyleSpan(Typeface.BOLD), bs, ns, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                bs = end.get(n);
                            }
                        }

                        if (bs < be)
                            ssb.setSpan(new StyleSpan(Typeface.BOLD), bs, be, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
            }
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
                .removeAttr("x-font-size-abs")
                .removeAttr("x-line-before")
                .removeAttr("x-line-after")
                .removeAttr("x-align")
                .removeAttr("x-column")
                .removeAttr("x-dashed")
                .removeAttr("x-tracking")
                .removeAttr("x-border")
                .removeAttr("x-list-style")
                .removeAttr("x-list-level")
                .removeAttr("x-plain")
                .remove("x-keep-line");
    }

    static void clearComposingText(TextView view) {
        if (view == null)
            return;

        CharSequence edit = view.getText();
        if (!(edit instanceof Spannable))
            return;

        // Copied from BaseInputConnection.removeComposingSpans
        Spannable text = (Spannable) edit;
        Object[] sps = text.getSpans(0, text.length(), Object.class);
        if (sps != null) {
            for (int i = sps.length - 1; i >= 0; i--) {
                Object o = sps[i];
                if (o instanceof ImageSpan) {
                    String source = ((ImageSpan) o).getSource();
                    if (source != null && source.startsWith("cid:"))
                        continue;
                }
                if ((text.getSpanFlags(o) & Spanned.SPAN_COMPOSING) != 0) {
                    text.removeSpan(o);
                }
            }
        }
    }

    static void clearComposingText(Spannable text) {
        if (text == null)
            return;
        BaseInputConnection.removeComposingSpans(text);
    }

    static Spanned fromHtml(@NonNull String html, Context context) {
        Document parsed = JsoupEx.parse(html);
        Document document = sanitizeView(context, parsed, false);
        return fromDocument(context, document, null, null);
    }

    static String toHtml(Spanned spanned, Context context) {
        HtmlEx converter = new HtmlEx(context);
        String html = converter.toHtml(spanned, TO_HTML_PARAGRAPH_LINES_INDIVIDUAL);

        Document doc = JsoupEx.parse(html);

        if (doc.head().select("meta[name=viewport]").first() == null)
            doc.head().prependElement("meta")
                    .attr("name", "viewport")
                    .attr("content", "width=device-width, initial-scale=1.0");

        for (Element span : doc.select("span")) {
            if (span.attr("dir").equals("rtl")) {
                Element next = span.nextElementSibling();
                if (next != null && next.tagName().equals("br")) {
                    span.tagName("div");
                    span.appendElement("br");
                    next.remove();
                }
            }

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

        for (Element e : doc.select("ol,ul")) {
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

            Element parent = e.parent();
            Element prev = e.previousElementSibling();
            if (parent != null && !"li".equals(parent.tagName()) &&
                    prev != null && "li".equals(prev.tagName())) {
                e.remove();
                prev.appendChild(e);
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

        for (Element line : doc.select("hr")) {
            Element next = line.nextElementSibling();
            if (next != null && "br".equals(next.tagName()))
                next.remove();
        }

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

    interface ImageGetterEx {
        Drawable getDrawable(Element element);
    }
}
