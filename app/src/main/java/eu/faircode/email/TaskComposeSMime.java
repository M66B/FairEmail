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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.security.KeyChain;
import android.text.TextUtils;

import androidx.preference.PreferenceManager;

import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSAlgorithm;
import org.bouncycastle.cms.CMSEnvelopedData;
import org.bouncycastle.cms.CMSEnvelopedDataGenerator;
import org.bouncycastle.cms.CMSProcessableFile;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.RecipientInfoGenerator;
import org.bouncycastle.cms.SignerInfoGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.bouncycastle.cms.jcajce.JceKeyAgreeRecipientInfoGenerator;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.OutputEncryptor;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

public class TaskComposeSMime extends SimpleTask<Void> {
    @Override
    protected Void onExecute(Context context, Bundle args) throws Throwable {
        long id = args.getLong("id");
        int type = args.getInt("type");
        String alias = args.getString("alias");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean check_certificate = prefs.getBoolean("check_certificate", true);

        File tmp = Helper.ensureExists(new File(context.getFilesDir(), "encryption"));

        DB db = DB.getInstance(context);

        // Get data
        EntityMessage draft = db.message().getMessage(id);
        if (draft == null)
            throw new MessageRemovedException("S/MIME");
        EntityIdentity identity = db.identity().getIdentity(draft.identity);
        if (identity == null)
            throw new IllegalArgumentException(context.getString(R.string.title_from_missing));

        // Get/clean attachments
        List<EntityAttachment> attachments = db.attachment().getAttachments(id);
        for (EntityAttachment attachment : new ArrayList<>(attachments))
            if (attachment.encryption != null) {
                db.attachment().deleteAttachment(attachment.id);
                attachments.remove(attachment);
            }

        // Build message to sign
        //   openssl smime -verify <xxx.eml
        Properties props = MessageHelper.getSessionProperties(true);
        Session isession = Session.getInstance(props, null);
        MimeMessage imessage = new MimeMessage(isession);
        MessageHelper.build(context, draft, attachments, identity, true, imessage);
        imessage.saveChanges();
        BodyPart bpContent = new MimeBodyPart() {
            @Override
            public void setContent(Object content, String type) throws MessagingException {
                super.setContent(content, type);

                // https://javaee.github.io/javamail/FAQ#howencode
                updateHeaders();
                if (content instanceof Multipart) {
                    try {
                        MessageHelper.overrideContentTransferEncoding((Multipart) content);
                    } catch (IOException ex) {
                        Log.e(ex);
                    }
                } else
                    setHeader("Content-Transfer-Encoding", "base64");
            }
        };
        bpContent.setContent(imessage.getContent(), imessage.getContentType());

        if (alias == null)
            throw new IllegalArgumentException("Key alias missing");

        // Get private key
        PrivateKey privkey = KeyChain.getPrivateKey(context, alias);
        if (privkey == null)
            throw new IllegalArgumentException("Private key missing");

        // Get public key
        X509Certificate[] chain = KeyChain.getCertificateChain(context, alias);
        if (chain == null || chain.length == 0)
            throw new IllegalArgumentException("Certificate missing");

        if (check_certificate) {
            // Check public key validity
            try {
                chain[0].checkValidity();
                // TODO: check digitalSignature/nonRepudiation key usage
                // https://datatracker.ietf.org/doc/html/rfc3850#section-4.4.2
            } catch (CertificateException ex) {
                String msg = ex.getMessage();
                throw new IllegalArgumentException(
                        TextUtils.isEmpty(msg) ? Log.formatThrowable(ex) : msg);
            }

            // Check public key email
            boolean known = false;
            List<String> emails = EntityCertificate.getEmailAddresses(chain[0]);
            for (String email : emails)
                if (email.equalsIgnoreCase(identity.email)) {
                    known = true;
                    break;
                }

            if (!known && emails.size() > 0) {
                String message = identity.email + " (" + TextUtils.join(", ", emails) + ")";
                throw new IllegalArgumentException(
                        context.getString(R.string.title_certificate_missing, message),
                        new CertificateException());
            }
        }

        // Store selected alias
        db.identity().setIdentitySignKeyAlias(identity.id, alias);

        // Build content
        File sinput = new File(tmp, draft.id + ".smime_sign");
        if (EntityMessage.SMIME_SIGNONLY.equals(type))
            try (OutputStream os = new MessageHelper.CanonicalizingStream(
                    new BufferedOutputStream(new FileOutputStream(sinput)), EntityAttachment.SMIME_CONTENT, null)) {
                bpContent.writeTo(os);
            }
        else
            try (FileOutputStream fos = new FileOutputStream(sinput)) {
                bpContent.writeTo(fos);
            }

        if (EntityMessage.SMIME_SIGNONLY.equals(type)) {
            EntityAttachment cattachment = new EntityAttachment();
            cattachment.message = draft.id;
            cattachment.sequence = db.attachment().getAttachmentSequence(draft.id) + 1;
            cattachment.name = "content.asc";
            cattachment.type = "text/plain";
            cattachment.disposition = Part.INLINE;
            cattachment.encryption = EntityAttachment.SMIME_CONTENT;
            cattachment.id = db.attachment().insertAttachment(cattachment);

            File content = cattachment.getFile(context);
            Helper.copy(sinput, content);

            db.attachment().setDownloaded(cattachment.id, content.length());
        }

        // Sign
        Store store = new JcaCertStore(Arrays.asList(chain));
        CMSSignedDataGenerator cmsGenerator = new CMSSignedDataGenerator();
        cmsGenerator.addCertificates(store);

        String signAlgorithm = prefs.getString("sign_algo_smime", "SHA-256");

        String algorithm = privkey.getAlgorithm();
        if (TextUtils.isEmpty(algorithm) || "RSA".equals(algorithm))
            Log.i("Private key algorithm=" + algorithm);
        else
            Log.e("Private key algorithm=" + algorithm);

        if (TextUtils.isEmpty(algorithm))
            algorithm = "RSA";
        else if ("EC".equals(algorithm))
            algorithm = "ECDSA";

        algorithm = signAlgorithm.replace("-", "") + "with" + algorithm;
        Log.i("Sign algorithm=" + algorithm);

        ContentSigner contentSigner = new JcaContentSignerBuilder(algorithm)
                .build(privkey);
        DigestCalculatorProvider digestCalculator = new JcaDigestCalculatorProviderBuilder()
                .build();
        SignerInfoGenerator signerInfoGenerator = new JcaSignerInfoGeneratorBuilder(digestCalculator)
                .build(contentSigner, chain[0]);
        cmsGenerator.addSignerInfoGenerator(signerInfoGenerator);

        CMSTypedData cmsData = new CMSProcessableFile(sinput);
        CMSSignedData cmsSignedData = cmsGenerator.generate(cmsData);
        byte[] signedMessage = cmsSignedData.getEncoded();

        Helper.secureDelete(sinput);

        // Build signature
        if (EntityMessage.SMIME_SIGNONLY.equals(type)) {
            ContentType ct = new ContentType("application/pkcs7-signature");
            ct.setParameter("micalg", signAlgorithm.toLowerCase(Locale.ROOT));

            EntityAttachment sattachment = new EntityAttachment();
            sattachment.message = draft.id;
            sattachment.sequence = db.attachment().getAttachmentSequence(draft.id) + 1;
            sattachment.name = "smime.p7s";
            sattachment.type = ct.toString();
            sattachment.disposition = Part.INLINE;
            sattachment.encryption = EntityAttachment.SMIME_SIGNATURE;
            sattachment.id = db.attachment().insertAttachment(sattachment);

            File file = sattachment.getFile(context);
            try (OutputStream os = new FileOutputStream(file)) {
                os.write(signedMessage);
            }

            db.attachment().setDownloaded(sattachment.id, file.length());

            return null;
        }

        List<Address> addresses = new ArrayList<>();
        if (draft.to != null)
            addresses.addAll(Arrays.asList(draft.to));
        if (draft.cc != null)
            addresses.addAll(Arrays.asList(draft.cc));
        if (draft.bcc != null)
            addresses.addAll(Arrays.asList(draft.bcc));

        List<X509Certificate> certs = new ArrayList<>();

        boolean own = true;
        for (Address address : addresses) {
            boolean found = false;
            Throwable cex = null;
            String email = ((InternetAddress) address).getAddress();
            List<EntityCertificate> acertificates = db.certificate().getCertificateByEmail(email);
            if (acertificates != null)
                for (EntityCertificate acertificate : acertificates) {
                    X509Certificate cert = acertificate.getCertificate();
                    try {
                        cert.checkValidity();
                        certs.add(cert);
                        found = true;
                        if (cert.equals(chain[0]))
                            own = false;
                    } catch (CertificateException ex) {
                        Log.w(ex);
                        cex = ex;
                    }
                }

            if (!found)
                if (cex == null)
                    throw new IllegalArgumentException(
                            context.getString(R.string.title_certificate_missing, email));
                else
                    throw new IllegalArgumentException(
                            context.getString(R.string.title_certificate_invalid, email), cex);
        }

        // Allow sender to decrypt own message
        if (own)
            certs.add(chain[0]);

        // Build signature
        BodyPart bpSignature = new MimeBodyPart();
        bpSignature.setFileName("smime.p7s");
        bpSignature.setDataHandler(new DataHandler(new ByteArrayDataSource(signedMessage, "application/pkcs7-signature")));
        bpSignature.setDisposition(Part.INLINE);

        // Build message
        ContentType ct = new ContentType("multipart/signed");
        ct.setParameter("micalg", signAlgorithm.toLowerCase(Locale.ROOT));
        ct.setParameter("protocol", "application/pkcs7-signature");
        ct.setParameter("smime-type", "signed-data");
        String ctx = ct.toString();
        int slash = ctx.indexOf("/");
        Multipart multipart = new MimeMultipart(ctx.substring(slash + 1));
        multipart.addBodyPart(bpContent);
        multipart.addBodyPart(bpSignature);
        imessage.setContent(multipart);
        imessage.saveChanges();

        // Encrypt
        CMSEnvelopedDataGenerator cmsEnvelopedDataGenerator = new CMSEnvelopedDataGenerator();
        if ("EC".equals(privkey.getAlgorithm())) {
            // https://datatracker.ietf.org/doc/html/draft-ietf-smime-3278bis
            JceKeyAgreeRecipientInfoGenerator gen = new JceKeyAgreeRecipientInfoGenerator(
                    CMSAlgorithm.ECCDH_SHA256KDF,
                    privkey,
                    chain[0].getPublicKey(),
                    CMSAlgorithm.AES128_WRAP);
            for (X509Certificate cert : certs)
                gen.addRecipient(cert);
            cmsEnvelopedDataGenerator.addRecipientInfoGenerator(gen);
            // https://security.stackexchange.com/a/53960
            // throw new IllegalArgumentException("ECDSA cannot be used for encryption");
        } else {
            for (X509Certificate cert : certs) {
                RecipientInfoGenerator gen = new JceKeyTransRecipientInfoGenerator(cert);
                cmsEnvelopedDataGenerator.addRecipientInfoGenerator(gen);
            }
        }

        File einput = new File(tmp, draft.id + ".smime_encrypt");
        try (FileOutputStream fos = new FileOutputStream(einput)) {
            imessage.writeTo(fos);
        }
        CMSTypedData msg = new CMSProcessableFile(einput);

        ASN1ObjectIdentifier encryptionOID;
        String encryptAlgorithm = prefs.getString("encrypt_algo_smime", "AES-128");
        switch (encryptAlgorithm) {
            case "AES-128":
                encryptionOID = CMSAlgorithm.AES128_CBC;
                break;
            case "AES-192":
                encryptionOID = CMSAlgorithm.AES192_CBC;
                break;
            case "AES-256":
                encryptionOID = CMSAlgorithm.AES256_CBC;
                break;
            default:
                encryptionOID = CMSAlgorithm.AES128_CBC;
        }
        Log.i("Encryption algorithm=" + encryptAlgorithm + " OID=" + encryptionOID);

        OutputEncryptor encryptor = new JceCMSContentEncryptorBuilder(encryptionOID)
                .build();
        CMSEnvelopedData cmsEnvelopedData = cmsEnvelopedDataGenerator
                .generate(msg, encryptor);

        EntityAttachment attachment = new EntityAttachment();
        attachment.message = draft.id;
        attachment.sequence = db.attachment().getAttachmentSequence(draft.id) + 1;
        attachment.name = "smime.p7m";
        attachment.type = "application/pkcs7-mime";
        attachment.disposition = Part.INLINE;
        attachment.encryption = EntityAttachment.SMIME_MESSAGE;
        attachment.id = db.attachment().insertAttachment(attachment);

        File encrypted = attachment.getFile(context);
        try (OutputStream os = new FileOutputStream(encrypted)) {
            cmsEnvelopedData.toASN1Structure().encodeTo(os);
        }

        Helper.secureDelete(einput);

        db.attachment().setDownloaded(attachment.id, encrypted.length());

        return null;
    }

    @Override
    protected void onException(Bundle args, Throwable ex) {
        throw new NotImplementedException("LoaderDraft");
    }
}
