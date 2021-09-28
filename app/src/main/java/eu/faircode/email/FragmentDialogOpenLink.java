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

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.Group;
import androidx.core.net.MailTo;
import androidx.core.util.PatternsCompat;
import androidx.lifecycle.Lifecycle;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import java.net.IDN;
import java.net.InetAddress;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FragmentDialogOpenLink extends FragmentDialogBase {
    private static final String URI_RESET_OPEN = "https://support.google.com/pixelphone/answer/6271667";

    private ImageButton ibMore;
    private TextView tvMore;
    private Button btnOwner;
    private ContentLoadingProgressBar pbWait;
    private TextView tvOwnerRemark;
    private TextView tvHost;
    private TextView tvOwner;
    private Group grpOwner;
    private Button btnSettings;
    private Button btnDefault;
    private TextView tvReset;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Uri _uri = getArguments().getParcelable("uri");
        String _title = getArguments().getString("title");
        if (_title != null)
            _title = _title.replace("\uFFFC", ""); // Object replacement character
        if (TextUtils.isEmpty(_title))
            _title = null;
        final String title = _title;

        final Context context = getContext();
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean check_links_dbl = prefs.getBoolean("check_links_dbl", BuildConfig.PLAY_STORE_RELEASE);
        boolean disconnect_links = prefs.getBoolean("disconnect_links", true);

        // Preload web view
        Helper.customTabsWarmup(context);

        final Uri uri = UriHelper.guessScheme(_uri);

        // Process link
        final Uri sanitized;
        if (uri.isOpaque())
            sanitized = uri;
        else {
            Uri s = UriHelper.sanitize(uri);
            sanitized = (s == null ? uri : s);
        }

        // Process title
        final Uri uriTitle;
        if (title != null && PatternsCompat.WEB_URL.matcher(title).matches()) {
            String t = title.replaceAll("\\s+", "");
            Uri u = Uri.parse(title.contains("://") ? t : "http://" + t);
            String host = u.getHost(); // Capture1.PNG
            uriTitle = (UriHelper.hasParentDomain(context, host) ? u : null);
        } else
            uriTitle = null;

        // Get views
        final View dview = LayoutInflater.from(context).inflate(R.layout.dialog_open_link, null);
        final ImageButton ibInfo = dview.findViewById(R.id.ibInfo);
        final TextView tvTitle = dview.findViewById(R.id.tvTitle);
        final ImageButton ibDifferent = dview.findViewById(R.id.ibDifferent);
        final EditText etLink = dview.findViewById(R.id.etLink);
        final TextView tvLink = dview.findViewById(R.id.tvLink);
        final ImageButton ibSearch = dview.findViewById(R.id.ibSearch);
        final ImageButton ibShare = dview.findViewById(R.id.ibShare);
        final ImageButton ibCopy = dview.findViewById(R.id.ibCopy);
        final TextView tvSuspicious = dview.findViewById(R.id.tvSuspicious);
        final TextView tvDisconnect = dview.findViewById(R.id.tvDisconnect);
        final TextView tvDisconnectCategories = dview.findViewById(R.id.tvDisconnectCategories);
        final CheckBox cbSecure = dview.findViewById(R.id.cbSecure);
        final CheckBox cbSanitize = dview.findViewById(R.id.cbSanitize);
        final CheckBox cbNotAgain = dview.findViewById(R.id.cbNotAgain);

        ibMore = dview.findViewById(R.id.ibMore);
        tvMore = dview.findViewById(R.id.tvMore);
        btnOwner = dview.findViewById(R.id.btnOwner);
        pbWait = dview.findViewById(R.id.pbWait);
        tvOwnerRemark = dview.findViewById(R.id.tvOwnerRemark);
        tvHost = dview.findViewById(R.id.tvHost);
        tvOwner = dview.findViewById(R.id.tvOwner);
        grpOwner = dview.findViewById(R.id.grpOwner);
        btnSettings = dview.findViewById(R.id.btnSettings);
        btnDefault = dview.findViewById(R.id.btnDefault);
        tvReset = dview.findViewById(R.id.tvReset);

        final Group grpDifferent = dview.findViewById(R.id.grpDifferent);

        // Wire

        ibInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 35);
            }
        });

        ibDifferent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etLink.setText(format(uriTitle, context));
            }
        });

        etLink.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence text, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                Uri uri = Uri.parse(editable.toString());

                boolean secure = UriHelper.isSecure(uri);
                boolean hyperlink = UriHelper.isHyperLink(uri);

                cbSecure.setTag(secure);
                cbSecure.setChecked(secure);

                cbSecure.setText(
                        secure ? R.string.title_link_https : R.string.title_link_http);
                cbSecure.setTextColor(Helper.resolveColor(context,
                        secure ? android.R.attr.textColorSecondary : R.attr.colorWarning));
                cbSecure.setTypeface(
                        secure ? Typeface.DEFAULT : Typeface.DEFAULT_BOLD);

                cbSecure.setVisibility(hyperlink ? View.VISIBLE : View.GONE);
            }
        });

        etLink.setHorizontallyScrolling(false);
        etLink.setMaxLines(10);
        etLink.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_POSITIVE).performClick();
                    return true;
                } else
                    return false;
            }
        });

        MailTo mailto = null;
        if ("mailto".equals(uri.getScheme()))
            try {
                mailto = MailTo.parse(uri);
            } catch (Throwable ex) {
                Log.w(ex);
            }
        ibSearch.setVisibility(
                mailto != null && !TextUtils.isEmpty(mailto.getTo())
                        ? View.VISIBLE : View.GONE);
        ibSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();

                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                lbm.sendBroadcast(
                        new Intent(ActivityView.ACTION_SEARCH_ADDRESS)
                                .putExtra("account", -1L)
                                .putExtra("folder", -1L)
                                .putExtra("query", MailTo.parse(uri).getTo()));
            }
        });

        ibShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent send = new Intent();
                send.setAction(Intent.ACTION_SEND);
                send.putExtra(Intent.EXTRA_TEXT, etLink.getText().toString());
                send.setType("text/plain");
                startActivity(Intent.createChooser(send, title));
            }
        });

        ibCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard =
                        (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard == null)
                    return;

                ClipData clip = ClipData.newPlainText(title, etLink.getText().toString());
                clipboard.setPrimaryClip(clip);

                ToastEx.makeText(context, R.string.title_clipboard_copied, Toast.LENGTH_LONG).show();
            }
        });

        cbSecure.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                boolean tag = (Boolean) compoundButton.getTag();
                if (tag == checked)
                    return;

                Uri uri = Uri.parse(etLink.getText().toString());
                etLink.setText(format(UriHelper.secure(uri, checked), context));
            }
        });

        cbSanitize.setVisibility(uri.equals(sanitized) ? View.GONE : View.VISIBLE);

        cbSanitize.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                Uri link = (checked ? sanitized : uri);
                boolean secure = UriHelper.isSecure(link);
                cbSecure.setTag(secure);
                cbSecure.setChecked(secure);
                etLink.setText(format(UriHelper.secure(link, secure), context));
            }
        });

        cbNotAgain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean(uri.getHost() + ".confirm_link", !isChecked).apply();
            }
        });

        View.OnClickListener onMore = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMore(btnOwner.getVisibility() == View.GONE);
            }
        };

        ibMore.setOnClickListener(onMore);
        tvMore.setOnClickListener(onMore);

        btnOwner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle args = new Bundle();
                args.putParcelable("uri", uri);

                new SimpleTask<Pair<InetAddress, IPInfo.Organization>>() {
                    @Override
                    protected void onPreExecute(Bundle args) {
                        ibMore.setEnabled(false);
                        tvMore.setEnabled(false);
                        btnOwner.setEnabled(false);
                        pbWait.setVisibility(View.VISIBLE);
                        grpOwner.setVisibility(View.GONE);
                    }

                    @Override
                    protected void onPostExecute(Bundle args) {
                        ibMore.setEnabled(true);
                        tvMore.setEnabled(true);
                        btnOwner.setEnabled(true);
                        pbWait.setVisibility(View.GONE);
                        grpOwner.setVisibility(View.VISIBLE);
                    }

                    @Override
                    protected Pair<InetAddress, IPInfo.Organization> onExecute(Context context, Bundle args) throws Throwable {
                        Uri uri = args.getParcelable("uri");
                        return IPInfo.getOrganization(uri, context);
                    }

                    @Override
                    protected void onExecuted(Bundle args, Pair<InetAddress, IPInfo.Organization> data) {
                        tvHost.setText(data.first.toString());
                        tvOwner.setText(data.second.name == null ? "?" : data.second.name);
                        ApplicationEx.getMainHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
                                    return;
                                dview.scrollTo(0, tvOwner.getBottom());
                            }
                        });
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        tvHost.setText(ex.getClass().getName());
                        tvOwner.setText(ex.getMessage());
                    }
                }.execute(FragmentDialogOpenLink.this, args, "link:owner");
            }
        });

        tvOwnerRemark.setMovementMethod(LinkMovementMethod.getInstance());

        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.getContext().startActivity(new Intent(v.getContext(), ActivitySetup.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .putExtra("tab", "privacy"));
            }
        });

        final Intent manage = new Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS);
        btnDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.getContext().startActivity(manage);
            }
        });

        tvReset.setPaintFlags(tvReset.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Helper.view(view.getContext(), Uri.parse(URI_RESET_OPEN), true);
            }
        });

        // Initialize

        tvTitle.setText(title);
        tvTitle.setVisibility(TextUtils.isEmpty(title) ? View.GONE : View.VISIBLE);

        String host = uri.getHost();
        String thost = (uriTitle == null ? null : uriTitle.getHost());

        String puny = null;
        try {
            if (host != null)
                puny = IDN.toASCII(host, IDN.ALLOW_UNASSIGNED);
        } catch (Throwable ex) {
            Log.i(ex);
            puny = host;
        }

        if (host != null && !host.equals(puny)) {
            etLink.setText(format(uri.buildUpon().encodedAuthority(puny).build(), context));
            tvLink.setText(uri.toString());
            tvSuspicious.setVisibility(View.VISIBLE);
        } else {
            etLink.setText(format(uri, context));
            tvLink.setText(null);
            tvSuspicious.setVisibility(Helper.isSingleScript(host) ? View.GONE : View.VISIBLE);
        }

        if (check_links_dbl &&
                tvSuspicious.getVisibility() != View.VISIBLE) {
            Bundle args = new Bundle();
            args.putParcelable("uri", uri);

            new SimpleTask<Boolean>() {
                @Override
                protected Boolean onExecute(Context context, Bundle args) throws Throwable {
                    Uri uri = args.getParcelable("uri");
                    return DnsBlockList.isJunk(context, uri);
                }

                @Override
                protected void onExecuted(Bundle args, Boolean blocklist) {
                    if (blocklist != null && blocklist)
                        tvSuspicious.setVisibility(View.VISIBLE);
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    // Ignored
                }
            }.execute(this, args, "link:blocklist");
        }

        grpDifferent.setVisibility(
                host == null || thost == null || host.equalsIgnoreCase(thost)
                        ? View.GONE : View.VISIBLE);

        List<String> categories = null;
        if (disconnect_links)
            categories = DisconnectBlacklist.getCategories(uri.getHost());
        if (categories != null)
            tvDisconnectCategories.setText(TextUtils.join(", ", categories));
        tvDisconnect.setVisibility(
                categories == null ? View.GONE : View.VISIBLE);
        tvDisconnectCategories.setVisibility(
                categories == null || !BuildConfig.DEBUG ? View.GONE : View.VISIBLE);

        cbNotAgain.setText(context.getString(R.string.title_no_ask_for_again, uri.getHost()));
        cbNotAgain.setVisibility(
                "https".equals(uri.getScheme()) && !TextUtils.isEmpty(uri.getHost())
                        ? View.VISIBLE : View.GONE);

        setMore(false);

        return new AlertDialog.Builder(context)
                .setView(dview)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Uri uri = Uri.parse(etLink.getText().toString());
                        Helper.view(context, uri, false);
                    }
                })
                .setNeutralButton(R.string.title_browse, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // https://developer.android.com/training/basics/intents/sending#AppChooser
                        Uri uri = Uri.parse(etLink.getText().toString());
                        Intent view = new Intent(Intent.ACTION_VIEW, uri);
                        Intent chooser = Intent.createChooser(view, context.getString(R.string.title_select_app));
                        try {
                            startActivity(chooser);
                        } catch (ActivityNotFoundException ex) {
                            Log.w(ex);
                            Helper.view(context, uri, true, true);
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    private void setMore(boolean show) {
        boolean n = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N);
        ibMore.setImageLevel(show ? 0 : 1);
        btnOwner.setVisibility(show ? View.VISIBLE : View.GONE);
        pbWait.setVisibility(View.GONE);
        tvOwnerRemark.setVisibility(show ? View.VISIBLE : View.GONE);
        grpOwner.setVisibility(View.GONE);
        btnSettings.setVisibility(show ? View.VISIBLE : View.GONE);
        btnDefault.setVisibility(show && n ? View.VISIBLE : View.GONE);
        tvReset.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private Spanned format(Uri uri, Context context) {
        String scheme = uri.getScheme();
        String host = uri.getHost();
        String text = uri.toString();
        SpannableStringBuilder ssb = new SpannableStringBuilderEx(text);

        try {
            int textColorLink = Helper.resolveColor(context, android.R.attr.textColorLink);
            int textColorSecondary = Helper.resolveColor(context, android.R.attr.textColorSecondary);
            int colorWarning = Helper.resolveColor(context, R.attr.colorWarning);

            if ("tel".equals(scheme)) {
                // tel://+123%2045%20678%123456
                host = Uri.decode(host);
                text = "tel://" + host;
            } else if ("mailto".equals(scheme)) {
                if (host == null) {
                    MailTo email = MailTo.parse(uri);
                    host = UriHelper.getEmailDomain(email.getTo());
                }
            }

            if (scheme != null) {
                int index = text.indexOf(scheme);
                if (index >= 0)
                    if ("http".equals(scheme)) {
                        ssb.setSpan(new ForegroundColorSpan(colorWarning),
                                index, index + scheme.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        ssb.setSpan(new StyleSpan(Typeface.BOLD),
                                index, index + scheme.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } else
                        ssb.setSpan(new ForegroundColorSpan(textColorSecondary),
                                index, index + scheme.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            if (host != null) {
                int index = text.indexOf(host);
                if (index >= 0)
                    ssb.setSpan(new ForegroundColorSpan(textColorLink),
                            index, index + host.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            if (uri.isHierarchical())
                for (String name : uri.getQueryParameterNames()) {
                    Pattern pattern = Pattern.compile("[?&]" + Pattern.quote(name) + "=");
                    Matcher matcher = pattern.matcher(text);
                    while (matcher.find()) {
                        ssb.setSpan(new ForegroundColorSpan(textColorLink),
                                matcher.start() + 1, matcher.end() - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
        } catch (Throwable ex) {
            Log.e(ex);
        }

        return ssb;
    }
}