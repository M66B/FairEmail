#include <jni.h>
#include <android/log.h>
#include <cstdio>

#include <errno.h>
#include <sys/socket.h>
#include <netinet/tcp.h>

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

extern "C" JNIEXPORT jobject JNICALL
Java_eu_faircode_email_CharsetHelper_jni_1detect(JNIEnv *env, jclass type, jbyteArray _octets) {
    int len = env->GetArrayLength(_octets);
    jbyte *octets = env->GetByteArrayElements(_octets, nullptr);

    // https://github.com/google/compact_enc_det

    bool is_reliable;
    int bytes_consumed;

    Encoding encoding = CompactEncDet::DetectEncoding(
            (const char *) octets, len,
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
    env->ReleaseByteArrayElements(_octets, octets, JNI_ABORT);

    jclass cls = env->FindClass("eu/faircode/email/CharsetHelper$DetectResult");
    jmethodID ctor = env->GetMethodID(cls, "<init>", "(Ljava/lang/String;IIZ)V");
    jstring jname = env->NewStringUTF(name);
    return env->NewObject(
            cls, ctor,
            jname,
            (jint) len,
            (jint) bytes_consumed,
            (jboolean) is_reliable);
}

extern "C"
JNIEXPORT jint JNICALL
Java_eu_faircode_email_EmailService_jni_1socket_1keep_1alive(
        JNIEnv *env, jclass clazz,
        jint fd, jint seconds) {
    // https://linux.die.net/man/2/setsockopt
    // https://linux.die.net/man/3/setsockopt
    // https://tldp.org/HOWTO/html_single/TCP-Keepalive-HOWTO/#setsockopt
    int value = seconds;

    int res = setsockopt(fd, SOL_TCP, TCP_KEEPIDLE, (void *) &value, sizeof(value));
    if (res < 0)
        res = errno;

    return res;
}
