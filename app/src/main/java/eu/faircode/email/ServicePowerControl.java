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

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.service.controls.Control;
import android.service.controls.ControlsProviderService;
import android.service.controls.DeviceTypes;
import android.service.controls.actions.BooleanAction;
import android.service.controls.actions.ControlAction;
import android.service.controls.templates.ControlButton;
import android.service.controls.templates.ToggleTemplate;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.preference.PreferenceManager;

import org.reactivestreams.FlowAdapters;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.function.Consumer;

import io.reactivex.Flowable;
import io.reactivex.processors.ReplayProcessor;

// https://developer.android.com/guide/topics/ui/device-control

@RequiresApi(api = Build.VERSION_CODES.R)
public class ServicePowerControl extends ControlsProviderService {
    private ReplayProcessor updatePublisher;

    private static String DEVICE_SYNC_TOGGLE = BuildConfig.APPLICATION_ID + ".sync_toggle";

    @NonNull
    @Override
    public Flow.Publisher<Control> createPublisherForAllAvailable() {
        List controls = new ArrayList<>();

        Control controlSyncToggle = new Control.StatelessBuilder(DEVICE_SYNC_TOGGLE, getPendingIntent())
                .setCustomIcon(Icon.createWithResource(this, R.drawable.twotone_sync_24))
                .setTitle(getString(R.string.title_power_menu_sync))
                .setSubtitle(getString(R.string.title_power_menu_on_off))
                .setDeviceType(DeviceTypes.TYPE_GENERIC_ON_OFF)
                .build();
        controls.add(controlSyncToggle);

        return FlowAdapters.toFlowPublisher(Flowable.fromIterable(controls));
    }

    @NonNull
    @Override
    public Flow.Publisher<Control> createPublisherFor(@NonNull List<String> controlIds) {
        updatePublisher = ReplayProcessor.create();

        if (controlIds.contains(DEVICE_SYNC_TOGGLE)) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            boolean enabled = prefs.getBoolean("enabled", true);

            Control controlSyncToggle = new Control.StatefulBuilder(DEVICE_SYNC_TOGGLE, getPendingIntent())
                    .setCustomIcon(Icon.createWithResource(this, enabled
                            ? R.drawable.twotone_sync_24
                            : R.drawable.twotone_sync_disabled_24))
                    .setTitle(getString(R.string.title_power_menu_sync))
                    .setSubtitle(getString(enabled
                            ? R.string.title_power_menu_on
                            : R.string.title_power_menu_off))
                    .setDeviceType(DeviceTypes.TYPE_GENERIC_ON_OFF)
                    .setStatus(Control.STATUS_OK)
                    .setControlTemplate(new ToggleTemplate(
                            DEVICE_SYNC_TOGGLE,
                            new ControlButton(enabled, getString(R.string.title_widget_title_sync))
                    ))
                    .build();

            updatePublisher.onNext(controlSyncToggle);
        }

        return FlowAdapters.toFlowPublisher(updatePublisher);
    }

    @Override
    public void performControlAction(@NonNull String controlId, @NonNull ControlAction action, @NonNull Consumer<Integer> consumer) {
        if (DEVICE_SYNC_TOGGLE.equals(controlId)) {
            consumer.accept(ControlAction.RESPONSE_OK);

            boolean enabled = ((BooleanAction) action).getNewState();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            prefs.edit().putBoolean("enabled", enabled).apply();

            Control controlSyncToggle = new Control.StatefulBuilder(DEVICE_SYNC_TOGGLE, getPendingIntent())
                    .setCustomIcon(Icon.createWithResource(this, enabled
                            ? R.drawable.twotone_sync_24
                            : R.drawable.twotone_sync_disabled_24))
                    .setTitle(getString(R.string.title_power_menu_sync))
                    .setSubtitle(getString(enabled
                            ? R.string.title_power_menu_on
                            : R.string.title_power_menu_off))
                    .setDeviceType(DeviceTypes.TYPE_GENERIC_ON_OFF)
                    .setStatus(Control.STATUS_OK)
                    .setControlTemplate(new ToggleTemplate(
                            DEVICE_SYNC_TOGGLE,
                            new ControlButton(enabled, getString(R.string.title_power_menu_on_off))
                    ))
                    .build();

            updatePublisher.onNext(controlSyncToggle);
        }
    }

    private PendingIntent getPendingIntent() {
        Context context = getBaseContext();
        return PendingIntentCompat.getActivity(
                context,
                ActivityView.PI_POWER,
                new Intent(context, ActivitySetup.class)
                        .setAction("misc")
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .putExtra("tab", "misc"),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        CoalMine.watch(this, this.getClass().getName() + "#onDestroy");
    }
}
