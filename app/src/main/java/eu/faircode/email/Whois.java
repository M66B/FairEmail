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

    Copyright 2018-2022 by Marcel Bokhorst (M66B)
*/

import java.io.IOException;
import java.net.Socket;

public class Whois {
    private static final String WHOIS_HOST = "whois.internic.net";
    private static final int WHOIS_PORT = 43;

    static String get(String domain) throws IOException {
        return get(domain, WHOIS_HOST, WHOIS_PORT);
    }

    static String get(String domain, String host) throws IOException {
        return get(domain, host, WHOIS_PORT);
    }

    static String get(String domain, String host, int port) throws IOException {
        try (Socket socket = new Socket(host, port)) {
            socket.getOutputStream().write((domain + "\n").getBytes());
            return Helper.readStream(socket.getInputStream());
        }
    }
}
