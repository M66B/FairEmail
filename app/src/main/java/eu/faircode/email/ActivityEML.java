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

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.ArrowKeyMovementMethod;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.sun.mail.imap.IMAPFolder;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.List;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
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
    private CardView cardStructure;
    private CardView cardHeaders;
    private TextView tvHeaders;
    private TextView tvAuthentication;
    private ContentLoadingProgressBar pbWait;
    private FloatingActionButton fabSave;
    private Group grpReady;

    private MessageHelper.AttachmentPart apart;
    private static final int REQUEST_ATTACHMENT = 1;
    private static final int REQUEST_ACCOUNT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = LayoutInflater.from(this).inflate(R.layout.activity_eml, null);
        setContentView(view);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setSubtitle("EML");

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
        cardStructure = findViewById(R.id.cardStructure);
        cardHeaders = findViewById(R.id.cardHeaders);
        tvHeaders = findViewById(R.id.tvHeaders);
        tvAuthentication = findViewById(R.id.tvAuthentication);
        pbWait = findViewById(R.id.pbWait);
        fabSave = findViewById(R.id.fabSave);
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
                        args.putBoolean("always_confirm", true);

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
                        Uri uri = args.getParcelable("uri");

                        if (uri == null)
                            throw new FileNotFoundException();

                        File dir = Helper.ensureExists(context, "shared");
                        File file = new File(dir, "email.eml");

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

        fabSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onActionSave(EntityFolder.INBOX);
            }
        });

        fabSave.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(ActivityEML.this, ActivityEML.this, fabSave);

                popupMenu.getMenu().add(Menu.NONE, R.string.title_folder_inbox, 1, R.string.title_folder_inbox)
                        .setIcon(R.drawable.twotone_inbox_24)
                        .setIntent(new Intent().putExtra("type", EntityFolder.INBOX));
                popupMenu.getMenu().add(Menu.NONE, R.string.title_folder_junk, 2, R.string.title_folder_junk)
                        .setIcon(R.drawable.twotone_report_24)
                        .setIntent(new Intent().putExtra("type", EntityFolder.JUNK));
                popupMenu.getMenu().add(Menu.NONE, R.string.title_folder_drafts, 3, R.string.title_folder_drafts)
                        .setIcon(R.drawable.twotone_drafts_24)
                        .setIntent(new Intent().putExtra("type", EntityFolder.DRAFTS));

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        Intent intent = item.getIntent();
                        String type = (intent == null ? null : intent.getStringExtra("type"));
                        if (TextUtils.isEmpty(type))
                            return false;

                        onActionSave(type);

                        return true;
                    }
                });

                popupMenu.insertIcons(ActivityEML.this);
                popupMenu.show();

                return true;
            }
        });

        // Initialize
        vSeparatorAttachments.setVisibility(View.GONE);
        grpReady.setVisibility(View.GONE);
        cardStructure.setVisibility(View.GONE);
        cardHeaders.setVisibility(View.GONE);

        load();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        load();
    }

    private void load() {
        Uri uri = getIntent().getData();
        Log.i("EML uri=" + uri);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean debug = prefs.getBoolean("debug", false);

        Bundle args = new Bundle();
        args.putParcelable("uri", uri);
        args.putBoolean("debug", debug || BuildConfig.DEBUG);

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
                boolean debug = args.getBoolean("debug");

                NoStreamException.check(uri, context);

                Result result = new Result();

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                boolean download_plain = prefs.getBoolean("download_plain", false);

                ContentResolver resolver = context.getContentResolver();
                try (InputStream is = resolver.openInputStream(uri)) {
                    if (is == null)
                        throw new FileNotFoundException(uri.toString());

                    Properties props = MessageHelper.getSessionProperties(true);
                    Session isession = Session.getInstance(props, null);
                    MimeMessage imessage = new MimeMessage(isession, is);

                    MessageHelper helper = new MessageHelper(imessage, context);

                    result.from = MessageHelper.formatAddresses(helper.getFrom());
                    result.to = MessageHelper.formatAddresses(helper.getTo());
                    result.replyTo = MessageHelper.formatAddresses(helper.getReply());
                    result.cc = MessageHelper.formatAddresses(helper.getCc());
                    result.bcc = MessageHelper.formatAddresses(helper.getBcc());
                    result.sent = helper.getSent();
                    result.received = helper.getReceivedHeader();
                    result.subject = helper.getSubject();
                    result.parts = helper.getMessageParts(false);

                    String html = result.parts.getHtml(context, download_plain);
                    if (html != null) {
                        Document parsed = JsoupEx.parse(html);
                        HtmlHelper.autoLink(parsed);
                        Document document = HtmlHelper.sanitizeView(context, parsed, false);
                        result.body = HtmlHelper.fromDocument(context, document, new HtmlHelper.ImageGetterEx() {
                            @Override
                            public Drawable getDrawable(Element img) {
                                Drawable d = null;
                                if (TextUtils.isEmpty(img.attr("x-tracking"))) {
                                    String src = img.attr("src");
                                    if (src.startsWith("cid:")) {
                                        String cid = "<" + src.substring(4) + ">";
                                        Integer w = Helper.parseInt(img.attr("width"));
                                        Integer h = Helper.parseInt(img.attr("height"));
                                        Resources res = context.getResources();
                                        int scaleToPixels = res.getDisplayMetrics().widthPixels;
                                        for (MessageHelper.AttachmentPart apart : result.parts.getAttachmentParts())
                                            if (cid.equals(apart.attachment.cid)) {
                                                try {
                                                    Bitmap bm = ImageHelper.getScaledBitmap(apart.part.getInputStream(), src, apart.attachment.type, scaleToPixels);
                                                    d = new BitmapDrawable(res, bm);
                                                    d.setBounds(0, 0, bm.getWidth(), bm.getHeight());
                                                    ImageHelper.fitDrawable(d, w == null ? 0 : w, h == null ? 0 : h, 1.0f, tvBody);
                                                } catch (Throwable ex) {
                                                    Log.e(ex);
                                                }
                                                break;
                                            }
                                        if (d == null)
                                            d = ContextCompat.getDrawable(context, R.drawable.twotone_broken_image_24);
                                    } else
                                        d = ContextCompat.getDrawable(context, R.drawable.twotone_image_24);
                                } else {
                                    d = ContextCompat.getDrawable(context, R.drawable.twotone_my_location_24);
                                    d.setTint(Helper.resolveColor(context, R.attr.colorWarning));
                                }
                                d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
                                return d;
                            }
                        }, null);
                    }

                    if (debug) {
                        int textColorLink = Helper.resolveColor(context, android.R.attr.textColorLink);
                        SpannableStringBuilder ssb = new SpannableStringBuilderEx();
                        MessageHelper.getStructure(imessage, ssb, 0, textColorLink);
                        result.structure = ssb;

                        result.headers = HtmlHelper.highlightHeaders(context,
                                helper.getFrom(),
                                helper.getTo(),
                                helper.getReceivedHeader(),
                                helper.getHeaders(),
                                false, false);

                        ssb = new SpannableStringBuilderEx();

                        String[] authentication = helper.getAuthentication();

                        Boolean tls = helper.getTLS();
                        Boolean dkim = MessageHelper.getAuthentication("dkim", authentication);
                        Boolean spf = MessageHelper.getAuthentication("spf", authentication);
                        if (spf == null)
                            spf = helper.getSPF();
                        Boolean dmarc = MessageHelper.getAuthentication("dmarc", authentication);
                        Boolean auth = MessageHelper.getAuthentication("auth", authentication);

                        List<String> signers = helper.verifyDKIM(context);
                        boolean aligned = helper.isAligned(context, signers,
                                helper.getReturnPath(), helper.getMailFrom(authentication), helper.getFrom(), helper.getSPF());

                        ssb.append("TLS:   ").append(tls == null ? "-" : (tls ? "✓" : "✗")).append('\n');
                        ssb.append("DKIM:  ").append(dkim == null ? "-" : (dkim ? "✓" : "✗")).append('\n');
                        ssb.append("SPF:   ").append(spf == null ? "-" : (spf ? "✓" : "✗")).append('\n');
                        ssb.append("DMARC: ").append(dmarc == null ? "-" : (dmarc ? "✓" : "✗")).append('\n');
                        ssb.append("AUTH:  ").append(auth == null ? "-" : (auth ? "✓" : "✗")).append('\n');

                        ssb.append('\n');
                        ssb.append("Signers: ").append('\n');
                        for (String signer : signers)
                            ssb.append("- ").append(signer).append('\n');
                        ssb.append('\n');
                        ssb.append("Aligned: ").append(Boolean.toString(aligned)).append('\n');

                        result.authentication = ssb;
                    }

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
                                create.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                create.setType(apart.attachment.getMimeType());
                                if (!TextUtils.isEmpty(apart.attachment.name))
                                    create.putExtra(Intent.EXTRA_TITLE, apart.attachment.name);
                                Helper.openAdvanced(ActivityEML.this, create);
                                if (create.resolveActivity(getPackageManager()) == null) // system whitelisted
                                    Log.unexpectedError(getSupportFragmentManager(),
                                            new IllegalArgumentException(getString(R.string.title_no_saf)), 25);
                                else
                                    startActivityForResult(Helper.getChooser(ActivityEML.this, create), REQUEST_ATTACHMENT);
                            }
                        });
                rvAttachment.setAdapter(adapter);

                tvBody.setText(result.body);
                grpReady.setVisibility(View.VISIBLE);

                tvStructure.setText(result.structure);
                tvHeaders.setText(result.headers);
                tvAuthentication.setText(result.authentication);

                boolean debug = args.getBoolean("debug");
                cardStructure.setVisibility(debug ? View.VISIBLE : View.GONE);
                cardHeaders.setVisibility(debug ? View.VISIBLE : View.GONE);
            }

            @Override
            protected void onException(Bundle args, @NonNull Throwable ex) {
                if (ex instanceof NoStreamException)
                    ((NoStreamException) ex).report(ActivityEML.this);
                else
                    Log.unexpectedError(getSupportFragmentManager(), ex, false);
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
                case REQUEST_ACCOUNT:
                    if (resultCode == RESULT_OK && data != null)
                        onSave(data.getBundleExtra("args"));
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

                if (uri == null)
                    throw new FileNotFoundException();

                if (!"content".equals(uri.getScheme())) {
                    Log.w("Save attachment uri=" + uri);
                    throw new IllegalArgumentException(context.getString(R.string.title_no_stream));
                }

                OutputStream os = null;
                InputStream is;
                try {
                    os = getContentResolver().openOutputStream(uri);
                    is = apart.part.getInputStream();

                    if (os == null)
                        throw new FileNotFoundException(uri.toString());

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
                boolean report = !(ex instanceof IllegalArgumentException || ex instanceof FileNotFoundException);
                Log.unexpectedError(getSupportFragmentManager(), ex, report);
            }
        }.execute(this, args, "eml:attachment");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onActionSave(String folderType) {
        Bundle args = new Bundle();
        args.putInt("type", EntityAccount.TYPE_IMAP);
        args.putString("folderType", folderType);

        FragmentDialogSelectAccount fragment = new FragmentDialogSelectAccount();
        fragment.setArguments(args);
        fragment.setTargetActivity(this, REQUEST_ACCOUNT);
        fragment.show(getSupportFragmentManager(), "eml:account");
    }

    private void onSave(Bundle args) {
        args.putParcelable("uri", getIntent().getData());

        new SimpleTask<String>() {
            private Toast toast = null;

            @Override
            protected void onPreExecute(Bundle args) {
                toast = ToastEx.makeText(ActivityEML.this, R.string.title_executing, Toast.LENGTH_LONG);
                toast.show();
            }

            @Override
            protected void onPostExecute(Bundle args) {
                if (toast != null)
                    toast.cancel();
            }

            @Override
            protected String onExecute(Context context, Bundle args) throws Throwable {
                Uri uri = args.getParcelable("uri");
                String type = args.getString("folderType");
                long aid = args.getLong("account");

                DB db = DB.getInstance(context);
                EntityAccount account = db.account().getAccount(aid);
                if (account == null)
                    return null;
                EntityFolder folder = db.folder().getFolderByType(account.id, type);
                if (folder == null)
                    throw new IllegalArgumentException(context.getString(R.string.title_no_folder));

                ContentResolver resolver = context.getContentResolver();
                try (InputStream is = resolver.openInputStream(uri)) {
                    if (is == null)
                        throw new FileNotFoundException(uri.toString());

                    Properties props = MessageHelper.getSessionProperties(true);
                    Session isession = Session.getInstance(props, null);
                    MimeMessage imessage = new MimeMessage(isession, is);

                    try (EmailService iservice = new EmailService(context, account, EmailService.PURPOSE_USE, true)) {
                        iservice.setPartialFetch(account.partial_fetch);
                        iservice.setRawFetch(account.raw_fetch);
                        iservice.setIgnoreBodyStructureSize(account.ignore_size);
                        iservice.connect(account);

                        IMAPFolder ifolder = (IMAPFolder) iservice.getStore().getFolder(folder.name);
                        ifolder.open(Folder.READ_WRITE);

                        if (ifolder.getPermanentFlags().contains(Flags.Flag.DRAFT))
                            imessage.setFlag(Flags.Flag.DRAFT, false);

                        ifolder.appendMessages(new Message[]{imessage});
                    }
                }

                EntityOperation.sync(context, folder.id, true);
                ServiceSynchronize.eval(context, "EML");

                return account.name + "/" + folder.name;
            }

            @Override
            protected void onExecuted(Bundle args, String name) {
                ToastEx.makeText(ActivityEML.this, name, Toast.LENGTH_LONG).show();
            }

            @Override
            protected void onDestroyed(Bundle args) {
                if (toast != null) {
                    toast.cancel();
                    toast = null;
                }
            }

            @Override
            protected void onException(Bundle args, @NonNull Throwable ex) {
                if (ex instanceof IllegalArgumentException)
                    Helper.setSnackbarOptions(
                                    Snackbar.make(findViewById(android.R.id.content), new ThrowableWrapper(ex).getSafeMessage(), Snackbar.LENGTH_LONG))
                            .show();
                else
                    Log.unexpectedError(getSupportFragmentManager(), ex);
            }
        }.execute(ActivityEML.this, args, "eml:store");
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
        Spanned headers;
        Spanned authentication;
    }
}
