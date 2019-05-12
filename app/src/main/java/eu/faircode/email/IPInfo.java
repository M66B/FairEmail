package eu.faircode.email;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class IPInfo {
    private static Map<String, String> hostOrganization = new HashMap<>();

    static String getOrganization(String host) throws IOException {
        synchronized (hostOrganization) {
            if (hostOrganization.containsKey(host))
                return hostOrganization.get(host);
        }
        InetAddress address = InetAddress.getByName(host);
        URL url = new URL("https://ipinfo.io/" + address.getHostAddress() + "/org");
        Log.i("GET " + url);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setReadTimeout(15 * 1000);
        connection.connect();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String organization = reader.readLine();
            if ("undefined".equals(organization))
                organization = null;
            synchronized (hostOrganization) {
                hostOrganization.put(host, organization);
            }
            return organization;
        }
    }
}
