package org.json;

import eu.faircode.email.Helper;

public class JSONAddress extends JSONObject {

    public final JSONObject jsonObject;

    public JSONAddress(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    @Override
    public int hashCode() {
        return Helper.computeAddressHashcode(jsonObject);
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && this.hashCode() == obj.hashCode();
    }
}
