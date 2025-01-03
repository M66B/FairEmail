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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;

import java.util.Objects;

public class TupleOperationEx extends EntityOperation {
    public int priority;
    public String accountName;
    public String folderName;
    public String folderType;
    public boolean synchronize;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TupleOperationEx) {
            TupleOperationEx other = (TupleOperationEx) obj;
            return (super.equals(obj) &&
                    this.priority == other.priority &&
                    Objects.equals(this.accountName, other.accountName) &&
                    Objects.equals(this.folderName, other.folderName) &&
                    Objects.equals(this.folderType, other.folderType) &&
                    this.synchronize == other.synchronize);
        } else
            return false;
    }

    int getPriority(boolean offline) {
        int priority = this.priority;

        if (offline)
            priority += 20; // connect folder is expensive

        if (EntityFolder.USER.equals(folderType)) // prioritize system folders
            priority += 100;
        else if (!EntityFolder.INBOX.equals(folderType)) // prioritize inbox
            priority += 50;

        return priority;
    }

    PartitionKey getPartitionKey(boolean offline) {
        PartitionKey key = new PartitionKey();

        key.folder = this.folder;
        key.order = this.id;
        key.priority = this.getPriority(offline);

        if (offline)
            return key;

        if (ADD.equals(name) ||
                DELETE.equals(name))
            key.id = "msg:" + message;
        else if (SEEN.equals(name) ||
                ANSWERED.equals(name) ||
                FLAG.equals(name) ||
                KEYWORD.equals(name) ||
                LABEL.equals(name))
            key.id = "flags:" + folder;
        else if (FETCH.equals(name))
            try {
                JSONArray jargs = new JSONArray(args);
                long uid = jargs.getLong(0);
                key.id = "uid:" + uid;
            } catch (Throwable ex) {
                Log.e(ex);
            }
        else if (!MOVE.equals(name))
            key.id = "id:" + id;

        key.operation = this.name;

        return key;
    }

    class PartitionKey {
        private long folder;
        private long order;
        private int priority;
        private String id;
        private String operation;

        long getOrder() {
            return this.order;
        }

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
                return (this.folder == other.folder &&
                        this.priority == other.priority &&
                        Objects.equals(this.id, other.id) &&
                        Objects.equals(this.operation, other.operation));
            } else
                return false;
        }

        @NonNull
        @Override
        public String toString() {
            return (priority + ":" +
                    (id == null ? "" : id) + ":" +
                    (operation == null ? "" : operation));
        }
    }
}
