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

    Copyright 2018-2019 by Marcel Bokhorst (M66B)
*/

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;

import static android.app.Activity.RESULT_OK;

public class FragmentOptions extends FragmentBase implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SwitchCompat swEnabled;
    private SwitchCompat swSchedule;
    private TextView tvScheduleStart;
    private TextView tvScheduleEnd;

    private TextView tvConnectionType;
    private SwitchCompat swMetered;
    private Spinner spDownload;

    private SwitchCompat swUnified;
    private SwitchCompat swDate;
    private SwitchCompat swThreading;
    private SwitchCompat swAvatars;
    private SwitchCompat swIdenticons;
    private SwitchCompat swNameEmail;
    private SwitchCompat swSubjectItalic;
    private SwitchCompat swFlags;
    private SwitchCompat swPreview;
    private SwitchCompat swAddresses;
    private SwitchCompat swHtml;
    private SwitchCompat swImages;
    private SwitchCompat swActionbar;

    private SwitchCompat swPull;
    private SwitchCompat swSwipeNav;
    private SwitchCompat swAutoExpand;
    private SwitchCompat swAutoClose;
    private SwitchCompat swAutoNext;
    private SwitchCompat swCollapse;
    private SwitchCompat swAutoRead;
    private SwitchCompat swAutoMove;
    private SwitchCompat swAutoResize;
    private SwitchCompat swSender;
    private SwitchCompat swAutoSend;

    private SwitchCompat swNotifyPreview;
    private SwitchCompat swSearchLocal;
    private SwitchCompat swLight;
    private Button btnSound;

    private SwitchCompat swUpdates;
    private SwitchCompat swDebug;

    private Group grpNotification;

    static String[] OPTIONS_RESTART = new String[]{
            "unified", "date", "threading", "avatars", "identicons", "name_email", "subject_italic", "flags", "preview", "addresses", "autoimages", "actionbar",
            "pull", "swipenav", "autoexpand", "autoclose", "autonext",
            "debug"
    };

    private final static String[] ADVANCED_OPTIONS = new String[]{
            "enabled", "schedule_start", "schedule_end",
            "metered", "download",
            "unified", "date", "threading", "avatars", "identicons", "name_email", "subject_italic", "flags", "preview", "addresses", "autoimages", "actionbar",
            "pull", "swipenav", "autoexpand", "autoclose", "autonext", "collapse", "autoread", "automove",
            "autoresize", "sender", "autosend",
            "notify_preview", "search_local", "light", "sound",
            "updates", "debug",
            "first", "why", "last_update_check", "app_support", "message_swipe", "message_select", "folder_actions", "folder_sync",
            "edit_ref_confirmed", "show_html_confirmed", "show_images_confirmed", "print_html_confirmed", "show_organization"
    };

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_advanced);
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_options, container, false);

        // Get controls
        swEnabled = view.findViewById(R.id.swEnabled);
        swSchedule = view.findViewById(R.id.swSchedule);
        tvScheduleStart = view.findViewById(R.id.tvScheduleStart);
        tvScheduleEnd = view.findViewById(R.id.tvScheduleEnd);

        tvConnectionType = view.findViewById(R.id.tvConnectionType);
        swMetered = view.findViewById(R.id.swMetered);
        spDownload = view.findViewById(R.id.spDownload);

        swUnified = view.findViewById(R.id.swUnified);
        swDate = view.findViewById(R.id.swDate);
        swThreading = view.findViewById(R.id.swThreading);
        swAvatars = view.findViewById(R.id.swAvatars);
        swIdenticons = view.findViewById(R.id.swIdenticons);
        swNameEmail = view.findViewById(R.id.swNameEmail);
        swSubjectItalic = view.findViewById(R.id.swSubjectItalic);
        swFlags = view.findViewById(R.id.swFlags);
        swPreview = view.findViewById(R.id.swPreview);
        swAddresses = view.findViewById(R.id.swAddresses);
        swHtml = view.findViewById(R.id.swHtml);
        swImages = view.findViewById(R.id.swImages);
        swActionbar = view.findViewById(R.id.swActionbar);

        swPull = view.findViewById(R.id.swPull);
        swSwipeNav = view.findViewById(R.id.swSwipeNav);
        swAutoExpand = view.findViewById(R.id.swAutoExpand);
        swAutoClose = view.findViewById(R.id.swAutoClose);
        swAutoNext = view.findViewById(R.id.swAutoNext);
        swCollapse = view.findViewById(R.id.swCollapse);
        swAutoRead = view.findViewById(R.id.swAutoRead);
        swAutoMove = view.findViewById(R.id.swAutoMove);
        swAutoResize = view.findViewById(R.id.swAutoResize);
        swSender = view.findViewById(R.id.swSender);
        swAutoSend = view.findViewById(R.id.swAutoSend);

        swNotifyPreview = view.findViewById(R.id.swNotifyPreview);
        swSearchLocal = view.findViewById(R.id.swSearchLocal);
        swLight = view.findViewById(R.id.swLight);
        btnSound = view.findViewById(R.id.btnSound);

        swUpdates = view.findViewById(R.id.swUpdates);
        swDebug = view.findViewById(R.id.swDebug);

        grpNotification = view.findViewById(R.id.grpNotification);

        // Wire controls

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        setOptions();

        swEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("enabled", checked).apply();
                ServiceSynchronize.reload(getContext(), "enabled=" + checked);
            }
        });

        swSchedule.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    if (Helper.isPro(getContext())) {
                        prefs.edit().putBoolean("schedule", true).apply();
                        ServiceSynchronize.reschedule(getContext());
                    } else {
                        swSchedule.setChecked(false);
                        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.content_frame, new FragmentPro()).addToBackStack("pro");
                        fragmentTransaction.commit();
                    }
                } else {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("schedule", false);
                    editor.putBoolean("enabled", true);
                    editor.apply();
                    ServiceSynchronize.reload(getContext(), "schedule=" + checked);
                }
            }
        });

        tvScheduleStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putBoolean("start", true);
                DialogFragment timePicker = new TimePickerFragment();
                timePicker.setArguments(args);
                timePicker.show(getFragmentManager(), "timePicker");
            }
        });

        tvScheduleEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putBoolean("start", false);
                DialogFragment timePicker = new TimePickerFragment();
                timePicker.setArguments(args);
                timePicker.show(getFragmentManager(), "timePicker");
            }
        });

        swUpdates.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("updates", checked).apply();
            }
        });

        swMetered.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("metered", checked).apply();
                ServiceSynchronize.reload(getContext(), "metered=" + checked);
            }
        });

        spDownload.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                Integer prev = (Integer) adapterView.getTag();
                if (!Objects.equals(prev, position)) {
                    adapterView.setTag(position);

                    int[] values = getResources().getIntArray(R.array.downloadValues);
                    prefs.edit().putInt("download", values[position]).apply();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                prefs.edit().remove("download").apply();
            }
        });

        swUnified.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("unified", checked).apply();
            }
        });

        swDate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("date", checked).apply();
            }
        });

        swThreading.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("threading", checked).apply();
            }
        });

        swAvatars.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("avatars", checked).apply();
                ContactInfo.clearCache();
            }
        });

        swIdenticons.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("identicons", checked).apply();
                ContactInfo.clearCache();
            }
        });

        swNameEmail.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("name_email", checked).apply();
            }
        });

        swSubjectItalic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("subject_italic", checked).apply();
            }
        });

        swFlags.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("flags", checked).apply();
            }
        });

        swPreview.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("preview", checked).apply();
            }
        });

        swAddresses.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("addresses", checked).apply();
            }
        });

        swHtml.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("autohtml", checked).apply();
                swImages.setEnabled(!checked);
            }
        });

        swImages.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("autoimages", checked).apply();
            }
        });

        swActionbar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("actionbar", checked).apply();
            }
        });

        swPull.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("pull", checked).apply();
            }
        });

        swSwipeNav.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("swipenav", checked).apply();
            }
        });

        swAutoExpand.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("autoexpand", checked).apply();
            }
        });

        swAutoClose.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("autoclose", checked).apply();
                swAutoNext.setEnabled(!checked);
            }
        });

        swAutoNext.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("autonext", checked).apply();
            }
        });

        swCollapse.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("collapse", checked).apply();
            }
        });

        swAutoRead.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("autoread", checked).apply();
            }
        });

        swAutoMove.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("automove", !checked).apply();
            }
        });

        swAutoResize.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("autoresize", checked).apply();
            }
        });

        swSender.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("sender", checked).apply();
            }
        });

        swAutoSend.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("autosend", !checked).apply();
            }
        });

        swNotifyPreview.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("notify_preview", checked).apply();
            }
        });

        swSearchLocal.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("search_local", checked).apply();
            }
        });

        swLight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("light", checked).apply();
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
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, sound == null ? null : Uri.parse(sound));
                startActivityForResult(Helper.getChooser(getContext(), intent), ActivitySetup.REQUEST_SOUND);
            }
        });

        swDebug.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("debug", checked).apply();
                ServiceSynchronize.reload(getContext(), "debug=" + checked);
            }
        });

        PreferenceManager.getDefaultSharedPreferences(getContext()).registerOnSharedPreferenceChangeListener(this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        cm.registerNetworkCallback(builder.build(), networkCallback);
    }

    @Override
    public void onPause() {
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        cm.unregisterNetworkCallback(networkCallback);

        super.onPause();
    }

    @Override
    public void onDestroyView() {
        PreferenceManager.getDefaultSharedPreferences(getContext()).unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_options, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_default:
                onMenuDefault();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onMenuDefault() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = prefs.edit();
        for (String option : ADVANCED_OPTIONS)
            editor.remove(option);
        editor.apply();

        setOptions();
    }

    private void setOptions() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        swEnabled.setChecked(prefs.getBoolean("enabled", true));
        swSchedule.setChecked(prefs.getBoolean("schedule", false));

        tvScheduleStart.setText(formatHour(prefs.getInt("schedule_start", 0)));
        tvScheduleEnd.setText(formatHour(prefs.getInt("schedule_end", 0)));

        swMetered.setChecked(prefs.getBoolean("metered", true));

        int download = prefs.getInt("download", 32768);
        int[] values = getResources().getIntArray(R.array.downloadValues);
        for (int pos = 0; pos < values.length; pos++)
            if (values[pos] == download) {
                spDownload.setTag(pos);
                spDownload.setSelection(pos);
                break;
            }

        boolean compact = prefs.getBoolean("compact", false);

        swUnified.setChecked(prefs.getBoolean("unified", true));
        swDate.setChecked(prefs.getBoolean("date", true));
        swThreading.setChecked(prefs.getBoolean("threading", true));
        swAvatars.setChecked(prefs.getBoolean("avatars", true));
        swIdenticons.setChecked(prefs.getBoolean("identicons", false));
        swNameEmail.setChecked(prefs.getBoolean("name_email", !compact));
        swSubjectItalic.setChecked(prefs.getBoolean("subject_italic", true));
        swFlags.setChecked(prefs.getBoolean("flags", true));
        swPreview.setChecked(prefs.getBoolean("preview", false));
        swAddresses.setChecked(prefs.getBoolean("addresses", true));
        swHtml.setChecked(prefs.getBoolean("autohtml", false));
        swImages.setChecked(prefs.getBoolean("autoimages", false));
        swImages.setEnabled(!swHtml.isChecked());
        swActionbar.setChecked(prefs.getBoolean("actionbar", true));

        swPull.setChecked(prefs.getBoolean("pull", true));
        swSwipeNav.setChecked(prefs.getBoolean("swipenav", true));
        swAutoExpand.setChecked(prefs.getBoolean("autoexpand", true));
        swAutoClose.setChecked(prefs.getBoolean("autoclose", true));
        swAutoNext.setChecked(prefs.getBoolean("autonext", false));
        swAutoNext.setEnabled(!swAutoClose.isChecked());
        swCollapse.setChecked(prefs.getBoolean("collapse", false));
        swAutoRead.setChecked(prefs.getBoolean("autoread", false));
        swAutoMove.setChecked(!prefs.getBoolean("automove", false));
        swAutoResize.setChecked(prefs.getBoolean("autoresize", true));
        swSender.setChecked(prefs.getBoolean("sender", false));
        swAutoSend.setChecked(!prefs.getBoolean("autosend", false));

        swNotifyPreview.setChecked(prefs.getBoolean("notify_preview", true));
        swNotifyPreview.setEnabled(Helper.isPro(getContext()));
        swSearchLocal.setChecked(prefs.getBoolean("search_local", false));
        swLight.setChecked(prefs.getBoolean("light", false));
        swUpdates.setChecked(prefs.getBoolean("updates", true));
        swUpdates.setVisibility(Helper.isPlayStoreInstall(getContext()) ? View.GONE : View.VISIBLE);
        swDebug.setChecked(prefs.getBoolean("debug", false));

        grpNotification.setVisibility(BuildConfig.DEBUG || Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O ? View.VISIBLE : View.GONE);
    }

    private String formatHour(int minutes) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, minutes / 60);
        cal.set(Calendar.MINUTE, minutes % 60);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT).format(cal.getTime());
    }

    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Bundle args = getArguments();
            boolean start = args.getBoolean("start");

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            int minutes = prefs.getInt("schedule_" + (start ? "start" : "end"), 0);

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, minutes / 60);
            cal.set(Calendar.MINUTE, minutes % 60);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            return new TimePickerDialog(getActivity(), this,
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hour, int minute) {
            Bundle args = getArguments();
            boolean start = args.getBoolean("start");

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("schedule_" + (start ? "start" : "end"), hour * 60 + minute);
            editor.putBoolean("schedule", true);
            editor.apply();

            ServiceSynchronize.reschedule(getContext());
        }
    }

    private ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            showConnectionType();
        }

        @Override
        public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
            showConnectionType();
        }

        @Override
        public void onLost(Network network) {
            showConnectionType();
        }
    };

    private void showConnectionType() {
        FragmentActivity activity = getActivity();
        if (activity == null)
            return;

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                    Boolean metered = Helper.isMetered(getContext(), false);

                    tvConnectionType.setVisibility(metered == null ? View.GONE : View.VISIBLE);
                    if (metered != null)
                        tvConnectionType.setText(metered ? R.string.title_legend_metered : R.string.title_legend_unmetered);
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("Result class=" + this.getClass().getSimpleName() +
                " request=" + requestCode + " result=" + resultCode + " data=" + data);

        if (requestCode == ActivitySetup.REQUEST_SOUND)
            if (resultCode == RESULT_OK) {
                Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                Log.i("Selected ringtone=" + uri);

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                if (uri == null)
                    prefs.edit().remove("sound").apply();
                else
                    prefs.edit().putString("sound", uri.toString()).apply();
            }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if ("enabled".equals(key))
            swEnabled.setChecked(prefs.getBoolean(key, true));
        else if ("schedule".equals(key))
            swSchedule.setChecked(prefs.getBoolean(key, false));
        else if ("schedule_start".equals(key))
            tvScheduleStart.setText(formatHour(prefs.getInt(key, 0)));
        else if ("schedule_end".equals(key))
            tvScheduleEnd.setText(formatHour(prefs.getInt(key, 0)));
    }
}
