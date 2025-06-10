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

import android.content.ClipDescription;
import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class UriType implements Parcelable {
    private Uri uri;
    private String type;

    protected UriType(Parcel in) {
        this.uri = in.readParcelable(Uri.class.getClassLoader());
        this.type = in.readString();
    }

    public UriType(Uri uri, ClipDescription description, Context context) {
        this(uri, description == null || description.getMimeTypeCount() <= 0 ? null : description.getMimeType(0), context);
    }

    public UriType(Uri uri, String type, Context context) {
        this.uri = uri;
        if (!TextUtils.isEmpty(type))
            this.type = type;

        //if (context != null) {
        //    Helper.UriInfo info = Helper.getInfo(this, context);
        //    this.type = EntityAttachment.getMimeType(type, info.name);
        //}
    }

    public Uri getUri() {
        return this.uri;
    }

    public String getType() {
        return this.type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int flags) {
        parcel.writeParcelable(this.uri, flags);
        parcel.writeString(this.type);
    }

    public static final Creator<UriType> CREATOR = new Creator<UriType>() {
        @Override
        public UriType createFromParcel(Parcel in) {
            return new UriType(in);
        }

        @Override
        public UriType[] newArray(int size) {
            return new UriType[size];
        }
    };

    public static List<UriType> getList(List<Uri> uris, Context context) {
        List<UriType> result = new ArrayList<>();
        if (uris != null)
            for (Uri uri : uris)
                result.add(new UriType(uri, (String) null, context));
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        return uri + " type=" + type;
    }
}
