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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Misc {
    public static List<String> getISPDBUrls(String domain, String email) {
        return Collections.unmodifiableList(Arrays.asList(
                "https://autoconfig." + domain + "/mail/config-v1.1.xml?emailaddress=" + email,
                "https://" + domain + "/.well-known/autoconfig/mail/config-v1.1.xml?emailaddress=" + email
        ));
    }
}
