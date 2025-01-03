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

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;

import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;

public class IMAPMessageEx extends IMAPMessage {
    protected IMAPMessageEx(IMAPFolder folder, int msgnum) {
        super(folder, msgnum);
    }

    protected IMAPMessageEx(Session session) {
        super(session);
    }

    @Override
    public synchronized void setFlags(Flags flag, boolean set) throws MessagingException {
        getFolder().setFlags(new Message[]{this}, flag, set);
    }
}
