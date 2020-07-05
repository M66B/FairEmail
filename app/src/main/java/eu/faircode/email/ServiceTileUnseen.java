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

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.IBinder;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import androidx.lifecycle.Observer;

import java.util.ArrayList;
import java.util.List;

@TargetApi(Build.VERSION_CODES.N)
public class ServiceTileUnseen extends TileService {
    private TwoStateOwner owner = new TwoStateOwner("ServiceTileUnseen");

    @Override
    public void onCreate() {
        super.onCreate();

        DB db = DB.getInstance(this);

        db.message().liveUnseenNotify().observe(owner, new Observer<List<TupleMessageEx>>() {
            @Override
            public void onChanged(List<TupleMessageEx> messages) {
                if (messages == null)
                    messages = new ArrayList<>();

                int unseen = 0;
                for (TupleMessageEx message : messages)
                    if (!message.ui_seen && !message.ui_ignored && !message.ui_hide)
                        unseen++;

                Log.i("Update tile unseen=" + unseen);

                Tile tile = getQsTile();
                if (tile != null)
                    try {
                        tile.setState(unseen > 0 ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
                        tile.setIcon(Icon.createWithResource(ServiceTileUnseen.this,
                                unseen > 0 ? R.drawable.baseline_mail_24 : R.drawable.baseline_mail_outline_24));
                        tile.setLabel(getResources().getQuantityString(
                                R.plurals.title_tile_unseen, unseen, unseen));
                        tile.updateTile();
                    } catch (Throwable ex) {
                        Log.w(ex);
                        // See ServiceTileSynchronize
                    }
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        owner.destroy();
        super.onDestroy();
    }

    public void onStartListening() {
        Log.i("Start tile unseen");
        owner.start();
    }

    public void onStopListening() {
        Log.i("Stop tile unseen");
        owner.stop();
    }
}
