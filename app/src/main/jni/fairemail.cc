#include <jni.h>
#include <android/log.h>

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

#include <cstdio>

#include <errno.h>
#include <sys/socket.h>
#include <netinet/tcp.h>
#include <sys/ioctl.h>
#include <netdb.h>
#include <unistd.h>

#include "compact_enc_det/compact_enc_det.h"
#include "cld_3/src/nnet_language_identifier.h"

int log_level = ANDROID_LOG_DEBUG;

extern "C"
JNIEXPORT void JNICALL
Java_eu_faircode_email_Log_jni_1set_1log_1level(JNIEnv *env, jclass clazz, jint level) {
    log_level = level;
}

void log_android(int prio, const char *fmt, ...) {
    if (prio >= log_level) {
        char line[1024];
        va_list argptr;
        va_start(argptr, fmt);
        vsprintf(line, fmt, argptr);
        __android_log_print(prio, "fairemail.jni", "%s", line);
        va_end(argptr);
    }
}

extern "C"
JNIEXPORT jstring JNICALL
Java_eu_faircode_email_ThrowableWrapper_jni_1get_1safe_1message(
        JNIEnv *env, jclass clazz, jthrowable ex) {
    jclass cls = env->FindClass("java/lang/Throwable");
    jmethodID mid = env->GetMethodID(cls, "getMessage", "()Ljava/lang/String;");
    return (jstring) env->CallObjectMethod(ex, mid);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_eu_faircode_email_ThrowableWrapper_jni_1get_1safe_1stack_1trace_1string(
        JNIEnv *env, jclass clazz, jthrowable ex) {
    jclass cls = env->FindClass("android/util/Log");
    jmethodID mid = env->GetStaticMethodID(cls, "getStackTraceString",
                                           "(Ljava/lang/Throwable;)Ljava/lang/String;");
    return (jstring) env->CallStaticObjectMethod(cls, mid, ex);
}

extern "C"
JNIEXPORT jlongArray JNICALL
Java_eu_faircode_email_Log_jni_1safe_1runtime_1stats(JNIEnv *env, jclass clazz) {
    jclass clsRuntime = env->FindClass("java/lang/Runtime");
    jmethodID mid = env->GetStaticMethodID(clsRuntime, "getRuntime", "()Ljava/lang/Runtime;");
    jobject jruntime = env->CallStaticObjectMethod(clsRuntime, mid);

    jmethodID midTotalMemory = env->GetMethodID(clsRuntime, "totalMemory", "()J");
    jlong totalMemory = env->CallLongMethod(jruntime, midTotalMemory);

    jmethodID midFreeMemory = env->GetMethodID(clsRuntime, "freeMemory", "()J");
    jlong freeMemory = env->CallLongMethod(jruntime, midFreeMemory);

    jmethodID midMaxMemory = env->GetMethodID(clsRuntime, "maxMemory", "()J");
    jlong maxMemory = env->CallLongMethod(jruntime, midMaxMemory);

    jmethodID midAvailableProcessors = env->GetMethodID(clsRuntime, "availableProcessors", "()I");
    jlong availableProcessors = env->CallIntMethod(jruntime, midAvailableProcessors);

    jclass clsDebug = env->FindClass("android/os/Debug");
    jmethodID midGetNativeHeapAllocatedSize = env->GetStaticMethodID(clsDebug, "getNativeHeapAllocatedSize", "()J");
    jlong getNativeHeapAllocatedSize = env->CallStaticLongMethod(clsDebug, midGetNativeHeapAllocatedSize);

    jlongArray result = env->NewLongArray(5);
    if (result == NULL)
        return NULL; /* out of memory error thrown */

    env->SetLongArrayRegion(result, 0, 1, &totalMemory);
    env->SetLongArrayRegion(result, 1, 1, &freeMemory);
    env->SetLongArrayRegion(result, 2, 1, &maxMemory);
    env->SetLongArrayRegion(result, 3, 1, &availableProcessors);
    env->SetLongArrayRegion(result, 4, 1, &getNativeHeapAllocatedSize);

    return result;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_eu_faircode_email_CharsetHelper_jni_1detect_1charset(
        JNIEnv *env, jclass type,
        jbyteArray _octets, jstring _ref, jstring _lang) {
    int len = env->GetArrayLength(_octets);
    jbyte *octets = env->GetByteArrayElements(_octets, nullptr);
    const char *ref = env->GetStringUTFChars(_ref, 0);
    const char *lang = env->GetStringUTFChars(_lang, 0);

    // ISO-8859-1 is unknown
    Encoding encoding_hint;
    EncodingFromName(ref, &encoding_hint);

    Language language_hint;
    LanguageFromCode(lang, &language_hint);

    // https://github.com/google/compact_enc_det

    bool is_reliable;
    int bytes_consumed;

    Encoding encoding = CompactEncDet::DetectEncoding(
            (const char *) octets, len,
            nullptr, nullptr, nullptr,
            encoding_hint,
            language_hint,
            CompactEncDet::EMAIL_CORPUS,
            false,
            &bytes_consumed,
            &is_reliable);
    // TODO: PreferredWebOutputEncoding?
    const char *name = MimeEncodingName(encoding);

    log_android(ANDROID_LOG_DEBUG,
                "detect=%d/%s bytes=%d reliable=%d"
                " ref=%s/%s lang=%s/%s",
                encoding, name, bytes_consumed, is_reliable,
                EncodingName(encoding_hint), ref, LanguageCode(language_hint), lang);

    // https://developer.android.com/training/articles/perf-jni#primitive-arrays
    env->ReleaseByteArrayElements(_octets, octets, JNI_ABORT);
    env->ReleaseStringUTFChars(_ref, ref);
    env->ReleaseStringUTFChars(_lang, lang);

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
JNIEXPORT jobject JNICALL
Java_eu_faircode_email_TextHelper_jni_1detect_1language(
        JNIEnv *env, jclass clazz,
        jbyteArray _octets) {
    int len = env->GetArrayLength(_octets);
    jbyte *octets = env->GetByteArrayElements(_octets, nullptr);

    std::string text(reinterpret_cast<char const *>(octets), len);

    chrome_lang_id::NNetLanguageIdentifier lang_id(0, 1000);
    const chrome_lang_id::NNetLanguageIdentifier::Result result = lang_id.FindLanguage(text);

    env->ReleaseByteArrayElements(_octets, octets, JNI_ABORT);

    jclass cls = env->FindClass("eu/faircode/email/TextHelper$DetectResult");
    jmethodID ctor = env->GetMethodID(cls, "<init>", "(Ljava/lang/String;FZF)V");
    jstring jlanguage = env->NewStringUTF(result.language.c_str());
    return env->NewObject(
            cls, ctor,
            jlanguage,
            (jfloat) result.probability,
            (jint) result.is_reliable,
            (jfloat) result.proportion);
}

extern "C"
JNIEXPORT jint JNICALL
Java_eu_faircode_email_ConnectionHelper_jni_1socket_1keep_1alive(
        JNIEnv *env, jclass clazz,
        jint fd, jint seconds) {
    // https://tldp.org/HOWTO/html_single/TCP-Keepalive-HOWTO/#setsockopt

    int optval;
    socklen_t optlen = sizeof(optval);

    if (getsockopt(fd, SOL_TCP, TCP_KEEPCNT, &optval, &optlen) == 0)
        log_android(ANDROID_LOG_DEBUG, "Default TCP_KEEPCNT=%d", optval);
    if (getsockopt(fd, SOL_TCP, TCP_KEEPINTVL, &optval, &optlen) == 0)
        log_android(ANDROID_LOG_DEBUG, "Default TCP_KEEPINTVL=%d", optval);
    if (getsockopt(fd, SOL_TCP, TCP_KEEPIDLE, &optval, &optlen) == 0)
        log_android(ANDROID_LOG_DEBUG, "Default TCP_KEEPIDLE=%d", optval);
    if (getsockopt(fd, SOL_SOCKET, SO_KEEPALIVE, &optval, &optlen) == 0)
        log_android(ANDROID_LOG_DEBUG, "Default SO_KEEPALIVE=%d", optval);

    int res;
    int on = 1;
    int tcp_keepalive_probes = 9;
    int tcp_keepalive_intvl = 75;
    int tcp_keepalive_time = seconds; // default 7200

    log_android(ANDROID_LOG_DEBUG, "Set TCP_KEEPCNT=%d", tcp_keepalive_probes);
    res = setsockopt(fd, SOL_TCP, TCP_KEEPCNT, &tcp_keepalive_probes, sizeof(tcp_keepalive_probes));
    if (res < 0)
        return errno;

    log_android(ANDROID_LOG_DEBUG, "Set TCP_KEEPINTVL=%d", tcp_keepalive_intvl);
    res = setsockopt(fd, SOL_TCP, TCP_KEEPINTVL, &tcp_keepalive_intvl, sizeof(tcp_keepalive_intvl));
    if (res < 0)
        return errno;

    log_android(ANDROID_LOG_DEBUG, "Set TCP_KEEPIDLE=%d", tcp_keepalive_time);
    res = setsockopt(fd, SOL_TCP, TCP_KEEPIDLE, &tcp_keepalive_time, sizeof(tcp_keepalive_time));
    if (res < 0)
        return errno;

    log_android(ANDROID_LOG_DEBUG, "Set SO_KEEPALIVE=%d", on);
    res = setsockopt(fd, SOL_SOCKET, SO_KEEPALIVE, &on, sizeof(on));
    if (res < 0)
        return errno;

    if (getsockopt(fd, SOL_TCP, TCP_KEEPCNT, &optval, &optlen) == 0)
        log_android(ANDROID_LOG_DEBUG, "Check TCP_KEEPCNT=%d", optval);
    if (getsockopt(fd, SOL_TCP, TCP_KEEPINTVL, &optval, &optlen) == 0)
        log_android(ANDROID_LOG_DEBUG, "Check TCP_KEEPINTVL=%d", optval);
    if (getsockopt(fd, SOL_TCP, TCP_KEEPIDLE, &optval, &optlen) == 0)
        log_android(ANDROID_LOG_DEBUG, "Check TCP_KEEPIDLE=%d", optval);
    if (getsockopt(fd, SOL_SOCKET, SO_KEEPALIVE, &optval, &optlen) == 0)
        log_android(ANDROID_LOG_DEBUG, "Check SO_KEEPALIVE=%d", optval);

    return res;
}

extern "C"
JNIEXPORT jint JNICALL
Java_eu_faircode_email_ConnectionHelper_jni_1socket_1get_1send_1buffer(
        JNIEnv *env, jclass clazz,
        jint fd) {
    int queued = 0;
    int res = ioctl(fd, TIOCOUTQ, &queued);
    if (res != 0)
        log_android(ANDROID_LOG_DEBUG, "ioctl(TIOCOUTQ) res=%d queued=%d", res, queued);
    return (res == 0 ? queued : 0);
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_eu_faircode_email_ConnectionHelper_jni_1is_1numeric_1address(
        JNIEnv *env, jclass clazz,
        jstring _ip) {
    jboolean numeric = 0;
    const char *ip = env->GetStringUTFChars(_ip, 0);

    // https://linux.die.net/man/3/getaddrinfo
    struct addrinfo hints;
    memset(&hints, 0, sizeof(struct addrinfo));
    hints.ai_family = AF_UNSPEC;
    hints.ai_flags = AI_NUMERICHOST; // suppresses any potentially lengthy network host address lookups
    struct addrinfo *result;
    int err = getaddrinfo(ip, nullptr, &hints, &result);
    if (err)
        log_android(ANDROID_LOG_DEBUG, "getaddrinfo(%s) error %d: %s", ip, err, gai_strerror(err));
    else
        numeric = (jboolean) (result != nullptr);

    if (result != nullptr)
        freeaddrinfo(result);

    env->ReleaseStringUTFChars(_ip, ip);
    return numeric;
}

extern "C"
JNIEXPORT void JNICALL
Java_eu_faircode_email_Helper_sync(JNIEnv *env, jclass clazz) {
    log_android(ANDROID_LOG_DEBUG, "sync");
    sync();
    log_android(ANDROID_LOG_DEBUG, "synced");
}
