package com.bugsnag.android;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

class StrictModeHandler {

    // Byte 1: Thread-policy (needs to be synced with StrictMode constants)
    private static final int DETECT_DISK_WRITE = 0x01;
    private static final int DETECT_DISK_READ = 0x02;
    private static final int DETECT_NETWORK = 0x04;
    private static final int DETECT_CUSTOM = 0x08;
    private static final int DETECT_RESOURCE_MISMATCH = 0x10;

    // Byte 2: Process-policy (needs to be synced with StrictMode constants)
    private static final int DETECT_VM_CURSOR_LEAKS = 0x01 << 8;
    private static final int DETECT_VM_CLOSABLE_LEAKS = 0x02 << 8;
    private static final int DETECT_VM_ACTIVITY_LEAKS = 0x04 << 8;
    private static final int DETECT_VM_INSTANCE_LEAKS = 0x08 << 8;
    private static final int DETECT_VM_REGISTRATION_LEAKS = 0x10 << 8;
    private static final int DETECT_VM_FILE_URI_EXPOSURE = 0x20 << 8;
    private static final int DETECT_VM_CLEARTEXT_NETWORK = 0x40 << 8;


    private static final String STRICT_MODE_CLZ_NAME = "android.os.strictmode";

    @SuppressLint("UseSparseArrays")
    private static final Map<Integer, String> POLICY_CODE_MAP = new HashMap<>();

    static {
        POLICY_CODE_MAP.put(DETECT_DISK_WRITE, "DiskWrite");
        POLICY_CODE_MAP.put(DETECT_DISK_READ, "DiskRead");
        POLICY_CODE_MAP.put(DETECT_NETWORK, "NetworkOperation");
        POLICY_CODE_MAP.put(DETECT_CUSTOM, "CustomSlowCall");
        POLICY_CODE_MAP.put(DETECT_RESOURCE_MISMATCH, "ResourceMismatch");

        POLICY_CODE_MAP.put(DETECT_VM_CURSOR_LEAKS, "CursorLeak");
        POLICY_CODE_MAP.put(DETECT_VM_CLOSABLE_LEAKS, "CloseableLeak");
        POLICY_CODE_MAP.put(DETECT_VM_ACTIVITY_LEAKS, "ActivityLeak");
        POLICY_CODE_MAP.put(DETECT_VM_INSTANCE_LEAKS, "InstanceLeak");
        POLICY_CODE_MAP.put(DETECT_VM_REGISTRATION_LEAKS, "RegistrationLeak");
        POLICY_CODE_MAP.put(DETECT_VM_FILE_URI_EXPOSURE, "FileUriLeak");
        POLICY_CODE_MAP.put(DETECT_VM_CLEARTEXT_NETWORK, "CleartextNetwork");
    }

    /**
     * Checks whether a throwable was originally thrown from the StrictMode class
     *
     * @param throwable the throwable
     * @return true if the throwable's root cause is a StrictMode policy violation
     */
    boolean isStrictModeThrowable(Throwable throwable) {
        Throwable cause = getRootCause(throwable);
        Class<? extends Throwable> causeClass = cause.getClass();
        String simpleName = causeClass.getName();
        return simpleName.toLowerCase(Locale.US).startsWith(STRICT_MODE_CLZ_NAME);
    }

    @Nullable
    String getViolationDescription(String exceptionMessage) {
        if (TextUtils.isEmpty(exceptionMessage)) {
            throw new IllegalArgumentException();
        }
        int indexOf = exceptionMessage.lastIndexOf("violation=");

        if (indexOf != -1) {
            String substring = exceptionMessage.substring(indexOf);
            substring = substring.replace("violation=", "");

            if (TextUtils.isDigitsOnly(substring)) {
                Integer code = Integer.valueOf(substring);
                return POLICY_CODE_MAP.get(code);
            }
        }
        return null;
    }

    /**
     * Recurse the stack to get the original cause of the throwable
     *
     * @param throwable the throwable
     * @return the root cause of the throwable
     */
    private Throwable getRootCause(Throwable throwable) {
        List<Throwable> causes = ThrowableUtils.safeUnrollCauses(throwable);
        return causes.get(causes.size() - 1);
    }
}
