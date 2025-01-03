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

import android.text.TextUtils;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

public class InternetAddressJson extends InternetAddress {
    private JSONObject json;

    private InternetAddressJson() {
    }

    private InternetAddressJson(String address) throws AddressException {
    }

    private InternetAddressJson(String address, boolean strict) throws AddressException {
    }

    private InternetAddressJson(String address, String personal) throws UnsupportedEncodingException {
    }

    private InternetAddressJson(String address, String personal, String charset) throws UnsupportedEncodingException {
    }

    public static Address from(JSONObject json) {
        InternetAddressJson result = new InternetAddressJson();
        result.json = json;
        return result;
    }

    @Override
    public Object clone() {
        ensureParsed();
        return super.clone();
    }

    @Override
    public String getAddress() {
        ensureParsed();
        return super.getAddress();
    }

    @Override
    public String getPersonal() {
        ensureParsed();
        return super.getPersonal();
    }

    @Override
    public String toString() {
        ensureParsed();
        return super.toString();
    }

    @Override
    public String toUnicodeString() {
        ensureParsed();
        return super.toUnicodeString();
    }

    @Override
    public boolean equals(Object a) {
        ensureParsed();
        return super.equals(a);
    }

    @Override
    public int hashCode() {
        ensureParsed();
        return super.hashCode();
    }

    @Override
    public void validate() throws AddressException {
        ensureParsed();
        super.validate();
    }

    private synchronized void ensureParsed() {
        if (this.json != null) {
            try {
                String email = json.getString("address");
                String personal = json.optString("personal");
                if (TextUtils.isEmpty(personal))
                    personal = null;
                this.setAddress(email);
                this.setPersonal(personal, StandardCharsets.UTF_8.name());
            } catch (Throwable ex) {
                Log.e(ex);
            }
            this.json = null;
        }
    }
}