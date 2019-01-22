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

    Copyright 2018-2019 by Marcel Bokhorst (M66B)
*/

import android.content.Context;

import com.sun.mail.iap.Argument;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.IMAPResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.Message;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.UIDFolder;
import javax.mail.search.BodyTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.FromStringTerm;
import javax.mail.search.OrTerm;
import javax.mail.search.RecipientStringTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;

import androidx.lifecycle.ViewModel;

public class ViewModelBrowse extends ViewModel {
    private State currentState = null;

    private class State {
        private Context context;
        private long fid;
        private String search;
        private int pageSize;

        int local = 0;
        List<Long> messages = null;
        IMAPStore istore = null;
        IMAPFolder ifolder = null;
        Message[] imessages = null;

        int index = -1;
        boolean error = false;
    }

    void set(Context context, long folder, String search, int pageSize) {
        currentState = new State();
        currentState.context = context;
        currentState.fid = folder;
        currentState.search = search;
        currentState.pageSize = pageSize;
        currentState.index = -1;
        currentState.error = false;
    }

    boolean isSearching() {
        State state = currentState;
        return (state != null && state.search != null);
    }

    void load() throws MessagingException, IOException {
        final State state = currentState;
        if (state == null || state.error)
            return;

        DB db = DB.getInstance(state.context);
        final List<EntityFolder> folders = db.folder().getFolders(
                state.fid < 0 ? null : state.fid, state.search != null);
        Log.i("Search fid=" + (state.fid < 0 ? null : state.fid) + " search=" + (state.search == null) + " count=" + folders.size());
        if (folders.size() == 0)
            return;

        if (state.search != null)
            try {
                db.beginTransaction();

                if (state.messages == null) {
                    state.messages = new ArrayList<>();
                    for (EntityFolder folder : folders)
                        state.messages.addAll(db.message().getMessageByFolder(folder.id));
                    Log.i("Messages=" + state.messages.size());
                }

                int matched = 0;
                for (int i = state.local; i < state.messages.size() && matched < state.pageSize; i++) {
                    state.local = i + 1;

                    boolean match = false;
                    String find = state.search.toLowerCase();
                    EntityMessage message = db.message().getMessage(state.messages.get(i));
                    String body = null;
                    if (message.content)
                        try {
                            body = Helper.readText(EntityMessage.getFile(state.context, message.id));
                        } catch (IOException ex) {
                            Log.e(ex);
                        }

                    if (message.from != null)
                        for (int j = 0; j < message.from.length && !match; j++)
                            match = message.from[j].toString().toLowerCase().contains(find);

                    if (message.to != null)
                        for (int j = 0; j < message.to.length && !match; j++)
                            match = message.to[j].toString().toLowerCase().contains(find);

                    if (message.subject != null && !match)
                        match = message.subject.toLowerCase().contains(find);

                    if (!match && message.content)
                        match = body.toLowerCase().contains(find);

                    if (match)
                        db.message().setMessageFound(message.account, message.thread);
                }

                db.setTransactionSuccessful();

                if (++matched >= state.pageSize)
                    return;
            } finally {
                db.endTransaction();
            }

        if (folders.size() > 1)
            return;

        final EntityFolder folder = folders.get(0);
        if (state.imessages == null) {
            EntityAccount account = db.account().getAccount(folder.account);

            try {
                Properties props = MessageHelper.getSessionProperties(account.auth_type, account.realm, account.insecure);
                Session isession = Session.getInstance(props, null);
                isession.setDebug(true);

                Log.i("Boundary connecting account=" + account.name);
                state.istore = (IMAPStore) isession.getStore(account.starttls ? "imap" : "imaps");
                Helper.connect(state.context, state.istore, account);

                Log.i("Boundary opening folder=" + folder.name);
                state.ifolder = (IMAPFolder) state.istore.getFolder(folder.name);
                state.ifolder.open(Folder.READ_WRITE);

                Log.i("Boundary searching=" + state.search);
                if (state.search == null)
                    state.imessages = state.ifolder.getMessages();
                else {
                    Object result = state.ifolder.doCommand(new IMAPFolder.ProtocolCommand() {
                        @Override
                        public Object doCommand(IMAPProtocol protocol) {
                            try {
                                if (protocol.supportsUtf8()) {
                                    // SEARCH OR OR FROM "x" TO "x" OR SUBJECT "x" BODY "x" ALL
                                    // SEARCH OR OR OR FROM "x" TO "x" OR SUBJECT "x" BODY "x" (KEYWORD x) ALL
                                    Argument arg = new Argument();
                                    if (folder.keywords.length > 0)
                                        arg.writeAtom("OR");
                                    arg.writeAtom("OR");
                                    arg.writeAtom("OR");
                                    arg.writeAtom("FROM");
                                    arg.writeBytes(state.search.getBytes());
                                    arg.writeAtom("TO");
                                    arg.writeBytes(state.search.getBytes());
                                    arg.writeAtom("OR");
                                    arg.writeAtom("SUBJECT");
                                    arg.writeBytes(state.search.getBytes());
                                    arg.writeAtom("BODY");
                                    arg.writeBytes(state.search.getBytes());
                                    if (folder.keywords.length > 0) {
                                        arg.writeAtom("KEYWORD");
                                        arg.writeBytes(state.search.getBytes());
                                    }
                                    arg.writeAtom("ALL");
                                    Response[] responses = protocol.command("SEARCH", arg);

                                    int msgnum;
                                    List<Integer> msgnums = new ArrayList<>();
                                    for (int i = 0; i < responses.length; i++) {
                                        if (responses[i] instanceof IMAPResponse) {
                                            IMAPResponse ir = (IMAPResponse) responses[i];
                                            if (ir.keyEquals("SEARCH")) {
                                                while ((msgnum = ir.readNumber()) != -1)
                                                    msgnums.add(msgnum);
                                            }
                                        } else {
                                            if (responses[i].isOK())
                                                Log.i(folder.name + " response=" + responses[i]);
                                            else
                                                throw new MessagingException(responses[i].toString());
                                        }
                                    }

                                    Message[] imessages = new Message[msgnums.size()];
                                    for (int i = 0; i < msgnums.size(); i++)
                                        imessages[i] = state.ifolder.getMessage(msgnums.get(i));

                                    return imessages;
                                } else {
                                    SearchTerm term = new OrTerm(
                                            new OrTerm(
                                                    new FromStringTerm(state.search),
                                                    new RecipientStringTerm(Message.RecipientType.TO, state.search)
                                            ),
                                            new OrTerm(
                                                    new SubjectTerm(state.search),
                                                    new BodyTerm(state.search)
                                            )
                                    );

                                    if (folder.keywords.length > 0)
                                        term = new OrTerm(term, new FlagTerm(
                                                new Flags(Helper.sanitizeKeyword(state.search)), true));

                                    return state.ifolder.search(term);
                                }
                            } catch (MessagingException ex) {
                                Log.e(ex);
                                return ex;
                            }
                        }
                    });

                    if (result instanceof MessagingException)
                        throw (MessagingException) result;

                    state.imessages = (Message[]) result;
                }
                Log.i("Boundary found messages=" + state.imessages.length);

                state.index = state.imessages.length - 1;
            } catch (Throwable ex) {
                state.error = true;
                if (ex instanceof FolderClosedException)
                    Log.w("Search", ex);
                else {
                    Log.e("Search", ex);
                    throw ex;
                }
            }
        }

        int count = 0;
        while (state.index >= 0 && count < state.pageSize && currentState != null) {
            Log.i("Boundary index=" + state.index);
            int from = Math.max(0, state.index - (state.pageSize - count) + 1);
            Message[] isub = Arrays.copyOfRange(state.imessages, from, state.index + 1);
            state.index -= (state.pageSize - count);

            FetchProfile fp = new FetchProfile();
            fp.add(FetchProfile.Item.ENVELOPE);
            fp.add(FetchProfile.Item.FLAGS);
            fp.add(FetchProfile.Item.CONTENT_INFO); // body structure
            fp.add(UIDFolder.FetchProfileItem.UID);
            fp.add(IMAPFolder.FetchProfileItem.HEADERS);
            fp.add(FetchProfile.Item.SIZE);
            fp.add(IMAPFolder.FetchProfileItem.INTERNALDATE);
            state.ifolder.fetch(isub, fp);

            try {
                db.beginTransaction();

                for (int j = isub.length - 1; j >= 0; j--)
                    try {
                        long uid = state.ifolder.getUID(isub[j]);
                        Log.i("Boundary sync uid=" + uid);
                        EntityMessage message = db.message().getMessageByUid(folder.id, uid);
                        if (message == null) {
                            message = ServiceSynchronize.synchronizeMessage(state.context,
                                    folder, state.ifolder, (IMAPMessage) isub[j],
                                    true,
                                    new ArrayList<EntityRule>());
                            count++;
                        }
                        db.message().setMessageFound(message.account, message.thread);
                    } catch (MessageRemovedException ex) {
                        Log.w(folder.name + " boundary", ex);
                    } catch (FolderClosedException ex) {
                        throw ex;
                    } catch (IOException ex) {
                        if (ex.getCause() instanceof MessagingException) {
                            Log.w(folder.name + " boundary", ex);
                            if (!(ex.getCause() instanceof MessageRemovedException))
                                db.folder().setFolderError(folder.id, Helper.formatThrowable(ex));
                        } else
                            throw ex;
                    } catch (Throwable ex) {
                        Log.e(folder.name + " boundary", ex);
                        db.folder().setFolderError(folder.id, Helper.formatThrowable(ex));
                    } finally {
                        ((IMAPMessage) isub[j]).invalidateHeaders();
                    }

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }

        Log.i("Boundary done");
    }

    void clear() {
        State state = currentState;
        if (state == null)
            return;
        currentState = null;

        Log.i("Boundary clear");
        try {
            if (state.istore != null)
                state.istore.close();
        } catch (Throwable ex) {
            Log.e("Boundary", ex);
        } finally {
            state.context = null;
            state.messages = null;
            state.istore = null;
            state.ifolder = null;
            state.imessages = null;
        }
    }
}
