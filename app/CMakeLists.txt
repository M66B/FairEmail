
# https://developer.android.com/studio/projects/configure-cmake

project(fairemail)
cmake_minimum_required(VERSION 3.4.1)

add_library(fairemail SHARED
        src/main/jni/fairemail.cc

        #https://github.com/google/compact_enc_det
        src/main/jni/compact_enc_det/compact_enc_det/compact_enc_det.cc
        src/main/jni/compact_enc_det/compact_enc_det/compact_enc_det_hint_code.cc
        src/main/jni/compact_enc_det/util/encodings/encodings.cc
        src/main/jni/compact_enc_det/util/languages/languages.cc

        #https://github.com/google/cld3
        src/main/jni/cld_3/src/base.cc
        src/main/jni/cld_3/src/embedding_feature_extractor.cc
        src/main/jni/cld_3/src/embedding_network.cc
        src/main/jni/cld_3/src/feature_extractor.cc
        src/main/jni/cld_3/src/feature_types.cc
        src/main/jni/cld_3/src/fml_parser.cc
        src/main/jni/cld_3/src/language_identifier_features.cc
        src/main/jni/cld_3/src/lang_id_nn_params.cc
        src/main/jni/cld_3/src/nnet_language_identifier.cc
        src/main/jni/cld_3/src/registry.cc
        src/main/jni/cld_3/src/relevant_script_feature.cc
        src/main/jni/cld_3/src/sentence_features.cc
        src/main/jni/cld_3/src/script_span/fixunicodevalue.cc
        src/main/jni/cld_3/src/script_span/generated_entities.cc
        src/main/jni/cld_3/src/script_span/generated_ulscript.cc
        src/main/jni/cld_3/src/script_span/getonescriptspan.cc
        src/main/jni/cld_3/src/script_span/offsetmap.cc
        src/main/jni/cld_3/src/script_span/text_processing.cc
        src/main/jni/cld_3/src/script_span/utf8statetable.cc
        src/main/jni/cld_3/src/task_context.cc
        src/main/jni/cld_3/src/task_context_params.cc
        src/main/jni/cld_3/src/unicodetext.cc
        src/main/jni/cld_3/src/utils.cc
        src/main/jni/cld_3/src/workspace.cc

        #protobuf 3.6.1.3
        src/main/jni/cld_3/protos/feature_extractor.pb.cc
        src/main/jni/cld_3/protos/sentence.pb.cc
        src/main/jni/cld_3/protos/task_spec.pb.cc

        #https://github.com/protocolbuffers/protobuf/
        src/main/jni/google/protobuf/stubs/bytestream.cc
        src/main/jni/google/protobuf/stubs/bytestream.h
        src/main/jni/google/protobuf/stubs/common.cc
        src/main/jni/google/protobuf/stubs/int128.cc
        src/main/jni/google/protobuf/stubs/io_win32.cc
        src/main/jni/google/protobuf/stubs/status.cc
        src/main/jni/google/protobuf/stubs/statusor.cc
        src/main/jni/google/protobuf/stubs/stringpiece.cc
        src/main/jni/google/protobuf/stubs/stringprintf.cc
        src/main/jni/google/protobuf/stubs/structurally_valid.cc
        src/main/jni/google/protobuf/stubs/strutil.cc
        src/main/jni/google/protobuf/stubs/time.cc
        src/main/jni/google/protobuf/arena.cc
        src/main/jni/google/protobuf/arenastring.cc
        src/main/jni/google/protobuf/extension_set.cc
        src/main/jni/google/protobuf/generated_message_util.cc
        src/main/jni/google/protobuf/generated_message_table_driven_lite.cc
        src/main/jni/google/protobuf/implicit_weak_message.cc
        src/main/jni/google/protobuf/message_lite.cc
        src/main/jni/google/protobuf/repeated_field.cc
        src/main/jni/google/protobuf/wire_format_lite.cc
        src/main/jni/google/protobuf/io/coded_stream.cc
        src/main/jni/google/protobuf/io/zero_copy_stream.cc
        src/main/jni/google/protobuf/io/zero_copy_stream_impl_lite.cc
)

include_directories(
        src/main/jni
        src/main/jni/compact_enc_det
)

find_library(log-lib log)

add_definitions(-DHAVE_MEMRCHR -DHAVE_PTHREAD)

target_compile_options(fairemail PRIVATE -Wno-c++11-narrowing -Wno-undefined-var-template)

target_link_libraries(fairemail ${log-lib})
