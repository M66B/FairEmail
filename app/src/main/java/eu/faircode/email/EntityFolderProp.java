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
        viewName = "folderprop",
        value = "SELECT id, account, name, type, download, display, unified, notify FROM folder")
public class EntityFolderProp {
    public Long id;
    public Long account;
    @NonNull
    public String name;
    @NonNull
    public String type;
    @NonNull
    public Boolean download = true;
    public String display;
    @NonNull
    public Boolean unified = false;
    @NonNull
    public Boolean notify = false;
}
