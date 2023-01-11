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

    Copyright 2018-2023 by Marcel Bokhorst (M66B)
*/

import androidx.lifecycle.ViewModel;

import java.util.HashMap;
import java.util.Map;

public class ViewModelExport extends ViewModel {
    private String password = null;
    private final Map<String, Boolean> options = new HashMap<>();

    public void setPassword(String password) {
        this.password = password;
    }

    public void setOptions(String name, boolean value) {
        options.put(name, value);
    }

    public String getPassword() {
        return password;
    }

    public boolean getOption(String name) {
        Boolean value = options.get(name);
        return (value != null && value);
    }
}
