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
import android.content.SharedPreferences;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.preference.PreferenceManager;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

@TargetApi(Build.VERSION_CODES.N)
public class ServiceTileSynchronize extends TileService implements SharedPreferences.OnSharedPreferenceChangeListener {
    public void onStartListening() {
        Log.i("Start tile synchronize");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
        update();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if ("enabled".equals(key))
            update();
    }

    private void update() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean enabled = prefs.getBoolean("enabled", true);
        Log.i("Update tile synchronize=" + enabled);

        Tile tile = getQsTile();
        if (tile != null) {
            tile.setState(enabled ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
            tile.setIcon(Icon.createWithResource(this,
                    enabled ? R.drawable.baseline_sync_24 : R.drawable.baseline_sync_disabled_24));
            tile.updateTile();
        }
    }

    public void onStopListening() {
        Log.i("Stop tile synchronize");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onClick() {
        Log.i("Click tile synchronize");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean enabled = !prefs.getBoolean("enabled", true);
        prefs.edit().putBoolean("enabled", enabled).apply();
        ServiceSynchronize.reload(this, "tile=" + enabled);
    }
}
