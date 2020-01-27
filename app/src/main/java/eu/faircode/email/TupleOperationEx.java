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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

public class TupleOperationEx extends EntityOperation {
    public int priority;
    public String accountName;
    public String folderName;
    public boolean synchronize;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TupleOperationEx) {
            TupleOperationEx other = (TupleOperationEx) obj;
            return (super.equals(obj) &&
                    this.priority == other.priority &&
                    Objects.equals(this.accountName, other.accountName) &&
                    Objects.equals(this.folderName, other.folderName) &&
                    this.synchronize == other.synchronize);
        } else
            return false;
    }

    PartitionKey getPartitionKey(boolean offline) {
        PartitionKey key = new PartitionKey();
        if (offline)
            key.priority = this.priority + 10;
        else {
            key.id = (MOVE.equals(name) || FETCH.equals(name) ? 0 : this.id);
            key.priority = this.priority;
            key.operation = this.name;
        }
        return key;
    }

    class PartitionKey {
        private long id;
        private int priority;
        @NonNull
        private String operation;

        int getPriority() {
            return this.priority;
        }

        @Override
        public int hashCode() {
            return toString().hashCode();
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj instanceof PartitionKey) {
                PartitionKey other = (PartitionKey) obj;
                return (this.id == other.id &&
                        this.priority == other.priority &&
                        Objects.equals(this.operation, other.operation));
            } else
                return false;
        }

        @NonNull
        @Override
        public String toString() {
            return operation + ":" + priority + ":" + id;
        }
    }
}
