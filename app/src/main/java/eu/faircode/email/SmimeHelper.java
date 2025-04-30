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

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
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

    private static List<X509Certificate> readCACertificates(Context context) throws CertificateException, IOException {
        List<X509Certificate> result = new ArrayList<>();

        CertificateFactory fact = CertificateFactory.getInstance("X.509");
        for (String name : context.getAssets().list("smime"))
            if (name.endsWith(".pem")) {
                Log.i("S/MIME reading=" + name);
                int count = 0;
                try (InputStream is = context.getAssets().open("smime/" + name)) {
                    try (PemReader reader = new PemReader(new InputStreamReader(is))) {
                        PemObject pem = reader.readPemObject();
                        while (pem != null) {
                            count++;
                            ByteArrayInputStream bis = new ByteArrayInputStream(pem.getContent());
                            X509Certificate cert = (X509Certificate) fact.generateCertificate(bis);
                            //Log.i("S/MIME cert=" + cert.getSubjectDN().getName());
                            result.add(cert);
                            pem = reader.readPemObject();
                        }
                    }
                }
                Log.i("S/MIME certs=" + count);
            }

        Log.i("S/MIME total certs=" + result.size());
        return result;
    }

    static KeyStore getCAStore(Context context) throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        KeyStore aks = KeyStore.getInstance("AndroidCAStore");
        aks.load(null, null);

        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);

        Enumeration<String> aliases = aks.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            if (aks.isCertificateEntry(alias))
                ks.setCertificateEntry(alias, aks.getCertificate(alias));
        }

        int idx = 1;
        for (X509Certificate ca : SmimeHelper.readCACertificates(context)) {
            String alias = "asset:" + idx++ + ":" + ca.getSubjectDN().getName();
            ks.setCertificateEntry(alias, ca);
        }

        return ks;
    }
}
