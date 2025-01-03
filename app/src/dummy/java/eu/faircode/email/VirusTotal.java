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
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.io.File;

public class VirusTotal {
    static final String URI_PRIVACY = "";

    static String getUrl(File file) {
        throw new IllegalArgumentException("VirusTotal");
    }

    static Bundle lookup(Context context, File file, String apiKey) {
        throw new IllegalArgumentException("VirusTotal");
    }

    static String upload(Context context, File file, String apiKey) {
        throw new IllegalArgumentException("VirusTotal");
    }

    static void waitForAnalysis(Context context, String id, String apiKey) {
        throw new IllegalArgumentException("VirusTotal");
    }

    public static class ScanResult implements Parcelable {
        public String name;
        public String category;

        ScanResult(String name, String category) {
            this.name = name;
            this.category = category;
        }

        protected ScanResult(Parcel in) {
            name = in.readString();
            category = in.readString();
        }

        @Override
        public void writeToParcel(@NonNull Parcel parcel, int i) {
            parcel.writeString(name);
            parcel.writeString(category);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<ScanResult> CREATOR = new Creator<ScanResult>() {
            @Override
            public ScanResult createFromParcel(Parcel in) {
                return new ScanResult(in);
            }

            @Override
            public ScanResult[] newArray(int size) {
                return new ScanResult[size];
            }
        };
    }
}
