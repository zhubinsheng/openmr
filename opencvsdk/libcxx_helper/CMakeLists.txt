cmake_minimum_required(VERSION 3.6)
set(CMAKE_VERBOSE_MAKEFILE on)
#要将 ADD_EXECUTABLE的声明放到设置库文件的TARGET_LINK_LIBRARIES之前
add_library(libopencv_java4 SHARED IMPORTED)
set_target_properties(libopencv_java4
        PROPERTIES IMPORTED_LOCATION
        ${PROJECT_SOURCE_DIR}/../native/libs/arm64-v8a/libopencv_java4.so)

include_directories(${PROJECT_SOURCE_DIR}/../native/jni/include)

message("========= ${ANDROID_ABI} =========")
message("========= ${CMAKE_ANDROID_ARCH_ABI} =========")

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

# 查找source目录下的所有*.cpp,*.c源文件,并将文件列表保存到 DIR_LIB_SRCS 变量
file(GLOB_RECURSE SRC_DIR_LIST "./*.cpp" "./*.h")

add_library(pnp SHARED ${SRC_DIR_LIST})

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        pnp
        libopencv_java4
        #opencv_java4
        # Links the target library to the log library
        # included in the NDK.
        ${log-lib}
        android
        jnigraphics
        )
