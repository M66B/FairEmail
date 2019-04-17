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
import android.content.SharedPreferences;

import androidx.lifecycle.ViewModel;
import androidx.preference.PreferenceManager;

import com.sun.mail.iap.Argument;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.IMAPResponse;

import java.io.IOException;
import java.text.Normalizer;
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

public class ViewModelBrowse extends ViewModel {
    private State currentState = null;

    private class State {
        private Context context;
        private Long fid;
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
        currentState.fid = (folder < 0 ? null : folder);
        currentState.search = search;
        currentState.pageSize = pageSize;
        currentState.index = -1;
        currentState.error = false;
    }

    int load() throws MessagingException, IOException {
        final State state = currentState;
        if (state == null || state.error)
            return 0;

        DB db = DB.getInstance(state.context);

        int local = 0;
        if (state.search != null)
            try {
                db.beginTransaction();

                if (state.messages == null) {
                    state.messages = db.message().getMessageIdsByFolder(state.fid);
                    Log.i("Boundary search folder=" + state.fid + " messages=" + state.messages.size());
                }

                for (int i = state.local; i < state.messages.size() && local < state.pageSize; i++) {
                    state.local = i + 1;

                    boolean match = false;
                    String find = state.search.toLowerCase();
                    EntityMessage message = db.message().getMessage(state.messages.get(i));
                    String body = null;
                    if (message.content)
                        try {
                            body = Helper.readText(message.getFile(state.context));
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

                    if (match) {
                        local++;
                        db.message().setMessageFound(message.account, message.thread);
                    }
                }

                db.setTransactionSuccessful();

                if (local == state.pageSize)
                    return local;
            } finally {
                db.endTransaction();
            }

        if (state.fid == null)
            return local;

        final EntityFolder folder = db.folder().getBrowsableFolder(state.fid, state.search != null);
        if (folder == null)
            return local;
        EntityAccount account = db.account().getAccount(folder.account);
        if (account == null)
            return local;

        if (state.imessages == null) {

            try {
                // Check connectivity
                if (!Helper.getNetworkState(state.context).isSuitable())
                    throw new IllegalArgumentException(state.context.getString(R.string.title_no_internet));

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(state.context);
                boolean debug = (prefs.getBoolean("debug", false) || BuildConfig.BETA_RELEASE);

                String protocol = account.getProtocol();

                // Get properties
                Properties props = MessageHelper.getSessionProperties(account.auth_type, account.realm, account.insecure);
                props.put("mail." + protocol + ".separatestoreconnection", "true");

                // Create session
                Session isession = Session.getInstance(props, null);
                isession.setDebug(debug);

                Log.i("Boundary connecting account=" + account.name);
                state.istore = (IMAPStore) isession.getStore(protocol);
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
                            // Yahoo! does not support keyword search, but uses the flags $Forwarded $Junk $NotJunk
                            boolean keywords = false;
                            for (String keyword : folder.keywords)
                                if (!keyword.startsWith("$")) {
                                    keywords = true;
                                    break;
                                }

                            try {
                                // https://tools.ietf.org/html/rfc3501#section-6.4.4
                                Argument arg = new Argument();
                                if (state.search.startsWith("raw:") && state.istore.hasCapability("X-GM-EXT-1")) {
                                    // https://support.google.com/mail/answer/7190
                                    // https://developers.google.com/gmail/imap/imap-extensions#extension_of_the_search_command_x-gm-raw
                                    arg.writeAtom("X-GM-RAW");
                                    arg.writeString(state.search.substring(4));
                                } else {
                                    if (!protocol.supportsUtf8()) {
                                        arg.writeAtom("CHARSET");
                                        arg.writeAtom("UTF-8");
                                    }
                                    if (keywords)
                                        arg.writeAtom("OR");
                                    arg.writeAtom("OR");
                                    arg.writeAtom("OR");
                                    arg.writeAtom("OR");
                                    arg.writeAtom("FROM");
                                    arg.writeBytes(state.search.getBytes());
                                    arg.writeAtom("TO");
                                    arg.writeBytes(state.search.getBytes());
                                    arg.writeAtom("SUBJECT");
                                    arg.writeBytes(state.search.getBytes());
                                    arg.writeAtom("BODY");
                                    arg.writeBytes(state.search.getBytes());
                                    if (keywords) {
                                        arg.writeAtom("KEYWORD");
                                        arg.writeBytes(state.search.getBytes());
                                    }
                                }

                                Log.i("Boundary UTF8 search=" + state.search);
                                Response[] responses = protocol.command("SEARCH", arg);
                                if (responses.length > 0 && responses[responses.length - 1].isOK()) {
                                    List<Integer> msgnums = new ArrayList<>();

                                    for (Response response : responses)
                                        if (((IMAPResponse) response).keyEquals("SEARCH")) {
                                            int msgnum;
                                            while ((msgnum = response.readNumber()) != -1)
                                                msgnums.add(msgnum);
                                        }

                                    Message[] imessages = new Message[msgnums.size()];
                                    for (int i = 0; i < msgnums.size(); i++)
                                        imessages[i] = state.ifolder.getMessage(msgnums.get(i));

                                    return imessages;
                                } else {
                                    // Assume no UTF-8 support
                                    String search = state.search.replace("ß", "ss"); // Eszett
                                    search = Normalizer.normalize(search, Normalizer.Form.NFD)
                                            .replaceAll("[^\\p{ASCII}]", "");

                                    Log.i("Boundary ASCII search=" + search);
                                    SearchTerm term = new OrTerm(
                                            new OrTerm(
                                                    new FromStringTerm(search),
                                                    new RecipientStringTerm(Message.RecipientType.TO, search)
                                            ),
                                            new OrTerm(
                                                    new SubjectTerm(search),
                                                    new BodyTerm(search)
                                            )
                                    );

                                    if (keywords)
                                        term = new OrTerm(term, new FlagTerm(
                                                new Flags(Helper.sanitizeKeyword(search)), true));

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

        int remote = 0;
        while (state.index >= 0 && remote < state.pageSize && currentState != null) {
            Log.i("Boundary index=" + state.index);
            int from = Math.max(0, state.index - (state.pageSize - remote) + 1);
            Message[] isub = Arrays.copyOfRange(state.imessages, from, state.index + 1);
            state.index -= (state.pageSize - remote);

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
                            message = Core.synchronizeMessage(state.context,
                                    account, folder,
                                    state.ifolder, (IMAPMessage) isub[j],
                                    true,
                                    new ArrayList<EntityRule>());
                            remote++;
                        }
                        db.message().setMessageFound(message.account, message.thread);
                    } catch (MessageRemovedException ex) {
                        Log.w(folder.name + " boundary", ex);
                    } catch (FolderClosedException ex) {
                        throw ex;
                    } catch (IOException ex) {
                        if (ex.getCause() instanceof MessagingException) {
                            Log.w(folder.name + " boundary", ex);
                            db.folder().setFolderError(folder.id, Helper.formatThrowable(ex, true));
                        } else
                            throw ex;
                    } catch (Throwable ex) {
                        Log.e(folder.name + " boundary", ex);
                        db.folder().setFolderError(folder.id, Helper.formatThrowable(ex, true));
                    } finally {
                        ((IMAPMessage) isub[j]).invalidateHeaders();
                    }

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }

        Log.i("Boundary done");
        return local + remote;
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
        }
    }
}
