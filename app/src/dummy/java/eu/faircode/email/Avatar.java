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

import android.content.Context;

import java.util.concurrent.Callable;

public class Avatar {
    static final String GRAVATAR_PRIVACY_URI = "";
    static final String LIBRAVATAR_PRIVACY_URI = "";
    static final String DDG_URI = "";
    static final String DDG_PRIVACY_URI = "";

    static Callable<ContactInfo.Favicon> getGravatar(String email, int scaleToPixels, Context context) {
        return new Callable<ContactInfo.Favicon>() {
            @Override
            public ContactInfo.Favicon call() throws Exception {
                throw new IllegalArgumentException("Gravatar");
            }
        };
    }

    static Callable<ContactInfo.Favicon> getLibravatar(String email, int scaleToPixels, Context context) {
        return new Callable<ContactInfo.Favicon>() {
            @Override
            public ContactInfo.Favicon call() throws Exception {
                throw new IllegalArgumentException("Libravatar");
            }
        };
    }
}
