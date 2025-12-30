cmake_minimum_required(VERSION 3.22.1)
project(RichText)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_C_STANDARD 11)
set(CMAKE_C_STANDARD_REQUIRED ON)

# Define paths
set(CPP_ROOT "${CMAKE_CURRENT_SOURCE_DIR}/../cpp")
set(ANDROID_CPP_DIR "${CMAKE_CURRENT_SOURCE_DIR}/src/main/cpp")

# Collect source files
file(GLOB MD4C_SOURCES "${CPP_ROOT}/md4c/*.c")
file(GLOB PARSER_SOURCES "${CPP_ROOT}/parser/*.cpp")

# Create shared library
add_library(${PROJECT_NAME} SHARED
    ${MD4C_SOURCES}
    ${PARSER_SOURCES}
    ${ANDROID_CPP_DIR}/jni-adapter.cpp
)

# Include directories
target_include_directories(${PROJECT_NAME} PRIVATE
    "${CPP_ROOT}/md4c"
    "${CPP_ROOT}/parser"
)

# Preprocessor definitions
target_compile_definitions(${PROJECT_NAME} PRIVATE
    MD4C_USE_UTF8=1
)

# Link against log library (for Android logging)
find_library(log-lib log)
target_link_libraries(${PROJECT_NAME} ${log-lib})

