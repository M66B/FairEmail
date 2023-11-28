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
import android.net.Uri;
import android.text.TextUtils;

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
        String title;
        StringBuilder sb = new StringBuilder();

        if ("https://schema.org".equals(jroot.optString("@context")) &&
                "FlightReservation".equals(jroot.optString("@type"))) {
            // https://www.schema.org/FlightReservation
            title = "Flight reservation";

            sb.append("Reservation #");
            if (jroot.has("reservationId"))
                sb.append(jroot.getString("reservationId"));
            sb.append('\n');

            sb.append("Passenger: ");
            if (jroot.has("underName")) {
                JSONObject jundername = jroot.getJSONObject("underName");
                if ("Person".equals(jundername.optString("@type")) &&
                        jundername.has("name"))
                    sb.append(jundername.getString("name"));
            }
            sb.append('\n');

            sb.append("Flight: ");
            if (jroot.has("reservationFor")) {
                JSONObject jreservation = jroot.getJSONObject("reservationFor");
                if ("Flight".equals(jreservation.optString("@type"))) {
                    if (jreservation.has("flightNumber"))
                        sb.append(jreservation.getString("flightNumber"));
                }
            }
            sb.append('\n');

            sb.append("Operated by: ");
            if (jroot.has("reservationFor")) {
                JSONObject jreservation = jroot.getJSONObject("reservationFor");
                if ("Flight".equals(jreservation.optString("@type"))) {
                    if (jreservation.has("provider")) {
                        JSONObject jprovider = jreservation.getJSONObject("provider");
                        if (jprovider.has("name"))
                            sb.append(jprovider.getString("name"));
                    }
                }
            }
            sb.append('\n');

            sb.append("Departing: ");
            if (jroot.has("reservationFor")) {
                JSONObject jreservation = jroot.getJSONObject("reservationFor");
                if (jreservation.has("departureAirport")) {
                    JSONObject jdeparture = jreservation.getJSONObject("departureAirport");
                    if ("Airport".equals(jdeparture.getString("@type"))) {
                        if (jdeparture.has("name"))
                            sb.append(jdeparture.getString("name")).append(' ');
                        if (jdeparture.has("iataCode"))
                            sb.append('(').append(jdeparture.getString("iataCode")).append(") ");
                    }
                }
                if (jreservation.has("departureTime"))
                    sb.append(jreservation.getString("departureTime"));
            }
            sb.append('\n');

            sb.append("Arriving: ");
            if (jroot.has("reservationFor")) {
                JSONObject jreservation = jroot.getJSONObject("reservationFor");
                if (jreservation.has("arrivalAirport")) {
                    JSONObject jarrival = jreservation.getJSONObject("arrivalAirport");
                    if ("Airport".equals(jarrival.getString("@type"))) {
                        if (jarrival.has("name"))
                            sb.append(jarrival.getString("name")).append(' ');
                        if (jarrival.has("iataCode"))
                            sb.append('(').append(jarrival.getString("iataCode")).append(") ");
                    }
                }
                if (jreservation.has("arrivalTime"))
                    sb.append(jreservation.getString("arrivalTime"));
            }
            sb.append('\n');

            sb.append("Passenger sequence number: ");
            if (jroot.has("passengerSequenceNumber"))
                sb.append(jroot.getString("passengerSequenceNumber"));
            sb.append('\n');

            sb.append("Boarding priority: ");
            if (jroot.has("passengerPriorityStatus"))
                sb.append(jroot.getString("passengerPriorityStatus"));
            sb.append('\n');

            sb.append("Boarding policy: ");
            if (jroot.has("reservationFor")) {
                JSONObject jreservation = jroot.getJSONObject("reservationFor");
                if ("Flight".equals(jreservation.optString("@type"))) {
                    if (jreservation.has("provider")) {
                        JSONObject jprovider = jreservation.getJSONObject("provider");
                        if (jprovider.has("boardingPolicy")) {
                            String policy = jprovider.getString("boardingPolicy");
                            try {
                                Uri p = Uri.parse(policy);
                                String path = p.getPath();
                                if (!TextUtils.isEmpty(path)) {
                                    path = path.substring(1);
                                    if (!TextUtils.isEmpty(path))
                                        policy = split(path);
                                }
                            } catch (Throwable ex) {
                                Log.w(ex);
                            }
                            sb.append(policy);
                        }
                    }
                }
            }
            sb.append('\n');

            sb.append("Security screening: ");
            if (jroot.has("securityScreening"))
                sb.append(jroot.getString("securityScreening"));
            sb.append('\n');
        } else {
            title = "Linked data";
            getHtml(jroot, 0, sb);
        }

        Document d = Document.createShell("");
        d.appendElement("hr");
        d.appendElement("div")
                .attr("style", "font-size: larger !important;")
                .text(title);
        d.appendElement("br");
        d.appendElement("div")
                .attr("style", "font-size: smaller !important;")
                .html(HtmlHelper.formatPlainText(sb.toString()));
        d.appendElement("hr");
        return d.html();
    }

    private void getHtml(Object obj, int indent, StringBuilder sb) throws JSONException {
        if (obj instanceof JSONObject) {
            JSONObject jobject = (JSONObject) obj;
            Iterator<String> keys = jobject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                if (key == null)
                    continue;
                Object v = (jobject.isNull(key) ? "" : jobject.get(key));
                if (v instanceof JSONObject || v instanceof JSONArray) {
                    sb.append(split(key))
                            .append(':')
                            .append('\n');
                    getHtml(v, indent + 1, sb);
                } else {
                    sb.append(indent(indent))
                            .append(split(key))
                            .append(": ")
                            .append(v)
                            .append('\n');
                }
            }
        } else if (obj instanceof JSONArray) {
            JSONArray jarray = (JSONArray) obj;
            for (int i = 0; i < jarray.length(); i++)
                getHtml(jarray.get(i), indent + 1, sb);
        } else {
            sb.append(indent(indent))
                    .append(obj == null ? "" : obj.toString())
                    .append('\n');
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
