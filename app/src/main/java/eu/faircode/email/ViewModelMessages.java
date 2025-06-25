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

    Copyright 2018-2025 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ViewModel;
import androidx.paging.DataSource;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;
import androidx.preference.PreferenceManager;
import androidx.room.paging.LimitOffsetDataSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

public class ViewModelMessages extends ViewModel {
    private AdapterMessage.ViewType last = AdapterMessage.ViewType.UNIFIED;
    private Map<AdapterMessage.ViewType, Model> models = new HashMap<AdapterMessage.ViewType, Model>() {
        @Nullable
        @Override
        public Model put(AdapterMessage.ViewType key, Model value) {
            Model existing = this.get(key);
            if (existing != null && existing.boundary != null)
                existing.boundary.destroy(existing.boundary.getState());
            return super.put(key, value);
        }

        @Nullable
        @Override
        public Model remove(@Nullable Object key) {
            Model existing = this.get(key);
            if (existing != null && existing.boundary != null)
                existing.boundary.destroy(existing.boundary.getState());
            return super.remove(key);
        }
    };

    private static final ExecutorService executor =
            Helper.getBackgroundExecutor(0, "model");

    private static final int LOCAL_PAGE_SIZE = 50;
    private static final int THREAD_PAGE_SIZE = 100;
    private static final int REMOTE_PAGE_SIZE = 10;
    private static final int SEARCH_PAGE_SIZE = 10;
    private static final int MAX_CACHED_ITEMS = LOCAL_PAGE_SIZE * 50;
    private static final int CHUNK_SIZE = 100;

    Model getModel(
            final Context context, final LifecycleOwner owner,
            final AdapterMessage.ViewType viewType,
            String type, String category, long account, long folder,
            String thread, long id,
            boolean threading,
            boolean filter_archive,
            BoundaryCallbackMessages.SearchCriteria criteria, boolean server) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean cache_lists = prefs.getBoolean("cache_lists", true);

        Args args = new Args(context,
                viewType, type, category, account, folder,
                thread, id, threading,
                filter_archive, criteria, server);
        Log.i("Get model=" + viewType + " " + args);
        dump();

        Model model = models.get(viewType);
        if (model == null || !model.args.equals(args)) {
            Log.i("Creating model=" + viewType + " replace=" + (model != null));

            if (model != null)
                model.list.removeObservers(owner);

            DB db = DB.getInstance(context);

            BoundaryCallbackMessages boundary = null;
            if (viewType == AdapterMessage.ViewType.FOLDER)
                boundary = new BoundaryCallbackMessages(context,
                        viewType, args.account, args.folder, true, args.criteria, REMOTE_PAGE_SIZE);
            else if (viewType == AdapterMessage.ViewType.SEARCH)
                boundary = new BoundaryCallbackMessages(context,
                        viewType, args.account, args.folder, args.server, args.criteria,
                        args.server ? REMOTE_PAGE_SIZE : SEARCH_PAGE_SIZE);

            DataSource.Factory<Integer, TupleMessageEx> pager;
            LivePagedListBuilder<Integer, TupleMessageEx> builder = null;
            switch (viewType) {
                case UNIFIED:
                    PagedList.Config configUnified = new PagedList.Config.Builder()
                            .setPageSize(LOCAL_PAGE_SIZE)
                            .setMaxSize(MAX_CACHED_ITEMS)
                            .build();
                    if ("sender_name".equals(args.sort1))
                        pager = db.message().pagedUnifiedJson(
                                args.type,
                                args.category,
                                args.threading,
                                args.group_category,
                                args.sort1, args.sort2, args.ascending,
                                args.filter_seen,
                                args.filter_unseen,
                                args.filter_flagged,
                                args.filter_unflagged,
                                args.filter_unknown,
                                args.filter_snoozed,
                                args.filter_deleted,
                                args.filter_language,
                                false,
                                args.debug);
                    else
                        pager = db.message().pagedUnified(
                                args.type,
                                args.category,
                                args.threading,
                                args.group_category,
                                args.sort1, args.sort2, args.ascending,
                                args.filter_seen,
                                args.filter_unseen,
                                args.filter_flagged,
                                args.filter_unflagged,
                                args.filter_unknown,
                                args.filter_snoozed,
                                args.filter_deleted,
                                args.filter_language,
                                false,
                                args.debug);
                    builder = new LivePagedListBuilder<>(pager, configUnified);
                    break;

                case FOLDER:
                    PagedList.Config configFolder = new PagedList.Config.Builder()
                            .setInitialLoadSizeHint(LOCAL_PAGE_SIZE)
                            .setPageSize(LOCAL_PAGE_SIZE)
                            .setPrefetchDistance(REMOTE_PAGE_SIZE)
                            .setMaxSize(MAX_CACHED_ITEMS)
                            .build();
                    if ("sender_name".equals(args.sort1))
                        pager = db.message().pagedFolderJson(
                                args.folder, args.threading,
                                args.sort1, args.sort2, args.ascending,
                                args.filter_seen,
                                args.filter_unseen,
                                args.filter_flagged,
                                args.filter_unflagged,
                                args.filter_unknown,
                                args.filter_snoozed,
                                args.filter_deleted,
                                args.filter_language,
                                false,
                                args.debug);
                    else
                        pager = db.message().pagedFolder(
                                args.folder, args.threading,
                                args.sort1, args.sort2, args.ascending,
                                args.filter_seen,
                                args.filter_unseen,
                                args.filter_flagged,
                                args.filter_unflagged,
                                args.filter_unknown,
                                args.filter_snoozed,
                                args.filter_deleted,
                                args.filter_language,
                                false,
                                args.debug);
                    builder = new LivePagedListBuilder<>(pager, configFolder);
                    builder.setBoundaryCallback(boundary);
                    break;

                case THREAD:
                    PagedList.Config configThread = new PagedList.Config.Builder()
                            .setPageSize(THREAD_PAGE_SIZE)
                            .build();
                    builder = new LivePagedListBuilder<>(
                            db.message().pagedThread(
                                    args.account, args.thread,
                                    args.threading ? null : args.id,
                                    args.filter_archive && args.threading,
                                    args.ascending,
                                    args.debug),
                            configThread);
                    break;

                case SEARCH:
                    PagedList.Config configSearch = new PagedList.Config.Builder()
                            .setPageSize(LOCAL_PAGE_SIZE)
                            .setPrefetchDistance(REMOTE_PAGE_SIZE)
                            .setMaxSize(MAX_CACHED_ITEMS)
                            .build();
                    if (args.folder < 0) {
                        if ("sender_name".equals(args.sort1))
                            pager = db.message().pagedUnifiedJson(
                                    null, null,
                                    args.threading, false,
                                    criteria == null || criteria.touched == null ? "time" : "touched", "", false,
                                    false, false, false, false, false, false, false,
                                    null,
                                    true,
                                    args.debug);
                        else
                            pager = db.message().pagedUnified(
                                    null, null,
                                    args.threading, false,
                                    criteria == null || criteria.touched == null ? "time" : "touched", "", false,
                                    false, false, false, false, false, false, false,
                                    null,
                                    true,
                                    args.debug);
                        builder = new LivePagedListBuilder<>(pager, configSearch);
                    } else {
                        if ("sender_name".equals(args.sort1))
                            pager = db.message().pagedFolderJson(
                                    args.folder, args.threading,
                                    criteria == null || criteria.touched == null ? "time" : "touched", "", false,
                                    false, false, false, false, false, false, false,
                                    null,
                                    true,
                                    args.debug);
                        else
                            pager = db.message().pagedFolder(
                                    args.folder, args.threading,
                                    criteria == null || criteria.touched == null ? "time" : "touched", "", false,
                                    false, false, false, false, false, false, false,
                                    null,
                                    true,
                                    args.debug);
                        builder = new LivePagedListBuilder<>(pager, configSearch);
                    }
                    builder.setBoundaryCallback(boundary);
                    break;
            }

            builder.setFetchExecutor(executor);

            model = new Model(args, builder.build(), boundary);
            models.put(viewType, model);
        }

        owner.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            public void onPaused() {
                Log.i("Paused model=" + viewType + " last=" + last);
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            public void onResumed() {
                Log.i("Resumed model=" + viewType + " last=" + last);
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            public void onDestroyed() {
                Log.i("Destroy model=" + viewType);

                Model model = models.get(viewType);
                if (model != null) {
                    Log.i("Remove observer model=" + viewType);
                    model.list.removeObservers(owner);
                }

                if (viewType == AdapterMessage.ViewType.THREAD || viewType == AdapterMessage.ViewType.SEARCH) {
                    Log.i("Remove model=" + viewType);
                    models.remove(viewType);
                }

                dump();

                owner.getLifecycle().removeObserver(this);
            }
        });

        if (cache_lists) {
            if (viewType == AdapterMessage.ViewType.UNIFIED)
                models.remove(AdapterMessage.ViewType.FOLDER);

            if (viewType != AdapterMessage.ViewType.SEARCH &&
                    viewType != AdapterMessage.ViewType.THREAD)
                models.remove(AdapterMessage.ViewType.SEARCH);
        } else {
            for (AdapterMessage.ViewType v : AdapterMessage.ViewType.values())
                if (v != viewType)
                    models.remove(v);
        }

        if (viewType != AdapterMessage.ViewType.THREAD) {
            last = viewType;
            Log.i("Last model=" + last);
        }

        Helper.gc("model:get");

        Log.i("Returning model=" + viewType);
        dump();

        return model;
    }

    void retry(AdapterMessage.ViewType viewType) {
        Model model = models.get(viewType);
        if (model != null && model.boundary != null)
            model.boundary.retry();
    }

    @Override
    protected void onCleared() {
        for (AdapterMessage.ViewType viewType : new ArrayList<>(models.keySet()))
            models.remove(viewType);
    }

    void observePrevNext(Context context, LifecycleOwner owner, final long id, int lpos, final IPrevNext intf) {
        Log.i("Observe prev/next model=" + last + " id=" + id + " lpos=" + lpos);

        final Model model = models.get(last);
        if (model == null) {
            // When showing accounts or folders
            intf.onPrevious(false, null);
            intf.onNext(false, null);
            intf.onFound(-1, 0);
            return;
        }

        Log.i("Observe previous/next id=" + id);
        //model.list.getValue().loadAround(lpos);
        model.list.observe(owner, new Observer<PagedList<TupleMessageEx>>() {
            private final Pair<Long, Integer>[] lastState = new Pair[3];

            @Override
            public void onChanged(PagedList<TupleMessageEx> messages) {
                Log.i("Observe previous/next id=" + id +
                        " lpos=" + lpos +
                        " messages=" + messages.size());

                Pair<Long, Integer>[] curState = new Pair[3];
                for (int pos = 0; pos < messages.size(); pos++) {
                    TupleMessageEx item = messages.get(pos);
                    if (item != null && id == item.id) {
                        curState[1] = new Pair<>(id, pos);
                        Log.i("Observe previous/next found id=" + id + " pos=" + pos);

                        if (pos - 1 >= 0) {
                            TupleMessageEx next = messages.get(pos - 1);
                            curState[2] = new Pair<>(next == null ? null : next.id, pos - 1);
                        }

                        if (pos + 1 < messages.size()) {
                            TupleMessageEx prev = messages.get(pos + 1);
                            curState[0] = new Pair<>(prev == null ? null : prev.id, pos + 1);
                        }

                        break;
                    }
                }

                if (curState[1] == null) // first changed
                    curState[1] = new Pair<>(id, -1);

                if (Objects.equals(lastState[0], curState[0]) &&
                        Objects.equals(lastState[1], curState[1]) &&
                        Objects.equals(lastState[2], curState[2])) {
                    Log.i("Observe previous/next unchanged");
                    return;
                }

                Log.i("Observe previous/next" +
                        " prev=" + (curState[0] == null ? null : curState[0].first + "/" + curState[0].second) +
                        " base=" + (curState[1] == null ? null : curState[1].first + "/" + curState[1].second) +
                        " next=" + (curState[2] == null ? null : curState[2].first + "/" + curState[2].second));

                if (curState[1].second >= 0)
                    intf.onFound(curState[1].second, messages.size());
                if (!(lastState[0] != null && curState[0] == null))
                    intf.onPrevious(curState[0] != null, curState[0] == null ? null : curState[0].first);
                if (!(lastState[2] != null && curState[2] == null))
                    intf.onNext(curState[2] != null, curState[2] == null ? null : curState[2].first);

                lastState[0] = curState[0];
                lastState[1] = curState[1];
                lastState[2] = curState[2];

                if (curState[1].second >= 0 &&
                        (curState[0] == null || curState[0].first != null) &&
                        (curState[2] == null || curState[2].first != null))
                    return;

                Bundle args = new Bundle();
                args.putLong("id", id);
                args.putInt("lpos", lpos);

                new SimpleTask<Pair<Long, Long>>() {
                    @Override
                    protected Pair<Long, Long> onExecute(Context context, Bundle args) {
                        long id = args.getLong("id");
                        int lpos = args.getInt("lpos");

                        if (!isAlive())
                            return null;

                        PagedList<TupleMessageEx> plist = model.list.getValue();
                        if (plist == null)
                            return null;

                        LimitOffsetDataSource<TupleMessageEx> ds = (LimitOffsetDataSource<TupleMessageEx>) plist.getDataSource();
                        int count = ds.countItems();

                        if (lpos >= 0) {
                            int from = Math.max(0, lpos - 10);
                            int load = Math.min(20, count - from);
                            Log.i("Observe previous/next load lpos=" + lpos +
                                    " range=" + from + "/#" + load);
                            List<TupleMessageEx> messages = ds.loadRange(from, load);
                            for (int j = 0; j < messages.size(); j++)
                                if (messages.get(j).id == id)
                                    return getPair(plist, ds, count, from + j);
                        }

                        for (int i = 0; i < count && isAlive(); i += CHUNK_SIZE) {
                            Log.i("Observe previous/next load" +
                                    " range=" + i + "/#" + count);
                            List<TupleMessageEx> messages = ds.loadRange(i, Math.min(CHUNK_SIZE, count - i));
                            for (int j = 0; j < messages.size(); j++)
                                if (messages.get(j).id == id)
                                    return getPair(plist, ds, count, i + j);

                            if (lpos < 0 && i == CHUNK_SIZE * 2 && count > CHUNK_SIZE * 4)
                                i = count - CHUNK_SIZE * 2;
                        }

                        Log.i("Observe previous/next message not found" +
                                " lpos=" + lpos + " count=" + count + " " + model.args);

                        return null;
                    }

                    @Override
                    protected void onExecuted(Bundle args, Pair<Long, Long> data) {
                        if (data == null) {
                            Log.i("Observe previous/next fallback=none");
                            return; // keep current
                        }

                        intf.onPrevious(data.first != null, data.first);
                        intf.onNext(data.second != null, data.second);
                        intf.onFound(-1, messages.size());
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        // No nothing
                    }

                    private Pair<Long, Long> getPair(
                            PagedList<TupleMessageEx> plist,
                            LimitOffsetDataSource<TupleMessageEx> ds,
                            int count, int pos) {
                        if (pos < plist.size())
                            plist.loadAround(pos);

                        List<TupleMessageEx> lprev = null;
                        if (pos - 1 >= 0)
                            lprev = ds.loadRange(pos - 1, 1);

                        List<TupleMessageEx> lnext = null;
                        if (pos + 1 < count)
                            lnext = ds.loadRange(pos + 1, 1);

                        TupleMessageEx prev = (lprev != null && lprev.size() > 0 ? lprev.get(0) : null);
                        TupleMessageEx next = (lnext != null && lnext.size() > 0 ? lnext.get(0) : null);

                        Pair<Long, Long> result = new Pair<>(
                                prev == null ? null : prev.id,
                                next == null ? null : next.id);
                        Log.i("Observe previous/next fallback=" + result);
                        return result;
                    }
                }.setExecutor(executor).execute(context, owner, args, "model:fallback");
            }
        });
    }

    void getIds(Context context, LifecycleOwner owner, long from, long to, final Observer<List<Long>> observer) {
        final Model model = models.get(last);
        if (model == null) {
            Log.w("Get IDs without model");
            observer.onChanged(new ArrayList<Long>());
            return;
        }

        new SimpleTask<List<Long>>() {
            @Override
            protected List<Long> onExecute(Context context, Bundle args) {
                List<Long> ids = new ArrayList<>();

                if (!isAlive())
                    return ids;

                PagedList<TupleMessageEx> plist = model.list.getValue();
                if (plist == null)
                    return ids;

                LimitOffsetDataSource<TupleMessageEx> ds = (LimitOffsetDataSource<TupleMessageEx>) plist.getDataSource();
                int count = ds.countItems();
                for (int i = 0; i < count && isAlive(); i += 100)
                    for (TupleMessageEx message : ds.loadRange(i, Math.min(100, count - i)))
                        if ((message.received >= from && message.received < to) &&
                                ((message.uid != null && !message.folderReadOnly) ||
                                        message.accountProtocol != EntityAccount.TYPE_IMAP))
                            ids.add(message.id);

                Log.i("Loaded messages #" + ids.size());
                return ids;
            }

            @Override
            protected void onExecuted(Bundle args, List<Long> ids) {
                observer.onChanged(ids);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                observer.onChanged(new ArrayList<Long>());
            }
        }.execute(context, owner, new Bundle(), "model:ids");
    }

    void cleanup() {
        dump();
        for (AdapterMessage.ViewType viewType : new ArrayList<>(models.keySet())) {
            if (viewType != last && !models.get(viewType).list.hasObservers()) {
                Log.i("Cleanup model viewType=" + viewType);
                models.remove(viewType);
            }
        }
    }

    void clear() {
        models.clear();
    }

    private class Args {
        private long account;
        private String type;
        private String category;
        private long folder;
        private String thread;
        private long id;
        private BoundaryCallbackMessages.SearchCriteria criteria;
        private boolean server;

        private boolean threading;
        private boolean group_category;
        private String sort1;
        private String sort2;
        private boolean ascending;
        private boolean filter_seen;
        private boolean filter_unseen;
        private boolean filter_flagged;
        private boolean filter_unflagged;
        private boolean filter_unknown;
        private boolean filter_snoozed;
        private boolean filter_archive;
        private boolean filter_deleted;
        private String filter_language;
        private boolean debug;

        Args(Context context,
             AdapterMessage.ViewType viewType,
             String type, String category, long account, long folder,
             String thread, long id, boolean threading,
             boolean filter_archive,
             BoundaryCallbackMessages.SearchCriteria criteria, boolean server) {

            this.type = type;
            this.category = category;
            this.account = account;
            this.folder = folder;
            this.thread = thread;
            this.id = id;
            this.threading = threading;
            this.filter_archive = filter_archive;
            this.criteria = criteria;
            this.server = server;

            boolean outbox = EntityFolder.OUTBOX.equals(type);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            this.group_category = prefs.getBoolean("group_category", false);

            String sort = prefs.getString(FragmentMessages.getSort(context, viewType, type), "time");
            String[] sorts = sort.split("\\+");
            this.sort1 = sorts[0];
            this.sort2 = (sorts.length > 1 ? sorts[1] : "");
            this.ascending = prefs.getBoolean(FragmentMessages.getSortOrder(context, viewType, type), outbox);

            if ("sender_name".equals(this.sort1) && !DB.hasJson())
                this.sort1 = "sender";

            this.filter_seen = prefs.getBoolean(FragmentMessages.getFilter(context, "seen", viewType, type), false);
            this.filter_unseen = prefs.getBoolean(FragmentMessages.getFilter(context, "unseen", viewType, type), false);
            this.filter_flagged = prefs.getBoolean(FragmentMessages.getFilter(context, "flagged", viewType, type), false);
            this.filter_unflagged = prefs.getBoolean(FragmentMessages.getFilter(context, "unflagged", viewType, type), false);
            this.filter_unknown = prefs.getBoolean(FragmentMessages.getFilter(context, "unknown", viewType, type), false);
            this.filter_snoozed = prefs.getBoolean(FragmentMessages.getFilter(context, "snoozed", viewType, type), true);
            this.filter_deleted = prefs.getBoolean(FragmentMessages.getFilter(context, "deleted", viewType, type), false);

            boolean language_detection = prefs.getBoolean("language_detection", false);
            String filter_language = prefs.getString("filter_language", null);
            this.filter_language = (language_detection ? filter_language : null);

            this.debug = prefs.getBoolean("debug", false);
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj instanceof Args) {
                Args other = (Args) obj;
                return (Objects.equals(this.type, other.type) &&
                        Objects.equals(this.category, other.category) &&
                        this.account == other.account &&
                        this.folder == other.folder &&
                        Objects.equals(this.thread, other.thread) &&
                        this.id == other.id &&
                        Objects.equals(this.criteria, other.criteria) &&
                        this.server == other.server &&

                        this.threading == other.threading &&
                        this.group_category == other.group_category &&
                        Objects.equals(this.sort1, other.sort1) &&
                        Objects.equals(this.sort2, other.sort2) &&
                        this.ascending == other.ascending &&
                        this.filter_seen == other.filter_seen &&
                        this.filter_unseen == other.filter_unseen &&
                        this.filter_flagged == other.filter_flagged &&
                        this.filter_unflagged == other.filter_unflagged &&
                        this.filter_unknown == other.filter_unknown &&
                        this.filter_snoozed == other.filter_snoozed &&
                        this.filter_archive == other.filter_archive &&
                        this.filter_deleted == other.filter_deleted &&
                        Objects.equals(this.filter_language, other.filter_language) &&
                        this.debug == other.debug);
            } else
                return false;
        }

        @NonNull
        @Override
        public String toString() {
            return "folder=" + type + "/" + category + ":" + account + ":" + folder +
                    " thread=" + thread + ":" + id +
                    " criteria=" + criteria + ":" + server + "" +
                    " threading=" + threading +
                    " category=" + group_category +
                    " sort=" + sort1 + "/" + sort2 + ":" + ascending +
                    " filter seen=" + filter_seen + "/" + filter_unseen +
                    " flagged=" + filter_flagged + "/" + filter_unflagged +
                    " unknown=" + filter_unknown +
                    " snoozed=" + filter_snoozed +
                    " archive=" + filter_archive +
                    " language=" + filter_language +
                    " debug=" + debug;
        }
    }

    private void dump() {
        Log.i("Current models=" + TextUtils.join(", ", models.keySet()));
    }

    class Model {
        private Args args;
        private LiveData<PagedList<TupleMessageEx>> list;
        private BoundaryCallbackMessages boundary;

        Model(Args args, LiveData<PagedList<TupleMessageEx>> list, BoundaryCallbackMessages boundary) {
            this.args = args;
            this.list = list;
            this.boundary = boundary;
        }

        void setCallback(LifecycleOwner owner, BoundaryCallbackMessages.IBoundaryCallbackMessages callback) {
            if (boundary != null) {
                BoundaryCallbackMessages.State state = boundary.setCallback(callback);
                PagedList<TupleMessageEx> plist = list.getValue();
                if (plist != null && plist.size() > 0)
                    plist.loadAround(0);

                owner.getLifecycle().addObserver(new LifecycleObserver() {
                    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                    public void onDestroyed() {
                        if (boundary != null) {
                            boundary.destroy(state);
                            boundary = null;
                        }
                        owner.getLifecycle().removeObserver(this);
                    }
                });
            }
        }

        void setObserver(LifecycleOwner owner, @NonNull Observer<PagedList<TupleMessageEx>> observer) {
            //list.removeObservers(owner);
            list.observe(owner, observer);
        }
    }

    interface IPrevNext {
        void onPrevious(boolean exists, Long id);

        void onNext(boolean exists, Long id);

        void onFound(int position, int size);
    }
}
