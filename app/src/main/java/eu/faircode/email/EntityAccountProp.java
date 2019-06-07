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

import androidx.annotation.NonNull;
import androidx.room.DatabaseView;

@DatabaseView(
        viewName = "accountprop",
        value = "SELECT id, name, color, synchronize, `primary`, notify, browse, swipe_left, swipe_right, created, `order` FROM account")
public class EntityAccountProp {
    public Long id;
    public String name;
    public Integer color;
    @NonNull
    public Boolean synchronize;
    @NonNull
    public Boolean primary;
    @NonNull
    public Boolean notify;
    @NonNull
    public Boolean browse = true;
    public Long swipe_left;
    public Long swipe_right;
    public Long created;
    public Integer order;
}
