#include <jni.h>
#include <android/log.h>
#include <cstdio>
#include "compact_enc_det/compact_enc_det.h"

void log_android(int prio, const char *fmt, ...) {
    if (prio >= ANDROID_LOG_DEBUG) {
        char line[1024];
        va_list argptr;
        va_start(argptr, fmt);
        vsprintf(line, fmt, argptr);
        __android_log_print(prio, "fairemail.jni", "%s", line);
        va_end(argptr);
    }
}

extern "C" JNIEXPORT jstring JNICALL
Java_eu_faircode_email_CharsetHelper_jni_1detect(JNIEnv *env, jclass type, jbyteArray _bytes) {
    int len = env->GetArrayLength(_bytes);
    jbyte *bytes = env->GetByteArrayElements(_bytes, nullptr);

    // https://github.com/google/compact_enc_det

    bool is_reliable;
    int bytes_consumed;

    Encoding encoding = CompactEncDet::DetectEncoding(
            (const char *) bytes, len,
            nullptr, nullptr, nullptr,
            UNKNOWN_ENCODING,
            UNKNOWN_LANGUAGE,
            CompactEncDet::EMAIL_CORPUS,
            false,
            &bytes_consumed,
            &is_reliable);
    const char *name = MimeEncodingName(encoding);

    log_android(ANDROID_LOG_DEBUG, "detect=%d/%s bytes=%d reliable=%d",
                encoding, name, bytes_consumed, is_reliable);

    // https://developer.android.com/training/articles/perf-jni#primitive-arrays
    env->ReleaseByteArrayElements(_bytes, bytes, JNI_ABORT);

    return env->NewStringUTF(name);
}
