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

public class UriHelper {
    static String getParentDomain(String host) {
        if (host == null)
            return null;

        String[] h = host.split("\\.");
        if (h.length >= 2)
            return h[h.length - 2] + "." + h[h.length - 1];

        return host;
    }

    static String getEmailUser(String address) {
        if (address == null)
            return null;

        int at = address.indexOf('@');
        if (at > 0)
            return address.substring(0, at);

        return null;
    }

    static String getEmailDomain(String address) {
        if (address == null)
            return null;

        int at = address.indexOf('@');
        if (at > 0)
            return address.substring(at + 1);

        return null;
    }
}
