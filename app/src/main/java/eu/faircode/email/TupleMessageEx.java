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

import androidx.room.Ignore;
import org.json.JSONAddress;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Objects;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.InternetAddressImpl;

public class TupleMessageEx extends EntityMessage {
    public String accountName;
    public Integer accountColor;
    public boolean accountNotify;
    public String folderName;
    public String folderDisplay;
    public String folderType;
    public boolean folderReadOnly;
    public String identityName;
    public String identityEmail;
    public Boolean identitySynchronize;
    public Address[] senders;
    public int count;
    public int unseen;
    public int unflagged;
    public int drafts;
    public int visible;
    public Long totalSize;

    @Ignore
    public boolean duplicate;

    @Ignore
    public boolean calculatedVia = false;
    @Ignore
    public Address via = null;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TupleMessageEx) {
            TupleMessageEx other = (TupleMessageEx) obj;
            return (super.equals(obj) &&
                    Objects.equals(this.accountName, other.accountName) &&
                    Objects.equals(this.accountColor, other.accountColor) &&
                    this.accountNotify == other.accountNotify &&
                    this.folderName.equals(other.folderName) &&
                    Objects.equals(this.folderDisplay, other.folderDisplay) &&
                    this.folderType.equals(other.folderType) &&
                    this.folderReadOnly == other.folderReadOnly &&
                    Objects.equals(this.identityName, other.identityName) &&
                    Objects.equals(this.identityEmail, other.identityEmail) &&
                    Objects.equals(this.identitySynchronize, other.identitySynchronize) &&
                    this.count == other.count &&
                    this.unseen == other.unseen &&
                    this.unflagged == other.unflagged &&
                    this.drafts == other.drafts &&
                    this.visible == other.visible &&
                    Objects.equals(this.totalSize, other.totalSize) &&
                    this.duplicate == other.duplicate);
        }
        return false;
    }

    public Address getVia() {
        if (!calculatedVia && identityEmail != null) {
            JSONObject jaddress = new JSONObject();
            JSONAddress key = null;
            try {
                jaddress.put("address", identityEmail);
                jaddress.put("personal", identityName);
                key = new JSONAddress(jaddress);
                via = DB.addressCache.get(key);
            } catch (JSONException e) {
            }
            if (via == null) {
                try {
                    via = new InternetAddressImpl(identityEmail, identityName);
                    if (key != null) {
                        synchronized (DB.addressCache) {
                            DB.inverseAddressCache.put(via, key);
                            DB.addressCache.put(key, via);
                        }
                    }
                } catch (UnsupportedEncodingException ignored) {
                }
            }
            calculatedVia = true;
        }
        return via;
    }
}
