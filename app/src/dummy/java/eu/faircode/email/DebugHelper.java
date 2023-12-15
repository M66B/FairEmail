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

    Copyright 2018-2023 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.os.Bundle;

import org.json.JSONException;

import java.io.IOException;

public class DebugHelper {
    static final String CRASH_LOG_NAME = "crash.log";

    static EntityMessage getDebugInfo(Context context, String source, int title, Throwable ex, String log, Bundle args) throws IOException, JSONException {
        return null;
    }

    static void writeCrashLog(Context context, Throwable ex) {
    }
}
