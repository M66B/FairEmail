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

import android.annotation.TargetApi;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.N)
public class ServiceTileClear extends ServiceTileBase {
    public void onClick() {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
                startActivityAndCollapse(ActivityClear.getIntent(this));
            else
                startActivityAndCollapse(ActivityClear.getPendingIntent(this));
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }
}
