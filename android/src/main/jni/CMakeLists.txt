cmake_minimum_required(VERSION 3.13)
set(CMAKE_VERBOSE_MAKEFILE ON)

set(LIB_LITERAL RichTextViewSpec)
set(LIB_TARGET_NAME react_codegen_${LIB_LITERAL})

set(LIB_ANDROID_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../../..)
set(LIB_ANDROID_GENERATED_JNI_DIR ${LIB_ANDROID_DIR}/generated/jni)
set(LIB_ANDROID_GENERATED_COMPONENTS_DIR ${LIB_ANDROID_GENERATED_JNI_DIR}/react/renderer/components/${LIB_LITERAL})

# Parser sources
set(CPP_ROOT "${LIB_ANDROID_DIR}/../cpp")
set(ANDROID_CPP_DIR "${LIB_ANDROID_DIR}/src/main/cpp")

file(GLOB MD4C_SOURCES "${CPP_ROOT}/md4c/*.c")
file(GLOB PARSER_SOURCES "${CPP_ROOT}/parser/*.cpp")

# Build md4c as a separate static library with C flags
add_library(md4c_lib STATIC ${MD4C_SOURCES})
target_include_directories(md4c_lib PUBLIC ${CPP_ROOT}/md4c)
target_compile_definitions(md4c_lib PRIVATE MD4C_USE_UTF8=1)
set_target_properties(md4c_lib PROPERTIES
  C_STANDARD 11
  C_STANDARD_REQUIRED ON
)

# All cpp sources from the generated directory (includes both codegen and our custom files)
file(GLOB LIB_CODEGEN_SRCS CONFIGURE_DEPENDS ${LIB_ANDROID_GENERATED_JNI_DIR}/*.cpp ${LIB_ANDROID_GENERATED_COMPONENTS_DIR}/*.cpp)

add_library(
  ${LIB_TARGET_NAME}
  SHARED
  ${LIB_CODEGEN_SRCS}
  ${PARSER_SOURCES}
  ${ANDROID_CPP_DIR}/jni-adapter.cpp
)

target_include_directories(
  ${LIB_TARGET_NAME}
  PUBLIC
  ${LIB_ANDROID_GENERATED_JNI_DIR}
  ${LIB_ANDROID_GENERATED_COMPONENTS_DIR}
  ${CPP_ROOT}/md4c
  ${CPP_ROOT}/parser
)

target_compile_definitions(${LIB_TARGET_NAME} PRIVATE
  MD4C_USE_UTF8=1
)

find_library(log-lib log)

if (REACTNATIVE_MERGED_SO)
  target_link_libraries(
    ${LIB_TARGET_NAME}
    ${log-lib}
    md4c_lib
    fbjni
    jsi
    reactnative
  )
else()
  target_link_libraries(
    ${LIB_TARGET_NAME}
    ${log-lib}
    md4c_lib
    fbjni
    folly_runtime
    glog
    jsi
    react_codegen_rncore
    react_debug
    react_nativemodule_core
    react_render_core
    react_render_debug
    react_render_graphics
    react_render_mapbuffer
    react_render_componentregistry
    react_utils
    rrc_view
    turbomodulejsijni
    yoga
  )
endif()

target_include_directories(
  ${CMAKE_PROJECT_NAME}
  PUBLIC
  ${CMAKE_CURRENT_SOURCE_DIR}
)

if(ReactAndroid_VERSION_MINOR GREATER_EQUAL 80)
  target_compile_reactnative_options(${LIB_TARGET_NAME} PUBLIC)
else()
  target_compile_options(
    ${LIB_TARGET_NAME}
    PRIVATE
    -fexceptions
    -frtti
    -std=c++20
    -Wall
  )
endif()
