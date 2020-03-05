package com.sun.mail.imap.protocol;

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

import com.sun.mail.iap.Argument;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.util.MailLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import eu.faircode.email.Helper;
import eu.faircode.email.Log;

// https://github.com/javaee/javamail/blob/master/mail/src/main/java/com/sun/mail/imap/protocol/IMAPSaslAuthenticator.java
public class IMAPSaslAuthenticator implements SaslAuthenticator {
    private IMAPProtocol pr;
    private String name;
    private Properties props;
    private MailLogger logger;
    private String host;

    public IMAPSaslAuthenticator(
            IMAPProtocol pr, String name, Properties props,
            MailLogger logger, String host) {
        this.pr = pr;
        this.name = name;
        this.props = props;
        this.logger = logger;
        this.host = host;
    }

    @Override
    public boolean authenticate(
            String[] mechs, final String realm,
            final String authzid, final String u,
            final String p) throws ProtocolException {

        if (!pr.hasCapability("AUTH=CRAM-MD5"))
            throw new UnsupportedOperationException("SASL not supported");

        List<Response> v = new ArrayList<>();
        String tag = null;
        Response r = null;
        boolean done = false;

        try {
            Log.i("SASL IMAP command=AUTHENTICATE");
            Argument args = new Argument();
            args.writeAtom("CRAM-MD5");
            tag = pr.writeCommand("AUTHENTICATE", args);
            Log.i("SASL IMAP tag=" + tag);
        } catch (Exception ex) {
            r = Response.byeResponse(ex);
            done = true;
        }

        while (!done) {
            try {
                r = pr.readResponse();
                Log.i("SASL IMAP response=" + r);
                if (r.isContinuation()) {
                    byte[] nonce = Base64.decode(r.getRest(), Base64.NO_WRAP);
                    Log.i("SASL IMAP nonce=" + new String(nonce));
                    String hmac = Helper.HMAC("MD5", 64, p.getBytes(), nonce);
                    String hash = Base64.encodeToString((u + " " + hmac).getBytes(), Base64.NO_WRAP) + "\r\n";
                    Log.i("SASL IMAP hash=" + hash);
                    pr.getIMAPOutputStream().write(hash.getBytes());
                    pr.getIMAPOutputStream().flush();
                } else if (r.isTagged() && r.getTag().equals(tag))
                    done = true;
                else if (r.isBYE())
                    done = true;
            } catch (Exception ex) {
                r = Response.byeResponse(ex);
                done = true;
            }
            v.add(r);
        }

        try {
            Response[] responses = v.toArray(new Response[0]);
            pr.handleCapabilityResponse(responses);
            pr.notifyResponseHandlers(responses);
            pr.handleLoginResult(r);
            pr.setCapabilities(r);
        } catch (ProtocolException ex) {
            Log.w(ex);
            throw new UnsupportedOperationException("SASL not authenticated");
        }

        Log.i("SASL IMAP authenticated");
        return true;
    }
}
