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
import androidx.annotation.Nullable;
import androidx.paging.PagedList;
import androidx.preference.PreferenceManager;

import com.sun.mail.iap.Argument;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.IMAPResponse;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
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
import javax.mail.search.ComparisonTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.FromStringTerm;
import javax.mail.search.OrTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.RecipientStringTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;

import io.requery.android.database.sqlite.SQLiteDatabase;

public class BoundaryCallbackMessages extends PagedList.BoundaryCallback<TupleMessageEx> {
    private Context context;
    private Long account;
    private Long folder;
    private boolean server;
    private SearchCriteria criteria;
    private int pageSize;

    private IBoundaryCallbackMessages intf;

    private Handler handler;

    private State state;

    private static final int SEARCH_LIMIT = 1000;
    private static ExecutorService executor = Helper.getBackgroundExecutor(1, "boundary");

    interface IBoundaryCallbackMessages {
        void onLoading();

        void onLoaded();

        void onException(@NonNull Throwable ex);
    }

    BoundaryCallbackMessages(Context context, long account, long folder, boolean server, SearchCriteria criteria, int pageSize) {
        this.context = context.getApplicationContext();
        this.account = (account < 0 ? null : account);
        this.folder = (folder < 0 ? null : folder);
        this.server = server;
        this.criteria = criteria;
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
        queue_load(state);
    }

    @Override
    public void onItemAtEndLoaded(@NonNull final TupleMessageEx itemAtEnd) {
        Log.i("Boundary at end");
        queue_load(state);
    }

    void retry() {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                close(state);
            }
        });
        queue_load(state);
    }

    private void queue_load(final State state) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    if (state.destroyed || state.error)
                        return;

                    if (intf != null)
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                intf.onLoading();
                            }
                        });
                    if (server)
                        try {
                            load_server(state);
                        } catch (Throwable ex) {
                            if (state.error || ex instanceof IllegalArgumentException)
                                throw ex;

                            Log.w("Boundary", ex);
                            close(state);

                            // Retry
                            load_server(state);
                        }
                    else
                        load_device(state);
                } catch (final Throwable ex) {
                    state.error = true;
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
                                intf.onLoaded();
                            }
                        });
                }
            }
        });
    }

    private int load_device(State state) {
        DB db = DB.getInstance(context);

        int found = 0;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean fts = prefs.getBoolean("fts", false);
        boolean pro = ActivityBilling.isPro(context);
        if (fts && pro && criteria.isQueryOnly()) {
            if (state.ids == null) {
                SQLiteDatabase sdb = FtsDbHelper.getInstance(context);
                state.ids = FtsDbHelper.match(sdb, account, folder, criteria);
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
                            account, folder,
                            criteria.query == null ? null : "%" + criteria.query + "%",
                            criteria.with_unseen,
                            criteria.with_flagged,
                            criteria.with_hidden,
                            criteria.with_encrypted,
                            criteria.with_attachments,
                            criteria.after,
                            criteria.before,
                            SEARCH_LIMIT, state.offset);
                    Log.i("Boundary device folder=" + folder +
                            " criteria=" + criteria +
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
                    if (criteria.query != null && (match.matched == null || !match.matched))
                        try {
                            File file = EntityMessage.getFile(context, match.id);
                            if (file.exists()) {
                                String html = Helper.readText(file);
                                if (html.toLowerCase().contains(criteria.query)) {
                                    String text = HtmlHelper.getFullText(html);
                                    if (text.toLowerCase().contains(criteria.query))
                                        match.matched = true;
                                }
                            }
                        } catch (IOException ex) {
                            Log.e(ex);
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
        final boolean debug = (prefs.getBoolean("debug", false) || BuildConfig.BETA_RELEASE);

        final EntityFolder browsable = db.folder().getBrowsableFolder(folder, criteria != null);
        if (browsable == null || !browsable.selectable) {
            Log.w("Boundary not browsable=" + (folder != null));
            return 0;
        }

        EntityAccount account = db.account().getAccount(browsable.account);
        if (account == null)
            return 0;

        if (state.imessages == null)
            try {
                // Check connectivity
                if (!ConnectionHelper.getNetworkState(context).isSuitable())
                    throw new IllegalStateException(context.getString(R.string.title_no_internet));

                Log.i("Boundary server connecting account=" + account.name);
                state.iservice = new EmailService(
                        context, account.getProtocol(), account.realm, account.insecure, EmailService.PURPOSE_SEARCH, debug);
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

                db.folder().setFolderError(browsable.id, null);

                int count = state.ifolder.getMessageCount();
                db.folder().setFolderTotal(browsable.id, count < 0 ? null : count);

                if (criteria == null) {
                    boolean filter_seen = prefs.getBoolean("filter_seen", false);
                    boolean filter_unflagged = prefs.getBoolean("filter_unflagged", false);
                    Log.i("Boundary filter seen=" + filter_seen + " unflagged=" + filter_unflagged);

                    List<SearchTerm> and = new ArrayList<>();

                    if (filter_seen &&
                            state.ifolder.getPermanentFlags().contains(Flags.Flag.SEEN))
                        and.add(new FlagTerm(new Flags(Flags.Flag.SEEN), false));

                    if (filter_unflagged &&
                            state.ifolder.getPermanentFlags().contains(Flags.Flag.FLAGGED))
                        and.add(new FlagTerm(new Flags(Flags.Flag.FLAGGED), true));

                    if (and.size() == 0)
                        state.imessages = state.ifolder.getMessages();
                    else
                        state.imessages = state.ifolder.search(new AndTerm(and.toArray(new SearchTerm[0])));
                } else {
                    Object result = state.ifolder.doCommand(new IMAPFolder.ProtocolCommand() {
                        @Override
                        public Object doCommand(IMAPProtocol protocol) throws ProtocolException {
                            try {
                                // https://tools.ietf.org/html/rfc3501#section-6.4.4
                                Argument arg = new Argument();
                                if (criteria.query != null &&
                                        criteria.query.startsWith("raw:") &&
                                        state.iservice.hasCapability("X-GM-EXT-1")) {
                                    // https://support.google.com/mail/answer/7190
                                    // https://developers.google.com/gmail/imap/imap-extensions#extension_of_the_search_command_x-gm-raw
                                    arg.writeAtom("X-GM-RAW");
                                    arg.writeString(criteria.query.substring(4));

                                    Response[] responses = protocol.command("SEARCH", arg);
                                    if (responses.length == 0)
                                        throw new ProtocolException("No response");
                                    if (!responses[responses.length - 1].isOK())
                                        throw new ProtocolException(responses[responses.length - 1]);

                                    Log.i("Boundary raw search=" + criteria.query);

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
                                    Log.i("Boundary server search=" + criteria);

                                    List<SearchTerm> or = new ArrayList<>();
                                    List<SearchTerm> and = new ArrayList<>();
                                    if (criteria.query != null) {
                                        String search = criteria.query;

                                        if (!protocol.supportsUtf8()) {
                                            search = search.replace("ß", "ss"); // Eszett
                                            search = Normalizer.normalize(search, Normalizer.Form.NFKD)
                                                    .replaceAll("[^\\p{ASCII}]", "");
                                        }

                                        // Yahoo! does not support keyword search, but uses the flags $Forwarded $Junk $NotJunk
                                        boolean keywords = false;
                                        for (String keyword : browsable.keywords)
                                            if (!keyword.startsWith("$")) {
                                                keywords = true;
                                                break;
                                            }

                                        if (criteria.in_senders)
                                            or.add(new FromStringTerm(search));
                                        if (criteria.in_receipients) {
                                            or.add(new RecipientStringTerm(Message.RecipientType.TO, search));
                                            or.add(new RecipientStringTerm(Message.RecipientType.CC, search));
                                            or.add(new RecipientStringTerm(Message.RecipientType.BCC, search));
                                        }
                                        if (criteria.in_subject)
                                            or.add(new SubjectTerm(search));
                                        if (criteria.in_keywords && keywords)
                                            or.add(new FlagTerm(new Flags(MessageHelper.sanitizeKeyword(search)), true));
                                        if (criteria.in_message)
                                            or.add(new BodyTerm(search));
                                    }

                                    if (criteria.with_unseen &&
                                            state.ifolder.getPermanentFlags().contains(Flags.Flag.SEEN))
                                        and.add(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
                                    if (criteria.with_flagged &&
                                            state.ifolder.getPermanentFlags().contains(Flags.Flag.FLAGGED))
                                        and.add(new FlagTerm(new Flags(Flags.Flag.FLAGGED), true));

                                    if (criteria.after != null)
                                        and.add(new ReceivedDateTerm(ComparisonTerm.GT, new Date(criteria.after)));
                                    if (criteria.before != null)
                                        and.add(new ReceivedDateTerm(ComparisonTerm.LT, new Date(criteria.before)));

                                    SearchTerm term = null;

                                    if (or.size() > 0)
                                        term = new OrTerm(or.toArray(new SearchTerm[0]));

                                    if (and.size() > 0)
                                        if (term == null)
                                            term = new AndTerm(and.toArray(new SearchTerm[0]));
                                        else
                                            term = new AndTerm(term, new AndTerm(and.toArray(new SearchTerm[0])));

                                    if (term == null)
                                        return new Message[0];

                                    return state.ifolder.search(term);
                                }
                            } catch (MessagingException ex) {
                                ProtocolException pex = new ProtocolException(
                                        "Search " + account.host + " " + criteria, ex);
                                Log.e(pex);
                                throw pex;
                            }
                        }
                    });

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

                Core.State astate = new Core.State(ConnectionHelper.getNetworkState(context));
                for (int j = isub.length - 1; j >= 0 && found < pageSize && !state.destroyed && astate.isRecoverable(); j--)
                    try {
                        long uid = state.ifolder.getUID(isub[j]);
                        Log.i("Boundary server sync uid=" + uid);
                        EntityMessage message = db.message().getMessageByUid(browsable.id, uid);
                        if (message == null) {
                            message = Core.synchronizeMessage(context,
                                    account, browsable,
                                    (IMAPStore) state.iservice.getStore(), state.ifolder, (MimeMessage) isub[j],
                                    true, true,
                                    rules, astate);
                            found++;
                        }
                        if (message != null && criteria != null /* browsed */)
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

    void destroy() {
        final State old = this.state;
        old.destroyed = true;

        this.state = new State();

        executor.submit(new Runnable() {
            @Override
            public void run() {
                close(old);
            }
        });
    }

    private void close(State state) {
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

        state.reset();
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

        void reset() {
            Log.i("Boundary reset");
            destroyed = false;
            error = false;
            index = 0;
            offset = 0;
            ids = null;
            matches = null;
            iservice = null;
            ifolder = null;
            imessages = null;
        }
    }

    static class SearchCriteria implements Serializable {
        String query;
        boolean in_senders = true;
        boolean in_receipients = true;
        boolean in_subject = true;
        boolean in_keywords = true;
        boolean in_message = true;
        boolean with_unseen;
        boolean with_flagged;
        boolean with_hidden;
        boolean with_encrypted;
        boolean with_attachments;
        Long after = null;
        Long before = null;

        SearchCriteria() {
        }

        SearchCriteria(String query) {
            this.query = query;
        }

        boolean isQueryOnly() {
            return (!TextUtils.isEmpty(query) &&
                    in_senders &&
                    in_receipients &&
                    in_subject &&
                    in_keywords &&
                    in_message &&
                    isWithout());
        }

        boolean isWithout() {
            return !(with_unseen ||
                    with_flagged ||
                    with_hidden ||
                    with_encrypted ||
                    with_attachments);
        }

        String getTitle(Context context) {
            List<String> flags = new ArrayList<>();
            if (with_unseen)
                flags.add(context.getString(R.string.title_search_flag_unseen));
            if (with_flagged)
                flags.add(context.getString(R.string.title_search_flag_flagged));
            if (with_hidden)
                flags.add(context.getString(R.string.title_search_flag_hidden));
            if (with_encrypted)
                flags.add(context.getString(R.string.title_search_flag_encrypted));
            if (with_attachments)
                flags.add(context.getString(R.string.title_search_flag_attachments));
            return (query == null ? "" : query)
                    + (flags.size() > 0 ? " +" : "")
                    + TextUtils.join(",", flags);
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj instanceof SearchCriteria) {
                SearchCriteria other = (SearchCriteria) obj;
                return (Objects.equals(this.query, other.query) &&
                        this.in_senders == other.in_senders &&
                        this.in_receipients == other.in_receipients &&
                        this.in_subject == other.in_subject &&
                        this.in_keywords == other.in_keywords &&
                        this.in_message == other.in_message &&
                        this.with_unseen == other.with_unseen &&
                        this.with_flagged == other.with_flagged &&
                        this.with_hidden == other.with_hidden &&
                        this.with_encrypted == other.with_encrypted &&
                        this.with_attachments == other.with_attachments &&
                        Objects.equals(this.after, other.after) &&
                        Objects.equals(this.before, other.before));
            } else
                return false;
        }

        @NonNull
        @Override
        public String toString() {
            return query +
                    " senders=" + in_senders +
                    " receipients=" + in_receipients +
                    " subject=" + in_subject +
                    " keywords=" + in_keywords +
                    " message=" + in_message +
                    " unseen=" + with_unseen +
                    " flagged=" + with_flagged +
                    " hidden=" + with_hidden +
                    " encrypted=" + with_encrypted +
                    " attachments=" + with_attachments;
        }
    }
}