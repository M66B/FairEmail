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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.Group;

import static android.app.Activity.RESULT_OK;

public class FragmentOptions extends FragmentEx implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SwitchCompat swEnabled;
    private SwitchCompat swUpdates;

    private SwitchCompat swMetered;
    private Spinner spDownload;

    private SwitchCompat swUnified;
    private SwitchCompat swThreading;
    private SwitchCompat swCompact;
    private SwitchCompat swAvatars;
    private SwitchCompat swIdenticons;
    private SwitchCompat swPreview;

    private SwitchCompat swLight;
    private Button btnSound;

    private SwitchCompat swSwipe;
    private SwitchCompat swActionbar;
    private SwitchCompat swAutoClose;
    private SwitchCompat swAutoRead;
    private SwitchCompat swCollapse;
    private SwitchCompat swAutoMove;
    private SwitchCompat swConfirm;
    private SwitchCompat swSender;

    private SwitchCompat swDebug;

    private Group grpNotification;

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_advanced);
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_options, container, false);

        // Get controls
        swEnabled = view.findViewById(R.id.swEnabled);
        swUpdates = view.findViewById(R.id.swUpdates);

        swMetered = view.findViewById(R.id.swMetered);
        spDownload = view.findViewById(R.id.spDownload);

        swUnified = view.findViewById(R.id.swUnified);
        swThreading = view.findViewById(R.id.swThreading);
        swCompact = view.findViewById(R.id.swCompact);
        swAvatars = view.findViewById(R.id.swAvatars);
        swIdenticons = view.findViewById(R.id.swIdenticons);
        swPreview = view.findViewById(R.id.swPreview);

        swLight = view.findViewById(R.id.swLight);
        btnSound = view.findViewById(R.id.btnSound);

        swSwipe = view.findViewById(R.id.swSwipe);
        swActionbar = view.findViewById(R.id.swActionbar);
        swAutoClose = view.findViewById(R.id.swAutoClose);
        swAutoRead = view.findViewById(R.id.swAutoRead);
        swCollapse = view.findViewById(R.id.swCollapse);
        swAutoMove = view.findViewById(R.id.swAutoMove);
        swConfirm = view.findViewById(R.id.swConfirm);
        swSender = view.findViewById(R.id.swSender);

        swDebug = view.findViewById(R.id.swDebug);

        grpNotification = view.findViewById(R.id.grpNotification);

        // Wire controls

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        swEnabled.setChecked(prefs.getBoolean("enabled", true));
        swEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("enabled", checked).apply();
                ServiceSynchronize.reload(getContext(), "enabled=" + checked);
            }
        });

        swUpdates.setChecked(prefs.getBoolean("updates", true));
        swUpdates.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("updates", checked).apply();
            }
        });
        swUpdates.setVisibility(Helper.isPlayStoreInstall(getContext()) ? View.GONE : View.VISIBLE);

        swMetered.setChecked(prefs.getBoolean("metered", true));
        swMetered.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("metered", checked).apply();
                ServiceSynchronize.reload(getContext(), "metered=" + checked);
            }
        });

        int download = prefs.getInt("download", 32768);
        final int[] values = getResources().getIntArray(R.array.downloadValues);
        for (int i = 0; i < values.length; i++)
            if (values[i] == download) {
                spDownload.setSelection(i);
                break;
            }
        spDownload.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                prefs.edit().putInt("download", values[position]).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                prefs.edit().remove("download").apply();
            }
        });

        swUnified.setChecked(prefs.getBoolean("unified", true));
        swUnified.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("unified", checked).apply();
            }
        });

        swThreading.setChecked(prefs.getBoolean("threading", true));
        swThreading.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("threading", checked).apply();
            }
        });

        swCompact.setChecked(prefs.getBoolean("compact", false));
        swCompact.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("compact", checked).apply();
                prefs.edit().remove("zoom").apply();
            }
        });

        swAvatars.setChecked(prefs.getBoolean("avatars", true));
        swAvatars.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("avatars", checked).apply();
            }
        });

        swIdenticons.setChecked(prefs.getBoolean("identicons", false));
        swIdenticons.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("identicons", checked).apply();
            }
        });

        swPreview.setChecked(prefs.getBoolean("preview", false));
        swPreview.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("preview", checked).apply();
                if (checked)
                    new SimpleTask<Void>() {
                        @Override
                        protected Void onExecute(Context context, Bundle args) {
                            DB db = DB.getInstance(context);

                            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                            boolean metered = (cm == null || cm.isActiveNetworkMetered());

                            for (Long id : db.message().getMessageWithoutPreview()) {
                                EntityMessage message = db.message().getMessage(id);
                                try {
                                    Log.i("Building preview id=" + id);
                                    String body = message.read(context);
                                    db.message().setMessageContent(
                                            message.id, true, HtmlHelper.getPreview(body));
                                } catch (IOException ex) {
                                    Log.e(ex);
                                    db.message().setMessageContent(message.id, false, null);
                                    if (!metered)
                                        EntityOperation.queue(db, message, EntityOperation.BODY);
                                }
                            }

                            return null;
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Helper.unexpectedError(getContext(), getViewLifecycleOwner(), ex);
                        }
                    }.execute(FragmentOptions.this, null);
            }
        });

        swLight.setChecked(prefs.getBoolean("light", false));
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

        swSwipe.setChecked(prefs.getBoolean("swipe", true));
        swSwipe.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("swipe", checked).apply();
            }
        });

        swActionbar.setChecked(prefs.getBoolean("actionbar", true));
        swActionbar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("actionbar", checked).apply();
            }
        });

        swAutoClose.setChecked(prefs.getBoolean("autoclose", true));
        swAutoClose.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("autoclose", checked).apply();
            }
        });

        swAutoRead.setChecked(prefs.getBoolean("autoread", false));
        swAutoRead.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("autoread", checked).apply();
            }
        });

        swCollapse.setChecked(prefs.getBoolean("collapse", false));
        swCollapse.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("collapse", checked).apply();
            }
        });

        swAutoMove.setChecked(!prefs.getBoolean("automove", false));
        swAutoMove.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("automove", !checked).apply();
            }
        });

        swConfirm.setChecked(prefs.getBoolean("confirm", false));
        swConfirm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("confirm", checked).apply();
            }
        });

        swSender.setChecked(prefs.getBoolean("sender", false));
        swSender.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("sender", checked).apply();
            }
        });

        swDebug.setChecked(prefs.getBoolean("debug", false));
        swDebug.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                prefs.edit().putBoolean("debug", checked).apply();
                ServiceSynchronize.reload(getContext(), "debug=" + checked);
            }
        });

        grpNotification.setVisibility(BuildConfig.DEBUG || Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O ? View.VISIBLE : View.GONE);

        PreferenceManager.getDefaultSharedPreferences(getContext()).registerOnSharedPreferenceChangeListener(this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        // Removed because of Android VPN service
        // builder.addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        cm.registerNetworkCallback(builder.build(), networkCallback);
    }

    @Override
    public void onPause() {
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        cm.unregisterNetworkCallback(networkCallback);

        super.onPause();
    }

    private ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            getActivity().invalidateOptionsMenu();
        }

        @Override
        public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
            getActivity().invalidateOptionsMenu();
        }

        @Override
        public void onLost(Network network) {
            getActivity().invalidateOptionsMenu();
        }
    };

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_options, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Boolean metered = Helper.isMetered(getContext(), false);
        menu.findItem(R.id.menu_metered).setVisible(metered != null);
        if (metered != null)
            menu.findItem(R.id.menu_metered).setIcon(
                    metered ? R.drawable.baseline_attach_money_24 : R.drawable.baseline_money_off_24);
        super.onPrepareOptionsMenu(menu);
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
    }

    @Override
    public void onDestroyView() {
        PreferenceManager.getDefaultSharedPreferences(getContext()).unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroyView();
    }
}
