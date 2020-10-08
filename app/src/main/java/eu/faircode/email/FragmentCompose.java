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

    Copyright 2018-2020 by Marcel Bokhorst (M66B)
*/

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.security.KeyChain;
import android.security.KeyChainException;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.BulletSpan;
import android.text.style.CharacterStyle;
import android.text.style.ImageSpan;
import android.text.style.ParagraphStyle;
import android.text.style.QuoteSpan;
import android.text.style.URLSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.FileProvider;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.documentfile.provider.DocumentFile;
import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;
import com.google.android.material.snackbar.Snackbar;

import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSAlgorithm;
import org.bouncycastle.cms.CMSEnvelopedData;
import org.bouncycastle.cms.CMSEnvelopedDataGenerator;
import org.bouncycastle.cms.CMSProcessableFile;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.RecipientInfoGenerator;
import org.bouncycastle.cms.SignerInfoGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.bouncycastle.cms.jcajce.JceKeyAgreeRecipientInfoGenerator;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.OutputEncryptor;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeFilter;
import org.openintents.openpgp.OpenPgpError;
import org.openintents.openpgp.util.OpenPgpApi;
import org.openintents.openpgp.util.OpenPgpServiceConnection;
import org.w3c.dom.css.CSSStyleSheet;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.Collator;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.ParseException;
import javax.mail.util.ByteArrayDataSource;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.property.Organizer;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_FIRST_USER;
import static android.app.Activity.RESULT_OK;
import static android.widget.AdapterView.INVALID_POSITION;

public class FragmentCompose extends FragmentBase {
    private enum State {NONE, LOADING, LOADED}

    private ViewGroup view;
    private Spinner spIdentity;
    private EditText etExtra;
    private TextView tvDomain;
    private MultiAutoCompleteTextView etTo;
    private ImageButton ibToAdd;
    private MultiAutoCompleteTextView etCc;
    private ImageButton ibCcAdd;
    private MultiAutoCompleteTextView etBcc;
    private ImageButton ibBccAdd;
    private EditText etSubject;
    private ImageButton ibCcBcc;
    private RecyclerView rvAttachment;
    private TextView tvNoInternetAttachments;
    private TextView tvPlainTextOnly;
    private EditTextCompose etBody;
    private TextView tvNoInternet;
    private TextView tvSignature;
    private CheckBox cbSignature;
    private ImageButton ibSignature;
    private TextView tvReference;
    private ImageButton ibCloseRefHint;
    private ImageButton ibReferenceEdit;
    private ImageButton ibReferenceImages;
    private BottomNavigationView style_bar;
    private BottomNavigationView media_bar;
    private BottomNavigationView bottom_navigation;
    private ContentLoadingProgressBar pbWait;
    private Group grpHeader;
    private Group grpExtra;
    private Group grpAddresses;
    private Group grpAttachments;
    private Group grpBody;
    private Group grpSignature;
    private Group grpReferenceHint;

    private ContentResolver resolver;
    private AdapterAttachment adapter;

    private boolean prefix_once = false;
    private boolean monospaced = false;
    private String compose_font;
    private Integer encrypt = null;
    private boolean media = true;
    private boolean compact = false;
    private int zoom = 0;

    private long working = -1;
    private State state = State.NONE;
    private boolean show_images = false;
    private int last_available = 0; // attachments
    private boolean saved = false;
    private String subject = null;

    private Uri photoURI = null;

    private OpenPgpServiceConnection pgpService;
    private String[] pgpUserIds;
    private long[] pgpKeyIds;
    private long pgpSignKeyId;

    private static final int REDUCED_IMAGE_SIZE = 1440; // pixels
    private static final int REDUCED_IMAGE_QUALITY = 90; // percent

    private static final int RECIPIENTS_WARNING = 10;

    private static final int REQUEST_CONTACT_TO = 1;
    private static final int REQUEST_CONTACT_CC = 2;
    private static final int REQUEST_CONTACT_BCC = 3;
    private static final int REQUEST_SHARED = 4;
    private static final int REQUEST_IMAGE = 5;
    private static final int REQUEST_IMAGE_FILE = 6;
    private static final int REQUEST_ATTACHMENT = 7;
    private static final int REQUEST_TAKE_PHOTO = 8;
    private static final int REQUEST_RECORD_AUDIO = 9;
    private static final int REQUEST_OPENPGP = 10;
    private static final int REQUEST_CONTACT_GROUP = 11;
    private static final int REQUEST_LINK = 12;
    private static final int REQUEST_DISCARD = 13;
    private static final int REQUEST_SEND = 14;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefix_once = prefs.getBoolean("prefix_once", true);
        monospaced = prefs.getBoolean("monospaced", false);
        compose_font = prefs.getString("compose_font", monospaced ? "monospace" : "sans-serif");
        media = prefs.getBoolean("compose_media", true);
        compact = prefs.getBoolean("compose_compact", false);
        zoom = prefs.getInt("compose_zoom", compact ? 0 : 1);

        setTitle(R.string.page_compose);
        setSubtitle(getResources().getQuantityString(R.plurals.page_message, 1));
    }

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = (ViewGroup) inflater.inflate(R.layout.fragment_compose, container, false);

        // Get controls
        spIdentity = view.findViewById(R.id.spIdentity);
        etExtra = view.findViewById(R.id.etExtra);
        tvDomain = view.findViewById(R.id.tvDomain);
        etTo = view.findViewById(R.id.etTo);
        ibToAdd = view.findViewById(R.id.ivToAdd);
        etCc = view.findViewById(R.id.etCc);
        ibCcAdd = view.findViewById(R.id.ivCcAdd);
        etBcc = view.findViewById(R.id.etBcc);
        ibBccAdd = view.findViewById(R.id.ivBccAdd);
        etSubject = view.findViewById(R.id.etSubject);
        ibCcBcc = view.findViewById(R.id.ivCcBcc);
        rvAttachment = view.findViewById(R.id.rvAttachment);
        tvNoInternetAttachments = view.findViewById(R.id.tvNoInternetAttachments);
        tvPlainTextOnly = view.findViewById(R.id.tvPlainTextOnly);
        etBody = view.findViewById(R.id.etBody);
        tvNoInternet = view.findViewById(R.id.tvNoInternet);
        tvSignature = view.findViewById(R.id.tvSignature);
        cbSignature = view.findViewById(R.id.cbSignature);
        ibSignature = view.findViewById(R.id.ibSignature);
        tvReference = view.findViewById(R.id.tvReference);
        ibCloseRefHint = view.findViewById(R.id.ibCloseRefHint);
        ibReferenceEdit = view.findViewById(R.id.ibReferenceEdit);
        ibReferenceImages = view.findViewById(R.id.ibReferenceImages);
        style_bar = view.findViewById(R.id.style_bar);
        media_bar = view.findViewById(R.id.media_bar);
        bottom_navigation = view.findViewById(R.id.bottom_navigation);

        pbWait = view.findViewById(R.id.pbWait);
        grpHeader = view.findViewById(R.id.grpHeader);
        grpExtra = view.findViewById(R.id.grpExtra);
        grpAddresses = view.findViewById(R.id.grpAddresses);
        grpAttachments = view.findViewById(R.id.grpAttachments);
        grpBody = view.findViewById(R.id.grpBody);
        grpSignature = view.findViewById(R.id.grpSignature);
        grpReferenceHint = view.findViewById(R.id.grpReferenceHint);

        resolver = getContext().getContentResolver();

        // Wire controls
        spIdentity.setOnItemSelectedListener(identitySelected);

        View.OnTouchListener onTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                EditText et = (EditText) v;
                int sstart = et.getSelectionStart();
                int send = et.getSelectionEnd();

                if (sstart == send && event.getAction() == MotionEvent.ACTION_DOWN) {
                    float x = event.getX() + et.getScrollX();
                    float y = event.getY() + et.getScrollY();
                    int pos = et.getOffsetForPosition(x, y);
                    if (pos >= 0)
                        et.setSelection(pos);
                }

                return false;
            }
        };

        View.OnLongClickListener longClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                EditText et = (EditText) v;
                int sstart = et.getSelectionStart();
                int send = et.getSelectionEnd();
                String text = et.getText().toString();

                if (send < 0 || send > sstart)
                    return false;

                int ecomma = text.indexOf(',', sstart);
                if (ecomma < 0)
                    return false;

                int scomma = text.substring(0, ecomma).lastIndexOf(',');
                scomma = (scomma < 0 ? 0 : scomma + 1);
                et.setSelection(scomma, ecomma + 1);
                return false;
            }
        };

        etTo.setMaxLines(Integer.MAX_VALUE);
        etTo.setHorizontallyScrolling(false);
        etTo.setOnTouchListener(onTouchListener);
        etTo.setOnLongClickListener(longClickListener);

        etCc.setMaxLines(Integer.MAX_VALUE);
        etCc.setHorizontallyScrolling(false);
        etCc.setOnTouchListener(onTouchListener);
        etCc.setOnLongClickListener(longClickListener);

        etBcc.setMaxLines(Integer.MAX_VALUE);
        etBcc.setHorizontallyScrolling(false);
        etBcc.setOnTouchListener(onTouchListener);
        etBcc.setOnLongClickListener(longClickListener);

        etSubject.setMaxLines(Integer.MAX_VALUE);
        etSubject.setHorizontallyScrolling(false);

        ibCcBcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMenuAddresses();
            }
        });

        ibCcBcc.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onMenuAddresses();
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                prefs.edit().putBoolean("cc_bcc", grpAddresses.getVisibility() == View.VISIBLE).apply();
                ToastEx.makeText(v.getContext(), R.string.title_default_changed, Toast.LENGTH_LONG).show();
                return true;
            }
        });

        View.OnClickListener onPick = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int request;
                switch (view.getId()) {
                    case R.id.ivToAdd:
                        request = REQUEST_CONTACT_TO;
                        break;
                    case R.id.ivCcAdd:
                        request = REQUEST_CONTACT_CC;
                        break;
                    case R.id.ivBccAdd:
                        request = REQUEST_CONTACT_BCC;
                        break;
                    default:
                        return;
                }

                Intent pick = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Email.CONTENT_URI);
                startActivityForResult(Helper.getChooser(getContext(), pick), request);
            }
        };

        ibToAdd.setOnClickListener(onPick);
        ibCcAdd.setOnClickListener(onPick);
        ibBccAdd.setOnClickListener(onPick);

        setZoom();

        etBody.setInputContentListener(new EditTextCompose.IInputContentListener() {
            @Override
            public void onInputContent(Uri uri) {
                onAddAttachment(Arrays.asList(uri), true, 0, false);
            }
        });

        etBody.setSelectionListener(new EditTextCompose.ISelection() {
            private boolean style = false;
            private boolean styling = false;

            @Override
            public void onSelected(boolean selection) {
                if (media) {
                    style = selection;
                    getMainHandler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (style != styling) {
                                styling = style;
                                media_bar.getMenu().clear();
                                media_bar.inflateMenu(styling ? R.menu.action_compose_style_alt : R.menu.action_compose_media);
                            }
                        }
                    }, 20);
                } else
                    style_bar.setVisibility(selection ? View.VISIBLE : View.GONE);
            }
        });

        // https://developer.android.com/reference/android/text/TextWatcher
        etBody.addTextChangedListener(new TextWatcher() {
            private Integer added = null;
            private Integer removed = null;

            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {
                if (count == 1 && after == 0 && text.charAt(start) == '\n') {
                    Log.i("Removed=" + start);
                    removed = start;
                }
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                Activity activity = getActivity();
                if (activity != null)
                    activity.onUserInteraction();

                if (before == 0 && count == 1 && start > 0 && text.charAt(start) == '\n') {
                    Log.i("Added=" + start);
                    added = start;
                }
            }

            @Override
            public void afterTextChanged(Editable text) {
                if (added != null)
                    try {
                        // break block quotes
                        boolean broken = false;
                        QuoteSpan[] spans = text.getSpans(added + 1, added + 1, QuoteSpan.class);
                        for (QuoteSpan span : spans) {
                            int s = text.getSpanStart(span);
                            int e = text.getSpanEnd(span);
                            int f = text.getSpanFlags(span) | Spanned.SPAN_PARAGRAPH;
                            Log.i("Span " + s + "..." + e + " added=" + added);

                            if (s > 0 && added - s > 0 && e - (added + 1) > 0 &&
                                    text.charAt(s - 1) == '\n' && text.charAt(added - 1) == '\n' &&
                                    text.charAt(added) == '\n' && text.charAt(e - 1) == '\n') {
                                broken = true;

                                QuoteSpan q1 = clone(span, QuoteSpan.class, etBody.getContext());
                                text.setSpan(q1, s, added, f);
                                Log.i("Span " + s + "..." + added);

                                QuoteSpan q2 = clone(span, QuoteSpan.class, etBody.getContext());
                                text.setSpan(q2, added + 1, e, f);
                                Log.i("Span " + (added + 1) + "..." + e);

                                text.removeSpan(span);
                            }
                        }

                        if (broken) {
                            CharacterStyle[] sspan = text.getSpans(added + 1, added + 1, CharacterStyle.class);
                            for (CharacterStyle span : sspan) {
                                int s = text.getSpanStart(span);
                                int e = text.getSpanEnd(span);
                                int f = text.getSpanFlags(span);
                                Log.i("Style span " + s + "..." + e + " start=" + added);

                                if (s <= added && added + 1 <= e) {
                                    CharacterStyle s1 = CharacterStyle.wrap(span);
                                    text.setSpan(s1, s, added, f);
                                    Log.i("Style span " + s + "..." + added);

                                    CharacterStyle s2 = CharacterStyle.wrap(span);
                                    text.setSpan(s2, added + 1, e, f);
                                    Log.i("Style span " + (added + 1) + "..." + e);

                                    text.removeSpan(span);
                                }
                            }

                            etBody.setSelection(added);
                        }

                        boolean renum = false;
                        BulletSpan[] bullets = text.getSpans(added + 1, added + 1, BulletSpan.class);
                        for (BulletSpan span : bullets) {
                            int s = text.getSpanStart(span);
                            int e = text.getSpanEnd(span);
                            int f = text.getSpanFlags(span) | Spanned.SPAN_PARAGRAPH;
                            Log.i("Span " + s + "..." + e + " added=" + added);

                            if (s > 0 &&
                                    added + 1 > s && e > added + 1 &&
                                    text.charAt(s - 1) == '\n' && text.charAt(e - 1) == '\n') {
                                if (e - s > 2) {
                                    BulletSpan b1 = clone(span, span.getClass(), etBody.getContext());
                                    text.setSpan(b1, s, added + 1, f);
                                    Log.i("Span " + s + "..." + (added + 1));

                                    BulletSpan b2 = clone(b1, span.getClass(), etBody.getContext());
                                    text.setSpan(b2, added + 1, e, f);
                                    Log.i("Span " + (added + 1) + "..." + e);
                                }

                                renum = true;
                                text.removeSpan(span);
                            }
                        }

                        if (renum)
                            renumber(text, false);
                    } catch (Throwable ex) {
                        Log.e(ex);
                    } finally {
                        added = null;
                    }

                if (removed != null) {
                    renumber(text, true);
                    removed = null;
                }

                //TextUtils.dumpSpans(text, new LogPrinter(android.util.Log.INFO, "FairEmail"), "afterTextChanged ");
            }

            public void renumber(Editable text, boolean clean) {
                Context context = etBody.getContext();
                int dp6 = Helper.dp2pixels(context, 6);
                int colorAccent = Helper.resolveColor(context, R.attr.colorAccent);

                Log.i("Renumber clean=" + clean + " text=" + text);

                int next;
                int index = 1;
                int pos = -1;
                for (int i = 0; i < text.length(); i = next) {
                    next = text.nextSpanTransition(i, text.length(), NumberSpan.class);
                    Log.i("Bullet span next=" + next);

                    BulletSpan[] spans = text.getSpans(i, next, BulletSpan.class);
                    for (BulletSpan span : spans) {
                        int start = text.getSpanStart(span);
                        int end = text.getSpanEnd(span);
                        int flags = text.getSpanFlags(span);
                        Log.i("Bullet span " + start + "..." + end);

                        if (clean && start == end) {
                            text.removeSpan(span);
                            continue;
                        }

                        if (span instanceof NumberSpan) {
                            if (start == pos)
                                index++;
                            else
                                index = 1;

                            NumberSpan ns = (NumberSpan) span;
                            if (index != ns.getIndex()) {
                                NumberSpan clone = new NumberSpan(dp6, colorAccent, ns.getTextSize(), index);
                                text.removeSpan(span);
                                text.setSpan(clone, start, end, flags);
                            }

                            pos = end;
                        }
                    }
                }
            }

            public <T extends ParagraphStyle> T clone(Object span, Class<T> type, Context context) {
                if (QuoteSpan.class.isAssignableFrom(type)) {
                    QuoteSpan q = (QuoteSpan) span;
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P)
                        return (T) new QuoteSpan(q.getColor());
                    else
                        return (T) new QuoteSpan(q.getColor(), q.getStripeWidth(), q.getGapWidth());
                } else if (NumberSpan.class.isAssignableFrom(type)) {
                    NumberSpan n = (NumberSpan) span;
                    int dp6 = Helper.dp2pixels(context, 6);
                    int colorAccent = Helper.resolveColor(context, R.attr.colorAccent);
                    return (T) new NumberSpan(dp6, colorAccent, n.getTextSize(), n.getIndex() + 1);
                } else if (BulletSpan.class.isAssignableFrom(type)) {
                    BulletSpan b = (BulletSpan) span;
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                        int dp6 = Helper.dp2pixels(context, 6);
                        int colorAccent = Helper.resolveColor(context, R.attr.colorAccent);
                        return (T) new BulletSpan(dp6, colorAccent);
                    } else
                        return (T) new BulletSpan(b.getGapWidth(), b.getColor(), b.getBulletRadius());

                } else
                    throw new IllegalArgumentException(type.getName());
            }
        });

        cbSignature.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                Object tag = cbSignature.getTag();
                if (tag == null || !tag.equals(checked)) {
                    cbSignature.setTag(checked);
                    if (tag != null)
                        onAction(R.id.action_save, "signature");
                }
            }
        });

        ibSignature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EntityIdentity identity = (EntityIdentity) spIdentity.getSelectedItem();
                if (identity == null || TextUtils.isEmpty(identity.signature))
                    return;

                ClipboardManager clipboard =
                        (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null) {
                    ClipData clip = ClipData.newHtmlText(
                            getContext().getString(R.string.title_edit_signature_text),
                            HtmlHelper.getText(getContext(), identity.signature),
                            identity.signature);
                    clipboard.setPrimaryClip(clip);

                    ToastEx.makeText(getContext(), R.string.title_clipboard_copied, Toast.LENGTH_LONG).show();
                }
            }
        });

        ibCloseRefHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                prefs.edit().putBoolean("compose_reference", false).apply();
                grpReferenceHint.setVisibility(View.GONE);
            }
        });

        ibReferenceEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onReferenceEdit();
            }
        });

        ibReferenceImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ibReferenceImages.setVisibility(View.GONE);
                onReferenceImages();
            }
        });

        etBody.setTypeface(Typeface.create(compose_font, Typeface.NORMAL));
        tvReference.setTypeface(monospaced ? Typeface.MONOSPACE : Typeface.DEFAULT);
        tvReference.setMovementMethod(LinkMovementMethod.getInstance());

        style_bar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int action = item.getItemId();
                return onActionStyle(action, style_bar.findViewById(action));
            }
        });

        media_bar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int action = item.getItemId();
                switch (action) {
                    case R.id.menu_record_audio:
                        onActionRecordAudio();
                        return true;
                    case R.id.menu_take_photo:
                        onActionImage(true);
                        return true;
                    case R.id.menu_image:
                        onActionImage(false);
                        return true;
                    case R.id.menu_attachment:
                        onActionAttachment();
                        return true;
                    case R.id.menu_link:
                        onActionLink();
                        return true;
                    default:
                        return onActionStyle(action, media_bar.findViewById(action));
                }
            }
        });

        setCompact(compact);

        bottom_navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                final int action = item.getItemId();
                switch (action) {
                    case R.id.action_delete:
                        onActionDiscard();
                        break;
                    case R.id.action_send:
                        onAction(R.id.action_check, "check");
                        break;
                    case R.id.action_save:
                        saved = true;
                        onAction(action, "save");
                        break;
                    default:
                        onAction(action, "navigation");
                }
                return true;
            }
        });

        addKeyPressedListener(onKeyPressedListener);

        // Initialize
        setHasOptionsMenu(true);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        //boolean beige = prefs.getBoolean("beige", true);
        //if (beige && !Helper.isDarkTheme(getContext()))
        //    view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.lightColorBackground_cards_beige));

        etExtra.setHint("");
        tvDomain.setText(null);
        tvPlainTextOnly.setVisibility(View.GONE);
        etBody.setText(null);

        grpHeader.setVisibility(View.GONE);
        grpExtra.setVisibility(View.GONE);
        ibCcBcc.setVisibility(View.GONE);
        grpAttachments.setVisibility(View.GONE);
        tvNoInternet.setVisibility(View.GONE);
        grpBody.setVisibility(View.GONE);
        grpSignature.setVisibility(View.GONE);
        grpReferenceHint.setVisibility(View.GONE);
        ibReferenceEdit.setVisibility(View.GONE);
        ibReferenceImages.setVisibility(View.GONE);
        tvReference.setVisibility(View.GONE);
        style_bar.setVisibility(View.GONE);
        media_bar.setVisibility(View.GONE);
        bottom_navigation.setVisibility(View.GONE);
        pbWait.setVisibility(View.VISIBLE);

        getActivity().invalidateOptionsMenu();
        Helper.setViewsEnabled(view, false);

        final DB db = DB.getInstance(getContext());

        final boolean suggest_sent = prefs.getBoolean("suggest_sent", true);
        final boolean suggest_received = prefs.getBoolean("suggest_received", false);
        final boolean suggest_frequently = prefs.getBoolean("suggest_frequently", false);
        final boolean cc_bcc = prefs.getBoolean("cc_bcc", false);
        final boolean circular = prefs.getBoolean("circular", true);
        final float dp3 = Helper.dp2pixels(getContext(), 3);

        SimpleCursorAdapter cadapter = new SimpleCursorAdapter(
                getContext(),
                R.layout.spinner_contact,
                null,
                new String[]{"name", "email", "photo"},
                new int[]{R.id.tvName, R.id.tvEmail, R.id.ivPhoto},
                0);

        cadapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            private int colName = -1;
            private int colLocal = -1;

            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                try {
                    int id = view.getId();
                    if (id == R.id.tvName) {
                        if (colName < 0)
                            colName = cursor.getColumnIndex("name");

                        if (cursor.isNull(colName)) {
                            ((TextView) view).setText("-");
                            return true;
                        }
                    } else if (id == R.id.ivPhoto) {
                        if (colLocal < 0)
                            colLocal = cursor.getColumnIndex("local");

                        ImageView photo = (ImageView) view;

                        GradientDrawable bg = new GradientDrawable();
                        if (circular)
                            bg.setShape(GradientDrawable.OVAL);
                        else
                            bg.setCornerRadius(dp3);
                        photo.setBackground(bg);
                        photo.setClipToOutline(true);

                        if (cursor.getInt(colLocal) == 1)
                            photo.setImageDrawable(null);
                        else {
                            String uri = cursor.getString(columnIndex);
                            if (uri == null)
                                photo.setImageResource(R.drawable.twotone_person_24);
                            else
                                photo.setImageURI(Uri.parse(uri));
                        }
                        return true;
                    }
                } catch (Throwable ex) {
                    Log.e(ex);
                }
                return false;
            }
        });

        cadapter.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
            private int colName = -1;
            private int colEmail = -1;

            public CharSequence convertToString(Cursor cursor) {
                try {
                    if (colName < 0)
                        colName = cursor.getColumnIndex("name");
                    if (colEmail < 0)
                        colEmail = cursor.getColumnIndex("email");

                    String name = cursor.getString(colName);
                    String email = MessageHelper.sanitizeEmail(cursor.getString(colEmail));
                    StringBuilder sb = new StringBuilder();
                    if (name == null)
                        sb.append(email);
                    else {
                        sb.append("\"").append(name).append("\" ");
                        sb.append("<").append(email).append(">");
                    }
                    return sb.toString();
                } catch (Throwable ex) {
                    Log.e(ex);
                    return ex.toString();
                }
            }
        });

        cadapter.setFilterQueryProvider(new FilterQueryProvider() {
            public Cursor runQuery(CharSequence typed) {
                MatrixCursor result = new MatrixCursor(new String[]{"_id", "name", "email", "photo", "local"});

                try {
                    Log.i("Suggest contact=" + typed);
                    if (typed == null)
                        return result;

                    String wildcard = "%" + typed + "%";
                    Map<String, EntityContact> map = new HashMap<>();

                    boolean contacts = Helper.hasPermission(getContext(), Manifest.permission.READ_CONTACTS);
                    if (contacts) {
                        Cursor cursor = resolver.query(
                                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                                new String[]{
                                        ContactsContract.Contacts.DISPLAY_NAME,
                                        ContactsContract.CommonDataKinds.Email.DATA,
                                        ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
                                        ContactsContract.Contacts.STARRED
                                },
                                ContactsContract.CommonDataKinds.Email.DATA + " <> ''" +
                                        " AND (" + ContactsContract.Contacts.DISPLAY_NAME + " LIKE ?" +
                                        " OR " + ContactsContract.CommonDataKinds.Email.DATA + " LIKE ?)",
                                new String[]{wildcard, wildcard},
                                null);

                        while (cursor != null && cursor.moveToNext()) {
                            EntityContact item = new EntityContact();
                            item.id = 0L;
                            item.name = cursor.getString(0);
                            item.email = cursor.getString(1);
                            item.avatar = cursor.getString(2);
                            item.times_contacted = (cursor.getInt(3) == 0 ? 0 : Integer.MAX_VALUE);
                            item.last_contacted = 0L;
                            EntityContact existing = map.get(item.email);
                            if (existing == null ||
                                    (existing.avatar == null && item.avatar != null))
                                map.put(item.email, item);
                        }
                    }

                    List<EntityContact> items = new ArrayList<>();
                    if (suggest_sent)
                        items.addAll(db.contact().searchContacts(null, EntityContact.TYPE_TO, wildcard));
                    if (suggest_received)
                        items.addAll(db.contact().searchContacts(null, EntityContact.TYPE_FROM, wildcard));
                    for (EntityContact item : items) {
                        EntityContact existing = map.get(item.email);
                        if (existing == null)
                            map.put(item.email, item);
                        else {
                            existing.times_contacted = Math.max(existing.times_contacted, item.times_contacted);
                            existing.last_contacted = Math.max(existing.last_contacted, item.last_contacted);
                        }
                    }

                    items = new ArrayList<>(map.values());

                    final Collator collator = Collator.getInstance(Locale.getDefault());
                    collator.setStrength(Collator.SECONDARY); // Case insensitive, process accents etc

                    Collections.sort(items, new Comparator<EntityContact>() {
                        @Override
                        public int compare(EntityContact i1, EntityContact i2) {
                            try {
                                if (suggest_frequently) {
                                    int t = -i1.times_contacted.compareTo(i2.times_contacted);
                                    if (t != 0)
                                        return t;

                                    int l = -i1.last_contacted.compareTo(i2.last_contacted);
                                    if (l != 0)
                                        return l;
                                } else {
                                    int a = -Boolean.compare(i1.id == 0, i2.id == 0);
                                    if (a != 0)
                                        return a;
                                }

                                if (TextUtils.isEmpty(i1.name) && TextUtils.isEmpty(i2.name))
                                    return 0;
                                if (TextUtils.isEmpty(i1.name) && !TextUtils.isEmpty(i2.name))
                                    return 1;
                                if (!TextUtils.isEmpty(i1.name) && TextUtils.isEmpty(i2.name))
                                    return -1;

                                int n = collator.compare(i1.name, i2.name);
                                if (n != 0)
                                    return n;

                                return collator.compare(i1.email, i2.email);
                            } catch (Throwable ex) {
                                Log.e(ex);
                                return 0;
                            }
                        }
                    });

                    for (int i = 0; i < items.size(); i++) {
                        EntityContact item = items.get(i);
                        result.newRow()
                                .add(i + 1) // id
                                .add(item.name)
                                .add(item.email)
                                .add(item.avatar)
                                .add(item.id == 0 ? 0 : 1);
                    }
                } catch (Throwable ex) {
                    Log.e(ex);
                }

                Log.i("Suggesting contacts=" + result.getCount());
                return result;
            }
        });

        etTo.setAdapter(cadapter);
        etCc.setAdapter(cadapter);
        etBcc.setAdapter(cadapter);

        etTo.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        etCc.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        etBcc.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        grpAddresses.setVisibility(cc_bcc ? View.VISIBLE : View.GONE);

        rvAttachment.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rvAttachment.setLayoutManager(llm);
        rvAttachment.setItemAnimator(null);

        adapter = new AdapterAttachment(this, false);
        rvAttachment.setAdapter(adapter);

        tvNoInternetAttachments.setVisibility(View.GONE);

        final String pkg = Helper.getOpenKeychainPackage(getContext());
        Log.i("PGP binding to " + pkg);
        pgpService = new OpenPgpServiceConnection(getContext(), pkg);
        pgpService.bindToService();

        return view;
    }

    private void onReferenceEdit() {
        PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(getContext(), getViewLifecycleOwner(), ibReferenceEdit);

        popupMenu.getMenu().add(Menu.NONE, R.string.title_edit_plain_text, 1, R.string.title_edit_plain_text);
        popupMenu.getMenu().add(Menu.NONE, R.string.title_edit_formatted_text, 2, R.string.title_edit_formatted_text);
        popupMenu.getMenu().add(Menu.NONE, R.string.title_delete, 3, R.string.title_delete);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.string.title_edit_plain_text:
                        convertRef(true);
                        return true;

                    case R.string.title_edit_formatted_text:
                        convertRef(false);
                        return true;

                    case R.string.title_delete:
                        deleteRef();
                        return true;

                    default:
                        return false;
                }
            }

            private void convertRef(boolean plain) {
                Bundle args = new Bundle();
                args.putLong("id", working);
                args.putBoolean("plain", plain);
                args.putString("body", HtmlHelper.toHtml(etBody.getText(), getContext()));

                new SimpleTask<String>() {
                    @Override
                    protected void onPreExecute(Bundle args) {
                        ibReferenceEdit.setEnabled(false);
                    }

                    @Override
                    protected void onPostExecute(Bundle args) {
                        ibReferenceEdit.setEnabled(true);
                    }

                    @Override
                    protected String onExecute(Context context, Bundle args) throws Throwable {
                        long id = args.getLong("id");
                        boolean plain = args.getBoolean("plain");
                        String body = args.getString("body");

                        File rfile = EntityMessage.getFile(context, id);
                        Document doc = JsoupEx.parse(rfile);
                        Elements ref = doc.select("div[fairemail=reference]");
                        ref.removeAttr("fairemail");

                        Document document = JsoupEx.parse(body);
                        if (plain) {
                            String text = HtmlHelper.getText(context, ref.outerHtml());
                            String[] line = text.split("\\r?\\n");
                            for (int i = 0; i < line.length; i++)
                                line[i] = Html.escapeHtml(line[i]);
                            Element p = document.createElement("p");
                            p.html(TextUtils.join("<br>", line));
                            document.body().appendChild(p);
                        } else {
                            Document d = HtmlHelper.sanitizeCompose(context, ref.outerHtml(), true);
                            Element b = d.body();
                            b.tagName("div");
                            document.body().appendChild(b);
                        }

                        return document.html();
                    }

                    @Override
                    protected void onExecuted(Bundle args, String html) {
                        Bundle extras = new Bundle();
                        extras.putString("html", html);
                        extras.putBoolean("show", true);
                        onAction(R.id.action_save, extras, "refedit");
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Log.unexpectedError(getParentFragmentManager(), ex);
                    }
                }.execute(FragmentCompose.this, args, "compose:convert");
            }

            private void deleteRef() {
                Bundle extras = new Bundle();
                extras.putString("html", HtmlHelper.toHtml(etBody.getText(), getContext()));
                extras.putBoolean("show", true);
                onAction(R.id.action_save, extras, "refdelete");
            }
        });

        popupMenu.show();
    }

    private void onReferenceImages() {
        show_images = true;
        Bundle extras = new Bundle();
        extras.putBoolean("show", true);
        onAction(R.id.action_save, extras, "refimages");
    }

    @Override
    public void onDestroyView() {
        adapter = null;

        if (pgpService != null && pgpService.isBound()) {
            Log.i("PGP unbinding");
            pgpService.unbindFromService();
        }
        pgpService = null;

        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putLong("fair:working", working);
        outState.putBoolean("fair:show_images", show_images);
        outState.putParcelable("fair:photo", photoURI);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        state = State.NONE;

        if (savedInstanceState == null) {
            if (working < 0) {
                Bundle a = getArguments();
                if (a == null) {
                    a = new Bundle();
                    a.putString("action", "new");
                    setArguments(a);
                }

                Bundle args = new Bundle();

                args.putString("action", a.getString("action"));
                args.putLong("id", a.getLong("id", -1));
                args.putLong("account", a.getLong("account", -1));
                args.putLong("identity", a.getLong("identity", -1));
                args.putLong("reference", a.getLong("reference", -1));
                args.putSerializable("ics", a.getSerializable("ics"));
                args.putString("status", a.getString("status"));
                args.putBoolean("raw", a.getBoolean("raw", false));
                args.putLong("answer", a.getLong("answer", -1));
                args.putString("to", a.getString("to"));
                args.putString("cc", a.getString("cc"));
                args.putString("bcc", a.getString("bcc"));
                args.putString("subject", a.getString("subject"));
                args.putString("body", a.getString("body"));
                args.putString("text", a.getString("text"));
                args.putString("selected", a.getString("selected"));

                if (a.containsKey("attachments")) {
                    args.putParcelableArrayList("attachments", a.getParcelableArrayList("attachments"));
                    a.remove("attachments");
                    setArguments(a);
                }

                draftLoader.execute(this, args, "compose:new");
            } else {
                Bundle args = new Bundle();
                args.putString("action", "edit");
                args.putLong("id", working);
                draftLoader.execute(this, args, "compose:edit");
            }
        } else {
            working = savedInstanceState.getLong("fair:working");
            show_images = savedInstanceState.getBoolean("fair:show_images");
            photoURI = savedInstanceState.getParcelable("fair:photo");

            Bundle args = new Bundle();
            args.putString("action", working < 0 ? "new" : "edit");
            args.putLong("id", working);
            draftLoader.execute(this, args, "compose:instance");
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        cm.registerNetworkCallback(builder.build(), networkCallback);

        if (!pgpService.isBound())
            pgpService.bindToService();
    }

    @Override
    public void onPause() {
        if (state == State.LOADED) {
            Bundle extras = new Bundle();
            extras.putBoolean("autosave", true);
            onAction(R.id.action_save, extras, "pause");
        }

        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        cm.unregisterNetworkCallback(networkCallback);

        super.onPause();
    }

    ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            check();
        }

        @Override
        public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
            check();
        }

        @Override
        public void onLost(Network network) {
            check();
        }

        private void check() {
            getMainHandler().post(new Runnable() {
                @Override
                public void run() {
                    if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                        checkInternet();
                }
            });
        }
    };

    private void checkInternet() {
        boolean suitable = ConnectionHelper.getNetworkState(getContext()).isSuitable();

        Boolean content = (Boolean) tvNoInternet.getTag();
        tvNoInternet.setVisibility(!suitable && content != null && !content ? View.VISIBLE : View.GONE);

        Boolean downloading = (Boolean) rvAttachment.getTag();
        tvNoInternetAttachments.setVisibility(!suitable && downloading != null && downloading ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_compose, menu);

        menu.findItem(R.id.menu_encrypt).setActionView(R.layout.action_button_text);
        ImageButton ib = menu.findItem(R.id.menu_encrypt).getActionView().findViewById(R.id.button);
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMenuEncrypt();
            }
        });
        ib.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int[] pos = new int[2];
                ib.getLocationOnScreen(pos);
                int dp24 = Helper.dp2pixels(v.getContext(), 24);

                Toast toast = ToastEx.makeTextBw(getContext(), getString(R.string.title_encrypt), Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP | Gravity.START, pos[0], pos[1] + dp24);
                toast.show();
                return true;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        menu.findItem(R.id.menu_encrypt).setEnabled(state == State.LOADED);
        menu.findItem(R.id.menu_zoom).setEnabled(state == State.LOADED);
        menu.findItem(R.id.menu_media).setEnabled(state == State.LOADED);
        menu.findItem(R.id.menu_compact).setEnabled(state == State.LOADED);
        menu.findItem(R.id.menu_contact_group).setEnabled(
                state == State.LOADED && hasPermission(Manifest.permission.READ_CONTACTS));
        menu.findItem(R.id.menu_answer).setEnabled(state == State.LOADED);
        menu.findItem(R.id.menu_clear).setEnabled(state == State.LOADED);

        int colorEncrypt = Helper.resolveColor(getContext(), R.attr.colorEncrypt);
        View v = menu.findItem(R.id.menu_encrypt).getActionView();
        ImageButton ib = v.findViewById(R.id.button);
        TextView tv = v.findViewById(R.id.text);
        ib.setEnabled(state == State.LOADED);
        if (EntityMessage.PGP_SIGNONLY.equals(encrypt) || EntityMessage.SMIME_SIGNONLY.equals(encrypt)) {
            ib.setImageResource(R.drawable.twotone_gesture_24);
            ib.setImageTintList(null);
            tv.setText(EntityMessage.PGP_SIGNONLY.equals(encrypt) ? "P" : "S");
        } else if (EntityMessage.PGP_SIGNENCRYPT.equals(encrypt) || EntityMessage.SMIME_SIGNENCRYPT.equals(encrypt)) {
            ib.setImageResource(R.drawable.twotone_lock_24);
            ib.setImageTintList(ColorStateList.valueOf(colorEncrypt));
            tv.setText(EntityMessage.PGP_SIGNENCRYPT.equals(encrypt) ? "P" : "S");
        } else {
            ib.setImageResource(R.drawable.twotone_lock_open_24);
            ib.setImageTintList(null);
            tv.setText(null);
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean save_drafts = prefs.getBoolean("save_drafts", true);
        boolean send_dialog = prefs.getBoolean("send_dialog", true);
        boolean image_dialog = prefs.getBoolean("image_dialog", true);

        menu.findItem(R.id.menu_save_drafts).setChecked(save_drafts);
        menu.findItem(R.id.menu_send_dialog).setChecked(send_dialog);
        menu.findItem(R.id.menu_image_dialog).setChecked(image_dialog);
        menu.findItem(R.id.menu_media).setChecked(media);
        menu.findItem(R.id.menu_compact).setChecked(compact);

        if (EntityMessage.PGP_SIGNONLY.equals(encrypt) ||
                EntityMessage.SMIME_SIGNONLY.equals(encrypt))
            bottom_navigation.getMenu().findItem(R.id.action_send).setTitle(R.string.title_sign);
        else if (EntityMessage.PGP_SIGNENCRYPT.equals(encrypt) ||
                EntityMessage.SMIME_SIGNENCRYPT.equals(encrypt))
            bottom_navigation.getMenu().findItem(R.id.action_send).setTitle(R.string.title_encrypt);
        else
            bottom_navigation.getMenu().findItem(R.id.action_send).setTitle(R.string.title_send);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_encrypt:
                onMenuEncrypt();
                return true;
            case R.id.menu_zoom:
                onMenuZoom();
                return true;
            case R.id.menu_save_drafts:
                onMenuSaveDrafts();
                return true;
            case R.id.menu_send_dialog:
                onMenuSendDialog();
                return true;
            case R.id.menu_image_dialog:
                onMenuImageDialog();
                return true;
            case R.id.menu_media:
                onMenuMediaBar();
                return true;
            case R.id.menu_compact:
                onMenuCompact();
                return true;
            case R.id.menu_contact_group:
                onMenuContactGroup();
                return true;
            case R.id.menu_answer:
                onMenuAnswer();
                return true;
            case R.id.menu_clear:
                StyleHelper.apply(R.id.menu_clear, null, etBody);
                return true;
            case R.id.menu_legend:
                onMenuLegend();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onMenuAddresses() {
        grpAddresses.setVisibility(grpAddresses.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);

        getMainHandler().post(new Runnable() {
            @Override
            public void run() {
                if (grpAddresses.getVisibility() == View.GONE)
                    etSubject.requestFocus();
                else
                    etCc.requestFocus();
            }
        });
    }

    private void onMenuEncrypt() {
        EntityIdentity identity = (EntityIdentity) spIdentity.getSelectedItem();
        if (identity == null || identity.encrypt == 0) {
            if (EntityMessage.ENCRYPT_NONE.equals(encrypt) || encrypt == null)
                encrypt = EntityMessage.PGP_SIGNENCRYPT;
            else if (EntityMessage.PGP_SIGNENCRYPT.equals(encrypt))
                encrypt = EntityMessage.PGP_SIGNONLY;
            else
                encrypt = EntityMessage.ENCRYPT_NONE;
        } else {
            if (EntityMessage.ENCRYPT_NONE.equals(encrypt) || encrypt == null)
                encrypt = EntityMessage.SMIME_SIGNENCRYPT;
            else if (EntityMessage.SMIME_SIGNENCRYPT.equals(encrypt))
                encrypt = EntityMessage.SMIME_SIGNONLY;
            else
                encrypt = EntityMessage.ENCRYPT_NONE;
        }

        getActivity().invalidateOptionsMenu();

        Bundle args = new Bundle();
        args.putLong("id", working);
        args.putInt("encrypt", encrypt);

        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) {
                long id = args.getLong("id");
                int encrypt = args.getInt("encrypt");

                DB db = DB.getInstance(context);
                if (EntityMessage.ENCRYPT_NONE.equals(encrypt))
                    db.message().setMessageUiEncrypt(id, null);
                else
                    db.message().setMessageUiEncrypt(id, encrypt);

                return null;
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "compose:encrypt");
    }

    private void onMenuZoom() {
        zoom = ++zoom % 3;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefs.edit().putInt("compose_zoom", zoom).apply();
        setZoom();
    }

    private void setZoom() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        int message_zoom = prefs.getInt("message_zoom", 100);
        float textSize = Helper.getTextSize(getContext(), zoom);
        if (textSize != 0) {
            etBody.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize * message_zoom / 100f);
            tvReference.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        }
    }

    private void onMenuSaveDrafts() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean save_drafts = prefs.getBoolean("save_drafts", true);
        prefs.edit().putBoolean("save_drafts", !save_drafts).apply();
    }

    private void onMenuSendDialog() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean send_dialog = prefs.getBoolean("send_dialog", true);
        prefs.edit().putBoolean("send_dialog", !send_dialog).apply();
    }

    private void onMenuImageDialog() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean image_dialog = prefs.getBoolean("image_dialog", true);
        prefs.edit().putBoolean("image_dialog", !image_dialog).apply();
    }

    private void onMenuMediaBar() {
        media = !media;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefs.edit().putBoolean("compose_media", media).apply();
        media_bar.setVisibility(media ? View.VISIBLE : View.GONE);
        media_bar.getMenu().clear();
        media_bar.inflateMenu(media && etBody.hasSelection() ? R.menu.action_compose_style_alt : R.menu.action_compose_media);
    }

    private void onMenuCompact() {
        compact = !compact;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefs.edit().putBoolean("compose_compact", compact).apply();
        setCompact(compact);
    }

    private void setCompact(boolean compact) {
        bottom_navigation.setLabelVisibilityMode(compact
                ? LabelVisibilityMode.LABEL_VISIBILITY_UNLABELED
                : LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
        ViewGroup.LayoutParams params = bottom_navigation.getLayoutParams();
        params.height = Helper.dp2pixels(view.getContext(), compact ? 36 : 56);
        bottom_navigation.setLayoutParams(params);
    }

    private void onMenuLegend() {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
            getParentFragmentManager().popBackStack("legend", FragmentManager.POP_BACK_STACK_INCLUSIVE);

        Bundle args = new Bundle();
        args.putString("tab", "compose");

        Fragment fragment = new FragmentLegend();
        fragment.setArguments(args);

        FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment).addToBackStack("legend");
        fragmentTransaction.commit();
    }

    private void onMenuContactGroup() {
        Bundle args = new Bundle();
        args.putLong("working", working);

        int focussed = 0;
        View v = view.findFocus();
        if (v != null) {
            if (v.getId() == R.id.etCc)
                focussed = 1;
            else if (v.getId() == R.id.etBcc)
                focussed = 2;
        }

        args.putInt("focussed", focussed);

        FragmentDialogContactGroup fragment = new FragmentDialogContactGroup();
        fragment.setArguments(args);
        fragment.setTargetFragment(this, REQUEST_CONTACT_GROUP);
        fragment.show(getParentFragmentManager(), "compose:groups");
    }

    private void onMenuAnswer() {
        new SimpleTask<List<EntityAnswer>>() {
            @Override
            protected List<EntityAnswer> onExecute(Context context, Bundle args) {
                List<EntityAnswer> answers = DB.getInstance(context).answer().getAnswers(false);
                return (answers == null ? new ArrayList<>() : answers);
            }

            @Override
            protected void onExecuted(Bundle args, final List<EntityAnswer> answers) {
                if (answers.size() == 0) {
                    ToastEx.makeText(getContext(), R.string.title_no_answers, Toast.LENGTH_LONG).show();
                    return;
                }

                View vwAnchorMenu = view.findViewById(R.id.vwAnchorMenu);
                PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(getContext(), getViewLifecycleOwner(), vwAnchorMenu);
                Menu main = popupMenu.getMenu();

                int order = 0;
                Map<String, SubMenu> map = new HashMap<>();
                for (EntityAnswer answer : answers) {
                    order++;
                    if (answer.group == null)
                        main.add(Menu.NONE, order, order++, answer.toString())
                                .setIntent(new Intent().putExtra("id", answer.id));
                    else {
                        if (!map.containsKey(answer.group))
                            map.put(answer.group, main.addSubMenu(Menu.NONE, order, order++, answer.group));
                        SubMenu smenu = map.get(answer.group);
                        smenu.add(Menu.NONE, smenu.size(), smenu.size() + 1, answer.toString())
                                .setIntent(new Intent().putExtra("id", answer.id));
                    }
                }

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem target) {
                        Intent intent = target.getIntent();
                        if (intent == null)
                            return false;

                        if (!ActivityBilling.isPro(getContext())) {
                            startActivity(new Intent(getContext(), ActivityBilling.class));
                            return true;
                        }

                        long id = intent.getLongExtra("id", -1);
                        for (EntityAnswer answer : answers)
                            if (answer.id.equals(id)) {
                                if (etSubject.getText().length() == 0)
                                    etSubject.setText(answer.name);

                                InternetAddress[] to = null;
                                try {
                                    to = InternetAddress.parseHeader(etTo.getText().toString(), false);
                                } catch (AddressException ignored) {
                                }

                                String html = EntityAnswer.replacePlaceholders(answer.text, to);

                                Spanned spanned = HtmlHelper.fromHtml(html, false, new Html.ImageGetter() {
                                    @Override
                                    public Drawable getDrawable(String source) {
                                        return ImageHelper.decodeImage(getContext(), working, source, true, zoom, 1.0f, etBody);
                                    }
                                }, null, getContext());

                                int start = etBody.getSelectionStart();
                                int end = etBody.getSelectionEnd();
                                if (start > end) {
                                    int tmp = start;
                                    start = end;
                                    end = tmp;
                                }

                                if (start >= 0 && start < end)
                                    etBody.getText().replace(start, end, spanned);
                                else {
                                    if (start < 0) {
                                        start = etBody.length() - 1;
                                        if (start < 0)
                                            start = 0;
                                    }

                                    etBody.getText().insert(start, spanned);
                                }

                                return true;
                            }

                        Log.e("Answer=" + id + " count=" + answers.size() + " not found");

                        return false;
                    }
                });

                popupMenu.show();
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(getContext(), getViewLifecycleOwner(), new Bundle(), "compose:answer");
    }

    private boolean onActionStyle(int action, View anchor) {
        Log.i("Style action=" + action);
        return StyleHelper.apply(action, anchor, etBody);
    }

    private void onActionRecordAudio() {
        // https://developer.android.com/reference/android/provider/MediaStore.Audio.Media.html#RECORD_SOUND_ACTION
        PackageManager pm = getContext().getPackageManager();
        Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
        if (intent.resolveActivity(pm) == null) { // action whitelisted
            Snackbar snackbar = Snackbar.make(view, getString(R.string.title_no_recorder), Snackbar.LENGTH_INDEFINITE)
                    .setGestureInsetBottomIgnored(true);
            snackbar.setAction(R.string.title_fix, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Helper.viewFAQ(getContext(), 158);
                }
            });
            snackbar.show();
        } else
            try {
                startActivityForResult(intent, REQUEST_RECORD_AUDIO);
            } catch (SecurityException ex) {
                Log.w(ex);
                Snackbar.make(view, getString(R.string.title_no_viewer, intent), Snackbar.LENGTH_INDEFINITE)
                        .setGestureInsetBottomIgnored(true).show();
            }
    }

    private void onActionImage(boolean photo) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean image_dialog = prefs.getBoolean("image_dialog", true);
        if (image_dialog) {
            Bundle args = new Bundle();
            args.putInt("title", photo
                    ? R.string.title_attachment_photo
                    : R.string.title_add_image_select);
            FragmentDialogAddImage fragment = new FragmentDialogAddImage();
            fragment.setArguments(args);
            fragment.setTargetFragment(this, REQUEST_IMAGE);
            fragment.show(getParentFragmentManager(), "compose:image");
        } else
            onAddImage(photo);
    }

    private void onActionAttachment() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        PackageManager pm = getContext().getPackageManager();
        if (intent.resolveActivity(pm) == null) // system whitelisted
            noStorageAccessFramework();
        else
            startActivityForResult(Helper.getChooser(getContext(), intent), REQUEST_ATTACHMENT);
    }

    private void noStorageAccessFramework() {
        Snackbar snackbar = Snackbar.make(view, R.string.title_no_saf, Snackbar.LENGTH_LONG)
                .setGestureInsetBottomIgnored(true);
        snackbar.setAction(R.string.title_fix, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(getContext(), 25);
            }
        });
        snackbar.show();
    }

    private void onActionLink() {
        Uri uri = null;

        if (etBody.hasSelection()) {
            int start = etBody.getSelectionStart();
            URLSpan[] spans = etBody.getText().getSpans(start, start, URLSpan.class);
            if (spans.length > 0) {
                String url = spans[0].getURL();
                if (url != null) {
                    uri = Uri.parse(url);
                    if (uri.getScheme() == null)
                        uri = null;
                }
            }
        }

        if (uri == null) {
            ClipboardManager cbm = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            if (cbm != null && cbm.hasPrimaryClip()) {
                String link = cbm.getPrimaryClip().getItemAt(0).coerceToText(getContext()).toString();
                uri = Uri.parse(link);
                if (uri.getScheme() == null)
                    uri = null;
            }
        }

        Bundle args = new Bundle();
        args.putParcelable("uri", uri);
        args.putInt("start", etBody.getSelectionStart());
        args.putInt("end", etBody.getSelectionEnd());

        FragmentDialogLink fragment = new FragmentDialogLink();
        fragment.setArguments(args);
        fragment.setTargetFragment(this, REQUEST_LINK);
        fragment.show(getParentFragmentManager(), "compose:link");
    }

    private void onActionDiscard() {
        if (isEmpty())
            onAction(R.id.action_delete, "discard");
        else {
            Bundle args = new Bundle();
            args.putString("question", getString(R.string.title_ask_discard));

            FragmentDialogAsk fragment = new FragmentDialogAsk();
            fragment.setArguments(args);
            fragment.setTargetFragment(this, REQUEST_DISCARD);
            fragment.show(getParentFragmentManager(), "compose:discard");
        }
    }

    private void onEncrypt(final EntityMessage draft, final int action, final Bundle extras, final boolean interactive) {
        if (EntityMessage.SMIME_SIGNONLY.equals(draft.ui_encrypt) ||
                EntityMessage.SMIME_SIGNENCRYPT.equals(draft.ui_encrypt)) {
            Bundle args = new Bundle();
            args.putLong("id", draft.id);
            args.putInt("type", draft.ui_encrypt);

            new SimpleTask<EntityIdentity>() {
                @Override
                protected EntityIdentity onExecute(Context context, Bundle args) throws KeyChainException, InterruptedException {
                    long id = args.getLong("id");

                    DB db = DB.getInstance(context);
                    EntityMessage draft = db.message().getMessage(id);
                    if (draft == null || draft.identity == null)
                        return null;

                    EntityIdentity identity = db.identity().getIdentity(draft.identity);
                    if (identity != null && identity.sign_key_alias != null)
                        try {
                            PrivateKey key = KeyChain.getPrivateKey(context, identity.sign_key_alias);
                            args.putBoolean("available", key != null);
                        } catch (Throwable ex) {
                            Log.w(ex);
                        }

                    return identity;
                }

                @Override
                protected void onExecuted(final Bundle args, EntityIdentity identity) {
                    if (identity == null)
                        return;

                    boolean available = args.getBoolean("available");
                    if (available) {
                        args.putString("alias", identity.sign_key_alias);
                        onSmime(args, action, extras);
                        return;
                    }

                    if (interactive)
                        Helper.selectKeyAlias(getActivity(), getViewLifecycleOwner(), identity.sign_key_alias, new Helper.IKeyAlias() {
                            @Override
                            public void onSelected(String alias) {
                                args.putString("alias", alias);
                                if (alias != null)
                                    onSmime(args, action, extras);
                            }

                            @Override
                            public void onNothingSelected() {
                                Snackbar snackbar = Snackbar.make(view, R.string.title_no_key, Snackbar.LENGTH_LONG)
                                        .setGestureInsetBottomIgnored(true);
                                final Intent intent = (Build.VERSION.SDK_INT < Build.VERSION_CODES.R
                                        ? KeyChain.createInstallIntent()
                                        : new Intent(Settings.ACTION_SECURITY_SETTINGS));
                                PackageManager pm = getContext().getPackageManager();
                                if (intent.resolveActivity(pm) != null) // package whitelisted
                                    snackbar.setAction(R.string.title_fix, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            startActivity(intent);
                                        }
                                    });
                                snackbar.show();
                            }
                        });
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(getParentFragmentManager(), ex);
                }
            }.execute(this, args, "compose:alias");
        } else {
            if (pgpService.isBound())
                try {
                    List<Address> recipients = new ArrayList<>();
                    if (draft.from != null)
                        recipients.addAll(Arrays.asList(draft.from));
                    if (draft.to != null)
                        recipients.addAll(Arrays.asList(draft.to));
                    if (draft.cc != null)
                        recipients.addAll(Arrays.asList(draft.cc));
                    if (draft.bcc != null)
                        recipients.addAll(Arrays.asList(draft.bcc));

                    if (recipients.size() == 0)
                        throw new IllegalArgumentException(getString(R.string.title_to_missing));

                    List<String> emails = new ArrayList<>();
                    for (int i = 0; i < recipients.size(); i++) {
                        InternetAddress recipient = (InternetAddress) recipients.get(i);
                        String email = recipient.getAddress().toLowerCase();
                        if (!emails.contains(email))
                            emails.add(email);
                    }
                    pgpUserIds = emails.toArray(new String[0]);

                    Intent intent;
                    if (EntityMessage.PGP_SIGNONLY.equals(draft.ui_encrypt))
                        intent = new Intent(OpenPgpApi.ACTION_GET_SIGN_KEY_ID);
                    else if (EntityMessage.PGP_SIGNENCRYPT.equals(draft.ui_encrypt)) {
                        intent = new Intent(OpenPgpApi.ACTION_GET_KEY_IDS);
                        intent.putExtra(OpenPgpApi.EXTRA_USER_IDS, pgpUserIds);
                    } else
                        throw new IllegalArgumentException("Invalid encrypt=" + draft.ui_encrypt);

                    Bundle largs = new Bundle();
                    largs.putLong("id", working);
                    largs.putInt("action", action);
                    largs.putBundle("extras", extras);
                    largs.putBoolean("interactive", interactive);
                    intent.putExtra(BuildConfig.APPLICATION_ID, largs);

                    onPgp(intent);
                } catch (Throwable ex) {
                    if (ex instanceof IllegalArgumentException)
                        Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG)
                                .setGestureInsetBottomIgnored(true).show();
                    else {
                        Log.e(ex);
                        Log.unexpectedError(getParentFragmentManager(), ex);
                    }
                }
            else {
                Snackbar snackbar = Snackbar.make(view, R.string.title_no_openpgp, Snackbar.LENGTH_LONG)
                        .setGestureInsetBottomIgnored(true);
                snackbar.setAction(R.string.title_fix, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Helper.viewFAQ(getContext(), 12);
                    }
                });
                snackbar.show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            switch (requestCode) {
                case REQUEST_CONTACT_TO:
                case REQUEST_CONTACT_CC:
                case REQUEST_CONTACT_BCC:
                    if (resultCode == RESULT_OK && data != null)
                        onPickContact(requestCode, data);
                    break;
                case REQUEST_SHARED:
                    if (resultCode == RESULT_OK && data != null) {
                        Bundle args = data.getBundleExtra("args");
                        onAddImageFile(args.getParcelableArrayList("images"));
                    }
                    break;
                case REQUEST_IMAGE:
                    if (resultCode == RESULT_OK) {
                        int title = data.getBundleExtra("args").getInt("title");
                        onAddImage(title == R.string.title_attachment_photo);
                    }
                    break;
                case REQUEST_IMAGE_FILE:
                    if (resultCode == RESULT_OK && data != null)
                        onAddImageFile(getUris(data));
                    break;
                case REQUEST_TAKE_PHOTO:
                    if (resultCode == RESULT_OK) {
                        if (photoURI != null)
                            onAddImageFile(Arrays.asList(photoURI));
                    }
                    break;
                case REQUEST_ATTACHMENT:
                case REQUEST_RECORD_AUDIO:
                    if (resultCode == RESULT_OK && data != null)
                        onAddAttachment(getUris(data), false, 0, false);
                    break;
                case REQUEST_OPENPGP:
                    if (resultCode == RESULT_OK && data != null)
                        onPgp(data);
                    break;
                case REQUEST_CONTACT_GROUP:
                    if (resultCode == RESULT_OK && data != null)
                        onContactGroupSelected(data.getBundleExtra("args"));
                    break;
                case REQUEST_LINK:
                    if (resultCode == RESULT_OK && data != null)
                        onLinkSelected(data.getBundleExtra("args"));
                    break;
                case REQUEST_DISCARD:
                    if (resultCode == RESULT_OK)
                        onActionDiscardConfirmed();
                    break;
                case REQUEST_SEND:
                    if (resultCode == RESULT_OK)
                        onAction(R.id.action_send, "send");
                    else if (resultCode == RESULT_FIRST_USER) {
                        Bundle extras = new Bundle();
                        extras.putBoolean("now", true);
                        onAction(R.id.action_send, extras, "sendnow");
                    }
                    break;
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    private void onPickContact(int requestCode, Intent data) {
        Uri uri = data.getData();
        if (uri == null)
            return;

        Bundle args = new Bundle();
        args.putLong("id", working);
        args.putInt("requestCode", requestCode);
        args.putParcelable("uri", uri);

        new SimpleTask<EntityMessage>() {
            @Override
            protected EntityMessage onExecute(Context context, Bundle args) throws Throwable {
                long id = args.getLong("id");
                int requestCode = args.getInt("requestCode");
                Uri uri = args.getParcelable("uri");

                EntityMessage draft = null;
                DB db = DB.getInstance(context);

                try (Cursor cursor = context.getContentResolver().query(
                        uri,
                        new String[]{
                                ContactsContract.CommonDataKinds.Email.ADDRESS,
                                ContactsContract.Contacts.DISPLAY_NAME
                        },
                        null, null, null)) {
                    if (cursor != null && cursor.moveToFirst()) {
                        int colEmail = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS);
                        int colName = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                        String email = MessageHelper.sanitizeEmail(cursor.getString(colEmail));
                        String name = cursor.getString(colName);

                        try {
                            db.beginTransaction();

                            draft = db.message().getMessage(id);
                            if (draft == null)
                                return null;

                            Address[] address = null;
                            if (requestCode == REQUEST_CONTACT_TO)
                                address = draft.to;
                            else if (requestCode == REQUEST_CONTACT_CC)
                                address = draft.cc;
                            else if (requestCode == REQUEST_CONTACT_BCC)
                                address = draft.bcc;

                            List<Address> list = new ArrayList<>();
                            if (address != null)
                                list.addAll(Arrays.asList(address));

                            list.add(new InternetAddress(email, name, StandardCharsets.UTF_8.name()));

                            if (requestCode == REQUEST_CONTACT_TO)
                                draft.to = list.toArray(new Address[0]);
                            else if (requestCode == REQUEST_CONTACT_CC)
                                draft.cc = list.toArray(new Address[0]);
                            else if (requestCode == REQUEST_CONTACT_BCC)
                                draft.bcc = list.toArray(new Address[0]);

                            db.message().updateMessage(draft);

                            db.setTransactionSuccessful();
                        } finally {
                            db.endTransaction();
                        }
                    }
                }

                return draft;
            }

            @Override
            protected void onExecuted(Bundle args, EntityMessage draft) {
                if (draft != null) {
                    etTo.setText(MessageHelper.formatAddressesCompose(draft.to));
                    etCc.setText(MessageHelper.formatAddressesCompose(draft.cc));
                    etBcc.setText(MessageHelper.formatAddressesCompose(draft.bcc));
                }
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "compose:picked");
    }

    private void onAddImage(boolean photo) {
        if (photo) {
            // https://developer.android.com/training/camera/photobasics
            PackageManager pm = getContext().getPackageManager();
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(pm) == null) { // action whitelisted
                Snackbar snackbar = Snackbar.make(view, getString(R.string.title_no_camera), Snackbar.LENGTH_LONG)
                        .setGestureInsetBottomIgnored(true);
                snackbar.setAction(R.string.title_fix, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Helper.viewFAQ(getContext(), 158);
                    }
                });
                snackbar.show();
            } else {
                File dir = new File(getContext().getCacheDir(), "photo");
                if (!dir.exists())
                    dir.mkdir();
                File file = new File(dir, working + ".jpg");
                try {
                    photoURI = FileProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID, file);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(intent, REQUEST_TAKE_PHOTO);
                } catch (SecurityException ex) {
                    Log.w(ex);
                    Snackbar.make(view, getString(R.string.title_no_viewer, intent), Snackbar.LENGTH_LONG)
                            .setGestureInsetBottomIgnored(true).show();
                }
            }
        } else {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            PackageManager pm = getContext().getPackageManager();
            if (intent.resolveActivity(pm) == null) // GET_CONTENT whitelisted
                noStorageAccessFramework();
            else
                startActivityForResult(Helper.getChooser(getContext(), intent), REQUEST_IMAGE_FILE);
        }
    }

    private void onAddImageFile(List<Uri> uri) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean add_inline = prefs.getBoolean("add_inline", true);
        boolean resize_images = prefs.getBoolean("resize_images", true);
        boolean privacy_images = prefs.getBoolean("privacy_images", false);
        int resize = prefs.getInt("resize", FragmentCompose.REDUCED_IMAGE_SIZE);
        onAddAttachment(uri, add_inline, resize_images ? resize : 0, privacy_images);
    }

    private void onAddAttachment(List<Uri> uris, boolean image, int resize, boolean privacy) {
        Bundle args = new Bundle();
        args.putLong("id", working);
        args.putParcelableArrayList("uris", new ArrayList<>(uris));
        args.putBoolean("image", image);
        args.putInt("resize", resize);
        args.putBoolean("privacy", privacy);
        args.putCharSequence("body", etBody.getText());
        args.putInt("start", etBody.getSelectionStart());

        new SimpleTask<Spanned>() {
            @Override
            protected Spanned onExecute(Context context, Bundle args) throws IOException {
                final long id = args.getLong("id");
                List<Uri> uris = args.getParcelableArrayList("uris");
                boolean image = args.getBoolean("image");
                int resize = args.getInt("resize");
                boolean privacy = args.getBoolean("privacy");
                CharSequence body = args.getCharSequence("body");
                int start = args.getInt("start");

                SpannableStringBuilder s = new SpannableStringBuilder(body);
                if (start < 0)
                    start = 0;
                if (start > s.length())
                    start = s.length();

                for (Uri uri : uris) {
                    EntityAttachment attachment = addAttachment(context, id, uri, image, resize, privacy);
                    if (attachment == null)
                        continue;
                    if (!image)
                        continue;

                    File file = attachment.getFile(context);
                    Uri cid = Uri.parse("cid:" + BuildConfig.APPLICATION_ID + "." + attachment.id);

                    Drawable d = Drawable.createFromPath(file.getAbsolutePath());
                    if (d == null)
                        throw new IllegalArgumentException(context.getString(R.string.title_no_image));

                    s.insert(start, "\n\uFFFC\n"); // Object replacement character
                    ImageSpan is = new ImageSpan(context, cid);
                    s.setSpan(is, start + 1, start + 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    start += 3;
                }

                if (!image)
                    return null;

                DB db = DB.getInstance(context);
                db.message().setMessagePlainOnly(id, false);

                args.putInt("start", start);

                // TODO: double conversion
                return HtmlHelper.fromHtml(HtmlHelper.toHtml(s, context), false, new Html.ImageGetter() {
                    @Override
                    public Drawable getDrawable(String source) {
                        return ImageHelper.decodeImage(context, id, source, true, zoom, 1.0f, etBody);
                    }
                }, null, getContext());
            }

            @Override
            protected void onExecuted(Bundle args, final Spanned body) {
                if (body != null) {
                    int start = args.getInt("start");

                    etBody.setText(body);
                    if (start < body.length())
                        etBody.setSelection(start);

                    // Save text with image
                    onAction(R.id.action_save, "image");
                }
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                // External app sending absolute file
                if (ex instanceof SecurityException)
                    handleFileShare();
                else if (ex instanceof IllegalArgumentException || ex instanceof FileNotFoundException)
                    Snackbar.make(view, ex.toString(), Snackbar.LENGTH_LONG)
                            .setGestureInsetBottomIgnored(true).show();
                else
                    Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "compose:attachment:add");
    }

    private List<Uri> getUris(Intent data) {
        List<Uri> result = new ArrayList<>();

        ClipData clipData = data.getClipData();
        if (clipData == null) {
            Uri uri = data.getData();
            if (uri != null)
                result.add(uri);
        } else {
            for (int i = 0; i < clipData.getItemCount(); i++) {
                ClipData.Item item = clipData.getItemAt(i);
                Uri uri = item.getUri();
                if (uri != null)
                    result.add(uri);
            }
        }

        return result;
    }

    private void onPgp(Intent data) {
        final Bundle args = new Bundle();
        args.putParcelable("data", data);

        new SimpleTask<Object>() {
            @Override
            protected Object onExecute(Context context, Bundle args) throws Throwable {
                // Get arguments
                Intent data = args.getParcelable("data");
                Bundle largs = data.getBundleExtra(BuildConfig.APPLICATION_ID);
                long id = largs.getLong("id", -1);

                DB db = DB.getInstance(context);

                // Get data
                EntityMessage draft = db.message().getMessage(id);
                if (draft == null)
                    throw new MessageRemovedException("PGP");
                if (draft.identity == null)
                    throw new IllegalArgumentException(context.getString(R.string.title_from_missing));
                EntityIdentity identity = db.identity().getIdentity(draft.identity);
                if (identity == null)
                    throw new IllegalArgumentException(context.getString(R.string.title_from_missing));

                // Create files
                File input = new File(context.getCacheDir(), "pgp_input." + draft.id);
                File output = new File(context.getCacheDir(), "pgp_output." + draft.id);

                // Serializing messages is NOT reproducible
                if ((EntityMessage.PGP_SIGNONLY.equals(draft.ui_encrypt) &&
                        OpenPgpApi.ACTION_GET_SIGN_KEY_ID.equals(data.getAction())) ||
                        (EntityMessage.PGP_SIGNENCRYPT.equals(draft.ui_encrypt) &&
                                OpenPgpApi.ACTION_GET_KEY_IDS.equals(data.getAction()))) {
                    // Get/clean attachments
                    List<EntityAttachment> attachments = db.attachment().getAttachments(draft.id);
                    for (EntityAttachment attachment : new ArrayList<>(attachments))
                        if (attachment.isEncryption()) {
                            db.attachment().deleteAttachment(attachment.id);
                            attachments.remove(attachment);
                        }

                    // Build message
                    Properties props = MessageHelper.getSessionProperties();
                    Session isession = Session.getInstance(props, null);
                    MimeMessage imessage = new MimeMessage(isession);
                    MessageHelper.build(context, draft, attachments, identity, true, imessage);

                    if (OpenPgpApi.ACTION_GET_SIGN_KEY_ID.equals(data.getAction())) {
                        // Serialize content
                        imessage.saveChanges();
                        BodyPart bpContent = new MimeBodyPart() {
                            @Override
                            public void setContent(Object content, String type) throws MessagingException {
                                super.setContent(content, type);

                                // https://javaee.github.io/javamail/FAQ#howencode
                                updateHeaders();
                                if (content instanceof Multipart) {
                                    try {
                                        MessageHelper.overrideContentTransferEncoding((Multipart) content);
                                    } catch (IOException ex) {
                                        Log.e(ex);
                                    }
                                } else
                                    setHeader("Content-Transfer-Encoding", "base64");
                            }
                        };
                        bpContent.setContent(imessage.getContent(), imessage.getContentType());

                        try (OutputStream out = new FileOutputStream(input)) {
                            bpContent.writeTo(out);
                        }
                    } else {
                        // Serialize message
                        try (OutputStream out = new FileOutputStream(input)) {
                            imessage.writeTo(out);
                        }
                    }
                }

                Intent result;
                if (OpenPgpApi.ACTION_GET_SIGN_KEY_ID.equals(data.getAction()) && identity.sign_key != null) {
                    // Short circuit
                    result = data;
                    result.putExtra(OpenPgpApi.RESULT_CODE, OpenPgpApi.RESULT_CODE_SUCCESS);
                    result.putExtra(OpenPgpApi.EXTRA_SIGN_KEY_ID, identity.sign_key);
                } else {
                    // Call OpenPGP
                    Log.i("Executing " + data.getAction());
                    Log.logExtras(data);
                    OpenPgpApi api = new OpenPgpApi(context, pgpService.getService());
                    result = api.executeApi(data, new FileInputStream(input), new FileOutputStream(output));
                }

                // Process result
                try {
                    int resultCode = result.getIntExtra(OpenPgpApi.RESULT_CODE, OpenPgpApi.RESULT_CODE_ERROR);
                    Log.i("Result action=" + data.getAction() + " code=" + resultCode);
                    Log.logExtras(data);
                    switch (resultCode) {
                        case OpenPgpApi.RESULT_CODE_SUCCESS:
                            // Attach key, signed/encrypted data
                            if (OpenPgpApi.ACTION_GET_KEY.equals(data.getAction()) ||
                                    OpenPgpApi.ACTION_DETACHED_SIGN.equals(data.getAction()) ||
                                    OpenPgpApi.ACTION_SIGN_AND_ENCRYPT.equals(data.getAction()))
                                try {
                                    db.beginTransaction();

                                    String name;
                                    ContentType ct = new ContentType("application/octet-stream");
                                    int encryption;
                                    if (OpenPgpApi.ACTION_GET_KEY.equals(data.getAction())) {
                                        name = "keydata.asc";
                                        encryption = EntityAttachment.PGP_KEY;
                                    } else if (OpenPgpApi.ACTION_DETACHED_SIGN.equals(data.getAction())) {
                                        name = "signature.asc";
                                        encryption = EntityAttachment.PGP_SIGNATURE;
                                        String micalg = result.getStringExtra(OpenPgpApi.RESULT_SIGNATURE_MICALG);
                                        if (TextUtils.isEmpty(micalg))
                                            throw new IllegalArgumentException("micalg missing");
                                        ct = new ContentType("application/pgp-signature");
                                        ct.setParameter("micalg", micalg);
                                    } else if (OpenPgpApi.ACTION_SIGN_AND_ENCRYPT.equals(data.getAction())) {
                                        name = "encrypted.asc";
                                        encryption = EntityAttachment.PGP_MESSAGE;
                                    } else
                                        throw new IllegalStateException(data.getAction());

                                    EntityAttachment attachment = new EntityAttachment();
                                    attachment.message = draft.id;
                                    attachment.sequence = db.attachment().getAttachmentSequence(draft.id) + 1;
                                    attachment.name = name;
                                    attachment.type = ct.toString();
                                    attachment.disposition = Part.INLINE;
                                    attachment.encryption = encryption;
                                    attachment.id = db.attachment().insertAttachment(attachment);

                                    File file = attachment.getFile(context);

                                    if (OpenPgpApi.ACTION_DETACHED_SIGN.equals(data.getAction())) {
                                        byte[] bytes = result.getByteArrayExtra(OpenPgpApi.RESULT_DETACHED_SIGNATURE);
                                        Log.i("Writing " + file + " size=" + bytes.length);
                                        try (OutputStream out = new FileOutputStream(file)) {
                                            out.write(bytes);
                                        }
                                        db.attachment().setDownloaded(attachment.id, (long) bytes.length);
                                    } else {
                                        Log.i("Writing " + file + " size=" + output.length());
                                        Helper.copy(output, file);
                                        db.attachment().setDownloaded(attachment.id, file.length());
                                    }

                                    db.setTransactionSuccessful();
                                } finally {
                                    db.endTransaction();
                                }

                            // Sign-only: [get sign key id], get key, detached sign
                            // Sign/encrypt: get key ids, [get sign key id], get key, sign and encrypt

                            if (OpenPgpApi.ACTION_GET_KEY_IDS.equals(data.getAction())) {
                                // Sign/encrypt
                                pgpKeyIds = result.getLongArrayExtra(OpenPgpApi.EXTRA_KEY_IDS);
                                Log.i("Keys=" + pgpKeyIds.length);
                                if (pgpKeyIds.length == 0) // One key can be for multiple users
                                    throw new IllegalArgumentException(context.getString(R.string.title_key_missing,
                                            TextUtils.join(", ", pgpUserIds)));

                                if (identity.sign_key != null) {
                                    pgpSignKeyId = identity.sign_key;

                                    // Get public key
                                    Intent intent = new Intent(OpenPgpApi.ACTION_GET_KEY);
                                    intent.putExtra(OpenPgpApi.EXTRA_KEY_ID, pgpSignKeyId);
                                    intent.putExtra(OpenPgpApi.EXTRA_REQUEST_ASCII_ARMOR, true);
                                    intent.putExtra(BuildConfig.APPLICATION_ID, largs);
                                    return intent;
                                } else {
                                    // Get sign key
                                    Intent intent = new Intent(OpenPgpApi.ACTION_GET_SIGN_KEY_ID);
                                    intent.putExtra(BuildConfig.APPLICATION_ID, largs);
                                    return intent;
                                }
                            } else if (OpenPgpApi.ACTION_GET_SIGN_KEY_ID.equals(data.getAction())) {
                                pgpSignKeyId = result.getLongExtra(OpenPgpApi.EXTRA_SIGN_KEY_ID, -1);
                                if (pgpSignKeyId == 0)
                                    throw new IllegalArgumentException(context.getString(R.string.title_no_sign_key));
                                db.identity().setIdentitySignKey(identity.id, pgpSignKeyId);

                                // Get public key
                                Intent intent = new Intent(OpenPgpApi.ACTION_GET_KEY);
                                intent.putExtra(OpenPgpApi.EXTRA_KEY_ID, pgpSignKeyId);
                                intent.putExtra(OpenPgpApi.EXTRA_REQUEST_ASCII_ARMOR, true);
                                intent.putExtra(BuildConfig.APPLICATION_ID, largs);
                                return intent;
                            } else if (OpenPgpApi.ACTION_GET_KEY.equals(data.getAction())) {
                                if (EntityMessage.PGP_SIGNONLY.equals(draft.ui_encrypt)) {
                                    // Get signature
                                    Intent intent = new Intent(OpenPgpApi.ACTION_DETACHED_SIGN);
                                    intent.putExtra(OpenPgpApi.EXTRA_SIGN_KEY_ID, pgpSignKeyId);
                                    intent.putExtra(OpenPgpApi.EXTRA_REQUEST_ASCII_ARMOR, true);
                                    intent.putExtra(BuildConfig.APPLICATION_ID, largs);
                                    return intent;
                                } else if (EntityMessage.PGP_SIGNENCRYPT.equals(draft.ui_encrypt)) {
                                    // Encrypt message
                                    Intent intent = new Intent(OpenPgpApi.ACTION_SIGN_AND_ENCRYPT);
                                    intent.putExtra(OpenPgpApi.EXTRA_KEY_IDS, pgpKeyIds);
                                    intent.putExtra(OpenPgpApi.EXTRA_SIGN_KEY_ID, pgpSignKeyId);
                                    intent.putExtra(OpenPgpApi.EXTRA_REQUEST_ASCII_ARMOR, true);
                                    intent.putExtra(BuildConfig.APPLICATION_ID, largs);
                                    return intent;
                                } else
                                    throw new IllegalArgumentException("Invalid encrypt=" + draft.ui_encrypt);
                            } else if (OpenPgpApi.ACTION_DETACHED_SIGN.equals(data.getAction())) {
                                EntityAttachment attachment = new EntityAttachment();
                                attachment.message = draft.id;
                                attachment.sequence = db.attachment().getAttachmentSequence(draft.id) + 1;
                                attachment.name = "content.asc";
                                attachment.type = "text/plain";
                                attachment.disposition = Part.INLINE;
                                attachment.encryption = EntityAttachment.PGP_CONTENT;
                                attachment.id = db.attachment().insertAttachment(attachment);

                                File file = attachment.getFile(context);
                                input.renameTo(file);

                                db.attachment().setDownloaded(attachment.id, file.length());

                                // send message
                                args.putInt("action", largs.getInt("action"));
                                args.putBundle("extras", largs.getBundle("extras"));
                                return null;
                            } else if (OpenPgpApi.ACTION_SIGN_AND_ENCRYPT.equals(data.getAction())) {
                                input.delete();

                                // send message
                                args.putInt("action", largs.getInt("action"));
                                args.putBundle("extras", largs.getBundle("extras"));
                                return null;
                            } else
                                throw new IllegalStateException("Unknown action=" + data.getAction());

                        case OpenPgpApi.RESULT_CODE_USER_INTERACTION_REQUIRED:
                            args.putBoolean("interactive", largs.getBoolean("interactive"));
                            return result.getParcelableExtra(OpenPgpApi.RESULT_INTENT);

                        case OpenPgpApi.RESULT_CODE_ERROR:
                            input.delete();
                            db.identity().setIdentitySignKey(identity.id, null);
                            OpenPgpError error = result.getParcelableExtra(OpenPgpApi.RESULT_ERROR);
                            throw new IllegalArgumentException(
                                    "OpenPgp" +
                                            " error " + (error == null ? "?" : error.getErrorId()) +
                                            ": " + (error == null ? "?" : error.getMessage()));

                        default:
                            throw new IllegalStateException("OpenPgp unknown result code=" + resultCode);
                    }
                } finally {
                    output.delete();
                }
            }

            @Override
            protected void onExecuted(Bundle args, Object result) {
                Log.i("Result= " + result);
                if (result == null) {
                    int action = args.getInt("action");
                    Bundle extras = args.getBundle("extras");
                    extras.putBoolean("encrypted", true);
                    onAction(action, extras, "pgp");
                } else if (result instanceof Intent) {
                    Intent intent = (Intent) result;
                    onPgp(intent);
                } else if (result instanceof PendingIntent)
                    if (args.getBoolean("interactive"))
                        try {
                            ToastEx.makeText(getContext(), R.string.title_user_interaction, Toast.LENGTH_SHORT).show();
                            PendingIntent pi = (PendingIntent) result;
                            startIntentSenderForResult(
                                    pi.getIntentSender(),
                                    REQUEST_OPENPGP,
                                    null, 0, 0, 0, null);
                        } catch (IntentSender.SendIntentException ex) {
                            Log.e(ex);
                            Log.unexpectedError(getParentFragmentManager(), ex);
                        }
                    else {
                        if (BuildConfig.DEBUG)
                            ToastEx.makeText(getContext(), "Non interactive", Toast.LENGTH_SHORT).show();
                    }
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (ex instanceof IllegalArgumentException
                        || ex instanceof GeneralSecurityException /* InvalidKeyException */) {
                    Log.i(ex);
                    Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG)
                            .setGestureInsetBottomIgnored(true).show();
                } else
                    Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "compose:pgp");
    }

    private void onSmime(Bundle args, final int action, final Bundle extras) {
        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) throws Throwable {
                long id = args.getLong("id");
                int type = args.getInt("type");
                String alias = args.getString("alias");

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                boolean check_certificate = prefs.getBoolean("check_certificate", true);

                DB db = DB.getInstance(context);

                // Get data
                EntityMessage draft = db.message().getMessage(id);
                if (draft == null)
                    throw new MessageRemovedException("S/MIME");
                EntityIdentity identity = db.identity().getIdentity(draft.identity);
                if (identity == null)
                    throw new IllegalArgumentException(context.getString(R.string.title_from_missing));

                // Get/clean attachments
                List<EntityAttachment> attachments = db.attachment().getAttachments(id);
                for (EntityAttachment attachment : new ArrayList<>(attachments))
                    if (attachment.encryption != null) {
                        db.attachment().deleteAttachment(attachment.id);
                        attachments.remove(attachment);
                    }

                // Build message to sign
                Properties props = MessageHelper.getSessionProperties();
                Session isession = Session.getInstance(props, null);
                MimeMessage imessage = new MimeMessage(isession);
                MessageHelper.build(context, draft, attachments, identity, true, imessage);
                imessage.saveChanges();
                BodyPart bpContent = new MimeBodyPart() {
                    @Override
                    public void setContent(Object content, String type) throws MessagingException {
                        super.setContent(content, type);

                        // https://javaee.github.io/javamail/FAQ#howencode
                        updateHeaders();
                        if (content instanceof Multipart) {
                            try {
                                MessageHelper.overrideContentTransferEncoding((Multipart) content);
                            } catch (IOException ex) {
                                Log.e(ex);
                            }
                        } else
                            setHeader("Content-Transfer-Encoding", "base64");
                    }
                };
                bpContent.setContent(imessage.getContent(), imessage.getContentType());

                if (alias == null)
                    throw new IllegalArgumentException("Key alias missing");

                // Get private key
                PrivateKey privkey = KeyChain.getPrivateKey(context, alias);
                if (privkey == null)
                    throw new IllegalArgumentException("Private key missing");

                // Get public key
                X509Certificate[] chain = KeyChain.getCertificateChain(context, alias);
                if (chain == null || chain.length == 0)
                    throw new IllegalArgumentException("Certificate missing");

                if (check_certificate) {
                    // Check public key validity
                    try {
                        chain[0].checkValidity();
                    } catch (CertificateException ex) {
                        throw new IllegalArgumentException(context.getString(R.string.title_invalid_key), ex);
                    }

                    // Check public key email
                    boolean known = false;
                    List<String> emails = EntityCertificate.getEmailAddresses(chain[0]);
                    for (String email : emails)
                        if (email.equals(identity.email)) {
                            known = true;
                            break;
                        }

                    if (!known && emails.size() > 0) {
                        String message = identity.email + " (" + TextUtils.join(", ", emails) + ")";
                        throw new IllegalArgumentException(
                                context.getString(R.string.title_certificate_missing, message),
                                new CertificateException());
                    }
                }

                // Store selected alias
                db.identity().setIdentitySignKeyAlias(identity.id, alias);

                // Build content
                if (EntityMessage.SMIME_SIGNONLY.equals(type)) {
                    EntityAttachment cattachment = new EntityAttachment();
                    cattachment.message = draft.id;
                    cattachment.sequence = db.attachment().getAttachmentSequence(draft.id) + 1;
                    cattachment.name = "content.asc";
                    cattachment.type = "text/plain";
                    cattachment.disposition = Part.INLINE;
                    cattachment.encryption = EntityAttachment.SMIME_CONTENT;
                    cattachment.id = db.attachment().insertAttachment(cattachment);

                    File content = cattachment.getFile(context);
                    try (OutputStream os = new FileOutputStream(content)) {
                        bpContent.writeTo(os);
                    }

                    db.attachment().setDownloaded(cattachment.id, content.length());
                }

                // Sign
                Store store = new JcaCertStore(Arrays.asList(chain));
                CMSSignedDataGenerator cmsGenerator = new CMSSignedDataGenerator();
                cmsGenerator.addCertificates(store);

                String algorithm = privkey.getAlgorithm();
                Log.i("Private key algorithm=" + algorithm);
                if (TextUtils.isEmpty(algorithm))
                    algorithm = "RSA";
                else if ("EC".equals(algorithm))
                    algorithm = "ECDSA";

                ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256with" + algorithm)
                        .build(privkey);
                DigestCalculatorProvider digestCalculator = new JcaDigestCalculatorProviderBuilder()
                        .build();
                SignerInfoGenerator signerInfoGenerator = new JcaSignerInfoGeneratorBuilder(digestCalculator)
                        .build(contentSigner, chain[0]);
                cmsGenerator.addSignerInfoGenerator(signerInfoGenerator);

                File sinput = new File(context.getCacheDir(), "smime_sign." + draft.id);
                try (FileOutputStream fos = new FileOutputStream(sinput)) {
                    bpContent.writeTo(fos);
                }

                CMSTypedData cmsData = new CMSProcessableFile(sinput);
                CMSSignedData cmsSignedData = cmsGenerator.generate(cmsData);
                byte[] signedMessage = cmsSignedData.getEncoded();

                sinput.delete();

                // Build signature
                if (EntityMessage.SMIME_SIGNONLY.equals(type)) {
                    ContentType ct = new ContentType("application/pkcs7-signature");
                    ct.setParameter("micalg", "sha-256");

                    EntityAttachment sattachment = new EntityAttachment();
                    sattachment.message = draft.id;
                    sattachment.sequence = db.attachment().getAttachmentSequence(draft.id) + 1;
                    sattachment.name = "smime.p7s";
                    sattachment.type = ct.toString();
                    sattachment.disposition = Part.INLINE;
                    sattachment.encryption = EntityAttachment.SMIME_SIGNATURE;
                    sattachment.id = db.attachment().insertAttachment(sattachment);

                    File file = sattachment.getFile(context);
                    try (OutputStream os = new FileOutputStream(file)) {
                        os.write(signedMessage);
                    }

                    db.attachment().setDownloaded(sattachment.id, file.length());

                    return null;
                }

                List<Address> addresses = new ArrayList<>();
                if (draft.to != null)
                    addresses.addAll(Arrays.asList(draft.to));
                if (draft.cc != null)
                    addresses.addAll(Arrays.asList(draft.cc));
                if (draft.bcc != null)
                    addresses.addAll(Arrays.asList(draft.bcc));

                List<X509Certificate> certs = new ArrayList<>();

                boolean own = true;
                for (Address address : addresses) {
                    boolean found = false;
                    Throwable cex = null;
                    String email = ((InternetAddress) address).getAddress();
                    List<EntityCertificate> acertificates = db.certificate().getCertificateByEmail(email);
                    if (acertificates != null)
                        for (EntityCertificate acertificate : acertificates) {
                            X509Certificate cert = acertificate.getCertificate();
                            try {
                                cert.checkValidity();
                                certs.add(cert);
                                found = true;
                                if (cert.equals(chain[0]))
                                    own = false;
                            } catch (CertificateException ex) {
                                Log.w(ex);
                                cex = ex;
                            }
                        }

                    if (!found)
                        if (cex == null)
                            throw new IllegalArgumentException(
                                    context.getString(R.string.title_certificate_missing, email));
                        else
                            throw new IllegalArgumentException(
                                    context.getString(R.string.title_certificate_invalid, email), cex);
                }

                // Allow sender to decrypt own message
                if (own)
                    certs.add(chain[0]);

                // Build signature
                BodyPart bpSignature = new MimeBodyPart();
                bpSignature.setFileName("smime.p7s");
                bpSignature.setDataHandler(new DataHandler(new ByteArrayDataSource(signedMessage, "application/pkcs7-signature")));
                bpSignature.setDisposition(Part.INLINE);

                // Build message
                ContentType ct = new ContentType("multipart/signed");
                ct.setParameter("micalg", "sha-256");
                ct.setParameter("protocol", "application/pkcs7-signature");
                ct.setParameter("smime-type", "signed-data");
                String ctx = ct.toString();
                int slash = ctx.indexOf("/");
                Multipart multipart = new MimeMultipart(ctx.substring(slash + 1));
                multipart.addBodyPart(bpContent);
                multipart.addBodyPart(bpSignature);
                imessage.setContent(multipart);
                imessage.saveChanges();

                // Encrypt
                CMSEnvelopedDataGenerator cmsEnvelopedDataGenerator = new CMSEnvelopedDataGenerator();
                if ("EC".equals(privkey.getAlgorithm())) {
                    JceKeyAgreeRecipientInfoGenerator gen = new JceKeyAgreeRecipientInfoGenerator(
                            CMSAlgorithm.ECDH_SHA256KDF,
                            privkey,
                            chain[0].getPublicKey(),
                            CMSAlgorithm.AES128_WRAP);
                    for (X509Certificate cert : certs)
                        gen.addRecipient(cert);
                    cmsEnvelopedDataGenerator.addRecipientInfoGenerator(gen);
                    // https://security.stackexchange.com/a/53960
                    // throw new IllegalArgumentException("ECDSA cannot be used for encryption");
                } else {
                    for (X509Certificate cert : certs) {
                        RecipientInfoGenerator gen = new JceKeyTransRecipientInfoGenerator(cert);
                        cmsEnvelopedDataGenerator.addRecipientInfoGenerator(gen);
                    }
                }

                File einput = new File(context.getCacheDir(), "smime_encrypt." + draft.id);
                try (FileOutputStream fos = new FileOutputStream(einput)) {
                    imessage.writeTo(fos);
                }
                CMSTypedData msg = new CMSProcessableFile(einput);

                OutputEncryptor encryptor = new JceCMSContentEncryptorBuilder(CMSAlgorithm.AES128_CBC)
                        .build();
                CMSEnvelopedData cmsEnvelopedData = cmsEnvelopedDataGenerator
                        .generate(msg, encryptor);

                EntityAttachment attachment = new EntityAttachment();
                attachment.message = draft.id;
                attachment.sequence = db.attachment().getAttachmentSequence(draft.id) + 1;
                attachment.name = "smime.p7m";
                attachment.type = "application/pkcs7-mime";
                attachment.disposition = Part.INLINE;
                attachment.encryption = EntityAttachment.SMIME_MESSAGE;
                attachment.id = db.attachment().insertAttachment(attachment);

                File encrypted = attachment.getFile(context);
                try (OutputStream os = new FileOutputStream(encrypted)) {
                    cmsEnvelopedData.toASN1Structure().encodeTo(os);
                }

                einput.delete();

                db.attachment().setDownloaded(attachment.id, encrypted.length());

                return null;
            }

            @Override
            protected void onExecuted(Bundle args, Void result) {
                extras.putBoolean("encrypted", true);
                onAction(action, extras, "smime");
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (ex instanceof IllegalArgumentException) {
                    Log.i(ex);
                    Snackbar snackbar = Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_INDEFINITE)
                            .setGestureInsetBottomIgnored(true);
                    snackbar.setAction(R.string.title_fix, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (ex.getCause() instanceof CertificateException)
                                startActivity(
                                        new Intent(getContext(), ActivitySetup.class)
                                                .putExtra("tab", "encryption"));
                            else {
                                View vwAnchor = view.findViewById(R.id.vwAnchor);
                                PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(getContext(), getViewLifecycleOwner(), vwAnchor);
                                popupMenu.getMenu().add(Menu.NONE, R.string.title_send_dialog, 1, R.string.title_send_dialog);
                                popupMenu.getMenu().add(Menu.NONE, R.string.title_advanced_manage_certificates, 2, R.string.title_advanced_manage_certificates);

                                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem item) {
                                        switch (item.getItemId()) {
                                            case R.string.title_send_dialog:
                                                FragmentDialogSend fragment = new FragmentDialogSend();
                                                fragment.setArguments(args);
                                                fragment.setTargetFragment(FragmentCompose.this, REQUEST_SEND);
                                                fragment.show(getParentFragmentManager(), "compose:send");
                                                return true;

                                            case R.string.title_advanced_manage_certificates:
                                                startActivity(
                                                        new Intent(getContext(), ActivitySetup.class)
                                                                .putExtra("tab", "encryption"));
                                                return true;

                                            default:
                                                return false;
                                        }
                                    }
                                });

                                popupMenu.show();
                            }
                        }
                    });
                    snackbar.show();
                } else {
                    boolean expected =
                            (ex instanceof OperatorCreationException &&
                                    ex.getCause() instanceof InvalidKeyException);
                    Log.unexpectedError(getParentFragmentManager(), ex, !expected);
                }
            }
        }.execute(this, args, "compose:s/mime");
    }

    private void onContactGroupSelected(Bundle args) {
        if (args.getInt("target") > 0)
            grpAddresses.setVisibility(View.VISIBLE);

        new SimpleTask<EntityMessage>() {
            @Override
            protected EntityMessage onExecute(Context context, Bundle args) throws Throwable {
                long id = args.getLong("id");
                int target = args.getInt("target");
                long group = args.getLong("group");

                List<Address> selected = new ArrayList<>();

                try (Cursor cursor = context.getContentResolver().query(
                        ContactsContract.Data.CONTENT_URI,
                        new String[]{ContactsContract.Data.CONTACT_ID},
                        ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + "= ?" + " AND "
                                + ContactsContract.CommonDataKinds.GroupMembership.MIMETYPE + "='"
                                + ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE + "'",
                        new String[]{String.valueOf(group)}, null)) {
                    while (cursor != null && cursor.moveToNext()) {
                        try (Cursor contact = getContext().getContentResolver().query(
                                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                                new String[]{
                                        ContactsContract.Contacts.DISPLAY_NAME,
                                        ContactsContract.CommonDataKinds.Email.DATA
                                },
                                ContactsContract.Data.CONTACT_ID + " = ?",
                                new String[]{cursor.getString(0)},
                                null)) {
                            if (contact != null && contact.moveToNext()) {
                                String name = contact.getString(0);
                                String email = contact.getString(1);
                                selected.add(new InternetAddress(email, name, StandardCharsets.UTF_8.name()));
                            }
                        }
                    }
                }

                EntityMessage draft;
                DB db = DB.getInstance(context);

                try {
                    db.beginTransaction();

                    draft = db.message().getMessage(id);
                    if (draft == null)
                        return null;

                    Address[] address = null;
                    if (target == 0)
                        address = draft.to;
                    else if (target == 1)
                        address = draft.cc;
                    else if (target == 2)
                        address = draft.bcc;

                    List<Address> list = new ArrayList<>();
                    if (address != null)
                        list.addAll(Arrays.asList(address));

                    list.addAll(selected);

                    if (target == 0)
                        draft.to = list.toArray(new Address[0]);
                    else if (target == 1)
                        draft.cc = list.toArray(new Address[0]);
                    else if (target == 2)
                        draft.bcc = list.toArray(new Address[0]);

                    db.message().updateMessage(draft);

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                return draft;
            }

            @Override
            protected void onExecuted(Bundle args, EntityMessage draft) {
                if (draft != null) {
                    etTo.setText(MessageHelper.formatAddressesCompose(draft.to));
                    etCc.setText(MessageHelper.formatAddressesCompose(draft.cc));
                    etBcc.setText(MessageHelper.formatAddressesCompose(draft.bcc));
                }
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "compose:picked");
    }

    private void onLinkSelected(Bundle args) {
        String link = args.getString("link");
        int start = args.getInt("start");
        int end = args.getInt("end");
        etBody.setSelection(start, end);
        StyleHelper.apply(R.id.menu_link, null, etBody, link);
    }

    private void onActionDiscardConfirmed() {
        onAction(R.id.action_delete, "delete");
    }

    private void onExit() {
        if (state == State.LOADED) {
            state = State.NONE;
            if (!saved && isEmpty())
                onAction(R.id.action_delete, "empty");
            else {
                Bundle extras = new Bundle();
                extras.putBoolean("autosave", true);
                onAction(R.id.action_save, extras, "exit");
                finish();
            }
        } else
            finish();
    }

    private boolean isEmpty() {
        if (!etSubject.getText().toString().equals(subject))
            return false;

        if (!TextUtils.isEmpty(JsoupEx.parse(HtmlHelper.toHtml(etBody.getText(), getContext())).text().trim()))
            return false;

        if (rvAttachment.getAdapter().getItemCount() > 0)
            return false;

        return true;
    }

    private void onAction(int action, String reason) {
        onAction(action, new Bundle(), reason);
    }

    private void onAction(int action, @NonNull Bundle extras, String reason) {
        EntityIdentity identity = (EntityIdentity) spIdentity.getSelectedItem();

        // Workaround underlines left by Android
        etBody.clearComposingText();

        Editable e = etBody.getText();
        boolean notext = e.toString().trim().isEmpty();
        boolean formatted = false;
        for (Object span : e.getSpans(0, e.length(), Object.class))
            if (span instanceof CharacterStyle || span instanceof ParagraphStyle) {
                formatted = true;
                break;
            }

        Bundle args = new Bundle();
        args.putLong("id", working);
        args.putInt("action", action);
        args.putLong("account", identity == null ? -1 : identity.account);
        args.putLong("identity", identity == null ? -1 : identity.id);
        args.putString("extra", etExtra.getText().toString().trim());
        args.putString("to", etTo.getText().toString().trim());
        args.putString("cc", etCc.getText().toString().trim());
        args.putString("bcc", etBcc.getText().toString().trim());
        args.putString("subject", etSubject.getText().toString().trim());
        args.putString("body", HtmlHelper.toHtml(etBody.getText(), getContext()));
        args.putBoolean("signature", cbSignature.isChecked());
        args.putBoolean("empty", isEmpty());
        args.putBoolean("notext", notext);
        args.putBoolean("formatted", formatted);
        args.putBoolean("interactive", getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED));
        args.putBundle("extras", extras);

        Log.i("Run execute id=" + working + " reason=" + reason);
        actionLoader.execute(this, args, "compose:action:" + action);
    }

    private static EntityAttachment addAttachment(
            Context context, long id, Uri uri, boolean image, int resize, boolean privacy) throws IOException {
        Log.w("Add attachment uri=" + uri + " image=" + image + " resize=" + resize + " privacy=" + privacy);

        if (!"content".equals(uri.getScheme()) &&
                !Helper.hasPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Log.w("Add attachment uri=" + uri);
            throw new SecurityException("Add attachment with file scheme");
        }

        EntityAttachment attachment = new EntityAttachment();
        UriInfo info = getInfo(uri, context);

        DB db = DB.getInstance(context);
        try {
            db.beginTransaction();

            EntityMessage draft = db.message().getMessage(id);
            if (draft == null)
                return null;

            Log.i("Attaching to id=" + id);

            attachment.message = draft.id;
            attachment.sequence = db.attachment().getAttachmentSequence(draft.id) + 1;
            if (privacy) {
                String ext = Helper.getExtension(info.name);
                attachment.name = "img" + attachment.sequence + (ext == null ? "" : "." + ext);
            } else
                attachment.name = info.name;
            attachment.type = info.type;
            attachment.disposition = (image ? Part.INLINE : Part.ATTACHMENT);
            attachment.size = info.size;
            attachment.progress = 0;

            attachment.id = db.attachment().insertAttachment(attachment);
            Log.i("Created attachment=" + attachment.name + ":" + attachment.sequence + " type=" + attachment.type);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        long size = 0;
        int lastProgress = 0;
        try {
            File file = attachment.getFile(context);

            InputStream is = null;
            OutputStream os = null;
            try {
                is = context.getContentResolver().openInputStream(uri);
                os = new FileOutputStream(file);

                byte[] buffer = new byte[Helper.BUFFER_SIZE];
                for (int len = is.read(buffer); len != -1; len = is.read(buffer)) {
                    size += len;
                    os.write(buffer, 0, len);

                    // Update progress
                    if (attachment.size != null && attachment.size > 0) {
                        int progress = (int) (size * 100 / attachment.size / 20 * 20);
                        if (progress != lastProgress) {
                            lastProgress = progress;
                            db.attachment().setProgress(attachment.id, progress);
                        }
                    }
                }

                if (image) {
                    attachment.cid = "<" + BuildConfig.APPLICATION_ID + "." + attachment.id + ">";
                    db.attachment().setCid(attachment.id, attachment.cid);
                }
            } finally {
                try {
                    if (is != null)
                        is.close();
                } finally {
                    if (os != null)
                        os.close();
                }
            }

            db.attachment().setDownloaded(attachment.id, size);

            if (BuildConfig.APPLICATION_ID.equals(uri.getAuthority())) {
                // content://eu.faircode.email/photo/nnn.jpg
                File tmp = new File(context.getCacheDir(), uri.getPath());
                Log.i("Deleting " + tmp);
                if (!tmp.delete())
                    Log.w("Error deleting " + tmp);
            } else
                Log.i("Authority=" + uri.getAuthority());

            if (resize > 0)
                resizeAttachment(context, attachment, resize);

            if (privacy)
                try {
                    ExifInterface exif = new ExifInterface(file.getAbsolutePath());

                    exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, null);
                    exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, null);
                    exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, null);
                    exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, null);
                    exif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF, null);
                    exif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE, null);
                    exif.setAttribute(ExifInterface.TAG_GPS_SPEED_REF, null);
                    exif.setAttribute(ExifInterface.TAG_GPS_SPEED, null);

                    exif.setAttribute(ExifInterface.TAG_GPS_DEST_LATITUDE_REF, null);
                    exif.setAttribute(ExifInterface.TAG_GPS_DEST_LATITUDE, null);
                    exif.setAttribute(ExifInterface.TAG_GPS_DEST_LONGITUDE_REF, null);
                    exif.setAttribute(ExifInterface.TAG_GPS_DEST_LONGITUDE, null);
                    exif.setAttribute(ExifInterface.TAG_GPS_DEST_BEARING_REF, null);
                    exif.setAttribute(ExifInterface.TAG_GPS_DEST_BEARING, null);
                    exif.setAttribute(ExifInterface.TAG_GPS_DEST_DISTANCE_REF, null);
                    exif.setAttribute(ExifInterface.TAG_GPS_DEST_DISTANCE, null);

                    exif.setAttribute(ExifInterface.TAG_GPS_IMG_DIRECTION_REF, null);
                    exif.setAttribute(ExifInterface.TAG_GPS_IMG_DIRECTION, null);

                    exif.setAttribute(ExifInterface.TAG_GPS_TRACK_REF, null);
                    exif.setAttribute(ExifInterface.TAG_GPS_TRACK, null);

                    exif.setAttribute(ExifInterface.TAG_GPS_AREA_INFORMATION, null);

                    exif.setAttribute(ExifInterface.TAG_DATETIME, null);
                    exif.setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, null);
                    exif.setAttribute(ExifInterface.TAG_DATETIME_DIGITIZED, null);

                    exif.setAttribute(ExifInterface.TAG_XMP, null);
                    exif.setAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION, null);
                    exif.setAttribute(ExifInterface.TAG_ARTIST, null);
                    exif.setAttribute(ExifInterface.TAG_IMAGE_UNIQUE_ID, null);
                    exif.setAttribute(ExifInterface.TAG_CAMERA_OWNER_NAME, null);
                    exif.setAttribute(ExifInterface.TAG_BODY_SERIAL_NUMBER, null);
                    exif.setAttribute(ExifInterface.TAG_LENS_SERIAL_NUMBER, null);

                    exif.saveAttributes();
                } catch (IOException ex) {
                    Log.i(ex);
                }

        } catch (Throwable ex) {
            // Reset progress on failure
            Log.e(ex);
            db.attachment().setError(attachment.id, Log.formatThrowable(ex, false));
            throw ex;
        }

        return attachment;
    }

    private static void resizeAttachment(Context context, EntityAttachment attachment, int resize) throws IOException {
        File file = attachment.getFile(context);
        if (file.exists() /* upload cancelled */ &&
                ("image/jpeg".equals(attachment.type) || "image/png".equals(attachment.type))) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(file.getAbsolutePath(), options);

            int factor = 1;
            while (options.outWidth / factor > resize ||
                    options.outHeight / factor > resize)
                factor *= 2;

            Matrix rotation = ("image/jpeg".equals(attachment.type) ? ImageHelper.getImageRotation(file) : null);
            Log.i("Image type=" + attachment.type + " rotation=" + rotation);
            if (factor > 1 || rotation != null) {
                options.inJustDecodeBounds = false;
                options.inSampleSize = factor;

                Log.i("Image target size=" + resize + " factor=" + factor + " source=" + options.outWidth + "x" + options.outHeight);
                Bitmap resized = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
                if (resized != null) {
                    Log.i("Image result size=" + resized.getWidth() + "x" + resized.getHeight() + " rotation=" + rotation);

                    if (rotation != null) {
                        Bitmap rotated = Bitmap.createBitmap(resized, 0, 0, resized.getWidth(), resized.getHeight(), rotation, true);
                        resized.recycle();
                        resized = rotated;
                    }

                    File tmp = File.createTempFile("image", ".resized", context.getCacheDir());
                    try (OutputStream out = new BufferedOutputStream(new FileOutputStream(tmp))) {
                        resized.compress("image/jpeg".equals(attachment.type)
                                        ? Bitmap.CompressFormat.JPEG
                                        : Bitmap.CompressFormat.PNG,
                                REDUCED_IMAGE_QUALITY, out);
                    } finally {
                        resized.recycle();
                    }

                    file.delete();
                    tmp.renameTo(file);

                    DB db = DB.getInstance(context);
                    db.attachment().setDownloaded(attachment.id, file.length());
                }
            }
        }
    }

    private SimpleTask<DraftData> draftLoader = new SimpleTask<DraftData>() {
        @Override
        protected DraftData onExecute(Context context, Bundle args) throws Throwable {
            String action = args.getString("action");
            long id = args.getLong("id", -1);
            long reference = args.getLong("reference", -1);
            File ics = (File) args.getSerializable("ics");
            String status = args.getString("status");
            long answer = args.getLong("answer", -1);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            boolean plain_only = prefs.getBoolean("plain_only", false);
            boolean resize_reply = prefs.getBoolean("resize_reply", true);
            boolean sign_default = prefs.getBoolean("sign_default", false);
            boolean encrypt_default = prefs.getBoolean("encrypt_default", false);
            boolean receipt_default = prefs.getBoolean("receipt_default", false);

            Log.i("Load draft action=" + action + " id=" + id + " reference=" + reference);

            Map<String, String> crumb = new HashMap<>();
            crumb.put("draft", Long.toString(id));
            crumb.put("reference", Long.toString(reference));
            crumb.put("action", action);
            Log.breadcrumb("compose", crumb);

            DraftData data = new DraftData();

            DB db = DB.getInstance(context);
            try {
                db.beginTransaction();

                data.identities = db.identity().getComposableIdentities(null);
                if (data.identities == null || data.identities.size() == 0)
                    throw new IllegalStateException(context.getString(R.string.title_no_composable));

                data.draft = db.message().getMessage(id);
                if (data.draft == null || data.draft.ui_hide) {
                    // New draft
                    if ("edit".equals(action))
                        throw new MessageRemovedException("Draft for edit was deleted hide=" + (data.draft != null));

                    EntityMessage ref = db.message().getMessage(reference);

                    data.draft = new EntityMessage();
                    data.draft.msgid = EntityMessage.generateMessageId();

                    // Select identity matching from address
                    EntityIdentity selected = null;
                    long aid = args.getLong("account", -1);
                    long iid = args.getLong("identity", -1);

                    if (aid < 0)
                        if (ref == null) {
                            EntityAccount primary = db.account().getPrimaryAccount();
                            if (primary != null)
                                aid = primary.id;
                        } else
                            aid = ref.account;
                    if (iid < 0 && ref != null && ref.identity != null)
                        iid = ref.identity;

                    if (iid >= 0)
                        for (EntityIdentity identity : data.identities)
                            if (identity.id.equals(iid)) {
                                Log.i("Selected requested identity=" + iid);
                                selected = identity;
                                break;
                            }

                    Address[] refto = (ref == null ? null
                            : ref.replySelf(data.identities, ref.account) ? ref.from : ref.to);
                    if (refto != null && refto.length > 0) {
                        if (selected == null)
                            for (Address sender : refto)
                                for (EntityIdentity identity : data.identities)
                                    if (identity.account.equals(aid) &&
                                            identity.sameAddress(sender)) {
                                        selected = identity;
                                        Log.i("Selected same account/identity");
                                        break;
                                    }

                        if (selected == null)
                            for (Address sender : refto)
                                for (EntityIdentity identity : data.identities)
                                    if (identity.account.equals(aid) &&
                                            identity.similarAddress(sender)) {
                                        selected = identity;
                                        Log.i("Selected similar account/identity");
                                        break;
                                    }

                        if (selected == null)
                            for (Address sender : refto)
                                for (EntityIdentity identity : data.identities)
                                    if (identity.sameAddress(sender)) {
                                        selected = identity;
                                        Log.i("Selected same */identity");
                                        break;
                                    }

                        if (selected == null)
                            for (Address sender : refto)
                                for (EntityIdentity identity : data.identities)
                                    if (identity.similarAddress(sender)) {
                                        selected = identity;
                                        Log.i("Selected similer */identity");
                                        break;
                                    }
                    }

                    if (selected == null)
                        for (EntityIdentity identity : data.identities)
                            if (identity.account.equals(aid) && identity.primary) {
                                selected = identity;
                                Log.i("Selected primary account/identity");
                                break;
                            }

                    if (selected == null)
                        for (EntityIdentity identity : data.identities)
                            if (identity.account.equals(aid)) {
                                selected = identity;
                                Log.i("Selected account/identity");
                                break;
                            }

                    if (selected == null)
                        for (EntityIdentity identity : data.identities)
                            if (identity.primary) {
                                Log.i("Selected primary */identity");
                                selected = identity;
                                break;
                            }

                    if (selected == null)
                        for (EntityIdentity identity : data.identities) {
                            Log.i("Selected */identity");
                            selected = identity;
                            break;
                        }

                    if (selected == null)
                        throw new IllegalArgumentException(context.getString(R.string.title_no_composable));

                    if (plain_only)
                        data.draft.plain_only = true;

                    if (encrypt_default)
                        if (selected.encrypt == 0)
                            data.draft.ui_encrypt = EntityMessage.PGP_SIGNENCRYPT;
                        else
                            data.draft.ui_encrypt = EntityMessage.SMIME_SIGNENCRYPT;
                    else if (sign_default)
                        if (selected.encrypt == 0)
                            data.draft.ui_encrypt = EntityMessage.PGP_SIGNONLY;
                        else
                            data.draft.ui_encrypt = EntityMessage.SMIME_SIGNONLY;

                    if (receipt_default)
                        data.draft.receipt_request = true;

                    Document document = Document.createShell("");

                    if (ref == null) {
                        data.draft.thread = data.draft.msgid;

                        try {
                            String to = args.getString("to");
                            data.draft.to = (TextUtils.isEmpty(to) ? null : InternetAddress.parseHeader(to, false));
                        } catch (AddressException ex) {
                            Log.w(ex);
                        }

                        try {
                            String cc = args.getString("cc");
                            data.draft.cc = (TextUtils.isEmpty(cc) ? null : InternetAddress.parseHeader(cc, false));
                        } catch (AddressException ex) {
                            Log.w(ex);
                        }

                        try {
                            String bcc = args.getString("bcc");
                            data.draft.bcc = (TextUtils.isEmpty(bcc) ? null : InternetAddress.parseHeader(bcc, false));
                        } catch (AddressException ex) {
                            Log.w(ex);
                        }

                        data.draft.subject = args.getString("subject", "");

                        String b = args.getString("body", "");
                        if (!TextUtils.isEmpty(b)) {
                            Document d = HtmlHelper.sanitizeCompose(context, b, false);
                            Element e = d.body();
                            e.tagName("div");
                            document.body().appendChild(e);
                        }

                        EntityAnswer a = (answer < 0
                                ? db.answer().getStandardAnswer()
                                : db.answer().getAnswer(answer));
                        if (a != null) {
                            data.draft.subject = a.name;
                            Document d = JsoupEx.parse(a.getText(null));
                            document.body().append(d.body().html());
                        }

                        addSignature(context, document, data.draft, selected);
                    } else {
                        // Actions:
                        // - reply
                        // - reply_all
                        // - forward
                        // - editasnew
                        // - list
                        // - receipt
                        // - participation

                        // References
                        if ("reply".equals(action) || "reply_all".equals(action) ||
                                "list".equals(action) ||
                                "receipt".equals(action) ||
                                "participation".equals(action)) {
                            data.draft.references = (ref.references == null ? "" : ref.references + " ") + ref.msgid;
                            data.draft.inreplyto = ref.msgid;
                            data.draft.thread = ref.thread;

                            if ("list".equals(action) && ref.list_post != null)
                                data.draft.to = ref.list_post;
                            else if ("receipt".equals(action) && ref.receipt_to != null)
                                data.draft.to = ref.receipt_to;
                            else {
                                // Prevent replying to self
                                if (ref.replySelf(data.identities, ref.account)) {
                                    data.draft.from = ref.from;
                                    data.draft.to = ref.to;
                                } else {
                                    data.draft.from = ref.to;
                                    data.draft.to = (ref.reply == null || ref.reply.length == 0 ? ref.from : ref.reply);
                                }

                                if (data.draft.from != null && data.draft.from.length > 0) {
                                    Address preferred = null;
                                    if (ref.identity != null) {
                                        EntityIdentity recognized = db.identity().getIdentity(ref.identity);
                                        if (recognized != null) {
                                            Address same = null;
                                            Address similar = null;

                                            for (Address from : data.draft.from) {
                                                if (same == null && recognized.sameAddress(from))
                                                    same = from;
                                                if (similar == null && recognized.similarAddress(from))
                                                    similar = from;
                                            }

                                            if (ref.deliveredto != null)
                                                try {
                                                    Address deliveredto = new InternetAddress(ref.deliveredto);
                                                    if (same == null && recognized.sameAddress(deliveredto))
                                                        same = deliveredto;
                                                    if (similar == null && recognized.similarAddress(deliveredto))
                                                        similar = deliveredto;
                                                } catch (AddressException ex) {
                                                    Log.w(ex);
                                                }

                                            preferred = (same == null ? similar : same);
                                        }
                                    }
                                    if (preferred != null) {
                                        String from = ((InternetAddress) preferred).getAddress();
                                        data.draft.extra = from.substring(0, from.indexOf("@"));
                                    }
                                }
                            }

                            if ("reply_all".equals(action))
                                data.draft.cc = ref.getAllRecipients(data.identities, ref.account);
                            else if ("receipt".equals(action)) {
                                data.draft.receipt = true;
                                data.draft.receipt_request = false;
                            }

                        } else if ("forward".equals(action)) {
                            data.draft.thread = data.draft.msgid; // new thread
                            data.draft.wasforwardedfrom = ref.msgid;
                        } else if ("editasnew".equals(action))
                            data.draft.thread = data.draft.msgid;

                        // Subject
                        String subject = (ref.subject == null ? "" : ref.subject);
                        if ("reply".equals(action) || "reply_all".equals(action)) {
                            if (prefix_once)
                                for (String re : Helper.getStrings(context, ref.language, R.string.title_subject_reply, ""))
                                    subject = unprefix(subject, re);
                            data.draft.subject = Helper.getString(context, ref.language, R.string.title_subject_reply, subject);

                            String t = args.getString("text");
                            if (t != null) {
                                Element div = document.createElement("div");
                                for (String line : t.split("\\r?\\n")) {
                                    Element span = document.createElement("span");
                                    span.text(line);
                                    div.appendChild(span);
                                    div.appendElement("br");
                                }
                                document.body().appendChild(div);
                            }
                        } else if ("forward".equals(action)) {
                            if (prefix_once)
                                for (String fwd : Helper.getStrings(context, ref.language, R.string.title_subject_forward, ""))
                                    subject = unprefix(subject, fwd);
                            data.draft.subject = Helper.getString(context, ref.language, R.string.title_subject_forward, subject);
                        } else if ("editasnew".equals(action)) {
                            if (ref.from != null && ref.from.length == 1) {
                                String from = ((InternetAddress) ref.from[0]).getAddress();
                                for (EntityIdentity identity : data.identities)
                                    if (identity.email.equals(from)) {
                                        selected = identity;
                                        break;
                                    }
                            }
                            data.draft.to = ref.to;
                            data.draft.cc = ref.cc;
                            data.draft.bcc = ref.bcc;
                            data.draft.subject = ref.subject;
                            if (ref.content) {
                                String html = Helper.readText(ref.getFile(context));
                                Document d = HtmlHelper.sanitizeCompose(context, html, true);
                                Element e = d.body();
                                e.tagName("div");
                                document.body().appendChild(e);
                            }
                        } else if ("list".equals(action)) {
                            data.draft.subject = ref.subject;
                        } else if ("receipt".equals(action)) {
                            data.draft.subject = context.getString(R.string.title_receipt_subject, subject);

                            for (String text : Helper.getStrings(context, ref.language, R.string.title_receipt_text)) {
                                Element p = document.createElement("p");
                                p.text(text);
                                document.body().appendChild(p);
                            }
                        } else if ("participation".equals(action))
                            data.draft.subject = status + ": " + ref.subject;

                        // Plain-only
                        if (ref.plain_only != null && ref.plain_only)
                            data.draft.plain_only = true;

                        // Encryption
                        if (EntityMessage.PGP_SIGNONLY.equals(ref.ui_encrypt) ||
                                EntityMessage.PGP_SIGNENCRYPT.equals(ref.ui_encrypt)) {
                            if (Helper.isOpenKeychainInstalled(context) && selected.sign_key != null)
                                data.draft.ui_encrypt = ref.ui_encrypt;
                        } else if (EntityMessage.SMIME_SIGNONLY.equals(ref.ui_encrypt) ||
                                EntityMessage.SMIME_SIGNENCRYPT.equals(ref.ui_encrypt)) {
                            if (ActivityBilling.isPro(context) && selected.sign_key_alias != null)
                                data.draft.ui_encrypt = ref.ui_encrypt;
                        }

                        // Reply template
                        EntityAnswer a = (answer < 0
                                ? db.answer().getStandardAnswer()
                                : db.answer().getAnswer(answer));
                        if (a != null) {
                            Document d = JsoupEx.parse(a.getText(data.draft.to));
                            document.body().append(d.body().html());
                        }

                        // Signature
                        if ("reply".equals(action) || "reply_all".equals(action))
                            data.draft.signature = prefs.getBoolean("signature_reply", true);
                        else if ("forward".equals(action))
                            data.draft.signature = prefs.getBoolean("signature_forward", true);
                        else
                            data.draft.signature = false;

                        // Reply header
                        String s = args.getString("selected");
                        if (ref.content &&
                                !"editasnew".equals(action) &&
                                !("list".equals(action) && TextUtils.isEmpty(s)) &&
                                !"receipt".equals(action)) {
                            // Reply/forward
                            Element reply = document.createElement("div");
                            reply.attr("fairemail", "reference");

                            reply.appendElement("br");

                            // Build reply header
                            boolean separate_reply = prefs.getBoolean("separate_reply", false);
                            boolean extended_reply = prefs.getBoolean("extended_reply", false);
                            Element p = ref.getReplyHeader(context, document, separate_reply, extended_reply);
                            reply.appendChild(p);

                            Document d;
                            if (TextUtils.isEmpty(s)) {
                                // Get referenced message body
                                d = JsoupEx.parse(ref.getFile(context));
                                for (Element e : d.select("[x-plain=true]"))
                                    e.removeAttr("x-plain");

                                // Remove signature separators
                                boolean remove_signatures = prefs.getBoolean("remove_signatures", false);
                                if (remove_signatures)
                                    d.body().filter(new NodeFilter() {
                                        private boolean remove = false;

                                        @Override
                                        public FilterResult head(Node node, int depth) {
                                            if (node instanceof TextNode) {
                                                TextNode tnode = (TextNode) node;
                                                String text = tnode.getWholeText()
                                                        .replaceAll("[\r\n]+$", "")
                                                        .replaceAll("^[\r\n]+", "");
                                                if ("-- ".equals(text)) {
                                                    if (tnode.getWholeText().endsWith("\n"))
                                                        remove = true;
                                                    else {
                                                        Node next = node.nextSibling();
                                                        if (next == null) {
                                                            Node parent = node.parent();
                                                            if (parent != null)
                                                                next = parent.nextSibling();
                                                        }
                                                        if (next != null && "br".equals(next.nodeName()))
                                                            remove = true;
                                                    }
                                                }
                                            }

                                            return (remove ? FilterResult.REMOVE : FilterResult.CONTINUE);
                                        }

                                        @Override
                                        public FilterResult tail(Node node, int depth) {
                                            return FilterResult.CONTINUE;
                                        }
                                    });
                            } else {
                                // Selected text
                                d = Document.createShell("");

                                Element div = d.createElement("div");
                                for (String line : s.split("\\r?\\n")) {
                                    Element span = document.createElement("span");
                                    span.text(line);
                                    div.appendChild(span);
                                    div.appendElement("br");
                                }
                                d.body().appendChild(div);
                            }

                            Element e = d.body();

                            // Apply styles
                            List<CSSStyleSheet> sheets = HtmlHelper.parseStyles(d.head().select("style"));
                            for (Element element : e.select("*")) {
                                String tag = element.tagName();
                                String clazz = element.attr("class");
                                String style = HtmlHelper.processStyles(tag, clazz, null, sheets);
                                style = HtmlHelper.mergeStyles(style, element.attr("style"));
                                element.attr("style", style);
                            }

                            // Quote referenced message body
                            boolean quote_reply = prefs.getBoolean("quote_reply", true);
                            boolean quote = (quote_reply &&
                                    ("reply".equals(action) || "reply_all".equals(action) || "list".equals(action)));

                            e.tagName(quote ? "blockquote" : "p");
                            reply.appendChild(e);

                            document.body().appendChild(reply);

                            addSignature(context, document, data.draft, selected);
                        }
                    }

                    EntityFolder drafts = db.folder().getFolderByType(selected.account, EntityFolder.DRAFTS);
                    if (drafts == null)
                        throw new IllegalArgumentException(context.getString(R.string.title_no_drafts));

                    data.draft.account = drafts.account;
                    data.draft.folder = drafts.id;
                    data.draft.identity = selected.id;
                    data.draft.from = new InternetAddress[]{new InternetAddress(selected.email, selected.name, StandardCharsets.UTF_8.name())};

                    data.draft.sender = MessageHelper.getSortKey(data.draft.from);
                    Uri lookupUri = ContactInfo.getLookupUri(data.draft.from);
                    data.draft.avatar = (lookupUri == null ? null : lookupUri.toString());

                    data.draft.received = new Date().getTime();
                    data.draft.seen = true;
                    data.draft.ui_seen = true;

                    data.draft.revision = 1;
                    data.draft.revisions = 1;

                    data.draft.id = db.message().insertMessage(data.draft);

                    String html = document.html();
                    Helper.writeText(data.draft.getFile(context), html);
                    Helper.writeText(data.draft.getFile(context, data.draft.revision), html);

                    db.message().setMessageContent(data.draft.id,
                            true,
                            HtmlHelper.getLanguage(context, html),
                            data.draft.plain_only,
                            HtmlHelper.getPreview(html),
                            null);

                    if ("participation".equals(action)) {
                        EntityAttachment attachment = new EntityAttachment();
                        attachment.message = data.draft.id;
                        attachment.sequence = 1;
                        attachment.name = "meeting.ics";
                        attachment.type = "text/calendar";
                        attachment.disposition = Part.ATTACHMENT;
                        attachment.size = ics.length();
                        attachment.progress = null;
                        attachment.available = true;
                        attachment.id = db.attachment().insertAttachment(attachment);
                        File file = attachment.getFile(context);
                        ics.renameTo(file);

                        ICalendar icalendar = Biweekly.parse(file).first();
                        VEvent event = icalendar.getEvents().get(0);
                        Organizer organizer = event.getOrganizer();
                        if (organizer != null) {
                            String email = organizer.getEmail();
                            String name = organizer.getCommonName();
                            if (!TextUtils.isEmpty(email)) {
                                InternetAddress o = new InternetAddress(email, name, StandardCharsets.UTF_8.name());
                                Log.i("Setting organizer=" + o);
                                data.draft.to = new Address[]{o};
                            }
                        }
                    }

                    if ("new".equals(action)) {
                        ArrayList<Uri> uris = args.getParcelableArrayList("attachments");
                        if (uris != null) {
                            ArrayList<Uri> images = new ArrayList<>();
                            for (Uri uri : uris)
                                try {
                                    UriInfo info = getInfo(uri, context);
                                    if (info.isImage())
                                        images.add(uri);
                                    else
                                        addAttachment(context, data.draft.id, uri, false, 0, false);
                                } catch (IOException ex) {
                                    Log.e(ex);
                                }

                            if (images.size() > 0)
                                args.putParcelableArrayList("images", images);
                        }
                    }

                    if (ref != null &&
                            ("reply".equals(action) || "reply_all".equals(action) ||
                                    "forward".equals(action) || "editasnew".equals(action))) {

                        List<String> cid = new ArrayList<>();
                        for (Element img : document.select("img")) {
                            String src = img.attr("src");
                            if (src.startsWith("cid:"))
                                cid.add("<" + src.substring(4) + ">");
                        }

                        int sequence = 0;
                        List<EntityAttachment> attachments = db.attachment().getAttachments(ref.id);
                        for (EntityAttachment attachment : attachments)
                            if (!attachment.isEncryption() &&
                                    ("forward".equals(action) || "editasnew".equals(action) ||
                                            cid.contains(attachment.cid))) {
                                if (attachment.available) {
                                    File source = attachment.getFile(context);

                                    if (cid.contains(attachment.cid))
                                        attachment.disposition = Part.INLINE;

                                    attachment.id = null;
                                    attachment.message = data.draft.id;
                                    attachment.sequence = ++sequence;
                                    attachment.id = db.attachment().insertAttachment(attachment);

                                    File target = attachment.getFile(context);
                                    Helper.copy(source, target);

                                    if (resize_reply && !"forward".equals(action))
                                        resizeAttachment(context, attachment, REDUCED_IMAGE_SIZE);
                                } else
                                    args.putBoolean("incomplete", true);
                            }
                    }
                } else {
                    args.putBoolean("saved", true);

                    // External draft
                    if (data.draft.identity == null) {
                        for (EntityIdentity identity : data.identities)
                            if (identity.account.equals(data.draft.account))
                                if (identity.primary) {
                                    data.draft.identity = identity.id;
                                    break;
                                } else if (data.draft.identity == null)
                                    data.draft.identity = identity.id;

                        if (data.draft.identity != null)
                            db.message().setMessageIdentity(data.draft.id, data.draft.identity);
                        Log.i("Selected external identity=" + data.draft.identity);
                    }

                    if (data.draft.revision == null) {
                        data.draft.revision = 1;
                        data.draft.revisions = 1;
                        db.message().setMessageRevision(data.draft.id, data.draft.revision);
                        db.message().setMessageRevisions(data.draft.id, data.draft.revisions);
                    }

                    if (data.draft.content || data.draft.uid == null) {
                        if (data.draft.uid == null && !data.draft.content)
                            Log.e("Draft without uid");

                        File file = data.draft.getFile(context);

                        Document doc = (data.draft.content ? JsoupEx.parse(file) : Document.createShell(""));
                        doc.select("div[fairemail=signature]").remove();
                        Elements ref = doc.select("div[fairemail=reference]");
                        ref.remove();

                        File refFile = data.draft.getRefFile(context);
                        if (refFile.exists()) {
                            ref.html(Helper.readText(refFile));
                            refFile.delete();
                        }

                        Document document = HtmlHelper.sanitizeCompose(context, doc.html(), true);

                        for (Element e : ref)
                            document.body().appendChild(e);

                        EntityIdentity identity = null;
                        if (data.draft.identity != null)
                            identity = db.identity().getIdentity(data.draft.identity);

                        addSignature(context, document, data.draft, identity);

                        String html = document.html();
                        Helper.writeText(file, html);
                        Helper.writeText(data.draft.getFile(context, data.draft.revision), html);

                        db.message().setMessageContent(data.draft.id,
                                true,
                                HtmlHelper.getLanguage(context, html),
                                data.draft.plain_only,
                                HtmlHelper.getPreview(html),
                                null);
                    } else
                        EntityOperation.queue(context, data.draft, EntityOperation.BODY);
                }

                List<EntityAttachment> attachments = db.attachment().getAttachments(data.draft.id);
                for (EntityAttachment attachment : attachments)
                    if (attachment.available) {
                        if (!attachment.isEncryption())
                            last_available++;
                    } else {
                        if (attachment.progress == null)
                            EntityOperation.queue(context, data.draft, EntityOperation.ATTACHMENT, attachment.id);
                    }

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }

            ServiceSynchronize.eval(context, "compose/draft");

            return data;
        }

        @Override
        protected void onExecuted(Bundle args, final DraftData data) {
            final String action = getArguments().getString("action");
            Log.i("Loaded draft id=" + data.draft.id + " action=" + action);

            working = data.draft.id;
            encrypt = data.draft.ui_encrypt;
            getActivity().invalidateOptionsMenu();

            subject = data.draft.subject;
            saved = args.getBoolean("saved");

            // Show identities
            AdapterIdentitySelect iadapter = new AdapterIdentitySelect(getContext(), data.identities);
            spIdentity.setAdapter(iadapter);

            // Select identity
            if (data.draft.identity != null)
                for (int pos = 0; pos < data.identities.size(); pos++) {
                    if (data.identities.get(pos).id.equals(data.draft.identity)) {
                        spIdentity.setSelection(pos);
                        break;
                    }
                }

            etExtra.setText(data.draft.extra);
            etTo.setText(MessageHelper.formatAddressesCompose(data.draft.to));
            etCc.setText(MessageHelper.formatAddressesCompose(data.draft.cc));
            etBcc.setText(MessageHelper.formatAddressesCompose(data.draft.bcc));
            etSubject.setText(data.draft.subject);

            long reference = args.getLong("reference", -1);
            etTo.setTag(reference < 0 ? "" : etTo.getText().toString());
            etSubject.setTag(reference < 0 ? "" : etSubject.getText().toString());
            cbSignature.setTag(data.draft.signature);

            grpHeader.setVisibility(View.VISIBLE);
            if ("reply_all".equals(action) ||
                    (data.draft.cc != null && data.draft.cc.length > 0) ||
                    (data.draft.bcc != null && data.draft.bcc.length > 0))
                grpAddresses.setVisibility(View.VISIBLE);
            ibCcBcc.setVisibility(View.VISIBLE);

            bottom_navigation.getMenu().findItem(R.id.action_undo).setVisible(data.draft.revision > 1);
            bottom_navigation.getMenu().findItem(R.id.action_redo).setVisible(data.draft.revision < data.draft.revisions);

            if (args.getBoolean("incomplete"))
                Snackbar.make(view, R.string.title_attachments_incomplete, Snackbar.LENGTH_LONG)
                        .setGestureInsetBottomIgnored(true).show();

            DB db = DB.getInstance(getContext());

            db.attachment().liveAttachments(data.draft.id).observe(getViewLifecycleOwner(),
                    new Observer<List<EntityAttachment>>() {
                        private Integer count = null;

                        @Override
                        public void onChanged(@Nullable List<EntityAttachment> attachments) {
                            if (attachments == null)
                                attachments = new ArrayList<>();

                            adapter.set(attachments);
                            grpAttachments.setVisibility(attachments.size() > 0 ? View.VISIBLE : View.GONE);

                            boolean downloading = false;
                            for (EntityAttachment attachment : attachments) {
                                if (attachment.isEncryption())
                                    continue;
                                if (attachment.progress != null)
                                    downloading = true;
                            }

                            Log.i("Attachments=" + attachments.size() + " downloading=" + downloading);

                            rvAttachment.setTag(downloading);
                            checkInternet();

                            if (count != null && count > attachments.size()) {
                                boolean updated = false;
                                Editable edit = etBody.getEditableText();

                                ImageSpan[] spans = edit.getSpans(0, edit.length(), ImageSpan.class);
                                for (int i = 0; i < spans.length && !updated; i++) {
                                    ImageSpan span = spans[i];
                                    String source = span.getSource();
                                    if (source != null && source.startsWith("cid:")) {
                                        String cid = "<" + source.substring(4) + ">";
                                        boolean found = false;
                                        for (EntityAttachment attachment : attachments)
                                            if (cid.equals(attachment.cid)) {
                                                found = true;
                                                break;
                                            }

                                        if (!found) {
                                            updated = true;
                                            int start = edit.getSpanStart(span);
                                            int end = edit.getSpanEnd(span);
                                            edit.removeSpan(span);
                                            edit.delete(start, end);
                                        }
                                    }
                                }

                                if (updated)
                                    etBody.setText(edit);
                            }

                            count = attachments.size();
                        }
                    });

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            final boolean plain_only = prefs.getBoolean("plain_only", false);

            db.message().liveMessage(data.draft.id).observe(getViewLifecycleOwner(), new Observer<EntityMessage>() {
                @Override
                public void onChanged(EntityMessage draft) {
                    // Draft was deleted
                    if (draft == null || draft.ui_hide)
                        finish();
                    else {
                        encrypt = draft.ui_encrypt;
                        getActivity().invalidateOptionsMenu();

                        Log.i("Draft content=" + draft.content);
                        if (draft.content && state == State.NONE)
                            showDraft(draft, false);

                        tvPlainTextOnly.setVisibility(
                                draft.plain_only != null && draft.plain_only && !plain_only ? View.VISIBLE : View.GONE);

                        tvNoInternet.setTag(draft.content);
                        checkInternet();
                    }
                }
            });

            if (args.containsKey("images")) {
                ArrayList<Uri> images = args.getParcelableArrayList("images");
                boolean image_dialog = prefs.getBoolean("image_dialog", true);
                if (image_dialog) {
                    Bundle aargs = new Bundle();
                    aargs.putInt("title", android.R.string.ok);
                    aargs.putParcelableArrayList("images", images);

                    FragmentDialogAddImage fragment = new FragmentDialogAddImage();
                    fragment.setArguments(aargs);
                    fragment.setTargetFragment(FragmentCompose.this, REQUEST_SHARED);
                    fragment.show(getParentFragmentManager(), "compose:shared");
                } else
                    onAddImageFile(images);
            }
        }

        @Override
        protected void onException(Bundle args, Throwable ex) {
            pbWait.setVisibility(View.GONE);

            // External app sending absolute file
            if (ex instanceof MessageRemovedException)
                finish();
            else if (ex instanceof SecurityException)
                handleFileShare();
            else if (ex instanceof IllegalArgumentException || ex instanceof FileNotFoundException)
                Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG)
                        .setGestureInsetBottomIgnored(true).show();
            else if (ex instanceof IllegalStateException) {
                Snackbar snackbar = Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_INDEFINITE)
                        .setGestureInsetBottomIgnored(true);
                snackbar.setAction(R.string.title_fix, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(getContext(), ActivitySetup.class));
                        getActivity().finish();
                    }
                });
                snackbar.show();
            } else
                Log.unexpectedError(getParentFragmentManager(), ex);
        }
    };

    private void handleFileShare() {
        Snackbar sb = Snackbar.make(view, R.string.title_no_stream, Snackbar.LENGTH_INDEFINITE)
                .setGestureInsetBottomIgnored(true);
        sb.setAction(R.string.title_info, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(getContext(), 49);
            }
        });
        sb.show();
    }

    private SimpleTask<EntityMessage> actionLoader = new SimpleTask<EntityMessage>() {
        @Override
        protected void onPreExecute(Bundle args) {
            setBusy(true);
        }

        @Override
        protected void onPostExecute(Bundle args) {
            int action = args.getInt("action");
            boolean needsEncryption = args.getBoolean("needsEncryption");
            if (action != R.id.action_check || needsEncryption)
                setBusy(false);
        }

        @Override
        protected EntityMessage onExecute(final Context context, Bundle args) throws Throwable {
            // Get data
            long id = args.getLong("id");
            int action = args.getInt("action");
            long aid = args.getLong("account");
            long iid = args.getLong("identity");
            String extra = args.getString("extra");
            String to = args.getString("to");
            String cc = args.getString("cc");
            String bcc = args.getString("bcc");
            String subject = args.getString("subject");
            String body = args.getString("body");
            boolean signature = args.getBoolean("signature");
            boolean empty = args.getBoolean("empty");
            boolean notext = args.getBoolean("notext");
            Bundle extras = args.getBundle("extras");

            EntityMessage draft;

            DB db = DB.getInstance(context);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            try {
                db.beginTransaction();

                // Get draft & selected identity
                draft = db.message().getMessage(id);
                EntityIdentity identity = db.identity().getIdentity(iid);

                // Draft deleted by server
                if (draft == null || draft.ui_hide)
                    throw new MessageRemovedException("Draft for action was deleted hide=" + (draft != null));

                Log.i("Load action id=" + draft.id + " action=" + getActionName(action));

                if (action == R.id.action_delete) {
                    boolean discard_delete = prefs.getBoolean("discard_delete", false);
                    EntityFolder trash = db.folder().getFolderByType(draft.account, EntityFolder.TRASH);
                    if (empty || trash == null || discard_delete)
                        EntityOperation.queue(context, draft, EntityOperation.DELETE);
                    else {
                        EntityOperation.queue(context, draft, EntityOperation.ADD);
                        EntityOperation.queue(context, draft, EntityOperation.MOVE, trash.id);
                    }

                    getMainHandler().post(new Runnable() {
                        public void run() {
                            ToastEx.makeText(context, R.string.title_draft_deleted, Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    boolean dirty = false;

                    // Move draft to new account
                    if (draft.account != aid && aid >= 0) {
                        Log.i("Account changed");

                        Long uid = draft.uid;
                        String msgid = draft.msgid;
                        boolean content = draft.content;
                        Boolean ui_hide = draft.ui_hide;

                        // To prevent violating constraints
                        draft.uid = null;
                        draft.msgid = null;
                        db.message().updateMessage(draft);

                        // Create copy to delete
                        draft.id = null;
                        draft.uid = uid;
                        draft.msgid = msgid;
                        draft.content = false;
                        draft.ui_hide = true;
                        draft.id = db.message().insertMessage(draft);
                        EntityOperation.queue(context, draft, EntityOperation.DELETE);

                        // Restore original with new account, no uid and new msgid
                        draft.id = id;
                        draft.account = aid;
                        draft.folder = db.folder().getFolderByType(aid, EntityFolder.DRAFTS).id;
                        draft.uid = null;
                        draft.msgid = EntityMessage.generateMessageId();
                        draft.content = content;
                        draft.ui_hide = ui_hide;
                        db.message().updateMessage(draft);

                        if (draft.content)
                            dirty = true;
                    }

                    Map<String, String> crumb = new HashMap<>();
                    crumb.put("draft", draft.folder + ":" + draft.id);
                    crumb.put("content", Boolean.toString(draft.content));
                    crumb.put("file", Boolean.toString(draft.getFile(context).exists()));
                    crumb.put("action", getActionName(action));
                    Log.breadcrumb("compose", crumb);

                    List<EntityAttachment> attachments = db.attachment().getAttachments(draft.id);

                    // Get data
                    InternetAddress[] afrom = (identity == null ? null : new InternetAddress[]{new InternetAddress(identity.email, identity.name, StandardCharsets.UTF_8.name())});
                    InternetAddress[] ato = (TextUtils.isEmpty(to) ? null : InternetAddress.parseHeader(to, false));
                    InternetAddress[] acc = (TextUtils.isEmpty(cc) ? null : InternetAddress.parseHeader(cc, false));
                    InternetAddress[] abcc = (TextUtils.isEmpty(bcc) ? null : InternetAddress.parseHeader(bcc, false));

                    // Safe guard
                    if (action == R.id.action_send) {
                        checkAddress(ato, context);
                        checkAddress(acc, context);
                        checkAddress(abcc, context);
                    }

                    if (TextUtils.isEmpty(extra))
                        extra = null;

                    int available = 0;
                    List<Integer> eparts = new ArrayList<>();
                    for (EntityAttachment attachment : attachments)
                        if (attachment.available)
                            if (attachment.isEncryption())
                                eparts.add(attachment.encryption);
                            else
                                available++;

                    if (EntityMessage.PGP_SIGNONLY.equals(draft.ui_encrypt)) {
                        if (!eparts.contains(EntityAttachment.PGP_KEY) ||
                                !eparts.contains(EntityAttachment.PGP_SIGNATURE) ||
                                !eparts.contains(EntityAttachment.PGP_CONTENT))
                            dirty = true;
                    } else if (EntityMessage.PGP_SIGNENCRYPT.equals(draft.ui_encrypt)) {
                        if (!eparts.contains(EntityAttachment.PGP_KEY) ||
                                !eparts.contains(EntityAttachment.PGP_MESSAGE))
                            dirty = true;
                    } else if (EntityMessage.SMIME_SIGNONLY.equals(draft.ui_encrypt)) {
                        if (!eparts.contains(EntityAttachment.SMIME_SIGNATURE) ||
                                !eparts.contains(EntityAttachment.SMIME_CONTENT))
                            dirty = true;
                    } else if (EntityMessage.SMIME_SIGNENCRYPT.equals(draft.ui_encrypt)) {
                        if (!eparts.contains(EntityAttachment.SMIME_MESSAGE))
                            dirty = true;
                    }

                    Long ident = (identity == null ? null : identity.id);
                    if (!Objects.equals(draft.identity, ident) ||
                            !Objects.equals(draft.extra, extra) ||
                            !MessageHelper.equal(draft.from, afrom) ||
                            !MessageHelper.equal(draft.to, ato) ||
                            !MessageHelper.equal(draft.cc, acc) ||
                            !MessageHelper.equal(draft.bcc, abcc) ||
                            !Objects.equals(draft.subject, subject) ||
                            !draft.signature.equals(signature) ||
                            last_available != available)
                        dirty = true;

                    last_available = available;

                    if (dirty) {
                        // Update draft
                        draft.identity = ident;
                        draft.extra = extra;
                        draft.from = afrom;
                        draft.to = ato;
                        draft.cc = acc;
                        draft.bcc = abcc;
                        draft.subject = subject;
                        draft.signature = signature;
                        draft.sender = MessageHelper.getSortKey(draft.from);
                        Uri lookupUri = ContactInfo.getLookupUri(draft.from);
                        draft.avatar = (lookupUri == null ? null : lookupUri.toString());
                        db.message().updateMessage(draft);
                    }

                    Document doc = JsoupEx.parse(draft.getFile(context));
                    doc.select("div[fairemail=signature]").remove();
                    Elements ref = doc.select("div[fairemail=reference]");
                    ref.remove();

                    Document b;
                    if (body == null)
                        b = Document.createShell("");
                    else
                        b = HtmlHelper.sanitizeCompose(context, body, true);

                    if (draft.revision == null) {
                        draft.revision = 1;
                        draft.revisions = 1;
                    }

                    int revision = draft.revision; // Save for undo/redo
                    if (dirty ||
                            TextUtils.isEmpty(body) ||
                            !b.body().html().equals(doc.body().html()) ||
                            (extras != null && extras.containsKey("html"))) {
                        dirty = true;

                        // Get saved body
                        Document d;
                        if (extras != null && extras.containsKey("html")) {
                            // Save current revision
                            Document c = JsoupEx.parse(body);

                            for (Element e : ref)
                                c.body().appendChild(e);

                            addSignature(context, c, draft, identity);

                            Helper.writeText(draft.getFile(context, draft.revision), c.html());

                            d = JsoupEx.parse(extras.getString("html"));
                        } else {
                            d = HtmlHelper.sanitizeCompose(context, body, true);

                            for (Element e : ref)
                                d.body().appendChild(e);

                            addSignature(context, d, draft, identity);
                        }

                        body = d.html();

                        // Create new revision
                        draft.revisions++;
                        draft.revision = draft.revisions;

                        Helper.writeText(draft.getFile(context, draft.revision), body);
                    } else
                        body = Helper.readText(draft.getFile(context));

                    if (action == R.id.action_undo || action == R.id.action_redo) {
                        if (action == R.id.action_undo) {
                            if (revision > 1)
                                draft.revision = revision - 1;
                            else
                                draft.revision = revision;
                        } else {
                            if (revision < draft.revisions)
                                draft.revision = revision + 1;
                            else
                                draft.revision = revision;
                        }

                        // Restore revision
                        Log.i("Restoring revision=" + draft.revision);
                        File file = draft.getFile(context, draft.revision);
                        if (file.exists())
                            body = Helper.readText(file);
                        else
                            Log.e("Missing revision=" + file);

                        dirty = true;
                    }

                    Helper.writeText(draft.getFile(context), body);

                    db.message().setMessageContent(draft.id,
                            true,
                            HtmlHelper.getLanguage(context, body),
                            draft.plain_only, // unchanged
                            HtmlHelper.getPreview(body),
                            null);

                    db.message().setMessageRevision(draft.id, draft.revision);
                    db.message().setMessageRevisions(draft.id, draft.revisions);

                    if (dirty) {
                        draft.received = new Date().getTime();
                        db.message().setMessageReceived(draft.id, draft.received);
                    }

                    // Execute action
                    boolean encrypted = extras.getBoolean("encrypted");
                    boolean shouldEncrypt = EntityMessage.PGP_SIGNENCRYPT.equals(draft.ui_encrypt) ||
                            (EntityMessage.PGP_SIGNONLY.equals(draft.ui_encrypt) && action == R.id.action_send) ||
                            EntityMessage.SMIME_SIGNENCRYPT.equals(draft.ui_encrypt) ||
                            (EntityMessage.SMIME_SIGNONLY.equals(draft.ui_encrypt) && action == R.id.action_send);
                    boolean needsEncryption = (dirty && !encrypted && shouldEncrypt);
                    boolean autosave = extras.getBoolean("autosave");
                    if (needsEncryption && !autosave) {
                        args.putBoolean("needsEncryption", true);
                        db.setTransactionSuccessful();
                        return draft;
                    }

                    if (action == R.id.action_save ||
                            action == R.id.action_undo ||
                            action == R.id.action_redo ||
                            action == R.id.action_check) {
                        if ((dirty || encrypted) && !needsEncryption) {
                            boolean save_drafts = prefs.getBoolean("save_drafts", true);
                            if (save_drafts)
                                EntityOperation.queue(context, draft, EntityOperation.ADD);
                        }

                        if (action == R.id.action_check) {
                            // Check data
                            if (draft.identity == null)
                                throw new IllegalArgumentException(context.getString(R.string.title_from_missing));

                            try {
                                checkAddress(ato, context);
                                checkAddress(acc, context);
                                checkAddress(abcc, context);

                                List<InternetAddress> check = new ArrayList<>();
                                List<String> checked = new ArrayList<>();
                                List<String> dup = new ArrayList<>();
                                if (ato != null)
                                    check.addAll(Arrays.asList(ato));
                                if (acc != null)
                                    check.addAll(Arrays.asList(acc));
                                if (abcc != null)
                                    check.addAll(Arrays.asList(abcc));

                                for (InternetAddress a : check) {
                                    String email = a.getAddress();
                                    if (TextUtils.isEmpty(email))
                                        continue;
                                    if (checked.contains(a.getAddress()))
                                        dup.add(email);
                                    else
                                        checked.add(email);
                                }

                                if (dup.size() > 0)
                                    throw new AddressException(context.getString(
                                            R.string.title_address_duplicate,
                                            TextUtils.join(", ", dup)));
                            } catch (AddressException ex) {
                                args.putString("address_error", ex.getMessage());
                            }

                            try {
                                checkMx(ato, context);
                                checkMx(acc, context);
                                checkMx(abcc, context);
                            } catch (UnknownHostException ex) {
                                args.putString("mx_error", ex.getMessage());
                            }

                            if (draft.to == null && draft.cc == null && draft.bcc == null &&
                                    (identity == null || (identity.cc == null && identity.bcc == null)))
                                args.putBoolean("remind_to", true);

                            //if (TextUtils.isEmpty(draft.extra) &&
                            //        identity != null && identity.sender_extra)
                            //    args.putBoolean("remind_extra", true);

                            if (pgpService != null && pgpService.isBound() &&
                                    (draft.ui_encrypt == null ||
                                            EntityMessage.ENCRYPT_NONE.equals(draft.ui_encrypt))) {
                                List<Address> recipients = new ArrayList<>();
                                if (draft.to != null)
                                    recipients.addAll(Arrays.asList(draft.to));
                                if (draft.cc != null)
                                    recipients.addAll(Arrays.asList(draft.cc));
                                if (draft.bcc != null)
                                    recipients.addAll(Arrays.asList(draft.bcc));

                                if (recipients.size() > 0) {
                                    String[] userIds = new String[recipients.size()];
                                    for (int i = 0; i < recipients.size(); i++) {
                                        InternetAddress recipient = (InternetAddress) recipients.get(i);
                                        userIds[i] = recipient.getAddress().toLowerCase();
                                    }

                                    Intent intent = new Intent(OpenPgpApi.ACTION_GET_KEY_IDS);
                                    intent.putExtra(OpenPgpApi.EXTRA_USER_IDS, userIds);

                                    try {
                                        OpenPgpApi api = new OpenPgpApi(context, pgpService.getService());
                                        Intent result = api.executeApi(intent, (InputStream) null, (OutputStream) null);
                                        int resultCode = result.getIntExtra(OpenPgpApi.RESULT_CODE, OpenPgpApi.RESULT_CODE_ERROR);
                                        if (resultCode == OpenPgpApi.RESULT_CODE_SUCCESS) {
                                            long[] keyIds = result.getLongArrayExtra(OpenPgpApi.EXTRA_KEY_IDS);
                                            args.putBoolean("remind_pgp", keyIds.length > 0);
                                        }
                                    } catch (Throwable ex) {
                                        Log.w(ex);
                                    }
                                }
                            }

                            if (TextUtils.isEmpty(draft.subject))
                                args.putBoolean("remind_subject", true);

                            Document d = JsoupEx.parse(body);

                            if (notext &&
                                    d.select("div[fairemail=reference]").isEmpty())
                                args.putBoolean("remind_text", true);

                            int attached = 0;
                            for (EntityAttachment attachment : attachments)
                                if (!attachment.available)
                                    throw new IllegalArgumentException(context.getString(R.string.title_attachments_missing));
                                else if (attachment.isAttachment())
                                    attached++;

                            // Check for missing attachments
                            if (attached == 0) {
                                List<String> keywords = new ArrayList<>();
                                for (String text : Helper.getStrings(context, R.string.title_attachment_keywords))
                                    keywords.addAll(Arrays.asList(text.split(",")));

                                d.select("div[fairemail=signature]").remove();
                                d.select("div[fairemail=reference]").remove();

                                String text = d.text();
                                for (String keyword : keywords)
                                    if (text.matches("(?si).*\\b" + Pattern.quote(keyword.trim()) + "\\b.*")) {
                                        args.putBoolean("remind_attachment", true);
                                        break;
                                    }
                            }

                            // Check size
                            if (identity != null && identity.max_size != null) {
                                Properties props = MessageHelper.getSessionProperties();
                                if (identity.unicode)
                                    props.put("mail.mime.allowutf8", "true");
                                Session isession = Session.getInstance(props, null);
                                Message imessage = MessageHelper.from(context, draft, identity, isession, false);

                                File file = draft.getRawFile(context);
                                try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
                                    imessage.writeTo(os);
                                }

                                long size = file.length();
                                if (size > identity.max_size) {
                                    args.putBoolean("remind_size", true);
                                    args.putLong("size", size);
                                    args.putLong("max_size", identity.max_size);
                                }
                            }

                        } else {
                            int mid;
                            if (action == R.id.action_undo)
                                mid = R.string.title_undo;
                            else if (action == R.id.action_redo)
                                mid = R.string.title_redo;
                            else
                                mid = R.string.title_draft_saved;
                            final String msg = context.getString(mid) +
                                    (BuildConfig.DEBUG ? ":" + draft.revision : "");

                            getMainHandler().post(new Runnable() {
                                public void run() {
                                    ToastEx.makeText(context, msg, Toast.LENGTH_LONG).show();
                                }
                            });
                        }

                    } else if (action == R.id.action_send) {
                        if (draft.plain_only == null || !draft.plain_only) {
                            // Remove unused inline images
                            List<String> cids = new ArrayList<>();
                            Document d = JsoupEx.parse(body);
                            for (Element element : d.select("img")) {
                                String src = element.attr("src");
                                if (src.startsWith("cid:"))
                                    cids.add("<" + src.substring(4) + ">");
                            }

                            for (EntityAttachment attachment : new ArrayList<>(attachments))
                                if (attachment.isInline() && attachment.isImage() &&
                                        attachment.cid != null && !cids.contains(attachment.cid)) {
                                    Log.i("Removing unused inline attachment cid=" + attachment.cid);
                                    db.attachment().deleteAttachment(attachment.id);
                                }
                        } else {
                            // Convert inline images to attachments
                            for (EntityAttachment attachment : new ArrayList<>(attachments))
                                if (attachment.isInline() && attachment.isImage()) {
                                    Log.i("Converting to attachment cid=" + attachment.cid);
                                    attachment.disposition = Part.ATTACHMENT;
                                    db.attachment().setDisposition(attachment.id, attachment.disposition);
                                }
                        }

                        // Delete draft (cannot move to outbox)
                        EntityOperation.queue(context, draft, EntityOperation.DELETE);

                        EntityFolder outbox = db.folder().getOutbox();
                        if (outbox == null) {
                            Log.w("Outbox missing");
                            outbox = EntityFolder.getOutbox();
                            outbox.id = db.folder().insertFolder(outbox);
                        }

                        // Copy message to outbox
                        draft.id = null;
                        draft.folder = outbox.id;
                        draft.uid = null;
                        draft.fts = false;
                        draft.ui_hide = false;
                        draft.id = db.message().insertMessage(draft);
                        Helper.writeText(draft.getFile(context), body);

                        // Move attachments
                        for (EntityAttachment attachment : attachments)
                            db.attachment().setMessage(attachment.id, draft.id);

                        // Delay sending message
                        int send_delayed = prefs.getInt("send_delayed", 0);
                        if (draft.ui_snoozed == null && send_delayed != 0) {
                            if (extras.getBoolean("now"))
                                draft.ui_snoozed = null;
                            else
                                draft.ui_snoozed = new Date().getTime() + send_delayed * 1000L;
                            db.message().setMessageSnoozed(draft.id, draft.ui_snoozed);
                        }

                        // Send message
                        if (draft.ui_snoozed == null)
                            EntityOperation.queue(context, draft, EntityOperation.SEND);

                        final String feedback;
                        if (draft.ui_snoozed == null)
                            feedback = context.getString(R.string.title_queued);
                        else {
                            DateFormat DTF = Helper.getDateTimeInstance(context);
                            feedback = context.getString(R.string.title_queued_at, DTF.format(draft.ui_snoozed));
                        }

                        getMainHandler().post(new Runnable() {
                            public void run() {
                                ToastEx.makeText(context, feedback, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }

            ServiceSynchronize.eval(context, "compose/action");

            if (action == R.id.action_send)
                if (draft.ui_snoozed == null)
                    ServiceSend.start(context);
                else {
                    Log.i("Delayed send id=" + draft.id + " at " + new Date(draft.ui_snoozed));
                    EntityMessage.snooze(context, draft.id, draft.ui_snoozed);
                }

            return draft;
        }

        @Override
        protected void onExecuted(Bundle args, EntityMessage draft) {
            if (draft == null)
                return;

            boolean needsEncryption = args.getBoolean("needsEncryption");
            int action = args.getInt("action");
            Log.i("Loaded action id=" + draft.id +
                    " action=" + getActionName(action) + " encryption=" + needsEncryption);

            etTo.setText(MessageHelper.formatAddressesCompose(draft.to));
            etCc.setText(MessageHelper.formatAddressesCompose(draft.cc));
            etBcc.setText(MessageHelper.formatAddressesCompose(draft.bcc));

            Bundle extras = args.getBundle("extras");
            boolean show = extras.getBoolean("show");
            boolean html = extras.containsKey("html");
            if (show)
                showDraft(draft, html);

            bottom_navigation.getMenu().findItem(R.id.action_undo).setVisible(draft.revision > 1);
            bottom_navigation.getMenu().findItem(R.id.action_redo).setVisible(draft.revision < draft.revisions);

            if (needsEncryption) {
                if (ActivityBilling.isPro(getContext()) ||
                        EntityMessage.PGP_SIGNONLY.equals(draft.ui_encrypt) ||
                        EntityMessage.PGP_SIGNENCRYPT.equals(draft.ui_encrypt)) {
                    boolean interactive = args.getBoolean("interactive");
                    onEncrypt(draft, action, extras, interactive);
                } else
                    startActivity(new Intent(getContext(), ActivityBilling.class));
                return;
            }

            if (action == R.id.action_delete) {
                state = State.NONE;
                finish();

            } else if (action == R.id.action_undo || action == R.id.action_redo) {
                showDraft(draft, false);

            } else if (action == R.id.action_save) {
                // Do nothing

            } else if (action == R.id.action_check) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                boolean send_dialog = prefs.getBoolean("send_dialog", true);
                boolean send_reminders = prefs.getBoolean("send_reminders", true);

                String address_error = args.getString("address_error");
                String mx_error = args.getString("mx_error");
                boolean remind_to = args.getBoolean("remind_to", false);
                boolean remind_extra = args.getBoolean("remind_extra", false);
                boolean remind_pgp = args.getBoolean("remind_pgp", false);
                boolean remind_subject = args.getBoolean("remind_subject", false);
                boolean remind_text = args.getBoolean("remind_text", false);
                boolean remind_attachment = args.getBoolean("remind_attachment", false);
                boolean remind_size = args.getBoolean("remind_size", false);
                boolean formatted = args.getBoolean("formatted", false);

                int recipients = (draft.to == null ? 0 : draft.to.length) +
                        (draft.cc == null ? 0 : draft.cc.length) +
                        (draft.bcc == null ? 0 : draft.bcc.length);
                if (send_dialog || address_error != null || mx_error != null || recipients > RECIPIENTS_WARNING || remind_size ||
                        (formatted && (draft.plain_only != null && draft.plain_only)) ||
                        (send_reminders &&
                                (remind_to || remind_extra || remind_pgp || remind_subject || remind_text || remind_attachment))) {
                    setBusy(false);

                    FragmentDialogSend fragment = new FragmentDialogSend();
                    fragment.setArguments(args);
                    fragment.setTargetFragment(FragmentCompose.this, REQUEST_SEND);
                    fragment.show(getParentFragmentManager(), "compose:send");
                } else
                    onAction(R.id.action_send, "dialog");

            } else if (action == R.id.action_send) {
                state = State.NONE;
                finish();
            }
        }

        @Override
        protected void onException(Bundle args, Throwable ex) {
            if (ex instanceof MessageRemovedException)
                finish();
            else {
                setBusy(false);
                if (ex instanceof IllegalArgumentException)
                    Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG)
                            .setGestureInsetBottomIgnored(true).show();
                else
                    Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }

        private String getActionName(int id) {
            switch (id) {
                case R.id.action_delete:
                    return "delete";
                case R.id.action_undo:
                    return "undo";
                case R.id.action_redo:
                    return "redo";
                case R.id.action_save:
                    return "save";
                case R.id.action_check:
                    return "check";
                case R.id.action_send:
                    return "send";
                default:
                    return Integer.toString(id);
            }
        }

        private void setBusy(boolean busy) {
            state = (busy ? State.LOADING : State.LOADED);
            Helper.setViewsEnabled(view, !busy);
            getActivity().invalidateOptionsMenu();
        }

        private void checkAddress(InternetAddress[] addresses, Context context) throws AddressException {
            if (addresses == null)
                return;

            for (InternetAddress address : addresses)
                try {
                    address.validate();
                } catch (AddressException ex) {
                    throw new AddressException(context.getString(R.string.title_address_parse_error,
                            MessageHelper.formatAddressesCompose(addresses), ex.getMessage()));
                }
        }

        private void checkMx(InternetAddress[] addresses, Context context) throws UnknownHostException {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            boolean lookup_mx = prefs.getBoolean("lookup_mx", false);
            if (!lookup_mx)
                return;

            if (addresses == null)
                return;

            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ani = (cm == null ? null : cm.getActiveNetworkInfo());
            if (ani != null && ani.isConnected())
                DnsHelper.checkMx(context, addresses);
        }
    };

    private static String unprefix(String subject, String prefix) {
        subject = subject.trim();
        prefix = prefix.trim().toLowerCase();
        while (subject.toLowerCase().startsWith(prefix))
            subject = subject.substring(prefix.length()).trim();
        return subject;
    }

    private static void addSignature(Context context, Document document, EntityMessage message, EntityIdentity identity) {
        if (!message.signature ||
                identity == null || TextUtils.isEmpty(identity.signature))
            return;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int signature_location = prefs.getInt("signature_location", 1);
        boolean usenet = prefs.getBoolean("usenet_signature", false);

        Element div = document.createElement("div");
        div.attr("fairemail", "signature");

        if (usenet) {
            // https://www.ietf.org/rfc/rfc3676.txt
            Element span = document.createElement("span");
            span.text("-- ");
            span.appendElement("br");
            div.appendChild(span);
        }

        div.append(identity.signature);

        Elements ref = document.select("div[fairemail=reference]");
        if (signature_location == 0)
            document.body().prependChild(div);
        else if (ref.size() == 0 || signature_location == 2)
            document.body().appendChild(div);
        else if (signature_location == 1)
            ref.first().before(div);
    }

    private void showDraft(final EntityMessage draft, final boolean scroll) {
        Bundle args = new Bundle();
        args.putLong("id", draft.id);
        args.putBoolean("show_images", show_images);

        new SimpleTask<Spanned[]>() {
            @Override
            protected void onPreExecute(Bundle args) {
                // Needed to get width for images
                grpBody.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onPostExecute(Bundle args) {
                pbWait.setVisibility(View.GONE);
                media_bar.setVisibility(media ? View.VISIBLE : View.GONE);
                bottom_navigation.getMenu().findItem(R.id.action_undo).setVisible(draft.revision > 1);
                bottom_navigation.getMenu().findItem(R.id.action_redo).setVisible(draft.revision < draft.revisions);
                bottom_navigation.setVisibility(View.VISIBLE);

                Helper.setViewsEnabled(view, true);

                getActivity().invalidateOptionsMenu();
            }

            @Override
            protected Spanned[] onExecute(final Context context, Bundle args) throws Throwable {
                final long id = args.getLong("id");
                final boolean show_images = args.getBoolean("show_images", false);

                int colorPrimary = Helper.resolveColor(context, R.attr.colorPrimary);
                int dp3 = Helper.dp2pixels(context, 3);
                int dp6 = Helper.dp2pixels(context, 6);

                DB db = DB.getInstance(context);
                EntityMessage draft = db.message().getMessage(id);
                if (draft == null || !draft.content)
                    throw new IllegalArgumentException(context.getString(R.string.title_no_body));

                Document doc = JsoupEx.parse(draft.getFile(context));
                doc.select("div[fairemail=signature]").remove();
                Elements ref = doc.select("div[fairemail=reference]");
                ref.remove();

                Spanned spannedBody = HtmlHelper.fromDocument(context, doc, false, new Html.ImageGetter() {
                    @Override
                    public Drawable getDrawable(String source) {
                        return ImageHelper.decodeImage(context, id, source, true, zoom, 1.0f, etBody);
                    }
                }, null);

                SpannableStringBuilder bodyBuilder = new SpannableStringBuilder(spannedBody);
                QuoteSpan[] bodySpans = bodyBuilder.getSpans(0, bodyBuilder.length(), QuoteSpan.class);
                for (QuoteSpan quoteSpan : bodySpans) {
                    QuoteSpan q;
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P)
                        q = new QuoteSpan(colorPrimary);
                    else
                        q = new QuoteSpan(colorPrimary, dp3, dp6);
                    bodyBuilder.setSpan(q,
                            bodyBuilder.getSpanStart(quoteSpan),
                            bodyBuilder.getSpanEnd(quoteSpan),
                            bodyBuilder.getSpanFlags(quoteSpan));
                    bodyBuilder.removeSpan(quoteSpan);
                }

                spannedBody = bodyBuilder;

                Spanned spannedRef = null;
                if (!ref.isEmpty()) {
                    Document dref = JsoupEx.parse(ref.outerHtml());
                    Document quote = HtmlHelper.sanitizeView(context, dref, show_images);
                    spannedRef = HtmlHelper.fromDocument(context, quote, true,
                            new Html.ImageGetter() {
                                @Override
                                public Drawable getDrawable(String source) {
                                    return ImageHelper.decodeImage(context, id, source, show_images, zoom, 1.0f, tvReference);
                                }
                            },
                            null);

                    // Strip newline of reply header
                    if (spannedRef.length() > 0 && spannedRef.charAt(0) == '\n')
                        spannedRef = (Spanned) spannedRef.subSequence(1, spannedRef.length());
                }

                args.putBoolean("ref_has_images", spannedRef != null &&
                        spannedRef.getSpans(0, spannedRef.length(), ImageSpan.class).length > 0);

                return new Spanned[]{spannedBody, spannedRef};
            }

            @Override
            protected void onExecuted(Bundle args, Spanned[] text) {
                etBody.setText(text[0]);
                if (scroll && text[0] != null)
                    etBody.setSelection(text[0].length());
                else
                    etBody.setSelection(0);
                grpBody.setVisibility(View.VISIBLE);

                cbSignature.setChecked(draft.signature);
                tvSignature.setAlpha(draft.signature ? 1.0f : Helper.LOW_LIGHT);

                boolean ref_has_images = args.getBoolean("ref_has_images");

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                boolean ref_hint = prefs.getBoolean("compose_reference", true);

                tvReference.setText(text[1]);
                tvReference.setVisibility(text[1] == null ? View.GONE : View.VISIBLE);
                grpReferenceHint.setVisibility(text[1] == null || !ref_hint ? View.GONE : View.VISIBLE);
                ibReferenceEdit.setVisibility(text[1] == null ? View.GONE : View.VISIBLE);
                ibReferenceImages.setVisibility(ref_has_images && !show_images ? View.VISIBLE : View.GONE);

                setBodyPadding();

                state = State.LOADED;

                final Context context = getContext();

                final View target;
                if (TextUtils.isEmpty(etTo.getText().toString().trim()))
                    target = etTo;
                else if (TextUtils.isEmpty(etSubject.getText().toString()))
                    target = etSubject;
                else
                    target = etBody;

                getMainHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        target.requestFocus();

                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                        boolean keyboard = prefs.getBoolean("keyboard", true);
                        if (keyboard) {
                            InputMethodManager imm =
                                    (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                            if (imm != null)
                                imm.showSoftInput(target, InputMethodManager.SHOW_IMPLICIT);
                        }
                    }
                });
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "compose:show");
    }

    private void setBodyPadding() {
        // Keep room for the style toolbar
        boolean pad =
                (grpSignature.getVisibility() == View.GONE &&
                        tvReference.getVisibility() == View.GONE);
        etBody.setPadding(0, 0, 0, pad ? Helper.dp2pixels(getContext(), 36) : 0);
    }

    private AdapterView.OnItemSelectedListener identitySelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            EntityIdentity identity = (EntityIdentity) parent.getAdapter().getItem(position);

            int at = (identity == null ? -1 : identity.email.indexOf('@'));
            etExtra.setHint(at < 0 ? null : identity.email.substring(0, at));
            tvDomain.setText(at < 0 ? null : identity.email.substring(at));
            grpExtra.setVisibility(identity != null && identity.sender_extra ? View.VISIBLE : View.GONE);

            Spanned signature = null;
            if (identity != null && !TextUtils.isEmpty(identity.signature))
                signature = HtmlHelper.fromHtml(identity.signature, false, new Html.ImageGetter() {
                    @Override
                    public Drawable getDrawable(String source) {
                        return ImageHelper.decodeImage(getContext(), working, source, true, 0, 1.0f, tvSignature);
                    }
                }, null, getContext());
            tvSignature.setText(signature);
            grpSignature.setVisibility(signature == null ? View.GONE : View.VISIBLE);

            setBodyPadding();

            updateEncryption();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            etExtra.setHint("");
            tvDomain.setText(null);

            tvSignature.setText(null);
            grpSignature.setVisibility(View.GONE);

            setBodyPadding();

            updateEncryption();
        }

        private void updateEncryption() {
            EntityIdentity identity = (EntityIdentity) spIdentity.getSelectedItem();
            if (identity == null)
                return;

            Bundle args = new Bundle();
            args.putLong("id", working);
            args.putLong("identity", identity.id);

            new SimpleTask<Integer>() {
                @Override
                protected Integer onExecute(Context context, Bundle args) {
                    long id = args.getLong("id");
                    long iid = args.getLong("identity");

                    DB db = DB.getInstance(context);
                    EntityMessage draft = db.message().getMessage(id);
                    if (draft == null ||
                            draft.ui_encrypt == null || EntityMessage.ENCRYPT_NONE.equals(draft.ui_encrypt))
                        return null;

                    if (draft.identity != null && draft.identity.equals(iid))
                        return draft.ui_encrypt;

                    EntityIdentity identity = db.identity().getIdentity(iid);
                    if (identity == null)
                        return null;

                    int encrypt = draft.ui_encrypt;
                    if (identity.encrypt == 0) {
                        if (EntityMessage.SMIME_SIGNONLY.equals(draft.ui_encrypt))
                            encrypt = EntityMessage.PGP_SIGNONLY;
                        else if (EntityMessage.SMIME_SIGNENCRYPT.equals(draft.ui_encrypt))
                            encrypt = EntityMessage.PGP_SIGNENCRYPT;
                    } else {
                        if (EntityMessage.PGP_SIGNONLY.equals(draft.ui_encrypt))
                            encrypt = EntityMessage.SMIME_SIGNONLY;
                        else if (EntityMessage.PGP_SIGNENCRYPT.equals(draft.ui_encrypt))
                            encrypt = EntityMessage.SMIME_SIGNENCRYPT;
                    }

                    if (draft.ui_encrypt != encrypt)
                        db.message().setMessageUiEncrypt(draft.id, encrypt);

                    return encrypt;
                }

                @Override
                protected void onExecuted(Bundle args, Integer encrypt) {
                    FragmentCompose.this.encrypt = encrypt;
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(getParentFragmentManager(), ex);
                }
            }.execute(FragmentCompose.this, args, "compose:identity");
        }
    };

    private ActivityBase.IKeyPressedListener onKeyPressedListener = new ActivityBase.IKeyPressedListener() {
        @Override
        public boolean onKeyPressed(KeyEvent event) {
            if (event.isCtrlPressed() && event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_ENTER:
                        onAction(R.id.action_check, "enter");
                        return true;
                    case KeyEvent.KEYCODE_B:
                        if (etBody.hasSelection())
                            return StyleHelper.apply(R.id.menu_bold, null, etBody);
                        else
                            return false;
                    case KeyEvent.KEYCODE_I:
                        if (etBody.hasSelection())
                            return StyleHelper.apply(R.id.menu_italic, null, etBody);
                        else
                            return false;
                    case KeyEvent.KEYCODE_U:
                        if (etBody.hasSelection())
                            return StyleHelper.apply(R.id.menu_underline, null, etBody);
                        else
                            return false;
                }
            }

            return false;
        }

        @Override
        public boolean onBackPressed() {
            if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                onExit();
                return true;
            } else
                return false;
        }
    };

    public static class FragmentDialogContactGroup extends FragmentDialogBase {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            final long working = getArguments().getLong("working");
            int focussed = getArguments().getInt("focussed");

            final Context context = getContext();

            View dview = LayoutInflater.from(context).inflate(R.layout.dialog_contact_group, null);
            final Spinner spGroup = dview.findViewById(R.id.spGroup);
            final Spinner spTarget = dview.findViewById(R.id.spTarget);

            Cursor groups = context.getContentResolver().query(
                    ContactsContract.Groups.CONTENT_SUMMARY_URI,
                    new String[]{
                            ContactsContract.Groups._ID,
                            ContactsContract.Groups.TITLE,
                            ContactsContract.Groups.SUMMARY_COUNT,
                            ContactsContract.Groups.ACCOUNT_NAME,
                            ContactsContract.Groups.ACCOUNT_TYPE,
                    },
                    // ContactsContract.Groups.GROUP_VISIBLE + " = 1" + " AND " +
                    ContactsContract.Groups.DELETED + " = 0" +
                            " AND " + ContactsContract.Groups.SUMMARY_COUNT + " > 0",
                    null,
                    ContactsContract.Groups.TITLE
            );

            SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                    context,
                    R.layout.spinner_contact_group,
                    groups,
                    new String[]{ContactsContract.Groups.TITLE, ContactsContract.Groups.ACCOUNT_NAME},
                    new int[]{R.id.tvGroup, R.id.tvAccount},
                    0);

            final NumberFormat NF = NumberFormat.getInstance();

            adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
                @Override
                public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                    if (view.getId() == R.id.tvGroup) {
                        String title = cursor.getString(1);
                        int count = cursor.getInt(2);
                        ((TextView) view).setText(context.getString(R.string.title_name_count, title, NF.format(count)));
                        return true;
                    } else if (view.getId() == R.id.tvAccount && BuildConfig.DEBUG) {
                        String account = cursor.getString(3);
                        String type = cursor.getString(4);
                        ((TextView) view).setText(account + ":" + type);
                        return true;
                    } else
                        return false;
                }
            });

            spGroup.setAdapter(adapter);

            spTarget.setSelection(focussed);

            return new AlertDialog.Builder(context)
                    .setView(dview)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int target = spTarget.getSelectedItemPosition();
                            Cursor cursor = (Cursor) spGroup.getSelectedItem();
                            if (target != INVALID_POSITION && cursor != null) {
                                long group = cursor.getLong(0);

                                Bundle args = getArguments();
                                args.putLong("id", working);
                                args.putInt("target", target);
                                args.putLong("group", group);

                                sendResult(RESULT_OK);
                            } else
                                sendResult(RESULT_CANCELED);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
        }
    }

    public static class FragmentDialogAddImage extends FragmentDialogBase {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            int title = getArguments().getInt("title");

            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            boolean add_inline = prefs.getBoolean("add_inline", true);
            boolean resize_images = prefs.getBoolean("resize_images", true);
            int resize = prefs.getInt("resize", FragmentCompose.REDUCED_IMAGE_SIZE);
            boolean privacy_images = prefs.getBoolean("privacy_images", false);
            boolean image_dialog = prefs.getBoolean("image_dialog", true);

            final ViewGroup dview = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_image, null);
            final RadioGroup rgAction = dview.findViewById(R.id.rgAction);
            final CheckBox cbResize = dview.findViewById(R.id.cbResize);
            final Spinner spResize = dview.findViewById(R.id.spResize);
            final TextView tvResize = dview.findViewById(R.id.tvResize);
            final CheckBox cbPrivacy = dview.findViewById(R.id.cbPrivacy);
            final CheckBox cbNotAgain = dview.findViewById(R.id.cbNotAgain);
            final TextView tvNotAgain = dview.findViewById(R.id.tvNotAgain);

            rgAction.check(add_inline ? R.id.rbInline : R.id.rbAttach);
            cbResize.setChecked(resize_images);
            spResize.setEnabled(resize_images);
            cbPrivacy.setChecked(privacy_images);

            final int[] resizeValues = getResources().getIntArray(R.array.resizeValues);
            for (int pos = 0; pos < resizeValues.length; pos++)
                if (resizeValues[pos] == resize) {
                    spResize.setSelection(pos);
                    tvResize.setText(getString(R.string.title_add_resize_pixels, resizeValues[pos]));
                    break;
                }

            rgAction.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    prefs.edit().putBoolean("add_inline", checkedId == R.id.rbInline).apply();
                }
            });

            cbResize.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    prefs.edit().putBoolean("resize_images", isChecked).apply();
                    spResize.setEnabled(isChecked);
                }
            });

            spResize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                    prefs.edit().putInt("resize", resizeValues[position]).apply();
                    tvResize.setText(getString(R.string.title_add_resize_pixels, resizeValues[position]));
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    prefs.edit().remove("resize").apply();
                }
            });

            cbPrivacy.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    prefs.edit().putBoolean("privacy_images", isChecked).apply();
                }
            });

            cbNotAgain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    prefs.edit().putBoolean("image_dialog", !isChecked).apply();
                    tvNotAgain.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                }
            });

            cbNotAgain.setChecked(!image_dialog);
            tvNotAgain.setVisibility(cbNotAgain.isChecked() ? View.VISIBLE : View.GONE);

            return new AlertDialog.Builder(getContext())
                    .setView(dview)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(title,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    sendResult(RESULT_OK);
                                }
                            })
                    .create();
        }
    }

    public static class FragmentDialogSend extends FragmentDialogBase {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            Bundle args = getArguments();
            long id = args.getLong("id");
            String address_error = args.getString("address_error");
            String mx_error = args.getString("mx_error");
            final boolean remind_to = args.getBoolean("remind_to", false);
            final boolean remind_extra = args.getBoolean("remind_extra", false);
            final boolean remind_pgp = args.getBoolean("remind_pgp", false);
            final boolean remind_subject = args.getBoolean("remind_subject", false);
            final boolean remind_text = args.getBoolean("remind_text", false);
            final boolean remind_attachment = args.getBoolean("remind_attachment", false);
            final boolean remind_size = args.getBoolean("remind_size", false);
            final boolean formatted = args.getBoolean("formatted", false);
            final long size = args.getLong("size", -1);
            final long max_size = args.getLong("max_size", -1);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            boolean send_reminders = prefs.getBoolean("send_reminders", true);
            int send_delayed = prefs.getInt("send_delayed", 0);
            boolean send_dialog = prefs.getBoolean("send_dialog", true);

            final int[] encryptValues = getResources().getIntArray(R.array.encryptValues);
            final int[] sendDelayedValues = getResources().getIntArray(R.array.sendDelayedValues);
            final String[] sendDelayedNames = getResources().getStringArray(R.array.sendDelayedNames);

            final ViewGroup dview = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.dialog_send, null);
            final TextView tvAddressError = dview.findViewById(R.id.tvAddressError);
            final TextView tvRemindSize = dview.findViewById(R.id.tvRemindSize);
            final TextView tvRemindTo = dview.findViewById(R.id.tvRemindTo);
            final TextView tvRemindExtra = dview.findViewById(R.id.tvRemindExtra);
            final TextView tvRemindPgp = dview.findViewById(R.id.tvRemindPgp);
            final TextView tvRemindSubject = dview.findViewById(R.id.tvRemindSubject);
            final TextView tvRemindText = dview.findViewById(R.id.tvRemindText);
            final TextView tvRemindAttachment = dview.findViewById(R.id.tvRemindAttachment);
            final SwitchCompat swSendReminders = dview.findViewById(R.id.swSendReminders);
            final TextView tvSendRemindersHint = dview.findViewById(R.id.tvSendRemindersHint);
            final TextView tvTo = dview.findViewById(R.id.tvTo);
            final TextView tvVia = dview.findViewById(R.id.tvVia);
            final CheckBox cbPlainOnly = dview.findViewById(R.id.cbPlainOnly);
            final TextView tvRemindPlain = dview.findViewById(R.id.tvRemindPlain);
            final CheckBox cbReceipt = dview.findViewById(R.id.cbReceipt);
            final TextView tvReceipt = dview.findViewById(R.id.tvReceiptType);
            final Spinner spEncrypt = dview.findViewById(R.id.spEncrypt);
            final ImageButton ibEncryption = dview.findViewById(R.id.ibEncryption);
            final Spinner spPriority = dview.findViewById(R.id.spPriority);
            final TextView tvSendAt = dview.findViewById(R.id.tvSendAt);
            final ImageButton ibSendAt = dview.findViewById(R.id.ibSendAt);
            final CheckBox cbNotAgain = dview.findViewById(R.id.cbNotAgain);
            final TextView tvNotAgain = dview.findViewById(R.id.tvNotAgain);

            tvAddressError.setText(address_error == null ? mx_error : address_error);
            tvAddressError.setVisibility(address_error == null && mx_error == null ? View.GONE : View.VISIBLE);

            tvRemindSize.setText(getString(R.string.title_size_reminder,
                    Helper.humanReadableByteCount(size),
                    Helper.humanReadableByteCount(max_size)));
            tvRemindSize.setVisibility(remind_size ? View.VISIBLE : View.GONE);

            tvRemindTo.setVisibility(send_reminders && remind_to ? View.VISIBLE : View.GONE);
            tvRemindExtra.setVisibility(send_reminders && remind_extra ? View.VISIBLE : View.GONE);
            tvRemindPgp.setVisibility(send_reminders && remind_pgp ? View.VISIBLE : View.GONE);
            tvRemindSubject.setVisibility(send_reminders && remind_subject ? View.VISIBLE : View.GONE);
            tvRemindText.setVisibility(send_reminders && remind_text ? View.VISIBLE : View.GONE);
            tvRemindAttachment.setVisibility(send_reminders && remind_attachment ? View.VISIBLE : View.GONE);

            tvTo.setText(null);
            tvVia.setText(null);
            tvRemindPlain.setVisibility(View.GONE);
            tvReceipt.setVisibility(View.GONE);
            spEncrypt.setTag(0);
            spEncrypt.setSelection(0);
            spPriority.setTag(1);
            spPriority.setSelection(1);
            tvSendAt.setText(null);
            cbNotAgain.setChecked(!send_dialog);
            cbNotAgain.setVisibility(send_dialog ? View.VISIBLE : View.GONE);
            tvNotAgain.setVisibility(cbNotAgain.isChecked() ? View.VISIBLE : View.GONE);

            Helper.setViewsEnabled(dview, false);

            boolean reminder = (remind_to || remind_extra || remind_pgp || remind_subject || remind_text || remind_attachment);
            swSendReminders.setChecked(send_reminders);
            swSendReminders.setVisibility(send_reminders && reminder ? View.VISIBLE : View.GONE);
            tvSendRemindersHint.setVisibility(View.GONE);
            swSendReminders.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                    prefs.edit().putBoolean("send_reminders", checked).apply();
                    tvRemindTo.setVisibility(checked && remind_to ? View.VISIBLE : View.GONE);
                    tvRemindExtra.setVisibility(checked && remind_extra ? View.VISIBLE : View.GONE);
                    tvRemindPgp.setVisibility(checked && remind_pgp ? View.VISIBLE : View.GONE);
                    tvRemindSubject.setVisibility(checked && remind_subject ? View.VISIBLE : View.GONE);
                    tvRemindText.setVisibility(checked && remind_text ? View.VISIBLE : View.GONE);
                    tvRemindAttachment.setVisibility(checked && remind_attachment ? View.VISIBLE : View.GONE);
                    tvSendRemindersHint.setVisibility(checked ? View.GONE : View.VISIBLE);
                }
            });

            cbNotAgain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    prefs.edit().putBoolean("send_dialog", !isChecked).apply();
                    tvNotAgain.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                }
            });

            cbPlainOnly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                    tvRemindPlain.setVisibility(checked && formatted ? View.VISIBLE : View.GONE);

                    Bundle args = new Bundle();
                    args.putLong("id", id);
                    args.putBoolean("plain_only", checked);

                    new SimpleTask<Void>() {
                        @Override
                        protected Void onExecute(Context context, Bundle args) {
                            long id = args.getLong("id");
                            boolean plain_only = args.getBoolean("plain_only");

                            DB db = DB.getInstance(context);
                            db.message().setMessagePlainOnly(id, plain_only);

                            return null;
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Log.unexpectedError(getParentFragmentManager(), ex);
                        }
                    }.execute(FragmentDialogSend.this, args, "compose:plain_only");
                }
            });

            cbReceipt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                    tvReceipt.setVisibility(checked ? View.VISIBLE : View.GONE);

                    Bundle args = new Bundle();
                    args.putLong("id", id);
                    args.putBoolean("receipt", checked);

                    new SimpleTask<Void>() {
                        @Override
                        protected Void onExecute(Context context, Bundle args) {
                            long id = args.getLong("id");
                            boolean receipt = args.getBoolean("receipt");

                            DB db = DB.getInstance(context);
                            db.message().setMessageReceiptRequest(id, receipt);

                            return null;
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Log.unexpectedError(getParentFragmentManager(), ex);
                        }
                    }.execute(FragmentDialogSend.this, args, "compose:receipt");
                }
            });

            spEncrypt.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    int last = (int) spEncrypt.getTag();
                    if (last != position) {
                        spEncrypt.setTag(position);
                        setEncrypt(encryptValues[position]);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    spEncrypt.setTag(0);
                    setEncrypt(encryptValues[0]);
                }

                private void setEncrypt(int encrypt) {
                    Bundle args = new Bundle();
                    args.putLong("id", id);
                    args.putInt("encrypt", encrypt);

                    new SimpleTask<Void>() {
                        @Override
                        protected Void onExecute(Context context, Bundle args) {
                            long id = args.getLong("id");
                            int encrypt = args.getInt("encrypt");

                            DB db = DB.getInstance(context);
                            try {
                                db.beginTransaction();

                                EntityMessage message = db.message().getMessage(id);
                                if (message == null)
                                    return null;

                                db.message().setMessageUiEncrypt(message.id, encrypt);

                                if (encrypt != EntityMessage.ENCRYPT_NONE &&
                                        message.identity != null) {
                                    int iencrypt =
                                            (encrypt == EntityMessage.SMIME_SIGNONLY ||
                                                    encrypt == EntityMessage.SMIME_SIGNENCRYPT
                                                    ? 1 : 0);
                                    db.identity().setIdentityEncrypt(message.identity, iencrypt);
                                }

                                db.setTransactionSuccessful();
                            } finally {
                                db.endTransaction();
                            }

                            return null;
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Log.unexpectedError(getParentFragmentManager(), ex);
                        }
                    }.execute(FragmentDialogSend.this, args, "compose:encrypt");
                }
            });

            ibEncryption.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Helper.viewFAQ(getContext(), 12);
                }
            });

            spPriority.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    int last = (int) spPriority.getTag();
                    if (last != position) {
                        spPriority.setTag(position);
                        setPriority(position);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    spPriority.setTag(1);
                    setPriority(1);
                }

                private void setPriority(int priority) {
                    Bundle args = new Bundle();
                    args.putLong("id", id);
                    args.putInt("priority", priority);

                    new SimpleTask<Void>() {
                        @Override
                        protected Void onExecute(Context context, Bundle args) {
                            long id = args.getLong("id");
                            int priority = args.getInt("priority");

                            DB db = DB.getInstance(context);
                            db.message().setMessagePriority(id, priority);

                            return null;
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Log.unexpectedError(getParentFragmentManager(), ex);
                        }
                    }.execute(FragmentDialogSend.this, args, "compose:priority");
                }
            });

            ibSendAt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle args = new Bundle();
                    args.putString("title", getString(R.string.title_send_at));
                    args.putLong("id", id);

                    FragmentDialogDuration fragment = new FragmentDialogDuration();
                    fragment.setArguments(args);
                    fragment.setTargetFragment(FragmentDialogSend.this, 1);
                    fragment.show(getParentFragmentManager(), "send:snooze");
                }
            });

            DB db = DB.getInstance(getContext());
            db.message().liveMessage(id).observe(getViewLifecycleOwner(), new Observer<TupleMessageEx>() {
                @Override
                public void onChanged(TupleMessageEx draft) {
                    if (draft == null) {
                        dismiss();
                        return;
                    }

                    int to = (draft.to == null ? 0 : draft.to.length);
                    int cc = (draft.cc == null ? 0 : draft.cc.length) + (draft.bcc == null ? 0 : draft.bcc.length);
                    if (cc == 0)
                        tvTo.setText(MessageHelper.formatAddressesShort(draft.to));
                    else
                        tvTo.setText(getString(R.string.title_name_plus,
                                MessageHelper.formatAddressesShort(draft.to), cc));
                    tvTo.setTextColor(Helper.resolveColor(getContext(),
                            to + cc > RECIPIENTS_WARNING ? R.attr.colorWarning : android.R.attr.textColorPrimary));
                    tvVia.setText(draft.identityEmail);

                    cbPlainOnly.setChecked(draft.plain_only != null && draft.plain_only);
                    cbReceipt.setChecked(draft.receipt_request != null && draft.receipt_request);

                    cbPlainOnly.setVisibility(draft.receipt != null && draft.receipt ? View.GONE : View.VISIBLE);
                    cbReceipt.setVisibility(draft.receipt != null && draft.receipt ? View.GONE : View.VISIBLE);

                    int encrypt = (draft.ui_encrypt == null ? EntityMessage.ENCRYPT_NONE : draft.ui_encrypt);
                    for (int i = 0; i < encryptValues.length; i++)
                        if (encryptValues[i] == encrypt) {
                            spEncrypt.setTag(i);
                            spEncrypt.setSelection(i);
                            break;
                        }
                    spEncrypt.setVisibility(draft.receipt != null && draft.receipt ? View.GONE : View.VISIBLE);

                    int priority = (draft.priority == null ? 1 : draft.priority);
                    spPriority.setTag(priority);
                    spPriority.setSelection(priority);

                    if (draft.ui_snoozed == null) {
                        if (send_delayed == 0)
                            tvSendAt.setText(getString(R.string.title_now));
                        else
                            for (int pos = 0; pos < sendDelayedValues.length; pos++)
                                if (sendDelayedValues[pos] == send_delayed) {
                                    tvSendAt.setText(getString(R.string.title_after, sendDelayedNames[pos]));
                                    break;
                                }
                    } else {
                        DateFormat DTF = Helper.getDateTimeInstance(getContext(), SimpleDateFormat.MEDIUM, SimpleDateFormat.SHORT);
                        DateFormat D = new SimpleDateFormat("E");
                        tvSendAt.setText(D.format(draft.ui_snoozed) + " " + DTF.format(draft.ui_snoozed));
                    }

                    Helper.setViewsEnabled(dview, true);
                }
            });

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                    .setView(dview)
                    .setNegativeButton(android.R.string.cancel, null);

            if (address_error == null && !remind_to && !remind_size) {
                if (send_delayed != 0)
                    builder.setNeutralButton(R.string.title_send_now, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sendResult(Activity.RESULT_FIRST_USER);
                        }
                    });
                builder.setPositiveButton(R.string.title_send, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendResult(Activity.RESULT_OK);
                    }
                });
            }

            return builder.create();
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
            super.onActivityResult(requestCode, resultCode, intent);

            if (resultCode == RESULT_OK && intent != null) {
                Bundle data = intent.getBundleExtra("args");
                long id = data.getLong("id");
                long duration = data.getLong("duration");
                long time = data.getLong("time");

                Bundle args = new Bundle();
                args.putLong("id", id);
                args.putLong("wakeup", duration == 0 ? -1 : time);

                new SimpleTask<Void>() {
                    @Override
                    protected Void onExecute(Context context, Bundle args) {
                        long id = args.getLong("id");
                        long wakeup = args.getLong("wakeup");

                        DB db = DB.getInstance(context);
                        db.message().setMessageSnoozed(id, wakeup < 0 ? null : wakeup);

                        return null;
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Log.unexpectedError(getParentFragmentManager(), ex);
                    }
                }.execute(this, args, "compose:snooze");
            }
        }
    }

    private static UriInfo getInfo(Uri uri, Context context) {
        UriInfo result = new UriInfo();
        try {
            DocumentFile dfile = DocumentFile.fromSingleUri(context, uri);
            if (dfile != null) {
                result.name = dfile.getName();
                result.type = dfile.getType();
                result.size = dfile.length();
            }
        } catch (SecurityException ex) {
            Log.e(ex);
        }

        // Check name
        if (TextUtils.isEmpty(result.name))
            result.name = uri.getLastPathSegment();

        // Check type
        if (!TextUtils.isEmpty(result.type))
            try {
                new ContentType(result.type);
            } catch (ParseException ex) {
                Log.w(ex);
                result.type = null;
            }

        if (TextUtils.isEmpty(result.type) ||
                "*/*".equals(result.type) ||
                "application/octet-stream".equals(result.type))
            result.type = Helper.guessMimeType(result.name);

        if (result.size != null && result.size <= 0)
            result.size = null;

        return result;
    }

    private static class UriInfo {
        String name;
        String type;
        Long size;

        boolean isImage() {
            return Helper.isImage(type);
        }
    }

    private static class DraftData {
        private EntityMessage draft;
        private List<TupleIdentityEx> identities;
    }
}
