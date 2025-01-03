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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class Whois {
    // https://datatracker.ietf.org/doc/html/rfc3912
    static final String WHOIS_INFO = "https://en.wikipedia.org/wiki/WHOIS";
    private static final int WHOIS_PORT = 43;
    private static final int WHOIS_TIMEOUT = 15 * 1000; // milliseconds
    private static final String WHOIS_IANA = "whois.iana.org";
    private static final String WHOIS_PREFIX = "whois:";

    static String get(String domain) throws IOException {
        return get(domain, getServer(domain), WHOIS_PORT);
    }

    static String get(String domain, String host) throws IOException {
        return get(domain, host, WHOIS_PORT);
    }

    static String get(String domain, String host, int port) throws IOException {
        Socket socket = ConnectionHelper.getSocket(host, port);
        socket.connect(new InetSocketAddress(host, port), WHOIS_TIMEOUT);
        try {
            byte[] request = (domain + "\r\n").getBytes(StandardCharsets.UTF_8);
            socket.getOutputStream().write(request);
            String response = Helper.readStream(socket.getInputStream(), StandardCharsets.UTF_8);
            return host + ":" + port + "\n\n" + response;
        } finally {
            socket.close();
        }
    }

    private static String getServer(String domain) throws IOException {
        String iana = get(domain, WHOIS_IANA);
        for (String line : iana.split("\\r?\\n"))
            if (line.startsWith(WHOIS_PREFIX))
                return line.substring(WHOIS_PREFIX.length()).trim();
        Log.w(iana);
        throw new UnknownHostException("whois server unknown " + domain);
    }
}
