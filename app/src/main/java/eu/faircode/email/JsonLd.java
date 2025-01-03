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

    Copyright 2018-2025 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.text.TextUtils;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

// https://json-ld.org/
// https://schema.org/
// https://schema.org/EmailMessage
// https://schema.org/FlightReservation
// https://developers.google.com/gmail/markup/reference/flight-reservation
// https://structured.email/content/introduction/getting_started.html

public class JsonLd {
    private Object jroot;
    private Throwable error = null;

    private static final String HTTP_SCHEMA_ORG = "http://schema.org";
    private static final String HTTPS_SCHEMA_ORG = "https://schema.org";
    private static final String PLACEHOLDER_START = "<!--";
    private static final String PLACEHOLDER_END = "-->";

    public JsonLd(String json) {
        try {
            if (TextUtils.isEmpty(json))
                jroot = null;
            else if (json.trim().startsWith("["))
                jroot = new JSONArray(json);
            else
                jroot = new JSONObject(json);
        } catch (Throwable ex) {
            Log.e(ex);
            error = ex;
        }
    }

    private String getTemplate(Context context, JSONObject jobject) {
        try {
            if (!jobject.has("@context") || jobject.isNull("@context"))
                return null;
            if (!jobject.has("@type") || jobject.isNull("@type"))
                return null;

            String jcontext = jobject.getString("@context");
            String jtype = jobject.getString("@type");
            Log.i("JSON-LD template " + jcontext + "=" + jtype);

            if (!HTTP_SCHEMA_ORG.equals(jcontext) &&
                    !HTTPS_SCHEMA_ORG.equals(jcontext)) {
                Log.e("JSON-LD " + jcontext + "?=" + jtype);
                // Organization, PromotionCard, DiscountOffer
                return null;
            }

            // https://github.com/json-path/JsonPath

            Configuration conf = Configuration.defaultConfiguration();
            Object document = conf.jsonProvider().parse(jobject.toString());

            String name = "schema.org/" + jtype.toLowerCase(Locale.ROOT) + ".html";
            Log.i("JSON-LD using=" + name);
            String template;
            try (InputStream is = context.getAssets().open(name)) {
                template = Helper.readStream(is);
            } catch (FileNotFoundException ex) {
                Log.e("JSON-LD " + jcontext + "=" + jtype + "?");
                throw ex;
            }

            int start = template.indexOf(PLACEHOLDER_START);
            while (start >= 0) {
                int end = template.indexOf(PLACEHOLDER_END, start + PLACEHOLDER_START.length());
                if (end < 0)
                    throw new IllegalArgumentException("Missing placeholder end @" + start);

                String placeholder = template.substring(start + PLACEHOLDER_START.length(), end).trim();

                String value;
                try {
                    value = JsonPath.read(document, placeholder);
                    if (value == null)
                        value = "";
                    if (value.startsWith(HTTP_SCHEMA_ORG + "/"))
                        value = unCamelCase(value.substring(HTTP_SCHEMA_ORG.length() + 1));
                    else if (value.startsWith(HTTPS_SCHEMA_ORG + "/"))
                        value = unCamelCase(value.substring(HTTPS_SCHEMA_ORG.length() + 1));
                } catch (PathNotFoundException ex) {
                    Log.i(ex);
                    value = "";
                }

                Log.i("JSON-LD " + placeholder + "=" + value);
                template = template.substring(0, start) + value + template.substring(end + PLACEHOLDER_END.length());

                start = template.indexOf(PLACEHOLDER_START);
            }

            return template;
        } catch (Throwable ex) {
            Log.w("JSON-LD", ex);
            return null;
        }
    }

    public String getHtml(Context context) {
        try {
            if (error != null)
                throw error;
            if (jroot == null)
                throw new IllegalArgumentException("JSON-LD empty");

            Document d = Document.createShell("");
            d.body().appendElement("hr");
            d.body().appendElement("div")
                    .appendElement("a")
                    .attr("style", "font-size: larger !important;")
                    .attr("href", "https://json-ld.org/")
                    .text("Linked data");
            d.body().appendElement("br");

            List<JSONObject> jschemas = new ArrayList<>();
            if (jroot instanceof JSONObject)
                jschemas.add((JSONObject) jroot);
            else if (jroot instanceof JSONArray) {
                JSONArray jarray = (JSONArray) jroot;
                for (int i = 0; i < jarray.length(); i++)
                    jschemas.add((JSONObject) jarray.get(i));
            }

            for (JSONObject jschema : jschemas) {
                String template = getTemplate(context, jschema);
                if (template != null) {
                    d.body().appendElement("div").append(template);
                    d.body().append("<br>");
                }
            }

            Element holder = d.body().appendElement("div")
                    .attr("style",
                            "font-family: monospace; font-size: smaller !important;");
            for (int i = 0; i < jschemas.size(); i++) {
                if (i > 0)
                    holder.appendElement("br");
                getHtml(context, jschemas.get(i), 0, holder);
            }
            d.body().appendElement("hr");
            return d.body().html();
        } catch (Throwable ex) {
            Log.e(ex);
            Document d = Document.createShell("");
            d.body().append("pre").text(Log.formatThrowable(ex, false));
            return d.body().html();
        }
    }

    private void getHtml(Context context, Object obj, int indent, Element holder) throws JSONException {
        if (obj instanceof JSONObject) {
            JSONObject jobject = (JSONObject) obj;

            Iterator<String> keys = jobject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                if (key == null)
                    continue;

                indent(indent, holder);
                Object v = (jobject.isNull(key) ? "" : jobject.get(key));
                holder.appendElement("strong")
                        .text(unCamelCase(key) + ": ");

                if (v instanceof JSONObject || v instanceof JSONArray) {
                    holder.appendElement("br");
                    getHtml(context, v, indent + 1, holder);
                } else {
                    String _v = v.toString();
                    if (_v.startsWith(HTTP_SCHEMA_ORG + "/"))
                        _v = unCamelCase(_v.substring(HTTP_SCHEMA_ORG.length() + 1));
                    else if (_v.startsWith(HTTPS_SCHEMA_ORG + "/"))
                        _v = unCamelCase(_v.substring(HTTPS_SCHEMA_ORG.length() + 1));
                    holder.appendElement("span").text(_v);
                    holder.appendElement("br");
                }
            }
        } else if (obj instanceof JSONArray) {
            JSONArray jarray = (JSONArray) obj;
            for (int i = 0; i < jarray.length(); i++)
                getHtml(context, jarray.get(i), indent, holder);
        } else {
            indent(indent, holder);
            String v = (obj == null ? "" : obj.toString());
            holder.appendElement("span").text(v);
            holder.appendElement("br");
        }
    }

    private static String unCamelCase(String key) {
        boolean split = false;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < key.length(); i++) {
            char kar = key.charAt(i);
            if (Character.isUpperCase(kar)) {
                if (split)
                    sb.append(kar);
                else {
                    split = true;
                    if (i > 0)
                        kar = Character.toLowerCase(kar);
                    sb.append(' ').append(kar);
                }
            } else {
                split = false;
                sb.append(kar);
            }
        }
        return sb.toString();
    }

    private static void indent(int count, Element holder) {
        if (count > 0) {
            Element span = holder.appendElement("span");
            for (int i = 0; i < count; i++)
                span.append("&nbsp;&nbsp;");
        }
    }
}
