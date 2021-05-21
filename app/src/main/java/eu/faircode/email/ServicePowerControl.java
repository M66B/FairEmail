package eu.faircode.email;

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

@RequiresApi(api = Build.VERSION_CODES.R)
public class ServicePowerControl extends ControlsProviderService {
    private ReplayProcessor updatePublisher;

    private static String DEVICE_SYNC = BuildConfig.APPLICATION_ID + ".sync";

    @NonNull
    @Override
    public Flow.Publisher<Control> createPublisherForAllAvailable() {
        List controls = new ArrayList<>();
        Control control = new Control.StatelessBuilder(DEVICE_SYNC, getPendingIntent())
                .setCustomIcon(Icon.createWithResource(this, R.drawable.twotone_sync_24))
                .setTitle(getString(R.string.app_name))
                .setSubtitle(getString(R.string.title_widget_title_sync))
                .setDeviceType(DeviceTypes.TYPE_GENERIC_ON_OFF)
                .build();
        controls.add(control);
        return FlowAdapters.toFlowPublisher(Flowable.fromIterable(controls));
    }

    @NonNull
    @Override
    public Flow.Publisher<Control> createPublisherFor(@NonNull List<String> controlIds) {
        updatePublisher = ReplayProcessor.create();

        if (controlIds.contains(DEVICE_SYNC)) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            boolean enabled = prefs.getBoolean("enabled", true);

            Control control = new Control.StatefulBuilder(DEVICE_SYNC, getPendingIntent())
                    .setCustomIcon(Icon.createWithResource(this, enabled
                            ? R.drawable.twotone_sync_24
                            : R.drawable.twotone_sync_disabled_24))
                    .setTitle(getString(R.string.app_name))
                    .setSubtitle(getString(enabled
                            ? R.string.title_legend_synchronize_on
                            : R.string.title_legend_synchronize_off))
                    .setDeviceType(DeviceTypes.TYPE_GENERIC_ON_OFF)
                    .setStatus(Control.STATUS_OK)
                    .setControlTemplate(new ToggleTemplate(
                            DEVICE_SYNC,
                            new ControlButton(enabled, getString(R.string.title_widget_title_sync))
                    ))
                    .build();

            updatePublisher.onNext(control);
        }

        return FlowAdapters.toFlowPublisher(updatePublisher);
    }

    @Override
    public void performControlAction(@NonNull String controlId, @NonNull ControlAction action, @NonNull Consumer<Integer> consumer) {
        if (action instanceof BooleanAction) {
            consumer.accept(ControlAction.RESPONSE_OK);

            boolean enabled = ((BooleanAction) action).getNewState();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            prefs.edit().putBoolean("enabled", enabled).apply();

            Control control = new Control.StatefulBuilder(DEVICE_SYNC, getPendingIntent())
                    .setCustomIcon(Icon.createWithResource(this, enabled
                            ? R.drawable.twotone_sync_24
                            : R.drawable.twotone_sync_disabled_24))
                    .setTitle(getString(R.string.app_name))
                    .setSubtitle(getString(enabled
                            ? R.string.title_legend_synchronize_on
                            : R.string.title_legend_synchronize_off))
                    .setDeviceType(DeviceTypes.TYPE_GENERIC_ON_OFF)
                    .setStatus(Control.STATUS_OK)
                    .setControlTemplate(new ToggleTemplate(
                            DEVICE_SYNC,
                            new ControlButton(enabled, getString(R.string.title_widget_title_sync))
                    ))
                    .build();

            updatePublisher.onNext(control);
        }
    }

    private PendingIntent getPendingIntent() {
        Context context = getBaseContext();
        return PendingIntentCompat.getActivity(
                context,
                ActivityView.REQUEST_POWER,
                new Intent(context, ActivityView.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
