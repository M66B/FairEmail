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

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Objects;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

public class SmimeHelper {
    static boolean hasSmimeKey(Context context, List<Address> recipients, boolean all) {
        if (recipients == null || recipients.size() == 0)
            return false;

        int count = 0;
        DB db = DB.getInstance(context);
        for (Address address : recipients) {
            String email = ((InternetAddress) address).getAddress();
            List<EntityCertificate> certs = db.certificate().getCertificateByEmail(email);
            if (certs != null && certs.size() > 0)
                count++;
        }

        return (all ? count == recipients.size() : count > 0);
    }

    static boolean match(PrivateKey privkey, X509Certificate cert) {
        if (privkey == null || cert == null)
            return false;
        PublicKey pubkey = cert.getPublicKey();
        if (pubkey == null)
            return false;
        return Objects.equals(privkey.getAlgorithm(), pubkey.getAlgorithm());
    }
}
