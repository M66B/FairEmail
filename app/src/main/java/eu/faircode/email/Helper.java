package eu.faircode.email;

/*
    This file is part of Safe email.

    Safe email is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    NetGuard is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with NetGuard.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2018 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.TypedValue;

public class Helper {
    static final String TAG = BuildConfig.APPLICATION_ID;

    static int resolveColor(Context context, int attr) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }

    static String localizeFolderName(Context context, String name) {
        if ("INBOX".equals(name))
            return context.getString(R.string.title_folder_inbox);
        else if ("OUTBOX".equals(name))
            return context.getString(R.string.title_folder_outbox);
        else
            return name;
    }

    static String localizeFolderName(Context context, TupleFolderEx folder) {
        if (TextUtils.isEmpty(folder.accountName))
            return localizeFolderName(context, folder.name);
        else
            return localizeFolderName(context, folder.name) + "/" + folder.accountName;
    }

    static String formatThrowable(Throwable ex) {
        StringBuilder sb = new StringBuilder();
        sb.append(ex.getMessage());
        Throwable cause = ex.getCause();
        while (cause != null) {
            sb.append(" ").append(cause.getMessage());
            cause = cause.getCause();
        }
        return sb.toString();
    }
}
