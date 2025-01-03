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

import android.text.TextUtils;

public class TupleThreadInfo {
    public String thread;
    public String msgid;
    public String hash;
    public String inreplyto;

    public boolean isSelf(String msgid) {
        return !TextUtils.isEmpty(this.msgid) && this.msgid.equals(msgid);
    }

    public boolean isReferenced(String msgid) {
        return !TextUtils.isEmpty(this.inreplyto) && this.inreplyto.equals(msgid);
    }

    public boolean isReferencing(String msgid) {
        return !TextUtils.isEmpty(msgid) && !isSelf(msgid) && !isReferenced(msgid);
    }
}
