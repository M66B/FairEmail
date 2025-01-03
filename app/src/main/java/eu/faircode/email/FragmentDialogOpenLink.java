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

import static androidx.browser.customtabs.CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.ContextCompat;
import androidx.core.net.MailTo;
import androidx.core.text.method.LinkMovementMethodCompat;
import androidx.core.util.PatternsCompat;
import androidx.lifecycle.Lifecycle;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import java.io.IOException;
import java.net.IDN;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FragmentDialogOpenLink extends FragmentDialogBase {
    private ScrollView scroll;
    private ImageButton ibMore;
    private TextView tvMore;
    private Button btnOwner;
    private ContentLoadingProgressBar pbWait;
    private TextView tvOwnerRemark;
    private TextView tvHost;
    private TextView tvOwner;
    private Button btnWhois;
    private ContentLoadingProgressBar pbWhois;
    private Group grpOwner;
    private Button btnSettings;
    private Button btnDefault;
    private TextView tvReset;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Bundle a = getArguments();
        Uri _uri = a.getParcelable("uri");
        String _title = a.getString("title");
        if (_title != null)
            _title = _title.replace("\uFFFC", ""); // Object replacement character
        if (TextUtils.isEmpty(_title))
            _title = null;
        final String title = _title;
        final boolean always_confirm = a.getBoolean("always_confirm", false);

        final Context context = getContext();
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean sanitize_links = prefs.getBoolean("sanitize_links", false);
        boolean check_links_dbl = prefs.getBoolean("check_links_dbl", BuildConfig.PLAY_STORE_RELEASE);
        boolean disconnect_links = prefs.getBoolean("disconnect_links", true);

        // Preload web view
        //Helper.customTabsWarmup(context);

        final Uri uri = UriHelper.guessScheme(_uri);

        // Process link
        final Uri sanitized;
        if (uri.isOpaque())
            sanitized = uri;
        else {
            Uri s = UriHelper.sanitize(context, uri);
            sanitized = (s == null ? uri : s);
        }

        // Process title
        final Uri uriTitle;
        String t = (title == null ? null : title.replaceAll("\\s+", ""));
        if (t != null && PatternsCompat.WEB_URL.matcher(t).matches()) {
            Uri u = Uri.parse(t.contains("://") ? t : "http://" + t);
            String host = u.getHost(); // Capture1.PNG
            uriTitle = (UriHelper.hasTld(context, host) ? u : null);
        } else
            uriTitle = null;

        MailTo mailto = null;
        if ("mailto".equals(uri.getScheme()))
            try {
                mailto = MailTo.parse(uri);
            } catch (Throwable ex) {
                Log.w(ex);
            }

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

        // Get views
        final View dview = LayoutInflater.from(context).inflate(R.layout.dialog_open_link, null);
        scroll = dview.findViewById(R.id.scroll);
        final TextView tvCaption = dview.findViewById(R.id.tvCaption);
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
        final CheckBox cbNeverAgain = dview.findViewById(R.id.cbNeverAgain);
        final Spinner spOpenWith = dview.findViewById(R.id.spOpenWith);

        ibMore = dview.findViewById(R.id.ibMore);
        tvMore = dview.findViewById(R.id.tvMore);
        btnOwner = dview.findViewById(R.id.btnOwner);
        pbWait = dview.findViewById(R.id.pbWait);
        tvOwnerRemark = dview.findViewById(R.id.tvOwnerRemark);
        tvHost = dview.findViewById(R.id.tvHost);
        tvOwner = dview.findViewById(R.id.tvOwner);
        grpOwner = dview.findViewById(R.id.grpOwner);
        btnWhois = dview.findViewById(R.id.btnWhois);
        pbWhois = dview.findViewById(R.id.pbWhois);
        btnSettings = dview.findViewById(R.id.btnSettings);
        btnDefault = dview.findViewById(R.id.btnDefault);
        tvReset = dview.findViewById(R.id.tvReset);

        final Group grpOpenWith = dview.findViewById(R.id.grpOpenWith);
        final Group grpDifferent = dview.findViewById(R.id.grpDifferent);

        // Wire

        ibInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 182);
            }
        });

        ibDifferent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Package pkg = (Package) spOpenWith.getSelectedItem();
                Log.i("Open title uri=" + uriTitle + " with=" + pkg);
                boolean tabs = (pkg != null && pkg.tabs);
                Helper.view(context, uriTitle, !tabs, !tabs);
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
                if (cbSecure == null)
                    return;

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

        ibSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();

                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                lbm.sendBroadcast(
                        new Intent(ActivityView.ACTION_SEARCH_ADDRESS)
                                .putExtra("account", -1L)
                                .putExtra("folder", -1L)
                                .putExtra("query", MailTo.parse(uri).getTo())
                                .putExtra("sender_only", false));
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
                final Context context = v.getContext();
                ClipboardManager clipboard = Helper.getSystemService(context, ClipboardManager.class);
                if (clipboard == null)
                    return;

                String link = etLink.getText().toString();
                if (link.startsWith("mailto:"))
                    link = link.substring("mailto:".length());

                ClipData clip = ClipData.newPlainText(title, link);
                clipboard.setPrimaryClip(clip);

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
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
                cbSanitize.setTextColor(Helper.resolveColor(context,
                        checked ? android.R.attr.textColorSecondary : R.attr.colorWarning));
                cbSanitize.setTypeface(
                        checked ? Typeface.DEFAULT : Typeface.DEFAULT_BOLD);

                Uri link = (checked ? sanitized : uri);
                boolean secure = cbSecure.isChecked();
                cbSecure.setTag(secure);
                cbSecure.setChecked(secure);
                etLink.setText(format(UriHelper.secure(link, secure), context));
            }
        });

        cbNotAgain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean(getConfirmHost(uri) + ".confirm_link", !isChecked).apply();
            }
        });

        cbNeverAgain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                cbNotAgain.setEnabled(!isChecked);
            }
        });

        spOpenWith.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Package pkg = (Package) parent.getAdapter().getItem(position);
                prefs.edit()
                        .putString("open_with_pkg", pkg.name)
                        .putBoolean("open_with_tabs", pkg.tabs)
                        .apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                prefs.edit()
                        .remove("open_with_pkg")
                        .remove("open_with_tabs")
                        .apply();
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
                args.putParcelable("uri", Uri.parse(etLink.getText().toString()));

                new SimpleTask<Pair<InetAddress, IPInfo>>() {
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
                    protected Pair<InetAddress, IPInfo> onExecute(Context context, Bundle args) throws Throwable {
                        Uri uri = args.getParcelable("uri");
                        return IPInfo.getOrganization(uri, context);
                    }

                    @Override
                    protected void onExecuted(Bundle args, Pair<InetAddress, IPInfo> data) {
                        StringBuilder sb = new StringBuilder();
                        IPInfo ipinfo = data.second;
                        for (String value : new String[]{ipinfo.org, ipinfo.city, ipinfo.region, ipinfo.country})
                            if (!TextUtils.isEmpty(value)) {
                                if (sb.length() != 0)
                                    sb.append("; ");
                                sb.append(value.replaceAll("\\r?\\n", " "));
                            }

                        tvHost.setText(data.first.toString());
                        tvOwner.setText(sb.length() == 0 ? "?" : sb.toString());

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
                        tvOwner.setText(new ThrowableWrapper(ex).getSafeMessage());
                    }
                }.execute(FragmentDialogOpenLink.this, args, "link:owner");
            }
        });

        tvOwnerRemark.setMovementMethod(LinkMovementMethodCompat.getInstance());

        btnWhois.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putParcelable("uri", Uri.parse(etLink.getText().toString()));

                new SimpleTask<String>() {
                    @Override
                    protected void onPreExecute(Bundle args) {
                        btnWhois.setEnabled(false);
                        pbWhois.setVisibility(View.VISIBLE);
                    }

                    @Override
                    protected void onPostExecute(Bundle args) {
                        btnWhois.setEnabled(true);
                        pbWhois.setVisibility(View.GONE);
                    }

                    @Override
                    protected String onExecute(Context context, Bundle args) throws Throwable {
                        Uri uri = args.getParcelable("uri");
                        String host = UriHelper.getRootDomain(context, UriHelper.getHost(uri));
                        if (TextUtils.isEmpty(host))
                            throw new UnknownHostException("No root domain " + uri);
                        args.putString("host", host);
                        return Whois.get(host);
                    }

                    @Override
                    protected void onExecuted(Bundle args, String whois) {
                        final View dview = LayoutInflater.from(context).inflate(R.layout.dialog_whois, null);
                        final TextView tvHost = dview.findViewById(R.id.tvHost);
                        final TextView tvWhois = dview.findViewById(R.id.tvWhois);
                        final ImageButton ibInfo = dview.findViewById(R.id.ibInfo);

                        tvWhois.setMovementMethod(LinkMovementMethodCompat.getInstance());

                        ibInfo.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Uri uri = Uri.parse(Whois.WHOIS_INFO);
                                Helper.view(v.getContext(), uri, true);
                            }
                        });

                        tvHost.setText(args.getString("host"));
                        tvWhois.setText(whois);

                        new AlertDialog.Builder(getContext())
                                .setView(dview)
                                .setPositiveButton(android.R.string.ok, null)
                                .show();
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Log.unexpectedError(getParentFragmentManager(), ex, !(ex instanceof IOException));
                    }
                }.execute(FragmentDialogOpenLink.this, args, "link:whois");
            }
        });

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
            public void onClick(View c) {
                Helper.view(c.getContext(), Uri.parse(Helper.URI_SUPPORT_RESET_OPEN), true);
            }
        });

        // Initialize

        int icon = 0;
        if (UriHelper.isHyperLink(uri))
            icon = R.drawable.twotone_insert_link_45_24;
        else if (UriHelper.isMail(uri))
            icon = R.drawable.twotone_mail_24;
        else if (UriHelper.isPhoneNumber(uri))
            icon = R.drawable.twotone_call_24;
        else if (UriHelper.isGeo(uri))
            icon = R.drawable.twotone_language_24;
        tvCaption.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);

        tvTitle.setText(title);
        tvTitle.setVisibility(TextUtils.isEmpty(title) ? View.GONE : View.VISIBLE);

        ibSearch.setVisibility(context instanceof ActivityView &&
                mailto != null && !TextUtils.isEmpty(mailto.getTo())
                ? View.VISIBLE : View.GONE);

        if (host != null && !host.equals(puny)) {
            etLink.setText(format(uri.buildUpon().encodedAuthority(puny).build(), context));
            tvLink.setText(uri.toString());
            tvSuspicious.setVisibility(View.GONE);
        } else {
            etLink.setText(format(uri, context));
            tvLink.setText(null);
            tvSuspicious.setVisibility(TextHelper.isSingleScript(host) ? View.GONE : View.VISIBLE);
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

        cbSanitize.setChecked(sanitize_links);

        String chost = getConfirmHost(uri);
        cbNotAgain.setText(context.getString(R.string.title_no_ask_for_again, chost));
        cbNotAgain.setVisibility(!always_confirm && !sanitize_links && chost != null ? View.VISIBLE : View.GONE);

        cbNeverAgain.setVisibility(!always_confirm && !sanitize_links ? View.VISIBLE : View.GONE);

        setMore(false);

        if (UriHelper.isHyperLink(uri)) {
            Bundle args = new Bundle();
            args.putParcelable("uri", uri);

            new SimpleTask<List<Package>>() {
                @Override
                protected List<Package> onExecute(Context context, Bundle args) throws Throwable {
                    Uri uri = args.getParcelable("uri");

                    List<Package> pkgs = new ArrayList<>();
                    int dp24 = Helper.dp2pixels(context, 24);
                    if (UriHelper.isHyperLink(uri)) {
                        try {
                            PackageManager pm = context.getPackageManager();
                            Intent intent = new Intent(Intent.ACTION_VIEW)
                                    .addCategory(Intent.CATEGORY_BROWSABLE)
                                    .setData(UriHelper.fix(uri));

                            ResolveInfo main = pm.resolveActivity(intent, 0);
                            if (main != null) {
                                Log.i("Open with main=" + main.activityInfo.packageName);
                                args.putString("main", main.activityInfo.packageName);
                            }

                            int flags = (Build.VERSION.SDK_INT < Build.VERSION_CODES.M ? 0 : PackageManager.MATCH_ALL);
                            List<ResolveInfo> ris = pm.queryIntentActivities(intent, flags);

                            intent.setData(Uri.parse("http://example.com"));
                            List<ResolveInfo> browsers = pm.queryIntentActivities(intent, flags);

                            for (ResolveInfo browser : browsers) {
                                boolean found = false;
                                for (ResolveInfo ri : ris)
                                    if (Objects.equals(ri.activityInfo.packageName, browser.activityInfo.packageName)) {
                                        found = true;
                                        break;
                                    }
                                if (!found)
                                    ris.add(browser);
                            }

                            for (ResolveInfo ri : ris) {
                                Resources res = pm.getResourcesForApplication(ri.activityInfo.applicationInfo);

                                Drawable icon;
                                try {
                                    icon = res.getDrawable(ri.activityInfo.applicationInfo.icon);
                                    // Maximum size = 192x192
                                    if (icon != null &&
                                            (icon.getIntrinsicWidth() > 256 || icon.getIntrinsicHeight() > 256))
                                        icon = null;
                                } catch (Throwable ex) {
                                    Log.w(ex);
                                    icon = null;
                                }
                                if (icon != null)
                                    icon.setBounds(0, 0, dp24, dp24);

                                CharSequence label;
                                try {
                                    if (ri.activityInfo.applicationInfo.labelRes == 0)
                                        label = null;
                                    else
                                        label = res.getString(ri.activityInfo.applicationInfo.labelRes);
                                    if (label == null)
                                        Log.w("Missing label" +
                                                " pkg=" + ri.activityInfo.packageName +
                                                " res=" + ri.activityInfo.applicationInfo.labelRes);
                                } catch (Throwable ex) {
                                    Log.w(ex);
                                    label = null;
                                }

                                boolean isBrowser = false;
                                for (ResolveInfo browser : browsers)
                                    if (Objects.equals(ri.activityInfo.packageName, browser.activityInfo.packageName)) {
                                        isBrowser = true;
                                        break;
                                    }

                                pkgs.add(new Package(
                                        icon,
                                        label,
                                        ri.activityInfo.packageName,
                                        false,
                                        isBrowser,
                                        ri.activityInfo.applicationInfo.enabled));

                                try {
                                    Intent serviceIntent = new Intent();
                                    serviceIntent.setAction(ACTION_CUSTOM_TABS_CONNECTION);
                                    serviceIntent.setPackage(ri.activityInfo.packageName);
                                    boolean tabs = (pm.resolveService(serviceIntent, 0) != null);
                                    Log.i("Open with pkg=" + ri.activityInfo.packageName + " tabs=" + tabs);
                                    if (tabs)
                                        pkgs.add(new Package(
                                                icon,
                                                label,
                                                ri.activityInfo.packageName,
                                                true,
                                                isBrowser,
                                                ri.activityInfo.applicationInfo.enabled));
                                } catch (Throwable ex) {
                                    Log.e(ex);
                                }
                            }
                        } catch (Throwable ex) {
                            Log.e(ex);
                        }
                    }

                    Drawable android = ContextCompat.getDrawable(context, R.drawable.android_robot);
                    android.setBounds(0, 0, dp24, dp24);
                    pkgs.add(new Package(
                            android,
                            context.getString(R.string.title_select_app),
                            "chooser",
                            false,
                            false,
                            true));
                    pkgs.add(new Package(
                            android,
                            context.getString(R.string.title_select_app),
                            "chooser",
                            true,
                            false,
                            true));

                    return pkgs;
                }

                @Override
                protected void onExecuted(Bundle args, List<Package> pkgs) {
                    AdapterPackage adapter = new AdapterPackage(getContext(), pkgs);
                    spOpenWith.setAdapter(adapter);

                    String main = args.getString("main", null);
                    String open_with_pkg = prefs.getString("open_with_pkg", null);
                    boolean open_with_tabs = prefs.getBoolean("open_with_tabs", true);
                    Log.i("Open with selected=" + open_with_pkg + " tabs=" + open_with_tabs);
                    for (int position = 0; position < pkgs.size(); position++) {
                        Package pkg = pkgs.get(position);
                        if (Objects.equals(pkg.name, open_with_pkg) && pkg.tabs == open_with_tabs) {
                            spOpenWith.setSelection(position);
                            break;
                        }
                        if (Objects.equals(main, pkg.name))
                            spOpenWith.setSelection(position);
                    }
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Log.unexpectedError(getParentFragmentManager(), ex);
                }
            }.execute(this, args, "open:package");
        } else
            grpOpenWith.setVisibility(View.GONE);

        dview.post(new Runnable() {
            @Override
            public void run() {
                Helper.hideKeyboard(etLink);
            }
        });

        Log.i("Open link dialog uri=" + uri);
        return new AlertDialog.Builder(context)
                .setView(dview)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (cbNeverAgain.getVisibility() == View.VISIBLE && cbNeverAgain.isChecked())
                            prefs.edit().putBoolean("confirm_links", false).apply();
                        else if (chost != null &&
                                cbNotAgain.getVisibility() == View.VISIBLE && cbNotAgain.isChecked()) {
                            prefs.edit()
                                    .putBoolean(chost + ".link_view", false)
                                    .putBoolean(chost + ".link_sanitize",
                                            cbSanitize.getVisibility() == View.VISIBLE && cbSanitize.isChecked())
                                    .apply();
                        }

                        Uri theUri = Uri.parse(etLink.getText().toString());
                        Package pkg = (Package) spOpenWith.getSelectedItem();
                        Log.i("Open link uri=" + theUri + " with=" + pkg);
                        boolean tabs = (pkg != null && pkg.tabs);
                        Helper.view(context, theUri, !tabs, !tabs);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i("Open link cancelled");
                    }
                })
                .setNeutralButton(R.string.title_browse, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (chost != null &&
                                cbNotAgain.getVisibility() == View.VISIBLE && cbNotAgain.isChecked())
                            prefs.edit()
                                    .putBoolean(chost + ".link_view", true)
                                    .putBoolean(chost + ".link_sanitize",
                                            cbSanitize.getVisibility() == View.VISIBLE && cbSanitize.isChecked())
                                    .apply();

                        // https://developer.android.com/training/basics/intents/sending#AppChooser
                        Uri theUri = Uri.parse(etLink.getText().toString());
                        Log.i("Open link with uri=" + theUri);
                        Intent view = new Intent(Intent.ACTION_VIEW, UriHelper.fix(theUri));
                        Intent chooser = Intent.createChooser(view, context.getString(R.string.title_select_app));
                        try {
                            startActivity(chooser);
                        } catch (ActivityNotFoundException ex) {
                            Log.w(ex);
                            Helper.view(context, theUri, true, true);
                        }
                    }
                })
                .create();
    }

    private void setMore(boolean show) {
        boolean n = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N);
        ibMore.setImageLevel(show ? 0 : 1);
        btnOwner.setVisibility(show ? View.VISIBLE : View.GONE);
        pbWait.setVisibility(View.GONE);
        tvOwnerRemark.setVisibility(show ? View.VISIBLE : View.GONE);
        grpOwner.setVisibility(View.GONE);
        btnWhois.setVisibility(show && !BuildConfig.PLAY_STORE_RELEASE ? View.VISIBLE : View.GONE);
        pbWhois.setVisibility(View.GONE);
        btnSettings.setVisibility(show ? View.VISIBLE : View.GONE);
        btnDefault.setVisibility(show && n ? View.VISIBLE : View.GONE);
        tvReset.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show)
            scroll.post(new RunnableEx("link:scroll#1") {
                public void delegate() {
                    scroll.getChildAt(0).post(new RunnableEx("link:scroll#2") {
                        public void delegate() {
                            scroll.scrollTo(0, scroll.getBottom());
                        }
                    });
                }
            });
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
                    if ("http".equalsIgnoreCase(scheme)) {
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

    private static class Package {
        private Drawable icon;
        private CharSequence title;
        private String name;
        private boolean tabs;
        private boolean browser;
        private boolean enabled;

        public Package(Drawable icon, CharSequence title, String name, boolean tabs, boolean browser, boolean enabled) {
            this.icon = icon;
            this.title = title;
            this.name = name;
            this.tabs = tabs;
            this.browser = browser;
            this.enabled = enabled;
        }

        @Override
        public String toString() {
            return name + ":" + tabs;
        }
    }

    public static String getConfirmHost(Uri uri) {
        String scheme = uri.getScheme();
        if ("https".equals(scheme)) {
            String host = uri.getHost();
            return (TextUtils.isEmpty(host) ? null : host.toLowerCase(Locale.ROOT));
        } else if ("mailto".equals(scheme)) {
            MailTo mailto = MailTo.parse(uri);
            String to = mailto.getTo();
            return (TextUtils.isEmpty(to) ? null : to.toLowerCase(Locale.ROOT));
        } else
            return null;
    }

    public static class AdapterPackage extends ArrayAdapter<Package> {
        private final Context context;
        private final List<Package> pkgs;
        private final Drawable external;
        private final Drawable browser;
        private final int textColorPrimary;
        private final int textColorSecondary;

        AdapterPackage(@NonNull Context context, List<Package> pkgs) {
            super(context, 0, pkgs);
            this.context = context;
            this.pkgs = pkgs;
            this.external = ContextCompat.getDrawable(context, R.drawable.twotone_open_in_new_24);
            if (external != null)
                external.setBounds(0, 0, external.getIntrinsicWidth(), external.getIntrinsicHeight());
            this.browser = ContextCompat.getDrawable(context, R.drawable.twotone_language_24);
            if (browser != null)
                browser.setBounds(0, 0, browser.getIntrinsicWidth(), browser.getIntrinsicHeight());
            this.textColorPrimary = Helper.resolveColor(context, android.R.attr.textColorPrimary);
            this.textColorSecondary = Helper.resolveColor(context, android.R.attr.textColorSecondary);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            return getLayout(position, convertView, parent, R.layout.spinner_package);
        }

        @Override
        public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
            return getLayout(position, convertView, parent, R.layout.spinner_package);
        }

        private View getLayout(int position, View convertView, ViewGroup parent, int resid) {
            View view = LayoutInflater.from(context).inflate(resid, parent, false);
            TextView text1 = view.findViewById(android.R.id.text1);

            Package pkg = pkgs.get(position);
            if (pkg != null) {
                view.setAlpha(pkg.enabled ? 1f : Helper.LOW_LIGHT);
                text1.setText(pkg.title == null ? pkg.name : pkg.title.toString());
                text1.setTextColor(pkg.browser ? textColorPrimary : textColorSecondary);
                text1.setCompoundDrawablesRelative(
                        pkg.icon == null ? browser : pkg.icon,
                        null,
                        pkg.tabs ? null : external,
                        null);
            }

            return view;
        }
    }
}