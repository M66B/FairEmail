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

import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.Html;
import android.text.Layout;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.openintents.openpgp.OpenPgpError;
import org.openintents.openpgp.util.OpenPgpApi;
import org.openintents.openpgp.util.OpenPgpServiceConnection;
import org.xml.sax.XMLReader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.Collator;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.mail.internet.InternetAddress;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.app.Activity.RESULT_OK;

public class FragmentMessage extends FragmentEx {
    private ViewGroup view;
    private View vwAnswerAnchor;
    private TextView tvFrom;
    private TextView tvSize;
    private TextView tvTime;
    private TextView tvTo;
    private TextView tvSubject;
    private TextView tvCount;
    private TextView tvReplyTo;
    private TextView tvCc;
    private TextView tvBcc;
    private RecyclerView rvAttachment;
    private TextView tvError;
    private View vSeparatorBody;
    private Button btnImages;
    private TextView tvBody;
    private ProgressBar pbBody;
    private FloatingActionButton fab;
    private BottomNavigationView bottom_navigation;
    private ProgressBar pbWait;
    private Group grpHeader;
    private Group grpAddresses;
    private Group grpAttachments;
    private Group grpError;
    private Group grpMessage;

    private TupleMessageEx message = null;
    private boolean free = false;
    private AdapterAttachment adapter;

    private String decrypted = null;
    private OpenPgpServiceConnection openPgpConnection = null;

    private boolean debug;
    private DateFormat df = SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

    private static final long CACHE_IMAGE_DURATION = 3 * 24 * 3600 * 1000L;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null)
            message = (TupleMessageEx) getArguments().getSerializable("message");
        else
            message = (TupleMessageEx) savedInstanceState.getSerializable("message");

        openPgpConnection = new OpenPgpServiceConnection(getContext(), "org.sufficientlysecure.keychain");
        openPgpConnection.bindToService();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (openPgpConnection != null) {
            openPgpConnection.unbindFromService();
            openPgpConnection = null;
        }
    }

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = (ViewGroup) inflater.inflate(R.layout.fragment_message, container, false);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        debug = prefs.getBoolean("debug", false);

        // Get controls
        vwAnswerAnchor = view.findViewById(R.id.vwAnswerAnchor);
        tvFrom = view.findViewById(R.id.tvFrom);
        tvSize = view.findViewById(R.id.tvSize);
        tvTime = view.findViewById(R.id.tvTime);
        tvTo = view.findViewById(R.id.tvTo);
        tvSubject = view.findViewById(R.id.tvSubject);
        tvCount = view.findViewById(R.id.tvCount);
        tvReplyTo = view.findViewById(R.id.tvReplyTo);
        tvCc = view.findViewById(R.id.tvCc);
        tvBcc = view.findViewById(R.id.tvBcc);
        rvAttachment = view.findViewById(R.id.rvAttachment);
        tvError = view.findViewById(R.id.tvError);
        vSeparatorBody = view.findViewById(R.id.vSeparatorBody);
        btnImages = view.findViewById(R.id.btnImages);
        tvBody = view.findViewById(R.id.tvBody);
        pbBody = view.findViewById(R.id.pbBody);
        fab = view.findViewById(R.id.fab);
        bottom_navigation = view.findViewById(R.id.bottom_navigation);
        pbWait = view.findViewById(R.id.pbWait);
        grpHeader = view.findViewById(R.id.grpHeader);
        grpAddresses = view.findViewById(R.id.grpAddresses);
        grpAttachments = view.findViewById(R.id.grpAttachments);
        grpError = view.findViewById(R.id.grpError);
        grpMessage = view.findViewById(R.id.grpMessage);

        setHasOptionsMenu(true);

        tvCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onMenuThread();
            }
        });

        tvBody.setMovementMethod(new LinkMovementMethod() {
            public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
                if (event.getAction() != MotionEvent.ACTION_UP)
                    return super.onTouchEvent(widget, buffer, event);

                int x = (int) event.getX();
                int y = (int) event.getY();

                x -= widget.getTotalPaddingLeft();
                y -= widget.getTotalPaddingTop();

                x += widget.getScrollX();
                y += widget.getScrollY();

                Layout layout = widget.getLayout();
                int line = layout.getLineForVertical(y);
                int off = layout.getOffsetForHorizontal(line, x);

                URLSpan[] link = buffer.getSpans(off, off, URLSpan.class);
                if (link.length != 0) {
                    String url = link[0].getURL();
                    Uri uri = Uri.parse(url);

                    if (!"http".equals(uri.getScheme()) && !"https".equals(uri.getScheme())) {
                        Toast.makeText(getContext(), getString(R.string.title_no_viewer, uri.toString()), Toast.LENGTH_LONG).show();
                        return true;
                    }

                    if (BuildConfig.APPLICATION_ID.equals(uri.getHost()) && "/activate/".equals(uri.getPath())) {
                        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
                        lbm.sendBroadcast(
                                new Intent(ActivityView.ACTION_ACTIVATE_PRO)
                                        .putExtra("uri", uri));

                    } else if (prefs.getBoolean("webview", false)) {
                        Bundle args = new Bundle();
                        args.putString("link", url);

                        FragmentWebView fragment = new FragmentWebView();
                        fragment.setArguments(args);

                        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("webview");
                        fragmentTransaction.commit();
                    } else {
                        // https://developer.chrome.com/multidevice/android/customtabs
                        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                        builder.setToolbarColor(Helper.resolveColor(getContext(), R.attr.colorPrimary));

                        CustomTabsIntent customTabsIntent = builder.build();
                        customTabsIntent.launchUrl(getContext(), Uri.parse(url));
                    }
                }

                return true;
            }
        });

        // Wire controls

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                free = true;
                getActivity().invalidateOptionsMenu();

                grpHeader.setVisibility(View.GONE);
                vSeparatorBody.setVisibility(View.GONE);
                fab.setVisibility(View.GONE);

                tvCount.setVisibility(View.GONE);
                grpAddresses.setVisibility(View.GONE);
                grpAttachments.setVisibility(View.GONE);
                grpError.setVisibility(View.GONE);

                tvCount.setTag(tvCount.getVisibility());
                tvCc.setTag(grpAddresses.getVisibility());
                tvError.setTag(grpError.getVisibility());
            }
        });

        ((ActivityBase) getActivity()).addBackPressedListener(new ActivityBase.IBackPressedListener() {
            @Override
            public boolean onBackPressed() {
                if (free && isVisible()) {
                    free = false;
                    getActivity().invalidateOptionsMenu();

                    grpHeader.setVisibility(View.VISIBLE);
                    vSeparatorBody.setVisibility(View.VISIBLE);
                    fab.setVisibility(View.VISIBLE);

                    RecyclerView.Adapter adapter = rvAttachment.getAdapter();

                    tvCount.setVisibility((int) tvCount.getTag());
                    grpAddresses.setVisibility((int) tvCc.getTag());
                    grpAttachments.setVisibility(adapter != null && adapter.getItemCount() > 0 ? View.VISIBLE : View.GONE);
                    grpError.setVisibility((int) tvError.getTag());

                    return true;
                }
                return false;
            }
        });

        bottom_navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_spam:
                        onActionSpam();
                        return true;
                    case R.id.action_trash:
                        onActionDelete();
                        return true;
                    case R.id.action_move:
                        onActionMove();
                        return true;
                    case R.id.action_archive:
                        onActionArchive();
                        return true;
                    case R.id.action_reply:
                        onActionReply();
                        return true;
                }
                return false;
            }
        });

        // Initialize
        grpHeader.setVisibility(View.GONE);
        grpAddresses.setVisibility(View.GONE);
        grpAttachments.setVisibility(View.GONE);
        btnImages.setVisibility(View.GONE);
        grpMessage.setVisibility(View.GONE);
        pbBody.setVisibility(View.GONE);
        bottom_navigation.setVisibility(View.GONE);
        tvCount.setVisibility(View.GONE);
        grpError.setVisibility(View.GONE);
        fab.setVisibility(View.GONE);
        pbWait.setVisibility(View.VISIBLE);

        tvSize.setText(null);

        rvAttachment.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rvAttachment.setLayoutManager(llm);

        adapter = new AdapterAttachment(getContext(), getViewLifecycleOwner(), true);
        rvAttachment.setAdapter(adapter);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("message", message);
        outState.putBoolean("free", free);
        if (free) {
            outState.putInt("tag_count", (int) tvCount.getTag());
            outState.putInt("tag_cc", (int) tvCc.getTag());
            outState.putInt("tag_error", (int) tvError.getTag());
        }
        outState.putString("decrypted", decrypted);
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState == null) {
            setSubtitle(Helper.localizeFolderName(getContext(), message.folderName));

            tvFrom.setText(MessageHelper.getFormattedAddresses(message.from, true));
            tvTime.setText(message.sent == null ? null : df.format(new Date(message.sent)));
            tvTo.setText(MessageHelper.getFormattedAddresses(message.to, true));
            tvSubject.setText(message.subject);

            tvCount.setText(Integer.toString(message.count));

            tvReplyTo.setText(MessageHelper.getFormattedAddresses(message.reply, true));
            tvCc.setText(MessageHelper.getFormattedAddresses(message.cc, true));
            tvBcc.setText(MessageHelper.getFormattedAddresses(message.bcc, true));

            tvError.setText(message.error);
        } else {
            free = savedInstanceState.getBoolean("free");
            if (free) {
                tvCount.setTag(savedInstanceState.getInt("tag_count"));
                tvCc.setTag(savedInstanceState.getInt("tag_cc"));
                rvAttachment.setTag(savedInstanceState.getInt("tag_attachment"));
                tvError.setTag(savedInstanceState.getInt("tag_error"));
            }
            decrypted = savedInstanceState.getString("decrypted");
        }

        if (tvBody.getTag() == null) {
            // Spanned text needs to be loaded after recreation too
            final Bundle args = new Bundle();
            args.putLong("id", message.id);
            args.putBoolean("has_images", false);
            args.putBoolean("show_images", false);

            pbBody.setVisibility(View.VISIBLE);
            final SimpleTask<Spanned> bodyTask = new SimpleTask<Spanned>() {
                @Override
                protected Spanned onLoad(final Context context, final Bundle args) throws Throwable {
                    final long id = args.getLong("id");
                    final boolean show_images = args.getBoolean("show_images");
                    String body = (decrypted == null ? message.read(context) : decrypted);
                    args.putInt("size", body.length());

                    return Html.fromHtml(HtmlHelper.sanitize(getContext(), body, false), new Html.ImageGetter() {
                        @Override
                        public Drawable getDrawable(String source) {
                            float scale = context.getResources().getDisplayMetrics().density;
                            int px = (int) (24 * scale + 0.5f);

                            if (show_images) {
                                // Get cache folder
                                File dir = new File(context.getCacheDir(), "images");
                                dir.mkdir();

                                // Cleanup cache
                                long now = new Date().getTime();
                                File[] images = dir.listFiles();
                                if (images != null)
                                    for (File image : images)
                                        if (image.isFile() && image.lastModified() + CACHE_IMAGE_DURATION < now) {
                                            Log.i(Helper.TAG, "Deleting from image cache " + image.getName());
                                            image.delete();
                                        }

                                // Create unique file name
                                File file = new File(dir, id + "_" + source.hashCode());

                                InputStream is = null;
                                FileOutputStream os = null;
                                try {
                                    // Get input stream
                                    if (file.exists()) {
                                        Log.i(Helper.TAG, "Using cached " + file);
                                        is = new FileInputStream(file);
                                    } else {
                                        Log.i(Helper.TAG, "Downloading " + source);
                                        is = new URL(source).openStream();
                                    }

                                    // Decode image from stream
                                    Bitmap bm = BitmapFactory.decodeStream(is);
                                    if (bm == null)
                                        throw new IllegalArgumentException();

                                    // Cache bitmap
                                    if (!file.exists()) {
                                        os = new FileOutputStream(file);
                                        bm.compress(Bitmap.CompressFormat.PNG, 100, os);
                                    }

                                    // Create drawable from bitmap
                                    Drawable d = new BitmapDrawable(context.getResources(), bm);
                                    d.setBounds(0, 0, bm.getWidth(), bm.getHeight());
                                    return d;
                                } catch (Throwable ex) {
                                    // Show warning icon
                                    Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                                    Drawable d = context.getResources().getDrawable(R.drawable.baseline_warning_24, context.getTheme());
                                    d.setBounds(0, 0, px, px);
                                    return d;
                                } finally {
                                    // Close streams
                                    if (is != null) {
                                        try {
                                            is.close();
                                        } catch (IOException e) {
                                            Log.w(Helper.TAG, e + "\n" + Log.getStackTraceString(e));
                                        }
                                    }
                                    if (os != null) {
                                        try {
                                            os.close();
                                        } catch (IOException e) {
                                            Log.w(Helper.TAG, e + "\n" + Log.getStackTraceString(e));
                                        }
                                    }
                                }
                            } else {
                                // Show placeholder icon
                                args.putBoolean("has_images", true);
                                Drawable d = context.getResources().getDrawable(R.drawable.baseline_image_24, context.getTheme());
                                d.setBounds(0, 0, px, px);
                                return d;
                            }
                        }
                    }, new Html.TagHandler() {
                        @Override
                        public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
                            Log.i(Helper.TAG, "HTML tag=" + tag + " opening=" + opening);
                        }
                    });
                }

                @Override
                protected void onLoaded(Bundle args, Spanned body) {
                    boolean has_images = args.getBoolean("has_images");
                    boolean show_images = args.getBoolean("show_images");
                    tvSize.setText(Helper.humanReadableByteCount(args.getInt("size"), false));
                    tvBody.setText(body);
                    tvBody.setTag(true);
                    btnImages.setVisibility(has_images && !show_images ? View.VISIBLE : View.GONE);
                    grpMessage.setVisibility(View.VISIBLE);
                    fab.setVisibility(free ? View.GONE : View.VISIBLE);
                    pbBody.setVisibility(View.GONE);
                }
            };

            bodyTask.load(FragmentMessage.this, args);

            btnImages.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.setEnabled(false);
                    args.putBoolean("show_images", true);
                    bodyTask.load(FragmentMessage.this, args);
                }
            });
        }

        setSeen();

        pbWait.setVisibility(View.GONE);

        grpHeader.setVisibility(free ? View.GONE : View.VISIBLE);
        vSeparatorBody.setVisibility(free ? View.GONE : View.VISIBLE);

        if (free) {
            tvCount.setVisibility((int) tvCount.getTag());
            grpAddresses.setVisibility((int) tvCc.getTag());
            grpError.setVisibility((int) tvError.getTag());
        } else {
            tvCount.setVisibility(!free && message.count > 1 ? View.VISIBLE : View.GONE);
            grpError.setVisibility(free || message.error == null ? View.GONE : View.VISIBLE);
        }

        DB db = DB.getInstance(getContext());

        if (!message.virtual) {
            // Observe message
            db.message().liveMessage(message.id).observe(getViewLifecycleOwner(), new Observer<TupleMessageEx>() {

                @Override
                public void onChanged(@Nullable final TupleMessageEx message) {
                    if (message == null || (!(debug && BuildConfig.DEBUG) && message.ui_hide)) {
                        // Message gone (moved, deleted)
                        finish();
                        return;
                    }

                    // Messages are immutable except for flags
                    FragmentMessage.this.message.seen = message.seen;
                    FragmentMessage.this.message.ui_seen = message.ui_seen;
                    setSeen();
                }
            });

            // Observe attachments
            db.attachment().liveAttachments(message.id).observe(getViewLifecycleOwner(),
                    new Observer<List<EntityAttachment>>() {
                        @Override
                        public void onChanged(@Nullable List<EntityAttachment> attachments) {
                            if (attachments == null)
                                attachments = new ArrayList<>();

                            adapter.set(attachments);
                            grpAttachments.setVisibility(!free && attachments.size() > 0 ? View.VISIBLE : View.GONE);
                        }
                    });

            // Observe folders
            db.folder().liveFolders(message.account).observe(getViewLifecycleOwner(), new Observer<List<TupleFolderEx>>() {
                @Override
                public void onChanged(@Nullable List<TupleFolderEx> folders) {
                    if (folders == null)
                        folders = new ArrayList<>();

                    boolean inInbox = EntityFolder.INBOX.equals(message.folderType);
                    boolean inOutbox = EntityFolder.OUTBOX.equals(message.folderType);
                    boolean inArchive = EntityFolder.ARCHIVE.equals(message.folderType);
                    boolean inTrash = EntityFolder.TRASH.equals(message.folderType);
                    boolean inJunk = EntityFolder.JUNK.equals(message.folderType);

                    boolean hasTrash = false;
                    boolean hasJunk = false;
                    boolean hasArchive = false;
                    boolean hasUser = false;
                    if (folders != null)
                        for (EntityFolder folder : folders) {
                            if (EntityFolder.TRASH.equals(folder.type))
                                hasTrash = true;
                            else if (EntityFolder.JUNK.equals(folder.type))
                                hasJunk = true;
                            else if (EntityFolder.ARCHIVE.equals(folder.type))
                                hasArchive = true;
                            else if (EntityFolder.USER.equals(folder.type))
                                hasUser = true;
                        }

                    bottom_navigation.setTag(inTrash || !hasTrash || inOutbox);

                    bottom_navigation.getMenu().findItem(R.id.action_spam).setVisible(message.uid != null && !inArchive && !inJunk && hasJunk);
                    bottom_navigation.getMenu().findItem(R.id.action_trash).setVisible((message.uid != null && hasTrash) || (inOutbox && !TextUtils.isEmpty(message.error)));
                    bottom_navigation.getMenu().findItem(R.id.action_move).setVisible(message.uid != null && (!inInbox || hasUser));
                    bottom_navigation.getMenu().findItem(R.id.action_archive).setVisible(message.uid != null && !inArchive && hasArchive);
                    bottom_navigation.getMenu().findItem(R.id.action_reply).setVisible(!inOutbox);
                    bottom_navigation.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private void setSeen() {
        int typeface = (message.ui_seen ? Typeface.NORMAL : Typeface.BOLD);
        tvFrom.setTypeface(null, typeface);
        tvTime.setTypeface(null, typeface);
        tvSubject.setTypeface(null, typeface);
        tvCount.setTypeface(null, typeface);

        int colorUnseen = Helper.resolveColor(getContext(), message.ui_seen
                ? android.R.attr.textColorSecondary : R.attr.colorUnread);
        tvFrom.setTextColor(colorUnseen);
        tvTime.setTextColor(colorUnseen);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_view, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        boolean inOutbox = EntityFolder.OUTBOX.equals(message.folderType);

        menu.findItem(R.id.menu_addresses).setVisible(!free);
        menu.findItem(R.id.menu_thread).setVisible(!free && !message.virtual && message.count > 1);
        menu.findItem(R.id.menu_seen).setVisible(!free && !message.virtual && !inOutbox);
        menu.findItem(R.id.menu_forward).setVisible(!free && !message.virtual && !inOutbox);
        menu.findItem(R.id.menu_reply_all).setVisible(!free && !message.virtual && message.cc != null && !inOutbox);
        menu.findItem(R.id.menu_decrypt).setVisible(decrypted == null);

        MenuItem menuSeen = menu.findItem(R.id.menu_seen);
        menuSeen.setIcon(message.ui_seen
                ? R.drawable.baseline_visibility_off_24
                : R.drawable.baseline_visibility_24);
        menuSeen.setTitle(message.ui_seen ? R.string.title_unseen : R.string.title_seen);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_addresses:
                onMenuAddresses();
                return true;
            case R.id.menu_thread:
                onMenuThread();
                return true;
            case R.id.menu_seen:
                onMenuSeen();
                return true;
            case R.id.menu_forward:
                onMenuForward();
                return true;
            case R.id.menu_reply_all:
                onMenuReplyAll();
                return true;
            case R.id.menu_answer:
                onMenuAnswer();
                return true;
            case R.id.menu_decrypt:
                onMenuDecrypt();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onMenuAddresses() {
        grpAddresses.setVisibility(grpAddresses.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
    }

    private void onMenuThread() {
        getFragmentManager().popBackStack("thread", FragmentManager.POP_BACK_STACK_INCLUSIVE);

        Bundle args = new Bundle();
        args.putLong("thread", message.id);

        FragmentMessages fragment = new FragmentMessages();
        fragment.setArguments(args);

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("thread");
        fragmentTransaction.commit();
    }

    private void onMenuSeen() {
        Helper.setViewsEnabled(view, false);

        Bundle args = new Bundle();
        args.putLong("id", message.id);

        new SimpleTask<Void>() {
            @Override
            protected Void onLoad(Context context, Bundle args) {
                long id = args.getLong("id");
                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    EntityMessage message = db.message().getMessage(id);
                    for (EntityMessage tmessage : db.message().getMessageByThread(message.account, message.thread)) {
                        db.message().setMessageUiSeen(tmessage.id, !message.ui_seen);
                        EntityOperation.queue(db, tmessage, EntityOperation.SEEN, !tmessage.ui_seen);
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                EntityOperation.process(context);

                return null;
            }

            @Override
            protected void onLoaded(Bundle args, Void data) {
                Helper.setViewsEnabled(view, true);
            }

            @Override
            public void onException(Bundle args, Throwable ex) {
                Helper.setViewsEnabled(view, true);
                Toast.makeText(getContext(), ex.toString(), Toast.LENGTH_LONG).show();
            }
        }.load(this, args);
    }

    private void onMenuForward() {
        startActivity(new Intent(getContext(), ActivityCompose.class)
                .putExtra("action", "forward")
                .putExtra("reference", message.id));
    }

    private void onMenuReplyAll() {
        startActivity(new Intent(getContext(), ActivityCompose.class)
                .putExtra("action", "reply_all")
                .putExtra("reference", message.id));
    }

    private void onMenuAnswer() {
        DB.getInstance(getContext()).answer().liveAnswers().observe(getViewLifecycleOwner(), new Observer<List<EntityAnswer>>() {
            @Override
            public void onChanged(List<EntityAnswer> answers) {
                final Collator collator = Collator.getInstance(Locale.getDefault());
                collator.setStrength(Collator.SECONDARY); // Case insensitive, process accents etc

                Collections.sort(answers, new Comparator<EntityAnswer>() {
                    @Override
                    public int compare(EntityAnswer a1, EntityAnswer a2) {
                        return collator.compare(a1.name, a2.name);
                    }
                });

                PopupMenu popupMenu = new PopupMenu(getContext(), vwAnswerAnchor);

                int order = 0;
                for (EntityAnswer answer : answers)
                    popupMenu.getMenu().add(Menu.NONE, answer.id.intValue(), order++,
                            Helper.localizeFolderName(getContext(), answer.name));

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem target) {
                        if (PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("pro", false)) {
                            startActivity(new Intent(getContext(), ActivityCompose.class)
                                    .putExtra("action", "reply")
                                    .putExtra("reference", message.id)
                                    .putExtra("answer", (long) target.getItemId()));
                        } else {
                            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.content_frame, new FragmentPro()).addToBackStack("pro");
                            fragmentTransaction.commit();
                        }
                        return true;
                    }
                });

                popupMenu.show();
            }
        });
    }

    private void onMenuDecrypt() {
        Log.i(Helper.TAG, "On decrypt");

        if (!PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("pro", false)) {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content_frame, new FragmentPro()).addToBackStack("pro");
            fragmentTransaction.commit();
            return;
        }

        try {
            if (openPgpConnection == null || !openPgpConnection.isBound())
                throw new IllegalArgumentException(getString(R.string.title_no_openpgp));

            if (message.to == null || message.to.length == 0)
                throw new IllegalArgumentException(getString(R.string.title_to_missing));

            InternetAddress to = (InternetAddress) message.to[0];

            Intent data = new Intent();
            data.setAction(OpenPgpApi.ACTION_DECRYPT_VERIFY);
            data.putExtra(OpenPgpApi.EXTRA_USER_IDS, new String[]{to.getAddress()});

            String encrypted = message.read(getContext());
            final InputStream is = new ByteArrayInputStream(encrypted.getBytes("UTF-8"));
            final ByteArrayOutputStream os = new ByteArrayOutputStream();

            OpenPgpApi api = new OpenPgpApi(getContext(), openPgpConnection.getService());
            api.executeApiAsync(data, is, os, new OpenPgpApi.IOpenPgpCallback() {
                @Override
                public void onReturn(Intent result) {
                    try {
                        int code = result.getIntExtra(OpenPgpApi.RESULT_CODE, OpenPgpApi.RESULT_CODE_ERROR);
                        switch (code) {
                            case OpenPgpApi.RESULT_CODE_SUCCESS: {
                                Log.i(Helper.TAG, "Decrypted");
                                FragmentMessage.this.decrypted = os.toString("UTF-8");
                                getActivity().invalidateOptionsMenu();
                                tvBody.setText(decrypted);
                                break;
                            }
                            case OpenPgpApi.RESULT_CODE_USER_INTERACTION_REQUIRED: {
                                Log.i(Helper.TAG, "User interaction");
                                PendingIntent pi = result.getParcelableExtra(OpenPgpApi.RESULT_INTENT);
                                startIntentSenderForResult(
                                        pi.getIntentSender(),
                                        ActivityView.REQUEST_OPENPGP,
                                        null, 0, 0, 0,
                                        new Bundle());
                                break;
                            }
                            case OpenPgpApi.RESULT_CODE_ERROR: {
                                OpenPgpError error = result.getParcelableExtra(OpenPgpApi.RESULT_ERROR);
                                throw new IllegalArgumentException(error.getMessage());
                            }
                        }
                    } catch (Throwable ex) {
                        Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                        if (ex instanceof IllegalArgumentException)
                            Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG).show();
                        else
                            Toast.makeText(getContext(), ex.toString(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        } catch (Throwable ex) {
            Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
            if (ex instanceof IllegalArgumentException)
                Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG).show();
            else
                Toast.makeText(getContext(), ex.toString(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(Helper.TAG, "Message onActivityResult request=" + requestCode + " result=" + resultCode + " data=" + data);
        if (resultCode == RESULT_OK) {
            if (requestCode == ActivityView.REQUEST_OPENPGP) {
                Log.i(Helper.TAG, "User interacted");
                onMenuDecrypt();
            }
        }
    }

    private void onActionSpam() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder
                .setMessage(R.string.title_ask_spam)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Helper.setViewsEnabled(view, false);

                        Bundle args = new Bundle();
                        args.putLong("id", message.id);

                        new SimpleTask<Void>() {
                            @Override
                            protected Void onLoad(Context context, Bundle args) {
                                long id = args.getLong("id");

                                DB db = DB.getInstance(context);
                                try {
                                    db.beginTransaction();

                                    db.message().setMessageUiHide(id, true);

                                    EntityMessage message = db.message().getMessage(id);
                                    EntityFolder spam = db.folder().getFolderByType(message.account, EntityFolder.JUNK);
                                    EntityOperation.queue(db, message, EntityOperation.MOVE, spam.id);

                                    db.setTransactionSuccessful();
                                } finally {
                                    db.endTransaction();
                                }

                                EntityOperation.process(context);

                                return null;
                            }

                            @Override
                            protected void onLoaded(Bundle args, Void result) {
                                Helper.setViewsEnabled(view, true);
                            }

                            @Override
                            protected void onException(Bundle args, Throwable ex) {
                                Helper.setViewsEnabled(view, true);
                                Toast.makeText(getContext(), ex.toString(), Toast.LENGTH_LONG).show();
                            }
                        }.load(FragmentMessage.this, args);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null).show();
    }

    private void onActionDelete() {
        boolean delete = (Boolean) bottom_navigation.getTag();
        if (delete) {
            // No trash or is trash
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder
                    .setMessage(R.string.title_ask_delete)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Helper.setViewsEnabled(view, false);

                            Bundle args = new Bundle();
                            args.putLong("id", message.id);

                            new SimpleTask<Void>() {
                                @Override
                                protected Void onLoad(Context context, Bundle args) {
                                    long id = args.getLong("id");

                                    DB db = DB.getInstance(context);
                                    try {
                                        db.beginTransaction();

                                        EntityMessage message = db.message().getMessage(id);
                                        if (message.uid == null && !TextUtils.isEmpty(message.error)) // outbox
                                            db.message().deleteMessage(id);
                                        else {
                                            db.message().setMessageUiHide(id, true);
                                            EntityOperation.queue(db, message, EntityOperation.DELETE);
                                        }

                                        db.setTransactionSuccessful();
                                    } finally {
                                        db.endTransaction();
                                    }

                                    EntityOperation.process(context);

                                    return null;
                                }

                                @Override
                                protected void onLoaded(Bundle args, Void result) {
                                    Helper.setViewsEnabled(view, true);
                                }

                                @Override
                                protected void onException(Bundle args, Throwable ex) {
                                    Helper.setViewsEnabled(view, true);
                                    Toast.makeText(getContext(), ex.toString(), Toast.LENGTH_LONG).show();
                                }
                            }.load(FragmentMessage.this, args);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null).show();
        } else {
            Helper.setViewsEnabled(view, false);

            Bundle args = new Bundle();
            args.putLong("id", message.id);

            new SimpleTask<Void>() {
                @Override
                protected Void onLoad(Context context, Bundle args) {
                    long id = args.getLong("id");
                    DB db = DB.getInstance(context);
                    try {
                        db.beginTransaction();

                        db.message().setMessageUiHide(id, true);

                        EntityMessage message = db.message().getMessage(id);
                        EntityFolder trash = db.folder().getFolderByType(message.account, EntityFolder.TRASH);
                        EntityOperation.queue(db, message, EntityOperation.MOVE, trash.id);

                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }

                    EntityOperation.process(context);

                    return null;
                }

                @Override
                protected void onLoaded(Bundle args, Void result) {
                    Helper.setViewsEnabled(view, true);
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Helper.setViewsEnabled(view, true);
                    Toast.makeText(getContext(), ex.toString(), Toast.LENGTH_LONG).show();
                }
            }.load(FragmentMessage.this, args);
        }
    }

    private void onActionMove() {
        Bundle args = new Bundle();
        args.putLong("id", message.id);

        new SimpleTask<List<EntityFolder>>() {
            @Override
            protected List<EntityFolder> onLoad(Context context, Bundle args) {
                EntityMessage message;
                List<EntityFolder> folders;

                DB db = DB.getInstance(getContext());
                try {
                    db.beginTransaction();

                    message = db.message().getMessage(args.getLong("id"));
                    folders = db.folder().getUserFolders(message.account);

                    for (int i = 0; i < folders.size(); i++)
                        if (folders.get(i).id.equals(message.folder)) {
                            folders.remove(i);
                            break;
                        }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                final Collator collator = Collator.getInstance(Locale.getDefault());
                collator.setStrength(Collator.SECONDARY); // Case insensitive, process accents etc

                Collections.sort(folders, new Comparator<EntityFolder>() {
                    @Override
                    public int compare(EntityFolder f1, EntityFolder f2) {
                        return collator.compare(f1.name, f2.name);
                    }
                });

                EntityFolder inbox = db.folder().getFolderByType(message.account, EntityFolder.INBOX);
                if (!message.folder.equals(inbox.id))
                    folders.add(0, inbox);

                return folders;
            }

            @Override
            protected void onLoaded(final Bundle args, List<EntityFolder> folders) {
                View anchor = bottom_navigation.findViewById(R.id.action_move);
                PopupMenu popupMenu = new PopupMenu(getContext(), anchor);

                int order = 0;
                for (EntityFolder folder : folders)
                    popupMenu.getMenu().add(Menu.NONE, folder.id.intValue(), order++,
                            Helper.localizeFolderName(getContext(), folder.name));

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(final MenuItem target) {
                        Helper.setViewsEnabled(view, false);

                        args.putLong("target", target.getItemId());

                        new SimpleTask<Boolean>() {
                            @Override
                            protected Boolean onLoad(Context context, Bundle args) {
                                long id = args.getLong("id");
                                long target = args.getLong("target");

                                boolean close;

                                DB db = DB.getInstance(context);
                                try {
                                    db.beginTransaction();

                                    EntityMessage message = db.message().getMessage(id);
                                    EntityFolder folder = db.folder().getFolder(message.folder);

                                    close = EntityFolder.ARCHIVE.equals(folder.type);
                                    if (!close)
                                        db.message().setMessageUiHide(message.id, true);

                                    EntityOperation.queue(db, message, EntityOperation.MOVE, target);

                                    db.setTransactionSuccessful();
                                } finally {
                                    db.endTransaction();
                                }

                                EntityOperation.process(context);

                                return close;
                            }

                            @Override
                            protected void onLoaded(Bundle args, Boolean close) {
                                Helper.setViewsEnabled(view, true);
                                if (close) // archived message
                                    getFragmentManager().popBackStack();
                            }

                            @Override
                            protected void onException(Bundle args, Throwable ex) {
                                Helper.setViewsEnabled(view, true);
                                Toast.makeText(getContext(), ex.toString(), Toast.LENGTH_LONG).show();
                            }
                        }.load(FragmentMessage.this, args);

                        return true;
                    }
                });

                popupMenu.show();
            }
        }.load(FragmentMessage.this, args);
    }

    private void onActionArchive() {
        Helper.setViewsEnabled(view, false);

        Bundle args = new Bundle();
        args.putLong("id", message.id);

        new SimpleTask<Void>() {
            @Override
            protected Void onLoad(Context context, Bundle args) {
                long id = args.getLong("id");

                DB db = DB.getInstance(context);
                try {
                    db.beginTransaction();

                    db.message().setMessageUiHide(id, true);

                    EntityMessage message = db.message().getMessage(id);
                    EntityFolder archive = db.folder().getFolderByType(message.account, EntityFolder.ARCHIVE);
                    EntityOperation.queue(db, message, EntityOperation.MOVE, archive.id);

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                EntityOperation.process(context);

                return null;
            }

            @Override
            protected void onLoaded(Bundle args, Void result) {
                getFragmentManager().popBackStack();
                Helper.setViewsEnabled(view, true);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.setViewsEnabled(view, true);
                Toast.makeText(getContext(), ex.toString(), Toast.LENGTH_LONG).show();
            }
        }.load(FragmentMessage.this, args);
    }

    private void onActionReply() {
        startActivity(new Intent(getContext(), ActivityCompose.class)
                .putExtra("action", "reply")
                .putExtra("reference", message.id));
    }
}
