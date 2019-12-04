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

import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(
        tableName = EntityCertificate.TABLE_NAME,
        foreignKeys = {
        },
        indices = {
                @Index(value = {"fingerprint", "email"}, unique = true),
                @Index(value = {"email"}),
        }
)
public class EntityCertificate {
    static final String TABLE_NAME = "certificate";

    @PrimaryKey(autoGenerate = true)
    public Long id;
    @NonNull
    public String fingerprint;
    @NonNull
    public String email;
    public String subject;
    @NonNull
    public String data;

    void setEncoded(byte[] encoded) {
        this.data = Base64.encodeToString(encoded, Base64.NO_WRAP);
    }

    byte[] getEncoded() {
        return Base64.decode(this.data, Base64.NO_WRAP);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EntityCertificate) {
            EntityCertificate other = (EntityCertificate) obj;
            return (this.fingerprint.equals(other.fingerprint) &&
                    Objects.equals(this.email, other.email) &&
                    Objects.equals(this.subject, other.subject) &&
                    this.data.equals(other.data));
        } else
            return false;
    }
}
