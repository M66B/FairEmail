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

import androidx.documentfile.provider.DocumentFile;

import java.io.InputStream;

public class Send {
    static final int DEFAULT_DLIMIT = 0;
    static final int DEFAULT_TLIMIT = 0;
    static final String DEFAULT_SERVER = "";

    public static String upload(InputStream is, DocumentFile dfile, int dLimit, int timeLimit, String host, IProgress intf) {
        throw new IllegalArgumentException("Send");
    }

    public interface IProgress {
        void onProgress(int percentage);

        boolean isRunning();
    }
}
