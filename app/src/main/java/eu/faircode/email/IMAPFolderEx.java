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

import com.sun.mail.iap.Argument;
import com.sun.mail.iap.CommandFailedException;
import com.sun.mail.iap.ConnectionException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.Utility;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.ListInfo;
import com.sun.mail.imap.protocol.MessageSet;
import com.sun.mail.imap.protocol.UIDSet;

import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.FolderNotFoundException;
import javax.mail.Message;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.UIDFolder;

public class IMAPFolderEx extends IMAPFolder {
    public IMAPFolderEx(String fullName, char separator, IMAPStore store, Boolean isNamespace) {
        super(fullName, separator, store, isNamespace);
    }

    public IMAPFolderEx(ListInfo li, IMAPStore store) {
        super(li, store);
    }

    @Override
    public synchronized void setFlags(Message[] msgs, Flags flag, boolean value) throws MessagingException {
        checkOpened();
        // checkFlags(flag);

        if (msgs.length == 0)
            return;

        synchronized (messageCacheLock) {
            try {
                IMAPProtocol p = getProtocol();
                if (p.hasCapability("X-UIDONLY") ||
                        (p.hasCapability("UIDPLUS") &&
                                Boolean.parseBoolean(System.getProperty("fairemail.uid_command")))) {
                    // Verizon
                    FetchProfile fp = new FetchProfile();
                    fp.add(UIDFolder.FetchProfileItem.UID);
                    fetch(msgs, fp);

                    UIDSet[] uids = Utility.toUIDSet(msgs);
                    if (uids == null)
                        return;

                    Response[] r = p.command("UID STORE " + UIDSet.toString(uids) +
                            " " + (value ? '+' : '-') + "FLAGS " + p.createFlagList(new Flags(flag)), null);
                    p.notifyResponseHandlers(r);
                    p.handleResult(r[r.length - 1]);
                    return;
                }
                MessageSet[] ms = Utility.toMessageSetSorted(msgs, null);
                if (ms == null)
                    throw new MessageRemovedException("Messages have been removed");
                p.storeFlags(ms, flag, value);
            } catch (ConnectionException cex) {
                throw new FolderClosedException(this, cex.getMessage());
            } catch (ProtocolException pex) {
                throw new MessagingException(pex.getMessage(), pex);
            }
        }
    }

    @Override
    public synchronized void copyMessages(Message[] msgs, Folder folder) throws MessagingException {
        copyMessages(msgs, folder, false);
    }

    public synchronized void moveMessages(Message[] msgs, Folder folder) throws MessagingException {
        copyMessages(msgs, folder, true);
    }

    private synchronized void copyMessages(Message[] msgs, Folder folder, boolean move) throws MessagingException {
        checkOpened();

        if (msgs.length == 0)
            return;

        if (folder.getStore() == store) {
            synchronized (messageCacheLock) {
                try {
                    IMAPProtocol p = getProtocol();
                    if (p.hasCapability("X-UIDONLY") ||
                            (p.hasCapability("UIDPLUS") &&
                                    Boolean.parseBoolean(System.getProperty("fairemail.uid_command")))) {
                        // Verizon
                        FetchProfile fp = new FetchProfile();
                        fp.add(UIDFolder.FetchProfileItem.UID);
                        fetch(msgs, fp);

                        UIDSet[] uids = Utility.toUIDSet(msgs);
                        if (uids == null)
                            return;

                        Argument args = new Argument();
                        args.writeAtom(UIDSet.toString(uids));
                        p.writeMailboxName(args, folder.getFullName());
                        Response[] r = p.command(move ? "UID MOVE" : "UID COPY", args);
                        p.notifyResponseHandlers(r);
                        p.handleResult(r[r.length - 1]);
                        return;
                    }
                    MessageSet[] ms = Utility.toMessageSet(msgs, null);
                    if (ms == null)
                        throw new MessageRemovedException("Messages have been removed");
                    if (move)
                        p.move(ms, folder.getFullName());
                    else
                        p.copy(ms, folder.getFullName());
                } catch (CommandFailedException cfx) {
                    if (cfx.getMessage() != null &&
                            cfx.getMessage().contains("TRYCREATE")) {
                        Log.w(cfx);
                        throw new FolderNotFoundException(folder,
                                folder.getFullName() + " does not exist");
                    }
                    else
                        throw new MessagingException(cfx.getMessage(), cfx);
                } catch (ConnectionException cex) {
                    throw new FolderClosedException(this, cex.getMessage());
                } catch (ProtocolException pex) {
                    throw new MessagingException(pex.getMessage(), pex);
                }
            }
        } else {
            if (move)
                throw new MessagingException("Move between stores not supported");
            else
                super.copyMessages(msgs, folder);
        }
    }

    @Override
    public IMAPMessage newIMAPMessage(int msgnum) {
        return new IMAPMessageEx(this, msgnum);
    }
}
