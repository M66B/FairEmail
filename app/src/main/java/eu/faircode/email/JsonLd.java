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

    Copyright 2018-2023 by Marcel Bokhorst (M66B)
*/

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.Iterator;

// https://json-ld.org/
// https://schema.org/
// https://schema.org/FlightReservation
// https://structured.email/content/introduction/getting_started.html

public class JsonLd {
    private Object jroot;
    private Throwable error = null;

    private static final String URI_SCHEMA_ORG = "https://schema.org/";

    public JsonLd(String json) {
        try {
            if (json != null && json.trim().startsWith("["))
                jroot = new JSONArray(json);
            else
                jroot = new JSONObject(json);
        } catch (Throwable ex) {
            Log.e(ex);
            error = ex;
        }
    }

    public String getHtml(Context context) {
        try {
            if (error != null)
                throw error;

            Document d = Document.createShell("");
            d.body().appendElement("hr");
            d.body().appendElement("div")
                    .attr("style",
                            "font-family: monospace; font-size: larger !important;")
                    .appendElement("a")
                    .attr("href", "https://json-ld.org/")
                    .text("Linked data");
            d.body().appendElement("br");
            Element holder = d.body().appendElement("div")
                    .attr("style",
                            "font-family: monospace; font-size: smaller !important;");
            getHtml(jroot, 0, holder);
            d.body().appendElement("hr");
            return d.body().html();
        } catch (Throwable ex) {
            Log.e(ex);
            Document d = Document.createShell("");
            d.body().append("pre").text(Log.formatThrowable(ex, false));
            return d.body().html();
        }
    }

    private void getHtml(Object obj, int indent, Element holder) throws JSONException {
        if (obj instanceof JSONObject) {
            JSONObject jobject = (JSONObject) obj;

            if (indent == 0 &&
                    jobject.has("@context") &&
                    jobject.has("@type")) {
                String context = (jobject.isNull("@context") ? null : jobject.optString("@context"));
                String type = (jobject.isNull("@type") ? null : jobject.optString("@type"));
                Log.e("JSON-LD " + context + "=" + type);
            }

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
                    getHtml(v, indent + 1, holder);
                } else {
                    String _v = v.toString();
                    if (_v.startsWith(URI_SCHEMA_ORG))
                        _v = unCamelCase(_v.substring(URI_SCHEMA_ORG.length()));
                    holder.appendElement("span").text(_v);
                    holder.appendElement("br");
                }
            }
        } else if (obj instanceof JSONArray) {
            JSONArray jarray = (JSONArray) obj;
            for (int i = 0; i < jarray.length(); i++) {
                if (indent == 0 && i > 0)
                    holder.appendElement("br");
                getHtml(jarray.get(i), indent, holder);
            }
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
                    sb.append(' ').append(Character.toLowerCase(kar));
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
