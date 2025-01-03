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

import java.util.Objects;

public class TupleRuleEx extends EntityRule {
    public long account;
    public String folderName;
    public String accountName;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TupleRuleEx) {
            TupleRuleEx other = (TupleRuleEx) obj;
            return (super.equals(obj) &&
                    this.account == other.account &&
                    Objects.equals(this.folderName, other.folderName) &&
                    Objects.equals(this.accountName, other.accountName));
        } else
            return false;
    }
}
