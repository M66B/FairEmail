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

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Document;

// https://structured.email/content/introduction/getting_started.html
// https://schema.org/FlightReservation

public class StructuredEmail {
    private JSONObject jroot;

    public StructuredEmail(String json) throws JSONException {
        jroot = new JSONObject(json);
    }

    public String getHtml() throws JSONException {
        Document d = Document.createShell("");
        d.appendElement("pre")
                .attr("style", "font-size: smaller !important;")
                .text(jroot.toString(2));
        return d.html();
    }
}
