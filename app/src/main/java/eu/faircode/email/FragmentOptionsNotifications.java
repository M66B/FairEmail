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

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.StatusBarManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.graphics.drawable.Icon;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.service.quicksettings.TileService;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.Group;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class FragmentOptionsNotifications extends FragmentBase implements SharedPreferences.OnSharedPreferenceChangeListener {
    private View view;
    private ImageButton ibHelp;
    private Button btnManage;
    private ImageButton ibClear;
    private Button btnManageDefault;
    private ImageView ivChannelDefault;
    private Button btnManageService;
    private ImageView ivChannelService;
    private ImageButton ibWhy;
    private TextView tvNotifySeparate;
    private SwitchCompat swNewestFirst;
    private SwitchCompat swNotifySummary;

    private CheckBox cbNotifyActionTrash;
    private CheckBox cbNotifyActionJunk;
    private CheckBox cbNotifyActionBlockSender;
    private CheckBox cbNotifyActionArchive;
    private CheckBox cbNotifyActionMove;
    private CheckBox cbNotifyActionReply;
    private CheckBox cbNotifyActionReplyDirect;
    private CheckBox cbNotifyActionFlag;
    private CheckBox cbNotifyActionSeen;
    private CheckBox cbNotifyActionHide;
    private CheckBox cbNotifyActionSnooze;
    private TextView tvNotifyActionsPro;
    private SwitchCompat swLight;
    private Button btnSound;
    private SwitchCompat swNotifyScreenOn;

    private SwitchCompat swBadge;
    private ImageButton ibBadge;
    private SwitchCompat swUnseenIgnored;
    private SwitchCompat swNotifyGrouping;
    private SwitchCompat swNotifyPrivate;
    private SwitchCompat swNotifyBackgroundOnly;
    private SwitchCompat swNotifyKnownOnly;
    private SwitchCompat swNotifySuppressInCall;
    private SwitchCompat swNotifySuppressInCar;
    private TextView tvNotifyKnownPro;
    private SwitchCompat swNotifyRemove;
    private SwitchCompat swNotifyClear;
    private SwitchCompat swNotifySubtext;
    private SwitchCompat swNotifyPreview;
    private SwitchCompat swNotifyPreviewAll;
    private SwitchCompat swNotifyPreviewOnly;
    private SwitchCompat swNotifyTransliterate;
    private SwitchCompat swNotifyAscii;
    private ImageButton ibLight;
    private SwitchCompat swWearablePreview;
    private ImageButton ibWearable;
    private SwitchCompat swMessagingStyle;
    private ImageButton ibCar;
    private SwitchCompat swBiometricsNotify;
    private SwitchCompat swNotifyOpenFolder;
    private SwitchCompat swBackground;
    private SwitchCompat swAlertOnce;
    private ImageButton ibTileSync;
    private ImageButton ibTileUnseen;
    private TextView tvNoGrouping;
    private TextView tvNoChannels;

    private Group grpChannel;
    private Group grpProperties;
    private Group grpScreenOn;
    private Group grpBackground;
    private Group grpTiles;

    final static List<String> RESET_OPTIONS = Collections.unmodifiableList(Arrays.asList(
            "notify_newest_first", "notify_summary",
            "notify_trash", "notify_junk", "notify_block_sender", "notify_archive", "notify_move",
            "notify_reply", "notify_reply_direct",
            "notify_flag", "notify_seen", "notify_hide", "notify_snooze",
            "light", "sound", "notify_screen_on",
            "badge", "unseen_ignored",
            "notify_grouping", "notify_private", "notify_background_only", "notify_known", "notify_suppress_in_call", "notify_suppress_in_car",
            "notify_remove", "notify_clear",
            "notify_subtext", "notify_preview", "notify_preview_all", "notify_preview_only", "notify_transliterate", "notify_ascii",
            "wearable_preview",
            "notify_messaging",
            "biometrics_notify", "notify_open_folder", "background_service", "alert_once"
    ));

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_setup);
        setHasOptionsMenu(true);

        view = inflater.inflate(R.layout.fragment_options_notifications, container, false);

        // Get controls

        ibHelp = view.findViewById(R.id.ibHelp);
        btnManage = view.findViewById(R.id.btnManage);
        ibClear = view.findViewById(R.id.ibClear);
        btnManageDefault = view.findViewById(R.id.btnManageDefault);
        ivChannelDefault = view.findViewById(R.id.ivChannelDefault);
        btnManageService = view.findViewById(R.id.btnManageService);
        ivChannelService = view.findViewById(R.id.ivChannelService);
        ibWhy = view.findViewById(R.id.ibWhy);
        tvNotifySeparate = view.findViewById(R.id.tvNotifySeparate);
        swNewestFirst = view.findViewById(R.id.swNewestFirst);
        swNotifySummary = view.findViewById(R.id.swNotifySummary);

        cbNotifyActionTrash = view.findViewById(R.id.cbNotifyActionTrash);
        cbNotifyActionJunk = view.findViewById(R.id.cbNotifyActionJunk);
        cbNotifyActionBlockSender = view.findViewById(R.id.cbNotifyActionBlockSender);
        cbNotifyActionArchive = view.findViewById(R.id.cbNotifyActionArchive);
        cbNotifyActionMove = view.findViewById(R.id.cbNotifyActionMove);
        cbNotifyActionReply = view.findViewById(R.id.cbNotifyActionReply);
        cbNotifyActionReplyDirect = view.findViewById(R.id.cbNotifyActionReplyDirect);
        cbNotifyActionFlag = view.findViewById(R.id.cbNotifyActionFlag);
        cbNotifyActionSeen = view.findViewById(R.id.cbNotifyActionSeen);
        cbNotifyActionHide = view.findViewById(R.id.cbNotifyActionHide);
        cbNotifyActionSnooze = view.findViewById(R.id.cbNotifyActionSnooze);
        tvNotifyActionsPro = view.findViewById(R.id.tvNotifyActionsPro);
        swLight = view.findViewById(R.id.swLight);
        btnSound = view.findViewById(R.id.btnSound);
        swNotifyScreenOn = view.findViewById(R.id.swNotifyScreenOn);

        swBadge = view.findViewById(R.id.swBadge);
        ibBadge = view.findViewById(R.id.ibBadge);
        swUnseenIgnored = view.findViewById(R.id.swUnseenIgnored);
        swNotifyGrouping = view.findViewById(R.id.swNotifyGrouping);
        swNotifyPrivate = view.findViewById(R.id.swNotifyPrivate);
        swNotifyBackgroundOnly = view.findViewById(R.id.swNotifyBackgroundOnly);
        swNotifyKnownOnly = view.findViewById(R.id.swNotifyKnownOnly);
        swNotifySuppressInCall = view.findViewById(R.id.swNotifySuppressInCall);
        swNotifySuppressInCar = view.findViewById(R.id.swNotifySuppressInCar);
        tvNotifyKnownPro = view.findViewById(R.id.tvNotifyKnownPro);
        swNotifyRemove = view.findViewById(R.id.swNotifyRemove);
        swNotifyClear = view.findViewById(R.id.swNotifyClear);
        swNotifySubtext = view.findViewById(R.id.swNotifySubtext);
        swNotifyPreview = view.findViewById(R.id.swNotifyPreview);
        swNotifyPreviewAll = view.findViewById(R.id.swNotifyPreviewAll);
        swNotifyPreviewOnly = view.findViewById(R.id.swNotifyPreviewOnly);
        swNotifyTransliterate = view.findViewById(R.id.swNotifyTransliterate);
        swNotifyAscii = view.findViewById(R.id.swNotifyAscii);
        ibLight = view.findViewById(R.id.ibLight);
        swWearablePreview = view.findViewById(R.id.swWearablePreview);
        ibWearable = view.findViewById(R.id.ibWearable);
        swMessagingStyle = view.findViewById(R.id.swMessagingStyle);
        ibCar = view.findViewById(R.id.ibCar);
        swBiometricsNotify = view.findViewById(R.id.swBiometricsNotify);
        swNotifyOpenFolder = view.findViewById(R.id.swNotifyOpenFolder);
        swBackground = view.findViewById(R.id.swBackground);
        swAlertOnce = view.findViewById(R.id.swAlertOnce);
        ibTileSync = view.findViewById(R.id.ibTileSync);
        ibTileUnseen = view.findViewById(R.id.ibTileUnseen);
        tvNoGrouping = view.findViewById(R.id.tvNoGrouping);
        tvNoChannels = view.findViewById(R.id.tvNoChannels);

        grpChannel = view.findViewById(R.id.grpChannel);
        grpProperties = view.findViewById(R.id.grpProperties);
        grpScreenOn = view.findViewById(R.id.grpScreenOn);
        grpBackground = view.findViewById(R.id.grpBackground);
        grpTiles = view.findViewById(R.id.grpTiles);

        setOptions();

        // Wire controls

        PackageManager pm = getContext().getPackageManager();
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean debug = prefs.getBoolean("debug", false);

        ibHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.view(v.getContext(), Helper.getSupportUri(v.getContext(), "Options:notifications"), false);
            }
        });

        final Intent manage = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra("app_package", getContext().getPackageName())
                .putExtra("app_uid", getContext().getApplicationInfo().uid)
                .putExtra(Settings.EXTRA_APP_PACKAGE, getContext().getPackageName());

        btnManage.setEnabled(manage.resolveActivity(pm) != null); // system whitelisted
        btnManage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(manage);
            }
        });

        ibClear.setVisibility(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? View.VISIBLE : View.GONE);
        ibClear.setOnClickListener(new View.OnClickListener() {
            @Override
            @RequiresApi(api = Build.VERSION_CODES.O)
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putBoolean("debug", BuildConfig.DEBUG || debug);

                new SimpleTask<Pair<String[], String[]>>() {
                    @Override
                    protected Pair<String[], String[]> onExecute(Context context, Bundle args) throws Throwable {
                        boolean debug = args.getBoolean("debug");

                        String[] ids = NotificationHelper.getChannelIds(context);
                        List<String> channels = new ArrayList<>();
                        List<String> titles = new ArrayList<>();

                        DB db = DB.getInstance(context);

                        for (int i = 0; i < ids.length; i++)
                            try {
                                if (ids[i].startsWith("notification.folder.")) {
                                    long fid = Long.parseLong(ids[i].split("\\.")[2]);
                                    EntityFolder folder = db.folder().getFolder(fid);
                                    EntityAccount account = db.account().getAccount(folder == null ? -1L : folder.account);
                                    channels.add(ids[i]);
                                    titles.add(folder == null ? ids[i] : account.name + "/" + folder.name);
                                } else if (ids[i].startsWith("notification.")) {
                                    String[] parts = ids[i].split("\\.");
                                    if (parts.length == 2 && TextUtils.isDigitsOnly(parts[1])) {
                                        long aid = Long.parseLong(parts[1]);
                                        EntityAccount account = db.account().getAccount(aid);
                                        channels.add(ids[i]);
                                        titles.add(account == null ? ids[i] : account.name);
                                    } else {
                                        channels.add(ids[i]);
                                        titles.add(ids[i].substring("notification.".length()));
                                    }
                                } else {
                                    channels.add(ids[i]);
                                    titles.add(ids[i]);
                                }
                            } catch (Throwable ex) {
                                Log.e(ex);
                                channels.add(ids[i]);
                                titles.add(ids[i]);
                            }

                        return new Pair<>(channels.toArray(new String[0]), titles.toArray(new String[0]));
                    }

                    @Override
                    protected void onExecuted(Bundle args, Pair<String[], String[]> data) {
                        boolean[] selected = new boolean[data.first.length];

                        new AlertDialog.Builder(v.getContext())
                                .setIcon(R.drawable.twotone_delete_24)
                                .setTitle(R.string.title_advanced_notifications_delete)
                                .setMultiChoiceItems(data.second, selected, new DialogInterface.OnMultiChoiceClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                        selected[which] = isChecked;
                                    }
                                })
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Bundle args = new Bundle();
                                        args.putStringArray("ids", data.first);
                                        args.putBooleanArray("selected", selected);

                                        new SimpleTask<Void>() {
                                            @Override
                                            protected Void onExecute(Context context, Bundle args) throws Throwable {
                                                String[] ids = args.getStringArray("ids");
                                                boolean[] selected = args.getBooleanArray("selected");

                                                DB db = DB.getInstance(context);

                                                for (int i = 0; i < selected.length; i++)
                                                    try {
                                                        if (!selected[i])
                                                            continue;

                                                        if (ids[i].startsWith("notification.")) {
                                                            String[] parts = ids[i].split("\\.");
                                                            if (parts.length == 2 && TextUtils.isDigitsOnly(parts[1])) {
                                                                long aid = Long.parseLong(ids[i].split("\\.")[1]);
                                                                if (db.account().setAccountNotify(aid, false) != 1)
                                                                    continue;
                                                            }
                                                        }

                                                        NotificationHelper.deleteChannel(context, ids[i]);
                                                    } catch (Throwable ex) {
                                                        Log.e(ex);
                                                    }

                                                return null;
                                            }

                                            @Override
                                            protected void onException(Bundle args, Throwable ex) {
                                                Log.unexpectedError(getParentFragmentManager(), ex);
                                            }
                                        }.execute(FragmentOptionsNotifications.this, args, "channel:delete");
                                    }
                                })
                                .setNegativeButton(android.R.string.cancel, null)
                                .show();
                    }

                    @Override
                    protected void onException(Bundle args, Throwable ex) {
                        Log.unexpectedError(getParentFragmentManager(), ex);
                    }
                }.execute(FragmentOptionsNotifications.this, args, "channel:list");
            }
        });

        final Intent channelNotification = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, getContext().getPackageName())
                .putExtra(Settings.EXTRA_CHANNEL_ID, "notification");

        btnManageDefault.setEnabled(channelNotification.resolveActivity(pm) != null); // system whitelisted
        btnManageDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(channelNotification);
            }
        });

        ivChannelDefault.setVisibility(View.GONE);

        final Intent channelService = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, getContext().getPackageName())
                .putExtra(Settings.EXTRA_CHANNEL_ID, "service");

        btnManageService.setEnabled(channelService.resolveActivity(pm) != null); // system whitelisted
        btnManageService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(channelService);
            }
        });

        ivChannelService.setVisibility(View.GONE);

        ibWhy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 2);
            }
        });

        tvNotifySeparate.setPaintFlags(tvNotifySeparate.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvNotifySeparate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Helper.viewFAQ(view.getContext(), 145);
            }
        });

        swNewestFirst.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("notify_newest_first", checked).apply();
            }
        });

        swNotifySummary.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("notify_summary", checked).apply();
                enableOptions();
            }
        });

        cbNotifyActionTrash.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
                prefs.edit().putBoolean("notify_trash", checked).apply();
            }
        });

        cbNotifyActionJunk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
                prefs.edit().putBoolean("notify_junk", checked).apply();
                cbNotifyActionBlockSender.setEnabled(checked);
            }
        });

        cbNotifyActionBlockSender.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
                prefs.edit().putBoolean("notify_block_sender", checked).apply();
            }
        });

        cbNotifyActionArchive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
                prefs.edit().putBoolean("notify_archive", checked).apply();
            }
        });

        cbNotifyActionMove.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
                prefs.edit().putBoolean("notify_move", checked).apply();
            }
        });

        cbNotifyActionReply.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
                prefs.edit().putBoolean("notify_reply", checked).apply();
            }
        });

        cbNotifyActionReplyDirect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
                prefs.edit().putBoolean("notify_reply_direct", checked).apply();
            }
        });

        cbNotifyActionFlag.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
                prefs.edit().putBoolean("notify_flag", checked).apply();
            }
        });

        cbNotifyActionSeen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
                prefs.edit().putBoolean("notify_seen", checked).apply();
            }
        });

        cbNotifyActionHide.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
                prefs.edit().putBoolean("notify_hide", checked).apply();
            }
        });

        cbNotifyActionSnooze.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
                prefs.edit().putBoolean("notify_snooze", checked).apply();
            }
        });

        Helper.linkPro(tvNotifyActionsPro);
        Helper.linkPro(tvNotifyKnownPro);

        swLight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("light", checked).apply();
            }
        });

        grpScreenOn.setVisibility(
                BuildConfig.DEBUG ||
                        Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU ||
                        hasPermission(Manifest.permission.TURN_SCREEN_ON)
                        ? View.VISIBLE : View.GONE);
        swNotifyScreenOn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("notify_screen_on", checked).apply();
            }
        });

        btnSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sound = prefs.getString("sound", null);
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.title_advanced_sound));
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, sound == null ? null : Uri.parse(sound));
                startActivityForResult(Helper.getChooser(getContext(), intent), ActivitySetup.REQUEST_SOUND_INBOUND);
            }
        });

        swBadge.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("badge", checked).apply();
                ServiceSynchronize.restart(compoundButton.getContext());
            }
        });

        ibBadge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 106);
            }
        });

        swUnseenIgnored.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("unseen_ignored", checked).apply();
                ServiceSynchronize.restart(compoundButton.getContext());
            }
        });

        swNotifyGrouping.setVisibility(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? View.VISIBLE : View.GONE);
        swNotifyGrouping.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("notify_grouping", checked).apply();
            }
        });

        swNotifyPrivate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("notify_private", checked).apply();
            }
        });

        swNotifyBackgroundOnly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("notify_background_only", checked).apply();
                enableOptions();
            }
        });

        swNotifyKnownOnly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("notify_known", checked).apply();
                enableOptions();
            }
        });

        swNotifySuppressInCall.setVisibility(
                Build.VERSION.SDK_INT < Build.VERSION_CODES.S
                        ? View.GONE : View.VISIBLE);
        swNotifySuppressInCall.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("notify_suppress_in_call", checked).apply();
                ServiceSynchronize.restart(compoundButton.getContext());
            }
        });

        swNotifySuppressInCar.setVisibility(
                Build.VERSION.SDK_INT < Build.VERSION_CODES.M || BuildConfig.PLAY_STORE_RELEASE
                        ? View.GONE : View.VISIBLE);
        swNotifySuppressInCar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("notify_suppress_in_car", checked).apply();
                ServiceSynchronize.restart(compoundButton.getContext());
            }
        });

        swNotifyRemove.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("notify_remove", checked).apply();
            }
        });

        swNotifyClear.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("notify_clear", checked).apply();
            }
        });

        swNotifySubtext.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("notify_subtext", checked).apply();
            }
        });

        swNotifyPreview.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("notify_preview", checked).apply();
                enableOptions();
            }
        });

        swNotifyPreviewAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("notify_preview_all", checked).apply();
            }
        });

        swNotifyPreviewOnly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("notify_preview_only", checked).apply();
            }
        });

        swNotifyTransliterate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("notify_transliterate", checked).apply();
            }
        });

        swNotifyAscii.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("notify_ascii", checked).apply();
            }
        });

        ibLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 21);
            }
        });

        swWearablePreview.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("wearable_preview", checked).apply();
            }
        });

        ibWearable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 126);
            }
        });

        swMessagingStyle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("notify_messaging", checked).apply();
            }
        });

        ibCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 165);
            }
        });

        swBiometricsNotify.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("biometrics_notify", checked).apply();
            }
        });

        swNotifyOpenFolder.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("notify_open_folder", checked).apply();
            }
        });

        swBackground.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("background_service", checked).apply();
                ServiceSynchronize.eval(compoundButton.getContext(), "background=" + checked);
            }
        });

        swAlertOnce.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("alert_once", !checked).apply();
            }
        });

        ibTileSync.setOnClickListener(new View.OnClickListener() {
            @Override
            @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
            public void onClick(View v) {
                addTile(v.getContext(), ServiceTileSynchronize.class, R.string.tile_synchronize, R.drawable.twotone_sync_24);
            }
        });

        ibTileUnseen.setOnClickListener(new View.OnClickListener() {
            @Override
            @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
            public void onClick(View v) {
                addTile(v.getContext(), ServiceTileUnseen.class, R.string.tile_unseen, R.drawable.twotone_mail_outline_24);
            }
        });

        // Initialize
        swNotifyTransliterate.setVisibility(TextHelper.canTransliterate() ? View.VISIBLE : View.GONE);
        swUnseenIgnored.setVisibility(Helper.isXiaomi() ? View.GONE : View.VISIBLE);
        swAlertOnce.setVisibility(Helper.isXiaomi() || BuildConfig.DEBUG ? View.VISIBLE : View.GONE);

        // https://developer.android.com/training/notify-user/group
        tvNoGrouping.setVisibility(Build.VERSION.SDK_INT < Build.VERSION_CODES.N ? View.VISIBLE : View.GONE);

        // https://developer.android.com/training/notify-user/channels
        tvNoChannels.setVisibility(Build.VERSION.SDK_INT < Build.VERSION_CODES.O ? View.VISIBLE : View.GONE);

        grpChannel.setVisibility(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? View.VISIBLE : View.GONE);
        grpProperties.setVisibility(
                Build.VERSION.SDK_INT < Build.VERSION_CODES.O || BuildConfig.DEBUG
                        ? View.VISIBLE : View.GONE);
        grpBackground.setVisibility(
                Build.VERSION.SDK_INT < Build.VERSION_CODES.O || BuildConfig.DEBUG
                        ? View.VISIBLE : View.GONE);
        grpTiles.setVisibility(
                Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || !BuildConfig.DEBUG
                        ? View.GONE : View.VISIBLE);

        PreferenceManager.getDefaultSharedPreferences(getContext()).registerOnSharedPreferenceChangeListener(this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = Helper.getSystemService(getContext(), NotificationManager.class);

            NotificationChannel notification = nm.getNotificationChannel("notification");
            if (notification != null) {
                ivChannelDefault.setImageLevel(notification.getImportance() == NotificationManager.IMPORTANCE_NONE ? 0 : 1);
                ivChannelDefault.setVisibility(View.VISIBLE);
            }

            NotificationChannel service = nm.getNotificationChannel("service");
            if (service != null) {
                ivChannelService.setImageLevel(service.getImportance() == NotificationManager.IMPORTANCE_NONE ? 0 : 1);
                ivChannelService.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onDestroyView() {
        PreferenceManager.getDefaultSharedPreferences(getContext()).unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroyView();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (!RESET_OPTIONS.contains(key))
            return;

        getMainHandler().removeCallbacks(update);
        getMainHandler().postDelayed(update, FragmentOptions.DELAY_SETOPTIONS);
    }

    private Runnable update = new RunnableEx("notifications") {
        @Override
        protected void delegate() {
            setOptions();
        }
    };

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_options, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_default) {
            FragmentOptions.reset(getContext(), RESET_OPTIONS, null);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setOptions() {
        try {
            if (view == null || getContext() == null)
                return;

            boolean pro = ActivityBilling.isPro(getContext());
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

            swNewestFirst.setChecked(prefs.getBoolean("notify_newest_first", false));
            swNotifySummary.setChecked(prefs.getBoolean("notify_summary", false));

            cbNotifyActionTrash.setChecked(prefs.getBoolean("notify_trash", true) || !pro);
            cbNotifyActionJunk.setChecked(prefs.getBoolean("notify_junk", false) && pro);
            cbNotifyActionBlockSender.setChecked(prefs.getBoolean("notify_block_sender", false) && pro);
            cbNotifyActionArchive.setChecked(prefs.getBoolean("notify_archive", true) || !pro);
            cbNotifyActionMove.setChecked(prefs.getBoolean("notify_move", false) && pro);
            cbNotifyActionReply.setChecked(prefs.getBoolean("notify_reply", false) && pro);
            cbNotifyActionReplyDirect.setChecked(prefs.getBoolean("notify_reply_direct", false) && pro);
            cbNotifyActionFlag.setChecked(prefs.getBoolean("notify_flag", false) && pro);
            cbNotifyActionSeen.setChecked(prefs.getBoolean("notify_seen", true) || !pro);
            cbNotifyActionHide.setChecked(prefs.getBoolean("notify_hide", false) && pro);
            cbNotifyActionSnooze.setChecked(prefs.getBoolean("notify_snooze", false) && pro);
            swLight.setChecked(prefs.getBoolean("light", false));
            swNotifyScreenOn.setChecked(prefs.getBoolean("notify_screen_on", false));

            swBadge.setChecked(prefs.getBoolean("badge", true));
            swUnseenIgnored.setChecked(prefs.getBoolean("unseen_ignored", false));
            swNotifyGrouping.setChecked(prefs.getBoolean("notify_grouping", true));
            swNotifyPrivate.setChecked(prefs.getBoolean("notify_private", true));
            swNotifyBackgroundOnly.setChecked(prefs.getBoolean("notify_background_only", false));
            swNotifyKnownOnly.setChecked(prefs.getBoolean("notify_known", false));
            swNotifySuppressInCall.setChecked(prefs.getBoolean("notify_suppress_in_call", false));
            swNotifySuppressInCar.setChecked(prefs.getBoolean("notify_suppress_in_car", false));
            swNotifyRemove.setChecked(prefs.getBoolean("notify_remove", true));
            swNotifyClear.setChecked(prefs.getBoolean("notify_clear", false));
            swNotifySubtext.setChecked(prefs.getBoolean("notify_subtext", true));
            swNotifyPreview.setChecked(prefs.getBoolean("notify_preview", true));
            swNotifyPreviewAll.setChecked(prefs.getBoolean("notify_preview_all", false));
            swNotifyPreviewOnly.setChecked(prefs.getBoolean("notify_preview_only", false));
            swNotifyTransliterate.setChecked(prefs.getBoolean("notify_transliterate", false));
            swNotifyAscii.setChecked(prefs.getBoolean("notify_ascii", false));
            swWearablePreview.setChecked(prefs.getBoolean("wearable_preview", false));
            swMessagingStyle.setChecked(prefs.getBoolean("notify_messaging", false));
            swBiometricsNotify.setChecked(prefs.getBoolean("biometrics_notify", true));
            swNotifyOpenFolder.setChecked(prefs.getBoolean("notify_open_folder", false));
            swBackground.setChecked(prefs.getBoolean("background_service", false));
            swAlertOnce.setChecked(!prefs.getBoolean("alert_once", true));

            enableOptions();
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    private void enableOptions() {
        boolean pro = ActivityBilling.isPro(getContext());
        boolean summary = swNotifySummary.isChecked();

        cbNotifyActionTrash.setEnabled(pro && !summary);
        cbNotifyActionJunk.setEnabled(pro && !summary);
        cbNotifyActionBlockSender.setEnabled(pro && !summary && cbNotifyActionJunk.isChecked());
        cbNotifyActionArchive.setEnabled(pro && !summary);
        cbNotifyActionMove.setEnabled(pro && !summary);
        cbNotifyActionReply.setEnabled(pro && !summary);
        cbNotifyActionReplyDirect.setEnabled(pro && !summary);
        cbNotifyActionFlag.setEnabled(pro && !summary);
        cbNotifyActionSeen.setEnabled(pro && !summary);
        cbNotifyActionHide.setEnabled(pro && !summary);
        cbNotifyActionSnooze.setEnabled(pro && !summary);
        swNotifyPreviewAll.setEnabled(!summary && swNotifyPreview.isChecked());
        swNotifyPreviewOnly.setEnabled(!summary && swNotifyPreview.isChecked());
        swWearablePreview.setEnabled(!summary && swNotifyPreview.isChecked());
        swBiometricsNotify.setEnabled(!summary);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            switch (requestCode) {
                case ActivitySetup.REQUEST_SOUND_INBOUND:
                    if (resultCode == RESULT_OK && data != null)
                        onSelectSound(data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI));
                    break;
            }
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    private void onSelectSound(Uri uri) {
        Log.i("Selected ringtone=" + uri);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        if (uri == null) // silent sound
            prefs.edit().putString("sound", "").apply();
        else {
            if ("content".equals(uri.getScheme())) {
                try {
                    getContext().getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    if (!Helper.isPersisted(getContext(), uri, true, false))
                        Log.unexpectedError(FragmentOptionsNotifications.this,
                                new IllegalStateException("No permission granted to access selected sound " + uri));
                } catch (Throwable ex) {
                    Log.w(ex);
                }
                prefs.edit().putString("sound", uri.toString()).apply();
            } else
                prefs.edit().remove("sound").apply();
        }
    }

    @RequiresApi(api = 33)
    private void addTile(Context context, Class<? extends TileService> cls, int title, int icon) {
        StatusBarManager sbm = Helper.getSystemService(context, StatusBarManager.class);
        sbm.requestAddTileService(
                ComponentName.createRelative(context, cls.getName()),
                context.getString(title),
                Icon.createWithResource(context, icon),
                Helper.getUIExecutor(),
                new Consumer<Integer>() {
                    @Override
                    public void accept(Integer result) {
                        Log.i("Tile result=" + result + " class=" + cls.getName());
                        if (result == null)
                            return;
                        switch (result) {
                            case StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ADDED:
                            case StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_NOT_ADDED:
                                break;
                            case StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ALREADY_ADDED:
                                getMainHandler().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        ToastEx.makeText(context, R.string.tile_already_added, Toast.LENGTH_LONG).show();
                                    }
                                });
                                break;
                            default:
                                Log.e("Tile result=" + result + " class=" + cls.getName());
                        }
                    }
                });
    }
}
