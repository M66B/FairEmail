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

    Copyright 2018-2021 by Marcel Bokhorst (M66B)
*/

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.ArrowKeyMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.Group;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.sun.mail.imap.IMAPFolder;

import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeMessage;

public class ActivityEML extends ActivityBase {
    private TextView tvFrom;
    private TextView tvTo;
    private TextView tvReplyTo;
    private TextView tvCc;
    private TextView tvBcc;
    private TextView tvSent;
    private TextView tvReceived;
    private TextView tvSubject;
    private View vSeparatorAttachments;
    private RecyclerView rvAttachment;
    private TextView tvBody;
    private TextView tvStructure;
    private ImageButton ibEml;
    private ContentLoadingProgressBar pbWait;
    private Group grpReady;

    private Uri uri;
    private MessageHelper.AttachmentPart apart;
    private static final int REQUEST_ATTACHMENT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setSubtitle("EML");

        View view = LayoutInflater.from(this).inflate(R.layout.activity_eml, null);
        setContentView(view);

        tvFrom = findViewById(R.id.tvFrom);
        tvTo = findViewById(R.id.tvTo);
        tvReplyTo = findViewById(R.id.tvReplyTo);
        tvCc = findViewById(R.id.tvCc);
        tvBcc = findViewById(R.id.tvBcc);
        tvSent = findViewById(R.id.tvSent);
        tvReceived = findViewById(R.id.tvReceived);
        tvSubject = findViewById(R.id.tvSubject);
        vSeparatorAttachments = findViewById(R.id.vSeparatorAttachments);
        rvAttachment = findViewById(R.id.rvAttachment);
        tvBody = findViewById(R.id.tvBody);
        tvStructure = findViewById(R.id.tvStructure);
        ibEml = findViewById(R.id.ibEml);
        pbWait = findViewById(R.id.pbWait);
        grpReady = findViewById(R.id.grpReady);

        rvAttachment.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rvAttachment.setLayoutManager(llm);

        tvBody.setMovementMethod(new ArrowKeyMovementMethod() {
            @Override
            public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    int off = Helper.getOffset(widget, buffer, event);
                    URLSpan[] link = buffer.getSpans(off, off, URLSpan.class);
                    if (link.length > 0) {
                        String url = link[0].getURL();
                        Uri uri = Uri.parse(url);
                        if (uri.getScheme() == null)
                            uri = Uri.parse("https://" + url);

                        int start = buffer.getSpanStart(link[0]);
                        int end = buffer.getSpanEnd(link[0]);
                        String title = (start < 0 || end < 0 || end <= start
                                ? null : buffer.subSequence(start, end).toString());
                        if (url.equals(title))
                            title = null;

                        Bundle args = new Bundle();
                        args.putParcelable("uri", uri);
                        args.putString("title", title);

                        FragmentDialogOpenLink fragment = new FragmentDialogOpenLink();
                        fragment.setArguments(args);
                        fragment.show(getSupportFragmentManager(), "open:link");

                        return true;
                    }
                }

                return super.onTouchEvent(widget, buffer, event);
            }
        });

        ibEml.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putParcelable("uri", getIntent().getData());
                new SimpleTask<File>() {
                    @Override
                    protected File onExecute(Context context, Bundle args) throws Throwable {
                        File dir = new File(getCacheDir(), "shared");
                        if (!dir.exists())
                            dir.mkdir();

                        File file = new File(dir, "email.eml");

                        Uri uri = args.getParcelable("uri");
                        Helper.copy(context, uri, file);
                        return file;
                    }

                    @Override
                    protected void onExecuted(Bundle args, File file) {
                        Helper.share(ActivityEML.this, file, "text/plain", file.getName());
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Log.unexpectedError(getSupportFragmentManager(), ex);
                    }
                }.execute(ActivityEML.this, args, "eml:share");
            }
        });

        // Initialize
        FragmentDialogTheme.setBackground(this, view, false);
        vSeparatorAttachments.setVisibility(View.GONE);
        grpReady.setVisibility(View.GONE);

        load();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        load();
    }

    private void load() {
        uri = getIntent().getData();
        Log.i("EML uri=" + uri);

        Bundle args = new Bundle();
        args.putParcelable("uri", uri);

        new SimpleTask<Result>() {
            @Override
            protected void onPreExecute(Bundle args) {
                pbWait.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onPostExecute(Bundle args) {
                pbWait.setVisibility(View.GONE);
            }

            @Override
            protected Result onExecute(Context context, Bundle args) throws Throwable {
                Uri uri = args.getParcelable("uri");

                if (uri == null)
                    throw new FileNotFoundException();

                if (!"content".equals(uri.getScheme()) &&
                        !Helper.hasPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Log.w("EML uri=" + uri);
                    throw new IllegalArgumentException(context.getString(R.string.title_no_stream));
                }

                Result result = new Result();

                ContentResolver resolver = context.getContentResolver();
                try (InputStream is = resolver.openInputStream(uri)) {

                    Properties props = MessageHelper.getSessionProperties();
                    Session isession = Session.getInstance(props, null);
                    MimeMessage imessage = new MimeMessage(isession, is);

                    MessageHelper helper = new MessageHelper(imessage, context);

                    result.from = MessageHelper.formatAddresses(helper.getFrom());
                    result.to = MessageHelper.formatAddresses(helper.getTo());
                    result.replyTo = MessageHelper.formatAddresses(helper.getReply());
                    result.cc = MessageHelper.formatAddresses(helper.getCc());
                    result.bcc = MessageHelper.formatAddresses(helper.getBcc());
                    result.sent = helper.getSent();
                    result.received = helper.getReceived();
                    result.subject = helper.getSubject();
                    result.parts = helper.getMessageParts();

                    String html = result.parts.getHtml(context);
                    if (html != null) {
                        Document parsed = JsoupEx.parse(html);
                        Document document = HtmlHelper.sanitizeView(context, parsed, false);
                        result.body = HtmlHelper.fromDocument(context, document, null, null);
                    }

                    int textColorLink = Helper.resolveColor(context, android.R.attr.textColorLink);
                    SpannableStringBuilder ssb = new SpannableStringBuilder();
                    getStructure(imessage, ssb, 0, textColorLink);
                    result.structure = ssb;

                    return result;
                }
            }

            @Override
            protected void onExecuted(Bundle args, Result result) {
                DateFormat DTF = Helper.getDateTimeInstance(ActivityEML.this);

                tvFrom.setText(result.from);
                tvTo.setText(result.to);
                tvReplyTo.setText(result.replyTo);
                tvCc.setText(result.cc);
                tvBcc.setText(result.bcc);
                tvSent.setText(result.sent == null ? null : DTF.format(result.sent));
                tvReceived.setText(result.received == null ? null : DTF.format(result.received));
                tvSubject.setText(result.subject);

                vSeparatorAttachments.setVisibility(result.parts.getAttachmentParts().size() > 0 ? View.VISIBLE : View.GONE);

                AdapterAttachmentEML adapter = new AdapterAttachmentEML(
                        ActivityEML.this,
                        result.parts.getAttachmentParts(),
                        new AdapterAttachmentEML.IEML() {
                            @Override
                            public void onShare(MessageHelper.AttachmentPart apart) {
                                new SimpleTask<File>() {
                                    @Override
                                    protected File onExecute(Context context, Bundle args) throws Throwable {
                                        apart.attachment.id = 0L;
                                        File file = apart.attachment.getFile(context);
                                        Log.i("Writing to " + file);

                                        try (InputStream is = apart.part.getInputStream()) {
                                            try (OutputStream os = new FileOutputStream(file)) {
                                                byte[] buffer = new byte[Helper.BUFFER_SIZE];
                                                for (int len = is.read(buffer); len != -1; len = is.read(buffer))
                                                    os.write(buffer, 0, len);
                                            }
                                        }

                                        return file;
                                    }

                                    @Override
                                    protected void onExecuted(Bundle args, File file) {
                                        Helper.share(ActivityEML.this, file,
                                                apart.attachment.getMimeType(),
                                                apart.attachment.name);
                                    }

                                    @Override
                                    protected void onException(Bundle args, Throwable ex) {
                                        Log.unexpectedError(getSupportFragmentManager(), ex);
                                    }
                                }.execute(ActivityEML.this, new Bundle(), "eml:share");
                            }

                            @Override
                            public void onSave(MessageHelper.AttachmentPart apart) {
                                ActivityEML.this.apart = apart;

                                Intent create = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                                create.addCategory(Intent.CATEGORY_OPENABLE);
                                create.setType(apart.attachment.getMimeType());
                                if (!TextUtils.isEmpty(apart.attachment.name))
                                    create.putExtra(Intent.EXTRA_TITLE, apart.attachment.name);
                                Helper.openAdvanced(create);
                                if (create.resolveActivity(getPackageManager()) == null) // system whitelisted
                                    ToastEx.makeText(ActivityEML.this, R.string.title_no_saf, Toast.LENGTH_LONG).show();
                                else
                                    startActivityForResult(Helper.getChooser(ActivityEML.this, create), REQUEST_ATTACHMENT);
                            }
                        });
                rvAttachment.setAdapter(adapter);

                tvBody.setText(result.body);
                tvStructure.setText(result.structure);
                grpReady.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onException(Bundle args, @NonNull Throwable ex) {
                if (ex instanceof IllegalArgumentException)
                    Snackbar.make(findViewById(android.R.id.content), ex.getMessage(), Snackbar.LENGTH_LONG)
                            .setGestureInsetBottomIgnored(true).show();
                else
                    Log.unexpectedError(getSupportFragmentManager(), ex, false);
            }

            private void getStructure(Part part, SpannableStringBuilder ssb, int level, int textColorLink) {
                try {
                    Enumeration<Header> headers;
                    if (level == 0) {
                        List<Header> h = new ArrayList<>();

                        String[] cte = part.getHeader("Content-Transfer-Encoding");
                        if (cte != null)
                            for (String header : cte)
                                h.add(new Header("Content-Transfer-Encoding", header));

                        String[] ct = part.getHeader("Content-Type");
                        if (ct == null)
                            h.add(new Header("Content-Type", "text/plain"));
                        else
                            for (String header : ct)
                                h.add(new Header("Content-Type", header));

                        headers = new Enumeration<Header>() {
                            private int index = -1;

                            @Override
                            public boolean hasMoreElements() {
                                return (index + 1 < h.size());
                            }

                            @Override
                            public Header nextElement() {
                                return h.get(++index);
                            }
                        };
                    } else
                        headers = part.getAllHeaders();

                    while (headers.hasMoreElements()) {
                        Header header = headers.nextElement();
                        for (int i = 0; i < level; i++)
                            ssb.append("  ");
                        int start = ssb.length();
                        ssb.append(header.getName());
                        ssb.setSpan(new ForegroundColorSpan(textColorLink), start, ssb.length(), 0);
                        ssb.append(": ").append(header.getValue()).append('\n');
                    }

                    for (int i = 0; i < level; i++)
                        ssb.append("  ");
                    int size = part.getSize();
                    ssb.append("Size: ")
                            .append(size > 0 ? Helper.humanReadableByteCount(size) : "?")
                            .append('\n');

                    if (!part.isMimeType("multipart/*")) {
                        Object content = part.getContent();
                        if (content instanceof String) {
                            String text = (String) content;
                            Charset detected = CharsetHelper.detect(text);

                            String charset;
                            try {
                                ContentType ct = new ContentType(part.getContentType());
                                charset = ct.getParameter("charset");
                            } catch (Throwable ignored) {
                                charset = null;
                            }
                            if (charset == null)
                                charset = StandardCharsets.ISO_8859_1.name();

                            Charset cs = Charset.forName(charset);
                            boolean isUtf8 = CharsetHelper.isUTF8(text.getBytes(cs));

                            for (int i = 0; i < level; i++)
                                ssb.append("  ");

                            ssb.append("Detected: ")
                                    .append(detected == null ? "?" : detected.toString())
                                    .append(" isUTF8=").append(Boolean.toString(isUtf8))
                                    .append('\n');
                        }
                    }

                    ssb.append('\n');

                    if (part.isMimeType("multipart/*")) {
                        Multipart multipart = (Multipart) part.getContent();
                        for (int i = 0; i < multipart.getCount(); i++)
                            try {
                                getStructure(multipart.getBodyPart(i), ssb, level + 1, textColorLink);
                            } catch (Throwable ex) {
                                Log.w(ex);
                                ssb.append(ex.toString()).append('\n');
                            }
                    }
                } catch (Throwable ex) {
                    Log.w(ex);
                    ssb.append(ex.toString()).append('\n');
                }
            }
        }.execute(this, args, "eml:decode");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            switch (requestCode) {
                case REQUEST_ATTACHMENT:
                    if (resultCode == RESULT_OK && data != null)
                        onSaveAttachment(data);
                    break;
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    private void onSaveAttachment(Intent data) {
        Bundle args = new Bundle();
        args.putParcelable("uri", data.getData());

        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) throws Throwable {
                Uri uri = args.getParcelable("uri");

                if (!"content".equals(uri.getScheme())) {
                    Log.w("Save attachment uri=" + uri);
                    throw new IllegalArgumentException(context.getString(R.string.title_no_stream));
                }

                OutputStream os = null;
                InputStream is;
                try {
                    os = getContentResolver().openOutputStream(uri);
                    is = apart.part.getInputStream();

                    byte[] buffer = new byte[Helper.BUFFER_SIZE];
                    int read;
                    while ((read = is.read(buffer)) != -1)
                        os.write(buffer, 0, read);
                } finally {
                    try {
                        if (os != null)
                            os.close();
                    } catch (Throwable ex) {
                        Log.w(ex);
                    }
                }

                return null;
            }

            @Override
            protected void onExecuted(Bundle args, Void data) {
                ToastEx.makeText(ActivityEML.this, R.string.title_attachment_saved, Toast.LENGTH_LONG).show();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (ex instanceof IllegalArgumentException || ex instanceof FileNotFoundException)
                    ToastEx.makeText(ActivityEML.this, ex.getMessage(), Toast.LENGTH_LONG).show();
                else
                    Log.unexpectedError(getSupportFragmentManager(), ex);
            }
        }.execute(this, args, "eml:attachment");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_eml, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_save) {
            onMenuSave();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onMenuSave() {
        new SimpleTask<List<EntityAccount>>() {
            @Override
            protected List<EntityAccount> onExecute(Context context, Bundle args) {
                DB db = DB.getInstance(context);
                return db.account().getSynchronizingAccounts();
            }

            @Override
            protected void onExecuted(Bundle args, List<EntityAccount> accounts) {
                ArrayAdapter<EntityAccount> adapter =
                        new ArrayAdapter<>(ActivityEML.this, R.layout.spinner_item1, android.R.id.text1);
                for (EntityAccount account : accounts)
                    if (account.protocol == EntityAccount.TYPE_IMAP)
                        adapter.add(account);

                new AlertDialog.Builder(ActivityEML.this)
                        .setTitle(R.string.title_save_eml)
                        .setAdapter(adapter, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                EntityAccount account = adapter.getItem(which);

                                Bundle args = new Bundle();
                                args.putParcelable("uri", uri);
                                args.putLong("account", account.id);

                                new SimpleTask<String>() {
                                    @Override
                                    protected void onPreExecute(Bundle args) {
                                        ToastEx.makeText(ActivityEML.this, R.string.title_executing, Toast.LENGTH_LONG).show();
                                    }

                                    @Override
                                    protected String onExecute(Context context, Bundle args) throws Throwable {
                                        Uri uri = args.getParcelable("uri");
                                        long aid = args.getLong("account");

                                        DB db = DB.getInstance(context);
                                        EntityAccount account = db.account().getAccount(aid);
                                        if (account == null)
                                            return null;
                                        EntityFolder inbox = db.folder().getFolderByType(account.id, EntityFolder.INBOX);
                                        if (inbox == null)
                                            throw new IllegalArgumentException(context.getString(R.string.title_no_folder));

                                        ContentResolver resolver = context.getContentResolver();
                                        try (InputStream is = resolver.openInputStream(uri)) {

                                            Properties props = MessageHelper.getSessionProperties();
                                            Session isession = Session.getInstance(props, null);
                                            MimeMessage imessage = new MimeMessage(isession, is);

                                            try (EmailService iservice = new EmailService(
                                                    context, account.getProtocol(), account.realm, account.encryption, account.insecure, true)) {
                                                iservice.setPartialFetch(account.partial_fetch);
                                                iservice.setIgnoreBodyStructureSize(account.ignore_size);
                                                iservice.connect(account);

                                                IMAPFolder ifolder = (IMAPFolder) iservice.getStore().getFolder(inbox.name);
                                                ifolder.open(Folder.READ_WRITE);

                                                if (ifolder.getPermanentFlags().contains(Flags.Flag.DRAFT))
                                                    imessage.setFlag(Flags.Flag.DRAFT, false);

                                                ifolder.appendMessages(new Message[]{imessage});
                                            }
                                        }

                                        EntityOperation.sync(context, inbox.id, true);
                                        ServiceSynchronize.eval(context, "EML");

                                        return account.name + "/" + inbox.name;
                                    }

                                    @Override
                                    protected void onExecuted(Bundle args, String name) {
                                        ToastEx.makeText(ActivityEML.this, name, Toast.LENGTH_LONG).show();
                                    }

                                    @Override
                                    protected void onException(Bundle args, @NonNull Throwable ex) {
                                        if (ex instanceof IllegalArgumentException)
                                            Snackbar.make(findViewById(android.R.id.content), ex.getMessage(), Snackbar.LENGTH_LONG)
                                                    .setGestureInsetBottomIgnored(true).show();
                                        else
                                            Log.unexpectedError(getSupportFragmentManager(), ex);
                                    }
                                }.execute(ActivityEML.this, args, "eml:store");
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            }

            @Override
            protected void onException(Bundle args, @NonNull Throwable ex) {
                Log.unexpectedError(getSupportFragmentManager(), ex);
            }
        }.execute(this, new Bundle(), "messages:accounts");
    }

    private class Result {
        String from;
        String to;
        String replyTo;
        String cc;
        String bcc;
        Long sent;
        Long received;
        String subject;
        MessageHelper.MessageParts parts;
        Spanned body;
        Spanned structure;
    }
}
