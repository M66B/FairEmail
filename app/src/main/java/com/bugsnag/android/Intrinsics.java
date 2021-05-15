package com.bugsnag.android;

class Intrinsics {

    static boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }
}
