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

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.PagedList;
import androidx.preference.PreferenceManager;

import com.sun.mail.gimap.GmailFolder;
import com.sun.mail.iap.Argument;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.IMAPResponse;
import com.sun.mail.imap.protocol.SearchSequence;
import com.sun.mail.util.MessageRemovedIOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;

import javax.mail.Address;
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
import javax.mail.search.NotTerm;
import javax.mail.search.OrTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.RecipientStringTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SizeTerm;
import javax.mail.search.SubjectTerm;

public class BoundaryCallbackMessages extends PagedList.BoundaryCallback<TupleMessageEx> {
    private Context context;
    private AdapterMessage.ViewType viewType;
    private Long account;
    private Long folder;
    private boolean server;
    private SearchCriteria criteria;
    private int pageSize;

    private IBoundaryCallbackMessages intf;

    private State state;
    private final ExecutorService executor = Helper.getBackgroundExecutor(1, "boundary");

    private static final int SEARCH_LIMIT_DEVICE = 1000;

    interface IBoundaryCallbackMessages {
        void onLoading();

        void onLoaded(int found);

        void onWarning(String message);

        void onException(@NonNull Throwable ex);
    }

    BoundaryCallbackMessages(
            Context context,
            AdapterMessage.ViewType viewType, long account, long folder,
            boolean server, SearchCriteria criteria,
            int pageSize) {
        this.context = context.getApplicationContext();
        this.viewType = viewType;
        this.account = (account < 0 ? null : account);
        this.folder = (folder < 0 ? null : folder);
        this.server = server;
        this.criteria = criteria;
        this.pageSize = pageSize;
    }

    State setCallback(IBoundaryCallbackMessages intf) {
        Log.i("Boundary callback=" + intf);
        if (Objects.equals(intf, this.intf))
            return this.state;

        this.intf = intf;
        this.state = new State();

        return this.state;
    }

    @Override
    public void onZeroItemsLoaded() {
        Log.i("Boundary zero loaded");
        queue_load(state);
    }

    @Override
    public void onItemAtEndLoaded(@NonNull final TupleMessageEx itemAtEnd) {
        Log.i("Boundary at end id=" + itemAtEnd.id);
        queue_load(state);
    }

    void retry() {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                close(state, true);
            }
        });
        queue_load(state);
    }

    private void queue_load(final State state) {
        if (state.queued > 1) {
            Log.i("Boundary queued =" + state.queued);
            return;
        }
        state.queued++;
        Log.i("Boundary queued +" + state.queued);

        executor.submit(new Runnable() {
            @Override
            public void run() {
                Helper.gc();

                int free = Log.getFreeMemMb();
                Map<String, String> crumb = new HashMap<>();
                crumb.put("queued", Integer.toString(state.queued));
                Log.breadcrumb("Boundary run", crumb);

                Log.i("Boundary run free=" + free);

                int found = 0;
                try {
                    if (state.destroyed || state.error) {
                        Log.i("Boundary was destroyed");
                        return;
                    }
                    if (!Objects.equals(state, BoundaryCallbackMessages.this.state)) {
                        Log.i("Boundary changed state");
                        return;
                    }

                    ApplicationEx.getMainHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            if (intf != null)
                                intf.onLoading();
                        }
                    });
                    if (server)
                        try {
                            found = load_server(state);
                        } catch (Throwable ex) {
                            if (state.error || ex instanceof IllegalArgumentException)
                                throw ex;

                            Log.w("Boundary", ex);
                            close(state, true);

                            // Retry
                            found = load_server(state);
                        }
                    else
                        found = load_device(state);
                } catch (final Throwable ex) {
                    state.error = true;
                    Log.e("Boundary", ex);
                    ApplicationEx.getMainHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            if (intf != null)
                                intf.onException(ex);
                        }
                    });
                } finally {
                    state.queued--;
                    Log.i("Boundary queued -" + state.queued);
                    Helper.gc();

                    crumb.put("queued", Integer.toString(state.queued));
                    Log.breadcrumb("Boundary done", crumb);

                    final int f = found;
                    ApplicationEx.getMainHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            if (intf != null)
                                intf.onLoaded(f);
                        }
                    });
                }
            }
        });
    }

    private int load_device(State state) {
        DB db = DB.getInstance(context);

        Log.i("Boundary device" +
                " index=" + state.index +
                " matches=" + (state.matches == null ? null : state.matches.size()));

        long[] exclude = new long[0];
        if (folder == null) {
            List<Long> folders = new ArrayList<>();
            if (!criteria.in_trash) {
                List<EntityFolder> trash = db.folder().getFoldersByType(EntityFolder.TRASH);
                if (trash != null)
                    for (EntityFolder folder : trash)
                        folders.add(folder.id);
            }
            if (!criteria.in_junk) {
                List<EntityFolder> junk = db.folder().getFoldersByType(EntityFolder.JUNK);
                if (junk != null)
                    for (EntityFolder folder : junk)
                        folders.add(folder.id);
            }
            exclude = Helper.toLongArray(folders);
        }

        int found = 0;

        List<String> word = new ArrayList<>();
        List<String> plus = new ArrayList<>();
        List<String> minus = new ArrayList<>();
        if (criteria.query != null) {
            for (String w : criteria.query.trim().split("\\s+"))
                if (w.length() > 1 && w.startsWith("+"))
                    plus.add(w.substring(1));
                else if (w.length() > 1 && w.startsWith("-"))
                    minus.add(w.substring(1));
                else
                    word.add(w);
            if (word.size() == 0 && plus.size() > 0)
                word.add(plus.get(0));
        }

        if (criteria.fts && word.size() > 0) {
            if (state.ids == null) {
                SQLiteDatabase sdb = Fts4DbHelper.getInstance(context);
                state.ids = Fts4DbHelper.match(sdb, account, folder, exclude, criteria, TextUtils.join(" ", word));
                EntityLog.log(context, "Boundary FTS " +
                        " account=" + account +
                        " folder=" + folder +
                        " criteria=" + criteria +
                        " ids=" + state.ids.size());
            }

            List<Long> excluded = Helper.fromLongArray(exclude);

            try {
                db.beginTransaction();

                for (; state.index < state.ids.size() && found < pageSize && !state.destroyed; state.index++) {
                    long id = state.ids.get(state.index);
                    EntityMessage message = db.message().getMessage(id);
                    if (message == null || message.ui_hide)
                        continue;

                    if (excluded.contains(message.folder))
                        continue;

                    if (!matchMessage(context, message, criteria))
                        continue;

                    found += db.message().setMessageFound(message.id, true);
                    Log.i("Boundary matched=" + message.id + " found=" + found);
                }
                db.setTransactionSuccessful();

            } finally {
                db.endTransaction();
            }

            return found;
        }

        while (found < pageSize && !state.destroyed) {
            if (state.matches == null ||
                    (state.matches.size() > 0 && state.index >= state.matches.size())) {
                String query = (word.size() == 0 ? null : '%' + TextUtils.join("%", word) + '%');
                state.matches = db.message().matchMessages(
                        account, folder, exclude,
                        query,
                        //criteria.in_senders,
                        //criteria.in_recipients,
                        //criteria.in_subject,
                        //criteria.in_keywords,
                        //criteria.in_message,
                        //criteria.in_notes,
                        //criteria.in_headers,
                        criteria.with_unseen,
                        criteria.with_flagged,
                        criteria.with_hidden,
                        criteria.with_encrypted,
                        criteria.with_attachments,
                        criteria.with_notes,
                        criteria.with_types == null ? 0 : criteria.with_types.length,
                        criteria.with_types == null ? new String[]{} : criteria.with_types,
                        criteria.with_size,
                        criteria.after,
                        criteria.before,
                        SEARCH_LIMIT_DEVICE, state.offset);
                EntityLog.log(context, "Boundary device" +
                        " account=" + account +
                        " folder=" + folder +
                        " criteria=" + criteria +
                        " query=" + query +
                        " offset=" + state.offset +
                        " size=" + state.matches.size());
                state.offset += Math.min(state.matches.size(), SEARCH_LIMIT_DEVICE);
                state.index = 0;
            }

            if (state.matches.size() == 0)
                break;

            for (int i = state.index; i < state.matches.size() && found < pageSize && !state.destroyed; i++) {
                state.index = i + 1;

                TupleMatch match = state.matches.get(i);
                boolean matched = (criteria.query == null || Boolean.TRUE.equals(match.matched));

                if (!matched) {
                    EntityMessage message = db.message().getMessage(match.id);
                    if (message != null && !message.ui_hide)
                        matched = matchMessage(context, message, criteria);
                }

                if (matched) {
                    found += db.message().setMessageFound(match.id, true);
                    Log.i("Boundary matched=" + match.id + " found=" + found);
                }
            }
        }

        Log.i("Boundary device done" +
                " found=" + found + "/" + pageSize +
                " destroyed=" + state.destroyed +
                " memory=" + Log.getFreeMemMb());
        return found;
    }

    private int load_server(final State state) throws MessagingException, ProtocolException, IOException {
        DB db = DB.getInstance(context);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean debug = prefs.getBoolean("debug", false);

        final EntityFolder browsable = db.folder().getBrowsableFolder(folder, criteria != null);
        if (browsable == null || !browsable.selectable || browsable.local) {
            Log.i("Boundary not browsable=" + (folder != null));
            return 0;
        }

        EntityAccount account = db.account().getAccount(browsable.account);
        if (account == null || account.protocol != EntityAccount.TYPE_IMAP)
            return 0;

        if (state.imessages == null)
            try {
                // Check connectivity
                if (!ConnectionHelper.getNetworkState(context).isSuitable())
                    throw new IllegalStateException(context.getString(R.string.title_no_internet));

                EntityLog.log(context, "Boundary server connecting account=" + account.name);
                state.iservice = new EmailService(
                        context, account.getProtocol(), account.realm, account.encryption, account.insecure, account.unicode,
                        EmailService.PURPOSE_SEARCH, debug || BuildConfig.DEBUG);
                state.iservice.setPartialFetch(account.partial_fetch);
                state.iservice.setIgnoreBodyStructureSize(account.ignore_size);
                state.iservice.connect(account);

                EntityLog.log(context, "Boundary server opening folder=" + browsable.name);
                state.ifolder = (IMAPFolder) state.iservice.getStore().getFolder(browsable.name);
                try {
                    state.ifolder.open(Folder.READ_WRITE);
                    browsable.read_only = state.ifolder.getUIDNotSticky();
                    db.folder().setFolderReadOnly(browsable.id, browsable.read_only);
                } catch (ReadOnlyFolderException ex) {
                    state.ifolder.open(Folder.READ_ONLY);
                    browsable.read_only = true;
                    db.folder().setFolderReadOnly(browsable.id, browsable.read_only);
                }

                db.folder().setFolderError(browsable.id, null);

                int count = MessageHelper.getMessageCount(state.ifolder);
                db.folder().setFolderTotal(browsable.id, count < 0 ? null : count, new Date().getTime());

                if (criteria == null) {
                    boolean filter_seen = prefs.getBoolean(FragmentMessages.getFilter(context, "seen", viewType, browsable.type), false);
                    boolean filter_unflagged = prefs.getBoolean(FragmentMessages.getFilter(context, "unflagged", viewType, browsable.type), false);
                    EntityLog.log(context, "Boundary filter seen=" + filter_seen + " unflagged=" + filter_unflagged);

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

                    EntityLog.log(context, "Boundary filter messages=" + state.imessages.length);
                } else
                    try {
                        Object result = state.ifolder.doCommand(new IMAPFolder.ProtocolCommand() {
                            @Override
                            public Object doCommand(IMAPProtocol protocol) throws ProtocolException {
                                try {
                                    // https://tools.ietf.org/html/rfc3501#section-6.4.4
                                    if (criteria.query != null &&
                                            criteria.query.startsWith("raw:") &&
                                            protocol.hasCapability("X-GM-EXT-1") &&
                                            EntityFolder.ARCHIVE.equals(browsable.type)) {
                                        // https://support.google.com/mail/answer/7190
                                        // https://developers.google.com/gmail/imap/imap-extensions#extension_of_the_search_command_x-gm-raw
                                        Log.i("Boundary raw search=" + criteria.query);

                                        Argument arg = new Argument();
                                        arg.writeAtom("X-GM-RAW");
                                        arg.writeString(criteria.query.substring(4));

                                        Response[] responses = protocol.command("SEARCH", arg);
                                        if (responses.length == 0)
                                            throw new ProtocolException("No response");
                                        if (!responses[responses.length - 1].isOK())
                                            throw new ProtocolException(
                                                    context.getString(R.string.title_service_auth, responses[responses.length - 1]));

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
                                        EntityLog.log(context, "Boundary server" +
                                                " account=" + account +
                                                " folder=" + folder +
                                                " search=" + criteria);

                                        try {
                                            return search(true, browsable.keywords, protocol, state);
                                        } catch (Throwable ex) {
                                            EntityLog.log(context, ex.toString());
                                            if (ex instanceof ProtocolException &&
                                                    ex.getMessage() != null &&
                                                    ex.getMessage().contains("full text search not supported")) {
                                                String msg = context.getString(R.string.title_service_auth,
                                                        account.host + ": " + getMessage(ex));
                                                ApplicationEx.getMainHandler().post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        if (intf != null)
                                                            intf.onWarning(msg);
                                                    }
                                                });
                                                criteria.in_message = false;
                                            }
                                        }

                                        return search(false, browsable.keywords, protocol, state);
                                    }
                                } catch (Throwable ex) {
                                    ProtocolException pex;
                                    if (ex instanceof ProtocolException)
                                        pex = new ProtocolException(
                                                context.getString(R.string.title_service_auth,
                                                        account.host + ": " + getMessage(ex)),
                                                ex.getCause());
                                    else
                                        pex = new ProtocolException("Search " + account.host, ex);
                                    Log.e(pex);
                                    throw pex;
                                }
                            }
                        });

                        state.imessages = (Message[]) result;
                    } catch (MessagingException ex) {
                        if (ex.getCause() instanceof ProtocolException)
                            throw (ProtocolException) ex.getCause();
                        else
                            throw ex;
                    }
                EntityLog.log(context, "Boundary found messages=" + state.imessages.length);

                FetchProfile fp = new FetchProfile();
                fp.add(UIDFolder.FetchProfileItem.UID);
                fp.add(IMAPFolder.FetchProfileItem.INTERNALDATE);
                state.ifolder.fetch(state.imessages, fp);
                Arrays.sort(state.imessages, new Comparator<Message>() {
                    @Override
                    public int compare(Message m1, Message m2) {
                        Date d1 = null;
                        try {
                            d1 = m1.getReceivedDate();
                        } catch (Throwable ex) {
                            Log.w(ex);
                        }

                        Date d2 = null;
                        try {
                            d2 = m2.getReceivedDate();
                        } catch (Throwable ex) {
                            Log.w(ex);
                        }

                        return Long.compare(d1 == null ? 0 : d1.getTime(), d2 == null ? 0 : d2.getTime());
                    }
                });

                EntityLog.log(context, "Boundary sorted messages=" + state.imessages.length);

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
            Arrays.fill(state.imessages, from, state.index + 1, null);
            state.index -= (pageSize - found);

            List<Message> add = new ArrayList<>();
            for (Message m : isub)
                try {
                    long uid = state.ifolder.getUID(m);
                    EntityMessage message = db.message().getMessageByUid(browsable.id, uid);
                    if (message == null)
                        add.add(m);
                } catch (FolderClosedException ex) {
                    throw ex;
                } catch (Throwable ex) {
                    Log.w(ex);
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
                //fp.add(IMAPFolder.FetchProfileItem.INTERNALDATE);
                if (account.isGmail()) {
                    fp.add(GmailFolder.FetchProfileItem.THRID);
                    fp.add(GmailFolder.FetchProfileItem.LABELS);
                }
                state.ifolder.fetch(add.toArray(new Message[0]), fp);
            }

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
                                rules, astate, null);
                        // SQLiteConstraintException
                        if (message != null && criteria == null)
                            found++; // browsed
                    }
                    if (message != null && criteria != null)
                        found += db.message().setMessageFound(message.id, true);
                    Log.i("Boundary matched=" + (message == null ? null : message.id) + " found=" + found);
                } catch (MessageRemovedException | MessageRemovedIOException ex) {
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
                    isub[j] = null;
                }
        }

        if (state.index < 0) {
            Log.i("Boundary server end");
            close(state, false);
        }

        Log.i("Boundary server done memory=" + Log.getFreeMemMb());
        return found;
    }

    private String getMessage(Throwable ex) {
        if (ex instanceof ProtocolException) {
            Response r = ((ProtocolException) ex).getResponse();
            if (r != null && !TextUtils.isEmpty(r.getRest()))
                return r.getRest();
        }

        return ex.toString();
    }

    private Message[] search(boolean utf8, String[] keywords, IMAPProtocol protocol, State state) throws IOException, MessagingException, ProtocolException {
        EntityLog.log(context, "Search utf8=" + utf8);

        SearchTerm terms = criteria.getTerms(utf8, state.ifolder.getPermanentFlags(), keywords);
        if (terms == null)
            return state.ifolder.getMessages();

        SearchSequence ss = new SearchSequence(protocol);
        Argument args = ss.generateSequence(terms, utf8 ? StandardCharsets.UTF_8.name() : null);
        args.writeAtom("ALL");

        Response[] responses = protocol.command("SEARCH", args); // no CHARSET !
        if (responses == null || responses.length == 0)
            throw new ProtocolException("No response from server");
        for (Response response : responses)
            Log.i("Search response=" + response);
        if (!responses[responses.length - 1].isOK())
            throw new ProtocolException(responses[responses.length - 1]);

        List<Integer> msgnums = new ArrayList<>();
        for (int r = 0; r < responses.length; r++) {
            IMAPResponse response = (IMAPResponse) responses[r];
            if (response.keyEquals("SEARCH")) {
                int msgnum;
                while ((msgnum = response.readNumber()) != -1)
                    msgnums.add(msgnum);
            }
        }

        EntityLog.log(context, "Search messages=" + msgnums.size());
        Message[] imessages = new Message[msgnums.size()];
        for (int i = 0; i < msgnums.size(); i++)
            imessages[i] = state.ifolder.getMessage(msgnums.get(i));

        return imessages;
    }

    private static boolean matchMessage(Context context, EntityMessage message, SearchCriteria criteria) {
        if (criteria.with_unseen) {
            if (message.ui_seen)
                return false;
        }

        if (criteria.with_flagged) {
            if (!message.ui_flagged)
                return false;
        }

        if (criteria.with_hidden) {
            if (message.ui_snoozed == null)
                return false;
        }

        if (criteria.with_encrypted) {
            if (message.encrypt == null ||
                    EntityMessage.ENCRYPT_NONE.equals(message.encrypt))
                return false;
        }

        if (criteria.with_attachments) {
            if (message.attachments == 0)
                return false;
        }

        //
        if (criteria.with_notes) {
            if (message.notes == null)
                return false;
        }

        //
        if (criteria.with_size != null) {
            if (message.total == null || message.total < criteria.with_size)
                return false;
        }

        //
        if (criteria.before != null) {
            if (message.received > criteria.before)
                return false;
        }

        //
        if (criteria.after != null) {
            if (message.received < criteria.after)
                return false;
        }

        if (criteria.in_senders) {
            if (contains(message.from, criteria.query))
                return true;
        }

        if (criteria.in_recipients) {
            if (contains(message.to, criteria.query) ||
                    contains(message.cc, criteria.query) ||
                    contains(message.bcc, criteria.query))
                return true;
        }

        if (criteria.in_subject) {
            if (contains(message.subject, criteria.query, false))
                return true;
        }

        if (criteria.in_keywords) {
            if (message.keywords != null)
                for (String keyword : message.keywords)
                    if (contains(keyword, criteria.query, false))
                        return true;
        }

        if (criteria.in_notes) {
            if (contains(message.notes, criteria.query, false))
                return true;
        }

        if (criteria.in_headers) {
            if (contains(message.headers, criteria.query, false))
                return true;
        }

        if (criteria.in_message)
            try {
                File file = EntityMessage.getFile(context, message.id);
                if (file.exists()) {
                    String html = Helper.readText(file);
                    if (contains(html, criteria.query, true)) {
                        String text = HtmlHelper.getFullText(html);
                        if (contains(text, criteria.query, false))
                            return true;
                    }
                }
            } catch (IOException ex) {
                Log.e(ex);
            }

        return false;
    }

    private static boolean contains(Address[] addresses, String query) {
        if (addresses == null)
            return false;
        for (Address address : addresses)
            if (contains(address.toString(), query, false))
                return true;
        return false;
    }

    private static boolean contains(String text, String query, boolean html) {
        if (TextUtils.isEmpty(text))
            return false;

        text = Fts4DbHelper.breakText(text);

        List<String> word = new ArrayList<>();
        for (String w : query.trim().split("\\s+"))
            if (w.length() > 1 && w.startsWith("+")) {
                if (!text.contains(Fts4DbHelper.preprocessText(w.substring(1))))
                    return false;
            } else if (w.length() > 1 && w.startsWith("-")) {
                if (!html && text.contains(Fts4DbHelper.preprocessText(w.substring(1))))
                    return false;
            } else
                word.addAll(Arrays.asList(Fts4DbHelper.breakText(w).split("\\s+")));

        if (word.size() == 0)
            return true;

        Pattern pat = Pattern.compile(".*?\\b(" + TextUtils.join("\\s+", word) + ")\\b.*?", Pattern.DOTALL);
        return pat.matcher(text).matches();
    }

    State getState() {
        return this.state;
    }

    void destroy(State state) {
        state.destroyed = true;
        this.intf = null;
        Log.i("Boundary destroy");

        executor.submit(new Runnable() {
            @Override
            public void run() {
                close(state, true);
            }
        });
    }

    private void close(State state, boolean reset) {
        Log.i("Boundary close");

        try {
            if (state.ifolder != null && state.ifolder.isOpen())
                state.ifolder.close(false);
        } catch (Throwable ex) {
            Log.e("Boundary", ex);
        }

        try {
            if (state.iservice != null && state.iservice.isOpen())
                state.iservice.close();
        } catch (Throwable ex) {
            Log.e("Boundary", ex);
        }

        if (reset)
            state.reset();
    }

    static class State {
        int queued = 0;
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
            queued = 0;
            destroyed = false;
            error = false;
            index = 0;
            offset = 0;
            ids = null;
            matches = null;
            iservice = null;
            ifolder = null;
            imessages = null;

            Helper.gc();
        }
    }

    static class SearchCriteria extends EntitySearch implements Serializable {
        String query;
        boolean fts = false;
        boolean in_senders = true;
        boolean in_recipients = true;
        boolean in_subject = true;
        boolean in_keywords = true;
        boolean in_message = true;
        boolean in_notes = true;
        boolean in_headers = false;
        boolean in_html = false;
        boolean with_unseen;
        boolean with_flagged;
        boolean with_hidden;
        boolean with_encrypted;
        boolean with_attachments;
        boolean with_notes;
        String[] with_types;
        Integer with_size = null;
        boolean in_trash = true;
        boolean in_junk = true;
        Long after = null;
        Long before = null;

        private static final String FROM = "from:";
        private static final String TO = "to:";
        private static final String CC = "cc:";
        private static final String BCC = "bcc:";
        private static final String KEYWORD = "keyword:";

        boolean onServer() {
            if (query == null)
                return false;

            for (String w : query.trim().split("\\s+"))
                if (w.length() > 1 && w.startsWith("?"))
                    return true;
                else if (w.length() > FROM.length() && w.startsWith(FROM))
                    return true;
                else if (w.length() > TO.length() && w.startsWith(TO))
                    return true;
                else if (w.length() > CC.length() && w.startsWith(CC))
                    return true;
                else if (w.length() > BCC.length() && w.startsWith(BCC))
                    return true;
                else if (w.length() > KEYWORD.length() && w.startsWith(KEYWORD))
                    return true;

            return false;
        }

        SearchTerm getTerms(boolean utf8, Flags flags, String[] keywords) {
            List<SearchTerm> or = new ArrayList<>();
            List<SearchTerm> and = new ArrayList<>();
            if (query != null) {
                String search = query;

                if (!utf8) {
                    search = search
                            .replace("ß", "ss") // Eszett
                            .replace("ĳ", "ij")
                            .replace("ø", "o");
                    search = Normalizer
                            .normalize(search, Normalizer.Form.NFKD)
                            .replaceAll("[^\\p{ASCII}]", "");
                }

                List<String> word = new ArrayList<>();
                List<String> plus = new ArrayList<>();
                List<String> minus = new ArrayList<>();
                List<String> opt = new ArrayList<>();
                List<String> andFrom = new ArrayList<>();
                List<String> andTo = new ArrayList<>();
                List<String> andCc = new ArrayList<>();
                List<String> andBcc = new ArrayList<>();
                List<String> andKeyword = new ArrayList<>();
                StringBuilder all = new StringBuilder();
                for (String w : search.trim().split("\\s+")) {
                    if (all.length() > 0)
                        all.append(' ');

                    if (w.length() > 1 && w.startsWith("+")) {
                        plus.add(w.substring(1));
                        all.append(w.substring(1));
                    } else if (w.length() > 1 && w.startsWith("-")) {
                        minus.add(w.substring(1));
                        all.append(w.substring(1));
                    } else if (w.length() > 1 && w.startsWith("?")) {
                        opt.add(w.substring(1));
                        all.append(w.substring(1));
                    } else if (w.length() > FROM.length() && w.startsWith(FROM))
                        andFrom.add(w.substring(FROM.length()));
                    else if (w.length() > TO.length() && w.startsWith(TO))
                        andTo.add(w.substring(TO.length()));
                    else if (w.length() > CC.length() && w.startsWith(CC))
                        andCc.add(w.substring(CC.length()));
                    else if (w.length() > BCC.length() && w.startsWith(BCC))
                        andBcc.add(w.substring(BCC.length()));
                    else if (w.length() > KEYWORD.length() && w.startsWith(KEYWORD))
                        andKeyword.add(w.substring(KEYWORD.length()));
                    else {
                        word.add(w);
                        all.append(w);
                    }
                }

                if (plus.size() + minus.size() + opt.size() +
                        andFrom.size() + andTo.size() + andCc.size() + andBcc.size() + andKeyword.size() > 0)
                    search = all.toString();

                // Yahoo! does not support keyword search, but uses the flags $Forwarded $Junk $NotJunk
                boolean hasKeywords = false;
                for (String keyword : keywords)
                    if (!keyword.startsWith("$")) {
                        hasKeywords = true;
                        break;
                    }

                if (andFrom.size() > 0) {
                    for (String term : andFrom)
                        and.add(new FromStringTerm(term));
                } else {
                    if (in_senders && !TextUtils.isEmpty(search) &&
                            plus.size() + minus.size() + opt.size() == 0)
                        or.add(new FromStringTerm(search));
                }

                if (andTo.size() + andCc.size() + andBcc.size() > 0) {
                    for (String term : andTo)
                        and.add(new RecipientStringTerm(Message.RecipientType.TO, term));
                    for (String term : andCc)
                        and.add(new RecipientStringTerm(Message.RecipientType.CC, term));
                    for (String term : andBcc)
                        and.add(new RecipientStringTerm(Message.RecipientType.BCC, term));
                } else {
                    if (in_recipients && !TextUtils.isEmpty(search) &&
                            plus.size() + minus.size() + opt.size() == 0) {
                        or.add(new RecipientStringTerm(Message.RecipientType.TO, search));
                        or.add(new RecipientStringTerm(Message.RecipientType.CC, search));
                        or.add(new RecipientStringTerm(Message.RecipientType.BCC, search));
                    }
                }

                if (in_subject && !TextUtils.isEmpty(search))
                    if (plus.size() + minus.size() + opt.size() == 0)
                        or.add(new SubjectTerm(search));
                    else
                        try {
                            or.add(construct(word, plus, minus, opt, SubjectTerm.class));
                        } catch (Throwable ex) {
                            Log.e(ex);
                            or.add(new SubjectTerm(search));
                        }

                if (hasKeywords)
                    if (andKeyword.size() > 0) {
                        for (String term : andKeyword)
                            and.add(new FlagTerm(new Flags(term), true));
                    } else {
                        if (in_keywords && !TextUtils.isEmpty(search) &&
                                plus.size() + minus.size() + opt.size() == 0) {
                            String keyword = MessageHelper.sanitizeKeyword(search);
                            if (TextUtils.isEmpty(keyword))
                                Log.w("Keyword empty=" + search);
                            else
                                or.add(new FlagTerm(new Flags(keyword), true));
                        }
                    }

                if (in_message && !TextUtils.isEmpty(search))
                    if (plus.size() + minus.size() + opt.size() == 0)
                        or.add(new BodyTerm(search));
                    else
                        try {
                            or.add(construct(word, plus, minus, opt, BodyTerm.class));
                        } catch (Throwable ex) {
                            Log.e(ex);
                            or.add(new BodyTerm(search));
                        }
            }

            if (with_unseen && flags.contains(Flags.Flag.SEEN))
                and.add(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
            if (with_flagged && flags.contains(Flags.Flag.FLAGGED))
                and.add(new FlagTerm(new Flags(Flags.Flag.FLAGGED), true));

            if (with_size != null)
                and.add(new SizeTerm(ComparisonTerm.GT, with_size));

            if (after != null)
                and.add(new ReceivedDateTerm(ComparisonTerm.GE, new Date(after)));
            if (before != null)
                and.add(new ReceivedDateTerm(ComparisonTerm.LE, new Date(before)));

            SearchTerm term = null;

            if (or.size() > 0)
                term = new OrTerm(or.toArray(new SearchTerm[0]));

            if (and.size() > 0)
                if (term == null)
                    term = new AndTerm(and.toArray(new SearchTerm[0]));
                else
                    term = new AndTerm(term, new AndTerm(and.toArray(new SearchTerm[0])));

            return term;
        }

        private SearchTerm construct(
                List<String> word,
                List<String> plus,
                List<String> minus,
                List<String> opt,
                Class<?> clazz) throws ReflectiveOperationException {
            SearchTerm term = null;
            Constructor<?> ctor = clazz.getConstructor(String.class);

            if (word.size() > 0)
                term = (SearchTerm) ctor.newInstance(TextUtils.join(" ", word));

            for (String p : plus)
                if (term == null)
                    term = (SearchTerm) ctor.newInstance(p);
                else
                    term = new AndTerm(term, (SearchTerm) ctor.newInstance(p));

            for (String m : minus)
                if (term == null)
                    term = new NotTerm((SearchTerm) ctor.newInstance(m));
                else
                    term = new AndTerm(term, new NotTerm((SearchTerm) ctor.newInstance(m)));

            for (String o : opt)
                if (term == null)
                    term = (SearchTerm) ctor.newInstance(o);
                else
                    term = new OrTerm(term, (SearchTerm) ctor.newInstance(o));

            return term;
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
            if (with_notes)
                flags.add(context.getString(R.string.title_search_flag_notes));
            if (with_types != null)
                if (with_types.length == 1 && "text/calendar".equals(with_types[0]))
                    flags.add(context.getString(R.string.title_search_flag_invite));
                else
                    flags.add(TextUtils.join(", ", with_types));
            if (with_size != null)
                flags.add(context.getString(R.string.title_search_flag_size,
                        Helper.humanReadableByteCount(with_size)));
            return (query == null ? "" : query + " ")
                    + (flags.size() > 0 ? "+" : "")
                    + TextUtils.join(",", flags);
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj instanceof SearchCriteria) {
                SearchCriteria other = (SearchCriteria) obj;
                return (Objects.equals(this.query, other.query) &&
                        this.in_senders == other.in_senders &&
                        this.in_recipients == other.in_recipients &&
                        this.in_subject == other.in_subject &&
                        this.in_keywords == other.in_keywords &&
                        this.in_message == other.in_message &&
                        this.in_notes == other.in_notes &&
                        this.in_headers == other.in_headers &&
                        this.in_html == other.in_html &&
                        this.with_unseen == other.with_unseen &&
                        this.with_flagged == other.with_flagged &&
                        this.with_hidden == other.with_hidden &&
                        this.with_encrypted == other.with_encrypted &&
                        this.with_attachments == other.with_attachments &&
                        this.with_notes == other.with_notes &&
                        Arrays.equals(this.with_types, other.with_types) &&
                        Objects.equals(this.with_size, other.with_size) &&
                        this.in_trash == other.in_trash &&
                        this.in_junk == other.in_junk &&
                        Objects.equals(this.after, other.after) &&
                        Objects.equals(this.before, other.before));
            } else
                return false;
        }

        JSONObject toJson() throws JSONException {
            JSONObject json = new JSONObject();
            json.put("query", query);
            json.put("in_senders", in_senders);
            json.put("in_recipients", in_recipients);
            json.put("in_subject", in_subject);
            json.put("in_keywords", in_keywords);
            json.put("in_message", in_message);
            json.put("in_notes", in_notes);
            json.put("in_headers", in_headers);
            json.put("in_html", in_html);
            json.put("with_unseen", with_unseen);
            json.put("with_flagged", with_flagged);
            json.put("with_hidden", with_hidden);
            json.put("with_encrypted", with_encrypted);
            json.put("with_attachments", with_attachments);
            json.put("with_notes", with_notes);

            if (with_types != null) {
                JSONArray jtypes = new JSONArray();
                for (String type : with_types)
                    jtypes.put(type);
                json.put("with_types", jtypes);
            }

            if (with_size != null)
                json.put("with_size", with_size);

            json.put("in_trash", in_trash);
            json.put("in_junk", in_junk);

            Calendar now = Calendar.getInstance();
            now.set(Calendar.MILLISECOND, 0);
            now.set(Calendar.SECOND, 0);
            now.set(Calendar.MINUTE, 0);
            now.set(Calendar.HOUR, 0);

            if (after != null)
                json.put("after", after - now.getTimeInMillis());

            if (before != null)
                json.put("before", before - now.getTimeInMillis());

            return json;
        }

        public static SearchCriteria fromJSON(JSONObject json) throws JSONException {
            SearchCriteria criteria = new SearchCriteria();
            criteria.query = json.optString("query");
            criteria.in_senders = json.optBoolean("in_senders");
            criteria.in_recipients = json.optBoolean("in_recipients");
            criteria.in_subject = json.optBoolean("in_subject");
            criteria.in_keywords = json.optBoolean("in_keywords");
            criteria.in_message = json.optBoolean("in_message");
            criteria.in_notes = json.optBoolean("in_notes");
            criteria.in_headers = json.optBoolean("in_headers");
            criteria.in_html = json.optBoolean("in_html");
            criteria.with_unseen = json.optBoolean("with_unseen");
            criteria.with_flagged = json.optBoolean("with_flagged");
            criteria.with_hidden = json.optBoolean("with_hidden");
            criteria.with_encrypted = json.optBoolean("with_encrypted");
            criteria.with_attachments = json.optBoolean("with_attachments");
            criteria.with_notes = json.optBoolean("with_notes");

            if (json.has("with_types")) {
                JSONArray jtypes = json.getJSONArray("with_types");
                criteria.with_types = new String[jtypes.length()];
                for (int i = 0; i < jtypes.length(); i++)
                    criteria.with_types[i] = jtypes.getString(i);
            }

            if (json.has("with_size"))
                criteria.with_size = json.getInt("with_size");

            criteria.in_trash = json.optBoolean("in_trash");
            criteria.in_junk = json.optBoolean("in_junk");

            Calendar now = Calendar.getInstance();
            now.set(Calendar.MILLISECOND, 0);
            now.set(Calendar.SECOND, 0);
            now.set(Calendar.MINUTE, 0);
            now.set(Calendar.HOUR, 0);

            if (json.has("after"))
                criteria.after = json.getLong("after") + now.getTimeInMillis();

            if (json.has("before"))
                criteria.before = json.getLong("before") + now.getTimeInMillis();

            return criteria;
        }

        @NonNull
        @Override
        public String toString() {
            return query +
                    " fts=" + fts +
                    " senders=" + in_senders +
                    " recipients=" + in_recipients +
                    " subject=" + in_subject +
                    " keywords=" + in_keywords +
                    " message=" + in_message +
                    " notes=" + in_notes +
                    " headers=" + in_headers +
                    " html=" + in_html +
                    " unseen=" + with_unseen +
                    " flagged=" + with_flagged +
                    " hidden=" + with_hidden +
                    " encrypted=" + with_encrypted +
                    " w/attachments=" + with_attachments +
                    " w/notes=" + with_notes +
                    " type=" + (with_types == null ? null : TextUtils.join(",", with_types)) +
                    " size=" + with_size +
                    " trash=" + in_trash +
                    " junk=" + in_junk +
                    " after=" + (after == null ? "" : new Date(after)) +
                    " before=" + (before == null ? "" : new Date(before));
        }
    }
}