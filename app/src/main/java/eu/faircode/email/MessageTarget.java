package eu.faircode.email;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

class MessageTarget implements Serializable {
    List<Long> ids = new ArrayList<>();
    EntityFolder target;
}