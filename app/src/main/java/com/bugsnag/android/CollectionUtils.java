package com.bugsnag.android;

import androidx.annotation.Nullable;

import java.util.Collection;

class CollectionUtils {
    static <T> boolean containsNullElements(@Nullable Collection<T> data) {
        if (data == null) {
            return true;
        }
        for (T datum : data) {
            if (datum == null) {
                return true;
            }
        }
        return false;
    }
}
