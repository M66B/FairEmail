package eu.faircode.email;

public class TupleContactEx extends EntityContact {
    public String accountName;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TupleContactEx) {
            TupleContactEx other = (TupleContactEx) obj;
            return (super.equals(obj) &&
                    accountName.equals(other.accountName));
        } else
            return false;
    }
}