package eu.faircode.email;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.UIDFolder;
import javax.mail.internet.MimeMessage;
import javax.mail.search.BodyTerm;
import javax.mail.search.FromStringTerm;
import javax.mail.search.OrTerm;
import javax.mail.search.SubjectTerm;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.paging.PositionalDataSource;

public class SearchDataSource extends PositionalDataSource<TupleMessageEx> implements LifecycleObserver {
    private Context context;
    private LifecycleOwner owner;
    private long fid;
    private String search;

    private EntityFolder folder;
    private EntityAccount account;
    private IMAPStore istore = null;
    private IMAPFolder ifolder;
    private Message[] imessages;

    private SparseArray<TupleMessageEx> cache = new SparseArray<>();

    private static final float MAX_MEMORY_USAGE = 0.6f; // percent

    SearchDataSource(Context context, LifecycleOwner owner, long folder, String search) {
        Log.i(Helper.TAG, "SDS create");

        this.context = context;
        this.owner = owner;
        this.fid = folder;
        this.search = search;

        owner.getLifecycle().addObserver(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroyed() {
        Log.i(Helper.TAG, "SDS destroy");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (istore != null)
                        istore.close();
                } catch (MessagingException ex) {
                    Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                } finally {
                    istore = null;
                    ifolder = null;
                    imessages = null;
                    cache.clear();
                }
            }
        }).start();

        owner.getLifecycle().removeObserver(this);
    }

    @Override
    public void loadInitial(LoadInitialParams params, LoadInitialCallback<TupleMessageEx> callback) {
        Log.i(Helper.TAG, "SDS load initial");
        try {
            SearchResult result = search(search, params.requestedStartPosition, params.requestedLoadSize);
            callback.onResult(result.messages, params.requestedStartPosition, result.total);
        } catch (Throwable ex) {
            Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
            reportError(ex);
        }
    }

    @Override
    public void loadRange(LoadRangeParams params, LoadRangeCallback<TupleMessageEx> callback) {
        Log.i(Helper.TAG, "SDS load range");
        try {
            SearchResult result = search(search, params.startPosition, params.loadSize);
            callback.onResult(result.messages);
        } catch (Throwable ex) {
            Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
            reportError(ex);
        }
    }

    private SearchResult search(String term, int from, int count) throws MessagingException, UnsupportedEncodingException {
        Log.i(Helper.TAG, "SDS search from=" + from + " count=" + count);

        if (istore == null) {
            DB db = DB.getInstance(context);
            folder = db.folder().getFolder(fid);
            account = db.account().getAccount(folder.account);

            // Refresh token
            if (account.auth_type == Helper.AUTH_TYPE_GMAIL) {
                account.password = Helper.refreshToken(context, "com.google", account.user, account.password);
                db.account().setAccountPassword(account.id, account.password);
            }

            Properties props = MessageHelper.getSessionProperties(context, account.auth_type);
            props.setProperty("mail.imap.throwsearchexception", "true");
            Session isession = Session.getInstance(props, null);

            Log.i(Helper.TAG, "SDS connecting account=" + account.name);
            istore = (IMAPStore) isession.getStore("imaps");
            istore.connect(account.host, account.port, account.user, account.password);

            Log.i(Helper.TAG, "SDS opening folder=" + folder.name);
            ifolder = (IMAPFolder) istore.getFolder(folder.name);
            ifolder.open(Folder.READ_WRITE);

            Log.i(Helper.TAG, "SDS searching term=" + term);
            imessages = ifolder.search(
                    new OrTerm(
                            new FromStringTerm(term),
                            new OrTerm(
                                    new SubjectTerm(term),
                                    new BodyTerm(term))));
            Log.i(Helper.TAG, "SDS found messages=" + imessages.length);
        }

        SearchResult result = new SearchResult();
        result.total = imessages.length;
        result.messages = new ArrayList<>();

        List<Message> selected = new ArrayList<>();
        int base = imessages.length - 1 - from;
        for (int i = base; i >= 0 && i >= base - count + 1; i--)
            selected.add(imessages[i]);
        Log.i(Helper.TAG, "SDS selected messages=" + selected.size());

        FetchProfile fp = new FetchProfile();
        fp.add(UIDFolder.FetchProfileItem.UID);
        fp.add(IMAPFolder.FetchProfileItem.FLAGS);
        fp.add(FetchProfile.Item.ENVELOPE);
        fp.add(FetchProfile.Item.CONTENT_INFO);
        fp.add(IMAPFolder.FetchProfileItem.HEADERS);
        fp.add(IMAPFolder.FetchProfileItem.MESSAGE);
        ifolder.fetch(selected.toArray(new Message[0]), fp);

        for (int s = 0; s < selected.size(); s++) {
            int pos = from + s;
            if (cache.get(pos) != null) {
                Log.i(Helper.TAG, "SDS from cache pos=" + pos);
                result.messages.add(cache.get(pos));
                continue;
            }

            Message imessage = selected.get(s);

            long uid = ifolder.getUID(imessage);

            MessageHelper helper = new MessageHelper((MimeMessage) imessage);
            boolean seen = helper.getSeen();

            TupleMessageEx message = new TupleMessageEx();
            message.id = uid;
            message.account = folder.account;
            message.folder = folder.id;
            message.uid = uid;
            message.msgid = helper.getMessageID();
            message.references = TextUtils.join(" ", helper.getReferences());
            message.inreplyto = helper.getInReplyTo();
            message.thread = helper.getThreadId(uid);
            message.from = helper.getFrom();
            message.to = helper.getTo();
            message.cc = helper.getCc();
            message.bcc = helper.getBcc();
            message.reply = helper.getReply();
            message.subject = imessage.getSubject();
            message.received = imessage.getReceivedDate().getTime();
            message.sent = (imessage.getSentDate() == null ? null : imessage.getSentDate().getTime());
            message.seen = seen;
            message.ui_seen = seen;
            message.ui_hide = false;

            message.accountName = account.name;
            message.folderName = folder.name;
            message.folderType = folder.type;
            message.count = 1;
            message.unseen = (seen ? 0 : 1);
            message.attachments = 0;

            message.body = helper.getHtml();
            message.virtual = true;

            result.messages.add(message);

            Runtime rt = Runtime.getRuntime();
            float used = (float) (rt.totalMemory() - rt.freeMemory()) / rt.maxMemory();
            if (used < MAX_MEMORY_USAGE)
                cache.put(pos, message);
            else
                Log.i(Helper.TAG, "SDS memory used=" + used);
        }

        Log.i(Helper.TAG, "SDS result=" + result.messages.size());
        return result;
    }

    private void reportError(final Throwable ex) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, Helper.formatThrowable(ex), Toast.LENGTH_LONG).show();
            }
        });
    }

    private class SearchResult {
        int total;
        List<TupleMessageEx> messages;
    }
}
