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

    Copyright 2018-2020 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.paging.PagedList;
import androidx.preference.PreferenceManager;

import com.sun.mail.iap.Argument;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.IMAPResponse;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;

import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.Message;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.ReadOnlyFolderException;
import javax.mail.UIDFolder;
import javax.mail.internet.MimeMessage;
import javax.mail.search.AndTerm;
import javax.mail.search.BodyTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.FromStringTerm;
import javax.mail.search.OrTerm;
import javax.mail.search.RecipientStringTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;

import io.requery.android.database.sqlite.SQLiteDatabase;

public class BoundaryCallbackMessages extends PagedList.BoundaryCallback<TupleMessageEx> {
    private Context context;
    private Long folder;
    private boolean server;
    private String query;
    private int pageSize;

    private IBoundaryCallbackMessages intf;

    private Handler handler;
    private ExecutorService executor = Helper.getBackgroundExecutor(1, "boundary");

    private State state;

    private static final int SEARCH_LIMIT = 1000;

    interface IBoundaryCallbackMessages {
        void onLoading();

        void onLoaded(int fetched);

        void onException(@NonNull Throwable ex);
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
        this.state = new State();
    }

    @Override
    public void onZeroItemsLoaded() {
        Log.i("Boundary zero loaded");
        queue_load(true);
    }

    @Override
    public void onItemAtEndLoaded(@NonNull final TupleMessageEx itemAtEnd) {
        Log.i("Boundary at end");
        queue_load(false);
    }

    private void queue_load(final boolean zero) {
        final State state = this.state;

        executor.submit(new Runnable() {
            private int fetched;

            @Override
            public void run() {
                try {
                    if (state.destroyed || state.error)
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
                        fetched = load_server(state);
                    else
                        fetched = load_device(state);
                } catch (final Throwable ex) {
                    Log.e("Boundary", ex);
                    if (intf != null)
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                intf.onException(ex);
                            }
                        });
                } finally {
                    if (intf != null)
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                intf.onLoaded(zero ? fetched : Integer.MAX_VALUE);
                            }
                        });
                }
            }
        });
    }

    private int load_device(State state) {
        DB db = DB.getInstance(context);

        Boolean seen = null;
        Boolean flagged = null;
        Boolean snoozed = null;
        Boolean encrypted = null;
        String find = (TextUtils.isEmpty(query) ? null : query.toLowerCase(Locale.ROOT));
        if (find != null && find.startsWith(context.getString(R.string.title_search_special_prefix) + ":")) {
            String special = find.split(":")[1];
            if (context.getString(R.string.title_search_special_unseen).equals(special))
                seen = false;
            else if (context.getString(R.string.title_search_special_flagged).equals(special))
                flagged = true;
            else if (context.getString(R.string.title_search_special_snoozed).equals(special))
                snoozed = true;
            else if (context.getString(R.string.title_search_special_encrypted).equals(special))
                encrypted = true;
        }

        int found = 0;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean fts = prefs.getBoolean("fts", false);
        boolean pro = ActivityBilling.isPro(context);
        if (fts && pro && seen == null && flagged == null && snoozed == null && encrypted == null) {
            if (state.ids == null) {
                SQLiteDatabase sdb = FtsDbHelper.getInstance(context);
                state.ids = FtsDbHelper.match(sdb, folder, query);
            }

            try {
                db.beginTransaction();

                for (; state.index < state.ids.size() && found < pageSize && !state.destroyed; state.index++) {
                    found++;
                    db.message().setMessageFound(state.ids.get(state.index));
                }
                db.setTransactionSuccessful();

            } finally {
                db.endTransaction();
            }

            return found;
        }

        try {
            db.beginTransaction();

            while (found < pageSize && !state.destroyed) {
                if (state.matches == null ||
                        (state.matches.size() > 0 && state.index >= state.matches.size())) {
                    state.matches = db.message().matchMessages(
                            folder,
                            "%" + find + "%",
                            seen, flagged, snoozed, encrypted,
                            SEARCH_LIMIT, state.offset);
                    Log.i("Boundary device folder=" + folder +
                            " query=" + query +
                            " seen=" + seen +
                            " flagged=" + flagged +
                            " snoozed=" + snoozed +
                            " encrypted=" + encrypted +
                            " offset=" + state.offset +
                            " size=" + state.matches.size());
                    state.offset += Math.min(state.matches.size(), SEARCH_LIMIT);
                    state.index = 0;
                }

                if (state.matches.size() == 0)
                    break;

                for (int i = state.index; i < state.matches.size() && found < pageSize && !state.destroyed; i++) {
                    state.index = i + 1;

                    TupleMatch match = state.matches.get(i);

                    if (find == null || seen != null || flagged != null || snoozed != null || encrypted != null)
                        match.matched = true;
                    else {
                        if (match.matched == null || !match.matched)
                            try {
                                File file = EntityMessage.getFile(context, match.id);
                                if (file.exists()) {
                                    String html = Helper.readText(file);
                                    if (html.toLowerCase(Locale.ROOT).contains(find)) {
                                        String text = HtmlHelper.getText(html);
                                        if (text.toLowerCase(Locale.ROOT).contains(find))
                                            match.matched = true;
                                    }
                                }
                            } catch (IOException ex) {
                                Log.e(ex);
                            }
                    }

                    if (match.matched != null && match.matched) {
                        found++;
                        db.message().setMessageFound(match.id);
                    }
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

    private int load_server(State state) throws MessagingException, IOException {
        DB db = DB.getInstance(context);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final boolean search_text = prefs.getBoolean("search_text", true);
        final boolean debug = (prefs.getBoolean("debug", false) || BuildConfig.BETA_RELEASE);

        final EntityFolder browsable = db.folder().getBrowsableFolder(folder, query != null);
        if (browsable == null)
            return 0;

        EntityAccount account = db.account().getAccount(browsable.account);
        if (account == null)
            return 0;

        if (state.imessages == null)
            try {
                // Check connectivity
                if (!ConnectionHelper.getNetworkState(context).isSuitable())
                    throw new IllegalStateException(context.getString(R.string.title_no_internet));

                Log.i("Boundary server connecting account=" + account.name);
                state.iservice = new EmailService(context, account.getProtocol(), account.realm, account.insecure, false, debug);
                state.iservice.setPartialFetch(account.partial_fetch);
                state.iservice.setIgnoreBodyStructureSize(account.ignore_size);
                state.iservice.connect(account);

                Log.i("Boundary server opening folder=" + browsable.name);
                state.ifolder = (IMAPFolder) state.iservice.getStore().getFolder(browsable.name);
                try {
                    state.ifolder.open(Folder.READ_WRITE);
                    db.folder().setFolderReadOnly(browsable.id, state.ifolder.getUIDNotSticky());
                } catch (ReadOnlyFolderException ex) {
                    state.ifolder.open(Folder.READ_ONLY);
                    db.folder().setFolderReadOnly(browsable.id, true);
                }

                int count = state.ifolder.getMessageCount();
                db.folder().setFolderTotal(browsable.id, count < 0 ? null : count);

                Log.i("Boundary server query=" + query);
                if (query == null) {
                    boolean filter_seen = prefs.getBoolean("filter_seen", false);
                    boolean filter_unflagged = prefs.getBoolean("filter_unflagged", false);
                    Log.i("Boundary filter seen=" + filter_seen + " unflagged=" + filter_unflagged);

                    SearchTerm searchUnseen = null;
                    if (filter_seen && state.ifolder.getPermanentFlags().contains(Flags.Flag.SEEN))
                        searchUnseen = new FlagTerm(new Flags(Flags.Flag.SEEN), false);

                    SearchTerm searchFlagged = null;
                    if (filter_unflagged && state.ifolder.getPermanentFlags().contains(Flags.Flag.FLAGGED))
                        searchFlagged = new FlagTerm(new Flags(Flags.Flag.FLAGGED), true);

                    if (searchUnseen != null && searchFlagged != null)
                        state.imessages = state.ifolder.search(new AndTerm(searchUnseen, searchFlagged));
                    else if (searchUnseen != null)
                        state.imessages = state.ifolder.search(searchUnseen);
                    else if (searchFlagged != null)
                        state.imessages = state.ifolder.search(searchFlagged);
                    else
                        state.imessages = state.ifolder.getMessages();
                } else if (query.startsWith(context.getString(R.string.title_search_special_prefix) + ":")) {
                    String special = query.split(":")[1];
                    if (context.getString(R.string.title_search_special_unseen).equals(special))
                        state.imessages = state.ifolder.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
                    else if (context.getString(R.string.title_search_special_flagged).equals(special))
                        state.imessages = state.ifolder.search(new FlagTerm(new Flags(Flags.Flag.FLAGGED), true));
                    else
                        state.imessages = new Message[0];
                } else {
                    Object result = state.ifolder.doCommand(new IMAPFolder.ProtocolCommand() {
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
                                if (query.startsWith("raw:") && state.iservice.hasCapability("X-GM-EXT-1")) {
                                    // https://support.google.com/mail/answer/7190
                                    // https://developers.google.com/gmail/imap/imap-extensions#extension_of_the_search_command_x-gm-raw
                                    arg.writeAtom("X-GM-RAW");
                                    arg.writeString(query.substring(4));
                                } else {
                                    if (!protocol.supportsUtf8()) {
                                        arg.writeAtom("CHARSET");
                                        arg.writeAtom(StandardCharsets.UTF_8.name());
                                    }
                                    arg.writeAtom("OR");
                                    arg.writeAtom("OR");
                                    if (search_text)
                                        arg.writeAtom("OR");
                                    if (keywords)
                                        arg.writeAtom("OR");
                                    arg.writeAtom("FROM");
                                    arg.writeBytes(query.getBytes());
                                    arg.writeAtom("TO");
                                    arg.writeBytes(query.getBytes());
                                    arg.writeAtom("SUBJECT");
                                    arg.writeBytes(query.getBytes());
                                    if (search_text) {
                                        arg.writeAtom("BODY");
                                        arg.writeBytes(query.getBytes());
                                    }
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
                                        imessages[i] = state.ifolder.getMessage(msgnums.get(i));

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
                                                new Flags(MessageHelper.sanitizeKeyword(search)), true));

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
                Log.i("Boundary server found messages=" + state.imessages.length);

                state.index = state.imessages.length - 1;
            } catch (Throwable ex) {
                state.error = true;
                if (ex instanceof FolderClosedException)
                    Log.w("Search", ex);
                else
                    Log.e("Search", ex);
                throw ex;
            }

        List<EntityRule> rules = db.rule().getEnabledRules(browsable.id);

        int found = 0;
        while (state.index >= 0 && found < pageSize && !state.destroyed) {
            Log.i("Boundary server index=" + state.index);
            int from = Math.max(0, state.index - (pageSize - found) + 1);
            Message[] isub = Arrays.copyOfRange(state.imessages, from, state.index + 1);
            state.index -= (pageSize - found);

            FetchProfile fp0 = new FetchProfile();
            fp0.add(UIDFolder.FetchProfileItem.UID);
            state.ifolder.fetch(isub, fp0);

            List<Message> add = new ArrayList<>();
            for (Message m : isub)
                try {
                    long uid = state.ifolder.getUID(m);
                    if (db.message().getMessageByUid(browsable.id, uid) == null)
                        add.add(m);
                } catch (Throwable ignored) {
                    add.add(m);
                }

            Log.i("Boundary fetching " + add.size() + "/" + isub.length);
            if (add.size() > 0) {
                FetchProfile fp = new FetchProfile();
                fp.add(FetchProfile.Item.ENVELOPE);
                fp.add(FetchProfile.Item.FLAGS);
                fp.add(FetchProfile.Item.CONTENT_INFO); // body structure
                fp.add(UIDFolder.FetchProfileItem.UID);
                fp.add(IMAPFolder.FetchProfileItem.HEADERS);
                //fp.add(IMAPFolder.FetchProfileItem.MESSAGE);
                fp.add(FetchProfile.Item.SIZE);
                fp.add(IMAPFolder.FetchProfileItem.INTERNALDATE);
                state.ifolder.fetch(add.toArray(new Message[0]), fp);
            }

            try {
                db.beginTransaction();

                for (int j = isub.length - 1; j >= 0 && found < pageSize && !state.destroyed; j--)
                    try {
                        long uid = state.ifolder.getUID(isub[j]);
                        Log.i("Boundary server sync uid=" + uid);
                        EntityMessage message = db.message().getMessageByUid(browsable.id, uid);
                        if (message == null) {
                            message = Core.synchronizeMessage(context,
                                    account, browsable,
                                    (IMAPStore) state.iservice.getStore(), state.ifolder, (MimeMessage) isub[j],
                                    true, true,
                                    rules, null);
                            found++;
                        }
                        if (message != null && query != null /* browsed */)
                            db.message().setMessageFound(message.id);
                    } catch (MessageRemovedException ex) {
                        Log.w(browsable.name + " boundary server", ex);
                    } catch (FolderClosedException ex) {
                        throw ex;
                    } catch (IOException ex) {
                        if (ex.getCause() instanceof MessagingException) {
                            Log.w(browsable.name + " boundary server", ex);
                            db.folder().setFolderError(browsable.id, Log.formatThrowable(ex));
                        } else
                            throw ex;
                    } catch (Throwable ex) {
                        Log.e(browsable.name + " boundary server", ex);
                        db.folder().setFolderError(browsable.id, Log.formatThrowable(ex));
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

    void close() {
        final State state = this.state;
        this.state = new State();
        state.destroyed = true;

        executor.submit(new Runnable() {
            @Override
            public void run() {
                Log.i("Boundary close");
                try {
                    if (state.ifolder != null)
                        state.ifolder.close();
                } catch (Throwable ex) {
                    Log.e("Boundary", ex);
                }
                try {
                    if (state.iservice != null)
                        state.iservice.close();
                } catch (Throwable ex) {
                    Log.e("Boundary", ex);
                }
            }
        });
    }

    private class State {
        boolean destroyed = false;
        boolean error = false;
        int index = 0;
        int offset = 0;
        List<Long> ids = null;
        List<TupleMatch> matches = null;

        EmailService iservice = null;
        IMAPFolder ifolder = null;
        Message[] imessages = null;
    }
}