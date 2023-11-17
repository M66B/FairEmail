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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.preference.PreferenceManager;

import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.openintents.openpgp.OpenPgpError;
import org.openintents.openpgp.util.OpenPgpApi;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

public class LoaderComposePgp extends SimpleTask<Object> {
    private String[] pgpUserIds;
    private long[] pgpKeyIds;
    private long pgpSignKeyId;

    @Override
    protected Object onExecute(Context context, Bundle args) throws Throwable {
        // Get arguments
        Intent data = args.getParcelable("data");
        Bundle largs = data.getBundleExtra(BuildConfig.APPLICATION_ID);
        long id = largs.getLong("id", -1);
        String session = largs.getString("session");

        if (data.hasExtra(OpenPgpApi.EXTRA_USER_IDS))
            pgpUserIds = data.getStringArrayExtra(OpenPgpApi.EXTRA_USER_IDS);

        DB db = DB.getInstance(context);

        // Get data
        EntityMessage draft = db.message().getMessage(id);
        if (draft == null)
            throw new MessageRemovedException("PGP");
        if (draft.identity == null)
            throw new IllegalArgumentException(context.getString(R.string.title_from_missing));
        EntityIdentity identity = db.identity().getIdentity(draft.identity);
        if (identity == null)
            throw new IllegalArgumentException(context.getString(R.string.title_from_missing));

        // Create files
        File tmp = Helper.ensureExists(new File(context.getFilesDir(), "encryption"));
        File input = new File(tmp, draft.id + "_" + session + ".pgp_input");
        File output = new File(tmp, draft.id + "_" + session + ".pgp_output");

        // Serializing messages is NOT reproducible
        if ((EntityMessage.PGP_SIGNONLY.equals(draft.ui_encrypt) &&
                OpenPgpApi.ACTION_GET_SIGN_KEY_ID.equals(data.getAction())) ||
                (EntityMessage.PGP_ENCRYPTONLY.equals(draft.ui_encrypt) &&
                        OpenPgpApi.ACTION_GET_KEY_IDS.equals(data.getAction())) ||
                (EntityMessage.PGP_SIGNENCRYPT.equals(draft.ui_encrypt) &&
                        OpenPgpApi.ACTION_GET_KEY_IDS.equals(data.getAction()))) {
            // Get/clean attachments
            List<EntityAttachment> attachments = db.attachment().getAttachments(draft.id);
            for (EntityAttachment attachment : new ArrayList<>(attachments))
                if (attachment.isEncryption()) {
                    db.attachment().deleteAttachment(attachment.id);
                    attachments.remove(attachment);
                }

            // Build message
            Properties props = MessageHelper.getSessionProperties(true);
            Session isession = Session.getInstance(props, null);
            MimeMessage imessage = new MimeMessage(isession);
            MessageHelper.build(context, draft, attachments, identity, true, imessage);

            if (OpenPgpApi.ACTION_GET_SIGN_KEY_ID.equals(data.getAction())) {
                // Serialize content
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

                try (OutputStream out = new MessageHelper.CanonicalizingStream(
                        new BufferedOutputStream(new FileOutputStream(input)), EntityAttachment.PGP_CONTENT, null)) {
                    bpContent.writeTo(out);
                }
            } else {
                // Serialize message
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                boolean encrypt_subject = prefs.getBoolean("encrypt_subject", false);
                if (encrypt_subject) {
                    // https://tools.ietf.org/id/draft-autocrypt-lamps-protected-headers-01.html
                    imessage.saveChanges();
                    BodyPart bpContent = new MimeBodyPart() {
                        @Override
                        public void setContent(Object content, String type) throws MessagingException {
                            super.setContent(content, type);

                            updateHeaders();

                            ContentType ct = new ContentType(type);
                            ct.setParameter("protected-headers", "v1");
                            setHeader("Content-Type", ct.toString());
                            String subject = (draft.subject == null ? "" : draft.subject);
                            try {
                                setHeader("Subject", MimeUtility.encodeWord(subject));
                            } catch (UnsupportedEncodingException ex) {
                                Log.e(ex);
                                setHeader("Subject", subject);
                            }
                        }
                    };

                    bpContent.setContent(imessage.getContent(), imessage.getContentType());

                    try (OutputStream out = new FileOutputStream(input)) {
                        bpContent.writeTo(out);
                    }
                } else
                    try (OutputStream out = new FileOutputStream(input)) {
                        imessage.writeTo(out);
                    }
            }
        }

        Intent result;
        if (OpenPgpApi.ACTION_GET_SIGN_KEY_ID.equals(data.getAction()) && identity.sign_key != null) {
            // Short circuit
            result = data;
            result.putExtra(OpenPgpApi.RESULT_CODE, OpenPgpApi.RESULT_CODE_SUCCESS);
            result.putExtra(OpenPgpApi.EXTRA_SIGN_KEY_ID, identity.sign_key);
        } else {
            // Call OpenPGP
            result = PgpHelper.execute(context, data, new FileInputStream(input), new FileOutputStream(output));
        }

        // Process result
        try {
            int resultCode = result.getIntExtra(OpenPgpApi.RESULT_CODE, OpenPgpApi.RESULT_CODE_ERROR);
            switch (resultCode) {
                case OpenPgpApi.RESULT_CODE_SUCCESS:
                    // Attach key, signed/encrypted data
                    if (OpenPgpApi.ACTION_GET_KEY.equals(data.getAction()) ||
                            OpenPgpApi.ACTION_DETACHED_SIGN.equals(data.getAction()) ||
                            OpenPgpApi.ACTION_ENCRYPT.equals(data.getAction()) ||
                            OpenPgpApi.ACTION_SIGN_AND_ENCRYPT.equals(data.getAction()))
                        try {
                            db.beginTransaction();

                            String name;
                            ContentType ct = new ContentType("application/octet-stream");
                            int encryption;
                            if (OpenPgpApi.ACTION_GET_KEY.equals(data.getAction())) {
                                name = "keydata.asc";
                                encryption = EntityAttachment.PGP_KEY;
                            } else if (OpenPgpApi.ACTION_DETACHED_SIGN.equals(data.getAction())) {
                                name = "signature.asc";
                                encryption = EntityAttachment.PGP_SIGNATURE;
                                String micalg = result.getStringExtra(OpenPgpApi.RESULT_SIGNATURE_MICALG);
                                if (TextUtils.isEmpty(micalg))
                                    throw new IllegalArgumentException("micalg missing");
                                ct = new ContentType("application/pgp-signature");
                                ct.setParameter("micalg", micalg);
                            } else if (OpenPgpApi.ACTION_ENCRYPT.equals(data.getAction()) ||
                                    OpenPgpApi.ACTION_SIGN_AND_ENCRYPT.equals(data.getAction())) {
                                name = "encrypted.asc";
                                encryption = EntityAttachment.PGP_MESSAGE;
                            } else
                                throw new IllegalStateException(data.getAction());

                            EntityAttachment attachment = new EntityAttachment();
                            attachment.message = draft.id;
                            attachment.sequence = db.attachment().getAttachmentSequence(draft.id) + 1;
                            attachment.name = name;
                            attachment.type = ct.toString();
                            attachment.disposition = Part.INLINE;
                            attachment.encryption = encryption;
                            attachment.id = db.attachment().insertAttachment(attachment);

                            File file = attachment.getFile(context);

                            // // 550 5.6.11 SMTPSEND.BareLinefeedsAreIllegal; message contains bare linefeeds, which cannot be sent via DATA and receiving system does not support BDAT (failed)
                            // https://learn.microsoft.com/en-us/exchange/troubleshoot/email-delivery/ndr/fix-error-code-550-5-6-11-in-exchange-online
                            try (InputStream is = new BufferedInputStream(
                                    OpenPgpApi.ACTION_DETACHED_SIGN.equals(data.getAction())
                                            ? new ByteArrayInputStream(result.getByteArrayExtra(OpenPgpApi.RESULT_DETACHED_SIGNATURE))
                                            : new FileInputStream(output))) {
                                try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
                                    int b;
                                    while ((b = is.read()) >= 0)
                                        if (b != '\r') {
                                            if (b == '\n')
                                                os.write('\r');
                                            os.write(b);
                                        }
                                }
                            }

                            db.attachment().setDownloaded(attachment.id, file.length());

                            db.setTransactionSuccessful();
                        } finally {
                            db.endTransaction();
                        }

                    // Sign-only: [get sign key id], get key, detached sign
                    // Sign/encrypt: get key ids, [get sign key id], get key, sign and encrypt

                    if (OpenPgpApi.ACTION_GET_KEY_IDS.equals(data.getAction())) {
                        // Sign/encrypt
                        pgpKeyIds = result.getLongArrayExtra(OpenPgpApi.EXTRA_KEY_IDS);
                        Log.i("Keys=" + pgpKeyIds.length);
                        if (pgpKeyIds.length == 0) // One key can be for multiple users
                            throw new IllegalArgumentException(context.getString(R.string.title_key_missing,
                                    TextUtils.join(", ", pgpUserIds)));

                        if (EntityMessage.PGP_ENCRYPTONLY.equals(draft.ui_encrypt)) {
                            // Encrypt message
                            Intent intent = new Intent(OpenPgpApi.ACTION_ENCRYPT);
                            intent.putExtra(OpenPgpApi.EXTRA_KEY_IDS, pgpKeyIds);
                            intent.putExtra(OpenPgpApi.EXTRA_REQUEST_ASCII_ARMOR, true);
                            intent.putExtra(BuildConfig.APPLICATION_ID, largs);
                            return intent;
                        } else {
                            if (identity.sign_key != null) {
                                pgpSignKeyId = identity.sign_key;

                                // Get public key
                                Intent intent = new Intent(OpenPgpApi.ACTION_GET_KEY);
                                intent.putExtra(OpenPgpApi.EXTRA_KEY_ID, pgpSignKeyId);
                                intent.putExtra(OpenPgpApi.EXTRA_MINIMIZE, true);
                                intent.putExtra(OpenPgpApi.EXTRA_MINIMIZE_USER_ID, identity.email);
                                intent.putExtra(OpenPgpApi.EXTRA_REQUEST_ASCII_ARMOR, true);
                                intent.putExtra(BuildConfig.APPLICATION_ID, largs);
                                return intent;
                            } else {
                                // Get sign key
                                Intent intent = new Intent(OpenPgpApi.ACTION_GET_SIGN_KEY_ID);
                                intent.putExtra(BuildConfig.APPLICATION_ID, largs);
                                return intent;
                            }
                        }
                    } else if (OpenPgpApi.ACTION_GET_SIGN_KEY_ID.equals(data.getAction())) {
                        pgpSignKeyId = result.getLongExtra(OpenPgpApi.EXTRA_SIGN_KEY_ID, -1);
                        if (pgpSignKeyId == 0)
                            throw new IllegalArgumentException(context.getString(R.string.title_no_sign_key));
                        db.identity().setIdentitySignKey(identity.id, pgpSignKeyId);

                        // Get public key
                        Intent intent = new Intent(OpenPgpApi.ACTION_GET_KEY);
                        intent.putExtra(OpenPgpApi.EXTRA_KEY_ID, pgpSignKeyId);
                        intent.putExtra(OpenPgpApi.EXTRA_MINIMIZE, true);
                        intent.putExtra(OpenPgpApi.EXTRA_MINIMIZE_USER_ID, identity.email);
                        intent.putExtra(OpenPgpApi.EXTRA_REQUEST_ASCII_ARMOR, true);
                        intent.putExtra(BuildConfig.APPLICATION_ID, largs);
                        return intent;
                    } else if (OpenPgpApi.ACTION_GET_KEY.equals(data.getAction())) {
                        if (EntityMessage.PGP_SIGNONLY.equals(draft.ui_encrypt)) {
                            // Get signature
                            Intent intent = new Intent(OpenPgpApi.ACTION_DETACHED_SIGN);
                            intent.putExtra(OpenPgpApi.EXTRA_SIGN_KEY_ID, pgpSignKeyId);
                            intent.putExtra(OpenPgpApi.EXTRA_REQUEST_ASCII_ARMOR, true);
                            intent.putExtra(BuildConfig.APPLICATION_ID, largs);
                            return intent;
                        } else if (EntityMessage.PGP_SIGNENCRYPT.equals(draft.ui_encrypt)) {
                            // Encrypt message
                            Intent intent = new Intent(OpenPgpApi.ACTION_SIGN_AND_ENCRYPT);
                            intent.putExtra(OpenPgpApi.EXTRA_KEY_IDS, pgpKeyIds);
                            intent.putExtra(OpenPgpApi.EXTRA_SIGN_KEY_ID, pgpSignKeyId);
                            intent.putExtra(OpenPgpApi.EXTRA_REQUEST_ASCII_ARMOR, true);
                            intent.putExtra(BuildConfig.APPLICATION_ID, largs);
                            return intent;
                        } else
                            throw new IllegalArgumentException("Invalid encrypt=" + draft.ui_encrypt);
                    } else if (OpenPgpApi.ACTION_DETACHED_SIGN.equals(data.getAction())) {
                        EntityAttachment attachment = new EntityAttachment();
                        attachment.message = draft.id;
                        attachment.sequence = db.attachment().getAttachmentSequence(draft.id) + 1;
                        attachment.name = "content.asc";
                        attachment.type = "text/plain";
                        attachment.disposition = Part.INLINE;
                        attachment.encryption = EntityAttachment.PGP_CONTENT;
                        attachment.id = db.attachment().insertAttachment(attachment);

                        File file = attachment.getFile(context);
                        input.renameTo(file);

                        db.attachment().setDownloaded(attachment.id, file.length());

                        // send message
                        args.putInt("action", largs.getInt("action"));
                        args.putBundle("extras", largs.getBundle("extras"));
                        return null;
                    } else if (OpenPgpApi.ACTION_ENCRYPT.equals(data.getAction()) ||
                            OpenPgpApi.ACTION_SIGN_AND_ENCRYPT.equals(data.getAction())) {
                        Helper.secureDelete(input);

                        // send message
                        args.putInt("action", largs.getInt("action"));
                        args.putBundle("extras", largs.getBundle("extras"));
                        return null;
                    } else
                        throw new IllegalStateException("Unknown action=" + data.getAction());

                case OpenPgpApi.RESULT_CODE_USER_INTERACTION_REQUIRED:
                    args.putBoolean("interactive", largs.getBoolean("interactive"));
                    return result.getParcelableExtra(OpenPgpApi.RESULT_INTENT);

                case OpenPgpApi.RESULT_CODE_ERROR:
                    Helper.secureDelete(input);
                    db.identity().setIdentitySignKey(identity.id, null);
                    OpenPgpError error = result.getParcelableExtra(OpenPgpApi.RESULT_ERROR);
                    if (error != null &&
                            error.getErrorId() == 0 && error.getMessage() == null)
                        error.setMessage("General error");
                    throw new IllegalArgumentException(
                            "OpenPgp" +
                                    " error " + (error == null ? "?" : error.getErrorId()) +
                                    ": " + (error == null ? "?" : error.getMessage()));

                default:
                    throw new IllegalStateException("OpenPgp unknown result code=" + resultCode);
            }
        } finally {
            Helper.secureDelete(output);
        }
    }

    @Override
    protected void onException(Bundle args, Throwable ex) {
        throw new NotImplementedException("LoaderDraft");
    }
}
