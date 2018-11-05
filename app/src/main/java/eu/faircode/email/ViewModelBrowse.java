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

    Copyright 2018 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.util.Log;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.util.FolderClosedIOException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.Message;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.UIDFolder;
import javax.mail.search.BodyTerm;
import javax.mail.search.FromStringTerm;
import javax.mail.search.OrTerm;
import javax.mail.search.RecipientStringTerm;
import javax.mail.search.SubjectTerm;

import androidx.lifecycle.ViewModel;

public class ViewModelBrowse extends ViewModel {
    private Context context;
    private long fid;
    private String search;
    private int pageSize;

    private int local = 0;
    private List<Long> messages = null;
    private IMAPStore istore = null;
    private IMAPFolder ifolder = null;
    private Message[] imessages = null;

    private int index = -1;

    void set(Context context, long folder, String search, int pageSize) {
        this.context = context;
        this.fid = folder;
        this.search = search;
        this.pageSize = pageSize;

        this.index = -1;
    }

    @Override
    protected void onCleared() {
        context = null;
        istore = null;
        ifolder = null;
        imessages = null;
    }

    Context getContext() {
        return context;
    }

    void load() throws MessagingException, IOException {
        DB db = DB.getInstance(context);
        EntityFolder folder = db.folder().getFolder(fid);

        if (search != null)
            try {
                db.beginTransaction();

                if (messages == null)
                    messages = db.message().getMessageByFolder(fid);

                int matched = 0;
                for (int i = local; i < messages.size() && matched < pageSize; i++) {
                    local = i + 1;

                    boolean match = false;
                    String find = search.toLowerCase();
                    EntityMessage message = db.message().getMessage(messages.get(i));
                    String body = (message.content ? message.read(context) : null);

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
                        matched++;
                        message.id = null;
                        message.ui_found = true;
                        message.id = db.message().insertMessage(message);
                        if (message.content)
                            message.write(context, body);
                    }
                }

                db.setTransactionSuccessful();

                if (++matched >= pageSize)
                    return;
            } finally {
                db.endTransaction();
            }

        if (imessages == null) {
            if (folder.account == null) // outbox
                return;

            EntityAccount account = db.account().getAccount(folder.account);

            Properties props = MessageHelper.getSessionProperties(account.auth_type, account.insecure);
            props.setProperty("mail.imap.throwsearchexception", "true");
            Session isession = Session.getInstance(props, null);

            Log.i(Helper.TAG, "Boundary connecting account=" + account.name);
            istore = (IMAPStore) isession.getStore(account.starttls ? "imap" : "imaps");
            Helper.connect(context, istore, account);

            Log.i(Helper.TAG, "Boundary opening folder=" + folder.name);
            ifolder = (IMAPFolder) istore.getFolder(folder.name);
            ifolder.open(Folder.READ_WRITE);

            Log.i(Helper.TAG, "Boundary searching=" + search);
            if (search == null)
                imessages = ifolder.getMessages();
            else
                imessages = ifolder.search(
                        new OrTerm(
                                new OrTerm(
                                        new FromStringTerm(search),
                                        new RecipientStringTerm(Message.RecipientType.TO, search)
                                ),
                                new OrTerm(
                                        new SubjectTerm(search),
                                        new BodyTerm(search)
                                )
                        )
                );
            Log.i(Helper.TAG, "Boundary found messages=" + imessages.length);

            index = imessages.length - 1;
        }

        int count = 0;
        while (index >= 0 && count < pageSize) {
            Log.i(Helper.TAG, "Boundary index=" + index);
            int from = Math.max(0, index - (pageSize - count) + 1);
            Message[] isub = Arrays.copyOfRange(imessages, from, index + 1);
            index -= (pageSize - count);

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

                for (int j = isub.length - 1; j >= 0; j--)
                    try {
                        long uid = ifolder.getUID(isub[j]);
                        Log.i(Helper.TAG, "Boundary sync uid=" + uid);
                        EntityMessage message = db.message().getMessageByUid(fid, uid, search != null);
                        if (message == null) {
                            ServiceSynchronize.synchronizeMessage(context, folder, ifolder, (IMAPMessage) isub[j], search != null);
                            count++;
                        }
                    } catch (MessageRemovedException ex) {
                        Log.w(Helper.TAG, "Boundary " + ex + "\n" + Log.getStackTraceString(ex));
                    } catch (FolderClosedException ex) {
                        throw ex;
                    } catch (FolderClosedIOException ex) {
                        throw ex;
                    } catch (Throwable ex) {
                        Log.e(Helper.TAG, "Boundary " + ex + "\n" + Log.getStackTraceString(ex));
                    } finally {
                        ((IMAPMessage) isub[j]).invalidateHeaders();
                    }

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }

        Log.i(Helper.TAG, "Boundary done");
    }

    void clear() {
        Log.i(Helper.TAG, "Boundary clear");
        try {
            if (istore != null)
                istore.close();
        } catch (Throwable ex) {
            Log.e(Helper.TAG, "Boundary " + ex + "\n" + Log.getStackTraceString(ex));
        } finally {
            context = null;
            local = 0;
            messages = null;
            istore = null;
            ifolder = null;
            imessages = null;
        }
    }
}
