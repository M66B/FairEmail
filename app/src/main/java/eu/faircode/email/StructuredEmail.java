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

import java.util.Iterator;

// https://structured.email/content/introduction/getting_started.html
// https://schema.org/FlightReservation

public class StructuredEmail {
    private final JSONObject jroot;

    public StructuredEmail(String json) throws JSONException {
        jroot = new JSONObject(json);
    }

    public String getHtml(Context context) throws JSONException {
        StringBuilder sb = new StringBuilder();
        getHtml(jroot, 0, sb);

        Document d = Document.createShell("");
        d.appendElement("pre")
                .attr("style", "font-size: smaller !important;")
                .text(sb.toString());
        return d.html();
    }

    private void getHtml(Object obj, int indent, StringBuilder sb) throws JSONException {
        if (obj instanceof JSONObject) {
            JSONObject jobject = (JSONObject) obj;
            Iterator<String> keys = jobject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                if (key == null || key.startsWith("@"))
                    continue;
                Object v = (jobject.isNull(key) ? "" : jobject.get(key));
                if (v instanceof JSONObject || v instanceof JSONArray) {
                    sb.append(split(key))
                            .append(':')
                            .append('\n');
                    getHtml(v, indent + 1, sb);
                } else
                    sb.append(indent(indent))
                            .append(split(key))
                            .append(": ")
                            .append(v)
                            .append('\n');
            }
        } else if (obj instanceof JSONArray) {
            JSONArray jarray = (JSONArray) obj;
            for (int i = 0; i < jarray.length(); i++)
                getHtml(jarray.get(i), indent + 1, sb);
        }
    }

    private String split(String key) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < key.length(); i++) {
            char kar = key.charAt(i);
            if (Character.isUpperCase(kar))
                sb.append(' ').append(Character.toLowerCase(kar));
            else
                sb.append(kar);
        }
        return sb.toString();
    }

    private String indent(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++)
            sb.append("  ");
        return sb.toString();
    }
}
