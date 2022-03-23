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

    Copyright 2018-2022 by Marcel Bokhorst (M66B)
*/

import com.sun.mail.iap.ConnectionException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.UID;

import javax.mail.Flags;
import javax.mail.FolderClosedException;
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
        synchronized (getMessageCacheLock()) {
            try {
                IMAPProtocol p = getProtocol();
                checkExpunged(); // Insure that this message is not expunged
                if (p.hasCapability("X-UIDONLY") ||
                        "imap.mail.yahoo.co.jp".equals(p.getInetAddress().getHostName()) ||
                        (p.hasCapability("UIDPLUS") &&
                                Boolean.parseBoolean(System.getProperty("fairemail.uid_command")))) {
                    // Verizon
                    // Yahoo: NO [CANNOT] STORE It's not possible to perform specified operation
                    long uid = getUID();
                    if (uid < 0) {
                        UID u = p.fetchUID(getSequenceNumber());
                        if (u != null)
                            uid = u.uid;
                    }
                    Response[] r = p.command("UID STORE " + uid +
                            " " + (set ? '+' : '-') + "FLAGS " + p.createFlagList(new Flags(flag)), null);
                    p.notifyResponseHandlers(r);
                    p.handleResult(r[r.length - 1]);
                    return;
                }
                p.storeFlags(getSequenceNumber(), flag, set);
            } catch (ConnectionException cex) {
                throw new FolderClosedException(folder, cex.getMessage());
            } catch (ProtocolException pex) {
                throw new MessagingException(pex.getMessage(), pex);
            }
        }
    }
}
