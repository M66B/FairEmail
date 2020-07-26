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

    Copyright 2018-2020 by Marcel Bokhorst (M66B)
*/

import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.security.auth.x500.X500Principal;

@Entity(
        tableName = EntityCertificate.TABLE_NAME,
        foreignKeys = {
        },
        indices = {
                @Index(value = {"fingerprint", "email"}, unique = true),
                @Index(value = {"email"}),
        }
)
public class EntityCertificate {
    static final String TABLE_NAME = "certificate";

    @PrimaryKey(autoGenerate = true)
    public Long id;
    @NonNull
    public String fingerprint;
    @NonNull
    public boolean intermediate;
    @NonNull
    public String email;
    public String subject;
    public Long after;
    public Long before;
    @NonNull
    public String data;

    static EntityCertificate from(X509Certificate certificate, String email) throws CertificateEncodingException, NoSuchAlgorithmException {
        return from(certificate, false, email);
    }

    static EntityCertificate from(X509Certificate certificate, boolean intermediate, String email) throws CertificateEncodingException, NoSuchAlgorithmException {
        EntityCertificate record = new EntityCertificate();
        record.fingerprint = getFingerprint(certificate);
        record.intermediate = intermediate;
        record.email = email;
        record.subject = getSubject(certificate);

        Date after = certificate.getNotBefore();
        Date before = certificate.getNotAfter();

        record.after = (after == null ? null : after.getTime());
        record.before = (before == null ? null : before.getTime());

        record.data = Base64.encodeToString(certificate.getEncoded(), Base64.NO_WRAP);

        return record;
    }

    X509Certificate getCertificate() throws CertificateException {
        byte[] encoded = Base64.decode(this.data, Base64.NO_WRAP);
        return (X509Certificate) CertificateFactory.getInstance("X.509")
                .generateCertificate(new ByteArrayInputStream(encoded));
    }

    boolean isExpired() {
        return isExpired(null);
    }

    boolean isExpired(Date date) {
        if (date == null)
            date = new Date();
        long now = date.getTime();
        return ((this.after != null && now <= this.after) || (this.before != null && now > this.before));
    }

    static String getFingerprint(X509Certificate certificate) throws CertificateEncodingException, NoSuchAlgorithmException {
        return Helper.sha256(certificate.getEncoded());
    }

    static String getSubject(X509Certificate certificate) {
        return certificate.getSubjectX500Principal().getName(X500Principal.RFC2253);
    }

    static List<String> getEmailAddresses(X509Certificate certificate) {
        List<String> result = new ArrayList<>();

        try {
            Collection<List<?>> altNames = certificate.getSubjectAlternativeNames();
            if (altNames != null)
                for (List altName : altNames)
                    if (altName.get(0).equals(GeneralName.rfc822Name))
                        result.add((String) altName.get(1));
                    else
                        Log.i("Alt type=" + altName.get(0) + " data=" + altName.get(1));
        } catch (CertificateParsingException ex) {
            Log.e(ex);
        }

        try {
            X500Name name = new JcaX509CertificateHolder(certificate).getSubject();
            if (name != null) {
                List<RDN> rdns = new ArrayList<>();
                rdns.addAll(Arrays.asList(name.getRDNs(BCStyle.CN)));
                rdns.addAll(Arrays.asList(name.getRDNs(BCStyle.EmailAddress)));
                for (RDN rdn : rdns)
                    for (AttributeTypeAndValue tv : rdn.getTypesAndValues()) {
                        ASN1Encodable enc = tv.getValue();
                        if (enc == null)
                            continue;
                        String email = enc.toString().toLowerCase(Locale.ROOT);
                        if (result.contains(email))
                            continue;
                        if (!Helper.EMAIL_ADDRESS.matcher(email).matches())
                            continue;
                        result.add(email);
                    }
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }

        return result;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("intermediate", intermediate);
        json.put("email", email);
        json.put("data", data);
        return json;
    }

    public static EntityCertificate fromJSON(JSONObject json) throws JSONException, CertificateException, NoSuchAlgorithmException {
        EntityCertificate certificate = new EntityCertificate();
        // id
        certificate.intermediate = json.optBoolean("intermediate");
        certificate.email = json.getString("email");
        certificate.data = json.getString("data");

        X509Certificate cert = certificate.getCertificate();
        certificate.fingerprint = getFingerprint(cert);
        certificate.subject = getSubject(cert);

        Date after = cert.getNotBefore();
        Date before = cert.getNotAfter();

        certificate.after = (after == null ? null : after.getTime());
        certificate.before = (before == null ? null : before.getTime());

        return certificate;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EntityCertificate) {
            EntityCertificate other = (EntityCertificate) obj;
            return (this.fingerprint.equals(other.fingerprint) &&
                    this.intermediate == other.intermediate &&
                    Objects.equals(this.email, other.email) &&
                    Objects.equals(this.subject, other.subject));
        } else
            return false;
    }
}
