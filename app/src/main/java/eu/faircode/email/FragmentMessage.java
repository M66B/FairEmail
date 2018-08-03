package eu.faircode.email;

/*
    This file is part of Safe email.

    Safe email is free software: you can redistribute it and/or modify
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

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.Group;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Layout;
import android.text.Spannable;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FragmentMessage extends Fragment {
    private TextView tvTime;
    private TextView tvFrom;
    private TextView tvTo;
    private TextView tvCc;
    private TextView tvBcc;
    private RecyclerView rvAttachment;
    private TextView tvSubject;
    private TextView tvCount;
    private BottomNavigationView top_navigation;
    private TextView tvBody;
    private BottomNavigationView bottom_navigation;
    private ProgressBar pbWait;
    private Group grpAddress;
    private Group grpAttachments;
    private Group grpReady;

    private AdapterAttachment adapter;
    private LiveData<TupleFolderEx> liveFolder;

    private ExecutorService executor = Executors.newCachedThreadPool();
    private DateFormat df = SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message, container, false);

        // Get arguments
        Bundle args = getArguments();
        final long folder = args.getLong("folder");
        final long id = args.getLong("id");

        // Get controls
        tvFrom = view.findViewById(R.id.tvFrom);
        tvTo = view.findViewById(R.id.tvTo);
        tvCc = view.findViewById(R.id.tvCc);
        tvBcc = view.findViewById(R.id.tvBcc);
        rvAttachment = view.findViewById(R.id.rvAttachment);
        tvTime = view.findViewById(R.id.tvTime);
        tvSubject = view.findViewById(R.id.tvSubject);
        tvCount = view.findViewById(R.id.tvCount);
        top_navigation = view.findViewById(R.id.top_navigation);
        tvBody = view.findViewById(R.id.tvBody);
        bottom_navigation = view.findViewById(R.id.bottom_navigation);
        pbWait = view.findViewById(R.id.pbWait);
        grpAddress = view.findViewById(R.id.grpAddress);
        grpAttachments = view.findViewById(R.id.grpAttachments);
        grpReady = view.findViewById(R.id.grpReady);

        setHasOptionsMenu(true);

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
                    Bundle args = new Bundle();
                    args.putString("link", link[0].getURL());

                    FragmentWebView fragment = new FragmentWebView();
                    fragment.setArguments(args);

                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("webview");
                    fragmentTransaction.commit();
                }
                return true;
            }
        });

        // Wire controls

        top_navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_seen:
                        onActionSeen(id);
                        return true;
                    case R.id.action_thread:
                        onActionThread(id);
                        return true;
                    case R.id.action_move:
                        onActionMove(id);
                        return true;
                    case R.id.action_forward:
                        onActionForward(id);
                        return true;
                    case R.id.action_reply_all:
                        onActionReplyAll(id);
                        return true;
                }
                return false;
            }
        });

        bottom_navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_delete:
                        onActionDelete(id);
                        return true;
                    case R.id.action_spam:
                        onActionSpam(id);
                        return true;
                    case R.id.action_archive:
                        onActionArchive(id);
                        return true;
                    case R.id.action_reply:
                        onActionReply(id);
                        return true;
                }
                return false;
            }
        });

        // Initialize
        grpAddress.setVisibility(View.GONE);
        grpAttachments.setVisibility(View.GONE);
        grpReady.setVisibility(View.GONE);
        pbWait.setVisibility(View.VISIBLE);

        rvAttachment.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rvAttachment.setLayoutManager(llm);

        adapter = new AdapterAttachment(getContext());
        rvAttachment.setAdapter(adapter);

        final DB db = DB.getInstance(getContext());

        // Observe folder
        liveFolder = db.folder().liveFolderEx(folder);

        // Observe message
        db.message().liveMessage(id).observe(this, new Observer<TupleMessageEx>() {
            @Override
            public void onChanged(@Nullable TupleMessageEx message) {
                pbWait.setVisibility(View.GONE);
                grpReady.setVisibility(View.VISIBLE);

                if (message == null || message.ui_hide) {
                    // Message gone (moved, deleted)
                    if (FragmentMessage.this.isVisible())
                        getFragmentManager().popBackStack();
                } else {
                    tvFrom.setText(MessageHelper.getFormattedAddresses(message.from));
                    tvTo.setText(MessageHelper.getFormattedAddresses(message.to));
                    tvCc.setText(MessageHelper.getFormattedAddresses(message.cc));
                    tvBcc.setText(MessageHelper.getFormattedAddresses(message.bcc));
                    tvTime.setText(message.sent == null ? null : df.format(new Date(message.sent)));
                    tvSubject.setText(message.subject);
                    tvCount.setText(Integer.toString(message.count));

                    int visibility = (message.ui_seen ? Typeface.NORMAL : Typeface.BOLD);
                    tvFrom.setTypeface(null, visibility);
                    tvTime.setTypeface(null, visibility);
                    tvSubject.setTypeface(null, visibility);
                    tvCount.setTypeface(null, visibility);

                    // Observe attachments
                    db.attachment().liveAttachments(id).removeObservers(FragmentMessage.this);
                    db.attachment().liveAttachments(id).observe(FragmentMessage.this, attachmentsObserver);

                    top_navigation.getMenu().findItem(R.id.action_thread).setVisible(message.count > 1);

                    MenuItem actionSeen = top_navigation.getMenu().findItem(R.id.action_seen);
                    actionSeen.setIcon(message.ui_seen
                            ? R.drawable.baseline_visibility_off_24
                            : R.drawable.baseline_visibility_24);
                    actionSeen.setTitle(message.ui_seen ? R.string.title_unseen : R.string.title_seen);

                    bottom_navigation.getMenu().findItem(R.id.action_spam).setVisible(message.account != null);
                    bottom_navigation.getMenu().findItem(R.id.action_archive).setVisible(message.account != null);
                    tvBody.setText(message.body == null
                            ? null
                            : Html.fromHtml(HtmlHelper.sanitize(getContext(), message.body, false)));
                }
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        liveFolder.observe(this, folderObserver);
    }

    @Override
    public void onPause() {
        super.onPause();
        liveFolder.removeObservers(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_cc, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_cc:
                onMenuCc();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onMenuCc() {
        if (grpReady.getVisibility() == View.VISIBLE)
            grpAddress.setVisibility(grpAddress.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
    }

    Observer<TupleFolderEx> folderObserver = new Observer<TupleFolderEx>() {
        @Override
        public void onChanged(@Nullable TupleFolderEx folder) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(folder == null
                    ? null
                    : Helper.localizeFolderName(getContext(), folder));
        }
    };

    Observer<List<EntityAttachment>> attachmentsObserver = new Observer<List<EntityAttachment>>() {
        @Override
        public void onChanged(@Nullable List<EntityAttachment> attachments) {
            adapter.set(attachments);
            grpAttachments.setVisibility(attachments.size() > 0 ? View.VISIBLE : View.GONE);
        }
    };

    private void onActionSeen(final long id) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    DB db = DB.getInstance(getContext());
                    EntityMessage message = db.message().getMessage(id);
                    message.ui_seen = !message.ui_seen;
                    db.message().updateMessage(message);
                    EntityOperation.queue(getContext(), message, EntityOperation.SEEN, message.ui_seen);
                } catch (Throwable ex) {
                    Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                }
            }
        });
    }

    private void onActionThread(long id) {
        Bundle args = new Bundle();
        args.putLong("thread", id); // message ID

        FragmentMessages fragment = new FragmentMessages();
        fragment.setArguments(args);

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("thread");
        fragmentTransaction.commit();
    }

    private void onActionMove(final long id) {
        Toast.makeText(getContext(), "Not implemented yet", Toast.LENGTH_LONG).show();
    }

    private void onActionForward(long id) {
        startActivity(new Intent(getContext(), ActivityCompose.class)
                .putExtra("id", id)
                .putExtra("action", "forward"));
    }

    private void onActionReplyAll(long id) {
        startActivity(new Intent(getContext(), ActivityCompose.class)
                .putExtra("id", id)
                .putExtra("action", "reply_all"));
    }

    private void onActionDelete(final long id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder
                .setMessage(R.string.title_ask_delete)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        executor.submit(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    DB db = DB.getInstance(getContext());
                                    EntityMessage message = db.message().getMessage(id);
                                    message.ui_hide = true;
                                    db.message().updateMessage(message);

                                    EntityOperation.queue(getContext(), message, EntityOperation.DELETE);
                                } catch (Throwable ex) {
                                    Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                                }
                            }
                        });
                    }
                })
                .setNegativeButton(android.R.string.cancel, null).show();
    }

    private void onActionSpam(final long id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder
                .setMessage(R.string.title_ask_spam)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        executor.submit(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    DB db = DB.getInstance(getContext());
                                    EntityMessage message = db.message().getMessage(id);
                                    EntityFolder spam = db.folder().getSpamFolder(message.account);
                                    if (spam == null) {
                                        Toast.makeText(getContext(), R.string.title_no_spam, Toast.LENGTH_LONG).show();
                                        return;
                                    }

                                    message.ui_hide = true;
                                    db.message().updateMessage(message);

                                    EntityOperation.queue(getContext(), message, EntityOperation.MOVE, spam.id);
                                } catch (Throwable ex) {
                                    Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                                }
                            }
                        });
                    }
                })
                .setNegativeButton(android.R.string.cancel, null).show();
    }

    private void onActionArchive(final long id) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    DB db = DB.getInstance(getContext());
                    EntityMessage message = db.message().getMessage(id);
                    EntityFolder archive = db.folder().getArchiveFolder(message.account);
                    if (archive == null) {
                        Toast.makeText(getContext(), R.string.title_no_archive, Toast.LENGTH_LONG).show();
                        return;
                    }

                    message.ui_hide = true;
                    db.message().updateMessage(message);

                    EntityOperation.queue(getContext(), message, EntityOperation.MOVE, archive.id);
                } catch (Throwable ex) {
                    Log.e(Helper.TAG, ex + "\n" + Log.getStackTraceString(ex));
                }
            }
        });
    }

    private void onActionReply(long id) {
        startActivity(new Intent(getContext(), ActivityCompose.class)
                .putExtra("id", id)
                .putExtra("action", "reply"));
    }
}
