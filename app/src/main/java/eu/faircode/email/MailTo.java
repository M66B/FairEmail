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

    Copyright 2018-2022 by Marcel Bokhorst (M66B)
*/

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.net.ParseException;

import java.util.Map;

public class MailTo {
    private final androidx.core.net.MailTo instance;

    // https://en.wikipedia.org/wiki/Mailto
    private MailTo(androidx.core.net.MailTo instance) {
        this.instance = instance;
    }

    @NonNull
    public static MailTo parse(@NonNull String uri) throws ParseException {
        // Workaround invalid percent encoding
        // https://en.wikipedia.org/wiki/Percent-encoding
        int i = 0;
        int pos = uri.indexOf('%', i);
        while (pos >= 0 && i + 3 < uri.length()) {
            try {
                if (pos + 3 > uri.length())
                    break;
                String hex = uri.substring(pos + 1, pos + 3);
                int kar = Integer.parseInt(hex, 16);
                if (kar > 127)
                    throw new NumberFormatException("Non ASCII: %" + hex);
            } catch (NumberFormatException ex) {
                uri = uri.substring(0, pos + 1) + "25" + uri.substring(pos + 1);
            } catch (Throwable ex) {
                Log.e(ex);
                break;
            }
            i = pos + 3;
            pos = uri.indexOf('%', i);
        }

        return new MailTo(androidx.core.net.MailTo.parse(uri));
    }

    @NonNull
    public static MailTo parse(@NonNull Uri uri) throws ParseException {
        return parse(uri.toString());
    }

    String getTo() {
        return instance.getTo();
    }

    String getCc() {
        return instance.getCc();
    }

    String getSubject() {
        return instance.getSubject();
    }

    Map<String, String> getHeaders() {
        return instance.getHeaders();
    }

    String getBody() {
        return instance.getBody();
    }
}
