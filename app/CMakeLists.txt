
# https://developer.android.com/studio/projects/configure-cmake

cmake_minimum_required(VERSION 3.4.1)

add_library(fairemail SHARED
	src/main/jni/fairemail.cc
	src/main/jni/compact_enc_det/compact_enc_det/compact_enc_det.cc
	src/main/jni/compact_enc_det/compact_enc_det/compact_enc_det_hint_code.cc
	src/main/jni/compact_enc_det/util/encodings/encodings.cc
	src/main/jni/compact_enc_det/util/languages/languages.cc
)

include_directories(src/main/jni/compact_enc_det)

find_library(log-lib log)

add_definitions(-DHAVE_MEMRCHR)

target_compile_options(fairemail PRIVATE -Wno-c++11-narrowing)

target_link_libraries(fairemail ${log-lib})
