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

    Copyright 2018-2020 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import androidx.preference.PreferenceManager;
import androidx.room.Ignore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.mail.Address;

public class TupleMessageEx extends EntityMessage {
    public Integer accountProtocol;
    public String accountName;
    public Integer accountColor;
    public boolean accountNotify;
    public boolean accountAutoSeen;
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
    public int signed;
    public int encrypted;
    public int visible;
    public Long totalSize;
    public Integer ui_priority;
    public Integer ui_importance;

    @Ignore
    boolean duplicate;

    @Ignore
    public Integer[] keyword_colors;

    String getFolderName(Context context) {
        return (folderDisplay == null
                ? Helper.localizeFolderName(context, folderName)
                : folderDisplay);
    }

    void resolveKeywordColors(Context context) {
        List<Integer> color = new ArrayList<>();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        for (int i = 0; i < this.keywords.length; i++) {
            String key = "keyword." + this.keywords[i];
            if (prefs.contains(key))
                color.add(prefs.getInt(key, Color.GRAY));
            else
                color.add(null);
        }

        this.keyword_colors = color.toArray(new Integer[0]);
    }


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TupleMessageEx) {
            TupleMessageEx other = (TupleMessageEx) obj;
            return (super.equals(obj) &&
                    this.accountProtocol.equals(other.accountProtocol) &&
                    Objects.equals(this.accountName, other.accountName) &&
                    Objects.equals(this.accountColor, other.accountColor) &&
                    this.accountNotify == other.accountNotify &&
                    this.accountAutoSeen == other.accountAutoSeen &&
                    this.folderName.equals(other.folderName) &&
                    Objects.equals(this.folderDisplay, other.folderDisplay) &&
                    this.folderType.equals(other.folderType) &&
                    this.folderReadOnly == other.folderReadOnly &&
                    Objects.equals(this.identityName, other.identityName) &&
                    Objects.equals(this.identityEmail, other.identityEmail) &&
                    Objects.equals(this.identitySynchronize, other.identitySynchronize) &&
                    MessageHelper.equal(this.senders, other.senders) &&
                    this.count == other.count &&
                    this.unseen == other.unseen &&
                    this.unflagged == other.unflagged &&
                    this.drafts == other.drafts &&
                    this.signed == other.signed &&
                    this.encrypted == other.encrypted &&
                    this.visible == other.visible &&
                    Objects.equals(this.totalSize, other.totalSize) &&
                    Objects.equals(this.ui_priority, other.ui_priority) &&
                    Objects.equals(this.ui_importance, other.ui_importance) &&
                    this.duplicate == other.duplicate);
        }
        return false;
    }
}
