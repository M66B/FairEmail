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

import java.io.InputStream;

import javax.mail.Flags;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

public class MimeMessageEx extends MimeMessage {
    private String msgid;
    private MimeMessage original;

    MimeMessageEx(MimeMessage imessage, String msgid) throws MessagingException {
        super(imessage);
        this.msgid = msgid;
    }

    MimeMessageEx(Session session, String msgid) {
        super(session);
        this.msgid = msgid;
    }

    MimeMessageEx(Session session, InputStream is, String msgid) throws MessagingException {
        super(session, is);
        this.msgid = msgid;
    }

    MimeMessageEx(Session session, InputStream is, MimeMessage original) throws MessagingException {
        super(session, is);
        this.original = original;
    }

    @Override
    public String getMessageID() throws MessagingException {
        if (this.msgid == null)
            return super.getMessageID();
        else
            return this.msgid;
    }

    @Override
    protected void updateMessageID() throws MessagingException {
        if (this.msgid == null)
            super.updateMessageID();
        else {
            setHeader("Message-ID", msgid);
            Log.i("Override Message-ID=" + msgid);
        }
    }

    public void updateMessageID(String msgid) throws MessagingException {
        this.msgid = msgid;
        updateMessageID();
    }

    @Override
    public synchronized Flags getFlags() throws MessagingException {
        if (original == null)
            return super.getFlags();
        else
            return original.getFlags();
    }

    @Override
    public synchronized boolean isSet(Flags.Flag flag) throws MessagingException {
        if (original == null)
            return super.isSet(flag);
        else
            return original.isSet(flag);
    }

    @Override
    public void setFlag(Flags.Flag flag, boolean set) throws MessagingException {
        if (original == null)
            super.setFlag(flag, set);
        else
            original.setFlag(flag, set);
    }

    @Override
    public synchronized void setFlags(Flags flag, boolean set) throws MessagingException {
        if (original == null)
            super.setFlags(flag, set);
        else
            original.setFlags(flag, set);
    }
}
