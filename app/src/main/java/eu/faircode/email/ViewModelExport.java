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

import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

public class ViewModelExport extends ViewModel {
    private SavedStateHandle state;

    public ViewModelExport(SavedStateHandle state) {
        this.state = state;
    }

    public void setPassword(String password) {
        state.set("password", password);
    }

    public void setOptions(String name, boolean value) {
        state.set(name, value);
    }

    public String getPassword() {
        return state.get("password");
    }

    public boolean getOption(String name) {
        Boolean value = state.get(name);
        return (value != null && value);
    }
}
