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
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Misc {
    public static List<String> getISPDBUrls(Context context, String domain, String email) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean open_safe = prefs.getBoolean("open_safe", false);

        List<String> result = new ArrayList<>();

        result.addAll(Arrays.asList(
                "https://autoconfig." + domain + "/mail/config-v1.1.xml?emailaddress=" + email,
                "https://" + domain + "/.well-known/autoconfig/mail/config-v1.1.xml?emailaddress=" + email
        ));

        if (!open_safe)
            result.addAll(Arrays.asList(
                    "http://autoconfig." + domain + "/mail/config-v1.1.xml?emailaddress=" + email,
                    "http://" + domain + "/.well-known/autoconfig/mail/config-v1.1.xml?emailaddress=" + email
            ));

        return Collections.unmodifiableList(result);
    }

    public static List<String> getMSUrls(Context context, String domain, String email) {
        return Collections.unmodifiableList(Arrays.asList(
                "https://" + domain + "/autodiscover/autodiscover.xml",
                "https://autodiscover." + domain + "/autodiscover/autodiscover.xml"
        ));
    }
}
