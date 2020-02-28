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
import android.database.MergeCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.OperationCanceledException;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.security.KeyChain;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.QuoteSpan;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.FileProvider;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.documentfile.provider.DocumentFile;
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
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.RecipientInfoGenerator;
import org.bouncycastle.cms.SignerInfoGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DigestCalculatorProvider;
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

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.BodyPart;
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
    private ImageButton ibCloseUnusedImagesHint;
    private EditTextCompose etBody;
    private TextView tvNoInternet;
    private TextView tvSignature;
    private CheckBox cbSignature;
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
    private Group grpUnusedImagesHint;
    private Group grpBody;
    private Group grpSignature;
    private Group grpReferenceHint;

    private ContentResolver resolver;
    private AdapterAttachment adapter;

    private boolean prefix_once = false;
    private boolean monospaced = false;
    private Integer encrypt = null;
    private boolean media = true;
    private boolean compact = false;
    private int zoom = 0;

    private long working = -1;
    private State state = State.NONE;
    private boolean show_images = false;
    private boolean autosave = false;
    private boolean busy = false;
    private boolean saved = false;

    private Uri photoURI = null;

    private OpenPgpServiceConnection pgpService;
    private String[] pgpUserIds;
    private long[] pgpKeyIds;
    private long pgpSignKeyId;

    static final int REDUCED_IMAGE_SIZE = 1440; // pixels
    static final int REDUCED_IMAGE_QUALITY = 90; // percent

    private static final int ADDRESS_ELLIPSIZE = 50;
    private static final int RECIPIENTS_WARNING = 10;

    private static final int REQUEST_CONTACT_TO = 1;
    private static final int REQUEST_CONTACT_CC = 2;
    private static final int REQUEST_CONTACT_BCC = 3;
    private static final int REQUEST_IMAGE = 4;
    private static final int REQUEST_ATTACHMENT = 5;
    private static final int REQUEST_TAKE_PHOTO = 6;
    private static final int REQUEST_RECORD_AUDIO = 7;
    private static final int REQUEST_OPENPGP = 8;
    private static final int REQUEST_COLOR = 9;
    private static final int REQUEST_CONTACT_GROUP = 10;
    private static final int REQUEST_ANSWER = 11;
    private static final int REQUEST_LINK = 12;
    private static final int REQUEST_DISCARD = 13;
    private static final int REQUEST_SEND = 14;
    private static final int REQUEST_CERTIFICATE = 15;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefix_once = prefs.getBoolean("prefix_once", true);
        monospaced = prefs.getBoolean("monospaced", false);
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
        ibCloseUnusedImagesHint = view.findViewById(R.id.ibCloseUnusedImagesHint);
        etBody = view.findViewById(R.id.etBody);
        tvNoInternet = view.findViewById(R.id.tvNoInternet);
        tvSignature = view.findViewById(R.id.tvSignature);
        cbSignature = view.findViewById(R.id.cbSignature);
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
        grpUnusedImagesHint = view.findViewById(R.id.grpUnusedImagesHint);
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
                if (pick.resolveActivity(getContext().getPackageManager()) == null)
                    Snackbar.make(view, R.string.title_no_contacts, Snackbar.LENGTH_LONG).show();
                else
                    startActivityForResult(Helper.getChooser(getContext(), pick), request);
            }
        };

        ibToAdd.setOnClickListener(onPick);
        ibCcAdd.setOnClickListener(onPick);
        ibBccAdd.setOnClickListener(onPick);

        setZoom();

        ibCloseUnusedImagesHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                prefs.edit().putBoolean("inline_image_hint", false).apply();
                grpUnusedImagesHint.setVisibility(View.GONE);
            }
        });

        etBody.setInputContentListener(new EditTextCompose.IInputContentListener() {
            @Override
            public void onInputContent(Uri uri) {
                onAddAttachment(uri, true);
            }
        });

        etBody.setSelectionListener(new EditTextCompose.ISelection() {
            @Override
            public void onSelected(boolean selection) {
                style_bar.setVisibility(selection ? View.VISIBLE : View.GONE);
            }
        });

        etBody.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                Activity activity = getActivity();
                if (activity != null)
                    activity.onUserInteraction();

                if (before == 0 && count == 1 && start > 0 && text.charAt(start) == '\n') {
                    // break block quotes
                    boolean broken = false;
                    SpannableStringBuilder ssb = new SpannableStringBuilder(text);
                    StyledQuoteSpan[] spans = ssb.getSpans(start + 1, start + 1, StyledQuoteSpan.class);
                    for (StyledQuoteSpan span : spans) {
                        int s = ssb.getSpanStart(span);
                        int e = ssb.getSpanEnd(span);
                        int f = ssb.getSpanFlags(span);
                        Log.i("Span " + s + "..." + e + " start=" + start);

                        if (start - s > 0 && e - (start + 1) > 0 &&
                                ssb.charAt(s - 1) == '\n' && ssb.charAt(start - 1) == '\n' &&
                                ssb.charAt(start) == '\n' && ssb.charAt(e - 1) == '\n') {
                            broken = true;

                            StyledQuoteSpan q1 = new StyledQuoteSpan(getContext(), span.getColor());
                            ssb.setSpan(q1, s, start, f);
                            Log.i("Span " + s + "..." + start);

                            StyledQuoteSpan q2 = new StyledQuoteSpan(getContext(), span.getColor());
                            ssb.setSpan(q2, start + 1, e, f);
                            Log.i("Span " + (start + 1) + "..." + e);

                            ssb.removeSpan(span);
                        }
                    }

                    if (broken) {
                        StyleSpan[] sspan = ssb.getSpans(start, start, StyleSpan.class);
                        for (StyleSpan span : sspan) {
                            int s = ssb.getSpanStart(span);
                            int e = ssb.getSpanEnd(span);
                            int f = ssb.getSpanFlags(span);
                            Log.i("Style span " + s + "..." + e + " start=" + start);

                            StyleSpan s1 = new StyleSpan(span.getStyle());
                            ssb.setSpan(s1, s, start, f);
                            Log.i("Style span " + s + "..." + start);

                            StyleSpan s2 = new StyleSpan(span.getStyle());
                            ssb.setSpan(s2, start + 1, e, f);
                            Log.i("Style span " + (start + 1) + "..." + e);

                            ssb.removeSpan(span);
                        }

                        int color = Helper.resolveColor(getContext(), android.R.attr.textColorPrimary);
                        ssb.setSpan(new ForegroundColorSpan(color), start, start, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

                        etBody.setText(ssb);
                        etBody.setSelection(start);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Do nothing
            }
        });

        cbSignature.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                Object tag = cbSignature.getTag();
                if (tag == null || !tag.equals(checked)) {
                    cbSignature.setTag(checked);
                    onAction(R.id.action_save);
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

        etBody.setTypeface(monospaced ? Typeface.MONOSPACE : Typeface.DEFAULT);
        tvReference.setTypeface(monospaced ? Typeface.MONOSPACE : Typeface.DEFAULT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            etBody.setRevealOnFocusHint(false); // Doesn't work
            tvSignature.setRevealOnFocusHint(false);
            tvReference.setRevealOnFocusHint(false);
        }

        style_bar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                return onActionStyle(item.getItemId());
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
                        onActionTakePhoto();
                        return true;
                    case R.id.menu_image:
                        onActionImage();
                        return true;
                    case R.id.menu_attachment:
                        onActionAttachment();
                        return true;
                    case R.id.menu_link:
                        onActionLink();
                        return true;
                    default:
                        return false;
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
                        onActionCheck(false);
                        break;
                    default:
                        onAction(action);
                }
                return true;
            }
        });

        //view.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);

        addKeyPressedListener(onKeyPressedListener);

        // Initialize
        setHasOptionsMenu(true);

        etExtra.setHint("");
        tvDomain.setText(null);
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

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        final boolean suggest_sent = prefs.getBoolean("suggest_sent", true);
        final boolean suggest_received = prefs.getBoolean("suggest_received", false);
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
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (view.getId() == R.id.ivPhoto) {
                    ImageView photo = (ImageView) view;

                    GradientDrawable bg = new GradientDrawable();
                    if (circular)
                        bg.setShape(GradientDrawable.OVAL);
                    else
                        bg.setCornerRadius(dp3);
                    photo.setBackground(bg);
                    photo.setClipToOutline(true);

                    if (cursor.getInt(cursor.getColumnIndex("local")) == 1)
                        photo.setImageDrawable(null);
                    else {
                        String uri = cursor.getString(columnIndex);
                        if (uri == null)
                            photo.setImageResource(R.drawable.baseline_person_24);
                        else
                            photo.setImageURI(Uri.parse(uri));
                    }
                    return true;
                }
                return false;
            }
        });

        cadapter.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
            public CharSequence convertToString(Cursor cursor) {
                int colName = cursor.getColumnIndex("name");
                int colEmail = cursor.getColumnIndex("email");
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
            }
        });

        cadapter.setFilterQueryProvider(new FilterQueryProvider() {
            public Cursor runQuery(CharSequence typed) {
                Log.i("Suggest contact=" + typed);

                MatrixCursor provided = new MatrixCursor(new String[]{"_id", "name", "email", "photo", "local"});
                if (typed == null)
                    return provided;

                String wildcard = "%" + typed + "%";
                List<Cursor> cursors = new ArrayList<>();

                boolean contacts = Helper.hasPermission(getContext(), Manifest.permission.READ_CONTACTS);
                if (contacts) {
                    Cursor cursor = resolver.query(
                            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                            new String[]{
                                    ContactsContract.CommonDataKinds.Email.CONTACT_ID,
                                    ContactsContract.Contacts.DISPLAY_NAME,
                                    ContactsContract.CommonDataKinds.Email.DATA,
                                    ContactsContract.Contacts.PHOTO_THUMBNAIL_URI
                            },
                            ContactsContract.CommonDataKinds.Email.DATA + " <> ''" +
                                    " AND (" + ContactsContract.Contacts.DISPLAY_NAME + " LIKE ?" +
                                    " OR " + ContactsContract.CommonDataKinds.Email.DATA + " LIKE ?)",
                            new String[]{wildcard, wildcard},
                            "CASE WHEN " + ContactsContract.Contacts.DISPLAY_NAME + " NOT LIKE '%@%' THEN 0 ELSE 1 END" +
                                    ", " + ContactsContract.Contacts.DISPLAY_NAME + " COLLATE NOCASE" +
                                    ", " + ContactsContract.CommonDataKinds.Email.DATA + " COLLATE NOCASE");

                    while (cursor != null && cursor.moveToNext())
                        provided.newRow()
                                .add(cursor.getLong(0)) // id
                                .add(cursor.getString(1)) // name
                                .add(cursor.getString(2)) // email
                                .add(cursor.getString(3)) // photo
                                .add(0); // local
                }
                cursors.add(provided);

                if (suggest_sent)
                    cursors.add(db.contact().searchContacts(null, EntityContact.TYPE_TO, wildcard));

                if (suggest_received)
                    cursors.add(db.contact().searchContacts(null, EntityContact.TYPE_FROM, wildcard));

                if (cursors.size() == 1)
                    return cursors.get(0);
                else
                    return new MergeCursor(cursors.toArray(new Cursor[0]));
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
        grpUnusedImagesHint.setVisibility(View.GONE);

        String pkg = Helper.getOpenKeychainPackage(getContext());
        Log.i("Binding to " + pkg);
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
                args.putString("body", HtmlHelper.toHtml(etBody.getText()));

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
                            String text = HtmlHelper.getText(ref.outerHtml());
                            Element p = document.createElement("p");
                            p.html(text.replaceAll("\\r?\\n", "<br>"));
                            document.body().appendChild(p);
                        } else {
                            Document d = HtmlHelper.sanitize(context, ref.outerHtml(), true, false);
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
                        onAction(R.id.action_save, extras);
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Log.unexpectedError(getParentFragmentManager(), ex);
                    }
                }.execute(FragmentCompose.this, args, "compose:convert");
            }

            private void deleteRef() {
                Bundle extras = new Bundle();
                extras.putString("html", HtmlHelper.toHtml(etBody.getText()));
                extras.putBoolean("show", true);
                onAction(R.id.action_save, extras);
            }
        });

        popupMenu.show();
    }

    private void onReferenceImages() {
        show_images = true;
        Bundle extras = new Bundle();
        extras.putBoolean("show", true);
        onAction(R.id.action_save, extras);
    }

    @Override
    public void onDestroyView() {
        adapter = null;

        if (pgpService != null && pgpService.isBound())
            pgpService.unbindFromService();

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
                args.putParcelableArrayList("attachments", a.getParcelableArrayList("attachments"));
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
        if (autosave && state == State.LOADED)
            onAction(R.id.action_save);

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
            Activity activity = getActivity();
            if (activity != null)
                activity.runOnUiThread(new Runnable() {
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

        menu.findItem(R.id.menu_encrypt).setActionView(R.layout.action_button);
        ImageButton ib = (ImageButton) menu.findItem(R.id.menu_encrypt).getActionView();
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMenuEncrypt();
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        menu.findItem(R.id.menu_encrypt).setVisible(state == State.LOADED);
        menu.findItem(R.id.menu_zoom).setVisible(state == State.LOADED);
        menu.findItem(R.id.menu_media).setVisible(state == State.LOADED);
        menu.findItem(R.id.menu_compact).setVisible(state == State.LOADED);
        menu.findItem(R.id.menu_clear).setVisible(state == State.LOADED);
        menu.findItem(R.id.menu_contact_group).setVisible(state == State.LOADED);
        menu.findItem(R.id.menu_answer).setVisible(state == State.LOADED);
        menu.findItem(R.id.menu_send).setVisible(state == State.LOADED);

        menu.findItem(R.id.menu_encrypt).setEnabled(!busy);
        menu.findItem(R.id.menu_zoom).setEnabled(!busy);
        menu.findItem(R.id.menu_media).setEnabled(!busy);
        menu.findItem(R.id.menu_compact).setEnabled(!busy);
        menu.findItem(R.id.menu_clear).setEnabled(!busy);
        menu.findItem(R.id.menu_contact_group).setEnabled(!busy && hasPermission(Manifest.permission.READ_CONTACTS));
        menu.findItem(R.id.menu_answer).setEnabled(!busy);
        menu.findItem(R.id.menu_send).setEnabled(!busy);

        int colorEncrypt = Helper.resolveColor(getContext(), R.attr.colorEncrypt);
        ImageButton ib = (ImageButton) menu.findItem(R.id.menu_encrypt).getActionView();
        ib.setEnabled(!busy);
        if (EntityMessage.PGP_SIGNONLY.equals(encrypt) || EntityMessage.SMIME_SIGNONLY.equals(encrypt)) {
            ib.setImageResource(R.drawable.baseline_gesture_24);
            ib.setImageTintList(null);
        } else if (EntityMessage.PGP_SIGNENCRYPT.equals(encrypt) || EntityMessage.SMIME_SIGNENCRYPT.equals(encrypt)) {
            ib.setImageResource(R.drawable.baseline_lock_24);
            ib.setImageTintList(ColorStateList.valueOf(colorEncrypt));
        } else {
            ib.setImageResource(R.drawable.baseline_lock_open_24);
            ib.setImageTintList(null);
        }

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
            case R.id.menu_media:
                onMenuMediabar();
                return true;
            case R.id.menu_compact:
                onMenuCompact();
                return true;
            case R.id.menu_clear:
                StyleHelper.apply(R.id.menu_clear, etBody);
                return true;
            case R.id.menu_legend:
                onMenuLegend();
                return true;
            case R.id.menu_contact_group:
                onMenuContactGroup();
                return true;
            case R.id.menu_answer:
                onMenuAnswer();
                return true;
            case R.id.menu_send:
                onActionCheck(true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onMenuAddresses() {
        grpAddresses.setVisibility(grpAddresses.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);

        new Handler().post(new Runnable() {
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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String encrypt_method = prefs.getString("default_encrypt_method", "pgp");

        if ("pgp".equals(encrypt_method)) {
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
        float textSize = Helper.getTextSize(getContext(), zoom);
        if (textSize != 0) {
            etBody.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            tvReference.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        }
    }

    private void onMenuMediabar() {
        media = !media;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefs.edit().putBoolean("compose_media", media).apply();
        media_bar.setVisibility(media ? View.VISIBLE : View.GONE);
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
        if (!ActivityBilling.isPro(getContext())) {
            startActivity(new Intent(getContext(), ActivityBilling.class));
            return;
        }

        FragmentDialogAnswer fragment = new FragmentDialogAnswer();
        fragment.setArguments(new Bundle());
        fragment.setTargetFragment(this, REQUEST_ANSWER);
        fragment.show(getParentFragmentManager(), "compose:answer");
    }

    private boolean onActionStyle(int action) {
        Log.i("Style action=" + action);

        if (action == R.id.menu_color) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
            if (imm != null)
                imm.hideSoftInputFromWindow(etBody.getWindowToken(), 0);

            Bundle args = new Bundle();
            args.putInt("color", Color.TRANSPARENT);
            args.putString("title", getString(R.string.title_style_color));
            args.putInt("start", etBody.getSelectionStart());
            args.putInt("end", etBody.getSelectionEnd());

            FragmentDialogColor fragment = new FragmentDialogColor();
            fragment.setArguments(args);
            fragment.setTargetFragment(FragmentCompose.this, REQUEST_COLOR);
            fragment.show(getParentFragmentManager(), "account:color");
            return true;
        } else
            return StyleHelper.apply(action, etBody);
    }

    private void onActionRecordAudio() {
        // https://developer.android.com/reference/android/provider/MediaStore.Audio.Media.html#RECORD_SOUND_ACTION
        PackageManager pm = getContext().getPackageManager();
        Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
        if (intent.resolveActivity(pm) == null) {
            Snackbar snackbar = Snackbar.make(view, getString(R.string.title_no_recorder), Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction(R.string.title_fix, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Helper.view(getContext(), Uri.parse(BuildConfig.RECORDER_URI), false);
                }
            });
            snackbar.show();
        } else
            try {
                startActivityForResult(intent, REQUEST_RECORD_AUDIO);
            } catch (SecurityException ex) {
                Log.w(ex);
                Snackbar.make(view, getString(R.string.title_no_viewer, intent.getAction()), Snackbar.LENGTH_INDEFINITE).show();
            }
    }

    private void onActionTakePhoto() {
        // https://developer.android.com/training/camera/photobasics
        PackageManager pm = getContext().getPackageManager();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(pm) == null) {
            Snackbar snackbar = Snackbar.make(view, getString(R.string.title_no_camera), Snackbar.LENGTH_LONG);
            snackbar.setAction(R.string.title_fix, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Helper.view(getContext(), Uri.parse(BuildConfig.CAMERA_URI), false);
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
                Snackbar.make(view, getString(R.string.title_no_viewer, intent.getAction()), Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private void onActionImage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        Helper.openAdvanced(intent);
        PackageManager pm = getContext().getPackageManager();
        if (intent.resolveActivity(pm) == null)
            noStorageAccessFramework();
        else
            startActivityForResult(Helper.getChooser(getContext(), intent), REQUEST_IMAGE);
    }

    private void onActionAttachment() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        Helper.openAdvanced(intent);
        PackageManager pm = getContext().getPackageManager();
        if (intent.resolveActivity(pm) == null)
            noStorageAccessFramework();
        else
            startActivityForResult(Helper.getChooser(getContext(), intent), REQUEST_ATTACHMENT);
    }

    private void noStorageAccessFramework() {
        Snackbar snackbar = Snackbar.make(view, R.string.title_no_saf, Snackbar.LENGTH_LONG);
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

        ClipboardManager cbm = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (cbm != null && cbm.hasPrimaryClip()) {
            String link = cbm.getPrimaryClip().getItemAt(0).coerceToText(getContext()).toString();
            uri = Uri.parse(link);
            if (uri.getScheme() == null)
                uri = null;
        }

        Bundle args = new Bundle();
        args.putParcelable("uri", uri);

        FragmentDialogLink fragment = new FragmentDialogLink();
        fragment.setArguments(args);
        fragment.setTargetFragment(this, REQUEST_LINK);
        fragment.show(getParentFragmentManager(), "compose:link");
    }

    private void onActionDiscard() {
        if (isEmpty())
            onAction(R.id.action_delete);
        else {
            Bundle args = new Bundle();
            args.putString("question", getString(R.string.title_ask_discard));

            FragmentDialogAsk fragment = new FragmentDialogAsk();
            fragment.setArguments(args);
            fragment.setTargetFragment(this, REQUEST_DISCARD);
            fragment.show(getParentFragmentManager(), "compose:discard");
        }
    }

    private void onActionCheck(boolean dialog) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean send_dialog = prefs.getBoolean("send_dialog", true);

        Bundle extras = new Bundle();
        extras.putBoolean("dialog", dialog || send_dialog);
        onAction(R.id.action_check, extras);
    }

    private void onEncrypt(final EntityMessage draft) {
        if (EntityMessage.SMIME_SIGNONLY.equals(draft.encrypt) ||
                EntityMessage.SMIME_SIGNENCRYPT.equals(draft.encrypt)) {
            Bundle args = new Bundle();
            args.putLong("id", draft.id);
            args.putInt("type", draft.encrypt);

            new SimpleTask<EntityIdentity>() {
                @Override
                protected EntityIdentity onExecute(Context context, Bundle args) {
                    long id = args.getLong("id");

                    DB db = DB.getInstance(context);
                    EntityMessage draft = db.message().getMessage(id);
                    if (draft == null || draft.identity == null)
                        return null;

                    return db.identity().getIdentity(draft.identity);
                }

                @Override
                protected void onExecuted(final Bundle args, EntityIdentity identity) {
                    Helper.selectKeyAlias(getActivity(), getViewLifecycleOwner(), identity.sign_key_alias, new Helper.IKeyAlias() {
                        @Override
                        public void onSelected(String alias) {
                            args.putString("alias", alias);
                            onSmime(args);
                        }

                        @Override
                        public void onNothingSelected() {
                            Snackbar snackbar = Snackbar.make(view, R.string.title_no_key, Snackbar.LENGTH_LONG);
                            final Intent intent = KeyChain.createInstallIntent();
                            if (intent.resolveActivity(getContext().getPackageManager()) != null)
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

                    pgpUserIds = new String[recipients.size()];
                    for (int i = 0; i < recipients.size(); i++) {
                        InternetAddress recipient = (InternetAddress) recipients.get(i);
                        pgpUserIds[i] = recipient.getAddress().toLowerCase();
                    }

                    Intent intent;
                    if (EntityMessage.PGP_SIGNONLY.equals(draft.encrypt))
                        intent = new Intent(OpenPgpApi.ACTION_GET_SIGN_KEY_ID);
                    else if (EntityMessage.PGP_SIGNENCRYPT.equals(draft.encrypt)) {
                        intent = new Intent(OpenPgpApi.ACTION_GET_KEY_IDS);
                        intent.putExtra(OpenPgpApi.EXTRA_USER_IDS, pgpUserIds);
                    } else
                        throw new IllegalArgumentException("Invalid encrypt=" + draft.encrypt);

                    intent.putExtra(BuildConfig.APPLICATION_ID, working);

                    onPgp(intent);
                } catch (Throwable ex) {
                    if (ex instanceof IllegalArgumentException)
                        Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG).show();
                    else {
                        Log.e(ex);
                        Log.unexpectedError(getParentFragmentManager(), ex);
                    }
                }
            else {
                Snackbar snackbar = Snackbar.make(view, R.string.title_no_openpgp, Snackbar.LENGTH_LONG);
                if (Helper.getIntentOpenKeychain().resolveActivity(getContext().getPackageManager()) != null)
                    snackbar.setAction(R.string.title_fix, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(Helper.getIntentOpenKeychain());
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
                case REQUEST_IMAGE:
                    if (resultCode == RESULT_OK && data != null) {
                        Uri uri = data.getData();
                        if (uri != null)
                            onAddAttachment(uri, true);
                    }
                    break;
                case REQUEST_ATTACHMENT:
                case REQUEST_RECORD_AUDIO:
                case REQUEST_TAKE_PHOTO:
                    if (resultCode == RESULT_OK)
                        if (requestCode == REQUEST_TAKE_PHOTO)
                            onAddMedia(new Intent().setData(photoURI));
                        else if (data != null)
                            onAddMedia(data);
                    break;
                case REQUEST_OPENPGP:
                    if (resultCode == RESULT_OK && data != null)
                        onPgp(data);
                    break;
                case REQUEST_CONTACT_GROUP:
                    if (resultCode == RESULT_OK && data != null)
                        onContactGroupSelected(data.getBundleExtra("args"));
                    break;
                case REQUEST_ANSWER:
                    if (resultCode == RESULT_OK && data != null)
                        onAnswerSelected(data.getBundleExtra("args"));
                    break;
                case REQUEST_COLOR:
                    if (resultCode == RESULT_OK && data != null)
                        onColorSelected(data.getBundleExtra("args"));
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
                        onActionSend(false);
                    else if (resultCode == RESULT_FIRST_USER)
                        onActionSend(true);
                    break;
                case REQUEST_CERTIFICATE:
                    if (resultCode == RESULT_OK && data != null)
                        onSmime(data.getBundleExtra("args"));
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

                            list.add(new InternetAddress(email, name));

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

    private void onAddAttachment(Uri uri, boolean image) {
        Bundle args = new Bundle();
        args.putLong("id", working);
        args.putParcelable("uri", uri);
        args.putBoolean("image", image);
        args.putCharSequence("body", etBody.getText());
        args.putInt("start", etBody.getSelectionStart());

        new SimpleTask<Spanned>() {
            @Override
            protected Spanned onExecute(Context context, Bundle args) throws IOException {
                long id = args.getLong("id");
                Uri uri = args.getParcelable("uri");
                boolean image = args.getBoolean("image");

                EntityAttachment attachment = addAttachment(context, id, uri, image);
                if (!image)
                    return null;

                File file = attachment.getFile(context);

                Drawable d = Drawable.createFromPath(file.getAbsolutePath());
                if (d == null)
                    throw new IllegalArgumentException(context.getString(R.string.title_no_image));
                d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());

                CharSequence body = args.getCharSequence("body");
                int start = args.getInt("start");
                Uri cid = Uri.parse("cid:" + BuildConfig.APPLICATION_ID + "." + attachment.id);

                SpannableStringBuilder s = new SpannableStringBuilder(body);
                s.insert(start, " ");
                ImageSpan is = new ImageSpan(context, cid, ImageSpan.ALIGN_BASELINE);
                s.setSpan(is, start, start + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                return HtmlHelper.fromHtml(HtmlHelper.toHtml(s), new Html.ImageGetter() {
                    @Override
                    public Drawable getDrawable(String source) {
                        return ImageHelper.decodeImage(context, id, source, true, zoom, etBody);
                    }
                }, null);
            }

            @Override
            protected void onExecuted(Bundle args, final Spanned body) {
                if (body == null)
                    return;
                int start = args.getInt("start");

                etBody.setText(body);
                if (start < body.length())
                    etBody.setSelection(start);

                // Save text & update remote draft
                onAction(R.id.action_save);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                // External app sending absolute file
                if (ex instanceof SecurityException)
                    handleFileShare();
                else if (ex instanceof IllegalArgumentException)
                    Snackbar.make(view, ex.toString(), Snackbar.LENGTH_LONG).show();
                else
                    Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "compose:attachment:add");
    }

    private void onAddMedia(Intent data) {
        Log.i("Add media data=" + data);
        Log.logExtras(data);

        ClipData clipData = data.getClipData();
        if (clipData == null) {
            Uri uri = data.getData();
            if (uri != null)
                onAddAttachment(uri, false);
        } else {
            for (int i = 0; i < clipData.getItemCount(); i++) {
                ClipData.Item item = clipData.getItemAt(i);
                Uri uri = item.getUri();
                if (uri != null)
                    onAddAttachment(uri, false);
            }
        }
    }

    private void onPgp(Intent data) {
        final Bundle args = new Bundle();
        args.putParcelable("data", data);

        new SimpleTask<Object>() {
            @Override
            protected Object onExecute(Context context, Bundle args) throws Throwable {
                // Get arguments
                Intent data = args.getParcelable("data");
                long id = data.getLongExtra(BuildConfig.APPLICATION_ID, -1);

                DB db = DB.getInstance(context);

                // Get data
                EntityMessage draft = db.message().getMessage(id);
                if (draft == null)
                    throw new MessageRemovedException("PGP");
                if (draft.identity == null)
                    throw new IllegalArgumentException(getString(R.string.title_from_missing));
                EntityIdentity identity = db.identity().getIdentity(draft.identity);
                if (identity == null)
                    throw new IllegalArgumentException(getString(R.string.title_from_missing));

                // Create files
                File input = new File(context.getCacheDir(), "pgp_input." + draft.id);
                File output = new File(context.getCacheDir(), "pgp_output." + draft.id);

                // Serializing messages is NOT reproducible
                if ((EntityMessage.PGP_SIGNONLY.equals(draft.encrypt) &&
                        OpenPgpApi.ACTION_GET_SIGN_KEY_ID.equals(data.getAction())) ||
                        (EntityMessage.PGP_SIGNENCRYPT.equals(draft.encrypt) &&
                                OpenPgpApi.ACTION_GET_KEY_IDS.equals(data.getAction()))) {
                    // Get/clean attachments
                    List<EntityAttachment> attachments = db.attachment().getAttachments(draft.id);
                    for (EntityAttachment attachment : new ArrayList<>(attachments))
                        if (attachment.encryption != null) {
                            db.attachment().deleteAttachment(attachment.id);
                            attachments.remove(attachment);
                        }

                    // Build message
                    Properties props = MessageHelper.getSessionProperties();
                    Session isession = Session.getInstance(props, null);
                    MimeMessage imessage = new MimeMessage(isession);
                    MessageHelper.build(context, draft, attachments, identity, imessage);

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
                                if (pgpKeyIds.length == 0)
                                    throw new OperationCanceledException("Got no key");

                                if (identity.sign_key != null) {
                                    pgpSignKeyId = identity.sign_key;

                                    // Get public key
                                    Intent intent = new Intent(OpenPgpApi.ACTION_GET_KEY);
                                    intent.putExtra(OpenPgpApi.EXTRA_KEY_ID, pgpSignKeyId);
                                    intent.putExtra(OpenPgpApi.EXTRA_REQUEST_ASCII_ARMOR, true);
                                    intent.putExtra(BuildConfig.APPLICATION_ID, draft.id);
                                    return intent;
                                } else {
                                    // Get sign key
                                    Intent intent = new Intent(OpenPgpApi.ACTION_GET_SIGN_KEY_ID);
                                    intent.putExtra(BuildConfig.APPLICATION_ID, draft.id);
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
                                intent.putExtra(BuildConfig.APPLICATION_ID, draft.id);
                                return intent;
                            } else if (OpenPgpApi.ACTION_GET_KEY.equals(data.getAction())) {
                                if (EntityMessage.PGP_SIGNONLY.equals(draft.encrypt)) {
                                    // Get signature
                                    Intent intent = new Intent(OpenPgpApi.ACTION_DETACHED_SIGN);
                                    intent.putExtra(OpenPgpApi.EXTRA_SIGN_KEY_ID, pgpSignKeyId);
                                    intent.putExtra(OpenPgpApi.EXTRA_REQUEST_ASCII_ARMOR, true);
                                    intent.putExtra(BuildConfig.APPLICATION_ID, draft.id);
                                    return intent;
                                } else if (EntityMessage.PGP_SIGNENCRYPT.equals(draft.encrypt)) {
                                    // Encrypt message
                                    Intent intent = new Intent(OpenPgpApi.ACTION_SIGN_AND_ENCRYPT);
                                    intent.putExtra(OpenPgpApi.EXTRA_KEY_IDS, pgpKeyIds);
                                    intent.putExtra(OpenPgpApi.EXTRA_SIGN_KEY_ID, pgpSignKeyId);
                                    intent.putExtra(OpenPgpApi.EXTRA_REQUEST_ASCII_ARMOR, true);
                                    intent.putExtra(BuildConfig.APPLICATION_ID, draft.id);
                                    return intent;
                                } else
                                    throw new IllegalArgumentException("Invalid encrypt=" + draft.encrypt);
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
                                return null;
                            } else if (OpenPgpApi.ACTION_SIGN_AND_ENCRYPT.equals(data.getAction())) {
                                input.delete();

                                // send message
                                return null;
                            } else
                                throw new IllegalStateException("Unknown action=" + data.getAction());

                        case OpenPgpApi.RESULT_CODE_USER_INTERACTION_REQUIRED:
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
                if (result == null)
                    onAction(R.id.action_send);
                else if (result instanceof Intent) {
                    Intent intent = (Intent) result;
                    onPgp(intent);
                } else if (result instanceof PendingIntent)
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
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (ex instanceof OperationCanceledException)
                    ; // Do nothing
                else if (ex instanceof IllegalArgumentException) {
                    Log.i(ex);
                    Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG).show();
                } else
                    Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "compose:pgp");
    }

    private void onSmime(Bundle args) {
        new SimpleTask<Void>() {
            @Override
            protected Void onExecute(Context context, Bundle args) throws Throwable {
                long id = args.getLong("id");
                int type = args.getInt("type");
                String alias = args.getString("alias");

                DB db = DB.getInstance(context);

                // Get data
                EntityMessage draft = db.message().getMessage(id);
                if (draft == null)
                    throw new MessageRemovedException("S/MIME");
                EntityIdentity identity = db.identity().getIdentity(draft.identity);
                if (identity == null)
                    throw new IllegalArgumentException(getString(R.string.title_from_missing));

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
                MessageHelper.build(context, draft, attachments, identity, imessage);
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

                // Store selected alias
                if (alias == null)
                    throw new IllegalArgumentException("Key alias missing");
                db.identity().setIdentitySignKeyAlias(identity.id, alias);

                // Get private key
                PrivateKey privkey = KeyChain.getPrivateKey(context, alias);
                if (privkey == null)
                    throw new IllegalArgumentException("Private key missing");
                X509Certificate[] chain = KeyChain.getCertificateChain(context, alias);
                if (chain == null || chain.length == 0)
                    throw new IllegalArgumentException("Certificate missing");
                try {
                    chain[0].checkValidity();
                } catch (CertificateException ex) {
                    throw new IllegalArgumentException(context.getString(R.string.title_invalid_key), ex);
                }

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

                ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256withRSA")
                        .build(privkey);
                DigestCalculatorProvider digestCalculator = new JcaDigestCalculatorProviderBuilder()
                        .build();
                SignerInfoGenerator signerInfoGenerator = new JcaSignerInfoGeneratorBuilder(digestCalculator)
                        .build(contentSigner, chain[0]);
                cmsGenerator.addSignerInfoGenerator(signerInfoGenerator);

                ByteArrayOutputStream osContent = new ByteArrayOutputStream();
                bpContent.writeTo(osContent);

                CMSTypedData cmsData = new CMSProcessableByteArray(osContent.toByteArray());
                CMSSignedData cmsSignedData = cmsGenerator.generate(cmsData);
                byte[] signedMessage = cmsSignedData.getEncoded();

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
                certs.add(chain[0]); // Allow sender to decrypt own message

                for (Address address : addresses) {
                    String email = ((InternetAddress) address).getAddress();

                    List<EntityCertificate> acertificates = db.certificate().getCertificateByEmail(email);
                    if (acertificates == null || acertificates.size() == 0)
                        throw new IllegalArgumentException(
                                context.getString(R.string.title_certificate_missing, email), new CertificateException());

                    for (EntityCertificate acertificate : acertificates) {
                        X509Certificate cert = acertificate.getCertificate();
                        try {
                            cert.checkValidity();
                        } catch (CertificateException ex) {
                            throw new IllegalArgumentException(
                                    context.getString(R.string.title_certificate_invalid, email), ex);
                        }
                        certs.add(cert);
                    }
                }

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
                for (X509Certificate cert : certs) {
                    RecipientInfoGenerator gen = new JceKeyTransRecipientInfoGenerator(cert);
                    cmsEnvelopedDataGenerator.addRecipientInfoGenerator(gen);
                }

                ByteArrayOutputStream osMessage = new ByteArrayOutputStream();
                imessage.writeTo(osMessage);
                CMSTypedData msg = new CMSProcessableByteArray(osMessage.toByteArray());

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

                db.attachment().setDownloaded(attachment.id, encrypted.length());

                return null;
            }

            @Override
            protected void onExecuted(Bundle args, Void result) {
                onAction(R.id.action_send);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                if (ex instanceof IllegalArgumentException) {
                    Log.i(ex);
                    Snackbar snackbar = Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG);
                    if (ex.getCause() instanceof CertificateException)
                        snackbar.setAction(R.string.title_fix, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(
                                        new Intent(getContext(), ActivitySetup.class)
                                                .putExtra("tab", "privacy"));
                            }
                        });
                    snackbar.show();
                } else
                    Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "compose:s/mimem");
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
                                selected.add(new InternetAddress(email, name));
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

    private void onAnswerSelected(Bundle args) {
        String answer = args.getString("answer");

        InternetAddress[] to = null;
        try {
            to = InternetAddress.parse(etTo.getText().toString());
        } catch (AddressException ignored) {
        }

        String text = EntityAnswer.replacePlaceholders(answer, to);

        Spanned spanned = HtmlHelper.fromHtml(text);
        etBody.getText().insert(etBody.getSelectionStart(), spanned);
    }

    private void onColorSelected(Bundle args) {
        int color = args.getInt("color");
        int start = args.getInt("start");
        int end = args.getInt("end");
        etBody.setSelection(start, end);
        StyleHelper.apply(R.id.menu_color, etBody, color);
    }

    private void onLinkSelected(Bundle args) {
        String link = args.getString("link");
        StyleHelper.apply(R.id.menu_link, etBody, link);
    }

    private void onActionDiscardConfirmed() {
        onAction(R.id.action_delete);
    }

    private void onActionSend(boolean now) {
        Bundle args = new Bundle();
        args.putLong("id", working);
        args.putBoolean("now", now);

        new SimpleTask<EntityMessage>() {
            @Override
            protected EntityMessage onExecute(Context context, Bundle args) {
                long id = args.getLong("id");
                boolean now = args.getBoolean("now");

                DB db = DB.getInstance(context);
                EntityMessage draft = db.message().getMessage(id);
                if (draft != null && now)
                    db.message().setMessageSnoozed(draft.id, new Date().getTime());
                return draft;
            }

            @Override
            protected void onExecuted(Bundle args, EntityMessage draft) {
                if (draft != null)
                    onActionSend(draft);
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Log.unexpectedError(getParentFragmentManager(), ex);
            }
        }.execute(this, args, "draft:get");

    }

    private void onActionSend(EntityMessage draft) {
        if (EntityMessage.SMIME_SIGNONLY.equals(draft.encrypt) ||
                EntityMessage.SMIME_SIGNENCRYPT.equals(draft.encrypt))
            if (!ActivityBilling.isPro(getContext())) {
                startActivity(new Intent(getContext(), ActivityBilling.class));
                return;
            }

        if (draft.encrypt == null || EntityMessage.ENCRYPT_NONE.equals(draft.encrypt))
            onAction(R.id.action_send);
        else
            onEncrypt(draft);
    }

    private void onExit() {
        if (state != State.LOADED)
            finish();
        else if (isEmpty() && !saved)
            onAction(R.id.action_delete);
        else {
            autosave = false;
            onAction(R.id.action_save);
            finish();
        }
    }

    private boolean isEmpty() {
        if (!TextUtils.isEmpty(JsoupEx.parse(HtmlHelper.toHtml(etBody.getText())).text().trim()))
            return false;
        if (rvAttachment.getAdapter().getItemCount() > 0)
            return false;
        return true;
    }

    private void onAction(int action) {
        onAction(action, new Bundle());
    }

    private void onAction(int action, @NonNull Bundle extras) {
        EntityIdentity identity = (EntityIdentity) spIdentity.getSelectedItem();

        // Workaround underlines left by Android
        etBody.clearComposingText();

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
        args.putString("body", HtmlHelper.toHtml(etBody.getText()));
        args.putBoolean("signature", cbSignature.isChecked());
        args.putBoolean("empty", isEmpty());
        args.putBundle("extras", extras);

        Log.i("Run execute id=" + working);
        actionLoader.execute(this, args, "compose:action:" + action);
    }

    private static EntityAttachment addAttachment(Context context, long id, Uri uri,
                                                  boolean image) throws IOException {
        Log.w("Add attachment uri=" + uri);

        if (!"content".equals(uri.getScheme()) &&
                !Helper.hasPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Log.w("Add attachment uri=" + uri);
            throw new SecurityException("Add attachment with file scheme");
        }

        EntityAttachment attachment = new EntityAttachment();

        String fname = null;
        String ftype = null;
        Long fsize = null;

        try {
            DocumentFile dfile = DocumentFile.fromSingleUri(context, uri);
            if (dfile != null) {
                fname = dfile.getName();
                ftype = dfile.getType();
                fsize = dfile.length();
            }
        } catch (SecurityException ex) {
            Log.e(ex);
        }

        // Check name
        if (TextUtils.isEmpty(fname))
            fname = uri.getLastPathSegment();

        // Check type
        if (!TextUtils.isEmpty(ftype))
            try {
                new ContentType(ftype);
            } catch (ParseException ex) {
                Log.w(ex);
                ftype = null;
            }

        if (TextUtils.isEmpty(ftype) || "*/*".equals(ftype))
            ftype = Helper.guessMimeType(fname);

        if (fsize != null && fsize <= 0)
            fsize = null;

        DB db = DB.getInstance(context);
        try {
            db.beginTransaction();

            EntityMessage draft = db.message().getMessage(id);
            Log.i("Attaching to id=" + id);

            attachment.message = draft.id;
            attachment.sequence = db.attachment().getAttachmentSequence(draft.id) + 1;
            attachment.name = fname;
            attachment.type = ftype;
            attachment.disposition = (image ? Part.INLINE : Part.ATTACHMENT);
            attachment.size = fsize;
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

            resizeAttachment(context, attachment);

        } catch (Throwable ex) {
            // Reset progress on failure
            Log.e(ex);
            db.attachment().setError(attachment.id, Log.formatThrowable(ex, false));
            throw ex;
        }

        return attachment;
    }

    private static void resizeAttachment(Context context, EntityAttachment attachment) throws IOException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean resize_images = prefs.getBoolean("resize_images", true);
        boolean resize_attachments = prefs.getBoolean("resize_attachments", true);

        File file = attachment.getFile(context);

        if (((resize_images && Part.INLINE.equals(attachment.disposition)) ||
                (resize_attachments && Part.ATTACHMENT.equals(attachment.disposition))) &&
                file.exists() /* upload cancelled */ &&
                ("image/jpeg".equals(attachment.type) || "image/png".equals(attachment.type))) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(file.getAbsolutePath(), options);

            int resize = prefs.getInt("resize", REDUCED_IMAGE_SIZE);

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
            String encrypt_method = prefs.getString("default_encrypt_method", "pgp");
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
                    throw new IllegalStateException(getString(R.string.title_no_identities));

                data.draft = db.message().getMessage(id);
                if (data.draft == null || data.draft.ui_hide) {
                    // New draft
                    if ("edit".equals(action))
                        throw new MessageRemovedException("Draft for edit was deleted hide=" + (data.draft != null));

                    EntityMessage ref = db.message().getMessage(reference);

                    data.draft = new EntityMessage();
                    data.draft.msgid = EntityMessage.generateMessageId();

                    if (plain_only)
                        data.draft.plain_only = true;
                    if (encrypt_default)
                        if ("s/mime".equals(encrypt_method))
                            data.draft.encrypt = EntityMessage.SMIME_SIGNENCRYPT;
                        else
                            data.draft.encrypt = EntityMessage.PGP_SIGNENCRYPT;
                    else if (sign_default)
                        if ("s/mime".equals(encrypt_method))
                            data.draft.encrypt = EntityMessage.SMIME_SIGNONLY;
                        else
                            data.draft.encrypt = EntityMessage.PGP_SIGNONLY;
                    data.draft.ui_encrypt = data.draft.encrypt;
                    if (receipt_default)
                        data.draft.receipt_request = true;

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
                        throw new IllegalArgumentException(context.getString(R.string.title_no_identities));

                    Document document = Document.createShell("");

                    if (ref == null) {
                        data.draft.thread = data.draft.msgid;

                        try {
                            String to = args.getString("to");
                            data.draft.to = (TextUtils.isEmpty(to) ? null : InternetAddress.parse(to));
                        } catch (AddressException ex) {
                            Log.w(ex);
                        }

                        try {
                            String cc = args.getString("cc");
                            data.draft.cc = (TextUtils.isEmpty(cc) ? null : InternetAddress.parse(cc));
                        } catch (AddressException ex) {
                            Log.w(ex);
                        }

                        try {
                            String bcc = args.getString("bcc");
                            data.draft.bcc = (TextUtils.isEmpty(bcc) ? null : InternetAddress.parse(bcc));
                        } catch (AddressException ex) {
                            Log.w(ex);
                        }

                        data.draft.subject = args.getString("subject", "");
                        String b = args.getString("body", "");
                        if (!TextUtils.isEmpty(b)) {
                            Document d = HtmlHelper.sanitize(context, b, false, false);
                            Element e = d.body();
                            e.tagName("div");
                            document.body().appendChild(e);
                        }

                        if (answer > 0) {
                            EntityAnswer a = db.answer().getAnswer(answer);
                            if (a != null) {
                                data.draft.subject = a.name;
                                Document d = JsoupEx.parse(a.getText(null));
                                Element e = d.body();
                                e.tagName("div");
                                document.body().appendChild(e);
                            }
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

                        } else if ("forward".equals(action) || "editasnew".equals(action))
                            data.draft.thread = data.draft.msgid; // new thread

                        String subject = (ref.subject == null ? "" : ref.subject);
                        if ("reply".equals(action) || "reply_all".equals(action)) {
                            if (prefix_once)
                                for (String re : Helper.getStrings(context, R.string.title_subject_reply, ""))
                                    subject = unprefix(subject, re);
                            data.draft.subject = context.getString(R.string.title_subject_reply, subject);
                        } else if ("forward".equals(action)) {
                            if (prefix_once)
                                for (String fwd : Helper.getStrings(context, R.string.title_subject_forward, ""))
                                    subject = unprefix(subject, fwd);
                            data.draft.subject = context.getString(R.string.title_subject_forward, subject);
                        } else if ("editasnew".equals(action)) {
                            data.draft.subject = ref.subject;
                            if (ref.content) {
                                String html = Helper.readText(ref.getFile(context));
                                Document d = HtmlHelper.sanitize(context, html, true, false);
                                Element e = d.body();
                                e.tagName("div");
                                document.body().appendChild(e);
                            }
                        } else if ("list".equals(action)) {
                            data.draft.subject = ref.subject;
                        } else if ("receipt".equals(action)) {
                            data.draft.subject = context.getString(R.string.title_receipt_subject, subject);

                            for (String text : Helper.getStrings(context, R.string.title_receipt_text)) {
                                Element p = document.createElement("p");
                                p.text(text);
                                document.body().appendChild(p);
                            }
                        } else if ("participation".equals(action))
                            data.draft.subject = status + ": " + ref.subject;

                        if (ref.plain_only != null && ref.plain_only)
                            data.draft.plain_only = true;
                        if (ref.ui_encrypt != null && !EntityMessage.ENCRYPT_NONE.equals(ref.ui_encrypt)) {
                            data.draft.encrypt = ref.ui_encrypt;
                            data.draft.ui_encrypt = ref.ui_encrypt;
                        }

                        if (answer > 0) {
                            EntityAnswer a = db.answer().getAnswer(answer);
                            if (a != null) {
                                Document d = JsoupEx.parse(a.getText(data.draft.to));
                                Element e = d.body();
                                e.tagName("div");
                                document.body().appendChild(e);
                            }
                        }

                        if (ref.content &&
                                !"editasnew".equals(action) &&
                                !"list".equals(action) &&
                                !"receipt".equals(action)) {
                            // Reply/forward
                            Element div = document.createElement("div");
                            div.attr("fairemail", "reference");

                            // Build reply header
                            Element p = document.createElement("p");
                            DateFormat DF = Helper.getDateTimeInstance(context);
                            boolean extended_reply = prefs.getBoolean("extended_reply", false);
                            if (extended_reply) {
                                if (ref.from != null && ref.from.length > 0) {
                                    Element strong = document.createElement("strong");
                                    strong.text(context.getString(R.string.title_from) + " ");
                                    p.appendChild(strong);
                                    p.appendText(MessageHelper.formatAddresses(ref.from));
                                    p.appendElement("br");
                                }
                                if (ref.to != null && ref.to.length > 0) {
                                    Element strong = document.createElement("strong");
                                    strong.text(context.getString(R.string.title_to) + " ");
                                    p.appendChild(strong);
                                    p.appendText(MessageHelper.formatAddresses(ref.to));
                                    p.appendElement("br");
                                }
                                if (ref.cc != null && ref.cc.length > 0) {
                                    Element strong = document.createElement("strong");
                                    strong.text(context.getString(R.string.title_cc) + " ");
                                    p.appendChild(strong);
                                    p.appendText(MessageHelper.formatAddresses(ref.cc));
                                    p.appendElement("br");
                                }
                                {
                                    Element strong = document.createElement("strong");
                                    strong.text(context.getString(R.string.title_received) + " ");
                                    p.appendChild(strong);
                                    p.appendText(DF.format(ref.received));
                                    p.appendElement("br");
                                }
                                {
                                    Element strong = document.createElement("strong");
                                    strong.text(context.getString(R.string.title_subject) + " ");
                                    p.appendChild(strong);
                                    p.appendText(ref.subject == null ? "" : ref.subject);
                                    p.appendElement("br");
                                }
                            } else
                                p.text(DF.format(new Date(ref.received)) + " " + MessageHelper.formatAddresses(ref.from) + ":");

                            div.appendChild(p);

                            // Get referenced message body
                            Document d = JsoupEx.parse(ref.getFile(context));

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

                            // Quote referenced message body
                            Element e = d.body();
                            boolean quote_reply = prefs.getBoolean("quote_reply", true);
                            boolean quote = (quote_reply && ("reply".equals(action) || "reply_all".equals(action)));

                            e.tagName(quote ? "blockquote" : "p");
                            div.appendChild(e);

                            document.body().appendChild(div);

                            addSignature(context, document, data.draft, selected);
                        }
                    }

                    EntityFolder drafts = db.folder().getFolderByType(selected.account, EntityFolder.DRAFTS);
                    if (drafts == null)
                        throw new IllegalArgumentException(context.getString(R.string.title_no_primary_drafts));

                    data.draft.account = drafts.account;
                    data.draft.folder = drafts.id;
                    data.draft.identity = selected.id;
                    data.draft.from = new InternetAddress[]{new InternetAddress(selected.email, selected.name)};

                    data.draft.sender = MessageHelper.getSortKey(data.draft.from);
                    Uri lookupUri = ContactInfo.getLookupUri(context, data.draft.from);
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
                                InternetAddress o = new InternetAddress(email, name);
                                Log.i("Setting organizer=" + o);
                                data.draft.to = new Address[]{o};
                            }
                        }
                    }

                    if ("new".equals(action)) {
                        ArrayList<Uri> uris = args.getParcelableArrayList("attachments");
                        if (uris != null)
                            for (Uri uri : uris)
                                try {
                                    addAttachment(context, data.draft.id, uri, false);
                                } catch (IOException ex) {
                                    Log.e(ex);
                                }
                    } else if (ref != null &&
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
                            if (attachment.encryption == null &&
                                    ("forward".equals(action) || "editasnew".equals(action) ||
                                            (cid.contains(attachment.cid) ||
                                                    (attachment.isInline() && attachment.isImage())))) {
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

                                    if (!"forward".equals(action))
                                        resizeAttachment(context, attachment);
                                } else
                                    args.putBoolean("incomplete", true);
                            }
                    }

                    EntityOperation.queue(context, data.draft, EntityOperation.ADD);
                } else {
                    if (data.draft.revision == null) {
                        data.draft.revision = 1;
                        data.draft.revisions = 1;
                        db.message().setMessageRevision(data.draft.id, data.draft.revision);
                        db.message().setMessageRevisions(data.draft.id, data.draft.revisions);
                    }

                    if (data.draft.content) {
                        File file = data.draft.getFile(context);

                        Document doc = JsoupEx.parse(file);
                        doc.select("div[fairemail=signature]").remove();
                        Elements ref = doc.select("div[fairemail=reference]");
                        ref.remove();

                        File refFile = data.draft.getRefFile(context);
                        if (refFile.exists()) {
                            ref.html(Helper.readText(refFile));
                            refFile.delete();
                        }

                        Document document = HtmlHelper.sanitize(context, doc.html(), true, false);

                        EntityIdentity identity = null;
                        if (data.draft.identity != null)
                            identity = db.identity().getIdentity(data.draft.identity);

                        for (Element e : ref)
                            document.body().appendChild(e);

                        addSignature(context, document, data.draft, identity);

                        String html = document.html();
                        Helper.writeText(file, html);
                        Helper.writeText(data.draft.getFile(context, data.draft.revision), html);

                        db.message().setMessageContent(data.draft.id,
                                true,
                                data.draft.plain_only,
                                HtmlHelper.getPreview(html),
                                null);
                    } else {
                        if (data.draft.uid == null)
                            throw new IllegalStateException("Draft without uid");
                        EntityOperation.queue(context, data.draft, EntityOperation.BODY);
                    }

                    List<EntityAttachment> attachments = db.attachment().getAttachments(data.draft.id);
                    for (EntityAttachment attachment : attachments)
                        if (!attachment.available)
                            EntityOperation.queue(context, data.draft, EntityOperation.ATTACHMENT, attachment.id);

                    args.putBoolean("saved", true);
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
            saved = args.getBoolean("saved");
            final String action = getArguments().getString("action");
            Log.i("Loaded draft id=" + data.draft.id + " action=" + action + " saved=" + saved);

            working = data.draft.id;
            encrypt = data.draft.encrypt;
            getActivity().invalidateOptionsMenu();

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
            if ("reply_all".equals(action))
                grpAddresses.setVisibility(View.VISIBLE);
            ibCcBcc.setVisibility(View.VISIBLE);

            bottom_navigation.getMenu().findItem(R.id.action_undo).setVisible(data.draft.revision > 1);
            bottom_navigation.getMenu().findItem(R.id.action_redo).setVisible(data.draft.revision < data.draft.revisions);

            if (args.getBoolean("incomplete"))
                Snackbar.make(view, R.string.title_attachments_incomplete, Snackbar.LENGTH_LONG).show();

            DB db = DB.getInstance(getContext());

            db.attachment().liveAttachments(data.draft.id).observe(getViewLifecycleOwner(),
                    new Observer<List<EntityAttachment>>() {
                        private int last_available = 0;

                        @Override
                        public void onChanged(@Nullable List<EntityAttachment> attachments) {
                            if (attachments == null)
                                attachments = new ArrayList<>();

                            adapter.set(attachments);
                            grpAttachments.setVisibility(attachments.size() > 0 ? View.VISIBLE : View.GONE);

                            int available = 0;
                            boolean downloading = false;
                            boolean inline_images = false;
                            for (EntityAttachment attachment : attachments) {
                                if (attachment.available)
                                    available++;
                                if (attachment.progress != null)
                                    downloading = true;
                                if (attachment.isInline() && attachment.isImage())
                                    inline_images = true;
                            }

                            Log.i("Attachments=" + attachments.size() +
                                    " available=" + available + " downloading=" + downloading);

                            // Attachment deleted: update remote draft
                            if (available < last_available)
                                onAction(R.id.action_save);

                            last_available = available;

                            rvAttachment.setTag(downloading);
                            checkInternet();

                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                            boolean inline_image_hint = prefs.getBoolean("inline_image_hint", true);
                            grpUnusedImagesHint.setVisibility(inline_images && inline_image_hint ? View.VISIBLE : View.GONE);
                        }
                    });

            db.message().liveMessage(data.draft.id).observe(getViewLifecycleOwner(), new Observer<EntityMessage>() {
                @Override
                public void onChanged(EntityMessage draft) {
                    // Draft was deleted
                    if (draft == null || draft.ui_hide)
                        finish();
                    else {
                        encrypt = draft.encrypt;
                        getActivity().invalidateOptionsMenu();

                        Log.i("Draft content=" + draft.content);
                        if (draft.content && state == State.NONE)
                            showDraft(draft);

                        tvNoInternet.setTag(draft.content);
                        checkInternet();
                    }
                }
            });
        }

        @Override
        protected void onException(Bundle args, Throwable ex) {
            pbWait.setVisibility(View.GONE);

            // External app sending absolute file
            if (ex instanceof MessageRemovedException)
                finish();
            else if (ex instanceof SecurityException)
                handleFileShare();
            else if (ex instanceof IllegalArgumentException)
                Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG).show();
            else if (ex instanceof IllegalStateException) {
                Snackbar snackbar = Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_INDEFINITE);
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
        Snackbar sb = Snackbar.make(view, R.string.title_no_stream, Snackbar.LENGTH_INDEFINITE);
        sb.setAction(R.string.title_info, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(getContext(), 49);
            }
        });
        sb.show();
    }

    private SimpleTask<EntityMessage> actionLoader = new SimpleTask<EntityMessage>() {
        int last_available = 0;

        @Override
        protected void onPreExecute(Bundle args) {
            setBusy(true);
        }

        @Override
        protected void onPostExecute(Bundle args) {
            int action = args.getInt("action");
            if (action != R.id.action_check)
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
                    else
                        EntityOperation.queue(context, draft, EntityOperation.MOVE, trash.id);

                    Handler handler = new Handler(context.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            ToastEx.makeText(context, R.string.title_draft_deleted, Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
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
                            EntityOperation.queue(context, draft, EntityOperation.ADD);
                    }

                    Map<String, String> crumb = new HashMap<>();
                    crumb.put("draft", draft.folder + ":" + draft.id);
                    crumb.put("content", Boolean.toString(draft.content));
                    crumb.put("file", Boolean.toString(draft.getFile(context).exists()));
                    crumb.put("action", getActionName(action));
                    Log.breadcrumb("compose", crumb);

                    List<EntityAttachment> attachments = db.attachment().getAttachments(draft.id);

                    // Get data
                    InternetAddress afrom[] = (identity == null ? null : new InternetAddress[]{new InternetAddress(identity.email, identity.name)});

                    InternetAddress ato[] = null;
                    InternetAddress acc[] = null;
                    InternetAddress abcc[] = null;

                    boolean lookup_mx = prefs.getBoolean("lookup_mx", false);

                    if (!TextUtils.isEmpty(to))
                        try {
                            ato = InternetAddress.parse(to);
                            if (action == R.id.action_send) {
                                for (InternetAddress address : ato)
                                    address.validate();
                                if (lookup_mx)
                                    ConnectionHelper.lookupMx(ato, context);
                            }
                        } catch (AddressException ex) {
                            throw new AddressException(context.getString(R.string.title_address_parse_error,
                                    Helper.ellipsize(to, ADDRESS_ELLIPSIZE), ex.getMessage()));
                        }

                    if (!TextUtils.isEmpty(cc))
                        try {
                            acc = InternetAddress.parse(cc);
                            if (action == R.id.action_send) {
                                for (InternetAddress address : acc)
                                    address.validate();
                                if (lookup_mx)
                                    ConnectionHelper.lookupMx(acc, context);
                            }
                        } catch (AddressException ex) {
                            throw new AddressException(context.getString(R.string.title_address_parse_error,
                                    Helper.ellipsize(cc, ADDRESS_ELLIPSIZE), ex.getMessage()));
                        }

                    if (!TextUtils.isEmpty(bcc))
                        try {
                            abcc = InternetAddress.parse(bcc);
                            if (action == R.id.action_send) {
                                for (InternetAddress address : abcc)
                                    address.validate();
                                if (lookup_mx)
                                    ConnectionHelper.lookupMx(abcc, context);
                            }
                        } catch (AddressException ex) {
                            throw new AddressException(context.getString(R.string.title_address_parse_error,
                                    Helper.ellipsize(bcc, ADDRESS_ELLIPSIZE), ex.getMessage()));
                        }

                    if (TextUtils.isEmpty(extra))
                        extra = null;

                    int available = 0;
                    for (EntityAttachment attachment : attachments)
                        if (attachment.available)
                            available++;

                    Long ident = (identity == null ? null : identity.id);
                    boolean dirty = (!Objects.equals(draft.identity, ident) ||
                            !Objects.equals(draft.extra, extra) ||
                            !MessageHelper.equal(draft.from, afrom) ||
                            !MessageHelper.equal(draft.to, ato) ||
                            !MessageHelper.equal(draft.cc, acc) ||
                            !MessageHelper.equal(draft.bcc, abcc) ||
                            !Objects.equals(draft.subject, subject) ||
                            !draft.signature.equals(signature) ||
                            last_available != available);

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
                        Uri lookupUri = ContactInfo.getLookupUri(context, draft.from);
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
                        b = HtmlHelper.sanitize(context, body, true, false);

                    if (TextUtils.isEmpty(body) ||
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
                            d = JsoupEx.parse(body);

                            for (Element e : ref)
                                d.body().appendChild(e);

                            addSignature(context, d, draft, identity);
                        }

                        body = d.html();

                        // Create new revision
                        if (action != R.id.action_undo && action != R.id.action_redo) {
                            draft.revisions++;
                            draft.revision = draft.revisions;
                        }

                        Helper.writeText(draft.getFile(context, draft.revision), body);
                    } else
                        body = Helper.readText(draft.getFile(context));

                    if (action == R.id.action_undo || action == R.id.action_redo) {
                        if (action == R.id.action_undo) {
                            if (draft.revision > 1)
                                draft.revision--;
                        } else {
                            if (draft.revision < draft.revisions)
                                draft.revision++;
                        }

                        // Restore revision
                        Log.i("Restoring revision=" + draft.revision);
                        body = Helper.readText(draft.getFile(context, draft.revision));
                    }

                    Helper.writeText(draft.getFile(context), body);

                    db.message().setMessageContent(draft.id,
                            true,
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
                    if (action == R.id.action_save ||
                            action == R.id.action_undo ||
                            action == R.id.action_redo ||
                            action == R.id.action_check) {
                        if (dirty)
                            EntityOperation.queue(context, draft, EntityOperation.ADD);

                        if (action == R.id.action_check) {
                            // Check data
                            if (draft.identity == null)
                                throw new IllegalArgumentException(context.getString(R.string.title_from_missing));

                            if (draft.to == null && draft.cc == null && draft.bcc == null)
                                args.putBoolean("remind_to", true);

                            if (TextUtils.isEmpty(draft.subject))
                                args.putBoolean("remind_subject", true);

                            Document d = JsoupEx.parse(body);

                            if (empty && d.select("div[fairemail=reference]").isEmpty())
                                args.putBoolean("remind_text", true);

                            int attached = 0;
                            for (EntityAttachment attachment : attachments)
                                if (!attachment.available)
                                    throw new IllegalArgumentException(context.getString(R.string.title_attachments_missing));
                                else if (!attachment.isInline() && attachment.encryption == null)
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
                        } else {
                            args.putBoolean("saved", true);

                            Handler handler = new Handler(context.getMainLooper());
                            handler.post(new Runnable() {
                                public void run() {
                                    ToastEx.makeText(context, R.string.title_draft_saved, Toast.LENGTH_LONG).show();
                                }
                            });
                        }

                    } else if (action == R.id.action_send) {
                        // Remove unused inline images
                        List<String> cids = new ArrayList<>();
                        for (Element element : JsoupEx.parse(body).select("img")) {
                            String src = element.attr("src");
                            if (src.startsWith("cid:"))
                                cids.add("<" + src.substring(4) + ">");
                        }

                        for (EntityAttachment attachment : new ArrayList<>(attachments))
                            if (attachment.isInline() && !cids.contains(attachment.cid)) {
                                Log.i("Removing unused inline attachment cid=" + attachment.cid);
                                db.attachment().deleteAttachment(attachment.id);
                            }

                        // Delete draft (cannot move to outbox)
                        EntityOperation.queue(context, draft, EntityOperation.DELETE);

                        // Copy message to outbox
                        draft.id = null;
                        draft.folder = db.folder().getOutbox().id;
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
                            draft.ui_snoozed = new Date().getTime() + send_delayed * 1000L;
                            db.message().setMessageSnoozed(draft.id, draft.ui_snoozed);
                        }

                        if (draft.ui_snoozed != null && draft.ui_snoozed <= new Date().getTime()) {
                            draft.ui_snoozed = null;
                            db.message().setMessageSnoozed(draft.id, null);
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

                        Handler handler = new Handler(context.getMainLooper());
                        handler.post(new Runnable() {
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
            boolean wasSaved = args.getBoolean("saved");
            int action = args.getInt("action");
            Log.i("Loaded action id=" + (draft == null ? null : draft.id) +
                    " action=" + getActionName(action) + " saved=" + wasSaved);

            if (wasSaved)
                saved = true;

            etTo.setText(MessageHelper.formatAddressesCompose(draft.to));
            etCc.setText(MessageHelper.formatAddressesCompose(draft.cc));
            etBcc.setText(MessageHelper.formatAddressesCompose(draft.bcc));

            bottom_navigation.getMenu().findItem(R.id.action_undo).setVisible(draft.revision > 1);
            bottom_navigation.getMenu().findItem(R.id.action_redo).setVisible(draft.revision < draft.revisions);

            if (action == R.id.action_delete) {
                autosave = false;
                finish();

            } else if (action == R.id.action_undo || action == R.id.action_redo) {
                showDraft(draft);

            } else if (action == R.id.action_save) {
                boolean show = args.getBundle("extras").getBoolean("show");
                if (show)
                    showDraft(draft);

            } else if (action == R.id.action_check) {
                boolean dialog = args.getBundle("extras").getBoolean("dialog");
                boolean remind_to = args.getBoolean("remind_to", false);
                boolean remind_subject = args.getBoolean("remind_subject", false);
                boolean remind_text = args.getBoolean("remind_text", false);
                boolean remind_attachment = args.getBoolean("remind_attachment", false);
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                boolean send_reminders = prefs.getBoolean("send_reminders", true);

                int recipients = (draft.to == null ? 0 : draft.to.length) +
                        (draft.cc == null ? 0 : draft.cc.length) +
                        (draft.bcc == null ? 0 : draft.bcc.length);
                if (dialog || (send_reminders &&
                        (remind_to || remind_subject || remind_text || remind_attachment ||
                                recipients > RECIPIENTS_WARNING))) {
                    setBusy(false);

                    FragmentDialogSend fragment = new FragmentDialogSend();
                    fragment.setArguments(args);
                    fragment.setTargetFragment(FragmentCompose.this, REQUEST_SEND);
                    fragment.show(getParentFragmentManager(), "compose:send");
                } else
                    onActionSend(draft);

            } else if (action == R.id.action_send) {
                autosave = false;
                finish();
            }
        }

        @Override
        protected void onException(Bundle args, Throwable ex) {
            int action = args.getInt("action");
            if (action == R.id.action_check)
                setBusy(false);

            if (ex instanceof MessageRemovedException)
                finish();
            else if (ex instanceof AddressException) {
                final Snackbar sb = Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_INDEFINITE);
                sb.setAction(android.R.string.ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sb.dismiss();
                    }
                });
                sb.show();
            } else if (ex instanceof IllegalArgumentException || ex instanceof UnknownHostException)
                Snackbar.make(view, ex.getMessage(), Snackbar.LENGTH_LONG).show();
            else
                Log.unexpectedError(getParentFragmentManager(), ex);
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
            FragmentCompose.this.busy = busy;
            Helper.setViewsEnabled(view, !busy);
            getActivity().invalidateOptionsMenu();
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

    private void showDraft(final EntityMessage draft) {
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
                state = State.LOADED;
                autosave = true;

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

                DB db = DB.getInstance(context);
                EntityMessage draft = db.message().getMessage(id);
                if (draft == null || !draft.content)
                    throw new IllegalArgumentException(context.getString(R.string.title_no_body));

                Document doc = JsoupEx.parse(draft.getFile(context));
                doc.select("div[fairemail=signature]").remove();
                Elements ref = doc.select("div[fairemail=reference]");
                ref.remove();

                Spanned spannedBody = HtmlHelper.fromHtml(doc.html(), new Html.ImageGetter() {
                    @Override
                    public Drawable getDrawable(String source) {
                        return ImageHelper.decodeImage(context, id, source, true, zoom, etBody);
                    }
                }, null);

                SpannableStringBuilder bodyBuilder = new SpannableStringBuilder(spannedBody);
                QuoteSpan[] bodySpans = bodyBuilder.getSpans(0, bodyBuilder.length(), QuoteSpan.class);
                for (QuoteSpan quoteSpan : bodySpans) {
                    bodyBuilder.setSpan(
                            new StyledQuoteSpan(context, colorPrimary),
                            bodyBuilder.getSpanStart(quoteSpan),
                            bodyBuilder.getSpanEnd(quoteSpan),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    bodyBuilder.removeSpan(quoteSpan);
                }

                spannedBody = bodyBuilder;

                Spanned spannedRef = null;
                if (!ref.isEmpty()) {
                    Document quote = HtmlHelper.sanitize(context, ref.outerHtml(), show_images, false);
                    Spanned spannedQuote = HtmlHelper.fromHtml(quote.html(),
                            new Html.ImageGetter() {
                                @Override
                                public Drawable getDrawable(String source) {
                                    return ImageHelper.decodeImage(context, id, source, show_images, zoom, tvReference);
                                }
                            },
                            null);

                    SpannableStringBuilder refBuilder = new SpannableStringBuilder(spannedQuote);
                    QuoteSpan[] refSpans = refBuilder.getSpans(0, refBuilder.length(), QuoteSpan.class);
                    for (QuoteSpan quoteSpan : refSpans) {
                        refBuilder.setSpan(
                                new StyledQuoteSpan(context, colorPrimary),
                                refBuilder.getSpanStart(quoteSpan),
                                refBuilder.getSpanEnd(quoteSpan),
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        refBuilder.removeSpan(quoteSpan);
                    }

                    spannedRef = refBuilder;
                }

                args.putBoolean("ref_has_images", spannedRef != null &&
                        spannedRef.getSpans(0, spannedRef.length(), ImageSpan.class).length > 0);

                return new Spanned[]{spannedBody, spannedRef};
            }

            @Override
            protected void onExecuted(Bundle args, Spanned[] text) {
                etBody.setText(text[0]);
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

                final Context context = getContext();

                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        View target;
                        if (TextUtils.isEmpty(etTo.getText().toString().trim()))
                            target = etTo;
                        else if (TextUtils.isEmpty(etSubject.getText().toString()))
                            target = etSubject;
                        else
                            target = etBody;

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
                signature = HtmlHelper.fromHtml(identity.signature, new Html.ImageGetter() {
                    @Override
                    public Drawable getDrawable(String source) {
                        return ActivitySignature.getDrawableByUri(getContext(), Uri.parse(source));
                    }
                }, null);
            tvSignature.setText(signature);
            grpSignature.setVisibility(signature == null ? View.GONE : View.VISIBLE);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            etExtra.setHint("");
            tvDomain.setText(null);

            tvSignature.setText(null);
            grpSignature.setVisibility(View.GONE);
        }
    };

    private ActivityBase.IKeyPressedListener onKeyPressedListener = new ActivityBase.IKeyPressedListener() {
        @Override
        public boolean onKeyPressed(KeyEvent event) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.isCtrlPressed()) {
                onActionSend(false);
                return true;
            }
            return false;
        }

        @Override
        public boolean onBackPressed() {
            if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                onExit();
            return true;
        }
    };

    public static class FragmentDialogContactGroup extends FragmentDialogBase {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            final long working = getArguments().getLong("working");
            int focussed = getArguments().getInt("focussed");

            View dview = LayoutInflater.from(getContext()).inflate(R.layout.dialog_contact_group, null);
            final Spinner spGroup = dview.findViewById(R.id.spGroup);
            final Spinner spTarget = dview.findViewById(R.id.spTarget);

            Cursor groups = getContext().getContentResolver().query(
                    ContactsContract.Groups.CONTENT_SUMMARY_URI,
                    new String[]{
                            ContactsContract.Groups._ID,
                            ContactsContract.Groups.TITLE,
                            ContactsContract.Groups.SUMMARY_COUNT
                    },
                    ContactsContract.Groups.DELETED + " = 0" +
                            " AND " + ContactsContract.Groups.SUMMARY_COUNT + " > 0",
                    null,
                    ContactsContract.Groups.TITLE
            );

            SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                    getContext(),
                    R.layout.spinner_item1_dropdown,
                    groups,
                    new String[]{ContactsContract.Groups.TITLE},
                    new int[]{android.R.id.text1},
                    0);
            spGroup.setAdapter(adapter);

            spTarget.setSelection(focussed);

            return new AlertDialog.Builder(getContext())
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

    public static class FragmentDialogAnswer extends FragmentDialogBase {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            final ArrayAdapter<EntityAnswer> adapter =
                    new ArrayAdapter<>(getContext(), R.layout.spinner_item1, android.R.id.text1);

            // TODO: spinner
            new SimpleTask<List<EntityAnswer>>() {
                @Override
                protected List<EntityAnswer> onExecute(Context context, Bundle args) {
                    DB db = DB.getInstance(getContext());
                    return db.answer().getAnswers(false);
                }

                @Override
                protected void onExecuted(Bundle args, List<EntityAnswer> answers) {
                    adapter.addAll(answers);
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(getParentFragmentManager(), ex);
                }
            }.execute(this, new Bundle(), "compose:answer");

            return new AlertDialog.Builder(getContext())
                    .setTitle(R.string.title_insert_template)
                    .setAdapter(adapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            EntityAnswer answer = adapter.getItem(which);
                            getArguments().putString("answer", answer.text);

                            sendResult(RESULT_OK);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
        }
    }

    public static class FragmentDialogSend extends FragmentDialogBase {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            long id = getArguments().getLong("id");

            Bundle args = getArguments();
            boolean dialog = args.getBundle("extras").getBoolean("dialog");
            boolean remind_to = args.getBoolean("remind_to", false);
            boolean remind_subject = args.getBoolean("remind_subject", false);
            boolean remind_text = args.getBoolean("remind_text", false);
            boolean remind_attachment = args.getBoolean("remind_attachment", false);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            int send_delayed = prefs.getInt("send_delayed", 0);
            boolean send_dialog = prefs.getBoolean("send_dialog", true);

            final int[] encryptValues = getResources().getIntArray(R.array.encryptValues);
            final int[] sendDelayedValues = getResources().getIntArray(R.array.sendDelayedValues);
            final String[] sendDelayedNames = getResources().getStringArray(R.array.sendDelayedNames);

            final ViewGroup dview = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.dialog_send, null);
            final TextView tvRemindTo = dview.findViewById(R.id.tvRemindTo);
            final TextView tvRemindSubject = dview.findViewById(R.id.tvRemindSubject);
            final TextView tvRemindText = dview.findViewById(R.id.tvRemindText);
            final TextView tvRemindAttachment = dview.findViewById(R.id.tvRemindAttachment);
            final TextView tvTo = dview.findViewById(R.id.tvTo);
            final TextView tvVia = dview.findViewById(R.id.tvVia);
            final CheckBox cbPlainOnly = dview.findViewById(R.id.cbPlainOnly);
            final CheckBox cbReceipt = dview.findViewById(R.id.cbReceipt);
            final TextView tvReceipt = dview.findViewById(R.id.tvReceipt);
            final Spinner spEncrypt = dview.findViewById(R.id.spEncrypt);
            final Spinner spPriority = dview.findViewById(R.id.spPriority);
            final TextView tvSendAt = dview.findViewById(R.id.tvSendAt);
            final ImageButton ibSendAt = dview.findViewById(R.id.ibSendAt);
            final CheckBox cbNotAgain = dview.findViewById(R.id.cbNotAgain);
            final TextView tvNotAgain = dview.findViewById(R.id.tvNotAgain);

            tvRemindTo.setVisibility(remind_to ? View.VISIBLE : View.GONE);
            tvRemindSubject.setVisibility(remind_subject ? View.VISIBLE : View.GONE);
            tvRemindText.setVisibility(remind_text ? View.VISIBLE : View.GONE);
            tvRemindAttachment.setVisibility(remind_attachment ? View.VISIBLE : View.GONE);
            tvTo.setText(null);
            tvVia.setText(null);
            tvReceipt.setVisibility(View.GONE);
            spEncrypt.setTag(0);
            spEncrypt.setSelection(0);
            spPriority.setTag(1);
            spPriority.setSelection(1);
            tvSendAt.setText(null);
            cbNotAgain.setChecked(!send_dialog);
            cbNotAgain.setVisibility(dialog ? View.VISIBLE : View.GONE);
            tvNotAgain.setVisibility(cbNotAgain.isChecked() && send_dialog ? View.VISIBLE : View.GONE);

            Helper.setViewsEnabled(dview, false);

            cbNotAgain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    prefs.edit().putBoolean("send_dialog", !isChecked).apply();
                    tvNotAgain.setVisibility(isChecked && send_dialog ? View.VISIBLE : View.GONE);
                }
            });

            cbPlainOnly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
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
                            db.message().setMessageUiEncrypt(id, encrypt);

                            return null;
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Log.unexpectedError(getParentFragmentManager(), ex);
                        }
                    }.execute(FragmentDialogSend.this, args, "compose:encrypt");
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

                    int encrypt = (draft.encrypt == null ? EntityMessage.ENCRYPT_NONE : draft.encrypt);
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

            if (!remind_to) {
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

    private class DraftData {
        private EntityMessage draft;
        private List<TupleIdentityEx> identities;
    }
}
