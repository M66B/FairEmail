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
import android.os.Handler;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.paging.PagedList;
import androidx.preference.PreferenceManager;

import com.sun.mail.iap.Argument;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.IMAPResponse;

import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.Address;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.Message;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.UIDFolder;
import javax.mail.internet.InternetAddress;
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

public class BoundaryCallbackMessages extends PagedList.BoundaryCallback<TupleMessageEx> {
    private Context context;
    private Long folder;
    private boolean server;
    private String query;
    private int pageSize;

    private IBoundaryCallbackMessages intf;

    private Handler handler;
    private ExecutorService executor = Executors.newSingleThreadExecutor(Helper.backgroundThreadFactory);

    private State state;

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

        if (state.messages == null) {
            state.messages = db.message().getMessageIdsByFolder(folder);
            Log.i("Boundary device folder=" + folder + " query=" + query + " messages=" + state.messages.size());
        }

        int found = 0;
        try {
            db.beginTransaction();

            String find = (TextUtils.isEmpty(query) ? null : query.toLowerCase(Locale.ROOT));
            for (int i = state.index; i < state.messages.size() && found < pageSize && !state.destroyed; i++) {
                state.index = i + 1;

                EntityMessage message = db.message().getMessage(state.messages.get(i));
                if (message == null)
                    continue;

                boolean match = false;
                if (find == null)
                    match = true;
                else {
                    if (find.startsWith(context.getString(R.string.title_search_special_prefix) + ":")) {
                        String special = find.split(":")[1];
                        if (context.getString(R.string.title_search_special_unseen).equals(special))
                            match = !message.ui_seen;
                        else if (context.getString(R.string.title_search_special_flagged).equals(special))
                            match = message.ui_flagged;
                        else if (context.getString(R.string.title_search_special_snoozed).equals(special))
                            match = (message.ui_snoozed != null);
                    } else {
                        List<Address> addresses = new ArrayList<>();
                        if (message.from != null)
                            addresses.addAll(Arrays.asList(message.from));
                        if (message.to != null)
                            addresses.addAll(Arrays.asList(message.to));
                        if (message.cc != null)
                            addresses.addAll(Arrays.asList(message.cc));

                        for (Address address : addresses) {
                            String email = ((InternetAddress) address).getAddress();
                            String name = ((InternetAddress) address).getPersonal();
                            if (email != null && email.toLowerCase(Locale.ROOT).contains(find) ||
                                    name != null && name.toLowerCase(Locale.ROOT).contains(find))
                                match = true;
                        }

                        if (!match && message.subject != null)
                            match = message.subject.toLowerCase(Locale.ROOT).contains(find);

                        if (!match && message.keywords != null && message.keywords.length > 0)
                            for (String keyword : message.keywords)
                                if (keyword.toLowerCase(Locale.ROOT).contains(find)) {
                                    match = true;
                                    break;
                                }

                        if (!match && message.content) {
                            try {
                                String body = Helper.readText(message.getFile(context));
                                match = body.toLowerCase(Locale.ROOT).contains(find);
                            } catch (IOException ex) {
                                Log.e(ex);
                            }
                        }
                    }
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

    private int load_server(State state) throws MessagingException, IOException {
        DB db = DB.getInstance(context);

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

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                boolean debug = (prefs.getBoolean("debug", false) || BuildConfig.BETA_RELEASE);

                Log.i("Boundary server connecting account=" + account.name);
                state.iservice = new MailService(context, account.getProtocol(), account.realm, account.insecure, debug);
                state.iservice.setPartialFetch(account.partial_fetch);
                state.iservice.setSeparateStoreConnection();
                state.iservice.connect(account);

                Log.i("Boundary server opening folder=" + browsable.name);
                state.ifolder = (IMAPFolder) state.iservice.getStore().getFolder(browsable.name);
                state.ifolder.open(Folder.READ_WRITE);

                int count = state.ifolder.getMessageCount();
                db.folder().setFolderTotal(browsable.id, count < 0 ? null : count);

                Log.i("Boundary server query=" + query);
                if (query == null) {
                    Calendar cal_browse = Calendar.getInstance();
                    if (browsable.synchronize)
                        cal_browse.add(Calendar.DAY_OF_MONTH, -browsable.keep_days);
                    else {
                        Long oldest = db.message().getMessageOldest(browsable.id);
                        Log.i("Boundary oldest=" + oldest);
                        if (oldest != null)
                            cal_browse.setTimeInMillis(oldest);
                    }

                    cal_browse.set(Calendar.HOUR_OF_DAY, 0);
                    cal_browse.set(Calendar.MINUTE, 0);
                    cal_browse.set(Calendar.SECOND, 0);
                    cal_browse.set(Calendar.MILLISECOND, 0);

                    cal_browse.add(Calendar.DAY_OF_MONTH, 1);

                    long browse_time = cal_browse.getTimeInMillis();
                    if (browse_time < 0)
                        browse_time = 0;

                    boolean filter_seen = prefs.getBoolean("filter_seen", false);
                    boolean filter_unflagged = prefs.getBoolean("filter_unflagged", false);
                    Log.i("Boundary browse after=" + new Date(browse_time) +
                            " filter seen=" + filter_seen + " unflagged=" + filter_unflagged);

                    SearchTerm searchTerm = new ReceivedDateTerm(ComparisonTerm.LE, new Date(browse_time));
                    if (filter_seen && state.ifolder.getPermanentFlags().contains(Flags.Flag.SEEN))
                        searchTerm = new AndTerm(searchTerm, new FlagTerm(new Flags(Flags.Flag.SEEN), false));
                    if (filter_unflagged && state.ifolder.getPermanentFlags().contains(Flags.Flag.FLAGGED))
                        searchTerm = new AndTerm(searchTerm, new FlagTerm(new Flags(Flags.Flag.FLAGGED), true));

                    state.imessages = state.ifolder.search(searchTerm);
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

            FetchProfile fp = new FetchProfile();
            fp.add(FetchProfile.Item.ENVELOPE);
            fp.add(FetchProfile.Item.FLAGS);
            fp.add(FetchProfile.Item.CONTENT_INFO); // body structure
            fp.add(UIDFolder.FetchProfileItem.UID);
            fp.add(IMAPFolder.FetchProfileItem.HEADERS);
            //fp.add(IMAPFolder.FetchProfileItem.MESSAGE);
            fp.add(FetchProfile.Item.SIZE);
            fp.add(IMAPFolder.FetchProfileItem.INTERNALDATE);
            state.ifolder.fetch(isub, fp);

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
                                    state.ifolder, (MimeMessage) isub[j],
                                    true, true,
                                    rules, null);
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
                            db.folder().setFolderError(browsable.id, Helper.formatThrowable(ex));
                        } else
                            throw ex;
                    } catch (Throwable ex) {
                        Log.e(browsable.name + " boundary server", ex);
                        db.folder().setFolderError(browsable.id, Helper.formatThrowable(ex));
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
        List<Long> messages = null;

        MailService iservice = null;
        IMAPFolder ifolder = null;
        Message[] imessages = null;
    }
}