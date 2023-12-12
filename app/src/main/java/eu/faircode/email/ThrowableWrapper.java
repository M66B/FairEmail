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

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ThrowableWrapper extends Throwable {
    private final Throwable ex;
    private final String msg;

    ThrowableWrapper(String msg) {
        this.ex = new Throwable();
        this.msg = msg;
    }

    ThrowableWrapper(Throwable ex) {
        this.ex = ex;
        this.msg = null;
    }

    public String getSafeMessage() {
        return (TextUtils.isEmpty(msg) ? super.getMessage() : msg);
    }

    public String getSafeMessageOrName() {
        String msg = getSafeMessage();
        return (msg == null ? ex.getClass().getName() : msg);
    }

    public String getStackTraceString() {
        return android.util.Log.getStackTraceString(ex);
    }

    public String toSafeString() {
        return ex.toString();
    }

    @Nullable
    @Override
    public String getMessage() {
        return ex.getMessage();
    }

    @Nullable
    @Override
    public String getLocalizedMessage() {
        return getMessage();
    }

    @NonNull
    @Override
    public StackTraceElement[] getStackTrace() {
        return ex.getStackTrace();
    }

    @Nullable
    @Override
    public synchronized Throwable getCause() {
        return ex.getCause();
    }

    @NonNull
    @Override
    public String toString() {
        return ex.toString();
    }
}
