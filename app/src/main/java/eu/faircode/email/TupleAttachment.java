package eu.faircode.email;

import android.support.annotation.NonNull;

public class TupleAttachment {
    @NonNull
    public Long id;
    @NonNull
    public Long message;
    @NonNull
    public Integer sequence;
    public String name;
    @NonNull
    public String type;
    public Integer size;
    public Integer progress;
    @NonNull
    public boolean content;
}
