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

import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;

import androidx.paging.PagedList;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

public class BoundaryCallbackMessages extends PagedList.BoundaryCallback<TupleMessageEx> {
    private Context context;
    private Long folder;
    private boolean server;
    private String query;
    private int pageSize;

    private IBoundaryCallbackMessages intf;

    private Handler handler;
    private ExecutorService executor = Executors.newSingleThreadExecutor(Helper.backgroundThreadFactory);

    private boolean destroyed = false;
    private boolean error = false;
    private int index = 0;

    private List<Long> messages = null;

    private IMAPStore istore = null;
    private IMAPFolder ifolder = null;
    private Message[] imessages = null;

    interface IBoundaryCallbackMessages {
        void onLoading();

        void onLoaded(int fetched);

        void onError(Throwable ex);
    }

    BoundaryCallbackMessages(Context context, long folder, boolean server, String query, int pageSize) {
        this.context = context.getApplicationContext();
        this.folder = (folder < 0 ? null : folder);
        this.server = server;
        this.query = query;
        this.pageSize = pageSize;
    }

    void setCallback(IBoundaryCallbackMessages intf) {
        this.handler = new Handler();
        this.intf = intf;
    }

    @Override
    public void onZeroItemsLoaded() {
        Log.i("Boundary zero loaded");
        queue_load();
    }

    @Override
    public void onItemAtEndLoaded(final TupleMessageEx itemAtEnd) {
        Log.i("Boundary at end");
        queue_load();
    }

    private void queue_load() {
        executor.submit(new Runnable() {
            private int fetched;

            @Override
            public void run() {
                try {
                    if (destroyed || error)
                        return;

                    fetched = 0;
                    if (intf != null)
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                intf.onLoading();
                            }
                        });
                    if (server)
                        fetched = load_server();
                    else
                        fetched = load_device();
                } catch (final Throwable ex) {
                    Log.e("Boundary", ex);
                    if (intf != null)
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                intf.onError(ex);
                            }
                        });
                } finally {
                    if (intf != null)
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                intf.onLoaded(fetched);
                            }
                        });
                }
            }
        });
    }

    private int load_device() {
        DB db = DB.getInstance(context);

        int found = 0;
        try {
            db.beginTransaction();

            if (messages == null) {
                messages = db.message().getMessageIdsByFolder(folder);
                Log.i("Boundary device folder=" + folder + " query=" + query + " messages=" + messages.size());
            }

            for (int i = index; i < messages.size() && found < pageSize && !destroyed; i++) {
                index = i + 1;

                EntityMessage message = db.message().getMessage(messages.get(i));

                boolean match = false;
                if (query == null)
                    match = true;
                else {
                    String find = query.toLowerCase();
                    String body = null;
                    if (message.content)
                        try {
                            body = Helper.readText(message.getFile(context));
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
                }

                if (match) {
                    found++;
                    db.message().setMessageFound(message.account, message.thread);
                }
            }

            db.setTransactionSuccessful();

            if (found == pageSize)
                return found;
        } finally {
            db.endTransaction();
        }

        Log.i("Boundary device done");
        return found;
    }

    private int load_server() throws MessagingException, IOException, AuthenticatorException, OperationCanceledException {
        DB db = DB.getInstance(context);

        final EntityFolder browsable = db.folder().getBrowsableFolder(folder, query != null);
        if (browsable == null)
            return 0;

        EntityAccount account = db.account().getAccount(browsable.account);
        if (account == null)
            return 0;

        if (imessages == null)
            try {
                // Check connectivity
                if (!ConnectionHelper.getNetworkState(context).isSuitable())
                    throw new IllegalArgumentException(context.getString(R.string.title_no_internet));

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                boolean debug = (prefs.getBoolean("debug", false) || BuildConfig.BETA_RELEASE);

                String protocol = account.getProtocol();

                // Get properties
                Properties props = MessageHelper.getSessionProperties(account.auth_type, account.realm, account.insecure);
                props.put("mail." + protocol + ".separatestoreconnection", "true");

                // Create session
                Session isession = Session.getInstance(props, null);
                isession.setDebug(debug);

                Log.i("Boundary server connecting account=" + account.name);
                istore = (IMAPStore) isession.getStore(protocol);
                ConnectionHelper.connect(context, istore, account);

                Log.i("Boundary server opening folder=" + browsable.name);
                ifolder = (IMAPFolder) istore.getFolder(browsable.name);
                ifolder.open(Folder.READ_WRITE);

                Log.i("Boundary server query=" + query);
                if (query == null)
                    imessages = ifolder.getMessages();
                else {
                    Object result = ifolder.doCommand(new IMAPFolder.ProtocolCommand() {
                        @Override
                        public Object doCommand(IMAPProtocol protocol) {
                            // Yahoo! does not support keyword search, but uses the flags $Forwarded $Junk $NotJunk
                            boolean keywords = false;
                            for (String keyword : browsable.keywords)
                                if (!keyword.startsWith("$")) {
                                    keywords = true;
                                    break;
                                }

                            try {
                                // https://tools.ietf.org/html/rfc3501#section-6.4.4
                                Argument arg = new Argument();
                                if (query.startsWith("raw:") && istore.hasCapability("X-GM-EXT-1")) {
                                    // https://support.google.com/mail/answer/7190
                                    // https://developers.google.com/gmail/imap/imap-extensions#extension_of_the_search_command_x-gm-raw
                                    arg.writeAtom("X-GM-RAW");
                                    arg.writeString(query.substring(4));
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
                                    arg.writeBytes(query.getBytes());
                                    arg.writeAtom("TO");
                                    arg.writeBytes(query.getBytes());
                                    arg.writeAtom("SUBJECT");
                                    arg.writeBytes(query.getBytes());
                                    arg.writeAtom("BODY");
                                    arg.writeBytes(query.getBytes());
                                    if (keywords) {
                                        arg.writeAtom("KEYWORD");
                                        arg.writeBytes(query.getBytes());
                                    }
                                }

                                Log.i("Boundary UTF8 search=" + query);
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
                                        imessages[i] = ifolder.getMessage(msgnums.get(i));

                                    return imessages;
                                } else {
                                    // Assume no UTF-8 support
                                    String search = query.replace("ß", "ss"); // Eszett
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

                                    return ifolder.search(term);
                                }

                            } catch (MessagingException ex) {
                                Log.e(ex);
                                return ex;
                            }
                        }
                    });

                    if (result instanceof MessagingException)
                        throw (MessagingException) result;

                    imessages = (Message[]) result;
                }
                Log.i("Boundary server found messages=" + imessages.length);

                index = imessages.length - 1;
            } catch (Throwable ex) {
                error = true;
                if (ex instanceof FolderClosedException)
                    Log.w("Search", ex);
                else {
                    Log.e("Search", ex);
                    throw ex;
                }
            }

        int found = 0;
        while (index >= 0 && found < pageSize && !destroyed) {
            Log.i("Boundary server index=" + index);
            int from = Math.max(0, index - (pageSize - found) + 1);
            Message[] isub = Arrays.copyOfRange(imessages, from, index + 1);
            index -= (pageSize - found);

            FetchProfile fp = new FetchProfile();
            fp.add(FetchProfile.Item.ENVELOPE);
            fp.add(FetchProfile.Item.FLAGS);
            fp.add(FetchProfile.Item.CONTENT_INFO); // body structure
            fp.add(UIDFolder.FetchProfileItem.UID);
            fp.add(IMAPFolder.FetchProfileItem.HEADERS);
            fp.add(FetchProfile.Item.SIZE);
            fp.add(IMAPFolder.FetchProfileItem.INTERNALDATE);
            ifolder.fetch(isub, fp);

            try {
                db.beginTransaction();

                for (int j = isub.length - 1; j >= 0 && found < pageSize && !destroyed; j--)
                    try {
                        long uid = ifolder.getUID(isub[j]);
                        Log.i("Boundary server sync uid=" + uid);
                        EntityMessage message = db.message().getMessageByUid(browsable.id, uid);
                        if (message == null) {
                            message = Core.synchronizeMessage(context,
                                    account, browsable,
                                    ifolder, (IMAPMessage) isub[j],
                                    true,
                                    new ArrayList<EntityRule>());
                            found++;
                        }
                        db.message().setMessageFound(message.account, message.thread);
                    } catch (MessageRemovedException ex) {
                        Log.w(browsable.name + " boundary server", ex);
                    } catch (FolderClosedException ex) {
                        throw ex;
                    } catch (IOException ex) {
                        if (ex.getCause() instanceof MessagingException) {
                            Log.w(browsable.name + " boundary server", ex);
                            db.folder().setFolderError(browsable.id, Helper.formatThrowable(ex, true));
                        } else
                            throw ex;
                    } catch (Throwable ex) {
                        Log.e(browsable.name + " boundary server", ex);
                        db.folder().setFolderError(browsable.id, Helper.formatThrowable(ex, true));
                    } finally {
                        ((IMAPMessage) isub[j]).invalidateHeaders();
                    }

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }

        Log.i("Boundary server done");
        return found;
    }

    void clear() {
        destroyed = true;

        executor.submit(new Runnable() {
            @Override
            public void run() {
                Log.i("Boundary destroy");
                try {
                    if (istore != null)
                        istore.close();
                } catch (Throwable ex) {
                    Log.e("Boundary", ex);
                }
            }
        });
    }
}