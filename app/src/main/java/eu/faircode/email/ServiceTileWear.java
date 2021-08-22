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

import static androidx.wear.tiles.DimensionBuilders.dp;
import static androidx.wear.tiles.DimensionBuilders.expand;
import static androidx.wear.tiles.DimensionBuilders.wrap;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.wear.tiles.LayoutElementBuilders;
import androidx.wear.tiles.RequestBuilders;
import androidx.wear.tiles.ResourceBuilders;
import androidx.wear.tiles.TileBuilders;
import androidx.wear.tiles.TileProviderService;
import androidx.wear.tiles.TimelineBuilders;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.text.NumberFormat;
import java.util.concurrent.Callable;

public class ServiceTileWear extends TileProviderService {
    private static final String RESOURCES_VERSION = "1";

    private static final ListeningExecutorService executor =
            MoreExecutors.listeningDecorator(Helper.getBackgroundExecutor(1, "wear"));

    @NonNull
    @Override
    protected ListenableFuture<TileBuilders.Tile> onTileRequest(
            @NonNull RequestBuilders.TileRequest requestParams) {

        DB db = DB.getInstance(this);
        NumberFormat NF = NumberFormat.getInstance();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean unseen_ignored = prefs.getBoolean("unseen_ignored", false);

        return executor.submit(new Callable<TileBuilders.Tile>() {
            @Override
            public TileBuilders.Tile call() throws Exception {
                TupleMessageStats stats = db.message().getWidgetUnseen(null);

                Integer unseen = (stats == null ? null : (unseen_ignored ? stats.notifying : stats.unseen));

                LayoutElementBuilders.LayoutElement layout = LayoutElementBuilders.Row.builder()
                        .setWidth(wrap())
                        .setHeight(expand())
                        .setVerticalAlignment(LayoutElementBuilders.VERTICAL_ALIGN_CENTER)
                        .addContent(LayoutElementBuilders.Image.builder()
                                .setResourceId("mail")
                                .setWidth(dp(24f))
                                .setHeight(dp(24f)))
                        .addContent(LayoutElementBuilders.Spacer.builder()
                                .setWidth(dp(3f)))
                        .addContent(LayoutElementBuilders.Text.builder()
                                .setText(unseen == null ? "-" : NF.format(unseen)))
                        .build();

                return TileBuilders.Tile.builder()
                        .setResourcesVersion(RESOURCES_VERSION)
                        .setTimeline(TimelineBuilders.Timeline.builder()
                                .addTimelineEntry(TimelineBuilders.TimelineEntry.builder().setLayout(
                                        LayoutElementBuilders.Layout.builder().setRoot(layout))))
                        .build();
            }
        });
    }

    @NonNull
    @Override
    protected ListenableFuture<ResourceBuilders.Resources> onResourcesRequest(
            @NonNull RequestBuilders.ResourcesRequest requestParams) {
        return executor.submit(new Callable<ResourceBuilders.Resources>() {
            @Override
            public ResourceBuilders.Resources call() throws Exception {
                return ResourceBuilders.Resources.builder()
                        .setVersion(RESOURCES_VERSION)
                        .addIdToImageMapping("mail",
                                ResourceBuilders.ImageResource.builder()
                                        .setAndroidResourceByResId(ResourceBuilders.AndroidImageResourceByResId.builder()
                                                .setResourceId(R.drawable.baseline_mail_24)
                                                .build())
                                        .build())
                        .build();
            }
        });
    }

    public static void update(Context context) {
        TileProviderService.getUpdater(context).requestUpdate(ServiceTileWear.class);
    }
}
