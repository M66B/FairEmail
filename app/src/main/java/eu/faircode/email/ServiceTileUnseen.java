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

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.IBinder;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import java.util.List;

import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

@TargetApi(Build.VERSION_CODES.N)
public class ServiceTileUnseen extends TileService {
    private LifecycleService owner = new LifecycleService();
    private LiveData<List<TupleMessageEx>> liveMessages;

    @Override
    public void onCreate() {
        owner.onCreate();
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        owner.onBind(intent);
        return super.onBind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        owner.onStartCommand(intent, flags, startId);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        owner.onDestroy();
        super.onDestroy();
    }

    public void onStartListening() {
        Log.i("Start tile unseen");
        liveMessages = DB.getInstance(this).message().liveUnseenNotify();
        liveMessages.observe(owner, new Observer<List<TupleMessageEx>>() {
            @Override
            public void onChanged(List<TupleMessageEx> messages) {
                Log.i("Update tile unseen=" + messages.size());

                Tile tile = getQsTile();
                if (tile != null) {
                    tile.setState(messages.size() > 0 ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
                    tile.setIcon(Icon.createWithResource(ServiceTileUnseen.this,
                            messages.size() > 0 ? R.drawable.baseline_mail_24 : R.drawable.baseline_mail_outline_24));
                    tile.setLabel(getResources().getQuantityString(
                            R.plurals.title_tile_unseen, messages.size(), messages.size()));
                    tile.updateTile();
                }
            }
        });
    }

    public void onStopListening() {
        Log.i("Stop tile unseen");
        if (liveMessages != null) {
            liveMessages.removeObservers(owner);
            liveMessages = null;
        }
    }

    public void onClick() {
        Log.i("Click tile unseen");

        Intent clear = new Intent(this, ServiceSynchronize.class);
        clear.setAction("clear");
        startService(clear);
    }
}
