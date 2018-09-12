package eu.faircode.email;

/*
    This file is part of FairEmail.

    FairEmail is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    NetGuard is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with NetGuard.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2018 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.IMAPStore;

import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.search.AndTerm;
import javax.mail.search.BodyTerm;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.FromStringTerm;
import javax.mail.search.OrTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SubjectTerm;

import androidx.lifecycle.GenericLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.paging.PagedList;

public class BoundaryCallbackMessages extends PagedList.BoundaryCallback<TupleMessageEx> {
    private Context context;
    private long fid;
    private String search;
    private Handler mainHandler;
    private IBoundaryCallbackMessages intf;
    private ExecutorService executor = Executors.newSingleThreadExecutor(Helper.backgroundThreadFactory);

    private boolean enabled = false;
    private IMAPStore istore = null;
    private IMAPFolder ifolder = null;
    private Message[] imessages = null;

    interface IBoundaryCallbackMessages {
        void onLoading();

        void onLoaded();

        void onError(Context context, Throwable ex);
    }

    BoundaryCallbackMessages(Context context, LifecycleOwner owner, long folder, String search, IBoundaryCallbackMessages intf) {
        this.context = context;
        this.fid = folder;
        this.search = search;
        this.mainHandler = new Handler(context.getMainLooper());
        this.intf = intf;

        owner.getLifecycle().addObserver(new GenericLifecycleObserver() {
            @Override
            public void onStateChanged(LifecycleOwner source, Lifecycle.Event event) {
                if (event == Lifecycle.Event.ON_DESTROY)
                    executor.submit(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(Helper.TAG, "Boundary close");
                            try {
                                if (istore != null)
                                    istore.close();
                            } catch (Throwable ex) {
                                Log.e(Helper.TAG, "Boundary " + ex + "\n" + Log.getStackTraceString(ex));
                            } finally {
                                istore = null;
                                ifolder = null;
                                imessages = null;
                            }
                        }
                    });
            }
        });
    }

    void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void onItemAtEndLoaded(final TupleMessageEx itemAtEnd) {
        Log.i(Helper.TAG, "onItemAtEndLoaded enabled=" + enabled);
        if (!enabled)
            return;
        load(itemAtEnd.received);
    }

    void load(final long before) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            intf.onLoading();
                        }
                    });

                    DB db = DB.getInstance(context);
                    EntityFolder folder = db.folder().getFolder(fid);
                    EntityAccount account = db.account().getAccount(folder.account);

                    if (imessages == null) {
                        // Refresh token
                        if (account.auth_type == Helper.AUTH_TYPE_GMAIL) {
                            account.password = Helper.refreshToken(context, "com.google", account.user, account.password);
                            db.account().setAccountPassword(account.id, account.password);
                        }

                        Properties props = MessageHelper.getSessionProperties(context, account.auth_type);
                        props.setProperty("mail.imap.throwsearchexception", "true");
                        Session isession = Session.getInstance(props, null);

                        Log.i(Helper.TAG, "Boundary connecting account=" + account.name);
                        istore = (IMAPStore) isession.getStore("imaps");
                        istore.connect(account.host, account.port, account.user, account.password);

                        Log.i(Helper.TAG, "Boundary opening folder=" + folder.name);
                        ifolder = (IMAPFolder) istore.getFolder(folder.name);
                        ifolder.open(Folder.READ_WRITE);

                        Log.i(Helper.TAG, "Boundary searching=" + search + " before=" + new Date(before));
                        imessages = ifolder.search(
                                new AndTerm(
                                        new ReceivedDateTerm(ComparisonTerm.LT, new Date(before)),
                                        new OrTerm(
                                                new FromStringTerm(search),
                                                new OrTerm(
                                                        new SubjectTerm(search),
                                                        new BodyTerm(search)))));
                        Log.i(Helper.TAG, "Boundary found messages=" + imessages.length);
                    }

                    int index = imessages.length - 1;
                    while (index >= 0) {
                        if (imessages[index].getReceivedDate().getTime() < before)
                            try {
                                Log.i(Helper.TAG, "Boundary sync uid=" + ifolder.getUID(imessages[index]));
                                ServiceSynchronize.synchronizeMessage(context, folder, ifolder, (IMAPMessage) imessages[index], true);
                                break;
                            } catch (Throwable ex) {
                                Log.e(Helper.TAG, "Boundary " + ex + "\n" + Log.getStackTraceString(ex));
                            }
                        index--;
                    }

                    EntityOperation.process(context); // download small attachments

                    Log.i(Helper.TAG, "Boundary done");
                } catch (final Throwable ex) {
                    Log.e(Helper.TAG, "Boundary " + ex + "\n" + Log.getStackTraceString(ex));
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            intf.onError(context, ex);
                        }
                    });
                } finally {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            intf.onLoaded();
                        }
                    });
                }
            }
        });
    }
}