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

import org.json.JSONArray;

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

        if (FETCH.equals(name))
            try {
                JSONArray jargs = new JSONArray(args);
                long uid = jargs.getLong(0);
                key.id = "uid:" + uid;
            } catch (Throwable ex) {
                Log.e(ex);
            }
        else if (!MOVE.equals(name))
            key.id = "id:" + id;

        key.priority = this.priority;
        key.operation = this.name;

        if (offline)
            key.priority += 10;

        return key;
    }

    class PartitionKey {
        private String id;
        private int priority;
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
                return (Objects.equals(this.id, other.id) &&
                        this.priority == other.priority &&
                        Objects.equals(this.operation, other.operation));
            } else
                return false;
        }

        @NonNull
        @Override
        public String toString() {
            return (id == null ? "" : id) + ":" +
                    priority + ":" +
                    (operation == null ? "" : operation);
        }
    }
}
